import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './Profile.css';

const Profile = () => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  const navigate = useNavigate();
  const [userInfo, setUserInfo] = useState({
    email: '',
    nickname: '',
    profile: ''
  });
  const [loading, setLoading] = useState(false);
  const [myPosts, setMyPosts] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const maxPageButtons = 5;

  // 내가 쓴 글 목록
  const fetchMyPosts = useCallback(async (page) => {
    try {
      const response = await api.get(`/v1/members/posts?page=${page}`);
      const pageResult = response.data.data;

      setMyPosts(pageResult.content);
      setCurrentPage(pageResult.currentPage);
      setTotalPages(pageResult.totalPages);
      setTotalElements(pageResult.totalElements);
    } catch (error) {
      console.error('내 글 목록 조회 중 오류가 발생했습니다:', error);
    }
  }, []);

  // 내 정보 조회
  useEffect(() => {
    const fetchUserInfo = async () => {
      setLoading(true);
      try {
        const userResponse = await api.get('/v1/members/me', { skipGlobalError: true });
        const memberData = userResponse.data.data;

        setUserInfo({
          email: memberData.email,
          nickname: memberData.nickname,
          profile: memberData.profileImageUrl 
            ? `${baseUrl}${memberData.profileImageUrl}` 
            : ''
        });

        // 회원 정보 조회가 성공하면 첫 페이지 글 목록 조회
        fetchMyPosts(1);
      } catch (error) {
        console.error('내 정보 조회 중 오류가 발생했습니다:', error);
        
        // 존재하지 않는 회원(DB 초기화 등)일 경우 에러창 1번만 띄우고 토큰 정리 후 홈으로 이동
        alert(error.response?.data?.message || '존재하지 않는 회원입니다.');
        localStorage.removeItem('itBlogAccessToken');
        navigate('/');
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, [baseUrl, fetchMyPosts, navigate]);

  // 페이지 버튼 클릭 시 호출되는 핸들러
  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    fetchMyPosts(newPage);
  };

  // 페이지네이션 관련 변수
  const currentBlock = Math.ceil(currentPage / maxPageButtons);
  const startPage = (currentBlock - 1) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  const pageNumbers = [];
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  // 회원 탈퇴
  const handleWithdraw = async () => {
    if (window.confirm('정말 회원 탈퇴를 하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      try {
        await api.delete('/v1/members');

        // 2. 로그아웃 API 호출 (서버 쿠키 및 블랙리스트 정리를 확실하게 보장)
        try {
          await api.post('/v1/auth/logout');
        } catch (logoutError) {
          console.warn('로그아웃 처리 중 부가적인 오류 (무시 가능):', logoutError);
        }

        // 3. 클라이언트 토큰 정리 및 홈 이동
        localStorage.removeItem('itBlogAccessToken');
        alert('회원 탈퇴가 완료되었습니다.');
        window.location.href = '/';
      } catch (error) {
        console.error('Error withdrawing user:', error);
        alert(error.response?.data?.message || '회원 탈퇴 중 오류가 발생했습니다.');
      }
    }
  };

  // 작성한 글 삭제
  const handleDeletePost = async (postId) => {
    if (window.confirm('글을 삭제하시겠습니까?')) {
      try {
        await api.delete(`/v1/posts/${postId}`);

        // 현재 페이지의 목록 개수를 확인하여 페이지 이동 처리
        // 현재 페이지에 글이 1개밖에 없고, 2페이지 이상이었다면 이전 페이지로 이동
        if (myPosts.length === 1 && currentPage > 1) {
          const prevPage = currentPage - 1;
          setCurrentPage(prevPage);
          fetchMyPosts(prevPage);
        } else {
          // 그 외에는 현재 페이지 다시 불러오기
          fetchMyPosts(currentPage);
        }
        
        alert('글이 삭제되었습니다.');
      } catch (error) {
        console.error('글 삭제 중 오류가 발생했습니다:', error);
        alert(error.response?.data?.message || '글 삭제 중 오류가 발생했습니다.');
      }
    }
  };

  // 로딩 처리
  if (loading) {
    return <div className="profile-loading">로딩 중...</div>;
  }

  return (
    <div className="profile-wrapper">
      <div className="profile-container">
        <div className="my-profile-section">
          <h2>내 정보</h2>
          <div className="profile-card">
            <div className="profile-image-section">
              <img
                src={userInfo.profile || 'https://via.placeholder.com/120'}
                alt="프로필 이미지"
                className="profile-avatar"
              />
            </div>
            <div className="profile-info-section">
              <div className="info-group">
                <label>이메일</label>
                <p>{userInfo.email}</p>
              </div>
              <div className="info-group">
                <label>닉네임</label>
                <p>{userInfo.nickname}</p>
              </div>
            </div>
          </div>
          <div className="profile-actions">
            <Link to="/" className="btn-home">
              홈으로
            </Link>
            <button onClick={handleWithdraw} className="btn-withdraw">
              회원 탈퇴
            </button>
          </div>
        </div>
        <hr className="my-profile-divider" />
        {/* 내가 쓴 글 */}
        <div className="my-posts-section">
          <h3>내가 쓴 글 ({totalElements})</h3>
          <div className="posts-table-container">
            {myPosts.length > 0 ? (
              <>
                <table className="posts-table">
                  <thead>
                    <tr>
                      <th>제목</th>
                      <th className="col-likes">좋아요</th>
                      <th className="col-views">조회수</th>
                      <th className="col-date">작성일</th>
                      <th className="col-actions">관리</th>
                    </tr>
                  </thead>
                  <tbody>
                    {myPosts.map((post) => (
                      <tr key={post.id}>
                        <td className="col-title">
                          <Link to={`/post/view?id=${post.id}`} title={post.title}>
                            {post.title}
                          </Link>
                        </td>
                        <td className="col-likes">{post.likes}</td>
                        <td className="col-views">{post.hits}</td>
                        <td className="col-date">
                          {post.registeredAt ? new Date(post.registeredAt).toLocaleDateString() : ''}
                        </td>
                        <td className="col-actions">
                          <button
                            onClick={() => handleDeletePost(post.id)}
                            className="btn-post-delete"
                          >
                            삭제
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {/* 페이지네이션 */}
                {totalPages > 1 && (
                  <div className="pagination">
                    {startPage > 1 && (
                      <button
                        onClick={() => handlePageChange(startPage - 1)}
                        className="page-btn"
                      >
                        <i className="fa-solid fa-angle-left"></i>
                      </button>
                    )}
                    {pageNumbers.map((number) => (
                      <button
                        key={number}
                        onClick={() => handlePageChange(number)}
                        className={`page-btn ${currentPage === number ? 'active' : ''}`}
                      >
                        {number}
                      </button>
                    ))}
                    {endPage < totalPages && (
                      <button
                        onClick={() => handlePageChange(endPage + 1)}
                        className="page-btn"
                      >
                        <i className="fa-solid fa-angle-right"></i>
                      </button>
                    )}
                  </div>
                )}
              </>
            ) : (
              <div className="posts-empty">작성한 글이 없습니다</div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
};

export default Profile;
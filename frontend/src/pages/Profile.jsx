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

  // 내가 쓴 글 목록 및 페이징 상태
  const [myPosts, setMyPosts] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const maxPageButtons = 5; // 한 화면에 보여줄 최대 페이지 버튼 수

  // 서버에서 내 글 목록을 가져오는 함수
  const fetchMyPosts = useCallback(async (page) => {
    try {
      const response = await api.get(`/v1/members/posts?page=${page}`);
      const pageResult = response.data.data; // PageResult<PostDetail>

      setMyPosts(pageResult.content);
      setCurrentPage(pageResult.currentPage);
      setTotalPages(pageResult.totalPages);
      setTotalElements(pageResult.totalElements);
    } catch (error) {
      console.error('내 글 목록 조회 중 오류가 발생했습니다:', error);
    }
  }, []);

  // 1. 컴포넌트 마운트 시 '내 정보' 최초 1회 조회 및 글 목록 연동
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

        // 💡 회원 정보 조회가 성공했을 때만 첫 페이지 글 목록 조회 실행
        fetchMyPosts(1);

      } catch (error) {
        console.error('내 정보 조회 중 오류가 발생했습니다:', error);
        
        // 💡 존재하지 않는 회원(DB 초기화 등)일 경우 에러창 1번만 띄우고 토큰 정리 후 홈으로 이동
        alert(error.response?.data?.message || '존재하지 않는 회원입니다.');
        localStorage.removeItem('itBlogAccessToken');
        navigate('/');
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, [baseUrl, fetchMyPosts, navigate]);

  // 페이지 버튼 클릭 시 호출되는 핸들러 (안전한 페이지 변경 및 글 목록 조회)
  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
    fetchMyPosts(newPage);
  };

  // 페이지 버튼 그룹 계산
  const currentBlock = Math.ceil(currentPage / maxPageButtons);
  const startPage = (currentBlock - 1) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  const pageNumbers = [];
  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  // 회원 탈퇴 핸들러
  const handleWithdraw = async () => {
    if (window.confirm('정말 회원 탈퇴를 하시겠습니까? 이 작업은 되돌릴 수 없습니다')) {
      try {
        await api.delete('/v1/members');
        localStorage.removeItem('itBlogAccessToken');
        alert('회원 탈퇴가 완료되었습니다');
        navigate('/');
      } catch (error) {
        console.error('Error withdrawing user:', error);
        alert('회원 탈퇴 중 오류가 발생했습니다');
      }
    }
  };

  // 작성한 글 삭제 핸들러
  const handleDeletePost = async (postId) => {
    if (window.confirm('글을 삭제하시겠습니까?')) {
      try {
        // 필요 시 백엔드 글 삭제 API 연동: await api.delete(`/v1/posts/${postId}`);
        
        const updatedPosts = myPosts.filter((post) => post.id !== postId);
        setMyPosts(updatedPosts);

        // 현재 페이지의 유일한 글을 지웠고 1페이지보다 크면 이전 페이지로 이동, 아니면 현재 페이지 재조회
        if (updatedPosts.length === 0 && currentPage > 1) {
          const prevPage = currentPage - 1;
          setCurrentPage(prevPage);
          fetchMyPosts(prevPage);
        } else {
          fetchMyPosts(currentPage);
        }
        
        alert('글이 삭제되었습니다.');
      } catch (error) {
        console.error('글 삭제 중 오류가 발생했습니다:', error);
        alert('글 삭제 중 오류가 발생했습니다.');
      }
    }
  };

  if (loading) {
    return <div className="profile-loading">로딩 중...</div>;
  }

  return (
    <div className="profile-wrapper">
      <div className="profile-container">
        
        {/* 1. 내 정보 섹션 */}
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
        
        {/* 2. 내가 쓴 글 목록 섹션 */}
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

                {/* 페이지네이션 버튼 영역 */}
                {totalPages > 1 && (
                  <div className="pagination">
                    {/* 첫 번째 세션(startPage가 1)이 아닐 때만 < 버튼 표시 */}
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

                    {/* 마지막 세션(endPage가 totalPages와 같지 않음)일 때만 > 버튼 표시 */}
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
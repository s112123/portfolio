import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './Profile.css';

const Profile = () => {
  const navigate = useNavigate();
  const [userInfo, setUserInfo] = useState({
    email: '',
    nickname: '',
    profile: ''
  });
  const [loading, setLoading] = useState(false);

  // 컴포넌트 마운트 시 내 정보 조회
  useEffect(() => {
    const fetchUserInfo = async () => {
      setLoading(true);

      try {
        const response = await api.get('/v1/members/me');
        const memberData = response.data.data;

        setUserInfo({
          email: memberData.email,
          nickname: memberData.nickname,
          // 프로필 이미지 URL이 상대 경로로 올 경우 백엔드 주소 결합
          profile: memberData.profileImage?.url 
            ? `http://localhost:8080${memberData.profileImage.url}` 
            : ''
        });
      } catch (error) {
        console.error('내 정보 조회 중 오류가 발생했습니다:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserInfo();
  }, []);

  const handleWithdraw = async () => {
    if (window.confirm('정말 회원 탈퇴를 하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      try {
        // 백엔드 회원 탈퇴 API 호출 (필요시 경로 확인: /v1/members 또는 /members)
        await api.delete('/v1/members');

        localStorage.removeItem('itBlogAccessToken');
        alert('회원 탈퇴가 완료되었습니다.');
        navigate('/');
      } catch (error) {
        console.error('Error withdrawing user:', error);
        alert('회원 탈퇴 중 오류가 발생했습니다.');
      }
    }
  };

  if (loading) {
    return <div className="profile-loading">로딩 중...</div>;
  }

  return (
    <div className="profile-wrapper">
      <div className="profile-container">
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
          <Link to="/" className="btn-home">홈으로</Link>
          <button onClick={handleWithdraw} className="btn-withdraw">
            회원 탈퇴
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile;
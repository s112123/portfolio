import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Header from '../components/Header'; 

const ServerCheckLayout = () => {
  // null: 체크 중, true: 정상, false: 다운
  const [isServerUp, setIsServerUp] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const checkServer = async () => {
      try {
        // 헬스체크 API 호출
        await api.get('/v1/health'); 
        setIsServerUp(true);
      } catch (error) {
        if (!error.response) {
          // 서버 연결 실패인 경우
          setIsServerUp(false);
          navigate('/error', { replace: true });
        } else {
          // 서버는 켜져 있으나 다른 응답 에러인 경우는 진입 허용
          setIsServerUp(true);
        }
      }
    };

    checkServer();
  }, [navigate]);
  // 서버 상태 확인 중일 때 보여줄 로딩 화면 (깜빡임 방지)
  if (isServerUp === null) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <p>서버 연결 상태 확인 중...</p>
      </div>
    );
  }

  // 서버가 정상인 경우, 렌더링
  return (
    <>
      <Header />
      <Outlet />
    </>
  );
};

export default ServerCheckLayout;
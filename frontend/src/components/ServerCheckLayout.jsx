import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Header from '../components/Header'; 

const ServerCheckLayout = () => {
  const [isServerUp, setIsServerUp] = useState(null); // null: 체크 중, true: 정상, false: 다운
  const navigate = useNavigate();

  useEffect(() => {
    const checkServer = async () => {
      try {
        // 백엔드 헬스체크 API 호출 (퍼블릭 경로 또는 적절한 엔드포인트)
        await api.get('/v1/health'); 
        setIsServerUp(true);
      } catch (error) {
        // 서버 연결 실패 (!error.response)인 경우
        if (!error.response) {
          setIsServerUp(false);
          navigate('/error', { replace: true }); // 에러 페이지로 강제 이동
        } else {
          // 서버는 켜져 있으나 다른 응답 에러(404, 500 등)인 경우는 진입 허용
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

  // 서버가 정상일 때 Header와 각 페이지들을 렌더링
  return (
    <>
      <Header />
      <Outlet />
    </>
  );
};

export default ServerCheckLayout;
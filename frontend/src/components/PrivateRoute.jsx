import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

export default function PrivateRoute() {
  // 로컬 스토리지나 상태 관리소에 Access Token 이 있는지 확인
  const token = localStorage.getItem('itBlogAccessToken');

  // 토큰이 없으면 로그인 페이지로 강제 리다이렉트
  if (!token) {
    alert('로그인이 필요한 서비스입니다.');
    return <Navigate to="/login" replace />;
  }

  // 토큰이 있으면 해당 자식 컴포넌트(글쓰기 페이지 등)를 정상적으로 렌더링
  return <Outlet />;
}
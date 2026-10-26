import React, { useState, useEffect } from 'react';
import './Header.css';

const Header = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    setIsLoggedIn(!!token);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    setIsLoggedIn(false);
    alert('로그아웃 되었습니다');
    window.location.href = '/';
  };

  return (
    <header className="header">
      <div className="container header-container">
        <div className="logo">
          <a href="/" className="logo-link">IT Blog</a>
        </div>
        <nav className="nav">
          <a href="/" className="nav-item">홈</a>
          <a href="/members" className="nav-item">회원목록</a>
        </nav>
        <div className="auth-area">
          {
            isLoggedIn ? (
              <>
                <a href="/profile" className="btn-secondary">내 정보</a>
                <button onClick={handleLogout} className="btn-primary">로그아웃</button>
              </>
            ) : (
              <>
                <a href="/login" className="btn-link">로그인</a>
                <a href="/signup" className="btn-primary">회원가입</a>
              </>
            )
          }
        </div>
      </div>
    </header>
  );
};

export default Header;
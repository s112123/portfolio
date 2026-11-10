import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import api from '../api/axios';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [isMessageOpen, setIsMessageOpen] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [chatTab, setChatTab] = useState('friend-list-container');
  const [activeChatRoom, setActiveChatRoom] = useState(null);
  const [searchTerm, setSearchTerm] = useState(searchParams.get('keyword') || '');
  const notificationRef = useRef(null);
  const messageRef = useRef(null);

  // URL 쿼리 스트링의 keyword 와 검색어 입력란 동기화
  useEffect(() => {
    setSearchTerm(searchParams.get('keyword') || '');
  }, [searchParams]);

  // 로그인 상태 여부
  useEffect(() => {
    checkLoginStatus();

    window.addEventListener('storage', checkLoginStatus);
    window.addEventListener('authChange', checkLoginStatus);

    return () => {
      window.removeEventListener('storage', checkLoginStatus);
      window.removeEventListener('authChange', checkLoginStatus);
    };
  }, []);

  // 알림, 채팅 드랍다운이 열렸을 때, 창 외부 클릭시 닫기 처리
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setIsNotificationOpen(false);
      }
      if (messageRef.current && !messageRef.current.contains(event.target)) {
        setIsMessageOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // 로그인 상태
  const checkLoginStatus = () => {
    const token = localStorage.getItem('itBlogAccessToken');
    setIsLoggedIn(!!token);
  };

  // 로그아웃
  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      await api.post('/v1/auth/logout');
    } catch (error) {
      console.error('로그아웃 요청 중 에러 발생:', error);
    } finally {
      localStorage.removeItem('itBlogAccessToken');
      setIsLoggedIn(false);
      window.dispatchEvent(new Event('authChange'));
      navigate('/');
    }
  };

  // 검색
  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const trimmedKeyword = searchTerm.trim();
    if (trimmedKeyword) {
      navigate(`/?keyword=${encodeURIComponent(trimmedKeyword)}`);
    } else {
      navigate(`/`);
    }
  };

  return (
    <>
      <header className="header">
        <div className="container header-container">
          {/* 로고 */}
          <div className="logo" id="logo">
            <Link to="/" 
                  className="logo-link"
                  onClick={(e) => {
                    if (window.location.pathname === '/') {
                      e.preventDefault();
                      window.location.href = '/';
                    }
                  }}
            >
              It's blog
            </Link>
          </div>
          {/* 검색 */}
          <form onSubmit={handleSearchSubmit} className="search-box">
            <input 
              type="text" 
              id="search" 
              placeholder="Search..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <button type="submit" id="btn-search">
              <i className="fa-solid fa-magnifying-glass"></i>
            </button>
          </form>
          {/* 네비게이션 */}
          <nav className="nav">
            <ul>
              {isLoggedIn ? (
                <>
                  <li id="notification" className={isNotificationOpen ? 'active' : ''} ref={notificationRef}>
                    <a
                      href="# "
                      onClick={(e) => {
                        e.preventDefault();
                        setIsNotificationOpen(!isNotificationOpen);
                        setIsMessageOpen(false);
                      }}
                    >
                      <i className="fa-regular fa-bell" id="bell"></i>
                    </a>
                    <div id="notification-module" className={`module ${isNotificationOpen ? 'active' : ''}`}>
                      <ul id="notification-list">
                        <div className="notification-no-data">새로운 알림이 없습니다</div>
                      </ul>
                      <button type="button" id="btn-notification-more">전체 알림</button>
                    </div>
                  </li>
                  <li id="message" className={isMessageOpen ? 'active' : ''} ref={messageRef}>
                    <a
                      href="# "
                      onClick={(e) => {
                        e.preventDefault();
                        setIsMessageOpen(!isMessageOpen);
                        setIsNotificationOpen(false);
                      }}
                    >
                      <i className="fa-regular fa-message" id="message-icon"></i>
                    </a>
                    <div id="message-module" className={`module ${isMessageOpen ? 'active' : ''}`}>
                      <ul id="message-list">
                        <div className="notification-message-no-data">새로운 메시지가 없습니다</div>
                      </ul>
                      <button
                        type="button"
                        id="btn-message-more"
                        onClick={() => {
                          setIsMessageOpen(false);
                          setIsChatOpen(true);
                        }}
                      >
                        전체 메세지 / 채팅 열기
                      </button>
                    </div>
                  </li>
                  <li id="profile">
                    <Link to="/profile">
                      <i className="fa-solid fa-user"></i>
                    </Link>
                  </li>
                  <li>
                    <a href="# " id="logout" onClick={handleLogout}>
                      <i className="fa-solid fa-right-from-bracket"></i>
                    </a>
                  </li>
                </>
              ) : (
                <div className="auth-area">
                  <Link to="/login" className="btn-link">로그인</Link>
                  <Link to="/signup" className="btn-primary">회원가입</Link>
                </div>
              )}
            </ul>
          </nav>
        </div>
      </header>
      {/* 채팅 목록 */}
      {isLoggedIn && (
        <div id="chat-window" className={`draggable-module ${isChatOpen ? 'active' : ''}`}>
          <div id="chat-header">
            <span className="chat-title">채팅</span>
            <div className="chat-header-ops">
              <button type="button" id="btn-chat-close" onClick={() => setIsChatOpen(false)}>
                <i className="fa-solid fa-xmark"></i>
              </button>
            </div>
          </div>
          <div className="chat-tabs">
            <div
              className={`chat-tab ${chatTab === 'friend-list-container' ? 'active' : ''}`}
              onClick={() => {
                setChatTab('friend-list-container');
                setActiveChatRoom(null);
              }}
            >
              친구목록
            </div>
            <div
              className={`chat-tab ${chatTab === 'chat-list-container' ? 'active' : ''}`}
              onClick={() => {
                setChatTab('chat-list-container');
                setActiveChatRoom(null);
              }}
            >
              대화목록
            </div>
          </div>
          <div id="chat-body">
            {!activeChatRoom ? (
              <div id="list-view">
                <div id="friend-list-container" className={`tab-content ${chatTab === 'friend-list-container' ? 'active' : ''}`}>
                  <ul className="chat-user-list" id="chat-user-list">
                    <div className="chat-user-list-no-data">등록된 친구가 없습니다</div>
                  </ul>
                </div>
                <div id="chat-list-container" className={`tab-content ${chatTab === 'chat-list-container' ? 'active' : ''}`}>
                  <ul className="chat-user-list" id="chat-list">
                    <div className="chat-list-no-data">대화 중인 내역이 없습니다</div>
                  </ul>
                </div>
              </div>
            ) : (
              <div id="chat-room-view" className="chat-room-view">
                <div className="chat-room-header">
                  <button type="button" id="btn-back" onClick={() => setActiveChatRoom(null)}>
                    <i className="fa-solid fa-chevron-left"></i>
                  </button>
                  <div className="target-info" id="target-info">
                    <img src={activeChatRoom.image || '#'} id="target-image" alt="target" />
                    <span id="target-name">{activeChatRoom.name}</span>
                  </div>
                </div>
                <div className="chat-messages" id="chat-messages">
                  <div id="chat-message-scroll" style={{ height: '1px' }}></div>
                  <div id="chat-message-container"></div>
                </div>
                <div className="chat-input-area">
                  <input type="text" id="message-content" placeholder="메시지를 입력하세요..." />
                  <button id="btn-send-message">전송</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </>
  );
};

export default Header;
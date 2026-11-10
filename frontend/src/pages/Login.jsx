import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axios';
import './Login.css';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [globalError, setGlobalError] = useState('');
  const [showWelcome, setShowWelcome] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const emailInputRef = useRef(null);

  // URL 쿼리스트링으로 "new" 가 있는지 확인 → 신규가입 회원
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('new') !== null) {
      setShowWelcome(true);
    }
  }, [location]);

  // 이메일 유효성 검사
  const validateEmail = (value) => {
    if (value.trim().length === 0) {
      setEmailError('이메일을 입력하세요');
      return false;
    }
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (value.trim().includes(' ') || !emailRegex.test(value.trim())) {
      setEmailError('이메일 형식이 아닙니다');
      return false;
    }
    setEmailError('');
    return true;
  };

  // 비밀번호 유효성 검사
  const validatePassword = (value) => {
    if (value.trim().length === 0) {
      setPasswordError('비밀번호를 입력하세요');
      return false;
    }
    setPasswordError('');
    return true;
  };

  // 로그인
  const handleLogin = async (e) => {
    e.preventDefault();
    
    // 요청 전 에러 초기화
    setEmailError('');
    setPasswordError('');
    setGlobalError('');

    if (!validateEmail(email)) return;
    if (!validatePassword(password)) return;

    try {
      const response = await api.post('/v1/auth/login', {
        email: email.trim(),
        password: password.trim(),
      });
      
      if (response.data.success) {
        const token = response.data.data;
        localStorage.setItem('itBlogAccessToken', token);
        
        navigate('/');
        window.location.href = '/';
      }
    } catch (error) {
      console.error('로그인 실패 응답:', error.response);
      const errorData = error.response?.data;
      const messages = errorData?.message;

      let handled = false;
      if (Array.isArray(messages)) {
        messages.forEach((msgItem) => {
          if (typeof msgItem === 'string' && msgItem.includes(':')) {
            const colonIndex = msgItem.indexOf(':');
            const field = msgItem.substring(0, colonIndex).trim().toLowerCase();
            const msg = msgItem.substring(colonIndex + 1).trim();

            if (field.includes('email')) {
              setEmailError(msg);
              handled = true;
            } else if (field.includes('password') || field.includes('pwd')) {
              setPasswordError(msg);
              handled = true;
            }
          }
        });
      } else if (typeof messages === 'string' && messages.includes(':')) {
        const colonIndex = messages.indexOf(':');
        const field = messages.substring(0, colonIndex).trim().toLowerCase();
        const msg = messages.substring(colonIndex + 1).trim();

        if (field.includes('email')) {
          setEmailError(msg);
          handled = true;
        } else if (field.includes('password') || field.includes('pwd')) {
          setPasswordError(msg);
          handled = true;
        }
      }

      // 필드 에러가 아닌 일반 전역 에러인 경우
      if (!handled) {
        const fallbackMsg = Array.isArray(messages) ? messages.join(', ') : (messages || '이메일 또는 비밀번호가 올바르지 않습니다');
        setGlobalError(fallbackMsg);
      }
    }
  };

  // 비밀번호 찾기
  const handleFindPassword = async () => {
    if (email.trim().length === 0) {
      alert('임시 비밀번호를 전송할 이메일 주소를 입력해주세요');
      setEmailError('이메일을 입력하세요');
      if (emailInputRef.current) {
        emailInputRef.current.focus();
      }
      return;
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (email.trim().includes(' ') || !emailRegex.test(email.trim())) {
      alert('이메일 주소 형식이 아닙니다');
      setEmailError('이메일 형식이 아닙니다');
      return;
    }

    if (confirm('입력하신 이메일 주소로 임시 비밀번호를 전송하시겠습니까?')) {
      try {
        const response = await api.patch(
          '/v1/members/change-password', 
          { email: email.trim() },
          { skipGlobalError: true }
        );
        alert('임시 비밀번호가 발송되었습니다');
        if (response.data.data && response.data.data) {
          alert('임시 비밀번호: ' + response.data.data);
        }
      } catch (error) {
        const status = error.response?.status;
        alert(status === 404 ? '가입되지 않은 이메일 주소입니다' : '전송 실패');
      }
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-container">
        {showWelcome && (
          <div id="welcome-message">
            회원가입을 환영합니다<br />로그인 후, 서비스를 이용해주시기 바랍니다
          </div>
        )}
        <div className="sign-in-box">
          <h2>로그인</h2>
          <form onSubmit={handleLogin} className="login-form">
            <div className="sign-in-item">
              <input
                type="text"
                id="email"
                ref={emailInputRef}
                placeholder="이메일"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  validateEmail(e.target.value);
                }}
                className={`login-input ${emailError ? 'error-input' : ''}`}
              />
              {emailError && <small className="error" style={{ display: 'block' }}>{emailError}</small>}
            </div>
            <div className="sign-in-item">
              <input
                type="password"
                id="password"
                placeholder="비밀번호"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  validatePassword(e.target.value);
                }}
                className={`login-input ${passwordError ? 'error-input' : ''}`}
              />
              {passwordError && <small className="error" style={{ display: 'block' }}>{passwordError}</small>}
            </div>
            {globalError && <small className="error global-error" style={{ display: 'block' }}>{globalError}</small>}
            <button type="submit" id="btn-sign-in" className="login-btn">로그인</button>
          </form>
          <p className="sign-in-box-more-action">
            <a href="/signup">회원가입</a>
            <span className="more-action-divider"></span>
            <a href="#/" onClick={(e) => { e.preventDefault(); handleFindPassword(); }}>
              비밀번호 찾기
            </a>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
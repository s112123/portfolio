// src/pages/Signup.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './Signup.css';

const Signup = () => {
  const navigate = useNavigate();

  // 1. 입력값 상태 (Form Data)
  const [form, setForm] = useState({
    email: '',
    password: '',
    rePassword: '',
    confirmCode: '',
  });

  // 2. 에러 및 UI 상태
  const [errors, setErrors] = useState({
    email: '',
    password: '',
    rePassword: '',
    confirmCode: '',
  });

  const [uiState, setUiState] = useState({
    showConfirmBox: false,
    sendMessage: '',
  });

  // 입력값 변경 핸들러 (공통)
  const handleChange = (e) => {
    const { id, value } = e.target;
    setForm((prev) => ({ ...prev, [id]: value }));
    // 입력 시 해당 필드의 에러 초기화
    setErrors((prev) => ({ ...prev, [id]: '' }));
  };

  // 개별 유효성 검사 함수들
  const validate = {
    email: (value) => {
      if (!value.trim()) return '이메일을 입력하세요';
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (value.includes(' ') || !emailRegex.test(value)) return '이메일 형식이 아닙니다';
      return '';
    },
    password: (value) => {
      if (!value.trim()) return '비밀번호를 입력하세요';
      if (value.length < 4 || value.length > 8) return '비밀번호는 4~8자로 입력하세요';
      return '';
    },
    rePassword: (reVal, pwVal) => {
      if (!reVal.trim()) return '비밀번호를 확인해주세요';
      if (reVal !== pwVal) return '비밀번호가 일치하지 않습니다';
      return '';
    },
    confirmCode: (value) => {
      if (!value.trim()) return '인증 코드를 입력하세요';
      return '';
    },
  };

  // [이벤트] 인증코드 전송 버튼 클릭
  const handleSendMail = async () => {
    const emailError = validate.email(form.email);
    if (emailError) {
      setErrors((prev) => ({ ...prev, email: emailError }));
      return;
    }

    try {
      const response = await api.post('/mail/send', { email: form.email.trim() });
      
      setUiState((prev) => ({
        ...prev,
        showConfirmBox: true,
        sendMessage: '인증 메일이 발송되었습니다.',
      }));
      setForm((prev) => ({ ...prev, confirmCode: '' }));
      setErrors((prev) => ({ ...prev, confirmCode: '' }));

      // 개발/테스트용 코드 알림
      if (response.data?.message && response.data.message !== 'sent') {
        alert(`인증 코드: ${response.data.message}`);
      }
    } catch (error) {
      const msg = error.response?.data?.message || '인증 메일 발송에 실패했습니다.';
      setErrors((prev) => ({ ...prev, email: msg }));
    }
  };

  // [이벤트] 회원가입 제출
  const handleSignup = async (e) => {
    e.preventDefault();

    // 전체 유효성 검사 수행
    const emailErr = validate.email(form.email);
    const passwordErr = validate.password(form.password);
    const rePasswordErr = validate.rePassword(form.rePassword, form.password);
    
    let codeErr = '';
    if (!uiState.showConfirmBox) {
      emailErr = emailErr || '이메일 인증을 진행해주세요.';
    } else {
      codeErr = validate.confirmCode(form.confirmCode);
    }

    setErrors({
      email: emailErr,
      password: passwordErr,
      rePassword: rePasswordErr,
      confirmCode: codeErr,
    });

    if (emailErr || passwordErr || rePasswordErr || codeErr) return;

    try {
      const payload = {
        email: form.email.trim(),
        password: form.password.trim(),
        code: form.confirmCode.trim(),
      };

      const response = await api.post('/signup', payload);
      if (response.status === 200 || response.status === 201) {
        navigate('/login?new');
      }
    } catch (error) {
      console.error('회원가입 실패:', error);
      const errorMsg = error.response?.data?.message || '회원가입에 실패했습니다. 인증 코드를 확인해주세요.';
      alert(errorMsg);
    }
  };

  return (
    <div className="signup-wrapper">
      <div className="sign-up-box">
        <h2>회원가입</h2>
        
        <form onSubmit={handleSignup}>
          {/* 이메일 및 인증 섹션 */}
          <div className="sign-up-item">
            <label htmlFor="email">이메일을 입력하세요</label>
            <div className="sign-up-email-box">
              <div className="sign-up-email">
                <input
                  type="text"
                  id="email"
                  placeholder="이메일"
                  value={form.email}
                  onChange={handleChange}
                />
              </div>
              <div className="btn-sign-up-email">
                <button type="button" id="btn-send-mail" onClick={handleSendMail}>
                  인증코드
                </button>
              </div>
            </div>

            {errors.email && <small className="error" style={{ display: 'block' }}>{errors.email}</small>}
            {uiState.sendMessage && <small id="send-message" style={{ display: 'block', color: 'green' }}>{uiState.sendMessage}</small>}

            {/* 인증코드 입력 박스 */}
            {uiState.showConfirmBox && (
              <div className="sign-up-confirm-box" id="confirm-code-box" style={{ marginTop: '10px' }}>
                <div className="sign-up-confirm">
                  <input
                    type="text"
                    id="confirmCode"
                    placeholder="인증코드 입력 (유효시간: 3분)"
                    value={form.confirmCode}
                    onChange={handleChange}
                  />
                </div>
              </div>
            )}
            {errors.confirmCode && <small className="error" style={{ display: 'block' }}>{errors.confirmCode}</small>}
          </div>

          {/* 비밀번호 섹션 */}
          <div className="sign-up-item">
            <label htmlFor="password">비밀번호를 입력하세요</label>
            <input
              type="password"
              id="password"
              placeholder="비밀번호"
              value={form.password}
              onChange={handleChange}
            />
            {errors.password && <small className="error" style={{ display: 'block' }}>{errors.password}</small>}
          </div>

          {/* 비밀번호 확인 섹션 */}
          <div className="sign-up-item">
            <label htmlFor="rePassword">비밀번호를 확인하세요</label>
            <input
              type="password"
              id="rePassword"
              placeholder="비밀번호 확인"
              value={form.rePassword}
              onChange={handleChange}
            />
            {errors.rePassword && <small className="error" style={{ display: 'block' }}>{errors.rePassword}</small>}
          </div>

          <button type="submit" id="btn-sign-up">가입하기</button>
        </form>
      </div>
    </div>
  );
};

export default Signup;
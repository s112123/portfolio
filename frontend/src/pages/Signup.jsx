import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './Signup.css';

const Signup = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: '',
    password: '',
    rePassword: '',
    confirmCode: '',
  });
  const [errors, setErrors] = useState({
    email: '',
    password: '',
    rePassword: '',
    confirmCode: '',
  });
  const [uiState, setUiState] = useState({
    currentEmail: '',
    showConfirmBox: false,
    validated: false,
    sendMessage: '',
    confirmCodeMessage: '',
  });

  // 타이머 상태 (초 단위, 3분 = 180초)
  const [timeLeft, setTimeLeft] = useState(180);
  const [isTimerRunning, setIsTimerRunning] = useState(false);

  // 타이머 작동 로직
  useEffect(() => {
    let timer;
    if (isTimerRunning && timeLeft > 0) {
      timer = setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
    } else if (timeLeft === 0) {
      setIsTimerRunning(false);
      setErrors((prev) => ({ ...prev, confirmCode: '인증 시간이 만료되었습니다. 다시 요청해주세요' }));
    }
    return () => clearInterval(timer);
  }, [isTimerRunning, timeLeft]);

  // 초를 MM:SS 형식으로 변환하는 함수 (예: 179 -> "02:59")
  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  };

  // 서버 에러 메시지 파싱 함수 ("필드명:메시지" 형태에서 필드명을 제거하고 메시지만 추출)
  const parseErrorMessage = (msg) => {
    if (!msg) return '';
    if (typeof msg !== 'string') {
      msg = String(msg);
    }
    if (msg.includes(':')) {
      const parts = msg.split(':');
      return parts.slice(1).join(':').trim();
    }
    return msg;
  };

  // 입력값 변경 핸들러
  const handleChange = (e) => {
    const { id, value } = e.target;
    setForm((prev) => ({ ...prev, [id]: value }));
    setErrors((prev) => ({ ...prev, [id]: '' }));
  };

  // 개별 유효성 검사 함수
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
  };

  // 인증코드 전송 버튼 클릭
  const handleSendMail = async () => {
    const emailErr = validate.email(form.email);
    if (emailErr) {
      setErrors((prev) => ({ ...prev, email: emailErr }));
      return;
    }

    const trimmedEmail = form.email.trim();
    setUiState((prev) => ({ ...prev, currentEmail: trimmedEmail }));

    try {
      const response = await api.post('/v1/otp/send/email', { email: trimmedEmail });
      setUiState((prev) => ({
        ...prev,
        showConfirmBox: true,
        validated: false,
        sendMessage: '인증 메일이 발송되었습니다',
        confirmCodeMessage: '',
      }));
      setForm((prev) => ({ ...prev, confirmCode: '' }));
      setErrors((prev) => ({ ...prev, email: '', confirmCode: '' }));

      // 타이머 초기화 및 시작 (180초)
      setTimeLeft(180);
      setIsTimerRunning(true);

      // 개발용 인증 코드 확인용
      if (response.data.success) {
        alert(`인증코드: ${response.data.data}`);
      }
    } catch (error) {
      const msg = error.response?.data?.message || '인증 메일 발송에 실패했습니다';
      setErrors((prev) => ({ ...prev, email: parseErrorMessage(msg) }));
    }
  };

  // 인증 코드 확인
  const handleConfirmCode = async () => {
    if (timeLeft === 0) {
      setErrors((prev) => ({ ...prev, confirmCode: '인증 시간이 만료되었습니다' }));
      return;
    }

    // 메일 변경 여부 검사
    if (form.email.trim() !== uiState.currentEmail) {
      setErrors((prev) => ({
        ...prev,
        email: '메일이 변경되었습니다. 재인증하세요',
        confirmCode: '',
      }));
      setUiState((prev) => ({
        ...prev,
        showConfirmBox: false,
        validated: false,
        sendMessage: '',
        confirmCodeMessage: '',
      }));
      setIsTimerRunning(false);
      return;
    }

    if (!form.confirmCode.trim()) {
      setErrors((prev) => ({ ...prev, confirmCode: '인증코드를 입력하세요' }));
      return;
    }

    try {
      const response = await api.post('/v1/otp/verify/email', {
        email: uiState.currentEmail,
        code: form.confirmCode.trim(),
      });

      const isVal = response.data && (response.data.validated || response.data.success);
      setUiState((prev) => ({ ...prev, validated: isVal }));

      if (isVal) {
        setUiState((prev) => ({
          ...prev,
          confirmCodeMessage: '인증이 완료되었습니다',
          showConfirmBox: false,
          sendMessage: '',
        }));
        setForm((prev) => ({ ...prev, confirmCode: '' }));
        // 인증 성공 시 기존 이메일 에러 메시지 초기화
        setErrors((prev) => ({ ...prev, email: '', confirmCode: '' }));
        // 인증 성공 시 타이머 정지
        setIsTimerRunning(false); 
      } else {
        setErrors((prev) => ({ ...prev, confirmCode: '인증코드가 유효하지 않습니다' }));
      }
    } catch (error) {
      const errorMsg = error.response?.data?.message || '인증 확인 중 오류가 발생했습니다';
      setErrors((prev) => ({ ...prev, confirmCode: parseErrorMessage(errorMsg) }));
    }
  };

  // 회원가입
  const handleSignup = async (e) => {
    e.preventDefault();

    const emailErr = validate.email(form.email);
    const passwordErr = validate.password(form.password);
    const rePasswordErr = validate.rePassword(form.rePassword, form.password);

    let emailErrorMsg = emailErr;
    let confirmCodeErrorMsg = '';

    if (!uiState.currentEmail) {
      emailErrorMsg = emailErrorMsg || '메일이 인증되지 않았습니다';
    } else if (form.email.trim() !== uiState.currentEmail) {
      emailErrorMsg = '메일이 변경되었습니다. 재인증하세요';
      setUiState((prev) => ({
        ...prev,
        showConfirmBox: false,
        validated: false,
        sendMessage: '',
        confirmCodeMessage: '',
      }));
      setIsTimerRunning(false);
    } else if (!uiState.validated) {
      emailErrorMsg = '메일이 인증되지 않았습니다';
    }

    setErrors({
      email: emailErrorMsg,
      password: passwordErr,
      rePassword: rePasswordErr,
      confirmCode: confirmCodeErrorMsg,
    });

    if (emailErrorMsg || passwordErr || rePasswordErr) return;

    try {
      const formData = {
        email: form.email.trim(),
        password: form.password.trim(),
      };

      const response = await api.post('http://localhost:8080/api/v1/members', formData);
      if (response.status === 200 || response.status === 201) {
        navigate('/login?new');
      }
    } catch (error) {
      console.error('회원가입 실패:', error);
      const errorMsg = error.response?.data?.message || '회원가입에 실패했습니다';
      alert(parseErrorMessage(errorMsg));
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

            {/* 인증코드 입력 박스 */}
            {uiState.showConfirmBox && (
              <div className="sign-up-confirm-box" id="confirm-code-box" style={{ marginTop: '10px' }}>
                <div className="sign-up-confirm">
                  <input
                    type="text"
                    id="confirmCode"
                    placeholder="인증코드"
                    value={form.confirmCode}
                    onChange={handleChange}
                  />
                </div>
                <div className="btn-sign-up-confirm">
                  <button type="button" id="btn-confirm-code" onClick={handleConfirmCode}>
                    인증확인
                  </button>
                </div>
              </div>
            )}

            {/* 타이머가 작동 중(isTimerRunning)일 때만 녹색 메시지 창 노출 */}
            {uiState.sendMessage && isTimerRunning && (
              <small id="send-message" style={{ display: 'block' }}>
                {uiState.sendMessage} ({formatTime(timeLeft)})
              </small>
            )}

            {/* 에러 메시지 */}
            {errors.email && <small className="error" style={{ display: 'block' }}>{errors.email}</small>}
            {errors.confirmCode && <small className="error" style={{ display: 'block' }}>{errors.confirmCode}</small>}

            {uiState.confirmCodeMessage && <small id="confirm-code-message" style={{ display: 'block', color: 'green' }}>{uiState.confirmCodeMessage}</small>}
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
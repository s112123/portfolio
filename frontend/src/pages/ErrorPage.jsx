import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css'; 

const ErrorPage = () => {
  const navigate = useNavigate();

  return (
    <div className="error-page-wrapper" style={{ textAlign: 'center', marginTop: '100px' }}>
      <div className="error-content">
        <h1 style={{ fontSize: '48px', color: '#ff4d4f' }}>⚠️ 503</h1>
        <h2>서버와 연결할 수 없습니다</h2>
        <p style={{ color: '#666', margin: '20px 0' }}>
          일시적인 네트워크 오류가 발생했거나, 서버가 점검 중입니다.<br />
          잠시 후 다시 시도해 주세요.
        </p>
        <button 
          onClick={() => window.location.href = '/'}
          style={{
            padding: '10px 20px',
            backgroundColor: '#1890ff',
            color: '#fff',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          홈으로 돌아가기
        </button>
      </div>
    </div>
  );
};

export default ErrorPage;
import React from 'react';
import { useNavigate } from 'react-router-dom';
import './NotFound.css';

export default function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="not-found-container">
      <h1 className="not-found-title">404 Not Found</h1>
      <p className="not-found-desc">존재하지 않거나 잘못된 경로입니다.</p>
      <button 
        onClick={() => navigate('/')}
        className="btn-not-found-home"
      >
        홈으로 돌아가기
      </button>
    </div>
  );
}
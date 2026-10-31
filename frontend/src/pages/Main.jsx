import React from 'react';
import { Link } from 'react-router-dom';
// import api from '../api/axios';
import './Main.css';

const Main = () => {
  return (
    <>
      <Link to="/post/write" className="logo-link">글쓰기</Link>
      <main className="container" style={{ padding: '40px 20px' }}>
        <h2>반갑습니다</h2>
        <p style={{ marginTop: '12px', color: '#666'}}>
          REST API 와 연동할 준비가 완료된 메인화면입니다
        </p>
      </main>
    </>
  );
};

export default Main;
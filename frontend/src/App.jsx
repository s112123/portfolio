import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ServerCheckLayout from './components/ServerCheckLayout';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Profile from './pages/Profile';
import ErrorPage from './pages/ErrorPage';
import './index.css';

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          <Route path="/error" element={<ErrorPage />} />
          <Route element={<ServerCheckLayout />}>
            <Route 
              path="/" 
              element={
                <main className="container" style={{ padding: '40px 20px' }}>
                  <h2>반갑습니다</h2>
                  <p style={{ marginTop: '12px', color: '#666'}}>
                    REST API 와 연동할 준비가 완료된 메인화면입니다
                  </p>
                </main>
              } 
            />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/profile" element={<Profile />} />
          </Route>
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
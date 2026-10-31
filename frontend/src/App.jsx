import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ServerCheckLayout from './components/ServerCheckLayout';
import PrivateRoute from './components/PrivateRoute';
import Main from './pages/Main';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Profile from './pages/Profile';
import PostWrite from './pages/PostWrite';
import PostRead from './pages/PostRead';
import PostEdit from './pages/PostEdit';
import ErrorPage from './pages/ErrorPage';
import './index.css';

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          <Route path="/error" element={<ErrorPage />} />
          
          <Route element={<ServerCheckLayout />}>
            {/* 1. 퍼블릭 페이지 */}
            <Route path="/" element={<Main />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/post/write" element={<PostWrite />} />
            <Route path="/post/view" element={<PostRead />} />
            <Route path="/post/edit" element={<PostEdit />} />

            {/* 2. 보호된 페이지 */}
            <Route element={<PrivateRoute />}>
              <Route path="/profile" element={<Profile />} />
            </Route>
          </Route>
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
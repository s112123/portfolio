import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Viewer } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor-viewer.css';
import api from '../api/axios';
import Comment from './Comment';
import './PostRead.css';

// JWT 파싱
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT 파싱 실패:', error);
    return null;
  }
};

// 날짜 포맷팅 함수 (yyyy-MM-dd HH:mm:ss)
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const replaced = dateString.replace('T', ' ');
  return replaced.split('.')[0];
};

// 숫자 포맷팅 함수 (K, M, B 단위 지원: 1500 -> 1.5K, 21000 -> 21K)
const formatCount = (count) => {
  if (count === undefined || count === null) return 0;
  if (count >= 1_000_000_000) {
    return (count / 1_000_000_000).toFixed(1).replace(/\.0$/, '') + 'b';
  }
  if (count >= 1_000_000) {
    return (count / 1_000_000).toFixed(1).replace(/\.0$/, '') + 'm';
  }
  if (count >= 1_000) {
    return (count / 1_000).toFixed(1).replace(/\.0$/, '') + 'k';
  }
  return count.toLocaleString();
};

export default function PostRead() {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  const [searchParams] = useSearchParams();
  const postId = searchParams.get('id');
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentUser, setCurrentUser] = useState(null); 
  const [liked, setLiked] = useState(false);

  useEffect(() => {
    if (!postId) {
      alert('잘못된 접근입니다.');
      navigate('/');
      return;
    }

    const token = localStorage.getItem('itBlogAccessToken');
    if (token) {
      const decodedToken = parseJwt(token);
      if (decodedToken && decodedToken.sub) {
        setCurrentUser({ memberId: decodedToken.sub });
      }
    }

    fetchPostData();
  }, [postId]);

  const fetchPostData = async () => {
    try {
      const response = await api.get(`/v1/posts/${postId}`);
      setPost(response.data.data);
      if (response.data.data.liked !== undefined) {
        setLiked(response.data.data.liked);
      }
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      setPost({
        title: '게시글 화면 UI 미리보기 샘플',
        content: '본문 내용입니다. 아래 댓글 UI를 확인해보세요.',
        likes: 12,
        registeredAt: '2026-03-30T09:00:00',
        writer: { id: 'writer01', nickname: '작성자', profileImageUrl: '' }
      });
    } finally {
      setLoading(false);
    }
  };

  const handleLikeToggle = async () => {
    if (!currentUser) {
      alert('로그인 후 이용해주세요.');
      return;
    }

    const currentLiked = liked;
    const previousLikesCount = post.likes;
    const nextLiked = !currentLiked;
    setLiked(nextLiked);
    setPost(prev => ({
      ...prev,
      likes: nextLiked ? prev.likes + 1 : prev.likes - 1
    }));

    try {
      if (currentLiked) {
        await api.delete(`/v1/likes/posts/${postId}`);
      } else {
        await api.post(`/v1/likes/posts/${postId}`);
      }
    } catch (error) {
      setLiked(currentLiked);
      setPost(prev => ({ ...prev, likes: previousLikesCount }));
      alert('요청 처리 중 오류가 발생했습니다.');
    }
  };

  if (loading) return <div className="loading">로딩 중...</div>;

  const isAuthor = currentUser && post && post.writer && 
    String(currentUser.memberId) === String(post.writer.id);

  return (
    <div className="post-read-container">
      <h1 className="post-title">{post.title}</h1>
      <div className="post-meta-header">
        <div className="author-info">
          <img 
            src={baseUrl + (post.writer.profileImageUrl || post.writer.profileImage) || 'https://via.placeholder.com/38'} 
            alt="프로필" 
            className="author-profile-img" 
          />
          <div className="author-details">
            <span className="author-name">{post.writer.nickname}</span>
            <span className="post-date">{formatDateTime(post.registeredAt)}</span>
          </div>
        </div>
      </div>
      <hr className="divider" />
      <div className="post-content-viewer">
        <Viewer initialValue={post.content || ''} />
      </div>
      <div className="post-like-section">
        <button onClick={handleLikeToggle} className={`btn-like ${liked ? 'liked' : ''}`}>
          <span className="btn-like-content">
            <span className="like-icon">
              <i className={liked ? "fa-solid fa-thumbs-up" : "fa-regular fa-thumbs-up"}></i>
            </span>
            <span className="like-count">{formatCount(post.likes)}</span>
          </span>
        </button>
      </div>
      <hr className="divider" />
      <div className="post-bottom-actions">
        <button onClick={() => navigate('/')} className="btn-list">목록으로</button>
        {isAuthor && (
          <div className="author-actions">
            <button onClick={() => navigate(`/post/edit?id=${postId}`)} className="btn-edit">수정</button>
            <button onClick={() => { if(window.confirm('정말 삭제하시겠습니까?')) {} }} className="btn-delete">삭제</button>
          </div>
        )}
      </div>
      <Comment postId={postId} currentUser={currentUser} />
    </div>
  );
}
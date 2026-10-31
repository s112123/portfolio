import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Viewer } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor-viewer.css';
import api from '../api/axios';
import './PostRead.css';

export default function PostRead() {
  const [searchParams] = useSearchParams();
  const postId = searchParams.get('id');
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [comments, setComments] = useState([]);
  const [commentContent, setCommentContent] = useState('');
  const [currentUser, setCurrentUser] = useState(null); 

  useEffect(() => {
    if (!postId) {
      alert('잘못된 접근입니다.');
      navigate('/');
      return;
    }
    fetchPostData();
    fetchComments();
    fetchUserInfo();
  }, [postId]);

  const fetchPostData = async () => {
    try {
      const response = await api.get(`/v1/posts/${postId}`);
      console.log(response.data.data);
      setPost(response.data.data);
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      alert('게시글을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async () => {
    try {
      // const response = await api.get(`/v1/posts/${postId}/comments`);
      // setComments(response.data.data);
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    }
  };

  const fetchUserInfo = async () => {
    try {
      const response = await api.get('/v1/members/me');
      setCurrentUser(response.data.data.nickname);
    } catch (error) {
      console.error('유저 정보 조회 실패:', error);
    }
  };

  // 날짜 포맷팅 함수 (yyyy-MM-dd HH:mm:ss)
  const formatDateTime = (dateString) => {
    if (!dateString) return '';
    const replaced = dateString.replace('T', ' ');
    return replaced.split('.')[0];
  };

  const handleLike = async () => {
    try {
      // await api.post(`/v1/posts/${postId}/likes`);
      setPost(prev => ({ ...prev, likes: prev.likes + 1 }));
    } catch (error) {
      console.error('좋아요 실패:', error);
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!commentContent.trim()) return;

    try {
      // await api.post(`/v1/posts/${postId}/comments`, { content: commentContent });
      setCommentContent('');
      fetchComments();
    } catch (error) {
      console.error('댓글 등록 실패:', error);
    }
  };

  if (loading) return <div className="loading">로딩 중...</div>;

  const isAuthor = currentUser && currentUser === post.writer;

  return (
    <div className="post-read-container">
      {/* 📌 상단 영역: 제목 아래에 사용자 정보가 오는 원래 구조 */}
      <h1 className="post-title">{post.title}</h1>
      
      <div className="post-meta-header">
        <div className="author-info">
          <img 
            src={post.writerProfileImage || 'https://via.placeholder.com/38'} 
            alt="프로필" 
            className="author-profile-img" 
          />
          <div className="author-details">
            <span className="author-name">{post.writer}</span>
            <span className="post-date">{formatDateTime(post.registeredAt)}</span>
          </div>
        </div>
      </div>

      <hr className="divider" />

      {/* 본문 내용 (Toast UI Viewer 사용) */}
      <div className="post-content-viewer">
        <Viewer initialValue={post.content || ''} />
      </div>

      
      {/* 좋아요 버튼 영역 */}
      <div className="post-like-section">
        <button onClick={handleLike} className="btn-like">
          <span className="btn-like-content">
            <span className="like-icon"><i className="fa-regular fa-thumbs-up"></i></span>
            <span className="like-count">{post.likes}</span>
          </span>
        </button>
      </div>

      <hr className="divider" />

      {/* 본문 바로 하단 버튼 영역 (좌측: 목록으로, 우측: 수정/삭제) */}
      <div className="post-bottom-actions">
        <button onClick={() => navigate('/')} className="btn-list">
          목록으로
        </button>

        {isAuthor && (
          <div className="author-actions">
            <button onClick={() => navigate(`/post/edit?id=${postId}`)} className="btn-edit">수정</button>
            <button onClick={() => {
              if(window.confirm('정말 삭제하시겠습니까?')) {
                // api.delete(`/v1/posts/${postId}`).then(() => navigate('/'));
              }
            }} className="btn-delete">삭제</button>
          </div>
        )}
      </div>

      {/* 댓글 섹션 */}
      <div className="comments-section">
        <h3>댓글</h3>
        
        <form onSubmit={handleCommentSubmit} className="comment-form">
          <textarea 
            placeholder="댓글을 남겨주세요"
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            className="comment-input"
          />
          <button type="submit" className="btn-comment-submit">등록</button>
        </form>

        <div className="comment-list">
          {comments.map((comment) => (
            <div key={comment.id} className="comment-item">
              <div className="comment-meta">
                <span className="comment-writer">{comment.writer}</span>
                <span className="comment-date">{formatDateTime(comment.registeredAt)}</span>
              </div>
              <p className="comment-text">{comment.content}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
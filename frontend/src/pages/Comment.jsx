import React, { useState, useEffect, useRef } from 'react';
import api from '../api/axios';
import './Comment.css';

const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const replaced = dateString.replace('T', ' ');
  return replaced.split('.')[0];
};

export default function Comment({ postId, currentUser }) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  
  const [commentData, setCommentData] = useState({
    content: [],
    currentPage: 1,
    totalPages: 0,
    hasPrevious: false,
    hasNext: false,
    totalElements: 0,
    size: 5
  });

  const [currentPage, setCurrentPage] = useState(1);
  const [commentContent, setCommentContent] = useState('');

  const [replyingTo, setReplyingTo] = useState(null);
  const [replyingToNickname, setReplyingToNickname] = useState(''); 
  const [replyContent, setReplyContent] = useState('');

  const replyInputRef = useRef(null);

  const getProfileImageUrl = (profileImageUrl) => {
    if (!profileImageUrl) return 'https://via.placeholder.com/24';
    if (profileImageUrl.startsWith('http')) return profileImageUrl;
    return `${baseUrl}${profileImageUrl}`;
  };

  useEffect(() => {
    fetchComments(currentPage);
  }, [postId, currentPage]);

  useEffect(() => {
    if (replyingTo !== null && replyInputRef.current) {
      replyInputRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      replyInputRef.current.focus();
    }
  }, [replyingTo]);

  const fetchComments = async (page) => {
    try {
      const response = await api.get(`/v1/comments?postId=${postId}&page=${page}`); 
      setCommentData(response.data.data);
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    }
  };

  // 일반 댓글 등록
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!currentUser) {
      alert('로그인 후 이용해주세요.');
      return;
    }
    if (!commentContent.trim()) return;

    try {
      const requestData = {
        postId: postId,
        writerId: currentUser.memberId,
        content: commentContent,
        parentId: null
      };

      await api.post('/v1/comments', requestData);
      setCommentContent('');
      
      if (currentPage === 1) {
        fetchComments(1);
      } else {
        setCurrentPage(1);
      }
    } catch (error) {
      console.error('댓글 등록 실패:', error);
      alert(error.response?.data?.message || '댓글 등록에 실패했습니다.');
    }
  };

  // 답글 등록
  const handleReplySubmit = async (targetCommentId) => {
    if (!currentUser) {
      alert('로그인 후 이용해주세요.');
      return;
    }
    if (!replyContent.trim()) return;

    try {
      const requestData = {
        postId: postId,
        writerId: currentUser.memberId,
        content: replyContent,
        parentId: targetCommentId
      };

      await api.post('/v1/comments', requestData);

      setReplyContent('');
      setReplyingTo(null);
      setReplyingToNickname('');
      
      fetchComments(currentPage);
    } catch (error) {
      console.error('답글 등록 실패:', error);
      alert(error.response?.data?.message || '답글 등록에 실패했습니다.');
    }
  };

  const handleCommentDelete = async (commentId) => {
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;
    try {
      await api.delete(`/v1/comments/${commentId}`);
      fetchComments(currentPage);
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
      alert(error.response?.data?.message || '댓글 삭제에 실패했습니다.');
    }
  };

  const maxPageButtons = 5;
  const currentBlock = Math.ceil(commentData.currentPage / maxPageButtons);
  const startPage = (currentBlock - 1) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, commentData.totalPages);

  return (
    <div className="comments-section">
      <h3>댓글 ({commentData.totalElements})</h3>
      <form onSubmit={handleCommentSubmit} className="comment-form">
        <div className="comment-input-container">
          <textarea 
            placeholder={currentUser ? "댓글을 입력해주세요" : "로그인 후 댓글을 입력해주세요"}
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
            // 💡 1번 개선점: 메인 댓글 입력창을 클릭(포커스)하면 열려있던 답글창을 닫아줍니다.
            onFocus={() => {
              setReplyingTo(null);
              setReplyingToNickname('');
              setReplyContent('');
            }}
            maxLength={100}
            disabled={!currentUser} 
            className={`comment-input ${!currentUser ? 'disabled' : ''}`}
          />
          <span className="comment-char-counter">
            {commentContent.length} / 100자
          </span>
        </div>

        {currentUser && (
          <button type="submit" className="btn-comment-submit">등록</button>
        )}
      </form>

      <div className="comment-list">
        {commentData.content.map((comment) => {
          const isCommentWriter = currentUser && String(currentUser.memberId) === String(comment.writer?.id);
          const isReply = comment.parentId !== null && comment.parentId !== undefined;

          // 💡 2번 문제 해결을 위한 팁: 
          // 현재 페이지 목록에 부모가 없으면 comment.parentWriterNickname 같은 서버 응답 값을 쓰거나 '상대방'으로 표시해야 합니다.
          let parentWriterNickname = '';
          if (isReply) {
            const parentComment = commentData.content.find(c => c.id === comment.parentId);
            // 만약 현재 페이지에 부모가 없다면 comment.parentWriterNickname (백엔드 추가 권장) 혹은 기본값 사용
            parentWriterNickname = parentComment ? parentComment.writer?.nickname : (comment.parentWriterNickname || '상대방');
          }

          return (
            <div 
              key={comment.id} 
              className={`comment-item ${isReply ? 'comment-reply-item' : ''}`}
            >
              <div className="comment-meta-header">
                <img 
                  src={getProfileImageUrl(comment.writer?.profileImageUrl)} 
                  alt="프로필" 
                  className="comment-profile-img" 
                />
                <span className="comment-writer">
                  {comment.writer?.nickname || '익명'}
                  {comment.isPostAuthor && (
                    <span className="comment-author-badge">
                      작성자
                    </span>
                  )}
                </span>
                <span className="comment-date">
                  {formatDateTime(comment.registeredAt)}
                </span>
              </div>

              <p className="comment-text">
                {isReply && (
                  <span className="comment-mention">
                    @{parentWriterNickname}
                  </span>
                )}
                {comment.content}
              </p>

              <div className="comment-actions">
                {currentUser && (
                  <button 
                    type="button" 
                    onClick={() => {
                      if (replyingTo === comment.id) {
                        setReplyingTo(null);
                        setReplyingToNickname('');
                        setReplyContent('');
                      } else {
                        setReplyingTo(comment.id);
                        setReplyingToNickname(comment.writer?.nickname || '익명');
                        setReplyContent('');
                      }
                    }}
                    className="btn-action-reply"
                  >
                    {replyingTo === comment.id ? '취소' : '답글'}
                  </button>
                )}
                {isCommentWriter && (
                  <button 
                    type="button" 
                    onClick={() => handleCommentDelete(comment.id)}
                    className="btn-action-delete"
                  >
                    삭제
                  </button>
                )}
              </div>

              {replyingTo === comment.id && (
                <div className="reply-form">
                  <div className="reply-form-header">
                    <span className="reply-target-name">
                      @{replyingToNickname}님에게 답글 작성 중
                    </span>
                    <button 
                      type="button" 
                      onClick={() => {
                        setReplyingTo(null);
                        setReplyingToNickname('');
                        setReplyContent('');
                      }}
                      className="btn-reply-close"
                    >
                      닫기 ✕
                    </button>
                  </div>

                  <div className="reply-input-wrapper">
                    <textarea 
                      ref={replyInputRef}
                      placeholder="답글을 입력해주세요"
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      maxLength={100}
                      className="reply-input"
                    />
                    <span className="reply-char-counter">
                      {replyContent.length} / 100자
                    </span>
                    <button 
                      type="button" 
                      onClick={() => handleReplySubmit(comment.id)}
                      className="btn-reply-submit"
                    >
                      등록
                    </button>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {commentData.totalPages > 1 && (
        <div className="comment-pagination">
          {startPage > 1 && (
            <button
              type="button"
              onClick={() => setCurrentPage(startPage - 1)}
              className="btn-page"
            >
              &lt;
            </button>
          )}

          {Array.from({ length: (endPage - startPage + 1) }, (_, index) => {
            const pageNum = startPage + index;
            return (
              <button
                key={pageNum}
                type="button"
                onClick={() => setCurrentPage(pageNum)}
                className={`btn-page ${currentPage === pageNum ? 'active' : ''}`}
              >
                {pageNum}
              </button>
            );
          })}

          {endPage < commentData.totalPages && (
            <button
              type="button"
              onClick={() => setCurrentPage(endPage + 1)}
              className="btn-page"
            >
              &gt;
            </button>
          )}
        </div>
      )}
    </div>
  );
}
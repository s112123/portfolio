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

  // 입력창 포커스를 위한 useRef 선언
  const commentInputRef = useRef(null);
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
    
    // 내용이 없을 때 입력창에 포커스 주기
    if (!commentContent.trim()) {
      if (commentInputRef.current) {
        commentInputRef.current.focus();
      }
      return;
    }

    try {
      const requestData = {
        postId: postId,
        writerId: currentUser.memberId,
        content: commentContent,
        parentId: null
      };

      const response = await api.post('/v1/comments', requestData);
      setCommentContent('');
      
      // 💡 백엔드가 등록 후 갱신된 PageResult 데이터를 내려준다면 곧바로 마지막 페이지로 이동 가능합니다.
      // 만약 응답으로 전체 데이터를 주지 않는다면 아래와 같이 계산하거나 재조회할 수 있습니다.
      // 여기서는 최신 댓글이 마지막에 달리므로, 등록 후 전체 개수 기반 마지막 페이지로 이동합니다.
      const updatedTotalElements = commentData.totalElements + 1;
      const calculatedLastPage = Math.ceil(updatedTotalElements / commentData.size) || 1;

      if (currentPage === calculatedLastPage) {
        fetchComments(calculatedLastPage);
      } else {
        setCurrentPage(calculatedLastPage);
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

    // 답글 내용이 없을 때 답글 입력창에 포커스 주기
    if (!replyContent.trim()) {
      if (replyInputRef.current) {
        replyInputRef.current.focus();
      }
      return;
    }

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
      
      // 답글 등록 시에도 최신 반영을 위해 마지막 페이지(또는 현재 페이지 유지)로 갱신
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
            ref={commentInputRef} 
            placeholder={currentUser ? "댓글을 입력해주세요" : "로그인 후 댓글을 입력해주세요"}
            value={commentContent}
            onChange={(e) => setCommentContent(e.target.value)}
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

          let parentWriterNickname = '';
          if (isReply) {
            const parentComment = commentData.content.find(c => c.id === comment.parentId);
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
              <i className="fa-solid fa-angle-left"></i>
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
              <i className="fa-solid fa-angle-right"></i>
            </button>
          )}
        </div>
      )}
    </div>
  );
}
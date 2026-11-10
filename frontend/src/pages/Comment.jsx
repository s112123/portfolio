import React, { useState, useEffect, useRef } from 'react';
import api from '../api/axios';
import './Comment.css';

// 날짜 포맷
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
  const commentInputRef = useRef(null);
  const replyInputRef = useRef(null);
  const commentsEndRef = useRef(null);

  // 댓글 조회
  useEffect(() => {
    fetchComments(currentPage);
  }, [postId, currentPage]);

  // 댓글 위치에 따른 스크롤 위치 조정
  useEffect(() => {
    if (replyingTo !== null && replyInputRef.current) {
      replyInputRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
      replyInputRef.current.focus();
    }
  }, [replyingTo]);

  // 댓글 조회 함수
  const fetchComments = async (page) => {
    try {
      const response = await api.get(`/v1/comments?postId=${postId}&page=${page}`); 
      setCommentData(response.data.data);
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    }
  };

  // 프로필 이미지 URL 처리
  const getProfileImageUrl = (profileImageUrl) => {
    if (!profileImageUrl) return 'https://via.placeholder.com/24';
    if (profileImageUrl.startsWith('http')) return profileImageUrl;
    return `${baseUrl}${profileImageUrl}`;
  };

  // 댓글 등록
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!currentUser) {
      alert('로그인 후 이용해주세요.');
      return;
    }
    
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

      await api.post('/v1/comments', requestData);
      setCommentContent('');
      
      // 댓글 등록 시, 마지막 페이지로 이동
      const updatedTotalElements = commentData.totalElements + 1;
      const calculatedLastPage = Math.ceil(updatedTotalElements / commentData.size) || 1;

      if (currentPage === calculatedLastPage) {
        await fetchComments(calculatedLastPage);
      } else {
        setCurrentPage(calculatedLastPage);
      }

      // 댓글 등록 후 하단으로 스크롤 이동
      setTimeout(() => {
        commentsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      }, 100);

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

      // 답글을 작성한 대상 댓글의 현재 페이지 위치 확인
      const targetIndex = commentData.content.findIndex(
        comment => String(comment.id) === String(targetCommentId)
      );

      // 대상 댓글이 현재 페이지에 없으면 현재 페이지 새로고침
      if (targetIndex === -1) {
        await fetchComments(currentPage);
        return;
      }

      // 전체 댓글 목록에서 대상 댓글의 인덱스 계산
      const parentGlobalIndex = (currentPage - 1) * commentData.size + targetIndex;

      // 대상 댓글의 전체 자손 댓글 수
      const targetComment = commentData.content[targetIndex];
      const replyCount = Number(targetComment.replyCount || 0);

      // 새로 등록한 답글이 들어갈 전체 목록의 인덱스
      // 부모 댓글 + 기존 자손 댓글 뒤에 새 답글이 추가된다
      const newReplyIndex = parentGlobalIndex + replyCount + 1;
      // 새 답글이 위치할 페이지 계산
      const targetPage = Math.floor(newReplyIndex / commentData.size) + 1;

      // 페이지 이동 또는 현재 페이지 새로고침
      if (currentPage !== targetPage) {
        setCurrentPage(targetPage);
      } else {
        await fetchComments(targetPage);
      }

      // 스크롤 이동
      setTimeout(() => {
        commentsEndRef.current?.scrollIntoView({
          behavior: 'smooth'
        });
      }, 100);

    } catch (error) {
      console.error('답글 등록 실패:', error);
      alert(error.response?.data?.message || '답글 등록에 실패했습니다.');
    }
  };

  // 댓글 삭제
  const handleCommentDelete = async (commentId) => {
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;
    try {
      await api.delete(`/v1/comments/${commentId}`);
      // 삭제 후 현재 페이지에 데이터가 없고 1페이지보다 크면 앞 페이지로 이동
      const remainingElements = commentData.totalElements - 1;
      const maxPage = Math.ceil(remainingElements / commentData.size) || 1;
      const targetPage = currentPage > maxPage ? maxPage : currentPage;
      
      setCurrentPage(targetPage);
      fetchComments(targetPage);
    } catch (error) {
      console.error('댓글 삭제 실패:', error);
      alert(error.response?.data?.message || '댓글 삭제에 실패했습니다.');
    }
  };

  // 댓글 페이지네이션 관련 변수
  const maxPageButtons = 5;
  const currentBlock = Math.ceil(commentData.currentPage / maxPageButtons);
  const startPage = (currentBlock - 1) * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, commentData.totalPages);

  return (
    <div className="comments-section">
      <h3>댓글 ({commentData.totalElements})</h3>
      
      {/* 댓글 입력 */}
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

      {/* 댓글 목록 */}
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

              {/* 답글 등록 */}
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

      {/* 스크롤 위치 타겟용 빈 엘리먼트 */}
      <div ref={commentsEndRef} />

      {/* 페이지네이션 */}
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
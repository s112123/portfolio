import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import './Comment.css';

// 날짜 포맷팅 함수 (yyyy-MM-dd HH:mm:ss)
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const replaced = dateString.replace('T', ' ');
  return replaced.split('.')[0];
};

// [화면 테스트용 임시 샘플 데이터]
const MOCK_COMMENTS_PAGE_1 = [
  {
    id: 1,
    content: '조희대대법장이 임기종료 되고 제2의김명수가 임명되면 끝장납니다!! 그전에 끝나야 하지요..시간가는게...',
    registeredAt: '2026-03-30T10:15:30.000',
    parentId: null,
    writer: {
      id: 'user01',
      nickname: '@이상용-b6i',
      profileImageUrl: 'https://via.placeholder.com/28/007bff/ffffff?text=A'
    }
  },
  {
    id: 2,
    content: '조씨는 허수아비',
    registeredAt: '2026-03-30T10:20:00.000',
    parentId: 1,
    writer: {
      id: 'user02',
      nickname: '@이상근-v6n',
      profileImageUrl: 'https://via.placeholder.com/28/28a745/ffffff?text=B'
    }
  },
  {
    id: 3,
    content: '이제는 달라져야 하는데 쫄...',
    registeredAt: '2026-03-30T11:00:12.000',
    parentId: 1,
    writer: {
      id: 'user03',
      nickname: '@여이현옥-v3z',
      profileImageUrl: 'https://via.placeholder.com/28/dc3545/ffffff?text=C'
    }
  },
  {
    id: 4,
    content: '두 번째 원본 댓글입니다. (독립된 스레드)',
    registeredAt: '2026-03-30T11:30:45.000',
    parentId: null,
    writer: {
      id: 'myMemberId',
      nickname: '@테스터-me',
      profileImageUrl: 'https://via.placeholder.com/28/ffc107/333333?text=Me'
    }
  }
];

const MOCK_COMMENTS_PAGE_2 = [
  {
    id: 5,
    content: '2페이지의 첫 번째 댓글입니다.',
    registeredAt: '2026-03-30T13:00:00.000',
    parentId: null,
    writer: {
      id: 'user05',
      nickname: '@테스터5',
      profileImageUrl: 'https://via.placeholder.com/28/17a2b8/ffffff?text=E'
    }
  }
];

export default function Comment({ postId, currentUser }) {
  const [comments, setComments] = useState([]);
  const [commentContent, setCommentContent] = useState('');
  const [commentPage, setCommentPage] = useState(1);
  const [commentTotalPages, setCommentTotalPages] = useState(2);

  // 답글(대댓글) 작성 상태 관리용
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyingToNickname, setReplyingToNickname] = useState(''); 
  const [replyContent, setReplyContent] = useState('');

  useEffect(() => {
    fetchComments(1);
  }, [postId]);

  const fetchComments = async (page = 1) => {
    try {
      // 추후 실제 API 연동 시 주석 해제
      // const response = await api.get(`/v1/posts/${postId}/comments?page=${page}&size=5`);
      // setComments(response.data.data.content);
      // setCommentTotalPages(response.data.data.totalPages);

      if (page === 1) {
        setComments(MOCK_COMMENTS_PAGE_1);
      } else {
        setComments(MOCK_COMMENTS_PAGE_2);
      }
      setCommentTotalPages(2);
      setCommentPage(page);
    } catch (error) {
      console.error('댓글 조회 실패:', error);
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!currentUser) {
      alert('로그인 후 이용해주세요.');
      return;
    }
    if (!commentContent.trim()) return;

    const newComment = {
      id: Date.now(),
      content: commentContent,
      registeredAt: new Date().toISOString(),
      parentId: null,
      writer: {
        id: currentUser.memberId,
        nickname: '@나(로그인유저)',
        profileImageUrl: 'https://via.placeholder.com/28/ffc107/333333?text=Me'
      }
    };
    setComments(prev => [newComment, ...prev]);
    setCommentContent('');
  };

  const handleReplySubmit = async (parentCommentId) => {
    if (!currentUser) {
      alert('로그인 후 이용해주세요.');
      return;
    }
    if (!replyContent.trim()) return;

    const newReply = {
      id: Date.now(),
      content: replyContent,
      registeredAt: new Date().toISOString(),
      parentId: parentCommentId,
      writer: {
        id: currentUser.memberId,
        nickname: '@나(로그인유저)',
        profileImageUrl: 'https://via.placeholder.com/28/ffc107/333333?text=Me'
      }
    };
    
    const targetIndex = comments.findIndex(c => c.id === parentCommentId);
    if (targetIndex !== -1) {
      const updated = [...comments];
      updated.splice(targetIndex + 1, 0, newReply);
      setComments(updated);
    } else {
      setComments(prev => [...prev, newReply]);
    }

    setReplyContent('');
    setReplyingTo(null);
    setReplyingToNickname('');
  };

  const handleCommentDelete = async (commentId) => {
    if (!window.confirm('댓글을 삭제하시겠습니까?')) return;
    setComments(prev => prev.filter(c => c.id !== commentId));
  };

  return (
    <div className="comments-section">
      <h3>댓글</h3>
      
      {/* 새 댓글 작성 폼 */}
      <form onSubmit={handleCommentSubmit} className="comment-form">
        <textarea 
          placeholder={currentUser ? "댓글을 남겨주세요" : "로그인 후 댓글을 남겨주세요."}
          value={commentContent}
          onChange={(e) => setCommentContent(e.target.value)}
          disabled={!currentUser} 
          className="comment-input"
          style={{ backgroundColor: !currentUser ? '#f8f9fa' : '#fff' }}
        />
        {currentUser && (
          <button type="submit" className="btn-comment-submit">
            등록
          </button>
        )}
      </form>

      {/* 댓글 목록 */}
      <div className="comment-list">
        {comments.map((comment) => {
          const isCommentWriter = currentUser && String(currentUser.memberId) === String(comment.writer?.id);
          const isReply = comment.parentId !== null && comment.parentId !== undefined;

          let parentWriterNickname = '';
          if (isReply) {
            const parentComment = comments.find(c => c.id === comment.parentId);
            parentWriterNickname = parentComment ? parentComment.writer?.nickname : '상대방';
          }

          return (
            <div 
              key={comment.id} 
              className={`comment-item ${isReply ? 'comment-reply-item' : ''}`}
            >
              {/* 상단 프로필 + 닉네임 + 날짜 */}
              <div className="comment-meta-header">
                <img 
                  src={comment.writer?.profileImageUrl || 'https://via.placeholder.com/24'} 
                  alt="프로필" 
                  className="comment-profile-img" 
                />
                <span className="comment-writer">
                  {comment.writer?.nickname || '익명'}
                </span>
                <span className="comment-date">
                  {formatDateTime(comment.registeredAt)}
                </span>
              </div>

              {/* 댓글 본문 */}
              <p className="comment-text">
                {isReply && (
                  <span style={{ color: '#007bff', fontWeight: 'bold', marginRight: '6px' }}>
                    @{parentWriterNickname}
                  </span>
                )}
                {comment.content}
              </p>

              {/* 하단 액션 (답글 / 삭제) */}
              <div className="comment-actions">
                {!isReply && currentUser && (
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

              {/* 답글 입력 상자 */}
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
                      placeholder="답글을 남겨주세요..."
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      className="reply-input"
                    />
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

      {/* 페이지네이션 */}
      {commentTotalPages > 1 && (
        <div className="comment-pagination">
          {Array.from({ length: commentTotalPages }, (_, index) => index + 1).map((pageNum) => (
            <button
              key={pageNum}
              onClick={() => fetchComments(pageNum)}
              className={`btn-page ${commentPage === pageNum ? 'active' : ''}`}
            >
              {pageNum}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './Main.css';

// 날짜 포맷팅 함수
const formatTimeAgo = (dateString) => {
    const now = new Date();
    const past = new Date(dateString);
    const diffInSeconds = Math.floor((now - past) / 1000);

    if (diffInSeconds < 60) return "방금 전";

    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) return `${diffInMinutes}분 전`;

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours}시간 전`;

    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 30) return `${diffInDays}일 전`;

    const diffInMonths = Math.floor(diffInDays / 30);
    if (diffInMonths < 12) return `${diffInMonths}달 전`;

    const diffInYears = Math.floor(diffInDays / 365);
    return `${diffInYears}년 전`;
};

// 마크다운/HTML 태그 제거
const getContentSummary = (content) => {
    if (!content) return "";
    let summary = content;
    summary = summary.replace(/```[\s\S]*?```/g, '');
    summary = summary.replace(/!\[.*?\]\(.*?\)/g, '');
    summary = summary.replace(/<(?=[a-zA-Z/!])[^>]*>/g, ' ');
    summary = summary.replace(/\[(.*?)\]\(.*?\)/g, '$1');
    summary = summary.replace(/\\/g, '');
    summary = summary.replace(/\s+/g, ' ').trim();
    return summary.trim();
}

// 숫자 형식 변환
const formatNumber = (num) => {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
    }
    return num.toString();
}

const Main = () => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const keyword = searchParams.get('keyword') || ''; // URL에서 검색어 추출

  const [sortType, setSortType] = useState('LATEST'); 
  const [posts, setPosts] = useState([]);                 
  const [lastPostId, setLastPostId] = useState(null);     
  const [lastValue, setLastValue] = useState(null);       
  const [hasNext, setHasNext] = useState(true);           
  const [loading, setLoading] = useState(false);          
  const [error, setError] = useState(null);               
  
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const observerRef = useRef(null); 

  // 로그인 상태 확인
  const checkLoginStatus = () => {
    const token = localStorage.getItem('itBlogAccessToken');
    setIsLoggedIn(!!token);
  };

  useEffect(() => {
    checkLoginStatus();
    window.addEventListener('storage', checkLoginStatus);
    window.addEventListener('authChange', checkLoginStatus);

    return () => {
      window.removeEventListener('storage', checkLoginStatus);
      window.removeEventListener('authChange', checkLoginStatus);
    };
  }, []);

  // 게시글 불러오기 함수
  const fetchPosts = async (cursorId, cursorValue, currentSortType, currentKeyword, isReset = false) => {
    if (loading) return;
    setLoading(true);
    setError(null);

    try {
      let url = `/v1/posts?sortType=${currentSortType}&size=10`;
      
      if (cursorId) {
        url += `&lastPostId=${cursorId}`;
      }
      if (cursorValue !== null && cursorValue !== undefined) {
        url += `&lastValue=${cursorValue}`;
      }
      if (currentKeyword) {
        url += `&keyword=${encodeURIComponent(currentKeyword)}`;
      }

      const response = await api.get(url);
      const apiResponse = response.data; 

      if (apiResponse && apiResponse.success && apiResponse.data) {
        const newContent = apiResponse.data.content;
        
        setPosts(prev => isReset ? newContent : [...prev, ...newContent]);
        setHasNext(apiResponse.data.hasNext);

        if (newContent.length > 0) {
          const lastItem = newContent[newContent.length - 1];
          setLastPostId(lastItem.id);

          if (currentSortType === 'LIKES') {
            setLastValue(lastItem.likes);
          } else if (currentSortType === 'HITS') {
            setLastValue(lastItem.hits);
          } else {
            setLastValue(null);
          }
        }
      } else {
        throw new Error('데이터를 불러오는데 실패했습니다.');
      }
    } catch (err) {
      console.error(err);
      setError(err.message || '네트워크 응답이 올바르지 않습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 정렬 탭이나 검색어(keyword)가 바뀔 때마다 상태 초기화 후 첫 페이지 로드
  useEffect(() => {
    setLastPostId(null);
    setLastValue(null);
    fetchPosts(null, null, sortType, keyword, true);
  }, [sortType, keyword]);

  // 무한 스크롤 옵저버
  const handleObserver = useCallback((entries) => {
    const target = entries[0];
    if (target.isIntersecting && hasNext && !loading) {
      fetchPosts(lastPostId, lastValue, sortType, keyword, false);
    }
  }, [lastPostId, lastValue, sortType, keyword, hasNext, loading]);

  useEffect(() => {
    const option = {
      root: null,
      rootMargin: '400px', 
      threshold: 0.1,
    };

    const observer = new IntersectionObserver(handleObserver, option);
    if (observerRef.current) {
      observer.observe(observerRef.current);
    }

    return () => {
      if (observerRef.current) {
        observer.unobserve(observerRef.current);
      }
    };
  }, [handleObserver]);

  // '전체' 버튼 클릭 시 검색어 제거 (홈으로 이동)
  const handleResetSearch = () => {
    navigate('/');
  };

  return (
    <main className="main-container">
      {/* 상단 정렬 탭 및 글쓰기 버튼 영역 */}
      <div className="posts-top">
        <ul className="posts-top-left">
          {/* 검색어가 있을 때만 '전체' 버튼이 활성화되어 출력됨 */}
          {keyword && (
            <li
              className="sort-item"
              id="sort-all"
              onClick={handleResetSearch}
            >
              전체
            </li>
          )}

          <li
            className={`sort-item ${sortType === 'LATEST' ? 'active' : ''}`}
            id="sort-latest"
            onClick={() => setSortType('LATEST')}
          >
            최신 순
          </li>
          <li
            className={`sort-item ${sortType === 'LIKES' ? 'active' : ''}`}
            id="sort-likes"
            onClick={() => setSortType('LIKES')}
          >
            좋아요 순
          </li>
          <li
            className={`sort-item ${sortType === 'HITS' ? 'active' : ''}`}
            id="sort-hits"
            onClick={() => setSortType('HITS')}
          >
            조회 순
          </li>
        </ul>

        <div className="posts-top-right">
          {isLoggedIn && (
            <Link to="/post/write" id="btn-add-post">
              글쓰기
            </Link>
          )}
        </div>
      </div>

      {/* 게시글 그리드 영역 */}
      <div className="posts-middle" id="posts-middle">
        {error && <div className="error-message">에러 발생: {error}</div>}

        {posts.length === 0 && !loading ? (
          <div className="no-data">검색된 글이 없습니다</div>
        ) : (
          <div className="post-grid">
            {posts.map((post) => (
              <article key={post.id} className="post-card">
                {post.thumbUrl && (
                  <Link to={`/post/view?id=${post.id}`} className="thumbnail-wrapper">
                    <img src={`${baseUrl}${post.thumbUrl}`} alt={post.title} className="thumbnail-img" />
                  </Link>
                )}

                <div className="card-content">
                  <div className="post-item-content-top">
                    <div className="post-item-content-profile">
                      <img src={`${baseUrl}${post.writer.profileImageUrl}`} alt={post.writer.nickname} />
                    </div>
                    <div className="post-item-content-writer">
                      <div className="writer-name">{post.writer.nickname}</div>
                      <div className="post-created-at">{formatTimeAgo(post.registeredAt)}</div>
                    </div>
                  </div>

                  <Link to={`/post/view?id=${post.id}`} className="post-link">
                    <h3 className="post-item-title">{post.title}</h3>
                    <p className="post-item-summary">{getContentSummary(post.content)}</p>
                  </Link>

                  <div className="post-item-content-bottom">
                    <div className="post-item-icon">
                      <i className="fa-regular fa-thumbs-up"></i>
                      <span>{formatNumber(post.likes)}</span>
                      <i className="fa-solid fa-eye"></i>
                      <span>{formatNumber(post.hits)}</span>
                    </div>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}

        <div ref={observerRef} style={{ height: '20px', margin: '20px 0' }} />

        <div className="loading-container">
          {loading && <div className="loading" style={{ textAlign: 'center', padding: '20px' }}>불러오는 중...</div>}
        </div>
      </div>
    </main>
  );
};

export default Main;
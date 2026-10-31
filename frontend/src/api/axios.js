import axios from 'axios';

// 인증이 불필요한 퍼블릭 API 경로
const PUBLIC_URLS = [
  '/v1/health',
  '/v1/otp/send/email',
  '/v1/otp/verify/email',
  '/v1/auth/signup', 
  '/v1/auth/login',
  '/v1/auth/refresh'
];

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    // console.log(`url=${config.url}`);
    const isPublic = PUBLIC_URLS.some((url) => config.url?.includes(url));
    if (!isPublic) {
      const token = localStorage.getItem('itBlogAccessToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } else {
      // 퍼블릭 경로인 경우 혹시 남아있을 수 있는 Authorization 헤더를 명시적으로 제거
      delete config.headers.Authorization;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 큐잉 처리를 위한 상태 변수
let isRefreshing = false; // 현재 토큰을 갱신 중인지 여부
let failedQueue = [];     // 갱신을 기다리는 요청들의 대기 큐

// 대기 중인 요청들을 처리하는 함수
const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// 응답 인터셉터
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 404 에러
    if (error.response && error.response.status === 404) {
      const errorMessage = error.response.data?.message || '요청하신 리소스를 찾을 수 없습니다.';
      alert(errorMessage);
      window.history.back();
      return Promise.reject(error);
    }

    // 401 에러이고, 아직 재시도하지 않은 요청인 경우에만 처리
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      // 리프레시를 시도하면 안 되는 퍼블릭 API 경로 (로그인, 회원가입 등)
      const skipRefreshUrls = ['/auth/login', '/auth/signup', '/auth/refresh', '/auth/logout'];
      const isSkipUrl = skipRefreshUrls.some((url) => originalRequest.url?.includes(url));

      // 만약 리프레시 제외 대상 API에서 401이 난 거라면 토큰 갱신을 하지 않고 바로 에러 반환
      if (isSkipUrl) {
        // 만약 /auth/refresh 자체에서 401이 난 거라면 세션 만료 처리
        // 리프레시 토큰이 만료되어 없는 경우, 서버에서 401 을 반환한다
        if (originalRequest.url.includes('/auth/refresh')) {
          localStorage.removeItem('itBlogAccessToken');
          // alert('세션이 만료되었습니다. 다시 로그인해주세요.');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // 2. 이미 다른 요청이 토큰을 갱신 중이라면 큐 (Queue) 에 담아두고 대기시킴
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            // 새 토큰을 붙여서 원래 요청 재시도
            return api(originalRequest); 
          })
          .catch(err => {
            return Promise.reject(err);
          });
      }

      // 3. 최초로 401을 마주친 요청이 갱신 프로세스 시작
      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // 백엔드의 /refresh 엔드포인트 호출 (쿠키에 담긴 Refresh Token 이 자동으로 전송됨)
        const response = await api.post('/v1/auth/refresh', {}, {
          withCredentials: true
        });

        // 새로운 Access Token 획득
        const newAccessToken = response.data.data;
        console.log(`newAccessToken: ${newAccessToken}`);
        localStorage.setItem('itBlogAccessToken', newAccessToken);

        // 헤더 갱신
        api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        // 4. 대기 중이던 다른 요청들에게 새 토큰을 전달하며 줄줄이 해제
        processQueue(null, newAccessToken);
        isRefreshing = false;

        // 방금 401이 났던 원래 요청 재시도
        return api(originalRequest);
      } catch (refreshError) {
        // 리프레시 토큰마저 만료되었거나 검증 실패 시 (탈취 감지 등)
        processQueue(refreshError, null);
        isRefreshing = false;

        localStorage.removeItem('itBlogAccessToken');
        window.location.href = '/login';

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
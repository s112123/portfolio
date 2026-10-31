import React, { useRef, useState, useEffect } from 'react';
import colorSyntax from '@toast-ui/editor-plugin-color-syntax';
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight-all.js';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Editor } from '@toast-ui/react-editor';
import api from '../api/axios';
import '@toast-ui/editor/dist/toastui-editor.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
import 'prismjs/themes/prism.css';
import '@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight.css';
import './PostEdit.css'; 

export default function PostUpdate() {
  const [title, setTitle] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 유효성 검사 에러 메시지 상태 관리
  const [titleError, setTitleError] = useState('');
  const [contentError, setContentError] = useState('');

  const titleRef = useRef(null);
  const editorRef = useRef();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const postId = searchParams.get('id');

  // 글 조회하기
  useEffect(() => {
    const fetchExistingPost = async () => {
      if (!postId) {
        alert('잘못된 요청입니다');
        navigate('/');
        return;
      }

      try {
        // 글 조회하기
        const response = await api.get(`/v1/posts/${postId}`);
        const postData = response.data.data;

        setTitle(postData.title);
        if (editorRef.current) {
          const editorInstance = editorRef.current.getInstance();
          editorInstance.setMarkdown(postData.content || ''); 
        }
      } catch (error) {
        // console.error('글 조회 중 에러 발생:', error);
        const errorMsg = error.response?.data?.message || '게시글을 불러오는 중 오류가 발생했습니다';
        alert(errorMsg);
        navigate(-1);
      }
    };

    fetchExistingPost();
  }, [postId, navigate]);

  const handleTitleKeyDown = (e) => {
    // 제목에서 탭을 누르면 에디터로 이동
    if (e.key === 'Tab' && !e.shiftKey) {
      e.preventDefault();
      const editor = editorRef.current?.getInstance();
      const editorElement = editor?.getEditorElements();
      editorElement?.wwEditor?.querySelector('.ProseMirror')?.focus();
    }
  };

  // 글 수정하기
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isSubmitting) return;
    
    const editorInstance = editorRef.current.getInstance();
    const markdownContent = editorInstance.getMarkdown();

    // 유효성 검사 초기화
    setTitleError('');
    setContentError('');

    let isValid = true;

    // 유효성 검사 -> 제목
    if (!title.trim()) {
      setTitleError('제목을 입력해주세요');
      titleRef.current?.focus();
      isValid = false;
    }

    // 유효성 검사 -> 내용
    if (!markdownContent.trim()) {
      setContentError('내용을 입력해주세요');
      if (isValid) {
        // 내용에만 에러가 났을 경우 에디터로 포커스 유도
        const editorElement = editorInstance.getEditorElements();
        editorElement?.wwEditor?.querySelector('.ProseMirror')?.focus();
      }
      isValid = false;
    }

    if (!isValid) return;

    const postData = {
      title: title,
      content: markdownContent,
    };

    let isSaved = false;
    try {
      setIsSubmitting(true);
      await api.put(`/v1/posts/${postId}`, postData);
      isSaved = true;
      navigate(`/post/view?id=${postId}`);
    } catch (error) {
      // console.error('글 수정 중 에러 발생:', error);
      if (isSaved) {
        // 서버 저장 성공 후, 에러가 난 경우
        navigate('/');
        return;
      }
      const errorMsg = error.response?.data?.message || '글 수정 중 오류가 발생했습니다';
      alert(errorMsg);
      setIsSubmitting(false);
    }
  };

  return (
    <div className="post-write-container">
      <h2 className="post-write-header">글 수정</h2>
      <form onSubmit={handleSubmit} className="post-write-form">
        <div className="post-title-wrapper">
          <input 
            type="text"
            placeholder="제목을 입력하세요"
            value={title}
            onChange={(e) => {
              setTitle(e.target.value);
              if (titleError) setTitleError('');
            }}
            onKeyDown={handleTitleKeyDown}
            className="post-title-input"
            id="post-title"
            ref={titleRef}
          />
        </div>
        {titleError && <div className="error-message">{titleError}</div>}
        <div className="toast-editor-box">
          <Editor
            ref={editorRef}
            previewType="vertical"    
            height="500px"
            initialEditType="wysiwyg"
            useCommandShortcut={true}
            plugins={[colorSyntax, codeSyntaxHighlight]}
            onChange={() => {
              if (contentError) setContentError('');
            }}
            hooks={{
              addImageBlobHook: async (blob, callback) => {
                try {
                  // 에디터에 등록된 이미지
                  const formData = new FormData();
                  formData.append("file", blob);
                  // 임시 이미지를 서버에 저장
                  const response = await api.post("/v1/post-images", formData, {
                    headers: {
                      'Content-Type': 'multipart/form-data'
                    }
                  });
                  // 서버에 저장된 이미지를 조회하는 API
                  const permanentImageUrl = 'http://localhost:8080' + response.data.data.url; 
                  callback(permanentImageUrl, blob.name);
                } catch (error) {
                  // console.error('이미지 업로드 실패:', error);
                  const errorMsg = error.response?.data?.message || '이미지 업로드 중 오류가 발생했습니다';
                  alert(errorMsg);
                }
              }
            }}
          />
        </div>
        {contentError && <div className="error-message">{contentError}</div>}
        <div className="post-actions">
          <button type="submit" className="btn-submit" disabled={isSubmitting}>
            {isSubmitting ? '수정 중...' : '수정'}
          </button>
          <button 
            type="button" 
            className="btn-cancel" 
            onClick={() => window.history.back()}
            disabled={isSubmitting}
          >
            취소
          </button>
        </div>
      </form>
    </div>
  );
}
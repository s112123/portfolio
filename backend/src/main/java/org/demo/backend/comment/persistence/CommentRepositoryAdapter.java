package org.demo.backend.comment.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.comment.domain.Comment;
import org.demo.backend.comment.domain.CommentRepository;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public Comment save(Comment comment) {
        return commentJpaRepository.save(comment);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return commentJpaRepository.findById(commentId);
    }

    @Override
    public void delete(Comment comment) {
        commentJpaRepository.delete(comment);
    }

    @Override
    public PageDetail<CommentProjection> fetchPageByPostId(Long postId, int page, int size) {
        // 유효성 검사
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("[댓글 목록 조회] page 와 size 는 1 이상이어야 합니다");
        }

        // 1. 전체 댓글 조회
        List<CommentProjection> comments = commentJpaRepository.fetchPageByPostId(postId);

        // 2. Map 으로 부모 댓글과 자식 댓글 연결
        // 최상위 댓글 목록 (parent_id = null)
        List<CommentProjection> roots = new ArrayList<>();

        // 부모 댓글 ID 를 기준으로 자식 댓글 그룹화
        Map<Long, List<CommentProjection>> childrenMap = new HashMap<>();
        for (CommentProjection comment : comments) {
            Long parentId = comment.getParentId();
            if (parentId == null) {
                roots.add(comment);
            } else {
                childrenMap
                        .computeIfAbsent(parentId, key -> new ArrayList<>())
                        .add(comment);
            }
        }

        // 3. Deque 를 이용해 평탄화 (DFS) → 부모 댓글 밑에는 자식 댓글이 붙도록 정렬한다
        // 최상위 댓글을 역순으로 stack 에 추가한다
        // stack 은 LIFO 이므로 역순으로 넣어야 원래 ID 오름차순으로 꺼낼 수 있다
        Deque<CommentProjection> stack = new ArrayDeque<>();
        for (int i = roots.size() - 1; i >= 0; i--) {
            stack.push(roots.get(i));
        }

        List<CommentProjection> flattened = new ArrayList<>();
        while (!stack.isEmpty()) {
            // stack 에서 댓글 하나 꺼내기
            CommentProjection current = stack.pop();
            // 평탄화 목록에 추가
            flattened.add(current);
            // 현재 댓글의 자식 댓글 조회
            List<CommentProjection> children = childrenMap.get(current.getId());
            if (children == null || children.isEmpty()) {
                continue;
            }
            // 자식 댓글을 역순으로 stack 에 추가
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
        }

        // 4. 역순으로 조회하면서 replyCount 계산 → replyCount 는 프론트가 구현한 로직에서 사용되므로 계산하여 응답
        // 각 댓글의 대댓글 수 초기화
        Map<Long, Integer> replyCountMap = new HashMap<>();
        for (CommentProjection comment : flattened) {
            replyCountMap.put(comment.getId(), 0);
        }

        // 트리에 포함되지 않은 댓글이 있는지 검증
        if (flattened.size() != comments.size()) {
            throw new IllegalStateException("댓글 트리 구성 실패: 부모 댓글이 없거나 댓글 관계가 잘못되었습니다");
        }

        // DFS 결과를 역순으로 순회하면 대댓글 수 계산
        // 평탄화 결과는 부모가 자식보다 먼저 등장하므로 역순으로 순회하면 항상 자식을 처리한 다음 부모를 처리할 수 있다
        // 따라서 자식의 replyCount 가 이미 계산된 상태에서 부모에게 누적할 수 있다
        for (int i = flattened.size() - 1; i >= 0; i--) {
            CommentProjection current = flattened.get(i);
            Long parentId = current.getParentId();

            // 최상위 댓글이면 부모에게 전달할 필요가 없다
            if (parentId == null) {
                continue;
            }
            
            // 현재 댓글의 모든 하위 댓글 수
            int currentReplyCount = replyCountMap.get(current.getId());
            // 부모 댓글의 현재까지 누적된 하위 댓글 수
            Integer parentReplyCount = replyCountMap.get(parentId);
            if (parentReplyCount == null) {
                throw new IllegalStateException("[대댓글 수 집계] 부모 댓글을 찾을 수 없습니다: parentId=" + parentId);
            }

            // 부모의 하위 댓글 수 = 부모의 기존 누적 개수 + 현재 댓글의 하위 댓글 수 + 현재 댓글 자신 (1)
            replyCountMap.put(
                    parentId,
                    parentReplyCount + currentReplyCount + 1
            );
        }

        // 5. 계산된 replyCount 를 Wrapper 에 적용
        List<CommentProjection> wrappedComments = new ArrayList<>();
        for (CommentProjection comment : flattened) {
            int replyCount = replyCountMap.get(comment.getId());
            wrappedComments.add(new CommentProjectionWrapper(comment, replyCount));
        }

        // 6. Java 페이지네이션
        // 전체 댓글 수
        int totalElements = wrappedComments.size();
        // 전체 페이지 수
        int totalPages = (int) (((long) totalElements + size - 1) / size);
        // 요청한 페이지의 첫 댓글이 전체 목록에서 해당하는 인덱스
        // int 범위를 초과하는 오버플로우 방지를 위해 long 을 사용한다
        long startIndex = (long) (page - 1) * size;
        // 댓글을 가져올 시작 인덱스
        int fromIndex = (int) Math.min(startIndex, totalElements);
        // 댓글을 가져올 마지막 인덱스 경계 (미포함 되는 인덱스)
        int toIndex = (int) Math.min(startIndex + size, totalElements);
        // 댓글 목록 (toIndex 는 미포함)
        List<CommentProjection> content = new ArrayList<>(wrappedComments.subList(fromIndex, toIndex));

        return PageDetail.<CommentProjection>builder()
                .content(content)
                .currentPage(page)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .totalElements((long) totalElements)
                .size(size)
                .build();
    }

    /* 재귀 쿼리 사용 시 → Page<CommentProjection> fetchPageByPostId(postId, pageable)
    @Override
    public PageDetail<CommentProjection> fetchPageByPostId(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of((page - 1), size);
        Page<CommentProjection> commentProjections = commentJpaRepository.fetchPageByPostId(postId, pageable);
        return PageDetail.<CommentProjection>builder()
                .content(commentProjections.getContent())
                .currentPage(page)
                .totalPages(commentProjections.getTotalPages())
                .hasNext(commentProjections.hasNext())
                .hasPrevious(commentProjections.hasPrevious())
                .totalElements(commentProjections.getTotalElements())
                .size(size)
                .build();
    }
    */

    @Override
    public int deleteByPostIdIn(List<Long> postIds) {
        return commentJpaRepository.deleteByPostIdIn(postIds);
    }

    @Override
    public int deleteByIdIn(List<Long> ids) {
        return commentJpaRepository.deleteByIdIn(ids);
    }

    @Override
    public CursorDetail<Comment> findCursorByMemberIdAndIdLessThan(Long memberId, Long lastCommentId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Comment> sliceComments =
                commentJpaRepository.findCursorByMemberIdAndIdLessThan(memberId, lastCommentId, pageable);
        return CursorDetail.<Comment>builder()
                .content(sliceComments.getContent())
                .hasNext(sliceComments.hasNext())
                .build();
    }
}

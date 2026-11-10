package org.demo.backend.post.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostRepository;
import org.demo.backend.post.domain.SortType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return postJpaRepository.findById(postId);
    }

    @Override
    public Optional<Post> fetchById(Long postId) {
        return postJpaRepository.fetchById(postId);
    }

    @Override
    public int incrementLikeById(Long postId) {
        return postJpaRepository.incrementLikeById(postId);
    }

    @Override
    public int decrementLikeById(Long postId) {
        return postJpaRepository.decrementLikeById(postId);
    }

    @Override
    public List<Post> fetchByIdIn(List<Long> postIds) {
        return postJpaRepository.fetchByIdIn(postIds);
    }

    @Override
    public CursorDetail<Post> fetchCursorByIdLessThan(
            SortType sortType,
            Long lastPostId,
            Long lastValue,
            int size,
            String keyword
    ) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> posts;

        if (sortType == SortType.LIKES) {
            posts = postJpaRepository.fetchCursorByIdLessThanLikes(lastPostId, lastValue, keyword, pageable);
        } else if (sortType == SortType.HITS) {
            posts = postJpaRepository.fetchCursorByIdLessThanHits(lastPostId, lastValue, keyword, pageable);
        } else {
            posts = postJpaRepository.fetchCursorByIdLessThanLatest(lastPostId, keyword, pageable);
        }

        return CursorDetail.<Post>builder()
                .content(posts.getContent())
                .hasNext(posts.hasNext())
                .build();
    }

    @Override
    public PageDetail<Post> fetchPageByMemberId(
            Long memberId,
            int page,
            int size
    ) {
        // 오프셋
        int offset = (page - 1) * size;
        // 전체 데이터
        List<Post> content = postJpaRepository.findPageByMemberId(memberId, offset, size);
        // 전체 데이터 개수
        long totalElements = postJpaRepository.countByMemberId(memberId);
        // 전체 페이지 수
        int totalPages = (int) Math.ceil((double) totalElements / size);
        // 이전 버튼
        boolean hasPrevious = page > 1;
        // 다음 버튼
        boolean hasNext = page < totalPages;

        return PageDetail.<Post>builder()
                .content(content)
                .currentPage(page)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .hasPrevious(hasPrevious)
                .hasNext(hasNext)
                .size(size)
                .build();
    }

    @Override
    public CursorDetail<Post> findCursorByIdLessThenAndMemberId(
            Long memberId,
            Long lastPostId,
            int size
    ) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> slicePosts = postJpaRepository.findCursorByIdLessThenAndMemberId(memberId, lastPostId, pageable);
        return CursorDetail.<Post>builder()
                .content(slicePosts.getContent())
                .hasNext(slicePosts.hasNext())
                .build();
    }

    @Override
    public void delete(Post post) {
        postJpaRepository.delete(post);
    }

    @Override
    public int deleteByIdIn(List<Long> ids) {
        return postJpaRepository.deleteByIdIn(ids);
    }
}

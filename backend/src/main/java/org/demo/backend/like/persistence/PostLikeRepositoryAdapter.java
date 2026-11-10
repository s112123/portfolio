package org.demo.backend.like.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.like.domain.PostLike;
import org.demo.backend.like.domain.PostLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryAdapter implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public PostLike save(PostLike postLike) {
        return postLikeJpaRepository.save(postLike);
    }

    @Override
    public Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeJpaRepository.findByMemberIdAndPostId(memberId, postId);
    }

    @Override
    public Page<PostLike> fetchPageByMemberId(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of((page - 1), size);
        return postLikeJpaRepository.fetchPageByMemberId(memberId, pageable);
    }

    @Override
    public void deleteById(Long postLikeId) {
        postLikeJpaRepository.deleteById(postLikeId);
    }

    @Override
    public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeJpaRepository.existsByMemberIdAndPostId(memberId, postId);
    }

    @Override
    public void deleteByPostIdIn(List<Long> postIds) {
        postLikeJpaRepository.deleteByPostIdIn(postIds);
    }

    @Override
    public void deleteByMemberId(Long memberId) {
        postLikeJpaRepository.deleteByMemberId(memberId);
    }
}

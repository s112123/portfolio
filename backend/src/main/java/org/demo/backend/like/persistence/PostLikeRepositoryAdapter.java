package org.demo.backend.like.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.like.domain.PostLike;
import org.demo.backend.like.domain.PostLikeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryAdapter implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public PostLike savePostLike(PostLike postLike) {
        return postLikeJpaRepository.save(postLike);
    }

    @Override
    public Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeJpaRepository.findByMemberIdAndPostId(memberId, postId);
    }

    @Override
    public void deleteById(Long postLikeId) {
        postLikeJpaRepository.deleteById(postLikeId);
    }

    @Override
    public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeJpaRepository.existsByMemberIdAndPostId(memberId, postId);
    }
}

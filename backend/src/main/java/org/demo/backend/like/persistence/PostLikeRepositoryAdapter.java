package org.demo.backend.like.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.like.domain.PostLike;
import org.demo.backend.like.domain.PostLikeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryAdapter implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeRepository;

    @Override
    public PostLike savePostLike(PostLike postLike) {
        return postLikeRepository.save(postLike);
    }

    @Override
    public Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeRepository.findByMemberIdAndPostId(memberId, postId);
    }

    @Override
    public void deleteById(Long postLikeId) {
        postLikeRepository.deleteById(postLikeId);
    }

    @Override
    public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
        return postLikeRepository.existsByMemberIdAndPostId(memberId, postId);
    }
}

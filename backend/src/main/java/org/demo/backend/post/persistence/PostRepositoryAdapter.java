package org.demo.backend.post.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostRepository;
import org.springframework.stereotype.Repository;

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
    public int incrementPostLike(Long postId) {
        return postJpaRepository.incrementPostLike(postId);
    }

    @Override
    public int decrementPostLike(Long postId) {
        return postJpaRepository.decrementPostLike(postId);
    }
}

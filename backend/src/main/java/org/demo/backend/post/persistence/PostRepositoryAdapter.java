package org.demo.backend.post.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImageStatus;
import org.demo.backend.post.domain.PostRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryAdapter implements PostRepository {

    private final PostJpaRepository postRepository;

    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Optional<Post> fetchById(Long postId) {
        return postRepository.fetchById(postId);
    }
}

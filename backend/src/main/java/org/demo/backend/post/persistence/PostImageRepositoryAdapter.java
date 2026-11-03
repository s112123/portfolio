package org.demo.backend.post.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImage;
import org.demo.backend.post.domain.PostImageRepository;
import org.demo.backend.post.domain.PostImageStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostImageRepositoryAdapter implements PostImageRepository {

    private final PostImageJpaRepository postImageJpaRepository;

    @Override
    public PostImage save(PostImage postImage) {
        return postImageJpaRepository.save(postImage);
    }

    @Override
    public Optional<PostImage> findByStoredFilename(String storedFilename) {
        return postImageJpaRepository.findByStoredFilename(storedFilename);
    }

    @Override
    public int attachImages(Post post, List<String> filenames) {
        return postImageJpaRepository.attachImages(post, filenames);
    }

    @Override
    public int detachImages(List<String> filenames) {
        return postImageJpaRepository.detachImages(filenames);
    }

    @Override
    public List<PostImage> findByStatusAndCreatedAtBefore(PostImageStatus status, LocalDateTime threshold) {
        return postImageJpaRepository.findByStatusAndCreatedAtBefore(status, threshold);
    }

    @Override
    public void deleteAllInBatch(List<PostImage> postImages) {
        postImageJpaRepository.deleteAllInBatch(postImages);
    }
}

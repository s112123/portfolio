package org.demo.backend.post.persistence;

import lombok.RequiredArgsConstructor;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostImage;
import org.demo.backend.post.domain.PostImageRepository;
import org.demo.backend.post.domain.PostImageStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostImageRepositoryAdapter implements PostImageRepository {

    private final PostImageJpaRepository postImageJpaRepository;

    @Override
    public PostImage save(PostImage postImage) {
        return postImageJpaRepository.save(postImage);
    }

    @Override
    public int attachImageByFilenameIn(Post post, List<String> filenames) {
        return postImageJpaRepository.attachImageByFilenameIn(post, filenames);
    }

    @Override
    public int detachImageByFilenameIn(List<String> filenames) {
        return postImageJpaRepository.detachImageByFilenameIn(filenames);
    }

    @Override
    public int detachImageByIdIn(List<Long> ids) {
        return postImageJpaRepository.detachImageByIdIn(ids);
    }

    @Override
    public List<PostImage> findByStatusAndCreatedAtBefore(PostImageStatus status, LocalDateTime threshold) {
        return postImageJpaRepository.findByStatusAndCreatedAtBefore(status, threshold);
    }

    @Override
    public void deleteByPostImageIn(List<PostImage> postImages) {
        postImageJpaRepository.deleteAllInBatch(postImages);
    }
}

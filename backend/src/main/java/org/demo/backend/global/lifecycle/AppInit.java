package org.demo.backend.global.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.member.domain.MemberRole;
import org.demo.backend.member.domain.ProfileImageProperties;
import org.demo.backend.post.application.dto.PostDetail;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostRepository;
import org.demo.backend.post.domain.SortType;
import org.demo.backend.post.domain.cache.PostListCacheKey;
import org.demo.backend.post.domain.cache.PostListCacheRepository;
import org.demo.backend.post.persistence.cache.PostListRedisCacheRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppInit {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageProperties profileImageProperties;
    private final PostRepository postRepository;
    private final PostListCacheRepository postListCacheRepository;

    @Bean
    public ApplicationRunner init() {
        return args -> {
            // 최고 관리자 및 관리자 등록
            createMember("root@test.com", "1234", "최고 관리자", MemberRole.ROOT);
            createMember("admin@test.com", "1234", "관리자", MemberRole.ADMIN);

            // 글목록 캐시 웜업
            try {
                // 최신순
                warmUpPostListCaches(SortType.LATEST);
                // 좋아요순
                warmUpPostListCaches(SortType.LIKES);
                // 조회순
                warmUpPostListCaches(SortType.HITS);
            } catch (Exception e) {
                log.warn("[Init] 글 목록 캐시 웜업 실패");
            }

            log.info("[Init] 애플리케이션 데이터 초기화 완료");
        };
    }

    // 회원 등록
    private void createMember(String email, String password, String nickname, MemberRole role) {
        if (!memberRepository.existsByEmail(email)) {
            Member member = Member.of(
                    email,
                    passwordEncoder.encode(password),
                    nickname,
                    profileImageProperties.getDefaultImageUrl(),
                    LocalDateTime.now()
            );
            member.updateRole(role);
            memberRepository.save(member);
        }
    }

    // 글 목록 캐시 웜업
    private void warmUpPostListCaches(SortType sortType) {
        postListCacheRepository.deleteKey(sortType);
        CursorDetail<Post> posts = postRepository.fetchCursorByIdLessThan(
                sortType,
                null,
                null,
                PostListRedisCacheRepository.MAX_CACHE_COUNT,
                null
        );
        CursorDetail<PostDetail> postDetails = posts.map(p -> PostDetail.summaryOf(p));
        postListCacheRepository.savePostList(sortType, postDetails.getContent());
    }
}

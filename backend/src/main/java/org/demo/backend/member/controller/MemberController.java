package org.demo.backend.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.web.dto.ApiResponse;
import org.demo.backend.global.web.dto.PageDetail;
import org.demo.backend.member.application.MemberService;
import org.demo.backend.member.application.dto.MemberDetail;
import org.demo.backend.member.controller.dto.MemberCreateRequest;
import org.demo.backend.member.controller.dto.PasswordChangeRequest;
import org.demo.backend.post.application.PostService;
import org.demo.backend.post.application.dto.PostDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PostService postService;

    // 회원 등록
    @PostMapping
    public ResponseEntity<ApiResponse<MemberDetail>> create(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberDetail memberDetail = memberService.save(request);
        URI location = URI.create(String.format("/api/v1/members/%d", memberDetail.getId()));
        return ResponseEntity.created(location).body(ApiResponse.success(memberDetail));
    }

    // 회원 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberDetail>> readById(
            @PathVariable("memberId") Long memberId
    ) {
        MemberDetail memberDetail = memberService.findById(memberId);
        return ResponseEntity.ok().body(ApiResponse.success(memberDetail));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberDetail>> readMe(
            @AuthenticationPrincipal Long myId
    ) {
        MemberDetail memberDetail = memberService.findById(myId);
        return ResponseEntity.ok().body(ApiResponse.success(memberDetail));
    }

    // 내가 쓴 글 목록
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageDetail<PostDetail>>> readMyPosts(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {
        PageDetail<PostDetail> postDetail = postService.fetchPageByMemberId(memberId, page < 1 ? 1 : page, 5);
        return ResponseEntity.ok().body(ApiResponse.success(postDetail));
    }

    // 비밀번호 변경
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        String rePassword = memberService.changePassword(request);
        return ResponseEntity.ok().body(ApiResponse.success(rePassword));
    }

    // 회원 삭제
    @DeleteMapping
    public ResponseEntity<?> deleteMe(
            @AuthenticationPrincipal Long myId
    ) {
        memberService.deleteById(myId);
        return ResponseEntity.noContent().build();
    }
}

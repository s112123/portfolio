package org.demo.backend.global.jwt.application.dto;

import lombok.*;
import org.demo.backend.global.jwt.domain.JwtType;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JwtDetail {

    private Long memberId;
    private JwtType type;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}

package org.demo.backend.common.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Long memberId = annotation.memberId();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(memberId, "", Collections.emptyList());
        context.setAuthentication(authentication);
        return context;
    }
}

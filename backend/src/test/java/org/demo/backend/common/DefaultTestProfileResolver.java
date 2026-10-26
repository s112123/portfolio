package org.demo.backend.common;

import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.util.StringUtils;

public class DefaultTestProfileResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(Class<?> testClass) {
        // 1. JVM 시스템 프로퍼티 (-Dspring.profiles.active=...) 확인
        String profile = System.getProperty("spring.profiles.active");
        // 2. 환경 변수 확인
        if (!StringUtils.hasText(profile)) {
            profile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        // 3. 지정된 프로파일이 없다면(IDE 단독 실행 등) 기본값 "test" 적용
        if (!StringUtils.hasText(profile)) {
            return new String[] { "test" };
        }
        return new String[] { profile };
    }
}

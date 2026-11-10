package org.demo.backend.global.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {

                    @Override
                    protected Resource resolveResourceInternal(
                            HttpServletRequest request,
                            String requestPath,
                            List<? extends Resource> locations, ResourceResolverChain chain
                    ) {
                        // 1. 요청한 리소스가 실제 존재하는지 확인
                        Resource resource = super.resolveResourceInternal(request, requestPath, locations, chain);
                        // 2. 파일이 존재하면(js, css, 이미지 등) 그대로 반환
                        if (resource != null) {
                            return resource;
                        }
                        // 3. 존재하지 않는 경로(예: /profile 새로고침)라면 index.html 반환
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}

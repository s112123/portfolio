package org.demo.backend.member.domain;

import lombok.Getter;
import lombok.Setter;
import org.demo.backend.post.domain.PostImageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "file-upload.member")
@Getter
@Setter
public class ProfileImageProperties {

    private String baseUrl;
    private ProfileImageProperties.DefaultImage defaultImage;
    private String storage;

    public String getDefaultImageUrl() {
        return getImageUrl(defaultImage.getFile());
    }

    public String getImageUrl(String filename) {
        return baseUrl + filename;
    }

    @Getter
    @Setter
    public static class DefaultImage {

        private String path;
        private String file;
    }
}

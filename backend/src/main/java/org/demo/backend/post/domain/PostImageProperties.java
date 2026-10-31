package org.demo.backend.post.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.PostPersist;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "file-upload.post")
@Getter
@Setter
public class PostImageProperties {

    private String baseUrl;
    private DefaultImage defaultImage;
    private String storage;

    public String getDefaultImageUrl() {
        return getImageUrl(defaultImage.getFile());
    }

    public String getImageUrl(String filename) {
        return baseUrl + filename;
    }

    public String getFirstImageUrl(List<String> filenames) {
        return filenames.isEmpty() ? getDefaultImageUrl() : getImageUrl(filenames.get(0));
    }

    @Getter
    @Setter
    public static class DefaultImage {

        private String path;
        private String file;
    }
}

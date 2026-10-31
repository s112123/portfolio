package org.demo.backend.global.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDetail {

    private String originalFilename;
    private String storedFilename;
    private String FilePath;
}

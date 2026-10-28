package org.demo.backend.global.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSerializer {

    private final ObjectMapper objectMapper;

    // 직렬화
    public String serializeOrException(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("[직렬화 실패] data={}", data, e);
            throw new RuntimeException(e);
        }
    }

    // 역직렬화
    public <T> T deserializeOrNull(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            log.error("[역직렬화 실패] data={}", data, e);
            return null;
        }
    }
}

package org.demo.backend.global.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorDetail<T> {

    @Builder.Default
    private List<T> content = new ArrayList<>();
    private boolean hasNext;

    public <R> CursorDetail<R> map(Function<T, R> converter) {
        List<R> converted = this.content.stream()
                .map(converter)
                .collect(Collectors.toList());
        return CursorDetail.<R>builder()
                .content(converted)
                .hasNext(this.hasNext)
                .build();
    }
}

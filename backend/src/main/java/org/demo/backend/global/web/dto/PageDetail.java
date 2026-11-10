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
public class PageDetail<T> {

    @Builder.Default
    private List<T> content = new ArrayList<>();
    private int currentPage;
    private int totalPages;
    private boolean hasPrevious;
    private boolean hasNext;
    private long totalElements;
    private int size;

    public <R> PageDetail<R> map(Function<T, R> converter) {
        List<R> convertedContent = this.content.stream()
                .map(converter)
                .collect(Collectors.toList());

        return PageDetail.<R>builder()
                .content(convertedContent)
                .currentPage(this.currentPage)
                .totalPages(this.totalPages)
                .totalElements(this.totalElements)
                .hasNext(this.hasNext)
                .hasPrevious(this.hasPrevious)
                .size(this.size)
                .build();
    }
}

package com.aiinterview.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> list;
    private int page;
    private int pageSize;
    private long total;
    private boolean hasMore;

    public static <T> PageResponse<T> of(List<T> list, int page, int pageSize, long total) {
        boolean hasMore = (long) page * pageSize < total;
        return new PageResponse<>(list, page, pageSize, total, hasMore);
    }
}

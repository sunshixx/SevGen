package com.aichat.roleplay.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * @author hxg111
 * @date 2025/9/25 22:20
 * @description: TODo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private Long nextCursor;  // 下一页游标（比如最后一条 messageId）
    private boolean hasMore;
}

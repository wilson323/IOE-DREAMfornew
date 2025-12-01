package net.lab1024.sa.base.common.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 缓存操作结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CacheResult<T> {

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 缓存数据
     */
    private T data;

    /**
     * 是否命中缓存
     */
    private Boolean hit;

    /**
     * 操作耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 缓存键
     */
    private String cacheKey;

    /**
     * 缓存命名空间
     */
    private String namespace;

    public static <T> CacheResult<T> success(T data) {
        return CacheResult.<T>builder()
                .success(true)
                .data(data)
                .hit(true)
                .operationTime(LocalDateTime.now())
                .build();
    }

    public static <T> CacheResult<T> success(T data, Boolean hit) {
        return CacheResult.<T>builder()
                .success(true)
                .data(data)
                .hit(hit)
                .operationTime(LocalDateTime.now())
                .build();
    }

    public static <T> CacheResult<T> fail(String errorMessage) {
        return CacheResult.<T>builder()
                .success(false)
                .errorMessage(errorMessage)
                .operationTime(LocalDateTime.now())
                .build();
    }

    public static <T> CacheResult<T> miss() {
        return CacheResult.<T>builder()
                .success(true)
                .hit(false)
                .operationTime(LocalDateTime.now())
                .build();
    }
}
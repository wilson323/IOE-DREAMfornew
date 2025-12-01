package net.lab1024.sa.common.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 缓存结果封装
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-24
 * @Copyright SmartAdmin v3
 */
@Data
@Builder
@Schema(description = "缓存结果")
public class CacheResult<T> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "缓存键")
    private String cacheKey;

    public CacheResult() {
    }

    public CacheResult(Boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public CacheResult(Boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public CacheResult(Boolean success, T data, String errorMessage, String cacheKey) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.cacheKey = cacheKey;
    }

    public static <T> CacheResult<T> success(T data) {
        return new CacheResult<>(true, data);
    }

    public static <T> CacheResult<T> success(T data, String cacheKey) {
        CacheResult<T> result = new CacheResult<>(true, data);
        result.setCacheKey(cacheKey);
        return result;
    }

    public static <T> CacheResult<T> failure(String errorMessage) {
        return new CacheResult<>(false, null, errorMessage);
    }

    public static <T> CacheResult<T> failure(String errorMessage, String cacheKey) {
        CacheResult<T> result = new CacheResult<>(false, null, errorMessage);
        result.setCacheKey(cacheKey);
        return result;
    }

    /**
     * 创建缓存未命中的结果
     *
     * @param <T> 数据类型
     * @return 缓存未命中结果
     */
    public static <T> CacheResult<T> miss() {
        return new CacheResult<>(false, null, "Cache miss");
    }

    /**
     * 创建缓存未命中的结果（带缓存键）
     *
     * @param cacheKey 缓存键
     * @param <T> 数据类型
     * @return 缓存未命中结果
     */
    public static <T> CacheResult<T> miss(String cacheKey) {
        CacheResult<T> result = new CacheResult<>(false, null, "Cache miss");
        result.setCacheKey(cacheKey);
        return result;
    }
}

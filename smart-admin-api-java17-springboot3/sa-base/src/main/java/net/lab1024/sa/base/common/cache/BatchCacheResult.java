package net.lab1024.sa.base.common.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 批量缓存操作结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchCacheResult<T> {

    /**
     * 操作是否成功
     */
    private Boolean success;

    /**
     * 成功的缓存数据
     */
    private Map<String, T> successData;

    /**
     * 失败的缓存键及错误信息
     */
    private Map<String, String> failedKeys;

    /**
     * 命中的缓存键
     */
    private List<String> hitKeys;

    /**
     * 未命中的缓存键
     */
    private List<String> missKeys;

    /**
     * 总操作数量
     */
    private Integer totalCount;

    /**
     * 成功操作数量
     */
    private Integer successCount;

    /**
     * 失败操作数量
     */
    private Integer failureCount;

    /**
     * 命中数量
     */
    private Integer hitCount;

    /**
     * 操作耗时（毫秒）
     */
    private Long duration;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 缓存命名空间
     */
    private String namespace;

    /**
     * 计算统计信息
     */
    public void calculateStats() {
        if (totalCount == null) {
            totalCount = (successData != null ? successData.size() : 0) +
                         (failedKeys != null ? failedKeys.size() : 0);
        }

        successCount = successData != null ? successData.size() : 0;
        failureCount = failedKeys != null ? failedKeys.size() : 0;
        hitCount = hitKeys != null ? hitKeys.size() : 0;
    }

    /**
     * 获取命中率
     */
    public Double getHitRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) hitCount / totalCount * 100;
    }

    /**
     * 获取成功率
     */
    public Double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    public static <T> BatchCacheResult<T> success(Map<String, T> data) {
        BatchCacheResult<T> result = new BatchCacheResult<>();
        result.setSuccess(true);
        result.setSuccessData(data);
        result.setOperationTime(LocalDateTime.now());
        result.calculateStats();
        return result;
    }

    public static <T> BatchCacheResult<T> fail(Map<String, String> failedKeys) {
        BatchCacheResult<T> result = new BatchCacheResult<>();
        result.setSuccess(false);
        result.setFailedKeys(failedKeys);
        result.setOperationTime(LocalDateTime.now());
        result.calculateStats();
        return result;
    }
}
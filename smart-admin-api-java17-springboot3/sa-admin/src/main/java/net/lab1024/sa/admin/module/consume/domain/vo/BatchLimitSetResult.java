package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量限制设置结果VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "批量限制设置结果")
public class BatchLimitSetResult {

    @Schema(description = "总设置数量")
    private Integer totalSettings;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failureCount;

    @Schema(description = "失败的用户ID列表")
    private List<Long> failedUserIds;

    @Schema(description = "失败详情列表")
    private List<String> failureDetails;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "操作员ID")
    private Long operatorId;

    @Schema(description = "批量操作ID")
    private String batchOperationId;

    /**
     * 获取成功率
     */

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return totalSettings != null &&
               successCount != null &&
               totalSettings.equals(successCount);
    }

    /**
     * 是否有失败
     */
    public boolean hasFailures() {
        return failureCount != null && failureCount > 0;
    }

    /**
     * 创建成功结果
     */
    public static BatchLimitSetResult success(Integer totalSettings, Integer successCount) {
        return BatchLimitSetResult.builder()
                .totalSettings(totalSettings)
                .successCount(successCount)
                .failureCount(totalSettings - successCount)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static BatchLimitSetResult failure(Integer totalSettings, Integer failureCount, String failureReason) {
        return BatchLimitSetResult.builder()
                .totalSettings(totalSettings)
                .successCount(totalSettings - failureCount)
                .failureCount(failureCount)
                .failureReason(failureReason)
                .operationTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)


















}

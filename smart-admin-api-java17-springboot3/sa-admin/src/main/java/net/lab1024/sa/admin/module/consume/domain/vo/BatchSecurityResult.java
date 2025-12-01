package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量安全操作结果VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "批量安全操作结果")
public class BatchSecurityResult {

    @Schema(description = "总数量")
    private Integer totalCount;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failureCount;

    @Schema(description = "失败的用户ID列表")
    private List<Long> failedUserIds;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    // 手动添加的getter/setter方法 (Lombok失效备用)












}

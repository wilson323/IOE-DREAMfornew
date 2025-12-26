package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 离线同步日志Entity
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_offline_sync_log")
@Schema(description = "离线同步日志")
public class OfflineSyncLogEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "同步批次号")
    private String syncBatchNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "同步类型 1-自动同步 2-手动同步 3-批量同步")
    private Integer syncType;

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "冲突数量")
    private Integer conflictCount;

    @Schema(description = "同步状态 0-进行中 1-成功 2-部分失败 3-全部失败")
    private Integer syncStatus;

    @Schema(description = "错误汇总")
    private String errorSummary;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时（毫秒）")
    private Long durationMs;

    @Schema(description = "创建人")
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

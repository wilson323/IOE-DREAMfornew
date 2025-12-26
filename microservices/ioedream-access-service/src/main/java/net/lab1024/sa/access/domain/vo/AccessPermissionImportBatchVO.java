package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁权限导入批次VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "门禁权限导入批次")
public class AccessPermissionImportBatchVO {

    /**
     * 批次ID
     */
    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    /**
     * 批次名称
     */
    @Schema(description = "批次名称", example = "2025-01权限导入")
    private String batchName;

    /**
     * 导入状态
     */
    @Schema(description = "导入状态（PENDING-待处理 PROCESSING-处理中 SUCCESS-成功 FAILED-失败 CANCELLED-已取消）",
             example = "SUCCESS")
    private String importStatus;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Integer totalCount;

    /**
     * 成功数
     */
    @Schema(description = "成功数", example = "95")
    private Integer successCount;

    /**
     * 失败数
     */
    @Schema(description = "失败数", example = "5")
    private Integer errorCount;

    /**
     * 处理进度
     */
    @Schema(description = "处理进度（%）", example = "100")
    private Integer progress;

    /**
     * 文件名
     */
    @Schema(description = "文件名", example = "permissions.xlsx")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）", example = "10240")
    private Long fileSize;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    /**
     * 错误消息（失败时）
     */
    @Schema(description = "错误消息（失败时）", example = "数据格式错误")
    private String errorMessage;

    /**
     * 异步任务ID（异步导入时）
     */
    @Schema(description = "异步任务ID", example = "task-123456")
    private String asyncTaskId;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-12-26T10:30:00")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2025-12-26T10:35:00")
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-26T10:30:00")
    private LocalDateTime createTime;
}

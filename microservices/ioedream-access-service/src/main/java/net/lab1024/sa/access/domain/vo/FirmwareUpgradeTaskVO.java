package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 固件升级任务VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件升级任务VO")
public class FirmwareUpgradeTaskVO {

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "任务名称", example = "2025年1月门禁设备固件升级")
    private String taskName;

    @Schema(description = "任务编号", example = "UPG202512250001")
    private String taskNo;

    @Schema(description = "固件ID", example = "1")
    private Long firmwareId;

    @Schema(description = "固件版本号", example = "v1.0.0")
    private String firmwareVersion;

    @Schema(description = "升级策略：1-立即升级 2-定时升级 3-分批升级", example = "1")
    private Integer upgradeStrategy;

    @Schema(description = "升级策略名称", example = "立即升级")
    private String upgradeStrategyName;

    @Schema(description = "定时升级时间")
    private LocalDateTime scheduleTime;

    @Schema(description = "分批大小", example = "10")
    private Integer batchSize;

    @Schema(description = "分批间隔（秒）", example = "300")
    private Integer batchInterval;

    // ==================== 统计信息 ====================

    @Schema(description = "目标设备总数", example = "100")
    private Integer targetDeviceCount;

    @Schema(description = "升级成功数量", example = "95")
    private Integer successCount;

    @Schema(description = "升级失败数量", example = "3")
    private Integer failedCount;

    @Schema(description = "待升级数量", example = "2")
    private Integer pendingCount;

    @Schema(description = "升级中数量", example = "0")
    private Integer upgradingCount;

    @Schema(description = "已回滚数量", example = "0")
    private Integer rollbackCount;

    @Schema(description = "升级进度（百分比）", example = "95.00")
    private BigDecimal upgradeProgress;

    // ==================== 状态信息 ====================

    @Schema(description = "任务状态：1-待执行 2-执行中 3-已暂停 4-已完成 5-已失败", example = "2")
    private Integer taskStatus;

    @Schema(description = "任务状态名称", example = "执行中")
    private String taskStatusName;

    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "执行耗时（秒）", example = "1200")
    private Integer durationSeconds;

    @Schema(description = "执行耗时（格式化）", example = "20分钟")
    private String durationFormatted;

    // ==================== 操作信息 ====================

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    // ==================== 回滚支持 ====================

    @Schema(description = "是否支持回滚：0-否 1-是", example = "1")
    private Integer rollbackSupported;

    @Schema(description = "回滚任务ID", example = "2")
    private Long rollbackTaskId;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "备注", example = "紧急安全补丁升级")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

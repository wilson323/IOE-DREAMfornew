package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 固件升级任务实体
 * <p>
 * 核心职责：
 * - 固件升级任务管理
 * - 升级策略控制（立即、定时、分批）
 * - 升级进度跟踪
 * - 升级结果统计
 * - 回滚支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_firmware_upgrade_task")
@Schema(description = "固件升级任务实体")
public class FirmwareUpgradeTaskEntity extends BaseEntity {

    @TableId(value = "task_id", type = IdType.AUTO)
    @Schema(description = "任务ID（主键）", example = "1")
    private Long taskId;

    // ==================== 基本信息 ====================

    @Schema(description = "任务名称", example = "2025年1月门禁设备固件升级")
    private String taskName;

    @Schema(description = "任务编号（如：UPG202512250001）", example = "UPG202512250001")
    private String taskNo;

    @Schema(description = "固件ID", example = "1")
    private Long firmwareId;

    @Schema(description = "固件版本号", example = "v1.0.0")
    private String firmwareVersion;

    // ==================== 升级策略 ====================

    @Schema(description = "升级策略：1-立即升级 2-定时升级 3-分批升级", example = "1")
    private Integer upgradeStrategy;

    @Schema(description = "定时升级时间（strategy=2时使用）")
    private LocalDateTime scheduleTime;

    @Schema(description = "分批大小（strategy=3时使用）", example = "10")
    private Integer batchSize;

    @Schema(description = "分批间隔（秒）", example = "300")
    private Integer batchInterval;

    // ==================== 设备统计 ====================

    @Schema(description = "目标设备总数", example = "100")
    private Integer targetDeviceCount;

    @Schema(description = "升级成功数量", example = "95")
    private Integer successCount;

    @Schema(description = "升级失败数量", example = "3")
    private Integer failedCount;

    @Schema(description = "待升级数量", example = "2")
    private Integer pendingCount;

    @Schema(description = "升级进度（百分比）", example = "95.00")
    private BigDecimal upgradeProgress;

    // ==================== 任务状态 ====================

    @Schema(description = "任务状态：1-待执行 2-执行中 3-已暂停 4-已完成 5-已失败", example = "2")
    private Integer taskStatus;

    @Schema(description = "开始执行时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "执行耗时（秒）", example = "1200")
    private Integer durationSeconds;

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

    // ==================== 逻辑删除（覆盖BaseEntity） ====================
    // 注意：SQL表使用TINYINT类型，需要Integer而非Boolean
    @Schema(description = "逻辑删除：0-未删除 1-已删除", example = "0")
    @TableField("deleted")
    private Integer deletedFlag;

    // ==================== 显式添加 getter/setter 方法以确保编译通过 ====================

    public Integer getTaskStatus() {
        return taskStatus;
    }
}

package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备控制结果VO
 * 用于返回设备控制操作的结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备控制结果VO")
public class DeviceControlResultVO {

    @Schema(description = "控制任务ID", example = "TASK20251216001")
    private String taskId;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备名称", example = "主入口门禁")
    private String deviceName;

    @Schema(description = "控制命令", example = "restart")
    private String command;

    @Schema(description = "控制状态", example = "success")
    private String status;

    @Schema(description = "控制状态描述", example = "执行成功")
    private String statusDesc;

    @Schema(description = "执行结果", example = "设备重启完成")
    private String result;

    @Schema(description = "执行时间(毫秒)", example = "2500")
    private Long executionTime;

    @Schema(description = "开始时间", example = "2025-12-16T14:30:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T14:30:02")
    private LocalDateTime endTime;

    @Schema(description = "操作员", example = "admin")
    private String operator;

    @Schema(description = "操作原因", example = "定期维护重启")
    private String reason;

    @Schema(description = "是否需要后续操作", example = "false")
    private Boolean needFollowUp;

    @Schema(description = "后续操作描述", example = "无")
    private String followUpAction;

    @Schema(description = "错误信息", example = "")
    private String errorMessage;

    @Schema(description = "错误代码", example = "")
    private String errorCode;

    // 设备状态变化

    @Schema(description = "控制前设备状态", example = "1")
    private Integer beforeStatus;

    @Schema(description = "控制后设备状态", example = "1")
    private Integer afterStatus;

    @Schema(description = "控制前设备状态名称", example = "在线")
    private String beforeStatusName;

    @Schema(description = "控制后设备状态名称", example = "在线")
    private String afterStatusName;

    // 详细执行信息

    @Schema(description = "执行步骤数", example = "3")
    private Integer totalSteps;

    @Schema(description = "已执行步骤数", example = "3")
    private Integer completedSteps;

    @Schema(description = "当前执行步骤", example = "设备重启验证")
    private String currentStep;

    @Schema(description = "执行进度(百分比)", example = "100")
    private Integer progress;

    @Schema(description = "设备响应时间(毫秒)", example = "1200")
    private Long deviceResponseTime;

    @Schema(description = "网络延迟(毫秒)", example = "50")
    private Long networkLatency;

    // 维护相关

    @Schema(description = "维护开始时间", example = "2025-12-16T14:30:00")
    private LocalDateTime maintenanceStartTime;

    @Schema(description = "维护结束时间", example = "2025-12-17T14:30:00")
    private LocalDateTime maintenanceEndTime;

    @Schema(description = "维护持续时间(小时)", example = "24")
    private Integer maintenanceDuration;

    // 校准相关

    @Schema(description = "校准类型", example = "face")
    private String calibrationType;

    @Schema(description = "校准精度", example = "high")
    private String calibrationPrecision;

    @Schema(description = "校准前准确率", example = "95.5")
    private Double beforeAccuracy;

    @Schema(description = "校准后准确率", example = "98.2")
    private Double afterAccuracy;

    // 日志记录

    @Schema(description = "操作日志ID", example = "LOG20251216001")
    private String logId;

    @Schema(description = "审计记录ID", example = "AUDIT20251216001")
    private String auditId;

    @Schema(description = "通知消息", example = "设备重启操作已完成")
    private String notificationMessage;

    @Schema(description = "是否发送通知", example = "true")
    private Boolean notificationSent;
}
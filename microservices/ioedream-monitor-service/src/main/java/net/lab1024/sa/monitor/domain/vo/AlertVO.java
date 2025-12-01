package net.lab1024.sa.monitor.domain.vo;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 告警VO
 *
 * 用于展示告警的详细信息，包括告警基本信息、状态、处理情况等
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "告警VO")
public class AlertVO {

    /**
     * 告警ID
     */
    @Schema(description = "告警ID", example = "1")
    private Long alertId;

    /**
     * 告警规则ID
     */
    @Schema(description = "告警规则ID", example = "1")
    private Long ruleId;

    /**
     * 告警标题
     */
    @Schema(description = "告警标题", example = "CPU使用率过高")
    private String alertTitle;

    /**
     * 告警描述
     */
    @Schema(description = "告警描述", example = "CPU使用率超过阈值80%")
    private String alertDescription;

    /**
     * 告警级别 (INFO、WARNING、ERROR、CRITICAL)
     */
    @Schema(description = "告警级别", example = "WARNING")
    private String alertLevel;

    /**
     * 告警类型 (SYSTEM、SERVICE、PERFORMANCE、SECURITY)
     */
    @Schema(description = "告警类型", example = "PERFORMANCE")
    private String alertType;

    /**
     * 关联服务
     */
    @Schema(description = "关联服务", example = "ioedream-system-service")
    private String serviceName;

    /**
     * 关联实例
     */
    @Schema(description = "关联实例", example = "instance-001")
    private String instanceId;

    /**
     * 告警状态 (ACTIVE、RESOLVED、SUPPRESSED)
     */
    @Schema(description = "告警状态", example = "ACTIVE")
    private String status;

    /**
     * 告警源
     */
    @Schema(description = "告警源", example = "Prometheus")
    private String alertSource;

    /**
     * 告警标签
     */
    @Schema(description = "告警标签", example = "[\"cpu\",\"performance\"]")
    private String alertTags;

    /**
     * 告警值
     */
    @Schema(description = "告警值", example = "85.5")
    private Double alertValue;

    /**
     * 阈值
     */
    @Schema(description = "阈值", example = "80.0")
    private Double thresholdValue;

    /**
     * 告警时间
     */
    @Schema(description = "告警时间", example = "2025-01-30 10:30:00")
    private LocalDateTime alertTime;

    /**
     * 解决时间
     */
    @Schema(description = "解决时间", example = "2025-01-30 11:00:00")
    private LocalDateTime resolveTime;

    /**
     * 解决人ID
     */
    @Schema(description = "解决人ID", example = "1")
    private Long resolveUserId;

    /**
     * 解决人姓名
     */
    @Schema(description = "解决人姓名", example = "张三")
    private String resolveUserName;

    /**
     * 解决说明
     */
    @Schema(description = "解决说明", example = "已重启服务，问题已解决")
    private String resolution;

    /**
     * 通知状态 (PENDING、SENT、FAILED)
     */
    @Schema(description = "通知状态", example = "SENT")
    private String notificationStatus;

    /**
     * 通知次数
     */
    @Schema(description = "通知次数", example = "3")
    private Integer notificationCount;

    /**
     * 最后通知时间
     */
    @Schema(description = "最后通知时间", example = "2025-01-30 10:35:00")
    private LocalDateTime lastNotificationTime;

    /**
     * 持续时间（秒）
     */
    @Schema(description = "持续时间（秒）", example = "1800")
    private Long duration;

    /**
     * 是否已确认
     */
    @Schema(description = "是否已确认", example = "true")
    private Boolean acknowledged;

    /**
     * 确认人
     */
    @Schema(description = "确认人", example = "李四")
    private String acknowledgedBy;

    /**
     * 确认时间
     */
    @Schema(description = "确认时间", example = "2025-01-30 10:32:00")
    private LocalDateTime acknowledgedTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-30 10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-30 11:00:00")
    private LocalDateTime updateTime;
}

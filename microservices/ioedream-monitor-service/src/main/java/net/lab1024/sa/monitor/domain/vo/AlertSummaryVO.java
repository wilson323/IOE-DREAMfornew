package net.lab1024.sa.monitor.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警摘要VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class AlertSummaryVO {

    /**
     * 告警ID
     */
    private Long alertId;

    /**
     * 告警标题
     */
    private String alertTitle;

    /**
     * 告警描述
     */
    private String alertDescription;

    /**
     * 告警级别 (INFO、WARNING、ERROR、CRITICAL)
     */
    private String alertLevel;

    /**
     * 告警类型 (SYSTEM、SERVICE、PERFORMANCE、SECURITY)
     */
    private String alertType;

    /**
     * 关联服务
     */
    private String serviceName;

    /**
     * 关联实例
     */
    private String instanceId;

    /**
     * 告警状态 (ACTIVE、RESOLVED、SUPPRESSED)
     */
    private String status;

    /**
     * 告警源
     */
    private String alertSource;

    /**
     * 告警值
     */
    private Double alertValue;

    /**
     * 阈值
     */
    private Double thresholdValue;

    /**
     * 告警时间
     */
    private LocalDateTime alertTime;

    /**
     * 持续时间（秒）
     */
    private Long duration;

    /**
     * 影响范围
     */
    private String impactScope;

    /**
     * 建议措施
     */
    private String recommendedActions;

    /**
     * 相关标签
     */
    private List<String> tags;

    /**
     * 通知状态
     */
    private String notificationStatus;

    /**
     * 分配给的运维人员
     */
    private String assignedTo;

    /**
     * 是否已确认
     */
    private Boolean acknowledged;

    /**
     * 确认人
     */
    private String acknowledgedBy;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedTime;
}
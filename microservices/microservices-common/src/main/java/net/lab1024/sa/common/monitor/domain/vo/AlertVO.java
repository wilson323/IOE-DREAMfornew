package net.lab1024.sa.common.monitor.domain.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 告警VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@JsonFormat格式化时间字段
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class AlertVO {

    /**
     * 告警ID
     */
    private Long alertId;

    /**
     * 告警级别
     */
    private String alertLevel;

    /**
     * 告警标题
     */
    private String alertTitle;

    /**
     * 告警消息
     */
    private String alertMessage;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 监控指标
     */
    private String metricName;

    /**
     * 指标值
     */
    private Double metricValue;

    /**
     * 阈值
     */
    private Double thresholdValue;

    /**
     * 状态
     */
    private String status;

    /**
     * 解决说明
     */
    private String resolutionNotes;

    /**
     * 解决时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}


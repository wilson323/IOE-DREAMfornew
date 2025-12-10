package net.lab1024.sa.common.performance.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 性能告警
 * <p>
 * JVM性能指标的告警信息
 * 包含告警级别、类型、描述等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class PerformanceAlert {

    /**
     * 告警ID
     */
    private String alertId;

    /**
     * 告警级别
     */
    private String level;

    /**
     * 告警类型
     */
    private String type;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 告警值
     */
    private double value;

    /**
     * 阈值
     */
    private double threshold;

    /**
     * 告警时间
     */
    private LocalDateTime alertTime;

    /**
     * 是否已处理
     */
    private boolean handled;
}
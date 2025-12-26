package net.lab1024.sa.common.monitor.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警摘要视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class AlertSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 创建时间
     */
    private LocalDateTime createTime;
}


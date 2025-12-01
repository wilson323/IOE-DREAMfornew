package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 生物识别告警VO
 * <p>
 * 用于返回生物识别系统的告警信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class BiometricAlertVO {

    /**
     * 告警ID
     */
    private Long alertId;

    /**
     * 告警类型
     */
    private String alertType;

    /**
     * 告警级别 1-低 2-中 3-高 4-紧急
     */
    private Integer alertLevel;

    /**
     * 告警标题
     */
    private String alertTitle;

    /**
     * 告警内容
     */
    private String alertContent;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 告警时间
     */
    private LocalDateTime alertTime;

    /**
     * 处理状态 0-未处理 1-处理中 2-已处理
     */
    private Integer handleStatus;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人
     */
    private String handler;
}

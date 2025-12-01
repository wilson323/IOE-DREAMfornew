package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 生物识别设备状态VO
 * <p>
 * 用于返回生物识别设备的状态信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class BiometricStatusVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备状态 0-离线 1-在线 2-故障
     */
    private Integer deviceStatus;

    /**
     * 在线状态
     */
    private Boolean online;

    /**
     * CPU使用率
     */
    private Double cpuUsage;

    /**
     * 内存使用率
     */
    private Double memoryUsage;

    /**
     * 磁盘使用率
     */
    private Double diskUsage;

    /**
     * 今日识别次数
     */
    private Long todayRecognitions;

    /**
     * 今日成功率
     */
    private Double todaySuccessRate;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后识别时间
     */
    private LocalDateTime lastRecognitionTime;
}

package net.lab1024.sa.device.domain.vo;

import java.util.Map;

import lombok.Data;

/**
 * 设备健康统计视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
public class DeviceHealthStatisticsVO {

    /**
     * 总设备数
     */
    private Integer totalDevices;

    /**
     * 正常设备数
     */
    private Integer healthyDevices;

    /**
     * 警告设备数
     */
    private Integer warningDevices;

    /**
     * 严重设备数
     */
    private Integer criticalDevices;

    /**
     * 故障设备数
     */
    private Integer faultyDevices;

    /**
     * 平均健康评分
     */
    private Double averageHealthScore;

    /**
     * 健康等级分布
     */
    private Map<String, Integer> healthLevelDistribution;

    /**
     * 设备类型统计
     */
    private Map<String, Map<String, Integer>> deviceTypeStatistics;

    /**
     * 告警统计
     */
    private Map<String, Integer> alarmStatistics;

    /**
     * 维护统计
     */
    private Map<String, Integer> maintenanceStatistics;
}

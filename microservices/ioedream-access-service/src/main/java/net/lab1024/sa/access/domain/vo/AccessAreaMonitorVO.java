package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 门禁区域监控视图对象
 * <p>
 * 用于区域空间管理模块显示区域监控数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessAreaMonitorVO {

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 设备状态统计
     */
    private DeviceStatusStatistics deviceStatus;

    /**
     * 人员通行统计
     */
    private AccessStatistics accessStatistics;

    /**
     * 容量监控
     */
    private CapacityMonitor capacityMonitor;

    /**
     * 设备状态列表
     */
    private List<DeviceStatusInfo> deviceStatusList;

    /**
     * 最近通行记录
     */
    private List<RecentAccessRecord> recentAccessRecords;

    /**
     * 设备状态统计
     */
    @Data
    public static class DeviceStatusStatistics {
        /**
         * 设备总数
         */
        private Long totalDevices;

        /**
         * 在线设备数
         */
        private Long onlineDevices;

        /**
         * 离线设备数
         */
        private Long offlineDevices;

        /**
         * 故障设备数
         */
        private Long faultDevices;

        /**
         * 维护设备数
         */
        private Long maintenanceDevices;
    }

    /**
     * 通行统计
     */
    @Data
    public static class AccessStatistics {
        /**
         * 今日通行总数
         */
        private Long todayTotal;

        /**
         * 今日成功通行数
         */
        private Long todaySuccess;

        /**
         * 今日失败通行数
         */
        private Long todayFailed;

        /**
         * 当前在线人数
         */
        private Long currentOnlinePersons;

        /**
         * 最近1小时通行数
         */
        private Long lastHourAccess;
    }

    /**
     * 容量监控
     */
    @Data
    public static class CapacityMonitor {
        /**
         * 容纳人数
         */
        private Integer capacity;

        /**
         * 当前人数
         */
        private Integer currentCount;

        /**
         * 使用率（百分比）
         */
        private Double usageRate;

        /**
         * 是否超限
         */
        private Boolean isOverLimit;

        /**
         * 预警级别
         * <p>
         * NORMAL-正常
         * WARNING-预警（85%）
         * CRITICAL-严重（95%）
         * </p>
         */
        private String alertLevel;
    }

    /**
     * 设备状态信息
     */
    @Data
    public static class DeviceStatusInfo {
        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 设备状态
         */
        private Integer deviceStatus;

        /**
         * 设备状态名称
         */
        private String deviceStatusName;

        /**
         * 最后在线时间
         */
        private LocalDateTime lastOnlineTime;

        /**
         * 响应时间（毫秒）
         */
        private Long responseTime;
    }

    /**
     * 最近通行记录
     */
    @Data
    public static class RecentAccessRecord {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 通行结果
         */
        private Integer accessResult;

        /**
         * 通行结果名称
         */
        private String accessResultName;

        /**
         * 通行时间
         */
        private LocalDateTime accessTime;
    }
}

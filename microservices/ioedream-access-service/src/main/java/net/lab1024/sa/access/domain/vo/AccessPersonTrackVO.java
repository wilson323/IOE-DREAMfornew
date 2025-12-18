package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 人员追踪视图对象
 * <p>
 * 用于实时监控模块显示人员轨迹信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessPersonTrackVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 当前区域ID
     */
    private Long currentAreaId;

    /**
     * 当前区域名称
     */
    private String currentAreaName;

    /**
     * 当前设备ID
     */
    private String currentDeviceId;

    /**
     * 当前设备名称
     */
    private String currentDeviceName;

    /**
     * 最后通行时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 轨迹点列表
     */
    private List<TrackingPoint> trackingPoints;

    /**
     * 是否异常
     * <p>
     * true - 检测到异常（如未授权区域、长时间逗留等）
     * false - 正常
     * </p>
     */
    private Boolean isAbnormal;

    /**
     * 异常描述（如果有）
     */
    private String abnormalDescription;

    /**
     * 轨迹点
     */
    @Data
    public static class TrackingPoint {

        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 区域ID
         */
        private Long areaId;

        /**
         * 区域名称
         */
        private String areaName;

        /**
         * 通行时间
         */
        private LocalDateTime accessTime;

        /**
         * 通行类型
         * <p>
         * IN - 进入
         * OUT - 离开
         * </p>
         */
        private String accessType;

        /**
         * 通行结果
         * <p>
         * SUCCESS - 成功
         * FAILED - 失败
         * </p>
         */
        private String accessResult;
    }
}

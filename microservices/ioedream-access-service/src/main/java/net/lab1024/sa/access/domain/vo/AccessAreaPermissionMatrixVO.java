package net.lab1024.sa.access.domain.vo;

import java.util.List;

import lombok.Data;

/**
 * 门禁区域权限矩阵视图对象
 * <p>
 * 用于区域空间管理模块显示人员-设备权限关系矩阵
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessAreaPermissionMatrixVO {

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 人员列表
     */
    private List<PersonInfo> persons;

    /**
     * 设备列表
     */
    private List<DeviceInfo> devices;

    /**
     * 权限矩阵
     * <p>
     * 格式：Map<userId, Map<deviceId, Boolean>>
     * 表示每个用户对每个设备的权限
     * </p>
     */
    private List<PermissionMatrixRow> permissionMatrix;

    /**
     * 人员信息
     */
    @Data
    public static class PersonInfo {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 用户工号
         */
        private String userNo;

        /**
         * 部门名称
         */
        private String departmentName;
    }

    /**
     * 设备信息
     */
    @Data
    public static class DeviceInfo {
        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 设备编码
         */
        private String deviceCode;

        /**
         * 设备状态
         * <p>
         * 1-在线 2-离线 3-故障 4-维护 5-停用
         * </p>
         */
        private Integer deviceStatus;
    }

    /**
     * 权限矩阵行
     */
    @Data
    public static class PermissionMatrixRow {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 设备权限列表
         * <p>
         * 格式：List<DevicePermission>
         * </p>
         */
        private List<DevicePermission> devicePermissions;
    }

    /**
     * 设备权限
     */
    @Data
    public static class DevicePermission {
        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 是否有权限
         */
        private Boolean hasPermission;

        /**
         * 权限类型
         * <p>
         * AUTO-自动分配
         * MANUAL-手动分配
         * </p>
         */
        private String permissionSource;

        /**
         * 权限生效时间
         */
        private java.time.LocalDateTime effectiveTime;
    }
}

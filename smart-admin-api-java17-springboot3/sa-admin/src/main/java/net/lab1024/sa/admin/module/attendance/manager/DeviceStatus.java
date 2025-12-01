package net.lab1024.sa.admin.module.attendance.manager;

/**
 * 设备状态常量类
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
public class DeviceStatus {

    /**
     * 在线状态
     */
    public static final String ONLINE = "ONLINE";

    /**
     * 离线状态
     */
    public static final String OFFLINE = "OFFLINE";

    /**
     * 故障状态
     */
    public static final String FAULT = "FAULT";

    /**
     * 维护状态
     */
    public static final String MAINTAIN = "MAINTAIN";

    /**
     * 未配置状态
     */
    public static final String UNCONFIGURED = "UNCONFIGURED";

    /**
     * 待激活状态
     */
    public static final String PENDING_ACTIVATION = "PENDING_ACTIVATION";

    /**
     * 已禁用状态
     */
    public static final String DISABLED = "DISABLED";

    /**
     * 获取状态描述
     */
    public static String getDescription(String status) {
        switch (status) {
            case ONLINE:
                return "在线";
            case OFFLINE:
                return "离线";
            case FAULT:
                return "故障";
            case MAINTAIN:
                return "维护中";
            case UNCONFIGURED:
                return "未配置";
            case PENDING_ACTIVATION:
                return "待激活";
            case DISABLED:
                return "已禁用";
            default:
                return "未知状态";
        }
    }

    /**
     * 判断是否为在线状态
     */
    public static boolean isOnline(String status) {
        return ONLINE.equals(status);
    }

    /**
     * 判断是否为离线状态
     */
    public static boolean isOffline(String status) {
        return OFFLINE.equals(status);
    }

    /**
     * 判断是否为故障状态
     */
    public static boolean isFault(String status) {
        return FAULT.equals(status);
    }

    /**
     * 判断是否为维护状态
     */
    public static boolean isMaintenance(String status) {
        return MAINTAIN.equals(status);
    }

    /**
     * 判断是否可用（非故障、非维护、非禁用）
     */
    public static boolean isAvailable(String status) {
        return !FAULT.equals(status) &&
               !MAINTAIN.equals(status) &&
               !DISABLED.equals(status);
    }
}
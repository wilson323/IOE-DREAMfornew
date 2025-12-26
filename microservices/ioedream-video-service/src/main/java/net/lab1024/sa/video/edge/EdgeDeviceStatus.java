package net.lab1024.sa.video.edge;

/**
 * 边缘设备状态枚举
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public enum EdgeDeviceStatus {
    OFFLINE("离线"),
    READY("就绪"),
    ERROR("异常");

    private final String description;

    EdgeDeviceStatus(String description) {
        this.description = description;
    }

    /**
     * 获取状态描述
     *
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
}


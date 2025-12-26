package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 位置信息
 * <p>
 * 封装位置相关信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfo {

    /**
     * 位置类型（GPS/WiFi/BaseStation）
     */
    private String locationType;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 地址
     */
    private String address;

    /**
     * 精度（米）
     */
    private Double accuracy;

    /**
     * WiFi SSID
     */
    private String wifiSsid;

    /**
     * 基站ID
     */
    private String baseStationId;
}

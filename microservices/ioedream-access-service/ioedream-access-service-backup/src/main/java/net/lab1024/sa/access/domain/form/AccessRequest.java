package net.lab1024.sa.access.domain.form;

import lombok.Data;

/**
 * 通行请求对象
 * <p>
 * 用于门禁权限策略验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
public class AccessRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 纬度（用于地理围栏验证）
     */
    private Double latitude;

    /**
     * 经度（用于地理围栏验证）
     */
    private Double longitude;

    /**
     * 通行类型（0-进入 1-离开）
     */
    private Integer accessType;

    /**
     * 生物识别数据
     */
    private String biometricData;

    /**
     * 卡片号
     */
    private String cardNo;
}

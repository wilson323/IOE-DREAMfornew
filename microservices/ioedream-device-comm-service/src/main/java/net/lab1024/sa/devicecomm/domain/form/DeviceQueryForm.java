package net.lab1024.sa.devicecomm.domain.form;

import lombok.Data;

/**
 * 设备分页查询Form
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class DeviceQueryForm {

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;

    /**
     * 设备编号（模糊匹配）
     */
    private String deviceCode;

    /**
     * 设备名称（模糊匹配）
     */
    private String deviceName;

    /**
     * 设备类型（精确匹配）
     */
    private String deviceType;
}


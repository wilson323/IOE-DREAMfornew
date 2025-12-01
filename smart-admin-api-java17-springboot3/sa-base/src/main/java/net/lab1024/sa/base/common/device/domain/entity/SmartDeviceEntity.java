package net.lab1024.sa.base.common.device.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 智能设备实体类
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device")
public class SmartDeviceEntity extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId(value = "device_id", type = IdType.AUTO)
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备类型
     */
    @TableField("device_type")
    private Integer deviceType;

    /**
     * 设备状态
     */
    @TableField("device_status")
    private Integer deviceStatus;

    /**
     * 设备位置
     */
    @TableField("device_location")
    private String deviceLocation;

    /**
     * 设备描述
     */
    @TableField("device_desc")
    private String deviceDesc;

    /**
     * 设备配置
     */
    @TableField("device_config")
    private String deviceConfig;

    /**
     * 安装时间
     */
    @TableField("install_time")
    private LocalDateTime installTime;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    // ✅ 以下审计字段已在BaseEntity中定义，无需重复
    // createTime, updateTime, createUserId, updateUserId, deletedFlag, version
}

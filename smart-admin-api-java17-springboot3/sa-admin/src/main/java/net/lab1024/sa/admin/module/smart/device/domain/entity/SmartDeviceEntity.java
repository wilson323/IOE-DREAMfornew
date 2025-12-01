package net.lab1024.sa.admin.module.smart.device.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 智能设备实体类
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device")
public class SmartDeviceEntity extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId(type = IdType.AUTO)
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型 (CAMERA-摄像头, ACCESS-门禁, CONSUME-消费机, ATTENDANCE-考勤机)
     */
    private String deviceType;

    /**
     * 设备状态 (ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTAIN-维护中)
     */
    private String deviceStatus;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 协议类型 (TCP, UDP, HTTP, HTTPS, MQTT)
     */
    private String protocolType;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 安装日期
     */
    private LocalDateTime installDate;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 配置信息(JSON格式)
     */
    private String configJson;

    /**
     * 是否启用 (0-禁用, 1-启用)
     */
    private Integer enabledFlag;

    /**
     * 备注
     */
    private String remark;
}
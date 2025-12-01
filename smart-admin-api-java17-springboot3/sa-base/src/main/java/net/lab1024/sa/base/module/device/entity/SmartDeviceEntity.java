package net.lab1024.sa.base.module.device.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 智能设备实体类
 * <p>
 * 基础设备实体，包含所有设备类型的通用字段
 * 业务模块可以通过扩展表添加特有字段
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device")
public class SmartDeviceEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID（业务标识）
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备编码（设备唯一标识）
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
     * ACCESS-门禁设备
     * ATTENDANCE-考勤设备
     * CONSUME-消费设备
     * VIDEO-视频设备
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备状态
     * ONLINE-在线
     * OFFLINE-离线
     * FAULT-故障
     * MAINTAIN-维护中
     */
    @TableField("device_status")
    private String deviceStatus;

    /**
     * 设备IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 设备端口
     */
    @TableField("port")
    private Integer port;

    /**
     * 协议类型
     * TCP-TCP协议
     * UDP-UDP协议
     * HTTP-HTTP协议
     * HTTPS-HTTPS协议
     * MQTT-MQTT协议
     * WEBSOCKET-WebSocket协议
     */
    @TableField("protocol_type")
    private String protocolType;

    /**
     * 设备位置描述
     */
    @TableField("location")
    private String location;

    /**
     * 设备描述
     */
    @TableField("description")
    private String description;

    /**
     * 制造商
     */
    @TableField("manufacturer")
    private String manufacturer;

    /**
     * 设备型号
     */
    @TableField("device_model")
    private String deviceModel;

    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 安装日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("install_date")
    private LocalDateTime installDate;

    /**
     * 最后在线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 分组ID（设备分组管理）
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 区域ID（设备所在区域）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 配置信息（JSON格式）
     * 存储设备的基础配置参数
     * 使用JSON类型处理器实现自动序列化/反序列化
     */
    @TableField(value = "config_json", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private java.util.Map<String, Object> configJson;

    /**
     * 扩展配置（JSON格式）
     * 存储业务模块特有的扩展配置
     * 使用JSON类型处理器实现自动序列化/反序列化
     */
    @TableField(value = "extension_config", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private java.util.Map<String, Object> extensionConfig;

    /**
     * 是否启用
     * 0-禁用，1-启用
     */
    @TableField("enabled_flag")
    private Integer enabledFlag;

    /**
     * 在线状态
     * 0-离线，1-在线
     */
    @TableField("online_status")
    private Integer onlineStatus;

    /**
     * 认证状态
     * 0-未认证，1-已认证，2-认证失败
     */
    @TableField("auth_status")
    private Integer authStatus;

    /**
     * 备注
     */
    private String remark;

    // ==================== 非数据库字段（用于业务查询） ====================

    /**
     * 设备类型名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String deviceTypeName;

    /**
     * 设备状态名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String deviceStatusName;

    /**
     * 协议类型名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String protocolTypeName;

    /**
     * 设备分组名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String groupName;

    /**
     * 区域名称（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String areaName;

    /**
     * 连接地址（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String connectionAddress;

    /**
     * 设备描述（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String deviceDescription;

    /**
     * 连接状态（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private String connectionStatus;

    /**
     * 是否超时（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private Boolean isTimeout;

    /**
     * 是否需要固件更新（非数据库字段，用于显示）
     */
    @TableField(exist = false)
    private Boolean needsFirmwareUpdate;

    // ==================== 业务方法 ====================

    /**
     * 获取最后心跳时间
     * 兼容性方法，确保心跳时间的访问
     *
     * @return 最后心跳时间，如果为null则返回最后在线时间
     */
    public LocalDateTime getLastHeartbeatTime() {
        return this.lastHeartbeatTime != null ? this.lastHeartbeatTime : this.lastOnlineTime;
    }

    /**
     * 设置最后心跳时间
     *
     * @param lastHeartbeatTime 最后心跳时间
     */
    public void setLastHeartbeatTime(LocalDateTime lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    /**
     * 判断是否为考勤设备
     *
     * @return true-考勤设备，false-非考勤设备
     */
    public boolean isAttendanceDevice() {
        return "ATTENDANCE".equals(this.deviceType) ||
               "FINGERPRINT".equals(this.deviceType) ||
               "FACE_RECOGNITION".equals(this.deviceType) ||
               "IC_CARD".equals(this.deviceType);
    }

    /**
     * 判断设备是否在线
     *
     * @return true-在线，false-离线
     */
    public boolean isOnline() {
        return Integer.valueOf(1).equals(this.onlineStatus) || "ONLINE".equals(this.deviceStatus);
    }

    /**
     * 判断设备是否离线
     *
     * @return true-离线，false-在线
     */
    public boolean isOffline() {
        return !isOnline();
    }

    /**
     * 判断设备是否有故障
     *
     * @return true-有故障，false-正常
     */
    public boolean hasFault() {
        return "FAULT".equals(this.deviceStatus) || "MAINTAIN".equals(this.deviceStatus);
    }

    /**
     * 获取在线状态
     *
     * @return 在线状态（1-在线，0-离线）
     */
    public Integer getOnlineStatus() {
        return this.onlineStatus;
    }

    /**
     * 设置在线状态
     *
     * @param onlineStatus 在线状态（1-在线，0-离线）
     */
    public void setOnlineStatus(Integer onlineStatus) {
        this.onlineStatus = onlineStatus;
        // 同步更新deviceStatus字段
        if (Integer.valueOf(1).equals(onlineStatus)) {
            this.deviceStatus = "ONLINE";
        } else {
            this.deviceStatus = "OFFLINE";
        }
    }

    /**
     * 获取认证状态
     *
     * @return 认证状态（0-未认证，1-已认证，2-认证失败）
     */
    public Integer getAuthStatus() {
        return this.authStatus;
    }

    /**
     * 设置认证状态
     *
     * @param authStatus 认证状态（0-未认证，1-已认证，2-认证失败）
     */
    public void setAuthStatus(Integer authStatus) {
        this.authStatus = authStatus;
    }
}
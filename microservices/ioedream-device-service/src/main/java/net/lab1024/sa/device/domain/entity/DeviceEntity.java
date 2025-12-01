package net.lab1024.sa.device.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 设备统一实体类
 * 基于现有SmartDeviceEntity重构，作为所有设备类型的基础抽象
 * 支持门禁设备、消费设备、考勤设备、视频监控设备等
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原SmartDeviceEntity重构)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device")
public class DeviceEntity extends BaseEntity {

    /**
     * 设备ID（主键，基于原deviceId）
     */
    @TableId(type = IdType.AUTO)
    private Long deviceId;

    /**
     * 设备编码（基于原deviceCode）
     */
    private String deviceCode;

    /**
     * 设备名称（基于原deviceName）
     */
    private String deviceName;

    /**
     * 设备类型（基于原deviceType扩展）
     * ACCESS-门禁设备, CONSUME-消费设备, ATTENDANCE-考勤设备, VIDEO-视频设备, MONITOR-监控设备
     */
    private String deviceType;

    /**
     * 设备状态（基于原deviceStatus）
     * ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTAIN-维护中, UNKNOWN-未知
     */
    private String deviceStatus;

    /**
     * 设备IP地址（基于原ipAddress）
     */
    private String ipAddress;

    /**
     * 设备端口（基于原port）
     */
    private Integer port;

    /**
     * 通信协议（基于原protocolType）
     * TCP, UDP, HTTP, HTTPS, MQTT, WEBSOCKET
     */
    private String protocolType;

    /**
     * 设备位置（基于原location）
     */
    private String location;

    /**
     * 设备描述（基于原description）
     */
    private String description;

    /**
     * 制造商（基于原manufacturer）
     */
    private String manufacturer;

    /**
     * 设备型号（基于原deviceModel）
     */
    private String deviceModel;

    /**
     * 固件版本（基于原firmwareVersion）
     */
    private String firmwareVersion;

    /**
     * 硬件版本（新增，基于AccessDeviceEntity）
     */
    private String hardwareVersion;

    /**
     * 设备序列号（基于AccessDeviceEntity）
     */
    private String serialNumber;

    /**
     * 安装日期（基于原installDate）
     */
    private LocalDateTime installDate;

    /**
     * 最后在线时间（基于原lastOnlineTime）
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 最后心跳时间（基于AccessDeviceEntity）
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 分组ID（基于原groupId）
     */
    private Long groupId;

    /**
     * 区域ID（基于AccessDeviceEntity.areaId）
     */
    private Long areaId;

    /**
     * 经度坐标（基于AccessDeviceEntity）
     */
    private BigDecimal longitude;

    /**
     * 纬度坐标（基于AccessDeviceEntity）
     */
    private BigDecimal latitude;

    /**
     * 设备照片路径（基于AccessDeviceEntity）
     */
    private String devicePhoto;

    /**
     * 配置信息（基于原configJson，JSON格式）
     */
    private String configJson;

    /**
     * 心跳间隔（基于AccessDeviceEntity）
     */
    private Integer heartbeatInterval;

    /**
     * 设备工作模式（基于AccessDeviceEntity.workMode）
     * 1-正常模式, 2-维护模式, 3-紧急模式, 4-锁闭模式
     */
    private Integer workMode;

    /**
     * 是否启用（基于原enabledFlag）
     */
    private Integer enabledFlag;

    /**
     * 维护人员（基于AccessDeviceEntity）
     */
    private String maintenancePerson;

    /**
     * 维护联系电话（基于AccessDeviceEntity）
     */
    private String maintenancePhone;

    /**
     * 上次维护时间（基于AccessDeviceEntity）
     */
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间（基于AccessDeviceEntity）
     */
    private LocalDateTime nextMaintenanceTime;

    /**
     * 备注（基于原remark）
     */
    private String remark;

    // 非数据库字段，用于展示和业务逻辑
    // 注意：deviceTypeName 和 deviceStatusName 通过 getter 方法动态计算，不需要字段存储

    /**
     * 区域名称（非数据库字段）
     */
    private transient String areaName;

    /**
     * 分组名称（非数据库字段）
     */
    private transient String groupName;

    /**
     * 维护人员姓名（非数据库字段）
     */
    private transient String maintenancePersonName;

    // 注意：workModeName 通过 getter 方法动态计算，不需要字段存储

    // 兼容性方法，保持与原有设备实体的兼容

    /**
     * 获取设备ID（兼容性方法，对应其他实体中的ID）
     */
    public Long getId() {
        return deviceId;
    }

    /**
     * 设置设备ID（兼容性方法）
     */
    public void setId(Long id) {
        this.deviceId = id;
    }

    // 业务方法，基于现有设备实体逻辑

    /**
     * 检查设备是否在线（基于AccessDeviceEntity.isOnline()）
     */
    public boolean isOnline() {
        return "ONLINE".equals(deviceStatus);
    }

    /**
     * 检查设备是否离线
     */
    public boolean isOffline() {
        return "OFFLINE".equals(deviceStatus);
    }

    /**
     * 检查设备是否故障
     */
    public boolean isFault() {
        return "FAULT".equals(deviceStatus);
    }

    /**
     * 检查设备是否启用（基于AccessDeviceEntity.isEnabled()）
     */
    public boolean isEnabled() {
        return enabledFlag != null && enabledFlag == 1;
    }

    /**
     * 检查设备是否禁用
     */
    public boolean isDisabled() {
        return enabledFlag != null && enabledFlag == 0;
    }

    /**
     * 检查设备心跳是否超时（基于AccessDeviceEntity.isHeartbeatTimeout()）
     */
    public boolean isHeartbeatTimeout() {
        if (lastHeartbeatTime == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        // 心跳超时阈值：心跳间隔的3倍时间，最少5分钟
        int timeoutMinutes = Math.max(heartbeatInterval != null ? heartbeatInterval * 3 : 5, 5);
        LocalDateTime timeoutThreshold = now.minusMinutes(timeoutMinutes);

        return lastHeartbeatTime.isBefore(timeoutThreshold);
    }

    /**
     * 检查设备是否需要维护（基于AccessDeviceEntity.needsMaintenance()）
     */
    public boolean needsMaintenance() {
        // 设备离线超过24小时需要维护
        if (!isOnline() && lastHeartbeatTime != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime maintenanceThreshold = now.minusHours(24);
            if (lastHeartbeatTime.isBefore(maintenanceThreshold)) {
                return true;
            }
        }

        // 设备状态为故障需要维护
        return isFault();
    }

    /**
     * 获取设备类型名称（基于现有实体逻辑）
     */
    public String getDeviceTypeName() {
        if (deviceType == null) {
            return "未知";
        }
        switch (deviceType) {
            case "ACCESS":
                return "门禁设备";
            case "CONSUME":
                return "消费设备";
            case "ATTENDANCE":
                return "考勤设备";
            case "VIDEO":
                return "视频设备";
            case "MONITOR":
                return "监控设备";
            default:
                return deviceType;
        }
    }

    /**
     * 获取设备状态名称（基于现有实体逻辑）
     */
    public String getDeviceStatusName() {
        if (deviceStatus == null) {
            return "未知";
        }
        switch (deviceStatus) {
            case "ONLINE":
                return "在线";
            case "OFFLINE":
                return "离线";
            case "FAULT":
                return "故障";
            case "MAINTAIN":
                return "维护中";
            case "UNKNOWN":
                return "未知";
            default:
                return deviceStatus;
        }
    }

    /**
     * 获取工作模式名称（基于AccessDeviceEntity.getWorkModeName()）
     */
    public String getWorkModeName() {
        if (workMode == null) {
            return "未知";
        }
        switch (workMode) {
            case 1:
                return "正常模式";
            case 2:
                return "维护模式";
            case 3:
                return "紧急模式";
            case 4:
                return "锁闭模式";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否支持远程控制（通用方法）
     */
    public boolean supportsRemoteControl() {
        return isOnline() && isEnabled();
    }

    /**
     * 获取设备完整地址信息
     */
    public String getFullLocation() {
        if (location != null && areaName != null) {
            return areaName + " - " + location;
        }
        return location != null ? location : (areaName != null ? areaName : "未设置");
    }

    /**
     * Builder模式支持
     */
    public static class Builder {
        private DeviceEntity device = new DeviceEntity();

        public Builder deviceId(Long deviceId) {
            device.setDeviceId(deviceId);
            return this;
        }

        public Builder deviceCode(String deviceCode) {
            device.setDeviceCode(deviceCode);
            return this;
        }

        public Builder deviceName(String deviceName) {
            device.setDeviceName(deviceName);
            return this;
        }

        public Builder deviceType(String deviceType) {
            device.setDeviceType(deviceType);
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            device.setIpAddress(ipAddress);
            return this;
        }

        public Builder port(Integer port) {
            device.setPort(port);
            return this;
        }

        public Builder protocolType(String protocolType) {
            device.setProtocolType(protocolType);
            return this;
        }

        public Builder manufacturer(String manufacturer) {
            device.setManufacturer(manufacturer);
            return this;
        }

        public Builder deviceModel(String deviceModel) {
            device.setDeviceModel(deviceModel);
            return this;
        }

        public Builder enabled() {
            device.setEnabledFlag(1);
            return this;
        }

        public Builder disabled() {
            device.setEnabledFlag(0);
            return this;
        }

        public DeviceEntity build() {
            return device;
        }
    }

    /**
     * 静态Builder方法
     */
    public static Builder builder() {
        return new Builder();
    }
}

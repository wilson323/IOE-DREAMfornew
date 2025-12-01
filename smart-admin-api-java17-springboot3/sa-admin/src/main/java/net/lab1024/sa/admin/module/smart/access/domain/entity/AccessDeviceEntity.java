package net.lab1024.sa.admin.module.smart.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.device.domain.entity.SmartDeviceEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门禁设备实体
 * <p>
 * 继承SmartDeviceEntity，扩展门禁专用功能
 * 严格遵循repowiki规范：
 * - 继承SmartDeviceEntity避免重复定义基础设备字段
 * - 使用Lombok简化代码
 * - 字段命名采用下划线分隔
 * - 完整的设备通信和状态管理字段
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_access_device")
public class AccessDeviceEntity extends SmartDeviceEntity {

    /**
     * 门禁设备ID（主键）
     */
    private Long accessDeviceId;

    /**
     * 设备ID（关联SmartDeviceEntity）
     */
    private Long deviceId;

    /**
     * 所属区域ID
     */
    private Long areaId;

    /**
     * 门禁设备类型
     * 1:门禁机 2:读卡器 3:指纹机 4:人脸识别机 5:密码键盘 6:三辊闸 7:翼闸 8:摆闸 9:其他
     */
    private Integer accessDeviceType;

    /**
     * 设备厂商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 通信协议（TCP/UDP/HTTP/HTTPS/MQTT）
     */
    private String protocol;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 通信密钥
     */
    private String commKey;

    /**
     * 设备方向
     * 0:单向进入 1:单向外出 2:双向
     */
    private Integer direction;

    /**
     * 开门方式
     * 1:刷卡 2:密码 3:指纹 4:人脸 5:二维码 6:组合方式
     */
    private Integer openMethod;

    /**
     * 开门延时时间（秒）
     */
    private Integer openDelay;

    /**
     * 有效开门时间（秒）
     */
    private Integer validTime;

    /**
     * 是否支持远程开门
     * 0:不支持 1:支持
     */
    private Integer remoteOpenEnabled;

    /**
     * 是否支持反潜回
     * 0:不支持 1:支持
     */
    private Integer antiPassbackEnabled;

    /**
     * 是否支持多人同时进入
     * 0:不支持 1:支持
     */
    private Integer multiPersonEnabled;

    /**
     * 是否支持门磁检测
     * 0:不支持 1:支持
     */
    private Integer doorSensorEnabled;

    /**
     * 门磁状态
     * 0:关闭 1:打开
     */
    private Integer doorSensorStatus;

    /**
     * 设备在线状态
     * 0:离线 1:在线
     */
    private Integer onlineStatus;

    /**
     * 最后通信时间
     */
    private LocalDateTime lastCommTime;

    /**
     * 设备工作模式
     * 1:正常模式 2:维护模式 3:紧急模式 4:锁闭模式
     */
    private Integer workMode;

    /**
     * 设备版本信息
     */
    private String firmwareVersion;

    /**
     * 硬件版本信息
     */
    private String hardwareVersion;

    /**
     * 心跳间隔（秒）
     */
    private Integer heartbeatInterval;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 是否启用
     * 0:禁用 1:启用
     */
    private Integer enabled;

    /**
     * 安装位置描述
     */
    private String installLocation;

    /**
     * 经度坐标
     */
    private BigDecimal longitude;

    /**
     * 纬度坐标
     */
    private BigDecimal latitude;

    /**
     * 设备照片路径
     */
    private String devicePhoto;

    /**
     * 维护人员
     */
    private String maintenancePerson;

    /**
     * 维护联系电话
     */
    private String maintenancePhone;

    /**
     * 上次维护时间
     */
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间
     */
    private LocalDateTime nextMaintenanceTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所在区域名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String areaName;

    /**
     * 门禁设备类型名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String accessDeviceTypeName;

    /**
     * 工作模式名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String workModeName;

    /**
     * 维护人员姓名（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String maintenancePersonName;

    // ==================== 业务方法 ====================

    /**
     * 检查设备是否在线
     */
    public boolean isOnline() {
        return onlineStatus != null && onlineStatus == 1;
    }

    /**
     * 检查设备是否启用
     */
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    /**
     * 获取设备类型名称
     */
    public String getAccessDeviceTypeName() {
        if (accessDeviceType == null) {
            return "未知";
        }
        switch (accessDeviceType) {
            case 1: return "门禁机";
            case 2: return "读卡器";
            case 3: return "指纹机";
            case 4: return "人脸识别机";
            case 5: return "密码键盘";
            case 6: return "三辊闸";
            case 7: return "翼闸";
            case 8: return "摆闸";
            case 9: return "其他";
            default: return "未知";
        }
    }

    /**
     * 获取工作模式名称
     */
    public String getWorkModeName() {
        if (workMode == null) {
            return "未知";
        }
        switch (workMode) {
            case 1: return "正常模式";
            case 2: return "维护模式";
            case 3: return "紧急模式";
            case 4: return "锁闭模式";
            default: return "未知";
        }
    }

    /**
     * 检查设备心跳是否超时
     */
    public boolean isHeartbeatTimeout() {
        if (lastHeartbeatTime == null) {
            return true; // 如果从未有心跳，认为超时
        }

        // 如果最后心跳时间超过5分钟，认为心跳超时
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime timeoutThreshold = now.minusMinutes(5);

        return lastHeartbeatTime.isBefore(timeoutThreshold);
    }

    /**
     * 检查设备是否需要维护
     */
    public boolean needsMaintenance() {
        // 如果设备离线超过24小时，需要维护
        if (!isOnline() && lastHeartbeatTime != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime maintenanceThreshold = now.minusHours(24);

            if (lastHeartbeatTime.isBefore(maintenanceThreshold)) {
                return true;
            }
        }

        // 如果设备状态为故障，需要维护
        return "FAULT".equals(this.getDeviceStatus());
    }
}

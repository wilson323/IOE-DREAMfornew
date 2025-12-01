package net.lab1024.sa.device.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物理设备实体
 *
 * @author IOE-DREAM Team
 */
@Data
@TableName("t_physical_device")
public class PhysicalDeviceEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID（业务主键）
     */
    private Long deviceId;

    /**
     * 设备编号（唯一标识）
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型：ACCESS-门禁设备，VIDEO-视频设备，SENSOR-传感器设备，OTHER-其他设备
     */
    private String deviceType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 设备状态：OFFLINE-离线，ONLINE-在线，MAINTENANCE-维护中，FAULT-故障，DISABLED-禁用
     */
    private String deviceStatus;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 安装区域ID
     */
    private Long areaId;

    /**
     * 设备描述
     */
    private String description;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 心跳数据（JSON格式）
     */
    private String heartbeatData;

    /**
     * 连接状态：DISCONNECTED-未连接，CONNECTED-已连接，CONNECTING-连接中，ERROR-连接错误
     */
    private String connectionStatus;

    /**
     * 协议类型：TCP- TCP协议，UDP- UDP协议，HTTP- HTTP协议，MQTT- MQTT协议，MODBUS- Modbus协议
     */
    private String protocolType;

    /**
     * 配置参数（JSON格式）
     */
    private String configParameters;

    /**
     * 设备版本
     */
    private String deviceVersion;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 安装时间
     */
    private LocalDateTime installTime;

    /**
     * 最后维护时间
     */
    private LocalDateTime lastMaintenanceTime;

    /**
     * 维护周期（天）
     */
    private Integer maintenanceCycle;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 软删除标记：0-正常，1-已删除
     */
    private Integer deletedFlag;

    // 以下为关联数据字段，不存储在数据库中
    /**
     * 心跳数据对象（运行时使用）
     */
    private transient java.util.Map<String, Object> heartbeatDataMap;

    /**
     * 配置参数对象（运行时使用）
     */
    private transient java.util.Map<String, Object> configParametersMap;
}
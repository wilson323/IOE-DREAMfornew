package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 设备实体类
 * <p>
 * 统一设备管理实体，支持多种设备类型和跨模块设备管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {

    /**
     * 设备ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String deviceId;

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
     * 设备类型：CAMERA-摄像头 ACCESS-门禁 CONSUME-消费机 ATTENDANCE-考勤机 BIOMETRIC-生物识别
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备子类型
     */
    @TableField("device_sub_type")
    private String deviceSubType;

    /**
     * 业务模块：access-门禁 attendance-考勤 consume-消费 visitor-访客 video-视频
     */
    @TableField("business_module")
    private String businessModule;

    /**
     * 品牌厂商
     */
    @TableField("brand")
    private String brand;

    /**
     * 设备型号
     */
    @TableField("model")
    private String model;

    /**
     * 设备序列号
     */
    @TableField("serial_number")
    private String serialNumber;

    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 设备状态：1-在线 2-离线 3-故障 4-维护 5-停用
     */
    @TableField("device_status")
    private Integer deviceStatus;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 安装位置
     */
    @TableField("location")
    private String location;

    /**
     * 安装位置描述
     */
    @TableField("location_description")
    private String locationDescription;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 端口号
     */
    @TableField("port")
    private Integer port;

    /**
     * MAC地址
     */
    @TableField("mac_address")
    private String macAddress;

    /**
     * 网络类型：1-有线 2-WiFi 3-4G 4-5G
     */
    @TableField("network_type")
    private Integer networkType;

    /**
     * 供电方式：1-市电 2-POE 3-电池 4-太阳能
     */
    @TableField("power_supply")
    private Integer powerSupply;

    /**
     * 负责人ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 负责人姓名
     */
    @TableField("manager_name")
    private String managerName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 采购时间
     */
    @TableField("purchase_time")
    private LocalDateTime purchaseTime;

    /**
     * 安装时间
     */
    @TableField("install_time")
    private LocalDateTime installTime;

    /**
     * 保修期（月）
     */
    @TableField("warranty_period")
    private Integer warrantyPeriod;

    /**
     * 维护周期（天）
     */
    @TableField("maintenance_cycle")
    private Integer maintenanceCycle;

    /**
     * 上次维护时间
     */
    @TableField("last_maintenance_time")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间
     */
    @TableField("next_maintenance_time")
    private LocalDateTime nextMaintenanceTime;

    /**
     * 设备价值
     */
    @TableField("device_value")
    private java.math.BigDecimal deviceValue;

    /**
     * 资产编号
     */
    @TableField("asset_number")
    private String assetNumber;

    /**
     * 设备供应商
     */
    @TableField("supplier")
    private String supplier;

    /**
     * 供应商联系方式
     */
    @TableField("supplier_contact")
    private String supplierContact;

    /**
     * 是否启用：0-禁用 1-启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 最后离线时间
     */
    @TableField("last_offline_time")
    private LocalDateTime lastOfflineTime;

    /**
     * 在线时长（小时）
     */
    @TableField("online_duration")
    private Long onlineDuration;

    /**
     * 设备配置（JSON格式）
     */
    @TableField("device_config")
    private String deviceConfig;

    /**
     * 设备参数（JSON格式）
     */
    @TableField("device_parameters")
    private String deviceParameters;

    /**
     * 设备描述
     */
    @TableField("description")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
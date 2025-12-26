package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 统一设备实体
 * <p>
 * 对应数据库表: t_common_device
 * 支持所有设备类型：门禁、考勤、消费、视频、访客、生物识别等
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范：
 * - 统一设备实体，禁止重复实现
 * - 使用扩展字段存储业务特定属性
 * - 支持多设备类型统一管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {

    /**
     * 设备ID（主键）
     */
    @TableId(value = "device_id", type = IdType.ASSIGN_ID)
    private Long deviceId;

    /**
     * 设备编码（唯一标识）
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
     * 1-门禁设备(ACCESS) 2-考勤设备(ATTENDANCE) 3-消费设备(CONSUME) 
     * 4-视频设备(CAMERA) 5-访客设备(VISITOR) 6-生物识别设备(BIOMETRIC)
     * 7-对讲机(INTERCOM) 8-报警器(ALARM) 9-传感器(SENSOR)
     */
    @TableField("device_type")
    private Integer deviceType;

    /**
     * 设备子类型（设备类型的具体分类）
     */
    @TableField("device_sub_type")
    private Integer deviceSubType;

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
     * 序列号
     */
    @TableField("serial_number")
    private String serialNumber;

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
     * 用户名（用于设备连接认证）
     */
    @TableField("username")
    private String username;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 区域ID（设备部署的区域）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 设备状态
     * 1-在线 2-离线 3-故障 4-维护中 5-停用
     */
    @TableField("device_status")
    private Integer deviceStatus;

    /**
     * 是否启用
     * 1-启用 0-禁用
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
     * 设备版本
     */
    @TableField("device_version")
    private String deviceVersion;

    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 扩展属性（JSON格式，存储业务特定字段）
     * 例如：门禁设备的开门时间、消费设备的支付方式等
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 经度（用于地图定位）
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 纬度（用于地图定位）
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 获取设备ID（兼容BaseEntity的getId方法）
     *
     * @return 设备ID
     */
    public Long getId() {
        return deviceId;
    }

    /**
     * 设置设备ID（兼容BaseEntity的setId方法）
     *
     * @param id 设备ID
     */
    public void setId(Long id) {
        this.deviceId = id;
    }
}


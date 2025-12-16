package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 设备实体类
 * <p>
 * 统一设备实体，用于所有业务模块的设备管理
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * - 使用extendedAttributes存储扩展属性
 * </p>
 * <p>
 * 业务场景：
 * - 门禁设备管理（ACCESS）
 * - 考勤设备管理（ATTENDANCE）
 * - 消费设备管理（CONSUME）
 * - 视频设备管理（CAMERA）
 * - 生物识别设备管理（BIOMETRIC）
 * </p>
 * <p>
 * 数据库表：t_common_device
 * 支持的设备类型：
 * - CAMERA - 摄像头
 * - ACCESS - 门禁设备
 * - CONSUME - 消费机
 * - ATTENDANCE - 考勤机
 * - BIOMETRIC - 生物识别设备
 * - INTERCOM - 对讲机
 * - ALARM - 报警器
 * - SENSOR - 传感器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ObjectMapper实例（线程安全，可复用）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * 避免每次调用getter方法时创建新实例，提升性能
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 设备ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列device_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "device_id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备编号
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 设备类型
     * <p>
     * 枚举值：
     * - CAMERA - 摄像头
     * - ACCESS - 门禁设备
     * - CONSUME - 消费机
     * - ATTENDANCE - 考勤机
     * - BIOMETRIC - 生物识别设备
     * - INTERCOM - 对讲机
     * - ALARM - 报警器
     * - SENSOR - 传感器
     * </p>
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 区域ID（外键）
     */
    @TableField("area_id")
    private Long areaId;

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
     * 设备状态
     * <p>
     * 枚举值：
     * - ONLINE - 在线
     * - OFFLINE - 离线
     * - MAINTAIN - 维护中
     * </p>
     */
    @TableField("device_status")
    private String deviceStatus;

    /**
     * 启用标志
     * <p>
     * 0-禁用
     * 1-启用
     * </p>
     */
    @TableField("enabled_flag")
    private Integer enabledFlag;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 存储业务特定字段，例如：
     * {
     *   "manufacturer": "中控智慧",
     *   "protocolType": "TCP",
     *   "accessDeviceType": "DOOR",
     *   "openMethod": "CARD_FACE"
     * }
     * </p>
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 设备制造商（从扩展属性中获取）
     *
     * @return 设备制造商，如果不存在则返回"UNKNOWN"
     */
    public String getManufacturer() {
        if (extendedAttributes == null || extendedAttributes.trim().isEmpty()) {
            return "UNKNOWN";
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(extendedAttributes, new TypeReference<Map<String, Object>>() {});
            Object manufacturer = map.get("manufacturer");
            return manufacturer != null ? manufacturer.toString() : "UNKNOWN";
        } catch (Exception e) {
            log.debug("[设备实体] 解析制造商信息失败, deviceId={}", getId(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 协议类型（从扩展属性中获取）
     *
     * @return 协议类型，如果不存在则返回"HTTP"
     */
    public String getProtocolType() {
        if (extendedAttributes == null || extendedAttributes.trim().isEmpty()) {
            return "HTTP";
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(extendedAttributes, new TypeReference<Map<String, Object>>() {});
            Object protocolType = map.get("protocolType");
            return protocolType != null ? protocolType.toString() : "HTTP";
        } catch (Exception e) {
            log.debug("[设备实体] 解析协议类型失败, deviceId={}", getId(), e);
            return "HTTP";
        }
    }

    /**
     * 门禁设备类型（从扩展属性中获取）
     *
     * @return 门禁设备类型，如果不存在则返回null
     */
    public String getAccessDeviceType() {
        if (extendedAttributes == null || extendedAttributes.trim().isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(extendedAttributes, new TypeReference<Map<String, Object>>() {});
            Object accessDeviceType = map.get("accessDeviceType");
            return accessDeviceType != null ? accessDeviceType.toString() : null;
        } catch (Exception e) {
            log.debug("[设备实体] 解析门禁设备类型失败, deviceId={}", getId(), e);
            return null;
        }
    }

    /**
     * 开门方式（从扩展属性中获取）
     *
     * @return 开门方式，如果不存在则返回null
     */
    public String getOpenMethod() {
        if (extendedAttributes == null || extendedAttributes.trim().isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(extendedAttributes, new TypeReference<Map<String, Object>>() {});
            Object openMethod = map.get("openMethod");
            return openMethod != null ? openMethod.toString() : null;
        } catch (Exception e) {
            log.debug("[设备实体] 解析开门方式失败, deviceId={}", getId(), e);
            return null;
        }
    }

    /**
     * 扩展配置（从扩展属性中获取）
     *
     * @return 扩展配置JSON字符串
     */
    public String getExtensionConfig() {
        return extendedAttributes;
    }

    /**
     * 获取设备状态（别名方法）
     *
     * @return 设备状态
     */
    public String getStatus() {
        return deviceStatus;
    }

    /**
     * 设置设备状态（别名方法）
     *
     * @param status 设备状态
     */
    public void setStatus(String status) {
        this.deviceStatus = status;
    }
}

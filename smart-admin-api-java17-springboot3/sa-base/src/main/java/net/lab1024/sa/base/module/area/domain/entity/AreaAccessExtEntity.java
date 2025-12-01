package net.lab1024.sa.base.module.area.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 区域门禁扩展实体类
 * 存储门禁模块特有的区域配置信息
 *
 * 命名规范：符合{BaseDomain}{Module}ExtEntity标准模式
 * 演进记录：从AccessAreaExtEntity重构而来，保持向后兼容
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_area_ext")
public class AreaAccessExtEntity extends BaseEntity {

    /**
     * 扩展ID
     */
    @TableId
    private Long extId;

    /**
     * 基础区域ID
     * 关联到t_area表的area_id
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 门禁级别
     * 1:普通 2:重要 3:核心
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 门禁模式
     * 多种验证方式组合，如：卡,指纹,人脸,二维码
     */
    @TableField("access_mode")
    private String accessMode;

    /**
     * 关联设备数量
     * 该区域关联的门禁设备数量
     */
    @TableField("device_count")
    private Integer deviceCount;

    /**
     * 是否需要安保人员
     * 0:否 1:是
     */
    @TableField("guard_required")
    private Boolean guardRequired;

    /**
     * 时间限制配置
     * JSON格式存储，包含工作日、周末等不同时间段的管理规则
     */
    @TableField("time_restrictions")
    private String timeRestrictions;

    /**
     * 是否允许访客
     * 0:否 1:是
     */
    @TableField("visitor_allowed")
    private Boolean visitorAllowed;

    /**
     * 是否为紧急通道
     * 0:否 1:是
     */
    @TableField("emergency_access")
    private Boolean emergencyAccess;

    /**
     * 是否启用监控
     * 0:否 1:是
     */
    @TableField("monitoring_enabled")
    private Boolean monitoringEnabled;

    /**
     * 告警配置
     * JSON格式存储，包含各种异常情况的告警规则
     */
    @TableField("alert_config")
    private String alertConfig;

    // 构造函数
    public AreaAccessExtEntity() {
        super();
    }

    public AreaAccessExtEntity(Long areaId) {
        super();
        this.areaId = areaId;
        this.accessLevel = 1; // 默认普通级别
        this.deviceCount = 0; // 默认无设备
        this.guardRequired = false; // 默认不需要安保
        this.visitorAllowed = true; // 默认允许访客
        this.emergencyAccess = false; // 默认非紧急通道
        this.monitoringEnabled = true; // 默认启用监控
    }

    /**
     * 设置默认的门禁模式
     * 根据门禁级别自动设置合适的验证方式
     */
    public void setDefaultAccessMode() {
        if (this.accessLevel == null) {
            this.accessLevel = 1;
        }

        switch (this.accessLevel) {
            case 1: // 普通
                this.accessMode = "卡";
                break;
            case 2: // 重要
                this.accessMode = "卡,指纹";
                break;
            case 3: // 核心
                this.accessMode = "卡,指纹,人脸,二维码";
                break;
            default:
                this.accessMode = "卡";
                break;
        }
    }

    /**
     * 检查是否包含指定的验证方式
     */
    public boolean hasAccessMode(String mode) {
        if (this.accessMode == null || mode == null) {
            return false;
        }
        return this.accessMode.contains(mode);
    }

    /**
     * 添加验证方式
     */
    public void addAccessMode(String mode) {
        if (this.accessMode == null) {
            this.accessMode = mode;
        } else if (!hasAccessMode(mode)) {
            this.accessMode += "," + mode;
        }
    }

    /**
     * 移除验证方式
     */
    public void removeAccessMode(String mode) {
        if (this.accessMode != null && hasAccessMode(mode)) {
            this.accessMode = this.accessMode.replace("," + mode, "")
                                          .replace(mode + ",", "")
                                          .replace(mode, "");
        }
    }

    /**
     * 获取所有验证方式的数组
     */
    public String[] getAccessModes() {
        if (this.accessMode == null || this.accessMode.trim().isEmpty()) {
            return new String[0];
        }
        return this.accessMode.split(",");
    }

    /**
     * 设备数量增加
     */
    public void incrementDeviceCount() {
        this.deviceCount = (this.deviceCount == null ? 0 : this.deviceCount) + 1;
    }

    /**
     * 设备数量减少
     */
    public void decrementDeviceCount() {
        this.deviceCount = Math.max(0, (this.deviceCount == null ? 0 : this.deviceCount) - 1);
    }

    /**
     * 设置设备数量
     */
    public void setDeviceCount(Integer deviceCount) {
        this.deviceCount = Math.max(0, deviceCount == null ? 0 : deviceCount);
    }

    /**
     * 判断是否为高级别门禁（重要或核心）
     */
    public boolean isHighSecurity() {
        return this.accessLevel != null && this.accessLevel >= 2;
    }

    /**
     * 判断是否为核心门禁
     */
    public boolean isCoreSecurity() {
        return this.accessLevel != null && this.accessLevel >= 3;
    }

    /**
     * 获取门禁级别名称
     */
    public String getAccessLevelName() {
        if (this.accessLevel == null) {
            return "未知";
        }
        switch (this.accessLevel) {
            case 1:
                return "普通";
            case 2:
                return "重要";
            case 3:
                return "核心";
            default:
                return "未知";
        }
    }

    /**
     * 获取安全级别颜色标识（用于前端显示）
     */
    public String getSecurityLevelColor() {
        if (this.accessLevel == null) {
            return "#666666"; // 灰色
        }
        switch (this.accessLevel) {
            case 1:
                return "#52c41a"; // 绿色
            case 2:
                return "#faad14"; // 橙色
            case 3:
                return "#f5222d"; // 红色
            default:
                return "#666666"; // 灰色
        }
    }

    @Override
    public String toString() {
        return "AreaAccessExtEntity{" +
                "extId" + extId +
                ", areaId" + areaId +
                ", accessLevel" + accessLevel +
                ", accessLevelName='" + getAccessLevelName() + '\'' +
                ", accessMode='" + accessMode + '\'' +
                ", deviceCount" + deviceCount +
                ", guardRequired" + guardRequired +
                ", visitorAllowed" + visitorAllowed +
                ", emergencyAccess" + emergencyAccess +
                ", monitoringEnabled" + monitoringEnabled +
                '}';
    }
}
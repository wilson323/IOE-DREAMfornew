package net.lab1024.sa.admin.module.access.domain.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;

/**
 * 门禁区域实体（重构版）
 * <p>
 * 严格遵循扩展表架构：AreaEntity + AreaAccessExtEntity
 * 消除重复字段定义，基于AreaEntity基础区域实体通过扩展表机制增加门禁特有功能
 *
 * 扩展表架构：t_area (基础区域表) + t_access_area_ext (门禁区域扩展表)
 * 所有基础字段（areaId, areaCode, areaName等）由AreaEntity提供，此处不再重复定义
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 * @updated 2025-11-25 重构为严格扩展表架构，消除重复字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area") // 使用基础区域表，扩展表通过JOIN查询获取
public class AccessAreaEntity extends AreaEntity {

    // ==================== 门禁扩展信息（来自t_access_area_ext扩展表） ====================

    /**
     * 是否启用门禁（0:禁用 1:启用）
     */
    @TableField("access_enabled")
    private Integer accessEnabled;

    /**
     * 门禁级别（1:普通 2:重要 3:核心）
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 门禁模式（多种验证方式组合，如：卡、密码、指纹、人脸等）
     */
    @TableField("access_mode")
    private String accessMode;

    /**
     * 是否需要特殊授权（0:不需要 1:需要）
     */
    @TableField("special_auth_required")
    private Integer specialAuthRequired;

    /**
     * 有效时间段开始（HH:mm格式）
     */
    @TableField("valid_time_start")
    private String validTimeStart;

    /**
     * 有效时间段结束（HH:mm格式）
     */
    @TableField("valid_time_end")
    private String validTimeEnd;

    /**
     * 有效星期（逗号分隔，1-7代表周一到周日）
     */
    @TableField("valid_weekdays")
    private String validWeekdays;

    /**
     * 关联设备数量
     */
    @TableField("device_count")
    private Integer deviceCount;

    /**
     * 是否需要安保人员
     */
    @TableField("guard_required")
    private Boolean guardRequired;

    /**
     * 时间限制配置（JSON格式，详细的时间限制规则）
     */
    @TableField("time_restrictions")
    private String timeRestrictions;

    /**
     * 是否允许访客
     */
    @TableField("visitor_allowed")
    private Boolean visitorAllowed;

    /**
     * 是否为紧急通道
     */
    @TableField("emergency_access")
    private Boolean emergencyAccess;

    /**
     * 是否启用监控
     */
    @TableField("monitoring_enabled")
    private Boolean monitoringEnabled;

    /**
     * 告警配置（JSON格式）
     */
    @TableField("alert_config")
    private String alertConfig;

    // ==================== 业务关联字段（门禁业务特定） ====================

    /**
     * 所在建筑ID（业务关联字段）
     */
    @TableField(exist = false)
    private Long buildingId;

    /**
     * 所在楼层ID（业务关联字段）
     */
    @TableField(exist = false)
    private Long floorId;

    // ==================== 非数据库字段（用于展示和业务逻辑） ====================

    /**
     * 子区域列表（非数据库字段，用于树形结构展示）
     */
    @TableField(exist = false)
    private List<AccessAreaEntity> accessChildren;

    /**
     * 子区域数量（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Integer childrenCount;

    /**
     * 是否有子区域（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Boolean hasChildren;

    /**
     * 父级区域名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 区域类型名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String areaTypeName;

    /**
     * 门禁级别名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String accessLevelName;

    /**
     * 门禁模式描述（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String accessModeDesc;

    /**
     * 关联设备列表（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private List<Object> devices;
}

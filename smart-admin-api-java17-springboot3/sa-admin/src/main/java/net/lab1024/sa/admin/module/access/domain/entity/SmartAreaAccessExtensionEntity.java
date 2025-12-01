package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 区域门禁扩展实体类
 * <p>
 * 基于AreaEntity的扩展表机制，增加门禁区域特有字段
 * 严格遵循扩展表架构设计，避免重复建设，基于现有区域管理增强和完善
 *
 * 扩展表架构：t_area (基础区域表) + t_smart_area_access_extension (区域门禁扩展表)
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_area_access_extension")
public class SmartAreaAccessExtensionEntity extends BaseEntity {

    /**
     * 区域ID（关联AreaEntity.areaId）
     */
    @NotNull(message = "区域ID不能为空")
    @TableField("area_id")
    private Long areaId;

    /**
     * 访问策略 (OPEN-开放, RESTRICTED-受限, PRIVATE-私有)
     */
    @TableField("access_policy")
    private String accessPolicy;

    /**
     * 自动权限分配开关 (0-关闭, 1-开启)
     */
    @TableField("auto_assign_permission")
    private Integer autoAssignPermission;

    /**
     * 最大并发人数
     */
    @TableField("max_concurrent_persons")
    private Integer maxConcurrentPersons;

    /**
     * 容量告警阈值 (0.85表示85%)
     */
    @TableField("alert_capacity_threshold")
    private Double alertCapacityThreshold;

    /**
     * 紧急访问启用 (0-禁用, 1-启用)
     */
    @TableField("emergency_access_enabled")
    private Integer emergencyAccessEnabled;

    /**
     * 视频监控启用 (0-禁用, 1-启用)
     */
    @TableField("video_surveillance_enabled")
    private Integer videoSurveillanceEnabled;

    /**
     * 区域门禁配置(JSON格式)
     */
    @TableField("access_config")
    private String accessConfig;

    /**
     * 通行时间限制(JSON格式)
     */
    @TableField("access_time_limits")
    private String accessTimeLimits;

    /**
     * 通行验证方式(JSON格式)
     */
    @TableField("verification_methods")
    private String verificationMethods;

    /**
     * 访问权限级别 (1-10，数字越大权限要求越高)
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 特殊授权要求 (0-不需要, 1-需要)
     */
    @TableField("special_auth_required")
    private Integer specialAuthRequired;

    /**
     * 有效时间段开始(HH:mm格式)
     */
    @TableField("valid_time_start")
    private String validTimeStart;

    /**
     * 有效时间段结束(HH:mm格式)
     */
    @TableField("valid_time_end")
    private String validTimeEnd;

    /**
     * 有效星期(逗号分隔，1-7代表周一到周日)
     */
    @TableField("valid_weekdays")
    private String validWeekdays;

    /**
     * 区域安全等级 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键)
     */
    @TableField("security_level")
    private String securityLevel;

    /**
     * 需要验证的生物特征类型(JSON格式)
     */
    @TableField("required_biometric_types")
    private String requiredBiometricTypes;

    /**
     * 访客访问控制 (0-不允许, 1-需要预约, 2-直接访问)
     */
    @TableField("visitor_access_control")
    private Integer visitorAccessControl;

    /**
     * 访客访问时间限制(分钟)
     */
    @TableField("visitor_access_time_limit")
    private Integer visitorAccessTimeLimit;

    /**
     * 区域容量监控启用 (0-禁用, 1-启用)
     */
    @TableField("capacity_monitoring_enabled")
    private Integer capacityMonitoringEnabled;

    /**
     * 实时追踪启用 (0-禁用, 1-启用)
     */
    @TableField("real_time_tracking_enabled")
    private Integer realTimeTrackingEnabled;

    /**
     * 紧急疏散启用 (0-禁用, 1-启用)
     */
    @TableField("emergency_evacuation_enabled")
    private Integer emergencyEvacuationEnabled;

    /**
     * 多人通行控制 (0-禁用, 1-启用)
     */
    @TableField("multi_person_control_enabled")
    private Integer multiPersonControlEnabled;

    /**
     * 反潜规则启用 (0-禁用, 1-启用)
     */
    @TableField("anti_passback_enabled")
    private Integer antiPassbackEnabled;

    /**
     * 区域门禁策略模板
     */
    @TableField("access_policy_template")
    private String accessPolicyTemplate;

    /**
     * 绑定的设备ID列表(JSON格式)
     */
    @TableField("bound_device_ids")
    private String boundDeviceIds;

    /**
     * 区域访问日志级别 (INFO-信息, WARNING-警告, ERROR-错误)
     */
    @TableField("access_log_level")
    private String accessLogLevel;

    /**
     * 最后访问统计时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_access_stats_time")
    private LocalDateTime lastAccessStatsTime;

    /**
     * 今日访问次数
     */
    @TableField("today_access_count")
    private Integer todayAccessCount;

    /**
     * 本月访问次数
     */
    @TableField("month_access_count")
    private Integer monthAccessCount;

    /**
     * 异常访问次数
     */
    @TableField("abnormal_access_count")
    private Integer abnormalAccessCount;

    /**
     * 区域状态 (ACTIVE-活跃, INACTIVE-非活跃, MAINTENANCE-维护)
     */
    @TableField("area_status")
    private String areaStatus;

    /**
     * 扩展配置1
     */
    @TableField("ext_config1")
    private String extConfig1;

    /**
     * 扩展配置2
     */
    @TableField("ext_config2")
    private String extConfig2;

    /**
     * 扩展配置3
     */
    @TableField("ext_config3")
    private String extConfig3;

    // Getter和Setter方法
    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(String accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public Integer getAutoAssignPermission() {
        return autoAssignPermission;
    }

    public void setAutoAssignPermission(Integer autoAssignPermission) {
        this.autoAssignPermission = autoAssignPermission;
    }
}
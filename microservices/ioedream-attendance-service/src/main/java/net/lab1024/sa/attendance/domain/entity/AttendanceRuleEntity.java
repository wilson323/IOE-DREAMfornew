package net.lab1024.sa.attendance.domain.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤规则实体
 *
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用jakarta包，避免javax包
 * - 使用Lombok简化代码
 * - 字段命名规范：下划线分隔
 * - JSON字段支持灵活配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_attendance_rule")
public class AttendanceRuleEntity extends BaseEntity {

    /**
     * 规则ID
     */
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 员工ID(个人规则时使用)
     */
    @TableField("employee_id")
    private Long employeeId;

    /**
     * 规则类型
     * GLOBAL-全局, DEPARTMENT-部门, INDIVIDUAL-个人
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 适用员工类型
     * FULL_TIME-全职, PART_TIME-兼职, INTERN-实习
     */
    @TableField("employee_type")
    private String employeeType;

    /**
     * 工作排班配置(JSON格式)
     * 格式: {
     * "workDays": [1,2,3,4,5],
     * "workStartTime": "09:00",
     * "workEndTime": "18:00",
     * "breakStartTime": "12:00",
     * "breakEndTime": "13:00"
     * }
     */
    @TableField("work_schedule")
    private String workSchedule;

    /**
     * 迟到容忍分钟数
     */
    @TableField("late_tolerance")
    private Integer lateTolerance;

    /**
     * 早退容忍分钟数
     */
    @TableField("early_tolerance")
    private Integer earlyTolerance;

    /**
     * 加班规则配置(JSON格式)
     * 格式: {
     * "enabled": true,
     * "minOvertimeMinutes": 30,
     * "doublePayHours": 2
     * }
     */
    @TableField("overtime_rules")
    private String overtimeRules;

    /**
     * 节假日规则配置(JSON格式)
     * 格式: {
     * "holidays": ["2024-01-01", "2024-02-10"],
     * "overtimeRate": 2.0
     * }
     */
    @TableField("holiday_rules")
    private String holidayRules;

    /**
     * 是否启用GPS验证
     * 0-否, 1-是
     */
    @TableField("gps_validation")
    private Integer gpsValidation;

    /**
     * GPS位置配置(JSON格式)
     * 格式: [{
     * "name": "主办公区",
     * "latitude": 39.9042,
     * "longitude": 116.4074,
     * "radius": 100
     * }]
     */
    @TableField("gps_locations")
    private String gpsLocations;

    /**
     * GPS验证范围(米)
     */
    @TableField("gps_range")
    private Integer gpsRange;

    /**
     * GPS纬度
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * GPS经度
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 是否需要拍照打卡
     * 0-否, 1-是
     */
    @TableField("photo_required")
    private Integer photoRequired;

    /**
     * 是否启用人脸识别
     * 0-否, 1-是
     */
    @TableField("face_recognition")
    private Integer faceRecognition;

    /**
     * GPS验证是否启用
     * 0-否, 1-是
     */
    @TableField("gps_validation_enabled")
    private Integer gpsValidationEnabled;

    public boolean isGpsValidationEnabled() {
        return gpsValidationEnabled != null && gpsValidationEnabled == 1;
    }

    /**
     * 设备限制配置(JSON格式)
     * 格式: {
     * "allowedDevices": ["device1", "device2"],
     * "maxDeviceCount": 3
     * }
     */
    @TableField("device_restrictions")
    private String deviceRestrictions;

    /**
     * 是否自动审批异常
     * 0-否, 1-是
     */
    @TableField("auto_approval")
    private Integer autoApproval;

    /**
     * 通知设置(JSON格式)
     * 格式: {
     * "late": true,
     * "absent": true,
     * "overtime": true
     * }
     */
    @TableField("notification_settings")
    private String notificationSettings;

    /**
     * 规则状态
     * ACTIVE-激活, INACTIVE-停用, DRAFT-草稿
     */
    @TableField("status")
    private String status;

    /**
     * 是否启用
     * 0-否, 1-是
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 是否要求位置验证
     * 0-否, 1-是
     */
    @TableField("location_required")
    private Boolean locationRequired;

    /**
     * 是否要求设备验证
     * 0-否, 1-是
     */
    @TableField("device_required")
    private Boolean deviceRequired;

    /**
     * 最大距离(米)
     */
    @TableField("max_distance")
    private Double maxDistance;

    /**
     * 迟到宽限分钟数
     */
    @TableField("late_grace_minutes")
    private Integer lateGraceMinutes;

    /**
     * 早退宽限分钟数
     */
    @TableField("early_leave_grace_minutes")
    private Integer earlyLeaveGraceMinutes;

    /**
     * 休息开始时间
     */
    @TableField("break_start_time")
    private LocalTime breakStartTime;

    /**
     * 休息结束时间
     */
    @TableField("break_end_time")
    private LocalTime breakEndTime;

    /**
     * 工作开始时间
     */
    @TableField("work_start_time")
    private LocalTime workStartTime;

    /**
     * 工作结束时间
     */
    @TableField("work_end_time")
    private LocalTime workEndTime;

    /**
     * 生效日期
     */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @TableField("expiry_date")
    private LocalDate expiryDate;

    /**
     * 优先级(数字越大优先级越高)
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 检查规则是否生效
     *
     * @return 是否生效
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && isEffective();
    }

    /**
     * 检查规则是否在有效期内
     *
     * @return 是否在有效期内
     */
    public boolean isEffective() {
        LocalDate now = LocalDate.now();
        boolean afterEffective = effectiveDate == null || !now.isBefore(effectiveDate);
        boolean beforeExpiry = expiryDate == null || !now.isAfter(expiryDate);
        return afterEffective && beforeExpiry;
    }

    /**
     * 检查规则是否适用于指定员工
     *
     * @param employeeId   员工ID
     * @param departmentId 部门ID
     * @param employeeType 员工类型
     * @return 是否适用
     */
    public boolean isApplicable(Long employeeId, Long departmentId, String employeeType) {
        switch (ruleType) {
            case "INDIVIDUAL":
                return this.employeeId != null && this.employeeId.equals(employeeId);
            case "DEPARTMENT":
                return this.departmentId != null && this.departmentId.equals(departmentId);
            case "GLOBAL":
                return this.employeeType == null || this.employeeType.equals(employeeType);
            default:
                return false;
        }
    }

    /**
     * 从JSON格式的workSchedule中获取工作开始时间（字符串格式）
     *
     * @deprecated 使用 workStartTime 字段替代
     * @return 工作开始时间字符串，解析失败返回null
     */
    @Deprecated
    public String getWorkStartTimeFromJson() {
        // 这里需要解析JSON格式的workSchedule字段
        // 实际实现中可以使用Jackson或FastJSON
        // 为了简化，这里返回null，实际使用时需要完整实现
        return null;
    }

    /**
     * 从JSON格式的workSchedule中获取工作结束时间（字符串格式）
     *
     * @deprecated 使用 workEndTime 字段替代
     * @return 工作结束时间字符串，解析失败返回null
     */
    @Deprecated
    public String getWorkEndTimeFromJson() {
        // 这里需要解析JSON格式的workSchedule字段
        return null;
    }

    /**
     * 获取工作天数
     *
     * @return 工作天数数组，解析失败返回空数组
     */
    public Integer[] getWorkDays() {
        // 这里需要解析JSON格式的workSchedule字段
        return new Integer[0];
    }

    /**
     * 比较规则优先级
     *
     * @param other 另一个规则
     * @return 比较结果
     */
    public int comparePriority(AttendanceRuleEntity other) {
        return Integer.compare(this.priority, other.priority);
    }

    // ==================== 手动添加的getter/setter方法（解决Lombok编译问题） ====================

    public Long getRuleId() {
        return ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public Integer getPriority() {
        return priority;
    }

    public Integer getGpsValidation() {
        return gpsValidation;
    }

    public Integer getLateTolerance() {
        return lateTolerance;
    }

    public Integer getEarlyTolerance() {
        return earlyTolerance;
    }

    public Integer getGpsRange() {
        return gpsRange;
    }

    public String getGpsLocations() {
        return gpsLocations;
    }

    public String getWorkSchedule() {
        return workSchedule;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setGpsValidation(Integer gpsValidation) {
        this.gpsValidation = gpsValidation;
    }

    public void setWorkSchedule(String workSchedule) {
        this.workSchedule = workSchedule;
    }

    // ==================== 新增字段的getter/setter方法 ====================

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getLocationRequired() {
        return locationRequired;
    }

    public void setLocationRequired(Boolean locationRequired) {
        this.locationRequired = locationRequired;
    }

    public Boolean getDeviceRequired() {
        return deviceRequired;
    }

    public void setDeviceRequired(Boolean deviceRequired) {
        this.deviceRequired = deviceRequired;
    }

    public Double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(Double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public Integer getLateGraceMinutes() {
        return lateGraceMinutes;
    }

    public void setLateGraceMinutes(Integer lateGraceMinutes) {
        this.lateGraceMinutes = lateGraceMinutes;
    }

    public Integer getEarlyLeaveGraceMinutes() {
        return earlyLeaveGraceMinutes;
    }

    public void setEarlyLeaveGraceMinutes(Integer earlyLeaveGraceMinutes) {
        this.earlyLeaveGraceMinutes = earlyLeaveGraceMinutes;
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public LocalTime getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(LocalTime breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }

    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }

    /**
     * GPS位置点内部类
     * 用于解析GPS位置配置JSON数据
     */
    public static class GpsLocationPoint {
        private String name;
        private Double latitude;
        private Double longitude;
        private Integer radius;

        public GpsLocationPoint() {
        }

        public GpsLocationPoint(String name, Double latitude, Double longitude, Integer radius) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public Integer getRadius() {
            return radius;
        }

        public void setRadius(Integer radius) {
            this.radius = radius;
        }
    }
}

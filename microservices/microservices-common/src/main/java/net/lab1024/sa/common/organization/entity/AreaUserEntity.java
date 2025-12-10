package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * 区域人员关联实体
 * <p>
 * 区域作为空间概念，需要关联人员
 * 人员信息可以下发到对应区域的设备中
 * 支持人员权限继承和区域权限管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_area_user_relation")
public class AreaUserEntity extends BaseEntity {

    /**
     * ObjectMapper实例（线程安全，可复用）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * 避免每次调用getter方法时创建新实例，提升性能
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 时间格式（HH:mm）
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 关联ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列relation_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * 注意：此实体类主键为String类型，使用雪花算法生成
     * </p>
     */
    @TableId(value = "relation_id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 区域编码
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 关联类型
     * 1-常驻人员 2-临时人员 3-访客 4-维护人员 5-管理人员
     */
    @TableField("relation_type")
    private Integer relationType;

    /**
     * 权限级别
     * 1-只读 2-读写 3-管理 4-超级管理员
     */
    @TableField("permission_level")
    private Integer permissionLevel;

    /**
     * 关联状态
     * 1-正常 2-禁用 3-过期 4-待审批
     */
    @TableField("relation_status")
    private Integer relationStatus;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 是否永久有效
     */
    @TableField("permanent")
    private Boolean permanent;

    /**
     * 权限继承（是否继承子区域权限）
     */
    @TableField("inherit_children")
    private Boolean inheritChildren;

    /**
     * 权限继承（是否继承父区域权限）
     */
    @TableField("inherit_parent")
    private Boolean inheritParent;

    /**
     * 允许进入时间（开始）
     */
    @TableField("allow_start_time")
    private String allowStartTime;

    /**
     * 允许进入时间（结束）
     */
    @TableField("allow_end_time")
    private String allowEndTime;

    /**
     * 工作日限制（周一到周五）
     */
    @TableField("workday_only")
    private Boolean workdayOnly;

    /**
     * 访问权限配置（JSON格式）
     * 存储具体的访问权限，如：门禁、考勤、消费等
     */
    @TableField("access_permissions")
    private String accessPermissions;

    /**
     * 设备下发状态
     * 0-未下发 1-下发中 2-下发成功 3-下发失败 4-已撤销
     */
    @TableField("device_sync_status")
    private Integer deviceSyncStatus;

    /**
     * 最后下发时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

    /**
     * 下发失败原因
     */
    @TableField("sync_failure_reason")
    private String syncFailureReason;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;






    // 版本字段已继承自BaseEntity，无需重复定义

    // ==================== 业务方法 ====================

    /**
     * 是否当前有效
     */
    public boolean isEffective() {
        if (permanent != null && permanent) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查生效时间
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }

        // 检查失效时间
        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 是否在允许时间范围内
     * <p>
     * 判断当前时间是否在允许的时间范围内
     * 支持跨天时间范围（如22:00-06:00）
     * 支持工作日限制（workdayOnly字段）
     * </p>
     *
     * @return 是否在允许时间范围内
     */
    public boolean isWithinAllowedTime() {
        if (allowStartTime == null || allowEndTime == null) {
            return true; // 无时间限制
        }

        try {
            // 1. 解析时间字符串（格式：HH:mm）
            LocalTime startTime = LocalTime.parse(allowStartTime, TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(allowEndTime, TIME_FORMATTER);
            LocalTime currentTime = LocalTime.now();

            // 2. 检查工作日限制
            if (workdayOnly != null && workdayOnly) {
                DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
                // 周六和周日不是工作日
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    return false;
                }
                // 检查节假日配置（从系统配置获取）
                if (isHolidayToday()) {
                    return false;
                }
            }

            // 3. 判断时间范围
            // 情况1：正常时间范围（如08:00-18:00）
            if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            }
            // 情况2：跨天时间范围（如22:00-06:00）
            else {
                // 当前时间在开始时间之后（如22:00-23:59）或结束时间之前（如00:00-06:00）
                return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
            }

        } catch (DateTimeParseException e) {
            // 时间格式错误，返回true（避免误判）
            return true;
        } catch (Exception e) {
            // 其他异常，返回true（避免误判）
            return true;
        }
    }

    /**
     * 是否需要设备同步
     */
    public boolean needsSync() {
        return deviceSyncStatus == null
                || deviceSyncStatus == 0 // 未下发
                || deviceSyncStatus == 3 // 下发失败
                || (deviceSyncStatus == 4 && retryCount != null && retryCount < 3); // 已撤销但可重试
    }

    /**
     * 是否有门禁权限
     * <p>
     * 解析accessPermissions JSON，检查门禁权限
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     * </p>
     *
     * @return 是否有门禁权限
     */
    public boolean hasAccessPermission() {
        return parsePermissionFromJson("access");
    }

    /**
     * 是否有考勤权限
     * <p>
     * 解析accessPermissions JSON，检查考勤权限
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     * </p>
     *
     * @return 是否有考勤权限
     */
    public boolean hasAttendancePermission() {
        return parsePermissionFromJson("attendance");
    }

    /**
     * 是否有消费权限
     * <p>
     * 解析accessPermissions JSON，检查消费权限
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     * </p>
     *
     * @return 是否有消费权限
     */
    public boolean hasConsumePermission() {
        return parsePermissionFromJson("consume");
    }

    /**
     * 从JSON中解析权限值（私有辅助方法）
     * <p>
     * 线程安全：ObjectMapper是线程安全的
     * </p>
     *
     * @param permissionKey 权限键（access/attendance/consume）
     * @return 是否有权限，默认返回false
     */
    private boolean parsePermissionFromJson(String permissionKey) {
        if (accessPermissions == null || accessPermissions.trim().isEmpty()) {
            return false; // 无权限配置，默认无权限
        }

        try {
            // 解析JSON
            Map<String, Object> permissionsMap = OBJECT_MAPPER.readValue(
                    accessPermissions,
                    new TypeReference<Map<String, Object>>() {}
            );

            if (permissionsMap == null || permissionsMap.isEmpty()) {
                return false;
            }

            // 获取权限值
            Object permissionValue = permissionsMap.get(permissionKey);
            if (permissionValue == null) {
                return false; // 权限字段不存在，默认无权限
            }

            // 转换为boolean
            if (permissionValue instanceof Boolean) {
                return (Boolean) permissionValue;
            } else if (permissionValue instanceof String) {
                return Boolean.parseBoolean((String) permissionValue);
            } else if (permissionValue instanceof Number) {
                return ((Number) permissionValue).intValue() != 0;
            }

            return false;

        } catch (Exception e) {
            // JSON解析异常，返回false（安全策略：无权限）
            return false;
        }
    }

    /**
     * 获取关联类型描述
     */
    public String getRelationTypeDesc() {
        switch (relationType) {
            case 1: return "常驻人员";
            case 2: return "临时人员";
            case 3: return "访客";
            case 4: return "维护人员";
            case 5: return "管理人员";
            default: return "未知类型";
        }
    }

    /**
     * 获取权限级别描述
     */
    public String getPermissionLevelDesc() {
        switch (permissionLevel) {
            case 1: return "只读";
            case 2: return "读写";
            case 3: return "管理";
            case 4: return "超级管理员";
            default: return "无权限";
        }
    }

    /**
     * 获取同步状态描述
     */
    public String getSyncStatusDesc() {
        switch (deviceSyncStatus) {
            case 0: return "未下发";
            case 1: return "下发中";
            case 2: return "下发成功";
            case 3: return "下发失败";
            case 4: return "已撤销";
            default: return "未知状态";
        }
    }

    /**
     * 检查今天是否是节假日
     *
     * @return true-是节假日，false-不是节假日
     */
    private boolean isHolidayToday() {
        try {
            LocalDate today = LocalDate.now();

            // 获取系统配置的节假日列表
            Set<String> holidays = getHolidaysFromConfig();

            // 检查今天是否在节假日列表中
            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return holidays.contains(todayStr);

        } catch (Exception e) {
            // 如果无法获取节假日配置，默认不是节假日（允许访问）
            return false;
        }
    }

    /**
     * 从系统配置获取节假日列表
     *
     * @return 节假日日期集合（格式：yyyy-MM-dd）
     */
    private Set<String> getHolidaysFromConfig() {
        // 这里可以从以下方式获取节假日配置：
        // 1. 系统配置表
        // 2. 配置文件
        // 3. 节假日服务API
        // 4. 数据库字典表

        // 示例：从系统配置获取
        Set<String> holidays = new HashSet<>();

        // 默认的法定节假日示例（实际应该从配置获取）
        holidays.addAll(Arrays.asList(
            "2025-01-01", // 元旦
            "2025-01-28", "2025-01-29", "2025-01-30", "2025-01-31", // 春节
            "2025-02-01", "2025-02-02", "2025-02-03",
            "2025-04-05", "2025-04-06", "2025-04-07", // 清明节
            "2025-05-01", "2025-05-02", "2025-05-03", // 劳动节
            "2025-06-08", "2025-06-09", "2025-06-10", // 端午节
            "2025-09-15", "2025-09-16", "2025-09-17", // 中秋节
            "2025-10-01", "2025-10-02", "2025-10-03", "2025-10-04", "2025-10-05", "2025-10-06", "2025-10-07" // 国庆节
        ));

        return holidays;
    }
}

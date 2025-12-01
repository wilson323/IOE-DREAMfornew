package net.lab1024.sa.attendance.domain.vo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 考勤规则视图对象
 *
 * <p>
 * 用于前端交互和API返回的数据传输对象
 * 包含考勤规则的所有可视信息
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-29
 */
@Data
@Accessors(chain = true)
@Schema(description = "考勤规则视图对象")
public class AttendanceRuleVO {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "标准考勤规则")
    private String ruleName;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码", example = "STANDARD_001")
    private String ruleCode;

    /**
     * 公司ID
     */
    @Schema(description = "公司ID", example = "1")
    private Long companyId;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 员工ID(个人规则时使用)
     */
    @Schema(description = "员工ID", example = "100")
    private Long employeeId;

    /**
     * 规则类型
     * GLOBAL-全局, DEPARTMENT-部门, INDIVIDUAL-个人
     */
    @Schema(description = "规则类型", example = "DEPARTMENT", allowableValues = { "GLOBAL", "DEPARTMENT", "INDIVIDUAL" })
    private String ruleType;

    /**
     * 适用员工类型
     * FULL_TIME-全职, PART_TIME-兼职, INTERN-实习
     */
    @Schema(description = "适用员工类型", example = "FULL_TIME", allowableValues = { "FULL_TIME", "PART_TIME", "INTERN" })
    private String employeeType;

    /**
     * 工作开始时间
     */
    @Schema(description = "工作开始时间", example = "09:00:00")
    private LocalTime workStartTime;

    /**
     * 工作结束时间
     */
    @Schema(description = "工作结束时间", example = "18:00:00")
    private LocalTime workEndTime;

    /**
     * 休息开始时间
     */
    @Schema(description = "休息开始时间", example = "12:00:00")
    private LocalTime breakStartTime;

    /**
     * 休息结束时间
     */
    @Schema(description = "休息结束时间", example = "13:00:00")
    private LocalTime breakEndTime;

    /**
     * 迟到容忍分钟数
     */
    @Schema(description = "迟到容忍分钟数", example = "5")
    private Integer lateTolerance;

    /**
     * 早退容忍分钟数
     */
    @Schema(description = "早退容忍分钟数", example = "5")
    private Integer earlyTolerance;

    /**
     * 迟到宽限分钟数
     */
    @Schema(description = "迟到宽限分钟数", example = "10")
    private Integer lateGraceMinutes;

    /**
     * 早退宽限分钟数
     */
    @Schema(description = "早退宽限分钟数", example = "10")
    private Integer earlyLeaveGraceMinutes;

    /**
     * 加班规则配置
     */
    @Schema(description = "加班规则配置", example = "{\"enabled\": true, \"minOvertimeMinutes\": 30}")
    private String overtimeRules;

    /**
     * 节假日规则配置
     */
    @Schema(description = "节假日规则配置", example = "{\"holidays\": [\"2024-01-01\"], \"overtimeRate\": 2.0}")
    private String holidayRules;

    /**
     * 是否启用GPS验证
     * 0-否, 1-是
     */
    @Schema(description = "是否启用GPS验证", example = "1", allowableValues = { "0", "1" })
    private Integer gpsValidation;

    /**
     * GPS位置配置
     */
    @Schema(description = "GPS位置配置", example = "[{\"name\": \"主办公区\", \"latitude\": 39.9042, \"longitude\": 116.4074, \"radius\": 100}]")
    private String gpsLocations;

    /**
     * GPS验证范围(米)
     */
    @Schema(description = "GPS验证范围(米)", example = "100")
    private Integer gpsRange;

    /**
     * 是否需要拍照打卡
     * 0-否, 1-是
     */
    @Schema(description = "是否需要拍照打卡", example = "0", allowableValues = { "0", "1" })
    private Integer photoRequired;

    /**
     * 是否启用人脸识别
     * 0-否, 1-是
     */
    @Schema(description = "是否启用人脸识别", example = "1", allowableValues = { "0", "1" })
    private Integer faceRecognition;

    /**
     * 设备限制配置
     */
    @Schema(description = "设备限制配置", example = "{\"allowedDevices\": [\"device1\"], \"maxDeviceCount\": 3}")
    private String deviceRestrictions;

    /**
     * 是否自动审批异常
     * 0-否, 1-是
     */
    @Schema(description = "是否自动审批异常", example = "0", allowableValues = { "0", "1" })
    private Integer autoApproval;

    /**
     * 通知设置
     */
    @Schema(description = "通知设置", example = "{\"late\": true, \"absent\": true, \"overtime\": true}")
    private String notificationSettings;

    /**
     * 规则状态
     * ACTIVE-激活, INACTIVE-停用, DRAFT-草稿
     */
    @Schema(description = "规则状态", example = "ACTIVE", allowableValues = { "ACTIVE", "INACTIVE", "DRAFT" })
    private String status;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 是否要求位置验证
     */
    @Schema(description = "是否要求位置验证", example = "true")
    private Boolean locationRequired;

    /**
     * 是否要求设备验证
     */
    @Schema(description = "是否要求设备验证", example = "false")
    private Boolean deviceRequired;

    /**
     * 最大距离(米)
     */
    @Schema(description = "最大距离(米)", example = "500.0")
    private Double maxDistance;

    /**
     * 生效日期
     */
    @Schema(description = "生效日期", example = "2024-01-01")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @Schema(description = "失效日期", example = "2024-12-31")
    private LocalDate expiryDate;

    /**
     * 优先级(数字越大优先级越高)
     */
    @Schema(description = "优先级", example = "10")
    private Integer priority;

    /**
     * 规则描述
     */
    @Schema(description = "规则描述", example = "标准工作日考勤规则，适用于全职员工")
    private String description;

    /**
     * 工作天数（解析后）
     */
    @Schema(description = "工作天数", example = "[1, 2, 3, 4, 5]")
    private List<Integer> workDays;

    /**
     * 是否在有效期内
     */
    @Schema(description = "是否在有效期内", example = "true")
    private Boolean effective;

    /**
     * 是否激活
     */
    @Schema(description = "是否激活", example = "true")
    private Boolean active;

    /**
     * 适用员工数量（统计信息）
     */
    @Schema(description = "适用员工数量", example = "150")
    private Integer coveredEmployeeCount;

    /**
     * 最后使用时间
     */
    @Schema(description = "最后使用时间", example = "2024-11-29T10:30:00")
    private String lastUsedTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T09:00:00")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-11-29T10:30:00")
    private String updateTime;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名", example = "张三")
    private String createUserName;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "李四")
    private String employeeName;
}

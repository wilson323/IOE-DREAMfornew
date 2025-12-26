package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤规则视图对象
 * <p>
 * 用于考勤规则响应数据
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤规则视图对象")
public class AttendanceRuleVO {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码", example = "RULE_TIME_001")
    private String ruleCode;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "迟到规则")
    private String ruleName;

    /**
     * 规则分类：TIME-时间规则 LOCATION-地点规则 ABSENCE-缺勤规则 OVERTIME-加班规则
     */
    @Schema(description = "规则分类", example = "TIME")
    private String ruleCategory;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "LATE_RULE")
    private String ruleType;

    /**
     * 规则条件（JSON格式）
     */
    @Schema(description = "规则条件（JSON格式）", example = "{\"lateMinutes\": 5}")
    private String ruleCondition;

    /**
     * 规则动作（JSON格式）
     */
    @Schema(description = "规则动作（JSON格式）", example = "{\"deductAmount\": 50}")
    private String ruleAction;

    /**
     * 规则优先级（数字越小优先级越高）
     */
    @Schema(description = "规则优先级", example = "1")
    private Integer rulePriority;

    /**
     * 生效开始时间
     */
    @Schema(description = "生效开始时间", example = "08:00")
    private String effectiveStartTime;

    /**
     * 生效结束时间
     */
    @Schema(description = "生效结束时间", example = "18:00")
    private String effectiveEndTime;

    /**
     * 生效日期（1,2,3,4,5,6,7）
     */
    @Schema(description = "生效日期", example = "1,2,3,4,5")
    private String effectiveDays;

    /**
     * 适用部门ID列表（JSON数组）
     */
    @Schema(description = "适用部门ID列表（JSON数组）", example = "[10, 20, 30]")
    private String departmentIds;

    /**
     * 适用用户ID列表（JSON数组）
     */
    @Schema(description = "适用用户ID列表（JSON数组）", example = "[1001, 1002, 1003]")
    private String userIds;

    /**
     * 规则状态：1-启用 0-禁用
     */
    @Schema(description = "规则状态", example = "1")
    private Integer ruleStatus;

    /**
     * 规则作用域：GLOBAL-全局 DEPARTMENT-部门 USER-个人
     */
    @Schema(description = "规则作用域", example = "DEPARTMENT")
    private String ruleScope;

    /**
     * 执行顺序
     */
    @Schema(description = "执行顺序", example = "1")
    private Integer executionOrder;

    /**
     * 父规则ID
     */
    @Schema(description = "父规则ID", example = "1234567890")
    private Long parentRuleId;

    /**
     * 规则版本
     */
    @Schema(description = "规则版本", example = "v1.0")
    private String ruleVersion;

    /**
     * 规则描述
     */
    @Schema(description = "规则描述", example = "迟到5分钟以内扣款50元")
    private String description;

    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T08:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01T08:00:00")
    private LocalDateTime updateTime;
}

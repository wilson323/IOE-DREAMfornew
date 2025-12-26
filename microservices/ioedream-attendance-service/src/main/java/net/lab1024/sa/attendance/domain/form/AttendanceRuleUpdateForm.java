package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤规则更新表单
 * <p>
 * 用于考勤规则更新请求参数
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
@Schema(description = "考勤规则更新表单")
public class AttendanceRuleUpdateForm {

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "迟到规则")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 规则分类：TIME-时间规则 LOCATION-地点规则 ABSENCE-缺勤规则 OVERTIME-加班规则
     */
    @Schema(description = "规则分类", example = "TIME")
    @NotBlank(message = "规则分类不能为空")
    private String ruleCategory;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "LATE_RULE")
    @NotBlank(message = "规则类型不能为空")
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
    @NotNull(message = "规则状态不能为空")
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
     * 规则描述
     */
    @Schema(description = "规则描述", example = "迟到5分钟以内扣款50元")
    private String description;

    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;
}

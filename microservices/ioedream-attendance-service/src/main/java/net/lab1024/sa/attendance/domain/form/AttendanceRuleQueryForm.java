package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤规则查询表单
 * <p>
 * 用于考勤规则查询请求参数
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
@Schema(description = "考勤规则查询表单")
public class AttendanceRuleQueryForm {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "迟到规则")
    private String ruleName;

    /**
     * 规则编码
     */
    @Schema(description = "规则编码", example = "RULE_TIME_001")
    private String ruleCode;

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
     * 员工ID（查询个人规则时使用）
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 部门ID（查询部门规则时使用）
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}

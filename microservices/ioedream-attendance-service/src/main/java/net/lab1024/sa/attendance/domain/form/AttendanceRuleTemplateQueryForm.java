package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考勤规则模板查询表单
 * <p>
 * 用于考勤规则模板查询请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤规则模板查询表单")
public class AttendanceRuleTemplateQueryForm {

    /**
     * 模板类型：SYSTEM-系统内置 CUSTOM-用户自定义
     */
    @Schema(description = "模板类型", example = "SYSTEM")
    private String templateType;

    /**
     * 模板分类：TIME-时间规则 ABSENCE-缺勤规则 OVERTIME-加班规则 LEAVE-请假规则
     */
    @Schema(description = "模板分类", example = "TIME")
    private String templateCategory;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 关键字搜索（模板名称、模板编码、标签）
     */
    @Schema(description = "关键字搜索", example = "迟到")
    private String keyword;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}

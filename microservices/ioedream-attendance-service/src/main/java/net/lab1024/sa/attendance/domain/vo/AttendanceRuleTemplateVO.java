package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考勤规则模板视图对象
 * <p>
 * 用于考勤规则模板响应数据
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
@Schema(description = "考勤规则模板视图对象")
public class AttendanceRuleTemplateVO {

    /**
     * 模板ID（主键）
     */
    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    /**
     * 模板编码（唯一标识）
     */
    @Schema(description = "模板编码", example = "LATE_DEDUCT_50")
    private String templateCode;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "迟到扣款50元")
    private String templateName;

    /**
     * 模板分类：TIME-时间规则 ABSENCE-缺勤规则 OVERTIME-加班规则 LEAVE-请假规则
     */
    @Schema(description = "模板分类", example = "TIME")
    private String templateCategory;

    /**
     * 模板类型：SYSTEM-系统内置 CUSTOM-用户自定义
     */
    @Schema(description = "模板类型", example = "SYSTEM")
    private String templateType;

    /**
     * 模板条件（JSON格式）
     * 示例：{"lateMinutes": 5}
     */
    @Schema(description = "模板条件（JSON）", example = "{\"lateMinutes\": 5}")
    private String templateCondition;

    /**
     * 模板动作（JSON格式）
     * 示例：{"deductAmount": 50}
     */
    @Schema(description = "模板动作（JSON）", example = "{\"deductAmount\": 50}")
    private String templateAction;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述", example = "迟到超过5分钟扣款50元")
    private String description;

    /**
     * 是否系统模板：0-否 1-是
     */
    @Schema(description = "是否系统模板", example = "1")
    private Integer isSystem;

    /**
     * 状态：0-禁用 1-启用
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 排序号（数字越小越靠前）
     */
    @Schema(description = "排序号", example = "1")
    private Integer sort;

    /**
     * 使用次数（统计字段）
     */
    @Schema(description = "使用次数", example = "100")
    private Integer useCount;

    /**
     * 适用部门ID列表（JSON数组，null表示全部）
     * 示例：[1, 2, 3]
     */
    @Schema(description = "适用部门ID列表", example = "[1, 2, 3]")
    private String departmentIds;

    /**
     * 标签（逗号分隔）
     * 示例：迟到,扣款,常用
     */
    @Schema(description = "标签", example = "迟到,扣款,常用")
    private String tags;

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "1.0.0")
    private String version;

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

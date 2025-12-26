package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 考勤规则模板实体类
 * <p>
 * 规则模板管理，支持系统预置模板和用户自定义模板
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_attendance_rule_template")
@Schema(description = "考勤规则模板实体")
public class AttendanceRuleTemplateEntity extends BaseEntity {

    /**
     * 模板ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    /**
     * 模板编码（唯一标识）
     */
    @TableField("template_code")
    @Schema(description = "模板编码", example = "LATE_DEDUCT_50")
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 模板名称
     */
    @TableField("template_name")
    @Schema(description = "模板名称", example = "迟到扣款50元")
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 模板分类：TIME-时间规则 ABSENCE-缺勤规则 OVERTIME-加班规则 LEAVE-请假规则
     */
    @TableField("template_category")
    @Schema(description = "模板分类", example = "TIME")
    @NotBlank(message = "模板分类不能为空")
    private String templateCategory;

    /**
     * 模板类型：SYSTEM-系统内置 CUSTOM-用户自定义
     */
    @TableField("template_type")
    @Schema(description = "模板类型", example = "SYSTEM")
    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    /**
     * 模板条件（JSON格式）
     * 示例：{"lateMinutes": 5}
     */
    @TableField("template_condition")
    @Schema(description = "模板条件（JSON）", example = "{\"lateMinutes\": 5}")
    @NotBlank(message = "模板条件不能为空")
    private String templateCondition;

    /**
     * 模板动作（JSON格式）
     * 示例：{"deductAmount": 50}
     */
    @TableField("template_action")
    @Schema(description = "模板动作（JSON）", example = "{\"deductAmount\": 50}")
    @NotBlank(message = "模板动作不能为空")
    private String templateAction;

    /**
     * 模板描述
     */
    @TableField("description")
    @Schema(description = "模板描述", example = "迟到超过5分钟扣款50元")
    private String description;

    /**
     * 是否系统模板：0-否 1-是
     */
    @TableField("is_system")
    @Schema(description = "是否系统模板", example = "1")
    @NotNull(message = "是否系统模板不能为空")
    private Integer isSystem;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    @Schema(description = "状态", example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 排序号（数字越小越靠前）
     */
    @TableField("sort")
    @Schema(description = "排序号", example = "1")
    private Integer sort;

    /**
     * 使用次数（统计字段）
     */
    @TableField("use_count")
    @Schema(description = "使用次数", example = "100")
    private Integer useCount;

    /**
     * 适用部门ID列表（JSON数组，null表示全部）
     * 示例：[1, 2, 3]
     */
    @TableField("department_ids")
    @Schema(description = "适用部门ID列表", example = "[1, 2, 3]")
    private String departmentIds;

    /**
     * 标签（逗号分隔）
     * 示例：迟到,扣款,常用
     */
    @TableField("tags")
    @Schema(description = "标签", example = "迟到,扣款,常用")
    private String tags;

    /**
     * 版本号（重命名为templateVersion以避免与BaseEntity的version冲突）
     */
    @TableField("version")
    @Schema(description = "版本号", example = "1.0.0")
    private String templateVersion;

    /**
     * 便捷方法：判断是否系统模板
     */
    public boolean isSystemTemplate() {
        return this.isSystem != null && this.isSystem == 1;
    }

    /**
     * 便捷方法：判断是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 便捷方法：增加使用次数
     */
    public void incrementUseCount() {
        if (this.useCount == null) {
            this.useCount = 0;
        }
        this.useCount++;
    }
}

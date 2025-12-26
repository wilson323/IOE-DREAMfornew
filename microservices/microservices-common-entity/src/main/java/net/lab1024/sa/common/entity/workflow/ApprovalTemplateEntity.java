package net.lab1024.sa.common.entity.workflow;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批模板实体
 * <p>
 * 存储审批流程模板信息，可复用的审批流程配置
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>审批模板管理</li>
 *   <li>审批模板复用</li>
 *   <li>审批模板版本控制</li>
 *   <li>审批模板分类</li>
 *   <li>审批模板统计</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>创建标准请假审批模板</li>
 *   <li>创建报销审批模板</li>
 *   <li>创建采购审批模板</li>
 *   <li>模板使用次数统计</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.ApprovalTemplateDao 审批模板DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_approval_template")
@Schema(description = "审批模板实体")
public class ApprovalTemplateEntity extends BaseEntity {

    /**
     * 模板ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "模板ID", example = "1001")
    private Long id;

    /**
     * 模板编码
     * <p>
     * 模板的唯一标识
     * </p>
     */
    @NotBlank
    @Size(max = 100)
    @TableField("template_code")
    @Schema(description = "模板编码", example = "LEAVE_APPROVAL_TEMPLATE")
    private String templateCode;

    /**
     * 模板名称
     * <p>
     * 模板的显示名称
     * </p>
     */
    @NotBlank
    @Size(max = 200)
    @TableField("template_name")
    @Schema(description = "模板名称", example = "标准请假审批模板")
    private String templateName;

    /**
     * 模板分类
     * <p>
     * 模板的分类，如：HR、财务、采购、合同等
     * </p>
     */
    @Size(max = 50)
    @TableField("category")
    @Schema(description = "模板分类", example = "HR")
    private String category;

    /**
     * 模板版本
     * <p>
     * 模板的版本号
     * </p>
     */
    @TableField("version")
    @Schema(description = "模板版本", example = "1")
    private Integer version;

    /**
     * 模板状态
     * <p>
     * DRAFT-草稿
     * PUBLISHED-已发布
     * DISABLED-已禁用
     * </p>
     */
    @NotBlank
    @Size(max = 20)
    @TableField("status")
    @Schema(description = "模板状态", example = "PUBLISHED")
    private String status;

    /**
     * 模板描述
     */
    @Size(max = 1000)
    @TableField("description")
    @Schema(description = "模板描述", example = "适用于员工请假申请的标准审批流程模板")
    private String description;

    /**
     * 使用次数
     * <p>
     * 该模板被使用的次数统计
     * </p>
     */
    @TableField("usage_count")
    @Schema(description = "使用次数", example = "100")
    private Integer usageCount;

    /**
     * 审批节点数
     * <p>
     * 该模板包含的审批节点数量
     * </p>
     */
    @TableField("node_count")
    @Schema(description = "审批节点数", example = "5")
    private Integer nodeCount;

    /**
     * 审批流程定义（JSON格式）
     * <p>
     * 存储审批流程的完整定义
     * 包括所有节点配置和流转规则
     * </p>
     */
    @TableField("process_definition")
    @Schema(description = "审批流程定义（JSON格式）")
    private String processDefinition;

    /**
     * 表单配置（JSON格式）
     * <p>
     * 审批表单的配置信息
     * 定义表单字段、验证规则等
     * </p>
     */
    @TableField("form_config")
    @Schema(description = "表单配置（JSON格式）")
    private String formConfig;

    /**
     * 默认审批规则（JSON格式）
     * <p>
     * 模板的默认审批规则配置
     * 示例：{"autoApprove":false,"requiredCount":3}
     * </p>
     */
    @TableField("default_rules")
    @Schema(description = "默认审批规则（JSON格式）")
    private String defaultRules;

    /**
     * 是否公开
     * <p>
     * true-公开模板，所有用户可见
     * false-私有模板，仅创建者可见
     * </p>
     */
    @TableField("is_public")
    @Schema(description = "是否公开", example = "true")
    private Boolean isPublic;

    /**
     * 创建人ID
     * <p>
     * 模板的创建人用户ID
     * 关联UserEntity.userId
     * </p>
     */
    @TableField("creator_id")
    @Schema(description = "创建人ID", example = "1001")
    private Long creatorId;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 模板状态枚举
     */
    public enum TemplateStatus {
        DRAFT("DRAFT", "草稿"),
        PUBLISHED("PUBLISHED", "已发布"),
        DISABLED("DISABLED", "已禁用");

        private final String code;
        private final String description;

        TemplateStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateStatus fromCode(String code) {
            for (TemplateStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid template status code: " + code);
        }
    }
}

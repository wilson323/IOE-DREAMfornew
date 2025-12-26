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
 * 审批配置实体
 * <p>
 * 存储业务类型的审批配置信息，定义业务流程的审批规则
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>审批配置管理</li>
 *   <li>业务类型关联</li>
 *   <li>审批规则定义</li>
 *   <li>审批模板关联</li>
 *   <li>审批配置版本控制</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>配置请假审批流程</li>
 *   <li>配置报销审批流程</li>
 *   <li>配置采购审批流程</li>
 *   <li>配置合同审批流程</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.ApprovalConfigDao 审批配置DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_approval_config")
@Schema(description = "审批配置实体")
public class ApprovalConfigEntity extends BaseEntity {

    /**
     * 审批配置ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "审批配置ID", example = "1001")
    private Long id;

    /**
     * 业务类型
     * <p>
     * 关联的业务类型，如：leave、reimbursement、purchase、contract等
     * 唯一标识一类业务的审批流程
     * </p>
     */
    @NotBlank
    @Size(max = 100)
    @TableField("business_type")
    @Schema(description = "业务类型", example = "leave")
    private String businessType;

    /**
     * 业务类型名称
     * <p>
     * 业务类型的显示名称
     * </p>
     */
    @NotBlank
    @Size(max = 200)
    @TableField("business_type_name")
    @Schema(description = "业务类型名称", example = "请假申请")
    private String businessTypeName;

    /**
     * 模块
     * <p>
     * 所属模块，如：OA、HR、FINANCE等
     * </p>
     */
    @Size(max = 50)
    @TableField("module")
    @Schema(description = "模块", example = "OA")
    private String module;

    /**
     * 流程定义ID
     * <p>
     * 关联WorkflowDefinitionEntity.id
     * 如果使用Flowable工作流引擎
     * </p>
     */
    @TableField("process_definition_id")
    @Schema(description = "流程定义ID", example = "1001")
    private Long processDefinitionId;

    /**
     * 流程编码
     * <p>
     * 关联的流程编码
     * </p>
     */
    @Size(max = 100)
    @TableField("process_key")
    @Schema(description = "流程编码", example = "leave_approval")
    private String processKey;

    /**
     * 审批模板ID
     * <p>
     * 关联ApprovalTemplateEntity.id
     * 如果使用审批模板
     * </p>
     */
    @TableField("approval_template_id")
    @Schema(description = "审批模板ID", example = "1001")
    private Long approvalTemplateId;

    /**
     * 配置状态
     * <p>
     * ENABLED-启用
     * DISABLED-禁用
     * </p>
     */
    @NotBlank
    @Size(max = 20)
    @TableField("status")
    @Schema(description = "配置状态", example = "ENABLED")
    private String status;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-12-27T00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    @Schema(description = "失效时间", example = "2026-12-31T23:59:59")
    private LocalDateTime expireTime;

    /**
     * 排序
     * <p>
     * 用于多个审批配置的排序
     * 值越大优先级越高
     * </p>
     */
    @TableField("sort_order")
    @Schema(description = "排序", example = "100")
    private Integer sortOrder;

    /**
     * 配置描述
     */
    @Size(max = 1000)
    @TableField("description")
    @Schema(description = "配置描述", example = "员工请假审批配置")
    private String description;

    /**
     * 审批规则（JSON格式）
     * <p>
     * 存储审批规则配置
     * 示例：{"autoApprove":false,"requiredCount":3}
     * </p>
     */
    @TableField("approval_rules")
    @Schema(description = "审批规则（JSON格式）")
    private String approvalRules;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 配置状态枚举
     */
    public enum ConfigStatus {
        ENABLED("ENABLED", "启用"),
        DISABLED("DISABLED", "禁用");

        private final String code;
        private final String description;

        ConfigStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ConfigStatus fromCode(String code) {
            for (ConfigStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid config status code: " + code);
        }
    }
}

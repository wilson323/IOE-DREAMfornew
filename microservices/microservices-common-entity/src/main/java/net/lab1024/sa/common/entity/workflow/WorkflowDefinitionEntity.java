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
 * 工作流定义实体
 * <p>
 * 存储工作流流程定义信息，包括流程编码、名称、版本、状态等
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>流程定义管理</li>
 *   <li>流程版本控制</li>
 *   <li>流程发布管理</li>
 *   <li>流程实例统计</li>
 *   <li>流程分类管理</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>工作流设计器创建流程定义</li>
 *   <li>流程定义发布和激活</li>
 *   <li>流程定义版本管理</li>
 *   <li>流程定义查询和统计</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.WorkflowDefinitionDao 工作流定义DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_workflow_definition")
@Schema(description = "工作流定义实体")
public class WorkflowDefinitionEntity extends BaseEntity {

    /**
     * 流程定义ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "流程定义ID", example = "1001")
    private Long id;

    /**
     * 流程编码（唯一标识）
     * <p>
     * 流程的唯一标识，用于引用和查询
     * 同一流程的不同版本共享相同的processKey
     * </p>
     */
    @NotBlank
    @Size(max = 100)
    @TableField("process_key")
    @Schema(description = "流程编码", example = "leave_approval")
    private String processKey;

    /**
     * 流程名称
     * <p>
     * 流程的显示名称，可包含中文
     * </p>
     */
    @NotBlank
    @Size(max = 200)
    @TableField("process_name")
    @Schema(description = "流程名称", example = "请假审批流程")
    private String processName;

    /**
     * 流程描述
     */
    @Size(max = 1000)
    @TableField("description")
    @Schema(description = "流程描述", example = "员工请假申请审批流程")
    private String description;

    /**
     * 流程分类
     * <p>
     * 用于分类管理流程，如：HR、财务、采购等
     * </p>
     */
    @Size(max = 50)
    @TableField("category")
    @Schema(description = "流程分类", example = "HR")
    private String category;

    /**
     * 流程版本号
     * <p>
     * 同一流程的版本号，每次修改递增
     * </p>
     */
    @TableField("version")
    @Schema(description = "流程版本号", example = "1")
    private Integer version;

    /**
     * 流程定义状态
     * <p>
     * DRAFT-草稿
     * PUBLISHED-已发布
     * DISABLED-已禁用
     * ACTIVE-活跃
     * </p>
     */
    @NotBlank
    @Size(max = 20)
    @TableField("status")
    @Schema(description = "流程定义状态", example = "PUBLISHED")
    private String status;

    /**
     * 是否最新版本
     * <p>
     * true-最新版本
     * false-历史版本
     * </p>
     */
    @TableField("is_latest")
    @Schema(description = "是否最新版本", example = "true")
    private Boolean isLatest;

    /**
     * 流程实例数量
     * <p>
     * 基于该流程定义创建的实例总数
     * </p>
     */
    @TableField("instance_count")
    @Schema(description = "流程实例数量", example = "100")
    private Integer instanceCount;

    /**
     * 最后部署时间
     */
    @TableField("last_deploy_time")
    @Schema(description = "最后部署时间", example = "2025-12-27T10:00:00")
    private LocalDateTime lastDeployTime;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-12-27T10:00:00")
    private LocalDateTime effectiveTime;

    /**
     * Flowable流程定义ID
     * <p>
     * Flowable引擎中的流程定义ID
     * </p>
     */
    @Size(max = 100)
    @TableField("flowable_def_id")
    @Schema(description = "Flowable流程定义ID", example = "leaveApproval:1:1001")
    private String flowableDefId;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 流程定义状态枚举
     */
    public enum DefinitionStatus {
        DRAFT("DRAFT", "草稿"),
        PUBLISHED("PUBLISHED", "已发布"),
        DISABLED("DISABLED", "已禁用"),
        ACTIVE("ACTIVE", "活跃");

        private final String code;
        private final String description;

        DefinitionStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static DefinitionStatus fromCode(String code) {
            for (DefinitionStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid definition status code: " + code);
        }
    }
}

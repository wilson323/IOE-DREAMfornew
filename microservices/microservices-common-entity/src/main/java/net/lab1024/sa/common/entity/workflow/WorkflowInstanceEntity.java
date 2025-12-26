package net.lab1024.sa.common.entity.workflow;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作流实例实体
 * <p>
 * 存储工作流流程实例信息，记录流程实例的运行状态和过程
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>流程实例管理</li>
 *   <li>流程状态跟踪</li>
 *   <li>流程操作记录</li>
 *   <li>流程统计分析</li>
 *   <li>流程历史查询</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>员工发起请假申请创建流程实例</li>
 *   <li>流程实例状态变更（运行中、已完成、已终止、已挂起）</li>
 *   <li>流程实例查询和统计</li>
 *   <li>流程实例历史追溯</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.WorkflowInstanceDao 工作流实例DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_workflow_instance")
@Schema(description = "工作流实例实体")
public class WorkflowInstanceEntity extends BaseEntity {

    /**
     * 流程实例ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "流程实例ID", example = "1001")
    private Long instanceId;

    /**
     * 流程定义ID
     * <p>
     * 关联WorkflowDefinitionEntity.id
     * </p>
     */
    @NotNull
    @TableField("process_definition_id")
    @Schema(description = "流程定义ID", example = "1001")
    private Long processDefinitionId;

    /**
     * 流程编码
     * <p>
     * 冗余字段，用于快速查询
     * 与WorkflowDefinitionEntity.processKey保持同步
     * </p>
     */
    @Size(max = 100)
    @TableField("process_key")
    @Schema(description = "流程编码", example = "leave_approval")
    private String processKey;

    /**
     * 流程名称
     * <p>
     * 冗余字段，用于快速查询
     * 与WorkflowDefinitionEntity.processName保持同步
     * </p>
     */
    @Size(max = 200)
    @TableField("process_name")
    @Schema(description = "流程名称", example = "请假审批流程")
    private String processName;

    /**
     * 发起人ID
     * <p>
     * 发起该流程实例的用户ID
     * 关联UserEntity.userId
     * </p>
     */
    @NotNull
    @TableField("initiator_id")
    @Schema(description = "发起人ID", example = "1001")
    private Long initiatorId;

    /**
     * 发起人姓名
     * <p>
     * 冗余字段，用于快速查询
     * </p>
     */
    @Size(max = 100)
    @TableField("initiator_name")
    @Schema(description = "发起人姓名", example = "张三")
    private String initiatorName;

    /**
     * 流程实例状态
     * <p>
     * 1-运行中
     * 2-已完成
     * 3-已终止
     * 4-已挂起
     * </p>
     */
    @NotNull
    @TableField("status")
    @Schema(description = "流程实例状态", example = "1")
    private Integer status;

    /**
     * 业务主键
     * <p>
     * 关联业务数据的ID，如请假ID、报销ID等
     * </p>
     */
    @TableField("business_key")
    @Schema(description = "业务主键", example = "LEAVE_20251227_001")
    private String businessKey;

    /**
     * 开始时间
     */
    @NotNull
    @TableField("start_time")
    @Schema(description = "开始时间", example = "2025-12-27T10:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    @Schema(description = "结束时间", example = "2025-12-27T15:00:00")
    private LocalDateTime endTime;

    /**
     * 挂起时间
     */
    @TableField("suspend_time")
    @Schema(description = "挂起时间", example = "2025-12-27T12:00:00")
    private LocalDateTime suspendTime;

    /**
     * 持续时间（毫秒）
     */
    @TableField("duration")
    @Schema(description = "持续时间（毫秒）", example = "1800000")
    private Long duration;

    /**
     * 当前节点ID
     * <p>
     * 流程当前所处的节点ID
     * </p>
     */
    @Size(max = 100)
    @TableField("current_node_id")
    @Schema(description = "当前节点ID", example = "UserTask_001")
    private String currentNodeId;

    /**
     * 当前节点名称
     * <p>
     * 冗余字段，用于快速查询
     * </p>
     */
    @Size(max = 200)
    @TableField("current_node_name")
    @Schema(description = "当前节点名称", example = "部门经理审批")
    private String currentNodeName;

    /**
     * 流程变量（JSON格式）
     * <p>
     * 存储流程执行过程中的变量数据
     * </p>
     */
    @TableField("process_variables")
    @Schema(description = "流程变量（JSON格式）")
    private String processVariables;

    /**
     * 原因/备注
     * <p>
     * 挂起、终止、撤销等操作的原因说明
     * </p>
     */
    @Size(max = 500)
    @TableField("reason")
    @Schema(description = "原因/备注", example = "审批未通过，流程终止")
    private String reason;

    /**
     * Flowable流程实例ID
     * <p>
     * Flowable引擎中的流程实例ID
     * </p>
     */
    @Size(max = 100)
    @TableField("flowable_process_instance_id")
    @Schema(description = "Flowable流程实例ID", example = "leaveApproval-1001")
    private String flowableProcessInstanceId;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 流程实例状态枚举
     */
    public enum InstanceStatus {
        RUNNING(1, "运行中"),
        COMPLETED(2, "已完成"),
        TERMINATED(3, "已终止"),
        SUSPENDED(4, "已挂起");

        private final int code;
        private final String description;

        InstanceStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static InstanceStatus fromCode(int code) {
            for (InstanceStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid instance status code: " + code);
        }
    }
}

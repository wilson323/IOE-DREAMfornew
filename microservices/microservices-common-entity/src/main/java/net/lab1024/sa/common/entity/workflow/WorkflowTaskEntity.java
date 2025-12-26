package net.lab1024.sa.common.entity.workflow;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作流任务实体
 * <p>
 * 存储工作流任务信息，包括待办、已办、已委派、已转交等任务
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>主要功能：</strong></p>
 * <ul>
 *   <li>任务管理</li>
 *   <li>任务分配</li>
 *   <li>任务执行</li>
 *   <li>任务委派和转交</li>
 *   <li>任务统计</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>部门经理收到请假审批任务</li>
 *   <li>任务受理、完成、驳回</li>
 *   <li>任务委派给其他人</li>
 *   <li>任务转交给其他人</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-27
 * @see net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao 工作流任务DAO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_common_workflow_task")
@Schema(description = "工作流任务实体")
public class WorkflowTaskEntity extends BaseEntity {

    /**
     * 任务ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "任务ID", example = "1001")
    private Long taskId;

    /**
     * 流程实例ID
     * <p>
     * 关联WorkflowInstanceEntity.instanceId
     * </p>
     */
    @NotNull
    @TableField("instance_id")
    @Schema(description = "流程实例ID", example = "1001")
    private Long instanceId;

    /**
     * 任务名称
     * <p>
     * 任务节点的名称
     * </p>
     */
    @NotBlank
    @Size(max = 200)
    @TableField("task_name")
    @Schema(description = "任务名称", example = "部门经理审批")
    private String taskName;

    /**
     * 任务描述
     */
    @Size(max = 1000)
    @TableField("task_description")
    @Schema(description = "任务描述", example = "审批员工请假申请")
    private String taskDescription;

    /**
     * 受理人ID
     * <p>
     * 当前任务的受理人用户ID
     * 关联UserEntity.userId
     * </p>
     */
    @TableField("assignee_id")
    @Schema(description = "受理人ID", example = "1001")
    private Long assigneeId;

    /**
     * 受理人姓名
     * <p>
     * 冗余字段，用于快速查询
     * </p>
     */
    @Size(max = 100)
    @TableField("assignee_name")
    @Schema(description = "受理人姓名", example = "张三")
    private String assigneeName;

    /**
     * 原始受理人ID
     * <p>
     * 任务委派或转交前的原始受理人ID
     * </p>
     */
    @TableField("original_assignee_id")
    @Schema(description = "原始受理人ID", example = "1002")
    private Long originalAssigneeId;

    /**
     * 原始受理人姓名
     */
    @Size(max = 100)
    @TableField("original_assignee_name")
    @Schema(description = "原始受理人姓名", example = "李四")
    private String originalAssigneeName;

    /**
     * 候选用户（JSON格式）
     * <p>
     * 可以受理该任务的候选用户ID列表
     * 示例：[1001, 1002, 1003]
     * </p>
     */
    @TableField("candidate_users")
    @Schema(description = "候选用户（JSON格式）", example = "[1001,1002,1003]")
    private String candidateUsers;

    /**
     * 候选组（JSON格式）
     * <p>
     * 可以受理该任务的候选组角色列表
     * 示例：["ROLE_MANAGER", "ROLE_HR"]
     * </p>
     */
    @TableField("candidate_groups")
    @Schema(description = "候选组（JSON格式）", example = "[\"ROLE_MANAGER\",\"ROLE_HR\"]")
    private String candidateGroups;

    /**
     * 任务状态
     * <p>
     * 1-待处理
     * 2-处理中
     * 3-已完成
     * 4-已转交
     * 5-已委派
     * 6-已驳回
     * </p>
     */
    @NotNull
    @TableField("status")
    @Schema(description = "任务状态", example = "1")
    private Integer status;

    /**
     * 处理结果
     * <p>
     * 1-通过
     * 2-驳回
     * 3-转交
     * 4-委派
     * </p>
     */
    @TableField("result")
    @Schema(description = "处理结果", example = "1")
    private Integer result;

    /**
     * 任务优先级
     * <p>
     * 1-低
     * 2-中
     * 3-高
     * 4-紧急
     * </p>
     */
    @TableField("priority")
    @Schema(description = "任务优先级", example = "2")
    private Integer priority;

    /**
     * 意见/备注
     * <p>
     * 任务处理时的审批意见或备注
     * </p>
     */
    @Size(max = 2000)
    @TableField("comment")
    @Schema(description = "意见/备注", example = "同意，准予请假")
    private String comment;

    /**
     * 任务创建时间
     * <p>
     * 任务节点激活的时间
     * </p>
     */
    @NotNull
    @TableField("task_create_time")
    @Schema(description = "任务创建时间", example = "2025-12-27T10:00:00")
    private LocalDateTime taskCreateTime;

    /**
     * 开始时间
     * <p>
     * 任务受理的时间
     * </p>
     */
    @TableField("start_time")
    @Schema(description = "开始时间", example = "2025-12-27T10:30:00")
    private LocalDateTime startTime;

    /**
     * 结束时间
     * <p>
     * 任务完成的时间
     * </p>
     */
    @TableField("end_time")
    @Schema(description = "结束时间", example = "2025-12-27T11:00:00")
    private LocalDateTime endTime;

    /**
     * 到期时间
     * <p>
     * 任务期望完成的时间
     * </p>
     */
    @TableField("due_time")
    @Schema(description = "到期时间", example = "2025-12-28T10:00:00")
    private LocalDateTime dueTime;

    /**
     * 委派/转交时间
     */
    @TableField("delegate_time")
    @Schema(description = "委派/转交时间", example = "2025-12-27T10:45:00")
    private LocalDateTime delegateTime;

    /**
     * 持续时间（毫秒）
     * <p>
     * 从任务创建到完成的总耗时
     * </p>
     */
    @TableField("duration")
    @Schema(description = "持续时间（毫秒）", example = "1800000")
    private Long duration;

    /**
     * Flowable任务ID
     * <p>
     * Flowable引擎中的任务ID
     * </p>
     */
    @Size(max = 100)
    @TableField("flowable_task_id")
    @Schema(description = "Flowable任务ID", example = "task-1001")
    private String flowableTaskId;

    /**
     * 任务节点定义Key
     * <p>
     * Flowable流程定义中的节点Key
     * </p>
     */
    @Size(max = 100)
    @TableField("task_def_key")
    @Schema(description = "任务节点定义Key", example = "UserTask_001")
    private String taskDefKey;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING(1, "待处理"),
        IN_PROGRESS(2, "处理中"),
        COMPLETED(3, "已完成"),
        TRANSFERRED(4, "已转交"),
        DELEGATED(5, "已委派"),
        REJECTED(6, "已驳回");

        private final int code;
        private final String description;

        TaskStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TaskStatus fromCode(int code) {
            for (TaskStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid task status code: " + code);
        }
    }

    /**
     * 处理结果枚举
     */
    public enum TaskResult {
        APPROVE(1, "通过"),
        REJECT(2, "驳回"),
        TRANSFER(3, "转交"),
        DELEGATE(4, "委派");

        private final int code;
        private final String description;

        TaskResult(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TaskResult fromCode(int code) {
            for (TaskResult result : values()) {
                if (result.code == code) {
                    return result;
                }
            }
            throw new IllegalArgumentException("Invalid task result code: " + code);
        }
    }

    /**
     * 任务优先级枚举
     */
    public enum TaskPriority {
        LOW(1, "低"),
        MEDIUM(2, "中"),
        HIGH(3, "高"),
        URGENT(4, "紧急");

        private final int code;
        private final String description;

        TaskPriority(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TaskPriority fromCode(int code) {
            for (TaskPriority priority : values()) {
                if (priority.code == code) {
                    return priority;
                }
            }
            throw new IllegalArgumentException("Invalid task priority code: " + code);
        }
    }
}

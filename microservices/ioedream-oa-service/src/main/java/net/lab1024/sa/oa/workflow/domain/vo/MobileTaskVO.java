package net.lab1024.sa.oa.workflow.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 移动端任务视图对象
 * <p>
 * 专门为移动端优化的任务展示对象
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
public class MobileTaskVO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态：PENDING-待处理 IN_PROGRESS-处理中 COMPLETED-已完成
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 优先级：1-低 2-中 3-高 4-紧急
     */
    private Integer priority;

    /**
     * 优先级描述
     */
    private String priorityDesc;

    /**
     * 发起人ID
     */
    private Long starterUserId;

    /**
     * 发起人姓名
     */
    private String starterUserName;

    /**
     * 发起人头像
     */
    private String starterAvatar;

    /**
     * 发起时间
     */
    private LocalDateTime startTime;

    /**
     * 截止时间
     */
    private LocalDateTime dueTime;

    /**
     * 剩余时间（小时）
     */
    private Long remainingHours;

    /**
     * 是否超时
     */
    private Boolean isOverdue;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 业务Key
     */
    private String businessKey;

    /**
     * 表单Key
     */
    private String formKey;

    /**
     * 表单数据（JSON格式）
     */
    private Map<String, Object> formData;

    /**
     * 表单摘要（用于列表展示）
     */
    private String formSummary;

    /**
     * 处理人ID列表
     */
    private List<Long> assigneeIds;

    /**
     * 处理人姓名列表
     */
    private List<String> assigneeNames;

    /**
     * 候选用户ID列表
     */
    private List<Long> candidateUserIds;

    /**
     * 候选组ID列表
     */
    private List<Long> candidateGroupIds;

    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 附件数量
     */
    private Integer attachmentCount;

    /**
     * 是否可撤回
     */
    private Boolean canWithdraw;

    /**
     * 是否可委派
     */
    private Boolean canDelegate;

    /**
     * 是否可转交
     */
    private Boolean canTransfer;

    /**
     * 操作按钮列表
     */
    private List<String> actionButtons;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;
}

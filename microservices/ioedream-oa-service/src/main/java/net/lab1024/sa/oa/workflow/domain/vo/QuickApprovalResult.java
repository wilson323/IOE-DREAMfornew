package net.lab1024.sa.oa.workflow.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 快速审批结果视图对象
 * <p>
 * 封装快速审批操作的结果信息
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
public class QuickApprovalResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 结果代码：SUCCESS-成功 FAILED-失败 PENDING-待处理
     */
    private String resultCode;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 审批操作：APPROVE-同意 REJECT-驳回 TRANSFER-转交 DELEGATE-委派
     */
    private String action;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 处理人ID
     */
    private Long handlerUserId;

    /**
     * 处理人姓名
     */
    private String handlerUserName;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 下一节点信息列表
     */
    private List<NextNodeInfo> nextNodes;

    /**
     * 流程是否结束
     */
    private Boolean processEnded;

    /**
     * 流程结束状态：APPROVED-已通过 REJECTED-已驳回 CANCELLED-已取消
     */
    private String processEndStatus;

    /**
     * 是否需要后续处理
     */
    private Boolean requireFollowUp;

    /**
     * 后续处理提示
     */
    private String followUpMessage;

    /**
     * 扩展信息
     */
    private Map<String, Object> extendedInfo;

    /**
     * 下一节点信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextNodeInfo {

        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 处理人ID列表
         */
        private List<Long> assigneeIds;

        /**
         * 处理人姓名列表
         */
        private List<String> assigneeNames;

        /**
         * 是否需要立即处理
         */
        private Boolean needImmediateAttention;

        /**
         * 预计处理时间（小时）
         */
        private Integer estimatedHours;
    }

    /**
     * 创建成功结果
     */
    public static QuickApprovalResult success(String taskId, String action) {
        return QuickApprovalResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .message("审批操作成功")
                .action(action)
                .handleTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static QuickApprovalResult fail(String message) {
        return QuickApprovalResult.builder()
                .success(false)
                .resultCode("FAILED")
                .message(message)
                .handleTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建待处理结果
     */
    public static QuickApprovalResult pending(String message) {
        return QuickApprovalResult.builder()
                .success(false)
                .resultCode("PENDING")
                .message(message)
                .handleTime(LocalDateTime.now())
                .build();
    }

    /**
     * 是否需要用户关注
     */
    public boolean requiresUserAttention() {
        return !success || requireFollowUp;
    }
}

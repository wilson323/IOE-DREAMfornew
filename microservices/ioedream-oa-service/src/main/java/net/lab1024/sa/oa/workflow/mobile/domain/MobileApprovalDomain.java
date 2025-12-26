package net.lab1024.sa.oa.workflow.mobile.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 移动端审批领域对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public class MobileApprovalDomain {

    /**
     * 待办统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingTaskStatistics {
        /**
         * 待处理总数
         */
        private Integer totalPending;

        /**
         * 紧急任务数
         */
        private Integer urgentCount;

        /**
         * 逾期任务数
         */
        private Integer overdueCount;

        /**
         * 今日到期数
         */
        private Integer dueTodayCount;

        /**
         * 本周到期数
         */
        private Integer dueThisWeekCount;

        /**
         * 已完成今日数
         */
        private Integer completedTodayCount;
    }

    /**
     * 我的申请VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyApplicationVO {
        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 业务Key
         */
        private String businessKey;

        /**
         * 当前状态
         * - running: 进行中
         * - completed: 已完成
         * - terminated: 已终止
         * - suspended: 已挂起
         */
        private String status;

        /**
         * 当前节点
         */
        private String currentNode;

        /**
         * 发起时间
         */
        private LocalDateTime createTime;

        /**
         * 完成时间
         */
        private LocalDateTime endTime;

        /**
         * 流程图片
         */
        private String processImage;
    }

    /**
     * 已办任务VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedTaskVO {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 审批结果
         * - approved: 同意
         * - rejected: 拒绝
         * - delegated: 转办
         * - withdrawn: 撤销
         */
        private String approvalResult;

        /**
         * 审批意见
         */
        private String comment;

        /**
         * 审批时间
         */
        private LocalDateTime approvalTime;

        /**
         * 处理时长（分钟）
         */
        private Long durationMinutes;
    }

    /**
     * 快速审批请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickApprovalRequest {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 审批结果
         * - approved: 同意
         * - rejected: 拒绝
         */
        private String decision;

        /**
         * 审批意见
         */
        private String comment;

        /**
         * 附件图片列表
         */
        private List<String> images;

        /**
         * 下一步节点
         *（可选，用于指定下一个审批人）
         */
        private String nextNode;

        /**
         * 下一步审批人
         */
        private Long nextAssignee;
    }

    /**
     * 快速审批结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickApprovalResult {
        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 审批结果
         */
        private String decision;

        /**
         * 消息
         */
        private String message;

        /**
         * 下一个任务信息
         */
        private NextTaskInfo nextTask;
    }

    /**
     * 下一个任务信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextTaskInfo {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 审批人姓名
         */
        private String assigneeName;

        /**
         * 是否结束
         */
        private Boolean processEnded;
    }

    /**
     * 批量审批请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchApprovalRequest {
        /**
         * 任务ID列表
         */
        private List<String> taskIds;

        /**
         * 审批结果
         * - approved: 同意
         * - rejected: 拒绝
         */
        private String decision;

        /**
         * 审批意见（统一）
         */
        private String comment;

        /**
         * 是否跳过验证错误
         */
        private Boolean skipErrors;
    }

    /**
     * 批量审批结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchApprovalResult {
        /**
         * 总数
         */
        private Integer total;

        /**
         * 成功数
         */
        private Integer successCount;

        /**
         * 失败数
         */
        private Integer failureCount;

        /**
         * 失败详情
         */
        private List<BatchApprovalFailure> failures;

        /**
         * 成功的任务ID列表
         */
        private List<String> successTaskIds;

        /**
         * 失败的任务ID列表
         */
        private List<String> failureTaskIds;
    }

    /**
     * 批量审批失败详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchApprovalFailure {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 错误消息
         */
        private String errorMessage;

        /**
         * 错误代码
         */
        private String errorCode;
    }

    /**
     * 审批决策
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalDecision {
        /**
         * 决策类型
         * - approved: 同意
         * - rejected: 拒绝
         * - unknown: 无法识别
         */
        private String decisionType;

        /**
         * 置信度
         * 0.0 - 1.0
         */
        private Double confidence;

        /**
         * 提取的审批意见
         */
        private String comment;

        /**
         * 关键词列表
         */
        private List<String> keywords;
    }

    /**
     * 语音审批请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceApprovalRequest {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 语音文本
         */
        private String voiceText;

        /**
         * 语音音频URL（备用）
         */
        private String audioUrl;

        /**
         * 语言（zh-CN, en-US）
         */
        private String language;
    }

    /**
     * 移动端审批详情VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobileApprovalDetailVO {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 流程实例ID
         */
        private String processInstanceId;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 发起人信息
         */
        private InitiatorInfo initiator;

        /**
         * 当前节点信息
         */
        private CurrentNodeInfo currentNode;

        /**
         * 表单数据
         */
        private Map<String, Object> formData;

        /**
         * 表单配置
         */
        private Map<String, Object> formConfig;

        /**
         * 流程图数据
         */
        private MobileProcessDiagram processDiagram;

        /**
         * 审批历史
         */
        private List<ApprovalHistoryItemVO> history;

        /**
         * 是否可以审批
         */
        private Boolean canApprove;

        /**
         * 是否可以拒绝
         */
        private Boolean canReject;

        /**
         * 是否可以转办
         */
        private Boolean canDelegate;

        /**
         * 是否可以撤回
         */
        private Boolean canWithdraw;

        /**
         * 操作按钮配置
         */
        private List<ActionButton> actionButtons;
    }

    /**
     * 发起人信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatorInfo {
        private Long userId;
        private String userName;
        private String avatar;
        private String department;
        private String position;
    }

    /**
     * 当前节点信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentNodeInfo {
        private String nodeId;
        private String nodeName;
        private LocalDateTime createTime;
        private LocalDateTime dueDate;
        private Boolean overdue;
    }

    /**
     * 审批历史项VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalHistoryItemVO {
        /**
         * 历史ID
         */
        private String historyId;

        /**
         * 任务名称
         */
        private String taskName;

        /**
         * 审批人姓名
         */
        private String approverName;

        /**
         * 审批人头像
         */
        private String approverAvatar;

        /**
         * 审批结果
         */
        private String approvalResult;

        /**
         * 审批意见
         */
        private String comment;

        /**
         * 附件列表
         */
        private List<String> attachments;

        /**
         * 审批时间
         */
        private LocalDateTime approvalTime;

        /**
         * 处理时长（分钟）
         */
        private Long durationMinutes;

        /**
         * 节点类型
         * - userTask: 用户任务
         * - serviceTask: 系统任务
         */
        private String nodeType;
    }

    /**
     * 移动端流程图
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobileProcessDiagram {
        /**
         * 流程图SVG
         */
        private String svg;

        /**
         * 当前节点位置
         */
        private NodePosition currentPosition;

        /**
         * 已完成节点ID列表
         */
        private List<String> completedNodes;

        /**
         * 当前节点ID列表
         */
        private List<String> currentNodes;

        /**
         * 待处理节点ID列表
         */
        private List<String> pendingNodes;
    }

    /**
     * 节点位置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodePosition {
        private String nodeId;
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }

    /**
     * 操作按钮
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionButton {
        /**
         * 按钮类型
         * - approve: 同意
         * - reject: 拒绝
         * - delegate: 转办
         * - withdraw: 撤回
         * - urge: 催办
         */
        private String actionType;

        /**
         * 按钮文本
         */
        private String label;

        /**
         * 按钮图标
         */
        private String icon;

        /**
         * 按钮颜色
         */
        private String color;

        /**
         * 是否显示
         */
        private Boolean visible;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    /**
     * 评论VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentVO {
        /**
         * 评论ID
         */
        private String commentId;

        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 评论人ID
         */
        private Long userId;

        /**
         * 评论人姓名
         */
        private String userName;

        /**
         * 评论人头像
         */
        private String avatar;

        /**
         * 评论内容
         */
        private String content;

        /**
         * 附件列表
         */
        private List<String> attachments;

        /**
         * 评论时间
         */
        private LocalDateTime createTime;

        /**
         * 评论类型
         * - comment: 评论
         * - system: 系统消息
         */
        private String commentType;
    }

    /**
     * 审批通知设置请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalNotificationRequest {
        /**
         * 是否启用推送通知
         */
        private Boolean pushEnabled;

        /**
         * 是否启用短信通知
         */
        private Boolean smsEnabled;

        /**
         * 是否启用邮件通知
         */
        private Boolean emailEnabled;

        /**
         * 推送时间段
         * 格式: 09:00-18:00
         */
        private String pushTimeRange;

        /**
         * 是否启用免打扰
         */
        private Boolean doNotDisturb;

        /**
         * 免打扰时间段
         */
        private String doNotDisturbTimeRange;
    }

    /**
     * 审批通知设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalNotificationSettings {
        /**
         * 是否启用推送通知
         */
        private Boolean pushEnabled;

        /**
         * 是否启用短信通知
         */
        private Boolean smsEnabled;

        /**
         * 是否启用邮件通知
         */
        private Boolean emailEnabled;

        /**
         * 推送时间段
         */
        private String pushTimeRange;

        /**
         * 是否启用免打扰
         */
        private Boolean doNotDisturb;

        /**
         * 免打扰时间段
         */
        private String doNotDisturbTimeRange;

        /**
         * 通知类型
         */
        private List<String> notificationTypes;
    }
}

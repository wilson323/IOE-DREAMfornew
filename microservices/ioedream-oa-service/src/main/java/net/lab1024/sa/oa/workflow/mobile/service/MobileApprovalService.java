package net.lab1024.sa.oa.workflow.mobile.service;

import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.oa.workflow.mobile.domain.*;

/**
 * 移动端审批服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface MobileApprovalService {

    // ==================== 待办管理 ====================

    /**
     * 获取待办任务列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param sortBy   排序字段（createTime, dueDate, priority）
     * @return 待办任务分页列表
     */
    PageResult<MobileTaskVO> getPendingTasks(Integer pageNum, Integer pageSize, String sortBy);

    /**
     * 获取待办任务统计
     *
     * @return 待办统计信息
     */
    PendingTaskStatistics getPendingCount();

    /**
     * 获取我的申请列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态筛选（可选）
     * @return 我的申请分页列表
     */
    PageResult<MyApplicationVO> getMyApplications(Integer pageNum, Integer pageSize, String status);

    /**
     * 获取已办任务列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 已办任务分页列表
     */
    PageResult<CompletedTaskVO> getCompletedTasks(Integer pageNum, Integer pageSize);

    // ==================== 快速审批 ====================

    /**
     * 快速审批（同意/拒绝）
     *
     * @param request 快速审批请求
     * @return 审批结果
     */
    QuickApprovalResult quickApprove(QuickApprovalRequest request);

    /**
     * 批量审批
     *
     * @param request 批量审批请求
     * @return 批量审批结果
     */
    BatchApprovalResult batchApprove(BatchApprovalRequest request);

    /**
     * 撤销审批
     *
     * @param taskId 任务ID
     * @param reason  撤销原因
     */
    void withdrawApproval(String taskId, String reason);

    /**
     * 转办任务
     *
     * @param taskId    任务ID
     * @param assigneeId 转办给谁
     * @param reason    转办原因
     */
    void delegateTask(String taskId, Long assigneeId, String reason);

    // ==================== 语音审批 ====================

    /**
     * 解析语音审批决策
     *
     * @param text 语音文本
     * @return 审批决策
     */
    ApprovalDecision parseApprovalDecision(String text);

    /**
     * 语音审批
     *
     * @param request 语音审批请求
     * @return 审批结果
     */
    QuickApprovalResult voiceApprove(VoiceApprovalRequest request);

    // ==================== 审批详情 ====================

    /**
     * 获取审批详情
     *
     * @param taskId 任务ID
     * @return 审批详情
     */
    MobileApprovalDetailVO getApprovalDetail(String taskId);

    /**
     * 获取审批历史
     *
     * @param taskId 任务ID
     * @return 审批历史列表
     */
    java.util.List<ApprovalHistoryItemVO> getApprovalHistory(String taskId);

    /**
     * 获取流程图
     *
     * @param taskId 任务ID
     * @return 流程图数据
     */
    MobileProcessDiagram getProcessDiagram(String taskId);

    /**
     * 获取表单数据
     *
     * @param taskId 任务ID
     * @return 表单数据
     */
    java.util.Map<String, Object> getFormData(String taskId);

    // ==================== 审批评论 ====================

    /**
     * 添加审批评论
     *
     * @param taskId 任务ID
     * @param comment 评论内容
     * @param images  附件图片列表（可选）
     */
    void addComment(String taskId, String comment, java.util.List<String> images);

    /**
     * 获取评论列表
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    java.util.List<CommentVO> getComments(String taskId);

    // ==================== 通知设置 ====================

    /**
     * 设置审批通知
     *
     * @param request 通知设置请求
     */
    void setApprovalNotification(ApprovalNotificationRequest request);

    /**
     * 获取通知设置
     *
     * @return 通知设置
     */
    ApprovalNotificationSettings getNotificationSettings();

    // ==================== 任务催办 ====================

    /**
     * 催办任务
     *
     * @param taskId 任务ID
     * @param message 催办消息
     */
    void urgeTask(String taskId, String message);

    /**
     * 批量催办
     *
     * @param taskIds 任务ID列表
     * @param message  催办消息
     */
    void batchUrge(java.util.List<String> taskIds, String message);
}

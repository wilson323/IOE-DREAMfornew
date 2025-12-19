package net.lab1024.sa.oa.workflow.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.WorkflowBatchOperationForm;
import net.lab1024.sa.oa.workflow.domain.vo.WorkflowBatchOperationResultVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 工作流批量操作服务
 * <p>
 * 提供批量任务处理、批量审批、批量流程管理等高级功能
 * 支持事务一致性、错误回滚、进度跟踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface WorkflowBatchOperationService {

    /**
     * 批量启动流程
     *
     * @param batchForm 批量启动配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchStartProcesses(WorkflowBatchOperationForm.BatchStartForm batchForm);

    /**
     * 批量完成任务
     *
     * @param batchForm 批量完成配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchCompleteTasks(WorkflowBatchOperationForm.BatchCompleteForm batchForm);

    /**
     * 批量审批
     *
     * @param batchForm 批量审批配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchApproveTasks(WorkflowBatchOperationForm.BatchApproveForm batchForm);

    /**
     * 批量拒绝
     *
     * @param batchForm 批量拒绝配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchRejectTasks(WorkflowBatchOperationForm.BatchRejectForm batchForm);

    /**
     * 批量分配任务
     *
     * @param batchForm 批量分配配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchAssignTasks(WorkflowBatchOperationForm.BatchAssignForm batchForm);

    /**
     * 批量撤销流程
     *
     * @param batchForm 批量撤销配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchCancelProcesses(WorkflowBatchOperationForm.BatchCancelForm batchForm);

    /**
     * 批量挂起流程
     *
     * @param batchForm 批量挂起配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchSuspendProcesses(WorkflowBatchOperationForm.BatchSuspendForm batchForm);

    /**
     * 批量激活流程
     *
     * @param batchForm 批量激活配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchActivateProcesses(WorkflowBatchOperationForm.BatchActivateForm batchForm);

    /**
     * 批量删除流程
     *
     * @param batchForm 批量删除配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchDeleteProcesses(WorkflowBatchOperationForm.BatchDeleteForm batchForm);

    /**
     * 批量设置流程变量
     *
     * @param batchForm 批量设置变量配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchSetProcessVariables(WorkflowBatchOperationForm.BatchSetVariablesForm batchForm);

    /**
     * 批量添加流程评论
     *
     * @param batchForm 批量添加评论配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchAddComments(WorkflowBatchOperationForm.BatchAddCommentsForm batchForm);

    /**
     * 批量添加流程附件
     *
     * @param batchForm 批量添加附件配置
     * @return 批量操作结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> batchAddAttachments(WorkflowBatchOperationForm.BatchAddAttachmentsForm batchForm);

    /**
     * 获取批量操作进度
     *
     * @param batchId 批量操作ID
     * @return 操作进度
     */
    ResponseDTO<WorkflowBatchOperationResultVO.BatchProgress> getBatchOperationProgress(String batchId);

    /**
     * 取消批量操作
     *
     * @param batchId 批量操作ID
     * @return 取消结果
     */
    ResponseDTO<Void> cancelBatchOperation(String batchId);

    /**
     * 重试失败的批量操作
     *
     * @param batchId 批量操作ID
     * @return 重试结果
     */
    CompletableFuture<ResponseDTO<WorkflowBatchOperationResultVO>> retryFailedBatchOperation(String batchId);

    /**
     * 获取批量操作历史
     *
     * @param queryForm 查询表单
     * @return 操作历史
     */
    ResponseDTO<List<WorkflowBatchOperationResultVO.BatchOperationHistory>> getBatchOperationHistory(WorkflowBatchOperationForm.BatchHistoryQueryForm queryForm);

    /**
     * 导出批量操作结果
     *
     * @param batchId 批量操作ID
     * @param format 导出格式
     * @return 导出结果
     */
    ResponseDTO<WorkflowBatchOperationResultVO.ExportResult> exportBatchOperationResult(String batchId, String format);
}

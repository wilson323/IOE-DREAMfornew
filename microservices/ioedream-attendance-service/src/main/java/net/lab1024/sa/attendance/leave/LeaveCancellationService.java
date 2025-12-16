package net.lab1024.sa.attendance.leave;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 销假服务接口
 * <p>
 * 定义请假销假的标准接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface LeaveCancellationService {

    /**
     * 申请销假
     *
     * @param request 销假申请请求
     * @return 销假申请结果
     */
    CompletableFuture<LeaveCancellationApplicationResult> applyLeaveCancellation(LeaveCancellationApplicationRequest request);

    /**
     * 审批销假申请
     *
     * @param approvalRequest 销假审批请求
     * @return 销假审批结果
     */
    CompletableFuture<LeaveCancellationApprovalResult> approveLeaveCancellation(LeaveCancellationApprovalRequest approvalRequest);

    /**
     * 驳回销假申请
     *
     * @param rejectionRequest 销假驳回请求
     * @return 销假驳回结果
     */
    CompletableFuture<LeaveCancellationRejectionResult> rejectLeaveCancellation(LeaveCancellationRejectionRequest rejectionRequest);

    /**
     * 撤销销假申请
     *
     * @param cancellationId 销假申请ID
     * @param reason 撤销原因
     * @param operatorId 操作人ID
     * @return 撤销结果
     */
    CompletableFuture<LeaveCancellationWithdrawalResult> withdrawLeaveCancellation(String cancellationId, String reason, Long operatorId);

    /**
     * 获取销假申请详情
     *
     * @param cancellationId 销假申请ID
     * @return 销假申请详情
     */
    CompletableFuture<LeaveCancellationDetail> getLeaveCancellationDetail(String cancellationId);

    /**
     * 获取员工销假申请列表
     *
     * @param employeeId 员工ID
     * @param queryParam 查询参数
     * @return 销假申请列表
     */
    CompletableFuture<LeaveCancellationListResult> getEmployeeLeaveCancellations(Long employeeId, LeaveCancellationQueryParam queryParam);

    /**
     * 获取待审批销假申请列表
     *
     * @param queryParam 查询参数
     * @return 待审批销假申请列表
     */
    CompletableFuture<LeaveCancellationListResult> getPendingLeaveCancellations(LeaveCancellationQueryParam queryParam);

    /**
     * 批量审批销假申请
     *
     * @param batchApprovalRequest 批量审批请求
     * @return 批量审批结果
     */
    CompletableFuture<BatchLeaveCancellationApprovalResult> batchApproveLeaveCancellations(BatchLeaveCancellationApprovalRequest batchApprovalRequest);

    /**
     * 获取销假统计数据
     *
     * @param statisticsRequest 统计请求
     * @return 销假统计结果
     */
    CompletableFuture<LeaveCancellationStatisticsResult> getLeaveCancellationStatistics(LeaveCancellationStatisticsRequest statisticsRequest);

    /**
     * 验证销假申请
     *
     * @param validationRequest 验证请求
     * @return 验证结果
     */
    CompletableFuture<LeaveCancellationValidationResult> validateLeaveCancellation(LeaveCancellationValidationRequest validationRequest);

    /**
     * 自动审批销假申请
     *
     * @param autoApprovalRequest 自动审批请求
     * @return 自动审批结果
     */
    CompletableFuture<AutoLeaveCancellationApprovalResult> autoApproveLeaveCancellations(AutoLeaveCancellationApprovalRequest autoApprovalRequest);

    /**
     * 获取销假影响分析
     *
     * @param impactAnalysisRequest 影响分析请求
     * @return 影响分析结果
     */
    CompletableFuture<LeaveCancellationImpactAnalysisResult> analyzeLeaveCancellationImpact(LeaveCancellationImpactAnalysisRequest impactAnalysisRequest);

    /**
     * 获取销假历史记录
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销假历史记录
     */
    CompletableFuture<List<LeaveCancellationHistoryRecord>> getLeaveCancellationHistory(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 导出销假申请数据
     *
     * @param exportRequest 导出请求
     * @return 导出结果
     */
    CompletableFuture<LeaveCancellationExportResult> exportLeaveCancellations(LeaveCancellationExportRequest exportRequest);
}
package net.lab1024.sa.visitor.service;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import net.lab1024.sa.common.response.ResponseDTO;
import net.lab1024.sa.common.response.PageResult;
import net.lab1024.sa.visitor.domain.form.ApprovalDecisionForm;
import net.lab1024.sa.visitor.domain.vo.ApprovalRecordVO;
import net.lab1024.sa.visitor.domain.vo.PendingApprovalVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 访客预约审批服务接口
 * <p>
 * 内存优化设计原则：
 * - 接口设计简洁，避免过度抽象
 * - 使用 CompletableFuture支持异步处理
 * - 熔断器配置，防止级联故障
 * - 合理的异常处理策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorApprovalService {

    /**
     * 审批预约
     * <p>
     * 核心业务方法，处理预约审批决策
     * 包含审批流程验证、状态更新、通知发送等
     * </p>
     *
     * @param appointmentId 预约ID
     * @param form 审批决策表单
     * @return 审批结果
     */
    @CircuitBreaker(name = "visitorApproval", fallbackMethod = "approveAppointmentFallback")
    @TimeLimiter(name = "visitorApproval")
    CompletableFuture<ResponseDTO<Void>> approveAppointment(
            Long appointmentId,
            ApprovalDecisionForm form
    );

    /**
     * 获取审批历史
     * <p>
     * 查询指定预约的完整审批记录
     * 按审批级别和时间排序显示
     * </p>
     *
     * @param appointmentId 预约ID
     * @return 审批记录列表
     */
    CompletableFuture<ResponseDTO<List<ApprovalRecordVO>>> getApprovalHistory(Long appointmentId);

    /**
     * 获取待审批列表
     * <p>
     * 查询指定审批人的待审批预约列表
     * 支持分页和条件筛选
     * </p>
     *
     * @param approverId 审批人ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 待审批预约列表
     */
    CompletableFuture<ResponseDTO<PageResult<PendingApprovalVO>>> getPendingApprovals(
            Long approverId,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 批量审批
     * <p>
     * 支持批量处理多个预约审批
     * 提高审批效率，减少重复操作
     * </p>
     *
     * @param appointmentIds 预约ID列表
     * @param form 批量审批决策表单
     * @return 批量审批结果
     */
    @CircuitBreaker(name = "batchApproval", fallbackMethod = "batchApproveFallback")
    CompletableFuture<ResponseDTO<List<Long>>> batchApproveAppointments(
            List<Long> appointmentIds,
            ApprovalDecisionForm form
    );

    /**
     * 检查审批权限
     * <p>
     * 验证用户是否有权限审批指定预约
     * 防止越权审批操作
     * </p>
     *
     * @param appointmentId 预约ID
     * @param approverId 审批人ID
     * @return 是否有审批权限
     */
    CompletableFuture<ResponseDTO<Boolean>> checkApprovalPermission(
            Long appointmentId,
            Long approverId
    );

    /**
     * 审批降级处理
     * <p>
     * 熔断器触发时的降级逻辑
     * 记录审批失败，保证系统可用性
     * </p>
     *
     * @param appointmentId 预约ID
     * @param form 审批决策表单
     * @param exception 异常信息
     * @return 降级处理结果
     */
    default CompletableFuture<ResponseDTO<Void>> approveAppointmentFallback(
            Long appointmentId,
            ApprovalDecisionForm form,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("APPROVAL_SERVICE_UNAVAILABLE", "审批服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 批量审批降级处理
     *
     * @param appointmentIds 预约ID列表
     * @param form 审批决策表单
     * @param exception 异常信息
     * @return 降级处理结果
     */
    default CompletableFuture<ResponseDTO<List<Long>>> batchApproveFallback(
            List<Long> appointmentIds,
            ApprovalDecisionForm form,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("BATCH_APPROVAL_SERVICE_UNAVAILABLE", "批量审批服务暂时不可用，请稍后重试")
        );
    }
}
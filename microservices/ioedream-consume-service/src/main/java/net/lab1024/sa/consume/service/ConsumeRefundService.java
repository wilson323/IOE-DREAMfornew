package net.lab1024.sa.consume.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeRefundAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeRefundQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRefundRecordVO;

/**
 * 退款服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeRefundService {

    /**
     * 分页查询退款记录
     *
     * @param queryForm 查询表单
     * @return 退款记录分页结果
     */
    PageResult<ConsumeRefundRecordVO> queryRefundRecords(ConsumeRefundQueryForm queryForm);

    /**
     * 获取退款记录详情
     *
     * @param refundId 退款ID
     * @return 退款记录详情
     */
    ConsumeRefundRecordVO getRefundRecordDetail(Long refundId);

    /**
     * 申请退款
     *
     * @param addForm 退款申请表单
     * @return 退款ID
     */
    Long applyRefund(ConsumeRefundAddForm addForm);

    /**
     * 审批退款
     *
     * @param refundId      退款ID
     * @param approved      是否批准
     * @param approveReason 审批原因
     */
    void approveRefund(Long refundId, Boolean approved, String approveReason);

    /**
     * 批量审批退款
     *
     * @param refundIds 退款ID列表
     * @param approved  是否批准
     * @param reason    审批原因
     * @return 批量操作结果
     */
    Map<String, Object> batchApproveRefunds(List<Long> refundIds, Boolean approved, String reason);

    /**
     * 获取待审批退款列表
     *
     * @return 待审批退款列表
     */
    List<ConsumeRefundRecordVO> getPendingRefunds();

    /**
     * 获取退款统计信息
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    Map<String, Object> getRefundStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取退款统计信息（支持String日期参数）
     *
     * @param userId    用户ID
     * @param startDate 开始日期（字符串格式）
     * @param endDate   结束日期（字符串格式）
     * @return 统计信息
     */
    Map<String, Object> getRefundStatistics(Long userId, String startDate, String endDate);

    /**
     * 执行退款
     *
     * @param refundId 退款ID
     * @return 是否成功
     */
    boolean executeRefund(Long refundId);

    /**
     * 取消退款申请
     *
     * @param refundId      退款ID
     * @param cancelReason  取消原因
     */
    void cancelRefund(Long refundId, String cancelReason);

    /**
     * 重新提交退款申请
     *
     * @param refundId 退款ID
     */
    void resubmitRefund(Long refundId);

    /**
     * 导出退款记录
     *
     * @param queryForm 查询表单
     * @return 导出文件URL
     */
    String exportRefundRecords(ConsumeRefundQueryForm queryForm);

    /**
     * 获取退款趋势
     *
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getRefundTrend(Integer days);

    /**
     * 根据消费记录ID查询退款
     *
     * @param recordId 消费记录ID
     * @return 退款记录列表
     */
    List<ConsumeRefundRecordVO> getRefundsByRecordId(Long recordId);

    /**
     * 取消退款申请（旧接口，保持兼容）
     *
     * @param refundId 退款ID
     * @deprecated 使用 {@link #cancelRefund(Long, String)} 替代
     */
    @Deprecated
    void cancelRefund(Long refundId);

    /**
     * 获取退款统计信息（旧接口，保持兼容）
     *
     * @param userId 用户ID
     * @return 统计信息
     * @deprecated 使用 {@link #getRefundStatistics(Long, LocalDateTime, LocalDateTime)} 替代
     */
    @Deprecated
    Map<String, Object> getRefundStatistics(Long userId);

    // ==================== 移动端API方法 ====================

    /**
     * 申请退款（移动端）
     *
     * @param addForm 退款申请表单
     * @return 退款申请结果
     */
    Map<String, Object> applyRefundForApp(ConsumeRefundAddForm addForm);

    /**
     * 申请退款（移动端 - 简化表单）
     *
     * @param applyForm 退款申请表单
     * @return 退款申请结果
     */
    Map<String, Object> applyRefundWithForm(net.lab1024.sa.consume.domain.form.ConsumeRefundApplyForm applyForm);

    /**
     * 获取用户退款记录（移动端）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 退款记录分页结果
     */
    PageResult<ConsumeRefundRecordVO> getRefundRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户退款记录（移动端 - 带状态筛选）
     *
     * @param userId       用户ID
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param refundStatus 退款状态（可选）
     * @return 退款记录分页结果
     */
    PageResult<ConsumeRefundRecordVO> getRefundRecords(Long userId, Integer pageNum, Integer pageSize, Integer refundStatus);

    /**
     * 获取退款状态
     *
     * @param refundId 退款ID
     * @return 退款状态信息
     */
    Map<String, Object> getRefundStatus(Long refundId);

    /**
     * 获取可申请退费的消费记录
     *
     * @param userId 用户ID
     * @return 可退款的消费记录列表
     */
    List<Map<String, Object>> getAvailableRefundRecords(Long userId);

    /**
     * 获取退款详情（移动端）
     *
     * @param refundId 退款ID
     * @return 退款详情
     */
    ConsumeRefundRecordVO getRefundDetail(Long refundId);
}

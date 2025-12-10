package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.RefundQueryForm;
import net.lab1024.sa.consume.domain.form.RefundRequestForm;
import net.lab1024.sa.consume.domain.vo.RefundRecordVO;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 消费退款服务接口
 * <p>
 * 提供完整的退款业务功能
 * 包括退款申请、审批、处理、查询等核心服务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
public interface ConsumeRefundService {

    /**
     * 申请退款
     *
     * @param refundRequest 退款申请表单
     * @return 退款ID
     */
    Long applyRefund(RefundRequestForm refundRequest);

    /**
     * 获取退款记录详情
     *
     * @param refundId 退款ID
     * @return 退款记录详情
     */
    RefundRecordVO getRefundDetail(Long refundId);

    /**
     * 分页查询退款记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<RefundRecordVO> getRefundPage(RefundQueryForm queryForm);

    /**
     * 批量申请退款
     *
     * @param transactionNos 交易号列表
     * @param reason 退款原因
     * @return 成功处理数量
     */
    int batchApplyRefund(List<String> transactionNos, String reason);

    /**
     * 批量申请退款（带金额）
     *
     * @param refundRequests 退款申请列表
     * @return 成功处理数量
     */
    int batchApplyRefundWithAmount(List<Map<String, Object>> refundRequests);

    /**
     * 审批退款申请
     *
     * @param refundId 退款ID
     * @param approved 审批结果
     * @param comment 审批意见
     * @return 是否成功
     */
    boolean approveRefund(Long refundId, Boolean approved, String comment);

    /**
     * 批量审批退款申请
     *
     * @param refundIds 退款ID列表
     * @param approved 审批结果
     * @param comment 审批意见
     * @return 成功处理数量
     */
    int batchApproveRefund(List<Long> refundIds, Boolean approved, String comment);

    /**
     * 取消退款申请
     *
     * @param refundId 退款ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelRefund(Long refundId, String reason);

    /**
     * 处理退款（执行退款）
     *
     * @param refundId 退款ID
     * @return 是否成功
     */
    boolean processRefund(Long refundId);

    /**
     * 获取退款统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getRefundStatistics();

    /**
     * 获取用户退款统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getUserRefundStatistics(Long userId);

    /**
     * 获取退款趋势数据
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param statisticsType 统计类型
     * @return 趋势数据
     */
    Map<String, Object> getRefundTrend(String startDate, String endDate, String statisticsType);

    /**
     * 导出退款数据
     *
     * @param queryForm 查询表单
     * @param response HTTP响应
     */
    void exportRefundData(RefundQueryForm queryForm, HttpServletResponse response);

    /**
     * 检查退款状态
     *
     * @param refundId 退款ID
     * @return 状态信息
     */
    Map<String, Object> checkRefundStatus(Long refundId);

    /**
     * 获取可退款金额
     *
     * @param transactionNo 交易号
     * @return 可退款金额信息
     */
    Map<String, Object> getAvailableRefundAmount(String transactionNo);

    /**
     * 根据交易号查询退款记录
     *
     * @param transactionNo 交易号
     * @return 退款记录列表
     */
    List<RefundRecordVO> getRefundByTransactionNo(String transactionNo);

    /**
     * 验证退款申请
     *
     * @param refundRequest 退款申请表单
     * @return 验证结果
     */
    Map<String, Object> validateRefundRequest(RefundRequestForm refundRequest);

    /**
     * 获取退款政策信息
     *
     * @return 政策信息
     */
    Map<String, Object> getRefundPolicy();
}
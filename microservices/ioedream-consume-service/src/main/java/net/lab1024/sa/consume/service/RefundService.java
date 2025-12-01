package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.consume.domain.dto.RefundQueryDTO;
import net.lab1024.sa.consume.domain.dto.RefundRequestDTO;
import net.lab1024.sa.consume.domain.dto.RefundResultDTO;
import net.lab1024.sa.consume.domain.entity.RefundRecordEntity;

/**
 * 退款服务接口
 * 负责处理退款申请、审核、执行等流程
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface RefundService {

    /**
     * 创建退款申请
     *
     * @param refundRequest 退款申请
     * @return 退款结果
     */
    ResponseDTO<RefundResultDTO> createRefund(RefundRequestDTO refundRequest);

    /**
     * 审核退款申请
     *
     * @param refundId    退款记录ID
     * @param approved    是否通过
     * @param auditRemark 审核备注
     * @return 审核结果
     */
    ResponseDTO<String> auditRefund(Long refundId, boolean approved, String auditRemark);

    /**
     * 查询退款记录
     *
     * @param queryDTO 查询条件
     * @return 退款记录分页
     */
    ResponseDTO<Page<RefundRecordEntity>> queryRefundRecords(RefundQueryDTO queryDTO);

    /**
     * 获取退款统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getRefundStatistics(Long userId, LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * 批量审核退款
     *
     * @param refundIds   退款记录ID列表
     * @param approved    是否通过
     * @param auditRemark 审核备注
     * @return 批量审核结果
     */
    ResponseDTO<Map<String, Object>> batchAuditRefund(List<Long> refundIds, boolean approved,
            String auditRemark);

    /**
     * 获取退款详情
     *
     * @param refundId 退款记录ID
     * @return 退款详情
     */
    ResponseDTO<RefundRecordEntity> getRefundDetail(Long refundId);

    /**
     * 根据退款单号获取退款记录
     *
     * @param refundNo 退款单号（refundId字段）
     * @return 退款记录
     */
    RefundRecordEntity getRefundByNo(String refundNo);

    /**
     * 取消退款申请
     *
     * @param refundNo 退款单号
     * @return 取消结果
     */
    ResponseDTO<String> cancelRefund(String refundNo);

    /**
     * 重新提交退款申请
     *
     * @param refundNo       原退款单号
     * @param refundRequest  退款申请
     * @return 退款结果
     */
    ResponseDTO<RefundResultDTO> resubmitRefund(String refundNo, RefundRequestDTO refundRequest);

    /**
     * 获取退款状态统计
     *
     * @return 状态统计数据
     */
    ResponseDTO<Map<String, Object>> getRefundStatusStatistics();
}

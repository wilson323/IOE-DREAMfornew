package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.lab1024.sa.consume.domain.dto.RefundQueryDTO;
import net.lab1024.sa.consume.domain.entity.RefundRecordEntity;

/**
 * 退款管理器
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface RefundManager {

    /**
     * 退款用户余额
     *
     * @param userId     用户ID
     * @param amount     退款金额
     * @param refundNo   退款单号
     */
    void refundUserBalance(Long userId, BigDecimal amount, String refundNo);

    /**
     * 查询退款记录
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 退款记录分页
     */
    Page<RefundRecordEntity> queryRefundRecords(RefundQueryDTO queryDTO, Pageable pageable);

    /**
     * 获取退款统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    Map<String, Object> getRefundStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
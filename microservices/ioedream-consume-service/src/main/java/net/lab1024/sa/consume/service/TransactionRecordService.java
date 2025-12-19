package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
public interface TransactionRecordService {

    /**
     * 记录交易
     * @param accountId 账户ID
     * @param amount 金额
     * @param transactionType 交易类型
     * @param description 描述
     * @return 是否成功
     */
    Boolean recordTransaction(Long accountId, BigDecimal amount, String transactionType, String description);

    /**
     * 查询交易记录
     * @param accountId 账户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易记录列表
     */
    Object queryTransactions(Long accountId, LocalDateTime startTime, LocalDateTime endTime);
}

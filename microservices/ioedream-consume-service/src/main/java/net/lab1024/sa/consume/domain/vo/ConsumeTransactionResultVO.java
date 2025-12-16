package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 消费交易结果VO
 * <p>
 * 用于返回消费交易结果
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeTransactionResultVO {

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 交易状态
     * <p>
     * 1-待处理
     * 2-成功
     * 3-失败
     * 4-已退款
     * </p>
     */
    private Integer transactionStatus;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;
}




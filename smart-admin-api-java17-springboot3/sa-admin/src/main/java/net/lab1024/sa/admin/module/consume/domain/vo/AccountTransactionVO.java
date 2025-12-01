package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户交易记录视图对象
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
public class AccountTransactionVO {

    /**
     * 交易ID
     */
    private Long transactionId;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 交易状态
     */
    private String status;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 备注
     */
    private String remark;
}


package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 账户详情视图对象
 *
 * @author OpenSpec Task 2.10 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@Data
public class AccountDetailVO {

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID（人员ID）
     */
    private Long userId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户类型
     */
    private String accountType;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 账户状态（0-正常，1-冻结，2-关闭）
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}


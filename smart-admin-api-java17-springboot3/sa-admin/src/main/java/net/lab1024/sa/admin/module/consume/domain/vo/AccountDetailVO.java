package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户详情VO
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的展示字段
 * - 包含格式化后的显示信息
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@Accessors(chain = true)
public class AccountDetailVO {

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户类型：1-个人账户 2-企业账户
     */
    private Integer accountType;

    /**
     * 账户类型描述
     */
    private String accountTypeText;

    /**
     * 账户状态：1-正常 2-冻结 3-注销
     */
    private Integer status;

    /**
     * 账户状态描述
     */
    private String statusText;

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
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 总消费金额
     */
    private BigDecimal totalConsumeAmount;

    /**
     * 总充值金额
     */
    private BigDecimal totalRechargeAmount;

    /**
     * 最后消费时间
     */
    private LocalDateTime lastConsumeTime;

    /**
     * 最后充值时间
     */
    private LocalDateTime lastRechargeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取可用余额
     *
     * @return 可用余额
     */
    public BigDecimal getAvailableBalance() {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (frozenAmount == null) {
            frozenAmount = BigDecimal.ZERO;
        }
        return balance.subtract(frozenAmount);
    }
}
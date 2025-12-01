package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户VO
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
public class AccountVO {

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取状态显示样式
     *
     * @return 状态样式
     */
    public String getStatusStyle() {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 1:
                return "success"; // 绿色
            case 2:
                return "warning"; // 橙色
            case 3:
                return "danger";  // 红色
            default:
                return "";
        }
    }

    /**
     * 获取格式化的余额显示
     *
     * @return 格式化余额
     */
    public String getFormattedBalance() {
        if (balance == null) {
            return "0.00";
        }
        return balance.toString();
    }

    /**
     * 获取格式化的可用余额
     *
     * @return 格式化可用余额
     */
    public String getFormattedAvailableBalance() {
        BigDecimal available = getAvailableBalance();
        return available.toString();
    }

    /**
     * 计算可用余额
     *
     * @return 可用余额
     */
    private BigDecimal getAvailableBalance() {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (frozenAmount == null) {
            frozenAmount = BigDecimal.ZERO;
        }
        return balance.subtract(frozenAmount);
    }
}
package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 充值类型枚举
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Getter
@AllArgsConstructor
public enum RechargeTypeEnum {

    /**
     * 现金充值
     */
    CASH(1, "现金充值"),

    /**
     * 银行卡充值
     */
    BANK_CARD(2, "银行卡充值"),

    /**
     * 微信充值
     */
    WECHAT(3, "微信充值"),

    /**
     * 支付宝充值
     */
    ALIPAY(4, "支付宝充值"),

    /**
     * 系统充值
     */
    SYSTEM(5, "系统充值"),

    /**
     * 补贴充值
     */
    SUBSIDY(6, "补贴充值");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;
}
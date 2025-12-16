package net.lab1024.sa.consume.service.integration.payment;

/**
 * 支付方式枚举
 * 定义系统支持的所有支付方式
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public enum PaymentMethod {

    /**
     * 余额支付
     */
    BALANCE("余额支付", "BALANCE", true),

    /**
     * 微信支付
     */
    WECHAT("微信支付", "WECHAT", true),

    /**
     * 支付宝
     */
    ALIPAY("支付宝", "ALIPAY", true),

    /**
     * 银行卡支付
     */
    BANK_CARD("银行卡", "BANK_CARD", true),

    /**
     * 现金支付
     */
    CASH("现金", "CASH", false),

    /**
     * Apple Pay
     */
    APPLE_PAY("Apple Pay", "APPLE_PAY", true),

    /**
     * 银联支付
     */
    UNIONPAY("银联", "UNIONPAY", true);

    private final String displayName;
    private final String code;
    private final boolean online;

    PaymentMethod(String displayName, String code, boolean online) {
        this.displayName = displayName;
        this.code = code;
        this.online = online;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public boolean isOnline() {
        return online;
    }

    /**
     * 根据代码获取支付方式
     *
     * @param code 支付方式代码
     * @return 支付方式
     */
    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("不支持的支付方式: " + code);
    }

    /**
     * 获取所有在线支付方式
     *
     * @return 在线支付方式数组
     */
    public static PaymentMethod[] getOnlineMethods() {
        return java.util.Arrays.stream(values())
                .filter(PaymentMethod::isOnline)
                .toArray(PaymentMethod[]::new);
    }
}
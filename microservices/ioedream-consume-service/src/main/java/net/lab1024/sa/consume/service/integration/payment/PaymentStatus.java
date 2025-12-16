package net.lab1024.sa.consume.service.integration.payment;

/**
 * 支付状态枚举
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public enum PaymentStatus {

    /**
     * 处理中
     */
    PROCESSING("处理中", "PROCESSING"),

    /**
     * 成功
     */
    SUCCESS("成功", "SUCCESS"),

    /**
     * 失败
     */
    FAILED("失败", "FAILED"),

    /**
     * 已取消
     */
    CANCELLED("已取消", "CANCELLED"),

    /**
     * 超时
     */
    TIMEOUT("超时", "TIMEOUT"),

    /**
     * 未知
     */
    UNKNOWN("未知", "UNKNOWN"),

    /**
     * 未找到
     */
    NOT_FOUND("未找到", "NOT_FOUND");

    private final String displayName;
    private final String code;

    PaymentStatus(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据代码获取支付状态
     *
     * @param code 状态代码
     * @return 支付状态
     */
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("不支持的支付状态: " + code);
    }

    /**
     * 是否为最终状态
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * 是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 是否为失败状态
     */
    public boolean isFailed() {
        return this == FAILED || this == TIMEOUT;
    }
}
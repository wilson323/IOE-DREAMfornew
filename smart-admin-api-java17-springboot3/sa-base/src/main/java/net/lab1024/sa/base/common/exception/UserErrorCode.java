/*
 * 用户相关错误码
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.base.common.exception;

/**
 * 用户相关错误码
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
public enum UserErrorCode {

    USER_NOT_FOUND("USER_001", "用户不存在"),
    USER_ACCOUNT_LOCKED("USER_002", "用户账户已锁定"),
    USER_PASSWORD_EXPIRED("USER_003", "用户密码已过期"),
    USER_INVALID_PASSWORD("USER_004", "用户密码错误"),
    USER_DISABLED("USER_005", "用户账户已禁用"),

    USER_NOT_AUTHORIZED("USER_006", "用户未授权"),
    USER_PERMISSION_DENIED("USER_007", "用户权限不足"),
    USER_LOGIN_EXPIRED("USER_008", "用户登录已过期"),

    USER_DATA_VALIDATION_ERROR("USER_009", "用户数据验证失败"),
    USER_DUPLICATE_DATA("USER_010", "用户数据重复"),

    USER_CONSUME_ACCOUNT_NOT_FOUND("USER_011", "用户消费账户不存在"),
    USER_CONSUME_ACCOUNT_FROZEN("USER_012", "用户消费账户已冻结"),
    USER_CONSUME_INSUFFICIENT_BALANCE("USER_013", "用户余额不足"),

    USER_PAYMENT_PASSWORD_NOT_SET("USER_014", "用户未设置支付密码"),
    USER_PAYMENT_PASSWORD_WRONG("USER_015", "用户支付密码错误"),
    USER_PAYMENT_PASSWORD_LOCKED("USER_016", "用户支付密码已锁定"),

    USER_DEVICE_NOT_TRUSTED("USER_017", "设备不受信任"),
    USER_LOCATION_ABNORMAL("USER_018", "用户位置异常"),
    USER_BEHAVIOR_ABNORMAL("USER_019", "用户行为异常"),

    ACCOUNT_SECURITY_RISK_HIGH("USER_020", "账户安全风险高"),
    ACCOUNT_SECURITY_SUSPICIOUS("USER_021", "账户活动可疑");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getFullMessage() {
        return String.format("[%s] %s", code, message);
    }
}
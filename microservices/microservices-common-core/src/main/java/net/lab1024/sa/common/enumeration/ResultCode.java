package net.lab1024.sa.common.enumeration;

/**
 * 响应结果码枚举
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
public enum ResultCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),

    VISITOR_NOT_FOUND(2001, "访客不存在"),
    VISITOR_ALREADY_EXISTS(2002, "访客已存在"),
    APPOINTMENT_NOT_FOUND(2003, "预约不存在"),

    PERMISSION_DENIED(3001, "权限不足"),
    ROLE_NOT_FOUND(3002, "角色不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

package net.lab1024.sa.common.dto;

/**
 * 统一响应结果封装类
 * <p>
 * 提供统一的API响应格式，包含状态码、消息、数据和时间戳
 * 注意：手动生成getter/setter，避免依赖Lombok
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 * @updated 2025-12-20 企业级重构，基于新版Flowable工作流实现
 */
public class ResponseDTO<T> {

    /**
     * 业务状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    // 默认构造函数
    public ResponseDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    // 全参数构造函数
    public ResponseDTO(Integer code, String message, T data, Long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // Getter和Setter方法
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseDTO<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseDTO<T> ok(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ResponseDTO<T> ok(String message, T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 成功响应（仅消息）
     */
    public static <T> ResponseDTO<T> okMsg(String message) {
        return ok(message, null);
    }

    /**
     * 错误响应（默认错误码）
     */
    public static <T> ResponseDTO<T> error(String message) {
        return error(500, message);
    }

    /**
     * 错误响应（自定义错误码）
     */
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(code);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ResponseDTO<T> error(String code, String message) {
        return error(resolveCode(code), message);
    }

    public static <T> ResponseDTO<T> error(String code, String message, T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(resolveCode(code));
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 错误响应（自定义错误码和数据）
     */
    public static <T> ResponseDTO<T> error(Integer code, String message, T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    /**
     * 用户错误响应
     */
    public static <T> ResponseDTO<T> userError(String message) {
        return error(400, message);
    }

    /**
     * 用户错误响应（带数据）
     */
    public static <T> ResponseDTO<T> userError(String message, T data) {
        return error(400, message, data);
    }

    /**
     * 参数错误响应
     */
    public static <T> ResponseDTO<T> paramError(String message) {
        return error(400, message);
    }

    /**
     * 参数错误响应（带数据）
     */
    public static <T> ResponseDTO<T> paramError(String message, T data) {
        return error(400, message, data);
    }

    /**
     * 用户参数错误响应（兼容别名）
     */
    public static <T> ResponseDTO<T> userErrorParam(String message) {
        return paramError(message);
    }

    /**
     * 系统错误响应
     */
    public static <T> ResponseDTO<T> systemError(String message) {
        return error(500, message);
    }

    /**
     * 系统错误响应（带数据）
     */
    public static <T> ResponseDTO<T> systemError(String message, T data) {
        return error(500, message, data);
    }

    /**
     * 业务错误响应
     */
    public static <T> ResponseDTO<T> businessError(String code, String message) {
        return error(Integer.parseInt(code), message);
    }

    /**
     * 业务错误响应（带数据）
     */
    public static <T> ResponseDTO<T> businessError(String code, String message, T data) {
        return error(Integer.parseInt(code), message, data);
    }

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 判断是否为错误响应
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 判断是否为用户错误
     */
    public boolean isUserError() {
        return code != null && code >= 400 && code < 500;
    }

    /**
     * 判断是否为系统错误
     */
    public boolean isSystemError() {
        return code != null && code >= 500;
    }

    /**
     * equals方法：比较code、message和data
     * 注意：不比较timestamp，因为每次创建都会生成新时间戳
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ResponseDTO<?> other = (ResponseDTO<?>) obj;
        if (code == null) {
            if (other.code != null) return false;
        } else if (!code.equals(other.code)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) return false;
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (data == null) {
            if (other.data != null) return false;
        } else if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }

    /**
     * hashCode方法：基于code、message和data计算
     * 注意：不包含timestamp
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    private static Integer resolveCode(String code) {
        if (code == null || code.isBlank()) {
            return 500;
        }
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException ignore) {
        }
        String upper = code.trim().toUpperCase();
        if ("UNAUTHORIZED".equals(upper)) {
            return 401;
        }
        if ("FORBIDDEN".equals(upper)) {
            return 403;
        }
        if ("NOT_FOUND".equals(upper)) {
            return 404;
        }
        if (upper.contains("PARAM") || upper.contains("INVALID") || upper.contains("ARG")) {
            return 400;
        }
        if (upper.contains("BUSINESS") || upper.contains("USER")) {
            return 400;
        }
        return 500;
    }
}


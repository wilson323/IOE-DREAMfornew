package net.lab1024.sa.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应DTO - 重构版本
 * <p>
 * 解决原有ResponseDTO的依赖混乱问题，提供简洁统一的响应格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
@Data
public class ResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
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
    @JsonProperty("timestamp")
    private Long timestamp;

    public ResponseDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResponseDTO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static <T> ResponseDTO<T> ok() {
        return new ResponseDTO<>(200, "操作成功", null);
    }

    /**
     * 成功响应 - 带数据
     */
    public static <T> ResponseDTO<T> ok(T data) {
        return new ResponseDTO<>(200, "操作成功", data);
    }

    /**
     * 成功响应 - 自定义消息
     */
    public static <T> ResponseDTO<T> ok(String message, T data) {
        return new ResponseDTO<>(200, message, data);
    }

    /**
     * 错误响应
     */
    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(500, message, null);
    }

    /**
     * 错误响应 - 带错误码
     */
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        return new ResponseDTO<>(code, message, null);
    }

    /**
     * 用户参数错误
     */
    public static <T> ResponseDTO<T> userError(String message) {
        return new ResponseDTO<>(400, message, null);
    }

    /**
     * 业务错误
     */
    public static <T> ResponseDTO<T> businessError(String message) {
        return new ResponseDTO<>(500, message, null);
    }

    /**
     * 系统错误
     */
    public static <T> ResponseDTO<T> systemError(String message) {
        return new ResponseDTO<>(500, "系统繁忙: " + message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
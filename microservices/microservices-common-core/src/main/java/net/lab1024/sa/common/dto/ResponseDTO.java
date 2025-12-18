package net.lab1024.sa.common.dto;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应DTO
 * <p>
 * 所有API接口的统一响应格式
 * 严格遵循CLAUDE.md规范：
 * - 统一的响应结构
 * - 标准化的错误码
 * - 完整的响应信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ResponseDTO.class);
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * <p>
     * 200-成功
     * 400-499 客户端错误
     * 500-599 服务端错误
     * 1000+ 业务错误码
     * </p>
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 判断是否为成功响应
     *
     * @return 是否成功
     */
    public boolean getOk() {
        return code != null && code == 200;
    }

    /**
     * 成功响应（无数据）
     *
     * @return 成功响应
     */
    public static <T> ResponseDTO<T> ok() {
        return ResponseDTO.<T>builder()
                .code(200)
                .message("操作成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（带数据）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求：
     * - message统一为"success"
     * </p>
     *
     * @param data 响应数据
     * @return 成功响应
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
     *
     * @param message 消息
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> ResponseDTO<T> ok(String message, T data) {
        return ResponseDTO.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 错误响应（整数错误码）
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ResponseDTO<T> error(Integer code, String message) {
        return ResponseDTO.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 错误响应（字符串错误码）
     * <p>
     * 优先尝试将字符串错误码转换为整数，失败则使用hashCode生成
     * </p>
     *
     * @param code 错误码（字符串）
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ResponseDTO<T> error(String code, String message) {
        try {
            // 优先尝试将字符串错误码转换为整数
            Integer errorCode = Integer.parseInt(code);
            return error(errorCode, message);
        } catch (NumberFormatException e) {
            // 如果无法解析为整数，使用hashCode生成错误码
            // 确保错误码在40000-139999范围内，避免与HTTP状态码冲突
            log.debug("[响应DTO] 错误码字符串解析失败，使用hashCode生成: code={}, error={}", code, e.getMessage());
            int errorCode = Math.abs(code.hashCode() % 100000) + 40000;
            return ResponseDTO.<T>builder()
                    .code(errorCode)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 错误响应（字符串错误码 + 数据）
     *
     * @param code 错误码（字符串）
     * @param message 错误消息
     * @param data 响应数据
     * @return 错误响应
     */
    public static <T> ResponseDTO<T> error(String code, String message, T data) {
        try {
            Integer errorCode = Integer.parseInt(code);
            return ResponseDTO.<T>builder()
                    .code(errorCode)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (NumberFormatException e) {
            log.debug("[响应DTO] 错误码字符串解析失败，使用hashCode生成: code={}, error={}", code, e.getMessage());
            int errorCode = Math.abs(code.hashCode() % 100000) + 40000;
            return ResponseDTO.<T>builder()
                    .code(errorCode)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 错误响应（仅消息，默认错误码500）
     * <p>
     * 便捷方法，用于快速返回错误响应
     * 使用默认错误码500（服务器内部错误）
        return response;
    }

    /**
     * 参数错误响应
     * <p>
     * 用于参数验证失败的情况
     * 错误码：400（客户端错误）
     * </p>
     *
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ResponseDTO<T> errorParam(String message) {
        return ResponseDTO.<T>builder()
                .code(400)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 资源未找到错误响应
     * <p>
     * 用于资源不存在的情况
     * 错误码：404（资源未找到）
     * </p>
     *
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ResponseDTO<T> errorNotFound(String message) {
        return ResponseDTO.<T>builder()
                .code(404)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（仅消息，无数据）
     * <p>
     * 用于操作成功但无需返回数据的场景
     * </p>
     *
     * @param message 成功消息
     * @return 成功响应
     */
    public static <T> ResponseDTO<T> okMsg(String message) {
        return ResponseDTO.<T>builder()
                .code(200)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 用户参数错误响应
     * <p>
     * 用于用户相关参数验证失败的情况
     * 错误码：400（客户端错误）
     * </p>
     *
     * @param message 错误消息
     * @return 错误响应
     */
    public static <T> ResponseDTO<T> userErrorParam(String message) {
        return ResponseDTO.<T>builder()
                .code(400)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 判断响应是否成功
     * <p>
     * 状态码为200表示成功
     * </p>
     *
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}

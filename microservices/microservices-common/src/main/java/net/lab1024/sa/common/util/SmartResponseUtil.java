package net.lab1024.sa.common.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import com.alibaba.fastjson2.JSON;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 智能响应工具类 - 企业级标准
 * 提供统一的HTTP响应处理和文件下载功能
 *
 * 功能特性：
 * 1. 统一JSON响应格式
 * 2. 文件下载支持
 * 3. 编码处理优化
 * 4. 异常处理增强
 * 5. 支持链路追踪
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-30
 */
@Slf4j
public class SmartResponseUtil {

    private static final String UTF_8 = "UTF-8";

    /**
     * 写入JSON响应
     *
     * @param response    HTTP响应对象
     * @param responseDTO 响应数据
     */
    public static void write(HttpServletResponse response, ResponseDTO<?> responseDTO) {
        if (response == null || responseDTO == null) {
            log.error("SmartResponseUtil.write: response或responseDTO为null");
            return;
        }

        try {
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(UTF_8);

            // 添加CORS头（如果需要）
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // 写入响应数据
            String jsonStr = JSON.toJSONString(responseDTO);
            response.getWriter().write(jsonStr);
            response.flushBuffer();

            log.debug("响应写入成功: {}", jsonStr);
        } catch (IOException ex) {
            log.error("响应写入失败: {}", ex.getMessage(), ex);
            throw new RuntimeException("响应写入失败", ex);
        }
    }

    /**
     * 设置文件下载响应头（无文件大小）
     *
     * @param response HTTP响应对象
     * @param fileName 文件名
     */
    public static void setDownloadFileHeader(HttpServletResponse response, String fileName) {
        setDownloadFileHeader(response, fileName, null);
    }

    /**
     * 设置文件下载响应头（带文件大小）
     *
     * @param response HTTP响应对象
     * @param fileName 文件名
     * @param fileSize 文件大小（字节）
     */
    public static void setDownloadFileHeader(HttpServletResponse response, String fileName, Long fileSize) {
        if (response == null) {
            log.error("SmartResponseUtil.setDownloadFileHeader: response为null");
            return;
        }

        try {
            response.setCharacterEncoding(UTF_8);

            // 设置文件大小
            if (fileSize != null && fileSize > 0) {
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
            }

            // 设置文件名和内容类型
            if (SmartStringUtil.isNotEmpty(fileName)) {
                // 获取媒体类型
                MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM);

                response.setHeader(HttpHeaders.CONTENT_TYPE, mediaType.toString() + ";charset=utf-8");

                // URL编码文件名
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                        .replaceAll("\\+", "%20");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=\"" + encodedFileName + "\"");
                response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        HttpHeaders.CONTENT_DISPOSITION);

                log.debug("文件下载响应头设置完成: fileName={}, fileSize={}", fileName, fileSize);
            }
        } catch (Exception ex) {
            log.error("设置文件下载响应头失败: {}", ex.getMessage(), ex);
            throw new RuntimeException("设置文件下载响应头失败", ex);
        }
    }

    // ==================== 快速响应创建方法 ====================

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> success(T data) {
        return ResponseDTO.ok(data);
    }

    /**
     * 成功响应（带消息）
     *
     * @param message 成功消息
     * @return 响应DTO
     */
    public static ResponseDTO<String> success(String message) {
        return ResponseDTO.ok(null, message);
    }

    /**
     * 成功响应（无数据）
     *
     * @return 响应DTO
     */
    public static ResponseDTO<Void> success() {
        return ResponseDTO.ok();
    }

    /**
     * 成功响应（带数据和消息）
     *
     * @param data    响应数据
     * @param message 成功消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> success(T data, String message) {
        return ResponseDTO.ok(data, message);
    }

    /**
     * 成功响应（布尔值）
     *
     * @param data 布尔值
     * @return 响应DTO
     */
    public static ResponseDTO<Boolean> success(boolean data) {
        return ResponseDTO.ok(data);
    }

    /**
     * 成功响应快捷方法（等同于 success(data)）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> ok(T data) {
        return ResponseDTO.ok(data);
    }

    /**
     * 成功响应快捷方法（无数据）
     *
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> ok() {
        return ResponseDTO.ok();
    }

    /**
     * 错误响应（泛型版本）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    @SuppressWarnings("unchecked")
    public static <T> ResponseDTO<T> error(String message) {
        // 使用显式类型转换确保泛型类型正确
        return (ResponseDTO<T>) ResponseDTO.error(message);
    }

    /**
     * 错误响应（带状态码，泛型版本）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> error(int code, String message) {
        return ResponseDTO.error(message);
    }

    /**
     * 用户认证错误（泛型版本）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> userError(String message) {
        return ResponseDTO.userErrorParam(message);
    }

    /**
     * 权限错误（泛型版本）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> permissionError(String message) {
        return ResponseDTO.error(message);
    }

    /**
     * 参数错误（泛型版本）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> paramError(String message) {
        return ResponseDTO.userErrorParam(message);
    }

    /**
     * 用户密码错误（泛型版本）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应DTO
     */
    public static <T> ResponseDTO<T> userPasswordError(String message) {
        return ResponseDTO.error(message);
    }

    // ==================== JSON处理工具方法 ====================

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return JSON.toJSONString(obj);
        } catch (Exception ex) {
            log.error("对象转JSON失败: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 从JSON字符串解析对象
     *
     * @param jsonStr JSON字符串
     * @param clazz   目标类型
     * @param <T>     类型
     * @return 解析后的对象
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        if (SmartStringUtil.isEmpty(jsonStr) || clazz == null) {
            return null;
        }
        try {
            return JSON.parseObject(jsonStr, clazz);
        } catch (Exception ex) {
            log.error("JSON转对象失败: {}", ex.getMessage(), ex);
            return null;
        }
    }
}

package net.lab1024.sa.base.common.util;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static cn.hutool.core.util.CharsetUtil.UTF_8;

/**
 * 响应工具类
 *
 * @Author 1024创新实验室-创始人:卓大
 * @Date 2023/11/25 18:51:32
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>，since 2012
 */

@Slf4j
public class SmartResponseUtil {

    public static void write(HttpServletResponse response, ResponseDTO<?> responseDTO) {
        // 重置response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8);

        try {
            response.getWriter().write(JSON.toJSONString(responseDTO));
            response.flushBuffer();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public static void setDownloadFileHeader(HttpServletResponse response, String fileName) {
        setDownloadFileHeader(response, fileName, null);
    }

    public static void setDownloadFileHeader(HttpServletResponse response, String fileName, Long fileSize) {
        response.setCharacterEncoding(UTF_8);
        if (fileSize != null) {
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
        }

        if (SmartStringUtil.isNotEmpty(fileName)) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaTypeFactory.getMediaType(fileName).orElse(MediaType.APPLICATION_OCTET_STREAM) + ";charset=utf-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        }
    }

    // 新增静态方法用于快速创建响应对象
    public static <T> ResponseDTO<T> success(T data) {
        return ResponseDTO.ok(data);
    }

    public static ResponseDTO<String> success(String message) {
        return ResponseDTO.okMsg(message);
    }

    public static ResponseDTO<String> error(String message) {
        return ResponseDTO.userErrorParam(message);
    }

    public static ResponseDTO<Void> success() {
        return ResponseDTO.ok();
    }

    public static <T> ResponseDTO<T> success(T data, String message) {
        return new ResponseDTO<>(ResponseDTO.OK_CODE, null, true, message, data);
    }

    public static ResponseDTO<Boolean> success(boolean data) {
        return ResponseDTO.ok(data);
    }

}
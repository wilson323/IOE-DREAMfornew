package net.lab1024.sa.common.filter;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.response.ResponseDTOAdapter;
import net.lab1024.sa.common.util.JsonUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 响应格式过滤器
 * <p>
 * 自动检测客户端类型并转换响应格式，确保前后端API 100%兼容
 * 支持Smart-Admin前端、移动端APP、小程序等多种客户端
 * </p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：common-core应保持最小稳定内核，不包含Spring Web Filter
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseFormatFilter extends OncePerRequestFilter {

    private static final Pattern JSON_PATTERN = Pattern.compile("^application/json(?!;charset=utf-8)");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 使用响应包装器捕获响应内容（response已通过@NonNull注解保证非空）
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 执行过滤器链
        filterChain.doFilter(request, responseWrapper);

        // 获取响应内容
        byte[] responseBytes = responseWrapper.getContentAsByteArray();
        if (responseBytes.length > 0) {
            String originalResponse = new String(responseBytes, StandardCharsets.UTF_8);

            // 尝试转换响应格式
            String convertedResponse = convertResponseFormat(originalResponse, request, responseWrapper);

            if (convertedResponse != null && !convertedResponse.equals(originalResponse)) {
                // 设置响应头
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

                // 写入转换后的响应
                byte[] convertedBytes = convertedResponse.getBytes(StandardCharsets.UTF_8);
                response.setContentLength(convertedBytes.length);
                response.getOutputStream().write(convertedBytes);
                response.getOutputStream().flush();

                log.debug("Response format converted for client: {} -> {}",
                         ResponseDTOAdapter.ClientType.detectFromRequest(request).getDescription(),
                         convertedResponse.substring(0, Math.min(convertedResponse.length(), 100)));
                return; // 已手动写入响应，不需要再复制
            }
        }

        // 如果没有转换，则复制原始响应内容
        responseWrapper.copyBodyToResponse();
    }

    /**
     * 转换响应格式
     *
     * @param originalResponse 原始响应
     * @param request HTTP请求
     * @param response HTTP响应
     * @return 转换后的响应
     */
    private String convertResponseFormat(String originalResponse, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 检查是否为JSON响应
            String contentType = response.getContentType();
            if (contentType == null || !JSON_PATTERN.matcher(contentType).find()) {
                return originalResponse;
            }

            // 解析原始响应（使用Jackson，符合企业级标准）
            Map<String, Object> originalMap = JsonUtil.toMap(originalResponse);

            // 检查是否为标准IOE-DREAM响应格式
            if (!isValidIOEDreamResponse(originalMap)) {
                return originalResponse;
            }

            // 重新构造ResponseDTO对象
            ResponseDTO<Object> ioeResponse = reconstructResponseDTO(originalMap);
            if (ioeResponse == null) {
                return originalResponse;
            }

            // 根据客户端类型转换格式
            Map<String, Object> convertedResponse = ResponseDTOAdapter.autoConvert(ioeResponse, request);

            // 使用Jackson序列化（符合企业级标准）
            return JsonUtil.toJson(convertedResponse);

        } catch (Exception e) {
            log.error("Failed to convert response format: {}", e.getMessage(), e);
            return originalResponse; // 转换失败时返回原始响应
        }
    }

    /**
     * 检查是否为有效的IOE-DREAM响应格式
     *
     * @param responseMap 响应Map
     * @return 是否为有效格式
     */
    private boolean isValidIOEDreamResponse(Map<String, Object> responseMap) {
        return responseMap.containsKey("code") &&
               responseMap.containsKey("message") &&
               (responseMap.containsKey("data") || responseMap.containsKey("timestamp"));
    }

    /**
     * 重新构造ResponseDTO对象
     *
     * @param responseMap 响应Map
     * @return ResponseDTO对象
     */
    private ResponseDTO<Object> reconstructResponseDTO(Map<String, Object> responseMap) {
        try {
            Integer code = (Integer) responseMap.get("code");
            String message = (String) responseMap.get("message");
            Object data = responseMap.get("data");

            ResponseDTO<Object> response = new ResponseDTO<>();
            response.setCode(code != null ? code : 200);
            response.setMessage(message != null ? message : "");
            response.setData(data);

            return response;
        } catch (Exception e) {
            log.error("Failed to reconstruct ResponseDTO: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();

        // 排除不需要格式转换的路径
        return path.contains("/actuator/") ||
               path.contains("/swagger") ||
               path.contains("/api-docs") ||
               path.contains("/webjars/") ||
               path.contains("/favicon") ||
               path.contains("/static/") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".gif") ||
               path.endsWith(".ico");
    }

}


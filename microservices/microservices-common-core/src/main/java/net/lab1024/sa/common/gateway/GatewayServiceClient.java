package net.lab1024.sa.common.gateway;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 网关服务客户端
 * <p>
 * 统一的服务间调用客户端，所有微服务间调用必须通过API网关
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class GatewayServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String gatewayUrl;
    private final String serviceName;

    private static final String API_PREFIX = "/api/v1";

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param restTemplate RestTemplate实例
     * @param objectMapper ObjectMapper实例
     * @param gatewayUrl 网关URL
     * @param serviceName 服务名称（用于分布式追踪）
     */
    public GatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper, String gatewayUrl, String serviceName) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.gatewayUrl = gatewayUrl != null ? gatewayUrl : "http://localhost:8080";
        this.serviceName = serviceName != null ? serviceName : "unknown-service";
    }

    /**
     * 构造函数注入依赖（兼容旧版本，serviceName默认为unknown-service）
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param restTemplate RestTemplate实例
     * @param objectMapper ObjectMapper实例
     * @param gatewayUrl 网关URL
     */
    public GatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper, String gatewayUrl) {
        this(restTemplate, objectMapper, gatewayUrl, "unknown-service");
    }

    /**
     * 调用公共服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callCommonService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveCommonPath(path)), method, requestBody, responseType);
    }

    /**
     * 调用公共服务（支持TypeReference）
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param typeReference TypeReference类型引用
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callCommonService(String path, HttpMethod method, Object requestBody, TypeReference<ResponseDTO<T>> typeReference) {
        return callService(buildUrl(resolveCommonPath(path)), method, requestBody, typeReference);
    }

    /**
     * 调用设备通讯服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callDeviceCommService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveApiV1Path(path, "/device", API_PREFIX + "/device")), method, requestBody, responseType);
    }

    /**
     * 调用OA服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callOAService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(normalizePath(path)), method, requestBody, responseType);
    }

    /**
     * 调用门禁服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callAccessService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveApiV1Path(path, "/access", API_PREFIX + "/access")), method, requestBody, responseType);
    }

    /**
     * 调用考勤服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callAttendanceService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveApiV1Path(path, "/attendance", API_PREFIX + "/attendance")), method, requestBody, responseType);
    }

    /**
     * 调用消费服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callConsumeService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveApiV1Path(path, "/consume", API_PREFIX + "/consume")), method, requestBody, responseType);
    }

    /**
     * 调用访客服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callVisitorService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveApiV1Path(path, "/visitor", API_PREFIX + "/visitor")), method, requestBody, responseType);
    }

    /**
     * 调用视频服务
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callVideoService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService(buildUrl(resolveApiV1Path(path, "/video", API_PREFIX + "/video")), method, requestBody, responseType);
    }

    private String buildUrl(String path) {
        String normalizedPath = normalizePath(path);
        String normalizedGatewayUrl = gatewayUrl != null && gatewayUrl.endsWith("/")
                ? gatewayUrl.substring(0, gatewayUrl.length() - 1)
                : gatewayUrl;
        return normalizedGatewayUrl + normalizedPath;
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String resolveCommonPath(String path) {
        String normalized = normalizePath(path);
        if (normalized.startsWith("/common/") || normalized.equals("/common")) {
            return normalized.substring("/common".length());
        }
        return normalized;
    }

    /**
     * 将 legacy 前缀路径统一为 /api/v1 下的 canonical 路径。
     * <p>
     * 规则：
     * - 已是 /api/*：直接透传（支持 /api/v1 与少量历史 /api/...）
     * - 以 legacyPrefix 开头：替换为 canonicalPrefix
     * - 其它：当作相对路径拼接到 canonicalPrefix 之下
     * </p>
     */
    private static String resolveApiV1Path(String path, String legacyPrefix, String canonicalPrefix) {
        String normalized = normalizePath(path);
        if (normalized.startsWith("/api/")) {
            return normalized;
        }
        if (legacyPrefix != null && (normalized.startsWith(legacyPrefix + "/") || normalized.equals(legacyPrefix))) {
            return canonicalPrefix + normalized.substring(legacyPrefix.length());
        }
        return canonicalPrefix + normalized;
    }

    /**
     * 通用服务调用方法（支持TypeReference）
     *
     * @param url 完整URL
     * @param method HTTP方法（非null）
     * @param requestBody 请求体
     * @param typeReference TypeReference类型引用
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    public <T> ResponseDTO<T> callService(String url, HttpMethod method, Object requestBody, TypeReference<ResponseDTO<T>> typeReference) {
        // 参数验证
        if (url == null || method == null) {
            log.error("[网关调用] 参数错误：url或method为空");
            return ResponseDTO.error("PARAM_ERROR", "URL或HTTP方法不能为空");
        }

        try {
            log.debug("[网关调用] 调用服务（TypeReference），url={}, method={}", url, method);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 添加分布式追踪头
            addTracingHeaders(headers);

            String requestBodyJson = null;
            if (requestBody != null) {
                requestBodyJson = objectMapper.writeValueAsString(requestBody);
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            // 使用String接收响应，然后通过ObjectMapper反序列化
            // url和method已通过null检查
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 使用TypeReference反序列化
                ResponseDTO<T> responseBody = objectMapper.readValue(
                        response.getBody(),
                        typeReference
                );
                log.debug("[网关调用] 调用成功，url={}", url);
                return responseBody;
            } else {
                log.warn("[网关调用] 调用失败，url={}, status={}", url, response.getStatusCode());
                return ResponseDTO.error("SERVICE_CALL_ERROR", "服务调用失败");
            }
        } catch (Exception e) {
            log.error("[网关调用] 调用异常，url={}", url, e);
            return ResponseDTO.error("SERVICE_CALL_ERROR", "服务调用异常：" + e.getMessage());
        }
    }

    /**
     * 通用服务调用方法
     *
     * @param url 完整URL
     * @param method HTTP方法（非null）
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应数据类型
     * @return 响应结果
     */
    private <T> ResponseDTO<T> callService(String url, HttpMethod method, Object requestBody, Class<T> responseType) {
        // 参数验证
        if (url == null || method == null) {
            log.error("[网关调用] 参数错误：url或method为空");
            return ResponseDTO.error("PARAM_ERROR", "URL或HTTP方法不能为空");
        }

        try {
            log.debug("[网关调用] 调用服务，url={}, method={}", url, method);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 添加分布式追踪头
            addTracingHeaders(headers);

            String requestBodyJson = null;
            if (requestBody != null) {
                requestBodyJson = objectMapper.writeValueAsString(requestBody);
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            // 使用String接收响应，然后通过ObjectMapper反序列化（更可靠的方式）
            // url和method已通过null检查
            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 反序列化为ResponseDTO<Object>
                ResponseDTO<Object> responseBody = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<ResponseDTO<Object>>() {}
                );

                if (responseBody.getData() != null && responseType != null) {
                    // 转换响应数据
                    Object data = responseBody.getData();
                    T convertedData = objectMapper.convertValue(data, responseType);
                    return ResponseDTO.ok(convertedData);
                }
                log.debug("[网关调用] 调用成功，url={}", url);
                @SuppressWarnings("unchecked")
                ResponseDTO<T> result = (ResponseDTO<T>) responseBody;
                return result;
            } else {
                log.warn("[网关调用] 调用失败，url={}, status={}", url, response.getStatusCode());
                return ResponseDTO.error("SERVICE_CALL_ERROR", "服务调用失败");
            }
        } catch (Exception e) {
            log.error("[网关调用] 调用异常，url={}", url, e);
            return ResponseDTO.error("SERVICE_CALL_ERROR", "服务调用异常：" + e.getMessage());
        }
    }

    /**
     * 添加分布式追踪头
     * <p>
     * 实现分布式追踪功能，添加X-Trace-Id和X-Source-Service头
     * - X-Trace-Id: 从MDC获取，如果没有则生成新的UUID
     * - X-Source-Service: 当前服务名称
     * </p>
     *
     * @param headers HTTP请求头
     */
    private void addTracingHeaders(HttpHeaders headers) {
        // 获取或生成Trace ID
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
            // 将生成的traceId放入MDC，供后续使用
            MDC.put("traceId", traceId);
        }
        headers.set("X-Trace-Id", traceId);

        // 添加源服务名称
        headers.set("X-Source-Service", serviceName);

        log.debug("[网关调用] 添加追踪头，traceId={}, sourceService={}", traceId, serviceName);
    }
}

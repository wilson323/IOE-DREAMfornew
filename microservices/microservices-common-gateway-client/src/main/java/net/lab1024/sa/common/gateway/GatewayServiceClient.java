package net.lab1024.sa.common.gateway;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 网关服务客户端
 * <p>
 * 提供统一的微服务间调用接口，支持通过API网关调用其他服务
 * </p>
 *
 * <p>
 * <strong>模块说明</strong>：此类位于`microservices-common-gateway-client`模块中，
 * 作为独立的网关客户端模块，避免业务服务同时依赖`microservices-common`和细粒度模块。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class GatewayServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String gatewayBaseUrl;

    public GatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper, String gatewayBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.gatewayBaseUrl = gatewayBaseUrl;
    }

    /**
     * 将Jackson TypeReference转换为Spring ParameterizedTypeReference
     */
    private <T> ParameterizedTypeReference<T> toParameterizedTypeReference(TypeReference<T> typeReference) {
        return new ParameterizedTypeReference<T>() {
            @Override
            public java.lang.reflect.Type getType() {
                return typeReference.getType();
            }
        };
    }

    /**
     * 调用公共服务
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Object request,
                                     Class<T> responseType) {
        log.info("Calling common service: {} {} with request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            if (request != null) {
                return restTemplate.postForObject(url, request, responseType);
            } else {
                return restTemplate.getForObject(url, responseType);
            }
        } catch (Exception e) {
            log.error("Failed to call common service: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用公共服务（Map参数）
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Map<String, Object> request,
                                     Class<T> responseType) {
        log.info("Calling common service: {} with map request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            if (request != null) {
                return restTemplate.postForObject(url, request, responseType);
            } else {
                return restTemplate.getForObject(url, responseType);
            }
        } catch (Exception e) {
            log.error("Failed to call common service: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用OA服务
     */
    public <T> T callOAService(String apiPath, HttpMethod method, Map<String, Object> request,
                                 Class<T> responseType) {
        log.info("Calling OA service: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/oa" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用设备通讯服务
     */
    public <T> T callDeviceCommService(String apiPath, HttpMethod method, Map<String, Object> request,
                                       Class<T> responseType) {
        log.info("Calling device comm service: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/device-comm" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用设备通讯服务（TypeReference）
     */
    public <T> T callDeviceCommService(String apiPath, HttpMethod method, Map<String, Object> request,
                                       TypeReference<T> responseType) {
        log.info("Calling device comm service with TypeReference: {} {} with request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + "/device-comm" + apiPath;
            T result;
            ParameterizedTypeReference<T> paramTypeRef = toParameterizedTypeReference(responseType);
            if (request != null && method == HttpMethod.POST) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);
                result = restTemplate.exchange(url, method, entity, paramTypeRef).getBody();
            } else {
                result = restTemplate.exchange(url, method, null, paramTypeRef).getBody();
            }

            if (result != null) {
                return result;
            }
            throw new RuntimeException("设备通讯服务调用返回空响应");
        } catch (Exception e) {
            log.error("Failed to call device comm service with TypeReference: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用设备通讯服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用消费服务
     */
    public <T> T callConsumeService(String apiPath, HttpMethod method, Map<String, Object> request,
                                   Class<T> responseType) {
        log.info("Calling consume service: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/consume" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用考勤服务
     */
    public <T> T callAttendanceService(String apiPath, HttpMethod method, Map<String, Object> request,
                                       Class<T> responseType) {
        log.info("Calling attendance service: {} {} with request: {}", method, apiPath, request);
        String url = gatewayBaseUrl + "/attendance" + apiPath;
        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * 调用访客服务
     */
    public <T> T callVisitorService(String apiPath, HttpMethod method, Map<String, Object> request,
                                   Class<T> responseType) {
        log.info("Calling visitor service: {} {} with request: {}", method, apiPath, request);
        return callCommonService(apiPath, method, request, responseType);
    }

    /**
     * 调用访客服务（TypeReference）
     */
    public <T> T callVisitorService(String apiPath, HttpMethod method, Map<String, Object> request,
                                   TypeReference<T> responseType) {
        log.info("Calling visitor service with TypeReference: {} {} with request: {}", method, apiPath, request);
        return callCommonService(apiPath, method, request, responseType);
    }

    /**
     * 调用门禁服务
     */
    public <T> T callAccessService(String apiPath, HttpMethod method, Map<String, Object> request,
                                  Class<T> responseType) {
        log.info("Calling access service: {} {} with request: {}", method, apiPath, request);
        return callCommonService(apiPath, method, request, responseType);
    }

    /**
     * 调用公共服务（TypeReference）
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Object request,
                                  TypeReference<T> responseType) {
        log.info("Calling common service with TypeReference: {} {} with request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            T result;
            ParameterizedTypeReference<T> paramTypeRef = toParameterizedTypeReference(responseType);
            if (request != null && method == HttpMethod.POST) {
                HttpEntity<Object> entity = new HttpEntity<>(request);
                result = restTemplate.exchange(url, method, entity, paramTypeRef).getBody();
            } else {
                result = restTemplate.exchange(url, method, null, paramTypeRef).getBody();
            }

            if (result != null) {
                return result;
            }
            throw new RuntimeException("服务调用返回空响应");
        } catch (Exception e) {
            log.error("Failed to call common service with TypeReference: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用公共服务（Map参数，TypeReference）
     */
    public <T> T callCommonService(String apiPath, HttpMethod method, Map<String, Object> request,
                                  TypeReference<T> responseType) {
        log.info("Calling common service with TypeReference: {} {} with map request: {}", method, apiPath, request);
        try {
            String url = gatewayBaseUrl + apiPath;
            T result;
            ParameterizedTypeReference<T> paramTypeRef = toParameterizedTypeReference(responseType);
            if (request != null && method == HttpMethod.POST) {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);
                result = restTemplate.exchange(url, method, entity, paramTypeRef).getBody();
            } else {
                result = restTemplate.exchange(url, method, null, paramTypeRef).getBody();
            }

            if (result != null) {
                return result;
            }
            throw new RuntimeException("服务调用返回空响应");
        } catch (Exception e) {
            log.error("Failed to call common service with TypeReference: {} {}", apiPath, e.getMessage(), e);
            throw new RuntimeException("调用公共服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简单的服务调用
     */
    public void invokeService(String serviceName, String apiPath) {
        log.info("Invoking service: {} at path: {}", serviceName, apiPath);
    }
}
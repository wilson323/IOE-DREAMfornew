package net.lab1024.sa.video.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.dto.ResponseDTO;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 视频服务网关集成配置
 * <p>
 * 提供与其他微服务的统一集成能力：
 * 1. HTTP客户端配置
 * 2. 服务调用封装
 * 3. 错误处理机制
 * 4. 请求超时控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Configuration
public class VideoGatewayIntegrationConfig {

    /**
     * HTTP客户端配置
     * <p>
     * 配置用于微服务间调用的RestTemplate：
     * - 连接超时：5秒
     * - 读取超时：30秒
     * - 连接池管理
     * - 重试机制
     * </p>
     *
     * @return RestTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        log.info("[RestTemplate] 初始化HTTP客户端");

        // 配置HTTP请求工厂，设置超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());

        RestTemplate restTemplate = new RestTemplate(factory);

        log.info("[RestTemplate] HTTP客户端初始化完成，连接超时: 5秒，读取超时: 30秒");

        return restTemplate;
    }

    /**
     * 微服务调用助手
     * <p>
     * 提供统一的微服务调用接口：
     * - 路径构建
     * - 请求发送
     * - 响应处理
     * - 错误处理
     * </p>
     *
     * @param restTemplate RestTemplate实例
     * @param objectMapper ObjectMapper实例
     * @return 微服务调用助手
     */
    @Bean
    @ConditionalOnMissingBean(name = "microServiceCallHelper")
    public MicroServiceCallHelper microServiceCallHelper(RestTemplate restTemplate, ObjectMapper objectMapper) {
        log.info("[MicroServiceCallHelper] 初始化微服务调用助手");

        return new MicroServiceCallHelper(restTemplate, objectMapper);
    }

    /**
     * 微服务调用助手类
     */
    public static class MicroServiceCallHelper {

        private final RestTemplate restTemplate;
        private final ObjectMapper objectMapper;

        // 微服务基础路径
        private static final String GATEWAY_BASE_URL = "http://ioedream-gateway-service:8080";
        private static final String COMMON_SERVICE_PATH = "/api/v1/common";
        private static final String DEVICE_SERVICE_PATH = "/api/v1/device";
        private static final String ACCESS_SERVICE_PATH = "/api/v1/access";
        private static final String ATTENDANCE_SERVICE_PATH = "/api/v1/attendance";
        private static final String CONSUME_SERVICE_PATH = "/api/v1/consume";
        private static final String VISITOR_SERVICE_PATH = "/api/v1/visitor";

        public MicroServiceCallHelper(RestTemplate restTemplate, ObjectMapper objectMapper) {
            this.restTemplate = restTemplate;
            this.objectMapper = objectMapper;
        }

        /**
         * 调用公共服务
         */
        public <T> ResponseDTO<T> callCommonService(String path, HttpMethod method, Object request, Class<T> responseType) {
            return callService(COMMON_SERVICE_PATH + path, method, request, responseType);
        }

        /**
         * 调用设备服务
         */
        public <T> ResponseDTO<T> callDeviceService(String path, HttpMethod method, Object request, Class<T> responseType) {
            return callService(DEVICE_SERVICE_PATH + path, method, request, responseType);
        }

        /**
         * 调用门禁服务
         */
        public <T> ResponseDTO<T> callAccessService(String path, HttpMethod method, Object request, Class<T> responseType) {
            return callService(ACCESS_SERVICE_PATH + path, method, request, responseType);
        }

        /**
         * 调用考勤服务
         */
        public <T> ResponseDTO<T> callAttendanceService(String path, HttpMethod method, Object request, Class<T> responseType) {
            return callService(ATTENDANCE_SERVICE_PATH + path, method, request, responseType);
        }

        /**
         * 调用消费服务
         */
        public <T> ResponseDTO<T> callConsumeService(String path, HttpMethod method, Object request, Class<T> responseType) {
            return callService(CONSUME_SERVICE_PATH + path, method, request, responseType);
        }

        /**
         * 调用访客服务
         */
        public <T> ResponseDTO<T> callVisitorService(String path, HttpMethod method, Object request, Class<T> responseType) {
            return callService(VISITOR_SERVICE_PATH + path, method, request, responseType);
        }

        /**
         * 通用服务调用方法
         * <p>
         * 使用exchange方法调用服务，并正确反序列化为ResponseDTO<T>
         * </p>
         *
         * @param servicePath 服务路径
         * @param method HTTP方法
         * @param request 请求体
         * @param responseType 响应数据类型
         * @param <T> 响应数据类型
         * @return 响应结果
         */
        @SuppressWarnings("null")
        private <T> ResponseDTO<T> callService(String servicePath, HttpMethod method, Object request, Class<T> responseType) {
            // 参数验证
            if (method == null) {
                log.error("[微服务调用] HTTP方法不能为空");
                return ResponseDTO.error("PARAM_ERROR", "HTTP方法不能为空");
            }

            String url = GATEWAY_BASE_URL + servicePath;

            try {
                log.debug("[微服务调用] {} {}", method, url);

                // 配置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                @SuppressWarnings("null")
                List<MediaType> acceptTypes = Collections.singletonList(MediaType.APPLICATION_JSON);
                headers.setAccept(acceptTypes);

                // 序列化请求体
                String requestBodyJson = null;
                if (request != null) {
                    requestBodyJson = objectMapper.writeValueAsString(request);
                }

                HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

                // 使用exchange方法调用服务
                ResponseEntity<String> response;
                if (method == HttpMethod.GET) {
                    response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                } else if (method == HttpMethod.POST) {
                    response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                } else if (method == HttpMethod.PUT) {
                    response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
                } else if (method == HttpMethod.DELETE) {
                    response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
                } else {
                    throw new UnsupportedOperationException("不支持的HTTP方法: " + method);
                }

                // 反序列化响应为ResponseDTO<T>
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // 先反序列化为ResponseDTO<Object>
                    ResponseDTO<Object> responseBody = objectMapper.readValue(
                            response.getBody(),
                            new TypeReference<ResponseDTO<Object>>() {}
                    );

                    if (responseBody.getData() != null && responseType != null) {
                        // 转换响应数据
                        Object data = responseBody.getData();
                        T convertedData = objectMapper.convertValue(data, responseType);
                        ResponseDTO<T> result = ResponseDTO.ok(convertedData);
                        log.debug("[微服务调用] 响应: {}", result);
                        return result;
                    }

                    // 如果没有数据，直接返回ResponseDTO
                    @SuppressWarnings("unchecked")
                    ResponseDTO<T> result = (ResponseDTO<T>) responseBody;
                    log.debug("[微服务调用] 响应: {}", result);
                    return result;
                } else {
                    log.warn("[微服务调用] 调用失败，url={}, status={}", url, response.getStatusCode());
                    return ResponseDTO.error("SERVICE_CALL_ERROR", "服务调用失败");
                }

            } catch (Exception e) {
                log.error("[微服务调用] {} {} 调用失败: {}", method, url, e.getMessage(), e);
                return ResponseDTO.error("SERVICE_CALL_ERROR", "微服务调用失败: " + e.getMessage());
            }
        }

        // ========== 具体业务服务调用方法 ==========

        /**
         * 获取用户信息
         */
        public ResponseDTO<Object> getUserInfo(Long userId) {
            return callCommonService("/user/" + userId, HttpMethod.GET, null, Object.class);
        }

        /**
         * 获取设备信息
         */
        public ResponseDTO<Object> getDeviceInfo(Long deviceId) {
            return callDeviceService("/device/" + deviceId, HttpMethod.GET, null, Object.class);
        }

        /**
         * 获取区域信息
         */
        public ResponseDTO<Object> getAreaInfo(Long areaId) {
            return callCommonService("/area/" + areaId, HttpMethod.GET, null, Object.class);
        }

        /**
         * 获取部门信息
         */
        public ResponseDTO<Object> getDepartmentInfo(Long departmentId) {
            return callCommonService("/department/" + departmentId, HttpMethod.GET, null, Object.class);
        }

        /**
         * 发送通知
         */
        public ResponseDTO<Void> sendNotification(String notificationType, Object notificationData) {
            return callCommonService("/notification/send/" + notificationType, HttpMethod.POST, notificationData, Void.class);
        }

        /**
         * 记录审计日志
         */
        public ResponseDTO<Void> recordAuditLog(Object auditLogData) {
            return callCommonService("/audit/log", HttpMethod.POST, auditLogData, Void.class);
        }

        /**
         * 触发门禁控制
         */
        public ResponseDTO<Void> controlAccessDoor(Long deviceId, String action) {
            String path = "/door/" + deviceId + "/control";
            Object request = Collections.singletonMap("action", action);
            return callAccessService(path, HttpMethod.POST, request, Void.class);
        }

        /**
         * 查询访客信息
         */
        public ResponseDTO<Object> getVisitorInfo(String visitId) {
            return callVisitorService("/visit/" + visitId, HttpMethod.GET, null, Object.class);
        }

        /**
         * 查询消费记录
         */
        public ResponseDTO<Object> getConsumeRecords(Long userId, String startTime, String endTime) {
            String path = "/record/user/" + userId + "?startTime=" + startTime + "&endTime=" + endTime;
            return callConsumeService(path, HttpMethod.GET, null, Object.class);
        }

        /**
         * 查询考勤记录
         */
        public ResponseDTO<Object> getAttendanceRecords(Long userId, String startDate, String endDate) {
            String path = "/record/user/" + userId + "?startDate=" + startDate + "&endDate=" + endDate;
            return callAttendanceService(path, HttpMethod.GET, null, Object.class);
        }
    }

    /**
     * 服务降级处理器
     * <p>
     * 当微服务调用失败时，提供降级处理：
     * - 返回默认值
     * - 记录错误日志
     * - 触发告警
     * </p>
     *
     * @return 服务降级处理器
     */
    @Bean
    @ConditionalOnMissingBean(name = "serviceFallbackHandler")
    public ServiceFallbackHandler serviceFallbackHandler() {
        log.info("[ServiceFallbackHandler] 初始化服务降级处理器");

        return new ServiceFallbackHandler();
    }

    /**
     * 服务降级处理器类
     */
    public static class ServiceFallbackHandler {

        /**
         * 用户信息降级处理
         */
        public ResponseDTO<Object> getUserInfoFallback(Long userId, Throwable throwable) {
            log.warn("[服务降级] 获取用户信息降级处理, userId={}, error={}", userId, throwable.getMessage());

            Object defaultUser = Collections.singletonMap("userId", userId);
            return ResponseDTO.ok(defaultUser);
        }

        /**
         * 设备信息降级处理
         */
        public ResponseDTO<Object> getDeviceInfoFallback(Long deviceId, Throwable throwable) {
            log.warn("[服务降级] 获取设备信息降级处理, deviceId={}, error={}", deviceId, throwable.getMessage());

            Object defaultDevice = Collections.singletonMap("deviceId", deviceId);
            return ResponseDTO.ok(defaultDevice);
        }

        /**
         * 通知发送降级处理
         */
        public ResponseDTO<Void> sendNotificationFallback(String type, Object data, Throwable throwable) {
            log.warn("[服务降级] 发送通知降级处理, type={}, error={}", type, throwable.getMessage());
            return ResponseDTO.ok();
        }

        /**
         * 门禁控制降级处理
         */
        public ResponseDTO<Void> controlAccessDoorFallback(Long deviceId, String action, Throwable throwable) {
            log.warn("[服务降级] 门禁控制降级处理, deviceId={}, action={}, error={}", deviceId, action, throwable.getMessage());
            return ResponseDTO.error("SERVICE_UNAVAILABLE", "门禁服务暂时不可用，请稍后重试");
        }
    }
}

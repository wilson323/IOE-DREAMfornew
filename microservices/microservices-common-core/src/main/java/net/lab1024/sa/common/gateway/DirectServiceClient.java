package net.lab1024.sa.common.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.MDC;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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
 * 直连服务客户端（东西向热路径直连）
 * <p>
 * 统一的服务直连调用客户端，基于DiscoveryClient选择目标实例，绕过网关。
 * 仅用于同域高频/低延迟热路径，需配套白名单与S2S鉴权（HMAC）。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-12
 */
@Slf4j
public class DirectServiceClient {

    private static final String DIRECT_CALL_HEADER = "X-Direct-Call";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NONCE_HEADER = "X-Nonce";
    private static final String SIGNATURE_HEADER = "X-Signature";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DiscoveryClient discoveryClient;
    private final String sourceServiceName;
    private final String sharedSecret;
    private final boolean enabled;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MeterRegistry meterRegistry;

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public DirectServiceClient(RestTemplate restTemplate,
                               ObjectMapper objectMapper,
                               DiscoveryClient discoveryClient,
                               String sourceServiceName,
                               String sharedSecret,
                               boolean enabled) {
        this(restTemplate, objectMapper, discoveryClient, sourceServiceName, sharedSecret, enabled, null, null, null);
    }

    public DirectServiceClient(RestTemplate restTemplate,
                               ObjectMapper objectMapper,
                               DiscoveryClient discoveryClient,
                               String sourceServiceName,
                               String sharedSecret,
                               boolean enabled,
                               RetryRegistry retryRegistry,
                               CircuitBreakerRegistry circuitBreakerRegistry,
                               MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.discoveryClient = discoveryClient;
        this.sourceServiceName = sourceServiceName != null ? sourceServiceName : "unknown-service";
        this.sharedSecret = sharedSecret != null ? sharedSecret : "";
        this.enabled = enabled;
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.meterRegistry = meterRegistry;
    }

    public boolean isEnabled() {
        return enabled && !sharedSecret.isBlank();
    }

    public <T> ResponseDTO<T> callCommonService(String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        return callService("ioedream-common-service", path, method, requestBody, responseType);
    }

    public <T> ResponseDTO<T> callCommonService(String path, HttpMethod method, Object requestBody, TypeReference<ResponseDTO<T>> typeReference) {
        return callService("ioedream-common-service", path, method, requestBody, typeReference);
    }

    public <T> ResponseDTO<T> callService(String serviceId, String path, HttpMethod method, Object requestBody, Class<T> responseType) {
        if (!isEnabled()) {
            return ResponseDTO.error("DIRECT_CALL_DISABLED", "直连调用未启用或未配置sharedSecret");
        }
        if (serviceId == null || path == null || method == null) {
            return ResponseDTO.error("PARAM_ERROR", "serviceId/path/method不能为空");
        }

        String url = resolveServiceUrl(serviceId, path);
        if (url == null) {
            return ResponseDTO.error("SERVICE_NOT_FOUND", "未发现服务实例: " + serviceId);
        }

        return executeWithResilience(serviceId, method, path, () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            addTracingHeaders(headers);
            addDirectAuthHeaders(headers, method, path, requestBody);

            String bodyJson = requestBody != null ? objectMapper.writeValueAsString(requestBody) : null;
            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ResponseDTO<Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<ResponseDTO<Object>>() {});
                if (responseBody.getData() != null && responseType != null) {
                    T converted = objectMapper.convertValue(responseBody.getData(), responseType);
                    return ResponseDTO.ok(converted);
                }
                @SuppressWarnings("unchecked")
                ResponseDTO<T> result = (ResponseDTO<T>) responseBody;
                return result;
            }
            return ResponseDTO.error("SERVICE_CALL_ERROR", "直连服务调用失败");
        });
    }

    public <T> ResponseDTO<T> callService(String serviceId, String path, HttpMethod method, Object requestBody, TypeReference<ResponseDTO<T>> typeReference) {
        if (!isEnabled()) {
            return ResponseDTO.error("DIRECT_CALL_DISABLED", "直连调用未启用或未配置sharedSecret");
        }
        if (serviceId == null || path == null || method == null) {
            return ResponseDTO.error("PARAM_ERROR", "serviceId/path/method不能为空");
        }

        String url = resolveServiceUrl(serviceId, path);
        if (url == null) {
            return ResponseDTO.error("SERVICE_NOT_FOUND", "未发现服务实例: " + serviceId);
        }

        return executeWithResilience(serviceId, method, path, () -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            addTracingHeaders(headers);
            addDirectAuthHeaders(headers, method, path, requestBody);

            String bodyJson = requestBody != null ? objectMapper.writeValueAsString(requestBody) : null;
            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

            @SuppressWarnings("null")
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), typeReference);
            }
            return ResponseDTO.error("SERVICE_CALL_ERROR", "直连服务调用失败");
        });
    }

    private <T> ResponseDTO<T> executeWithResilience(String serviceId, HttpMethod method, String path, CheckedSupplier<ResponseDTO<T>> call) {
        Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;
        String endpoint = normalizePathForMetrics(path);
        try {
            Supplier<ResponseDTO<T>> guarded = () -> {
                try {
                    ResponseDTO<T> response = call.get();
                    if (response == null || !response.isSuccess()) {
                        throw new DirectCallFailedException(response);
                    }
                    return response;
                } catch (DirectCallFailedException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            Supplier<ResponseDTO<T>> decorated = guarded;
            if (circuitBreakerRegistry != null) {
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("direct-call." + serviceId);
                decorated = CircuitBreaker.decorateSupplier(circuitBreaker, decorated);
            }
            if (retryRegistry != null) {
                Retry retry = retryRegistry.retry("direct-call." + serviceId);
                decorated = Retry.decorateSupplier(retry, decorated);
            }

            ResponseDTO<T> response = decorated.get();
            recordCallMetrics(serviceId, method, endpoint, "success");
            return response;
        } catch (DirectCallFailedException e) {
            recordCallMetrics(serviceId, method, endpoint, "failure");
            @SuppressWarnings("unchecked")
            ResponseDTO<T> response = (ResponseDTO<T>) e.getResponse();
            return response != null ? response : ResponseDTO.error("SERVICE_CALL_ERROR", "直连服务调用失败");
        } catch (Exception e) {
            recordCallMetrics(serviceId, method, endpoint, "error");
            log.error("[直连调用] 异常，serviceId={}, path={}", serviceId, path, e);
            return ResponseDTO.error("SERVICE_CALL_ERROR", "直连服务调用异常：" + e.getMessage());
        } finally {
            if (meterRegistry != null && sample != null) {
                Timer timer = Timer.builder("ioedream.direct.call.latency")
                        .tag("serviceId", serviceId)
                        .tag("method", method != null ? method.name() : "UNKNOWN")
                        .tag("endpoint", endpoint)
                        .register(meterRegistry);
                sample.stop(timer);
            }
        }
    }

    private void recordCallMetrics(String serviceId, HttpMethod method, String endpoint, String result) {
        if (meterRegistry == null) {
            return;
        }
        Counter.builder("ioedream.direct.call.total")
                .tag("serviceId", serviceId != null ? serviceId : "UNKNOWN")
                .tag("method", method != null ? method.name() : "UNKNOWN")
                .tag("endpoint", endpoint)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    private static String normalizePathForMetrics(String path) {
        if (path == null || path.isBlank()) {
            return "unknown";
        }
        String trimmed = path.trim();
        String prefix = "/api/v1/";
        int idx = trimmed.indexOf(prefix);
        if (idx != 0) {
            return "custom";
        }
        String rest = trimmed.substring(prefix.length());
        int slash = rest.indexOf('/');
        String resource = slash >= 0 ? rest.substring(0, slash) : rest;
        if (resource.isBlank()) {
            return "/api/v1/**";
        }
        return prefix + resource + "/**";
    }

    private String resolveServiceUrl(String serviceId, String path) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances == null || instances.isEmpty()) {
            return null;
        }

        int index;
        AtomicInteger counter = counters.computeIfAbsent(serviceId, k -> new AtomicInteger(0));
        int size = instances.size();
        int next = counter.getAndIncrement();
        if (next < 0) {
            counter.set(0);
            next = 0;
        }
        index = next % size;

        ServiceInstance chosen = instances.get(index);
        String baseUrl = chosen.getUri().toString();
        return baseUrl + path;
    }

    private void addTracingHeaders(HttpHeaders headers) {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        headers.set(TRACE_ID_HEADER, traceId);
        headers.set(SOURCE_SERVICE_HEADER, sourceServiceName);
    }

    private void addDirectAuthHeaders(HttpHeaders headers, HttpMethod method, String path, Object requestBody) {
        headers.set(DIRECT_CALL_HEADER, "true");

        long timestamp = System.currentTimeMillis();
        String nonce = Long.toHexString(ThreadLocalRandom.current().nextLong()) + "-" + UUID.randomUUID();

        headers.set(TIMESTAMP_HEADER, String.valueOf(timestamp));
        headers.set(NONCE_HEADER, nonce);

        try {
            String bodyJson = requestBody != null ? objectMapper.writeValueAsString(requestBody) : "";
            String bodyHash = sha256Hex(bodyJson.getBytes(StandardCharsets.UTF_8));
            String message = method.name() + "\n" + path + "\n" + bodyHash + "\n" + timestamp + "\n" + nonce;
            String signature = hmacSha256Base64(sharedSecret, message);
            headers.set(SIGNATURE_HEADER, signature);
        } catch (Exception e) {
            log.warn("[直连调用] 生成签名失败，path={}", path, e);
        }
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hmacSha256Base64(String secret, String message) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(raw);
    }

    private static final class DirectCallFailedException extends RuntimeException {
        private final ResponseDTO<?> response;

        private DirectCallFailedException(ResponseDTO<?> response) {
            this.response = response;
        }

        private ResponseDTO<?> getResponse() {
            return response;
        }
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}

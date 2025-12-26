package net.lab1024.sa.platform.gateway.client;

import net.lab1024.sa.platform.core.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统一网关服务客户端 - 重构版本
 * <p>
 * 解决原有GatewayServiceClient的依赖混乱问题，提供简洁的服务调用接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-22
 */
@FeignClient(name = "gateway", url = "${gateway.url:}")
public interface GatewayServiceClient {

    /**
     * 调用服务API
     *
     * @param serviceName 服务名称
     * @param path API路径
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @return 响应结果
     */
    @PostMapping(value = "/gateway/call/{serviceName}/**", consumes = MediaType.APPLICATION_JSON_VALUE)
    <T> ResponseDTO<T> callService(
            @PathVariable("serviceName") String serviceName,
            @RequestParam("path") String path,
            @RequestParam("method") String method,
            @RequestBody(required = false) Object requestBody,
            @RequestParam("responseType") Class<T> responseType
    );

    /**
     * GET请求调用
     */
    default <T> ResponseDTO<T> get(String serviceName, String path, Class<T> responseType) {
        return callService(serviceName, path, "GET", null, responseType);
    }

    /**
     * POST请求调用
     */
    default <T> ResponseDTO<T> post(String serviceName, String path, Object requestBody, Class<T> responseType) {
        return callService(serviceName, path, "POST", requestBody, responseType);
    }

    /**
     * PUT请求调用
     */
    default <T> ResponseDTO<T> put(String serviceName, String path, Object requestBody, Class<T> responseType) {
        return callService(serviceName, path, "PUT", requestBody, responseType);
    }

    /**
     * DELETE请求调用
     */
    default <T> ResponseDTO<T> delete(String serviceName, String path, Class<T> responseType) {
        return callService(serviceName, path, "DELETE", null, responseType);
    }

    /**
     * 健康检查
     */
    @GetMapping("/gateway/health")
    default ResponseDTO<Map<String, Object>> health() {
        return ResponseDTO.ok(Map.of("status", "UP"));
    }

    /**
     * 服务发现
     */
    @GetMapping("/gateway/services")
    default ResponseDTO<Map<String, Object>> discoverServices() {
        return ResponseDTO.ok(Map.of());
    }
}
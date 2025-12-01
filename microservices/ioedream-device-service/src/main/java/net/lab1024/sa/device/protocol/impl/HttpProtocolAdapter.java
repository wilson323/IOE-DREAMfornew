package net.lab1024.sa.device.protocol.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.device.protocol.DeviceProtocolAdapter;
import net.lab1024.sa.device.protocol.DeviceProtocolException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * HTTP协议适配器实现 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 实现DeviceProtocolAdapter接口
 * - 完整的异常处理和日志记录
 * - 超时控制和重试机制
 * - 支持RESTful API和传统HTTP接口
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Slf4j
public class HttpProtocolAdapter implements DeviceProtocolAdapter {

    /**
     * 默认连接超时时间(秒)
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 10;

    /**
     * 重试次数
     */
    private static final int RETRY_COUNT = 3;

    /**
     * 支持的厂商列表
     */
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
            "海康威视", "大华", "宇视", "雄迈", "中维世纪", "天地伟业", "华为", "阿里云"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpProtocolAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.HTTP;
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return new ArrayList<>(SUPPORTED_MANUFACTURERS);
    }

    @Override
    public boolean remoteOpen(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议远程开门，设备: {}", device.getDeviceName());

        String url = buildDeviceUrl(device, "/api/remote/open");
        Map<String, Object> requestBody = buildRemoteOpenRequest(device);

        return executeCommandWithRetry(device, url, requestBody, "远程开门");
    }

    @Override
    public boolean restartDevice(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议重启设备，设备: {}", device.getDeviceName());

        String url = buildDeviceUrl(device, "/api/device/restart");
        Map<String, Object> requestBody = buildRestartRequest(device);

        return executeCommandWithRetry(device, url, requestBody, "重启设备");
    }

    @Override
    public boolean syncDeviceTime(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议同步设备时间，设备: {}", device.getDeviceName());

        String url = buildDeviceUrl(device, "/api/device/sync-time");
        Map<String, Object> requestBody = buildSyncTimeRequest(device);

        return executeCommandWithRetry(device, url, requestBody, "同步时间");
    }

    @Override
    public boolean checkConnection(AccessDeviceEntity device) throws DeviceProtocolException {
        log.debug("HTTP协议检查设备连接，设备: {}", device.getDeviceName());

        try {
            String url = buildDeviceUrl(device, "/api/heartbeat");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200 && isResponseSuccess(response.body());
        } catch (Exception e) {
            throw new DeviceProtocolException(
                "设备连接检查失败: " + e.getMessage(),
                e,
                "CONNECTION_CHECK_FAILED",
                device.getAccessDeviceId(),
                "HTTP"
            );
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议获取设备状态，设备: {}", device.getDeviceName());

        try {
            String url = buildDeviceUrl(device, "/api/device/status");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP请求失败，状态码: " + response.statusCode());
            }

            return parseStatusResponse(response.body());
        } catch (Exception e) {
            throw new DeviceProtocolException(
                "获取设备状态失败: " + e.getMessage(),
                e,
                "GET_STATUS_FAILED",
                device.getAccessDeviceId(),
                "HTTP"
            );
        }
    }

    @Override
    public Map<String, Object> sendCustomCommand(AccessDeviceEntity device, String command, Map<String, Object> params) throws DeviceProtocolException {
        log.info("HTTP协议发送自定义命令，设备: {}, 命令: {}", device.getDeviceName(), command);

        try {
            String url = buildDeviceUrl(device, "/api/custom/command");
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("command", command);
            requestBody.put("params", params);
            requestBody.put("deviceId", device.getDeviceCode());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP请求失败，状态码: " + response.statusCode());
            }

            return parseCustomResponse(response.body());
        } catch (Exception e) {
            throw new DeviceProtocolException(
                "发送自定义命令失败: " + e.getMessage(),
                e,
                "CUSTOM_COMMAND_FAILED",
                device.getAccessDeviceId(),
                "HTTP"
            );
        }
    }

    /**
     * 构建设备URL
     */
    private String buildDeviceUrl(AccessDeviceEntity device, String path) {
        String protocol = device.getPort() == 443 ? "https" : "http";
        String baseUrl = String.format("%s://%s:%d", protocol, device.getIpAddress(), device.getPort());
        return baseUrl + path;
    }

    /**
     * 带重试的命令执行
     */
    private boolean executeCommandWithRetry(AccessDeviceEntity device, String url, Map<String, Object> requestBody, String operation) throws DeviceProtocolException {
        Exception lastException = null;

        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                log.debug("HTTP协议执行{}，第{}次尝试，设备: {}", operation, i + 1, device.getDeviceName());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && isResponseSuccess(response.body())) {
                    log.info("HTTP协议{}成功，设备: {}", operation, device.getDeviceName());
                    return true;
                } else {
                    log.warn("HTTP协议{}失败，状态码: {}, 响应: {}", operation, response.statusCode(), response.body());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("HTTP协议{}第{}次尝试失败: {}", operation, i + 1, e.getMessage());

                if (i < RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new DeviceProtocolException(
            String.format("HTTP协议%s失败，已重试%d次", operation, RETRY_COUNT),
            lastException,
            "EXECUTE_COMMAND_FAILED",
            device.getAccessDeviceId(),
            "HTTP"
        );
    }

    /**
     * 构建远程开门请求
     */
    private Map<String, Object> buildRemoteOpenRequest(AccessDeviceEntity device) {
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", device.getDeviceCode());
        request.put("action", "remote_open");
        request.put("timestamp", System.currentTimeMillis());
        return request;
    }

    /**
     * 构建重启请求
     */
    private Map<String, Object> buildRestartRequest(AccessDeviceEntity device) {
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", device.getDeviceCode());
        request.put("action", "restart");
        request.put("timestamp", System.currentTimeMillis());
        return request;
    }

    /**
     * 构建时间同步请求
     */
    private Map<String, Object> buildSyncTimeRequest(AccessDeviceEntity device) {
        Map<String, Object> request = new HashMap<>();
        request.put("deviceId", device.getDeviceCode());
        request.put("action", "sync_time");
        request.put("serverTime", new Date().getTime());
        request.put("timestamp", System.currentTimeMillis());
        return request;
    }

    /**
     * 判断响应是否成功
     */
    private boolean isResponseSuccess(String response) {
        if (response == null) {
            return false;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Object success = responseMap.get("success");
            Object code = responseMap.get("code");

            return (success instanceof Boolean && (Boolean) success) ||
                   (code instanceof Number && ((Number) code).intValue() == 0) ||
                   response.contains("SUCCESS") ||
                   response.contains("OK") ||
                   response.contains("DONE");
        } catch (Exception e) {
            // 如果解析失败，直接检查文本内容
            return response.contains("SUCCESS") ||
                   response.contains("OK") ||
                   response.contains("DONE");
        }
    }

    /**
     * 解析状态响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseStatusResponse(String response) throws IOException {
        try {
            Map<String, Object> status = objectMapper.readValue(response, Map.class);

            // 确保返回的Map包含必要的字段
            if (!status.containsKey("success")) {
                status.put("success", true);
            }

            return status;
        } catch (Exception e) {
            // 如果解析失败，返回基础状态信息
            Map<String, Object> status = new HashMap<>();
            status.put("success", true);
            status.put("rawResponse", response);
            status.put("parseError", e.getMessage());
            return status;
        }
    }

    /**
     * 解析自定义响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseCustomResponse(String response) throws IOException {
        try {
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            result.put("success", isResponseSuccess(response));
            return result;
        } catch (Exception e) {
            // 如果解析失败，返回基础信息
            Map<String, Object> result = new HashMap<>();
            result.put("success", isResponseSuccess(response));
            result.put("rawResponse", response);
            result.put("parseError", e.getMessage());
            return result;
        }
    }
}
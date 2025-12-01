package net.lab1024.sa.admin.module.smart.access.protocol.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolAdapter;
import net.lab1024.sa.admin.module.smart.access.protocol.DeviceProtocolException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * HTTP协议适配器实现
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - 完整的异常处理和日志记录
 * - 超时控制和重试机制
 * <p>
 * 支持HTTP/HTTPS协议的门禁设备通信
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@Slf4j
@Component
public class HttpProtocolAdapter implements DeviceProtocolAdapter {

    /**
     * 默认连接超时时间（毫秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    /**
     * 默认读取超时时间（毫秒）
     */
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 支持的厂商列表
     */
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
            "海康威视", "Hikvision", "大华", "Dahua", "宇视", "Uniview", "通用HTTP设备"
    );

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.HTTP;
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return SUPPORTED_MANUFACTURERS;
    }

    /**
     * HTTP协议远程开门
     * <p>
     * 向设备发送HTTP POST请求执行远程开门操作
     * 性能优化：使用连接超时和读取超时控制，支持重试机制
     *
     * @param device 门禁设备实体，必须包含ipAddress、port、protocol字段
     * @return 是否成功，true表示开门成功，false表示设备返回失败响应
     * @throws DeviceProtocolException 协议调用异常，包括连接失败、HTTP错误、响应解析失败等
     */
    @Override
    public boolean remoteOpen(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议远程开门，deviceId: {}, url: http://{}:{}/api/open", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            // 构建HTTP请求URL（支持HTTP和HTTPS）
            String url = buildRequestUrl(device, "/api/open");
            
            // 构建请求体（JSON格式）
            String requestBody = buildOpenRequestBody(device);

            // 发送HTTP请求（带重试机制）
            String response = sendHttpRequest(device, url, "POST", requestBody);

            // 解析响应
            boolean success = parseOpenResponse(device, response);
            if (success) {
                log.info("HTTP协议远程开门成功，deviceId: {}", device.getAccessDeviceId());
            } else {
                log.warn("HTTP协议远程开门失败，设备返回失败响应，deviceId: {}", device.getAccessDeviceId());
            }
            return success;

        } catch (Exception e) {
            log.error("HTTP协议远程开门异常，deviceId: {}", device.getAccessDeviceId(), e);
            throw new DeviceProtocolException(
                    "HTTP协议远程开门失败: " + e.getMessage(),
                    device.getAccessDeviceId(),
                    ProtocolType.HTTP,
                    "remoteOpen"
            );
        }
    }

    @Override
    public boolean restartDevice(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议重启设备，deviceId: {}, url: http://{}:{}/api/restart", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            String url = buildRequestUrl(device, "/api/restart");
            String requestBody = buildRestartRequestBody(device);
            String response = sendHttpRequest(device, url, "POST", requestBody);

            boolean success = parseRestartResponse(device, response);
            if (success) {
                log.info("HTTP协议重启设备成功，deviceId: {}", device.getAccessDeviceId());
            } else {
                log.warn("HTTP协议重启设备失败，设备返回失败响应，deviceId: {}", device.getAccessDeviceId());
            }
            return success;

        } catch (Exception e) {
            log.error("HTTP协议重启设备异常，deviceId: {}", device.getAccessDeviceId(), e);
            throw new DeviceProtocolException(
                    "HTTP协议重启设备失败: " + e.getMessage(),
                    device.getAccessDeviceId(),
                    ProtocolType.HTTP,
                    "restartDevice"
            );
        }
    }

    @Override
    public boolean syncDeviceTime(AccessDeviceEntity device) throws DeviceProtocolException {
        log.info("HTTP协议同步设备时间，deviceId: {}, url: http://{}:{}/api/sync-time", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            LocalDateTime now = LocalDateTime.now();
            String timeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String url = buildRequestUrl(device, "/api/sync-time");
            String requestBody = buildSyncTimeRequestBody(device, timeString);
            String response = sendHttpRequest(device, url, "POST", requestBody);

            boolean success = parseSyncTimeResponse(device, response);
            if (success) {
                log.info("HTTP协议同步设备时间成功，deviceId: {}, time: {}", 
                        device.getAccessDeviceId(), timeString);
            } else {
                log.warn("HTTP协议同步设备时间失败，设备返回失败响应，deviceId: {}", device.getAccessDeviceId());
            }
            return success;

        } catch (Exception e) {
            log.error("HTTP协议同步设备时间异常，deviceId: {}", device.getAccessDeviceId(), e);
            throw new DeviceProtocolException(
                    "HTTP协议同步设备时间失败: " + e.getMessage(),
                    device.getAccessDeviceId(),
                    ProtocolType.HTTP,
                    "syncDeviceTime"
            );
        }
    }

    @Override
    public boolean checkConnection(AccessDeviceEntity device) throws DeviceProtocolException {
        log.debug("HTTP协议检查设备连接，deviceId: {}, url: http://{}:{}/api/status", 
                device.getAccessDeviceId(), device.getIpAddress(), device.getPort());

        try {
            String url = buildRequestUrl(device, "/api/status");
            String response = sendHttpRequest(device, url, "GET", null);
            return response != null && !response.isEmpty();

        } catch (Exception e) {
            log.warn("HTTP协议检查设备连接失败，deviceId: {}", device.getAccessDeviceId(), e);
            return false;
        }
    }

    /**
     * 构建HTTP请求URL
     *
     * @param device 设备实体
     * @param path API路径
     * @return 完整URL
     */
    private String buildRequestUrl(AccessDeviceEntity device, String path) {
        String protocol = "http";
        if (device.getProtocol() != null && device.getProtocol().toUpperCase().contains("HTTPS")) {
            protocol = "https";
        }
        return String.format("%s://%s:%d%s", protocol, device.getIpAddress(), device.getPort(), path);
    }

    /**
     * 发送HTTP请求（带重试机制）
     *
     * @param device 设备实体
     * @param url 请求URL
     * @param method HTTP方法
     * @param requestBody 请求体
     * @return 响应内容
     * @throws Exception 异常
     */
    private String sendHttpRequest(AccessDeviceEntity device, String url, String method, String requestBody) 
            throws Exception {
        Exception lastException = null;

        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            try {
                return sendHttpRequestOnce(url, method, requestBody, device);
            } catch (Exception e) {
                lastException = e;
                log.warn("HTTP协议发送请求失败，重试 {}/{}，deviceId: {}", 
                        i + 1, MAX_RETRY_COUNT, device.getAccessDeviceId(), e);
                
                if (i < MAX_RETRY_COUNT - 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("重试等待被中断", ie);
                    }
                }
            }
        }

        throw new Exception("HTTP协议发送请求失败，已重试" + MAX_RETRY_COUNT + "次", lastException);
    }

    /**
     * 发送HTTP请求（单次）
     *
     * @param url 请求URL
     * @param method HTTP方法
     * @param requestBody 请求体
     * @param device 设备实体（用于认证）
     * @return 响应内容
     * @throws Exception 异常
     */
    private String sendHttpRequestOnce(String url, String method, String requestBody, AccessDeviceEntity device) 
            throws Exception {
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            
            // 设置请求方法和属性
            connection.setRequestMethod(method);
            connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
            connection.setDoInput(true);
            
            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            
            // 如果有通信密钥，添加到请求头
            if (device.getCommKey() != null && !device.getCommKey().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + device.getCommKey());
            }

            // 发送请求体
            if (requestBody != null && !requestBody.isEmpty()) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                throw new Exception("HTTP请求失败，响应码: " + responseCode);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 构建开门请求体
     *
     * @param device 设备实体
     * @return JSON格式的请求体
     */
    private String buildOpenRequestBody(AccessDeviceEntity device) {
        return String.format("{\"deviceId\":%d,\"timestamp\":%d}", 
                device.getAccessDeviceId(), 
                System.currentTimeMillis());
    }

    /**
     * 构建重启请求体
     *
     * @param device 设备实体
     * @return JSON格式的请求体
     */
    private String buildRestartRequestBody(AccessDeviceEntity device) {
        return String.format("{\"deviceId\":%d,\"timestamp\":%d}", 
                device.getAccessDeviceId(), 
                System.currentTimeMillis());
    }

    /**
     * 构建时间同步请求体
     *
     * @param device 设备实体
     * @param timeString 时间字符串
     * @return JSON格式的请求体
     */
    private String buildSyncTimeRequestBody(AccessDeviceEntity device, String timeString) {
        return String.format("{\"deviceId\":%d,\"time\":\"%s\"}", 
                device.getAccessDeviceId(), 
                timeString);
    }

    /**
     * 解析开门响应
     *
     * @param device 设备实体
     * @param response 响应内容
     * @return 是否成功
     */
    private boolean parseOpenResponse(AccessDeviceEntity device, String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        // 通用JSON响应格式：{"success":true} 或 {"code":0}
        return response.contains("\"success\":true") || 
               response.contains("\"code\":0") ||
               response.contains("\"status\":\"ok\"");
    }

    /**
     * 解析重启响应
     *
     * @param device 设备实体
     * @param response 响应内容
     * @return 是否成功
     */
    private boolean parseRestartResponse(AccessDeviceEntity device, String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        return response.contains("\"success\":true") || 
               response.contains("\"code\":0") ||
               response.contains("\"status\":\"ok\"");
    }

    /**
     * 解析时间同步响应
     *
     * @param device 设备实体
     * @param response 响应内容
     * @return 是否成功
     */
    private boolean parseSyncTimeResponse(AccessDeviceEntity device, String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        return response.contains("\"success\":true") || 
               response.contains("\"code\":0") ||
               response.contains("\"status\":\"ok\"");
    }
}


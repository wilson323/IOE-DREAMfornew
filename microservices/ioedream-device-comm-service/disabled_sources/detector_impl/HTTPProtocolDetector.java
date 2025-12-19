package net.lab1024.sa.device.comm.discovery.detector.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.detector.ProtocolDetector;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * HTTP协议检测器
 * <p>
 * 通过HTTP请求检测设备协议：
 * 1. HTTP请求发送和响应分析
 * 2. HTTP头信息识别
 * 3. 路径特征匹配
 * 4. 内容类型检测
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class HTTPProtocolDetector implements ProtocolDetector {

    private static final List<String> COMMON_PATHS = Arrays.asList(
            "/",
            "/index.html",
            "/index.htm",
            "/default.html",
            "/login.html",
            "/info.html",
            "/status.html",
            "/api/v1/info",
            "/ISAPI/System/deviceInfo",
            "/cgi-bin/global.cgi",
            "/PSIA/System/deviceInfo",
            "/dvr/api/v1/systemInfo",
            "/api/v1/system/info",
            "/api/v2/device/info"
    );

    private static final Map<String, String> VENDOR_SIGNATURES = new HashMap<>();

    static {
        // HTTP服务器厂商签名
        VENDOR_SIGNATURES.put("Hikvision-Webs", "HIKVISION_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("DahuaWEB", "DAHUA_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("Uniview", "UNIVIEW_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("EZVIZ", "EZVIZ_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("Boa", "BOA_WEB_SERVER");
        VENDOR_SIGNATURES.put("nginx", "NGINX");
        VENDOR_SIGNATURES.put("Apache", "APACHE_HTTP_SERVER");
        VENDOR_SIGNATURES.put("Microsoft-IIS", "MICROSOFT_IIS");
    }

    @Override
    public String detectProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout,
                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查HTTP相关端口
            Integer httpPort = findHttpPort(openPorts);
            if (httpPort == null) {
                return null;
            }

            log.debug("[HTTP检测器] 检测HTTP协议: {} - 端口: {}", ipAddress, httpPort);

            // 尝试HTTP连接
            String detectedProtocol = detectByHTTP(ipAddress, httpPort, timeout, fingerprints);
            if (detectedProtocol != null && !"UNKNOWN".equals(detectedProtocol)) {
                return detectedProtocol;
            }

            return null;

        } catch (Exception e) {
            log.error("[HTTP检测器] 协议检测异常: {}", ipAddress, e);
            return null;
        }
    }

    @Override
    public String getDetectorType() {
        return "HTTP";
    }

    @Override
    public String getDescription() {
        return "HTTP协议检测器";
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("HIKVISION_VIDEO_V2_0", "DAHUA_VIDEO_V2_0", "UNIVIEW_VIDEO_V2_0", "EZVIZ_VIDEO_V2_0");
    }

    /**
     * 通过HTTP连接检测协议
     */
    private String detectByHTTP(String ipAddress, int port, int timeout,
                                Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            for (String path : COMMON_PATHS) {
                String url = String.format("http://%s:%d%s", ipAddress, port, path);
                String detectedProtocol = testHTTPConnection(url, timeout, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }
            }
        } catch (Exception e) {
            log.debug("[HTTP检测器] HTTP连接检测失败", e);
        }
        return null;
    }

    /**
     * 测试HTTP连接
     */
    private String testHTTPConnection(String urlString, int timeout,
                                        Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setInstanceFollowRedirects(false);

            // 设置用户代理
            connection.setRequestProperty("User-Agent", "IOE-DREAM-Device-Discovery/1.0");
            connection.setRequestProperty("Accept", "*/*");

            // 发送请求
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK ||
                responseCode == HttpURLConnection.HTTP_UNAUTHORIZED ||
                responseCode == HttpURLConnection.HTTP_FORBIDDEN) {

                // 分析响应头
                Map<String, String> headers = connection.getHeaderFields();
                String detectedProtocol = analyzeHTTPHeaders(headers, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                // 读取响应内容
                String content = readResponseContent(connection);
                if (content != null) {
                    detectedProtocol = analyzeResponseContent(content, fingerprints);
                    if (detectedProtocol != null) {
                        return detectedProtocol;
                    }
                }

                // 如果路径匹配某个指纹
                detectedProtocol = analyzeURLPath(urlString, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }
            }

        } catch (Exception e) {
            log.debug("[HTTP检测器] HTTP连接异常: {} - {}", urlString, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    /**
     * 分析HTTP响应头
     */
    private String analyzeHTTPHeaders(Map<String, String> headers,
                                         Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查Server头
            String serverHeader = headers.get("Server");
            if (serverHeader != null) {
                String detectedProtocol = VENDOR_SIGNATURES.get(serverHeader);
                if (detectedProtocol != null) {
                    log.debug("[HTTP检测器] 通过Server头检测到协议: {} - {}", serverHeader, detectedProtocol);
                    return detectedProtocol;
                }
            }

            // 检查其他厂商特征头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String headerName = entry.getKey().toLowerCase();
                String headerValue = entry.getValue();

                // 检查所有指纹
                for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                    ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                    if (fingerprint.getHttpHeaders() != null) {
                        for (String pattern : fingerprint.getHttpHeaders()) {
                            if (headerValue.toLowerCase().contains(pattern.toLowerCase())) {
                                log.debug("[HTTP检测器] 通过HTTP头特征检测到协议: {} - {}:{}",
                                        fingerprintEntry.getKey(), headerName, pattern);
                                return fingerprintEntry.getKey();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[HTTP检测器] HTTP头分析异常", e);
        }

        return null;
    }

    /**
     * 分析响应内容
     */
    private String analyzeResponseContent(String content,
                                          Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            content = content.toLowerCase();

            // 检查所有指纹
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getHttpPaths() != null) {
                    for (String path : fingerprint.getHttpPaths()) {
                        if (content.contains(path.toLowerCase())) {
                            log.debug("[HTTP检测器] 通过内容特征检测到协议: {} - {}", fingerprintEntry.getKey(), path);
                            return fingerprintEntry.getKey();
                        }
                    }
                }
            }

            // 检查JSON响应中的特定字段
            if (content.contains("devicename") || content.contains("deviceName") ||
                content.contains("serialnumber") || content.contains("serialNumber") ||
                content.contains("firmwareversion") || content.contains("firmwareVersion")) {
                // 可能是设备API响应，但无法确定具体厂商
                log.debug("[HTTP检测器] 检测到设备API响应，但无法确定厂商");
                return "DEVICE_API";
            }

        } catch (Exception e) {
            log.debug("[HTTP检测器] 响应内容分析异常", e);
        }

        return null;
    }

    /**
     * 分析URL路径
     */
    private String analyzeURLPath(String urlString,
                                   Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getHttpPaths() != null) {
                    for (String path : fingerprint.getHttpPaths()) {
                        if (urlString.endsWith(path) || urlString.contains(path)) {
                            log.debug("[HTTP检测器] 通过URL路径检测到协议: {} - {}", fingerprintEntry.getKey(), path);
                            return fingerprintEntry.getKey();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[HTTP检测器] URL路径分析异常", e);
        }

        return null;
    }

    /**
     * 读取响应内容
     */
    private String readResponseContent(HttpURLConnection connection) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                // 限制读取内容大小
                if (content.length() > 8192) {
                    break;
                }
            }
        }
        return content.toString();
    }

    /**
     * 查找HTTP端口
     */
    private Integer findHttpPort(Map<Integer, String> openPorts) {
        // 优先检查标准HTTP端口
        if (openPorts.containsKey(80)) {
            return 80;
        }
        if (openPorts.containsKey(443)) {
            return 443;
        }
        if (openPorts.containsKey(8080)) {
            return 8080;
        }
        if (openPorts.containsKey(8088)) {
            return 8088;
        }

        // 检查其他可能为HTTP的端口
        for (Map.Entry<Integer, String> entry : openPorts.entrySet()) {
            String service = entry.getValue();
            if (service != null && (
                    service.startsWith("HTTP") ||
                    service.contains("WEB") ||
                    service.contains("API"))) {
                return entry.getKey();
            }
        }

        return null;
    }
}
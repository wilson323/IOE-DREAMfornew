package net.lab1024.sa.device.comm.discovery.detector.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.detector.ProtocolDetector;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RTSP协议检测器
 * <p>
 * 通过RTSP协议检测视频监控设备：
 * 1. RTSP连接测试和响应分析
 * 2. RTSP头信息识别
 * 3. 支持的RTSP命令检测
 * 4. 视频流格式识别
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class RTSPProtocolDetector implements ProtocolDetector {

    private static final List<Integer> RTSP_PORTS = Arrays.asList(554, 8554, 1935, 8000, 9000);

    private static final Map<String, String> VENDOR_SIGNATURES = new HashMap<>();

    // RTSP标准命令
    private static final List<String> RTSP_COMMANDS = Arrays.asList(
            "OPTIONS", "DESCRIBE", "SETUP", "PLAY", "PAUSE", "TEARDOWN"
    );

    static {
        // RTSP服务器厂商签名
        VENDOR_SIGNATURES.put("Hikvision-Webs", "HIKVISION_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("DahuaWEB", "DAHUA_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("Uniview", "UNIVIEW_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("EZVIZ", "EZVIZ_VIDEO_V2_0");
        VENDOR_SIGNATURES.put("Axis", "AXIS_CAMERA");
        VENDOR_SIGNATURES.put("Sony", "SONY_CAMERA");
        VENDOR_SIGNATURES.put("Bosch", "BOSCH_CAMERA");
        VENDOR_SIGNATURES.put("Panasonic", "PANASONIC_CAMERA");
        VENDOR_SIGNATURES.put("Pelco", "PELCO_CAMERA");
        VENDOR_SIGNATURES.put("Vivotek", "VIVOTEK_CAMERA");
        VENDOR_SIGNATURES.put("Grandstream", "GRANDSTREAM_CAMERA");
        VENDOR_SIGNATURES.put("Ubiquiti", "UBIQUITI_CAMERA");
    }

    @Override
    public String detectProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout,
                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查RTSP相关端口
            Integer rtspPort = findRtspPort(openPorts);
            if (rtspPort == null) {
                return null;
            }

            log.debug("[RTSP检测器] 检测RTSP协议: {} - 端口: {}", ipAddress, rtspPort);

            // 尝试RTSP连接
            String detectedProtocol = detectByRTSP(ipAddress, rtspPort, timeout, fingerprints);
            if (detectedProtocol != null && !"UNKNOWN".equals(detectedProtocol)) {
                return detectedProtocol;
            }

            return null;

        } catch (Exception e) {
            log.error("[RTSP检测器] 协议检测异常: {}", ipAddress, e);
            return null;
        }
    }

    @Override
    public String getDetectorType() {
        return "RTSP";
    }

    @Override
    public String getDescription() {
        return "RTSP协议检测器";
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("HIKVISION_VIDEO_V2_0", "DAHUA_VIDEO_V2_0", "UNIVIEW_VIDEO_V2_0",
                          "EZVIZ_VIDEO_V2_0", "AXIS_CAMERA", "SONY_CAMERA", "BOSCH_CAMERA");
    }

    /**
     * 通过RTSP连接检测协议
     */
    private String detectByRTSP(String ipAddress, int port, int timeout,
                                Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        Socket socket = null;
        try {
            // 创建RTSP连接
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(ipAddress, port), timeout);
            socket.setSoTimeout(timeout);

            // 发送RTSP OPTIONS命令
            String optionsCommand = String.format("OPTIONS rtsp://%s:%d/ RTSP/1.0\r\n" +
                    "CSeq: 1\r\n" +
                    "User-Agent: IOE-DREAM-Device-Discovery/1.0\r\n" +
                    "\r\n", ipAddress, port);

            socket.getOutputStream().write(optionsCommand.getBytes());
            socket.getOutputStream().flush();

            // 读取响应
            String response = readRTSPResponse(socket);
            if (response != null) {
                // 分析RTSP响应头
                String detectedProtocol = analyzeRTSPHeaders(response, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                // 尝试DESCRIBE命令获取更多信息
                detectedProtocol = tryDescribeCommand(ipAddress, port, timeout, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }
            }

        } catch (Exception e) {
            log.debug("[RTSP检测器] RTSP连接异常: {}:{} - {}", ipAddress, port, e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }

        return null;
    }

    /**
     * 读取RTSP响应
     */
    private String readRTSPResponse(Socket socket) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
            // 空行表示响应头结束
            if (line.trim().isEmpty()) {
                break;
            }
            // 限制响应大小
            if (response.length() > 4096) {
                break;
            }
        }

        return response.toString();
    }

    /**
     * 分析RTSP响应头
     */
    private String analyzeRTSPHeaders(String response,
                                       Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查RTSP版本
            if (response.contains("RTSP/1.0") || response.contains("RTSP/1.1") || response.contains("RTSP/2.0")) {

                // 检查Server头
                String serverHeader = extractHeader(response, "Server");
                if (serverHeader != null) {
                    String detectedProtocol = VENDOR_SIGNATURES.get(serverHeader);
                    if (detectedProtocol != null) {
                        log.debug("[RTSP检测器] 通过Server头检测到协议: {} - {}", serverHeader, detectedProtocol);
                        return detectedProtocol;
                    }
                }

                // 检查厂商特征头
                for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                    if (response.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        log.debug("[RTSP检测器] 通过响应特征检测到协议: {} - {}", entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }

                // 检查所有指纹
                for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                    ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                    if (fingerprint.getRtspHeaders() != null) {
                        for (String pattern : fingerprint.getRtspHeaders()) {
                            if (response.toLowerCase().contains(pattern.toLowerCase())) {
                                log.debug("[RTSP检测器] 通过RTSP头特征检测到协议: {} - {}",
                                        fingerprintEntry.getKey(), pattern);
                                return fingerprintEntry.getKey();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[RTSP检测器] RTSP头分析异常", e);
        }

        return null;
    }

    /**
     * 尝试DESCRIBE命令
     */
    private String tryDescribeCommand(String ipAddress, int port, int timeout,
                                     Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(ipAddress, port), timeout);
            socket.setSoTimeout(timeout);

            // 发送RTSP DESCRIBE命令
            String describeCommand = String.format("DESCRIBE rtsp://%s:%d/ RTSP/1.0\r\n" +
                    "CSeq: 2\r\n" +
                    "User-Agent: IOE-DREAM-Device-Discovery/1.0\r\n" +
                    "Accept: application/sdp\r\n" +
                    "\r\n", ipAddress, port);

            socket.getOutputStream().write(describeCommand.getBytes());
            socket.getOutputStream().flush();

            // 读取SDP响应
            String response = readRTSPResponse(socket);
            if (response != null && response.contains("application/sdp")) {
                // 分析SDP内容
                String detectedProtocol = analyzeSDPContent(response, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }
            }

        } catch (Exception e) {
            log.debug("[RTSP检测器] DESCRIBE命令异常: {}:{}", ipAddress, port, e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }

        return null;
    }

    /**
     * 分析SDP内容
     */
    private String analyzeSDPContent(String sdpContent,
                                     Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查视频编码格式
            if (sdpContent.contains("H264") || sdpContent.contains("H.264")) {
                log.debug("[RTSP检测器] 检测到H264视频格式");
            }

            if (sdpContent.contains("H265") || sdpContent.contains("H.265")) {
                log.debug("[RTSP检测器] 检测到H265视频格式");
            }

            // 检查厂商特定的SDP属性
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getRtspPaths() != null) {
                    for (String path : fingerprint.getRtspPaths()) {
                        if (sdpContent.toLowerCase().contains(path.toLowerCase())) {
                            log.debug("[RTSP检测器] 通过SDP内容检测到协议: {} - {}", fingerprintEntry.getKey(), path);
                            return fingerprintEntry.getKey();
                        }
                    }
                }
            }

            // 检查通用摄像头特征
            if (sdpContent.contains("video") || sdpContent.contains("audio")) {
                log.debug("[RTSP检测器] 检测到视频流设备，但无法确定具体厂商");
                return "RTSP_CAMERA";
            }

        } catch (Exception e) {
            log.debug("[RTSP检测器] SDP内容分析异常", e);
        }

        return null;
    }

    /**
     * 提取RTSP头
     */
    private String extractHeader(String response, String headerName) {
        Pattern pattern = Pattern.compile(headerName + ":\\s*(.+?)\\r?\\n", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 查找RTSP端口
     */
    private Integer findRtspPort(Map<Integer, String> openPorts) {
        // 优先检查标准RTSP端口
        for (Integer port : RTSP_PORTS) {
            if (openPorts.containsKey(port)) {
                return port;
            }
        }

        // 检查其他可能的RTSP端口
        for (Map.Entry<Integer, String> entry : openPorts.entrySet()) {
            String service = entry.getValue();
            if (service != null && (
                    service.toUpperCase().contains("RTSP") ||
                    service.toUpperCase().contains("STREAMING") ||
                    service.toUpperCase().contains("VIDEO"))) {
                return entry.getKey();
            }
        }

        return null;
    }
}
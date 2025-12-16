package net.lab1024.sa.device.comm.discovery.detector.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.detector.ProtocolDetector;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ONVIF协议检测器
 * <p>
 * 通过ONVIF协议检测网络摄像头设备：
 * 1. ONVIF设备发现服务
 * 2. GetCapabilities操作测试
 * 3. GetDeviceInformation操作
 * 4. ONVIF厂商和型号识别
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class ONVIFProtocolDetector implements ProtocolDetector {

    private static final List<Integer> ONVIF_PORTS = Arrays.asList(80, 8080, 8000, 8800, 8888, 554, 8001);

    private static final Map<String, String> VENDOR_SIGNATURES = new HashMap<>();

    // ONVIF服务命名空间
    private static final String ONVIF_DEVICE_NAMESPACE = "http://www.onvif.org/ver10/device/wsdl";
    private static final String ONVIF_MEDIA_NAMESPACE = "http://www.onvif.org/ver10/media/wsdl";
    private static final String ONVIF_PTZ_NAMESPACE = "http://www.onvif.org/ver20/ptz/wsdl";

    // ONVIF标准操作
    private static final String GET_CAPABILITIES = "GetCapabilities";
    private static final String GET_DEVICE_INFORMATION = "GetDeviceInformation";
    private static final String GET_SERVICES = "GetServices";

    // ONVIF发现服务地址
    private static final String ONVIF_DISCOVERY_ADDRESS = "239.255.255.250";
    private static final int ONVIF_DISCOVERY_PORT = 3702;

    static {
        // ONVIF设备厂商签名
        VENDOR_SIGNATURES.put("Hikvision", "HIKVISION_ONVIF");
        VENDOR_SIGNATURES.put("Dahua", "DAHUA_ONVIF");
        VENDOR_SIGNATURES.put("Uniview", "UNIVIEW_ONVIF");
        VENDOR_SIGNATURES.put("Axis", "AXIS_ONVIF");
        VENDOR_SIGNATURES.put("Sony", "SONY_ONVIF");
        VENDOR_SIGNATURES.put("Bosch", "BOSCH_ONVIF");
        VENDOR_SIGNATURES.put("Panasonic", "PANASONIC_ONVIF");
        VENDOR_SIGNATURES.put("Pelco", "PELCO_ONVIF");
        VENDOR_SIGNATURES.put("Vivotek", "VIVOTEK_ONVIF");
        VENDOR_SIGNATURES.put("Grandstream", "GRANDSTREAM_ONVIF");
        VENDOR_SIGNATURES.put("Ubiquiti", "UBIQUITI_ONVIF");
        VENDOR_SIGNATURES.put("Honeywell", "HONEYWELL_ONVIF");
        VENDOR_SIGNATURES.put("Avigilon", "AVIGILON_ONVIF");
        VENDOR_SIGNATURES.put("Flir", "FLIR_ONVIF");
        VENDOR_SIGNATURES.put("GeoVision", "GEOVISION_ONVIF");
    }

    @Override
    public String detectProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout,
                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查ONVIF相关端口
            Integer onvifPort = findOnvifPort(openPorts);
            if (onvifPort == null) {
                return null;
            }

            log.debug("[ONVIF检测器] 检测ONVIF协议: {} - 端口: {}", ipAddress, onvifPort);

            // 尝试ONVIF连接
            String detectedProtocol = detectByONVIF(ipAddress, onvifPort, timeout, fingerprints);
            if (detectedProtocol != null && !"UNKNOWN".equals(detectedProtocol)) {
                return detectedProtocol;
            }

            // 如果直接检测失败，尝试WS-Discovery
            detectedProtocol = tryWSDiscovery(ipAddress, timeout, fingerprints);
            if (detectedProtocol != null) {
                return detectedProtocol;
            }

            return null;

        } catch (Exception e) {
            log.error("[ONVIF检测器] 协议检测异常: {}", ipAddress, e);
            return null;
        }
    }

    @Override
    public String getDetectorType() {
        return "ONVIF";
    }

    @Override
    public String getDescription() {
        return "ONVIF协议检测器";
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("HIKVISION_ONVIF", "DAHUA_ONVIF", "UNIVIEW_ONVIF", "AXIS_ONVIF",
                          "SONY_ONVIF", "BOSCH_ONVIF", "PANASONIC_ONVIF", "PELCO_ONVIF");
    }

    /**
     * 通过ONVIF连接检测协议
     */
    private String detectByONVIF(String ipAddress, int port, int timeout,
                                  Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 尝试GetCapabilities操作
            String capabilitiesResponse = sendOnvifRequest(ipAddress, port, GET_CAPABILITIES, timeout);
            if (capabilitiesResponse != null) {
                log.debug("[ONVIF检测器] 获取ONVIF能力成功");

                // 分析能力响应
                String detectedProtocol = analyzeCapabilitiesResponse(capabilitiesResponse, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                // 检查厂商签名
                for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                    if (capabilitiesResponse.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        log.debug("[ONVIF检测器] 通过能力响应检测到协议: {} - {}", entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }
            }

            // 尝试GetDeviceInformation操作
            String deviceInfoResponse = sendOnvifRequest(ipAddress, port, GET_DEVICE_INFORMATION, timeout);
            if (deviceInfoResponse != null) {
                log.debug("[ONVIF检测器] 获取设备信息成功");

                // 分析设备信息响应
                String detectedProtocol = analyzeDeviceInfoResponse(deviceInfoResponse, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                // 检查厂商签名
                for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                    if (deviceInfoResponse.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        log.debug("[ONVIF检测器] 通过设备信息检测到协议: {} - {}", entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }
            }

            // 检查指纹
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getOnvifServices() != null) {
                    for (String service : fingerprint.getOnvifServices()) {
                        // 尝试访问特定ONVIF服务
                        if (testOnvifService(ipAddress, port, service, timeout)) {
                            log.debug("[ONVIF检测器] 通过ONVIF服务检测到协议: {} - {}", fingerprintEntry.getKey(), service);
                            return fingerprintEntry.getKey();
                        }
                    }
                }
            }

            log.debug("[ONVIF检测器] 检测到ONVIF设备，但无法确定具体厂商");
            return "ONVIF_CAMERA";

        } catch (Exception e) {
            log.debug("[ONVIF检测器] ONVIF连接异常: {}:{} - {}", ipAddress, port, e.getMessage());
        }

        return null;
    }

    /**
     * 尝试WS-Discovery
     */
    private String tryWSDiscovery(String ipAddress, int timeout,
                                  Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 发送WS-Discovery探测消息
            String discoveryResponse = sendWSDiscoveryProbe(ipAddress, timeout);
            if (discoveryResponse != null) {
                log.debug("[ONVIF检测器] WS-Discovery响应成功");

                // 分析发现响应
                String detectedProtocol = analyzeDiscoveryResponse(discoveryResponse, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                // 检查厂商签名
                for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                    if (discoveryResponse.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        log.debug("[ONVIF检测器] 通过WS-Discovery检测到协议: {} - {}", entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }

                return "ONVIF_DISCOVERED";
            }

        } catch (Exception e) {
            log.debug("[ONVIF检测器] WS-Discovery异常", e);
        }

        return null;
    }

    /**
     * 发送ONVIF请求
     */
    private String sendOnvifRequest(String ipAddress, int port, String operation, int timeout) {
        try {
            // 构造SOAP请求
            String soapRequest = buildOnvifSoapRequest(operation);

            // 发送HTTP请求
            URL url = new URL(String.format("http://%s:%d/onvif/device_service", ipAddress, port));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
            connection.setRequestProperty("SOAPAction", String.format("\"%s/%s\"", ONVIF_DEVICE_NAMESPACE, operation));

            connection.setDoOutput(true);
            connection.getOutputStream().write(soapRequest.getBytes("UTF-8"));
            connection.getOutputStream().flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                // 读取响应
                return readHttpResponse(connection);
            }

        } catch (Exception e) {
            log.debug("[ONVIF检测器] 发送ONVIF请求异常: {} - operation: {}", ipAddress, operation, e.getMessage());
        }

        return null;
    }

    /**
     * 构造ONVIF SOAP请求
     */
    private String buildOnvifSoapRequest(String operation) {
        StringBuilder soap = new StringBuilder();
        soap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        soap.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n");
        soap.append("  <soap:Body>\n");

        switch (operation) {
            case GET_CAPABILITIES:
                soap.append("    <tds:GetCapabilities xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\">\n");
                soap.append("      <tds:Category>All</tds:Category>\n");
                soap.append("    </tds:GetCapabilities>\n");
                break;
            case GET_DEVICE_INFORMATION:
                soap.append("    <tds:GetDeviceInformation xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\">\n");
                soap.append("    </tds:GetDeviceInformation>\n");
                break;
            case GET_SERVICES:
                soap.append("    <tds:GetServices xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\">\n");
                soap.append("      <tds:IncludeCapability>true</tds:IncludeCapability>\n");
                soap.append("    </tds:GetServices>\n");
                break;
        }

        soap.append("  </soap:Body>\n");
        soap.append("</soap:Envelope>\n");

        return soap.toString();
    }

    /**
     * 读取HTTP响应
     */
    private String readHttpResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                // 限制响应大小
                if (response.length() > 8192) {
                    break;
                }
            }
        }
        return response.toString();
    }

    /**
     * 发送WS-Discovery探测
     */
    private String sendWSDiscoveryProbe(String ipAddress, int timeout) {
        try {
            // 构造WS-Discovery探测消息
            String probeMessage = buildWSDiscoveryProbe();

            // 发送UDP组播
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);

            byte[] messageBytes = probeMessage.getBytes("UTF-8");
            InetAddress groupAddress = InetAddress.getByName(ONVIF_DISCOVERY_ADDRESS);
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, groupAddress, ONVIF_DISCOVERY_PORT);

            socket.send(packet);

            // 等待响应
            byte[] responseBuffer = new byte[8192];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

            try {
                socket.receive(responsePacket);
                String response = new String(responsePacket.getData(), 0, responsePacket.getLength(), "UTF-8");

                // 检查是否来自目标IP
                if (responsePacket.getAddress().getHostAddress().equals(ipAddress)) {
                    return response;
                }
            } catch (SocketTimeoutException e) {
                // 超时是正常的，可能没有设备响应
            }

            socket.close();

        } catch (Exception e) {
            log.debug("[ONVIF检测器] WS-Discovery探测异常", e);
        }

        return null;
    }

    /**
     * 构造WS-Discovery探测消息
     */
    private String buildWSDiscoveryProbe() {
        StringBuilder probe = new StringBuilder();
        probe.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        probe.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" ");
        probe.append("xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" ");
        probe.append("xmlns:wsd=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\">\n");
        probe.append("  <soap:Header>\n");
        probe.append("    <wsa:MessageID>urn:uuid:").append(UUID.randomUUID().toString()).append("</wsa:MessageID>\n");
        probe.append("    <wsa:To>urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>\n");
        probe.append("    <wsa:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>\n");
        probe.append("  </soap:Header>\n");
        probe.append("  <soap:Body>\n");
        probe.append("    <wsd:Probe>\n");
        probe.append("      <wsd:Types>dn:NetworkVideoTransmitter</wsd:Types>\n");
        probe.append("    </wsd:Probe>\n");
        probe.append("  </soap:Body>\n");
        probe.append("</soap:Envelope>\n");

        return probe.toString();
    }

    /**
     * 分析能力响应
     */
    private String analyzeCapabilitiesResponse(String response,
                                              Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查ONVIF命名空间
            if (response.contains(ONVIF_DEVICE_NAMESPACE) ||
                response.contains(ONVIF_MEDIA_NAMESPACE) ||
                response.contains(ONVIF_PTZ_NAMESPACE)) {

                log.debug("[ONVIF检测器] 检测到标准ONVIF命名空间");

                // 检查指纹
                for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                    ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                    if (fingerprint.getOnvifServices() != null) {
                        for (String service : fingerprint.getOnvifServices()) {
                            if (response.toLowerCase().contains(service.toLowerCase())) {
                                log.debug("[ONVIF检测器] 通过能力服务检测到协议: {} - {}", fingerprintEntry.getKey(), service);
                                return fingerprintEntry.getKey();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[ONVIF检测器] 能力响应分析异常", e);
        }

        return null;
    }

    /**
     * 分析设备信息响应
     */
    private String analyzeDeviceInfoResponse(String response,
                                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查设备信息字段
            if (response.contains("Manufacturer") || response.contains("Model") || response.contains("FirmwareVersion")) {
                log.debug("[ONVIF检测器] 检测到ONVIF设备信息");

                // 检查指纹
                for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                    ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                    if (fingerprint.getDeviceDescriptions() != null) {
                        for (String description : fingerprint.getDeviceDescriptions()) {
                            if (response.toLowerCase().contains(description.toLowerCase())) {
                                log.debug("[ONVIF检测器] 通过设备信息特征检测到协议: {} - {}", fingerprintEntry.getKey(), description);
                                return fingerprintEntry.getKey();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[ONVIF检测器] 设备信息响应分析异常", e);
        }

        return null;
    }

    /**
     * 分析发现响应
     */
    private String analyzeDiscoveryResponse(String response,
                                           Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查WS-Discovery响应
            if (response.contains("ProbeMatches") || response.contains("NetworkVideoTransmitter")) {
                log.debug("[ONVIF检测器] 检测到WS-Discovery设备");

                // 检查指纹
                for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                    ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                    if (fingerprint.getOnvifServices() != null) {
                        for (String service : fingerprint.getOnvifServices()) {
                            if (response.toLowerCase().contains(service.toLowerCase())) {
                                log.debug("[ONVIF检测器] 通过发现服务检测到协议: {} - {}", fingerprintEntry.getKey(), service);
                                return fingerprintEntry.getKey();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[ONVIF检测器] 发现响应分析异常", e);
        }

        return null;
    }

    /**
     * 测试ONVIF服务
     */
    private boolean testOnvifService(String ipAddress, int port, String service, int timeout) {
        try {
            URL url = new URL(String.format("http://%s:%d/%s", ipAddress, port, service));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK ||
                   responseCode == HttpURLConnection.HTTP_BAD_REQUEST; // 400也表示服务存在

        } catch (Exception e) {
            log.debug("[ONVIF检测器] 测试ONVIF服务异常: {} - {}", service, e.getMessage());
        }

        return false;
    }

    /**
     * 查找ONVIF端口
     */
    private Integer findOnvifPort(Map<Integer, String> openPorts) {
        // 优先检查标准ONVIF端口
        for (Integer port : ONVIF_PORTS) {
            if (openPorts.containsKey(port)) {
                return port;
            }
        }

        // 检查其他可能的ONVIF端口
        for (Map.Entry<Integer, String> entry : openPorts.entrySet()) {
            String service = entry.getValue();
            if (service != null && (
                    service.toUpperCase().contains("HTTP") ||  // ONVIF基于HTTP
                    service.toUpperCase().contains("ONVIF") ||
                    service.toUpperCase().contains("WEBSERVICE") ||
                    service.toUpperCase().contains("SOAP"))) {
                return entry.getKey();
            }
        }

        return null;
    }
}
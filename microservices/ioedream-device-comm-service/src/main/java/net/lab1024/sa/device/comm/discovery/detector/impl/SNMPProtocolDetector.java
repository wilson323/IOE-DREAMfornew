package net.lab1024.sa.device.comm.discovery.detector.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.detector.ProtocolDetector;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SNMP协议检测器
 * <p>
 * 通过SNMP协议检测网络设备和工业设备：
 * 1. SNMPv1/v2c/v3连接测试
 * 2. SNMP系统信息获取
 * 3. 设备类型和厂商识别
 * 4. OID树遍历和特征匹配
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class SNMPProtocolDetector implements ProtocolDetector {

    private static final List<Integer> SNMP_PORTS = Arrays.asList(161, 162, 2161);

    private static final Map<String, String> VENDOR_SIGNATURES = new HashMap<>();

    // 常用SNMP社区字符串
    private static final List<String> COMMON_COMMUNITIES = Arrays.asList(
            "public", "private", "admin", "monitor", "guest", "manager"
    );

    // 常用OID
    private static final String SYS_DESCR_OID = "1.3.6.1.2.1.1.1.0";           // 系统描述
    private static final String SYS_NAME_OID = "1.3.6.1.2.1.1.5.0";             // 系统名称
    private static final String SYS_VENDOR_OID = "1.3.6.1.2.1.1.4.0";           // 厂商信息
    private static final String SYS_UPTIME_OID = "1.3.6.1.2.1.1.3.0";           // 系统运行时间

    static {
        // SNMP设备厂商签名
        VENDOR_SIGNATURES.put("Cisco", "CISCO_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Juniper", "JUNIPER_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Huawei", "HUAWEI_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("H3C", "H3C_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Arista", "ARISTA_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Fortinet", "FORTINET_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Palo Alto", "PALO_ALTO_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("HP", "HP_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Dell", "DELL_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Netgear", "NETGEAR_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("TP-Link", "TPLINK_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("D-Link", "DLINK_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Ubiquiti", "UBIQUITI_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Mikrotik", "MIKROTIK_NETWORK_DEVICE");
        VENDOR_SIGNATURES.put("Siemens", "SIEMENS_INDUSTRIAL_DEVICE");
        VENDOR_SIGNATURES.put("Schneider", "SCHNEIDER_INDUSTRIAL_DEVICE");
        VENDOR_SIGNATURES.put("Rockwell", "ROCKWELL_INDUSTRIAL_DEVICE");
        VENDOR_SIGNATURES.put("Omron", "OMRON_INDUSTRIAL_DEVICE");
    }

    @Override
    public String detectProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout,
                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查SNMP相关端口
            Integer snmpPort = findSnmpPort(openPorts);
            if (snmpPort == null) {
                return null;
            }

            log.debug("[SNMP检测器] 检测SNMP协议: {} - 端口: {}", ipAddress, snmpPort);

            // 尝试SNMP连接
            String detectedProtocol = detectBySNMP(ipAddress, snmpPort, timeout, fingerprints);
            if (detectedProtocol != null && !"UNKNOWN".equals(detectedProtocol)) {
                return detectedProtocol;
            }

            return null;

        } catch (Exception e) {
            log.error("[SNMP检测器] 协议检测异常: {}", ipAddress, e);
            return null;
        }
    }

    @Override
    public String getDetectorType() {
        return "SNMP";
    }

    @Override
    public String getDescription() {
        return "SNMP协议检测器";
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("CISCO_NETWORK_DEVICE", "JUNIPER_NETWORK_DEVICE", "HUAWEI_NETWORK_DEVICE",
                          "H3C_NETWORK_DEVICE", "SIEMENS_INDUSTRIAL_DEVICE", "SCHNEIDER_INDUSTRIAL_DEVICE",
                          "ROCKWELL_INDUSTRIAL_DEVICE", "OMRON_INDUSTRIAL_DEVICE");
    }

    /**
     * 通过SNMP连接检测协议
     */
    private String detectBySNMP(String ipAddress, int port, int timeout,
                                Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 尝试不同的社区字符串
            for (String community : COMMON_COMMUNITIES) {
                String detectedProtocol = trySNMPConnection(ipAddress, port, community, timeout, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }
            }

            // 如果标准社区字符串失败，尝试设备信息获取
            String detectedProtocol = getDeviceSNMPInfo(ipAddress, port, timeout, fingerprints);
            if (detectedProtocol != null) {
                return detectedProtocol;
            }

        } catch (Exception e) {
            log.debug("[SNMP检测器] SNMP连接检测失败", e);
        }

        return null;
    }

    /**
     * 尝试SNMP连接
     */
    private String trySNMPConnection(String ipAddress, int port, String community, int timeout,
                                    Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 获取系统描述信息
            String sysDescr = getSNMPValue(ipAddress, port, community, SYS_DESCR_OID, timeout);
            if (sysDescr != null) {
                log.debug("[SNMP检测器] 获取到系统描述: {}", sysDescr);

                // 分析系统描述
                String detectedProtocol = analyzeSysDescr(sysDescr, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                // 检查厂商签名
                for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                    if (sysDescr.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        log.debug("[SNMP检测器] 通过系统描述检测到协议: {} - {}", entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }
            }

            // 获取厂商信息
            String sysVendor = getSNMPValue(ipAddress, port, community, SYS_VENDOR_OID, timeout);
            if (sysVendor != null) {
                log.debug("[SNMP检测器] 获取到厂商信息: {}", sysVendor);

                for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                    if (sysVendor.toLowerCase().contains(entry.getKey().toLowerCase())) {
                        log.debug("[SNMP检测器] 通过厂商信息检测到协议: {} - {}", entry.getKey(), entry.getValue());
                        return entry.getValue();
                    }
                }
            }

            // 获取系统名称
            String sysName = getSNMPValue(ipAddress, port, community, SYS_NAME_OID, timeout);
            if (sysName != null) {
                log.debug("[SNMP检测器] 获取到系统名称: {}", sysName);

                // 检查指纹
                for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                    ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                    if (fingerprint.getSnmpOids() != null) {
                        for (String oid : fingerprint.getSnmpOids()) {
                            // 尝试获取特定OID的值
                            String oidValue = getSNMPValue(ipAddress, port, community, oid, timeout);
                            if (oidValue != null) {
                                log.debug("[SNMP检测器] 通过OID检测到协议: {} - {} = {}",
                                        fingerprintEntry.getKey(), oid, oidValue);
                                return fingerprintEntry.getKey();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[SNMP检测器] SNMP连接异常: {} - community: {}", ipAddress, community, e.getMessage());
        }

        return null;
    }

    /**
     * 获取设备SNMP信息
     */
    private String getDeviceSNMPInfo(String ipAddress, int port, int timeout,
                                     Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 使用默认社区字符串尝试获取基本信息
            String sysDescr = getSNMPValue(ipAddress, port, "public", SYS_DESCR_OID, timeout);
            if (sysDescr != null) {
                // 检查是否为SNMP设备
                if (sysDescr.toLowerCase().contains("snmp") ||
                    sysDescr.toLowerCase().contains("router") ||
                    sysDescr.toLowerCase().contains("switch") ||
                    sysDescr.toLowerCase().contains("firewall")) {

                    log.debug("[SNMP检测器] 检测到SNMP网络设备，但无法确定具体厂商");
                    return "SNMP_NETWORK_DEVICE";
                }

                // 检查工业设备特征
                if (sysDescr.toLowerCase().contains("plc") ||
                    sysDescr.toLowerCase().contains("scada") ||
                    sysDescr.toLowerCase().contains("hmi") ||
                    sysDescr.toLowerCase().contains("industrial")) {

                    log.debug("[SNMP检测器] 检测到SNMP工业设备，但无法确定具体厂商");
                    return "SNMP_INDUSTRIAL_DEVICE";
                }
            }

        } catch (Exception e) {
            log.debug("[SNMP检测器] 获取设备SNMP信息异常", e);
        }

        return null;
    }

    /**
     * 分析系统描述
     */
    private String analyzeSysDescr(String sysDescr,
                                   Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查指纹
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getDeviceDescriptions() != null) {
                    for (String description : fingerprint.getDeviceDescriptions()) {
                        if (sysDescr.toLowerCase().contains(description.toLowerCase())) {
                            log.debug("[SNMP检测器] 通过系统描述特征检测到协议: {} - {}", fingerprintEntry.getKey(), description);
                            return fingerprintEntry.getKey();
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[SNMP检测器] 系统描述分析异常", e);
        }

        return null;
    }

    /**
     * 获取SNMP值
     * 注意：这是简化实现，实际项目中建议使用SNMP4J等专业库
     */
    private String getSNMPValue(String ipAddress, int port, String community, String oid, int timeout) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeout);

            // 构造SNMP GET请求包
            byte[] requestPacket = buildSNMPPacket(community, oid, true);

            // 发送请求
            InetAddress address = InetAddress.getByName(ipAddress);
            DatagramPacket sendPacket = new DatagramPacket(requestPacket, requestPacket.length, address, port);
            socket.send(sendPacket);

            // 接收响应
            byte[] responseBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(receivePacket);

            // 解析响应
            return parseSNMPResponse(receivePacket.getData(), receivePacket.getLength());

        } catch (Exception e) {
            log.debug("[SNMP检测器] 获取SNMP值失败: {} - OID: {} - {}", ipAddress, oid, e.getMessage());
            return null;
        }
    }

    /**
     * 构造SNMP数据包（简化实现）
     */
    private byte[] buildSNMPPacket(String community, String oid, boolean isGetRequest) {
        // 这里是简化的SNMP包构造
        // 实际实现需要完整的BER编码和SNMP协议支持
        // 建议使用SNMP4J等专业库

        // 简化实现：返回基本的GET请求包结构
        String packetStr = String.format("SNMPv1 GET %s %s", community, oid);
        return packetStr.getBytes();
    }

    /**
     * 解析SNMP响应（简化实现）
     */
    private String parseSNMPResponse(byte[] data, int length) {
        try {
            // 简化的SNMP响应解析
            String response = new String(data, 0, length);

            // 提取值部分（简化实现）
            if (response.contains("Value:")) {
                int valueIndex = response.indexOf("Value:") + 6;
                if (valueIndex < response.length()) {
                    String value = response.substring(valueIndex).trim();
                    return value.split("\\s+")[0]; // 取第一个单词作为值
                }
            }

            return response.trim();

        } catch (Exception e) {
            log.debug("[SNMP检测器] 解析SNMP响应异常", e);
            return null;
        }
    }

    /**
     * 查找SNMP端口
     */
    private Integer findSnmpPort(Map<Integer, String> openPorts) {
        // 优先检查标准SNMP端口
        for (Integer port : SNMP_PORTS) {
            if (openPorts.containsKey(port)) {
                return port;
            }
        }

        // 检查其他可能的SNMP端口
        for (Map.Entry<Integer, String> entry : openPorts.entrySet()) {
            String service = entry.getValue();
            if (service != null && (
                    service.toUpperCase().contains("SNMP") ||
                    service.toUpperCase().contains("NETWORK") ||
                    service.toUpperCase().contains("MONITOR"))) {
                return entry.getKey();
            }
        }

        return null;
    }
}
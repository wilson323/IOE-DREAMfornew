package net.lab1024.sa.device.comm.discovery.detector.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.discovery.detector.ProtocolDetector;
import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Modbus协议检测器
 * <p>
 * 通过Modbus协议检测工业自动化设备：
 * 1. Modbus TCP连接测试
 * 2. Modbus功能码支持检测
 * 3. 设备信息寄存器读取
 * 4. 工业设备厂商识别
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class ModbusProtocolDetector implements ProtocolDetector {

    private static final List<Integer> MODBUS_PORTS = Arrays.asList(502, 5020, 1502, 2052);

    private static final Map<String, String> VENDOR_SIGNATURES = new HashMap<>();

    // Modbus功能码
    private static final int READ_COILS = 0x01;
    private static final int READ_DISCRETE_INPUTS = 0x02;
    private static final int READ_HOLDING_REGISTERS = 0x03;
    private static final int READ_INPUT_REGISTERS = 0x04;
    private static final int WRITE_SINGLE_COIL = 0x05;
    private static final int WRITE_SINGLE_REGISTER = 0x06;
    private static final int WRITE_MULTIPLE_COILS = 0x0F;
    private static final int WRITE_MULTIPLE_REGISTERS = 0x10;

    // Modbus异常码
    private static final int ILLEGAL_FUNCTION = 0x01;
    private static final int ILLEGAL_DATA_ADDRESS = 0x02;
    private static final int ILLEGAL_DATA_VALUE = 0x03;
    private static final int SERVER_DEVICE_FAILURE = 0x04;

    static {
        // Modbus设备厂商签名
        VENDOR_SIGNATURES.put("Siemens", "SIEMENS_PLC");
        VENDOR_SIGNATURES.put("Rockwell", "ROCKWELL_PLC");
        VENDOR_SIGNATURES.put("Schneider", "SCHNEIDER_PLC");
        VENDOR_SIGNATURES.put("Mitsubishi", "MITSUBISHI_PLC");
        VENDOR_SIGNATURES.put("Omron", "OMRON_PLC");
        VENDOR_SIGNATURES.put("ABB", "ABB_PLC");
        VENDOR_SIGNATURES.put("Yokogawa", "YOKOGAWA_PLC");
        VENDOR_SIGNATURES.put("Honeywell", "HONEYWELL_PLC");
        VENDOR_SIGNATURES.put("Emerson", "EMERSON_PLC");
        VENDOR_SIGNATURES.put("GE", "GE_PLC");
        VENDOR_SIGNATURES.put("Modicon", "MODICON_PLC");
        VENDOR_SIGNATURES.put("Beckhoff", "BECKHOFF_PLC");
        VENDOR_SIGNATURES.put("Wago", "WAGO_PLC");
        VENDOR_SIGNATURES.put("Phoenix", "PHOENIX_PLC");
        VENDOR_SIGNATURES.put("B&R", "BR_PLC");
    }

    @Override
    public String detectProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout,
                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查Modbus相关端口
            Integer modbusPort = findModbusPort(openPorts);
            if (modbusPort == null) {
                return null;
            }

            log.debug("[Modbus检测器] 检测Modbus协议: {} - 端口: {}", ipAddress, modbusPort);

            // 尝试Modbus连接
            String detectedProtocol = detectByModbus(ipAddress, modbusPort, timeout, fingerprints);
            if (detectedProtocol != null && !"UNKNOWN".equals(detectedProtocol)) {
                return detectedProtocol;
            }

            return null;

        } catch (Exception e) {
            log.error("[Modbus检测器] 协议检测异常: {}", ipAddress, e);
            return null;
        }
    }

    @Override
    public String getDetectorType() {
        return "MODBUS";
    }

    @Override
    public String getDescription() {
        return "Modbus协议检测器";
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("SIEMENS_PLC", "ROCKWELL_PLC", "SCHNEIDER_PLC", "MITSUBISHI_PLC",
                          "OMRON_PLC", "ABB_PLC", "YOKOGAWA_PLC", "HONEYWELL_PLC");
    }

    /**
     * 通过Modbus连接检测协议
     */
    private String detectByModbus(String ipAddress, int port, int timeout,
                                  Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        Socket socket = null;
        try {
            // 创建Modbus TCP连接
            socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress, port), timeout);
            socket.setSoTimeout(timeout);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // 测试基本Modbus通信
            if (testModbusCommunication(out, in)) {
                log.debug("[Modbus检测器] Modbus通信测试成功");

                // 读取设备信息寄存器
                String deviceInfo = readDeviceInfo(out, in);
                if (deviceInfo != null) {
                    // 分析设备信息
                    String detectedProtocol = analyzeDeviceInfo(deviceInfo, fingerprints);
                    if (detectedProtocol != null) {
                        return detectedProtocol;
                    }

                    // 检查厂商签名
                    for (Map.Entry<String, String> entry : VENDOR_SIGNATURES.entrySet()) {
                        if (deviceInfo.toLowerCase().contains(entry.getKey().toLowerCase())) {
                            log.debug("[Modbus检测器] 通过设备信息检测到协议: {} - {}", entry.getKey(), entry.getValue());
                            return entry.getValue();
                        }
                    }
                }

                // 尝试读取标准寄存器
                detectedProtocol = tryStandardRegisters(out, in, fingerprints);
                if (detectedProtocol != null) {
                    return detectedProtocol;
                }

                log.debug("[Modbus检测器] 检测到Modbus设备，但无法确定具体厂商");
                return "MODBUS_DEVICE";
            }

        } catch (Exception e) {
            log.debug("[Modbus检测器] Modbus连接异常: {}:{} - {}", ipAddress, port, e.getMessage());
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
     * 测试Modbus通信
     */
    private boolean testModbusCommunication(OutputStream out, InputStream in) throws IOException {
        try {
            // 发送读取设备识别寄存器的请求
            byte[] request = buildModbusRequest(1, READ_HOLDING_REGISTERS, 0x0000, 1);
            out.write(request);
            out.flush();

            // 读取响应
            byte[] response = new byte[256];
            int bytesRead = in.read(response);

            if (bytesRead > 0) {
                // 检查Modbus响应格式
                return isValidModbusResponse(response, bytesRead);
            }

        } catch (Exception e) {
            log.debug("[Modbus检测器] Modbus通信测试异常", e);
        }

        return false;
    }

    /**
     * 读取设备信息
     */
    private String readDeviceInfo(OutputStream out, InputStream in) throws IOException {
        try {
            StringBuilder deviceInfo = new StringBuilder();

            // 尝试读取多个寄存器获取设备信息
            int[] registers = {0x0000, 0x0001, 0x0002, 0x0003, 0x0008}; // 常见设备信息寄存器地址

            for (int address : registers) {
                byte[] request = buildModbusRequest(1, READ_HOLDING_REGISTERS, address, 1);
                out.write(request);
                out.flush();

                byte[] response = new byte[256];
                int bytesRead = in.read(response);

                if (bytesRead > 5) {
                    String value = parseModbusResponse(response, bytesRead);
                    if (value != null && !value.equals("0")) {
                        deviceInfo.append(value).append(" ");
                    }
                }

                // 避免过快请求
                Thread.sleep(10);
            }

            return deviceInfo.length() > 0 ? deviceInfo.toString().trim() : null;

        } catch (Exception e) {
            log.debug("[Modbus检测器] 读取设备信息异常", e);
            return null;
        }
    }

    /**
     * 尝试标准寄存器
     */
    private String tryStandardRegisters(OutputStream out, InputStream in,
                                       Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查指纹
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getModbusRegisters() != null) {
                    for (String registerInfo : fingerprint.getModbusRegisters()) {
                        // 解析寄存器地址和数量
                        String[] parts = registerInfo.split(":");
                        if (parts.length >= 2) {
                            try {
                                int address = Integer.parseInt(parts[0], 16);
                                int count = Integer.parseInt(parts[1]);

                                byte[] request = buildModbusRequest(1, READ_HOLDING_REGISTERS, address, count);
                                out.write(request);
                                out.flush();

                                byte[] response = new byte[256];
                                int bytesRead = in.read(response);

                                if (bytesRead > 5) {
                                    String value = parseModbusResponse(response, bytesRead);
                                    if (value != null && !value.equals("0")) {
                                        log.debug("[Modbus检测器] 通过寄存器检测到协议: {} - {} = {}",
                                                fingerprintEntry.getKey(), registerInfo, value);
                                        return fingerprintEntry.getKey();
                                    }
                                }
                            } catch (NumberFormatException e) {
                                log.debug("[Modbus检测器] 寄存器格式错误: {}", registerInfo);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("[Modbus检测器] 尝试标准寄存器异常", e);
        }

        return null;
    }

    /**
     * 分析设备信息
     */
    private String analyzeDeviceInfo(String deviceInfo,
                                   Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints) {
        try {
            // 检查指纹
            for (Map.Entry<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprintEntry : fingerprints.entrySet()) {
                ProtocolAutoDiscoveryManager.ProtocolFingerprint fingerprint = fingerprintEntry.getValue();
                if (fingerprint.getDeviceDescriptions() != null) {
                    for (String description : fingerprint.getDeviceDescriptions()) {
                        if (deviceInfo.toLowerCase().contains(description.toLowerCase())) {
                            log.debug("[Modbus检测器] 通过设备信息特征检测到协议: {} - {}", fingerprintEntry.getKey(), description);
                            return fingerprintEntry.getKey();
                        }
                    }
                }
            }

            // 检查PLC特征
            if (deviceInfo.toLowerCase().contains("plc") ||
                deviceInfo.toLowerCase().contains("controller") ||
                deviceInfo.toLowerCase().contains("automation")) {

                log.debug("[Modbus检测器] 检测到PLC设备特征");
                return "MODBUS_PLC";
            }

        } catch (Exception e) {
            log.debug("[Modbus检测器] 设备信息分析异常", e);
        }

        return null;
    }

    /**
     * 构造Modbus请求
     */
    private byte[] buildModbusRequest(int unitId, int functionCode, int startAddress, int quantity) {
        // Modbus TCP请求格式
        // MBAP Header (7 bytes) + PDU
        byte[] request = new byte[12];

        // Transaction Identifier (2 bytes)
        request[0] = 0x00;
        request[1] = 0x01;

        // Protocol Identifier (2 bytes) - Modbus protocol = 0
        request[2] = 0x00;
        request[3] = 0x00;

        // Length (2 bytes)
        request[4] = 0x00;
        request[5] = 0x06;

        // Unit Identifier (1 byte)
        request[6] = (byte) unitId;

        // Function Code (1 byte)
        request[7] = (byte) functionCode;

        // Starting Address (2 bytes)
        request[8] = (byte) ((startAddress >> 8) & 0xFF);
        request[9] = (byte) (startAddress & 0xFF);

        // Quantity (2 bytes)
        request[10] = (byte) ((quantity >> 8) & 0xFF);
        request[11] = (byte) (quantity & 0xFF);

        return request;
    }

    /**
     * 验证Modbus响应
     */
    private boolean isValidModbusResponse(byte[] response, int length) {
        try {
            if (length < 8) {
                return false;
            }

            // 检查MBAP头
            // Transaction Identifier
            int transactionId = ((response[0] & 0xFF) << 8) | (response[1] & 0xFF);

            // Protocol Identifier
            int protocolId = ((response[2] & 0xFF) << 8) | (response[3] & 0xFF);
            if (protocolId != 0) {
                return false;
            }

            // Length
            int lengthField = ((response[4] & 0xFF) << 8) | (response[5] & 0xFF);
            if (lengthField + 6 != length) {
                return false;
            }

            // Unit Identifier
            int unitId = response[6] & 0xFF;

            // Function Code
            int functionCode = response[7] & 0xFF;

            // 检查异常响应
            if ((functionCode & 0x80) != 0) {
                int exceptionCode = response[8] & 0xFF;
                log.debug("[Modbus检测器] Modbus异常响应: 功能码={}, 异常码={}",
                         functionCode & 0x7F, exceptionCode);
                // 异常响应也算有效的Modbus响应
                return true;
            }

            return true;

        } catch (Exception e) {
            log.debug("[Modbus检测器] 验证Modbus响应异常", e);
            return false;
        }
    }

    /**
     * 解析Modbus响应
     */
    private String parseModbusResponse(byte[] response, int length) {
        try {
            if (length < 9) {
                return null;
            }

            // 检查是否为异常响应
            int functionCode = response[7] & 0xFF;
            if ((functionCode & 0x80) != 0) {
                int exceptionCode = response[8] & 0xFF;
                return "EXCEPTION:" + exceptionCode;
            }

            // 解析正常响应
            int byteCount = response[8] & 0xFF;
            if (length >= 9 + byteCount) {
                if (byteCount >= 2) {
                    // 读取16位寄存器值
                    int value = ((response[9] & 0xFF) << 8) | (response[10] & 0xFF);
                    return String.valueOf(value);
                } else if (byteCount == 1) {
                    return String.valueOf(response[9] & 0xFF);
                }
            }

        } catch (Exception e) {
            log.debug("[Modbus检测器] 解析Modbus响应异常", e);
        }

        return null;
    }

    /**
     * 查找Modbus端口
     */
    private Integer findModbusPort(Map<Integer, String> openPorts) {
        // 优先检查标准Modbus端口
        for (Integer port : MODBUS_PORTS) {
            if (openPorts.containsKey(port)) {
                return port;
            }
        }

        // 检查其他可能的Modbus端口
        for (Map.Entry<Integer, String> entry : openPorts.entrySet()) {
            String service = entry.getValue();
            if (service != null && (
                    service.toUpperCase().contains("MODBUS") ||
                    service.toUpperCase().contains("TCP/IP") ||
                    service.toUpperCase().contains("PLC") ||
                    service.toUpperCase().contains("INDUSTRIAL"))) {
                return entry.getKey();
            }
        }

        return null;
    }
}
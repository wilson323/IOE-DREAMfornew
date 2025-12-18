package net.lab1024.sa.device.comm.discovery.detector;

import net.lab1024.sa.device.comm.discovery.ProtocolAutoDiscoveryManager;

import java.util.Map;

/**
 * 协议检测器接口
 * <p>
 * 定义设备协议自动检测的标准接口：
 * 1. HTTP协议检测
 * 2. RTSP协议检测
 * 3. SNMP协议检测
 * 4. Modbus协议检测
 * 5. 自定义协议检测
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ProtocolDetector {

    /**
     * 检测设备协议
     *
     * @param ipAddress 设备IP地址
     * @param openPorts 开放端口映射
     * @param timeout 检测超时时间（毫秒）
     * @param fingerprints 协议指纹库
     * @return 检测到的协议类型，未知返回"UNKNOWN"
     */
    String detectProtocol(String ipAddress, Map<Integer, String> openPorts, int timeout,
                            Map<String, ProtocolAutoDiscoveryManager.ProtocolFingerprint> fingerprints);

    /**
     * 获取检测器类型
     *
     * @return 检测器类型
     */
    String getDetectorType();

    /**
     * 获取检测器描述
     *
     * @return 检测器描述
     */
    String getDescription();

    /**
     * 获取支持的协议类型
     *
     * @return 支持的协议类型列表
     */
    java.util.List<String> getSupportedProtocols();
}
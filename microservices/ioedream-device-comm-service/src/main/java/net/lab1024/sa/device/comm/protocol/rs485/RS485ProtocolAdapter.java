package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.domain.*;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolBuildException;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * RS485协议适配器
 * <p>
 * 支持RS485工业设备通讯协议
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Slf4j
public class RS485ProtocolAdapter implements ProtocolAdapter {

    private static final String PROTOCOL_TYPE = "RS485_STANDARD_V1_0";
    private static final String MANUFACTURER = "通用RS485";
    private static final String VERSION = "V1.0";
    private static final String[] SUPPORTED_MODELS = {"RS485-GENERIC"};

    private String adapterStatus = "INITIALIZED";

    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }

    @Override
    public String getManufacturer() {
        return MANUFACTURER;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return SUPPORTED_MODELS.clone();
    }

    @Override
    public boolean isDeviceModelSupported(String deviceModel) {
        return deviceModel != null && deviceModel.startsWith("RS485");
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        log.debug("[RS485协议] 解析设备消息, deviceId={}, dataLength={}", deviceId, rawData != null ? rawData.length : 0);

        if (rawData == null || rawData.length == 0) {
            throw new ProtocolParseException("数据为空");
        }

        return ProtocolMessage.builder()
                .deviceId(deviceId)
                .protocolType(PROTOCOL_TYPE)
                .messageType("DATA")
                .rawDataHex(bytesToHex(rawData))
                .timestamp(java.time.LocalDateTime.now())
                .direction("INBOUND")
                .status("PROCESSED")
                .build();
    }

    @Override
    public ProtocolMessage parseDeviceMessage(String hexData, Long deviceId) throws ProtocolParseException {
        byte[] rawData = hexToBytes(hexData);
        return parseDeviceMessage(rawData, deviceId);
    }

    @Override
    public byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        log.debug("[RS485协议] 构建设备响应, deviceId={}, messageType={}", deviceId, messageType);

        // 简单实现：返回确认响应
        return new byte[]{0x06}; // ACK
    }

    @Override
    public String buildDeviceResponseHex(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        byte[] response = buildDeviceResponse(messageType, businessData, deviceId);
        return bytesToHex(response);
    }

    @Override
    public ProtocolValidationResult validateMessage(ProtocolMessage message) {
        if (message == null) {
            return ProtocolValidationResult.failure("INVALID_MESSAGE", "消息为空");
        }
        return ProtocolValidationResult.success();
    }

    @Override
    public ProtocolPermissionResult validateDevicePermission(Long deviceId, String operation) {
        return ProtocolPermissionResult.permit();
    }

    @Override
    public Future<ProtocolInitResult> initializeDevice(Map<String, Object> deviceInfo, Map<String, Object> config) {
        return CompletableFuture.completedFuture(
                ProtocolInitResult.success(null, "RS485_CONN_" + System.currentTimeMillis())
        );
    }

    @Override
    public ProtocolRegistrationResult handleDeviceRegistration(Map<String, Object> registrationData, Long deviceId) {
        return ProtocolRegistrationResult.success(deviceId, "RS485_REG_" + deviceId);
    }

    @Override
    public ProtocolHeartbeatResult handleDeviceHeartbeat(Map<String, Object> heartbeatData, Long deviceId) {
        return ProtocolHeartbeatResult.success(deviceId);
    }

    @Override
    public ProtocolDeviceStatus getDeviceStatus(Long deviceId) {
        return ProtocolDeviceStatus.builder()
                .deviceId(deviceId)
                .online(true)
                .connectionStatus("CONNECTED")
                .manufacturer(MANUFACTURER)
                .build();
    }

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        return CompletableFuture.completedFuture(
                ProtocolProcessResult.success(businessType, businessData)
        );
    }

    @Override
    public Future<ProtocolProcessResult> processAttendanceBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        return CompletableFuture.completedFuture(
                ProtocolProcessResult.success(businessType, businessData)
        );
    }

    @Override
    public Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        return CompletableFuture.completedFuture(
                ProtocolProcessResult.success(businessType, businessData)
        );
    }

    @Override
    public Map<String, Object> getProtocolConfig(Long deviceId) {
        return new HashMap<>();
    }

    @Override
    public boolean updateProtocolConfig(Long deviceId, Map<String, Object> config) {
        return true;
    }

    @Override
    public ProtocolErrorResponse handleProtocolError(String errorCode, String errorMessage, Long deviceId) {
        return ProtocolErrorResponse.create(errorCode, errorMessage, deviceId);
    }

    @Override
    public Map<String, ProtocolErrorInfo> getErrorCodeMapping() {
        return new HashMap<>();
    }

    @Override
    public void initialize() {
        adapterStatus = "RUNNING";
        log.info("[RS485协议] 适配器初始化完成");
    }

    @Override
    public void destroy() {
        adapterStatus = "STOPPED";
        log.info("[RS485协议] 适配器已销毁");
    }

    @Override
    public String getAdapterStatus() {
        return adapterStatus;
    }

    @Override
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("status", adapterStatus);
        stats.put("protocol", PROTOCOL_TYPE);
        return stats;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}

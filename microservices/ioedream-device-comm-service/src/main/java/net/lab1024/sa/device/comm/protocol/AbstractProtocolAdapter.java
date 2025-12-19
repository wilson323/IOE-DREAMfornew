package net.lab1024.sa.device.comm.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.*;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolBuildException;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 协议适配器抽象基类
 * <p>
 * 提供协议适配器的默认实现，子类可按需覆盖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractProtocolAdapter implements ProtocolAdapter {

    protected String adapterStatus = "INITIALIZED";

    @Override
    public abstract String getProtocolType();

    @Override
    public abstract String getManufacturer();

    @Override
    public abstract String getVersion();

    @Override
    public abstract String[] getSupportedDeviceModels();

    @Override
    public boolean isDeviceModelSupported(String deviceModel) {
        if (deviceModel == null) return false;
        for (String model : getSupportedDeviceModels()) {
            if (model.equalsIgnoreCase(deviceModel)) return true;
        }
        return false;
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        throw new ProtocolParseException("Not implemented");
    }

    @Override
    public ProtocolMessage parseDeviceMessage(String hexData, Long deviceId) throws ProtocolParseException {
        throw new ProtocolParseException("Not implemented");
    }

    @Override
    public byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        throw new ProtocolBuildException("Not implemented");
    }

    @Override
    public String buildDeviceResponseHex(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        throw new ProtocolBuildException("Not implemented");
    }

    @Override
    public ProtocolValidationResult validateMessage(ProtocolMessage message) {
        ProtocolValidationResult result = new ProtocolValidationResult();
        result.setValid(true);
        return result;
    }

    @Override
    public ProtocolPermissionResult validateDevicePermission(Long deviceId, String operation) {
        ProtocolPermissionResult result = new ProtocolPermissionResult();
        result.setPermitted(true);
        return result;
    }

    @Override
    public Future<ProtocolInitResult> initializeDevice(Map<String, Object> deviceInfo, Map<String, Object> config) {
        ProtocolInitResult result = new ProtocolInitResult();
        result.setSuccess(true);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public ProtocolRegistrationResult handleDeviceRegistration(Map<String, Object> registrationData, Long deviceId) {
        ProtocolRegistrationResult result = new ProtocolRegistrationResult();
        result.setSuccess(true);
        return result;
    }

    @Override
    public ProtocolHeartbeatResult handleDeviceHeartbeat(Map<String, Object> heartbeatData, Long deviceId) {
        ProtocolHeartbeatResult result = new ProtocolHeartbeatResult();
        result.setSuccess(true);
        result.setOnline(true);
        return result;
    }

    @Override
    public ProtocolDeviceStatus getDeviceStatus(Long deviceId) {
        ProtocolDeviceStatus status = new ProtocolDeviceStatus();
        status.setOnline(true);
        return status;
    }

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();
        result.setSuccess(true);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public Future<ProtocolProcessResult> processAttendanceBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();
        result.setSuccess(true);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();
        result.setSuccess(true);
        return CompletableFuture.completedFuture(result);
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
        ProtocolErrorResponse response = new ProtocolErrorResponse();
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        return response;
    }

    @Override
    public Map<String, ProtocolErrorInfo> getErrorCodeMapping() {
        return new HashMap<>();
    }

    @Override
    public void initialize() {
        this.adapterStatus = "RUNNING";
        log.info("[{}] 协议适配器初始化完成", getProtocolType());
    }

    @Override
    public void destroy() {
        this.adapterStatus = "STOPPED";
        log.info("[{}] 协议适配器已销毁", getProtocolType());
    }

    @Override
    public String getAdapterStatus() {
        return adapterStatus;
    }

    @Override
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("protocolType", getProtocolType());
        stats.put("status", getAdapterStatus());
        return stats;
    }
}

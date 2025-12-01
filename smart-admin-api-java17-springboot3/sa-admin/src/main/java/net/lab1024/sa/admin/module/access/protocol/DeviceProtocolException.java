package net.lab1024.sa.admin.module.access.protocol;

import net.lab1024.sa.base.common.exception.SmartException;

/**
 * 设备协议异常
 * <p>
 * 用于封装设备协议调用过程中的异常
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
public class DeviceProtocolException extends SmartException {

    /**
     * 设备ID
     */
    private final Long deviceId;

    /**
     * 协议类型
     */
    private final DeviceProtocolAdapter.ProtocolType protocolType;

    /**
     * 操作类型
     */
    private final String operation;

    public DeviceProtocolException(String message, Long deviceId, 
                                   DeviceProtocolAdapter.ProtocolType protocolType, 
                                   String operation) {
        super(message);
        this.deviceId = deviceId;
        this.protocolType = protocolType;
        this.operation = operation;
    }

    public DeviceProtocolException(String message, Throwable cause, Long deviceId,
                                   DeviceProtocolAdapter.ProtocolType protocolType,
                                   String operation) {
        super(message, cause);
        this.deviceId = deviceId;
        this.protocolType = protocolType;
        this.operation = operation;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public DeviceProtocolAdapter.ProtocolType getProtocolType() {
        return protocolType;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return String.format("DeviceProtocolException{deviceId=%d, protocolType=%s, operation='%s', message='%s'}",
                deviceId, protocolType, operation, getMessage());
    }
}


package net.lab1024.sa.device.protocol;

/**
 * 设备协议异常类 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 统一异常处理机制
 * - 详细错误信息和错误代码
 * - 支持异常链传递
 * - 设备协议相关异常统一管理
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
public class DeviceProtocolException extends Exception {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 设备ID
     */
    private final Long deviceId;

    /**
     * 协议类型
     */
    private final String protocolType;

    /**
     * 构造函数 - 仅错误信息
     *
     * @param message 错误信息
     */
    public DeviceProtocolException(String message) {
        super(message);
        this.errorCode = "DEVICE_PROTOCOL_ERROR";
        this.deviceId = null;
        this.protocolType = null;
    }

    /**
     * 构造函数 - 错误信息和原因
     *
     * @param message 错误信息
     * @param cause    异常原因
     */
    public DeviceProtocolException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DEVICE_PROTOCOL_ERROR";
        this.deviceId = null;
        this.protocolType = null;
    }

    /**
     * 构造函数 - 完整参数
     *
     * @param message      错误信息
     * @param errorCode    错误代码
     * @param deviceId     设备ID
     * @param protocolType 协议类型
     */
    public DeviceProtocolException(String message, String errorCode, Long deviceId, String protocolType) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "DEVICE_PROTOCOL_ERROR";
        this.deviceId = deviceId;
        this.protocolType = protocolType;
    }

    /**
     * 构造函数 - 完整参数包含异常链
     *
     * @param message      错误信息
     * @param cause        异常原因
     * @param errorCode    错误代码
     * @param deviceId     设备ID
     * @param protocolType 协议类型
     */
    public DeviceProtocolException(String message, Throwable cause, String errorCode, Long deviceId, String protocolType) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "DEVICE_PROTOCOL_ERROR";
        this.deviceId = deviceId;
        this.protocolType = protocolType;
    }

    /**
     * 获取错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public Long getDeviceId() {
        return deviceId;
    }

    /**
     * 获取协议类型
     *
     * @return 协议类型
     */
    public String getProtocolType() {
        return protocolType;
    }

    /**
     * 获取详细的错误信息
     *
     * @return 详细错误信息
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());

        if (deviceId != null) {
            sb.append(" [设备ID: ").append(deviceId).append("]");
        }

        if (protocolType != null) {
            sb.append(" [协议类型: ").append(protocolType).append("]");
        }

        if (!"DEVICE_PROTOCOL_ERROR".equals(errorCode)) {
            sb.append(" [错误代码: ").append(errorCode).append("]");
        }

        return sb.toString();
    }
}
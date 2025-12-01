package net.lab1024.sa.base.common.device;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;

/**
 * 设备协议异常
 * <p>
 * 设备协议相关的专用异常类，用于封装设备通信过程中的各种错误
 * 提供详细的错误分类和静态工厂方法，便于异常处理和错误分析
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public class DeviceProtocolException extends Exception {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 设备信息
     */
    private SmartDeviceEntity device;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 时间戳
     */
    private long timestamp;

    public DeviceProtocolException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    public DeviceProtocolException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    public DeviceProtocolException(String message, SmartDeviceEntity device, String errorCode) {
        super(message);
        this.device = device;
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }

    public DeviceProtocolException(String message, SmartDeviceEntity device, String errorCode, String operationType) {
        super(message);
        this.device = device;
        this.errorCode = errorCode;
        this.operationType = operationType;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 连接失败异常
     */
    public static DeviceProtocolException connectionFailed(SmartDeviceEntity device) {
        return new DeviceProtocolException(
            "设备连接失败: " + device.getDeviceName(),
            device,
            "CONNECTION_FAILED",
            "CONNECT"
        );
    }

    /**
     * 连接超时异常
     */
    public static DeviceProtocolException connectionTimeout(SmartDeviceEntity device, Long timeoutMs) {
        return new DeviceProtocolException(
            "设备连接超时: " + device.getDeviceName() + " (timeout: " + timeoutMs + "ms)",
            device,
            "CONNECTION_TIMEOUT",
            "CONNECT"
        );
    }

    /**
     * 认证失败异常
     */
    public static DeviceProtocolException authenticationFailed(String credentialType) {
        return new DeviceProtocolException(
            "设备认证失败: " + credentialType,
            "AUTHENTICATION_FAILED"
        );
    }

    /**
     * 权限不足异常
     */
    public static DeviceProtocolException permissionDenied(String operation) {
        return new DeviceProtocolException(
            "权限不足，无法执行操作: " + operation,
            "PERMISSION_DENIED"
        );
    }

    /**
     * 设备不支持异常
     */
    public static DeviceProtocolException deviceNotSupported(String deviceType, String manufacturer) {
        return new DeviceProtocolException(
            "不支持的设备类型: " + deviceType + " (制造商: " + manufacturer + ")",
            "DEVICE_NOT_SUPPORTED"
        );
    }

    /**
     * 协议错误异常
     */
    public static DeviceProtocolException protocolError(String protocol, String detail) {
        return new DeviceProtocolException(
            "协议错误: " + protocol + " - " + detail,
            "PROTOCOL_ERROR"
        );
    }

    /**
     * 数据格式错误异常
     */
    public static DeviceProtocolException dataFormatError(String dataType, String detail) {
        return new DeviceProtocolException(
            "数据格式错误: " + dataType + " - " + detail,
            "DATA_FORMAT_ERROR"
        );
    }

    /**
     * 数据校验失败异常
     */
    public static DeviceProtocolException validationFailed(String field, String reason) {
        return new DeviceProtocolException(
            "数据校验失败: " + field + " - " + reason,
            "VALIDATION_FAILED"
        );
    }

    /**
     * 操作超时异常
     */
    public static DeviceProtocolException operationTimeout(String operation, Long timeoutMs) {
        return new DeviceProtocolException(
            "操作超时: " + operation + " (timeout: " + timeoutMs + "ms)",
            "OPERATION_TIMEOUT"
        );
    }

    /**
     * 网络错误异常
     */
    public static DeviceProtocolException networkError(String networkType, String detail) {
        return new DeviceProtocolException(
            "网络错误: " + networkType + " - " + detail,
            "NETWORK_ERROR"
        );
    }

    /**
     * 设备忙异常
     */
    public static DeviceProtocolException deviceBusy(String deviceName) {
        return new DeviceProtocolException(
            "设备忙: " + deviceName + " 正在处理其他操作",
            "DEVICE_BUSY"
        );
    }

    /**
     * 设备离线异常
     */
    public static DeviceProtocolException deviceOffline(SmartDeviceEntity device) {
        return new DeviceProtocolException(
            "设备离线: " + device.getDeviceName(),
            device,
            "DEVICE_OFFLINE"
        );
    }

    /**
     * 设备故障异常
     */
    public static DeviceProtocolException deviceFault(SmartDeviceEntity device, String faultDetail) {
        return new DeviceProtocolException(
            "设备故障: " + device.getDeviceName() + " - " + faultDetail,
            device,
            "DEVICE_FAULT"
        );
    }

    /**
     * 配置错误异常
     */
    public static DeviceProtocolException configurationError(String configItem, String detail) {
        return new DeviceProtocolException(
            "配置错误: " + configItem + " - " + detail,
            "CONFIGURATION_ERROR"
        );
    }

    /**
     * 未知异常
     */
    public static DeviceProtocolException unknownError(String detail) {
        return new DeviceProtocolException(
            "未知错误: " + detail,
            "UNKNOWN_ERROR"
        );
    }

    /**
     * 临时异常（可重试）
     */
    public static DeviceProtocolException temporaryError(String detail) {
        return new DeviceProtocolException(
            "临时错误: " + detail,
            "TEMPORARY_ERROR"
        );
    }

    /**
     * 系统资源不足异常
     */
    public static DeviceProtocolException resourceExhausted(String resourceType) {
        return new DeviceProtocolException(
            "系统资源不足: " + resourceType,
            "RESOURCE_EXHAUSTED"
        );
    }

    // ==================== 实例方法 ====================

    /**
     * 设置重试次数
     */
    public DeviceProtocolException withRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * 增加重试次数
     */
    public DeviceProtocolException incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        return this;
    }

    /**
     * 检查是否为可重试异常
     */
    public boolean isRetryable() {
        if (errorCode == null) {
            return false;
        }

        return errorCode.equals("CONNECTION_TIMEOUT") ||
               errorCode.equals("OPERATION_TIMEOUT") ||
               errorCode.equals("NETWORK_ERROR") ||
               errorCode.equals("DEVICE_BUSY") ||
               errorCode.equals("TEMPORARY_ERROR") ||
               errorCode.equals("RESOURCE_EXHAUSTED");
    }

    /**
     * 检查是否为严重错误
     */
    public boolean isCritical() {
        if (errorCode == null) {
            return false;
        }

        return errorCode.equals("AUTHENTICATION_FAILED") ||
               errorCode.equals("PERMISSION_DENIED") ||
               errorCode.equals("DEVICE_NOT_SUPPORTED") ||
               errorCode.equals("CONFIGURATION_ERROR") ||
               errorCode.equals("DEVICE_FAULT");
    }

    /**
     * 获取设备信息字符串
     */
    public String getDeviceInfoString() {
        if (device == null) {
            return "Unknown Device";
        }

        return String.format("%s (%s) - %s:%d",
            device.getDeviceName(),
            device.getDeviceCode(),
            device.getIpAddress(),
            device.getPort());
    }

    /**
     * 获取异常摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append("[").append(errorCode).append("] ");

        if (operationType != null) {
            summary.append(operationType).append(" ");
        }

        summary.append(getMessage());

        if (device != null) {
            summary.append(" (Device: ").append(device.getDeviceName()).append(")");
        }

        if (retryCount != null && retryCount > 0) {
            summary.append(" [Retry: ").append(retryCount).append("]");
        }

        return summary.toString();
    }

    // ==================== Getter和Setter ====================

    public String getErrorCode() {
        return errorCode;
    }

    public SmartDeviceEntity getDevice() {
        return device;
    }

    public String getOperationType() {
        return operationType;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
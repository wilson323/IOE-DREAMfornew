package net.lab1024.sa.common.device;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备下发结果
 * <p>
 * 统一的设备操作结果封装，用于标准化设备适配器的操作响应
 * 包含操作状态、消息、错误代码、响应数据等信息
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDispatchResult {

    /**
     * 操作是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 响应数据
     */
    private Map<String, Object> responseData;

    /**
     * 操作耗时（毫秒）
     */
    private Long executionTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 设备信息
     */
    private DeviceInfo deviceInfo;

    /**
     * 创建成功结果
     *
     * @param message      成功消息
     * @param responseData 响应数据
     * @return 成功结果对象
     */
    public static DeviceDispatchResult success(String message, Map<String, Object> responseData) {
        return DeviceDispatchResult.builder()
                .success(true)
                .message(message)
                .responseData(responseData != null ? responseData : new HashMap<>())
                .retryCount(0)
                .build();
    }

    /**
     * 创建成功结果（无响应数据）
     *
     * @param message 成功消息
     * @return 成功结果对象
     */
    public static DeviceDispatchResult success(String message) {
        return success(message, null);
    }

    /**
     * 创建失败结果
     *
     * @param message   失败消息
     * @param errorCode 错误代码
     * @return 失败结果对象
     */
    public static DeviceDispatchResult failure(String message, String errorCode) {
        return DeviceDispatchResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .responseData(new HashMap<>())
                .retryCount(0)
                .build();
    }

    /**
     * 创建失败结果（通用错误）
     *
     * @param message 失败消息
     * @return 失败结果对象
     */
    public static DeviceDispatchResult failure(String message) {
        return failure(message, "GENERAL_ERROR");
    }

    /**
     * 创建超时结果
     *
     * @param timeoutMs     超时毫秒数
     * @param operationType 操作类型
     * @return 超时结果对象
     */
    public static DeviceDispatchResult timeout(Long timeoutMs, String operationType) {
        return DeviceDispatchResult.builder()
                .success(false)
                .message("操作超时: " + timeoutMs + "ms")
                .errorCode("TIMEOUT_ERROR")
                .operationType(operationType)
                .executionTime(timeoutMs)
                .responseData(new HashMap<>())
                .build();
    }

    /**
     * 创建连接失败结果
     *
     * @param deviceInfo 设备信息
     * @param detail     详细信息
     * @return 连接失败结果对象
     */
    public static DeviceDispatchResult connectionFailure(DeviceInfo deviceInfo, String detail) {
        return DeviceDispatchResult.builder()
                .success(false)
                .message("设备连接失败: " + detail)
                .errorCode("CONNECTION_FAILED")
                .deviceInfo(deviceInfo)
                .responseData(new HashMap<>())
                .build();
    }

    /**
     * 创建协议错误结果
     *
     * @param protocol 协议名称
     * @param detail   详细信息
     * @return 协议错误结果对象
     */
    public static DeviceDispatchResult protocolError(String protocol, String detail) {
        return DeviceDispatchResult.builder()
                .success(false)
                .message("协议错误: " + protocol + " - " + detail)
                .errorCode("PROTOCOL_ERROR")
                .responseData(new HashMap<>())
                .build();
    }

    /**
     * 创建数据格式错误结果
     *
     * @param detail 详细信息
     * @return 数据格式错误结果对象
     */
    public static DeviceDispatchResult dataFormatError(String detail) {
        return DeviceDispatchResult.builder()
                .success(false)
                .message("数据格式错误: " + detail)
                .errorCode("DATA_FORMAT_ERROR")
                .responseData(new HashMap<>())
                .build();
    }

    /**
     * 创建设备不支持结果
     *
     * @param deviceType   设备类型
     * @param manufacturer 制造商
     * @return 设备不支持结果对象
     */
    public static DeviceDispatchResult deviceNotSupported(String deviceType, String manufacturer) {
        return DeviceDispatchResult.builder()
                .success(false)
                .message("不支持的设备: " + deviceType + " (" + manufacturer + ")")
                .errorCode("DEVICE_NOT_SUPPORTED")
                .responseData(new HashMap<>())
                .build();
    }

    /**
     * 创建部分成功结果
     *
     * @param message      部分成功消息
     * @param successCount 成功数量
     * @param totalCount   总数量
     * @param details      详细信息
     * @return 部分成功结果对象
     */
    public static DeviceDispatchResult partialSuccess(String message, int successCount, int totalCount,
            Map<String, Object> details) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("successCount", successCount);
        responseData.put("totalCount", totalCount);
        responseData.put("details", details != null ? details : new HashMap<>());

        return DeviceDispatchResult.builder()
                .success(false) // 部分成功不算完全成功
                .message(message + " (成功: " + successCount + "/" + totalCount + ")")
                .errorCode("PARTIAL_SUCCESS")
                .responseData(responseData)
                .build();
    }

    /**
     * 设置操作耗时
     *
     * @param executionTimeMs 执行耗时（毫秒）
     * @return 当前结果对象
     */
    public DeviceDispatchResult withExecutionTime(Long executionTimeMs) {
        this.executionTime = executionTimeMs;
        return this;
    }

    /**
     * 增加重试次数
     *
     * @return 当前结果对象
     */
    public DeviceDispatchResult incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
        return this;
    }

    /**
     * 设置设备信息
     *
     * @param deviceInfo 设备信息
     * @return 当前结果对象
     */
    public DeviceDispatchResult withDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    /**
     * 添加响应数据
     *
     * @param key   键
     * @param value 值
     * @return 当前结果对象
     */
    public DeviceDispatchResult addResponseData(String key, Object value) {
        if (this.responseData == null) {
            this.responseData = new HashMap<>();
        }
        this.responseData.put(key, value);
        return this;
    }

    /**
     * 检查是否为重试错误
     *
     * @return 是否为可重试错误
     */
    public boolean isRetryableError() {
        if (success) {
            return false;
        }

        // 定义可重试的错误类型
        return errorCode != null && (errorCode.equals("TIMEOUT_ERROR") ||
                errorCode.equals("CONNECTION_FAILED") ||
                errorCode.equals("NETWORK_ERROR") ||
                errorCode.equals("TEMPORARY_ERROR"));
    }

    /**
     * 检查是否为严重错误
     *
     * @return 是否为严重错误
     */
    public boolean isCriticalError() {
        if (success) {
            return false;
        }

        // 定义严重错误类型
        return errorCode != null && (errorCode.equals("DEVICE_NOT_SUPPORTED") ||
                errorCode.equals("AUTHENTICATION_FAILED") ||
                errorCode.equals("PERMISSION_DENIED") ||
                errorCode.equals("CONFIGURATION_ERROR"));
    }

    /**
     * 获取结果摘要
     *
     * @return 结果摘要字符串
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();

        if (success) {
            summary.append("✅ 成功");
        } else {
            summary.append("❌ 失败");
        }

        if (message != null) {
            summary.append(" - ").append(message);
        }

        if (executionTime != null) {
            summary.append(" (").append(executionTime).append("ms)");
        }

        return summary.toString();
    }

    /**
     * 设备信息内部类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceInfo {
        private Long deviceId;
        private String deviceCode;
        private String deviceName;
        private String deviceType;
        private String manufacturer;
        private String ipAddress;
        private Integer port;
    }
}

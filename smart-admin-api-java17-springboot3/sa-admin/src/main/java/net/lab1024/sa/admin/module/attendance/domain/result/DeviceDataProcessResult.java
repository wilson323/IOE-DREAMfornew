package net.lab1024.sa.admin.module.attendance.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备数据处理结果
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDataProcessResult {

    /**
     * 处理是否成功
     */
    private Boolean success;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 处理的数据类型
     */
    private String dataType;

    /**
     * 处理的数据数量
     */
    private Integer dataCount;

    /**
     * 成功处理的数据数量
     */
    private Integer successCount;

    /**
     * 失败处理的数据数量
     */
    private Integer failureCount;

    /**
     * 处理开始时间
     */
    private LocalDateTime startTime;

    /**
     * 处理结束时间
     */
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 创建成功结果
     *
     * @param deviceId 设备ID
     * @param dataCount 数据数量
     * @return 成功结果
     */
    public static DeviceDataProcessResult success(Long deviceId, Integer dataCount) {
        return DeviceDataProcessResult.builder()
                .success(true)
                .deviceId(deviceId)
                .dataCount(dataCount)
                .successCount(dataCount)
                .failureCount(0)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param deviceId 设备ID
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static DeviceDataProcessResult failure(Long deviceId, String errorCode, String errorMessage) {
        return DeviceDataProcessResult.builder()
                .success(false)
                .deviceId(deviceId)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .dataCount(0)
                .successCount(0)
                .failureCount(0)
                .build();
    }

    /**
     * 计算成功率
     *
     * @return 成功率（百分比）
     */
    public Double getSuccessRate() {
        if (dataCount == null || dataCount == 0) {
            return 0.0;
        }
        return (double) successCount / dataCount * 100;
    }
}
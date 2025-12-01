package net.lab1024.sa.common.device.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * 设备数据采集表单
 * <p>
 * 用于设备数据采集的参数封装，包含采集策略、时间范围、数据类型等配置
 * 支持实时数据采集和历史数据查询
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-30
 */
@Data
@Schema(description = "设备数据采集表单")
public class DeviceDataCollectionForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    /**
     * 采集类型：REAL_TIME(实时)、HISTORY(历史)、BATCH(批量)
     */
    @NotBlank(message = "采集类型不能为空")
    @Schema(description = "采集类型", example = "REAL_TIME", allowableValues = {"REAL_TIME", "HISTORY", "BATCH"})
    private String collectionType;

    /**
     * 数据类型：ALL(全部)、STATUS(状态)、ALARM(告警)、METRIC(指标)
     */
    @Schema(description = "数据类型", example = "ALL", allowableValues = {"ALL", "STATUS", "ALARM", "METRIC"})
    private String dataType = "ALL";

    /**
     * 开始时间（历史数据采集时使用）
     */
    @Schema(description = "开始时间", example = "2025-11-30T00:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间（历史数据采集时使用）
     */
    @Schema(description = "结束时间", example = "2025-11-30T23:59:59")
    private LocalDateTime endTime;

    /**
     * 采集间隔（秒）
     */
    @Min(value = 1, message = "采集间隔不能小于1秒")
    @Max(value = 3600, message = "采集间隔不能超过3600秒")
    @Schema(description = "采集间隔（秒）", example = "60")
    private Integer intervalSeconds = 60;

    /**
     * 最大采集数量（限制返回数据量）
     */
    @Min(value = 1, message = "最大采集数量不能小于1")
    @Max(value = 10000, message = "最大采集数量不能超过10000")
    @Schema(description = "最大采集数量", example = "1000")
    private Integer maxRecords = 1000;

    /**
     * 是否包含详细信息
     */
    @Schema(description = "是否包含详细信息", example = "false")
    private Boolean includeDetails = false;

    /**
     * 数据格式：JSON(默认)、XML、CSV
     */
    @Schema(description = "数据格式", example = "JSON", allowableValues = {"JSON", "XML", "CSV"})
    private String dataFormat = "JSON";

    /**
     * 过滤条件（JSON格式）
     */
    @Schema(description = "过滤条件", example = "{\"level\": \"ERROR\", \"source\": \"SENSOR\"}")
    private String filterConditions;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "timestamp")
    private String sortField = "timestamp";

    /**
     * 排序方向：ASC(升序)、DESC(降序)
     */
    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";

    /**
     * 是否启用缓存
     */
    @Schema(description = "是否启用缓存", example = "true")
    private Boolean enableCache = true;

    /**
     * 缓存时间（秒）
     */
    @Min(value = 0, message = "缓存时间不能小于0")
    @Schema(description = "缓存时间（秒）", example = "300")
    private Integer cacheTimeoutSeconds = 300;

    /**
     * 回调URL（异步采集时使用）
     */
    @Schema(description = "回调URL", example = "http://localhost:8080/api/callback/data-collection")
    private String callbackUrl;

    /**
     * 采集优先级：LOW(1)、NORMAL(5)、HIGH(10)
     */
    @Min(value = 1, message = "优先级不能小于1")
    @Max(value = 10, message = "优先级不能大于10")
    @Schema(description = "采集优先级", example = "5")
    private Integer priority = 5;

    /**
     * 超时时间（秒）
     */
    @Min(value = 1, message = "超时时间不能小于1秒")
    @Max(value = 300, message = "超时时间不能超过300秒")
    @Schema(description = "超时时间（秒）", example = "30")
    private Integer timeoutSeconds = 30;

    /**
     * 重试次数
     */
    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 5, message = "重试次数不能超过5次")
    @Schema(description = "重试次数", example = "3")
    private Integer retryCount = 3;

    /**
     * 采集标签（用于分类和查询）
     */
    @Schema(description = "采集标签", example = "production,critical")
    private String tags;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "生产设备数据采集")
    private String remarks;

    // ==================== 工具方法 ====================

    /**
     * 检查是否为实时采集
     *
     * @return 是否为实时采集
     */
    public boolean isRealTimeCollection() {
        return "REAL_TIME".equals(this.collectionType);
    }

    /**
     * 检查是否为历史数据采集
     *
     * @return 是否为历史数据采集
     */
    public boolean isHistoryCollection() {
        return "HISTORY".equals(this.collectionType);
    }

    /**
     * 检查是否为批量采集
     *
     * @return 是否为批量采集
     */
    public boolean isBatchCollection() {
        return "BATCH".equals(this.collectionType);
    }

    /**
     * 检查时间范围是否有效
     *
     * @return 时间范围是否有效
     */
    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) {
            return true; // 如果没有设置时间，则认为有效
        }
        return startTime.isBefore(endTime);
    }

    /**
     * 检查采集间隔是否合理
     *
     * @return 采集间隔是否合理
     */
    public boolean isReasonableInterval() {
        if (isRealTimeCollection()) {
            return intervalSeconds >= 1 && intervalSeconds <= 300; // 实时采集间隔不超过5分钟
        } else if (isHistoryCollection()) {
            return intervalSeconds >= 60; // 历史数据采集间隔至少1分钟
        }
        return true;
    }

    /**
     * 获取采集描述
     *
     * @return 采集描述
     */
    public String getCollectionDescription() {
        StringBuilder desc = new StringBuilder();

        desc.append("采集类型: ").append(collectionType);

        if (deviceCode != null) {
            desc.append(", 设备: ").append(deviceCode);
        }

        if (dataType != null && !"ALL".equals(dataType)) {
            desc.append(", 数据类型: ").append(dataType);
        }

        if (intervalSeconds != null) {
            desc.append(", 间隔: ").append(intervalSeconds).append("秒");
        }

        return desc.toString();
    }

    /**
     * 校验表单参数
     *
     * @throws IllegalArgumentException 参数校验失败时抛出
     */
    public void validate() {
        // 基础字段校验已在注解中完成

        // 时间范围校验
        if (!isValidTimeRange()) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }

        // 采集间隔校验
        if (!isReasonableInterval()) {
            throw new IllegalArgumentException("采集间隔设置不合理");
        }

        // 历史数据采集必须设置时间范围
        if (isHistoryCollection() && (startTime == null || endTime == null)) {
            throw new IllegalArgumentException("历史数据采集必须设置开始和结束时间");
        }

        // 实时数据采集不需要设置时间范围
        if (isRealTimeCollection() && (startTime != null || endTime != null)) {
            throw new IllegalArgumentException("实时数据采集不需要设置时间范围");
        }
    }

    /**
     * 创建默认的实时采集表单
     *
     * @param deviceId 设备ID
     * @param deviceCode 设备编号
     * @return 实时采集表单
     */
    public static DeviceDataCollectionForm createRealTimeForm(Long deviceId, String deviceCode) {
        DeviceDataCollectionForm form = new DeviceDataCollectionForm();
        form.setDeviceId(deviceId);
        form.setDeviceCode(deviceCode);
        form.setCollectionType("REAL_TIME");
        form.setDataType("ALL");
        form.setIntervalSeconds(60);
        form.setMaxRecords(1000);
        form.setIncludeDetails(false);
        form.setDataFormat("JSON");
        form.setEnableCache(true);
        form.setCacheTimeoutSeconds(300);
        form.setTimeoutSeconds(30);
        form.setRetryCount(3);
        return form;
    }

    /**
     * 创建默认的历史数据采集表单
     *
     * @param deviceId 设备ID
     * @param deviceCode 设备编号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据采集表单
     */
    public static DeviceDataCollectionForm createHistoryForm(Long deviceId, String deviceCode,
                                                          LocalDateTime startTime, LocalDateTime endTime) {
        DeviceDataCollectionForm form = new DeviceDataCollectionForm();
        form.setDeviceId(deviceId);
        form.setDeviceCode(deviceCode);
        form.setCollectionType("HISTORY");
        form.setDataType("ALL");
        form.setStartTime(startTime);
        form.setEndTime(endTime);
        form.setMaxRecords(5000);
        form.setIncludeDetails(true);
        form.setDataFormat("JSON");
        form.setEnableCache(true);
        form.setCacheTimeoutSeconds(600);
        form.setTimeoutSeconds(60);
        form.setRetryCount(2);
        return form;
    }
}
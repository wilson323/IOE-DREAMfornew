package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备异常检测结果
 * 严格遵循repowiki规范：数据传输对象，包含设备异常检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAnomalyResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型：POS-收银机，KIOSK-自助终端，MOBILE-移动设备，CARD_READER-读卡器
     */
    private String deviceType;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 设备状态：NORMAL-正常，OFFLINE-离线，MAINTENANCE-维护，ERROR-故障
     */
    private String deviceStatus;

    /**
     * 是否首次使用该设备
     */
    private Boolean isFirstTimeDevice;

    /**
     * 历史使用该设备次数
     */
    private Integer historicalDeviceUseCount;

    /**
     * 设备可信度评分（0-100）
     */
    private BigDecimal deviceTrustScore;

    /**
     * 异常置信度（0-100）
     */
    private BigDecimal anomalyConfidence;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险
     */
    private String riskLevel;

    /**
     * 异常类型：NEW_DEVICE-新设备，UNUSUAL_DEVICE-异常设备，COMPROMISED_DEVICE-风险设备
     */
    private String anomalyType;

    /**
     * 设备认证状态：AUTHENTICATED-已认证，UNAUTHENTICATED-未认证，SUSPICIOUS-可疑
     */
    private String authenticationStatus;

    /**
     * 设备固件版本
     */
    private String firmwareVersion;

    /**
     * 设备最后维护时间
     */
    private LocalDateTime lastMaintenanceTime;

    /**
     * 设备最后在线时间
     */
    private LocalDateTime lastOnlineTime;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 异常描述
     */
    private String anomalyDescription;

    /**
     * 建议处理方式
     */
    private String recommendedAction;

    /**
     * 是否需要人工审核
     */
    private Boolean requiresManualReview;

    /**
     * 匹配的规则ID
     */
    private Long ruleId;

    /**
     * 匹配的规则名称
     */
    private String ruleName;

    /**
     * 设备地理位置（纬度）
     */
    private BigDecimal deviceLatitude;

    /**
     * 设备地理位置（经度）
     */
    private BigDecimal deviceLongitude;

    /**
     * 设备网络IP地址
     */
    private String deviceIpAddress;

    /**
     * 设备MAC地址
     */
    private String deviceMacAddress;

    /**
     * 24小时内该设备交易次数
     */
    private Integer deviceTransactionCount24h;

    /**
     * 设备异常历史次数
     */
    private Integer deviceAnomalyHistoryCount;

    /**
     * 检测模型版本
     */
    private String detectionModelVersion;

    /**
     * 扩展信息
     */
    private String extendedInfo;

    /**
     * 创建新设备异常结果
     */
    public static DeviceAnomalyResult newDevice(Long userId, Long consumeRecordId, String deviceId, String deviceName) {
        return DeviceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .isFirstTimeDevice(true)
                .historicalDeviceUseCount(0)
                .anomalyType("NEW_DEVICE")
                .anomalyConfidence(new BigDecimal("75.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("首次使用新设备进行消费")
                .recommendedAction("建议进行身份验证")
                .authenticationStatus("UNAUTHENTICATED")
                .build();
    }

    /**
     * 创建风险设备异常结果
     */
    public static DeviceAnomalyResult compromisedDevice(Long userId, Long consumeRecordId, String deviceId, String deviceName) {
        return DeviceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .anomalyType("COMPROMISED_DEVICE")
                .anomalyConfidence(new BigDecimal("95.0"))
                .riskLevel("HIGH")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("检测到风险设备")
                .recommendedAction("立即暂停该设备权限并调查")
                .authenticationStatus("SUSPICIOUS")
                .deviceTrustScore(new BigDecimal("20.0"))
                .build();
    }

    /**
     * 创建异常设备使用结果
     */
    public static DeviceAnomalyResult unusualDevice(Long userId, Long consumeRecordId, String deviceId, String deviceName,
                                                   Integer historicalUseCount, BigDecimal deviceTrustScore) {
        return DeviceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .historicalDeviceUseCount(historicalUseCount)
                .deviceTrustScore(deviceTrustScore)
                .anomalyType("UNUSUAL_DEVICE")
                .anomalyConfidence(new BigDecimal("65.0"))
                .riskLevel("LOW")
                .requiresManualReview(false)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("不常用设备消费")
                .recommendedAction("监控后续使用情况")
                .build();
    }

    /**
     * 创建设备离线异常结果
     */
    public static DeviceAnomalyResult offlineDevice(Long userId, Long consumeRecordId, String deviceId, String deviceName,
                                                   LocalDateTime lastOnlineTime) {
        return DeviceAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceStatus("OFFLINE")
                .lastOnlineTime(lastOnlineTime)
                .anomalyType("UNUSUAL_DEVICE")
                .anomalyConfidence(new BigDecimal("80.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("设备状态异常离线")
                .recommendedAction("检查设备连接状态")
                .build();
    }

    /**
     * 检查是否为高风险
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel);
    }

    /**
     * 检查是否需要人工审核
     */
    public boolean needsReview() {
        return Boolean.TRUE.equals(requiresManualReview);
    }

    /**
     * 获取风险等级描述
     */
    public String getRiskLevelDescription() {
        switch (riskLevel) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            default:
                return "未知风险";
        }
    }

    /**
     * 获取异常类型描述
     */
    public String getAnomalyTypeDescription() {
        switch (anomalyType) {
            case "NEW_DEVICE":
                return "新设备";
            case "UNUSUAL_DEVICE":
                return "异常设备";
            case "COMPROMISED_DEVICE":
                return "风险设备";
            default:
                return "未知异常";
        }
    }

    /**
     * 获取设备类型描述
     */
    public String getDeviceTypeDescription() {
        switch (deviceType) {
            case "POS":
                return "收银机";
            case "KIOSK":
                return "自助终端";
            case "MOBILE":
                return "移动设备";
            case "CARD_READER":
                return "读卡器";
            default:
                return "未知设备";
        }
    }

    /**
     * 获取认证状态描述
     */
    public String getAuthenticationStatusDescription() {
        switch (authenticationStatus) {
            case "AUTHENTICATED":
                return "已认证";
            case "UNAUTHENTICATED":
                return "未认证";
            case "SUSPICIOUS":
                return "可疑";
            default:
                return "未知状态";
        }
    }

    /**
     * 检查设备是否可信
     */
    public boolean isDeviceTrusted() {
        return deviceTrustScore != null && deviceTrustScore.compareTo(new BigDecimal("60")) >= 0;
    }
}
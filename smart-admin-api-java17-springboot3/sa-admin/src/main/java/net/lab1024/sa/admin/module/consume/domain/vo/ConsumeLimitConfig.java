/*
 * 消费限额配置
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费限额配置
 * 封装各种消费限额的配置信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeLimitConfig {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 单次消费限额
     */
    private BigDecimal singleLimit;

    /**
     * 日度消费限额
     */
    private BigDecimal dailyLimit;

    /**
     * 周度消费限额
     */
    private BigDecimal weeklyLimit;

    /**
     * 月度消费限额
     */
    private BigDecimal monthlyLimit;

    /**
     * 年度消费限额
     */
    private BigDecimal yearlyLimit;

    /**
     * 日度消费次数限制
     */
    private Integer dailyCountLimit;

    /**
     * 周度消费次数限制
     */
    private Integer weeklyCountLimit;

    /**
     * 月度消费次数限制
     */
    private Integer monthlyCountLimit;

    /**
     * 最小消费金额
     */
    private BigDecimal minAmount;

    /**
     * 最大消费金额（单次）
     */
    private BigDecimal maxAmount;

    /**
     * 时间段限制配置
     */
    private Map<String, TimeSlotLimit> timeSlotLimits;

    /**
     * 星期限制配置
     */
    private Map<Integer, DayLimit> dayLimits;

    /**
     * 消费模式限制
     */
    private Map<String, BigDecimal> modeLimits;

    /**
     * 设备类型限制
     */
    private Map<String, BigDecimal> deviceTypeLimits;

    /**
     * 区域限制
     */
    private Map<String, BigDecimal> regionLimits;

    /**
     * 商户限制
     */
    private Map<String, BigDecimal> merchantLimits;

    /**
     * 商品类别限制
     */
    private Map<String, BigDecimal> categoryLimits;

    /**
     * 限额生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 限额过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 配置类型（USER/DEVICE/REGION/GLOBAL/MODE）
     */
    private String configType;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否临时配置
     */
    private Boolean isTemporary;

    /**
     * 临时配置时长（分钟）
     */
    private Integer temporaryDuration;

    /**
     * 扩展属性（JSON格式）
     */
    private String extendProperties;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 检查配置是否有效
     */
    public boolean isValid() {
        if (!Boolean.TRUE.equals(enabled)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }

        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否为临时配置
     */
    public boolean isTemporaryConfig() {
        return Boolean.TRUE.equals(isTemporary) && temporaryDuration != null && temporaryDuration > 0;
    }

    /**
     * 获取指定消费模式的限额
     */
    public BigDecimal getModeLimit(String mode) {
        return modeLimits != null ? modeLimits.get(mode) : null;
    }

    /**
     * 获取指定设备类型的限额
     */
    public BigDecimal getDeviceTypeLimit(String deviceType) {
        return deviceTypeLimits != null ? deviceTypeLimits.get(deviceType) : null;
    }

    /**
     * 获取指定区域的限额
     */
    public BigDecimal getRegionLimit(String regionId) {
        return regionLimits != null ? regionLimits.get(regionId) : null;
    }

    /**
     * 获取当前时间段的限额
     */
    public TimeSlotLimit getCurrentTimeSlotLimit() {
        if (timeSlotLimits == null || timeSlotLimits.isEmpty()) {
            return null;
        }

        int currentHour = LocalDateTime.now().getHour();
        for (TimeSlotLimit timeSlot : timeSlotLimits.values()) {
            if (timeSlot.isInTimeSlot(currentHour)) {
                return timeSlot;
            }
        }

        return null;
    }

    /**
     * 获取今天的限额配置
     */
    public DayLimit getTodayLimit() {
        if (dayLimits == null || dayLimits.isEmpty()) {
            return null;
        }

        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        return dayLimits.get(dayOfWeek);
    }

    /**
     * 计算有效单次限额（考虑多个限制条件）
     */
    public BigDecimal getEffectiveSingleLimit(String mode, String deviceType, String regionId) {
        BigDecimal effectiveLimit = singleLimit;

        // 检查消费模式限额
        BigDecimal modeLimit = getModeLimit(mode);
        if (modeLimit != null && (effectiveLimit == null || modeLimit.compareTo(effectiveLimit) < 0)) {
            effectiveLimit = modeLimit;
        }

        // 检查设备类型限额
        BigDecimal deviceLimit = getDeviceTypeLimit(deviceType);
        if (deviceLimit != null && (effectiveLimit == null || deviceLimit.compareTo(effectiveLimit) < 0)) {
            effectiveLimit = deviceLimit;
        }

        // 检查区域限额
        BigDecimal regionLimit = getRegionLimit(regionId);
        if (regionLimit != null && (effectiveLimit == null || regionLimit.compareTo(effectiveLimit) < 0)) {
            effectiveLimit = regionLimit;
        }

        // 检查时间段限额
        TimeSlotLimit currentTimeSlot = getCurrentTimeSlotLimit();
        if (currentTimeSlot != null && currentTimeSlot.getSingleLimit() != null) {
            BigDecimal timeSlotLimit = currentTimeSlot.getSingleLimit();
            if (effectiveLimit == null || timeSlotLimit.compareTo(effectiveLimit) < 0) {
                effectiveLimit = timeSlotLimit;
            }
        }

        // 检查今日限额
        DayLimit todayLimit = getTodayLimit();
        if (todayLimit != null && todayLimit.getSingleLimit() != null) {
            BigDecimal dayLimit = todayLimit.getSingleLimit();
            if (effectiveLimit == null || dayLimit.compareTo(effectiveLimit) < 0) {
                effectiveLimit = dayLimit;
            }
        }

        return effectiveLimit;
    }

    /**
     * 创建默认限额配置
     */
    public static ConsumeLimitConfig createDefault() {
        return ConsumeLimitConfig.builder()
                .singleLimit(new BigDecimal("500.00"))
                .dailyLimit(new BigDecimal("2000.00"))
                .weeklyLimit(new BigDecimal("10000.00"))
                .monthlyLimit(new BigDecimal("30000.00"))
                .yearlyLimit(new BigDecimal("300000.00"))
                .dailyCountLimit(20)
                .weeklyCountLimit(100)
                .monthlyCountLimit(300)
                .minAmount(new BigDecimal("0.01"))
                .maxAmount(new BigDecimal("1000.00"))
                .enabled(true)
                .priority(100)
                .build();
    }

    /**
     * 创建高安全限额配置
     */
    public static ConsumeLimitConfig createHighSecurity() {
        return ConsumeLimitConfig.builder()
                .singleLimit(new BigDecimal("200.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .weeklyLimit(new BigDecimal("5000.00"))
                .monthlyLimit(new BigDecimal("15000.00"))
                .yearlyLimit(new BigDecimal("150000.00"))
                .dailyCountLimit(10)
                .weeklyCountLimit(50)
                .monthlyCountLimit(150)
                .minAmount(new BigDecimal("0.01"))
                .maxAmount(new BigDecimal("500.00"))
                .enabled(true)
                .priority(200)
                .build();
    }

    /**
     * 创建宽松限额配置
     */
    public static ConsumeLimitConfig createRelaxed() {
        return ConsumeLimitConfig.builder()
                .singleLimit(new BigDecimal("1000.00"))
                .dailyLimit(new BigDecimal("5000.00"))
                .weeklyLimit(new BigDecimal("20000.00"))
                .monthlyLimit(new BigDecimal("80000.00"))
                .yearlyLimit(new BigDecimal("800000.00"))
                .dailyCountLimit(50)
                .weeklyCountLimit(200)
                .monthlyCountLimit(600)
                .minAmount(new BigDecimal("0.01"))
                .maxAmount(new BigDecimal("2000.00"))
                .enabled(true)
                .priority(50)
                .build();
    }
}
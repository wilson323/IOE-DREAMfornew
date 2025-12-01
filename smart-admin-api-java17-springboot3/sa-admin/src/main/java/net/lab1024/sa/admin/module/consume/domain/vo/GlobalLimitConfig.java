package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 全局限制配置VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "全局限制配置")
public class GlobalLimitConfig {

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "全局单次限额")
    private BigDecimal globalSingleLimit;

    @Schema(description = "全局日限额")
    private BigDecimal globalDailyLimit;

    @Schema(description = "全局周限额")
    private BigDecimal globalWeeklyLimit;

    @Schema(description = "全局月限额")
    private BigDecimal globalMonthlyLimit;

    @Schema(description = "全局年限额")
    private BigDecimal globalYearlyLimit;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "操作员ID")
    private Long operatorId;

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    /**
     * 检查是否超过限额
     */
    public boolean isOverLimit(String limitType, BigDecimal amount) {
        if (!isEnabled() || amount == null) {
            return false;
        }

        switch (limitType.toLowerCase()) {
            case "single":
                return globalSingleLimit != null && amount.compareTo(globalSingleLimit) > 0;
            case "daily":
                return globalDailyLimit != null && amount.compareTo(globalDailyLimit) > 0;
            case "weekly":
                return globalWeeklyLimit != null && amount.compareTo(globalWeeklyLimit) > 0;
            case "monthly":
                return globalMonthlyLimit != null && amount.compareTo(globalMonthlyLimit) > 0;
            case "yearly":
                return globalYearlyLimit != null && amount.compareTo(globalYearlyLimit) > 0;
            default:
                return false;
        }
    }

    /**
     * 获取指定类型的限额
     */

    /**
     * 创建默认配置
     */
    public static GlobalLimitConfig createDefault() {
        return GlobalLimitConfig.builder()
                .configName("全局默认限制")
                .globalSingleLimit(new BigDecimal("1000.00"))
                .globalDailyLimit(new BigDecimal("10000.00"))
                .globalWeeklyLimit(new BigDecimal("50000.00"))
                .globalMonthlyLimit(new BigDecimal("200000.00"))
                .globalYearlyLimit(new BigDecimal("2000000.00"))
                .status(1)
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建禁用配置
     */
    public static GlobalLimitConfig createDisabled() {
        return GlobalLimitConfig.builder()
                .configName("全局限制已禁用")
                .status(0)
                .enabled(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)

























}

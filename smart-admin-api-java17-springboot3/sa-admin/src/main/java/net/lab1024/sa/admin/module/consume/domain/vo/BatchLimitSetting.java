package net.lab1024.sa.admin.module.consume.domain.vo;
import lombok.extern.slf4j.Slf4j;


import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批量限制设置VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "批量限制设置")
// // @Slf4j - 手动添加log变量替代Lombok注解 - 手动添加log变量替代Lombok注解
public class BatchLimitSetting {

    // // @Slf4j - 手动添加log变量替代Lombok注解 - 手动添加log变量替代Lombok注解

    @Schema(description = "用户ID列表")
    private List<Long> userIds;

    @Schema(description = "人员ID列表")
    private List<Long> personIds;

    @Schema(description = "单日限制")
    private BigDecimal dailyLimit;

    @Schema(description = "单次限制")
    private BigDecimal singleLimit;

    @Schema(description = "月度限制")
    private BigDecimal monthlyLimit;

    @Schema(description = "限制配置")
    private ConsumeLimitConfig limitConfig;

    @Schema(description = "操作人员ID")
    private Long operatorId;

    @Schema(description = "操作人员姓名")
    private String operatorName;

    /**
     * 获取人员ID
     * @return 人员ID
     */

    /**
     * 创建默认设置
     */
    public static BatchLimitSetting createDefault(List<Long> userIds) {
        ConsumeLimitConfig config = ConsumeLimitConfig.createDefault();
        return BatchLimitSetting.builder()
                .userIds(userIds)
                .personIds(userIds)  // 默认人员ID和用户ID相同
                .limitConfig(config)
                .build();
    }

    /**
     * 创建设置指定限制
     */
    public static BatchLimitSetting createWithLimits(List<Long> userIds, BigDecimal dailyLimit,
                                                    BigDecimal singleLimit, BigDecimal monthlyLimit) {
        ConsumeLimitConfig config = ConsumeLimitConfig.createDefault();
        config.setDailyLimit(dailyLimit);
        config.setSingleLimit(singleLimit);
        config.setMonthlyLimit(monthlyLimit);

        return BatchLimitSetting.builder()
                .userIds(userIds)
                .personIds(userIds)  // 默认人员ID和用户ID相同
                .dailyLimit(dailyLimit)
                .singleLimit(singleLimit)
                .monthlyLimit(monthlyLimit)
                .limitConfig(config)
                .build();
    }

    // ========== 手动添加的getter/setter方法 (Lombok失效备用) ==========

















}

package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会员等级枚举
 * 严格遵循repowiki规范：定义不同会员等级的权益和折扣策略
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Getter
@AllArgsConstructor
public enum MemberLevelEnum {

    /**
     * 普通会员 - 无折扣
     */
    NORMAL("NORMAL", "普通会员", "新用户注册默认等级",
            BigDecimal.ZERO, 0, BigDecimal.ZERO),

    /**
     * 铜卡会员 - 9.5折
     */
    SILVER("SILVER", "银卡会员", "消费满500升级",
            new BigDecimal("0.95"), 1, new BigDecimal("500")),

    /**
     * 金卡会员 - 9折
     */
    GOLD("GOLD", "金卡会员", "消费满2000升级",
            new BigDecimal("0.90"), 2, new BigDecimal("2000")),

    /**
     * 白金会员 - 8.5折
     */
    PLATINUM("PLATINUM", "白金会员", "消费满5000升级",
            new BigDecimal("0.85"), 3, new BigDecimal("5000")),

    /**
     * 钻石会员 - 8折
     */
    DIAMOND("DIAMOND", "钻石会员", "消费满10000升级",
            new BigDecimal("0.80"), 4, new BigDecimal("10000")),

    /**
     * VIP会员 - 7.5折
     */
    VIP("VIP", "VIP会员", "特邀用户",
            new BigDecimal("0.75"), 5, null);

    private final String code;
    private final String name;
    private final String description;
    private final BigDecimal discountRate;
    private final Integer level;
    private final BigDecimal upgradeThreshold;

    /**
     * 获取会员折扣后的价格
     */
    public BigDecimal getMemberPrice(BigDecimal originalPrice) {
        if (originalPrice == null) {
            return null;
        }
        return originalPrice.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算会员折扣金额
     */
    public BigDecimal getMemberDiscountAmount(BigDecimal originalPrice) {
        if (originalPrice == null) {
            return null;
        }
        return originalPrice.subtract(getMemberPrice(originalPrice));
    }

    /**
     * 检查是否有会员折扣
     */
    public boolean hasMemberDiscount() {
        return discountRate.compareTo(BigDecimal.ZERO) > 0 && discountRate.compareTo(BigDecimal.ONE) < 0;
    }

    /**
     * 获取折扣百分比字符串
     */
    public String getDiscountPercentage() {
        if (!hasMemberDiscount()) {
            return "无折扣";
        }
        BigDecimal percentage = BigDecimal.ONE.subtract(discountRate).multiply(new BigDecimal("100"));
        return percentage.setScale(0, BigDecimal.ROUND_HALF_UP) + "%";
    }

    /**
     * 检查是否高于指定等级
     */
    public boolean isHigherThan(MemberLevelEnum other) {
        return this.level > other.level;
    }

    /**
     * 检查是否低于指定等级
     */
    public boolean isLowerThan(MemberLevelEnum other) {
        return this.level < other.level;
    }

    /**
     * 检查是否可以升级到指定等级
     */
    public boolean canUpgradeTo(MemberLevelEnum targetLevel) {
        return targetLevel.isHigherThan(this);
    }

    /**
     * 根据代码获取会员等级
     */
    public static MemberLevelEnum fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (MemberLevelEnum memberLevel : values()) {
            if (memberLevel.getCode().equals(code)) {
                return memberLevel;
            }
        }

        return null;
    }

    /**
     * 检查代码是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * 根据消费金额获取建议的会员等级
     */
    public static MemberLevelEnum getRecommendedLevel(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return NORMAL;
        }

        for (int i = values().length - 1; i >= 0; i--) {
            MemberLevelEnum level = values()[i];
            if (level.getUpgradeThreshold() == null || totalAmount.compareTo(level.getUpgradeThreshold()) >= 0) {
                return level;
            }
        }

        return NORMAL;
    }

    /**
     * 检查是否可以升级（基于累计消费）
     */
    public boolean canUpgrade(BigDecimal totalAmount) {
        if (this == VIP || totalAmount == null) {
            return false;
        }

        // 查找下一个等级
        MemberLevelEnum nextLevel = getNextLevel();
        return nextLevel != null && totalAmount.compareTo(nextLevel.getUpgradeThreshold()) >= 0;
    }

    /**
     * 获取下一个等级
     */
    public MemberLevelEnum getNextLevel() {
        MemberLevelEnum[] levels = values();
        for (int i = 0; i < levels.length - 1; i++) {
            if (levels[i] == this) {
                return levels[i + 1];
            }
        }
        return null; // 已经是最高等级
    }

    /**
     * 获取上一个等级
     */
    public MemberLevelEnum getPreviousLevel() {
        MemberLevelEnum[] levels = values();
        for (int i = 1; i < levels.length; i++) {
            if (levels[i] == this) {
                return levels[i - 1];
            }
        }
        return null; // 已经是最低等级
    }

    /**
     * 获取所有有折扣的会员等级
     */
    public static List<MemberLevelEnum> getDiscountedLevels() {
        return Arrays.stream(values())
                .filter(MemberLevelEnum::hasMemberDiscount)
                .collect(Collectors.toList());
    }

    /**
     * 获取会员等级代码到折扣率的映射
     */
    public static Map<String, BigDecimal> getDiscountRateMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        MemberLevelEnum::getCode,
                        MemberLevelEnum::getDiscountRate
                ));
    }

    /**
     * 应用会员折扣到价格
     */
    public static BigDecimal applyMemberDiscount(BigDecimal originalPrice, String memberLevelCode) {
        MemberLevelEnum memberLevel = fromCode(memberLevelCode);
        if (memberLevel == null || !memberLevel.hasMemberDiscount()) {
            return originalPrice;
        }
        return memberLevel.getMemberPrice(originalPrice);
    }

    /**
     * 获取会员权益信息
     */
    public Map<String, Object> getMemberBenefits() {
        return Map.of(
                "level", level,
                "discountRate", discountRate,
                "discountPercentage", getDiscountPercentage(),
                "hasDiscount", hasMemberDiscount(),
                "upgradeThreshold", upgradeThreshold
        );
    }

    /**
     * 获取升级建议
     */
    public Map<String, Object> getUpgradeSuggestion(BigDecimal currentAmount) {
        MemberLevelEnum nextLevel = getNextLevel();
        if (nextLevel == null) {
            return Map.of(
                    "canUpgrade", false,
                    "message", "已经是最高等级",
                    "currentLevel", this.name
            );
        }

        BigDecimal neededAmount = nextLevel.getUpgradeThreshold().subtract(currentAmount);
        boolean canUpgrade = neededAmount.compareTo(BigDecimal.ZERO) <= 0;

        return Map.of(
                "canUpgrade", canUpgrade,
                "currentLevel", this.name,
                "nextLevel", nextLevel.getName(),
                "nextLevelCode", nextLevel.getCode(),
                "nextDiscount", nextLevel.getDiscountPercentage(),
                "neededAmount", canUpgrade ? BigDecimal.ZERO : neededAmount,
                "upgradeThreshold", nextLevel.getUpgradeThreshold()
        );
    }

    /**
     * 检查会员等级是否有效
     */
    public boolean isValid() {
        return code != null && name != null && level != null;
    }
}
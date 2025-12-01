package net.lab1024.sa.admin.module.device.domain.enums;

import lombok.Getter;

/**
 * 设备健康等级枚举
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Getter
public enum DeviceHealthLevelEnum {

    HEALTHY("healthy", "健康", 85, 100),
    WARNING("warning", "警告", 60, 84),
    CRITICAL("critical", "危险", 0, 59),
    UNKNOWN("unknown", "未知", 0, 100);

    private final String code;
    private final String description;
    private final Integer minScore;
    private final Integer maxScore;

    DeviceHealthLevelEnum(String code, String description, Integer minScore, Integer maxScore) {
        this.code = code;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * 根据评分获取健康等级
     */
    public static DeviceHealthLevelEnum getByScore(Integer score) {
        if (score == null) {
            return UNKNOWN;
        }

        for (DeviceHealthLevelEnum level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }
        return CRITICAL;
    }

    /**
     * 根据代码获取健康等级
     */
    public static DeviceHealthLevelEnum getByCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }

        for (DeviceHealthLevelEnum level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return UNKNOWN;
    }
}
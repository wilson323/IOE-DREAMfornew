package net.lab1024.sa.admin.module.smart.biometric.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 生物识别告警级别枚举
 *
 * <p>定义生物识别系统中的告警级别，用于区分不同严重程度的事件和异常情况。
 * 级别从低到高分别为：LOW、MEDIUM、HIGH、CRITICAL</p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-28
 */
@Getter
@Schema(description = "告警级别枚举")
public enum AlertLevel implements BaseEnum {

    /**
     * 低级别告警
     *
     * <p>适用于一般性的信息通知，如设备状态变化、常规操作记录等</p>
     */
    LOW("LOW", "低级别", 1),

    /**
     * 中级别告警
     *
     * <p>适用于需要关注的异常情况，如识别成功率下降、设备轻微故障等</p>
     */
    MEDIUM("MEDIUM", "中级别", 2),

    /**
     * 高级别告警
     *
     * <p>适用于严重的异常情况，如安全风险、设备严重故障等</p>
     */
    HIGH("HIGH", "高级别", 3),

    /**
     * 关键级别告警
     *
     * <p>适用于最严重的安全事件，如非法入侵、系统崩溃等</p>
     */
    CRITICAL("CRITICAL", "关键级别", 4);

    private final String value;
    private final String description;
    private final int level;

    AlertLevel(String value, String description, int level) {
        this.value = value;
        this.description = description;
        this.level = level;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return description;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return 对应的枚举，如果不存在则返回null
     */
    public static AlertLevel fromValue(String value) {
        for (AlertLevel level : values()) {
            if (level.getValue().equals(value)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 获取级别数值
     *
     * @return 级别数值（1-4）
     */
    public int getLevel() {
        return level;
    }

    /**
     * 判断是否为高级别以上告警
     *
     * @return true如果是HIGH或CRITICAL级别
     */
    public boolean isHighPriority() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * 判断是否需要立即处理
     *
     * @return true如果是CRITICAL级别
     */
    public boolean requiresImmediateAction() {
        return this == CRITICAL;
    }

    /**
     * 获取建议的处理时效（分钟）
     *
     * @return 建议处理时效
     */
    public int getSuggestedResponseTime() {
        switch (this) {
            case LOW:
                return 1440;    // 24小时
            case MEDIUM:
                return 240;     // 4小时
            case HIGH:
                return 60;      // 1小时
            case CRITICAL:
                return 5;       // 5分钟
            default:
                return 1440;
        }
    }

    /**
     * 获取告警颜色代码
     *
     * @return 十六进制颜色代码
     */
    public String getColorCode() {
        switch (this) {
            case LOW:
                return "#52C41A";  // 绿色
            case MEDIUM:
                return "#FAAD14";  // 橙色
            case HIGH:
                return "#FA8C16";  // 深橙色
            case CRITICAL:
                return "#F5222D";  // 红色
            default:
                return "#52C41A";
        }
    }

    /**
     * 获取状态图标
     *
     * @return 状态图标Unicode字符
     */
    public String getIcon() {
        switch (this) {
            case LOW:
                return "ⓘ️";
            case MEDIUM:
                return "⚠️";
            case HIGH:
                return "🚨";
            case CRITICAL:
                return "🔴";
            default:
                return "❓";
        }
    }
}
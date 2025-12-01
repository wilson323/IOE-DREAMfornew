/*
 * 星期限额配置
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

/**
 * 星期限额配置
 * 定义特定星期的消费限制
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayLimit {

    /**
     * 星期编号（1-7，1表示周一，7表示周日）
     */
    private Integer dayOfWeek;

    /**
     * 星期名称
     */
    private String dayName;

    /**
     * 单次消费限额
     */
    private BigDecimal singleLimit;

    /**
     * 当日累计限额
     */
    private BigDecimal dailyLimit;

    /**
     * 当日消费次数限制
     */
    private Integer countLimit;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 描述
     */
    private String description;

    /**
     * 获取星期名称
     */
    public String getDayName() {
        if (dayName != null) {
            return dayName;
        }

        if (dayOfWeek == null) {
            return "未知";
        }

        switch (dayOfWeek) {
            case 1:
                return "周一";
            case 2:
                return "周二";
            case 3:
                return "周三";
            case 4:
                return "周四";
            case 5:
                return "周五";
            case 6:
                return "周六";
            case 7:
                return "周日";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否为工作日
     */
    public boolean isWeekday() {
        return dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 5;
    }

    /**
     * 检查是否为周末
     */
    public boolean isWeekend() {
        return dayOfWeek != null && (dayOfWeek == 6 || dayOfWeek == 7);
    }

    /**
     * 创建工作日限额配置
     */
    public static DayLimit createWeekday() {
        return DayLimit.builder()
                .dayOfWeek(1) // 周一作为代表
                .dayName("工作日")
                .singleLimit(new BigDecimal("30.00"))
                .dailyLimit(new BigDecimal("100.00"))
                .countLimit(8)
                .enabled(true)
                .priority(100)
                .description("工作日消费限额")
                .build();
    }

    /**
     * 创建周末限额配置
     */
    public static DayLimit createWeekend() {
        return DayLimit.builder()
                .dayOfWeek(6) // 周六作为代表
                .dayName("周末")
                .singleLimit(new BigDecimal("50.00"))
                .dailyLimit(new BigDecimal("200.00"))
                .countLimit(12)
                .enabled(true)
                .priority(100)
                .description("周末消费限额")
                .build();
    }

    /**
     * 创建周一限额配置
     */
    public static DayLimit createMonday() {
        return DayLimit.builder()
                .dayOfWeek(1)
                .dayName("周一")
                .singleLimit(new BigDecimal("25.00"))
                .dailyLimit(new BigDecimal("80.00"))
                .countLimit(6)
                .enabled(true)
                .priority(100)
                .description("周一消费限额")
                .build();
    }

    /**
     * 创建周二限额配置
     */
    public static DayLimit createTuesday() {
        return DayLimit.builder()
                .dayOfWeek(2)
                .dayName("周二")
                .singleLimit(new BigDecimal("25.00"))
                .dailyLimit(new BigDecimal("80.00"))
                .countLimit(6)
                .enabled(true)
                .priority(100)
                .description("周二消费限额")
                .build();
    }

    /**
     * 创建周三限额配置
     */
    public static DayLimit createWednesday() {
        return DayLimit.builder()
                .dayOfWeek(3)
                .dayName("周三")
                .singleLimit(new BigDecimal("25.00"))
                .dailyLimit(new BigDecimal("80.00"))
                .countLimit(6)
                .enabled(true)
                .priority(100)
                .description("周三消费限额")
                .build();
    }

    /**
     * 创建周四限额配置
     */
    public static DayLimit createThursday() {
        return DayLimit.builder()
                .dayOfWeek(4)
                .dayName("周四")
                .singleLimit(new BigDecimal("25.00"))
                .dailyLimit(new BigDecimal("80.00"))
                .countLimit(6)
                .enabled(true)
                .priority(100)
                .description("周四消费限额")
                .build();
    }

    /**
     * 创建周五限额配置
     */
    public static DayLimit createFriday() {
        return DayLimit.builder()
                .dayOfWeek(5)
                .dayName("周五")
                .singleLimit(new BigDecimal("35.00"))
                .dailyLimit(new BigDecimal("120.00"))
                .countLimit(8)
                .enabled(true)
                .priority(100)
                .description("周五消费限额")
                .build();
    }

    /**
     * 创建周六限额配置
     */
    public static DayLimit createSaturday() {
        return DayLimit.builder()
                .dayOfWeek(6)
                .dayName("周六")
                .singleLimit(new BigDecimal("50.00"))
                .dailyLimit(new BigDecimal("200.00"))
                .countLimit(12)
                .enabled(true)
                .priority(100)
                .description("周六消费限额")
                .build();
    }

    /**
     * 创建周日限额配置
     */
    public static DayLimit createSunday() {
        return DayLimit.builder()
                .dayOfWeek(7)
                .dayName("周日")
                .singleLimit(new BigDecimal("50.00"))
                .dailyLimit(new BigDecimal("200.00"))
                .countLimit(12)
                .enabled(true)
                .priority(100)
                .description("周日消费限额")
                .build();
    }

    /**
     * 创建节假日限额配置
     */
    public static DayLimit createHoliday() {
        return DayLimit.builder()
                .dayOfWeek(0) // 特殊标记节假日
                .dayName("节假日")
                .singleLimit(new BigDecimal("60.00"))
                .dailyLimit(new BigDecimal("300.00"))
                .countLimit(15)
                .enabled(true)
                .priority(200)
                .description("节假日消费限额")
                .build();
    }
}
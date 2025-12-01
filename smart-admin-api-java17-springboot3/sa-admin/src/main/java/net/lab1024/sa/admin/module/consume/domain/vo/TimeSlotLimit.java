/*
 * 时间段限额配置
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
import java.time.LocalTime;

/**
 * 时间段限额配置
 * 定义特定时间段的消费限制
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotLimit {

    /**
     * 时间段名称
     */
    private String slotName;

    /**
     * 开始时间（HH:mm格式）
     */
    private LocalTime startTime;

    /**
     * 结束时间（HH:mm格式）
     */
    private LocalTime endTime;

    /**
     * 单次消费限额
     */
    private BigDecimal singleLimit;

    /**
     * 时间段累计限额
     */
    private BigDecimal slotLimit;

    /**
     * 时间段消费次数限制
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
     * 适用星期（1-7，1表示周一，7表示周日）
     */
    private String applicableDays;

    /**
     * 描述
     */
    private String description;

    /**
     * 检查指定时间是否在时间段内
     */
    public boolean isInTimeSlot(LocalTime time) {
        if (startTime == null || endTime == null || !Boolean.TRUE.equals(enabled)) {
            return false;
        }

        if (startTime.isBefore(endTime)) {
            // 正常时间段，如 09:00-17:00
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        } else {
            // 跨天时间段，如 22:00-06:00
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        }
    }

    /**
     * 检查指定小时是否在时间段内
     */
    public boolean isInTimeSlot(int hour) {
        LocalTime time = LocalTime.of(hour, 0);
        return isInTimeSlot(time);
    }

    /**
     * 检查今天是否适用此时间段
     */
    public boolean isApplicableToday() {
        if (applicableDays == null || applicableDays.isEmpty()) {
            return true; // 如果没有指定星期，则全部适用
        }

        int dayOfWeek = java.time.LocalDateTime.now().getDayOfWeek().getValue();
        return applicableDays.contains(String.valueOf(dayOfWeek));
    }

    /**
     * 检查当前时间是否适用此时间段
     */
    public boolean isCurrentlyApplicable() {
        return isApplicableToday() && isInTimeSlot(LocalTime.now());
    }

    /**
     * 创建早餐时间段限额（06:00-10:00）
     */
    public static TimeSlotLimit createBreakfast() {
        return TimeSlotLimit.builder()
                .slotName("早餐时段")
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(10, 0))
                .singleLimit(new BigDecimal("20.00"))
                .slotLimit(new BigDecimal("50.00"))
                .countLimit(3)
                .enabled(true)
                .priority(100)
                .applicableDays("1,2,3,4,5,6,7") // 每天
                .description("早餐时间段消费限额")
                .build();
    }

    /**
     * 创建午餐时间段限额（11:00-14:00）
     */
    public static TimeSlotLimit createLunch() {
        return TimeSlotLimit.builder()
                .slotName("午餐时段")
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(14, 0))
                .singleLimit(new BigDecimal("30.00"))
                .slotLimit(new BigDecimal("80.00"))
                .countLimit(3)
                .enabled(true)
                .priority(100)
                .applicableDays("1,2,3,4,5") // 工作日
                .description("午餐时间段消费限额")
                .build();
    }

    /**
     * 创建晚餐时间段限额（17:00-21:00）
     */
    public static TimeSlotLimit createDinner() {
        return TimeSlotLimit.builder()
                .slotName("晚餐时段")
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(21, 0))
                .singleLimit(new BigDecimal("40.00"))
                .slotLimit(new BigDecimal("100.00"))
                .countLimit(3)
                .enabled(true)
                .priority(100)
                .applicableDays("1,2,3,4,5,6,7") // 每天
                .description("晚餐时间段消费限额")
                .build();
    }

    /**
     * 创建夜宵时间段限额（21:00-24:00）
     */
    public static TimeSlotLimit createLateNight() {
        return TimeSlotLimit.builder()
                .slotName("夜宵时段")
                .startTime(LocalTime.of(21, 0))
                .endTime(LocalTime.of(23, 59))
                .singleLimit(new BigDecimal("25.00"))
                .slotLimit(new BigDecimal("60.00"))
                .countLimit(2)
                .enabled(true)
                .priority(80)
                .applicableDays("5,6,7") // 周五到周日
                .description("夜宵时间段消费限额")
                .build();
    }

    /**
     * 创建全天时间段限额
     */
    public static TimeSlotLimit createAllDay() {
        return TimeSlotLimit.builder()
                .slotName("全天时段")
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(23, 59))
                .singleLimit(new BigDecimal("500.00"))
                .slotLimit(new BigDecimal("2000.00"))
                .countLimit(20)
                .enabled(true)
                .priority(1)
                .applicableDays("1,2,3,4,5,6,7")
                .description("全天消费限额")
                .build();
    }
}
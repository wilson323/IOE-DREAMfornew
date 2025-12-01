package net.lab1024.sa.admin.module.smart.access.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 时间段权限验证器单元测试
 * <p>
 * 测试时间段验证器的各种场景：
 * - 工作日时间段验证
 * - 周末时间段验证
 * - 节假日时间段验证
 * - 特定日期时间段验证
 * - 跨天时间段验证
 * - 多个时间段组合验证
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@DisplayName("时间段权限验证器单元测试")
class TimeSlotValidatorTest {

    /**
     * 测试工作日时间段验证 - 在允许时间内
     */
    @Test
    @DisplayName("测试工作日时间段验证 - 在允许时间内")
    void testValidateWeekday_InAllowedTime() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekday",
                      "days": [1, 2, 3, 4, 5],
                      "timeRanges": [
                        {"start": "08:00", "end": "18:00"}
                      ]
                    }
                  ]
                }
                """;

        // 周一 10:00 - 在工作日允许时间内
        LocalDateTime monday10am = LocalDateTime.of(2025, 11, 17, 10, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday10am), "周一10:00应该在允许时间内");

        // 周五 15:00 - 在工作日允许时间内
        LocalDateTime friday3pm = LocalDateTime.of(2025, 11, 21, 15, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, friday3pm), "周五15:00应该在允许时间内");
    }

    /**
     * 测试工作日时间段验证 - 在允许时间外
     */
    @Test
    @DisplayName("测试工作日时间段验证 - 在允许时间外")
    void testValidateWeekday_OutOfAllowedTime() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekday",
                      "days": [1, 2, 3, 4, 5],
                      "timeRanges": [
                        {"start": "08:00", "end": "18:00"}
                      ]
                    }
                  ]
                }
                """;

        // 周一 07:00 - 在工作日允许时间外
        LocalDateTime monday7am = LocalDateTime.of(2025, 11, 17, 7, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, monday7am), "周一07:00应该在允许时间外");

        // 周五 19:00 - 在工作日允许时间外
        LocalDateTime friday7pm = LocalDateTime.of(2025, 11, 21, 19, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, friday7pm), "周五19:00应该在允许时间外");
    }

    /**
     * 测试周末时间段验证
     */
    @Test
    @DisplayName("测试周末时间段验证")
    void testValidateWeekend() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekend",
                      "days": [6, 7],
                      "timeRanges": [
                        {"start": "10:00", "end": "16:00"}
                      ]
                    }
                  ]
                }
                """;

        // 周六 12:00 - 在周末允许时间内
        LocalDateTime saturday12pm = LocalDateTime.of(2025, 11, 22, 12, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, saturday12pm), "周六12:00应该在允许时间内");

        // 周日 14:00 - 在周末允许时间内
        LocalDateTime sunday2pm = LocalDateTime.of(2025, 11, 23, 14, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, sunday2pm), "周日14:00应该在允许时间内");

        // 周一 12:00 - 不在周末
        LocalDateTime monday12pm = LocalDateTime.of(2025, 11, 17, 12, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, monday12pm), "周一12:00不应该在周末允许时间内");
    }

    /**
     * 测试节假日时间段验证
     */
    @Test
    @DisplayName("测试节假日时间段验证")
    void testValidateHoliday() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "holiday",
                      "dates": ["2025-01-01", "2025-05-01"],
                      "timeRanges": [
                        {"start": "09:00", "end": "17:00"}
                      ]
                    }
                  ]
                }
                """;

        // 2025-01-01 10:00 - 在节假日允许时间内
        LocalDateTime newYear10am = LocalDateTime.of(2025, 1, 1, 10, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, newYear10am), "元旦10:00应该在允许时间内");

        // 2025-05-01 15:00 - 在节假日允许时间内
        LocalDateTime laborDay3pm = LocalDateTime.of(2025, 5, 1, 15, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, laborDay3pm), "劳动节15:00应该在允许时间内");

        // 2025-01-02 10:00 - 不在节假日
        LocalDateTime normalDay10am = LocalDateTime.of(2025, 1, 2, 10, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, normalDay10am), "非节假日不应该在允许时间内");
    }

    /**
     * 测试跨天时间段验证
     */
    @Test
    @DisplayName("测试跨天时间段验证")
    void testValidateCrossDay() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekday",
                      "days": [1, 2, 3, 4, 5],
                      "timeRanges": [
                        {"start": "22:00", "end": "06:00"}
                      ]
                    }
                  ]
                }
                """;

        // 周一 23:00 - 在跨天时间段内（当天部分）
        LocalDateTime monday11pm = LocalDateTime.of(2025, 11, 17, 23, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday11pm), "周一23:00应该在跨天时间段内");

        // 周二 05:00 - 在跨天时间段内（次日部分）
        LocalDateTime tuesday5am = LocalDateTime.of(2025, 11, 18, 5, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, tuesday5am), "周二05:00应该在跨天时间段内");

        // 周一 10:00 - 不在跨天时间段内
        LocalDateTime monday10am = LocalDateTime.of(2025, 11, 17, 10, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, monday10am), "周一10:00不应该在跨天时间段内");
    }

    /**
     * 测试多个时间段组合验证
     */
    @Test
    @DisplayName("测试多个时间段组合验证")
    void testValidateMultipleTimeSlots() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekday",
                      "days": [1, 2, 3, 4, 5],
                      "timeRanges": [
                        {"start": "08:00", "end": "12:00"},
                        {"start": "14:00", "end": "18:00"}
                      ]
                    },
                    {
                      "type": "weekend",
                      "days": [6, 7],
                      "timeRanges": [
                        {"start": "10:00", "end": "16:00"}
                      ]
                    }
                  ]
                }
                """;

        // 周一 10:00 - 在第一个时间段内
        LocalDateTime monday10am = LocalDateTime.of(2025, 11, 17, 10, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday10am), "周一10:00应该在允许时间内");

        // 周一 15:00 - 在第二个时间段内
        LocalDateTime monday3pm = LocalDateTime.of(2025, 11, 17, 15, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday3pm), "周一15:00应该在允许时间内");

        // 周六 12:00 - 在周末时间段内
        LocalDateTime saturday12pm = LocalDateTime.of(2025, 11, 22, 12, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, saturday12pm), "周六12:00应该在允许时间内");
    }

    /**
     * 测试空时间段配置 - 应该默认允许
     */
    @Test
    @DisplayName("测试空时间段配置 - 应该默认允许")
    void testValidateEmptyConfig() {
        // 空配置
        assertTrue(TimeSlotValidator.validate(null, LocalDateTime.now()), "空配置应该默认允许");
        assertTrue(TimeSlotValidator.validate("", LocalDateTime.now()), "空字符串应该默认允许");
        assertTrue(TimeSlotValidator.validate("{}", LocalDateTime.now()), "空JSON应该默认允许");
    }

    /**
     * 测试边界时间点
     */
    @Test
    @DisplayName("测试边界时间点")
    void testValidateBoundaryTime() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekday",
                      "days": [1, 2, 3, 4, 5],
                      "timeRanges": [
                        {"start": "08:00", "end": "18:00"}
                      ]
                    }
                  ]
                }
                """;

        // 周一 08:00 - 开始时间点
        LocalDateTime monday8am = LocalDateTime.of(2025, 11, 17, 8, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday8am), "开始时间点应该允许");

        // 周一 18:00 - 结束时间点
        LocalDateTime monday6pm = LocalDateTime.of(2025, 11, 17, 18, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday6pm), "结束时间点应该允许");
    }

    /**
     * 测试无效JSON配置 - 应该返回false（安全策略）
     */
    @Test
    @DisplayName("测试无效JSON配置 - 应该返回false（安全策略）")
    void testValidateInvalidJson() {
        String invalidJson = "{ invalid json }";
        LocalDateTime now = LocalDateTime.now();

        // 无效JSON应该返回false（安全策略：验证失败时不允许访问）
        assertFalse(TimeSlotValidator.validate(invalidJson, now), "无效JSON应该返回false");
    }
}

package net.lab1024.sa.admin.module.smart.access.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.admin.module.smart.access.util.TimeSlotValidator;

/**
 * 时间段权限验证器集成测试
 * <p>
 * 测试时间段验证器在实际业务场景中的使用
 * - 与权限实体集成测试
 * - 复杂时间段配置测试
 * - 性能测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("时间段权限验证器集成测试")
class TimeSlotValidatorIntegrationTest {

    /**
     * 测试工作日时间段在实际业务中的使用
     */
    @Test
    @DisplayName("测试工作日时间段在实际业务中的使用")
    void testWeekdayTimeSlotInBusiness() {
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
                    }
                  ]
                }
                """;

        // 测试上午时间段
        LocalDateTime monday9am = LocalDateTime.of(2025, 11, 17, 9, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday9am), "周一上午9:00应该允许");

        // 测试下午时间段
        LocalDateTime monday3pm = LocalDateTime.of(2025, 11, 17, 15, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday3pm), "周一下午15:00应该允许");

        // 测试中午时间段（不在允许范围内）
        LocalDateTime monday1pm = LocalDateTime.of(2025, 11, 17, 13, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, monday1pm), "周一中午13:00不应该允许");
    }

    /**
     * 测试复杂时间段组合在实际业务中的使用
     */
    @Test
    @DisplayName("测试复杂时间段组合在实际业务中的使用")
    void testComplexTimeSlotInBusiness() {
        String timePeriodsJson = """
                {
                  "timeSlots": [
                    {
                      "type": "weekday",
                      "days": [1, 2, 3, 4, 5],
                      "timeRanges": [
                        {"start": "08:00", "end": "18:00"}
                      ]
                    },
                    {
                      "type": "weekend",
                      "days": [6, 7],
                      "timeRanges": [
                        {"start": "10:00", "end": "16:00"}
                      ]
                    },
                    {
                      "type": "holiday",
                      "dates": ["2025-01-01", "2025-10-01"],
                      "timeRanges": [
                        {"start": "09:00", "end": "17:00"}
                      ]
                    }
                  ]
                }
                """;

        // 工作日测试
        LocalDateTime weekday = LocalDateTime.of(2025, 11, 17, 10, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, weekday), "工作日应该允许");

        // 周末测试
        LocalDateTime weekend = LocalDateTime.of(2025, 11, 22, 12, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, weekend), "周末应该允许");

        // 节假日测试
        LocalDateTime holiday = LocalDateTime.of(2025, 1, 1, 10, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, holiday), "节假日应该允许");
    }

    /**
     * 测试跨天时间段在实际业务中的使用
     */
    @Test
    @DisplayName("测试跨天时间段在实际业务中的使用")
    void testCrossDayTimeSlotInBusiness() {
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

        // 测试当天晚上
        LocalDateTime monday11pm = LocalDateTime.of(2025, 11, 17, 23, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, monday11pm), "周一晚上23:00应该允许");

        // 测试次日早上
        LocalDateTime tuesday5am = LocalDateTime.of(2025, 11, 18, 5, 0);
        assertTrue(TimeSlotValidator.validate(timePeriodsJson, tuesday5am), "周二早上05:00应该允许");

        // 测试中午（不在跨天时间段）
        LocalDateTime monday12pm = LocalDateTime.of(2025, 11, 17, 12, 0);
        assertFalse(TimeSlotValidator.validate(timePeriodsJson, monday12pm), "周一中午12:00不应该允许");
    }

    /**
     * 测试性能 - 多次验证
     */
    @Test
    @DisplayName("测试性能 - 多次验证")
    void testPerformance() {
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

        LocalDateTime testTime = LocalDateTime.of(2025, 11, 17, 10, 0);

        // 执行1000次验证，测试性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            TimeSlotValidator.validate(timePeriodsJson, testTime);
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能：1000次验证应该在1秒内完成
        assertTrue(duration < 1000, "1000次验证应该在1秒内完成，实际耗时: " + duration + "ms");
    }
}

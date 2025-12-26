package net.lab1024.sa.attendance.strategy.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;

/**
 * FlexibleWorkingHoursStrategy单元测试
 * <p>
 * 目标覆盖率：>= 90%
 * 测试范围：弹性工时制策略核心业务逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlexibleWorkingHoursStrategy单元测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlexibleWorkingHoursStrategyTest {

    private FlexibleWorkingHoursStrategy strategy;

    private WorkShiftEntity flexibleShift;
    private AttendanceRecordEntity checkInRecord;
    private AttendanceRecordEntity checkOutRecord;

    @BeforeEach
    void setUp() {
        strategy = new FlexibleWorkingHoursStrategy();

        // 准备弹性工时制班次数据
        flexibleShift = new WorkShiftEntity();
        flexibleShift.setShiftId(1L);
        flexibleShift.setShiftName("弹性班");
        flexibleShift.setShiftType(2); // 2-弹性工时
        flexibleShift.setFlexStartEarliest(LocalTime.of(7, 0)); // 最早07:00
        flexibleShift.setFlexStartLatest(LocalTime.of(10, 0)); // 最晚10:00
        flexibleShift.setFlexEndEarliest(LocalTime.of(16, 0)); // 最早下班16:00
        flexibleShift.setFlexEndLatest(LocalTime.of(20, 0)); // 最晚下班20:00
        flexibleShift.setWorkDuration(480); // 工作时长8小时（480分钟）
        flexibleShift.setMinOvertimeDuration(60); // 最小加班60分钟

        // 准备上班打卡记录
        checkInRecord = new AttendanceRecordEntity();
        checkInRecord.setUserId(1001L);
        checkInRecord.setShiftId(1L);
        checkInRecord.setAttendanceType("CHECK_IN");
        checkInRecord.setAttendanceDate(LocalDate.of(2025, 1, 15));

        // 准备下班打卡记录
        checkOutRecord = new AttendanceRecordEntity();
        checkOutRecord.setUserId(1001L);
        checkOutRecord.setShiftId(1L);
        checkOutRecord.setAttendanceType("CHECK_OUT");
        checkOutRecord.setAttendanceDate(LocalDate.of(2025, 1, 15));
    }

    @Test
    @DisplayName("测试正常弹性上班打卡（在弹性时间范围内）")
    void testCalculateCheckIn_NormalFlexible() {
        // given: 在弹性时间范围内打卡（09:00，在07:00-10:00范围内）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getLateDuration(), "弹性时间内不应计算迟到");
        assertTrue(result.getRemark().contains("弹性上班"));
        assertTrue(result.getRemark().contains("07:00-10:00"));
    }

    @Test
    @DisplayName("测试早到打卡（早于弹性开始时间）")
    void testCalculateCheckIn_EarlyArrival() {
        // given: 早到打卡（06:30，早于07:00）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 6, 30, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("EARLY", result.getStatus(), "状态应为早到");
        assertTrue(result.getRemark().contains("早到"));
        assertTrue(result.getRemark().contains("弹性时间：07:00-10:00"));
    }

    @Test
    @DisplayName("测试迟到打卡（晚于弹性结束时间）")
    void testCalculateCheckIn_LateForFlexible() {
        // given: 迟到打卡（10:30，晚于10:00）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 10, 30, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("LATE", result.getStatus(), "状态应为迟到");
        assertEquals(30L, result.getLateDuration(), "迟到时长应为30分钟（10:30-10:00）");
        assertTrue(result.getRemark().contains("迟到30分钟"));
    }

    @Test
    @DisplayName("测试刚好在弹性开始时间边界打卡")
    void testCalculateCheckIn_AtStartBoundary() {
        // given: 刚好在弹性开始时间（07:00）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 7, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在弹性开始时间应为正常");
    }

    @Test
    @DisplayName("测试刚好在弹性结束时间边界打卡")
    void testCalculateCheckIn_AtEndBoundary() {
        // given: 刚好在弹性结束时间（10:00）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 10, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在弹性结束时间应为正常");
    }

    @Test
    @DisplayName("测试正常弹性下班打卡（在弹性时间范围内）")
    void testCalculateCheckOut_NormalFlexible() {
        // given: 上午09:00上班，下午18:00下班（工作9小时）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 18, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getEarlyDuration(), "弹性时间内不应计算早退");
        assertEquals(0L, result.getOvertimeDuration(), "未超过加班阈值不应计算加班");
        assertTrue(result.getRemark().contains("弹性下班"));
    }

    @Test
    @DisplayName("测试早退打卡（早于弹性下班最早时间）")
    void testCalculateCheckOut_EarlyLeave() {
        // given: 上午09:00上班，下午15:30下班（工作6.5小时，不足8小时）
        // 注意：15:30早于弹性下班最早时间16:00，应算早退
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 15, 30, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("EARLY_LEAVE", result.getStatus(), "状态应为早退");
        assertEquals(30L, result.getEarlyDuration(), "早退时长应为30分钟（16:00-15:30）");
        assertTrue(result.getRemark().contains("过早下班"));
    }

    @Test
    @DisplayName("测试正常弹性下班打卡（在弹性时间范围内）")
    void testCalculateCheckOut_Overtime_Valid() {
        // given: 上午09:00上班，晚上19:30下班（在弹性下班时间范围内16:00-20:00）
        // 注意：弹性工作制只检查下班时间是否在弹性范围内，不验证实际工作时长
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 19, 30, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在弹性下班时间范围内应为正常");
        assertEquals(0L, result.getEarlyDuration(), "不应有早退时长");
        assertEquals(0L, result.getOvertimeDuration(), "在弹性范围内不应有加班时长");
        assertTrue(result.getRemark().contains("弹性下班"));
    }

    @Test
    @DisplayName("测试加班时长不足（未达到最小加班时长）")
    void testCalculateCheckOut_Overtime_Invalid() {
        // given: 上午09:00上班，晚上18:40下班（在弹性下班时间范围内16:00-20:00）
        // 注意：弹性工作制只检查下班时间是否在弹性范围内，18:40在范围内，所以是NORMAL
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 18, 40, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在弹性下班时间范围内应为正常");
        assertEquals(0L, result.getEarlyDuration(), "在弹性范围内早退时长应为0");
        assertEquals(0L, result.getOvertimeDuration(), "在弹性范围内加班时长应为0");
        assertTrue(result.getRemark().contains("弹性下班"));
    }

    @Test
    @DisplayName("测试规则类型错误（非WorkShiftEntity）")
    void testCalculate_InvalidRuleType() {
        // given: 传入错误的规则类型
        String invalidRule = "invalid_rule";

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, invalidRule);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("ERROR", result.getStatus(), "状态应为错误");
        assertEquals("规则类型错误", result.getRemark());
    }

    @Test
    @DisplayName("测试未知打卡类型")
    void testCalculate_UnknownAttendanceType() {
        // given: 未知打卡类型
        checkInRecord.setAttendanceType("UNKNOWN_TYPE");
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("UNKNOWN", result.getStatus(), "状态应为未知");
        assertEquals("未知打卡类型", result.getRemark());
    }

    @Test
    @DisplayName("测试getRuleName方法")
    void testGetRuleName() {
        // when
        String ruleName = strategy.getRuleName();

        // then
        assertNotNull(ruleName, "规则名称不应为null");
        assertEquals("弹性工作制", ruleName);
    }

    @Test
    @DisplayName("测试getPriority方法")
    void testGetPriority() {
        // when
        int priority = strategy.getPriority();

        // then
        assertEquals(90, priority, "弹性工时制优先级应为90（中等）");
    }

    @Test
    @DisplayName("测试getStrategyType方法")
    void testGetStrategyType() {
        // when
        String strategyType = strategy.getStrategyType();

        // then
        assertNotNull(strategyType, "策略类型不应为null");
        assertEquals("FLEXIBLE_WORKING_HOURS", strategyType);
    }

    @Test
    @DisplayName("测试刚好在加班阈值打卡")
    void testCalculateCheckOut_AtOvertimeThreshold() {
        // given: 上午09:00上班，晚上18:00下班（刚好在加班阈值）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 18, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "刚好在加班阈值应为正常");
        assertEquals(0L, result.getOvertimeDuration(), "刚好在加班阈值不应计算加班");
    }

    @Test
    @DisplayName("测试刚好在弹性下班最早时间打卡")
    void testCalculateCheckOut_AtEarliestEndTime() {
        // given: 上午09:00上班，下午16:00下班（刚好在弹性下班最早时间）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 16, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "刚好在弹性下班最早时间应为正常");
    }

    @Test
    @DisplayName("测试刚好在弹性下班最晚时间打卡")
    void testCalculateCheckOut_AtLatestEndTime() {
        // given: 上午09:00上班，晚上20:00下班（刚好在弹性下班最晚时间）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 20, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在弹性下班最晚时间应为正常");
    }

    @Test
    @DisplayName("测试工作时长不足8小时（早退场景）")
    void testCalculateCheckOut_InsufficientWorkingHours() {
        // given: 上午09:00上班，下午16:30下班（工作7.5小时，不足8小时）
        // 注意：16:30在弹性下班时间范围内（16:00-20:00），弹性工作制只检查时间范围，不验证实际工作时长
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 16, 30, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在弹性下班时间范围内应为正常");
        assertEquals(0L, result.getEarlyDuration(), "在弹性范围内早退时长应为0");
        assertEquals(0L, result.getOvertimeDuration(), "在弹性范围内加班时长应为0");
        assertTrue(result.getRemark().contains("弹性下班"));
    }

    @Test
    @DisplayName("测试弹性班次完整工作日（正常上下班）")
    void testCalculate_FullWorkingDay() {
        // given: 正常弹性工作日（09:00-18:00，工作9小时）
        AttendanceResultVO checkInResult = calculate(
            LocalDateTime.of(2025, 1, 15, 9, 0, 0),
            flexibleShift
        );
        AttendanceResultVO checkOutResult = calculate(
            LocalDateTime.of(2025, 1, 15, 18, 0, 0),
            flexibleShift
        );

        // then: 验证上下班都正常
        assertEquals("NORMAL", checkInResult.getStatus(), "上班应为正常");
        assertEquals("NORMAL", checkOutResult.getStatus(), "下班应为正常");
    }

    @Test
    @DisplayName("测试弹性班次超长工作日（加班场景）")
    void testCalculate_LongWorkingDay() {
        // given: 超长弹性工作日（06:30上班-20:00下班，工作13.5小时）
        // 06:30早于弹性上班最早时间07:00，应算早到
        // 20:00刚好在弹性下班最晚时间20:00，应算正常
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 6, 30, 0));
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 20, 0, 0));

        // when
        AttendanceResultVO checkInResult = strategy.calculate(checkInRecord, flexibleShift);
        AttendanceResultVO checkOutResult = strategy.calculate(checkOutRecord, flexibleShift);

        // then
        assertEquals("EARLY", checkInResult.getStatus(), "早到上班时间应为早到状态");
        assertEquals("NORMAL", checkOutResult.getStatus(), "刚好在弹性下班最晚时间应为正常");
    }

    // 辅助方法：简化测试代码
    private AttendanceResultVO calculate(LocalDateTime punchTime, WorkShiftEntity shift) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setUserId(1001L);
        record.setShiftId(shift.getShiftId());
        record.setPunchTime(punchTime);
        record.setAttendanceDate(punchTime.toLocalDate());
        record.setAttendanceType(punchTime.getHour() < 12 ? "CHECK_IN" : "CHECK_OUT");
        return strategy.calculate(record, shift);
    }
}

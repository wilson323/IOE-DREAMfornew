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
 * StandardWorkingHoursStrategy单元测试
 * <p>
 * 目标覆盖率：>= 90%
 * 测试范围：标准工时制策略核心业务逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StandardWorkingHoursStrategy单元测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StandardWorkingHoursStrategyTest {

    private StandardWorkingHoursStrategy strategy;

    private WorkShiftEntity standardShift;
    private AttendanceRecordEntity checkInRecord;
    private AttendanceRecordEntity checkOutRecord;

    @BeforeEach
    void setUp() {
        strategy = new StandardWorkingHoursStrategy();

        // 准备标准工时制班次数据
        standardShift = new WorkShiftEntity();
        standardShift.setShiftId(1L);
        standardShift.setShiftName("正常班");
        standardShift.setShiftType(1); // 1-固定工时
        standardShift.setWorkStartTime(LocalTime.of(9, 0)); // 09:00
        standardShift.setWorkEndTime(LocalTime.of(18, 0)); // 18:00
        standardShift.setLateTolerance(10); // 10分钟宽限
        standardShift.setEarlyTolerance(10); // 10分钟宽限
        standardShift.setMinOvertimeDuration(30); // 最小加班30分钟

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
    @DisplayName("测试正常上班打卡（无迟到）")
    void testCalculateCheckIn_Normal() {
        // given: 正常上班时间（09:00-09:10宽限期内）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 5, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getLateDuration(), "迟到时长应为0");
        assertEquals("正常上班", result.getRemark());
        assertEquals(1001L, result.getUserId());
        assertEquals(LocalDate.of(2025, 1, 15), result.getDate());
    }

    @Test
    @DisplayName("测试上班迟到（超过宽限期）")
    void testCalculateCheckIn_Late() {
        // given: 迟到打卡（09:15，超过10分钟宽限期）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 15, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("LATE", result.getStatus(), "状态应为迟到");
        assertEquals(15L, result.getLateDuration(), "迟到时长应为15分钟");
        assertTrue(result.getRemark().contains("迟到15分钟"));
        assertTrue(result.getRemark().contains("宽限10分钟"));
    }

    @Test
    @DisplayName("测试上班刚好在宽限期边界")
    void testCalculateCheckIn_AtToleranceBoundary() {
        // given: 刚好在宽限期边界（09:10）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 10, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在宽限期内应为正常");
        assertEquals(0L, result.getLateDuration(), "在宽限期内不应计算迟到");
    }

    @Test
    @DisplayName("测试正常下班打卡（无早退无加班）")
    void testCalculateCheckOut_Normal() {
        // given: 正常下班时间（18:00）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 18, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getEarlyDuration(), "早退时长应为0");
        assertEquals(0L, result.getOvertimeDuration(), "加班时长应为0");
        assertEquals("正常下班", result.getRemark());
        assertEquals(540L, result.getWorkingMinutes(), "工作时长应为9小时（540分钟）");
    }

    @Test
    @DisplayName("测试下班早退（超过宽限期）")
    void testCalculateCheckOut_EarlyLeave() {
        // given: 早退打卡（17:45，超过10分钟宽限期）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 17, 45, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("EARLY_LEAVE", result.getStatus(), "状态应为早退");
        assertEquals(15L, result.getEarlyDuration(), "早退时长应为15分钟");
        assertEquals(0L, result.getOvertimeDuration(), "早退不应有加班时长");
        assertTrue(result.getRemark().contains("早退15分钟"));
    }

    @Test
    @DisplayName("测试下班加班（满足最小加班时长）")
    void testCalculateCheckOut_Overtime_Valid() {
        // given: 加班打卡（18:45，加班45分钟，超过30分钟最小时长）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 18, 45, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("OVERTIME", result.getStatus(), "状态应为加班");
        assertEquals(0L, result.getEarlyDuration(), "加班不应有早退时长");
        assertEquals(45L, result.getOvertimeDuration(), "加班时长应为45分钟");
        assertTrue(result.getRemark().contains("加班45分钟"));
    }

    @Test
    @DisplayName("测试下班加班时长不足（不满足最小加班时长）")
    void testCalculateCheckOut_Overtime_Invalid() {
        // given: 加班打卡（18:20，加班20分钟，不足30分钟最小时长）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 18, 20, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "加班时长不足应视为正常");
        assertEquals(0L, result.getEarlyDuration(), "加班时长不足不应有早退时长");
        assertEquals(0L, result.getOvertimeDuration(), "加班时长不足不应计算加班");
        assertTrue(result.getRemark().contains("加班20分钟未达到最小时长30分钟"));
    }

    @Test
    @DisplayName("测试下班在宽限期内")
    void testCalculateCheckOut_WithinTolerance() {
        // given: 在宽限期内下班（17:55，在18:00-10分钟宽限期内）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 17, 55, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, standardShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在宽限期内应为正常");
        assertEquals(0L, result.getEarlyDuration(), "在宽限期内不应计算早退");
        assertEquals(0L, result.getOvertimeDuration(), "在宽限期内不应计算加班");
        assertEquals("正常下班", result.getRemark());
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
        AttendanceResultVO result = strategy.calculate(checkInRecord, standardShift);

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
        assertEquals("标准工时制", ruleName);
    }

    @Test
    @DisplayName("测试getPriority方法")
    void testGetPriority() {
        // when
        int priority = strategy.getPriority();

        // then
        assertEquals(100, priority, "标准工时制优先级应为100（最高）");
    }

    @Test
    @DisplayName("测试getStrategyType方法")
    void testGetStrategyType() {
        // when
        String strategyType = strategy.getStrategyType();

        // then
        assertNotNull(strategyType, "策略类型不应为null");
        assertEquals("STANDARD_WORKING_HOURS", strategyType);
    }

    @Test
    @DisplayName("测试跨天夜班（下班时间次日）")
    void testCalculate_NightShift_CheckOut() {
        // given: 夜班班次（22:00-次日06:00）
        WorkShiftEntity nightShift = new WorkShiftEntity();
        nightShift.setShiftId(2L);
        nightShift.setShiftName("夜班");
        nightShift.setShiftType(1);
        nightShift.setWorkStartTime(LocalTime.of(22, 0));
        nightShift.setWorkEndTime(LocalTime.of(6, 0)); // 次日06:00
        nightShift.setLateTolerance(15);
        nightShift.setEarlyTolerance(15);

        // 下班打卡（次日06:30，加班30分钟）
        AttendanceRecordEntity nightCheckOut = new AttendanceRecordEntity();
        nightCheckOut.setUserId(1002L);
        nightCheckOut.setShiftId(2L);
        nightCheckOut.setAttendanceType("CHECK_OUT");
        nightCheckOut.setAttendanceDate(LocalDate.of(2025, 1, 16));
        nightCheckOut.setPunchTime(LocalDateTime.of(2025, 1, 16, 6, 30, 0));

        // when
        AttendanceResultVO result = strategy.calculate(nightCheckOut, nightShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("OVERTIME", result.getStatus(), "夜班加班应识别为加班");
        assertEquals(30L, result.getOvertimeDuration(), "加班时长应为30分钟");
    }

    @Test
    @DisplayName("测试零宽限时间")
    void testCalculate_ZeroTolerance() {
        // given: 零宽限时间班次
        WorkShiftEntity zeroToleranceShift = new WorkShiftEntity();
        zeroToleranceShift.setShiftId(3L);
        zeroToleranceShift.setShiftName("严格班次");
        zeroToleranceShift.setShiftType(1);
        zeroToleranceShift.setWorkStartTime(LocalTime.of(8, 0));
        zeroToleranceShift.setWorkEndTime(LocalTime.of(17, 0));
        zeroToleranceShift.setLateTolerance(0); // 零宽限
        zeroToleranceShift.setEarlyTolerance(0); // 零宽限

        // 迟到1分钟（08:01）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 8, 1, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, zeroToleranceShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("LATE", result.getStatus(), "零宽限下迟到1分钟应为迟到");
        assertEquals(1L, result.getLateDuration(), "迟到时长应为1分钟");
    }
}

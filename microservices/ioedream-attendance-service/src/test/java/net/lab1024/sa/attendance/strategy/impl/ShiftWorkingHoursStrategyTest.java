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
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.entity.WorkShiftEntity;

/**
 * ShiftWorkingHoursStrategy单元测试
 * <p>
 * 目标覆盖率：>= 90%
 * 测试范围：轮班制策略核心业务逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShiftWorkingHoursStrategy单元测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShiftWorkingHoursStrategyTest {

    private ShiftWorkingHoursStrategy strategy;

    private WorkShiftEntity morningShift;
    private WorkShiftEntity afternoonShift;
    private WorkShiftEntity nightShift;
    private AttendanceRecordEntity checkInRecord;
    private AttendanceRecordEntity checkOutRecord;

    @BeforeEach
    void setUp() {
        strategy = new ShiftWorkingHoursStrategy();

        // 准备早班数据（06:00-14:00）
        morningShift = new WorkShiftEntity();
        morningShift.setShiftId(1L);
        morningShift.setShiftName("早班");
        morningShift.setShiftType(3); // 3-轮班制
        morningShift.setWorkStartTime(LocalTime.of(6, 0));
        morningShift.setWorkEndTime(LocalTime.of(14, 0));
        morningShift.setLateTolerance(15);
        morningShift.setEarlyTolerance(15);
        morningShift.setMinOvertimeDuration(60);

        // 准备中班数据（14:00-22:00）
        afternoonShift = new WorkShiftEntity();
        afternoonShift.setShiftId(2L);
        afternoonShift.setShiftName("中班");
        afternoonShift.setShiftType(3);
        afternoonShift.setWorkStartTime(LocalTime.of(14, 0));
        afternoonShift.setWorkEndTime(LocalTime.of(22, 0));
        afternoonShift.setLateTolerance(15);
        afternoonShift.setEarlyTolerance(15);
        afternoonShift.setMinOvertimeDuration(60);

        // 准备晚班数据（22:00-次日06:00）
        nightShift = new WorkShiftEntity();
        nightShift.setShiftId(3L);
        nightShift.setShiftName("晚班");
        nightShift.setShiftType(3);
        nightShift.setWorkStartTime(LocalTime.of(22, 0));
        nightShift.setWorkEndTime(LocalTime.of(6, 0));
        nightShift.setLateTolerance(15);
        nightShift.setEarlyTolerance(15);
        nightShift.setMinOvertimeDuration(60);

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
    @DisplayName("测试早班正常上班打卡")
    void testCalculate_MorningShift_NormalCheckIn() {
        // given: 早班正常上班（06:00-06:15宽限期内）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 6, 10, 0));
        checkInRecord.setShiftId(1L);

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, morningShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getLateDuration(), "迟到时长应为0");
        assertTrue(result.getRemark().contains("[早班]"), "备注应包含班次名称");
        assertTrue(result.getRemark().contains("正常上班"), "备注应包含正常上班");
    }

    @Test
    @DisplayName("测试早班上班迟到")
    void testCalculate_MorningShift_LateCheckIn() {
        // given: 早班迟到打卡（06:25，超过15分钟宽限期）
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 6, 25, 0));
        checkInRecord.setShiftId(1L);

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, morningShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("LATE", result.getStatus(), "状态应为迟到");
        assertEquals(25L, result.getLateDuration(), "迟到时长应为25分钟");
        assertTrue(result.getRemark().contains("[早班]"), "备注应包含班次名称");
        assertTrue(result.getRemark().contains("迟到25分钟"));
    }

    @Test
    @DisplayName("测试中班正常下班打卡")
    void testCalculate_AfternoonShift_NormalCheckOut() {
        // given: 中班正常下班（22:00）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 22, 0, 0));
        checkOutRecord.setShiftId(2L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, afternoonShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getEarlyDuration(), "早退时长应为0");
        assertEquals(0L, result.getOvertimeDuration(), "加班时长应为0");
        assertTrue(result.getRemark().contains("[中班]"));
        assertTrue(result.getRemark().contains("正常下班"));
    }

    @Test
    @DisplayName("测试中班下班早退")
    void testCalculate_AfternoonShift_EarlyCheckOut() {
        // given: 中班早退（21:30，超过15分钟宽限期）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 21, 30, 0));
        checkOutRecord.setShiftId(2L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, afternoonShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("EARLY_LEAVE", result.getStatus(), "状态应为早退");
        assertEquals(30L, result.getEarlyDuration(), "早退时长应为30分钟");
        assertEquals(0L, result.getOvertimeDuration(), "早退不应有加班时长");
        assertTrue(result.getRemark().contains("[中班]"));
        assertTrue(result.getRemark().contains("早退30分钟"));
    }

    @Test
    @DisplayName("测试晚班跨天正常下班打卡")
    void testCalculate_NightShift_NormalCheckOut() {
        // given: 晚班下班（次日06:00）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 16, 6, 0, 0));
        checkOutRecord.setAttendanceDate(LocalDate.of(2025, 1, 16));
        checkOutRecord.setShiftId(3L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, nightShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "状态应为正常");
        assertEquals(0L, result.getEarlyDuration(), "早退时长应为0");
        assertEquals(0L, result.getOvertimeDuration(), "加班时长应为0");
        assertTrue(result.getRemark().contains("[晚班]"));
    }

    @Test
    @DisplayName("测试晚班跨天加班（满足最小加班时长）")
    void testCalculate_NightShift_Overtime_Valid() {
        // given: 晚班加班（次日07:30，加班90分钟，超过60分钟最小时长）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 16, 7, 30, 0));
        checkOutRecord.setAttendanceDate(LocalDate.of(2025, 1, 16));
        checkOutRecord.setShiftId(3L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, nightShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("OVERTIME", result.getStatus(), "状态应为加班");
        assertEquals(0L, result.getEarlyDuration(), "加班不应有早退时长");
        assertEquals(90L, result.getOvertimeDuration(), "加班时长应为90分钟");
        assertTrue(result.getRemark().contains("[晚班]"));
        assertTrue(result.getRemark().contains("加班90分钟"));
    }

    @Test
    @DisplayName("测试晚班跨天加班时长不足")
    void testCalculate_NightShift_Overtime_Invalid() {
        // given: 晚班加班时长不足（次日06:45，加班45分钟，不足60分钟最小时长）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 16, 6, 45, 0));
        checkOutRecord.setAttendanceDate(LocalDate.of(2025, 1, 16));
        checkOutRecord.setShiftId(3L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, nightShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "加班时长不足应视为正常");
        assertEquals(0L, result.getEarlyDuration(), "加班时长不足不应有早退时长");
        assertEquals(0L, result.getOvertimeDuration(), "加班时长不足不应计算加班");
        assertTrue(result.getRemark().contains("加班45分钟未达到最小时长60分钟"));
    }

    @Test
    @DisplayName("测试班次类型不是轮班制（警告但不影响计算）")
    void testCalculate_Warning_NonShiftType() {
        // given: 班次类型不是轮班制（shiftType=1固定工时）
        WorkShiftEntity fixedShift = new WorkShiftEntity();
        fixedShift.setShiftId(4L);
        fixedShift.setShiftName("正常班");
        fixedShift.setShiftType(1); // 不是轮班制
        fixedShift.setWorkStartTime(LocalTime.of(9, 0));
        fixedShift.setWorkEndTime(LocalTime.of(18, 0));
        fixedShift.setLateTolerance(10);
        fixedShift.setEarlyTolerance(10);

        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 5, 0));
        checkInRecord.setShiftId(4L);

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, fixedShift);

        // then: 策略应正常执行（警告不影响计算）
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "应正常计算");
        assertTrue(result.getRemark().contains("[正常班]"), "备注应包含班次名称");
    }

    @Test
    @DisplayName("测试规则类型错误")
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
        checkInRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 6, 0, 0));

        // when
        AttendanceResultVO result = strategy.calculate(checkInRecord, morningShift);

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
        assertEquals("轮班制", ruleName);
    }

    @Test
    @DisplayName("测试getPriority方法")
    void testGetPriority() {
        // when
        int priority = strategy.getPriority();

        // then
        assertEquals(80, priority, "轮班制优先级应为80（较低）");
    }

    @Test
    @DisplayName("测试getStrategyType方法")
    void testGetStrategyType() {
        // when
        String strategyType = strategy.getStrategyType();

        // then
        assertNotNull(strategyType, "策略类型不应为null");
        assertEquals("SHIFT_WORKING_HOURS", strategyType);
    }

    @Test
    @DisplayName("测试工作时长计算（早班8小时）")
    void testCalculate_WorkingMinutes_MorningShift() {
        // given: 早班正常下班
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 14, 0, 0));
        checkOutRecord.setShiftId(1L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, morningShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals(480L, result.getWorkingMinutes(), "早班工作时长应为8小时（480分钟）");
    }

    @Test
    @DisplayName("测试工作时长计算（中班8小时）")
    void testCalculate_WorkingMinutes_AfternoonShift() {
        // given: 中班正常下班
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 22, 0, 0));
        checkOutRecord.setShiftId(2L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, afternoonShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals(480L, result.getWorkingMinutes(), "中班工作时长应为8小时（480分钟）");
    }

    @Test
    @DisplayName("测试工作时长计算（晚班跨天8小时）")
    void testCalculate_WorkingMinutes_NightShift() {
        // given: 晚班正常下班（次日06:00）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 16, 6, 0, 0));
        checkOutRecord.setAttendanceDate(LocalDate.of(2025, 1, 16));
        checkOutRecord.setShiftId(3L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, nightShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals(480L, result.getWorkingMinutes(), "晚班工作时长应为8小时（480分钟）");
    }

    @Test
    @DisplayName("测试中班加班（满足最小时长）")
    void testCalculate_AfternoonShift_Overtime_Valid() {
        // given: 中班加班（23:30，加班90分钟，超过60分钟最小时长）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 23, 30, 0));
        checkOutRecord.setShiftId(2L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, afternoonShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("OVERTIME", result.getStatus(), "状态应为加班");
        assertEquals(90L, result.getOvertimeDuration(), "加班时长应为90分钟");
        assertTrue(result.getRemark().contains("[中班]"));
        assertTrue(result.getRemark().contains("加班90分钟"));
    }

    @Test
    @DisplayName("测试早班在宽限期内下班")
    void testCalculate_MorningShift_WithinTolerance() {
        // given: 早班在宽限期内下班（13:50，在14:00-10分钟宽限期内）
        checkOutRecord.setPunchTime(LocalDateTime.of(2025, 1, 15, 13, 50, 0));
        checkOutRecord.setShiftId(1L);

        // when
        AttendanceResultVO result = strategy.calculate(checkOutRecord, morningShift);

        // then
        assertNotNull(result, "结果不应为null");
        assertEquals("NORMAL", result.getStatus(), "在宽限期内应为正常");
        assertEquals(0L, result.getEarlyDuration(), "在宽限期内不应计算早退");
        assertEquals(0L, result.getOvertimeDuration(), "在宽限期内不应计算加班");
        assertTrue(result.getRemark().contains("[早班]"));
        assertTrue(result.getRemark().contains("正常下班"));
    }
}

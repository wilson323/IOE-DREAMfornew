package net.lab1024.sa.attendance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 跨天班次工具类单元测试
 * <p>
 * 测试跨天班次的日期计算、打卡匹配、时长统计等核心功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@DisplayName("跨天班次工具类测试")
class CrossDayShiftUtilTest {

    /**
     * 测试计算考勤日期 - START_DATE规则（夜班推荐）
     */
    @Test
    @DisplayName("START_DATE规则 - 夜班下班打卡归属到班次开始日期")
    void testCalculateAttendanceDate_StartDateRule() {
        // 场景：夜班 22:00-06:00，员工在1月2日06:05下班打卡
        LocalDateTime punchTime = LocalDateTime.of(2025, 1, 2, 6, 5);
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        String crossDayRule = CrossDayShiftUtil.CROSS_DAY_RULE_START_DATE;
        String attendanceType = "CHECK_OUT";

        LocalDate result = CrossDayShiftUtil.calculateAttendanceDate(
                punchTime, workStartTime, workEndTime, crossDayRule, attendanceType);

        // 期望：06:05在缓冲期内，归属到班次开始日期（1月1日）
        assertEquals(LocalDate.of(2025, 1, 1), result,
                "夜班下班打卡在缓冲期内，应归属到班次开始日期");
    }

    /**
     * 测试计算考勤日期 - START_DATE规则（上班打卡）
     */
    @Test
    @DisplayName("START_DATE规则 - 夜班上班打卡归属到班次开始日期")
    void testCalculateAttendanceDate_StartDateRule_CheckIn() {
        // 场景：夜班 22:00-06:00，员工在1月1日21:55上班打卡
        LocalDateTime punchTime = LocalDateTime.of(2025, 1, 1, 21, 55);
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        String crossDayRule = CrossDayShiftUtil.CROSS_DAY_RULE_START_DATE;
        String attendanceType = "CHECK_IN";

        LocalDate result = CrossDayShiftUtil.calculateAttendanceDate(
                punchTime, workStartTime, workEndTime, crossDayRule, attendanceType);

        // 期望：归属到1月1日
        assertEquals(LocalDate.of(2025, 1, 1), result,
                "夜班上班打卡应归属到班次开始日期");
    }

    /**
     * 测试计算考勤日期 - END_DATE规则
     */
    @Test
    @DisplayName("END_DATE规则 - 夜班打卡归属到班次结束日期")
    void testCalculateAttendanceDate_EndDateRule() {
        // 场景：夜班 22:00-06:00，使用END_DATE规则
        LocalDateTime punchTime = LocalDateTime.of(2025, 1, 1, 21, 55);
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        String crossDayRule = CrossDayShiftUtil.CROSS_DAY_RULE_END_DATE;
        String attendanceType = "CHECK_IN";

        LocalDate result = CrossDayShiftUtil.calculateAttendanceDate(
                punchTime, workStartTime, workEndTime, crossDayRule, attendanceType);

        // 期望：21:55在上班时间之前，归属到当天（1月1日）
        assertEquals(LocalDate.of(2025, 1, 1), result,
                "END_DATE规则下，上班时间之前的打卡应归属到当天");
    }

    /**
     * 测试计算考勤日期 - SPLIT规则
     */
    @Test
    @DisplayName("SPLIT规则 - 上班打卡归属开始日期，下班打卡归属结束日期")
    void testCalculateAttendanceDate_SplitRule() {
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        String crossDayRule = CrossDayShiftUtil.CROSS_DAY_RULE_SPLIT;

        // 测试上班打卡（1月1日21:55）
        LocalDate checkInResult = CrossDayShiftUtil.calculateAttendanceDate(
                LocalDateTime.of(2025, 1, 1, 21, 55),
                workStartTime, workEndTime, crossDayRule, "CHECK_IN");
        assertEquals(LocalDate.of(2025, 1, 1), checkInResult,
                "SPLIT规则下，上班打卡应归属到开始日期");

        // 测试下班打卡（1月2日06:05）
        LocalDate checkOutResult = CrossDayShiftUtil.calculateAttendanceDate(
                LocalDateTime.of(2025, 1, 2, 6, 5),
                workStartTime, workEndTime, crossDayRule, "CHECK_OUT");
        assertEquals(LocalDate.of(2025, 1, 2), checkOutResult,
                "SPLIT规则下，下班打卡应归属到结束日期");
    }

    /**
     * 测试非跨天班次 - 正常班次
     */
    @Test
    @DisplayName("非跨天班次 - 打卡日期保持不变")
    void testCalculateAttendanceDate_NonCrossDay() {
        // 场景：正常班次 09:00-18:00
        LocalDateTime punchTime = LocalDateTime.of(2025, 1, 1, 9, 5);
        LocalTime workStartTime = LocalTime.of(9, 0);
        LocalTime workEndTime = LocalTime.of(18, 0);
        String crossDayRule = CrossDayShiftUtil.CROSS_DAY_RULE_START_DATE;
        String attendanceType = "CHECK_IN";

        LocalDate result = CrossDayShiftUtil.calculateAttendanceDate(
                punchTime, workStartTime, workEndTime, crossDayRule, attendanceType);

        // 期望：保持原日期（1月1日）
        assertEquals(LocalDate.of(2025, 1, 1), result,
                "非跨天班次，打卡日期应保持不变");
    }

    /**
     * 测试打卡时间有效性验证 - 跨天班次
     */
    @Test
    @DisplayName("打卡时间有效性 - 跨天班次时间范围验证")
    void testIsValidPunchTime_CrossDayShift() {
        // 场景：夜班 1月1日 22:00 - 1月2日 06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        LocalDate shiftDate = LocalDate.of(2025, 1, 1);

        // 有效打卡时间：1月1日22:00 - 1月2日06:00
        assertTrue(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 22, 0), workStartTime, workEndTime, shiftDate),
                "班次开始时间应有效");

        assertTrue(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 2, 6, 0), workStartTime, workEndTime, shiftDate),
                "班次结束时间应有效");

        assertTrue(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 23, 59), workStartTime, workEndTime, shiftDate),
                "班次中间时间应有效");

        // 无效打卡时间
        assertFalse(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 21, 59), workStartTime, workEndTime, shiftDate),
                "班次开始之前的时间应无效");

        assertFalse(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 2, 6, 1), workStartTime, workEndTime, shiftDate),
                "班次结束之后的时间应无效");
    }

    /**
     * 测试打卡时间有效性验证 - 非跨天班次
     */
    @Test
    @DisplayName("打卡时间有效性 - 非跨天班次时间范围验证")
    void testIsValidPunchTime_NonCrossDayShift() {
        // 场景：正常班次 09:00-18:00
        LocalTime workStartTime = LocalTime.of(9, 0);
        LocalTime workEndTime = LocalTime.of(18, 0);
        LocalDate shiftDate = LocalDate.of(2025, 1, 1);

        // 有效打卡时间
        assertTrue(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 9, 0), workStartTime, workEndTime, shiftDate),
                "上班时间应有效");

        assertTrue(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 18, 0), workStartTime, workEndTime, shiftDate),
                "下班时间应有效");

        // 无效打卡时间
        assertFalse(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 8, 59), workStartTime, workEndTime, shiftDate),
                "上班之前应无效");

        assertFalse(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 1, 18, 1), workStartTime, workEndTime, shiftDate),
                "下班之后应无效");

        assertFalse(CrossDayShiftUtil.isValidPunchTime(
                LocalDateTime.of(2025, 1, 2, 9, 0), workStartTime, workEndTime, shiftDate),
                "不同日期应无效");
    }

    /**
     * 测试跨天工作时长计算
     */
    @Test
    @DisplayName("工作时长计算 - 跨天班次时长计算")
    void testCalculateCrossDayWorkDuration() {
        // 场景：夜班 1月1日 22:00 - 1月2日 06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        LocalDate shiftDate = LocalDate.of(2025, 1, 1);

        // 实际打卡：21:55上班，06:10下班（8小时15分钟）
        LocalDateTime actualStartTime = LocalDateTime.of(2025, 1, 1, 21, 55);
        LocalDateTime actualEndTime = LocalDateTime.of(2025, 1, 2, 6, 10);

        int duration = CrossDayShiftUtil.calculateCrossDayWorkDuration(
                workStartTime, workEndTime, shiftDate, actualStartTime, actualEndTime);

        // 期望：8小时15分钟 = 495分钟
        // 但限制在班次时间范围内：22:00-06:00 = 8小时 = 480分钟
        assertEquals(480, duration,
                "跨天工作时长应限制在班次时间范围内");
    }

    /**
     * 测试跨天工作时长计算 - 提前下班
     */
    @Test
    @DisplayName("工作时长计算 - 跨天班次提前下班")
    void testCalculateCrossDayWorkDuration_EarlyLeave() {
        // 场景：夜班 1月1日 22:00 - 1月2日 06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        LocalDate shiftDate = LocalDate.of(2025, 1, 1);

        // 实际打卡：22:00上班，05:30下班（提前30分钟下班）
        LocalDateTime actualStartTime = LocalDateTime.of(2025, 1, 1, 22, 0);
        LocalDateTime actualEndTime = LocalDateTime.of(2025, 1, 2, 5, 30);

        int duration = CrossDayShiftUtil.calculateCrossDayWorkDuration(
                workStartTime, workEndTime, shiftDate, actualStartTime, actualEndTime);

        // 期望：7.5小时 = 450分钟
        assertEquals(450, duration,
                "提前下班的工作时长应正确计算");
    }

    /**
     * 测试跨天工作时长计算 - 迟到
     */
    @Test
    @DisplayName("工作时长计算 - 跨天班次迟到")
    void testCalculateCrossDayWorkDuration_LateArrival() {
        // 场景：夜班 1月1日 22:00 - 1月2日 06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        LocalDate shiftDate = LocalDate.of(2025, 1, 1);

        // 实际打卡：22:30上班（迟到30分钟），06:00下班
        LocalDateTime actualStartTime = LocalDateTime.of(2025, 1, 1, 22, 30);
        LocalDateTime actualEndTime = LocalDateTime.of(2025, 1, 2, 6, 0);

        int duration = CrossDayShiftUtil.calculateCrossDayWorkDuration(
                workStartTime, workEndTime, shiftDate, actualStartTime, actualEndTime);

        // 期望：7.5小时 = 450分钟
        assertEquals(450, duration,
                "迟到的工作时长应正确计算");
    }

    /**
     * 测试同一跨天班次判断 - 正常顺序
     */
    @Test
    @DisplayName("跨天班次匹配 - 正常打卡顺序判断")
    void testIsSameCrossDayShift_NormalOrder() {
        // 场景：夜班 22:00-06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);

        // 第一次打卡：21:55（上班，接近22:00）
        // 第二次打卡：06:05（下班，接近06:00）
        LocalDateTime firstPunch = LocalDateTime.of(2025, 1, 1, 21, 55);
        LocalDateTime secondPunch = LocalDateTime.of(2025, 1, 2, 6, 5);

        boolean isSameShift = CrossDayShiftUtil.isSameCrossDayShift(
                firstPunch, secondPunch, workStartTime, workEndTime);

        assertTrue(isSameShift, "正常顺序的打卡应属于同一跨天班次");
    }

    /**
     * 测试同一跨天班次判断 - 反向顺序
     */
    @Test
    @DisplayName("跨天班次匹配 - 反向打卡顺序判断")
    void testIsSameCrossDayShift_ReverseOrder() {
        // 场景：夜班 22:00-06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);

        // 第一次打卡：06:05（下班，接近06:00）
        // 第二次打卡：21:55（上班，接近22:00）
        LocalDateTime firstPunch = LocalDateTime.of(2025, 1, 2, 6, 5);
        LocalDateTime secondPunch = LocalDateTime.of(2025, 1, 1, 21, 55);

        boolean isSameShift = CrossDayShiftUtil.isSameCrossDayShift(
                firstPunch, secondPunch, workStartTime, workEndTime);

        assertTrue(isSameShift, "反向顺序的打卡应属于同一跨天班次");
    }

    /**
     * 测试同一跨天班次判断 - 不属于同一班次
     */
    @Test
    @DisplayName("跨天班次匹配 - 不同班次判断")
    void testIsSameCrossDayShift_DifferentShift() {
        // 场景：夜班 22:00-06:00
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);

        // 第一次打卡：22:00
        // 第二次打卡：22:30（同一个班次的第二次打卡，不应匹配）
        LocalDateTime firstPunch = LocalDateTime.of(2025, 1, 1, 22, 0);
        LocalDateTime secondPunch = LocalDateTime.of(2025, 1, 1, 22, 30);

        boolean isSameShift = CrossDayShiftUtil.isSameCrossDayShift(
                firstPunch, secondPunch, workStartTime, workEndTime);

        assertFalse(isSameShift, "相隔很近的打卡不应属于同一跨天班次");
    }

    /**
     * 测试非跨天班次判断
     */
    @Test
    @DisplayName("非跨天班次匹配 - 同一天打卡判断")
    void testIsSameCrossDayShift_NonCrossDay() {
        // 场景：正常班次 09:00-18:00
        LocalTime workStartTime = LocalTime.of(9, 0);
        LocalTime workEndTime = LocalTime.of(18, 0);

        // 第一次打卡：09:00
        // 第二次打卡：18:00
        LocalDateTime firstPunch = LocalDateTime.of(2025, 1, 1, 9, 0);
        LocalDateTime secondPunch = LocalDateTime.of(2025, 1, 1, 18, 0);

        boolean isSameShift = CrossDayShiftUtil.isSameCrossDayShift(
                firstPunch, secondPunch, workStartTime, workEndTime);

        assertTrue(isSameShift, "同一天的打卡应属于同一班次");
    }

    /**
     * 参数化测试 - 各种夜班场景
     */
    @ParameterizedTest
    @CsvSource({
        // 上班时间, 下班时间, 打卡日期时间, 期望考勤日期
        "22:00, 06:00, 2025-01-01T21:55, 2025-01-01", // 夜班上班打卡（提前）
        "22:00, 06:00, 2025-01-01T22:00, 2025-01-01", // 夜班上班打卡（准时）
        "22:00, 06:00, 2025-01-02T06:00, 2025-01-01", // 夜班下班打卡（准时）
        "22:00, 06:00, 2025-01-02T06:05, 2025-01-01", // 夜班下班打卡（延后）
        "20:00, 04:00, 2025-01-02T04:05, 2025-01-01", // 早期夜班下班打卡
        "23:00, 07:00, 2025-01-02T07:10, 2025-01-01"  // 晚期夜班下班打卡
    })
    @DisplayName("参数化测试 - 夜班各种打卡场景（START_DATE规则）")
    void testCalculateAttendanceDate_Parameterized(
            String startTimeStr, String endTimeStr, String punchTimeStr, String expectedDateStr) {

        LocalTime workStartTime = LocalTime.parse(startTimeStr);
        LocalTime workEndTime = LocalTime.parse(endTimeStr);
        LocalDateTime punchTime = LocalDateTime.parse(punchTimeStr);
        LocalDate expectedDate = LocalDate.parse(expectedDateStr);

        LocalDate result = CrossDayShiftUtil.calculateAttendanceDate(
                punchTime, workStartTime, workEndTime,
                CrossDayShiftUtil.CROSS_DAY_RULE_START_DATE, "CHECK_OUT");

        assertEquals(expectedDate, result,
                String.format("打卡时间 %s 应归属到 %s", punchTimeStr, expectedDateStr));
    }

    /**
     * 测试边界条件 - 空值处理
     */
    @Test
    @DisplayName("边界条件 - null值处理")
    void testNullHandling() {
        // 场景：传入null的打卡时间
        // 期望：抛出IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            CrossDayShiftUtil.calculateAttendanceDate(
                    null, LocalTime.of(22, 0), LocalTime.of(6, 0),
                    CrossDayShiftUtil.CROSS_DAY_RULE_START_DATE, "CHECK_IN");
        }, "打卡时间为null时应抛出IllegalArgumentException");
    }

    /**
     * 测试边界条件 - 未知跨天规则
     */
    @Test
    @DisplayName("边界条件 - 未知跨天规则使用默认值")
    void testUnknownCrossDayRule() {
        LocalDateTime punchTime = LocalDateTime.of(2025, 1, 2, 6, 5);
        LocalTime workStartTime = LocalTime.of(22, 0);
        LocalTime workEndTime = LocalTime.of(6, 0);
        String unknownRule = "UNKNOWN_RULE";

        // 期望：使用默认START_DATE规则（应用缓冲期），不应抛出异常
        assertDoesNotThrow(() -> {
            LocalDate result = CrossDayShiftUtil.calculateAttendanceDate(
                    punchTime, workStartTime, workEndTime, unknownRule, "CHECK_OUT");
            assertEquals(LocalDate.of(2025, 1, 1), result,
                    "未知规则使用默认START_DATE规则，06:05下班打卡在缓冲期内应归属到1月1日");
        });
    }
}

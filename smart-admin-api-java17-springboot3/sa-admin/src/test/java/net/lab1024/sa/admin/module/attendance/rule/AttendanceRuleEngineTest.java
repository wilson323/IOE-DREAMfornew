package net.lab1024.sa.admin.module.attendance.rule;

import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 考勤规则引擎测试
 *
 * <p>
 * 测试考勤规则引擎的各种功能：
 * - 位置验证
 * - 设备验证
 * - 考勤状态计算
 * - 工作日检查
 * - 工作时长计算
 * - 加班时长计算
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("考勤规则引擎测试")
class AttendanceRuleEngineTest {

    private AttendanceRuleEngine ruleEngine;

    private AttendanceRuleEntity testRule;
    private AttendanceRecordEntity testRecord;

    @BeforeEach
    void setUp() {
        ruleEngine = new AttendanceRuleEngine();

        // 创建测试用的考勤规则
        testRule = new AttendanceRuleEntity();
        testRule.setRuleId(1L);
        testRule.setWorkStartTime(LocalTime.of(9, 0));
        testRule.setWorkEndTime(LocalTime.of(18, 0));
        testRule.setBreakStartTime(LocalTime.of(12, 0));
        testRule.setBreakEndTime(LocalTime.of(13, 0));
        testRule.setLocationRequired(true);
        testRule.setDeviceRequired(true);
        testRule.setMaxDistance(500.0);
        testRule.setLateGraceMinutes(5);
        testRule.setEarlyLeaveGraceMinutes(5);

        // 创建测试用的考勤记录
        testRecord = new AttendanceRecordEntity();
        testRecord.setRecordId(1L);
        testRecord.setEmployeeId(100L);
        testRecord.setAttendanceDate(LocalDate.now());
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));
    }

    @Test
    @DisplayName("位置验证测试 - 有效位置")
    void testValidateLocation_ValidLocation() {
        // Given: 有效位置（公司附近）
        Double latitude = 39.9042;  // 示例坐标
        Double longitude = 116.4074;

        // When: 验证位置
        boolean result = ruleEngine.validateLocation(100L, latitude, longitude);

        // Then: 应该通过验证
        assertTrue(result, "有效位置应该通过验证");
    }

    @Test
    @DisplayName("位置验证测试 - 超出距离")
    void testValidateLocation_InvalidLocation() {
        // Given: 超出允许距离的位置
        Double latitude = 40.9042;  // 远离公司的坐标
        Double longitude = 117.4074;

        // When: 验证位置
        boolean result = ruleEngine.validateLocation(100L, latitude, longitude);

        // Then: 应该验证失败
        assertFalse(result, "超出距离的位置应该验证失败");
    }

    @Test
    @DisplayName("位置验证测试 - 无位置信息")
    void testValidateLocation_NoLocation() {
        // Given: 没有位置信息
        Double latitude = null;
        Double longitude = null;

        // When: 验证位置
        boolean result = ruleEngine.validateLocation(100L, latitude, longitude);

        // Then: 应该通过验证（跳过验证）
        assertTrue(result, "无位置信息应该跳过验证");
    }

    @Test
    @DisplayName("设备验证测试 - 有效设备")
    void testValidateDevice_ValidDevice() {
        // Given: 有效设备ID
        String deviceId = "MOBILE_100";

        // When: 验证设备
        boolean result = ruleEngine.validateDevice(100L, deviceId);

        // Then: 应该通过验证
        assertTrue(result, "有效设备应该通过验证");
    }

    @Test
    @DisplayName("设备验证测试 - 无效设备")
    void testValidateDevice_InvalidDevice() {
        // Given: 无效设备ID
        String deviceId = "UNKNOWN_DEVICE";

        // When: 验证设备
        boolean result = ruleEngine.validateDevice(100L, deviceId);

        // Then: 应该验证失败
        assertFalse(result, "无效设备应该验证失败");
    }

    @Test
    @DisplayName("设备验证测试 - 空设备ID")
    void testValidateDevice_EmptyDevice() {
        // Given: 空设备ID
        String deviceId = "";

        // When: 验证设备
        boolean result = ruleEngine.validateDevice(100L, deviceId);

        // Then: 应该验证失败
        assertFalse(result, "空设备ID应该验证失败");
    }

    @Test
    @DisplayName("考勤状态计算测试 - 正常")
    void testCalculateAttendanceStatus_Normal() {
        // Given: 正常打卡（9:00上班，18:00下班）
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: 计算考勤状态
        String status = ruleEngine.calculateAttendanceStatus(testRecord, testRule);

        // Then: 应该为正常
        assertEquals("NORMAL", status, "正常打卡应该返回NORMAL状态");
    }

    @Test
    @DisplayName("考勤状态计算测试 - 迟到")
    void testCalculateAttendanceStatus_Late() {
        // Given: 迟到打卡（9:10上班，18:00下班）
        testRecord.setPunchInTime(LocalTime.of(9, 10));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: 计算考勤状态
        String status = ruleEngine.calculateAttendanceStatus(testRecord, testRule);

        // Then: 应该为迟到
        assertEquals("LATE", status, "迟到打卡应该返回LATE状态");
    }

    @Test
    @DisplayName("考勤状态计算测试 - 早退")
    void testCalculateAttendanceStatus_EarlyLeave() {
        // Given: 早退打卡（9:00上班，17:50下班）
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(17, 50));

        // When: 计算考勤状态
        String status = ruleEngine.calculateAttendanceStatus(testRecord, testRule);

        // Then: 应该为早退
        assertEquals("EARLY_LEAVE", status, "早退打卡应该返回EARLY_LEAVE状态");
    }

    @Test
    @DisplayName("考勤状态计算测试 - 迟到且早退")
    void testCalculateAttendanceStatus_LateAndEarlyLeave() {
        // Given: 迟到且早退打卡（9:10上班，17:50下班）
        testRecord.setPunchInTime(LocalTime.of(9, 10));
        testRecord.setPunchOutTime(LocalTime.of(17, 50));

        // When: 计算考勤状态
        String status = ruleEngine.calculateAttendanceStatus(testRecord, testRule);

        // Then: 应该为异常
        assertEquals("ABNORMAL", status, "迟到且早退应该返回ABNORMAL状态");
    }

    @Test
    @DisplayName("考勤状态计算测试 - 旷工")
    void testCalculateAttendanceStatus_Absent() {
        // Given: 没有打卡记录
        testRecord.setPunchInTime(null);
        testRecord.setPunchOutTime(null);

        // When: 计算考勤状态
        String status = ruleEngine.calculateAttendanceStatus(testRecord, testRule);

        // Then: 应该为旷工
        assertEquals("ABSENT", status, "无打卡记录应该返回ABSENT状态");
    }

    @Test
    @DisplayName("迟到检查测试 - 未迟到")
    void testIsLate_NotLate() {
        // Given: 准时上班（9:00打卡）
        testRecord.setPunchInTime(LocalTime.of(9, 0));

        // When: 检查是否迟到
        boolean isLate = ruleEngine.isLate(testRecord, testRule);

        // Then: 应该未迟到
        assertFalse(isLate, "准时上班不应该被判断为迟到");
    }

    @Test
    @DisplayName("迟到检查测试 - 迟到")
    void testIsLate_Late() {
        // Given: 迟到上班（9:30打卡）
        testRecord.setPunchInTime(LocalTime.of(9, 30));

        // When: 检查是否迟到
        boolean isLate = ruleEngine.isLate(testRecord, testRule);

        // Then: 应该迟到
        assertTrue(isLate, "迟到上班应该被判断为迟到");
    }

    @Test
    @DisplayName("迟到检查测试 - 在宽限时间内")
    void testIsLate_WithinGracePeriod() {
        // Given: 在宽限时间内迟到（9:05打卡，宽限5分钟）
        testRule.setLateGraceMinutes(5);
        testRecord.setPunchInTime(LocalTime.of(9, 5));

        // When: 检查是否迟到
        boolean isLate = ruleEngine.isLate(testRecord, testRule);

        // Then: 应该未迟到
        assertFalse(isLate, "在宽限时间内的迟到不应该被判断为迟到");
    }

    @Test
    @DisplayName("早退检查测试 - 未早退")
    void testIsEarlyLeave_NotEarlyLeave() {
        // Given: 准时下班（18:00打卡）
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: 检查是否早退
        boolean isEarlyLeave = ruleEngine.isEarlyLeave(testRecord, testRule);

        // Then: 应该未早退
        assertFalse(isEarlyLeave, "准时下班不应该被判断为早退");
    }

    @Test
    @DisplayName("早退检查测试 - 早退")
    void testIsEarlyLeave_EarlyLeave() {
        // Given: 早退下班（17:30打卡）
        testRecord.setPunchOutTime(LocalTime.of(17, 30));

        // When: 检查是否早退
        boolean isEarlyLeave = ruleEngine.isEarlyLeave(testRecord, testRule);

        // Then: 应该早退
        assertTrue(isEarlyLeave, "早退下班应该被判断为早退");
    }

    @Test
    @DisplayName("早退检查测试 - 在宽限时间内")
    void testIsEarlyLeave_WithinGracePeriod() {
        // Given: 在宽限时间内早退（17:55打卡，宽限5分钟）
        testRule.setEarlyLeaveGraceMinutes(5);
        testRecord.setPunchOutTime(LocalTime.of(17, 55));

        // When: 检查是否早退
        boolean isEarlyLeave = ruleEngine.isEarlyLeave(testRecord, testRule);

        // Then: 应该未早退
        assertFalse(isEarlyLeave, "在宽限时间内的早退不应该被判断为早退");
    }

    @Test
    @DisplayName("工作日检查测试 - 工作日")
    void testIsWorkingDay_WorkingDay() {
        // Given: 周三（工作日）
        LocalDate date = LocalDate.of(2025, 11, 13); // 2025-11-13是周四

        // When: 检查是否为工作日
        boolean isWorkingDay = ruleEngine.isWorkingDay(100L, date);

        // Then: 应该是工作日
        assertTrue(isWorkingDay, "周四应该是工作日");
    }

    @Test
    @DisplayName("工作日检查测试 - 周末")
    void testIsWorkingDay_Weekend() {
        // Given: 周六（周末）
        LocalDate date = LocalDate.of(2025, 11, 15); // 2025-11-15是周六

        // When: 检查是否为工作日
        boolean isWorkingDay = ruleEngine.isWorkingDay(100L, date);

        // Then: 应该不是工作日
        assertFalse(isWorkingDay, "周六不应该是工作日");
    }

    @Test
    @DisplayName("工作日检查测试 - 周日")
    void testIsWorkingDay_Weekend() {
        // Given: 周日（周末）
        LocalDate date = LocalDate.of(2025, 11, 16); // 2025-11-16是周日

        // When: 检查是否为工作日
        boolean isWorkingDay = ruleEngine.isWorkingDay(100L, date);

        // Then: 应该不是工作日
        assertFalse(isWorkingDay, "周日不应该是工作日");
    }

    @Test
    @DisplayName("工作时长计算测试 - 正常工作时间")
    void testCalculateWorkHours_NormalWorkHours() {
        // Given: 9:00-18:00工作，包含1小时休息时间
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: 计算工作时长
        double workHours = ruleEngine.calculateWorkHours(testRecord, testRule);

        // Then: 应该是8小时（9小时减去1小时休息）
        assertEquals(8.0, workHours, "正常工作时间应该是8小时");
    }

    @Test
    @DisplayName("工作时长计算测试 - 无休息时间")
    void testCalculateWorkHours_NoBreakTime() {
        // Given: 9:00-18:00工作，没有休息时间
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));
        testRule.setBreakStartTime(null);
        testRule.setBreakEndTime(null);

        // When: 计算工作时长
        double workHours = ruleEngine.calculateWorkHours(testRecord, testRule);

        // Then: 应该是9小时
        assertEquals(9.0, workHours, "无休息时间的工作时间应该是9小时");
    }

    @Test
    @DisplayName("工作时长计算测试 - 跨天工作")
    void testCalculateWorkHours_CrossDayWork() {
        // Given: 跨天工作（22:00-次日6:00）
        testRecord.setPunchInTime(LocalTime.of(22, 0));
        testRecord.setPunchOutTime(LocalTime.of(6, 0));

        // When: 计算工作时长
        double workHours = ruleEngine.calculateWorkHours(testRecord, testRule);

        // Then: 应该是8小时
        assertEquals(8.0, workHours, "跨天工作时间应该是8小时");
    }

    @Test
    @DisplayName("加班时长计算测试 - 无加班")
    void testCalculateOvertimeHours_NoOvertime() {
        // Given: 准时下班（18:00下班）
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: 计算加班时长
        double overtimeHours = ruleEngine.calculateOvertimeHours(testRecord, testRule);

        // Then: 应该没有加班
        assertEquals(0.0, overtimeHours, "准时下班应该没有加班时长");
    }

    @Test
    @DisplayName("加班时长计算测试 - 有加班")
    void testCalculateOvertimeHours_HasOvertime() {
        // Given: 加班（19:00下班）
        testRecord.setPunchOutTime(LocalTime.of(19, 0));

        // When: 计算加班时长
        double overtimeHours = ruleEngine.calculateOvertimeHours(testRecord, testRule);

        // Then: 应该是1小时加班
        assertEquals(1.0, overtimeHours, "加班1小时应该返回1.0加班时长");
    }

    @Test
    @DisplayName("加班时长计算测试 - 无下班记录")
    void testCalculateOvertimeHours_NoPunchOut() {
        // Given: 没有下班打卡
        testRecord.setPunchOutTime(null);

        // When: 计算加班时长
        double overtimeHours = ruleEngine.calculateOvertimeHours(testRecord, testRule);

        // Then: 应该没有加班
        assertEquals(0.0, overtimeHours, "无下班打卡应该没有加班时长");
    }

    @Test
    @DisplayName("缓存清除测试")
    void testClearCache() {
        // When: 清除缓存
        ruleEngine.clearCache();

        // Then: 应该成功执行（不抛异常）
        assertDoesNotThrow(() -> ruleEngine.clearCache(), "清除缓存不应该抛出异常");
    }

    @Test
    @DisplayName("员工缓存清除测试")
    void testClearEmployeeCache() {
        // When: 清除员工缓存
        ruleEngine.clearEmployeeCache(100L);

        // Then: 应该成功执行（不抛异常）
        assertDoesNotThrow(() -> ruleEngine.clearEmployeeCache(100L), "清除员工缓存不应该抛出异常");
    }
}
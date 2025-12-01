package net.lab1024.sa.admin.module.attendance;

import net.lab1024.sa.admin.module.attendance.rule.AttendanceRuleEngine;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 考勤规则引擎简单测试类
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
public class AttendanceRuleEngineSimpleTest {

    private AttendanceRuleEngine ruleEngine;
    private AttendanceRuleEntity testRule;
    private AttendanceRecordEntity testRecord;

    @BeforeEach
    void setUp() {
        ruleEngine = new AttendanceRuleEngine();

        // Create test rule
        testRule = new AttendanceRuleEntity();
        testRule.setRuleId(1L);
        testRule.setWorkStartTime(LocalTime.of(9, 0));
        testRule.setWorkEndTime(LocalTime.of(18, 0));
        testRule.setBreakStartTime(LocalTime.of(12, 0));
        testRule.setBreakEndTime(LocalTime.of(13, 0));
        testRule.setLocationRequired(false);
        testRule.setDeviceRequired(false);
        testRule.setMaxDistance(500.0);
        testRule.setLateGraceMinutes(5);
        testRule.setEarlyLeaveGraceMinutes(5);

        // Create test record
        testRecord = new AttendanceRecordEntity();
        testRecord.setRecordId(1L);
        testRecord.setEmployeeId(100L);
        testRecord.setAttendanceDate(LocalDate.now());
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));
    }

    @Test
    void testValidateLocation_NoLocation() {
        // Given: No location info
        Double latitude = null;
        Double longitude = null;

        // When: Validate location
        boolean result = ruleEngine.validateLocation(100L, latitude, longitude);

        // Then: Should pass validation
        assertTrue(result, "No location should pass validation");
    }

    @Test
    void testValidateDevice_ValidDevice() {
        // Given: Valid device ID
        String deviceId = "MOBILE_100";

        // When: Validate device
        boolean result = ruleEngine.validateDevice(100L, deviceId);

        // Then: Should pass validation
        assertTrue(result, "Valid device should pass validation");
    }

    @Test
    void testCalculateAttendanceStatus_Normal() {
        // Given: Normal punch time
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: Calculate attendance status
        String status = ruleEngine.calculateAttendanceStatus(testRecord, testRule);

        // Then: Should be normal
        assertEquals("NORMAL", status, "Normal punch should return NORMAL status");
    }

    @Test
    void testIsLate_NotLate() {
        // Given: On time punch in
        testRecord.setPunchInTime(LocalTime.of(9, 0));

        // When: Check if late
        boolean isLate = ruleEngine.isLate(testRecord, testRule);

        // Then: Should not be late
        assertFalse(isLate, "On time punch should not be late");
    }

    @Test
    void testIsLate_Late() {
        // Given: Late punch in
        testRecord.setPunchInTime(LocalTime.of(9, 30));

        // When: Check if late
        boolean isLate = ruleEngine.isLate(testRecord, testRule);

        // Then: Should be late
        assertTrue(isLate, "Late punch should be detected as late");
    }

    @Test
    void testCalculateWorkHours_NormalWorkHours() {
        // Given: 9:00-18:00 work with 1 hour break
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: Calculate work hours
        double workHours = ruleEngine.calculateWorkHours(testRecord, testRule);

        // Then: Should be 8 hours
        assertEquals(8.0, workHours, "Normal work hours should be 8 hours");
    }

    @Test
    void testCalculateOvertimeHours_NoOvertime() {
        // Given: On time punch out
        testRecord.setPunchOutTime(LocalTime.of(18, 0));

        // When: Calculate overtime hours
        double overtimeHours = ruleEngine.calculateOvertimeHours(testRecord, testRule);

        // Then: Should be no overtime
        assertEquals(0.0, overtimeHours, "On time punch out should have no overtime");
    }

    @Test
    void testIsWorkingDay_WorkingDay() {
        // Given: Thursday (working day)
        LocalDate date = LocalDate.of(2025, 11, 13); // 2025-11-13 is Thursday

        // When: Check if working day
        boolean isWorkingDay = ruleEngine.isWorkingDay(100L, date);

        // Then: Should be working day
        assertTrue(isWorkingDay, "Thursday should be working day");
    }

    @Test
    void testClearCache() {
        // When: Clear cache
        ruleEngine.clearCache();

        // Then: Should not throw exception
        assertDoesNotThrow(() -> ruleEngine.clearCache(), "Clear cache should not throw exception");
    }

    @Test
    void testClearEmployeeCache() {
        // When: Clear employee cache
        ruleEngine.clearEmployeeCache(100L);

        // Then: Should not throw exception
        assertDoesNotThrow(() -> ruleEngine.clearEmployeeCache(100L), "Clear employee cache should not throw exception");
    }
}
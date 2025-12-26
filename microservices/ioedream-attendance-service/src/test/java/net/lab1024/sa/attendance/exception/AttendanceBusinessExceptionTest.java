package net.lab1024.sa.attendance.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * AttendanceBusinessException 单元测试
 * <p>
 * 测试考勤业务异常的各种构造函数和方法 验证异常信息的正确性和完整性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤业务异常测试")
class AttendanceBusinessExceptionTest {

    @Test
    @DisplayName("使用错误码构造异常")
    void testConstructorWithErrorCode () {
        // Given
        AttendanceBusinessException.ErrorCode errorCode = AttendanceBusinessException.ErrorCode.ATTENDANCE_RECORD_NOT_FOUND;

        // When
        AttendanceBusinessException exception = new AttendanceBusinessException (errorCode);

        // Then
        assertEquals (errorCode, exception.getErrorCode ());
        assertEquals (errorCode.getDefaultMessage (), exception.getMessage ());
        assertNull (exception.getBusinessId ());
    }

    @Test
    @DisplayName("测试打卡失败工厂方法")
    void testClockInFailedFactoryMethod () {
        // Given
        String userId = "USER001";
        String reason = "GPS信号弱";

        // When
        AttendanceBusinessException exception = AttendanceBusinessException.clockInFailed (userId, reason);

        // Then
        assertEquals (AttendanceBusinessException.ErrorCode.CLOCK_IN_FAILED, exception.getErrorCode ());
        assertTrue (exception.getMessage ().contains ("打卡失败"));
        assertTrue (exception.getMessage ().contains (reason));
        assertEquals (userId, exception.getBusinessId ());
        assertEquals ("CLOCK_IN_FAILED", exception.getCode ());
    }

    @Test
    @DisplayName("测试班次冲突工厂方法")
    void testScheduleConflictFactoryMethod () {
        // Given
        String userId = "USER002";
        String scheduleId = "SCH001";

        // When
        AttendanceBusinessException exception = AttendanceBusinessException.scheduleConflict (userId, scheduleId);

        // Then
        assertEquals (AttendanceBusinessException.ErrorCode.SCHEDULE_CONFLICT, exception.getErrorCode ());
        assertEquals ("班次冲突", exception.getMessage ());
        assertEquals (userId + "|" + scheduleId, exception.getBusinessId ());
        assertEquals ("SCHEDULE_CONFLICT", exception.getCode ());
    }

    @Test
    @DisplayName("验证getCode()和getDetails()兼容性方法")
    void testCompatibilityMethods () {
        // Given
        String userId = "USER003";
        AttendanceBusinessException exception = AttendanceBusinessException.clockInFailed (userId, "测试原因");

        // When
        String code = exception.getCode ();
        Object details = exception.getDetails ();

        // Then
        assertEquals ("CLOCK_IN_FAILED", code);
        assertEquals (userId, details);
    }
}

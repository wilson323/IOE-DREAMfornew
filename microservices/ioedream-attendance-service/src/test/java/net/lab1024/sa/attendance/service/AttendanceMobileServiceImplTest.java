package net.lab1024.sa.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceShiftDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.service.impl.AttendanceMobileServiceImpl;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import net.lab1024.sa.common.system.employee.domain.entity.EmployeeEntity;

/**
 * AttendanceMobileServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of AttendanceMobileServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceMobileServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class AttendanceMobileServiceImplTest {

    @Mock
    private AttendanceRecordDao attendanceRecordDao;

    @Mock
    private EmployeeDao employeeDao;

    @Mock
    private AttendanceShiftDao attendanceShiftDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private AttendanceMobileServiceImpl attendanceMobileServiceImpl;

    private EmployeeEntity mockEmployee;
    private AttendanceRecordEntity mockCheckInRecord;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockEmployee = new EmployeeEntity();
        mockEmployee.setId(100L);
        mockEmployee.setUserId(100L);
        mockEmployee.setEmployeeName("Test Employee");
        mockEmployee.setDepartmentId(1L);
        mockEmployee.setDeletedFlag(0);

        mockCheckInRecord = new AttendanceRecordEntity();
        mockCheckInRecord.setRecordId(1L);
        mockCheckInRecord.setUserId(100L);
        mockCheckInRecord.setAttendanceDate(LocalDate.now());
        mockCheckInRecord.setAttendanceType("CHECK_IN");
        mockCheckInRecord.setShiftId(1L);
        mockCheckInRecord.setShiftName("Normal Shift");
    }

    @Test
    @DisplayName("Test gpsPunch - Success Scenario (Check In)")
    void test_gpsPunch_Success_CheckIn() {
        // Given
        Long employeeId = 100L;
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        String address = "Test Address";
        String photoUrl = "http://example.com/photo.jpg";

        when(employeeDao.selectById(employeeId)).thenReturn(mockEmployee);
        // Mock checkLocationInRange to return true (this is a private method, so we test through behavior)
        when(attendanceRecordDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // No existing record
        doAnswer(invocation -> {
            AttendanceRecordEntity entity = invocation.getArgument(0);
            entity.setRecordId(1L);
            return 1;
        }).when(attendanceRecordDao).insert(any(AttendanceRecordEntity.class));

        // When
        ResponseDTO<String> result = attendanceMobileServiceImpl.gpsPunch(employeeId, latitude, longitude, address, photoUrl);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(employeeDao, atLeastOnce()).selectById(employeeId);
        verify(attendanceRecordDao, atLeastOnce()).selectOne(any(LambdaQueryWrapper.class));
        verify(attendanceRecordDao, times(1)).insert(any(AttendanceRecordEntity.class));
    }

    @Test
    @DisplayName("Test gpsPunch - Success Scenario (Check Out)")
    void test_gpsPunch_Success_CheckOut() {
        // Given
        Long employeeId = 100L;
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        String address = "Test Address";
        String photoUrl = "http://example.com/photo.jpg";

        when(employeeDao.selectById(employeeId)).thenReturn(mockEmployee);
        // First call returns checkInRecord, second call returns null (no checkOutRecord)
        when(attendanceRecordDao.selectOne(any(LambdaQueryWrapper.class)))
            .thenReturn(mockCheckInRecord)  // Check in record exists
            .thenReturn(null);  // No check out record
        doAnswer(invocation -> {
            AttendanceRecordEntity entity = invocation.getArgument(0);
            entity.setRecordId(2L);
            return 1;
        }).when(attendanceRecordDao).insert(any(AttendanceRecordEntity.class));

        // When
        ResponseDTO<String> result = attendanceMobileServiceImpl.gpsPunch(employeeId, latitude, longitude, address, photoUrl);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(employeeDao, atLeastOnce()).selectById(employeeId);
        verify(attendanceRecordDao, atLeast(2)).selectOne(any(LambdaQueryWrapper.class));
        verify(attendanceRecordDao, times(1)).insert(any(AttendanceRecordEntity.class));
    }

    @Test
    @DisplayName("Test gpsPunch - Null EmployeeId")
    void test_gpsPunch_NullEmployeeId() {
        // Given
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        String address = "Test Address";
        String photoUrl = "http://example.com/photo.jpg";

        // When & Then
        ParamException exception = assertThrows(ParamException.class, () -> {
            attendanceMobileServiceImpl.gpsPunch(null, latitude, longitude, address, photoUrl);
        });
        assertTrue(exception.getMessage().contains("员工ID不能为空"));
        verify(employeeDao, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test gpsPunch - Null GPS Coordinates")
    void test_gpsPunch_NullCoordinates() {
        // Given
        Long employeeId = 100L;
        String address = "Test Address";
        String photoUrl = "http://example.com/photo.jpg";

        // When & Then
        ParamException exception = assertThrows(ParamException.class, () -> {
            attendanceMobileServiceImpl.gpsPunch(employeeId, null, null, address, photoUrl);
        });
        assertTrue(exception.getMessage().contains("GPS坐标不能为空"));
        verify(employeeDao, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test gpsPunch - Empty Address")
    void test_gpsPunch_EmptyAddress() {
        // Given
        Long employeeId = 100L;
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        String photoUrl = "http://example.com/photo.jpg";

        // When & Then
        ParamException exception = assertThrows(ParamException.class, () -> {
            attendanceMobileServiceImpl.gpsPunch(employeeId, latitude, longitude, "", photoUrl);
        });
        assertTrue(exception.getMessage().contains("打卡地址不能为空"));
        verify(employeeDao, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test gpsPunch - Employee Not Found")
    void test_gpsPunch_EmployeeNotFound() {
        // Given
        Long employeeId = 999L;
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        String address = "Test Address";
        String photoUrl = "http://example.com/photo.jpg";

        when(employeeDao.selectById(employeeId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceMobileServiceImpl.gpsPunch(employeeId, latitude, longitude, address, photoUrl);
        });
        assertEquals("员工不存在", exception.getMessage());
        verify(employeeDao, times(1)).selectById(employeeId);
    }

    @Test
    @DisplayName("Test gpsPunch - Already Punched")
    void test_gpsPunch_AlreadyPunched() {
        // Given
        Long employeeId = 100L;
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        String address = "Test Address";
        String photoUrl = "http://example.com/photo.jpg";

        AttendanceRecordEntity checkOutRecord = new AttendanceRecordEntity();
        checkOutRecord.setRecordId(2L);
        checkOutRecord.setUserId(100L);
        checkOutRecord.setAttendanceDate(LocalDate.now());
        checkOutRecord.setAttendanceType("CHECK_OUT");

        when(employeeDao.selectById(employeeId)).thenReturn(mockEmployee);
        when(attendanceRecordDao.selectOne(any(LambdaQueryWrapper.class)))
            .thenReturn(mockCheckInRecord)  // Check in record exists
            .thenReturn(checkOutRecord);  // Check out record also exists

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceMobileServiceImpl.gpsPunch(employeeId, latitude, longitude, address, photoUrl);
        });
        assertEquals("今日已完成上下班打卡", exception.getMessage());
        verify(employeeDao, atLeastOnce()).selectById(employeeId);
        verify(attendanceRecordDao, atLeast(2)).selectOne(any(LambdaQueryWrapper.class));
        verify(attendanceRecordDao, never()).insert(any(AttendanceRecordEntity.class));
    }
}


package net.lab1024.sa.visitor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.common.entity.visitor.VisitorAppointmentEntity;

/**
 * VisitorCheckInServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of VisitorCheckInServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorCheckInServiceImpl Unit Test")
class VisitorCheckInServiceImplTest {

    @Mock
    private VisitorAppointmentDao visitorAppointmentDao;

    @Spy
    @InjectMocks
    private net.lab1024.sa.visitor.service.impl.VisitorCheckInServiceImpl visitorCheckInServiceImpl;

    private VisitorAppointmentEntity mockAppointment;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockAppointment = new VisitorAppointmentEntity();
        mockAppointment.setAppointmentId(1L);
        mockAppointment.setVisitorName("Test Visitor");
        mockAppointment.setStatus("APPROVED");
        mockAppointment.setCheckInTime(null);
        mockAppointment.setCreateTime(LocalDateTime.now());
        mockAppointment.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test checkIn - Success Scenario")
    void test_checkIn_Success() {
        // Given
        Long appointmentId = 1L;
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(mockAppointment);
        when(visitorAppointmentDao.updateById(any(VisitorAppointmentEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = visitorCheckInServiceImpl.checkIn(appointmentId);

        // Then
        assertTrue(result.isSuccess());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
        verify(visitorAppointmentDao, times(1)).updateById(any(VisitorAppointmentEntity.class));
    }

    @Test
    @DisplayName("Test checkIn - Null AppointmentId")
    void test_checkIn_NullAppointmentId() {
        // When & Then
        ParamException exception = assertThrows(ParamException.class, () -> {
            visitorCheckInServiceImpl.checkIn(null);
        });
        assertTrue(exception.getMessage().contains("预约ID不能为空"));
        verify(visitorAppointmentDao, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test checkIn - Appointment Not Found")
    void test_checkIn_AppointmentNotFound() {
        // Given
        Long appointmentId = 999L;
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            visitorCheckInServiceImpl.checkIn(appointmentId);
        });
        assertEquals("预约不存在", exception.getMessage());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
    }

    @Test
    @DisplayName("Test checkIn - Appointment Not Approved")
    void test_checkIn_AppointmentNotApproved() {
        // Given
        Long appointmentId = 1L;
        mockAppointment.setStatus("PENDING");
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(mockAppointment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            visitorCheckInServiceImpl.checkIn(appointmentId);
        });
        assertEquals("预约未通过审核", exception.getMessage());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
    }

    @Test
    @DisplayName("Test checkIn - Already Checked In")
    void test_checkIn_AlreadyCheckedIn() {
        // Given
        Long appointmentId = 1L;
        mockAppointment.setCheckInTime(LocalDateTime.now());
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(mockAppointment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            visitorCheckInServiceImpl.checkIn(appointmentId);
        });
        assertEquals("已经签到过了", exception.getMessage());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
    }

    @Test
    @DisplayName("Test checkOut - Success Scenario")
    void test_checkOut_Success() {
        // Given
        Long appointmentId = 1L;
        mockAppointment.setCheckInTime(LocalDateTime.now());
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(mockAppointment);
        when(visitorAppointmentDao.updateById(any(VisitorAppointmentEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = visitorCheckInServiceImpl.checkOut(appointmentId);

        // Then
        assertTrue(result.isSuccess());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
        verify(visitorAppointmentDao, times(1)).updateById(any(VisitorAppointmentEntity.class));
    }

    @Test
    @DisplayName("Test checkOut - Not Checked In")
    void test_checkOut_NotCheckedIn() {
        // Given
        Long appointmentId = 1L;
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(mockAppointment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            visitorCheckInServiceImpl.checkOut(appointmentId);
        });
        assertEquals("还未签到，不能签退", exception.getMessage());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
    }

    @Test
    @DisplayName("Test checkOut - Already Checked Out")
    void test_checkOut_AlreadyCheckedOut() {
        // Given
        Long appointmentId = 1L;
        mockAppointment.setCheckInTime(LocalDateTime.now());
        mockAppointment.setStatus("CHECKED_OUT");
        when(visitorAppointmentDao.selectById(appointmentId)).thenReturn(mockAppointment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            visitorCheckInServiceImpl.checkOut(appointmentId);
        });
        assertEquals("已经签退过了", exception.getMessage());
        verify(visitorAppointmentDao, times(1)).selectById(appointmentId);
    }
}

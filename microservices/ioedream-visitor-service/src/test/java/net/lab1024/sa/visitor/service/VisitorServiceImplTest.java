package net.lab1024.sa.visitor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * VisitorServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of VisitorServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorServiceImpl Unit Test")
class VisitorServiceImplTest {

    @Mock
    private VisitorAppointmentDao visitorAppointmentDao;

    @Spy
    @InjectMocks
    private net.lab1024.sa.visitor.service.impl.VisitorServiceImpl visitorServiceImpl;

    private VisitorAppointmentEntity mockAppointment;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockAppointment = new VisitorAppointmentEntity();
        mockAppointment.setAppointmentId(1L);
        mockAppointment.setVisitorName("Test Visitor");
        mockAppointment.setPhoneNumber("13800138000");
        mockAppointment.setVisitPurpose("Business meeting");
        mockAppointment.setVisitUserId(100L);
        mockAppointment.setVisitUserName("Host User");
        mockAppointment.setStatus("APPROVED");
        mockAppointment.setCreateTime(LocalDateTime.now());
        mockAppointment.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test getVisitorInfo - Success Scenario")
    void test_getVisitorInfo_Success() {
        // Given
        Long visitorId = 1L;
        when(visitorAppointmentDao.selectById(visitorId)).thenReturn(mockAppointment);

        // When
        ResponseDTO<?> result = visitorServiceImpl.getVisitorInfo(visitorId);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(visitorAppointmentDao, times(1)).selectById(visitorId);
    }

    @Test
    @DisplayName("Test getVisitorInfo - Null VisitorId")
    void test_getVisitorInfo_NullVisitorId() {
        // When & Then
        ParamException exception = assertThrows(ParamException.class, () -> {
            visitorServiceImpl.getVisitorInfo(null);
        });
        assertTrue(exception.getMessage().contains("访客ID不能为空"));
        verify(visitorAppointmentDao, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("Test getVisitorInfo - Visitor Not Found")
    void test_getVisitorInfo_NotFound() {
        // Given
        Long visitorId = 999L;
        when(visitorAppointmentDao.selectById(visitorId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            visitorServiceImpl.getVisitorInfo(visitorId);
        });
        assertEquals("访客信息不存在", exception.getMessage());
        verify(visitorAppointmentDao, times(1)).selectById(visitorId);
    }
}

package net.lab1024.sa.visitor.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;

/**
 * VisitorStatisticsServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of VisitorStatisticsServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorStatisticsServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class VisitorStatisticsServiceImplTest {

    @Mock
    private VisitorAppointmentDao visitorAppointmentDao;

    @Spy
    @InjectMocks
    private net.lab1024.sa.visitor.service.impl.VisitorStatisticsServiceImpl visitorStatisticsServiceImpl;

    @BeforeEach
    void setUp() {
        // Prepare test data
    }

    @Test
    @DisplayName("Test getStatistics - Success Scenario")
    void test_getStatistics_Success() {
        // Given
        when(visitorAppointmentDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);

        // When
        ResponseDTO<Map<String, Object>> result = visitorStatisticsServiceImpl.getStatistics();

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("totalAppointments"));
        verify(visitorAppointmentDao, atLeastOnce()).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getStatisticsByDateRange - Success Scenario")
    void test_getStatisticsByDateRange_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        when(visitorAppointmentDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L);

        // When
        ResponseDTO<Map<String, Object>> result = visitorStatisticsServiceImpl.getStatisticsByDateRange(startDate,
                endDate);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().containsKey("totalAppointments"));
        verify(visitorAppointmentDao, atLeastOnce()).selectCount(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getStatisticsByDateRange - Null StartDate")
    void test_getStatisticsByDateRange_NullStartDate() {
        // When & Then
        assertThrows(Exception.class, () -> {
            visitorStatisticsServiceImpl.getStatisticsByDateRange(null, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Test getStatisticsByDateRange - Null EndDate")
    void test_getStatisticsByDateRange_NullEndDate() {
        // When & Then
        assertThrows(Exception.class, () -> {
            visitorStatisticsServiceImpl.getStatisticsByDateRange(LocalDate.now(), null);
        });
    }
}

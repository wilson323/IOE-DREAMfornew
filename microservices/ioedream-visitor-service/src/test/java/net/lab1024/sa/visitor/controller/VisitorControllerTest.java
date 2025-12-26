package net.lab1024.sa.visitor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.visitor.domain.form.VisitorAppointmentQueryForm;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;
import net.lab1024.sa.visitor.service.VisitorAppointmentService;
import net.lab1024.sa.visitor.service.VisitorQueryService;
import net.lab1024.sa.visitor.service.VisitorStatisticsService;

/**
 * VisitorController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：VisitorController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorController单元测试")
class VisitorControllerTest {
    @Mock
    private VisitorAppointmentService visitorAppointmentService;
    @Mock
    private VisitorQueryService visitorQueryService;
    @Mock
    private VisitorStatisticsService visitorStatisticsService;
    @InjectMocks
    private VisitorController visitorController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("分页查询访客预约-成功场景")
    void test_queryAppointments_Success() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 20;
        String visitorName = "张三";
        Long hostUserId = 1001L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        String status = "PENDING";

        PageResult<VisitorAppointmentDetailVO> mockResult = new PageResult<>();
        mockResult.setList(java.util.Collections.emptyList());
        mockResult.setTotal(0L);
        mockResult.setPageNum(pageNum);
        mockResult.setPageSize(pageSize);

        when(visitorAppointmentService.queryAppointments(any(VisitorAppointmentQueryForm.class)))
                .thenReturn(mockResult);

        // When
        ResponseDTO<PageResult<VisitorAppointmentDetailVO>> response = visitorController.queryAppointments(
                pageNum, pageSize, visitorName, hostUserId, startDate, endDate, status);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(0L, response.getData().getTotal());
        verify(visitorAppointmentService, times(1)).queryAppointments(any(VisitorAppointmentQueryForm.class));
    }

    @Test
    @DisplayName("分页查询访客预约-参数异常")
    void test_queryAppointments_ParamException() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 20;

        when(visitorAppointmentService.queryAppointments(any(VisitorAppointmentQueryForm.class)))
                .thenThrow(new ParamException("PARAM_ERROR", "参数错误"));

        // When
        ResponseDTO<PageResult<VisitorAppointmentDetailVO>> response = visitorController.queryAppointments(
                pageNum, pageSize, null, null, null, null, null);

        // Then
        assertEquals(ResponseDTO.error("PARAM_ERROR", "参数错误").getCode(), response.getCode());
        assertNull(response.getData());
        verify(visitorAppointmentService, times(1)).queryAppointments(any(VisitorAppointmentQueryForm.class));
    }

    @Test
    @DisplayName("分页查询访客预约-业务异常")
    void test_queryAppointments_BusinessException() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 20;

        when(visitorAppointmentService.queryAppointments(any(VisitorAppointmentQueryForm.class)))
                .thenThrow(new BusinessException("BUSINESS_ERROR", "业务错误"));

        // When
        ResponseDTO<PageResult<VisitorAppointmentDetailVO>> response = visitorController.queryAppointments(
                pageNum, pageSize, null, null, null, null, null);

        // Then
        assertEquals(ResponseDTO.error("BUSINESS_ERROR", "业务错误").getCode(), response.getCode());
        assertNull(response.getData());
        verify(visitorAppointmentService, times(1)).queryAppointments(any(VisitorAppointmentQueryForm.class));
    }

    @Test
    @DisplayName("获取访客统计-成功场景")
    void test_getVisitorStatistics_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        Map<String, Object> statisticsData = new HashMap<>();
        statisticsData.put("totalVisitors", 100);
        statisticsData.put("totalAppointments", 150);
        statisticsData.put("approvedAppointments", 120);
        statisticsData.put("rejectedAppointments", 10);
        statisticsData.put("pendingAppointments", 20);

        ResponseDTO<Map<String, Object>> mockResult = ResponseDTO.ok(statisticsData);
        when(visitorStatisticsService.getStatisticsByDateRange(startDate, endDate))
                .thenReturn(mockResult);

        // When
        ResponseDTO<Map<String, Object>> response = visitorController.getVisitorStatistics(startDate, endDate);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(100, response.getData().get("totalVisitors"));
        verify(visitorStatisticsService, times(1)).getStatisticsByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("获取访客统计-服务返回null")
    void test_getVisitorStatistics_ServiceReturnsNull() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(visitorStatisticsService.getStatisticsByDateRange(startDate, endDate))
                .thenReturn(null);

        // When
        ResponseDTO<Map<String, Object>> response = visitorController.getVisitorStatistics(startDate, endDate);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(0, response.getData().get("totalVisitors"));
        assertEquals(0, response.getData().get("totalAppointments"));
        verify(visitorStatisticsService, times(1)).getStatisticsByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("获取访客统计-参数异常")
    void test_getVisitorStatistics_ParamException() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(visitorStatisticsService.getStatisticsByDateRange(startDate, endDate))
                .thenThrow(new ParamException("PARAM_ERROR", "参数错误"));

        // When
        ResponseDTO<Map<String, Object>> response = visitorController.getVisitorStatistics(startDate, endDate);

        // Then
        assertEquals(ResponseDTO.error("PARAM_ERROR", "参数错误").getCode(), response.getCode());
        assertNull(response.getData());
        verify(visitorStatisticsService, times(1)).getStatisticsByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("获取访客统计-系统异常")
    void test_getVisitorStatistics_SystemException() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(visitorStatisticsService.getStatisticsByDateRange(startDate, endDate))
                .thenThrow(new SystemException("SYSTEM_ERROR", "系统错误", new RuntimeException()));

        // When
        ResponseDTO<Map<String, Object>> response = visitorController.getVisitorStatistics(startDate, endDate);

        // Then
        assertEquals(ResponseDTO.error("SYSTEM_ERROR", "系统错误").getCode(), response.getCode());
        assertNull(response.getData());
        verify(visitorStatisticsService, times(1)).getStatisticsByDateRange(startDate, endDate);
    }
}


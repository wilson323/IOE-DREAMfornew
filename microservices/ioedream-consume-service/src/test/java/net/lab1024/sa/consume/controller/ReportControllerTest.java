package net.lab1024.sa.consume.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.report.domain.form.ReportParams;
import net.lab1024.sa.consume.service.ConsumeReportService;

/**
 * ReportController单元测试
 * <p>
 * 测试范围：报表管理REST API接口
 * 目标：提升测试覆盖率至70%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController单元测试")
class ReportControllerTest {

    @Mock
    private ConsumeReportService consumeReportService;

    @InjectMocks
    private ReportController reportController;

    private ReportParams reportParams;

    @BeforeEach
    void setUp() {
        reportParams = new ReportParams();
        reportParams.setStartTime(java.time.LocalDateTime.of(2025, 1, 1, 0, 0));
        reportParams.setEndTime(java.time.LocalDateTime.of(2025, 1, 31, 23, 59));
    }

    // ==================== generateReport 测试 ====================

    @Test
    @DisplayName("测试生成消费报表-成功")
    void testGenerateReport_Success() {
        // Given
        Long templateId = 1L;
        Map<String, Object> mockReportData = new HashMap<>();
        mockReportData.put("totalAmount", 10000.00);
        mockReportData.put("totalCount", 100);

        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(mockReportData);
        when(consumeReportService.generateReport(eq(templateId), any(ReportParams.class)))
            .thenReturn(mockResponse);

        // When
        ResponseDTO<Map<String, Object>> result = reportController.generateReport(templateId, reportParams);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeReportService, times(1)).generateReport(eq(templateId), any(ReportParams.class));
    }

    @Test
    @DisplayName("测试生成消费报表-参数为null")
    void testGenerateReport_ParamsIsNull() {
        // Given
        Long templateId = 1L;
        Map<String, Object> mockReportData = new HashMap<>();

        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(mockReportData);
        when(consumeReportService.generateReport(eq(templateId), isNull()))
            .thenReturn(mockResponse);

        // When
        ResponseDTO<Map<String, Object>> result = reportController.generateReport(templateId, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(consumeReportService, times(1)).generateReport(eq(templateId), isNull());
    }

    @Test
    @DisplayName("测试生成消费报表-异常处理")
    void testGenerateReport_Exception() {
        // Given
        Long templateId = 1L;
        when(consumeReportService.generateReport(eq(templateId), any(ReportParams.class)))
            .thenThrow(new RuntimeException("生成报表失败"));

        // When
        ResponseDTO<Map<String, Object>> result = reportController.generateReport(templateId, reportParams);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMessage().contains("生成消费报表失败"));
    }

    // ==================== exportReport 测试 ====================

    @Test
    @DisplayName("测试导出报表-Excel格式")
    void testExportReport_Excel() {
        // Given
        Long templateId = 1L;
        String format = "EXCEL";
        ResponseDTO<String> mockResponse = ResponseDTO.ok("/path/to/report.xlsx");

        when(consumeReportService.exportReport(
            eq(templateId), any(ReportParams.class), eq(format)))
            .thenReturn(mockResponse);

        // When
        ResponseDTO<String> result = reportController.exportReport(templateId, reportParams, format);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(consumeReportService, times(1)).exportReport(
            eq(templateId), any(ReportParams.class), eq(format));
    }

    @Test
    @DisplayName("测试导出报表-PDF格式")
    void testExportReport_PDF() {
        // Given
        Long templateId = 1L;
        String format = "PDF";
        ResponseDTO<String> mockResponse = ResponseDTO.ok("/path/to/report.pdf");

        when(consumeReportService.exportReport(
            eq(templateId), any(ReportParams.class), eq(format)))
            .thenReturn(mockResponse);

        // When
        ResponseDTO<String> result = reportController.exportReport(templateId, reportParams, format);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(consumeReportService, times(1)).exportReport(
            eq(templateId), any(ReportParams.class), eq(format));
    }

    @Test
    @DisplayName("测试导出报表-格式为null")
    void testExportReport_FormatIsNull() {
        // Given
        Long templateId = 1L;
        String format = null;
        ResponseDTO<String> mockResponse = ResponseDTO.ok("/path/to/report.xlsx");
        when(consumeReportService.exportReport(eq(templateId), any(ReportParams.class), isNull()))
                .thenReturn(mockResponse);

        // When & Then
        // 根据实际实现，可能使用默认格式或抛出异常
        ResponseDTO<String> result = reportController.exportReport(templateId, reportParams, format);
        assertNotNull(result);
        assertTrue(result.getOk());
    }

    // ==================== getReportTemplates 测试 ====================

    @Test
    @DisplayName("测试获取报表模板列表-成功")
    void testGetReportTemplates_Success() {
        // Given
        // 使用doReturn().when()方式避免泛型通配符类型捕获问题
        @SuppressWarnings("rawtypes")
        ResponseDTO mockResponse = ResponseDTO.ok(java.util.Collections.emptyList());
        // 使用doReturn避免泛型类型检查
        doReturn(mockResponse).when(consumeReportService).getReportTemplates(any());

        // When
        ResponseDTO<?> result = reportController.getReportTemplates(null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(consumeReportService, times(1)).getReportTemplates(isNull());
    }

    // ==================== getReportStatistics 测试 ====================

    @Test
    @DisplayName("测试获取报表统计数据-成功")
    void testGetReportStatistics_Success() {
        // Given
        String startTime = "2025-01-01T00:00:00";
        String endTime = "2025-01-31T23:59:59";
        Map<String, Object> dimensions = new HashMap<>();

        Map<String, Object> mockStatistics = new HashMap<>();
        mockStatistics.put("totalConsume", 100000.00);
        mockStatistics.put("todayConsume", 5000.00);

        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(mockStatistics);
        when(consumeReportService.getReportStatistics(
            any(java.time.LocalDateTime.class),
            any(java.time.LocalDateTime.class),
            any()))
            .thenReturn(mockResponse);

        // When
        ResponseDTO<Map<String, Object>> result = reportController.getReportStatistics(
            startTime, endTime, dimensions);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeReportService, times(1)).getReportStatistics(
            any(java.time.LocalDateTime.class),
            any(java.time.LocalDateTime.class),
            any());
    }

    @Test
    @DisplayName("测试获取报表统计数据-维度为null")
    void testGetReportStatistics_DimensionsIsNull() {
        // Given
        String startTime = "2025-01-01T00:00:00";
        String endTime = "2025-01-31T23:59:59";

        Map<String, Object> mockStatistics = new HashMap<>();
        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(mockStatistics);
        when(consumeReportService.getReportStatistics(
            any(java.time.LocalDateTime.class),
            any(java.time.LocalDateTime.class),
            isNull()))
            .thenReturn(mockResponse);

        // When
        ResponseDTO<Map<String, Object>> result = reportController.getReportStatistics(
            startTime, endTime, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(consumeReportService, times(1)).getReportStatistics(
            any(java.time.LocalDateTime.class),
            any(java.time.LocalDateTime.class),
            isNull());
    }
}



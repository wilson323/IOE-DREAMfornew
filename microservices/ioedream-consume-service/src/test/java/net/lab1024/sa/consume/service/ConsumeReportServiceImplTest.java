package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import net.lab1024.sa.consume.report.manager.ConsumeReportManager;
import net.lab1024.sa.consume.service.impl.ConsumeReportServiceImpl;

/**
 * ConsumeReportServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ConsumeReportServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeReportServiceImpl Unit Test")
class ConsumeReportServiceImplTest {

    @Mock
    private ConsumeReportManager consumeReportManager;

    @InjectMocks
    private ConsumeReportServiceImpl consumeReportServiceImpl;

    private ReportParams mockReportParams;
    private Map<String, Object> mockReportData;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockReportParams = new ReportParams();
        mockReportParams.setStartTime(LocalDateTime.now().minusDays(7));
        mockReportParams.setEndTime(LocalDateTime.now());

        mockReportData = new HashMap<>();
        mockReportData.put("totalAmount", 10000.00);
        mockReportData.put("totalCount", 100);
    }

    @Test
    @DisplayName("Test generateReport - Success Scenario")
    void test_generateReport_Success() {
        // Given
        Long templateId = 1L;
        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(mockReportData);  // 修复：Manager返回ResponseDTO
        when(consumeReportManager.generateReport(templateId, mockReportParams)).thenReturn(mockResponse);

        // When
        ResponseDTO<Map<String, Object>> result = consumeReportServiceImpl.generateReport(templateId, mockReportParams);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(mockReportData, result.getData());
        verify(consumeReportManager, times(1)).generateReport(templateId, mockReportParams);
    }

    @Test
    @DisplayName("Test exportReport - Success Scenario")
    void test_exportReport_Success() {
        // Given
        Long templateId = 1L;
        String exportFormat = "EXCEL";
        String filePath = "/tmp/report.xlsx";
        ResponseDTO<String> mockResponse = ResponseDTO.ok(filePath);  // 修复：Manager返回ResponseDTO
        when(consumeReportManager.exportReport(templateId, mockReportParams, exportFormat)).thenReturn(mockResponse);

        // When
        ResponseDTO<String> result = consumeReportServiceImpl.exportReport(templateId, mockReportParams, exportFormat);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(filePath, result.getData());
        verify(consumeReportManager, times(1)).exportReport(templateId, mockReportParams, exportFormat);
    }

    @Test
    @DisplayName("Test getReportTemplates - Success Scenario")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void test_getReportTemplates_Success() {
        // Given
        String templateType = "CONSUME";
        Map<String, Object> templates = new HashMap<>();
        templates.put("templates", Arrays.asList("Template1", "Template2"));
        ResponseDTO mockResponse = (ResponseDTO) ResponseDTO.ok(templates);  // 修复：使用原始类型避免通配符问题
        when(consumeReportManager.getReportTemplates(templateType)).thenReturn(mockResponse);

        // When
        ResponseDTO<?> result = consumeReportServiceImpl.getReportTemplates(templateType);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeReportManager, times(1)).getReportTemplates(templateType);
    }

    @Test
    @DisplayName("Test getReportStatistics - Success Scenario")
    void test_getReportStatistics_Success() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        Map<String, Object> dimensions = new HashMap<>();
        dimensions.put("dimension", "area");

        ResponseDTO<Map<String, Object>> mockResponse = ResponseDTO.ok(mockReportData);  // 修复：Manager返回ResponseDTO
        when(consumeReportManager.getReportStatistics(startTime, endTime, dimensions)).thenReturn(mockResponse);

        // When
        ResponseDTO<Map<String, Object>> result =
            consumeReportServiceImpl.getReportStatistics(startTime, endTime, dimensions);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(mockReportData, result.getData());
        verify(consumeReportManager, times(1)).getReportStatistics(startTime, endTime, dimensions);
    }
}



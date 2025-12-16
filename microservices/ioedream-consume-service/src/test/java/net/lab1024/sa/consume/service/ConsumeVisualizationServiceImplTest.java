package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.form.ConsumeStatisticsForm;
import net.lab1024.sa.consume.domain.vo.ConsumeComparisonVO;
import net.lab1024.sa.consume.domain.vo.ConsumeForecastAnalysisVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRankingVO;
import net.lab1024.sa.consume.domain.vo.ConsumeStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTrendVO;
import net.lab1024.sa.consume.domain.vo.ConsumeUserBehaviorAnalysisVO;
import net.lab1024.sa.consume.service.impl.ConsumeVisualizationServiceImpl;

/**
 * ConsumeVisualizationServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ConsumeVisualizationServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeVisualizationServiceImpl Unit Test")
class ConsumeVisualizationServiceImplTest {

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private ConsumeVisualizationServiceImpl consumeVisualizationServiceImpl;

    private ConsumeStatisticsForm mockForm;
    private ConsumeStatisticsVO mockStatisticsVO;
    private ConsumeTrendVO mockTrendVO;
    private ConsumeRankingVO mockRankingVO;
    private ConsumeComparisonVO mockComparisonVO;
    private ConsumeUserBehaviorAnalysisVO mockUserBehaviorVO;
    private ConsumeForecastAnalysisVO mockForecastVO;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockForm = new ConsumeStatisticsForm();
        mockForm.setStartDate(LocalDate.parse("2025-01-01"));  // 修复：使用LocalDate类型
        mockForm.setEndDate(LocalDate.parse("2025-01-31"));  // 修复：使用LocalDate类型

        mockStatisticsVO = new ConsumeStatisticsVO();
        mockStatisticsVO.setTotalAmount(new BigDecimal("10000.00"));  // 修复：使用BigDecimal类型
        mockStatisticsVO.setTotalCount(100L);  // 修复：使用Long类型

        mockTrendVO = new ConsumeTrendVO();
        mockTrendVO.setDataPoints(new java.util.ArrayList<>());  // 修复：使用setDataPoints方法

        mockRankingVO = new ConsumeRankingVO();
        mockRankingVO.setRankingList(new java.util.ArrayList<>());

        mockComparisonVO = new ConsumeComparisonVO();
        mockComparisonVO.setDatasets(new java.util.ArrayList<>());  // 修复：使用setDatasets方法

        mockUserBehaviorVO = new ConsumeUserBehaviorAnalysisVO();
        // 修复：ConsumeUserBehaviorAnalysisVO没有setBehaviorData方法，使用其他字段
        mockUserBehaviorVO.setTimeSlotAnalysis(new java.util.ArrayList<>());

        mockForecastVO = new ConsumeForecastAnalysisVO();
        mockForecastVO.setForecastData(new java.util.ArrayList<>());  // 修复：使用List<ForecastDataPoint>类型
    }

    @Test
    @DisplayName("Test getStatistics - Success Scenario")
    void test_getStatistics_Success() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(new java.util.ArrayList<>());

        // When
        ResponseDTO<ConsumeStatisticsVO> result = consumeVisualizationServiceImpl.getStatistics(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeTransactionDao, atLeastOnce()).selectList(any());
    }

    @Test
    @DisplayName("Test getTrend - Success Scenario")
    void test_getTrend_Success() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(new java.util.ArrayList<>());

        // When
        ResponseDTO<ConsumeTrendVO> result = consumeVisualizationServiceImpl.getTrend(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeTransactionDao, atLeastOnce()).selectList(any());
    }

    @Test
    @DisplayName("Test getRanking - Success Scenario")
    void test_getRanking_Success() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(new java.util.ArrayList<>());

        // When
        ResponseDTO<ConsumeRankingVO> result = consumeVisualizationServiceImpl.getRanking(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeTransactionDao, atLeastOnce()).selectList(any());
    }

    @Test
    @DisplayName("Test getComparison - Success Scenario")
    void test_getComparison_Success() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(new java.util.ArrayList<>());

        // When
        ResponseDTO<ConsumeComparisonVO> result = consumeVisualizationServiceImpl.getComparison(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeTransactionDao, atLeastOnce()).selectList(any());
    }

    @Test
    @DisplayName("Test getUserBehaviorAnalysis - Success Scenario")
    void test_getUserBehaviorAnalysis_Success() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(new java.util.ArrayList<>());

        // When
        ResponseDTO<ConsumeUserBehaviorAnalysisVO> result =
            consumeVisualizationServiceImpl.getUserBehaviorAnalysis(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeTransactionDao, atLeastOnce()).selectList(any());
    }

    @Test
    @DisplayName("Test getForecastAnalysis - Success Scenario")
    void test_getForecastAnalysis_Success() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(new java.util.ArrayList<>());

        // When
        ResponseDTO<ConsumeForecastAnalysisVO> result =
            consumeVisualizationServiceImpl.getForecastAnalysis(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(consumeTransactionDao, atLeastOnce()).selectList(any());
    }
}



package net.lab1024.sa.video.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Map;

import net.lab1024.sa.video.sdk.AiSdkProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 人群分析管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@DisplayName("人群分析管理器测试")
class CrowdAnalysisManagerTest {

    @Mock
    private AiSdkProvider aiSdkProvider;

    private CrowdAnalysisManager crowdAnalysisManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 配置 mock 对象默认行为
        when(aiSdkProvider.isAvailable()).thenReturn(true);
        when(aiSdkProvider.countPeople(org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn(0);

        crowdAnalysisManager = new CrowdAnalysisManager(aiSdkProvider);
    }

    @Test
    @DisplayName("计算人流密度 - 返回结果")
    void calculateDensity_shouldReturnResult() {
        byte[] frameData = new byte[1024];

        CrowdAnalysisManager.DensityResult result = crowdAnalysisManager.calculateDensity("CAM001", frameData);

        assertNotNull(result);
        assertEquals("CAM001", result.cameraId());
        assertNotNull(result.timestamp());
    }

    @Test
    @DisplayName("生成热力图 - 返回正确尺寸")
    void generateHeatmap_shouldReturnCorrectSize() {
        CrowdAnalysisManager.HeatmapData heatmap = crowdAnalysisManager.generateHeatmap("CAM001", 640, 480, 32);

        assertNotNull(heatmap);
        assertEquals("CAM001", heatmap.cameraId());
        assertEquals(32, heatmap.gridSize());
        assertEquals(15, heatmap.grid().length); // 480 / 32 = 15
        assertEquals(20, heatmap.grid()[0].length); // 640 / 32 = 20
    }

    @Test
    @DisplayName("拥挤预警检查 - 无数据")
    void checkCrowdWarning_noData_shouldReturnNormal() {
        CrowdAnalysisManager.CrowdWarning warning = crowdAnalysisManager.checkCrowdWarning("CAM_NEW");

        assertNotNull(warning);
        assertFalse(warning.warning());
        assertEquals(CrowdAnalysisManager.CrowdLevel.NORMAL, warning.level());
    }

    @Test
    @DisplayName("获取人流趋势 - 无数据返回空")
    void getFlowTrend_noData_shouldReturnEmpty() {
        Map<LocalDateTime, Integer> trend = crowdAnalysisManager.getFlowTrend("CAM_NEW", 10);

        assertNotNull(trend);
        assertTrue(trend.isEmpty());
    }
}

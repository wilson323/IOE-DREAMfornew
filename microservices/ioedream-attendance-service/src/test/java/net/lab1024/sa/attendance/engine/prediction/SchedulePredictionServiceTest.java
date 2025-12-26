package net.lab1024.sa.attendance.engine.prediction;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.SchedulePredictor;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.SchedulePrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SchedulePredictionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("排班预测服务测试")
@Slf4j
class SchedulePredictionServiceTest {

    @Mock
    private SchedulePredictor schedulePredictor;

    private SchedulePredictionService schedulePredictionService;

    @BeforeEach
    void setUp() {
        schedulePredictionService = new SchedulePredictionService(schedulePredictor);
    }

    @Test
    @DisplayName("测试预测排班效果-成功预测")
    void testPredictEffect_Success() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder().planId(1L).build();
        SchedulePrediction expectedResult = SchedulePrediction.builder()
                .predictionSuccessful(true)
                .predictedQuality(85.0)
                .build();

        when(schedulePredictor.predict(any(ScheduleData.class)))
                .thenReturn(expectedResult);

        // When
        SchedulePrediction result = schedulePredictionService.predictEffect(scheduleData);

        // Then
        assertNotNull(result);
        assertTrue(result.getPredictionSuccessful());
        assertEquals(85.0, result.getPredictedQuality());
        verify(schedulePredictor).predict(scheduleData);
        log.info("[测试] 成功预测测试通过");
    }

    @Test
    @DisplayName("测试预测排班效果-失败预测")
    void testPredictEffect_Failed() {
        // Given
        ScheduleData scheduleData = ScheduleData.builder().planId(1L).build();
        SchedulePrediction expectedResult = SchedulePrediction.builder()
                .predictionSuccessful(false)
                .build();

        when(schedulePredictor.predict(any(ScheduleData.class)))
                .thenReturn(expectedResult);

        // When
        SchedulePrediction result = schedulePredictionService.predictEffect(scheduleData);

        // Then
        assertNotNull(result);
        assertFalse(result.getPredictionSuccessful());
        log.info("[测试] 失败预测测试通过");
    }

    @Test
    @DisplayName("测试预测排班效果-空数据")
    void testPredictEffect_NullData() {
        // When
        SchedulePrediction result = schedulePredictionService.predictEffect(null);

        // Then
        assertNotNull(result);
        log.info("[测试] 空数据测试通过");
    }
}

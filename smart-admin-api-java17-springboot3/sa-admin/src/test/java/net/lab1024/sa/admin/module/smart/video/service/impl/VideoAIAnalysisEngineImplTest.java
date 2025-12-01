/*
 * 视频AI分析引擎实现类单元测试
 * 基于现有VideoAIAnalysisEngineImpl的单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-11-27
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.video.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.admin.module.smart.video.dao.VideoFaceFeatureDao;
import net.lab1024.sa.admin.module.smart.video.domain.vo.DetectionResultVO;
import net.lab1024.sa.admin.module.smart.video.domain.vo.FaceSearchResultVO;
import net.lab1024.sa.admin.module.smart.video.domain.vo.TrajectoryAnalysisVO;
import net.lab1024.sa.admin.module.smart.video.service.VideoAIAnalysisEngine;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.BaseTest;

/**
 * 视频AI分析引擎实现类单元测试
 * 基于现有VideoAIAnalysisEngineImpl的详细功能测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("视频AI分析引擎实现类单元测试")
class VideoAIAnalysisEngineImplTest extends BaseTest {

    @Mock
    private VideoFaceFeatureDao videoFaceFeatureDao;

    @InjectMocks
    private VideoAIAnalysisEngineImpl videoAIAnalysisEngine;

    @BeforeEach
    void setUp() {
        logTestStart("VideoAIAnalysisEngineImpl单元测试");
    }

    @Test
    @DisplayName("测试人脸检测功能")
    void testFaceDetection() {
        logTestStep("开始测试人脸检测功能");

        // 准备测试数据
        String imageUrl = "http://example.com/test_face.jpg";
        double confidence = 0.85;
        Long deviceId = 1L;

        // 执行人脸检测
        ResponseDTO<List<DetectionResultVO>> result = videoAIAnalysisEngine.detectFaces(imageUrl, confidence, deviceId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        List<DetectionResultVO> faceResults = result.getData();
        assertFalse(faceResults.isEmpty());

        // 验证检测结果结构
        DetectionResultVO faceResult = faceResults.get(0);
        assertNotNull(faceResult.getType());
        assertEquals("FACE", faceResult.getType());
        assertTrue(faceResult.getConfidence() >= confidence);
        assertNotNull(faceResult.getBoundingBox());

        logTestStep("人脸检测功能测试完成");
    }

    @Test
    @DisplayName("测试目标检测功能")
    void testObjectDetection() {
        logTestStep("开始测试目标检测功能");

        // 准备测试数据
        String imageUrl = "http://example.com/test_objects.jpg";
        double confidence = 0.75;
        List<String> targetTypes = Arrays.asList("PERSON", "VEHICLE", "ANIMAL");
        Long deviceId = 1L;

        // 执行目标检测
        ResponseDTO<List<DetectionResultVO>> result = videoAIAnalysisEngine.detectObjects(
                imageUrl, confidence, targetTypes, deviceId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        List<DetectionResultVO> objectResults = result.getData();
        assertFalse(objectResults.isEmpty());

        // 验证检测结果包含指定类型
        boolean hasTargetType = objectResults.stream()
                .anyMatch(detection -> targetTypes.contains(detection.getType()));
        assertTrue(hasTargetType);

        logTestStep("目标检测功能测试完成");
    }

    @Test
    @DisplayName("测试行为分析功能")
    void testBehaviorAnalysis() {
        logTestStep("开始测试行为分析功能");

        // 准备测试数据
        String videoUrl = "http://example.com/test_behavior.mp4";
        List<String> behaviorTypes = Arrays.asList("WALKING", "RUNNING", "FALLING", "FIGHTING");
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 27, 10, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 27, 10, 5, 0);
        Long deviceId = 1L;

        // 执行行为分析
        ResponseDTO<Map<String, Object>> result = videoAIAnalysisEngine.analyzeBehavior(
                videoUrl, behaviorTypes, startTime, endTime, deviceId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        Map<String, Object> analysisResult = result.getData();
        assertTrue(analysisResult.containsKey("behaviors"));
        assertTrue(analysisResult.containsKey("statistics"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> behaviors = (List<Map<String, Object>>) analysisResult.get("behaviors");
        assertFalse(behaviors.isEmpty());

        // 验证行为类型匹配
        boolean hasBehaviorType = behaviors.stream()
                .anyMatch(behavior -> behaviorTypes.contains(behavior.get("type")));
        assertTrue(hasBehaviorType);

        logTestStep("行为分析功能测试完成");
    }

    @Test
    @DisplayName("测试人脸搜索功能")
    void testFaceSearch() {
        logTestStep("开始测试人脸搜索功能");

        // 准备测试数据
        String queryImage = "http://example.com/query_face.jpg";
        Long deviceId = 1L;
        double confidence = 0.8;
        int maxResults = 10;
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 27, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 27, 23, 59, 59);

        // 模拟数据库返回
        List<Map<String, Object>> mockFaceFeatures = Arrays.asList(
                Map.of("faceId", 1L, "deviceId", 1L, "featureData", "feature_data_1"),
                Map.of("faceId", 2L, "deviceId", 1L, "featureData", "feature_data_2")
        );
        when(videoFaceFeatureDao.selectByDeviceIdAndTimeRange(deviceId, startTime, endTime))
                .thenReturn(mockFaceFeatures);

        // 执行人脸搜索
        ResponseDTO<List<FaceSearchResultVO>> result = videoAIAnalysisEngine.searchFaces(
                queryImage, deviceId, confidence, maxResults, startTime, endTime);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        List<FaceSearchResultVO> searchResults = result.getData();
        assertTrue(searchResults.size() <= maxResults);

        // 验证搜索结果结构
        if (!searchResults.isEmpty()) {
            FaceSearchResultVO searchResult = searchResults.get(0);
            assertNotNull(searchResult.getFaceId());
            assertNotNull(searchResult.getDeviceId());
            assertTrue(searchResult.getSimilarity() >= confidence);
            assertNotNull(searchResult.getTimestamp());
        }

        // 验证DAO调用
        verify(videoFaceFeatureDao).selectByDeviceIdAndTimeRange(deviceId, startTime, endTime);

        logTestStep("人脸搜索功能测试完成");
    }

    @Test
    @DisplayName("测试轨迹分析功能")
    void testTrajectoryAnalysis() {
        logTestStep("开始测试轨迹分析功能");

        // 准备测试数据
        String targetId = "PERSON_001";
        List<Long> deviceIds = Arrays.asList(1L, 2L, 3L);
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 27, 9, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 27, 18, 0, 0);

        // 执行轨迹分析
        ResponseDTO<TrajectoryAnalysisVO> result = videoAIAnalysisEngine.analyzeTrajectory(
                targetId, deviceIds, startTime, endTime);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        TrajectoryAnalysisVO trajectoryAnalysis = result.getData();
        assertEquals(targetId, trajectoryAnalysis.getTargetId());
        assertNotNull(trajectoryAnalysis.getTrajectoryPoints());
        assertNotNull(trajectoryAnalysis.getStatistics());

        // 验证轨迹点数据
        List<Map<String, Object>> trajectoryPoints = trajectoryAnalysis.getTrajectoryPoints();
        assertFalse(trajectoryPoints.isEmpty());

        Map<String, Object> firstPoint = trajectoryPoints.get(0);
        assertTrue(firstPoint.containsKey("deviceId"));
        assertTrue(firstPoint.containsKey("timestamp"));
        assertTrue(firstPoint.containsKey("location"));
        assertTrue(deviceIds.contains(firstPoint.get("deviceId")));

        logTestStep("轨迹分析功能测试完成");
    }

    @Test
    @DisplayName("测试车牌识别功能")
    void testLicensePlateRecognition() {
        logTestStep("开始测试车牌识别功能");

        // 准备测试数据
        String imageUrl = "http://example.com/test_license_plate.jpg";
        double confidence = 0.9;
        Long deviceId = 1L;

        // 执行车牌识别
        ResponseDTO<List<DetectionResultVO>> result = videoAIAnalysisEngine.recognizeLicensePlates(
                imageUrl, confidence, deviceId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        List<DetectionResultVO> plateResults = result.getData();
        assertFalse(plateResults.isEmpty());

        // 验证车牌识别结果结构
        DetectionResultVO plateResult = plateResults.get(0);
        assertEquals("LICENSE_PLATE", plateResult.getType());
        assertTrue(plateResult.getConfidence() >= confidence);
        assertNotNull(plateResult.getAttributes());
        assertTrue(plateResult.getAttributes().containsKey("plateNumber"));
        assertTrue(plateResult.getAttributes().containsKey("plateType"));

        logTestStep("车牌识别功能测试完成");
    }

    @Test
    @DisplayName("测试实时分析功能")
    void testRealTimeAnalysis() {
        logTestStep("开始测试实时分析功能");

        // 准备测试数据
        Long deviceId = 1L;
        String streamUrl = "rtsp://192.168.1.100:554/live";
        List<String> analysisTypes = Arrays.asList("FACE_DETECTION", "OBJECT_DETECTION", "BEHAVIOR_ANALYSIS");
        Map<String, Object> parameters = Map.of(
                "faceConfidence", 0.8,
                "objectConfidence", 0.75,
                "behaviorTypes", Arrays.asList("WALKING", "RUNNING")
        );

        // 启动实时分析
        ResponseDTO<String> startResult = videoAIAnalysisEngine.startRealTimeAnalysis(
                deviceId, streamUrl, analysisTypes, parameters);

        // 验证启动结果
        assertNotNull(startResult);
        assertTrue(startResult.getOk());
        assertNotNull(startResult.getData());

        String analysisTaskId = startResult.getData();
        assertFalse(analysisTaskId.isEmpty());

        // 获取实时分析状态
        ResponseDTO<Map<String, Object>> statusResult = videoAIAnalysisEngine.getRealTimeAnalysisStatus(
                deviceId);

        // 验证状态结果
        assertNotNull(statusResult);
        assertTrue(statusResult.getOk());
        assertNotNull(statusResult.getData());

        Map<String, Object> status = statusResult.getData();
        assertTrue(status.containsKey("taskId"));
        assertTrue(status.containsKey("status"));
        assertTrue(status.containsKey("startTime"));

        // 停止实时分析
        ResponseDTO<Boolean> stopResult = videoAIAnalysisEngine.stopRealTimeAnalysis(deviceId);

        // 验证停止结果
        assertNotNull(stopResult);
        assertTrue(stopResult.getOk());
        assertTrue(stopResult.getData());

        logTestStep("实时分析功能测试完成");
    }

    @Test
    @DisplayName("测试批量分析功能")
    void testBatchAnalysis() {
        logTestStep("开始测试批量分析功能");

        // 准备测试数据
        List<String> imageUrls = Arrays.asList(
                "http://example.com/batch_image_1.jpg",
                "http://example.com/batch_image_2.jpg",
                "http://example.com/batch_image_3.jpg"
        );
        String analysisType = "FACE_DETECTION";
        double confidence = 0.8;
        Map<String, Object> parameters = Map.of("maxFaces", 5);

        // 执行批量分析
        ResponseDTO<Map<String, Object>> result = videoAIAnalysisEngine.batchAnalyze(
                imageUrls, analysisType, confidence, parameters);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        Map<String, Object> batchResult = result.getData();
        assertTrue(batchResult.containsKey("taskId"));
        assertTrue(batchResult.containsKey("totalImages"));
        assertTrue(batchResult.containsKey("processedImages"));
        assertTrue(batchResult.containsKey("results"));

        assertEquals(imageUrls.size(), batchResult.get("totalImages"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) batchResult.get("results");
        assertEquals(imageUrls.size(), results.size());

        // 验证每个图片的分析结果
        for (Map<String, Object> imageResult : results) {
            assertTrue(imageResult.containsKey("imageUrl"));
            assertTrue(imageResult.containsKey("detections"));
            assertTrue(imageResult.containsKey("processingTime"));
        }

        logTestStep("批量分析功能测试完成");
    }

    @Test
    @DisplayName("测试分析统计功能")
    void testAnalysisStatistics() {
        logTestStep("开始测试分析统计功能");

        // 准备测试数据
        Long deviceId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2025, 11, 27, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 11, 27, 23, 59, 59);
        List<String> analysisTypes = Arrays.asList("FACE_DETECTION", "OBJECT_DETECTION", "BEHAVIOR_ANALYSIS");

        // 获取分析统计
        ResponseDTO<Map<String, Object>> result = videoAIAnalysisEngine.getAnalysisStatistics(
                deviceId, startTime, endTime, analysisTypes);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());

        Map<String, Object> statistics = result.getData();
        assertTrue(statistics.containsKey("deviceId"));
        assertTrue(statistics.containsKey("timeRange"));
        assertTrue(statistics.containsKey("totalAnalyses"));
        assertTrue(statistics.containsKey("successCount"));
        assertTrue(statistics.containsKey("errorCount"));
        assertTrue(statistics.containsKey("averageProcessingTime"));

        assertEquals(deviceId, statistics.get("deviceId"));

        @SuppressWarnings("unchecked")
        Map<String, Object> typeStatistics = (Map<String, Object>) statistics.get("typeStatistics");
        for (String analysisType : analysisTypes) {
            assertTrue(typeStatistics.containsKey(analysisType));
        }

        logTestStep("分析统计功能测试完成");
    }

    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        logTestStep("开始测试错误处理");

        // 测试空图片URL
        ResponseDTO<List<DetectionResultVO>> result1 = videoAIAnalysisEngine.detectFaces(
                null, 0.8, 1L);
        assertFalse(result1.getOk());
        assertNotNull(result1.getMsg());

        // 测试无效置信度
        ResponseDTO<List<DetectionResultVO>> result2 = videoAIAnalysisEngine.detectFaces(
                "http://example.com/test.jpg", 1.5, 1L);
        assertFalse(result2.getOk());
        assertNotNull(result2.getMsg());

        // 测试空设备ID
        ResponseDTO<List<DetectionResultVO>> result3 = videoAIAnalysisEngine.detectFaces(
                "http://example.com/test.jpg", 0.8, null);
        assertFalse(result3.getOk());
        assertNotNull(result3.getMsg());

        // 测试空行为类型列表
        ResponseDTO<Map<String, Object>> result4 = videoAIAnalysisEngine.analyzeBehavior(
                "http://example.com/test.mp4", null,
                LocalDateTime.now(), LocalDateTime.now(), 1L);
        assertFalse(result4.getOk());
        assertNotNull(result4.getMsg());

        logTestStep("错误处理测试完成");
    }

    @Test
    @DisplayName("测试参数验证")
    void testParameterValidation() {
        logTestStep("开始测试参数验证");

        // 测试置信度边界值
        ResponseDTO<List<DetectionResultVO>> result1 = videoAIAnalysisEngine.detectFaces(
                "http://example.com/test.jpg", 0.0, 1L);
        assertTrue(result1.getOk()); // 0.0是有效的

        ResponseDTO<List<DetectionResultVO>> result2 = videoAIAnalysisEngine.detectFaces(
                "http://example.com/test.jpg", 1.0, 1L);
        assertTrue(result2.getOk()); // 1.0是有效的

        ResponseDTO<List<DetectionResultVO>> result3 = videoAIAnalysisEngine.detectFaces(
                "http://example.com/test.jpg", -0.1, 1L);
        assertFalse(result3.getOk()); // 负值无效

        ResponseDTO<List<DetectionResultVO>> result4 = videoAIAnalysisEngine.detectFaces(
                "http://example.com/test.jpg", 1.1, 1L);
        assertFalse(result4.getOk()); // 大于1.0无效

        logTestStep("参数验证测试完成");
    }
}
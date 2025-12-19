package net.lab1024.sa.video.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.manager.BehaviorDetectionManager;
import net.lab1024.sa.video.manager.CrowdAnalysisManager;
import net.lab1024.sa.video.manager.FaceRecognitionManager;
import net.lab1024.sa.video.service.VideoAiAnalysisService;

/**
 * 视频AI分析控制器
 * <p>
 * 提供完整的视频AI智能分析API接口
 * 包括人脸识别、行为检测、人群分析、实时分析等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/ai-analysis")
@RequiredArgsConstructor
@Tag(name = "视频AI分析", description = "视频AI智能分析相关接口")
@Validated
public class VideoAiAnalysisController {

    private final VideoAiAnalysisService videoAiAnalysisService;

    // ==================== 人脸识别相关接口 ====================

    @PostMapping("/face/detect")
    @Operation(summary = "人脸检测", description = "检测图像中的人脸信息")
    public ResponseDTO<List<FaceRecognitionManager.FaceDetectResult>> detectFaces(
            @Parameter(description = "图像文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            byte[] imageData = file.getBytes();
            List<FaceRecognitionManager.FaceDetectResult> results = videoAiAnalysisService.detectFaces(imageData);
            return ResponseDTO.ok(results);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸检测失败", e);
            return ResponseDTO.error("FACE_DETECT_ERROR", "人脸检测失败: " + e.getMessage());
        }
    }

    @PostMapping("/face/compare")
    @Operation(summary = "人脸比对", description = "比对待识别人脸与人脸库中的人脸")
    public ResponseDTO<Double> compareFaces(
            @Parameter(description = "人脸图像1", required = true) @RequestParam("file1") MultipartFile file1,
            @Parameter(description = "人脸图像2", required = true) @RequestParam("file2") MultipartFile file2) {
        try {
            byte[] faceImage1 = file1.getBytes();
            byte[] faceImage2 = file2.getBytes();
            byte[] feature1 = videoAiAnalysisService.extractFaceFeature(faceImage1);
            byte[] feature2 = videoAiAnalysisService.extractFaceFeature(faceImage2);
            double similarity = videoAiAnalysisService.compareFaces(feature1, feature2);
            return ResponseDTO.ok(similarity);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸比对失败", e);
            return ResponseDTO.error("FACE_COMPARE_ERROR", "人脸比对失败: " + e.getMessage());
        }
    }

    @PostMapping("/face/search")
    @Operation(summary = "人脸搜索", description = "在人脸库中搜索相似人脸")
    public ResponseDTO<List<FaceRecognitionManager.FaceMatchResult>> searchFaces(
            @Parameter(description = "人脸图像", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "相似度阈值") @RequestParam(defaultValue = "0.7") double threshold) {
        try {
            byte[] faceImage = file.getBytes();
            byte[] feature = videoAiAnalysisService.extractFaceFeature(faceImage);
            List<FaceRecognitionManager.FaceMatchResult> results = videoAiAnalysisService.searchFaces(feature,
                    threshold);
            return ResponseDTO.ok(results);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸搜索失败", e);
            return ResponseDTO.error("FACE_SEARCH_ERROR", "人脸搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/{cameraId}")
    @Operation(summary = "获取摄像头分析统计", description = "获取指定摄像头的分析统计信息")
    public ResponseDTO<VideoAiAnalysisService.CameraAnalysisStatistics> getCameraAnalysisStatistics(
            @Parameter(description = "摄像头ID", required = true) @PathVariable @NotNull String cameraId,
            @Parameter(description = "统计时间范围（分钟）") @RequestParam(defaultValue = "60") int minutes) {
        try {
            VideoAiAnalysisService.CameraAnalysisStatistics statistics = videoAiAnalysisService
                    .getCameraAnalysisStatistics(
                            cameraId, minutes);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[视频AI分析] 获取摄像头分析统计失败，cameraId={}", cameraId, e);
            return ResponseDTO.error("GET_CAMERA_STATISTICS_ERROR", "获取摄像头分析统计失败: " + e.getMessage());
        }
    }

    // ==================== 行为检测相关接口 ====================

    @PostMapping("/behavior/detect-fall")
    @Operation(summary = "跌倒检测", description = "检测图像中是否有人员跌倒")
    public ResponseDTO<VideoAiAnalysisService.FallDetectionResult> detectFall(
            @Parameter(description = "跌倒检测请求", required = true) @Valid @RequestBody VideoAiAnalysisService.FallDetectionRequest request) {
        try {
            BehaviorDetectionManager.FallDetectionResult managerResult = videoAiAnalysisService.detectFall(
                    request.getCameraId(), request.getImageData());
            // 转换为Service内部类类型
            VideoAiAnalysisService.FallDetectionResult result = new VideoAiAnalysisService.FallDetectionResult();
            result.setDetected(managerResult.isDetected());
            result.setConfidence(managerResult.getConfidence());
            result.setLocation(managerResult.getLocation());
            result.setTimestamp(managerResult.getTimestamp());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频AI分析] 跌倒检测失败", e);
            return ResponseDTO.error("FALL_DETECTION_ERROR", "跌倒检测失败: " + e.getMessage());
        }
    }

    @PostMapping("/behavior/analyze")
    @Operation(summary = "行为分析", description = "分析图像中的异常行为")
    public ResponseDTO<List<BehaviorDetectionManager.AbnormalBehavior>> analyzeBehavior(
            @Parameter(description = "行为分析请求", required = true) @Valid @RequestBody VideoAiAnalysisService.BehaviorAnalysisRequest request) {
        try {
            // 当前Service接口未提供 analyzeBehavior：这里使用 detectAbnormalBehaviors 作为最小可交付实现
            List<BehaviorDetectionManager.AbnormalBehavior> result = videoAiAnalysisService.detectAbnormalBehaviors(
                    request.getCameraId(), request.getImageData());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频AI分析] 行为分析失败", e);
            return ResponseDTO.error("BEHAVIOR_ANALYSIS_ERROR", "行为分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/behavior/statistics/{cameraId}")
    @Operation(summary = "获取行为统计", description = "获取指定摄像头的行为分析统计信息")
    public ResponseDTO<VideoAiAnalysisService.BehaviorStatistics> getBehaviorStatistics(
            @Parameter(description = "摄像头ID", required = true) @PathVariable @NotNull String cameraId,
            @Parameter(description = "统计时间范围（分钟）") @RequestParam(defaultValue = "60") int minutes) {
        try {
            VideoAiAnalysisService.BehaviorStatistics statistics = videoAiAnalysisService
                    .getBehaviorStatistics(cameraId, minutes);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[视频AI分析] 获取行为统计失败，cameraId={}", cameraId, e);
            return ResponseDTO.error("GET_BEHAVIOR_STATISTICS_ERROR", "获取行为统计失败: " + e.getMessage());
        }
    }

    // ==================== 人群分析相关接口 ====================

    @PostMapping("/crowd/density")
    @Operation(summary = "人流密度计算", description = "计算指定区域的人流密度")
    public ResponseDTO<VideoAiAnalysisService.DensityResult> calculateDensity(
            @Parameter(description = "人流密度计算请求", required = true) @Valid @RequestBody VideoAiAnalysisService.DensityCalculationRequest request) {
        try {
            CrowdAnalysisManager.DensityResult managerResult = videoAiAnalysisService.calculateDensity(
                    request.getCameraId(), request.getImageData());
            // 转换为Service内部类类型
            VideoAiAnalysisService.DensityResult result = new VideoAiAnalysisService.DensityResult();
            result.setDensity(managerResult.getDensity());
            result.setCount(managerResult.getCount());
            result.setArea(managerResult.getArea());
            result.setLevel(managerResult.getLevel());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频AI分析] 人流密度计算失败", e);
            return ResponseDTO.error("DENSITY_CALCULATION_ERROR", "人流密度计算失败: " + e.getMessage());
        }
    }

    @PostMapping("/crowd/heatmap")
    @Operation(summary = "生成热力图", description = "基于人流数据生成热力图")
    public ResponseDTO<VideoAiAnalysisService.HeatmapData> generateHeatmap(
            @Parameter(description = "热力图生成请求", required = true) @Valid @RequestBody VideoAiAnalysisService.HeatmapRequest request) {
        try {
            CrowdAnalysisManager.HeatmapData managerHeatmap = videoAiAnalysisService.generateHeatmap(
                    request.getCameraId(), request.getWidth(), request.getHeight(), request.getGridSize());
            // 转换为Service内部类类型
            VideoAiAnalysisService.HeatmapData heatmapData = new VideoAiAnalysisService.HeatmapData();
            heatmapData.setWidth(managerHeatmap.getWidth());
            heatmapData.setHeight(managerHeatmap.getHeight());
            heatmapData.setGridSize(managerHeatmap.getGridSize());
            heatmapData.setData(managerHeatmap.getData());
            return ResponseDTO.ok(heatmapData);
        } catch (Exception e) {
            log.error("[视频AI分析] 热力图生成失败", e);
            return ResponseDTO.error("HEATMAP_GENERATION_ERROR", "热力图生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/crowd/warning/{cameraId}")
    @Operation(summary = "拥挤预警检查", description = "检查指定摄像头的拥挤预警状态")
    public ResponseDTO<VideoAiAnalysisService.CrowdWarning> checkCrowdWarning(
            @Parameter(description = "摄像头ID", required = true) @PathVariable @NotNull String cameraId) {
        try {
            CrowdAnalysisManager.CrowdWarning managerWarning = videoAiAnalysisService.checkCrowdWarning(cameraId);
            // 转换为Service内部类类型
            VideoAiAnalysisService.CrowdWarning warning = new VideoAiAnalysisService.CrowdWarning();
            warning.setCameraId(managerWarning.getCameraId());
            warning.setLevel(managerWarning.getLevel());
            warning.setMessage(managerWarning.getMessage());
            warning.setTimestamp(managerWarning.getTimestamp());
            return ResponseDTO.ok(warning);
        } catch (Exception e) {
            log.error("[视频AI分析] 拥挤预警检查失败，cameraId={}", cameraId, e);
            return ResponseDTO.error("CROWD_WARNING_CHECK_ERROR", "拥挤预警检查失败: " + e.getMessage());
        }
    }

    // ==================== 综合分析相关接口 ====================

    @PostMapping("/comprehensive")
    @Operation(summary = "综合分析", description = "对图像进行综合的AI分析，包含人脸、行为、人群等多维度分析")
    public ResponseDTO<VideoAiAnalysisService.ComprehensiveAnalysisResult> comprehensiveAnalysis(
            @Parameter(description = "综合分析请求", required = true) @Valid @RequestBody VideoAiAnalysisService.ComprehensiveAnalysisRequest request) {
        try {
            VideoAiAnalysisService.ComprehensiveAnalysisResult result = videoAiAnalysisService.comprehensiveAnalysis(
                    request.getCameraId(), request.getImageData());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频AI分析] 综合分析失败", e);
            return ResponseDTO.error("COMPREHENSIVE_ANALYSIS_ERROR", "综合分析失败: " + e.getMessage());
        }
    }

    @PostMapping("/realtime/start")
    @Operation(summary = "启动实时分析", description = "启动指定摄像头的实时AI分析")
    public ResponseDTO<String> startRealtimeAnalysis(
            @Parameter(description = "实时分析启动请求", required = true) @Valid @RequestBody VideoAiAnalysisService.RealtimeAnalysisStartRequest request) {
        try {
            String taskId = videoAiAnalysisService.startRealtimeAnalysis(
                    request.getCameraId(), request.getStreamUrl(), request.getAnalysisConfig());
            return ResponseDTO.ok(taskId);
        } catch (Exception e) {
            log.error("[视频AI分析] 启动实时分析失败", e);
            return ResponseDTO.error("START_REALTIME_ANALYSIS_ERROR", "启动实时分析失败: " + e.getMessage());
        }
    }

    @PostMapping("/realtime/stop/{taskId}")
    @Operation(summary = "停止实时分析", description = "停止指定任务ID的实时AI分析")
    public ResponseDTO<Boolean> stopRealtimeAnalysis(
            @Parameter(description = "任务ID", required = true) @PathVariable @NotNull String taskId) {
        try {
            boolean success = videoAiAnalysisService.stopRealtimeAnalysis(taskId);
            return ResponseDTO.ok(success);
        } catch (Exception e) {
            log.error("[视频AI分析] 停止实时分析失败，taskId={}", taskId, e);
            return ResponseDTO.error("STOP_REALTIME_ANALYSIS_ERROR", "停止实时分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/realtime/status/{taskId}")
    @Operation(summary = "获取实时分析状态", description = "获取指定实时分析任务的状态信息")
    public ResponseDTO<VideoAiAnalysisService.RealtimeAnalysisStatus> getRealtimeAnalysisStatus(
            @Parameter(description = "任务ID", required = true) @PathVariable @NotNull String taskId) {
        try {
            VideoAiAnalysisService.RealtimeAnalysisStatus status = videoAiAnalysisService
                    .getRealtimeAnalysisStatus(taskId);
            return ResponseDTO.ok(status);
        } catch (Exception e) {
            log.error("[视频AI分析] 获取实时分析状态失败，taskId={}", taskId, e);
            return ResponseDTO.error("GET_REALTIME_STATUS_ERROR", "获取实时分析状态失败: " + e.getMessage());
        }
    }

    // ==================== 历史视频分析相关接口 ====================

    @PostMapping("/historical/analyze")
    @Operation(summary = "历史视频分析", description = "分析指定时间段的历史视频")
    public ResponseDTO<VideoAiAnalysisService.HistoricalAnalysisResult> analyzeHistoricalVideo(
            @Parameter(description = "历史视频分析请求", required = true) @Valid @RequestBody VideoAiAnalysisService.HistoricalAnalysisRequest request) {
        try {
            VideoAiAnalysisService.HistoricalAnalysisResult result = videoAiAnalysisService.analyzeHistoricalVideo(
                    request.getVideoId(), request.getStartTime(), request.getEndTime(), request.getAnalysisTypes());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频AI分析] 历史视频分析失败", e);
            return ResponseDTO.error("HISTORICAL_ANALYSIS_ERROR", "历史视频分析失败: " + e.getMessage());
        }
    }

    // ==================== 报告生成相关接口 ====================

    @PostMapping("/report/generate")
    @Operation(summary = "生成分析报告", description = "生成指定时间段和范围的AI分析报告")
    public ResponseDTO<VideoAiAnalysisService.AnalysisReport> generateAnalysisReport(
            @Parameter(description = "报告生成请求", required = true) @Valid @RequestBody VideoAiAnalysisService.ReportGenerationRequest request) {
        try {
            VideoAiAnalysisService.AnalysisReport report = videoAiAnalysisService.generateAnalysisReport(
                    request.getCameraIds(), request.getStartTime(), request.getEndTime(), request.getReportType());
            return ResponseDTO.ok(report);
        } catch (Exception e) {
            log.error("[视频AI分析] 生成分析报告失败", e);
            return ResponseDTO.error("GENERATE_REPORT_ERROR", "生成分析报告失败: " + e.getMessage());
        }
    }

    // ==================== 便捷查询接口 ====================

    @GetMapping("/camera/{cameraId}/quick-analysis")
    @Operation(summary = "快速分析", description = "对指定摄像头进行快速的综合分析")
    public ResponseDTO<VideoAiAnalysisService.ComprehensiveAnalysisResult> quickAnalysis(
            @Parameter(description = "摄像头ID", required = true) @PathVariable @NotNull String cameraId,
            @Parameter(description = "图像文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            byte[] imageData = file.getBytes();
            VideoAiAnalysisService.ComprehensiveAnalysisResult result = videoAiAnalysisService.comprehensiveAnalysis(
                    cameraId, imageData);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频AI分析] 快速分析失败，cameraId={}", cameraId, e);
            return ResponseDTO.error("QUICK_ANALYSIS_ERROR", "快速分析失败: " + e.getMessage());
        }
    }

    @GetMapping("/camera/{cameraId}/capabilities")
    @Operation(summary = "获取AI能力", description = "获取指定摄像头支持的AI分析能力")
    public ResponseDTO<List<String>> getCameraAiCapabilities(
            @Parameter(description = "摄像头ID", required = true) @PathVariable @NotNull String cameraId) {
        try {
            List<String> capabilities = videoAiAnalysisService.getCameraAiCapabilities(cameraId);
            return ResponseDTO.ok(capabilities);
        } catch (Exception e) {
            log.error("[视频AI分析] 获取AI能力失败，cameraId={}", cameraId, e);
            return ResponseDTO.error("GET_AI_CAPABILITIES_ERROR", "获取AI能力失败: " + e.getMessage());
        }
    }

    @GetMapping("/system/status")
    @Operation(summary = "获取AI系统状态", description = "获取整个AI分析系统的运行状态")
    public ResponseDTO<VideoAiAnalysisService.AiSystemStatus> getAiSystemStatus() {
        try {
            VideoAiAnalysisService.AiSystemStatus status = videoAiAnalysisService.getAiSystemStatus();
            return ResponseDTO.ok(status);
        } catch (Exception e) {
            log.error("[视频AI分析] 获取AI系统状态失败", e);
            return ResponseDTO.error("GET_AI_SYSTEM_STATUS_ERROR", "获取AI系统状态失败: " + e.getMessage());
        }
    }
}

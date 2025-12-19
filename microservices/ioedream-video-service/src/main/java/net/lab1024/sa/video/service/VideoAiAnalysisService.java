package net.lab1024.sa.video.service;

import net.lab1024.sa.video.manager.FaceRecognitionManager;
import net.lab1024.sa.video.manager.BehaviorDetectionManager;
import net.lab1024.sa.video.manager.CrowdAnalysisManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频AI分析服务接口
 * <p>
 * 提供视频AI智能分析的业务服务接口
 * 包括人脸识别、行为检测、人群分析等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface VideoAiAnalysisService {

    // ==================== 人脸识别相关接口 ====================

    /**
     * 人脸检测
     *
     * @param imageData 图像数据
     * @return 检测到的人脸列表
     */
    List<FaceRecognitionManager.FaceDetectResult> detectFaces(byte[] imageData);

    /**
     * 提取人脸特征
     *
     * @param faceImage 人脸图像
     * @return 人脸特征向量
     */
    byte[] extractFaceFeature(byte[] faceImage);

    /**
     * 人脸比对
     *
     * @param feature1 特征1
     * @param feature2 特征2
     * @return 相似度（0-1）
     */
    double compareFaces(byte[] feature1, byte[] feature2);

    /**
     * 人脸搜索（1:N）
     *
     * @param feature   待搜索的人脸特征
     * @param threshold 相似度阈值
     * @return 匹配结果列表
     */
    List<FaceRecognitionManager.FaceMatchResult> searchFaces(byte[] feature, double threshold);

    /**
     * 注册人脸
     *
     * @param userId    用户ID
     * @param faceImage 人脸图像
     * @return 是否成功
     */
    boolean registerFace(Long userId, byte[] faceImage);

    /**
     * 黑名单检查
     *
     * @param faceImage 人脸图像
     * @return 黑名单检查结果
     */
    FaceRecognitionManager.BlacklistCheckResult checkBlacklist(byte[] faceImage);

    /**
     * 批量人脸检测
     *
     * @param imageList 图像列表
     * @return 检测结果列表
     */
    List<BatchFaceDetectionResult> batchDetectFaces(List<byte[]> imageList);

    // ==================== 行为检测相关接口 ====================

    /**
     * 徘徊检测
     *
     * @param cameraId  摄像头ID
     * @param personId  人员标识
     * @param x         位置X
     * @param y         位置Y
     * @param timestamp 时间戳
     * @return 徘徊检测结果
     */
    BehaviorDetectionManager.LoiteringResult detectLoitering(String cameraId, String personId, int x, int y,
            LocalDateTime timestamp);

    /**
     * 聚集检测
     *
     * @param cameraId        摄像头ID
     * @param personPositions 人员位置列表
     * @return 聚集检测结果
     */
    BehaviorDetectionManager.GatheringResult detectGathering(String cameraId,
            List<BehaviorDetectionManager.PersonPosition> personPositions);

    /**
     * 跌倒检测
     *
     * @param cameraId  摄像头ID
     * @param frameData 视频帧数据
     * @return 跌倒检测结果
     */
    BehaviorDetectionManager.FallDetectionResult detectFall(String cameraId, byte[] frameData);

    /**
     * 异常行为检测
     *
     * @param cameraId  摄像头ID
     * @param frameData 视频帧数据
     * @return 异常行为列表
     */
    List<BehaviorDetectionManager.AbnormalBehavior> detectAbnormalBehaviors(String cameraId, byte[] frameData);

    /**
     * 清理过期轨迹
     *
     * @param expireMinutes 过期分钟数
     */
    void cleanExpiredTracks(int expireMinutes);

    /**
     * 获取行为统计
     *
     * @param cameraId 摄像头ID
     * @param minutes  统计时间范围（分钟）
     * @return 行为统计信息
     */
    BehaviorStatistics getBehaviorStatistics(String cameraId, int minutes);

    // ==================== 人群分析相关接口 ====================

    /**
     * 计算人流密度
     *
     * @param cameraId  摄像头ID
     * @param frameData 视频帧数据
     * @return 人流密度结果
     */
    CrowdAnalysisManager.DensityResult calculateDensity(String cameraId, byte[] frameData);

    /**
     * 生成热力图数据
     *
     * @param cameraId 摄像头ID
     * @param width    宽度
     * @param height   高度
     * @param gridSize 网格大小
     * @return 热力图数据
     */
    CrowdAnalysisManager.HeatmapData generateHeatmap(String cameraId, int width, int height, int gridSize);

    /**
     * 拥挤预警检查
     *
     * @param cameraId 摄像头ID
     * @return 预警结果
     */
    CrowdAnalysisManager.CrowdWarning checkCrowdWarning(String cameraId);

    /**
     * 获取人流趋势
     *
     * @param cameraId 摄像头ID
     * @param minutes  时间范围（分钟）
     * @return 趋势数据
     */
    Map<LocalDateTime, Integer> getFlowTrend(String cameraId, int minutes);

    /**
     * 添加轨迹点
     *
     * @param cameraId 摄像头ID
     * @param x        X坐标
     * @param y        Y坐标
     */
    void addTrajectoryPoint(String cameraId, int x, int y);

    /**
     * 获取摄像头分析统计
     *
     * @param cameraId 摄像头ID
     * @param minutes  统计时间范围（分钟）
     * @return 分析统计信息
     */
    CameraAnalysisStatistics getCameraAnalysisStatistics(String cameraId, int minutes);

    /**
     * 获取摄像头支持的AI分析能力（最小可交付版本）
     * <p>
     * 说明：当前返回固定能力集合；后续可根据设备型号/配置/授权动态计算。
     * </p>
     *
     * @param cameraId 摄像头ID
     * @return AI能力列表
     */
    default List<String> getCameraAiCapabilities(String cameraId) {
        return List.of("FACE_RECOGNITION", "BEHAVIOR_ANALYSIS", "CROWD_ANALYSIS");
    }

    // ==================== 综合AI分析接口 ====================

    /**
     * 综合AI分析
     *
     * @param cameraId  �像头ID
     * @param frameData 视频帧数据
     * @return 综合分析结果
     */
    ComprehensiveAnalysisResult comprehensiveAnalysis(String cameraId, byte[] frameData);

    /**
     * 实时视频流分析
     *
     * @param cameraId       摄像头ID
     * @param streamUrl      视频流地址
     * @param analysisConfig 分析配置
     * @return 分析任务ID
     */
    String startRealtimeAnalysis(String cameraId, String streamUrl, RealtimeAnalysisConfig analysisConfig);

    /**
     * 停止实时分析
     *
     * @param analysisId 分析任务ID
     * @return 是否成功
     */
    boolean stopRealtimeAnalysis(String analysisId);

    /**
     * 获取实时分析状态
     *
     * @param analysisId 分析任务ID
     * @return 分析状态
     */
    RealtimeAnalysisStatus getRealtimeAnalysisStatus(String analysisId);

    /**
     * 历史视频分析
     *
     * @param videoId       视频ID
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param analysisTypes 分析类型
     * @return 历史分析结果
     */
    HistoricalAnalysisResult analyzeHistoricalVideo(String videoId, LocalDateTime startTime, LocalDateTime endTime,
            List<String> analysisTypes);

    /**
     * 生成AI分析报告
     *
     * @param cameraId   摄像头ID
     * @param reportType 报告类型
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分析报告
     */
    AiAnalysisReport generateAnalysisReport(String cameraId, String reportType, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 生成分析报告（多摄像头聚合版本，供Controller直接调用）
     * <p>
     * 说明：最小可交付版本默认选取第一个cameraId生成报告；后续可扩展为真正的多摄像头聚合报表。
     * </p>
     *
     * @param cameraIds  摄像头ID列表
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param reportType 报告类型
     * @return 分析报告
     */
    default AnalysisReport generateAnalysisReport(
            List<String> cameraIds,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String reportType) {
        String cameraId = (cameraIds != null && !cameraIds.isEmpty()) ? cameraIds.get(0) : null;
        AiAnalysisReport report = generateAnalysisReport(cameraId, reportType, startTime, endTime);
        AnalysisReport wrapper = new AnalysisReport();
        if (report != null) {
            wrapper.setReportId(report.getReportId());
            wrapper.setCameraId(report.getCameraId());
            wrapper.setReportType(report.getReportType());
            wrapper.setStartTime(report.getStartTime());
            wrapper.setEndTime(report.getEndTime());
            wrapper.setReportContent(report.getReportContent());
            wrapper.setStatistics(report.getStatistics());
            wrapper.setRecommendations(report.getRecommendations());
            wrapper.setGeneratedTime(report.getGeneratedTime());
        }
        return wrapper;
    }

    /**
     * 获取AI系统状态
     *
     * @return AI系统状态
     */
    AiSystemStatus getAiSystemStatus();

    // ==================== 内部类 ====================

    /**
     * 跌倒检测请求
     */
    class FallDetectionRequest {
        private String cameraId;
        private byte[] imageData;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public void setImageData(byte[] imageData) {
            this.imageData = imageData;
        }
    }

    /**
     * 跌倒检测结果
     */
    class FallDetectionResult {
        private boolean detected;
        private double confidence;
        private String location;
        private LocalDateTime timestamp;

        public boolean isDetected() {
            return detected;
        }

        public void setDetected(boolean detected) {
            this.detected = detected;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * 行为分析请求
     */
    class BehaviorAnalysisRequest {
        private String cameraId;
        private byte[] imageData;
        private List<String> analysisTypes;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public void setImageData(byte[] imageData) {
            this.imageData = imageData;
        }

        public List<String> getAnalysisTypes() {
            return analysisTypes;
        }

        public void setAnalysisTypes(List<String> analysisTypes) {
            this.analysisTypes = analysisTypes;
        }
    }

    /**
     * 密度计算请求
     */
    class DensityCalculationRequest {
        private String cameraId;
        private byte[] imageData;
        private int gridSize;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public void setImageData(byte[] imageData) {
            this.imageData = imageData;
        }

        public int getGridSize() {
            return gridSize;
        }

        public void setGridSize(int gridSize) {
            this.gridSize = gridSize;
        }
    }

    /**
     * 密度结果
     */
    class DensityResult {
        private int totalCount;
        private double density;
        private String cameraId;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public double getDensity() {
            return density;
        }

        public void setDensity(double density) {
            this.density = density;
        }

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }
    }

    /**
     * 热力图请求
     */
    class HeatmapRequest {
        private String cameraId;
        private int width;
        private int height;
        private int gridSize;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getGridSize() {
            return gridSize;
        }

        public void setGridSize(int gridSize) {
            this.gridSize = gridSize;
        }
    }

    /**
     * 热力图数据
     */
    class HeatmapData {
        private int width;
        private int height;
        private double[][] data;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public double[][] getData() {
            return data;
        }

        public void setData(double[][] data) {
            this.data = data;
        }
    }

    /**
     * 人群预警
     */
    class CrowdWarning {
        private String cameraId;
        private String level;
        private int count;
        private String message;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 综合分析请求
     */
    class ComprehensiveAnalysisRequest {
        private String cameraId;
        private byte[] imageData;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public byte[] getImageData() {
            return imageData;
        }

        public void setImageData(byte[] imageData) {
            this.imageData = imageData;
        }
    }

    /**
     * 启动实时分析请求
     */
    class RealtimeAnalysisStartRequest {
        private String cameraId;
        private String streamUrl;
        private RealtimeAnalysisConfig analysisConfig;

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public String getStreamUrl() {
            return streamUrl;
        }

        public void setStreamUrl(String streamUrl) {
            this.streamUrl = streamUrl;
        }

        public RealtimeAnalysisConfig getAnalysisConfig() {
            return analysisConfig;
        }

        public void setAnalysisConfig(RealtimeAnalysisConfig analysisConfig) {
            this.analysisConfig = analysisConfig;
        }
    }

    /**
     * 历史分析请求
     */
    class HistoricalAnalysisRequest {
        private String videoId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<String> analysisTypes;

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public List<String> getAnalysisTypes() {
            return analysisTypes;
        }

        public void setAnalysisTypes(List<String> analysisTypes) {
            this.analysisTypes = analysisTypes;
        }
    }

    /**
     * 报告生成请求
     */
    class ReportGenerationRequest {
        private List<String> cameraIds;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String reportType;

        public List<String> getCameraIds() {
            return cameraIds;
        }

        public void setCameraIds(List<String> cameraIds) {
            this.cameraIds = cameraIds;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public String getReportType() {
            return reportType;
        }

        public void setReportType(String reportType) {
            this.reportType = reportType;
        }
    }

    /**
     * 报告响应（兼容Controller使用的类型名）
     */
    class AnalysisReport extends AiAnalysisReport {
    }

    /**
     * AI系统状态
     */
    class AiSystemStatus {
        private String status;
        private int runningRealtimeTasks;
        private LocalDateTime reportTime;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getRunningRealtimeTasks() {
            return runningRealtimeTasks;
        }

        public void setRunningRealtimeTasks(int runningRealtimeTasks) {
            this.runningRealtimeTasks = runningRealtimeTasks;
        }

        public LocalDateTime getReportTime() {
            return reportTime;
        }

        public void setReportTime(LocalDateTime reportTime) {
            this.reportTime = reportTime;
        }
    }

    /**
     * 批量人脸检测结果
     */
    class BatchFaceDetectionResult {
        private int imageIndex;
        private List<FaceRecognitionManager.FaceDetectResult> faces;
        private boolean success;
        private String errorMessage;

        // getters and setters
        public int getImageIndex() {
            return imageIndex;
        }

        public void setImageIndex(int imageIndex) {
            this.imageIndex = imageIndex;
        }

        public List<FaceRecognitionManager.FaceDetectResult> getFaces() {
            return faces;
        }

        public void setFaces(List<FaceRecognitionManager.FaceDetectResult> faces) {
            this.faces = faces;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 行为统计信息
     */
    class BehaviorStatistics {
        private String cameraId;
        private int loiteringCount;
        private int gatheringCount;
        private int fallCount;
        private int abnormalBehaviorCount;
        private LocalDateTime statisticsTime;

        // getters and setters
        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public int getLoiteringCount() {
            return loiteringCount;
        }

        public void setLoiteringCount(int loiteringCount) {
            this.loiteringCount = loiteringCount;
        }

        public int getGatheringCount() {
            return gatheringCount;
        }

        public void setGatheringCount(int gatheringCount) {
            this.gatheringCount = gatheringCount;
        }

        public int getFallCount() {
            return fallCount;
        }

        public void setFallCount(int fallCount) {
            this.fallCount = fallCount;
        }

        public int getAbnormalBehaviorCount() {
            return abnormalBehaviorCount;
        }

        public void setAbnormalBehaviorCount(int abnormalBehaviorCount) {
            this.abnormalBehaviorCount = abnormalBehaviorCount;
        }

        public LocalDateTime getStatisticsTime() {
            return statisticsTime;
        }

        public void setStatisticsTime(LocalDateTime statisticsTime) {
            this.statisticsTime = statisticsTime;
        }
    }

    /**
     * 摄像头分析统计
     */
    class CameraAnalysisStatistics {
        private String cameraId;
        private int totalFrames;
        private int faceDetectionCount;
        private int behaviorDetectionCount;
        private int crowdAnalysisCount;
        private double averageProcessingTime;
        private LocalDateTime statisticsTime;

        // getters and setters
        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public int getTotalFrames() {
            return totalFrames;
        }

        public void setTotalFrames(int totalFrames) {
            this.totalFrames = totalFrames;
        }

        public int getFaceDetectionCount() {
            return faceDetectionCount;
        }

        public void setFaceDetectionCount(int faceDetectionCount) {
            this.faceDetectionCount = faceDetectionCount;
        }

        public int getBehaviorDetectionCount() {
            return behaviorDetectionCount;
        }

        public void setBehaviorDetectionCount(int behaviorDetectionCount) {
            this.behaviorDetectionCount = behaviorDetectionCount;
        }

        public int getCrowdAnalysisCount() {
            return crowdAnalysisCount;
        }

        public void setCrowdAnalysisCount(int crowdAnalysisCount) {
            this.crowdAnalysisCount = crowdAnalysisCount;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public void setAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
        }

        public LocalDateTime getStatisticsTime() {
            return statisticsTime;
        }

        public void setStatisticsTime(LocalDateTime statisticsTime) {
            this.statisticsTime = statisticsTime;
        }
    }

    /**
     * 实时分析配置
     */
    class RealtimeAnalysisConfig {
        private boolean enableFaceDetection;
        private boolean enableBehaviorDetection;
        private boolean enableCrowdAnalysis;
        private double detectionThreshold;
        private int analysisInterval;
        private boolean enableAlarm;
        private List<String> alarmTypes;

        // getters and setters
        public boolean isEnableFaceDetection() {
            return enableFaceDetection;
        }

        public void setEnableFaceDetection(boolean enableFaceDetection) {
            this.enableFaceDetection = enableFaceDetection;
        }

        public boolean isEnableBehaviorDetection() {
            return enableBehaviorDetection;
        }

        public void setEnableBehaviorDetection(boolean enableBehaviorDetection) {
            this.enableBehaviorDetection = enableBehaviorDetection;
        }

        public boolean isEnableCrowdAnalysis() {
            return enableCrowdAnalysis;
        }

        public void setEnableCrowdAnalysis(boolean enableCrowdAnalysis) {
            this.enableCrowdAnalysis = enableCrowdAnalysis;
        }

        public double getDetectionThreshold() {
            return detectionThreshold;
        }

        public void setDetectionThreshold(double detectionThreshold) {
            this.detectionThreshold = detectionThreshold;
        }

        public int getAnalysisInterval() {
            return analysisInterval;
        }

        public void setAnalysisInterval(int analysisInterval) {
            this.analysisInterval = analysisInterval;
        }

        public boolean isEnableAlarm() {
            return enableAlarm;
        }

        public void setEnableAlarm(boolean enableAlarm) {
            this.enableAlarm = enableAlarm;
        }

        public List<String> getAlarmTypes() {
            return alarmTypes;
        }

        public void setAlarmTypes(List<String> alarmTypes) {
            this.alarmTypes = alarmTypes;
        }
    }

    /**
     * 实时分析状态
     */
    class RealtimeAnalysisStatus {
        private String analysisId;
        private String cameraId;
        private String status;
        private String statusMessage;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;
        private int processedFrames;
        private AnalysisStatistics statistics;

        // getters and setters
        public String getAnalysisId() {
            return analysisId;
        }

        public void setAnalysisId(String analysisId) {
            this.analysisId = analysisId;
        }

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public int getProcessedFrames() {
            return processedFrames;
        }

        public void setProcessedFrames(int processedFrames) {
            this.processedFrames = processedFrames;
        }

        public AnalysisStatistics getStatistics() {
            return statistics;
        }

        public void setStatistics(AnalysisStatistics statistics) {
            this.statistics = statistics;
        }
    }

    /**
     * 分析统计
     */
    class AnalysisStatistics {
        private int totalProcessed;
        private int faceDetections;
        private int behaviorDetections;
        private int crowdWarnings;
        private int alarms;
        private double averageProcessingTime;

        // getters and setters
        public int getTotalProcessed() {
            return totalProcessed;
        }

        public void setTotalProcessed(int totalProcessed) {
            this.totalProcessed = totalProcessed;
        }

        public int getFaceDetections() {
            return faceDetections;
        }

        public void setFaceDetections(int faceDetections) {
            this.faceDetections = faceDetections;
        }

        public int getBehaviorDetections() {
            return behaviorDetections;
        }

        public void setBehaviorDetections(int behaviorDetections) {
            this.behaviorDetections = behaviorDetections;
        }

        public int getCrowdWarnings() {
            return crowdWarnings;
        }

        public void setCrowdWarnings(int crowdWarnings) {
            this.crowdWarnings = crowdWarnings;
        }

        public int getAlarms() {
            return alarms;
        }

        public void setAlarms(int alarms) {
            this.alarms = alarms;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public void setAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
        }
    }

    /**
     * 综合分析结果
     */
    class ComprehensiveAnalysisResult {
        private String cameraId;
        private LocalDateTime timestamp;
        private FaceAnalysisResult faceResult;
        private BehaviorAnalysisResult behaviorResult;
        private CrowdAnalysisResult crowdResult;
        private double processingTime;

        // getters and setters
        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public FaceAnalysisResult getFaceResult() {
            return faceResult;
        }

        public void setFaceResult(FaceAnalysisResult faceResult) {
            this.faceResult = faceResult;
        }

        public BehaviorAnalysisResult getBehaviorResult() {
            return behaviorResult;
        }

        public void setBehaviorResult(BehaviorAnalysisResult behaviorResult) {
            this.behaviorResult = behaviorResult;
        }

        public CrowdAnalysisResult getCrowdResult() {
            return crowdResult;
        }

        public void setCrowdResult(CrowdAnalysisResult crowdResult) {
            this.crowdResult = crowdResult;
        }

        public double getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(double processingTime) {
            this.processingTime = processingTime;
        }
    }

    /**
     * 人脸分析结果
     */
    class FaceAnalysisResult {
        private int faceCount;
        private List<FaceRecognitionManager.FaceDetectResult> faces;
        private boolean hasBlacklistMatch;
        private List<FaceRecognitionManager.BlacklistCheckResult> blacklistMatches;

        // getters and setters
        public int getFaceCount() {
            return faceCount;
        }

        public void setFaceCount(int faceCount) {
            this.faceCount = faceCount;
        }

        public List<FaceRecognitionManager.FaceDetectResult> getFaces() {
            return faces;
        }

        public void setFaces(List<FaceRecognitionManager.FaceDetectResult> faces) {
            this.faces = faces;
        }

        public boolean isHasBlacklistMatch() {
            return hasBlacklistMatch;
        }

        public void setHasBlacklistMatch(boolean hasBlacklistMatch) {
            this.hasBlacklistMatch = hasBlacklistMatch;
        }

        public List<FaceRecognitionManager.BlacklistCheckResult> getBlacklistMatches() {
            return blacklistMatches;
        }

        public void setBlacklistMatches(List<FaceRecognitionManager.BlacklistCheckResult> blacklistMatches) {
            this.blacklistMatches = blacklistMatches;
        }
    }

    /**
     * 行为分析结果
     */
    class BehaviorAnalysisResult {
        private boolean hasLoitering;
        private List<BehaviorDetectionManager.LoiteringResult> loiteringDetections;
        private boolean hasGathering;
        private List<BehaviorDetectionManager.GatheringResult> gatheringDetections;
        private boolean hasFall;
        private List<BehaviorDetectionManager.FallDetectionResult> fallDetections;
        private List<BehaviorDetectionManager.AbnormalBehavior> abnormalBehaviors;

        // getters and setters
        public boolean isHasLoitering() {
            return hasLoitering;
        }

        public void setHasLoitering(boolean hasLoitering) {
            this.hasLoitering = hasLoitering;
        }

        public List<BehaviorDetectionManager.LoiteringResult> getLoiteringDetections() {
            return loiteringDetections;
        }

        public void setLoiteringDetections(List<BehaviorDetectionManager.LoiteringResult> loiteringDetections) {
            this.loiteringDetections = loiteringDetections;
        }

        public boolean isHasGathering() {
            return hasGathering;
        }

        public void setHasGathering(boolean hasGathering) {
            this.hasGathering = hasGathering;
        }

        public List<BehaviorDetectionManager.GatheringResult> getGatheringDetections() {
            return gatheringDetections;
        }

        public void setGatheringDetections(List<BehaviorDetectionManager.GatheringResult> gatheringDetections) {
            this.gatheringDetections = gatheringDetections;
        }

        public boolean isHasFall() {
            return hasFall;
        }

        public void setHasFall(boolean hasFall) {
            this.hasFall = hasFall;
        }

        public List<BehaviorDetectionManager.FallDetectionResult> getFallDetections() {
            return fallDetections;
        }

        public void setFallDetections(List<BehaviorDetectionManager.FallDetectionResult> fallDetections) {
            this.fallDetections = fallDetections;
        }

        public List<BehaviorDetectionManager.AbnormalBehavior> getAbnormalBehaviors() {
            return abnormalBehaviors;
        }

        public void setAbnormalBehaviors(List<BehaviorDetectionManager.AbnormalBehavior> abnormalBehaviors) {
            this.abnormalBehaviors = abnormalBehaviors;
        }
    }

    /**
     * 人群分析结果
     */
    class CrowdAnalysisResult {
        private int personCount;
        private double density;
        private CrowdAnalysisManager.CrowdLevel crowdLevel;
        private CrowdAnalysisManager.CrowdWarning crowdWarning;
        private boolean hasCrowdWarning;

        // getters and setters
        public int getPersonCount() {
            return personCount;
        }

        public void setPersonCount(int personCount) {
            this.personCount = personCount;
        }

        public double getDensity() {
            return density;
        }

        public void setDensity(double density) {
            this.density = density;
        }

        public CrowdAnalysisManager.CrowdLevel getCrowdLevel() {
            return crowdLevel;
        }

        public void setCrowdLevel(CrowdAnalysisManager.CrowdLevel crowdLevel) {
            this.crowdLevel = crowdLevel;
        }

        public CrowdAnalysisManager.CrowdWarning getCrowdWarning() {
            return crowdWarning;
        }

        public void setCrowdWarning(CrowdAnalysisManager.CrowdWarning crowdWarning) {
            this.crowdWarning = crowdWarning;
        }

        public boolean isHasCrowdWarning() {
            return hasCrowdWarning;
        }

        public void setHasCrowdWarning(boolean hasCrowdWarning) {
            this.hasCrowdWarning = hasCrowdWarning;
        }
    }

    /**
     * 历史分析结果
     */
    class HistoricalAnalysisResult {
        private String videoId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Map<String, Object> analysisResults;
        private AnalysisSummary summary;

        // getters and setters
        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Map<String, Object> getAnalysisResults() {
            return analysisResults;
        }

        public void setAnalysisResults(Map<String, Object> analysisResults) {
            this.analysisResults = analysisResults;
        }

        public AnalysisSummary getSummary() {
            return summary;
        }

        public void setSummary(AnalysisSummary summary) {
            this.summary = summary;
        }
    }

    /**
     * 分析摘要
     */
    class AnalysisSummary {
        private int totalFrames;
        private Map<String, Integer> detectionCounts;
        private Map<String, Integer> eventCounts;
        private List<String> alerts;

        // getters and setters
        public int getTotalFrames() {
            return totalFrames;
        }

        public void setTotalFrames(int totalFrames) {
            this.totalFrames = totalFrames;
        }

        public Map<String, Integer> getDetectionCounts() {
            return detectionCounts;
        }

        public void setDetectionCounts(Map<String, Integer> detectionCounts) {
            this.detectionCounts = detectionCounts;
        }

        public Map<String, Integer> getEventCounts() {
            return eventCounts;
        }

        public void setEventCounts(Map<String, Integer> eventCounts) {
            this.eventCounts = eventCounts;
        }

        public List<String> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<String> alerts) {
            this.alerts = alerts;
        }
    }

    /**
     * AI分析报告
     */
    class AiAnalysisReport {
        private String reportId;
        private String cameraId;
        private String reportType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String reportContent;
        private Map<String, Object> statistics;
        private List<String> recommendations;
        private LocalDateTime generatedTime;

        // getters and setters
        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public String getCameraId() {
            return cameraId;
        }

        public void setCameraId(String cameraId) {
            this.cameraId = cameraId;
        }

        public String getReportType() {
            return reportType;
        }

        public void setReportType(String reportType) {
            this.reportType = reportType;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public String getReportContent() {
            return reportContent;
        }

        public void setReportContent(String reportContent) {
            this.reportContent = reportContent;
        }

        public Map<String, Object> getStatistics() {
            return statistics;
        }

        public void setStatistics(Map<String, Object> statistics) {
            this.statistics = statistics;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }

        public LocalDateTime getGeneratedTime() {
            return generatedTime;
        }

        public void setGeneratedTime(LocalDateTime generatedTime) {
            this.generatedTime = generatedTime;
        }
    }
}
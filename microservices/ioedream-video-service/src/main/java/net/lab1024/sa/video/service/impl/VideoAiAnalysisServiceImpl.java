package net.lab1024.sa.video.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.manager.FaceRecognitionManager;
import net.lab1024.sa.video.manager.BehaviorDetectionManager;
import net.lab1024.sa.video.manager.CrowdAnalysisManager;
import net.lab1024.sa.video.service.VideoAiAnalysisService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 视频AI分析服务实现
 * <p>
 * 提供完整的视频AI智能分析服务实现
 * 集成人脸识别、行为检测、人群分析等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RequiredArgsConstructor
public class VideoAiAnalysisServiceImpl implements VideoAiAnalysisService {

    private final FaceRecognitionManager faceRecognitionManager;
    private final BehaviorDetectionManager behaviorDetectionManager;
    private final CrowdAnalysisManager crowdAnalysisManager;

    // 实时分析任务管理
    private final Map<String, RealtimeAnalysisTask> realtimeAnalysisTasks = new ConcurrentHashMap<>();

    // ==================== 人脸识别相关实现 ====================

    @Override
    public List<FaceRecognitionManager.FaceDetectResult> detectFaces(byte[] imageData) {
        log.info("[视频AI分析] 开始人脸检测，imageSize={}", imageData.length);

        try {
            return faceRecognitionManager.detectFaces(imageData);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸检测失败，error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public byte[] extractFaceFeature(byte[] faceImage) {
        log.debug("[视频AI分析] 提取人脸特征，faceImageSize={}", faceImage.length);

        try {
            return faceRecognitionManager.extractFeature(faceImage);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸特征提取失败，error={}", e.getMessage(), e);
            return new byte[0];
        }
    }

    @Override
    public double compareFaces(byte[] feature1, byte[] feature2) {
        log.debug("[视频AI分析] 人脸比对");

        try {
            return faceRecognitionManager.compareFaces(feature1, feature2);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸比对失败，error={}", e.getMessage(), e);
            return 0.0;
        }
    }

    @Override
    public List<FaceRecognitionManager.FaceMatchResult> searchFaces(byte[] feature, double threshold) {
        log.debug("[视频AI分析] 人脸搜索，threshold={}", threshold);

        try {
            return faceRecognitionManager.searchFace(feature, threshold);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸搜索失败，error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean registerFace(Long userId, byte[] faceImage) {
        log.info("[视频AI分析] 注册人脸，userId={}", userId);

        try {
            return faceRecognitionManager.registerFace(userId, faceImage);
        } catch (Exception e) {
            log.error("[视频AI分析] 人脸注册失败，userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FaceRecognitionManager.BlacklistCheckResult checkBlacklist(byte[] faceImage) {
        log.debug("[视频AI分析] 黑名单检查");

        try {
            return faceRecognitionManager.checkBlacklist(faceImage);
        } catch (Exception e) {
            log.error("[视频AI分析] 黑名单检查失败，error={}", e.getMessage(), e);
            return new FaceRecognitionManager.BlacklistCheckResult(false, null, 0.0);
        }
    }

    @Override
    public List<BatchFaceDetectionResult> batchDetectFaces(List<byte[]> imageList) {
        log.info("[视频AI分析] 批量人脸检测，imageCount={}", imageList.size());

        List<BatchFaceDetectionResult> results = new ArrayList<>();
        for (int i = 0; i < imageList.size(); i++) {
            BatchFaceDetectionResult result = new BatchFaceDetectionResult();
            result.setImageIndex(i);

            try {
                List<FaceRecognitionManager.FaceDetectResult> faces = detectFaces(imageList.get(i));
                result.setFaces(faces);
                result.setSuccess(true);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
            }

            results.add(result);
        }

        return results;
    }

    // ==================== 行为检测相关实现 ====================

    @Override
    public BehaviorDetectionManager.LoiteringResult detectLoitering(String cameraId, String personId, int x, int y, LocalDateTime timestamp) {
        log.debug("[视频AI分析] 徘徊检测，cameraId={}, personId=({}, {})", cameraId, x, y);

        try {
            return behaviorDetectionManager.detectLoitering(cameraId, personId, x, y, timestamp);
        } catch (Exception e) {
            log.error("[视频AI分析] 徘徊检测失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new BehaviorDetectionManager.LoiteringResult(false, personId, 0, x, y);
        }
    }

    @Override
    public BehaviorDetectionManager.GatheringResult detectGathering(String cameraId, List<BehaviorDetectionManager.PersonPosition> personPositions) {
        log.debug("[视频AI分析] 聚集检测，cameraId={}, personCount={}", cameraId, personPositions.size());

        try {
            return behaviorDetectionManager.detectGathering(cameraId, personPositions);
        } catch (Exception e) {
            log.error("[视频AI分析] 聚集检测失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new BehaviorDetectionManager.GatheringResult(false, 0, 0, 0, 0);
        }
    }

    @Override
    public BehaviorDetectionManager.FallDetectionResult detectFall(String cameraId, byte[] frameData) {
        log.debug("[视频AI分析] 跌倒检测，cameraId={}", cameraId);

        try {
            return behaviorDetectionManager.detectFall(cameraId, frameData);
        } catch (Exception e) {
            log.error("[视频AI分析] 跌倒检测失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new BehaviorDetectionManager.FallDetectionResult(false, 0.0, 0, 0);
        }
    }

    @Override
    public List<BehaviorDetectionManager.AbnormalBehavior> detectAbnormalBehaviors(String cameraId, byte[] frameData) {
        log.debug("[视频AI分析] 异常行为检测，cameraId={}", cameraId);

        try {
            return behaviorDetectionManager.detectAbnormalBehaviors(cameraId, frameData);
        } catch (Exception e) {
            log.error("[视频AI分析] 异常行为检测失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void cleanExpiredTracks(int expireMinutes) {
        log.info("[视频AI分析] 清理过期轨迹，expireMinutes={}", expireMinutes);

        try {
            behaviorDetectionManager.cleanExpiredTracks(expireMinutes);
        } catch (Exception e) {
            log.error("[视频AI分析] 清理过期轨迹失败，error={}", e.getMessage(), e);
        }
    }

    @Override
    public BehaviorStatistics getBehaviorStatistics(String cameraId, int minutes) {
        log.debug("[视频AI分析] 获取行为统计，cameraId={}, minutes={}", cameraId, minutes);

        try {
            BehaviorStatistics statistics = new BehaviorStatistics();
            statistics.setCameraId(cameraId);
            statistics.setStatisticsTime(LocalDateTime.now());

            // TODO: 实现统计数据计算逻辑
            // 当前返回默认值
            statistics.setLoiteringCount(0);
            statistics.setGatheringCount(0);
            statistics.setFallCount(0);
            statistics.setAbnormalBehaviorCount(0);

            return statistics;
        } catch (Exception e) {
            log.error("[视频AI分析] 获取行为统计失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return null;
        }
    }

    // ==================== 人群分析相关实现 ====================

    @Override
    public CrowdAnalysisManager.DensityResult calculateDensity(String cameraId, byte[] frameData) {
        log.debug("[视频AI分析] 计算人流密度，cameraId={}", cameraId);

        try {
            return crowdAnalysisManager.calculateDensity(cameraId, frameData);
        } catch (Exception e) {
            log.error("[视频AI分析] 人流密度计算失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new CrowdAnalysisManager.DensityResult(cameraId, 0, 0.0, CrowdAnalysisManager.CrowdLevel.NORMAL, LocalDateTime.now());
        }
    }

    @Override
    public CrowdAnalysisManager.HeatmapData generateHeatmap(String cameraId, int width, int height, int gridSize) {
        log.debug("[视频AI分析] 生成热力图，cameraId={}, size={}x{}, gridSize={}", cameraId, width, height, gridSize);

        try {
            return crowdAnalysisManager.generateHeatmap(cameraId, width, height, gridSize);
        } catch (Exception e) {
            log.error("[视频AI分析] 热力图生成失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new CrowdAnalysisManager.HeatmapData(cameraId, new int[0][0], gridSize, LocalDateTime.now());
        }
    }

    @Override
    public CrowdAnalysisManager.CrowdWarning checkCrowdWarning(String cameraId) {
        log.debug("[视频AI分析] 拥挤预警检查，cameraId={}", cameraId);

        try {
            return crowdAnalysisManager.checkCrowdWarning(cameraId);
        } catch (Exception e) {
            log.error("[视频AI分析] 拥挤预警检查失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new CrowdAnalysisManager.CrowdWarning(false, CrowdAnalysisManager.CrowdLevel.NORMAL, 0, "正常");
        }
    }

    @Override
    public Map<LocalDateTime, Integer> getFlowTrend(String cameraId, int minutes) {
        log.debug("[视频AI分析] 获取人流趋势，cameraId={}, minutes={}", cameraId, minutes);

        try {
            return crowdAnalysisManager.getFlowTrend(cameraId, minutes);
        } catch (Exception e) {
            log.error("[视频AI分析] 人流趋势获取失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public void addTrajectoryPoint(String cameraId, int x, int y) {
        try {
            crowdAnalysisManager.addTrajectoryPoint(cameraId, x, y);
        } catch (Exception e) {
            log.error("[视频AI分析] 添加轨迹点失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
        }
    }

    @Override
    public CameraAnalysisStatistics getCameraAnalysisStatistics(String cameraId, int minutes) {
        log.debug("[视频AI分析] 获取摄像头分析统计，cameraId={}, minutes={}", cameraId, minutes);

        try {
            CameraAnalysisStatistics statistics = new CameraAnalysisStatistics();
            statistics.setCameraId(cameraId);
            statistics.setStatisticsTime(LocalDateTime.now());

            // TODO: 实现统计数据计算逻辑
            // 当前返回默认值
            statistics.setTotalFrames(0);
            statistics.setFaceDetectionCount(0);
            statistics.setBehaviorDetectionCount(0);
            statistics.setCrowdAnalysisCount(0);
            statistics.setAverageProcessingTime(0.0);

            return statistics;
        } catch (Exception e) {
            log.error("[视频AI分析] 获取摄像头分析统计失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            return null;
        }
    }

    // ==================== 综合AI分析接口实现 ====================

    @Override
    public ComprehensiveAnalysisResult comprehensiveAnalysis(String cameraId, byte[] frameData) {
        log.info("[视频AI分析] 开始综合分析，cameraId={}", cameraId);
        long startTime = System.currentTimeMillis();

        try {
            ComprehensiveAnalysisResult result = new ComprehensiveAnalysisResult();
            result.setCameraId(cameraId);
            result.setTimestamp(LocalDateTime.now());

            // 人脸分析
            FaceAnalysisResult faceResult = new FaceAnalysisResult();
            try {
                List<FaceRecognitionManager.FaceDetectResult> faces = detectFaces(frameData);
                faceResult.setFaceCount(faces.size());
                faceResult.setFaces(faces);
                faceResult.setHasBlacklistMatch(false);
                faceResult.setBlacklistMatches(new ArrayList<>());
                result.setFaceResult(faceResult);
            } catch (Exception e) {
                log.error("[视频AI分析] 人脸分析部分失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
                faceResult.setFaceCount(0);
                faceResult.setFaces(new ArrayList<>());
                faceResult.setHasBlacklistMatch(false);
                faceResult.setBlacklistMatches(new ArrayList<>());
                result.setFaceResult(faceResult);
            }

            // 行为分析
            BehaviorAnalysisResult behaviorResult = new BehaviorAnalysisResult();
            try {
                // 简化实现：只进行跌倒检测
                BehaviorDetectionManager.FallDetectionResult fallResult = detectFall(cameraId, frameData);
                behaviorResult.setHasFall(fallResult.detected());
                behaviorResult.setFallDetections(fallResult.detected() ? List.of(fallResult) : new ArrayList<>());
                behaviorResult.setHasLoitering(false);
                behaviorResult.setLoiteringDetections(new ArrayList<>());
                behaviorResult.setHasGathering(false);
                behaviorResult.setGatheringDetections(new ArrayList<>());
                behaviorResult.setAbnormalBehaviors(new ArrayList<>());
                result.setBehaviorResult(behaviorResult);
            } catch (Exception e) {
                log.error("[视频AI分析] 行为分析部分失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
                behaviorResult.setHasFall(false);
                behaviorResult.setFallDetections(new ArrayList<>());
                behaviorResult.setHasLoitering(false);
                behaviorResult.setLoiteringDetections(new ArrayList<>());
                behaviorResult.setHasGathering(false);
                behaviorResult.setGatheringDetections(new ArrayList<>());
                behaviorResult.setAbnormalBehaviors(new ArrayList<>());
                result.setBehaviorResult(behaviorResult);
            }

            // 人群分析
            CrowdAnalysisResult crowdResult = new CrowdAnalysisResult();
            try {
                CrowdAnalysisManager.DensityResult densityResult = calculateDensity(cameraId, frameData);
                crowdResult.setPersonCount(densityResult.personCount());
                crowdResult.setDensity(densityResult.density());
                crowdResult.setCrowdLevel(densityResult.level());
                crowdResult.setCrowdWarning(checkCrowdWarning(cameraId));
                crowdResult.setHasCrowdWarning(crowdResult.getCrowdWarning().warning());
                result.setCrowdResult(crowdResult);
            } catch (Exception e) {
                log.error("[视频AI分析] 人群分析部分失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
                crowdResult.setPersonCount(0);
                crowdResult.setDensity(0.0);
                crowdResult.setCrowdLevel(CrowdAnalysisManager.CrowdLevel.NORMAL);
                crowdResult.setCrowdWarning(new CrowdAnalysisManager.CrowdWarning(false, CrowdAnalysisManager.CrowdLevel.NORMAL, 0, "正常"));
                crowdResult.setHasCrowdWarning(false);
                result.setCrowdResult(crowdResult);
            }

            result.setProcessingTime(System.currentTimeMillis() - startTime);

            log.debug("[视频AI分析] 综合分析完成，cameraId={}, processingTime={}ms", cameraId, result.getProcessingTime());
            return result;
        } catch (Exception e) {
            log.error("[视频AI分析] 综合分析失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            ComprehensiveAnalysisResult result = new ComprehensiveAnalysisResult();
            result.setCameraId(cameraId);
            result.setTimestamp(LocalDateTime.now());
            result.setProcessingTime(System.currentTimeMillis() - startTime);
            return result;
        }
    }

    @Override
    public String startRealtimeAnalysis(String cameraId, String streamUrl, RealtimeAnalysisConfig analysisConfig) {
        log.info("[视频AI分析] 开始实时分析，cameraId={}, streamUrl={}", cameraId, streamUrl);

        String analysisId = UUID.randomUUID().toString();
        RealtimeAnalysisTask task = new RealtimeAnalysisTask();
        task.setAnalysisId(analysisId);
        task.setCameraId(cameraId);
        task.setStreamUrl(streamUrl);
        task.setConfig(analysisConfig);
        task.setStartTime(LocalDateTime.now());
        task.setStatus("RUNNING");
        task.setProcessedFrames(0);

        realtimeAnalysisTasks.put(analysisId, task);

        // 启动异步分析线程
        Thread analysisThread = new Thread(() -> performRealtimeAnalysis(task));
        analysisThread.setDaemon(true);
        analysisThread.start();

        log.info("[视频AI分析] 实时分析任务已启动，analysisId={}", analysisId);
        return analysisId;
    }

    @Override
    public boolean stopRealtimeAnalysis(String analysisId) {
        log.info("[视频AI分析] 停止实时分析，analysisId={}", analysisId);

        RealtimeTask task = realtimeAnalysisTasks.get(analysisId);
        if (task != null) {
            task.setStatus("STOPPED");
            realtimeAnalysisTasks.remove(analysisId);
            log.info("[视频AI分析] 实时分析任务已停止，analysisId={}", analysisId);
            return true;
        }

        log.warn("[视频AI分析] 实时分析任务不存在，analysisId={}", analysisId);
        return false;
    }

    @Override
    public RealtimeAnalysisStatus getRealtimeAnalysisStatus(String analysisId) {
        RealtimeAnalysisTask task = realtimeAnalysisTasks.get(analysisId);
        if (task != null) {
            RealtimeAnalysisStatus status = new RealtimeAnalysisStatus();
            status.setAnalysisId(task.getAnalysisId());
            status.setCameraId(task.getCameraId());
            status.setStatus(task.getStatus());
            status.setStatusMessage(getStatusMessage(task.getStatus()));
            status.setStartTime(task.getStartTime());
            status.setLastUpdateTime(LocalDateTime.now());
            status.setProcessedFrames(task.getProcessedFrames());
            status.setStatistics(task.getStatistics());

            return status;
        }

        return null;
    }

    @Override
    public HistoricalAnalysisResult analyzeHistoricalVideo(String videoId, LocalDateTime startTime, LocalDateTime endTime, List<String> analysisTypes) {
        log.info("[视频AI分析] 历史视频分析，videoId={}, startTime={}, endTime={}, types={}",
                videoId, startTime, endTime, analysisTypes);

        try {
            HistoricalAnalysisResult result = new HistoricalAnalysisResult();
            result.setVideoId(videoId);
            result.setStartTime(startTime);
            result.setEndTime(endTime);
            result.setAnalysisResults(new HashMap<>());
            result.setSummary(new AnalysisSummary());

            // TODO: 实现历史视频分析逻辑
            // 当前返回空结果
            result.setTotalFrames(0);
            result.setDetectionCounts(new HashMap<>());
            result.setEventCounts(new HashMap<>());
            result.setAlerts(new ArrayList<>());

            return result;
        } catch (Exception e) {
            log.error("[视频AI分析] 历史视频分析失败，videoId={}, error={}", videoId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AiAnalysisReport generateAnalysisReport(String cameraId, String reportType, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[视频AI分析] 生成AI分析报告，cameraId={}, reportType={}", cameraId, reportType);

        try {
            AiAnalysisReport report = new AiAnalysisReport();
            report.setReportId(UUID.randomUUID().toString());
            report.setCameraId(cameraId);
            report.setReportType(reportType);
            report.setStartTime(startTime);
            report.setEndTime(endTime);
            report.setStatistics(new HashMap<>());
            report.setRecommendations(new ArrayList<>());
            report.setGeneratedTime(LocalDateTime.now());

            // TODO: 根据报告类型生成报告内容
            String content = generateReportContent(reportType, startTime, endTime);
            report.setReportContent(content);

            return report;
        } catch (Exception e) {
            log.error("[视频AI分析] 生成AI分析报告失败，cameraId={}, reportType={}, error={}",
                    cameraId, reportType, e.getMessage(), e);
            return null;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 执行实时分析
     */
    private void performRealtimeAnalysis(RealtimeAnalysisTask task) {
        log.info("[视频AI分析] 实时分析线程启动，cameraId={}", task.getCameraId());

        try {
            while ("RUNNING".equals(task.getStatus()) && !Thread.currentThread().isInterrupted()) {
                // 模拟分析一帧
                analyzeFrame(task);

                // 根据配置的间隔等待
                Thread.sleep(task.getConfig().getAnalysisInterval() * 1000L);
            }
        } catch (InterruptedException e) {
            log.info("[视频AI分析] 实时分析线程被中断，cameraId={}", task.getCameraId());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[视频AI分析] 实时分析异常，cameraId={}, error={}", task.getCameraId(), e.getMessage(), e);
        } finally {
            task.setStatus("STOPPED");
            log.info("[视频AI分析] 实时分析线程结束，cameraId={}", task.getCameraId());
        }
    }

    /**
     * 分析单帧
     */
    private void analyzeFrame(RealtimeAnalysisTask task) {
        try {
            // 模拟获取视频帧数据
            byte[] frameData = new byte[0]; // TODO: 实际从视频流获取帧数据

            if (frameData.length == 0) {
                return;
            }

            // 更新统计
            task.setProcessedFrames(task.getProcessedFrames() + 1);
            AnalysisStatistics stats = task.getStatistics();
            if (stats == null) {
                stats = new AnalysisStatistics();
                task.setStatistics(stats);
            }
            stats.setTotalProcessed(stats.getTotalProcessed() + 1);

            // 根据配置执行分析
            RealtimeAnalysisConfig config = task.getConfig();
            if (config.isEnableFaceDetection()) {
                stats.setFaceDetections(stats.getFaceDetections() + 1);
            }
            if (config.isEnableBehaviorDetection()) {
                stats.setBehaviorDetections(stats.getBehaviorDetections() + 1);
            }
            if (config.isEnableCrowdAnalysis()) {
                stats.setCrowdWarnings(stats.getCrowdWarnings() + 1);
            }

            // 更新最后更新时间
            task.setLastUpdateTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("[视频AI分析] 分析帧失败，cameraId={}, error={}", task.getCameraId(), e.getMessage(), e);
        }
    }

    /**
     * 获取状态消息
     */
    private String getStatusMessage(String status) {
        return switch (status) {
            case "RUNNING" -> "正在运行";
            case "STOPPED" -> "已停止";
            case "ERROR" -> "发生错误";
            default -> "未知状态";
        };
    }

    /**
     * 生成报告内容
     */
    private String generateReportContent(String reportType, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder content = new StringBuilder();
        content.append("# AI分析报告\n\n");
        content.append("报告类型: ").append(reportType).append("\n");
        content.append("分析时间范围: ").append(startTime).append(" 至 ").append(endTime).append("\n\n");
        content.append("## 检测统计\n");
        content.append("- 人脸检测: 0 次\n");
        content.append("- 行为检测: 0 次\n");
        content.append("- 人群分析: 0 次\n\n");
        content.append("## 建议\n");
        content.append("- 建议优化检测算法以提高准确率\n");
        content.append("- 建议增加更多分析维度\n");

        return content.toString();
    }

    // ==================== 内部类 ====================

    /**
     * 实时分析任务
     */
    private static class RealtimeAnalysisTask {
        private String analysisId;
        private String cameraId;
        private String streamUrl;
        private RealtimeAnalysisConfig config;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;
        private int processedFrames;
        private AnalysisStatistics statistics;

        // getters and setters
        public String getAnalysisId() { return analysisId; }
        public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
        public String getCameraId() { return cameraId; }
        public void setCameraId(String cameraId) { this.cameraId = cameraId; }
        public String getStreamUrl() { return streamUrl; }
        public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
        public RealtimeAnalysisConfig getConfig() { return config; }
        public void setConfig(RealtimeAnalysisConfig config) { this.config = config; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        public int getProcessedFrames() { return processedFrames; }
        public void setProcessedFrames(int processedFrames) { this.processedFrames = processedFrames; }
        public AnalysisStatistics getStatistics() { return statistics; }
        public void setStatistics(AnalysisStatistics statistics) { this.statistics = statistics; }
    }
}
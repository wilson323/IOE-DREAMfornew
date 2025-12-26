package net.lab1024.sa.video.service;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * 视频AI模型优化服务
 * <p>
 * 核心功能：
 * 1. 入侵检测优化（YOLO算法参数调整）
 * 2. 徘徊检测优化（轨迹分析算法）
 * 3. 检测准确率统计
 * 4. 误报率分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface VideoAIModelOptimizationService {

    /**
     * 优化入侵检测模型
     * <p>
     * 优化目标：
     * - 检测准确率 ≥95%
     * - 误报率 <5%
     * - 检测速度 <200ms/帧
     * </p>
     *
     * @param deviceId 设备ID
     * @return 优化结果
     */
    IntrusionDetectionOptimizationResult optimizeIntrusionDetection(String deviceId);

    /**
     * 优化徘徊检测模型
     * <p>
     * 优化目标：
     * - 检测准确率 ≥90%
     * - 轨迹分析精度 >80%
     * - 时间窗口检测准确率 >85%
     * </p>
     *
     * @param deviceId 设备ID
     * @return 优化结果
     */
    LoiteringDetectionOptimizationResult optimizeLoiteringDetection(String deviceId);

    /**
     * 获取检测准确率统计
     *
     * @param deviceId 设备ID
     * @param detectionType 检测类型（intrusion/loitering）
     * @param date 日期（YYYY-MM-DD）
     * @return 准确率统计
     */
    DetectionAccuracyStats getDetectionAccuracy(String deviceId, String detectionType, String date);

    /**
     * 分析入侵检测误报原因
     *
     * @param deviceId 设备ID
     * @param falsePositiveEvents 误报事件列表
     * @return 误报分析报告
     */
    FalsePositiveAnalysis analyzeIntrusionFalsePositives(String deviceId, List<Object> falsePositiveEvents);

    /**
     * 分析徘徊检测误报原因
     *
     * @param deviceId 设备ID
     * @param falsePositiveEvents 误报事件列表
     * @return 误报分析报告
     */
    FalsePositiveAnalysis analyzeLoiteringFalsePositives(String deviceId, List<Object> falsePositiveEvents);

    /**
     * 入侵检测优化结果
     */
    class IntrusionDetectionOptimizationResult {
        private String deviceId;
        private boolean optimized;
        private double originalAccuracy;
        private double optimizedAccuracy;
        private double accuracyImprovement;
        private double originalFalsePositiveRate;
        private double optimizedFalsePositiveRate;
        private double falsePositiveReduction;
        private Map<String, Object> optimizedParameters;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public boolean isOptimized() { return optimized; }
        public void setOptimized(boolean optimized) { this.optimized = optimized; }
        public double getOriginalAccuracy() { return originalAccuracy; }
        public void setOriginalAccuracy(double originalAccuracy) { this.originalAccuracy = originalAccuracy; }
        public double getOptimizedAccuracy() { return optimizedAccuracy; }
        public void setOptimizedAccuracy(double optimizedAccuracy) { this.optimizedAccuracy = optimizedAccuracy; }
        public double getAccuracyImprovement() { return accuracyImprovement; }
        public void setAccuracyImprovement(double accuracyImprovement) { this.accuracyImprovement = accuracyImprovement; }
        public double getOriginalFalsePositiveRate() { return originalFalsePositiveRate; }
        public void setOriginalFalsePositiveRate(double originalFalsePositiveRate) { this.originalFalsePositiveRate = originalFalsePositiveRate; }
        public double getOptimizedFalsePositiveRate() { return optimizedFalsePositiveRate; }
        public void setOptimizedFalsePositiveRate(double optimizedFalsePositiveRate) { this.optimizedFalsePositiveRate = optimizedFalsePositiveRate; }
        public double getFalsePositiveReduction() { return falsePositiveReduction; }
        public void setFalsePositiveReduction(double falsePositiveReduction) { this.falsePositiveReduction = falsePositiveReduction; }
        public Map<String, Object> getOptimizedParameters() { return optimizedParameters; }
        public void setOptimizedParameters(Map<String, Object> optimizedParameters) { this.optimizedParameters = optimizedParameters; }
    }

    /**
     * 徘徊检测优化结果
     */
    class LoiteringDetectionOptimizationResult {
        private String deviceId;
        private boolean optimized;
        private double originalAccuracy;
        private double optimizedAccuracy;
        private double accuracyImprovement;
        private double trajectoryAccuracy;
        private double timeWindowAccuracy;
        private Map<String, Object> optimizedParameters;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public boolean isOptimized() { return optimized; }
        public void setOptimized(boolean optimized) { this.optimized = optimized; }
        public double getOriginalAccuracy() { return originalAccuracy; }
        public void setOriginalAccuracy(double originalAccuracy) { this.originalAccuracy = originalAccuracy; }
        public double getOptimizedAccuracy() { return optimizedAccuracy; }
        public void setOptimizedAccuracy(double optimizedAccuracy) { this.optimizedAccuracy = optimizedAccuracy; }
        public double getAccuracyImprovement() { return accuracyImprovement; }
        public void setAccuracyImprovement(double accuracyImprovement) { this.accuracyImprovement = accuracyImprovement; }
        public double getTrajectoryAccuracy() { return trajectoryAccuracy; }
        public void setTrajectoryAccuracy(double trajectoryAccuracy) { this.trajectoryAccuracy = trajectoryAccuracy; }
        public double getTimeWindowAccuracy() { return timeWindowAccuracy; }
        public void setTimeWindowAccuracy(double timeWindowAccuracy) { this.timeWindowAccuracy = timeWindowAccuracy; }
        public Map<String, Object> getOptimizedParameters() { return optimizedParameters; }
        public void setOptimizedParameters(Map<String, Object> optimizedParameters) { this.optimizedParameters = optimizedParameters; }
    }

    /**
     * 检测准确率统计
     */
    class DetectionAccuracyStats {
        private String deviceId;
        private String detectionType;
        private String date;
        private int totalCount;
        private int truePositive;
        private int trueNegative;
        private int falsePositive;
        private int falseNegative;
        private double accuracy;
        private double precision;
        private double recall;
        private double f1Score;
        private double falsePositiveRate;
        private boolean metTarget;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDetectionType() { return detectionType; }
        public void setDetectionType(String detectionType) { this.detectionType = detectionType; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getTruePositive() { return truePositive; }
        public void setTruePositive(int truePositive) { this.truePositive = truePositive; }
        public int getTrueNegative() { return trueNegative; }
        public void setTrueNegative(int trueNegative) { this.trueNegative = trueNegative; }
        public int getFalsePositive() { return falsePositive; }
        public void setFalsePositive(int falsePositive) { this.falsePositive = falsePositive; }
        public int getFalseNegative() { return falseNegative; }
        public void setFalseNegative(int falseNegative) { this.falseNegative = falseNegative; }
        public double getAccuracy() { return accuracy; }
        public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
        public double getPrecision() { return precision; }
        public void setPrecision(double precision) { this.precision = precision; }
        public double getRecall() { return recall; }
        public void setRecall(double recall) { this.recall = recall; }
        public double getF1Score() { return f1Score; }
        public void setF1Score(double f1Score) { this.f1Score = f1Score; }
        public double getFalsePositiveRate() { return falsePositiveRate; }
        public void setFalsePositiveRate(double falsePositiveRate) { this.falsePositiveRate = falsePositiveRate; }
        public boolean isMetTarget() { return metTarget; }
        public void setMetTarget(boolean metTarget) { this.metTarget = metTarget; }
    }

    /**
     * 误报分析报告
     */
    class FalsePositiveAnalysis {
        private String deviceId;
        private String detectionType;
        private int totalFalsePositives;
        private Map<String, Integer> falsePositiveReasons;
        private List<String> recommendations;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDetectionType() { return detectionType; }
        public void setDetectionType(String detectionType) { this.detectionType = detectionType; }
        public int getTotalFalsePositives() { return totalFalsePositives; }
        public void setTotalFalsePositives(int totalFalsePositives) { this.totalFalsePositives = totalFalsePositives; }
        public Map<String, Integer> getFalsePositiveReasons() { return falsePositiveReasons; }
        public void setFalsePositiveReasons(Map<String, Integer> falsePositiveReasons) { this.falsePositiveReasons = falsePositiveReasons; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
}

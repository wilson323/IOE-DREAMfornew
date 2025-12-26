package net.lab1024.sa.video.manager;

import net.lab1024.sa.video.entity.VideoBehaviorEntity;
import net.lab1024.sa.video.entity.VideoBehaviorPatternEntity;
import net.lab1024.sa.video.dao.VideoBehaviorDao;
import net.lab1024.sa.video.dao.VideoBehaviorPatternDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 视频行为分析管理器
 * 处理行为检测、模式分析、告警处理等业务编排
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
public class VideoBehaviorManager {

    private final VideoBehaviorDao videoBehaviorDao;
    private final VideoBehaviorPatternDao videoBehaviorPatternDao;

    public VideoBehaviorManager(VideoBehaviorDao videoBehaviorDao, VideoBehaviorPatternDao videoBehaviorPatternDao) {
        this.videoBehaviorDao = videoBehaviorDao;
        this.videoBehaviorPatternDao = videoBehaviorPatternDao;
    }

    // ==================== 行为检测管理 ====================

    /**
     * 保存行为检测结果
     */
    public VideoBehaviorEntity saveBehaviorDetection(VideoBehaviorEntity behaviorEntity) {
        // 设置默认值
        if (behaviorEntity.getProcessStatus() == null) {
            behaviorEntity.setProcessStatus(0); // 未处理
        }
        if (behaviorEntity.getAlarmTriggered() == null) {
            behaviorEntity.setAlarmTriggered(0); // 未触发告警
        }
        if (behaviorEntity.getNeedManualConfirm() == null) {
            behaviorEntity.setNeedManualConfirm(0); // 不需要人工确认
        }
        if (behaviorEntity.getSeverityLevel() == null) {
            behaviorEntity.setSeverityLevel(2); // 中风险
        }

        // 根据置信度和行为类型确定是否需要人工确认
        determineNeedManualConfirm(behaviorEntity);

        // 插入行为检测记录
        videoBehaviorDao.insert(behaviorEntity);
        return behaviorEntity;
    }

    /**
     * 批量保存行为检测结果
     */
    public List<VideoBehaviorEntity> batchSaveBehaviorDetections(List<VideoBehaviorEntity> behaviorEntities) {
        List<VideoBehaviorEntity> savedEntities = new ArrayList<>();

        for (VideoBehaviorEntity entity : behaviorEntities) {
            try {
                VideoBehaviorEntity saved = saveBehaviorDetection(entity);
                savedEntities.add(saved);
            } catch (Exception e) {
                // 记录错误但继续处理其他记录
                System.err.println("保存行为检测记录失败: " + e.getMessage());
            }
        }

        return savedEntities;
    }

    /**
     * 更新行为处理状态
     */
    public boolean updateBehaviorProcessStatus(Long behaviorId, Integer processStatus, Long userId, String userName, String remark) {
        int result = videoBehaviorDao.updateProcessStatus(behaviorId, processStatus, userId, userName, remark);
        return result > 0;
    }

    /**
     * 批量更新行为处理状态
     */
    public int batchUpdateBehaviorProcessStatus(List<Long> behaviorIds, Integer processStatus, Long userId, String userName) {
        if (behaviorIds == null || behaviorIds.isEmpty()) {
            return 0;
        }

        String idsStr = behaviorIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return videoBehaviorDao.batchUpdateProcessStatus(idsStr, processStatus, userId, userName);
    }

    /**
     * 获取未处理的告警记录
     */
    public List<VideoBehaviorEntity> getUnprocessedAlarms() {
        return videoBehaviorDao.selectUnprocessedAlarms();
    }

    /**
     * 获取需要人工确认的记录
     */
    public List<VideoBehaviorEntity> getNeedingManualConfirm() {
        return videoBehaviorDao.selectNeedingManualConfirm();
    }

    /**
     * 获取高风险行为记录
     */
    public List<VideoBehaviorEntity> getHighRiskBehaviors() {
        return videoBehaviorDao.selectHighRiskBehaviors();
    }

    /**
     * 获取设备行为记录
     */
    public List<VideoBehaviorEntity> getDeviceBehaviors(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return videoBehaviorDao.selectByDeviceIdAndTimeRange(deviceId, startTime, endTime);
    }

    /**
     * 获取人员行为记录
     */
    public List<VideoBehaviorEntity> getPersonBehaviors(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        List<VideoBehaviorEntity> allBehaviors = videoBehaviorDao.selectByPersonId(personId);

        // 按时间过滤
        return allBehaviors.stream()
                .filter(behavior -> {
                    if (startTime != null && behavior.getDetectionTime().isBefore(startTime)) {
                        return false;
                    }
                    if (endTime != null && behavior.getDetectionTime().isAfter(endTime)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取行为统计分析数据
     */
    public Map<String, Object> getBehaviorStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // 按行为类型统计
        List<Map<String, Object>> typeStats = videoBehaviorDao.countByBehaviorType();

        // 按严重程度统计
        List<Map<String, Object>> severityStats = videoBehaviorDao.countBySeverityLevel();

        // 按告警级别统计
        List<Map<String, Object>> alarmStats = videoBehaviorDao.countByAlarmLevel();

        // 按处理状态统计
        List<Map<String, Object>> processStats = videoBehaviorDao.countByProcessStatus();

        // 按设备统计
        List<Map<String, Object>> deviceStats = videoBehaviorDao.countByDevice();

        // 按人员统计
        List<Map<String, Object>> personStats = videoBehaviorDao.countByPerson();

        return Map.of(
                "typeStatistics", typeStats,
                "severityStatistics", severityStats,
                "alarmStatistics", alarmStats,
                "processStatistics", processStats,
                "deviceStatistics", deviceStats,
                "personStatistics", personStats
        );
    }

    /**
     * 获取异常行为模式分析
     */
    public List<Map<String, Object>> getAbnormalBehaviorPatterns() {
        return videoBehaviorDao.selectAbnormalBehaviorPatterns();
    }

    /**
     * 获取行为发生频率统计
     */
    public List<Map<String, Object>> getBehaviorFrequency() {
        return videoBehaviorDao.selectBehaviorFrequency();
    }

    /**
     * 获取处理时效分析
     */
    public List<Map<String, Object>> getProcessEfficiency() {
        return videoBehaviorDao.selectProcessEfficiency();
    }

    /**
     * 清理历史行为记录
     */
    public int cleanOldBehaviorRecords(LocalDateTime cutoffTime) {
        return videoBehaviorDao.cleanOldBehaviors(cutoffTime);
    }

    // ==================== 行为模式管理 ====================

    /**
     * 添加行为模式
     */
    public VideoBehaviorPatternEntity addBehaviorPattern(VideoBehaviorPatternEntity patternEntity) {
        // 设置默认值
        if (patternEntity.getPatternStatus() == null) {
            patternEntity.setPatternStatus(1); // 启用
        }
        if (patternEntity.getPatternPriority() == null) {
            patternEntity.setPatternPriority(2); // 中优先级
        }
        if (patternEntity.getTrainingIntervalDays() == null) {
            patternEntity.setTrainingIntervalDays(30); // 30天
        }

        // 计算下次训练时间
        if (patternEntity.getLastTrainingTime() != null && patternEntity.getTrainingIntervalDays() != null) {
            LocalDateTime nextTraining = patternEntity.getLastTrainingTime().plusDays(patternEntity.getTrainingIntervalDays());
            patternEntity.setNextTrainingTime(nextTraining);
        }

        videoBehaviorPatternDao.insert(patternEntity);
        return patternEntity;
    }

    /**
     * 更新行为模式
     */
    public VideoBehaviorPatternEntity updateBehaviorPattern(VideoBehaviorPatternEntity patternEntity) {
        VideoBehaviorPatternEntity existingPattern = videoBehaviorPatternDao.selectById(patternEntity.getPatternId());
        if (existingPattern == null) {
            throw new RuntimeException("行为模式不存在");
        }

        videoBehaviorPatternDao.updateById(patternEntity);
        return patternEntity;
    }

    /**
     * 更新模式训练信息
     */
    public boolean updatePatternTrainingInfo(Long patternId, LocalDateTime trainingTime, Double trainingAccuracy,
                                              Double validationAccuracy, Double falsePositiveRate, Double falseNegativeRate,
                                              Long trainingSamples, LocalDateTime nextTrainingTime, String version) {
        int result = videoBehaviorPatternDao.updateTrainingInfo(
                patternId, trainingTime, trainingAccuracy, validationAccuracy,
                falsePositiveRate, falseNegativeRate, trainingSamples, nextTrainingTime, version);
        return result > 0;
    }

    /**
     * 获取启用中的模式
     */
    public List<VideoBehaviorPatternEntity> getActivePatterns() {
        return videoBehaviorPatternDao.selectActivePatterns();
    }

    /**
     * 获取需要重新训练的模式
     */
    public List<VideoBehaviorPatternEntity> getPatternsNeedingRetraining() {
        return videoBehaviorPatternDao.selectPatternsNeedingRetraining(LocalDateTime.now());
    }

    /**
     * 获取已过期的模式
     */
    public List<VideoBehaviorPatternEntity> getExpiredPatterns() {
        return videoBehaviorPatternDao.selectExpiredPatterns(LocalDateTime.now());
    }

    /**
     * 获取模式性能指标
     */
    public List<Map<String, Object>> getPatternPerformanceMetrics() {
        return videoBehaviorPatternDao.selectPatternPerformanceMetrics();
    }

    /**
     * 获取模式训练计划
     */
    public List<Map<String, Object>> getTrainingPlan() {
        return videoBehaviorPatternDao.selectTrainingPlan();
    }

    /**
     * 获取需要维护的模式
     */
    public List<VideoBehaviorPatternEntity> getPatternsNeedingMaintenance(Double minAccuracy, Double maxFalsePositiveRate) {
        return videoBehaviorPatternDao.selectPatternsNeedingMaintenance(minAccuracy, maxFalsePositiveRate);
    }

    /**
     * 更新模式状态
     */
    public boolean updatePatternStatus(Long patternId, Integer patternStatus) {
        int result = videoBehaviorPatternDao.updatePatternStatus(patternId, patternStatus);
        return result > 0;
    }

    /**
     * 批量更新模式状态
     */
    public int batchUpdatePatternStatus(List<Long> patternIds, Integer patternStatus) {
        if (patternIds == null || patternIds.isEmpty()) {
            return 0;
        }

        String idsStr = patternIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return videoBehaviorPatternDao.batchUpdatePatternStatus(idsStr, patternStatus);
    }

    /**
     * 更新模式性能指标
     */
    public boolean updatePatternPerformanceMetrics(Long patternId, String performanceMetrics) {
        int result = videoBehaviorPatternDao.updatePerformanceMetrics(patternId, performanceMetrics);
        return result > 0;
    }

    /**
     * 更新模式使用统计
     */
    public boolean updatePatternUsageStatistics(Long patternId, String usageStatistics) {
        int result = videoBehaviorPatternDao.updateUsageStatistics(patternId, usageStatistics);
        return result > 0;
    }

    /**
     * 清理过期模式
     */
    public int cleanExpiredPatterns() {
        return videoBehaviorPatternDao.cleanExpiredPatterns(LocalDateTime.now());
    }

    // ==================== 行为分析和预测 ====================

    /**
     * 分析行为模式
     */
    public Map<String, Object> analyzeBehaviorPatterns(List<VideoBehaviorEntity> behaviors) {
        // TODO: 实现行为模式分析算法
        // 可以使用机器学习算法识别行为模式

        Map<String, Object> analysisResult = Map.of(
                "totalBehaviors", behaviors.size(),
                "patternTypes", Arrays.asList("时间模式", "空间模式", "行为模式", "异常模式"),
                "detectedPatterns", new ArrayList<>(),
                "recommendations", new ArrayList<>()
        );

        return analysisResult;
    }

    /**
     * 预测未来行为趋势
     */
    public Map<String, Object> predictBehaviorTrend(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: 实现行为趋势预测算法
        // 基于历史数据预测未来行为

        Map<String, Object> predictionResult = Map.of(
                "predictionPeriod", startDate + " to " + endDate,
                "trend", "上升",
                "confidence", 0.85,
                "predictedBehaviors", new ArrayList<>()
        );

        return predictionResult;
    }

    /**
     * 生成行为报告
     */
    public Map<String, Object> generateBehaviorReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = getBehaviorStatistics(startTime, endTime);
        List<Map<String, Object>> abnormalPatterns = getAbnormalBehaviorPatterns();
        List<Map<String, Object>> behaviorFrequency = getBehaviorFrequency();

        return Map.of(
                "reportPeriod", startTime + " to " + endTime,
                "statistics", statistics,
                "abnormalPatterns", abnormalPatterns,
                "behaviorFrequency", behaviorFrequency,
                "riskAssessment", "中等风险",
                "recommendations", Arrays.asList("加强监控", "优化模式识别", "提升告警准确性")
        );
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否需要人工确认
     */
    private void determineNeedManualConfirm(VideoBehaviorEntity behaviorEntity) {
        // 高风险或低置信度的行为需要人工确认
        if (behaviorEntity.getSeverityLevel() != null && behaviorEntity.getSeverityLevel() >= 3) {
            behaviorEntity.setNeedManualConfirm(1);
            return;
        }

        if (behaviorEntity.getConfidenceScore() != null && behaviorEntity.getConfidenceScore().compareTo(new BigDecimal("60")) < 0) {
            behaviorEntity.setNeedManualConfirm(1);
            return;
        }

        // 特定行为类型需要人工确认
        Integer behaviorType = behaviorEntity.getBehaviorType();
        if (behaviorType != null && (behaviorType == 5 || behaviorType == 7)) { // 异常行为或其他行为
            behaviorEntity.setNeedManualConfirm(1);
        }
    }

    /**
     * 根据置信度计算风险等级
     */
    private Integer calculateRiskLevel(BigDecimal confidenceScore, Integer severityLevel) {
        if (confidenceScore == null || severityLevel == null) {
            return 3; // 默认中等风险
        }

        // 综合置信度和严重程度计算风险等级
        double confidence = confidenceScore.doubleValue();
        int severity = severityLevel;

        if (confidence >= 90 && severity <= 2) {
            return 1; // 低风险
        } else if (confidence >= 75 && severity <= 3) {
            return 2; // 中风险
        } else if (confidence >= 60 && severity <= 4) {
            return 3; // 高风险
        } else {
            return 4; // 极高风险
        }
    }

    /**
     * 生成处理建议
     */
    private List<String> generateProcessingRecommendations(VideoBehaviorEntity behavior) {
        List<String> recommendations = new ArrayList<>();

        Integer severityLevel = behavior.getSeverityLevel();
        BigDecimal confidenceScore = behavior.getConfidenceScore();
        Integer behaviorType = behavior.getBehaviorType();

        // 根据严重程度生成建议
        if (severityLevel != null && severityLevel >= 3) {
            recommendations.add("立即处理，优先级高");
        }

        // 根据置信度生成建议
        if (confidenceScore != null && confidenceScore.compareTo(new BigDecimal("70")) < 0) {
            recommendations.add("建议人工复核检测结果");
        }

        // 根据行为类型生成建议
        if (behaviorType != null) {
            switch (behaviorType) {
                case 5: // 异常行为
                    recommendations.add("加强该区域监控");
                    recommendations.add("通知安保人员关注");
                    break;
                case 1: // 人员检测
                    recommendations.add("记录人员活动轨迹");
                    break;
                case 2: // 车辆检测
                    recommendations.add("记录车辆进出记录");
                    break;
                default:
                    recommendations.add("按标准流程处理");
            }
        }

        return recommendations;
    }
}


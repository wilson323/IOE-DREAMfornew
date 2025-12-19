package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoBehaviorAnalysisForm;
import net.lab1024.sa.video.domain.form.VideoBehaviorPatternForm;
import net.lab1024.sa.video.domain.vo.VideoBehaviorVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频行为分析服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
public interface VideoBehaviorService {

    /**
     * 查询行为检测记录
     */
    PageResult<VideoBehaviorVO> queryBehaviorPage(VideoBehaviorAnalysisForm form);

    /**
     * 根据ID查询行为详情
     */
    ResponseDTO<VideoBehaviorVO> getBehaviorById(Long behaviorId);

    /**
     * 获取行为统计数据
     */
    ResponseDTO<Map<String, Object>> getBehaviorStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取设备行为记录
     */
    ResponseDTO<List<VideoBehaviorVO>> getDeviceBehaviors(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取人员行为记录
     */
    ResponseDTO<List<VideoBehaviorVO>> getPersonBehaviors(Long personId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 处理行为记录
     */
    ResponseDTO<Void> processBehavior(Long behaviorId, Integer processStatus, Long userId, String userName, String remark);

    /**
     * 批量处理行为记录
     */
    ResponseDTO<Integer> batchProcessBehaviors(List<Long> behaviorIds, Integer processStatus, Long userId, String userName);

    /**
     * 获取未处理告警
     */
    ResponseDTO<List<VideoBehaviorVO>> getUnprocessedAlarms();

    /**
     * 获取需要人工确认的记录
     */
    ResponseDTO<List<VideoBehaviorVO>> getNeedingManualConfirm();

    /**
     * 获取高风险行为
     */
    ResponseDTO<List<VideoBehaviorVO>> getHighRiskBehaviors();

    /**
     * 获取异常行为模式
     */
    ResponseDTO<List<Map<String, Object>>> getAbnormalBehaviorPatterns();

    /**
     * 获取行为频率统计
     */
    ResponseDTO<List<Map<String, Object>>> getBehaviorFrequency();

    /**
     * 获取处理时效分析
     */
    ResponseDTO<List<Map<String, Object>>> getProcessEfficiency();

    /**
     * 生成行为分析报告
     */
    ResponseDTO<Map<String, Object>> generateBehaviorReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 预测行为趋势
     */
    ResponseDTO<Map<String, Object>> predictBehaviorTrend(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 分析行为模式
     */
    ResponseDTO<Map<String, Object>> analyzeBehaviorPatterns(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 行为模式管理 ====================

    /**
     * 创建行为模式
     */
    ResponseDTO<Void> createBehaviorPattern(VideoBehaviorPatternForm form);

    /**
     * 更新行为模式
     */
    ResponseDTO<Void> updateBehaviorPattern(Long patternId, VideoBehaviorPatternForm form);

    /**
     * 删除行为模式
     */
    ResponseDTO<Void> deleteBehaviorPattern(Long patternId);

    /**
     * 根据ID查询行为模式
     */
    ResponseDTO<VideoBehaviorPatternForm> getBehaviorPatternById(Long patternId);

    /**
     * 查询行为模式列表
     */
    ResponseDTO<PageResult<VideoBehaviorPatternForm>> queryBehaviorPatternPage(VideoBehaviorPatternForm form, PageParam pageParam);

    /**
     * 获取启用中的模式
     */
    ResponseDTO<List<VideoBehaviorPatternForm>> getActivePatterns();

    /**
     * 更新模式状态
     */
    ResponseDTO<Void> updatePatternStatus(Long patternId, Integer status);

    /**
     * 批量更新模式状态
     */
    ResponseDTO<Integer> batchUpdatePatternStatus(List<Long> patternIds, Integer status);

    /**
     * 获取需要重新训练的模式
     */
    ResponseDTO<List<VideoBehaviorPatternForm>> getPatternsNeedingRetraining();

    /**
     * 获取已过期的模式
     */
    ResponseDTO<List<VideoBehaviorPatternForm>> getExpiredPatterns();

    /**
     * 更新模式训练信息
     */
    ResponseDTO<Void> updatePatternTrainingInfo(Long patternId, LocalDateTime trainingTime, Double trainingAccuracy,
                                                     Double validationAccuracy, Double falsePositiveRate, Double falseNegativeRate,
                                                     Long trainingSamples, LocalDateTime nextTrainingTime, String version);

    /**
     * 获取模式性能指标
     */
    ResponseDTO<List<Map<String, Object>>> getPatternPerformanceMetrics();

    /**
     * 获取模式训练计划
     */
    ResponseDTO<List<Map<String, Object>>> getTrainingPlan();

    /**
     * 获取需要维护的模式
     */
    ResponseDTO<List<VideoBehaviorPatternForm>> getPatternsNeedingMaintenance(Double minAccuracy, Double maxFalsePositiveRate);

    /**
     * 更新模式性能指标
     */
    ResponseDTO<Void> updatePatternPerformanceMetrics(Long patternId, String performanceMetrics);

    /**
     * 更新模式使用统计
     */
    ResponseDTO<Void> updatePatternUsageStatistics(Long patternId, String usageStatistics);

    /**
     * 清理过期模式
     */
    ResponseDTO<Integer> cleanExpiredPatterns();

    /**
     * 获取模式版本分布
     */
    ResponseDTO<List<Map<String, Object>>> getVersionDistribution();

    /**
     * 获取算法模型使用统计
     */
    ResponseDTO<List<Map<String, Object>>> getAlgorithmModelUsage();

    // ==================== AI智能分析 ====================

    /**
     * 实时行为检测
     */
    ResponseDTO<VideoBehaviorVO> detectBehavior(Long deviceId, String videoStreamUrl, Map<String, Object> parameters);

    /**
     * 历史视频行为分析
     */
    ResponseDTO<List<VideoBehaviorVO>> analyzeVideoBehavior(Long deviceId, String videoFilePath, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量行为分析
     */
    ResponseDTO<List<VideoBehaviorVO>> batchAnalyzeBehaviors(List<Long> deviceIds, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 异常行为识别
     */
    ResponseDTO<List<VideoBehaviorVO>> identifyAbnormalBehaviors(Long deviceId, LocalDateTime startTime, LocalDateTime endTime, Double confidenceThreshold);

    /**
     * 行为模式匹配
     */
    ResponseDTO<List<Map<String, Object>>> matchBehaviorPatterns(Long deviceId, List<Long> behaviorIds);

    /**
     * 自定义行为检测
     */
    ResponseDTO<VideoBehaviorVO> customBehaviorDetection(Long deviceId, Map<String, Object> customRules, Map<String, Object> thresholds);

    // ==================== 导出和报表 ====================

    /**
     * 导出行为数据
     */
    ResponseDTO<String> exportBehaviorData(VideoBehaviorAnalysisForm form, String exportFormat);

    /**
     * 导出行为报告
     */
    ResponseDTO<String> exportBehaviorReport(LocalDateTime startTime, LocalDateTime endTime, String reportType);

    /**
     * 生成行为图表数据
     */
    ResponseDTO<Map<String, Object>> generateBehaviorChartData(VideoBehaviorAnalysisForm form, List<String> chartTypes);

    /**
     * 创建行为分析任务
     */
    ResponseDTO<String> createAnalysisTask(VideoBehaviorAnalysisForm form, String taskName);

    /**
     * 获取分析任务状态
     */
    ResponseDTO<Map<String, Object>> getAnalysisTaskStatus(String taskId);

    /**
     * 取消分析任务
     */
    ResponseDTO<Void> cancelAnalysisTask(String taskId);

    // ==================== 告警和通知 ====================

    /**
     * 发送行为告警
     */
    ResponseDTO<Void> sendBehaviorAlarm(Long behaviorId, List<String> alarmTypes, Integer alarmLevel);

    /**
     * 获取行为告警列表
     */
    ResponseDTO<PageResult<VideoBehaviorVO>> getBehaviorAlarms(VideoBehaviorAnalysisForm form, PageParam pageParam);

    /**
     * 配置行为告警规则
     */
    ResponseDTO<Void> configureAlarmRules(Map<String, Object> alarmRules);

    /**
     * 获取告警规则配置
     */
    ResponseDTO<Map<String, Object>> getAlarmRulesConfiguration();

    /**
     * 测试行为告警
     */
    ResponseDTO<Void> testBehaviorAlarm(Long deviceId, Map<String, Object> testParameters);

    // ==================== 数据清理和维护 ====================

    /**
     * 清理历史行为记录
     */
    ResponseDTO<Integer> cleanOldBehaviorRecords(LocalDateTime cutoffTime);

    /**
     * 备份行为数据
     */
    ResponseDTO<String> backupBehaviorData(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 恢复行为数据
     */
    ResponseDTO<Void> restoreBehaviorData(String backupFilePath);

    /**
     * 数据完整性检查
     */
    ResponseDTO<Map<String, Object>> checkDataIntegrity();

    /**
     * 重建行为索引
     */
    ResponseDTO<Void> rebuildBehaviorIndex();

    /**
     * 优化行为数据
     */
    ResponseDTO<Map<String, Object>> optimizeBehaviorData();
}
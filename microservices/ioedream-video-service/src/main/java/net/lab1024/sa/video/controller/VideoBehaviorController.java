package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.page.PageParam;
import net.lab1024.sa.common.page.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoBehaviorAnalysisForm;
import net.lab1024.sa.video.domain.form.VideoBehaviorPatternForm;
import net.lab1024.sa.video.domain.vo.VideoBehaviorVO;
import net.lab1024.sa.video.service.VideoBehaviorService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频行为分析控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/behavior")
@Tag(name = "视频行为分析", description = "视频行为检测、模式分析、异常识别等功能")
public class VideoBehaviorController {

    @Resource
    private VideoBehaviorService videoBehaviorService;

    // ==================== 行为检测记录管理 ====================

    @Operation(summary = "查询行为检测记录")
    @PostMapping("/queryPage")
    public ResponseDTO<PageResult<VideoBehaviorVO>> queryBehaviorPage(@Valid @RequestBody VideoBehaviorAnalysisForm form) {
        return videoBehaviorService.queryBehaviorPage(form);
    }

    @Operation(summary = "根据ID查询行为详情")
    @GetMapping("/{behaviorId}")
    public ResponseDTO<VideoBehaviorVO> getBehaviorById(
            @Parameter(description = "行为ID", required = true) @PathVariable Long behaviorId) {
        return videoBehaviorService.getBehaviorById(behaviorId);
    }

    @Operation(summary = "获取行为统计数据")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getBehaviorStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        return videoBehaviorService.getBehaviorStatistics(startTime, endTime);
    }

    @Operation(summary = "获取设备行为记录")
    @GetMapping("/device/{deviceId}")
    public ResponseDTO<List<VideoBehaviorVO>> getDeviceBehaviors(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        return videoBehaviorService.getDeviceBehaviors(deviceId, startTime, endTime);
    }

    @Operation(summary = "获取人员行为记录")
    @GetMapping("/person/{personId}")
    public ResponseDTO<List<VideoBehaviorVO>> getPersonBehaviors(
            @Parameter(description = "人员ID", required = true) @PathVariable Long personId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {
        return videoBehaviorService.getPersonBehaviors(personId, startTime, endTime);
    }

    @Operation(summary = "处理行为记录")
    @PutMapping("/{behaviorId}/process")
    public ResponseDTO<Void> processBehavior(
            @Parameter(description = "行为ID", required = true) @PathVariable Long behaviorId,
            @Parameter(description = "处理状态", required = true) @RequestParam Integer processStatus,
            @Parameter(description = "处理人ID", required = true) @RequestParam Long userId,
            @Parameter(description = "处理人姓名", required = true) @RequestParam String userName,
            @Parameter(description = "处理备注") @RequestParam(required = false) String remark) {
        return videoBehaviorService.processBehavior(behaviorId, processStatus, userId, userName, remark);
    }

    @Operation(summary = "批量处理行为记录")
    @PutMapping("/batchProcess")
    public ResponseDTO<Integer> batchProcessBehaviors(
            @Parameter(description = "行为ID列表", required = true) @RequestParam List<Long> behaviorIds,
            @Parameter(description = "处理状态", required = true) @RequestParam Integer processStatus,
            @Parameter(description = "处理人ID", required = true) @RequestParam Long userId,
            @Parameter(description = "处理人姓名", required = true) @RequestParam String userName) {
        return videoBehaviorService.batchProcessBehaviors(behaviorIds, processStatus, userId, userName);
    }

    @Operation(summary = "获取未处理告警")
    @GetMapping("/alarms/unprocessed")
    public ResponseDTO<List<VideoBehaviorVO>> getUnprocessedAlarms() {
        return videoBehaviorService.getUnprocessedAlarms();
    }

    @Operation(summary = "获取需要人工确认的记录")
    @GetMapping("/manualConfirm")
    public ResponseDTO<List<VideoBehaviorVO>> getNeedingManualConfirm() {
        return videoBehaviorService.getNeedingManualConfirm();
    }

    @Operation(summary = "获取高风险行为")
    @GetMapping("/highRisk")
    public ResponseDTO<List<VideoBehaviorVO>> getHighRiskBehaviors() {
        return videoBehaviorService.getHighRiskBehaviors();
    }

    @Operation(summary = "获取异常行为模式")
    @GetMapping("/patterns/abnormal")
    public ResponseDTO<List<Map<String, Object>>> getAbnormalBehaviorPatterns() {
        return videoBehaviorService.getAbnormalBehaviorPatterns();
    }

    @Operation(summary = "获取行为频率统计")
    @GetMapping("/frequency")
    public ResponseDTO<List<Map<String, Object>>> getBehaviorFrequency() {
        return videoBehaviorService.getBehaviorFrequency();
    }

    @Operation(summary = "获取处理时效分析")
    @GetMapping("/efficiency")
    public ResponseDTO<List<Map<String, Object>>> getProcessEfficiency() {
        return videoBehaviorService.getProcessEfficiency();
    }

    @Operation(summary = "生成行为分析报告")
    @GetMapping("/report/generate")
    public ResponseDTO<Map<String, Object>> generateBehaviorReport(
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        return videoBehaviorService.generateBehaviorReport(startTime, endTime);
    }

    @Operation(summary = "预测行为趋势")
    @GetMapping("/trend/predict")
    public ResponseDTO<Map<String, Object>> predictBehaviorTrend(
            @Parameter(description = "预测开始时间", required = true) @RequestParam LocalDateTime startDate,
            @Parameter(description = "预测结束时间", required = true) @RequestParam LocalDateTime endDate) {
        return videoBehaviorService.predictBehaviorTrend(startDate, endDate);
    }

    @Operation(summary = "分析行为模式")
    @GetMapping("/pattern/analyze")
    public ResponseDTO<Map<String, Object>> analyzeBehaviorPatterns(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        return videoBehaviorService.analyzeBehaviorPatterns(deviceId, startTime, endTime);
    }

    // ==================== 行为模式管理 ====================

    @Operation(summary = "创建行为模式")
    @PostMapping("/pattern/create")
    public ResponseDTO<Void> createBehaviorPattern(@Valid @RequestBody VideoBehaviorPatternForm form) {
        return videoBehaviorService.createBehaviorPattern(form);
    }

    @Operation(summary = "更新行为模式")
    @PutMapping("/pattern/{patternId}")
    public ResponseDTO<Void> updateBehaviorPattern(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId,
            @Valid @RequestBody VideoBehaviorPatternForm form) {
        return videoBehaviorService.updateBehaviorPattern(patternId, form);
    }

    @Operation(summary = "删除行为模式")
    @DeleteMapping("/pattern/{patternId}")
    public ResponseDTO<Void> deleteBehaviorPattern(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId) {
        return videoBehaviorService.deleteBehaviorPattern(patternId);
    }

    @Operation(summary = "根据ID查询行为模式")
    @GetMapping("/pattern/{patternId}")
    public ResponseDTO<VideoBehaviorPatternForm> getBehaviorPatternById(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId) {
        return videoBehaviorService.getBehaviorPatternById(patternId);
    }

    @Operation(summary = "查询行为模式列表")
    @PostMapping("/pattern/queryPage")
    public ResponseDTO<PageResult<VideoBehaviorPatternForm>> queryBehaviorPatternPage(
            @Valid @RequestBody VideoBehaviorPatternForm form,
            PageParam pageParam) {
        return videoBehaviorService.queryBehaviorPatternPage(form, pageParam);
    }

    @Operation(summary = "获取启用中的模式")
    @GetMapping("/pattern/active")
    public ResponseDTO<List<VideoBehaviorPatternForm>> getActivePatterns() {
        return videoBehaviorService.getActivePatterns();
    }

    @Operation(summary = "更新模式状态")
    @PutMapping("/pattern/{patternId}/status")
    public ResponseDTO<Void> updatePatternStatus(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId,
            @Parameter(description = "状态", required = true) @RequestParam Integer status) {
        return videoBehaviorService.updatePatternStatus(patternId, status);
    }

    @Operation(summary = "批量更新模式状态")
    @PutMapping("/pattern/batchStatus")
    public ResponseDTO<Integer> batchUpdatePatternStatus(
            @Parameter(description = "模式ID列表", required = true) @RequestParam List<Long> patternIds,
            @Parameter(description = "状态", required = true) @RequestParam Integer status) {
        return videoBehaviorService.batchUpdatePatternStatus(patternIds, status);
    }

    @Operation(summary = "获取需要重新训练的模式")
    @GetMapping("/pattern/retrain")
    public ResponseDTO<List<VideoBehaviorPatternForm>> getPatternsNeedingRetraining() {
        return videoBehaviorService.getPatternsNeedingRetraining();
    }

    @Operation(summary = "获取已过期的模式")
    @GetMapping("/pattern/expired")
    public ResponseDTO<List<VideoBehaviorPatternForm>> getExpiredPatterns() {
        return videoBehaviorService.getExpiredPatterns();
    }

    @Operation(summary = "更新模式训练信息")
    @PutMapping("/pattern/{patternId}/training")
    public ResponseDTO<Void> updatePatternTrainingInfo(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId,
            @Parameter(description = "训练时间") @RequestParam(required = false) LocalDateTime trainingTime,
            @Parameter(description = "训练准确率") @RequestParam(required = false) Double trainingAccuracy,
            @Parameter(description = "验证准确率") @RequestParam(required = false) Double validationAccuracy,
            @Parameter(description = "误报率") @RequestParam(required = false) Double falsePositiveRate,
            @Parameter(description = "漏报率") @RequestParam(required = false) Double falseNegativeRate,
            @Parameter(description = "训练样本数") @RequestParam(required = false) Long trainingSamples,
            @Parameter(description = "下次训练时间") @RequestParam(required = false) LocalDateTime nextTrainingTime,
            @Parameter(description = "版本号") @RequestParam(required = false) String version) {
        return videoBehaviorService.updatePatternTrainingInfo(patternId, trainingTime, trainingAccuracy,
                validationAccuracy, falsePositiveRate, falseNegativeRate,
                trainingSamples, nextTrainingTime, version);
    }

    @Operation(summary = "获取模式性能指标")
    @GetMapping("/pattern/performance")
    public ResponseDTO<List<Map<String, Object>>> getPatternPerformanceMetrics() {
        return videoBehaviorService.getPatternPerformanceMetrics();
    }

    @Operation(summary = "获取模式训练计划")
    @GetMapping("/pattern/training/plan")
    public ResponseDTO<List<Map<String, Object>>> getTrainingPlan() {
        return videoBehaviorService.getTrainingPlan();
    }

    @Operation(summary = "获取需要维护的模式")
    @GetMapping("/pattern/maintenance")
    public ResponseDTO<List<VideoBehaviorPatternForm>> getPatternsNeedingMaintenance(
            @Parameter(description = "最低准确率", required = true) @RequestParam Double minAccuracy,
            @Parameter(description = "最高误报率", required = true) @RequestParam Double maxFalsePositiveRate) {
        return videoBehaviorService.getPatternsNeedingMaintenance(minAccuracy, maxFalsePositiveRate);
    }

    @Operation(summary = "更新模式性能指标")
    @PutMapping("/pattern/{patternId}/performance")
    public ResponseDTO<Void> updatePatternPerformanceMetrics(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId,
            @Parameter(description = "性能指标", required = true) @RequestParam String performanceMetrics) {
        return videoBehaviorService.updatePatternPerformanceMetrics(patternId, performanceMetrics);
    }

    @Operation(summary = "更新模式使用统计")
    @PutMapping("/pattern/{patternId}/usage")
    public ResponseDTO<Void> updatePatternUsageStatistics(
            @Parameter(description = "模式ID", required = true) @PathVariable Long patternId,
            @Parameter(description = "使用统计", required = true) @RequestParam String usageStatistics) {
        return videoBehaviorService.updatePatternUsageStatistics(patternId, usageStatistics);
    }

    @Operation(summary = "清理过期模式")
    @DeleteMapping("/pattern/clean")
    public ResponseDTO<Integer> cleanExpiredPatterns() {
        return videoBehaviorService.cleanExpiredPatterns();
    }

    @Operation(summary = "获取模式版本分布")
    @GetMapping("/pattern/versions")
    public ResponseDTO<List<Map<String, Object>>> getVersionDistribution() {
        return videoBehaviorService.getVersionDistribution();
    }

    @Operation(summary = "获取算法模型使用统计")
    @GetMapping("/pattern/algorithms")
    public ResponseDTO<List<Map<String, Object>>> getAlgorithmModelUsage() {
        return videoBehaviorService.getAlgorithmModelUsage();
    }

    // ==================== AI智能分析 ====================

    @Operation(summary = "实时行为检测")
    @PostMapping("/detect")
    public ResponseDTO<VideoBehaviorVO> detectBehavior(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "视频流URL", required = true) @RequestParam String videoStreamUrl,
            @Parameter(description = "检测参数") @RequestParam(required = false) Map<String, Object> parameters) {
        return videoBehaviorService.detectBehavior(deviceId, videoStreamUrl, parameters);
    }

    @Operation(summary = "历史视频行为分析")
    @PostMapping("/analyze/video")
    public ResponseDTO<List<VideoBehaviorVO>> analyzeVideoBehavior(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "视频文件路径", required = true) @RequestParam String videoFilePath,
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        return videoBehaviorService.analyzeVideoBehavior(deviceId, videoFilePath, startTime, endTime);
    }

    @Operation(summary = "批量行为分析")
    @PostMapping("/analyze/batch")
    public ResponseDTO<List<VideoBehaviorVO>> batchAnalyzeBehaviors(
            @Parameter(description = "设备ID列表", required = true) @RequestParam List<Long> deviceIds,
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        return videoBehaviorService.batchAnalyzeBehaviors(deviceIds, startTime, endTime);
    }

    @Operation(summary = "异常行为识别")
    @PostMapping("/identify/abnormal")
    public ResponseDTO<List<VideoBehaviorVO>> identifyAbnormalBehaviors(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime,
            @Parameter(description = "置信度阈值") @RequestParam(required = false) Double confidenceThreshold) {
        return videoBehaviorService.identifyAbnormalBehaviors(deviceId, startTime, endTime, confidenceThreshold);
    }

    @Operation(summary = "行为模式匹配")
    @PostMapping("/pattern/match")
    public ResponseDTO<List<Map<String, Object>>> matchBehaviorPatterns(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "行为ID列表", required = true) @RequestParam List<Long> behaviorIds) {
        return videoBehaviorService.matchBehaviorPatterns(deviceId, behaviorIds);
    }

    @Operation(summary = "自定义行为检测")
    @PostMapping("/detect/custom")
    public ResponseDTO<VideoBehaviorVO> customBehaviorDetection(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "自定义规则", required = true) @RequestParam Map<String, Object> customRules,
            @Parameter(description = "阈值设置") @RequestParam(required = false) Map<String, Object> thresholds) {
        return videoBehaviorService.customBehaviorDetection(deviceId, customRules, thresholds);
    }

    // ==================== 导出和报表 ====================

    @Operation(summary = "导出行为数据")
    @PostMapping("/export/data")
    public ResponseDTO<String> exportBehaviorData(
            @Valid @RequestBody VideoBehaviorAnalysisForm form,
            @Parameter(description = "导出格式", required = true) @RequestParam String exportFormat) {
        return videoBehaviorService.exportBehaviorData(form, exportFormat);
    }

    @Operation(summary = "导出行为报告")
    @GetMapping("/export/report")
    public ResponseDTO<String> exportBehaviorReport(
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime,
            @Parameter(description = "报告类型", required = true) @RequestParam String reportType) {
        return videoBehaviorService.exportBehaviorReport(startTime, endTime, reportType);
    }

    @Operation(summary = "生成行为图表数据")
    @PostMapping("/charts")
    public ResponseDTO<Map<String, Object>> generateBehaviorChartData(
            @Valid @RequestBody VideoBehaviorAnalysisForm form,
            @Parameter(description = "图表类型", required = true) @RequestParam List<String> chartTypes) {
        return videoBehaviorService.generateBehaviorChartData(form, chartTypes);
    }

    @Operation(summary = "创建分析任务")
    @PostMapping("/task/create")
    public ResponseDTO<String> createAnalysisTask(
            @Valid @RequestBody VideoBehaviorAnalysisForm form,
            @Parameter(description = "任务名称", required = true) @RequestParam String taskName) {
        return videoBehaviorService.createAnalysisTask(form, taskName);
    }

    @Operation(summary = "获取分析任务状态")
    @GetMapping("/task/{taskId}/status")
    public ResponseDTO<Map<String, Object>> getAnalysisTaskStatus(
            @Parameter(description = "任务ID", required = true) @PathVariable String taskId) {
        return videoBehaviorService.getAnalysisTaskStatus(taskId);
    }

    @Operation(summary = "取消分析任务")
    @DeleteMapping("/task/{taskId}")
    public ResponseDTO<Void> cancelAnalysisTask(
            @Parameter(description = "任务ID", required = true) @PathVariable String taskId) {
        return videoBehaviorService.cancelAnalysisTask(taskId);
    }

    // ==================== 告警和通知 ====================

    @Operation(summary = "发送行为告警")
    @PostMapping("/{behaviorId}/alarm")
    public ResponseDTO<Void> sendBehaviorAlarm(
            @Parameter(description = "行为ID", required = true) @PathVariable Long behaviorId,
            @Parameter(description = "告警类型列表", required = true) @RequestParam List<String> alarmTypes,
            @Parameter(description = "告警级别", required = true) @RequestParam Integer alarmLevel) {
        return videoBehaviorService.sendBehaviorAlarm(behaviorId, alarmTypes, alarmLevel);
    }

    @Operation(summary = "获取行为告警列表")
    @PostMapping("/alarms/queryPage")
    public ResponseDTO<PageResult<VideoBehaviorVO>> getBehaviorAlarms(
            @Valid @RequestBody VideoBehaviorAnalysisForm form,
            PageParam pageParam) {
        return videoBehaviorService.getBehaviorAlarms(form, pageParam);
    }

    @Operation(summary = "配置行为告警规则")
    @PutMapping("/alarm/rules")
    public ResponseDTO<Void> configureAlarmRules(
            @Parameter(description = "告警规则", required = true) @RequestParam Map<String, Object> alarmRules) {
        return videoBehaviorService.configureAlarmRules(alarmRules);
    }

    @Operation(summary = "获取告警规则配置")
    @GetMapping("/alarm/rules")
    public ResponseDTO<Map<String, Object>> getAlarmRulesConfiguration() {
        return videoBehaviorService.getAlarmRulesConfiguration();
    }

    @Operation(summary = "测试行为告警")
    @PostMapping("/alarm/test")
    public ResponseDTO<Void> testBehaviorAlarm(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "测试参数") @RequestParam(required = false) Map<String, Object> testParameters) {
        return videoBehaviorService.testBehaviorAlarm(deviceId, testParameters);
    }

    // ==================== 数据清理和维护 ====================

    @Operation(summary = "清理历史行为记录")
    @DeleteMapping("/clean/old")
    public ResponseDTO<Integer> cleanOldBehaviorRecords(
            @Parameter(description = "截止时间", required = true) @RequestParam LocalDateTime cutoffTime) {
        return videoBehaviorService.cleanOldBehaviorRecords(cutoffTime);
    }

    @Operation(summary = "备份行为数据")
    @PostMapping("/backup")
    public ResponseDTO<String> backupBehaviorData(
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime) {
        return videoBehaviorService.backupBehaviorData(startTime, endTime);
    }

    @Operation(summary = "恢复行为数据")
    @PostMapping("/restore")
    public ResponseDTO<Void> restoreBehaviorData(
            @Parameter(description = "备份文件路径", required = true) @RequestParam String backupFilePath) {
        return videoBehaviorService.restoreBehaviorData(backupFilePath);
    }

    @Operation(summary = "数据完整性检查")
    @GetMapping("/integrity/check")
    public ResponseDTO<Map<String, Object>> checkDataIntegrity() {
        return videoBehaviorService.checkDataIntegrity();
    }

    @Operation(summary = "重建行为索引")
    @PostMapping("/index/rebuild")
    public ResponseDTO<Void> rebuildBehaviorIndex() {
        return videoBehaviorService.rebuildBehaviorIndex();
    }

    @Operation(summary = "优化行为数据")
    @PostMapping("/optimize")
    public ResponseDTO<Map<String, Object>> optimizeBehaviorData() {
        return videoBehaviorService.optimizeBehaviorData();
    }
}
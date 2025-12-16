package net.lab1024.sa.attendance.engine.prediction;

import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排班预测器接口
 * <p>
 * 负责预测排班需求和趋势
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface SchedulePredictor {

    /**
     * 预测排班需求
     *
     * @param predictionData 预测数据
     * @param predictionScope 预测范围
     * @return 预测结果
     */
    SchedulePredictionResult predictScheduleDemand(PredictionData predictionData, PredictionScope predictionScope);

    /**
     * 预测人员需求
     *
     * @param timeRange 时间范围
     * @param skillRequirements 技能要求
     * @param historicalData 历史数据
     * @return 人员需求预测结果
     */
    StaffingPredictionResult predictStaffingDemand(TimeRange timeRange, List<SkillRequirement> skillRequirements, HistoricalData historicalData);

    /**
     * 预测缺勤率
     *
     * @param timeRange 时间范围
     * @param employeeData 员工数据
     * @param historicalData 历史数据
     * @return 缺勤率预测结果
     */
    AbsenteeismPredictionResult predictAbsenteeismRate(TimeRange timeRange, EmployeeData employeeData, HistoricalData historicalData);

    /**
     * 预测工作量
     *
     * @param timeRange 时间范围
     * @param businessFactors 业务因素
     * @param historicalData 历史数据
     * @return 工作量预测结果
     */
    WorkloadPredictionResult predictWorkload(TimeRange timeRange, List<BusinessFactor> businessFactors, HistoricalData historicalData);

    /**
     * 预测成本
     *
     * @param scheduleRecords 排班记录
     * @param costFactors 成本因素
     * @param timeRange 时间范围
     * @return 成本预测结果
     */
    CostPredictionResult predictCost(List<ScheduleRecord> scheduleRecords, List<CostFactor> costFactors, TimeRange timeRange);

    /**
     * 预测员工满意度
     *
     * @param scheduleRecords 排班记录
     * @param employeePreferences 员工偏好
     * @return 满意度预测结果
     */
    SatisfactionPredictionResult predictSatisfaction(List<ScheduleRecord> scheduleRecords, Map<Long, EmployeePreference> employeePreferences);

    /**
     * 预测排班冲突
     *
     * @param proposedSchedule 提议的排班
     * @param scheduleData 排班数据
     * @return 冲突预测结果
     */
    ConflictPredictionResult predictConflicts(List<ScheduleRecord> proposedSchedule, ScheduleData scheduleData);

    /**
     * 预测季节性需求
     *
     * @param historicalData 历史数据
     * @param seasonality 季节性参数
     * @return 季节性需求预测结果
     */
    SeasonalPredictionResult predictSeasonalDemand(HistoricalData historicalData, SeasonalityParameters seasonality);

    /**
     * 实时预测更新
     *
     * @param currentData 当前数据
     * @param predictionContext 预测上下文
     * @return 实时预测结果
     */
    RealTimePredictionResult updateRealTimePrediction(CurrentData currentData, PredictionContext predictionContext);

    /**
     * 验证预测准确性
     *
     * @param predictedValues 预测值
     * @param actualValues 实际值
     * @param predictionType 预测类型
     * @return 验证结果
     */
    PredictionValidationResult validatePrediction(List<Double> predictedValues, List<Double> actualValues, String predictionType);

    /**
     * 获取预测模型性能
     *
     * @param predictionType 预测类型
     * @return 模型性能指标
     */
    ModelPerformanceMetrics getModelPerformance(String predictionType);

    /**
     * 获取预测建议
     *
     * @param predictionResults 预测结果列表
     * @return 预测建议
     */
    List<PredictionSuggestion> getPredictionSuggestions(List<SchedulePredictionResult> predictionResults);

    /**
     * 批量预测
     *
     * @param predictionRequests 批量预测请求
     * @return 批量预测结果
     */
    BatchPredictionResult predictBatch(List<PredictionRequest> predictionRequests);
}
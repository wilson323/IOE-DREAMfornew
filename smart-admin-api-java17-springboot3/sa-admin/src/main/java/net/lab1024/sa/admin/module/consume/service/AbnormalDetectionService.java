/*
 * 异常操作检测服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.AbnormalDetectionResult;
import net.lab1024.sa.admin.module.consume.domain.result.BehaviorMonitoringResult;
import net.lab1024.sa.admin.module.consume.domain.result.UserPatternAnalysis;
import net.lab1024.sa.admin.module.consume.domain.result.LocationAnomalyResult;
import net.lab1024.sa.admin.module.consume.domain.result.TimeAnomalyResult;
import net.lab1024.sa.admin.module.consume.domain.result.AmountAnomalyResult;
import net.lab1024.sa.admin.module.consume.domain.result.DeviceAnomalyResult;
import net.lab1024.sa.admin.module.consume.domain.result.FrequencyAnomalyResult;
import net.lab1024.sa.admin.module.consume.domain.result.UserRiskScore;
import net.lab1024.sa.admin.module.consume.domain.result.AbnormalOperationReport;
import net.lab1024.sa.admin.module.consume.domain.result.SequenceAnomalyResult;
import net.lab1024.sa.admin.module.consume.domain.result.AbnormalTrendAnalysis;
import net.lab1024.sa.admin.module.consume.domain.result.BatchDetectionResult;
import net.lab1024.sa.admin.module.consume.domain.result.ModelTrainingResult;
import net.lab1024.sa.admin.module.consume.domain.result.MLDetectionResult;
import net.lab1024.sa.admin.module.consume.domain.result.DetectionPerformanceMetrics;
import net.lab1024.sa.admin.module.consume.domain.vo.UserBehaviorBaseline;
import net.lab1024.sa.admin.module.consume.domain.entity.OperationEventEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.DetectionRuleEntity;
import net.lab1024.sa.admin.module.consume.domain.result.ExportResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 异常操作检测服务接口
 * 提供消费异常行为检测和分析功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface AbnormalDetectionService {

    /**
     * 检测消费操作是否异常
     *
     * @param personId 人员ID
     * @param amount 消费金额
     * @param deviceId 设备ID
     * @param regionId 区域ID
     * @param clientIp 客户端IP
     * @param consumptionMode 消费模式
     * @return 检测结果
     */
    AbnormalDetectionResult detectAbnormalOperation(@NotNull Long personId, @NotNull BigDecimal amount,
                                                   Long deviceId, String regionId, String clientIp,
                                                   String consumptionMode);

    /**
     * 实时监控用户行为
     *
     * @param personId 人员ID
     * @param operationType 操作类型
     * @param operationData 操作数据
     * @return 监控结果
     */
    BehaviorMonitoringResult monitorUserBehavior(@NotNull Long personId, @NotNull String operationType,
                                                Object operationData);

    /**
     * 分析用户消费模式
     *
     * @param personId 人员ID
     * @param analysisDays 分析天数
     * @return 模式分析结果
     */
    UserPatternAnalysis analyzeUserPattern(@NotNull Long personId, Integer analysisDays);

    /**
     * 检测地理位置异常
     *
     * @param personId 人员ID
     * @param currentLocation 当前位置
     * @param deviceId 设备ID
     * @return 位置检测结果
     */
    LocationAnomalyResult detectLocationAnomaly(@NotNull Long personId, @NotNull String currentLocation,
                                                Long deviceId);

    /**
     * 检测时间异常
     *
     * @param personId 人员ID
     * @param operationTime 操作时间
     * @param operationType 操作类型
     * @return 时间检测结果
     */
    TimeAnomalyResult detectTimeAnomaly(@NotNull Long personId, java.time.LocalDateTime operationTime,
                                        String operationType);

    /**
     * 检测金额异常
     *
     * @param personId 人员ID
     * @param amount 操作金额
     * @param operationType 操作类型
     * @return 金额检测结果
     */
    AmountAnomalyResult detectAmountAnomaly(@NotNull Long personId, @NotNull BigDecimal amount,
                                            String operationType);

    /**
     * 检测设备异常
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param operationType 操作类型
     * @return 设备检测结果
     */
    DeviceAnomalyResult detectDeviceAnomaly(@NotNull Long personId, @NotNull Long deviceId,
                                            String operationType);

    /**
     * 检测频率异常
     *
     * @param personId 人员ID
     * @param operationType 操作类型
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 频率检测结果
     */
    FrequencyAnomalyResult detectFrequencyAnomaly(@NotNull Long personId, @NotNull String operationType,
                                                   Integer timeWindowMinutes);

    /**
     * 获取用户风险评分
     *
     * @param personId 人员ID
     * @return 风险评分
     */
    UserRiskScore getUserRiskScore(@NotNull Long personId);

    /**
     * 更新用户风险评分
     *
     * @param personId 人员ID
     * @param operationResult 操作结果
     * @return 更新结果
     */
    boolean updateUserRiskScore(@NotNull Long personId, @NotNull AbnormalDetectionResult operationResult);

    /**
     * 获取异常操作报告
     *
     * @param personId 人员ID
     * @param reportType 报告类型
     * @param timeRange 时间范围
     * @return 异常报告
     */
    AbnormalOperationReport getAbnormalOperationReport(@NotNull Long personId, @NotNull String reportType,
                                                         String timeRange);

    /**
     * 生成异常告警
     *
     * @param alertLevel 告警级别
     * @param alertType 告警类型
     * @param alertMessage 告警消息
     * @param relatedData 相关数据
     * @return 告警结果
     */
    boolean generateAlert(@NotNull String alertLevel, @NotNull String alertType, @NotNull String alertMessage,
                          Object relatedData);

    /**
     * 获取用户行为基线
     *
     * @param personId 人员ID
     * @return 行为基线
     */
    UserBehaviorBaseline getUserBehaviorBaseline(@NotNull Long personId);

    /**
     * 更新用户行为基线
     *
     * @param personId 人员ID
     * @param baseline 行为基线
     * @return 更新结果
     */
    boolean updateUserBehaviorBaseline(@NotNull Long personId, @NotNull UserBehaviorBaseline baseline);

    /**
     * 检测序列异常
     *
     * @param personId 人员ID
     * @param operations 操作序列
     * @return 序列检测结果
     */
    SequenceAnomalyResult detectSequenceAnomaly(@NotNull Long personId, @NotNull List<OperationEventEntity> operations);

    /**
     * 获取异常趋势分析
     *
     * @param analysisType 分析类型
     * @param timeRange 时间范围
     * @return 趋势分析结果
     */
    AbnormalTrendAnalysis getAbnormalTrendAnalysis(@NotNull String analysisType, @NotNull String timeRange);

    /**
     * 设置检测规则
     *
     * @param ruleType 规则类型
     * @param rules 规则列表
     * @return 设置结果
     */
    boolean setDetectionRuleEntitys(@NotNull String ruleType, @NotNull List<DetectionRuleEntity> rules);

    /**
     * 获取检测规则
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    List<DetectionRuleEntity> getDetectionRuleEntitys(@NotNull String ruleType);

    /**
     * 批量检测异常操作
     *
     * @param operations 操作列表
     * @return 批量检测结果
     */
    BatchDetectionResult batchDetectAbnormalOperations(@NotNull List<OperationEventEntity> operations);

    /**
     * 训练机器学习模型
     *
     * @param modelType 模型类型
     * @param trainingData 训练数据
     * @return 训练结果
     */
    ModelTrainingResult trainMachineLearningModel(@NotNull String modelType, @NotNull Object trainingData);

    /**
     * 使用机器学习模型检测异常
     *
     * @param modelType 模型类型
     * @param inputData 输入数据
     * @return 检测结果
     */
    MLDetectionResult detectAnomalyWithML(@NotNull String modelType, @NotNull Object inputData);

    /**
     * 获取检测性能指标
     *
     * @param timeRange 时间范围
     * @return 性能指标
     */
    DetectionPerformanceMetrics getDetectionPerformanceMetrics(@NotNull String timeRange);

    /**
     * 导出检测数据
     *
     * @param exportType 导出类型
     * @param filterConditions 过滤条件
     * @return 导出结果
     */
    ExportResult exportDetectionData(@NotNull String exportType, @NotNull Object filterConditions);
}
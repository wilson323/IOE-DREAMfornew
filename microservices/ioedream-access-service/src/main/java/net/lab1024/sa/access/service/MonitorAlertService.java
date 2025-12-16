package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 监控告警服务接口
 * <p>
 * 提供统一智能的监控告警体系：
 * - 多维度异常检测与预警
 * - 智能告警分级与推送
 * - 告警处理流程跟踪
 * - 监控指标统计分析
 * - 告警规则动态配置
 * - 故障自愈与恢复机制
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 清晰的方法注释
 * - 统一的数据传输对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface MonitorAlertService {

    /**
     * 创建监控告警
     * <p>
     * 创建一个新的监控告警事件：
     * - 告警信息完整性验证
     * - 告警级别智能评估
     * - 告警去重与聚合
     * - 告警路由策略确定
     * </p>
     *
     * @param request 告警创建请求
     * @return 告警创建结果
     */
    ResponseDTO<MonitorAlertResult> createMonitorAlert(CreateMonitorAlertRequest request);

    /**
     * 智能告警分级
     * <p>
     * 基于多维度指标进行智能告警分级：
     * - 影响范围评估
     * - 紧急程度分析
     * - 业务影响评估
     * - 风险等级计算
     * </p>
     *
     * @param request 分级请求
     * @return 分级结果
     */
    ResponseDTO<AlertLevelAssessmentResult> assessAlertLevel(AlertLevelAssessmentRequest request);

    /**
     * 告警通知推送
     * <p>
     * 根据告警级别和配置规则推送告警通知：
     * - 多渠道通知支持（邮件、短信、微信、钉钉）
     * - 通知模板动态渲染
     * - 通知发送状态跟踪
     * - 通知频率控制
     * </p>
     *
     * @param request 通知推送请求
     * @return 推送结果
     */
    ResponseDTO<AlertNotificationResult> sendAlertNotification(AlertNotificationRequest request);

    /**
     * 获取告警列表
     * <p>
     * 查询监控系统告警列表：
     * - 多维度条件筛选
     * - 告警状态跟踪
     * - 处理历史记录
     * - 统计分析支持
     * </p>
     *
     * @param request 查询请求
     * @return 告警列表
     */
    ResponseDTO<List<MonitorAlertVO>> getMonitorAlertList(MonitorAlertQueryRequest request);

    /**
     * 处理告警事件
     * <p>
     * 处理监控告警事件：
     * - 告警确认操作
     * - 处理方案执行
     * - 处理结果记录
     * - 关联事件更新
     * </p>
     *
     * @param request 处理请求
     * @return 处理结果
     */
    ResponseDTO<AlertHandleResult> handleAlert(AlertHandleRequest request);

    /**
     * 告警统计分析
     * <p>
     * 生成监控告警的统计分析报告：
     * - 告警趋势分析
     * - 处理效率统计
     * - 问题根源分析
     * - 改进建议生成
     * </p>
     *
     * @param request 统计请求
     * @return 统计报告
     */
    ResponseDTO<AlertStatisticsReport> generateAlertStatistics(AlertStatisticsRequest request);

    /**
     * 配置告警规则
     * <p>
     * 管理监控告警的规则配置：
     * - 规则创建与更新
     * - 条件表达式定义
     * - 动作策略配置
     * - 规则有效性验证
     * </p>
     *
     * @param request 配置请求
     * @return 配置结果
     */
    ResponseDTO<AlertRuleResult> configureAlertRule(AlertRuleConfigureRequest request);

    /**
     * 获取告警规则列表
     * <p>
     * 查询系统配置的告警规则列表：
     * - 规则状态管理
     * - 规则优先级排序
     * - 规则使用统计
     * - 规则效果评估
     * </p>
     *
     * @param request 查询请求
     * @return 规则列表
     */
    ResponseDTO<List<AlertRuleVO>> getAlertRuleList(AlertRuleQueryRequest request);

    /**
     * 系统健康检查
     * <p>
     * 执行系统全面的健康检查：
     * - 服务可用性检查
     * - 性能指标监控
     * - 资源使用评估
     * - 潜在风险识别
     * </p>
     *
     * @param request 检查请求
     * @return 健康检查结果
     */
    ResponseDTO<SystemHealthCheckResult> performSystemHealthCheck(SystemHealthCheckRequest request);

    /**
     * 故障自愈处理
     * <p>
     * 执行系统故障的自动恢复处理：
     * - 故障类型识别
     * - 自愈策略执行
     * - 恢复结果验证
     * - 人工介入判断
     * </p>
     *
     * @param request 自愈请求
     * @return 自愈结果
     */
    ResponseDTO<SelfHealingResult> performSelfHealing(SelfHealingRequest request);

    /**
     * 告警趋势预测
     * <p>
     * 基于历史数据预测告警趋势：
     * - 时间序列分析
     * - 季节性模式识别
     * - 异常趋势预警
     * - 容量规划建议
     * </p>
     *
     * @param request 预测请求
     * @return 预测结果
     */
    ResponseDTO<AlertTrendPredictionResult> predictAlertTrend(AlertTrendPredictionRequest request);

    // ==================== 内部数据传输对象 ====================

    /**
     * 创建监控告警请求
     */
    class CreateMonitorAlertRequest {
        private String alertTitle;           // 告警标题
        private String alertDescription;      // 告警描述
        private String alertType;             // 告警类型
        private String sourceSystem;          // 来源系统
        private String sourceComponent;       // 来源组件
        private String sourceInstanceId;      // 来源实例ID
        private LocalDateTime occurTime;       // 发生时间
        private String severityLevel;         // 严重等级（LOW/MEDIUM/HIGH/CRITICAL）
        private Map<String, Object> alertData; // 告警数据
        private List<String> affectedServices; // 影响的服务
        private List<String> tags;           // 标签列表
        private Boolean needNotification;     // 是否需要通知
        private String alertCategory;        // 告警分类
        private Integer priority;            // 优先级（1-10）
        private String assignedTo;            // 分配给
        private String escalationRule;        // 升级规则
    }

    /**
     * 监控告警结果
     */
    class MonitorAlertResult {
        private String alertId;              // 告警ID
        private String alertTitle;           // 告警标题
        private String alertDescription;      // 告警描述
        private LocalDateTime createTime;     // 创建时间
        private String severityLevel;         // 严重等级
        private String status;               // 状态（NEW/ACKNOWLEDGED/RESOLVED/CLOSED）
        private List<String> notificationIds; // 通知ID列表
        private List<String> relatedAlertIds;  // 关联告警ID
        private String assignedTo;            // 分配给
        private LocalDateTime estimatedResolveTime; // 预计解决时间
        private Map<String, Object> metadata;  // 元数据
    }

    /**
     * 告警分级评估请求
     */
    class AlertLevelAssessmentRequest {
        private String alertType;             // 告警类型
        private String sourceSystem;          // 来源系统
        private Map<String, Object> alertMetrics; // 告警指标
        private String businessImpact;        // 业务影响
        private String affectedScope;         // 影响范围
        private LocalDateTime occurTime;       // 发生时间
        private Integer affectedUsers;        // 影响用户数
        private List<String> affectedServices; // 影响的服务
        private Boolean isRecurring;          // 是否重复发生
        private Integer recurrenceCount;      // 重复次数
    }

    /**
     * 告警级别评估结果
     */
    class AlertLevelAssessmentResult {
        private String assessedLevel;         // 评估的级别
        private Double confidenceScore;       // 置信度评分
        private String assessmentReason;      // 评估原因
        private List<String> assessmentFactors; // 评估因素
        private String businessImpactLevel;   // 业务影响级别
        private String urgencyLevel;          // 紧急程度
        private String recommendedAction;     // 建议措施
        private Integer recommendedPriority; // 推荐优先级
        private LocalDateTime escalateTime;   // 升级时间
        private Map<String, Object> detailedMetrics; // 详细指标
    }

    /**
     * 告警通知推送请求
     */
    class AlertNotificationRequest {
        private String alertId;              // 告警ID
        private List<String> notificationChannels; // 通知渠道
        private List<String> recipients;      // 接收人列表
        private Map<String, Object> notificationData; // 通知数据
        private String notificationTemplate;  // 通知模板
        private Boolean needEscalation;       // 是否需要升级
        private Integer maxRetries;           // 最大重试次数
        private Integer retryInterval;        // 重试间隔（秒）
        private Map<String, String> customHeaders; // 自定义头部
    }

    /**
     * 告警通知结果
     */
    class AlertNotificationResult {
        private String notificationId;       // 通知ID
        private String alertId;              // 告警ID
        private List<NotificationChannelResult> channelResults; // 渠道结果
        private LocalDateTime sendTime;      // 发送时间
        private Integer totalRecipients;      // 总接收人数
        private Integer successCount;         // 成功发送数
        private Integer failureCount;         // 失败发送数
        private String overallStatus;         // 整体状态
        private List<String> failedRecipients; // 失败接收人
        private Map<String, Object> responseMetadata; // 响应元数据
    }

    /**
     * 通知渠道结果
     */
    class NotificationChannelResult {
        private String channel;              // 渠道（EMAIL/SMS/WECHAT/DINGTALK）
        private Integer sentCount;           // 发送数量
        private Integer successCount;         // 成功数量
        private Integer failureCount;         // 失败数量
        private String status;               // 渠道状态
        private String errorMessage;         // 错误信息
        private LocalDateTime completedTime;  // 完成时间
    }

    /**
     * 监控告警查询请求
     */
    class MonitorAlertQueryRequest {
        private String alertId;              // 告警ID
        private String alertType;             // 告警类型
        private String severityLevel;         // 严重等级
        private String status;               // 状态
        private String sourceSystem;          // 来源系统
        private String assignedTo;            // 分配给
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private List<String> tags;           // 标签
        private Integer pageNum;             // 页码
        private Integer pageSize;            // 每页大小
        private String sortBy;               // 排序字段
        private String sortOrder;            // 排序方向
    }

    /**
     * 监控告警视图对象
     */
    class MonitorAlertVO {
        private String alertId;              // 告警ID
        private String alertTitle;           // 告警标题
        private String alertDescription;      // 告警描述
        private String alertType;             // 告警类型
        private String sourceSystem;          // 来源系统
        private LocalDateTime occurTime;       // 发生时间
        private LocalDateTime createTime;     // 创建时间
        private LocalDateTime updateTime;     // 更新时间
        private String severityLevel;         // 严重等级
        private String status;               // 状态
        private String assignedTo;            // 分配给
        private String assignedToName;        // 分配人姓名
        private Integer duration;             // 持续时长（分钟）
        private List<String> affectedServices; // 影响的服务
        private List<String> tags;           // 标签
        private Boolean isRecurring;          // 是否重复
        private Integer recurrenceCount;      // 重复次数
        private String businessImpact;        // 业务影响
        private String resolution;            // 解决方案
        private LocalDateTime resolvedTime;   // 解决时间
        private Integer resolutionDuration;   // 解决时长（分钟）
    }

    /**
     * 告警处理请求
     */
    class AlertHandleRequest {
        private String alertId;              // 告警ID
        private String handleAction;          // 处理动作（ACKNOWLEDGE/ASSIGN/RESOLVE/CLOSE/ESCALATE）
        private String handleComment;         // 处理说明
        private String assignedTo;            // 分配给
        private String resolutionMethod;      // 解决方法
        private List<String> attachments;      // 附件
        private Boolean markAsResolved;       // 标记为已解决
        private LocalDateTime estimatedResolveTime; // 预计解决时间
        private Map<String, Object> handleData; // 处理数据
        private Boolean sendNotification;     // 是否发送通知
    }

    /**
     * 告警处理结果
     */
    class AlertHandleResult {
        private String alertId;              // 告警ID
        private String handleAction;          // 处理动作
        private String previousStatus;        // 处理前状态
        private String currentStatus;         // 处理后状态
        private LocalDateTime handleTime;     // 处理时间
        private String handledBy;             // 处理人
        private String handleComment;         // 处理说明
        private Boolean autoGenerated;        // 是否自动生成
        private List<String> affectedAlerts;  // 影响的告警
        private Map<String, Object> handleMetadata; // 处理元数据
    }

    /**
     * 告警统计请求
     */
    class AlertStatisticsRequest {
        private String statisticsType;       // 统计类型（DAILY/WEEKLY/MONTHLY/CUSTOM）
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private List<String> alertTypes;      // 告警类型筛选
        private List<String> severityLevels;  // 严重等级筛选
        private List<String> sourceSystems;   // 来源系统筛选
        private String groupBy;               // 分组方式（TYPE/SEVERITY/SOURCE/STATUS）
        private List<String> metrics;          // 统计指标
        private Boolean includeTrends;        // 是否包含趋势
        private Integer topCount;             // Top数量
    }

    /**
     * 告警统计报告
     */
    class AlertStatisticsReport {
        private String statisticsPeriod;     // 统计周期
        private LocalDateTime reportTime;     // 报告时间
        private Long totalAlerts;            // 总告警数
        private Map<String, Long> alertsByType; // 按类型分组的告警数
        private Map<String, Long> alertsBySeverity; // 按严重等级分组的告警数
        private Map<String, Long> alertsBySource; // 按来源系统分组的告警数
        private Map<String, Long> alertsByStatus; // 按状态分组的告警数
        private Double averageResolutionTime; // 平均解决时间
        private Double resolutionRate;        // 解决率
        private List<AlertTrendData> trendData; // 趋势数据
        private List<AlertTopItem> topAlertTypes; // 主要告警类型
        private List<AlertTopItem> topSources; // 主要来源系统
        private Map<String, Object> insights; // 洞察分析
    }

    /**
     * 告警趋势数据
     */
    class AlertTrendData {
        private String timeSlot;             // 时间段
        private Long alertCount;             // 告警数量
        private Double resolutionTime;        // 平均解决时间
        private Double resolutionRate;        // 解决率
        private List<String> topAlertTypes;   // 主要告警类型
    }

    /**
     * 告警Top项目
     */
    class AlertTopItem {
        private String itemName;             // 项目名称
        private Long count;                  // 数量
        private Double percentage;            // 百分比
        private String trend;                // 趋势（UP/DOWN/STABLE）
    }

    /**
     * 告警规则配置请求
     */
    class AlertRuleConfigureRequest {
        private String ruleId;               // 规则ID（新增时为空）
        private String ruleName;             // 规则名称
        private String ruleDescription;      // 规则描述
        private String ruleType;             // 规则类型（THRESHOLD/PATTERN/ANOMALY/COMPOSITE）
        private Boolean enabled;             // 是否启用
        private Integer priority;            // 优先级
        private String conditionExpression;  // 条件表达式
        private List<AlertRuleAction> actions; // 动作列表
        private String evaluationInterval;   // 评估间隔
        private Integer consecutiveFailures; // 连续失败次数
        private String severityLevel;         // 默认严重等级
        private List<String> tags;           // 标签
        private Map<String, Object> ruleParameters; // 规则参数
    }

    /**
     * 告警规则动作
     */
    class AlertRuleAction {
        private String actionType;           // 动作类型（NOTIFICATION/ESCALATION/AUTO_REMEDY/WEBHOOK）
        private String actionName;           // 动作名称
        private Map<String, Object> actionParameters; // 动作参数
        private Boolean enabled;             // 是否启用
        private Integer order;                // 执行顺序
        private String condition;            // 执行条件
    }

    /**
     * 告警规则结果
     */
    class AlertRuleResult {
        private String ruleId;               // 规则ID
        private String ruleName;             // 规则名称
        private Boolean enabled;             // 是否启用
        private String status;               // 状态（ACTIVE/INACTIVE/ERROR）
        private LocalDateTime lastEvaluated;  // 最后评估时间
        private Integer evaluationCount;     // 评估次数
        private Integer triggerCount;         // 触发次数
        private String validationMessage;    // 验证消息
        private List<String> warnings;       // 警告信息
    }

    /**
     * 告警规则查询请求
     */
    class AlertRuleQueryRequest {
        private String ruleId;               // 规则ID
        private String ruleName;             // 规则名称
        private String ruleType;             // 规则类型
        private Boolean enabled;             // 是否启用
        private String status;               // 状态
        private List<String> tags;           // 标签
        private Integer pageNum;             // 页码
        private Integer pageSize;            // 每页大小
    }

    /**
     * 告警规则视图对象
     */
    class AlertRuleVO {
        private String ruleId;               // 规则ID
        private String ruleName;             // 规则名称
        private String ruleDescription;      // 规则描述
        private String ruleType;             // 规则类型
        private Boolean enabled;             // 是否启用
        private Integer priority;            // 优先级
        private String conditionExpression;  // 条件表达式
        private List<AlertRuleAction> actions; // 动作列表
        private String evaluationInterval;   // 评估间隔
        private String severityLevel;         // 默认严重等级
        private LocalDateTime createTime;     // 创建时间
        private LocalDateTime updateTime;     // 更新时间
        private LocalDateTime lastEvaluated;  // 最后评估时间
        private Integer triggerCount;         // 触发次数
        private String status;               // 状态
        private List<String> tags;           // 标签
    }

    /**
     * 系统健康检查请求
     */
    class SystemHealthCheckRequest {
        private List<String> checkCategories; // 检查类别
        private List<String> checkItems;       // 检查项目
        private Boolean includeDetails;       // 是否包含详细信息
        private Boolean generateReport;       // 是否生成报告
        private String reportFormat;          // 报告格式（JSON/HTML/PDF）
        private Map<String, Object> checkParameters; // 检查参数
    }

    /**
     * 系统健康检查结果
     */
    class SystemHealthCheckResult {
        private String checkId;              // 检查ID
        private LocalDateTime checkTime;     // 检查时间
        private String overallHealth;        // 整体健康状态（HEALTHY/WARNING/CRITICAL）
        private Double overallScore;          // 整体评分（0-100）
        private List<HealthCheckItem> checkItems; // 检查项目列表
        private Map<String, Object> systemMetrics; // 系统指标
        private List<String> recommendations; // 建议
        private String reportUrl;            // 报告URL
        private Integer totalChecks;         // 总检查数
        private Integer passedChecks;        // 通过检查数
        private Integer failedChecks;        // 失败检查数
        private Integer warningChecks;       // 警告检查数
    }

    /**
     * 健康检查项目
     */
    class HealthCheckItem {
        private String itemName;             // 项目名称
        private String category;             // 类别
        private String status;               // 状态（PASS/WARN/FAIL/SKIP）
        private Double score;                // 评分
        private String message;              // 消息
        private Map<String, Object> details;  // 详细信息
        private String recommendation;       // 建议
        private LocalDateTime checkTime;     // 检查时间
    }

    /**
     * 故障自愈请求
     */
    class SelfHealingRequest {
        private String incidentId;           // 事件ID
        private String failureType;          // 故障类型
        private String failureDescription;   // 故障描述
        private String affectedComponent;    // 受影响组件
        private Map<String, Object> failureData; // 故障数据
        private Boolean requireApproval;     // 是否需要审批
        private String selfHealingStrategy;  // 自愈策略
        private Integer maxRetries;           // 最大重试次数
        private Integer timeoutSeconds;       // 超时时间
        private Map<String, Object> healingParameters; // 自愈参数
    }

    /**
     * 故障自愈结果
     */
    class SelfHealingResult {
        private String healingId;            // 自愈ID
        private String incidentId;           // 事件ID
        private Boolean healingSuccess;       // 自愈是否成功
        private String healingStrategy;      // 自愈策略
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private Integer duration;            // 持续时间（秒）
        private Integer attemptCount;         // 尝试次数
        private String finalStatus;          // 最终状态
        private String failureReason;        // 失败原因
        private List<String> actionsTaken;   // 已执行动作
        private Boolean requireManualIntervention; // 是否需要人工干预
        private Map<String, Object> healingMetrics; // 自愈指标
    }

    /**
     * 告警趋势预测请求
     */
    class AlertTrendPredictionRequest {
        private String predictionModel;      // 预测模型（LINEAR/SEASONAL/ARIMA/ML）
        private LocalDateTime startTime;      // 开始时间
        private LocalDateTime endTime;        // 结束时间
        private String predictionPeriod;     // 预测周期（HOURLY/DAILY/WEEKLY/MONTHLY）
        private List<String> alertTypes;      // 告警类型
        private List<String> sourceSystems;   // 来源系统
        private Integer predictionDays;      // 预测天数
        private Double confidenceThreshold;   // 置信度阈值
        private Boolean includeSeasonalFactors; // 是否包含季节性因素
        private Map<String, Object> modelParameters; // 模型参数
    }

    /**
     * 告警趋势预测结果
     */
    class AlertTrendPredictionResult {
        private String predictionId;         // 预测ID
        private String predictionModel;      // 预测模型
        private LocalDateTime predictionTime; // 预测时间
        private List<AlertPredictionData> predictions; // 预测数据
        private Double modelAccuracy;        // 模型准确度
        private Double confidenceScore;      // 置信度评分
        private List<String> identifiedPatterns; // 识别的模式
        private List<AlertAnomaly> anomalies; // 异常点
        private Map<String, Object> modelMetrics; // 模型指标
        private List<String> recommendations; // 建议
    }

    /**
     * 告警预测数据
     */
    class AlertPredictionData {
        private String timeSlot;             // 时间段
        private Long predictedCount;         // 预测数量
        private Double confidenceIntervalLower; // 置信区间下限
        private Double confidenceIntervalUpper; // 置信区间上限
        private String trendDirection;       // 趋势方向（UP/DOWN/STABLE）
        private List<String> influencingFactors; // 影响因素
    }

    /**
     * 告警异常点
     */
    class AlertAnomaly {
        private String timeSlot;             // 时间段
        private Long actualCount;            // 实际数量
        private Long predictedCount;         // 预测数量
        private Double deviationScore;        // 偏差评分
        private String anomalyType;          // 异常类型（SPIKE/DROP/OUTLIER）
        private String description;          // 描述
        private Boolean needAttention;       // 是否需要关注
    }
}
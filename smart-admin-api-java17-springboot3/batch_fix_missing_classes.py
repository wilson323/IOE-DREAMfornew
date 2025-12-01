#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修复缺失类的脚本
基于编译错误自动生成缺失的VO/DTO类
"""

import os
import re
import json
from pathlib import Path

# 项目根目录
PROJECT_ROOT = Path(__file__).parent
SA_ADMIN_ROOT = PROJECT_ROOT / "sa-admin" / "src" / "main" / "java" / "net" / "lab1024" / "sa" / "admin" / "module" / "consume"

# 需要创建的类映射
MISSING_CLASSES = {
    # 异常检测相关
    "AbnormalDetectionResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long detectionId;",
            "private Long userId;",
            "private String detectionType;",
            "private String riskLevel;",
            "private Double abnormalScore;",
            "private String description;",
            "private String details;",
            "private LocalDateTime detectionTime;",
            "private String status;",
            "private String remarks;",
            "private List<String> tags;"
        ]
    },
    "BehaviorMonitoringResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String behaviorType;",
            "private String riskLevel;",
            "private Double score;",
            "private String description;",
            "private LocalDateTime detectionTime;",
            "private Map<String, Object> details;"
        ]
    },
    "UserPatternAnalysis": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String patternType;",
            "private Double deviationScore;",
            "private String status;",
            "private LocalDateTime analysisTime;",
            "private Map<String, Object> patternData;"
        ]
    },
    "LocationAnomalyResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String locationId;",
            "private String anomalyType;",
            "private Double confidence;",
            "private String description;",
            "private LocalDateTime detectionTime;"
        ]
    },
    "TimeAnomalyResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String timeRange;",
            "private String anomalyType;",
            "private Double deviation;",
            "private String description;",
            "private LocalDateTime detectionTime;"
        ]
    },
    "AmountAnomalyResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String transactionType;",
            "private BigDecimal amount;",
            "private BigDecimal expectedAmount;",
            "private Double deviationPercentage;",
            "private String riskLevel;",
            "private LocalDateTime detectionTime;"
        ]
    },
    "DeviceAnomalyResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String deviceId;",
            "private String deviceType;",
            "private String anomalyType;",
            "private String description;",
            "private String severity;",
            "private LocalDateTime detectionTime;"
        ]
    },
    "FrequencyAnomalyResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String actionType;",
            "private Integer actualFrequency;",
            "private Integer expectedFrequency;",
            "private Double deviationRatio;",
            "private String riskLevel;",
            "private LocalDateTime detectionTime;"
        ]
    },
    "UserRiskScore": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private Double riskScore;",
            "private String riskLevel;",
            "private Map<String, Double> componentScores;",
            "private LocalDateTime calculatedTime;",
            "private Integer validDays;"
        ]
    },
    "AbnormalOperationReport": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long reportId;",
            "private String reportType;",
            "private LocalDateTime reportTime;",
            "private Integer totalAnomalies;",
            "private Map<String, Integer> anomalyCounts;",
            "private String summary;",
            "private List<String> recommendations;"
        ]
    },
    "UserBehaviorBaseline": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String baselineType;",
            "private Map<String, Object> baselineData;",
            "private LocalDateTime lastUpdated;",
            "private Integer sampleSize;",
            "private Double confidenceLevel;"
        ]
    },
    "OperationEvent": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String eventId;",
            "private Long userId;",
            "private String operationType;",
            "private LocalDateTime eventTime;",
            "private Map<String, Object> eventData;",
            "private String source;"
        ]
    },
    "SequenceAnomalyResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String sequenceType;",
            "private String expectedPattern;",
            "private String actualPattern;",
            "private Double anomalyScore;",
            "private LocalDateTime detectionTime;"
        ]
    },
    "AbnormalTrendAnalysis": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String analysisType;",
            "private LocalDateTime startTime;",
            "private LocalDateTime endTime;",
            "private String trendDirection;",
            "private Double changeRate;",
            "private List<Map<String, Object>> dataPoints;"
        ]
    },
    "DetectionRule": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long ruleId;",
            "private String ruleName;",
            "private String ruleType;",
            "private String conditionExpression;",
            "private String actionType;",
            "private Integer priority;",
            "private Boolean enabled;",
            "private LocalDateTime createTime;",
            "private LocalDateTime updateTime;"
        ]
    },
    "BatchDetectionResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String batchId;",
            "private Integer totalCount;",
            "private Integer anomalyCount;",
            "private LocalDateTime startTime;",
            "private LocalDateTime endTime;",
            "private List<AbnormalDetectionResult> results;"
        ]
    },
    "ModelTrainingResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String modelId;",
            "private String modelName;",
            "private String modelType;",
            "private Double accuracy;",
            "private Double precision;",
            "private Double recall;",
            "private LocalDateTime trainingTime;",
            "private Integer trainingSamples;"
        ]
    },
    "MLDetectionResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String predictionResult;",
            "private Double confidenceScore;",
            "private Map<String, Double> featureImportance;",
            "private LocalDateTime detectionTime;",
            "private String modelVersion;"
        ]
    },
    "DetectionPerformanceMetrics": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String metricType;",
            "private Double value;",
            "private LocalDateTime measurementTime;",
            "private Map<String, Object> additionalMetrics;"
        ]
    },
    "ExportResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String exportId;",
            "private String exportType;",
            "private String fileName;",
            "private Integer recordCount;",
            "private LocalDateTime exportTime;",
            "private String status;",
            "private String downloadUrl;"
        ]
    },
    # 账户安全相关
    "AccountSecurityResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String operation;",
            "private Boolean success;",
            "private String message;",
            "private LocalDateTime operationTime;",
            "private Map<String, Object> details;"
        ]
    },
    "AccountFreezeInfo": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String freezeReason;",
            "private LocalDateTime freezeTime;",
            "private LocalDateTime unfreezeTime;",
            "private String operatorId;",
            "private String status;"
        ]
    },
    "AccountFreezeHistory": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long historyId;",
            "private Long userId;",
            "private String freezeReason;",
            "private LocalDateTime freezeTime;",
            "private LocalDateTime unfreezeTime;",
            "private String operatorId;",
            "private String remarks;"
        ]
    },
    "AccountFreezeRequest": {
        "package": "net.lab1024.sa.admin.module.consume.domain.dto",
        "type": "dto",
        "fields": [
            "private Long userId;",
            "private String freezeReason;",
            "private String operatorId;",
            "private String remarks;",
            "private Integer freezeDuration;"
        ]
    },
    "AccountUnfreezeRequest": {
        "package": "net.lab1024.sa.admin.module.consume.domain.dto",
        "type": "dto",
        "fields": [
            "private Long userId;",
            "private String operatorId;",
            "private String remarks;",
            "private Boolean forceUnfreeze;"
        ]
    },
    "BatchSecurityResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String batchId;",
            "private Integer totalCount;",
            "private Integer successCount;",
            "private Integer failureCount;",
            "private List<AccountSecurityResult> results;",
            "private LocalDateTime operationTime;"
        ]
    },
    "AutoFreezeRule": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long ruleId;",
            "private String ruleName;",
            "private String triggerCondition;",
            "private String actionType;",
            "private Integer freezeDuration;",
            "private Boolean enabled;",
            "private LocalDateTime createTime;",
            "private LocalDateTime updateTime;"
        ]
    },
    "AccountSecurityStatus": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String status;",
            "private String securityLevel;",
            "private LocalDateTime lastSecurityCheck;",
            "private Map<String, Object> securityFlags;"
        ]
    },
    "AccountSecurityScore": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private Integer totalScore;",
            "private String securityLevel;",
            "private Map<String, Integer> componentScores;",
            "private LocalDateTime calculatedTime;"
        ]
    },
    "AccountSecurityEvent": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long eventId;",
            "private Long userId;",
            "private String eventType;",
            "private String eventDescription;",
            "private LocalDateTime eventTime;",
            "private String ipAddress;",
            "private String userAgent;"
        ]
    },
    "AccountSecurityConfig": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long configId;",
            "private String configKey;",
            "private String configValue;",
            "private String description;",
            "private Boolean enabled;",
            "private LocalDateTime updateTime;"
        ]
    },
    "AccountSecurityReport": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String reportId;",
            "private String reportType;",
            "private LocalDateTime reportTime;",
            "private Integer totalEvents;",
            "private Map<String, Integer> eventCounts;",
            "private String summary;"
        ]
    },
    "AccountRiskInfo": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String riskLevel;",
            "private List<String> riskFactors;",
            "private Double riskScore;",
            "private LocalDateTime lastAssessmentTime;",
            "private Map<String, Object> riskDetails;"
        ]
    },
    "OperationPermissionResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String operation;",
            "private Boolean hasPermission;",
            "private String reason;",
            "private LocalDateTime checkTime;"
        ]
    },
    "EmergencyContact": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long contactId;",
            "private Long userId;",
            "private String contactName;",
            "private String contactPhone;",
            "private String relationship;",
            "private Boolean isPrimary;",
            "private LocalDateTime createTime;"
        ]
    },
    # 消费限制配置相关
    "ConsumeLimitConfig": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long configId;",
            "private Long userId;",
            "private String limitType;",
            "private BigDecimal dailyLimit;",
            "private BigDecimal monthlyLimit;",
            "private Boolean enabled;",
            "private LocalDateTime createTime;",
            "private LocalDateTime updateTime;"
        ]
    },
    "LimitValidationResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private BigDecimal requestAmount;",
            "private BigDecimal remainingLimit;",
            "private Boolean isValid;",
            "private String validationType;",
            "private String message;"
        ]
    },
    "ConsumeStatistics": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String statisticsType;",
            "private BigDecimal totalAmount;",
            "private Integer transactionCount;",
            "private LocalDateTime startDate;",
            "private LocalDateTime endDate;"
        ]
    },
    "LimitCheckResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String limitType;",
            "private BigDecimal currentUsage;",
            "private BigDecimal limitAmount;",
            "private BigDecimal remainingAmount;",
            "private Boolean withinLimit;"
        ]
    },
    "LimitChangeHistory": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long historyId;",
            "private Long userId;",
            "private String limitType;",
            "private BigDecimal oldLimit;",
            "private BigDecimal newLimit;",
            "private String operatorId;",
            "private LocalDateTime changeTime;"
        ]
    },
    "BatchLimitSetting": {
        "package": "net.lab1024.sa.admin.module.consume.domain.dto",
        "type": "dto",
        "fields": [
            "private List<Long> userIds;",
            "private String limitType;",
            "private BigDecimal limitAmount;",
            "private String operatorId;",
            "private String remarks;"
        ]
    },
    "BatchLimitSetResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String batchId;",
            "private Integer totalCount;",
            "private Integer successCount;",
            "private Integer failureCount;",
            "private List<String> failedUserIds;",
            "private LocalDateTime operationTime;"
        ]
    },
    "GlobalLimitConfig": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long configId;",
            "private String configType;",
            "private BigDecimal defaultValue;",
            "private BigDecimal maxValue;",
            "private String description;",
            "private Boolean enabled;"
        ]
    },
    "LimitUsageReport": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String reportType;",
            "private LocalDateTime reportTime;",
            "private Integer totalUsers;",
            "private Integer usersWithLimit;",
            "private BigDecimal totalLimitUsage;",
            "private Map<String, Object> breakdown;"
        ]
    },
    "LimitConflictCheckResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private Boolean hasConflict;",
            "private List<String> conflictTypes;",
            "private String description;",
            "private Map<String, Object> conflictDetails;"
        ]
    },
    # 支付密码相关
    "PaymentPasswordResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private Long userId;",
            "private String operation;",
            "private Boolean success;",
            "private String message;",
            "private LocalDateTime operationTime;",
            "private Map<String, Object> details;"
        ]
    },
    "PasswordSecurityPolicy": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long policyId;",
            "private String policyName;",
            "private Integer minLength;",
            "private Boolean requireSpecialChars;",
            "private Boolean requireNumbers;",
            "private Integer maxRetryAttempts;",
            "private Integer lockoutDuration;"
        ]
    },
    "PasswordStrengthResult": {
        "package": "net.lab1024.sa.admin.module.consume.domain.vo",
        "type": "vo",
        "fields": [
            "private String password;",
            "private Integer strengthScore;",
            "private String strengthLevel;",
            "private List<String> weaknessReasons;",
            "private List<String> suggestions;"
        ]
    },
    "PasswordVerificationHistory": {
        "package": "net.lab1024.sa.admin.module.consume.domain.entity",
        "type": "entity",
        "fields": [
            "private Long historyId;",
            "private Long userId;",
            "private String verificationType;",
            "private Boolean success;",
            "private String failureReason;",
            "private String ipAddress;",
            "private LocalDateTime verificationTime;"
        ]
    }
}

def generate_java_class(class_name, class_info):
    """生成Java类代码"""
    package_name = class_info["package"]
    class_type = class_info["type"]
    fields = class_info["fields"]

    # 确定导入
    imports = []
    if "LocalDateTime" in str(fields):
        imports.append("java.time.LocalDateTime")
    if "BigDecimal" in str(fields):
        imports.append("java.math.BigDecimal")
    if "List" in str(fields):
        imports.append("java.util.List")
    if "Map" in str(fields):
        imports.append("java.util.Map")

    # 生成类内容
    class_content = f"""package {package_name};

import lombok.Data;
"""

    # 添加导入
    for imp in sorted(set(imports)):
        class_content += f"import {imp};\n"

    class_content += f"""

/**
 * {class_name}
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Data
public class {class_name} {{
"""

    # 添加字段
    for field in fields:
        class_content += f"    {field}\n"

    class_content += """
}
"""

    return class_content

def main():
    """主函数"""
    print("开始批量生成缺失的Java类...")

    created_count = 0

    for class_name, class_info in MISSING_CLASSES.items():
        try:
            # 确定文件路径
            package_parts = class_info["package"].split(".")
            relative_path = "/".join(package_parts)

            # 创建目录
            dir_path = SA_ADMIN_ROOT / relative_path
            dir_path.mkdir(parents=True, exist_ok=True)

            # 生成文件路径
            file_path = dir_path / f"{class_name}.java"

            # 生成类内容
            class_content = generate_java_class(class_name, class_info)

            # 写入文件
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(class_content)

            print(f"✓ 创建类: {class_name} -> {file_path}")
            created_count += 1

        except Exception as e:
            print(f"✗ 创建类失败: {class_name}, 错误: {e}")

    print(f"\n批量生成完成！共创建 {created_count} 个类")

if __name__ == "__main__":
    main()
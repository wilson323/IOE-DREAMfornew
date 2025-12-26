# P0-6 设备质量诊断功能实施指南

**📅 创建时间**: 2025-12-26
**👯‍♂️ 工作量**: 4人天
**⭐ 优先级**: P0级核心功能
**🎯 目标**: 实现智慧园区设备质量自动诊断和告警功能

---

## 📊 功能需求概述

### 核心功能
1. **设备健康度评估** - 综合评估设备运行状态
2. **质量诊断分析** - 分析设备故障和异常
3. **预测性维护** - 提前预警设备故障风险
4. **质量报告生成** - 定期生成设备质量报告
5. **维护建议推送** - 自动推送维护建议

### 技术方案
- **诊断规则引擎** - 基于规则的质量评分
- **时间序列分析** - 设备性能趋势分析
- **异常检测算法** - 统计学异常检测
- **告警机制** - 多级告警推送

---

## 🏗️ 后端架构设计

### 目录结构
```
ioedream-consume-service/src/main/java/net/lab1024/sa/consume/
├── controller/
│   └── device/                         # 设备管理控制器
│       └── DeviceQualityController.java
├── service/
│   └── device/                         # 设备服务
│       ├── DeviceQualityService.java
│       └── impl/
│           └── DeviceQualityServiceImpl.java
├── manager/
│   └── device/                         # 设备管理器
│       ├── DeviceQualityManager.java    # 质量诊断管理
│       ├── QualityRuleEngine.java       # 质量规则引擎
│       └── AnomalyDetector.java         # 异常检测器
└── dao/
    └── device/                         # 设备数据访问
        ├── DeviceQualityDao.java        # 质量记录DAO
        └── DeviceHealthMetricDao.java   # 健康指标DAO
```

---

## 📝 开发步骤

### 步骤1: 数据库设计（0.5天）
- [ ] 创建设备质量记录表（t_device_quality_record）
- [ ] 创建设备健康指标表（t_device_health_metric）
- [ ] 创建质量诊断规则表（t_quality_diagnosis_rule）
- [ ] 创建质量告警表（t_quality_alarm）

### 步骤2: Entity实体层（0.5天）
- [ ] DeviceQualityRecordEntity - 质量记录实体
- [ ] DeviceHealthMetricEntity - 健康指标实体
- [ ] QualityDiagnosisRuleEntity - 诊断规则实体
- [ ] QualityAlarmEntity - 质量告警实体

### 步骤3: DAO数据访问层（0.5天）
- [ ] DeviceQualityDao
- [ ] DeviceHealthMetricDao
- [ ] QualityDiagnosisRuleDao
- [ ] QualityAlarmDao

### 步骤4: Manager业务编排层（1天）
- [ ] DeviceQualityManager - 质量诊断管理器
- [ ] QualityRuleEngine - 规则引擎（300行）
- [ ] AnomalyDetector - 异常检测器（200行）

### 步骤5: Service服务层（0.5天）
- [ ] DeviceQualityService - 质量诊断服务
- [ ] 实现质量评分算法
- [ ] 实现告警生成逻辑

### 步骤6: Controller控制器层（0.5天）
- [ ] DeviceQualityController - 质量诊断API
- [ ] 实现质量查询接口
- [ ] 实现质量报告接口

### 步骤7: 测试验证（0.5天）
- [ ] 单元测试
- [ ] 集成测试
- [ ] 质量评分算法验证

---

## 🎨 核心算法设计

### 健康度评分算法
```java
/**
 * 设备健康度评分（0-100分）
 */
public Integer calculateHealthScore(DeviceEntity device, List<HealthMetric> metrics) {
    double score = 100.0;

    // 1. 在线状态评分（权重30%）
    int statusScore = calculateStatusScore(device.getStatus());
    score = score * 0.3 + statusScore * 0.3;

    // 2. 性能指标评分（权重40%）
    double performanceScore = calculatePerformanceScore(metrics);
    score = score * 0.4 + performanceScore * 0.4;

    // 3. 故障历史评分（权重20%）
    double faultScore = calculateFaultScore(device.getDeviceId());
    score = score * 0.2 + faultScore * 0.2;

    // 4. 维护记录评分（权重10%）
    double maintenanceScore = calculateMaintenanceScore(device.getDeviceId());
    score = score * 0.1 + maintenanceScore * 0.1;

    return (int) Math.round(score);
}

/**
 * 在线状态评分
 */
private int calculateStatusScore(Integer status) {
    if (status == 1) return 100;      // 在线
    if (status == 2) return 60;       // 离线
    if (status == 3) return 20;       // 故障
    return 0;                          // 停用
}
```

### 异常检测算法
```java
/**
 * 统计学异常检测（3-Sigma原则）
 */
public boolean detectAnomaly(List<HealthMetric> metrics, HealthMetric current) {
    if (metrics.size() < 30) {
        return false;  // 数据不足，无法判断
    }

    // 计算均值和标准差
    double mean = metrics.stream()
        .mapToDouble(HealthMetric::getValue)
        .average()
        .orElse(0.0);

    double stddev = Math.sqrt(
        metrics.stream()
            .mapToDouble(m -> Math.pow(m.getValue() - mean, 2))
            .average()
            .orElse(0.0)
    );

    // 3-Sigma原则：超出3个标准差视为异常
    double zscore = (current.getValue() - mean) / stddev;
    return Math.abs(zscore) > 3.0;
}
```

### 质量等级判定
```java
/**
 * 质量等级判定
 */
public String getQualityLevel(Integer healthScore) {
    if (healthScore >= 90) return "优秀";  // A级
    if (healthScore >= 80) return "良好";  // B级
    if (healthScore >= 60) return "合格";  // C级
    if (healthScore >= 40) return "较差";  // D级
    return "危险";                          // E级
}
```

---

## 🔌 API接口设计

### 质量诊断API
```java
@RestController
@RequestMapping("/api/device/quality")
public class DeviceQualityController {

    /**
     * 获取设备质量评分
     */
    @GetMapping("/{deviceId}/score")
    public ResponseDTO<Map<String, Object>> getQualityScore(@PathVariable String deviceId);

    /**
     * 批量获取设备质量评分
     */
    @PostMapping("/batch/score")
    public ResponseDTO<List<Map<String, Object>>> getBatchQualityScore(
        @RequestBody List<String> deviceIds);

    /**
     * 获取设备健康趋势
     */
    @GetMapping("/{deviceId}/trend")
    public ResponseDTO<List<HealthMetric>> getHealthTrend(
        @PathVariable String deviceId,
        @RequestParam Integer days);

    /**
     * 执行质量诊断
     */
    @PostMapping("/{deviceId}/diagnose")
    public ResponseDTO<Map<String, Object>> diagnoseDevice(@PathVariable String deviceId);

    /**
     * 获取质量告警列表
     */
    @GetMapping("/alarms")
    public ResponseDTO<PageResult<QualityAlarm>> getAlarms(
        @RequestParam(required = false) Integer level,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize);

    /**
     * 生成质量报告
     */
    @PostMapping("/report/generate")
    public ResponseDTO<Long> generateQualityReport(
        @RequestBody QualityReportForm form);
}
```

---

## 📦 数据库设计

### 1. 设备质量记录表
```sql
CREATE TABLE t_device_quality_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    health_score INT COMMENT '健康评分(0-100)',
    quality_level VARCHAR(20) COMMENT '质量等级(优秀/良好/合格/较差/危险)',
    diagnosis_result TEXT COMMENT '诊断结果(JSON)',
    alarm_level INT COMMENT '告警级别(0-无 1-低 2-中 3-高 4-紧急)',
    diagnosis_time DATETIME NOT NULL COMMENT '诊断时间',
    create_time DATETIME NOT NULL,
    INDEX idx_device_id (device_id),
    INDEX idx_diagnosis_time (diagnosis_time),
    INDEX idx_health_score (health_score)
) COMMENT='设备质量诊断记录表';
```

### 2. 设备健康指标表
```sql
CREATE TABLE t_device_health_metric (
    metric_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    metric_type VARCHAR(50) NOT NULL COMMENT '指标类型(cpu/memory/temperature/delay)',
    metric_value DECIMAL(10,2) NOT NULL COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME NOT NULL,
    INDEX idx_device_type_time (device_id, metric_type, collect_time),
    INDEX idx_collect_time (collect_time)
) COMMENT='设备健康指标表';
```

### 3. 质量诊断规则表
```sql
CREATE TABLE t_quality_diagnosis_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(100) NOT NULL COMMENT '规则编码',
    device_type INT COMMENT '设备类型(1-门禁 2-考勤 3-消费 4-视频 5-访客)',
    metric_type VARCHAR(50) COMMENT '指标类型',
    rule_expression VARCHAR(500) COMMENT '规则表达式',
    alarm_level INT COMMENT '告警级别',
    rule_status TINYINT DEFAULT 1 COMMENT '规则状态(1-启用 0-禁用)',
    create_time DATETIME NOT NULL,
    INDEX idx_device_type (device_type),
    INDEX idx_rule_status (rule_status)
) COMMENT='质量诊断规则表';
```

### 4. 质量告警表
```sql
CREATE TABLE t_quality_alarm (
    alarm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    rule_id BIGINT COMMENT '触发的规则ID',
    alarm_level INT COMMENT '告警级别',
    alarm_title VARCHAR(200) COMMENT '告警标题',
    alarm_content TEXT COMMENT '告警内容',
    alarm_status TINYINT DEFAULT 1 COMMENT '告警状态(1-待处理 2-处理中 3-已处理)',
    handle_result TEXT COMMENT '处理结果',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME NOT NULL,
    INDEX idx_device_id (device_id),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_alarm_status (alarm_status),
    INDEX idx_create_time (create_time)
) COMMENT='设备质量告警表';
```

---

## ✅ 验收标准

### 功能验收
- [ ] 设备健康度评分准确（0-100分）
- [ ] 质量等级判定合理（5个等级）
- [ ] 异常检测有效（3-Sigma算法）
- [ ] 告警生成及时
- [ ] 质量报告完整

### 性能验收
- [ ] 质量评分响应时间 < 500ms
- [ ] 批量评分支持100+设备
- [ ] 健康趋势查询性能良好
- [ ] 异常检测准确率 > 90%

### 代码质量
- [ ] 严格遵循四层架构规范
- [ ] Jakarta EE 9+规范
- [ ] 完整的单元测试覆盖
- [ ] 代码注释完整

---

## 🚀 实施优先级

**P0核心功能（必须完成）**:
1. 数据库表设计和创建
2. 健康度评分算法
3. 质量等级判定
4. 告警生成逻辑
5. REST API接口

**P1增强功能（可选）**:
1. 异常检测算法
2. 健康趋势分析
3. 预测性维护
4. 质量报告生成

**P2优化功能（可选）**:
1. 机器学习模型集成
2. 智能告警聚合
3. 维护建议推送
4. 自动化工单生成

---

**📅 预计完成时间**: 4个工作日
**👥 开发人员**: 后端工程师
**🎯 里程碑**: 每日下班前提交代码并演示进度

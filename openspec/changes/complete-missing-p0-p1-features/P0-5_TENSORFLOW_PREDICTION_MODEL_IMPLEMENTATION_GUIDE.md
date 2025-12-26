# P0-5 TensorFlow预测模型集成实施指南

**📅 创建时间**: 2025-12-26
**👯‍♂️ 工作量**: 6人天
**⭐ 优先级**: P0级核心功能
**🎯 目标**: 集成TensorFlow进行考勤预测和异常检测

---

## 📊 功能需求概述

### 核心功能
1. **考勤预测** - 基于历史数据预测未来考勤趋势
2. **异常检测** - 使用ML模型检测考勤异常行为
3. **早退迟到预测** - 预测员工可能的迟到/早退行为
4. **排班优化建议** - 基于预测结果提供排班优化建议
5. **模型训练** - 支持模型训练和版本管理

### 技术方案
- **TensorFlow 2.x**: 机器学习框架
- **LSTM神经网络**: 时间序列预测
- **异常检测算法**: Isolation Forest / One-Class SVM
- **特征工程**: 时间特征、统计特征、行为特征
- **模型部署**: TensorFlow SavedModel格式

---

## 🏗️ 系统架构设计

### TensorFlow核心概念
```
1. Model（模型）: 训练好的预测模型
2. Feature（特征）: 输入模型的特征向量
3. Prediction（预测）: 模型输出结果
4. Training（训练）: 模型训练过程
5. Inference（推理）: 模型预测推理
```

### 目录结构
```
ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/
├── controller/
│   └── prediction/                      # 预测管理
│       └── AttendancePredictionController.java
├── service/
│   └── prediction/                      # 预测服务
│       ├── AttendancePredictionService.java
│       └── impl/
│           └── AttendancePredictionServiceImpl.java
├── manager/
│   └── prediction/                      # 预测管理器
│       ├── ModelTrainingManager.java    # 模型训练
│       ├── FeatureEngineeringManager.java # 特征工程
│       ├── PredictionManager.java       # 预测执行
│       └── AnomalyDetectionManager.java # 异常检测
└── model/                              # 模型相关
    ├── PredictionModel.java             # 预测模型接口
    ├── LstmPredictor.java               # LSTM预测器
    └── AnomalyDetector.java             # 异常检测器
```

---

## 📝 开发步骤

### 步骤1: TensorFlow依赖配置（0.5天）
- [ ] 添加TensorFlow依赖到pom.xml
- [ ] 添加ND4J依赖（数值计算）
- [ ] 验证依赖安装

### 步骤2: 数据库设计（0.5天）
- [ ] 创建预测任务表（t_prediction_task）
- [ ] 创建预测结果表（t_prediction_result）
- [ ] 创建模型版本表（t_model_version）
- [ ] 创建训练数据表（t_training_data）

### 步骤3: 特征工程（1.5天）
- [ ] FeatureEngineeringManager - 特征提取器
- [ ] 时间特征提取（星期、月份、节假日）
- [ ] 统计特征提取（均值、方差、趋势）
- [ ] 行为特征提取（迟到率、早退率）

### 步骤4: LSTM模型实现（2天）
- [ ] LstmPredictor - LSTM预测器
- [ ] 模型架构设计（输入层、隐藏层、输出层）
- [ ] 模型训练流程
- [ ] 模型保存和加载

### 步骤5: 异常检测实现（1天）
- [ ] AnomalyDetectionManager - 异常检测管理器
- [ ] Isolation Forest算法
- [ ] 异常评分系统
- [ ] 告警生成

### 步骤6: Service和Controller层（0.5天）
- [ ] AttendancePredictionService - 预测服务
- [ ] AttendancePredictionController - REST API
- [ ] 预测结果查询和展示

---

## 🔧 Maven依赖配置

```xml
<!-- TensorFlow 2.x -->
<dependency>
    <groupId>org.tensorflow</groupId>
    <artifactId>tensorflow-core-platform</artifactId>
    <version>0.4.2</version>
</dependency>
<dependency>
    <groupId>org.tensorflow</groupId>
    <artifactId>tensorflow-core-api</artifactId>
    <version>0.4.2</version>
</dependency>

<!-- ND4J (数值计算) -->
<dependency>
    <groupId>org.nd4j</groupId>
    <artifactId>nd4j-native-platform</artifactId>
    <version>1.0.0-beta7</version>
</dependency>

<!-- Java ML库 (可选) -->
<dependency>
    <groupId>com.github.haifengl</groupId>
    <artifactId>smile-core</artifactId>
    <version>3.0.2</version>
</dependency>
```

---

## 🎨 模型设计

### 1. LSTM时间序列预测模型

**模型架构**:
```java
public class LstmPredictor {

    /**
     * 模型结构
     * Input Layer: [batch_size, time_steps, features]
     * LSTM Layer 1: 128 units, return_sequences=True
     * Dropout Layer 1: 0.2
     * LSTM Layer 2: 64 units, return_sequences=False
     * Dropout Layer 2: 0.2
     * Dense Layer: 32 units, ReLU activation
     * Output Layer: 1 unit, Linear activation
     */

    /**
     * 训练模型
     */
    public void train(List<double[]> historicalData, int epochs) {
        // 数据预处理
        // 构建LSTM网络
        // 编译模型 (Optimizer: Adam, Loss: MSE)
        // 训练模型
        // 保存模型
    }

    /**
     * 预测未来值
     */
    public List<Double> predict(List<double[]> historicalData, int futureSteps) {
        // 加载模型
        // 数据预处理
        // 执行预测
        // 返回预测结果
        return predictions;
    }
}
```

### 2. 特征工程

**特征列表**:
- **时间特征**: 星期几、月份、是否工作日、是否节假日
- **统计特征**: 过去7天平均值、过去30天标准差、趋势斜率
- **行为特征**: 迟到率、早退率、缺勤率、加班频率

```java
public class FeatureEngineeringManager {

    /**
     * 提取特征
     */
    public double[] extractFeatures(AttendanceRecordEntity record) {
        double[] features = new double[15];

        // 时间特征 (5维)
        features[0] = record.getCheckInTime().getDayOfWeek().getValue(); // 星期
        features[1] = record.getCheckInTime().getMonthValue();         // 月份
        features[2] = isWorkDay(record.getCheckInTime()) ? 1.0 : 0.0;  // 工作日
        features[3] = isHoliday(record.getCheckInTime()) ? 1.0 : 0.0;   // 节假日
        features[4] = record.getShiftType();                           // 班次类型

        // 统计特征 (7维)
        features[5] = calculateAverageAttendance(record.getEmployeeId(), 7);  // 7天平均出勤
        features[6] = calculateStandardDeviation(record.getEmployeeId(), 30); // 30天标准差
        features[7] = calculateTrend(record.getEmployeeId(), 14);             // 14天趋势
        features[8] = calculateLateRate(record.getEmployeeId(), 30);          // 30天迟到率
        features[9] = calculateEarlyLeaveRate(record.getEmployeeId(), 30);   // 30天早退率
        features[10] = calculateAbsenceRate(record.getEmployeeId(), 30);      // 30天缺勤率
        features[11] = calculateOvertimeFrequency(record.getEmployeeId(), 30); // 30天加班频率

        return features;
    }
}
```

### 3. 异常检测模型

**Isolation Forest算法**:
```java
public class AnomalyDetectionManager {

    /**
     * 训练异常检测模型
     */
    public void trainModel(List<double[]> normalData) {
        // 使用Isolation Forest算法
        // 训练正常行为模型
        // 保存模型
    }

    /**
     * 检测异常
     */
    public boolean isAnomalous(double[] features) {
        // 加载模型
        // 计算异常分数
        // 判断是否异常 (阈值: 0.5)
        double anomalyScore = model.score(features);
        return anomalyScore > 0.5;
    }
}
```

---

## 📊 REST API设计

### 预测API
```java
@RestController
@RequestMapping("/api/prediction")
public class AttendancePredictionController {

    /**
     * 训练预测模型
     */
    @PostMapping("/model/train")
    public ResponseDTO<Long> trainModel(@RequestBody ModelTrainingForm form);

    /**
     * 执行考勤预测
     */
    @PostMapping("/predict")
    public ResponseDTO<List<PredictionResultVO>> predict(@RequestBody PredictionForm form);

    /**
     * 获取预测结果
     */
    @GetMapping("/result/{taskId}")
    public ResponseDTO<PredictionResultVO> getResult(@PathVariable Long taskId);

    /**
     * 检测考勤异常
     */
    @PostMapping("/anomaly/detect")
    public ResponseDTO<List<AnomalyVO>> detectAnomalies(@RequestBody AnomalyDetectionForm form);

    /**
     * 获取模型版本列表
     */
    @GetMapping("/model/versions")
    public ResponseDTO<List<ModelVersionVO>> getModelVersions();

    /**
     * 导出模型
     */
    @PostMapping("/model/{modelId}/export")
    public ResponseDTO<String> exportModel(@PathVariable Long modelId);

    /**
     * 导入模型
     */
    @PostMapping("/model/import")
    public ResponseDTO<Long> importModel(@RequestParam MultipartFile file);
}
```

---

## ✅ 验收标准

### 功能验收
- [ ] TensorFlow模型成功集成
- [ ] 能够训练LSTM预测模型
- [ ] 能够执行考勤预测
- [ ] 异常检测算法有效
- [ ] 预测准确率可接受（>80%）

### 性能验收
- [ ] 模型训练时间 < 30分钟（1000条数据）
- [ ] 预测推理时间 < 1秒
- [ ] 内存占用合理（< 4GB）
- [ ] 支持模型版本管理

### 代码质量
- [ ] 严格遵循四层架构规范
- [ ] TensorFlow模型设计规范
- [ ] 特征工程完整
- [ ] 代码注释完整

---

## 🚀 实施优先级

**P0核心功能（必须完成）**:
1. TensorFlow依赖集成
2. 核心特征工程（时间、统计、行为特征）
3. LSTM预测模型（基本架构）
4. 简单异常检测（统计方法）
5. REST API接口

**P1增强功能（可选）**:
1. 复杂特征工程（高级特征）
2. 模型超参数调优
3. 模型性能监控
4. 模型A/B测试

**P2优化功能（可选）**:
1. 分布式训练
2. GPU加速
3. 在线学习
4. 模型解释性

---

**📅 预计完成时间**: 6个工作日
**👥 开发人员**: 后端工程师（熟悉TensorFlow）
**🎯 里程碑**: 每日下班前提交代码并演示进度

# 📊 数据处理专家技能

> **版本**: v1.0.0 - 企业级数据处理
> **更新时间**: 2025-11-23
> **分类**: 数据处理技能 > 大数据治理
> **标签**: ["数据清洗", "ETL流程", "数据质量", "数据分析"]
> **技能等级**: ★★★ 专家级
> **适用角色**: 数据工程师、数据分析师、ETL开发工程师

---

## 📋 技能概述

本技能专门解决大数据处理、数据质量保证和数据治理问题，构建完整的数据处理流水线，确保数据的准确性、完整性和及时性。

**核心能力**: 建立从数据采集、清洗、转换到分析的数据全生命周期管理体系，实现数据驱动决策。

## 🚨 当前数据处理问题分析

### 1. 数据质量问题
**问题现象**:
```java
// 重复数据
List<User> users = userRepository.findAll();  // 包含重复记录

// 数据不一致
User user1 = userRepository.findById(1L);  // name="张三"
User user2 = cacheService.get(1L);         // name="李四"  // 同一用户不同数据

// 数据缺失
User user = userRepository.findById(1L);
// user.getPhone() == null  // 关键字段缺失
```

**根本原因**:
- 缺乏数据质量检查机制
- 没有统一的数据标准
- 缺乏数据治理流程

### 2. 数据处理效率低下
**问题现象**:
```java
// 低效的数据处理
public List<Report> generateReport() {
    List<Order> orders = orderRepository.findAll();  // 加载所有数据
    List<Report> reports = new ArrayList<>();

    for (Order order : orders) {  // 内存中处理大数据集
        Report report = processOrder(order);
        reports.add(report);
    }
    return reports;
}
```

### 3. ETL流程不规范
**问题现象**:
- 数据抽取、转换、加载过程缺乏监控
- 错误处理机制不完善
- 数据血缘关系不清晰

## 🛠️ 数据处理最佳实践

### 1. 数据质量管理系统

#### 数据质量检查器
```java
@Component
@Slf4j
public class DataQualityChecker {

    @Resource
    private List<DataQualityRule> qualityRules;

    /**
     * 执行数据质量检查
     */
    public DataQualityReport checkDataQuality(String tableName, Map<String, Object> data) {
        DataQualityReport report = new DataQualityReport();
        report.setTableName(tableName);
        report.setCheckTime(System.currentTimeMillis());

        List<QualityIssue> issues = new ArrayList<>();

        // 执行所有质量规则
        for (DataQualityRule rule : qualityRules) {
            try {
                QualityIssue issue = rule.evaluate(tableName, data);
                if (issue != null) {
                    issues.add(issue);
                }
            } catch (Exception e) {
                log.error("数据质量规则执行失败: rule={}", rule.getClass().getSimpleName(), e);
            }
        }

        report.setIssues(issues);
        report.setTotalIssues(issues.size());
        report.setQualityScore(calculateQualityScore(issues));

        log.info("数据质量检查完成: table={}, issues={}, score={}",
                tableName, issues.size(), report.getQualityScore());

        return report;
    }

    /**
     * 批量数据质量检查
     */
    public BatchQualityReport batchCheckQuality(String tableName, List<Map<String, Object>> dataList) {
        BatchQualityReport report = new BatchQualityReport();
        report.setTableName(tableName);
        report.setTotalRecords(dataList.size());

        Map<QualityIssueType, Integer> issueCounts = new HashMap<>();
        List<Map<String, Object>> invalidRecords = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            DataQualityRecord record = new DataQualityRecord();
            record.setRecordIndex(i);
            record.setData(data);

            DataQualityRecordQualityResult recordReport = checkRecordQuality(tableName, data);
            record.setQualityReport(recordReport);

            if (!recordReport.isValid()) {
                invalidRecords.add(data);

                // 统计问题类型
                for (QualityIssue issue : recordReport.getIssues()) {
                    issueCounts.merge(issue.getType(), 1, Integer::sum);
                }
            }
        }

        report.setInvalidRecords(invalidRecords);
        report.setInvalidRecordCount(invalidRecords.size());
        report.setValidRecordCount(dataList.size() - invalidRecords.size());
        report.setIssueCounts(issueCounts);
        report.setQualityScore(calculateBatchQualityScore(report));

        return report;
    }

    /**
     * 数据质量规则：必填字段检查
     */
    @Component
    public static class RequiredFieldRule implements DataQualityRule {

        @Override
        public QualityIssue evaluate(String tableName, Map<String, Object> data) {
            // 根据表名确定必填字段
            Set<String> requiredFields = getRequiredFields(tableName);

            for (String field : requiredFields) {
                Object value = data.get(field);
                if (value == null || StringUtils.isEmpty(value.toString())) {
                    QualityIssue issue = new QualityIssue();
                    issue.setType(QualityIssueType.MISSING_REQUIRED_FIELD);
                    issue.setFieldName(field);
                    issue.setDescription("必填字段缺失: " + field);
                    issue.setSeverity(Severity.HIGH);
                    issue.setSuggestion("请提供必填字段: " + field);

                    return issue;
                }
            }

            return null; // 没有问题
        }

        private Set<String> getRequiredFields(String tableName) {
            switch (tableName) {
                case "users":
                    return Set.of("username", "email", "password");
                case "orders":
                    return Set.of("user_id", "order_no", "amount");
                case "products":
                    return Set.of("product_name", "price", "category");
                default:
                    return Collections.emptySet();
            }
        }
    }

    /**
     * 数据质量规则：数据格式检查
     */
    @Component
    public static class DataFormatRule implements DataQualityRule {

        @Override
        public QualityIssue evaluate(String tableName, Map<String, Object> data) {
            // 检查邮箱格式
            String email = (String) data.get("email");
            if (email != null && !isValidEmail(email)) {
                QualityIssue issue = new QualityIssue();
                issue.setType(QualityIssueType.INVALID_FORMAT);
                issue.setFieldName("email");
                issue.setDescription("邮箱格式不正确: " + email);
                issue.setSeverity(Severity.MEDIUM);
                issue.setSuggestion("请使用正确的邮箱格式");

                return issue;
            }

            // 检查手机号格式
            String phone = (String) data.get("phone");
            if (phone != null && !isValidPhone(phone)) {
                QualityIssue issue = new QualityIssue();
                issue.setType(QualityIssueType.INVALID_FORMAT);
                issue.setFieldName("phone");
                issue.setDescription("手机号格式不正确: " + phone);
                issue.setSeverity(Severity.MEDIUM);
                issue.setSuggestion("请使用正确的手机号格式");

                return issue;
            }

            return null;
        }

        private boolean isValidEmail(String email) {
            return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        }

        private boolean isValidPhone(String phone) {
            return phone.matches("^1[3-9]\\d{9}$");
        }
    }
}
```

#### 数据清洗处理器
```java
@Service
@Slf4j
public class DataCleaner {

    @Resource
    private DataQualityChecker qualityChecker;

    /**
     * 清洗单条数据
     */
    public Map<String, Object> cleanData(String tableName, Map<String, Object> rawData) {
        if (rawData == null || rawData.isEmpty()) {
            return rawData;
        }

        Map<String, Object> cleanedData = new HashMap<>(rawData);

        // 1. 去除前后空格
        trimStringValues(cleanedData);

        // 2. 处理null值
        handleNullValues(cleanedData, tableName);

        // 3. 标准化数据格式
        standardizeDataFormats(cleanedData);

        // 4. 去重处理
        removeDuplicates(cleanedData);

        log.debug("数据清洗完成: table={}, fields={}", tableName, cleanedData.keySet());

        return cleanedData;
    }

    /**
     * 批量数据清洗
     */
    public List<Map<String, Object>> batchCleanData(String tableName, List<Map<String, Object>> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return rawDataList;
        }

        return rawDataList.parallelStream()
                .map(data -> cleanData(tableName, data))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void trimStringValues(Map<String, Object> data) {
        data.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof String) {
                String value = ((String) entry.getValue()).trim();
                entry.setValue(value.isEmpty() ? null : value);
            }
        });
    }

    private void handleNullValues(Map<String, Object> data, String tableName) {
        // 根据表类型处理null值
        switch (tableName) {
            case "users":
                handleUserNullValues(data);
                break;
            case "orders":
                handleOrderNullValues(data);
                break;
            case "products":
                handleProductNullValues(data);
                break;
        }
    }

    private void handleUserNullValues(Map<String, Object> data) {
        // 为缺失的必填字段设置默认值
        if (data.get("status") == null) {
            data.put("status", "active");
        }
        if (data.get("created_time") == null) {
            data.put("created_time", System.currentTimeMillis());
        }
    }

    private void standardizeDataFormats(Map<String, Object> data) {
        // 标准化日期格式
        standardizeDateFormat(data, "created_time");
        standardizeDateFormat(data, "updated_time");

        // 标准化数字格式
        standardizeNumberFormat(data, "amount");
        standardizeNumberFormat(data, "price");

        // 标准化布尔格式
        standardizeBooleanFormat(data, "active");
        standardizeBooleanFormat(data, "enabled");
    }

    private void standardizeDateFormat(Map<String, Object> data, String fieldName) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof Long)) {
            try {
                if (value instanceof String) {
                    LocalDateTime dateTime = LocalDateTime.parse((String) value);
                    data.put(fieldName, dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                } else if (value instanceof Date) {
                    data.put(fieldName, ((Date) value).getTime());
                }
            } catch (Exception e) {
                log.warn("日期格式标准化失败: field={}, value={}", fieldName, value);
            }
        }
    }

    private void standardizeNumberFormat(Map<String, Object> data, String fieldName) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof Number)) {
            try {
                if (value instanceof String) {
                    data.put(fieldName, Double.parseDouble((String) value));
                }
            } catch (Exception e) {
                log.warn("数字格式标准化失败: field={}, value={}", fieldName, value);
            }
        }
    }

    private void standardizeBooleanFormat(Map<String, Object> data, String fieldName) {
        Object value = data.get(fieldName);
        if (value != null && !(value instanceof Boolean)) {
            if (value instanceof Integer) {
                data.put(fieldName, ((Integer) value) != 0);
            } else if (value instanceof String) {
                String strValue = ((String) value).toLowerCase();
                data.put(fieldName, Arrays.asList("true", "1", "yes", "on").contains(strValue));
            }
        }
    }
}
```

### 2. 高效ETL处理系统

#### ETL任务调度器
```java
@Service
@Slf4j
public class ETLTaskScheduler {

    @Resource
    private TaskExecutor etlTaskExecutor;

    @Resource
    private DataExtractor dataExtractor;

    @Resource
    private DataTransformer dataTransformer;

    @Resource
    private DataLoader dataLoader;

    /**
     * 执行ETL任务
     */
    @Async("etlTaskExecutor")
    public CompletableFuture<ETLResult> executeETLTask(ETLTaskConfig config) {
        log.info("开始执行ETL任务: {}", config.getTaskName());

        ETLResult result = new ETLResult();
        result.setTaskName(config.getTaskName());
        result.setStartTime(System.currentTimeMillis());

        try {
            // 1. 数据抽取 (Extract)
            log.debug("开始数据抽取: source={}", config.getDataSource());
            ExtractResult extractResult = dataExtractor.extract(config.getExtractConfig());
            result.setExtractResult(extractResult);

            if (extractResult.isSuccess()) {
                // 2. 数据转换 (Transform)
                log.debug("开始数据转换: records={}", extractResult.getRecordCount());
                TransformResult transformResult = dataTransformer.transform(
                        extractResult.getData(), config.getTransformConfig());
                result.setTransformResult(transformResult);

                if (transformResult.isSuccess()) {
                    // 3. 数据加载 (Load)
                    log.debug("开始数据加载: target={}", config.getDataTarget());
                    LoadResult loadResult = dataLoader.load(
                            transformResult.getTransformedData(), config.getLoadConfig());
                    result.setLoadResult(loadResult);

                    result.setSuccess(loadResult.isSuccess());
                } else {
                    result.setSuccess(false);
                    result.setErrorMessage("数据转换失败: " + transformResult.getErrorMessage());
                }
            } else {
                result.setSuccess(false);
                result.setErrorMessage("数据抽取失败: " + extractResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("ETL任务执行失败: {}", config.getTaskName(), e);
            result.setSuccess(false);
            result.setErrorMessage("ETL任务执行异常: " + e.getMessage());
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        log.info("ETL任务执行完成: {}, 成功={}, 耗时={}ms",
                config.getTaskName(), result.isSuccess(), result.getDuration());

        return CompletableFuture.completedFuture(result);
    }

    /**
     * 批量执行ETL任务
     */
    public List<CompletableFuture<ETLResult>> executeBatchETL(List<ETLTaskConfig> configs) {
        return configs.stream()
                .map(this::executeETLTask)
                .collect(Collectors.toList());
    }

    /**
     * 增量ETL处理
     */
    public CompletableFuture<ETLResult> executeIncrementalETL(ETLTaskConfig config, Long lastSyncTime) {
        // 设置增量抽取配置
        ExtractConfig extractConfig = config.getExtractConfig();
        extractConfig.setIncremental(true);
        extractConfig.setLastSyncTime(lastSyncTime);

        config.setExtractConfig(extractConfig);

        return executeETLTask(config);
    }

    /**
     * ETL任务监控
     */
    @Scheduled(fixedRate = 60 * 1000) // 每分钟检查一次
    public void monitorETLTasks() {
        try {
            // 获取运行中的任务
            List<ETLTaskStatus> runningTasks = getRunningETLTasks();

            for (ETLTaskStatus taskStatus : runningTasks) {
                // 检查任务超时
                if (isTaskTimeout(taskStatus)) {
                    log.warn("ETL任务超时: {}", taskStatus.getTaskName());
                    handleTaskTimeout(taskStatus);
                }

                // 检查任务资源使用
                if (isTaskResourceHeavy(taskStatus)) {
                    log.warn("ETL任务资源使用过高: {}", taskStatus.getTaskName());
                    handleResourceHeavyTask(taskStatus);
                }
            }

        } catch (Exception e) {
            log.error("ETL任务监控失败", e);
        }
    }

    private boolean isTaskTimeout(ETLTaskStatus taskStatus) {
        long maxDuration = taskStatus.getMaxDuration() != null ?
                taskStatus.getMaxDuration() : 30 * 60 * 1000L; // 默认30分钟

        return (System.currentTimeMillis() - taskStatus.getStartTime()) > maxDuration;
    }

    private boolean isTaskResourceHeavy(ETLTaskStatus taskStatus) {
        return taskStatus.getMemoryUsage() > 0.8 || taskStatus.getCpuUsage() > 0.8;
    }
}
```

#### 数据抽取器
```java
@Component
@Slf4j
public class DataExtractor {

    @Resource
    private DatabaseExtractor databaseExtractor;

    @Resource
    private FileExtractor fileExtractor;

    @Resource
    private ApiExtractor apiExtractor;

    /**
     * 抽取数据
     */
    public ExtractResult extract(ExtractConfig config) {
        ExtractResult result = new ExtractResult();
        result.setStartTime(System.currentTimeMillis());

        try {
            switch (config.getSourceType()) {
                case DATABASE:
                    result = databaseExtractor.extract(config);
                    break;
                case FILE:
                    result = fileExtractor.extract(config);
                    break;
                case API:
                    result = apiExtractor.extract(config);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + config.getSourceType());
            }

        } catch (Exception e) {
            log.error("数据抽取失败: source={}", config.getSourceType(), e);
            result.setSuccess(false);
            result.setErrorMessage("数据抽取失败: " + e.getMessage());
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        return result;
    }
}

/**
 * 数据库数据抽取器
 */
@Component
public class DatabaseExtractor {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public ExtractResult extract(ExtractConfig config) {
        ExtractResult result = new ExtractResult();
        List<Map<String, Object>> data = new ArrayList<>();

        try {
            String sql = buildExtractSQL(config);
            log.debug("执行SQL: {}", sql);

            if (config.isBatchMode()) {
                // 批量抽取模式
                data = extractBatch(sql, config);
            } else {
                // 单次抽取模式
                data = jdbcTemplate.queryForList(sql);
            }

            result.setData(data);
            result.setRecordCount(data.size());
            result.setSuccess(true);

            log.info("数据库抽取完成: records={}", data.size());

        } catch (Exception e) {
            log.error("数据库抽取失败", e);
            result.setSuccess(false);
            result.setErrorMessage("数据库抽取失败: " + e.getMessage());
        }

        return result;
    }

    private List<Map<String, Object>> extractBatch(String sql, ExtractConfig config) {
        List<Map<String, Object>> allData = new ArrayList<>();
        int batchSize = config.getBatchSize() != null ? config.getBatchSize() : 1000;
        int offset = 0;

        while (true) {
            String batchSQL = sql + " LIMIT " + batchSize + " OFFSET " + offset;
            List<Map<String, Object>> batchData = jdbcTemplate.queryForList(batchSQL);

            if (batchData.isEmpty()) {
                break;
            }

            allData.addAll(batchData);
            offset += batchSize;

            log.debug("批量抽取: offset={}, size={}", offset, batchData.size());
        }

        return allData;
    }

    private String buildExtractSQL(ExtractConfig config) {
        StringBuilder sql = new StringBuilder();

        // SELECT子句
        if (config.getFields() != null && !config.getFields().isEmpty()) {
            sql.append("SELECT ").append(String.join(", ", config.getFields()));
        } else {
            sql.append("SELECT *");
        }

        // FROM子句
        sql.append(" FROM ").append(config.getTableName());

        // WHERE子句
        if (StringUtils.isNotBlank(config.getWhereClause())) {
            sql.append(" WHERE ").append(config.getWhereClause());
        }

        // 增量抽取条件
        if (config.isIncremental() && config.getLastSyncTime() != null) {
            if (StringUtils.isNotBlank(config.getWhereClause())) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append(config.getTimestampField())
               .append(" > '")
               .append(new Date(config.getLastSyncTime()))
               .append("'");
        }

        // ORDER BY子句
        if (StringUtils.isNotBlank(config.getOrderBy())) {
            sql.append(" ORDER BY ").append(config.getOrderBy());
        }

        return sql.toString();
    }
}
```

### 3. 数据分析和报告系统

#### 数据分析引擎
```java
@Service
@Slf4j
public class DataAnalysisEngine {

    @Resource
    private StatisticAnalyzer statisticAnalyzer;

    @Resource
    private TrendAnalyzer trendAnalyzer;

    @Resource
    private CorrelationAnalyzer correlationAnalyzer;

    /**
     * 执行数据分析
     */
    public AnalysisResult performAnalysis(AnalysisRequest request) {
        AnalysisResult result = new AnalysisResult();
        result.setRequest(request);
        result.setStartTime(System.currentTimeMillis());

        try {
            switch (request.getAnalysisType()) {
                case STATISTICAL:
                    result = performStatisticalAnalysis(request);
                    break;
                case TREND:
                    result = performTrendAnalysis(request);
                    break;
                case CORRELATION:
                    result = performCorrelationAnalysis(request);
                    break;
                case CUSTOM:
                    result = performCustomAnalysis(request);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的分析类型: " + request.getAnalysisType());
            }

        } catch (Exception e) {
            log.error("数据分析失败: type={}", request.getAnalysisType(), e);
            result.setSuccess(false);
            result.setErrorMessage("数据分析失败: " + e.getMessage());
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    private AnalysisResult performStatisticalAnalysis(AnalysisRequest request) {
        AnalysisResult result = new AnalysisResult();
        List<Map<String, Object>> data = request.getData();

        if (data == null || data.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("数据为空，无法进行分析");
            return result;
        }

        // 基本统计信息
        StatisticalInfo statisticalInfo = statisticAnalyzer.calculateBasicStatistics(data, request.getFields());
        result.setStatisticalInfo(statisticalInfo);

        // 描述性统计
        DescriptiveStatistics descriptiveStats = statisticAnalyzer.calculateDescriptiveStatistics(data, request.getFields());
        result.setDescriptiveStatistics(descriptiveStats);

        // 数据分布分析
        DistributionAnalysis distributionAnalysis = statisticAnalyzer.analyzeDistribution(data, request.getFields());
        result.setDistributionAnalysis(distributionAnalysis);

        result.setSuccess(true);

        log.info("统计分析完成: records={}, fields={}", data.size(), request.getFields());

        return result;
    }

    private AnalysisResult performTrendAnalysis(AnalysisRequest request) {
        AnalysisResult result = new AnalysisResult();
        List<Map<String, Object>> data = request.getData();

        if (data == null || data.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("数据为空，无法进行趋势分析");
            return result;
        }

        // 时间序列趋势分析
        TrendAnalysis trendAnalysis = trendAnalyzer.analyzeTimeSeries(data, request.getTimeField(), request.getValueFields());
        result.setTrendAnalysis(trendAnalysis);

        // 季节性分析
        SeasonalityAnalysis seasonalityAnalysis = trendAnalyzer.analyzeSeasonality(data, request.getTimeField(), request.getValueFields());
        result.setSeasonalityAnalysis(seasonalityAnalysis);

        // 预测分析
        ForecastAnalysis forecastAnalysis = trendAnalyzer.generateForecast(data, request.getTimeField(), request.getValueFields(), request.getForecastPeriods());
        result.setForecastAnalysis(forecastAnalysis);

        result.setSuccess(true);

        log.info("趋势分析完成: records={}, forecastPeriods={}", data.size(), request.getForecastPeriods());

        return result;
    }

    private AnalysisResult performCorrelationAnalysis(AnalysisRequest request) {
        AnalysisResult result = new AnalysisResult();
        List<Map<String, Object>> data = request.getData();

        if (data == null || data.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("数据为空，无法进行相关性分析");
            return result;
        }

        // 相关性矩阵
        CorrelationMatrix correlationMatrix = correlationAnalyzer.calculateCorrelationMatrix(data, request.getFields());
        result.setCorrelationMatrix(correlationMatrix);

        // 强相关性分析
        List<CorrelationPair> strongCorrelations = correlationAnalyzer.findStrongCorrelations(correlationMatrix, 0.7);
        result.setStrongCorrelations(strongCorrelations);

        // 因果关系分析（简化版）
        CausalityAnalysis causalityAnalysis = correlationAnalyzer.analyzeCausality(data, request.getFields());
        result.setCausalityAnalysis(causalityAnalysis);

        result.setSuccess(true);

        log.info("相关性分析完成: records={}, fields={}, strongCorrelations={}",
                data.size(), request.getFields().size(), strongCorrelations.size());

        return result;
    }
}

/**
 * 统计分析器
 */
@Component
public class StatisticAnalyzer {

    public StatisticalInfo calculateBasicStatistics(List<Map<String, Object>> data, List<String> fields) {
        StatisticalInfo info = new StatisticalInfo();
        info.setTotalRecords(data.size());
        info.setFieldCount(fields.size());

        // 缺失值统计
        Map<String, Integer> missingValueCounts = new HashMap<>();
        // 数据类型统计
        Map<String, DataType> dataTypes = new HashMap<>();

        for (String field : fields) {
            missingValueCounts.put(field, calculateMissingValueCount(data, field));
            dataTypes.put(field, inferDataType(data, field));
        }

        info.setMissingValueCounts(missingValueCounts);
        info.setDataTypes(dataTypes);

        return info;
    }

    public DescriptiveStatistics calculateDescriptiveStatistics(List<Map<String, Object>> data, List<String> fields) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (String field : fields) {
            FieldStatistics fieldStats = calculateFieldStatistics(data, field);
            stats.addFieldStatistics(field, fieldStats);
        }

        return stats;
    }

    private FieldStatistics calculateFieldStatistics(List<Map<String, Object>> data, String field) {
        FieldStatistics fieldStats = new FieldStatistics();
        fieldStats.setFieldName(field);

        List<Object> values = data.stream()
                .map(row -> row.get(field))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return fieldStats;
        }

        // 数值型字段统计
        if (isNumericField(values)) {
            List<Double> numericValues = values.stream()
                    .map(value -> Double.parseDouble(value.toString()))
                    .collect(Collectors.toList());

            fieldStats.setMin(numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0));
            fieldStats.setMax(numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0));
            fieldStats.setMean(numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            fieldStats.setMedian(calculateMedian(numericValues));
            fieldStats.setStandardDeviation(calculateStandardDeviation(numericValues));
        }

        // 分类字段统计
        else {
            Map<Object, Long> frequencyMap = values.stream()
                    .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

            fieldStats.setUniqueValues(frequencyMap.size());
            fieldStats.setFrequencyMap(frequencyMap);
            fieldStats.setMode(frequencyMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null));
        }

        return fieldStats;
    }

    private boolean isNumericField(List<Object> values) {
        return values.stream()
                .allMatch(value -> value instanceof Number || isNumericString(value.toString()));
    }

    private boolean isNumericString(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double calculateMedian(List<Double> values) {
        List<Double> sortedValues = values.stream().sorted().collect(Collectors.toList());
        int size = sortedValues.size();

        if (size % 2 == 0) {
            return (sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0;
        } else {
            return sortedValues.get(size / 2);
        }
    }

    private double calculateStandardDeviation(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }
}
```

## 🎯 数据处理场景应用

### 1. 数据质量管理
- 数据完整性检查
- 数据一致性验证
- 数据准确性提升
- 数据标准化处理

### 2. ETL流程管理
- 数据抽取优化
- 转换规则定义
- 加载策略配置
- 任务监控和告警

### 3. 数据分析和报告
- 统计分析
- 趋势分析
- 相关性分析
- 自动化报告生成

## 📊 数据处理指标

### 核心数据处理KPI
- **数据质量得分**: 完整性、准确性、一致性综合评分
- **处理效率**: ETL任务执行时间和吞吐量
- **错误率**: 数据处理失败率和异常处理率
- **数据覆盖率**: 数据采集的覆盖范围

### 质量指标
- **数据完整率**: 必填字段完整度
- **数据准确率**: 格式正确性和逻辑一致性
- **数据时效性**: 数据更新频率和延迟
- **数据唯一率**: 重复数据的比例

---

## 🚀 技能等级要求

### 初级 (★☆☆)
- 了解基本的数据处理概念
- 掌握SQL和Excel数据处理
- 能够进行简单的数据清洗

### 中级 (★★☆)
- 掌握ETL工具和技术
- 能够设计和实现数据质量检查
- 熟悉大数据处理框架

### 专家级 (★★★)
- 能够设计企业级数据处理架构
- 掌握数据治理和数据血缘管理
- 能够构建实时数据处理系统

---

**技能使用提示**: 当需要进行数据处理、质量检查、ETL流程建设或数据分析时，调用此技能获得专业的数据处理解决方案。

**记忆要点**:
- 数据质量是数据应用的基础，必须严格把控
- ETL流程要考虑性能、可靠性和可监控性
- 数据分析要基于业务目标，提供有价值的洞察
- 数据血缘追踪有助于问题排查和影响分析
- 自动化是提高数据处理效率的关键
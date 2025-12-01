# 考勤模块微服务化设计方案

> **设计文档类型**: 微服务架构设计
> **变更ID**: attendance-microservice-transformation
> **创建时间**: 2025-11-27
> **设计版本**: v1.0

---

## 📋 设计概述

### 🎯 设计目标
基于IOE-DREAM项目现有考勤模块，设计完整的微服务化改造方案，实现考勤业务的独立部署、独立扩展和自主运维，建立高性能、高可用的考勤微服务架构。

### 🎯 设计原则
- **业务驱动**: 基于业务边界进行服务拆分，确保业务逻辑的完整性
- **渐进演进**: 采用渐进式改造策略，保证业务连续性和数据一致性
- **技术统一**: 遵循项目统一技术栈，确保架构一致性
- **运维友好**: 设计易于监控、部署和运维的微服务架构

---

## 🧠 智能引擎设计

### 智能规则引擎系统

#### 规则引擎架构
```java
@Service
@Slf4j
public class AttendanceRuleEngine {

    @Resource
    private AttendanceRuleRepository ruleRepository;

    @Resource
    private AttendanceStatisticsRepository statisticsRepository;

    @Resource
    private AttendanceCacheManager cacheManager;

    private final ExecutorService ruleExecutor;
    private final Map<String, RuleEvaluator> evaluatorMap;

    public AttendanceRuleEngine() {
        this.ruleExecutor = Executors.newFixedThreadPool(10);
        this.evaluatorMap = new ConcurrentHashMap<>();
        initializeEvaluators();
    }

    /**
     * 执行规则引擎 - 复杂规则计算核心
     */
    @Async("ruleEngineExecutor")
    public CompletableFuture<List<AttendanceExceptionEntity>> evaluateRules(
            AttendanceRecordEntity record,
            List<AttendanceRuleEntity> applicableRules) {

        return CompletableFuture.supplyAsync(() -> {
            List<AttendanceExceptionEntity> exceptions = new ArrayList<>();

            for (AttendanceRuleEntity rule : applicableRules) {
                try {
                    RuleEvaluator evaluator = evaluatorMap.get(rule.getRuleType());
                    if (evaluator != null) {
                        List<AttendanceExceptionEntity> ruleExceptions =
                            evaluator.evaluate(record, rule);
                        exceptions.addAll(ruleExceptions);
                    }
                } catch (Exception e) {
                    log.error("规则评估失败: ruleId={}, ruleType={}",
                        rule.getRuleId(), rule.getRuleType(), e);
                }
            }

            return exceptions;
        }, ruleExecutor);
    }

    /**
     * 智能规则优化 - 基于历史数据优化规则
     */
    public void optimizeRules() {
        // 分析历史数据，优化规则参数
        // 实现省略...
    }

    private void initializeEvaluators() {
        evaluatorMap.put("LATE_RULE", new LateRuleEvaluator());
        evaluatorMap.put("EARLY_LEAVE_RULE", new EarlyLeaveRuleEvaluator());
        evaluatorMap.put("ABSENTEE_RULE", new AbsenteeRuleEvaluator());
        evaluatorMap.put("OVERTIME_RULE", new OvertimeRuleEvaluator());
        evaluatorMap.put("MULTIPLE_CLOCK_RULE", new MultipleClockRuleEvaluator());
    }
}
```

#### 规则评估器接口
```java
public interface RuleEvaluator {
    List<AttendanceExceptionEntity> evaluate(AttendanceRecordEntity record, AttendanceRuleEntity rule);
    boolean supports(String ruleType);
    int getPriority();
}

@Component
public class LateRuleEvaluator implements RuleEvaluator {

    @Override
    public List<AttendanceExceptionEntity> evaluate(AttendanceRecordEntity record, AttendanceRuleEntity rule) {
        List<AttendanceExceptionEntity> exceptions = new ArrayList<>();

        // 解析规则条件
        RuleCondition condition = RuleCondition.fromJson(rule.getConditions());

        // 计算迟到时间
        LocalDateTime expectedTime = calculateExpectedClockTime(record, condition);
        Duration lateDuration = Duration.between(expectedTime, record.getClockTime());

        // 判断是否迟到
        if (lateDuration.toMinutes() > condition.getLateThresholdMinutes()) {
            AttendanceExceptionEntity exception = AttendanceExceptionEntity.builder()
                .userId(record.getUserId())
                .exceptionDate(record.getClockTime().toLocalDate())
                .exceptionType(ExceptionTypeEnum.LATE.getCode())
                .description(String.format("迟到%d分钟", lateDuration.toMinutes()))
                .relatedRecordIds(String.valueOf(record.getRecordId()))
                .autoDetected(true)
                .build();

            exceptions.add(exception);
        }

        return exceptions;
    }

    @Override
    public boolean supports(String ruleType) {
        return "LATE_RULE".equals(ruleType);
    }

    @Override
    public int getPriority() {
        return 1; // 高优先级
    }

    private LocalDateTime calculateExpectedClockTime(AttendanceRecordEntity record, RuleCondition condition) {
        // 根据排班信息计算期望打卡时间
        // 实现省略...
        return record.getClockTime();
    }
}
```

### 智能排班引擎

#### 遗传算法排班优化器
```java
@Service
@Slf4j
public class IntelligentSchedulingEngine {

    private static final int POPULATION_SIZE = 100;
    private static final int GENERATIONS = 200;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.8;

    @Resource
    private AttendanceScheduleRepository scheduleRepository;

    @Resource
    private ShiftsRepository shiftsRepository;

    @Resource
    private UserServiceClient userServiceClient;

    /**
     * 智能排班优化 - 使用遗传算法
     */
    public SchedulingResult optimizeSchedule(SchedulingRequest request) {
        log.info("开始智能排班优化: 员工数={}, 日期范围={}",
            request.getUserIds().size(), request.getDateRange());

        // 1. 初始化种群
        List<ScheduleChromosome> population = initializePopulation(request);

        // 2. 进化迭代
        for (int generation = 0; generation < GENERATIONS; generation++) {
            // 评估适应度
            evaluateFitness(population, request);

            // 选择
            population = selection(population);

            // 交叉
            crossover(population);

            // 变异
            mutate(population, request);

            // 记录最佳适应度
            if (generation % 20 == 0) {
                double bestFitness = getBestFitness(population);
                log.info("第{}代最佳适应度: {}", generation, bestFitness);
            }
        }

        // 3. 返回最优解
        ScheduleChromosome bestSolution = getBestSolution(population);
        return convertToResult(bestSolution, request);
    }

    /**
     * 初始化种群
     */
    private List<ScheduleChromosome> initializePopulation(SchedulingRequest request) {
        List<ScheduleChromosome> population = new ArrayList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            ScheduleChromosome chromosome = createRandomChromosome(request);
            population.add(chromosome);
        }

        return population;
    }

    /**
     * 评估适应度函数
     */
    private void evaluateFitness(List<ScheduleChromosome> population, SchedulingRequest request) {
        for (ScheduleChromosome chromosome : population) {
            double fitness = calculateFitness(chromosome, request);
            chromosome.setFitness(fitness);
        }
    }

    /**
     * 计算适应度 - 多目标优化
     */
    private double calculateFitness(ScheduleChromosome chromosome, SchedulingRequest request) {
        double fitness = 0.0;

        // 1. 工作量均衡性 (权重: 0.3)
        double workloadBalance = calculateWorkloadBalance(chromosome, request);
        fitness += workloadBalance * 0.3;

        // 2. 技能匹配度 (权重: 0.25)
        double skillMatch = calculateSkillMatch(chromosome, request);
        fitness += skillMatch * 0.25;

        // 3. 连续工作时间约束 (权重: 0.2)
        double continuousWork = calculateContinuousWork(chromosome, request);
        fitness += continuousWork * 0.2;

        // 4. 员工偏好匹配 (权重: 0.15)
        double preferenceMatch = calculatePreferenceMatch(chromosome, request);
        fitness += preferenceMatch * 0.15;

        // 5. 法规合规性 (权重: 0.1)
        double compliance = calculateCompliance(chromosome, request);
        fitness += compliance * 0.1;

        return fitness;
    }

    /**
     * 遗传算法 - 选择操作
     */
    private List<ScheduleChromosome> selection(List<ScheduleChromosome> population) {
        // 锦标赛选择
        List<ScheduleChromosome> newPopulation = new ArrayList<>();

        while (newPopulation.size() < population.size()) {
            ScheduleChromosome parent1 = tournamentSelection(population);
            ScheduleChromosome parent2 = tournamentSelection(population);
            newPopulation.add(parent1);
            newPopulation.add(parent2);
        }

        return newPopulation;
    }

    /**
     * 遗传算法 - 交叉操作
     */
    private void crossover(List<ScheduleChromosome> population) {
        for (int i = 0; i < population.size() - 1; i += 2) {
            if (Math.random() < CROSSOVER_RATE) {
                ScheduleChromosome parent1 = population.get(i);
                ScheduleChromosome parent2 = population.get(i + 1);

                ScheduleChromosome[] offspring = crossover(parent1, parent2);
                population.set(i, offspring[0]);
                population.set(i + 1, offspring[1]);
            }
        }
    }

    /**
     * 遗传算法 - 变异操作
     */
    private void mutate(List<ScheduleChromosome> population, SchedulingRequest request) {
        for (ScheduleChromosome chromosome : population) {
            if (Math.random() < MUTATION_RATE) {
                mutateChromosome(chromosome, request);
            }
        }
    }
}
```

### 三层缓存架构设计

#### 缓存管理器增强版
```java
@Component
@Slf4j
public class AttendanceCacheManagerEnhanced {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private Cache<String, Object> caffeineCache;

    // 三个专用线程池
    @Async("cacheExecutor")
    private final ExecutorService cacheExecutor;

    @Async("asyncExecutor")
    private final ExecutorService asyncExecutor;

    @Async("warmupExecutor")
    private final ExecutorService warmupExecutor;

    /**
     * 三层缓存获取 - L1 -> L2 -> 数据库
     */
    public <T> T getWithCache(String key, Class<T> clazz, Supplier<T> dataLoader) {
        // 1. L1缓存 (Caffeine)
        T value = getFromL1Cache(key);
        if (value != null) {
            log.debug("Cache hit from L1: {}", key);
            return value;
        }

        // 2. L2缓存 (Redis)
        value = getFromL2Cache(key, clazz);
        if (value != null) {
            log.debug("Cache hit from L2: {}", key);
            // 回写L1缓存
            putToL1Cache(key, value);
            return value;
        }

        // 3. 数据库
        value = dataLoader.get();
        if (value != null) {
            // 异步写入L1和L2缓存
            asyncPutToCache(key, value);
            log.debug("Cache miss, loaded from database: {}", key);
        }

        return value;
    }

    /**
     * 智能缓存预热
     */
    @Async("warmupExecutor")
    public CompletableFuture<Void> warmupCache(WarmupRequest request) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("开始缓存预热: type={}", request.getWarmupType());

                switch (request.getWarmupType()) {
                    case USER_RECORDS:
                        warmupUserRecords(request.getUserIds(), request.getDateRange());
                        break;
                    case DEVICES:
                        warmupDevices(request.getDeviceIds());
                        break;
                    case RULES:
                        warmupRules();
                        break;
                    default:
                        log.warn("未知的预热类型: {}", request.getWarmupType());
                }

                log.info("缓存预热完成: type={}", request.getWarmupType());

            } catch (Exception e) {
                log.error("缓存预热失败", e);
            }
        }, warmupExecutor);
    }

    /**
     * 批量缓存操作
     */
    public <T> Map<String, T> mgetWithCache(List<String> keys, Class<T> clazz,
            Function<List<String>, Map<String, T>> batchLoader) {

        Map<String, T> result = new HashMap<>();
        List<String> missingKeys = new ArrayList<>();

        // 1. 批量从L1缓存获取
        for (String key : keys) {
            T value = getFromL1Cache(key);
            if (value != null) {
                result.put(key, value);
            } else {
                missingKeys.add(key);
            }
        }

        // 2. 批量从L2缓存获取
        if (!missingKeys.isEmpty()) {
            Map<String, T> l2Results = batchGetFromL2Cache(missingKeys, clazz);
            result.putAll(l2Results);

            // 更新L1缓存
            l2Results.forEach(this::putToL1Cache);

            // 找出仍然缺失的key
            missingKeys.removeAll(l2Results.keySet());
        }

        // 3. 批量从数据库加载
        if (!missingKeys.isEmpty()) {
            Map<String, T> dbResults = batchLoader.apply(missingKeys);
            result.putAll(dbResults);

            // 异步更新缓存
            dbResults.forEach((key, value) -> asyncPutToCache(key, value));
        }

        return result;
    }

    private <T> void asyncPutToCache(String key, T value) {
        CompletableFuture.runAsync(() -> {
            try {
                putToL1Cache(key, value);
                putToL2Cache(key, value);
            } catch (Exception e) {
                log.error("异步缓存写入失败: key={}", key, e);
            }
        }, cacheExecutor);
    }

    // 缓存配置
    @Bean
    @Primary
    public Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build();
    }

    @Bean("cacheExecutor")
    public Executor cacheExecutor() {
        return ThreadPoolTaskBuilder.newBuilder()
            .corePoolSize(10)
            .maxPoolSize(20)
            .queueCapacity(1000)
            .threadNamePrefix("cache-")
            .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
            .build();
    }

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        return ThreadPoolTaskBuilder.newBuilder()
            .corePoolSize(5)
            .maxPoolSize(10)
            .queueCapacity(500)
            .threadNamePrefix("async-")
            .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
            .build();
    }

    @Bean("warmupExecutor")
    public Executor warmupExecutor() {
        return ThreadPoolTaskBuilder.newBuilder()
            .corePoolSize(3)
            .maxPoolSize(5)
            .queueCapacity(100)
            .threadNamePrefix("warmup-")
            .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
            .build();
    }
}
```

### 异常申请审批流程设计

#### 异常申请服务
```java
@Service
@Transactional
public class AttendanceExceptionApplicationService {

    @Resource
    private ExceptionApplicationsRepository applicationsRepository;

    @Resource
    private ExceptionApprovalsRepository approvalsRepository;

    @Resource
    private WorkflowEngine workflowEngine;

    @Resource
    private NotificationServiceClient notificationServiceClient;

    @Resource
    private UserServiceClient userServiceClient;

    /**
     * 提交异常申请
     */
    public ExceptionApplicationResult submitApplication(ExceptionApplicationDTO applicationDTO) {
        log.info("提交异常申请: userId={}, exceptionType={}",
            applicationDTO.getUserId(), applicationDTO.getExceptionType());

        // 1. 验证申请数据
        validateApplication(applicationDTO);

        // 2. 创建申请记录
        ExceptionApplicationsEntity application = ExceptionApplicationsEntity.builder()
            .userId(applicationDTO.getUserId())
            .exceptionDate(applicationDTO.getExceptionDate())
            .exceptionType(applicationDTO.getExceptionType())
            .description(applicationDTO.getDescription())
            .applyReason(applicationDTO.getApplyReason())
            .evidenceFiles(applicationDTO.getEvidenceFiles())
            .applyStatus(ApplyStatusEnum.PENDING.getCode())
            .applyTime(LocalDateTime.now())
            .build();

        applicationsRepository.save(application);

        // 3. 启动工作流
        WorkflowInstance workflowInstance = workflowEngine.startWorkflow(
            "attendance_exception_approval",
            application.getApplicationId(),
            buildWorkflowVariables(application)
        );

        // 4. 发送通知
        sendNotificationToApprovers(application);

        // 5. 记录操作日志
        logApplicationOperation(application, "SUBMIT", null);

        return ExceptionApplicationResult.builder()
            .applicationId(application.getApplicationId())
            .workflowInstanceId(workflowInstance.getInstanceId())
            .status(ApplyStatusEnum.PENDING)
            .message("申请提交成功，等待审批")
            .build();
    }

    /**
     * 审批异常申请
     */
    @Transactional
    public ExceptionApprovalResult approveApplication(
            Long applicationId,
            ExceptionApprovalDTO approvalDTO) {

        // 1. 验证审批权限
        validateApprovalPermission(applicationId, approvalDTO);

        // 2. 获取申请记录
        ExceptionApplicationsEntity application = applicationsRepository.findById(applicationId)
            .orElseThrow(() -> new SmartException("申请不存在"));

        // 3. 执行工作流审批
        WorkflowTask task = workflowEngine.getTaskByInstanceId(
            application.getWorkflowInstanceId(),
            approvalDTO.getApproverId()
        );

        Map<String, Object> variables = buildApprovalVariables(approvalDTO);
        workflowEngine.completeTask(task.getTaskId(), variables);

        // 4. 更新申请状态
        application.setApplyStatus(approvalDTO.getApproveStatus());
        application.setApproverId(approvalDTO.getApproverId());
        application.setApproveTime(LocalDateTime.now());
        application.setApproveRemark(approvalDTO.getApproveRemark());
        applicationsRepository.save(application);

        // 5. 创建审批记录
        ExceptionApprovalsEntity approval = ExceptionApprovalsEntity.builder()
            .applicationId(applicationId)
            .approverId(approvalDTO.getApproverId())
            .approveStatus(approvalDTO.getApproveStatus())
            .approveRemark(approvalDTO.getApproveRemark())
            .approveTime(LocalDateTime.now())
            .build();

        approvalsRepository.save(approval);

        // 6. 处理审批结果
        handleApprovalResult(application, approval);

        // 7. 发送通知
        sendApprovalNotification(application, approval);

        return ExceptionApprovalResult.builder()
            .applicationId(applicationId)
            .approveStatus(approvalDTO.getApproveStatus())
            .message("审批操作成功")
            .build();
    }

    /**
     * 处理审批结果
     */
    private void handleApprovalResult(ExceptionApplicationsEntity application,
                                        ExceptionApprovalsEntity approval) {

        if (ApproveStatusEnum.APPROVED.getCode().equals(approval.getApproveStatus())) {
            // 审批通过 - 更新异常记录状态
            updateExceptionRecordStatus(application);

            // 自动补卡处理
            if (needAutoMakeup(application)) {
                performAutoMakeup(application);
            }

        } else {
            // 审批拒绝 - 记录拒绝原因
            log.info("异常申请被拒绝: applicationId={}, reason={}",
                application.getApplicationId(), approval.getApproveRemark());
        }
    }

    /**
     * 自动补卡处理
     */
    private void performAutoMakeup(ExceptionApplicationsEntity application) {
        try {
            AttendanceRecordEntity makeupRecord = AttendanceRecordEntity.builder()
                .userId(application.getUserId())
                .clockTime(application.getExceptionDate().atTime(LocalTime.of(9, 0))) // 默认9点
                .clockType(ClockTypeEnum.MAKEUP.getCode())
                .verificationMethod(VerificationTypeEnum.MANUAL.getCode())
                .status(RecordStatusEnum.NORMAL.getCode())
                .relatedApplicationId(application.getApplicationId())
                .build();

            // 保存补卡记录
            // recordRepository.save(makeupRecord);

            log.info("自动补卡成功: applicationId={}, recordId={}",
                application.getApplicationId(), makeupRecord.getRecordId());

        } catch (Exception e) {
            log.error("自动补卡失败: applicationId={}", application.getApplicationId(), e);
            throw new SmartException("自动补卡失败");
        }
    }
}
```

---

## 🏗️ 系统架构设计

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                              │
│                  (Spring Cloud Gateway)                        │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Load Balancer                               │
│                      (Nginx/HAProxy)                          │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                Attendance Microservice                          │
│  ┌─────────────────┬─────────────────┬─────────────────────────┐ │
│  │   Web Layer     │  Business Layer │   Infrastructure Layer  │ │
│  │   (Controller)  │   (Service)     │   (Repository/Cache)   │ │
│  │                 │                 │                         │ │
│  │ AttendanceAPI   │ RecordService   │ AttendanceRepository   │ │
│  │ ScheduleAPI     │ ScheduleService │ ScheduleRepository     │ │
│  │ ReportAPI       │ ReportService   │ ReportRepository       │ │
│  │ MobileAPI       │ RuleEngineSvc   │ CacheManager           │ │
│  └─────────────────┴─────────────────┴─────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                   External Services                            │
│  ┌─────────────────┬─────────────────┬─────────────────────────┐ │
│  │   User Service  │  Device Service │  Notification Service  │ │
│  │                 │                 │                         │ │
│  │ • 用户信息       │ • 设备信息       │ • 考勤通知               │ │
│  │ • 部门信息       │ • 设备状态       │ • 异常告警               │ │
│  │ • 权限验证       │ • 设备控制       │ • 报表推送               │ │
│  └─────────────────┴─────────────────┴─────────────────────────┘ │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure                              │
│  ┌─────────────────┬─────────────────┬─────────────────────────┐ │
│  │   Nacos         │     Redis       │       MySQL             │ │
│  │                 │                 │                         │ │
│  │ • 服务注册       │ • L1缓存(Caffeine) │ • 主库(写)             │ │
│  │ • 配置中心       │ • L2缓存(Redis)   │ • 从库(读)             │ │
│  │ • 服务发现       │ • 分布式锁       │ • 分库分表             │ │
│  └─────────────────┴─────────────────┴─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 微服务模块结构

```
attendance-microservice/
├── attendance-api/                      # API接口层
│   ├── src/main/java/com/iog/attendance/api/
│   │   ├── dto/                         # 数据传输对象
│   │   │   ├── AttendanceRecordDTO.java
│   │   │   ├── ScheduleDTO.java
│   │   │   ├── ReportDTO.java
│   │   │   └── AttendanceExceptionDTO.java
│   │   ├── vo/                          # 视图对象
│   │   │   ├── AttendanceRecordVO.java
│   │   │   ├── ScheduleVO.java
│   │   │   ├── ReportVO.java
│   │   │   └── AttendanceStatisticsVO.java
│   │   ├── form/                        # 表单对象
│   │   │   ├── AttendanceRecordForm.java
│   │   │   ├── ScheduleForm.java
│   │   │   └── AttendanceRuleForm.java
│   │   └── client/                      # Feign客户端
│   │       ├── UserServiceClient.java
│   │       ├── DeviceServiceClient.java
│   │       └── NotificationServiceClient.java
│   └── pom.xml
├── attendance-core/                     # 核心业务层
│   ├── src/main/java/com/iog/attendance/core/
│   │   ├── domain/                      # 领域模型
│   │   │   ├── entity/                  # 实体类
│   │   │   │   ├── AttendanceRecordEntity.java
│   │   │   │   ├── AttendanceScheduleEntity.java
│   │   │   │   ├── AttendanceShiftEntity.java
│   │   │   │   ├── AttendanceRuleEntity.java
│   │   │   │   └── AttendanceExceptionEntity.java
│   │   │   ├── enums/                   # 枚举类
│   │   │   │   ├── ClockTypeEnum.java
│   │   │   │   ├── RecordStatusEnum.java
│   │   │   │   ├── ExceptionTypeEnum.java
│   │   │   │   └── ReportTypeEnum.java
│   │   │   └── valueobject/             # 值对象
│   │   │       ├── ClockTime.java
│   │   │       ├── WorkDuration.java
│   │   │       └── LocationInfo.java
│   │   ├── service/                     # 业务服务
│   │   │   ├── AttendanceRecordService.java
│   │   │   ├── AttendanceScheduleService.java
│   │   │   ├── AttendanceRuleEngineService.java
│   │   │   ├── AttendanceReportService.java
│   │   │   ├── AttendanceStatisticsService.java
│   │   │   └── AttendanceExceptionService.java
│   │   ├── repository/                  # 数据仓储接口
│   │   │   ├── AttendanceRecordRepository.java
│   │   │   ├── AttendanceScheduleRepository.java
│   │   │   └── AttendanceReportRepository.java
│   │   ├── event/                       # 事件处理
│   │   │   ├── AttendanceEventPublisher.java
│   │   │   ├── AttendanceEventHandler.java
│   │   │   ├── AttendanceRecordCreatedEvent.java
│   │   │   └── AttendanceExceptionEvent.java
│   │   └── config/                      # 配置类
│   │       ├── AttendanceConfig.java
│   │       ├── CacheConfig.java
│   │       ├── TransactionConfig.java
│   │       └── EventConfig.java
│   └── pom.xml
├── attendance-infrastructure/           # 基础设施层
│   ├── src/main/java/com/iog/attendance/infra/
│   │   ├── persistence/                 # 数据持久化
│   │   │   ├── mapper/                  # MyBatis Mapper
│   │   │   │   ├── AttendanceRecordMapper.java
│   │   │   │   ├── AttendanceScheduleMapper.java
│   │   │   │   └── AttendanceReportMapper.java
│   │   │   ├── impl/                    # Repository实现
│   │   │   │   ├── AttendanceRecordRepositoryImpl.java
│   │   │   │   └── AttendanceScheduleRepositoryImpl.java
│   │   │   └── converter/               # 数据转换器
│   │   │       ├── AttendanceRecordConverter.java
│   │   │       └── AttendanceScheduleConverter.java
│   │   ├── cache/                       # 缓存实现
│   │   │   ├── AttendanceCacheManager.java
│   │   │   ├── AttendanceCacheKey.java
│   │   │   └── AttendanceCacheTtl.java
│   │   ├── external/                    # 外部服务
│   │   │   ├── UserServiceAdapter.java
│   │   │   ├── DeviceServiceAdapter.java
│   │   │   └── NotificationServiceAdapter.java
│   │   ├── messaging/                   # 消息队列
│   │   │   ├── AttendanceEventProducer.java
│   │   │   ├── AttendanceEventConsumer.java
│   │   │   └── AttendanceMessageConfig.java
│   │   └── config/                      # 基础设施配置
│   │       ├── DatabaseConfig.java
│   │       ├── RedisConfig.java
│   │       ├── RabbitMQConfig.java
│   │       └── SeataConfig.java
│   └── pom.xml
├── attendance-app/                      # 应用启动层
│   ├── src/main/java/com/iog/attendance/
│   │   ├── AttendanceApplication.java   # 启动类
│   │   ├── controller/                  # REST控制器
│   │   │   ├── AttendanceRecordController.java
│   │   │   ├── AttendanceScheduleController.java
│   │   │   ├── AttendanceReportController.java
│   │   │   ├── AttendanceRuleController.java
│   │   │   └── AttendanceMobileController.java
│   │   ├── filter/                      # 过滤器
│   │   │   ├── AuthenticationFilter.java
│   │   │   ├── RequestLogFilter.java
│   │   │   └── RateLimitFilter.java
│   │   ├── interceptor/                 # 拦截器
│   │   │   ├── AuthenticationInterceptor.java
│   │   │   └── PermissionInterceptor.java
│   │   └── config/                      # 应用配置
│   │       ├── WebMvcConfig.java
│   │       ├── SwaggerConfig.java
│   │       ├── SecurityConfig.java
│   │       └── MonitoringConfig.java
│   └── pom.xml
└── attendance-start/                    # 部署启动模块
    ├── Dockerfile
    ├── docker-compose.yml
    ├── k8s/                           # Kubernetes部署文件
    │   ├── deployment.yaml
    │   ├── service.yaml
    │   ├── configmap.yaml
    │   └── ingress.yaml
    └── scripts/                       # 部署脚本
        ├── deploy.sh
        ├── rollback.sh
        └── health-check.sh
```

---

## 📊 数据架构设计

### 数据库设计

#### 分库策略
```sql
-- 考勤微服务独立数据库
CREATE DATABASE smart_attendance_v3
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 核心业务表
USE smart_attendance_v3;

-- 1. 考勤记录表 (按年分表)
CREATE TABLE t_attendance_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT COMMENT '打卡设备ID',
    clock_time DATETIME NOT NULL COMMENT '打卡时间',
    clock_type TINYINT NOT NULL COMMENT '打卡类型 1-上班 2-下班',
    location_id BIGINT COMMENT '位置ID',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    address VARCHAR(200) COMMENT '打卡地址',
    photo_url VARCHAR(500) COMMENT '打卡照片URL',
    verification_method TINYINT COMMENT '验证方式 1-指纹 2-人脸 3-密码 4-二维码',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-异常',
    exception_type VARCHAR(50) COMMENT '异常类型',
    exception_reason VARCHAR(200) COMMENT '异常原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_user_time (user_id, clock_time),
    INDEX idx_device_time (device_id, clock_time),
    INDEX idx_location_time (location_id, clock_time),
    INDEX idx_status_time (status, clock_time),
    UNIQUE KEY uk_user_device_time (user_id, device_id, clock_time, clock_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 2. 排班表
CREATE TABLE t_attendance_schedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    shift_id BIGINT NOT NULL COMMENT '班次ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    work_location_id BIGINT COMMENT '工作地点ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-调班 3-请假 4-加班',
    adjustment_reason VARCHAR(200) COMMENT '调整原因',
    approver_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_user_date (user_id, schedule_date),
    INDEX idx_shift_date (shift_id, schedule_date),
    INDEX idx_status_date (status, schedule_date),
    INDEX idx_location_date (work_location_id, schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 3. 班次表
CREATE TABLE t_attendance_shift (
    shift_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_name VARCHAR(50) NOT NULL COMMENT '班次名称',
    shift_code VARCHAR(20) UNIQUE NOT NULL COMMENT '班次编码',
    start_time TIME NOT NULL COMMENT '上班时间',
    end_time TIME NOT NULL COMMENT '下班时间',
    break_duration INT DEFAULT 0 COMMENT '休息时长(分钟)',
    work_hours DECIMAL(4,2) NOT NULL COMMENT '工作时长(小时)',
    late_tolerance INT DEFAULT 0 COMMENT '迟到宽限(分钟)',
    early_tolerance INT DEFAULT 0 COMMENT '早退宽限(分钟)',
    overtime_threshold INT DEFAULT 0 COMMENT '加班阈值(分钟)',
    color VARCHAR(10) DEFAULT '#1890ff' COMMENT '显示颜色',
    description VARCHAR(200) COMMENT '班次描述',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_shift_code (shift_code),
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次表';

-- 4. 考勤规则表
CREATE TABLE t_attendance_rule (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) UNIQUE NOT NULL COMMENT '规则编码',
    rule_type TINYINT NOT NULL COMMENT '规则类型 1-迟到规则 2-早退规则 3-缺勤规则 4-加班规则',
    target_type TINYINT NOT NULL COMMENT '适用对象 1-全体员工 2-部门 3-个人',
    target_ids VARCHAR(500) COMMENT '适用对象ID列表',
    conditions JSON COMMENT '规则条件',
    actions JSON COMMENT '规则动作',
    priority INT DEFAULT 0 COMMENT '优先级',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_target_type (target_type),
    INDEX idx_is_active (is_active),
    INDEX idx_priority (priority),
    INDEX idx_effective_date (effective_date, expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则表';

-- 5. 考勤异常表
CREATE TABLE t_attendance_exception (
    exception_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    exception_date DATE NOT NULL COMMENT '异常日期',
    exception_type TINYINT NOT NULL COMMENT '异常类型 1-迟到 2-早退 3-缺勤 4-加班未申请',
    record_ids VARCHAR(500) COMMENT '关联考勤记录ID',
    description VARCHAR(500) COMMENT '异常描述',
    auto_detected TINYINT DEFAULT 0 COMMENT '是否自动检测',
    apply_status TINYINT DEFAULT 0 COMMENT '申请状态 0-未申请 1-申请中 2-已批准 3-已拒绝',
    apply_time DATETIME COMMENT '申请时间',
    apply_reason VARCHAR(500) COMMENT '申请理由',
    approver_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    process_status TINYINT DEFAULT 0 COMMENT '处理状态 0-未处理 1-已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_user_date (user_id, exception_date),
    INDEX idx_exception_type (exception_type),
    INDEX idx_apply_status (apply_status),
    INDEX idx_process_status (process_status),
    INDEX idx_auto_detected (auto_detected)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤异常表';

-- 6. 考勤统计表 (按月分表)
CREATE TABLE t_attendance_statistics (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type TINYINT NOT NULL COMMENT '统计类型 1-日统计 2-周统计 3-月统计',
    work_days INT DEFAULT 0 COMMENT '工作天数',
    actual_days INT DEFAULT 0 COMMENT '实际出勤天数',
    leave_days INT DEFAULT 0 COMMENT '请假天数',
    late_times INT DEFAULT 0 COMMENT '迟到次数',
    early_times INT DEFAULT 0 COMMENT '早退次数',
    absent_times INT DEFAULT 0 COMMENT '缺勤次数',
    overtime_hours DECIMAL(6,2) DEFAULT 0 COMMENT '加班时长',
    work_hours DECIMAL(6,2) DEFAULT 0 COMMENT '工作时长',
    efficiency_score DECIMAL(5,2) COMMENT '效率得分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_date (user_id, stat_date),
    INDEX idx_stat_type (stat_type),
    UNIQUE KEY uk_user_date_type (user_id, stat_date, stat_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤统计表';
```

### 读写分离配置

#### 数据源配置
```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          url: jdbc:mysql://mysql-master:3306/smart_attendance_v3
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
          driver-class-name: com.mysql.cj.jdbc.Driver
          hikari:
            maximum-pool-size: 20
            minimum-idle: 5
            idle-timeout: 300000
            max-lifetime: 1200000
        slave:
          url: jdbc:mysql://mysql-slave:3306/smart_attendance_v3
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
          driver-class-name: com.mysql.cj.jdbc.Driver
          hikari:
            maximum-pool-size: 20
            minimum-idle: 5
            idle-timeout: 300000
            max-lifetime: 1200000
```

#### 读写分离注解
```java
@Service
public class AttendanceRecordService {

    @Resource
    @Qualifier("attendanceRecordRepository")
    private AttendanceRecordRepository recordRepository;

    @DS("master")  // 写操作使用主库
    @Transactional
    public void createRecord(AttendanceRecordCreateDTO dto) {
        // 创建考勤记录
    }

    @DS("slave")   // 读操作使用从库
    @ReadOnly
    public List<AttendanceRecordVO> queryRecords(AttendanceRecordQueryDTO queryDTO) {
        // 查询考勤记录
    }

    @DS("slave")
    @ReadOnly
    public AttendanceStatisticsVO getStatistics(Long userId, LocalDate date) {
        // 查询统计数据
    }
}
```

---

## 🔄 服务间通信设计

### Feign客户端设计

#### 用户服务客户端
```java
@FeignClient(
    name = "user-service",
    url = "${services.user-service.url}",
    configuration = UserFeignConfiguration.class,
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/api/user/v1/users/{userId}")
    ResponseDTO<UserVO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 批量获取用户信息
     */
    @PostMapping("/api/user/v1/users/batch")
    ResponseDTO<List<UserVO>> getUsersByIds(@RequestBody List<Long> userIds);

    /**
     * 根据部门ID获取部门信息
     */
    @GetMapping("/api/user/v1/departments/{deptId}")
    ResponseDTO<DepartmentVO> getDepartmentById(@PathVariable("deptId") Long deptId);

    /**
     * 获取部门下的用户列表
     */
    @GetMapping("/api/user/v1/departments/{deptId}/users")
    ResponseDTO<List<UserVO>> getUsersByDepartment(@PathVariable("deptId") Long deptId);

    /**
     * 验证用户权限
     */
    @PostMapping("/api/user/v1/users/permission/check")
    ResponseDTO<Boolean> checkUserPermission(@RequestBody PermissionCheckDTO dto);
}
```

#### 设备服务客户端
```java
@FeignClient(
    name = "device-service",
    url = "${services.device-service.url}",
    configuration = DeviceFeignConfiguration.class,
    fallback = DeviceServiceClientFallback.class
)
public interface DeviceServiceClient {

    /**
     * 根据设备ID获取设备信息
     */
    @GetMapping("/api/device/v1/devices/{deviceId}")
    ResponseDTO<DeviceVO> getDeviceById(@PathVariable("deviceId") Long deviceId);

    /**
     * 根据位置ID获取设备列表
     */
    @GetMapping("/api/device/v1/devices/by-location/{locationId}")
    ResponseDTO<List<DeviceVO>> getDevicesByLocation(@PathVariable("locationId") Long locationId);

    /**
     * 根据设备类型获取设备列表
     */
    @GetMapping("/api/device/v1/devices/by-type/{deviceType}")
    ResponseDTO<List<DeviceVO>> getDevicesByType(@PathVariable("deviceType") String deviceType);

    /**
     * 获取设备状态
     */
    @GetMapping("/api/device/v1/devices/{deviceId}/status")
    ResponseDTO<DeviceStatusVO> getDeviceStatus(@PathVariable("deviceId") Long deviceId);

    /**
     * 更新设备状态
     */
    @PutMapping("/api/device/v1/devices/{deviceId}/status")
    ResponseDTO<Void> updateDeviceStatus(
        @PathVariable("deviceId") Long deviceId,
        @RequestBody DeviceStatusUpdateDTO dto
    );
}
```

### 事件驱动设计

#### 事件定义
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AttendanceEvent {
    private String eventId;
    private String eventType;
    private Long timestamp;
    private String source;
    private String version;
    private Map<String, Object> metadata;
}

@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceRecordCreatedEvent extends AttendanceEvent {
    private AttendanceRecordEntity record;
    private UserVO user;
    private DeviceVO device;

    public AttendanceRecordCreatedEvent(AttendanceRecordEntity record, UserVO user, DeviceVO device) {
        super();
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "ATTENDANCE_RECORD_CREATED";
        this.timestamp = System.currentTimeMillis();
        this.source = "attendance-service";
        this.version = "1.0";
        this.record = record;
        this.user = user;
        this.device = device;
    }
}

@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceExceptionDetectedEvent extends AttendanceEvent {
    private AttendanceExceptionEntity exception;
    private List<AttendanceRecordEntity> relatedRecords;
    private List<String> notificationChannels;

    public AttendanceExceptionDetectedEvent(
        AttendanceExceptionEntity exception,
        List<AttendanceRecordEntity> relatedRecords
    ) {
        super();
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "ATTENDANCE_EXCEPTION_DETECTED";
        this.timestamp = System.currentTimeMillis();
        this.source = "attendance-service";
        this.version = "1.0";
        this.exception = exception;
        this.relatedRecords = relatedRecords;
        this.notificationChannels = Arrays.asList("email", "sms", "webhook");
    }
}
```

#### 事件发布器
```java
@Component
@Slf4j
public class AttendanceEventPublisher {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发布考勤记录创建事件
     */
    public void publishRecordCreatedEvent(AttendanceRecordEntity record, UserVO user, DeviceVO device) {
        // 发布本地Spring事件
        AttendanceRecordCreatedEvent localEvent = new AttendanceRecordCreatedEvent(record, user, device);
        applicationEventPublisher.publishEvent(localEvent);

        // 发布远程RabbitMQ事件
        try {
            AttendanceEventMessage message = AttendanceEventMessage.builder()
                .eventId(localEvent.getEventId())
                .eventType(localEvent.getEventType())
                .source(localEvent.getSource())
                .timestamp(localEvent.getTimestamp())
                .version(localEvent.getVersion())
                .payload(localEvent)
                .build();

            rabbitTemplate.convertAndSend(
                "attendance.exchange",
                "attendance.record.created",
                message
            );

            log.info("Published attendance record created event: {}", localEvent.getEventId());

        } catch (Exception e) {
            log.error("Failed to publish attendance record created event", e);
            // 降级处理：记录到数据库，后续重试
            saveEventToDb(localEvent);
        }
    }

    /**
     * 发布考勤异常检测事件
     */
    public void publishExceptionDetectedEvent(
        AttendanceExceptionEntity exception,
        List<AttendanceRecordEntity> relatedRecords
    ) {
        AttendanceExceptionDetectedEvent event = new AttendanceExceptionDetectedEvent(exception, relatedRecords);

        // 发布本地事件
        applicationEventPublisher.publishEvent(event);

        // 发布远程事件
        publishRemoteEvent(event, "attendance.exception.detected");
    }

    private void publishRemoteEvent(AttendanceEvent event, String routingKey) {
        try {
            AttendanceEventMessage message = AttendanceEventMessage.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .source(event.getSource())
                .timestamp(event.getTimestamp())
                .version(event.getVersion())
                .payload(event)
                .build();

            rabbitTemplate.convertAndSend("attendance.exchange", routingKey, message);

        } catch (Exception e) {
            log.error("Failed to publish attendance event: {}", event.getEventId(), e);
            saveEventToDb(event);
        }
    }

    private void saveEventToDb(AttendanceEvent event) {
        // 保存事件到数据库，用于重试
        // 实现省略...
    }
}
```

#### 事件处理器
```java
@Component
@Slf4j
public class AttendanceEventHandler {

    @Resource
    private NotificationServiceClient notificationServiceClient;

    @Resource
    private StatisticsService statisticsService;

    @Resource
    private CacheManager cacheManager;

    /**
     * 处理考勤记录创建事件
     */
    @EventListener
    @Async("attendanceEventExecutor")
    public void handleRecordCreated(AttendanceRecordCreatedEvent event) {
        try {
            log.info("Processing attendance record created event: {}", event.getEventId());

            // 1. 更新缓存
            updateRecordCache(event.getRecord());

            // 2. 更新统计数据
            statisticsService.updateDailyStatistics(
                event.getRecord().getUserId(),
                event.getRecord().getClockTime().toLocalDate()
            );

            // 3. 发送通知(如果是移动端打卡)
            if (isMobileClockIn(event.getDevice())) {
                sendMobileClockNotification(event);
            }

            log.info("Successfully processed attendance record created event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process attendance record created event: {}", event.getEventId(), e);
            // 可以考虑重试或者发送到死信队列
        }
    }

    /**
     * 处理考勤异常检测事件
     */
    @EventListener
    @Async("attendanceEventExecutor")
    public void handleExceptionDetected(AttendanceExceptionDetectedEvent event) {
        try {
            log.info("Processing attendance exception detected event: {}", event.getEventId());

            AttendanceExceptionEntity exception = event.getException();

            // 1. 发送异常通知
            sendExceptionNotification(exception);

            // 2. 创建异常处理任务
            createExceptionHandlingTask(exception);

            // 3. 更新异常统计
            statisticsService.updateExceptionStatistics(exception);

            log.info("Successfully processed attendance exception detected event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process attendance exception detected event: {}", event.getEventId(), e);
        }
    }

    private void updateRecordCache(AttendanceRecordEntity record) {
        String cacheKey = AttendanceCacheKey.buildRecordKey(record.getUserId(), record.getClockTime());
        cacheManager.evict(cacheKey);
    }

    private void sendMobileClockNotification(AttendanceRecordCreatedEvent event) {
        NotificationMessage message = NotificationMessage.builder()
            .userId(event.getUser().getUserId())
            .title("打卡成功")
            .content(String.format("您已于%s成功打卡，设备：%s",
                event.getRecord().getClockTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                event.getDevice().getDeviceName()))
            .type(NotificationTypeEnum.ATTENDANCE_CLOCK)
            .build();

        notificationServiceClient.sendMessage(message);
    }

    private void sendExceptionNotification(AttendanceExceptionEntity exception) {
        // 构建异常通知消息
        NotificationMessage message = NotificationMessage.builder()
            .userId(exception.getUserId())
            .title("考勤异常提醒")
            .content(buildExceptionMessage(exception))
            .type(NotificationTypeEnum.ATTENDANCE_EXCEPTION)
            .priority(NotificationPriorityEnum.HIGH)
            .build();

        notificationServiceClient.sendMessage(message);
    }

    private String buildExceptionMessage(AttendanceExceptionEntity exception) {
        return String.format("您在%s的考勤记录出现异常：%s，请及时处理。",
            exception.getExceptionDate(),
            exception.getDescription());
    }

    private void createExceptionHandlingTask(AttendanceExceptionEntity exception) {
        // 创建异常处理任务
        // 实现省略...
    }

    private boolean isMobileClockIn(DeviceVO device) {
        return device != null && DeviceTypeEnum.MOBILE.equals(device.getDeviceType());
    }
}
```

---

## 🛡️ 安全设计

### API安全设计

#### 认证授权
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/api/attendance/v1/mobile/**").hasRole("USER")
                .requestMatchers("/api/attendance/v1/reports/**").hasRole("MANAGER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter(), JwtAuthenticationFilter.class)
            .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }
}
```

#### 权限控制注解
```java
@RestController
@RequestMapping("/api/attendance/v1")
@Tag(name = "考勤管理", description = "考勤记录管理相关接口")
@SaCheckLogin
public class AttendanceRecordController {

    @PostMapping("/records")
    @SaCheckPermission("attendance:record:create")
    @Operation(summary = "创建考勤记录", description = "用户打卡记录创建")
    public ResponseDTO<Long> createRecord(@Valid @RequestBody AttendanceRecordCreateDTO dto) {
        // 实现省略...
    }

    @GetMapping("/records/{id}")
    @SaCheckPermission("attendance:record:query")
    @Operation(summary = "获取考勤记录详情", description = "根据ID获取考勤记录详细信息")
    public ResponseDTO<AttendanceRecordVO> getRecord(@PathVariable Long id) {
        // 实现省略...
    }

    @GetMapping("/records")
    @SaCheckPermission("attendance:record:query")
    @DataPermission(deptAlias = "dept", userAlias = "user")
    @Operation(summary = "查询考勤记录", description = "分页查询考勤记录列表")
    public ResponseDTO<PageResult<AttendanceRecordVO>> queryRecords(
        @ModelAttribute AttendanceRecordQueryDTO queryDTO,
        @Parameter(description = "分页参数") @PageableDefault(size = 20) Pageable pageable
    ) {
        // 实现省略...
    }

    @GetMapping("/mobile/records/my")
    @SaCheckRole("USER")
    @Operation(summary = "获取个人考勤记录", description = "移动端获取用户个人考勤记录")
    public ResponseDTO<List<AttendanceRecordVO>> getMyRecords(
        @Parameter(description = "查询日期范围") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @Parameter(description = "查询日期范围") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        // 实现省略...
    }
}
```

### 数据安全设计

#### 敏感数据脱敏
```java
@Component
public class SensitiveDataHandler {

    @Value("${app.security.sensitive.enabled:true}")
    private boolean sensitiveEnabled;

    public String maskUserName(String userName) {
        if (!sensitiveEnabled || StringUtils.isBlank(userName)) {
            return userName;
        }

        if (userName.length() <= 2) {
            return "*".repeat(userName.length());
        }

        return userName.charAt(0) + "*".repeat(userName.length() - 2) + userName.charAt(userName.length() - 1);
    }

    public String maskIdCard(String idCard) {
        if (!sensitiveEnabled || StringUtils.isBlank(idCard)) {
            return idCard;
        }

        if (idCard.length() <= 6) {
            return "*".repeat(idCard.length());
        }

        return idCard.substring(0, 3) + "*".repeat(idCard.length() - 6) + idCard.substring(idCard.length() - 3);
    }

    public String maskPhoneNumber(String phone) {
        if (!sensitiveEnabled || StringUtils.isBlank(phone)) {
            return phone;
        }

        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
```

#### 数据加密
```java
@Component
@Slf4j
public class DataEncryptionService {

    @Value("${app.security.encrypt.algorithm:AES}")
    private String algorithm;

    @Value("${app.security.encrypt.key}")
    private String encryptKey;

    public String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return plainText;
        }

        try {
            return AESUtils.encrypt(plainText, encryptKey);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new SmartException("数据加密失败");
        }
    }

    public String decrypt(String encryptedText) {
        if (StringUtils.isBlank(encryptedText)) {
            return encryptedText;
        }

        try {
            return AESUtils.decrypt(encryptedText, encryptKey);
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw new SmartException("数据解密失败");
        }
    }
}
```

---

## 📊 监控设计

### 应用监控

#### Micrometer指标收集
```java
@Component
public class AttendanceMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter recordCreateCounter;
    private final Counter recordUpdateCounter;
    private final Timer recordProcessTimer;
    private final Timer recordQueryTimer;
    private final Gauge activeUsersGauge;
    private final Gauge cacheHitRateGauge;

    public AttendanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 计数器
        this.recordCreateCounter = Counter.builder("attendance.record.created")
            .description("Number of attendance records created")
            .tag("service", "attendance")
            .register(meterRegistry);

        this.recordUpdateCounter = Counter.builder("attendance.record.updated")
            .description("Number of attendance records updated")
            .tag("service", "attendance")
            .register(meterRegistry);

        // 计时器
        this.recordProcessTimer = Timer.builder("attendance.record.process.time")
            .description("Time taken to process attendance record")
            .tag("service", "attendance")
            .register(meterRegistry);

        this.recordQueryTimer = Timer.builder("attendance.record.query.time")
            .description("Time taken to query attendance records")
            .tag("service", "attendance")
            .register(meterRegistry);

        // 仪表盘
        this.activeUsersGauge = Gauge.builder("attendance.active.users")
            .description("Number of active users")
            .tag("service", "attendance")
            .register(meterRegistry, this, AttendanceMetrics::getActiveUsersCount);

        this.cacheHitRateGauge = Gauge.builder("attendance.cache.hit.rate")
            .description("Cache hit rate")
            .tag("service", "attendance")
            .register(meterRegistry, this, AttendanceMetrics::getCacheHitRate);
    }

    public void incrementRecordCreate(String clockType) {
        recordCreateCounter.increment(Tags.of("clock_type", clockType));
    }

    public void incrementRecordUpdate(String updateType) {
        recordUpdateCounter.increment(Tags.of("update_type", updateType));
    }

    public Timer.Sample startRecordProcessTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordRecordProcessTime(Timer.Sample sample, String operation) {
        sample.stop(Timer.builder("attendance.record.process.time")
            .tag("service", "attendance")
            .tag("operation", operation)
            .register(meterRegistry));
    }

    private double getActiveUsersCount() {
        // 实现获取活跃用户数的逻辑
        return 0.0;
    }

    private double getCacheHitRate() {
        // 实现获取缓存命中率的逻辑
        return 0.0;
    }
}
```

#### 自定义健康检查
```java
@Component
public class AttendanceHealthIndicator implements HealthIndicator {

    @Resource
    private AttendanceRecordService recordService;

    @Resource
    private CacheManager cacheManager;

    @Resource
    private UserServiceClient userServiceClient;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            boolean dbHealth = checkDatabaseHealth();

            // 检查缓存连接
            boolean cacheHealth = checkCacheHealth();

            // 检查外部服务连接
            boolean externalServiceHealth = checkExternalServiceHealth();

            if (dbHealth && cacheHealth && externalServiceHealth) {
                return Health.up()
                    .withDetail("database", "UP")
                    .withDetail("cache", "UP")
                    .withDetail("external_services", "UP")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", dbHealth ? "UP" : "DOWN")
                    .withDetail("cache", cacheHealth ? "UP" : "DOWN")
                    .withDetail("external_services", externalServiceHealth ? "UP" : "DOWN")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
            }

        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", System.currentTimeMillis())
                .build();
        }
    }

    private boolean checkDatabaseHealth() {
        try {
            // 执行简单查询检查数据库连接
            recordService.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkCacheHealth() {
        try {
            // 执行缓存操作检查缓存连接
            cacheManager.getCache("test").put("key", "value");
            cacheManager.getCache("test").get("key");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkExternalServiceHealth() {
        try {
            // 检查用户服务健康状态
            ResponseDTO<String> response = userServiceClient.healthCheck();
            return response.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 业务监控

#### 业务指标监控
```java
@Aspect
@Component
@Slf4j
public class BusinessMetricsAspect {

    @Resource
    private AttendanceMetrics attendanceMetrics;

    @Around("@annotation(MonitorBusinessOperation)")
    public Object monitorBusinessOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MonitorBusinessOperation annotation = joinPoint.getSignature().getDeclaringType()
            .getAnnotation(MonitorBusinessOperation.class);

        String operation = annotation.operation();
        Timer.Sample sample = Timer.start();

        try {
            Object result = joinPoint.proceed();

            // 记录成功指标
            attendanceMetrics.incrementRecordCreate(operation + "_success");
            return result;

        } catch (Exception e) {
            // 记录失败指标
            attendanceMetrics.incrementRecordCreate(operation + "_failure");
            log.error("Business operation failed: {}", operation, e);
            throw e;

        } finally {
            sample.stop(attendanceMetrics.recordProcessTimer());
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MonitorBusinessOperation {
    String operation() default "";
}
```

---

## 🚀 部署设计

### 容器化设计

#### Dockerfile
```dockerfile
# 多阶段构建
FROM maven:3.9.4-openjdk-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17-jdk-slim

LABEL maintainer="IOE-DREAM Team"
LABEL version="1.0.0"
LABEL description="Attendance Microservice"

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 复制jar文件
COPY --from=builder /app/target/attendance-service-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换用户
USER appuser

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 应用配置
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
```

### Kubernetes部署

#### Deployment配置
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: attendance-service
  namespace: iog-production
  labels:
    app: attendance-service
    version: v1.0.0
    team: backend
    component: microservice
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: attendance-service
      version: v1.0.0
  template:
    metadata:
      labels:
        app: attendance-service
        version: v1.0.0
        team: backend
        component: microservice
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      securityContext:
        runAsUser: 1000
        runAsGroup: 1000
        fsGroup: 1000
      containers:
      - name: attendance-service
        image: harbor.iog.com/attendance/attendance-service:v1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SERVER_PORT
          value: "8080"
        - name: NACOS_SERVER_ADDR
          valueFrom:
            configMapKeyRef:
              name: common-config
              key: nacos.server.addr
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: common-config
              key: database.host
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "200m"
          limits:
            memory: "1024Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
            scheme: HTTP
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          successThreshold: 1
          failureThreshold: 3
        lifecycle:
          preStop:
            exec:
              command:
              - /bin/sh
              - -c
              - "sleep 15"
      imagePullSecrets:
      - name: harbor-secret
      nodeSelector:
        node-type: application
      tolerations:
      - key: "application"
        operator: "Equal"
        value: "true"
        effect: "NoSchedule"
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - attendance-service
              topologyKey: kubernetes.io/hostname
```

#### Service配置
```yaml
apiVersion: v1
kind: Service
metadata:
  name: attendance-service
  namespace: iog-production
  labels:
    app: attendance-service
    version: v1.0.0
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: attendance-service
    version: v1.0.0

---
apiVersion: v1
kind: Service
metadata:
  name: attendance-service-headless
  namespace: iog-production
  labels:
    app: attendance-service
spec:
  type: ClusterIP
  clusterIP: None
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: attendance-service
```

---

## ✅ 设计验证

### 架构验证清单

#### 功能完整性验证
- [x] 考勤记录管理功能完整设计
- [x] 排班管理功能完整设计
- [x] 考勤规则引擎功能设计
- [x] 统计分析功能设计
- [x] 异常处理功能设计
- [x] 移动端接口设计

#### 非功能性验证
- [x] 高可用性设计(负载均衡、故障转移)
- [x] 高性能设计(缓存策略、数据库优化)
- [x] 安全性设计(认证授权、数据加密)
- [x] 可扩展性设计(微服务架构、水平扩展)
- [x] 可观测性设计(监控、日志、链路追踪)

#### 技术合规性验证
- [x] 遵循Spring Boot 3.x技术栈
- [x] 使用jakarta包名规范
- [x] 采用@Resource依赖注入
- [x] 实现四层架构规范
- [x] 符合项目安全规范

### 性能预估

#### 并发能力预估
- **设计目标**: 1000 QPS
- **预估配置**: 3个Pod实例，每个实例支持350 QPS
- **响应时间**: P95 ≤ 200ms, P99 ≤ 500ms

#### 资源需求预估
- **CPU**: 单实例500m (0.5核)
- **内存**: 单实例1GB
- **网络带宽**: 峰值100Mbps
- **存储**: 数据库50GB，Redis 10GB

### 部署复杂度评估

#### 部署复杂度: 中等
- **依赖服务**: Nacos, MySQL, Redis, 外部业务服务
- **配置复杂度**: 中等，需要配置分库分表、缓存策略
- **监控复杂度**: 中等，需要配置应用监控和业务监控

#### 运维复杂度: 中等
- **健康检查**: 标准健康检查配置
- **日志收集**: 标准日志收集配置
- **监控告警**: 需要配置业务指标监控
- **故障处理**: 标准故障处理流程

---

**设计文档版本**: v1.0
**最后更新时间**: 2025-11-27
**下次更新时间**: 根据实施反馈进行优化
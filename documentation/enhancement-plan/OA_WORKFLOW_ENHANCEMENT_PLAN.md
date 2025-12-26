# OA工作流模块企业级完善计划

**当前完成度**: 60%
**目标完成度**: 100%
**预计工期**: 2周
**质量标准**: 企业级生产就绪

---

## 📊 现状分析

### ✅ 已实现功能（60%）

#### 1. 工作流引擎集成（10个控制器）
```
ioedream-oa-service/
├── OAFileController.java                    # 文件管理
├── ApprovalController.java                  # 审批管理
├── ApprovalConfigController.java            # 审批配置
├── WorkflowEngineController.java            # 工作流引擎
├── WorkflowAdvancedController.java         # 高级工作流
├── WorkflowPerformanceController.java      # 性能优化
├── WorkflowStartCompatController.java      # 启动兼容
├── FormDesignerController.java             # 表单设计器
├── ProcessDesignerController.java          # 流程设计器
└── WorkflowWebSocketController.java        # WebSocket
```

#### 2. Flowable工作流引擎
- ✅ BPMN 2.0流程引擎集成
- ✅ 流程定义管理
- ✅ 流程实例管理
- ✅ 任务管理
- ✅ 历史查询

#### 3. 表单和流程设计器
- ✅ 表单设计器（FormDesignerController）
- ✅ 流程设计器（ProcessDesignerController）
- ✅ 动态表单Schema
- ✅ 流程模板管理

#### 4. 审批核心功能
- ✅ 审批配置管理
- ✅ 审批实例管理
- ✅ 审批任务管理
- ✅ 审批统计

---

## 🎯 企业级增强方案（40%提升）

### 🚀 优先级P0（核心功能完善）

#### 1. 可视化审批流程配置

**当前问题**:
- 缺少可视化配置界面
- 配置项分散，不易理解
- 缺少流程模板库

**增强方案**:

##### 1.1 可视化流程配置UI
```java
@RestController
@RequestMapping("/api/v1/oa/workflow/visual-config")
public class VisualWorkflowConfigController {

    /**
     * 获取可视化流程配置
     * - 流程图（BPMN）
     * - 节点配置
     * - 连线配置
     * - 条件表达式
     */
    @GetMapping("/{processDefinitionId}")
    public ResponseDTO<VisualWorkflowConfig> getVisualConfig(
            @PathVariable String processDefinitionId) {

        VisualWorkflowConfig config = VisualWorkflowConfig.builder()
                .processDefinitionId(processDefinitionId)
                .processName("请假审批")
                .nodes(getProcessNodes(processDefinitionId))
                .edges(getProcessEdges(processDefinitionId))
                .nodeConfigs(getNodeConfigs(processDefinitionId))
                .build();

        return ResponseDTO.ok(config);
    }

    /**
     * 保存可视化流程配置
     */
    @PostMapping("/save")
    public ResponseDTO<Void> saveVisualConfig(
            @RequestBody @Valid VisualWorkflowConfigForm form) {

        // 1. 验证配置合法性
        validateConfig(form);

        // 2. 生成BPMN XML
        String bpmnXml = generateBpmnXml(form);

        // 3. 部署流程
        workflowService.deployProcessDefinition(
                form.getProcessKey(),
                form.getProcessName(),
                bpmnXml
        );

        return ResponseDTO.ok();
    }

    /**
     * 流程模板库
     * - 请假审批模板
     * - 报销审批模板
     * - 采购审批模板
     * - 用车申请模板
     */
    @GetMapping("/templates")
    public ResponseDTO<List<ProcessTemplate>> getProcessTemplates() {
        List<ProcessTemplate> templates = Arrays.asList(
                ProcessTemplate.builder()
                        .templateId("LEAVE_REQUEST")
                        .templateName("请假审批")
                        .category("人事管理")
                        .description("员工请假申请审批流程")
                        .bpmnXml(loadTemplateBpmn("leave_request.bpmn20.xml"))
                        .screenshot("/templates/leave_request.png")
                        .build(),
                // ... 更多模板
        );

        return ResponseDTO.ok(templates);
    }

    /**
     * 使用模板创建流程
     */
    @PostMapping("/apply-template")
    public ResponseDTO<String> applyTemplate(
            @RequestParam String templateId,
            @RequestParam String newProcessKey,
            @RequestParam String newProcessName) {

        // 1. 加载模板
        ProcessTemplate template = loadTemplate(templateId);

        // 2. 复制并创建新流程
        String newProcessId = workflowService.copyProcessDefinition(
                template.getBpmnXml(),
                newProcessKey,
                newProcessName
        );

        return ResponseDTO.ok(newProcessId);
    }
}
```

##### 1.2 流程配置数据模型
```java
@Data
@Builder
public class VisualWorkflowConfig {
    private String processDefinitionId;
    private String processKey;
    private String processName;
    private String processCategory;
    private List<ProcessNode> nodes;
    private List<ProcessEdge> edges;
    private Map<String, NodeConfig> nodeConfigs;
}

@Data
@Builder
public class ProcessNode {
    private String nodeId;
    private String nodeType;  // START, END, USER_TASK, GATEWAY
    private String name;
    private Integer x;  // UI坐标
    private Integer y;
    private Integer width;
    private Integer height;
    private List<String> incoming;
    private List<String> outgoing;
}

@Data
@Builder
public class ProcessEdge {
    private String edgeId;
    private String sourceNodeId;
    private String targetNodeId;
    private String conditionExpression;  // 条件表达式
    private String name;
}

@Data
@Builder
public class NodeConfig {
    private String nodeId;
    private String assigneeType;  // USER, ROLE, DEPT_LEADER, SCRIPT
    private String assigneeValue;
    private String formKey;
    private Integer dueDate;  // 截止时间（天）
    private String priority;  // 优先级
    private Boolean multiInstance;  // 是否会签
    private String notificationType;  // 通知方式
}
```

#### 2. 低代码表单设计器增强

**当前问题**:
- 表单组件类型有限
- 缺少表单验证规则
- 缺少表单逻辑配置

**增强方案**:

##### 2.1 增强表单设计器
```java
@RestController
@RequestMapping("/api/v1/oa/form/designer")
public class EnhancedFormDesignerController {

    /**
     * 表单组件库
     * - 基础组件：文本、数字、日期、下拉、多选
     * - 高级组件：图片、附件、表格、子表单
     * - 业务组件：人员选择器、部门选择器、审批历史
     */
    @GetMapping("/components")
    public ResponseDTO<List<FormComponent>> getFormComponents() {
        List<FormComponent> components = Arrays.asList(
                // 基础组件
                FormComponent.builder()
                        .componentId("TEXT_INPUT")
                        .componentName("单行文本")
                        .componentType("INPUT")
                        .icon("icon-text")
                        .category("基础组件")
                        .config(getTextInputConfig())
                        .build(),

                FormComponent.builder()
                        .componentId("NUMBER_INPUT")
                        .componentName("数字输入")
                        .componentType("INPUT_NUMBER")
                        .icon("icon-number")
                        .category("基础组件")
                        .build(),

                FormComponent.builder()
                        .componentId("DATE_PICKER")
                        .componentName("日期选择")
                        .componentType("DATE")
                        .icon("icon-calendar")
                        .category("基础组件")
                        .build(),

                // 业务组件
                FormComponent.builder()
                        .componentId("EMPLOYEE_SELECTOR")
                        .componentName("人员选择器")
                        .componentType("EMPLOYEE_SELECT")
                        .icon("icon-user")
                        .category("业务组件")
                        .config(getEmployeeSelectorConfig())
                        .build(),

                FormComponent.builder()
                        .componentId("DEPARTMENT_SELECTOR")
                        .componentName("部门选择器")
                        .componentType("DEPARTMENT_SELECT")
                        .icon("icon-dept")
                        .category("业务组件")
                        .build()
        );

        return ResponseDTO.ok(components);
    }

    /**
     * 表单验证规则
     * - 必填验证
     * - 格式验证（邮箱、手机、身份证）
     * - 长度验证
     * - 数值范围验证
     * - 自定义正则表达式
     * - 跨字段验证
     */
    @PostMapping("/validation-rules")
    public ResponseDTO<List<ValidationResult>> validateForm(
            @RequestBody FormValidationRequest request) {

        List<ValidationResult> results = new ArrayList<>();

        for (FormField field : request.getFields()) {
            // 必填验证
            if (field.getRequired() && isEmpty(field.getValue())) {
                results.add(ValidationResult.builder()
                        .fieldId(field.getFieldId())
                        .fieldName(field.getFieldName())
                        .valid(false)
                        .errorMessage("该字段为必填项")
                        .build());
                continue;
            }

            // 格式验证
            if (!validateFormat(field)) {
                results.add(ValidationResult.builder()
                        .fieldId(field.getFieldId())
                        .fieldName(field.getFieldName())
                        .valid(false)
                        .errorMessage("格式不正确")
                        .build());
            }

            // 长度验证
            if (!validateLength(field)) {
                results.add(ValidationResult.builder()
                        .fieldId(field.getFieldId())
                        .fieldName(field.getFieldName())
                        .valid(false)
                        .errorMessage("长度超出限制")
                        .build());
            }
        }

        return ResponseDTO.ok(results);
    }

    /**
     * 表单逻辑配置
     * - 显示/隐藏逻辑
     * - 启用/禁用逻辑
     * - 联动下拉选项
     * - 自动计算字段
     */
    @PostMapping("/logic-config")
    public ResponseDTO<Void> saveFormLogic(
            @RequestBody FormLogicConfig config) {

        // 保存表单逻辑配置
        formLogicService.saveFormLogic(config);

        return ResponseDTO.ok();
    }
}
```

#### 3. 移动端审批支持

**当前问题**:
- 移动端界面不友好
- 缺少移动端专属功能
- 性能未优化

**增强方案**:

##### 3.1 移动端审批控制器
```java
@RestController
@RequestMapping("/api/v1/oa/mobile/approval")
public class MobileApprovalController {

    /**
     * 获取待办列表（移动端优化）
     * - 分页加载
     * - 只返回必要字段
     * - 支持下拉刷新
     */
    @GetMapping("/pending-tasks")
    public ResponseDTO<PageResult<MobileTaskVO>> getPendingTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        PageResult<MobileTaskVO> result = mobileApprovalService.getPendingTasks(
                SecurityUtils.getLoginUserId(),
                pageNum,
                pageSize
        );

        return ResponseDTO.ok(result);
    }

    /**
     * 快速审批（一键操作）
     * - 同意
     * - 拒绝
     * - 转办
     */
    @PostMapping("/quick-approve")
    public ResponseDTO<Void> quickApprove(
            @RequestParam String taskId,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String comment) {

        mobileApprovalService.quickApprove(taskId, approved, comment);

        return ResponseDTO.ok();
    }

    /**
     * 批量审批
     * - 批量同意
     * - 批量拒绝
     */
    @PostMapping("/batch-approve")
    public ResponseDTO<BatchApprovalResult> batchApprove(
            @RequestBody BatchApprovalRequest request) {

        BatchApprovalResult result = mobileApprovalService.batchApprove(
                request.getTaskIds(),
                request.getApproved(),
                request.getComment()
        );

        return ResponseDTO.ok(result);
    }

    /**
     * 语音审批（语音转文字）
     */
    @PostMapping("/voice-approve")
    public ResponseDTO<Void> voiceApprove(
            @RequestParam String taskId,
            @RequestParam MultipartFile voiceFile) {

        // 1. 语音转文字
        String text = speechToTextService.convert(voiceFile);

        // 2. 解析审批意见
        ApprovalDecision decision = parseApprovalDecision(text);

        // 3. 执行审批
        mobileApprovalService.quickApprove(
                taskId,
                decision.getApproved(),
                decision.getComment()
        );

        return ResponseDTO.ok();
    }

    /**
     * 获取审批详情（移动端优化）
     * - 流程图
     * - 表单数据
     * - 审批历史
     * - 操作按钮
     */
    @GetMapping("/detail/{taskId}")
    public ResponseDTO<MobileApprovalDetailVO> getApprovalDetail(
            @PathVariable String taskId) {

        MobileApprovalDetailVO detail = mobileApprovalService.getApprovalDetail(taskId);

        return ResponseDTO.ok(detail);
    }
}
```

##### 3.2 移动端适配
```java
@Data
@Builder
public class MobileTaskVO {
    private String taskId;
    private String taskName;
    private String processName;
    private String applicantName;
    private String applicantAvatar;
    private LocalDateTime createTime;
    private Integer dueTime;  // 剩余时间（小时）
    private String priority;  // HIGH, MEDIUM, LOW
    private Boolean canApprove;
    private Boolean canReject;
    private Boolean canTransfer;
}

@Data
@Builder
public class MobileApprovalDetailVO {
    private String taskId;
    private String processName;
    private String taskName;

    // 流程图（移动端优化）
    private MobileProcessDiagram processDiagram;

    // 表单数据
    private Map<String, Object> formData;

    // 审批历史
    private List<ApprovalHistoryItem> approvalHistory;

    // 操作按钮配置
    private List<ActionConfig> availableActions;
}
```

---

### 🔧 优先级P1（功能完善）

#### 4. 审批统计分析

```java
@RestController
@RequestMapping("/api/v1/oa/approval/statistics")
public class ApprovalStatisticsController {

    /**
     * 审批效率统计
     * - 平均审批时长
     * - 超时率
     * - 审批通过率
     */
    @GetMapping("/efficiency")
    public ResponseDTO<ApprovalEfficiencyStatistics> getEfficiencyStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        ApprovalEfficiencyStatistics stats = approvalStatisticsService
                .calculateEfficiencyStatistics(startDate, endDate);

        return ResponseDTO.ok(stats);
    }

    /**
     * 审批工作量统计
     * - 按人员统计
     * - 按部门统计
     * - 按流程类型统计
     */
    @GetMapping("/workload")
    public ResponseDTO<List<WorkloadStatistics>> getWorkloadStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String groupBy) {

        List<WorkloadStatistics> stats = approvalStatisticsService
                .calculateWorkloadStatistics(startDate, endDate, groupBy);

        return ResponseDTO.ok(stats);
    }

    /**
     * 审批热力图
     * - 审批时间分布
     * - 瓶颈环节识别
     */
    @GetMapping("/heatmap")
    public ResponseDTO<List<ApprovalHeatmapData>> getApprovalHeatmap(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        List<ApprovalHeatmapData> heatmap = approvalStatisticsService
                .generateApprovalHeatmap(startDate, endDate);

        return ResponseDTO.ok(heatmap);
    }

    /**
     * 导出统计报表
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStatisticsReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String reportType) {

        byte[] reportData = approvalStatisticsService
                .generateReport(startDate, endDate, reportType);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=approval_statistics.xlsx")
                .body(reportData);
    }
}
```

#### 5. 审批流程仿真测试

```java
@RestController
@RequestMapping("/api/v1/oa/workflow/simulation")
public class WorkflowSimulationController {

    /**
     * 流程仿真
     * - 模拟流程执行
     * - 识别潜在问题
     * - 优化建议
     */
    @PostMapping("/simulate")
    public ResponseDTO<WorkflowSimulationResult> simulateWorkflow(
            @RequestBody WorkflowSimulationRequest request) {

        WorkflowSimulationResult result = workflowSimulationService
                .simulate(request);

        return ResponseDTO.ok(result);
    }

    /**
     * 流程诊断
     * - 识别死循环
     * - 识别孤立节点
     * - 识别不可达节点
     */
    @PostMapping("/diagnose")
    public ResponseDTO<List<WorkflowIssue>> diagnoseWorkflow(
            @RequestBody WorkflowDiagnosisRequest request) {

        List<WorkflowIssue> issues = workflowSimulationService
                .diagnose(request);

        return ResponseDTO.ok(issues);
    }

    /**
     * 性能预测
     * - 预估执行时间
     * - 预估资源消耗
     * - 预估并发处理能力
     */
    @PostMapping("/predict-performance")
    public ResponseDTO<PerformancePrediction> predictPerformance(
            @RequestBody PerformancePredictionRequest request) {

        PerformancePrediction prediction = workflowSimulationService
                .predictPerformance(request);

        return ResponseDTO.ok(prediction);
    }
}
```

#### 6. 审批消息推送优化

```java
@Service
public class EnhancedApprovalNotificationService {

    /**
     * 多渠道消息推送
     * - 站内信
     * - 邮件
     * - 短信
     * - 微信/钉钉/企业微信
     * - 移动端推送
     */
    public void sendApprovalNotification(
            String taskId,
            String notificationType,
            List<String> recipientIds) {

        for (String recipientId : recipientIds) {
            // 获取用户通知偏好
            UserNotificationPreference preference =
                    userPreferenceService.getNotificationPreference(recipientId);

            // 根据偏好发送通知
            if (preference.getStationMessageEnabled()) {
                sendStationMessage(taskId, recipientId);
            }

            if (preference.getEmailEnabled()) {
                sendEmailNotification(taskId, recipientId);
            }

            if (preference.getSmsEnabled()) {
                sendSmsNotification(taskId, recipientId);
            }

            if (preference.getWeChatEnabled()) {
                sendWeChatNotification(taskId, recipientId);
            }

            if (preference.getMobilePushEnabled()) {
                sendMobilePush(taskId, recipientId);
            }
        }
    }

    /**
     * 智能提醒策略
     * - 即将超时提醒
     * - 重要任务提醒
     * - 批量任务汇总提醒
     */
    @Scheduled(cron = "0 */10 * * * *")  // 每10分钟执行一次
    public void sendSmartReminders() {
        // 1. 查询即将超时的任务（<1小时）
        List<ApprovalTask> urgentTasks = findUrgentTasks();

        // 2. 发送提醒
        for (ApprovalTask task : urgentTasks) {
            sendUrgentReminder(task);
        }

        // 3. 批量汇总提醒
        sendBatchSummaryReminders();
    }

    /**
     * 消息去重
     * - 相同任务10分钟内只推送一次
     * - 紧急任务不受限制
     */
    public void sendNotificationWithDeduplication(
            String taskId,
            String recipientId,
            NotificationMessage message) {

        String dedupKey = taskId + "_" + recipientId;

        // 检查是否最近已发送
        if (notificationCache.getIfPresent(dedupKey) != null) {
            log.debug("[消息推送] 消息已发送，跳过: taskId={}, recipientId={}",
                    taskId, recipientId);
            return;
        }

        // 发送消息
        sendNotification(taskId, recipientId, message);

        // 记录到缓存（10分钟）
        notificationCache.put(dedupKey, true, 10, TimeUnit.MINUTES);
    }
}
```

---

## 📋 实施计划

### Week 1: P0核心功能

| 天数 | 任务 | 负责模块 | 交付物 |
|-----|------|---------|--------|
| Day 1-2 | 可视化流程配置 | VisualWorkflowConfigController | 可视化配置界面 |
| Day 3-4 | 低代码表单设计器 | EnhancedFormDesignerController | 组件库、验证规则 |
| Day 5-6 | 移动端审批 | MobileApprovalController | 移动端适配、快速审批 |

### Week 2: P1功能完善

| 天数 | 任务 | 负责模块 | 交付物 |
|-----|------|---------|--------|
| Day 1-2 | 审批统计分析 | ApprovalStatisticsController | 统计报表、热力图 |
| Day 3-4 | 流程仿真测试 | WorkflowSimulationController | 仿真、诊断、性能预测 |
| Day 5-6 | 消息推送优化 | EnhancedApprovalNotificationService | 多渠道推送、智能提醒 |

---

## ✅ 验收标准

### 功能完整性

- [x] 可视化流程配置界面
- [x] 低代码表单设计器（15+组件）
- [x] 移动端审批支持
- [x] 审批统计分析
- [x] 流程仿真测试
- [x] 多渠道消息推送

### 性能指标

| 指标 | 目标 |
|-----|------|
| 移动端待办列表加载 | <1秒 |
| 表单渲染时间 | <500ms |
| 审批操作响应 | <1秒 |
| 消息推送延迟 | <5秒 |
| 统计报表导出 | <10秒 |

### 代码质量

- 单元测试覆盖率 ≥ 85%
- 集成测试覆盖关键流程
- 移动端响应式设计
- 代码规范100%符合CLAUDE.md

---

**文档版本**: v1.0
**制定日期**: 2025-12-26
**预计完成**: 2026-01-09
**负责人**: IOE-DREAM架构团队

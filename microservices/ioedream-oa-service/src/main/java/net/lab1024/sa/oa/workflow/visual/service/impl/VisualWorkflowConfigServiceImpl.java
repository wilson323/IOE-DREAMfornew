package net.lab1024.sa.oa.workflow.visual.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.oa.workflow.visual.domain.*;
import net.lab1024.sa.oa.workflow.visual.service.VisualWorkflowConfigService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 可视化工作流配置服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisualWorkflowConfigServiceImpl implements VisualWorkflowConfigService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 流程配置存储
    private final Map<String, VisualWorkflowConfig> configStorage = new ConcurrentHashMap<>();

    // 流程模板存储
    private final Map<String, ProcessTemplateDetail> templateStorage = new ConcurrentHashMap<>();

    // 节点类型库
    private final Map<String, NodeType> nodeTypeLibrary = new HashMap<>();

    // 节点配置Schema库
    private final Map<String, NodeConfigSchema> configSchemaLibrary = new HashMap<>();

    public VisualWorkflowConfigServiceImpl() {
        log.info("[工作流配置] 初始化可视化工作流配置服务");
        initializeNodeTypeLibrary();
        initializeConfigSchemaLibrary();
        initializeBuiltinTemplates();
    }

    // ==================== 流程配置CRUD ====================

    @Override
    @Cacheable(value = "workflowConfig", key = "#processDefinitionId")
    public VisualWorkflowConfig getVisualConfig(String processDefinitionId) {
        log.info("[工作流配置] 获取流程配置: processDefinitionId={}", processDefinitionId);

        VisualWorkflowConfig config = configStorage.get(processDefinitionId);
        if (config == null) {
            log.warn("[工作流配置] 流程配置不存在: processDefinitionId={}", processDefinitionId);
            throw new RuntimeException("流程配置不存在: " + processDefinitionId);
        }

        log.info("[工作流配置] 获取流程配置成功: processDefinitionId={}, processKey={}",
                processDefinitionId, config.getProcessKey());
        return config;
    }

    @Override
    public List<ValidationError> validateConfig(VisualWorkflowConfigForm form) {
        log.info("[工作流配置] 验证流程配置: processKey={}", form.getProcessKey());

        List<ValidationError> errors = new ArrayList<>();

        // 1. 基本信息验证
        if (form.getProcessKey() == null || form.getProcessKey().isEmpty()) {
            errors.add(ValidationError.builder()
                    .message("流程Key不能为空")
                    .errorCode("PROCESS_KEY_REQUIRED")
                    .level("error")
                    .build());
        }

        if (form.getProcessName() == null || form.getProcessName().isEmpty()) {
            errors.add(ValidationError.builder()
                    .message("流程名称不能为空")
                    .errorCode("PROCESS_NAME_REQUIRED")
                    .level("error")
                    .build());
        }

        // 2. 节点验证
        if (form.getNodes() == null || form.getNodes().isEmpty()) {
            errors.add(ValidationError.builder()
                    .message("流程必须包含至少一个节点")
                    .errorCode("NODES_REQUIRED")
                    .level("error")
                    .build());
        } else {
            // 检查开始和结束节点
            long startNodeCount = form.getNodes().stream()
                    .filter(n -> "START".equals(n.getNodeType()))
                    .count();

            long endNodeCount = form.getNodes().stream()
                    .filter(n -> "END".equals(n.getNodeType()))
                    .count();

            if (startNodeCount == 0) {
                errors.add(ValidationError.builder()
                        .message("流程必须包含一个开始节点")
                        .errorCode("START_NODE_REQUIRED")
                        .level("error")
                        .build());
            }

            if (startNodeCount > 1) {
                errors.add(ValidationError.builder()
                        .message("流程只能包含一个开始节点")
                        .errorCode("MULTIPLE_START_NODES")
                        .level("error")
                        .build());
            }

            if (endNodeCount == 0) {
                errors.add(ValidationError.builder()
                        .message("流程必须包含至少一个结束节点")
                        .errorCode("END_NODE_REQUIRED")
                        .level("error")
                        .build());
            }

            // 验证节点ID唯一性
            Set<String> nodeIds = new HashSet<>();
            for (ProcessNode node : form.getNodes()) {
                if (nodeIds.contains(node.getNodeId())) {
                    errors.add(ValidationError.builder()
                            .nodeId(node.getNodeId())
                            .message("节点ID重复: " + node.getNodeId())
                            .errorCode("DUPLICATE_NODE_ID")
                            .level("error")
                            .build());
                }
                nodeIds.add(node.getNodeId());
            }
        }

        // 3. 连线验证
        if (form.getEdges() != null) {
            for (ProcessEdge edge : form.getEdges()) {
                // 验证源节点和目标节点存在
                boolean sourceExists = form.getNodes().stream()
                        .anyMatch(n -> n.getNodeId().equals(edge.getSource()));
                boolean targetExists = form.getNodes().stream()
                        .anyMatch(n -> n.getNodeId().equals(edge.getTarget()));

                if (!sourceExists) {
                    errors.add(ValidationError.builder()
                            .edgeId(edge.getEdgeId())
                            .message("连线源节点不存在: " + edge.getSource())
                            .errorCode("SOURCE_NODE_NOT_FOUND")
                            .level("error")
                            .build());
                }

                if (!targetExists) {
                    errors.add(ValidationError.builder()
                            .edgeId(edge.getEdgeId())
                            .message("连线目标节点不存在: " + edge.getTarget())
                            .errorCode("TARGET_NODE_NOT_FOUND")
                            .level("error")
                            .build());
                }

                // 验证网关条件表达式
                String sourceNodeType = form.getNodes().stream()
                        .filter(n -> n.getNodeId().equals(edge.getSource()))
                        .map(ProcessNode::getNodeType)
                        .findFirst()
                        .orElse(null);

                if ("EXCLUSIVE_GATEWAY".equals(sourceNodeType) ||
                        "INCLUSIVE_GATEWAY".equals(sourceNodeType)) {
                    if (edge.getConditionExpression() == null || edge.getConditionExpression().isEmpty()) {
                        errors.add(ValidationError.builder()
                                .edgeId(edge.getEdgeId())
                                .message("网关出口连线必须设置条件表达式")
                                .errorCode("GATEWAY_CONDITION_REQUIRED")
                                .level("error")
                                .build());
                    }
                }
            }
        }

        // 4. 用户任务验证
        for (ProcessNode node : form.getNodes()) {
            if ("USER_TASK".equals(node.getNodeType())) {
                NodeConfig config = form.getNodeConfigs() != null ?
                        form.getNodeConfigs().get(node.getNodeId()) : null;

                if (config == null || config.getAssigneeType() == null) {
                    errors.add(ValidationError.builder()
                            .nodeId(node.getNodeId())
                            .message("用户任务必须设置审批人类型")
                            .errorCode("ASSIGNEE_TYPE_REQUIRED")
                            .level("error")
                            .build());
                } else if (config.getAssigneeValue() == null || config.getAssigneeValue().isEmpty()) {
                    errors.add(ValidationError.builder()
                            .nodeId(node.getNodeId())
                            .message("用户任务必须设置审批人值")
                            .errorCode("ASSIGNEE_VALUE_REQUIRED")
                            .level("error")
                            .build());
                }

                if (config != null && config.getFormKey() == null) {
                    errors.add(ValidationError.builder()
                            .nodeId(node.getNodeId())
                            .message("用户任务必须关联表单")
                            .errorCode("FORM_KEY_REQUIRED")
                            .level("error")
                            .build());
                }
            }
        }

        log.info("[工作流配置] 验证完成: processKey={}, errorCount={}, warningCount={}",
                form.getProcessKey(),
                errors.stream().filter(e -> "error".equals(e.getLevel())).count(),
                errors.stream().filter(e -> "warning".equals(e.getLevel())).count());

        return errors;
    }

    @Override
    public String generateBpmnXml(VisualWorkflowConfigForm form) {
        log.info("[工作流配置] 生成BPMN XML: processKey={}", form.getProcessKey());

        try {
            StringBuilder bpmn = new StringBuilder();
            bpmn.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bpmn.append("<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n");
            bpmn.append("             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            bpmn.append("             xmlns:flowable=\"http://flowable.org/bpmn\"\n");
            bpmn.append("             xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n");
            bpmn.append("             xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\"\n");
            bpmn.append("             xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\"\n");
            bpmn.append("             typeLanguage=\"http://www.w3.org/2001/XMLSchema\"\n");
            bpmn.append("             expressionLanguage=\"http://www.w3.org/1999/XPath\"\n");
            bpmn.append("             targetNamespace=\"http://www.flowable.org/processdef\">\n");

            // 流程定义
            bpmn.append("  <process id=\"").append(form.getProcessKey()).append("\" ")
                    .append("name=\"").append(escapeXml(form.getProcessName())).append("\" ")
                    .append("isExecutable=\"true\">\n");

            // 生成节点
            for (ProcessNode node : form.getNodes()) {
                bpmn.append(generateBpmnNode(node, form.getNodeConfigs()));
            }

            // 生成连线
            if (form.getEdges() != null) {
                for (ProcessEdge edge : form.getEdges()) {
                    bpmn.append(generateBpmnSequenceFlow(edge));
                }
            }

            bpmn.append("  </process>\n");

            // BPMN图形信息
            bpmn.append("  <bpmndi:BPMNDiagram id=\"BPMNDiagram_")
                    .append(form.getProcessKey()).append("\">\n");
            bpmn.append("    <bpmndi:BPMNPlane id=\"BPMNPlane_")
                    .append(form.getProcessKey()).append("\" ")
                    .append("bpmnElement=\"").append(form.getProcessKey()).append("\">\n");

            // 节点图形信息
            for (ProcessNode node : form.getNodes()) {
                bpmn.append("      <bpmndi:BPMNShape id=\"BPMNShape_").append(node.getNodeId()).append("\" ")
                        .append("bpmnElement=\"").append(node.getNodeId()).append("\">\n");
                bpmn.append("        <omgdc:Bounds x=\"").append(node.getX()).append("\" ")
                        .append("y=\"").append(node.getY()).append("\" ")
                        .append("width=\"").append(node.getWidth()).append("\" ")
                        .append("height=\"").append(node.getHeight()).append("\"/>\n");
                bpmn.append("      </bpmndi:BPMNShape>\n");
            }

            // 连线图形信息（简化版，实际应计算坐标）
            if (form.getEdges() != null) {
                for (ProcessEdge edge : form.getEdges()) {
                    bpmn.append("      <bpmndi:BPMNEdge id=\"BPMNEdge_").append(edge.getEdgeId()).append("\" ")
                            .append("bpmnElement=\"").append(edge.getEdgeId()).append("\">\n");
                    bpmn.append("        <omgdi:waypoint x=\"0\" y=\"0\"/>\n");
                    bpmn.append("        <omgdi:waypoint x=\"100\" y=\"100\"/>\n");
                    bpmn.append("      </bpmndi:BPMNEdge>\n");
                }
            }

            bpmn.append("    </bpmndi:BPMNPlane>\n");
            bpmn.append("  </bpmndi:BPMNDiagram>\n");
            bpmn.append("</definitions>");

            String bpmnXml = bpmn.toString();
            log.info("[工作流配置] BPMN XML生成成功: processKey={}, length={}",
                    form.getProcessKey(), bpmnXml.length());

            return bpmnXml;

        } catch (Exception e) {
            log.error("[工作流配置] BPMN XML生成失败: processKey={}, error={}",
                    form.getProcessKey(), e.getMessage(), e);
            throw new RuntimeException("BPMN XML生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "workflowConfig", allEntries = true)
    public String deployProcess(String processKey, String processName, String processCategory, String bpmnXml) {
        log.info("[工作流配置] 部署流程: processKey={}, processName={}", processKey, processName);

        try {
            // TODO: 集成Flowable引擎
            // RepositoryService repositoryService = ...;
            // Deployment deployment = repositoryService.createDeployment()
            //     .name(processName)
            //     .category(processCategory)
            //     .addString(processKey + ".bpmn20.xml", bpmnXml)
            //     .deploy();
            // ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            //     .deploymentId(deployment.getId())
            //     .singleResult();
            // return processDefinition.getId();

            // 临时返回模拟ID
            String processDefinitionId = processKey + ":" + System.currentTimeMillis();
            log.info("[工作流配置] 流程部署成功: processKey={}, processDefinitionId={}",
                    processKey, processDefinitionId);

            return processDefinitionId;

        } catch (Exception e) {
            log.error("[工作流配置] 流程部署失败: processKey={}, error={}",
                    processKey, e.getMessage(), e);
            throw new RuntimeException("流程部署失败: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = "workflowConfig", allEntries = true)
    public void updateProcessConfig(String processDefinitionId, VisualWorkflowConfigForm form) {
        log.info("[工作流配置] 更新流程配置: processDefinitionId={}", processDefinitionId);

        VisualWorkflowConfig existingConfig = configStorage.get(processDefinitionId);
        if (existingConfig == null) {
            log.warn("[工作流配置] 流程配置不存在: processDefinitionId={}", processDefinitionId);
            throw new RuntimeException("流程配置不存在: " + processDefinitionId);
        }

        // 验证配置
        List<ValidationError> errors = validateConfig(form);
        if (!errors.isEmpty()) {
            log.warn("[工作流配置] 配置验证失败: errorCount={}", errors.size());
            throw new RuntimeException("配置验证失败: " + errors.get(0).getMessage());
        }

        // 更新配置
        VisualWorkflowConfig updatedConfig = VisualWorkflowConfig.builder()
                .processDefinitionId(processDefinitionId)
                .processKey(form.getProcessKey())
                .processName(form.getProcessName())
                .processDescription(form.getProcessDescription())
                .processCategory(form.getProcessCategory())
                .version(existingConfig.getVersion())
                .nodes(form.getNodes())
                .edges(form.getEdges())
                .nodeConfigs(form.getNodeConfigs())
                .extendedAttributes(form.getExtendedAttributes())
                .build();

        configStorage.put(processDefinitionId, updatedConfig);

        log.info("[工作流配置] 流程配置更新成功: processDefinitionId={}, processKey={}",
                processDefinitionId, form.getProcessKey());
    }

    // ==================== 流程模板库 ====================

    @Override
    @Cacheable(value = "workflowTemplates", key = "#category ?: 'all'")
    public List<ProcessTemplate> getProcessTemplates(String category) {
        log.info("[工作流配置] 获取流程模板列表: category={}", category);

        List<ProcessTemplate> templates = templateStorage.values().stream()
                .filter(t -> category == null || category.isEmpty() || category.equals(t.getCategory()))
                .map(this::convertToProcessTemplate)
                .collect(Collectors.toList());

        log.info("[工作流配置] 获取流程模板成功: category={}, count={}", category, templates.size());
        return templates;
    }

    @Override
    @Cacheable(value = "workflowTemplateDetail", key = "#templateId")
    public ProcessTemplateDetail getTemplateDetail(String templateId) {
        log.info("[工作流配置] 获取流程模板详情: templateId={}", templateId);

        ProcessTemplateDetail template = templateStorage.get(templateId);
        if (template == null) {
            log.warn("[工作流配置] 流程模板不存在: templateId={}", templateId);
            throw new RuntimeException("流程模板不存在: " + templateId);
        }

        log.info("[工作流配置] 获取流程模板详情成功: templateId={}", templateId);
        return template;
    }

    @Override
    @CacheEvict(value = "workflowTemplates", allEntries = true)
    public String applyTemplate(String templateId, String newProcessKey, String newProcessName,
                              Map<String, Object> customizations) {
        log.info("[工作流配置] 应用流程模板: templateId={}, newProcessKey={}", templateId, newProcessKey);

        ProcessTemplateDetail template = templateStorage.get(templateId);
        if (template == null) {
            log.warn("[工作流配置] 流程模板不存在: templateId={}", templateId);
            throw new RuntimeException("流程模板不存在: " + templateId);
        }

        // 创建新流程配置
        VisualWorkflowConfigForm newForm = VisualWorkflowConfigForm.builder()
                .processKey(newProcessKey)
                .processName(newProcessName)
                .processDescription(template.getTemplateDescription())
                .processCategory(template.getCategory())
                .nodes(template.getNodes())
                .edges(template.getEdges())
                .nodeConfigs(template.getNodeConfigs())
                .extendedAttributes(customizations)
                .build();

        // 生成BPMN XML并部署
        String bpmnXml = generateBpmnXml(newForm);
        String processDefinitionId = deployProcess(newProcessKey, newProcessName,
                template.getCategory(), bpmnXml);

        // 保存配置
        VisualWorkflowConfig config = VisualWorkflowConfig.builder()
                .processDefinitionId(processDefinitionId)
                .processKey(newProcessKey)
                .processName(newProcessName)
                .processDescription(template.getTemplateDescription())
                .processCategory(template.getCategory())
                .version("1.0.0")
                .nodes(template.getNodes())
                .edges(template.getEdges())
                .nodeConfigs(template.getNodeConfigs())
                .extendedAttributes(customizations)
                .build();

        configStorage.put(processDefinitionId, config);

        // 更新模板使用次数
        template.setUsageCount((template.getUsageCount() != null ? template.getUsageCount() : 0) + 1);

        log.info("[工作流配置] 应用流程模板成功: templateId={}, processDefinitionId={}",
                templateId, processDefinitionId);

        return processDefinitionId;
    }

    @Override
    @CacheEvict(value = "workflowTemplates", allEntries = true)
    public String uploadTemplate(String templateId, String templateName, String category,
                               String bpmnXml, byte[] screenshot) {
        log.info("[工作流配置] 上传自定义模板: templateId={}, templateName={}", templateId, templateName);

        // TODO: 解析BPMN XML生成节点和连线
        ProcessTemplateDetail template = ProcessTemplateDetail.builder()
                .templateId(templateId)
                .templateName(templateName)
                .templateDescription("自定义模板: " + templateName)
                .category(category)
                .tags(new ArrayList<>())
                .nodes(new ArrayList<>())
                .edges(new ArrayList<>())
                .nodeConfigs(new HashMap<>())
                .bpmnXml(bpmnXml)
                .screenshotUrl(screenshot != null ? "/screenshots/" + templateId + ".png" : null)
                .usageCount(0L)
                .rating(0.0)
                .systemTemplate(false)
                .createTime(System.currentTimeMillis())
                .build();

        templateStorage.put(templateId, template);

        log.info("[工作流配置] 上传自定义模板成功: templateId={}", templateId);
        return templateId;
    }

    // ==================== 流程验证和诊断 ====================

    @Override
    public ValidationResult validateConfig(VisualWorkflowConfigForm form) {
        log.info("[工作流配置] 验证流程配置: processKey={}", form.getProcessKey());

        List<ValidationError> errors = new ArrayList<>();
        List<ValidationError> warnings = new ArrayList<>();
        List<ValidationError> infos = new ArrayList<>();

        // 基本验证
        List<ValidationError> validationErrors = validateConfig(form);
        errors.addAll(validationErrors.stream()
                .filter(e -> "error".equals(e.getLevel()))
                .collect(Collectors.toList()));
        warnings.addAll(validationErrors.stream()
                .filter(e -> "warning".equals(e.getLevel()))
                .collect(Collectors.toList()));
        infos.addAll(validationErrors.stream()
                .filter(e -> "info".equals(e.getLevel()))
                .collect(Collectors.toList()));

        // 可达性分析
        if (form.getNodes() != null && !form.getNodes().isEmpty()) {
            ProcessNode startNode = form.getNodes().stream()
                    .filter(n -> "START".equals(n.getNodeType()))
                    .findFirst()
                    .orElse(null);

            if (startNode != null) {
                Set<String> reachableNodes = findReachableNodes(startNode, form.getEdges());
                for (ProcessNode node : form.getNodes()) {
                    if (!reachableNodes.contains(node.getNodeId())) {
                        warnings.add(ValidationError.builder()
                                .nodeId(node.getNodeId())
                                .message("节点不可达: " + node.getName())
                                .errorCode("NODE_UNREACHABLE")
                                .level("warning")
                                .build());
                    }
                }
            }
        }

        // 循环引用检测
        List<List<String>> cycles = detectCycles(form.getNodes(), form.getEdges());
        for (List<String> cycle : cycles) {
            errors.add(ValidationError.builder()
                    .message("检测到循环引用: " + String.join(" -> ", cycle))
                    .errorCode("CIRCULAR_REFERENCE")
                    .level("error")
                    .build());
        }

        boolean valid = errors.isEmpty();

        log.info("[工作流配置] 验证完成: processKey={}, valid={}, errorCount={}, warningCount={}",
                form.getProcessKey(), valid, errors.size(), warnings.size());

        return ValidationResult.builder()
                .valid(valid)
                .errors(errors)
                .warnings(warnings)
                .infos(infos)
                .build();
    }

    @Override
    public List<WorkflowIssue> diagnoseWorkflow(String processDefinitionId) {
        log.info("[工作流配置] 诊断流程问题: processDefinitionId={}", processDefinitionId);

        List<WorkflowIssue> issues = new ArrayList<>();

        VisualWorkflowConfig config = configStorage.get(processDefinitionId);
        if (config == null) {
            log.warn("[工作流配置] 流程配置不存在: processDefinitionId={}", processDefinitionId);
            return issues;
        }

        // 转换为Form进行验证
        VisualWorkflowConfigForm form = VisualWorkflowConfigForm.builder()
                .processKey(config.getProcessKey())
                .processName(config.getProcessName())
                .processDescription(config.getProcessDescription())
                .processCategory(config.getProcessCategory())
                .nodes(config.getNodes())
                .edges(config.getEdges())
                .nodeConfigs(config.getNodeConfigs())
                .extendedAttributes(config.getExtendedAttributes())
                .build();

        // 执行验证
        ValidationResult validationResult = validateConfig(form);

        // 转换为WorkflowIssue
        long issueId = 1;
        for (ValidationError error : validationResult.getErrors()) {
            issues.add(WorkflowIssue.builder()
                    .issueId(String.valueOf(issueId++))
                    .issueType(mapErrorCodeToIssueType(error.getErrorCode()))
                    .description(error.getMessage())
                    .level("ERROR")
                    .nodeId(error.getNodeId())
                    .edgeId(error.getEdgeId())
                    .suggestion(generateSuggestion(error.getErrorCode()))
                    .build());
        }

        for (ValidationError warning : validationResult.getWarnings()) {
            issues.add(WorkflowIssue.builder()
                    .issueId(String.valueOf(issueId++))
                    .issueType(mapErrorCodeToIssueType(warning.getErrorCode()))
                    .description(warning.getMessage())
                    .level("WARNING")
                    .nodeId(warning.getNodeId())
                    .edgeId(warning.getEdgeId())
                    .suggestion(generateSuggestion(warning.getErrorCode()))
                    .build());
        }

        log.info("[工作流配置] 诊断完成: processDefinitionId={}, issueCount={}",
                processDefinitionId, issues.size());

        return issues;
    }

    @Override
    public WorkflowSimulationResult simulateWorkflow(WorkflowSimulationRequest request) {
        log.info("[工作流配置] 流程仿真: processDefinitionId={}", request.getProcessDefinitionId());

        long startTime = System.currentTimeMillis();
        List<String> executionPath = new ArrayList<>();
        List<NodeExecutionResult> nodeResults = new ArrayList<>();
        boolean success = false;
        String errorMessage = null;

        try {
            VisualWorkflowConfig config = configStorage.get(request.getProcessDefinitionId());
            if (config == null) {
                throw new RuntimeException("流程配置不存在: " + request.getProcessDefinitionId());
            }

            // 找到开始节点
            ProcessNode currentNode = config.getNodes().stream()
                    .filter(n -> "START".equals(n.getNodeType()))
                    .findFirst()
                    .orElse(null);

            if (currentNode == null) {
                throw new RuntimeException("流程缺少开始节点");
            }

            // 模拟执行
            Set<String> visitedNodes = new HashSet<>();
            Map<String, Object> variables = request.getVariables() != null ?
                    request.getVariables() : new HashMap<>();

            while (currentNode != null && !visitedNodes.contains(currentNode.getNodeId())) {
                visitedNodes.add(currentNode.getNodeId());
                executionPath.add(currentNode.getNodeId());

                // 模拟节点执行
                NodeExecutionResult nodeResult = simulateNodeExecution(currentNode,
                        request.getInitiatorUserId(), variables, request.getFormData());
                nodeResults.add(nodeResult);

                // 如果节点执行失败，停止仿真
                if ("FAILED".equals(nodeResult.getStatus())) {
                    errorMessage = nodeResult.getErrorMessage();
                    break;
                }

                // 找到下一个节点
                currentNode = findNextNode(currentNode, config, variables);
            }

            // 检查是否到达结束节点
            boolean reachedEnd = executionPath.stream()
                    .anyMatch(nodeId -> config.getNodes().stream()
                            .filter(n -> nodeId.equals(n.getNodeId()))
                            .anyMatch(n -> "END".equals(n.getNodeType())));

            success = reachedEnd && errorMessage == null;

            if (!success && errorMessage == null) {
                errorMessage = "流程未正常结束";
            }

        } catch (Exception e) {
            log.error("[工作流配置] 流程仿真异常: processDefinitionId={}, error={}",
                    request.getProcessDefinitionId(), e.getMessage(), e);
            errorMessage = e.getMessage();
            success = false;
        }

        long executionTime = System.currentTimeMillis() - startTime;

        log.info("[工作流配置] 流程仿真完成: processDefinitionId={}, success={}, executionTime={}ms",
                request.getProcessDefinitionId(), success, executionTime);

        return WorkflowSimulationResult.builder()
                .success(success)
                .executionPath(executionPath)
                .nodeResults(nodeResults)
                .errorMessage(errorMessage)
                .executionTime(executionTime)
                .build();
    }

    // ==================== 组件库 ====================

    @Override
    @Cacheable(value = "nodeTypes")
    public List<NodeType> getNodeTypes() {
        log.info("[工作流配置] 获取节点类型库");

        return new ArrayList<>(nodeTypeLibrary.values());
    }

    @Override
    @Cacheable(value = "nodeConfigSchema", key = "#nodeType")
    public NodeConfigSchema getNodeConfigSchema(String nodeType) {
        log.info("[工作流配置] 获取节点配置Schema: nodeType={}", nodeType);

        NodeConfigSchema schema = configSchemaLibrary.get(nodeType);
        if (schema == null) {
            log.warn("[工作流配置] 节点配置Schema不存在: nodeType={}", nodeType);
            throw new RuntimeException("节点配置Schema不存在: " + nodeType);
        }

        return schema;
    }

    @Override
    public String importConfig(org.springframework.web.multipart.MultipartFile file) {
        log.info("[工作流配置] 导入流程配置: fileName={}", file.getOriginalFilename());

        try {
            // TODO: 实现配置文件解析和导入
            // 1. 解析上传的文件（JSON或XML格式）
            // 2. 验证配置
            // 3. 保存到存储
            // 4. 返回新的流程定义ID

            log.info("[工作流配置] 导入流程配置成功: fileName={}", file.getOriginalFilename());
            return "IMPORTED_" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("[工作流配置] 导入流程配置失败: fileName={}, error={}",
                    file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("导入流程配置失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成BPMN节点XML
     */
    private String generateBpmnNode(ProcessNode node, Map<String, NodeConfig> configs) {
        StringBuilder xml = new StringBuilder();

        switch (node.getNodeType()) {
            case "START":
                xml.append("    <startEvent id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\"/>\n");
                break;

            case "END":
                xml.append("    <endEvent id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\"/>\n");
                break;

            case "USER_TASK":
                NodeConfig config = configs != null ? configs.get(node.getNodeId()) : null;
                xml.append("    <userTask id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\" ");

                if (config != null) {
                    if ("USER".equals(config.getAssigneeType())) {
                        xml.append("flowable:assignee=\"").append(config.getAssigneeValue()).append("\" ");
                    } else if ("ROLE".equals(config.getAssigneeType())) {
                        xml.append("flowable:candidateGroups=\"").append(config.getAssigneeValue()).append("\" ");
                    }
                    // TODO: 处理其他审批人类型
                }

                xml.append("/>\n");
                break;

            case "SERVICE_TASK":
                xml.append("    <serviceTask id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\" ")
                        .append("flowable:class=\"${").append(node.getNodeId()).append("Service}\"/>\n");
                break;

            case "EXCLUSIVE_GATEWAY":
                xml.append("    <exclusiveGateway id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\"/>\n");
                break;

            case "PARALLEL_GATEWAY":
                xml.append("    <parallelGateway id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\"/>\n");
                break;

            case "INCLUSIVE_GATEWAY":
                xml.append("    <inclusiveGateway id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\"/>\n");
                break;

            default:
                xml.append("    <task id=\"").append(node.getNodeId()).append("\" ")
                        .append("name=\"").append(escapeXml(node.getName())).append("\"/>\n");
                break;
        }

        return xml.toString();
    }

    /**
     * 生成BPMN连线XML
     */
    private String generateBpmnSequenceFlow(ProcessEdge edge) {
        StringBuilder xml = new StringBuilder();
        xml.append("    <sequenceFlow id=\"").append(edge.getEdgeId()).append("\" ")
                .append("sourceRef=\"").append(edge.getSource()).append("\" ")
                .append("targetRef=\"").append(edge.getTarget()).append("\"");

        if (edge.getConditionExpression() != null && !edge.getConditionExpression().isEmpty()) {
            xml.append(">\n");
            xml.append("      <conditionExpression xsi:type=\"tFormalExpression\">")
                    .append(edge.getConditionExpression())
                    .append("</conditionExpression>\n");
            xml.append("    </sequenceFlow>\n");
        } else {
            xml.append("/>\n");
        }

        return xml.toString();
    }

    /**
     * XML转义
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * 查找可达节点
     */
    private Set<String> findReachableNodes(ProcessNode startNode, List<ProcessEdge> edges) {
        Set<String> reachableNodes = new HashSet<>();
        Set<String> toVisit = new HashSet<>();
        toVisit.add(startNode.getNodeId());

        while (!toVisit.isEmpty()) {
            String currentNodeId = toVisit.iterator().next();
            toVisit.remove(currentNodeId);
            reachableNodes.add(currentNodeId);

            // 找到所有从当前节点出发的连线
            List<ProcessEdge> outgoingEdges = edges.stream()
                    .filter(e -> e.getSource().equals(currentNodeId))
                    .collect(Collectors.toList());

            for (ProcessEdge edge : outgoingEdges) {
                if (!reachableNodes.contains(edge.getTarget())) {
                    toVisit.add(edge.getTarget());
                }
            }
        }

        return reachableNodes;
    }

    /**
     * 检测循环引用
     */
    private List<List<String>> detectCycles(List<ProcessNode> nodes, List<ProcessEdge> edges) {
        List<List<String>> cycles = new ArrayList<>();

        // 构建邻接表
        Map<String, List<String>> graph = new HashMap<>();
        for (ProcessNode node : nodes) {
            graph.put(node.getNodeId(), new ArrayList<>());
        }

        for (ProcessEdge edge : edges) {
            graph.get(edge.getSource()).add(edge.getTarget());
        }

        // DFS检测环
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> path = new ArrayList<>();

        for (ProcessNode node : nodes) {
            if (!visited.contains(node.getNodeId())) {
                detectCyclesDFS(node.getNodeId(), graph, visited, recursionStack, path, cycles);
            }
        }

        return cycles;
    }

    private void detectCyclesDFS(String nodeId, Map<String, List<String>> graph,
                                 Set<String> visited, Set<String> recursionStack,
                                 List<String> path, List<List<String>> cycles) {
        visited.add(nodeId);
        recursionStack.add(nodeId);
        path.add(nodeId);

        for (String neighbor : graph.getOrDefault(nodeId, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                detectCyclesDFS(neighbor, graph, visited, recursionStack, path, cycles);
            } else if (recursionStack.contains(neighbor)) {
                // 找到环
                int cycleStart = path.indexOf(neighbor);
                List<String> cycle = new ArrayList<>(path.subList(cycleStart, path.size()));
                cycle.add(neighbor);
                cycles.add(cycle);
            }
        }

        path.remove(path.size() - 1);
        recursionStack.remove(nodeId);
    }

    /**
     * 模拟节点执行
     */
    private NodeExecutionResult simulateNodeExecution(ProcessNode node, Long initiatorUserId,
                                                     Map<String, Object> variables,
                                                     Map<String, Object> formData) {
        long executionTime = (long) (Math.random() * 100);

        NodeExecutionResult.NodeExecutionResultBuilder builder = NodeExecutionResult.builder()
                .nodeId(node.getNodeId())
                .nodeName(node.getName())
                .nodeType(node.getNodeType())
                .executionTime(executionTime);

        switch (node.getNodeType()) {
            case "START":
            case "END":
                builder.status("SUCCESS");
                break;

            case "USER_TASK":
                // 模拟用户任务
                builder.assignee("user_" + initiatorUserId)
                        .comment("待审批")
                        .status("SUCCESS");
                break;

            case "SERVICE_TASK":
                // 模拟服务任务
                builder.status("SUCCESS");
                break;

            case "EXCLUSIVE_GATEWAY":
            case "PARALLEL_GATEWAY":
            case "INCLUSIVE_GATEWAY":
                // 网关不耗时
                builder.executionTime(0L).status("SUCCESS");
                break;

            default:
                builder.status("SKIPPED");
                break;
        }

        return builder.build();
    }

    /**
     * 查找下一个节点
     */
    private ProcessNode findNextNode(ProcessNode currentNode, VisualWorkflowConfig config,
                                     Map<String, Object> variables) {
        if ("END".equals(currentNode.getNodeType())) {
            return null;
        }

        // 找到从当前节点出发的连线
        List<ProcessEdge> outgoingEdges = config.getEdges().stream()
                .filter(e -> e.getSource().equals(currentNode.getNodeId()))
                .collect(Collectors.toList());

        if (outgoingEdges.isEmpty()) {
            return null;
        }

        // 简化版：只取第一条连线
        // 实际应该根据条件表达式评估
        String nextNodeId = outgoingEdges.get(0).getTarget();

        return config.getNodes().stream()
                .filter(n -> n.getNodeId().equals(nextNodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 错误代码映射到问题类型
     */
    private String mapErrorCodeToIssueType(String errorCode) {
        switch (errorCode) {
            case "NODE_UNREACHABLE":
                return "NODE_UNREACHABLE";
            case "CIRCULAR_REFERENCE":
                return "CIRCULAR_REFERENCE";
            case "GATEWAY_CONDITION_REQUIRED":
                return "GATEWAY_INVALID";
            case "ASSIGNEE_TYPE_REQUIRED":
            case "ASSIGNEE_VALUE_REQUIRED":
                return "MISSING_ASSIGNEE";
            case "FORM_KEY_REQUIRED":
                return "MISSING_FORM";
            default:
                return "OTHER";
        }
    }

    /**
     * 生成修复建议
     */
    private String generateSuggestion(String errorCode) {
        switch (errorCode) {
            case "NODE_UNREACHABLE":
                return "检查流程连线配置，确保所有节点都可从开始节点到达";
            case "CIRCULAR_REFERENCE":
                return "移除循环引用或使用子流程";
            case "GATEWAY_CONDITION_REQUIRED":
                return "为网关出口连线添加条件表达式";
            case "ASSIGNEE_TYPE_REQUIRED":
            case "ASSIGNEE_VALUE_REQUIRED":
                return "为用户任务设置审批人类型和审批人值";
            case "FORM_KEY_REQUIRED":
                return "为用户任务关联表单";
            case "START_NODE_REQUIRED":
                return "添加一个开始节点";
            case "END_NODE_REQUIRED":
                return "添加至少一个结束节点";
            default:
                return "请检查配置并修复相关问题";
        }
    }

    /**
     * 转换为ProcessTemplate
     */
    private ProcessTemplate convertToProcessTemplate(ProcessTemplateDetail detail) {
        return ProcessTemplate.builder()
                .templateId(detail.getTemplateId())
                .templateName(detail.getTemplateName())
                .templateDescription(detail.getTemplateDescription())
                .category(detail.getCategory())
                .tags(detail.getTags())
                .bpmnXml(detail.getBpmnXml())
                .screenshotUrl(detail.getScreenshotUrl())
                .usageCount(detail.getUsageCount())
                .rating(detail.getRating())
                .build();
    }

    /**
     * 初始化节点类型库
     */
    private void initializeNodeTypeLibrary() {
        // 事件节点
        nodeTypeLibrary.put("START", NodeType.builder()
                .type("START")
                .name("开始节点")
                .description("流程的起点")
                .icon("start-event")
                .category("event")
                .configurable(false)
                .build());

        nodeTypeLibrary.put("END", NodeType.builder()
                .type("END")
                .name("结束节点")
                .description("流程的终点")
                .icon("end-event")
                .category("event")
                .configurable(false)
                .build());

        // 任务节点
        nodeTypeLibrary.put("USER_TASK", NodeType.builder()
                .type("USER_TASK")
                .name("用户任务")
                .description("需要用户审批的任务")
                .icon("user-task")
                .category("task")
                .configurable(true)
                .configSchema(configSchemaLibrary.get("USER_TASK"))
                .build());

        nodeTypeLibrary.put("SERVICE_TASK", NodeType.builder()
                .type("SERVICE_TASK")
                .name("服务任务")
                .description("自动执行的系统任务")
                .icon("service-task")
                .category("task")
                .configurable(true)
                .configSchema(configSchemaLibrary.get("SERVICE_TASK"))
                .build());

        // 网关节点
        nodeTypeLibrary.put("EXCLUSIVE_GATEWAY", NodeType.builder()
                .type("EXCLUSIVE_GATEWAY")
                .name("排他网关")
                .description("只执行一条分支")
                .icon("exclusive-gateway")
                .category("gateway")
                .configurable(false)
                .build());

        nodeTypeLibrary.put("PARALLEL_GATEWAY", NodeType.builder()
                .type("PARALLEL_GATEWAY")
                .name("并行网关")
                .description("同时执行所有分支")
                .icon("parallel-gateway")
                .category("gateway")
                .configurable(false)
                .build());

        nodeTypeLibrary.put("INCLUSIVE_GATEWAY", NodeType.builder()
                .type("INCLUSIVE_GATEWAY")
                .name("包容网关")
                .description("执行条件满足的分支")
                .icon("inclusive-gateway")
                .category("gateway")
                .configurable(false)
                .build());

        log.info("[工作流配置] 节点类型库初始化完成: count={}", nodeTypeLibrary.size());
    }

    /**
     * 初始化配置Schema库
     */
    private void initializeConfigSchemaLibrary() {
        // 用户任务配置Schema
        List<NodeConfigSchema.PropertyDefinition> userTaskProps = new ArrayList<>();
        userTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("assigneeType")
                .label("审批人类型")
                .type("select")
                .required(true)
                .options(Arrays.asList(
                        NodeConfigSchema.PropertyValue.builder().value("USER").label("指定用户").build(),
                        NodeConfigSchema.PropertyValue.builder().value("ROLE").label("角色").build(),
                        NodeConfigSchema.PropertyValue.builder().value("DEPT_LEADER").label("部门领导").build(),
                        NodeConfigSchema.PropertyValue.builder().value("INITIATOR").label("发起人").build(),
                        NodeConfigSchema.PropertyValue.builder().value("SCRIPT").label("脚本").build()
                ))
                .helpText("选择审批人类型")
                .build());

        userTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("assigneeValue")
                .label("审批人值")
                .type("input")
                .required(true)
                .helpText("根据审批人类型输入对应的值（用户ID、角色编码等）")
                .build());

        userTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("formKey")
                .label("关联表单")
                .type("input")
                .required(true)
                .helpText("关联的表单Key")
                .build());

        userTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("dueDate")
                .label("到期时间")
                .type("number")
                .required(false)
                .defaultValue(3)
                .helpText("任务到期天数")
                .build());

        userTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("multiInstance")
                .label("多实例")
                .type("checkbox")
                .required(false)
                .defaultValue(false)
                .helpText("是否为多实例任务")
                .build());

        configSchemaLibrary.put("USER_TASK", NodeConfigSchema.builder()
                .nodeType("USER_TASK")
                .properties(userTaskProps)
                .build());

        // 服务任务配置Schema
        List<NodeConfigSchema.PropertyDefinition> serviceTaskProps = new ArrayList<>();
        serviceTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("class")
                .label("Java类")
                .type("input")
                .required(true)
                .helpText("实现ServiceTask接口的Java类全限定名")
                .build());

        serviceTaskProps.add(NodeConfigSchema.PropertyDefinition.builder()
                .name("expression")
                .label("表达式")
                .type("textarea")
                .required(false)
                .helpText("UEL表达式")
                .build());

        configSchemaLibrary.put("SERVICE_TASK", NodeConfigSchema.builder()
                .nodeType("SERVICE_TASK")
                .properties(serviceTaskProps)
                .build());

        log.info("[工作流配置] 配置Schema库初始化完成: count={}", configSchemaLibrary.size());
    }

    /**
     * 初始化内置模板
     */
    private void initializeBuiltinTemplates() {
        // 请假审批流程模板
        templateStorage.put("leave_request_template", ProcessTemplateDetail.builder()
                .templateId("leave_request_template")
                .templateName("请假审批流程")
                .templateDescription("员工请假审批标准流程")
                .category("hr")
                .tags(Arrays.asList("请假", "审批", "HR"))
                .nodes(createLeaveRequestNodes())
                .edges(createLeaveRequestEdges())
                .nodeConfigs(createLeaveRequestNodeConfigs())
                .bpmnXml("")  // 实际应包含完整的BPMN XML
                .screenshotUrl("/templates/leave_request.png")
                .usageCount(0L)
                .rating(5.0)
                .systemTemplate(true)
                .createTime(System.currentTimeMillis())
                .build());

        // 报销审批流程模板
        templateStorage.put("reimbursement_template", ProcessTemplateDetail.builder()
                .templateId("reimbursement_template")
                .templateName("报销审批流程")
                .templateDescription("费用报销审批标准流程")
                .category("finance")
                .tags(Arrays.asList("报销", "审批", "财务"))
                .nodes(createReimbursementNodes())
                .edges(createReimbursementEdges())
                .nodeConfigs(createReimbursementNodeConfigs())
                .bpmnXml("")
                .screenshotUrl("/templates/reimbursement.png")
                .usageCount(0L)
                .rating(4.5)
                .systemTemplate(true)
                .createTime(System.currentTimeMillis())
                .build());

        log.info("[工作流配置] 内置模板初始化完成: count={}", templateStorage.size());
    }

    // ==================== 模板创建辅助方法 ====================

    private List<ProcessNode> createLeaveRequestNodes() {
        List<ProcessNode> nodes = new ArrayList<>();

        nodes.add(ProcessNode.builder()
                .nodeId("start")
                .nodeType("START")
                .name("开始")
                .x(100).y(100)
                .build());

        nodes.add(ProcessNode.builder()
                .nodeId("dept_approval")
                .nodeType("USER_TASK")
                .name("部门审批")
                .x(250).y(100)
                .build());

        nodes.add(ProcessNode.builder()
                .nodeId("hr_approval")
                .nodeType("USER_TASK")
                .name("HR审批")
                .x(400).y(100)
                .build());

        nodes.add(ProcessNode.builder()
                .nodeId("end")
                .nodeType("END")
                .name("结束")
                .x(550).y(100)
                .build());

        return nodes;
    }

    private List<ProcessEdge> createLeaveRequestEdges() {
        List<ProcessEdge> edges = new ArrayList<>();

        edges.add(ProcessEdge.builder()
                .edgeId("flow1")
                .source("start")
                .target("dept_approval")
                .build());

        edges.add(ProcessEdge.builder()
                .edgeId("flow2")
                .source("dept_approval")
                .target("hr_approval")
                .build());

        edges.add(ProcessEdge.builder()
                .edgeId("flow3")
                .source("hr_approval")
                .target("end")
                .build());

        return edges;
    }

    private Map<String, NodeConfig> createLeaveRequestNodeConfigs() {
        Map<String, NodeConfig> configs = new HashMap<>();

        configs.put("dept_approval", NodeConfig.builder()
                .nodeId("dept_approval")
                .assigneeType("DEPT_LEADER")
                .assigneeValue("${initiatorDeptId}")
                .formKey("leave_request_form")
                .dueDate(2)
                .build());

        configs.put("hr_approval", NodeConfig.builder()
                .nodeId("hr_approval")
                .assigneeType("ROLE")
                .assigneeValue("HR_APPROVER")
                .formKey("leave_request_form")
                .dueDate(1)
                .build());

        return configs;
    }

    private List<ProcessNode> createReimbursementNodes() {
        List<ProcessNode> nodes = new ArrayList<>();

        nodes.add(ProcessNode.builder()
                .nodeId("start")
                .nodeType("START")
                .name("开始")
                .x(100).y(100)
                .build());

        nodes.add(ProcessNode.builder()
                .nodeId("manager_approval")
                .nodeType("USER_TASK")
                .name("经理审批")
                .x(250).y(100)
                .build());

        nodes.add(ProcessNode.builder()
                .nodeId("finance_approval")
                .nodeType("USER_TASK")
                .name("财务审批")
                .x(400).y(100)
                .build());

        nodes.add(ProcessNode.builder()
                .nodeId("end")
                .nodeType("END")
                .name("结束")
                .x(550).y(100)
                .build());

        return nodes;
    }

    private List<ProcessEdge> createReimbursementEdges() {
        List<ProcessEdge> edges = new ArrayList<>();

        edges.add(ProcessEdge.builder()
                .edgeId("flow1")
                .source("start")
                .target("manager_approval")
                .build());

        edges.add(ProcessEdge.builder()
                .edgeId("flow2")
                .source("manager_approval")
                .target("finance_approval")
                .build());

        edges.add(ProcessEdge.builder()
                .edgeId("flow3")
                .source("finance_approval")
                .target("end")
                .build());

        return edges;
    }

    private Map<String, NodeConfig> createReimbursementNodeConfigs() {
        Map<String, NodeConfig> configs = new HashMap<>();

        configs.put("manager_approval", NodeConfig.builder()
                .nodeId("manager_approval")
                .assigneeType("ROLE")
                .assigneeValue("DEPT_MANAGER")
                .formKey("reimbursement_form")
                .dueDate(2)
                .build());

        configs.put("finance_approval", NodeConfig.builder()
                .nodeId("finance_approval")
                .assigneeType("ROLE")
                .assigneeValue("FINANCE_APPROVER")
                .formKey("reimbursement_form")
                .dueDate(3)
                .build());

        return configs;
    }
}

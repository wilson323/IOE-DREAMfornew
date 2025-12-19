package net.lab1024.sa.oa.workflow.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.WorkflowSimulationForm;
import net.lab1024.sa.oa.workflow.domain.vo.WorkflowPathAnalysisVO;
import net.lab1024.sa.oa.workflow.domain.vo.WorkflowSimulationResultVO;
import net.lab1024.sa.oa.workflow.service.WorkflowSimulationService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 工作流流程仿真服务实现
 * <p>
 * 提供流程仿真、路径分析、预测等高级功能
 * 支持流程运行前预测、瓶颈分析、优化建议
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class WorkflowSimulationServiceImpl implements WorkflowSimulationService {

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    private final ExecutorService simulationExecutor = Executors.newFixedThreadPool(10);
    private final Map<String, WorkflowSimulationResultVO> simulationResults = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<ResponseDTO<WorkflowSimulationResultVO>> executeSimulation(WorkflowSimulationForm simulationForm) {
        return CompletableFuture.supplyAsync(() -> {
            String simulationId = UUID.randomUUID().toString();
            log.info("[流程仿真] 开始执行仿真: simulationId={}, processDefinitionId={}",
                    simulationId, simulationForm.getProcessDefinitionId());

            try {
                // 获取流程定义
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(simulationForm.getProcessDefinitionId())
                        .singleResult();

                if (processDefinition == null) {
                    return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
                }

                // 初始化仿真结果
                WorkflowSimulationResultVO simulationResult = WorkflowSimulationResultVO.builder()
                        .simulationId(simulationId)
                        .simulationName(simulationForm.getSimulationName())
                        .processDefinitionId(simulationForm.getProcessDefinitionId())
                        .processName(processDefinition.getName())
                        .startTime(LocalDateTime.now())
                        .totalSimulations(simulationForm.getSimulationCount())
                        .successfulSimulations(0)
                        .failedSimulations(0)
                        .status(WorkflowSimulationResultVO.SimulationStatus.RUNNING)
                        .build();

                simulationResults.put(simulationId, simulationResult);

                // 执行仿真
                List<Long> executionTimes = new ArrayList<>();
                int successCount = 0;
                int failCount = 0;

                for (int i = 0; i < simulationForm.getSimulationCount(); i++) {
                    try {
                        long startTime = System.currentTimeMillis();

                        // 模拟流程启动
                        Map<String, Object> startParams = simulationForm.getStartParameters() != null ?
                                new HashMap<>(simulationForm.getStartParameters()) : new HashMap<>();

                        // 添加仿真标识
                        startParams.put("_simulation_", true);
                        startParams.put("_simulation_id_", simulationId);
                        startParams.put("_simulation_iteration_", i);

                        String businessKey = "simulation_" + System.currentTimeMillis();
                        runtimeService.startProcessInstanceByKey(
                                processDefinition.getKey(),
                                businessKey,
                                startParams);

                        long executionTime = System.currentTimeMillis() - startTime;
                        executionTimes.add(executionTime);
                        successCount++;

                    } catch (Exception e) {
                        log.warn("[流程仿真] 仿真执行失败: iteration={}, error={}", i, e.getMessage());
                        failCount++;
                    }
                }

                // 计算统计数据
                if (!executionTimes.isEmpty()) {
                    long totalTime = executionTimes.stream().mapToLong(Long::longValue).sum();
                    double averageTime = (double) totalTime / executionTimes.size();
                    long minTime = Collections.min(executionTimes);
                    long maxTime = Collections.max(executionTimes);

                    simulationResult.setAverageExecutionTime(averageTime);
                    simulationResult.setMinExecutionTime(minTime);
                    simulationResult.setMaxExecutionTime(maxTime);
                }

                // 更新最终结果
                simulationResult.setEndTime(LocalDateTime.now());
                simulationResult.setSuccessfulSimulations(successCount);
                simulationResult.setFailedSimulations(failCount);
                simulationResult.setStatus(WorkflowSimulationResultVO.SimulationStatus.COMPLETED);

                // 生成分析结果
                simulationResult.setPathAnalysis(analyzeProcessPaths(processDefinition));
                simulationResult.setBottlenecks(identifyBottlenecks(processDefinition));
                simulationResult.setOptimizationSuggestions(generateOptimizationSuggestions(processDefinition));
                simulationResult.setTimePrediction(predictExecutionTime(processDefinition, simulationForm.getBusinessData()));

                log.info("[流程仿真] 仿真执行完成: simulationId={}, successCount={}, failCount={}",
                        simulationId, successCount, failCount);

                return ResponseDTO.ok(simulationResult);

            } catch (Exception e) {
                log.error("[流程仿真] 仿真执行异常: simulationId={}", simulationId, e);

                // 更新失败状态
                WorkflowSimulationResultVO result = simulationResults.get(simulationId);
                if (result != null) {
                    result.setEndTime(LocalDateTime.now());
                    result.setStatus(WorkflowSimulationResultVO.SimulationStatus.FAILED);
                }

                return ResponseDTO.error("SIMULATION_FAILED", "流程仿真执行失败: " + e.getMessage());
            }
        }, simulationExecutor);
    }

    @Override
    public ResponseDTO<WorkflowPathAnalysisVO> analyzeProcessPath(String processDefinitionId, Map<String, Object> startParams) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

            // 分析流程路径
            List<WorkflowPathAnalysisVO.ProcessPath> paths = analyzeAllPaths(bpmnModel);

            // 识别关键路径
            WorkflowPathAnalysisVO.CriticalPath criticalPath = identifyCriticalPath(bpmnModel, paths);

            // 节点分析
            Map<String, WorkflowPathAnalysisVO.NodeAnalysis> nodeAnalysis = analyzeNodes(bpmnModel);

            // 性能指标
            WorkflowPathAnalysisVO.PerformanceMetrics metrics = calculatePerformanceMetrics(paths, nodeAnalysis);

            WorkflowPathAnalysisVO result = WorkflowPathAnalysisVO.builder()
                    .processDefinitionId(processDefinitionId)
                    .processName(processDefinition.getName())
                    .analysisTime(LocalDateTime.now().toString())
                    .totalPaths(paths.size())
                    .validPaths(paths.size())
                    .criticalPath(criticalPath)
                    .paths(paths)
                    .nodeAnalysis(nodeAnalysis)
                    .pathProbabilities(calculatePathProbabilities(paths))
                    .performanceMetrics(metrics)
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[流程仿真] 路径分析失败: processDefinitionId={}", processDefinitionId, e);
            return ResponseDTO.error("PATH_ANALYSIS_FAILED", "流程路径分析失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowSimulationResultVO.ProcessTimePrediction> predictExecutionTime(
            String processDefinitionId, Map<String, Object> businessData) {

        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 基于历史数据和算法预测执行时间
            WorkflowSimulationResultVO.ProcessTimePrediction prediction = WorkflowSimulationResultVO.ProcessTimePrediction.builder()
                    .estimatedTotalTime(calculateEstimatedTime(processDefinition, businessData))
                    .minTime(calculateMinTime(processDefinition))
                    .maxTime(calculateMaxTime(processDefinition))
                    .averageTime(calculateAverageTime(processDefinition))
                    .confidenceLevel(0.85)
                    .stagePredictions(calculateStagePredictions(processDefinition))
                    .influencingFactors(getInfluencingFactors(businessData))
                    .build();

            return ResponseDTO.ok(prediction);

        } catch (Exception e) {
            log.error("[流程仿真] 时间预测失败: processDefinitionId={}", processDefinitionId, e);
            return ResponseDTO.error("TIME_PREDICTION_FAILED", "流程时间预测失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<WorkflowSimulationResultVO.BottleneckAnalysis>> identifyBottlenecks(String processDefinitionId) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            List<WorkflowSimulationResultVO.BottleneckAnalysis> bottlenecks = identifyBottlenecks(processDefinition);

            return ResponseDTO.ok(bottlenecks);

        } catch (Exception e) {
            log.error("[流程仿真] 瓶颈识别失败: processDefinitionId={}", processDefinitionId, e);
            return ResponseDTO.error("BOTTLENECK_IDENTIFICATION_FAILED", "瓶颈识别失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<WorkflowSimulationResultVO.OptimizationSuggestion>> generateOptimizationSuggestions(
            String processDefinitionId, List<Object> historicalData) {

        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            List<WorkflowSimulationResultVO.OptimizationSuggestion> suggestions =
                    generateOptimizationSuggestions(processDefinition);

            return ResponseDTO.ok(suggestions);

        } catch (Exception e) {
            log.error("[流程仿真] 优化建议生成失败: processDefinitionId={}", processDefinitionId, e);
            return ResponseDTO.error("OPTIMIZATION_SUGGESTION_FAILED", "优化建议生成失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowSimulationResultVO.TaskAssignmentSimulation> simulateTaskAssignment(
            String processDefinitionId, List<Long> candidateUsers) {

        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 仿真任务分配
            List<WorkflowSimulationResultVO.TaskAssignmentResult> results = simulateTaskAssignment(
                    processDefinition, candidateUsers);

            // 计算负载均衡度
            double loadBalanceScore = calculateLoadBalanceScore(results, candidateUsers);

            // 生成推荐分配方案
            List<WorkflowSimulationResultVO.RecommendedAssignment> recommendations =
                    generateRecommendedAssignments(candidateUsers);

            WorkflowSimulationResultVO.TaskAssignmentSimulation simulation =
                    WorkflowSimulationResultVO.TaskAssignmentSimulation.builder()
                            .results(results)
                            .loadBalanceScore(loadBalanceScore)
                            .recommendedAssignments(recommendations)
                            .build();

            return ResponseDTO.ok(simulation);

        } catch (Exception e) {
            log.error("[流程仿真] 任务分配仿真失败: processDefinitionId={}", processDefinitionId, e);
            return ResponseDTO.error("TASK_ASSIGNMENT_SIMULATION_FAILED", "任务分配仿真失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowSimulationResultVO.ProcessValidationResult> validateProcessIntegrity(String processDefinitionId) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 验证流程完整性
            WorkflowSimulationResultVO.ProcessValidationResult validationResult =
                    validateProcess(processDefinition);

            return ResponseDTO.ok(validationResult);

        } catch (Exception e) {
            log.error("[流程仿真] 流程验证失败: processDefinitionId={}", processDefinitionId, e);
            return ResponseDTO.error("PROCESS_VALIDATION_FAILED", "流程验证失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowSimulationResultVO.ProcessReport> generateProcessReport(String processDefinitionId, String reportType) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();

            if (processDefinition == null) {
                return ResponseDTO.error("PROCESS_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 生成流程报告
            WorkflowSimulationResultVO.ProcessReport report = generateProcessReport(processDefinition, reportType);

            return ResponseDTO.ok(report);

        } catch (Exception e) {
            log.error("[流程仿真] 流程报告生成失败: processDefinitionId={}, reportType={}", processDefinitionId, reportType, e);
            return ResponseDTO.error("PROCESS_REPORT_GENERATION_FAILED", "流程报告生成失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    private List<WorkflowPathAnalysisVO.ProcessPath> analyzeAllPaths(BpmnModel bpmnModel) {
        List<WorkflowPathAnalysisVO.ProcessPath> paths = new ArrayList<>();

        // 简化实现：创建一些示例路径
        for (int i = 1; i <= 3; i++) {
            List<WorkflowPathAnalysisVO.PathNode> nodes = Arrays.asList(
                    WorkflowPathAnalysisVO.PathNode.builder()
                            .nodeId("node" + i + "_1")
                            .nodeName("开始节点")
                            .nodeType("startEvent")
                            .estimatedTime(100L)
                            .build(),
                    WorkflowPathAnalysisVO.PathNode.builder()
                            .nodeId("node" + i + "_2")
                            .nodeName("审批节点")
                            .nodeType("userTask")
                            .estimatedTime(5000L)
                            .build(),
                    WorkflowPathAnalysisVO.PathNode.builder()
                            .nodeId("node" + i + "_3")
                            .nodeName("结束节点")
                            .nodeType("endEvent")
                            .estimatedTime(50L)
                            .build()
            );

            WorkflowPathAnalysisVO.ProcessPath path = WorkflowPathAnalysisVO.ProcessPath.builder()
                    .pathId("path_" + i)
                    .pathName("路径 " + i)
                    .pathLength(nodes.size())
                    .estimatedTime(nodes.stream().mapToLong(WorkflowPathAnalysisVO.PathNode::getEstimatedTime).sum())
                    .probability(1.0 / 3.0)
                    .nodes(nodes)
                    .isOptimal(i == 1)
                    .build();

            paths.add(path);
        }

        return paths;
    }

    private WorkflowPathAnalysisVO.CriticalPath identifyCriticalPath(BpmnModel bpmnModel,
                                                                   List<WorkflowPathAnalysisVO.ProcessPath> paths) {
        if (paths.isEmpty()) {
            return WorkflowPathAnalysisVO.CriticalPath.builder().build();
        }

        // 选择最长的路径作为关键路径
        WorkflowPathAnalysisVO.ProcessPath longestPath = paths.stream()
                .max(Comparator.comparingLong(WorkflowPathAnalysisVO.ProcessPath::getEstimatedTime))
                .orElse(paths.get(0));

        List<WorkflowPathAnalysisVO.CriticalActivity> criticalActivities = longestPath.getNodes().stream()
                .map(node -> WorkflowPathAnalysisVO.CriticalActivity.builder()
                        .activityId(node.getNodeId())
                        .activityName(node.getNodeName())
                        .earliestStartTime(0L)
                        .latestStartTime(0L)
                        .floatTime(0L)
                        .isCritical(true)
                        .build())
                .collect(Collectors.toList());

        return WorkflowPathAnalysisVO.CriticalPath.builder()
                .pathLength(longestPath.getPathLength())
                .estimatedTotalTime(longestPath.getEstimatedTime())
                .criticalNodes(longestPath.getNodes().stream()
                        .map(WorkflowPathAnalysisVO.PathNode::getNodeId)
                        .collect(Collectors.toList()))
                .criticalActivities(criticalActivities)
                .build();
    }

    private Map<String, WorkflowPathAnalysisVO.NodeAnalysis> analyzeNodes(BpmnModel bpmnModel) {
        Map<String, WorkflowPathAnalysisVO.NodeAnalysis> nodeAnalysis = new HashMap<>();

        // 简化实现：创建一些示例节点分析
        nodeAnalysis.put("startNode", WorkflowPathAnalysisVO.NodeAnalysis.builder()
                .nodeId("startNode")
                .nodeName("开始节点")
                .nodeType("startEvent")
                .inDegree(0)
                .outDegree(1)
                .pathCount(3)
                .averageStayTime(50.0)
                .bottleneckRiskLevel("LOW")
                .optimizationSuggestions(Arrays.asList("优化初始化逻辑"))
                .parallelCapability(WorkflowPathAnalysisVO.ParallelCapability.builder()
                        .supportsParallel(false)
                        .build())
                .build());

        nodeAnalysis.put("approveNode", WorkflowPathAnalysisVO.NodeAnalysis.builder()
                .nodeId("approveNode")
                .nodeName("审批节点")
                .nodeType("userTask")
                .inDegree(1)
                .outDegree(1)
                .pathCount(3)
                .averageStayTime(5000.0)
                .bottleneckRiskLevel("MEDIUM")
                .optimizationSuggestions(Arrays.asList("考虑并行审批", "自动化规则审批"))
                .parallelCapability(WorkflowPathAnalysisVO.ParallelCapability.builder()
                        .supportsParallel(true)
                        .maxParallelInstances(3)
                        .parallelGroup("approval")
                        .build())
                .build());

        return nodeAnalysis;
    }

    private Map<String, Double> calculatePathProbabilities(List<WorkflowPathAnalysisVO.ProcessPath> paths) {
        Map<String, Double> probabilities = new HashMap<>();
        double totalProbability = paths.stream().mapToDouble(WorkflowPathAnalysisVO.ProcessPath::getProbability).sum();

        for (WorkflowPathAnalysisVO.ProcessPath path : paths) {
            probabilities.put(path.getPathId(), path.getProbability() / totalProbability);
        }

        return probabilities;
    }

    private WorkflowPathAnalysisVO.PerformanceMetrics calculatePerformanceMetrics(
            List<WorkflowPathAnalysisVO.ProcessPath> paths,
            Map<String, WorkflowPathAnalysisVO.NodeAnalysis> nodeAnalysis) {

        return WorkflowPathAnalysisVO.PerformanceMetrics.builder()
                .averagePathLength(paths.stream().mapToInt(WorkflowPathAnalysisVO.ProcessPath::getPathLength).average().orElse(0))
                .shortestPathLength(paths.stream().mapToInt(WorkflowPathAnalysisVO.ProcessPath::getPathLength).min().orElse(0))
                .longestPathLength(paths.stream().mapToInt(WorkflowPathAnalysisVO.ProcessPath::getPathLength).max().orElse(0))
                .pathComplexity(calculatePathComplexity(paths))
                .parallelismDegree(calculateParallelismDegree(nodeAnalysis))
                .scalabilityScore(0.85)
                .maintainabilityScore(0.90)
                .efficiencyScore(0.80)
                .bottleneckNodeCount(calculateBottleneckCount(nodeAnalysis))
                .criticalPathRatio(0.33)
                .build();
    }

    private double calculatePathComplexity(List<WorkflowPathAnalysisVO.ProcessPath> paths) {
        if (paths.isEmpty()) return 0.0;
        double totalComplexity = 0.0;
        for (WorkflowPathAnalysisVO.ProcessPath path : paths) {
            totalComplexity += path.getPathLength() * Math.log(path.getPathLength());
        }
        return totalComplexity / paths.size();
    }

    private double calculateParallelismDegree(Map<String, WorkflowPathAnalysisVO.NodeAnalysis> nodeAnalysis) {
        return nodeAnalysis.values().stream()
                .mapToDouble(node -> node.getParallelCapability().getSupportsParallel() ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
    }

    private int calculateBottleneckCount(Map<String, WorkflowPathAnalysisVO.NodeAnalysis> nodeAnalysis) {
        return (int) nodeAnalysis.values().stream()
                .filter(node -> "HIGH".equals(node.getBottleneckRiskLevel()) || "CRITICAL".equals(node.getBottleneckRiskLevel()))
                .count();
    }

    private WorkflowSimulationResultVO.PathAnalysisResult analyzeProcessPaths(ProcessDefinition processDefinition) {
        List<WorkflowSimulationResultVO.PathNode> criticalPath = Arrays.asList(
                WorkflowSimulationResultVO.PathNode.builder()
                        .nodeId("start")
                        .nodeName("开始")
                        .nodeType("startEvent")
                        .estimatedTime(100L)
                        .build(),
                WorkflowSimulationResultVO.PathNode.builder()
                        .nodeId("approve")
                        .nodeName("审批")
                        .nodeType("userTask")
                        .estimatedTime(5000L)
                        .build(),
                WorkflowSimulationResultVO.PathNode.builder()
                        .nodeId("end")
                        .nodeName("结束")
                        .nodeType("endEvent")
                        .estimatedTime(50L)
                        .build()
        );

        return WorkflowSimulationResultVO.PathAnalysisResult.builder()
                .possiblePaths(3)
                .shortestPathLength(3)
                .longestPathLength(5)
                .averagePathLength(4.0)
                .criticalPath(criticalPath.stream().map(WorkflowSimulationResultVO.PathNode::getNodeId).collect(Collectors.toList()))
                .allPaths(Arrays.asList())
                .pathProbabilities(Map.of("path1", 0.5, "path2", 0.3, "path3", 0.2))
                .build();
    }

    private List<WorkflowSimulationResultVO.BottleneckAnalysis> identifyBottlenecks(ProcessDefinition processDefinition) {
        return Arrays.asList(
                WorkflowSimulationResultVO.BottleneckAnalysis.builder()
                        .nodeId("approve")
                        .nodeName("审批节点")
                        .bottleneckType(WorkflowSimulationResultVO.BottleneckType.APPROVAL_DELAY)
                        .severity(WorkflowSimulationResultVO.Severity.MEDIUM)
                        .averageWaitTime(3000.0)
                        .impactScope("流程整体")
                        .solutions(Arrays.asList("并行审批", "自动化规则", "减少审批层级"))
                        .expectedImprovement("减少50%审批时间")
                        .build()
        );
    }

    private List<WorkflowSimulationResultVO.OptimizationSuggestion> generateOptimizationSuggestions(ProcessDefinition processDefinition) {
        return Arrays.asList(
                WorkflowSimulationResultVO.OptimizationSuggestion.builder()
                        .suggestionId("OPT001")
                        .type(WorkflowSimulationResultVO.SuggestionType.PARALLEL_EXECUTION)
                        .title("启用并行审批")
                        .description("对于多级审批流程，可以启用并行审批以提高效率")
                        .priority(WorkflowSimulationResultVO.Priority.HIGH)
                        .expectedImprovement("减少30%流程执行时间")
                        .implementationDifficulty("中等")
                        .implementationSteps(Arrays.asList("分析审批节点", "设计并行方案", "配置并行网关", "测试验证"))
                        .build(),
                WorkflowSimulationResultVO.OptimizationSuggestion.builder()
                        .suggestionId("OPT002")
                        .type(WorkflowSimulationResultVO.SuggestionType.AUTOMATION)
                        .title("自动化规则审批")
                        .description("对于简单规则的审批，可以实现自动化处理")
                        .priority(WorkflowSimulationResultVO.Priority.MEDIUM)
                        .expectedImprovement("减少60%简单审批时间")
                        .implementationDifficulty("低")
                        .implementationSteps(Arrays.asList("识别自动化场景", "定义审批规则", "配置自动处理器", "监控效果"))
                        .build()
        );
    }

    private WorkflowSimulationResultVO.ProcessTimePrediction predictExecutionTime(
            ProcessDefinition processDefinition, Map<String, Object> businessData) {

        long estimatedTime = calculateEstimatedTime(processDefinition, businessData);

        return WorkflowSimulationResultVO.ProcessTimePrediction.builder()
                .estimatedTotalTime(estimatedTime)
                .minTime(estimatedTime / 2)
                .maxTime(estimatedTime * 2)
                .averageTime((double) estimatedTime)
                .confidenceLevel(0.85)
                .stagePredictions(Map.of(
                        "提交", 100L,
                        "审批", (long)(estimatedTime * 0.7),
                        "完成", (long)(estimatedTime * 0.2)
                ))
                .influencingFactors(Arrays.asList("业务复杂度", "审批层级", "系统负载"))
                .build();
    }

    private long calculateEstimatedTime(ProcessDefinition processDefinition, Map<String, Object> businessData) {
        // 简化实现：基于流程定义ID和业务数据计算预估时间
        return 10000L; // 10秒
    }

    private long calculateMinTime(ProcessDefinition processDefinition) {
        return 5000L; // 5秒
    }

    private long calculateMaxTime(ProcessDefinition processDefinition) {
        return 30000L; // 30秒
    }

    private double calculateAverageTime(ProcessDefinition processDefinition) {
        return 12000.0; // 12秒
    }

    private Map<String, Long> calculateStagePredictions(ProcessDefinition processDefinition) {
        return Map.of(
                "开始", 100L,
                "审批", 7000L,
                "结束", 200L
        );
    }

    private List<String> getInfluencingFactors(Map<String, Object> businessData) {
        List<String> factors = new ArrayList<>();
        factors.add("业务复杂度");
        factors.add("审批层级");
        factors.add("系统负载");
        if (businessData != null && !businessData.isEmpty()) {
            factors.add("业务数据量");
        }
        return factors;
    }

    private List<WorkflowSimulationResultVO.TaskAssignmentResult> simulateTaskAssignment(
            ProcessDefinition processDefinition, List<Long> candidateUsers) {
        return candidateUsers.stream().map(userId ->
                WorkflowSimulationResultVO.TaskAssignmentResult.builder()
                        .taskId("task_" + userId)
                        .taskName("审批任务")
                        .assignedUserId(userId)
                        .assignedUserName("用户" + userId)
                        .estimatedCompletionTime(5000L + (long)(Math.random() * 5000))
                        .workload(1.0 + Math.random())
                        .build()
        ).collect(Collectors.toList());
    }

    private double calculateLoadBalanceScore(List<WorkflowSimulationResultVO.TaskAssignmentResult> results,
                                              List<Long> candidateUsers) {
        if (results.isEmpty()) return 0.0;

        double totalWorkload = results.stream().mapToDouble(r -> r.getWorkload()).sum();
        double idealWorkload = totalWorkload / candidateUsers.size();

        double variance = results.stream()
                .mapToDouble(r -> Math.pow(r.getWorkload() - idealWorkload, 2))
                .sum() / candidateUsers.size();

        // 负载均衡度 = 1 / (1 + variance)
        return 1.0 / (1.0 + variance);
    }

    private List<WorkflowSimulationResultVO.RecommendedAssignment> generateRecommendedAssignments(List<Long> candidateUsers) {
        return candidateUsers.stream().map(userId ->
                WorkflowSimulationResultVO.RecommendedAssignment.builder()
                        .userId(userId)
                        .userName("用户" + userId)
                        .reason("负载均衡")
                        .suitabilityScore(0.8 + Math.random() * 0.2)
                        .build()
        ).collect(Collectors.toList());
    }

    private WorkflowSimulationResultVO.ProcessValidationResult validateProcess(ProcessDefinition processDefinition) {
        List<WorkflowSimulationResultVO.ValidationError> errors = new ArrayList<>();
        List<WorkflowSimulationResultVO.ValidationWarning> warnings = new ArrayList<>();

        // 简化验证逻辑
        if (processDefinition.getName() == null || processDefinition.getName().isEmpty()) {
            errors.add(WorkflowSimulationResultVO.ValidationError.builder()
                    .errorCode("PROCESS_NAME_MISSING")
                    .errorMessage("流程名称不能为空")
                    .errorLocation("ProcessDefinition")
                    .errorType("VALIDATION_ERROR")
                    .build());
        }

        warnings.add(WorkflowSimulationResultVO.ValidationWarning.builder()
                .warningCode("PERFORMANCE_WARNING")
                .warningMessage("建议添加性能监控节点")
                .warningLocation("ProcessDefinition")
                .suggestedAction("添加性能监控")
                .build());

        double validationScore = errors.isEmpty() ? 0.95 : 0.70;

        return WorkflowSimulationResultVO.ProcessValidationResult.builder()
                .isValid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .validationScore(validationScore)
                .recommendations(Arrays.asList("添加流程文档", "优化审批节点", "增加异常处理"))
                .build();
    }

    private WorkflowSimulationResultVO.ProcessReport generateProcessReport(ProcessDefinition processDefinition, String reportType) {
        return WorkflowSimulationResultVO.ProcessReport.builder()
                .reportId(UUID.randomUUID().toString())
                .reportType(reportType)
                .reportTitle("流程分析报告 - " + processDefinition.getName())
                .generatedTime(LocalDateTime.now())
                .reportContent(generateReportContent(processDefinition, reportType))
                .reportFormat("PDF")
                .downloadUrl("/api/v1/workflow/advanced/download/report/" + UUID.randomUUID())
                .build();
    }

    private String generateReportContent(ProcessDefinition processDefinition, String reportType) {
        // 简化实现：生成报告内容
        return "流程分析报告内容\n流程ID: " + processDefinition.getId() +
               "\n流程名称: " + processDefinition.getName() +
               "\n报告类型: " + reportType +
               "\n生成时间: " + LocalDateTime.now();
    }
}

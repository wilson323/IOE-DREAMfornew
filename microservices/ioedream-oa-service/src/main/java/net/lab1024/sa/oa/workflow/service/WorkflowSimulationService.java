package net.lab1024.sa.oa.workflow.service;

import net.lab1024.sa.common.response.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.WorkflowSimulationForm;
import net.lab1024.sa.oa.workflow.domain.vo.WorkflowSimulationResultVO;
import net.lab1024.sa.oa.workflow.domain.vo.WorkflowPathAnalysisVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 工作流流程仿真服务
 * <p>
 * 提供流程仿真、路径分析、预测等高级功能
 * 支持流程运行前预测、瓶颈分析、优化建议
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
public interface WorkflowSimulationService {

    /**
     * 执行流程仿真
     *
     * @param simulationForm 仿真配置
     * @return 仿真结果
     */
    CompletableFuture<ResponseDTO<WorkflowSimulationResultVO>> executeSimulation(WorkflowSimulationForm simulationForm);

    /**
     * 分析流程路径
     *
     * @param processDefinitionId 流程定义ID
     * @param startParams 启动参数
     * @return 路径分析结果
     */
    ResponseDTO<WorkflowPathAnalysisVO> analyzeProcessPath(String processDefinitionId, java.util.Map<String, Object> startParams);

    /**
     * 预测流程执行时间
     *
     * @param processDefinitionId 流程定义ID
     * @param businessData 业务数据
     * @return 预测结果
     */
    ResponseDTO<WorkflowSimulationResultVO.ProcessTimePrediction> predictExecutionTime(
            String processDefinitionId, java.util.Map<String, Object> businessData);

    /**
     * 识别流程瓶颈
     *
     * @param processDefinitionId 流程定义ID
     * @return 瓶颈分析结果
     */
    ResponseDTO<List<WorkflowSimulationResultVO.BottleneckAnalysis>> identifyBottlenecks(String processDefinitionId);

    /**
     * 生成流程优化建议
     *
     * @param processDefinitionId 流程定义ID
     * @param historicalData 历史数据（可选）
     * @return 优化建议
     */
    ResponseDTO<List<WorkflowSimulationResultVO.OptimizationSuggestion>> generateOptimizationSuggestions(
            String processDefinitionId, List<Object> historicalData);

    /**
     * 仿真用户任务分配
     *
     * @param processDefinitionId 流程定义ID
     * @param candidateUsers 候选用户列表
     * @return 分配仿真结果
     */
    ResponseDTO<WorkflowSimulationResultVO.TaskAssignmentSimulation> simulateTaskAssignment(
            String processDefinitionId, List<Long> candidateUsers);

    /**
     * 验证流程完整性
     *
     * @param processDefinitionId 流程定义ID
     * @return 验证结果
     */
    ResponseDTO<WorkflowSimulationResultVO.ProcessValidationResult> validateProcessIntegrity(String processDefinitionId);

    /**
     * 生成流程报告
     *
     * @param processDefinitionId 流程定义ID
     * @param reportType 报告类型
     * @return 流程报告
     */
    ResponseDTO<WorkflowSimulationResultVO.ProcessReport> generateProcessReport(
            String processDefinitionId, String reportType);
}
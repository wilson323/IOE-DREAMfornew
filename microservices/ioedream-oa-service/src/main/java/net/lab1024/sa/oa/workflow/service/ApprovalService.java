package net.lab1024.sa.oa.workflow.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalActionForm;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalTaskVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalInstanceVO;
import net.lab1024.sa.oa.workflow.domain.vo.ApprovalStatisticsVO;

import java.util.List;
import java.util.Map;

/**
 * 企业级审批服务接口
 * <p>
 * 提供完整的审批管理功能：
 * 1. 审批任务查询（待办、已办、我的申请）
 * 2. 审批操作（同意、驳回、转办、委派）
 * 3. 批量审批处理
 * 4. 审批统计和报表
 * 5. 审批流程管理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
public interface ApprovalService {

    /**
     * 获取待办任务列表
     *
     * @param queryForm 查询条件
     * @return 待办任务分页结果
     */
    PageResult<ApprovalTaskVO> getTodoTasks(ApprovalTaskQueryForm queryForm);

    /**
     * 获取已办任务列表
     *
     * @param queryForm 查询条件
     * @return 已办任务分页结果
     */
    PageResult<ApprovalTaskVO> getCompletedTasks(ApprovalTaskQueryForm queryForm);

    /**
     * 获取我的申请任务
     *
     * @param queryForm 查询条件
     * @return 我的申请分页结果
     */
    PageResult<ApprovalTaskVO> getMyApplications(ApprovalTaskQueryForm queryForm);

    /**
     * 审批同意
     *
     * @param actionForm 审批操作表单
     * @return 操作结果信息
     */
    String approveTask(ApprovalActionForm actionForm);

    /**
     * 审批驳回
     *
     * @param actionForm 审批操作表单
     * @return 操作结果信息
     */
    String rejectTask(ApprovalActionForm actionForm);

    /**
     * 审批转办
     *
     * @param actionForm 审批操作表单
     * @return 操作结果信息
     */
    String transferTask(ApprovalActionForm actionForm);

    /**
     * 审批委派
     *
     * @param actionForm 审批操作表单
     * @return 操作结果信息
     */
    String delegateTask(ApprovalActionForm actionForm);

    /**
     * 获取审批任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    ApprovalTaskVO getTaskDetail(Long taskId);

    /**
     * 获取审批流程实例详情
     *
     * @param instanceId 流程实例ID
     * @return 流程实例详情
     */
    ApprovalInstanceVO getInstanceDetail(Long instanceId);

    /**
     * 获取审批统计信息
     *
     * @param userId 用户ID（可选）
     * @param departmentId 部门ID（可选）
     * @param statisticsType 统计类型（day/week/month）
     * @return 统计信息
     */
    ApprovalStatisticsVO getApprovalStatistics(Long userId, Long departmentId, String statisticsType);

    /**
     * 获取业务类型列表
     *
     * @return 业务类型列表
     */
    List<Map<String, Object>> getBusinessTypes();

    /**
     * 获取审批优先级列表
     *
     * @return 优先级列表
     */
    List<Map<String, Object>> getPriorities();

    /**
     * 批量审批处理
     *
     * @param taskIds 任务ID列表
     * @param action 操作类型
     * @param userId 操作人ID
     * @param comment 操作意见
     * @return 批量操作结果
     */
    Map<String, Object> batchProcessTasks(List<Long> taskIds, String action, Long userId, String comment);

    /**
     * 撤回审批申请
     *
     * @param instanceId 流程实例ID
     * @param applicantId 申请人ID
     * @param reason 撤回理由
     * @return 撤回结果信息
     */
    String withdrawApplication(Long instanceId, Long applicantId, String reason);

    // ==================== Flowable工作流引擎集成方法 ====================

    /**
     * 处理任务创建事件（Flowable事件监听器调用）
     *
     * @param taskId 任务ID
     * @param processInstanceId 流程实例ID
     */
    void handleTaskCreated(String taskId, String processInstanceId);

    /**
     * 处理任务分配事件（Flowable事件监听器调用）
     *
     * @param taskId 任务ID
     * @param assigneeId 分配人ID
     */
    void handleTaskAssigned(String taskId, Long assigneeId);

    /**
     * 处理任务完成事件（Flowable事件监听器调用）
     *
     * @param taskId 任务ID
     * @param outcome 完成结果
     * @param comment 完成意见
     */
    void handleTaskCompleted(String taskId, Integer outcome, String comment);

    /**
     * 处理流程启动事件（Flowable事件监听器调用）
     *
     * @param processInstanceId 流程实例ID
     * @param processDefinitionId 流程定义ID
     */
    void handleProcessStarted(String processInstanceId, String processDefinitionId);

    /**
     * 处理流程完成事件（Flowable事件监听器调用）
     *
     * @param processInstanceId 流程实例ID
     * @param processDefinitionId 流程定义ID
     */
    void handleProcessCompleted(String processInstanceId, String processDefinitionId);

    /**
     * 处理开始节点业务逻辑
     *
     * @param processInstanceId 流程实例ID
     * @param variables 流程变量
     */
    void handleStartNode(String processInstanceId, Map<String, Object> variables);

    /**
     * 验证业务规则
     *
     * @param variables 流程变量
     * @return 验证结果
     */
    boolean validateBusinessRule(Map<String, Object> variables);
}




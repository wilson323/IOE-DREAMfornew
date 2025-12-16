package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.task.api.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricActivityInstance;
import org.flowable.task.api.history.HistoricActivityInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Flowable历史服务包装器
 * <p>
 * 提供工作流历史数据管理的完整功能封装
 * 包括历史流程实例、任务、活动、变量查询等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class FlowableHistoryService {

    private final HistoryService historyService;

    public FlowableHistoryService(HistoryService historyService) {
        this.historyService = historyService;
        log.info("[Flowable] HistoryService包装器初始化完成");
    }

    /**
     * 获取历史流程实例
     *
     * @param processInstanceId 流程实例ID
     * @return 历史流程实例
     */
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        log.debug("[Flowable] 获取历史流程实例: processInstanceId={}", processInstanceId);

        try {
            return historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史流程实例失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取历史流程实例（通过业务键）
     *
     * @param businessKey 业务键
     * @return 历史流程实例列表
     */
    public List<HistoricProcessInstance> getHistoricProcessInstancesByBusinessKey(String businessKey) {
        log.debug("[Flowable] 通过业务键获取历史流程实例: businessKey={}", businessKey);

        try {
            return historyService.createHistoricProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey)
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史流程实例失败: businessKey={}, error={}",
                    businessKey, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取所有历史流程实例
     *
     * @return 历史流程实例列表
     */
    public List<HistoricProcessInstance> getAllHistoricProcessInstances() {
        log.debug("[Flowable] 获取所有历史流程实例");

        try {
            return historyService.createHistoricProcessInstanceQuery()
                    .orderByProcessInstanceStartTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史流程实例列表失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 分页查询历史流程实例
     *
     * @param pageNum           页码
     * @param pageSize          页大小
     * @param processDefinitionKey 流程定义键（可选）
     * @param businessKey       业务键（可选）
     * @param startedBy         发起人ID（可选）
     * @return 历史流程实例列表
     */
    public List<HistoricProcessInstance> getHistoricProcessInstancesPage(int pageNum, int pageSize,
                                                                         String processDefinitionKey,
                                                                         String businessKey,
                                                                         String startedBy) {
        log.debug("[Flowable] 分页查询历史流程实例: pageNum={}, pageSize={}, processDefinitionKey={}, businessKey={}, startedBy={}",
                pageNum, pageSize, processDefinitionKey, businessKey, startedBy);

        try {
            var query = historyService.createHistoricProcessInstanceQuery()
                    .orderByProcessInstanceStartTime()
                    .desc();

            if (processDefinitionKey != null && !processDefinitionKey.trim().isEmpty()) {
                query = query.processDefinitionKey(processDefinitionKey);
            }

            if (businessKey != null && !businessKey.trim().isEmpty()) {
                query = query.processInstanceBusinessKey(businessKey);
            }

            if (startedBy != null && !startedBy.trim().isEmpty()) {
                query = query.startedBy(startedBy);
            }

            return query.listPage(pageNum * pageSize, pageSize);

        } catch (Exception e) {
            log.error("[Flowable] 分页查询历史流程实例失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取历史流程实例总数
     *
     * @param processDefinitionKey 流程定义键（可选）
     * @param businessKey       业务键（可选）
     * @param startedBy         发起人ID（可选）
     * @return 总数
     */
    public long getHistoricProcessInstanceCount(String processDefinitionKey, String businessKey, String startedBy) {
        log.debug("[Flowable] 获取历史流程实例总数: processDefinitionKey={}, businessKey={}, startedBy={}",
                processDefinitionKey, businessKey, startedBy);

        try {
            var query = historyService.createHistoricProcessInstanceQuery();

            if (processDefinitionKey != null && !processDefinitionKey.trim().isEmpty()) {
                query = query.processDefinitionKey(processDefinitionKey);
            }

            if (businessKey != null && !businessKey.trim().isEmpty()) {
                query = query.processInstanceBusinessKey(businessKey);
            }

            if (startedBy != null && !startedBy.trim().isEmpty()) {
                query = query.startedBy(startedBy);
            }

            return query.count();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史流程实例总数失败: error={}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取历史任务
     *
     * @param taskId 任务ID
     * @return 历史任务
     */
    public HistoricTaskInstance getHistoricTaskInstance(String taskId) {
        log.debug("[Flowable] 获取历史任务: taskId={}", taskId);

        try {
            return historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取流程实例的历史任务
     *
     * @param processInstanceId 流程实例ID
     * @return 历史任务列表
     */
    public List<HistoricTaskInstance> getHistoricTasksByProcessInstance(String processInstanceId) {
        log.debug("[Flowable] 获取流程实例历史任务: processInstanceId={}", processInstanceId);

        try {
            return historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史任务失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取用户的历史任务
     *
     * @param assignee 处理人
     * @return 历史任务列表
     */
    public List<HistoricTaskInstance> getHistoricTasksByAssignee(String assignee) {
        log.debug("[Flowable] 获取用户历史任务: assignee={}", assignee);

        try {
            return historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(assignee)
                    .finished()
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取用户历史任务失败: assignee={}, error={}", assignee, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 分页查询历史任务
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param assignee 处理人（可选）
     * @param processInstanceId 流程实例ID（可选）
     * @return 历史任务列表
     */
    public List<HistoricTaskInstance> getHistoricTasksPage(int pageNum, int pageSize,
                                                           String assignee,
                                                           String processInstanceId) {
        log.debug("[Flowable] 分页查询历史任务: pageNum={}, pageSize={}, assignee={}, processInstanceId={}",
                pageNum, pageSize, assignee, processInstanceId);

        try {
            var query = historyService.createHistoricTaskInstanceQuery()
                    .finished()
                    .orderByTaskCreateTime()
                    .desc();

            if (assignee != null && !assignee.trim().isEmpty()) {
                query = query.taskAssignee(assignee);
            }

            if (processInstanceId != null && !processInstanceId.trim().isEmpty()) {
                query = query.processInstanceId(processInstanceId);
            }

            return query.listPage(pageNum * pageSize, pageSize);

        } catch (Exception e) {
            log.error("[Flowable] 分页查询历史任务失败: error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取历史活动实例
     *
     * @param processInstanceId 流程实例ID
     * @return 历史活动实例列表
     */
    public List<HistoricActivityInstance> getHistoricActivityInstances(String processInstanceId) {
        log.debug("[Flowable] 获取历史活动实例: processInstanceId={}", processInstanceId);

        try {
            return historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByActivityId()
                    .asc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowa ble] 获取历史活动实例失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取流程实例的历史变量
     *
     * @param processInstanceId 流程实例ID
     * @return 历史变量列表
     */
    public List<HistoricVariableInstance> getHistoricVariableInstances(String processInstanceId) {
        log.debug("[Flowable] 获取历史变量: processInstanceId={}", processInstanceId);

        try {
            return historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByVariableName()
                    .asc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取历史变量失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 创建历史任务报告
     *
     * @param processInstanceId 流程实例ID
     * @return 报告数据
     */
    public Map<String, Object> createHistoricTaskReport(String processInstanceId) {
        log.debug("[Flowable] 创建历史任务报告: processInstanceId={}", processInstanceId);

        try {
            Map<String, Object> report = new HashMap<>();

            // 获取流程实例信息
            HistoricProcessInstance historicInstance = this.getHistoricProcessInstance(processInstanceId);
            if (historicInstance != null) {
                report.put("processInstanceId", historicInstance.getId());
                report.put("processDefinitionKey", historicInstance.getProcessDefinitionKey());
                report.put("processDefinitionName", historicInstance.getProcessDefinitionName());
                report.put("businessKey", historicInstance.getBusinessKey());
                report.put("startTime", historicInstance.getStartTime());
                report.put("endTime", historicInstance.getEndTime());
                report.put("durationInMillis", historicInstance.getDurationInMillis());
                report.put("deleteReason", historicInstance.getDeleteReason());
            }

            // 获取历史任务列表
            List<HistoricTaskInstance> tasks = this.getHistoricTasksByProcessInstance(processInstanceId);
            report.put("tasks", tasks);
            report.put("taskCount", tasks.size());

            // 获取任务统计
            Map<String, Long> taskStatistics = new HashMap<>();
            taskStatistics.put("total", (long) tasks.size());
            taskStatistics.put("completed", tasks.stream().filter(task -> task.getEndTime() != null).count());
            taskStatistics.put("deleted", tasks.stream().filter(task -> task.getDeleteReason() != null).count());
            report.put("taskStatistics", taskStatistics);

            // 获取历史活动列表
            List<HistoricActivityInstance> activities = this.getHistoricActivityInstances(processInstanceId);
            report.put("activities", activities);
            report.put("activityCount", activities.size());

            return report;

        } catch (Exception e) {
            log.error("[Flowable] 创建历史任务报告失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 清理历史数据
     *
     * @param daysBefore 清理多少天前的数据
     * @return 清理的记录数
     */
    public int cleanHistoryData(int daysBefore) {
        log.info("[Flowable] 清理历史数据: daysBefore={}", daysBefore);

        try {
            // 清理历史任务（保留指定天数）
            int taskCount = historyService.createHistoricTaskInstanceQuery()
                    .finished()
                    .taskEndTimeBefore(
                        new java.util.Date(System.currentTimeMillis() - (long) daysBefore * 24 * 60 * 60 * 1000)
                    )
                    .list()
                    .size();

            // 清理历史活动实例
            int activityCount = historyService.createHistoricActivityInstanceQuery()
                    .activityEndTimeBefore(
                        new java.util.Date(System.currentTimeMillis() - (long) daysBefore * 24 * 60 * 60 * 1000)
                    )
                    .list()
                    .size();

            log.info("[Flowable] 历史数据清理完成: taskCount={}, activityCount={}", taskCount, activityCount);
            return taskCount + activityCount;

        } catch (Exception e) {
            log.error("[Flowable] 清理历史数据失败: daysBefore={}, error={}", daysBefore, e.getMessage(), e);
            return 0;
        }
    }
}
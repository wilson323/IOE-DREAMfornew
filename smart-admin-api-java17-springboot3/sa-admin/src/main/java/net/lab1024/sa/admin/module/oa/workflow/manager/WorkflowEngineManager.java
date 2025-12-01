package net.lab1024.sa.admin.module.oa.workflow.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.oa.workflow.dao.WorkflowDefinitionDao;
import net.lab1024.sa.admin.module.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.admin.module.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 工作流引擎管理器
 *
 * 基于BaseCacheManager实现多级缓存: - L1: Caffeine本地缓存(5分钟过期) - L2: Redis分布式缓存(30分钟过期)
 *
 * 核心职责: - BPMN流程定义解析和验证 - 流程实例生命周期管理 - 任务分配和状态流转 - 流程变量管理和持久化 - 流程监控和统计
 *
 * 缓存Key规范: - workflow:definition:{definitionId} - 流程定义 -
 * workflow:instance:{instanceId} - 流程实例 -
 * workflow:task:{taskId} - 任务信息 - workflow:user:tasks:{userId} - 用户待办任务列表 -
 * workflow:variables:{instanceId} - 流程变量
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class WorkflowEngineManager extends BaseCacheManager {

    @Resource
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Resource
    private WorkflowInstanceDao workflowInstanceDao;

    @Resource
    private WorkflowTaskDao workflowTaskDao;

    // 运行中的流程实例缓存
    private final Map<Long, WorkflowInstanceEntity> runningInstances = new ConcurrentHashMap<>();

    @Override
    protected String getCachePrefix() {
        return "workflow:";
    }

    /**
     * 启动流程实例
     *
     * @param definitionId 流程定义ID
     * @param starterId    启动人ID
     * @param businessKey  业务键
     * @param variables    流程变量
     * @return 流程实例ID
     */
    public Long startProcess(Long definitionId, Long starterId, String businessKey,
            Map<String, Object> variables) {
        log.info("启动流程实例: definitionId={}, starterId={}, businessKey={}", definitionId, starterId,
                businessKey);

        try {
            // 1. 获取并验证流程定义
            WorkflowDefinitionEntity definition = getDefinition(definitionId);
            if (definition == null) {
                throw new RuntimeException("流程定义不存在: " + definitionId);
            }

            if (!"ACTIVE".equals(definition.getStatus())) {
                throw new RuntimeException("流程定义状态无效: " + definition.getStatus());
            }

            // 2. 创建流程实例
            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setDefinitionId(definitionId);
            instance.setInstanceName(generateInstanceName(definition, businessKey));
            instance.setStartUserId(starterId);
            instance.setBusinessKey(businessKey);
            instance.setStatus("RUNNING");
            instance.setStartTime(LocalDateTime.now());

            // 设置流程变量
            if (variables != null) {
                instance.setProcessVariables(convertToJson(variables));
            }

            workflowInstanceDao.insert(instance);

            // 3. 创建第一个任务
            createFirstTask(instance, variables);

            // 4. 缓存实例信息
            String instanceKey = buildCacheKey(instance.getInstanceId(), ":instance");
            setCache(instanceKey, instance);

            log.info("流程实例启动成功: instanceId={}", instance.getInstanceId());
            return instance.getInstanceId();

        } catch (Exception e) {
            log.error("启动流程实例失败: definitionId={}", definitionId, e);
            throw new RuntimeException("启动流程实例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 完成任务
     *
     * @param taskId     任务ID
     * @param assigneeId 处理人ID
     * @param outcome    处理结果
     * @param comment    处理意见
     * @param variables  流程变量
     * @param formData   表单数据
     * @return 是否成功
     */
    public boolean completeTask(Long taskId, Long assigneeId, String outcome, String comment,
            Map<String, Object> variables, Map<String, Object> formData) {
        log.info("开始完成任务: taskId={}, assigneeId={}, outcome={}", taskId, assigneeId, outcome);

        try {
            // 1. 获取任务信息
            WorkflowTaskEntity task = getTask(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskId);
            }

            if (!"ACTIVE".equals(task.getStatus())) {
                throw new RuntimeException("任务状态不正确，无法完成: " + task.getStatus());
            }

            if (!assigneeId.equals(task.getAssigneeId())) {
                throw new RuntimeException("任务未被当前用户认领: " + taskId);
            }

            // 2. 更新任务状态
            task.setStatus("COMPLETED");
            task.setOutcome(outcome);
            task.setComment(comment);
            task.setEndTime(LocalDateTime.now());
            task.setDuration(System.currentTimeMillis()
                    - task.getStartTime().toLocalDate().toEpochDay() * 24 * 60 * 60 * 1000);

            // 3. 更新任务变量和表单数据
            if (variables != null) {
                task.setTaskVariables(convertToJson(variables));
            }
            if (formData != null) {
                task.setFormData(convertToJson(formData));
            }

            workflowTaskDao.updateById(task);

            // 4. 更新流程实例变量
            WorkflowInstanceEntity instance = getInstance(task.getInstanceId());
            if (instance != null && variables != null) {
                updateInstanceVariables(instance, variables);
            }

            // 5. 执行流程流转逻辑
            executeProcessFlow(instance, task, outcome, variables);

            // 6. 清除相关缓存
            clearTaskCache(taskId);
            clearUserTaskCache(assigneeId);

            log.info("任务完成成功: taskId={}", taskId);
            return true;

        } catch (Exception e) {
            log.error("完成任务失败: taskId={}", taskId, e);
            throw new RuntimeException("完成任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取流程定义
     */
    public WorkflowDefinitionEntity getDefinition(Long definitionId) {
        String cacheKey = buildCacheKey(definitionId, ":definition");
        return getCache(cacheKey, () -> workflowDefinitionDao.selectById(definitionId));
    }

    /**
     * 获取流程实例
     */
    public WorkflowInstanceEntity getInstance(Long instanceId) {
        String cacheKey = buildCacheKey(instanceId, ":instance");
        return getCache(cacheKey, () -> workflowInstanceDao.selectById(instanceId));
    }

    /**
     * 获取任务信息
     */
    public WorkflowTaskEntity getTask(Long taskId) {
        String cacheKey = buildCacheKey(taskId, ":task");
        return getCache(cacheKey, () -> workflowTaskDao.selectById(taskId));
    }

    /**
     * 获取用户待办任务列表
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowTaskEntity> getUserTasks(Long userId) {
        String cacheKey = buildCacheKey(userId, ":user:tasks");
        return getCache(cacheKey, () -> {
            LambdaQueryWrapper<WorkflowTaskEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WorkflowTaskEntity::getAssigneeId, userId)
                    .eq(WorkflowTaskEntity::getStatus, "ACTIVE")
                    .orderByAsc(WorkflowTaskEntity::getCreateTime);
            return workflowTaskDao.selectList(wrapper);
        });
    }

    // ========== 私有方法 ==========

    /**
     * 生成实例名称
     */
    private String generateInstanceName(WorkflowDefinitionEntity definition, String businessKey) {
        return definition.getProcessName() + "-" + businessKey + "-" + System.currentTimeMillis();
    }

    /**
     * 创建第一个任务
     */
    private void createFirstTask(WorkflowInstanceEntity instance, Map<String, Object> variables) {
        WorkflowTaskEntity task = new WorkflowTaskEntity();
        task.setInstanceId(instance.getInstanceId());
        task.setTaskName("开始");
        task.setTaskKey("start");
        task.setAssigneeId(instance.getStartUserId());
        task.setStatus("ACTIVE");
        task.setCreateTime(LocalDateTime.now());
        task.setStartTime(LocalDateTime.now());

        if (variables != null) {
            task.setTaskVariables(convertToJson(variables));
        }

        workflowTaskDao.insert(task);
    }

    /**
     * 执行流程流转
     */
    private void executeProcessFlow(WorkflowInstanceEntity instance, WorkflowTaskEntity currentTask,
            String outcome, Map<String, Object> variables) {
        // 简化的流程流转逻辑
        // 实际实现需要根据BPMN定义进行复杂的状态流转

        log.debug("执行流程流转: instanceId={}, taskId={}, outcome={}", instance.getInstanceId(),
                currentTask.getTaskId(), outcome);

        // 检查是否所有任务都已完成
        LambdaQueryWrapper<WorkflowTaskEntity> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(WorkflowTaskEntity::getInstanceId, instance.getInstanceId())
                .eq(WorkflowTaskEntity::getStatus, "ACTIVE");
        List<WorkflowTaskEntity> activeTasks = workflowTaskDao.selectList(activeWrapper);

        if (activeTasks.isEmpty()) {
            // 所有任务完成，结束流程实例
            instance.setStatus("COMPLETED");
            instance.setEndTime(LocalDateTime.now());
            workflowInstanceDao.updateById(instance);

            // 清除实例缓存
            String instanceKey = buildCacheKey(instance.getInstanceId(), ":instance");
            removeCache(instanceKey);
        }
    }

    /**
     * 更新实例变量
     */
    private void updateInstanceVariables(WorkflowInstanceEntity instance,
            Map<String, Object> variables) {
        try {
            // 简化实现，实际需要合并现有变量
            instance.setProcessVariables(convertToJson(variables));
            workflowInstanceDao.updateById(instance);
        } catch (Exception e) {
            log.warn("更新实例变量失败: instanceId={}", instance.getInstanceId(), e);
        }
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(Map<String, Object> data) {
        try {
            return com.alibaba.fastjson.JSON.toJSONString(data);
        } catch (Exception e) {
            log.warn("JSON转换失败", e);
            return "{}";
        }
    }

    /**
     * 清除任务缓存
     */
    private void clearTaskCache(Long taskId) {
        String taskKey = buildCacheKey(taskId, ":task");
        removeCache(taskKey);
    }

    /**
     * 清除用户任务缓存
     */
    private void clearUserTaskCache(Long userId) {
        String userTaskKey = buildCacheKey(userId, ":user:tasks");
        removeCache(userTaskKey);
    }

    /**
     * 获取流程统计信息
     */
    public Map<String, Object> getProcessStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        try {
            // 运行中的实例数
            LambdaQueryWrapper<WorkflowInstanceEntity> runningWrapper = new LambdaQueryWrapper<>();
            runningWrapper.eq(WorkflowInstanceEntity::getStatus, "RUNNING");
            long runningCount = workflowInstanceDao.selectCount(runningWrapper);
            stats.put("runningInstances", runningCount);

            // 今日完成的实例数
            LambdaQueryWrapper<WorkflowInstanceEntity> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.eq(WorkflowInstanceEntity::getStatus, "COMPLETED").ge(
                    WorkflowInstanceEntity::getEndTime,
                    LocalDateTime.now().toLocalDate().atStartOfDay());
            long completedToday = workflowInstanceDao.selectCount(completedWrapper);
            stats.put("completedToday", completedToday);

            // 待处理任务数
            LambdaQueryWrapper<WorkflowTaskEntity> taskCountWrapper = new LambdaQueryWrapper<>();
            taskCountWrapper.eq(WorkflowTaskEntity::getStatus, "ACTIVE");
            long activeTasks = workflowTaskDao.selectCount(taskCountWrapper);
            stats.put("activeTasks", activeTasks);

        } catch (Exception e) {
            log.error("获取流程统计信息失败", e);
        }

        return stats;
    }
}

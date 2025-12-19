package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Flowable任务服务包装器
 * <p>
 * 提供任务管理的完整功能封装
 * 包括任务查询、完成、委派、转办等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class FlowableTaskService {

    private final TaskService taskService;

    public FlowableTaskService(TaskService taskService) {
        this.taskService = taskService;
        log.info("[Flowable] TaskService包装器初始化完成");
    }

    /**
     * 获取任务
     *
     * @param taskId 任务ID
     * @return 任务
     */
    public Task getTask(String taskId) {
        log.debug("[Flowable] 获取任务: taskId={}", taskId);

        try {
            return taskService.createTaskQuery().taskId(taskId).singleResult();

        } catch (Exception e) {
            log.error("[Flowable] 获取任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取我的待办任务
     *
     * @param assignee 处理人
     * @return 任务列表
     */
    public List<Task> getMyTasks(String assignee) {
        log.debug("[Flowable] 获取我的待办任务: assignee={}", assignee);

        try {
            return taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .active()
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取我的待办任务失败: assignee={}, error={}", assignee, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取我的待办任务（分页）
     *
     * @param assignee 处理人
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 任务列表
     */
    public List<Task> getMyTasksPage(String assignee, int pageNum, int pageSize) {
        log.debug("[Flowable] 获取我的待办任务: assignee={}, pageNum={}, pageSize={}",
                assignee, pageNum, pageSize);

        try {
            return taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .active()
                    .orderByTaskCreateTime()
                    .desc()
                    .listPage(pageNum * pageSize, pageSize);

        } catch (Exception e) {
            log.error("[Flowable] 获取我的待办任务失败: assignee={}, error={}", assignee, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取我的可处理任务（包括我的候选任务）
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<Task> getMyCandidateTasks(String userId) {
        log.debug("[Flowable] 获取我的可处理任务: userId={}", userId);

        try {
            return taskService.createTaskQuery()
                    .taskCandidateOrAssigned(userId)
                    .active()
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 获取我的可处理任务失败: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取我的可处理任务（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 任务列表
     */
    public List<Task> getMyCandidateTasksPage(String userId, int pageNum, int pageSize) {
        log.debug("[Flowable] 获取我的可处理任务: userId={}, pageNum={}, pageSize={}",
                userId, pageNum, pageSize);

        try {
            return taskService.createTaskQuery()
                    .taskCandidateOrAssigned(userId)
                    .active()
                    .orderByTaskCreateTime()
                    .desc()
                    .listPage(pageNum * pageSize, pageSize);

        } catch (Exception e) {
            log.error("[Flowable] 获取我的可处理任务失败: userId={}, error={}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取我的待办任务总数
     *
     * @param assignee 处理人
     * @return 总数
     */
    public long getMyTaskCount(String assignee) {
        log.debug("[Flowable] 获取我的待办任务总数: assignee={}", assignee);

        try {
            return taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .active()
                    .count();

        } catch (Exception e) {
            log.error("[Flowable] 获取我的待办任务总数失败: assignee={}, error={}", assignee, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取我的可处理任务总数
     *
     * @param userId 用户ID
     * @return 总数
     */
    public long getMyCandidateTaskCount(String userId) {
        log.debug("[Flowable] 获取我的可处理任务总数: userId={}", userId);

        try {
            return taskService.createTaskQuery()
                    .taskCandidateOrAssigned(userId)
                    .active()
                    .count();

        } catch (Exception e) {
            log.error("[Flowable] 获取我的可处理任务总数失败: userId={}, error={}", userId, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 完成任务
     *
     * @param taskId    任务ID
     * @param variables 流程变量
     * @param comment   处理意见
     */
    public void completeTask(String taskId, Map<String, Object> variables, String comment) {
        log.info("[Flowable] 完成任务: taskId={}, comment={}", taskId, comment);

        try {
            // 添加处理意见
            if (comment != null && !comment.trim().isEmpty()) {
                taskService.addComment(taskId, null, comment);
            }

            // 完成任务
            taskService.complete(taskId, variables);

            log.info("[Flowable] 任务完成成功: taskId={}", taskId);

        } catch (Exception e) {
            log.error("[Flowable] 完成任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("完成任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简单完成任务
     *
     * @param taskId 任务ID
     */
    public void completeTask(String taskId) {
        log.info("[Flowable] 简单完成任务: taskId={}", taskId);

        try {
            taskService.complete(taskId);
            log.info("[Flowable] 任务完成成功: taskId={}", taskId);

        } catch (Exception e) {
            log.error("[Flowable] 完成任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("完成任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 认领任务
     *
     * @param taskId   任务ID
     * @param assignee 处理人
     */
    public void claimTask(String taskId, String assignee) {
        log.info("[Flowable] 认领任务: taskId={}, assignee={}", taskId, assignee);

        try {
            taskService.claim(taskId, assignee);
            log.info("[Flowable] 任务认领成功: taskId={}, assignee={}", taskId, assignee);

        } catch (Exception e) {
            log.error("[Flowable] 认领任务失败: taskId={}, assignee={}, error={}",
                    taskId, assignee, e.getMessage(), e);
            throw new RuntimeException("认领任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 取消认领任务
     *
     * @param taskId 任务ID
     */
    public void unclaimTask(String taskId) {
        log.info("[Flowable] 取消认领任务: taskId={}", taskId);

        try {
            taskService.unclaim(taskId);
            log.info("[Flowable] 任务取消认领成功: taskId={}", taskId);

        } catch (Exception e) {
            log.error("[Flowable] 取消认领任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("取消认领任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 委派任务
     *
     * @param taskId   任务ID
     * @param assignee 处理人
     */
    public void delegateTask(String taskId, String assignee) {
        log.info("[Flowable] 委派任务: taskId={}, assignee={}", taskId, assignee);

        try {
            taskService.delegateTask(taskId, assignee);
            log.info("[Flowable] 任务委派成功: taskId={}, assignee={}", taskId, assignee);

        } catch (Exception e) {
            log.error("[Flowable] 委派任务失败: taskId={}, assignee={}, error={}",
                    taskId, assignee, e.getMessage(), e);
            throw new RuntimeException("委派任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转办任务
     *
     * @param taskId   任务ID
     * @param assignee 处理人
     */
    public void assignTask(String taskId, String assignee) {
        log.info("[Flowable] 转办任务: taskId={}, assignee={}", taskId, assignee);

        try {
            taskService.setAssignee(taskId, assignee);
            log.info("[Flowable] 任务转办成功: taskId={}, assignee={}", taskId, assignee);

        } catch (Exception e) {
            log.error("[Flowable] 转办任务失败: taskId={}, assignee={}, error={}",
                    taskId, assignee, e.getMessage(), e);
            throw new RuntimeException("转办任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加候选用户
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void addCandidateUser(String taskId, String userId) {
        log.info("[Flowable] 添加候选用户: taskId={}, userId={}", taskId, userId);

        try {
            taskService.addCandidateUser(taskId, userId);
            log.info("[Flowable] 候选用户添加成功: taskId={}, userId={}", taskId, userId);

        } catch (Exception e) {
            log.error("[Flowable] 添加候选用户失败: taskId={}, userId={}, error={}",
                    taskId, userId, e.getMessage(), e);
            throw new RuntimeException("添加候选用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加候选组
     *
     * @param taskId 任务ID
     * @param groupId 组ID
     */
    public void addCandidateGroup(String taskId, String groupId) {
        log.info("[Flowable] 添加候选组: taskId={}, groupId={}", taskId, groupId);

        try {
            taskService.addCandidateGroup(taskId, groupId);
            log.info("[Flowable] 候选组添加成功: taskId={}, groupId={}", taskId, groupId);

        } catch (Exception e) {
            log.error("[Flowable] 添加候选组失败: taskId={}, groupId={}, error={}",
                    taskId, groupId, e.getMessage(), e);
            throw new RuntimeException("添加候选组失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除候选用户
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    public void deleteCandidateUser(String taskId, String userId) {
        log.info("[Flowable] 删除候选用户: taskId={}, userId={}", taskId, userId);

        try {
            taskService.deleteCandidateUser(taskId, userId);
            log.info("[Flowable] 候选用户删除成功: taskId={}, userId={}", taskId, userId);

        } catch (Exception e) {
            log.error("[Flowable] 删除候选用户失败: taskId={}, userId={}, error={}",
                    taskId, userId, e.getMessage(), e);
            throw new RuntimeException("删除候选用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除候选组
     *
     * @param taskId 任务ID
     * @param groupId 组ID
     */
    public void deleteCandidateGroup(String taskId, String groupId) {
        log.info("[Flowable] 删除候选组: taskId={}, groupId={}", taskId, groupId);

        try {
            taskService.deleteCandidateGroup(taskId, groupId);
            log.info("[Flowable] 候选组删除成功: taskId={}, groupId={}", taskId, groupId);

        } catch (Exception e) {
            log.error("[Flowable] 删除候选组失败: taskId={}, groupId={}, error={}",
                    taskId, groupId, e.getMessage(), e);
            throw new RuntimeException("删除候选组失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加任务评论
     *
     * @param taskId  任务ID
     * @param comment 评论内容
     */
    public void addTaskComment(String taskId, String comment) {
        log.debug("[Flowable] 添加任务评论: taskId={}", taskId);

        try {
            taskService.addComment(taskId, null, comment);
            log.debug("[Flowable] 任务评论添加成功: taskId={}", taskId);

        } catch (Exception e) {
            log.error("[Flowable] 添加任务评论失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("添加任务评论失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取任务评论
     *
     * @param taskId 任务ID
     * @return 评论列表
     */
    public List<org.flowable.engine.task.Comment> getTaskComments(String taskId) {
        log.debug("[Flowable] 获取任务评论: taskId={}", taskId);

        try {
            return taskService.getTaskComments(taskId);

        } catch (Exception e) {
            log.error("[Flowable] 获取任务评论失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取任务变量
     *
     * @param taskId       任务ID
     * @param variableNames 变量名称
     * @return 变量Map
     */
    public Map<String, Object> getTaskVariables(String taskId, List<String> variableNames) {
        log.debug("[Flowable] 获取任务变量: taskId={}", taskId);

        try {
            return taskService.getVariables(taskId, variableNames);

        } catch (Exception e) {
            log.error("[Flowable] 获取任务变量失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Map.of();
        }
    }

    /**
     * 设置任务变量
     *
     * @param taskId    任务ID
     * @param variables 变量
     */
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        log.debug("[Flowable] 设置任务变量: taskId={}, variables={}",
                taskId, variables != null ? variables.size() : 0);

        try {
            taskService.setVariables(taskId, variables);
            log.debug("[Flowable] 任务变量设置成功: taskId={}", taskId);

        } catch (Exception e) {
            log.error("[Flowable] 设置任务变量失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("设置任务变量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据流程实例ID获取任务列表
     *
     * @param processInstanceId 流程实例ID
     * @return 任务列表
     */
    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        log.debug("[Flowable] 根据流程实例ID获取任务: processInstanceId={}", processInstanceId);

        try {
            return taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .active()
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

        } catch (Exception e) {
            log.error("[Flowable] 根据流程实例ID获取任务失败: processInstanceId={}, error={}",
                    processInstanceId, e.getMessage(), e);
            return List.of();
        }
    }
}

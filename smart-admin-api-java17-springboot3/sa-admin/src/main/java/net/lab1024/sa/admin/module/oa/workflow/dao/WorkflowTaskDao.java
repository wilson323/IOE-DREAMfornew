package net.lab1024.sa.admin.module.oa.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowTaskEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流任务DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface WorkflowTaskDao extends BaseMapper<WorkflowTaskEntity> {

    /**
     * 查询用户的活跃任务
     *
     * @param assigneeId 处理人ID
     * @return 活跃任务列表
     */
    List<WorkflowTaskEntity> selectActiveTasksByAssignee(Long assigneeId);

    /**
     * 查询用户的已完成任务
     *
     * @param assigneeId 处理人ID
     * @param limit 限制数量
     * @return 已完成任务列表
     */
    List<WorkflowTaskEntity> selectCompletedTasksByAssignee(Long assigneeId, Integer limit);

    /**
     * 分页查询任务
     *
     * @param instanceId 实例ID
     * @param assigneeId 处理人ID
     * @param status 状态
     * @param taskType 任务类型
     * @param priority 优先级
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 任务列表
     */
    List<WorkflowTaskEntity> selectTasksByCondition(Long instanceId, Long assigneeId, String status,
                                                   String taskType, Integer priority,
                                                   LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询候选任务
     *
     * @param userId 用户ID
     * @param groupIds 用户组ID列表
     * @return 候选任务列表
     */
    List<WorkflowTaskEntity> selectCandidateTasks(Long userId, List<Long> groupIds);

    /**
     * 查询实例的所有任务
     *
     * @param instanceId 实例ID
     * @return 任务列表
     */
    List<WorkflowTaskEntity> selectByInstanceId(Long instanceId);

    /**
     * 查询超期任务
     *
     * @param currentTime 当前时间
     * @return 超期任务列表
     */
    List<WorkflowTaskEntity> selectOverdueTasks(LocalDateTime currentTime);

    /**
     * 查询即将到期的任务
     *
     * @param currentTime 当前时间
     * @param hours 小时数
     * @return 即将到期任务列表
     */
    List<WorkflowTaskEntity> selectDueSoonTasks(LocalDateTime currentTime, Integer hours);

    /**
     * 统计用户任务数量
     *
     * @param userId 用户ID
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 任务数量
     */
    Integer countUserTasks(Long userId, String status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计任务处理情况
     *
     * @param definitionId 定义ID(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 处理情况统计
     */
    Map<String, Object> getTaskStatistics(Long definitionId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计部门工作量
     *
     * @param deptId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工作量统计
     */
    Map<String, Object> getDeptWorkloadStatistics(Long deptId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询任务历史
     *
     * @param instanceId 实例ID
     * @return 任务历史
     */
    List<Map<String, Object>> selectTaskHistory(Long instanceId);

    /**
     * 批量更新任务状态
     *
     * @param taskIds 任务ID列表
     * @param status 新状态
     * @return 更新数量
     */
    Integer batchUpdateTaskStatus(List<Long> taskIds, String status);

    /**
     * 删除实例的所有任务
     *
     * @param instanceId 实例ID
     * @return 删除数量
     */
    Integer deleteByInstanceId(Long instanceId);

    /**
     * 查询指定处理人的任务数量
     *
     * @param assigneeId 处理人ID
     * @param status 状态
     * @return 任务数量
     */
    Integer countByAssigneeAndStatus(Long assigneeId, String status);

    /**
     * 查询流程定义的任务统计
     *
     * @param definitionId 定义ID
     * @return 任务统计
     */
    Map<String, Integer> getTaskStatsByDefinition(Long definitionId);
}
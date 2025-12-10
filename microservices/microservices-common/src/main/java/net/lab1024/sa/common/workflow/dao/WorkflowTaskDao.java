package net.lab1024.sa.common.workflow.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.workflow.entity.WorkflowTaskEntity;

/**
 * 工作流任务DAO接口
 * <p>
 * 严格遵循四层架构规范：
 * - 使用@Mapper注解，禁止使用@Repository注解
 * - 继承BaseMapper提供基础CRUD功能
 * - 只负责数据访问，不包含业务逻辑
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 * @version 3.0.0
 */
@Mapper
public interface WorkflowTaskDao extends BaseMapper<WorkflowTaskEntity> {

    /**
     * 分页查询我的待办任务
     *
     * @param page      分页对象
     * @param userId    用户ID
     * @param category  流程分类
     * @param priority  优先级
     * @param dueStatus 截止状态（OVERDUE-已过期, DUE_SOON-即将到期, NORMAL-正常）
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT t.* FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status IN (1, 2) " +
            "AND (t.assignee_id = #{userId} " +
            "OR JSON_CONTAINS(t.candidate_users, CAST(#{userId} AS JSON)) " +
            "OR EXISTS (SELECT 1 FROM t_common_user_role ur " +
            "INNER JOIN t_common_role r ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{userId} AND JSON_CONTAINS(t.candidate_groups, CAST(r.role_code AS JSON)))) " +
            "<if test='category != null and category != \"\"'>AND i.process_key LIKE CONCAT(#{category}, '%')</if> " +
            "<if test='priority != null'>AND t.priority = #{priority}</if> " +
            "<if test='dueStatus == \"OVERDUE\"'>AND t.due_time IS NOT NULL AND t.due_time &lt; NOW()</if> " +
            "<if test='dueStatus == \"DUE_SOON\"'>AND t.due_time IS NOT NULL AND t.due_time &gt; NOW() AND t.due_time &lt;= DATE_ADD(NOW(), INTERVAL 24 HOUR)</if> " +
            "ORDER BY t.priority DESC, t.task_create_time ASC" +
            "</script>")
    IPage<WorkflowTaskEntity> selectMyTasksPage(
            Page<WorkflowTaskEntity> page,
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("priority") Integer priority,
            @Param("dueStatus") String dueStatus
    );

    /**
     * 分页查询我的已办任务
     *
     * @param page      分页对象
     * @param userId    用户ID
     * @param category  流程分类
     * @param outcome   处理结果
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT t.* FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status = 3 " +
            "AND (t.assignee_id = #{userId} OR t.original_assignee_id = #{userId}) " +
            "<if test='category != null and category != \"\"'>AND i.process_key LIKE CONCAT(#{category}, '%')</if> " +
            "<if test='outcome != null'>AND t.result = #{outcome}</if> " +
            "<if test='startDate != null and startDate != \"\"'>AND DATE(t.end_time) &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'>AND DATE(t.end_time) &lt;= #{endDate}</if> " +
            "ORDER BY t.end_time DESC" +
            "</script>")
    IPage<WorkflowTaskEntity> selectMyCompletedTasksPage(
            Page<WorkflowTaskEntity> page,
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("outcome") String outcome,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 受理任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_task SET " +
            "assignee_id = #{userId}, " +
            "status = 2, " +
            "start_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE task_id = #{taskId} AND status = 1 " +
            "AND (assignee_id IS NULL OR assignee_id = #{userId} " +
            "OR JSON_CONTAINS(candidate_users, CAST(#{userId} AS JSON)))")
    int claimTask(@Param("taskId") Long taskId, @Param("userId") Long userId);

    /**
     * 取消受理任务
     *
     * @param taskId 任务ID
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_task SET " +
            "assignee_id = NULL, " +
            "status = 1, " +
            "start_time = NULL, " +
            "update_time = NOW() " +
            "WHERE task_id = #{taskId} AND status = 2")
    int unclaimTask(@Param("taskId") Long taskId);

    /**
     * 委派任务
     *
     * @param taskId        任务ID
     * @param targetUserId  目标用户ID
     * @param originalUserId 原用户ID
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_task SET " +
            "original_assignee_id = #{originalUserId}, " +
            "original_assignee_name = (SELECT user_name FROM t_common_user WHERE user_id = #{originalUserId}), " +
            "assignee_id = #{targetUserId}, " +
            "assignee_name = (SELECT user_name FROM t_common_user WHERE user_id = #{targetUserId}), " +
            "status = 5, " +
            "result = 4, " +
            "delegate_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE task_id = #{taskId} AND status IN (1, 2)")
    int delegateTask(@Param("taskId") Long taskId,
            @Param("targetUserId") Long targetUserId,
            @Param("originalUserId") Long originalUserId);

    /**
     * 转交任务
     *
     * @param taskId       任务ID
     * @param targetUserId 目标用户ID
     * @param originalUserId 原用户ID
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_task SET " +
            "original_assignee_id = #{originalUserId}, " +
            "original_assignee_name = (SELECT user_name FROM t_common_user WHERE user_id = #{originalUserId}), " +
            "assignee_id = #{targetUserId}, " +
            "assignee_name = (SELECT user_name FROM t_common_user WHERE user_id = #{targetUserId}), " +
            "status = 4, " +
            "result = 3, " +
            "delegate_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE task_id = #{taskId} AND status IN (1, 2)")
    int transferTask(@Param("taskId") Long taskId,
            @Param("targetUserId") Long targetUserId,
            @Param("originalUserId") Long originalUserId);

    /**
     * 完成任务
     *
     * @param taskId  任务ID
     * @param outcome 处理结果（1-同意）
     * @param comment 处理意见
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_task SET " +
            "status = 3, " +
            "result = #{outcome}, " +
            "comment = #{comment}, " +
            "end_time = NOW(), " +
            "duration = CASE " +
            "WHEN start_time IS NOT NULL THEN TIMESTAMPDIFF(MICROSECOND, start_time, NOW()) / 1000 " +
            "ELSE TIMESTAMPDIFF(MICROSECOND, task_create_time, NOW()) / 1000 END, " +
            "update_time = NOW() " +
            "WHERE task_id = #{taskId} AND status IN (1, 2)")
    int completeTask(@Param("taskId") Long taskId,
            @Param("outcome") Integer outcome,
            @Param("comment") String comment);

    /**
     * 驳回任务
     *
     * @param taskId  任务ID
     * @param comment 驳回意见
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_task SET " +
            "status = 6, " +
            "result = 2, " +
            "comment = #{comment}, " +
            "end_time = NOW(), " +
            "duration = CASE " +
            "WHEN start_time IS NOT NULL THEN TIMESTAMPDIFF(MICROSECOND, start_time, NOW()) / 1000 " +
            "ELSE TIMESTAMPDIFF(MICROSECOND, task_create_time, NOW()) / 1000 END, " +
            "update_time = NOW() " +
            "WHERE task_id = #{taskId} AND status IN (1, 2)")
    int rejectTask(@Param("taskId") Long taskId, @Param("comment") String comment);

    /**
     * 查询用户工作量统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT " +
            "COUNT(1) as totalTasks, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as pendingTasks, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as processingTasks, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as completedTasks, " +
            "AVG(duration) as avgDuration, " +
            "SUM(CASE WHEN due_time IS NOT NULL AND due_time &lt; end_time THEN 1 ELSE 0 END) as overdueCount " +
            "FROM t_common_workflow_task " +
            "WHERE deleted_flag = 0 " +
            "AND (assignee_id = #{userId} OR original_assignee_id = #{userId}) " +
            "<if test='startDate != null and startDate != \"\"'>AND DATE(task_create_time) &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'>AND DATE(task_create_time) &lt;= #{endDate}</if>" +
            "</script>")
    List<Object> selectUserWorkloadStatistics(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 查询超时任务
     * <p>
     * 查询截止时间小于等于指定时间的待处理任务
     * 用于审批超时提醒和处理
     * </p>
     *
     * @param deadline 截止时间
     * @return 超时任务列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT t.*, i.process_name " +
            "FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status IN (1, 2) " +
            "AND t.due_time IS NOT NULL " +
            "AND t.due_time <= #{deadline} " +
            "ORDER BY t.due_time ASC")
    List<WorkflowTaskEntity> selectTimeoutTasks(@Param("deadline") LocalDateTime deadline);

    /**
     * 查询任务统计信息
     * <p>
     * 统计指定时间范围内的任务数量（总数、待处理、已完成）
     * </p>
     *
     * @param instanceId 流程实例ID（可选）
     * @param definitionId 流程定义ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计结果Map，包含total、pending、completed等字段
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT " +
            "COUNT(1) as total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as pending, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as completed " +
            "FROM t_common_workflow_task " +
            "WHERE deleted_flag = 0 " +
            "<if test='instanceId != null'>AND instance_id = #{instanceId}</if> " +
            "<if test='definitionId != null'>AND instance_id IN (SELECT instance_id FROM t_common_workflow_instance WHERE definition_id = #{definitionId} AND deleted_flag = 0)</if> " +
            "<if test='startDate != null and startDate != \"\"'>AND DATE(task_create_time) &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'>AND DATE(task_create_time) &lt;= #{endDate}</if>" +
            "</script>")
    Map<String, Object> selectTaskStatistics(
            @Param("instanceId") Long instanceId,
            @Param("definitionId") Long definitionId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}

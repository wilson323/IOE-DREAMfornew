package net.lab1024.sa.common.workflow.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.common.workflow.domain.form.ApprovalTaskQueryForm;
import net.lab1024.sa.common.workflow.entity.WorkflowTaskEntity;

/**
 * 审批任务DAO接口（别名）
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解，禁止使用@Repository注解
 * - 继承WorkflowTaskDao提供基础CRUD功能
 * - 只负责数据访问，不包含业务逻辑
 * </p>
 * <p>
 * 注意：此接口是WorkflowTaskDao的别名，用于审批模块
 * 实际实现使用WorkflowTaskDao
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Mapper
public interface ApprovalTaskDao extends WorkflowTaskDao {

    /**
     * 查询待办任务列表
     *
     * @param queryForm 查询条件
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT t.* FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status IN (1, 2) " +
            "AND (t.assignee_id = #{queryForm.userId} " +
            "OR JSON_CONTAINS(t.candidate_users, CAST(#{queryForm.userId} AS JSON)) " +
            "OR EXISTS (SELECT 1 FROM t_common_user_role ur " +
            "INNER JOIN t_common_role r ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{queryForm.userId} AND JSON_CONTAINS(t.candidate_groups, CAST(r.role_code AS JSON)))) " +
            "<if test='queryForm.businessType != null and queryForm.businessType != \"\"'>AND i.business_type = #{queryForm.businessType}</if> " +
            "<if test='queryForm.priority != null'>AND t.priority = #{queryForm.priority}</if> " +
            "ORDER BY t.priority DESC, t.task_create_time ASC " +
            "LIMIT #{queryForm.pageSize} OFFSET (#{queryForm.pageNum} - 1) * #{queryForm.pageSize}" +
            "</script>")
    List<WorkflowTaskEntity> selectTodoTasks(@Param("queryForm") ApprovalTaskQueryForm queryForm);

    /**
     * 统计待办任务数量
     *
     * @param queryForm 查询条件
     * @return 任务数量
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT COUNT(1) FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status IN (1, 2) " +
            "AND (t.assignee_id = #{queryForm.userId} " +
            "OR JSON_CONTAINS(t.candidate_users, CAST(#{queryForm.userId} AS JSON)) " +
            "OR EXISTS (SELECT 1 FROM t_common_user_role ur " +
            "INNER JOIN t_common_role r ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{queryForm.userId} AND JSON_CONTAINS(t.candidate_groups, CAST(r.role_code AS JSON)))) " +
            "<if test='queryForm.businessType != null and queryForm.businessType != \"\"'>AND i.business_type = #{queryForm.businessType}</if> " +
            "<if test='queryForm.priority != null'>AND t.priority = #{queryForm.priority}</if>" +
            "</script>")
    Long countTodoTasks(@Param("queryForm") ApprovalTaskQueryForm queryForm);

    /**
     * 查询已办任务列表
     *
     * @param queryForm 查询条件
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT t.* FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status = 3 " +
            "AND (t.assignee_id = #{queryForm.userId} OR t.original_assignee_id = #{queryForm.userId}) " +
            "<if test='queryForm.businessType != null and queryForm.businessType != \"\"'>AND i.business_type = #{queryForm.businessType}</if> " +
            "<if test='queryForm.startDate != null and queryForm.startDate != \"\"'>AND DATE(t.end_time) &gt;= #{queryForm.startDate}</if> " +
            "<if test='queryForm.endDate != null and queryForm.endDate != \"\"'>AND DATE(t.end_time) &lt;= #{queryForm.endDate}</if> " +
            "ORDER BY t.end_time DESC " +
            "LIMIT #{queryForm.pageSize} OFFSET (#{queryForm.pageNum} - 1) * #{queryForm.pageSize}" +
            "</script>")
    List<WorkflowTaskEntity> selectCompletedTasks(@Param("queryForm") ApprovalTaskQueryForm queryForm);

    /**
     * 统计已办任务数量
     *
     * @param queryForm 查询条件
     * @return 任务数量
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT COUNT(1) FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND t.status = 3 " +
            "AND (t.assignee_id = #{queryForm.userId} OR t.original_assignee_id = #{queryForm.userId}) " +
            "<if test='queryForm.businessType != null and queryForm.businessType != \"\"'>AND i.business_type = #{queryForm.businessType}</if> " +
            "<if test='queryForm.startDate != null and queryForm.startDate != \"\"'>AND DATE(t.end_time) &gt;= #{queryForm.startDate}</if> " +
            "<if test='queryForm.endDate != null and queryForm.endDate != \"\"'>AND DATE(t.end_time) &lt;= #{queryForm.endDate}</if>" +
            "</script>")
    Long countCompletedTasks(@Param("queryForm") ApprovalTaskQueryForm queryForm);

    /**
     * 查询我的申请任务列表
     *
     * @param queryForm 查询条件
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT t.* FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND i.initiator_id = #{queryForm.applicantId} " +
            "<if test='queryForm.businessType != null and queryForm.businessType != \"\"'>AND i.business_type = #{queryForm.businessType}</if> " +
            "<if test='queryForm.startDate != null and queryForm.startDate != \"\"'>AND DATE(i.start_time) &gt;= #{queryForm.startDate}</if> " +
            "<if test='queryForm.endDate != null and queryForm.endDate != \"\"'>AND DATE(i.start_time) &lt;= #{queryForm.endDate}</if> " +
            "ORDER BY i.start_time DESC " +
            "LIMIT #{queryForm.pageSize} OFFSET (#{queryForm.pageNum} - 1) * #{queryForm.pageSize}" +
            "</script>")
    List<WorkflowTaskEntity> selectMyApplications(@Param("queryForm") ApprovalTaskQueryForm queryForm);

    /**
     * 统计我的申请任务数量
     *
     * @param queryForm 查询条件
     * @return 任务数量
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT COUNT(1) FROM t_common_workflow_task t " +
            "INNER JOIN t_common_workflow_instance i ON t.instance_id = i.instance_id " +
            "WHERE t.deleted_flag = 0 AND i.deleted_flag = 0 " +
            "AND i.initiator_id = #{queryForm.applicantId} " +
            "<if test='queryForm.businessType != null and queryForm.businessType != \"\"'>AND i.business_type = #{queryForm.businessType}</if> " +
            "<if test='queryForm.startDate != null and queryForm.startDate != \"\"'>AND DATE(i.start_time) &gt;= #{queryForm.startDate}</if> " +
            "<if test='queryForm.endDate != null and queryForm.endDate != \"\"'>AND DATE(i.start_time) &lt;= #{queryForm.endDate}</if>" +
            "</script>")
    Long countMyApplications(@Param("queryForm") ApprovalTaskQueryForm queryForm);

    /**
     * 查询流程实例的待处理任务
     *
     * @param instanceId 流程实例ID
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_workflow_task " +
            "WHERE deleted_flag = 0 AND instance_id = #{instanceId} " +
            "AND status IN (1, 2) " +
            "ORDER BY task_create_time ASC")
    List<WorkflowTaskEntity> selectPendingTasksByInstance(@Param("instanceId") Long instanceId);
}


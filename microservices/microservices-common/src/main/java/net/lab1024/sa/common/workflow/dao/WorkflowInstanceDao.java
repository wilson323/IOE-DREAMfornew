package net.lab1024.sa.common.workflow.dao;

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
import net.lab1024.sa.common.workflow.entity.WorkflowInstanceEntity;

/**
 * 工作流实例DAO接口
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
public interface WorkflowInstanceDao extends BaseMapper<WorkflowInstanceEntity> {

    /**
     * 分页查询流程实例
     *
     * @param page           分页对象
     * @param definitionId   流程定义ID
     * @param status         状态
     * @param startUserId    发起人ID
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT * FROM t_common_workflow_instance " +
            "WHERE deleted_flag = 0 " +
            "<if test='definitionId != null'>AND process_definition_id = #{definitionId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='startUserId != null'>AND initiator_id = #{startUserId}</if> " +
            "<if test='startDate != null and startDate != \"\"'>AND DATE(start_time) &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'>AND DATE(start_time) &lt;= #{endDate}</if> " +
            "ORDER BY start_time DESC" +
            "</script>")
    IPage<WorkflowInstanceEntity> selectInstancePage(
            Page<WorkflowInstanceEntity> page,
            @Param("definitionId") Long definitionId,
            @Param("status") Integer status,
            @Param("startUserId") Long startUserId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 挂起流程实例
     *
     * @param instanceId 实例ID
     * @param reason     原因
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_instance SET " +
            "status = 4, " +
            "suspend_time = NOW(), " +
            "reason = #{reason}, " +
            "update_time = NOW() " +
            "WHERE instance_id = #{instanceId} AND status = 1")
    int suspendInstance(@Param("instanceId") Long instanceId, @Param("reason") String reason);

    /**
     * 激活流程实例
     *
     * @param instanceId 实例ID
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_instance SET " +
            "status = 1, " +
            "suspend_time = NULL, " +
            "reason = NULL, " +
            "update_time = NOW() " +
            "WHERE instance_id = #{instanceId} AND status = 4")
    int activateInstance(@Param("instanceId") Long instanceId);

    /**
     * 终止流程实例
     *
     * @param instanceId 实例ID
     * @param reason     原因
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_instance SET " +
            "status = 3, " +
            "end_time = NOW(), " +
            "reason = #{reason}, " +
            "duration = TIMESTAMPDIFF(MICROSECOND, start_time, NOW()) / 1000, " +
            "update_time = NOW() " +
            "WHERE instance_id = #{instanceId} AND status IN (1, 4)")
    int terminateInstance(@Param("instanceId") Long instanceId, @Param("reason") String reason);

    /**
     * 撤销流程实例
     *
     * @param instanceId 实例ID
     * @param reason     原因
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_instance SET " +
            "status = 3, " +
            "end_time = NOW(), " +
            "reason = #{reason}, " +
            "duration = TIMESTAMPDIFF(MICROSECOND, start_time, NOW()) / 1000, " +
            "update_time = NOW() " +
            "WHERE instance_id = #{instanceId} AND status = 1")
    int revokeInstance(@Param("instanceId") Long instanceId, @Param("reason") String reason);

    /**
     * 更新当前节点
     *
     * @param instanceId    实例ID
     * @param currentNodeId 当前节点ID
     * @param nodeName      节点名称
     * @return 更新数量
     */
    @Update("UPDATE t_common_workflow_instance SET " +
            "current_node_id = #{currentNodeId}, " +
            "current_node_name = #{nodeName}, " +
            "update_time = NOW() " +
            "WHERE instance_id = #{instanceId}")
    int updateCurrentNode(@Param("instanceId") Long instanceId,
            @Param("currentNodeId") String currentNodeId,
            @Param("nodeName") String nodeName);

    /**
     * 查询流程历史记录
     *
     * @param instanceId 实例ID
     * @return 历史记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT " +
            "task_id as id, " +
            "task_name as name, " +
            "assignee_name as assignee, " +
            "status, " +
            "comment, " +
            "start_time, " +
            "end_time, " +
            "duration " +
            "FROM t_common_workflow_task " +
            "WHERE instance_id = #{instanceId} AND deleted_flag = 0 " +
            "ORDER BY task_create_time ASC")
    List<Map<String, Object>> selectProcessHistory(@Param("instanceId") Long instanceId);

    /**
     * 统计流程实例数量
     *
     * @param definitionId 流程定义ID
     * @param status       状态
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT " +
            "COUNT(1) as total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as running, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completed, " +
            "SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as terminated, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as suspended, " +
            "AVG(duration) as avgDuration " +
            "FROM t_common_workflow_instance " +
            "WHERE deleted_flag = 0 " +
            "<if test='definitionId != null'>AND process_definition_id = #{definitionId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='startDate != null and startDate != \"\"'>AND DATE(start_time) &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'>AND DATE(start_time) &lt;= #{endDate}</if>" +
            "</script>")
    Map<String, Object> selectProcessStatistics(
            @Param("definitionId") Long definitionId,
            @Param("status") Integer status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}

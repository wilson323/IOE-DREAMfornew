package net.lab1024.sa.oa.workflow.form;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 表单实例数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Mapper
public interface FormInstanceRepository extends BaseMapper<FormInstanceEntity> {

    /**
     * 根据条件查询表单实例
     *
     * @param processInstanceId 流程实例ID
     * @param taskId 任务ID
     * @param submitterId 提交者ID
     * @return 表单实例列表
     */
    @Select("<script>" +
            "SELECT * FROM t_workflow_form_instance WHERE deleted_flag = 0 " +
            "<if test='processInstanceId != null and processInstanceId != \"\"'>" +
            "AND process_instance_id = #{processInstanceId} " +
            "</if>" +
            "<if test='taskId != null and taskId != \"\"'>" +
            "AND task_id = #{taskId} " +
            "</if>" +
            "<if test='submitterId != null'>" +
            "AND submitter_id = #{submitterId} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<FormInstanceEntity> findByConditions(@Param("processInstanceId") String processInstanceId,
                                               @Param("taskId") String taskId,
                                               @Param("submitterId") Long submitterId);

    /**
     * 根据流程实例ID查询表单实例
     *
     * @param processInstanceId 流程实例ID
     * @return 表单实例列表
     */
    @Select("SELECT * FROM t_workflow_form_instance " +
            "WHERE process_instance_id = #{processInstanceId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<FormInstanceEntity> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 根据任务ID查询表单实例
     *
     * @param taskId 任务ID
     * @return 表单实例
     */
    @Select("SELECT * FROM t_workflow_form_instance " +
            "WHERE task_id = #{taskId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT 1")
    FormInstanceEntity findByTaskId(@Param("taskId") String taskId);
}

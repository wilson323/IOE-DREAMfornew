package net.lab1024.sa.oa.workflow.designer.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.oa.workflow.designer.entity.ProcessTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程模板数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Mapper
public interface ProcessTemplateDao extends BaseMapper<ProcessTemplateEntity> {

    @Select("SELECT * FROM t_workflow_process_template " +
            "WHERE template_key = #{templateKey} AND deleted_flag = 0 " +
            "ORDER BY version DESC LIMIT 1")
    ProcessTemplateEntity findLatestByKey(@Param("templateKey") String templateKey);

    @Select("SELECT COALESCE(MAX(version), 0) FROM t_workflow_process_template " +
            "WHERE template_key = #{templateKey} AND deleted_flag = 0")
    int findLatestVersionByKey(@Param("templateKey") String templateKey);

    @Select("SELECT t1.* FROM t_workflow_process_template t1 " +
            "INNER JOIN (SELECT template_key, MAX(version) as max_version " +
            "FROM t_workflow_process_template WHERE deleted_flag = 0 " +
            "GROUP BY template_key) t2 " +
            "ON t1.template_key = t2.template_key AND t1.version = t2.max_version " +
            "WHERE t1.deleted_flag = 0 AND t1.status = 1 " +
            "ORDER BY t1.update_time DESC")
    List<ProcessTemplateEntity> findLatestVersions();

    @Select("SELECT t1.* FROM t_workflow_process_template t1 " +
            "INNER JOIN (SELECT template_key, MAX(version) as max_version " +
            "FROM t_workflow_process_template WHERE deleted_flag = 0 " +
            "GROUP BY template_key) t2 " +
            "ON t1.template_key = t2.template_key AND t1.version = t2.max_version " +
            "WHERE t1.deleted_flag = 0 AND t1.status = 1 AND t1.category = #{category} " +
            "ORDER BY t1.update_time DESC")
    List<ProcessTemplateEntity> findByCategory(@Param("category") String category);
}

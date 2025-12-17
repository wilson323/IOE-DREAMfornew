package net.lab1024.sa.oa.workflow.form;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 表单Schema定义数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Mapper
public interface FormSchemaRepository extends BaseMapper<FormSchemaEntity> {

    /**
     * 根据表单键查找最新版本
     *
     * @param formKey 表单键
     * @return 最新版本的表单定义
     */
    @Select("SELECT * FROM t_workflow_form_definition " +
            "WHERE form_key = #{formKey} AND deleted_flag = 0 " +
            "ORDER BY version DESC LIMIT 1")
    FormSchemaEntity findLatestByKey(@Param("formKey") String formKey);

    /**
     * 获取表单键的最新版本号
     *
     * @param formKey 表单键
     * @return 最新版本号，若不存在返回0
     */
    @Select("SELECT COALESCE(MAX(version), 0) FROM t_workflow_form_definition " +
            "WHERE form_key = #{formKey} AND deleted_flag = 0")
    int findLatestVersionByKey(@Param("formKey") String formKey);

    /**
     * 获取所有表单的最新版本
     *
     * @return 表单定义列表
     */
    @Select("SELECT t1.* FROM t_workflow_form_definition t1 " +
            "INNER JOIN (SELECT form_key, MAX(version) as max_version " +
            "FROM t_workflow_form_definition WHERE deleted_flag = 0 " +
            "GROUP BY form_key) t2 " +
            "ON t1.form_key = t2.form_key AND t1.version = t2.max_version " +
            "WHERE t1.deleted_flag = 0 AND t1.status = 1 " +
            "ORDER BY t1.update_time DESC")
    List<FormSchemaEntity> findLatestVersions();

    /**
     * 根据分类获取表单定义
     *
     * @param category 分类
     * @return 表单定义列表
     */
    @Select("SELECT t1.* FROM t_workflow_form_definition t1 " +
            "INNER JOIN (SELECT form_key, MAX(version) as max_version " +
            "FROM t_workflow_form_definition WHERE deleted_flag = 0 " +
            "GROUP BY form_key) t2 " +
            "ON t1.form_key = t2.form_key AND t1.version = t2.max_version " +
            "WHERE t1.deleted_flag = 0 AND t1.status = 1 AND t1.category = #{category} " +
            "ORDER BY t1.update_time DESC")
    List<FormSchemaEntity> findByCategory(@Param("category") String category);
}

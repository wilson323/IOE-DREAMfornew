package net.lab1024.sa.oa.workflow.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.oa.workflow.entity.WorkflowDefinitionEntity;

/**
 * 工作流定义DAO接口
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
public interface WorkflowDefinitionDao extends BaseMapper<WorkflowDefinitionEntity> {

    /**
     * 分页查询工作流定义列表
     *
     * @param page      分页对象
     * @param category  流程分类
     * @param status    发布状态
     * @param keyword   关键词
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT * FROM t_common_workflow_definition " +
            "WHERE deleted_flag = 0 " +
            "<if test='category != null and category != \"\"'>AND category = #{category}</if> " +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (process_key LIKE CONCAT('%', #{keyword}, '%') " +
            "OR process_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<WorkflowDefinitionEntity> selectDefinitionPage(
            Page<WorkflowDefinitionEntity> page,
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    /**
     * 根据流程编码查询最新版本定义
     *
     * @param processKey 流程编码
     * @return 工作流定义
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_workflow_definition " +
            "WHERE process_key = #{processKey} AND is_latest = 1 AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT 1")
    WorkflowDefinitionEntity selectLatestByKey(@Param("processKey") String processKey);

    /**
     * 更新流程实例数量
     *
     * @param definitionId 定义ID
     * @param increment    增量
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_common_workflow_definition SET " +
            "instance_count = COALESCE(instance_count, 0) + #{increment}, " +
            "update_time = NOW() " +
            "WHERE id = #{definitionId}")
    int updateInstanceCount(@Param("definitionId") Long definitionId, @Param("increment") Integer increment);

    /**
     * 更新最后部署时间
     *
     * @param definitionId 定义ID
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_common_workflow_definition SET " +
            "last_deploy_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE id = #{definitionId}")
    int updateLastDeployTime(@Param("definitionId") Long definitionId);

    /**
     * 发布工作流定义（激活）
     *
     * @param definitionId 定义ID
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_common_workflow_definition SET " +
            "status = 'PUBLISHED', " +
            "effective_time = NOW(), " +
            "update_time = NOW() " +
            "WHERE id = #{definitionId}")
    int activateDefinition(@Param("definitionId") Long definitionId);

    /**
     * 禁用工作流定义
     *
     * @param definitionId 定义ID
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_common_workflow_definition SET " +
            "status = 'DISABLED', " +
            "update_time = NOW() " +
            "WHERE id = #{definitionId}")
    int disableDefinition(@Param("definitionId") Long definitionId);

    /**
     * 检查流程编码是否存在
     *
     * @param processKey 流程编码
     * @param excludeId 排除的定义ID
     * @return 存在数量
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT COUNT(1) FROM t_common_workflow_definition " +
            "WHERE process_key = #{processKey} AND deleted_flag = 0 " +
            "<if test='excludeId != null'>AND id != #{excludeId}</if>" +
            "</script>")
    int countByProcessKey(@Param("processKey") String processKey, @Param("excludeId") Long excludeId);

    /**
     * 按流程Key和状态统计数量
     *
     * @param processKey 流程编码
     * @param status     状态
     * @return 数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(1) FROM t_common_workflow_definition " +
            "WHERE process_key = #{processKey} AND status = #{status} AND deleted_flag = 0")
    int countByProcessKey(@Param("processKey") String processKey, @Param("status") String status);

    /**
     * 更新最新版本状态
     *
     * @param processKey 流程编码
     * @param isLatest   是否最新
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_common_workflow_definition SET " +
            "is_latest = #{isLatest}, " +
            "update_time = NOW() " +
            "WHERE process_key = #{processKey} AND deleted_flag = 0")
    int updateLatestStatus(@Param("processKey") String processKey, @Param("isLatest") int isLatest);

    /**
     * 按状态统计流程定义数量
     * <p>
     * 状态说明：
     * DRAFT-草稿 PUBLISHED-已发布 DISABLED-已禁用 ACTIVE-活跃
     * </p>
     *
     * @param status 定义状态
     * @return 统计数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(1) FROM t_common_workflow_definition WHERE status = #{status} AND deleted_flag = 0")
    Long countByStatus(@Param("status") String status);
}





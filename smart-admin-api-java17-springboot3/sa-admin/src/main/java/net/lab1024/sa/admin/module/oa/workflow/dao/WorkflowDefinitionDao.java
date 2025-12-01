package net.lab1024.sa.admin.module.oa.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowDefinitionEntity;

import java.util.List;

/**
 * 工作流定义DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface WorkflowDefinitionDao extends BaseMapper<WorkflowDefinitionEntity> {

    /**
     * 查询指定Key的最新版本定义
     *
     * @param processKey 流程Key
     * @return 最新版本定义
     */
    WorkflowDefinitionEntity selectLatestByKey(String processKey);

    /**
     * 分页查询流程定义
     *
     * @param processName 流程名称
     * @param processKey 流程Key
     * @param category 分类
     * @param status 状态
     * @param keyword 关键词
     * @return 定义列表
     */
    List<WorkflowDefinitionEntity> selectDefinitionsByCondition(String processName, String processKey,
                                                               String category, String status, String keyword);

    /**
     * 查询所有发布状态的定义
     *
     * @return 发布的定义列表
     */
    List<WorkflowDefinitionEntity> selectPublishedDefinitions();

    /**
     * 根据分类查询定义
     *
     * @param category 分类
     * @return 定义列表
     */
    List<WorkflowDefinitionEntity> selectByCategory(String category);

    /**
     * 查询指定定义的所有版本
     *
     * @param processKey 流程Key
     * @return 版本列表
     */
    List<WorkflowDefinitionEntity> selectVersionsByKey(String processKey);

    /**
     * 统计分类下的定义数量
     *
     * @param category 分类
     * @return 数量
     */
    Integer countByCategory(String category);
}
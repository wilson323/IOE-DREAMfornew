package net.lab1024.sa.common.workflow.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.workflow.entity.ApprovalNodeConfigEntity;

/**
 * 审批节点配置DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalNodeConfigDao extends BaseMapper<ApprovalNodeConfigEntity> {

    /**
     * 根据审批配置ID查询所有节点配置
     * <p>
     * 按节点顺序排序
     * </p>
     *
     * @param approvalConfigId 审批配置ID
     * @return 节点配置列表
     */
    List<ApprovalNodeConfigEntity> selectByApprovalConfigId(@Param("approvalConfigId") Long approvalConfigId);

    /**
     * 根据审批配置ID和节点顺序查询节点配置
     *
     * @param approvalConfigId 审批配置ID
     * @param nodeOrder 节点顺序
     * @return 节点配置实体
     */
    ApprovalNodeConfigEntity selectByApprovalConfigIdAndOrder(
            @Param("approvalConfigId") Long approvalConfigId,
            @Param("nodeOrder") Integer nodeOrder);

    /**
     * 删除审批配置的所有节点配置
     *
     * @param approvalConfigId 审批配置ID
     * @return 删除数量
     */
    int deleteByApprovalConfigId(@Param("approvalConfigId") Long approvalConfigId);
}


package net.lab1024.sa.oa.workflow.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.oa.workflow.entity.ApprovalNodeConfigEntity;

/**
 * 审批节点配置DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ApprovalNodeConfigDao extends BaseMapper<ApprovalNodeConfigEntity> {

    /**
     * 根据审批配置ID查询所有节点配置
     */
    List<ApprovalNodeConfigEntity> selectByApprovalConfigId(@Param("approvalConfigId") Long approvalConfigId);

    /**
     * 根据审批配置ID和节点顺序查询节点配置
     */
    ApprovalNodeConfigEntity selectByApprovalConfigIdAndOrder(
            @Param("approvalConfigId") Long approvalConfigId,
            @Param("nodeOrder") Integer nodeOrder);

    /**
     * 删除审批配置的所有节点配置
     */
    int deleteByApprovalConfigId(@Param("approvalConfigId") Long approvalConfigId);
}





package net.lab1024.sa.oa.workflow.service;

import java.util.List;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalNodeConfigForm;
import net.lab1024.sa.oa.workflow.entity.ApprovalNodeConfigEntity;

/**
 * 审批节点配置服务接口
 * <p>
 * 提供审批节点配置的CRUD操作
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在ioedream-oa-service中
 * - 使用@Resource注入依赖
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ApprovalNodeConfigService {

    /**
     * 根据审批配置ID查询所有节点配置
     *
     * @param approvalConfigId 审批配置ID
     * @return 节点配置列表
     */
    ResponseDTO<List<ApprovalNodeConfigEntity>> getNodeConfigsByApprovalConfigId(Long approvalConfigId);

    /**
     * 根据ID查询节点配置
     *
     * @param id 节点配置ID
     * @return 节点配置实体
     */
    ResponseDTO<ApprovalNodeConfigEntity> getNodeConfig(Long id);

    /**
     * 创建节点配置
     *
     * @param form 节点配置表单
     * @return 创建的节点配置实体
     */
    ResponseDTO<ApprovalNodeConfigEntity> createNodeConfig(ApprovalNodeConfigForm form);

    /**
     * 批量创建节点配置
     *
     * @param approvalConfigId 审批配置ID
     * @param forms 节点配置表单列表
     * @return 创建的节点配置列表
     */
    ResponseDTO<List<ApprovalNodeConfigEntity>> batchCreateNodeConfigs(Long approvalConfigId, List<ApprovalNodeConfigForm> forms);

    /**
     * 更新节点配置
     *
     * @param id 节点配置ID
     * @param form 节点配置表单
     * @return 更新后的节点配置实体
     */
    ResponseDTO<ApprovalNodeConfigEntity> updateNodeConfig(Long id, ApprovalNodeConfigForm form);

    /**
     * 删除节点配置
     *
     * @param id 节点配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteNodeConfig(Long id);

    /**
     * 删除审批配置的所有节点配置
     *
     * @param approvalConfigId 审批配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteNodeConfigsByApprovalConfigId(Long approvalConfigId);

    /**
     * 启用节点配置
     *
     * @param id 节点配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> enableNodeConfig(Long id);

    /**
     * 禁用节点配置
     *
     * @param id 节点配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> disableNodeConfig(Long id);
}






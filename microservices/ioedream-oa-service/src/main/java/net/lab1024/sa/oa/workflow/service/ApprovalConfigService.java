package net.lab1024.sa.oa.workflow.service;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalConfigForm;
import net.lab1024.sa.oa.workflow.entity.ApprovalConfigEntity;

/**
 * 审批配置服务接口
 * <p>
 * 提供审批配置的CRUD操作
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
public interface ApprovalConfigService {

    /**
     * 分页查询审批配置
     *
     * @param pageParam 分页参数
     * @param businessType 业务类型（可选）
     * @param module 所属模块（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    ResponseDTO<PageResult<ApprovalConfigEntity>> pageConfigs(
            PageParam pageParam,
            String businessType,
            String module,
            String status);

    /**
     * 根据ID查询审批配置
     *
     * @param id 配置ID
     * @return 审批配置实体
     */
    ResponseDTO<ApprovalConfigEntity> getConfig(Long id);

    /**
     * 根据业务类型查询审批配置
     *
     * @param businessType 业务类型
     * @return 审批配置实体
     */
    ResponseDTO<ApprovalConfigEntity> getConfigByBusinessType(String businessType);

    /**
     * 创建审批配置
     *
     * @param form 审批配置表单
     * @return 创建的审批配置实体
     */
    ResponseDTO<ApprovalConfigEntity> createConfig(ApprovalConfigForm form);

    /**
     * 更新审批配置
     *
     * @param id 配置ID
     * @param form 审批配置表单
     * @return 更新后的审批配置实体
     */
    ResponseDTO<ApprovalConfigEntity> updateConfig(Long id, ApprovalConfigForm form);

    /**
     * 删除审批配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteConfig(Long id);

    /**
     * 启用审批配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> enableConfig(Long id);

    /**
     * 禁用审批配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    ResponseDTO<Void> disableConfig(Long id);
}






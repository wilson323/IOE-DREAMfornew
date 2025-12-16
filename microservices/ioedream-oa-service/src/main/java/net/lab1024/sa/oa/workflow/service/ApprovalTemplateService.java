package net.lab1024.sa.oa.workflow.service;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalTemplateForm;
import net.lab1024.sa.oa.workflow.entity.ApprovalTemplateEntity;

/**
 * 审批模板服务接口
 * <p>
 * 提供审批模板的CRUD操作
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
public interface ApprovalTemplateService {

    /**
     * 分页查询审批模板
     *
     * @param pageParam 分页参数
     * @param category 模板分类（可选）
     * @param keyword 关键词（可选）
     * @param isPublic 是否公开（可选）
     * @return 分页结果
     */
    ResponseDTO<PageResult<ApprovalTemplateEntity>> pageTemplates(
            PageParam pageParam,
            String category,
            String keyword,
            Integer isPublic);

    /**
     * 根据ID查询审批模板
     *
     * @param id 模板ID
     * @return 审批模板实体
     */
    ResponseDTO<ApprovalTemplateEntity> getTemplate(Long id);

    /**
     * 根据模板编码查询审批模板
     *
     * @param templateCode 模板编码
     * @return 审批模板实体
     */
    ResponseDTO<ApprovalTemplateEntity> getTemplateByCode(String templateCode);

    /**
     * 创建审批模板
     *
     * @param form 审批模板表单
     * @return 创建的审批模板实体
     */
    ResponseDTO<ApprovalTemplateEntity> createTemplate(ApprovalTemplateForm form);

    /**
     * 复制审批模板
     *
     * @param id 模板ID
     * @param newTemplateName 新模板名称
     * @param newTemplateCode 新模板编码
     * @return 复制的审批模板实体
     */
    ResponseDTO<ApprovalTemplateEntity> copyTemplate(Long id, String newTemplateName, String newTemplateCode);

    /**
     * 更新审批模板
     *
     * @param id 模板ID
     * @param form 审批模板表单
     * @return 更新后的审批模板实体
     */
    ResponseDTO<ApprovalTemplateEntity> updateTemplate(Long id, ApprovalTemplateForm form);

    /**
     * 删除审批模板
     *
     * @param id 模板ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteTemplate(Long id);

    /**
     * 启用审批模板
     *
     * @param id 模板ID
     * @return 操作结果
     */
    ResponseDTO<Void> enableTemplate(Long id);

    /**
     * 禁用审批模板
     *
     * @param id 模板ID
     * @return 操作结果
     */
    ResponseDTO<Void> disableTemplate(Long id);

    /**
     * 从模板创建审批配置
     *
     * @param templateId 模板ID
     * @param businessType 业务类型
     * @param businessTypeName 业务类型名称
     * @return 创建的审批配置实体
     */
    ResponseDTO<net.lab1024.sa.common.workflow.entity.ApprovalConfigEntity> createConfigFromTemplate(
            Long templateId,
            String businessType,
            String businessTypeName);
}






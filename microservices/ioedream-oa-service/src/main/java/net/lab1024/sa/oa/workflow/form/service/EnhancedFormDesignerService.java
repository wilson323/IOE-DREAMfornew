package net.lab1024.sa.oa.workflow.form.service;

import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.oa.workflow.form.domain.*;

/**
 * 增强型表单设计器服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface EnhancedFormDesignerService {

    // ==================== 表单CRUD ====================

    /**
     * 获取表单设计详情
     *
     * @param formId 表单ID
     * @return 表单设计详情
     */
    FormDesignDetail getFormDesign(Long formId);

    /**
     * 保存表单设计
     *
     * @param form 表单设计表单
     * @return 表单ID
     */
    Long saveFormDesign(FormDesignForm form);

    /**
     * 更新表单设计
     *
     * @param formId 表单ID
     * @param form  表单设计表单
     */
    void updateFormDesign(Long formId, FormDesignForm form);

    /**
     * 删除表单设计
     *
     * @param formId 表单ID
     */
    void deleteFormDesign(Long formId);

    /**
     * 查询表单设计列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param formName 表单名称（可选）
     * @return 分页结果
     */
    PageResult<FormDesignVO> queryFormDesignPage(Integer pageNum, Integer pageSize, String formName);

    // ==================== 表单验证 ====================

    /**
     * 验证表单设计
     *
     * @param form 表单设计表单
     * @return 验证错误列表
     */
    java.util.List<OtherDomainObjects.ValidationError> validateFormDesign(FormDesignForm form);

    // ==================== 组件库管理 ====================

    /**
     * 获取表单组件库
     *
     * @param category 组件分类（basic, advanced, business）
     * @return 组件列表
     */
    java.util.List<FormComponent> getFormComponents(String category);

    /**
     * 获取组件配置Schema
     *
     * @param componentType 组件类型
     * @return 配置Schema
     */
    ComponentConfigSchema getComponentConfigSchema(String componentType);

    /**
     * 添加自定义组件
     *
     * @param component 组件定义
     * @return 组件ID
     */
    String addCustomComponent(OtherDomainObjects.CustomComponentForm component);

    // ==================== 验证规则管理 ====================

    /**
     * 获取验证规则库
     *
     * @return 验证规则列表
     */
    java.util.List<ValidationRule> getValidationRules();

    /**
     * 配置字段验证规则
     *
     * @param formId  表单ID
     * @param fieldId 字段ID
     * @param rules   验证规则列表
     */
    void configureFieldValidation(Long formId, String fieldId, java.util.List<ValidationRuleConfig> rules);

    // ==================== 表单逻辑管理 ====================

    /**
     * 配置表单逻辑
     *
     * @param formId 表单ID
     * @param logic  逻辑配置
     */
    void configureFormLogic(Long formId, FormLogicConfig logic);

    /**
     * 获取表单逻辑配置
     *
     * @param formId 表单ID
     * @return 逻辑配置列表
     */
    java.util.List<FormLogicConfig> getFormLogic(Long formId);

    /**
     * 删除表单逻辑
     *
     * @param formId 表单ID
     * @param logicId 逻辑ID
     */
    void deleteFormLogic(Long formId, String logicId);

    /**
     * 执行表单逻辑
     *
     * @param formId   表单ID
     * @param formData 表单数据
     * @return 执行结果
     */
    OtherDomainObjects.FormLogicExecutionResult executeFormLogic(Long formId, java.util.Map<String, Object> formData);

    // ==================== 表单模板管理 ====================

    /**
     * 获取表单模板列表
     *
     * @param category 分类（可选）
     * @return 模板列表
     */
    java.util.List<FormTemplate> getFormTemplates(String category);

    /**
     * 应用模板创建表单
     *
     * @param templateId       模板ID
     * @param newFormName      新表单名称
     * @param customizations   自定义配置
     * @return 新表单ID
     */
    Long applyTemplate(String templateId, String newFormName, java.util.Map<String, Object> customizations);

    /**
     * 上传自定义模板
     *
     * @param template 模板定义
     * @return 模板ID
     */
    String uploadTemplate(FormTemplate template);

    // ==================== 表单预览和测试 ====================

    /**
     * 预览表单
     *
     * @param formId 表单ID
     * @return 表单预览数据
     */
    FormPreviewData previewForm(Long formId);

    /**
     * 测试表单验证
     *
     * @param formId   表单ID
     * @param testData 测试数据
     * @return 验证结果
     */
    OtherDomainObjects.ValidationResult testFormValidation(Long formId, OtherDomainObjects.FormTestData testData);

    /**
     * 测试表单逻辑
     *
     * @param formId   表单ID
     * @param testData 测试数据
     * @return 逻辑执行结果
     */
    OtherDomainObjects.FormLogicExecutionResult testFormLogic(Long formId, OtherDomainObjects.FormTestData testData);

    // ==================== 表单版本管理 ====================

    /**
     * 获取表单版本历史
     *
     * @param formId 表单ID
     * @return 版本列表
     */
    java.util.List<FormVersion> getFormVersions(Long formId);

    /**
     * 发布表单版本
     *
     * @param formId 表单ID
     * @return 版本号
     */
    String publishForm(Long formId);

    /**
     * 回滚到指定版本
     *
     * @param formId  表单ID
     * @param version 版本号
     */
    void rollbackFormVersion(Long formId, String version);

    // ==================== 表单导入导出 ====================

    /**
     * 导入表单设计
     *
     * @param form 表单设计数据
     * @return 新表单ID
     */
    Long importForm(FormDesignForm form);

    // ==================== 表单统计 ====================

    /**
     * 获取表单使用统计
     *
     * @param formId 表单ID
     * @return 统计数据
     */
    FormStatistics getFormStatistics(Long formId);

    // ==================== 表单逻辑引擎 ====================

    /**
     * 触发表单逻辑（当字段值变化时）
     *
     * @param formId     表单ID
     * @param triggerFieldId 触发字段ID
     * @param formData   当前表单数据
     * @return 执行的动作列表
     */
    java.util.List<FormLogicConfig.LogicAction> triggerFieldLogic(Long formId, String triggerFieldId, java.util.Map<String, Object> formData);

    /**
     * 验证表单数据
     *
     * @param formId   表单ID
     * @param formData 表单数据
     * @return 验证结果
     */
    OtherDomainObjects.ValidationResult validateFormData(Long formId, java.util.Map<String, Object> formData);
}

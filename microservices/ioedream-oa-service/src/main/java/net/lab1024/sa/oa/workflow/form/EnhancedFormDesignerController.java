package net.lab1024.sa.oa.workflow.form;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.form.domain.*;
import net.lab1024.sa.oa.workflow.form.service.EnhancedFormDesignerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 增强型低代码表单设计器控制器
 *
 * 提供拖拽式表单设计功能，包括：
 * 1. 表单组件库（基础、高级、业务组件，共15+种）
 * 2. 表单设计器（拖拽式设计、实时预览）
 * 3. 表单验证规则配置
 * 4. 表单逻辑配置（显示/隐藏、启用/禁用、级联下拉）
 * 5. 表单模板库
 * 6. 表单版本管理
 * 7. 表单测试和调试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oa/workflow/form-designer")
@Validated
public class EnhancedFormDesignerController {

    @Resource
    private EnhancedFormDesignerService formDesignerService;

    // ==================== 表单CRUD ====================

    /**
     * 获取表单设计详情
     *
     * @param formId 表单ID
     * @return 表单设计详情
     */
    @GetMapping("/{formId}")
    public ResponseDTO<FormDesignDetail> getFormDesign(@PathVariable Long formId) {

        log.info("[表单设计器] 获取表单设计: formId={}", formId);

        FormDesignDetail detail = formDesignerService.getFormDesign(formId);

        return ResponseDTO.ok(detail);
    }

    /**
     * 保存表单设计
     *
     * @param form 表单设计表单
     * @return 表单ID
     */
    @PostMapping("/save")
    public ResponseDTO<Long> saveFormDesign(@RequestBody @Valid FormDesignForm form) {

        log.info("[表单设计器] 保存表单设计: formName={}, componentCount={}",
                form.getFormName(), form.getComponents().size());

        // 1. 验证表单设计
        List<ValidationError> errors = formDesignerService.validateFormDesign(form);
        if (!errors.isEmpty()) {
            return ResponseDTO.error("FORM_VALIDATION_ERROR",
                    "表单验证失败: " + errors.get(0).getMessage());
        }

        // 2. 保存表单设计
        Long formId = formDesignerService.saveFormDesign(form);

        log.info("[表单设计器] 表单设计保存成功: formId={}", formId);

        return ResponseDTO.ok(formId);
    }

    /**
     * 更新表单设计
     *
     * @param formId 表单ID
     * @param form 表单设计表单
     * @return 操作结果
     */
    @PostMapping("/{formId}/update")
    public ResponseDTO<Void> updateFormDesign(
            @PathVariable Long formId,
            @RequestBody @Valid FormDesignForm form) {

        log.info("[表单设计器] 更新表单设计: formId={}", formId);

        formDesignerService.updateFormDesign(formId, form);

        return ResponseDTO.ok();
    }

    /**
     * 删除表单设计
     *
     * @param formId 表单ID
     * @return 操作结果
     */
    @PostMapping("/{formId}/delete")
    public ResponseDTO<Void> deleteFormDesign(@PathVariable Long formId) {

        log.info("[表单设计器] 删除表单设计: formId={}", formId);

        formDesignerService.deleteFormDesign(formId);

        return ResponseDTO.ok();
    }

    /**
     * 获取表单列表（分页）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param formName 表单名称（可选）
     * @return 表单列表
     */
    @GetMapping("/page")
    public ResponseDTO<PageResult<FormDesignVO>> queryFormDesignPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String formName) {

        log.info("[表单设计器] 查询表单列表: pageNum={}, pageSize={}, formName={}",
                pageNum, pageSize, formName);

        PageResult<FormDesignVO> result = formDesignerService.queryFormDesignPage(
                pageNum, pageSize, formName
        );

        return ResponseDTO.ok(result);
    }

    // ==================== 组件库管理 ====================

    /**
     * 获取表单组件库
     *
     * @param category 组件分类（basic, advanced, business）
     * @return 组件列表
     */
    @GetMapping("/components")
    public ResponseDTO<List<FormComponent>> getFormComponents(
            @RequestParam(required = false) String category) {

        log.info("[表单设计器] 获取组件库: category={}", category);

        List<FormComponent> components = formDesignerService.getFormComponents(category);

        return ResponseDTO.ok(components);
    }

    /**
     * 获取组件配置Schema
     *
     * @param componentType 组件类型
     * @return 配置Schema
     */
    @GetMapping("/components/{componentType}/schema")
    public ResponseDTO<ComponentConfigSchema> getComponentConfigSchema(
            @PathVariable String componentType) {

        log.info("[表单设计器] 获取组件配置Schema: componentType={}", componentType);

        ComponentConfigSchema schema = formDesignerService.getComponentConfigSchema(componentType);

        return ResponseDTO.ok(schema);
    }

    /**
     * 添加自定义组件
     *
     * @param component 组件定义
     * @return 组件ID
     */
    @PostMapping("/components/custom")
    public ResponseDTO<String> addCustomComponent(@RequestBody @Valid CustomComponentForm component) {

        log.info("[表单设计器] 添加自定义组件: componentName={}", component.getComponentName());

        String componentId = formDesignerService.addCustomComponent(component);

        return ResponseDTO.ok(componentId);
    }

    // ==================== 验证规则配置 ====================

    /**
     * 获取验证规则库
     *
     * @return 验证规则列表
     */
    @GetMapping("/validation-rules")
    public ResponseDTO<List<ValidationRule>> getValidationRules() {

        List<ValidationRule> rules = formDesignerService.getValidationRules();

        return ResponseDTO.ok(rules);
    }

    /**
     * 配置字段验证规则
     *
     * @param formId 表单ID
     * @param fieldId 字段ID
     * @param rules 验证规则列表
     * @return 操作结果
     */
    @PostMapping("/{formId}/fields/{fieldId}/validation")
    public ResponseDTO<Void> configureFieldValidation(
            @PathVariable Long formId,
            @PathVariable String fieldId,
            @RequestBody @Valid List<ValidationRuleConfig> rules) {

        log.info("[表单设计器] 配置字段验证: formId={}, fieldId={}, ruleCount={}",
                formId, fieldId, rules.size());

        formDesignerService.configureFieldValidation(formId, fieldId, rules);

        return ResponseDTO.ok();
    }

    // ==================== 表单逻辑配置 ====================

    /**
     * 配置表单逻辑（显示/隐藏、启用/禁用、级联下拉）
     *
     * @param formId 表单ID
     * @param logic 逻辑配置
     * @return 操作结果
     */
    @PostMapping("/{formId}/logic")
    public ResponseDTO<Void> configureFormLogic(
            @PathVariable Long formId,
            @RequestBody @Valid FormLogicConfig logic) {

        log.info("[表单设计器] 配置表单逻辑: formId={}, logicType={}",
                formId, logic.getLogicType());

        formDesignerService.configureFormLogic(formId, logic);

        return ResponseDTO.ok();
    }

    /**
     * 获取表单逻辑配置
     *
     * @param formId 表单ID
     * @return 逻辑配置列表
     */
    @GetMapping("/{formId}/logic")
    public ResponseDTO<List<FormLogicConfig>> getFormLogic(@PathVariable Long formId) {

        List<FormLogicConfig> logicConfigs = formDesignerService.getFormLogic(formId);

        return ResponseDTO.ok(logicConfigs);
    }

    /**
     * 删除表单逻辑
     *
     * @param formId 表单ID
     * @param logicId 逻辑ID
     * @return 操作结果
     */
    @PostMapping("/{formId}/logic/{logicId}/delete")
    public ResponseDTO<Void> deleteFormLogic(
            @PathVariable Long formId,
            @PathVariable String logicId) {

        log.info("[表单设计器] 删除表单逻辑: formId={}, logicId={}", formId, logicId);

        formDesignerService.deleteFormLogic(formId, logicId);

        return ResponseDTO.ok();
    }

    // ==================== 表单模板库 ====================

    /**
     * 获取表单模板列表
     *
     * @param category 分类（可选）
     * @return 模板列表
     */
    @GetMapping("/templates")
    public ResponseDTO<List<FormTemplate>> getFormTemplates(
            @RequestParam(required = false) String category) {

        log.info("[表单设计器] 获取表单模板: category={}", category);

        List<FormTemplate> templates = formDesignerService.getFormTemplates(category);

        return ResponseDTO.ok(templates);
    }

    /**
     * 使用模板创建表单
     *
     * @param form 应用模板表单
     * @return 新表单ID
     */
    @PostMapping("/apply-template")
    public ResponseDTO<Long> applyTemplate(@RequestBody @Valid ApplyTemplateForm form) {

        log.info("[表单设计器] 应用模板: templateId={}, newFormName={}",
                form.getTemplateId(), form.getNewFormName());

        Long formId = formDesignerService.applyTemplate(
                form.getTemplateId(),
                form.getNewFormName(),
                form.getCustomizations()
        );

        return ResponseDTO.ok(formId);
    }

    /**
     * 上传自定义模板
     *
     * @param template 模板定义
     * @return 模板ID
     */
    @PostMapping("/templates/upload")
    public ResponseDTO<String> uploadTemplate(@RequestBody @Valid FormTemplate template) {

        log.info("[表单设计器] 上传自定义模板: templateName={}", template.getTemplateName());

        String templateId = formDesignerService.uploadTemplate(template);

        return ResponseDTO.ok(templateId);
    }

    // ==================== 表单预览和测试 ====================

    /**
     * 预览表单（渲染JSON）
     *
     * @param formId 表单ID
     * @return 表单渲染数据
     */
    @GetMapping("/{formId}/preview")
    public ResponseDTO<FormPreviewData> previewForm(@PathVariable Long formId) {

        log.info("[表单设计器] 预览表单: formId={}", formId);

        FormPreviewData previewData = formDesignerService.previewForm(formId);

        return ResponseDTO.ok(previewData);
    }

    /**
     * 测试表单验证
     *
     * @param formId 表单ID
     * @param formData 表单数据
     * @return 验证结果
     */
    @PostMapping("/{formId}/test-validation")
    public ResponseDTO<ValidationResult> testFormValidation(
            @PathVariable Long formId,
            @RequestBody @Valid FormTestData formData) {

        log.info("[表单设计器] 测试表单验证: formId={}", formId);

        ValidationResult result = formDesignerService.testFormValidation(formId, formData);

        return ResponseDTO.ok(result);
    }

    /**
     * 测试表单逻辑
     *
     * @param formId 表单ID
     * @param formData 表单数据
     * @return 逻辑执行结果
     */
    @PostMapping("/{formId}/test-logic")
    public ResponseDTO<FormLogicExecutionResult> testFormLogic(
            @PathVariable Long formId,
            @RequestBody @Valid FormTestData formData) {

        log.info("[表单设计器] 测试表单逻辑: formId={}", formId);

        FormLogicExecutionResult result = formDesignerService.testFormLogic(formId, formData);

        return ResponseDTO.ok(result);
    }

    // ==================== 表单版本管理 ====================

    /**
     * 获取表单版本历史
     *
     * @param formId 表单ID
     * @return 版本列表
     */
    @GetMapping("/{formId}/versions")
    public ResponseDTO<List<FormVersion>> getFormVersions(@PathVariable Long formId) {

        log.info("[表单设计器] 获取表单版本历史: formId={}", formId);

        List<FormVersion> versions = formDesignerService.getFormVersions(formId);

        return ResponseDTO.ok(versions);
    }

    /**
     * 发布表单版本
     *
     * @param formId 表单ID
     * @return 版本号
     */
    @PostMapping("/{formId}/publish")
    public ResponseDTO<String> publishForm(@PathVariable Long formId) {

        log.info("[表单设计器] 发布表单: formId={}", formId);

        String version = formDesignerService.publishForm(formId);

        return ResponseDTO.ok(version);
    }

    /**
     * 回滚到指定版本
     *
     * @param formId 表单ID
     * @param version 版本号
     * @return 操作结果
     */
    @PostMapping("/{formId}/rollback/{version}")
    public ResponseDTO<Void> rollbackFormVersion(
            @PathVariable Long formId,
            @PathVariable String version) {

        log.info("[表单设计器] 回滚表单版本: formId={}, version={}", formId, version);

        formDesignerService.rollbackFormVersion(formId, version);

        return ResponseDTO.ok();
    }

    // ==================== 表单导入导出 ====================

    /**
     * 导出表单设计
     *
     * @param formId 表单ID
     * @return 表单设计JSON
     */
    @GetMapping("/{formId}/export")
    public ResponseDTO<FormDesignDetail> exportForm(@PathVariable Long formId) {

        log.info("[表单设计器] 导出表单设计: formId={}", formId);

        FormDesignDetail detail = formDesignerService.getFormDesign(formId);

        return ResponseDTO.ok(detail);
    }

    /**
     * 导入表单设计
     *
     * @param form 表单设计数据
     * @return 新表单ID
     */
    @PostMapping("/import")
    public ResponseDTO<Long> importForm(@RequestBody @Valid FormDesignForm form) {

        log.info("[表单设计器] 导入表单设计: formName={}", form.getFormName());

        Long formId = formDesignerService.importForm(form);

        return ResponseDTO.ok(formId);
    }

    // ==================== 表单统计 ====================

    /**
     * 获取表单使用统计
     *
     * @param formId 表单ID
     * @return 统计数据
     */
    @GetMapping("/{formId}/statistics")
    public ResponseDTO<FormStatistics> getFormStatistics(@PathVariable Long formId) {

        log.info("[表单设计器] 获取表单统计: formId={}", formId);

        FormStatistics statistics = formDesignerService.getFormStatistics(formId);

        return ResponseDTO.ok(statistics);
    }
}

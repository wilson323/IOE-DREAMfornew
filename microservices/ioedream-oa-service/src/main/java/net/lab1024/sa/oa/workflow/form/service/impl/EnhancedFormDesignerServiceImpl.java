package net.lab1024.sa.oa.workflow.form.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.oa.workflow.form.domain.*;
import net.lab1024.sa.oa.workflow.form.service.EnhancedFormDesignerService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 增强型表单设计器服务实现
 *
 * 核心功能:
 * 1. 表单CRUD管理
 * 2. 表单验证引擎
 * 3. 表单逻辑引擎（显示/隐藏、级联、计算）
 * 4. 表单版本管理
 * 5. 表单模板库
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class EnhancedFormDesignerServiceImpl implements EnhancedFormDesignerService {

    @Resource
    private ObjectMapper objectMapper;

    // 模拟数据库存储（实际应该使用DAO层）
    private final Map<Long, FormDesignDetail> formDesignStorage = new ConcurrentHashMap<>();
    private final Map<String, FormComponent> componentLibrary = new ConcurrentHashMap<>();
    private final Map<String, FormTemplate> templateLibrary = new ConcurrentHashMap<>();
    private final Map<Long, List<FormVersion>> versionStorage = new ConcurrentHashMap<>();

    /**
     * 初始化组件库和模板库
     */
    public EnhancedFormDesignerServiceImpl() {
        initializeComponentLibrary();
        initializeTemplateLibrary();
    }

    // ==================== 表单CRUD ====================

    @Override
    @Cacheable(value = "formDesign", key = "#formId")
    public FormDesignDetail getFormDesign(Long formId) {
        log.info("[表单设计器] 获取表单设计: formId={}", formId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        log.info("[表单设计器] 表单设计获取成功: formId={}, formName={}", formId, detail.getFormName());
        return detail;
    }

    @Override
    @CacheEvict(value = "formDesign", allEntries = true)
    public Long saveFormDesign(FormDesignForm form) {
        log.info("[表单设计器] 保存表单设计: formName={}, componentCount={}",
                form.getFormName(), form.getComponents().size());

        try {
            // 1. 生成表单ID
            Long formId = System.currentTimeMillis();

            // 2. 构建表单设计详情
            FormDesignDetail detail = FormDesignDetail.builder()
                    .formId(formId)
                    .formName(form.getFormName())
                    .formDescription(form.getFormDescription())
                    .formCategory(form.getFormCategory())
                    .version("1.0.0")
                    .status("draft")
                    .components(form.getComponents())
                    .layoutConfig(form.getLayoutConfig())
                    .styleConfig(form.getStyleConfig())
                    .submitConfig(form.getSubmitConfig())
                    .permissionConfig(form.getPermissionConfig())
                    .extendedAttributes(form.getExtendedAttributes())
                    .createUserId(1L)  // TODO: 从上下文获取当前用户
                    .createUserName("Admin")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .usageCount(0L)
                    .build();

            // 3. 生成JSON Schema
            String jsonSchema = generateJsonSchema(detail);
            detail.setJsonSchema(jsonSchema);

            // 4. 保存到存储
            formDesignStorage.put(formId, detail);

            // 5. 初始化版本历史
            List<FormVersion> versions = new ArrayList<>();
            versions.add(FormVersion.builder()
                    .versionId(formId)
                    .formId(formId)
                    .version("1.0.0")
                    .description("初始版本")
                    .status("draft")
                    .formDesignJson(serializeFormDesign(form))
                    .createUserId(1L)
                    .createUserName("Admin")
                    .createTime(LocalDateTime.now())
                    .current(true)
                    .build());
            versionStorage.put(formId, versions);

            log.info("[表单设计器] 表单设计保存成功: formId={}, formName={}", formId, form.getFormName());
            return formId;

        } catch (Exception e) {
            log.error("[表单设计器] 保存表单设计失败: formName={}", form.getFormName(), e);
            throw new RuntimeException("保存表单设计失败", e);
        }
    }

    @Override
    @CacheEvict(value = "formDesign", allEntries = true)
    public void updateFormDesign(Long formId, FormDesignForm form) {
        log.info("[表单设计器] 更新表单设计: formId={}", formId);

        FormDesignDetail existing = formDesignStorage.get(formId);
        if (existing == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        try {
            // 更新表单设计
            existing.setFormName(form.getFormName());
            existing.setFormDescription(form.getFormDescription());
            existing.setComponents(form.getComponents());
            existing.setLayoutConfig(form.getLayoutConfig());
            existing.setStyleConfig(form.getStyleConfig());
            existing.setSubmitConfig(form.getSubmitConfig());
            existing.setPermissionConfig(form.getPermissionConfig());
            existing.setExtendedAttributes(form.getExtendedAttributes());
            existing.setUpdateTime(LocalDateTime.now());

            // 重新生成JSON Schema
            String jsonSchema = generateJsonSchema(existing);
            existing.setJsonSchema(jsonSchema);

            log.info("[表单设计器] 表单设计更新成功: formId={}", formId);

        } catch (Exception e) {
            log.error("[表单设计器] 更新表单设计失败: formId={}", formId, e);
            throw new RuntimeException("更新表单设计失败", e);
        }
    }

    @Override
    @CacheEvict(value = "formDesign", allEntries = true)
    public void deleteFormDesign(Long formId) {
        log.info("[表单设计器] 删除表单设计: formId={}", formId);

        FormDesignDetail removed = formDesignStorage.remove(formId);
        if (removed == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        versionStorage.remove(formId);

        log.info("[表单设计器] 表单设计删除成功: formId={}", formId);
    }

    @Override
    public PageResult<FormDesignVO> queryFormDesignPage(Integer pageNum, Integer pageSize, String formName) {
        log.info("[表单设计器] 查询表单列表: pageNum={}, pageSize={}, formName={}",
                pageNum, pageSize, formName);

        // 模拟分页查询
        List<FormDesignVO> allForms = formDesignStorage.values().stream()
                .filter(detail -> formName == null || detail.getFormName().contains(formName))
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allForms.size());

        List<FormDesignVO> pageData = allForms.subList(start, end);

        return PageResult.<FormDesignVO>builder()
                .list(pageData)
                .total((long) allForms.size())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages((allForms.size() + pageSize - 1) / pageSize)
                .build();
    }

    // ==================== 表单验证 ====================

    @Override
    public List<OtherDomainObjects.ValidationError> validateFormDesign(FormDesignForm form) {
        log.info("[表单设计器] 验证表单设计: formName={}", form.getFormName());

        List<OtherDomainObjects.ValidationError> errors = new ArrayList<>();

        // 1. 验证基本信息
        if (form.getFormName() == null || form.getFormName().trim().isEmpty()) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId("formName")
                    .message("表单名称不能为空")
                    .errorCode("FORM_NAME_EMPTY")
                    .level("error")
                    .build());
        }

        // 2. 验证组件列表
        if (form.getComponents() == null || form.getComponents().isEmpty()) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId("components")
                    .message("表单组件不能为空")
                    .errorCode("COMPONENTS_EMPTY")
                    .level("error")
                    .build());
        } else {
            // 验证每个组件
            for (FormComponent component : form.getComponents()) {
                List<OtherDomainObjects.ValidationError> componentErrors = validateComponent(component);
                errors.addAll(componentErrors);
            }
        }

        // 3. 验证字段ID唯一性
        Set<String> fieldIds = new HashSet<>();
        Set<String> duplicateFieldIds = new HashSet<>();
        for (FormComponent component : form.getComponents()) {
            if (!fieldIds.add(component.getFieldName())) {
                duplicateFieldIds.add(component.getFieldName());
            }
        }

        if (!duplicateFieldIds.isEmpty()) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId("fieldIds")
                    .message("字段ID重复: " + String.join(", ", duplicateFieldIds))
                    .errorCode("DUPLICATE_FIELD_IDS")
                    .level("error")
                    .build());
        }

        log.info("[表单设计器] 表单验证完成: errorCount={}, warningCount={}",
                errors.stream().filter(e -> "error".equals(e.getLevel())).count(),
                errors.stream().filter(e -> "warning".equals(e.getLevel())).count());

        return errors;
    }

    /**
     * 验证单个组件
     */
    private List<OtherDomainObjects.ValidationError> validateComponent(FormComponent component) {
        List<OtherDomainObjects.ValidationError> errors = new ArrayList<>();

        // 验证组件ID
        if (component.getComponentId() == null || component.getComponentId().trim().isEmpty()) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId(component.getFieldName())
                    .message("组件ID不能为空")
                    .errorCode("COMPONENT_ID_EMPTY")
                    .level("error")
                    .build());
        }

        // 验证字段名称
        if (component.getFieldName() == null || component.getFieldName().trim().isEmpty()) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId(component.getComponentId())
                    .message("字段名称不能为空")
                    .errorCode("FIELD_NAME_EMPTY")
                    .level("error")
                    .build());
        }

        // 验证字段名称格式（只允许字母、数字、下划线）
        if (component.getFieldName() != null && !component.getFieldName().matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId(component.getFieldName())
                    .message("字段名称格式错误，只允许字母、数字、下划线，且必须以字母开头")
                    .errorCode("FIELD_NAME_FORMAT_ERROR")
                    .level("error")
                    .build());
        }

        // 验证组件标签
        if (component.getLabel() == null || component.getLabel().trim().isEmpty()) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId(component.getFieldName())
                    .message("字段标签不能为空")
                    .errorCode("FIELD_LABEL_EMPTY")
                    .level("warning")
                    .build());
        }

        return errors;
    }

    // ==================== 组件库管理 ====================

    @Override
    @Cacheable(value = "formComponents", key = "#category")
    public List<FormComponent> getFormComponents(String category) {
        log.info("[表单设计器] 获取组件库: category={}", category);

        List<FormComponent> components = componentLibrary.values().stream()
                .filter(component -> category == null || category.equals(component.getCategory()))
                .collect(Collectors.toList());

        log.info("[表单设计器] 组件库获取成功: category={}, count={}", category, components.size());
        return components;
    }

    @Override
    @Cacheable(value = "componentSchema", key = "#componentType")
    public ComponentConfigSchema getComponentConfigSchema(String componentType) {
        log.info("[表单设计器] 获取组件配置Schema: componentType={}", componentType);

        FormComponent component = componentLibrary.get(componentType);
        if (component == null) {
            throw new RuntimeException("组件不存在: " + componentType);
        }

        // 构建配置Schema
        ComponentConfigSchema schema = buildComponentConfigSchema(componentType);

        log.info("[表单设计器] 组件配置Schema获取成功: componentType={}", componentType);
        return schema;
    }

    @Override
    public String addCustomComponent(OtherDomainObjects.CustomComponentForm component) {
        log.info("[表单设计器] 添加自定义组件: componentName={}", component.getComponentName());

        String componentId = "custom_" + System.currentTimeMillis();

        FormComponent customComponent = FormComponent.builder()
                .componentId(componentId)
                .componentType("custom")
                .category(component.getCategory())
                .fieldName("custom_field")
                .label(component.getComponentName())
                .build();

        componentLibrary.put(componentId, customComponent);

        log.info("[表单设计器] 自定义组件添加成功: componentId={}", componentId);
        return componentId;
    }

    // ==================== 验证规则管理 ====================

    @Override
    public List<ValidationRule> getValidationRules() {
        log.info("[表单设计器] 获取验证规则库");

        return Arrays.asList(
                ValidationRule.builder()
                        .ruleId("required")
                        .ruleName("必填")
                        .ruleType("required")
                        .description("字段不能为空")
                        .errorMessage("该字段为必填项")
                        .build(),
                ValidationRule.builder()
                        .ruleId("minLength")
                        .ruleName("最小长度")
                        .ruleType("minLength")
                        .description("字段最小长度")
                        .errorMessage("字段长度不能小于{min}个字符")
                        .build(),
                ValidationRule.builder()
                        .ruleId("maxLength")
                        .ruleName("最大长度")
                        .ruleType("maxLength")
                        .description("字段最大长度")
                        .errorMessage("字段长度不能超过{max}个字符")
                        .build(),
                ValidationRule.builder()
                        .ruleId("pattern")
                        .ruleName("正则表达式")
                        .ruleType("pattern")
                        .description("正则表达式验证")
                        .errorMessage("字段格式不正确")
                        .build(),
                ValidationRule.builder()
                        .ruleId("email")
                        .ruleName("邮箱格式")
                        .ruleType("email")
                        .description("邮箱格式验证")
                        .errorMessage("请输入正确的邮箱地址")
                        .build(),
                ValidationRule.builder()
                        .ruleId("phone")
                        .ruleName("手机号格式")
                        .ruleType("phone")
                        .description("手机号格式验证")
                        .errorMessage("请输入正确的手机号")
                        .build()
        );
    }

    @Override
    public void configureFieldValidation(Long formId, String fieldId, List<ValidationRuleConfig> rules) {
        log.info("[表单设计器] 配置字段验证: formId={}, fieldId={}, ruleCount={}",
                formId, fieldId, rules.size());

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        // 查找目标组件并更新验证规则
        for (FormComponent component : detail.getComponents()) {
            if (component.getFieldName().equals(fieldId)) {
                component.setValidationRules(rules);
                log.info("[表单设计器] 字段验证配置成功: fieldId={}", fieldId);
                return;
            }
        }

        throw new RuntimeException("字段不存在: " + fieldId);
    }

    // ==================== 表单逻辑管理 ====================

    @Override
    public void configureFormLogic(Long formId, FormLogicConfig logic) {
        log.info("[表单设计器] 配置表单逻辑: formId={}, logicType={}",
                formId, logic.getLogicType());

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        List<FormLogicConfig> logicConfigs = detail.getLogicConfigs();
        if (logicConfigs == null) {
            logicConfigs = new ArrayList<>();
            detail.setLogicConfigs(logicConfigs);
        }

        // 检查是否已存在相同逻辑ID
        logicConfigs.removeIf(l -> l.getLogicId().equals(logic.getLogicId()));
        logicConfigs.add(logic);

        // 按顺序排序
        logicConfigs.sort(Comparator.comparing(FormLogicConfig::getOrder));

        log.info("[表单设计器] 表单逻辑配置成功: logicId={}, logicType={}",
                logic.getLogicId(), logic.getLogicType());
    }

    @Override
    public List<FormLogicConfig> getFormLogic(Long formId) {
        log.info("[表单设计器] 获取表单逻辑: formId={}", formId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        return detail.getLogicConfigs() != null ? detail.getLogicConfigs() : new ArrayList<>();
    }

    @Override
    public void deleteFormLogic(Long formId, String logicId) {
        log.info("[表单设计器] 删除表单逻辑: formId={}, logicId={}", formId, logicId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        if (detail.getLogicConfigs() != null) {
            detail.getLogicConfigs().removeIf(l -> l.getLogicId().equals(logicId));
            log.info("[表单设计器] 表单逻辑删除成功: logicId={}", logicId);
        }
    }

    @Override
    public OtherDomainObjects.FormLogicExecutionResult executeFormLogic(Long formId, Map<String, Object> formData) {
        log.info("[表单逻辑引擎] 执行表单逻辑: formId={}", formId);

        long startTime = System.currentTimeMillis();

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        List<FormLogicConfig> logicConfigs = detail.getLogicConfigs();
        List<OtherDomainObjects.FormLogicExecutionResult.ExecutedAction> executedActions = new ArrayList<>();

        if (logicConfigs != null && !logicConfigs.isEmpty()) {
            for (FormLogicConfig logicConfig : logicConfigs) {
                if (!logicConfig.getEnabled()) {
                    continue;
                }

                // 检查触发条件
                if (evaluateTriggerConditions(logicConfig.getTriggerConditions(), formData)) {
                    // 执行动作
                    for (FormLogicConfig.LogicAction action : logicConfig.getActions()) {
                        OtherDomainObjects.FormLogicExecutionResult.ExecutedAction executedAction =
                                executeAction(action, formData);

                        executedActions.add(executedAction);
                    }
                }
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return OtherDomainObjects.FormLogicExecutionResult.builder()
                .success(true)
                .executedActions(executedActions)
                .executionTime(executionTime)
                .build();
    }

    /**
     * 评估触发条件
     */
    private boolean evaluateTriggerConditions(List<FormLogicConfig.TriggerCondition> conditions, Map<String, Object> formData) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (FormLogicConfig.TriggerCondition condition : conditions) {
            Object fieldValue = formData.get(condition.getFieldId());

            if (!evaluateCondition(condition, fieldValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 评估单个条件
     */
    private boolean evaluateCondition(FormLogicConfig.TriggerCondition condition, Object fieldValue) {
        boolean result = false;

        switch (condition.getOperator()) {
            case "eq":
                result = Objects.equals(fieldValue, condition.getValue());
                break;
            case "ne":
                result = !Objects.equals(fieldValue, condition.getValue());
                break;
            case "isEmpty":
                result = fieldValue == null || "".equals(fieldValue);
                break;
            case "isNotEmpty":
                result = fieldValue != null && !"".equals(fieldValue);
                break;
            case "contains":
                result = fieldValue != null && fieldValue.toString().contains(condition.getValue().toString());
                break;
            // TODO: 实现更多操作符
            default:
                result = true;
        }

        return condition.getNot() ? !result : result;
    }

    /**
     * 执行动作
     */
    private OtherDomainObjects.FormLogicExecutionResult.ExecutedAction executeAction(
            FormLogicConfig.LogicAction action, Map<String, Object> formData) {

        Object beforeValue = formData.get(action.getTargetFieldId());
        Object afterValue = beforeValue;
        String status = "success";
        String errorMessage = null;

        try {
            switch (action.getActionType()) {
                case "show":
                case "enable":
                case "setRequired":
                    // 这些动作在前端处理，这里只记录
                    break;

                case "hide":
                case "disable":
                case "setNotRequired":
                    // 这些动作在前端处理，这里只记录
                    break;

                case "setValue":
                    afterValue = action.getValue();
                    formData.put(action.getTargetFieldId(), afterValue);
                    break;

                case "clearValue":
                    afterValue = null;
                    formData.remove(action.getTargetFieldId());
                    break;

                default:
                    status = "skipped";
                    break;
            }

        } catch (Exception e) {
            status = "failed";
            errorMessage = e.getMessage();
            log.error("[表单逻辑引擎] 执行动作失败: actionType={}, targetFieldId={}",
                    action.getActionType(), action.getTargetFieldId(), e);
        }

        return OtherDomainObjects.FormLogicExecutionResult.ExecutedAction.builder()
                .actionType(action.getActionType())
                .targetFieldId(action.getTargetFieldId())
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .status(status)
                .errorMessage(errorMessage)
                .build();
    }

    @Override
    public List<FormLogicConfig.LogicAction> triggerFieldLogic(Long formId, String triggerFieldId, Map<String, Object> formData) {
        log.info("[表单逻辑引擎] 触发字段逻辑: formId={}, triggerFieldId={}", formId, triggerFieldId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        List<FormLogicConfig.LogicAction> actionsToExecute = new ArrayList<>();

        List<FormLogicConfig> logicConfigs = detail.getLogicConfigs();
        if (logicConfigs != null) {
            for (FormLogicConfig logicConfig : logicConfigs) {
                if (!logicConfig.getEnabled()) {
                    continue;
                }

                // 检查是否由触发字段引发
                for (FormLogicConfig.TriggerCondition condition : logicConfig.getTriggerConditions()) {
                    if (condition.getFieldId().equals(triggerFieldId)) {
                        if (evaluateTriggerConditions(logicConfig.getTriggerConditions(), formData)) {
                            actionsToExecute.addAll(logicConfig.getActions());
                        }
                        break;
                    }
                }
            }
        }

        return actionsToExecute;
    }

    @Override
    public OtherDomainObjects.ValidationResult validateFormData(Long formId, Map<String, Object> formData) {
        log.info("[表单验证引擎] 验证表单数据: formId={}", formId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        List<OtherDomainObjects.ValidationError> errors = new ArrayList<>();

        // 遍历所有组件，进行验证
        for (FormComponent component : detail.getComponents()) {
            List<OtherDomainObjects.ValidationError> componentErrors =
                    validateComponentData(component, formData);

            errors.addAll(componentErrors);
        }

        boolean valid = errors.stream()
                .noneMatch(error -> "error".equals(error.getLevel()));

        return OtherDomainObjects.ValidationResult.builder()
                .valid(valid)
                .errors(errors)
                .build();
    }

    /**
     * 验证组件数据
     */
    private List<OtherDomainObjects.ValidationError> validateComponentData(FormComponent component, Map<String, Object> formData) {
        List<OtherDomainObjects.ValidationError> errors = new ArrayList<>();

        Object fieldValue = formData.get(component.getFieldName());

        // 1. 验证必填
        if (component.getRequired() && (fieldValue == null || "".equals(fieldValue))) {
            errors.add(OtherDomainObjects.ValidationError.builder()
                    .fieldId(component.getFieldName())
                    .message(component.getLabel() + "不能为空")
                    .errorCode("REQUIRED")
                    .level("error")
                    .build());
            return errors;  // 必填验证失败，不再验证其他规则
        }

        if (fieldValue == null) {
            return errors;  // 非必填且值为空，验证通过
        }

        // 2. 验证配置的验证规则
        if (component.getValidationRules() != null) {
            for (ValidationRuleConfig ruleConfig : component.getValidationRules()) {
                if (!ruleConfig.getEnabled()) {
                    continue;
                }

                OtherDomainObjects.ValidationError error = validateRule(component, ruleConfig, fieldValue);
                if (error != null) {
                    errors.add(error);
                }
            }
        }

        return errors;
    }

    /**
     * 验证单个规则
     */
    private OtherDomainObjects.ValidationError validateRule(FormComponent component, ValidationRuleConfig ruleConfig, Object fieldValue) {
        String errorMessage = ruleConfig.getErrorMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = component.getLabel() + "格式不正确";
        }

        switch (ruleConfig.getRuleType()) {
            case "minLength":
                Integer minLength = (Integer) ruleConfig.getParameters().get("min");
                if (fieldValue.toString().length() < minLength) {
                    return OtherDomainObjects.ValidationError.builder()
                            .fieldId(component.getFieldName())
                            .message(errorMessage)
                            .errorCode("MIN_LENGTH")
                            .level("error")
                            .build();
                }
                break;

            case "maxLength":
                Integer maxLength = (Integer) ruleConfig.getParameters().get("max");
                if (fieldValue.toString().length() > maxLength) {
                    return OtherDomainObjects.ValidationError.builder()
                            .fieldId(component.getFieldName())
                            .message(errorMessage)
                            .errorCode("MAX_LENGTH")
                            .level("error")
                            .build();
                }
                break;

            case "pattern":
                String pattern = (String) ruleConfig.getParameters().get("pattern");
                if (!fieldValue.toString().matches(pattern)) {
                    return OtherDomainObjects.ValidationError.builder()
                            .fieldId(component.getFieldName())
                            .message(errorMessage)
                            .errorCode("PATTERN")
                            .level("error")
                            .build();
                }
                break;

            // TODO: 实现更多验证规则
        }

        return null;
    }

    // ==================== 表单模板管理 ====================

    @Override
    public List<FormTemplate> getFormTemplates(String category) {
        log.info("[表单设计器] 获取表单模板: category={}", category);

        return templateLibrary.values().stream()
                .filter(template -> category == null || category.equals(template.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public Long applyTemplate(String templateId, String newFormName, Map<String, Object> customizations) {
        log.info("[表单设计器] 应用模板: templateId={}, newFormName={}", templateId, newFormName);

        FormTemplate template = templateLibrary.get(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateId);
        }

        // 创建新表单
        FormDesignForm form = FormDesignForm.builder()
                .formName(newFormName)
                .formDescription(template.getTemplateDescription())
                .formCategory(template.getCategory())
                .components(template.getComponents())
                .layoutConfig(template.getLayoutConfig())
                .styleConfig(template.getStyleConfig())
                .extendedAttributes(template.getExtendedAttributes())
                .build();

        // 应用自定义配置
        if (customizations != null) {
            applyCustomizations(form, customizations);
        }

        return saveFormDesign(form);
    }

    @Override
    public String uploadTemplate(FormTemplate template) {
        log.info("[表单设计器] 上传自定义模板: templateName={}", template.getTemplateName());

        String templateId = "template_" + System.currentTimeMillis();
        template.setTemplateId(templateId);
        template.setSystemTemplate(false);
        template.setCreateTime(System.currentTimeMillis());

        templateLibrary.put(templateId, template);

        log.info("[表单设计器] 自定义模板上传成功: templateId={}", templateId);
        return templateId;
    }

    // ==================== 表单预览和测试 ====================

    @Override
    public FormPreviewData previewForm(Long formId) {
        log.info("[表单设计器] 预览表单: formId={}", formId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        // 构建表单数据模型（默认值）
        Map<String, Object> formData = new HashMap<>();
        for (FormComponent component : detail.getComponents()) {
            if (component.getDefaultValue() != null) {
                formData.put(component.getFieldName(), component.getDefaultValue());
            }
        }

        // 构建验证规则映射
        Map<String, List<ValidationRuleConfig>> validationRules = new HashMap<>();
        for (FormComponent component : detail.getComponents()) {
            if (component.getValidationRules() != null && !component.getValidationRules().isEmpty()) {
                validationRules.put(component.getFieldName(), component.getValidationRules());
            }
        }

        // 构建表单UI配置
        FormPreviewData.FormUIConfig uiConfig = FormPreviewData.FormUIConfig.builder()
                .width(detail.getStyleConfig() != null ? detail.getStyleConfig().getFormWidth() : "100%")
                .backgroundColor(detail.getStyleConfig() != null ? detail.getStyleConfig().getBackgroundColor() : "#ffffff")
                .padding("20px")
                .build();

        FormPreviewData.FormRenderConfig renderConfig = FormPreviewData.FormRenderConfig.builder()
                .renderMode("preview")
                .showLabel(true)
                .showError(true)
                .showRequired(true)
                .labelWidth(detail.getLayoutConfig() != null ? detail.getLayoutConfig().getLabelWidth() : 120)
                .labelAlign(detail.getLayoutConfig() != null ? detail.getLayoutConfig().getLabelAlign() : "right")
                .build();

        return FormPreviewData.builder()
                .formId(detail.getFormId())
                .formName(detail.getFormName())
                .renderConfig(renderConfig)
                .components(detail.getComponents())
                .formData(formData)
                .validationRules(validationRules)
                .logicConfigs(detail.getLogicConfigs())
                .uiConfig(uiConfig)
                .build();
    }

    @Override
    public OtherDomainObjects.ValidationResult testFormValidation(Long formId, OtherDomainObjects.FormTestData testData) {
        log.info("[表单设计器] 测试表单验证: formId={}", formId);

        return validateFormData(formId, testData.getFormData());
    }

    @Override
    public OtherDomainObjects.FormLogicExecutionResult testFormLogic(Long formId, OtherDomainObjects.FormTestData testData) {
        log.info("[表单设计器] 测试表单逻辑: formId={}", formId);

        return executeFormLogic(formId, testData.getFormData());
    }

    // ==================== 表单版本管理 ====================

    @Override
    public List<FormVersion> getFormVersions(Long formId) {
        log.info("[表单设计器] 获取表单版本历史: formId={}", formId);

        List<FormVersion> versions = versionStorage.get(formId);
        return versions != null ? versions : new ArrayList<>();
    }

    @Override
    @CacheEvict(value = "formDesign", allEntries = true)
    public String publishForm(Long formId) {
        log.info("[表单设计器] 发布表单: formId={}", formId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        // 生成新版本号
        String oldVersion = detail.getVersion();
        String newVersion = incrementVersion(oldVersion);

        // 更新表单状态
        detail.setVersion(newVersion);
        detail.setStatus("published");
        detail.setPublishTime(LocalDateTime.now());

        // 创建版本记录
        List<FormVersion> versions = versionStorage.computeIfAbsent(formId, k -> new ArrayList<>());

        // 取消旧版本的current标记
        versions.forEach(v -> v.setCurrent(false));

        versions.add(FormVersion.builder()
                .versionId(System.currentTimeMillis())
                .formId(formId)
                .version(newVersion)
                .description("发布版本: " + newVersion)
                .status("published")
                .formDesignJson(serializeFormDesign(detail))
                .createUserId(1L)
                .createUserName("Admin")
                .createTime(LocalDateTime.now())
                .publishTime(LocalDateTime.now())
                .current(true)
                .build());

        log.info("[表单设计器] 表单发布成功: formId={}, version={}", formId, newVersion);
        return newVersion;
    }

    @Override
    @CacheEvict(value = "formDesign", allEntries = true)
    public void rollbackFormVersion(Long formId, String version) {
        log.info("[表单设计器] 回滚表单版本: formId={}, version={}", formId, version);

        List<FormVersion> versions = versionStorage.get(formId);
        if (versions == null) {
            throw new RuntimeException("版本历史不存在");
        }

        // 查找目标版本
        FormVersion targetVersion = versions.stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("版本不存在: " + version));

        // 恢复表单设计
        FormDesignForm form = deserializeFormDesign(targetVersion.getFormDesignJson());
        FormDesignDetail detail = formDesignStorage.get(formId);

        detail.setComponents(form.getComponents());
        detail.setLayoutConfig(form.getLayoutConfig());
        detail.setStyleConfig(form.getStyleConfig());
        detail.setSubmitConfig(form.getSubmitConfig());
        detail.setPermissionConfig(form.getPermissionConfig());
        detail.setExtendedAttributes(form.getExtendedAttributes());
        detail.setVersion(version);
        detail.setUpdateTime(LocalDateTime.now());

        log.info("[表单设计器] 表单回滚成功: formId={}, version={}", formId, version);
    }

    // ==================== 表单导入导出 ====================

    @Override
    public Long importForm(FormDesignForm form) {
        log.info("[表单设计器] 导入表单设计: formName={}", form.getFormName());

        return saveFormDesign(form);
    }

    // ==================== 表单统计 ====================

    @Override
    public FormStatistics getFormStatistics(Long formId) {
        log.info("[表单设计器] 获取表单统计: formId={}", formId);

        FormDesignDetail detail = formDesignStorage.get(formId);
        if (detail == null) {
            throw new RuntimeException("表单不存在: " + formId);
        }

        return FormStatistics.builder()
                .formId(detail.getFormId())
                .formName(detail.getFormName())
                .usageCount(detail.getUsageCount())
                .submitCount(0L)  // TODO: 从数据库统计
                .validationFailCount(0L)  // TODO: 从数据库统计
                .avgCompleteTime(0.0)  // TODO: 从数据库统计
                .minCompleteTime(0L)  // TODO: 从数据库统计
                .maxCompleteTime(0L)  // TODO: 从数据库统计
                .lastUsedTime(detail.getUpdateTime() != null ? detail.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .createTime(detail.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                .updateTime(detail.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                .versionCount(versionStorage.getOrDefault(formId, new ArrayList<>()).size())
                .componentCount(detail.getComponents() != null ? detail.getComponents().size() : 0)
                .logicRuleCount(detail.getLogicConfigs() != null ? detail.getLogicConfigs().size() : 0)
                .validationRuleCount((int) detail.getComponents().stream()
                        .filter(c -> c.getValidationRules() != null && !c.getValidationRules().isEmpty())
                        .count())
                .build();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 初始化组件库
     */
    private void initializeComponentLibrary() {
        // 基础组件
        componentLibrary.put("input", createBasicComponent("input", "单行文本", "basic"));
        componentLibrary.put("textarea", createBasicComponent("textarea", "多行文本", "basic"));
        componentLibrary.put("number", createBasicComponent("number", "数字输入", "basic"));
        componentLibrary.put("password", createBasicComponent("password", "密码输入", "basic"));
        componentLibrary.put("select", createBasicComponent("select", "下拉选择", "basic"));
        componentLibrary.put("checkbox", createBasicComponent("checkbox", "复选框", "basic"));
        componentLibrary.put("radio", createBasicComponent("radio", "单选框", "basic"));
        componentLibrary.put("date", createBasicComponent("date", "日期选择", "basic"));
        componentLibrary.put("time", createBasicComponent("time", "时间选择", "basic"));
        componentLibrary.put("file", createBasicComponent("file", "文件上传", "basic"));

        // 高级组件
        componentLibrary.put("richtext", createBasicComponent("richtext", "富文本编辑器", "advanced"));
        componentLibrary.put("codeeditor", createBasicComponent("codeeditor", "代码编辑器", "advanced"));
        componentLibrary.put("treeselect", createBasicComponent("treeselect", "树形选择", "advanced"));
        componentLibrary.put("cascader", createBasicComponent("cascader", "级联选择", "advanced"));
        componentLibrary.put("transfer", createBasicComponent("transfer", "穿梭框", "advanced"));
        componentLibrary.put("slider", createBasicComponent("slider", "滑块", "advanced"));
        componentLibrary.put("rate", createBasicComponent("rate", "评分", "advanced"));
        componentLibrary.put("colorpicker", createBasicComponent("colorpicker", "颜色选择器", "advanced"));
        componentLibrary.put("switch", createBasicComponent("switch", "开关", "advanced"));

        // 业务组件
        componentLibrary.put("userselect", createBasicComponent("userselect", "用户选择", "business"));
        componentLibrary.put("deptselect", createBasicComponent("deptselect", "部门选择", "business"));
        componentLibrary.put("employeeselect", createBasicComponent("employeeselect", "员工选择", "business"));
        componentLibrary.put("areaselect", createBasicComponent("areaselect", "区域选择", "business"));
        componentLibrary.put("deviceSelect", createBasicComponent("deviceSelect", "设备选择", "business"));
        componentLibrary.put("orgselect", createBasicComponent("orgselect", "组织选择", "business"));

        log.info("[表单设计器] 组件库初始化完成: count={}", componentLibrary.size());
    }

    /**
     * 创建基础组件
     */
    private FormComponent createBasicComponent(String componentType, String label, String category) {
        return FormComponent.builder()
                .componentId(componentType)
                .componentType(componentType)
                .category(category)
                .fieldName(componentType + "_field")
                .label(label)
                .placeholder("请输入" + label)
                .required(false)
                .disabled(false)
                .readonly(false)
                .hidden(false)
                .span(24)
                .sortOrder(0)
                .build();
    }

    /**
     * 初始化模板库
     */
    private void initializeTemplateLibrary() {
        // 请假审批表单模板
        FormTemplate leaveTemplate = FormTemplate.builder()
                .templateId("leave_request")
                .templateName("请假审批表单")
                .templateDescription("员工请假申请表单，包含请假类型、时间、原因等信息")
                .category("leave")
                .tags(Arrays.asList("人事管理", "请假"))
                .thumbnailUrl("/templates/leave_request.png")
                .systemTemplate(true)
                .usageCount(0L)
                .rating(5.0)
                .createTime(System.currentTimeMillis())
                .build();

        templateLibrary.put("leave_request", leaveTemplate);

        // 报销审批表单模板
        FormTemplate reimbursementTemplate = FormTemplate.builder()
                .templateId("reimbursement")
                .templateName("报销审批表单")
                .templateDescription("费用报销申请表单，包含报销类型、金额、事由等信息")
                .category("reimbursement")
                .tags(Arrays.asList("财务管理", "报销"))
                .thumbnailUrl("/templates/reimbursement.png")
                .systemTemplate(true)
                .usageCount(0L)
                .rating(5.0)
                .createTime(System.currentTimeMillis())
                .build();

        templateLibrary.put("reimbursement", reimbursementTemplate);

        log.info("[表单设计器] 模板库初始化完成: count={}", templateLibrary.size());
    }

    /**
     * 构建组件配置Schema
     */
    private ComponentConfigSchema buildComponentConfigSchema(String componentType) {
        // TODO: 根据组件类型构建详细的配置Schema
        return ComponentConfigSchema.builder()
                .componentType(componentType)
                .componentName(getComponentName(componentType))
                .description("组件配置")
                .baseProperties(new ArrayList<>())
                .validationProperties(new ArrayList<>())
                .styleProperties(new ArrayList<>())
                .advancedProperties(new ArrayList<>())
                .build();
    }

    /**
     * 获取组件名称
     */
    private String getComponentName(String componentType) {
        Map<String, String> componentNames = new HashMap<>();
        componentNames.put("input", "单行文本");
        componentNames.put("textarea", "多行文本");
        componentNames.put("number", "数字输入");
        componentNames.put("select", "下拉选择");
        // ... 其他组件

        return componentNames.getOrDefault(componentType, componentType);
    }

    /**
     * 转换为VO对象
     */
    private FormDesignVO convertToVO(FormDesignDetail detail) {
        return FormDesignVO.builder()
                .formId(detail.getFormId())
                .formName(detail.getFormName())
                .formDescription(detail.getFormDescription())
                .formCategory(detail.getFormCategory())
                .version(detail.getVersion())
                .status(detail.getStatus())
                .componentCount(detail.getComponents() != null ? detail.getComponents().size() : 0)
                .createUserId(detail.getCreateUserId())
                .createUserName(detail.getCreateUserName())
                .createTime(detail.getCreateTime())
                .updateTime(detail.getUpdateTime())
                .publishTime(detail.getPublishTime())
                .usageCount(detail.getUsageCount())
                .build();
    }

    /**
     * 生成JSON Schema
     */
    private String generateJsonSchema(FormDesignDetail detail) {
        try {
            Map<String, Object> schema = new HashMap<>();
            schema.put("formId", detail.getFormId());
            schema.put("formName", detail.getFormName());
            schema.put("fields", extractFields(detail));

            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            log.error("[表单设计器] 生成JSON Schema失败", e);
            return "{}";
        }
    }

    /**
     * 提取字段定义
     */
    private List<Map<String, Object>> extractFields(FormDesignDetail detail) {
        List<Map<String, Object>> fields = new ArrayList<>();

        for (FormComponent component : detail.getComponents()) {
            Map<String, Object> field = new HashMap<>();
            field.put("fieldName", component.getFieldName());
            field.put("label", component.getLabel());
            field.put("componentType", component.getComponentType());
            field.put("required", component.getRequired());
            fields.add(field);
        }

        return fields;
    }

    /**
     * 应用自定义配置
     */
    private void applyCustomizations(FormDesignForm form, Map<String, Object> customizations) {
        // TODO: 实现自定义配置应用逻辑
    }

    /**
     * 序列化表单设计
     */
    private String serializeFormDesign(FormDesignForm form) {
        try {
            return objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException e) {
            log.error("[表单设计器] 序列化表单设计失败", e);
            return "{}";
        }
    }

    /**
     * 序列化表单设计
     */
    private String serializeFormDesign(FormDesignDetail detail) {
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            log.error("[表单设计器] 序列化表单设计失败", e);
            return "{}";
        }
    }

    /**
     * 反序列化表单设计
     */
    private FormDesignForm deserializeFormDesign(String json) {
        try {
            return objectMapper.readValue(json, FormDesignForm.class);
        } catch (JsonProcessingException e) {
            log.error("[表单设计器] 反序列化表单设计失败", e);
            throw new RuntimeException("反序列化表单设计失败", e);
        }
    }

    /**
     * 版本号递增
     */
    private String incrementVersion(String version) {
        String[] parts = version.split("\\.");
        int patch = Integer.parseInt(parts[2]);
        return parts[0] + "." + parts[1] + "." + (patch + 1);
    }
}

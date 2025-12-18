package net.lab1024.sa.oa.workflow.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * JSON Schema 表单引擎服务
 * <p>
 * Flowable 7.2.0已移除内置Form引擎，本服务使用JSON Schema替代，
 * 提供企业级表单管理功能，支持动态表单定义、表单实例管理、
 * 表单验证、表单渲染、表单版本控制等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - JSON Schema版本
 * @since 2025-01-16
 */
@Slf4j
@Service
public class FormEngineService {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private FormSchemaRepository formSchemaRepository;

    @Resource
    private FormInstanceRepository formInstanceRepository;

    /**
     * 部署表单定义
     *
     * @param formSchema JSON Schema格式的表单定义
     * @param formDefinitionKey 表单定义键
     * @param formDefinitionName 表单定义名称
     * @return 部署结果（表单定义ID）
     */
    public String deployFormDefinition(String formSchema, String formDefinitionKey, String formDefinitionName) {
        try {
            log.info("[表单引擎] 开始部署表单定义: key={}, name={}", formDefinitionKey, formDefinitionName);

            // 验证JSON Schema格式
            JsonNode schemaNode = objectMapper.readTree(formSchema);
            if (!isValidFormSchema(schemaNode)) {
                throw new RuntimeException("无效的表单Schema格式");
            }

            // 查找当前最新版本
            int latestVersion = formSchemaRepository.findLatestVersionByKey(formDefinitionKey);
            int newVersion = latestVersion + 1;

            // 创建表单定义记录
            FormSchemaEntity formSchemaEntity = new FormSchemaEntity();
            formSchemaEntity.setFormKey(formDefinitionKey);
            formSchemaEntity.setFormName(formDefinitionName);
            formSchemaEntity.setFormSchema(formSchema);
            formSchemaEntity.setVersion(newVersion);
            formSchemaEntity.setStatus(1); // 1-启用
            formSchemaEntity.setCreateTime(LocalDateTime.now());
            formSchemaEntity.setUpdateTime(LocalDateTime.now());

            formSchemaRepository.insert(formSchemaEntity);

            log.info("[表单引擎] 表单定义部署成功: id={}, key={}, version={}",
                    formSchemaEntity.getId(), formDefinitionKey, newVersion);
            return formSchemaEntity.getId().toString();

        } catch (Exception e) {
            log.error("[表单引擎] 表单定义部署失败: key={}, error={}", formDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("表单定义部署失败", e);
        }
    }

    /**
     * 启动表单实例
     *
     * @param formDefinitionKey 表单定义键
     * @param processInstanceId 流程实例ID
     * @param taskId 任务ID
     * @param variables 表单变量
     * @param userId 用户ID
     * @return 表单实例ID
     */
    public String startFormInstance(String formDefinitionKey, String processInstanceId, String taskId,
                                     Map<String, Object> variables, String userId) {
        try {
            log.info("[表单引擎] 开始启动表单实例: formKey={}, processInstanceId={}, taskId={}",
                    formDefinitionKey, processInstanceId, taskId);

            // 查找最新表单定义
            FormSchemaEntity formSchema = formSchemaRepository.findLatestByKey(formDefinitionKey);
            if (formSchema == null) {
                throw new RuntimeException("表单定义不存在: " + formDefinitionKey);
            }

            // 准备表单变量
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put("processInstanceId", processInstanceId);
            variables.put("taskId", taskId);
            variables.put("userId", userId);
            variables.put("startTime", LocalDateTime.now().toString());

            // 创建表单实例
            FormInstanceEntity formInstance = new FormInstanceEntity();
            formInstance.setFormSchemaId(formSchema.getId());
            formInstance.setFormKey(formDefinitionKey);
            formInstance.setProcessInstanceId(processInstanceId);
            formInstance.setTaskId(taskId);
            formInstance.setFormData(objectMapper.writeValueAsString(variables));
            formInstance.setSubmitterId(Long.parseLong(userId));
            formInstance.setStatus(1); // 1-进行中
            formInstance.setCreateTime(LocalDateTime.now());
            formInstance.setUpdateTime(LocalDateTime.now());

            formInstanceRepository.insert(formInstance);

            log.info("[表单引擎] 表单实例启动成功: formInstanceId={}, formSchemaId={}",
                    formInstance.getId(), formSchema.getId());

            return formInstance.getId().toString();

        } catch (Exception e) {
            log.error("[表单引擎] 表单实例启动失败: formKey={}, error={}", formDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("表单实例启动失败", e);
        }
    }

    /**
     * 提交表单实例
     *
     * @param formInstanceId 表单实例ID
     * @param formVariables 表单变量
     * @param userId 用户ID
     * @return 提交结果
     */
    public boolean submitFormInstance(String formInstanceId, Map<String, Object> formVariables, String userId) {
        try {
            log.info("[表单引擎] 开始提交表单实例: formInstanceId={}, userId={}", formInstanceId, userId);

            FormInstanceEntity formInstance = formInstanceRepository.selectById(Long.parseLong(formInstanceId));
            if (formInstance == null) {
                throw new RuntimeException("表单实例不存在: " + formInstanceId);
            }

            // 合并表单数据
            @SuppressWarnings("unchecked")
            Map<String, Object> existingData = objectMapper.readValue(
                    formInstance.getFormData(), Map.class);
            if (formVariables != null && !formVariables.isEmpty()) {
                existingData.putAll(formVariables);
            }
            existingData.put("submitterId", userId);
            existingData.put("submitTime", LocalDateTime.now().toString());

            // 验证表单数据
            FormSchemaEntity formSchema = formSchemaRepository.selectById(formInstance.getFormSchemaId());
            Map<String, Object> validationResult = validateFormData(formSchema.getFormSchema(), existingData);
            if (!(Boolean) validationResult.get("valid")) {
                throw new RuntimeException("表单验证失败: " + validationResult.get("errors"));
            }

            // 更新表单实例
            formInstance.setFormData(objectMapper.writeValueAsString(existingData));
            formInstance.setStatus(2); // 2-已提交
            formInstance.setSubmitTime(LocalDateTime.now());
            formInstance.setUpdateTime(LocalDateTime.now());

            formInstanceRepository.updateById(formInstance);

            log.info("[表单引擎] 表单实例提交成功: formInstanceId={}", formInstanceId);
            return true;

        } catch (Exception e) {
            log.error("[表单引擎] 表单实例提交失败: formInstanceId={}, error={}", formInstanceId, e.getMessage(), e);
            throw new RuntimeException("表单实例提交失败", e);
        }
    }

    /**
     * 验证表单数据
     *
     * @param formSchemaJson 表单Schema JSON
     * @param formData 表单数据
     * @return 验证结果
     */
    public Map<String, Object> validateFormData(String formSchemaJson, Map<String, Object> formData) {
        Map<String, Object> validationResult = new HashMap<>();
        validationResult.put("valid", true);
        List<String> errors = new ArrayList<>();

        try {
            JsonNode schemaNode = objectMapper.readTree(formSchemaJson);
            JsonNode propertiesNode = schemaNode.get("properties");
            JsonNode requiredNode = schemaNode.get("required");

            // 验证必填字段
            if (requiredNode != null && requiredNode.isArray()) {
                for (JsonNode requiredField : requiredNode) {
                    String fieldName = requiredField.asText();
                    if (!formData.containsKey(fieldName) || formData.get(fieldName) == null ||
                            formData.get(fieldName).toString().trim().isEmpty()) {
                        errors.add("字段 '" + fieldName + "' 是必填项");
                        validationResult.put("valid", false);
                    }
                }
            }

            // 验证字段类型
            if (propertiesNode != null) {
                propertiesNode.fields().forEachRemaining(entry -> {
                    String fieldName = entry.getKey();
                    JsonNode fieldSchema = entry.getValue();
                    if (formData.containsKey(fieldName) && formData.get(fieldName) != null) {
                        String expectedType = fieldSchema.has("type") ? fieldSchema.get("type").asText() : "string";
                        Object value = formData.get(fieldName);
                        if (!isValidType(value, expectedType)) {
                            errors.add("字段 '" + fieldName + "' 类型不正确，期望: " + expectedType);
                            validationResult.put("valid", false);
                        }
                    }
                });
            }

        } catch (Exception e) {
            log.error("[表单引擎] 表单验证异常: {}", e.getMessage());
            errors.add("表单验证异常: " + e.getMessage());
            validationResult.put("valid", false);
        }

        validationResult.put("errors", errors);
        return validationResult;
    }

    /**
     * 渲染表单
     *
     * @param formInstanceId 表单实例ID
     * @param taskType 任务类型
     * @param userId 用户ID
     * @return 渲染后的表单数据
     */
    public Map<String, Object> renderForm(String formInstanceId, String taskType, String userId) {
        try {
            log.debug("[表单引擎] 开始渲染表单: formInstanceId={}, taskType={}, userId={}",
                    formInstanceId, taskType, userId);

            Map<String, Object> renderResult = new HashMap<>();

            FormInstanceEntity formInstance = formInstanceRepository.selectById(Long.parseLong(formInstanceId));
            if (formInstance == null) {
                throw new RuntimeException("表单实例不存在: " + formInstanceId);
            }

            FormSchemaEntity formSchema = formSchemaRepository.selectById(formInstance.getFormSchemaId());

            // 构建表单渲染数据
            renderResult.put("formInstanceId", formInstanceId);
            renderResult.put("formDefinitionKey", formSchema.getFormKey());
            renderResult.put("formDefinitionName", formSchema.getFormName());
            renderResult.put("formDefinitionVersion", formSchema.getVersion());
            renderResult.put("formSchema", objectMapper.readTree(formSchema.getFormSchema()));
            renderResult.put("formData", objectMapper.readValue(formInstance.getFormData(), Map.class));
            renderResult.put("taskType", taskType);
            renderResult.put("userId", userId);
            renderResult.put("renderTime", LocalDateTime.now().toString());

            // 根据任务类型调整渲染配置
            boolean isReadonly = "readonly".equals(taskType) || formInstance.getStatus() == 2;
            renderResult.put("readonly", isReadonly);
            renderResult.put("editable", !isReadonly);

            log.debug("[表单引擎] 表单渲染完成: formInstanceId={}", formInstanceId);
            return renderResult;

        } catch (Exception e) {
            log.error("[表单引擎] 表单渲染失败: formInstanceId={}, error={}", formInstanceId, e.getMessage(), e);
            throw new RuntimeException("表单渲染失败", e);
        }
    }

    /**
     * 获取表单定义列表
     *
     * @return 表单定义列表
     */
    public List<FormSchemaEntity> getFormDefinitions() {
        try {
            log.debug("[表单引擎] 获取表单定义列表");
            List<FormSchemaEntity> formSchemas = formSchemaRepository.findLatestVersions();
            log.debug("[表单引擎] 获取表单定义列表完成: count={}", formSchemas.size());
            return formSchemas;
        } catch (Exception e) {
            log.error("[表单引擎] 获取表单定义列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取表单定义列表失败", e);
        }
    }

    /**
     * 获取表单实例列表
     *
     * @param processInstanceId 流程实例ID
     * @param taskId 任务ID
     * @param submitterId 提交者ID
     * @return 表单实例列表
     */
    public List<FormInstanceEntity> getFormInstances(String processInstanceId, String taskId, String submitterId) {
        try {
            log.debug("[表单引擎] 获取表单实例列表: processInstanceId={}, taskId={}, submitterId={}",
                    processInstanceId, taskId, submitterId);

            List<FormInstanceEntity> formInstances = formInstanceRepository.findByConditions(
                    processInstanceId, taskId, submitterId != null ? Long.parseLong(submitterId) : null);

            log.debug("[表单引擎] 获取表单实例列表完成: count={}", formInstances.size());
            return formInstances;
        } catch (Exception e) {
            log.error("[表单引擎] 获取表单实例列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取表单实例列表失败", e);
        }
    }

    /**
     * 获取表单Schema信息
     *
     * @param formDefinitionKey 表单定义键
     * @return 表单Schema信息
     */
    public Map<String, Object> getFormInfo(String formDefinitionKey) {
        try {
            log.debug("[表单引擎] 获取表单Schema信息: formDefinitionKey={}", formDefinitionKey);

            FormSchemaEntity formSchema = formSchemaRepository.findLatestByKey(formDefinitionKey);
            if (formSchema == null) {
                throw new RuntimeException("表单定义不存在: " + formDefinitionKey);
            }

            Map<String, Object> formInfo = new HashMap<>();
            formInfo.put("id", formSchema.getId());
            formInfo.put("formKey", formSchema.getFormKey());
            formInfo.put("formName", formSchema.getFormName());
            formInfo.put("version", formSchema.getVersion());
            formInfo.put("status", formSchema.getStatus());
            formInfo.put("schema", objectMapper.readTree(formSchema.getFormSchema()));
            formInfo.put("createTime", formSchema.getCreateTime());

            log.debug("[表单引擎] 获取表单Schema信息完成: formDefinitionKey={}", formDefinitionKey);
            return formInfo;

        } catch (Exception e) {
            log.error("[表单引擎] 获取表单Schema信息失败: formDefinitionKey={}, error={}",
                    formDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("获取表单Schema信息失败", e);
        }
    }

    /**
     * 删除表单实例
     *
     * @param formInstanceId 表单实例ID
     * @return 删除结果
     */
    public boolean deleteFormInstance(String formInstanceId) {
        try {
            log.info("[表单引擎] 开始删除表单实例: formInstanceId={}", formInstanceId);

            formInstanceRepository.deleteById(Long.parseLong(formInstanceId));

            log.info("[表单引擎] 表单实例删除成功: formInstanceId={}", formInstanceId);
            return true;

        } catch (Exception e) {
            log.error("[表单引擎] 表单实例删除失败: formInstanceId={}, error={}", formInstanceId, e.getMessage(), e);
            throw new RuntimeException("表单实例删除失败", e);
        }
    }

    // ========== 私有方法 ==========

    /**
     * 验证表单Schema格式是否有效
     */
    private boolean isValidFormSchema(JsonNode schemaNode) {
        // 基本验证：必须包含type和properties
        return schemaNode != null &&
                schemaNode.has("type") &&
                "object".equals(schemaNode.get("type").asText()) &&
                schemaNode.has("properties");
    }

    /**
     * 验证值类型是否匹配
     */
    private boolean isValidType(Object value, String expectedType) {
        switch (expectedType) {
            case "string":
                return value instanceof String;
            case "number":
            case "integer":
                return value instanceof Number;
            case "boolean":
                return value instanceof Boolean;
            case "array":
                return value instanceof List;
            case "object":
                return value instanceof Map;
            default:
                return true;
        }
    }
}

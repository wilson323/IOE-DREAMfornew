package net.lab1024.sa.oa.workflow.form;

import lombok.extern.slf4j.Slf4j;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.form.api.FormService;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormInstance;
import org.flowable.form.model.FormInfo;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Flowable表单引擎服务
 * <p>
 * 提供企业级表单管理功能，支持表单定义部署、表单实例管理、
 * 表单验证、表单渲染、表单版本控制等高级功能
 * 集成Flowable 6.8.0表单引擎特性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class FormEngineService {

    @Resource
    private FormRepositoryService formRepositoryService;

    @Resource
    private FormService formService;

    /**
     * 部署表单定义
     *
     * @param formDefinitionXml 表单定义XML
     * @param formDefinitionKey 表单定义键
     * @param formDefinitionName 表单定义名称
     * @return 部署结果
     */
    public String deployFormDefinition(String formDefinitionXml, String formDefinitionKey, String formDefinitionName) {
        try {
            log.info("[表单引擎] 开始部署表单定义: key={}, name={}", formDefinitionKey, formDefinitionName);

            InputStream inputStream = new java.io.ByteArrayInputStream(formDefinitionXml.getBytes());

            org.flowable.form.api.repository.DeploymentBuilder deploymentBuilder =
                formRepositoryService.createDeployment()
                    .addInputStream(formDefinitionKey + ".form", inputStream)
                    .name(formDefinitionName)
                    .key(formDefinitionKey);

            String deploymentId = deploymentBuilder.deploy().getId();

            log.info("[表单引擎] 表单定义部署成功: deploymentId={}, key={}", deploymentId, formDefinitionKey);
            return deploymentId;

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

            // 查找表单定义
            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
                    .formDefinitionKey(formDefinitionKey)
                    .latestVersion()
                    .singleResult();

            if (formDefinition == null) {
                throw new RuntimeException("表单定义不存在: " + formDefinitionKey);
            }

            // 准备表单变量
            if (variables == null) {
                variables = new HashMap<>();
            }

            variables.put("processInstanceId", processInstanceId);
            variables.put("taskId", taskId);
            variables.put("userId", userId);
            variables.put("startTime", LocalDateTime.now());

            // 启动表单实例
            String formInstanceId = formService.createFormInstanceBuilder()
                    .formDefinitionId(formDefinition.getId())
                    .processInstanceId(processInstanceId)
                    .taskId(taskId)
                    .variables(variables)
                    .start();

            log.info("[表单引擎] 表单实例启动成功: formInstanceId={}, formDefinitionId={}",
                    formInstanceId, formDefinition.getId());

            return formInstanceId;

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

            FormInstance formInstance = formService.getFormInstance(formInstanceId);
            if (formInstance == null) {
                throw new RuntimeException("表单实例不存在: " + formInstanceId);
            }

            // 更新表单变量
            if (formVariables != null && !formVariables.isEmpty())) {
                formService.setFormInstanceVariables(formInstanceId, formVariables);
            }

            // 设置提交者
            if (userId != null && !userId.isEmpty()) {
                formService.setFormInstanceVariable(formInstanceId, "submitterId", userId);
            }

            // 设置提交时间
            formService.setFormInstanceVariable(formInstanceId, "submitTime", LocalDateTime.now());

            // 完成表单实例
            formService.completeFormInstance(formInstanceId);

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
     * @param formInstanceId 表单实例ID
     * @param formData 表单数据
     * @return 验证结果
     */
    public Map<String, Object> validateFormData(String formInstanceId, Map<String, Object> formData) {
        try {
            log.debug("[表单引擎] 开始验证表单数据: formInstanceId={}", formInstanceId);

            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("valid", true);
            validationResult.put("errors", new ArrayList<>());

            FormInstance formInstance = formService.getFormInstance(formInstanceId);
            if (formInstance == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("valid", false);
                Map<String, String> errors = new HashMap<>();
                errors.put("formInstanceId", "表单实例不存在: " + formInstanceId);
                errorResult.put("errors", errors);
                return errorResult;
            }

            // 根据表单定义验证数据
            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
                    .formDefinitionId(formInstance.getFormDefinitionId())
                    .singleResult();

            if (formDefinition != null) {
                validationResult = validateFormFields(formDefinition, formData);
            }

            log.debug("[表单引擎] 表单数据验证完成: formInstanceId={}, valid={}",
                    formInstanceId, validationResult.get("valid"));
            return validationResult;

        } catch (Exception e) {
            log.error("[表单引擎] 表单数据验证失败: formInstanceId={}, error={}", formInstanceId, e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("valid", false);
            Map<String, String> errors = new HashMap<>();
            errors.put("validationError", e.getMessage());
            errorResult.put("errors", errors);
            return errorResult;
        }
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
            log.debug("[表单引擎] 开始渲染表单: formInstanceId={}, taskType={}, userId={}", formInstanceId, taskType, userId);

            Map<String, Object> renderResult = new HashMap<>();

            FormInstance formInstance = formService.getFormInstance(formInstanceId);
            if (formInstance == null) {
                throw new RuntimeException("表单实例不存在: " + formInstanceId);
            }

            // 获取表单定义
            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
                    .formDefinitionId(formInstance.getFormDefinitionId())
                    .singleResult();

            // 获取表单变量
            Map<String, Object> formVariables = formService.getFormInstanceVariables(formInstanceId);

            // 构建表单渲染数据
            renderResult.put("formInstanceId", formInstanceId);
            renderResult.put("formDefinitionKey", formDefinition.getKey());
            renderResult("formDefinitionName", formDefinition.getName());
            renderResult("formDefinitionVersion", formDefinition.getVersion());
            renderResult("formData", formVariables);
            renderResult("taskType", taskType);
            renderResult("userId", userId);
            renderResult("renderTime", LocalDateTime.now());

            // 根据任务类型调整渲染配置
            if ("readonly".equals(taskType)) {
                renderResult("readonly", true);
                renderResult("editable", false);
            } else {
                renderResult("readonly", false);
                renderResult("editable", true);
            }

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
    public List<FormDefinition> getFormDefinitions() {
        try {
            log.debug("[表单引擎] 获取表单定义列表");

            List<FormDefinition> formDefinitions = formRepositoryService.createFormDefinitionQuery()
                    .latestVersion()
                    .orderByFormDefinitionVersion().desc()
                    .list();

            log.debug("[表单引擎] 获取表单定义列表完成: count={}", formDefinitions.size());
            return formDefinitions;

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
    public List<FormInstance> getFormInstances(String processInstanceId, String taskId, String submitterId) {
        try {
            log.debug("[表单引擎] 获取表单实例列表: processInstanceId={}, taskId={}, submitterId={}",
                    processInstanceId, taskId, submitterId);

            org.flowable.form.api.FormInstanceQuery query = formService.createFormInstanceQuery();

            if (processInstanceId != null && !processInstanceId.isEmpty()) {
                query.processInstanceId(processInstanceId);
            }

            if (taskId != null && !taskId.isEmpty()) {
                query.taskId(taskId);
            }

            if (submitterId != null && !submitterId.isEmpty()) {
                query.submitterId(submitterId);
            }

            query.orderByStartTime().desc();

            List<FormInstance> formInstances = query.list();

            log.debug("[表单引擎] 获取表单实例列表完成: count={}", formInstances.size());
            return formInstances;

        } catch (Exception e) {
            log.error("[表单引擎] 获取表单实例列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取表单实例列表失败", e);
        }
    }

    /**
     * 获取表单表单信息
     *
     * @param formDefinitionKey 表单定义键
     * @return 表单表单信息
     */
    public FormInfo getFormInfo(String formDefinitionKey) {
        try {
            log.debug("[表单引擎] 获取表单表单信息: formDefinitionKey={}", formDefinitionKey);

            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
                    .formDefinitionKey(formDefinitionKey)
                    .latestVersion()
                    .singleResult();

            if (formDefinition == null) {
                throw new RuntimeException("表单定义不存在: " + formDefinitionKey);
            }

            FormInfo formInfo = new FormInfo();
            formInfo.setFormDefinitionKey(formDefinition.getKey());
            formInfo.setFormDefinitionName(formDefinition.getName());
            formInfo.setFormDefinitionVersion(formDefinition.getVersion());
            formInfo.setFormDefinitionId(formDefinition.getId());
            formInfo.setFormDefinitionDeploymentId(formDefinition.getDeploymentId());
            formInfo.setFormDefinitionTenantId(formDefinition.getTenantId());

            log.debug("[表单引擎] 获取表单表单信息完成: formDefinitionKey={}", formDefinitionKey);
            return formInfo;

        } catch (Exception e) {
            log.error("[表单引擎] 获取表单表单信息失败: formDefinitionKey={}, error={}", formDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("获取表单表单信息失败", e);
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

            formService.deleteFormInstance(formInstanceId);

            log.info("[表单引擎] 表单实例删除成功: formInstanceId={}", formInstanceId);
            return true;

        } catch (Exception e) {
            log.error("[表单引擎] 表单实例删除失败: formInstanceId={}, error={}", formInstanceId, e.getMessage(), e);
            throw new RuntimeException("表单实例删除失败", e);
        }
    }

    // ========== 私有方法 ==========

    private Map<String, Object> validateFormFields(FormDefinition formDefinition, Map<String, Object> formData) {
        Map<String, Object> validationResult = new HashMap<>();
        validationResult.put("valid", true);
        validationResult.put("errors", new ArrayList<>());

        // 这里可以根据表单定义的字段配置进行验证
        // 示例：必填字段验证、数据类型验证、格式验证等

        return validationResult;
    }
}
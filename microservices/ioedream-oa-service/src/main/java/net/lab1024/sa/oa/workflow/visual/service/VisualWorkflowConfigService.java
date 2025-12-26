package net.lab1024.sa.oa.workflow.visual.service;

import net.lab1024.sa.oa.workflow.visual.domain.*;

import java.util.List;

/**
 * 可视化工作流配置服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface VisualWorkflowConfigService {

    // ==================== 流程配置CRUD ====================

    /**
     * 获取可视化流程配置
     *
     * @param processDefinitionId 流程定义ID
     * @return 可视化配置
     */
    VisualWorkflowConfig getVisualConfig(String processDefinitionId);

    /**
     * 保存可视化流程配置
     *
     * @param form 配置表单
     * @return 验证错误列表（空表示验证通过）
     */
    List<ValidationError> validateConfig(VisualWorkflowConfigForm form);

    /**
     * 生成BPMN XML
     *
     * @param form 可视化配置表单
     * @return BPMN XML字符串
     */
    String generateBpmnXml(VisualWorkflowConfigForm form);

    /**
     * 部署流程
     *
     * @param processKey    流程Key
     * @param processName   流程名称
     * @param processCategory 流程分类
     * @param bpmnXml       BPMN XML
     * @return 流程定义ID
     */
    String deployProcess(String processKey, String processName, String processCategory, String bpmnXml);

    /**
     * 更新流程配置
     *
     * @param processDefinitionId 流程定义ID
     * @param form 配置表单
     */
    void updateProcessConfig(String processDefinitionId, VisualWorkflowConfigForm form);

    // ==================== 流程模板库 ====================

    /**
     * 获取流程模板列表
     *
     * @param category 分类（可选）
     * @return 模板列表
     */
    List<ProcessTemplate> getProcessTemplates(String category);

    /**
     * 获取流程模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    ProcessTemplateDetail getTemplateDetail(String templateId);

    /**
     * 应用模板创建流程
     *
     * @param templateId       模板ID
     * @param newProcessKey    新流程Key
     * @param newProcessName   新流程名称
     * @param customizations   自定义配置
     * @return 新流程定义ID
     */
    String applyTemplate(String templateId, String newProcessKey, String newProcessName,
                        java.util.Map<String, Object> customizations);

    /**
     * 上传自定义模板
     *
     * @param templateId  模板ID
     * @param templateName 模板名称
     * @param category    分类
     * @param bpmnXml     BPMN XML
     * @param screenshot  截图
     * @return 上传的模板ID
     */
    String uploadTemplate(String templateId, String templateName, String category,
                        String bpmnXml, byte[] screenshot);

    // ==================== 流程验证和诊断 ====================

    /**
     * 验证流程配置
     *
     * @param form 配置表单
     * @return 验证结果
     */
    ValidationResult validateConfig(VisualWorkflowConfigForm form);

    /**
     * 诊断流程问题
     *
     * @param processDefinitionId 流程定义ID
     * @return 问题列表
     */
    List<WorkflowIssue> diagnoseWorkflow(String processDefinitionId);

    /**
     * 流程仿真
     *
     * @param request 仿真请求
     * @return 仿真结果
     */
    WorkflowSimulationResult simulateWorkflow(WorkflowSimulationRequest request);

    // ==================== 组件库 ====================

    /**
     * 获取节点类型库
     *
     * @return 节点类型列表
     */
    List<NodeType> getNodeTypes();

    /**
     * 获取节点配置Schema
     *
     * @param nodeType 节点类型
     * @return 配置Schema
     */
    NodeConfigSchema getNodeConfigSchema(String nodeType);

    // ==================== 导入导出 ====================

    /**
     * 导入流程配置
     *
     * @param file 配置文件
     * @return 新流程定义ID
     */
    String importConfig(org.springframework.web.multipart.MultipartFile file);
}

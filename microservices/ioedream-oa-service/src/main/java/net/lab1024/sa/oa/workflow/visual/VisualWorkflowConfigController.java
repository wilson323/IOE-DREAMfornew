package net.lab1024.sa.oa.workflow.visual;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.visual.domain.*;
import net.lab1024.sa.oa.workflow.visual.service.VisualWorkflowConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 可视化工作流配置控制器
 *
 * 提供拖拽式流程配置界面，支持：
 * 1. 流程图可视化编辑
 * 2. 节点属性配置
 * 3. 连线条件配置
 * 4. 流程模板库
 * 5. 流程验证和诊断
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oa/workflow/visual-config")
@Validated
public class VisualWorkflowConfigController {

    @Resource
    private VisualWorkflowConfigService visualWorkflowConfigService;

    // ==================== 流程配置 CRUD ====================

    /**
     * 获取可视化流程配置
     *
     * @param processDefinitionId 流程定义ID
     * @return 可视化配置
     */
    @GetMapping("/{processDefinitionId}")
    public ResponseDTO<VisualWorkflowConfig> getVisualConfig(
            @PathVariable String processDefinitionId) {

        log.info("[可视化配置] 获取流程配置: processDefinitionId={}", processDefinitionId);

        VisualWorkflowConfig config = visualWorkflowConfigService.getVisualConfig(processDefinitionId);

        return ResponseDTO.ok(config);
    }

    /**
     * 保存可视化流程配置
     *
     * @param form 配置表单
     * @return 操作结果
     */
    @PostMapping("/save")
    public ResponseDTO<String> saveVisualConfig(
            @RequestBody @Valid VisualWorkflowConfigForm form) {

        log.info("[可视化配置] 保存流程配置: processKey={}, processName={}",
                form.getProcessKey(), form.getProcessName());

        // 1. 验证配置合法性
        List<ValidationError> errors = visualWorkflowConfigService.validateConfig(form);
        if (!errors.isEmpty()) {
            return ResponseDTO.error("CONFIG_VALIDATION_ERROR",
                    "配置验证失败: " + errors.get(0).getMessage());
        }

        // 2. 生成BPMN XML
        String bpmnXml = visualWorkflowConfigService.generateBpmnXml(form);

        // 3. 部署流程
        String processDefinitionId = visualWorkflowConfigService.deployProcess(
                form.getProcessKey(),
                form.getProcessName(),
                form.getProcessCategory(),
                bpmnXml
        );

        log.info("[可视化配置] 流程部署成功: processDefinitionId={}", processDefinitionId);

        return ResponseDTO.ok(processDefinitionId);
    }

    /**
     * 更新流程配置
     *
     * @param processDefinitionId 流程定义ID
     * @param form 配置表单
     * @return 操作结果
     */
    @PostMapping("/{processDefinitionId}/update")
    public ResponseDTO<Void> updateVisualConfig(
            @PathVariable String processDefinitionId,
            @RequestBody @Valid VisualWorkflowConfigForm form) {

        log.info("[可视化配置] 更新流程配置: processDefinitionId={}", processDefinitionId);

        visualWorkflowConfigService.updateProcessConfig(processDefinitionId, form);

        return ResponseDTO.ok();
    }

    // ==================== 流程模板库 ====================

    /**
     * 获取流程模板列表
     *
     * @param category 分类（可选）
     * @return 模板列表
     */
    @GetMapping("/templates")
    public ResponseDTO<List<ProcessTemplate>> getProcessTemplates(
            @RequestParam(required = false) String category) {

        log.info("[可视化配置] 获取流程模板: category={}", category);

        List<ProcessTemplate> templates = visualWorkflowConfigService.getProcessTemplates(category);

        return ResponseDTO.ok(templates);
    }

    /**
     * 获取流程模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    @GetMapping("/templates/{templateId}")
    public ResponseDTO<ProcessTemplateDetail> getTemplateDetail(
            @PathVariable String templateId) {

        log.info("[可视化配置] 获取模板详情: templateId={}", templateId);

        ProcessTemplateDetail detail = visualWorkflowConfigService.getTemplateDetail(templateId);

        return ResponseDTO.ok(detail);
    }

    /**
     * 使用模板创建流程
     *
     * @param form 应用模板表单
     * @return 新流程定义ID
     */
    @PostMapping("/apply-template")
    public ResponseDTO<String> applyTemplate(
            @RequestBody @Valid ApplyTemplateForm form) {

        log.info("[可视化配置] 应用模板: templateId={}, newProcessKey={}",
                form.getTemplateId(), form.getNewProcessKey());

        String processDefinitionId = visualWorkflowConfigService.applyTemplate(
                form.getTemplateId(),
                form.getNewProcessKey(),
                form.getNewProcessName(),
                form.getCustomizations()
        );

        return ResponseDTO.ok(processDefinitionId);
    }

    /**
     * 上传自定义模板
     *
     * @param templateId 模板ID
     * @param templateName 模板名称
     * @param category 分类
     * @param bpmnFile BPMN文件
     * @param screenshot 截图文件
     * @return 操作结果
     */
    @PostMapping("/templates/upload")
    public ResponseDTO<String> uploadTemplate(
            @RequestParam String templateId,
            @RequestParam String templateName,
            @RequestParam String category,
            @RequestParam MultipartFile bpmnFile,
            @RequestParam(required = false) MultipartFile screenshot) {

        log.info("[可视化配置] 上传模板: templateId={}, templateName={}",
                templateId, templateName);

        String uploadedTemplateId = visualWorkflowConfigService.uploadTemplate(
                templateId,
                templateName,
                category,
                bpmnFile,
                screenshot
        );

        return ResponseDTO.ok(uploadedTemplateId);
    }

    // ==================== 流程验证和诊断 ====================

    /**
     * 验证流程配置
     *
     * @param form 配置表单
     * @return 验证结果
     */
    @PostMapping("/validate")
    public ResponseDTO<ValidationResult> validateConfig(
            @RequestBody @Valid VisualWorkflowConfigForm form) {

        log.info("[可视化配置] 验证流程配置: processKey={}", form.getProcessKey());

        ValidationResult result = visualWorkflowConfigService.validateConfig(form);

        return ResponseDTO.ok(result);
    }

    /**
     * 诊断流程问题
     *
     * @param processDefinitionId 流程定义ID
     * @return 诊断结果
     */
    @PostMapping("/{processDefinitionId}/diagnose")
    public ResponseDTO<List<WorkflowIssue>> diagnoseWorkflow(
            @PathVariable String processDefinitionId) {

        log.info("[可视化配置] 诊断流程: processDefinitionId={}", processDefinitionId);

        List<WorkflowIssue> issues = visualWorkflowConfigService.diagnoseWorkflow(processDefinitionId);

        return ResponseDTO.ok(issues);
    }

    /**
     * 流程仿真
     *
     * @param request 仿真请求
     * @return 仿真结果
     */
    @PostMapping("/simulate")
    public ResponseDTO<WorkflowSimulationResult> simulateWorkflow(
            @RequestBody @Valid WorkflowSimulationRequest request) {

        log.info("[可视化配置] 仿真流程: processDefinitionId={}", request.getProcessDefinitionId());

        WorkflowSimulationResult result = visualWorkflowConfigService.simulateWorkflow(request);

        return ResponseDTO.ok(result);
    }

    // ==================== 组件库 ====================

    /**
     * 获取节点类型库
     *
     * @return 节点类型列表
     */
    @GetMapping("/node-types")
    public ResponseDTO<List<NodeType>> getNodeTypes() {
        List<NodeType> nodeTypes = visualWorkflowConfigService.getNodeTypes();

        return ResponseDTO.ok(nodeTypes);
    }

    /**
     * 获取节点配置Schema
     *
     * @param nodeType 节点类型
     * @return 配置Schema
     */
    @GetMapping("/node-types/{nodeType}/schema")
    public ResponseDTO<NodeConfigSchema> getNodeConfigSchema(
            @PathVariable String nodeType) {

        NodeConfigSchema schema = visualWorkflowConfigService.getNodeConfigSchema(nodeType);

        return ResponseDTO.ok(schema);
    }

    // ==================== 导入导出 ====================

    /**
     * 导出流程配置
     *
     * @param processDefinitionId 流程定义ID
     * @return 配置JSON
     */
    @GetMapping("/{processDefinitionId}/export")
    public ResponseDTO<VisualWorkflowConfig> exportConfig(
            @PathVariable String processDefinitionId) {

        log.info("[可视化配置] 导出流程配置: processDefinitionId={}", processDefinitionId);

        VisualWorkflowConfig config = visualWorkflowConfigService.getVisualConfig(processDefinitionId);

        return ResponseDTO.ok(config);
    }

    /**
     * 导入流程配置
     *
     * @param file 配置文件
     * @return 新流程定义ID
     */
    @PostMapping("/import")
    public ResponseDTO<String> importConfig(
            @RequestParam MultipartFile file) {

        log.info("[可视化配置] 导入流程配置: fileName={}", file.getOriginalFilename());

        String processDefinitionId = visualWorkflowConfigService.importConfig(file);

        return ResponseDTO.ok(processDefinitionId);
    }
}

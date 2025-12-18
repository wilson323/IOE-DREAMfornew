package net.lab1024.sa.oa.workflow.designer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.designer.domain.ProcessDefinitionDTO;
import net.lab1024.sa.oa.workflow.designer.service.ProcessDesignerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程设计器控制器
 * <p>
 * 提供低代码流程设计器的REST API，支持BPMN 2.0标准
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/process-designer")
@Tag(name = "流程设计器", description = "低代码流程设计器API")
public class ProcessDesignerController {

    @Resource
    private ProcessDesignerService processDesignerService;

    /**
     * 保存流程模板
     */
    @PostMapping("/template")
    @Operation(summary = "保存流程模板", description = "创建或更新流程模板")
    public ResponseDTO<Long> saveProcessTemplate(@Valid @RequestBody ProcessDefinitionDTO processDefinition) {
        log.info("[流程设计器] 保存流程模板: key={}", processDefinition.getTemplateKey());
        Long id = processDesignerService.saveProcessTemplate(processDefinition);
        return ResponseDTO.ok(id);
    }

    /**
     * 获取流程模板
     */
    @GetMapping("/template/{templateKey}")
    @Operation(summary = "获取流程模板", description = "根据templateKey获取最新版本流程模板")
    public ResponseDTO<ProcessDefinitionDTO> getProcessTemplate(@PathVariable String templateKey) {
        log.info("[流程设计器] 获取流程模板: key={}", templateKey);
        ProcessDefinitionDTO processDefinition = processDesignerService.getProcessTemplate(templateKey);
        return ResponseDTO.ok(processDefinition);
    }

    /**
     * 获取流程模板列表
     */
    @GetMapping("/templates")
    @Operation(summary = "获取流程模板列表", description = "获取所有启用的流程模板")
    public ResponseDTO<List<ProcessDefinitionDTO>> listProcessTemplates(
            @RequestParam(required = false) String category) {
        log.info("[流程设计器] 获取流程模板列表: category={}", category);
        List<ProcessDefinitionDTO> templates = processDesignerService.listProcessTemplates(category);
        return ResponseDTO.ok(templates);
    }

    /**
     * 部署流程
     */
    @PostMapping("/deploy/{templateKey}")
    @Operation(summary = "部署流程", description = "将流程模板部署到Flowable引擎")
    public ResponseDTO<String> deployProcess(@PathVariable String templateKey) {
        log.info("[流程设计器] 部署流程: key={}", templateKey);
        String deploymentId = processDesignerService.deployProcess(templateKey);
        return ResponseDTO.ok(deploymentId);
    }

    /**
     * 验证BPMN XML
     */
    @PostMapping("/validate-bpmn")
    @Operation(summary = "验证BPMN XML", description = "验证BPMN XML格式是否正确")
    public ResponseDTO<Map<String, Object>> validateBpmn(@RequestBody String bpmnXml) {
        log.info("[流程设计器] 验证BPMN XML");
        Map<String, Object> validationResult = processDesignerService.validateBpmn(bpmnXml);
        return ResponseDTO.ok(validationResult);
    }

    /**
     * 获取流程图SVG
     */
    @GetMapping("/diagram/{processDefinitionId}")
    @Operation(summary = "获取流程图", description = "获取已部署流程的SVG图形")
    public ResponseDTO<String> getProcessDiagram(@PathVariable String processDefinitionId) {
        log.info("[流程设计器] 获取流程图: processDefinitionId={}", processDefinitionId);
        String svg = processDesignerService.getProcessDiagram(processDefinitionId);
        return ResponseDTO.ok(svg);
    }
}

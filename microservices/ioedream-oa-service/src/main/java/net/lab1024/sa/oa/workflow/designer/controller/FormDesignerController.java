package net.lab1024.sa.oa.workflow.designer.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.designer.domain.FormDefinitionDTO;
import net.lab1024.sa.oa.workflow.designer.service.FormDesignerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 表单设计器控制器
 * <p>
 * 提供低代码表单设计器的REST API，支持JSON Schema格式表单定义
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/v1/workflow/form-designer")
@Tag(name = "表单设计器", description = "低代码表单设计器API")
@Slf4j
public class FormDesignerController {

    @Resource
    private FormDesignerService formDesignerService;

    /**
     * 保存表单定义
     */
    @PostMapping("/definition")
    @Operation(summary = "保存表单定义", description = "创建或更新表单定义")
    public ResponseDTO<Long> saveFormDefinition (@Valid @RequestBody FormDefinitionDTO formDefinition) {
        log.info ("[表单设计器] 保存表单定义: key={}", formDefinition.getFormKey ());
        Long id = formDesignerService.saveFormDefinition (formDefinition);
        return ResponseDTO.ok (id);
    }

    /**
     * 获取表单定义
     */
    @GetMapping("/definition/{formKey}")
    @Operation(summary = "获取表单定义", description = "根据formKey获取最新版本表单定义")
    public ResponseDTO<FormDefinitionDTO> getFormDefinition (@PathVariable String formKey) {
        log.info ("[表单设计器] 获取表单定义: key={}", formKey);
        FormDefinitionDTO formDefinition = formDesignerService.getFormDefinition (formKey);
        return ResponseDTO.ok (formDefinition);
    }

    /**
     * 获取表单定义列表
     */
    @GetMapping("/definitions")
    @Operation(summary = "获取表单定义列表", description = "获取所有启用的表单定义")
    public ResponseDTO<List<FormDefinitionDTO>> listFormDefinitions (@RequestParam(required = false) String category) {
        log.info ("[表单设计器] 获取表单定义列表: category={}", category);
        List<FormDefinitionDTO> formDefinitions = formDesignerService.listFormDefinitions (category);
        return ResponseDTO.ok (formDefinitions);
    }

    /**
     * 删除表单定义
     */
    @DeleteMapping("/definition/{formKey}")
    @Operation(summary = "删除表单定义", description = "逻辑删除表单定义")
    public ResponseDTO<Boolean> deleteFormDefinition (@PathVariable String formKey) {
        log.info ("[表单设计器] 删除表单定义: key={}", formKey);
        boolean result = formDesignerService.deleteFormDefinition (formKey);
        return ResponseDTO.ok (result);
    }

    /**
     * 预览表单
     */
    @PostMapping("/preview")
    @Operation(summary = "预览表单", description = "根据Schema预览表单渲染效果")
    public ResponseDTO<Map<String, Object>> previewForm (@RequestBody Map<String, Object> formSchema) {
        log.info ("[表单设计器] 预览表单");
        Map<String, Object> previewData = formDesignerService.previewForm (formSchema);
        return ResponseDTO.ok (previewData);
    }

    /**
     * 验证表单Schema
     */
    @PostMapping("/validate-schema")
    @Operation(summary = "验证表单Schema", description = "验证JSON Schema格式是否正确")
    public ResponseDTO<Map<String, Object>> validateSchema (@RequestBody String schemaJson) {
        log.info ("[表单设计器] 验证表单Schema");
        Map<String, Object> validationResult = formDesignerService.validateSchema (schemaJson);
        return ResponseDTO.ok (validationResult);
    }
}

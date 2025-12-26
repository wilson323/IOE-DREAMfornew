package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO: 取消注释以下import（待创建相关Form和VO类后）
// import net.lab1024.sa.attendance.domain.form.AttendanceRuleTemplateAddForm;
// import net.lab1024.sa.attendance.domain.form.AttendanceRuleTemplateQueryForm;
// import net.lab1024.sa.attendance.domain.form.AttendanceRuleTemplateUpdateForm;
// import net.lab1024.sa.attendance.domain.vo.AttendanceRuleTemplateVO;
// import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.attendance.service.AttendanceRuleTemplateService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 考勤规则模板控制器
 * <p>
 * 提供考勤规则模板管理相关API接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/rule-template")
@Tag(name = "考勤规则模板管理")
public class AttendanceRuleTemplateController {

    @Resource
    private AttendanceRuleTemplateService attendanceRuleTemplateService;

    /**
     * 分页查询规则模板
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    // TODO: 取消注释（待创建相关VO类后）
    @Observed(name = "ruleTemplate.queryPage", contextualName = "query-template-page")
    @GetMapping("/page")
    @Operation(summary = "分页查询规则模板", description = "分页查询考勤规则模板列表")
    public ResponseDTO<Object> queryPage(Object queryForm) {
        log.info("[规则模板] 分页查询模板: queryForm={}", queryForm);
        Object pageResult = attendanceRuleTemplateService.queryTemplatePage(queryForm);
        log.info("[规则模板] 分页查询成功");
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询系统模板列表
     *
     * @param category 模板分类（可选）
     * @return 系统模板列表
     */
    // TODO: 取消注释（待创建相关VO类后）
    @Observed(name = "ruleTemplate.getSystemTemplates", contextualName = "get-system-templates")
    @GetMapping("/system")
    @Operation(summary = "查询系统模板列表", description = "查询系统预置的规则模板列表")
    public ResponseDTO<List<Object>> getSystemTemplates(
            @RequestParam(required = false) @Parameter(description = "模板分类（可选）", required = false) String category) {
        log.info("[规则模板] 查询系统模板: category={}", category);
        List<Object> templates = attendanceRuleTemplateService.getSystemTemplates(category);
        log.info("[规则模板] 查询系统模板成功: count={}", templates.size());
        return ResponseDTO.ok(templates);
    }

    /**
     * 查询用户自定义模板列表
     *
     * @param userId 用户ID
     * @param category 模板分类（可选）
     * @return 用户模板列表
     */
    // TODO: 取消注释（待创建相关VO类后）
    @Observed(name = "ruleTemplate.getUserTemplates", contextualName = "get-user-templates")
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户模板列表", description = "查询指定用户的自定义规则模板列表")
    public ResponseDTO<List<Object>> getUserTemplates(
            @PathVariable @Parameter(description = "用户ID", required = true) Long userId,
            @RequestParam(required = false) @Parameter(description = "模板分类（可选）", required = false) String category) {
        log.info("[规则模板] 查询用户模板: userId={}, category={}", userId, category);
        List<Object> templates = attendanceRuleTemplateService.getUserTemplates(userId, category);
        log.info("[规则模板] 查询用户模板成功: userId={}, count={}", userId, templates.size());
        return ResponseDTO.ok(templates);
    }

    /**
     * 查询模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    // TODO: 取消注释（待创建相关VO类后）
    @Observed(name = "ruleTemplate.getDetail", contextualName = "get-template-detail")
    @GetMapping("/{templateId}")
    @Operation(summary = "查询模板详情", description = "查询指定模板的详细信息")
    public ResponseDTO<Object> getDetail(
            @PathVariable @Parameter(description = "模板ID", required = true) Long templateId) {
        log.info("[规则模板] 查询模板详情: templateId={}", templateId);
        Object template = attendanceRuleTemplateService.getTemplateDetail(templateId);
        log.info("[规则模板] 查询模板详情成功: templateId={}", templateId);
        return ResponseDTO.ok(template);
    }

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 模板详情
     */
    // TODO: 取消注释（待创建相关VO类后）
    @Observed(name = "ruleTemplate.getByCode", contextualName = "get-template-by-code")
    @GetMapping("/code/{templateCode}")
    @Operation(summary = "根据编码查询模板", description = "根据模板编码查询模板详情")
    public ResponseDTO<Object> getByCode(
            @PathVariable @Parameter(description = "模板编码", required = true) String templateCode) {
        log.info("[规则模板] 根据编码查询模板: templateCode={}", templateCode);
        Object template = attendanceRuleTemplateService.getTemplateByCode(templateCode);
        log.info("[规则模板] 根据编码查询模板成功: templateCode={}", templateCode);
        return ResponseDTO.ok(template);
    }

    /**
     * 创建规则模板
     *
     * @param addForm 新增表单
     * @return 模板ID
     */
    // TODO: 取消注释（待创建相关Form类后）
    @Observed(name = "ruleTemplate.create", contextualName = "create-template")
    @PostMapping
    @Operation(summary = "创建规则模板", description = "创建新的考勤规则模板")
    public ResponseDTO<Long> create(@Valid @RequestBody Object addForm) {
        log.info("[规则模板] 创建模板: addForm={}", addForm);
        Long templateId = attendanceRuleTemplateService.createTemplate(addForm);
        log.info("[规则模板] 创建模板成功: templateId={}", templateId);
        return ResponseDTO.ok(templateId);
    }

    /**
     * 更新规则模板
     *
     * @param templateId 模板ID
     * @param updateForm 更新表单
     * @return 无返回
     */
    // TODO: 取消注释（待创建相关Form类后）
    @Observed(name = "ruleTemplate.update", contextualName = "update-template")
    @PutMapping("/{templateId}")
    @Operation(summary = "更新规则模板", description = "更新指定的考勤规则模板")
    public ResponseDTO<Void> update(
            @PathVariable @Parameter(description = "模板ID", required = true) Long templateId,
            @Valid @RequestBody Object updateForm) {
        log.info("[规则模板] 更新模板: templateId={}", templateId);
        attendanceRuleTemplateService.updateTemplate(templateId, updateForm);
        log.info("[规则模板] 更新模板成功: templateId={}", templateId);
        return ResponseDTO.ok();
    }

    /**
     * 删除规则模板
     *
     * @param templateId 模板ID
     * @return 无返回
     */
    @Observed(name = "ruleTemplate.delete", contextualName = "delete-template")
    @DeleteMapping("/{templateId}")
    @Operation(summary = "删除规则模板", description = "删除指定的考勤规则模板（系统模板不允许删除）")
    public ResponseDTO<Void> delete(
            @PathVariable @Parameter(description = "模板ID", required = true) Long templateId) {
        log.info("[规则模板] 删除模板: templateId={}", templateId);
        attendanceRuleTemplateService.deleteTemplate(templateId);
        log.info("[规则模板] 删除模板成功: templateId={}", templateId);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除规则模板
     *
     * @param templateIds 模板ID列表
     * @return 无返回
     */
    @Observed(name = "ruleTemplate.batchDelete", contextualName = "batch-delete-templates")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除规则模板", description = "批量删除指定的考勤规则模板（系统模板不会被删除）")
    public ResponseDTO<Void> batchDelete(@RequestBody List<Long> templateIds) {
        log.info("[规则模板] 批量删除模板: count={}", templateIds.size());
        attendanceRuleTemplateService.batchDeleteTemplates(templateIds);
        log.info("[规则模板] 批量删除模板成功: count={}", templateIds.size());
        return ResponseDTO.ok();
    }

    /**
     * 应用模板到规则
     *
     * @param templateId 模板ID
     * @param ruleId 规则ID
     * @return 无返回
     */
    @Observed(name = "ruleTemplate.applyToRule", contextualName = "apply-template-to-rule")
    @PostMapping("/{templateId}/apply/{ruleId}")
    @Operation(summary = "应用模板到规则", description = "将模板的条件和动作应用到指定规则")
    public ResponseDTO<Void> applyToRule(
            @PathVariable @Parameter(description = "模板ID", required = true) Long templateId,
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId) {
        log.info("[规则模板] 应用模板到规则: templateId={}, ruleId={}", templateId, ruleId);
        attendanceRuleTemplateService.applyTemplateToRule(templateId, ruleId);
        log.info("[规则模板] 应用模板到规则成功: templateId={}, ruleId={}", templateId, ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 导出模板
     *
     * @param templateId 模板ID
     * @return 模板JSON数据
     */
    @Observed(name = "ruleTemplate.export", contextualName = "export-template")
    @GetMapping("/{templateId}/export")
    @Operation(summary = "导出模板", description = "导出指定模板的JSON数据")
    public ResponseDTO<String> export(
            @PathVariable @Parameter(description = "模板ID", required = true) Long templateId) {
        log.info("[规则模板] 导出模板: templateId={}", templateId);
        String json = attendanceRuleTemplateService.exportTemplate(templateId);
        log.info("[规则模板] 导出模板成功: templateId={}", templateId);
        return ResponseDTO.ok(json);
    }

    /**
     * 导入模板
     *
     * @param templateJson 模板JSON数据
     * @return 模板ID
     */
    @Observed(name = "ruleTemplate.import", contextualName = "import-template")
    @PostMapping("/import")
    @Operation(summary = "导入模板", description = "从JSON数据导入模板")
    public ResponseDTO<Long> importTemplate(@RequestBody String templateJson) {
        log.info("[规则模板] 导入模板");
        Long templateId = attendanceRuleTemplateService.importTemplate(templateJson);
        log.info("[规则模板] 导入模板成功: templateId={}", templateId);
        return ResponseDTO.ok(templateId);
    }

    /**
     * 复制模板
     *
     * @param templateId 模板ID
     * @param newTemplateName 新模板名称
     * @return 新模板ID
     */
    @Observed(name = "ruleTemplate.copy", contextualName = "copy-template")
    @PostMapping("/{templateId}/copy")
    @Operation(summary = "复制模板", description = "复制指定模板创建新模板")
    public ResponseDTO<Long> copy(
            @PathVariable @Parameter(description = "模板ID", required = true) Long templateId,
            @RequestParam @Parameter(description = "新模板名称", required = true) String newTemplateName) {
        log.info("[规则模板] 复制模板: templateId={}, newTemplateName={}", templateId, newTemplateName);
        Long newTemplateId = attendanceRuleTemplateService.copyTemplate(templateId, newTemplateName);
        log.info("[规则模板] 复制模板成功: sourceTemplateId={}, newTemplateId={}", templateId, newTemplateId);
        return ResponseDTO.ok(newTemplateId);
    }
}

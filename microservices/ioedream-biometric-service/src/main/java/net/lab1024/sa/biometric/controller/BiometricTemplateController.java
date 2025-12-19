package net.lab1024.sa.biometric.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateAddForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateQueryForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateUpdateForm;
import net.lab1024.sa.biometric.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.biometric.service.BiometricTemplateService;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 生物模板管理控制器
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数验证
 * - 统一的ResponseDTO响应格式
 * - RESTful API设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/biometric/template")
@Tag(name = "生物模板管理", description = "生物识别模板管理API")
@Validated
public class BiometricTemplateController {

    @Resource
    private BiometricTemplateService biometricTemplateService;

    /**
     * 添加生物模板
     */
    @PostMapping
    @Operation(summary = "添加生物模板", description = "用户入职时上传生物特征，创建模板并同步到设备")
    public ResponseDTO<BiometricTemplateVO> addTemplate(@Valid @RequestBody BiometricTemplateAddForm addForm) {
        return biometricTemplateService.addTemplate(addForm);
    }

    /**
     * 更新生物模板
     */
    @PutMapping("/{templateId}")
    @Operation(summary = "更新生物模板", description = "更新模板信息")
    public ResponseDTO<Void> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody BiometricTemplateUpdateForm updateForm) {
        updateForm.setTemplateId(templateId);
        return biometricTemplateService.updateTemplate(updateForm);
    }

    /**
     * 删除生物模板
     */
    @DeleteMapping("/{templateId}")
    @Operation(summary = "删除生物模板", description = "用户离职时删除模板，并从所有设备删除")
    public ResponseDTO<Void> deleteTemplate(@PathVariable Long templateId) {
        return biometricTemplateService.deleteTemplate(templateId);
    }

    /**
     * 根据用户ID和类型删除模板
     */
    @DeleteMapping("/user/{userId}/type/{biometricType}")
    @Operation(summary = "删除用户指定类型模板", description = "删除用户指定类型的生物模板")
    public ResponseDTO<Void> deleteTemplateByUserAndType(
            @PathVariable Long userId,
            @PathVariable Integer biometricType) {
        return biometricTemplateService.deleteTemplateByUserAndType(userId, biometricType);
    }

    /**
     * 根据ID查询模板
     */
    @GetMapping("/{templateId}")
    @Operation(summary = "查询模板详情", description = "根据模板ID查询模板信息")
    public ResponseDTO<BiometricTemplateVO> getTemplateById(@PathVariable Long templateId) {
        return biometricTemplateService.getTemplateById(templateId);
    }

    /**
     * 根据用户ID查询模板列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户模板列表", description = "根据用户ID查询所有生物模板")
    public ResponseDTO<List<BiometricTemplateVO>> getTemplatesByUserId(@PathVariable Long userId) {
        return biometricTemplateService.getTemplatesByUserId(userId);
    }

    /**
     * 根据用户ID和类型查询模板
     */
    @GetMapping("/user/{userId}/type/{biometricType}")
    @Operation(summary = "查询用户指定类型模板", description = "根据用户ID和生物识别类型查询模板")
    public ResponseDTO<BiometricTemplateVO> getTemplateByUserAndType(
            @PathVariable Long userId,
            @PathVariable Integer biometricType) {
        return biometricTemplateService.getTemplateByUserAndType(userId, biometricType);
    }

    /**
     * 分页查询模板
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询模板", description = "分页查询生物模板列表")
    public ResponseDTO<PageResult<BiometricTemplateVO>> pageTemplate(@Valid BiometricTemplateQueryForm queryForm) {
        return biometricTemplateService.pageTemplate(queryForm);
    }

    /**
     * 更新模板状态
     */
    @PutMapping("/{templateId}/status")
    @Operation(summary = "更新模板状态", description = "更新模板状态（激活/禁用/锁定等）")
    public ResponseDTO<Void> updateTemplateStatus(
            @PathVariable Long templateId,
            @RequestParam Integer templateStatus) {
        return biometricTemplateService.updateTemplateStatus(templateId, templateStatus);
    }
}

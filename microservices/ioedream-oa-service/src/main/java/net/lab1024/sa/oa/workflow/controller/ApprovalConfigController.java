package net.lab1024.sa.oa.workflow.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalConfigForm;
import net.lab1024.sa.oa.workflow.entity.ApprovalConfigEntity;
import net.lab1024.sa.oa.workflow.service.ApprovalConfigService;

/**
 * 审批配置控制器
 * <p>
 * 提供审批配置的REST API接口
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/approval-config")
@Tag(name = "审批配置管理", description = "审批配置的CRUD操作，支持自定义审批类型和流程配置")
@PermissionCheck(value = "OA_CONFIG", description = "审批配置管理模块权限")
public class ApprovalConfigController {

    @Resource
    private ApprovalConfigService approvalConfigService;

    @Observed(name = "approvalConfig.pageConfigs", contextualName = "approval-config-page-configs")
    @GetMapping("/page")
    @Operation(summary = "分页查询审批配置", description = "支持按业务类型、模块、状态筛选")
    @PermissionCheck(value = "OA_CONFIG_VIEW", description = "查看审批配置")
    public ResponseDTO<PageResult<ApprovalConfigEntity>> pageConfigs(
            PageParam pageParam,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status) {
        return approvalConfigService.pageConfigs(pageParam, businessType, module, status);
    }

    @Observed(name = "approvalConfig.getConfig", contextualName = "approval-config-get-config")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询审批配置")
    @PermissionCheck(value = "OA_CONFIG_VIEW", description = "查看审批配置详情")
    public ResponseDTO<ApprovalConfigEntity> getConfig(@PathVariable Long id) {
        return approvalConfigService.getConfig(id);
    }

    @Observed(name = "approvalConfig.getConfigByBusinessType", contextualName = "approval-config-get-by-business-type")
    @GetMapping("/business-type/{businessType}")
    @Operation(summary = "根据业务类型查询审批配置", description = "支持自定义业务类型")
    @PermissionCheck(value = "OA_CONFIG_VIEW", description = "按业务类型查看配置")
    public ResponseDTO<ApprovalConfigEntity> getConfigByBusinessType(@PathVariable String businessType) {
        return approvalConfigService.getConfigByBusinessType(businessType);
    }

    @Observed(name = "approvalConfig.createConfig", contextualName = "approval-config-create-config")
    @PostMapping
    @Operation(summary = "创建审批配置", description = "支持自定义业务类型和审批流程配置")
    @PermissionCheck(value = "OA_CONFIG_MANAGE", description = "创建审批配置")
    public ResponseDTO<ApprovalConfigEntity> createConfig(@Valid @RequestBody ApprovalConfigForm form) {
        return approvalConfigService.createConfig(form);
    }

    @Observed(name = "approvalConfig.updateConfig", contextualName = "approval-config-update-config")
    @PutMapping("/{id}")
    @Operation(summary = "更新审批配置")
    @PermissionCheck(value = "OA_CONFIG_MANAGE", description = "更新审批配置")
    public ResponseDTO<ApprovalConfigEntity> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalConfigForm form) {
        return approvalConfigService.updateConfig(id, form);
    }

    @Observed(name = "approvalConfig.deleteConfig", contextualName = "approval-config-delete-config")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除审批配置")
    @PermissionCheck(value = "OA_CONFIG_DELETE", description = "删除审批配置")
    public ResponseDTO<Void> deleteConfig(@PathVariable Long id) {
        return approvalConfigService.deleteConfig(id);
    }

    @Observed(name = "approvalConfig.enableConfig", contextualName = "approval-config-enable-config")
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用审批配置")
    @PermissionCheck(value = "OA_CONFIG_MANAGE", description = "启用审批配置")
    public ResponseDTO<Void> enableConfig(@PathVariable Long id) {
        return approvalConfigService.enableConfig(id);
    }

    @Observed(name = "approvalConfig.disableConfig", contextualName = "approval-config-disable-config")
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用审批配置")
    @PermissionCheck(value = "OA_CONFIG_MANAGE", description = "禁用审批配置")
    public ResponseDTO<Void> disableConfig(@PathVariable Long id) {
        return approvalConfigService.disableConfig(id);
    }
}






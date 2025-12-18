package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessPermissionApplyService;

/**
 * 门禁权限申请控制器
 * <p>
 * 提供门禁权限申请相关API接口
 * 严格遵循CLAUDE.md规范：
 * - Controller负责接收请求、参数验证、返回响应
 * - 使用@Resource注入Service
 * - 使用@Valid进行参数验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/permission/apply")
@Tag(name = "门禁权限申请管理")
public class AccessPermissionApplyController {

    @Resource
    private AccessPermissionApplyService accessPermissionApplyService;

    /**
     * 提交权限申请
     * <p>
     * 功能说明：
     * 1. 创建权限申请记录
     * 2. 启动工作流审批流程
     * 3. 关联工作流实例ID
     * </p>
     *
     * @param form 权限申请表单
     * @return 权限申请实体（包含workflowInstanceId）
     */
    @Observed(name = "accessPermissionApply.submitPermissionApply", contextualName = "access-permission-apply-submit")
    @PostMapping("/submit")
    @Operation(
            summary = "提交权限申请",
            description = "提交权限申请并启动审批流程，支持临时权限和长期权限申请",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "权限申请表单，包含申请人ID、区域ID、申请类型、申请原因、有效期等",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "提交成功",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AccessPermissionApplyEntity.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "参数错误"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "工作流启动失败"
                    )
            }
    )
    public ResponseDTO<AccessPermissionApplyEntity> submitPermissionApply(
            @Valid @RequestBody AccessPermissionApplyForm form) {
        log.info("[权限申请] 接收权限申请请求：applicantId={}, areaId={}, applyType={}",
                form.getApplicantId(), form.getAreaId(), form.getApplyType());
        AccessPermissionApplyEntity entity = accessPermissionApplyService.submitPermissionApply(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 更新权限申请状态（供审批结果监听器调用）
     * <p>
     * 功能说明：
     * 1. 更新权限申请状态（PENDING/APPROVED/REJECTED）
     * 2. 如果审批通过，执行权限授权逻辑
     * 3. 记录审批意见和审批时间
     * </p>
     *
     * @param applyNo 申请编号
     * @param requestParams 请求参数（包含status和approvalComment）
     * @return 操作结果
     */
    @Observed(name = "accessPermissionApply.updatePermissionApplyStatus", contextualName = "access-permission-apply-update-status")
    @PutMapping("/{applyNo}/status")
    @Operation(
            summary = "更新权限申请状态",
            description = "由审批结果监听器调用，更新权限申请状态，审批通过时自动授予权限",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "applyNo",
                            description = "申请编号",
                            required = true,
                            example = "APPLY20250130001"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "状态更新参数，包含status（审批状态）和approvalComment（审批意见）",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "更新成功"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "申请不存在"
                    )
            }
    )
    public ResponseDTO<Void> updatePermissionApplyStatus(
            @PathVariable String applyNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[权限申请] 接收状态更新请求，applyNo={}", applyNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        accessPermissionApplyService.updatePermissionApplyStatus(applyNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}

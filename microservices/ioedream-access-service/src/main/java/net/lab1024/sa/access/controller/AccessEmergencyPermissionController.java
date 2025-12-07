package net.lab1024.sa.access.controller;

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
import net.lab1024.sa.access.domain.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessEmergencyPermissionService;

/**
 * 紧急权限申请控制器
 * <p>
 * 提供紧急权限申请相关API接口
 * 特点：
 * - 简化审批流程（快速审批）
 * - 临时权限生效
 * - 自动过期机制（24小时后自动失效）
 * 严格遵循CLAUDE.md规范：
 * - Controller层负责接收请求、参数验证、返回响应
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
@RequestMapping("/api/v1/access/emergency-permission")
@Tag(name = "紧急权限申请管理")
public class AccessEmergencyPermissionController {

    @Resource
    private AccessEmergencyPermissionService accessEmergencyPermissionService;

    /**
     * 提交紧急权限申请
     * <p>
     * 紧急权限申请特点：
     * - 简化审批流程，只需部门经理快速审批
     * - 审批通过后立即生效
     * - 权限有效期24小时（自动过期）
     * </p>
     *
     * @param form 紧急权限申请表单
     * @return 权限申请实体
     */
    @PostMapping("/submit")
    @Operation(summary = "提交紧急权限申请", description = "提交紧急权限申请并启动快速审批流程，权限有效期24小时")
    public ResponseDTO<AccessPermissionApplyEntity> submitEmergencyPermissionApply(
            @Valid @RequestBody AccessPermissionApplyForm form) {
        log.info("[紧急权限申请] 接收紧急权限申请请求，applicantId={}, areaId={}, reason={}",
                form.getApplicantId(), form.getAreaId(), form.getApplyReason());
        
        // 验证申请类型
        if (form.getApplyType() != null && !"EMERGENCY".equals(form.getApplyType())) {
            return ResponseDTO.error("申请类型必须是EMERGENCY");
        }
        
        // 强制设置为紧急权限类型
        form.setApplyType("EMERGENCY");
        
        AccessPermissionApplyEntity entity = accessEmergencyPermissionService.submitEmergencyPermissionApply(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 更新紧急权限申请状态（供审批结果监听器调用）
     *
     * @param applyNo 申请编号
     * @param requestParams 请求参数（包含status和approvalComment）
     * @return 操作结果
     */
    @PutMapping("/{applyNo}/status")
    @Operation(summary = "更新紧急权限申请状态", description = "由审批结果监听器调用，更新紧急权限申请状态")
    public ResponseDTO<Void> updateEmergencyPermissionStatus(
            @PathVariable String applyNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[紧急权限申请] 接收状态更新请求，applyNo={}", applyNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        accessEmergencyPermissionService.updateEmergencyPermissionStatus(applyNo, status, approvalComment);
        return ResponseDTO.ok();
    }

    /**
     * 检查并回收过期权限
     * <p>
     * 紧急权限有效期24小时，过期后自动回收
     * </p>
     *
     * @param applyNo 申请编号
     * @return 是否已过期并回收
     */
    @PostMapping("/{applyNo}/revoke-expired")
    @Operation(summary = "检查并回收过期权限", description = "检查紧急权限是否已过期，如果过期则自动回收")
    public ResponseDTO<Boolean> checkAndRevokeExpiredPermission(@PathVariable String applyNo) {
        log.info("[紧急权限申请] 检查过期权限请求，applyNo={}", applyNo);
        boolean revoked = accessEmergencyPermissionService.checkAndRevokeExpiredPermission(applyNo);
        return ResponseDTO.ok(revoked);
    }
}


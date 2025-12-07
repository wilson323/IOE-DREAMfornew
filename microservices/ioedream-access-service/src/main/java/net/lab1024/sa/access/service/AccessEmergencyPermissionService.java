package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;

/**
 * 紧急权限申请服务接口
 * <p>
 * 提供紧急权限申请相关业务功能
 * 特点：
 * - 简化审批流程（快速审批）
 * - 临时权限生效
 * - 自动过期机制（24小时后自动失效）
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessEmergencyPermissionService {

    /**
     * 提交紧急权限申请（启动快速审批流程）
     * <p>
     * 紧急权限申请特点：
     * - 简化审批流程，只需部门经理快速审批
     * - 审批通过后立即生效
     * - 权限有效期24小时（自动过期）
     * </p>
     *
     * @param form 紧急权限申请表单
     * @return 权限申请实体（包含workflowInstanceId）
     */
    AccessPermissionApplyEntity submitEmergencyPermissionApply(AccessPermissionApplyForm form);

    /**
     * 更新紧急权限申请状态（由审批结果监听器调用）
     *
     * @param applyNo 申请编号
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    void updateEmergencyPermissionStatus(String applyNo, String status, String approvalComment);

    /**
     * 检查并回收过期权限
     * <p>
     * 紧急权限有效期24小时，过期后自动回收
     * </p>
     *
     * @param applyNo 申请编号
     * @return 是否已过期并回收
     */
    boolean checkAndRevokeExpiredPermission(String applyNo);
}


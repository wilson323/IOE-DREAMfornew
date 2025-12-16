package net.lab1024.sa.access.service;

import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;

/**
 * 门禁权限申请服务接口
 * <p>
 * 提供门禁权限申请相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessPermissionApplyService {

    /**
     * 提交权限申请（启动审批流程）
     *
     * @param form 权限申请表单
     * @return 权限申请实体（包含workflowInstanceId）
     */
    AccessPermissionApplyEntity submitPermissionApply(AccessPermissionApplyForm form);

    /**
     * 更新权限申请状态（由审批结果监听器调用）
     *
     * @param applyNo 申请编号
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    void updatePermissionApplyStatus(String applyNo, String status, String approvalComment);
}



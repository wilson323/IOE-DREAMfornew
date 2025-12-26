package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.entity.AttendanceAnomalyApplyEntity;

/**
 * 考勤异常审批服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceAnomalyApprovalService {

    /**
     * 批准申请
     *
     * @param applyId 申请ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param comment 审批意见
     * @return 是否成功
     */
    Boolean approveApply(Long applyId, Long approverId, String approverName, String comment);

    /**
     * 驳回申请
     *
     * @param applyId 申请ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param comment 驳回原因
     * @return 是否成功
     */
    Boolean rejectApply(Long applyId, Long approverId, String approverName, String comment);

    /**
     * 批量审批申请
     *
     * @param applyIds 申请ID列表
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param comment 审批意见
     * @param approve 是否批准（true=批准，false=驳回）
     * @return 成功处理的数量
     */
    Integer batchApprove(Long[] applyIds, Long approverId, String approverName,
                         String comment, Boolean approve);
}

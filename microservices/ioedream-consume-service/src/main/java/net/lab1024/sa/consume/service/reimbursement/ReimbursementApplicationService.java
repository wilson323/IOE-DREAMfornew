package net.lab1024.sa.consume.service.reimbursement;

import net.lab1024.sa.consume.entity.ReimbursementApplicationEntity;
import net.lab1024.sa.consume.domain.form.ReimbursementApplicationForm;

/**
 * 报销申请服务接口
 * <p>
 * 提供报销申请相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ReimbursementApplicationService {

    /**
     * 提交报销申请（启动审批流程）
     *
     * @param form 报销申请表单
     * @return 报销申请实体（包含workflowInstanceId）
     */
    ReimbursementApplicationEntity submitReimbursementApplication(ReimbursementApplicationForm form);

    /**
     * 更新报销申请状态（由审批结果监听器调用）
     *
     * @param reimbursementNo 报销申请编号
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    void updateReimbursementStatus(String reimbursementNo, String status, String approvalComment);
}





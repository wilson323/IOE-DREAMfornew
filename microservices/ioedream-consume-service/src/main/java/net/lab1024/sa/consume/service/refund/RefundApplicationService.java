package net.lab1024.sa.consume.service.refund;

import net.lab1024.sa.consume.domain.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundApplicationForm;

/**
 * 退款申请服务接口
 * <p>
 * 提供退款申请相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface RefundApplicationService {

    /**
     * 提交退款申请（启动审批流程）
     *
     * @param form 退款申请表单
     * @return 退款申请实体（包含workflowInstanceId）
     */
    RefundApplicationEntity submitRefundApplication(RefundApplicationForm form);

    /**
     * 更新退款申请状态（由审批结果监听器调用）
     *
     * @param refundNo 退款申请编号
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    void updateRefundStatus(String refundNo, String status, String approvalComment);
}


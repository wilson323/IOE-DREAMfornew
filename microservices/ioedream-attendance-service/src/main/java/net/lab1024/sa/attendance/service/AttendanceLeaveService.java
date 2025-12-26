package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.entity.attendance.AttendanceLeaveEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceLeaveForm;

/**
 * 考勤请假服务接口
 * <p>
 * 提供考勤请假相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceLeaveService {

    /**
     * 提交请假申请（启动审批流程）
     *
     * @param form 请假申请表单
     * @return 请假实体（包含workflowInstanceId）
     */
    AttendanceLeaveEntity submitLeaveApplication(AttendanceLeaveForm form);

    /**
     * 更新请假申请状态（由审批结果监听器调用）
     *
     * @param leaveNo 请假申请编号
     * @param status 审批状态
     * @param approvalComment 审批意见
     */
    void updateLeaveStatus(String leaveNo, String status, String approvalComment);
}




package net.lab1024.sa.admin.module.attendance.service;

import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.attendance.domain.dto.ExceptionApplicationDTO;
import net.lab1024.sa.admin.module.attendance.domain.dto.ExceptionApprovalDTO;
import net.lab1024.sa.admin.module.attendance.domain.vo.ExceptionApplicationVO;

/**
 * 考勤异常申请服务接口
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
public interface AttendanceExceptionApplicationService {

    /**
     * 提交异常申请
     *
     * @param applicationDTO 申请信息
     * @return 申请结果
     */
    ResponseDTO<String> submitExceptionApplication(ExceptionApplicationDTO applicationDTO);

    /**
     * 审批异常申请
     *
     * @param approvalDTO 审批信息
     * @return 审批结果
     */
    ResponseDTO<String> approveExceptionApplication(ExceptionApprovalDTO approvalDTO);

    /**
     * 获取员工异常申请列表
     *
     * @param employeeId 员工ID
     * @param status 申请状态
     * @param pageParam 分页参数
     * @return 申请列表
     */
    ResponseDTO<PageResult<ExceptionApplicationVO>> getEmployeeApplications(Long employeeId, String status, PageParam pageParam);

    /**
     * 获取待审批申请列表
     *
     * @param approverId 审批人ID
     * @param pageParam 分页参数
     * @return 待审批申请列表
     */
    ResponseDTO<PageResult<ExceptionApplicationVO>> getPendingApplications(Long approverId, PageParam pageParam);

    /**
     * 取消异常申请
     *
     * @param applicationId 申请ID
     * @param employeeId 员工ID
     * @return 取消结果
     */
    ResponseDTO<String> cancelExceptionApplication(Long applicationId, Long employeeId);
}
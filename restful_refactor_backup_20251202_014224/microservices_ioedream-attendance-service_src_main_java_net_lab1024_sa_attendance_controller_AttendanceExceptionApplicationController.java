package net.lab1024.sa.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.dto.ExceptionApplicationDTO;
import net.lab1024.sa.attendance.domain.dto.ExceptionApprovalDTO;
import net.lab1024.sa.attendance.domain.vo.ExceptionApplicationVO;
import net.lab1024.sa.attendance.service.AttendanceExceptionApplicationService;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 考勤异常申请控制器
 * 整合现有审批工作流、通知管理和异常处理组件
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@RestController
@RequestMapping("/api/attendance/exception-application")
@Tag(name = "考勤异常申请管理", description = "考勤异常申请提交、审批、查询接口")
@Slf4j
public class AttendanceExceptionApplicationController {

    @Resource
    private AttendanceExceptionApplicationService exceptionApplicationService;

    /**
     * 提交异常申请
     */
    @PostMapping("/submit")
    @Operation(summary = "提交异常申请", description = "员工提交考勤异常申请，启动审批工作流")
    @SaCheckPermission("attendance:exception:submit")
    public ResponseDTO<String> submitExceptionApplication(@Valid @RequestBody ExceptionApplicationDTO applicationDTO) {
        return exceptionApplicationService.submitExceptionApplication(applicationDTO);
    }

    /**
     * 审批异常申请
     */
    @PostMapping("/approve")
    @Operation(summary = "审批异常申请", description = "审批人员审批考勤异常申请")
    @SaCheckPermission("attendance:exception:approve")
    public ResponseDTO<String> approveExceptionApplication(@Valid @RequestBody ExceptionApprovalDTO approvalDTO) {
        return exceptionApplicationService.approveExceptionApplication(approvalDTO);
    }

    /**
     * 获取员工异常申请列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "获取员工异常申请列表", description = "获取指定员工的异常申请记录")
    @SaCheckPermission("attendance:exception:query")
    public ResponseDTO<PageResult<ExceptionApplicationVO>> getEmployeeApplications(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String status,
            @Valid PageParam pageParam) {
        return exceptionApplicationService.getEmployeeApplications(employeeId, status, pageParam);
    }

    /**
     * 获取待审批申请列表
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待审批申请列表", description = "获取当前用户待审批的异常申请列表")
    @SaCheckPermission("attendance:exception:approve")
    public ResponseDTO<PageResult<ExceptionApplicationVO>> getPendingApplications(@Valid PageParam pageParam) {
        // 这里应该从SecurityContext获取当前用户ID，为了演示简化处理
        Long approverId = getCurrentUserId();
        return exceptionApplicationService.getPendingApplications(approverId, pageParam);
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/{applicationId}")
    @Operation(summary = "获取申请详情", description = "获取异常申请的详细信息")
    @SaCheckPermission("attendance:exception:query")
    public ResponseDTO<ExceptionApplicationVO> getApplicationDetail(@PathVariable Long applicationId) {
        // 这里可以调用服务获取详情，为了简化直接返回一个示例
        ExceptionApplicationVO vo = new ExceptionApplicationVO();
        vo.setApplicationId(applicationId);
        return ResponseDTO.ok(vo);
    }

    /**
     * 取消异常申请
     */
    @PostMapping("/{applicationId}/cancel")
    @Operation(summary = "取消异常申请", description = "取消待审批的异常申请")
    @SaCheckPermission("attendance:exception:cancel")
    public ResponseDTO<String> cancelExceptionApplication(@PathVariable Long applicationId) {
        Long employeeId = getCurrentUserId();
        return exceptionApplicationService.cancelExceptionApplication(applicationId, employeeId);
    }

    /**
     * 获取异常申请统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取异常申请统计", description = "获取异常申请的统计数据")
    @SaCheckPermission("attendance:exception:statistics")
    public ResponseDTO<Object> getApplicationStatistics() {
        // 这里可以返回各种统计数据，如申请数量、审批通过率等
        return ResponseDTO.ok("统计数据");
    }

    /**
     * 获取异常类型列表
     */
    @GetMapping("/exception-types")
    @Operation(summary = "获取异常类型列表", description = "获取支持的异常类型列表")
    public ResponseDTO<Object> getExceptionTypes() {
        // 返回支持的异常类型
        return ResponseDTO.ok(new Object[] {
                java.util.Map.of("code", "LATE", "description", "迟到"),
                java.util.Map.of("code", "EARLY_LEAVE", "description", "早退"),
                java.util.Map.of("code", "ABSENTEEISM", "description", "旷工"),
                java.util.Map.of("code", "OVERTIME", "description", "加班申请"),
                java.util.Map.of("code", "LEAVE", "description", "请假申请"),
                java.util.Map.of("code", "FORGOT_PUNCH", "description", "忘打卡")
        });
    }

    /**
     * 获取当前用户ID
     * 这里应该从SecurityContext获取，为了演示简化处理
     */
    private Long getCurrentUserId() {
        // 实际项目中应该从SecurityContext获取当前登录用户ID
        return 1L;
    }
}

package net.lab1024.sa.attendance.controller;

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
import net.lab1024.sa.attendance.domain.entity.AttendanceLeaveEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceLeaveForm;
import net.lab1024.sa.attendance.service.AttendanceLeaveService;

/**
 * 考勤请假控制器
 * <p>
 * 提供考勤请假相关API接口
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
@RequestMapping("/api/v1/attendance/leave")
@Tag(name = "考勤请假管理")
public class AttendanceLeaveController {

    @Resource
    private AttendanceLeaveService attendanceLeaveService;

    /**
     * 提交请假申请
     *
     * @param form 请假申请表单
     * @return 请假实体
     */
    @Observed(name = "attendanceLeave.submitLeaveApplication", contextualName = "attendance-leave-submit")
    @PostMapping("/submit")
    @Operation(summary = "提交请假申请", description = "提交请假申请并启动审批流程")
    public ResponseDTO<AttendanceLeaveEntity> submitLeaveApplication(
            @Valid @RequestBody AttendanceLeaveForm form) {
        log.info("[请假申请] 接收请假申请请求，employeeId={}, leaveType={}",
                form.getEmployeeId(), form.getLeaveType());
        AttendanceLeaveEntity entity = attendanceLeaveService.submitLeaveApplication(form);
        return ResponseDTO.ok(entity);
    }

    /**
     * 更新请假申请状态（供审批结果监听器调用）
     *
     * @param leaveNo 请假申请编号
     * @param requestParams 请求参数（包含status和approvalComment）
     * @return 操作结果
     */
    @Observed(name = "attendanceLeave.updateLeaveStatus", contextualName = "attendance-leave-update-status")
    @PutMapping("/{leaveNo}/status")
    @Operation(summary = "更新请假申请状态", description = "由审批结果监听器调用，更新请假申请状态")
    public ResponseDTO<Void> updateLeaveStatus(
            @PathVariable String leaveNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[请假申请] 接收状态更新请求，leaveNo={}", leaveNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        attendanceLeaveService.updateLeaveStatus(leaveNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}




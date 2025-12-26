package net.lab1024.sa.attendance.controller;

import lombok.extern.slf4j.Slf4j;

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
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeForm;
import net.lab1024.sa.attendance.service.AttendanceOvertimeService;
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/overtime")
@Tag(name = "考勤加班管理")
 public class AttendanceOvertimeController {

    @Resource
    private AttendanceOvertimeService attendanceOvertimeService;

    @Observed(name = "attendanceOvertime.submitOvertimeApplication", contextualName = "attendance-overtime-submit")
    @PostMapping("/submit")
    @Operation(summary = "提交加班申请", description = "提交加班申请并启动审批流程")
    public ResponseDTO<AttendanceOvertimeEntity> submitOvertimeApplication(
            @Valid @RequestBody AttendanceOvertimeForm form) {
        log.info("[加班申请] 接收加班申请请求，employeeId={}, overtimeDate={}", form.getEmployeeId(), form.getOvertimeDate());
        AttendanceOvertimeEntity entity = attendanceOvertimeService.submitOvertimeApplication(form);
        return ResponseDTO.ok(entity);
    }

    @Observed(name = "attendanceOvertime.updateOvertimeStatus", contextualName = "attendance-overtime-update-status")
    @PutMapping("/{overtimeNo}/status")
    @Operation(summary = "更新加班申请状态", description = "由审批结果监听器调用，更新加班申请状态")
    public ResponseDTO<Void> updateOvertimeStatus(
            @PathVariable String overtimeNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[加班申请] 接收状态更新请求，overtimeNo={}", overtimeNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        attendanceOvertimeService.updateOvertimeStatus(overtimeNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}


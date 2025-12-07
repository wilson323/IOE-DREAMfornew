package net.lab1024.sa.attendance.controller;

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
import net.lab1024.sa.attendance.domain.entity.AttendanceShiftEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceShiftForm;
import net.lab1024.sa.attendance.service.AttendanceShiftService;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/shift")
@Tag(name = "考勤调班管理")
public class AttendanceShiftController {

    @Resource
    private AttendanceShiftService attendanceShiftService;

    @PostMapping("/submit")
    @Operation(summary = "提交调班申请", description = "提交调班申请并启动审批流程")
    public ResponseDTO<AttendanceShiftEntity> submitShiftApplication(
            @Valid @RequestBody AttendanceShiftForm form) {
        log.info("[调班申请] 接收调班申请请求，employeeId={}, shiftDate={}", form.getEmployeeId(), form.getShiftDate());
        AttendanceShiftEntity entity = attendanceShiftService.submitShiftApplication(form);
        return ResponseDTO.ok(entity);
    }

    @PutMapping("/{shiftNo}/status")
    @Operation(summary = "更新调班申请状态", description = "由审批结果监听器调用，更新调班申请状态")
    public ResponseDTO<Void> updateShiftStatus(
            @PathVariable String shiftNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[调班申请] 接收状态更新请求，shiftNo={}", shiftNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        attendanceShiftService.updateShiftStatus(shiftNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}


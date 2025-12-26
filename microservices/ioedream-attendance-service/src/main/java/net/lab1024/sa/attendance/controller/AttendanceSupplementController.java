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
import net.lab1024.sa.common.entity.attendance.AttendanceSupplementEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceSupplementForm;
import net.lab1024.sa.attendance.service.AttendanceSupplementService;
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/supplement")
@Tag(name = "考勤补签管理")
 public class AttendanceSupplementController {

    @Resource
    private AttendanceSupplementService attendanceSupplementService;

    @Observed(name = "attendanceSupplement.submitSupplementApplication", contextualName = "attendance-supplement-submit")
    @PostMapping("/submit")
    @Operation(summary = "提交补签申请", description = "提交补签申请并启动审批流程")
    public ResponseDTO<AttendanceSupplementEntity> submitSupplementApplication(
            @Valid @RequestBody AttendanceSupplementForm form) {
        log.info("[补签申请] 接收补签申请请求，employeeId={}, supplementDate={}", form.getEmployeeId(), form.getSupplementDate());
        AttendanceSupplementEntity entity = attendanceSupplementService.submitSupplementApplication(form);
        return ResponseDTO.ok(entity);
    }

    @Observed(name = "attendanceSupplement.updateSupplementStatus", contextualName = "attendance-supplement-update-status")
    @PutMapping("/{supplementNo}/status")
    @Operation(summary = "更新补签申请状态", description = "由审批结果监听器调用，更新补签申请状态")
    public ResponseDTO<Void> updateSupplementStatus(
            @PathVariable String supplementNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[补签申请] 接收状态更新请求，supplementNo={}", supplementNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        attendanceSupplementService.updateSupplementStatus(supplementNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}


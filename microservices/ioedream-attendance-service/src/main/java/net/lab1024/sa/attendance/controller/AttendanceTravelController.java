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
import net.lab1024.sa.attendance.domain.entity.AttendanceTravelEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceTravelForm;
import net.lab1024.sa.attendance.service.AttendanceTravelService;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/travel")
@Tag(name = "考勤出差管理")
public class AttendanceTravelController {

    @Resource
    private AttendanceTravelService attendanceTravelService;

    @PostMapping("/submit")
    @Operation(summary = "提交出差申请", description = "提交出差申请并启动审批流程")
    public ResponseDTO<AttendanceTravelEntity> submitTravelApplication(
            @Valid @RequestBody AttendanceTravelForm form) {
        log.info("[出差申请] 接收出差申请请求，employeeId={}, destination={}", form.getEmployeeId(), form.getDestination());
        AttendanceTravelEntity entity = attendanceTravelService.submitTravelApplication(form);
        return ResponseDTO.ok(entity);
    }

    @PutMapping("/{travelNo}/status")
    @Operation(summary = "更新出差申请状态", description = "由审批结果监听器调用，更新出差申请状态")
    public ResponseDTO<Void> updateTravelStatus(
            @PathVariable String travelNo,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[出差申请] 接收状态更新请求，travelNo={}", travelNo);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        attendanceTravelService.updateTravelStatus(travelNo, status, approvalComment);
        return ResponseDTO.ok();
    }
}


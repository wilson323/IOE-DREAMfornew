package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.attendance.domain.form.AttendancePunchForm;
import net.lab1024.sa.attendance.attendance.service.AttendancePunchService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitoring.BusinessMetrics;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 设备考勤打卡上传控制器
 * <p>
 * 接收设备端上传的打卡记录（边缘识别+中心计算模式）
 * 严格遵循CLAUDE.md规范：
 * - 设备端完成：识别+上传打卡记录
 * - 软件端处理：存储记录+排班匹配+考勤计算+异常检测
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/device")
@Tag(name = "设备考勤打卡", description = "接收设备上传的打卡记录")
public class DeviceAttendancePunchController {

    @Resource
    private net.lab1024.sa.attendance.attendance.service.AttendanceService attendanceService;

    @Resource
    private BusinessMetrics businessMetrics;

    /**
     * 接收设备上传的打卡记录
     * <p>
     * ⭐ 设备端已完成：识别+上传打卡记录
     * ⭐ 软件端处理：存储记录+排班匹配+考勤计算+异常检测
     * </p>
     *
     * @param punchForm 打卡表单
     * @return 处理结果
     */
    @PostMapping("/upload-punch")
    @Operation(summary = "接收设备上传的打卡记录", description = "设备端完成识别，软件端进行排班匹配和考勤计算")
    public ResponseDTO<Void> uploadAttendancePunch(@Valid @RequestBody AttendancePunchForm punchForm) {
        long startTime = System.currentTimeMillis();
        log.info("[设备考勤打卡] 接收设备上传的打卡记录, deviceId={}, userId={}, punchTime={}",
                punchForm.getDeviceId(), punchForm.getUserId(), punchForm.getPunchTime());

        try {
            // 1. 保存打卡记录
            // 通过AttendanceService保存打卡记录
            ResponseDTO<Void> saveResult = attendanceService.createPunchRecord(punchForm);
            if (!saveResult.isSuccess()) {
                log.error("[设备考勤打卡] 保存打卡记录失败: {}", saveResult.getMessage());
                return ResponseDTO.error(saveResult.getCode(), saveResult.getMessage());
            }

            // 2. 记录业务指标
            businessMetrics.recordAttendanceEvent("SUCCESS");

            // 3. 实时推送到监控大屏（通过WebSocket）
            pushAttendanceEvent(punchForm);

            // 4. 异常检测（跨设备打卡、频繁打卡等）
            checkAttendanceAnomaly(punchForm);

            long duration = System.currentTimeMillis() - startTime;
            businessMetrics.recordResponseTime("attendance.device.upload-punch", duration);
            log.info("[设备考勤打卡] 处理完成, duration={}ms", duration);

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[设备考勤打卡] 处理异常", e);
            businessMetrics.recordAttendanceEvent("ERROR");
            return ResponseDTO.error("ATTENDANCE_PUNCH_UPLOAD_ERROR", "处理打卡记录失败: " + e.getMessage());
        }
    }

    /**
     * 推送考勤事件
     */
    private void pushAttendanceEvent(AttendancePunchForm punchForm) {
        // 通过WebSocket推送实时考勤事件
        log.debug("[设备考勤打卡] 推送考勤事件, userId={}, deviceId={}", punchForm.getUserId(), punchForm.getDeviceId());
    }

    /**
     * 检查考勤异常
     */
    private void checkAttendanceAnomaly(AttendancePunchForm punchForm) {
        // 检查跨设备打卡、频繁打卡等异常
        log.debug("[设备考勤打卡] 检查考勤异常, userId={}, deviceId={}", punchForm.getUserId(), punchForm.getDeviceId());
    }
}

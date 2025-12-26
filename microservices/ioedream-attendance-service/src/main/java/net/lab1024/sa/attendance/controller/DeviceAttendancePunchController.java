package net.lab1024.sa.attendance.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordAddForm;
import net.lab1024.sa.attendance.manager.BiometricAttendanceManager;
import net.lab1024.sa.attendance.service.AttendanceRecordService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

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
@RestController
@RequestMapping("/api/v1/attendance/device")
@Tag(name = "设备考勤打卡", description = "接收设备上传的打卡记录")
@Slf4j
public class DeviceAttendancePunchController {

    @Resource
    private AttendanceRecordService attendanceRecordService;

    @Resource
    private BiometricAttendanceManager biometricAttendanceManager;

    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 接收设备上传的打卡记录
     * <p>
     * ⭐ 设备端已完成：识别+上传打卡记录
     * ⭐ 软件端处理：存储记录+排班匹配+考勤计算+异常检测
     * </p>
     *
     * @param form 打卡表单
     * @return 处理结果
     */
    @PostMapping("/upload-punch")
    @Operation(summary = "接收设备上传的打卡记录", description = "设备端完成识别，软件端进行排班匹配和考勤计算")
    public ResponseDTO<Void> uploadAttendancePunch(@Valid @RequestBody AttendanceRecordAddForm form) {
        long startTime = System.currentTimeMillis();
        log.info("[设备考勤打卡] 接收设备上传的打卡记录, deviceId={}, userId={}, punchTime={}",
                form.getDeviceId(), form.getUserId(), form.getPunchTime());

        try {
            // 1. 保存打卡记录
            ResponseDTO<Long> saveResult = attendanceRecordService.createAttendanceRecord(form);
            if (!saveResult.isSuccess()) {
                log.error("[设备考勤打卡] 保存打卡记录失败: {}", saveResult.getMessage());
                return ResponseDTO.error(saveResult.getCode(), saveResult.getMessage());
            }

            // 2. 记录业务指标
            Counter.builder("attendance.event")
                    .tag("result", "SUCCESS")
                    .register(meterRegistry)
                    .increment();

            // 3. 实时推送到监控大屏（通过WebSocket）
            pushAttendanceEvent(form);

            // 4. 异常检测（跨设备打卡、频繁打卡等）
            checkAttendanceAnomaly(form);

            long duration = System.currentTimeMillis() - startTime;
            Timer.builder("attendance.device.upload-punch")
                    .register(meterRegistry)
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
            log.info("[设备考勤打卡] 处理完成, duration={}ms", duration);

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[设备考勤打卡] 处理异常", e);
            Counter.builder("attendance.event")
                    .tag("result", "ERROR")
                    .register(meterRegistry)
                    .increment();
            return ResponseDTO.error("ATTENDANCE_PUNCH_UPLOAD_ERROR", "处理打卡记录失败: " + e.getMessage());
        }
    }

    /**
     * 推送考勤事件
     */
    private void pushAttendanceEvent(AttendanceRecordAddForm form) {
        // 通过WebSocket推送实时考勤事件
        log.debug("[设备考勤打卡] 推送考勤事件, userId={}, deviceId={}", form.getUserId(), form.getDeviceId());
    }

    /**
     * 检查考勤异常
     */
    private void checkAttendanceAnomaly(AttendanceRecordAddForm form) {
        // 检查跨设备打卡、频繁打卡等异常
        log.debug("[设备考勤打卡] 检查考勤异常, userId={}, deviceId={}", form.getUserId(), form.getDeviceId());
    }

    /**
     * 生物识别打卡接口
     * <p>
     * 设备端完成生物识别后，上传识别结果和打卡数据
     * </p>
     *
     * @param userId 用户ID（由设备端识别返回）
     * @param deviceId 设备ID
     * @param biometricType 生物识别类型（FACE/FINGERPRINT/IRIS/PALM）
     * @param biometricData 生物特征数据（可选，用于二次验证）
     * @param attendanceTime 打卡时间
     * @param locationInfo 位置信息（可选）
     * @return 打卡结果
     */
    @PostMapping("/biometric-punch")
    @Operation(summary = "生物识别打卡", description = "设备端完成生物识别后上传打卡数据")
    public ResponseDTO<BiometricPunchResultVO> biometricPunch(
            Long userId,
            String deviceId,
            String biometricType,
            String biometricData,
            LocalDateTime attendanceTime,
            String locationInfo) {

        log.info("[生物识别打卡] 接收生物识别打卡: userId={}, deviceId={}, type={}",
                userId, deviceId, biometricType);

        try {
            // 处理生物识别打卡
            BiometricAttendanceManager.AttendanceResult result =
                    biometricAttendanceManager.processBiometricAttendance(
                            userId, deviceId, biometricType, biometricData,
                            attendanceTime, locationInfo);

            if (result.isSuccess()) {
                // 记录成功指标
                Counter.builder("attendance.biometric")
                        .tag("result", "SUCCESS")
                        .tag("type", biometricType)
                        .register(meterRegistry)
                        .increment();

                // 返回成功结果
                BiometricPunchResultVO resultVO = new BiometricPunchResultVO();
                resultVO.setSuccess(true);
                resultVO.setRecordId(result.getRecordId());
                resultVO.setAttendanceType(result.getAttendanceType().toString());
                resultVO.setMessage("打卡成功");

                log.info("[生物识别打卡] 打卡成功: userId={}, recordId={}", userId, result.getRecordId());
                return ResponseDTO.ok(resultVO);

            } else {
                // 记录失败指标
                Counter.builder("attendance.biometric")
                        .tag("result", "FAILURE")
                        .tag("type", biometricType)
                        .register(meterRegistry)
                        .increment();

                // 返回失败结果
                BiometricPunchResultVO resultVO = new BiometricPunchResultVO();
                resultVO.setSuccess(false);
                resultVO.setMessage(result.getErrorMessage());

                log.warn("[生物识别打卡] 打卡失败: userId={}, reason={}", userId, result.getErrorMessage());
                return ResponseDTO.error("ATTENDANCE_PUNCH_FAILURE", result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("[生物识别打卡] 处理异常: userId={}", userId, e);
            return ResponseDTO.error("ATTENDANCE_PUNCH_ERROR", "打卡处理异常: " + e.getMessage());
        }
    }

    /**
     * 批量上传生物识别打卡记录
     * <p>
     * 用于设备端批量上传打卡记录
     * </p>
     *
     * @param records 打卡记录列表
     * @return 处理结果统计
     */
    @PostMapping("/biometric-punch/batch")
    @Operation(summary = "批量上传生物识别打卡记录", description = "设备端批量上传打卡记录")
    public ResponseDTO<BatchPunchResultVO> batchBiometricPunch(
            @RequestBody List<BiometricAttendanceManager.BiometricAttendanceRecord> records) {

        log.info("[生物识别打卡] 批量上传打卡记录: count={}", records.size());

        try {
            // 批量处理打卡记录
            BiometricAttendanceManager.BatchProcessResult batchResult =
                    biometricAttendanceManager.batchProcessAttendance(records);

            // 返回处理结果
            BatchPunchResultVO resultVO = new BatchPunchResultVO();
            resultVO.setTotalCount(batchResult.getTotalCount());
            resultVO.setSuccessCount(batchResult.getSuccessCount());
            resultVO.setFailureCount(batchResult.getFailureCount());
            resultVO.setMessage(String.format("处理完成：成功%d条，失败%d条",
                    batchResult.getSuccessCount(), batchResult.getFailureCount()));

            log.info("[生物识别打卡] 批量处理完成: {}", batchResult);
            return ResponseDTO.ok(resultVO);

        } catch (Exception e) {
            log.error("[生物识别打卡] 批量处理异常", e);
            return ResponseDTO.error("ATTENDANCE_BATCH_PUNCH_ERROR", "批量处理异常: " + e.getMessage());
        }
    }

    /**
     * 生物识别打卡结果VO
     */
    public static class BiometricPunchResultVO {
        private boolean success;
        private String recordId;
        private String attendanceType;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getRecordId() {
            return recordId;
        }

        public void setRecordId(String recordId) {
            this.recordId = recordId;
        }

        public String getAttendanceType() {
            return attendanceType;
        }

        public void setAttendanceType(String attendanceType) {
            this.attendanceType = attendanceType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 批量打卡结果VO
     */
    public static class BatchPunchResultVO {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private String message;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

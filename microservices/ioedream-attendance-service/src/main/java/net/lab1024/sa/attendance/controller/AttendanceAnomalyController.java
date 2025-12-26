package net.lab1024.sa.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.attendance.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.service.AttendanceAnomalyDetectionService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 考勤异常管理Controller
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/anomaly")
@Tag(name = "考勤异常管理")
public class AttendanceAnomalyController {

    private final AttendanceAnomalyDetectionService detectionService;

    public AttendanceAnomalyController(AttendanceAnomalyDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    /**
     * 分页查询异常记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询异常记录")
    public ResponseDTO<Page<AttendanceAnomalyEntity>> pageAnomalies(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String anomalyType,
            @RequestParam(required = false) String anomalyStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("[异常管理] 分页查询异常记录: pageNum={}, pageSize={}, userId={}, type={}, status={}",
                pageNum, pageSize, userId, anomalyType, anomalyStatus);

        QueryWrapper<AttendanceAnomalyEntity> queryWrapper = new QueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (departmentId != null) {
            queryWrapper.eq("department_id", departmentId);
        }
        if (anomalyType != null) {
            queryWrapper.eq("anomaly_type", anomalyType);
        }
        if (anomalyStatus != null) {
            queryWrapper.eq("anomaly_status", anomalyStatus);
        }
        if (startDate != null) {
            queryWrapper.ge("attendance_date", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("attendance_date", endDate);
        }

        queryWrapper.orderByDesc("attendance_date", "create_time");

        Page<AttendanceAnomalyEntity> page = new Page<>(pageNum, pageSize);
        // TODO: 调用DAO分页查询

        return ResponseDTO.ok(page);
    }

    /**
     * 查询异常详情
     */
    @GetMapping("/{anomalyId}")
    @Operation(summary = "查询异常详情")
    public ResponseDTO<AttendanceAnomalyEntity> getAnomalyDetail(@PathVariable Long anomalyId) {
        log.info("[异常管理] 查询异常详情: anomalyId={}", anomalyId);
        // TODO: 实现查询逻辑
        return ResponseDTO.ok();
    }

    /**
     * 手动触发异常检测
     */
    @PostMapping("/detect/{attendanceDate}")
    @Operation(summary = "手动触发异常检测")
    public ResponseDTO<Integer> triggerDetection(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate) {

        log.info("[异常管理] 手动触发异常检测: date={}", attendanceDate);

        Integer count = detectionService.autoDetectAndCreate(attendanceDate);

        log.info("[异常管理] 异常检测完成: date={}, detectedCount={}", attendanceDate, count);
        return ResponseDTO.ok(count);
    }

    /**
     * 忽略异常
     */
    @PutMapping("/{anomalyId}/ignore")
    @Operation(summary = "忽略异常")
    public ResponseDTO<Void> ignoreAnomaly(@PathVariable Long anomalyId,
                                           @RequestParam Long handlerId,
                                           @RequestParam String handlerName,
                                           @RequestParam(required = false) String comment) {
        log.info("[异常管理] 忽略异常: anomalyId={}, handlerId={}", anomalyId, handlerId);
        // TODO: 实现忽略逻辑
        return ResponseDTO.ok();
    }

    /**
     * 修正异常
     */
    @PutMapping("/{anomalyId}/correct")
    @Operation(summary = "修正异常")
    public ResponseDTO<Void> correctAnomaly(@PathVariable Long anomalyId,
                                             @RequestParam Long handlerId,
                                             @RequestParam String handlerName,
                                             @RequestParam(required = false) String comment) {
        log.info("[异常管理] 修正异常: anomalyId={}, handlerId={}", anomalyId, handlerId);
        // TODO: 实现修正逻辑
        return ResponseDTO.ok();
    }
}

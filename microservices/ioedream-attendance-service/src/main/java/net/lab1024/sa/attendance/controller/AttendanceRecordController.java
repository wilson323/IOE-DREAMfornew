package net.lab1024.sa.attendance.controller;

import io.micrometer.observation.annotation.Observed;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordStatisticsVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.attendance.service.AttendanceRecordService;

import jakarta.annotation.Resource;
import java.time.LocalDate;

/**
 * 考勤记录管理PC端控制器
 * <p>
 * 提供PC端考勤记录管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 考勤记录查询（分页）
 * - 考勤记录统计
 * - 考勤记录导出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/record")
@Tag(name = "考勤记录管理PC端", description = "考勤记录查询、统计、导出等API")
public class AttendanceRecordController {

    @Resource
    private AttendanceRecordService attendanceRecordService;

    /**
     * 分页查询考勤记录
     * <p>
     * 支持多条件筛选：员工ID、部门ID、日期范围、考勤状态、考勤类型等
     * 支持分页查询，默认每页20条记录
     * </p>
     *
     * @param pageNum 页码（从1开始，默认1）
     * @param pageSize 每页大小（默认20）
     * @param employeeId 员工ID（可选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param status 考勤状态（可选：NORMAL-正常、LATE-迟到、EARLY-早退、ABSENT-缺勤）
     * @param attendanceType 考勤类型（可选：CHECK_IN-上班、CHECK_OUT-下班）
     * @param departmentId 部门ID（可选）
     * @return 分页的考勤记录列表
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/attendance/record/query?pageNum=1&pageSize=20&employeeId=1001&startDate=2025-01-01&endDate=2025-01-31
     * </pre>
     */
    @Observed(name = "attendanceRecord.queryAttendanceRecords", contextualName = "attendance-record-query")
    @GetMapping("/query")
    @Operation(
        summary = "分页查询考勤记录",
        description = "根据条件分页查询考勤记录，支持多条件筛选（员工ID、部门ID、日期范围、考勤状态、考勤类型等）。支持分页查询，默认每页20条记录。严格遵循RESTful规范：查询操作使用GET方法。",
        tags = {"考勤记录管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResult.class)
        )
    )
    @PreAuthorize("hasRole('ATTENDANCE_MANAGER')")
    public ResponseDTO<PageResult<AttendanceRecordVO>> queryAttendanceRecords(
            @Parameter(description = "页码（从1开始）", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "员工ID（可选）", example = "1001")
            @RequestParam(required = false) Long employeeId,
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String attendanceType,
            @RequestParam(required = false) Long departmentId) {
        log.info("[考勤记录] 分页查询考勤记录，pageNum={}, pageSize={}, employeeId={}, startDate={}, endDate={}, status={}, attendanceType={}, departmentId={}",
                pageNum, pageSize, employeeId, startDate, endDate, status, attendanceType, departmentId);

        // 构建查询表单
        AttendanceRecordQueryForm form = new AttendanceRecordQueryForm();
        form.setPageNum(pageNum);
        form.setPageSize(pageSize);
        form.setEmployeeId(employeeId);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setStatus(status);
        form.setAttendanceType(attendanceType);
        form.setDepartmentId(departmentId);

        return attendanceRecordService.queryAttendanceRecords(form);
    }

    /**
     * 获取考勤记录统计
     * <p>
     * 根据时间范围和员工ID获取考勤记录统计数据，包括正常出勤、迟到、早退、缺勤等统计信息
     * </p>
     *
     * @param startDate 开始日期（必填，格式：yyyy-MM-dd）
     * @param endDate 结束日期（必填，格式：yyyy-MM-dd）
     * @param employeeId 员工ID（可选，不指定则统计所有员工）
     * @return 统计数据，包含正常出勤、迟到、早退、缺勤等统计信息
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/attendance/record/statistics?startDate=2025-01-01&endDate=2025-01-31&employeeId=1001
     * </pre>
     */

    /**
     * 创建考勤记录
     * <p>
     * 用于设备协议推送考勤记录
     * 支持设备自动推送和手动创建
     * </p>
     *
     * @param form 考勤记录创建表单
     * @return 创建的考勤记录ID
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/attendance/record/create
     * {
     *   "userId": 1001,
     *   "deviceId": 1,
     *   "deviceCode": "DEV001",
     *   "punchTime": 1706582400,
     *   "punchType": 0,
     *   "punchAddress": "北京市朝阳区"
     * }
     * </pre>
     */
    @Observed(name = "attendanceRecord.createAttendanceRecord", contextualName = "attendance-record-create")
    @PostMapping("/create")
    @Operation(
        summary = "创建考勤记录",
        description = "创建考勤记录，用于设备协议推送考勤记录。支持设备自动推送和手动创建。",
        tags = {"考勤记录管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "创建成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Long.class)
        )
    )
    public ResponseDTO<Long> createAttendanceRecord(
            @Valid @RequestBody AttendanceRecordAddForm form) {
        log.info("[考勤记录] 创建考勤记录，userId={}, deviceId={}, punchType={}",
                form.getUserId(), form.getDeviceId(), form.getPunchType());
        return attendanceRecordService.createAttendanceRecord(form);
    }

    @Observed(name = "attendanceRecord.getAttendanceRecordStatistics", contextualName = "attendance-record-statistics")
    @GetMapping("/statistics")
    @Operation(
        summary = "获取考勤记录统计",
        description = "根据时间范围和员工ID获取考勤记录统计数据，包括正常出勤、迟到、早退、缺勤等统计信息。如果不指定员工ID，则统计所有员工的考勤数据。",
        tags = {"考勤记录管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttendanceRecordStatisticsVO.class)
        )
    )
    @PreAuthorize("hasRole('ATTENDANCE_MANAGER')")
    public ResponseDTO<AttendanceRecordStatisticsVO> getAttendanceRecordStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd", required = true, example = "2025-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "员工ID（可选）", example = "1001")
            @RequestParam(required = false) Long employeeId) {
        log.info("[考勤记录] 获取考勤记录统计，startDate={}, endDate={}, employeeId={}", startDate, endDate, employeeId);
        return attendanceRecordService.getAttendanceRecordStatistics(startDate, endDate, employeeId);
    }
}




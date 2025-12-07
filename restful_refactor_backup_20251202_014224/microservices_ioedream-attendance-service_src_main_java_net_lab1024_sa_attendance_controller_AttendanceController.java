package net.lab1024.sa.attendance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.attendance.service.AttendanceService;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 考勤管理控制器
 *
 * @author IOE-DREAM Team
 */
@RestController
@RequestMapping("/attendance")
@Tag(name = "考勤管理", description = "打卡记录、考勤统计、排班管理")
@Validated
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    /**
     * 分页查询考勤记录
     */
    @GetMapping("/records/page")
    @Operation(summary = "分页查询考勤记录")
    public ResponseDTO<PageResult<Map<String, Object>>> queryAttendancePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer attendanceType) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(current.longValue());
        pageParam.setPageSize(size.longValue());

        PageResult<Map<String, Object>> result = attendanceService.queryAttendancePage(
                pageParam, employeeId, startDate, endDate, attendanceType);

        return ResponseDTO.ok(result);
    }

    /**
     * 员工打卡
     */
    @PostMapping("/punch")
    @Operation(summary = "员工打卡")
    public ResponseDTO<String> punchAttendance(@Valid @RequestBody Map<String, Object> punchData) {
        try {
            attendanceService.punchAttendance(punchData);
            return ResponseDTO.ok("打卡成功");
        } catch (Exception e) {
            return ResponseDTO.error("打卡失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日考勤状态
     */
    @GetMapping("/today/{employeeId}")
    @Operation(summary = "获取今日考勤状态")
    public ResponseDTO<Map<String, Object>> getTodayAttendanceStatus(@PathVariable Long employeeId) {
        Map<String, Object> status = attendanceService.getTodayAttendanceStatus(employeeId);
        return ResponseDTO.ok(status);
    }

    /**
     * 获取考勤统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取考勤统计")
    public ResponseDTO<Map<String, Object>> getAttendanceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId) {
        Map<String, Object> statistics = attendanceService.getAttendanceStatistics(startDate, endDate, departmentId);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取考勤异常记录
     */
    @GetMapping("/exceptions/page")
    @Operation(summary = "分页查询考勤异常记录")
    public ResponseDTO<PageResult<Map<String, Object>>> queryAttendanceExceptions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer exceptionType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(current.longValue());
        pageParam.setPageSize(size.longValue());

        PageResult<Map<String, Object>> result = attendanceService.queryAttendanceExceptions(
                pageParam, employeeId, exceptionType, startDate, endDate);

        return ResponseDTO.ok(result);
    }

    /**
     * 处理考勤异常
     */
    @PostMapping("/exceptions/{exceptionId}/process")
    @Operation(summary = "处理考勤异常")
    public ResponseDTO<String> processAttendanceException(
            @PathVariable Long exceptionId,
            @Valid @RequestBody Map<String, Object> processData) {
        try {
            attendanceService.processAttendanceException(exceptionId, processData);
            return ResponseDTO.ok("异常处理完成");
        } catch (Exception e) {
            return ResponseDTO.error("处理异常失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入考勤数据
     */
    @PostMapping("/batch-import")
    @Operation(summary = "批量导入考勤数据")
    public ResponseDTO<String> batchImportAttendance(@RequestBody List<Map<String, Object>> attendanceData) {
        try {
            attendanceService.batchImportAttendance(attendanceData);
            return ResponseDTO.ok("批量导入成功");
        } catch (Exception e) {
            return ResponseDTO.error("批量导入失败: " + e.getMessage());
        }
    }
}

package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyUpdateForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceOvertimeApplyVO;
import net.lab1024.sa.attendance.service.AttendanceOvertimeApplyService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 加班申请Controller
 * <p>
 * 提供加班申请的REST API接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@Tag(name = "加班申请管理")
@RequestMapping("/api/attendance/overtime/apply")
public class AttendanceOvertimeApplyController {

    @Resource
    private AttendanceOvertimeApplyService overtimeApplyService;

    @Operation(summary = "分页查询加班申请")
    @PostMapping("/page")
    public ResponseDTO<PageResult<AttendanceOvertimeApplyVO>> queryPage(@RequestBody AttendanceOvertimeApplyQueryForm form) {
        log.info("[加班申请API] 分页查询: form={}", form);
        PageResult<AttendanceOvertimeApplyVO> result = overtimeApplyService.queryPage(form);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "查询加班申请详情")
    @GetMapping("/{applyId}")
    public ResponseDTO<AttendanceOvertimeApplyVO> queryDetail(@PathVariable Long applyId) {
        log.info("[加班申请API] 查询详情: applyId={}", applyId);
        AttendanceOvertimeApplyVO result = overtimeApplyService.queryDetail(applyId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增加班申请")
    @PostMapping("/add")
    public ResponseDTO<Long> add(@Valid @RequestBody AttendanceOvertimeApplyAddForm form) {
        log.info("[加班申请API] 新增申请: form={}", form);
        Long applyId = overtimeApplyService.add(form);
        return ResponseDTO.ok(applyId);
    }

    @Operation(summary = "更新加班申请")
    @PostMapping("/update/{applyId}")
    public ResponseDTO<Void> update(@PathVariable Long applyId, @Valid @RequestBody AttendanceOvertimeApplyUpdateForm form) {
        log.info("[加班申请API] 更新申请: applyId={}, form={}", applyId, form);
        overtimeApplyService.update(applyId, form);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除加班申请")
    @PostMapping("/delete/{applyId}")
    public ResponseDTO<Void> delete(@PathVariable Long applyId) {
        log.info("[加班申请API] 删除申请: applyId={}", applyId);
        overtimeApplyService.delete(applyId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "批量删除加班申请")
    @PostMapping("/batchDelete")
    public ResponseDTO<Void> batchDelete(@RequestBody List<Long> applyIds) {
        log.info("[加班申请API] 批量删除: applyIds={}", applyIds);
        overtimeApplyService.batchDelete(applyIds);
        return ResponseDTO.ok();
    }

    @Operation(summary = "提交加班申请")
    @PostMapping("/submit/{applyId}")
    public ResponseDTO<Void> submit(@PathVariable Long applyId) {
        log.info("[加班申请API] 提交申请: applyId={}", applyId);
        overtimeApplyService.submit(applyId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "撤销加班申请")
    @PostMapping("/cancel/{applyId}")
    public ResponseDTO<Void> cancel(@PathVariable Long applyId, @RequestParam String cancelReason) {
        log.info("[加班申请API] 撤销申请: applyId={}, reason={}", applyId, cancelReason);
        overtimeApplyService.cancel(applyId, cancelReason);
        return ResponseDTO.ok();
    }

    @Operation(summary = "审批通过")
    @PostMapping("/approve")
    public ResponseDTO<Void> approve(
        @RequestParam Long applyId,
        @RequestParam Integer approvalLevel,
        @RequestParam Long approverId,
        @RequestParam(required = false) String approvalComment
    ) {
        log.info("[加班申请API] 审批通过: applyId={}, level={}, approverId={}", applyId, approvalLevel, approverId);
        overtimeApplyService.approve(applyId, approvalLevel, approverId, approvalComment);
        return ResponseDTO.ok();
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/reject")
    public ResponseDTO<Void> reject(
        @RequestParam Long applyId,
        @RequestParam Integer approvalLevel,
        @RequestParam Long approverId,
        @RequestParam String rejectReason
    ) {
        log.info("[加班申请API] 审批驳回: applyId={}, level={}, approverId={}, reason={}", applyId, approvalLevel, approverId, rejectReason);
        overtimeApplyService.reject(applyId, approvalLevel, approverId, rejectReason);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询我的加班申请")
    @GetMapping("/my/{applicantId}")
    public ResponseDTO<List<AttendanceOvertimeApplyVO>> queryMyApplications(@PathVariable Long applicantId) {
        log.info("[加班申请API] 查询我的申请: applicantId={}", applicantId);
        List<AttendanceOvertimeApplyVO> result = overtimeApplyService.queryMyApplications(applicantId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "查询待我审批的申请")
    @GetMapping("/pending/{approverId}")
    public ResponseDTO<List<AttendanceOvertimeApplyVO>> queryPendingApprovals(@PathVariable Long approverId) {
        log.info("[加班申请API] 查询待审批: approverId={}", approverId);
        List<AttendanceOvertimeApplyVO> result = overtimeApplyService.queryPendingApprovals(approverId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "统计部门加班时长")
    @GetMapping("/statistics/department")
    public ResponseDTO<java.math.BigDecimal> sumOvertimeHoursByDepartment(
        @RequestParam Long departmentId,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        log.info("[加班申请API] 统计部门加班: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);
        java.math.BigDecimal result = overtimeApplyService.sumOvertimeHoursByDepartment(departmentId, startDate, endDate);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "部门统计报表")
    @GetMapping("/statistics/department/report")
    public ResponseDTO<Map<String, Object>> generateDepartmentStatistics(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        log.info("[加班申请API] 部门统计报表: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = overtimeApplyService.generateDepartmentStatistics(startDate, endDate);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "员工统计报表")
    @GetMapping("/statistics/employee/report")
    public ResponseDTO<Map<String, Object>> generateEmployeeStatistics(
        @RequestParam(required = false) Long departmentId,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        log.info("[加班申请API] 员工统计报表: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);
        Map<String, Object> result = overtimeApplyService.generateEmployeeStatistics(departmentId, startDate, endDate);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "类型统计报表")
    @GetMapping("/statistics/type/report")
    public ResponseDTO<Map<String, Object>> generateTypeStatistics(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        log.info("[加班申请API] 类型统计报表: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = overtimeApplyService.generateTypeStatistics(startDate, endDate);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "导出加班申请数据")
    @PostMapping("/export")
    public ResponseDTO<byte[]> exportData(@RequestBody AttendanceOvertimeApplyQueryForm form) {
        log.info("[加班申请API] 导出数据: form={}", form);
        byte[] data = overtimeApplyService.exportData(form);
        return ResponseDTO.ok(data);
    }
}

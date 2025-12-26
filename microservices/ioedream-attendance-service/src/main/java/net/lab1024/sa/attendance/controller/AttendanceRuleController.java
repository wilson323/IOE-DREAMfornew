package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.AttendanceRuleAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRuleUpdateForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRuleVO;
import net.lab1024.sa.attendance.service.AttendanceRuleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 考勤规则控制器
 * <p>
 * 提供考勤规则管理相关API接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/rule")
@Tag(name = "考勤规则管理")
public class AttendanceRuleController {

    @Resource
    private AttendanceRuleService attendanceRuleService;

    /**
     * 分页查询考勤规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Observed(name = "attendanceRule.queryPage", contextualName = "query-rule-page")
    @GetMapping("/page")
    @Operation(summary = "分页查询考勤规则", description = "分页查询考勤规则列表")
    public ResponseDTO<PageResult<AttendanceRuleVO>> queryPage(AttendanceRuleQueryForm queryForm) {
        log.info("[考勤规则] 分页查询规则: queryForm={}", queryForm);
        PageResult<AttendanceRuleVO> pageResult = attendanceRuleService.queryRulePage(queryForm);
        log.info("[考勤规则] 分页查询规则成功: total={}", pageResult.getTotal());
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询员工考勤规则
     *
     * @param employeeId 员工ID
     * @return 规则列表
     */
    @Observed(name = "attendanceRule.getEmployeeRules", contextualName = "get-employee-rules")
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "查询员工考勤规则", description = "查询指定员工的考勤规则，包括全局规则和个人规则")
    public ResponseDTO<List<AttendanceRuleVO>> getEmployeeRules(
            @PathVariable @Parameter(description = "员工ID", required = true) Long employeeId) {
        log.info("[考勤规则] 查询员工规则: employeeId={}", employeeId);
        List<AttendanceRuleVO> rules = attendanceRuleService.getEmployeeRules(employeeId);
        log.info("[考勤规则] 查询员工规则成功: employeeId={}, count={}", employeeId, rules.size());
        return ResponseDTO.ok(rules);
    }

    /**
     * 查询部门考勤规则
     *
     * @param departmentId 部门ID
     * @return 规则列表
     */
    @Observed(name = "attendanceRule.getDepartmentRules", contextualName = "get-department-rules")
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "查询部门考勤规则", description = "查询指定部门的考勤规则，包括全局规则和部门规则")
    public ResponseDTO<List<AttendanceRuleVO>> getDepartmentRules(
            @PathVariable @Parameter(description = "部门ID", required = true) Long departmentId) {
        log.info("[考勤规则] 查询部门规则: departmentId={}", departmentId);
        List<AttendanceRuleVO> rules = attendanceRuleService.getDepartmentRules(departmentId);
        log.info("[考勤规则] 查询部门规则成功: departmentId={}, count={}", departmentId, rules.size());
        return ResponseDTO.ok(rules);
    }

    /**
     * 查询规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    @Observed(name = "attendanceRule.getRuleDetail", contextualName = "get-rule-detail")
    @GetMapping("/{ruleId}")
    @Operation(summary = "查询规则详情", description = "查询指定规则的详细信息")
    public ResponseDTO<AttendanceRuleVO> getRuleDetail(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId) {
        log.info("[考勤规则] 查询规则详情: ruleId={}", ruleId);
        AttendanceRuleVO rule = attendanceRuleService.getRuleDetail(ruleId);
        log.info("[考勤规则] 查询规则详情成功: ruleId={}", ruleId);
        return ResponseDTO.ok(rule);
    }

    /**
     * 创建考勤规则
     *
     * @param addForm 新增表单
     * @return 规则ID
     */
    @Observed(name = "attendanceRule.createRule", contextualName = "create-rule")
    @PostMapping
    @Operation(summary = "创建考勤规则", description = "创建新的考勤规则")
    public ResponseDTO<Long> createRule(@Valid @RequestBody AttendanceRuleAddForm addForm) {
        log.info("[考勤规则] 创建规则: ruleName={}, ruleScope={}", addForm.getRuleName(), addForm.getRuleScope());
        Long ruleId = attendanceRuleService.createRule(addForm);
        log.info("[考勤规则] 创建规则成功: ruleId={}", ruleId);
        return ResponseDTO.ok(ruleId);
    }

    /**
     * 更新考勤规则
     *
     * @param ruleId 规则ID
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @Observed(name = "attendanceRule.updateRule", contextualName = "update-rule")
    @PutMapping("/{ruleId}")
    @Operation(summary = "更新考勤规则", description = "更新指定的考勤规则配置")
    public ResponseDTO<Void> updateRule(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId,
            @Valid @RequestBody AttendanceRuleUpdateForm updateForm) {
        log.info("[考勤规则] 更新规则: ruleId={}, ruleName={}", ruleId, updateForm.getRuleName());
        attendanceRuleService.updateRule(ruleId, updateForm);
        log.info("[考勤规则] 更新规则成功: ruleId={}", ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 删除考勤规则
     *
     * @param ruleId 规则ID
     * @return 删除结果
     */
    @Observed(name = "attendanceRule.deleteRule", contextualName = "delete-rule")
    @DeleteMapping("/{ruleId}")
    @Operation(summary = "删除考勤规则", description = "删除指定的考勤规则")
    public ResponseDTO<Void> deleteRule(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId) {
        log.info("[考勤规则] 删除规则: ruleId={}", ruleId);
        attendanceRuleService.deleteRule(ruleId);
        log.info("[考勤规则] 删除规则成功: ruleId={}", ruleId);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除考勤规则
     *
     * @param ruleIds 规则ID列表
     * @return 删除结果
     */
    @Observed(name = "attendanceRule.batchDeleteRules", contextualName = "batch-delete-rules")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除考勤规则", description = "批量删除指定的考勤规则")
    public ResponseDTO<Void> batchDeleteRules(
            @RequestBody @Parameter(description = "规则ID列表", required = true) List<Long> ruleIds) {
        log.info("[考勤规则] 批量删除规则: count={}", ruleIds.size());
        attendanceRuleService.batchDeleteRules(ruleIds);
        log.info("[考勤规则] 批量删除规则成功: count={}", ruleIds.size());
        return ResponseDTO.ok();
    }
}

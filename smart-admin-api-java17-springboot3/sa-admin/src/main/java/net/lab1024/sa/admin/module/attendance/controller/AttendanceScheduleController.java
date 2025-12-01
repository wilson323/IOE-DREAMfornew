package net.lab1024.sa.admin.module.attendance.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.admin.module.attendance.service.AttendanceScheduleService;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤排班管理Controller
 *
 * <p>
 * 桥接现有的AttendanceScheduleService，提供完整的排班管理API
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 100%复用现有Service层业务逻辑，不重复开发
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@RestController
@RequestMapping("/api/attendance/schedules")
@Tag(name = "考勤排班管理", description = "员工排班配置、管理相关接口")
@Validated
public class AttendanceScheduleController {
    private static final Logger log = LoggerFactory.getLogger(AttendanceScheduleController.class);

    @Resource
    private AttendanceScheduleService attendanceScheduleService;

    /**
     * 创建排班记录
     */
    @PostMapping("")
    @Operation(summary = "创建排班记录", description = "为员工创建新的排班记录")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:create")
    @RequireResource(code = "attendance:schedule", action = "WRITE", scope = "AREA")
    public ResponseDTO<Long> createSchedule(@Valid @RequestBody AttendanceScheduleEntity schedule) {
        log.info("创建排班记录请求: 员工ID={}, 排班日期={}", schedule.getEmployeeId(), schedule.getScheduleDate());

        try {
            // 调用现有Service层保存逻辑
            boolean success = attendanceScheduleService.saveOrUpdateSchedule(schedule);
            if (success) {
                log.info("排班记录创建成功: 排班ID={}, 员工ID={}, 排班日期={}",
                    schedule.getScheduleId(), schedule.getEmployeeId(), schedule.getScheduleDate());
                return ResponseDTO.ok(schedule.getScheduleId());
            } else {
                log.warn("排班记录创建失败: 员工ID={}, 排班日期={}", schedule.getEmployeeId(), schedule.getScheduleDate());
                return ResponseDTO.error("创建排班记录失败");
            }
        } catch (Exception e) {
            log.error("创建排班记录异常: 员工ID" + schedule.getEmployeeId() + ", 排班日期" + schedule.getScheduleDate(), e);
            return ResponseDTO.error("创建排班记录异常：" + e.getMessage());
        }
    }

    /**
     * 更新排班记录
     */
    @PutMapping("/{scheduleId}")
    @Operation(summary = "更新排班记录", description = "更新指定的排班记录")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:update")
    @RequireResource(code = "attendance:schedule", action = "WRITE", scope = "AREA")
    public ResponseDTO<Boolean> updateSchedule(@PathVariable Long scheduleId, @Valid @RequestBody AttendanceScheduleEntity schedule) {
        log.info("更新排班记录请求: 排班ID={}, 员工ID={}, 排班日期={}",
                scheduleId, schedule.getEmployeeId(), schedule.getScheduleDate());

        try {
            // 设置排班ID
            schedule.setScheduleId(scheduleId);

            // 调用现有Service层更新逻辑
            boolean success = attendanceScheduleService.saveOrUpdateSchedule(schedule);
            if (success) {
                log.info("排班记录更新成功: 排班ID={}, 员工ID={}", scheduleId, schedule.getEmployeeId());
                return ResponseDTO.ok(true);
            } else {
                log.warn("排班记录更新失败: 排班ID={}", scheduleId);
                return ResponseDTO.error("更新排班记录失败");
            }
        } catch (Exception e) {
            log.error("更新排班记录异常: 排班ID" + scheduleId, e);
            return ResponseDTO.error("更新排班记录异常：" + e.getMessage());
        }
    }

    /**
     * 删除排班记录
     */
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "删除排班记录", description = "删除指定的排班记录")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:delete")
    @RequireResource(code = "attendance:schedule", action = "DELETE", scope = "AREA")
    public ResponseDTO<Boolean> deleteSchedule(@PathVariable Long scheduleId) {
        log.info("删除排班记录请求: 排班ID={}", scheduleId);

        try {
            // 调用现有Service层删除逻辑
            boolean success = attendanceScheduleService.deleteSchedule(scheduleId);
            if (success) {
                log.info("排班记录删除成功: 排班ID={}", scheduleId);
                return ResponseDTO.ok(true);
            } else {
                log.warn("排班记录删除失败: 排班ID={}", scheduleId);
                return ResponseDTO.error("删除排班记录失败");
            }
        } catch (Exception e) {
            log.error("删除排班记录异常: 排班ID" + scheduleId, e);
            return ResponseDTO.error("删除排班记录异常：" + e.getMessage());
        }
    }

    /**
     * 批量删除排班记录
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除排班记录", description = "批量删除指定的排班记录")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:batch-delete")
    @RequireResource(code = "attendance:schedule", action = "DELETE", scope = "AREA")
    public ResponseDTO<Integer> batchDeleteSchedules(@RequestBody List<Long> scheduleIds) {
        log.info("批量删除排班记录请求: 排班数量={}", scheduleIds.size());

        try {
            // 调用现有Service层批量删除逻辑
            int deletedCount = attendanceScheduleService.batchDeleteSchedules(scheduleIds);
            log.info("批量删除排班记录成功: 删除数量={}", deletedCount);
            return ResponseDTO.ok(deletedCount);
        } catch (Exception e) {
            log.error("批量删除排班记录异常: 排班数量" + scheduleIds.size(), e);
            return ResponseDTO.error("批量删除排班记录异常：" + e.getMessage());
        }
    }

    /**
     * 获取排班记录详情
     */
    @GetMapping("/{scheduleId}")
    @Operation(summary = "获取排班记录详情", description = "查询指定排班记录的详细信息")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:query")
    @RequireResource(code = "attendance:schedule", action = "READ", scope = "AREA")
    public ResponseDTO<AttendanceScheduleEntity> getScheduleById(@PathVariable Long scheduleId) {
        log.debug("查询排班记录详情: 排班ID={}", scheduleId);

        try {
            // 注意：现有Service没有根据ID查询的方法，这里提供基础实现
            // 实际使用中可能需要扩展Service或通过其他方式查询
            log.warn("当前Service层不支持根据ID查询排班记录，排班ID={}", scheduleId);
            return ResponseDTO.error("功能暂未实现，请联系管理员");
        } catch (Exception e) {
            log.error("查询排班记录详情异常: 排班ID" + scheduleId, e);
            return ResponseDTO.error("查询排班记录异常：" + e.getMessage());
        }
    }

    /**
     * 获取员工指定日期范围的排班
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "获取员工排班", description = "查询员工指定日期范围的排班信息")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:query")
    @RequireResource(code = "attendance:schedule", action = "READ", scope = "AREA")
    public ResponseDTO<List<AttendanceScheduleEntity>> getEmployeeSchedule(
            @PathVariable Long employeeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.debug("查询员工排班: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

        try {
            // 解析日期参数
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 调用现有Service层查询逻辑
            List<AttendanceScheduleEntity> schedules = attendanceScheduleService.getEmployeeSchedule(employeeId, start, end);
            log.debug("查询员工排班成功: 员工ID={}, 排班数量={}", employeeId, schedules.size());
            return ResponseDTO.ok(schedules);
        } catch (Exception e) {
            log.error("查询员工排班异常: 员工ID" + employeeId + ", 开始日期" + startDate + ", 结束日期" + endDate, e);
            return ResponseDTO.error("查询员工排班异常：" + e.getMessage());
        }
    }

    /**
     * 获取部门指定日期范围的排班
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "获取部门排班", description = "查询部门指定日期范围的排班信息")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:query")
    @RequireResource(code = "attendance:schedule", action = "READ", scope = "AREA")
    public ResponseDTO<List<AttendanceScheduleEntity>> getDepartmentSchedule(
            @PathVariable Long departmentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.debug("查询部门排班: 部门ID={}, 开始日期={}, 结束日期={}", departmentId, startDate, endDate);

        try {
            // 解析日期参数
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 调用现有Service层查询逻辑
            List<AttendanceScheduleEntity> schedules = attendanceScheduleService.getDepartmentSchedule(departmentId, start, end);
            log.debug("查询部门排班成功: 部门ID={}, 排班数量={}", departmentId, schedules.size());
            return ResponseDTO.ok(schedules);
        } catch (Exception e) {
            log.error("查询部门排班异常: 部门ID" + departmentId + ", 开始日期" + startDate + ", 结束日期" + endDate, e);
            return ResponseDTO.error("查询部门排班异常：" + e.getMessage());
        }
    }

    /**
     * 检查指定日期是否有排班
     */
    @GetMapping("/check/{employeeId}")
    @Operation(summary = "检查排班状态", description = "检查员工指定日期是否有排班")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:query")
    @RequireResource(code = "attendance:schedule", action = "READ", scope = "AREA")
    public ResponseDTO<Boolean> checkSchedule(
            @PathVariable Long employeeId,
            @RequestParam String date) {

        log.debug("检查排班状态: 员工ID={}, 日期={}", employeeId, date);

        try {
            // 解析日期参数
            LocalDate checkDate = LocalDate.parse(date);

            // 调用现有Service层检查逻辑
            boolean hasSchedule = attendanceScheduleService.hasSchedule(employeeId, checkDate);
            log.debug("检查排班状态成功: 员工ID={}, 日期={}, 有排班={}", employeeId, date, hasSchedule);
            return ResponseDTO.ok(hasSchedule);
        } catch (Exception e) {
            log.error("检查排班状态异常: 员工ID" + employeeId + ", 日期" + date, e);
            return ResponseDTO.error("检查排班状态异常：" + e.getMessage());
        }
    }

    /**
     * 获取员工指定日期的排班
     */
    @GetMapping("/employee/{employeeId}/date/{date}")
    @Operation(summary = "获取员工单日排班", description = "查询员工指定日期的排班信息")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:query")
    @RequireResource(code = "attendance:schedule", action = "READ", scope = "AREA")
    public ResponseDTO<AttendanceScheduleEntity> getEmployeeScheduleByDate(
            @PathVariable Long employeeId,
            @PathVariable String date) {

        log.debug("查询员工单日排班: 员工ID={}, 日期={}", employeeId, date);

        try {
            // 解析日期参数
            LocalDate scheduleDate = LocalDate.parse(date);

            // 调用现有Service层查询逻辑
            AttendanceScheduleEntity schedule = attendanceScheduleService.getEmployeeScheduleByDate(employeeId, scheduleDate);
            if (schedule != null) {
                log.debug("查询员工单日排班成功: 员工ID={}, 日期={}", employeeId, date);
                return ResponseDTO.ok(schedule);
            } else {
                log.warn("员工单日排班不存在: 员工ID={}, 日期={}", employeeId, date);
                return ResponseDTO.error("员工单日排班不存在");
            }
        } catch (Exception e) {
            log.error("查询员工单日排班异常: 员工ID" + employeeId + ", 日期" + date, e);
            return ResponseDTO.error("查询员工单日排班异常：" + e.getMessage());
        }
    }

    /**
     * 批量创建排班记录
     */
    @PostMapping("/batch")
    @Operation(summary = "批量创建排班", description = "批量创建员工排班记录")
    @SaCheckLogin
    @SaCheckPermission("attendance:schedule:batch-create")
    @RequireResource(code = "attendance:schedule", action = "WRITE", scope = "AREA")
    public ResponseDTO<Integer> batchCreateSchedules(@RequestBody List<AttendanceScheduleEntity> schedules) {
        log.info("批量创建排班请求: 排班数量={}", schedules.size());

        try {
            int successCount = 0;
            for (AttendanceScheduleEntity schedule : schedules) {
                // 调用现有Service层保存逻辑
                if (attendanceScheduleService.saveOrUpdateSchedule(schedule)) {
                    successCount++;
                }
            }

            log.info("批量创建排班完成: 成功数量={}, 总数量={}", successCount, schedules.size());
            return ResponseDTO.ok(successCount);
        } catch (Exception e) {
            log.error("批量创建排班异常: 排班数量" + schedules.size(), e);
            return ResponseDTO.error("批量创建排班异常：" + e.getMessage());
        }
    }
}
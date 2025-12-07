package net.lab1024.sa.attendance.controller;

import lombok.extern.slf4j.Slf4j;

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
import net.lab1024.sa.attendance.domain.entity.ShiftsEntity;
import net.lab1024.sa.attendance.domain.query.ShiftsQuery;
import net.lab1024.sa.common.annotation.RequireResource;
import net.lab1024.sa.common.domain.ResponseDTO;
// TEMP: RAC module not yet available
import net.lab1024.sa.attendance.service.ShiftsService;

import java.util.List;

/**
 * 班次管理Controller
 *
 * <p>
 * 基于现有ShiftsService，提供完整的班次管理API
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 100%复用现有Service层业务逻辑，不重复开发
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance/shifts")
@Tag(name = "班次管理", description = "班次配置、管理相关接口")
@Validated
public class ShiftsController {

    @Resource
    private ShiftsService shiftsService;

    /**
     * 创建班次
     */
    @PostMapping("")
    @Operation(summary = "创建班次", description = "创建新的班次配置")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:create")
    @RequireResource(resource = "attendance:shifts", action = "WRITE", message = "您没有权限创建班次")
    public ResponseDTO<Long> createShifts(@Valid @RequestBody ShiftsEntity shifts) {
        log.info("创建班次请求: 班次编码={}, 班次名称={}", shifts.getShiftCode(), shifts.getShiftName());

        try {
            // 调用现有Service层保存逻辑
            boolean success = shiftsService.saveOrUpdateShifts(shifts);
            if (success) {
                log.info("班次创建成功: 班次ID={}, 班次编码={}", shifts.getShiftId(), shifts.getShiftCode());
                return ResponseDTO.ok(shifts.getShiftId());
            } else {
                log.warn("班次创建失败: 班次编码={}", shifts.getShiftCode());
                return ResponseDTO.error("创建班次失败");
            }
        } catch (Exception e) {
            log.error("创建班次异常: 班次编码" + shifts.getShiftCode(), e);
            return ResponseDTO.error("创建班次异常：" + e.getMessage());
        }
    }

    /**
     * 更新班次
     */
    @PutMapping("/{shiftId}")
    @Operation(summary = "更新班次", description = "更新指定的班次配置")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:update")
    @RequireResource(resource = "attendance:shifts", action = "WRITE", message = "您没有权限更新班次")
    public ResponseDTO<Boolean> updateShifts(@PathVariable Long shiftId, @Valid @RequestBody ShiftsEntity shifts) {
        log.info("更新班次请求: 班次ID={}, 班次编码={}", shiftId, shifts.getShiftCode());

        try {
            // 设置班次ID
            shifts.setShiftId(shiftId);

            // 调用现有Service层更新逻辑
            boolean success = shiftsService.saveOrUpdateShifts(shifts);
            if (success) {
                log.info("班次更新成功: 班次ID={}, 班次编码={}", shiftId, shifts.getShiftCode());
                return ResponseDTO.ok(true);
            } else {
                log.warn("班次更新失败: 班次ID={}", shiftId);
                return ResponseDTO.error("更新班次失败");
            }
        } catch (Exception e) {
            log.error("更新班次异常: 班次ID" + shiftId, e);
            return ResponseDTO.error("更新班次异常：" + e.getMessage());
        }
    }

    /**
     * 删除班次
     */
    @DeleteMapping("/{shiftId}")
    @Operation(summary = "删除班次", description = "删除指定的班次")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:delete")
    @RequireResource(resource = "attendance:shifts", action = "DELETE", message = "您没有权限删除班次")
    public ResponseDTO<Boolean> deleteShifts(@PathVariable Long shiftId) {
        log.info("删除班次请求: 班次ID={}", shiftId);

        try {
            // 调用现有Service层删除逻辑
            boolean success = shiftsService.deleteShifts(shiftId);
            if (success) {
                log.info("班次删除成功: 班次ID={}", shiftId);
                return ResponseDTO.ok(true);
            } else {
                log.warn("班次删除失败: 班次ID={}", shiftId);
                return ResponseDTO.error("删除班次失败");
            }
        } catch (Exception e) {
            log.error("删除班次异常: 班次ID" + shiftId, e);
            return ResponseDTO.error("删除班次异常：" + e.getMessage());
        }
    }

    /**
     * 批量删除班次
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除班次", description = "批量删除指定的班次")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:batch-delete")
    @RequireResource(resource = "attendance:shifts", action = "DELETE", message = "您没有权限批量删除班次")
    public ResponseDTO<Integer> batchDeleteShifts(@RequestBody List<Long> shiftIds) {
        log.info("批量删除班次请求: 班次数量={}", shiftIds.size());

        try {
            // 调用现有Service层批量删除逻辑
            int deletedCount = shiftsService.batchDeleteShifts(shiftIds);
            log.info("批量删除班次成功: 删除数量={}", deletedCount);
            return ResponseDTO.ok(deletedCount);
        } catch (Exception e) {
            log.error("批量删除班次异常: 班次数量" + shiftIds.size(), e);
            return ResponseDTO.error("批量删除班次异常：" + e.getMessage());
        }
    }

    /**
     * 获取班次详情
     */
    @GetMapping("/{shiftId}")
    @Operation(summary = "获取班次详情", description = "查询指定班次的详细信息")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:query")
    @RequireResource(resource = "attendance:shifts", action = "READ", message = "您没有权限查询班次")
    public ResponseDTO<ShiftsEntity> getShiftsById(@PathVariable Long shiftId) {
        log.debug("查询班次详情: 班次ID={}", shiftId);

        try {
            // 调用现有Service层查询逻辑
            ShiftsEntity shifts = shiftsService.getById(shiftId);
            if (shifts != null && shifts.getDeletedFlag() == 0) {
                log.debug("查询班次详情成功: 班次ID={}, 班次编码={}", shiftId, shifts.getShiftCode());
                return ResponseDTO.ok(shifts);
            } else {
                log.warn("班次不存在: 班次ID={}", shiftId);
                return ResponseDTO.error("班次不存在");
            }
        } catch (Exception e) {
            log.error("查询班次详情异常: 班次ID" + shiftId, e);
            return ResponseDTO.error("查询班次详情异常：" + e.getMessage());
        }
    }

    /**
     * 查询班次列表
     */
    @GetMapping("")
    @Operation(summary = "查询班次列表", description = "根据查询条件查询班次列表")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:list")
    @RequireResource(resource = "attendance:shifts", action = "READ", message = "您没有权限查询班次")
    public ResponseDTO<List<ShiftsEntity>> queryShiftsList(ShiftsQuery query) {
        log.debug("查询班次列表: 查询条件={}", query);

        try {
            // 调用现有Service层查询逻辑
            List<ShiftsEntity> shiftsList = shiftsService.queryByPage(query);
            log.debug("查询班次列表成功: 结果数量={}", shiftsList.size());
            return ResponseDTO.ok(shiftsList);
        } catch (Exception e) {
            log.error("查询班次列表异常: 查询条件" + query, e);
            return ResponseDTO.error("查询班次列表异常：" + e.getMessage());
        }
    }

    /**
     * 根据班次编码查询班次
     */
    @GetMapping("/code/{shiftCode}")
    @Operation(summary = "根据编码查询班次", description = "根据班次编码查询班次信息")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:query")
    @RequireResource(resource = "attendance:shifts", action = "READ", message = "您没有权限查询班次")
    public ResponseDTO<ShiftsEntity> getShiftsByCode(@PathVariable String shiftCode) {
        log.debug("根据编码查询班次: 班次编码={}", shiftCode);

        try {
            // 调用现有Service层查询逻辑
            ShiftsEntity shifts = shiftsService.getByShiftCode(shiftCode);
            if (shifts != null) {
                log.debug("根据编码查询班次成功: 班次编码={}, 班次名称={}", shiftCode, shifts.getShiftName());
                return ResponseDTO.ok(shifts);
            } else {
                log.warn("班次编码不存在: 班次编码={}", shiftCode);
                return ResponseDTO.error("班次编码不存在");
            }
        } catch (Exception e) {
            log.error("根据编码查询班次异常: 班次编码" + shiftCode, e);
            return ResponseDTO.error("根据编码查询班次异常：" + e.getMessage());
        }
    }

    /**
     * 根据班次类型查询班次列表
     */
    @GetMapping("/type/{shiftType}")
    @Operation(summary = "根据类型查询班次", description = "根据班次类型查询班次列表")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:query")
    @RequireResource(resource = "attendance:shifts", action = "READ", message = "您没有权限查询班次")
    public ResponseDTO<List<ShiftsEntity>> getShiftsByType(@PathVariable String shiftType) {
        log.debug("根据类型查询班次: 班次类型={}", shiftType);

        try {
            // 调用现有Service层查询逻辑
            List<ShiftsEntity> shiftsList = shiftsService.getByShiftType(shiftType);
            log.debug("根据类型查询班次成功: 班次类型={}, 结果数量={}", shiftType, shiftsList.size());
            return ResponseDTO.ok(shiftsList);
        } catch (Exception e) {
            log.error("根据类型查询班次异常: 班次类型" + shiftType, e);
            return ResponseDTO.error("根据类型查询班次异常：" + e.getMessage());
        }
    }

    /**
     * 获取所有启用的班次
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取启用的班次", description = "查询所有启用的班次列表")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:list")
    @RequireResource(resource = "attendance:shifts", action = "READ", message = "您没有权限查询班次")
    public ResponseDTO<List<ShiftsEntity>> getAllEnabledShifts() {
        log.debug("查询所有启用的班次");

        try {
            // 调用现有Service层查询逻辑
            List<ShiftsEntity> enabledShifts = shiftsService.getAllEnabledShifts();
            log.debug("查询所有启用的班次成功: 结果数量={}", enabledShifts.size());
            return ResponseDTO.ok(enabledShifts);
        } catch (Exception e) {
            log.error("查询所有启用的班次异常", e);
            return ResponseDTO.error("查询启用的班次异常：" + e.getMessage());
        }
    }

    /**
     * 启用或禁用班次
     */
    @PutMapping("/{shiftId}/status")
    @Operation(summary = "更新班次状态", description = "启用或禁用指定的班次")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:update")
    @RequireResource(resource = "attendance:shifts", action = "WRITE", message = "您没有权限更新班次状态")
    public ResponseDTO<Boolean> updateShiftsStatus(
            @PathVariable Long shiftId,
            @RequestParam Boolean status) {

        log.info("更新班次状态请求: 班次ID={}, 状态={}", shiftId, status ? "启用" : "禁用");

        try {
            // 调用现有Service层更新逻辑
            boolean success = shiftsService.updateShiftsStatus(shiftId, status);
            if (success) {
                log.info("班次状态更新成功: 班次ID={}, 状态={}", shiftId, status ? "启用" : "禁用");
                return ResponseDTO.ok(true);
            } else {
                log.warn("班次状态更新失败: 班次ID={}", shiftId);
                return ResponseDTO.error("更新班次状态失败");
            }
        } catch (Exception e) {
            log.error("更新班次状态异常: 班次ID" + shiftId + ", 状态" + status, e);
            return ResponseDTO.error("更新班次状态异常：" + e.getMessage());
        }
    }

    /**
     * 批量更新班次状态
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新班次状态", description = "批量启用或禁用指定的班次")
    @SaCheckLogin
    @SaCheckPermission("attendance:shifts:batch-update")
    @RequireResource(resource = "attendance:shifts", action = "WRITE", message = "您没有权限批量更新班次状态")
    public ResponseDTO<Integer> batchUpdateShiftsStatus(
            @RequestBody List<Long> shiftIds,
            @RequestParam Boolean status) {

        log.info("批量更新班次状态请求: 班次数量={}, 状态={}", shiftIds.size(), status ? "启用" : "禁用");

        try {
            // 调用现有Service层批量更新逻辑
            int updatedCount = shiftsService.batchUpdateShiftsStatus(shiftIds, status);
            log.info("批量更新班次状态成功: 更新数量={}, 状态={}", updatedCount, status ? "启用" : "禁用");
            return ResponseDTO.ok(updatedCount);
        } catch (Exception e) {
            log.error("批量更新班次状态异常: 班次数量" + shiftIds.size() + ", 状态" + status, e);
            return ResponseDTO.error("批量更新班次状态异常：" + e.getMessage());
        }
    }
}

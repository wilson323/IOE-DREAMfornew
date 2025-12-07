package net.lab1024.sa.access.advanced.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.advanced.domain.dto.InterlockTriggerDTO;
import net.lab1024.sa.access.advanced.domain.dto.InterlockRuleQueryDTO;
import net.lab1024.sa.access.advanced.domain.dto.InterlockRuleCreateDTO;
import net.lab1024.sa.access.advanced.domain.dto.InterlockRuleUpdateDTO;
import net.lab1024.sa.access.advanced.domain.vo.InterlockRuleVO;
import net.lab1024.sa.access.advanced.service.InterlockRuleService;
import net.lab1024.sa.access.advanced.service.InterlockLogService;
import net.lab1024.sa.access.advanced.engine.GlobalInterlockEngine;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 全局互锁控制器
 * 提供设备间互锁管理和控制功能
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/interlock")
@Tag(name = "全局互锁管理", description = "设备间互锁控制和管理")
@Slf4j
public class GlobalInterlockController {

    @Resource
    private InterlockRuleService interlockRuleService;

    @Resource
    private InterlockLogService interlockLogService;

    @Resource
    private GlobalInterlockEngine interlockEngine;

    /**
     * 申请设备互锁
     *
     * @param deviceId 设备ID
     * @param lockType 锁类型
     * @param lockedBy 锁定者
     * @param reason 锁定原因
     * @param priority 优先级
     * @param timeoutMs 超时时间
     * @return 锁定结果
     */
    @PostMapping("/devices/{deviceId}/lock")
    @Operation(summary = "申请设备互锁", description = "为指定设备申请互锁")
    @SaCheckPermission("access:interlock:lock")
    public ResponseDTO<Boolean> lockDevice(
            @PathVariable String deviceId,
            @RequestParam String lockType,
            @RequestParam String lockedBy,
            @RequestParam String reason,
            @RequestParam(defaultValue = "5") int priority,
            @RequestParam(defaultValue = "0") long timeoutMs) {
        log.info("[GlobalInterlockController] 申请设备互锁: deviceId={}, lockType={}, lockedBy={}",
                deviceId, lockType, lockedBy);

        try {
            return interlockEngine.requestDeviceLock(deviceId, lockType, lockedBy, reason, priority, timeoutMs).get();

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 申请设备互锁失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("申请设备互锁失败: " + e.getMessage());
        }
    }

    /**
     * 释放设备互锁
     *
     * @param deviceId 设备ID
     * @param lockedBy 锁定者
     * @return 释放结果
     */
    @PostMapping("/devices/{deviceId}/unlock")
    @Operation(summary = "释放设备互锁", description = "释放指定设备的互锁")
    @SaCheckPermission("access:interlock:unlock")
    public ResponseDTO<Boolean> unlockDevice(
            @PathVariable String deviceId,
            @RequestParam String lockedBy) {
        log.info("[GlobalInterlockController] 释放设备互锁: deviceId={}, lockedBy={}", deviceId, lockedBy);

        try {
            return interlockEngine.releaseDeviceLock(deviceId, lockedBy).get();

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 释放设备互锁失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("释放设备互锁失败: " + e.getMessage());
        }
    }

    /**
     * 强制释放设备互锁
     *
     * @param deviceId 设备ID
     * @param reason 强制释放原因
     * @param operator 操作者
     * @return 释放结果
     */
    @PostMapping("/devices/{deviceId}/force-unlock")
    @Operation(summary = "强制释放设备互锁", description = "强制释放指定设备的互锁")
    @SaCheckPermission("access:interlock:force-unlock")
    public ResponseDTO<Boolean> forceUnlockDevice(
            @PathVariable String deviceId,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.warn("[GlobalInterlockController] 强制释放设备互锁: deviceId={}, operator={}", deviceId, operator);

        try {
            return interlockEngine.forceReleaseDeviceLock(deviceId, reason, operator).get();

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 强制释放设备互锁失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("强制释放设备互锁失败: " + e.getMessage());
        }
    }

    /**
     * 检查设备互锁状态
     *
     * @param deviceId 设备ID
     * @return 锁定状态信息
     */
    @GetMapping("/devices/{deviceId}/status")
    @Operation(summary = "检查设备互锁状态", description = "获取指定设备的互锁状态")
    @SaCheckPermission("access:interlock:status")
    public ResponseDTO<Map<String, Object>> getDeviceLockStatus(@PathVariable String deviceId) {
        log.debug("[GlobalInterlockController] 检查设备互锁状态: deviceId={}", deviceId);

        try {
            Map<String, Object> status = interlockEngine.checkDeviceLockStatus(deviceId);
            return ResponseDTO.userOk(status);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 检查设备互锁状态失败: deviceId={}, error={}",
                    deviceId, e.getMessage(), e);
            return ResponseDTO.error("检查设备互锁状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量检查设备互锁状态
     *
     * @param deviceIds 设备ID列表
     * @return 锁定状态信息列表
     */
    @PostMapping("/devices/batch-status")
    @Operation(summary = "批量检查设备互锁状态", description = "批量获取设备的互锁状态")
    @SaCheckPermission("access:interlock:batch-status")
    public ResponseDTO<List<Map<String, Object>>> batchCheckDeviceLockStatus(@RequestBody List<String> deviceIds) {
        log.debug("[GlobalInterlockController] 批量检查设备互锁状态: deviceCount={}", deviceIds.size());

        try {
            List<Map<String, Object>> statusList = interlockEngine.batchCheckDeviceLockStatus(deviceIds);
            return ResponseDTO.userOk(statusList);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 批量检查设备互锁状态失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("批量检查设备互锁状态失败: " + e.getMessage());
        }
    }

    /**
     * 触发互锁事件
     *
     * @param triggerDTO 互锁触发信息
     * @return 处理结果
     */
    @PostMapping("/trigger")
    @Operation(summary = "触发互锁事件", description = "根据指定条件触发互锁处理")
    @SaCheckPermission("access:interlock:trigger")
    public ResponseDTO<String> triggerInterlock(@Valid @RequestBody InterlockTriggerDTO triggerDTO) {
        log.info("[GlobalInterlockController] 触发互锁事件: triggerType={}, deviceId={}",
                triggerDTO.getTriggerType(), triggerDTO.getDeviceId());

        try {
            return interlockEngine.processInterlockTrigger(triggerDTO).get();

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 触发互锁事件失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("触发互锁事件失败: " + e.getMessage());
        }
    }

    /**
     * 获取互锁规则列表
     *
     * @param queryDTO 查询条件
     * @return 规则列表
     */
    @GetMapping("/rules")
    @Operation(summary = "获取互锁规则列表", description = "根据条件查询互锁规则")
    @SaCheckPermission("access:interlock:rules:list")
    public ResponseDTO<List<InterlockRuleVO>> getInterlockRules(InterlockRuleQueryDTO queryDTO) {
        log.debug("[GlobalInterlockController] 获取互锁规则列表: {}", queryDTO);

        try {
            List<InterlockRuleVO> rules = interlockRuleService.getRules(queryDTO);
            return ResponseDTO.userOk(rules);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 获取互锁规则列表失败", e);
            return ResponseDTO.error("获取互锁规则列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建互锁规则
     *
     * @param createDTO 创建表单
     * @return 创建结果
     */
    @PostMapping("/rules")
    @Operation(summary = "创建互锁规则", description = "创建新的设备互锁规则")
    @SaCheckPermission("access:interlock:rules:create")
    public ResponseDTO<String> createInterlockRule(@Valid @RequestBody InterlockRuleCreateDTO createDTO) {
        log.info("[GlobalInterlockController] 创建互锁规则: ruleName={}", createDTO.getRuleName());

        try {
            String ruleId = interlockRuleService.createRule(createDTO);
            return ResponseDTO.userOk(String.format("互锁规则创建成功，ID: %s", ruleId));

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 创建互锁规则失败", e);
            return ResponseDTO.error("创建互锁规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新互锁规则
     *
     * @param ruleId 规则ID
     * @param updateDTO 更新表单
     * @return 更新结果
     */
    @PutMapping("/rules/{ruleId}")
    @Operation(summary = "更新互锁规则", description = "更新指定的互锁规则")
    @SaCheckPermission("access:interlock:rules:update")
    public ResponseDTO<String> updateInterlockRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody InterlockRuleUpdateDTO updateDTO) {
        log.info("[GlobalInterlockController] 更新互锁规则: ruleId={}", ruleId);

        try {
            interlockRuleService.updateRule(ruleId, updateDTO);
            return ResponseDTO.userOk("互锁规则更新成功");

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 更新互锁规则失败", e);
            return ResponseDTO.error("更新互锁规则失败: " + e.getMessage());
        }
    }

    /**
     * 删除互锁规则
     *
     * @param ruleId 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/rules/{ruleId}")
    @Operation(summary = "删除互锁规则", description = "删除指定的互锁规则")
    @SaCheckPermission("access:interlock:rules:delete")
    public ResponseDTO<String> deleteInterlockRule(@PathVariable Long ruleId) {
        log.info("[GlobalInterlockController] 删除互锁规则: ruleId={}", ruleId);

        try {
            interlockRuleService.deleteRule(ruleId);
            return ResponseDTO.userOk("互锁规则删除成功");

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 删除互锁规则失败", e);
            return ResponseDTO.error("删除互锁规则失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用互锁规则
     *
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PutMapping("/rules/{ruleId}/status")
    @Operation(summary = "启用/禁用互锁规则", description = "切换互锁规则的启用状态")
    @SaCheckPermission("access:interlock:rules:status")
    public ResponseDTO<String> toggleRuleStatus(
            @PathVariable Long ruleId,
            @RequestParam boolean enabled) {
        log.info("[GlobalInterlockController] 切换互锁规则状态: ruleId={}, enabled={}", ruleId, enabled);

        try {
            interlockRuleService.toggleRuleStatus(ruleId, enabled);
            return ResponseDTO.userOk(String.format("互锁规则已%s", enabled ? "启用" : "禁用"));

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 切换互锁规则状态失败", e);
            return ResponseDTO.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 测试互锁规则
     *
     * @param ruleId 规则ID
     * @param testDeviceId 测试设备ID
     * @param testLockType 测试锁类型
     * @param testLockedBy 测试锁定者
     * @return 测试结果
     */
    @PostMapping("/rules/{ruleId}/test")
    @Operation(summary = "测试互锁规则", description = "测试指定互锁规则的执行效果")
    @SaCheckPermission("access:interlock:rules:test")
    public ResponseDTO<Map<String, Object>> testInterlockRule(
            @PathVariable Long ruleId,
            @RequestParam String testDeviceId,
            @RequestParam String testLockType,
            @RequestParam String testLockedBy) {
        log.info("[GlobalInterlockController] 测试互锁规则: ruleId={}, testDeviceId={}", ruleId, testDeviceId);

        try {
            Map<String, Object> testResult = interlockRuleService.testRule(ruleId, testDeviceId, testLockType, testLockedBy);
            return ResponseDTO.userOk(testResult);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 测试互锁规则失败", e);
            return ResponseDTO.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取互锁统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取互锁统计信息", description = "获取互锁系统的运行统计数据")
    @SaCheckPermission("access:interlock:statistics")
    public ResponseDTO<Map<String, Object>> getInterlockStatistics() {
        log.debug("[GlobalInterlockController] 获取互锁统计信息");

        try {
            // 获取引擎统计
            Map<String, Object> engineStats = interlockEngine.getInterlockStatistics();

            // 获取规则统计
            Map<String, Object> ruleStats = interlockRuleService.getRuleStatistics();

            // 获取日志统计
            Map<String, Object> logStats = interlockLogService.getExecutionStatistics();

            Map<String, Object> allStats = Map.of(
                "engine", engineStats,
                "rules", ruleStats,
                "logs", logStats,
                "timestamp", System.currentTimeMillis()
            );

            return ResponseDTO.userOk(allStats);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 获取互锁统计信息失败", e);
            return ResponseDTO.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期互锁
     *
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期互锁", description = "清理过期的互锁状态")
    @SaCheckPermission("access:interlock:cleanup")
    public ResponseDTO<Map<String, Object>> cleanupExpiredLocks() {
        log.info("[GlobalInterlockController] 清理过期互锁");

        try {
            Map<String, Object> cleanupResult = interlockEngine.cleanupExpiredLocks();
            return ResponseDTO.userOk(cleanupResult);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 清理过期互锁失败", e);
            return ResponseDTO.error("清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取区域互锁状态
     *
     * @param areaId 区域ID
     * @return 区域互锁状态
     */
    @GetMapping("/areas/{areaId}/status")
    @Operation(summary = "获取区域互锁状态", description = "获取指定区域的互锁状态信息")
    @SaCheckPermission("access:interlock:area-status")
    public ResponseDTO<Map<String, Object>> getAreaInterlockStatus(@PathVariable Long areaId) {
        log.debug("[GlobalInterlockController] 获取区域互锁状态: areaId={}", areaId);

        try {
            Map<String, Object> status = new HashMap<>();
            status.put("areaId", areaId);

            // 这里需要从引擎获取区域锁状态
            // 由于当前实现中areaLocks是私有的，需要在引擎中添加相应方法
            // status.put("lockedDevices", engine.getAreaLockedDevices(areaId));
            // status.put("totalDevices", engine.getTotalDevicesInArea(areaId));
            // status.put("lockRatio", engine.getAreaLockRatio(areaId));

            return ResponseDTO.userOk(status);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 获取区域互锁状态失败: areaId={}, error={}",
                    areaId, e.getMessage(), e);
            return ResponseDTO.error("获取区域互锁状态失败: " + e.getMessage());
        }
    }

    /**
     * 设置区域互锁模式
     *
     * @param areaId 区域ID
     * @param mode 互锁模式
     * @param reason 设置原因
     * @param operator 操作者
     * @return 设置结果
     */
    @PostMapping("/areas/{areaId}/interlock-mode")
    @Operation(summary = "设置区域互锁模式", description = "设置指定区域的互锁模式")
    @SaCheckPermission("access:interlock:area-mode")
    public ResponseDTO<String> setAreaInterlockMode(
            @PathVariable Long areaId,
            @RequestParam String mode,
            @RequestParam String reason,
            @RequestParam String operator) {
        log.info("[GlobalInterlockController] 设置区域互锁模式: areaId={}, mode={}", areaId, mode);

        try {
            // 创建互锁触发事件
            InterlockTriggerDTO triggerDTO = new InterlockTriggerDTO();
            triggerDTO.setTriggerId(UUID.randomUUID().toString());
            triggerDTO.setTriggerType("AREA_INTERLOCK");
            triggerDTO.setAffectedAreaId(areaId);
            triggerDTO.getTriggerData().put("mode", mode);
            triggerDTO.getTriggerData().put("reason", reason);
            triggerDTO.getTriggerData().put("operator", operator);

            return interlockEngine.processInterlockTrigger(triggerDTO).get();

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 设置区域互锁模式失败: areaId={}, error={}",
                    areaId, e.getMessage(), e);
            return ResponseDTO.error("设置区域互锁模式失败: " + e.getMessage());
        }
    }

    /**
     * 获取互锁执行记录
     *
     * @param deviceId 设备ID（可选）
     * @param status 执行状态（可选）
     * @param pageSize 页面大小
     * @return 执行记录列表
     */
    @GetMapping("/executions")
    @Operation(summary = "获取互锁执行记录", description = "查询互锁规则的执行历史")
    @SaCheckPermission("access:interlock:executions:list")
    public ResponseDTO<List<Map<String, Object>>> getInterlockExecutions(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        log.debug("[GlobalInterlockController] 获取互锁执行记录: deviceId={}, status={}", deviceId, status);

        try {
            // 调用日志服务获取执行记录
            List<Map<String, Object>> executions = interlockLogService.getExecutions(
                deviceId != null ? Long.valueOf(deviceId) : null,
                status,
                pageSize
            );

            return ResponseDTO.userOk(executions);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 获取互锁执行记录失败", e);
            return ResponseDTO.error("获取执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 导出互锁规则
     *
     * @param ruleIds 规则ID列表（可选）
     * @return 导出结果
     */
    @GetMapping("/rules/export")
    @Operation(summary = "导出互锁规则", description = "导出指定的互锁规则配置")
    @SaCheckPermission("access:interlock:rules:export")
    public ResponseDTO<Map<String, Object>> exportInterlockRules(
            @RequestParam(required = false) List<Long> ruleIds) {
        log.info("[GlobalInterlockController] 导出互锁规则: ruleCount={}", ruleIds != null ? ruleIds.size() : "all");

        try {
            Map<String, Object> exportData = interlockRuleService.exportRules(ruleIds);
            return ResponseDTO.userOk(exportData);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 导出互锁规则失败", e);
            return ResponseDTO.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导入互锁规则
     *
     * @param importData 导入数据
     * @return 导入结果
     */
    @PostMapping("/rules/import")
    @Operation(summary = "导入互锁规则", description = "导入互锁规则配置")
    @SaCheckPermission("access:interlock:rules:import")
    public ResponseDTO<Map<String, Object>> importInterlockRules(@RequestBody Map<String, Object> importData) {
        log.info("[GlobalInterlockController] 导入互锁规则");

        try {
            Map<String, Object> importResult = interlockRuleService.importRules(importData);
            return ResponseDTO.userOk(importResult);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 导入互锁规则失败", e);
            return ResponseDTO.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取互锁模板
     *
     * @param templateType 模板类型
     * @return 模板数据
     */
    @GetMapping("/templates/{templateType}")
    @Operation(summary = "获取互锁模板", description = "获取指定类型的互锁规则模板")
    @SaCheckPermission("access:interlock:templates")
    public ResponseDTO<Map<String, Object>> getInterlockTemplate(@PathVariable String templateType) {
        log.debug("[GlobalInterlockController] 获取互锁模板: templateType={}", templateType);

        try {
            Map<String, Object> template = interlockRuleService.getTemplate(templateType);
            return ResponseDTO.userOk(template);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 获取互锁模板失败", e);
            return ResponseDTO.error("获取模板失败: " + e.getMessage());
        }
    }

    /**
     * 验证互锁规则配置
     *
     * @param ruleData 规则配置数据
     * @return 验证结果
     */
    @PostMapping("/rules/validate")
    @Operation(summary = "验证互锁规则配置", description = "验证互锁规则配置的正确性")
    @SaCheckPermission("access:interlock:rules:validate")
    public ResponseDTO<Map<String, Object>> validateInterlockRuleConfiguration(@RequestBody Map<String, Object> ruleData) {
        log.debug("[GlobalInterlockController] 验证互锁规则配置");

        try {
            Map<String, Object> validationResult = interlockRuleService.validateRuleConfiguration(ruleData);
            return ResponseDTO.userOk(validationResult);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 验证互锁规则配置失败", e);
            return ResponseDTO.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取互锁健康状态
     *
     * @return 健康状态信息
     */
    @GetMapping("/health")
    @Operation(summary = "获取互锁健康状态", description = "获取互锁系统的健康状态")
    @SaCheckPermission("access:interlock:health")
    public ResponseDTO<Map<String, Object>> getInterlockHealth() {
        log.debug("[GlobalInterlockController] 获取互锁健康状态");

        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());

            // 获取基本统计
            Map<String, Object> stats = interlockEngine.getInterlockStats();
            health.putAll(stats);

            // 检查系统健康状态
            boolean isHealthy = checkInterlockHealth(stats);
            health.put("healthy", isHealthy);

            // 添加健康检查详情
            List<String> healthChecks = new ArrayList<>();
            if (stats.containsKey("activeLockCount")) {
                int activeLockCount = ((Number) stats.get("activeLockCount")).intValue();
                if (activeLockCount > 1000) {
                    healthChecks.add("活跃锁数量过多: " + activeLockCount);
                }
            }

            if (stats.containsKey("expiredLocks")) {
                int expiredLocks = ((Number) stats.get("expiredLocks")).intValue();
                if (expiredLocks > 50) {
                    healthChecks.add("过期锁数量过多: " + expiredLocks);
                }
            }

            health.put("healthChecks", healthChecks);

            return ResponseDTO.userOk(health);

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 获取互锁健康状态失败", e);
            Map<String, Object> errorHealth = Map.of(
                "status", "ERROR",
                "timestamp", System.currentTimeMillis(),
                "healthy", false,
                "error", e.getMessage()
            );
            return ResponseDTO.userOk(errorHealth);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查互锁系统健康状态
     */
    private boolean checkInterlockHealth(Map<String, Object> stats) {
        try {
            // 检查基本健康指标
            long totalRequests = stats.containsKey("totalLockRequests") ?
                    ((Number) stats.get("totalLockRequests")).longValue() : 0;
            int activeLocks = stats.containsKey("activeLockCount") ?
                    ((Number) stats.get("activeLockCount")).intValue() : 0;
            int deviceLocks = stats.containsKey("deviceLockCount") ?
                    ((Number) stats.get("deviceLockCount")).intValue() : 0;

            // 基本健康检查
            if (totalRequests > 0 && activeLocks > deviceLocks) {
                return false; // 活跃锁数不应该超过设备锁数
            }

            // 检查过期锁比例
            if (stats.containsKey("expiredLocks") && deviceLocks > 0) {
                int expiredLocks = ((Number) stats.get("expiredLocks")).intValue();
                double expiredRatio = (double) expiredLocks / deviceLocks;
                if (expiredRatio > 0.3) {
                    return false; // 过期锁不应该超过30%
                }
            }

            return true;

        } catch (Exception e) {
            log.error("[GlobalInterlockController] 检查互锁健康状态失败", e);
            return false;
        }
    }
}
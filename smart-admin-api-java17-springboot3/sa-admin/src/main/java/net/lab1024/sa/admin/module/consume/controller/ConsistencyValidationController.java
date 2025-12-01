package net.lab1024.sa.admin.module.consume.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.service.consistency.ConsistencyValidator;
import net.lab1024.sa.admin.module.consume.service.consistency.DataConsistencyManager;
import net.lab1024.sa.admin.module.consume.service.consistency.ReconciliationService;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 数据一致性保障验证控制器
 * 用于验证和监控消费模块的数据一致性保障机制
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@RestController
@RequestMapping("/api/consume/consistency")
@Tag(name = "数据一致性保障", description = "消费模块数据一致性保障相关接口")
public class ConsistencyValidationController {

    @Resource
    private ConsistencyValidator consistencyValidator;

    @Resource
    private DataConsistencyManager consistencyManager;

    @Resource
    private ReconciliationService reconciliationService;

    /**
     * 执行完整的一致性保障验证
     */
    @PostMapping("/validate/all")
    @Operation(summary = "执行完整的一致性保障验证", description = "验证分布式锁、版本控制、事务操作、并发安全性等所有机制")
    @SaCheckPermission("consume:consistency:validate")
    public ResponseDTO<Map<String, Object>> validateAllConsistencyMechanisms() {
        try {
            log.info("开始执行完整的数据一致性保障验证...");

            // 执行完整验证
            boolean allPassed = consistencyValidator.validateAllConsistencyMechanisms();

            // 获取系统一致性状态
            DataConsistencyManager.ConsistencyCheckResult systemStatus = consistencyValidator.checkSystemConsistency();

            // 构建响应结果
            Map<String, Object> result = new HashMap<>();
            result.put("allMechanismsValid", allPassed);
            result.put("systemHealthy", systemStatus != null && systemStatus.isHealthy());
            result.put("validationTime", System.currentTimeMillis());

            if (systemStatus != null) {
                result.put("activeLocks", systemStatus.getActiveLocks());
                result.put("versionEntries", systemStatus.getVersionEntries());
                result.put("redisHealthy", systemStatus.isRedisHealthy());
                result.put("errorMessage", systemStatus.getErrorMessage());
            }

            if (allPassed && (systemStatus == null || systemStatus.isHealthy())) {
                return ResponseDTO.ok(result, "数据一致性保障机制验证通过");
            } else {
                return ResponseDTO.ok(result, "数据一致性保障机制验证发现问题，请查看详情");
            }

        } catch (Exception e) {
            log.error("执行数据一致性保障验证异常", e);
            return ResponseDTO.error("验证执行异常: " + e.getMessage());
        }
    }

    /**
     * 验证分布式锁机制
     */
    @PostMapping("/validate/lock")
    @Operation(summary = "验证分布式锁机制", description = "测试分布式锁的获取、释放、互斥性等功能")
    @SaCheckPermission("consume:consistency:validate")
    public ResponseDTO<Boolean> validateDistributedLock() {
        try {
            boolean result = consistencyValidator.validateDistributedLock();
            return ResponseDTO.ok(result, result ? "分布式锁机制验证通过" : "分布式锁机制验证失败");
        } catch (Exception e) {
            log.error("验证分布式锁机制异常", e);
            return ResponseDTO.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证数据版本控制机制
     */
    @PostMapping("/validate/version")
    @Operation(summary = "验证数据版本控制机制", description = "测试数据版本号管理、冲突检测、原子性操作等功能")
    @SaCheckPermission("consume:consistency:validate")
    public ResponseDTO<Boolean> validateVersionControl() {
        try {
            boolean result = consistencyValidator.validateVersionControl();
            return ResponseDTO.ok(result, result ? "数据版本控制机制验证通过" : "数据版本控制机制验证失败");
        } catch (Exception e) {
            log.error("验证数据版本控制机制异常", e);
            return ResponseDTO.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证事务性操作机制
     */
    @PostMapping("/validate/transaction")
    @Operation(summary = "验证事务性操作机制", description = "测试分布式锁+版本控制组合的事务性操作")
    @SaCheckPermission("consume:consistency:validate")
    public ResponseDTO<Boolean> validateTransactionalOperation() {
        try {
            boolean result = consistencyValidator.validateTransactionalOperation();
            return ResponseDTO.ok(result, result ? "事务性操作机制验证通过" : "事务性操作机制验证失败");
        } catch (Exception e) {
            log.error("验证事务性操作机制异常", e);
            return ResponseDTO.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证并发安全性
     */
    @PostMapping("/validate/concurrent")
    @Operation(summary = "验证并发安全性", description = "测试高并发场景下的数据一致性和锁竞争处理")
    @SaCheckPermission("consume:consistency:validate")
    public ResponseDTO<Boolean> validateConcurrentSafety() {
        try {
            boolean result = consistencyValidator.validateConcurrentSafety();
            return ResponseDTO.ok(result, result ? "并发安全性验证通过" : "并发安全性验证失败");
        } catch (Exception e) {
            log.error("验证并发安全性异常", e);
            return ResponseDTO.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 检查系统一致性状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查系统一致性状态", description = "获取当前系统的一致性健康状态")
    @SaCheckPermission("consume:consistency:view")
    public ResponseDTO<DataConsistencyManager.ConsistencyCheckResult> checkSystemConsistency() {
        try {
            DataConsistencyManager.ConsistencyCheckResult result = consistencyValidator.checkSystemConsistency();
            return ResponseDTO.ok(result, "系统一致性状态获取成功");
        } catch (Exception e) {
            log.error("检查系统一致性状态异常", e);
            return ResponseDTO.error("状态检查异常: " + e.getMessage());
        }
    }

    /**
     * 执行日终对账
     */
    @PostMapping("/reconciliation/daily")
    @Operation(summary = "执行日终对账", description = "执行指定日期的日终对账处理")
    @SaCheckPermission("consume:reconciliation:execute")
    public ResponseDTO<Map<String, Object>> executeDailyReconciliation(
            @RequestParam(required = false) String date) {
        try {
            LocalDate reconcileDate = date != null ? LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : LocalDate.now().minusDays(1);

            log.info("开始执行日终对账: {}", reconcileDate);

            // 执行对账 - 使用正确的方法名和内部类
            ReconciliationService.ReconciliationResult result = reconciliationService
                    .performDailyReconciliation(reconcileDate);

            // 构建响应 - 从accountResult中获取数据
            Map<String, Object> response = new HashMap<>();
            response.put("reconcileDate", reconcileDate);
            response.put("success", result.isSuccess());

            // 从accountResult中获取账户相关数据
            if (result.getAccountResult() != null) {
                response.put("totalAccounts", result.getAccountResult().getTotalAccounts());
                response.put("successAccounts",
                        result.getAccountResult().getTotalAccounts() - result.getAccountResult().getDiscrepancyCount());
                response.put("discrepancyCount", result.getAccountResult().getDiscrepancyCount());
            } else {
                response.put("totalAccounts", 0);
                response.put("successAccounts", 0);
                response.put("discrepancyCount", 0);
            }

            response.put("fixCount", result.getTotalDiscrepancyCount());
            response.put("errors", result.getErrorMessage() != null ? java.util.Arrays.asList(result.getErrorMessage())
                    : new java.util.ArrayList<>());

            // 计算执行时间
            long executionTime = 0;
            if (result.getStartTime() != null && result.getEndTime() != null) {
                executionTime = java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis();
            }
            response.put("executionTime", executionTime);

            return ResponseDTO.ok(response, result.isSuccess() ? "日终对账执行成功" : "日终对账发现问题");

        } catch (Exception e) {
            log.error("执行日终对账异常", e);
            return ResponseDTO.error("对账执行异常: " + e.getMessage());
        }
    }

    /**
     * 批量修复数据差异
     */
    @PostMapping("/reconciliation/fix")
    @Operation(summary = "批量修复数据差异", description = "修复对账过程中发现的数据差异")
    @SaCheckPermission("consume:reconciliation:fix")
    public ResponseDTO<Map<String, Object>> batchFixDiscrepancies(
            @RequestBody Map<String, Object> request) {
        try {
            // 这里应该包含具体的差异数据和修复策略
            // 由于安全考虑，实际实现需要更详细的权限验证和审计日志

            log.warn("批量数据差异修复请求已接收，需要进一步实现: {}", request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量修复功能需要进一步实现安全验证和审计日志");
            response.put("requestReceived", request);

            return ResponseDTO.ok(response, "请求已接收，待完善实现");

        } catch (Exception e) {
            log.error("批量修复数据差异异常", e);
            return ResponseDTO.error("修复执行异常: " + e.getMessage());
        }
    }

    /**
     * 清理过期的锁和版本数据
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期的锁和版本数据", description = "清理系统中过期的分布式锁和数据版本信息")
    @SaCheckPermission("consume:consistency:cleanup")
    public ResponseDTO<DataConsistencyManager.ConsistencyCleanupResult> cleanupExpiredData() {
        try {
            log.info("开始清理过期的锁和版本数据...");

            DataConsistencyManager.ConsistencyCleanupResult result = consistencyManager.cleanupExpiredData();

            if (result.isSuccess()) {
                return ResponseDTO.ok(result, "数据清理完成");
            } else {
                return ResponseDTO.ok(result, "数据清理完成，存在问题: " + result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("清理过期数据异常", e);
            return ResponseDTO.error("清理执行异常: " + e.getMessage());
        }
    }

    /**
     * 获取一致性保障监控指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取一致性保障监控指标", description = "获取数据一致性保障相关的监控指标和统计信息")
    @SaCheckPermission("consume:consistency:view")
    public ResponseDTO<Map<String, Object>> getConsistencyMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();

            // 获取系统状态
            DataConsistencyManager.ConsistencyCheckResult status = consistencyManager.checkConsistency();

            // 基础指标
            metrics.put("timestamp", System.currentTimeMillis());
            metrics.put("systemHealthy", status != null && status.isHealthy());
            metrics.put("activeLocks", status != null ? status.getActiveLocks() : 0);
            metrics.put("versionEntries", status != null ? status.getVersionEntries() : 0);
            metrics.put("redisHealthy", status != null && status.isRedisHealthy());

            // 功能状态指标
            metrics.put("distributedLockEnabled", true);
            metrics.put("versionControlEnabled", true);
            metrics.put("transactionalOperationEnabled", true);
            metrics.put("reconciliationEnabled", true);

            // 性能指标（可以添加更多具体的性能监控）
            metrics.put("lockAcquisitionTimeout", 30000); // 30秒
            metrics.put("maxRetryAttempts", 3);
            metrics.put("concurrentThreads", 10);

            return ResponseDTO.ok(metrics, "监控指标获取成功");

        } catch (Exception e) {
            log.error("获取一致性保障监控指标异常", e);
            return ResponseDTO.error("指标获取异常: " + e.getMessage());
        }
    }
}

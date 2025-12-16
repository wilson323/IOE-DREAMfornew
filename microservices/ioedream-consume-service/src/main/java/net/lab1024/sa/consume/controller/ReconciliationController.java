package net.lab1024.sa.consume.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.service.consistency.ReconciliationService;

/**
 * 对账管理控制器
 * <p>
 * 提供对账相关的REST API接口
 * 支持日终对账、实时对账、对账历史查询等功能
 * 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/reconciliation")
@Tag(name = "对账管理", description = "消费对账相关接口")
public class ReconciliationController {

    @Resource
    private ReconciliationService reconciliationService;

    /**
     * 执行日终对账
     * <p>
     * 功能说明：
     * 1. 对指定日期的所有账户进行对账
     * 2. 对比系统余额与交易记录计算出的余额
     * 3. 发现差异记录并生成对账报告
     * 4. 支持自动修复（可选）
     * </p>
     *
     * @param reconcileDate 对账日期（格式：yyyy-MM-dd），默认为昨天
     * @return 对账结果
     */
    @PostMapping("/daily")
    @Observed(name = "reconciliation.performDailyReconciliation", contextualName = "reconciliation-daily")
    @Operation(summary = "执行日终对账", description = "对指定日期的所有账户进行对账")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<ReconciliationService.ReconciliationResult> performDailyReconciliation(
            @Parameter(description = "对账日期，格式：yyyy-MM-dd，默认为昨天")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reconcileDate) {

        log.info("[对账管理] 执行日终对账: reconcileDate={}", reconcileDate);

        try {
            // 如果没有指定日期，默认为昨天
            if (reconcileDate == null) {
                reconcileDate = LocalDate.now().minusDays(1);
            }

            ReconciliationService.ReconciliationResult result =
                    reconciliationService.performDailyReconciliation(reconcileDate);

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[对账管理] 执行日终对账参数错误: reconcileDate={}, error={}", reconcileDate, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[对账管理] 执行日终对账业务异常: reconcileDate={}, code={}, message={}", reconcileDate, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[对账管理] 执行日终对账系统异常: reconcileDate={}, code={}, message={}", reconcileDate, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("RECONCILIATION_SYSTEM_ERROR", "执行日终对账失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[对账管理] 执行日终对账未知异常: reconcileDate={}", reconcileDate, e);
            return ResponseDTO.error("RECONCILIATION_ERROR", "执行日终对账失败: " + e.getMessage());
        }
    }

    /**
     * 执行实时对账
     * <p>
     * 功能说明：
     * 1. 对指定账户或所有账户进行实时余额验证
     * 2. 对比系统余额与交易记录计算出的余额
     * 3. 发现差异立即告警并记录
     * 4. 支持自动修复（可选）
     * </p>
     *
     * @param accountId 账户ID（可选，null表示对所有账户对账）
     * @return 实时对账结果
     */
    @PostMapping("/realtime")
    @Observed(name = "reconciliation.performRealtimeReconciliation", contextualName = "reconciliation-realtime")
    @Operation(summary = "执行实时对账", description = "对指定账户或所有账户进行实时余额验证")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<ReconciliationService.ReconciliationResult> performRealtimeReconciliation(
            @Parameter(description = "账户ID，可选，null表示对所有账户对账")
            @RequestParam(required = false) Long accountId) {

        log.info("[对账管理] 执行实时对账: accountId={}", accountId);

        try {
            ReconciliationService.ReconciliationResult result =
                    reconciliationService.performRealTimeReconciliation(accountId);

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[对账管理] 执行实时对账参数错误: accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[对账管理] 执行实时对账业务异常: accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[对账管理] 执行实时对账系统异常: accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("REALTIME_RECONCILIATION_SYSTEM_ERROR", "执行实时对账失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[对账管理] 执行实时对账未知异常: accountId={}", accountId, e);
            return ResponseDTO.error("REALTIME_RECONCILIATION_ERROR", "执行实时对账失败: " + e.getMessage());
        }
    }

    /**
     * 查询对账历史
     * <p>
     * 功能说明：
     * 1. 查询指定日期范围内的对账历史记录
     * 2. 支持分页查询
     * 3. 返回对账结果详情
     * </p>
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate   结束日期（格式：yyyy-MM-dd）
     * @param pageNum   页码（从1开始）
     * @param pageSize  每页大小
     * @return 对账历史分页结果
     */
    @GetMapping("/history")
    @Observed(name = "reconciliation.queryReconciliationHistory", contextualName = "reconciliation-history")
    @Operation(summary = "查询对账历史", description = "查询指定日期范围内的对账历史记录")
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<PageResult<ReconciliationService.ReconciliationResult>> queryReconciliationHistory(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "页码，从1开始")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("[对账管理] 查询对账历史: startDate={}, endDate={}, pageNum={}, pageSize={}",
                startDate, endDate, pageNum, pageSize);

        try {
            ReconciliationService.ReconciliationHistoryResult historyResult =
                    reconciliationService.queryReconciliationHistory(startDate, endDate, pageNum, pageSize);

            // 转换为PageResult格式
            PageResult<ReconciliationService.ReconciliationResult> result = new PageResult<>();
            if (historyResult != null && historyResult.getRecords() != null) {
                result.setList(historyResult.getRecords().stream()
                        .map(record -> {
                            ReconciliationService.ReconciliationResult reconciliationResult = new ReconciliationService.ReconciliationResult();
                            reconciliationResult.setReconcileDate(record.getReconcileDate());
                            reconciliationResult.setStatus(record.getStatus());
                            return reconciliationResult;
                        })
                        .collect(java.util.stream.Collectors.toList()));
                result.setTotal(historyResult.getTotal() != null ? historyResult.getTotal() : 0L);
                result.setPageNum(historyResult.getPageNum() != null ? historyResult.getPageNum() : pageNum);
                result.setPageSize(historyResult.getPageSize() != null ? historyResult.getPageSize() : pageSize);
                result.setPages((int) Math.ceil((double) result.getTotal() / result.getPageSize()));
            } else {
                result.setList(new java.util.ArrayList<>());
                result.setTotal(0L);
                result.setPageNum(pageNum);
                result.setPageSize(pageSize);
                result.setPages(0);
            }

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[对账管理] 查询对账历史参数错误: startDate={}, endDate={}, error={}", startDate, endDate, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[对账管理] 查询对账历史业务异常: startDate={}, endDate={}, code={}, message={}", startDate, endDate, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[对账管理] 查询对账历史系统异常: startDate={}, endDate={}, code={}, message={}", startDate, endDate, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_RECONCILIATION_HISTORY_SYSTEM_ERROR", "查询对账历史失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[对账管理] 查询对账历史未知异常: startDate={}, endDate={}", startDate, endDate, e);
            return ResponseDTO.error("QUERY_RECONCILIATION_HISTORY_ERROR", "查询对账历史失败: " + e.getMessage());
        }
    }
}




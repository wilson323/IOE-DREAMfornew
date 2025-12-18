package net.lab1024.sa.visitor.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.BlacklistAddForm;
import net.lab1024.sa.visitor.domain.form.BlacklistQueryForm;
import net.lab1024.sa.visitor.domain.vo.BlacklistVO;
import net.lab1024.sa.visitor.service.VisitorBlacklistService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 访客黑名单管理控制器
 * <p>
 * 内存优化设计：
 * - 使用异步处理，提高并发性能
 * - 合理的分页参数限制，避免内存溢出
 * - 熔断器保护，防止级联故障
 * - 参数验证和异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/visitor/blacklist")
@Tag(name = "访客黑名单管理", description = "访客黑名单管理相关API")
@Validated
@CircuitBreaker(name = "visitorBlacklistController")
public class VisitorBlacklistController {

    @Resource
    private VisitorBlacklistService visitorBlacklistService;

    /**
     * 添加到黑名单
     * <p>
     * 将访客添加到黑名单中
     * 支持永久黑名单和临时黑名单
     * 自动生效，实时生效
     * </p>
     *
     * @param form 黑名单添加表单
     * @return 添加结果
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "添加到黑名单",
        description = "将访客添加到黑名单中，支持永久和临时黑名单"
    )
    @PostMapping
    public CompletableFuture<ResponseDTO<Long>> addToBlacklist(
            @Valid @RequestBody BlacklistAddForm form
    ) {
        log.info("[黑名单管理] 接收到添加黑名单请求, idCard={}, phone={}",
                maskSensitiveData(form.getIdCard()), maskSensitiveData(form.getPhone()));
        return visitorBlacklistService.addToBlacklist(form);
    }

    /**
     * 从黑名单移除
     * <p>
     * 将访客从黑名单中移除
     * 立即生效，解除所有限制
     * 记录移除操作日志
     * </p>
     *
     * @param visitorId 访客ID
     * @param reason 移除原因
     * @return 移除结果
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "从黑名单移除",
        description = "将访客从黑名单中移除，解除所有限制"
    )
    @DeleteMapping("/{visitorId}")
    public CompletableFuture<ResponseDTO<Void>> removeFromBlacklist(
            @Parameter(description = "访客ID", required = true, example = "1001")
            @PathVariable Long visitorId,
            @Parameter(description = "移除原因", required = true, example = "误添加，需要解除限制")
            @RequestParam String reason
    ) {
        log.info("[黑名单管理] 接收到移除黑名单请求, visitorId={}, reason={}", visitorId, reason);
        return visitorBlacklistService.removeFromBlacklist(visitorId, reason);
    }

    /**
     * 查询黑名单
     * <p>
     * 分页查询黑名单记录
     * 支持多种条件筛选
     * 按时间倒序排列
     * </p>
     *
     * @param form 查询表单
     * @return 黑名单分页列表
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "查询黑名单",
        description = "分页查询黑名单记录，支持多种条件筛选"
    )
    @GetMapping("/query")
    public CompletableFuture<ResponseDTO<PageResult<BlacklistVO>>> queryBlacklist(
            @Valid @ModelAttribute BlacklistQueryForm form
    ) {
        log.info("[黑名单管理] 接收到查询黑名单请求, pageNum={}, pageSize={}",
                form.getPageNum(), form.getPageSize());
        return visitorBlacklistService.queryBlacklist(form);
    }

    /**
     * 检查黑名单状态
     * <p>
     * 检查访客是否在黑名单中
     * 支持身份证号和手机号查询
     * 实时检查，缓存优化
     * </p>
     *
     * @param visitorId 访客ID
     * @param idCard 身份证号
     * @param phone 手机号
     * @return 黑名单状态
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "检查黑名单状态",
        description = "检查访客是否在黑名单中，支持多种查询方式"
    )
    @GetMapping("/check")
    public CompletableFuture<ResponseDTO<Boolean>> checkBlacklistStatus(
            @Parameter(description = "访客ID", example = "1001")
            @RequestParam(required = false) Long visitorId,
            @Parameter(description = "身份证号", example = "110101199001011234")
            @RequestParam(required = false) String idCard,
            @Parameter(description = "手机号", example = "13800138000")
            @RequestParam(required = false) String phone
    ) {
        log.info("[黑名单管理] 检查黑名单状态, visitorId={}, idCard={}, phone={}",
                visitorId, maskSensitiveData(idCard), maskSensitiveData(phone));
        return visitorBlacklistService.checkBlacklistStatus(visitorId, idCard, phone);
    }

    /**
     * 批量检查黑名单
     * <p>
     * 批量检查多个访客的黑名单状态
     * 高效批量查询，减少数据库访问
     * 适用于批量验证场景
     * </p>
     *
     * @param visitorIds 访客ID列表
     * @return 黑名单状态映射
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "批量检查黑名单",
        description = "批量检查多个访客的黑名单状态，提高查询效率"
    )
    @GetMapping("/batch-check")
    public CompletableFuture<ResponseDTO<List<Long>>> batchCheckBlacklistStatus(
            @Parameter(description = "访客ID列表", required = true)
            @RequestParam List<Long> visitorIds
    ) {
        log.info("[黑名单管理] 批量检查黑名单状态, count={}", visitorIds.size());
        return visitorBlacklistService.batchCheckBlacklistStatus(visitorIds);
    }

    /**
     * 更新黑名单状态
     * <p>
     * 更新黑名单记录的状态
     * 支持激活和停用操作
     * 自动清理过期记录
     * </p>
     *
     * @param blacklistId 黑名单记录ID
     * @param status 新状态
     * @return 更新结果
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "更新黑名单状态",
        description = "更新黑名单记录的状态，支持激活和停用"
    )
    @PutMapping("/{blacklistId}/status")
    public CompletableFuture<ResponseDTO<Void>> updateBlacklistStatus(
            @Parameter(description = "黑名单记录ID", required = true, example = "1001")
            @PathVariable Long blacklistId,
            @Parameter(description = "状态", required = true, example = "1")
            @RequestParam Integer status
    ) {
        log.info("[黑名单管理] 更新黑名单状态, blacklistId={}, status={}", blacklistId, status);
        return visitorBlacklistService.updateBlacklistStatus(blacklistId, status);
    }

    /**
     * 清理过期黑名单
     * <p>
     * 清理已过期的临时黑名单记录
     * 定时任务调用，维护数据一致性
     * 释放系统资源
     * </p>
     *
     * @return 清理统计结果
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "清理过期黑名单",
        description = "清理已过期的临时黑名单记录，维护数据一致性"
    )
    @DeleteMapping("/clean-expired")
    public CompletableFuture<ResponseDTO<Integer>> cleanExpiredBlacklist() {
        log.info("[黑名单管理] 开始清理过期黑名单");
        return visitorBlacklistService.cleanExpiredBlacklist();
    }

    /**
     * 获取黑名单统计
     * <p>
     * 获取黑名单统计数据
     * 包括总数、分类统计、趋势分析
     * 用于管理决策支持
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @TimeLimiter(name = "visitorBlacklistController")
    @Operation(
        summary = "获取黑名单统计",
        description = "获取黑名单统计数据，包括总数、分类统计、趋势分析"
    )
    @GetMapping("/statistics")
    public CompletableFuture<ResponseDTO<Object>> getBlacklistStatistics(
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime
    ) {
        log.info("[黑名单管理] 获取黑名单统计, startTime={}, endTime={}", startTime, endTime);
        return visitorBlacklistService.getBlacklistStatistics(startTime, endTime);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 脱敏敏感数据
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }
}
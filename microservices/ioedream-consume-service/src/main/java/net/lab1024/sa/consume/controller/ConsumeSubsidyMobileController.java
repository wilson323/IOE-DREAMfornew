package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.vo.ConsumeSubsidyVO;
import net.lab1024.sa.consume.service.ConsumeSubsidyService;

/**
 * 移动端补贴管理控制器
 * <p>
 * 提供移动端补贴查询功能，包括：
 * 1. 补贴余额查询
 * 2. 补贴发放记录
 * 3. 补贴使用明细
 * 4. 补贴统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/v1/consume/mobile/subsidy")
@Tag(name = "移动端补贴管理", description = "移动端补贴查询接口")
@Slf4j
public class ConsumeSubsidyMobileController {

    @Resource
    private ConsumeSubsidyService consumeSubsidyService;

    @Operation(summary = "获取补贴余额汇总", description = "获取用户各类补贴余额汇总信息")
    @GetMapping("/balance/{userId}")
    public ResponseDTO<Map<String, Object>> getSubsidyBalance(@PathVariable Long userId) {
        log.info("[移动端补贴] 查询补贴余额: userId={}", userId);
        try {
            Map<String, Object> balanceInfo = consumeSubsidyService.getUserSubsidyStatistics(userId);
            log.info("[移动端补贴] 查询补贴余额成功: userId={}, totalBalance={}",
                userId, balanceInfo.get("totalBalance"));
            return ResponseDTO.ok(balanceInfo);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询补贴余额异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取用户补贴列表", description = "分页获取用户补贴发放记录")
    @GetMapping("/records/{userId}")
    public ResponseDTO<PageResult<ConsumeSubsidyVO>> getSubsidyRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端补贴] 查询补贴记录: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        try {
            // 获取用户所有补贴
            List<ConsumeSubsidyVO> subsidies = consumeSubsidyService.getUserSubsidies(userId);

            // 手动分页
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, subsidies.size());

            List<ConsumeSubsidyVO> pageList = subsidies.subList(start, end);
            PageResult<ConsumeSubsidyVO> pageResult = PageResult.of(
                pageList,
                (long) subsidies.size(),
                pageNum,
                pageSize
            );

            log.info("[移动端补贴] 查询补贴记录成功: userId={}, total={}", userId, subsidies.size());
            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询补贴记录异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取可用补贴列表", description = "获取用户当前可使用的补贴列表")
    @GetMapping("/available/{userId}")
    public ResponseDTO<List<ConsumeSubsidyVO>> getAvailableSubsidies(@PathVariable Long userId) {
        log.info("[移动端补贴] 查询可用补贴: userId={}", userId);
        try {
            List<ConsumeSubsidyVO> availableSubsidies = consumeSubsidyService.getAvailableSubsidies(userId);
            log.info("[移动端补贴] 查询可用补贴成功: userId={}, count={}", userId, availableSubsidies.size());
            return ResponseDTO.ok(availableSubsidies);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询可用补贴异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取即将过期的补贴", description = "获取指定天数内即将过期的补贴")
    @GetMapping("/expiring/{userId}")
    public ResponseDTO<List<ConsumeSubsidyVO>> getExpiringSubsidies(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("[移动端补贴] 查询即将过期补贴: userId={}, days={}", userId, days);
        try {
            List<ConsumeSubsidyVO> expiringSubsidies = consumeSubsidyService.getExpiringSoonSubsidies(days);
            log.info("[移动端补贴] 查询即将过期补贴成功: userId={}, count={}", userId, expiringSubsidies.size());
            return ResponseDTO.ok(expiringSubsidies);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询即将过期补贴异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取补贴使用明细", description = "分页获取补贴使用明细记录")
    @GetMapping("/usage/{userId}")
    public ResponseDTO<PageResult<Map<String, Object>>> getSubsidyUsage(
            @PathVariable Long userId,
            @RequestParam(required = false) Long subsidyId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端补贴] 查询补贴使用明细: userId={}, subsidyId={}, pageNum={}, pageSize={}",
            userId, subsidyId, pageNum, pageSize);
        try {
            PageResult<Map<String, Object>> usageResult = consumeSubsidyService.getSubsidyUsage(
                subsidyId != null ? subsidyId : 0L, pageNum, pageSize);
            log.info("[移动端补贴] 查询补贴使用明细成功: userId={}, total={}",
                userId, usageResult.getTotal());
            return ResponseDTO.ok(usageResult);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询补贴使用明细异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取补贴统计信息", description = "获取用户补贴统计汇总信息")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getSubsidyStatistics(@PathVariable Long userId) {
        log.info("[移动端补贴] 查询补贴统计: userId={}", userId);
        try {
            Map<String, Object> statistics = consumeSubsidyService.getUserSubsidyStatistics(userId);
            log.info("[移动端补贴] 查询补贴统计成功: userId={}", userId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询补贴统计异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取即将用完的补贴", description = "获取使用率超过指定阈值的补贴")
    @GetMapping("/nearly-depleted/{userId}")
    public ResponseDTO<List<ConsumeSubsidyVO>> getNearlyDepletedSubsidies(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "80") Integer threshold) {
        log.info("[移动端补贴] 查询即将用完补贴: userId={}, threshold={}%", userId, threshold);
        try {
            BigDecimal thresholdRate = new BigDecimal(threshold);
            List<ConsumeSubsidyVO> nearlyDepleted = consumeSubsidyService.getNearlyDepletedSubsidies(thresholdRate);

            // 过滤出当前用户的补贴
            List<ConsumeSubsidyVO> userSubsidies = nearlyDepleted.stream()
                .filter(subsidy -> userId.equals(subsidy.getUserId()))
                .toList();

            log.info("[移动端补贴] 查询即将用完补贴成功: userId={}, count={}", userId, userSubsidies.size());
            return ResponseDTO.ok(userSubsidies);
        } catch (Exception e) {
            log.error("[移动端补贴] 查询即将用完补贴异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}

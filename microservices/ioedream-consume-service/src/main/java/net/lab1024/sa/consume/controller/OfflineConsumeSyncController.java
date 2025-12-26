package net.lab1024.sa.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.entity.OfflineConsumeRecordEntity;
import net.lab1024.sa.consume.entity.OfflineSyncLogEntity;
import net.lab1024.sa.consume.entity.OfflineWhitelistEntity;
import net.lab1024.sa.consume.service.OfflineConsumeRecordService;
import net.lab1024.sa.consume.service.OfflineSyncLogService;
import net.lab1024.sa.consume.service.OfflineWhitelistService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 离线消费同步Controller
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@RestController
@RequestMapping("/api/consume/offline")
@Tag(name = "离线消费同步", description = "离线消费记录同步与管理")
@Slf4j
public class OfflineConsumeSyncController {

    @Resource
    private OfflineConsumeRecordService offlineConsumeRecordService;

    @Resource
    private OfflineSyncLogService syncLogService;

    @Resource
    private OfflineWhitelistService whitelistService;

    /**
     * 批量同步离线消费记录
     */
    @PostMapping("/sync")
    @Operation(summary = "批量同步离线消费记录")
    public ResponseDTO<Object> batchSync(@RequestBody List<OfflineConsumeRecordEntity> records) {
        log.info("[离线消费API] 批量同步请求: recordCount={}", records.size());
        Object result = offlineConsumeRecordService.batchSync(records);
        return ResponseDTO.ok(result);
    }

    /**
     * 查询用户待同步记录
     */
    @GetMapping("/records/pending")
    @Operation(summary = "查询用户待同步记录")
    public ResponseDTO<List<OfflineConsumeRecordEntity>> getPendingRecords(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        log.info("[离线消费API] 查询待同步记录: userId={}", userId);
        List<OfflineConsumeRecordEntity> records = offlineConsumeRecordService.getPendingRecordsByUserId(userId);
        return ResponseDTO.ok(records);
    }

    /**
     * 查询未解决冲突
     */
    @GetMapping("/records/conflicts")
    @Operation(summary = "查询未解决冲突记录")
    public ResponseDTO<List<OfflineConsumeRecordEntity>> getUnresolvedConflicts() {
        log.info("[离线消费API] 查询未解决冲突记录");
        List<OfflineConsumeRecordEntity> conflicts = offlineConsumeRecordService.getUnresolvedConflicts();
        return ResponseDTO.ok(conflicts);
    }

    /**
     * 人工解决冲突
     */
    @PutMapping("/records/{recordId}/resolve")
    @Operation(summary = "人工解决冲突")
    public ResponseDTO<Void> resolveConflict(
            @Parameter(description = "记录ID") @PathVariable String recordId,
            @Parameter(description = "解决备注") @RequestParam String resolvedRemark) {
        log.info("[离线消费API] 解决冲突: recordId={}, remark={}", recordId, resolvedRemark);
        offlineConsumeRecordService.resolveConflict(recordId, resolvedRemark);
        return ResponseDTO.ok();
    }

    /**
     * 查询同步日志
     */
    @GetMapping("/sync/logs")
    @Operation(summary = "查询同步日志")
    public ResponseDTO<List<OfflineSyncLogEntity>> getSyncLogs(
            @Parameter(description = "查询数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("[离线消费API] 查询同步日志: limit={}", limit);
        List<OfflineSyncLogEntity> logs = syncLogService.getRecentLogs(limit);
        return ResponseDTO.ok(logs);
    }

    /**
     * 查询用户有效白名单
     */
    @GetMapping("/whitelist/user/{userId}")
    @Operation(summary = "查询用户有效白名单")
    public ResponseDTO<List<OfflineWhitelistEntity>> getUserWhitelist(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("[离线消费API] 查询用户白名单: userId={}", userId);
        List<OfflineWhitelistEntity> whitelist = whitelistService.getValidWhitelistByUserId(userId);
        return ResponseDTO.ok(whitelist);
    }

    /**
     * 验证用户白名单
     */
    @GetMapping("/whitelist/validate")
    @Operation(summary = "验证用户是否在白名单中")
    public ResponseDTO<Boolean> validateWhitelist(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "设备ID") @RequestParam Long deviceId) {
        log.info("[离线消费API] 验证白名单: userId={}, deviceId={}", userId, deviceId);
        boolean isValid = whitelistService.isValidWhitelistUser(userId, deviceId);
        return ResponseDTO.ok(isValid);
    }

    /**
     * 批量添加白名单
     */
    @PostMapping("/whitelist/batch")
    @Operation(summary = "批量添加白名单")
    public ResponseDTO<Integer> batchAddWhitelist(@RequestBody List<OfflineWhitelistEntity> whitelist) {
        log.info("[离线消费API] 批量添加白名单: count={}", whitelist.size());
        Integer successCount = whitelistService.batchAddWhitelist(whitelist);
        return ResponseDTO.ok(successCount);
    }
}

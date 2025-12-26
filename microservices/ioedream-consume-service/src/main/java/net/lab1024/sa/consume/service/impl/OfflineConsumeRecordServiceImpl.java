package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.consume.dao.OfflineConsumeRecordDao;
import net.lab1024.sa.consume.domain.vo.OfflineConsumeDTO;
import net.lab1024.sa.consume.domain.vo.SyncResultVO;
import net.lab1024.sa.common.entity.consume.OfflineConsumeRecordEntity;
import net.lab1024.sa.common.entity.consume.OfflineSyncLogEntity;
import net.lab1024.sa.consume.service.OfflineConsumeRecordService;
import net.lab1024.sa.consume.service.OfflineSyncLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 离线消费记录Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Service
@Slf4j
public class OfflineConsumeRecordServiceImpl extends ServiceImpl<OfflineConsumeRecordDao, OfflineConsumeRecordEntity>
        implements OfflineConsumeRecordService {

    @Resource
    private OfflineSyncLogService syncLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object batchSync(List<OfflineConsumeRecordEntity> records) {
        long startTime = System.currentTimeMillis();
        log.info("[离线消费同步] 开始同步 {} 条记录", records.size());

        // 生成同步批次号
        String syncBatchNo = "SYNC_" + System.currentTimeMillis();

        try {
            // 1. 批量校验
            ValidationResult validation = validateRecords(records);
            if (!validation.isSuccess()) {
                log.warn("[离线消费同步] 记录校验失败: {}", validation.getErrors());
                return SyncResultVO.fail(validation.getErrors());
            }

            // 2. 冲突检测
            List<OfflineConsumeRecordEntity> conflicts = detectConflicts(records);
            if (!conflicts.isEmpty()) {
                log.warn("[离线消费同步] 检测到 {} 条冲突记录", conflicts.size());
            }

            // 3. 批量保存
            int successCount = 0;
            int failedCount = 0;

            for (OfflineConsumeRecordEntity record : records) {
                try {
                    // 更新同步状态
                    record.setSyncStatus(2); // 已同步
                    record.setSyncTime(LocalDateTime.now());

                    this.saveOrUpdate(record);
                    successCount++;

                } catch (Exception e) {
                    log.error("[离线消费同步] 记录同步失败: id={}, error={}",
                        record.getId(), e.getMessage(), e);
                    record.setSyncStatus(3); // 冲突/失败
                    record.setSyncErrorMessage(e.getMessage());
                    this.updateById(record);
                    failedCount++;
                }
            }

            // 4. 记录同步日志
            long durationMs = System.currentTimeMillis() - startTime;
            syncLogService.recordSyncLog(syncBatchNo, records.size(), successCount,
                failedCount, conflicts.size(), durationMs);

            log.info("[离线消费同步] 同步完成: 总数={}, 成功={}, 失败={}, 冲突={}, 耗时={}ms",
                records.size(), successCount, failedCount, conflicts.size(), durationMs);

            return SyncResultVO.success(syncBatchNo, records.size(), durationMs);

        } catch (Exception e) {
            log.error("[离线消费同步] 同步异常: {}", e.getMessage(), e);
            throw new BusinessException("OFFLINE_SYNC_ERROR", "离线消费同步失败: " + e.getMessage());
        }
    }

    @Override
    public List<OfflineConsumeRecordEntity> getPendingRecordsByUserId(Long userId) {
        log.debug("[离线消费记录] 查询用户待同步记录: userId={}", userId);
        return baseMapper.selectPendingRecordsByUserId(userId);
    }

    @Override
    public List<OfflineConsumeRecordEntity> getUnresolvedConflicts() {
        log.debug("[离线消费记录] 查询未解决冲突记录");
        return baseMapper.selectUnresolvedConflicts(100);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveConflict(String recordId, String resolvedRemark) {
        log.info("[离线消费记录] 解决冲突: recordId={}, remark={}", recordId, resolvedRemark);

        OfflineConsumeRecordEntity record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("RECORD_NOT_FOUND", "离线消费记录不存在");
        }

        // 标记为已人工处理
        record.setResolved(2);
        record.setResolvedTime(LocalDateTime.now());
        record.setResolvedRemark(resolvedRemark);

        this.updateById(record);
        log.info("[离线消费记录] 冲突已解决: recordId={}", recordId);
    }

    /**
     * 校验记录
     */
    private ValidationResult validateRecords(List<OfflineConsumeRecordEntity> records) {
        List<String> errors = new ArrayList<>();

        for (OfflineConsumeRecordEntity record : records) {
            // 必填字段校验
            if (record.getUserId() == null) {
                errors.add("记录ID " + record.getId() + ": 用户ID不能为空");
                continue;
            }

            if (record.getDeviceId() == null) {
                errors.add("记录ID " + record.getId() + ": 设备ID不能为空");
                continue;
            }

            if (record.getAmount() == null || record.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                errors.add("记录ID " + record.getId() + ": 消费金额必须大于0");
                continue;
            }

            // 时效校验（24小时内）
            if (record.getConsumeTime().isBefore(LocalDateTime.now().minusDays(1))) {
                errors.add("记录ID " + record.getId() + ": 消费记录过期（超过24小时）");
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.fail(errors);
    }

    /**
     * 检测冲突
     */
    private List<OfflineConsumeRecordEntity> detectConflicts(List<OfflineConsumeRecordEntity> records) {
        List<OfflineConsumeRecordEntity> conflicts = new ArrayList<>();

        // 按用户分组，检测同一用户的重复消费
        Map<Long, List<OfflineConsumeRecordEntity>> userRecords = records.stream()
            .collect(Collectors.groupingBy(OfflineConsumeRecordEntity::getUserId));

        for (Map.Entry<Long, List<OfflineConsumeRecordEntity>> entry : userRecords.entrySet()) {
            Long userId = entry.getKey();
            List<OfflineConsumeRecordEntity> userRecs = entry.getValue();

            // 按时间排序
            userRecs.sort(Comparator.comparing(OfflineConsumeRecordEntity::getConsumeTime));

            // 检测时间戳冲突（5分钟内）
            for (int i = 1; i < userRecs.size(); i++) {
                OfflineConsumeRecordEntity prev = userRecs.get(i - 1);
                OfflineConsumeRecordEntity curr = userRecs.get(i);

                long interval = Duration.between(prev.getConsumeTime(), curr.getConsumeTime()).toSeconds();
                if (interval < 300) { // 5分钟内
                    curr.setConflictType(1); // 时间戳冲突
                    curr.setConflictReason("与记录 " + prev.getId() + " 时间间隔过短（" + interval + "秒）");
                    curr.setResolved(0);
                    conflicts.add(curr);
                }
            }
        }

        return conflicts;
    }

    /**
     * 校验结果
     */
    @Data
    private static class ValidationResult {
        private Boolean success;
        private List<String> errors;

        public boolean isSuccess() {
            return success != null && success;
        }

        public static ValidationResult success() {
            ValidationResult result = new ValidationResult();
            result.setSuccess(true);
            result.setErrors(new ArrayList<>());
            return result;
        }

        public static ValidationResult fail(List<String> errors) {
            ValidationResult result = new ValidationResult();
            result.setSuccess(false);
            result.setErrors(errors);
            return result;
        }
    }
}

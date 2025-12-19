package net.lab1024.sa.access.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.AccessCacheConstants;
import net.lab1024.sa.access.service.EdgeOfflineRecordReplayService;
import net.lab1024.sa.access.util.AccessRecordIdempotencyUtil;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 边缘验证离线记录补录服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解注册为Spring Bean
 * - 使用@Resource注入依赖
 * - 复用AccessRecordIdempotencyUtil工具类
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 核心职责：
 * - 定时补录Redis中缓存的离线记录
 * - 网络恢复后自动同步离线记录到数据库
 * - 支持手动触发补录
 * - 幂等性检查（防止重复补录）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class EdgeOfflineRecordReplayServiceImpl implements EdgeOfflineRecordReplayService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存键前缀和过期时间统一使用AccessCacheConstants
     */

    /**
     * 批量补录大小（每次补录的记录数）
     */
    private static final int BATCH_REPLAY_SIZE = 100;

    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 补录离线记录
     * <p>
     * 从Redis中读取离线记录队列，批量补录到数据库
     * </p>
     *
     * @return 补录结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<EdgeOfflineRecordReplayService.ReplayResult> replayOfflineRecords() {
        long startTime = System.currentTimeMillis();
        log.info("[离线记录补录] 开始补录离线记录");

        ReplayResult result = new ReplayResult();
        result.setTotalCount(0);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setDuplicateCount(0);

        try {
            // 1. 从Redis队列获取离线记录唯一标识列表
            List<String> recordUniqueIds = getOfflineRecordUniqueIds();
            result.setTotalCount(recordUniqueIds.size());

            if (recordUniqueIds.isEmpty()) {
                result.setProcessTime(System.currentTimeMillis() - startTime);
                log.info("[离线记录补录] 没有待补录的记录");
                return ResponseDTO.ok(result);
            }

            // 2. 批量处理（分批补录，避免一次性处理过多）
            List<AccessRecordEntity> entitiesToInsert = new ArrayList<>();
            int processedCount = 0;

            for (String recordUniqueId : recordUniqueIds) {
                try {
                    // 2.1 从Redis获取离线记录
                    AccessRecordEntity entity = getOfflineRecord(recordUniqueId);
                    if (entity == null) {
                        result.setFailCount(result.getFailCount() + 1);
                        log.warn("[离线记录补录] 记录不存在: recordUniqueId={}", recordUniqueId);
                        continue;
                    }

                    // 2.2 幂等性检查（使用统一工具类）
                    if (AccessRecordIdempotencyUtil.isDuplicateRecord(
                            recordUniqueId, entity.getUserId(), entity.getDeviceId(), entity.getAccessTime(),
                            redisTemplate, accessRecordDao)) {
                        result.setDuplicateCount(result.getDuplicateCount() + 1);
                        log.debug("[离线记录补录] 记录重复（幂等性检查）: recordUniqueId={}", recordUniqueId);
                        // 从队列中移除
                        removeFromOfflineQueue(recordUniqueId);
                        continue;
                    }

                    // 2.3 添加到待插入列表
                    entitiesToInsert.add(entity);
                    processedCount++;

                    // 2.4 达到批量大小，执行批量插入
                    if (entitiesToInsert.size() >= BATCH_REPLAY_SIZE) {
                        int inserted = batchInsertRecords(entitiesToInsert, result);
                        result.setSuccessCount(result.getSuccessCount() + inserted);
                        entitiesToInsert.clear();
                    }

                } catch (Exception e) {
                    result.setFailCount(result.getFailCount() + 1);
                    log.error("[离线记录补录] 处理记录异常: recordUniqueId={}, error={}",
                            recordUniqueId, e.getMessage(), e);
                }
            }

            // 3. 插入剩余记录
            if (!entitiesToInsert.isEmpty()) {
                int inserted = batchInsertRecords(entitiesToInsert, result);
                result.setSuccessCount(result.getSuccessCount() + inserted);
            }

            // 4. 更新处理时间
            result.setProcessTime(System.currentTimeMillis() - startTime);

            log.info("[离线记录补录] 补录完成: total={}, success={}, fail={}, duplicate={}, processTime={}ms",
                    result.getTotalCount(), result.getSuccessCount(),
                    result.getFailCount(), result.getDuplicateCount(), result.getProcessTime());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            result.setProcessTime(System.currentTimeMillis() - startTime);
            log.error("[离线记录补录] 补录异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("REPLAY_ERROR", "补录异常: " + e.getMessage());
        }
    }

    /**
     * 获取离线记录统计
     *
     * @return 统计结果
     */
    @Override
    public ResponseDTO<EdgeOfflineRecordReplayService.OfflineRecordStatistics> getOfflineRecordStatistics() {
        log.debug("[离线记录补录] 获取离线记录统计");

        try {
            OfflineRecordStatistics statistics = new OfflineRecordStatistics();

            // 1. 获取队列长度
            Long queueSize = redisTemplate.opsForList().size(AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE);
            statistics.setTotalCount(queueSize != null ? queueSize.intValue() : 0);

            // 2. 获取最早和最晚记录时间
            if (statistics.getTotalCount() > 0) {
                // 获取队列中的第一个和最后一个记录唯一标识
                String firstRecordId = (String) redisTemplate.opsForList().index(AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE, 0);
                String lastRecordId = (String) redisTemplate.opsForList().index(
                        AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE, statistics.getTotalCount() - 1);

                if (firstRecordId != null) {
                    AccessRecordEntity firstRecord = getOfflineRecord(firstRecordId);
                    if (firstRecord != null && firstRecord.getAccessTime() != null) {
                        statistics.setEarliestRecordTime(firstRecord.getAccessTime().format(TIME_FORMATTER));
                    }
                }

                if (lastRecordId != null) {
                    AccessRecordEntity lastRecord = getOfflineRecord(lastRecordId);
                    if (lastRecord != null && lastRecord.getAccessTime() != null) {
                        statistics.setLatestRecordTime(lastRecord.getAccessTime().format(TIME_FORMATTER));
                    }
                }
            }

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[离线记录补录] 获取统计异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("STATISTICS_ERROR", "获取统计异常: " + e.getMessage());
        }
    }

    /**
     * 从Redis队列获取离线记录唯一标识列表
     *
     * @return 记录唯一标识列表
     */
    private List<String> getOfflineRecordUniqueIds() {
        try {
            Long queueSize = redisTemplate.opsForList().size(AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE);
            if (queueSize == null || queueSize == 0) {
                return new ArrayList<>();
            }

            // 获取所有记录唯一标识（限制数量，避免一次性处理过多）
            List<Object> recordIds = redisTemplate.opsForList().range(
                    AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE, 0, Math.min(queueSize.intValue(), BATCH_REPLAY_SIZE * 10) - 1);

            List<String> result = new ArrayList<>();
            if (recordIds != null) {
                for (Object recordId : recordIds) {
                    if (recordId instanceof String) {
                        result.add((String) recordId);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("[离线记录补录] 获取离线记录队列异常: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 从Redis获取离线记录
     *
     * @param recordUniqueId 记录唯一标识
     * @return 记录实体
     */
    private AccessRecordEntity getOfflineRecord(String recordUniqueId) {
        try {
            String cacheKey = AccessCacheConstants.buildOfflineRecordKey(recordUniqueId);
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof AccessRecordEntity) {
                return (AccessRecordEntity) cached;
            }
            return null;
        } catch (Exception e) {
            log.warn("[离线记录补录] 获取离线记录失败: recordUniqueId={}, error={}",
                    recordUniqueId, e.getMessage());
            return null;
        }
    }

    /**
     * 批量插入记录
     *
     * @param entities 实体列表
     * @param result 补录结果（用于更新统计）
     * @return 成功插入数量
     */
    private int batchInsertRecords(List<AccessRecordEntity> entities, ReplayResult result) {
        try {
            // 1. 批量插入数据库
            int insertCount = accessRecordDao.batchInsert(entities);

            // 2. 更新记录唯一标识缓存（使用统一工具类）
            for (AccessRecordEntity entity : entities) {
                if (entity.getRecordUniqueId() != null) {
                    AccessRecordIdempotencyUtil.updateRecordUniqueIdCache(
                            entity.getRecordUniqueId(), redisTemplate);
                    // 从离线队列中移除
                    removeFromOfflineQueue(entity.getRecordUniqueId());
                    // 删除离线记录缓存
                    deleteOfflineRecord(entity.getRecordUniqueId());
                }
            }

            log.debug("[离线记录补录] 批量插入成功: count={}", insertCount);
            return insertCount;

        } catch (Exception e) {
            log.error("[离线记录补录] 批量插入异常: count={}, error={}",
                    entities.size(), e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 从离线队列中移除记录唯一标识
     *
     * @param recordUniqueId 记录唯一标识
     */
    private void removeFromOfflineQueue(String recordUniqueId) {
        try {
            redisTemplate.opsForList().remove(AccessCacheConstants.CACHE_KEY_OFFLINE_QUEUE, 1, recordUniqueId);
        } catch (Exception e) {
            log.warn("[离线记录补录] 从队列移除失败: recordUniqueId={}, error={}",
                    recordUniqueId, e.getMessage());
        }
    }

    /**
     * 删除离线记录缓存
     *
     * @param recordUniqueId 记录唯一标识
     */
    private void deleteOfflineRecord(String recordUniqueId) {
        try {
            String cacheKey = AccessCacheConstants.buildOfflineRecordKey(recordUniqueId);
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("[离线记录补录] 删除离线记录缓存失败: recordUniqueId={}, error={}",
                    recordUniqueId, e.getMessage());
        }
    }

    /**
     * 补录结果实现
     */
    @Data
    public static class ReplayResult implements EdgeOfflineRecordReplayService.ReplayResult {
        private int totalCount;
        private int successCount;
        private int failCount;
        private int duplicateCount;
        private long processTime;
    }

    /**
     * 离线记录统计实现
     */
    @Data
    public static class OfflineRecordStatistics implements EdgeOfflineRecordReplayService.OfflineRecordStatistics {
        private int totalCount;
        private String earliestRecordTime;
        private String latestRecordTime;
    }
}

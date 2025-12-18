package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessRecordBatchUploadRequest;
import net.lab1024.sa.access.service.AccessRecordBatchService;
import net.lab1024.sa.access.util.AccessRecordIdempotencyUtil;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.common.response.ResponseDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 门禁记录批量上传服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解注册为Spring Bean
 * - 使用@Resource注入依赖
 * - 批量插入数据库
 * - 幂等性检查（防止重复上传）
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AccessRecordBatchServiceImpl implements AccessRecordBatchService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存键前缀（复用统一工具类的记录唯一标识缓存键）
     */
    private static final String CACHE_KEY_BATCH_STATUS = "access:batch:status:";
    
    /**
     * 缓存过期时间
     */
    private static final Duration CACHE_EXPIRE_BATCH = Duration.ofHours(24); // 批次状态缓存24小时

    /**
     * 批量上传通行记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<AccessRecordBatchUploadRequest.BatchUploadResult> batchUploadRecords(
            AccessRecordBatchUploadRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("[批量上传] 开始批量上传: deviceId={}, recordCount={}", 
                request.getDeviceId(), request.getRecords() != null ? request.getRecords().size() : 0);

        // 1. 生成批次ID（如果未提供）
        String batchId = request.getBatchId();
        if (batchId == null || batchId.trim().isEmpty()) {
            batchId = generateBatchId(request.getDeviceId());
        }

        // 2. 检查批次是否已处理（幂等性检查）
        AccessRecordBatchUploadRequest.BatchUploadResult cachedResult = getCachedBatchResult(batchId);
        if (cachedResult != null && "SUCCESS".equals(cachedResult.getStatus())) {
            log.info("[批量上传] 批次已处理（幂等性检查）: batchId={}", batchId);
            return ResponseDTO.ok(cachedResult);
        }

        // 3. 初始化结果
        AccessRecordBatchUploadRequest.BatchUploadResult result = 
                AccessRecordBatchUploadRequest.BatchUploadResult.builder()
                        .batchId(batchId)
                        .totalCount(request.getRecords() != null ? request.getRecords().size() : 0)
                        .successCount(0)
                        .failCount(0)
                        .duplicateCount(0)
                        .status("PROCESSING")
                        .errorMessages(new ArrayList<>())
                        .build();

        try {
            // 4. 幂等性检查：过滤重复记录
            List<AccessRecordBatchUploadRequest.AccessRecordDTO> validRecords = 
                    filterDuplicateRecords(request.getRecords(), request.getDeviceId(), result);

            if (validRecords.isEmpty()) {
                // 所有记录都是重复的
                result.setStatus("SUCCESS");
                result.setProcessTime(System.currentTimeMillis() - startTime);
                cacheBatchResult(batchId, result);
                log.info("[批量上传] 所有记录都是重复的: batchId={}, duplicateCount={}", 
                        batchId, result.getDuplicateCount());
                return ResponseDTO.ok(result);
            }

            // 5. 转换为Entity列表
            List<AccessRecordEntity> entities = convertToEntities(validRecords, request.getDeviceId());

            // 6. 批量插入数据库
            int insertCount = accessRecordDao.batchInsert(entities);
            result.setSuccessCount(insertCount);

            // 7. 更新记录唯一标识缓存（用于后续幂等性检查）
            updateRecordUniqueIdCache(validRecords);

            // 8. 更新结果状态
            result.setFailCount(result.getTotalCount() - result.getSuccessCount() - result.getDuplicateCount());
            result.setStatus(result.getFailCount() == 0 ? "SUCCESS" : "FAILED");
            result.setProcessTime(System.currentTimeMillis() - startTime);

            // 9. 缓存结果
            cacheBatchResult(batchId, result);

            log.info("[批量上传] 批量上传完成: batchId={}, total={}, success={}, fail={}, duplicate={}, processTime={}ms",
                    batchId, result.getTotalCount(), result.getSuccessCount(), 
                    result.getFailCount(), result.getDuplicateCount(), result.getProcessTime());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[批量上传] 批量上传异常: batchId={}, error={}", batchId, e.getMessage(), e);
            result.setStatus("FAILED");
            result.setFailCount(result.getTotalCount());
            result.setProcessTime(System.currentTimeMillis() - startTime);
            result.getErrorMessages().add("批量上传异常: " + e.getMessage());
            cacheBatchResult(batchId, result);
            return ResponseDTO.error("BATCH_UPLOAD_ERROR", "批量上传异常: " + e.getMessage());
        }
    }

    /**
     * 查询批量上传状态
     */
    @Override
    public ResponseDTO<AccessRecordBatchUploadRequest.BatchUploadResult> getUploadStatus(String batchId) {
        log.debug("[批量上传] 查询上传状态: batchId={}", batchId);

        try {
            AccessRecordBatchUploadRequest.BatchUploadResult result = getCachedBatchResult(batchId);
            if (result == null) {
                return ResponseDTO.error("BATCH_NOT_FOUND", "批次不存在或已过期");
            }
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[批量上传] 查询上传状态异常: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_STATUS_ERROR", "查询上传状态异常: " + e.getMessage());
        }
    }

    /**
     * 生成批次ID
     */
    private String generateBatchId(String deviceId) {
        return "BATCH_" + deviceId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 过滤重复记录（幂等性检查）
     * <p>
     * 使用 userId + deviceId + accessTime 的组合作为唯一标识
     * </p>
     */
    private List<AccessRecordBatchUploadRequest.AccessRecordDTO> filterDuplicateRecords(
            List<AccessRecordBatchUploadRequest.AccessRecordDTO> records,
            String deviceId,
            AccessRecordBatchUploadRequest.BatchUploadResult result) {
        
        List<AccessRecordBatchUploadRequest.AccessRecordDTO> validRecords = new ArrayList<>();
        int duplicateCount = 0;

        // 将deviceId转换为Long（统一处理）
        Long deviceIdLong = null;
        try {
            deviceIdLong = Long.parseLong(deviceId);
        } catch (NumberFormatException e) {
            log.warn("[批量上传] 设备ID格式错误: deviceId={}", deviceId);
            // 如果转换失败，后续处理会使用hashCode作为临时方案
        }

        for (AccessRecordBatchUploadRequest.AccessRecordDTO record : records) {
            // 1. 生成记录唯一标识（使用统一工具类）
            String recordUniqueId = deviceIdLong != null ? 
                    generateRecordUniqueId(record, deviceIdLong) : 
                    generateRecordUniqueIdFallback(record, deviceId);
            record.setRecordUniqueId(recordUniqueId);

            // 2. 使用统一工具类进行幂等性检查
            // 注意：需要将deviceId从String转换为Long
            try {
                Long deviceIdLong = Long.parseLong(deviceId);
                if (AccessRecordIdempotencyUtil.isDuplicateRecord(
                        recordUniqueId, record.getUserId(), deviceIdLong, record.getAccessTime(),
                        redisTemplate, accessRecordDao)) {
                    duplicateCount++;
                    log.debug("[批量上传] 记录重复（幂等性检查）: recordUniqueId={}", recordUniqueId);
                    continue;
                }
            } catch (NumberFormatException e) {
                log.warn("[批量上传] 设备ID格式错误，跳过幂等性检查: deviceId={}, recordUniqueId={}", 
                        deviceId, recordUniqueId);
                // 继续处理，不因为格式错误而跳过记录
            } catch (Exception e) {
                log.warn("[批量上传] 幂等性检查异常，跳过: recordUniqueId={}, error={}", 
                        recordUniqueId, e.getMessage());
                // 继续处理，不因为检查异常而跳过记录
            }

            // 记录有效，添加到列表
            validRecords.add(record);
        }

        result.setDuplicateCount(duplicateCount);
        return validRecords;
    }

    /**
     * 生成记录唯一标识（使用统一工具类）
     * <p>
     * 复用AccessRecordIdempotencyUtil，确保全局一致性
     * 注意：需要deviceId参数，从外部传入
     * </p>
     *
     * @param record 记录DTO
     * @param deviceId 设备ID（Long类型）
     * @return 记录唯一标识
     */
    private String generateRecordUniqueId(AccessRecordBatchUploadRequest.AccessRecordDTO record, Long deviceId) {
        // 如果请求中已提供唯一标识，直接使用
        if (record.getRecordUniqueId() != null && !record.getRecordUniqueId().trim().isEmpty()) {
            return record.getRecordUniqueId();
        }
        
        // 使用统一工具类生成（复用AccessRecordIdempotencyUtil）
        LocalDateTime accessTime = record.getAccessTime() != null ? 
                record.getAccessTime() : LocalDateTime.now();
        return AccessRecordIdempotencyUtil.generateRecordUniqueId(
                record.getUserId(), deviceId, accessTime);
    }

    /**
     * 生成记录唯一标识（降级方案，当deviceId无法转换为Long时使用）
     * <p>
     * 使用deviceId的hashCode作为临时方案
     * </p>
     */
    private String generateRecordUniqueIdFallback(
            AccessRecordBatchUploadRequest.AccessRecordDTO record, String deviceId) {
        LocalDateTime accessTime = record.getAccessTime() != null ? 
                record.getAccessTime() : LocalDateTime.now();
        // 使用deviceId的hashCode作为临时deviceId
        Long deviceIdLong = (long) deviceId.hashCode();
        return AccessRecordIdempotencyUtil.generateRecordUniqueId(
                record.getUserId(), deviceIdLong, accessTime);
    }

    /**
     * 转换为Entity列表
     */
    private List<AccessRecordEntity> convertToEntities(
            List<AccessRecordBatchUploadRequest.AccessRecordDTO> records,
            String deviceId) {
        
        return records.stream().map(record -> {
            AccessRecordEntity entity = new AccessRecordEntity();
            entity.setUserId(record.getUserId());
            // 注意：deviceId可能是String类型，需要转换为Long
            try {
                entity.setDeviceId(Long.parseLong(deviceId));
            } catch (NumberFormatException e) {
                log.warn("[批量上传] 设备ID格式错误，尝试查询设备: deviceId={}", deviceId);
                // 如果deviceId是String，需要通过DeviceDao查询转换为Long
                // 这里暂时使用hashCode作为临时方案，实际应该查询数据库
                entity.setDeviceId((long) deviceId.hashCode());
            }
            entity.setAreaId(record.getAreaId());
            entity.setAccessResult(record.getAccessResult());
            entity.setAccessTime(record.getAccessTime() != null ? record.getAccessTime() : LocalDateTime.now());
            entity.setAccessType(record.getAccessType() != null ? record.getAccessType() : "IN");
            entity.setVerifyMethod(record.getVerifyMethod());
            entity.setPhotoPath(record.getPhotoPath());
            entity.setRecordUniqueId(record.getRecordUniqueId());
            return entity;
        }).collect(Collectors.toList());
    }

    /**
     * 更新记录唯一标识缓存（使用统一工具类）
     */
    private void updateRecordUniqueIdCache(List<AccessRecordBatchUploadRequest.AccessRecordDTO> records) {
        for (AccessRecordBatchUploadRequest.AccessRecordDTO record : records) {
            if (record.getRecordUniqueId() != null) {
                AccessRecordIdempotencyUtil.updateRecordUniqueIdCache(record.getRecordUniqueId(), redisTemplate);
            }
        }
    }

    /**
     * 缓存批次结果
     */
    private void cacheBatchResult(String batchId, AccessRecordBatchUploadRequest.BatchUploadResult result) {
        try {
            String cacheKey = CACHE_KEY_BATCH_STATUS + batchId;
            redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_BATCH);
            log.debug("[批量上传] 批次结果已缓存: batchId={}", batchId);
        } catch (Exception e) {
            log.warn("[批量上传] 缓存批次结果失败: batchId={}, error={}", batchId, e.getMessage());
        }
    }

    /**
     * 获取缓存的批次结果
     */
    private AccessRecordBatchUploadRequest.BatchUploadResult getCachedBatchResult(String batchId) {
        try {
            String cacheKey = CACHE_KEY_BATCH_STATUS + batchId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof AccessRecordBatchUploadRequest.BatchUploadResult) {
                return (AccessRecordBatchUploadRequest.BatchUploadResult) cached;
            }
        } catch (Exception e) {
            log.warn("[批量上传] 获取缓存批次结果失败: batchId={}, error={}", batchId, e.getMessage());
        }
        return null;
    }
}

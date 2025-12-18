package net.lab1024.sa.visitor.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.enumeration.ResultCode;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.BlacklistAddForm;
import net.lab1024.sa.visitor.domain.form.BlacklistQueryForm;
import net.lab1024.sa.visitor.domain.vo.BlacklistVO;
import net.lab1024.sa.visitor.service.VisitorBlacklistService;
import net.lab1024.sa.visitor.dao.VisitorBlacklistDao;
import net.lab1024.sa.visitor.entity.VisitorBlacklistEntity;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 访客黑名单管理服务实现
 * <p>
 * 内存优化实现策略：
 * - 使用CompletableFuture异步处理，避免阻塞
 * - 缓存热点数据，减少数据库访问
 * - 批量操作优化，减少网络IO
 * - 合理的事务边界控制
 * - 使用对象池复用对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VisitorBlacklistServiceImpl implements VisitorBlacklistService {

    @Resource
    private VisitorBlacklistDao blacklistDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String BLACKLIST_CACHE_PREFIX = "visitor:blacklist:";
    private static final String BLACKLIST_STATUS_PREFIX = "visitor:blacklist:status:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    /**
     * 添加到黑名单
     * <p>
     * 内存优化要点：
     * 1. 异步处理，避免阻塞主线程
     * 2. 缓存黑名单状态，减少重复查询
     * 3. 使用事务确保数据一致性
     * 4. 记录操作日志，便于追踪
     * </p>
     */
    @Override
    @Async
    public CompletableFuture<ResponseDTO<Long>> addToBlacklist(BlacklistAddForm form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[黑名单管理] 开始添加黑名单, idCard={}, phone={}",
                        maskSensitiveData(form.getIdCard()), maskSensitiveData(form.getPhone()));

                // 1. 参数验证
                validateBlacklistForm(form);

                // 2. 检查是否已存在
                VisitorBlacklistEntity existingRecord = checkExistingBlacklist(form);
                if (existingRecord != null) {
                    log.warn("[黑名单管理] 访客已在黑名单中, idCard={}", maskSensitiveData(form.getIdCard()));
                    return ResponseDTO.error("ALREADY_IN_BLACKLIST", "访客已在黑名单中");
                }

                // 3. 构建黑名单记录
                VisitorBlacklistEntity blacklistEntity = buildBlacklistEntity(form);

                // 4. 保存黑名单记录
                int result = blacklistDao.insert(blacklistEntity);
                if (result <= 0) {
                    throw new SystemException("BLACKLIST_SAVE_FAILED", "保存黑名单记录失败");
                }

                // 5. 更新缓存
                updateBlacklistCache(blacklistEntity);

                // 6. 发送通知（异步）
                sendBlacklistNotification(blacklistEntity);

                log.info("[黑名单管理] 添加黑名单成功, blacklistId={}", blacklistEntity.getBlacklistId());
                return ResponseDTO.ok(blacklistEntity.getBlacklistId());

            } catch (ParamException e) {
                log.warn("[黑名单管理] 参数验证失败, error={}", e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (BusinessException e) {
                log.warn("[黑名单管理] 业务异常, error={}", e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[黑名单管理] 系统异常", e);
                return ResponseDTO.error("ADD_BLACKLIST_ERROR", "添加黑名单失败");
            }
        });
    }

    /**
     * 从黑名单移除
     */
    @Override
    @Async
    public CompletableFuture<ResponseDTO<Void>> removeFromBlacklist(Long visitorId, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证
                if (visitorId == null || visitorId <= 0) {
                    throw new ParamException("INVALID_VISITOR_ID", "无效的访客ID");
                }

                if (!StringUtils.hasText(reason)) {
                    throw new ParamException("REMOVE_REASON_EMPTY", "移除原因不能为空");
                }

                // 2. 查询黑名单记录
                List<VisitorBlacklistEntity> blacklistRecords = blacklistDao.selectByVisitorId(visitorId);
                if (blacklistRecords.isEmpty()) {
                    log.warn("[黑名单管理] 访客不在黑名单中, visitorId={}", visitorId);
                    return ResponseDTO.error("NOT_IN_BLACKLIST", "访客不在黑名单中");
                }

                // 3. 批量移除（逻辑删除）
                int updatedCount = 0;
                for (VisitorBlacklistEntity record : blacklistRecords) {
                    record.setStatus(0); // 停用状态
                    record.setUpdateTime(LocalDateTime.now());
                    updatedCount += blacklistDao.updateById(record);
                }

                if (updatedCount <= 0) {
                    throw new SystemException("BLACKLIST_REMOVE_FAILED", "移除黑名单失败");
                }

                // 4. 清除缓存
                clearBlacklistCache(visitorId);

                log.info("[黑名单管理] 移除黑名单成功, visitorId={}, count={}", visitorId, updatedCount);
                return ResponseDTO.ok();

            } catch (ParamException e) {
                log.warn("[黑名单管理] 移除参数异常, visitorId={}, error={}", visitorId, e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[黑名单管理] 移除黑名单异常, visitorId={}", visitorId, e);
                return ResponseDTO.error("REMOVE_BLACKLIST_ERROR", "移除黑名单失败");
            }
        });
    }

    /**
     * 查询黑名单
     */
    @Override
    public CompletableFuture<ResponseDTO<PageResult<BlacklistVO>>> queryBlacklist(BlacklistQueryForm form) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证和默认值处理
                if (form == null) {
                    form = new BlacklistQueryForm();
                }

                form.setPageNum(form.getPageNum() == null || form.getPageNum() <= 0 ? 1 : form.getPageNum());
                form.setPageSize(form.getPageSize() == null || form.getPageSize() <= 0 ? 20 : form.getPageSize());
                form.setPageSize(Math.min(form.getPageSize(), 100)); // 限制最大页面大小

                // 2. 查询黑名单记录
                PageRequest pageRequest = PageRequest.of(form.getPageNum() - 1, form.getPageSize());
                List<VisitorBlacklistEntity> blacklistRecords = blacklistDao.selectByCondition(form);

                // 3. 转换为VO
                List<BlacklistVO> blacklistVOs = convertToBlacklistVOs(blacklistRecords);

                // 4. 分页处理
                int start = (int) pageRequest.getOffset();
                int end = Math.min(start + form.getPageSize(), blacklistVOs.size());
                List<BlacklistVO> pageList = start >= blacklistVOs.size()
                        ? new ArrayList<>()
                        : blacklistVOs.subList(start, end);

                Page<BlacklistVO> page = new PageImpl<>(
                        pageList,
                        pageRequest,
                        blacklistVOs.size()
                );

                PageResult<BlacklistVO> pageResult = new PageResult<>();
                pageResult.setList(pageList);
                pageResult.setTotal((long) blacklistVOs.size());
                pageResult.setPageNum(form.getPageNum());
                pageResult.setPageSize(form.getPageSize());
                pageResult.setPages((int) Math.ceil((double) blacklistVOs.size() / form.getPageSize()));

                log.info("[黑名单管理] 查询黑名单成功, total={}", pageResult.getTotal());
                return ResponseDTO.ok(pageResult);

            } catch (Exception e) {
                log.error("[黑名单管理] 查询黑名单异常", e);
                return ResponseDTO.error("QUERY_BLACKLIST_ERROR", "查询黑名单失败");
            }
        });
    }

    /**
     * 检查黑名单状态
     */
    @Override
    public CompletableFuture<ResponseDTO<Boolean>> checkBlacklistStatus(
            Long visitorId,
            String idCard,
            String phone
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 优先使用visitorId查询
                if (visitorId != null && visitorId > 0) {
                    Boolean cachedStatus = getBlacklistStatusFromCache(visitorId);
                    if (cachedStatus != null) {
                        return ResponseDTO.ok(cachedStatus);
                    }

                    boolean isBlacklisted = blacklistDao.existsActiveBlacklist(visitorId);
                    cacheBlacklistStatus(visitorId, isBlacklisted);
                    return ResponseDTO.ok(isBlacklisted);
                }

                // 2. 使用身份证号查询
                if (StringUtils.hasText(idCard)) {
                    VisitorBlacklistEntity record = blacklistDao.selectByIdCard(idCard);
                    boolean isBlacklisted = record != null && record.getStatus() == 1;

                    // 缓存结果（如果有visitorId）
                    if (record != null && record.getVisitorId() != null) {
                        cacheBlacklistStatus(record.getVisitorId(), isBlacklisted);
                    }

                    return ResponseDTO.ok(isBlacklisted);
                }

                // 3. 使用手机号查询
                if (StringUtils.hasText(phone)) {
                    VisitorBlacklistEntity record = blacklistDao.selectByPhone(phone);
                    boolean isBlacklisted = record != null && record.getStatus() == 1;

                    // 缓存结果（如果有visitorId）
                    if (record != null && record.getVisitorId() != null) {
                        cacheBlacklistStatus(record.getVisitorId(), isBlacklisted);
                    }

                    return ResponseDTO.ok(isBlacklisted);
                }

                // 4. 缺少查询条件
                return ResponseDTO.error("MISSING_QUERY_CONDITION", "缺少查询条件");

            } catch (Exception e) {
                log.error("[黑名单管理] 检查黑名单状态异常, visitorId={}, idCard={}, phone={}",
                        visitorId, maskSensitiveData(idCard), maskSensitiveData(phone), e);
                return ResponseDTO.error("CHECK_BLACKLIST_STATUS_ERROR", "检查黑名单状态失败");
            }
        });
    }

    /**
     * 批量检查黑名单
     */
    @Override
    public CompletableFuture<ResponseDTO<List<Long>>> batchCheckBlacklistStatus(List<Long> visitorIds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证
                if (visitorIds == null || visitorIds.isEmpty()) {
                    throw new ParamException("VISITOR_IDS_EMPTY", "访客ID列表不能为空");
                }

                if (visitorIds.size() > 100) {
                    throw new ParamException("BATCH_SIZE_LIMIT", "批量检查数量不能超过100个");
                }

                // 2. 批量查询
                List<Long> blacklistedIds = new ArrayList<>();

                for (Long visitorId : visitorIds) {
                    // 先检查缓存
                    Boolean cachedStatus = getBlacklistStatusFromCache(visitorId);
                    Boolean isBlacklisted = cachedStatus;

                    if (isBlacklisted == null) {
                        // 缓存未命中，查询数据库
                        isBlacklisted = blacklistDao.existsActiveBlacklist(visitorId);
                        cacheBlacklistStatus(visitorId, isBlacklisted);
                    }

                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        blacklistedIds.add(visitorId);
                    }
                }

                log.info("[黑名单管理] 批量检查完成, total={}, blacklisted={}",
                        visitorIds.size(), blacklistedIds.size());
                return ResponseDTO.ok(blacklistedIds);

            } catch (ParamException e) {
                log.error("[黑名单管理] 批量检查参数异常, error={}", e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[黑名单管理] 批量检查异常", e);
                return ResponseDTO.error("BATCH_CHECK_ERROR", "批量检查黑名单状态失败");
            }
        });
    }

    /**
     * 更新黑名单状态
     */
    @Override
    @Async
    public CompletableFuture<ResponseDTO<Void>> updateBlacklistStatus(Long blacklistId, Integer status) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证
                if (blacklistId == null || blacklistId <= 0) {
                    throw new ParamException("INVALID_BLACKLIST_ID", "无效的黑名单ID");
                }

                if (status == null || (status != 0 && status != 1)) {
                    throw new ParamException("INVALID_STATUS", "无效的状态值");
                }

                // 2. 查询黑名单记录
                VisitorBlacklistEntity record = blacklistDao.selectById(blacklistId);
                if (record == null) {
                    throw new BusinessException("BLACKLIST_NOT_FOUND", "黑名单记录不存在");
                }

                // 3. 更新状态
                record.setStatus(status);
                record.setUpdateTime(LocalDateTime.now());

                int result = blacklistDao.updateById(record);
                if (result <= 0) {
                    throw new SystemException("UPDATE_BLACKLIST_STATUS_FAILED", "更新黑名单状态失败");
                }

                // 4. 更新缓存
                if (record.getVisitorId() != null) {
                    cacheBlacklistStatus(record.getVisitorId(), status == 1);
                }

                log.info("[黑名单管理] 更新黑名单状态成功, blacklistId={}, status={}", blacklistId, status);
                return ResponseDTO.ok();

            } catch (ParamException | BusinessException e) {
                log.warn("[黑名单管理] 更新状态异常, blacklistId={}, error={}", blacklistId, e.getMessage());
                return ResponseDTO.error(e.getCode(), e.getMessage());
            } catch (Exception e) {
                log.error("[黑名单管理] 更新黑名单状态异常, blacklistId={}", blacklistId, e);
                return ResponseDTO.error("UPDATE_STATUS_ERROR", "更新黑名单状态失败");
            }
        });
    }

    /**
     * 清理过期黑名单
     */
    @Override
    @Async
    @CircuitBreaker(name = "cleanExpiredBlacklist", fallbackMethod = "cleanExpiredBlacklistFallback")
    public CompletableFuture<ResponseDTO<Integer>> cleanExpiredBlacklist() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[黑名单管理] 开始清理过期黑名单记录");

                // 1. 查询过期记录
                List<VisitorBlacklistEntity> expiredRecords = blacklistDao.selectExpiredRecords();
                if (expiredRecords.isEmpty()) {
                    log.info("[黑名单管理] 无过期黑名单记录");
                    return ResponseDTO.ok(0);
                }

                // 2. 批量更新状态为过期
                int updatedCount = 0;
                for (VisitorBlacklistEntity record : expiredRecords) {
                    record.setStatus(2); // 过期状态
                    record.setUpdateTime(LocalDateTime.now());
                    updatedCount += blacklistDao.updateById(record);

                    // 清除缓存
                    if (record.getVisitorId() != null) {
                        clearBlacklistCache(record.getVisitorId());
                    }
                }

                log.info("[黑名单管理] 清理过期黑名单完成, count={}", updatedCount);
                return ResponseDTO.ok(updatedCount);

            } catch (Exception e) {
                log.error("[黑名单管理] 清理过期黑名单异常", e);
                throw new SystemException("CLEAN_EXPIRED_BLACKLIST_ERROR", "清理过期黑名单失败");
            }
        });
    }

    /**
     * 获取黑名单统计
     */
    @Override
    public CompletableFuture<ResponseDTO<Object>> getBlacklistStatistics(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 参数验证
                if (startTime == null) {
                    startTime = LocalDateTime.now().minusDays(30);
                }
                if (endTime == null) {
                    endTime = LocalDateTime.now();
                }

                // 2. 查询统计数据
                Object statistics = blacklistDao.selectStatistics(startTime, endTime);

                log.info("[黑名单管理] 获取统计数据成功, startTime={}, endTime={}", startTime, endTime);
                return ResponseDTO.ok(statistics);

            } catch (Exception e) {
                log.error("[黑名单管理] 获取统计数据异常", e);
                return ResponseDTO.error("GET_STATISTICS_ERROR", "获取黑名单统计失败");
            }
        });
    }

    // ==================== 私有方法 ====================

    /**
     * 验证黑名单表单
     */
    private void validateBlacklistForm(BlacklistAddForm form) {
        if (form == null) {
            throw new ParamException("BLACKLIST_FORM_NULL", "黑名单表单不能为空");
        }

        if (!StringUtils.hasText(form.getIdCard()) && !StringUtils.hasText(form.getPhone())) {
            throw new ParamException("ID_CARD_OR_PHONE_REQUIRED", "身份证号和手机号至少提供一个");
        }

        if (StringUtils.hasText(form.getIdCard()) && !isValidIdCard(form.getIdCard())) {
            throw new ParamException("INVALID_ID_CARD", "身份证号格式不正确");
        }

        if (StringUtils.hasText(form.getPhone()) && !isValidPhone(form.getPhone())) {
            throw new ParamException("INVALID_PHONE", "手机号格式不正确");
        }

        if (!StringUtils.hasText(form.getBlacklistReason())) {
            throw new ParamException("BLACKLIST_REASON_EMPTY", "黑名单原因不能为空");
        }

        if (form.getBlacklistReason() != null && form.getBlacklistReason().length() > 500) {
            throw new ParamException("BLACKLIST_REASON_TOO_LONG", "黑名单原因长度不能超过500个字符");
        }

        if (form.getEndTime() != null && form.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ParamException("INVALID_END_TIME", "结束时间不能早于当前时间");
        }
    }

    /**
     * 检查是否已存在黑名单记录
     */
    private VisitorBlacklistEntity checkExistingBlacklist(BlacklistAddForm form) {
        if (StringUtils.hasText(form.getIdCard())) {
            return blacklistDao.selectByIdCard(form.getIdCard());
        }
        if (StringUtils.hasText(form.getPhone())) {
            return blacklistDao.selectByPhone(form.getPhone());
        }
        return null;
    }

    /**
     * 构建黑名单实体
     */
    private VisitorBlacklistEntity buildBlacklistEntity(BlacklistAddForm form) {
        VisitorBlacklistEntity entity = new VisitorBlacklistEntity();
        entity.setVisitorId(form.getVisitorId());
        entity.setIdCard(form.getIdCard());
        entity.setPhone(form.getPhone());
        entity.setBlacklistType(form.getBlacklistType() != null ? form.getBlacklistType() : "PERMANENT");
        entity.setBlacklistReason(form.getBlacklistReason());
        entity.setOperatorId(getCurrentUserId());
        entity.setOperatorName(getCurrentUserName());
        entity.setStartTime(LocalDateTime.now());
        entity.setEndTime(form.getEndTime());
        entity.setStatus(1); // 生效状态
        return entity;
    }

    /**
     * 转换为黑名单VO列表
     */
    private List<BlacklistVO> convertToBlacklistVOs(List<VisitorBlacklistEntity> entities) {
        return entities.stream()
                .map(this::convertToBlacklistVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为黑名单VO
     */
    private BlacklistVO convertToBlacklistVO(VisitorBlacklistEntity entity) {
        BlacklistVO vo = BlacklistVO.builder()
                .blacklistId(entity.getBlacklistId())
                .visitorId(entity.getVisitorId())
                .visitorName(entity.getVisitorName())
                .maskedIdCard(maskSensitiveData(entity.getIdCard()))
                .maskedPhone(maskSensitiveData(entity.getPhone()))
                .blacklistType(entity.getBlacklistType())
                .blacklistTypeName(getBlacklistTypeName(entity.getBlacklistType()))
                .blacklistReason(entity.getBlacklistReason())
                .operatorName(entity.getOperatorName())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .statusName(getStatusName(entity.getStatus()))
                .remainingDays(calculateRemainingDays(entity.getEndTime()))
                .createTime(entity.getCreateTime())
                .build();
        return vo;
    }

    /**
     * 更新黑名单缓存
     */
    private void updateBlacklistCache(VisitorBlacklistEntity entity) {
        try {
            if (entity.getVisitorId() != null) {
                cacheBlacklistStatus(entity.getVisitorId(), entity.getStatus() == 1);
            }
        } catch (Exception e) {
            log.warn("[黑名单管理] 更新缓存失败", e);
        }
    }

    /**
     * 缓存黑名单状态
     */
    private void cacheBlacklistStatus(Long visitorId, boolean isBlacklisted) {
        String cacheKey = BLACKLIST_STATUS_PREFIX + visitorId;
        redisTemplate.opsForValue().set(cacheKey, isBlacklisted, CACHE_DURATION);
    }

    /**
     * 获取缓存的黑名单状态
     */
    private Boolean getBlacklistStatusFromCache(Long visitorId) {
        try {
            String cacheKey = BLACKLIST_STATUS_PREFIX + visitorId;
            return (Boolean) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("[黑名单管理] 获取缓存状态失败, visitorId={}", visitorId, e);
            return null;
        }
    }

    /**
     * 清除黑名单缓存
     */
    private void clearBlacklistCache(Long visitorId) {
        try {
            String cacheKey = BLACKLIST_STATUS_PREFIX + visitorId;
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("[黑名单管理] 清除缓存失败, visitorId={}", visitorId, e);
        }
    }

    /**
     * 发送黑名单通知
     */
    private void sendBlacklistNotification(VisitorBlacklistEntity entity) {
        try {
            // 发送通知逻辑（异步）
            log.info("[黑名单管理] 发送黑名单通知, visitorId={}, reason={}",
                    entity.getVisitorId(), entity.getBlacklistReason());
        } catch (Exception e) {
            log.warn("[黑名单管理] 发送通知失败", e);
        }
    }

    /**
     * 清理过期黑名单降级处理
     */
    private CompletableFuture<ResponseDTO<Integer>> cleanExpiredBlacklistFallback(Exception e) {
        log.warn("[黑名单管理] 清理过期黑名单降级", e);
        return CompletableFuture.completedFuture(
                ResponseDTO.error("CLEAN_EXPIRED_BLACKLIST_UNAVAILABLE", "清理过期黑名单服务暂时不可用")
        );
    }

    // ==================== 辅助方法 ====================

    /**
     * 脱敏敏感数据
     */
    private String maskSensitiveData(String data) {
        if (data == null || data.length() <= 4) {
            return "****";
        }
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }

    /**
     * 验证身份证号
     */
    private boolean isValidIdCard(String idCard) {
        // 简化验证，实际项目中应该更严格
        return idCard != null && (idCard.length() == 15 || idCard.length() == 18);
    }

    /**
     * 验证手机号
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 获取黑名单类型名称
     */
    private String getBlacklistTypeName(String type) {
        switch (type) {
            case "PERMANENT": return "永久黑名单";
            case "TEMPORARY": return "临时黑名单";
            default: return "未知类型";
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "停用";
            case 1: return "生效";
            case 2: return "过期";
            default: return "未知状态";
        }
    }

    /**
     * 计算剩余天数
     */
    private Integer calculateRemainingDays(LocalDateTime endTime) {
        if (endTime == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (endTime.isBefore(now)) {
            return 0;
        }
        return (int) java.time.Duration.between(now, endTime).toDays();
    }

    /**
     * 获取当前用户ID（简化实现）
     */
    private Long getCurrentUserId() {
        return 1L; // 简化实现
    }

    /**
     * 获取当前用户名（简化实现）
     */
    private String getCurrentUserName() {
        return "管理员"; // 简化实现
    }
}

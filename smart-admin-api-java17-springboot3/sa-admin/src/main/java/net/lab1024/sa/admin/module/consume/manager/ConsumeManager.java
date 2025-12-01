package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import net.lab1024.sa.base.common.cache.BaseCacheManager;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionModeEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 消费管理器
 * 严格遵循repowiki四层架构规范：Manager层负责复杂业务逻辑封装和缓存管理
 * 封装消费业务的核心逻辑，包括金额计算、账户扣款、消费记录管理等
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
@Slf4j
@Component
public class ConsumeManager {

    private static final String CACHE_PREFIX = "consume:";
    private static final int CACHE_TTL_MINUTES = 15;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private AccountManager accountManager;

    @Resource
    private ConsumptionModeEngine consumptionModeEngine;

    @Resource
    private BaseCacheManager cacheManager;

    /**
     * 执行消费操作
     * 这是一个复杂业务操作，涉及账户验证、余额检查、消费记录创建等多个步骤
     *
     * @param consumeRequest 消费请求
     * @return 消费结果
     */
    @Transactional(rollbackFor = Throwable.class)
    public ConsumeResultDTO executeConsume(ConsumeRequestDTO consumeRequest) {
        log.info("开始执行消费操作: accountId={}, amount={}", consumeRequest.getAccountId(), consumeRequest.getAmount());

        try {
            // 1. 验证消费请求参数
            if (consumeRequest == null || consumeRequest.getAccountId() == null || consumeRequest.getAmount() == null) {
                throw new SmartException("消费请求参数不能为空");
            }

            if (consumeRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SmartException("消费金额必须大于0");
            }

            // 2. 获取账户信息（从缓存或数据库）
            AccountEntity account = getAccountWithCache(consumeRequest.getAccountId());
            if (account == null) {
                throw new SmartException("账户不存在");
            }

            // 3. 验证账户状态
            if (!accountManager.validateAccount(account).isValid()) {
                throw new SmartException("账户状态异常，无法消费");
            }

            // 4. 检查账户余额是否充足
            if (!accountManager.checkBalanceSufficient(account.getAccountId(), consumeRequest.getAmount())) {
                throw new SmartException("账户余额不足");
            }

            // 5. 计算消费金额（考虑折扣、优惠等）
            BigDecimal actualAmount = calculateConsumeAmount(consumeRequest, account);

            // 6. 创建消费记录
            ConsumeRecordEntity consumeRecord = createConsumeRecord(consumeRequest, account, actualAmount);

            // 7. 保存消费记录
            consumeRecordDao.insert(consumeRecord);

            // 8. 更新账户余额（这里应该调用账户服务进行扣款）
            // updateAccountBalance(account.getAccountId(), actualAmount);

            // 9. 清除相关缓存
            clearConsumeCache(account.getAccountId());

            log.info("消费操作成功: accountId={}, amount={}, recordId={}",
                consumeRequest.getAccountId(), actualAmount, consumeRecord.getConsumeId());

            return ConsumeResultDTO.success(consumeRecord.getConsumeId(), actualAmount, "消费成功");

        } catch (Exception e) {
            log.error("消费操作失败: accountId={}, amount={}",
                consumeRequest.getAccountId(), consumeRequest.getAmount(), e);
            throw new SmartException("消费操作失败: " + e.getMessage());
        }
    }

    /**
     * 批量消费操作
     *
     * @param consumeRequests 消费请求列表
     * @return 消费结果列表
     */
    @Transactional(rollbackFor = Throwable.class)
    public List<ConsumeResultDTO> batchConsume(List<ConsumeRequestDTO> consumeRequests) {
        log.info("开始批量消费操作: 数量={}", consumeRequests.size());

        try {
            if (consumeRequests == null || consumeRequests.isEmpty()) {
                throw new SmartException("批量消费请求不能为空");
            }

            // 验证批量消费数量限制
            if (consumeRequests.size() > 100) {
                throw new SmartException("批量消费数量不能超过100条");
            }

            // 逐个执行消费操作
            return consumeRequests.stream()
                .map(this::executeConsume)
                .toList();

        } catch (Exception e) {
            log.error("批量消费操作失败: 数量={}", consumeRequests.size(), e);
            throw new SmartException("批量消费操作失败: " + e.getMessage());
        }
    }

    /**
     * 计算消费金额
     * 考虑折扣、优惠、会员等级等因素
     *
     * @param consumeRequest 消费请求
     * @param account 账户信息
     * @return 实际消费金额
     */
    private BigDecimal calculateConsumeAmount(ConsumeRequestDTO consumeRequest, AccountEntity account) {
        try {
            // 使用消费模式引擎计算金额
            BigDecimal originalAmount = consumeRequest.getAmount();
            BigDecimal discountedAmount = consumptionModeEngine.calculateAmount(consumeRequest, account);

            log.debug("消费金额计算: 原始金额={}, 折扣后金额={}", originalAmount, discountedAmount);

            return discountedAmount;

        } catch (Exception e) {
            log.error("消费金额计算失败", e);
            // 计算失败时使用原金额
            return consumeRequest.getAmount();
        }
    }

    /**
     * 创建消费记录
     *
     * @param consumeRequest 消费请求
     * @param account 账户信息
     * @param actualAmount 实际消费金额
     * @return 消费记录实体
     */
    private ConsumeRecordEntity createConsumeRecord(ConsumeRequestDTO consumeRequest, AccountEntity account, BigDecimal actualAmount) {
        ConsumeRecordEntity consumeRecord = new ConsumeRecordEntity();

        consumeRecord.setConsumeId(generateConsumeId());
        consumeRecord.setAccountId(account.getAccountId());
        consumeRecord.setAccountName(account.getAccountName());
        consumeRecord.setOriginalAmount(consumeRequest.getAmount());
        consumeRecord.setActualAmount(actualAmount);
        consumeRecord.setDiscountAmount(consumeRequest.getAmount().subtract(actualAmount));
        consumeRecord.setConsumeMode(consumeRequest.getConsumeMode());
        consumeRecord.setConsumeType(consumeRequest.getConsumeType());
        consumeRecord.setMerchantId(consumeRequest.getMerchantId());
        consumeRecord.setMerchantName(consumeRequest.getMerchantName());
        consumeRecord.setDescription(consumeRequest.getDescription());
        consumeRecord.setConsumeTime(LocalDateTime.now());
        consumeRecord.setCreateTime(LocalDateTime.now());
        consumeRecord.setCreateUserId(consumeRequest.getCreateUserId());
        consumeRecord.setUpdateUserId(consumeRequest.getCreateUserId());

        return consumeRecord;
    }

    /**
     * 获取账户信息（带缓存）
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    private AccountEntity getAccountWithCache(Long accountId) {
        try {
            String cacheKey = CACHE_PREFIX + "account:" + accountId;
            AccountEntity account = cacheManager.get(cacheKey, AccountEntity.class);

            if (account == null) {
                // 缓存未命中，从数据库获取
                account = accountManager.getAccountById(accountId);
                if (account != null) {
                    // 写入缓存
                    cacheManager.put(cacheKey, account, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                }
            }

            return account;

        } catch (Exception e) {
            log.error("获取账户信息失败: accountId={}", accountId, e);
            return null;
        }
    }

    /**
     * 清除消费相关缓存
     *
     * @param accountId 账户ID
     */
    private void clearConsumeCache(Long accountId) {
        try {
            if (accountId == null) {
                return;
            }

            // 清除账户缓存
            accountManager.clearAccountCache(accountId);

            // 清除消费记录缓存
            cacheManager.evict(CACHE_PREFIX + "account:" + accountId);
            cacheManager.evict(CACHE_PREFIX + "records:" + accountId);

            log.debug("清除消费缓存: accountId={}", accountId);

        } catch (Exception e) {
            log.error("清除消费缓存失败: accountId={}", accountId, e);
        }
    }

    /**
     * 生成消费记录ID
     *
     * @return 消费记录ID
     */
    private String generateConsumeId() {
        // 简单的ID生成策略，实际项目中可以使用分布式ID生成器
        return "C" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 获取消费记录缓存Key
     *
     * @param accountId 账户ID
     * @param params 查询参数
     * @return 缓存Key
     */
    public String getConsumeRecordCacheKey(Long accountId, String params) {
        String key = "records:" + accountId;
        if (SmartStringUtil.isNotEmpty(params)) {
            key += ":" + SmartStringUtil.md5(params);
        }
        return CACHE_PREFIX + key;
    }

    /**
     * 验证消费请求
     *
     * @param consumeRequest 消费请求
     * @throws SmartException 验证失败抛出异常
     */
    public void validateConsumeRequest(ConsumeRequestDTO consumeRequest) {
        if (consumeRequest == null) {
            throw new SmartException("消费请求不能为空");
        }

        if (consumeRequest.getAccountId() == null) {
            throw new SmartException("账户ID不能为空");
        }

        if (consumeRequest.getAmount() == null || consumeRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SmartException("消费金额必须大于0");
        }

        if (SmartStringUtil.isEmpty(consumeRequest.getConsumeMode())) {
            throw new SmartException("消费模式不能为空");
        }

        if (SmartStringUtil.isEmpty(consumeRequest.getConsumeType())) {
            throw new SmartException("消费类型不能为空");
        }
    }
}
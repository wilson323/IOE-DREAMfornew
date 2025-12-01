package net.lab1024.sa.admin.module.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 消费缓存管理器
 *
 * 基于BaseCacheManager实现多级缓存架构
 * - L1: Caffeine本地缓存(5分钟过期)
 * - L2: Redis分布式缓存(30分钟过期)
 *
 * 核心职责:
 * - 员工消费记录缓存管理
 * - 消费统计数据缓存
 * - 账户余额缓存
 * - 消费限额管理缓存
 *
 * 缓存Key规范:
 * - consume:record:{employeeId}:{date} - 员工消费记录
 * - consume:balance:{employeeId} - 账户余额
 * - consume:stats:{employeeId}:{period} - 消费统计
 * - consume:limit:{employeeId}:{type} - 消费限额
 * - consume:daily:total:{date} - 当日总消费
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class ConsumeCacheManager extends BaseCacheManager {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    // 账户余额缓存 (内存缓存，用于实时查询)
    private final Map<Long, BigDecimal> accountBalanceCache = new ConcurrentHashMap<>();

    // 消费限额配置缓存
    private final Map<Long, Map<String, Object>> consumeLimits = new ConcurrentHashMap<>();

    @Override
    protected String getCachePrefix() {
        return "consume:";
    }

    /**
     * 获取员工账户余额
     *
     * @param employeeId 员工ID
     * @return 账户余额
     */
    public BigDecimal getAccountBalance(Long employeeId) {
        // 优先从内存缓存获取
        return accountBalanceCache.computeIfAbsent(employeeId, id -> {
            // 从数据库查询最新余额
            LambdaQueryWrapper<ConsumeRecordEntity> latestWrapper = new LambdaQueryWrapper<>();
            latestWrapper.eq(ConsumeRecordEntity::getEmployeeId, employeeId)
                    .orderByDesc(ConsumeRecordEntity::getCreateTime)
                    .last("limit 1");
            ConsumeRecordEntity latestRecord = consumeRecordDao.selectOne(latestWrapper);

            return latestRecord != null ? latestRecord.getBalanceAfter() : BigDecimal.ZERO;
        });
    }

    /**
     * 更新账户余额缓存
     *
     * @param employeeId 员工ID
     * @param balance    新余额
     */
    public void updateAccountBalance(Long employeeId, BigDecimal balance) {
        accountBalanceCache.put(employeeId, balance);

        // 同时缓存到Redis
        String cacheKey = buildCacheKey(employeeId, ":balance");
        setCache(cacheKey, balance);
    }

    /**
     * 获取员工当日消费记录
     *
     * @param employeeId 员工ID
     * @param date       日期 yyyy-MM-dd
     * @return 消费记录列表
     */
    @SuppressWarnings("unchecked")
    public List<ConsumeRecordEntity> getDailyConsumeRecords(Long employeeId, String date) {
        String cacheKey = buildCacheKey(employeeId, ":record:" + date);
        return getCache(cacheKey, () -> {
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getEmployeeId, employeeId)
                    .eq(ConsumeRecordEntity::getConsumeDate, date)
                    .orderByDesc(ConsumeRecordEntity::getCreateTime);
            return consumeRecordDao.selectList(wrapper);
        });
    }

    /**
     * 缓存消费记录
     *
     * @param record 消费记录
     */
    public void cacheConsumeRecord(ConsumeRecordEntity record) {
        if (record != null && record.getEmployeeId() != null && record.getConsumeDate() != null) {
            String cacheKey = buildCacheKey(record.getEmployeeId(), ":record:" + record.getConsumeDate());
            setCache(cacheKey, record);

            // 更新账户余额缓存
            if (record.getBalanceAfter() != null) {
                updateAccountBalance(record.getEmployeeId(), record.getBalanceAfter());
            }

            // 清除当日统计缓存
            String dateStr = record.getConsumeDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            clearDailyStatsCache(record.getEmployeeId(), dateStr);
        }
    }

    /**
     * 获取员工当日消费总额
     *
     * @param employeeId 员工ID
     * @param date       日期 yyyy-MM-dd
     * @return 消费总额
     */
    public BigDecimal getDailyTotalAmount(Long employeeId, String date) {
        String cacheKey = buildCacheKey(employeeId, ":total:" + date);
        BigDecimal cached = getCache(cacheKey, null);
        if (cached != null) {
            return cached;
        }

        // 计算当日消费总额
        List<ConsumeRecordEntity> records = getDailyConsumeRecords(employeeId, date);
        BigDecimal total = records.stream()
                .filter(r -> r.getConsumeAmount() != null)
                .map(ConsumeRecordEntity::getConsumeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 缓存结果
        setCache(cacheKey, total);
        return total;
    }

    /**
     * 获取员工月度消费统计
     *
     * @param employeeId 员工ID
     * @param yearMonth  年月 yyyy-MM
     * @return 消费统计
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMonthlyStats(Long employeeId, String yearMonth) {
        String cacheKey = buildCacheKey(employeeId, ":stats:monthly:" + yearMonth);
        return getCache(cacheKey, () -> {
            // 查询数据库获取月度消费记录
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getEmployeeId, employeeId)
                    .likeRight(ConsumeRecordEntity::getConsumeDate, yearMonth)
                    .orderByDesc(ConsumeRecordEntity::getCreateTime);
            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            return calculateMonthlyStats(records);
        });
    }

    /**
     * 检查消费限额
     *
     * @param employeeId    员工ID
     * @param consumeAmount 消费金额
     * @return 检查结果 {allow: boolean, reason: string}
     */
    public Map<String, Object> checkConsumeLimit(Long employeeId, BigDecimal consumeAmount) {
        Map<String, Object> result = new ConcurrentHashMap<>();

        try {
            // 获取当日消费总额
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            BigDecimal dailyTotal = getDailyTotalAmount(employeeId, today);

            // 获取消费限额配置
            Map<String, Object> limits = getConsumeLimits(employeeId);

            BigDecimal dailyLimit = new BigDecimal(limits.getOrDefault("dailyLimit", "100").toString());
            BigDecimal monthlyLimit = new BigDecimal(limits.getOrDefault("monthlyLimit", "3000").toString());

            // 检查日限额
            if (dailyTotal.add(consumeAmount).compareTo(dailyLimit) > 0) {
                result.put("allow", false);
                result.put("reason", "超过每日消费限额: " + dailyLimit + "元");
                return result;
            }

            // 检查月限额
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Map<String, Object> monthlyStats = getMonthlyStats(employeeId, currentMonth);
            BigDecimal monthlyTotal = new BigDecimal(monthlyStats.getOrDefault("totalAmount", "0").toString());

            if (monthlyTotal.add(consumeAmount).compareTo(monthlyLimit) > 0) {
                result.put("allow", false);
                result.put("reason", "超过每月消费限额: " + monthlyLimit + "元");
                return result;
            }

            // 检查账户余额
            BigDecimal balance = getAccountBalance(employeeId);
            if (balance.compareTo(consumeAmount) < 0) {
                result.put("allow", false);
                result.put("reason", "账户余额不足: 余额" + balance + "元，需消费" + consumeAmount + "元");
                return result;
            }

            result.put("allow", true);
            result.put("reason", "可以消费");

        } catch (Exception e) {
            log.error("检查消费限额异常: employeeId={}", employeeId, e);
            result.put("allow", false);
            result.put("reason", "系统异常，请稍后重试");
        }

        return result;
    }

    /**
     * 获取消费限额配置
     *
     * @param employeeId 员工ID
     * @return 限额配置
     */
    public Map<String, Object> getConsumeLimits(Long employeeId) {
        return consumeLimits.computeIfAbsent(employeeId, id -> {
            Map<String, Object> limits = new ConcurrentHashMap<>();
            limits.put("dailyLimit", 100); // 每日限额100元
            limits.put("monthlyLimit", 3000); // 每月限额3000元
            limits.put("singleLimit", 50); // 单笔限额50元
            limits.put("dailyTimes", 10); // 每日次数限制10次
            return limits;
        });
    }

    /**
     * 更新消费限额配置
     *
     * @param employeeId 员工ID
     * @param limits     限额配置
     */
    public void updateConsumeLimits(Long employeeId, Map<String, Object> limits) {
        consumeLimits.put(employeeId, limits);

        // 清除相关缓存
        clearEmployeeConsumeCache(employeeId);
    }

    /**
     * 计算月度消费统计
     *
     * @param records 消费记录列表
     * @return 统计信息
     */
    private Map<String, Object> calculateMonthlyStats(List<ConsumeRecordEntity> records) {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        int totalTimes = records.size();
        BigDecimal totalAmount = records.stream()
                .filter(r -> r.getConsumeAmount() != null)
                .map(ConsumeRecordEntity::getConsumeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算平均消费金额
        BigDecimal avgAmount = totalTimes > 0
                ? totalAmount.divide(new BigDecimal(totalTimes), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        // 统计消费类型分布
        Map<String, Integer> consumeTypeStats = new ConcurrentHashMap<>();
        records.forEach(record -> {
            String type = record.getConsumeType();
            consumeTypeStats.merge(type, 1, Integer::sum);
        });

        stats.put("totalTimes", totalTimes);
        stats.put("totalAmount", totalAmount);
        stats.put("avgAmount", avgAmount);
        stats.put("consumeTypeStats", consumeTypeStats);

        return stats;
    }

    /**
     * 清除员工消费相关缓存
     *
     * @param employeeId 员工ID
     */
    public void clearEmployeeConsumeCache(Long employeeId) {
        // 清除记录缓存
        String pattern = buildCacheKey(employeeId, ":record:*");
        removeCacheByPattern(pattern);

        // 清除统计缓存
        pattern = buildCacheKey(employeeId, ":stats:*");
        removeCacheByPattern(pattern);

        // 清除总额缓存
        pattern = buildCacheKey(employeeId, ":total:*");
        removeCacheByPattern(pattern);

        // 清除余额缓存
        String balanceKey = buildCacheKey(employeeId, ":balance");
        removeCache(balanceKey);

        // 清除内存余额缓存
        accountBalanceCache.remove(employeeId);
    }

    /**
     * 清除当日统计缓存
     *
     * @param employeeId 员工ID
     * @param date       日期
     */
    public void clearDailyStatsCache(Long employeeId, String date) {
        String cacheKey = buildCacheKey(employeeId, ":total:" + date);
        removeCache(cacheKey);

        // 清除当日记录缓存
        String recordKey = buildCacheKey(employeeId, ":record:" + date);
        removeCache(recordKey);
    }

    /**
     * 清除日期相关缓存
     *
     * @param date 日期
     */
    public void clearDateCache(String date) {
        String pattern = buildCacheKey("*", ":*:" + date);
        removeCacheByPattern(pattern);

        // 清除当日总额缓存
        pattern = buildCacheKey("*", ":total:" + date);
        removeCacheByPattern(pattern);
    }

    /**
     * 获取消费统计概览
     *
     * @param date 日期
     * @return 统计概览
     */
    public Map<String, Object> getConsumeOverview(String date) {
        Map<String, Object> overview = new ConcurrentHashMap<>();

        try {
            // 当日总消费次数
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConsumeRecordEntity> countWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            countWrapper.eq(ConsumeRecordEntity::getConsumeDate, date);
            long totalCount = consumeRecordDao.selectCount(countWrapper);
            overview.put("totalCount", totalCount);

            // 当日总消费金额
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConsumeRecordEntity> listWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            listWrapper.eq(ConsumeRecordEntity::getConsumeDate, date);
            List<ConsumeRecordEntity> todayRecords = consumeRecordDao.selectList(listWrapper);
            BigDecimal totalAmount = todayRecords.stream()
                    .filter(r -> r.getConsumeAmount() != null)
                    .map(ConsumeRecordEntity::getConsumeAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            overview.put("totalAmount", totalAmount);

            // 平均消费金额
            BigDecimal avgAmount = totalCount > 0
                    ? totalAmount.divide(new BigDecimal(totalCount), 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;
            overview.put("avgAmount", avgAmount);

        } catch (Exception e) {
            log.error("获取消费统计概览异常: date={}", date, e);
            overview.put("error", "获取统计数据失败");
        }

        return overview;
    }
}

package net.lab1024.sa.consume.service.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.service.ConsumeCacheService;

/**
 * 报表分析服务
 * 负责高级数据分析和统计
 *
 * @author SmartAdmin Team
 * @date 2025/01/30
 */
@Slf4j
@Service
public class ReportAnalysisService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ConsumeCacheService consumeCacheService;

    // 缓存前缀和TTL
    private static final String REPORT_CACHE_PREFIX = "report:";
    private static final int REPORT_CACHE_TTL = 300; // 5分钟
    private static final int DASHBOARD_CACHE_TTL = 60; // 1分钟

    /**
     * 获取消费趋势
     *
     * @param timeDimension 时间维度（DAY/MONTH/YEAR/HOUR）
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param trendType     趋势类型（AMOUNT/COUNT）
     * @param deviceId      设备ID
     * @param consumeMode   消费模式
     * @return 趋势数据列表
     */
    public List<Map<String, Object>> getConsumeTrend(String timeDimension, LocalDateTime startTime,
            LocalDateTime endTime, String trendType, Long deviceId, String consumeMode) {
        log.info("获取消费趋势, timeDimension={}, startTime={}, endTime={}, trendType={}, deviceId={}, consumeMode={}",
                timeDimension, startTime, endTime, trendType, deviceId, consumeMode);

        try {
            // 0. 构建缓存键
            String cacheKey = REPORT_CACHE_PREFIX + "trend:" + timeDimension + ":" +
                    (startTime != null ? startTime.toString() : "null") + ":" +
                    (endTime != null ? endTime.toString() : "null") + ":" +
                    (trendType != null ? trendType : "null") + ":" +
                    (deviceId != null ? deviceId : "null") + ":" +
                    (consumeMode != null ? consumeMode : "null");

            // 0.1. 检查缓存
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cachedResult = (List<Map<String, Object>>) consumeCacheService
                    .getCachedValue(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取消费趋势数据, cacheKey={}", cacheKey);
                return cachedResult;
            }

            // 1. 构建查询条件
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS");

            if (startTime != null) {
                wrapper.ge(ConsumeRecordEntity::getPayTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(ConsumeRecordEntity::getPayTime, endTime);
            }
            if (deviceId != null) {
                wrapper.eq(ConsumeRecordEntity::getDeviceId, deviceId);
            }
            if (consumeMode != null && !consumeMode.isEmpty()) {
                wrapper.eq(ConsumeRecordEntity::getConsumptionMode, consumeMode);
            }

            // 2. 查询记录
            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 3. 按时间维度分组统计
            List<Map<String, Object>> trendData = new ArrayList<>();
            String pattern = "yyyy-MM-dd";
            if ("MONTH".equalsIgnoreCase(timeDimension)) {
                pattern = "yyyy-MM";
            } else if ("YEAR".equalsIgnoreCase(timeDimension)) {
                pattern = "yyyy";
            } else if ("HOUR".equalsIgnoreCase(timeDimension)) {
                pattern = "yyyy-MM-dd HH";
            }

            final String datePattern = pattern;
            Map<String, List<ConsumeRecordEntity>> grouped = records.stream()
                    .filter(r -> r.getPayTime() != null)
                    .collect(Collectors.groupingBy(
                            r -> r.getPayTime().format(DateTimeFormatter.ofPattern(datePattern))));

            for (Map.Entry<String, List<ConsumeRecordEntity>> entry : grouped.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("time", entry.getKey());
                List<ConsumeRecordEntity> list = entry.getValue();

                if ("AMOUNT".equalsIgnoreCase(trendType)) {
                    BigDecimal totalAmount = list.stream()
                            .filter(r -> r.getAmount() != null)
                            .map(ConsumeRecordEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    item.put("value", totalAmount);
                } else {
                    item.put("value", list.size());
                }
                trendData.add(item);
            }

            // 4. 按时间排序
            trendData.sort((a, b) -> String.valueOf(a.get("time")).compareTo(String.valueOf(b.get("time"))));

            // 5. 设置缓存
            consumeCacheService.setCachedValue(cacheKey, trendData, REPORT_CACHE_TTL);

            log.debug("消费趋势分析完成, 数据点数量: {}", trendData.size());
            return trendData;

        } catch (Exception e) {
            log.error("获取消费趋势失败, timeDimension={}, startTime={}, endTime={}",
                    timeDimension, startTime, endTime, e);
            throw new RuntimeException("获取消费趋势失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取仪表盘数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 仪表盘数据
     */
    public Map<String, Object> getDashboardData(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取仪表盘数据, startTime={}, endTime={}", startTime, endTime);

        try {
            // 0. 构建缓存键
            String cacheKey = REPORT_CACHE_PREFIX + "dashboard:" +
                    (startTime != null ? startTime.toString() : "null") + ":" +
                    (endTime != null ? endTime.toString() : "null");

            // 0.1. 检查缓存
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedResult = (Map<String, Object>) consumeCacheService.getCachedValue(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存获取仪表盘数据, cacheKey={}", cacheKey);
                return cachedResult;
            }

            Map<String, Object> dashboard = new HashMap<>();

            // 1. 今日数据
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime todayEnd = LocalDateTime.now();

            LambdaQueryWrapper<ConsumeRecordEntity> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS")
                    .ge(ConsumeRecordEntity::getPayTime, todayStart)
                    .le(ConsumeRecordEntity::getPayTime, todayEnd);

            List<ConsumeRecordEntity> todayRecords = consumeRecordDao.selectList(todayWrapper);
            BigDecimal todayAmount = todayRecords.stream()
                    .filter(r -> r.getAmount() != null)
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long todayCount = todayRecords.size();

            // 2. 时间段数据
            BigDecimal totalAmount = BigDecimal.ZERO;
            long totalCount = 0;

            if (startTime != null || endTime != null) {
                LambdaQueryWrapper<ConsumeRecordEntity> rangeWrapper = new LambdaQueryWrapper<>();
                rangeWrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS");

                if (startTime != null) {
                    rangeWrapper.ge(ConsumeRecordEntity::getPayTime, startTime);
                }
                if (endTime != null) {
                    rangeWrapper.le(ConsumeRecordEntity::getPayTime, endTime);
                }

                List<ConsumeRecordEntity> rangeRecords = consumeRecordDao.selectList(rangeWrapper);
                totalAmount = rangeRecords.stream()
                        .filter(r -> r.getAmount() != null)
                        .map(ConsumeRecordEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalCount = rangeRecords.size();
            }

            // 3. 构建仪表盘数据
            dashboard.put("todayAmount", todayAmount);
            dashboard.put("todayCount", todayCount);
            dashboard.put("totalAmount", totalAmount);
            dashboard.put("totalCount", totalCount);
            dashboard.put("avgAmount", todayCount > 0
                    ? todayAmount.divide(new BigDecimal(todayCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            dashboard.put("updateTime", LocalDateTime.now());

            // 设置缓存
            consumeCacheService.setCachedValue(cacheKey, dashboard, DASHBOARD_CACHE_TTL);

            log.debug("仪表盘数据生成完成, todayAmount={}, todayCount={}", todayAmount, todayCount);
            return dashboard;

        } catch (Exception e) {
            log.error("获取仪表盘数据失败, startTime={}, endTime={}", startTime, endTime, e);
            throw new RuntimeException("获取仪表盘数据失败: " + e.getMessage(), e);
        }
    }
}

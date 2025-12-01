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

/**
 * 报表数据服务
 * 负责生成各类报表的原始数据
 *
 * @author SmartAdmin Team
 * @date 2025/01/30
 */
@Slf4j
@Service
public class ReportDataService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    // TODO: 充值相关功能待实现
    // @Resource
    // private RechargeRecordDao rechargeRecordDao;

    /**
     * 生成消费报表数据
     *
     * @param params 查询参数
     * @return 报表数据
     */
    public Map<String, Object> generateConsumeReport(Map<String, Object> params) {
        log.info("开始生成消费报表, params={}", params);

        try {
            // 1. 解析参数
            LocalDateTime startTime = params.get("startTime") != null
                    ? (LocalDateTime) params.get("startTime")
                    : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = params.get("endTime") != null
                    ? (LocalDateTime) params.get("endTime")
                    : LocalDateTime.now();
            String timeDimension = params.get("timeDimension") != null
                    ? (String) params.get("timeDimension")
                    : "DAY";
            Long deviceId = params.get("deviceId") != null
                    ? ((Number) params.get("deviceId")).longValue()
                    : null;
            String consumeMode = params.get("consumeMode") != null
                    ? (String) params.get("consumeMode")
                    : null;

            // 2. 构建查询条件
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS")
                    .ge(ConsumeRecordEntity::getPayTime, startTime)
                    .le(ConsumeRecordEntity::getPayTime, endTime);

            if (deviceId != null) {
                wrapper.eq(ConsumeRecordEntity::getDeviceId, deviceId);
            }
            if (consumeMode != null && !consumeMode.isEmpty()) {
                wrapper.eq(ConsumeRecordEntity::getConsumptionMode, consumeMode);
            }

            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 3. 统计数据
            BigDecimal totalAmount = records.stream()
                    .filter(r -> r.getAmount() != null)
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long totalCount = records.size();
            BigDecimal avgAmount = totalCount > 0
                    ? totalAmount.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // 4. 按时间维度分组
            Map<String, Object> details = groupByTimeDimension(records, timeDimension);

            // 5. 构建报表结果
            Map<String, Object> report = new HashMap<>();
            report.put("reportId", System.currentTimeMillis());
            report.put("reportType", "CONSUME");
            report.put("generatedTime", LocalDateTime.now());
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("totalAmount", totalAmount);
            report.put("totalCount", totalCount);
            report.put("avgAmount", avgAmount);
            report.put("timeDimension", timeDimension);
            report.put("details", details);
            report.put("status", "SUCCESS");

            log.debug("消费报表生成完成, totalAmount={}, totalCount={}", totalAmount, totalCount);
            return report;

        } catch (Exception e) {
            log.error("生成消费报表失败, params={}", params, e);
            throw new RuntimeException("生成消费报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成充值报表数据
     * TODO: 待实现充值相关功能
     *
     * @param params 查询参数
     * @return 报表数据
     */
    public Map<String, Object> generateRechargeReport(Map<String, Object> params) {
        log.info("开始生成充值报表, params={}", params);

        try {
            // 1. 解析参数
            LocalDateTime startTime = params.get("startTime") != null
                    ? (LocalDateTime) params.get("startTime")
                    : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = params.get("endTime") != null
                    ? (LocalDateTime) params.get("endTime")
                    : LocalDateTime.now();
            String timeDimension = params.get("timeDimension") != null
                    ? (String) params.get("timeDimension")
                    : "DAY";

            // TODO: 充值功能待实现，暂时返回空数据
            Map<String, Object> report = new HashMap<>();
            report.put("reportId", System.currentTimeMillis());
            report.put("reportType", "RECHARGE");
            report.put("generatedTime", LocalDateTime.now());
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("totalAmount", BigDecimal.ZERO);
            report.put("totalCount", 0L);
            report.put("avgAmount", BigDecimal.ZERO);
            report.put("timeDimension", timeDimension);
            report.put("details", new HashMap<>());
            report.put("status", "SUCCESS");
            report.put("message", "充值报表功能待实现");

            log.warn("充值报表功能待实现, 返回空数据");
            return report;

        } catch (Exception e) {
            log.error("生成充值报表失败, params={}", params, e);
            throw new RuntimeException("生成充值报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成用户消费报表数据
     *
     * @param params 查询参数
     * @return 报表数据
     */
    public Map<String, Object> generateUserConsumeReport(Map<String, Object> params) {
        log.info("开始生成用户消费报表, params={}", params);

        try {
            // 1. 解析参数
            LocalDateTime startTime = params.get("startTime") != null
                    ? (LocalDateTime) params.get("startTime")
                    : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = params.get("endTime") != null
                    ? (LocalDateTime) params.get("endTime")
                    : LocalDateTime.now();

            // 2. 查询消费记录
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS")
                    .ge(ConsumeRecordEntity::getPayTime, startTime)
                    .le(ConsumeRecordEntity::getPayTime, endTime)
                    .isNotNull(ConsumeRecordEntity::getPersonId);

            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 3. 按用户分组统计
            Map<Long, List<ConsumeRecordEntity>> userGroups = records.stream()
                    .filter(r -> r.getPersonId() != null)
                    .collect(Collectors.groupingBy(ConsumeRecordEntity::getPersonId));

            // 4. 计算用户统计数据
            List<Map<String, Object>> userStats = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Map.Entry<Long, List<ConsumeRecordEntity>> entry : userGroups.entrySet()) {
                Map<String, Object> userStat = new HashMap<>();
                Long userId = entry.getKey();
                List<ConsumeRecordEntity> userRecords = entry.getValue();

                BigDecimal userAmount = userRecords.stream()
                        .filter(r -> r.getAmount() != null)
                        .map(ConsumeRecordEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                userStat.put("userId", userId);
                userStat.put("userName", userRecords.get(0).getPersonName());
                userStat.put("amount", userAmount);
                userStat.put("count", userRecords.size());
                userStat.put("avgAmount", userRecords.size() > 0
                        ? userAmount.divide(new BigDecimal(userRecords.size()), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

                userStats.add(userStat);
                totalAmount = totalAmount.add(userAmount);
            }

            // 5. 计算汇总数据
            long userCount = userStats.size();
            BigDecimal avgAmount = userCount > 0
                    ? totalAmount.divide(new BigDecimal(userCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal avgConsumePerUser = records.size() > 0
                    ? new BigDecimal(records.size()).divide(new BigDecimal(userCount > 0 ? userCount : 1), 2,
                            RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // 6. 按金额排序
            userStats.sort((a, b) -> {
                BigDecimal amountA = (BigDecimal) a.get("amount");
                BigDecimal amountB = (BigDecimal) b.get("amount");
                return amountB.compareTo(amountA);
            });

            // 7. 构建报表结果
            Map<String, Object> report = new HashMap<>();
            report.put("reportId", System.currentTimeMillis());
            report.put("reportType", "USER_CONSUME");
            report.put("generatedTime", LocalDateTime.now());
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("userCount", userCount);
            report.put("totalAmount", totalAmount);
            report.put("totalCount", records.size());
            report.put("avgAmount", avgAmount);
            report.put("avgConsumePerUser", avgConsumePerUser);
            report.put("userStats", userStats);
            report.put("status", "SUCCESS");

            log.debug("用户消费报表生成完成, userCount={}, totalAmount={}", userCount, totalAmount);
            return report;

        } catch (Exception e) {
            log.error("生成用户消费报表失败, params={}", params, e);
            throw new RuntimeException("生成用户消费报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成设备使用报表数据
     *
     * @param params 查询参数
     * @return 报表数据
     */
    public Map<String, Object> generateDeviceUsageReport(Map<String, Object> params) {
        log.info("开始生成设备使用报表, params={}", params);

        try {
            // 1. 解析参数
            LocalDateTime startTime = params.get("startTime") != null
                    ? (LocalDateTime) params.get("startTime")
                    : LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = params.get("endTime") != null
                    ? (LocalDateTime) params.get("endTime")
                    : LocalDateTime.now();

            // 2. 查询消费记录（只查询有设备ID的记录）
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS")
                    .ge(ConsumeRecordEntity::getPayTime, startTime)
                    .le(ConsumeRecordEntity::getPayTime, endTime)
                    .isNotNull(ConsumeRecordEntity::getDeviceId);

            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 3. 按设备分组统计
            Map<Long, List<ConsumeRecordEntity>> deviceGroups = records.stream()
                    .filter(r -> r.getDeviceId() != null)
                    .collect(Collectors.groupingBy(ConsumeRecordEntity::getDeviceId));

            // 4. 计算设备统计数据
            List<Map<String, Object>> deviceStats = new ArrayList<>();
            long totalUsageCount = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Map.Entry<Long, List<ConsumeRecordEntity>> entry : deviceGroups.entrySet()) {
                Map<String, Object> deviceStat = new HashMap<>();
                Long deviceId = entry.getKey();
                List<ConsumeRecordEntity> deviceRecords = entry.getValue();

                BigDecimal deviceAmount = deviceRecords.stream()
                        .filter(r -> r.getAmount() != null)
                        .map(ConsumeRecordEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                deviceStat.put("deviceId", deviceId);
                deviceStat.put("deviceName", deviceRecords.get(0).getDeviceName());
                deviceStat.put("usageCount", deviceRecords.size());
                deviceStat.put("totalAmount", deviceAmount);
                deviceStat.put("avgAmount", deviceRecords.size() > 0
                        ? deviceAmount.divide(new BigDecimal(deviceRecords.size()), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

                deviceStats.add(deviceStat);
                totalUsageCount += deviceRecords.size();
                totalAmount = totalAmount.add(deviceAmount);
            }

            // 5. 计算汇总数据
            long deviceCount = deviceStats.size();
            BigDecimal avgUsagePerDevice = deviceCount > 0
                    ? new BigDecimal(totalUsageCount).divide(new BigDecimal(deviceCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // 6. 按使用次数排序
            deviceStats.sort((a, b) -> {
                Long countA = (Long) a.get("usageCount");
                Long countB = (Long) b.get("usageCount");
                return countB.compareTo(countA);
            });

            // 7. 构建报表结果
            Map<String, Object> report = new HashMap<>();
            report.put("reportId", System.currentTimeMillis());
            report.put("reportType", "DEVICE_USAGE");
            report.put("generatedTime", LocalDateTime.now());
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("deviceCount", deviceCount);
            report.put("usageCount", totalUsageCount);
            report.put("totalAmount", totalAmount);
            report.put("avgUsagePerDevice", avgUsagePerDevice);
            report.put("deviceStats", deviceStats);
            report.put("status", "SUCCESS");

            log.debug("设备使用报表生成完成, deviceCount={}, usageCount={}", deviceCount, totalUsageCount);
            return report;

        } catch (Exception e) {
            log.error("生成设备使用报表失败, params={}", params, e);
            throw new RuntimeException("生成设备使用报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按时间维度分组数据
     *
     * @param records       记录列表
     * @param timeDimension 时间维度（DAY/MONTH）
     * @return 分组后的数据
     */
    private Map<String, Object> groupByTimeDimension(List<ConsumeRecordEntity> records, String timeDimension) {
        Map<String, Object> result = new HashMap<>();

        switch (timeDimension.toUpperCase()) {
            case "DAY":
                result = records.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getPayTime() != null
                                        ? r.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        : "未知",
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            Map<String, Object> dayStats = new HashMap<>();
                                            BigDecimal dayAmount = list.stream()
                                                    .filter(r -> r.getAmount() != null)
                                                    .map(ConsumeRecordEntity::getAmount)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            dayStats.put("amount", dayAmount);
                                            dayStats.put("count", list.size());
                                            return dayStats;
                                        })));
                break;
            case "MONTH":
                result = records.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getPayTime() != null
                                        ? r.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                                        : "未知",
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            Map<String, Object> monthStats = new HashMap<>();
                                            BigDecimal monthAmount = list.stream()
                                                    .filter(r -> r.getAmount() != null)
                                                    .map(ConsumeRecordEntity::getAmount)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            monthStats.put("amount", monthAmount);
                                            monthStats.put("count", list.size());
                                            return monthStats;
                                        })));
                break;
            default:
                log.warn("不支持的时间维度: {}", timeDimension);
        }

        return result;
    }
}

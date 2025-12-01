package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import net.lab1024.sa.consume.service.ReportService;
import net.lab1024.sa.consume.service.report.ReportAnalysisService;

/**
 * 报表服务实现类
 * 实现ReportService接口，委托给其他专业服务类处理
 *
 * @author SmartAdmin Team
 * @date 2025/01/30
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ReportAnalysisService reportAnalysisService;

    /**
     * 获取设备日报表
     *
     * @param deviceId  设备ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日报表数据列表
     */
    @Override
    public List<ConsumeDeviceDailyReportVO> getDeviceDailyReport(Long deviceId, LocalDate startDate,
            LocalDate endDate) {
        log.info("获取设备日报表, deviceId={}, startDate={}, endDate={}", deviceId, startDate, endDate);

        try {
            List<ConsumeDeviceDailyReportVO> result = new ArrayList<>();

            // 遍历日期范围
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                LocalDateTime dayStart = currentDate.atStartOfDay();
                LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);

                // 构建查询条件
                LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS")
                        .ge(ConsumeRecordEntity::getPayTime, dayStart)
                        .le(ConsumeRecordEntity::getPayTime, dayEnd);

                if (deviceId != null) {
                    wrapper.eq(ConsumeRecordEntity::getDeviceId, deviceId);
                }

                // 查询记录
                List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

                // 统计数据
                int consumeCount = records.size();
                BigDecimal consumeAmount = records.stream()
                        .filter(r -> r.getAmount() != null)
                        .map(ConsumeRecordEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 按类型统计
                Map<String, Long> typeStatistics = records.stream()
                        .filter(r -> r.getConsumptionMode() != null)
                        .collect(Collectors.groupingBy(
                                ConsumeRecordEntity::getConsumptionMode,
                                Collectors.counting()));

                Map<String, BigDecimal> typeAmountStatistics = records.stream()
                        .filter(r -> r.getConsumptionMode() != null && r.getAmount() != null)
                        .collect(Collectors.groupingBy(
                                ConsumeRecordEntity::getConsumptionMode,
                                Collectors.reducing(BigDecimal.ZERO, ConsumeRecordEntity::getAmount, BigDecimal::add)));

                // 构建VO
                ConsumeDeviceDailyReportVO vo = new ConsumeDeviceDailyReportVO();
                vo.setDeviceId(deviceId);
                vo.setReportDate(currentDate);
                vo.setConsumeCount(consumeCount);
                vo.setConsumeAmount(consumeAmount);
                vo.setTypeStatistics(typeStatistics);
                vo.setTypeAmountStatistics(typeAmountStatistics);

                result.add(vo);

                // 移动到下一天
                currentDate = currentDate.plusDays(1);
            }

            log.debug("设备日报表生成完成, 记录数: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取设备日报表失败, deviceId={}, startDate={}, endDate={}", deviceId, startDate, endDate, e);
            throw new RuntimeException("获取设备日报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取消费趋势分析
     *
     * @param deviceId  设备ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势分析数据
     */
    @Override
    public Map<String, Object> getConsumeTrendAnalysis(Long deviceId, LocalDate startDate, LocalDate endDate) {
        log.info("获取消费趋势分析, deviceId={}, startDate={}, endDate={}", deviceId, startDate, endDate);

        try {
            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atTime(23, 59, 59);

            // 委托给ReportAnalysisService
            List<Map<String, Object>> trendData = reportAnalysisService.getConsumeTrend(
                    "DAY", startTime, endTime, "AMOUNT", deviceId, null);

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("trendData", trendData);

            // 计算汇总数据
            BigDecimal totalAmount = trendData.stream()
                    .filter(item -> item.get("value") instanceof BigDecimal)
                    .map(item -> (BigDecimal) item.get("value"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalCount = trendData.stream()
                    .filter(item -> item.get("value") instanceof Number)
                    .mapToLong(item -> ((Number) item.get("value")).longValue())
                    .sum();

            result.put("totalAmount", totalAmount);
            result.put("totalCount", totalCount);
            result.put("avgAmount", totalCount > 0
                    ? totalAmount.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            log.debug("消费趋势分析完成, 数据点数量: {}", trendData.size());
            return result;

        } catch (Exception e) {
            log.error("获取消费趋势分析失败, deviceId={}, startDate={}, endDate={}", deviceId, startDate, endDate, e);
            throw new RuntimeException("获取消费趋势分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按类型获取消费统计
     *
     * @param deviceId  设备ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 按类型统计的消费数据
     */
    @Override
    public Map<String, BigDecimal> getConsumptionByType(Long deviceId, LocalDate startDate, LocalDate endDate) {
        log.info("按类型获取消费统计, deviceId={}, startDate={}, endDate={}", deviceId, startDate, endDate);

        try {
            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atTime(23, 59, 59);

            // 构建查询条件
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getStatus, "SUCCESS")
                    .ge(ConsumeRecordEntity::getPayTime, startTime)
                    .le(ConsumeRecordEntity::getPayTime, endTime)
                    .isNotNull(ConsumeRecordEntity::getConsumptionMode);

            if (deviceId != null) {
                wrapper.eq(ConsumeRecordEntity::getDeviceId, deviceId);
            }

            // 查询记录
            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 按类型统计金额
            Map<String, BigDecimal> result = records.stream()
                    .filter(r -> r.getConsumptionMode() != null && r.getAmount() != null)
                    .collect(Collectors.groupingBy(
                            ConsumeRecordEntity::getConsumptionMode,
                            Collectors.reducing(BigDecimal.ZERO, ConsumeRecordEntity::getAmount, BigDecimal::add)));

            log.debug("按类型消费统计完成, 类型数量: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("按类型获取消费统计失败, deviceId={}, startDate={}, endDate={}", deviceId, startDate, endDate, e);
            throw new RuntimeException("按类型获取消费统计失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取小时级消费分析
     *
     * @param deviceId 设备ID
     * @param date     日期
     * @return 小时级分析数据
     */
    @Override
    public List<Map<String, Object>> getHourlyConsumptionAnalysis(Long deviceId, LocalDate date) {
        log.info("获取小时级消费分析, deviceId={}, date={}", deviceId, date);

        try {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);

            // 委托给ReportAnalysisService
            List<Map<String, Object>> trendData = reportAnalysisService.getConsumeTrend(
                    "HOUR", dayStart, dayEnd, "AMOUNT", deviceId, null);

            // 转换为小时级数据格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> item : trendData) {
                Map<String, Object> hourData = new HashMap<>();
                String timeStr = String.valueOf(item.get("time"));
                // 提取小时部分
                if (timeStr.length() >= 13) {
                    hourData.put("hour", Integer.parseInt(timeStr.substring(11, 13)));
                } else {
                    hourData.put("hour", 0);
                }
                hourData.put("amount", item.get("value"));
                hourData.put("time", timeStr);
                result.add(hourData);
            }

            // 按小时排序
            result.sort((a, b) -> {
                Integer hourA = (Integer) a.get("hour");
                Integer hourB = (Integer) b.get("hour");
                return hourA.compareTo(hourB);
            });

            log.debug("小时级消费分析完成, 数据点数量: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取小时级消费分析失败, deviceId={}, date={}", deviceId, date, e);
            throw new RuntimeException("获取小时级消费分析失败: " + e.getMessage(), e);
        }
    }
}

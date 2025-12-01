package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.consume.domain.dto.RefundQueryDTO;
import net.lab1024.sa.consume.domain.dto.RefundResultDTO;
import net.lab1024.sa.consume.domain.entity.RefundRecordEntity;

/**
 * 报表服务接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ReportService {

    /**
     * 获取设备日报表
     *
     * @param deviceId   设备ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 日报表数据
     */
    List<ConsumeDeviceDailyReportVO> getDeviceDailyReport(Long deviceId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取消费趋势分析
     *
     * @param deviceId   设备ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 趋势分析数据
     */
    Map<String, Object> getConsumeTrendAnalysis(Long deviceId, LocalDate startDate, LocalDate endDate);

    /**
     * 按类型获取消费统计
     *
     * @param deviceId   设备ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 按类型统计的消费数据
     */
    Map<String, BigDecimal> getConsumptionByType(Long deviceId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取小时级消费分析
     *
     * @param deviceId 设备ID
     * @param date     日期
     * @return 小时级分析数据
     */
    List<Map<String, Object>> getHourlyConsumptionAnalysis(Long deviceId, LocalDate date);

    // 内部VO类定义
    class ConsumeDeviceDailyReportVO {
        private Long deviceId;
        private LocalDate reportDate;
        private Integer consumeCount;
        private BigDecimal consumeAmount;
        private Map<String, Long> typeStatistics;
        private Map<String, BigDecimal> typeAmountStatistics;

        // Getters and Setters
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        public LocalDate getReportDate() { return reportDate; }
        public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
        public Integer getConsumeCount() { return consumeCount; }
        public void setConsumeCount(Integer consumeCount) { this.consumeCount = consumeCount; }
        public BigDecimal getConsumeAmount() { return consumeAmount; }
        public void setConsumeAmount(BigDecimal consumeAmount) { this.consumeAmount = consumeAmount; }
        public Map<String, Long> getTypeStatistics() { return typeStatistics; }
        public void setTypeStatistics(Map<String, Long> typeStatistics) { this.typeStatistics = typeStatistics; }
        public Map<String, BigDecimal> getTypeAmountStatistics() { return typeAmountStatistics; }
        public void setTypeAmountStatistics(Map<String, BigDecimal> typeAmountStatistics) { this.typeAmountStatistics = typeAmountStatistics; }
    }
}
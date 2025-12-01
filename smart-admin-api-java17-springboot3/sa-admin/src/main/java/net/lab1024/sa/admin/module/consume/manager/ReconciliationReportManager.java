package net.lab1024.sa.admin.module.consume.manager;


import net.lab1024.sa.admin.module.consume.manager.TransactionManagementManager.ReconciliationResult;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.base.common.util.SmartDateUtil;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对账报告管理器
 * 负责生成详细的对账报告、差异分析和异常处理
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class ReconciliationReportManager {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private TransactionManagementManager transactionManagementManager;

    /**
     * 对账报告
     */
    public static class ReconciliationReport {
        private String reportId;
        private LocalDate reportDate;
        private LocalDateTime generatedAt;
        private String summary; // 摘要
        private boolean reconciled; // 是否对账成功
        private Map<String, Object> overview; // 概览信息
        private List<TransactionDiscrepancy> discrepancies; // 交易差异
        private List<AnomalyRecord> anomalies; // 异常记录
        private Map<String, Object> detailedStats; // 详细统计
        private List<String> recommendations; // 建议措施

        // 构造函数
        public ReconciliationReport() {
            this.discrepancies = new ArrayList<>();
            this.anomalies = new ArrayList<>();
            this.overview = new HashMap<>();
            this.detailedStats = new HashMap<>();
            this.recommendations = new ArrayList<>();
        }

        // Getters and Setters
        public String getReportId() { return reportId; }
        public void setReportId(String reportId) { this.reportId = reportId; }

        public LocalDate getReportDate() { return reportDate; }
        public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public boolean isReconciled() { return reconciled; }
        public void setReconciled(boolean reconciled) { this.reconciled = reconciled; }

        public Map<String, Object> getOverview() { return overview; }
        public List<TransactionDiscrepancy> getDiscrepancies() { return discrepancies; }
        public List<AnomalyRecord> getAnomalies() { return anomalies; }
        public Map<String, Object> getDetailedStats() { return detailedStats; }
        public List<String> getRecommendations() { return recommendations; }
    }

    /**
     * 交易差异记录
     */
    public static class TransactionDiscrepancy {
        private String orderId;
        private String discrepancyType; // MISSING, EXTRA, AMOUNT_MISMATCH
        private BigDecimal systemAmount;
        private BigDecimal thirdPartyAmount;
        private BigDecimal difference;
        private String description;
        private LocalDateTime discoveredAt;

        // 构造函数
        public TransactionDiscrepancy(String orderId, String discrepancyType,
                                        BigDecimal systemAmount, BigDecimal thirdPartyAmount) {
            this.orderId = orderId;
            this.discrepancyType = discrepancyType;
            this.systemAmount = systemAmount;
            this.thirdPartyAmount = thirdPartyAmount;
            this.difference = systemAmount.subtract(thirdPartyAmount).abs();
            this.discoveredAt = LocalDateTime.now();
        }

        // Getters
        public String getOrderId() { return orderId; }
        public String getDiscrepancyType() { return discrepancyType; }
        public BigDecimal getSystemAmount() { return systemAmount; }
        public BigDecimal getThirdPartyAmount() { return thirdPartyAmount; }
        public BigDecimal getDifference() { return difference; }
        public String getDescription() { return description; }
        public LocalDateTime getDiscoveredAt() { return discoveredAt; }

        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 异常记录
     */
    public static class AnomalyRecord {
        private String anomalyId;
        private String anomalyType; // FREQUENCY_ANOMALY, AMOUNT_ANOMALY, TIME_ANOMALY
        private String description;
        private String severity; // HIGH, MEDIUM, LOW
        private Map<String, Object> details;
        private LocalDateTime detectedAt;
        private boolean resolved;

        // 构造函数
        public AnomalyRecord(String anomalyType, String description, String severity) {
            this.anomalyId = UUID.randomUUID().toString();
            this.anomalyType = anomalyType;
            this.description = description;
            this.severity = severity;
            this.details = new HashMap<>();
            this.detectedAt = LocalDateTime.now();
            this.resolved = false;
        }

        // Getters and Setters
        public String getAnomalyId() { return anomalyId; }
        public String getAnomalyType() { return anomalyType; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public Map<String, Object> getDetails() { return details; }
        public LocalDateTime getDetectedAt() { return detectedAt; }
        public boolean isResolved() { return resolved; }

        public void addDetail(String key, Object value) { this.details.put(key, value); }
        public void setResolved(boolean resolved) { this.resolved = resolved; }
    }

    /**
     * 生成对账报告
     */
    public ReconciliationReport generateReconciliationReport(LocalDate date) {
        try {
            log.info("生成对账报告: date={}", date);

            ReconciliationReport report = new ReconciliationReport();
            report.setReportId("RECON_" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + System.currentTimeMillis());
            report.setReportDate(date);
            report.setGeneratedAt(LocalDateTime.now());

            // 1. 执行对账
            ReconciliationResult reconciliationResult = transactionManagementManager.performReconciliation(date);
            report.setReconciled(reconciliationResult.isReconciled());

            // 2. 生成概览信息
            generateOverview(report, reconciliationResult);

            // 3. 分析交易差异
            analyzeTransactionDiscrepancies(report, date);

            // 4. 检测异常模式
            detectAnomalies(report, date);

            // 5. 生成详细统计
            generateDetailedStatistics(report, date);

            // 6. 生成建议措施
            generateRecommendations(report);

            // 7. 生成摘要
            generateSummary(report);

            log.info("对账报告生成完成: reportId={}, reconciled={}, discrepancies={}",
                    report.getReportId(), report.isReconciled(), report.getDiscrepancies().size());

            return report;

        } catch (Exception e) {
            log.error("生成对账报告失败: date={}", date, e);
            ReconciliationReport errorReport = new ReconciliationReport();
            errorReport.setReportId("ERROR_" + System.currentTimeMillis());
            errorReport.setReportDate(date);
            errorReport.setGeneratedAt(LocalDateTime.now());
            errorReport.setReconciled(false);
            errorReport.setSummary("报告生成失败: " + e.getMessage());
            return errorReport;
        }
    }

    /**
     * 生成概览信息
     */
    private void generateOverview(ReconciliationReport report, ReconciliationResult result) {
        Map<String, Object> overview = new HashMap<>();
        overview.put("reconciliationStatus", result.isReconciled() ? "SUCCESS" : "FAILED");
        overview.put("systemData", Map.of(
            "totalCount", result.getSystemTotalCount(),
            "totalAmount", result.getSystemTotalAmount()
        ));
        overview.put("thirdPartyData", Map.of(
            "totalCount", result.getThirdPartyTotalCount(),
            "totalAmount", result.getThirdPartyTotalAmount()
        ));
        overview.put("discrepancies", Map.of(
            "countMismatch", result.getSystemTotalCount() != result.getThirdPartyTotalCount(),
            "amountMismatch", result.getSystemTotalAmount().compareTo(result.getThirdPartyTotalAmount()) != 0
        ));

        if (!result.isReconciled()) {
            overview.put("mismatchDetails", Map.of(
                "countDifference", Math.abs((int)(result.getSystemTotalCount() - result.getThirdPartyTotalCount())),
                "amountDifference", result.getMismatchedAmount()
            ));
        }

        report.setOverview(overview);
    }

    /**
     * 分析交易差异
     */
    private void analyzeTransactionDiscrepancies(ReconciliationReport report, LocalDate date) {
        try {
            log.debug("分析交易差异: date={}", date);

            // 获取系统交易数据
            List<ConsumeRecordEntity> systemTransactions = getSystemTransactions(date);

            // 模拟第三方交易数据（实际应该从第三方API获取）
            Map<String, BigDecimal> thirdPartyTransactions = getThirdPartyTransactions(date);

            Set<String> systemOrderIds = systemTransactions.stream()
                    .map(ConsumeRecordEntity::getOrderNo)
                    .collect(Collectors.toSet());

            Set<String> thirdPartyOrderIds = thirdPartyTransactions.keySet();

            // 1. 检查系统有而第三方没有的交易（缺失）
            for (String systemOrderId : systemOrderIds) {
                if (!thirdPartyOrderIds.contains(systemOrderId)) {
                    ConsumeRecordEntity transaction = systemTransactions.stream()
                            .filter(t -> systemOrderId.equals(t.getOrderNo()))
                            .findFirst()
                            .orElse(null);

                    if (transaction != null) {
                        TransactionDiscrepancy discrepancy = new TransactionDiscrepancy(
                            systemOrderId, "MISSING", transaction.getAmount(), BigDecimal.ZERO);
                        discrepancy.setDescription("系统记录中存在但第三方记录中缺失");
                        report.getDiscrepancies().add(discrepancy);
                    }
                }
            }

            // 2. 检查第三方有而系统没有的交易（额外）
            for (String thirdPartyOrderId : thirdPartyOrderIds) {
                if (!systemOrderIds.contains(thirdPartyOrderId)) {
                    TransactionDiscrepancy discrepancy = new TransactionDiscrepancy(
                            thirdPartyOrderId, "EXTRA", BigDecimal.ZERO, thirdPartyTransactions.get(thirdPartyOrderId));
                    discrepancy.setDescription("第三方记录中存在但系统记录中缺失");
                    report.getDiscrepancies().add(discrepancy);
                }
            }

            // 3. 检查金额不匹配的交易
            for (String commonOrderId : systemOrderIds) {
                if (thirdPartyOrderIds.contains(commonOrderId)) {
                    ConsumeRecordEntity transaction = systemTransactions.stream()
                            .filter(t -> commonOrderId.equals(t.getOrderNo()))
                            .findFirst()
                            .orElse(null);

                    BigDecimal systemAmount = transaction != null ? transaction.getAmount() : BigDecimal.ZERO;
                    BigDecimal thirdPartyAmount = thirdPartyTransactions.get(commonOrderId);

                    if (systemAmount.compareTo(thirdPartyAmount) != 0) {
                        TransactionDiscrepancy discrepancy = new TransactionDiscrepancy(
                                commonOrderId, "AMOUNT_MISMATCH", systemAmount, thirdPartyAmount);
                        discrepancy.setDescription("系统记录金额与第三方记录金额不匹配");
                        report.getDiscrepancies().add(discrepancy);
                    }
                }
            }

            log.debug("交易差异分析完成: totalDiscrepancies={}", report.getDiscrepancies().size());

        } catch (Exception e) {
            log.error("分析交易差异失败: date={}", date, e);
        }
    }

    /**
     * 检测异常模式
     */
    private void detectAnomalies(ReconciliationReport report, LocalDate date) {
        try {
            log.debug("检测异常模式: date={}", date);

            List<ConsumeRecordEntity> transactions = getSystemTransactions(date);

            // 1. 检测高频交易异常
            detectHighFrequencyAnomalies(report, transactions);

            // 2. 检测大额交易异常
            detectLargeAmountAnomalies(report, transactions);

            // 3. 检测时间异常
            detectTimeAnomalies(report, transactions);

            // 4. 检测重复订单号异常
            detectDuplicateOrderAnomalies(report, transactions);

            log.debug("异常检测完成: totalAnomalies={}", report.getAnomalies().size());

        } catch (Exception e) {
            log.error("检测异常模式失败: date={}", date, e);
        }
    }

    /**
     * 检测高频交易异常
     */
    private void detectHighFrequencyAnomalies(ReconciliationReport report, List<ConsumeRecordEntity> transactions) {
        // 按用户分组，检测异常高频交易
        Map<Long, List<ConsumeRecordEntity>> userTransactions = transactions.stream()
                .collect(Collectors.groupingBy(ConsumeRecordEntity::getPersonId));

        for (Map.Entry<Long, List<ConsumeRecordEntity>> entry : userTransactions.entrySet()) {
            Long userId = entry.getKey();
            List<ConsumeRecordEntity> userTxList = entry.getValue();

            // 检测10分钟内交易次数
            Map<Long, Long> frequencyCount = new HashMap<>();
            for (ConsumeRecordEntity tx : userTxList) {
                long minuteBucket = tx.getPayTime().toLocalDate().toEpochDay() * 24 * 60 +
                                 tx.getPayTime().getHour() * 60 + tx.getPayTime().getMinute();
                frequencyCount.merge(minuteBucket, 1L, Long::sum);
            }

            long maxFrequency = frequencyCount.values().stream().max(Long::compare).orElse(0L);
            if (maxFrequency > 10) { // 10分钟内超过10次交易认为异常
                AnomalyRecord anomaly = new AnomalyRecord("FREQUENCY_ANOMALY",
                        "用户在10分钟内进行了" + maxFrequency + "次交易", "HIGH");
                anomaly.addDetail("userId", userId);
                anomaly.addDetail("maxFrequency", maxFrequency);
                report.getAnomalies().add(anomaly);
            }
        }
    }

    /**
     * 检测大额交易异常
     */
    private void detectLargeAmountAnomalies(ReconciliationReport report, List<ConsumeRecordEntity> transactions) {
        BigDecimal threshold = new BigDecimal("1000.00"); // 大额交易阈值

        for (ConsumeRecordEntity transaction : transactions) {
            if (transaction.getAmount().compareTo(threshold) > 0) {
                AnomalyRecord anomaly = new AnomalyRecord("AMOUNT_ANOMALY",
                        "检测到大额交易: " + transaction.getAmount(), "MEDIUM");
                anomaly.addDetail("orderId", transaction.getOrderNo());
                anomaly.addDetail("amount", transaction.getAmount());
                anomaly.addDetail("userId", transaction.getPersonId());
                report.getAnomalies().add(anomaly);
            }
        }
    }

    /**
     * 检测时间异常
     */
    private void detectTimeAnomalies(ReconciliationReport report, List<ConsumeRecordEntity> transactions) {
        // 检测非营业时间交易
        for (ConsumeRecordEntity transaction : transactions) {
            int hour = transaction.getPayTime().getHour();
            if (hour < 6 || hour > 22) { // 6:00-22:00外为非营业时间
                AnomalyRecord anomaly = new AnomalyRecord("TIME_ANOMALY",
                        "检测到非营业时间交易: " + hour + "点", "LOW");
                anomaly.addDetail("orderId", transaction.getOrderNo());
                anomaly.addDetail("transactionTime", transaction.getPayTime().toString());
                report.getAnomalies().add(anomaly);
            }
        }
    }

    /**
     * 检测重复订单号异常
     */
    private void detectDuplicateOrderAnomalies(ReconciliationReport report, List<ConsumeRecordEntity> transactions) {
        Map<String, Long> orderCount = transactions.stream()
                .collect(Collectors.groupingBy(ConsumeRecordEntity::getOrderNo, Collectors.counting()));

        for (Map.Entry<String, Long> entry : orderCount.entrySet()) {
            if (entry.getValue() > 1) {
                AnomalyRecord anomaly = new AnomalyRecord("DUPLICATE_ORDER",
                        "检测到重复订单号: " + entry.getKey() + "，出现次数: " + entry.getValue(), "HIGH");
                anomaly.addDetail("orderId", entry.getKey());
                anomaly.addDetail("count", entry.getValue());
                report.getAnomalies().add(anomaly);
            }
        }
    }

    /**
     * 生成详细统计
     */
    private void generateDetailedStatistics(ReconciliationReport report, LocalDate date) {
        try {
            Map<String, Object> stats = new HashMap<>();

            List<ConsumeRecordEntity> transactions = getSystemTransactions(date);

            // 基础统计
            long totalCount = transactions.size();
            BigDecimal totalAmount = transactions.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averageAmount = totalCount > 0 ?
                    totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            stats.put("totalCount", totalCount);
            stats.put("totalAmount", totalAmount);
            stats.put("averageAmount", averageAmount);

            // 时间分布统计
            Map<Integer, Long> hourlyDistribution = transactions.stream()
                    .collect(Collectors.groupingBy(
                        tx -> tx.getPayTime().getHour(),
                        Collectors.counting()
                    ));
            stats.put("hourlyDistribution", hourlyDistribution);

            // 消费模式统计
            Map<String, Long> modeDistribution = transactions.stream()
                    .collect(Collectors.groupingBy(
                        tx -> tx.getConsumeMode() != null ? tx.getConsumeMode() : "UNKNOWN",
                        Collectors.counting()
                    ));
            stats.put("modeDistribution", modeDistribution);

            // 支付方式统计
            Map<String, Long> paymentMethodDistribution = transactions.stream()
                    .collect(Collectors.groupingBy(
                        tx -> tx.getPayMethod() != null ? tx.getPayMethod() : "UNKNOWN",
                        Collectors.counting()
                    ));
            stats.put("paymentMethodDistribution", paymentMethodDistribution);

            // 异常统计
            stats.put("totalAnomalies", report.getAnomalies().size());
            stats.put("highSeverityAnomalies", report.getAnomalies().stream()
                    .filter(a -> "HIGH".equals(a.getSeverity()))
                    .count());
            stats.put("mediumSeverityAnomalies", report.getAnomalies().stream()
                    .filter(a -> "MEDIUM".equals(a.getSeverity()))
                    .count());
            stats.put("lowSeverityAnomalies", report.getAnomalies().stream()
                    .filter(a -> "LOW".equals(a.getSeverity()))
                    .count());

            // 差异统计
            stats.put("totalDiscrepancies", report.getDiscrepancies().size());
            Map<String, Long> discrepancyTypeStats = report.getDiscrepancies().stream()
                    .collect(Collectors.groupingBy(
                        TransactionDiscrepancy::getDiscrepancyType,
                        Collectors.counting()
                    ));
            stats.put("discrepancyTypeDistribution", discrepancyTypeStats);

            report.setDetailedStats(stats);

        } catch (Exception e) {
            log.error("生成详细统计失败: date={}", date, e);
        }
    }

    /**
     * 生成建议措施
     */
    private void generateRecommendations(ReconciliationReport report) {
        List<String> recommendations = new ArrayList<>();

        if (!report.isReconciled()) {
            recommendations.add("立即进行手动对账，解决发现的差异问题");
            recommendations.add("检查第三方支付平台的API连接和数据同步状态");
        }

        if (!report.getDiscrepancies().isEmpty()) {
            recommendations.add("建立自动化差异检测和修复机制");
            recommendations.add("增加实时数据同步验证");
        }

        if (!report.getAnomalies().isEmpty()) {
            long highSeverityCount = report.getAnomalies().stream()
                    .filter(a -> "HIGH".equals(a.getSeverity()))
                    .count();

            if (highSeverityCount > 0) {
                recommendations.add("立即调查高严重性异常，可能存在安全风险");
            }

            recommendations.add("建立异常检测规则和自动预警机制");
        }

        // 性能优化建议
        if (report.getDetailedStats() != null) {
            Map<String, Object> stats = report.getDetailedStats();
            Object totalCount = stats.get("totalCount");

            if (totalCount instanceof Integer && (Integer)totalCount > 1000) {
                recommendations.add("考虑实现数据分区以提高查询性能");
            }
        }

        // 通用建议
        recommendations.add("定期进行系统性能和安全审计");
        recommendations.add("完善日志记录和监控告警机制");

        report.setRecommendations(recommendations);
    }

    /**
     * 生成摘要
     */
    private void generateSummary(ReconciliationReport report) {
        StringBuilder summary = new StringBuilder();

        summary.append("对账报告摘要：\n");
        summary.append("- 对账日期: ").append(report.getReportDate().toString()).append("\n");
        summary.append("- 对账状态: ").append(report.isReconciled() ? "成功" : "失败").append("\n");

        if (report.getOverview() != null) {
            Map<String, Object> overview = report.getOverview();
            summary.append("- 系统交易数: ").append(getNestedValue(overview, "systemData", "totalCount")).append("\n");
            summary.append("- 第三方交易数: ").append(getNestedValue(overview, "thirdPartyData", "totalCount")).append("\n");
        }

        summary.append("- 发现差异: ").append(report.getDiscrepancies().size()).append(" 条\n");
        summary.append("- 检测异常: ").append(report.getAnomalies().size()).append(" 条\n");

        if (!report.getRecommendations().isEmpty()) {
            summary.append("- 建议措施: ").append(report.getRecommendations().size()).append(" 条");
        }

        report.setSummary(summary.toString());
    }

    /**
     * 获取系统交易数据
     */
    private List<ConsumeRecordEntity> getSystemTransactions(LocalDate date) {
        // TODO: 实现从数据库查询系统交易数据
        return new ArrayList<>();
    }

    /**
     * 模拟获取第三方交易数据
     */
    private Map<String, BigDecimal> getThirdPartyTransactions(LocalDate date) {
        // TODO: 实现从第三方API获取交易数据
        return new HashMap<>();
    }

    /**
     * 获取嵌套Map值
     */
    private Object getNestedValue(Map<String, Object> map, String parentKey, String childKey) {
        Object parentValue = map.get(parentKey);
        if (parentValue instanceof Map) {
            Map<?, ?> parentMap = (Map<?, ?>) parentValue;
            return parentMap.get(childKey);
        }
        return null;
    }
}
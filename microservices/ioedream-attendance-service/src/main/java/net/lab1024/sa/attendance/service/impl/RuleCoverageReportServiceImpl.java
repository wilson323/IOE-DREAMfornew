package net.lab1024.sa.attendance.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.attendance.dao.RuleCoverageReportDao;
import net.lab1024.sa.attendance.dao.RuleTestHistoryDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.domain.form.RuleCoverageReportForm;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageReportVO;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageTrendVO;
import net.lab1024.sa.attendance.domain.vo.RuleCoverageDetailVO;
import net.lab1024.sa.common.entity.attendance.RuleCoverageReportEntity;
import net.lab1024.sa.common.entity.attendance.RuleTestHistoryEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceRuleEntity;
import net.lab1024.sa.attendance.service.RuleCoverageReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则覆盖率报告服务实现类
 * <p>
 * 实现规则覆盖率统计和分析功能
 * 支持日报、周报、月报和自定义报告
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class RuleCoverageReportServiceImpl implements RuleCoverageReportService {

    @Resource
    private RuleCoverageReportDao ruleCoverageReportDao;

    @Resource
    private RuleTestHistoryDao ruleTestHistoryDao;

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 生成覆盖率报告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleCoverageReportVO generateCoverageReport(RuleCoverageReportForm form) {
        log.info("[覆盖率报告] 开始生成覆盖率报告: type={}, startDate={}, endDate={}",
                form.getReportType(), form.getStartDate(), form.getEndDate());

        long startTime = System.currentTimeMillis();

        try {
            // 确定报告日期范围
            LocalDate startDate = determineStartDate(form);
            LocalDate endDate = determineEndDate(form);
            LocalDate reportDate = endDate; // 报告日期使用结束日期

            // 统计总规则数
            int totalRules = countTotalRules();

            // 统计测试数据
            Map<String, Object> coverageStats = calculateCoverageStats(startDate, endDate);

            // 创建报告实体
            RuleCoverageReportEntity reportEntity = RuleCoverageReportEntity.builder()
                    .reportDate(reportDate)
                    .reportType(form.getReportType())
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalRules(totalRules)
                    .testedRules((Integer) coverageStats.get("testedRules"))
                    .coverageRate((Double) coverageStats.get("coverageRate"))
                    .totalTests((Integer) coverageStats.get("totalTests"))
                    .successTests((Integer) coverageStats.get("successTests"))
                    .failedTests((Integer) coverageStats.get("failedTests"))
                    .successRate((Double) coverageStats.get("successRate"))
                    .coverageDetails((String) coverageStats.get("coverageDetails"))
                    .uncoveredRules((String) coverageStats.get("uncoveredRules"))
                    .lowCoverageRules((String) coverageStats.get("lowCoverageRules"))
                    .reportStatus("COMPLETED")
                    .generationTimeMs(System.currentTimeMillis() - startTime)
                    .build();

            // 保存报告
            ruleCoverageReportDao.insert(reportEntity);

            log.info("[覆盖率报告] 报告生成成功: reportId={}, coverageRate={}%, totalRules={}, testedRules={}",
                    reportEntity.getReportId(), reportEntity.getCoverageRate(),
                    reportEntity.getTotalRules(), reportEntity.getTestedRules());

            return convertToVO(reportEntity);

        } catch (Exception e) {
            log.error("[覆盖率报告] 报告生成失败: type={}, error={}", form.getReportType(), e.getMessage(), e);
            throw new RuntimeException("报告生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询报告结果
     */
    @Override
    public RuleCoverageReportVO getReport(Long reportId) {
        log.info("[覆盖率报告] 查询报告: reportId={}", reportId);

        RuleCoverageReportEntity entity = ruleCoverageReportDao.selectById(reportId);
        if (entity == null) {
            log.warn("[覆盖率报告] 报告不存在: reportId={}", reportId);
            return null;
        }

        return convertToVO(entity);
    }

    /**
     * 查询指定日期的报告
     */
    @Override
    public RuleCoverageReportVO getReportByDate(LocalDate reportDate) {
        log.info("[覆盖率报告] 查询日期报告: reportDate={}", reportDate);

        RuleCoverageReportEntity entity = ruleCoverageReportDao.queryByReportDate(reportDate);
        if (entity == null) {
            log.warn("[覆盖率报告] 报告不存在: reportDate={}", reportDate);
            return null;
        }

        return convertToVO(entity);
    }

    /**
     * 查询最近的报告列表
     */
    @Override
    public List<RuleCoverageReportVO> getRecentReports(Integer limit) {
        log.info("[覆盖率报告] 查询最近报告: limit={}", limit);

        List<RuleCoverageReportEntity> entities = ruleCoverageReportDao.queryRecentReports(limit);

        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询覆盖率趋势数据
     */
    @Override
    public List<RuleCoverageTrendVO> getCoverageTrend(LocalDate startDate, LocalDate endDate) {
        log.info("[覆盖率报告] 查询趋势数据: startDate={}, endDate={}", startDate, endDate);

        List<RuleCoverageReportEntity> entities = ruleCoverageReportDao.queryTrendData(startDate, endDate);

        return entities.stream()
                .map(entity -> RuleCoverageTrendVO.builder()
                        .date(entity.getReportDate())
                        .coverageRate(entity.getCoverageRate())
                        .testedRules(entity.getTestedRules())
                        .totalRules(entity.getTotalRules())
                        .totalTests(entity.getTotalTests())
                        .successRate(entity.getSuccessRate())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 查询规则覆盖详情
     */
    @Override
    public List<RuleCoverageDetailVO> getRuleCoverageDetails(Long reportId) {
        log.info("[覆盖率报告] 查询规则详情: reportId={}", reportId);

        RuleCoverageReportEntity report = ruleCoverageReportDao.selectById(reportId);
        if (report == null) {
            log.warn("[覆盖率报告] 报告不存在: reportId={}", reportId);
            return Collections.emptyList();
        }

        // 解析覆盖率详情JSON
        try {
            if (report.getCoverageDetails() != null) {
                Map<Long, Map<String, Object>> detailsMap = objectMapper.readValue(
                        report.getCoverageDetails(),
                        new TypeReference<Map<Long, Map<String, Object>>>() {}
                );

                // 获取所有规则信息
                List<AttendanceRuleEntity> allRules = attendanceRuleDao.selectList(null);

                return allRules.stream()
                        .map(rule -> {
                            Map<String, Object> detail = detailsMap.getOrDefault(rule.getRuleId(), new HashMap<>());
                            return RuleCoverageDetailVO.builder()
                                    .ruleId(rule.getRuleId())
                                    .ruleName(rule.getRuleName())
                                    .ruleType(rule.getRuleType())
                                    .testCount(((Number) detail.getOrDefault("testCount", 0)).intValue())
                                    .successCount(((Number) detail.getOrDefault("successCount", 0)).intValue())
                                    .failedCount(((Number) detail.getOrDefault("failedCount", 0)).intValue())
                                    .successRate((Double) detail.getOrDefault("successRate", 0.0))
                                    .lastTestTime((String) detail.get("lastTestTime"))
                                    .coverageStatus((String) detail.getOrDefault("coverageStatus", "UNCOVERED"))
                                    .coverageLevel((String) detail.getOrDefault("coverageLevel", "NONE"))
                                    .build();
                        })
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("[覆盖率报告] 解析详情失败: reportId={}", reportId, e);
        }

        return Collections.emptyList();
    }

    /**
     * 删除报告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long reportId) {
        log.info("[覆盖率报告] 删除报告: reportId={}", reportId);

        int count = ruleCoverageReportDao.deleteById(reportId);
        if (count > 0) {
            log.info("[覆盖率报告] 删除成功: reportId={}", reportId);
        } else {
            log.warn("[覆盖率报告] 删除失败，记录不存在: reportId={}", reportId);
        }
    }

    /**
     * 批量删除报告
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteReports(List<Long> reportIds) {
        log.info("[覆盖率报告] 批量删除报告: count={}", reportIds.size());

        int count = 0;
        for (Long reportId : reportIds) {
            count += ruleCoverageReportDao.deleteById(reportId);
        }

        log.info("[覆盖率报告] 批量删除成功: count={}", count);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 确定报告开始日期
     */
    private LocalDate determineStartDate(RuleCoverageReportForm form) {
        if (form.getStartDate() != null) {
            return form.getStartDate();
        }

        LocalDate today = LocalDate.now();
        return switch (form.getReportType()) {
            case "DAILY" -> today;
            case "WEEKLY" -> today.minusWeeks(1);
            case "MONTHLY" -> today.minusMonths(1);
            default -> today;
        };
    }

    /**
     * 确定报告结束日期
     */
    private LocalDate determineEndDate(RuleCoverageReportForm form) {
        if (form.getEndDate() != null) {
            return form.getEndDate();
        }

        LocalDate today = LocalDate.now();
        if (form.getReportDate() != null) {
            return form.getReportDate();
        }

        return today;
    }

    /**
     * 统计总规则数
     */
    private int countTotalRules() {
        return Math.toIntExact(attendanceRuleDao.selectCount(null));
    }

    /**
     * 计算覆盖率统计数据（核心方法）
     */
    private Map<String, Object> calculateCoverageStats(LocalDate startDate, LocalDate endDate) {
        log.info("[覆盖率报告] 开始统计覆盖率: startDate={}, endDate={}", startDate, endDate);

        // 查询所有规则
        List<AttendanceRuleEntity> allRules = attendanceRuleDao.selectList(null);
        int totalRules = allRules.size();

        // 查询测试历史
        List<RuleTestHistoryEntity> testHistories = ruleTestHistoryDao.selectList(null);

        // 统计每个规则的覆盖情况
        Map<Long, RuleCoverageStats> ruleStatsMap = new HashMap<>();
        Set<Long> testedRuleIds = new HashSet<>();
        int totalTests = 0;
        int successTests = 0;
        int failedTests = 0;

        for (RuleTestHistoryEntity history : testHistories) {
            Long ruleId = history.getRuleId();
            if (ruleId == null) {
                continue;
            }

            // 初始化规则统计
            ruleStatsMap.putIfAbsent(ruleId, new RuleCoverageStats());
            RuleCoverageStats stats = ruleStatsMap.get(ruleId);

            // 更新统计
            stats.testCount++;
            totalTests++;

            if ("SUCCESS".equals(history.getTestResult())) {
                stats.successCount++;
                successTests++;
            } else {
                stats.failedCount++;
                failedTests++;
            }

            // 更新最后测试时间
            if (stats.lastTestTime == null || history.getCreateTime().isAfter(stats.lastTestTime)) {
                stats.lastTestTime = history.getCreateTime();
            }

            testedRuleIds.add(ruleId);
        }

        int testedRules = testedRuleIds.size();
        double coverageRate = totalRules > 0 ? (testedRules * 100.0 / totalRules) : 0.0;
        double successRate = totalTests > 0 ? (successTests * 100.0 / totalTests) : 0.0;

        // 构建详情数据
        Map<String, Object> result = new HashMap<>();
        result.put("testedRules", testedRules);
        result.put("coverageRate", coverageRate);
        result.put("totalTests", totalTests);
        result.put("successTests", successTests);
        result.put("failedTests", failedTests);
        result.put("successRate", successRate);

        // 构建规则覆盖详情
        Map<Long, Map<String, Object>> coverageDetails = new HashMap<>();
        List<Long> uncoveredRules = new ArrayList<>();
        List<Long> lowCoverageRules = new ArrayList<>();

        for (AttendanceRuleEntity rule : allRules) {
            Long ruleId = rule.getRuleId();
            RuleCoverageStats stats = ruleStatsMap.getOrDefault(ruleId, new RuleCoverageStats());

            Map<String, Object> detail = new HashMap<>();
            detail.put("testCount", stats.testCount);
            detail.put("successCount", stats.successCount);
            detail.put("failedCount", stats.failedCount);
            detail.put("successRate", stats.testCount > 0 ? (stats.successCount * 100.0 / stats.testCount) : 0.0);
            detail.put("lastTestTime", stats.lastTestTime != null ? stats.lastTestTime.toString() : null);

            // 确定覆盖状态
            if (stats.testCount == 0) {
                detail.put("coverageStatus", "UNCOVERED");
                detail.put("coverageLevel", "NONE");
                uncoveredRules.add(ruleId);
            } else if (stats.testCount < 5) {
                detail.put("coverageStatus", "LOW_COVERAGE");
                detail.put("coverageLevel", "LOW");
                lowCoverageRules.add(ruleId);
            } else if (stats.testCount < 10) {
                detail.put("coverageStatus", "COVERED");
                detail.put("coverageLevel", "MEDIUM");
            } else {
                detail.put("coverageStatus", "COVERED");
                detail.put("coverageLevel", "HIGH");
            }

            coverageDetails.put(ruleId, detail);
        }

        // 转换为JSON
        try {
            result.put("coverageDetails", objectMapper.writeValueAsString(coverageDetails));
            result.put("uncoveredRules", objectMapper.writeValueAsString(uncoveredRules));
            result.put("lowCoverageRules", objectMapper.writeValueAsString(lowCoverageRules));
        } catch (Exception e) {
            log.error("[覆盖率报告] JSON转换失败", e);
        }

        log.info("[覆盖率报告] 统计完成: totalRules={}, testedRules={}, coverageRate={}%",
                totalRules, testedRules, coverageRate);

        return result;
    }

    /**
     * 转换为VO
     */
    private RuleCoverageReportVO convertToVO(RuleCoverageReportEntity entity) {
        return RuleCoverageReportVO.builder()
                .reportId(entity.getReportId())
                .reportDate(entity.getReportDate())
                .totalRules(entity.getTotalRules())
                .testedRules(entity.getTestedRules())
                .untestedRules(entity.getTotalRules() - entity.getTestedRules())
                .coverageRate(entity.getCoverageRate())
                .totalTests(entity.getTotalTests())
                .successTests(entity.getSuccessTests())
                .failedTests(entity.getFailedTests())
                .successRate(entity.getSuccessRate())
                .reportType(entity.getReportType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .reportStatus(entity.getReportStatus())
                .generationTimeMs(entity.getGenerationTimeMs())
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 规则覆盖统计（内部类）
     */
    private static class RuleCoverageStats {
        int testCount = 0;
        int successCount = 0;
        int failedCount = 0;
        LocalDateTime lastTestTime = null;
    }
}

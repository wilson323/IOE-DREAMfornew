package net.lab1024.sa.report.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.report.domain.entity.ReportTemplateEntity;
import net.lab1024.sa.report.manager.ReportDataManager;

/**
 * 报表数据服务
 * <p>
 * 四层架构中的Service层，负责业务逻辑处理
 * 严格遵循repowiki编码规范：
 * - 使用jakarta包名
 * - 使用@Resource依赖注入
 * - 四层架构：Controller → Service → Manager → DAO/Repository
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataService {

    @Resource
    private ReportDataManager reportDataManager;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 获取报表数据
     */
    public Map<String, Object> getReportData(ReportTemplateEntity template, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            if (template.getSqlQuery() != null && !template.getSqlQuery().trim().isEmpty()) {
                // 验证SQL安全性
                ReportDataManager.SqlValidationResult validation = reportDataManager
                        .validateReportSql(template.getSqlQuery());
                if (!validation.isValid()) {
                    throw new IllegalArgumentException("SQL验证失败: " + validation.getMessage());
                }

                // 执行SQL查询
                log.info("执行报表SQL: {}", template.getSqlQuery());

                // 通过Manager层执行查询
                List<Map<String, Object>> data = reportDataManager.executeReportQuery(template.getSqlQuery(), params);

                result.put("data", data);
                result.put("totalCount", data.size());
                result.put("sql", template.getSqlQuery());

                // 记录报表生成日志
                long executionTime = System.currentTimeMillis() - startTime;
                reportDataManager.recordReportGeneration(template.getTemplateId(), params, data.size(), executionTime);

            } else {
                // 模拟数据（用于演示）
                result.put("data", generateMockData(template, params));
                result.put("totalCount", 100);
                result.put("mock", true);
            }

            result.put("templateId", template.getTemplateId());
            result.put("templateName", template.getTemplateName());
            result.put("generatedTime", LocalDateTime.now());

            return result;

        } catch (Exception e) {
            log.error("获取报表数据失败，模板ID: {}", template.getTemplateId(), e);
            result.put("error", e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("generatedTime", LocalDateTime.now());
            return result;
        }
    }

    /**
     * 构建SQL语句
     *
     * @deprecated 当前未使用，预留方法，待后续功能扩展时启用
     */
    @Deprecated
    private String buildSql(String templateSql, Map<String, Object> params) {
        String sql = templateSql;

        if (params != null && !params.isEmpty()) {
            // 替换参数占位符
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (sql.contains(placeholder)) {
                    sql = sql.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }

            // 处理分页参数
            if (params.containsKey("pageNum") && params.containsKey("pageSize")) {
                int pageNum = (Integer) params.get("pageNum");
                int pageSize = (Integer) params.get("pageSize");
                int offset = (pageNum - 1) * pageSize;

                sql += " LIMIT " + offset + ", " + pageSize;
            }
        }

        return sql;
    }

    /**
     * 生成模拟数据
     */
    private List<Map<String, Object>> generateMockData(ReportTemplateEntity template, Map<String, Object> params) {
        List<Map<String, Object>> mockData = new ArrayList<>();

        // 根据模板类型生成不同的模拟数据
        switch (template.getTemplateType()) {
            case "TABLE":
                mockData = generateTableMockData(template, params);
                break;
            case "CHART":
                mockData = generateChartMockData(template, params);
                break;
            case "MIX":
                mockData = generateMixMockData(template, params);
                break;
            default:
                mockData = generateDefaultMockData(params);
        }

        return mockData;
    }

    /**
     * 生成表格模拟数据
     */
    private List<Map<String, Object>> generateTableMockData(ReportTemplateEntity template, Map<String, Object> params) {
        List<Map<String, Object>> data = new ArrayList<>();
        int limit = params != null && params.containsKey("limit") ? (Integer) params.get("limit") : 20;

        for (int i = 1; i <= limit; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "数据项" + i);
            row.put("value", Math.random() * 1000);
            row.put("category", "类别" + (i % 5 + 1));
            row.put("createTime", LocalDateTime.now().minusDays(i));
            data.add(row);
        }

        return data;
    }

    /**
     * 生成图表模拟数据
     */
    private List<Map<String, Object>> generateChartMockData(ReportTemplateEntity template, Map<String, Object> params) {
        List<Map<String, Object>> data = new ArrayList<>();

        // 模拟月度数据
        String[] months = { "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月" };

        for (String month : months) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("sales", Math.random() * 50000 + 10000);
            item.put("profit", Math.random() * 10000 + 1000);
            item.put("customers", Math.random() * 1000 + 100);
            data.add(item);
        }

        return data;
    }

    /**
     * 生成混合模拟数据
     */
    private List<Map<String, Object>> generateMixMockData(ReportTemplateEntity template, Map<String, Object> params) {
        List<Map<String, Object>> data = generateTableMockData(template, params);

        // 添加统计信息
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSales", data.stream().mapToDouble(row -> (Double) row.get("value")).sum());
        summary.put("avgSales", data.stream().mapToDouble(row -> (Double) row.get("value")).average().orElse(0));
        summary.put("maxSales", data.stream().mapToDouble(row -> (Double) row.get("value")).max().orElse(0));
        summary.put("minSales", data.stream().mapToDouble(row -> (Double) row.get("value")).min().orElse(0));

        // 将汇总信息添加到第一行
        if (!data.isEmpty()) {
            data.get(0).putAll(summary);
        }

        return data;
    }

    /**
     * 生成默认模拟数据
     */
    private List<Map<String, Object>> generateDefaultMockData(Map<String, Object> params) {
        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("key", "key" + i);
            row.put("value", Math.random() * 100);
            data.add(row);
        }

        return data;
    }

    /**
     * 验证SQL语法
     */
    public boolean validateSql(String sql) {
        try {
            // 这里应该使用SQL解析器进行语法验证
            // 简化实现，检查基本的SQL关键字
            if (sql == null || sql.trim().isEmpty()) {
                return false;
            }

            String upperSql = sql.toUpperCase();
            if (!upperSql.contains("SELECT")) {
                return false;
            }

            // 检查危险的SQL操作
            if (upperSql.contains("DROP ") || upperSql.contains("DELETE ") ||
                    upperSql.contains("UPDATE ") || upperSql.contains("INSERT ")) {
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("SQL验证失败", e);
            return false;
        }
    }

    /**
     * 记录报表生成日志 - 通过Manager层
     *
     * @deprecated 当前未使用，预留方法，待后续功能扩展时启用
     */
    @Deprecated
    private void recordReportGeneration(Long templateId, Map<String, Object> params, int dataCount) {
        try {
            // 通过Manager层记录日志，符合四层架构规范
            reportDataManager.recordReportGenerationLog(templateId, params, dataCount);
        } catch (Exception e) {
            log.error("记录报表生成日志失败", e);
        }
    }

    /**
     * 获取今日报表生成数量 - 通过Manager层
     */
    public long getTodayReportCount() {
        try {
            // 通过Manager层获取数据，符合四层架构规范
            return reportDataManager.getTodayReportCount();
        } catch (Exception e) {
            log.error("获取今日报表数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取本月报表生成数量 - 通过Manager层
     */
    public long getMonthReportCount() {
        try {
            // 通过Manager层获取数据，符合四层架构规范
            return reportDataManager.getMonthReportCount();
        } catch (Exception e) {
            log.error("获取本月报表数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取热门报表模板 - 通过Manager层
     */
    public List<Map<String, Object>> getPopularTemplates(int limit) {
        try {
            // 通过Manager层获取数据，符合四层架构规范
            return reportDataManager.getPopularTemplates(limit);
        } catch (Exception e) {
            log.error("获取热门模板失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取报表类型分布 - 通过Manager层
     */
    public Map<String, Long> getReportTypeDistribution() {
        try {
            // 通过Manager层获取数据，符合四层架构规范
            return reportDataManager.getReportTypeDistribution();
        } catch (Exception e) {
            log.error("获取报表类型分布失败", e);
            return new HashMap<>();
        }
    }
}

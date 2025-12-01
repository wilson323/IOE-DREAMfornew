package net.lab1024.sa.report.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.report.domain.entity.ReportTemplateEntity;
import net.lab1024.sa.report.domain.vo.ReportResponseVO;

/**
 * 简化报表服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class SimpleReportService {

    /**
     * 获取报表数据
     */
    public ReportResponseVO getReportData(ReportTemplateEntity template, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 模拟报表数据
            List<Map<String, Object>> data = generateMockReportData(template, params);

            result.put("data", data);
            result.put("totalCount", data.size());
            result.put("templateId", template.getTemplateId());
            result.put("templateName", template.getTemplateName());
            result.put("generatedTime", LocalDateTime.now());

            ReportResponseVO response = ReportResponseVO.success();
            response.setTemplateId(template.getTemplateId() != null ? template.getTemplateId().toString() : null);
            response.setTemplateName(template.getTemplateName());
            response.setData(result);
            response.setGeneratedTime(LocalDateTime.now());

            return response;

        } catch (Exception e) {
            log.error("获取报表数据失败，模板ID: {}", template.getTemplateId(), e);
            ReportResponseVO response = ReportResponseVO.error("报表生成失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 生成模拟报表数据
     */
    private List<Map<String, Object>> generateMockReportData(ReportTemplateEntity template,
            Map<String, Object> params) {
        List<Map<String, Object>> data = new ArrayList<>();
        int limit = params != null && params.containsKey("limit") ? (Integer) params.get("limit") : 20;

        for (int i = 1; i <= limit; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "数据项" + i);
            row.put("value", Math.random() * 1000);
            row.put("category", "类别" + (i % 5 + 1));
            row.put("createTime", LocalDateTime.now().minusDays(i));
            row.put("status", i % 2 == 0 ? "活跃" : "暂停");
            data.add(row);
        }

        return data;
    }

    /**
     * 验证SQL语法
     */
    public boolean validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String upperSql = sql.toUpperCase();
        if (!upperSql.contains("SELECT")) {
            return false;
        }

        if (upperSql.contains("DROP ") || upperSql.contains("DELETE ") ||
                upperSql.contains("UPDATE ") || upperSql.contains("INSERT ")) {
            return false;
        }

        return true;
    }

    /**
     * 获取报表统计信息
     */
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("templateCount", 15L);
        stats.put("todayReportCount", 42L);
        stats.put("monthReportCount", 856L);

        List<Map<String, Object>> popularTemplates = Arrays.asList(
                Map.of("templateName", "销售统计报表", "usageCount", 128L),
                Map.of("templateName", "用户分析报表", "usageCount", 96L),
                Map.of("templateName", "财务报表", "usageCount", 87L),
                Map.of("templateName", "考勤统计", "usageCount", 74L),
                Map.of("templateName", "门禁报表", "usageCount", 62L));
        stats.put("popularTemplates", popularTemplates);

        Map<String, Long> reportTypeDistribution = Map.of(
                "TABLE", 8L,
                "CHART", 5L,
                "MIX", 2L);
        stats.put("reportTypeDistribution", reportTypeDistribution);

        return stats;
    }

    /**
     * 创建模拟模板实体
     */
    public ReportTemplateEntity createMockTemplate(Long templateId) {
        ReportTemplateEntity template = new ReportTemplateEntity();
        template.setTemplateId(templateId);
        template.setTemplateName("示例报表模板");
        template.setTemplateCode("DEMO_REPORT");
        template.setDescription("这是一个示例报表模板");
        template.setTemplateType("TABLE");
        template.setSqlQuery("SELECT * FROM t_demo WHERE id > 0");
        template.setEnabled(1);
        template.setSortOrder(1);

        return template;
    }
}

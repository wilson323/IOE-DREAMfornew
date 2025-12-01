package net.lab1024.sa.report.service;

import net.lab1024.sa.report.domain.entity.ReportTemplateEntity;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 图表生成服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
public interface ChartGeneratorService {

    /**
     * 生成图表数据
     */
    Map<String, Object> generateCharts(ReportTemplateEntity template, Map<String, Object> data);

    /**
     * 验证图表配置
     */
    boolean validateChartConfig(String chartConfig);

    /**
     * 默认实现类
     */
    class ChartGeneratorServiceImpl implements ChartGeneratorService {

        @Override
        public Map<String, Object> generateCharts(ReportTemplateEntity template, Map<String, Object> data) {
            Map<String, Object> charts = new HashMap<>();

            // 根据模板类型生成不同的图表
            switch (template.getTemplateType()) {
                case "CHART":
                    charts.put("mainChart", generateMainChart(data));
                    break;
                case "MIX":
                    charts.put("mainChart", generateMainChart(data));
                    charts.put("pieChart", generatePieChart(data));
                    break;
                default:
                    // TABLE类型不需要图表
                    break;
            }

            return charts;
        }

        @Override
        public boolean validateChartConfig(String chartConfig) {
            if (chartConfig == null || chartConfig.trim().isEmpty()) {
                return true; // 空配置是有效的
            }

            try {
                // 简单的JSON格式验证
                return chartConfig.contains("{") && chartConfig.contains("}");
            } catch (Exception e) {
                return false;
            }
        }

        private Map<String, Object> generateMainChart(Map<String, Object> data) {
            Map<String, Object> chart = new HashMap<>();
            chart.put("type", "line");
            chart.put("title", "数据趋势图");

            List<Map<String, Object>> seriesData = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                Map<String, Object> point = new HashMap<>();
                point.put("name", "第" + i + "月");
                point.put("value", Math.random() * 1000);
                seriesData.add(point);
            }

            chart.put("data", seriesData);
            return chart;
        }

        private Map<String, Object> generatePieChart(Map<String, Object> data) {
            Map<String, Object> chart = new HashMap<>();
            chart.put("type", "pie");
            chart.put("title", "数据分布图");

            List<Map<String, Object>> pieData = new ArrayList<>();
            String[] categories = {"类别A", "类别B", "类别C", "类别D"};
            for (String category : categories) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", category);
                item.put("value", Math.random() * 100);
                pieData.add(item);
            }

            chart.put("data", pieData);
            return chart;
        }
    }
}
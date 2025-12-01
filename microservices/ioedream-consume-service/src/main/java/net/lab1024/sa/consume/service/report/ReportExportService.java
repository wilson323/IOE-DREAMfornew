package net.lab1024.sa.consume.service.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 报表导出服务
 * 负责报表的导出和CSV文件生成
 *
 * @author SmartAdmin Team
 * @date 2025/01/30
 */
@Slf4j
@Service
public class ReportExportService {

    @Resource
    private ReportDataService reportDataService;

    @Value("${file.storage.local.upload-path:D:/Progect/mart-admin-master/upload/}")
    private String fileUploadPath;

    /**
     * 导出报表
     *
     * @param reportType 报表类型
     * @param params     查询参数
     * @param format     导出格式（csv/xlsx）
     * @return 导出文件路径
     */
    public String exportReport(String reportType, Map<String, Object> params, String format) {
        log.info("开始导出报表, reportType={}, format={}", reportType, format);

        try {
            // 1. 生成报表数据
            Map<String, Object> reportData = null;
            switch (reportType.toUpperCase()) {
                case "CONSUME":
                    reportData = reportDataService.generateConsumeReport(params);
                    break;
                case "RECHARGE":
                    reportData = reportDataService.generateRechargeReport(params);
                    break;
                case "USER_CONSUME":
                    reportData = reportDataService.generateUserConsumeReport(params);
                    break;
                case "DEVICE_USAGE":
                    reportData = reportDataService.generateDeviceUsageReport(params);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的报表类型: " + reportType);
            }

            // 2. 确定导出格式
            String exportFormat = (format != null && !format.isEmpty()) ? format.toLowerCase() : "csv";
            if (!exportFormat.equals("csv") && !exportFormat.equals("xlsx")) {
                exportFormat = "csv";
            }

            // 3. 生成文件路径
            String fileName = reportType.toLowerCase() + "_export_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "." + exportFormat;
            String exportDir = fileUploadPath + "/export";
            Path exportPath = Paths.get(exportDir);

            // 确保导出目录存在
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            Path filePath = exportPath.resolve(fileName);

            // 4. 生成导出文件
            if ("csv".equals(exportFormat)) {
                generateCsvFile(reportData, filePath, reportType);
            } else {
                // 目前只支持CSV，Excel功能待实现
                generateCsvFile(reportData, filePath, reportType);
            }

            log.info("报表导出完成, filePath={}, reportType={}, format={}", filePath, reportType, exportFormat);
            return "/export/" + fileName;

        } catch (IOException e) {
            log.error("报表导出失败, IO异常, reportType={}, format={}", reportType, format, e);
            throw new RuntimeException("报表导出失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("报表导出失败, reportType={}, format={}", reportType, format, e);
            throw new RuntimeException("报表导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成CSV文件
     * 支持UTF-8编码，添加BOM以兼容Excel
     *
     * @param reportData 报表数据
     * @param filePath   文件路径
     * @param reportType 报表类型
     * @throws IOException IO异常
     */
    private void generateCsvFile(Map<String, Object> reportData, Path filePath, String reportType)
            throws IOException {
        StringBuilder content = new StringBuilder();

        // 添加UTF-8 BOM，确保Excel正确识别UTF-8编码
        content.append("\uFEFF");

        // 根据报表类型生成不同的CSV内容
        switch (reportType.toUpperCase()) {
            case "CONSUME":
                generateConsumeReportCsv(reportData, content);
                break;
            case "RECHARGE":
                generateRechargeReportCsv(reportData, content);
                break;
            case "USER_CONSUME":
                generateUserConsumeReportCsv(reportData, content);
                break;
            case "DEVICE_USAGE":
                generateDeviceUsageReportCsv(reportData, content);
                break;
            default:
                // 默认格式：键值对
                content.append("报表类型,值\n");
                for (Map.Entry<String, Object> entry : reportData.entrySet()) {
                    content.append(entry.getKey()).append(",")
                            .append(entry.getValue() != null ? entry.getValue().toString() : "").append("\n");
                }
        }

        Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成消费报表CSV
     */
    @SuppressWarnings("unchecked")
    private void generateConsumeReportCsv(Map<String, Object> reportData, StringBuilder content) {
        content.append("消费报表\n");
        content.append("报表类型,").append(reportData.get("reportType")).append("\n");
        content.append("生成时间,").append(reportData.get("generatedTime")).append("\n");
        content.append("开始时间,").append(reportData.get("startTime")).append("\n");
        content.append("结束时间,").append(reportData.get("endTime")).append("\n");
        content.append("总金额,").append(reportData.get("totalAmount")).append("\n");
        content.append("总笔数,").append(reportData.get("totalCount")).append("\n");
        content.append("平均金额,").append(reportData.get("avgAmount")).append("\n");
        content.append("\n");

        // 明细数据
        Object details = reportData.get("details");
        if (details instanceof Map) {
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            content.append("日期,金额,笔数\n");
            for (Map.Entry<String, Object> entry : detailsMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> stats = (Map<String, Object>) entry.getValue();
                    content.append(entry.getKey()).append(",")
                            .append(stats.get("amount")).append(",")
                            .append(stats.get("count")).append("\n");
                }
            }
        }
    }

    /**
     * 生成充值报表CSV
     */
    @SuppressWarnings("unchecked")
    private void generateRechargeReportCsv(Map<String, Object> reportData, StringBuilder content) {
        content.append("充值报表\n");
        content.append("报表类型,").append(reportData.get("reportType")).append("\n");
        content.append("生成时间,").append(reportData.get("generatedTime")).append("\n");
        content.append("开始时间,").append(reportData.get("startTime")).append("\n");
        content.append("结束时间,").append(reportData.get("endTime")).append("\n");
        content.append("总金额,").append(reportData.get("totalAmount")).append("\n");
        content.append("总笔数,").append(reportData.get("totalCount")).append("\n");
        content.append("平均金额,").append(reportData.get("avgAmount")).append("\n");
        content.append("\n");

        // 明细数据
        Object details = reportData.get("details");
        if (details instanceof Map) {
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            content.append("日期,金额,笔数\n");
            for (Map.Entry<String, Object> entry : detailsMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> stats = (Map<String, Object>) entry.getValue();
                    content.append(entry.getKey()).append(",")
                            .append(stats.get("amount")).append(",")
                            .append(stats.get("count")).append("\n");
                }
            }
        }
    }

    /**
     * 生成用户消费报表CSV
     */
    @SuppressWarnings("unchecked")
    private void generateUserConsumeReportCsv(Map<String, Object> reportData, StringBuilder content) {
        content.append("用户消费报表\n");
        content.append("报表类型,").append(reportData.get("reportType")).append("\n");
        content.append("生成时间,").append(reportData.get("generatedTime")).append("\n");
        content.append("开始时间,").append(reportData.get("startTime")).append("\n");
        content.append("结束时间,").append(reportData.get("endTime")).append("\n");
        content.append("用户数量,").append(reportData.get("userCount")).append("\n");
        content.append("总金额,").append(reportData.get("totalAmount")).append("\n");
        content.append("总笔数,").append(reportData.get("totalCount")).append("\n");
        content.append("平均金额,").append(reportData.get("avgAmount")).append("\n");
        content.append("\n");

        // 用户统计数据
        Object userStats = reportData.get("userStats");
        if (userStats instanceof List) {
            List<Map<String, Object>> statsList = (List<Map<String, Object>>) userStats;
            content.append("用户ID,用户名称,消费金额,消费笔数,平均金额\n");
            for (Map<String, Object> stat : statsList) {
                content.append(stat.get("userId")).append(",")
                        .append(stat.get("userName") != null ? stat.get("userName") : "").append(",")
                        .append(stat.get("amount")).append(",")
                        .append(stat.get("count")).append(",")
                        .append(stat.get("avgAmount")).append("\n");
            }
        }
    }

    /**
     * 生成设备使用报表CSV
     */
    @SuppressWarnings("unchecked")
    private void generateDeviceUsageReportCsv(Map<String, Object> reportData, StringBuilder content) {
        content.append("设备使用报表\n");
        content.append("报表类型,").append(reportData.get("reportType")).append("\n");
        content.append("生成时间,").append(reportData.get("generatedTime")).append("\n");
        content.append("开始时间,").append(reportData.get("startTime")).append("\n");
        content.append("结束时间,").append(reportData.get("endTime")).append("\n");
        content.append("设备数量,").append(reportData.get("deviceCount")).append("\n");
        content.append("使用次数,").append(reportData.get("usageCount")).append("\n");
        content.append("总金额,").append(reportData.get("totalAmount")).append("\n");
        content.append("平均使用次数,").append(reportData.get("avgUsagePerDevice")).append("\n");
        content.append("\n");

        // 设备统计数据
        Object deviceStats = reportData.get("deviceStats");
        if (deviceStats instanceof List) {
            List<Map<String, Object>> statsList = (List<Map<String, Object>>) deviceStats;
            content.append("设备ID,设备名称,使用次数,总金额,平均金额\n");
            for (Map<String, Object> stat : statsList) {
                content.append(stat.get("deviceId")).append(",")
                        .append(stat.get("deviceName") != null ? stat.get("deviceName") : "").append(",")
                        .append(stat.get("usageCount")).append(",")
                        .append(stat.get("totalAmount")).append(",")
                        .append(stat.get("avgAmount")).append("\n");
            }
        }
    }
}

package net.lab1024.sa.report.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.lab1024.sa.report.domain.vo.ReportResponseVO;

/**
 * 报表导出服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
public interface ExportService {

    /**
     * 导出报表
     */
    String exportReport(ReportResponseVO reportData, String exportType);

    /**
     * 默认实现类
     */
    class ExportServiceImpl implements ExportService {

        @Override
        public String exportReport(ReportResponseVO reportData, String exportType) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_%s_%s.%s",
                    reportData.getTemplateName(),
                    "report",
                    timestamp,
                    exportType.toLowerCase());

            String filePath = "./temp/" + fileName;

            // 模拟导出过程
            try {
                Thread.sleep(1000); // 模拟导出耗时
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return filePath;
        }
    }
}

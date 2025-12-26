package net.lab1024.sa.data.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.data.dao.ExportTaskDao;
import net.lab1024.sa.data.domain.DataAnalysisDomain.DataExportRequest;
import net.lab1024.sa.data.domain.DataAnalysisDomain.DataExportResult;
import net.lab1024.sa.data.domain.DataAnalysisDomain.ReportQueryRequest;
import net.lab1024.sa.data.domain.DataAnalysisDomain.ReportQueryResult;
import net.lab1024.sa.common.entity.data.ExportTaskEntity;
import net.lab1024.sa.data.service.excel.ReportDataExcelWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件导出服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class FileExportService {

    @Resource
    private ReportService reportService;

    @Resource
    private ExportTaskDao exportTaskDao;

    @Value("${export.file.path:/tmp/exports}")
    private String exportFilePath;

    /**
     * 导出报表为Excel
     */
    @Async
    public void exportToExcel(String taskId, Long reportId, Map<String, Object> params) {
        log.info("[文件导出] 开始Excel导出: taskId={}, reportId={}", taskId, reportId);

        FileOutputStream outputStream = null;
        String fileName = null;
        String filePath = null;

        try {
            // 更新任务状态为processing
            updateTaskStatus(taskId, "processing", null, null, null);

            // 查询报表数据
            ReportQueryRequest request = ReportQueryRequest.builder()
                    .reportId(reportId)
                    .params(params)
                    .build();

            ReportQueryResult result = reportService.queryReportData(request);

            // 生成文件名
            fileName = "report_" + reportId + "_" +
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                     ".xlsx";

            // 确保导出目录存在
            File exportDir = new File(exportFilePath);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            filePath = exportFilePath + File.separator + fileName;

            // 使用EasyExcel导出
            outputStream = new FileOutputStream(filePath);

            List<Map<String, Object>> dataList = result.getDataList();

            if (dataList != null && !dataList.isEmpty()) {
                // 写入Excel
                EasyExcel.write(outputStream, Map.class)
                        .sheet("报表数据")
                        .doWrite(dataList);

                // 获取文件大小
                File file = new File(filePath);
                long fileSize = file.length();

                // 更新任务状态为completed
                String fileUrl = "/exports/" + fileName;
                updateTaskStatus(taskId, "completed", fileName, fileUrl, fileSize);

                log.info("[文件导出] Excel导出成功: file={}, records={}, size={}KB",
                         fileName, dataList.size(), fileSize / 1024);
            } else {
                log.warn("[文件导出] 没有数据需要导出: reportId={}", reportId);
                updateTaskStatus(taskId, "completed", fileName, "/exports/" + fileName, 0L);
            }

        } catch (Exception e) {
            log.error("[文件导出] Excel导出失败: taskId={}", taskId, e);
            updateTaskStatus(taskId, "failed", null, null, null, "Excel导出失败: " + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("[文件导出] 关闭输出流失败", e);
                }
            }
        }
    }

    /**
     * 导出报表为PDF
     */
    @Async
    public void exportToPdf(String taskId, Long reportId, Map<String, Object> params) {
        log.info("[文件导出] 开始PDF导出: taskId={}, reportId={}", taskId, reportId);

        PdfWriter writer = null;
        String fileName = null;
        String filePath = null;

        try {
            // 更新任务状态为processing
            updateTaskStatus(taskId, "processing", null, null, null);

            // 查询报表数据
            ReportQueryRequest request = ReportQueryRequest.builder()
                    .reportId(reportId)
                    .params(params)
                    .build();

            ReportQueryResult result = reportService.queryReportData(request);

            // 生成文件名
            fileName = "report_" + reportId + "_" +
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                     ".pdf";

            // 确保导出目录存在
            File exportDir = new File(exportFilePath);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            filePath = exportFilePath + File.separator + fileName;

            // 使用iText生成PDF
            writer = new PdfWriter(new FileOutputStream(filePath));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // 设置中文字体（如果系统有中文字体）
            PdfFont font = PdfFontFactory.createFont();

            // 添加标题
            Paragraph title = new Paragraph("报表名称: " + result.getReportName())
                    .setFont(font)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // 添加生成时间
            Paragraph time = new Paragraph("生成时间: " + result.getQueryTime())
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(20);
            document.add(time);

            // 添加表格
            List<Map<String, Object>> dataList = result.getDataList();

            if (dataList != null && !dataList.isEmpty()) {
                // 创建表格（假设第一行是表头）
                Map<String, Object> firstRow = dataList.get(0);
                int columnCount = firstRow.size();

                Table table = new Table(unitValue = new com.itextpdf.layout.properties.UnitValue(
                        com.itextpdf.layout.properties.UnitValue.createPointArray(
                                new float[]{10, 30, 30, 30, 30, 30, 30, 30, 30, 30}))
                ));

                // 添加表头
                for (String key : firstRow.keySet()) {
                    Cell headerCell = new Cell()
                            .add(new Paragraph(key))
                            .setBold()
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY);
                    table.addHeaderCell(headerCell);
                }

                // 添加数据行
                for (Map<String, Object> dataRow : dataList) {
                    for (Object value : dataRow.values()) {
                        Cell dataCell = new Cell()
                                .add(new Paragraph(value != null ? value.toString() : ""));
                        table.addCell(dataCell);
                    }
                }

                document.add(table);
            }

            document.close();

            // 获取文件大小
            File file = new File(filePath);
            long fileSize = file.length();

            // 更新任务状态为completed
            String fileUrl = "/exports/" + fileName;
            updateTaskStatus(taskId, "completed", fileName, fileUrl, fileSize);

            log.info("[文件导出] PDF导出成功: file={}, records={}, size={}KB",
                     fileName, dataList != null ? dataList.size() : 0, fileSize / 1024);

        } catch (Exception e) {
            log.error("[文件导出] PDF导出失败: taskId={}", taskId, e);
            updateTaskStatus(taskId, "failed", null, null, null, "PDF导出失败: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    log.error("[文件导出] 关闭PDF Writer失败", e);
                }
            }
        }
    }

    /**
     * 更新导出任务状态
     */
    private void updateTaskStatus(String taskId, String status, String fileName,
                                   String fileUrl, Long fileSize, String errorMessage) {
        try {
            ExportTaskEntity taskEntity = exportTaskDao.selectById(taskId);
            if (taskEntity != null) {
                taskEntity.setStatus(status);
                taskEntity.setFileName(fileName);
                taskEntity.setFileUrl(fileUrl);
                taskEntity.setFileSize(fileSize);
                taskEntity.setErrorMessage(errorMessage);
                taskEntity.setCompleteTime(LocalDateTime.now());
                exportTaskDao.updateById(taskEntity);
            }
        } catch (Exception e) {
            log.error("[文件导出] 更新任务状态失败: taskId={}", taskId, e);
        }
    }

    /**
     * 获取导出文件路径
     */
    public String getExportFilePath() {
        return exportFilePath;
    }
}

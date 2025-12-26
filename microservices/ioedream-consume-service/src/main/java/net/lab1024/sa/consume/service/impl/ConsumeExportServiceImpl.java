package net.lab1024.sa.consume.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.domain.excel.ConsumeAnalysisExcelModel;
import net.lab1024.sa.consume.domain.excel.ConsumeRecordExcelModel;
import net.lab1024.sa.consume.domain.form.ConsumptionAnalysisQueryForm;
import net.lab1024.sa.consume.domain.vo.*;
import net.lab1024.sa.consume.service.ConsumeAnalysisService;
import net.lab1024.sa.consume.service.ConsumeExportService;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 消费数据导出服务实现
 * <p>
 * 使用iText生成PDF报告，EasyExcel生成Excel报告
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Service
public class ConsumeExportServiceImpl implements ConsumeExportService {

    private final ConsumeAnalysisService analysisService;
    private final ConsumeRecordDao recordDao;

    // 数字格式化
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

    public ConsumeExportServiceImpl(ConsumeAnalysisService analysisService,
                                     ConsumeRecordDao recordDao) {
        this.analysisService = analysisService;
        this.recordDao = recordDao;
    }

    @Override
    public void exportAnalysisPdf(ConsumptionAnalysisQueryForm queryForm, HttpServletResponse response)
            throws IOException {
        log.info("[导出服务] 导出消费分析PDF: userId={}, period={}",
                queryForm.getUserId(), queryForm.getPeriod());

        try {
            // 获取分析数据
            ConsumptionAnalysisVO analysis = analysisService.getConsumptionAnalysis(queryForm);

            // 创建PDF文档
            Document document = new Document(PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            // 设置响应头
            String fileName = String.format("消费分析报告_%s_%s.pdf",
                    queryForm.getUserId(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 创建PDF写入器
            PdfWriter.getInstance(document, response.getOutputStream());

            // 打开文档
            document.open();

            // 加载中文字体（使用系统字体）
            BaseFont baseFont = BaseFont.createFont(
                    "STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 24, Font.BOLD);
            Font headingFont = new Font(baseFont, 16, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            Font smallFont = new Font(baseFont, 10, Font.NORMAL);

            // 添加标题
            Paragraph title = new Paragraph("消费分析报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // 添加报告信息
            Paragraph reportInfo = new Paragraph(
                    String.format("用户ID: %d  |  时间周期: %s  |  生成时间: %s",
                            queryForm.getUserId(),
                            getPeriodLabel(queryForm.getPeriod()),
                            LocalDateTime.now().format(DATE_FORMATTER)),
                    smallFont);
            reportInfo.setSpacingAfter(20f);
            document.add(reportInfo);

            // 添加消费总览
            document.add(createSectionTitle("消费总览", headingFont));
            PdfPTable overviewTable = createOverviewTable(analysis, normalFont);
            document.add(overviewTable);
            document.add(Chunk.NEWLINE);

            // 添加消费趋势
            document.add(createSectionTitle("消费趋势", headingFont));
            Paragraph trendNote = new Paragraph(
                    "注：详细趋势数据请参考Excel导出", smallFont);
            document.add(trendNote);
            document.add(Chunk.NEWLINE);

            // 添加消费分类
            document.add(createSectionTitle("消费分类占比", headingFont));
            PdfPTable categoryTable = createCategoryTable(analysis, normalFont);
            document.add(categoryTable);
            document.add(Chunk.NEWLINE);

            // 添加消费习惯
            document.add(createSectionTitle("消费习惯分析", headingFont));
            PdfPTable habitsTable = createHabitsTable(analysis, normalFont);
            document.add(habitsTable);
            document.add(Chunk.NEWLINE);

            // 添加智能推荐（TODO: 待实现getRecommendations方法）
            // if (!analysis.getRecommendations().isEmpty()) {
            //     document.add(createSectionTitle("智能推荐", headingFont));
            //     List<SmartRecommendationVO> recommendations = analysis.getRecommendations();
            //     for (SmartRecommendationVO recommend : recommendations) {
            //         Paragraph recommendPara = new Paragraph(
            //                 String.format("• %s: %s", recommend.getTitle(), recommend.getDescription()),
            //                 normalFont);
            //         recommendPara.setIndentationLeft(20f);
            //         document.add(recommendPara);
            //     }
            //     document.add(Chunk.NEWLINE);
            // }

            // 添加页脚
            Paragraph footer = new Paragraph(
                    "本报告由IOE-DREAM智慧园区系统自动生成",
                    smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30f);
            document.add(footer);

            // 关闭文档
            document.close();

            log.info("[导出服务] PDF导出成功: userId={}", queryForm.getUserId());

        } catch (Exception e) {
            log.error("[导出服务] PDF导出失败: userId={}", queryForm.getUserId(), e);
            throw new IOException("PDF导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportAnalysisExcel(ConsumptionAnalysisQueryForm queryForm, HttpServletResponse response)
            throws IOException {
        log.info("[导出服务] 导出消费分析Excel: userId={}, period={}",
                queryForm.getUserId(), queryForm.getPeriod());

        try {
            // 获取分析数据
            ConsumptionAnalysisVO analysis = analysisService.getConsumptionAnalysis(queryForm);
            List<CategoryStatsVO> categoryStats = analysisService.getCategoryStats(queryForm);

            // 转换为Excel模型
            List<ConsumeAnalysisExcelModel> excelModels = new ArrayList<>();
            BigDecimal totalAmount = categoryStats.stream()
                    .map(CategoryStatsVO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            for (CategoryStatsVO stat : categoryStats) {
                ConsumeAnalysisExcelModel model = new ConsumeAnalysisExcelModel();
                model.setCategoryName(stat.getCategoryName());
                model.setAmount(stat.getAmount());
                model.setCount(stat.getCount());
                model.setPercent(stat.getPercent().toString() + "%");

                // 计算平均每单
                if (stat.getCount() != null && stat.getCount() > 0) {
                    model.setAverageAmount(stat.getAmount().divide(
                            BigDecimal.valueOf(stat.getCount()), 2, BigDecimal.ROUND_HALF_UP));
                } else {
                    model.setAverageAmount(BigDecimal.ZERO);
                }

                excelModels.add(model);
            }

            // 设置响应头
            String fileName = String.format("消费分析报告_%s_%s.xlsx",
                    queryForm.getUserId(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), ConsumeAnalysisExcelModel.class)
                    .sheet("消费分析")
                    .doWrite(excelModels);

            log.info("[导出服务] Excel导出成功: userId={}, size={}", queryForm.getUserId(), excelModels.size());

        } catch (Exception e) {
            log.error("[导出服务] Excel导出失败: userId={}", queryForm.getUserId(), e);
            throw new IOException("Excel导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportRecordsExcel(Long userId, String period, HttpServletResponse response)
            throws IOException {
        log.info("[导出服务] 导出消费记录Excel: userId={}, period={}", userId, period);

        try {
            // 计算时间范围
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            LocalDateTime endTime = now;

            switch (period) {
                case "week":
                    startTime = now.minusDays(now.getDayOfWeek().getValue() - 1).with(LocalTime.MIN);
                    break;
                case "month":
                    startTime = now.withDayOfMonth(1).with(LocalTime.MIN);
                    break;
                case "quarter":
                    int currentMonth = now.getMonthValue();
                    int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                    startTime = now.withMonth(quarterStartMonth).withDayOfMonth(1).with(LocalTime.MIN);
                    break;
                default:
                    startTime = now.minusDays(7).with(LocalTime.MIN);
            }

            // 查询消费记录
            List<ConsumeRecordEntity> records = recordDao.selectRecordsByUserAndTime(userId, startTime, endTime);

            // 转换为Excel模型
            List<ConsumeRecordExcelModel> excelModels = new ArrayList<>();
            for (ConsumeRecordEntity record : records) {
                ConsumeRecordExcelModel model = new ConsumeRecordExcelModel();
                model.setConsumeTime(record.getCreateTime());
                model.setMerchantName(record.getMerchantName());
                model.setConsumeTypeName(record.getConsumeTypeName());
                model.setAmount(record.getAmount());
                model.setOriginalAmount(record.getOriginalAmount());
                model.setDiscountAmount(record.getDiscountAmount());
                model.setPaymentMethod(record.getPaymentMethod());
                model.setDeviceName(record.getDeviceName());
                model.setOrderNo(record.getOrderNo());
                excelModels.add(model);
            }

            // 设置响应头
            String fileName = String.format("消费记录明细_%s_%s.xlsx",
                    userId,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), ConsumeRecordExcelModel.class)
                    .sheet("消费记录")
                    .doWrite(excelModels);

            log.info("[导出服务] 消费记录导出成功: userId={}, size={}", userId, excelModels.size());

        } catch (Exception e) {
            log.error("[导出服务] 消费记录导出失败: userId={}", userId, e);
            throw new IOException("消费记录导出失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建章节标题
     */
    private Paragraph createSectionTitle(String title, Font font) {
        Paragraph paragraph = new Paragraph(title, font);
        paragraph.setSpacingBefore(15f);
        paragraph.setSpacingAfter(10f);
        return paragraph;
    }

    /**
     * 创建消费总览表格
     */
    private PdfPTable createOverviewTable(ConsumptionAnalysisVO analysis, Font font) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addTableCell(table, "总消费金额", CURRENCY_FORMAT.format(analysis.getTotalAmount()), font);
        addTableCell(table, "消费次数", analysis.getTotalCount() + " 次", font);
        addTableCell(table, "日均消费", CURRENCY_FORMAT.format(analysis.getDailyAverage()), font);
        addTableCell(table, "平均每单", CURRENCY_FORMAT.format(analysis.getAveragePerOrder()), font);
        addTableCell(table, "消费天数", analysis.getConsumeDays() + " 天", font);
        addTableCell(table, "最常时段", analysis.getMostFrequentTime(), font);

        return table;
    }

    /**
     * 创建消费分类表格
     */
    private PdfPTable createCategoryTable(ConsumptionAnalysisVO analysis, Font font) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        // 表头
        addTableHeaderCell(table, "分类名称");
        addTableHeaderCell(table, "消费金额");
        addTableHeaderCell(table, "占比");

        // 数据行
        List<ConsumptionAnalysisVO.CategoryConsumptionVO> categories = analysis.getCategories();
        for (ConsumptionAnalysisVO.CategoryConsumptionVO category : categories) {
            addSingleTableCell(table, category.getName(), font);
            addSingleTableCell(table, CURRENCY_FORMAT.format(category.getAmount()), font);
            addSingleTableCell(table, category.getPercent() + "%", font);
        }

        return table;
    }

    /**
     * 创建消费习惯表格
     */
    private PdfPTable createHabitsTable(ConsumptionAnalysisVO analysis, Font font) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        addTableCell(table, "最常消费时段", analysis.getMostFrequentTime() != null ? analysis.getMostFrequentTime() : "无", font);
        addTableCell(table, "最喜欢的品类", analysis.getFavoriteCategory() != null ? analysis.getFavoriteCategory() : "无", font);

        return table;
    }

    /**
     * 添加表格单元格（键值对）
     */
    private void addTableCell(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBackgroundColor(new BaseColor(240, 240, 240));
        labelCell.setPadding(5f);
        labelCell.setBorderWidth(1f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(5f);
        valueCell.setBorderWidth(1f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * 添加单个表格单元格
     */
    private void addSingleTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setBorderWidth(1f);
        table.addCell(cell);
    }

    /**
     * 添加表头单元格
     */
    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        cell.setBackgroundColor(new BaseColor(102, 126, 234));
        cell.setBorderColor(new BaseColor(102, 126, 234));
        cell.setPadding(8f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /**
     * 获取周期标签
     */
    private String getPeriodLabel(String period) {
        switch (period) {
            case "week":
                return "本周";
            case "month":
                return "本月";
            case "quarter":
                return "本季";
            default:
                return period;
        }
    }
}

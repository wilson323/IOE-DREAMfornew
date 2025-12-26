package net.lab1024.sa.attendance.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 考勤Excel导出工具类
 * <p>
 * 基于EasyExcel实现考勤报表和统计数据的Excel导出功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
public class AttendanceExcelUtil {

    /**
     * 默认Sheet名称
     */
    private static final String DEFAULT_SHEET_NAME = "考勤数据";

    /**
     * 导出Excel到HttpServletResponse
     *
     * @param response HTTP响应
     * @param data 数据列表
     * @param clazz 实体类Class
     * @param fileName 文件名称
     */
    public static <T> void exportExcel(HttpServletResponse response, List<T> data, Class<T> clazz, String fileName) {
        try {
            // 设置响应头
            setResponseHeader(response, fileName);

            // 设置样式
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 12);
            headWriteFont.setBold(true);
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(DEFAULT_SHEET_NAME)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .doWrite(data);

            log.info("[Excel导出] 导出成功: fileName={}, rowCount={}", fileName, data.size());

        } catch (IOException e) {
            log.error("[Excel导出] 导出失败: fileName={}, error={}", fileName, e.getMessage(), e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出Excel到HttpServletResponse（指定Sheet名称）
     *
     * @param response HTTP响应
     * @param data 数据列表
     * @param clazz 实体类Class
     * @param fileName 文件名称
     * @param sheetName Sheet名称
     */
    public static <T> void exportExcel(HttpServletResponse response, List<T> data, Class<T> clazz,
                                         String fileName, String sheetName) {
        try {
            // 设置响应头
            setResponseHeader(response, fileName);

            // 设置样式
            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 12);
            headWriteFont.setBold(true);
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            // 写入Excel
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .doWrite(data);

            log.info("[Excel导出] 导出成功: fileName={}, sheetName={}, rowCount={}", fileName, sheetName, data.size());

        } catch (IOException e) {
            log.error("[Excel导出] 导出失败: fileName={}, sheetName={}, error={}", fileName, sheetName, e.getMessage(), e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置响应头
     *
     * @param response HTTP响应
     * @param fileName 文件名称
     */
    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // URL编码文件名，支持中文
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }

    /**
     * 生成导出文件名（带时间戳）
     *
     * @param prefix 文件名前缀
     * @return 文件名
     */
    public static String generateFileName(String prefix) {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + "_" + timestamp;
    }
}

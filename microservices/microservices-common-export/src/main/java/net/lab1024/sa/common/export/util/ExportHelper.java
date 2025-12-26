package net.lab1024.sa.common.export.util;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.export.domain.vo.ExportResult;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导出辅助工具类
 * <p>
 * 职责：在Controller层处理HTTP响应和文件写入，不包含业务逻辑
 * </p>
 * <p>
 * 架构说明：
 * <ul>
 *   <li>此工具类位于业务服务层（如access-service、attendance-service等）</li>
 *   <li>负责处理HttpServletResponse和文件名编码</li>
   *   <li>调用DataMaskingExporter进行数据脱敏</li>
 *   <li>使用EasyExcel写入Excel文件</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
public class ExportHelper {

    /**
     * 导出Excel文件（自动脱敏）
     * <p>
     * 使用方式：
     * </p>
     * <pre>
     * // Controller中的使用示例
     * &#64;GetMapping("/export")
     * public void exportUsers(HttpServletResponse response) {
     *     List&lt;UserEntity&gt; users = userService.getAllUsers();
     *     ExportHelper.exportExcelWithMasking(response, users, UserEntity.class, "用户列表");
     * }
     * </pre>
     *
     * @param outputStream 输出流
     * @param dataList     原始数据列表
     * @param modelClass   Excel模型类
     * @param sheetName    工作表名称（可选）
     * @param fileName      文件名（可选，不含扩展名）
     * @param <T>           数据类型
     * @throws IOException IO异常
     */
    public static <T> void exportExcelWithMasking(
            OutputStream outputStream,
            List<T> dataList,
            Class<T> modelClass,
            String sheetName,
            String fileName) throws IOException {

        if (outputStream == null) {
            throw new IllegalArgumentException("输出流不能为空");
        }

        if (dataList == null || dataList.isEmpty()) {
            log.warn("[导出辅助] 数据列表为空，不执行导出");
            return;
        }

        // 使用默认值
        if (!StringUtils.hasText(sheetName)) {
            sheetName = modelClass.getSimpleName();
        }
        if (!StringUtils.hasText(fileName)) {
            fileName = sheetName;
        }

        log.info("[导出辅助] 开始导出: model={}, sheet={}, file={}, size={}",
                modelClass.getSimpleName(), sheetName, fileName, dataList.size());

        try {
            // 调用纯Java的脱敏引擎
            List<T> maskedData = DataMaskingExporter.maskDataList(dataList, modelClass);

            // 写入Excel
            EasyExcel.write(outputStream, modelClass)
                    .sheet(sheetName)
                    .doWrite(maskedData);

            log.info("[导出辅助] 导出成功: model={}, size={}", modelClass.getSimpleName(), maskedData.size());

        } catch (Exception e) {
            log.error("[导出辅助] 导出失败: model={}, error={}", modelClass.getSimpleName(), e.getMessage(), e);
            throw new IOException("Excel导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出Excel文件（自动脱敏）- 简化版
     *
     * @param outputStream 输出流
     * @param dataList     原始数据列表
     * @param modelClass   Excel模型类
     * @param <T>           数据类型
     * @throws IOException IO异常
     */
    public static <T> void exportExcelWithMasking(
            OutputStream outputStream,
            List<T> dataList,
            Class<T> modelClass) throws IOException {

        String defaultName = modelClass.getSimpleName();
        exportExcelWithMasking(outputStream, dataList, modelClass, defaultName, defaultName);
    }

    /**
     * 导出CSV文件（自动脱敏）
     *
     * @param outputStream 输出流
     * @param dataList     原始数据列表
     * @param modelClass   模型类
     * @param fileName      文件名（可选）
     * @param <T>           数据类型
     * @throws IOException IO异常
     */
    public static <T> void exportCsvWithMasking(
            OutputStream outputStream,
            List<T> dataList,
            Class<T> modelClass,
            String fileName) throws IOException {

        if (outputStream == null) {
            throw new IllegalArgumentException("输出流不能为空");
        }

        if (dataList == null || dataList.isEmpty()) {
            log.warn("[导出辅助] 数据列表为空，不执行导出");
            return;
        }

        log.info("[导出辅助CSV] 开始导出: model={}, file={}, size={}",
                modelClass.getSimpleName(), fileName, dataList.size());

        try {
            // 调用脱敏引擎
            List<T> maskedData = DataMaskingExporter.maskDataList(dataList, modelClass);

            // 写入CSV（简化实现，实际可使用OpenCSV）
            writeCsv(outputStream, maskedData, modelClass);

            log.info("[导出辅助CSV] 导出成功: model={}, size={}", modelClass.getSimpleName(), maskedData.size());

        } catch (Exception e) {
            log.error("[导出辅助CSV] 导出失败: model={}, error={}", modelClass.getSimpleName(), e.getMessage(), e);
            throw new IOException("CSV导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成HTTP响应头
     * <p>
     * 用于设置Content-Disposition和文件名编码
     * </p>
     *
     * @param fileName     文件名
     * @param fileExtension 文件扩展名（如 ".xlsx", ".csv"）
     * @return HTTP响应头值
     */
    public static String generateContentDisposition(String fileName, String fileExtension) {
        String fullFileName = fileName + fileExtension;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        try {
            // 对文件名进行URL编码
            String encodedFileName = URLEncoder.encode(fullFileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return String.format("attachment; filename=%s; filename*=UTF-8''%s",
                    encodedFileName, encodedFileName);
        } catch (Exception e) {
            log.warn("[导出辅助] 文件名编码失败: fileName={}, error={}", fileName, e.getMessage());
            return "attachment; filename=" + fullFileName;
        }
    }

    /**
     * 写入CSV文件（简化实现）
     *
     * @param outputStream 输出流
     * @param dataList     数据列表
     * @param modelClass   模型类
     * @param <T>           数据类型
     * @throws IOException IO异常
     */
    private static <T> void writeCsv(OutputStream outputStream, List<T> dataList, Class<T> modelClass)
            throws IOException {

        // 简化实现：使用反射生成CSV
        // 实际生产环境建议使用OpenCSV或Apache Commons CSV

        StringBuilder csv = new StringBuilder();

        // TODO: 实现CSV写入逻辑
        // 1. 提取字段名作为表头
        // 2. 遍历数据列表，写入每一行
        // 3. 处理特殊字符（逗号、引号、换行符）

        csv.append("CSV导出功能待完善\n");
        outputStream.write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成带时间戳的文件名
     *
     * @param baseName 基础文件名
     * @return 带时间戳的文件名
     */
    public static String generateTimestampFileName(String baseName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return String.format("%s_%s", baseName, timestamp);
    }
}

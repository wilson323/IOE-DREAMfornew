package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据导出格式枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义导出格式，避免魔法数字
 * - 提供完整的业务信息和描述
 * - 支持根据代码获取枚举实例
 * - 包含友好的显示名称
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Getter
@AllArgsConstructor
public enum DataExportFormatEnum {

    /**
     * Excel格式
     */
    EXCEL("EXCEL", "Excel格式", "Microsoft Excel表格格式(.xlsx)，支持数据分析"),

    /**
     * CSV格式
     */
    CSV("CSV", "CSV格式", "逗号分隔值格式(.csv)，通用性强，文件小"),

    /**
     * PDF格式
     */
    PDF("PDF", "PDF格式", "便携式文档格式(.pdf)，适合打印和存档"),

    /**
     * JSON格式
     */
    JSON("JSON", "JSON格式", "JavaScript对象表示法(.json)，适合程序处理"),

    /**
     * XML格式
     */
    XML("XML", "XML格式", "可扩展标记语言(.xml)，结构化数据交换"),

    /**
     * Word格式
     */
    WORD("WORD", "Word格式", "Microsoft Word文档格式(.docx)，适合报告"),

    /**
     * 图片格式
     */
    IMAGE("IMAGE", "图片格式", "图表图片格式(.png)，适合可视化展示"),

    /**
     * HTML格式
     */
    HTML("HTML", "HTML格式", "超文本标记语言(.html)，适合网页展示");

    /**
     * 导出格式代码
     */
    private final String code;

    /**
     * 导出格式名称
     */
    private final String name;

    /**
     * 导出格式描述
     */
    private final String description;

    /**
     * 根据代码获取数据导出格式枚举
     *
     * @param code 数据导出格式代码
     * @return 数据导出格式枚举，如果未找到返回null
     */
    public static DataExportFormatEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (DataExportFormatEnum format : values()) {
            if (format.getCode().equals(code.trim())) {
                return format;
            }
        }
        return null;
    }

    /**
     * 根据名称获取数据导出格式枚举
     *
     * @param name 数据导出格式名称
     * @return 数据导出格式枚举，如果未找到返回null
     */
    public static DataExportFormatEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (DataExportFormatEnum format : values()) {
            if (format.getName().equals(name.trim())) {
                return format;
            }
        }
        return null;
    }

    /**
     * 获取文件扩展名
     *
     * @return 文件扩展名
     */
    public String getFileExtension() {
        switch (this) {
            case EXCEL:
                return ".xlsx";
            case CSV:
                return ".csv";
            case PDF:
                return ".pdf";
            case JSON:
                return ".json";
            case XML:
                return ".xml";
            case WORD:
                return ".docx";
            case IMAGE:
                return ".png";
            case HTML:
                return ".html";
            default:
                return ".txt";
        }
    }

    /**
     * 获取MIME类型
     *
     * @return MIME类型
     */
    public String getContentType() {
        switch (this) {
            case EXCEL:
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV:
                return "text/csv";
            case PDF:
                return "application/pdf";
            case JSON:
                return "application/json";
            case XML:
                return "application/xml";
            case WORD:
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case IMAGE:
                return "image/png";
            case HTML:
                return "text/html";
            default:
                return "text/plain";
        }
    }

    /**
     * 判断是否为结构化数据格式
     *
     * @return 是否为结构化数据格式
     */
    public boolean isStructuredFormat() {
        return this == EXCEL || this == CSV || this == JSON || this == XML;
    }

    /**
     * 判断是否为文档格式
     *
     * @return 是否为文档格式
     */
    public boolean isDocumentFormat() {
        return this == PDF || this == WORD || this == HTML;
    }

    /**
     * 判断是否为可视化格式
     *
     * @return 是否为可视化格式
     */
    public boolean isVisualFormat() {
        return this == IMAGE || this == PDF;
    }

    /**
     * 判断是否支持大数据量
     *
     * @return 是否支持大数据量
     */
    public boolean supportsLargeData() {
        return this == CSV || this == JSON || this == XML;
    }

    /**
     * 判断是否支持复杂格式
     *
     * @return 是否支持复杂格式
     */
    public boolean supportsComplexFormatting() {
        return this == EXCEL || this == PDF || this == WORD;
    }

    /**
     * 获取推荐的数据量范围
     *
     * @return 推荐的数据量范围描述
     */
    public String getRecommendedDataSizeRange() {
        switch (this) {
            case EXCEL:
                return "1万 - 100万条记录";
            case CSV:
                return "10万 - 1000万条记录";
            case PDF:
                return "1万 - 50万条记录";
            case JSON:
                return "1万 - 500万条记录";
            case XML:
                return "10万 - 500万条记录";
            case WORD:
                return "1千 - 10万条记录";
            case IMAGE:
                return "适合图表展示，不限记录数";
            case HTML:
                return "1万 - 100万条记录";
            default:
                return "建议不超过10万条记录";
        }
    }

    /**
     * 获取优先级（数字越大优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case EXCEL:
                return 9;
            case CSV:
                return 8;
            case PDF:
                return 7;
            case JSON:
                return 6;
            case XML:
                return 5;
            case WORD:
                return 4;
            case HTML:
                return 3;
            case IMAGE:
                return 2;
            default:
                return 1;
        }
    }

    @Override
    public String toString() {
        return String.format("DataExportFormatEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}
package net.lab1024.sa.base.common.device.domain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出结果类
 * 用于封装文件导出操作的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult {

    /**
     * 导出是否成功
     */
    @Builder.Default
    private boolean success = false;

    /**
     * 错误信息（失败时使用）
     */
    private String errorMessage;

    /**
     * 导出文件名
     */
    private String fileName;

    /**
     * 导出文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Builder.Default
    private long fileSize = 0L;

    /**
     * 导出数据行数
     */
    @Builder.Default
    private long totalRows = 0L;

    /**
     * 导出耗时（毫秒）
     */
    @Builder.Default
    private long processingTime = 0L;

    /**
     * 文件格式（excel, csv, pdf等）
     */
    private String fileFormat;

    /**
     * 下载URL（可选）
     */
    private String downloadUrl;

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static ExportResult failure(String errorMessage) {
        return ExportResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建成功结果
     *
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @return 成功结果
     */
    public static ExportResult success(String fileName, long fileSize) {
        return ExportResult.builder()
                .success(true)
                .fileName(fileName)
                .fileSize(fileSize)
                .build();
    }

    /**
     * 创建成功结果（完整信息）
     *
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @param totalRows 数据行数
     * @param processingTime 处理耗时
     * @param fileFormat 文件格式
     * @return 成功结果
     */
    public static ExportResult success(String fileName, String filePath, long fileSize,
                                     long totalRows, long processingTime, String fileFormat) {
        return ExportResult.builder()
                .success(true)
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(fileSize)
                .totalRows(totalRows)
                .processingTime(processingTime)
                .fileFormat(fileFormat)
                .build();
    }

    /**
     * 创建成功结果（带下载链接）
     *
     * @param fileName 文件名
     * @param filePath 文件路径
     * @param fileSize 文件大小
     * @param downloadUrl 下载链接
     * @return 成功结果
     */
    public static ExportResult success(String fileName, String filePath, long fileSize, String downloadUrl) {
        return ExportResult.builder()
                .success(true)
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(fileSize)
                .downloadUrl(downloadUrl)
                .build();
    }

    /**
     * 获取格式化的文件大小
     *
     * @return 格式化后的文件大小（如：1.2 MB）
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取消息（兼容方法）
     * @return 消息内容
     */
    public String getMessage() {
        return errorMessage;
    }

    /**
     * 设置消息（兼容方法）
     * @param message 消息内容
     */
    public void setMessage(String message) {
        this.errorMessage = message;
    }
}
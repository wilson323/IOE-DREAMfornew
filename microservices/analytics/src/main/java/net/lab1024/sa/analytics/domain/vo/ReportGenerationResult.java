package net.lab1024.sa.analytics.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表生成结果
 *
 * @author IOE-DREAM Team
 */
@Data
public class ReportGenerationResult {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件格式
     */
    private String fileFormat;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 预览URL
     */
    private String previewUrl;

    /**
     * 生成状态：pending, processing, completed, failed, cancelled
     */
    private String status;

    /**
     * 生成进度（0-100）
     */
    private Integer progress;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;

    /**
     * 数据行数
     */
    private Integer dataRowCount;

    /**
     * 数据列数
     */
    private Integer dataColumnCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 扩展信息
     */
    private Map<String, Object> extendInfo;

    /**
     * 创建成功的静态方法
     */
    public static ReportGenerationResult success(String taskId, String fileId, String fileName,
                                                 String filePath, Long fileSize, String fileFormat) {
        ReportGenerationResult result = new ReportGenerationResult();
        result.setTaskId(taskId);
        result.setFileId(fileId);
        result.setFileName(fileName);
        result.setFilePath(filePath);
        result.setFileSize(fileSize);
        result.setFileFormat(fileFormat);
        result.setStatus("completed");
        result.setProgress(100);
        result.setCompleteTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建失败的静态方法
     */
    public static ReportGenerationResult failure(String taskId, String errorMessage) {
        ReportGenerationResult result = new ReportGenerationResult();
        result.setTaskId(taskId);
        result.setStatus("failed");
        result.setProgress(0);
        result.setErrorMessage(errorMessage);
        result.setCompleteTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建进行中的静态方法
     */
    public static ReportGenerationResult processing(String taskId, Integer progress) {
        ReportGenerationResult result = new ReportGenerationResult();
        result.setTaskId(taskId);
        result.setStatus("processing");
        result.setProgress(progress);
        return result;
    }

    /**
     * 创建待处理的静态方法
     */
    public static ReportGenerationResult pending(String taskId) {
        ReportGenerationResult result = new ReportGenerationResult();
        result.setTaskId(taskId);
        result.setStatus("pending");
        result.setProgress(0);
        result.setStartTime(LocalDateTime.now());
        return result;
    }
}
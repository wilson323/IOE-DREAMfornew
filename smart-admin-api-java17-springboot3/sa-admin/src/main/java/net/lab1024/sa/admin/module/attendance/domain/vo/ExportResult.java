package net.lab1024.sa.admin.module.attendance.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 导出结果
 * 考勤模块数据导出的返回结果
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExportResult {

    /**
     * 导出是否成功
     */
    private Boolean success;

    /**
     * 导出文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 导出记录数
     */
    private Integer recordCount;

    /**
     * 导出耗时（毫秒）
     */
    private Long exportDuration;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 导出时间
     */
    private java.time.LocalDateTime exportTime;

    /**
     * 设置错误信息
     *
     * @param message 错误信息
     */
    public void setMessage(String message) {
        this.errorMessage = message;
        this.success = false;
    }
}

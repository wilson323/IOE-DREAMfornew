package net.lab1024.sa.common.storage;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件存储响应
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 存储时间
     */
    private LocalDateTime storageTime;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 创建成功响应
     */
    public static FileStorageResponse success(String filePath, String fileUrl, Long fileSize) {
        FileStorageResponse response = new FileStorageResponse();
        response.setSuccess(true);
        response.setFilePath(filePath);
        response.setFileUrl(fileUrl);
        response.setFileSize(fileSize);
        response.setStorageTime(LocalDateTime.now());
        return response;
    }

    /**
     * 创建失败响应
     */
    public static FileStorageResponse error(String errorMessage) {
        FileStorageResponse response = new FileStorageResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setStorageTime(LocalDateTime.now());
        return response;
    }
}
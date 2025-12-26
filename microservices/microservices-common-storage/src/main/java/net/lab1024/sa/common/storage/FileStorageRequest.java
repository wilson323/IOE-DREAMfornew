package net.lab1024.sa.common.storage;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 文件存储请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageRequest {

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /**
     * 文件内容
     */
    @NotNull(message = "文件内容不能为空")
    private byte[] fileContent;

    /**
     * 文件类型 (MIME类型)
     */
    private String contentType;

    /**
     * 存储路径 (可选)
     */
    private String storagePath;
}
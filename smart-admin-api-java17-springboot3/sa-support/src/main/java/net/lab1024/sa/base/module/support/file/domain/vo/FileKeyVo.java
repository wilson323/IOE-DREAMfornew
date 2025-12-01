package net.lab1024.sa.base.module.support.file.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件Key值对象
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileKeyVo {

    /**
     * 文件key
     */
    private String fileKey;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 预览URL
     */
    private String previewUrl;
}
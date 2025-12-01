package net.lab1024.sa.base.module.support.file.domain.vo;

import lombok.Data;

/**
 * 文件视图对象
 *
 * @Author 1024创新实验室: 罗伊
 * @Date 2019-09-02 23:21:10
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Data
public class FileVO {

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
     * 文件URL
     */
    private String fileUrl;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * 上传人
     */
    private String uploadUser;
}
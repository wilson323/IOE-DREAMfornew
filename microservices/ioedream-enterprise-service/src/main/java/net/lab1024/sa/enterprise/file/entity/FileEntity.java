package net.lab1024.sa.enterprise.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 文件管理实体类
 *
 * @author 老王
 * @since 2025-11-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_info")
public class FileEntity extends BaseEntity {

    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件原始名称
     */
    private String originalFilename;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件MD5值
     */
    private String fileMd5;

    /**
     * 存储类型（LOCAL: 本地, OSS: 阿里云OSS, COS: 腾讯云COS）
     */
    private String storageType;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 上传用户名
     */
    private String uploadUserName;

    /**
     * 业务模块
     */
    private String businessModule;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 文件状态（0: 正常, 1: 已删除）
     */
    private Integer fileStatus;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 最后下载时间
     */
    private LocalDateTime lastDownloadTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否公开（0: 私有, 1: 公开）
     */
    private Integer isPublic;

    /**
     * 文件描述
     */
    private String fileDescription;

    /**
     * 标签（JSON格式）
     */
    private String tags;

}
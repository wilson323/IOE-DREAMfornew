package net.lab1024.sa.enterprise.file.service;

import net.lab1024.sa.enterprise.file.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件管理服务接口
 *
 * @author 老王
 * @since 2025-11-30
 */
public interface FileService {

    /**
     * 上传文件
     */
    FileEntity uploadFile(MultipartFile file, String businessModule, String businessId, String fileDescription);

    /**
     * 批量上传文件
     */
    List<FileEntity> uploadFiles(List<MultipartFile> files, String businessModule, String businessId);

    /**
     * 下载文件
     */
    byte[] downloadFile(Long fileId, Long userId);

    /**
     * 获取文件信息
     */
    FileEntity getFileInfo(Long fileId);

    /**
     * 根据MD5获取文件信息
     */
    FileEntity getFileInfoByMd5(String fileMd5);

    /**
     * 删除文件
     */
    boolean deleteFile(Long fileId, Long userId);

    /**
     * 批量删除文件
     */
    boolean batchDeleteFiles(List<Long> fileIds, Long userId);

    /**
     * 获取文件列表（分页）
     */
    List<FileEntity> getFileList(String fileName, String fileType, String businessModule,
                                Long uploadUserId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的文件列表
     */
    List<FileEntity> getMyFileList(Long userId, String fileName, String fileType,
                                  Integer pageNum, Integer pageSize);

    /**
     * 根据业务模块获取文件列表
     */
    List<FileEntity> getBusinessFileList(String businessModule, String businessId);

    /**
     * 更新文件信息
     */
    boolean updateFileInfo(FileEntity fileEntity);

    /**
     * 生成文件访问URL
     */
    String generateFileUrl(Long fileId, Long userId, Integer expireHours);

    /**
     * 生成缩略图
     */
    String generateThumbnail(Long fileId);

    /**
     * 清理过期文件
     */
    int cleanExpiredFiles();

    /**
     * 获取文件统计信息
     */
    Object getFileStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);

}
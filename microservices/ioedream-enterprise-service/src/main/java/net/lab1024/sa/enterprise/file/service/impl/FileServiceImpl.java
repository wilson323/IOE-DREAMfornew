package net.lab1024.sa.enterprise.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartMd5Util;
import net.lab1024.sa.common.util.SmartStringUtil;
import net.lab1024.sa.enterprise.file.dao.FileDao;
import net.lab1024.sa.enterprise.file.entity.FileEntity;
import net.lab1024.sa.enterprise.file.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 文件管理服务实现类
 *
 * @author 老王
 * @since 2025-11-30
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileDao fileDao;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:104857600}") // 100MB
    private Long maxFileSize;

    @Value("${file.base-url:http://localhost:8080/files}")
    private String baseUrl;

    public FileServiceImpl(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileEntity uploadFile(MultipartFile file, String businessModule, String businessId, String fileDescription) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制");
        }

        try {
            // 计算文件MD5
            String fileMd5 = SmartMd5Util.md5(file.getBytes());

            // 检查文件是否已存在
            FileEntity existFile = fileDao.selectByMd5(fileMd5, 0);
            if (existFile != null) {
                log.info("文件已存在，返回现有文件信息: {}", existFile.getFileId());
                return existFile;
            }

            // 生成文件信息
            String originalFilename = file.getOriginalFilename();
            String fileExtension = SmartStringUtil.getExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + (fileExtension != null ? "." + fileExtension : "");

            // 创建存储目录
            String datePath = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filePath = Paths.get(uploadPath, datePath, fileName).toString();

            Path directory = Paths.get(uploadPath, datePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 保存文件
            Files.write(Paths.get(filePath), file.getBytes());

            // 保存文件信息到数据库
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setOriginalFilename(originalFilename);
            fileEntity.setFilePath(filePath);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFileExtension(fileExtension);
            fileEntity.setFileMd5(fileMd5);
            fileEntity.setStorageType("LOCAL");
            fileEntity.setFileUrl(baseUrl + "/" + datePath + "/" + fileName);
            fileEntity.setBusinessModule(businessModule);
            fileEntity.setBusinessId(businessId);
            fileEntity.setFileDescription(fileDescription);
            fileEntity.setFileStatus(0);
            fileEntity.setDownloadCount(0);
            fileEntity.setIsPublic(0);

            fileDao.insert(fileEntity);

            log.info("文件上传成功: {}", fileEntity.getFileId());
            return fileEntity;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileEntity> uploadFiles(List<MultipartFile> files, String businessModule, String businessId) {
        List<FileEntity> result = new ArrayList<>();
        for (MultipartFile file : files) {
            FileEntity fileEntity = uploadFile(file, businessModule, businessId, null);
            result.add(fileEntity);
        }
        return result;
    }

    @Override
    public byte[] downloadFile(Long fileId, Long userId) {
        FileEntity fileEntity = fileDao.selectById(fileId);
        if (fileEntity == null || fileEntity.getFileStatus() == 1) {
            throw new BusinessException("文件不存在或已删除");
        }

        try {
            // 更新下载次数
            fileEntity.setDownloadCount(fileEntity.getDownloadCount() + 1);
            fileEntity.setLastDownloadTime(LocalDateTime.now());
            fileDao.updateDownloadCount(fileId, fileEntity.getDownloadCount(), fileEntity.getLastDownloadTime());

            // 读取文件内容
            Path filePath = Paths.get(fileEntity.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BusinessException("文件物理文件不存在");
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("文件下载失败: {}", fileId, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public FileEntity getFileInfo(Long fileId) {
        return fileDao.selectById(fileId);
    }

    @Override
    public FileEntity getFileInfoByMd5(String fileMd5) {
        return fileDao.selectByMd5(fileMd5, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFile(Long fileId, Long userId) {
        FileEntity fileEntity = fileDao.selectById(fileId);
        if (fileEntity == null) {
            return false;
        }

        // 软删除
        fileEntity.setFileStatus(1);
        fileEntity.setUpdateTime(LocalDateTime.now());

        return fileDao.updateById(fileEntity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteFiles(List<Long> fileIds, Long userId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return false;
        }

        return fileDao.batchUpdateStatus(fileIds, 1) > 0;
    }

    @Override
    public List<FileEntity> getFileList(String fileName, String fileType, String businessModule,
                                       Long uploadUserId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<FileEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileEntity::getFileStatus, 0);

        if (SmartStringUtil.isNotEmpty(fileName)) {
            queryWrapper.like(FileEntity::getOriginalFilename, fileName);
        }
        if (SmartStringUtil.isNotEmpty(fileType)) {
            queryWrapper.eq(FileEntity::getFileType, fileType);
        }
        if (SmartStringUtil.isNotEmpty(businessModule)) {
            queryWrapper.eq(FileEntity::getBusinessModule, businessModule);
        }
        if (uploadUserId != null) {
            queryWrapper.eq(FileEntity::getUploadUserId, uploadUserId);
        }

        queryWrapper.orderByDesc(FileEntity::getCreateTime);

        Page<FileEntity> page = new Page<>(pageNum, pageSize);
        IPage<FileEntity> pageResult = fileDao.selectPage(page, queryWrapper);

        return pageResult.getRecords();
    }

    @Override
    public List<FileEntity> getMyFileList(Long userId, String fileName, String fileType,
                                          Integer pageNum, Integer pageSize) {
        return getFileList(fileName, fileType, null, userId, pageNum, pageSize);
    }

    @Override
    public List<FileEntity> getBusinessFileList(String businessModule, String businessId) {
        return fileDao.selectByBusiness(businessModule, businessId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFileInfo(FileEntity fileEntity) {
        if (fileEntity.getFileId() == null) {
            throw new BusinessException("文件ID不能为空");
        }

        FileEntity existFile = fileDao.selectById(fileEntity.getFileId());
        if (existFile == null) {
            throw new BusinessException("文件不存在");
        }

        // 只更新允许修改的字段
        FileEntity updateEntity = new FileEntity();
        updateEntity.setFileId(fileEntity.getFileId());
        updateEntity.setFileDescription(fileEntity.getFileDescription());
        updateEntity.setTags(fileEntity.getTags());
        updateEntity.setBusinessModule(fileEntity.getBusinessModule());
        updateEntity.setBusinessId(fileEntity.getBusinessId());
        updateEntity.setUpdateTime(LocalDateTime.now());

        return fileDao.updateById(updateEntity) > 0;
    }

    @Override
    public String generateFileUrl(Long fileId, Long userId, Integer expireHours) {
        FileEntity fileEntity = fileDao.selectById(fileId);
        if (fileEntity == null || fileEntity.getFileStatus() == 1) {
            throw new BusinessException("文件不存在或已删除");
        }

        // 简单实现：直接返回文件URL
        // 实际生产环境应该生成带签名的临时URL
        return fileEntity.getFileUrl();
    }

    @Override
    public String generateThumbnail(Long fileId) {
        // 缩略图生成功能
        // 这里可以根据需要实现图片缩略图生成
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredFiles() {
        List<FileEntity> expiredFiles = fileDao.selectExpiredFiles(LocalDateTime.now());
        if (expiredFiles.isEmpty()) {
            return 0;
        }

        List<Long> fileIds = new ArrayList<>();
        for (FileEntity file : expiredFiles) {
            fileIds.add(file.getFileId());

            // 删除物理文件
            try {
                Path filePath = Paths.get(file.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                log.warn("删除过期文件失败: {}", file.getFilePath(), e);
            }
        }

        // 软删除数据库记录
        return fileDao.batchUpdateStatus(fileIds, 1);
    }

    @Override
    public Object getFileStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 文件统计功能
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<FileEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileEntity::getFileStatus, 0);

        if (userId != null) {
            queryWrapper.eq(FileEntity::getUploadUserId, userId);
        }
        if (startTime != null) {
            queryWrapper.ge(FileEntity::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(FileEntity::getCreateTime, endTime);
        }

        Long totalCount = fileDao.selectCount(queryWrapper);

        // 统计文件总大小
        queryWrapper.select(FileEntity::getFileSize);
        List<FileEntity> files = fileDao.selectList(queryWrapper);
        long totalSize = files.stream().mapToLong(FileEntity::getFileSize).sum();

        statistics.put("totalCount", totalCount);
        statistics.put("totalSize", totalSize);
        statistics.put("avgSize", totalCount > 0 ? totalSize / totalCount : 0);

        return statistics;
    }

}
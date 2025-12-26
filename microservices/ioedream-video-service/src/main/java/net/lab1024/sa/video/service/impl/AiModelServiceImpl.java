package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.business.video.manager.AiModelManager;
import net.lab1024.sa.common.business.video.manager.DeviceModelSyncManager;
import net.lab1024.sa.common.entity.video.AiModelEntity;
import net.lab1024.sa.common.entity.video.DeviceModelSyncEntity;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import net.lab1024.sa.video.dao.AiModelDao;
import net.lab1024.sa.video.dao.DeviceModelSyncDao;
import net.lab1024.sa.video.domain.form.AiModelQueryForm;
import net.lab1024.sa.video.domain.form.AiModelSyncForm;
import net.lab1024.sa.video.domain.form.AiModelUploadForm;
import net.lab1024.sa.video.domain.vo.AiModelVO;
import net.lab1024.sa.video.domain.vo.DeviceModelSyncProgressVO;
import net.lab1024.sa.video.service.AiModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI模型服务实现类
 * <p>
 * 实现AI模型管理的业务逻辑：
 * 1. 模型上传和存储管理
 * 2. 模型发布和版本控制
 * 3. 设备同步进度管理
 * 4. 模型查询和删除
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AiModelServiceImpl implements AiModelService {

    private final AiModelDao aiModelDao;
    private final DeviceModelSyncDao deviceModelSyncDao;
    private final AiModelManager aiModelManager;
    private final DeviceModelSyncManager deviceModelSyncManager;
    private final FileStorageStrategy fileStorageStrategy;

    public AiModelServiceImpl(AiModelDao aiModelDao, DeviceModelSyncDao deviceModelSyncDao,
                              AiModelManager aiModelManager, DeviceModelSyncManager deviceModelSyncManager,
                              FileStorageStrategy fileStorageStrategy) {
        this.aiModelDao = aiModelDao;
        this.deviceModelSyncDao = deviceModelSyncDao;
        this.aiModelManager = aiModelManager;
        this.deviceModelSyncManager = deviceModelSyncManager;
        this.fileStorageStrategy = fileStorageStrategy;
    }

    @Override
    public Long uploadModel(MultipartFile file, AiModelUploadForm uploadForm) {
        log.info("[AI模型Service] 开始上传AI模型: fileName={}, modelName={}, modelVersion={}",
                file.getOriginalFilename(), uploadForm.getModelName(), uploadForm.getModelVersion());

        try {
            // 计算文件MD5
            String fileMd5 = calculateFileMd5(file.getInputStream());
            Long fileSize = file.getSize();

            // 验证文件大小
            if (!aiModelManager.validateFileSize(fileSize)) {
                throw new IllegalArgumentException("模型文件不能超过500MB");
            }

            // 验证模型版本格式
            if (!aiModelManager.validateModelVersion(uploadForm.getModelVersion())) {
                throw new IllegalArgumentException("模型版本格式应为x.y.z");
            }

            // 检查版本是否已存在
            LambdaQueryWrapper<AiModelEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AiModelEntity::getModelName, uploadForm.getModelName())
                    .eq(AiModelEntity::getModelVersion, uploadForm.getModelVersion())
                    .eq(AiModelEntity::getDeletedFlag, 0);
            Long count = aiModelDao.selectCount(queryWrapper);
            if (count > 0) {
                log.error("[AI模型Service] 模型版本已存在: modelName={}, modelVersion={}",
                        uploadForm.getModelName(), uploadForm.getModelVersion());
                throw new IllegalArgumentException("该模型版本已存在");
            }

            // 生成MinIO存储路径
            String minioPath = aiModelManager.generateModelPath(
                    uploadForm.getModelType(),
                    uploadForm.getModelName(),
                    uploadForm.getModelVersion()
            );

            // 上传文件到MinIO
            log.info("[AI模型Service] 上传模型文件到MinIO: minioPath={}", minioPath);
            String storedPath = fileStorageStrategy.storeFile(file, minioPath);

            // 创建模型记录
            AiModelEntity entity = new AiModelEntity();
            entity.setModelName(uploadForm.getModelName());
            entity.setModelVersion(uploadForm.getModelVersion());
            entity.setModelType(uploadForm.getModelType());
            entity.setFilePath(storedPath); // 存储MinIO返回的路径
            entity.setFileSize(fileSize);
            entity.setFileMd5(fileMd5);
            entity.setSupportedEvents(uploadForm.getSupportedEvents());
            entity.setModelStatus(0); // 草稿状态

            aiModelDao.insert(entity);

            log.info("[AI模型Service] 模型上传成功: modelId={}, modelName={}, minioPath={}",
                    entity.getModelId(), uploadForm.getModelName(), storedPath);
            return entity.getModelId();

        } catch (IllegalArgumentException e) {
            log.error("[AI模型Service] 参数验证失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[AI模型Service] AI模型上传失败: modelName={}, error={}",
                    uploadForm.getModelName(), e.getMessage(), e);
            throw new RuntimeException("AI模型上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void publishModel(Long modelId, Long userId) {
        log.info("[AI模型Service] 开始发布AI模型: modelId={}, userId={}", modelId, userId);

        AiModelEntity entity = aiModelDao.selectById(modelId);
        if (entity == null) {
            log.error("[AI模型Service] 模型不存在: modelId={}", modelId);
            throw new IllegalArgumentException("模型不存在");
        }

        if (entity.getModelStatus() != 0) {
            log.error("[AI模型Service] 模型状态不允许发布: modelId={}, status={}", modelId, entity.getModelStatus());
            throw new IllegalArgumentException("只有草稿状态的模型才能发布");
        }

        // 验证状态转换
        if (!aiModelManager.validateStatusTransition(entity.getModelStatus(), 1)) {
            throw new IllegalArgumentException("模型状态不允许转换到已发布");
        }

        // 更新模型状态为已发布
        entity.setModelStatus(1);
        entity.setPublishTime(LocalDateTime.now());
        entity.setPublishedBy(userId);

        aiModelDao.updateById(entity);

        log.info("[AI模型Service] 模型发布成功: modelId={}", modelId);
    }

    @Override
    public String syncModelToDevices(AiModelSyncForm syncForm) {
        log.info("[AI模型Service] 开始同步模型到设备: modelId={}, deviceCount={}",
                syncForm.getModelId(), syncForm.getDeviceIds().size());

        // 验证模型存在且已发布
        AiModelEntity model = aiModelDao.selectById(syncForm.getModelId());
        if (model == null) {
            log.error("[AI模型Service] 模型不存在: modelId={}", syncForm.getModelId());
            throw new IllegalArgumentException("模型不存在");
        }
        if (model.getModelStatus() != 1) {
            log.error("[AI模型Service] 模型未发布，无法同步: modelId={}, status={}",
                    syncForm.getModelId(), model.getModelStatus());
            throw new IllegalArgumentException("只有已发布的模型才能同步到设备");
        }

        // 生成同步任务ID
        String syncTaskId = deviceModelSyncManager.generateSyncTaskId();

        // 为每个设备创建同步记录
        int pendingCount = 0;
        for (String deviceId : syncForm.getDeviceIds()) {
            // 检查是否已有同步记录
            LambdaQueryWrapper<DeviceModelSyncEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceModelSyncEntity::getModelId, syncForm.getModelId())
                    .eq(DeviceModelSyncEntity::getDeviceId, deviceId);
            DeviceModelSyncEntity existing = deviceModelSyncDao.selectOne(queryWrapper);

            if (existing != null && existing.getSyncStatus() != 3) {
                log.debug("[AI模型Service] 设备已有同步记录，跳过: deviceId={}", deviceId);
                continue;
            }

            DeviceModelSyncEntity syncEntity = new DeviceModelSyncEntity();
            syncEntity.setModelId(syncForm.getModelId());
            syncEntity.setDeviceId(deviceId);
            syncEntity.setSyncStatus(0); // 待同步
            syncEntity.setSyncProgress(0);

            deviceModelSyncDao.insert(syncEntity);
            pendingCount++;
        }

        log.info("[AI模型Service] 同步任务创建成功: syncTaskId={}, pendingDevices={}", syncTaskId, pendingCount);

        // TODO: 实际的设备同步逻辑将在设备通讯模块中实现
        // 这里只是创建同步记录，实际的同步由DeviceCommService处理

        return syncTaskId;
    }

    @Override
    public IPage<AiModelEntity> queryModelList(AiModelQueryForm queryForm) {
        log.info("[AI模型Service] 查询AI模型列表: modelType={}, modelStatus={}, pageNum={}, pageSize={}",
                queryForm.getModelType(), queryForm.getModelStatus(),
                queryForm.getPageNum(), queryForm.getPageSize());

        LambdaQueryWrapper<AiModelEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (queryForm.getModelType() != null && !queryForm.getModelType().isEmpty()) {
            queryWrapper.eq(AiModelEntity::getModelType, queryForm.getModelType());
        }
        if (queryForm.getModelStatus() != null) {
            queryWrapper.eq(AiModelEntity::getModelStatus, queryForm.getModelStatus());
        }
        queryWrapper.orderByDesc(AiModelEntity::getCreateTime);

        Page<AiModelEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        IPage<AiModelEntity> result = aiModelDao.selectPage(page, queryWrapper);

        log.debug("[AI模型Service] 查询到{}个模型", result.getTotal());
        return result;
    }

    @Override
    public AiModelVO getModelDetail(Long modelId) {
        log.info("[AI模型Service] 获取AI模型详情: modelId={}", modelId);

        AiModelEntity entity = aiModelDao.selectById(modelId);
        if (entity == null) {
            log.warn("[AI模型Service] 模型不存在: modelId={}", modelId);
            throw new IllegalArgumentException("模型不存在");
        }

        return convertToVO(entity);
    }

    @Override
    public DeviceModelSyncProgressVO querySyncProgress(Long modelId) {
        log.info("[AI模型Service] 查询同步进度: modelId={}", modelId);

        LambdaQueryWrapper<DeviceModelSyncEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceModelSyncEntity::getModelId, modelId);

        List<DeviceModelSyncEntity> syncRecords = deviceModelSyncDao.selectList(queryWrapper);

        int totalDevices = syncRecords.size();
        int pendingDevices = 0;
        int syncingDevices = 0;
        int successDevices = 0;
        int failedDevices = 0;

        for (DeviceModelSyncEntity record : syncRecords) {
            switch (record.getSyncStatus()) {
                case 0 -> pendingDevices++;
                case 1 -> syncingDevices++;
                case 2 -> successDevices++;
                case 3 -> failedDevices++;
            }
        }

        int progress = deviceModelSyncManager.calculateProgress(totalDevices, successDevices + failedDevices);

        DeviceModelSyncProgressVO vo = new DeviceModelSyncProgressVO();
        vo.setTotalDevices(totalDevices);
        vo.setPendingDevices(pendingDevices);
        vo.setSyncingDevices(syncingDevices);
        vo.setSuccessDevices(successDevices);
        vo.setFailedDevices(failedDevices);
        vo.setProgress(progress);
        vo.setProgressDesc(progress + "%");

        return vo;
    }

    @Override
    public void deleteModel(Long modelId) {
        log.info("[AI模型Service] 开始删除AI模型: modelId={}", modelId);

        AiModelEntity entity = aiModelDao.selectById(modelId);
        if (entity == null) {
            log.error("[AI模型Service] 模型不存在: modelId={}", modelId);
            throw new IllegalArgumentException("模型不存在");
        }

        // 只有草稿状态的模型可以删除
        if (entity.getModelStatus() != 0) {
            log.error("[AI模型Service] 只有草稿状态的模型才能删除: modelId={}, status={}",
                    modelId, entity.getModelStatus());
            throw new IllegalArgumentException("只有草稿状态的模型才能删除");
        }

        aiModelDao.deleteById(modelId);

        log.info("[AI模型Service] 模型删除成功: modelId={}", modelId);
    }

    /**
     * 计算文件MD5
     */
    private String calculateFileMd5(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * 转换Entity为VO
     */
    private AiModelVO convertToVO(AiModelEntity entity) {
        AiModelVO vo = new AiModelVO();
        vo.setModelId(entity.getModelId());
        vo.setModelName(entity.getModelName());
        vo.setModelVersion(entity.getModelVersion());
        vo.setModelType(entity.getModelType());
        vo.setFilePath(entity.getFilePath());
        vo.setFileSize(entity.getFileSize());
        vo.setFileSizeMb(aiModelManager.formatFileSizeMb(entity.getFileSize()));
        vo.setFileMd5(entity.getFileMd5());
        vo.setSupportedEvents(entity.getSupportedEvents());
        vo.setModelStatus(entity.getModelStatus());
        vo.setModelStatusName(aiModelManager.getStatusName(entity.getModelStatus()));
        vo.setAccuracyRate(entity.getAccuracyRate());
        vo.setAccuracyPercent(aiModelManager.calculateAccuracyPercent(entity.getAccuracyRate()));
        vo.setPublishTime(entity.getPublishTime());
        vo.setPublishedBy(entity.getPublishedBy());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        return vo;
    }
}

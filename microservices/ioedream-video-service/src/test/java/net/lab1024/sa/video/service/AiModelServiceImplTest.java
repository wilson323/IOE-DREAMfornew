package net.lab1024.sa.video.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
import net.lab1024.sa.video.service.impl.AiModelServiceImpl;

/**
 * AI模型服务实现类单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AI模型管理核心业务方法
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI模型服务实现类单元测试")
class AiModelServiceImplTest {

    @Mock
    private AiModelDao aiModelDao;

    @Mock
    private DeviceModelSyncDao deviceModelSyncDao;

    @Mock
    private AiModelManager aiModelManager;

    @Mock
    private DeviceModelSyncManager deviceModelSyncManager;

    @Mock
    private FileStorageStrategy fileStorageStrategy;

    private AiModelService aiModelService;

    private AiModelEntity mockModelEntity;
    private DeviceModelSyncEntity mockSyncEntity;

    @BeforeEach
    void setUp() {
        aiModelService = new AiModelServiceImpl(
                aiModelDao,
                deviceModelSyncDao,
                aiModelManager,
                deviceModelSyncManager,
                fileStorageStrategy
        );

        // 准备测试数据
        mockModelEntity = new AiModelEntity();
        mockModelEntity.setModelId(1L);
        mockModelEntity.setModelName("fall_detection");
        mockModelEntity.setModelVersion("1.0.0");
        mockModelEntity.setModelType("YOLOv8");
        mockModelEntity.setFilePath("ai-models/YOLOv8/fall_detection/1.0.0/model.onnx");
        mockModelEntity.setFileSize(100L * 1024 * 1024); // 100MB
        mockModelEntity.setFileMd5("abc123def456");
        mockModelEntity.setSupportedEvents("FALL,ABNORMAL_GAIT");
        mockModelEntity.setModelStatus(0); // 草稿
        mockModelEntity.setAccuracyRate(new BigDecimal("0.95"));
        mockModelEntity.setCreateTime(LocalDateTime.now());
        mockModelEntity.setUpdateTime(LocalDateTime.now());

        mockSyncEntity = new DeviceModelSyncEntity();
        mockSyncEntity.setModelId(1L);
        mockSyncEntity.setDeviceId("CAM001");
        mockSyncEntity.setSyncStatus(0); // 待同步
        mockSyncEntity.setSyncProgress(0);
    }

    // ==================== 上传模型测试 ====================

    @Test
    @DisplayName("上传模型 - 成功")
    void uploadModel_Success_ReturnModelId() throws Exception {
        // Given
        byte[] fileContent = "mock model content".getBytes();
        MultipartFile file = new MockMultipartFile("model.onnx", fileContent);
        AiModelUploadForm uploadForm = new AiModelUploadForm();
        uploadForm.setModelName("fall_detection");
        uploadForm.setModelVersion("1.0.0");
        uploadForm.setModelType("YOLOv8");
        uploadForm.setSupportedEvents("FALL");

        when(aiModelManager.validateFileSize(anyLong())).thenReturn(true);
        when(aiModelManager.validateModelVersion(anyString())).thenReturn(true);
        when(aiModelDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(aiModelManager.generateModelPath(anyString(), anyString(), anyString()))
                .thenReturn("ai-models/YOLOv8/fall_detection/1.0.0/model.onnx");
        when(fileStorageStrategy.storeFile(any(MultipartFile.class), anyString()))
                .thenReturn("ai-models/YOLOv8/fall_detection/1.0.0/model.onnx");
        when(aiModelDao.insert(any(AiModelEntity.class))).thenAnswer(invocation -> {
            AiModelEntity entity = invocation.getArgument(0);
            entity.setModelId(1L);
            return 1;
        });

        // When
        Long modelId = aiModelService.uploadModel(file, uploadForm);

        // Then
        assertNotNull(modelId);
        assertEquals(1L, modelId);
        verify(aiModelDao, times(1)).insert(any(AiModelEntity.class));
        verify(fileStorageStrategy, times(1)).storeFile(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("上传模型 - 文件大小超过限制")
    void uploadModel_FileSizeExceedsLimit_ThrowException() throws Exception {
        // Given
        byte[] fileContent = new byte[501 * 1024 * 1024]; // 501MB
        MultipartFile file = new MockMultipartFile("model.onnx", fileContent);
        AiModelUploadForm uploadForm = new AiModelUploadForm();
        uploadForm.setModelName("fall_detection");
        uploadForm.setModelVersion("1.0.0");

        when(aiModelManager.validateFileSize(anyLong())).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.uploadModel(file, uploadForm)
        );
        assertTrue(exception.getMessage().contains("模型文件不能超过500MB"));
    }

    @Test
    @DisplayName("上传模型 - 版本格式无效")
    void uploadModel_InvalidVersionFormat_ThrowException() throws Exception {
        // Given
        byte[] fileContent = "mock model content".getBytes();
        MultipartFile file = new MockMultipartFile("model.onnx", fileContent);
        AiModelUploadForm uploadForm = new AiModelUploadForm();
        uploadForm.setModelName("fall_detection");
        uploadForm.setModelVersion("1.0"); // 无效格式

        when(aiModelManager.validateFileSize(anyLong())).thenReturn(true);
        when(aiModelManager.validateModelVersion(anyString())).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.uploadModel(file, uploadForm)
        );
        assertTrue(exception.getMessage().contains("模型版本格式应为x.y.z"));
    }

    @Test
    @DisplayName("上传模型 - 版本已存在")
    void uploadModel_VersionAlreadyExists_ThrowException() throws Exception {
        // Given
        byte[] fileContent = "mock model content".getBytes();
        MultipartFile file = new MockMultipartFile("model.onnx", fileContent);
        AiModelUploadForm uploadForm = new AiModelUploadForm();
        uploadForm.setModelName("fall_detection");
        uploadForm.setModelVersion("1.0.0");

        when(aiModelManager.validateFileSize(anyLong())).thenReturn(true);
        when(aiModelManager.validateModelVersion(anyString())).thenReturn(true);
        when(aiModelDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.uploadModel(file, uploadForm)
        );
        assertTrue(exception.getMessage().contains("该模型版本已存在"));
    }

    // ==================== 发布模型测试 ====================

    @Test
    @DisplayName("发布模型 - 成功")
    void publishModel_Success_UpdateStatus() {
        // Given
        Long modelId = 1L;
        Long userId = 1001L;
        mockModelEntity.setModelStatus(0); // 草稿

        when(aiModelDao.selectById(modelId)).thenReturn(mockModelEntity);
        when(aiModelManager.validateStatusTransition(eq(0), eq(1))).thenReturn(true);
        when(aiModelDao.updateById(any(AiModelEntity.class))).thenReturn(1);

        // When
        aiModelService.publishModel(modelId, userId);

        // Then
        verify(aiModelDao, times(1)).updateById(any(AiModelEntity.class));
        assertEquals(1, mockModelEntity.getModelStatus()); // 已发布
        assertNotNull(mockModelEntity.getPublishTime());
        assertEquals(userId, mockModelEntity.getPublishedBy());
    }

    @Test
    @DisplayName("发布模型 - 模型不存在")
    void publishModel_ModelNotFound_ThrowException() {
        // Given
        Long modelId = 999L;
        Long userId = 1001L;

        when(aiModelDao.selectById(modelId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.publishModel(modelId, userId)
        );
        assertTrue(exception.getMessage().contains("模型不存在"));
    }

    @Test
    @DisplayName("发布模型 - 状态不允许发布")
    void publishModel_InvalidStatusTransition_ThrowException() {
        // Given
        Long modelId = 1L;
        Long userId = 1001L;
        mockModelEntity.setModelStatus(1); // 已发布

        when(aiModelDao.selectById(modelId)).thenReturn(mockModelEntity);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.publishModel(modelId, userId)
        );
        assertTrue(exception.getMessage().contains("只有草稿状态的模型才能发布"));
    }

    // ==================== 模型同步测试 ====================

    @Test
    @DisplayName("同步模型到设备 - 成功")
    void syncModelToDevices_Success_ReturnTaskId() {
        // Given
        AiModelSyncForm syncForm = new AiModelSyncForm();
        syncForm.setModelId(1L);
        syncForm.setDeviceIds(List.of("CAM001", "CAM002"));

        mockModelEntity.setModelStatus(1); // 已发布
        when(aiModelDao.selectById(1L)).thenReturn(mockModelEntity);
        when(deviceModelSyncDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(deviceModelSyncDao.insert(any(DeviceModelSyncEntity.class))).thenReturn(1);
        when(deviceModelSyncManager.generateSyncTaskId()).thenReturn("sync_abc123");

        // When
        String taskId = aiModelService.syncModelToDevices(syncForm);

        // Then
        assertNotNull(taskId);
        assertEquals("sync_abc123", taskId);
        verify(deviceModelSyncDao, times(2)).insert(any(DeviceModelSyncEntity.class));
    }

    @Test
    @DisplayName("同步模型到设备 - 模型未发布")
    void syncModelToDevices_ModelNotPublished_ThrowException() {
        // Given
        AiModelSyncForm syncForm = new AiModelSyncForm();
        syncForm.setModelId(1L);
        syncForm.setDeviceIds(List.of("CAM001"));

        mockModelEntity.setModelStatus(0); // 草稿
        when(aiModelDao.selectById(1L)).thenReturn(mockModelEntity);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.syncModelToDevices(syncForm)
        );
        assertTrue(exception.getMessage().contains("只有已发布的模型才能同步到设备"));
    }

    @Test
    @DisplayName("同步模型到设备 - 跳过已有同步记录")
    void syncModelToDevices_SkipExistingRecords_ReturnTaskId() {
        // Given
        AiModelSyncForm syncForm = new AiModelSyncForm();
        syncForm.setModelId(1L);
        syncForm.setDeviceIds(List.of("CAM001"));

        mockModelEntity.setModelStatus(1); // 已发布
        when(aiModelDao.selectById(1L)).thenReturn(mockModelEntity);
        when(deviceModelSyncDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockSyncEntity);
        when(deviceModelSyncManager.generateSyncTaskId()).thenReturn("sync_abc123");

        // When
        String taskId = aiModelService.syncModelToDevices(syncForm);

        // Then
        assertNotNull(taskId);
        verify(deviceModelSyncDao, times(0)).insert(any(DeviceModelSyncEntity.class));
    }

    // ==================== 查询模型列表测试 ====================

    @Test
    @DisplayName("查询模型列表 - 成功")
    void queryModelList_Success_ReturnPageResult() {
        // Given
        AiModelQueryForm queryForm = new AiModelQueryForm();
        queryForm.setModelType("YOLOv8");
        queryForm.setModelStatus(0);
        queryForm.setPageNum(1);
        queryForm.setPageSize(20);

        List<AiModelEntity> modelList = new ArrayList<>();
        modelList.add(mockModelEntity);

        Page<AiModelEntity> pageResult = new Page<>(1, 20);
        pageResult.setRecords(modelList);
        pageResult.setTotal(1);

        when(aiModelDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        IPage<AiModelEntity> result = aiModelService.queryModelList(queryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(aiModelDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ==================== 获取模型详情测试 ====================

    @Test
    @DisplayName("获取模型详情 - 成功")
    void getModelDetail_Success_ReturnModelVO() {
        // Given
        Long modelId = 1L;
        when(aiModelDao.selectById(modelId)).thenReturn(mockModelEntity);
        when(aiModelManager.formatFileSizeMb(anyLong())).thenReturn("100.00");
        when(aiModelManager.calculateAccuracyPercent(any(BigDecimal.class))).thenReturn("95%");
        when(aiModelManager.getStatusName(eq(0))).thenReturn("草稿");

        // When
        AiModelVO result = aiModelService.getModelDetail(modelId);

        // Then
        assertNotNull(result);
        assertEquals(modelId, result.getModelId());
        assertEquals("fall_detection", result.getModelName());
        assertEquals("1.0.0", result.getModelVersion());
    }

    @Test
    @DisplayName("获取模型详情 - 模型不存在")
    void getModelDetail_ModelNotFound_ThrowException() {
        // Given
        Long modelId = 999L;
        when(aiModelDao.selectById(modelId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.getModelDetail(modelId)
        );
        assertTrue(exception.getMessage().contains("模型不存在"));
    }

    // ==================== 查询同步进度测试 ====================

    @Test
    @DisplayName("查询同步进度 - 成功")
    void querySyncProgress_Success_ReturnProgressVO() {
        // Given
        Long modelId = 1L;

        List<DeviceModelSyncEntity> syncRecords = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DeviceModelSyncEntity record = new DeviceModelSyncEntity();
            record.setModelId(modelId);
            record.setDeviceId("CAM00" + i);
            record.setSyncStatus(i % 4); // 0,1,2,3 循环
            syncRecords.add(record);
        }

        when(deviceModelSyncDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(syncRecords);
        when(deviceModelSyncManager.calculateProgress(eq(10), any(Integer.class))).thenReturn(30);

        // When
        DeviceModelSyncProgressVO result = aiModelService.querySyncProgress(modelId);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getTotalDevices());
        assertEquals(30, result.getProgress());
    }

    // ==================== 删除模型测试 ====================

    @Test
    @DisplayName("删除模型 - 成功")
    void deleteModel_Success_DeleteRecord() {
        // Given
        Long modelId = 1L;
        mockModelEntity.setModelStatus(0); // 草稿

        when(aiModelDao.selectById(modelId)).thenReturn(mockModelEntity);
        when(aiModelDao.deleteById(modelId)).thenReturn(1);

        // When
        aiModelService.deleteModel(modelId);

        // Then
        verify(aiModelDao, times(1)).deleteById(modelId);
    }

    @Test
    @DisplayName("删除模型 - 模型不存在")
    void deleteModel_ModelNotFound_ThrowException() {
        // Given
        Long modelId = 999L;
        when(aiModelDao.selectById(modelId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.deleteModel(modelId)
        );
        assertTrue(exception.getMessage().contains("模型不存在"));
    }

    @Test
    @DisplayName("删除模型 - 非草稿状态")
    void deleteModel_NonDraftStatus_ThrowException() {
        // Given
        Long modelId = 1L;
        mockModelEntity.setModelStatus(1); // 已发布

        when(aiModelDao.selectById(modelId)).thenReturn(mockModelEntity);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aiModelService.deleteModel(modelId)
        );
        assertTrue(exception.getMessage().contains("只有草稿状态的模型才能删除"));
    }
}

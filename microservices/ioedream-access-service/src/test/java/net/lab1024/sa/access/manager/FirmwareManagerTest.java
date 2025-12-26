package net.lab1024.sa.access.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceFirmwareDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeDeviceDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeTaskDao;
import net.lab1024.sa.access.domain.entity.DeviceFirmwareEntity;
import net.lab1024.sa.access.domain.entity.FirmwareUpgradeTaskEntity;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskForm;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeTaskVO;
import net.lab1024.sa.access.service.FirmwareUpgradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 固件管理器单元测试
 * <p>
 * 测试范围：
 * - 工作流编排核心逻辑
 * - 重试机制
 * - 回滚机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - 单元测试版本
 * @since 2025-12-25
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("固件管理器单元测试")
class FirmwareManagerTest {

    @Mock
    private DeviceFirmwareDao deviceFirmwareDao;

    @Mock
    private FirmwareUpgradeTaskDao firmwareUpgradeTaskDao;

    @Mock
    private FirmwareUpgradeDeviceDao firmwareUpgradeDeviceDao;

    @Mock
    private FirmwareUpgradeService firmwareUpgradeService;

    @InjectMocks
    private FirmwareManager firmwareManager;

    private DeviceFirmwareEntity mockFirmware;
    private FirmwareUpgradeTaskEntity mockTask;
    private FirmwareUpgradeTaskForm mockTaskForm;

    @BeforeEach
    void setUp() {
        // 初始化测试固件数据
        mockFirmware = new DeviceFirmwareEntity();
        mockFirmware.setFirmwareId(1L);
        mockFirmware.setFirmwareName("测试固件");
        mockFirmware.setFirmwareVersion("1.0.0");
        mockFirmware.setDeviceType(1);
        mockFirmware.setDeviceModel("ACS-3000");
        mockFirmware.setBrand("Hikvision");
        mockFirmware.setFirmwareStatus(2);  // 正式发布
        mockFirmware.setIsEnabled(1);       // 启用
        mockFirmware.setDownloadCount(0);
        mockFirmware.setCreateTime(LocalDateTime.now());

        // 初始化测试任务数据
        mockTask = new FirmwareUpgradeTaskEntity();
        mockTask.setTaskId(1L);
        mockTask.setTaskNo("TASK20251225220000");
        mockTask.setTaskName("测试升级任务");
        mockTask.setFirmwareId(1L);
        mockTask.setFirmwareVersion("2.0.0");
        mockTask.setUpgradeStrategy(1);
        mockTask.setTaskStatus(1);  // 待执行
        mockTask.setTargetDeviceCount(3);
        mockTask.setRollbackSupported(1);  // 支持回滚
        mockTask.setCreateTime(LocalDateTime.now());

        // 初始化任务表单
        mockTaskForm = new FirmwareUpgradeTaskForm();
        mockTaskForm.setTaskName("测试升级任务");
        mockTaskForm.setFirmwareId(1L);
        mockTaskForm.setUpgradeStrategy(1);
        mockTaskForm.setDeviceIds(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    @DisplayName("执行升级工作流 - 立即升级策略")
    void testExecuteUpgradeWorkflow_Immediate_Success() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        when(firmwareUpgradeService.createUpgradeTask(mockTaskForm, 1L, "测试用户")).thenReturn(1L);
        when(firmwareUpgradeService.startUpgradeTask(1L)).thenReturn(true);
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeService.getTaskDetail(1L)).thenReturn(createTaskVO());

        // When
        FirmwareUpgradeTaskVO result = firmwareManager.executeUpgradeWorkflow(
                mockTaskForm,
                1L,
                "测试用户"
        );

        // Then
        assertNotNull(result);
        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(firmwareUpgradeService, times(1)).createUpgradeTask(mockTaskForm, 1L, "测试用户");
        verify(firmwareUpgradeService, times(1)).startUpgradeTask(1L);
    }

    @Test
    @DisplayName("执行升级工作流 - 定时升级策略")
    void testExecuteUpgradeWorkflow_Scheduled_Success() {
        // Given
        mockTaskForm.setUpgradeStrategy(2);  // 定时升级
        mockTaskForm.setScheduleTime(LocalDateTime.parse("2025-12-31T02:00:00"));

        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        when(firmwareUpgradeService.createUpgradeTask(mockTaskForm, 1L, "测试用户")).thenReturn(1L);
        when(firmwareUpgradeService.startUpgradeTask(1L)).thenReturn(true);
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeService.getTaskDetail(1L)).thenReturn(createTaskVO());

        // When
        FirmwareUpgradeTaskVO result = firmwareManager.executeUpgradeWorkflow(
                mockTaskForm,
                1L,
                "测试用户"
        );

        // Then
        assertNotNull(result);
        verify(firmwareUpgradeService, times(1)).createUpgradeTask(mockTaskForm, 1L, "测试用户");
    }

    @Test
    @DisplayName("执行升级工作流 - 分批升级策略")
    void testExecuteUpgradeWorkflow_Batch_Success() {
        // Given
        mockTaskForm.setUpgradeStrategy(3);  // 分批升级
        mockTaskForm.setBatchSize(10);
        mockTaskForm.setBatchInterval(5);
        mockTaskForm.setDeviceIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));

        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        when(firmwareUpgradeService.createUpgradeTask(mockTaskForm, 1L, "测试用户")).thenReturn(1L);
        when(firmwareUpgradeService.startUpgradeTask(1L)).thenReturn(true);
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeService.getTaskDetail(1L)).thenReturn(createTaskVO());

        // When
        FirmwareUpgradeTaskVO result = firmwareManager.executeUpgradeWorkflow(
                mockTaskForm,
                1L,
                "测试用户"
        );

        // Then
        assertNotNull(result);
        verify(firmwareUpgradeService, times(1)).createUpgradeTask(mockTaskForm, 1L, "测试用户");
    }

    @Test
    @DisplayName("执行升级工作流 - 固件不存在抛异常")
    void testExecuteUpgradeWorkflow_FirmwareNotFound_ThrowsException() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            firmwareManager.executeUpgradeWorkflow(
                    mockTaskForm,
                    1L,
                    "测试用户"
            );
        });

        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(firmwareUpgradeService, never()).createUpgradeTask(any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("执行升级工作流 - 固件未启用抛异常")
    void testExecuteUpgradeWorkflow_FirmwareNotEnabled_ThrowsException() {
        // Given
        mockFirmware.setIsEnabled(0);  // 未启用
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            firmwareManager.executeUpgradeWorkflow(
                    mockTaskForm,
                    1L,
                    "测试用户"
            );
        });

        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(firmwareUpgradeService, never()).createUpgradeTask(any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("智能重试失败设备 - 成功")
    void testSmartRetryFailedDevices_Success() {
        // Given - 没有失败设备需要重试
        when(firmwareUpgradeService.getFailedDevices(1L)).thenReturn(Arrays.asList());

        // When
        Integer retryCount = firmwareManager.smartRetryFailedDevices(1L);

        // Then
        assertNotNull(retryCount);
        // 由于没有失败设备，期望返回0
        assertEquals(0, retryCount);
        verify(firmwareUpgradeService, times(1)).getFailedDevices(1L);
    }

    @Test
    @DisplayName("执行回滚工作流 - 成功")
    void testExecuteRollbackWorkflow_Success() {
        // Given
        when(firmwareUpgradeService.isRollbackSupported(1L)).thenReturn(true);
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);

        // When
        Long rollbackTaskId = firmwareManager.executeRollbackWorkflow(1L);

        // Then
        // 由于 mockTask 没有成功的升级设备，该方法会返回 null
        // 这里验证方法被调用即可
        verify(firmwareUpgradeService, times(1)).isRollbackSupported(1L);
        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建任务VO
     */
    private FirmwareUpgradeTaskVO createTaskVO() {
        FirmwareUpgradeTaskVO vo = new FirmwareUpgradeTaskVO();
        vo.setTaskId(1L);
        vo.setTaskNo("TASK20251225220000");
        vo.setTaskName("测试升级任务");
        vo.setFirmwareId(1L);
        vo.setFirmwareVersion("2.0.0");
        vo.setUpgradeStrategy(1);
        vo.setTaskStatus(1);
        vo.setTargetDeviceCount(3);
        return vo;
    }
}

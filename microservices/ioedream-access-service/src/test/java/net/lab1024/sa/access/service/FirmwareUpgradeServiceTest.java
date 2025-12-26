package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.dao.DeviceFirmwareDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeDeviceDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeTaskDao;
import net.lab1024.sa.common.entity.access.DeviceFirmwareEntity;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeDeviceEntity;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeTaskEntity;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskForm;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskQueryForm;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeDeviceVO;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeTaskVO;
import net.lab1024.sa.access.service.impl.FirmwareUpgradeServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 固件升级服务单元测试
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - 单元测试版本
 * @since 2025-12-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("固件升级服务单元测试")
class FirmwareUpgradeServiceTest {

    @Mock
    private FirmwareUpgradeTaskDao firmwareUpgradeTaskDao;

    @Mock
    private FirmwareUpgradeDeviceDao firmwareUpgradeDeviceDao;

    @Mock
    private DeviceFirmwareDao deviceFirmwareDao;

    @InjectMocks
    private FirmwareUpgradeServiceImpl firmwareUpgradeService;

    private DeviceFirmwareEntity mockFirmware;
    private FirmwareUpgradeTaskEntity mockTask;
    private FirmwareUpgradeTaskForm mockTaskForm;

    @BeforeEach
    void setUp() {
        // 初始化测试固件数据
        mockFirmware = new DeviceFirmwareEntity();
        mockFirmware.setFirmwareId(1L);
        mockFirmware.setFirmwareName("测试固件");
        mockFirmware.setFirmwareVersion("2.0.0");
        mockFirmware.setDeviceType(1);
        mockFirmware.setCreateTime(LocalDateTime.now());

        // 初始化测试任务数据
        mockTask = new FirmwareUpgradeTaskEntity();
        mockTask.setTaskId(1L);
        mockTask.setTaskNo("TASK20251225220000");
        mockTask.setTaskName("测试升级任务");
        mockTask.setFirmwareId(1L);
        mockTask.setFirmwareVersion("2.0.0");
        mockTask.setUpgradeStrategy(1);
        mockTask.setTaskStatus(1);  // 默认状态：待执行
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
    @DisplayName("分页查询升级任务 - 成功")
    void testQueryTasksPage_Success() {
        // Given
        Page<FirmwareUpgradeTaskEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockTask));
        page.setTotal(1);

        when(firmwareUpgradeTaskDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        FirmwareUpgradeTaskQueryForm queryForm = new FirmwareUpgradeTaskQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        // When
        PageResult<FirmwareUpgradeTaskVO> result = firmwareUpgradeService.queryTasksPage(queryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("测试升级任务", result.getList().get(0).getTaskName());

        verify(firmwareUpgradeTaskDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取任务详情 - 成功")
    void testGetTaskDetail_Success() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);

        // When
        FirmwareUpgradeTaskVO result = firmwareUpgradeService.getTaskDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTaskId());
        assertEquals("TASK20251225220000", result.getTaskNo());
        assertEquals("2.0.0", result.getFirmwareVersion());
        assertEquals("测试升级任务", result.getTaskName());

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("获取任务详情 - 任务不存在")
    void testGetTaskDetail_NotFound() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            firmwareUpgradeService.getTaskDetail(999L);
        });

        verify(firmwareUpgradeTaskDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("启动升级任务 - 成功")
    void testStartUpgradeTask_Success() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeTaskDao.updateById(any(FirmwareUpgradeTaskEntity.class))).thenReturn(1);

        // When
        Boolean result = firmwareUpgradeService.startUpgradeTask(1L);

        // Then
        assertTrue(result);
        assertEquals(2, mockTask.getTaskStatus()); // 升级中

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
        verify(firmwareUpgradeTaskDao, times(1)).updateById(any(FirmwareUpgradeTaskEntity.class));
    }

    @Test
    @DisplayName("启动升级任务 - 任务不存在")
    void testStartUpgradeTask_NotFound() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            firmwareUpgradeService.startUpgradeTask(999L);
        });

        verify(firmwareUpgradeTaskDao, times(1)).selectById(999L);
        verify(firmwareUpgradeTaskDao, never()).updateById(any(FirmwareUpgradeTaskEntity.class));
    }

    @Test
    @DisplayName("暂停升级任务 - 成功")
    void testPauseUpgradeTask_Success() {
        // Given
        mockTask.setTaskStatus(2); // 升级中
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeTaskDao.updateById(any(FirmwareUpgradeTaskEntity.class))).thenReturn(1);
        when(firmwareUpgradeDeviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        // When
        Boolean result = firmwareUpgradeService.pauseUpgradeTask(1L);

        // Then
        assertTrue(result);
        assertEquals(3, mockTask.getTaskStatus()); // 已暂停 (状态码3)

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
        verify(firmwareUpgradeTaskDao, times(1)).updateById(any(FirmwareUpgradeTaskEntity.class));
    }

    @Test
    @DisplayName("恢复升级任务 - 成功")
    void testResumeUpgradeTask_Success() {
        // Given
        mockTask.setTaskStatus(3); // 已暂停 (状态码3)
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeTaskDao.updateById(any(FirmwareUpgradeTaskEntity.class))).thenReturn(1);

        // When
        Boolean result = firmwareUpgradeService.resumeUpgradeTask(1L);

        // Then
        assertTrue(result);
        assertEquals(2, mockTask.getTaskStatus()); // 升级中

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
        verify(firmwareUpgradeTaskDao, times(1)).updateById(any(FirmwareUpgradeTaskEntity.class));
    }

    @Test
    @DisplayName("停止升级任务 - 成功")
    void testStopUpgradeTask_Success() {
        // Given
        mockTask.setTaskStatus(2); // 升级中
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeTaskDao.updateById(any(FirmwareUpgradeTaskEntity.class))).thenReturn(1);
        when(firmwareUpgradeDeviceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

        // When
        Boolean result = firmwareUpgradeService.stopUpgradeTask(1L);

        // Then
        assertTrue(result);
        assertEquals(4, mockTask.getTaskStatus()); // 已完成 (状态码4)

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
        verify(firmwareUpgradeTaskDao, times(1)).updateById(any(FirmwareUpgradeTaskEntity.class));
    }

    @Test
    @DisplayName("获取任务进度统计 - 成功")
    void testGetTaskProgress_Success() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);

        // Mock selectTaskStatistics方法返回统计数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", 3);
        statistics.put("successCount", 1);
        statistics.put("failedCount", 1);
        statistics.put("pendingCount", 1);
        statistics.put("upgradingCount", 0);

        when(firmwareUpgradeDeviceDao.selectTaskStatistics(1L)).thenReturn(statistics);

        // When
        Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(1L);

        // Then
        assertNotNull(progress);
        assertTrue(progress.containsKey("totalCount"));     // 总数
        assertTrue(progress.containsKey("successCount"));   // 成功数
        assertTrue(progress.containsKey("failedCount"));    // 失败数
        assertTrue(progress.containsKey("pendingCount"));   // 待处理数
        assertTrue(progress.containsKey("taskId"));         // 任务ID
        assertTrue(progress.containsKey("taskStatus"));     // 任务状态

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
        verify(firmwareUpgradeDeviceDao, times(1)).selectTaskStatistics(1L);
    }

    @Test
    @DisplayName("获取任务进度统计 - 任务不存在")
    void testGetTaskProgress_NotFound() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(999L)).thenReturn(null);

        // Mock statistics返回空统计
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", 0);
        when(firmwareUpgradeDeviceDao.selectTaskStatistics(999L)).thenReturn(statistics);

        // When & Then
        // getTaskProgress不会抛异常，即使task不存在，也会返回空的progress
        Map<String, Object> progress = firmwareUpgradeService.getTaskProgress(999L);
        assertNotNull(progress);

        verify(firmwareUpgradeTaskDao, times(1)).selectById(999L);
        verify(firmwareUpgradeDeviceDao, times(1)).selectTaskStatistics(999L);
    }

    @Test
    @DisplayName("获取任务设备列表 - 成功")
    void testGetTaskDevices_Success() {
        // Given
        List<FirmwareUpgradeDeviceEntity> deviceEntities = Arrays.asList(
                createDeviceEntity(1L, 3),
                createDeviceEntity(2L, 4)
        );

        // Mock selectByTaskId方法
        when(firmwareUpgradeDeviceDao.selectByTaskId(1L)).thenReturn(deviceEntities);

        // When
        List<FirmwareUpgradeDeviceVO> result = firmwareUpgradeService.getTaskDevices(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(vo -> vo.getTaskId().equals(1L)));

        verify(firmwareUpgradeDeviceDao, times(1)).selectByTaskId(1L);
    }

    @Test
    @DisplayName("删除升级任务 - 成功")
    void testDeleteUpgradeTask_Success() {
        // Given
        mockTask.setTaskStatus(4); // 已完成，可以删除
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);
        when(firmwareUpgradeTaskDao.deleteById(1L)).thenReturn(1);

        // When
        Boolean result = firmwareUpgradeService.deleteUpgradeTask(1L);

        // Then
        assertTrue(result);

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
        verify(firmwareUpgradeTaskDao, times(1)).deleteById(1L);
        verify(firmwareUpgradeDeviceDao, times(1)).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("删除升级任务 - 任务不存在")
    void testDeleteUpgradeTask_NotFound() {
        // Given
        when(firmwareUpgradeTaskDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            firmwareUpgradeService.deleteUpgradeTask(999L);
        });

        verify(firmwareUpgradeTaskDao, times(1)).selectById(999L);
        verify(firmwareUpgradeTaskDao, never()).deleteById(any());
        verify(firmwareUpgradeDeviceDao, never()).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("检查任务是否支持回滚 - 支持")
    void testIsRollbackSupported_Supported() {
        // Given
        mockTask.setRollbackSupported(1); // 支持回滚
        when(firmwareUpgradeTaskDao.selectById(1L)).thenReturn(mockTask);

        // When
        Boolean result = firmwareUpgradeService.isRollbackSupported(1L);

        // Then
        assertNotNull(result);
        assertTrue(result);

        verify(firmwareUpgradeTaskDao, times(1)).selectById(1L);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建设备升级实体
     */
    private FirmwareUpgradeDeviceEntity createDeviceEntity(Long deviceId, Integer upgradeStatus) {
        FirmwareUpgradeDeviceEntity entity = new FirmwareUpgradeDeviceEntity();
        entity.setDeviceId(deviceId);
        entity.setTaskId(1L);
        entity.setUpgradeStatus(upgradeStatus);
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }
}

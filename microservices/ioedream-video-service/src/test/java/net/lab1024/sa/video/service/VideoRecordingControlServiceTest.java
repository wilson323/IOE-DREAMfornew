package net.lab1024.sa.video.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.dao.VideoRecordingPlanDao;
import net.lab1024.sa.video.dao.VideoRecordingTaskDao;
import net.lab1024.sa.common.entity.video.VideoRecordingPlanEntity;
import net.lab1024.sa.common.entity.video.VideoRecordingTaskEntity;
import net.lab1024.sa.video.domain.form.VideoRecordingControlForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingTaskVO;
import net.lab1024.sa.video.manager.VideoRecordingManager;
import net.lab1024.sa.video.service.impl.VideoRecordingControlServiceImpl;

/**
 * 视频录像控制服务单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("视频录像控制服务测试")
class VideoRecordingControlServiceTest {

    @Mock
    private VideoRecordingPlanDao videoRecordingPlanDao;

    @Mock
    private VideoRecordingTaskDao videoRecordingTaskDao;

    @Mock
    private VideoRecordingManager videoRecordingManager;

    @InjectMocks
    private VideoRecordingControlServiceImpl videoRecordingControlService;

    private VideoRecordingPlanEntity mockPlan;
    private VideoRecordingTaskEntity mockTask;
    private VideoRecordingControlForm controlForm;

    @BeforeEach
    void setUp() {
        // 创建模拟录像计划
        mockPlan = new VideoRecordingPlanEntity();
        mockPlan.setPlanId(10001L);
        mockPlan.setPlanName("主入口全天录像");
        mockPlan.setPlanType(1);
        mockPlan.setDeviceId("CAM001");
        mockPlan.setChannelId(1);
        mockPlan.setRecordingType(1);
        mockPlan.setQuality(3);
        mockPlan.setEnabled(true);
        mockPlan.setStartTime(LocalDateTime.of(2025, 1, 30, 0, 0, 0));
        mockPlan.setEndTime(LocalDateTime.of(2025, 1, 30, 23, 59, 59));
        mockPlan.setStorageLocation("/recordings/cam001/");

        // 创建模拟录像任务
        mockTask = new VideoRecordingTaskEntity();
        mockTask.setTaskId(20001L);
        mockTask.setPlanId(10001L);
        mockTask.setDeviceId("CAM001");
        mockTask.setChannelId(1);
        mockTask.setStatus(VideoRecordingTaskEntity.TaskStatus.RUNNING.getCode());
        mockTask.setTriggerType(VideoRecordingTaskEntity.TriggerType.SCHEDULE.getCode());
        mockTask.setQuality(3);
        mockTask.setStartTime(LocalDateTime.of(2025, 1, 30, 9, 0, 0));
        mockTask.setFilePath("/recordings/2025/01/30/cam001_001.mp4");
        mockTask.setFileSize(104857600L);
        mockTask.setDurationSeconds(3600);

        // 创建手动录像控制表单
        controlForm = new VideoRecordingControlForm();
        controlForm.setDeviceId("CAM001");
        controlForm.setChannelId(1);
        controlForm.setOperationType(1);
        controlForm.setQuality(3);
        controlForm.setMaxDurationMinutes(120);
        controlForm.setStorageLocation("/recordings/manual/cam001/");
        controlForm.setRecordingReason("特殊情况录像");
    }

    @Test
    @DisplayName("根据计划启动录像 - 成功")
    void testStartRecordingByPlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM001")).thenReturn(null);
        when(videoRecordingTaskDao.insert(any(VideoRecordingTaskEntity.class))).thenAnswer(invocation -> {
            VideoRecordingTaskEntity task = invocation.getArgument(0);
            task.setTaskId(20001L);
            return 1;
        });

        // When
        Long taskId = videoRecordingControlService.startRecordingByPlan(10001L);

        // Then
        assertNotNull(taskId);
        assertEquals(20001L, taskId);
        verify(videoRecordingTaskDao).insert(any(VideoRecordingTaskEntity.class));
        verify(videoRecordingManager).startRecordingAsync(anyLong(), eq(mockPlan));
    }

    @Test
    @DisplayName("根据计划启动录像 - 计划不存在")
    void testStartRecordingByPlan_PlanNotFound() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.startRecordingByPlan(10001L);
        });

        assertEquals("PLAN_NOT_FOUND", exception.getCode());
        verify(videoRecordingTaskDao, never()).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("根据计划启动录像 - 计划未启用")
    void testStartRecordingByPlan_PlanDisabled() {
        // Given
        mockPlan.setEnabled(false);
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.startRecordingByPlan(10001L);
        });

        assertEquals("PLAN_DISABLED", exception.getCode());
        verify(videoRecordingTaskDao, never()).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("根据计划启动录像 - 设备正在录像")
    void testStartRecordingByPlan_DeviceRecording() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM001")).thenReturn(mockTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.startRecordingByPlan(10001L);
        });

        assertEquals("DEVICE_RECORDING", exception.getCode());
        verify(videoRecordingTaskDao, never()).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("手动启动录像 - 成功")
    void testStartManualRecording_Success() {
        // Given
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM001")).thenReturn(null);
        when(videoRecordingTaskDao.insert(any(VideoRecordingTaskEntity.class))).thenAnswer(invocation -> {
            VideoRecordingTaskEntity task = invocation.getArgument(0);
            task.setTaskId(20002L);
            return 1;
        });

        // When
        Long taskId = videoRecordingControlService.startManualRecording(controlForm);

        // Then
        assertNotNull(taskId);
        assertEquals(20002L, taskId);
        verify(videoRecordingTaskDao).insert(any(VideoRecordingTaskEntity.class));
        verify(videoRecordingManager).startManualRecordingAsync(anyLong(), eq(controlForm));
    }

    @Test
    @DisplayName("手动启动录像 - 操作类型错误")
    void testStartManualRecording_InvalidOperation() {
        // Given
        controlForm.setOperationType(2); // 停止操作

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.startManualRecording(controlForm);
        });

        assertEquals("INVALID_OPERATION", exception.getCode());
        verify(videoRecordingTaskDao, never()).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("停止录像 - 成功")
    void testStopRecording_Success() {
        // Given
        when(videoRecordingTaskDao.selectById(20001L)).thenReturn(mockTask);
        when(videoRecordingTaskDao.updateById(any(VideoRecordingTaskEntity.class))).thenReturn(1);

        // When
        Integer rows = videoRecordingControlService.stopRecording(20001L);

        // Then
        assertEquals(1, rows);
        verify(videoRecordingManager).stopRecording(20001L);
        verify(videoRecordingTaskDao).updateById(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("停止录像 - 任务不存在")
    void testStopRecording_NotFound() {
        // Given
        when(videoRecordingTaskDao.selectById(20001L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.stopRecording(20001L);
        });

        assertEquals("TASK_NOT_FOUND", exception.getCode());
        verify(videoRecordingManager, never()).stopRecording(anyLong());
    }

    @Test
    @DisplayName("停止录像 - 任务未运行")
    void testStopRecording_NotRunning() {
        // Given
        mockTask.setStatus(VideoRecordingTaskEntity.TaskStatus.COMPLETED.getCode());
        when(videoRecordingTaskDao.selectById(20001L)).thenReturn(mockTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.stopRecording(20001L);
        });

        assertEquals("TASK_NOT_RUNNING", exception.getCode());
        verify(videoRecordingManager, never()).stopRecording(anyLong());
    }

    @Test
    @DisplayName("获取录像任务状态 - 成功")
    void testGetRecordingStatus_Success() {
        // Given
        when(videoRecordingTaskDao.selectById(20001L)).thenReturn(mockTask);
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);

        // When
        VideoRecordingTaskVO taskVO = videoRecordingControlService.getRecordingStatus(20001L);

        // Then
        assertNotNull(taskVO);
        assertEquals(20001L, taskVO.getTaskId());
        assertEquals("RUNNING", taskVO.getStatus());
        assertEquals("录像中", taskVO.getStatusName());
        assertEquals("主入口全天录像", taskVO.getPlanName());
        assertEquals("CAM001", taskVO.getDeviceId());
        assertTrue(taskVO.getIsRunning());
        assertFalse(taskVO.getIsCompleted());
        assertFalse(taskVO.getIsFailed());
    }

    @Test
    @DisplayName("获取设备当前录像状态 - 有运行任务")
    void testGetDeviceRecordingStatus_HasRunningTask() {
        // Given
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM001")).thenReturn(mockTask);
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);

        // When
        VideoRecordingTaskVO taskVO = videoRecordingControlService.getDeviceRecordingStatus("CAM001");

        // Then
        assertNotNull(taskVO);
        assertEquals(20001L, taskVO.getTaskId());
        assertEquals("CAM001", taskVO.getDeviceId());
    }

    @Test
    @DisplayName("获取设备当前录像状态 - 无运行任务")
    void testGetDeviceRecordingStatus_NoRunningTask() {
        // Given
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM002")).thenReturn(null);

        // When
        VideoRecordingTaskVO taskVO = videoRecordingControlService.getDeviceRecordingStatus("CAM002");

        // Then
        assertNull(taskVO);
    }

    @Test
    @DisplayName("检查设备是否正在录像 - 正在录像")
    void testIsDeviceRecording_True() {
        // Given
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM001")).thenReturn(mockTask);

        // When
        Boolean isRecording = videoRecordingControlService.isDeviceRecording("CAM001");

        // Then
        assertTrue(isRecording);
    }

    @Test
    @DisplayName("检查设备是否正在录像 - 未录像")
    void testIsDeviceRecording_False() {
        // Given
        when(videoRecordingTaskDao.selectRunningTaskByDevice("CAM002")).thenReturn(null);

        // When
        Boolean isRecording = videoRecordingControlService.isDeviceRecording("CAM002");

        // Then
        assertFalse(isRecording);
    }

    @Test
    @DisplayName("重试失败的录像任务 - 成功")
    void testRetryFailedTask_Success() {
        // Given
        VideoRecordingTaskEntity failedTask = new VideoRecordingTaskEntity();
        failedTask.setTaskId(20001L);
        failedTask.setPlanId(10001L);
        failedTask.setDeviceId("CAM001");
        failedTask.setChannelId(1);
        failedTask.setStatus(VideoRecordingTaskEntity.TaskStatus.FAILED.getCode());
        failedTask.setTriggerType(VideoRecordingTaskEntity.TriggerType.SCHEDULE.getCode());
        failedTask.setQuality(3);
        failedTask.setRetryCount(2);
        failedTask.setMaxRetryCount(5);

        when(videoRecordingTaskDao.selectById(20001L)).thenReturn(failedTask);
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingTaskDao.insert(any(VideoRecordingTaskEntity.class))).thenAnswer(invocation -> {
            VideoRecordingTaskEntity task = invocation.getArgument(0);
            task.setTaskId(20003L);
            return 1;
        });

        // When
        Long newTaskId = videoRecordingControlService.retryFailedTask(20001L);

        // Then
        assertNotNull(newTaskId);
        assertEquals(20003L, newTaskId);
        verify(videoRecordingTaskDao).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("重试失败的录像任务 - 已达最大重试次数")
    void testRetryFailedTask_MaxRetryReached() {
        // Given
        VideoRecordingTaskEntity failedTask = new VideoRecordingTaskEntity();
        failedTask.setTaskId(20001L);
        failedTask.setStatus(VideoRecordingTaskEntity.TaskStatus.FAILED.getCode());
        failedTask.setRetryCount(5);
        failedTask.setMaxRetryCount(5);

        when(videoRecordingTaskDao.selectById(20001L)).thenReturn(failedTask);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingControlService.retryFailedTask(20001L);
        });

        assertEquals("MAX_RETRY_REACHED", exception.getCode());
        verify(videoRecordingTaskDao, never()).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("处理事件触发录像 - 有匹配计划")
    void testHandleEventRecording_HasMatchingPlan() {
        // Given
        VideoRecordingPlanEntity eventPlan = new VideoRecordingPlanEntity();
        eventPlan.setPlanId(10002L);
        eventPlan.setPlanType(2); // 事件录像
        eventPlan.setDeviceId("CAM001");
        eventPlan.setChannelId(1);
        eventPlan.setEnabled(true);
        eventPlan.setQuality(3);
        eventPlan.setEventTypes("MOTION_DETECTED,FACE_DETECTED");
        eventPlan.setStorageLocation("/recordings/events/");

        when(videoRecordingPlanDao.selectEnabledPlansByDevice("CAM001"))
                .thenReturn(Arrays.asList(eventPlan));
        when(videoRecordingTaskDao.insert(any(VideoRecordingTaskEntity.class))).thenAnswer(invocation -> {
            VideoRecordingTaskEntity task = invocation.getArgument(0);
            task.setTaskId(20004L);
            return 1;
        });

        // When
        Long taskId = videoRecordingControlService.handleEventRecording("CAM001", "MOTION_DETECTED");

        // Then
        assertNotNull(taskId);
        assertEquals(20004L, taskId);
        verify(videoRecordingTaskDao).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("处理事件触发录像 - 无匹配计划")
    void testHandleEventRecording_NoMatchingPlan() {
        // Given
        when(videoRecordingPlanDao.selectEnabledPlansByDevice("CAM002")).thenReturn(Arrays.asList());

        // When
        Long taskId = videoRecordingControlService.handleEventRecording("CAM002", "MOTION_DETECTED");

        // Then
        assertNull(taskId);
        verify(videoRecordingTaskDao, never()).insert(any(VideoRecordingTaskEntity.class));
    }

    @Test
    @DisplayName("统计录像文件存储大小 - 成功")
    void testSumRecordingFileSize_Success() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 31, 23, 59, 59);
        Long expectedSize = 10737418240L; // 10GB

        when(videoRecordingTaskDao.sumFileSizeByTimeRange(startTime, endTime)).thenReturn(expectedSize);

        // When
        Long totalSize = videoRecordingControlService.sumRecordingFileSize(startTime, endTime);

        // Then
        assertNotNull(totalSize);
        assertEquals(expectedSize, totalSize);
    }

    @Test
    @DisplayName("清理过期录像任务 - 成功")
    void testCleanExpiredTasks_Success() {
        // Given
        LocalDateTime beforeDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        when(videoRecordingTaskDao.deleteCompletedTasksBefore(beforeDate)).thenReturn(50);

        // When
        Integer rows = videoRecordingControlService.cleanExpiredTasks(beforeDate);

        // Then
        assertEquals(50, rows);
        verify(videoRecordingTaskDao).deleteCompletedTasksBefore(beforeDate);
    }
}

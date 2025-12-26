package net.lab1024.sa.common.business.video.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备模型同步管理器单元测试
 * <p>
 * 目标覆盖率：>= 85%
 * 测试范围：设备模型同步的纯业务逻辑方法
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@DisplayName("设备模型同步管理器单元测试")
class DeviceModelSyncManagerTest {

    private DeviceModelSyncManager syncManager;

    @BeforeEach
    void setUp() {
        syncManager = new DeviceModelSyncManager();
    }

    // ==================== 同步任务ID生成测试 ====================

    @Test
    @DisplayName("生成同步任务ID - 返回唯一ID")
    void generateSyncTaskId_ReturnUniqueId() {
        // When
        String taskId1 = syncManager.generateSyncTaskId();
        String taskId2 = syncManager.generateSyncTaskId();

        // Then
        assertNotNull(taskId1);
        assertNotNull(taskId2);
        assertNotEquals(taskId1, taskId2);
        assertTrue(taskId1.startsWith("sync_"));
        assertTrue(taskId2.startsWith("sync_"));
        assertEquals(13, taskId1.length()); // "sync_" (5) + 8字符UUID = 13
        assertEquals(13, taskId2.length());
    }

    // ==================== 同步状态验证测试 ====================

    @Test
    @DisplayName("验证同步状态 - 待同步")
    void validateSyncStatus_Pending_ReturnTrue() {
        // When
        boolean result = syncManager.validateSyncStatus(0);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证同步状态 - 同步中")
    void validateSyncStatus_Syncing_ReturnTrue() {
        // When
        boolean result = syncManager.validateSyncStatus(1);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证同步状态 - 成功")
    void validateSyncStatus_Success_ReturnTrue() {
        // When
        boolean result = syncManager.validateSyncStatus(2);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证同步状态 - 失败")
    void validateSyncStatus_Failed_ReturnTrue() {
        // When
        boolean result = syncManager.validateSyncStatus(3);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("验证同步状态 - 空值")
    void validateSyncStatus_Null_ReturnFalse() {
        // When
        boolean result = syncManager.validateSyncStatus(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证同步状态 - 负数")
    void validateSyncStatus_Negative_ReturnFalse() {
        // When
        boolean result = syncManager.validateSyncStatus(-1);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("验证同步状态 - 超出范围")
    void validateSyncStatus_OutOfRange_ReturnFalse() {
        // When
        boolean result = syncManager.validateSyncStatus(4);

        // Then
        assertFalse(result);
    }

    // ==================== 同步状态名称获取测试 ====================

    @Test
    @DisplayName("获取同步状态名称 - 待同步")
    void getSyncStatusName_Pending_ReturnCorrectName() {
        // When
        String name = syncManager.getSyncStatusName(0);

        // Then
        assertEquals("待同步", name);
    }

    @Test
    @DisplayName("获取同步状态名称 - 同步中")
    void getSyncStatusName_Syncing_ReturnCorrectName() {
        // When
        String name = syncManager.getSyncStatusName(1);

        // Then
        assertEquals("同步中", name);
    }

    @Test
    @DisplayName("获取同步状态名称 - 成功")
    void getSyncStatusName_Success_ReturnCorrectName() {
        // When
        String name = syncManager.getSyncStatusName(2);

        // Then
        assertEquals("成功", name);
    }

    @Test
    @DisplayName("获取同步状态名称 - 失败")
    void getSyncStatusName_Failed_ReturnCorrectName() {
        // When
        String name = syncManager.getSyncStatusName(3);

        // Then
        assertEquals("失败", name);
    }

    @Test
    @DisplayName("获取同步状态名称 - 未知状态")
    void getSyncStatusName_Unknown_ReturnUnknown() {
        // When
        String name = syncManager.getSyncStatusName(99);

        // Then
        assertEquals("未知", name);
    }

    @Test
    @DisplayName("获取同步状态名称 - 空值")
    void getSyncStatusName_Null_ReturnUnknown() {
        // When
        String name = syncManager.getSyncStatusName(null);

        // Then
        assertEquals("未知", name);
    }

    // ==================== 同步进度计算测试 ====================

    @Test
    @DisplayName("计算同步进度 - 全部完成")
    void calculateProgress_AllCompleted_Return100() {
        // Given
        Integer totalDevices = 10;
        Integer completedDevices = 10;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(100, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 一半完成")
    void calculateProgress_HalfCompleted_Return50() {
        // Given
        Integer totalDevices = 10;
        Integer completedDevices = 5;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(50, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 部分完成")
    void calculateProgress_PartialCompleted_Return30() {
        // Given
        Integer totalDevices = 10;
        Integer completedDevices = 3;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(30, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 无完成")
    void calculateProgress_NoneCompleted_Return0() {
        // Given
        Integer totalDevices = 10;
        Integer completedDevices = 0;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(0, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 总设备数为空")
    void calculateProgress_NullTotal_Return0() {
        // Given
        Integer totalDevices = null;
        Integer completedDevices = 5;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(0, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 总设备数为零")
    void calculateProgress_ZeroTotal_Return0() {
        // Given
        Integer totalDevices = 0;
        Integer completedDevices = 0;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(0, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 完成数为空")
    void calculateProgress_NullCompleted_Return0() {
        // Given
        Integer totalDevices = 10;
        Integer completedDevices = null;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(0, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 两者都为空")
    void calculateProgress_BothNull_Return0() {
        // Given
        Integer totalDevices = null;
        Integer completedDevices = null;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(0, progress);
    }

    @Test
    @DisplayName("计算同步进度 - 整数除法向下取整")
    void calculateProgress_RoundDown_Return33() {
        // Given
        Integer totalDevices = 3;
        Integer completedDevices = 1;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(33, progress); // 1/3 = 0.333... -> 33%
    }

    @Test
    @DisplayName("计算同步进度 - 大数值")
    void calculateProgress_LargeNumbers_ReturnCorrect() {
        // Given
        Integer totalDevices = 1000;
        Integer completedDevices = 789;

        // When
        Integer progress = syncManager.calculateProgress(totalDevices, completedDevices);

        // Then
        assertEquals(78, progress); // 789/1000 = 78.9% -> 78%
    }
}

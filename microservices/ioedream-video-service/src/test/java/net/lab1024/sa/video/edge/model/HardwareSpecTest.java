package net.lab1024.sa.video.edge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * HardwareSpec 单元测试
 * <p>
 * 测试目标：
 * - 硬件规格字段的Getter/Setter
 * - 新添加字段的验证
 * - memorySize 和 memoryMB 的同步逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("HardwareSpec 单元测试")
class HardwareSpecTest {

    private HardwareSpec spec;

    @BeforeEach
    void setUp() {
        spec = new HardwareSpec();
    }

    @Test
    @DisplayName("测试设置CPU型号")
    void testSetCpuModel() {
        // When
        spec.setCpuModel("Intel i7-12700K");

        // Then
        assertEquals("Intel i7-12700K", spec.getCpuModel());
    }

    @Test
    @DisplayName("测试设置GPU型号")
    void testSetGpuModel() {
        // When
        spec.setGpuModel("NVIDIA RTX 4090");

        // Then
        assertEquals("NVIDIA RTX 4090", spec.getGpuModel());
    }

    @Test
    @DisplayName("测试设置内存大小（GB）-自动同步到MB")
    void testSetMemorySize_SyncToMB() {
        // Given
        assertNull(spec.getMemoryMB());

        // When
        spec.setMemorySize(16);

        // Then
        assertEquals(16, spec.getMemorySize());
        assertEquals(16384, spec.getMemoryMB()); // 16 * 1024
    }

    @Test
    @DisplayName("测试设置内存大小（GB）-不覆盖已设置的MB")
    void testSetMemorySize_NotOverrideMB() {
        // Given
        spec.setMemoryMB(8192);

        // When
        spec.setMemorySize(16);

        // Then
        assertEquals(16, spec.getMemorySize());
        assertEquals(8192, spec.getMemoryMB()); // 保持原值
    }

    @Test
    @DisplayName("测试设置存储大小")
    void testSetStorageSize() {
        // When
        spec.setStorageSize(1024);

        // Then
        assertEquals(1024, spec.getStorageSize());
    }

    @Test
    @DisplayName("测试hasGPU-有GPU")
    void testHasGPU_True() {
        // Given
        spec.setGpu(true);

        // When & Then
        assertTrue(spec.hasGPU());
    }

    @Test
    @DisplayName("测试hasGPU-无GPU")
    void testHasGPU_False() {
        // Given
        spec.setGpu(false);

        // When & Then
        assertFalse(spec.hasGPU());
    }

    @Test
    @DisplayName("测试hasGPU-GPU为null")
    void testHasGPU_Null() {
        // When & Then
        assertFalse(spec.hasGPU());
    }

    @Test
    @DisplayName("测试完整硬件规格设置")
    void testCompleteHardwareSpec() {
        // When
        spec.setCpuModel("Intel i7-12700K");
        spec.setCpuCores(12);
        spec.setGpuModel("NVIDIA RTX 4090");
        spec.setGpu(true);
        spec.setMemorySize(32);
        spec.setStorageSize(1024);

        // Then
        assertEquals("Intel i7-12700K", spec.getCpuModel());
        assertEquals(12, spec.getCpuCores());
        assertEquals("NVIDIA RTX 4090", spec.getGpuModel());
        assertTrue(spec.hasGPU());
        assertEquals(32, spec.getMemorySize());
        assertEquals(32768, spec.getMemoryMB());
        assertEquals(1024, spec.getStorageSize());
    }
}

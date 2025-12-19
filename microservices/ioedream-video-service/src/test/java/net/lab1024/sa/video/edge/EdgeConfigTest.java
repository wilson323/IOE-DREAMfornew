package net.lab1024.sa.video.edge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EdgeConfig 单元测试
 * <p>
 * 测试目标：
 * - 配置字段的Getter/Setter
 * - 默认值验证
 * - 新添加字段的验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("EdgeConfig 单元测试")
class EdgeConfigTest {

    private EdgeConfig config;

    @BeforeEach
    void setUp() {
        config = new EdgeConfig();
    }

    @Test
    @DisplayName("测试默认值")
    void testDefaultValues() {
        // Then
        assertNotNull(config.getMaxConcurrentTasks());
        assertEquals(8, config.getMaxConcurrentTasks());
        assertNotNull(config.getMaxInferenceTime());
        assertEquals(3000L, config.getMaxInferenceTime());
        assertNotNull(config.getEdgeInferenceTimeout());
        assertEquals(5000L, config.getEdgeInferenceTimeout());
        assertNotNull(config.getCloudCollaborationThreshold());
        assertEquals(0.85, config.getCloudCollaborationThreshold());
        assertNotNull(config.getModelSyncEnabled());
        assertTrue(config.getModelSyncEnabled());
        assertNotNull(config.getModelSyncTimeout());
        assertEquals(30000L, config.getModelSyncTimeout());
        assertNotNull(config.getHeartbeatInterval());
        assertEquals(10000L, config.getHeartbeatInterval());
        assertNotNull(config.getConnectionRetryCount());
        assertEquals(3, config.getConnectionRetryCount());
        assertNotNull(config.getConnectionRetryInterval());
        assertEquals(5000L, config.getConnectionRetryInterval());
    }

    @Test
    @DisplayName("测试设置最大并发任务数")
    void testSetMaxConcurrentTasks() {
        // When
        config.setMaxConcurrentTasks(16);

        // Then
        assertEquals(16, config.getMaxConcurrentTasks());
    }

    @Test
    @DisplayName("测试设置模型同步启用状态")
    void testSetModelSyncEnabled() {
        // When
        config.setModelSyncEnabled(false);

        // Then
        assertTrue(!config.getModelSyncEnabled());
    }

    @Test
    @DisplayName("测试设置模型同步超时时间")
    void testSetModelSyncTimeout() {
        // When
        config.setModelSyncTimeout(60000L);

        // Then
        assertEquals(60000L, config.getModelSyncTimeout());
    }

    @Test
    @DisplayName("测试设置心跳检测间隔")
    void testSetHeartbeatInterval() {
        // When
        config.setHeartbeatInterval(5000L);

        // Then
        assertEquals(5000L, config.getHeartbeatInterval());
    }

    @Test
    @DisplayName("测试设置连接重试次数")
    void testSetConnectionRetryCount() {
        // When
        config.setConnectionRetryCount(5);

        // Then
        assertEquals(5, config.getConnectionRetryCount());
    }

    @Test
    @DisplayName("测试设置连接重试间隔")
    void testSetConnectionRetryInterval() {
        // When
        config.setConnectionRetryInterval(10000L);

        // Then
        assertEquals(10000L, config.getConnectionRetryInterval());
    }
}

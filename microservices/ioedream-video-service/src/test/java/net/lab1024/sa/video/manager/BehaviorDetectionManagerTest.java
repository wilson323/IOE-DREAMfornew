package net.lab1024.sa.video.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 行为检测管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@DisplayName("行为检测管理器测试")
class BehaviorDetectionManagerTest {

    private BehaviorDetectionManager behaviorDetectionManager;

    @BeforeEach
    void setUp() {
        behaviorDetectionManager = new BehaviorDetectionManager();
    }

    @Test
    @DisplayName("徘徊检测 - 短时间停留不触发")
    void detectLoitering_shortDuration_shouldNotDetect() {
        LocalDateTime now = LocalDateTime.now();

        BehaviorDetectionManager.LoiteringResult result = behaviorDetectionManager.detectLoitering(
                "CAM001", "person1", 100, 100, now);

        assertNotNull(result);
        assertFalse(result.detected());
        assertEquals("person1", result.personId());
    }

    @Test
    @DisplayName("聚集检测 - 人数不足不触发")
    void detectGathering_fewPeople_shouldNotDetect() {
        List<BehaviorDetectionManager.PersonPosition> positions = List.of(
                new BehaviorDetectionManager.PersonPosition("p1", 100, 100),
                new BehaviorDetectionManager.PersonPosition("p2", 110, 110)
        );

        BehaviorDetectionManager.GatheringResult result = behaviorDetectionManager.detectGathering("CAM001", positions);

        assertNotNull(result);
        assertFalse(result.detected());
        assertEquals(2, result.personCount());
    }

    @Test
    @DisplayName("聚集检测 - 人数达到阈值触发")
    void detectGathering_manyPeople_shouldDetect() {
        List<BehaviorDetectionManager.PersonPosition> positions = List.of(
                new BehaviorDetectionManager.PersonPosition("p1", 100, 100),
                new BehaviorDetectionManager.PersonPosition("p2", 110, 110),
                new BehaviorDetectionManager.PersonPosition("p3", 105, 105),
                new BehaviorDetectionManager.PersonPosition("p4", 115, 115),
                new BehaviorDetectionManager.PersonPosition("p5", 120, 120)
        );

        BehaviorDetectionManager.GatheringResult result = behaviorDetectionManager.detectGathering("CAM001", positions);

        assertNotNull(result);
        assertTrue(result.detected());
        assertEquals(5, result.personCount());
    }

    // ============================================================
    // ⚠️ 测试删除（2025-01-30）
    // ============================================================
    // 以下测试已被删除，因为测试的方法已被删除：
    // - detectFall_shouldReturnResult()
    // - detectAbnormalBehaviors_shouldReturnList()
    //
    // 原因：这些方法违反边缘计算架构
    // 替代测试：参见 DeviceAIEventReceiverTest（待创建）
    // ============================================================

    @Test
    @DisplayName("清理过期轨迹")
    void cleanExpiredTracks_shouldNotThrowException() {
        // 先添加一些轨迹
        behaviorDetectionManager.detectLoitering("CAM001", "person1", 100, 100, LocalDateTime.now());

        // 清理过期轨迹不应抛出异常
        assertDoesNotThrow(() -> behaviorDetectionManager.cleanExpiredTracks(30));
    }
}

package net.lab1024.sa.admin.module.smart.biometric.service;

import net.lab1024.sa.admin.module.smart.biometric.engine.BiometricRecognitionEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * BiometricMonitorService 行为测试.
 *
 * @author AI
 */
class BiometricMonitorServiceTest {

    private final BiometricMonitorService monitorService = new BiometricMonitorService();

    @Test
    void shouldRefreshSnapshotAndBuildDashboard() {
        BiometricMonitorService.EngineStatusPayload payload = BiometricMonitorService.EngineStatusPayload.builder()
                .engineId("engine-A")
                .registeredAlgorithms(List.of("FACE", "FINGERPRINT"))
                .algorithmStatuses(Map.of("FACE", "READY"))
                .successfulAuthentications(120)
                .failedAuthentications(3)
                .totalProcessingTimeMs(6000)
                .averageProcessingTimeMs(50)
                .systemResourceUsage(Map.of("usedMemory", 128_000L))
                .heartbeatTime(Instant.now())
                .build();
        BiometricMonitorService.EngineSnapshot snapshot = monitorService.refreshEngineStatus(payload);
        Assertions.assertEquals("engine-A", snapshot.getEngineId());
        Assertions.assertEquals(0.9756, snapshot.getSuccessRate(), 0.0001);

        BiometricMonitorService.BiometricMonitorDashboard dashboard = monitorService.buildDashboard();
        Assertions.assertEquals(1, dashboard.getEngineCount());
        Assertions.assertTrue(dashboard.getSuccessRate() > 0.9);
    }

    @Test
    void shouldTriggerAndResolveAlerts() {
        BiometricMonitorService.BiometricAlert alert = monitorService.triggerAlert(
                "engine-B",
                "HIGH_LATENCY",
                BiometricMonitorService.AlertLevel.CRITICAL,
                "平均响应超过阈值");
        Assertions.assertEquals(BiometricMonitorService.AlertStatus.ACTIVE, alert.getStatus());
        Assertions.assertFalse(monitorService.getActiveAlerts().isEmpty());

        monitorService.resolveAlert(alert.getAlertId(), "流量已恢复");
        Assertions.assertEquals(BiometricMonitorService.AlertStatus.RESOLVED,
                monitorService.getAlerts().stream()
                        .filter(item -> item.getAlertId().equals(alert.getAlertId()))
                        .findFirst()
                        .orElseThrow()
                        .getStatus());
    }

    @Test
    void shouldAdaptEngineStatusFromRecognitionEngine() {
        BiometricRecognitionEngine.BiometricEngineStatusEntity status = new BiometricRecognitionEngine.BiometricEngineStatusEntity();
        status.setEngineId("engine-C");
        status.setRegisteredAlgorithms(List.of());
        BiometricRecognitionEngine.BiometricEngineStatistics statistics = new BiometricRecognitionEngine.BiometricEngineStatistics();
        statistics.incrementSuccessfulAuthentications();
        statistics.incrementSuccessfulAuthentications();
        statistics.incrementFailedAuthentications();
        status.setStatistics(statistics);
        status.setSystemResourceUsage(Map.of());
        monitorService.refreshEngineStatus(status);

        Assertions.assertTrue(monitorService.getLatestSnapshot("engine-C").isPresent());
    }
}


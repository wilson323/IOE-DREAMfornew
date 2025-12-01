package net.lab1024.sa.admin.module.smart.biometric.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.biometric.engine.BiometricRecognitionEngine;
import net.lab1024.sa.admin.module.smart.biometric.domain.enums.AlertLevel;

/**
 * 鐢熺墿识别寮曟搸瀹炴椂鐩戞帶涓庡憡璀︽湇鍔?
 *
 * <p>
 * 璇ユ湇鍔¤礋璐ｆ帴鏀舵潵鑷櫤鑳介棬绂佸瓙绯荤粺鐨勫績监控暟鎹? 璁＄畻瀹炴椂鐩戞帶浠〃鐩? 骞朵负外部儴璋冪敤鑰呮彁渚涘憡璀︾敓鍛藉懆鏈熺鐞嗚兘鍔涖€傚疄鐜扮洰鏍?
 *
 * <ul>
 * <li>鐩戞帶数据鍦?30 绉掑唴蹇呴』刷新, 瓒呮椂鍗宠涓鸿繃鏈?</li>
 * <li>鍛婅瑙﹀彂鍚?30 绉掑唴蹇呴』琚‘璁?瑙ｅ喅, 鍚﹀垯迁移涘叆閫炬湡列表.</li>
 * <li>发送緵缁熶竴鐨勪华琛ㄧ洏鑱氬悎数据, 鏂逛究鍓嶇钀藉湴瀹炴椂澶у睆.</li>
 * </ul>
 *
 * @author AI
 */
@Slf4j
@Service
public class BiometricMonitorService {

    private static final Duration SNAPSHOT_TTL = Duration.ofSeconds(30);
    private static final Duration ALERT_RESPONSE_SLA = Duration.ofSeconds(30);

    private final Map<String, EngineSnapshot> snapshotStore = new ConcurrentHashMap<>();
    private final Map<String, BiometricAlert> alertStore = new ConcurrentHashMap<>();

    /**
     * 浣跨敤寮曟搸鍘熷状态佸埛鏂扮洃鎺у揩鐓?
     *
     * @param status BiometricRecognitionEngine状态佸璞?     * @return 鏈€新增揩鐓?     */
    public EngineSnapshot refreshEngineStatus(
            BiometricRecognitionEngine.BiometricEngineStatusEntity status) {
        Objects.requireNonNull(status, "status");
        EngineStatusPayload payload = EngineStatusPayload.builder().engineId(status.getEngineId())
                .registeredAlgorithms(status.getRegisteredAlgorithms() == null ? List.of()
                        : status.getRegisteredAlgorithms().stream().map(Enum::name)
                                .collect(Collectors.toList()))
                .algorithmStatuses(status.getAlgorithmStatuses() == null ? Map.of()
                        : status.getAlgorithmStatuses().entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getKey().name(),
                                        entry -> entry.getValue().name())))
                .successfulAuthentications(status.getStatistics() == null ? 0
                        : status.getStatistics().getSuccessfulAuthentications())
                .failedAuthentications(status.getStatistics() == null ? 0
                        : status.getStatistics().getFailedAuthentications())
                .totalProcessingTimeMs(status.getStatistics() == null ? 0
                        : status.getStatistics().getTotalProcessingTimeMs())
                .averageProcessingTimeMs(status.getStatistics() == null ? 0
                        : status.getStatistics().getAverageProcessingTimeMs())
                .systemResourceUsage(
                        Optional.ofNullable(status.getSystemResourceUsage()).orElseGet(Map::of))
                .heartbeatTime(Instant.now()).build();
        return refreshEngineStatus(payload);
    }

    /**
     * 浣跨敤鑷畾涔夊績璺宠浇鑽峰埛鏂扮洃鎺у揩鐓?渚涘閮ˋPI璋冪敤).
     *
     * @param payload 蹇冭烦杞借嵎
     * @return 鏈€新增揩鐓?     */
    public EngineSnapshot refreshEngineStatus(EngineStatusPayload payload) {
        Objects.requireNonNull(payload, "payload");
        Instant heartbeat =
                payload.getHeartbeatTime() == null ? Instant.now() : payload.getHeartbeatTime();
        EngineSnapshot snapshot = EngineSnapshot.builder().engineId(payload.getEngineId())
                .registeredAlgorithms(List.copyOf(
                        Optional.ofNullable(payload.getRegisteredAlgorithms()).orElseGet(List::of)))
                .algorithmStatuses(
                        Optional.ofNullable(payload.getAlgorithmStatuses()).orElseGet(Map::of))
                .successfulAuthentications(payload.getSuccessfulAuthentications())
                .failedAuthentications(payload.getFailedAuthentications())
                .totalProcessingTimeMs(payload.getTotalProcessingTimeMs())
                .averageProcessingTimeMs(payload.getAverageProcessingTimeMs())
                .successRate(calculateSuccessRate(payload.getSuccessfulAuthentications(),
                        payload.getFailedAuthentications()))
                .systemResourceUsage(
                        Optional.ofNullable(payload.getSystemResourceUsage()).orElseGet(Map::of))
                .heartbeatTime(heartbeat).stale(false).build();
        snapshotStore.put(snapshot.getEngineId(), snapshot);
        return snapshot;
    }

    /**
     * 迁移斿洖鎸囧畾寮曟搸鐨勬渶新增揩鐓?
     *
     * @param engineId 寮曟搸ID
     * @return 鏈€新增揩鐓?鑻ュ凡迁移囨湡鍒欒繑鍥炵┖)
     */
    public Optional<EngineSnapshot> getLatestSnapshot(String engineId) {
        purgeExpiredSnapshots();
        EngineSnapshot snapshot = snapshotStore.get(engineId);
        if (snapshot == null) {
            return Optional.empty();
        }
        boolean stale = isSnapshotStale(snapshot);
        if (stale) {
            snapshot.setStale(true);
        }
        return Optional.of(snapshot);
    }

    /**
     * 生成鐩戞帶浠〃鐩?
     *
     * @return 浠〃鐩樹俊鎭?     */
    public BiometricMonitorDashboard buildDashboard() {
        purgeExpiredSnapshots();
        Instant now = Instant.now();
        List<EngineSnapshot> snapshots = snapshotStore.values().stream()
                .peek(snapshot -> snapshot.setStale(isSnapshotStale(snapshot)))
                .sorted((a, b) -> a.getEngineId().compareToIgnoreCase(b.getEngineId()))
                .collect(Collectors.toList());

        long totalSuccess =
                snapshots.stream().mapToLong(EngineSnapshot::getSuccessfulAuthentications).sum();
        long totalFailure =
                snapshots.stream().mapToLong(EngineSnapshot::getFailedAuthentications).sum();
        double avgProcessingTime = snapshots.isEmpty() ? 0
                : snapshots.stream().mapToLong(EngineSnapshot::getAverageProcessingTimeMs).average()
                        .orElse(0);

        BiometricMonitorDashboard dashboard = new BiometricMonitorDashboard();
        dashboard.setEngineCount(snapshots.size());
        dashboard.setStaleEngineCount(
                (int) snapshots.stream().filter(EngineSnapshot::isStale).count());
        dashboard.setActiveAlertCount((int) alertStore.values().stream()
                .filter(alert -> alert.getStatus() != AlertStatus.RESOLVED).count());
        dashboard.setAverageProcessingTimeMs(avgProcessingTime);
        dashboard.setSuccessRate(calculateSuccessRate(totalSuccess, totalFailure));
        dashboard.setGeneratedAt(now);
        dashboard.setSnapshots(Collections.unmodifiableList(snapshots));
        dashboard.setOverdueAlerts(Collections.unmodifiableList(getOverdueAlerts()));
        return dashboard;
    }

    /**
     * 获取当前所有夊憡璀?鍖呭惈宸茶В鍐?銆?     */
    public Collection<BiometricAlert> getAlerts() {
        return Collections.unmodifiableCollection(alertStore.values());
    }

    /**
     * 获取澶勪簬娲诲姩状态佺殑鍛婅銆?     */
    public List<BiometricAlert> getActiveAlerts() {
        return alertStore.values().stream()
                .filter(alert -> alert.getStatus() != AlertStatus.RESOLVED)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * 瑙﹀彂鍛婅銆?     *
     * @param engineId 寮曟搸ID
     * @param alertCode 鍛婅编码爜
     * @param level 鍛婅绾у埆
     * @param message 鍛婅鎻忚堪
     * @return 鍛婅瀵硅薄
     */
    public BiometricAlert triggerAlert(String engineId, String alertCode, AlertLevel level,
            String message) {
        Objects.requireNonNull(level, "level");
        String alertId = UUID.randomUUID().toString();
        BiometricAlert alert = new BiometricAlert();
        alert.setAlertId(alertId);
        alert.setEngineId(engineId);
        alert.setAlertCode(alertCode);
        alert.setLevel(level);
        alert.setMessage(message);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setCreatedAt(Instant.now());
        alert.setUpdatedAt(alert.getCreatedAt());
        alertStore.put(alertId, alert);
        log.warn("Biometric alert triggered: id={}, engine={}, code={}, level={}", alertId,
                engineId, alertCode, level);
        return alert;
    }

    /**
     * 确认鍛婅銆?     *
     * @param alertId 鍛婅ID
     * @param operator 处理浜?     */
    public void acknowledgeAlert(String alertId, String operator) {
        BiometricAlert alert = alertStore.get(alertId);
        if (alert == null || alert.getStatus() == AlertStatus.RESOLVED) {
            return;
        }
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setUpdatedAt(Instant.now());
        alert.setResolution(operator);
    }

    /**
     * 瑙ｅ喅鍛婅銆?     *
     * @param alertId 鍛婅ID
     * @param resolution 瑙ｅ喅说明
     */
    public void resolveAlert(String alertId, String resolution) {
        BiometricAlert alert = alertStore.get(alertId);
        if (alert == null) {
            return;
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolution(resolution);
        alert.setUpdatedAt(Instant.now());
        log.info("Biometric alert resolved: id={}, resolution={}", alertId, resolution);
    }

    /**
     * 寮曟搸状态佽浇鑽?渚涘閮ㄨ皟鐢?.
     */
    @Data
    @Builder
    public static class EngineStatusPayload {
        @NotBlank
        private String engineId;
        private List<String> registeredAlgorithms;
        private Map<String, String> algorithmStatuses;
        private long successfulAuthentications;
        private long failedAuthentications;
        private long totalProcessingTimeMs;
        private long averageProcessingTimeMs;
        private Map<String, Object> systemResourceUsage;
        @NotNull
        private Instant heartbeatTime;
    }

    @Data
    @Builder
    public static class EngineSnapshot {
        private String engineId;
        private List<String> registeredAlgorithms;
        private Map<String, String> algorithmStatuses;
        private long successfulAuthentications;
        private long failedAuthentications;
        private long totalProcessingTimeMs;
        private long averageProcessingTimeMs;
        private double successRate;
        private Map<String, Object> systemResourceUsage;
        private Instant heartbeatTime;
        private boolean stale;
    }

    @Data
    public static class BiometricMonitorDashboard {
        private int engineCount;
        private int staleEngineCount;
        private int activeAlertCount;
        private double averageProcessingTimeMs;
        private double successRate;
        private Instant generatedAt;
        private List<EngineSnapshot> snapshots = List.of();
        private List<BiometricAlert> overdueAlerts = List.of();
    }

    @Data
    public static class BiometricAlert {
        private String alertId;
        private String engineId;
        private String alertCode;
        private AlertLevel level;
        private AlertStatus status;
        private String message;
        private String resolution;
        private Instant createdAt;
        private Instant updatedAt;
    }

    public enum AlertLevel {
        INFO, WARNING, CRITICAL
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED
    }

    private void purgeExpiredSnapshots() {
        Instant now = Instant.now();
        snapshotStore.entrySet()
                .removeIf(entry -> Duration.between(entry.getValue().getHeartbeatTime(), now)
                        .compareTo(SNAPSHOT_TTL.multipliedBy(2)) > 0);
    }

    private boolean isSnapshotStale(EngineSnapshot snapshot) {
        return Duration.between(snapshot.getHeartbeatTime(), Instant.now())
                .compareTo(SNAPSHOT_TTL) > 0;
    }

    private List<BiometricAlert> getOverdueAlerts() {
        Instant now = Instant.now();
        return alertStore.values().stream()
                .filter(alert -> alert.getStatus() != AlertStatus.RESOLVED)
                .filter(alert -> Duration.between(alert.getCreatedAt(), now)
                        .compareTo(ALERT_RESPONSE_SLA) > 0)
                .collect(Collectors.toUnmodifiableList());
    }

    private double calculateSuccessRate(long success, long failure) {
        long total = success + failure;
        if (total == 0) {
            return 1.0;
        }
        return (double) success / total;
    }
}



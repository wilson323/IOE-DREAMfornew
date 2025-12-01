package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.smart.biometric.service.BiometricMonitorService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 鐢熺墿识别寮曟搸状态佷笂鎶O.
 *
 * @author AI
 */
@Data
public class BiometricEngineStatusReportVO {

    @NotBlank(message = "engineId涓嶈兘涓虹┖")
    private String engineId;

    @NotEmpty(message = "registeredAlgorithms涓嶈兘涓虹┖")
    private List<String> registeredAlgorithms;

    @NotNull(message = "algorithmStatuses涓嶈兘涓虹┖")
    private Map<String, String> algorithmStatuses;

    private long successfulAuthentications;

    private long failedAuthentications;

    private long totalProcessingTimeMs;

    private long averageProcessingTimeMs;

    @NotNull(message = "systemResourceUsage涓嶈兘涓虹┖")
    private Map<String, Object> systemResourceUsage;

    private Long heartbeatTimestamp;

    public BiometricMonitorService.EngineStatusPayload toPayload() {
        return BiometricMonitorService.EngineStatusPayload.builder()
                .engineId(engineId)
                .registeredAlgorithms(registeredAlgorithms)
                .algorithmStatuses(algorithmStatuses)
                .successfulAuthentications(successfulAuthentications)
                .failedAuthentications(failedAuthentications)
                .totalProcessingTimeMs(totalProcessingTimeMs)
                .averageProcessingTimeMs(averageProcessingTimeMs)
                .systemResourceUsage(systemResourceUsage)
                .heartbeatTime(heartbeatTimestamp == null ? Instant.now() : Instant.ofEpochMilli(heartbeatTimestamp))
                .build();
    }
}



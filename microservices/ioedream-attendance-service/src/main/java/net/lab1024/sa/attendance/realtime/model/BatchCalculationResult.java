package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量计算结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCalculationResult {
    private String batchId;
    private int totalEvents;
    private Boolean success;
    private String errorMessage;

    private LocalDateTime processingStartTime;
    private LocalDateTime processingEndTime;
    private long totalProcessingTime;

    private List<RealtimeCalculationResult> results;
    private Integer successfulEvents;
    private Integer failedEvents;
    private Boolean batchSuccessful;
}


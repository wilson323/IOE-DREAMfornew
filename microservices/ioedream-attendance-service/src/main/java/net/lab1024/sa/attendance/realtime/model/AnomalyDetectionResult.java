package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异常检测结果（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionResult {
    private String detectionId;
    private LocalDateTime detectionTime;
    private TimeRange timeRange;
    private List<Object> anomalies;
    private boolean detectionSuccessful;
    private String errorMessage;
}


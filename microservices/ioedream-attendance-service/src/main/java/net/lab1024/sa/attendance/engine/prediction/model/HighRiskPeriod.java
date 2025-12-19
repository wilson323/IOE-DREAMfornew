package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高风险时段（占位）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighRiskPeriod {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double riskScore;
}


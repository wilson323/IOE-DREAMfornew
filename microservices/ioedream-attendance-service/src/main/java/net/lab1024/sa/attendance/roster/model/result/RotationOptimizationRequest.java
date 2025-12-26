package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班优化请求
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationOptimizationRequest {
    private String systemId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String optimizationStrategy;
}


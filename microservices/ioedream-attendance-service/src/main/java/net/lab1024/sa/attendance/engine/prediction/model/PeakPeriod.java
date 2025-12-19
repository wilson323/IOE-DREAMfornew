package net.lab1024.sa.attendance.engine.prediction.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 峰值时段（占位）
 *
 * <p>用于描述预测到的业务高峰时间区间。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeakPeriod {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double intensity;
}


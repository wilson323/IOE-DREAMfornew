package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预测范围模型
 * <p>
 * 封装预测的时间范围和业务范围
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionScope {
    private LocalDate startDate;
    private LocalDate endDate;
    private String departmentId;
    private String shiftType;

    public LocalDateTime getStartTime() {
        return startDate != null ? startDate.atStartOfDay() : null;
    }

    public LocalDateTime getEndTime() {
        return endDate != null ? endDate.atTime(23, 59, 59) : null;
    }
}

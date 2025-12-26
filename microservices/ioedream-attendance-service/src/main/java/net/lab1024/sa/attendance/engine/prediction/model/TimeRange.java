package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 时间范围模型
 * <p>
 * 封装时间范围信息
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
public class TimeRange {
    private LocalDate startDate;
    private LocalDate endDate;
    private String timeUnit;

    // 添加缺失的getter方法
    public LocalDate getWorkStartTime() {
        return this.startDate;
    }

    public LocalDate getWorkEndTime() {
        return this.endDate;
    }
}

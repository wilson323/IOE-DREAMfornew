package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.roster.model.RotationSystemConfig;

/**
 * 轮班制度摘要
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationSystemSummary {
    private String systemId;
    private String systemName;
    private RotationSystemConfig.RotationSystemType systemType;
    private RotationSystemConfig.RotationCycleType cycleType;
    private Integer cycleDays;
    private Integer shiftCount;
    private Integer employeeCount;
    private Integer scheduleCount;
    private RotationSystemConfig.SystemStatus status;
    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
}

package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.roster.model.RotationSystemConfig;

/**
 * 轮班制度详情
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationSystemDetail {
    private String systemId;
    private Boolean exists;
    private RotationSystemConfig config;
    private Integer scheduleCount;
    private LocalDateTime lastUpdated;
    private LocalDateTime queryTime;
    private String errorMessage;
}

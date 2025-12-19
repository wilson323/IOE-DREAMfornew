package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班统计结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationStatisticsResult {
    private Boolean success;
    private Map<String, Object> statistics;
    private LocalDateTime statisticsTime;
    private String errorMessage;
    private String errorCode;
}

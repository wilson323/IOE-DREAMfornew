package net.lab1024.sa.attendance.realtime.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 引擎停止结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngineShutdownResult {
    private boolean success;
    private LocalDateTime shutdownTime;
    private String errorMessage;
}

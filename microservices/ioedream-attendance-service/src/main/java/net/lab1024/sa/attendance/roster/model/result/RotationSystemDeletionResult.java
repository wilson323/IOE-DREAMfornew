package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班制度删除结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationSystemDeletionResult {
    private Boolean success;
    private String systemId;
    private String message;
    private String errorMessage;
    private String errorCode;
    private Integer scheduleCount;
    private LocalDateTime deletionTime;
}

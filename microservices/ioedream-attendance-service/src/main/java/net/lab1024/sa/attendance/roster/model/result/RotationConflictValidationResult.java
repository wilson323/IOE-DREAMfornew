package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班冲突验证结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationConflictValidationResult {
    private Boolean success;
    private Boolean hasConflict;
    private List<Object> conflicts;
    private LocalDateTime validationTime;
    private String errorMessage;
    private String errorCode;
}

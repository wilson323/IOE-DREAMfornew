package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班调整结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationAdjustmentResult {
    private Boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
    private LocalDateTime adjustmentTime;
}


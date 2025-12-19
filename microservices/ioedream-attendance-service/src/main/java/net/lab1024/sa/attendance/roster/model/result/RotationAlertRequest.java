package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班预警查询请求
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationAlertRequest {
    private String systemId;
    private LocalDate date;
    private String alertType;
}

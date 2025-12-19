package net.lab1024.sa.attendance.roster.model.result;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班计划生成请求
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationPlanRequest {
    private String systemId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> employeeIds;
}

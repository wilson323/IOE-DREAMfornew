package net.lab1024.sa.attendance.roster.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.roster.model.RotationSystemConfig;

/**
 * 轮班制度查询参数
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationSystemQueryParam {
    @Builder.Default
    private Integer pageNum = 1;

    @Builder.Default
    private Integer pageSize = 20;

    private String keyword;
    private RotationSystemConfig.RotationSystemType systemType;
    private RotationSystemConfig.SystemStatus status;
    private String sortBy;
    @Builder.Default
    private boolean asc = true;
}

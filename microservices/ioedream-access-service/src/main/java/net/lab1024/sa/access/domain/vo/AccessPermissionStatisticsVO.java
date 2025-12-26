package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限统计VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "权限统计VO")
public class AccessPermissionStatisticsVO {

    @Schema(description = "总权限数")
    private Integer total;

    @Schema(description = "有效权限数")
    private Integer valid;

    @Schema(description = "即将过期权限数")
    private Integer expiring;

    @Schema(description = "已过期权限数")
    private Integer expired;

    @Schema(description = "已冻结权限数")
    private Integer frozen;

    @Schema(description = "永久权限数")
    private Integer permanent;

    @Schema(description = "临时权限数")
    private Integer temporary;

    @Schema(description = "时段权限数")
    private Integer timeBased;
}

package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 权限查询表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "权限查询表单")
public class PermissionQueryForm {

    @Schema(description = "权限状态：1-有效，2-即将过期，3-已过期，4-已冻结")
    private Integer permissionStatus;

    @Schema(description = "权限类型：1-永久，2-临时，3-时段")
    private Integer permissionType;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 20;
}

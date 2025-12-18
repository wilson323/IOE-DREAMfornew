package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 撤销门禁权限请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "撤销门禁权限请求")
public class RevokeAccessPermissionRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    @Schema(description = "设备ID列表（为空则撤销所有权限）", example = "[\"ACCESS_001\", \"ACCESS_002\"]")
    private List<String> deviceIds;

    @Schema(description = "区域ID列表（为空则撤销所有区域权限）", example = "[1, 2]")
    private List<Long> areaIds;

    @Schema(description = "撤销原因", example = "离职")
    private String reason;

    @Schema(description = "是否立即生效", example = "true")
    private Boolean immediate = true;

    @Schema(description = "撤销时间", example = "2025-12-16T15:30:00")
    private String revokeTime;

    @Schema(description = "撤销类型", example = "all", allowableValues = {"all", "device", "area", "temporary"})
    private String revokeType = "all";

    @Schema(description = "操作人ID", example = "1002")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}

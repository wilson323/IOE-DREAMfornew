package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 门禁控制请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "门禁控制请求")
public class AccessControlRequest {

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "门禁设备ID", example = "ACCESS_001", required = true)
    private String deviceId;

    @Size(max = 500, message = "操作原因长度不能超过500个字符")
    @Schema(description = "操作原因", example = "客户通行")
    private String reason;

    @Schema(description = "操作人ID", example = "1001")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    @Schema(description = "是否记录日志", example = "true")
    private Boolean recordLog = true;

    @Schema(description = "开门时长（秒）", example = "30")
    private Integer openDuration;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}

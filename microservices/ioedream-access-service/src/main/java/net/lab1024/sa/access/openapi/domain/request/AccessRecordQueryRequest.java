package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 门禁通行记录查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "门禁通行记录查询请求")
public class AccessRecordQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "设备ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主入口门禁")
    private String deviceName;

    @Schema(description = "通行状态", example = "1", allowableValues = {"0", "1"})
    private Integer accessStatus;

    @Schema(description = "验证方式", example = "card", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
    private String verifyType;

    @Schema(description = "通行方向", example = "in", allowableValues = {"in", "out"})
    private String direction;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "开始时间", example = "2025-12-16 00:00:00")
    private String startTime;

    @Schema(description = "结束时间", example = "2025-12-16 23:59:59")
    private String endTime;

    @Schema(description = "排序字段", example = "accessTime")
    private String sortField;

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;

    @Schema(description = "是否包含异常记录", example = "false")
    private Boolean includeAbnormal;

    @Schema(description = "最低体温", example = "35.0")
    private Double minTemperature;

    @Schema(description = "最高体温", example = "38.0")
    private Double maxTemperature;
}
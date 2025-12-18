package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 闂ㄧ璁板綍鏌ヨ璇锋眰
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "闂ㄧ璁板綍鏌ヨ璇锋眰")
public class AccessRecordQueryRequest {

    @Schema(description = "椤电爜", example = "1")
    private Integer pageNum;

    @Schema(description = "椤靛ぇ灏?, example = "20")
    private Integer pageSize;

    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @Schema(description = "鐢ㄦ埛鍚?, example = "admin")
    private String username;

    @Schema(description = "璁惧ID", example = "ACCESS_001")
    private String deviceId;

    @Schema(description = "璁惧鍚嶇О", example = "涓婚棬绂?)
    private String deviceName;

    @Schema(description = "閫氳鐘舵€?, example = "1", allowableValues = {"0", "1"})
    private Integer accessStatus;

    @Schema(description = "楠岃瘉鏂瑰紡", example = "card", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
    private String verifyType;

    @Schema(description = "閫氳鏂瑰悜", example = "in", allowableValues = {"in", "out"})
    private String direction;

    @Schema(description = "鍖哄煙ID", example = "1")
    private Long areaId;

    @Schema(description = "閮ㄩ棬ID", example = "1")
    private Long departmentId;

    @Schema(description = "寮€濮嬫椂闂?, example = "2025-12-16 00:00:00")
    private String startTime;

    @Schema(description = "缁撴潫鏃堕棿", example = "2025-12-16 23:59:59")
    private String endTime;

    @Schema(description = "鎺掑簭瀛楁", example = "accessTime")
    private String sortField;

    @Schema(description = "鎺掑簭鏂瑰悜", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder;

    @Schema(description = "鏄惁鍖呭惈寮傚父璁板綍", example = "false")
    private Boolean includeAbnormal;

    @Schema(description = "鏈€浣庢俯搴?, example = "35.0")
    private Double minTemperature;

    @Schema(description = "鏈€楂樻俯搴?, example = "38.0")
    private Double maxTemperature;
}
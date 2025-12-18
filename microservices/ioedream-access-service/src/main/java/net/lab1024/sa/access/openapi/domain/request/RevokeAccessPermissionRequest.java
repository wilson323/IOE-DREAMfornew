package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 鎾ら攢闂ㄧ鏉冮檺璇锋眰
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "鎾ら攢闂ㄧ鏉冮檺璇锋眰")
public class RevokeAccessPermissionRequest {

    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "1001", required = true)
    private Long userId;

    @Schema(description = "璁惧ID鍒楄〃锛堜负绌哄垯鎾ら攢鎵€鏈夋潈闄愶級", example = "[\"ACCESS_001\", \"ACCESS_002\"]")
    private List<String> deviceIds;

    @Schema(description = "鍖哄煙ID鍒楄〃锛堜负绌哄垯鎾ら攢鎵€鏈夊尯鍩熸潈闄愶級", example = "[1, 2]")
    private List<Long> areaIds;

    @Schema(description = "鎾ら攢鍘熷洜", example = "绂昏亴")
    private String reason;

    @Schema(description = "鏄惁绔嬪嵆鐢熸晥", example = "true")
    private Boolean immediate = true;

    @Schema(description = "鎾ら攢鏃堕棿", example = "2025-12-16T15:30:00")
    private String revokeTime;

    @Schema(description = "鎾ら攢绫诲瀷", example = "all", allowableValues = {"all", "device", "area", "temporary"})
    private String revokeType = "all";

    @Schema(description = "鎿嶄綔浜篒D", example = "1002")
    private Long operatorId;

    @Schema(description = "鎿嶄綔浜哄鍚?, example = "绠＄悊鍛?)
    private String operatorName;

    @Schema(description = "鎵╁睍鍙傛暟锛圝SON鏍煎紡锛?, example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}
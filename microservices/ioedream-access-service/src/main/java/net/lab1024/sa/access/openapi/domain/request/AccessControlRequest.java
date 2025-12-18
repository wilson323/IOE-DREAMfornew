package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 闂ㄧ鎺у埗璇锋眰
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "闂ㄧ鎺у埗璇锋眰")
public class AccessControlRequest {

    @NotBlank(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "闂ㄧ璁惧ID", example = "ACCESS_001", required = true)
    private String deviceId;

    @Size(max = 500, message = "鎿嶄綔鍘熷洜闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Schema(description = "鎿嶄綔鍘熷洜", example = "璁垮閫氳")
    private String reason;

    @Schema(description = "鎿嶄綔浜篒D", example = "1001")
    private Long operatorId;

    @Schema(description = "鎿嶄綔浜哄鍚?, example = "寮犱笁")
    private String operatorName;

    @Schema(description = "鏄惁璁板綍鏃ュ織", example = "true")
    private Boolean recordLog = true;

    @Schema(description = "寮€闂ㄦ椂闀匡紙绉掞級", example = "30")
    private Integer openDuration;

    @Schema(description = "鎵╁睍鍙傛暟锛圝SON鏍煎紡锛?, example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}
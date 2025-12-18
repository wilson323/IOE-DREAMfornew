package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 闂ㄧ璁惧鎺у埗璇锋眰
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "闂ㄧ璁惧鎺у埗璇锋眰")
public class AccessDeviceControlRequest {

    @NotNull(message = "鎿嶄綔绫诲瀷涓嶈兘涓虹┖")
    @Schema(description = "鎿嶄綔绫诲瀷", example = "open", required = true,
            allowableValues = {"open", "close", "lock", "unlock", "restart", "config"})
    private String action;

    @Schema(description = "鎿嶄綔鍙傛暟锛圝SON鏍煎紡锛?, example = "{\"duration\":30}")
    private String parameters;

    @Schema(description = "鎿嶄綔鍘熷洜", example = "绱ф€ュ紑闂?)
    private String reason;

    @Schema(description = "鏄惁绔嬪嵆鎵ц", example = "true")
    private Boolean immediate = true;

    @Schema(description = "鎵ц鏃堕棿", example = "2025-12-16T15:30:00")
    private String executeTime;

    @Schema(description = "閲嶅娆℃暟", example = "1")
    private Integer repeatCount = 1;

    @Schema(description = "閲嶅闂撮殧锛堢锛?, example = "60")
    private Integer repeatInterval = 60;
}
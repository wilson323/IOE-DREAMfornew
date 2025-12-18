package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 鎺堜簣闂ㄧ鏉冮檺璇锋眰
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "鎺堜簣闂ㄧ鏉冮檺璇锋眰")
public class GrantAccessPermissionRequest {

    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "1001", required = true)
    private Long userId;

    @Schema(description = "璁惧ID鍒楄〃", example = "[\"ACCESS_001\", \"ACCESS_002\"]")
    private List<String> deviceIds;

    @Schema(description = "璁惧绫诲瀷", example = "access", allowableValues = {"access", "turnstile", "gate"})
    private String deviceType;

    @Schema(description = "鍖哄煙ID鍒楄〃", example = "[1, 2]")
    private List<Long> areaIds;

    @Schema(description = "鏉冮檺绫诲瀷", example = "permanent", allowableValues = {"permanent", "temporary", "scheduled"})
    private String permissionType = "permanent";

    @Schema(description = "鐢熸晥鏃堕棿", example = "2025-12-16T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "澶辨晥鏃堕棿", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "鍏佽閫氳鐨勬椂闂存", example = "[{\"dayOfWeek\":\"1-5\",\"startTime\":\"08:00\",\"endTime\":\"18:00\"}]")
    private List<TimeSlot> allowedTimeSlots;

    @Schema(description = "閫氳娆℃暟闄愬埗", example = "100")
    private Integer maxAccessCount;

    @Schema(description = "姣忔棩閫氳娆℃暟闄愬埗", example = "10")
    private Integer maxDailyAccessCount;

    @Schema(description = "鏉冮檺绾у埆", example = "normal", allowableValues = {"normal", "vip", "emergency"})
    private String permissionLevel = "normal";

    @Schema(description = "鏄惁闇€瑕佸鏍?, example = "false")
    private Boolean requireApproval = false;

    @Schema(description = "瀹℃壒浜篒D", example = "1002")
    private Long approverId;

    @Schema(description = "鏉冮檺鍘熷洜", example = "鍔炲叕闇€瑕?)
    private String reason;

    @Schema(description = "鎵╁睍鍙傛暟锛圝SON鏍煎紡锛?, example = "{\"key1\":\"value1\"}")
    private String extendedParams;

    /**
     * 鏃堕棿娈?     */
    @Data
    @Schema(description = "鏃堕棿娈?)
    public static class TimeSlot {

        @Schema(description = "鏄熸湡鍑狅紙1-7锛?涓哄懆涓€锛?, example = "1-5")
        private String dayOfWeek;

        @Schema(description = "寮€濮嬫椂闂?, example = "08:00")
        private String startTime;

        @Schema(description = "缁撴潫鏃堕棿", example = "18:00")
        private String endTime;
    }
}
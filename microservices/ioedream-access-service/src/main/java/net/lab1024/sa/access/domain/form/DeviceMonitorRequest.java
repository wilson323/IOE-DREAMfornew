package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 璁惧鐩戞帶璇锋眰琛ㄥ崟
 * <p>
 * 鐢ㄤ簬璁惧鍋ュ悍鐩戞帶鐨勮姹傚弬鏁?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Data娉ㄨВ
 * - 瀹屾暣鐨勫瓧娈甸獙璇佹敞瑙?
 * - Swagger鏂囨。娉ㄨВ
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "璁惧鐩戞帶璇锋眰")
public class DeviceMonitorRequest {

    /**
     * 璁惧ID
     */
    @NotNull(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "璁惧ID", example = "1001")
    private Long deviceId;

    /**
     * 鐩戞帶绫诲瀷
     * BASIC - 鍩虹鐩戞帶
     * DETAILED - 璇︾粏鐩戞帶
     * REALTIME - 瀹炴椂鐩戞帶
     */
    @Schema(description = "鐩戞帶绫诲瀷", example = "DETAILED", allowableValues = {"BASIC", "DETAILED", "REALTIME"})
    private String monitorType = "DETAILED";

    /**
     * 鐩戞帶鏃堕暱锛堢锛?
     */
    @Schema(description = "鐩戞帶鏃堕暱锛堢锛?, example = "300")
    private Integer monitorDuration = 300;

    /**
     * 鏄惁鍖呭惈缃戠粶璇婃柇
     */
    @Schema(description = "鏄惁鍖呭惈缃戠粶璇婃柇", example = "true")
    private Boolean includeNetworkDiagnosis = true;

    /**
     * 鏄惁鍖呭惈鎬ц兘娴嬭瘯
     */
    @Schema(description = "鏄惁鍖呭惈鎬ц兘娴嬭瘯", example = "true")
    private Boolean includePerformanceTest = true;

    /**
     * 鏄惁鐢熸垚鍋ュ悍鎶ュ憡
     */
    @Schema(description = "鏄惁鐢熸垚鍋ュ悍鎶ュ憡", example = "false")
    private Boolean generateHealthReport = false;
}
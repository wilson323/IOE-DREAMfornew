package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 闂ㄧ璁板綍鍒涘缓琛ㄥ崟
 * <p>
 * 鐢ㄤ簬璁惧鍗忚鎺ㄩ€侀棬绂佽褰? * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤Form鍚庣紑鍛藉悕
 * - 浣跨敤Jakarta楠岃瘉娉ㄨВ
 * - 浣跨敤@Schema娉ㄨВ鎻忚堪瀛楁
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "闂ㄧ璁板綍鍒涘缓琛ㄥ崟")
public class AccessRecordAddForm {

    /**
     * 璁惧ID
     */
    @Schema(description = "璁惧ID", example = "1")
    private Long deviceId;

    /**
     * 璁惧缂栧彿
     */
    @Schema(description = "璁惧缂栧彿", example = "DEV001")
    private String deviceCode;

    /**
     * 鐢ㄦ埛ID
     */
    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    /**
     * 閫氳鏃堕棿锛堟椂闂存埑鎴朙ocalDateTime锛?     */
    @Schema(description = "閫氳鏃堕棿锛堟椂闂存埑鎴朙ocalDateTime锛?, example = "2025-01-30T08:00:00")
    private Object passTime;

    /**
     * 閫氳绫诲瀷
     * <p>
     * 0-杩涘叆
     * 1-绂诲紑
     * </p>
     */
    @Schema(description = "閫氳绫诲瀷锛?-杩涘叆锛?-绂诲紑", example = "0")
    private Integer passType;

    /**
     * 闂ㄥ彿
     */
    @Schema(description = "闂ㄥ彿", example = "1")
    private Integer doorNo;

    /**
     * 閫氳鏂瑰紡
     * <p>
     * 0-鍗＄墖
     * 1-浜鸿劯
     * 2-鎸囩汗
     * </p>
     */
    @Schema(description = "閫氳鏂瑰紡锛?-鍗＄墖锛?-浜鸿劯锛?-鎸囩汗", example = "1")
    private Integer passMethod;

    /**
     * 閫氳缁撴灉
     * <p>
     * 1-鎴愬姛
     * 0-澶辫触
     * </p>
     */
    @Schema(description = "閫氳缁撴灉锛?-鎴愬姛锛?-澶辫触", example = "1")
    private Integer accessResult;

    /**
     * 鍖哄煙ID
     */
    @Schema(description = "鍖哄煙ID", example = "1")
    private Long areaId;

    /**
     * 澶囨敞
     */
    @Schema(description = "澶囨敞", example = "璁惧鑷姩鎺ㄩ€?)
    private String remark;
}



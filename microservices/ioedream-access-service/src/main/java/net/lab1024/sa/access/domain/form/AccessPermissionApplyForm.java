package net.lab1024.sa.access.domain.form;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 闂ㄧ鏉冮檺鐢宠琛ㄥ崟
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤Form鍚庣紑鍛藉悕
 * - 浣跨敤Jakarta楠岃瘉娉ㄨВ
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessPermissionApplyForm {

    /**
     * 鐢宠浜篒D
     */
    @NotNull(message = "鐢宠浜篒D涓嶈兘涓虹┖")
    private Long applicantId;

    /**
     * 鍖哄煙ID
     */
    @NotNull(message = "鍖哄煙ID涓嶈兘涓虹┖")
    private Long areaId;

    /**
     * 鐢宠绫诲瀷
     * <p>
     * NORMAL-鏅€氭潈闄愮敵璇?     * EMERGENCY-绱ф€ユ潈闄愮敵璇?     * </p>
     */
    @NotBlank(message = "鐢宠绫诲瀷涓嶈兘涓虹┖")
    private String applyType;

    /**
     * 鐢宠鍘熷洜
     */
    @NotBlank(message = "鐢宠鍘熷洜涓嶈兘涓虹┖")
    @Size(max = 500, message = "鐢宠鍘熷洜闀垮害涓嶈兘瓒呰繃500瀛楃")
    private String applyReason;

    /**
     * 鐢宠寮€濮嬫椂闂?     */
    private LocalDateTime startTime;

    /**
     * 鐢宠缁撴潫鏃堕棿
     */
    private LocalDateTime endTime;

    /**
     * 澶囨敞
     */
    @Size(max = 500, message = "澶囨敞闀垮害涓嶈兘瓒呰繃500瀛楃")
    private String remark;
}



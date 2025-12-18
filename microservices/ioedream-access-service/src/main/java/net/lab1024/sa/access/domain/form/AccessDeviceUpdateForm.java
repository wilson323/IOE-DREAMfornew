package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 闂ㄧ璁惧鏇存柊琛ㄥ崟
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤Form鍚庣紑鍛藉悕
 * - 浣跨敤Jakarta楠岃瘉娉ㄨВ
 * - 瀹屾暣鐨勫弬鏁伴獙璇? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessDeviceUpdateForm {

    /**
     * 璁惧ID锛堜富閿級
     */
    @NotNull(message = "璁惧ID涓嶈兘涓虹┖")
    private Long deviceId;

    /**
     * 璁惧鍚嶇О
     */
    @NotBlank(message = "璁惧鍚嶇О涓嶈兘涓虹┖")
    @Size(max = 100, message = "璁惧鍚嶇О闀垮害涓嶈兘瓒呰繃100瀛楃")
    private String deviceName;

    /**
     * 璁惧缂栧彿
     */
    @NotBlank(message = "璁惧缂栧彿涓嶈兘涓虹┖")
    @Size(max = 50, message = "璁惧缂栧彿闀垮害涓嶈兘瓒呰繃50瀛楃")
    private String deviceCode;

    /**
     * 鍖哄煙ID锛堝閿級
     */
    @NotNull(message = "鍖哄煙ID涓嶈兘涓虹┖")
    private Long areaId;

    /**
     * IP鍦板潃
     */
    @NotBlank(message = "IP鍦板潃涓嶈兘涓虹┖")
    @Size(max = 50, message = "IP鍦板潃闀垮害涓嶈兘瓒呰繃50瀛楃")
    private String ipAddress;

    /**
     * 绔彛鍙?     */
    @NotNull(message = "绔彛鍙蜂笉鑳戒负绌?)
    private Integer port;

    /**
     * 鍚敤鏍囧織
     * <p>
     * 0-绂佺敤
     * 1-鍚敤
     * </p>
     */
    private Integer enabledFlag;

    /**
     * 澶囨敞
     */
    @Size(max = 500, message = "澶囨敞闀垮害涓嶈兘瓒呰繃500瀛楃")
    private String remark;
}



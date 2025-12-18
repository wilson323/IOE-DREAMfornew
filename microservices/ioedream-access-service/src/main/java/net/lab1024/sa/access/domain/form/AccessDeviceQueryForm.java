package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 闂ㄧ璁惧鏌ヨ琛ㄥ崟
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤Form鍚庣紑鍛藉悕
 * - 浣跨敤Jakarta楠岃瘉娉ㄨВ
 * - 缁ф壙鍒嗛〉鍙傛暟瑙勮寖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessDeviceQueryForm {

    /**
     * 椤电爜锛堜粠1寮€濮嬶級
     */
    @NotNull(message = "椤电爜涓嶈兘涓虹┖")
    @Min(value = 1, message = "椤电爜蹇呴』澶т簬0")
    private Integer pageNum = 1;

    /**
     * 姣忛〉澶у皬
     */
    @NotNull(message = "姣忛〉澶у皬涓嶈兘涓虹┖")
    @Min(value = 1, message = "姣忛〉澶у皬蹇呴』澶т簬0")
    private Integer pageSize = 20;

    /**
     * 鍏抽敭璇嶏紙璁惧鍚嶇О銆佽澶囩紪鍙凤級
     */
    @Nullable
    private String keyword;

    /**
     * 鍖哄煙ID
     */
    private Long areaId;

    /**
     * 璁惧鐘舵€?     * <p>
     * 鏋氫妇鍊硷細
     * - ONLINE - 鍦ㄧ嚎
     * - OFFLINE - 绂荤嚎
     * - MAINTAIN - 缁存姢涓?     * </p>
     */
    @Nullable
    private String deviceStatus;

    /**
     * 鍚敤鏍囧織
     * <p>
     * 0-绂佺敤
     * 1-鍚敤
     * </p>
     */
    private Integer enabledFlag;
}



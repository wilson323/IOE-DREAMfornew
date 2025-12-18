package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 闂ㄧ璁板綍瑙嗗浘瀵硅薄
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤VO鍚庣紑鍛藉悕
 * - 鐢ㄤ簬Controller杩斿洖缁欏墠绔殑鏁版嵁
 * - 涓嶅寘鍚晱鎰熶俊鎭? * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessRecordVO {

    /**
     * 璁板綍ID
     */
    private Long logId;

    /**
     * 鐢ㄦ埛ID
     */
    private Long userId;

    /**
     * 鐢ㄦ埛鍚?     */
    private String userName;

    /**
     * 璁惧ID
     */
    private String deviceId;

    /**
     * 璁惧鍚嶇О
     */
    private String deviceName;

    /**
     * 鍖哄煙ID
     */
    private String areaId;

    /**
     * 鍖哄煙鍚嶇О
     */
    private String areaName;

    /**
     * 鎿嶄綔绫诲瀷锛堥獙璇佹柟寮忥級
     */
    private String operation;

    /**
     * 鎿嶄綔缁撴灉
     * <p>
     * 1-鎴愬姛
     * 2-澶辫触
     * 3-寮傚父
     * </p>
     */
    private Integer result;

    /**
     * 鎿嶄綔鏃堕棿
     */
    private LocalDateTime createTime;

    /**
     * IP鍦板潃
     */
    private String ipAddress;

    /**
     * 澶囨敞
     */
    private String remark;
}



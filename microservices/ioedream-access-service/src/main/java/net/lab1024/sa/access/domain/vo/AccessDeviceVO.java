package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 闂ㄧ璁惧瑙嗗浘瀵硅薄
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
public class AccessDeviceVO {

    /**
     * 璁惧ID
     */
    private Long deviceId;

    /**
     * 璁惧鍚嶇О
     */
    private String deviceName;

    /**
     * 璁惧缂栧彿
     */
    private String deviceCode;

    /**
     * 璁惧绫诲瀷
     */
    private String deviceType;

    /**
     * 鍖哄煙ID
     */
    private Long areaId;

    /**
     * 鍖哄煙鍚嶇О
     */
    private String areaName;

    /**
     * IP鍦板潃
     */
    private String ipAddress;

    /**
     * 绔彛鍙?     */
    private Integer port;

    /**
     * 璁惧鐘舵€?     */
    private String deviceStatus;

    /**
     * 鍚敤鏍囧織
     */
    private Integer enabledFlag;

    /**
     * 鏈€鍚庡湪绾挎椂闂?     */
    private LocalDateTime lastOnlineTime;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;
}



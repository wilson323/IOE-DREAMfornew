package net.lab1024.sa.access.domain.vo;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 闂ㄧ璁板綍缁熻瑙嗗浘瀵硅薄
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤VO鍚庣紑鍛藉悕
 * - 鐢ㄤ簬Controller杩斿洖缁熻鏁版嵁
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessRecordStatisticsVO {

    /**
     * 鎬昏褰曟暟
     */
    private Long totalCount;

    /**
     * 鎴愬姛璁板綍鏁?     */
    private Long successCount;

    /**
     * 澶辫触璁板綍鏁?     */
    private Long failedCount;

    /**
     * 寮傚父璁板綍鏁?     */
    private Long abnormalCount;

    /**
     * 鎸夋搷浣滅被鍨嬬粺璁?     */
    private List<Map<String, Object>> statisticsByOperation;

    /**
     * 鎸夊尯鍩熺粺璁?     */
    private List<Map<String, Object>> statisticsByArea;

    /**
     * 鎸夎澶囩粺璁?     */
    private List<Map<String, Object>> statisticsByDevice;

    /**
     * 鎸夋椂闂寸粺璁★紙鎸夋棩锛?     */
    private List<Map<String, Object>> statisticsByDate;
}



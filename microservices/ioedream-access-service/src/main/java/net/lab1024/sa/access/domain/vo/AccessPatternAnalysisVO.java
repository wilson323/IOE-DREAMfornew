package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 璁块棶妯″紡鍒嗘瀽瑙嗗浘瀵硅薄
 * <p>
 * 鐢ㄦ埛璁块棶妯″紡鍒嗘瀽缁撴灉鐨勬暟鎹紶杈撳璞?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Data娉ㄨВ
 * - 瀹屾暣鐨勫瓧娈垫枃妗ｆ敞瑙?
 * - 鏋勫缓鑰呮ā寮忔敮鎸?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "璁块棶妯″紡鍒嗘瀽淇℃伅")
public class AccessPatternAnalysisVO {

    /**
     * 鐢ㄦ埛ID
     */
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    /**
     * 鍖哄煙ID
     */
    @Schema(description = "鍖哄煙ID", example = "2001")
    private Long areaId;

    /**
     * 妯″紡璇勫垎锛?-100锛?
     */
    @Schema(description = "妯″紡璇勫垎锛?-100锛?, example = "85.5")
    private BigDecimal patternScore;

    /**
     * 鏄惁妫€娴嬪埌寮傚父
     */
    @Schema(description = "鏄惁妫€娴嬪埌寮傚父", example = "false")
    private Boolean anomalyDetected;

    /**
     * 甯歌璁块棶鏃堕棿妯″紡
     */
    @Schema(description = "甯歌璁块棶鏃堕棿妯″紡", example = "WORKING_HOURS")
    private String regularTimePattern;

    /**
     * 璁块棶棰戠巼绛夌骇
     * LOW - 浣庨
     * NORMAL - 姝ｅ父
     * HIGH - 楂橀
     * EXCESSIVE - 杩囬
     */
    @Schema(description = "璁块棶棰戠巼绛夌骇", example = "NORMAL")
    private String accessFrequencyLevel;

    /**
     * 涓€鑷存€ц瘎鍒?
     */
    @Schema(description = "涓€鑷存€ц瘎鍒?, example = "92.3")
    private BigDecimal consistencyScore;

    /**
     * 鍒嗘瀽鏃堕棿
     */
    @Schema(description = "鍒嗘瀽鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime analysisTime;
}
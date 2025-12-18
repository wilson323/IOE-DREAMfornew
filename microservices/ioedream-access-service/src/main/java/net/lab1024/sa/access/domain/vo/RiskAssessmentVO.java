package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 椋庨櫓璇勪及瑙嗗浘瀵硅薄
 * <p>
 * 璁块棶椋庨櫓璇勪及缁撴灉鐨勬暟鎹紶杈撳璞?
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
@Schema(description = "椋庨櫓璇勪及淇℃伅")
public class RiskAssessmentVO {

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
     * 璁块棶绫诲瀷
     */
    @Schema(description = "璁块棶绫诲瀷", example = "CARD_ACCESS")
    private String accessType;

    /**
     * 椋庨櫓璇勫垎锛?-100锛?
     */
    @Schema(description = "椋庨櫓璇勫垎锛?-100锛?, example = "35.5")
    private BigDecimal riskScore;

    /**
     * 椋庨櫓绛夌骇
     * LOW - 浣庨闄?
     * MEDIUM - 涓瓑椋庨櫓
     * HIGH - 楂橀闄?
     * CRITICAL - 涓ラ噸椋庨櫓
     */
    @Schema(description = "椋庨櫓绛夌骇", example = "MEDIUM")
    private String riskLevel;

    /**
     * 椋庨櫓鍥犲瓙
     */
    @Schema(description = "椋庨櫓鍥犲瓙")
    private Map<String, Integer> riskFactors;

    /**
     * 璇勪及鏃堕棿
     */
    @Schema(description = "璇勪及鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime assessmentTime;

    /**
     * 娣诲姞椋庨櫓鍥犲瓙
     */
    public void addRiskFactor(String factorType, Integer score) {
        if (riskFactors == null) {
            riskFactors = new HashMap<>();
        }
        riskFactors.put(factorType, score);
    }
}
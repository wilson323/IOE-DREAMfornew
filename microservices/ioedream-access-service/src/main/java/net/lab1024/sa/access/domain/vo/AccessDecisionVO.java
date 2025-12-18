package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 璁块棶鍐崇瓥瑙嗗浘瀵硅薄
 * <p>
 * 鏅鸿兘璁块棶鎺у埗鍐崇瓥缁撴灉鐨勬暟鎹紶杈撳璞?
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
@Schema(description = "璁块棶鍐崇瓥淇℃伅")
public class AccessDecisionVO {

    /**
     * 鐢ㄦ埛ID
     */
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    /**
     * 璁惧ID
     */
    @Schema(description = "璁惧ID", example = "3001")
    private Long deviceId;

    /**
     * 鍖哄煙ID
     */
    @Schema(description = "鍖哄煙ID", example = "2001")
    private Long areaId;

    /**
     * 椋庨櫓绛夌骇
     */
    @Schema(description = "椋庨櫓绛夌骇", example = "MEDIUM")
    private String riskLevel;

    /**
     * 椋庨櫓璇勫垎
     */
    @Schema(description = "椋庨櫓璇勫垎", example = "35.5")
    private BigDecimal riskScore;

    /**
     * 璁块棶绾у埆
     * DENIED - 鎷掔粷
     * RESTRICTED - 鍙楅檺
     * NORMAL - 姝ｅ父
     * ENHANCED - 澧炲己
     */
    @Schema(description = "璁块棶绾у埆", example = "NORMAL")
    private String accessLevel;

    /**
     * 鏄惁闇€瑕佷簩娆￠獙璇?
     */
    @Schema(description = "鏄惁闇€瑕佷簩娆￠獙璇?, example = "false")
    private Boolean requireSecondaryVerification;

    /**
     * 棰濆瀹夊叏鎺柦
     */
    @Schema(description = "棰濆瀹夊叏鎺柦")
    private List<String> additionalSecurityMeasures;

    /**
     * 鍐崇瓥鏃堕棿
     */
    @Schema(description = "鍐崇瓥鏃堕棿", example = "2025-01-30T15:45:00")
    private LocalDateTime decisionTime;
}
package net.lab1024.sa.access.service;

import java.util.List;

import lombok.Data;

/**
 * 楂樼骇闂ㄧ鎺у埗鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵楂樼骇闂ㄧ鎺у埗鐩稿叧涓氬姟鍔熻兘锛屽寘鎷細
 * - 鏅鸿兘椋庨櫓璇勪及
 * - 鍔ㄦ€佹潈闄愯皟鏁? * - 寮傚父琛屼负妫€娴? * - 璁块棶妯″紡鍒嗘瀽
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 娓呮櫚鐨勬柟娉曟敞閲? * - 缁熶竴鐨勬暟鎹紶杈撳璞? * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
public interface AdvancedAccessControlService {

    /**
     * 鎵ц闂ㄧ鎺у埗妫€鏌?     * <p>
     * 鍩虹鐨勮闂帶鍒舵鏌ワ紝鍖呮嫭鏉冮檺楠岃瘉鍜岄闄╄瘎浼?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param areaId 鍖哄煙ID
     * @param verificationData 楠岃瘉鏁版嵁
     * @param accessType 璁块棶绫诲瀷
     * @return 鎺у埗缁撴灉
     */
    AccessControlResult performAccessControlCheck(
            Long userId,
            Long deviceId,
            Long areaId,
            String verificationData,
            String accessType);

    /**
     * 鎵ц楂樼骇鏅鸿兘璁块棶鎺у埗
     * <p>
     * 澧炲己鐨勮闂帶鍒讹紝鍖呮嫭妯″紡鍒嗘瀽銆佸姩鎬佹潈闄愯皟鏁村拰寮傚父妫€娴?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param areaId 鍖哄煙ID
     * @param verificationData 楠岃瘉鏁版嵁
     * @param accessType 璁块棶绫诲瀷
     * @return 鎺у埗缁撴灉
     */
    AccessControlResult performIntelligentAccessControl(
            Long userId,
            Long deviceId,
            Long areaId,
            String verificationData,
            String accessType);

    /**
     * 闂ㄧ鎺у埗缁撴灉
     */
    @Data
    class AccessControlResult {
        /**
         * 鏄惁鍏佽璁块棶
         */
        private Boolean allowed;

        /**
         * 鎷掔粷鍘熷洜
         */
        private String denyReason;

        /**
         * 鏄惁闇€瑕佷簩娆￠獙璇?         */
        private Boolean requireSecondaryVerification;

        /**
         * 璁块棶绾у埆
         * DENIED - 鎷掔粷
         * RESTRICTED - 鍙楅檺
         * NORMAL - 姝ｅ父
         * ENHANCED - 澧炲己
         */
        private String accessLevel;

        /**
         * 棰濆瀹夊叏鎺柦
         */
        private List<String> additionalSecurityMeasures;

        /**
         * 鏄惁妫€娴嬪埌寮傚父琛屼负
         */
        private Boolean anomalousBehavior;

        /**
         * 寮傚父琛屼负鎻忚堪
         */
        private String anomalyDescription;

        /**
         * 椋庨櫓璇勫垎
         */
        private Integer riskScore;

        /**
         * 椋庨櫓绛夌骇
         */
        private String riskLevel;

        /**
         * 鍒ゆ柇鏄惁鍏佽璁块棶
         *
         * @return 鏄惁鍏佽璁块棶
         */
        public boolean isAllowed() {
            return allowed != null && allowed;
        }
    }
}


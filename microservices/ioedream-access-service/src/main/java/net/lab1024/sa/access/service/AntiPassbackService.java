package net.lab1024.sa.access.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 闂ㄧ鍙嶆綔鍥炴湇鍔℃帴锟?
 * <p>
 * 鍐呭瓨浼樺寲璁捐鍘熷垯锟?
 * - 鎺ュ彛绮剧畝锛岃亴璐ｅ崟涓€
 * - 浣跨敤寮傛澶勭悊锛屾彁楂樺苟鍙戞€ц兘
 * - 鐔旀柇鍣ㄤ繚鎶わ紝闃叉绾ц仈鏁呴殰
 * - 缂撳瓨绛栫暐浼樺寲锛屽噺灏戦噸澶嶆煡锟?
 * - 鎵归噺鎿嶄綔鏀寔锛屽噺灏慖O寮€閿€
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AntiPassbackService {

    // ==================== 鍙嶆綔鍥為獙锟?====================

    /**
     * 鎵ц鍙嶆綔鍥炴锟?
     * <p>
     * 妫€鏌ョ敤鎴锋槸鍚﹀瓨鍦ㄦ綔鍥炶锟?
     * 鏀寔澶氱鍙嶆綔鍥炴ā寮忥細纭弽娼滃洖銆佽蒋鍙嶆綔鍥炪€佸尯鍩熷弽娼滃洖
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param areaId 鍖哄煙ID
     * @param verificationData 楠岃瘉鏁版嵁
     * @return 鍙嶆綔鍥炴鏌ョ粨锟?
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<AntiPassbackResult>> performAntiPassbackCheck(
            Long userId,
            Long deviceId,
            Long areaId,
            String verificationData
    );

    /**
     * 妫€鏌ュ尯鍩熷弽娼滃洖
     * <p>
     * 妫€鏌ョ敤鎴锋槸鍚︿粠鍖哄煙A杩涘叆鍚庢湭浠庡尯鍩烞绂诲紑
     * 閫傜敤浜庨渶瑕佹垚瀵硅繘鍑虹殑鍖哄煙鎺у埗
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param entryAreaId 杩涘叆鍖哄煙ID
     * @param exitAreaId 绂诲紑鍖哄煙ID
     * @param direction 杩涘嚭鏂瑰悜锛坕n/out锟?
     * @return 鍖哄煙鍙嶆綔鍥炴鏌ョ粨锟?
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackCheckFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<AntiPassbackResult>> checkAreaAntiPassback(
            Long userId,
            Long entryAreaId,
            Long exitAreaId,
            String direction
    );

    // ==================== 鍙嶆綔鍥為厤缃锟?====================

    /**
     * 璁剧疆璁惧鍙嶆綔鍥炵瓥锟?
     *
     * @param deviceId 璁惧ID
     * @param antiPassbackType 鍙嶆綔鍥炵被鍨嬶紙hard/soft/area/global锟?
     * @param config 閰嶇疆鍙傛暟
     * @return 璁剧疆缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> setAntiPassbackPolicy(
            Long deviceId,
            String antiPassbackType,
            Map<String, Object> config
    );

    /**
     * 鑾峰彇璁惧鍙嶆綔鍥炵瓥锟?
     *
     * @param deviceId 璁惧ID
     * @return 鍙嶆綔鍥炵瓥鐣ヤ俊锟?
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getAntiPassbackPolicy(Long deviceId);

    /**
     * 鏇存柊鍙嶆綔鍥為厤锟?
     *
     * @param deviceId 璁惧ID
     * @param config 閰嶇疆鍙傛暟
     * @return 鏇存柊缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> updateAntiPassbackConfig(
            Long deviceId,
            Map<String, Object> config
    );

    // ==================== 鍙嶆綔鍥炶褰曠锟?====================

    /**
     * 璁板綍閫氳浜嬩欢
     * <p>
     * 璁板綍鐢ㄦ埛閫氳浜嬩欢锛岀敤浜庡弽娼滃洖鍒嗘瀽
     * 鍖呮嫭杩涘嚭鏃堕棿銆佽澶囥€佸尯鍩熴€佹柟鍚戠瓑淇℃伅
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param areaId 鍖哄煙ID
     * @param direction 杩涘嚭鏂瑰悜
     * @param verificationData 楠岃瘉鏁版嵁
     * @param result 閫氳缁撴灉
     * @return 璁板綍缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> recordAccessEvent(
            Long userId,
            Long deviceId,
            Long areaId,
            String direction,
            String verificationData,
            Boolean result
    );

    /**
     * 鑾峰彇鐢ㄦ埛鍙嶆綔鍥炵姸锟?
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛鍙嶆綔鍥炵姸锟?
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getUserAntiPassbackStatus(Long userId);

    /**
     * 娓呯悊鐢ㄦ埛鍙嶆綔鍥炶锟?
     * <p>
     * 娓呯悊鐢ㄦ埛鐨勫弽娼滃洖璁板綍锛岄€氬父鍦ㄦ甯稿畬鎴愯繘鍑烘祦绋嬪悗璋冪敤
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @return 娓呯悊缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> clearUserAntiPassbackRecords(
            Long userId,
            Long deviceId
    );

    // ==================== 鍙嶆綔鍥炲紓甯稿锟?====================

    /**
     * 澶勭悊鍙嶆綔鍥炲紓锟?
     * <p>
     * 褰撴娴嬪埌鍙嶆綔鍥炲紓甯告椂杩涜澶勭悊
     * 鍖呮嫭鍛婅閫氱煡銆佽褰曞紓甯搞€佷复鏃堕攣瀹氱瓑
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param deviceId 璁惧ID
     * @param antiPassbackResult 鍙嶆綔鍥炵粨锟?
     * @return 澶勭悊缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> handleAntiPassbackViolation(
            Long userId,
            Long deviceId,
            AntiPassbackResult antiPassbackResult
    );

    /**
     * 閲嶇疆鐢ㄦ埛鍙嶆綔鍥炵姸锟?
     * <p>
     * 绠＄悊鍛樻墜鍔ㄩ噸缃敤鎴风殑鍙嶆綔鍥炵姸锟?
     * 鐢ㄤ簬瑙ｅ喅寮傚父鎯呭喌鎴栬锟?
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param operatorId 鎿嶄綔鍛業D
     * @param reason 閲嶇疆鍘熷洜
     * @return 閲嶇疆缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Void>> resetUserAntiPassbackStatus(
            Long userId,
            Long operatorId,
            String reason
    );

    // ==================== 鍙嶆綔鍥炵粺璁″垎锟?====================

    /**
     * 鑾峰彇鍙嶆綔鍥炵粺璁′俊锟?
     *
     * @param startTime 寮€濮嬫椂锟?
     * @param endTime 缁撴潫鏃堕棿
     * @return 缁熻淇℃伅
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getAntiPassbackStatistics(
            String startTime,
            String endTime
    );

    /**
     * 鑾峰彇鍙嶆綔鍥炲紓甯告姤锟?
     *
     * @param deviceId 璁惧ID
     * @param startTime 寮€濮嬫椂锟?
     * @param endTime 缁撴潫鏃堕棿
     * @return 寮傚父鎶ュ憡
     */
    @CircuitBreaker(name = "antiPassbackService")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Object>> getAntiPassbackViolationReport(
            Long deviceId,
            String startTime,
            String endTime
    );

    // ==================== 鎵归噺鎿嶄綔 ====================

    /**
     * 鎵归噺妫€鏌ュ弽娼滃洖鐘讹拷?
     *
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @return 鎵归噺妫€鏌ョ粨锟?
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackBatchOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Map<Long, Object>>> batchCheckAntiPassbackStatus(
            String userIds
    );

    /**
     * 鎵归噺娓呯悊鍙嶆綔鍥炶锟?
     *
     * @param userIds 鐢ㄦ埛ID鍒楄〃
     * @return 鎵归噺娓呯悊缁撴灉
     */
    @CircuitBreaker(name = "antiPassbackService", fallbackMethod = "antiPassbackBatchOperationFallback")
    @TimeLimiter(name = "antiPassbackService")
    CompletableFuture<ResponseDTO<Map<Long, Object>>> batchClearAntiPassbackRecords(
            String userIds
    );

    // ==================== 鍐呴儴鏁版嵁锟?====================

    /**
     * 鍙嶆綔鍥炵粨锟?
     */
    class AntiPassbackResult {
        /**
         * 鏄惁閫氳繃鍙嶆綔鍥炴锟?
         */
        private Boolean passed;

        /**
         * 鍙嶆綔鍥炵被锟?
         */
        private String antiPassbackType;

        /**
         * 鎷掔粷鍘熷洜
         */
        private String denyReason;

        /**
         * 鏈€鍚庨€氳璁板綍
         */
        private Object lastAccessRecord;

        /**
         * 杩濊绾у埆
         */
        private String violationLevel;

        /**
         * 寤鸿澶勭悊鏂瑰紡
         */
        private String recommendedAction;

        /**
         * 椋庨櫓璇勫垎
         */
        private Integer riskScore;

        // Getters and Setters
        public Boolean getPassed() {
            return passed;
        }

        public void setPassed(Boolean passed) {
            this.passed = passed;
        }

        public String getAntiPassbackType() {
            return antiPassbackType;
        }

        public void setAntiPassbackType(String antiPassbackType) {
            this.antiPassbackType = antiPassbackType;
        }

        public String getDenyReason() {
            return denyReason;
        }

        public void setDenyReason(String denyReason) {
            this.denyReason = denyReason;
        }

        public Object getLastAccessRecord() {
            return lastAccessRecord;
        }

        public void setLastAccessRecord(Object lastAccessRecord) {
            this.lastAccessRecord = lastAccessRecord;
        }

        public String getViolationLevel() {
            return violationLevel;
        }

        public void setViolationLevel(String violationLevel) {
            this.violationLevel = violationLevel;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public void setRecommendedAction(String recommendedAction) {
            this.recommendedAction = recommendedAction;
        }

        public Integer getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Integer riskScore) {
            this.riskScore = riskScore;
        }

        public boolean isPassed() {
            return passed != null && passed;
        }
    }

    // ==================== 闄嶇骇澶勭悊鏂规硶 ====================

    /**
     * 鍙嶆綔鍥炴鏌ラ檷绾у锟?
     */
    default CompletableFuture<ResponseDTO<AntiPassbackResult>> antiPassbackCheckFallback(
            Long userId, Exception exception, Object... params) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTIPASSBACK_SERVICE_UNAVAILABLE", "鍙嶆綔鍥炴湇鍔℃殏鏃朵笉鍙敤锛屽凡闄嶇骇涓哄父瑙勬锟?)
        );
    }

    /**
     * 鍙嶆綔鍥炴搷浣滈檷绾у锟?
     */
    default CompletableFuture<ResponseDTO<Void>> antiPassbackOperationFallback(
            Exception exception, Object... params) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTIPASSBACK_SERVICE_UNAVAILABLE", "鍙嶆綔鍥炴湇鍔℃殏鏃朵笉鍙敤锛岃绋嶅悗閲嶈瘯")
        );
    }

    /**
     * 鍙嶆綔鍥炴壒閲忔搷浣滈檷绾у锟?
     */
    default CompletableFuture<ResponseDTO<Map<Long, Object>>> antiPassbackBatchOperationFallback(
            String params, Exception exception) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ANTIPASSBACK_BATCH_SERVICE_UNAVAILABLE", "鎵归噺鍙嶆綔鍥炴湇鍔℃殏鏃朵笉鍙敤锛岃绋嶅悗閲嶈瘯")
        );
    }
}

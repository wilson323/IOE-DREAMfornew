package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 鐩戞帶鍛婅鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵缁熶竴鏅鸿兘鐨勭洃鎺у憡璀︿綋绯伙細
 * - 澶氱淮搴﹀紓甯告娴嬩笌棰勮
 * - 鏅鸿兘鍛婅鍒嗙骇涓庢帹閫?
 * - 鍛婅澶勭悊娴佺▼璺熻釜
 * - 鐩戞帶鎸囨爣缁熻鍒嗘瀽
 * - 鍛婅瑙勫垯鍔ㄦ€侀厤缃?
 * - 鏁呴殰鑷剤涓庢仮澶嶆満鍒?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - Service鎺ュ彛瀹氫箟鍦ㄤ笟鍔℃湇鍔℃ā鍧椾腑
 * - 娓呮櫚鐨勬柟娉曟敞閲?
 * - 缁熶竴鐨勬暟鎹紶杈撳璞?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface MonitorAlertService {

    /**
     * 鍒涘缓鐩戞帶鍛婅
     * <p>
     * 鍒涘缓涓€涓柊鐨勭洃鎺у憡璀︿簨浠讹細
     * - 鍛婅淇℃伅瀹屾暣鎬ч獙璇?
     * - 鍛婅绾у埆鏅鸿兘璇勪及
     * - 鍛婅鍘婚噸涓庤仛鍚?
     * - 鍛婅璺敱绛栫暐纭畾
     * </p>
     *
     * @param request 鍛婅鍒涘缓璇锋眰
     * @return 鍛婅鍒涘缓缁撴灉
     */
    ResponseDTO<MonitorAlertResult> createMonitorAlert(CreateMonitorAlertRequest request);

    /**
     * 鏅鸿兘鍛婅鍒嗙骇
     * <p>
     * 鍩轰簬澶氱淮搴︽寚鏍囪繘琛屾櫤鑳藉憡璀﹀垎绾э細
     * - 褰卞搷鑼冨洿璇勪及
     * - 绱ф€ョ▼搴﹀垎鏋?
     * - 涓氬姟褰卞搷璇勪及
     * - 椋庨櫓绛夌骇璁＄畻
     * </p>
     *
     * @param request 鍒嗙骇璇锋眰
     * @return 鍒嗙骇缁撴灉
     */
    ResponseDTO<AlertLevelAssessmentResult> assessAlertLevel(AlertLevelAssessmentRequest request);

    /**
     * 鍛婅閫氱煡鎺ㄩ€?
     * <p>
     * 鏍规嵁鍛婅绾у埆鍜岄厤缃鍒欐帹閫佸憡璀﹂€氱煡锛?
     * - 澶氭笭閬撻€氱煡鏀寔锛堥偖浠躲€佺煭淇°€佸井淇°€侀拤閽夛級
     * - 閫氱煡妯℃澘鍔ㄦ€佹覆鏌?
     * - 閫氱煡鍙戦€佺姸鎬佽窡韪?
     * - 閫氱煡棰戠巼鎺у埗
     * </p>
     *
     * @param request 閫氱煡鎺ㄩ€佽姹?
     * @return 鎺ㄩ€佺粨鏋?
     */
    ResponseDTO<AlertNotificationResult> sendAlertNotification(AlertNotificationRequest request);

    /**
     * 鑾峰彇鍛婅鍒楄〃
     * <p>
     * 鏌ヨ鐩戞帶绯荤粺鍛婅鍒楄〃锛?
     * - 澶氱淮搴︽潯浠剁瓫閫?
     * - 鍛婅鐘舵€佽窡韪?
     * - 澶勭悊鍘嗗彶璁板綍
     * - 缁熻鍒嗘瀽鏀寔
     * </p>
     *
     * @param request 鏌ヨ璇锋眰
     * @return 鍛婅鍒楄〃
     */
    ResponseDTO<List<MonitorAlertVO>> getMonitorAlertList(MonitorAlertQueryRequest request);

    /**
     * 澶勭悊鍛婅浜嬩欢
     * <p>
     * 澶勭悊鐩戞帶鍛婅浜嬩欢锛?
     * - 鍛婅纭鎿嶄綔
     * - 澶勭悊鏂规鎵ц
     * - 澶勭悊缁撴灉璁板綍
     * - 鍏宠仈浜嬩欢鏇存柊
     * </p>
     *
     * @param request 澶勭悊璇锋眰
     * @return 澶勭悊缁撴灉
     */
    ResponseDTO<AlertHandleResult> handleAlert(AlertHandleRequest request);

    /**
     * 鍛婅缁熻鍒嗘瀽
     * <p>
     * 鐢熸垚鐩戞帶鍛婅鐨勭粺璁″垎鏋愭姤鍛婏細
     * - 鍛婅瓒嬪娍鍒嗘瀽
     * - 澶勭悊鏁堢巼缁熻
     * - 闂鏍规簮鍒嗘瀽
     * - 鏀硅繘寤鸿鐢熸垚
     * </p>
     *
     * @param request 缁熻璇锋眰
     * @return 缁熻鎶ュ憡
     */
    ResponseDTO<AlertStatisticsReport> generateAlertStatistics(AlertStatisticsRequest request);

    /**
     * 閰嶇疆鍛婅瑙勫垯
     * <p>
     * 绠＄悊鐩戞帶鍛婅鐨勮鍒欓厤缃細
     * - 瑙勫垯鍒涘缓涓庢洿鏂?
     * - 鏉′欢琛ㄨ揪寮忓畾涔?
     * - 鍔ㄤ綔绛栫暐閰嶇疆
     * - 瑙勫垯鏈夋晥鎬ч獙璇?
     * </p>
     *
     * @param request 閰嶇疆璇锋眰
     * @return 閰嶇疆缁撴灉
     */
    ResponseDTO<AlertRuleResult> configureAlertRule(AlertRuleConfigureRequest request);

    /**
     * 鑾峰彇鍛婅瑙勫垯鍒楄〃
     * <p>
     * 鏌ヨ绯荤粺閰嶇疆鐨勫憡璀﹁鍒欏垪琛細
     * - 瑙勫垯鐘舵€佺鐞?
     * - 瑙勫垯浼樺厛绾ф帓搴?
     * - 瑙勫垯浣跨敤缁熻
     * - 瑙勫垯鏁堟灉璇勪及
     * </p>
     *
     * @param request 鏌ヨ璇锋眰
     * @return 瑙勫垯鍒楄〃
     */
    ResponseDTO<List<AlertRuleVO>> getAlertRuleList(AlertRuleQueryRequest request);

    /**
     * 绯荤粺鍋ュ悍妫€鏌?
     * <p>
     * 鎵ц绯荤粺鍏ㄩ潰鐨勫仴搴锋鏌ワ細
     * - 鏈嶅姟鍙敤鎬ф鏌?
     * - 鎬ц兘鎸囨爣鐩戞帶
     * - 璧勬簮浣跨敤璇勪及
     * - 娼滃湪椋庨櫓璇嗗埆
     * </p>
     *
     * @param request 妫€鏌ヨ姹?
     * @return 鍋ュ悍妫€鏌ョ粨鏋?
     */
    ResponseDTO<SystemHealthCheckResult> performSystemHealthCheck(SystemHealthCheckRequest request);

    /**
     * 鏁呴殰鑷剤澶勭悊
     * <p>
     * 鎵ц绯荤粺鏁呴殰鐨勮嚜鍔ㄦ仮澶嶅鐞嗭細
     * - 鏁呴殰绫诲瀷璇嗗埆
     * - 鑷剤绛栫暐鎵ц
     * - 鎭㈠缁撴灉楠岃瘉
     * - 浜哄伐浠嬪叆鍒ゆ柇
     * </p>
     *
     * @param request 鑷剤璇锋眰
     * @return 鑷剤缁撴灉
     */
    ResponseDTO<SelfHealingResult> performSelfHealing(SelfHealingRequest request);

    /**
     * 鍛婅瓒嬪娍棰勬祴
     * <p>
     * 鍩轰簬鍘嗗彶鏁版嵁棰勬祴鍛婅瓒嬪娍锛?
     * - 鏃堕棿搴忓垪鍒嗘瀽
     * - 瀛ｈ妭鎬фā寮忚瘑鍒?
     * - 寮傚父瓒嬪娍棰勮
     * - 瀹归噺瑙勫垝寤鸿
     * </p>
     *
     * @param request 棰勬祴璇锋眰
     * @return 棰勬祴缁撴灉
     */
    ResponseDTO<AlertTrendPredictionResult> predictAlertTrend(AlertTrendPredictionRequest request);

    // ==================== 鍐呴儴鏁版嵁浼犺緭瀵硅薄 ====================

    /**
     * 鍒涘缓鐩戞帶鍛婅璇锋眰
     */
    class CreateMonitorAlertRequest {
        private String alertTitle;           // 鍛婅鏍囬
        private String alertDescription;      // 鍛婅鎻忚堪
        private String alertType;             // 鍛婅绫诲瀷
        private String sourceSystem;          // 鏉ユ簮绯荤粺
        private String sourceComponent;       // 鏉ユ簮缁勪欢
        private String sourceInstanceId;      // 鏉ユ簮瀹炰緥ID
        private LocalDateTime occurTime;       // 鍙戠敓鏃堕棿
        private String severityLevel;         // 涓ラ噸绛夌骇锛圠OW/MEDIUM/HIGH/CRITICAL锛?
        private Map<String, Object> alertData; // 鍛婅鏁版嵁
        private List<String> affectedServices; // 褰卞搷鐨勬湇鍔?
        private List<String> tags;           // 鏍囩鍒楄〃
        private Boolean needNotification;     // 鏄惁闇€瑕侀€氱煡
        private String alertCategory;        // 鍛婅鍒嗙被
        private Integer priority;            // 浼樺厛绾э紙1-10锛?
        private String assignedTo;            // 鍒嗛厤缁?
        private String escalationRule;        // 鍗囩骇瑙勫垯
    }

    /**
     * 鐩戞帶鍛婅缁撴灉
     */
    class MonitorAlertResult {
        private String alertId;              // 鍛婅ID
        private String alertTitle;           // 鍛婅鏍囬
        private String alertDescription;      // 鍛婅鎻忚堪
        private LocalDateTime createTime;     // 鍒涘缓鏃堕棿
        private String severityLevel;         // 涓ラ噸绛夌骇
        private String status;               // 鐘舵€侊紙NEW/ACKNOWLEDGED/RESOLVED/CLOSED锛?
        private List<String> notificationIds; // 閫氱煡ID鍒楄〃
        private List<String> relatedAlertIds;  // 鍏宠仈鍛婅ID
        private String assignedTo;            // 鍒嗛厤缁?
        private LocalDateTime estimatedResolveTime; // 棰勮瑙ｅ喅鏃堕棿
        private Map<String, Object> metadata;  // 鍏冩暟鎹?
    }

    /**
     * 鍛婅鍒嗙骇璇勪及璇锋眰
     */
    class AlertLevelAssessmentRequest {
        private String alertType;             // 鍛婅绫诲瀷
        private String sourceSystem;          // 鏉ユ簮绯荤粺
        private Map<String, Object> alertMetrics; // 鍛婅鎸囨爣
        private String businessImpact;        // 涓氬姟褰卞搷
        private String affectedScope;         // 褰卞搷鑼冨洿
        private LocalDateTime occurTime;       // 鍙戠敓鏃堕棿
        private Integer affectedUsers;        // 褰卞搷鐢ㄦ埛鏁?
        private List<String> affectedServices; // 褰卞搷鐨勬湇鍔?
        private Boolean isRecurring;          // 鏄惁閲嶅鍙戠敓
        private Integer recurrenceCount;      // 閲嶅娆℃暟
    }

    /**
     * 鍛婅绾у埆璇勪及缁撴灉
     */
    class AlertLevelAssessmentResult {
        private String assessedLevel;         // 璇勪及鐨勭骇鍒?
        private Double confidenceScore;       // 缃俊搴﹁瘎鍒?
        private String assessmentReason;      // 璇勪及鍘熷洜
        private List<String> assessmentFactors; // 璇勪及鍥犵礌
        private String businessImpactLevel;   // 涓氬姟褰卞搷绾у埆
        private String urgencyLevel;          // 绱ф€ョ▼搴?
        private String recommendedAction;     // 寤鸿鎺柦
        private Integer recommendedPriority; // 鎺ㄨ崘浼樺厛绾?
        private LocalDateTime escalateTime;   // 鍗囩骇鏃堕棿
        private Map<String, Object> detailedMetrics; // 璇︾粏鎸囨爣
    }

    /**
     * 鍛婅閫氱煡鎺ㄩ€佽姹?
     */
    class AlertNotificationRequest {
        private String alertId;              // 鍛婅ID
        private List<String> notificationChannels; // 閫氱煡娓犻亾
        private List<String> recipients;      // 鎺ユ敹浜哄垪琛?
        private Map<String, Object> notificationData; // 閫氱煡鏁版嵁
        private String notificationTemplate;  // 閫氱煡妯℃澘
        private Boolean needEscalation;       // 鏄惁闇€瑕佸崌绾?
        private Integer maxRetries;           // 鏈€澶ч噸璇曟鏁?
        private Integer retryInterval;        // 閲嶈瘯闂撮殧锛堢锛?
        private Map<String, String> customHeaders; // 鑷畾涔夊ご閮?
    }

    /**
     * 鍛婅閫氱煡缁撴灉
     */
    class AlertNotificationResult {
        private String notificationId;       // 閫氱煡ID
        private String alertId;              // 鍛婅ID
        private List<NotificationChannelResult> channelResults; // 娓犻亾缁撴灉
        private LocalDateTime sendTime;      // 鍙戦€佹椂闂?
        private Integer totalRecipients;      // 鎬绘帴鏀朵汉鏁?
        private Integer successCount;         // 鎴愬姛鍙戦€佹暟
        private Integer failureCount;         // 澶辫触鍙戦€佹暟
        private String overallStatus;         // 鏁翠綋鐘舵€?
        private List<String> failedRecipients; // 澶辫触鎺ユ敹浜?
        private Map<String, Object> responseMetadata; // 鍝嶅簲鍏冩暟鎹?
    }

    /**
     * 閫氱煡娓犻亾缁撴灉
     */
    class NotificationChannelResult {
        private String channel;              // 娓犻亾锛圗MAIL/SMS/WECHAT/DINGTALK锛?
        private Integer sentCount;           // 鍙戦€佹暟閲?
        private Integer successCount;         // 鎴愬姛鏁伴噺
        private Integer failureCount;         // 澶辫触鏁伴噺
        private String status;               // 娓犻亾鐘舵€?
        private String errorMessage;         // 閿欒淇℃伅
        private LocalDateTime completedTime;  // 瀹屾垚鏃堕棿
    }

    /**
     * 鐩戞帶鍛婅鏌ヨ璇锋眰
     */
    class MonitorAlertQueryRequest {
        private String alertId;              // 鍛婅ID
        private String alertType;             // 鍛婅绫诲瀷
        private String severityLevel;         // 涓ラ噸绛夌骇
        private String status;               // 鐘舵€?
        private String sourceSystem;          // 鏉ユ簮绯荤粺
        private String assignedTo;            // 鍒嗛厤缁?
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private List<String> tags;           // 鏍囩
        private Integer pageNum;             // 椤电爜
        private Integer pageSize;            // 姣忛〉澶у皬
        private String sortBy;               // 鎺掑簭瀛楁
        private String sortOrder;            // 鎺掑簭鏂瑰悜
    }

    /**
     * 鐩戞帶鍛婅瑙嗗浘瀵硅薄
     */
    class MonitorAlertVO {
        private String alertId;              // 鍛婅ID
        private String alertTitle;           // 鍛婅鏍囬
        private String alertDescription;      // 鍛婅鎻忚堪
        private String alertType;             // 鍛婅绫诲瀷
        private String sourceSystem;          // 鏉ユ簮绯荤粺
        private LocalDateTime occurTime;       // 鍙戠敓鏃堕棿
        private LocalDateTime createTime;     // 鍒涘缓鏃堕棿
        private LocalDateTime updateTime;     // 鏇存柊鏃堕棿
        private String severityLevel;         // 涓ラ噸绛夌骇
        private String status;               // 鐘舵€?
        private String assignedTo;            // 鍒嗛厤缁?
        private String assignedToName;        // 鍒嗛厤浜哄鍚?
        private Integer duration;             // 鎸佺画鏃堕暱锛堝垎閽燂級
        private List<String> affectedServices; // 褰卞搷鐨勬湇鍔?
        private List<String> tags;           // 鏍囩
        private Boolean isRecurring;          // 鏄惁閲嶅
        private Integer recurrenceCount;      // 閲嶅娆℃暟
        private String businessImpact;        // 涓氬姟褰卞搷
        private String resolution;            // 瑙ｅ喅鏂规
        private LocalDateTime resolvedTime;   // 瑙ｅ喅鏃堕棿
        private Integer resolutionDuration;   // 瑙ｅ喅鏃堕暱锛堝垎閽燂級
    }

    /**
     * 鍛婅澶勭悊璇锋眰
     */
    class AlertHandleRequest {
        private String alertId;              // 鍛婅ID
        private String handleAction;          // 澶勭悊鍔ㄤ綔锛圓CKNOWLEDGE/ASSIGN/RESOLVE/CLOSE/ESCALATE锛?
        private String handleComment;         // 澶勭悊璇存槑
        private String assignedTo;            // 鍒嗛厤缁?
        private String resolutionMethod;      // 瑙ｅ喅鏂规硶
        private List<String> attachments;      // 闄勪欢
        private Boolean markAsResolved;       // 鏍囪涓哄凡瑙ｅ喅
        private LocalDateTime estimatedResolveTime; // 棰勮瑙ｅ喅鏃堕棿
        private Map<String, Object> handleData; // 澶勭悊鏁版嵁
        private Boolean sendNotification;     // 鏄惁鍙戦€侀€氱煡
    }

    /**
     * 鍛婅澶勭悊缁撴灉
     */
    class AlertHandleResult {
        private String alertId;              // 鍛婅ID
        private String handleAction;          // 澶勭悊鍔ㄤ綔
        private String previousStatus;        // 澶勭悊鍓嶇姸鎬?
        private String currentStatus;         // 澶勭悊鍚庣姸鎬?
        private LocalDateTime handleTime;     // 澶勭悊鏃堕棿
        private String handledBy;             // 澶勭悊浜?
        private String handleComment;         // 澶勭悊璇存槑
        private Boolean autoGenerated;        // 鏄惁鑷姩鐢熸垚
        private List<String> affectedAlerts;  // 褰卞搷鐨勫憡璀?
        private Map<String, Object> handleMetadata; // 澶勭悊鍏冩暟鎹?
    }

    /**
     * 鍛婅缁熻璇锋眰
     */
    class AlertStatisticsRequest {
        private String statisticsType;       // 缁熻绫诲瀷锛圖AILY/WEEKLY/MONTHLY/CUSTOM锛?
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private List<String> alertTypes;      // 鍛婅绫诲瀷绛涢€?
        private List<String> severityLevels;  // 涓ラ噸绛夌骇绛涢€?
        private List<String> sourceSystems;   // 鏉ユ簮绯荤粺绛涢€?
        private String groupBy;               // 鍒嗙粍鏂瑰紡锛圱YPE/SEVERITY/SOURCE/STATUS锛?
        private List<String> metrics;          // 缁熻鎸囨爣
        private Boolean includeTrends;        // 鏄惁鍖呭惈瓒嬪娍
        private Integer topCount;             // Top鏁伴噺
    }

    /**
     * 鍛婅缁熻鎶ュ憡
     */
    class AlertStatisticsReport {
        private String statisticsPeriod;     // 缁熻鍛ㄦ湡
        private LocalDateTime reportTime;     // 鎶ュ憡鏃堕棿
        private Long totalAlerts;            // 鎬诲憡璀︽暟
        private Map<String, Long> alertsByType; // 鎸夌被鍨嬪垎缁勭殑鍛婅鏁?
        private Map<String, Long> alertsBySeverity; // 鎸変弗閲嶇瓑绾у垎缁勭殑鍛婅鏁?
        private Map<String, Long> alertsBySource; // 鎸夋潵婧愮郴缁熷垎缁勭殑鍛婅鏁?
        private Map<String, Long> alertsByStatus; // 鎸夌姸鎬佸垎缁勭殑鍛婅鏁?
        private Double averageResolutionTime; // 骞冲潎瑙ｅ喅鏃堕棿
        private Double resolutionRate;        // 瑙ｅ喅鐜?
        private List<AlertTrendData> trendData; // 瓒嬪娍鏁版嵁
        private List<AlertTopItem> topAlertTypes; // 涓昏鍛婅绫诲瀷
        private List<AlertTopItem> topSources; // 涓昏鏉ユ簮绯荤粺
        private Map<String, Object> insights; // 娲炲療鍒嗘瀽
    }

    /**
     * 鍛婅瓒嬪娍鏁版嵁
     */
    class AlertTrendData {
        private String timeSlot;             // 鏃堕棿娈?
        private Long alertCount;             // 鍛婅鏁伴噺
        private Double resolutionTime;        // 骞冲潎瑙ｅ喅鏃堕棿
        private Double resolutionRate;        // 瑙ｅ喅鐜?
        private List<String> topAlertTypes;   // 涓昏鍛婅绫诲瀷
    }

    /**
     * 鍛婅Top椤圭洰
     */
    class AlertTopItem {
        private String itemName;             // 椤圭洰鍚嶇О
        private Long count;                  // 鏁伴噺
        private Double percentage;            // 鐧惧垎姣?
        private String trend;                // 瓒嬪娍锛圲P/DOWN/STABLE锛?
    }

    /**
     * 鍛婅瑙勫垯閰嶇疆璇锋眰
     */
    class AlertRuleConfigureRequest {
        private String ruleId;               // 瑙勫垯ID锛堟柊澧炴椂涓虹┖锛?
        private String ruleName;             // 瑙勫垯鍚嶇О
        private String ruleDescription;      // 瑙勫垯鎻忚堪
        private String ruleType;             // 瑙勫垯绫诲瀷锛圱HRESHOLD/PATTERN/ANOMALY/COMPOSITE锛?
        private Boolean enabled;             // 鏄惁鍚敤
        private Integer priority;            // 浼樺厛绾?
        private String conditionExpression;  // 鏉′欢琛ㄨ揪寮?
        private List<AlertRuleAction> actions; // 鍔ㄤ綔鍒楄〃
        private String evaluationInterval;   // 璇勪及闂撮殧
        private Integer consecutiveFailures; // 杩炵画澶辫触娆℃暟
        private String severityLevel;         // 榛樿涓ラ噸绛夌骇
        private List<String> tags;           // 鏍囩
        private Map<String, Object> ruleParameters; // 瑙勫垯鍙傛暟
    }

    /**
     * 鍛婅瑙勫垯鍔ㄤ綔
     */
    class AlertRuleAction {
        private String actionType;           // 鍔ㄤ綔绫诲瀷锛圢OTIFICATION/ESCALATION/AUTO_REMEDY/WEBHOOK锛?
        private String actionName;           // 鍔ㄤ綔鍚嶇О
        private Map<String, Object> actionParameters; // 鍔ㄤ綔鍙傛暟
        private Boolean enabled;             // 鏄惁鍚敤
        private Integer order;                // 鎵ц椤哄簭
        private String condition;            // 鎵ц鏉′欢
    }

    /**
     * 鍛婅瑙勫垯缁撴灉
     */
    class AlertRuleResult {
        private String ruleId;               // 瑙勫垯ID
        private String ruleName;             // 瑙勫垯鍚嶇О
        private Boolean enabled;             // 鏄惁鍚敤
        private String status;               // 鐘舵€侊紙ACTIVE/INACTIVE/ERROR锛?
        private LocalDateTime lastEvaluated;  // 鏈€鍚庤瘎浼版椂闂?
        private Integer evaluationCount;     // 璇勪及娆℃暟
        private Integer triggerCount;         // 瑙﹀彂娆℃暟
        private String validationMessage;    // 楠岃瘉娑堟伅
        private List<String> warnings;       // 璀﹀憡淇℃伅
    }

    /**
     * 鍛婅瑙勫垯鏌ヨ璇锋眰
     */
    class AlertRuleQueryRequest {
        private String ruleId;               // 瑙勫垯ID
        private String ruleName;             // 瑙勫垯鍚嶇О
        private String ruleType;             // 瑙勫垯绫诲瀷
        private Boolean enabled;             // 鏄惁鍚敤
        private String status;               // 鐘舵€?
        private List<String> tags;           // 鏍囩
        private Integer pageNum;             // 椤电爜
        private Integer pageSize;            // 姣忛〉澶у皬
    }

    /**
     * 鍛婅瑙勫垯瑙嗗浘瀵硅薄
     */
    class AlertRuleVO {
        private String ruleId;               // 瑙勫垯ID
        private String ruleName;             // 瑙勫垯鍚嶇О
        private String ruleDescription;      // 瑙勫垯鎻忚堪
        private String ruleType;             // 瑙勫垯绫诲瀷
        private Boolean enabled;             // 鏄惁鍚敤
        private Integer priority;            // 浼樺厛绾?
        private String conditionExpression;  // 鏉′欢琛ㄨ揪寮?
        private List<AlertRuleAction> actions; // 鍔ㄤ綔鍒楄〃
        private String evaluationInterval;   // 璇勪及闂撮殧
        private String severityLevel;         // 榛樿涓ラ噸绛夌骇
        private LocalDateTime createTime;     // 鍒涘缓鏃堕棿
        private LocalDateTime updateTime;     // 鏇存柊鏃堕棿
        private LocalDateTime lastEvaluated;  // 鏈€鍚庤瘎浼版椂闂?
        private Integer triggerCount;         // 瑙﹀彂娆℃暟
        private String status;               // 鐘舵€?
        private List<String> tags;           // 鏍囩
    }

    /**
     * 绯荤粺鍋ュ悍妫€鏌ヨ姹?
     */
    class SystemHealthCheckRequest {
        private List<String> checkCategories; // 妫€鏌ョ被鍒?
        private List<String> checkItems;       // 妫€鏌ラ」鐩?
        private Boolean includeDetails;       // 鏄惁鍖呭惈璇︾粏淇℃伅
        private Boolean generateReport;       // 鏄惁鐢熸垚鎶ュ憡
        private String reportFormat;          // 鎶ュ憡鏍煎紡锛圝SON/HTML/PDF锛?
        private Map<String, Object> checkParameters; // 妫€鏌ュ弬鏁?
    }

    /**
     * 绯荤粺鍋ュ悍妫€鏌ョ粨鏋?
     */
    class SystemHealthCheckResult {
        private String checkId;              // 妫€鏌D
        private LocalDateTime checkTime;     // 妫€鏌ユ椂闂?
        private String overallHealth;        // 鏁翠綋鍋ュ悍鐘舵€侊紙HEALTHY/WARNING/CRITICAL锛?
        private Double overallScore;          // 鏁翠綋璇勫垎锛?-100锛?
        private List<HealthCheckItem> checkItems; // 妫€鏌ラ」鐩垪琛?
        private Map<String, Object> systemMetrics; // 绯荤粺鎸囨爣
        private List<String> recommendations; // 寤鸿
        private String reportUrl;            // 鎶ュ憡URL
        private Integer totalChecks;         // 鎬绘鏌ユ暟
        private Integer passedChecks;        // 閫氳繃妫€鏌ユ暟
        private Integer failedChecks;        // 澶辫触妫€鏌ユ暟
        private Integer warningChecks;       // 璀﹀憡妫€鏌ユ暟
    }

    /**
     * 鍋ュ悍妫€鏌ラ」鐩?
     */
    class HealthCheckItem {
        private String itemName;             // 椤圭洰鍚嶇О
        private String category;             // 绫诲埆
        private String status;               // 鐘舵€侊紙PASS/WARN/FAIL/SKIP锛?
        private Double score;                // 璇勫垎
        private String message;              // 娑堟伅
        private Map<String, Object> details;  // 璇︾粏淇℃伅
        private String recommendation;       // 寤鸿
        private LocalDateTime checkTime;     // 妫€鏌ユ椂闂?
    }

    /**
     * 鏁呴殰鑷剤璇锋眰
     */
    class SelfHealingRequest {
        private String incidentId;           // 浜嬩欢ID
        private String failureType;          // 鏁呴殰绫诲瀷
        private String failureDescription;   // 鏁呴殰鎻忚堪
        private String affectedComponent;    // 鍙楀奖鍝嶇粍浠?
        private Map<String, Object> failureData; // 鏁呴殰鏁版嵁
        private Boolean requireApproval;     // 鏄惁闇€瑕佸鎵?
        private String selfHealingStrategy;  // 鑷剤绛栫暐
        private Integer maxRetries;           // 鏈€澶ч噸璇曟鏁?
        private Integer timeoutSeconds;       // 瓒呮椂鏃堕棿
        private Map<String, Object> healingParameters; // 鑷剤鍙傛暟
    }

    /**
     * 鏁呴殰鑷剤缁撴灉
     */
    class SelfHealingResult {
        private String healingId;            // 鑷剤ID
        private String incidentId;           // 浜嬩欢ID
        private Boolean healingSuccess;       // 鑷剤鏄惁鎴愬姛
        private String healingStrategy;      // 鑷剤绛栫暐
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private Integer duration;            // 鎸佺画鏃堕棿锛堢锛?
        private Integer attemptCount;         // 灏濊瘯娆℃暟
        private String finalStatus;          // 鏈€缁堢姸鎬?
        private String failureReason;        // 澶辫触鍘熷洜
        private List<String> actionsTaken;   // 宸叉墽琛屽姩浣?
        private Boolean requireManualIntervention; // 鏄惁闇€瑕佷汉宸ュ共棰?
        private Map<String, Object> healingMetrics; // 鑷剤鎸囨爣
    }

    /**
     * 鍛婅瓒嬪娍棰勬祴璇锋眰
     */
    class AlertTrendPredictionRequest {
        private String predictionModel;      // 棰勬祴妯″瀷锛圠INEAR/SEASONAL/ARIMA/ML锛?
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private String predictionPeriod;     // 棰勬祴鍛ㄦ湡锛圚OURLY/DAILY/WEEKLY/MONTHLY锛?
        private List<String> alertTypes;      // 鍛婅绫诲瀷
        private List<String> sourceSystems;   // 鏉ユ簮绯荤粺
        private Integer predictionDays;      // 棰勬祴澶╂暟
        private Double confidenceThreshold;   // 缃俊搴﹂槇鍊?
        private Boolean includeSeasonalFactors; // 鏄惁鍖呭惈瀛ｈ妭鎬у洜绱?
        private Map<String, Object> modelParameters; // 妯″瀷鍙傛暟
    }

    /**
     * 鍛婅瓒嬪娍棰勬祴缁撴灉
     */
    class AlertTrendPredictionResult {
        private String predictionId;         // 棰勬祴ID
        private String predictionModel;      // 棰勬祴妯″瀷
        private LocalDateTime predictionTime; // 棰勬祴鏃堕棿
        private List<AlertPredictionData> predictions; // 棰勬祴鏁版嵁
        private Double modelAccuracy;        // 妯″瀷鍑嗙‘搴?
        private Double confidenceScore;      // 缃俊搴﹁瘎鍒?
        private List<String> identifiedPatterns; // 璇嗗埆鐨勬ā寮?
        private List<AlertAnomaly> anomalies; // 寮傚父鐐?
        private Map<String, Object> modelMetrics; // 妯″瀷鎸囨爣
        private List<String> recommendations; // 寤鸿
    }

    /**
     * 鍛婅棰勬祴鏁版嵁
     */
    class AlertPredictionData {
        private String timeSlot;             // 鏃堕棿娈?
        private Long predictedCount;         // 棰勬祴鏁伴噺
        private Double confidenceIntervalLower; // 缃俊鍖洪棿涓嬮檺
        private Double confidenceIntervalUpper; // 缃俊鍖洪棿涓婇檺
        private String trendDirection;       // 瓒嬪娍鏂瑰悜锛圲P/DOWN/STABLE锛?
        private List<String> influencingFactors; // 褰卞搷鍥犵礌
    }

    /**
     * 鍛婅寮傚父鐐?
     */
    class AlertAnomaly {
        private String timeSlot;             // 鏃堕棿娈?
        private Long actualCount;            // 瀹為檯鏁伴噺
        private Long predictedCount;         // 棰勬祴鏁伴噺
        private Double deviationScore;        // 鍋忓樊璇勫垎
        private String anomalyType;          // 寮傚父绫诲瀷锛圫PIKE/DROP/OUTLIER锛?
        private String description;          // 鎻忚堪
        private Boolean needAttention;       // 鏄惁闇€瑕佸叧娉?
    }
}
package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 瑙嗛鑱斿姩鐩戞帶鏈嶅姟鎺ュ彛
 * <p>
 * 鎻愪緵闂ㄧ涓庤棰戠洃鎺х郴缁熺殑鏅鸿兘鑱斿姩鍔熻兘锛?
 * - 闂ㄧ浜嬩欢瑙﹀彂瀹炴椂瑙嗛鐩戞帶
 * - 浜鸿劯璇嗗埆楠岃瘉涓庤韩浠界‘璁?
 * - 寮傚父琛屼负妫€娴嬩笌鍛婅鎺ㄩ€?
 * - 瑙嗛鐗囨鑷姩褰曞埗涓庡瓨鍌?
 * - 澶氱敾闈㈢洃鎺т笌浜戝彴鎺у埗
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
public interface VideoLinkageMonitorService {

    /**
     * 闂ㄧ浜嬩欢瑙﹀彂瑙嗛鑱斿姩
     * <p>
     * 褰撻棬绂佷簨浠跺彂鐢熸椂锛岃嚜鍔ㄨЕ鍙戣棰戠洃鎺х郴缁燂細
     * - 鍚姩鐩稿叧鎽勫儚澶村綍鍍?
     * - 鎵ц浜鸿劯璇嗗埆楠岃瘉
     * - 璁板綍浜嬩欢瑙嗛鐗囨
     * - 瀹炴椂鎺ㄩ€佺粰鐩戞帶涓績
     * </p>
     *
     * @param request 鑱斿姩璇锋眰
     * @return 鑱斿姩缁撴灉
     */
    ResponseDTO<VideoLinkageResult> triggerVideoLinkage(VideoLinkageRequest request);

    /**
     * 瀹炴椂瑙嗛娴佽幏鍙?
     * <p>
     * 鑾峰彇鎸囧畾鎽勫儚澶寸殑瀹炴椂瑙嗛娴侊細
     * - 澶氬崗璁敮鎸侊紙RTSP/RTMP/HLS/WebRTC锛?
     * - 澶氱爜娴佽嚜閫傚簲鍒囨崲
     * - 瀹炴椂鎬ц兘鐩戞帶
     * - 鏂嚎鑷姩閲嶈繛
     * </p>
     *
     * @param request 瑙嗛娴佽姹?
     * @return 瑙嗛娴佷俊鎭?
     */
    ResponseDTO<VideoStreamResult> getRealTimeStream(VideoStreamRequest request);

    /**
     * 浜鸿劯璇嗗埆楠岃瘉
     * <p>
     * 閫氳繃瑙嗛娴佽繘琛屽疄鏃朵汉鑴歌瘑鍒獙璇侊細
     * - 娲讳綋妫€娴嬮槻姝㈡楠?
     * - 澶氳搴︿汉鑴歌瘑鍒?
     * - 鐗瑰緛鎻愬彇涓庢瘮瀵?
     * - 缃俊搴﹁瘎浼?
     * </p>
     *
     * @param request 浜鸿劯璇嗗埆璇锋眰
     * @return 璇嗗埆缁撴灉
     */
    ResponseDTO<FaceRecognitionResult> performFaceRecognition(FaceRecognitionRequest request);

    /**
     * 寮傚父琛屼负妫€娴?
     * <p>
     * 鍩轰簬瑙嗛娴佺殑鏅鸿兘寮傚父琛屼负妫€娴嬶細
     * - 灏鹃殢妫€娴?
     * - 鍙枒寰樺緤璇嗗埆
     * - 寮鸿闂叆妫€娴?
     * - 寮傚父鐗╁搧鎼哄甫璇嗗埆
     * </p>
     *
     * @param request 琛屼负妫€娴嬭姹?
     * @return 妫€娴嬬粨鏋?
     */
    ResponseDTO<AbnormalBehaviorResult> detectAbnormalBehavior(AbnormalBehaviorRequest request);

    /**
     * 瑙嗛鐗囨褰曞埗绠＄悊
     * <p>
     * 绠＄悊浜嬩欢鐩稿叧鐨勮棰戠墖娈靛綍鍒讹細
     * - 浜嬩欢瑙﹀彂褰曞埗
     * - 棰勫綍鍒剁紦鍐?
     * - 鏅鸿兘鍓緫浼樺寲
     * - 浜戠鍚屾瀛樺偍
     * </p>
     *
     * @param request 褰曞埗璇锋眰
     * @return 褰曞埗缁撴灉
     */
    ResponseDTO<VideoRecordingResult> manageVideoRecording(VideoRecordingRequest request);

    /**
     * 澶氱敾闈㈢洃鎺у睍绀?
     * <p>
     * 鎻愪緵澶氱敾闈㈠疄鏃剁洃鎺у睍绀哄姛鑳斤細
     * - 鍔ㄦ€佸竷灞€璋冩暣
     * - 鐢婚潰杞挱璁剧疆
     * - 閲嶇偣鐢婚潰绐佸嚭
     * - 鍛婅鐢婚潰鑷姩鍒囨崲
     * </p>
     *
     * @param request 澶氱敾闈㈣姹?
     * @return 鐩戞帶鐢婚潰淇℃伅
     */
    ResponseDTO<MultiScreenResult> getMultiScreenView(MultiScreenRequest request);

    /**
     * 浜戝彴鎺у埗鎿嶄綔
     * <p>
     * 杩滅▼鎺у埗鎽勫儚澶寸殑浜戝彴鎿嶄綔锛?
     * - 涓婁笅宸﹀彸绉诲姩
     * - 鍙樺€嶅鐒︽帶鍒?
     * - 棰勮浣嶇疆璋冪敤
     * - 宸¤埅璺緞璁剧疆
     * </p>
     *
     * @param request 浜戝彴鎺у埗璇锋眰
     * @return 鎺у埗缁撴灉
     */
    ResponseDTO<PTZControlResult> controlPTZCamera(PTZControlRequest request);

    /**
     * 鍘嗗彶瑙嗛鍥炴斁
     * <p>
     * 鎻愪緵鍘嗗彶瑙嗛鐨勫洖鏀惧姛鑳斤細
     * - 鏃堕棿绮剧‘瀹氫綅
     * - 鍊嶉€熸挱鏀炬帶鍒?
     * - 浜嬩欢鐗囨璺宠浆
     * - 涓嬭浇鍒嗕韩鍔熻兘
     * </p>
     *
     * @param request 鍥炴斁璇锋眰
     * @return 鍥炴斁淇℃伅
     */
    ResponseDTO<VideoPlaybackResult> playbackHistoricalVideo(VideoPlaybackRequest request);

    /**
     * 鑾峰彇鑱斿姩浜嬩欢鍒楄〃
     * <p>
     * 鏌ヨ瑙嗛鑱斿姩浜嬩欢鐨勫巻鍙茶褰曪細
     * - 浜嬩欢绫诲瀷绛涢€?
     * - 鏃堕棿鑼冨洿鏌ヨ
     * - 澶勭悊鐘舵€佽窡韪?
     * - 缁熻鍒嗘瀽鎶ヨ〃
     * </p>
     *
     * @param request 鏌ヨ璇锋眰
     * @return 浜嬩欢鍒楄〃
     */
    ResponseDTO<List<VideoLinkageEventVO>> getLinkageEvents(VideoLinkageEventQueryRequest request);

    /**
     * 鑾峰彇鐩戞帶缁熻鎶ュ憡
     * <p>
     * 鐢熸垚瑙嗛鐩戞帶绯荤粺鐨勭粺璁″垎鏋愭姤鍛婏細
     * - 璁惧杩愯鐘舵€佺粺璁?
     * - 浜嬩欢澶勭悊鏁堢巼鍒嗘瀽
     * - 瀛樺偍绌洪棿浣跨敤鎯呭喌
     * - 绯荤粺鎬ц兘鎸囨爣鎶ュ憡
     * </p>
     *
     * @param request 鎶ュ憡璇锋眰
     * @return 缁熻鎶ュ憡
     */
    ResponseDTO<MonitorStatisticsVO> getMonitorStatistics(MonitorStatisticsRequest request);

    // ==================== 鍐呴儴鏁版嵁浼犺緭瀵硅薄 ====================

    /**
     * 瑙嗛鑱斿姩璇锋眰
     */
    class VideoLinkageRequest {
        private String accessEventId;      // 闂ㄧ浜嬩欢ID
        private Long deviceId;              // 闂ㄧ璁惧ID
        private String userId;               // 鐢ㄦ埛ID
        private String userName;             // 鐢ㄦ埛濮撳悕
        private Integer accessType;          // 璁块棶绫诲瀷锛?-杩涘叆 2-绂诲紑锛?
        private String areaId;               // 鍖哄煙ID
        private String accessTime;           // 璁块棶鏃堕棿
        private Integer verificationResult;   // 楠岃瘉缁撴灉锛?-鎴愬姛 2-澶辫触锛?
        private String eventType;            // 浜嬩欢绫诲瀷
        private List<Long> cameraIds;        // 鍏宠仈鎽勫儚澶碔D鍒楄〃
        private Boolean enableRecording;     // 鏄惁鍚敤褰曞埗
        private Boolean enableFaceRecognition; // 鏄惁鍚敤浜鸿劯璇嗗埆
        private Integer recordDuration;      // 褰曞埗鏃堕暱锛堢锛?
        private String description;          // 浜嬩欢鎻忚堪
    }

    /**
     * 瑙嗛鑱斿姩缁撴灉
     */
    class VideoLinkageResult {
        private String linkageId;            // 鑱斿姩ID
        private String accessEventId;        // 闂ㄧ浜嬩欢ID
        private LocalDateTime triggerTime;    // 瑙﹀彂鏃堕棿
        private List<CameraLinkageInfo> cameraLinkages; // 鎽勫儚澶磋仈鍔ㄤ俊鎭?
        private List<String> recordingIds;   // 褰曞埗鏂囦欢ID鍒楄〃
        private FaceVerificationResult faceVerification; // 浜鸿劯楠岃瘉缁撴灉
        private List<AbnormalEvent> detectedEvents; // 妫€娴嬪埌鐨勫紓甯镐簨浠?
        private Integer linkageStatus;       // 鑱斿姩鐘舵€侊紙1-鎴愬姛 2-閮ㄥ垎鎴愬姛 3-澶辫触锛?
        private String statusDescription;    // 鐘舵€佹弿杩?
        private Integer processingDuration;  // 澶勭悊鏃堕暱锛堟绉掞級
    }

    /**
     * 鎽勫儚澶磋仈鍔ㄤ俊鎭?
     */
    class CameraLinkageInfo {
        private Long cameraId;               // 鎽勫儚澶碔D
        private String cameraName;           // 鎽勫儚澶村悕绉?
        private String cameraLocation;       // 鎽勫儚澶翠綅缃?
        private String streamUrl;            // 瑙嗛娴佸湴鍧€
        private Boolean recordingEnabled;    // 鏄惁鍚敤褰曞埗
        private String recordingId;          // 褰曞埗鏂囦欢ID
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private Integer linkagePriority;     // 鑱斿姩浼樺厛绾?
        private String cameraStatus;         // 鎽勫儚澶寸姸鎬?
    }

    /**
     * 浜鸿劯楠岃瘉缁撴灉
     */
    class FaceVerificationResult {
        private Boolean verificationSuccess; // 楠岃瘉鏄惁鎴愬姛
        private Double confidenceScore;      // 缃俊搴﹁瘎鍒?
        private String matchedUserId;        // 鍖归厤鐨勭敤鎴稩D
        private String matchedUserName;      // 鍖归厤鐨勭敤鎴峰鍚?
        private String faceImageUrl;         // 鎶撴媿鐨勪汉鑴稿浘鐗嘦RL
        private LocalDateTime verifyTime;     // 楠岃瘉鏃堕棿
        private String verifyMethod;         // 楠岃瘉鏂规硶
        private List<String> failureReasons; // 澶辫触鍘熷洜
    }

    /**
     * 寮傚父浜嬩欢
     */
    class AbnormalEvent {
        private String eventId;              // 浜嬩欢ID
        private String eventType;            // 浜嬩欢绫诲瀷
        private String description;          // 浜嬩欢鎻忚堪
        private LocalDateTime eventTime;     // 浜嬩欢鏃堕棿
        private Integer severityLevel;       // 涓ラ噸绛夌骇
        private String imageUrl;             // 浜嬩欢鎴浘URL
        private String videoClipUrl;         // 瑙嗛鐗囨URL
        private Boolean needAlert;           // 鏄惁闇€瑕佸憡璀?
    }

    /**
     * 瑙嗛娴佽姹?
     */
    class VideoStreamRequest {
        private Long cameraId;               // 鎽勫儚澶碔D
        private Integer channelId;           // 閫氶亾ID
        private String streamType;           // 娴佺被鍨嬶紙MAIN/SUB/MOBILE锛?
        private String protocol;             // 鍗忚绫诲瀷锛圧TSP/RTMP/HLS/WebRTC锛?
        private Integer quality;             // 鐢昏川璐ㄩ噺锛?-楂樻竻 2-鏍囨竻 3-娴佺晠锛?
        private Boolean enableAudio;         // 鏄惁鍚敤闊抽
        private Integer maxConnections;      // 鏈€澶ц繛鎺ユ暟
        private String clientType;           // 瀹㈡埛绔被鍨嬶紙WEB/MOBILE/DESKTOP锛?
    }

    /**
     * 瑙嗛娴佺粨鏋?
     */
    class VideoStreamResult {
        private String streamId;             // 娴両D
        private String streamUrl;            // 娴佸湴鍧€
        private String streamType;           // 娴佺被鍨?
        private String protocol;             // 鍗忚绫诲瀷
        private String quality;              // 鐢昏川璐ㄩ噺
        private Integer width;               // 瑙嗛瀹藉害
        private Integer height;              // 瑙嗛楂樺害
        private Integer fps;                 // 甯х巼
        private Long bitrate;                // 鐮佺巼
        private String expireTime;           // 杩囨湡鏃堕棿
        private String accessToken;          // 璁块棶浠ょ墝
        private Boolean audioEnabled;        // 闊抽鏄惁鍚敤
    }

    /**
     * 浜鸿劯璇嗗埆璇锋眰
     */
    class FaceRecognitionRequest {
        private String streamId;             // 瑙嗛娴両D
        private Long userId;                 // 鐢ㄦ埛ID
        private String userPhotoUrl;         // 鐢ㄦ埛鐓х墖URL
        private Integer recognitionTimeout;   // 璇嗗埆瓒呮椂鏃堕棿锛堢锛?
        private Double confidenceThreshold;  // 缃俊搴﹂槇鍊?
        private Boolean enableLivenessCheck; // 鏄惁鍚敤浜鸿劯妫€娴?
        private Integer maxAttempts;         // 鏈€澶у皾璇曟鏁?
    }

    /**
     * 浜鸿劯璇嗗埆缁撴灉
     */
    class FaceRecognitionResult {
        private Boolean recognitionSuccess;  // 璇嗗埆鏄惁鎴愬姛
        private Double confidenceScore;      // 缃俊搴﹁瘎鍒?
        private String matchedUserId;        // 鍖归厤鐨勭敤鎴稩D
        private String matchedUserName;      // 鍖归厤鐨勭敤鎴峰鍚?
        private String capturedFaceUrl;      // 鎶撴媿鐨勪汉鑴稿浘鐗嘦RL
        private LocalDateTime recognitionTime; // 璇嗗埆鏃堕棿
        private Integer faceCount;           // 妫€娴嬪埌鐨勪汉鑴告暟閲?
        private String faceQuality;          // 浜鸿劯璐ㄩ噺璇勪及
        private LivenessCheckResult livenessCheck; // 娲讳綋妫€娴嬬粨鏋?
        private List<String> failureReasons; // 璇嗗埆澶辫触鍘熷洜
    }

    /**
     * 娲讳綋妫€娴嬬粨鏋?
     */
    class LivenessCheckResult {
        private Boolean isLive;              // 鏄惁涓烘椿浣?
        private Double livenessScore;        // 娲讳綋璇勫垎
        private String detectionMethod;      // 妫€娴嬫柟娉?
        private List<String> spoofingIndicators; // 娆洪獥鎸囨爣
    }

    /**
     * 寮傚父琛屼负妫€娴嬭姹?
     */
    class AbnormalBehaviorRequest {
        private String streamId;             // 瑙嗛娴両D
        private String areaId;               // 鍖哄煙ID
        private List<String> behaviorTypes;  // 妫€娴嬬殑琛屼负绫诲瀷
        private Integer detectionDuration;   // 妫€娴嬫椂闀匡紙绉掞級
        private Double sensitivityLevel;     // 鐏垫晱搴︾骇鍒?
        private Boolean enableAlert;         // 鏄惁鍚敤鍛婅
    }

    /**
     * 寮傚父琛屼负妫€娴嬬粨鏋?
     */
    class AbnormalBehaviorResult {
        private String detectionId;          // 妫€娴婭D
        private Boolean hasAbnormalBehavior;  // 鏄惁鏈夊紓甯歌涓?
        private List<DetectedBehavior> detectedBehaviors; // 妫€娴嬪埌鐨勮涓?
        private LocalDateTime detectionTime;  // 妫€娴嬫椂闂?
        private Integer behaviorCount;       // 寮傚父琛屼负鏁伴噺
        private Double riskScore;            // 椋庨櫓璇勫垎
        private String alertLevel;            // 鍛婅绾у埆
        private List<String> recommendedActions; // 寤鸿鎺柦
    }

    /**
     * 妫€娴嬪埌鐨勮涓?
     */
    class DetectedBehavior {
        private String behaviorType;        // 琛屼负绫诲瀷
        private String description;          // 琛屼负鎻忚堪
        private Double confidence;           // 缃俊搴?
        private LocalDateTime occurTime;     // 鍙戠敓鏃堕棿
        private String imageUrl;             // 琛屼负鎴浘URL
        private String videoClipUrl;         // 鐩稿叧瑙嗛鐗囨
        private Integer severityLevel;       // 涓ラ噸绛夌骇
    }

    /**
     * 瑙嗛褰曞埗璇锋眰
     */
    class VideoRecordingRequest {
        private String linkageId;            // 鑱斿姩ID
        private List<Long> cameraIds;        // 鎽勫儚澶碔D鍒楄〃
        private Integer preRecordSeconds;    // 棰勫綍鍒舵椂闀匡紙绉掞級
        private Integer postRecordSeconds;   // 鍚庡綍鍒舵椂闀匡紙绉掞級
        private String recordingQuality;     // 褰曞埗璐ㄩ噺
        private Boolean enableAudio;         // 鏄惁褰曞埗闊抽
        private String storageType;          // 瀛樺偍绫诲瀷锛圠OCAL/CLOUD/HYBRID锛?
        private Integer retentionDays;       // 淇濈暀澶╂暟
    }

    /**
     * 瑙嗛褰曞埗缁撴灉
     */
    class VideoRecordingResult {
        private String recordingTaskId;      // 褰曞埗浠诲姟ID
        private List<RecordingFileInfo> recordingFiles; // 褰曞埗鏂囦欢淇℃伅
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private Long totalDuration;          // 鎬绘椂闀匡紙绉掞級
        private Long totalSize;              // 鎬诲ぇ灏忥紙瀛楄妭锛?
        private Integer recordingStatus;     // 褰曞埗鐘舵€?
        private List<String> tags;           // 鏍囩鍒楄〃
    }

    /**
     * 褰曞埗鏂囦欢淇℃伅
     */
    class RecordingFileInfo {
        private String recordingId;          // 褰曞埗鏂囦欢ID
        private Long cameraId;               // 鎽勫儚澶碔D
        private String fileName;             // 鏂囦欢鍚?
        private String filePath;             // 鏂囦欢璺緞
        private Long fileSize;               // 鏂囦欢澶у皬锛堝瓧鑺傦級
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private Integer duration;            // 鏃堕暱锛堢锛?
        private String quality;              // 鐢昏川璐ㄩ噺
        private String format;               // 鏂囦欢鏍煎紡
        private String downloadUrl;          // 涓嬭浇鍦板潃
    }

    /**
     * 澶氱敾闈㈢洃鎺ц姹?
     */
    class MultiScreenRequest {
        private List<Long> cameraIds;        // 鎽勫儚澶碔D鍒楄〃
        private String layoutType;           // 甯冨眬绫诲瀷锛?x1/2x2/3x3/4x4/CUSTOM锛?
        private Integer displayCols;         // 鏄剧ず鍒楁暟
        private Integer displayRows;         // 鏄剧ず琛屾暟
        private String quality;              // 鐢昏川璐ㄩ噺
        private Boolean enableAudio;         // 鏄惁鍚敤闊抽
        private Integer refreshInterval;     // 鍒锋柊闂撮殧锛堢锛?
        private Boolean autoSwitchAlert;     // 鏄惁鑷姩鍒囨崲鍛婅鐢婚潰
    }

    /**
     * 澶氱敾闈㈢洃鎺х粨鏋?
     */
    class MultiScreenResult {
        private String sessionId;            // 鐩戞帶浼氳瘽ID
        private String layoutType;           // 甯冨眬绫诲瀷
        private List<CameraStreamInfo> cameraStreams; // 鎽勫儚澶磋棰戞祦淇℃伅
        private LocalDateTime createTime;     // 鍒涘缓鏃堕棿
        private Integer totalCameras;        // 鎬绘憚鍍忓ご鏁?
        private Integer activeCameras;       // 娲昏穬鎽勫儚澶存暟
        private String monitorUrl;           // 鐩戞帶鍦板潃
        private List<String> availableQualities; // 鍙敤鐢昏川閫夐」
    }

    /**
     * 鎽勫儚澶磋棰戞祦淇℃伅
     */
    class CameraStreamInfo {
        private Long cameraId;               // 鎽勫儚澶碔D
        private String cameraName;           // 鎽勫儚澶村悕绉?
        private String streamUrl;            // 瑙嗛娴佸湴鍧€
        private String position;             // 鍦ㄥ鐢婚潰涓殑浣嶇疆
        private Integer width;               // 瑙嗛瀹藉害
        private Integer height;              // 瑙嗛楂樺害
        private String status;               // 娴佺姸鎬?
        private LocalDateTime lastUpdateTime; // 鏈€鍚庢洿鏂版椂闂?
    }

    /**
     * 浜戝彴鎺у埗璇锋眰
     */
    class PTZControlRequest {
        private Long cameraId;               // 鎽勫儚澶碔D
        private String action;               // 鎺у埗鍔ㄤ綔
        private Integer speed;               // 鎺у埗閫熷害锛?-10锛?
        private Integer duration;            // 鎸佺画鏃堕棿锛堟绉掞級
        private Double pan;                  // 姘村钩杞姩瑙掑害
        private Double tilt;                 // 鍨傜洿杞姩瑙掑害
        private Double zoom;                 // 鍙樺€嶅€嶆暟
        private Integer presetId;            // 棰勮浣岻D
        private String patrolPath;           // 宸¤埅璺緞
    }

    /**
     * 浜戝彴鎺у埗缁撴灉
     */
    class PTZControlResult {
        private Boolean controlSuccess;      // 鎺у埗鏄惁鎴愬姛
        private String commandId;            // 鍛戒护ID
        private LocalDateTime executeTime;    // 鎵ц鏃堕棿
        private Integer responseTime;        // 鍝嶅簲鏃堕棿锛堟绉掞級
        private String currentPosition;       // 褰撳墠浣嶇疆
        private Integer currentZoom;         // 褰撳墠鍙樺€?
        private List<PTZPresetInfo> availablePresets; // 鍙敤棰勮浣?
    }

    /**
     * PTZ棰勮浣嶄俊鎭?
     */
    class PTZPresetInfo {
        private Integer presetId;            // 棰勮浣岻D
        private String presetName;           // 棰勮浣嶅悕绉?
        private String description;          // 鎻忚堪
        private String position;             // 浣嶇疆淇℃伅
    }

    /**
     * 瑙嗛鍥炴斁璇锋眰
     */
    class VideoPlaybackRequest {
        private Long cameraId;               // 鎽勫儚澶碔D
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private String playbackQuality;      // 鍥炴斁璐ㄩ噺
        private Double playbackSpeed;        // 鎾斁閫熷害
        private Boolean enableAudio;         // 鏄惁鍚敤闊抽
        private String eventType;            // 浜嬩欢绫诲瀷绛涢€?
    }

    /**
     * 瑙嗛鍥炴斁缁撴灉
     */
    class VideoPlaybackResult {
        private String playbackId;           // 鍥炴斁ID
        private String playbackUrl;          // 鍥炴斁鍦板潃
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private Integer totalDuration;       // 鎬绘椂闀匡紙绉掞級
        private List<VideoEventMarker> eventMarkers; // 浜嬩欢鏍囪鐐?
        private String quality;              // 鐢昏川璐ㄩ噺
        private Boolean audioAvailable;      // 闊抽鏄惁鍙敤
    }

    /**
     * 瑙嗛浜嬩欢鏍囪鐐?
     */
    class VideoEventMarker {
        private String eventId;              // 浜嬩欢ID
        private LocalDateTime eventTime;     // 浜嬩欢鏃堕棿
        private String eventType;            // 浜嬩欢绫诲瀷
        private String description;          // 浜嬩欢鎻忚堪
        private Integer offsetSeconds;       // 鍋忕Щ绉掓暟
        private String thumbnailUrl;         // 缂╃暐鍥綰RL
    }

    /**
     * 瑙嗛鑱斿姩浜嬩欢鏌ヨ璇锋眰
     */
    class VideoLinkageEventQueryRequest {
        private String userId;               // 鐢ㄦ埛ID
        private Long deviceId;               // 璁惧ID
        private String areaId;               // 鍖哄煙ID
        private String eventType;            // 浜嬩欢绫诲瀷
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private Integer linkageStatus;       // 鑱斿姩鐘舵€?
        private Integer pageNum;             // 椤电爜
        private Integer pageSize;            // 姣忛〉澶у皬
    }

    /**
     * 瑙嗛鑱斿姩浜嬩欢瑙嗗浘瀵硅薄
     */
    class VideoLinkageEventVO {
        private String linkageId;            // 鑱斿姩ID
        private String accessEventId;        // 闂ㄧ浜嬩欢ID
        private String userId;               // 鐢ㄦ埛ID
        private String userName;             // 鐢ㄦ埛濮撳悕
        private Long deviceId;               // 璁惧ID
        private String deviceName;           // 璁惧鍚嶇О
        private String areaId;               // 鍖哄煙ID
        private String areaName;             // 鍖哄煙鍚嶇О
        private String eventType;            // 浜嬩欢绫诲瀷
        private String accessTime;           // 璁块棶鏃堕棿
        private Integer linkageStatus;       // 鑱斿姩鐘舵€?
        private LocalDateTime triggerTime;    // 瑙﹀彂鏃堕棿
        private Integer cameraCount;         // 鍏宠仈鎽勫儚澶存暟閲?
        private Boolean faceVerificationSuccess; // 浜鸿劯楠岃瘉鏄惁鎴愬姛
        private Integer abnormalEventCount;  // 寮傚父浜嬩欢鏁伴噺
        private String description;          // 鎻忚堪
    }

    /**
     * 鐩戞帶缁熻璇锋眰
     */
    class MonitorStatisticsRequest {
        private List<Long> cameraIds;        // 鎽勫儚澶碔D鍒楄〃
        private String statisticsType;       // 缁熻绫诲瀷锛圖AILY/WEEKLY/MONTHLY锛?
        private LocalDateTime startTime;      // 寮€濮嬫椂闂?
        private LocalDateTime endTime;        // 缁撴潫鏃堕棿
        private List<String> metrics;        // 缁熻鎸囨爣
        private String groupBy;              // 鍒嗙粍鏂瑰紡
    }

    /**
     * 鐩戞帶缁熻缁撴灉
     */
    class MonitorStatisticsVO {
        private String statisticsPeriod;     // 缁熻鍛ㄦ湡
        private Long totalLinkageEvents;     // 鎬昏仈鍔ㄤ簨浠舵暟
        private Long successfulLinkages;     // 鎴愬姛鑱斿姩鏁?
        private Long faceVerificationAttempts; // 浜鸿劯楠岃瘉灏濊瘯娆℃暟
        private Long faceVerificationSuccesses; // 浜鸿劯楠岃瘉鎴愬姛娆℃暟
        private Double faceVerificationSuccessRate; // 浜鸿劯楠岃瘉鎴愬姛鐜?
        private Long abnormalBehaviorDetections; // 寮傚父琛屼负妫€娴嬫鏁?
        private Long totalRecordingDuration; // 鎬诲綍鍒舵椂闀匡紙灏忔椂锛?
        private Long totalStorageUsed;       // 鎬诲瓨鍌ㄤ娇鐢ㄩ噺锛圙B锛?
        private Double averageResponseTime;  // 骞冲潎鍝嶅簲鏃堕棿锛堟绉掞級
        private Integer onlineCameraCount;   // 鍦ㄧ嚎鎽勫儚澶存暟閲?
        private Integer totalCameraCount;    // 鎬绘憚鍍忓ご鏁伴噺
        private List<CameraStatistics> cameraStatistics; // 鎽勫儚澶寸粺璁?
    }

    /**
     * 鎽勫儚澶寸粺璁′俊鎭?
     */
    class CameraStatistics {
        private Long cameraId;               // 鎽勫儚澶碔D
        private String cameraName;           // 鎽勫儚澶村悕绉?
        private Long linkageEventCount;      // 鑱斿姩浜嬩欢鏁伴噺
        private Long recordingDuration;      // 褰曞埗鏃堕暱锛堝皬鏃讹級
        private Long storageUsed;            // 瀛樺偍浣跨敤閲忥紙GB锛?
        private String status;               // 鎽勫儚澶寸姸鎬?
        private Double onlineRate;           // 鍦ㄧ嚎鐜?
        private List<String> topEventTypes;  // 涓昏浜嬩欢绫诲瀷
    }
}
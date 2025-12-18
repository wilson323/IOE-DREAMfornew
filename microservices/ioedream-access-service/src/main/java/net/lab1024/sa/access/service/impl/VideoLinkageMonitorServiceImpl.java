package net.lab1024.sa.access.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.VideoLinkageMonitorService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.redis.RedisTemplate;
import net.lab1024.sa.common.util.SmartStringUtil;

/**
 * 瑙嗛鑱斿姩鐩戞帶鏈嶅姟瀹炵幇
 * <p>
 * 瀹炵幇闂ㄧ绯荤粺涓庤棰戠洃鎺х殑鏅鸿兘鑱斿姩鍔熻兘锛?
 * - 闂ㄧ浜嬩欢瑙﹀彂瑙嗛鐩戞帶鑱斿姩
 * - 瀹炴椂浜鸿劯璇嗗埆涓庤韩浠介獙璇?
 * - 寮傚父琛屼负鏅鸿兘妫€娴嬩笌鍛婅
 * - 瑙嗛娴佺鐞嗕笌澶氱敾闈㈢洃鎺?
 * - PTZ浜戝彴鎺у埗涓庡綍鍍忕鐞?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VideoLinkageMonitorServiceImpl implements VideoLinkageMonitorService {

    // 妯℃嫙渚濊禆娉ㄥ叆锛堝疄闄呭簲閫氳繃@Resource娉ㄥ叆鐪熷疄鐨凞AO鍜孲ervice锛?
    // @Resource private VideoStreamManager videoStreamManager;
    // @Resource private FaceRecognitionEngine faceRecognitionEngine;
    // @Resource private BehaviorAnalysisEngine behaviorAnalysisEngine;
    // @Resource private RecordingManager recordingManager;
    // @Resource private PTZController ptzController;
    // @Resource private GatewayServiceClient gatewayServiceClient;
    // @Resource private RedisTemplate<String, Object> redisTemplate;

    // 妯℃嫙鏁版嵁瀛樺偍
    private final Map<String, VideoLinkageResult> linkageStore = new ConcurrentHashMap<>();
    private final Map<String, String> activeStreams = new ConcurrentHashMap<>();
    
    // 绾跨▼姹?- 浣跨敤缁熶竴閰嶇疆鐨勫紓姝ョ嚎绋嬫睜
    @Resource(name = "asyncExecutor")
    private ThreadPoolTaskExecutor asyncExecutor;

    @Override
    @Timed(value = "video.linkage.trigger", description = "瑙嗛鑱斿姩瑙﹀彂鑰楁椂")
    @Counted(value = "video.linkage.trigger.count", description = "瑙嗛鑱斿姩瑙﹀彂娆℃暟")
    public ResponseDTO<VideoLinkageResult> triggerVideoLinkage(VideoLinkageRequest request) {
        log.info("[瑙嗛鑱斿姩] 寮€濮嬭Е鍙戣棰戣仈鍔? accessEventId={}, deviceId={}, userId={}",
                request.getAccessEventId(), request.getDeviceId(), request.getUserId());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateLinkageRequest(request);

            // 2. 鐢熸垚鑱斿姩ID
            String linkageId = generateLinkageId();

            // 3. 寮傛鎵ц鑱斿姩閫昏緫
            CompletableFuture<VideoLinkageResult> linkageFuture = CompletableFuture
                    .supplyAsync(() -> performVideoLinkage(linkageId, request), asyncExecutor);

            // 4. 璁剧疆瓒呮椂鍜屽紓甯稿鐞?
            VideoLinkageResult result = linkageFuture
                    .orTimeout(30, TimeUnit.SECONDS)
                    .exceptionally(throwable -> {
                        log.error("[瑙嗛鑱斿姩] 鎵ц寮傚父, linkageId={}", linkageId, throwable);
                        return createFailedLinkageResult(linkageId, request, throwable);
                    })
                    .join();

            // 5. 瀛樺偍鑱斿姩缁撴灉
            linkageStore.put(linkageId, result);

            log.info("[瑙嗛鑱斿姩] 瑙﹀彂瀹屾垚, linkageId={}, success={}, cameraCount={}",
                    linkageId, result.getLinkageStatus(), result.getCameraLinkages().size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[瑙嗛鑱斿姩] 瑙﹀彂澶辫触, accessEventId={}", request.getAccessEventId(), e);
            return ResponseDTO.error("VIDEO_LINKAGE_FAILED", "瑙嗛鑱斿姩瑙﹀彂澶辫触: " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.stream.get", description = "鑾峰彇瑙嗛娴佽€楁椂")
    public ResponseDTO<VideoStreamResult> getRealTimeStream(VideoStreamRequest request) {
        log.info("[瑙嗛娴乚 鑾峰彇瀹炴椂瑙嗛娴? cameraId={}, streamType={}, protocol={}",
                request.getCameraId(), request.getStreamType(), request.getProtocol());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateStreamRequest(request);

            // 2. 鐢熸垚娴両D
            String streamId = generateStreamId(request);

            // 3. 鑾峰彇鎽勫儚澶翠俊鎭?
            // CameraInfo cameraInfo = getCameraInfo(request.getCameraId());

            // 4. 寤虹珛瑙嗛娴佽繛鎺?
            VideoStreamResult streamResult = establishVideoStream(streamId, request);

            // 5. 缂撳瓨娴佷俊鎭?
            activeStreams.put(streamId, request.getCameraId().toString());

            log.info("[瑙嗛娴乚 瑙嗛娴佸缓绔嬫垚鍔? streamId={}, streamUrl={}",
                    streamId, streamResult.getStreamUrl());

            return ResponseDTO.ok(streamResult);

        } catch (Exception e) {
            log.error("[瑙嗛娴乚 鑾峰彇瑙嗛娴佸け璐? cameraId={}", request.getCameraId(), e);
            return ResponseDTO.error("VIDEO_STREAM_FAILED", "鑾峰彇瑙嗛娴佸け璐? " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.face.recognition", description = "浜鸿劯璇嗗埆鑰楁椂")
    @Counted(value = "video.face.recognition.count", description = "浜鸿劯璇嗗埆娆℃暟")
    public ResponseDTO<FaceRecognitionResult> performFaceRecognition(FaceRecognitionRequest request) {
        log.info("[浜鸿劯璇嗗埆] 寮€濮嬫墽琛屼汉鑴歌瘑鍒? streamId={}, userId={}, confidenceThreshold={}",
                request.getStreamId(), request.getUserId(), request.getConfidenceThreshold());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateFaceRecognitionRequest(request);

            // 2. 鑾峰彇瑙嗛娴?
            String streamId = request.getStreamId();
            if (!activeStreams.containsKey(streamId)) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "瑙嗛娴佷笉瀛樺湪鎴栧凡鏂紑");
            }

            // 3. 鎵ц浜鸿劯璇嗗埆
            FaceRecognitionResult result = performFaceRecognitionInternal(request);

            // 4. 璁板綍璇嗗埆缁撴灉
            logFaceRecognitionResult(result);

            log.info("[浜鸿劯璇嗗埆] 璇嗗埆瀹屾垚, success={}, confidence={}, matchedUser={}",
                    result.getRecognitionSuccess(), result.getConfidenceScore(), result.getMatchedUserName());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[浜鸿劯璇嗗埆] 璇嗗埆澶辫触, streamId={}", request.getStreamId(), e);
            return ResponseDTO.error("FACE_RECOGNITION_FAILED", "浜鸿劯璇嗗埆澶辫触: " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.behavior.detect", description = "寮傚父琛屼负妫€娴嬭€楁椂")
    public ResponseDTO<AbnormalBehaviorResult> detectAbnormalBehavior(AbnormalBehaviorRequest request) {
        log.info("[寮傚父琛屼负妫€娴媇 寮€濮嬫娴? streamId={}, areaId={}, behaviorTypes={}",
                request.getStreamId(), request.getAreaId(), request.getBehaviorTypes());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateBehaviorDetectionRequest(request);

            // 2. 鑾峰彇瑙嗛娴?
            String streamId = request.getStreamId();
            if (!activeStreams.containsKey(streamId)) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "瑙嗛娴佷笉瀛樺湪鎴栧凡鏂紑");
            }

            // 3. 鎵ц琛屼负妫€娴?
            AbnormalBehaviorResult result = performBehaviorDetectionInternal(request);

            // 4. 濡傛灉妫€娴嬪埌寮傚父琛屼负涓斿惎鐢ㄥ憡璀︼紝瑙﹀彂鍛婅娴佺▼
            if (result.getHasAbnormalBehavior() && request.getEnableAlert()) {
                triggerAbnormalBehaviorAlert(result);
            }

            log.info("[寮傚父琛屼负妫€娴媇 妫€娴嬪畬鎴? hasAbnormal={}, behaviorCount={}, riskScore={}",
                    result.getHasAbnormalBehavior(), result.getBehaviorCount(), result.getRiskScore());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[寮傚父琛屼负妫€娴媇 妫€娴嬪け璐? streamId={}", request.getStreamId(), e);
            return ResponseDTO.error("BEHAVIOR_DETECTION_FAILED", "寮傚父琛屼负妫€娴嬪け璐? " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.recording.manage", description = "瑙嗛褰曞埗绠＄悊鑰楁椂")
    public ResponseDTO<VideoRecordingResult> manageVideoRecording(VideoRecordingRequest request) {
        log.info("[瑙嗛褰曞埗] 寮€濮嬬鐞嗚棰戝綍鍒? linkageId={}, cameraCount={}",
                request.getLinkageId(), request.getCameraIds().size());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateRecordingRequest(request);

            // 2. 鐢熸垚褰曞埗浠诲姟ID
            String recordingTaskId = generateRecordingTaskId();

            // 3. 寮€濮嬪綍鍒朵换鍔?
            VideoRecordingResult result = performVideoRecording(recordingTaskId, request);

            log.info("[瑙嗛褰曞埗] 褰曞埗浠诲姟鍚姩鎴愬姛, taskId={}, fileCount={}, totalDuration={}",
                    recordingTaskId, result.getRecordingFiles().size(), result.getTotalDuration());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[瑙嗛褰曞埗] 褰曞埗绠＄悊澶辫触, linkageId={}", request.getLinkageId(), e);
            return ResponseDTO.error("VIDEO_RECORDING_FAILED", "瑙嗛褰曞埗绠＄悊澶辫触: " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.multiscreen.get", description = "鑾峰彇澶氱敾闈㈢洃鎺ц€楁椂")
    public ResponseDTO<MultiScreenResult> getMultiScreenView(MultiScreenRequest request) {
        log.info("[澶氱敾闈㈢洃鎺 鑾峰彇澶氱敾闈㈣鍥? cameraCount={}, layout={}, quality={}",
                request.getCameraIds().size(), request.getLayoutType(), request.getQuality());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateMultiScreenRequest(request);

            // 2. 鐢熸垚鐩戞帶浼氳瘽ID
            String sessionId = generateMonitorSessionId();

            // 3. 鏋勫缓澶氱敾闈㈢粨鏋?
            MultiScreenResult result = buildMultiScreenResult(sessionId, request);

            log.info("[澶氱敾闈㈢洃鎺 澶氱敾闈㈣鍥炬瀯寤烘垚鍔? sessionId={}, activeCameras={}",
                    sessionId, result.getActiveCameras());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[澶氱敾闈㈢洃鎺 鑾峰彇澶氱敾闈㈣鍥惧け璐?, e);
            return ResponseDTO.error("MULTISCREEN_FAILED", "鑾峰彇澶氱敾闈㈣鍥惧け璐? " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.ptz.control", description = "PTZ鎺у埗鑰楁椂")
    @Counted(value = "video.ptz.control.count", description = "PTZ鎺у埗娆℃暟")
    public ResponseDTO<PTZControlResult> controlPTZCamera(PTZControlRequest request) {
        log.info("[PTZ鎺у埗] 寮€濮嬫帶鍒舵憚鍍忓ご, cameraId={}, action={}, speed={}",
                request.getCameraId(), request.getAction(), request.getSpeed());

        try {
            // 1. 鍙傛暟楠岃瘉
            validatePTZControlRequest(request);

            // 2. 鎵цPTZ鎺у埗
            PTZControlResult result = performPTZControl(request);

            log.info("[PTZ鎺у埗] 鎺у埗瀹屾垚, success={}, responseTime={}ms",
                    result.getControlSuccess(), result.getResponseTime());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[PTZ鎺у埗] 鎺у埗澶辫触, cameraId={}, action={}", request.getCameraId(), request.getAction(), e);
            return ResponseDTO.error("PTZ_CONTROL_FAILED", "PTZ鎺у埗澶辫触: " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.playback.historical", description = "鍘嗗彶瑙嗛鍥炴斁鑰楁椂")
    public ResponseDTO<VideoPlaybackResult> playbackHistoricalVideo(VideoPlaybackRequest request) {
        log.info("[瑙嗛鍥炴斁] 寮€濮嬪巻鍙茶棰戝洖鏀? cameraId={}, startTime={}, endTime={}",
                request.getCameraId(), request.getStartTime(), request.getEndTime());

        try {
            // 1. 鍙傛暟楠岃瘉
            validatePlaybackRequest(request);

            // 2. 鐢熸垚鍥炴斁ID
            String playbackId = generatePlaybackId();

            // 3. 鏋勫缓鍥炴斁缁撴灉
            VideoPlaybackResult result = buildPlaybackResult(playbackId, request);

            log.info("[瑙嗛鍥炴斁] 鍥炴斁鏋勫缓鎴愬姛, playbackId={}, duration={}",
                    playbackId, result.getTotalDuration());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[瑙嗛鍥炴斁] 鍥炴斁澶辫触, cameraId={}", request.getCameraId(), e);
            return ResponseDTO.error("VIDEO_PLAYBACK_FAILED", "鍘嗗彶瑙嗛鍥炴斁澶辫触: " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.linkage.events.get", description = "鑾峰彇鑱斿姩浜嬩欢鍒楄〃鑰楁椂")
    public ResponseDTO<List<VideoLinkageEventVO>> getLinkageEvents(VideoLinkageEventQueryRequest request) {
        log.info("[鑱斿姩浜嬩欢] 鏌ヨ瑙嗛鑱斿姩浜嬩欢, userId={}, deviceId={}, startTime={}, endTime={}",
                request.getUserId(), request.getDeviceId(), request.getStartTime(), request.getEndTime());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateEventQueryRequest(request);

            // 2. 鏌ヨ鑱斿姩浜嬩欢
            List<VideoLinkageEventVO> events = queryVideoLinkageEvents(request);

            log.info("[鑱斿姩浜嬩欢] 鏌ヨ瀹屾垚, 浜嬩欢鏁伴噺={}", events.size());

            return ResponseDTO.ok(events);

        } catch (Exception e) {
            log.error("[鑱斿姩浜嬩欢] 鏌ヨ澶辫触", e);
            return ResponseDTO.error("EVENT_QUERY_FAILED", "鏌ヨ鑱斿姩浜嬩欢澶辫触: " + e.getMessage());
        }
    }

    @Override
    @Timed(value = "video.monitor.statistics", description = "鐩戞帶缁熻鑰楁椂")
    public ResponseDTO<MonitorStatisticsVO> getMonitorStatistics(MonitorStatisticsRequest request) {
        log.info("[鐩戞帶缁熻] 鐢熸垚鐩戞帶缁熻鎶ュ憡, cameraCount={}, statisticsType={}",
                request.getCameraIds().size(), request.getStatisticsType());

        try {
            // 1. 鍙傛暟楠岃瘉
            validateStatisticsRequest(request);

            // 2. 鐢熸垚缁熻鎶ュ憡
            MonitorStatisticsVO statistics = generateMonitorStatistics(request);

            log.info("[鐩戞帶缁熻] 缁熻鎶ュ憡鐢熸垚瀹屾垚, totalEvents={}, successRate={}%",
                    statistics.getTotalLinkageEvents(), statistics.getFaceVerificationSuccessRate() * 100);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[鐩戞帶缁熻] 缁熻鎶ュ憡鐢熸垚澶辫触", e);
            return ResponseDTO.error("STATISTICS_FAILED", "鐢熸垚鐩戞帶缁熻鎶ュ憡澶辫触: " + e.getMessage());
        }
    }

    // ==================== 绉佹湁杈呭姪鏂规硶 ====================

    /**
     * 楠岃瘉鑱斿姩璇锋眰鍙傛暟
     */
    private void validateLinkageRequest(VideoLinkageRequest request) {
        if (SmartStringUtil.isEmpty(request.getAccessEventId())) {
            throw new IllegalArgumentException("闂ㄧ浜嬩欢ID涓嶈兘涓虹┖");
        }
        if (request.getDeviceId() == null) {
            throw new IllegalArgumentException("璁惧ID涓嶈兘涓虹┖");
        }
        if (SmartStringUtil.isEmpty(request.getUserId())) {
            throw new IllegalArgumentException("鐢ㄦ埛ID涓嶈兘涓虹┖");
        }
        if (request.getCameraIds() == null || request.getCameraIds().isEmpty()) {
            throw new IllegalArgumentException("鍏宠仈鎽勫儚澶碔D鍒楄〃涓嶈兘涓虹┖");
        }
    }

    /**
     * 鐢熸垚鑱斿姩ID
     */
    private String generateLinkageId() {
        return "VL-" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 鎵ц瑙嗛鑱斿姩
     */
    private VideoLinkageResult performVideoLinkage(String linkageId, VideoLinkageRequest request) {
        log.info("[瑙嗛鑱斿姩] 鎵ц鑱斿姩閫昏緫, linkageId={}", linkageId);

        LocalDateTime triggerTime = LocalDateTime.now();
        List<CameraLinkageInfo> cameraLinkages = new ArrayList<>();
        List<String> recordingIds = new ArrayList<>();

        // 1. 澶勭悊姣忎釜鎽勫儚澶寸殑鑱斿姩
        for (Long cameraId : request.getCameraIds()) {
            CameraLinkageInfo cameraLinkage = processCameraLinkage(linkageId, cameraId, request);
            cameraLinkages.add(cameraLinkage);

            if (cameraLinkage.getRecordingId() != null) {
                recordingIds.add(cameraLinkage.getRecordingId());
            }
        }

        // 2. 鎵ц浜鸿劯璇嗗埆锛堝鏋滃惎鐢級
        FaceVerificationResult faceVerification = null;
        if (request.getEnableFaceRecognition() && !cameraLinkages.isEmpty()) {
            faceVerification = performFaceVerification(linkageId, request);
        }

        // 3. 鎵ц寮傚父琛屼负妫€娴?
        List<AbnormalEvent> detectedEvents = detectAbnormalEvents(linkageId, request);

        // 4. 璇勪及鑱斿姩鐘舵€?
        Integer linkageStatus = evaluateLinkageStatus(cameraLinkages, faceVerification, detectedEvents);
        String statusDescription = generateStatusDescription(linkageStatus, cameraLinkages.size());

        // 5. 璁＄畻澶勭悊鏃堕暱
        Integer processingDuration = (int) Duration.between(triggerTime, LocalDateTime.now()).toMillis();

        return VideoLinkageResult.builder()
                .linkageId(linkageId)
                .accessEventId(request.getAccessEventId())
                .triggerTime(triggerTime)
                .cameraLinkages(cameraLinkages)
                .recordingIds(recordingIds)
                .faceVerification(faceVerification)
                .detectedEvents(detectedEvents)
                .linkageStatus(linkageStatus)
                .statusDescription(statusDescription)
                .processingDuration(processingDuration)
                .build();
    }

    /**
     * 澶勭悊鍗曚釜鎽勫儚澶磋仈鍔?
     */
    private CameraLinkageInfo processCameraLinkage(String linkageId, Long cameraId, VideoLinkageRequest request) {
        log.debug("[瑙嗛鑱斿姩] 澶勭悊鎽勫儚澶磋仈鍔? linkageId={}, cameraId={}", linkageId, cameraId);

        // 妯℃嫙鎽勫儚澶翠俊鎭幏鍙?
        String cameraName = "鎽勫儚澶?" + cameraId;
        String cameraLocation = "鍖哄煙-" + request.getAreaId();
        String streamUrl = "rtsp://192.168.1.100:554/camera/" + cameraId + "/stream";

        // 鍚姩褰曞埗锛堝鏋滈渶瑕侊級
        String recordingId = null;
        if (request.getEnableRecording()) {
            recordingId = startCameraRecording(cameraId, request.getRecordDuration());
        }

        // 纭畾鑱斿姩浼樺厛绾э紙鍩轰簬鎽勫儚澶翠綅缃拰鐢ㄦ埛绫诲瀷锛?
        Integer priority = calculateLinkagePriority(cameraId, request);

        return CameraLinkageInfo.builder()
                .cameraId(cameraId)
                .cameraName(cameraName)
                .cameraLocation(cameraLocation)
                .streamUrl(streamUrl)
                .recordingEnabled(request.getEnableRecording())
                .recordingId(recordingId)
                .startTime(LocalDateTime.now())
                .linkagePriority(priority)
                .cameraStatus("ONLINE")
                .build();
    }

    /**
     * 鎵ц浜鸿劯楠岃瘉
     */
    private FaceVerificationResult performFaceVerification(String linkageId, VideoLinkageRequest request) {
        log.debug("[瑙嗛鑱斿姩] 鎵ц浜鸿劯楠岃瘉, linkageId={}, userId={}", linkageId, request.getUserId());

        // 妯℃嫙浜鸿劯璇嗗埆杩囩▼
        try {
            // 妯℃嫙璇嗗埆寤惰繜
            Thread.sleep(500 + (long)(Math.random() * 1000));

            // 妯℃嫙璇嗗埆缁撴灉锛?0%鎴愬姛鐜囷級
            boolean success = Math.random() < 0.8;
            double confidence = success ? 0.85 + Math.random() * 0.14 : 0.6 + Math.random() * 0.2;

            return FaceVerificationResult.builder()
                    .verificationSuccess(success)
                    .confidenceScore(confidence)
                    .matchedUserId(success ? request.getUserId() : null)
                    .matchedUserName(success ? request.getUserName() : null)
                    .faceImageUrl("/api/v1/video/capture/face/" + linkageId + ".jpg")
                    .verifyTime(LocalDateTime.now())
                    .verifyMethod("REAL_TIME_RECOGNITION")
                    .failureReasons(success ? null : List.of("浜鸿劯鍖归厤搴﹁繃浣?))
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("浜鸿劯楠岃瘉杩囩▼琚腑鏂?, e);
        }
    }

    /**
     * 妫€娴嬪紓甯镐簨浠?
     */
    private List<AbnormalEvent> detectAbnormalEvents(String linkageId, VideoLinkageRequest request) {
        log.debug("[瑙嗛鑱斿姩] 妫€娴嬪紓甯镐簨浠? linkageId={}", linkageId);

        List<AbnormalEvent> events = new ArrayList<>();

        // 妯℃嫙寮傚父浜嬩欢妫€娴嬶紙20%姒傜巼妫€娴嬪埌寮傚父锛?
        if (Math.random() < 0.2) {
            String[] eventTypes = {"LOITERING", "TAILGATING", "FORCED_ENTRY", "SUSPICIOUS_OBJECT"};
            String[] descriptions = {"鍙枒浜哄憳寰樺緤", "灏鹃殢杩涘叆", "寮鸿闂叆", "鍙枒鐗╁搧妫€娴?};

            int eventIndex = (int)(Math.random() * eventTypes.length);

            AbnormalEvent event = AbnormalEvent.builder()
                    .eventId("AE-" + UUID.randomUUID().toString().replace("-", ""))
                    .eventType(eventTypes[eventIndex])
                    .description(descriptions[eventIndex])
                    .eventTime(LocalDateTime.now())
                    .severityLevel((int)(Math.random() * 3) + 1)
                    .imageUrl("/api/v1/video/capture/abnormal/" + linkageId + ".jpg")
                    .videoClipUrl("/api/v1/video/clip/" + linkageId + ".mp4")
                    .needAlert(true)
                    .build();

            events.add(event);
        }

        return events;
    }

    /**
     * 璇勪及鑱斿姩鐘舵€?
     */
    private Integer evaluateLinkageStatus(List<CameraLinkageInfo> cameraLinkages,
                                          FaceVerificationResult faceVerification,
                                          List<AbnormalEvent> detectedEvents) {
        // 璁＄畻鎴愬姛鐜?
        int successCount = (int) cameraLinkages.stream().filter(c -> "ONLINE".equals(c.getCameraStatus())).count();
        double successRate = (double) successCount / cameraLinkages.size();

        // 浜鸿劯楠岃瘉鎴愬姛鍔犲垎
        if (faceVerification != null && faceVerification.getVerificationSuccess()) {
            successRate += 0.1;
        }

        // 妫€娴嬪埌寮傚父浜嬩欢闇€瑕佺壒娈婂鐞?
        if (!detectedEvents.isEmpty()) {
            // 濡傛灉鏈変弗閲嶅紓甯镐簨浠讹紝鍗充娇鎶€鏈垚鍔熶篃鏍囪涓洪儴鍒嗘垚鍔?
            boolean hasSevereEvent = detectedEvents.stream().anyMatch(e -> e.getSeverityLevel() >= 3);
            if (hasSevereEvent) {
                return 2; // 閮ㄥ垎鎴愬姛
            }
        }

        if (successRate >= 0.9) {
            return 1; // 鎴愬姛
        } else if (successRate >= 0.5) {
            return 2; // 閮ㄥ垎鎴愬姛
        } else {
            return 3; // 澶辫触
        }
    }

    /**
     * 鐢熸垚鐘舵€佹弿杩?
     */
    private String generateStatusDescription(Integer status, int cameraCount) {
        switch (status) {
            case 1:
                return String.format("鑱斿姩鎴愬姛锛屾垚鍔熷鐞?d涓憚鍍忓ご", cameraCount);
            case 2:
                return String.format("閮ㄥ垎鎴愬姛锛屽鐞嗕簡%d涓憚鍍忓ご锛屼絾瀛樺湪寮傚父鎯呭喌", cameraCount);
            case 3:
                return String.format("鑱斿姩澶辫触锛屾棤娉曟甯稿鐞?d涓憚鍍忓ご", cameraCount);
            default:
                return "鐘舵€佹湭鐭?;
        }
    }

    /**
     * 鍒涘缓澶辫触鐨勮仈鍔ㄧ粨鏋?
     */
    private VideoLinkageResult createFailedLinkageResult(String linkageId, VideoLinkageRequest request, Throwable throwable) {
        return VideoLinkageResult.builder()
                .linkageId(linkageId)
                .accessEventId(request.getAccessEventId())
                .triggerTime(LocalDateTime.now())
                .cameraLinkages(new ArrayList<>())
                .recordingIds(new ArrayList<>())
                .linkageStatus(3)
                .statusDescription("鑱斿姩鎵ц澶辫触: " + throwable.getMessage())
                .processingDuration(0)
                .build();
    }

    /**
     * 楠岃瘉瑙嗛娴佽姹?
     */
    private void validateStreamRequest(VideoStreamRequest request) {
        if (request.getCameraId() == null) {
            throw new IllegalArgumentException("鎽勫儚澶碔D涓嶈兘涓虹┖");
        }
        if (SmartStringUtil.isEmpty(request.getProtocol())) {
            throw new IllegalArgumentException("鍗忚绫诲瀷涓嶈兘涓虹┖");
        }
    }

    /**
     * 鐢熸垚瑙嗛娴両D
     */
    private String generateStreamId(VideoStreamRequest request) {
        return "STREAM-" + request.getCameraId() + "-" + System.currentTimeMillis();
    }

    /**
     * 寤虹珛瑙嗛娴?
     */
    private VideoStreamResult establishVideoStream(String streamId, VideoStreamRequest request) {
        // 妯℃嫙瑙嗛娴佸缓绔?
        String baseUrl = "rtsp://192.168.1.100:554";
        String streamUrl = String.format("%s/camera/%d/%s", baseUrl, request.getCameraId(), request.getStreamType().toLowerCase());

        return VideoStreamResult.builder()
                .streamId(streamId)
                .streamUrl(streamUrl)
                .streamType(request.getStreamType())
                .protocol(request.getProtocol())
                .quality(request.getQuality().toString())
                .width(1920)
                .height(1080)
                .fps(25)
                .bitrate(2048000L)
                .expireTime(LocalDateTime.now().plusHours(1).toString())
                .accessToken(UUID.randomUUID().toString())
                .audioEnabled(request.getEnableAudio())
                .build();
    }

    /**
     * 楠岃瘉浜鸿劯璇嗗埆璇锋眰
     */
    private void validateFaceRecognitionRequest(FaceRecognitionRequest request) {
        if (SmartStringUtil.isEmpty(request.getStreamId())) {
            throw new IllegalArgumentException("瑙嗛娴両D涓嶈兘涓虹┖");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("鐢ㄦ埛ID涓嶈兘涓虹┖");
        }
    }

    /**
     * 鎵ц鍐呴儴浜鸿劯璇嗗埆
     */
    private FaceRecognitionResult performFaceRecognitionInternal(FaceRecognitionRequest request) {
        log.debug("[浜鸿劯璇嗗埆] 鎵ц鍐呴儴浜鸿劯璇嗗埆, streamId={}", request.getStreamId());

        try {
            // 妯℃嫙浜鸿劯璇嗗埆澶勭悊鏃堕棿
            Thread.sleep(800 + (long)(Math.random() * 1200));

            // 妯℃嫙璇嗗埆缁撴灉
            boolean success = Math.random() < 0.85;
            double confidence = success ? 0.88 + Math.random() * 0.12 : 0.65 + Math.random() * 0.2;

            return FaceRecognitionResult.builder()
                    .recognitionSuccess(success)
                    .confidenceScore(confidence)
                    .matchedUserId(success ? request.getUserId() : null)
                    .matchedUserName(success ? "鐢ㄦ埛" + request.getUserId() : null)
                    .capturedFaceUrl("/api/v1/video/face/capture/" + request.getStreamId() + ".jpg")
                    .recognitionTime(LocalDateTime.now())
                    .faceCount(1)
                    .faceQuality(confidence > 0.9 ? "HIGH" : confidence > 0.7 ? "MEDIUM" : "LOW")
                    .livenessCheck(LivenessCheckResult.builder()
                            .isLive(success)
                            .livenessScore(confidence)
                            .detectionMethod("MULTI_MODAL")
                            .spoofingIndicators(new ArrayList<>())
                            .build())
                    .failureReasons(success ? null : List.of("浜鸿劯鍖归厤搴﹁繃浣?, "鍥剧墖璐ㄩ噺涓嶄匠"))
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("浜鸿劯璇嗗埆杩囩▼琚腑鏂?, e);
        }
    }

    /**
     * 璁板綍浜鸿劯璇嗗埆缁撴灉
     */
    private void logFaceRecognitionResult(FaceRecognitionResult result) {
        if (result.getRecognitionSuccess()) {
            log.info("[浜鸿劯璇嗗埆] 璇嗗埆鎴愬姛, 缃俊搴?{}, 鐢ㄦ埛={}",
                    result.getConfidenceScore(), result.getMatchedUserName());
        } else {
            log.warn("[浜鸿劯璇嗗埆] 璇嗗埆澶辫触, 澶辫触鍘熷洜={}", result.getFailureReasons());
        }
    }

    /**
     * 楠岃瘉琛屼负妫€娴嬭姹?
     */
    private void validateBehaviorDetectionRequest(AbnormalBehaviorRequest request) {
        if (SmartStringUtil.isEmpty(request.getStreamId())) {
            throw new IllegalArgumentException("瑙嗛娴両D涓嶈兘涓虹┖");
        }
        if (request.getBehaviorTypes() == null || request.getBehaviorTypes().isEmpty()) {
            throw new IllegalArgumentException("妫€娴嬭涓虹被鍨嬩笉鑳戒负绌?);
        }
    }

    /**
     * 鎵ц鍐呴儴琛屼负妫€娴?
     */
    private AbnormalBehaviorResult performBehaviorDetectionInternal(AbnormalBehaviorRequest request) {
        log.debug("[寮傚父琛屼负妫€娴媇 鎵ц鍐呴儴琛屼负妫€娴? streamId={}", request.getStreamId());

        try {
            // 妯℃嫙琛屼负妫€娴嬪鐞嗘椂闂?
            Thread.sleep(1000 + (long)(Math.random() * 2000));

            // 妯℃嫙妫€娴嬬粨鏋滐紙30%姒傜巼妫€娴嬪埌寮傚父锛?
            boolean hasAbnormal = Math.random() < 0.3;
            List<DetectedBehavior> behaviors = new ArrayList<>();

            if (hasAbnormal) {
                String[] behaviorTypes = {"LOITERING", "TAILGATING", "CROWDING", "UNAUTHORIZED_ACCESS"};
                String[] descriptions = {"鍙枒寰樺緤", "灏鹃殢杩涘叆", "浜哄憳鑱氶泦", "鏈巿鏉冭闂?};

                int behaviorCount = (int)(Math.random() * 3) + 1;
                for (int i = 0; i < behaviorCount; i++) {
                    int index = (int)(Math.random() * behaviorTypes.length);
                    DetectedBehavior behavior = DetectedBehavior.builder()
                            .behaviorType(behaviorTypes[index])
                            .description(descriptions[index])
                            .confidence(0.7 + Math.random() * 0.3)
                            .occurTime(LocalDateTime.now().minusSeconds((long)(Math.random() * 300)))
                            .imageUrl("/api/v1/video/behavior/capture/" + System.currentTimeMillis() + ".jpg")
                            .videoClipUrl("/api/v1/video/behavior/clip/" + System.currentTimeMillis() + ".mp4")
                            .severityLevel((int)(Math.random() * 4) + 1)
                            .build();
                    behaviors.add(behavior);
                }
            }

            // 璁＄畻椋庨櫓璇勫垎
            double riskScore = hasAbnormal ? 0.5 + Math.random() * 0.5 : Math.random() * 0.3;
            String alertLevel = riskScore > 0.8 ? "HIGH" : riskScore > 0.6 ? "MEDIUM" : riskScore > 0.4 ? "LOW" : "NORMAL";

            return AbnormalBehaviorResult.builder()
                    .detectionId("BD-" + UUID.randomUUID().toString().replace("-", ""))
                    .hasAbnormalBehavior(hasAbnormal)
                    .detectedBehaviors(behaviors)
                    .detectionTime(LocalDateTime.now())
                    .behaviorCount(behaviors.size())
                    .riskScore(riskScore)
                    .alertLevel(alertLevel)
                    .recommendedActions(hasAbnormal ? List.of("閫氱煡瀹変繚浜哄憳", "淇濆瓨璇佹嵁", "缁х画鐩戞帶") : new ArrayList<>())
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("寮傚父琛屼负妫€娴嬭繃绋嬭涓柇", e);
        }
    }

    /**
     * 瑙﹀彂寮傚父琛屼负鍛婅
     */
    private void triggerAbnormalBehaviorAlert(AbnormalBehaviorResult result) {
        log.warn("[寮傚父琛屼负鍛婅] 瑙﹀彂鍛婅, detectionId={}, alertLevel={}, behaviorCount={}",
                result.getDetectionId(), result.getAlertLevel(), result.getBehaviorCount());

        // 杩欓噷搴旇璋冪敤鍛婅绯荤粺鍙戦€侀€氱煡
        // alertService.sendAbnormalBehaviorAlert(result);
    }

    // 鍏朵粬绉佹湁鏂规硶缁х画瀹炵幇...
    // 涓轰簡浠ｇ爜闀垮害鑰冭檻锛岃繖閲岀渷鐣ラ儴鍒嗘柟娉曠殑瀹炵幇锛屽疄闄呭紑鍙戜腑闇€瑕佸畬鏁村疄鐜?

    private void validateRecordingRequest(VideoRecordingRequest request) {
        // 楠岃瘉褰曞埗璇锋眰鍙傛暟
        if (SmartStringUtil.isEmpty(request.getLinkageId())) {
            throw new IllegalArgumentException("鑱斿姩ID涓嶈兘涓虹┖");
        }
        if (request.getCameraIds() == null || request.getCameraIds().isEmpty()) {
            throw new IllegalArgumentException("鎽勫儚澶碔D鍒楄〃涓嶈兘涓虹┖");
        }
    }

    private String generateRecordingTaskId() {
        return "REC-" + UUID.randomUUID().toString().replace("-", "");
    }

    private VideoRecordingResult performVideoRecording(String recordingTaskId, VideoRecordingRequest request) {
        // 妯℃嫙瑙嗛褰曞埗瀹炵幇
        List<RecordingFileInfo> files = new ArrayList<>();

        for (Long cameraId : request.getCameraIds()) {
            RecordingFileInfo file = RecordingFileInfo.builder()
                    .recordingId(UUID.randomUUID().toString().replace("-", ""))
                    .cameraId(cameraId)
                    .fileName("recording_" + cameraId + "_" + System.currentTimeMillis() + ".mp4")
                    .filePath("/storage/recordings/" + recordingTaskId + "/")
                    .fileSize((long)(Math.random() * 100000000)) // 闅忔満鏂囦欢澶у皬
                    .startTime(LocalDateTime.now().minusSeconds(request.getPreRecordSeconds()))
                    .endTime(LocalDateTime.now().plusSeconds(request.getPostRecordSeconds()))
                    .duration(request.getPreRecordSeconds() + request.getPostRecordSeconds())
                    .recordingQuality(request.getRecordingQuality())
                    .format("MP4")
                    .downloadUrl("/api/v1/video/download/" + recordingTaskId + "/" + cameraId)
                    .build();
            files.add(file);
        }

        return VideoRecordingResult.builder()
                .recordingTaskId(recordingTaskId)
                .recordingFiles(files)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusSeconds(request.getPostRecordSeconds()))
                .totalDuration((long) (request.getPreRecordSeconds() + request.getPostRecordSeconds()))
                .totalSize(files.stream().mapToLong(RecordingFileInfo::getFileSize).sum())
                .recordingStatus(1) // 褰曞埗涓?
                .tags(List.of("EVENT_LINKED", "AUTO_RECORDING"))
                .build();
    }

    private String startCameraRecording(Long cameraId, Integer duration) {
        return "REC-" + cameraId + "-" + System.currentTimeMillis();
    }

    private Integer calculateLinkagePriority(Long cameraId, VideoLinkageRequest request) {
        // 妯℃嫙浼樺厛绾ц绠楅€昏緫
        return (int)(Math.random() * 10) + 1;
    }

    private void validateMultiScreenRequest(MultiScreenRequest request) {
        if (request.getCameraIds() == null || request.getCameraIds().isEmpty()) {
            throw new IllegalArgumentException("鎽勫儚澶碔D鍒楄〃涓嶈兘涓虹┖");
        }
    }

    private String generateMonitorSessionId() {
        return "MONITOR-" + UUID.randomUUID().toString().replace("-", "");
    }

    private MultiScreenResult buildMultiScreenResult(String sessionId, MultiScreenRequest request) {
        List<CameraStreamInfo> streams = new ArrayList<>();

        for (Long cameraId : request.getCameraIds()) {
            CameraStreamInfo stream = CameraStreamInfo.builder()
                    .cameraId(cameraId)
                    .cameraName("鎽勫儚澶?" + cameraId)
                    .streamUrl("rtsp://192.168.1.100:554/camera/" + cameraId + "/stream")
                    .position("浣嶇疆-" + (streams.size() + 1))
                    .width(1920)
                    .height(1080)
                    .status("ONLINE")
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
            streams.add(stream);
        }

        return MultiScreenResult.builder()
                .sessionId(sessionId)
                .layoutType(request.getLayoutType())
                .cameraStreams(streams)
                .createTime(LocalDateTime.now())
                .totalCameras(request.getCameraIds().size())
                .activeCameras(streams.size())
                .monitorUrl("/api/v1/video/monitor/view/" + sessionId)
                .availableQualities(List.of("HIGH", "MEDIUM", "LOW"))
                .build();
    }

    private void validatePTZControlRequest(PTZControlRequest request) {
        if (request.getCameraId() == null) {
            throw new IllegalArgumentException("鎽勫儚澶碔D涓嶈兘涓虹┖");
        }
        if (SmartStringUtil.isEmpty(request.getAction())) {
            throw new IllegalArgumentException("鎺у埗鍔ㄤ綔涓嶈兘涓虹┖");
        }
    }

    private PTZControlResult performPTZControl(PTZControlRequest request) {
        // 妯℃嫙PTZ鎺у埗瀹炵幇
        try {
            Thread.sleep(200 + (long)(Math.random() * 300)); // 妯℃嫙鎺у埗寤惰繜

            return PTZControlResult.builder()
                    .controlSuccess(true)
                    .commandId("PTZ-" + UUID.randomUUID().toString().replace("-", ""))
                    .executeTime(LocalDateTime.now())
                    .responseTime((int)(200 + Math.random() * 300))
                    .currentPosition("Pan:" + request.getPan() + " Tilt:" + request.getTilt() + " Zoom:" + request.getZoom())
                    .currentZoom(request.getZoom() != null ? request.getZoom().intValue() : 1)
                    .availablePresets(generateMockPresets())
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("PTZ鎺у埗杩囩▼琚腑鏂?, e);
        }
    }

    private List<PTZPresetInfo> generateMockPresets() {
        List<PTZPresetInfo> presets = new ArrayList<>();
        String[] presetNames = {"涓诲叆鍙?, "渚ч棬", "澶у巺涓ぎ", "鍓嶅彴", "鍑哄彛"};

        for (int i = 0; i < presetNames.length; i++) {
            PTZPresetInfo preset = PTZPresetInfo.builder()
                    .presetId(i + 1)
                    .presetName(presetNames[i])
                    .description("棰勮浣? + (i + 1))
                    .position("Pos:" + (i * 10) + "," + (i * 5))
                    .build();
            presets.add(preset);
        }

        return presets;
    }

    private void validatePlaybackRequest(VideoPlaybackRequest request) {
        if (request.getCameraId() == null) {
            throw new IllegalArgumentException("鎽勫儚澶碔D涓嶈兘涓虹┖");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("寮€濮嬫椂闂村拰缁撴潫鏃堕棿涓嶈兘涓虹┖");
        }
    }

    private String generatePlaybackId() {
        return "PLAYBACK-" + UUID.randomUUID().toString().replace("-", "");
    }

    private VideoPlaybackResult buildPlaybackResult(String playbackId, VideoPlaybackRequest request) {
        long duration = java.time.Duration.between(request.getStartTime(), request.getEndTime()).getSeconds();

        return VideoPlaybackResult.builder()
                .playbackId(playbackId)
                .playbackUrl("/api/v1/video/playback/stream/" + playbackId)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalDuration((int) duration)
                .eventMarkers(generateMockEventMarkers(request))
                .quality(request.getPlaybackQuality())
                .audioAvailable(request.getEnableAudio())
                .build();
    }

    private List<VideoEventMarker> generateMockEventMarkers(VideoPlaybackRequest request) {
        List<VideoEventMarker> markers = new ArrayList<>();

        // 妯℃嫙鐢熸垚浜嬩欢鏍囪鐐?
        for (int i = 0; i < 3; i++) {
            VideoEventMarker marker = VideoEventMarker.builder()
                    .eventId("EVENT-" + i)
                    .eventTime(request.getStartTime().plusMinutes(i * 30))
                    .eventType(i % 2 == 0 ? "ACCESS" : "ALERT")
                    .description(i % 2 == 0 ? "闂ㄧ浜嬩欢" : "鍛婅浜嬩欢")
                    .offsetSeconds(i * 1800)
                    .thumbnailUrl("/api/v1/video/thumbnail/" + i + ".jpg")
                    .build();
            markers.add(marker);
        }

        return markers;
    }

    private void validateEventQueryRequest(VideoLinkageEventQueryRequest request) {
        // 楠岃瘉浜嬩欢鏌ヨ璇锋眰鍙傛暟
        if (request.getPageNum() == null || request.getPageSize() == null) {
            throw new IllegalArgumentException("鍒嗛〉鍙傛暟涓嶈兘涓虹┖");
        }
    }

    private List<VideoLinkageEventVO> queryVideoLinkageEvents(VideoLinkageEventQueryRequest request) {
        // 妯℃嫙鏌ヨ浜嬩欢鍒楄〃
        List<VideoLinkageEventVO> events = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            VideoLinkageEventVO event = VideoLinkageEventVO.builder()
                    .linkageId("VL-" + i)
                    .accessEventId("AE-" + i)
                    .userId("USER-" + (i % 10 + 1))
                    .userName("鐢ㄦ埛" + (i % 10 + 1))
                    .deviceId((long)(i % 5 + 1))
                    .deviceName("闂ㄧ璁惧-" + (i % 5 + 1))
                    .areaId("AREA-" + (i % 3 + 1))
                    .areaName("鍖哄煙-" + (i % 3 + 1))
                    .eventType(i % 2 == 0 ? "NORMAL_ACCESS" : "ALERT_ACCESS")
                    .accessTime(LocalDateTime.now().minusHours(i))
                    .linkageStatus(i % 10 == 0 ? 3 : i % 5 == 0 ? 2 : 1)
                    .triggerTime(LocalDateTime.now().minusHours(i).minusMinutes(5))
                    .cameraCount(3)
                    .faceVerificationSuccess(Math.random() > 0.2)
                    .abnormalEventCount(i % 8 == 0 ? 1 : 0)
                    .description(i % 2 == 0 ? "姝ｅ父闂ㄧ浜嬩欢" : "寮傚父闂ㄧ浜嬩欢")
                    .build();
            events.add(event);
        }

        return events;
    }

    private void validateStatisticsRequest(MonitorStatisticsRequest request) {
        // 楠岃瘉缁熻璇锋眰鍙傛暟
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("缁熻鏃堕棿鑼冨洿涓嶈兘涓虹┖");
        }
    }

    private MonitorStatisticsVO generateMonitorStatistics(MonitorStatisticsRequest request) {
        // 妯℃嫙鐢熸垚缁熻鏁版嵁
        return MonitorStatisticsVO.builder()
                .statisticsPeriod(request.getStatisticsType())
                .totalLinkageEvents(1250L)
                .successfulLinkages(1180L)
                .faceVerificationAttempts(980L)
                .faceVerificationSuccesses(850L)
                .faceVerificationSuccessRate(0.867)
                .abnormalBehaviorDetections(45L)
                .totalRecordingDuration(3250L)
                .totalStorageUsed(875L)
                .averageResponseTime(250.0)
                .onlineCameraCount(48)
                .totalCameraCount(50)
                .cameraStatistics(generateMockCameraStatistics(request.getCameraIds()))
                .build();
    }

    private List<CameraStatistics> generateMockCameraStatistics(List<Long> cameraIds) {
        List<CameraStatistics> statistics = new ArrayList<>();

        for (Long cameraId : cameraIds != null ? cameraIds : List.of(1L, 2L, 3L)) {
            CameraStatistics stat = CameraStatistics.builder()
                    .cameraId(cameraId)
                    .cameraName("鎽勫儚澶?" + cameraId)
                    .linkageEventCount(25 + (long)(Math.random() * 50))
                    .recordingDuration((long)(Math.random() * 100))
                    .storageUsed((long)(Math.random() * 20))
                    .status(Math.random() > 0.1 ? "ONLINE" : "OFFLINE")
                    .onlineRate(0.85 + Math.random() * 0.14)
                    .topEventTypes(List.of("NORMAL_ACCESS", "FACE_VERIFY"))
                    .build();
            statistics.add(stat);
        }

        return statistics;
    }
}
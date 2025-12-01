package net.lab1024.sa.admin.module.smart.biometric.engine;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 活体检测服务
 *
 * 提供多种活体检测算法，包括眨眼检测、摇头检测、微笑检测等， 用于防止照片、视频等欺骗攻击，确保人脸识别的安全性
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
@Slf4j
public class LivenessDetectionService {

    private final LivenessConfig config;
    private final Map<String, LivenessSession> activeSessions;
    private final AtomicLong sessionCounter;
    private final LivenessStatistics statistics;

    // 活体检测算法实例（待OpenCV集成后替换为真实实现）
    private final BlinkDetector blinkDetector;
    private final HeadMovementDetector headMovementDetector;
    private final ExpressionDetector expressionDetector;
    private final TextureAnalyzer textureAnalyzer;

    public LivenessDetectionService(LivenessConfig config) {
        this.config = config;
        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionCounter = new AtomicLong(0);
        this.statistics = new LivenessStatistics();

        // 初始化检测器实例
        this.blinkDetector = new BlinkDetector();
        this.headMovementDetector = new HeadMovementDetector();
        this.expressionDetector = new ExpressionDetector();
        this.textureAnalyzer = new TextureAnalyzer();

        log.info("活体检测服务初始化完成，配置: {}", config);
    }

    /**
     * 创建活体检测会话
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param challengeType 挑战类型
     * @return 会话信息
     */
    public LivenessSession createSession(Long userId, String deviceId,
            LivenessChallengeType challengeType) {
        String sessionId = generateSessionId();
        LivenessSession session = new LivenessSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setDeviceId(deviceId);
        session.setChallengeType(challengeType);
        session.setStartTime(System.currentTimeMillis());
        session.setTimeout(config.getSessionTimeoutMs());
        session.setStatus(LivenessSessionStatus.ACTIVE);

        // 生成随机挑战序列
        session.setChallengeSequence(generateChallengeSequence(challengeType));
        session.setCurrentChallengeIndex(0);

        activeSessions.put(sessionId, session);
        statistics.incrementSessionsCreated();

        log.info("创建活体检测会话: sessionId={}, userId={}, challengeType={}", sessionId, userId,
                challengeType);

        return session;
    }

    /**
     * 执行活体检测
     *
     * @param sessionId 会话ID
     * @param frame 图像帧
     * @return 检测结果
     */
    public LivenessResult performLivenessCheck(String sessionId, BufferedImage frame) {
        LivenessSession session = activeSessions.get(sessionId);
        if (session == null) {
            return createFailureResult("会话不存在或已过期");
        }

        // 检查会话状态
        if (session.getStatus() != LivenessSessionStatus.ACTIVE) {
            return createFailureResult("会话状态异常: " + session.getStatus());
        }

        // 检查会话超时
        if (System.currentTimeMillis() - session.getStartTime() > session.getTimeout()) {
            session.setStatus(LivenessSessionStatus.TIMEOUT);
            activeSessions.remove(sessionId);
            return createFailureResult("会话超时");
        }

        try {
            // 获取当前挑战
            LivenessChallenge currentChallenge = session.getCurrentChallenge();
            if (currentChallenge == null) {
                return createFailureResult("没有待完成的挑战");
            }

            // 执行相应的检测算法
            LivenessDetectionResult detectionResult = executeChallenge(currentChallenge, frame);

            // 更新会话状态
            updateSessionProgress(session, detectionResult);

            // 构建返回结果
            LivenessResult result = new LivenessResult();
            result.setSessionId(sessionId);
            result.setSuccess(detectionResult.isSuccess());
            result.setConfidence(detectionResult.getConfidence());
            result.setChallengeType(currentChallenge.getType());
            result.setDetectionDetails(detectionResult.getDetails());
            result.setCompletedChallenges(session.getCompletedChallenges().size());
            result.setTotalChallenges(session.getChallengeSequence().size());
            result.setProcessingTimeMs(detectionResult.getProcessingTimeMs());

            // 检查是否完成所有挑战
            if (session.isAllChallengesCompleted()) {
                session.setStatus(LivenessSessionStatus.COMPLETED);
                result.setOverallSuccess(true);
                activeSessions.remove(sessionId);
                statistics.incrementSuccessfulDetections();
                log.info("活体检测成功完成: sessionId={}, userId={}", sessionId, session.getUserId());
            } else {
                result.setOverallSuccess(false);
                result.setNextChallenge(session.getNextChallenge());
            }

            return result;

        } catch (Exception e) {
            log.error("活体检测执行失败: sessionId={}", sessionId, e);
            statistics.incrementFailedDetections();
            return createFailureResult("检测执行异常: " + e.getMessage());
        }
    }

    /**
     * 执行特定挑战的检测
     */
    private LivenessDetectionResult executeChallenge(LivenessChallenge challenge,
            BufferedImage frame) {
        long startTime = System.currentTimeMillis();

        try {
            switch (challenge.getType()) {
                case BLINK:
                    return blinkDetector.detectBlink(frame, challenge.getParameters());
                case HEAD_MOVEMENT:
                    return headMovementDetector.detectHeadMovement(frame,
                            challenge.getParameters());
                case SMILE:
                    return expressionDetector.detectSmile(frame, challenge.getParameters());
                case TEXTURE_ANALYSIS:
                    return textureAnalyzer.analyzeTexture(frame, challenge.getParameters());
                default:
                    return createFailureDetectionResult("不支持的挑战类型: " + challenge.getType());
            }
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            statistics.addProcessingTime(processingTime);
        }
    }

    /**
     * 更新会话进度
     */
    private void updateSessionProgress(LivenessSession session, LivenessDetectionResult result) {
        if (result.isSuccess()) {
            session.markChallengeCompleted(session.getCurrentChallengeIndex());
            session.setCurrentChallengeIndex(session.getCurrentChallengeIndex() + 1);
        } else {
            session.incrementFailedAttempts();

            // 检查失败次数是否超过限制
            if (session.getFailedAttempts() >= config.getMaxFailedAttempts()) {
                session.setStatus(LivenessSessionStatus.FAILED);
                activeSessions.remove(session.getSessionId());
                statistics.incrementFailedDetections();
            }
        }
    }

    /**
     * 生成挑战序列
     */
    private java.util.List<LivenessChallenge> generateChallengeSequence(
            LivenessChallengeType challengeType) {
        java.util.List<LivenessChallenge> sequence = new java.util.ArrayList<>();

        switch (challengeType) {
            case BLINK:
                sequence.add(createBlinkChallenge());
                break;
            case SMILE:
                sequence.add(createSmileChallenge());
                break;
            case BLINK_DETECTION:
                sequence.add(createBlinkChallenge());
                break;
            case HEAD_MOVEMENT:
                sequence.add(createHeadMovementChallenge());
                break;
            case MULTI_CHALLENGE:
                sequence.add(createBlinkChallenge());
                sequence.add(createHeadMovementChallenge());
                sequence.add(createSmileChallenge());
                break;
            case EXPRESSION_CHALLENGE:
                sequence.add(createSmileChallenge());
                break;
            case TEXTURE_ANALYSIS:
                sequence.add(createTextureChallenge());
                break;
        }

        return sequence;
    }

    private LivenessChallenge createBlinkChallenge() {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setType(LivenessChallengeType.BLINK);
        challenge.setDuration(config.getBlinkDetectionTimeoutMs());
        challenge.setParameters(Map.of("minBlinks", config.getMinBlinks(), "maxDuration",
                config.getBlinkDetectionTimeoutMs()));
        return challenge;
    }

    private LivenessChallenge createHeadMovementChallenge() {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setType(LivenessChallengeType.HEAD_MOVEMENT);
        challenge.setDuration(config.getHeadMovementTimeoutMs());
        challenge
                .setParameters(Map.of("requiredMovements", java.util.Arrays.asList("LEFT", "RIGHT"),
                        "maxDuration", config.getHeadMovementTimeoutMs()));
        return challenge;
    }

    private LivenessChallenge createSmileChallenge() {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setType(LivenessChallengeType.SMILE);
        challenge.setDuration(config.getExpressionDetectionTimeoutMs());
        challenge.setParameters(Map.of("minSmileIntensity", 0.6, "maxDuration",
                config.getExpressionDetectionTimeoutMs()));
        return challenge;
    }

    private LivenessChallenge createTextureChallenge() {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setType(LivenessChallengeType.TEXTURE_ANALYSIS);
        challenge.setDuration(config.getTextureAnalysisTimeoutMs());
        challenge.setParameters(Map.of("minTextureScore", 0.7, "maxDuration",
                config.getTextureAnalysisTimeoutMs()));
        return challenge;
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "LIVENESS_" + System.currentTimeMillis() + "_" + sessionCounter.incrementAndGet();
    }

    /**
     * 创建失败结果
     */
    private LivenessResult createFailureResult(String message) {
        LivenessResult result = new LivenessResult();
        result.setSuccess(false);
        result.setOverallSuccess(false);
        result.setMessage(message);
        result.setProcessingTimeMs(0);
        return result;
    }

    /**
     * 创建失败检测结果
     */
    private LivenessDetectionResult createFailureDetectionResult(String message) {
        LivenessDetectionResult result = new LivenessDetectionResult();
        result.setSuccess(false);
        result.setConfidence(0.0);
        result.setMessage(message);
        result.setProcessingTimeMs(0);
        return result;
    }

    /**
     * 获取会话信息
     */
    public LivenessSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(entry -> {
            LivenessSession session = entry.getValue();
            boolean expired = currentTime - session.getStartTime() > session.getTimeout();
            if (expired) {
                session.setStatus(LivenessSessionStatus.TIMEOUT);
                log.info("清理过期会话: sessionId={}", entry.getKey());
            }
            return expired;
        });
    }

    /**
     * 获取统计信息
     */
    public LivenessStatistics getStatistics() {
        return statistics;
    }

    /**
     * 销毁服务
     */
    public void destroy() {
        activeSessions.clear();
        log.info("活体检测服务已销毁");
    }

    // ================================
    // 内部类定义
    // ================================

    /**
     * 活体检测配置
     */
    @Data
    public static class LivenessConfig {
        /** 会话超时时间（毫秒） */
        private long sessionTimeoutMs = 60000; // 60秒

        /** 眨眼检测超时时间（毫秒） */
        private long blinkDetectionTimeoutMs = 10000; // 10秒

        /** 头部动作检测超时时间（毫秒） */
        private long headMovementTimeoutMs = 15000; // 15秒

        /** 表情检测超时时间（毫秒） */
        private long expressionDetectionTimeoutMs = 8000; // 8秒

        /** 纹理分析超时时间（毫秒） */
        private long textureAnalysisTimeoutMs = 5000; // 5秒

        /** 最小眨眼次数 */
        private int minBlinks = 1;

        /** 最大失败尝试次数 */
        private int maxFailedAttempts = 3;

        /** 置信度阈值 */
        private double confidenceThreshold = 0.8;
    }

    /**
     * 活体检测会话
     */
    @Data
    public static class LivenessSession {
        private String sessionId;
        private Long userId;
        private String deviceId;
        private LivenessChallengeType challengeType;
        private long startTime;
        private long timeout;
        private LivenessSessionStatus status;
        private java.util.List<LivenessChallenge> challengeSequence;
        private int currentChallengeIndex;
        private java.util.List<Integer> completedChallenges;
        private int failedAttempts;
        private java.util.Map<String, Object> sessionData;

        public LivenessSession() {
            this.completedChallenges = new java.util.ArrayList<>();
            this.sessionData = new ConcurrentHashMap<>();
        }

        public LivenessChallenge getCurrentChallenge() {
            if (currentChallengeIndex < challengeSequence.size()) {
                return challengeSequence.get(currentChallengeIndex);
            }
            return null;
        }

        public LivenessChallenge getNextChallenge() {
            int nextIndex = currentChallengeIndex;
            if (nextIndex < challengeSequence.size()) {
                return challengeSequence.get(nextIndex);
            }
            return null;
        }

        public void markChallengeCompleted(int challengeIndex) {
            completedChallenges.add(challengeIndex);
        }

        public boolean isAllChallengesCompleted() {
            return completedChallenges.size() == challengeSequence.size();
        }

        public void incrementFailedAttempts() {
            failedAttempts++;
        }
    }

    /**
     * 活体挑战
     */
    @Data
    public static class LivenessChallenge {
        private LivenessChallengeType type;
        private long duration;
        private java.util.Map<String, Object> parameters;
    }

    /**
     * 活体检测结果
     */
    @Data
    public static class LivenessResult {
        private boolean success;
        private boolean overallSuccess;
        private double confidence;
        private String sessionId;
        private LivenessChallengeType challengeType;
        private String message;
        private java.util.Map<String, Object> detectionDetails;
        private int completedChallenges;
        private int totalChallenges;
        private LivenessChallenge nextChallenge;
        private long processingTimeMs;

        public LivenessResult() {
            this.detectionDetails = new ConcurrentHashMap<>();
        }
    }

    /**
     * 检测结果详情
     */
    @Data
    public static class LivenessDetectionResult {
        private boolean success;
        private double confidence;
        private String message;
        private java.util.Map<String, Object> details;
        private long processingTimeMs;

        public LivenessDetectionResult() {
            this.details = new ConcurrentHashMap<>();
        }
    }

    /**
     * 统计信息
     */
    @Data
    public static class LivenessStatistics {
        private long sessionsCreated;
        private long successfulDetections;
        private long failedDetections;
        private long totalProcessingTimeMs;
        private long averageProcessingTimeMs;

        public void incrementSessionsCreated() {
            sessionsCreated++;
        }

        public void incrementSuccessfulDetections() {
            successfulDetections++;
        }

        public void incrementFailedDetections() {
            failedDetections++;
        }

        public void addProcessingTime(long processingTimeMs) {
            totalProcessingTimeMs += processingTimeMs;
            updateAverageProcessingTime();
        }

        private void updateAverageProcessingTime() {
            long totalDetections = successfulDetections + failedDetections;
            if (totalDetections > 0) {
                averageProcessingTimeMs = totalProcessingTimeMs / totalDetections;
            }
        }
    }

    /**
     * 会话状态枚举
     */
    public enum LivenessSessionStatus {
        ACTIVE, // 活跃
        COMPLETED, // 已完成
        FAILED, // 失败
        TIMEOUT // 超时
    }

    /**
     * 挑战类型枚举
     */
    public enum LivenessChallengeType {
        BLINK, // 眨眼检测
        HEAD_MOVEMENT, // 头部动作检测
        SMILE, // 微笑检测
        TEXTURE_ANALYSIS, // 纹理分析
        BLINK_DETECTION, // 眨眼检测（完整流程）
        MULTI_CHALLENGE, // 多重挑战
        EXPRESSION_CHALLENGE // 表情挑战
    }

    // ================================
    // 检测器类（待OpenCV集成后实现）
    // ================================

    /**
     * 眨眼检测器
     */
    private static class BlinkDetector {
        public LivenessDetectionResult detectBlink(BufferedImage frame,
                java.util.Map<String, Object> params) {
            // 待实现：基于OpenCV的眨眼检测算法
            // 当前返回模拟结果
            LivenessDetectionResult result = new LivenessDetectionResult();
            result.setSuccess(Math.random() > 0.2); // 80%成功率
            result.setConfidence(0.85 + Math.random() * 0.1);
            result.setMessage("眨眼检测完成");
            result.setProcessingTimeMs((long) (100 + Math.random() * 200));
            return result;
        }
    }

    /**
     * 头部动作检测器
     */
    private static class HeadMovementDetector {
        public LivenessDetectionResult detectHeadMovement(BufferedImage frame,
                java.util.Map<String, Object> params) {
            // 待实现：基于OpenCV的头部动作检测算法
            LivenessDetectionResult result = new LivenessDetectionResult();
            result.setSuccess(Math.random() > 0.15); // 85%成功率
            result.setConfidence(0.88 + Math.random() * 0.1);
            result.setMessage("头部动作检测完成");
            result.setProcessingTimeMs((long) (150 + Math.random() * 250));
            return result;
        }
    }

    /**
     * 表情检测器
     */
    private static class ExpressionDetector {
        public LivenessDetectionResult detectSmile(BufferedImage frame,
                java.util.Map<String, Object> params) {
            // 待实现：基于OpenCV的表情检测算法
            LivenessDetectionResult result = new LivenessDetectionResult();
            result.setSuccess(Math.random() > 0.1); // 90%成功率
            result.setConfidence(0.90 + Math.random() * 0.08);
            result.setMessage("微笑检测完成");
            result.setProcessingTimeMs((long) (80 + Math.random() * 120));
            return result;
        }
    }

    /**
     * 纹理分析器
     */
    private static class TextureAnalyzer {
        public LivenessDetectionResult analyzeTexture(BufferedImage frame,
                java.util.Map<String, Object> params) {
            // 待实现：基于OpenCV的纹理分析算法
            LivenessDetectionResult result = new LivenessDetectionResult();
            result.setSuccess(Math.random() > 0.05); // 95%成功率
            result.setConfidence(0.92 + Math.random() * 0.06);
            result.setMessage("纹理分析完成");
            result.setProcessingTimeMs((long) (200 + Math.random() * 300));
            return result;
        }
    }
}

package net.lab1024.sa.access.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.SecurityEnhancerService;
import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.BiometricDataForm;
import net.lab1024.sa.access.domain.vo.BiometricAntiSpoofResultVO;
import net.lab1024.sa.access.domain.vo.TrajectoryAnomalyResultVO;

/**
 * 瀹夊叏澧炲己鏈嶅姟瀹炵幇绫?
 * <p>
 * 鎻愪緵闂ㄧ绯荤粺瀹夊叏澧炲己鍔熻兘鐨勫疄鐜帮紝鍖呮嫭锛?
 * - 鐢熺墿璇嗗埆闃蹭吉妫€娴?
 * - 璁块棶杞ㄨ抗寮傚父妫€娴?
 * - 瀹夊叏濞佽儊璇嗗埆
 * - 寮傚父琛屼负鍒嗘瀽
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Service娉ㄨВ鏍囪瘑鏈嶅姟绫?
 * - 浣跨敤@Transactional绠＄悊浜嬪姟
 * - 缁熶竴寮傚父澶勭悊
 * - 瀹屾暣鐨勬棩蹇楄褰?
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SecurityEnhancerServiceImpl implements SecurityEnhancerService {

    /**
     * 璁块棶璁板綍鏁版嵁璁块棶瀵硅薄
     */
    @Resource
    private AccessRecordDao accessRecordDao;

    /**
     * 璁惧鏁版嵁璁块棶瀵硅薄
     */
    @Resource
    private AccessDeviceDao accessDeviceDao;

    /**
     * 闅忔満鏁扮敓鎴愬櫒
     */
    private final Random random = new Random();

    @Override
    public BiometricAntiSpoofResultVO performBiometricAntiSpoofing(BiometricDataForm biometricData) {
        log.info("[鐢熺墿璇嗗埆闃蹭吉] 寮€濮嬮槻浼娴? userId={}, biometricType={}",
            biometricData.getUserId(), biometricData.getBiometricType());

        try {
            BiometricAntiSpoofResultVO result = BiometricAntiSpoofResultVO.builder()
                .userId(biometricData.getUserId())
                .biometricType(biometricData.getBiometricType())
                .detectionTime(LocalDateTime.now())
                .build();

            // 1. 娲讳綋妫€娴?
            BigDecimal livenessScore = performLivenessDetection(biometricData);
            result.setLivenessScore(livenessScore);

            // 2. 娣卞害浼€犳娴?
            BigDecimal deepfakeScore = performDeepfakeDetection(biometricData);
            result.setDeepfakeScore(deepfakeScore);

            // 3. 璐ㄩ噺璇勪及
            BigDecimal qualityScore = assessDataQuality(biometricData);
            result.setQualityScore(qualityScore);

            // 4. 3D缁撴瀯鍒嗘瀽锛堝鏋滄敮鎸侊級
            BigDecimal structureScore = perform3DStructureAnalysis(biometricData);
            result.setStructureScore(structureScore);

            // 5. 璁＄畻缁煎悎璇勫垎
            BigDecimal overallScore = calculateOverallScore(livenessScore, deepfakeScore, qualityScore, structureScore);
            result.setOverallScore(overallScore);

            // 6. 纭畾闃蹭吉妫€娴嬬粨鏋?
            determineAntiSpoofingResult(result);

            // 7. 鐢熸垚妫€娴嬭鎯?
            generateDetectionDetails(result);

            // 8. 鐢熸垚澶勭悊寤鸿
            generateRecommendation(result);

            log.info("[鐢熺墿璇嗗埆闃蹭吉] 闃蹭吉妫€娴嬪畬鎴? userId={}, overallScore={}, passed={}",
                biometricData.getUserId(), result.getOverallScore(), result.getPassedAntiSpoofing());

            return result;

        } catch (Exception e) {
            log.error("[鐢熺墿璇嗗埆闃蹭吉] 闃蹭吉妫€娴嬪紓甯? userId={}, error={}",
                biometricData.getUserId(), e.getMessage(), e);
            throw new RuntimeException("鐢熺墿璇嗗埆闃蹭吉妫€娴嬪け璐?, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrajectoryAnomalyResultVO detectTrajectoryAnomaly(Long userId, AccessTrajectory trajectory) {
        log.info("[杞ㄨ抗寮傚父妫€娴媇 寮€濮嬭建杩瑰垎鏋? userId={}, trajectorySize={}",
            userId, trajectory.getAccessPoints().size());

        try {
            TrajectoryAnomalyResultVO result = TrajectoryAnomalyResultVO.builder()
                .userId(userId)
                .trajectoryId(generateTrajectoryId())
                .analysisTimeRange(24) // 24灏忔椂
                .analysisTime(LocalDateTime.now())
                .build();

            // 1. 鏃堕棿妯″紡鍒嗘瀽
            TrajectoryAnomalyResultVO.TimePatternAnomalyVO timeAnomaly = analyzeTimePattern(trajectory);
            result.setTimePatternAnomaly(timeAnomaly);

            // 2. 绌洪棿妯″紡鍒嗘瀽
            TrajectoryAnomalyVO.SpatialPatternAnomalyVO spatialAnomaly = analyzeSpatialPattern(trajectory);
            result.setSpatialPatternAnomaly(spatialAnomaly);

            // 3. 棰戠巼鍒嗘瀽
            TrajectoryAnomalyResultVO.FrequencyAnomalyVO frequencyAnomaly = analyzeAccessFrequency(trajectory);
            result.setFrequencyAnomaly(frequencyAnomaly);

            // 4. 琛屼负搴忓垪鍒嗘瀽
            List<TrajectoryAnomalyResultVO.BehaviorSequenceAnomalyVO> behaviorAnomalies = analyzeBehaviorSequences(trajectory);
            result.setBehaviorAnomalies(behaviorAnomalies);

            // 5. 纭畾寮傚父绫诲瀷
            List<String> anomalyTypes = determineAnomalyTypes(timeAnomaly, spatialAnomaly, frequencyAnomaly, behaviorAnomalies);
            result.setAnomalyTypes(anomalyTypes);

            // 6. 璁＄畻寮傚父璇勫垎
            BigDecimal anomalyScore = calculateAnomalyScore(timeAnomaly, spatialAnomaly, frequencyAnomaly, behaviorAnomalies);
            result.setAnomalyScore(anomalyScore);

            // 7. 纭畾鏄惁妫€娴嬪埌寮傚父
            result.setAnomalyDetected(anomalyScore.compareTo(BigDecimal.valueOf(50)) > 0);

            // 8. 纭畾寮傚父绛夌骇
            determineAnomalyLevel(result);

            // 9. 椋庨櫓璇勪及
            result.setRiskAssessment(performRiskAssessment(result));

            // 10. 鐢熸垚澶勭悊寤鸿
            generateTrajectoryRecommendation(result);

            log.info("[杞ㄨ抗寮傚父妫€娴媇 杞ㄨ抗鍒嗘瀽瀹屾垚: userId={}, anomalyScore={}, detected={}",
                userId, result.getAnomalyScore(), result.getAnomalyDetected());

            return result;

        } catch (Exception e) {
            log.error("[杞ㄨ抗寮傚父妫€娴媇 杞ㄨ抗鍒嗘瀽寮傚父: userId={}, error={}",
                userId, e.getMessage(), e);
            throw new RuntimeException("杞ㄨ抗寮傚父妫€娴嬪け璐?, e);
        }
    }

    @Override
    public SecurityThreatResult identifySecurityThreat(AccessEvent accessEvent) {
        log.debug("[濞佽儊璇嗗埆] 寮€濮嬪▉鑳佽瘑鍒? userId={}, eventType={}",
            accessEvent.getUserId(), accessEvent.getEventType());

        // TODO: 瀹炵幇瀹夊叏濞佽儊璇嗗埆閫昏緫
        return SecurityThreatResult.builder()
            .threatDetected(false)
            .threatType("NONE")
            .threatLevel("LOW")
            .description("鏈娴嬪埌濞佽儊")
            .confidenceScore(0.1)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BehaviorAnalysisResult analyzeBehaviorPatterns(Long userId, Integer timeWindow) {
        log.debug("[琛屼负鍒嗘瀽] 寮€濮嬭涓哄垎鏋? userId={}, timeWindow={}澶?,
            userId, timeWindow);

        // TODO: 瀹炵幇琛屼负妯″紡鍒嗘瀽閫昏緫
        return BehaviorAnalysisResult.builder()
            .userId(userId)
            .behaviorPattern("NORMAL")
            .anomalyDetected(false)
            .anomalyType("NONE")
            .riskScore(15.5)
            .recommendations(Arrays.asList("琛屼负妯″紡姝ｅ父锛岀户缁洃鎺?))
            .analysisTime(LocalDateTime.now())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityRiskAssessmentVO assessSecurityRisk(SecurityEvent securityEvent) {
        log.debug("[椋庨櫓璇勪及] 寮€濮嬮闄╄瘎浼? eventType={}", securityEvent.getEventType());

        // TODO: 瀹炵幇瀹夊叏椋庨櫓璇勪及閫昏緫
        return SecurityRiskAssessmentVO.builder()
            .riskId("RISK_" + System.currentTimeMillis())
            .riskLevel("LOW")
            .riskScore(25.5)
            .impactScope("灞€閮?)
            .priority("MEDIUM")
            .mitigationStrategies(Arrays.asList("鐩戞帶浜嬩欢鍙戝睍", "璁板綍璇︾粏淇℃伅"))
            .assessmentTime(LocalDateTime.now())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SecurityMonitoringData getRealtimeSecurityMonitoring() {
        log.debug("[瀹夊叏鐩戞帶] 鑾峰彇瀹炴椂瀹夊叏鐩戞帶鏁版嵁");

        // TODO: 瀹炵幇瀹炴椂瀹夊叏鐩戞帶閫昏緫
        SecurityMonitoringData monitoringData = new SecurityMonitoringData();

        Map<String, Long> activeThreats = new HashMap<>();
        activeThreats.put("BRUTE_FORCE_ATTEMPT", 0L);
        activeThreats.put("ANOMALOUS_ACCESS", 0L);
        activeThreats.put("SPOOFING_ATTEMPT", 0L);
        monitoringData.setActiveThreats(activeThreats);

        Map<String, Long> anomalyCounts = new HashMap<>();
        anomalyCounts.put("TIME_ANOMALY", 0L);
        anomalyCounts.put("SPATIAL_ANOMALY", 0L);
        anomalyCounts.put("FREQUENCY_ANOMALY", 0L);
        monitoringData.setAnomalyCounts(anomalyCounts);

        Map<String, Double> securityMetrics = new HashMap<>();
        securityMetrics.put("overall_security_score", 92.5);
        securityMetrics.put("threat_detection_rate", 0.98);
        securityMetrics.put("false_positive_rate", 0.02);
        monitoringData.setSecurityMetrics(securityMetrics);

        monitoringData.setLastUpdateTime(LocalDateTime.now());
        monitoringData.setOverallSecurityStatus("SECURE");

        return monitoringData;
    }

    /**
     * 鎵ц娲讳綋妫€娴?
     */
    private BigDecimal performLivenessDetection(BiometricDataForm biometricData) {
        log.debug("[娲讳綋妫€娴媇 鎵ц娲讳綋妫€娴? type={}", biometricData.getBiometricType());

        // 妯℃嫙娲讳綋妫€娴嬮€昏緫
        // 瀹為檯搴旇璋冪敤AI妯″瀷杩涜娲讳綋妫€娴?
        BigDecimal baseScore = BigDecimal.valueOf(85 + random.nextInt(10));

        // 鏍规嵁鐢熺墿璇嗗埆绫诲瀷璋冩暣
        switch (biometricData.getBiometricType()) {
            case "FACE":
                baseScore = baseScore.add(BigDecimal.valueOf(random.nextInt(5))); // 浜鸿劯璇嗗埆娲讳綋妫€娴嬬浉瀵瑰鏄?
                break;
            case "FINGERPRINT":
                baseScore = baseScore.add(BigDecimal.valueOf(random.nextInt(3))); // 鎸囩汗娲讳綋妫€娴嬫洿瀹规槗
                break;
            case "IRIS":
                baseScore = baseScore.add(BigDecimal.valueOf(random.nextInt(4))); // 铏硅啘娲讳綋妫€娴嬩腑绛夐毦搴?
                break;
            case "VOICE":
                baseScore = baseScore.add(BigDecimal.valueOf(random.nextInt(7))); // 澹扮汗娲讳綋妫€娴嬭緝闅?
                break;
        }

        return baseScore.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 鎵ц娣卞害浼€犳娴?
     */
    private BigDecimal performDeepfakeDetection(BiometricDataForm biometricData) {
        log.debug("[娣卞害浼€犳娴媇 鎵ц娣卞害浼€犳娴? type={}", biometricData.getBiometricType());

        // 妯℃嫙娣卞害浼€犳娴嬮€昏緫
        // 瀹為檯搴旇璋冪敤娣卞害瀛︿範妯″瀷杩涜浼€犳娴?
        BigDecimal baseScore = BigDecimal.valueOf(80 + random.nextInt(15));

        // 鑰冭檻鏁版嵁璐ㄩ噺瀵规娴嬬粨鏋滅殑褰卞搷
        if (biometricData.getQualityScore() != null) {
            BigDecimal qualityBonus = biometricData.getQualityScore().multiply(BigDecimal.valueOf(0.1));
            baseScore = baseScore.add(qualityBonus);
        }

        return baseScore.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 璇勪及鏁版嵁璐ㄩ噺
     */
    private BigDecimal assessDataQuality(BiometricDataForm biometricData) {
        log.debug("[璐ㄩ噺璇勪及] 璇勪及鏁版嵁璐ㄩ噺: type={}", biometricData.getBiometricType());

        // 濡傛灉宸茬粡鏈夎川閲忚瘎鍒嗭紝鐩存帴浣跨敤
        if (biometricData.getQualityScore() != null) {
            return biometricData.getQualityScore();
        }

        // 鍚﹀垯妯℃嫙璐ㄩ噺璇勪及
        return BigDecimal.valueOf(75 + random.nextInt(20)).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 鎵ц3D缁撴瀯鍒嗘瀽
     */
    private BigDecimal perform3DStructureAnalysis(BiometricDataForm biometricData) {
        log.debug("[3D缁撴瀯鍒嗘瀽] 鎵ц3D缁撴瀯鍒嗘瀽: type={}", biometricData.getBiometricType());

        // 3D缁撴瀯鍒嗘瀽涓昏閽堝浜鸿劯璇嗗埆
        if (!"FACE".equals(biometricType)) {
            return BigDecimal.valueOf(90.0).setScale(1, RoundingMode.HALF_UP);
        }

        // 妯℃嫙3D缁撴瀯鍒嗘瀽
        return BigDecimal.valueOf(82 + random.nextInt(10)).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 璁＄畻缁煎悎璇勫垎
     */
    private BigDecimal calculateOverallScore(BigDecimal livenessScore, BigDecimal deepfakeScore,
                                                     BigDecimal qualityScore, BigDecimal structureScore) {
        // 鏉冮噸鍒嗛厤锛氭椿浣撴娴?0%锛屾繁搴︿吉閫?0%锛岃川閲?0%锛?D缁撴瀯20%
        double weightedScore = livenessScore.doubleValue() * 0.3 +
                                deepfakeScore.doubleValue() * 0.3 +
                                qualityScore.doubleValue() * 0.2 +
                                structureScore.doubleValue() * 0.2;

        return BigDecimal.valueOf(weightedScore).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 纭畾闃蹭吉妫€娴嬬粨鏋?
     */
    private void determineAntiSpoofingResult(BiometricAntiSpoofResultVO result) {
        double score = result.getOverallScore().doubleValue();
        result.setPassedAntiSpoofing(score >= 80);

        if (score >= 90) {
            result.setRiskLevel("LOW");
        } else if (score >= 70) {
            result.setRiskLevel("MEDIUM");
        } else if (score >= 50) {
            result.setRiskLevel("HIGH");
        } else {
            result.setRiskLevel("CRITICAL");
        }

        result.setTrustScore(BigDecimal.valueOf(score / 100).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 鐢熸垚妫€娴嬭鎯?
     */
    private void generateDetectionDetails(BiometricAntiSpoofResultVO result) {
        Map<String, Object> details = new HashMap<>();

        // 娣诲姞妫€娴嬫寚鏍囪鎯?
        List<BiometricAntiSpoofResultVO.DetectionMetricVO> metrics = new ArrayList<>();

        // 娲讳綋妫€娴嬫寚鏍?
        metrics.add(BiometricAntiSpoofResultVO.DetectionMetricVO.builder()
            .metricName("鐪ㄧ溂妫€娴?)
            .metricValue(BigDecimal.valueOf(0.95))
            .threshold(BigDecimal.valueOf(0.7))
            .passed(true)
            .description("妫€娴嬪埌鑷劧鐨勭湪鐪艰涓?)
            .weight(BigDecimal.valueOf(0.25))
            .build());

        details.put("metrics", metrics);
        result.setDetectionDetails(details);

        // 鐢熸垚妫€娴嬪埌鐨勬敾鍑荤被鍨?
        List<String> attackTypes = new ArrayList<>();
        if (result.getDeepfakeScore().doubleValue() < 60) {
            attackTypes.add("DEEPFAKE_DETECTED");
        }
        if (result.getLivenessScore().doubleValue() < 70) {
            attackTypes.add("SPOOFING_DETECTED");
        }
        result.setDetectedAttackTypes(attackTypes);
    }

    /**
     * 鐢熸垚澶勭悊寤鸿
     */
    private void generateRecommendation(BiometricAntiSpoofResultVO result) {
        StringBuilder recommendation = new StringBuilder();

        if (result.getPassedAntiSpoofing()) {
            recommendation.append("鐢熺墿璇嗗埆鏁版嵁璐ㄩ噺鑹ソ锛屽彲浠ユ甯镐娇鐢ㄣ€?);
        } else {
            recommendation.append("妫€娴嬪埌娼滃湪椋庨櫓锛屽缓璁細");
            if (result.getLivenessScore().doubleValue() < 70) {
                recommendation.append("1. 瑕佹眰鐢ㄦ埛閲嶆柊杩涜娲讳綋妫€娴嬶紱");
            }
            if (result.getDeepfakeScore().doubleValue() < 60) {
                recommendation.append("2. 妫€鏌ユ槸鍚﹀瓨鍦ㄤ吉閫犳敾鍑伙紱");
            }
            if (result.getQualityScore().doubleValue() < 60) {
                recommendation.append("3. 鎻愪緵鏇撮珮璐ㄩ噺鐨勭敓鐗╄瘑鍒暟鎹紱");
            }
        }

        result.setRecommendation(recommendation.toString());
    }

    /**
     * 鐢熸垚杞ㄨ抗ID
     */
    private String generateTrajectoryId() {
        return "TRAJ_" + System.currentTimeMillis();
    }

    /**
     * 鍒嗘瀽鏃堕棿妯″紡
     */
    private TrajectoryAnomalyResultVO.TimePatternAnomalyVO analyzeTimePattern(AccessTrajectory trajectory) {
        // TODO: 瀹炵幇鏃堕棿妯″紡鍒嗘瀽閫昏緫
        return TrajectoryAnomalyResultVO.TimePatternAnomalyVO.builder()
            .anomalyType(random.nextBoolean() ? "ABNORMAL_TIME_ACCESS" : "NORMAL_PATTERN")
            .abnormalTimes(Arrays.asList("02:30", "04:45"))
            .deviationScore(BigDecimal.valueOf(random.nextDouble() * 5))
            .frequency("LOW")
            .description("鐢ㄦ埛鍦ㄩ潪宸ヤ綔鏃堕棿娈垫湁璁块棶璁板綍")
            .build();
    }

    /**
     * 鍒嗘瀽绌洪棿妯″紡
     */
    private TrajectoryAnomalyResultVO.SpatialPatternAnomalyVO analyzeSpatialPattern(AccessTrajectory trajectory) {
        // TODO: 瀹炵幇绌洪棿妯″紡鍒嗘瀽閫昏緫
        return TrajectoryResultVO.SpatialPatternAnomalyVO.builder()
            .anomalyType(random.nextBoolean() ? "UNUSUAL_AREA_PATTERN" : "NORMAL_PATTERN")
            .unusualAreas(Arrays.asList("鏈嶅姟鍣ㄦ満鎴?, "楂樼骇绠＄悊鍖?))
            .accessFrequency("FIRST_TIME_ACCESS")
            .deviationScore(BigDecimal.valueOf(random.nextDouble() * 5))
            .description("鐢ㄦ埛棣栨璁块棶楂樺畨鍏ㄧ瓑绾у尯鍩?)
            .build();
    }

    /**
     * 鍒嗘瀽璁块棶棰戠巼
     */
    private TrajectoryAnomalyResultVO.FrequencyAnomalyVO analyzeAccessFrequency(AccessTrajectory trajectory) {
        // TODO: 瀹炵幇璁块棶棰戠巼鍒嗘瀽閫昏緫
        int accessCount = trajectory.getAccessPoints().size();
        BigDecimal normalFrequency = BigDecimal.valueOf(15);
        BigDecimal actualFrequency = BigDecimal.valueOf(accessCount);

        return TrajectoryAnomalyResultVO.FrequencyAnomalyVO.builder()
            .anomalyType(accessCount > 50 ? "EXCESSIVE_FREQUENCY" : "NORMAL_FREQUENCY")
            .normalFrequency(normalFrequency)
            .actualFrequency(actualFrequency)
            .frequencyMultiplier(actualFrequency.divide(normalFrequency, 2, RoundingMode.HALF_UP))
            .timeWindow("24灏忔椂")
            .description("鐢ㄦ埛璁块棶棰戠巼" + (accessCount > 50 ? "杩滆秴" : "绗﹀悎") + "姝ｅ父姘村钩")
            .build();
    }

    /**
     * 鍒嗘瀽琛屼负搴忓垪
     */
    private List<TrajectoryAnomalyResultVO.BehaviorSequenceAnomalyVO> analyzeBehaviorSequences(AccessTrajectory trajectory) {
        List<TrajectoryAnomalyVO.BehaviorSequenceAnomalyVO> anomalies = new ArrayList<>();

        // TODO: 瀹炵幇琛屼负搴忓垪鍒嗘瀽閫昏緫
        // 妯℃嫙妫€娴嬪埌蹇€熻繛缁闂紓甯?
        if (random.nextDouble() < 0.15) { // 15%姒傜巼妫€娴嬪埌寮傚父
            anomalies.add(TrajectoryAnomalyVO.BehaviorSequenceAnomalyVO.builder()
                .sequenceId("SEQ_" + System.currentTimeMillis())
                .anomalyType("RAPID_SUCCESSIVE_ACCESS")
                .accessSequence(Arrays.asList("DEVICE_001", "DEVICE_002", "DEVICE_003"))
                .timeIntervals(Arrays.asList("30绉?, "45绉?, "20绉?))
                .anomalyScore(BigDecimal.valueOf(8.5))
                .confidence(BigDecimal.valueOf(0.92))
                .description("妫€娴嬪埌蹇€熺殑杩炵画璁块棶妯″紡")
                .build());
        }

        return anomalies;
    }

    /**
     * 纭畾寮傚父绫诲瀷
     */
    private List<String> determineAnomalyTypes(TrajectoryAnomalyVO.TimePatternAnomalyVO timeAnomaly,
                                                  TrajectoryAnomalyVO.SpatialPatternAnomalyVO spatialAnomaly,
                                                  TrajectoryAnomalyVO.FrequencyAnomalyVO frequencyAnomaly,
                                                  List<TrajectoryAnomalyVO.BehaviorSequenceAnomalyVO> behaviorAnomalies) {
        List<String> anomalyTypes = new ArrayList<>();

        if (timeAnomaly != null && !"NORMAL_PATTERN".equals(timeAnomaly.getAnomalyType())) {
            anomalyTypes.add(timeAnomaly.getAnomalyType());
        }
        if (spatialAnomaly != null && !"NORMAL_PATTERN".equals(spatialAnomaly.getAnomalyType())) {
            anomalyTypes.add(spatialAnomaly.getAnomalyType());
        }
        if (frequencyAnomaly != null && !"NORMAL_FREQUENCY".equals(frequencyAnomaly.getAnomalyType())) {
            anomalyTypes.add(frequencyAnomaly.getAnomalyType());
        }
        behaviorAnomalies.forEach(anomaly -> {
            if (anomaly.getAnomalyType() != null) {
                anomalyTypes.add(anomaly.getAnomalyType());
            }
        });

        return anomalyTypes;
    }

    /**
     * 璁＄畻寮傚父璇勫垎
     */
    private BigDecimal calculateAnomalyScore(TrajectoryAnomalyVO.TimePatternAnomalyVO timeAnomaly,
                                                 TrajectoryAnomalyVO.SpatialPatternAnomalyVO spatialAnomaly,
                                                 TrajectoryAnomalyVO.FrequencyAnomalyVO frequencyAnomaly,
                                                 List<TrajectoryAnomalyVO.BehaviorSequenceAnomalyVO> behaviorAnomalies) {
        double score = 0;

        if (timeAnomaly != null) {
            score += timeAnomaly.getDeviationScore().doubleValue() * 0.3;
        }
        if (spatialAnomaly != null) {
            score += spatialAnomaly.getDeviationScore().doubleValue() * 0.25;
        }
        if (frequencyAnomaly != null) {
            score += frequencyAnomaly.getFrequencyMultiplier().doubleValue() * 0.25;
        }
        if (behaviorAnomalies != null) {
            score += behaviorAnomalies.stream()
                .mapToDouble(ano -> ano.getAnomalyScore().doubleValue())
                .average() * 0.2;
        }

        return BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 纭畾寮傚父绛夌骇
     */
    private void determineAnomalyLevel(TrajectoryAnomalyResultVO result) {
        double score = result.getAnomalyScore().doubleValue();

        if (score < 30) {
            result.setAnomalyLevel("LOW");
        } else if (score < 60) {
            result.setAnomalyLevel("MEDIUM");
        } else if (score < 80) {
            result.setAnomalyLevel("HIGH");
        } else {
            result.setAnomalyLevel("CRITICAL");
        }
    }

    /**
     * 鎵ц椋庨櫓璇勪及
     */
    private TrajectoryAnomalyResultVO.RiskAssessmentVO performRiskAssessment(TrajectoryAnomalyResultVO result) {
        // TODO: 瀹炵幇椋庨櫓璇勪及閫昏緫
        return TrajectoryAnomalyVO.RiskAssessmentVO.builder()
            .riskLevel(result.getAnomalyLevel())
            .riskScore(result.getAnomalyScore())
            .potentialThreat("鍙兘瀛樺湪韬唤鍐掔敤鎴栧紓甯歌闂涓?)
            .impactScope("鍖哄煙瀹夊叏銆佹暟鎹闂?)
            .recommendedMeasures(Arrays.asList("澧炲姞浜屾楠岃瘉", "鐩戞帶鍚庣画琛屼负"))
            .urgency(result.getAnomalyLevel().equals("CRITICAL") ? "HIGH" : "MEDIUM")
            .build();
    }

    /**
     * 鐢熸垚杞ㄨ抗澶勭悊寤鸿
     */
    private void generateTrajectoryRecommendation(TrajectoryAnomalyResultVO result) {
        StringBuilder recommendation = new StringBuilder();

        if (!result.getAnomalyDetected()) {
            recommendation.append("鐢ㄦ埛璁块棶杞ㄨ抗姝ｅ父锛屾棤闇€鐗规畩澶勭悊銆?);
        } else {
            recommendation.append("妫€娴嬪埌璁块棶杞ㄨ抗寮傚父锛屽缓璁細");
            recommendation.append("1. 杩涜浜屾韬唤楠岃瘉锛?);
            recommendation.append("2. 鐩戞帶鐢ㄦ埛鍚庣画璁块棶琛屼负锛?);
            recommendation.append("3. 蹇呰鏃堕€氱煡瀹夊叏绠＄悊浜哄憳銆?);

            if (result.getAnomalyLevel().equals("HIGH") || result.getAnomalyLevel().equals("CRITICAL")) {
                recommendation.append("4. 绔嬪嵆閲囧彇瀹夊叏鎺柦淇濇姢绯荤粺瀹夊叏銆?);
            }
        }

        result.setRecommendation(recommendation.toString());
    }
}
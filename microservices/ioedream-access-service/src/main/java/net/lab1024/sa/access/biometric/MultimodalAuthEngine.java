package net.lab1024.sa.access.biometric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 多模态认证引擎
 * 支持人脸、指纹、虹膜、掌纹等多种生物特征的融合认证
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class MultimodalAuthEngine {

    /**
     * 生物特征类型枚举
     */
    public enum BiometricType {
        FACE("FACE", "人脸识别"),
        FINGERPRINT("FINGERPRINT", "指纹识别"),
        IRIS("IRIS", "虹膜识别"),
        PALMPRINT("PALMPRINT", "掌纹识别"),
        VOICE("VOICE", "声纹识别"),
        FACE_3D("FACE_3D", "3D人脸识别");

        private final String code;
        private final String description;

        BiometricType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 认证策略枚举
     */
    public enum AuthStrategy {
        SINGLE_FACTOR("SINGLE_FACTOR", "单因子认证"),
        MULTI_FACTOR("MULTI_FACTOR", "多因子认证"),
        ADAPTIVE("ADAPTIVE", "自适应认证"),
        WEIGHTED_FUSION("WEIGHTED_FUSION", "加权融合认证");

        private final String code;
        private final String description;

        AuthStrategy(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 认证结果枚举
     */
    public enum AuthResult {
        SUCCESS("SUCCESS", "认证成功"),
        FAILURE("FAILURE", "认证失败"),
        PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功"),
        INCONCLUSIVE("INCONCLUSIVE", "结果不确定"),
        TIMEOUT("TIMEOUT", "认证超时"),
        ERROR("ERROR", "系统错误");

        private final String code;
        private final String description;

        AuthResult(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 生物特征模板
     */
    @Data
    public static class BiometricTemplate {
        private Long templateId;
        private Long employeeId;
        private BiometricType biometricType;
        private String featureData; // 特征数据
        private String qualityScore; // 质量评分
        private LocalDateTime createTime;
        private LocalDateTime lastUpdateTime;
        private Integer usageCount; // 使用次数
        private Boolean isActive; // 是否激活
        private String algorithmVersion; // 算法版本
        private Map<String, Object> metadata; // 元数据
    }

    /**
     * 认证请求
     */
    @Data
    public static class AuthenticationRequest {
        private String requestId;
        private Long employeeId; // 员工ID（可选，用于1:1验证）
        private AuthStrategy strategy; // 认证策略
        private List<BiometricData> biometricData; // 生物特征数据
        private Map<String, Object> context; // 认证上下文
        private LocalDateTime requestTime;
        private Integer timeoutSeconds; // 超时时间
        private BigDecimal securityLevel; // 安全级别要求
    }

    /**
     * 生物特征数据
     */
    @Data
    public static class BiometricData {
        private BiometricType biometricType;
        private String rawData; // 原始数据
        private String processedData; // 处理后数据
        private BigDecimal qualityScore; // 数据质量评分
        private Map<String, Object> captureMetadata; // 采集元数据
    }

    /**
     * 认证响应
     */
    @Data
    public static class AuthenticationResponse {
        private String requestId;
        private AuthResult result; // 认证结果
        private Long employeeId; // 匹配的员工ID
        private String employeeName; // 员工姓名
        private BigDecimal confidenceScore; // 综合置信度
        private List<FactorResult> factorResults; // 各因子结果
        private String strategy; // 使用的策略
        private Integer processingTimeMs; // 处理时间
        private LocalDateTime responseTime;
        private String message; // 结果消息
        private List<String> warnings; // 警告信息
        private Map<String, Object> details; // 详细信息
    }

    /**
     * 单因子认证结果
     */
    @Data
    public static class FactorResult {
        private BiometricType biometricType;
        private AuthResult result;
        private BigDecimal confidenceScore;
        private BigDecimal matchScore; // 匹配分数
        private String templateId; // 使用的模板ID
        private Integer processingTimeMs;
        private String algorithmVersion;
        private Map<String, Object> details;
    }

    /**
     * 融合权重配置
     */
    @Data
    public static class FusionWeights {
        private Map<BiometricType, BigDecimal> weights;
        private BigDecimal globalThreshold;
        private Map<BiometricType, BigDecimal> thresholds;
        private Boolean enableAdaptiveWeighting;
    }

    /**
     * 生物特征模板存储
     */
    private Map<Long, List<BiometricTemplate>> templateStore = new HashMap<>();

    /**
     * 认证规则配置
     */
    private Map<AuthStrategy, FusionWeights> fusionConfigs = new HashMap<>();

    /**
     * 认证统计信息
     */
    private Map<String, Object> statistics = new HashMap<>();

    /**
     * 初始化多模态认证引擎
     */
    public void initializeEngine() {
        log.info("多模态认证引擎初始化开始");

        // 加载默认融合权重配置
        loadDefaultFusionConfigs();

        // 加载生物特征模板
        loadBiometricTemplates();

        log.info("多模态认证引擎初始化完成，支持{}种生物特征类型", BiometricType.values().length);
    }

    /**
     * 加载默认融合权重配置
     */
    private void loadDefaultFusionConfigs() {
        // 单因子认证配置
        FusionWeights singleFactorWeights = new FusionWeights();
        singleFactorWeights.setWeights(new HashMap<>());
        singleFactorWeights.getWeights().put(BiometricType.FACE, BigDecimal.ONE);
        singleFactorWeights.setGlobalThreshold(BigDecimal.valueOf(0.85));
        singleFactorWeights.setEnableAdaptiveWeighting(false);
        fusionConfigs.put(AuthStrategy.SINGLE_FACTOR, singleFactorWeights);

        // 多因子认证配置
        FusionWeights multiFactorWeights = new FusionWeights();
        multiFactorWeights.setWeights(new HashMap<>());
        multiFactorWeights.getWeights().put(BiometricType.FACE, BigDecimal.valueOf(0.6));
        multiFactorWeights.getWeights().put(BiometricType.FINGERPRINT, BigDecimal.valueOf(0.4));
        multiFactorWeights.setGlobalThreshold(BigDecimal.valueOf(0.75));
        multiFactorWeights.setThresholds(new HashMap<>());
        multiFactorWeights.getThresholds().put(BiometricType.FACE, BigDecimal.valueOf(0.8));
        multiFactorWeights.getThresholds().put(BiometricType.FINGERPRINT, BigDecimal.valueOf(0.7));
        multiFactorWeights.setEnableAdaptiveWeighting(true);
        fusionConfigs.put(AuthStrategy.MULTI_FACTOR, multiFactorWeights);

        // 自适应认证配置
        FusionWeights adaptiveWeights = new FusionWeights();
        adaptiveWeights.setWeights(new HashMap<>());
        adaptiveWeights.getWeights().put(BiometricType.FACE, BigDecimal.valueOf(0.5));
        adaptiveWeights.getWeights().put(BiometricType.FINGERPRINT, BigDecimal.valueOf(0.3));
        adaptiveWeights.getWeights().put(BiometricType.IRIS, BigDecimal.valueOf(0.2));
        adaptiveWeights.setGlobalThreshold(BigDecimal.valueOf(0.7));
        adaptiveWeights.setEnableAdaptiveWeighting(true);
        fusionConfigs.put(AuthStrategy.ADAPTIVE, adaptiveWeights);

        log.debug("加载了{}个认证策略配置", fusionConfigs.size());
    }

    /**
     * 加载生物特征模板
     */
    private void loadBiometricTemplates() {
        // 模拟加载生物特征模板
        for (long employeeId = 1L; employeeId <= 100L; employeeId++) {
            List<BiometricTemplate> templates = new ArrayList<>();

            // 人脸模板
            BiometricTemplate faceTemplate = new BiometricTemplate();
            faceTemplate.setTemplateId(System.currentTimeMillis() + employeeId * 1000);
            faceTemplate.setEmployeeId(employeeId);
            faceTemplate.setBiometricType(BiometricType.FACE);
            faceTemplate.setFeatureData("face_feature_" + employeeId);
            faceTemplate.setQualityScore("0.95");
            faceTemplate.setCreateTime(LocalDateTime.now().minusDays(30));
            faceTemplate.setIsActive(true);
            templates.add(faceTemplate);

            // 指纹模板
            BiometricTemplate fingerprintTemplate = new BiometricTemplate();
            fingerprintTemplate.setTemplateId(System.currentTimeMillis() + employeeId * 1000 + 1);
            fingerprintTemplate.setEmployeeId(employeeId);
            fingerprintTemplate.setBiometricType(BiometricType.FINGERPRINT);
            fingerprintTemplate.setFeatureData("fingerprint_feature_" + employeeId);
            fingerprintTemplate.setQualityScore("0.92");
            fingerprintTemplate.setCreateTime(LocalDateTime.now().minusDays(30));
            fingerprintTemplate.setIsActive(true);
            templates.add(fingerprintTemplate);

            templateStore.put(employeeId, templates);
        }

        log.debug("加载了{}个员工的生物特征模板", templateStore.size());
    }

    /**
     * 执行多模态认证
     *
     * @param request 认证请求
     * @return 认证响应
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("开始多模态认证：请求ID={}，策略={}，生物特征数量={}",
                request.getRequestId(), request.getStrategy(), request.getBiometricData().size());

        AuthenticationResponse response = new AuthenticationResponse();
        response.setRequestId(request.getRequestId());
        response.setResponseTime(LocalDateTime.now());
        response.setFactorResults(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        response.setDetails(new HashMap<>());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证请求参数
            String validationError = validateRequest(request);
            if (validationError != null) {
                response.setResult(AuthResult.ERROR);
                response.setMessage("请求参数错误：" + validationError);
                return response;
            }

            // 2. 获取融合权重配置
            FusionWeights weights = fusionConfigs.get(request.getStrategy());
            if (weights == null) {
                response.setResult(AuthResult.ERROR);
                response.setMessage("不支持的认证策略");
                return response;
            }

            // 3. 执行单因子认证
            List<FactorResult> factorResults = new ArrayList<>();
            for (BiometricData biometricData : request.getBiometricData()) {
                FactorResult factorResult = authenticateSingleFactor(
                        biometricData, request.getEmployeeId(), weights);
                factorResults.add(factorResult);
            }
            response.setFactorResults(factorResults);

            // 4. 融合认证结果
            AuthResult fusionResult = fuseResults(factorResults, weights);
            response.setResult(fusionResult);

            // 5. 计算综合置信度
            BigDecimal confidenceScore = calculateFusionConfidence(factorResults, weights);
            response.setConfidenceScore(confidenceScore);

            // 6. 确定最终结果
            if (fusionResult == AuthResult.SUCCESS) {
                // 找到匹配的员工ID
                Long matchedEmployeeId = findBestMatchEmployeeId(factorResults);
                response.setEmployeeId(matchedEmployeeId);
                response.setEmployeeName("员工" + matchedEmployeeId); // 简化处理
            }

            // 7. 设置响应信息
            response.setStrategy(request.getStrategy().getDescription());
            response.setResponseTime(LocalDateTime.now());
            response.setProcessingTimeMs((int) (System.currentTimeMillis() - startTime));

            String message = generateResponseMessage(fusionResult, confidenceScore);
            response.setMessage(message);

            // 8. 更新统计信息
            updateStatistics(fusionResult, request.getStrategy(), factorResults);

            log.info("多模态认证完成：请求ID={}，结果={}，置信度={}，处理时间={}ms",
                    request.getRequestId(), fusionResult, confidenceScore, response.getProcessingTimeMs());

        } catch (Exception e) {
            log.error("多模态认证失败", e);
            response.setResult(AuthResult.ERROR);
            response.setMessage("认证失败：" + e.getMessage());
            response.setResponseTime(LocalDateTime.now());
            response.setProcessingTimeMs((int) (System.currentTimeMillis() - startTime));
        }

        return response;
    }

    /**
     * 验证请求参数
     */
    private String validateRequest(AuthenticationRequest request) {
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            return "请求ID不能为空";
        }

        if (request.getBiometricData() == null || request.getBiometricData().isEmpty()) {
            return "生物特征数据不能为空";
        }

        if (request.getStrategy() == null) {
            return "认证策略不能为空";
        }

        // 验证生物特征数据
        for (BiometricData data : request.getBiometricData()) {
            if (data.getBiometricType() == null) {
                return "生物特征类型不能为空";
            }
            if (data.getRawData() == null || data.getRawData().trim().isEmpty()) {
                return "生物特征原始数据不能为空";
            }
        }

        return null;
    }

    /**
     * 单因子认证
     */
    private FactorResult authenticateSingleFactor(BiometricData biometricData, Long targetEmployeeId,
            FusionWeights weights) {
        long startTime = System.currentTimeMillis();

        FactorResult result = new FactorResult();
        result.setBiometricType(biometricData.getBiometricType());
        result.setAlgorithmVersion("v2.1.0");
        result.setDetails(new HashMap<>());

        try {
            // 模拟特征提取
            String extractedFeatures = extractFeatures(biometricData);
            result.getDetails().put("extractedFeatures", extractedFeatures);

            // 模拟模板匹配
            TemplateMatchResult matchResult = matchTemplates(extractedFeatures, biometricData.getBiometricType(),
                    targetEmployeeId);

            result.setMatchScore(matchResult.getMatchScore());
            result.setConfidenceScore(
                    calculateSingleFactorConfidence(matchResult.getMatchScore(), biometricData.getQualityScore()));
            result.setTemplateId(matchResult.getTemplateId());

            // 判断认证结果
            BigDecimal threshold = weights.getThresholds() != null
                    ? weights.getThresholds().get(biometricData.getBiometricType())
                    : weights.getGlobalThreshold();

            if (result.getConfidenceScore().compareTo(threshold) >= 0) {
                result.setResult(AuthResult.SUCCESS);
            } else {
                result.setResult(AuthResult.FAILURE);
            }

        } catch (Exception e) {
            log.error("单因子认证失败", e);
            result.setResult(AuthResult.ERROR);
            result.setConfidenceScore(BigDecimal.ZERO);
        }

        result.setProcessingTimeMs((int) (System.currentTimeMillis() - startTime));
        return result;
    }

    /**
     * 特征提取（模拟）
     */
    private String extractFeatures(BiometricData biometricData) {
        // 模拟特征提取过程
        return "extracted_features_" + biometricData.getBiometricType().getCode() + "_" + System.currentTimeMillis();
    }

    /**
     * 模板匹配（模拟）
     */
    private TemplateMatchResult matchTemplates(String features, BiometricType biometricType, Long targetEmployeeId) {
        TemplateMatchResult result = new TemplateMatchResult();

        // 模拟匹配过程
        BigDecimal matchScore = BigDecimal.valueOf(0.85 + Math.random() * 0.14);
        result.setMatchScore(matchScore);

        // 查找最佳匹配的模板
        String bestTemplateId = null;
        for (Map.Entry<Long, List<BiometricTemplate>> entry : templateStore.entrySet()) {
            for (BiometricTemplate template : entry.getValue()) {
                if (template.getBiometricType().equals(biometricType) && template.getIsActive()) {
                    // 如果指定了目标员工ID，优先匹配
                    if (targetEmployeeId != null && !targetEmployeeId.equals(template.getEmployeeId())) {
                        continue;
                    }

                    // 简化处理，选择第一个匹配的模板
                    bestTemplateId = template.getTemplateId().toString();
                    result.setEmployeeId(template.getEmployeeId());
                    break;
                }
            }
            if (bestTemplateId != null) {
                break;
            }
        }

        result.setTemplateId(bestTemplateId);
        return result;
    }

    /**
     * 计算单因子置信度
     */
    private BigDecimal calculateSingleFactorConfidence(BigDecimal matchScore, BigDecimal qualityScore) {
        if (qualityScore == null) {
            qualityScore = BigDecimal.valueOf(0.8);
        }

        // 匹配分数权重70%，质量分数权重30%
        BigDecimal matchWeighted = matchScore.multiply(BigDecimal.valueOf(0.7));
        BigDecimal qualityWeighted = qualityScore.multiply(BigDecimal.valueOf(0.3));

        return matchWeighted.add(qualityWeighted).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 融合认证结果
     */
    private AuthResult fuseResults(List<FactorResult> factorResults, FusionWeights weights) {
        if (factorResults.isEmpty()) {
            return AuthResult.FAILURE;
        }

        // 计算加权平均置信度
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedConfidence = BigDecimal.ZERO;
        int successCount = 0;
        int failureCount = 0;

        for (FactorResult result : factorResults) {
            BigDecimal factorWeight = weights.getWeights().get(result.getBiometricType());
            totalWeight = totalWeight.add(factorWeight);
            weightedConfidence = weightedConfidence.add(result.getConfidenceScore().multiply(factorWeight));

            if (result.getResult() == AuthResult.SUCCESS) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        BigDecimal averageConfidence = totalWeight.compareTo(BigDecimal.ZERO) > 0
                ? weightedConfidence.divide(totalWeight, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 根据策略判断最终结果
        AuthStrategy strategy = null;
        for (Map.Entry<AuthStrategy, FusionWeights> entry : fusionConfigs.entrySet()) {
            if (entry.getValue().equals(weights)) {
                strategy = entry.getKey();
                break;
            }
        }

        // 如果未找到策略，返回失败
        if (strategy == null) {
            return AuthResult.FAILURE;
        }

        switch (strategy) {
            case SINGLE_FACTOR:
                return factorResults.get(0).getResult();
            case MULTI_FACTOR:
                if (successCount == factorResults.size()) {
                    return averageConfidence.compareTo(weights.getGlobalThreshold()) >= 0 ? AuthResult.SUCCESS
                            : AuthResult.INCONCLUSIVE;
                } else if (successCount > failureCount) {
                    return AuthResult.PARTIAL_SUCCESS;
                } else {
                    return AuthResult.FAILURE;
                }
            case ADAPTIVE:
                // 自适应策略：根据置信度和成功率判断
                if (averageConfidence.compareTo(weights.getGlobalThreshold()) >= 0
                        && successCount >= factorResults.size() / 2) {
                    return AuthResult.SUCCESS;
                } else if (averageConfidence.compareTo(BigDecimal.valueOf(0.6)) >= 0) {
                    return AuthResult.PARTIAL_SUCCESS;
                } else {
                    return AuthResult.FAILURE;
                }
            default:
                return AuthResult.FAILURE;
        }
    }

    /**
     * 计算融合置信度
     */
    private BigDecimal calculateFusionConfidence(List<FactorResult> factorResults, FusionWeights weights) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedConfidence = BigDecimal.ZERO;

        for (FactorResult result : factorResults) {
            BigDecimal factorWeight = weights.getWeights().get(result.getBiometricType());
            totalWeight = totalWeight.add(factorWeight);
            weightedConfidence = weightedConfidence.add(result.getConfidenceScore().multiply(factorWeight));
        }

        return totalWeight.compareTo(BigDecimal.ZERO) > 0
                ? weightedConfidence.divide(totalWeight, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    /**
     * 查找最佳匹配的员工ID
     */
    private Long findBestMatchEmployeeId(List<FactorResult> factorResults) {
        Map<Long, BigDecimal> employeeScores = new HashMap<>();

        for (FactorResult result : factorResults) {
            if (result.getResult() == AuthResult.SUCCESS && result.getTemplateId() != null) {
                // 从模板ID解析员工ID（简化处理）
                Long employeeId = Long.parseLong(result.getTemplateId()) / 1000;
                employeeScores.put(employeeId,
                        employeeScores.getOrDefault(employeeId, BigDecimal.ZERO).add(result.getConfidenceScore()));
            }
        }

        // 返回得分最高的员工ID
        return employeeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 生成响应消息
     */
    private String generateResponseMessage(AuthResult result, BigDecimal confidenceScore) {
        switch (result) {
            case SUCCESS:
                return String.format("认证成功（置信度：%.2f%%）", confidenceScore.multiply(BigDecimal.valueOf(100)));
            case FAILURE:
                return "认证失败";
            case PARTIAL_SUCCESS:
                return String.format("部分认证成功（置信度：%.2f%%）", confidenceScore.multiply(BigDecimal.valueOf(100)));
            case INCONCLUSIVE:
                return "认证结果不确定";
            default:
                return "认证异常";
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(AuthResult result, AuthStrategy strategy, List<FactorResult> factorResults) {
        String key = "auth_count_" + strategy.getCode();
        statistics.put(key, ((Integer) statistics.getOrDefault(key, 0)) + 1);

        String successKey = "auth_success_" + strategy.getCode();
        if (result == AuthResult.SUCCESS) {
            statistics.put(successKey, ((Integer) statistics.getOrDefault(successKey, 0)) + 1);
        }

        // 更新生物特征类型统计
        for (FactorResult factorResult : factorResults) {
            String factorKey = "factor_count_" + factorResult.getBiometricType().getCode();
            statistics.put(factorKey, ((Integer) statistics.getOrDefault(factorKey, 0)) + 1);
        }
    }

    /**
     * 注册生物特征模板
     *
     * @param template 生物特征模板
     * @return 注册结果
     */
    public Boolean enrollTemplate(BiometricTemplate template) {
        log.info("注册生物特征模板：员工ID={}，类型={}", template.getEmployeeId(), template.getBiometricType());

        try {
            template.setTemplateId(System.currentTimeMillis());
            template.setCreateTime(LocalDateTime.now());
            template.setIsActive(true);
            template.setUsageCount(0);

            List<BiometricTemplate> templates = templateStore.computeIfAbsent(
                    template.getEmployeeId(), k -> new ArrayList<>());
            templates.add(template);

            log.info("生物特征模板注册成功：模板ID={}", template.getTemplateId());
            return true;

        } catch (Exception e) {
            log.error("生物特征模板注册失败", e);
            return false;
        }
    }

    /**
     * 删除生物特征模板
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    public Boolean deleteTemplate(Long templateId) {
        log.info("删除生物特征模板：{}", templateId);

        for (Map.Entry<Long, List<BiometricTemplate>> entry : templateStore.entrySet()) {
            List<BiometricTemplate> templates = entry.getValue();
            boolean removed = templates.removeIf(template -> template.getTemplateId().equals(templateId));
            if (removed) {
                log.info("生物特征模板删除成功：{}", templateId);
                return true;
            }
        }

        log.warn("未找到生物特征模板：{}", templateId);
        return false;
    }

    /**
     * 获取员工的所有生物特征模板
     *
     * @param employeeId 员工ID
     * @return 模板列表
     */
    public List<BiometricTemplate> getEmployeeTemplates(Long employeeId) {
        return templateStore.getOrDefault(employeeId, new ArrayList<>());
    }

    /**
     * 获取认证统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getStatistics() {
        return new HashMap<>(statistics);
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        statistics.clear();
        log.info("认证统计信息已重置");
    }

    /**
     * 添加认证策略配置
     *
     * @param strategy 认证策略
     * @param weights  融合权重
     */
    public void addFusionConfig(AuthStrategy strategy, FusionWeights weights) {
        fusionConfigs.put(strategy, weights);
        log.info("添加认证策略配置：{}", strategy.getDescription());
    }

    /**
     * 获取所有支持的生物特征类型
     *
     * @return 生物特征类型列表
     */
    public List<BiometricType> getAllBiometricTypes() {
        return Arrays.asList(BiometricType.values());
    }

    /**
     * 获取所有认证策略
     *
     * @return 认证策略列表
     */
    public List<AuthStrategy> getAllAuthStrategies() {
        return Arrays.asList(AuthStrategy.values());
    }

    /**
     * 模板匹配结果内部类
     */
    @Data
    private static class TemplateMatchResult {
        private BigDecimal matchScore;
        private String templateId;
        private Long employeeId;
    }
}

package net.lab1024.sa.admin.module.smart.biometric.engine;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;
import net.lab1024.sa.admin.module.smart.biometric.constant.SecurityLevelEnum;

/**
 * 生物识别统一引擎接口
 *
 * 作为多模态生物识别系统的核心协调器，统一管理不同类型的生物识别算法，
 * 提供统一的认证接口、策略管理、性能监控和安全控制
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
@Slf4j
public class BiometricRecognitionEngine {

    private final Map<BiometricTypeEnum, BiometricAlgorithm> algorithmRegistry;
    private final BiometricEngineConfig config;
    private final ExecutorService executorService;
    private final BiometricEngineStatistics statistics;
    private final AtomicLong operationCounter;

    // 活体检测服务
    private final LivenessDetectionService livenessDetectionService;

    public BiometricRecognitionEngine(BiometricEngineConfig config) {
        this.config = config;
        this.algorithmRegistry = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(config.getMaxConcurrentOperations());
        this.statistics = new BiometricEngineStatistics();
        this.operationCounter = new AtomicLong(0);

        // 初始化活体检测服务
        this.livenessDetectionService = new LivenessDetectionService(config.getLivenessConfig());

        log.info("生物识别统一引擎初始化完成，配置: {}", config);
    }

    /**
     * 注册生物识别算法
     *
     * @param algorithm 算法实现
     * @return 注册是否成功
     */
    public boolean registerAlgorithm(BiometricAlgorithm algorithm) {
        try {
            BiometricTypeEnum algorithmType = algorithm.getAlgorithmType();

            // 初始化算法
            boolean initialized = algorithm.initialize(config.getAlgorithmConfigs().get(algorithmType));
            if (!initialized) {
                log.error("算法初始化失败: {}", algorithmType);
                return false;
            }

            // 注册到引擎
            algorithmRegistry.put(algorithmType, algorithm);
            statistics.incrementRegisteredAlgorithms();

            log.info("生物识别算法注册成功: {}", algorithmType);
            return true;

        } catch (Exception e) {
            log.error("算法注册失败", e);
            return false;
        }
    }

    /**
     * 注销生物识别算法
     *
     * @param algorithmType 算法类型
     * @return 注销是否成功
     */
    public boolean unregisterAlgorithm(BiometricTypeEnum algorithmType) {
        try {
            BiometricAlgorithm algorithm = algorithmRegistry.remove(algorithmType);
            if (algorithm != null) {
                algorithm.cleanup();
                statistics.incrementUnregisteredAlgorithms();
                log.info("生物识别算法注销成功: {}", algorithmType);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("算法注销失败: {}", algorithmType, e);
            return false;
        }
    }

    /**
     * 执行单模态生物识别认证
     *
     * @param request 认证请求
     * @return 认证结果
     */
    public CompletableFuture<BiometricAuthResult> authenticate(BiometricAuthRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = generateOperationId();
            long startTime = System.currentTimeMillis();

            try {
                log.info("开始生物识别认证: operationId={}, userId={}, biometricType={}",
                        operationId, request.getUserId(), request.getBiometricType());

                // 验证请求参数
                BiometricValidationResult validationResult = validateAuthRequest(request);
                if (!validationResult.isValid()) {
                    return createFailureResult(operationId, request,
                            "请求验证失败: " + validationResult.getErrorMessage());
                }

                // 获取算法实例
                BiometricAlgorithm algorithm = algorithmRegistry.get(request.getBiometricType());
                if (algorithm == null) {
                    return createFailureResult(operationId, request,
                            "不支持的生物识别类型: " + request.getBiometricType());
                }

                // 检查算法状态
                BiometricAlgorithm.AlgorithmStatus algorithmStatus = algorithm.getAlgorithmStatus();
                if (algorithmStatus != BiometricAlgorithm.AlgorithmStatus.READY) {
                    return createFailureResult(operationId, request,
                            "算法不可用，当前状态: " + algorithmStatus);
                }

                // 执行活体检测（如果需要）
                if (requiresLivenessDetection(request)) {
                    LivenessDetectionService.LivenessResult livenessResult = performLivenessCheck(request);
                    if (!livenessResult.isSuccess()) {
                        return createFailureResult(operationId, request,
                                "活体检测失败: " + livenessResult.getMessage());
                    }
                }

                // 执行生物识别认证
                BiometricAlgorithm.BiometricResult algorithmResult = algorithm.authenticate(
                        request.getUserId(),
                        request.getDeviceId(),
                        request.getBiometricData(),
                        request.getTemplateId());

                // 构建认证结果
                BiometricAuthResult authResult = new BiometricAuthResult();
                authResult.setOperationId(operationId);
                authResult.setSuccess(algorithmResult.isSuccess());
                authResult.setConfidence(algorithmResult.getConfidence());
                authResult.setBiometricType(request.getBiometricType());
                authResult.setUserId(request.getUserId());
                authResult.setDeviceId(request.getDeviceId());
                authResult.setTemplateId(algorithmResult.getTemplateId());
                authResult.setProcessingTimeMs(algorithmResult.getProcessingTimeMs());
                authResult.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);
                authResult.setMessage(algorithmResult.getMessage());
                authResult.setExtraData(algorithmResult.getExtraData());

                // 更新统计信息
                if (authResult.isSuccess()) {
                    statistics.incrementSuccessfulAuthentications();
                } else {
                    statistics.incrementFailedAuthentications();
                }
                statistics.addProcessingTime(authResult.getTotalProcessingTimeMs());

                log.info("生物识别认证完成: operationId={}, success={}, confidence={}, processingTime={}ms",
                        operationId, authResult.isSuccess(), authResult.getConfidence(),
                        authResult.getTotalProcessingTimeMs());

                return authResult;

            } catch (Exception e) {
                log.error("生物识别认证异常: operationId={}", operationId, e);
                statistics.incrementFailedAuthentications();
                return createFailureResult(operationId, request, "认证执行异常: " + e.getMessage());
            }
        }, executorService);
    }

    /**
     * 执行多模态生物识别认证
     *
     * @param request 多模态认证请求
     * @return 认证结果
     */
    public CompletableFuture<MultimodalAuthResult> authenticateMultimodal(MultimodalAuthRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = generateOperationId();
            long startTime = System.currentTimeMillis();

            try {
                log.info("开始多模态生物识别认证: operationId={}, userId={}, biometricTypes={}",
                        operationId, request.getUserId(), request.getBiometricTypes());

                // 验证请求参数
                MultimodalValidationResult validationResult = validateMultimodalRequest(request);
                if (!validationResult.isValid()) {
                    return createMultimodalFailureResult(operationId, request,
                            "请求验证失败: " + validationResult.getErrorMessage());
                }

                // 检查安全级别要求
                SecurityLevelEnum requiredLevel = request.getSecurityLevel();
                if (!meetsSecurityLevelRequirements(request, requiredLevel)) {
                    return createMultimodalFailureResult(operationId, request,
                            "不满足安全级别要求: " + requiredLevel);
                }

                // 并行执行多个生物识别认证
                List<CompletableFuture<BiometricAuthResult>> futures = new ArrayList<>();
                for (BiometricAuthRequest singleRequest : request.getAuthRequests()) {
                    CompletableFuture<BiometricAuthResult> future = authenticate(singleRequest);
                    futures.add(future);
                }

                // 等待所有认证完成
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));

                allFutures.join(); // 等待所有任务完成

                // 收集认证结果
                List<BiometricAuthResult> results = new ArrayList<>();
                for (CompletableFuture<BiometricAuthResult> future : futures) {
                    results.add(future.get());
                }

                // 应用融合策略
                MultimodalFusionResult fusionResult = applyFusionStrategy(results, request.getFusionStrategy());

                // 构建多模态认证结果
                MultimodalAuthResult authResult = new MultimodalAuthResult();
                authResult.setOperationId(operationId);
                authResult.setUserId(request.getUserId());
                authResult.setDeviceId(request.getDeviceId());
                authResult.setSecurityLevel(requiredLevel);
                authResult.setBiometricTypes(new ArrayList<>(request.getBiometricTypes()));
                authResult.setIndividualResults(results);
                authResult.setOverallSuccess(fusionResult.isSuccess());
                authResult.setOverallConfidence(fusionResult.getConfidence());
                authResult.setFusionStrategy(request.getFusionStrategy());
                authResult.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);
                authResult.setMessage(fusionResult.getMessage());
                authResult.setDecisionDetails(fusionResult.getDetails());

                // 更新统计信息
                if (authResult.isOverallSuccess()) {
                    statistics.incrementSuccessfulMultimodalAuthentications();
                } else {
                    statistics.incrementFailedMultimodalAuthentications();
                }

                log.info("多模态生物识别认证完成: operationId={}, success={}, overallConfidence={}, processingTime={}ms",
                        operationId, authResult.isOverallSuccess(), authResult.getOverallConfidence(),
                        authResult.getTotalProcessingTimeMs());

                return authResult;

            } catch (Exception e) {
                log.error("多模态生物识别认证异常: operationId={}", operationId, e);
                statistics.incrementFailedMultimodalAuthentications();
                return createMultimodalFailureResult(operationId, request, "认证执行异常: " + e.getMessage());
            }
        }, executorService);
    }

    /**
     * 注册生物特征模板
     *
     * @param request 注册请求
     * @return 注册结果
     */
    public CompletableFuture<TemplateRegistrationResult> registerTemplate(TemplateRegistrationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = generateOperationId();
            long startTime = System.currentTimeMillis();

            try {
                log.info("开始生物特征模板注册: operationId={}, userId={}, biometricType={}",
                        operationId, request.getUserId(), request.getBiometricType());

                // 验证请求参数
                if (!validateTemplateRegistrationRequest(request)) {
                    return createTemplateFailureResult(operationId, request, "请求参数验证失败");
                }

                // 获取算法实例
                BiometricAlgorithm algorithm = algorithmRegistry.get(request.getBiometricType());
                if (algorithm == null) {
                    return createTemplateFailureResult(operationId, request,
                            "不支持的生物识别类型: " + request.getBiometricType());
                }

                // 执行活体检测（如果需要）
                if (request.isRequireLivenessCheck()) {
                    // TODO: 实现模板注册时的活体检测
                }

                // 注册模板
                BiometricAlgorithm.BiometricResult algorithmResult = algorithm.registerTemplate(
                        request.getUserId(),
                        request.getDeviceId(),
                        request.getBiometricData());

                // 构建注册结果
                TemplateRegistrationResult registrationResult = new TemplateRegistrationResult();
                registrationResult.setOperationId(operationId);
                registrationResult.setSuccess(algorithmResult.isSuccess());
                registrationResult.setBiometricType(request.getBiometricType());
                registrationResult.setUserId(request.getUserId());
                registrationResult.setDeviceId(request.getDeviceId());
                registrationResult.setTemplateId(algorithmResult.getTemplateId());
                registrationResult.setConfidence(algorithmResult.getConfidence());
                registrationResult.setProcessingTimeMs(algorithmResult.getProcessingTimeMs());
                registrationResult.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);
                registrationResult.setMessage(algorithmResult.getMessage());
                registrationResult.setExtraData(algorithmResult.getExtraData());

                // 更新统计信息
                if (registrationResult.isSuccess()) {
                    statistics.incrementSuccessfulRegistrations();
                } else {
                    statistics.incrementFailedRegistrations();
                }

                log.info("生物特征模板注册完成: operationId={}, success={}, templateId={}, processingTime={}ms",
                        operationId, registrationResult.isSuccess(), registrationResult.getTemplateId(),
                        registrationResult.getTotalProcessingTimeMs());

                return registrationResult;

            } catch (Exception e) {
                log.error("生物特征模板注册异常: operationId={}", operationId, e);
                statistics.incrementFailedRegistrations();
                return createTemplateFailureResult(operationId, request, "注册执行异常: " + e.getMessage());
            }
        }, executorService);
    }

    /**
     * 删除生物特征模板
     *
     * @param request 删除请求
     * @return 删除结果
     */
    public CompletableFuture<TemplateDeletionResult> deleteTemplate(TemplateDeletionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String operationId = generateOperationId();
            long startTime = System.currentTimeMillis();

            try {
                log.info("开始删除生物特征模板: operationId={}, templateId={}",
                        operationId, request.getTemplateId());

                // 获取算法实例
                BiometricAlgorithm algorithm = algorithmRegistry.get(request.getBiometricType());
                if (algorithm == null) {
                    return createDeletionFailureResult(operationId, request,
                            "不支持的生物识别类型: " + request.getBiometricType());
                }

                // 删除模板
                BiometricAlgorithm.BiometricResult algorithmResult = algorithm.deleteTemplate(request.getTemplateId());

                // 构建删除结果
                TemplateDeletionResult deletionResult = new TemplateDeletionResult();
                deletionResult.setOperationId(operationId);
                deletionResult.setSuccess(algorithmResult.isSuccess());
                deletionResult.setBiometricType(request.getBiometricType());
                deletionResult.setTemplateId(request.getTemplateId());
                deletionResult.setProcessingTimeMs(algorithmResult.getProcessingTimeMs());
                deletionResult.setTotalProcessingTimeMs(System.currentTimeMillis() - startTime);
                deletionResult.setMessage(algorithmResult.getMessage());
                deletionResult.setExtraData(algorithmResult.getExtraData());

                // 更新统计信息
                if (deletionResult.isSuccess()) {
                    statistics.incrementSuccessfulDeletions();
                } else {
                    statistics.incrementFailedDeletions();
                }

                log.info("生物特征模板删除完成: operationId={}, success={}, processingTime={}ms",
                        operationId, deletionResult.isSuccess(), deletionResult.getTotalProcessingTimeMs());

                return deletionResult;

            } catch (Exception e) {
                log.error("生物特征模板删除异常: operationId={}", operationId, e);
                statistics.incrementFailedDeletions();
                return createDeletionFailureResult(operationId, request, "删除执行异常: " + e.getMessage());
            }
        }, executorService);
    }

    /**
     * 获取引擎状态
     *
     * @return 引擎状态
     */
    public BiometricEngineStatusEntity getEngineStatus() {
        BiometricEngineStatusEntity status = new BiometricEngineStatusEntity();
        status.setEngineId(config.getEngineId());
        status.setStartTime(config.getStartTime());
        status.setRegisteredAlgorithms(new ArrayList<>(algorithmRegistry.keySet()));

        Map<BiometricTypeEnum, BiometricAlgorithm.AlgorithmStatus> algorithmStatuses = new HashMap<>();
        for (Map.Entry<BiometricTypeEnum, BiometricAlgorithm> entry : algorithmRegistry.entrySet()) {
            algorithmStatuses.put(entry.getKey(), entry.getValue().getAlgorithmStatus());
        }
        status.setAlgorithmStatuses(algorithmStatuses);

        status.setStatistics(statistics);
        status.setSystemResourceUsage(getSystemResourceUsage());

        return status;
    }

    /**
     * 获取算法性能指标
     *
     * @param algorithmType 算法类型
     * @return 性能指标
     */
    public BiometricAlgorithm.PerformanceMetrics getAlgorithmPerformanceMetrics(BiometricTypeEnum algorithmType) {
        BiometricAlgorithm algorithm = algorithmRegistry.get(algorithmType);
        if (algorithm != null) {
            return algorithm.getPerformanceMetrics();
        }
        return null;
    }

    // ================================
    // 私有辅助方法
    // ================================

    private String generateOperationId() {
        return "BIO_OP_" + System.currentTimeMillis() + "_" + operationCounter.incrementAndGet();
    }

    private BiometricValidationResult validateAuthRequest(BiometricAuthRequest request) {
        if (request == null) {
            return BiometricValidationResult.failure("请求对象为空");
        }
        if (request.getUserId() == null) {
            return BiometricValidationResult.failure("用户ID不能为空");
        }
        if (request.getBiometricType() == null) {
            return BiometricValidationResult.failure("生物识别类型不能为空");
        }
        if (request.getBiometricData() == null || request.getBiometricData().length == 0) {
            return BiometricValidationResult.failure("生物特征数据不能为空");
        }
        return BiometricValidationResult.success();
    }

    private MultimodalValidationResult validateMultimodalRequest(MultimodalAuthRequest request) {
        if (request == null) {
            return MultimodalValidationResult.failure("请求对象为空");
        }
        if (request.getUserId() == null) {
            return MultimodalValidationResult.failure("用户ID不能为空");
        }
        if (request.getBiometricTypes() == null || request.getBiometricTypes().isEmpty()) {
            return MultimodalValidationResult.failure("生物识别类型列表不能为空");
        }
        if (request.getAuthRequests() == null || request.getAuthRequests().isEmpty()) {
            return MultimodalValidationResult.failure("认证请求列表不能为空");
        }
        return MultimodalValidationResult.success();
    }

    private boolean validateTemplateRegistrationRequest(TemplateRegistrationRequest request) {
        return request != null &&
                request.getUserId() != null &&
                request.getBiometricType() != null &&
                request.getBiometricData() != null &&
                request.getBiometricData().length > 0;
    }

    private boolean requiresLivenessDetection(BiometricAuthRequest request) {
        // 根据生物识别类型和安全级别判断是否需要活体检测
        if (request.getBiometricType() == BiometricTypeEnum.FACE) {
            return request.getSecurityLevel() != null &&
                    request.getSecurityLevel().getLevel() >= SecurityLevelEnum.MEDIUM.getLevel();
        }
        return false;
    }

    private LivenessDetectionService.LivenessResult performLivenessCheck(BiometricAuthRequest request) {
        try {
            log.debug("开始活体检测: userId={}, deviceId={}, biometricType={}",
                    request.getUserId(), request.getDeviceId(), request.getBiometricType());

            // 1. 验证生物识别数据
            if (request.getBiometricData() == null || request.getBiometricData().length == 0) {
                log.warn("活体检测失败: 生物识别数据为空");
                LivenessDetectionService.LivenessResult result = new LivenessDetectionService.LivenessResult();
                result.setSuccess(false);
                result.setOverallSuccess(false);
                result.setMessage("生物识别数据为空");
                return result;
            }

            // 2. 将byte[]转换为BufferedImage（仅人脸识别需要）
            if (request.getBiometricType() != BiometricTypeEnum.FACE) {
                // 非人脸识别类型，暂时跳过活体检测
                log.debug("非人脸识别类型，跳过活体检测: type={}", request.getBiometricType());
                LivenessDetectionService.LivenessResult result = new LivenessDetectionService.LivenessResult();
                result.setSuccess(true);
                result.setOverallSuccess(true);
                result.setConfidence(1.0);
                result.setMessage("非人脸识别类型，无需活体检测");
                return result;
            }

            // 3. 创建活体检测会话
            String sessionId = livenessDetectionService.createSession(
                    request.getUserId(),
                    request.getDeviceId(),
                    LivenessDetectionService.LivenessChallengeType.BLINK).getSessionId();

            // 4. 将byte[]转换为BufferedImage
            BufferedImage frame = convertBytesToImage(request.getBiometricData());
            if (frame == null) {
                log.warn("活体检测失败: 无法将字节数组转换为图像");
                LivenessDetectionService.LivenessResult result = new LivenessDetectionService.LivenessResult();
                result.setSuccess(false);
                result.setOverallSuccess(false);
                result.setMessage("无法将字节数组转换为图像");
                return result;
            }

            // 5. 执行活体检测
            LivenessDetectionService.LivenessResult result = livenessDetectionService.performLivenessCheck(sessionId,
                    frame);

            log.debug("活体检测完成: sessionId={}, success={}, confidence={}",
                    sessionId, result.isSuccess(), result.getConfidence());

            return result;

        } catch (Exception e) {
            log.error("活体检测异常: userId={}", request.getUserId(), e);
            LivenessDetectionService.LivenessResult result = new LivenessDetectionService.LivenessResult();
            result.setSuccess(false);
            result.setOverallSuccess(false);
            result.setMessage("活体检测异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 将字节数组转换为BufferedImage
     */
    private BufferedImage convertBytesToImage(byte[] imageBytes) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
            // ImageIO reading - javax.imageio is part of JDK, not Jakarta EE
            return javax.imageio.ImageIO.read(bais);
        } catch (Exception e) {
            log.error("转换图像失败", e);
            return null;
        }
    }

    private boolean meetsSecurityLevelRequirements(MultimodalAuthRequest request, SecurityLevelEnum requiredLevel) {
        int requiredTypes = requiredLevel.getRequiredBiometricTypes();
        return request.getBiometricTypes().size() >= requiredTypes;
    }

    private MultimodalFusionResult applyFusionStrategy(List<BiometricAuthResult> results, FusionStrategy strategy) {
        switch (strategy) {
            case WEIGHTED_AVERAGE:
                return applyWeightedAverageFusion(results);
            case MAJORITY_VOTING:
                return applyMajorityVotingFusion(results);
            case HIGHEST_CONFIDENCE:
                return applyHighestConfidenceFusion(results);
            case CASCADE:
                return applyCascadeFusion(results);
            default:
                return applyWeightedAverageFusion(results);
        }
    }

    private MultimodalFusionResult applyWeightedAverageFusion(List<BiometricAuthResult> results) {
        double totalWeight = 0.0;
        double weightedSum = 0.0;
        boolean allSuccessful = true;

        for (BiometricAuthResult result : results) {
            double weight = getBiometricTypeWeight(result.getBiometricType());
            totalWeight += weight;
            weightedSum += result.getConfidence() * weight;

            if (!result.isSuccess()) {
                allSuccessful = false;
            }
        }

        double overallConfidence = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        boolean overallSuccess = allSuccessful && overallConfidence >= config.getDefaultConfidenceThreshold();

        MultimodalFusionResult fusionResult = new MultimodalFusionResult();
        fusionResult.setSuccess(overallSuccess);
        fusionResult.setConfidence(overallConfidence);
        fusionResult.setMessage("加权平均融合完成");
        fusionResult.setDetails(
                Map.of("fusionMethod", "WEIGHTED_AVERAGE", "weightedSum", weightedSum, "totalWeight", totalWeight));

        return fusionResult;
    }

    private MultimodalFusionResult applyMajorityVotingFusion(List<BiometricAuthResult> results) {
        long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
        long totalCount = results.size();
        boolean overallSuccess = successCount > totalCount / 2;

        double averageConfidence = results.stream()
                .mapToDouble(BiometricAuthResult::getConfidence)
                .average()
                .orElse(0.0);

        MultimodalFusionResult fusionResult = new MultimodalFusionResult();
        fusionResult.setSuccess(overallSuccess);
        fusionResult.setConfidence(averageConfidence);
        fusionResult.setMessage("多数投票融合完成");
        fusionResult.setDetails(
                Map.of("fusionMethod", "MAJORITY_VOTING", "successCount", successCount, "totalCount", totalCount));

        return fusionResult;
    }

    private MultimodalFusionResult applyHighestConfidenceFusion(List<BiometricAuthResult> results) {
        BiometricAuthResult bestResult = results.stream()
                .max(Comparator.comparing(BiometricAuthResult::getConfidence))
                .orElse(null);

        if (bestResult != null) {
            MultimodalFusionResult fusionResult = new MultimodalFusionResult();
            fusionResult.setSuccess(bestResult.isSuccess());
            fusionResult.setConfidence(bestResult.getConfidence());
            fusionResult.setMessage("最高置信度融合完成");
            fusionResult.setDetails(
                    Map.of("fusionMethod", "HIGHEST_CONFIDENCE", "bestType", bestResult.getBiometricType()));
            return fusionResult;
        }

        return new MultimodalFusionResult();
    }

    private MultimodalFusionResult applyCascadeFusion(List<BiometricAuthResult> results) {
        // 级联融合：按优先级依次尝试，直到成功或全部失败
        for (BiometricAuthResult result : results) {
            if (result.isSuccess() && result.getConfidence() >= config.getDefaultConfidenceThreshold()) {
                MultimodalFusionResult fusionResult = new MultimodalFusionResult();
                fusionResult.setSuccess(true);
                fusionResult.setConfidence(result.getConfidence());
                fusionResult.setMessage("级联融合成功");
                fusionResult.setDetails(Map.of("fusionMethod", "CASCADE", "successfulType", result.getBiometricType()));
                return fusionResult;
            }
        }

        MultimodalFusionResult fusionResult = new MultimodalFusionResult();
        fusionResult.setSuccess(false);
        fusionResult.setConfidence(0.0);
        fusionResult.setMessage("级联融合失败");
        fusionResult.setDetails(Map.of("fusionMethod", "CASCADE", "result", "ALL_FAILED"));

        return fusionResult;
    }

    private double getBiometricTypeWeight(BiometricTypeEnum type) {
        // 根据生物识别类型分配权重
        switch (type) {
            case FACE:
                return 0.8;
            case FINGERPRINT:
                return 0.9;
            case IRIS:
                return 1.0;
            case PALMPRINT:
                return 0.85;
            default:
                return 0.7;
        }
    }

    private BiometricAuthResult createFailureResult(String operationId, BiometricAuthRequest request, String message) {
        BiometricAuthResult result = new BiometricAuthResult();
        result.setOperationId(operationId);
        result.setSuccess(false);
        result.setUserId(request.getUserId());
        result.setBiometricType(request.getBiometricType());
        result.setMessage(message);
        result.setTotalProcessingTimeMs(0);
        return result;
    }

    private MultimodalAuthResult createMultimodalFailureResult(String operationId, MultimodalAuthRequest request,
            String message) {
        MultimodalAuthResult result = new MultimodalAuthResult();
        result.setOperationId(operationId);
        result.setOverallSuccess(false);
        result.setUserId(request.getUserId());
        result.setBiometricTypes(new ArrayList<>(request.getBiometricTypes()));
        result.setMessage(message);
        result.setTotalProcessingTimeMs(0);
        return result;
    }

    private TemplateRegistrationResult createTemplateFailureResult(String operationId,
            TemplateRegistrationRequest request, String message) {
        TemplateRegistrationResult result = new TemplateRegistrationResult();
        result.setOperationId(operationId);
        result.setSuccess(false);
        result.setUserId(request.getUserId());
        result.setBiometricType(request.getBiometricType());
        result.setMessage(message);
        result.setTotalProcessingTimeMs(0);
        return result;
    }

    private TemplateDeletionResult createDeletionFailureResult(String operationId, TemplateDeletionRequest request,
            String message) {
        TemplateDeletionResult result = new TemplateDeletionResult();
        result.setOperationId(operationId);
        result.setSuccess(false);
        result.setBiometricType(request.getBiometricType());
        result.setTemplateId(request.getTemplateId());
        result.setMessage(message);
        result.setTotalProcessingTimeMs(0);
        return result;
    }

    private Map<String, Object> getSystemResourceUsage() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> usage = new HashMap<>();
        usage.put("totalMemory", runtime.totalMemory());
        usage.put("freeMemory", runtime.freeMemory());
        usage.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        usage.put("maxMemory", runtime.maxMemory());
        usage.put("activeThreads", Thread.activeCount());
        return usage;
    }

    /**
     * 关闭引擎
     */
    public void shutdown() {
        try {
            log.info("开始关闭生物识别引擎...");

            // 关闭所有算法
            for (BiometricAlgorithm algorithm : algorithmRegistry.values()) {
                try {
                    algorithm.cleanup();
                } catch (Exception e) {
                    log.error("算法清理失败", e);
                }
            }
            algorithmRegistry.clear();

            // 关闭活体检测服务
            livenessDetectionService.destroy();

            // 关闭线程池
            executorService.shutdown();

            log.info("生物识别引擎已关闭");

        } catch (Exception e) {
            log.error("关闭生物识别引擎时发生异常", e);
        }
    }

    // ================================
    // 数据类定义
    // ================================

    @Data
    public static class BiometricEngineConfig {
        private String engineId = "BIO_ENGINE_001";
        private long startTime = System.currentTimeMillis();
        private int maxConcurrentOperations = 10;
        private double defaultConfidenceThreshold = 0.8;
        private Map<BiometricTypeEnum, Map<String, Object>> algorithmConfigs = new HashMap<>();
        private LivenessDetectionService.LivenessConfig livenessConfig = new LivenessDetectionService.LivenessConfig();
    }

    @Data
    public static class BiometricAuthRequest {
        private Long userId;
        private String deviceId;
        private BiometricTypeEnum biometricType;
        private byte[] biometricData;
        private String templateId;
        private SecurityLevelEnum securityLevel;
        private boolean requireLivenessCheck = false;
    }

    @Data
    public static class MultimodalAuthRequest {
        private Long userId;
        private String deviceId;
        private List<BiometricTypeEnum> biometricTypes;
        private List<BiometricAuthRequest> authRequests;
        private SecurityLevelEnum securityLevel;
        private FusionStrategy fusionStrategy = FusionStrategy.WEIGHTED_AVERAGE;
    }

    @Data
    public static class TemplateRegistrationRequest {
        private Long userId;
        private String deviceId;
        private BiometricTypeEnum biometricType;
        private byte[] biometricData;
        private boolean requireLivenessCheck = true;
    }

    @Data
    public static class TemplateDeletionRequest {
        private String templateId;
        private BiometricTypeEnum biometricType;
    }

    @Data
    public static class BiometricAuthResult {
        private String operationId;
        private boolean success;
        private double confidence;
        private BiometricTypeEnum biometricType;
        private Long userId;
        private String deviceId;
        private String templateId;
        private long processingTimeMs;
        private long totalProcessingTimeMs;
        private String message;
        private Map<String, Object> extraData;
    }

    @Data
    public static class MultimodalAuthResult {
        private String operationId;
        private Long userId;
        private String deviceId;
        private SecurityLevelEnum securityLevel;
        private List<BiometricTypeEnum> biometricTypes;
        private List<BiometricAuthResult> individualResults;
        private boolean overallSuccess;
        private double overallConfidence;
        private FusionStrategy fusionStrategy;
        private long totalProcessingTimeMs;
        private String message;
        private Map<String, Object> decisionDetails;
    }

    @Data
    public static class TemplateRegistrationResult {
        private String operationId;
        private boolean success;
        private BiometricTypeEnum biometricType;
        private Long userId;
        private String deviceId;
        private String templateId;
        private double confidence;
        private long processingTimeMs;
        private long totalProcessingTimeMs;
        private String message;
        private Map<String, Object> extraData;
    }

    @Data
    public static class TemplateDeletionResult {
        private String operationId;
        private boolean success;
        private BiometricTypeEnum biometricType;
        private String templateId;
        private long processingTimeMs;
        private long totalProcessingTimeMs;
        private String message;
        private Map<String, Object> extraData;
    }

    @Data
    public static class BiometricEngineStatusEntity {
        private String engineId;
        private long startTime;
        private List<BiometricTypeEnum> registeredAlgorithms;
        private Map<BiometricTypeEnum, BiometricAlgorithm.AlgorithmStatus> algorithmStatuses;
        private BiometricEngineStatistics statistics;
        private Map<String, Object> systemResourceUsage;
    }

    @Data
    public static class BiometricEngineStatistics {
        private long registeredAlgorithms;
        private long unregisteredAlgorithms;
        private long successfulAuthentications;
        private long failedAuthentications;
        private long successfulMultimodalAuthentications;
        private long failedMultimodalAuthentications;
        private long successfulRegistrations;
        private long failedRegistrations;
        private long successfulDeletions;
        private long failedDeletions;

        private long totalProcessingTimeMs;
        private long averageProcessingTimeMs;

        public void incrementRegisteredAlgorithms() {
            registeredAlgorithms++;
        }

        public void incrementUnregisteredAlgorithms() {
            unregisteredAlgorithms++;
        }

        public void incrementSuccessfulAuthentications() {
            successfulAuthentications++;
        }

        public void incrementFailedAuthentications() {
            failedAuthentications++;
        }

        public void incrementSuccessfulMultimodalAuthentications() {
            successfulMultimodalAuthentications++;
        }

        public void incrementFailedMultimodalAuthentications() {
            failedMultimodalAuthentications++;
        }

        public void incrementSuccessfulRegistrations() {
            successfulRegistrations++;
        }

        public void incrementFailedRegistrations() {
            failedRegistrations++;
        }

        public void incrementSuccessfulDeletions() {
            successfulDeletions++;
        }

        public void incrementFailedDeletions() {
            failedDeletions++;
        }

        public void addProcessingTime(long processingTimeMs) {
            totalProcessingTimeMs += processingTimeMs;
            long totalOperations = successfulAuthentications + failedAuthentications;
            if (totalOperations > 0) {
                averageProcessingTimeMs = totalProcessingTimeMs / totalOperations;
            }
        }
    }

    @Data
    public static class BiometricValidationResult {
        private boolean valid;
        private String errorMessage;

        public static BiometricValidationResult success() {
            BiometricValidationResult result = new BiometricValidationResult();
            result.setValid(true);
            return result;
        }

        public static BiometricValidationResult failure(String errorMessage) {
            BiometricValidationResult result = new BiometricValidationResult();
            result.setValid(false);
            result.setErrorMessage(errorMessage);
            return result;
        }
    }

    @Data
    public static class MultimodalValidationResult {
        private boolean valid;
        private String errorMessage;

        public static MultimodalValidationResult success() {
            MultimodalValidationResult result = new MultimodalValidationResult();
            result.setValid(true);
            return result;
        }

        public static MultimodalValidationResult failure(String errorMessage) {
            MultimodalValidationResult result = new MultimodalValidationResult();
            result.setValid(false);
            result.setErrorMessage(errorMessage);
            return result;
        }
    }

    @Data
    public static class MultimodalFusionResult {
        private boolean success;
        private double confidence;
        private String message;
        private Map<String, Object> details;
    }

    public enum FusionStrategy {
        WEIGHTED_AVERAGE, // 加权平均
        MAJORITY_VOTING, // 多数投票
        HIGHEST_CONFIDENCE, // 最高置信度
        CASCADE // 级联
    }
}

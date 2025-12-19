package net.lab1024.sa.consume.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.dto.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.domain.vo.*;
import net.lab1024.sa.consume.edge.service.MobileEdgeConsumeService;
import net.lab1024.sa.consume.service.BiometricConsumeService;
import net.lab1024.sa.consume.edge.dto.*;
import net.lab1024.sa.consume.edge.vo.*;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 增强移动端消费控制器
 * <p>
 * 基于边缘计算和生物识别的移动端消费增强功能：
 * - 生物识别消费（人脸、指纹、虹膜）
 * - 边缘设备支付处理
 * - 智能推荐与个性化服务
 * - 实时离线模式支持
 * - 移动设备安全管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/v2/consume")
@Tag(name = "增强移动端消费", description = "基于边缘计算的增强移动端消费功能")
@PermissionCheck(value = "CONSUME", description = "增强移动端消费管理")
public class EnhancedMobileConsumeController {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private MobileEdgeConsumeService mobileEdgeConsumeService;

    @Resource
    private BiometricConsumeService biometricConsumeService;

    // ==================== 生物识别消费 ====================

    /**
     * 生物识别快捷消费
     * <p>
     * 使用人脸识别、指纹等生物识别进行消费支付
     * 支持多模态生物融合验证
     * </p>
     *
     * @param request 生物识别消费请求
     * @return 消费结果
     */
    @Observed(name = "enhancedMobileConsume.biometricConsume", contextualName = "biometric-mobile-consume")
    @PostMapping("/biometric/consume")
    @Operation(
            summary = "生物识别消费",
            description = "使用生物识别进行消费支付",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "消费成功"
                    )
            }
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileBiometricConsumeResultVO> performBiometricConsume(
            @Valid @RequestBody BiometricConsumeRequestDTO request) {

        log.info("[增强移动消费] 生物识别消费，userId={}, amount={}, biometricType={}",
                request.getUserId(), request.getAmount(), request.getBiometricType());

        try {
            // 1. 生物识别验证
            CompletableFuture<BiometricConsumeAuthResultVO> authResult = biometricConsumeService
                .performBiometricAuthentication(request);

            BiometricConsumeAuthResultVO auth = authResult.get();
            if (!auth.isAuthenticated()) {
                return ResponseDTO.error("BIOMETRIC_AUTH_FAILED", "生物识别验证失败：" + auth.getErrorMessage());
            }

            // 2. 构建消费请求
            ConsumeRequestDTO consumeRequest = buildConsumeRequest(request, auth.getUserId());

            // 3. 执行消费
            ResponseDTO<ConsumeTransactionResultVO> consumeResult = consumeService.consume(consumeRequest);

            if (!consumeResult.getOk()) {
                return ResponseDTO.error(consumeResult.getCode(), consumeResult.getMessage());
            }

            // 4. 转换为移动端结果
            MobileBiometricConsumeResultVO result = convertToBiometricResult(consumeResult.getData(), auth);

            log.info("[增强移动消费] 生物识别消费成功，userId={}, orderId={}, confidence={}",
                    request.getUserId(), result.getOrderId(), result.getBiometricConfidence());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强移动消费] 生物识别消费异常，userId={}, error={}",
                    request.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("BIOMETRIC_CONSUME_ERROR", "生物识别消费异常：" + e.getMessage());
        }
    }

    /**
     * 人脸识别扫码消费
     * <p>
     * 使用人脸识别技术进行扫码支付
     * 提供无接触式消费体验
     * </p>
     *
     * @param request 人脸识别请求
     * @return 消费结果
     */
    @Observed(name = "enhancedMobileConsume.faceScanConsume", contextualName = "face-scan-consume")
    @PostMapping("/face/scan-consume")
    @Operation(
            summary = "人脸识别扫码消费",
            description = "使用人脸识别进行扫码支付"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileFaceScanConsumeResultVO> performFaceScanConsume(
            @Valid @RequestBody FaceScanConsumeRequestDTO request) {

        log.info("[增强移动消费] 人脸识别扫码消费，deviceId={}, qrCode={}",
                request.getDeviceId(), request.getQrCode());

        try {
            // 1. 边缘设备人脸识别
            CompletableFuture<MobileFaceScanAuthResultVO> faceResult = mobileEdgeConsumeService
                .performFaceScanAuthentication(request);

            MobileFaceScanAuthResultVO faceAuth = faceResult.get();
            if (!faceAuth.isAuthenticated()) {
                return ResponseDTO.error("FACE_SCAN_AUTH_FAILED", "人脸识别验证失败：" + faceAuth.getErrorMessage());
            }

            // 2. 扫码信息解析
            CompletableFuture<QrCodeParseResultVO> qrResult = mobileEdgeConsumeService
                .parseQrCode(request.getQrCode());

            // 3. 执行消费
            ConsumeRequestDTO consumeRequest = buildConsumeRequestFromFaceScan(request, faceAuth.getUserId());
            ResponseDTO<ConsumeTransactionResultVO> consumeResult = consumeService.consume(consumeRequest);

            if (!consumeResult.getOk()) {
                return ResponseDTO.error(consumeResult.getCode(), consumeResult.getMessage());
            }

            // 4. 构建结果
            MobileFaceScanConsumeResultVO result = convertToFaceScanResult(consumeResult.getData(), faceAuth, qrResult.get());

            log.info("[增强移动消费] 人脸识别扫码消费成功，userId={}, orderId={}",
                    faceAuth.getUserId(), result.getOrderId());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强移动消费] 人脸识别扫码消费异常，deviceId={}, error={}",
                    request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("FACE_SCAN_CONSUME_ERROR", "人脸识别扫码消费异常：" + e.getMessage());
        }
    }

    // ==================== 边缘设备支付 ====================

    /**
     * 边缘设备支付处理
     * <p>
     * 直接在边缘设备上进行支付处理
     * 提供毫秒级支付响应
     * </p>
     *
     * @param request 边缘支付请求
     * @return 支付结果
     */
    @Observed(name = "enhancedMobileConsume.edgePayment", contextualName = "edge-device-payment")
    @PostMapping("/edge/payment")
    @Operation(
            summary = "边缘设备支付",
            description = "在边缘设备上进行支付处理"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileEdgePaymentResultVO> performEdgePayment(
            @Valid @RequestBody MobileEdgePaymentRequestDTO request) {

        log.info("[增强移动消费] 边缘设备支付，edgeDeviceId={}, amount={}",
                request.getEdgeDeviceId(), request.getAmount());

        try {
            // 1. 边缘设备支付处理
            CompletableFuture<MobileEdgePaymentResultVO> edgeResult = mobileEdgeConsumeService
                .performEdgePayment(request);

            MobileEdgePaymentResultVO result = edgeResult.get();

            // 2. 同步到云端
            if (result.isSuccess() && request.isSyncToCloud()) {
                CompletableFuture<Void> syncResult = mobileEdgeConsumeService
                    .syncPaymentToCloud(result.getPaymentId());
                syncResult.get();
            }

            log.info("[增强移动消费] 边缘设备支付成功，paymentId={}, processedIn={}ms",
                    result.getPaymentId(), result.getProcessingTime());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强移动消费] 边缘设备支付异常，edgeDeviceId={}, error={}",
                    request.getEdgeDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("EDGE_PAYMENT_ERROR", "边缘设备支付异常：" + e.getMessage());
        }
    }

    /**
     * 批量边缘支付
     * <p>
     * 支持多笔交易的批量边缘处理
     * 提高支付效率
     * </p>
     *
     * @param request 批量支付请求
     * @return 批量支付结果
     */
    @Observed(name = "enhancedMobileConsume.batchEdgePayment", contextualName = "batch-edge-payment")
    @PostMapping("/edge/batch-payment")
    @Operation(
            summary = "批量边缘支付",
            description = "支持多笔交易的批量边缘处理"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_MANAGER", description = "移动端消费管理")
    public ResponseDTO<Map<String, MobileEdgePaymentResultVO>> performBatchEdgePayment(
            @Valid @RequestBody MobileBatchEdgePaymentRequestDTO request) {

        log.info("[增强移动消费] 批量边缘支付，交易数量={}", request.getTransactions().size());

        try {
            Map<String, Future<MobileEdgePaymentResultVO>> results = mobileEdgeConsumeService
                .performBatchEdgePayment(request);

            // 等待所有结果
            Map<String, MobileEdgePaymentResultVO> finalResults = new java.util.concurrent.ConcurrentHashMap<>();
            for (Map.Entry<String, Future<MobileEdgePaymentResultVO>> entry : results.entrySet()) {
                finalResults.put(entry.getKey(), entry.getValue().get());
            }

            log.info("[增强移动消费] 批量边缘支付完成，总数量={}, 成功数量={}",
                finalResults.size(),
                finalResults.values().stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());

            return ResponseDTO.ok(finalResults);

        } catch (Exception e) {
            log.error("[增强移动消费] 批量边缘支付异常，数量={}, error={}",
                    request.getTransactions().size(), e.getMessage(), e);
            return ResponseDTO.error("BATCH_EDGE_PAYMENT_ERROR", "批量边缘支付异常：" + e.getMessage());
        }
    }

    // ==================== 离线模式支持 ====================

    /**
     * 离线数据同步
     * <p>
     * 同步离线模式所需的账户和权限数据
     * 确保离线时也能正常消费
     * </p>
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @Observed(name = "enhancedMobileConsume.offlineSync", contextualName = "offline-data-sync")
    @PostMapping("/offline/sync")
    @Operation(
            summary = "离线数据同步",
            description = "同步离线模式所需的数据"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileOfflineSyncResultVO> syncOfflineData(
            @Valid @RequestBody MobileOfflineSyncRequestDTO request) {

        log.info("[增强移动消费] 离线数据同步，userId={}, lastSyncTime={}",
                request.getUserId(), request.getLastSyncTime());

        try {
            MobileOfflineSyncResultVO result = mobileEdgeConsumeService
                .syncOfflineData(request);

            log.info("[增强移动消费] 离线数据同步完成，userId={}, syncedCount={}, lastSyncTime={}",
                    result.getUserId(), result.getSyncedCount(), result.getLastSyncTime());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强移动消费] 离线数据同步异常，userId={}, error={}",
                    request.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("OFFLINE_SYNC_ERROR", "离线数据同步异常：" + e.getMessage());
        }
    }

    /**
     * 离线消费验证
     * <p>
     * 在网络不可用时验证消费权限
     * 支持本地权限验证和数据缓存
     * </p>
     *
     * @param request 离线消费请求
     * @return 验证结果
     */
    @Observed(name = "enhancedMobileConsume.offlineValidate", contextualName = "offline-consume-validation")
    @PostMapping("/offline/validate")
    @Operation(
            summary = "离线消费验证",
            description = "离线模式下的消费权限验证"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileOfflineValidationResultVO> validateOfflineConsume(
            @Valid @RequestBody MobileOfflineConsumeRequestDTO request) {

        log.info("[增强移动消费] 离线消费验证，deviceId={}, amount={}",
                request.getDeviceId(), request.getAmount());

        try {
            MobileOfflineValidationResultVO result = mobileEdgeConsumeService
                .validateOfflineConsume(request);

            log.info("[增强移动消费] 离线消费验证完成，deviceId={}, allowed={}, reason={}",
                    request.getDeviceId(), result.isAllowed(), result.getValidationReason());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强移动消费] 离线消费验证异常，deviceId={}, error={}",
                    request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("OFFLINE_VALIDATION_ERROR", "离线消费验证异常：" + e.getMessage());
        }
    }

    /**
     * 离线记录上报
     * <p>
     * 网络恢复后上报离线期间的消费记录
     * </p>
     *
     * @param request 离线记录上报请求
     * @return 上报结果
     */
    @Observed(name = "enhancedMobileConsume.offlineReport", contextualName = "offline-record-report")
    @PostMapping("/offline/report")
    @Operation(
            summary = "离线记录上报",
            description = "上报离线期间的消费记录"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileOfflineReportResultVO> reportOfflineRecords(
            @Valid @RequestBody MobileOfflineReportRequestDTO request) {

        log.info("[增强移动消费] 离线记录上报，userId={}, recordCount={}",
                request.getUserId(), request.getRecords().size());

        try {
            MobileOfflineReportResultVO result = mobileEdgeConsumeService
                .reportOfflineRecords(request);

            log.info("[增强移动消费] 离线记录上报完成，userId={}, successCount={}, failedCount={}",
                    result.getUserId(), result.getSuccessCount(), result.getFailedCount());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[增强移动消费] 离线记录上报异常，userId={}, recordCount={}, error={}",
                    request.getUserId(), request.getRecords().size(), e.getMessage(), e);
            return ResponseDTO.error("OFFLINE_REPORT_ERROR", "离线记录上报异常：" + e.getMessage());
        }
    }

    // ==================== 智能推荐 ====================

    /**
     * 获取智能推荐
     * <p>
     * 基于用户行为模式的智能推荐
     * 包括商品推荐、优惠推荐等
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 推荐结果
     */
    @Observed(name = "enhancedMobileConsume.smartRecommendation", contextualName = "smart-recommendation")
    @GetMapping("/recommendation/{userId}")
    @Operation(
            summary = "获取智能推荐",
            description = "基于用户行为模式的智能推荐",
            parameters = {
                    @Parameter(name = "userId", description = "用户ID", required = true),
                    @Parameter(name = "deviceId", description = "设备ID", required = false)
            }
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<MobileSmartRecommendationVO> getSmartRecommendation(
            @PathVariable Long userId,
            @RequestParam(required = false) String deviceId) {

        log.info("[增强移动消费] 获取智能推荐，userId={}, deviceId={}", userId, deviceId);

        try {
            MobileSmartRecommendationVO recommendation = mobileEdgeConsumeService
                .generateSmartRecommendation(userId, deviceId);

            log.info("[增强移动消费] 智能推荐生成完成，userId={}, recommendationCount={}",
                    userId, recommendation.getRecommendations().size());

            return ResponseDTO.ok(recommendation);

        } catch (Exception e) {
            log.error("[增强移动消费] 获取智能推荐异常，userId={}, error={}",
                    userId, e.getMessage(), e);
            return ResponseDTO.error("SMART_RECOMMENDATION_ERROR", "智能推荐异常：" + e.getMessage());
        }
    }

    /**
     * 个性化偏好设置
     * <p>
     * 用户个性化消费偏好设置
     * 影响智能推荐结果
     * </p>
     *
     * @param request 偏好设置请求
     * @return 设置结果
     */
    @Observed(name = "enhancedMobileConsume.personalization", contextualName = "personalization-settings")
    @PostMapping("/personalization/settings")
    @Operation(
            summary = "个性化偏好设置",
            description = "用户个性化消费偏好设置"
    )
    @PermissionCheck(value = "MOBILE_CONSUME_USER", description = "移动端消费操作")
    public ResponseDTO<Void> setPersonalizationSettings(
            @Valid @RequestBody MobilePersonalizationRequestDTO request) {

        log.info("[增强移动消费] 设置个性化偏好，userId={}, preferences={}",
                request.getUserId(), request.getPreferences().size());

        try {
            boolean success = mobileEdgeConsumeService
                .savePersonalizationSettings(request);

            if (success) {
                log.info("[增强移动消费] 个性化偏好设置成功，userId={}", request.getUserId());
                return ResponseDTO.ok();
            } else {
                return ResponseDTO.error("PERSONALIZATION_SAVE_FAILED", "个性化偏好设置失败");
            }

        } catch (Exception e) {
            log.error("[增强移动消费] 设置个性化偏好异常，userId={}, error={}",
                    request.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("PERSONALIZATION_ERROR", "个性化偏好设置异常：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    private ConsumeRequestDTO buildConsumeRequest(BiometricConsumeRequestDTO request, Long authenticatedUserId) {
        ConsumeRequestDTO consumeRequest = new ConsumeRequestDTO();
        consumeRequest.setUserId(authenticatedUserId);
        consumeRequest.setAmount(request.getAmount());
        consumeRequest.setConsumeType(request.getConsumeType());
        consumeRequest.setDeviceId(request.getDeviceId());
        consumeRequest.setDescription("生物识别消费");
        consumeRequest.setPaymentMethod(request.getPaymentMethod());
        consumeRequest.setClientType("MOBILE");
        return consumeRequest;
    }

    private ConsumeRequestDTO buildConsumeRequestFromFaceScan(FaceScanConsumeRequestDTO request, Long authenticatedUserId) {
        ConsumeRequestDTO consumeRequest = new ConsumeRequestDTO();
        consumeRequest.setUserId(authenticatedUserId);
        consumeRequest.setAmount(request.getAmount());
        consumeRequest.setConsumeType("QR_CODE");
        consumeRequest.setDeviceId(request.getDeviceId());
        consumeRequest.setDescription("人脸识别扫码消费");
        consumeRequest.setClientType("MOBILE");
        consumeRequest.setQrCode(request.getQrCode());
        return consumeRequest;
    }

    private MobileBiometricConsumeResultVO convertToBiometricResult(
            ConsumeTransactionResultVO transactionResult,
            BiometricConsumeAuthResultVO authResult) {

        MobileBiometricConsumeVO result = new MobileBiometricConsumeVO();
        result.setOrderId(transactionResult.getOrderId());
        result.setAmount(transactionResult.getAmount());
        result.setPaymentStatus(transactionResult.getStatus());
        result.setPaymentTime(transactionResult.getPaymentTime());
        result.setBiometricType(authResult.getBiometricType());
        result.setBiometricConfidence(authResult.getConfidence());
        result.setTransactionId(transactionResult.getTransactionId());
        result.setReceiptUrl(transactionResult.getReceiptUrl());
        return result;
    }

    private MobileFaceScanConsumeResultVO convertToFaceScanResult(
            ConsumeTransactionResultVO transactionResult,
            MobileFaceScanAuthResultVO faceAuth,
            QrCodeParseResultVO qrResult) {

        MobileFaceScanConsumeResultVO result = new MobileFaceScanConsumeVO();
        result.setOrderId(transactionResult.getOrderId());
        result.setAmount(transactionResult.getAmount());
        result.setPaymentStatus(transactionResult.getStatus());
        result.setPaymentTime(transactionResult.getPaymentTime());
        result.setFaceConfidence(faceAuth.getConfidence());
        result.setQrType(qrResult.getQrType());
        result.setMerchantInfo(qrResult.getMerchantInfo());
        result.setDeviceId(faceAuth.getDeviceId());
        return result;
    }
}

package net.lab1024.sa.common.biometric.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.biometric.service.BiometricService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 生物识别服务实现类
 * <p>
 * 通过GatewayServiceClient调用生物识别服务
 * 注意：此服务仅作为fallback，正常情况下设备端应完成识别
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Slf4j
public class BiometricServiceImpl implements BiometricService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    private static final String BIOMETRIC_SERVICE_BASE_URL = "http://ioedream-biometric-service:8096";

    @Override
    public Long recognizeFace(String faceImageData, String deviceId) {
        log.info("[生物识别] 开始人脸识别: deviceId={}", deviceId);

        try {
            // 参数验证
            if (faceImageData == null || faceImageData.isEmpty()) {
                log.warn("[生物识别] 人脸图像数据为空");
                return null;
            }

            // 调用生物识别服务
            Map<String, Object> params = new HashMap<>();
            params.put("faceImageData", faceImageData);
            params.put("deviceId", deviceId);
            params.put("recognizeType", "1:N");

            // 创建BiometricResult的类型引用
            TypeReference<BiometricResult> typeRef = new TypeReference<BiometricResult>() {};

            // 先调用原始服务获取响应
            BiometricResult result = gatewayServiceClient.callCommonService(
                    "/api/biometric/recognize-face",
                    HttpMethod.POST,
                    params,
                    BiometricResult.class
            );

            if (result == null || result.getMatchedUserId() == null) {
                log.info("[生物识别] 未识别到匹配用户");
                return null;
            }

            Long userId = result.getMatchedUserId();
            log.info("[生物识别] 人脸识别成功: userId={}, confidence={}",
                    userId, result.getConfidence());

            return userId;

        } catch (Exception e) {
            log.error("[生物识别] 人脸识别异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long recognizeFingerprint(String fingerprintData, String deviceId) {
        log.info("[生物识别] 开始指纹识别: deviceId={}", deviceId);

        try {
            // 参数验证
            if (fingerprintData == null || fingerprintData.isEmpty()) {
                log.warn("[生物识别] 指纹数据为空");
                return null;
            }

            // 调用生物识别服务
            Map<String, Object> params = new HashMap<>();
            params.put("fingerprintData", fingerprintData);
            params.put("deviceId", deviceId);
            params.put("recognizeType", "1:N");

            BiometricResult result = gatewayServiceClient.callCommonService(
                    "/api/biometric/recognize-fingerprint",
                    HttpMethod.POST,
                    params,
                    BiometricResult.class
            );

            if (result == null || result.getMatchedUserId() == null) {
                log.info("[生物识别] 未识别到匹配用户");
                return null;
            }

            Long userId = result.getMatchedUserId();
            log.info("[生物识别] 指纹识别成功: userId={}, confidence={}",
                    userId, result.getConfidence());

            return userId;

        } catch (Exception e) {
            log.error("[生物识别] 指纹识别异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        // TODO: 实现健康检查逻辑
        // 可以通过调用生物识别服务的健康检查端点来验证
        return true;
    }

    /**
     * 生物识别结果内部类
     */
    @ lombok.Data
    public static class BiometricResult {
        /**
         * 匹配到的用户ID
         */
        private Long matchedUserId;

        /**
         * 置信度（0-100）
         */
        private Integer confidence;

        /**
         * 匹配时间（毫秒）
         */
        private Long matchTime;
    }
}

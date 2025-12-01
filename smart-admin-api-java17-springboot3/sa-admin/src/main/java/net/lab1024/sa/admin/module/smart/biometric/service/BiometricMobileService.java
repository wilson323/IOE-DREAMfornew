package net.lab1024.sa.admin.module.smart.biometric.service;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.service.SmartAccessControlService;
import net.lab1024.sa.admin.module.smart.biometric.constant.BiometricTypeEnum;
import net.lab1024.sa.admin.module.smart.biometric.dao.BiometricRecordDao;
import net.lab1024.sa.admin.module.smart.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.admin.module.smart.biometric.domain.entity.BiometricRecordEntity;
import net.lab1024.sa.admin.module.smart.biometric.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricOfflineTokenRequest;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricOfflineTokenResponse;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricRegisterRequest;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricRegisterResult;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricTypeOption;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricVerifyRequest;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricVerifyResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 移动端生物识别服务
 *
 * @author AI
 */
@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class BiometricMobileService {

    private static final int TEMPLATE_DEFAULT_STATUS = 1;

    private final BiometricTemplateDao biometricTemplateDao;
    private final BiometricRecordDao biometricRecordDao;
    private final SmartAccessControlService smartAccessControlService;
    private final BiometricDataEncryptionService biometricDataEncryptionService;

    @Transactional(rollbackFor = Exception.class)
    public BiometricRegisterResult registerTemplate(BiometricRegisterRequest request) {
        BiometricDataEncryptionService.EncryptionResult encryptionResult =
                biometricDataEncryptionService.encryptPayload(request.getBiometricPayload(),
                        request.getEmployeeId(), request.getDeviceId());

        BiometricTemplateEntity entity = new BiometricTemplateEntity();
        entity.setEmployeeId(request.getEmployeeId());
        entity.setEmployeeName(request.getEmployeeName());
        entity.setEmployeeCode(request.getEmployeeCode());
        entity.setBiometricType(request.getBiometricType().getValue());
        entity.setTemplateVersion("v1");
        entity.setTemplateData(encryptionResult.getCipherText());
        entity.setQualityMetrics(request.getCaptureMetadata());
        entity.setEnrollDate(LocalDate.now());
        entity.setTemplateStatus(TEMPLATE_DEFAULT_STATUS);
        entity.setSecurityMetadata(JSON.toJSONString(Map.of(
                "keyId", encryptionResult.getKeyId(),
                "fingerprint", encryptionResult.getFingerprint(),
                "channel", request.getCaptureChannel())));
        entity.setDeviceInfo(JSON.toJSONString(Map.of(
                "deviceId", request.getDeviceId(),
                "channel", request.getCaptureChannel())));
        entity.setEnrollConfidence(BigDecimal.valueOf(0.9));
        entity.setValidityDays(180);
        entity.setExpireDate(LocalDate.now().plusDays(180));

        biometricTemplateDao.insert(entity);
        saveRecord(entity, request.getCaptureMetadata(), request.getLocation(), request.getDeviceId());

        return BiometricRegisterResult.builder()
                .templateId(entity.getTemplateId())
                .biometricType(request.getBiometricType().getDesc())
                .encryptionKeyId(encryptionResult.getKeyId())
                .securityFingerprint(encryptionResult.getFingerprint())
                .cipherTextPreview(StringUtils.left(encryptionResult.getCipherText(), 32))
                .location(request.getLocation())
                .message("生物特征注册成功")
                .build();
    }

    public List<BiometricTypeOption> getSupportedTypes() {
        return Arrays.stream(BiometricTypeEnum.values())
                .map(type -> BiometricTypeOption.builder()
                        .value(type.getValue())
                        .label(type.getDesc())
                        .description(buildTypeDescription(type))
                        .icon("/static/images/biometric/" + type.getValue().toLowerCase() + ".png")
                        .build())
                .toList();
    }

    public BiometricVerifyResult verify(BiometricVerifyRequest request) {
        ResponseDTO<Map<String, Object>> response = dispatchVerify(request);
        boolean success = Boolean.TRUE.equals(response.getOk());
        double confidence = extractConfidence(response.getData());
        BiometricVerifyResult result = BiometricVerifyResult.builder()
                .success(success)
                .confidence(confidence)
                .message(response.getMsg())
                .biometricType(request.getBiometricType().getDesc())
                .detail(response.getData())
                .verifiedAt(LocalDateTime.now())
                .sessionId(UUID.randomUUID().toString())
                .build();

        saveVerifyRecord(request, result);
        return result;
    }

    public BiometricOfflineTokenResponse requestOfflineToken(BiometricOfflineTokenRequest request) {
        String token = smartAccessControlService.generateAccessToken(request.getPersonId(),
                request.getDeviceId(), request.getValidMinutes());
        return BiometricOfflineTokenResponse.builder()
                .accessToken(token)
                .expireAt(LocalDateTime.now().plusMinutes(request.getValidMinutes()))
                .build();
    }

    private ResponseDTO<Map<String, Object>> dispatchVerify(BiometricVerifyRequest request) {
        if (BiometricTypeEnum.FACE.equals(request.getBiometricType())) {
            return smartAccessControlService.verifyFaceAccess(request.getCredential(), request.getDeviceId());
        }
        if (BiometricTypeEnum.FINGERPRINT.equals(request.getBiometricType())) {
            return smartAccessControlService.verifyFingerprintAccess(request.getCredential(), request.getDeviceId());
        }
        return smartAccessControlService.verifyAccess(request.getPersonId(), request.getDeviceId(),
                request.getBiometricType().getValue(), request.getCredential());
    }

    private void saveRecord(BiometricTemplateEntity entity, String metadata, String location, Long deviceId) {
        BiometricRecordEntity record = buildBaseRecord(entity.getEmployeeId(),
                entity.getEmployeeName(), entity.getBiometricType(), entity.getTemplateData(), location, deviceId);
        record.setRecognitionMode("ENROLL");
        record.setVerificationResult(BiometricRecordEntity.VerificationResult.SUCCESS.getValue());
        record.setVerificationMetadata(metadata);
        record.setRecordTime(LocalDateTime.now());
        biometricRecordDao.insert(record);
    }

    private void saveVerifyRecord(BiometricVerifyRequest request, BiometricVerifyResult result) {
        BiometricRecordEntity record = buildBaseRecord(request.getPersonId(), null,
                request.getBiometricType().getValue(), request.getCredential(), request.getLocation(), request.getDeviceId());
        record.setRecognitionMode("VERIFY");
        record.setVerificationResult(result.isSuccess()
                ? BiometricRecordEntity.VerificationResult.SUCCESS.getValue()
                : BiometricRecordEntity.VerificationResult.FAILURE.getValue());
        record.setConfidenceScore(BigDecimal.valueOf(result.getConfidence()));
        record.setRecordTime(result.getVerifiedAt());
        record.setVerificationMetadata(JSON.toJSONString(result.getDetail()));
        biometricRecordDao.insert(record);
    }

    private BiometricRecordEntity buildBaseRecord(Long personId, String personName,
                                                  String biometricType, String credential,
                                                  String location, Long deviceId) {
        BiometricRecordEntity record = new BiometricRecordEntity();
        record.setEmployeeId(personId);
        record.setEmployeeName(personName);
        record.setBiometricType(biometricType);
        record.setFeatureVectors(credential);
        record.setDeviceId(deviceId);
        record.setDeviceName("MOBILE-APP");
        record.setDeviceCode(deviceId == null ? "UNKNOWN" : deviceId.toString());
        record.setLocationInfo(location);
        record.setSessionId(UUID.randomUUID().toString());
        record.setBusinessType("ACCESS");
        return record;
    }

    private double extractConfidence(Map<String, Object> data) {
        if (data == null) {
            return 0D;
        }
        Object value = data.get("confidence");
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0D;
    }

    private String buildTypeDescription(BiometricTypeEnum type) {
        return switch (type) {
            case FACE -> "支持活体检测的人脸识别";
            case FINGERPRINT -> "适用于移动端快速打卡的指纹识别";
            case PALMPRINT -> "掌静脉与掌纹融合识别";
            case IRIS -> "高安全级别虹膜匹配";
        };
    }
}


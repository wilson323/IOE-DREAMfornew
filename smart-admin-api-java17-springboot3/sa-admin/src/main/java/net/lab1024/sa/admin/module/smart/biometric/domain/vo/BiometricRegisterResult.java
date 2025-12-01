package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 鐢熺墿鐗瑰緛娉ㄥ唽结果
 *
 * @author AI
 */
@Data
@Builder
public class BiometricRegisterResult {

    private Long templateId;

    private String encryptionKeyId;

    private String cipherTextPreview;

    private String securityFingerprint;

    private String biometricType;

    private String location;

    private String message;
}


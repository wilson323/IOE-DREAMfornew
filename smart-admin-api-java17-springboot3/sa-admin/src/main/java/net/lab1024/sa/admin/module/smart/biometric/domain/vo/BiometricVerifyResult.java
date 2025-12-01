package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 鐢熺墿识别楠岃瘉结果
 *
 * @author AI
 */
@Data
@Builder
public class BiometricVerifyResult {

    private boolean success;

    private double confidence;

    private String message;

    private String biometricType;

    private Map<String, Object> detail;

    private LocalDateTime verifiedAt;

    private String sessionId;
}


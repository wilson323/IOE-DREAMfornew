package net.lab1024.sa.admin.module.smart.biometric.domain.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * 离线浠ょ墝响应簲
 *
 * @author AI
 */
@Data
@Builder
public class BiometricOfflineTokenResponse {

    private String accessToken;

    private LocalDateTime expireAt;
}


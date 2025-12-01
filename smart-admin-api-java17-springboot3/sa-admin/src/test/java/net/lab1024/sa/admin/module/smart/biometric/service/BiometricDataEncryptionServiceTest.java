package net.lab1024.sa.admin.module.smart.biometric.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 单元测试：验证生物特征加密服务
 *
 * @author AI
 */
class BiometricDataEncryptionServiceTest {

    private final BiometricDataEncryptionService service = new BiometricDataEncryptionService();

    @Test
    void shouldEncryptAndDecryptPayload() {
        var result = service.encryptPayload("sample-biometric-data", 1L, 2L);
        Assertions.assertNotNull(result.getKeyId());
        Assertions.assertNotNull(result.getCipherText());
        String plain = service.decryptPayload(result.getKeyId(), result.getCipherText());
        Assertions.assertEquals("sample-biometric-data", plain);
    }
}


package net.lab1024.sa.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AESUtil 简化单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-20
 */
@DisplayName("AESUtil 简化单元测试")
class AESUtilSimpleTest {

    private static final String TEST_KEY = "1234567890123456"; // 16位密钥
    private static final String TEST_PLAINTEXT = "这是一段需要加密的测试文本";

    @Test
    @DisplayName("测试基本加密解密功能")
    void testBasicEncryptDecrypt() {
        // Given
        String plaintext = TEST_PLAINTEXT;

        // When
        String encrypted = AESUtil.encrypt(plaintext, TEST_KEY);
        String decrypted = AESUtil.decrypt(encrypted, TEST_KEY);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertNotEquals(plaintext, encrypted); // 加密后应该不同
        assertEquals(plaintext, decrypted);    // 解密后应该相同
    }

    @Test
    @DisplayName("测试空字符串加密")
    void testEmptyStringEncryptDecrypt() {
        // Given
        String emptyString = "";

        // When
        String encrypted = AESUtil.encrypt(emptyString, TEST_KEY);
        String decrypted = AESUtil.decrypt(encrypted, TEST_KEY);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(emptyString, decrypted);
    }

    @Test
    @DisplayName("测试中文字符加密解密")
    void testChineseCharacterEncryptDecrypt() {
        // Given
        String chineseText = "中文字符测试@#$%^&*()";

        // When
        String encrypted = AESUtil.encrypt(chineseText, TEST_KEY);
        String decrypted = AESUtil.decrypt(encrypted, TEST_KEY);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(chineseText, decrypted);
    }

    @Test
    @DisplayName("测试长文本加密解密")
    void testLongTextEncryptDecrypt() {
        // Given
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("这是第").append(i).append("行文本内容，包含一些特殊字符：@#$%\n");
        }

        // When
        String encrypted = AESUtil.encrypt(longText.toString(), TEST_KEY);
        String decrypted = AESUtil.decrypt(encrypted, TEST_KEY);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(longText.toString(), decrypted);
    }

    @Test
    @DisplayName("测试不同密钥加密结果不同")
    void testDifferentKeyDifferentResult() {
        // Given
        String plaintext = TEST_PLAINTEXT;
        String key1 = "1234567890123456";
        String key2 = "6543210987654321";

        // When
        String encrypted1 = AESUtil.encrypt(plaintext, key1);
        String encrypted2 = AESUtil.encrypt(plaintext, key2);

        // Then
        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotEquals(encrypted1, encrypted2); // 不同密钥应该产生不同结果

        // 验证各自解密
        String decrypted1 = AESUtil.decrypt(encrypted1, key1);
        String decrypted2 = AESUtil.decrypt(encrypted2, key2);
        assertEquals(plaintext, decrypted1);
        assertEquals(plaintext, decrypted2);
    }

    @Test
    @DisplayName("测试16位密钥（AES标准密钥长度）")
    void testStandardKeyLength() {
        // Given
        String plaintext = TEST_PLAINTEXT;
        String standardKey16 = "1234567890123456"; // 16位密钥（AES-128）

        // When
        String encrypted = AESUtil.encrypt(plaintext, standardKey16);
        String decrypted = AESUtil.decrypt(encrypted, standardKey16);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("测试32位密钥（AES-256）")
    void testAES256KeyLength() {
        // Given
        String plaintext = TEST_PLAINTEXT;
        String key32 = "12345678901234567890123456789012"; // 32位密钥（AES-256）

        // When
        String encrypted = AESUtil.encrypt(plaintext, key32);
        String decrypted = AESUtil.decrypt(encrypted, key32);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("测试数字和字母混合加密")
    void testAlphanumericEncryptDecrypt() {
        // Given
        String mixedText = "ABC123abc!@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        String encrypted = AESUtil.encrypt(mixedText, TEST_KEY);
        String decrypted = AESUtil.decrypt(encrypted, TEST_KEY);

        // Then
        assertNotNull(encrypted);
        assertNotNull(decrypted);
        assertEquals(mixedText, decrypted);
    }

    @Test
    @DisplayName("测试加密结果一致性")
    void testEncryptionConsistency() {
        // Given
        String plaintext = TEST_PLAINTEXT;

        // When - 多次加密同一文本
        String encrypted1 = AESUtil.encrypt(plaintext, TEST_KEY);
        String encrypted2 = AESUtil.encrypt(plaintext, TEST_KEY);
        String encrypted3 = AESUtil.encrypt(plaintext, TEST_KEY);

        // Then - 验证加密结果一致性（AES是确定性加密）
        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotNull(encrypted3);
        assertEquals(encrypted1, encrypted2);
        assertEquals(encrypted2, encrypted3);

        // 验证解密结果一致性
        String decrypted1 = AESUtil.decrypt(encrypted1, TEST_KEY);
        String decrypted2 = AESUtil.decrypt(encrypted2, TEST_KEY);
        String decrypted3 = AESUtil.decrypt(encrypted3, TEST_KEY);
        assertEquals(decrypted1, decrypted2);
        assertEquals(decrypted2, decrypted3);
        assertEquals(plaintext, decrypted1);
    }
}

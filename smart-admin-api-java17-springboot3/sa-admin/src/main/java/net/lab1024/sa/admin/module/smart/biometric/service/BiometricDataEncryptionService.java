package net.lab1024.sa.admin.module.smart.biometric.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.crypto.SM3Digest;
import net.lab1024.sa.base.common.crypto.SM4Cipher;

/**
 * 生物特征数据加密服务，基于SM3/SM4提供基础的加解密能力
 *
 * @author AI
 */
@Slf4j
@Service
public class BiometricDataEncryptionService {

    private static final long KEY_TTL = Duration.ofHours(24).toMillis();

    private final Map<String, SecretKeyInfo> keyCache = new ConcurrentHashMap<>();

    /**
     * 对生物特征内容进行加密
     *
     * @param payload 原始内容（可以是文件地址或Base64字符串）
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 加密结果
     */
    public EncryptionResult encryptPayload(String payload, Long userId, Long deviceId) {
        if (StringUtils.isBlank(payload)) {
            throw new IllegalArgumentException("payload不能为空");
        }
        byte[] source = payload.getBytes(StandardCharsets.UTF_8);
        // 生成密钥与IV（字节），使用字节重载的CBC接口，加密后返回Base64密文
        byte[] keyBytes = new byte[16];
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(keyBytes);
        new SecureRandom().nextBytes(ivBytes);
        byte[] cipherBytes = encryptCBCBytes(keyBytes, ivBytes, source);
        String cipherText = Base64.getEncoder().encodeToString(cipherBytes);

        String keyId = buildKeyId(userId, deviceId);
        keyCache.put(keyId, new SecretKeyInfo(keyBytes, ivBytes, System.currentTimeMillis()));
        cleanupExpiredKeys();

        return EncryptionResult.builder().keyId(keyId).cipherText(cipherText)
                .fingerprint(SM3Digest.hash(payload + ":" + userId + ":" + deviceId))
                .metadata(JSON.toJSONString(Map.of("algorithm", "SM4-CBC",
                        "ivLength", 16, "payloadSize", source.length)))
                .build();
    }

    /**
     * 解密生物特征内容
     *
     * @param keyId keyId
     * @param cipherText 密文
     * @return 原始内容
     */
    public String decryptPayload(String keyId, String cipherText) {
        SecretKeyInfo keyInfo = keyCache.get(keyId);
        if (keyInfo == null) {
            throw new IllegalStateException("密钥已经过期或不存在");
        }
        byte[] ciphertextBytes = Base64.getDecoder().decode(cipherText);
        byte[] plaintext = decryptCBCBytes(keyInfo.getKey(), keyInfo.getIv(), ciphertextBytes);
        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private String buildKeyId(Long userId, Long deviceId) {
        return "BIO-KEY-" + (userId == null ? "anonymous" : userId) + "-"
                + (deviceId == null ? "device" : deviceId) + "-" + System.nanoTime();
    }

    private void cleanupExpiredKeys() {
        long now = System.currentTimeMillis();
        keyCache.entrySet().removeIf(entry -> now - entry.getValue().getCreateTime() > KEY_TTL);
    }

    @Data
    @Builder
    public static class EncryptionResult {
        private String keyId;
        private String cipherText;
        private String fingerprint;
        private String metadata;
    }

    @Data
    private static class SecretKeyInfo {
        private final byte[] key;
        private final byte[] iv;
        private final long createTime;
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null) {
            return new byte[0];
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] encryptCBCBytes(byte[] key, byte[] iv, byte[] plaintext) {
        try {
            // 优先尝试字节签名
            var m = SM4Cipher.class.getMethod("encryptCBC", byte[].class, byte[].class, byte[].class);
            return (byte[]) m.invoke(null, key, iv, plaintext);
        } catch (NoSuchMethodException e) {
            // 回退：使用字符串签名并解Base64
            String keyHex = bytesToHex(key);
            String ivHex = bytesToHex(iv);
            String base64 = SM4Cipher.encryptCBC(keyHex, ivHex, new String(plaintext, StandardCharsets.UTF_8));
            return Base64.getDecoder().decode(base64);
        } catch (Exception ex) {
            throw new RuntimeException("SM4 CBC加密失败", ex);
        }
    }

    private static byte[] decryptCBCBytes(byte[] key, byte[] iv, byte[] ciphertext) {
        try {
            // 优先尝试字节签名
            var m = SM4Cipher.class.getMethod("decryptCBC", byte[].class, byte[].class, byte[].class);
            return (byte[]) m.invoke(null, key, iv, ciphertext);
        } catch (NoSuchMethodException e) {
            // 回退：使用字符串签名，先Base64编码密文
            String keyHex = bytesToHex(key);
            String ivHex = bytesToHex(iv);
            String base64 = Base64.getEncoder().encodeToString(ciphertext);
            String plaintext = SM4Cipher.decryptCBC(keyHex, ivHex, base64);
            return plaintext.getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("SM4 CBC解密失败", ex);
        }
    }
}

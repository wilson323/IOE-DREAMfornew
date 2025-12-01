package net.lab1024.sa.base.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * SM3 摘要工具（占位实现，使用 SHA-256 接口对齐）
 */
@Slf4j
public class SM3Digest {

    public static String hash(String data) {
        return hash(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found", e);
            throw new RuntimeException("哈希算法不可用", e);
        }
    }

    public static String hashBase64(String data) {
        return hashBase64(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String hashBase64(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found", e);
            throw new RuntimeException("哈希算法不可用", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}



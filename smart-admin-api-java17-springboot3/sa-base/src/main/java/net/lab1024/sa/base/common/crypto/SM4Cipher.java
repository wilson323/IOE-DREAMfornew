package net.lab1024.sa.base.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SM4Cipher {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final String ALGORITHM_NAME = "SM4";
    public static final int KEY_SIZE_BYTES = 16;
    public static final int KEY_SIZE_BITS = 128;
    public static final int BLOCK_SIZE_BYTES = 16;
    public static final int BLOCK_SIZE_BITS = 128;
    public static final String MODE_ECB = "ECB";
    public static final String MODE_CBC = "CBC";
    public static final String MODE_CFB = "CFB";
    public static final String MODE_OFB = "OFB";
    public static final String MODE_CTR = "CTR";
    public static final String PADDING_NO = "NoPadding";
    public static final String PADDING_PKCS5 = "PKCS5Padding";
    public static final String PADDING_PKCS7 = "PKCS7Padding";
    public static final String PADDING_ISO10126 = "ISO10126Padding";

    public static String generateKey() {
        return generateKeyHex();
    }

    public static byte[] generateKeyBytes() {
        byte[] key = new byte[KEY_SIZE_BYTES];
        new SecureRandom().nextBytes(key);
        return key;
    }

    public static String generateKeyHex() {
        byte[] key = generateKeyBytes();
        return bytesToHex(key);
    }

    public static String generateKeyBase64() {
        byte[] key = generateKeyBytes();
        return Base64.getEncoder().encodeToString(key);
    }

    public static String encryptECB(String keyHex, String plaintext) {
        if (keyHex == null || plaintext == null) {
            throw new IllegalArgumentException("Key and plaintext cannot be null");
        }
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = encryptECB(keyBytes, plaintextBytes);
            return Base64.getEncoder().encodeToString(ciphertextBytes);
        } catch (Exception e) {
            log.error("SM4 ECB encryption failed", e);
            throw new RuntimeException("SM4 ECB加密失败", e);
        }
    }

    public static byte[] encryptECB(byte[] key, byte[] plaintext) {
        if (key == null || plaintext == null) {
            throw new IllegalArgumentException("Key and plaintext cannot be null");
        }
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid key size: " + key.length + " bytes, expected: " + KEY_SIZE_BYTES);
        }
        try {
            String transformation = ALGORITHM_NAME + "/" + MODE_ECB + "/" + PADDING_PKCS5;
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            log.error("SM4 ECB encryption failed", e);
            throw new RuntimeException("SM4 ECB加密失败", e);
        }
    }

    public static String decryptECB(String keyHex, String ciphertext) {
        if (keyHex == null || ciphertext == null) {
            throw new IllegalArgumentException("Key and ciphertext cannot be null");
        }
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
            byte[] plaintextBytes = decryptECB(keyBytes, ciphertextBytes);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM4 ECB decryption failed", e);
            throw new RuntimeException("SM4 ECB解密失败", e);
        }
    }

    public static byte[] decryptECB(byte[] key, byte[] ciphertext) {
        if (key == null || ciphertext == null) {
            throw new IllegalArgumentException("Key and ciphertext cannot be null");
        }
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid key size: " + key.length + " bytes, expected: " + KEY_SIZE_BYTES);
        }
        try {
            String transformation = ALGORITHM_NAME + "/" + MODE_ECB + "/" + PADDING_PKCS5;
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            log.error("SM4 ECB decryption failed", e);
            throw new RuntimeException("SM4 ECB解密失败", e);
        }
    }

    public static String encryptCBC(String keyHex, String ivHex, String plaintext) {
        if (keyHex == null || ivHex == null || plaintext == null) {
            throw new IllegalArgumentException("Key, IV and plaintext cannot be null");
        }
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            byte[] ivBytes = hexToBytes(ivHex);
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = encryptCBC(keyBytes, ivBytes, plaintextBytes);
            return Base64.getEncoder().encodeToString(ciphertextBytes);
        } catch (Exception e) {
            log.error("SM4 CBC encryption failed", e);
            throw new RuntimeException("SM4 CBC加密失败", e);
        }
    }

    public static byte[] encryptCBC(byte[] key, byte[] iv, byte[] plaintext) {
        if (key == null || iv == null || plaintext == null) {
            throw new IllegalArgumentException("Key, IV and plaintext cannot be null");
        }
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid key size: " + key.length + " bytes, expected: " + KEY_SIZE_BYTES);
        }
        if (iv.length != BLOCK_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid IV size: " + iv.length + " bytes, expected: " + BLOCK_SIZE_BYTES);
        }
        try {
            String transformation = ALGORITHM_NAME + "/" + MODE_CBC + "/" + PADDING_PKCS5;
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            log.error("SM4 CBC encryption failed", e);
            throw new RuntimeException("SM4 CBC加密失败", e);
        }
    }

    public static String decryptCBC(String keyHex, String ivHex, String ciphertext) {
        if (keyHex == null || ivHex == null || ciphertext == null) {
            throw new IllegalArgumentException("Key, IV and ciphertext cannot be null");
        }
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            byte[] ivBytes = hexToBytes(ivHex);
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
            byte[] plaintextBytes = decryptCBC(keyBytes, ivBytes, ciphertextBytes);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM4 CBC decryption failed", e);
            throw new RuntimeException("SM4 CBC解密失败", e);
        }
    }

    public static byte[] decryptCBC(byte[] key, byte[] iv, byte[] ciphertext) {
        if (key == null || iv == null || ciphertext == null) {
            throw new IllegalArgumentException("Key, IV and ciphertext cannot be null");
        }
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid key size: " + key.length + " bytes, expected: " + KEY_SIZE_BYTES);
        }
        if (iv.length != BLOCK_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid IV size: " + iv.length + " bytes, expected: " + BLOCK_SIZE_BYTES);
        }
        try {
            String transformation = ALGORITHM_NAME + "/" + MODE_CBC + "/" + PADDING_PKCS5;
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            log.error("SM4 CBC decryption failed", e);
            throw new RuntimeException("SM4 CBC解密失败", e);
        }
    }

    public static String generateIV() {
        return generateIVHex();
    }

    public static byte[] generateIVBytes() {
        byte[] iv = new byte[BLOCK_SIZE_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static String generateIVHex() {
        byte[] iv = generateIVBytes();
        return bytesToHex(iv);
    }

    public static String generateIVBase64() {
        byte[] iv = generateIVBytes();
        return Base64.getEncoder().encodeToString(iv);
    }

    public static String encrypt(String keyHex, String ivHex, String plaintext, String mode, String padding) {
        if (keyHex == null || plaintext == null || mode == null || padding == null) {
            throw new IllegalArgumentException("Key, plaintext, mode and padding cannot be null");
        }
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            byte[] ivBytes = (ivHex != null && !ivHex.isEmpty()) ? hexToBytes(ivHex) : null;
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = encrypt(keyBytes, ivBytes, plaintextBytes, mode, padding);
            return Base64.getEncoder().encodeToString(ciphertextBytes);
        } catch (Exception e) {
            log.error("SM4 custom encryption failed", e);
            throw new RuntimeException("SM4自定义加密失败", e);
        }
    }

    public static byte[] encrypt(byte[] key, byte[] iv, byte[] plaintext, String mode, String padding) {
        if (key == null || plaintext == null || mode == null || padding == null) {
            throw new IllegalArgumentException("Key, plaintext, mode and padding cannot be null");
        }
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid key size: " + key.length + " bytes, expected: " + KEY_SIZE_BYTES);
        }
        if (!MODE_ECB.equals(mode) && (iv == null || iv.length != BLOCK_SIZE_BYTES)) {
            throw new IllegalArgumentException(
                    "IV is required and must be " + BLOCK_SIZE_BYTES + " bytes for " + mode + " mode");
        }
        try {
            String transformation = ALGORITHM_NAME + "/" + mode + "/" + padding;
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            if (MODE_ECB.equals(mode)) {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            } else {
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            }
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            log.error("SM4 custom encryption failed", e);
            throw new RuntimeException("SM4自定义加密失败", e);
        }
    }

    public static String decrypt(String keyHex, String ivHex, String ciphertext, String mode, String padding) {
        if (keyHex == null || ciphertext == null || mode == null || padding == null) {
            throw new IllegalArgumentException("Key, ciphertext, mode and padding cannot be null");
        }
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            byte[] ivBytes = (ivHex != null && !ivHex.isEmpty()) ? hexToBytes(ivHex) : null;
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
            byte[] plaintextBytes = decrypt(keyBytes, ivBytes, ciphertextBytes, mode, padding);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("SM4 custom decryption failed", e);
            throw new RuntimeException("SM4自定义解密失败", e);
        }
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] ciphertext, String mode, String padding) {
        if (key == null || ciphertext == null || mode == null || padding == null) {
            throw new IllegalArgumentException("Key, ciphertext, mode and padding cannot be null");
        }
        if (key.length != KEY_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid key size: " + key.length + " bytes, expected: " + KEY_SIZE_BYTES);
        }
        if (!MODE_ECB.equals(mode) && (iv == null || iv.length != BLOCK_SIZE_BYTES)) {
            throw new IllegalArgumentException(
                    "IV is required and must be " + BLOCK_SIZE_BYTES + " bytes for " + mode + " mode");
        }
        try {
            String transformation = ALGORITHM_NAME + "/" + mode + "/" + padding;
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM_NAME);
            if (MODE_ECB.equals(mode)) {
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
            } else {
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            }
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            log.error("SM4 custom decryption failed", e);
            throw new RuntimeException("SM4自定义解密失败", e);
        }
    }

    public static boolean validateKey(String keyHex) {
        try {
            byte[] keyBytes = hexToBytes(keyHex);
            return validateKey(keyBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateKey(byte[] key) {
        return key != null && key.length == KEY_SIZE_BYTES;
    }

    public static boolean validateIV(String ivHex) {
        try {
            byte[] ivBytes = hexToBytes(ivHex);
            return validateIV(ivBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateIV(byte[] iv) {
        return iv != null && iv.length == BLOCK_SIZE_BYTES;
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        hex = hex.toLowerCase().replaceAll("\\s+", "");
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex string: " + hex);
            }
            result[i / 2] = (byte) ((high << 4) | low);
        }
        return result;
    }
}

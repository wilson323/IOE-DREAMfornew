import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Nacos配置加密工具
 * 用于加密配置文件中的敏感信息
 */
public class ConfigEncryptionTool {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;

    /**
     * 生成AES密钥
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_LENGTH);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 加密明文密码
     */
    public static String encrypt(String plaintext, String keyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // 生成随机IV
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // 组合IV和加密数据
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 解密密文密码
     */
    public static String decrypt(String ciphertextBase64, String keyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] combined = Base64.getDecoder().decode(ciphertextBase64);

        // 提取IV和加密数据
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 批量加密配置文件
     */
    public static void encryptConfigFile(String filePath, String keyBase64) throws Exception {
        // 实现配置文件加密逻辑
        System.out.println("加密配置文件: " + filePath);
        // TODO: 实现具体的文件加密逻辑
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("用法:");
                System.out.println("  java ConfigEncryptionTool generate    - 生成加密密钥");
                System.out.println("  java ConfigEncryptionTool encrypt <明文> <密钥>  - 加密文本");
                System.out.println("  java ConfigEncryptionTool decrypt <密文> <密钥>  - 解密文本");
                return;
            }

            String command = args[0];

            switch (command) {
                case "generate":
                    String key = generateKey();
                    System.out.println("生成的密钥: " + key);
                    break;

                case "encrypt":
                    if (args.length < 3) {
                        System.out.println("错误: 加密需要明文和密钥");
                        return;
                    }
                    String plaintext = args[1];
                    String encKey = args[2];
                    String encrypted = encrypt(plaintext, encKey);
                    System.out.println("明文: " + plaintext);
                    System.out.println("密文: " + encrypted);
                    break;

                case "decrypt":
                    if (args.length < 3) {
                        System.out.println("错误: 解密需要密文和密钥");
                        return;
                    }
                    String ciphertext = args[1];
                    String decKey = args[2];
                    String decrypted = decrypt(ciphertext, decKey);
                    System.out.println("密文: " + ciphertext);
                    System.out.println("明文: " + decrypted);
                    break;

                default:
                    System.out.println("错误: 未知命令: " + command);
                    break;
            }
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
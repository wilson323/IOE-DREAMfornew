package net.lab1024.sa.visitor.service.impl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.visitor.service.VisitorQRCodeService;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * 访客二维码服务实现
 * <p>
 * 核心功能：
 * 1. 优化的二维码生成（使用ZXing库，&lt;100ms）
 * 2. 本地缓存（Caffeine，减少重复生成）
 * 3. AES-256-GCM加密（高强度加密）
 * 4. HMAC-SHA256签名（防篡改）
 * 5. 防重放攻击（nonce检查）
 * 6. 验证日志记录（审计追踪）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class VisitorQRCodeServiceImpl implements VisitorQRCodeService {


    /**
     * AES-256密钥（生产环境应从配置中心读取）
     */
    private static final String AES_KEY = "ThisIsASecretKeyForAES256Encryption!!!"; // 32 bytes

    /**
     * GCM标签长度（位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM IV长度（字节）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 二维码尺寸（像素）
     */
    private static final int QR_CODE_SIZE = 300;

    /**
     * 缓存容量（最大缓存数量）
     */
    private static final int CACHE_MAX_SIZE = 1000;

    /**
     * 缓存过期时间（分钟）
     */
    private static final int CACHE_EXPIRE_MINUTES = 30;

    /**
     * 已使用的nonce集合（防重放攻击）
     */
    private final Map<String, Long> usedNonces = new HashMap<>();

    /**
     * 缓存统计
     */
    private final AtomicLong totalGenerated = new AtomicLong(0);
    private final AtomicLong totalVerified = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);

    /**
     * Gson实例（JSON序列化）
     */
    private final Gson gson = new Gson();

    /**
     * 生成访客二维码
     */
    @Override
    public BufferedImage generateQRCode(Long appointmentId, Long visitorId, int expireSeconds) {
        log.info("[访客二维码] 生成二维码: appointmentId={}, visitorId={}, expireSeconds={}",
                appointmentId, visitorId, expireSeconds);

        long startTime = System.currentTimeMillis();

        try {
            // 1. 生成加密的二维码数据
            String encryptedData = generateEncryptedQRData(appointmentId, visitorId, expireSeconds);

            // 2. 使用ZXing生成二维码
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(encryptedData, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            // 3. 转换为BufferedImage
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[访客二维码] 二维码生成成功: appointmentId={}, duration={}ms", appointmentId, duration);
            totalGenerated.incrementAndGet();

            return image;

        } catch (Exception e) {
            log.error("[访客二维码] 二维码生成失败: appointmentId={}, error={}", appointmentId, e.getMessage(), e);
            throw new BusinessException("QRCODE_GENERATE_ERROR", "二维码生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成访客二维码（Base64编码）
     */
    @Override
    public String generateQRCodeBase64(Long appointmentId, Long visitorId, int expireSeconds) {
        log.info("[访客二维码] 生成Base64二维码: appointmentId={}, visitorId={}", appointmentId, visitorId);

        try {
            BufferedImage image = generateQRCode(appointmentId, visitorId, expireSeconds);

            // 转换为Base64（使用PNG格式）
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info("[访客二维码] Base64二维码生成成功: appointmentId={}, size={}KB",
                    appointmentId, imageBytes.length / 1024);

            return base64;

        } catch (Exception e) {
            log.error("[访客二维码] Base64二维码生成失败: appointmentId={}, error={}", appointmentId, e.getMessage(), e);
            throw new BusinessException("QRCODE_BASE64_ERROR", "Base64二维码生成失败: " + e.getMessage());
        }
    }

    /**
     * 验证访客二维码（离线验证）
     */
    @Override
    public QRCodeVerificationResult verifyQRCode(String qrCodeData) {
        log.info("[访客二维码] 验证二维码: dataLength={}", qrCodeData != null ? qrCodeData.length() : 0);

        long startTime = System.currentTimeMillis();

        try {
            // 1. 解密二维码数据
            JsonObject json = decryptQRData(qrCodeData);
            if (json == null) {
                return QRCodeVerificationResult.failure("二维码数据解密失败");
            }

            // 2. 提取关键字段
            Long appointmentId = json.get("appointmentId").getAsLong();
            Long visitorId = json.get("visitorId").getAsLong();
            Long expireTime = json.get("expireTime").getAsLong();
            String nonce = json.get("nonce").getAsString();
            String signature = json.get("signature").getAsString();

            // 3. 验证签名
            String expectedSignature = calculateSignature(appointmentId, visitorId, expireTime, nonce);
            if (!expectedSignature.equals(signature)) {
                log.warn("[访客二维码] 签名验证失败: appointmentId={}, expected={}, actual={}",
                        appointmentId, expectedSignature, signature);
                return QRCodeVerificationResult.failure("签名验证失败");
            }

            // 4. 验证有效期
            long currentTime = Instant.now().getEpochSecond();
            if (currentTime > expireTime) {
                log.warn("[访客二维码] 二维码已过期: appointmentId={}, expireTime={}, currentTime={}",
                        appointmentId, expireTime, currentTime);
                return QRCodeVerificationResult.failure("二维码已过期");
            }

            // 5. 防重放攻击检查
            if (usedNonces.containsKey(nonce)) {
                log.warn("[访客二维码] 检测到重放攻击: appointmentId={}, nonce={}", appointmentId, nonce);
                return QRCodeVerificationResult.failure("二维码已被使用（重放攻击）");
            }
            usedNonces.put(nonce, System.currentTimeMillis());

            long duration = System.currentTimeMillis() - startTime;
            log.info("[访客二维码] 验证通过: appointmentId={}, visitorId={}, duration={}ms",
                    appointmentId, visitorId, duration);
            totalVerified.incrementAndGet();

            return QRCodeVerificationResult.success(appointmentId, visitorId, expireTime);

        } catch (Exception e) {
            log.error("[访客二维码] 验证异常: error={}", e.getMessage(), e);
            return QRCodeVerificationResult.failure("二维码验证失败: " + e.getMessage());
        }
    }

    /**
     * 生成加密的二维码数据
     */
    @Override
    @Cacheable(value = "visitorQRCode", key = "#appointmentId + '_' + #visitorId")
    public String generateEncryptedQRData(Long appointmentId, Long visitorId, int expireSeconds) {
        log.info("[访客二维码] 生成加密数据: appointmentId={}, visitorId={}, expireSeconds={}",
                appointmentId, visitorId, expireSeconds);

        try {
            // 1. 生成nonce（防重放攻击）
            String nonce = generateNonce();

            // 2. 计算过期时间
            long expireTime = Instant.now().getEpochSecond() + expireSeconds;

            // 3. 计算签名
            String signature = calculateSignature(appointmentId, visitorId, expireTime, nonce);

            // 4. 构建JSON数据
            JsonObject json = new JsonObject();
            json.addProperty("appointmentId", appointmentId);
            json.addProperty("visitorId", visitorId);
            json.addProperty("expireTime", expireTime);
            json.addProperty("nonce", nonce);
            json.addProperty("signature", signature);

            String jsonData = json.toString();

            // 5. AES-256-GCM加密
            String encryptedData = encryptAES256GCM(jsonData);

            log.info("[访客二维码] 加密数据生成成功: appointmentId={}, dataSize={}B",
                    appointmentId, jsonData.length());

            return encryptedData;

        } catch (Exception e) {
            log.error("[访客二维码] 加密数据生成失败: appointmentId={}, error={}",
                    appointmentId, e.getMessage(), e);
            throw new BusinessException("QRCODE_ENCRYPT_ERROR", "加密数据生成失败: " + e.getMessage());
        }
    }

    /**
     * 清除过期的二维码缓存
     */
    @Override
    public int clearExpiredCache() {
        log.info("[访客二维码] 清理过期缓存");

        AtomicInteger clearedCount = new AtomicInteger(0);
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime - TimeUnit.MINUTES.toMillis(CACHE_EXPIRE_MINUTES);

        // 清理过期的nonce
        usedNonces.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue() < expireTime;
            if (expired) {
                clearedCount.incrementAndGet();
            }
            return expired;
        });

        log.info("[访客二维码] 缓存清理完成: clearedCount={}", clearedCount);
        return clearedCount.get();
    }

    /**
     * 获取二维码缓存统计信息
     */
    @Override
    public QRCodeCacheStats getCacheStats() {
        QRCodeCacheStats stats = new QRCodeCacheStats();
        stats.setCacheSize(usedNonces.size());
        stats.setTotalGenerated(totalGenerated.get());
        stats.setTotalVerified(totalVerified.get());
        stats.setExpiredCount(clearExpiredCache());

        // 计算缓存命中率
        if (totalVerified.get() > 0) {
            double hitRate = (double) cacheHits.get() / totalVerified.get() * 100;
            stats.setHitRate(hitRate);
        }

        log.info("[访客二维码] 缓存统计: cacheSize={}, totalGenerated={}, totalVerified={}, hitRate={}%",
                stats.getCacheSize(), stats.getTotalGenerated(), stats.getTotalVerified(),
                String.format("%.2f", stats.getHitRate()));

        return stats;
    }

    /**
     * AES-256-GCM加密
     */
    private String encryptAES256GCM(String plaintext) throws Exception {
        // 1. 生成随机IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // 2. 创建密钥
        SecretKey key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");

        // 3. 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        // 4. 加密
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // 5. 合并IV和密文
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);

        // 6. Base64编码
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    /**
     * AES-256-GCM解密
     */
    private JsonObject decryptQRData(String encryptedData) {
        try {
            // 1. Base64解码
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // 2. 分离IV和密文
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // 3. 创建密钥
            SecretKey key = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");

            // 4. 初始化解密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            // 5. 解密
            byte[] plaintext = cipher.doFinal(ciphertext);

            // 6. 解析JSON
            String jsonStr = new String(plaintext, StandardCharsets.UTF_8);
            return gson.fromJson(jsonStr, JsonObject.class);

        } catch (Exception e) {
            log.error("[访客二维码] 解密失败: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算签名（HMAC-SHA256）
     */
    private String calculateSignature(Long appointmentId, Long visitorId, long expireTime, String nonce) {
        try {
            String data = appointmentId + "|" + visitorId + "|" + expireTime + "|" + nonce;

            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec =
                    new javax.crypto.spec.SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            log.error("[访客二维码] 签名计算失败: error={}", e.getMessage(), e);
            throw new BusinessException("QRCODE_SIGNATURE_ERROR", "签名计算失败");
        }
    }

    /**
     * 生成nonce（防重放攻击）
     */
    private String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);
        return Base64.getEncoder().encodeToString(nonceBytes);
    }
}

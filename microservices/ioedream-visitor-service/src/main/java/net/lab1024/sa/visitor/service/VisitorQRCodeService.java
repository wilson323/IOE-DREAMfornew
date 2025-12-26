package net.lab1024.sa.visitor.service;

import java.awt.image.BufferedImage;

/**
 * 访客二维码服务
 * <p>
 * 提供访客二维码的生成、验证和缓存功能：
 * 1. 二维码生成（优化算法，提升生成速度）
 * 2. 二维码验证（离线验证支持，防重放攻击）
 * 3. 本地缓存（减少重复生成开销）
 * 4. 安全增强（AES-256加密，签名验证）
 * </p>
 * <p>
 * 性能要求：
 * - 生成速度: &lt;100ms
 * - 验证速度: &lt;200ms
 * - 加密强度: AES-256-GCM
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface VisitorQRCodeService {

    /**
     * 生成访客二维码
     * <p>
     * 基于访客预约信息生成二维码，包含：
     * - 访客ID
     * - 预约ID
     * - 有效期
     * - 签名（防篡改）
     * </p>
     *
     * @param appointmentId 预约ID
     * @param visitorId     访客ID
     * @param expireSeconds 有效期（秒）
     * @return 二维码图片（BufferedImage）
     */
    BufferedImage generateQRCode(Long appointmentId, Long visitorId, int expireSeconds);

    /**
     * 生成访客二维码（Base64编码）
     * <p>
     * 生成二维码并返回Base64编码字符串，便于前端展示
     * </p>
     *
     * @param appointmentId 预约ID
     * @param visitorId     访客ID
     * @param expireSeconds 有效期（秒）
     * @return Base64编码的二维码图片
     */
    String generateQRCodeBase64(Long appointmentId, Long visitorId, int expireSeconds);

    /**
     * 验证访客二维码（离线验证）
     * <p>
     * 离线验证二维码，支持：
     * - 签名验证（防篡改）
     * - 有效期验证
     * - 防重放攻击（nonce检查）
     * - 本地缓存验证（减少数据库查询）
     * </p>
     *
     * @param qrCodeData 二维码数据（加密的JSON字符串）
     * @return 验证结果
     */
    QRCodeVerificationResult verifyQRCode(String qrCodeData);

    /**
     * 生成加密的二维码数据
     * <p>
     * 生成二维码的加密数据，包含：
     * - AES-256-GCM加密
     * - HMAC-SHA256签名
     * - Base64编码
     * </p>
     *
     * @param appointmentId 预约ID
     * @param visitorId     访客ID
     * @param expireSeconds 有效期（秒）
     * @return 加密的二维码数据
     */
    String generateEncryptedQRData(Long appointmentId, Long visitorId, int expireSeconds);

    /**
     * 清除过期的二维码缓存
     * <p>
     * 定时清理过期的二维码缓存，释放内存
     * </p>
     *
     * @return 清除的缓存数量
     */
    int clearExpiredCache();

    /**
     * 获取二维码缓存统计信息
     *
     * @return 缓存统计信息
     */
    QRCodeCacheStats getCacheStats();

    /**
     * 二维码验证结果
     */
    class QRCodeVerificationResult {
        private boolean valid;
        private Long appointmentId;
        private Long visitorId;
        private String message;
        private Long expireTime;

        public static QRCodeVerificationResult success(Long appointmentId, Long visitorId, Long expireTime) {
            QRCodeVerificationResult result = new QRCodeVerificationResult();
            result.valid = true;
            result.appointmentId = appointmentId;
            result.visitorId = visitorId;
            result.expireTime = expireTime;
            result.message = "验证通过";
            return result;
        }

        public static QRCodeVerificationResult failure(String message) {
            QRCodeVerificationResult result = new QRCodeVerificationResult();
            result.valid = false;
            result.message = message;
            return result;
        }

        public boolean isValid() {
            return valid;
        }

        public Long getAppointmentId() {
            return appointmentId;
        }

        public Long getVisitorId() {
            return visitorId;
        }

        public String getMessage() {
            return message;
        }

        public Long getExpireTime() {
            return expireTime;
        }
    }

    /**
     * 二维码缓存统计信息
     */
    class QRCodeCacheStats {
        private int cacheSize;
        private long totalGenerated;
        private long totalVerified;
        private int expiredCount;
        private double hitRate;

        public int getCacheSize() {
            return cacheSize;
        }

        public void setCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
        }

        public long getTotalGenerated() {
            return totalGenerated;
        }

        public void setTotalGenerated(long totalGenerated) {
            this.totalGenerated = totalGenerated;
        }

        public long getTotalVerified() {
            return totalVerified;
        }

        public void setTotalVerified(long totalVerified) {
            this.totalVerified = totalVerified;
        }

        public int getExpiredCount() {
            return expiredCount;
        }

        public void setExpiredCount(int expiredCount) {
            this.expiredCount = expiredCount;
        }

        public double getHitRate() {
            return hitRate;
        }

        public void setHitRate(double hitRate) {
            this.hitRate = hitRate;
        }
    }
}

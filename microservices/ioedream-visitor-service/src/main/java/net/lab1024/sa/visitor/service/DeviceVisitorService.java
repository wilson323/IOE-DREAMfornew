package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.dto.ResponseDTO;

import java.time.LocalDateTime;

/**
 * 设备访客验证服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service层负责业务逻辑和事务管理
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 * <p>
 * 核心职责：
 * - 临时访客中心验证
 * - 常客边缘验证
 * - 访客验证结果处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface DeviceVisitorService {

    /**
     * 临时访客中心验证
     * <p>
     * ⭐ 临时访客必须中心验证
     * ⭐ 软件端验证：预约状态、时间范围、人脸验证
     * ⭐ 生成临时模板并下发到设备
     * </p>
     *
     * @param appointmentId 预约ID
     * @param biometricData 生物特征数据
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<VisitorVerificationResult> verifyTemporaryVisitor(Long appointmentId, String biometricData, Long deviceId);

    /**
     * 常客边缘验证
     * <p>
     * ⭐ 常客支持边缘验证
     * ⭐ 设备端验证：本地模板比对
     * ⭐ 软件端记录：上传验证结果
     * </p>
     *
     * @param passId 电子通行证ID
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<VisitorVerificationResult> verifyRegularVisitor(String passId, Long deviceId);

    /**
     * 访客验证结果
     */
    class VisitorVerificationResult {
        private boolean success;
        private String templateId;
        private String passId;
        private LocalDateTime expireTime;
        private String message;

        public static VisitorVerificationResultBuilder builder() {
            return new VisitorVerificationResultBuilder();
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        public String getPassId() { return passId; }
        public void setPassId(String passId) { this.passId = passId; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public static class VisitorVerificationResultBuilder {
            private boolean success;
            private String templateId;
            private String passId;
            private LocalDateTime expireTime;
            private String message;

            public VisitorVerificationResultBuilder success(boolean success) {
                this.success = success;
                return this;
            }

            public VisitorVerificationResultBuilder templateId(String templateId) {
                this.templateId = templateId;
                return this;
            }

            public VisitorVerificationResultBuilder passId(String passId) {
                this.passId = passId;
                return this;
            }

            public VisitorVerificationResultBuilder expireTime(LocalDateTime expireTime) {
                this.expireTime = expireTime;
                return this;
            }

            public VisitorVerificationResultBuilder message(String message) {
                this.message = message;
                return this;
            }

            public VisitorVerificationResult build() {
                VisitorVerificationResult result = new VisitorVerificationResult();
                result.setSuccess(this.success);
                result.setTemplateId(this.templateId);
                result.setPassId(this.passId);
                result.setExpireTime(this.expireTime);
                result.setMessage(this.message);
                return result;
            }
        }
    }
}

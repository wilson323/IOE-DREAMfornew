package net.lab1024.sa.visitor.strategy;

/**
 * 访客验证策略接口
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用策略模式实现不同的访客验证策略：
 * - 临时访客策略（中心验证）
 * - 常客策略（边缘验证）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IVisitorVerificationStrategy {

    /**
     * 策略名称
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 验证访客
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 根据访客类型和验证数据验证访客身份
     * </p>
     *
     * @param visitorId 访客ID
     * @param verificationData 验证数据
     * @return 验证结果
     */
    VisitorVerificationResult verify(Long visitorId, String verificationData);

    /**
     * 获取策略优先级
     * <p>
     * 用于策略工厂排序，优先级高的策略优先使用
     * </p>
     *
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 获取策略类型
     * <p>
     * 用于策略工厂识别策略类型
     * </p>
     *
     * @return 策略类型标识
     */
    String getStrategyType();

    /**
     * 访客验证结果
     */
    class VisitorVerificationResult {
        private final boolean success;
        private final String message;
        private final String templateId;
        private final String passId;

        private VisitorVerificationResult(boolean success, String message, String templateId, String passId) {
            this.success = success;
            this.message = message;
            this.templateId = templateId;
            this.passId = passId;
        }

        public static VisitorVerificationResult success(String templateId, String passId) {
            return new VisitorVerificationResult(true, "验证成功", templateId, passId);
        }

        public static VisitorVerificationResult failed(String message) {
            return new VisitorVerificationResult(false, message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getTemplateId() {
            return templateId;
        }

        public String getPassId() {
            return passId;
        }
    }
}

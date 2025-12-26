package net.lab1024.sa.consume.strategy;

import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.consume.entity.PosidAccountEntity;
import net.lab1024.sa.consume.entity.PosidAreaEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费模式策略接口
 *
 * 职责：定义6种消费模式的计算逻辑
 *
 * 支持的消费模式：
 * - FIXED_AMOUNT: 固定金额模式
 * - FREE_AMOUNT: 自由金额模式
 * - METERED: 计量计费模式
 * - PRODUCT: 商品模式
 * - ORDER: 订餐模式
 * - INTELLIGENCE: 智能模式
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
public interface ConsumeModeStrategy {

    /**
     * 获取消费模式类型
     *
     * @return 消费模式类型
     */
    String getModeType();

    /**
     * 计算消费金额
     *
     * @param account 账户信息
     * @param area 区域信息
     * @param modeConfig 模式配置
     * @param params 业务参数（如用户输入金额、商品列表等）
     * @return 计算结果
     */
    ConsumeResult calculateAmount(PosidAccountEntity account,
                                  PosidAreaEntity area,
                                  ModeConfig modeConfig,
                                  Map<String, Object> params);

    /**
     * 验证消费参数
     *
     * @param params 业务参数
     * @param modeConfig 模式配置
     * @return 是否有效
     */
    boolean validateParams(Map<String, Object> params, ModeConfig modeConfig);

    /**
     * 消费结果
     */
    class ConsumeResult {
        private BigDecimal amount;
        private boolean success;
        private String message;
        private Map<String, Object> details;

        public static ConsumeResult success(BigDecimal amount, String message) {
            ConsumeResult result = new ConsumeResult();
            result.setAmount(amount);
            result.setSuccess(true);
            result.setMessage(message);
            return result;
        }

        public static ConsumeResult failure(String message) {
            ConsumeResult result = new ConsumeResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }
    }
}

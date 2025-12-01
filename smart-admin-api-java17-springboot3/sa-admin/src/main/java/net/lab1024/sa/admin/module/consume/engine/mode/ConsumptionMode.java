package net.lab1024.sa.admin.module.consume.engine.mode;

import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费模式基础接口
 * 定义所有消费模式必须实现的基础方法
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
public interface ConsumptionMode {

    /**
     * 获取模式代码
     *
     * @return 模式代码（如：FIXED_AMOUNT, FREE_AMOUNT等）
     */
    String getModeCode();

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    String getModeName();

    /**
     * 获取模式描述
     *
     * @return 模式描述
     */
    String getDescription();

    /**
     * 处理消费请求
     *
     * @param request 消费请求
     * @return 消费结果
     */
    ConsumeResult process(ConsumeRequest request);

    /**
     * 验证消费请求
     *
     * @param request 消费请求
     * @return 验证结果
     */
    ValidationResult validate(ConsumeRequest request);

    /**
     * 计算消费金额
     *
     * @param request 消费请求
     * @return 计算结果
     */
    BigDecimal calculateAmount(ConsumeRequest request);

    /**
     * 获取模式配置模板
     *
     * @return 配置模板
     */
    Map<String, Object> getConfigTemplate();

    /**
     * 验证模式配置
     *
     * @param config 配置数据
     * @return 验证结果
     */
    boolean validateConfig(Map<String, Object> config);

    /**
     * 检查模式是否适用于指定设备类型
     *
     * @param deviceType 设备类型
     * @return 是否适用
     */
    boolean isApplicableTo(String deviceType);

    /**
     * 获取支持的设备类型
     *
     * @return 支持的设备类型列表
     */
    String[] getSupportedDeviceTypes();

    /**
     * 检查模式是否支持离线模式
     *
     * @return 是否支持离线
     */
    boolean supportsOfflineMode();

    /**
     * 检查模式是否需要网络连接
     *
     * @return 是否需要网络
     */
    boolean requiresNetwork();

    /**
     * 获取模式优先级
     * 数值越大优先级越高
     *
     * @return 优先级
     */
    int getPriority();

    /**
     * 初始化模式
     *
     * @param config 配置数据
     */
    void initialize(Map<String, Object> config);

    /**
     * 销毁模式
     */
    void destroy();

    /**
     * 验证结果类
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorCode;
        private final String errorMessage;

        public ValidationResult(boolean valid) {
            this(valid, null, null);
        }

        public ValidationResult(boolean valid, String errorCode, String errorMessage) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true);
        }

        public static ValidationResult failure(String errorCode, String errorMessage) {
            return new ValidationResult(false, errorCode, errorMessage);
        }
    }
}
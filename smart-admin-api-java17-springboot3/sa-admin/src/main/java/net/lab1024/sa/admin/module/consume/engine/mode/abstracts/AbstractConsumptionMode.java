package net.lab1024.sa.admin.module.consume.engine.mode.abstracts;

import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.base.common.util.SmartStringUtil;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * 消费模式抽象基类
 * 严格遵循repowiki规范：提供消费模式的通用实现，减少代码重复
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
public abstract class AbstractConsumptionMode implements ConsumptionMode {

    protected final String modeId;
    protected final String modeName;
    protected final String description;

    protected AbstractConsumptionMode(String modeId, String modeName, String description) {
        this.modeId = modeId;
        this.modeName = modeName;
        this.description = description;
    }

    @Override
    public String getModeCode() {
        return modeId;
    }

    // Note: getModeId() is provided for backward compatibility
    public String getModeId() {
        return modeId;
    }

    @Override
    public String getModeName() {
        return modeName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ConsumeResult process(ConsumeRequest request) {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 process(ConsumeRequest) 方法");
    }

    @Override
    public ConsumptionMode.ValidationResult validate(ConsumeRequest request) {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 validate(ConsumeRequest) 方法");
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequest request) {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 calculateAmount(ConsumeRequest) 方法");
    }

    @Override
    public Map<String, Object> getConfigTemplate() {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 getConfigTemplate() 方法");
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 validateConfig(Map) 方法");
    }

    @Override
    public boolean isApplicableTo(String deviceType) {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 isApplicableTo(String) 方法");
    }

    @Override
    public String[] getSupportedDeviceTypes() {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 getSupportedDeviceTypes() 方法");
    }

    @Override
    public boolean supportsOfflineMode() {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 supportsOfflineMode() 方法");
    }

    @Override
    public boolean requiresNetwork() {
        // 子类必须实现
        throw new UnsupportedOperationException("子类必须实现 requiresNetwork() 方法");
    }

    @Override
    public int getPriority() {
        // 默认优先级
        return 0;
    }

    @Override
    public void initialize(Map<String, Object> config) {
        // 子类可重写
    }

    @Override
    public void destroy() {
        // 子类可重写
    }

    public boolean validateParameters(Map<String, Object> params) {
        try {
            // 基础参数验证
            if (params == null || params.isEmpty()) {
                return false;
            }

            // 金额参数验证
            Object amountObj = params.get("amount");
            if (amountObj != null) {
                if (amountObj instanceof BigDecimal) {
                    return ((BigDecimal) amountObj).compareTo(BigDecimal.ZERO) > 0;
                } else if (amountObj instanceof Number) {
                    return ((Number) amountObj).doubleValue() > 0;
                }
            }

            // 调用子类特定验证
            return doValidateParameters(params);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAllowed(Map<String, Object> params) {
        try {
            // 基础权限检查
            if (!validateParameters(params)) {
                return false;
            }

            // 调用子类特定权限检查
            return doIsAllowed(params);
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getLimits() {
        Map<String, Object> limits = new HashMap<>();
        limits.put("minAmount", getMinAmount());
        limits.put("maxAmount", getMaxAmount());
        limits.put("supportedFields", getSupportedFields());
        return limits;
    }

    public Map<String, Object> preProcess(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("processed", true);
        result.put("timestamp", System.currentTimeMillis());

        // 调用子类特定预处理
        Map<String, Object> customResult = doPreProcess(params);
        if (customResult != null) {
            result.putAll(customResult);
        }

        return result;
    }

    public Map<String, Object> postProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new HashMap<>();
        postResult.put("processed", true);
        postResult.put("timestamp", System.currentTimeMillis());

        // 调用子类特定后处理
        Map<String, Object> customResult = doPostProcess(params, result);
        if (customResult != null) {
            postResult.putAll(customResult);
        }

        return postResult;
    }

    /**
     * 子类必须实现的参数验证逻辑
     */
    protected abstract boolean doValidateParameters(Map<String, Object> params);

    /**
     * 子类必须实现的权限检查逻辑
     */
    protected abstract boolean doIsAllowed(Map<String, Object> params);

    /**
     * 获取最小金额限制（子类可重写）
     */
    protected BigDecimal getMinAmount() {
        return BigDecimal.ZERO;
    }

    /**
     * 获取最大金额限制（子类可重写）
     */
    protected BigDecimal getMaxAmount() {
        return new BigDecimal("9999.99");
    }

    /**
     * 获取支持的字段列表（子类可重写）
     */
    protected String[] getSupportedFields() {
        return new String[]{"amount", "accountId", "deviceId"};
    }

    /**
     * 子类可重写的预处理逻辑
     */
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        return null;
    }

    /**
     * 子类可重写的后处理逻辑
     */
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        return null;
    }

    /**
     * 从参数中获取金额
     */
    protected BigDecimal getAmountFromParams(Map<String, Object> params) {
        Object amountObj = params.get("amount");
        if (amountObj instanceof BigDecimal) {
            return (BigDecimal) amountObj;
        } else if (amountObj instanceof Number) {
            return BigDecimal.valueOf(((Number) amountObj).doubleValue());
        } else if (amountObj instanceof String) {
            try {
                return new BigDecimal((String) amountObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从参数中获取字符串值
     */
    protected String getStringFromParams(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return value != null ? value.toString() : null;
    }

    /**
     * 从参数中获取整数值
     */
    protected Integer getIntegerFromParams(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 验证金额是否在允许范围内
     */
    protected boolean isAmountInRange(BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        BigDecimal min = getMinAmount();
        BigDecimal max = getMaxAmount();

        return amount.compareTo(min) >= 0 && amount.compareTo(max) <= 0;
    }

    /**
     * 检查必填参数
     */
    protected boolean hasRequiredParams(Map<String, Object> params, String... requiredKeys) {
        if (params == null || requiredKeys == null) {
            return false;
        }

        for (String key : requiredKeys) {
            if (!params.containsKey(key) || params.get(key) == null) {
                return false;
            }

            Object value = params.get(key);
            if (value instanceof String && SmartStringUtil.isBlank((String) value)) {
                return false;
            }
        }

        return true;
    }
}
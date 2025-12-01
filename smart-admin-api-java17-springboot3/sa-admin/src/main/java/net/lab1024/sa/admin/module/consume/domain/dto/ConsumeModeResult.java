package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * 消费模式结果DTO
 * 严格遵循repowiki规范：数据传输对象，包含消费模式处理的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeModeResult {

    /**
     * 消费模式
     */
    private String consumeMode;

    /**
     * 消费模式名称
     */
    private String modeName;

    /**
     * 计算后的金额
     */
    private BigDecimal amount;

    /**
     * 原始金额
     */
    private BigDecimal originalAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 补贴金额
     */
    private BigDecimal subsidyAmount;

    /**
     * 预处理结果
     */
    private Map<String, Object> preProcessResult;

    /**
     * 后处理结果
     */
    private Map<String, Object> postProcessResult;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 处理消息
     */
    private String message;

    /**
     * 处理时间（毫秒）
     */
    private Long processTime;

    /**
     * 创建成功结果
     */
    public static ConsumeModeResult success(String consumeMode, BigDecimal amount) {
        return ConsumeModeResult.builder()
                .consumeMode(consumeMode)
                .amount(amount)
                .originalAmount(amount)
                .success(true)
                .message("模式处理成功")
                .processTime(System.currentTimeMillis())
                .preProcessResult(new HashMap<>())
                .postProcessResult(new HashMap<>())
                .build();
    }

    /**
     * 创建成功结果（包含模式名称）
     */
    public static ConsumeModeResult success(String consumeMode, String modeName, BigDecimal amount) {
        ConsumeModeResult result = success(consumeMode, amount);
        result.setModeName(modeName);
        return result;
    }

    /**
     * 创建成功结果（包含完整信息）
     */
    public static ConsumeModeResult success(String consumeMode, String modeName,
                                           BigDecimal amount, BigDecimal originalAmount,
                                           BigDecimal discountAmount, BigDecimal subsidyAmount) {
        return ConsumeModeResult.builder()
                .consumeMode(consumeMode)
                .modeName(modeName)
                .amount(amount)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .subsidyAmount(subsidyAmount)
                .success(true)
                .message("模式处理成功")
                .processTime(System.currentTimeMillis())
                .preProcessResult(new HashMap<>())
                .postProcessResult(new HashMap<>())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ConsumeModeResult failure(String consumeMode, String message) {
        return ConsumeModeResult.builder()
                .consumeMode(consumeMode)
                .success(false)
                .message(message)
                .processTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * 检查是否失败
     */
    public boolean isFailure() {
        return Boolean.FALSE.equals(success);
    }

    /**
     * 添加预处理结果
     */
    public void addPreProcessResult(String key, Object value) {
        if (preProcessResult == null) {
            preProcessResult = new HashMap<>();
        }
        preProcessResult.put(key, value);
    }

    /**
     * 添加后处理结果
     */
    public void addPostProcessResult(String key, Object value) {
        if (postProcessResult == null) {
            postProcessResult = new HashMap<>();
        }
        postProcessResult.put(key, value);
    }

    /**
     * 获取总优惠金额
     */
    public BigDecimal getTotalDiscountAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (discountAmount != null) {
            total = total.add(discountAmount);
        }
        if (subsidyAmount != null) {
            total = total.add(subsidyAmount);
        }
        return total;
    }

    /**
     * 获取格式化的金额
     */
    public String getFormattedAmount() {
        return amount != null ? amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }

    /**
     * 获取格式化的原始金额
     */
    public String getFormattedOriginalAmount() {
        return originalAmount != null ? originalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }

    /**
     * 获取格式化的总优惠金额
     */
    public String getFormattedTotalDiscountAmount() {
        return getTotalDiscountAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
package net.lab1024.sa.admin.module.consume.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费模式结果对象
 * 包含消费模式处理的返回结果
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeModeResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 结果码
     */
    private String code;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 模式代码
     */
    private String modeCode;

    /**
     * 最终消费金额
     */
    private BigDecimal finalAmount;

    /**
     * 原始金额
     */
    private BigDecimal originalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 模式特定结果数据
     */
    private Map<String, Object> modeData;

    /**
     * 扩展信息
     */
    private String extendInfo;

    /**
     * 创建成功结果
     */
    public static ConsumeModeResult success(String modeCode, BigDecimal finalAmount) {
        return ConsumeModeResult.builder()
                .success(true)
                .code("SUCCESS")
                .message("处理成功")
                .modeCode(modeCode)
                .finalAmount(finalAmount)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ConsumeModeResult failure(String code, String message) {
        return ConsumeModeResult.builder()
                .success(false)
                .code(code)
                .message(message)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建验证失败结果
     */
    public static ConsumeModeResult validationFailure(String message) {
        return ConsumeModeResult.builder()
                .success(false)
                .code("VALIDATION_FAILED")
                .message(message)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建带优惠的成功结果
     */
    public static ConsumeModeResult successWithDiscount(String modeCode, BigDecimal originalAmount,
                                                      BigDecimal finalAmount, BigDecimal discountAmount) {
        return ConsumeModeResult.builder()
                .success(true)
                .code("SUCCESS")
                .message("处理成功")
                .modeCode(modeCode)
                .originalAmount(originalAmount)
                .finalAmount(finalAmount)
                .discountAmount(discountAmount)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建带手续费的結果
     */
    public static ConsumeModeResult successWithFee(String modeCode, BigDecimal finalAmount, BigDecimal feeAmount) {
        return ConsumeModeResult.builder()
                .success(true)
                .code("SUCCESS")
                .message("处理成功")
                .modeCode(modeCode)
                .finalAmount(finalAmount)
                .feeAmount(feeAmount)
                .processTime(LocalDateTime.now())
                .build();
    }

    /**
     * 设置模式数据
     */
    public void setModeData(String key, Object value) {
        if (this.modeData == null) {
            this.modeData = new java.util.HashMap<>();
        }
        this.modeData.put(key, value);
    }

    /**
     * 获取模式数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getModeData(String key, Class<T> type) {
        if (modeData == null || !modeData.containsKey(key)) {
            return null;
        }

        Object value = modeData.get(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        throw new IllegalArgumentException("无法将模式数据 " + key + " 转换为类型 " + type.getSimpleName());
    }
}
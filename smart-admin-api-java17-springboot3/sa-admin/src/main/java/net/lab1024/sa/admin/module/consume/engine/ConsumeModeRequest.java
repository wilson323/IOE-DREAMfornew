package net.lab1024.sa.admin.module.consume.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费模式请求对象
 * 包含消费模式处理的通用参数
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeModeRequest {

    /**
     * 人员ID
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 消费金额
     */
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    /**
     * 消费模式代码
     */
    private String modeCode;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 区域ID
     */
    private String regionId;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 账户类型
     */
    private String accountType;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 模式配置参数
     * 根据不同的消费模式包含不同的参数
     */
    private Map<String, Object> modeParams;

    /**
     * 扩展数据
     */
    private String extendData;

    /**
     * 创建固定金额模式请求
     */
    public static ConsumeModeRequest forFixedAmount(Long personId, BigDecimal amount, Long deviceId, String orderNo) {
        return ConsumeModeRequest.builder()
                .personId(personId)
                .amount(amount)
                .deviceId(deviceId)
                .orderNo(orderNo)
                .modeCode("FIXED_AMOUNT")
                .build();
    }

    /**
     * 创建自由金额模式请求
     */
    public static ConsumeModeRequest forFreeAmount(Long personId, BigDecimal amount, Long deviceId, String orderNo) {
        return ConsumeModeRequest.builder()
                .personId(personId)
                .amount(amount)
                .deviceId(deviceId)
                .orderNo(orderNo)
                .modeCode("FREE_AMOUNT")
                .build();
    }

    /**
     * 创建计量模式请求
     */
    public static ConsumeModeRequest forMetering(Long personId, Long deviceId, String orderNo,
                                                 BigDecimal unitPrice, BigDecimal quantity, String unit) {
        Map<String, Object> params = Map.of(
                "unitPrice", unitPrice,
                "quantity", quantity,
                "unit", unit
        );

        return ConsumeModeRequest.builder()
                .personId(personId)
                .deviceId(deviceId)
                .orderNo(orderNo)
                .modeCode("METERING")
                .modeParams(params)
                .build();
    }

    /**
     * 创建商品模式请求
     */
    public static ConsumeModeRequest forProduct(Long personId, Long deviceId, String orderNo,
                                               String productCode, Integer quantity) {
        Map<String, Object> params = Map.of(
                "productCode", productCode,
                "quantity", quantity
        );

        return ConsumeModeRequest.builder()
                .personId(personId)
                .deviceId(deviceId)
                .orderNo(orderNo)
                .modeCode("PRODUCT")
                .modeParams(params)
                .build();
    }

    /**
     * 创建订餐模式请求
     */
    public static ConsumeModeRequest forOrdering(Long personId, Long deviceId, String orderNo,
                                                 String mealType, String date) {
        Map<String, Object> params = Map.of(
                "mealType", mealType,
                "date", date
        );

        return ConsumeModeRequest.builder()
                .personId(personId)
                .deviceId(deviceId)
                .orderNo(orderNo)
                .modeCode("ORDERING")
                .modeParams(params)
                .build();
    }

    /**
     * 获取模式参数
     */
    @SuppressWarnings("unchecked")
    public <T> T getModeParam(String key, Class<T> type) {
        if (modeParams == null || !modeParams.containsKey(key)) {
            return null;
        }

        Object value = modeParams.get(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        // 类型转换处理
        if (type == BigDecimal.class && value instanceof Number) {
            return (T) BigDecimal.valueOf(((Number) value).doubleValue());
        }

        if (type == String.class) {
            return (T) value.toString();
        }

        throw new IllegalArgumentException("无法将参数 " + key + " 转换为类型 " + type.getSimpleName());
    }

    /**
     * 获取模式参数（带默认值）
     */
    public <T> T getModeParam(String key, Class<T> type, T defaultValue) {
        T value = getModeParam(key, type);
        return value != null ? value : defaultValue;
    }
}
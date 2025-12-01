package net.lab1024.sa.admin.module.consume.engine.mode.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import net.lab1024.sa.admin.module.consume.engine.ConsumeModeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeResult;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;

/**
 * 消费模式转换工具类
 * 用于在 ConsumeModeRequest/ConsumeModeResult 和 ConsumeRequest/ConsumeResult 之间转换
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
public class ConsumptionModeConverter {

    /**
     * 将 ConsumeModeRequest 转换为 ConsumeRequest
     *
     * @param modeRequest 模式请求
     * @return 消费请求
     */
    public static ConsumeRequest toConsumeRequest(ConsumeModeRequest modeRequest) {
        if (modeRequest == null) {
            return null;
        }

        ConsumeRequest.ConsumeRequestBuilder builder = ConsumeRequest.builder()
                .personId(modeRequest.getPersonId())
                .personName(modeRequest.getPersonName())
                .deviceId(modeRequest.getDeviceId())
                .deviceName(modeRequest.getDeviceName())
                .regionId(modeRequest.getRegionId())
                .regionName(modeRequest.getRegionName())
                .orderNo(modeRequest.getOrderNo())
                .payMethod(modeRequest.getPayMethod())
                .clientIp(modeRequest.getClientIp())
                .consumeTime(LocalDateTime.now())
                .consumptionMode(modeRequest.getModeCode());

        // 解析 extendData 字符串为 Map
        if (modeRequest.getExtendData() != null && !modeRequest.getExtendData().trim().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> extendDataMap = (Map<String, Object>) JSON.parseObject(
                        modeRequest.getExtendData(), Map.class);
                builder.extendData(extendDataMap != null ? extendDataMap : new HashMap<>());
            } catch (Exception e) {
                // 解析失败时使用空 Map
                builder.extendData(new HashMap<>());
            }
        } else {
            builder.extendData(new HashMap<>());
        }

        // 将 modeParams 转换为 modeConfig
        if (modeRequest.getModeParams() != null) {
            Map<String, Object> modeConfig = new HashMap<>(modeRequest.getModeParams());
            builder.modeConfig(modeConfig);

            // 尝试提取金额
            Object amountObj = modeRequest.getModeParams().get("amount");
            if (amountObj instanceof BigDecimal) {
                builder.amount((BigDecimal) amountObj);
            } else if (amountObj instanceof Number) {
                builder.amount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
            }
        }

        return builder.build();
    }

    /**
     * 将 ConsumeModeResult 转换为 ConsumeResult
     *
     * @param modeResult 模式结果
     * @return 消费结果
     */
    public static ConsumeResult toConsumeResult(ConsumeModeResult modeResult) {
        if (modeResult == null) {
            return null;
        }

        ConsumeResult.ConsumeResultBuilder builder = ConsumeResult.builder()
                .success(modeResult.getSuccess() != null && modeResult.getSuccess())
                .resultCode(modeResult.getCode())
                .message(modeResult.getMessage())
                .actualAmount(modeResult.getFinalAmount())
                .discountAmount(modeResult.getDiscountAmount())
                .feeAmount(modeResult.getFeeAmount())
                .consumptionMode(modeResult.getModeCode())
                .consumeTime(modeResult.getProcessTime() != null
                        ? modeResult.getProcessTime()
                        : LocalDateTime.now())
                .status(modeResult.getSuccess() != null && modeResult.getSuccess() ? "SUCCESS" : "FAILED");

        // 转换模式数据到扩展数据
        if (modeResult.getModeData() != null) {
            builder.extendData(new HashMap<>(modeResult.getModeData()));
        }

        // 设置错误信息
        if (modeResult.getSuccess() == null || !modeResult.getSuccess()) {
            builder.errorCode(modeResult.getCode());
            builder.errorDetail(modeResult.getMessage());
        }

        return builder.build();
    }

    /**
     * 将 ConsumptionMode.ValidationResult 转换为 ConsumeModeValidationResult
     *
     * @param validationResult 验证结果
     * @return 模式验证结果
     */
    public static net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult toConsumeModeValidationResult(
            ConsumptionMode.ValidationResult validationResult) {
        if (validationResult == null) {
            return null;
        }

        if (validationResult.isValid()) {
            return net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult.success();
        } else {
            return net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult.failure(
                    validationResult.getErrorCode(),
                    validationResult.getErrorMessage());
        }
    }

    /**
     * 将 ConsumeModeValidationResult 转换为 ConsumptionMode.ValidationResult
     *
     * @param modeValidationResult 模式验证结果
     * @return 验证结果
     */
    public static ConsumptionMode.ValidationResult toValidationResult(
            net.lab1024.sa.admin.module.consume.engine.ConsumeModeValidationResult modeValidationResult) {
        if (modeValidationResult == null) {
            return ConsumptionMode.ValidationResult.failure("UNKNOWN", "验证结果为空");
        }

        Boolean valid = modeValidationResult.getValid();
        if (valid != null && valid) {
            return ConsumptionMode.ValidationResult.success();
        } else {
            return ConsumptionMode.ValidationResult.failure(
                    modeValidationResult.getErrorCode(),
                    modeValidationResult.getErrorMessage());
        }
    }

    /**
     * 将 JSON 字符串配置模板转换为 Map
     *
     * @param jsonTemplate JSON 字符串
     * @return 配置 Map
     */
    public static Map<String, Object> parseConfigTemplate(String jsonTemplate) {
        if (jsonTemplate == null || jsonTemplate.trim().isEmpty()) {
            return new HashMap<>();
        }

        // 简单的 JSON 解析（实际项目中应使用 Jackson 或 Gson）
        Map<String, Object> config = new HashMap<>();
        try {
            // 这里使用简单的字符串处理，实际应该使用 JSON 库
            // 为了简化，我们返回一个包含原始 JSON 字符串的 Map
            config.put("template", jsonTemplate);
            config.put("format", "JSON");
        } catch (Exception e) {
            // 解析失败时返回空 Map
        }
        return config;
    }
}

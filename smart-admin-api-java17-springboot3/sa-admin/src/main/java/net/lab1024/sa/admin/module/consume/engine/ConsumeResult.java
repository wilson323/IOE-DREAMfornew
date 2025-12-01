package net.lab1024.sa.admin.module.consume.engine;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费结果类
 * 封装消费处理的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果代码
     */
    private String resultCode;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 错误代码（失败时使用）
     */
    private String errorCode;

    /**
     * 错误详情（失败时使用）
     */
    private String errorDetail;

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 第三方订单号
     */
    private String thirdPartyOrderNo;

    /**
     * 交易流水号
     */
    private String transactionId;

    /**
     * 实际消费金额
     */
    private BigDecimal actualAmount;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 消费前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 消费后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 补贴金额
     */
    private BigDecimal subsidyAmount;

    /**
     * 消费模式
     */
    private String consumptionMode;

    /**
     * 餐别（用于固定金额模式）
     */
    private String mealPeriod;

    /**
     * 是否为固定金额消费
     */
    private Boolean fixedAmount;

    /**
     * 补贴类型
     */
    private String subsidyType;

    /**
     * 补贴规则
     */
    private String subsidyRule;

    /**
     * 最终金额（扣除补贴后）
     */
    private BigDecimal finalAmount;

    /**
     * 获得积分
     */
    private Integer pointsEarned;

    /**
     * 使用积分
     */
    private Integer pointsUsed;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 处理时长（毫秒）
     */
    private Long processingTime;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 消费状态
     */
    private String status;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 扩展数据
     */
    private Map<String, Object> extendData;

    /**
     * 建议操作（失败时使用）
     */
    private String suggestedAction;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否可以重试
     */
    private Boolean retryable;

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static ConsumeResult success() {
        return ConsumeResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .message("消费成功")
                .status("SUCCESS")
                .build();
    }

    /**
     * 创建成功结果（带金额）
     *
     * @param actualAmount 实际消费金额
     * @return 成功结果
     */
    public static ConsumeResult success(BigDecimal actualAmount) {
        return ConsumeResult.builder()
                .success(true)
                .resultCode("SUCCESS")
                .message("消费成功")
                .actualAmount(actualAmount)
                .status("SUCCESS")
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @return 失败结果
     */
    public static ConsumeResult failure(String errorCode, String message) {
        return ConsumeResult.builder()
                .success(false)
                .resultCode("FAILURE")
                .errorCode(errorCode)
                .message(message)
                .status("FAILED")
                .build();
    }

    /**
     * 创建失败结果（带详情）
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param errorDetail 错误详情
     * @return 失败结果
     */
    public static ConsumeResult failure(String errorCode, String message, String errorDetail) {
        return ConsumeResult.builder()
                .success(false)
                .resultCode("FAILURE")
                .errorCode(errorCode)
                .message(message)
                .errorDetail(errorDetail)
                .status("FAILED")
                .build();
    }

    /**
     * 创建待处理结果
     *
     * @param message 处理中消息
     * @return 待处理结果
     */
    public static ConsumeResult pending(String message) {
        return ConsumeResult.builder()
                .success(false)
                .resultCode("PENDING")
                .message(message)
                .status("PENDING")
                .build();
    }

    /**
     * 检查结果是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 检查是否可以重试
     *
     * @return 是否可以重试
     */
    public boolean isRetryable() {
        return retryable != null && retryable && retryCount < 3;
    }

    /**
     * 获取格式化的金额信息
     *
     * @return 金额信息字符串
     */
    public String getAmountInfo() {
        StringBuilder info = new StringBuilder();
        if (actualAmount != null) {
            info.append("消费金额: ").append(actualAmount);
        }
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.append(", 优惠: ").append(discountAmount);
        }
        if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
            info.append(", 手续费: ").append(feeAmount);
        }
        return info.toString();
    }

    /**
     * 获取余额变化信息
     *
     * @return 余额变化信息
     */
    public String getBalanceChangeInfo() {
        if (balanceBefore != null && balanceAfter != null) {
            return String.format("余额: %s -> %s (变化: %s)",
                              balanceBefore, balanceAfter, balanceBefore.subtract(balanceAfter));
        }
        return "余额信息不完整";
    }

    /**
     * 获取处理时间信息
     *
     * @return 处理时间信息
     */
    public String getProcessingTimeInfo() {
        if (processingTime != null) {
            return String.format("处理时长: %dms", processingTime);
        }
        return "处理时间信息不完整";
    }
}

package net.lab1024.sa.admin.module.consume.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费结果对象
 * 核心消费引擎的统一响应结果
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeResult {

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
     * 订单号
     */
    private String orderNo;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 消费后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 消费时间
     */
    private LocalDateTime payTime;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 消费模式
     */
    private String consumptionMode;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 区域ID
     */
    private String regionId;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 账户类型
     */
    private String accountType;

    /**
     * 交易状态
     */
    private String status;

    /**
     * 交易流水号
     */
    private String transactionId;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 扩展信息
     */
    private String extendInfo;

    /**
     * 创建成功结果
     */
    public static ConsumeResult success(String message, String orderNo, Long personId, String personName,
                                       BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        return ConsumeResult.builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .orderNo(orderNo)
                .personId(personId)
                .personName(personName)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .payTime(LocalDateTime.now())
                .status("SUCCESS")
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ConsumeResult failure(String code, String message) {
        return ConsumeResult.builder()
                .success(false)
                .code(code)
                .message(message)
                .payTime(LocalDateTime.now())
                .status("FAILED")
                .build();
    }

    /**
     * 创建余额不足结果
     */
    public static ConsumeResult insufficientBalance(BigDecimal balance) {
        return ConsumeResult.builder()
                .success(false)
                .code("INSUFFICIENT_BALANCE")
                .message("余额不足，当前余额：" + balance)
                .balanceAfter(balance)
                .payTime(LocalDateTime.now())
                .status("FAILED")
                .build();
    }

    /**
     * 创建权限不足结果
     */
    public static ConsumeResult permissionDenied(String message) {
        return ConsumeResult.builder()
                .success(false)
                .code("PERMISSION_DENIED")
                .message(message)
                .payTime(LocalDateTime.now())
                .status("FAILED")
                .build();
    }
}
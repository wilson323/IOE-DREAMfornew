package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账户扣减结果DTO
 * 严格遵循repowiki规范：数据传输对象，包含账户余额扣减的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDeductResult {

    /**
     * 是否扣减成功
     */
    private Boolean success;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 扣减前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 扣减金额
     */
    private BigDecimal deductAmount;

    /**
     * 扣减后余额
     */
    private BigDecimal afterBalance;

    /**
     * 余额变动记录ID
     */
    private Long changeRecordId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 处理消息
     */
    private String message;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 处理时间（毫秒）
     */
    private Long processTime;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 创建成功结果
     */
    public static AccountDeductResult success(Long accountId, BigDecimal beforeBalance,
                                            BigDecimal deductAmount, BigDecimal afterBalance,
                                            Long changeRecordId, String orderNo) {
        return AccountDeductResult.builder()
                .success(true)
                .accountId(accountId)
                .beforeBalance(beforeBalance)
                .deductAmount(deductAmount)
                .afterBalance(afterBalance)
                .changeRecordId(changeRecordId)
                .orderNo(orderNo)
                .message("余额扣减成功")
                .processTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static AccountDeductResult failure(String message, String errorCode) {
        return AccountDeductResult.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .processTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建余额不足结果
     */
    public static AccountDeductResult insufficientBalance(Long accountId, BigDecimal availableBalance,
                                                          BigDecimal deductAmount, String orderNo) {
        return AccountDeductResult.builder()
                .success(false)
                .accountId(accountId)
                .deductAmount(deductAmount)
                .orderNo(orderNo)
                .message(String.format("余额不足，可用余额：%.2f，扣减金额：%.2f", availableBalance, deductAmount))
                .errorCode("INSUFFICIENT_BALANCE")
                .processTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建版本冲突结果
     */
    public static AccountDeductResult versionConflict(Long accountId, String orderNo, Integer currentVersion) {
        return AccountDeductResult.builder()
                .success(false)
                .accountId(accountId)
                .orderNo(orderNo)
                .version(currentVersion)
                .message("数据版本冲突，请重试")
                .errorCode("VERSION_CONFLICT")
                .processTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建账户异常结果
     */
    public static AccountDeductResult accountError(Long accountId, String accountStatus, String orderNo) {
        return AccountDeductResult.builder()
                .success(false)
                .accountId(accountId)
                .orderNo(orderNo)
                .message("账户状态异常: " + accountStatus)
                .errorCode("ACCOUNT_ERROR")
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
     * 检查是否为余额不足
     */
    public boolean isInsufficientBalance() {
        return isFailure() && "INSUFFICIENT_BALANCE".equals(errorCode);
    }

    /**
     * 检查是否为版本冲突
     */
    public boolean isVersionConflict() {
        return isFailure() && "VERSION_CONFLICT".equals(errorCode);
    }

    /**
     * 检查是否为账户错误
     */
    public boolean isAccountError() {
        return isFailure() && "ACCOUNT_ERROR".equals(errorCode);
    }

    /**
     * 获取格式化的扣减前余额
     */
    public String getFormattedBeforeBalance() {
        return beforeBalance != null ? beforeBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }

    /**
     * 获取格式化的扣减金额
     */
    public String getFormattedDeductAmount() {
        return deductAmount != null ? deductAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }

    /**
     * 获取格式化的扣减后余额
     */
    public String getFormattedAfterBalance() {
        return afterBalance != null ? afterBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "0.00";
    }
}
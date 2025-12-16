package net.lab1024.sa.consume.service.integration.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付结果
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public class PaymentResult {

    private String transactionId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;
    private BigDecimal beforeBalance;
    private BigDecimal afterBalance;
    private LocalDateTime transactionTime;
    private PaymentDetails paymentDetails;

    private PaymentResult() {}

    public static PaymentResult success(String transactionId) {
        PaymentResult result = new PaymentResult();
        result.transactionId = transactionId;
        result.status = PaymentStatus.SUCCESS;
        return result;
    }

    public static PaymentResult failure(String failureReason) {
        PaymentResult result = new PaymentResult();
        result.status = PaymentStatus.FAILED;
        result.failureReason = failureReason;
        return result;
    }

    public PaymentResult transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public PaymentResult paymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentResult amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PaymentResult status(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentResult failureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    public PaymentResult beforeBalance(BigDecimal beforeBalance) {
        this.beforeBalance = beforeBalance;
        return this;
    }

    public PaymentResult afterBalance(BigDecimal afterBalance) {
        this.afterBalance = afterBalance;
        return this;
    }

    public PaymentResult transactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
        return this;
    }

    public PaymentResult paymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
        return this;
    }

    public boolean isSuccess() {
        return PaymentStatus.SUCCESS.equals(status);
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public BigDecimal getBeforeBalance() {
        return beforeBalance;
    }

    public void setBeforeBalance(BigDecimal beforeBalance) {
        this.beforeBalance = beforeBalance;
    }

    public BigDecimal getAfterBalance() {
        return afterBalance;
    }

    public void setAfterBalance(BigDecimal afterBalance) {
        this.afterBalance = afterBalance;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
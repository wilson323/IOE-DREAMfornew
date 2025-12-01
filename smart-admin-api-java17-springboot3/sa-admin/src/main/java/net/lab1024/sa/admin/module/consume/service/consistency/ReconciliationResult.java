package net.lab1024.sa.admin.module.consume.service.consistency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 对账结果
 * 严格遵循repowiki规范：定义对账操作的标准返回格式
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "对账结果")
public class ReconciliationResult {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "总账户数")
    private int totalAccounts;

    @Schema(description = "成功账户数")
    private int successAccounts;

    @Schema(description = "差异账户数")
    private int discrepancyCount;

    @Schema(description = "修复数量")
    private int fixCount;

    @Schema(description = "执行时间（毫秒）")
    private long executionTime;

    @Schema(description = "错误信息")
    private List<String> errors = new ArrayList<>();

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "对账模式：FULL-全量, INCREMENTAL-增量, REALTIME-实时")
    private String reconciliationMode;

    @Schema(description = "数据一致性百分比")
    private double consistencyRate;

    @Schema(description = "处理记录数")
    private long processedRecords;

    @Schema(description = "异常记录数")
    private long exceptionRecords;

    // ===== 手动添加getter/setter方法 =====

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(int totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public int getSuccessAccounts() {
        return successAccounts;
    }

    public void setSuccessAccounts(int successAccounts) {
        this.successAccounts = successAccounts;
    }

    public int getDiscrepancyCount() {
        return discrepancyCount;
    }

    public void setDiscrepancyCount(int discrepancyCount) {
        this.discrepancyCount = discrepancyCount;
    }

    public int getFixCount() {
        return fixCount;
    }

    public void setFixCount(int fixCount) {
        this.fixCount = fixCount;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getReconciliationMode() {
        return reconciliationMode;
    }

    public void setReconciliationMode(String reconciliationMode) {
        this.reconciliationMode = reconciliationMode;
    }

    public double getConsistencyRate() {
        return consistencyRate;
    }

    public void setConsistencyRate(double consistencyRate) {
        this.consistencyRate = consistencyRate;
    }

    public long getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(long processedRecords) {
        this.processedRecords = processedRecords;
    }

    public long getExceptionRecords() {
        return exceptionRecords;
    }

    public void setExceptionRecords(long exceptionRecords) {
        this.exceptionRecords = exceptionRecords;
    }

    /**
     * 创建成功结果
     */
    public static ReconciliationResult success() {
        ReconciliationResult result = new ReconciliationResult();
        result.setSuccess(true);
        result.setStartTime(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now());
        return result;
    }

    /**
     * 创建失败结果
     */
    public static ReconciliationResult failure(String error) {
        ReconciliationResult result = new ReconciliationResult();
        result.setSuccess(false);
        result.setStartTime(LocalDateTime.now());
        result.setEndTime(LocalDateTime.now());
        result.getErrors().add(error);
        return result;
    }

    /**
     * 添加错误信息
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * 计算一致性比率
     */
    public double calculateConsistencyRate() {
        if (totalAccounts == 0) {
            return 0.0;
        }
        return (double) successAccounts / totalAccounts * 100;
    }

    /**
     * 完成对账并计算执行时间
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.executionTime = java.time.Duration.between(startTime, endTime).toMillis();
        }
        this.consistencyRate = calculateConsistencyRate();
    }

    /**
     * 获取摘要信息
     */
    public String getSummary() {
        if (success) {
            return String.format("对账成功: 总账户%d, 成功%d, 差异%d, 一致性%.2f%%",
                    totalAccounts, successAccounts, discrepancyCount, consistencyRate);
        } else {
            return String.format("对账失败: %s", String.join("; ", errors));
        }
    }
}
package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费充值记录视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消费充值记录视图对象")
public class ConsumeRechargeRecordVO {

    @Schema(description = "记录ID", example = "1001")
    private Long recordId;

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username; // 保持username字段，添加userName别名方法

    @Schema(description = "用户编号", example = "EMP001")
    private String userCode;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "充值方式", example = "CASH")
    private String rechargeWay;

    @Schema(description = "充值方式名称", example = "现金充值")
    private String rechargeWayName;

    @Schema(description = "充值渠道", example = "COUNTER")
    private String rechargeChannel;

    @Schema(description = "充值来源", example = "SUBSIDY")
    private String rechargeSource;

    @Schema(description = "充值来源名称", example = "补贴充值")
    private String rechargeSourceName;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    @Schema(description = "充值时间", example = "2025-12-21T10:15:00")
    private LocalDateTime rechargeTime;

    @Schema(description = "充值状态", example = "SUCCESS")
    private String status;

    @Schema(description = "充值状态名称", example = "充值成功")
    private String statusName;

    @Schema(description = "操作人", example = "张管理员")
    private String operator;

    @Schema(description = "账户余额（充值前）", example = "158.50")
    private BigDecimal balanceBefore;

    @Schema(description = "账户余额（充值后）", example = "258.50")
    private BigDecimal balanceAfter;

    @Schema(description = "创建时间", example = "2025-12-21T10:15:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T10:15:00")
    private LocalDateTime updateTime;

    @Schema(description = "充值说明", example = "月度补贴")
    private String remark;

    @Schema(description = "是否已验证", example = "true")
    private Boolean verified;

    @Schema(description = "验证时间", example = "2025-12-21T10:16:00")
    private LocalDateTime verifyTime;

    @Schema(description = "验证人", example = "系统自动")
    private String verifyUser;

    // 添加userName方法作为username的别名，以兼容代码调用
    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    // 兼容性字段别名方法
    public BigDecimal getRechargeAmount() {
        return this.amount;
    }

    public void setRechargeAmount(BigDecimal rechargeAmount) {
        this.amount = rechargeAmount;
    }

    public BigDecimal getBeforeBalance() {
        return this.balanceBefore;
    }

    public void setBeforeBalance(BigDecimal beforeBalance) {
        this.balanceBefore = beforeBalance;
    }

    public BigDecimal getAfterBalance() {
        return this.balanceAfter;
    }

    public void setAfterBalance(BigDecimal afterBalance) {
        this.balanceAfter = afterBalance;
    }

    // 为Builder添加userName方法
    public static class ConsumeRechargeRecordVOBuilder {
        public ConsumeRechargeRecordVOBuilder userName(String userName) {
            return this.username(userName);
        }

        public ConsumeRechargeRecordVOBuilder rechargeAmount(BigDecimal rechargeAmount) {
            return this.amount(rechargeAmount);
        }

        public ConsumeRechargeRecordVOBuilder beforeBalance(BigDecimal beforeBalance) {
            return this.balanceBefore(beforeBalance);
        }

        public ConsumeRechargeRecordVOBuilder afterBalance(BigDecimal afterBalance) {
            return this.balanceAfter(afterBalance);
        }

        // 移除不存在字段的Builder方法
    }
}
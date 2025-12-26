package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费账户视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@Schema(description = "消费账户视图对象")
public class ConsumeAccountVO {

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "用户编号", example = "EMP001")
    private String userCode;

    @Schema(description = "账户类型", example = "STAFF")
    private String accountType;

    @Schema(description = "账户类型名称", example = "员工账户")
    private String accountTypeName;

    @Schema(description = "当前余额", example = "258.50")
    private BigDecimal balance;

    @Schema(description = "信用额度", example = "500.00")
    private BigDecimal creditLimit;

    @Schema(description = "可用额度", example = "758.50")
    private BigDecimal availableLimit;

    @Schema(description = "月消费限额", example = "2000.00")
    private BigDecimal monthlyLimit;

    @Schema(description = "单次消费限额", example = "100.00")
    private BigDecimal singleTransactionLimit;

    @Schema(description = "每日消费限额", example = "500.00")
    private BigDecimal dailyLimit;

    @Schema(description = "本月已消费金额", example = "1250.00")
    private BigDecimal monthlyConsumed;

    @Schema(description = "今日已消费金额", example = "85.50")
    private BigDecimal dailyConsumed;

    @Schema(description = "账户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "账户状态名称", example = "正常")
    private String statusName;

    @Schema(description = "最后消费时间", example = "2025-12-21T12:30:00")
    private LocalDateTime lastConsumeTime;

    @Schema(description = "最后充值时间", example = "2025-12-20T10:15:00")
    private LocalDateTime lastRechargeTime;

    @Schema(description = "创建时间", example = "2025-01-01T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "员工账户")
    private String remark;

    @Schema(description = "总消费次数", example = "156")
    private Integer totalConsumeCount;

    @Schema(description = "总消费金额", example = "5680.50")
    private BigDecimal totalConsumeAmount;

    @Schema(description = "总充值次数", example = "12")
    private Integer totalRechargeCount;

    @Schema(description = "总充值金额", example = "6000.00")
    private BigDecimal totalRechargeAmount;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;
}
package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动端账户信息VO
 * 移动端显示的账户详细信息
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端账户信息")
public class MobileAccountInfoVO {

    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    @Schema(description = "账户编号", example = "ACC1001")
    private String accountNumber;

    @Schema(description = "账户名称", example = "员工食堂账户")
    private String accountName;

    @Schema(description = "账户余额", example = "850.30")
    private BigDecimal balance;

    @Schema(description = "冻结金额", example = "0.00")
    private BigDecimal frozenAmount;

    @Schema(description = "可用余额", example = "850.30")
    private BigDecimal availableBalance;

    @Schema(description = "账户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDescription;

    @Schema(description = "账户类型", example = "PERSONAL")
    private String accountType;

    @Schema(description = "账户类型描述", example = "个人账户")
    private String accountTypeDescription;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "手机号", example = "138****8888")
    private String userPhone;

    @Schema(description = "最近消费时间", example = "2025-01-30 12:30:00")
    private LocalDateTime lastConsumeTime;

    @Schema(description = "最近消费金额", example = "25.00")
    private BigDecimal lastConsumeAmount;

    @Schema(description = "今日消费次数", example = "3")
    private Integer todayConsumeCount;

    @Schema(description = "今日消费金额", example = "68.50")
    private BigDecimal todayConsumeAmount;

    @Schema(description = "信用额度", example = "1000.00")
    private BigDecimal creditLimit;

    @Schema(description = "已用信用额度", example = "0.00")
    private BigDecimal usedCreditLimit;

    @Schema(description = "可用信用额度", example = "1000.00")
    private BigDecimal availableCreditLimit;

    @Schema(description = "账户创建时间", example = "2024-01-01 00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间", example = "2025-01-30 12:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "是否支持透支", example = "false")
    private Boolean allowOverdraw;

    @Schema(description = "透支限额", example = "0.00")
    private BigDecimal overdrawLimit;

    @Schema(description = "账户等级", example = "VIP1")
    private String accountLevel;

    @Schema(description = "积分余额", example = "1250")
    private Integer pointsBalance;

    @Schema(description = "本月积分获取", example = "120")
    private Integer monthlyPointsGained;

    @Schema(description = "账户有效期", example = "2025-12-31 23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "是否即将过期", example = "false")
    private Boolean isExpiringSoon;

    @Schema(description = "天数到过期", example = "335")
    private Integer daysToExpire;
}



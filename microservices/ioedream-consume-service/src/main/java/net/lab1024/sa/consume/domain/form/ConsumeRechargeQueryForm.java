package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

import net.lab1024.sa.common.domain.form.BaseQueryForm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费充值查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费充值查询表单")
public class ConsumeRechargeQueryForm extends BaseQueryForm {

    @Schema(description = "记录ID", example = "1001")
    private Long recordId;

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "充值渠道", example = "WECHAT")
    private String rechargeChannel;

    @Schema(description = "充值状态", example = "1")
    private Integer rechargeStatus;

    @Schema(description = "设备编码", example = "POS001")
    private String deviceCode;

    @Schema(description = "操作人姓名", example = "张管理员")
    private String operatorName;

    @Schema(description = "批次号", example = "BATCH20251221001")
    private String batchNo;

    @Schema(description = "充值方式", example = "CASH")
    private String rechargeWay;

    @Schema(description = "充值来源", example = "SUBSIDY")
    private String rechargeSource;

    @Schema(description = "最小金额", example = "0.01")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", example = "10000.00")
    private BigDecimal maxAmount;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

      @Schema(description = "充值开始时间", example = "2025-12-21T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "充值结束时间", example = "2025-12-21T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "操作人", example = "张管理员")
    private String operator;

    @Schema(description = "关键词搜索", example = "补贴")
    private String keyword;

    @Schema(description = "审核状态", example = "1")
    private Integer auditStatus;

    /**
     * 获取用户姓名（兼容性方法）
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * 设置用户姓名（兼容性方法）
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
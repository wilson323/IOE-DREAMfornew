package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费充值添加表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费充值添加表单")
public class ConsumeRechargeAddForm {

    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @NotBlank(message = "充值方式不能为空")
    @Schema(description = "充值方式", example = "CASH", allowableValues = {"CASH", "BANK_CARD", "WECHAT", "ALIPAY", "TRANSFER"})
    private String rechargeWay;

    @Schema(description = "充值方式名称", example = "现金充值")
    private String rechargeWayName;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    @Schema(description = "充值时间")
    private java.time.LocalDateTime rechargeTime;

    @NotBlank(message = "操作人不能为空")
    @Schema(description = "操作人", example = "张管理员")
    private String operator;

    @Schema(description = "充值说明", example = "月度补贴")
    private String remark;

    @Schema(description = "是否自动到账", example = "true")
    private Boolean autoConfirm = true;

    @Schema(description = "充值来源", example = "SUBSIDY", allowableValues = {"SUBSIDY", "SALARY", "PERSONAL", "REFUND"})
    private String rechargeSource;

    @Schema(description = "充值来源名称", example = "补贴充值")
    private String rechargeSourceName;

    // 添加缺失的字段以匹配Service实现中的调用
    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal rechargeAmount;

    @Schema(description = "充值前余额", example = "50.00")
    private BigDecimal beforeBalance;

    @Schema(description = "充值渠道", example = "WECHAT")
    private String rechargeChannel;

    @Schema(description = "第三方流水号", example = "WX202512210001")
    private String thirdPartyNo;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编码", example = "POS001")
    private String deviceCode;

    @Schema(description = "操作员ID", example = "1001")
    private Long operatorId;

    @Schema(description = "操作员姓名", example = "张管理员")
    private String operatorName;

    @Schema(description = "充值说明", example = "月度补贴")
    private String rechargeDescription;

    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    @Schema(description = "部门名称", example = "财务部")
    private String departmentName;
}
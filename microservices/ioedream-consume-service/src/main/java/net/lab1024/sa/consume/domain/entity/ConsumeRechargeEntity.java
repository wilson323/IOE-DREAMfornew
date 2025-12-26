package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费充值记录实体
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的充值字段定义
 * - 数据验证和审计信息
 * - 软删除和乐观锁
 * - 业务状态判断方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_recharge_record")
@Schema(description = "消费充值记录实体")
public class ConsumeRechargeEntity {

    /**
     * 充值记录ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "充值记录ID", example = "1")
    private Long recordId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "123")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @NotBlank(message = "用户姓名不能为空")
    @Size(max = 100, message = "用户姓名长度不能超过100个字符")
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 充值金额
     */
    @TableField("recharge_amount")
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "充值金额格式不正确")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal rechargeAmount;

    /**
     * 充值前余额
     */
    @TableField("before_balance")
    @NotNull(message = "充值前余额不能为空")
    @Digits(integer = 10, fraction = 2, message = "充值前余额格式不正确")
    @Schema(description = "充值前余额", example = "50.00")
    private BigDecimal beforeBalance;

    /**
     * 充值后余额
     */
    @TableField("after_balance")
    @NotNull(message = "充值后余额不能为空")
    @Digits(integer = 10, fraction = 2, message = "充值后余额格式不正确")
    @Schema(description = "充值后余额", example = "150.00")
    private BigDecimal afterBalance;

    /**
     * 充值方式（1-现金 2-微信 3-支付宝 4-银行卡 5-转账）
     */
    @TableField("recharge_way")
    @NotNull(message = "充值方式不能为空")
    @Min(value = 1, message = "充值方式值不正确")
    @Max(value = 5, message = "充值方式值不正确")
    @Schema(description = "充值方式", example = "2", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer rechargeWay;

    /**
     * 充值渠道（1-线上 2-线下 3-自助终端）
     */
    @TableField("recharge_channel")
    @NotNull(message = "充值渠道不能为空")
    @Min(value = 1, message = "充值渠道值不正确")
    @Max(value = 3, message = "充值渠道值不正确")
    @Schema(description = "充值渠道", example = "2", allowableValues = {"1", "2", "3"})
    private Integer rechargeChannel;

    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    @Size(max = 100, message = "交易流水号长度不能超过100个字符")
    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    /**
     * 第三方交易号
     */
    @TableField("third_party_no")
    @Size(max = 200, message = "第三方交易号长度不能超过200个字符")
    @Schema(description = "第三方交易号", example = "WX202512210000123456")
    private String thirdPartyNo;

    /**
     * 充值状态（1-成功 2-失败 3-处理中 4-已冲正）
     */
    @TableField("recharge_status")
    @NotNull(message = "充值状态不能为空")
    @Min(value = 1, message = "充值状态值不正确")
    @Max(value = 4, message = "充值状态值不正确")
    @Schema(description = "充值状态", example = "1", allowableValues = {"1", "2", "3", "4"})
    private Integer rechargeStatus;

    /**
     * 充值时间
     */
    @TableField("recharge_time")
    @NotNull(message = "充值时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "充值时间", example = "2025-12-21T10:30:00")
    private LocalDateTime rechargeTime;

    /**
     * 到账时间
     */
    @TableField("arrival_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "到账时间", example = "2025-12-21T10:30:05")
    private LocalDateTime arrivalTime;

    /**
     * 充值设备ID
     */
    @TableField("device_id")
    @Schema(description = "充值设备ID", example = "1001")
    private Long deviceId;

    /**
     * 充值设备编码
     */
    @TableField("device_code")
    @Size(max = 50, message = "充值设备编码长度不能超过50个字符")
    @Schema(description = "充值设备编码", example = "CONS_001")
    private String deviceCode;

    /**
     * 操作员ID
     */
    @TableField("operator_id")
    @Schema(description = "操作员ID", example = "456")
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @TableField("operator_name")
    @Size(max = 100, message = "操作员姓名长度不能超过100个字符")
    @Schema(description = "操作员姓名", example = "李四")
    private String operatorName;

    /**
     * 充值说明
     */
    @TableField("recharge_description")
    @Size(max = 500, message = "充值说明长度不能超过500个字符")
    @Schema(description = "充值说明", example = "员工12月份餐补充值")
    private String rechargeDescription;

    /**
     * 备注信息
     */
    @TableField("remark")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @Schema(description = "备注信息", example = "定期充值")
    private String remark;

    /**
     * 充值凭证
     */
    @TableField("recharge_voucher")
    @Size(max = 1000, message = "充值凭证长度不能超过1000个字符")
    @Schema(description = "充值凭证", example = "voucher_receipt_001.jpg")
    private String rechargeVoucher;

    /**
     * 异常信息
     */
    @TableField("error_message")
    @Size(max = 1000, message = "异常信息长度不能超过1000个字符")
    @Schema(description = "异常信息", example = "网络异常，充值失败")
    private String errorMessage;

    /**
     * 冲正原因
     */
    @TableField("reverse_reason")
    @Size(max = 500, message = "冲正原因长度不能超过500个字符")
    @Schema(description = "冲正原因", example = "重复充值")
    private String reverseReason;

    /**
     * 冲正时间
     */
    @TableField("reverse_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "冲正时间", example = "2025-12-21T11:00:00")
    private LocalDateTime reverseTime;

    /**
     * 部门ID
     */
    @TableField("department_id")
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 部门名称
     */
    @TableField("department_name")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 关联批次号
     */
    @TableField("batch_no")
    @Size(max = 50, message = "关联批次号长度不能超过50个字符")
    @Schema(description = "关联批次号", example = "BATCH20251221001")
    private String batchNo;

    /**
     * 审核状态（1-待审核 2-已审核 3-审核驳回）
     */
    @TableField("audit_status")
    @Min(value = 1, message = "审核状态值不正确")
    @Max(value = 3, message = "审核状态值不正确")
    @Schema(description = "审核状态", example = "2", allowableValues = {"1", "2", "3"})
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    @Schema(description = "审核人ID", example = "789")
    private Long auditorId;

    /**
     * 审核人姓名
     */
    @TableField("auditor_name")
    @Size(max = 100, message = "审核人姓名长度不能超过100个字符")
    @Schema(description = "审核人姓名", example = "王五")
    private String auditorName;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间", example = "2025-12-21T10:35:00")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @TableField("audit_remark")
    @Size(max = 500, message = "审核意见长度不能超过500个字符")
    @Schema(description = "审核意见", example = "审核通过")
    private String auditRemark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"source\": \"auto\", \"priority\": \"high\"}")
    private String extendedAttributes;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-12-21T00:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-12-21T00:00:00")
    private LocalDateTime updateTime;

    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Schema(description = "乐观锁版本号", example = "1")
    private Integer version;

    // ==================== 业务状态判断方法 ====================

    /**
     * 判断是否成功
     */
    @JsonIgnore
    public boolean isSuccess() {
        return rechargeStatus != null && rechargeStatus == 1;
    }

    /**
     * 判断是否失败
     */
    @JsonIgnore
    public boolean isFailed() {
        return rechargeStatus != null && rechargeStatus == 2;
    }

    /**
     * 判断是否处理中
     */
    @JsonIgnore
    public boolean isProcessing() {
        return rechargeStatus != null && rechargeStatus == 3;
    }

    /**
     * 判断是否已冲正
     */
    @JsonIgnore
    public boolean isReversed() {
        return rechargeStatus != null && rechargeStatus == 4;
    }

    /**
     * 判断是否需要审核
     */
    @JsonIgnore
    public boolean needsAudit() {
        return auditStatus != null && auditStatus == 1;
    }

    /**
     * 判断是否已审核
     */
    @JsonIgnore
    public boolean isAudited() {
        return auditStatus != null && auditStatus == 2;
    }

    /**
     * 判断是否审核驳回
     */
    @JsonIgnore
    public boolean isAuditRejected() {
        return auditStatus != null && auditStatus == 3;
    }

    /**
     * 判断是否线上充值
     */
    @JsonIgnore
    public boolean isOnlineRecharge() {
        return rechargeChannel != null && rechargeChannel == 1;
    }

    /**
     * 判断是否线下充值
     */
    @JsonIgnore
    public boolean isOfflineRecharge() {
        return rechargeChannel != null && rechargeChannel == 2;
    }

    /**
     * 判断是否自助终端充值
     */
    @JsonIgnore
    public boolean isSelfServiceRecharge() {
        return rechargeChannel != null && rechargeChannel == 3;
    }

    /**
     * 判断是否有第三方交易号
     */
    @JsonIgnore
    public boolean hasThirdPartyNo() {
        return thirdPartyNo != null && !thirdPartyNo.trim().isEmpty();
    }

    /**
     * 判断是否异常
     */
    @JsonIgnore
    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    /**
     * 获取充值方式名称
     */
    @JsonIgnore
    public String getRechargeWayName() {
        if (rechargeWay == null) {
            return "";
        }
        switch (rechargeWay) {
            case 1: return "现金";
            case 2: return "微信";
            case 3: return "支付宝";
            case 4: return "银行卡";
            case 5: return "转账";
            default: return "未知";
        }
    }

    /**
     * 获取充值渠道名称
     */
    @JsonIgnore
    public String getRechargeChannelName() {
        if (rechargeChannel == null) {
            return "";
        }
        switch (rechargeChannel) {
            case 1: return "线上";
            case 2: return "线下";
            case 3: return "自助终端";
            default: return "未知";
        }
    }

    /**
     * 获取充值状态名称
     */
    @JsonIgnore
    public String getRechargeStatusName() {
        if (rechargeStatus == null) {
            return "";
        }
        switch (rechargeStatus) {
            case 1: return "成功";
            case 2: return "失败";
            case 3: return "处理中";
            case 4: return "已冲正";
            default: return "未知";
        }
    }

    /**
     * 获取审核状态名称
     */
    @JsonIgnore
    public String getAuditStatusName() {
        if (auditStatus == null) {
            return "无需审核";
        }
        switch (auditStatus) {
            case 1: return "待审核";
            case 2: return "已审核";
            case 3: return "审核驳回";
            default: return "无需审核";
        }
    }

    /**
     * 获取充值状态颜色（用于前端显示）
     */
    @JsonIgnore
    public String getRechargeStatusColor() {
        if (rechargeStatus == null) {
            return "#95a5a6"; // 灰色
        }
        switch (rechargeStatus) {
            case 1: return "#2ed573"; // 绿色 - 成功
            case 2: return "#ff4757"; // 红色 - 失败
            case 3: return "#f39c12"; // 橙色 - 处理中
            case 4: return "#718093"; // 深灰色 - 已冲正
            default: return "#95a5a6";
        }
    }

    /**
     * 验证业务规则
     */
    @JsonIgnore
    public java.util.List<String> validateBusinessRules() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 验证金额合理性
        if (rechargeAmount != null && rechargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("充值金额必须大于0");
        }

        // 验证余额计算
        if (beforeBalance != null && afterBalance != null && rechargeAmount != null) {
            BigDecimal expectedBalance = beforeBalance.add(rechargeAmount);
            if (afterBalance.compareTo(expectedBalance) != 0) {
                errors.add("充值后余额计算不正确");
            }
        }

        // 验证时间顺序
        if (arrivalTime != null && rechargeTime != null && arrivalTime.isBefore(rechargeTime)) {
            errors.add("到账时间不能早于充值时间");
        }

        // 验证冲正时间
        if (reverseTime != null && rechargeTime != null && reverseTime.isBefore(rechargeTime)) {
            errors.add("冲正时间不能早于充值时间");
        }

        return errors;
    }

    /**
     * 判断是否为批量充值
     */
    @JsonIgnore
    public boolean isBatchRecharge() {
        return batchNo != null && !batchNo.trim().isEmpty();
    }

    /**
     * 判断是否到账
     */
    @JsonIgnore
    public boolean hasArrived() {
        return arrivalTime != null;
    }

    /**
     * 获取处理时长（毫秒）
     */
    @JsonIgnore
    public Long getProcessingDuration() {
        if (arrivalTime == null || rechargeTime == null) {
            return null;
        }
        return java.time.Duration.between(rechargeTime, arrivalTime).toMillis();
    }

    // ==================== 兼容性方法（为Manager层提供支持）====================

    /**
     * 获取用户名（兼容性方法）
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * 设置用户名（兼容性方法）
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取充值渠道（兼容性方法）
     */
    public Integer getRechargeChannel() {
        return this.rechargeChannel;
    }

    /**
     * 设置充值渠道（兼容性方法）
     */
    public void setRechargeChannel(Integer rechargeChannel) {
        this.rechargeChannel = rechargeChannel;
    }

    /**
     * 获取版本号（兼容性方法）
     */
    public Integer getVersion() {
        return this.version;
    }
}
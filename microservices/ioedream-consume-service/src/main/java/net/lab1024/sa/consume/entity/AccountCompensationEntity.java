package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户服务补偿记录实体
 * <p>
 * 用于记录账户服务调用失败时的补偿记录，支持后续重试
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@TableName("t_account_compensation")
@Schema(description = "账户服务补偿记录")
public class AccountCompensationEntity {

    /**
     * 补偿记录ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "补偿记录ID")
    private Long compensationId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型", example = "INCREASE",
            allowableValues = {"INCREASE", "DECREASE"})
    private String operation;

    /**
     * 金额
     */
    @Schema(description = "金额", example = "100.00")
    private BigDecimal amount;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型", example = "SUBSIDY_GRANT")
    private String businessType;

    /**
     * 业务编号（用于幂等性控制）
     */
    @Schema(description = "业务编号", example = "SUB-20251223-0001")
    private String businessNo;

    /**
     * 关联业务编号
     */
    @Schema(description = "关联业务编号", example = "CON-20251223-0001")
    private String relatedBusinessNo;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "PENDING",
            allowableValues = {"PENDING", "SUCCESS", "FAILED", "CANCELLED"})
    private String status;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetryCount;

    /**
     * 错误码
     */
    @Schema(description = "错误码", example = "SERVICE_UNAVAILABLE")
    private String errorCode;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "账户服务暂时不可用")
    private String errorMessage;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "补贴发放失败，等待重试")
    private String remark;

    /**
     * 扩展参数（JSON格式）
     */
    @Schema(description = "扩展参数", example = "{\"deviceId\":\"POS001\"}")
    @TableField(value = "extended_params", exist = false)
    private String extendedParams;

    /**
     * 下次重试时间
     */
    @Schema(description = "下次重试时间", example = "2025-12-23T08:00:00")
    private LocalDateTime nextRetryTime;

    /**
     * 最后重试时间
     */
    @Schema(description = "最后重试时间", example = "2025-12-23T07:30:00")
    private LocalDateTime lastRetryTime;

    /**
     * 成功时间
     */
    @Schema(description = "成功时间", example = "2025-12-23T07:35:00")
    private LocalDateTime successTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @Schema(description = "删除标记", example = "0")
    @TableField("deleted_flag")
    private Integer deletedFlag;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建余额增加补偿记录
     */
    public static AccountCompensationEntity forIncrease(Long userId, BigDecimal amount,
                                                         String businessType, String businessNo,
                                                         String errorMessage) {
        AccountCompensationEntity entity = new AccountCompensationEntity();
        entity.setUserId(userId);
        entity.setOperation("INCREASE");
        entity.setAmount(amount);
        entity.setBusinessType(businessType);
        entity.setBusinessNo(businessNo);
        entity.setStatus("PENDING");
        entity.setRetryCount(0);
        entity.setMaxRetryCount(3);
        entity.setErrorMessage(errorMessage);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setNextRetryTime(calculateNextRetryTime(0));
        return entity;
    }

    /**
     * 创建余额扣减补偿记录
     */
    public static AccountCompensationEntity forDecrease(Long userId, BigDecimal amount,
                                                         String businessType, String businessNo,
                                                         String errorMessage) {
        AccountCompensationEntity entity = new AccountCompensationEntity();
        entity.setUserId(userId);
        entity.setOperation("DECREASE");
        entity.setAmount(amount);
        entity.setBusinessType(businessType);
        entity.setBusinessNo(businessNo);
        entity.setStatus("PENDING");
        entity.setRetryCount(0);
        entity.setMaxRetryCount(3);
        entity.setErrorMessage(errorMessage);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setNextRetryTime(calculateNextRetryTime(0));
        return entity;
    }

    /**
     * 计算下次重试时间（指数退避）
     */
    private static LocalDateTime calculateNextRetryTime(Integer retryCount) {
        // 指数退避：1分钟、2分钟、4分钟、8分钟...
        int delayMinutes = (int) Math.pow(2, retryCount);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }

    /**
     * 增加重试次数并更新下次重试时间
     */
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryTime = LocalDateTime.now();
        this.nextRetryTime = calculateNextRetryTime(this.retryCount);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return "PENDING".equals(this.status)
                && this.retryCount < this.maxRetryCount
                && LocalDateTime.now().isAfter(this.nextRetryTime);
    }

    /**
     * 判断是否达到最大重试次数
     */
    public boolean isMaxRetryReached() {
        return this.retryCount >= this.maxRetryCount;
    }

    /**
     * 标记为成功
     */
    public void markAsSuccess() {
        this.status = "SUCCESS";
        this.successTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记为失败
     */
    public void markAsFailed(String errorCode, String errorMessage) {
        this.status = "FAILED";
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取消补偿
     */
    public void cancel() {
        this.status = "CANCELLED";
        this.updateTime = LocalDateTime.now();
    }
}

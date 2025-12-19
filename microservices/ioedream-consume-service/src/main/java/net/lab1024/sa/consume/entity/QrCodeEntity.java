package net.lab1024.sa.consume.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 二维码实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_qr_code")
@Schema(description = "二维码实体")
public class QrCodeEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 二维码ID
     */
    @TableField("qr_id")
    @Schema(description = "二维码ID")
    private Long qrId;
    /**
     * 二维码内容
     */
    @TableField("qr_code")
    @Schema(description = "二维码内容")
    private String qrCode;
    /**
     * 二维码类型
     */
    @TableField("qr_type")
    @Schema(description = "二维码类型")
    private String qrType;
    /**
     * 关联账户
     */
    @TableField("account_no")
    @Schema(description = "关联账户")
    private String accountNo;
    /**
     * 有效期
     */
    @TableField("valid_time")
    @Schema(description = "有效期")
    private java.time.LocalDateTime validTime;
    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 二维码令牌
     */
    @TableField("qr_token")
    @Schema(description = "二维码令牌")
    private String qrToken;

    /**
     * 到期时间
     */
    @TableField("expire_time")
    @Schema(description = "到期时间")
    private java.time.LocalDateTime expireTime;

    /**
     * 业务模块标识
     * <p>
     * 根据chonggou.txt任务1.4要求添加
     * 标识二维码所属业务模块：consume/access/attendance/visitor
     * </p>
     */
    @TableField("business_module")
    @Schema(description = "业务模块标识")
    private String businessModule;

    /**
     * 生效时间
     * <p>
     * 根据chonggou.txt任务1.4要求添加
     * 二维码开始生效的时间
     * </p>
     */
    @TableField("effective_time")
    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    /**
     * 二维码内容（实际内容字段）
     * <p>
     * 根据chonggou.txt任务1.4要求添加
     * 存储二维码的实际内容字符串
     * </p>
     */
    @TableField("qr_content")
    @Schema(description = "二维码内容")
    private String qrContent;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    // 缺失字段 - 根据错误日志添加
    /**
     * 用户ID (String类型兼容)
     */
    @TableField("user_id_str")
    @Schema(description = "用户ID(String)")
    private String userIdStr;

    /**
     * 二维码数据 (JSON格式)
     */
    @TableField("qr_data")
    @Schema(description = "二维码数据")
    private String qrData;

    /**
     * 生成时间
     */
    @TableField("generate_time")
    @Schema(description = "生成时间")
    private LocalDateTime generateTime;

    /**
     * 使用次数限制
     */
    @TableField("usage_limit")
    @Schema(description = "使用次数限制")
    private Integer usageLimit;

    /**
     * 已使用次数
     */
    @TableField("used_count")
    @Schema(description = "已使用次数")
    private Integer usedCount;

      /**
     * 金额限制
     */
    @TableField("amount_limit")
    @Schema(description = "金额限制")
    private java.math.BigDecimal amountLimit;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 安全级别
     * <p>
     * 根据chonggou.txt要求，类型为Integer（1-低 2-中 3-高）
     * </p>
     */
    @TableField("security_level")
    @Schema(description = "安全级别")
    private Integer securityLevel;

    /**
     * 二维码状态（详细状态）
     */
    @TableField("qr_status")
    @Schema(description = "二维码状态")
    private Integer qrStatus;

    /**
     * 最后使用时间
     */
    @TableField("last_used_time")
    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    /**
     * 最后使用设备
     */
    @TableField("last_used_device")
    @Schema(description = "最后使用设备")
    private String lastUsedDevice;

    /**
     * 最后使用位置
     */
    @TableField("last_used_location")
    @Schema(description = "最后使用位置")
    private String lastUsedLocation;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 缺失的getter/setter方法 - 根据错误日志添加
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getUserIdStr() {
        return this.userIdStr;
    }

    public void setUserIdStr(String userIdStr) {
        this.userIdStr = userIdStr;
    }

    public String getQrData() {
        return this.qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }

    public LocalDateTime getGenerateTime() {
        return this.generateTime;
    }

    public void setGenerateTime(LocalDateTime generateTime) {
        this.generateTime = generateTime;
    }

    public Integer getUsageLimit() {
        return this.usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsedCount() {
        return this.usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    // 新增字段的getter/setter方法
    public java.math.BigDecimal getAmountLimit() {
        return this.amountLimit;
    }

    public void setAmountLimit(java.math.BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public Long getAreaId() {
        return this.areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getSecurityLevel() {
        return this.securityLevel;
    }

    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }

    public Integer getQrStatus() {
        return this.qrStatus;
    }

    public void setQrStatus(Integer qrStatus) {
        this.qrStatus = qrStatus;
    }

    public LocalDateTime getLastUsedTime() {
        return this.lastUsedTime;
    }

    public void setLastUsedTime(LocalDateTime lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getLastUsedDevice() {
        return this.lastUsedDevice;
    }

    public void setLastUsedDevice(String lastUsedDevice) {
        this.lastUsedDevice = lastUsedDevice;
    }

    public String getLastUsedLocation() {
        return this.lastUsedLocation;
    }

    public void setLastUsedLocation(String lastUsedLocation) {
        this.lastUsedLocation = lastUsedLocation;
    }

    // 兼容性方法 - 优先使用qrContent字段，否则使用qrCode
    public String getQrContent() {
        return this.qrContent != null ? this.qrContent : this.qrCode;
    }

    // 设置二维码内容
    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
        // 如果qrCode为空，同时设置qrCode
        if (this.qrCode == null) {
            this.qrCode = qrContent;
        }
    }

    public void setQrStatus(int qrStatus) {
        this.qrStatus = qrStatus;
    }

    // 业务模块获取方法 - 优先使用businessModule字段，否则根据qrType判断
    public String getBusinessModule() {
        if (this.businessModule != null) {
            return this.businessModule;
        }
        if (this.qrType == null) {
            return null;
        }

        switch (this.qrType) {
            case "CONSUME":
                return "consume";
            case "ACCESS":
                return "access";
            case "ATTENDANCE":
                return "attendance";
            case "VISITOR":
                return "visitor";
            default:
                return this.qrType.toLowerCase();
        }
    }

    // 设置业务模块
    public void setBusinessModule(String businessModule) {
        this.businessModule = businessModule;
    }

    // 获取和设置qrType（String类型）
    public String getQrType() {
        return this.qrType;
    }

    public void setQrType(String qrType) {
        this.qrType = qrType;
    }

    // getEffectiveTime 方法 - 优先使用effectiveTime字段，否则使用validTime
    public LocalDateTime getEffectiveTime() {
        return this.effectiveTime != null ? this.effectiveTime : this.validTime;
    }

    // 设置生效时间
    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
        // 如果validTime为空，同时设置validTime
        if (this.validTime == null) {
            this.validTime = effectiveTime;
        }
    }

    // 缺失字段 - 根据错误日志添加
    /**
     * 是否需要生物验证
     * <p>
     * 根据chonggou.txt要求，类型为Integer（0-不需要 1-需要）
     * </p>
     */
    @TableField("require_biometric")
    @Schema(description = "是否需要生物验证")
    private Integer requireBiometric;

    /**
     * 是否需要位置验证
     * <p>
     * 根据chonggou.txt要求，类型为Integer（0-不需要 1-需要）
     * </p>
     */
    @TableField("require_location")
    @Schema(description = "是否需要位置验证")
    private Integer requireLocation;

    /**
     * 创建方式
     * <p>
     * 根据chonggou.txt要求，类型为Integer（1-系统生成 2-手动创建 3-批量导入）
     * </p>
     */
    @TableField("create_method")
    @Schema(description = "创建方式")
    private Integer createMethod;

    /**
     * 生成原因
     */
    @TableField("generate_reason")
    @Schema(description = "生成原因")
    private String generateReason;

    /**
     * 扩展属性 (JSON格式)
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // 缺失的getter/setter方法 - 根据错误日志添加
    public Integer getRequireBiometric() {
        return this.requireBiometric;
    }

    public void setRequireBiometric(Integer requireBiometric) {
        this.requireBiometric = requireBiometric;
    }

    public Integer getRequireLocation() {
        return this.requireLocation;
    }

    public void setRequireLocation(Integer requireLocation) {
        this.requireLocation = requireLocation;
    }

    public Integer getCreateMethod() {
        return this.createMethod;
    }

    public void setCreateMethod(Integer createMethod) {
        this.createMethod = createMethod;
    }

    public String getGenerateReason() {
        return this.generateReason;
    }

    public void setGenerateReason(String generateReason) {
        this.generateReason = generateReason;
    }

    public String getExtendedAttributes() {
        return this.extendedAttributes;
    }

    public void setExtendedAttributes(String extendedAttributes) {
        this.extendedAttributes = extendedAttributes;
    }
}

package net.lab1024.sa.common.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 二维码实体类
 * <p>
 * 企业级二维码管理实体，支持消费、门禁、考勤等业务场景
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_qrcode")
public class QrCodeEntity extends BaseEntity {

    /**
     * 二维码ID
     */
    @TableId(value = "qr_id", type = IdType.ASSIGN_ID)
    private String qrId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    /**
     * 二维码类型：1-消费码 2-门禁码 3-考勤码 4-访客码 5-通用码
     */
    @NotNull(message = "二维码类型不能为空")
    @TableField("qr_type")
    private Integer qrType;

    /**
     * 二维码内容（JSON格式）
     */
    @NotBlank(message = "二维码内容不能为空")
    @Size(max = 2000, message = "二维码内容长度不能超过2000个字符")
    @TableField("qr_content")
    private String qrContent;

    /**
     * 二维码标识（用于快速识别）
     */
    @NotBlank(message = "二维码标识不能为空")
    @Size(max = 100, message = "二维码标识长度不能超过100个字符")
    @TableField("qr_token")
    private String qrToken;

    /**
     * 业务模块：consume-消费 access-门禁 attendance-考勤 visitor-访客
     */
    @NotBlank(message = "业务模块不能为空")
    @Size(max = 50, message = "业务模块长度不能超过50个字符")
    @TableField("business_module")
    private String businessModule;

    /**
     * 有效开始时间
     */
    @NotNull(message = "有效开始时间不能为空")
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 有效结束时间
     */
    @NotNull(message = "有效结束时间不能为空")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 使用次数限制：0-无限制 >0-指定次数
     */
    @TableField("usage_limit")
    private Integer usageLimit;

    /**
     * 已使用次数
     */
    @TableField("used_count")
    private Integer usedCount;

    /**
     * 二维码状态：1-有效 2-已过期 3-已用完 4-已撤销 5-暂停使用
     */
    @TableField("qr_status")
    private Integer qrStatus;

    /**
     * 安全级别：1-低 2-中 3-高 4-极高
     */
    @TableField("security_level")
    private Integer securityLevel;

    /**
     * 区域ID限制（为空表示全局有效）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 设备ID限制（为空表示所有设备有效）
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 金额限制（消费码专用）
     */
    @TableField("amount_limit")
    private java.math.BigDecimal amountLimit;

    /**
     * 是否需要生物验证：0-不需要 1-需要指纹 2-需要人脸 3-需要指纹+人脸
     */
    @TableField("require_biometric")
    private Integer requireBiometric;

    /**
     * 是否需要位置验证：0-不需要 1-需要
     */
    @TableField("require_location")
    private Integer requireLocation;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 最后使用时间
     */
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    /**
     * 最后使用设备ID
     */
    @TableField("last_used_device")
    private String lastUsedDevice;

    /**
     * 最后使用位置
     */
    @TableField("last_used_location")
    private String lastUsedLocation;

    /**
     * 创建方式：1-用户生成 2-系统生成 3-管理员生成 4-API生成
     */
    @TableField("create_method")
    private Integer createMethod;

    /**
     * 生成原因/备注
     */
    @Size(max = 500, message = "生成原因长度不能超过500个字符")
    @TableField("generate_reason")
    private String generateReason;
}




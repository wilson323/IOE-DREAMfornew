package net.lab1024.sa.common.consume.domain.vo;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 二维码视图对象
 * <p>
 * 企业级二维码展示VO，支持移动端和Web端显示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
@Schema(description = "二维码视图对象")
public class QrCodeVO {

    /**
     * 二维码ID
     */
    @Schema(description = "二维码ID")
    private String qrId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 二维码类型：1-消费码 2-门禁码 3-考勤码 4-访客码 5-通用码
     */
    @Schema(description = "二维码类型")
    private Integer qrType;

    /**
     * 二维码类型名称
     */
    @Schema(description = "二维码类型名称")
    private String qrTypeName;

    /**
     * 二维码内容（JSON格式）
     */
    @Schema(description = "二维码内容")
    private String qrContent;

    /**
     * 二维码图片URL（base64或网络URL）
     */
    @Schema(description = "二维码图片URL")
    private String qrImageUrl;

    /**
     * 二维码标识（用于快速识别）
     */
    @Schema(description = "二维码标识")
    private String qrToken;

    /**
     * 业务模块：consume-消费 access-门禁 attendance-考勤 visitor-访客
     */
    @Schema(description = "业务模块")
    private String businessModule;

    /**
     * 业务模块名称
     */
    @Schema(description = "业务模块名称")
    private String businessModuleName;

    /**
     * 有效开始时间
     */
    @Schema(description = "有效开始时间")
    private LocalDateTime effectiveTime;

    /**
     * 有效结束时间
     */
    @Schema(description = "有效结束时间")
    private LocalDateTime expireTime;

    /**
     * 剩余有效时间（秒）
     */
    @Schema(description = "剩余有效时间（秒）")
    private Long remainingTime;

    /**
     * 使用次数限制：0-无限制 >0-指定次数
     */
    @Schema(description = "使用次数限制")
    private Integer usageLimit;

    /**
     * 已使用次数
     */
    @Schema(description = "已使用次数")
    private Integer usedCount;

    /**
     * 剩余使用次数
     */
    @Schema(description = "剩余使用次数")
    private Integer remainingUsage;

    /**
     * 二维码状态：1-有效 2-已过期 3-已用完 4-已撤销 5-暂停使用
     */
    @Schema(description = "二维码状态")
    private Integer qrStatus;

    /**
     * 二维码状态名称
     */
    @Schema(description = "二维码状态名称")
    private String qrStatusName;

    /**
     * 安全级别：1-低 2-中 3-高 4-极高
     */
    @Schema(description = "安全级别")
    private Integer securityLevel;

    /**
     * 安全级别名称
     */
    @Schema(description = "安全级别名称")
    private String securityLevelName;

    /**
     * 区域ID限制
     */
    @Schema(description = "区域ID限制")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 设备ID限制
     */
    @Schema(description = "设备ID限制")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 金额限制（消费码专用）
     */
    @Schema(description = "金额限制")
    private BigDecimal amountLimit;

    /**
     * 已使用金额
     */
    @Schema(description = "已使用金额")
    private BigDecimal usedAmount;

    /**
     * 剩余金额
     */
    @Schema(description = "剩余金额")
    private BigDecimal remainingAmount;

    /**
     * 是否需要生物验证：0-不需要 1-需要指纹 2-需要人脸 3-需要指纹+人脸
     */
    @Schema(description = "是否需要生物验证")
    private Integer requireBiometric;

    /**
     * 生物验证要求描述
     */
    @Schema(description = "生物验证要求描述")
    private String biometricRequireDesc;

    /**
     * 是否需要位置验证：0-不需要 1-需要
     */
    @Schema(description = "是否需要位置验证")
    private Integer requireLocation;

    /**
     * 最后使用时间
     */
    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    /**
     * 最后使用设备ID
     */
    @Schema(description = "最后使用设备ID")
    private String lastUsedDevice;

    /**
     * 最后使用位置
     */
    @Schema(description = "最后使用位置")
    private String lastUsedLocation;

    /**
     * 创建方式：1-用户生成 2-系统生成 3-管理员生成 4-API生成
     */
    @Schema(description = "创建方式")
    private Integer createMethod;

    /**
     * 创建方式名称
     */
    @Schema(description = "创建方式名称")
    private String createMethodName;

    /**
     * 生成原因/备注
     */
    @Schema(description = "生成原因")
    private String generateReason;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 扩展属性（JSON格式）
     */
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    /**
     * 是否可以立即使用
     */
    @Schema(description = "是否可以立即使用")
    private Boolean immediatelyUsable;

    /**
     * 使用进度百分比
     */
    @Schema(description = "使用进度百分比")
    private Integer usageProgress;
}
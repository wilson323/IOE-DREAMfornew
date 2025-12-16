package net.lab1024.sa.consume.consume.domain.form;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * 二维码生成表单
 * <p>
 * 企业级二维码生成请求表单，支持多种业务场景
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class QrCodeGenerateForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 二维码类型：1-消费码 2-门禁码 3-考勤码 4-访客码 5-通用码
     */
    @NotNull(message = "二维码类型不能为空")
    @Min(value = 1, message = "二维码类型值不正确")
    @Max(value = 5, message = "二维码类型值不正确")
    private Integer qrType;

    /**
     * 业务模块：consume-消费 access-门禁 attendance-考勤 visitor-访客
     */
    @NotBlank(message = "业务模块不能为空")
    @Size(max = 50, message = "业务模块长度不能超过50个字符")
    private String businessModule;

    /**
     * 有效开始时间（为空表示立即生效）
     */
    private LocalDateTime effectiveTime;

    /**
     * 有效结束时间（为空表示永久有效）
     */
    private LocalDateTime expireTime;

    /**
     * 使用次数限制：0-无限制 >0-指定次数
     */
    @Min(value = 0, message = "使用次数限制不能小于0")
    private Integer usageLimit;

    /**
     * 安全级别：1-低 2-中 3-高 4-极高
     */
    @Min(value = 1, message = "安全级别值不正确")
    @Max(value = 4, message = "安全级别值不正确")
    private Integer securityLevel;

    /**
     * 区域ID限制（为空表示全局有效）
     */
    private Long areaId;

    /**
     * 设备ID限制（为空表示所有设备有效）
     */
    private String deviceId;

    /**
     * 金额限制（消费码专用）
     */
    private java.math.BigDecimal amountLimit;

    /**
     * 是否需要生物验证：0-不需要 1-需要指纹 2-需要人脸 3-需要指纹+人脸
     */
    @Min(value = 0, message = "生物验证要求值不正确")
    @Max(value = 3, message = "生物验证要求值不正确")
    private Integer requireBiometric;

    /**
     * 是否需要位置验证：0-不需要 1-需要
     */
    @Min(value = 0, message = "位置验证要求值不正确")
    @Max(value = 1, message = "位置验证要求值不正确")
    private Integer requireLocation;

    /**
     * 扩展属性（JSON格式）
     */
    @Size(max = 2000, message = "扩展属性长度不能超过2000个字符")
    private String extendedAttributes;

    /**
     * 生成原因/备注
     */
    @Size(max = 500, message = "生成原因长度不能超过500个字符")
    private String generateReason;

    /**
     * 创建方式：1-用户生成 2-系统生成 3-管理员生成 4-API生成
     */
    @Min(value = 1, message = "创建方式值不正确")
    @Max(value = 4, message = "创建方式值不正确")
    private Integer createMethod;
}




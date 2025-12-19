package net.lab1024.sa.visitor.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 黑名单添加表单
 * <p>
 * 内存优化设计：
 * - 最小化字段数量，只包含必要信息
 * - 使用基本数据类型而非包装类型
 * - 合理的字符串长度限制
 * - 可选字段使用null而非空字符串
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "黑名单添加表单")
public class BlacklistAddForm {

    /**
     * 访客ID
     */
    @NotNull(message = "访客ID不能为空")
    @Schema(description = "访客ID", example = "1001")
    private Long visitorId;

    /**
     * 访客姓名
     */
    @NotBlank(message = "访客姓名不能为空")
    @Size(max = 50, message = "访客姓名长度不能超过50个字符")
    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Size(max = 18, message = "身份证号长度不能超过18个字符")
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    /**
     * 手机号
     */
    @Size(max = 11, message = "手机号长度不能超过11个字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 黑名单类型
     * <p>
     * PERMANENT-永久黑名单
     * TEMPORARY-临时黑名单
     * WARNING-警告名单
     * </p>
     */
    @NotBlank(message = "黑名单类型不能为空")
    @Size(max = 20, message = "黑名单类型长度不能超过20个字符")
    @Schema(description = "黑名单类型", example = "PERMANENT")
    private String blacklistType;

    /**
     * 黑名单原因
     */
    @NotBlank(message = "黑名单原因不能为空")
    @Size(max = 200, message = "黑名单原因长度不能超过200个字符")
    @Schema(description = "黑名单原因", example = "违反园区安全管理规定")
    private String blacklistReason;

    /**
     * 生效时间（默认为当前时间）
     */
    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime startTime;

    /**
     * 失效时间（临时黑名单专用）
     */
    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;
}
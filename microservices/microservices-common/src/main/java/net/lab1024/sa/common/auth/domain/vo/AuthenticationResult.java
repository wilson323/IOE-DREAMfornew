package net.lab1024.sa.common.auth.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证结果VO
 * <p>
 * 统一身份认证系统认证结果返回对象
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "认证结果")
public class AuthenticationResult {

    @Schema(description = "认证是否成功", example = "true")
    private Boolean success;

    @Schema(description = "错误码", example = "USER_NOT_FOUND")
    private String errorCode;

    @Schema(description = "错误信息", example = "用户不存在")
    private String errorMessage;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "登录名", example = "admin")
    private String loginName;

    @Schema(description = "用户姓名", example = "管理员")
    private String userName;

    @Schema(description = "手机号", example = "13800138000")
    private String userPhone;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String userEmail;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "认证令牌")
    private AuthToken token;

    @Schema(description = "用户权限列表")
    private List<String> permissions;

    @Schema(description = "用户角色列表")
    private List<String> roles;

    @Schema(description = "是否为管理员", example = "true")
    private Boolean administratorFlag;

    @Schema(description = "账户状态", example = "ACTIVE")
    private String accountStatus;

    @Schema(description = "是否需要多因素认证", example = "false")
    private Boolean requireMfa;

    @Schema(description = "支持的MFA类型")
    private List<String> supportedMfaTypes;

    @Schema(description = "认证时间")
    private LocalDateTime authTime;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "登录设备", example = "device-uuid")
    private String deviceId;
}

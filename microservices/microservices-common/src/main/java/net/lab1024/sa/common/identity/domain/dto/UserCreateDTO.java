package net.lab1024.sa.common.identity.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Schema(description = "用户创建DTO")
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Size(max = 100, message = "真实姓名长度不能超过100个字符")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "头像URL", example = "http://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "备注", example = "新员工")
    private String remark;
}


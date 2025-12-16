package net.lab1024.sa.common.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateUserProfileRequest {

    @Size(max = 100, message = "真实姓名长度不能超过100个字符")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Schema(description = "手机号码", example = "13812345678")
    private String phone;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "系统管理员")
    private String remark;

    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    private String birthday;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;
}
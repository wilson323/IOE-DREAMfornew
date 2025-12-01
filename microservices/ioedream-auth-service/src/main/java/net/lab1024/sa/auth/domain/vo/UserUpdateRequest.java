package net.lab1024.sa.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 用户更新请求VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    @Size(min = 4, max = 50, message = "用户名长度必须在4-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
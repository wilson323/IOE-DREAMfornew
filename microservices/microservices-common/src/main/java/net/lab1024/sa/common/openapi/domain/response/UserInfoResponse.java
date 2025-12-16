package net.lab1024.sa.common.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户基本信息响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户基本信息响应")
public class UserInfoResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号", example = "13812345678")
    private String phone;

    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
    private Integer gender;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "系统管理员")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    @Schema(description = "用户状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "最后登录时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间", example = "2025-01-01T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "备注", example = "系统管理员")
    private String remark;
}
package net.lab1024.sa.admin.module.hr.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新员工DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "更新员工DTO")
public class EmployeeUpdateDTO {

    @Schema(description = "员工ID", example = "1", required = true)
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    @Size(max = 50, message = "员工姓名长度不能超过50个字符")
    private String employeeName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Schema(description = "状态：1-在职 2-离职", example = "1")
    private Integer status;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;
}
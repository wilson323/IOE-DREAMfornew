package net.lab1024.sa.admin.module.hr.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新增员工DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "新增员工DTO")
public class EmployeeAddDTO {

    @Schema(description = "员工姓名", example = "张三", required = true)
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 50, message = "员工姓名长度不能超过50个字符")
    private String employeeName;

    @Schema(description = "部门ID", example = "1", required = true)
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Schema(description = "手机号", example = "13800138000", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "入职时间", example = "2023-01-01T09:00:00")
    private String hireDate;
}
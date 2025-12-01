package net.lab1024.sa.system.employee.domain.form;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 员工更新表单
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@Schema(description = "员工更新表单")
public class EmployeeUpdateForm {

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    @NotBlank(message = "员工姓名不能为空")
    @Schema(description = "员工姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String employeeName;

    @NotNull(message = "性别不能为空")
    @Schema(description = "性别：1-男，2-女", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gender;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用 1-启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Pattern(regexp = "^\\d{17}[\\dX]$", message = "身份证号格式不正确")
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "薪资", example = "10000.00")
    private Double salary;

    @Schema(description = "入职日期", example = "2023-01-01")
    private LocalDate joinDate;

    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;

    @Schema(description = "备注", example = "优秀员工")
    private String remark;
}

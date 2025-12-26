package net.lab1024.sa.common.system.employee.domain.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 员工新增DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Schema(description = "员工新增DTO")
public class EmployeeAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "员工姓名", required = true, example = "张三")
    @NotBlank(message = "员工姓名不能为空")
    private String employeeName;

    @Schema(description = "员工工号", example = "EMP001")
    private String employeeNo;

    @Schema(description = "登录账号", example = "zhangsan")
    private String loginName;

    @Schema(description = "登录密码", example = "123456")
    private String loginPwd;

    @Schema(description = "部门ID", required = true, example = "1001")
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "新员工")
    private String remark;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCardNo;
}

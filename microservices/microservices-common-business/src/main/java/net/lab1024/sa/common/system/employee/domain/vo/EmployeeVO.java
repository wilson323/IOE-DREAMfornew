package net.lab1024.sa.common.system.employee.domain.vo;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Schema(description = "员工视图对象")
public class EmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "员工工号", example = "EMP001")
    private String employeeNo;

    @Schema(description = "登录账号", example = "zhangsan")
    private String loginName;

    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "备注", example = "新员工")
    private String remark;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "性别描述", example = "男")
    private String genderDesc;

    @Schema(description = "员工类型", example = "1")
    private Integer employeeType;

    @Schema(description = "员工类型描述", example = "正式")
    private String employeeTypeDesc;

    @Schema(description = "工龄", example = "5")
    private Integer workYears;
}

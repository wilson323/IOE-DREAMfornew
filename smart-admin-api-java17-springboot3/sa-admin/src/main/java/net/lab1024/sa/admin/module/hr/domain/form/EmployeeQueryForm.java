package net.lab1024.sa.admin.module.hr.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

import jakarta.validation.constraints.Min;

/**
 * 员工查询表单
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "员工查询表单")
public class EmployeeQueryForm extends PageParam {

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "状态：1-在职 2-离职", example = "1")
    private Integer status;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
}
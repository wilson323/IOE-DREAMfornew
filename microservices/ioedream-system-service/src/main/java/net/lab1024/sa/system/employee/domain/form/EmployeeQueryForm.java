package net.lab1024.sa.system.employee.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

/**
 * 员工查询表单
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "员工查询表单")
public class EmployeeQueryForm extends PageForm {

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "性别：1-男，2-女", example = "1")
    private Integer gender;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
}

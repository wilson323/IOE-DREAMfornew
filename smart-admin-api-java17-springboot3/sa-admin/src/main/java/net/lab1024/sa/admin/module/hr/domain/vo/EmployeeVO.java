package net.lab1024.sa.admin.module.hr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工信息VO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@Schema(description = "员工信息VO")
public class EmployeeVO {

    @Schema(description = "员工ID", example = "1")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "状态：1-在职 2-离职", example = "1")
    private Integer status;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "入职时间", example = "2023-01-01T09:00:00")
    private LocalDateTime hireDate;

    @Schema(description = "创建时间", example = "2023-01-01T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-01-01T09:00:00")
    private LocalDateTime updateTime;
}
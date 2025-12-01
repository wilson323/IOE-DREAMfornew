package net.lab1024.sa.system.employee.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工信息VO
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@Schema(description = "员工信息VO")
public class EmployeeVO {

    @Schema(description = "员工ID", example = "1")
    private Long employeeId;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "性别", example = "1")
    private Integer gender;

    @Schema(description = "性别描述", example = "男")
    private String genderDesc;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "软件工程师")
    private String position;

    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

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

    @Schema(description = "创建时间", example = "2023-01-01T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2023-01-01T09:00:00")
    private LocalDateTime updateTime;
}

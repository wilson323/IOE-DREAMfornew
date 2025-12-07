package net.lab1024.sa.common.system.employee.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;

/**
 * 员工查询DTO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Min验证分页参数
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class EmployeeQueryDTO {

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 员工状态
     */
    private Integer status;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 员工类型
     */
    private Integer employeeType;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Min(value = 1, message = "页大小必须大于0")
    private Integer pageSize = 20;
}


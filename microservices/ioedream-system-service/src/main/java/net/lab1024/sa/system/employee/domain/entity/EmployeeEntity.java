package net.lab1024.sa.system.employee.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 员工实体
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_hr_employee")
public class EmployeeEntity extends BaseEntity {

    /**
     * 员工ID
     */
    @TableId(value = "employee_id", type = IdType.AUTO)
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 性别：1-男，2-女
     */
    private Integer gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 薪资
     */
    private Double salary;

    /**
     * 入职日期
     */
    private LocalDate joinDate;

    /**
     * 地址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;
}
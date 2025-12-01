package net.lab1024.sa.common.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 员工实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_hr_employee")
public class EmployeeEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long employeeId;

    private String employeeName;

    /**
     * 性别：1男 2女
     */
    private Integer gender;

    private String email;

    private String phone;

    private Long departmentId;

    private String position;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    private String idCard;

    private Double salary;

    private LocalDate joinDate;

    private String address;

    private String remark;
}
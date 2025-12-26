package net.lab1024.sa.common.system.employee.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 员工实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_employee")
public class EmployeeEntity extends BaseEntity {

    /**
     * 员工ID（主键）
     */
    @TableId(value = "employee_id", type = IdType.AUTO)
    private Long id;

    /**
     * 员工姓名（业务字段，用于查询和显示）
     */
    @TableField("employee_name")
    private String employeeName;

    /**
     * 员工工号（业务字段，用于查询和唯一标识）
     */
    @TableField("employee_no")
    private String employeeNo;

    /**
     * 实际姓名（保留字段，可能与employeeName相同）
     */
    @TableField("actual_name")
    private String actualName;

    /**
     * 登录账号
     */
    @TableField("login_name")
    private String loginName;

    /**
     * 登录密码
     */
    private String loginPwd;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 关联用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 1:启用 0:禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 性别 1:男 2:女 0:未知
     */
    private Integer gender;

    /**
     * 员工类型 1:正式 2:试用 3:实习 4:外包
     */
    @TableField("employee_type")
    private Integer employeeType;

    /**
     * 入职日期
     */
    @TableField("hire_date")
    private java.time.LocalDate hireDate;

    /**
     * 身份证号
     */
    @TableField("id_card_no")
    private String idCardNo;
}

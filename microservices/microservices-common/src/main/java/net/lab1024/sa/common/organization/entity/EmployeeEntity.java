package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 员工实体类
 * <p>
 * 员工信息的数据模型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_employee")
public class EmployeeEntity extends BaseEntity {

    /**
     * 员工ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列employee_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "employee_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 员工编号
     */
    @TableField("employee_code")
    private String employeeCode;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 入职时间
     */
    @TableField("join_date")
    private LocalDateTime joinDate;

    /**
     * 状态（1-在职，2-离职，3-休假）
     */
    @TableField("status")
    private Integer status;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

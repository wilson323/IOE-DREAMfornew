package net.lab1024.sa.hr.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工信息实体
 *
 * @author IOE-DREAM Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_hr_employee")
public class EmployeeEntity {

    /**
     * 员工ID
     */
    @TableId(value = "employee_id", type = IdType.AUTO)
    private Long employeeId;

    /**
     * 员工编号
     */
    @TableField("employee_no")
    private String employeeNo;

    /**
     * 用户ID（关联身份服务）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 性别（1-男 2-女）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField("birth_date")
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 手机号码
     */
    @TableField("mobile")
    private String mobile;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 所属部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 职位ID
     */
    @TableField("position_id")
    private Long positionId;

    /**
     * 职级
     */
    @TableField("job_level")
    private String jobLevel;

    /**
     * 入职日期
     */
    @TableField("hire_date")
    private LocalDate hireDate;

    /**
     * 员工状态（1-试用期 2-正式 3-离职）
     */
    @TableField("employee_status")
    private Integer employeeStatus;

    /**
     * 工作地点
     */
    @TableField("work_location")
    private String workLocation;

    /**
     * 直接上级ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 基本工资
     */
    @TableField("base_salary")
    private BigDecimal baseSalary;

    /**
     * 银行账号
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 紧急联系人
     */
    @TableField("emergency_contact")
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    @TableField("emergency_phone")
    private String emergencyPhone;

    /**
     * 住址
     */
    @TableField("address")
    private String address;

    /**
     * 毕业院校
     */
    @TableField("university")
    private String university;

    /**
     * 专业
     */
    @TableField("major")
    private String major;

    /**
     * 学历（1-高中 2-大专 3-本科 4-硕士 5-博士）
     */
    @TableField("education")
    private Integer education;

    /**
     * 工作经验（年）
     */
    @TableField("work_experience")
    private Integer workExperience;

    /**
     * 技能特长
     */
    @TableField("skills")
    private String skills;

    /**
     * 合同类型（1-正式合同 2-实习合同 3-劳务合同）
     */
    @TableField("contract_type")
    private Integer contractType;

    /**
     * 合同开始日期
     */
    @TableField("contract_start_date")
    private LocalDate contractStartDate;

    /**
     * 合同结束日期
     */
    @TableField("contract_end_date")
    private LocalDate contractEndDate;

    /**
     * 社保号码
     */
    @TableField("social_security_no")
    private String socialSecurityNo;

    /**
     * 公积金账号
     */
    @TableField("provident_fund_no")
    private String providentFundNo;

    /**
     * 员工照片URL
     */
    @TableField("photo_url")
    private String photoUrl;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
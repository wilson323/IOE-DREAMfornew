package net.lab1024.sa.common.system.employee.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 员工实体
 * <p>
 * 企业员工档案管理，与User（系统账户）是一对一关系
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 继承BaseEntity获取公共字段
 * - 使用@TableName指定数据库表名
 * - 完整的员工档案字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_employee")
public class EmployeeEntity extends BaseEntity {

    /**
     * 员工ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列employee_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "employee_id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID（外键关联t_user表）
     */
    private Long userId;

    /**
     * 员工工号（唯一）
     */
    private String employeeNo;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 性别（1-男 2-女 3-未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称（冗余字段，便于查询）
     */
    private String departmentName;

    /**
     * 职位/岗位
     */
    private String position;

    /**
     * 职级
     */
    private String jobLevel;

    /**
     * 直属上级ID
     */
    private Long supervisorId;

    /**
     * 直属上级姓名（冗余字段）
     */
    private String supervisorName;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 转正日期
     */
    private LocalDate regularDate;

    /**
     * 离职日期
     */
    private LocalDate resignDate;

    /**
     * 员工状态（1-在职 2-离职 3-休假 4-停职）
     */
    private Integer status;

    /**
     * 员工类型（1-正式 2-实习 3-外包 4-兼职）
     */
    private Integer employeeType;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 工作电话
     */
    private String workPhone;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 家庭住址
     */
    private String homeAddress;

    /**
     * 学历（1-高中 2-大专 3-本科 4-硕士 5-博士）
     */
    private Integer education;

    /**
     * 毕业院校
     */
    private String graduateSchool;

    /**
     * 专业
     */
    private String major;

    /**
     * 工作经验（年）
     */
    private Integer workExperience;

    /**
     * 技能标签（JSON格式）
     */
    private String skills;

    /**
     * 合同类型（1-劳动合同 2-劳务合同 3-实习协议）
     */
    private Integer contractType;

    /**
     * 合同开始日期
     */
    private LocalDate contractStartDate;

    /**
     * 合同结束日期
     */
    private LocalDate contractEndDate;

    /**
     * 社保账号
     */
    private String socialSecurityNo;

    /**
     * 公积金账号
     */
    private String housingFundNo;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展字段（JSON格式）
     */
    private String extendedFields;
}


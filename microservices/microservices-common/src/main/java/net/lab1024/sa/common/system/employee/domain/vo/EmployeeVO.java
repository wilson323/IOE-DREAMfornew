package net.lab1024.sa.common.system.employee.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 员工VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@JsonFormat格式化时间字段
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class EmployeeVO {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 性别描述
     */
    private String genderDesc;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * 部门名称
     */
    private String departmentName;

    /**
     * 职位
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
     * 直属上级姓名
     */
    private String supervisorName;

    /**
     * 入职日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /**
     * 转正日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate regularDate;

    /**
     * 离职日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignDate;

    /**
     * 员工状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 员工类型
     */
    private Integer employeeType;

    /**
     * 员工类型描述
     */
    private String employeeTypeDesc;

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
     * 学历
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
     * 工龄（年）- 计算字段
     */
    private Integer workYears;

    /**
     * 技能标签
     */
    private String skills;

    /**
     * 合同类型
     */
    private Integer contractType;

    /**
     * 合同开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;

    /**
     * 合同结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}


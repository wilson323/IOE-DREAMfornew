package net.lab1024.sa.common.system.employee.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 员工新增DTO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@NotBlank/@NotNull验证必填字段
 * - 使用@Pattern验证格式
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class EmployeeAddDTO {

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 员工工号
     */
    @NotBlank(message = "员工工号不能为空")
    private String employeeNo;

    /**
     * 员工姓名
     */
    @NotBlank(message = "员工姓名不能为空")
    private String employeeName;

    /**
     * 性别
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", message = "身份证号格式不正确")
    private String idCardNo;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    /**
     * 部门ID
     */
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    /**
     * 职位
     */
    @NotBlank(message = "职位不能为空")
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
     * 入职日期
     */
    @NotNull(message = "入职日期不能为空")
    private LocalDate hireDate;

    /**
     * 员工类型
     */
    @NotNull(message = "员工类型不能为空")
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
}


package net.lab1024.sa.hr.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.dto.BaseDTO;

/**
 * 员工新增DTO
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "员工新增DTO")
public class EmployeeAddDTO extends BaseDTO {

    /**
     * 员工工号
     */
    @Schema(description = "员工工号", example = "EMP001")
    @NotBlank(message = "员工工号不能为空")
    @Size(max = 50, message = "员工工号长度不能超过50个字符")
    private String employeeCode;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 100, message = "员工姓名长度不能超过100个字符")
    private String employeeName;

    /**
     * 性别：1-男 2-女
     */
    @Schema(description = "性别", example = "1")
    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    /**
     * 出生日期
     */
    @Schema(description = "出生日期", example = "1990-01-01")
    @NotNull(message = "出生日期不能为空")
    @Past(message = "出生日期必须是过去的时间")
    private LocalDate birthDate;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号", example = "110101199001011234")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", message = "身份证号格式不正确")
    private String idCard;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@company.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 200, message = "邮箱长度不能超过200个字符")
    private String email;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    @NotNull(message = "部门ID不能为空")
    @Min(value = 1, message = "部门ID不能小于1")
    private Long departmentId;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID", example = "1")
    @NotNull(message = "岗位ID不能为空")
    @Min(value = 1, message = "岗位ID不能小于1")
    private Long positionId;

    /**
     * 职级ID
     */
    @Schema(description = "职级ID", example = "1")
    @Min(value = 1, message = "职级ID不能小于1")
    private Long rankId;

    /**
     * 员工类型：1-全职 2-兼职 3-实习 4-外包
     */
    @Schema(description = "员工类型", example = "1")
    @NotNull(message = "员工类型不能为空")
    @Min(value = 1, message = "员工类型值不正确")
    @Max(value = 4, message = "员工类型值不正确")
    private Integer employeeType;

    /**
     * 学历：1-高中 2-大专 3-本科 4-硕士 5-博士
     */
    @Schema(description = "学历", example = "3")
    @Min(value = 1, message = "学历值不正确")
    @Max(value = 5, message = "学历值不正确")
    private Integer education;

    /**
     * 毕业院校
     */
    @Schema(description = "毕业院校", example = "清华大学")
    @Size(max = 200, message = "毕业院校长度不能超过200个字符")
    private String graduateSchool;

    /**
     * 专业
     */
    @Schema(description = "专业", example = "计算机科学与技术")
    @Size(max = 200, message = "专业长度不能超过200个字符")
    private String major;

    /**
     * 入职时间
     */
    @Schema(description = "入职时间", example = "2023-01-01")
    @NotNull(message = "入职时间不能为空")
    @PastOrPresent(message = "入职时间不能是未来时间")
    private LocalDate entryDate;

    /**
     * 试用期结束时间
     */
    @Schema(description = "试用期结束时间", example = "2023-04-01")
    @Future(message = "试用期结束时间必须是未来时间")
    private LocalDate probationEndDate;

    /**
     * 劳动合同类型：1-无固定期限 2-固定期限 3-完成一定工作任务 4-临时用工
     */
    @Schema(description = "劳动合同类型", example = "2")
    @Min(value = 1, message = "劳动合同类型值不正确")
    @Max(value = 4, message = "劳动合同类型值不正确")
    private Integer contractType;

    /**
     * 合同开始时间
     */
    @Schema(description = "合同开始时间", example = "2023-01-01")
    @PastOrPresent(message = "合同开始时间不能是未来时间")
    private LocalDate contractStartDate;

    /**
     * 合同结束时间
     */
    @Schema(description = "合同结束时间", example = "2025-12-31")
    @Future(message = "合同结束时间必须是未来时间")
    private LocalDate contractEndDate;

    /**
     * 基本工资
     */
    @Schema(description = "基本工资", example = "8000.00")
    @DecimalMin(value = "0.00", message = "基本工资不能小于0")
    @Digits(integer = 10, fraction = 2, message = "基本工资格式不正确")
    private BigDecimal baseSalary;

    /**
     * 绩效工资
     */
    @Schema(description = "绩效工资", example = "2000.00")
    @DecimalMin(value = "0.00", message = "绩效工资不能小于0")
    @Digits(integer = 10, fraction = 2, message = "绩效工资格式不正确")
    private BigDecimal performanceSalary;

    /**
     * 社保状态：0-未参保 1-参保中 2-已停保
     */
    @Schema(description = "社保状态", example = "1")
    @Min(value = 0, message = "社保状态值不正确")
    @Max(value = 2, message = "社保状态值不正确")
    private Integer socialSecurityStatus;

    /**
     * 公积金状态：0-未缴存 1-缴存中 2-已停缴
     */
    @Schema(description = "公积金状态", example = "1")
    @Min(value = 0, message = "公积金状态值不正确")
    @Max(value = 2, message = "公积金状态值不正确")
    private Integer housingFundStatus;

    /**
     * 技能标签
     */
    @Schema(description = "技能标签", example = "Java,Spring,MySQL")
    @Size(max = 1000, message = "技能标签长度不能超过1000个字符")
    private String skills;

    /**
     * 紧急联系人姓名
     */
    @Schema(description = "紧急联系人姓名", example = "李四")
    @Size(max = 100, message = "紧急联系人姓名长度不能超过100个字符")
    private String emergencyContactName;

    /**
     * 紧急联系人电话
     */
    @Schema(description = "紧急联系人电话", example = "13900139000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系人电话格式不正确")
    private String emergencyContactPhone;

    /**
     * 紧急联系人关系
     */
    @Schema(description = "紧急联系人关系", example = "配偶")
    @Size(max = 50, message = "紧急联系人关系长度不能超过50个字符")
    private String emergencyContactRelation;

    /**
     * 家庭住址
     */
    @Schema(description = "家庭住址", example = "北京市朝阳区xxx街道")
    @Size(max = 500, message = "家庭住址长度不能超过500个字符")
    private String address;

    /**
     * 现居住地
     */
    @Schema(description = "现居住地", example = "北京市朝阳区xxx小区")
    @Size(max = 500, message = "现居住地长度不能超过500个字符")
    private String currentAddress;

    /**
     * 邮政编码
     */
    @Schema(description = "邮政编码", example = "100000")
    @Pattern(regexp = "^\\d{6}$", message = "邮政编码格式不正确")
    private String zipCode;

    /**
     * 直属上级ID
     */
    @Schema(description = "直属上级ID", example = "2")
    @Min(value = 1, message = "直属上级ID不能小于1")
    private Long superiorId;

    /**
     * 头像文件ID
     */
    @Schema(description = "头像文件ID", example = "123")
    private Long avatarFileId;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "优秀员工")
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;

    /**
     * 附件文件ID列表
     */
    @Schema(description = "附件文件ID列表", example = "[1, 2, 3]")
    private List<Long> attachmentFileIds;

    /**
     * 是否关键岗位
     */
    @Schema(description = "是否关键岗位", example = "false")
    private Boolean isKeyPosition;

    /**
     * 是否管理人员
     */
    @Schema(description = "是否管理人员", example = "false")
    private Boolean isManager;

    /**
     * 验证入职时间和试用期结束时间的逻辑关系
     */
    public boolean isValidProbationPeriod() {
        if (entryDate != null && probationEndDate != null) {
            return probationEndDate.isAfter(entryDate);
        }
        return true;
    }

    /**
     * 验证合同时间的逻辑关系
     */
    public boolean isValidContractPeriod() {
        if (contractStartDate != null && contractEndDate != null) {
            return contractEndDate.isAfter(contractStartDate);
        }
        return true;
    }

    /**
     * 获取工资总和
     */
    public BigDecimal getTotalSalary() {
        BigDecimal total = BigDecimal.ZERO;
        if (baseSalary != null) {
            total = total.add(baseSalary);
        }
        if (performanceSalary != null) {
            total = total.add(performanceSalary);
        }
        return total;
    }

    /**
     * 获取技能标签数组
     */
    public String[] getSkillArray() {
        if (skills != null && !skills.trim().isEmpty()) {
            return skills.split(",");
        }
        return new String[0];
    }
}

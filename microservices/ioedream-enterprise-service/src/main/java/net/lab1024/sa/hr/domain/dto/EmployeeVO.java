package net.lab1024.sa.hr.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.dto.BaseVO;

/**
 * 员工信息VO
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "员工信息VO")
public class EmployeeVO extends BaseVO {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1")
    private Long employeeId;

    /**
     * 员工工号
     */
    @Schema(description = "员工工号", example = "EMP001")
    private String employeeCode;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    /**
     * 性别：1-男 2-女
     */
    @Schema(description = "性别", example = "1")
    private Integer gender;

    /**
     * 性别描述
     */
    @Schema(description = "性别描述", example = "男")
    private String genderDesc;

    /**
     * 出生日期
     */
    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthDate;

    /**
     * 年龄
     */
    @Schema(description = "年龄", example = "33")
    private Integer age;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@company.com")
    private String email;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID", example = "1")
    private Long positionId;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称", example = "软件工程师")
    private String positionName;

    /**
     * 职级ID
     */
    @Schema(description = "职级ID", example = "1")
    private Long rankId;

    /**
     * 职级名称
     */
    @Schema(description = "职级名称", example = "P5")
    private String rankName;

    /**
     * 在职状态：1-在职 2-离职 3-休假 4-停薪留职
     */
    @Schema(description = "在职状态", example = "1")
    private Integer workStatus;

    /**
     * 在职状态描述
     */
    @Schema(description = "在职状态描述", example = "在职")
    private String workStatusDesc;

    /**
     * 员工类型：1-全职 2-兼职 3-实习 4-外包
     */
    @Schema(description = "员工类型", example = "1")
    private Integer employeeType;

    /**
     * 员工类型描述
     */
    @Schema(description = "员工类型描述", example = "全职")
    private String employeeTypeDesc;

    /**
     * 学历：1-高中 2-大专 3-本科 4-硕士 5-博士
     */
    @Schema(description = "学历", example = "3")
    private Integer education;

    /**
     * 学历描述
     */
    @Schema(description = "学历描述", example = "本科")
    private String educationDesc;

    /**
     * 毕业院校
     */
    @Schema(description = "毕业院校", example = "清华大学")
    private String graduateSchool;

    /**
     * 专业
     */
    @Schema(description = "专业", example = "计算机科学与技术")
    private String major;

    /**
     * 入职时间
     */
    @Schema(description = "入职时间", example = "2023-01-01")
    private LocalDate entryDate;

    /**
     * 试用期结束时间
     */
    @Schema(description = "试用期结束时间", example = "2023-04-01")
    private LocalDate probationEndDate;

    /**
     * 试用期状态：0-已转正 1-试用期中
     */
    @Schema(description = "试用期状态", example = "0")
    private Integer probationStatus;

    /**
     * 试用期状态描述
     */
    @Schema(description = "试用期状态描述", example = "已转正")
    private String probationStatusDesc;

    /**
     * 劳动合同类型：1-无固定期限 2-固定期限 3-完成一定工作任务 4-临时用工
     */
    @Schema(description = "劳动合同类型", example = "2")
    private Integer contractType;

    /**
     * 劳动合同类型描述
     */
    @Schema(description = "劳动合同类型描述", example = "固定期限")
    private String contractTypeDesc;

    /**
     * 合同开始时间
     */
    @Schema(description = "合同开始时间", example = "2023-01-01")
    private LocalDate contractStartDate;

    /**
     * 合同结束时间
     */
    @Schema(description = "合同结束时间", example = "2025-12-31")
    private LocalDate contractEndDate;

    /**
     * 基本工资
     */
    @Schema(description = "基本工资", example = "8000.00")
    private BigDecimal baseSalary;

    /**
     * 绩效工资
     */
    @Schema(description = "绩效工资", example = "2000.00")
    private BigDecimal performanceSalary;

    /**
     * 总工资
     */
    @Schema(description = "总工资", example = "10000.00")
    private BigDecimal totalSalary;

    /**
     * 社保状态：0-未参保 1-参保中 2-已停保
     */
    @Schema(description = "社保状态", example = "1")
    private Integer socialSecurityStatus;

    /**
     * 社保状态描述
     */
    @Schema(description = "社保状态描述", example = "参保中")
    private String socialSecurityStatusDesc;

    /**
     * 公积金状态：0-未缴存 1-缴存中 2-已停缴
     */
    @Schema(description = "公积金状态", example = "1")
    private Integer housingFundStatus;

    /**
     * 公积金状态描述
     */
    @Schema(description = "公积金状态描述", example = "缴存中")
    private String housingFundStatusDesc;

    /**
     * 工作年限
     */
    @Schema(description = "工作年限", example = "5")
    private Integer workYears;

    /**
     * 公司工作年限
     */
    @Schema(description = "公司工作年限", example = "2")
    private Integer companyWorkYears;

    /**
     * 技能标签
     */
    @Schema(description = "技能标签", example = "Java,Spring,MySQL")
    private String skills;

    /**
     * 技能标签数组
     */
    @Schema(description = "技能标签数组", example = "[\"Java\", \"Spring\", \"MySQL\"]")
    private String[] skillArray;

    /**
     * 紧急联系人姓名
     */
    @Schema(description = "紧急联系人姓名", example = "李四")
    private String emergencyContactName;

    /**
     * 紧急联系人电话
     */
    @Schema(description = "紧急联系人电话", example = "13900139000")
    private String emergencyContactPhone;

    /**
     * 紧急联系人关系
     */
    @Schema(description = "紧急联系人关系", example = "配偶")
    private String emergencyContactRelation;

    /**
     * 家庭住址
     */
    @Schema(description = "家庭住址", example = "北京市朝阳区xxx街道")
    private String address;

    /**
     * 现居住地
     */
    @Schema(description = "现居住地", example = "北京市朝阳区xxx小区")
    private String currentAddress;

    /**
     * 邮政编码
     */
    @Schema(description = "邮政编码", example = "100000")
    private String zipCode;

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
     * 直属上级ID
     */
    @Schema(description = "直属上级ID", example = "2")
    private Long superiorId;

    /**
     * 直属上级姓名
     */
    @Schema(description = "直属上级姓名", example = "李四")
    private String superiorName;

    /**
     * 离职时间
     */
    @Schema(description = "离职时间", example = "2023-12-31")
    private LocalDate leaveDate;

    /**
     * 离职原因
     */
    @Schema(description = "离职原因", example = "个人发展")
    private String leaveReason;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "/upload/avatar/employee_001.jpg")
    private String avatarUrl;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "优秀员工")
    private String remark;

    /**
     * 是否已删除
     */
    @Schema(description = "是否已删除", example = "false")
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    @Schema(description = "删除时间", example = "2023-12-31 23:59:59")
    private LocalDateTime deleteTime;

    /**
     * 删除人ID
     */
    @Schema(description = "删除人ID", example = "1")
    private Long deleteUserId;

    /**
     * 获取全名
     */
    public String getFullName() {
        return employeeName + "(" + employeeCode + ")";
    }

    /**
     * 获取工作信息描述
     */
    public String getWorkInfoDesc() {
        return departmentName + "-" + positionName + "-" + rankName;
    }

    /**
     * 是否在职
     */
    public boolean isActive() {
        return workStatus != null && workStatus == 1;
    }

    /**
     * 是否试用期中
     */
    public boolean isInProbation() {
        return probationStatus != null && probationStatus == 1;
    }
}

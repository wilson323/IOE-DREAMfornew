package net.lab1024.sa.hr.domain.form;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

/**
 * 员工查询表单
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "员工查询表单")
public class EmployeeQueryForm extends PageForm {

    /**
     * 员工姓名（模糊查询）
     */
    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    /**
     * 员工工号
     */
    @Schema(description = "员工工号", example = "EMP001")
    private String employeeCode;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    @Min(value = 1, message = "部门ID不能小于1")
    private Long departmentId;

    /**
     * 部门名称（模糊查询）
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 岗位ID
     */
    @Schema(description = "岗位ID", example = "1")
    private Long positionId;

    /**
     * 岗位名称（模糊查询）
     */
    @Schema(description = "岗位名称", example = "软件工程师")
    private String positionName;

    /**
     * 职级ID
     */
    @Schema(description = "职级ID", example = "1")
    private Long rankId;

    /**
     * 职级名称（模糊查询）
     */
    @Schema(description = "职级名称", example = "P5")
    private String rankName;

    /**
     * 在职状态：1-在职 2-离职 3-休假 4-停薪留职
     */
    @Schema(description = "在职状态", example = "1")
    private Integer workStatus;

    /**
     * 员工类型：1-全职 2-兼职 3-实习 4-外包
     */
    @Schema(description = "员工类型", example = "1")
    private Integer employeeType;

    /**
     * 性别：1-男 2-女
     */
    @Schema(description = "性别", example = "1")
    private Integer gender;

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
     * 学历：1-高中 2-大专 3-本科 4-硕士 5-博士
     */
    @Schema(description = "学历", example = "3")
    private Integer education;

    /**
     * 入职开始时间
     */
    @Schema(description = "入职开始时间", example = "2023-01-01")
    private LocalDate entryDateStart;

    /**
     * 入职结束时间
     */
    @Schema(description = "入职结束时间", example = "2023-12-31")
    private LocalDate entryDateEnd;

    /**
     * 离职开始时间
     */
    @Schema(description = "离职开始时间", example = "2023-01-01")
    private LocalDate leaveDateStart;

    /**
     * 离职结束时间
     */
    @Schema(description = "离职结束时间", example = "2023-12-31")
    private LocalDate leaveDateEnd;

    /**
     * 年龄开始
     */
    @Schema(description = "年龄开始", example = "20")
    private Integer ageStart;

    /**
     * 年龄结束
     */
    @Schema(description = "年龄结束", example = "50")
    private Integer ageEnd;

    /**
     * 工作年限开始
     */
    @Schema(description = "工作年限开始", example = "1")
    private Integer workYearsStart;

    /**
     * 工作年限结束
     */
    @Schema(description = "工作年限结束", example = "10")
    private Integer workYearsEnd;

    /**
     * 试用期状态：0-已转正 1-试用期中
     */
    @Schema(description = "试用期状态", example = "0")
    private Integer probationStatus;

    /**
     * 劳动合同类型：1-无固定期限 2-固定期限 3-完成一定工作任务 4-临时用工
     */
    @Schema(description = "劳动合同类型", example = "2")
    private Integer contractType;

    /**
     * 社保状态：0-未参保 1-参保中 2-已停保
     */
    @Schema(description = "社保状态", example = "1")
    private Integer socialSecurityStatus;

    /**
     * 公积金状态：0-未缴存 1-缴存中 2-已停缴
     */
    @Schema(description = "公积金状态", example = "1")
    private Integer housingFundStatus;

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
     * 技能标签（模糊查询）
     */
    @Schema(description = "技能标签", example = "Java,Spring")
    private String skills;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 创建开始时间
     */
    @Schema(description = "创建开始时间", example = "2023-01-01")
    private LocalDate createTimeStart;

    /**
     * 创建结束时间
     */
    @Schema(description = "创建结束时间", example = "2023-12-31")
    private LocalDate createTimeEnd;

    /**
     * 排序字段：1-入职时间 2-工号 3-姓名 4-年龄
     */
    @Schema(description = "排序字段", example = "1")
    private Integer sortField;

    /**
     * 获取排序字段名称（重写父类方法）
     */
    @Override
    public String getSortField() {
        if (this.sortField == null) {
            return "createTime"; // 默认按创建时间排序
        }
        switch (this.sortField) {
            case 1:
                return "entryDate";
            case 2:
                return "employeeCode";
            case 3:
                return "employeeName";
            case 4:
                return "birthDate";
            default:
                return "createTime";
        }
    }

    /**
     * 获取原始排序字段值
     */
    public Integer getOriginalSortField() {
        return sortField;
    }

    /**
     * 获取在职状态描述
     */
    public String getWorkStatusDesc() {
        switch (workStatus) {
            case 1:
                return "在职";
            case 2:
                return "离职";
            case 3:
                return "休假";
            case 4:
                return "停薪留职";
            default:
                return "未知";
        }
    }

    /**
     * 获取员工类型描述
     */
    public String getEmployeeTypeDesc() {
        switch (employeeType) {
            case 1:
                return "全职";
            case 2:
                return "兼职";
            case 3:
                return "实习";
            case 4:
                return "外包";
            default:
                return "未知";
        }
    }

    /**
     * 获取性别描述
     */
    public String getGenderDesc() {
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }
}

package net.lab1024.sa.hr.domain.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工更新DTO
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "员工更新DTO")
public class EmployeeUpdateDTO extends EmployeeAddDTO {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1")
    @NotNull(message = "员工ID不能为空")
    @Min(value = 1, message = "员工ID不能小于1")
    private Long employeeId;

    /**
     * 在职状态：1-在职 2-离职 3-休假 4-停薪留职
     */
    @Schema(description = "在职状态", example = "1")
    @Min(value = 1, message = "在职状态值不正确")
    @Max(value = 4, message = "在职状态值不正确")
    private Integer workStatus;

    /**
     * 离职时间
     */
    @Schema(description = "离职时间", example = "2023-12-31")
    @Past(message = "离职时间必须是过去的时间")
    private LocalDate leaveDate;

    /**
     * 离职原因
     */
    @Schema(description = "离职原因", example = "个人发展")
    @Size(max = 500, message = "离职原因长度不能超过500个字符")
    private String leaveReason;

    /**
     * 头像URL（更新时可能直接传入URL）
     */
    @Schema(description = "头像URL", example = "/upload/avatar/employee_001.jpg")
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    /**
     * 修改前的部门ID（用于变更记录）
     */
    @Schema(description = "修改前的部门ID", example = "1")
    private Long originalDepartmentId;

    /**
     * 修改前的岗位ID（用于变更记录）
     */
    @Schema(description = "修改前的岗位ID", example = "1")
    private Long originalPositionId;

    /**
     * 修改前的职级ID（用于变更记录）
     */
    @Schema(description = "修改前的职级ID", example = "1")
    private Long originalRankId;

    /**
     * 修改前的基本工资（用于变更记录）
     */
    @Schema(description = "修改前的基本工资", example = "7500.00")
    @DecimalMin(value = "0.00", message = "修改前的基本工资不能小于0")
    @Digits(integer = 10, fraction = 2, message = "修改前的基本工资格式不正确")
    private BigDecimal originalBaseSalary;

    /**
     * 变更原因
     */
    @Schema(description = "变更原因", example = "晋升")
    @Size(max = 500, message = "变更原因长度不能超过500个字符")
    private String changeReason;

    /**
     * 是否需要重新计算试用期
     */
    @Schema(description = "是否需要重新计算试用期", example = "false")
    private Boolean needRecalculateProbation;

    /**
     * 新的试用期结束时间
     */
    @Schema(description = "新的试用期结束时间", example = "2023-06-01")
    private LocalDate newProbationEndDate;

    /**
     * 是否需要变更合同
     */
    @Schema(description = "是否需要变更合同", example = "false")
    private Boolean needChangeContract;

    /**
     * 新合同结束时间
     */
    @Schema(description = "新合同结束时间", example = "2026-12-31")
    private LocalDate newContractEndDate;

    /**
     * 变更生效时间
     */
    @Schema(description = "变更生效时间", example = "2023-07-01")
    private LocalDate changeEffectiveDate;

    /**
     * 删除的附件文件ID列表
     */
    @Schema(description = "删除的附件文件ID列表", example = "[1, 2]")
    private List<Long> deletedAttachmentFileIds;

    /**
     * 是否为批量操作
     */
    @Schema(description = "是否为批量操作", example = "false")
    private Boolean isBatchOperation;

    /**
     * 批量操作的员工ID列表（批量操作时使用）
     */
    @Schema(description = "批量操作的员工ID列表", example = "[1, 2, 3]")
    private List<Long> batchEmployeeIds;

    /**
     * 验证离职时间与入职时间的逻辑关系
     */
    public boolean isValidLeaveDate() {
        if (getEntryDate() != null && leaveDate != null) {
            return leaveDate.isAfter(getEntryDate()) || leaveDate.isEqual(getEntryDate());
        }
        return true;
    }

    /**
     * 验证变更生效时间
     */
    public boolean isValidChangeEffectiveDate() {
        if (changeEffectiveDate != null) {
            return !changeEffectiveDate.isBefore(LocalDate.now());
        }
        return true;
    }

    /**
     * 验证新的试用期结束时间
     */
    public boolean isValidNewProbationEndDate() {
        if (needRecalculateProbation != null && needRecalculateProbation && newProbationEndDate != null
                && getEntryDate() != null) {
            return newProbationEndDate.isAfter(getEntryDate());
        }
        return true;
    }

    /**
     * 验证新合同结束时间
     */
    public boolean isValidNewContractEndDate() {
        if (needChangeContract != null && needChangeContract && newContractEndDate != null) {
            return newContractEndDate.isAfter(LocalDate.now());
        }
        return true;
    }

    /**
     * 检查是否有部门变更
     */
    public boolean hasDepartmentChange() {
        return originalDepartmentId != null && getDepartmentId() != null &&
                !originalDepartmentId.equals(getDepartmentId());
    }

    /**
     * 检查是否有岗位变更
     */
    public boolean hasPositionChange() {
        return originalPositionId != null && getPositionId() != null &&
                !originalPositionId.equals(getPositionId());
    }

    /**
     * 检查是否有职级变更
     */
    public boolean hasRankChange() {
        return originalRankId != null && getRankId() != null &&
                !originalRankId.equals(getRankId());
    }

    /**
     * 检查是否有工资变更
     */
    public boolean hasSalaryChange() {
        if (originalBaseSalary != null && getBaseSalary() != null) {
            return originalBaseSalary.compareTo(getBaseSalary()) != 0;
        }
        return false;
    }

    /**
     * 检查是否有关键信息变更
     */
    public boolean hasKeyInfoChange() {
        return hasDepartmentChange() || hasPositionChange() || hasRankChange() || hasSalaryChange();
    }

    /**
     * 获取工资变更幅度
     */
    public BigDecimal getSalaryChangeAmount() {
        if (originalBaseSalary != null && getBaseSalary() != null) {
            return getBaseSalary().subtract(originalBaseSalary);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取工资变更百分比
     */
    public BigDecimal getSalaryChangePercent() {
        if (originalBaseSalary != null && getBaseSalary() != null
                && originalBaseSalary.compareTo(BigDecimal.ZERO) > 0) {
            return getSalaryChangeAmount()
                    .divide(originalBaseSalary, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
}

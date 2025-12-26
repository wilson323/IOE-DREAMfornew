package net.lab1024.sa.common.gateway.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工信息响应对象
 * <p>
 * 用于跨服务传递员工信息，避免直接使用Entity
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工信息响应")
public class EmployeeResponse {

    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @Schema(description = "员工编号", example = "EMP001")
    private String employeeCode;

    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "部门ID", example = "100")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "职位", example = "高级工程师")
    private String position;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@company.com")
    private String email;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "员工状态", example = "1")
    private Integer status;

    @Schema(description = "入职日期", example = "2023-01-01T09:00:00")
    private LocalDateTime hireDate;

    @Schema(description = "基本工资", example = "15000.00")
    private BigDecimal baseSalary;

    @Schema(description = "工龄", example = "3")
    private Integer workYears;

    @Schema(description = "主管ID", example = "100")
    private Long managerId;

    @Schema(description = "主管姓名", example = "李四")
    private String managerName;

    @Schema(description = "办公位置", example = "A栋3楼")
    private String officeLocation;

    @Schema(description = "扩展属性")
    private Object extendedAttributes;
}
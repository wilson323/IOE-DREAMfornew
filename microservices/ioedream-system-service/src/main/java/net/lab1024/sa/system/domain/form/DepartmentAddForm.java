package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门新增表单
 * <p>
 * 用于部门新增的数据验证和传输
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "部门新增表单")
public class DepartmentAddForm {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    @Schema(description = "部门名称", example = "技术部", requiredMode = Schema.RequiredMode.REQUIRED)
    private String departmentName;

    /**
     * 部门编码
     */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "部门编码只能包含大写字母、数字和下划线")
    @Schema(description = "部门编码", example = "TECH", requiredMode = Schema.RequiredMode.REQUIRED)
    private String departmentCode;

    /**
     * 父部门ID
     */
    @NotNull(message = "父部门ID不能为空")
    @Schema(description = "父部门ID", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentId;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortNumber;

    /**
     * 部门状态（1-启用，0-禁用）
     */
    @Schema(description = "部门状态", example = "1", allowableValues = { "0", "1" })
    private Integer status;

    /**
     * 部门描述
     */
    @Size(max = 500, message = "部门描述长度不能超过500个字符")
    @Schema(description = "部门描述", example = "技术研发部")
    private String description;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$", message = "联系电话格式不正确")
    @Schema(description = "联系电话", example = "021-12345678")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "联系邮箱格式不正确")
    @Schema(description = "联系邮箱", example = "tech@company.com")
    private String contactEmail;

    /**
     * 部门负责人
     */
    @Size(max = 50, message = "部门负责人长度不能超过50个字符")
    @Schema(description = "部门负责人", example = "张三")
    private String manager;

    /**
     * 负责人ID
     */
    @Schema(description = "负责人ID", example = "100")
    private Long managerUserId;

    /**
     * 部门地址
     */
    @Size(max = 200, message = "部门地址长度不能超过200个字符")
    @Schema(description = "部门地址", example = "上海市浦东新区XXX路XXX号")
    private String address;

    /**
     * 扩展字段（JSON格式）
     */
    @Schema(description = "扩展字段（JSON格式）", example = "{\"budget\": \"1000000\", \"location\": \"floor3\"}")
    private String extendInfo;
}

package net.lab1024.sa.system.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门视图对象
 * <p>
 * 用于前端展示的部门信息 *
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "部门视图对象")
public class DepartmentVO {

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
     * 部门编码
     */
    @Schema(description = "部门编码", example = "TECH")
    private String departmentCode;

    /**
     * 父部门ID
     */
    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    /**
     * 父部门名称
     */
    @Schema(description = "父部门名称", example = "总公司")
    private String parentName;

    /**
     * 部门层级
     */
    @Schema(description = "部门层级", example = "1")
    private Integer level;

    /**
     * 部门路径
     */
    @Schema(description = "部门路径", example = "0,1,2")
    private String departmentPath;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortNumber;

    /**
     * 部门状态（1-启用，0-禁用）
     */
    @Schema(description = "部门状态", example = "1")
    private Integer status;

    /**
     * 部门状态名称
     */
    @Schema(description = "部门状态名称", example = "启用")
    private String statusName;

    /**
     * 部门描述
     */
    @Schema(description = "部门描述", example = "技术研发部")
    private String description;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "021-12345678")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱", example = "tech@company.com")
    private String contactEmail;

    /**
     * 部门负责人
     */
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
    @Schema(description = "部门地址", example = "上海市浦东新区XXX路XXX号")
    private String address;

    /**
     * 员工数量
     */
    @Schema(description = "员工数量", example = "25")
    private Integer employeeCount;

    /**
     * 子部门列表
     */
    @Schema(description = "子部门列表")
    private List<DepartmentVO> children;

    /**
     * 是否有子部门
     */
    @Schema(description = "是否有子部门", example = "true")
    private Boolean hasChildren;

    /**
     * 子部门数量
     */
    @Schema(description = "子部门数量", example = "3")
    private Integer childCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2025-01-01 12:00:00")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "管理员")
    private String createUserName;

    /**
     * 更新人
     */
    @Schema(description = "更新人", example = "管理员")
    private String updateUserName;

    /**
     * 扩展字段解析结果
     */
    @Schema(description = "扩展字段", example = "{}")
    private Object extendInfo;
}

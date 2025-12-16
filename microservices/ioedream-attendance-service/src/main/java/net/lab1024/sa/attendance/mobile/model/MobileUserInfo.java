package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 移动端用户信息
 * <p>
 * 封装移动端用户的详细信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "移动端用户信息")
public class MobileUserInfo {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 工号
     */
    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@company.com")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 职位ID
     */
    @Schema(description = "职位ID", example = "2001")
    private Long positionId;

    /**
     * 职位名称
     */
    @Schema(description = "职位名称", example = "软件工程师")
    private String positionName;

    /**
     * 用户头像URL
     */
    @Schema(description = "用户头像URL", example = "https://example.com/avatar/1001.jpg")
    private String avatarUrl;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    private String status;

    /**
     * 入职日期
     */
    @Schema(description = "入职日期", example = "2023-01-15")
    private String hireDate;

    /**
     * 工作地点
     */
    @Schema(description = "工作地点", example = "北京总部")
    private String workLocation;

    /**
     * 直属上级ID
     */
    @Schema(description = "直属上级ID", example = "1002")
    private Long supervisorId;

    /**
     * 直属上级姓名
     */
    @Schema(description = "直属上级姓名", example = "李四")
    private String supervisorName;

    /**
     * 权限列表
     */
    @Schema(description = "权限列表")
    private java.util.List<String> permissions;

    /**
     * 角色列表
     */
    @Schema(description = "角色列表")
    private java.util.List<String> roles;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性")
    private java.util.Map<String, Object> extendedAttributes;
}
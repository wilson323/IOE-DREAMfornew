package net.lab1024.sa.common.openapi.manager;

import net.lab1024.sa.common.openapi.domain.request.UserExtendedInfoRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 安全管理器接口
 * <p>
 * 提供用户安全相关的扩展信息管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
public interface SecurityManager {

    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户部门ID
     */
    Long getUserDepartmentId(Long userId);

    /**
     * 获取用户部门名称
     */
    String getUserDepartmentName(Long userId);

    /**
     * 获取用户职位
     */
    String getUserPosition(Long userId);

    /**
     * 获取用户工号
     */
    String getUserEmployeeNo(Long userId);

    /**
     * 获取用户性别
     */
    Integer getUserGender(Long userId);

    /**
     * 获取用户生日
     */
    LocalDate getUserBirthday(Long userId);

    /**
     * 更新用户扩展信息
     */
    void updateUserExtendedInfo(Long userId, UserExtendedInfoRequest request);

    /**
     * 检查用户是否拥有指定权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 获取用户权限详情列表（包含权限名称、编码等详细信息）
     */
    List<Object> getUserPermissionsWithDetails(Long userId);

    /**
     * 获取用户角色详情列表（包含角色名称、编码等详细信息）
     */
    List<Object> getUserRolesWithDetails(Long userId);
}


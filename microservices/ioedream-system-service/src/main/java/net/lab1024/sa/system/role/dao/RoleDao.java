package net.lab1024.sa.system.role.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.system.role.domain.entity.RoleEntity;

/**
 * 角色DAO
 * <p>
 * 提供角色的数据访问操作
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Mapper
public interface RoleDao extends BaseMapper<RoleEntity> {

    /**
     * 分页查询角色
     *
     * @param queryForm 查询条件
     * @return 角色列表
     */
    List<RoleEntity> selectRolePage(@Param("query") Map<String, Object> queryForm);

    /**
     * 根据条件查询角色列表
     *
     * @param queryForm 查询条件
     * @return 角色列表
     */
    List<RoleEntity> selectRoleList(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    List<RoleEntity> selectAllRoles();

    /**
     * 根据ID查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleEntity selectRoleDetail(@Param("roleId") Long roleId);

    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> selectRolePermissionIds(@Param("roleId") Long roleId);

    /**
     * 获取角色的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<Map<String, Object>> selectRoleUsers(@Param("roleId") Long roleId);

    /**
     * 检查角色编码是否唯一
     *
     * @param roleCode  角色编码
     * @param excludeId 排除的角色ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkRoleCodeUnique(@Param("roleCode") String roleCode,
            @Param("excludeId") Long excludeId);

    /**
     * 检查角色名称是否唯一
     *
     * @param roleName  角色名称
     * @param excludeId 排除的角色ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkRoleNameUnique(@Param("roleName") String roleName,
            @Param("excludeId") Long excludeId);

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param status 状态
     * @param userId 操作人ID
     * @return 影响行数
     */
    int updateRoleStatus(@Param("roleId") Long roleId,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 批量修改角色状态
     *
     * @param roleIds 角色ID列表
     * @param status  状态
     * @param userId  操作人ID
     * @return 影响行数
     */
    int batchUpdateRoleStatus(@Param("roleIds") List<Long> roleIds,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @param userId        操作人ID
     * @return 影响行数
     */
    int assignRolePermissions(@Param("roleId") Long roleId,
            @Param("permissionIds") List<Long> permissionIds,
            @Param("userId") Long userId);

    /**
     * 移除角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @param userId        操作人ID
     * @return 影响行数
     */
    int removeRolePermissions(@Param("roleId") Long roleId,
            @Param("permissionIds") List<Long> permissionIds,
            @Param("userId") Long userId);

    /**
     * 清空角色权限
     *
     * @param roleId 角色ID
     * @param userId 操作人ID
     * @return 影响行数
     */
    int clearRolePermissions(@Param("roleId") Long roleId,
            @Param("userId") Long userId);

    /**
     * 为角色分配用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @param userId  操作人ID
     * @return 影响行数
     */
    int assignRoleUsers(@Param("roleId") Long roleId,
            @Param("userIds") List<Long> userIds,
            @Param("userId") Long userId);

    /**
     * 移除角色用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @param userId  操作人ID
     * @return 影响行数
     */
    int removeRoleUsers(@Param("roleId") Long roleId,
            @Param("userIds") List<Long> userIds,
            @Param("userId") Long userId);

    /**
     * 清空角色用户
     *
     * @param roleId 角色ID
     * @param userId 操作人ID
     * @return 影响行数
     */
    int clearRoleUsers(@Param("roleId") Long roleId,
            @Param("userId") Long userId);

    /**
     * 检查角色是否被用户使用
     *
     * @param roleId 角色ID
     * @return 是否被用户使用（0-未使用，1-已使用）
     */
    int checkRoleUsedByUser(@Param("roleId") Long roleId);

    /**
     * 获取角色统计信息
     *
     * @return 角色统计信息
     */
    Map<String, Object> getRoleStatistics();

    /**
     * 根据状态查询角色
     *
     * @param status 状态
     * @return 角色列表
     */
    List<RoleEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据角色类型查询角色
     *
     * @param roleType 角色类型
     * @return 角色列表
     */
    List<RoleEntity> selectByRoleType(@Param("roleType") String roleType);

    /**
     * 根据角色名称模糊查询角色
     *
     * @param roleName 角色名称
     * @return 角色列表
     */
    List<RoleEntity> selectByRoleName(@Param("roleName") String roleName);

    /**
     * 查询系统内置角色
     *
     * @return 系统内置角色列表
     */
    List<RoleEntity> selectSystemRoles();

    /**
     * 查询自定义角色
     *
     * @return 自定义角色列表
     */
    List<RoleEntity> selectCustomRoles();

    /**
     * 获取角色及其用户数量
     *
     * @return 角色及用户数量
     */
    List<Map<String, Object>> selectRoleWithUserCount();

    /**
     * 获取角色及其权限数量
     *
     * @return 角色及权限数量
     */
    List<Map<String, Object>> selectRoleWithPermissionCount();

    /**
     * 获取角色的完整信息（包含权限和用户）
     *
     * @param roleId 角色ID
     * @return 角色完整信息
     */
    Map<String, Object> selectRoleFullInfo(@Param("roleId") Long roleId);

    /**
     * 批量插入角色
     *
     * @param roleList 角色列表
     * @return 影响行数
     */
    int batchInsertRole(@Param("roleList") List<RoleEntity> roleList);

    /**
     * 批量更新角色
     *
     * @param roleList 角色列表
     * @return 影响行数
     */
    int batchUpdateRole(@Param("roleList") List<RoleEntity> roleList);

    /**
     * 批量删除角色（逻辑删除）
     *
     * @param roleIds 角色ID列表
     * @param userId  操作人ID
     * @return 影响行数
     */
    int batchDeleteRole(@Param("roleIds") List<Long> roleIds,
            @Param("userId") Long userId);

    /**
     * 获取最近的角色
     *
     * @param limit 限制数量
     * @return 最近的角色列表
     */
    List<RoleEntity> selectRecentRoles(@Param("limit") Integer limit);

    /**
     * 根据用户ID获取角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据多个用户ID获取角色映射
     *
     * @param userIds 用户ID列表
     * @return 用户角色映射
     */
    List<Map<String, Object>> selectUserRoleMapping(@Param("userIds") List<Long> userIds);

    /**
     * 导出角色数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    List<Map<String, Object>> exportRoleData(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取角色缓存数据
     *
     * @return 角色缓存数据
     */
    List<Map<String, Object>> selectRoleCacheData();

    /**
     * 获取角色的所有权限标识
     *
     * @param roleId 角色ID
     * @return 权限标识列表
     */
    List<String> selectRolePermissionCodes(@Param("roleId") Long roleId);

    /**
     * 获取角色的菜单权限
     *
     * @param roleId 角色ID
     * @return 菜单权限列表
     */
    List<Map<String, Object>> selectRoleMenuPermissions(@Param("roleId") Long roleId);

    /**
     * 获取角色的按钮权限
     *
     * @param roleId 角色ID
     * @return 按钮权限列表
     */
    List<Map<String, Object>> selectRoleButtonPermissions(@Param("roleId") Long roleId);

    /**
     * 检查角色是否有指定权限
     *
     * @param roleId     角色ID
     * @param permission 权限标识
     * @return 是否有权限（0-没有，1-有）
     */
    int checkRoleHasPermission(@Param("roleId") Long roleId,
            @Param("permission") String permission);

    /**
     * 获取角色在指定模块的权限
     *
     * @param roleId 角色ID
     * @param module 模块标识
     * @return 模块权限
     */
    List<Map<String, Object>> selectRoleModulePermissions(@Param("roleId") Long roleId,
            @Param("module") String module);

    /**
     * 复制角色权限
     *
     * @param sourceRoleId 源角色ID
     * @param targetRoleId 目标角色ID
     * @param userId       操作人ID
     * @return 影响行数
     */
    int copyRolePermissions(@Param("sourceRoleId") Long sourceRoleId,
            @Param("targetRoleId") Long targetRoleId,
            @Param("userId") Long userId);

    /**
     * 获取角色的数据权限配置
     *
     * @param roleId 角色ID
     * @return 数据权限配置
     */
    Map<String, Object> selectRoleDataPermission(@Param("roleId") Long roleId);

    /**
     * 更新角色的数据权限配置
     *
     * @param roleId        角色ID
     * @param dataScope     数据范围
     * @param dataScopeRule 数据范围规则
     * @param userId        操作人ID
     * @return 影响行数
     */
    int updateRoleDataPermission(@Param("roleId") Long roleId,
            @Param("dataScope") String dataScope,
            @Param("dataScopeRule") String dataScopeRule,
            @Param("userId") Long userId);
}

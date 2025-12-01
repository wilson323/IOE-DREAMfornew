package net.lab1024.sa.system.role.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.system.role.domain.vo.RoleVO;

/**
 * 角色服务接口
 * <p>
 * 提供角色的CRUD操作和业务逻辑处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
public interface RoleService {

    /**
     * 新增角色
     *
     * @param roleForm 角色表单数据
     * @param userId   操作人ID
     * @return 角色ID
     */
    ResponseDTO<Long> addRole(Map<String, Object> roleForm, Long userId);

    /**
     * 更新角色
     *
     * @param roleForm 角色表单数据
     * @param userId   操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> updateRole(Map<String, Object> roleForm, Long userId);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @param userId 操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteRole(Long roleId, Long userId);

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    ResponseDTO<RoleVO> getRoleById(Long roleId);

    /**
     * 分页查询角色
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    ResponseDTO<PageResult<RoleVO>> queryRolePage(Map<String, Object> queryForm);

    /**
     * 查询角色列表
     *
     * @param queryForm 查询条件
     * @return 角色列表
     */
    ResponseDTO<List<RoleVO>> queryRoleList(Map<String, Object> queryForm);

    /**
     * 获取所有角色
     *
     * @return 角色列表
     */
    ResponseDTO<List<RoleVO>> getAllRoles();

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @param userId        操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> assignPermissions(Long roleId, List<Long> permissionIds, Long userId);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    ResponseDTO<List<Long>> getRolePermissions(Long roleId);

    /**
     * 为角色分配用户
     *
     * @param roleId  角色ID
     * @param userIds 用户ID列表
     * @param userId  操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> assignUsers(Long roleId, List<Long> userIds, Long userId);

    /**
     * 获取角色的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    ResponseDTO<List<Map<String, Object>>> getRoleUsers(Long roleId);

    /**
     * 刷新角色缓存
     *
     * @param roleId 角色ID，为空则刷新所有
     * @return 操作结果
     */
    ResponseDTO<Void> refreshRoleCache(Long roleId);

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param status 状态
     * @param userId 操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> changeRoleStatus(Long roleId, Integer status, Long userId);

    /**
     * 检查角色编码是否唯一
     *
     * @param roleCode  角色编码
     * @param excludeId 排除的角色ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkRoleCodeUnique(String roleCode, Long excludeId);

    /**
     * 检查角色名称是否唯一
     *
     * @param roleName  角色名称
     * @param excludeId 排除的角色ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkRoleNameUnique(String roleName, Long excludeId);

    /**
     * 检查角色是否被用户使用
     *
     * @param roleId 角色ID
     * @return 是否被用户使用
     */
    ResponseDTO<Boolean> isRoleUsedByUser(Long roleId);

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID列表
     * @param userId  操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeleteRole(List<Long> roleIds, Long userId);

    /**
     * 导出角色数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    ResponseDTO<List<Map<String, Object>>> exportRoleData(Map<String, Object> queryForm);

    /**
     * 导入角色数据
     *
     * @param importData 导入数据
     * @param userId     操作人ID
     * @return 导入结果
     */
    ResponseDTO<Map<String, Object>> importRoleData(List<Map<String, Object>> importData, Long userId);
}

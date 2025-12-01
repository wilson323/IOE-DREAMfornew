package net.lab1024.sa.auth.repository;

import net.lab1024.sa.auth.domain.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    /**
     * 根据角色代码查找角色
     */
    Optional<RoleEntity> findByRoleCode(String roleCode);

    /**
     * 根据角色名称查找角色
     */
    Optional<RoleEntity> findByRoleName(String roleName);

    /**
     * 检查角色代码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByRoleName(String roleName);

    /**
     * 根据状态查找角色
     */
    List<RoleEntity> findByStatus(Integer status);

    /**
     * 根据角色代码和状态查找角色
     */
    Optional<RoleEntity> findByRoleCodeAndStatus(String roleCode, Integer status);

    /**
     * 根据角色名称模糊查询
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.roleName LIKE %:roleName%")
    Page<RoleEntity> findByRoleNameContaining(@Param("roleName") String roleName, Pageable pageable);

    /**
     * 根据角色代码模糊查询
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.roleCode LIKE %:roleCode%")
    Page<RoleEntity> findByRoleCodeContaining(@Param("roleCode") String roleCode, Pageable pageable);

    /**
     * 查找系统内置角色
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.isBuiltIn = true")
    List<RoleEntity> findBuiltInRoles();

    /**
     * 查找非系统内置角色
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.isBuiltIn = false")
    List<RoleEntity> findCustomRoles();

    /**
     * 统计角色数量
     */
    @Query("SELECT COUNT(r) FROM RoleEntity r WHERE r.status = :status")
    long countByStatus(@Param("status") Integer status);

    /**
     * 根据权限查找包含该权限的角色
     */
    @Query("SELECT DISTINCT r FROM RoleEntity r JOIN r.rolePermissions rp WHERE rp.permission.permissionId = :permissionId")
    List<RoleEntity> findByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据多个权限ID查找包含任一权限的角色
     */
    @Query("SELECT DISTINCT r FROM RoleEntity r JOIN r.rolePermissions rp WHERE rp.permission.permissionId IN :permissionIds")
    List<RoleEntity> findByPermissionIds(@Param("permissionIds") List<Long> permissionIds);
}
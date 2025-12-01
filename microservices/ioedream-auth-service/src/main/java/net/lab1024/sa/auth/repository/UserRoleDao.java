package net.lab1024.sa.auth.repository;

import net.lab1024.sa.auth.domain.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关联数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    /**
     * 根据用户ID查找用户角色关联
     */
    List<UserRoleEntity> findByUserId(Long userId);

    /**
     * 根据角色ID查找用户角色关联
     */
    List<UserRoleEntity> findByRoleId(Long roleId);

    /**
     * 根据用户ID和角色ID查找关联
     */
    Optional<UserRoleEntity> findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 检查用户角色关联是否存在
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据用户ID和状态查找关联
     */
    List<UserRoleEntity> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据角色ID和状态查找关联
     */
    List<UserRoleEntity> findByRoleIdAndStatus(Long roleId, Integer status);

    /**
     * 查找有效的用户角色关联
     */
    List<UserRoleEntity> findByUserIdAndStatusAndRoleIdIn(Long userId, Integer status, List<Long> roleIds);

    /**
     * 统计用户的角色数量
     */
    @Query("SELECT COUNT(ur) FROM UserRoleEntity ur WHERE ur.userId = :userId AND ur.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 统计角色的用户数量
     */
    @Query("SELECT COUNT(ur) FROM UserRoleEntity ur WHERE ur.roleId = :roleId AND ur.status = :status")
    long countByRoleIdAndStatus(@Param("roleId") Long roleId, @Param("status") Integer status);

    /**
     * 删除用户的所有角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的所有用户关联
     */
    @Modifying
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.roleId = :roleId")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量更新用户角色关联状态
     */
    @Modifying
    @Query("UPDATE UserRoleEntity ur SET ur.status = :status WHERE ur.userId = :userId")
    int updateStatusByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 批量更新角色用户关联状态
     */
    @Modifying
    @Query("UPDATE UserRoleEntity ur SET ur.status = :status WHERE ur.roleId = :roleId")
    int updateStatusByRoleId(@Param("roleId") Long roleId, @Param("status") Integer status);
}
package net.lab1024.sa.auth.repository;

import net.lab1024.sa.auth.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    Optional<UserEntity> findByPhone(String phone);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<UserEntity> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据状态查找用户
     */
    List<UserEntity> findByStatus(Integer status);

    /**
     * 根据状态分页查找用户
     */
    Page<UserEntity> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据关键词和状态查找用户
     */
    @Query("SELECT u FROM UserEntity u WHERE (u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.email LIKE %:keyword%) AND u.status = :status")
    Page<UserEntity> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    /**
     * 查找已锁定的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.lockTime IS NOT NULL AND u.lockTime > :currentTime")
    List<UserEntity> findLockedUsers(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找长时间未登录的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.lastLoginTime < :lastLoginThreshold OR u.lastLoginTime IS NULL")
    List<UserEntity> findInactiveUsers(@Param("lastLoginThreshold") LocalDateTime lastLoginThreshold);

    /**
     * 根据真实姓名模糊查询
     */
    @Query("SELECT u FROM UserEntity u WHERE u.realName LIKE %:realName%")
    Page<UserEntity> findByRealNameContaining(@Param("realName") String realName, Pageable pageable);

    /**
     * 统计用户数量
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.status = :status")
    long countByStatus(@Param("status") Integer status);

    /**
     * 更新用户最后登录信息
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginTime = :loginTime, u.lastLoginIp = :loginIp, u.loginFailedCount = 0, u.lockTime = NULL WHERE u.userId = :userId")
    int updateLastLoginInfo(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime, @Param("loginIp") String loginIp);

    /**
     * 增加登录失败次数
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.loginFailedCount = u.loginFailedCount + 1 WHERE u.userId = :userId")
    int incrementLoginFailedCount(@Param("userId") Long userId);

    /**
     * 锁定用户账户
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.lockTime = :lockTime WHERE u.userId = :userId")
    int lockUser(@Param("userId") Long userId, @Param("lockTime") LocalDateTime lockTime);

    /**
     * 解锁用户账户
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.lockTime = NULL, u.loginFailedCount = 0 WHERE u.userId = :userId")
    int unlockUser(@Param("userId") Long userId);

    /**
     * 重置登录失败次数
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.loginFailedCount = 0, u.lockTime = NULL WHERE u.userId = :userId")
    int resetLoginFailedCount(@Param("userId") Long userId);

    /**
     * 更新用户状态
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.status = :status WHERE u.userId = :userId")
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查找用户权限
     */
    @Query("SELECT DISTINCT p.permissionCode FROM UserEntity u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.role r " +
           "JOIN r.rolePermissions rp " +
           "JOIN rp.permission p " +
           "WHERE u.userId = :userId AND u.status = 1 AND r.status = 1")
    List<String> findUserPermissions(@Param("userId") Long userId);

    /**
     * 根据角色代码和状态查找用户
     */
    @Query("SELECT u FROM UserEntity u " +
           "JOIN u.userRoles ur " +
           "JOIN ur.role r " +
           "WHERE r.roleCode = :roleCode AND u.status = :status")
    List<UserEntity> findByRoleCodeAndStatus(@Param("roleCode") String roleCode, @Param("status") int status);
}
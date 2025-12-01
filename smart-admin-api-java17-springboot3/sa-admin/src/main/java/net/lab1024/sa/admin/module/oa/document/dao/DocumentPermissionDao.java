package net.lab1024.sa.admin.module.oa.document.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentPermissionEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档权限DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface DocumentPermissionDao extends BaseMapper<DocumentPermissionEntity> {

    /**
     * 查询文档的所有权限
     *
     * @param documentId 文档ID
     * @return 权限列表
     */
    List<DocumentPermissionEntity> selectByDocumentId(Long documentId);

    /**
     * 查询用户的文档权限
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 权限列表
     */
    List<DocumentPermissionEntity> selectByDocumentAndUser(Long documentId, Long userId);

    /**
     * 查询用户拥有的文档权限
     *
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 权限列表
     */
    List<DocumentPermissionEntity> selectByUserAndPermission(Long userId, String permission);

    /**
     * 查询用户可访问的文档ID
     *
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 文档ID列表
     */
    List<Long> selectAccessibleDocumentIds(Long userId, String permission);

    /**
     * 检查用户是否有指定文档的指定权限
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    boolean checkUserPermission(Long documentId, Long userId, String permission);

    /**
     * 查询用户的文档权限总数
     *
     * @param userId 用户ID
     * @return 权限数量
     */
    Integer countByUserId(Long userId);

    /**
     * 查询文档的权限数量
     *
     * @param documentId 文档ID
     * @return 权限数量
     */
    Integer countByDocumentId(Long documentId);

    /**
     * 查询按权限类型分组的统计
     *
     * @param documentId 文档ID
     * @return 权限类型统计
     */
    List<Object> countByPermissionType(Long documentId);

    /**
     * 查询过期的权限
     *
     * @param currentTime 当前时间
     * @return 过期权限列表
     */
    List<DocumentPermissionEntity> selectExpiredPermissions(LocalDateTime currentTime);

    /**
     * 查询即将过期的权限
     *
     * @param hours 小时数
     * @return 即将过期权限列表
     */
    List<DocumentPermissionEntity> selectExpiringPermissions(Integer hours);

    /**
     * 查询被撤销的权限
     *
     * @param documentId 文档ID(可选)
     * @return 撤销权限列表
     */
    List<DocumentPermissionEntity> selectRevokedPermissions(Long documentId);

    /**
     * 批量撤销权限
     *
     * @param permissionIds 权限ID列表
     * @param revokedById 撤销人ID
     * @param revokedByName 撤销人姓名
     * @param revokedTime 撤销时间
     * @param revokeReason 撤销原因
     * @return 撤销数量
     */
    Integer batchRevokePermissions(List<Long> permissionIds, Long revokedById,
                                       String revokedByName, LocalDateTime revokedTime, String revokeReason);

    /**
     * 批量更新权限有效期
     *
     * @param permissionIds 权限ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 更新数量
     */
    Integer batchUpdateEffectiveTime(List<Long> permissionIds, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询部门权限
     *
     * @param documentId 文档ID
     * @param deptId 部门ID
     * @return 权限列表
     */
    List<DocumentPermissionEntity> selectByDepartment(Long documentId, Long deptId);

    /**
     * 查询角色权限
     *
     * @param documentId 文档ID
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<DocumentPermissionEntity> selectByRole(Long documentId, Long roleId);

    /**
     * 查询自定义权限
     *
     * @param documentId 文档ID
     * @return 权限列表
     */
    List<DocumentPermissionEntity> selectByCustomType(Long documentId);

    /**
     * 检查权限冲突
     *
     * @param documentId 文档ID
     * @param permissionType 权限类型
     * @param targetId 目标ID
     * @param excludePermissionId 排除的权限ID
     * @return 是否冲突
     */
    boolean hasPermissionConflict(Long documentId, String permissionType, Long targetId, Long excludePermissionId);

    /**
     * 获取权限统计
     *
     * @param documentId 文档ID(可选)
     * @param userId 用户ID(可选)
     * @return 权限统计
     */
    List<Object> getPermissionStatistics(Long documentId, Long userId);

    /**
     * 查询权限活动日志
     *
     * @param documentId 文档ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活动日志
     */
    List<Object> selectPermissionActivityLog(Long documentId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新权限访问信息
     *
     * @param permissionId 权限ID
     * @param accessCount 访问次数增量
     * @param lastAccessTime 最后访问时间
     * @return 更新数量
     */
    Integer updateAccessInfo(Long permissionId, Integer accessCount, LocalDateTime lastAccessTime);

    /**
     * 批量删除文档的所有权限
     *
     * @param documentId 文档ID
     * @return 删除数量
     */
    Integer deleteByDocumentId(Long documentId);

    /**
     * 删除用户的所有权限
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    Integer deleteByUserId(Long userId);

    /**
     * 清理过期的权限缓存标记
     *
     * @param expireDays 过期天数
     * @return 清理数量
     */
    Integer cleanExpiredPermissionCache(Integer expireDays);

    /**
     * 生成权限报告
     *
     * @param documentId 文档ID
     * @return 权限报告
     */
    List<Object> generatePermissionReport(Long documentId);
}
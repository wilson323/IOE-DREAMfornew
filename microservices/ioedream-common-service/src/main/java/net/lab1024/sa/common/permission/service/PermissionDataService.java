package net.lab1024.sa.common.permission.service;

import java.util.List;
import java.util.Set;

import net.lab1024.sa.common.permission.domain.vo.MenuPermissionVO;
import net.lab1024.sa.common.permission.domain.vo.PermissionDataVO;
import net.lab1024.sa.common.permission.domain.vo.PermissionStatsVO;
import net.lab1024.sa.common.permission.domain.vo.UserPermissionVO;

/**
 * 权限数据服务接口
 * <p>
 * 提供前后端权限一致性保障的统一权限数据服务
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
public interface PermissionDataService {

    /**
     * 获取用户完整权限数据
     *
     * @param userId 用户ID（为空则获取当前用户）
     * @return 用户权限数据
     */
    UserPermissionVO getUserPermissions(Long userId);

    /**
     * 获取菜单权限树结构
     *
     * @param userId 用户ID（为空则获取当前用户）
     * @return 菜单权限树
     */
    List<MenuPermissionVO> getMenuPermissionTree(Long userId);

    /**
     * 获取用户权限标识列表（webPerms格式）
     *
     * @param userId 用户ID（为空则获取当前用户）
     * @return 权限标识集合
     */
    Set<String> getUserPermissionCodes(Long userId);

    /**
     * 获取权限数据统计信息
     *
     * @return 权限统计信息
     */
    PermissionStatsVO getPermissionStats();

    /**
     * 获取完整权限数据（包含所有信息）
     *
     * @param userId 用户ID（为空则获取当前用户）
     * @return 完整权限数据
     */
    PermissionDataVO getFullPermissionData(Long userId);

    /**
     * 获取菜单权限列表
     *
     * @param userId 用户ID
     * @return 菜单权限列表
     */
    List<MenuPermissionVO> getMenuPermissions(Long userId);

    /**
     * 批量获取用户权限
     *
     * @param userIds 用户ID列表
     * @return 用户权限列表
     */
    List<UserPermissionVO> getBatchUserPermissions(List<Long> userIds);

    /**
     * 获取权限变更通知
     *
     * @param lastSyncTime 最后同步时间
     * @return 权限变更列表
     */
    List<PermissionDataVO> getPermissionChanges(Long lastSyncTime);

    /**
     * 确认权限同步
     *
     * @param userId      用户ID
     * @param dataVersion 数据版本号
     * @param syncType    同步类型
     */
    void confirmPermissionSync(Long userId, String dataVersion, String syncType);

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);

    /**
     * 批量清除权限缓存
     *
     * @param userIds 用户ID列表
     */
    void clearBatchPermissionCache(List<Long> userIds);
}

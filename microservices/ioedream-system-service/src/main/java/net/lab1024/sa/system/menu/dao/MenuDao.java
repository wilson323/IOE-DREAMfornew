package net.lab1024.sa.system.menu.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.system.menu.domain.entity.MenuEntity;

/**
 * 菜单DAO
 * <p>
 * 提供菜单的数据访问操作
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {

    /**
     * 分页查询菜单
     *
     * @param queryForm 查询条件
     * @return 菜单列表
     */
    List<MenuEntity> selectMenuPage(@Param("query") Map<String, Object> queryForm);

    /**
     * 根据条件查询菜单列表
     *
     * @param queryForm 查询条件
     * @return 菜单列表
     */
    List<MenuEntity> selectMenuList(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取所有菜单
     *
     * @return 菜单列表
     */
    List<MenuEntity> selectAllMenus();

    /**
     * 根据ID查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    MenuEntity selectMenuDetail(@Param("menuId") Long menuId);

    /**
     * 获取菜单树
     *
     * @param onlyEnabled 是否只查询启用的菜单
     * @return 菜单树
     */
    List<MenuEntity> selectMenuTree(@Param("onlyEnabled") Boolean onlyEnabled);

    /**
     * 获取用户的菜单
     *
     * @param userId 用户ID
     * @return 用户菜单列表
     */
    List<Map<String, Object>> selectUserMenu(@Param("userId") Long userId);

    /**
     * 获取用户的权限标识
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> selectUserPermissions(@Param("userId") Long userId);

    /**
     * 获取角色的权限标识
     *
     * @param roleId 角色ID
     * @return 权限标识列表
     */
    List<String> selectRolePermissions(@Param("roleId") Long roleId);

    /**
     * 获取所有菜单权限标识
     *
     * @return 权限标识列表
     */
    List<String> selectAllMenuPermissions();

    /**
     * 根据父菜单ID查询子菜单
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    List<MenuEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据菜单类型查询菜单
     *
     * @param menuType 菜单类型
     * @return 菜单列表
     */
    List<MenuEntity> selectByMenuType(@Param("menuType") Integer menuType);

    /**
     * 检查菜单名称在父菜单下是否唯一
     *
     * @param parentId  父菜单ID
     * @param menuName  菜单名称
     * @param excludeId 排除的菜单ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkMenuNameUnique(@Param("parentId") Long parentId,
            @Param("menuName") String menuName,
            @Param("excludeId") Long excludeId);

    /**
     * 检查菜单编码是否唯一
     *
     * @param menuCode  菜单编码
     * @param excludeId 排除的菜单ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkMenuCodeUnique(@Param("menuCode") String menuCode,
            @Param("excludeId") Long excludeId);

    /**
     * 修改菜单状态
     *
     * @param menuId 菜单ID
     * @param status 状态
     * @param userId 操作人ID
     * @return 影响行数
     */
    int updateMenuStatus(@Param("menuId") Long menuId,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 批量修改菜单状态
     *
     * @param menuIds 菜单ID列表
     * @param status  状态
     * @param userId  操作人ID
     * @return 影响行数
     */
    int batchUpdateMenuStatus(@Param("menuIds") List<Long> menuIds,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 移动菜单（更新父菜单ID）
     *
     * @param menuId      菜单ID
     * @param newParentId 新父菜单ID
     * @param userId      操作人ID
     * @return 影响行数
     */
    int moveMenu(@Param("menuId") Long menuId,
            @Param("newParentId") Long newParentId,
            @Param("userId") Long userId);

    /**
     * 检查是否存在子菜单
     *
     * @param menuId 菜单ID
     * @return 子菜单数量
     */
    int countChildMenu(@Param("menuId") Long menuId);

    /**
     * 检查菜单是否被角色使用
     *
     * @param menuId 菜单ID
     * @return 是否被角色使用（0-未使用，1-已使用）
     */
    int checkMenuUsedByRole(@Param("menuId") Long menuId);

    /**
     * 获取菜单统计信息
     *
     * @return 菜单统计信息
     */
    Map<String, Object> getMenuStatistics();

    /**
     * 根据状态查询菜单
     *
     * @param status 状态
     * @return 菜单列表
     */
    List<MenuEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据菜单名称模糊查询菜单
     *
     * @param menuName 菜单名称
     * @return 菜单列表
     */
    List<MenuEntity> selectByMenuName(@Param("menuName") String menuName);

    /**
     * 根据权限标识查询菜单
     *
     * @param permission 权限标识
     * @return 菜单列表
     */
    List<MenuEntity> selectByPermission(@Param("permission") String permission);

    /**
     * 获取菜单及其子菜单数量
     *
     * @return 菜单及子菜单数量
     */
    List<Map<String, Object>> selectMenuWithChildCount();

    /**
     * 获取菜单路径
     *
     * @param menuId 菜单ID
     * @return 菜单路径
     */
    List<Map<String, Object>> selectMenuPath(@Param("menuId") Long menuId);

    /**
     * 获取顶级菜单
     *
     * @return 顶级菜单列表
     */
    List<MenuEntity> selectTopMenu();

    /**
     * 获取启用的菜单（用于路由）
     *
     * @return 启用的菜单列表
     */
    List<MenuEntity> selectEnabledMenu();

    /**
     * 批量插入菜单
     *
     * @param menuList 菜单列表
     * @return 影响行数
     */
    int batchInsertMenu(@Param("menuList") List<MenuEntity> menuList);

    /**
     * 批量更新菜单
     *
     * @param menuList 菜单列表
     * @return 影响行数
     */
    int batchUpdateMenu(@Param("menuList") List<MenuEntity> menuList);

    /**
     * 批量删除菜单（逻辑删除）
     *
     * @param menuIds 菜单ID列表
     * @param userId  操作人ID
     * @return 影响行数
     */
    int batchDeleteMenu(@Param("menuIds") List<Long> menuIds,
            @Param("userId") Long userId);

    /**
     * 根据父菜单删除所有子菜单（逻辑删除）
     *
     * @param parentId 父菜单ID
     * @param userId   操作人ID
     * @return 影响行数
     */
    int deleteMenuByParentId(@Param("parentId") Long parentId,
            @Param("userId") Long userId);

    /**
     * 获取最近的菜单
     *
     * @param limit 限制数量
     * @return 最近的菜单列表
     */
    List<MenuEntity> selectRecentMenu(@Param("limit") Integer limit);

    /**
     * 获取菜单的最大排序号
     *
     * @param parentId 父菜单ID
     * @return 最大排序号
     */
    Integer getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 更新菜单排序
     *
     * @param menuId    菜单ID
     * @param sortOrder 排序号
     * @param userId    操作人ID
     * @return 影响行数
     */
    int updateSortOrder(@Param("menuId") Long menuId,
            @Param("sortOrder") Integer sortOrder,
            @Param("userId") Long userId);

    /**
     * 批量更新菜单排序
     *
     * @param sortUpdates 排序更新列表
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchUpdateSortOrder(@Param("sortUpdates") List<Map<String, Object>> sortUpdates,
            @Param("userId") Long userId);

    /**
     * 获取菜单缓存数据
     *
     * @return 菜单缓存数据
     */
    List<Map<String, Object>> selectMenuCacheData();

    /**
     * 导出菜单数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    List<Map<String, Object>> exportMenuData(@Param("query") Map<String, Object> queryForm);

    /**
     * 根据角色获取菜单
     *
     * @param roleId 角色ID
     * @return 角色菜单列表
     */
    List<MenuEntity> selectMenuByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据多个角色获取菜单
     *
     * @param roleIds 角色ID列表
     * @return 菜单列表
     */
    List<MenuEntity> selectMenuByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 获取菜单的权限范围
     *
     * @param menuId 菜单ID
     * @return 权限范围
     */
    Map<String, Object> getMenuPermissionScope(@Param("menuId") Long menuId);
}

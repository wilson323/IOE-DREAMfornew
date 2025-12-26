package net.lab1024.sa.common.menu.service;

import java.util.List;

import net.lab1024.sa.common.menu.entity.MenuEntity;

/**
 * 菜单服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
public interface MenuService {

    /**
     * 获取用户菜单树
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<MenuEntity> getUserMenuTree(Long userId);

    /**
     * 获取用户菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<MenuEntity> getUserMenuList(Long userId);

    /**
     * 获取完整菜单树
     *
     * @return 菜单树
     */
    List<MenuEntity> getFullMenuTree();

    /**
     * 获取子菜单
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    List<MenuEntity> getChildMenus(Long parentId);

    /**
     * 检查用户菜单权限
     *
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否有权限
     */
    boolean hasMenuPermission(Long userId, Long menuId);

    /**
     * 检查用户权限标识
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 根据编码获取菜单
     *
     * @param menuCode 菜单编码
     * @return 菜单实体
     */
    MenuEntity getMenuByCode(String menuCode);

    /**
     * 根据权限标识获取菜单
     *
     * @param permission 权限标识
     * @return 菜单实体
     */
    MenuEntity getMenuByPermission(String permission);

    /**
     * 获取用户权限标识列表
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取菜单面包屑
     *
     * @param menuId 菜单ID
     * @return 面包屑列表
     */
    List<MenuEntity> getMenuBreadcrumb(Long menuId);

    /**
     * 构建菜单树
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    List<MenuEntity> buildMenuTree(List<MenuEntity> menuList);
}

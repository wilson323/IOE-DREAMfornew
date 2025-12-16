package net.lab1024.sa.common.menu.manager;

import net.lab1024.sa.common.menu.dao.MenuDao;
import net.lab1024.sa.common.menu.entity.MenuEntity;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 菜单管理器
 * <p>
 * 遵循四层架构规范：Controller → Service → Manager → DAO
 * 纯Java类，不使用Spring注解，通过构造函数注入依赖
 * 负责菜单数据的业务逻辑处理和复杂流程编排
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class MenuManager {

    private final MenuDao menuDao;
    private final EmployeeDao employeeDao;

    public MenuManager(MenuDao menuDao, EmployeeDao employeeDao) {
        this.menuDao = menuDao;
        this.employeeDao = employeeDao;
    }

    /**
     * 获取用户菜单树（含缓存优化）
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    public List<MenuEntity> getUserMenuTree(Long userId) {
        log.info("[菜单管理器] 获取用户菜单树, userId={}", userId);

        // 验证用户对应的员工是否存在（通过userId查询员工）
        if (employeeDao.selectByUserId(userId) == null) {
            log.warn("[菜单管理器] 用户对应的员工不存在, userId={}", userId);
            return new ArrayList<>();
        }

        // 查询用户菜单
        List<MenuEntity> menuList = menuDao.selectMenuListByUserId(userId);

        // 构建菜单树
        return buildMenuTree(menuList);
    }

    /**
     * 构建菜单树形结构
     *
     * @param menuList 菜单列表
     * @return 菜单树
     */
    public List<MenuEntity> buildMenuTree(List<MenuEntity> menuList) {
        if (menuList == null || menuList.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建菜单映射
        Map<Long, MenuEntity> menuMap = new HashMap<>();
        for (MenuEntity menu : menuList) {
            menuMap.put(menu.getId(), menu);
        }

        // 构建树形结构
        List<MenuEntity> menuTree = new ArrayList<>();
        for (MenuEntity menu : menuList) {
            Long parentId = menu.getParentId();

            if (parentId == null || parentId == 0) {
                // 根节点
                menuTree.add(menu);
            } else {
                // 子节点
                MenuEntity parentMenu = menuMap.get(parentId);
                if (parentMenu != null) {
                    if (parentMenu.getChildren() == null) {
                        parentMenu.setChildren(new ArrayList<>());
                    }
                    parentMenu.getChildren().add(menu);
                }
            }
        }

        // 排序
        sortMenuTree(menuTree);

        return menuTree;
    }

    /**
     * 递归排序菜单树
     */
    private void sortMenuTree(List<MenuEntity> menuList) {
        if (menuList == null || menuList.isEmpty()) {
            return;
        }

        menuList.sort((m1, m2) -> {
            Integer sort1 = m1.getSortOrder() != null ? m1.getSortOrder() : 999;
            Integer sort2 = m2.getSortOrder() != null ? m2.getSortOrder() : 999;

            int sortCompare = sort1.compareTo(sort2);
            if (sortCompare != 0) {
                return sortCompare;
            }

            Long id1 = m1.getId() != null ? m1.getId() : 0L;
            Long id2 = m2.getId() != null ? m2.getId() : 0L;

            return id1.compareTo(id2);
        });

        for (MenuEntity menu : menuList) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortMenuTree(menu.getChildren());
            }
        }
    }

    /**
     * 获取菜单面包屑导航
     *
     * @param menuId 菜单ID
     * @return 面包屑列表
     */
    public List<MenuEntity> getMenuBreadcrumb(Long menuId) {
        List<MenuEntity> breadcrumb = new ArrayList<>();

        MenuEntity currentMenu = menuDao.selectById(menuId);
        if (currentMenu == null) {
            return breadcrumb;
        }

        breadcrumb.add(currentMenu);

        // 递归查找父菜单
        Long parentId = currentMenu.getParentId();
        while (parentId != null && parentId > 0) {
            MenuEntity parentMenu = menuDao.selectById(parentId);
            if (parentMenu == null) {
                break;
            }

            breadcrumb.add(0, parentMenu);
            parentId = parentMenu.getParentId();
        }

        return breadcrumb;
    }

    /**
     * 获取用户所有权限标识
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    public List<String> getUserPermissions(Long userId) {
        List<MenuEntity> userMenus = menuDao.selectMenuListByUserId(userId);

        return userMenus.stream()
                .filter(menu -> menu.getPermission() != null && !menu.getPermission().trim().isEmpty())
                .map(MenuEntity::getPermission)
                .distinct()
                .toList();
    }

    /**
     * 检查用户是否有菜单权限
     *
     * @param userId 用户ID
     * @param menuId 菜单ID
     * @return 是否有权限
     */
    public boolean hasMenuPermission(Long userId, Long menuId) {
        List<MenuEntity> userMenus = menuDao.selectMenuListByUserId(userId);
        return userMenus.stream()
                .anyMatch(menu -> menu.getId().equals(menuId));
    }

    /**
     * 检查用户是否有权限标识
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(Long userId, String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }

        List<MenuEntity> userMenus = menuDao.selectMenuListByUserId(userId);
        return userMenus.stream()
                .filter(menu -> menu.getPermission() != null)
                .anyMatch(menu -> permission.equals(menu.getPermission()));
    }
}

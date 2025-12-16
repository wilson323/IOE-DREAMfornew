package net.lab1024.sa.common.menu.service.impl;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.menu.dao.MenuDao;
import net.lab1024.sa.common.menu.entity.MenuEntity;
import net.lab1024.sa.common.menu.service.MenuService;
import net.lab1024.sa.common.system.employee.dao.EmployeeDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuDao menuDao;

    @Resource
    private EmployeeDao employeeDao;

    @Override
    @Observed(name = "menu.getUserMenuTree", contextualName = "menu-get-user-tree")
    public List<MenuEntity> getUserMenuTree(Long userId) {
        log.info("[菜单服务] 获取用户菜单树, userId={}", userId);

        // 获取用户菜单列表
        List<MenuEntity> menuList = getUserMenuList(userId);

        // 构建菜单树
        List<MenuEntity> menuTree = buildMenuTree(menuList);

        log.info("[菜单服务] 用户菜单树构建完成, userId={}, menuCount={}", userId, menuTree.size());
        return menuTree;
    }

    @Override
    @Observed(name = "menu.getUserMenuList", contextualName = "menu-get-user-list")
    public List<MenuEntity> getUserMenuList(Long userId) {
        log.debug("[菜单服务] 获取用户菜单列表, userId={}", userId);

        List<MenuEntity> menuList = menuDao.selectMenuListByUserId(userId);

        log.debug("[菜单服务] 用户菜单列表获取完成, userId={}, menuCount={}", userId, menuList.size());
        return menuList;
    }

    @Override
    @Observed(name = "menu.getFullMenuTree", contextualName = "menu-get-full-tree")
    public List<MenuEntity> getFullMenuTree() {
        log.debug("[菜单服务] 获取完整菜单树");

        List<MenuEntity> menuList = menuDao.selectMenuTree();
        return buildMenuTree(menuList);
    }

    @Override
    @Observed(name = "menu.getChildMenus", contextualName = "menu-get-children")
    public List<MenuEntity> getChildMenus(Long parentId) {
        log.debug("[菜单服务] 获取子菜单, parentId={}", parentId);

        return menuDao.selectChildMenus(parentId);
    }

    @Override
    @Observed(name = "menu.hasMenuPermission", contextualName = "menu-has-menu-permission")
    public boolean hasMenuPermission(Long userId, Long menuId) {
        log.debug("[菜单服务] 检查用户菜单权限, userId={}, menuId={}", userId, menuId);

        List<MenuEntity> userMenus = getUserMenuList(userId);
        return userMenus.stream()
                .anyMatch(menu -> menu.getId().equals(menuId));
    }

    @Override
    @Observed(name = "menu.hasPermission", contextualName = "menu-has-permission")
    public boolean hasPermission(Long userId, String permission) {
        log.debug("[菜单服务] 检查用户权限标识, userId={}, permission={}", userId, permission);

        List<String> userPermissions = getUserPermissions(userId);
        return userPermissions.contains(permission);
    }

    @Override
    @Observed(name = "menu.getMenuByCode", contextualName = "menu-get-by-code")
    public MenuEntity getMenuByCode(String menuCode) {
        log.debug("[菜单服务] 根据编码获取菜单, menuCode={}", menuCode);

        return menuDao.selectByMenuCode(menuCode);
    }

    @Override
    @Observed(name = "menu.getMenuByPermission", contextualName = "menu-get-by-permission")
    public MenuEntity getMenuByPermission(String permission) {
        log.debug("[菜单服务] 根据权限标识获取菜单, permission={}", permission);

        return menuDao.selectByPermission(permission);
    }

    @Override
    @Observed(name = "menu.getUserPermissions", contextualName = "menu-get-user-permissions")
    public List<String> getUserPermissions(Long userId) {
        log.debug("[菜单服务] 获取用户权限标识列表, userId={}", userId);

        List<MenuEntity> userMenus = getUserMenuList(userId);

        return userMenus.stream()
                .filter(menu -> menu.getPermission() != null && !menu.getPermission().trim().isEmpty())
                .map(MenuEntity::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Observed(name = "menu.getMenuBreadcrumb", contextualName = "menu-get-breadcrumb")
    public List<MenuEntity> getMenuBreadcrumb(Long menuId) {
        log.debug("[菜单服务] 获取菜单面包屑, menuId={}", menuId);

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

            breadcrumb.add(0, parentMenu); // 插入到列表开头
            parentId = parentMenu.getParentId();
        }

        return breadcrumb;
    }

    @Override
    @Observed(name = "menu.buildMenuTree", contextualName = "menu-build-tree")
    public List<MenuEntity> buildMenuTree(List<MenuEntity> menuList) {
        log.debug("[菜单服务] 构建菜单树, menuCount={}", menuList.size());

        if (menuList == null || menuList.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建菜单映射，提高查找效率
        Map<Long, MenuEntity> menuMap = menuList.stream()
                .collect(Collectors.toMap(MenuEntity::getId, menu -> menu));

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

        // 对每个层级的菜单进行排序
        sortMenuTree(menuTree);

        log.debug("[菜单服务] 菜单树构建完成, rootCount={}", menuTree.size());
        return menuTree;
    }

    @Override
    @Observed(name = "menu.getMenuStatistics", contextualName = "menu-get-statistics")
    public Map<String, Object> getMenuStatistics() {
        log.debug("[菜单服务] 获取菜单统计信息");

        Map<String, Object> statistics = new HashMap<>();

        // 获取所有菜单
        List<MenuEntity> allMenus = menuDao.selectMenuTree();

        // 统计各类菜单数量
        long directoryCount = allMenus.stream()
                .filter(menu -> menu.getMenuType() != null && menu.getMenuType() == 1)
                .count();

        long menuCount = allMenus.stream()
                .filter(menu -> menu.getMenuType() != null && menu.getMenuType() == 2)
                .count();

        long functionCount = allMenus.stream()
                .filter(menu -> menu.getMenuType() != null && menu.getMenuType() == 3)
                .count();

        long visibleCount = allMenus.stream()
                .filter(menu -> menu.getIsVisible() != null && menu.getIsVisible() == 1)
                .count();

        long disabledCount = allMenus.stream()
                .filter(menu -> menu.getIsDisabled() != null && menu.getIsDisabled() == 1)
                .count();

        statistics.put("totalCount", allMenus.size());
        statistics.put("directoryCount", directoryCount);
        statistics.put("menuCount", menuCount);
        statistics.put("functionCount", functionCount);
        statistics.put("visibleCount", visibleCount);
        statistics.put("disabledCount", disabledCount);
        statistics.put("enabledCount", allMenus.size() - disabledCount);

        return statistics;
    }

    /**
     * 递归排序菜单树
     *
     * @param menuList 菜单列表
     */
    private void sortMenuTree(List<MenuEntity> menuList) {
        if (menuList == null || menuList.isEmpty()) {
            return;
        }

        // 按sortOrder和menuId排序
        menuList.sort((m1, m2) -> {
            // 先按sortOrder排序
            Integer sort1 = m1.getSortOrder() != null ? m1.getSortOrder() : 999;
            Integer sort2 = m2.getSortOrder() != null ? m2.getSortOrder() : 999;

            int sortCompare = sort1.compareTo(sort2);
            if (sortCompare != 0) {
                return sortCompare;
            }

            // 再按menuId排序
            Long id1 = m1.getId() != null ? m1.getId() : 0L;
            Long id2 = m2.getId() != null ? m2.getId() : 0L;

            return id1.compareTo(id2);
        });

        // 递归排序子菜单
        for (MenuEntity menu : menuList) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                sortMenuTree(menu.getChildren());
            }
        }
    }
}

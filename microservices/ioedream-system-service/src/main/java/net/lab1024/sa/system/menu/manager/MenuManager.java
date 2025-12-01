package net.lab1024.sa.system.menu.manager;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.system.menu.dao.MenuDao;
import net.lab1024.sa.system.menu.domain.entity.MenuEntity;

/**
 * 菜单管理器
 * <p>
 * 提供菜单相关的业务逻辑封装
 * 遵循四层架构：Service → Manager → DAO
 *
 * @author SmartAdmin Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class MenuManager {

    @Resource
    private MenuDao menuDao;

    /**
     * 获取下一个排序号
     *
     * @param parentId 父菜单ID
     * @return 下一个排序号
     */
    public Integer getNextSortOrder(Long parentId) {
        try {
            Integer maxSortOrder = menuDao.getMaxSortOrder(parentId);
            return maxSortOrder != null ? maxSortOrder + 1 : 1;
        } catch (Exception e) {
            log.error("获取下一个排序号失败，parentId：{}", parentId, e);
            return 1;
        }
    }

    /**
     * 检查是否是子菜单（防止循环引用）
     *
     * @param parentId 父菜单ID
     * @param menuId   菜单ID
     * @return 是否是子菜单
     */
    public boolean isChildMenu(Long parentId, Long menuId) {
        if (parentId == null || menuId == null || parentId.equals(menuId)) {
            return false;
        }
        try {
            // 递归检查 parentId 是否是 menuId 的子菜单
            return checkIsChildRecursive(parentId, menuId);
        } catch (Exception e) {
            log.error("检查是否是子菜单失败，parentId：{}，menuId：{}", parentId, menuId, e);
            return false;
        }
    }

    /**
     * 递归检查是否是子菜单
     *
     * @param parentId 父菜单ID
     * @param menuId   菜单ID
     * @return 是否是子菜单
     */
    private boolean checkIsChildRecursive(Long parentId, Long menuId) {
        if (parentId == null) {
            return false;
        }
        if (parentId.equals(menuId)) {
            return true;
        }
        MenuEntity parentMenu = menuDao.selectById(parentId);
        if (parentMenu == null || parentMenu.getParentId() == null) {
            return false;
        }
        return checkIsChildRecursive(parentMenu.getParentId(), menuId);
    }

    /**
     * 检查菜单是否被角色使用
     *
     * @param menuId 菜单ID
     * @return 是否被角色使用
     */
    public boolean isMenuUsedByRole(Long menuId) {
        if (menuId == null) {
            return false;
        }
        try {
            Integer count = menuDao.checkMenuUsedByRole(menuId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查菜单是否被角色使用失败，menuId：{}", menuId, e);
            return false;
        }
    }
}

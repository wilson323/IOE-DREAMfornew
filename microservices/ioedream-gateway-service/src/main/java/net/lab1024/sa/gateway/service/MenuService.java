package net.lab1024.sa.gateway.service;

import net.lab1024.sa.common.menu.entity.MenuEntity;

import java.util.List;

/**
 * 菜单服务接口
 * <p>
 * 遵循四层架构规范：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
public interface MenuService {

    /**
     * 获取用户菜单树
     *
     * @param userId 用户ID
     * @return 菜单树列表
     */
    List<MenuEntity> getUserMenuTree(Long userId);
}
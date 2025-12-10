package net.lab1024.sa.gateway.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.menu.manager.MenuManager;
import net.lab1024.sa.common.menu.entity.MenuEntity;
import net.lab1024.sa.gateway.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单服务实现
 * <p>
 * 遵循四层架构规范：Controller → Service → Manager → DAO
 * 负责事务管理和业务逻辑编排
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuManager menuManager;

    @Override
    public List<MenuEntity> getUserMenuTree(Long userId) {
        log.info("[菜单服务] 获取用户菜单树, userId={}", userId);
        try {
            List<MenuEntity> menuList = menuManager.getUserMenuTree(userId);
            log.info("[菜单服务] 查询成功，共{}个菜单", menuList.size());
            return menuList;
        } catch (Exception e) {
            log.error("[菜单服务] 查询失败, userId={}", userId, e);
            throw new RuntimeException("获取用户菜单失败", e);
        }
    }
}
package net.lab1024.sa.common.menu.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.menu.entity.MenuEntity;
import net.lab1024.sa.common.menu.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 * <p>
 * 从网关服务迁移至common-service
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-14
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menu")
@Tag(name = "菜单管理", description = "菜单管理相关接口")
public class MenuController {

    @Resource
    private MenuService menuService;

    @Observed(name = "menu.getUserMenuTree", contextualName = "menu-get-user-tree")
    @Operation(summary = "获取用户菜单树")
    @GetMapping("/tree/{userId}")
    public ResponseDTO<List<MenuEntity>> getUserMenuTree(@PathVariable Long userId) {
        log.info("[菜单服务] 获取用户菜单树, userId={}", userId);
        List<MenuEntity> menuTree = menuService.getUserMenuTree(userId);
        return ResponseDTO.ok(menuTree);
    }

    @Observed(name = "menu.getUserMenuList", contextualName = "menu-get-user-list")
    @Operation(summary = "获取用户菜单列表")
    @GetMapping("/list/{userId}")
    public ResponseDTO<List<MenuEntity>> getUserMenuList(@PathVariable Long userId) {
        log.info("[菜单服务] 获取用户菜单列表, userId={}", userId);
        List<MenuEntity> menuList = menuService.getUserMenuList(userId);
        return ResponseDTO.ok(menuList);
    }

}

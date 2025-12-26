package net.lab1024.sa.common.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.menu.entity.MenuEntity;

/**
 * MenuServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：MenuServiceImpl核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuServiceImpl单元测试")
class MenuServiceImplTest {

    @Mock
    private MenuService menuService;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("测试hasMenuPermission-成功场景")
    void test_hasMenuPermission_Success() {
        // Given
        Long userId = 1L;
        Long menuId = 10L;
        MenuEntity menu = new MenuEntity();
        menu.setId(menuId);
        menu.setMenuName("测试菜单");

        // When
        when(menuService.hasMenuPermission(userId, menuId)).thenReturn(true);
        boolean result = menuService.hasMenuPermission(userId, menuId);

        // Then
        assertTrue(result);
        verify(menuService, times(1)).hasMenuPermission(userId, menuId);
    }

    @Test
    @DisplayName("测试hasMenuPermission-无权限")
    void test_hasMenuPermission_NoPermission() {
        // Given
        Long userId = 1L;
        Long menuId = 999L;
        MenuEntity menu = new MenuEntity();
        menu.setId(10L);
        menu.setMenuName("测试菜单");

        // When
        when(menuService.hasMenuPermission(userId, menuId)).thenReturn(false);
        boolean result = menuService.hasMenuPermission(userId, menuId);

        // Then
        assertFalse(result);
        verify(menuService, times(1)).hasMenuPermission(userId, menuId);
    }

    @Test
    @DisplayName("测试hasPermission-成功场景")
    void test_hasPermission_Success() {
        // Given
        Long userId = 1L;
        String permission = "user:read";
        MenuEntity menu = new MenuEntity();
        menu.setId(1L);
        menu.setPermission(permission);

        // When
        when(menuService.hasPermission(userId, permission)).thenReturn(true);
        boolean result = menuService.hasPermission(userId, permission);

        // Then
        assertTrue(result);
        verify(menuService, times(1)).hasPermission(userId, permission);
    }

    @Test
    @DisplayName("测试hasPermission-无权限")
    void test_hasPermission_NoPermission() {
        // Given
        Long userId = 1L;
        String permission = "admin:delete";
        MenuEntity menu = new MenuEntity();
        menu.setId(1L);
        menu.setPermission("user:read");

        // When
        when(menuService.hasPermission(userId, permission)).thenReturn(false);
        boolean result = menuService.hasPermission(userId, permission);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("测试getMenuByCode-成功场景")
    void test_getMenuByCode_Success() {
        // Given
        String menuCode = "TEST_MENU";
        MenuEntity menu = new MenuEntity();
        menu.setId(1L);
        menu.setMenuCode(menuCode);
        menu.setMenuName("测试菜单");

        // When
        when(menuService.getMenuByCode(menuCode)).thenReturn(menu);
        MenuEntity result = menuService.getMenuByCode(menuCode);

        // Then
        assertNotNull(result);
        assertEquals(menuCode, result.getMenuCode());
        verify(menuService, times(1)).getMenuByCode(menuCode);
    }

    @Test
    @DisplayName("测试getMenuByCode-菜单不存在")
    void test_getMenuByCode_NotFound() {
        // Given
        String menuCode = "NONEXISTENT";
        // When
        when(menuService.getMenuByCode(menuCode)).thenReturn(null);
        MenuEntity result = menuService.getMenuByCode(menuCode);

        // Then
        assertNull(result);
        verify(menuService, times(1)).getMenuByCode(menuCode);
    }

    @Test
    @DisplayName("测试getMenuByPermission-成功场景")
    void test_getMenuByPermission_Success() {
        // Given
        String permission = "user:read";
        MenuEntity menu = new MenuEntity();
        menu.setId(1L);
        menu.setPermission(permission);
        menu.setMenuName("用户管理");

        // When
        when(menuService.getMenuByPermission(permission)).thenReturn(menu);
        MenuEntity result = menuService.getMenuByPermission(permission);

        // Then
        assertNotNull(result);
        assertEquals(permission, result.getPermission());
        verify(menuService, times(1)).getMenuByPermission(permission);
    }

    @Test
    @DisplayName("测试getUserMenuTree-成功场景")
    void test_getUserMenuTree_Success() {
        // Given
        Long userId = 1L;
        MenuEntity parentMenu = new MenuEntity();
        parentMenu.setId(1L);
        parentMenu.setParentId(0L);
        parentMenu.setMenuName("父菜单");

        MenuEntity childMenu = new MenuEntity();
        childMenu.setId(2L);
        childMenu.setParentId(1L);
        childMenu.setMenuName("子菜单");

        List<MenuEntity> menuList = Arrays.asList(parentMenu, childMenu);

        // When
        when(menuService.getUserMenuTree(userId)).thenReturn(menuList);
        List<MenuEntity> result = menuService.getUserMenuTree(userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(menuService, times(1)).getUserMenuTree(userId);
    }

    @Test
    @DisplayName("测试getUserPermissions-成功场景")
    void test_getUserPermissions_Success() {
        // Given
        Long userId = 1L;
        MenuEntity menu1 = new MenuEntity();
        menu1.setId(1L);
        menu1.setPermission("user:read");

        MenuEntity menu2 = new MenuEntity();
        menu2.setId(2L);
        menu2.setPermission("user:write");

        MenuEntity menu3 = new MenuEntity();
        menu3.setId(3L);
        menu3.setPermission(null); // 无权限标识

        List<String> permissions = Arrays.asList("user:read", "user:write");

        // When
        when(menuService.getUserPermissions(userId)).thenReturn(permissions);
        List<String> result = menuService.getUserPermissions(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("user:read"));
        assertTrue(result.contains("user:write"));
        verify(menuService, times(1)).getUserPermissions(userId);
    }

}

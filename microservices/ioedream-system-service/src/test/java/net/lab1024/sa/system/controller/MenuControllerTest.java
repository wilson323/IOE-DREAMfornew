package net.lab1024.sa.system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.system.menu.domain.form.MenuAddForm;
import net.lab1024.sa.system.menu.domain.form.MenuUpdateForm;
import net.lab1024.sa.system.menu.domain.vo.MenuVO;
import net.lab1024.sa.system.menu.service.MenuService;

/**
 * MenuController 单元测试
 * 测试菜单权限管理API的完整功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("菜单权限控制器测试")
public class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long mockUserId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuController).build();
        objectMapper = new ObjectMapper();

        // Mock SmartRequestUtil 获取当前用户
        MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class);
        mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);
    }

    // ==================== 基础菜单管理测试 ====================

    @Test
    @DisplayName("创建菜单 - 成功")
    void testCreateMenu_Success() throws Exception {
        MenuAddForm addForm = new MenuAddForm();
        addForm.setMenuType(2); // 2-菜单
        addForm.setMenuName("系统管理");
        addForm.setMenuCode("SYSTEM_MANAGE");
        addForm.setIcon("system");
        addForm.setPath("/system");
        addForm.setParentId(0L);
        addForm.setSortOrder(1);
        addForm.setPermission("system:manage");

        when(menuService.addMenu(any(MenuAddForm.class), eq(mockUserId)))
                .thenReturn(ResponseDTO.ok(1L));

        mockMvc.perform(post("/api/menu/add")
                .header("X-User-Id", mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(1L));

        verify(menuService, times(1)).addMenu(any(MenuAddForm.class), eq(mockUserId));
    }

    @Test
    @DisplayName("更新菜单 - 成功")
    void testUpdateMenu_Success() throws Exception {
        MenuUpdateForm updateForm = new MenuUpdateForm();
        updateForm.setMenuId(1L);
        updateForm.setMenuName("系统管理(已更新)");
        updateForm.setIcon("system-updated");
        updateForm.setSortOrder(2);

        when(menuService.updateMenu(any(MenuUpdateForm.class), eq(mockUserId)))
                .thenReturn(ResponseDTO.ok());

        mockMvc.perform(post("/api/menu/update")
                .header("X-User-Id", mockUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(menuService, times(1)).updateMenu(any(MenuUpdateForm.class), eq(mockUserId));
    }

    @Test
    @DisplayName("删除菜单 - 成功")
    void testDeleteMenu_Success() throws Exception {
        Long menuId = 1L;

        when(menuService.deleteMenu(menuId, mockUserId))
                .thenReturn(ResponseDTO.ok());

        mockMvc.perform(delete("/api/menu/{menuId}", menuId)
                .header("X-User-Id", mockUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(menuService, times(1)).deleteMenu(menuId, mockUserId);
    }

    @Test
    @DisplayName("根据ID查询菜单 - 成功")
    void testGetMenuById_Success() throws Exception {
        Long menuId = 1L;

        MenuVO menuVO = new MenuVO();
        menuVO.setMenuId(menuId);
        menuVO.setMenuType(2); // 2-菜单
        menuVO.setMenuName("系统管理");
        menuVO.setMenuCode("SYSTEM_MANAGE");
        menuVO.setIcon("system");
        menuVO.setPath("/system");
        menuVO.setParentId(0L);
        menuVO.setSortOrder(1);
        menuVO.setPermission("system:manage");
        menuVO.setParentName("根菜单");

        when(menuService.getMenuById(menuId))
                .thenReturn(ResponseDTO.ok(menuVO));

        mockMvc.perform(get("/api/menu/{menuId}", menuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.menuId").value(menuId))
                .andExpect(jsonPath("$.data.menuName").value("系统管理"))
                .andExpect(jsonPath("$.data.parentName").value("根菜单"));

        verify(menuService, times(1)).getMenuById(menuId);
    }

    // ==================== 菜单树结构测试 ====================

    @Test
    @DisplayName("获取菜单树 - 成功")
    void testGetMenuTree_Success() throws Exception {
        List<MenuVO> menuTree = Arrays.asList(
                createMockMenuTreeVO(1L, "系统管理", "SYSTEM_MANAGE", 0L, Arrays.asList(
                        createMockMenuTreeVO(2L, "用户管理", "USER_MANAGE", 1L, Arrays.asList()),
                        createMockMenuTreeVO(3L, "角色管理", "ROLE_MANAGE", 1L, Arrays.asList()))),
                createMockMenuTreeVO(4L, "业务管理", "BUSINESS_MANAGE", 0L, Arrays.asList(
                        createMockMenuTreeVO(5L, "门禁管理", "ACCESS_MANAGE", 4L, Arrays.asList()))));

        when(menuService.getMenuTree(true))
                .thenReturn(ResponseDTO.ok(menuTree));

        mockMvc.perform(get("/api/menu/tree")
                .param("onlyEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].menuName").value("系统管理"))
                .andExpect(jsonPath("$.data[0].children").isArray())
                .andExpect(jsonPath("$.data[0].children.length()").value(2));

        verify(menuService, times(1)).getMenuTree(true);
    }

    // ==================== 用户角色菜单测试 ====================

    @Test
    @DisplayName("获取用户菜单 - 成功")
    void testGetUserMenu_Success() throws Exception {
        List<Map<String, Object>> userMenus = Arrays.asList(
                Map.of("menuId", 1L, "menuName", "系统管理"),
                Map.of("menuId", 2L, "menuName", "用户管理"));

        when(menuService.getUserMenu(mockUserId))
                .thenReturn(ResponseDTO.ok(userMenus));

        mockMvc.perform(get("/api/menu/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(menuService, times(1)).getUserMenu(mockUserId);
    }

    // ==================== 菜单权限测试 ====================

    @Test
    @DisplayName("获取菜单权限标识 - 成功")
    void testGetMenuPermissions_Success() throws Exception {
        List<String> permissions = Arrays.asList(
                "system:manage",
                "user:add",
                "user:edit",
                "user:delete",
                "role:manage");

        when(menuService.getMenuPermissions())
                .thenReturn(ResponseDTO.ok(permissions));

        mockMvc.perform(get("/api/menu/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0]").value("system:manage"));

        verify(menuService, times(1)).getMenuPermissions();
    }

    @Test
    @DisplayName("移动菜单 - 成功")
    void testMoveMenu_Success() throws Exception {
        Long menuId = 3L;
        Long newParentId = 4L;

        when(menuService.moveMenu(menuId, newParentId, mockUserId))
                .thenReturn(ResponseDTO.ok());

        mockMvc.perform(post("/api/menu/move/{menuId}", menuId)
                .param("newParentId", newParentId.toString())
                .header("X-User-Id", mockUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(menuService, times(1)).moveMenu(menuId, newParentId, mockUserId);
    }

    // ==================== 菜单统计测试 ====================

    @Test
    @DisplayName("获取菜单统计 - 成功")
    void testGetMenuStatistics_Success() throws Exception {
        Map<String, Object> statistics = Map.of(
                "totalCount", 25,
                "enabledCount", 20,
                "disabledCount", 5,
                "menuCount", 12,
                "buttonCount", 13,
                "directoryCount", 3);

        when(menuService.getMenuStatistics())
                .thenReturn(ResponseDTO.ok(statistics));

        mockMvc.perform(get("/api/menu/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(25))
                .andExpect(jsonPath("$.data.enabledCount").value(20))
                .andExpect(jsonPath("$.data.menuCount").value(12));

        verify(menuService, times(1)).getMenuStatistics();
    }

    // ==================== 辅助方法 ====================

    private MenuVO createMockMenuVO(Long menuId, String menuName, String menuCode, Long parentId,
            Integer roleCount, Integer userCount) {
        MenuVO menuVO = new MenuVO();
        menuVO.setMenuId(menuId);
        menuVO.setMenuType(2); // 2-菜单
        menuVO.setMenuName(menuName);
        menuVO.setMenuCode(menuCode);
        menuVO.setIcon(menuName.toLowerCase());
        menuVO.setPath("/" + menuName.toLowerCase());
        menuVO.setParentId(parentId);
        menuVO.setSortOrder(1);
        menuVO.setPermission(menuCode.toLowerCase());
        menuVO.setStatus(1);
        menuVO.setCreateTime(LocalDateTime.now());
        return menuVO;
    }

    private MenuVO createMockMenuTreeVO(Long menuId, String menuName, String menuCode, Long parentId,
            List<MenuVO> children) {
        MenuVO menuVO = createMockMenuVO(menuId, menuName, menuCode, parentId, 0, 0);
        menuVO.setChildren(children);
        return menuVO;
    }
}

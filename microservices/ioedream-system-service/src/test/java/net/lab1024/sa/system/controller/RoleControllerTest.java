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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.system.role.domain.vo.RoleVO;
import net.lab1024.sa.system.role.service.RoleService;

/**
 * RoleController 单元测试
 * <p>
 * 测试角色权限管理API的完整功能
 * 严格遵循项目测试规范：
 * - 使用Mockito进行依赖注入和Mock
 * - 使用MockMvc进行HTTP请求模拟
 * - 完整的测试覆盖和断言验证
 * - 符合Google风格的测试代码规范
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("角色权限控制器测试")
public class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long mockUserId = 1L;

    /**
     * 测试初始化方法
     * 在每个测试方法执行前初始化MockMvc和ObjectMapper
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== 基础角色管理测试 ====================

    @Test
    @DisplayName("分页查询角色 - 成功")
    @SuppressWarnings("unchecked")
    void testQueryRolePage_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> queryForm = new HashMap<>();
        queryForm.put("roleName", "系统");
        queryForm.put("status", 1);
        queryForm.put("pageNum", 1);
        queryForm.put("pageSize", 10);

        PageResult<RoleVO> pageResult = new PageResult<>();
        pageResult.setTotal(2L);
        pageResult.setList(Arrays.asList(
                createMockRoleVO(1L, "系统管理员", "SYSTEM_ADMIN"),
                createMockRoleVO(2L, "系统操作员", "SYSTEM_OPERATOR")));

        // Mock服务层方法
        when(roleService.queryRolePage(any(Map.class)))
                .thenReturn(ResponseDTO.ok(pageResult));

        // 执行请求并验证
        mockMvc.perform(post("/api/role/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(2L))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(2));

        // 验证服务层方法调用
        verify(roleService, times(1)).queryRolePage(any(Map.class));
    }

    @Test
    @DisplayName("查询角色列表 - 成功")
    @SuppressWarnings("unchecked")
    void testQueryRoleList_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> queryForm = new HashMap<>();
        queryForm.put("status", 1);

        List<RoleVO> roleList = Arrays.asList(
                createMockRoleVO(1L, "系统管理员", "SYSTEM_ADMIN"),
                createMockRoleVO(2L, "普通用户", "NORMAL_USER"),
                createMockRoleVO(3L, "访客", "GUEST"));

        // Mock服务层方法
        when(roleService.queryRoleList(any(Map.class)))
                .thenReturn(ResponseDTO.ok(roleList));

        // 执行请求并验证
        mockMvc.perform(post("/api/role/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].roleName").value("系统管理员"));

        // 验证服务层方法调用
        verify(roleService, times(1)).queryRoleList(any(Map.class));
    }

    @Test
    @DisplayName("获取角色详情 - 成功")
    void testGetRoleById_Success() throws Exception {
        // 准备测试数据
        Long roleId = 1L;
        RoleVO roleVO = createMockRoleVO(roleId, "系统管理员", "SYSTEM_ADMIN");

        // Mock服务层方法
        when(roleService.getRoleById(roleId))
                .thenReturn(ResponseDTO.ok(roleVO));

        // 执行请求并验证
        mockMvc.perform(get("/api/role/{roleId}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.roleId").value(roleId))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.roleCode").value("SYSTEM_ADMIN"));

        // 验证服务层方法调用
        verify(roleService, times(1)).getRoleById(roleId);
    }

    @Test
    @DisplayName("新增角色 - 成功")
    @SuppressWarnings("unchecked")
    void testAddRole_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Map<String, Object> roleForm = new HashMap<>();
            roleForm.put("roleName", "系统管理员");
            roleForm.put("roleCode", "SYSTEM_ADMIN");
            roleForm.put("roleType", "SYSTEM");
            roleForm.put("description", "系统管理员角色");
            roleForm.put("status", 1);

            // Mock服务层方法
            when(roleService.addRole(any(Map.class), eq(mockUserId)))
                    .thenReturn(ResponseDTO.ok(1L));

            // 执行请求并验证
            mockMvc.perform(post("/api/role/add")
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(roleForm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data").value(1L));

            // 验证服务层方法调用
            verify(roleService, times(1)).addRole(any(Map.class), eq(mockUserId));
        }
    }

    @Test
    @DisplayName("更新角色 - 成功")
    @SuppressWarnings("unchecked")
    void testUpdateRole_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Map<String, Object> roleForm = new HashMap<>();
            roleForm.put("roleId", 1L);
            roleForm.put("roleName", "超级管理员");
            roleForm.put("roleCode", "SYSTEM_ADMIN");
            roleForm.put("roleType", "SYSTEM");
            roleForm.put("description", "超级管理员角色");
            roleForm.put("status", 1);

            // Mock服务层方法
            when(roleService.updateRole(any(Map.class), eq(mockUserId)))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(post("/api/role/update")
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(roleForm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(roleService, times(1)).updateRole(any(Map.class), eq(mockUserId));
        }
    }

    @Test
    @DisplayName("删除角色 - 成功")
    void testDeleteRole_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Long roleId = 1L;

            // Mock服务层方法
            when(roleService.deleteRole(roleId, mockUserId))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(delete("/api/role/{roleId}", roleId)
                    .header("X-User-Id", mockUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(roleService, times(1)).deleteRole(roleId, mockUserId);
        }
    }

    // ==================== 角色权限关联测试 ====================

    @Test
    @DisplayName("分配权限给角色 - 成功")
    void testAssignPermissions_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Long roleId = 1L;
            List<Long> permissionIds = Arrays.asList(1L, 2L, 3L);

            // Mock服务层方法
            when(roleService.assignPermissions(roleId, permissionIds, mockUserId))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(post("/api/role/{roleId}/permissions", roleId)
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(permissionIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(roleService, times(1)).assignPermissions(roleId, permissionIds, mockUserId);
        }
    }

    @Test
    @DisplayName("获取角色权限列表 - 成功")
    void testGetRolePermissions_Success() throws Exception {
        // 准备测试数据
        Long roleId = 1L;
        List<Long> permissionIds = Arrays.asList(1L, 2L, 3L, 4L);

        // Mock服务层方法
        when(roleService.getRolePermissions(roleId))
                .thenReturn(ResponseDTO.ok(permissionIds));

        // 执行请求并验证
        mockMvc.perform(get("/api/role/{roleId}/permissions", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0]").value(1L));

        // 验证服务层方法调用
        verify(roleService, times(1)).getRolePermissions(roleId);
    }

    // ==================== 角色用户关联测试 ====================

    @Test
    @DisplayName("分配用户到角色 - 成功")
    void testAssignUsers_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Long roleId = 1L;
            List<Long> userIds = Arrays.asList(1L, 2L, 3L);

            // Mock服务层方法
            when(roleService.assignUsers(roleId, userIds, mockUserId))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(post("/api/role/{roleId}/users", roleId)
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(roleService, times(1)).assignUsers(roleId, userIds, mockUserId);
        }
    }

    @Test
    @DisplayName("获取角色用户列表 - 成功")
    void testGetRoleUsers_Success() throws Exception {
        // 准备测试数据
        Long roleId = 1L;
        List<Map<String, Object>> userList = new ArrayList<>();
        userList.add(createMockUserMap(1L, "admin", "管理员"));
        userList.add(createMockUserMap(2L, "operator", "操作员"));

        // Mock服务层方法
        when(roleService.getRoleUsers(roleId))
                .thenReturn(ResponseDTO.ok(userList));

        // 执行请求并验证
        mockMvc.perform(get("/api/role/{roleId}/users", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].userName").value("admin"));

        // 验证服务层方法调用
        verify(roleService, times(1)).getRoleUsers(roleId);
    }

    // ==================== 角色缓存管理测试 ====================

    @Test
    @DisplayName("刷新角色缓存 - 成功")
    void testRefreshRoleCache_Success() throws Exception {
        // Mock服务层方法
        when(roleService.refreshRoleCache(null))
                .thenReturn(ResponseDTO.<Void>ok());

        // 执行请求并验证
        mockMvc.perform(post("/api/role/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 验证服务层方法调用
        verify(roleService, times(1)).refreshRoleCache(null);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建Mock角色VO对象
     *
     * @param roleId   角色ID
     * @param roleName 角色名称
     * @param roleCode 角色编码
     * @return RoleVO对象
     */
    private RoleVO createMockRoleVO(Long roleId, String roleName, String roleCode) {
        RoleVO roleVO = new RoleVO();
        roleVO.setRoleId(roleId);
        roleVO.setRoleName(roleName);
        roleVO.setRoleCode(roleCode);
        roleVO.setRoleType("SYSTEM");
        roleVO.setDescription(roleName + "角色");
        roleVO.setStatus(1);
        roleVO.setUserCount(0);
        roleVO.setPermissionCount(0);
        roleVO.setCreateTime(LocalDateTime.now());
        return roleVO;
    }

    /**
     * 创建Mock用户Map对象
     *
     * @param userId   用户ID
     * @param userName 用户名
     * @param realName 真实姓名
     * @return Map对象
     */
    private Map<String, Object> createMockUserMap(Long userId, String userName, String realName) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("userName", userName);
        userMap.put("realName", realName);
        userMap.put("status", 1);
        return userMap;
    }
}

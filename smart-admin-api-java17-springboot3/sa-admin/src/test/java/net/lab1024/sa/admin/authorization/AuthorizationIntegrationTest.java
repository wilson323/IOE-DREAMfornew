package net.lab1024.sa.admin.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.admin.module.smart.access.controller.AccessDeviceController;
import net.lab1024.sa.base.authorization.annotation.RequireResource;
import net.lab1024.sa.base.authorization.util.AuthorizationContextHolder;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 权限系统集成测试
 * <p>
 * 测试RAC权限中间层的完整集成，包括：
 * - 权限注解拦截器
 * - 数据域过滤
 * - 前后端权限一致性
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("权限系统集成测试")
class AuthorizationIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AccessDeviceController accessDeviceController;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("权限注解功能测试")
    void testPermissionAnnotation() throws Exception {
        // Given: 构建设备查询请求
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);

        // When: 访问需要权限的接口
        MvcResult result = mockMvc.perform(post("/api/smart/access/device/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageParam)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: 应该成功响应（测试用户应该有权限）
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent, "响应内容不能为空");
        assertTrue(responseContent.contains("\"code\":200"), "应该返回成功状态码");
    }

    @Test
    @DisplayName("权限注解元数据验证")
    void testPermissionAnnotationMetadata() throws Exception {
        // Given: 获取Controller方法
        var method = AccessDeviceController.class
                .getMethod("getDevicePage", PageParam.class, Long.class, Long.class,
                          String.class, Integer.class, Integer.class, Integer.class);

        // When: 检查权限注解
        RequireResource annotation = method.getAnnotation(RequireResource.class);

        // Then: 权限注解应该存在且配置正确
        assertNotNull(annotation, "权限注解不能为空");
        assertEquals("ACCESS_DEVICE_VIEW", annotation.code(), "资源码应该正确");
        assertEquals("AREA", annotation.scope(), "数据域应该正确");
        assertEquals("READ", annotation.action(), "操作类型应该正确");
        assertTrue(annotation.description().contains("查询门禁设备"), "描述应该包含相关信息");
    }

    @Test
    @DisplayName("无权限访问测试")
    void testAccessWithoutPermission() throws Exception {
        // Given: 构建需要管理员权限的请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("pageNum", 1);
        requestBody.put("pageSize", 10);

        // When: 访问需要特殊权限的控制接口
        mockMvc.perform(post("/api/smart/access/device/control/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden()); // 应该返回403 Forbidden
    }

    @Test
    @DisplayName("权限上下文获取测试")
    void testAuthorizationContextRetrieval() throws Exception {
        // When: 通过正常流程访问接口
        mockMvc.perform(get("/api/smart/access/device/1"))
                .andExpect(status().isOk());

        // Then: 权限上下文应该可以正确获取
        // 注意：在实际应用中，权限上下文会在拦截器中设置
        // 这里主要测试上下文获取工具方法的存在性和基本功能
        assertDoesNotThrow(() -> {
            AuthorizationContextHolder.getCurrentContext();
        }, "获取当前权限上下文不应该抛出异常");
    }

    @Test
    @DisplayName("数据域过滤功能测试")
    void testDataScopeFiltering() throws Exception {
        // Given: 构建查询请求
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(20);

        // When: 访问应用数据域过滤的接口
        MvcResult result = mockMvc.perform(post("/api/smart/access/device/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageParam)))
                .andExpect(status().isOk())
                .andReturn();

        // Then: 响应该包含数据域过滤的结果
        String responseContent = result.getResponse().getContentAsString();
        ResponseDTO<?> response = objectMapper.readValue(responseContent, ResponseDTO.class);

        assertEquals(200, response.getCode(), "响应状态码应该是200");
        assertNotNull(response.getData(), "响应数据不能为空");
    }

    @Test
    @DisplayName("权限异常处理测试")
    void testPermissionExceptionHandling() throws Exception {
        // Given: 模拟权限检查失败的情况
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("pageNum", -1); // 无效的页码
        invalidRequest.put("pageSize", 1000); // 过大的页面大小

        // When & Then: 应该正确处理权限和参数验证异常
        mockMvc.perform(post("/api/smart/access/device/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // 应该返回400 Bad Request
    }

    @Test
    @DisplayName("权限拦截器注册测试")
    void testPermissionInterceptorRegistration() {
        // Given: 获取Controller实例
        AccessDeviceController controller = accessDeviceController;

        // Then: Controller应该正确注入且不为空
        assertNotNull(controller, "权限Controller应该正确注入");

        // 验证Controller方法上有权限注解
        var methods = AccessDeviceController.class.getDeclaredMethods();
        boolean hasPermissionAnnotation = false;

        for (var method : methods) {
            if (method.isAnnotationPresent(RequireResource.class)) {
                hasPermissionAnnotation = true;
                break;
            }
        }

        assertTrue(hasPermissionAnnotation, "Controller方法应该有权限注解");
    }

    @Test
    @DisplayName("权限资源码一致性测试")
    void testPermissionResourceCodeConsistency() throws Exception {
        // Given: 获取Controller中所有带有权限注解的方法
        var methods = AccessDeviceController.class.getDeclaredMethods();
        Map<String, String> resourceCodes = new HashMap<>();

        // When: 收集所有资源码
        for (var method : methods) {
            RequireResource annotation = method.getAnnotation(RequireResource.class);
            if (annotation != null) {
                resourceCodes.put(method.getName(), annotation.code());
            }
        }

        // Then: 资源码应该符合命名规范
        for (Map.Entry<String, String> entry : resourceCodes.entrySet()) {
            String methodName = entry.getKey();
            String resourceCode = entry.getValue();

            assertNotNull(resourceCode, "方法 " + methodName + " 的资源码不能为空");
            assertFalse(resourceCode.trim().isEmpty(), "方法 " + methodName + " 的资源码不能为空字符串");

            // 验证资源码格式：应该是大写字母和下划线组成
            assertTrue(resourceCode.matches("^[A-Z][A-Z0-9_]*$"),
                      "方法 " + methodName + " 的资源码格式不正确: " + resourceCode);
        }

        // 验证至少有一些方法有权限注解
        assertFalse(resourceCodes.isEmpty(), "应该有方法使用权限注解");
    }

    @Test
    @DisplayName("权限数据域参数验证测试")
    void testPermissionDataScopeParameters() throws Exception {
        // Given: 获取Controller中所有带有权限注解的方法
        var methods = AccessDeviceController.class.getDeclaredMethods();

        // When: 检查权限注解的数据域参数
        for (var method : methods) {
            RequireResource annotation = method.getAnnotation(RequireResource.class);
            if (annotation != null) {
                String scope = annotation.scope();
                String action = annotation.action();

                // Then: 数据域参数应该符合规范
                assertNotNull(scope, "方法 " + method.getName() + " 的数据域不能为空");
                assertNotNull(action, "方法 " + method.getName() + " 的操作类型不能为空");

                // 验证数据域类型
                assertTrue(scope.matches("^(AREA|DEPT|SELF|CUSTOM)$"),
                          "方法 " + method.getName() + " 的数据域类型不正确: " + scope);

                // 验证操作类型
                assertTrue(action.matches("^(READ|WRITE|DELETE|MANAGE|CONFIG|EXPORT)$"),
                          "方法 " + method.getName() + " 的操作类型不正确: " + action);
            }
        }
    }

    @Test
    @DisplayName("权限测试覆盖率统计")
    void testPermissionCoverageStatistics() {
        // Given: 获取Controller中所有公共方法
        var methods = AccessDeviceController.class.getDeclaredMethods();
        int totalMethods = 0;
        int methodsWithPermission = 0;
        int methodsWithDataScope = 0;
        int methodsWithAction = 0;

        // When: 统计权限注解覆盖率
        for (var method : methods) {
            if (java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                totalMethods++;
                RequireResource annotation = method.getAnnotation(RequireResource.class);
                if (annotation != null) {
                    methodsWithPermission++;
                    if (annotation.scope() != null && !annotation.scope().isEmpty()) {
                        methodsWithDataScope++;
                    }
                    if (annotation.action() != null && !annotation.action().isEmpty()) {
                        methodsWithAction++;
                    }
                }
            }
        }

        // Then: 权限覆盖率应该达到要求
        if (totalMethods > 0) {
            double permissionCoverage = (double) methodsWithPermission / totalMethods * 100;
            double dataScopeCoverage = (double) methodsWithDataScope / totalMethods * 100;
            double actionCoverage = (double) methodsWithAction / totalMethods * 100;

            System.out.println("权限注解覆盖率: " + permissionCoverage + "%");
            System.out.println("数据域配置覆盖率: " + dataScopeCoverage + "%");
            System.out.println("操作类型配置覆盖率: " + actionCoverage + "%");

            // 至少80%的方法应该有权限注解
            assertTrue(permissionCoverage >= 80.0,
                      "权限注解覆盖率应该≥80%，实际: " + permissionCoverage + "%");
        }

        assertTrue(totalMethods > 0, "应该有公共方法进行测试");
    }
}
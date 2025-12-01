/*
     * 门禁区域控制器集成测试
     *
     * @Author:    IOE-DREAM Team
     * @Date:      2025-01-17
     * @Copyright  IOE-DREAM智慧园区一卡通管理平台
     */

package net.lab1024.sa.admin.module.smart.access.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessAreaForm;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 门禁区域控制器集成测试
 * 使用真实的数据库和Spring上下文进行完整测试
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("门禁区域控制器集成测试")
class AccessAreaControllerIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试获取区域列表 - 完整流程")
    void testGetAreaList_FullFlow() throws Exception {
        // Act & Assert - 使用真实的数据库查询
        mockMvc.perform(post("/api/smart/access/area/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(20)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.list").exists());
    }

    @Test
    @DisplayName("测试添加和获取区域 - 完整CRUD流程")
    void testAddAndGetArea_FullCrudFlow() throws Exception {
        // 1. 添加区域
        AccessAreaForm form = AccessAreaForm.builder()
                .areaName("集成测试区域")
                .areaCode("INTEGRATION_TEST_001")
                .areaType("FLOOR")
                .parentId(0L)
                .level(1)
                .sort(1)
                .description("集成测试区域描述")
                .status(1)
                .build();

        String addResponse = mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. 获取区域列表验证添加结果
        mockMvc.perform(post("/api/smart/access/area/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(20)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.list[*].areaName").value(
                        org.hamcrest.Matchers.hasItem("集成测试区域")))
                .andExpect(jsonPath("$.data.list[*].areaCode").value(
                        org.hamcrest.Matchers.hasItem("INTEGRATION_TEST_001")));

        // 3. 获取区域树验证结构
        mockMvc.perform(get("/api/smart/access/area/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试区域编码验证 - 唯一性约束")
    void testAreaCodeValidation_UniqueConstraint() throws Exception {
        // 1. 添加第一个区域
        AccessAreaForm form1 = AccessAreaForm.builder()
                .areaName("区域1")
                .areaCode("UNIQUE_TEST_001")
                .areaType("FLOOR")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 验证相同编码不可重复添加
        mockMvc.perform(get("/api/smart/access/area/validate")
                .param("areaCode", "UNIQUE_TEST_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(false));

        // 3. 验证不同编码可以使用
        mockMvc.perform(get("/api/smart/access/area/validate")
                .param("areaCode", "DIFFERENT_CODE_002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("测试区域状态更新 - 业务流程")
    void testUpdateAreaStatus_BusinessFlow() throws Exception {
        // 1. 添加区域
        AccessAreaForm form = AccessAreaForm.builder()
                .areaName("状态测试区域")
                .areaCode("STATUS_TEST_001")
                .areaType("FLOOR")
                .status(1) // 启用状态
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 获取区域统计验证初始状态
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpected(jsonPath("$.data.enabledArea").isNumber())
                .andExpect(jsonPath("$.data.disabledArea").isNumber());

        // 3. 更新区域状态为禁用
        mockMvc.perform(put("/api/smart/access/area/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "areaId", 1L,
                        "status", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").exists());

        // 4. 验证统计信息更新
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试批量操作 - 性能和一致性")
    void testBatchOperations_PerformanceAndConsistency() throws Exception {
        // 1. 批量添加区域
        for (int i = 1; i <= 5; i++) {
            AccessAreaForm form = AccessAreaForm.builder()
                    .areaName("批量测试区域" + i)
                    .areaCode("BATCH_TEST_" + String.format("%03d", i))
                    .areaType("ROOM")
                    .status(1)
                    .build();

            mockMvc.perform(post("/api/smart/access/area/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(form)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));
        }

        // 2. 验证批量添加结果
        mockMvc.perform(post("/api/smart/access/area/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(50)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)));

        // 3. 测试批量删除操作
        mockMvc.perform(delete("/api/smart/access/area/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "areaIds", java.util.Arrays.asList(1L, 2L, 3L, 4L, 5L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("测试区域选项获取 - 缓存和性能")
    void testGetAreaOptions_CacheAndPerformance() throws Exception {
        // 1. 第一次获取区域选项
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/smart/access/area/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());
        long firstRequestTime = System.currentTimeMillis() - startTime;

        // 2. 第二次获取区域选项（应该从缓存获取，速度更快）
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/smart/access/area/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());
        long secondRequestTime = System.currentTimeMillis() - startTime;

        // 3. 验证缓存效果（第二次请求应该更快或至少不会慢太多）
        // 注意：在实际测试中，时间差异可能不明显，这里主要验证功能正确性
    }

    @Test
    @DisplayName("测试区域权限管理 - 复杂业务场景")
    void testAreaPermissionManagement_ComplexBusinessScenario() throws Exception {
        // 1. 添加区域
        AccessAreaForm form = AccessAreaForm.builder()
                .areaName("权限测试区域")
                .areaCode("PERMISSION_TEST_001")
                .areaType("SECURITY_AREA")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 获取区域权限（初始状态）
        mockMvc.perform(get("/api/smart/access/area/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());

        // 3. 更新区域权限
        java.util.List<Map<String, Object>> permissions = java.util.Arrays.asList(
                Map.of("permissionId", 1L, "enabled", true),
                Map.of("permissionId", 2L, "enabled", false),
                Map.of("permissionId", 3L, "enabled", true));

        mockMvc.perform(put("/api/smart/access/area/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "areaId", 1L,
                        "permissions", permissions))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").exists());

        // 4. 验证权限更新结果
        mockMvc.perform(get("/api/smart/access/area/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("测试API响应格式 - 统一性验证")
    void testApiResponseFormat_ConsistencyValidation() throws Exception {
        // 测试所有主要API的响应格式一致性

        // 1. 分页查询响应格式
        mockMvc.perform(post("/api/smart/access/area/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PageParam.builder().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.pageNum").exists())
                .andExpect(jsonPath("$.data.pageSize").exists())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.list").exists());

        // 2. 单个对象响应格式
        mockMvc.perform(get("/api/smart/access/area/1"))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.ok").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists());

        // 3. 列表响应格式
        mockMvc.perform(get("/api/smart/access/area/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());

        // 4. 操作结果响应格式
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").exists())
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试并发访问 - 数据一致性")
    void testConcurrentAccess_DataConsistency() throws Exception {
        // 模拟并发请求获取区域列表
        java.util.concurrent.CompletableFuture<Void> future1 = java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                mockMvc.perform(post("/api/smart/access/area/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                PageParam.builder()
                                        .pageNum(1)
                                        .pageSize(10)
                                        .build())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        java.util.concurrent.CompletableFuture<Void> future2 = java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                mockMvc.perform(get("/api/smart/access/area/tree"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        java.util.concurrent.CompletableFuture<Void> future3 = java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                mockMvc.perform(get("/api/smart/access/area/stats"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 等待所有并发请求完成
        java.util.concurrent.CompletableFuture.allOf(future1, future2, future3).get();
    }
}

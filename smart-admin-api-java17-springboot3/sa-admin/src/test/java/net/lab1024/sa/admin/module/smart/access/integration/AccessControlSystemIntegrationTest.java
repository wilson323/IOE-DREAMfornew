/*
     * 门禁系统端到端集成测试套件
     *
     * @Author:    IOE-DREAM Team
     * @Date:      2025-01-17
     * @Copyright  IOE-DREAM智慧园区一卡通管理平台
     */

package net.lab1024.sa.admin.module.smart.access.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessDeviceForm;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 门禁系统端到端集成测试套件
 * 测试完整的业务流程和数据一致性
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("门禁系统端到端集成测试")
class AccessControlSystemIntegrationTest {

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
    @Order(1)
    @DisplayName("测试完整的门禁区域管理流程")
    void testCompleteAreaManagementFlow() throws Exception {
        // 1. 创建根区域
        AccessAreaForm rootArea = AccessAreaForm.builder()
                .areaName("智慧园区主楼")
                .areaCode("SMART_CAMPUS_MAIN")
                .areaType("BUILDING")
                .parentId(0L)
                .level(1)
                .sort(1)
                .description("智慧园区主建筑")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rootArea)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 创建子区域楼层
        AccessAreaForm floor1 = AccessAreaForm.builder()
                .areaName("一楼")
                .areaCode("FIRST_FLOOR")
                .areaType("FLOOR")
                .parentId(1L)
                .level(2)
                .sort(1)
                .description("一楼办公区")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(floor1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 3. 创建房间区域
        AccessAreaForm room = AccessAreaForm.builder()
                .areaName("101会议室")
                .areaCode("ROOM_101")
                .areaType("ROOM")
                .parentId(2L)
                .level(3)
                .sort(1)
                .description("一楼101会议室")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 4. 验证区域树结构
        mockMvc.perform(get("/api/smart/access/area/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());

        // 5. 验证区域统计
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.totalArea").value(3));
    }

    @Test
    @Order(2)
    @DisplayName("测试门禁设备与区域关联流程")
    void testDeviceAreaAssociationFlow() throws Exception {
        // 1. 创建设备
        AccessDeviceForm device = AccessDeviceForm.builder()
                .deviceName("主楼入口门禁")
                .deviceCode("MAIN_ENTRANCE_001")
                .deviceType("ACCESS_CONTROL")
                .areaId(1L) // 关联到主楼
                .manufacturer("ZKTeco")
                .model("ProID200")
                .serialNumber("SN20250117001")
                .ipAddress("192.168.1.100")
                .port(8080)
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/device/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 验证区域下的设备列表
        mockMvc.perform(get("/api/smart/access/area/1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray());

        // 3. 验证设备详情
        mockMvc.perform(get("/api/smart/access/device/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.deviceName").value("主楼入口门禁"))
                .andExpect(jsonPath("$.data.areaId").value(1));
    }

    @Test
    @Order(3)
    @DisplayName("测试权限分配和管理流程")
    void testPermissionManagementFlow() throws Exception {
        // 1. 为区域分配权限
        List<Map<String, Object>> permissions = Arrays.asList(
                Map.of("permissionId", 1L, "permissionName", "进入权限", "enabled", true),
                Map.of("permissionId", 2L, "permissionName", "离开权限", "enabled", true),
                Map.of("permissionId", 3L, "permissionName", "访客权限", "enabled", false));

        mockMvc.perform(put("/api/smart/access/area/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "areaId", 1L,
                        "permissions", permissions))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 2. 验证权限分配结果
        mockMvc.perform(get("/api/smart/access/area/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        // 3. 更新部分权限状态
        List<Map<String, Object>> updatedPermissions = Arrays.asList(
                Map.of("permissionId", 1L, "enabled", true),
                Map.of("permissionId", 2L, "enabled", false), // 禁用离开权限
                Map.of("permissionId", 3L, "enabled", true) // 启用访客权限
        );

        mockMvc.perform(put("/api/smart/access/area/1/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "areaId", 1L,
                        "permissions", updatedPermissions))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("测试数据一致性和完整性")
    void testDataConsistencyAndIntegrity() throws Exception {
        // 1. 检查区域列表查询的一致性
        mockMvc.perform(post("/api/smart/access/area/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(20)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(3)) // 之前创建的3个区域
                .andExpect(jsonPath("$.data.list.length()").value(3));

        // 2. 检查设备列表查询的一致性
        mockMvc.perform(post("/api/smart/access/device/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(20)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(1)) // 之前创建的1个设备
                .andExpect(jsonPath("$.data.list.length()").value(1));

        // 3. 验证关联关系的完整性
        mockMvc.perform(get("/api/smart/access/area/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @Order(5)
    @DisplayName("测试并发操作和数据隔离")
    void testConcurrentOperationsAndDataIsolation() throws Exception {
        // 模拟多个用户同时操作不同区域
        java.util.concurrent.CompletableFuture<Void> operation1 = java.util.concurrent.CompletableFuture
                .runAsync(() -> {
                    try {
                        AccessAreaForm area = AccessAreaForm.builder()
                                .areaName("并发测试区域1")
                                .areaCode("CONCURRENT_AREA_1")
                                .areaType("ROOM")
                                .status(1)
                                .build();

                        mockMvc.perform(post("/api/smart/access/area/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(area)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ok").value(true));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        java.util.concurrent.CompletableFuture<Void> operation2 = java.util.concurrent.CompletableFuture
                .runAsync(() -> {
                    try {
                        AccessAreaForm area = AccessAreaForm.builder()
                                .areaName("并发测试区域2")
                                .areaCode("CONCURRENT_AREA_2")
                                .areaType("ROOM")
                                .status(1)
                                .build();

                        mockMvc.perform(post("/api/smart/access/area/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(area)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ok").value(true));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        java.util.concurrent.CompletableFuture<Void> operation3 = java.util.concurrent.CompletableFuture
                .runAsync(() -> {
                    try {
                        mockMvc.perform(get("/api/smart/access/area/stats"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ok").value(true))
                                .andExpect(jsonPath("$.data.totalArea").exists());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // 等待所有操作完成
        java.util.concurrent.CompletableFuture.allOf(operation1, operation2, operation3).get();

        // 验证所有操作都成功完成
        mockMvc.perform(post("/api/smart/access/area/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        PageParam.builder()
                                .pageNum(1)
                                .pageSize(50)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(5)); // 3个原有 + 2个并发新增
    }

    @Test
    @Order(6)
    @DisplayName("测试系统性能和响应时间")
    void testSystemPerformanceAndResponseTime() throws Exception {
        // 1. 测试区域查询性能
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/smart/access/area/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long queryTime = System.currentTimeMillis() - startTime;

        // 2. 测试统计查询性能
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long statsTime = System.currentTimeMillis() - startTime;

        // 3. 测试选项查询性能
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/smart/access/area/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        long optionsTime = System.currentTimeMillis() - startTime;

        // 验证性能要求（根据实际需求调整阈值）
        assertTrue("区域树查询时间过长: " + queryTime + "ms", queryTime < 5000);
        assertTrue("统计查询时间过长: " + statsTime + "ms", statsTime < 3000);
        assertTrue("选项查询时间过长: " + optionsTime + "ms", optionsTime < 2000);
    }

    @Test
    @Order(7)
    @DisplayName("测试错误处理和异常恢复")
    void testErrorHandlingAndExceptionRecovery() throws Exception {
        // 1. 测试无效ID的错误处理
        mockMvc.perform(get("/api/smart/access/area/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());

        // 2. 测试重复编码的错误处理
        AccessAreaForm duplicateForm = AccessAreaForm.builder()
                .areaName("重复编码区域")
                .areaCode("SMART_CAMPUS_MAIN") // 使用已存在的编码
                .areaType("ROOM")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());

        // 3. 测试无效参数的错误处理
        AccessAreaForm invalidForm = AccessAreaForm.builder()
                .areaName("") // 空名称
                .areaCode("INVALID_AREA")
                .areaType("ROOM")
                .status(1)
                .build();

        mockMvc.perform(post("/api/smart/access/area/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @Order(8)
    @DisplayName("测试系统完整性和集成验证")
    void testSystemIntegrityAndIntegrationValidation() throws Exception {
        // 1. 验证所有API端点都正常响应
        String[] endpoints = {
                "/api/smart/access/area/query",
                "/api/smart/access/area/tree",
                "/api/smart/access/area/stats",
                "/api/smart/access/area/options",
                "/api/smart/access/device/query"
        };

        for (String endpoint : endpoints) {
            if (endpoint.contains("query")) {
                mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PageParam.builder().build())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            } else {
                mockMvc.perform(get(endpoint))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.ok").value(true));
            }
        }

        // 2. 验证数据完整性约束
        mockMvc.perform(get("/api/smart/access/area/validate")
                .param("areaCode", "NONEXISTENT_CODE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(true));

        // 3. 验证系统一致性
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.totalArea").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.enabledArea").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.disabledArea").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)));
    }
}

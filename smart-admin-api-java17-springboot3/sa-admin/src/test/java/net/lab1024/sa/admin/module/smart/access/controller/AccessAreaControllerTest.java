/*
 * 门禁区域控制器单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.access.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessAreaForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.smart.access.service.AccessAreaService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 门禁区域控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁区域控制器单元测试")
class AccessAreaControllerTest {

    @Mock
    private AccessAreaService accessAreaService;

    @InjectMocks
    private AccessAreaController accessAreaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accessAreaController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("测试获取区域列表 - 成功")
    void testGetAreaList_Success() throws Exception {
        // Arrange
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(20);

        PageResult<Object> pageResult = PageResult.of(Arrays.asList(), 1, 20, 1);
        when(accessAreaService.getAreaList(any())).thenReturn(pageResult);

        // Act & Assert
        mockMvc.perform(post("/api/smart/access/area/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pageParam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20));

        verify(accessAreaService, times(1)).getAreaList(any());
    }

    @Test
    @DisplayName("测试获取区域详情 - 成功")
    void testGetAreaDetail_Success() throws Exception {
        // Arrange
        when(accessAreaService.getAreaDetail(1L)).thenReturn(
                Map.of("areaId", 1L, "areaName", "测试区域", "areaCode", "TEST_AREA_001")
        );

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.areaId").value(1))
                .andExpect(jsonPath("$.data.areaName").value("测试区域"))
                .andExpect(jsonPath("$.data.areaCode").value("TEST_AREA_001"));

        verify(accessAreaService, times(1)).getAreaDetail(1L);
    }

    @Test
    @DisplayName("测试添加区域 - 成功")
    void testAddArea_Success() throws Exception {
        // Arrange
        AccessAreaForm form = AccessAreaForm.builder()
                .areaName("新区域")
                .areaCode("NEW_AREA_001")
                .areaType("AREA")
                .description("新区域描述")
                .status(1)
                .build();

        when(accessAreaService.addArea(any(AccessAreaForm.class)))
                .thenReturn(ResponseDTO.ok("添加成功"));

        // Act & Assert
        mockMvc.perform(post("/api/smart/access/area/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("添加成功"));

        verify(accessAreaService, times(1)).addArea(any(AccessAreaForm.class));
    }

    @Test
    @DisplayName("测试更新区域 - 成功")
    void testUpdateArea_Success() throws Exception {
        // Arrange
        AccessAreaForm form = AccessAreaForm.builder()
                .areaId(1L)
                .areaName("更新区域")
                .areaCode("UPDATE_AREA_001")
                .areaType("AREA")
                .description("更新区域描述")
                .status(1)
                .build();

        when(accessAreaService.updateArea(any(AccessAreaForm.class)))
                .thenReturn(ResponseDTO.ok("更新成功"));

        // Act & Assert
        mockMvc.perform(put("/api/smart/access/area/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("更新成功"));

        verify(accessAreaService, times(1)).updateArea(any(AccessAreaForm.class));
    }

    @Test
    @DisplayName("测试删除区域 - 成功")
    void testDeleteArea_Success() throws Exception {
        // Arrange
        when(accessAreaService.deleteArea(1L))
                .thenReturn(ResponseDTO.ok("删除成功"));

        // Act & Assert
        mockMvc.perform(delete("/api/smart/access/area/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("删除成功"));

        verify(accessAreaService, times(1)).deleteArea(1L);
    }

    @Test
    @DisplayName("测试批量删除区域 - 成功")
    void testBatchDeleteAreas_Success() throws Exception {
        // Arrange
        List<Long> areaIds = Arrays.asList(1L, 2L);
        when(accessAreaService.batchDeleteAreas(areaIds))
                .thenReturn(ResponseDTO.ok("批量删除成功"));

        // Act & Assert
        mockMvc.perform(delete("/api/smart/access/area/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("areaIds", areaIds))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("批量删除成功"));

        verify(accessAreaService, times(1)).batchDeleteAreas(areaIds);
    }

    @Test
    @DisplayName("测试获取区域树 - 成功")
    void testGetAreaTree_Success() throws Exception {
        // Arrange
        List<AccessAreaTreeVO> treeData = Arrays.asList(
                AccessAreaTreeVO.builder()
                        .areaId(1L)
                        .areaName("根区域")
                        .areaCode("ROOT_AREA")
                        .areaType("BUILDING")
                        .children(Arrays.asList())
                        .build()
        );

        when(accessAreaService.getAreaTree()).thenReturn(treeData);

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].areaId").value(1))
                .andExpect(jsonPath("$.data[0].areaName").value("根区域"))
                .andExpect(jsonPath("$.data[0].areaCode").value("ROOT_AREA"))
                .andExpect(jsonPath("$.data[0].areaType").value("BUILDING"));

        verify(accessAreaService, times(1)).getAreaTree();
    }

    @Test
    @DisplayName("测试更新区域状态 - 成功")
    void testUpdateAreaStatus_Success() throws Exception {
        // Arrange
        when(accessAreaService.updateAreaStatus(1L, 0))
                .thenReturn(ResponseDTO.ok("状态更新成功"));

        // Act & Assert
        mockMvc.perform(put("/api/smart/access/area/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "areaId", 1L,
                                "status", 0
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("状态更新成功"));

        verify(accessAreaService, times(1)).updateAreaStatus(1L, 0);
    }

    @Test
    @DisplayName("测试验证区域编码 - 成功")
    void testValidateAreaCode_Success() throws Exception {
        // Arrange
        when(accessAreaService.validateAreaCode("NEW_CODE", null))
                .thenReturn(ResponseDTO.ok(true));

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/validate")
                        .param("areaCode", "NEW_CODE")
                        .param("excludeId", (String) null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(accessAreaService, times(1)).validateAreaCode("NEW_CODE", null);
    }

    @Test
    @DisplayName("测试获取区域统计 - 成功")
    void testGetAreaStats_Success() throws Exception {
        // Arrange
        Map<String, Object> stats = Map.of(
                "totalArea", 10,
                "enabledArea", 8,
                "disabledArea", 2,
                "deviceStats", Map.of(
                        "totalDevices", 50,
                        "onlineDevices", 45,
                        "offlineDevices", 5
                )
        );

        when(accessAreaService.getAreaStats())
                .thenReturn(ResponseDTO.ok(stats));

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.totalArea").value(10))
                .andExpect(jsonPath("$.data.enabledArea").value(8))
                .andExpect(jsonPath("$.data.disabledArea").value(2))
                .andExpect(jsonPath("$.data.deviceStats.totalDevices").value(50));

        verify(accessAreaService, times(1)).getAreaStats();
    }

    @Test
    @DisplayName("测试获取区域选项 - 成功")
    void testGetAreaOptions_Success() throws Exception {
        // Arrange
        List<Map<String, Object>> options = Arrays.asList(
                Map.of("areaId", 1L, "areaName", "区域1", "areaCode", "AREA_001"),
                Map.of("areaId", 2L, "areaName", "区域2", "areaCode", "AREA_002")
        );

        when(accessAreaService.getAreaOptions())
                .thenReturn(ResponseDTO.ok(options));

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].areaId").value(1))
                .andExpect(jsonPath("$.data[0].areaName").value("区域1"))
                .andExpect(jsonPath("$.data[0].areaCode").value("AREA_001"))
                .andExpect(jsonPath("$.data[1].areaId").value(2))
                .andExpect(jsonPath("$.data[1].areaName").value("区域2"))
                .andExpect(jsonPath("$.data[1].areaCode").value("AREA_002"));

        verify(accessAreaService, times(1)).getAreaOptions();
    }

    @Test
    @DisplayName("测试获取区域设备 - 成功")
    void testGetAreaDevices_Success() throws Exception {
        // Arrange
        List<Map<String, Object>> devices = Arrays.asList(
                Map.of("deviceId", 1L, "deviceName", "设备1", "deviceStatus", "ONLINE"),
                Map.of("deviceId", 2L, "deviceName", "设备2", "deviceStatus", "OFFLINE")
        );

        when(accessAreaService.getAreaDevices(1L))
                .thenReturn(ResponseDTO.ok(devices));

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].deviceId").value(1))
                .andExpect(jsonPath("$.data[0].deviceName").value("设备1"))
                .andExpect(jsonPath("$.data[0].deviceStatus").value("ONLINE"))
                .andExpect(jsonPath("$.data[1].deviceId").value(2))
                .andExpect(jsonPath("$.data[1].deviceName").value("设备2"))
                .andExpect(jsonPath("$.data[1].deviceStatus").value("OFFLINE"));

        verify(accessAreaService, times(1)).getAreaDevices(1L);
    }

    @Test
    @DisplayName("测试获取区域权限 - 成功")
    void testGetAreaPermissions_Success() throws Exception {
        // Arrange
        List<Map<String, Object>> permissions = Arrays.asList(
                Map.of("permissionId", 1L, "permissionName", "进入权限", "enabled", true),
                Map.of("permissionId", 2L, "permissionName", "离开权限", "enabled", false)
        );

        when(accessAreaService.getAreaPermissions(1L))
                .thenReturn(ResponseDTO.ok(permissions));

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].permissionId").value(1))
                .andExpect(jsonPath("$.data[0].permissionName").value("进入权限"))
                .andExpect(jsonPath("$.data[0].enabled").value(true))
                .andExpect(jsonPath("$.data[1].permissionId").value(2))
                .andExpect(jsonPath("$.data[1].permissionName").value("离开权限"))
                .andExpect(jsonPath("$.data[1].enabled").value(false));

        verify(accessAreaService, times(1)).getAreaPermissions(1L);
    }

    @Test
    @DisplayName("测试更新区域权限 - 成功")
    void testUpdateAreaPermissions_Success() throws Exception {
        // Arrange
        List<Map<String, Object>> permissions = Arrays.asList(
                Map.of("permissionId", 1L, "enabled", true),
                Map.of("permissionId", 2L, "enabled", false)
        );

        when(accessAreaService.updateAreaPermissions(1L, permissions))
                .thenReturn(ResponseDTO.ok("权限更新成功"));

        // Act & Assert
        mockMvc.perform(put("/api/smart/access/area/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "areaId", 1L,
                                "permissions", permissions
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.msg").value("权限更新成功"));

        verify(accessAreaService, times(1)).updateAreaPermissions(1L, permissions);
    }

    @Test
    @DisplayName("测试添加区域 - 参数验证失败")
    void testAddArea_ValidationError() throws Exception {
        // Arrange
        AccessAreaForm invalidForm = AccessAreaForm.builder()
                .areaName("") // 空名称
                .areaCode("") // 空编码
                .build();

        when(accessAreaService.addArea(any(AccessAreaForm.class)))
                .thenReturn(ResponseDTO.error("区域名称不能为空"));

        // Act & Assert
        mockMvc.perform(post("/api/smart/access/area/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").value("区域名称不能为空"));

        verify(accessAreaService, times(1)).addArea(any(AccessAreaForm.class));
    }

    @Test
    @DisplayName("测试更新区域 - 参数验证失败")
    void testUpdateArea_ValidationError() throws Exception {
        // Arrange
        AccessAreaForm invalidForm = AccessAreaForm.builder()
                .areaId(1L)
                .areaName("") // 空名称
                .build();

        when(accessAreaService.updateArea(any(AccessAreaForm.class)))
                .thenReturn(ResponseDTO.error("区域名称不能为空"));

        // Act & Assert
        mockMvc.perform(put("/api/smart/access/area/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").value("区域名称不能为空"));

        verify(accessAreaService, times(1)).updateArea(any(AccessAreaForm.class));
    }

    @Test
    @DisplayName("测试删除区域 - ID不存在")
    void testDeleteArea_NotFound() throws Exception {
        // Arrange
        when(accessAreaService.deleteArea(999L))
                .thenReturn(ResponseDTO.error("区域不存在"));

        // Act & Assert
        mockMvc.perform(delete("/api/smart/access/area/delete/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").value("区域不存在"));

        verify(accessAreaService, times(1)).deleteArea(999L);
    }

    @Test
    @DisplayName("测试控制器异常处理")
    void testController_ExceptionHandling() throws Exception {
        // Arrange
        when(accessAreaService.getAreaDetail(1L))
                .thenThrow(new RuntimeException("服务异常"));

        // Act & Assert
        mockMvc.perform(get("/api/smart/access/area/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").exists());

        verify(accessAreaService, times(1)).getAreaDetail(1L);
    }
}
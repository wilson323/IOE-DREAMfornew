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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.system.domain.entity.DepartmentEntity;
import net.lab1024.sa.system.domain.form.DepartmentAddForm;
import net.lab1024.sa.system.domain.form.DepartmentQueryForm;
import net.lab1024.sa.system.domain.form.DepartmentUpdateForm;
import net.lab1024.sa.system.domain.vo.DepartmentVO;
import net.lab1024.sa.system.service.DepartmentService;

/**
 * DepartmentController 单元测试
 * 测试部门管理API的完整功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("部门管理控制器测试")
public class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long mockUserId = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController).build();
        objectMapper = new ObjectMapper();

        // Mock SmartRequestUtil 获取当前用户
        MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class);
        mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);
    }

    // ==================== 基础部门管理测试 ====================

    @Test
    @DisplayName("创建部门 - 成功")
    void testCreateDepartment_Success() throws Exception {
        DepartmentAddForm createForm = new DepartmentAddForm();
        createForm.setDepartmentName("技术部");
        createForm.setDepartmentCode("TECH");
        createForm.setParentId(0L);
        createForm.setManagerUserId(1L);
        createForm.setContactPhone("021-12345678");
        createForm.setContactEmail("tech@company.com");
        createForm.setSortNumber(1);
        createForm.setDescription("技术研发部门");

        DepartmentEntity createdEntity = new DepartmentEntity();
        createdEntity.setDepartmentId(1L);
        createdEntity.setDepartmentName("技术部");
        createdEntity.setDepartmentCode("TECH");
        createdEntity.setParentId(0L);
        createdEntity.setManagerUserId(1L);
        createdEntity.setContactPhone("021-12345678");
        createdEntity.setContactEmail("tech@company.com");
        createdEntity.setSortNumber(1);
        createdEntity.setDescription("技术研发部门");
        createdEntity.setStatus(1);

        when(departmentService.addDepartment(any(DepartmentAddForm.class), eq(mockUserId)))
                .thenReturn(ResponseDTO.ok(1L));

        mockMvc.perform(post("/api/department/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(1L));

        verify(departmentService, times(1)).addDepartment(any(DepartmentAddForm.class), eq(mockUserId));
    }

    @Test
    @DisplayName("更新部门 - 成功")
    void testUpdateDepartment_Success() throws Exception {
        DepartmentUpdateForm updateForm = new DepartmentUpdateForm();
        updateForm.setDepartmentId(1L);
        updateForm.setDepartmentName("技术研发部");
        updateForm.setContactPhone("021-87654321");
        updateForm.setSortNumber(2);

        when(departmentService.updateDepartment(any(DepartmentUpdateForm.class), eq(mockUserId)))
                .thenReturn(ResponseDTO.okMsg("更新成功"));

        mockMvc.perform(post("/api/department/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(departmentService, times(1)).updateDepartment(any(DepartmentUpdateForm.class), eq(mockUserId));
    }

    @Test
    @DisplayName("删除部门 - 成功")
    void testDeleteDepartment_Success() throws Exception {
        Long departmentId = 1L;

        when(departmentService.deleteDepartment(departmentId, mockUserId))
                .thenReturn(ResponseDTO.okMsg("删除成功"));

        mockMvc.perform(delete("/api/department/{departmentId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(departmentService, times(1)).deleteDepartment(departmentId, mockUserId);
    }

    @Test
    @DisplayName("根据ID查询部门 - 成功")
    void testGetDepartmentById_Success() throws Exception {
        Long departmentId = 1L;

        DepartmentVO departmentVO = new DepartmentVO();
        departmentVO.setDepartmentId(departmentId);
        departmentVO.setDepartmentName("技术部");
        departmentVO.setDepartmentCode("TECH");
        departmentVO.setParentId(0L);
        departmentVO.setManager("张三");
        departmentVO.setContactPhone("021-12345678");
        departmentVO.setContactEmail("tech@company.com");
        departmentVO.setSortNumber(1);
        departmentVO.setDescription("技术研发部门");
        departmentVO.setStatus(1);
        departmentVO.setEmployeeCount(25);
        departmentVO.setChildCount(3);
        departmentVO.setParentName("根部门");

        when(departmentService.getDepartmentById(departmentId))
                .thenReturn(ResponseDTO.ok(departmentVO));

        mockMvc.perform(get("/api/department/{departmentId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(departmentId))
                .andExpect(jsonPath("$.data.departmentName").value("技术部"))
                .andExpect(jsonPath("$.data.employeeCount").value(25))
                .andExpect(jsonPath("$.data.childCount").value(3))
                .andExpect(jsonPath("$.data.parentName").value("根部门"));

        verify(departmentService, times(1)).getDepartmentById(departmentId);
    }

    @Test
    @DisplayName("分页查询部门 - 成功")
    void testQueryDepartmentPage_Success() throws Exception {
        DepartmentQueryForm queryForm = new DepartmentQueryForm();
        queryForm.setDepartmentName("技术");
        queryForm.setStatus(1);
        // PageForm继承的分页字段会自动设置

        PageResult<DepartmentVO> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(
                createMockDepartmentVO(1L, "技术部", "TECH", 0L, 25, 3),
                createMockDepartmentVO(2L, "技术支持部", "TECH_SUPPORT", 1L, 15, 2)));
        pageResult.setTotal(2L);
        pageResult.setPageNum(1L);
        pageResult.setPageSize(10L);

        when(departmentService.queryDepartmentPage(any(DepartmentQueryForm.class)))
                .thenReturn(pageResult);

        mockMvc.perform(post("/api/department/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(2L))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].departmentName").value("技术部"));

        verify(departmentService, times(1)).queryDepartmentPage(any(DepartmentQueryForm.class));
    }

    @Test
    @DisplayName("获取所有启用部门 - 成功")
    void testGetAllEnabledDepartments_Success() throws Exception {
        List<DepartmentVO> departmentList = Arrays.asList(
                createMockDepartmentVO(1L, "技术部", "TECH", 0L, 25, 3),
                createMockDepartmentVO(2L, "市场部", "MARKET", 0L, 18, 2),
                createMockDepartmentVO(3L, "人事部", "HR", 0L, 8, 1));

        when(departmentService.getAllEnabledDepartments()).thenReturn(departmentList);

        mockMvc.perform(get("/api/department/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].departmentName").value("技术部"));

        verify(departmentService, times(1)).getAllEnabledDepartments();
    }

    // ==================== 部门树结构测试 ====================

    @Test
    @DisplayName("获取部门树 - 成功")
    void testGetDepartmentTree_Success() throws Exception {
        List<DepartmentVO> departmentTree = Arrays.asList(
                createMockDepartmentTreeVO(1L, "技术部", "TECH", 0L, Arrays.asList(
                        createMockDepartmentTreeVO(2L, "研发组", "TECH_DEV", 1L, Arrays.asList()),
                        createMockDepartmentTreeVO(3L, "测试组", "TECH_TEST", 1L, Arrays.asList()))),
                createMockDepartmentTreeVO(4L, "市场部", "MARKET", 0L, Arrays.asList(
                        createMockDepartmentTreeVO(5L, "销售组", "MARKET_SALES", 4L, Arrays.asList()))));

        when(departmentService.getDepartmentTree(true)).thenReturn(departmentTree);

        mockMvc.perform(get("/api/department/tree")
                .param("onlyEnabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].departmentName").value("技术部"))
                .andExpect(jsonPath("$.data[0].children").isArray())
                .andExpect(jsonPath("$.data[0].children.length()").value(2));

        verify(departmentService, times(1)).getDepartmentTree(true);
    }

    @Test
    @DisplayName("获取部门及其子部门ID - 成功")
    void testGetDepartmentSelfAndChildrenIds_Success() throws Exception {
        Long departmentId = 1L;
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        when(departmentService.getDepartmentSelfAndChildrenIds(departmentId)).thenReturn(ids);

        mockMvc.perform(get("/api/department/{departmentId}/children", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));

        verify(departmentService, times(1)).getDepartmentSelfAndChildrenIds(departmentId);
    }

    // ==================== 部门移动测试 ====================

    @Test
    @DisplayName("移动部门 - 成功")
    void testMoveDepartment_Success() throws Exception {
        Long departmentId = 3L;
        Long newParentId = 4L;

        when(departmentService.moveDepartment(departmentId, newParentId, mockUserId))
                .thenReturn(ResponseDTO.okMsg("移动成功"));

        mockMvc.perform(post("/api/department/move/{departmentId}", departmentId)
                .param("newParentId", newParentId.toString())
                .header("X-User-Id", mockUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(departmentService, times(1)).moveDepartment(departmentId, newParentId, mockUserId);
    }

    // ==================== 部门验证测试 ====================

    @Test
    @DisplayName("检查部门名称是否存在 - 成功")
    void testCheckDepartmentName_Success() throws Exception {
        String departmentName = "技术部";
        Long parentId = 0L;
        Long excludeId = null;

        when(departmentService.checkDepartmentNameExists(departmentName, parentId, excludeId)).thenReturn(true);

        mockMvc.perform(get("/api/department/check/name")
                .param("departmentName", departmentName)
                .param("parentId", parentId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(departmentService, times(1)).checkDepartmentNameExists(departmentName, parentId, excludeId);
    }

    @Test
    @DisplayName("检查部门编码是否存在 - 成功")
    void testCheckDepartmentCode_Success() throws Exception {
        String departmentCode = "TECH";
        Long excludeId = null;

        when(departmentService.checkDepartmentCodeExists(departmentCode, excludeId)).thenReturn(true);

        mockMvc.perform(get("/api/department/check/code")
                .param("departmentCode", departmentCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(departmentService, times(1)).checkDepartmentCodeExists(departmentCode, excludeId);
    }

    @Test
    @DisplayName("检查是否有子部门 - 成功")
    void testHasChildren_Success() throws Exception {
        Long departmentId = 1L;

        when(departmentService.hasChildren(departmentId)).thenReturn(true);

        mockMvc.perform(get("/api/department/{departmentId}/has-children", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(departmentService, times(1)).hasChildren(departmentId);
    }

    // ==================== 部门统计测试 ====================

    @Test
    @DisplayName("获取部门统计 - 成功")
    void testGetDepartmentStatistics_Success() throws Exception {
        Map<String, Object> statistics = Map.of(
                "totalCount", 15,
                "enabledCount", 13,
                "disabledCount", 2,
                "rootCount", 3,
                "childCount", 12,
                "totalEmployeeCount", 156,
                "averageEmployeeCount", 10.4);

        when(departmentService.getDepartmentStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/department/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(15))
                .andExpect(jsonPath("$.data.enabledCount").value(13))
                .andExpect(jsonPath("$.data.totalEmployeeCount").value(156));

        verify(departmentService, times(1)).getDepartmentStatistics();
    }

    // ==================== 部门状态管理测试 ====================

    @Test
    @DisplayName("修改部门状态 - 成功")
    void testChangeDepartmentStatus_Success() throws Exception {
        Long departmentId = 1L;
        Integer status = 0; // 禁用

        when(departmentService.changeDepartmentStatus(departmentId, status, mockUserId))
                .thenReturn(ResponseDTO.okMsg("修改状态成功"));

        mockMvc.perform(post("/api/department/status/{departmentId}", departmentId)
                .param("status", status.toString())
                .header("X-User-Id", mockUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(departmentService, times(1)).changeDepartmentStatus(departmentId, status, mockUserId);
    }

    @Test
    @DisplayName("批量修改部门状态 - 成功")
    void testBatchChangeDepartmentStatus_Success() throws Exception {
        List<Long> departmentIds = Arrays.asList(1L, 2L);
        Integer status = 0; // 禁用

        when(departmentService.batchChangeDepartmentStatus(departmentIds, status, mockUserId))
                .thenReturn(ResponseDTO.okMsg("批量修改状态成功"));

        mockMvc.perform(post("/api/department/status/batch")
                .param("status", status.toString())
                .param("departmentIds", "1")
                .param("departmentIds", "2")
                .header("X-User-Id", mockUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(departmentService, times(1)).batchChangeDepartmentStatus(departmentIds, status, mockUserId);
    }

    // ==================== 辅助方法 ====================

    private DepartmentVO createMockDepartmentVO(Long departmentId, String departmentName, String departmentCode,
            Long parentId, Integer employeeCount, Integer childCount) {
        DepartmentVO departmentVO = new DepartmentVO();
        departmentVO.setDepartmentId(departmentId);
        departmentVO.setDepartmentName(departmentName);
        departmentVO.setDepartmentCode(departmentCode);
        departmentVO.setParentId(parentId);
        departmentVO.setManager("经理" + departmentId);
        departmentVO.setContactPhone("021-1234567" + departmentId);
        departmentVO.setContactEmail(departmentCode.toLowerCase() + "@company.com");
        departmentVO.setSortNumber(1);
        departmentVO.setDescription(departmentName + "部门");
        departmentVO.setStatus(1);
        departmentVO.setEmployeeCount(employeeCount);
        departmentVO.setChildCount(childCount);
        departmentVO.setParentName(parentId == null || parentId == 0L ? "根部门" : "父部门");
        departmentVO.setCreateTime(LocalDateTime.now());
        return departmentVO;
    }

    private DepartmentVO createMockDepartmentTreeVO(Long departmentId, String departmentName, String departmentCode,
            Long parentId, List<DepartmentVO> children) {
        DepartmentVO departmentVO = createMockDepartmentVO(departmentId, departmentName, departmentCode, parentId, 0,
                0);
        departmentVO.setChildren(children);
        return departmentVO;
    }
}

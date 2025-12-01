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
import net.lab1024.sa.system.dict.service.DictDataService;
import net.lab1024.sa.system.dict.service.DictTypeService;
import net.lab1024.sa.system.domain.form.DictDataAddForm;
import net.lab1024.sa.system.domain.form.DictDataUpdateForm;
import net.lab1024.sa.system.domain.form.DictQueryForm;
import net.lab1024.sa.system.domain.form.DictTypeAddForm;
import net.lab1024.sa.system.domain.form.DictTypeUpdateForm;
import net.lab1024.sa.system.domain.vo.DictDataVO;
import net.lab1024.sa.system.domain.vo.DictTypeVO;

/**
 * DictController 单元测试
 * <p>
 * 测试数据字典管理API的完整功能
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
@DisplayName("数据字典控制器测试")
public class DictControllerTest {

    @Mock
    private DictTypeService dictTypeService;

    @Mock
    private DictDataService dictDataService;

    @InjectMocks
    private DictController dictController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long mockUserId = 1L;

    /**
     * 测试初始化方法
     * 在每个测试方法执行前初始化MockMvc和ObjectMapper
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dictController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== 字典类型管理测试 ====================

    @Test
    @DisplayName("分页查询字典类型 - 成功")
    void testQueryDictTypePage_Success() throws Exception {
        // 准备测试数据
        DictQueryForm queryForm = new DictQueryForm();
        // PageForm 已有默认值 pageNum=1, pageSize=20

        PageResult<DictTypeVO> pageResult = new PageResult<>();
        pageResult.setTotal(2L);
        pageResult.setList(Arrays.asList(
                createMockDictTypeVO(1L, "用户状态", "USER_STATUS", 5),
                createMockDictTypeVO(2L, "用户类型", "USER_TYPE", 3)));

        // Mock服务层方法
        when(dictTypeService.queryDictTypePage(any(DictQueryForm.class)))
                .thenReturn(ResponseDTO.ok(pageResult));

        // 执行请求并验证
        mockMvc.perform(post("/api/dict/type/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(2L))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(2));

        // 验证服务层方法调用
        verify(dictTypeService, times(1)).queryDictTypePage(any(DictQueryForm.class));
    }

    @Test
    @DisplayName("查询字典类型列表 - 成功")
    void testQueryDictTypeList_Success() throws Exception {
        // 准备测试数据
        DictQueryForm queryForm = new DictQueryForm();

        List<DictTypeVO> dictTypeList = Arrays.asList(
                createMockDictTypeVO(1L, "用户状态", "USER_STATUS", 5),
                createMockDictTypeVO(2L, "用户类型", "USER_TYPE", 3),
                createMockDictTypeVO(3L, "系统配置", "SYSTEM_CONFIG", 8));

        // Mock服务层方法
        when(dictTypeService.queryDictTypeList(any(DictQueryForm.class)))
                .thenReturn(ResponseDTO.ok(dictTypeList));

        // 执行请求并验证
        mockMvc.perform(post("/api/dict/type/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].dictTypeName").value("用户状态"));

        // 验证服务层方法调用
        verify(dictTypeService, times(1)).queryDictTypeList(any(DictQueryForm.class));
    }

    @Test
    @DisplayName("获取字典类型详情 - 成功")
    void testGetDictTypeById_Success() throws Exception {
        // 准备测试数据
        Long dictTypeId = 1L;
        DictTypeVO dictTypeVO = createMockDictTypeVO(dictTypeId, "用户状态", "USER_STATUS", 5);

        // Mock服务层方法
        when(dictTypeService.getDictTypeDetail(dictTypeId))
                .thenReturn(ResponseDTO.ok(dictTypeVO));

        // 执行请求并验证
        mockMvc.perform(get("/api/dict/type/{dictTypeId}", dictTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.dictTypeId").value(dictTypeId))
                .andExpect(jsonPath("$.data.dictTypeName").value("用户状态"))
                .andExpect(jsonPath("$.data.dictTypeCode").value("USER_STATUS"));

        // 验证服务层方法调用
        verify(dictTypeService, times(1)).getDictTypeDetail(dictTypeId);
    }

    @Test
    @DisplayName("新增字典类型 - 成功")
    void testAddDictType_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            DictTypeAddForm addForm = new DictTypeAddForm();
            addForm.setDictTypeName("用户状态");
            addForm.setDictTypeCode("USER_STATUS");
            addForm.setDescription("用户状态字典");
            addForm.setSortNumber(1);
            addForm.setStatus(1);

            // Mock服务层方法
            when(dictTypeService.addDictType(any(DictTypeAddForm.class), eq(mockUserId)))
                    .thenReturn(ResponseDTO.ok(1L));

            // 执行请求并验证
            mockMvc.perform(post("/api/dict/type/add")
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addForm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data").value(1L));

            // 验证服务层方法调用
            verify(dictTypeService, times(1)).addDictType(any(DictTypeAddForm.class), eq(mockUserId));
        }
    }

    @Test
    @DisplayName("更新字典类型 - 成功")
    void testUpdateDictType_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            DictTypeUpdateForm updateForm = new DictTypeUpdateForm();
            updateForm.setDictTypeId(1L);
            updateForm.setDictTypeName("用户状态(已更新)");
            updateForm.setDescription("更新的用户状态字典");
            updateForm.setSortNumber(2);

            // Mock服务层方法
            when(dictTypeService.updateDictType(any(DictTypeUpdateForm.class), eq(mockUserId)))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(post("/api/dict/type/update")
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateForm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(dictTypeService, times(1)).updateDictType(any(DictTypeUpdateForm.class), eq(mockUserId));
        }
    }

    @Test
    @DisplayName("删除字典类型 - 成功")
    void testDeleteDictType_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Long dictTypeId = 1L;

            // Mock服务层方法
            when(dictTypeService.deleteDictType(dictTypeId, mockUserId))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(delete("/api/dict/type/{dictTypeId}", dictTypeId)
                    .header("X-User-Id", mockUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(dictTypeService, times(1)).deleteDictType(dictTypeId, mockUserId);
        }
    }

    // ==================== 字典数据管理测试 ====================

    @Test
    @DisplayName("分页查询字典数据 - 成功")
    void testQueryDictDataPage_Success() throws Exception {
        // 准备测试数据
        DictQueryForm queryForm = new DictQueryForm();
        // DictQueryForm 继承自 PageForm，pageNum 和 pageSize 已存在
        queryForm.setDictTypeId(1L);

        PageResult<DictDataVO> pageResult = new PageResult<>();
        pageResult.setTotal(2L);
        pageResult.setList(Arrays.asList(
                createMockDictDataVO(1L, 1L, "正常", "1", 1),
                createMockDictDataVO(2L, 1L, "禁用", "0", 2)));

        // Mock服务层方法
        when(dictDataService.queryDictDataPage(any(DictQueryForm.class)))
                .thenReturn(ResponseDTO.ok(pageResult));

        // 执行请求并验证
        mockMvc.perform(post("/api/dict/data/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(2L))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(2));

        // 验证服务层方法调用
        verify(dictDataService, times(1)).queryDictDataPage(any(DictQueryForm.class));
    }

    @Test
    @DisplayName("根据字典类型ID查询字典数据 - 成功")
    void testGetDictDataByTypeId_Success() throws Exception {
        // 准备测试数据
        Long dictTypeId = 1L;
        List<DictDataVO> dictDataList = Arrays.asList(
                createMockDictDataVO(1L, dictTypeId, "正常", "1", 1),
                createMockDictDataVO(2L, dictTypeId, "禁用", "0", 2));

        // Mock服务层方法
        when(dictDataService.getDictDataByTypeId(dictTypeId))
                .thenReturn(ResponseDTO.ok(dictDataList));

        // 执行请求并验证
        mockMvc.perform(get("/api/dict/data/type/{dictTypeId}", dictTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].dictLabel").value("正常"))
                .andExpect(jsonPath("$.data[1].dictLabel").value("禁用"));

        // 验证服务层方法调用
        verify(dictDataService, times(1)).getDictDataByTypeId(dictTypeId);
    }

    @Test
    @DisplayName("根据字典类型编码查询字典数据 - 成功")
    void testGetDictDataByTypeCode_Success() throws Exception {
        // 准备测试数据
        String dictTypeCode = "USER_STATUS";
        List<DictDataVO> dictDataList = Arrays.asList(
                createMockDictDataVO(1L, 1L, "正常", "1", 1),
                createMockDictDataVO(2L, 1L, "禁用", "0", 2));

        // Mock服务层方法
        when(dictDataService.getDictDataByTypeCode(dictTypeCode))
                .thenReturn(ResponseDTO.ok(dictDataList));

        // 执行请求并验证
        mockMvc.perform(get("/api/dict/data/code/{dictTypeCode}", dictTypeCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 验证服务层方法调用
        verify(dictDataService, times(1)).getDictDataByTypeCode(dictTypeCode);
    }

    @Test
    @DisplayName("获取字典数据详情 - 成功")
    void testGetDictDataById_Success() throws Exception {
        // 准备测试数据
        Long dictDataId = 1L;
        DictDataVO dictDataVO = createMockDictDataVO(dictDataId, 1L, "正常", "1", 1);

        // Mock服务层方法
        when(dictDataService.getDictDataDetail(dictDataId))
                .thenReturn(ResponseDTO.ok(dictDataVO));

        // 执行请求并验证
        mockMvc.perform(get("/api/dict/data/{dictDataId}", dictDataId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.dictDataId").value(dictDataId))
                .andExpect(jsonPath("$.data.dictLabel").value("正常"))
                .andExpect(jsonPath("$.data.dictValue").value("1"));

        // 验证服务层方法调用
        verify(dictDataService, times(1)).getDictDataDetail(dictDataId);
    }

    @Test
    @DisplayName("新增字典数据 - 成功")
    void testAddDictData_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            DictDataAddForm addForm = new DictDataAddForm();
            addForm.setDictTypeId(1L);
            addForm.setDictLabel("正常");
            addForm.setDictValue("1");
            addForm.setSortNumber(1);
            addForm.setRemark("正常状态");
            addForm.setStatus(1);

            // Mock服务层方法
            when(dictDataService.addDictData(any(DictDataAddForm.class), eq(mockUserId)))
                    .thenReturn(ResponseDTO.ok(1L));

            // 执行请求并验证
            mockMvc.perform(post("/api/dict/data/add")
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addForm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true))
                    .andExpect(jsonPath("$.data").value(1L));

            // 验证服务层方法调用
            verify(dictDataService, times(1)).addDictData(any(DictDataAddForm.class), eq(mockUserId));
        }
    }

    @Test
    @DisplayName("更新字典数据 - 成功")
    void testUpdateDictData_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            DictDataUpdateForm updateForm = new DictDataUpdateForm();
            updateForm.setDictDataId(1L);
            updateForm.setDictLabel("启用");
            updateForm.setDictValue("2");
            updateForm.setSortNumber(2);
            updateForm.setRemark("启用状态");

            // Mock服务层方法
            when(dictDataService.updateDictData(any(DictDataUpdateForm.class), eq(mockUserId)))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(post("/api/dict/data/update")
                    .header("X-User-Id", mockUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateForm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(dictDataService, times(1)).updateDictData(any(DictDataUpdateForm.class), eq(mockUserId));
        }
    }

    @Test
    @DisplayName("删除字典数据 - 成功")
    void testDeleteDictData_Success() throws Exception {
        try (MockedStatic<SmartRequestUtil> mockedRequestUtil = mockStatic(SmartRequestUtil.class)) {
            // Mock用户ID获取
            mockedRequestUtil.when(SmartRequestUtil::getRequestUserId).thenReturn(mockUserId);

            // 准备测试数据
            Long dictDataId = 1L;

            // Mock服务层方法
            when(dictDataService.deleteDictData(dictDataId, mockUserId))
                    .thenReturn(ResponseDTO.<Void>ok());

            // 执行请求并验证
            mockMvc.perform(delete("/api/dict/data/{dictDataId}", dictDataId)
                    .header("X-User-Id", mockUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));

            // 验证服务层方法调用
            verify(dictDataService, times(1)).deleteDictData(dictDataId, mockUserId);
        }
    }

    // ==================== 字典缓存管理测试 ====================

    @Test
    @DisplayName("刷新字典缓存 - 成功")
    void testRefreshDictCache_Success() throws Exception {
        // Mock服务层方法
        when(dictTypeService.refreshDictTypeCache(null))
                .thenReturn(ResponseDTO.<Void>ok());
        when(dictDataService.refreshDictDataCache(null))
                .thenReturn(ResponseDTO.<Void>ok());

        // 执行请求并验证
        mockMvc.perform(post("/api/dict/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 验证服务层方法调用
        verify(dictTypeService, times(1)).refreshDictTypeCache(null);
        verify(dictDataService, times(1)).refreshDictDataCache(null);
    }

    @Test
    @DisplayName("获取字典缓存 - 成功")
    void testGetDictCache_Success() throws Exception {
        // 准备测试数据
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("USER_STATUS", Arrays.asList(
                Map.of("dictLabel", "正常", "dictValue", "1"),
                Map.of("dictLabel", "禁用", "dictValue", "0")));

        // Mock服务层方法
        when(dictDataService.getDictCache())
                .thenReturn(ResponseDTO.ok(cacheData));

        // 执行请求并验证
        mockMvc.perform(get("/api/dict/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        // 验证服务层方法调用
        verify(dictDataService, times(1)).getDictCache();
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建Mock字典类型VO对象
     *
     * @param dictTypeId   字典类型ID
     * @param dictTypeName 字典类型名称
     * @param dictTypeCode 字典类型编码
     * @param dataCount    关联数据项数量
     * @return DictTypeVO对象
     */
    private DictTypeVO createMockDictTypeVO(Long dictTypeId, String dictTypeName, String dictTypeCode,
            Integer dataCount) {
        DictTypeVO dictTypeVO = new DictTypeVO();
        dictTypeVO.setDictTypeId(dictTypeId);
        dictTypeVO.setDictTypeName(dictTypeName);
        dictTypeVO.setDictTypeCode(dictTypeCode);
        dictTypeVO.setDescription(dictTypeName + "字典");
        dictTypeVO.setStatus(1);
        dictTypeVO.setSortNumber(1);
        dictTypeVO.setCreateTime(LocalDateTime.now());
        return dictTypeVO;
    }

    /**
     * 创建Mock字典数据VO对象
     *
     * @param dictDataId 字典数据ID
     * @param dictTypeId 字典类型ID
     * @param dictLabel  字典标签
     * @param dictValue  字典值
     * @param dictSort   排序
     * @return DictDataVO对象
     */
    private DictDataVO createMockDictDataVO(Long dictDataId, Long dictTypeId, String dictLabel, String dictValue,
            Integer dictSort) {
        DictDataVO dictDataVO = new DictDataVO();
        dictDataVO.setDictDataId(dictDataId);
        dictDataVO.setDictTypeId(dictTypeId);
        dictDataVO.setDictLabel(dictLabel);
        dictDataVO.setDictValue(dictValue);
        dictDataVO.setSortNumber(dictSort);
        dictDataVO.setRemark(dictLabel + "状态");
        dictDataVO.setStatus(1);
        dictDataVO.setCreateTime(LocalDateTime.now());
        return dictDataVO;
    }
}

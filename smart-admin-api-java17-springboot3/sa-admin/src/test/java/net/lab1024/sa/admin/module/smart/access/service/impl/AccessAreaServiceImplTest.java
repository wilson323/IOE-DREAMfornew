/*
 * 门禁区域服务实现类单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.access.service.impl;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.access.dao.AccessAreaDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessAreaForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.smart.access.manager.AccessAreaManager;
import net.lab1024.sa.admin.module.smart.access.service.AccessAreaService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁区域服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁区域服务实现类单元测试")
class AccessAreaServiceImplTest {

    @Mock
    private AccessAreaDao accessAreaDao;

    @Mock
    private AccessAreaManager accessAreaManager;

    @InjectMocks
    private AccessAreaServiceImpl accessAreaService;

    private AccessAreaEntity testArea;
    private AccessAreaForm testAreaForm;
    private PageParam pageParam;

    @BeforeEach
    void setUp() {
        // 测试数据初始化
        testArea = AccessAreaEntity.builder()
                .areaId(1L)
                .areaName("测试区域")
                .areaCode("TEST_AREA_001")
                .areaType("AREA")
                .parentAreaId(null)
                .areaLevel(1)
                .areaPath("/1")
                .description("测试区域描述")
                .status(1)
                .sortOrder(1)
                .isDefault(false)
                .createUserId(1001L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testAreaForm = AccessAreaForm.builder()
                .areaId(1L)
                .areaName("测试区域")
                .areaCode("TEST_AREA_001")
                .areaType("AREA")
                .parentAreaId(null)
                .description("测试区域描述")
                .status(1)
                .sortOrder(1)
                .isDefault(false)
                .build();

        pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(20);
    }

    @Test
    @DisplayName("测试获取区域列表 - 成功")
    void testGetAreaList_Success() {
        // Arrange
        List<AccessAreaEntity> areaList = Arrays.asList(testArea);
        when(accessAreaDao.selectList(any())).thenReturn(areaList);
        when(accessAreaDao.selectCount(any())).thenReturn(1);

        // Act
        PageResult<AccessAreaEntity> result = accessAreaService.getAreaList(pageParam);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(1, result.getList().size());
        assertEquals("测试区域", result.getList().get(0).getAreaName());

        verify(accessAreaDao, times(1)).selectList(any());
        verify(accessAreaDao, times(1)).selectCount(any());
    }

    @Test
    @DisplayName("测试获取区域详情 - 成功")
    void testGetAreaDetail_Success() {
        // Arrange
        when(accessAreaDao.selectById(1L)).thenReturn(testArea);

        // Act
        AccessAreaEntity result = accessAreaService.getAreaDetail(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAreaId());
        assertEquals("测试区域", result.getAreaName());
        assertEquals("TEST_AREA_001", result.getAreaCode());

        verify(accessAreaDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试获取区域详情 - 区域不存在")
    void testGetAreaDetail_NotFound() {
        // Arrange
        when(accessAreaDao.selectById(999L)).thenReturn(null);

        // Act
        AccessAreaEntity result = accessAreaService.getAreaDetail(999L);

        // Assert
        assertNull(result);
        verify(accessAreaDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("测试添加区域 - 成功")
    void testAddArea_Success() {
        // Arrange
        AccessAreaForm newAreaForm = AccessAreaForm.builder()
                .areaName("新区域")
                .areaCode("NEW_AREA_001")
                .areaType("AREA")
                .parentAreaId(null)
                .description("新区域描述")
                .status(1)
                .sortOrder(1)
                .isDefault(false)
                .build();

        doNothing().when(accessAreaManager).addArea(any(AccessAreaEntity.class));

        // Act
        ResponseDTO<String> result = accessAreaService.addArea(newAreaForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("添加成功", result.getMsg());

        verify(accessAreaManager, times(1)).addArea(any(AccessAreaEntity.class));
    }

    @Test
    @DisplayName("测试添加区域 - 区域名称为空")
    void testAddArea_EmptyName() {
        // Arrange
        AccessAreaForm invalidForm = AccessAreaForm.builder()
                .areaName("")
                .areaCode("NEW_AREA_001")
                .areaType("AREA")
                .build();

        // Act
        ResponseDTO<String> result = accessAreaService.addArea(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("区域名称不能为空"));
    }

    @Test
    @DisplayName("测试添加区域 - 区域编码为空")
    void testAddArea_EmptyCode() {
        // Arrange
        AccessAreaForm invalidForm = AccessAreaForm.builder()
                .areaName("测试区域")
                .areaCode("")
                .areaType("AREA")
                .build();

        // Act
        ResponseDTO<String> result = accessAreaService.addArea(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("区域编码不能为空"));
    }

    @Test
    @DisplayName("测试更新区域 - 成功")
    void testUpdateArea_Success() {
        // Arrange
        when(accessAreaDao.selectById(1L)).thenReturn(testArea);
        doNothing().when(accessAreaManager).updateArea(any(AccessAreaEntity.class));

        // Act
        ResponseDTO<String> result = accessAreaService.updateArea(testAreaForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("更新成功", result.getMsg());

        verify(accessAreaDao, times(1)).selectById(1L);
        verify(accessAreaManager, times(1)).updateArea(any(AccessAreaEntity.class));
    }

    @Test
    @DisplayName("测试更新区域 - 区域不存在")
    void testUpdateArea_NotFound() {
        // Arrange
        when(accessAreaDao.selectById(999L)).thenReturn(null);

        AccessAreaForm updateForm = AccessAreaForm.builder()
                .areaId(999L)
                .areaName("不存在的区域")
                .build();

        // Act
        ResponseDTO<String> result = accessAreaService.updateArea(updateForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("区域不存在"));

        verify(accessAreaDao, times(1)).selectById(999L);
        verify(accessAreaManager, never()).updateArea(any());
    }

    @Test
    @DisplayName("测试删除区域 - 成功")
    void testDeleteArea_Success() {
        // Arrange
        when(accessAreaDao.selectById(1L)).thenReturn(testArea);
        doNothing().when(accessAreaManager).deleteArea(1L);

        // Act
        ResponseDTO<String> result = accessAreaService.deleteArea(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("删除成功", result.getMsg());

        verify(accessAreaDao, times(1)).selectById(1L);
        verify(accessAreaManager, times(1)).deleteArea(1L);
    }

    @Test
    @DisplayName("测试删除区域 - 区域不存在")
    void testDeleteArea_NotFound() {
        // Arrange
        when(accessAreaDao.selectById(999L)).thenReturn(null);

        // Act
        ResponseDTO<String> result = accessAreaService.deleteArea(999L);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("区域不存在"));

        verify(accessAreaDao, times(1)).selectById(999L);
        verify(accessAreaManager, never()).deleteArea(any());
    }

    @Test
    @DisplayName("测试删除区域 - 区域包含子区域")
    void testDeleteArea_HasChildren() {
        // Arrange
        AccessAreaEntity parentArea = AccessAreaEntity.builder()
                .areaId(1L)
                .areaName("父区域")
                .build();

        when(accessAreaDao.selectById(1L)).thenReturn(parentArea);
        when(accessAreaDao.selectCountByParentId(1L)).thenReturn(2);

        // Act
        ResponseDTO<String> result = accessAreaService.deleteArea(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("存在子区域，无法删除"));

        verify(accessAreaDao, times(1)).selectById(1L);
        verify(accessAreaDao, times(1)).selectCountByParentId(1L);
        verify(accessAreaManager, never()).deleteArea(any());
    }

    @Test
    @DisplayName("测试获取区域树 - 成功")
    void testGetAreaTree_Success() {
        // Arrange
        AccessAreaEntity rootArea = AccessAreaEntity.builder()
                .areaId(1L)
                .areaName("根区域")
                .parentAreaId(null)
                .areaLevel(1)
                .build();

        AccessAreaEntity childArea = AccessAreaEntity.builder()
                .areaId(2L)
                .areaName("子区域")
                .parentAreaId(1L)
                .areaLevel(2)
                .build();

        List<AccessAreaEntity> areaList = Arrays.asList(rootArea, childArea);
        when(accessAreaDao.selectAll()).thenReturn(areaList);

        // Act
        List<AccessAreaTreeVO> result = accessAreaService.getAreaTree();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // 只有根节点

        AccessAreaTreeVO root = result.get(0);
        assertEquals(1L, root.getAreaId());
        assertEquals("根区域", root.getAreaName());
        assertEquals(1, root.getChildren().size());

        AccessAreaTreeVO child = root.getChildren().get(0);
        assertEquals(2L, child.getAreaId());
        assertEquals("子区域", child.getAreaName());

        verify(accessAreaDao, times(1)).selectAll();
    }

    @Test
    @DisplayName("测试获取区域树 - 空列表")
    void testGetAreaTree_EmptyList() {
        // Arrange
        when(accessAreaDao.selectAll()).thenReturn(Arrays.asList());

        // Act
        List<AccessAreaTreeVO> result = accessAreaService.getAreaTree();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accessAreaDao, times(1)).selectAll();
    }

    @Test
    @DisplayName("测试更新区域状态 - 成功")
    void testUpdateAreaStatus_Success() {
        // Arrange
        when(accessAreaDao.selectById(1L)).thenReturn(testArea);
        when(accessAreaDao.updateById(any(AccessAreaEntity.class))).thenReturn(1);

        // Act
        ResponseDTO<String> result = accessAreaService.updateAreaStatus(1L, 0);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("状态更新成功", result.getMsg());

        verify(accessAreaDao, times(1)).selectById(1L);
        verify(accessAreaDao, times(1)).updateById(any(AccessAreaEntity.class));
    }

    @Test
    @DisplayName("测试更新区域状态 - 区域不存在")
    void testUpdateAreaStatus_NotFound() {
        // Arrange
        when(accessAreaDao.selectById(999L)).thenReturn(null);

        // Act
        ResponseDTO<String> result = accessAreaService.updateAreaStatus(999L, 0);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("区域不存在"));

        verify(accessAreaDao, times(1)).selectById(999L);
        verify(accessAreaDao, never()).updateById(any());
    }

    @Test
    @DisplayName("测试批量删除区域 - 成功")
    void testBatchDeleteAreas_Success() {
        // Arrange
        List<Long> areaIds = Arrays.asList(1L, 2L);

        when(accessAreaDao.selectById(1L)).thenReturn(testArea);
        when(accessAreaDao.selectById(2L)).thenReturn(testArea);
        when(accessAreaDao.selectCountByParentId(1L)).thenReturn(0);
        when(accessAreaDao.selectCountByParentId(2L)).thenReturn(0);

        doNothing().when(accessAreaManager).batchDeleteAreas(areaIds);

        // Act
        ResponseDTO<String> result = accessAreaService.batchDeleteAreas(areaIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("批量删除成功", result.getMsg());

        verify(accessAreaDao, times(1)).selectById(1L);
        verify(accessAreaDao, times(1)).selectById(2L);
        verify(accessAreaDao, times(1)).selectCountByParentId(1L);
        verify(accessAreaDao, times(1)).selectCountByParentId(2L);
        verify(accessAreaManager, times(1)).batchDeleteAreas(areaIds);
    }

    @Test
    @DisplayName("测试批量删除区域 - 部分区域不存在")
    void testBatchDeleteAreas_SomeNotFound() {
        // Arrange
        List<Long> areaIds = Arrays.asList(1L, 999L);

        when(accessAreaDao.selectById(1L)).thenReturn(testArea);
        when(accessAreaDao.selectById(999L)).thenReturn(null);

        // Act
        ResponseDTO<String> result = accessAreaService.batchDeleteAreas(areaIds);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("区域不存在"));

        verify(accessAreaDao, times(1)).selectById(1L);
        verify(accessAreaDao, times(1)).selectById(999L);
        verify(accessAreaManager, never()).batchDeleteAreas(any());
    }

    @Test
    @DisplayName("测试验证区域编码唯一性 - 编码可用")
    void testValidateAreaCode_Available() {
        // Arrange
        when(accessAreaDao.selectCountByAreaCode("NEW_CODE", null)).thenReturn(0);

        // Act
        ResponseDTO<Boolean> result = accessAreaService.validateAreaCode("NEW_CODE", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());

        verify(accessAreaDao, times(1)).selectCountByAreaCode("NEW_CODE", null);
    }

    @Test
    @DisplayName("测试验证区域编码唯一性 - 编码已存在")
    void testValidateAreaCode_AlreadyExists() {
        // Arrange
        when(accessAreaDao.selectCountByAreaCode("EXIST_CODE", null)).thenReturn(1);

        // Act
        ResponseDTO<Boolean> result = accessAreaService.validateAreaCode("EXIST_CODE", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertFalse(result.getData());

        verify(accessAreaDao, times(1)).selectCountByAreaCode("EXIST_CODE", null);
    }

    @Test
    @DisplayName("测试验证区域编码唯一性 - 更新时排除自身")
    void testValidateAreaCode_UpdateExcludeSelf() {
        // Arrange
        when(accessAreaDao.selectCountByAreaCode("TEST_CODE", 1L)).thenReturn(0);

        // Act
        ResponseDTO<Boolean> result = accessAreaService.validateAreaCode("TEST_CODE", 1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());

        verify(accessAreaDao, times(1)).selectCountByAreaCode("TEST_CODE", 1L);
    }

    @Test
    @DisplayName("测试获取区域统计信息")
    void testGetAreaStats() {
        // Arrange
        when(accessAreaDao.selectTotalCount()).thenReturn(100);
        when(accessAreaDao.selectCountByStatus(1)).thenReturn(85);
        when(accessAreaDao.selectCountByStatus(0)).thenReturn(15);

        // Act
        ResponseDTO<Object> result = accessAreaService.getAreaStats();

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());

        Object statsData = result.getData();
        assertNotNull(statsData);

        verify(accessAreaDao, times(1)).selectTotalCount();
        verify(accessAreaDao, times(1)).selectCountByStatus(1);
        verify(accessAreaDao, times(1)).selectCountByStatus(0);
    }

    @Test
    @DisplayName("测试获取区域选项列表")
    void testGetAreaOptions() {
        // Arrange
        List<AccessAreaEntity> areaList = Arrays.asList(
                AccessAreaEntity.builder()
                        .areaId(1L)
                        .areaName("区域1")
                        .areaCode("AREA_001")
                        .status(1)
                        .build(),
                AccessAreaEntity.builder()
                        .areaId(2L)
                        .areaName("区域2")
                        .areaCode("AREA_002")
                        .status(1)
                        .build()
        );

        when(accessAreaDao.selectByStatus(1)).thenReturn(areaList);

        // Act
        ResponseDTO<List<AccessAreaEntity>> result = accessAreaService.getAreaOptions();

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(2, result.getData().size());

        verify(accessAreaDao, times(1)).selectByStatus(1);
    }
}
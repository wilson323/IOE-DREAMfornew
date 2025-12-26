package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.dao.AccessEvacuationPointDao;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointAddForm;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointQueryForm;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessEvacuationPointVO;
import net.lab1024.sa.common.entity.access.AccessEvacuationPointEntity;
import net.lab1024.sa.access.service.impl.AccessEvacuationServiceImpl;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.domain.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁疏散点管理Service单元测试
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁疏散点管理Service单元测试")
class AccessEvacuationServiceTest {

    @Mock
    private AccessEvacuationPointDao evacuationPointDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private AccessEvacuationServiceImpl evacuationService;

    private AccessEvacuationPointEntity mockPoint;
    private AccessEvacuationPointAddForm mockAddForm;
    private AccessEvacuationPointUpdateForm mockUpdateForm;
    private AccessEvacuationPointQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockPoint = new AccessEvacuationPointEntity();
        mockPoint.setPointId(1L);
        mockPoint.setPointName("A栋消防出口");
        mockPoint.setAreaId(1001L);
        mockPoint.setEvacuationType("FIRE");
        mockPoint.setDoorIds("[3001,3002,3003]");  // 修复：使用无空格的JSON格式
        mockPoint.setPriority(1);
        mockPoint.setEnabled(1);
        mockPoint.setDescription("主消防疏散通道");
        mockPoint.setCreateTime(LocalDateTime.now());
        mockPoint.setUpdateTime(LocalDateTime.now());

        // 初始化新增表单
        mockAddForm = new AccessEvacuationPointAddForm();
        mockAddForm.setPointName("B栋消防出口");
        mockAddForm.setAreaId(1002L);
        mockAddForm.setEvacuationType("FIRE");
        mockAddForm.setDoorIds(Arrays.asList(3004L, 3005L));
        mockAddForm.setPriority(1);
        mockAddForm.setEnabled(1);
        mockAddForm.setDescription("副消防疏散通道");

        // 初始化更新表单
        mockUpdateForm = new AccessEvacuationPointUpdateForm();
        mockUpdateForm.setPointId(1L);
        mockUpdateForm.setPointName("A栋主消防出口(更新)");
        mockUpdateForm.setPriority(2);

        // 初始化查询表单
        mockQueryForm = new AccessEvacuationPointQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
    }

    @Test
    @DisplayName("分页查询疏散点 - 成功")
    void testQueryPage_Success() {
        // Given
        Page<AccessEvacuationPointEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockPoint));
        page.setTotal(1);

        when(evacuationPointDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessEvacuationPointVO> result = evacuationService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("A栋消防出口", result.getList().get(0).getPointName());
        assertEquals("FIRE", result.getList().get(0).getEvacuationType());

        verify(evacuationPointDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询疏散点 - 按疏散类型过滤")
    void testQueryPage_WithEvacuationTypeFilter() {
        // Given
        mockQueryForm.setEvacuationType("FIRE");
        Page<AccessEvacuationPointEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockPoint));
        page.setTotal(1);

        when(evacuationPointDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessEvacuationPointVO> result = evacuationService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(evacuationPointDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询疏散点 - 按区域过滤")
    void testQueryPage_WithAreaFilter() {
        // Given
        mockQueryForm.setAreaId(1001L);
        Page<AccessEvacuationPointEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockPoint));
        page.setTotal(1);

        when(evacuationPointDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessEvacuationPointVO> result = evacuationService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(evacuationPointDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询疏散点 - 成功")
    void testGetById_Success() {
        // Given
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);

        // When
        AccessEvacuationPointVO result = evacuationService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getPointId());
        assertEquals("A栋消防出口", result.getPointName());
        assertEquals("FIRE", result.getEvacuationType());
        assertEquals(1, result.getPriority());
        verify(evacuationPointDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID查询疏散点 - 不存在")
    void testGetById_NotFound() {
        // Given
        when(evacuationPointDao.selectById(999L)).thenReturn(null);

        // When
        AccessEvacuationPointVO result = evacuationService.getById(999L);

        // Then
        assertNull(result);
        verify(evacuationPointDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("新增疏散点 - 成功")
    void testAddPoint_Success() {
        // Given
        doAnswer(invocation -> {
            AccessEvacuationPointEntity entity = invocation.getArgument(0);
            entity.setPointId(1L); // 模拟MyBatis-Plus的ID回填
            return 1;
        }).when(evacuationPointDao).insert(any(AccessEvacuationPointEntity.class));

        // When
        Long pointId = evacuationService.addPoint(mockAddForm);

        // Then
        assertNotNull(pointId);
        assertEquals(1L, pointId);
        verify(evacuationPointDao, times(1)).insert(any(AccessEvacuationPointEntity.class));
    }

    @Test
    @DisplayName("更新疏散点 - 成功")
    void testUpdatePoint_Success() {
        // Given
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);
        when(evacuationPointDao.updateById(any(AccessEvacuationPointEntity.class))).thenReturn(1);

        // When
        Boolean result = evacuationService.updatePoint(1L, mockUpdateForm);

        // Then
        assertTrue(result);
        verify(evacuationPointDao, times(1)).selectById(1L);
        verify(evacuationPointDao, times(1)).updateById(any(AccessEvacuationPointEntity.class));
    }

    @Test
    @DisplayName("更新疏散点 - 疏散点不存在")
    void testUpdatePoint_NotFound() {
        // Given
        when(evacuationPointDao.selectById(999L)).thenReturn(null);

        // When
        Boolean result = evacuationService.updatePoint(999L, mockUpdateForm);

        // Then
        assertFalse(result);
        verify(evacuationPointDao, times(1)).selectById(999L);
        verify(evacuationPointDao, never()).updateById(any(AccessEvacuationPointEntity.class));
    }

    @Test
    @DisplayName("删除疏散点 - 成功")
    void testDeletePoint_Success() {
        // Given
        when(evacuationPointDao.deleteById(1L)).thenReturn(1);

        // When
        Boolean result = evacuationService.deletePoint(1L);

        // Then
        assertTrue(result);
        verify(evacuationPointDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除疏散点 - 失败")
    void testDeletePoint_Failed() {
        // Given
        when(evacuationPointDao.deleteById(1L)).thenReturn(0);

        // When
        Boolean result = evacuationService.deletePoint(1L);

        // Then
        assertFalse(result);
        verify(evacuationPointDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("更新疏散点启用状态 - 成功")
    void testUpdateEnabled_Success() {
        // Given
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);
        when(evacuationPointDao.updateById(any(AccessEvacuationPointEntity.class))).thenReturn(1);

        // When
        Boolean result = evacuationService.updateEnabled(1L, 0);

        // Then
        assertTrue(result);
        assertEquals(0, mockPoint.getEnabled());
        verify(evacuationPointDao, times(1)).selectById(1L);
        verify(evacuationPointDao, times(1)).updateById(any(AccessEvacuationPointEntity.class));
    }

    @Test
    @DisplayName("触发一键疏散 - 成功")
    void testTriggerEvacuation_Success() {
        // Given
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);
        when(gatewayServiceClient.callDeviceCommService(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"code\":200,\"message\":\"success\"}");

        // When
        String result = evacuationService.triggerEvacuation(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("一键疏散已触发"));
        assertTrue(result.contains("成功打开3/3个门"));
        verify(evacuationPointDao, times(1)).selectById(1L);
        verify(gatewayServiceClient, times(3)).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("触发一键疏散 - 疏散点不存在")
    void testTriggerEvacuation_NotFound() {
        // Given
        when(evacuationPointDao.selectById(999L)).thenReturn(null);

        // When
        String result = evacuationService.triggerEvacuation(999L);

        // Then
        assertNotNull(result);
        assertEquals("疏散点不存在", result);
        verify(evacuationPointDao, times(1)).selectById(999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("触发一键疏散 - 疏散点未启用")
    void testTriggerEvacuation_NotEnabled() {
        // Given
        mockPoint.setEnabled(0);
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);

        // When
        String result = evacuationService.triggerEvacuation(1L);

        // Then
        assertNotNull(result);
        assertEquals("疏散点未启用", result);
        verify(evacuationPointDao, times(1)).selectById(1L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("取消疏散 - 成功")
    void testCancelEvacuation_Success() {
        // Given
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);
        when(gatewayServiceClient.callDeviceCommService(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"code\":200,\"message\":\"success\"}");

        // When
        String result = evacuationService.cancelEvac(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("疏散已取消"));
        assertTrue(result.contains("成功关闭3/3个门"));
        verify(evacuationPointDao, times(1)).selectById(1L);
        verify(gatewayServiceClient, times(3)).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("取消疏散 - 疏散点不存在")
    void testCancelEvacuation_NotFound() {
        // Given
        when(evacuationPointDao.selectById(999L)).thenReturn(null);

        // When
        String result = evacuationService.cancelEvac(999L);

        // Then
        assertNotNull(result);
        assertEquals("疏散点不存在", result);
        verify(evacuationPointDao, times(1)).selectById(999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("测试疏散点 - 成功")
    void testTestPoint_Success() {
        // Given
        when(evacuationPointDao.selectById(1L)).thenReturn(mockPoint);

        // When
        String result = evacuationService.testPoint(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("测试成功"));
        assertTrue(result.contains("A栋消防出口"));
        assertTrue(result.contains("火灾疏散"));
        assertTrue(result.contains("关联门: 3个"));
        verify(evacuationPointDao, times(1)).selectById(1L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("测试疏散点 - 疏散点不存在")
    void testTestPoint_NotFound() {
        // Given
        when(evacuationPointDao.selectById(999L)).thenReturn(null);

        // When
        String result = evacuationService.testPoint(999L);

        // Then
        assertNotNull(result);
        assertEquals("疏散点不存在", result);
        verify(evacuationPointDao, times(1)).selectById(999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }
}

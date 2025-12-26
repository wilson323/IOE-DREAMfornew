package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.dao.AccessCapacityControlDao;
import net.lab1024.sa.access.domain.form.AccessCapacityControlAddForm;
import net.lab1024.sa.access.domain.form.AccessCapacityControlQueryForm;
import net.lab1024.sa.access.domain.form.AccessCapacityControlUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessCapacityControlVO;
import net.lab1024.sa.common.entity.access.AccessCapacityControlEntity;
import net.lab1024.sa.access.service.impl.AccessCapacityServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁容量控制Service单元测试
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("门禁容量控制Service单元测试")
class AccessCapacityServiceTest {

    @Mock
    private AccessCapacityControlDao capacityControlDao;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AccessCapacityServiceImpl capacityService;

    private AccessCapacityControlEntity mockControl;
    private AccessCapacityControlAddForm mockAddForm;
    private AccessCapacityControlUpdateForm mockUpdateForm;
    private AccessCapacityControlQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化Redis mock
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // 初始化测试数据
        mockControl = new AccessCapacityControlEntity();
        mockControl.setControlId(1L);
        mockControl.setAreaId(1001L);
        mockControl.setMaxCapacity(100);
        mockControl.setCurrentCount(50);
        mockControl.setControlMode("STRICT");
        mockControl.setAlertThreshold(80);
        mockControl.setEntryBlocked(0);
        mockControl.setEnabled(1);
        mockControl.setDescription("会议室A容量控制");
        mockControl.setCreateTime(LocalDateTime.now());
        mockControl.setUpdateTime(LocalDateTime.now());

        // 初始化新增表单
        mockAddForm = new AccessCapacityControlAddForm();
        mockAddForm.setAreaId(1002L);
        mockAddForm.setMaxCapacity(50);
        mockAddForm.setControlMode("WARNING");
        mockAddForm.setAlertThreshold(90);
        mockAddForm.setEnabled(1);
        mockAddForm.setDescription("会议室B容量控制");

        // 初始化更新表单
        mockUpdateForm = new AccessCapacityControlUpdateForm();
        mockUpdateForm.setControlId(1L);
        mockUpdateForm.setMaxCapacity(150);
        mockUpdateForm.setAlertThreshold(85);

        // 初始化查询表单
        mockQueryForm = new AccessCapacityControlQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
    }

    @Test
    @DisplayName("分页查询容量控制规则 - 成功")
    void testQueryPage_Success() {
        // Given
        Page<AccessCapacityControlEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockControl));
        page.setTotal(1);

        when(capacityControlDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessCapacityControlVO> result = capacityService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1001L, result.getList().get(0).getAreaId());
        assertEquals(100, result.getList().get(0).getMaxCapacity());
        assertEquals(50, result.getList().get(0).getCurrentCount());

        verify(capacityControlDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询容量控制规则 - 按控制模式过滤")
    void testQueryPage_WithControlModeFilter() {
        // Given
        mockQueryForm.setControlMode("STRICT");
        Page<AccessCapacityControlEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockControl));
        page.setTotal(1);

        when(capacityControlDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessCapacityControlVO> result = capacityService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(capacityControlDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询容量控制规则 - 成功")
    void testGetById_Success() {
        // Given
        when(capacityControlDao.selectById(1L)).thenReturn(mockControl);

        // When
        AccessCapacityControlVO result = capacityService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getControlId());
        assertEquals(1001L, result.getAreaId());
        assertEquals(100, result.getMaxCapacity());
        assertEquals(50, result.getCurrentCount());
        assertEquals("STRICT", result.getControlMode());
        verify(capacityControlDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID查询容量控制规则 - 不存在")
    void testGetById_NotFound() {
        // Given
        when(capacityControlDao.selectById(999L)).thenReturn(null);

        // When
        AccessCapacityControlVO result = capacityService.getById(999L);

        // Then
        assertNull(result);
        verify(capacityControlDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("新增容量控制规则 - 成功")
    void testAddControl_Success() {
        // Given
        doAnswer(invocation -> {
            AccessCapacityControlEntity entity = invocation.getArgument(0);
            entity.setControlId(1L); // 模拟MyBatis-Plus的ID回填
            return 1;
        }).when(capacityControlDao).insert(any(AccessCapacityControlEntity.class));

        // When
        Long controlId = capacityService.addControl(mockAddForm);

        // Then
        assertNotNull(controlId);
        assertEquals(1L, controlId);
        verify(capacityControlDao, times(1)).insert(any(AccessCapacityControlEntity.class));
        verify(valueOperations, times(1)).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("更新容量控制规则 - 成功")
    void testUpdateControl_Success() {
        // Given
        when(capacityControlDao.selectById(1L)).thenReturn(mockControl);
        when(capacityControlDao.updateById(any(AccessCapacityControlEntity.class))).thenReturn(1);

        // When
        Boolean result = capacityService.updateControl(1L, mockUpdateForm);

        // Then
        assertTrue(result);
        verify(capacityControlDao, times(1)).selectById(1L);
        verify(capacityControlDao, times(1)).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("更新容量控制规则 - 规则不存在")
    void testUpdateControl_NotFound() {
        // Given
        when(capacityControlDao.selectById(999L)).thenReturn(null);

        // When
        Boolean result = capacityService.updateControl(999L, mockUpdateForm);

        // Then
        assertFalse(result);
        verify(capacityControlDao, times(1)).selectById(999L);
        verify(capacityControlDao, never()).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("删除容量控制规则 - 成功")
    void testDeleteControl_Success() {
        // Given
        when(capacityControlDao.selectById(1L)).thenReturn(mockControl);
        when(capacityControlDao.deleteById(1L)).thenReturn(1);

        // When
        Boolean result = capacityService.deleteControl(1L);

        // Then
        assertTrue(result);
        verify(capacityControlDao, times(1)).selectById(1L);
        verify(capacityControlDao, times(1)).deleteById(1L);
        verify(stringRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("更新容量控制启用状态 - 成功")
    void testUpdateEnabled_Success() {
        // Given
        when(capacityControlDao.selectById(1L)).thenReturn(mockControl);
        when(capacityControlDao.updateById(any(AccessCapacityControlEntity.class))).thenReturn(1);

        // When
        Boolean result = capacityService.updateEnabled(1L, 0);

        // Then
        assertTrue(result);
        assertEquals(0, mockControl.getEnabled());
        verify(capacityControlDao, times(1)).selectById(1L);
        verify(capacityControlDao, times(1)).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("增加区域内人数 - 成功")
    void testIncrementCount_Success() {
        // Given
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("50");
        when(capacityControlDao.updateById(any(AccessCapacityControlEntity.class))).thenReturn(1);

        // When
        String result = capacityService.incrementCount(1001L, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("人数增加成功"));
        assertTrue(result.contains("60"));
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
        verify(valueOperations, times(1)).set(anyString(), eq("60"), anyLong(), any());
        verify(capacityControlDao, times(1)).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("增加区域内人数 - 严格模式超过最大容量")
    void testIncrementCount_ExceedCapacity_StrictMode() {
        // Given
        mockControl.setCurrentCount(95);
        mockControl.setControlMode("STRICT");
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("95");

        // When
        String result = capacityService.incrementCount(1001L, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("超过最大容量"));
        assertTrue(result.contains("禁止进入"));
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
        verify(capacityControlDao, never()).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("增加区域内人数 - 警告模式超过最大容量")
    void testIncrementCount_ExceedCapacity_WarningMode() {
        // Given
        mockControl.setCurrentCount(95);
        mockControl.setControlMode("WARNING");
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("95");
        when(capacityControlDao.updateById(any(AccessCapacityControlEntity.class))).thenReturn(1);

        // When
        String result = capacityService.incrementCount(1001L, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("警告"));
        assertTrue(result.contains("超过最大容量"));
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
        verify(valueOperations, times(1)).set(anyString(), eq("105"), anyLong(), any());
        verify(capacityControlDao, times(1)).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("减少区域内人数 - 成功")
    void testDecrementCount_Success() {
        // Given
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("60");
        when(capacityControlDao.updateById(any(AccessCapacityControlEntity.class))).thenReturn(1);

        // When
        String result = capacityService.decrementCount(1001L, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("人数减少成功"));
        assertTrue(result.contains("50"));
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).set(anyString(), eq("50"), anyLong(), any());
        verify(capacityControlDao, times(1)).updateById(any(AccessCapacityControlEntity.class));
    }

    @Test
    @DisplayName("重置区域内人数 - 成功")
    void testResetCount_Success() {
        // Given
        when(capacityControlDao.selectById(1L)).thenReturn(mockControl);
        when(capacityControlDao.updateById(any(AccessCapacityControlEntity.class))).thenReturn(1);

        // When
        String result = capacityService.resetCount(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("人数已重置"));
        assertTrue(result.contains("0"));
        assertEquals(0, mockControl.getCurrentCount());
        assertEquals(0, mockControl.getEntryBlocked());
        verify(capacityControlDao, times(1)).selectById(1L);
        verify(capacityControlDao, times(1)).updateById(any(AccessCapacityControlEntity.class));
        verify(stringRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("获取区域当前人数 - 成功")
    void testGetCurrentCount_Success() {
        // Given
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("50");

        // When
        Integer count = capacityService.getCurrentCount(1001L);

        // Then
        assertNotNull(count);
        assertEquals(50, count);
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("获取区域当前人数 - 从数据库获取")
    void testGetCurrentCount_FromDatabase() {
        // Given
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        Integer count = capacityService.getCurrentCount(1001L);

        // Then
        assertNotNull(count);
        assertEquals(50, count);
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("检查是否允许进入 - 允许")
    void testCheckEntryAllowed_Allowed() {
        // Given
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("50");

        // When
        Boolean allowed = capacityService.checkEntryAllowed(1001L);

        // Then
        assertTrue(allowed);
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("检查是否允许进入 - 禁止")
    void testCheckEntryAllowed_NotAllowed() {
        // Given
        mockControl.setCurrentCount(100);
        when(capacityControlDao.selectOne(any())).thenReturn(mockControl);
        when(valueOperations.get(anyString())).thenReturn("100");

        // When
        Boolean allowed = capacityService.checkEntryAllowed(1001L);

        // Then
        assertFalse(allowed);
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, times(1)).get(anyString());
    }

    @Test
    @DisplayName("检查是否允许进入 - 未配置控制规则")
    void testCheckEntryAllowed_NoControl() {
        // Given
        when(capacityControlDao.selectOne(any())).thenReturn(null);

        // When
        Boolean allowed = capacityService.checkEntryAllowed(9999L);

        // Then
        assertTrue(allowed);
        verify(capacityControlDao, times(1)).selectOne(any());
        verify(valueOperations, never()).get(anyString());
    }
}

package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.access.dao.AccessInterlockRuleDao;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessInterlockRuleVO;
import net.lab1024.sa.common.entity.access.AccessInterlockRuleEntity;
import net.lab1024.sa.access.service.impl.AccessInterlockServiceImpl;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.domain.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁全局互锁Service单元测试
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁全局互锁Service单元测试")
class AccessInterlockServiceTest {

    @Mock
    private AccessInterlockRuleDao interlockRuleDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccessInterlockServiceImpl interlockService;

    private AccessInterlockRuleEntity mockRule;
    private AccessInterlockRuleAddForm mockAddForm;
    private AccessInterlockRuleUpdateForm mockUpdateForm;
    private AccessInterlockRuleQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockRule = new AccessInterlockRuleEntity();
        mockRule.setRuleId(1L);
        mockRule.setRuleName("测试互锁规则");
        mockRule.setAreaAId(1001L);
        mockRule.setAreaBId(1002L);
        mockRule.setAreaADoorIds("[3001, 3002]");
        mockRule.setAreaBDoorIds("[3003, 3004]");
        mockRule.setInterlockMode("BIDIRECTIONAL");
        mockRule.setUnlockCondition("MANUAL");
        mockRule.setUnlockDelaySeconds(0);
        mockRule.setEnabled(1);
        mockRule.setPriority(100);
        mockRule.setCreateTime(LocalDateTime.now());

        // 初始化新增表单
        mockAddForm = new AccessInterlockRuleAddForm();
        mockAddForm.setRuleName("测试互锁规则");
        mockAddForm.setAreaAId(1001L);
        mockAddForm.setAreaBId(1002L);
        mockAddForm.setAreaADoorIds("[3001, 3002]");
        mockAddForm.setAreaBDoorIds("[3003, 3004]");
        mockAddForm.setInterlockMode("BIDIRECTIONAL");
        mockAddForm.setUnlockCondition("MANUAL");
        mockAddForm.setUnlockDelaySeconds(0);
        mockAddForm.setPriority(100);

        // 初始化更新表单
        mockUpdateForm = new AccessInterlockRuleUpdateForm();
        mockUpdateForm.setRuleName("更新后的互锁规则");
        mockUpdateForm.setInterlockMode("UNIDIRECTIONAL");

        // 初始化查询表单
        mockQueryForm = new AccessInterlockRuleQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
    }

    @Test
    @DisplayName("分页查询互锁规则 - 成功")
    void testQueryPage_Success() {
        // Given
        Page<AccessInterlockRuleEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockRule));
        page.setTotal(1);

        when(interlockRuleDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessInterlockRuleVO> result = interlockService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("测试互锁规则", result.getList().get(0).getRuleName());
        assertEquals("BIDIRECTIONAL", result.getList().get(0).getInterlockMode());

        verify(interlockRuleDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询互锁规则 - 带互锁模式过滤")
    void testQueryPage_WithInterlockModeFilter() {
        // Given
        mockQueryForm.setInterlockMode("BIDIRECTIONAL");
        Page<AccessInterlockRuleEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockRule));
        page.setTotal(1);

        when(interlockRuleDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessInterlockRuleVO> result = interlockService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(interlockRuleDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询规则 - 成功")
    void testGetById_Success() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);

        // When
        AccessInterlockRuleVO result = interlockService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRuleId());
        assertEquals("测试互锁规则", result.getRuleName());
        assertEquals("BIDIRECTIONAL", result.getInterlockMode());
        verify(interlockRuleDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID查询规则 - 不存在")
    void testGetById_NotFound() {
        // Given
        when(interlockRuleDao.selectById(999L)).thenReturn(null);

        // When
        AccessInterlockRuleVO result = interlockService.getById(999L);

        // Then
        assertNull(result);
        verify(interlockRuleDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("新增互锁规则 - 成功")
    void testAddRule_Success() {
        // Given
        doAnswer(invocation -> {
            AccessInterlockRuleEntity entity = invocation.getArgument(0);
            entity.setRuleId(1L); // 模拟MyBatis-Plus的ID回填
            return 1;
        }).when(interlockRuleDao).insert(any(AccessInterlockRuleEntity.class));

        // When
        Long ruleId = interlockService.addRule(mockAddForm);

        // Then
        assertNotNull(ruleId);
        assertEquals(1L, ruleId);
        verify(interlockRuleDao, times(1)).insert(any(AccessInterlockRuleEntity.class));
    }

    @Test
    @DisplayName("更新互锁规则 - 成功")
    void testUpdateRule_Success() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);
        when(interlockRuleDao.updateById(any(AccessInterlockRuleEntity.class))).thenReturn(1);

        // When
        Boolean result = interlockService.updateRule(1L, mockUpdateForm);

        // Then
        assertTrue(result);
        verify(interlockRuleDao, times(1)).selectById(1L);
        verify(interlockRuleDao, times(1)).updateById(any(AccessInterlockRuleEntity.class));
    }

    @Test
    @DisplayName("更新互锁规则 - 规则不存在")
    void testUpdateRule_NotFound() {
        // Given
        when(interlockRuleDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            interlockService.updateRule(999L, mockUpdateForm);
        });
        verify(interlockRuleDao, times(1)).selectById(999L);
        verify(interlockRuleDao, never()).updateById(any(AccessInterlockRuleEntity.class));
    }

    @Test
    @DisplayName("删除互锁规则 - 成功")
    void testDeleteRule_Success() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);
        when(interlockRuleDao.deleteById(1L)).thenReturn(1);

        // When
        Boolean result = interlockService.deleteRule(1L);

        // Then
        assertTrue(result);
        verify(interlockRuleDao, times(1)).selectById(1L);
        verify(interlockRuleDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除互锁规则 - 失败")
    void testDeleteRule_Failed() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);
        when(interlockRuleDao.deleteById(1L)).thenReturn(0);

        // When
        Boolean result = interlockService.deleteRule(1L);

        // Then
        assertFalse(result);
        verify(interlockRuleDao, times(1)).selectById(1L);
        verify(interlockRuleDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("更新规则启用状态 - 成功")
    void testUpdateEnabled_Success() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);
        when(interlockRuleDao.updateById(any(AccessInterlockRuleEntity.class))).thenReturn(1);

        // When
        Boolean result = interlockService.updateEnabled(1L, 0);

        // Then
        assertTrue(result);
        assertEquals(0, mockRule.getEnabled());
        verify(interlockRuleDao, times(1)).selectById(1L);
        verify(interlockRuleDao, times(1)).updateById(any(AccessInterlockRuleEntity.class));
    }

    @Test
    @DisplayName("触发双向互锁 - 成功")
    void testTriggerInterlock_Bidirectional_Success() {
        // Given
        mockRule.setInterlockMode("BIDIRECTIONAL");
        when(interlockRuleDao.selectEnabledRulesByArea(1001L)).thenReturn(Arrays.asList(mockRule));
        // 注意：异步执行不需要stub gatewayServiceClient

        // When
        String result = interlockService.triggerInterlock(1001L, 3001L, "DOOR_OPEN");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("已触发"));
        assertEquals(1, Integer.parseInt(result.replaceAll("\\D+", "")));
        verify(interlockRuleDao, times(1)).selectEnabledRulesByArea(1001L);
        // 注意：gatewayServiceClient在异步线程中调用，此处不验证
    }

    @Test
    @DisplayName("触发单向互锁 - 成功")
    void testTriggerInterlock_Unidirectional_Success() {
        // Given
        mockRule.setInterlockMode("UNIDIRECTIONAL");
        when(interlockRuleDao.selectEnabledRulesByArea(1001L)).thenReturn(Arrays.asList(mockRule));
        // 注意：异步执行不需要stub gatewayServiceClient

        // When
        String result = interlockService.triggerInterlock(1001L, 3001L, "DOOR_OPEN");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("已触发"));
        verify(interlockRuleDao, times(1)).selectEnabledRulesByArea(1001L);
        // 注意：gatewayServiceClient在异步线程中调用，此处不验证
    }

    @Test
    @DisplayName("触发互锁 - 无匹配规则")
    void testTriggerInterlock_NoMatchingRule() {
        // Given
        when(interlockRuleDao.selectEnabledRulesByArea(9999L)).thenReturn(Arrays.asList());

        // When
        String result = interlockService.triggerInterlock(9999L, 9999L, "DOOR_OPEN");

        // Then
        assertNotNull(result);
        assertEquals("未找到匹配的互锁规则", result);
        verify(interlockRuleDao, times(1)).selectEnabledRulesByArea(9999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("手动解锁 - 成功")
    void testManualUnlock_Success() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);
        when(gatewayServiceClient.callDeviceCommService(anyString(), any(), any(), eq(String.class))).thenReturn("unlock success");

        // When
        Boolean result = interlockService.manualUnlock(1L, 1002L);

        // Then
        assertTrue(result);
        verify(interlockRuleDao, times(1)).selectById(1L);
        verify(gatewayServiceClient, atLeastOnce()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("手动解锁 - 无匹配规则")
    void testManualUnlock_NoMatchingRule() {
        // Given
        when(interlockRuleDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            interlockService.manualUnlock(999L, 9998L);
        });
        verify(interlockRuleDao, times(1)).selectById(999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("测试互锁规则 - 成功")
    void testTestRule_Success() {
        // Given
        when(interlockRuleDao.selectById(1L)).thenReturn(mockRule);

        // When
        String result = interlockService.testRule(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("测试成功"));
        assertTrue(result.contains("已模拟锁定区域B"));
        verify(interlockRuleDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试互锁规则 - 规则不存在")
    void testTestRule_NotFound() {
        // Given
        when(interlockRuleDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            interlockService.testRule(999L);
        });
        verify(interlockRuleDao, times(1)).selectById(999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }
}

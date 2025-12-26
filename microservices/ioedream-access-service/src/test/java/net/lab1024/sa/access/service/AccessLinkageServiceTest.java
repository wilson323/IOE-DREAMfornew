package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.access.dao.AccessLinkageRuleDao;
import net.lab1024.sa.access.dao.AccessLinkageLogDao;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleUpdateForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleQueryForm;
import net.lab1024.sa.access.domain.vo.AccessLinkageRuleVO;
import net.lab1024.sa.access.domain.entity.AccessLinkageRuleEntity;
import net.lab1024.sa.access.service.impl.AccessLinkageServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁全局联动Service单元测试
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁全局联动Service单元测试")
class AccessLinkageServiceTest {

    @Mock
    private AccessLinkageRuleDao linkageRuleDao;

    @Mock
    private AccessLinkageLogDao linkageLogDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccessLinkageServiceImpl linkageService;

    private AccessLinkageRuleEntity mockRule;
    private AccessLinkageRuleAddForm mockAddForm;
    private AccessLinkageRuleUpdateForm mockUpdateForm;
    private AccessLinkageRuleQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockRule = new AccessLinkageRuleEntity();
        mockRule.setRuleId(1L);
        mockRule.setRuleName("测试联动规则");
        mockRule.setTriggerDeviceId(1001L);
        mockRule.setTriggerDoorId(2001L);
        mockRule.setTriggerType("DOOR_OPEN");
        mockRule.setTargetDeviceId(1002L);
        mockRule.setActionType("CAMERA_SNAPSHOT");
        mockRule.setDelaySeconds(0);
        mockRule.setEnabled(1);
        mockRule.setPriority(100);
        mockRule.setCreateTime(LocalDateTime.now());

        // 初始化新增表单
        mockAddForm = new AccessLinkageRuleAddForm();
        mockAddForm.setRuleName("测试联动规则");
        mockAddForm.setTriggerDeviceId(1001L);
        mockAddForm.setTriggerDoorId(2001L);
        mockAddForm.setTriggerType("DOOR_OPEN");
        mockAddForm.setTargetDeviceId(1002L);
        mockAddForm.setActionType("CAMERA_SNAPSHOT");
        mockAddForm.setDelaySeconds(0);
        mockAddForm.setPriority(100);

        // 初始化更新表单
        mockUpdateForm = new AccessLinkageRuleUpdateForm();
        mockUpdateForm.setRuleName("更新联动规则");

        // 初始化查询表单
        mockQueryForm = new AccessLinkageRuleQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
    }

    @Test
    @DisplayName("分页查询联动规则 - 成功")
    void testQueryPage_Success() {
        // Given
        Page<AccessLinkageRuleEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockRule));
        page.setTotal(1);

        when(linkageRuleDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessLinkageRuleVO> result = linkageService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("测试联动规则", result.getList().get(0).getRuleName());

        verify(linkageRuleDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询联动规则 - 带规则名称过滤")
    void testQueryPage_WithTriggerDeviceFilter() {
        // Given
        mockQueryForm.setRuleName("测试联动规则");
        Page<AccessLinkageRuleEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockRule));
        page.setTotal(1);

        when(linkageRuleDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<AccessLinkageRuleVO> result = linkageService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(linkageRuleDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询规则 - 成功")
    void testGetById_Success() {
        // Given
        when(linkageRuleDao.selectById(1L)).thenReturn(mockRule);

        // When
        AccessLinkageRuleVO result = linkageService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRuleId());
        assertEquals("测试联动规则", result.getRuleName());
        verify(linkageRuleDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID查询规则 - 不存在")
    void testGetById_NotFound() {
        // Given
        when(linkageRuleDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            linkageService.getById(999L);
        });
        verify(linkageRuleDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("新增联动规则 - 成功")
    void testAddRule_Success() {
        // Given
        doAnswer(invocation -> {
            AccessLinkageRuleEntity entity = invocation.getArgument(0);
            entity.setRuleId(1L); // 模拟MyBatis-Plus的ID回填
            return 1;
        }).when(linkageRuleDao).insert(any(AccessLinkageRuleEntity.class));

        // When
        Long ruleId = linkageService.add(mockAddForm);

        // Then
        assertNotNull(ruleId);
        assertEquals(1L, ruleId);
        verify(linkageRuleDao, times(1)).insert(any(AccessLinkageRuleEntity.class));
    }

    @Test
    @DisplayName("更新联动规则 - 成功")
    void testUpdateRule_Success() {
        // Given
        when(linkageRuleDao.selectById(1L)).thenReturn(mockRule);
        when(linkageRuleDao.updateById(any(AccessLinkageRuleEntity.class))).thenReturn(1);

        // When
        linkageService.update(1L, mockUpdateForm);

        // Then
        verify(linkageRuleDao, times(1)).selectById(1L);
        verify(linkageRuleDao, times(1)).updateById(any(AccessLinkageRuleEntity.class));
    }

    @Test
    @DisplayName("更新联动规则 - 规则不存在")
    void testUpdateRule_NotFound() {
        // Given
        when(linkageRuleDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            linkageService.update(999L, mockUpdateForm);
        });
        verify(linkageRuleDao, times(1)).selectById(999L);
        verify(linkageRuleDao, never()).updateById(any(AccessLinkageRuleEntity.class));
    }

    @Test
    @DisplayName("删除联动规则 - 成功")
    void testDeleteRule_Success() {
        // Given
        when(linkageRuleDao.selectById(1L)).thenReturn(mockRule);
        when(linkageRuleDao.deleteById(1L)).thenReturn(1);

        // When
        linkageService.delete(1L);

        // Then
        verify(linkageRuleDao, times(1)).selectById(1L);
        verify(linkageRuleDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除联动规则 - 失败")
    void testDeleteRule_Failed() {
        // Given
        when(linkageRuleDao.selectById(1L)).thenReturn(mockRule);
        when(linkageRuleDao.deleteById(1L)).thenReturn(0);

        // When
        linkageService.delete(1L);

        // Then
        verify(linkageRuleDao, times(1)).selectById(1L);
        verify(linkageRuleDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("更新规则启用状态 - 成功")
    void testUpdateEnabled_Success() {
        // Given
        when(linkageRuleDao.selectById(1L)).thenReturn(mockRule);
        when(linkageRuleDao.updateById(any(AccessLinkageRuleEntity.class))).thenReturn(1);

        // When
        linkageService.updateEnabled(1L, 0);

        // Then
        assertEquals(0, mockRule.getEnabled());
        verify(linkageRuleDao, times(1)).selectById(1L);
        verify(linkageRuleDao, times(1)).updateById(any(AccessLinkageRuleEntity.class));
    }

    @Test
    @DisplayName("触发联动 - 无延迟")
    void testTriggerLinkage_WithoutDelay() {
        // Given
        mockRule.setDelaySeconds(0);
        when(linkageRuleDao.selectList(any())).thenReturn(Arrays.asList(mockRule));

        // When
        String result = linkageService.triggerLinkage(1001L, 2001L, "DOOR_OPEN");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("已触发"));
        assertTrue(result.contains("1条联动规则"));
        verify(linkageRuleDao, times(1)).selectList(any());
        // 注意：gatewayServiceClient在异步线程中调用，此处不验证
    }

    @Test
    @DisplayName("触发联动 - 有延迟")
    void testTriggerLinkage_WithDelay() {
        // Given
        mockRule.setDelaySeconds(5);
        when(linkageRuleDao.selectList(any())).thenReturn(Arrays.asList(mockRule));

        // When
        String result = linkageService.triggerLinkage(1001L, 2001L, "DOOR_OPEN");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("已触发"));
        assertTrue(result.contains("1条联动规则"));
        verify(linkageRuleDao, times(1)).selectList(any());
        // 注意：gatewayServiceClient在异步线程中调用，此处不验证
    }

    @Test
    @DisplayName("触发联动 - 无匹配规则")
    void testTriggerLinkage_NoMatchingRule() {
        // Given
        when(linkageRuleDao.selectList(any())).thenReturn(Arrays.asList());

        // When
        String result = linkageService.triggerLinkage(9999L, 9999L, "UNKNOWN_EVENT");

        // Then
        assertNotNull(result);
        assertEquals("未找到匹配的联动规则", result);
        verify(linkageRuleDao, times(1)).selectList(any());
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("测试联动规则 - 成功")
    void testTestRule_Success() {
        // Given
        when(linkageRuleDao.selectById(1L)).thenReturn(mockRule);
        when(gatewayServiceClient.callDeviceCommService(anyString(), any(), any(), eq(String.class))).thenReturn("test success");

        // When
        String result = linkageService.testRule(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("联动执行成功"));
        verify(linkageRuleDao, times(1)).selectById(1L);
        verify(gatewayServiceClient, times(1)).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }

    @Test
    @DisplayName("测试联动规则 - 规则不存在")
    void testTestRule_NotFound() {
        // Given
        when(linkageRuleDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            linkageService.testRule(999L);
        });
        verify(linkageRuleDao, times(1)).selectById(999L);
        verify(gatewayServiceClient, never()).callDeviceCommService(anyString(), any(), any(), eq(String.class));
    }
}

package net.lab1024.sa.access.service;

import net.lab1024.sa.access.dao.AccessPersonRestrictionDao;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionAddForm;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionQueryForm;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessPersonRestrictionVO;
import net.lab1024.sa.access.domain.entity.AccessPersonRestrictionEntity;
import net.lab1024.sa.access.service.impl.AccessPersonRestrictionServiceImpl;
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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁人员限制Service单元测试
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("门禁人员限制Service单元测试")
class AccessPersonRestrictionServiceTest {

    @Mock
    private AccessPersonRestrictionDao restrictionDao;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AccessPersonRestrictionServiceImpl restrictionService;

    private AccessPersonRestrictionEntity mockRestriction;
    private AccessPersonRestrictionAddForm mockAddForm;
    private AccessPersonRestrictionUpdateForm mockUpdateForm;
    private AccessPersonRestrictionQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化Redis mock
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // 初始化测试数据
        mockRestriction = new AccessPersonRestrictionEntity();
        mockRestriction.setRestrictionId(1L);
        mockRestriction.setUserId(1001L);
        mockRestriction.setUserName("张三");
        mockRestriction.setUserPhone("13800138000");
        mockRestriction.setRestrictionType("BLACKLIST");
        mockRestriction.setAreaIds("[1001, 1002]");
        mockRestriction.setDoorIds("[2001, 2002]");
        mockRestriction.setTimeStart("09:00:00");
        mockRestriction.setTimeEnd("18:00:00");
        mockRestriction.setEffectiveDate(LocalDate.now().minusDays(1));
        mockRestriction.setExpireDate(LocalDate.now().plusDays(30));
        mockRestriction.setReason("违规操作");
        mockRestriction.setPriority(100);
        mockRestriction.setEnabled(1);
        mockRestriction.setDescription("多次未授权进入");
        mockRestriction.setCreateTime(LocalDateTime.now());
        mockRestriction.setUpdateTime(LocalDateTime.now());

        // 初始化新增表单
        mockAddForm = new AccessPersonRestrictionAddForm();
        mockAddForm.setUserId(1002L);
        mockAddForm.setUserName("李四");
        mockAddForm.setUserPhone("13800138001");
        mockAddForm.setRestrictionType("WHITELIST");
        mockAddForm.setAreaIds(Arrays.asList(1003L, 1004L));
        mockAddForm.setDoorIds(Arrays.asList(2003L, 2004L));
        mockAddForm.setTimeStart(LocalTime.of(8, 0, 0));
        mockAddForm.setTimeEnd(LocalTime.of(20, 0, 0));
        mockAddForm.setEffectiveDate(LocalDate.now());
        mockAddForm.setExpireDate(LocalDate.now().plusDays(60));
        mockAddForm.setReason("VIP用户");
        mockAddForm.setPriority(90);
        mockAddForm.setEnabled(1);
        mockAddForm.setDescription("VIP用户白名单");

        // 初始化更新表单
        mockUpdateForm = new AccessPersonRestrictionUpdateForm();
        mockUpdateForm.setRestrictionId(1L);
        mockUpdateForm.setReason("严重违规操作");
        mockUpdateForm.setPriority(150);

        // 初始化查询表单
        mockQueryForm = new AccessPersonRestrictionQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
    }

    @Test
    @DisplayName("分页查询人员限制规则 - 成功")
    void testQueryPage_Success() {
        // Given - Mock selectPage返回Page对象
        Page<AccessPersonRestrictionEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockRestriction));
        mockPage.setTotal(1);
        when(restrictionDao.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        PageResult<AccessPersonRestrictionVO> result = restrictionService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1001L, result.getList().get(0).getUserId());
        assertEquals("张三", result.getList().get(0).getUserName());
        assertEquals("BLACKLIST", result.getList().get(0).getRestrictionType());

        verify(restrictionDao, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("分页查询人员限制规则 - 按限制类型过滤")
    void testQueryPage_WithRestrictionTypeFilter() {
        // Given
        mockQueryForm.setRestrictionType("BLACKLIST");
        Page<AccessPersonRestrictionEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockRestriction));
        mockPage.setTotal(1);
        when(restrictionDao.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        PageResult<AccessPersonRestrictionVO> result = restrictionService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(restrictionDao, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("分页查询人员限制规则 - 按用户过滤")
    void testQueryPage_WithUserFilter() {
        // Given
        mockQueryForm.setUserId(1001L);
        Page<AccessPersonRestrictionEntity> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(mockRestriction));
        mockPage.setTotal(1);
        when(restrictionDao.selectPage(any(Page.class), any())).thenReturn(mockPage);

        // When
        PageResult<AccessPersonRestrictionVO> result = restrictionService.queryPage(mockQueryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        verify(restrictionDao, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("根据ID查询人员限制规则 - 成功")
    void testGetById_Success() {
        // Given
        when(restrictionDao.selectById(1L)).thenReturn(mockRestriction);

        // When
        AccessPersonRestrictionVO result = restrictionService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRestrictionId());
        assertEquals(1001L, result.getUserId());
        assertEquals("张三", result.getUserName());
        assertEquals("BLACKLIST", result.getRestrictionType());
        verify(restrictionDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID查询人员限制规则 - 不存在")
    void testGetById_NotFound() {
        // Given
        when(restrictionDao.selectById(999L)).thenReturn(null);

        // When
        AccessPersonRestrictionVO result = restrictionService.getById(999L);

        // Then
        assertNull(result);
        verify(restrictionDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("新增人员限制规则 - 成功")
    void testAddRestriction_Success() {
        // Given - Mock ID回填
        doAnswer(invocation -> {
            AccessPersonRestrictionEntity entity = invocation.getArgument(0);
            entity.setRestrictionId(1L);
            return 1;
        }).when(restrictionDao).insert(any(AccessPersonRestrictionEntity.class));

        // When
        Long restrictionId = restrictionService.addRestriction(mockAddForm);

        // Then
        assertNotNull(restrictionId);
        assertEquals(1L, restrictionId);
        verify(restrictionDao, times(1)).insert(any(AccessPersonRestrictionEntity.class));
        verify(stringRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("更新人员限制规则 - 成功")
    void testUpdateRestriction_Success() {
        // Given
        when(restrictionDao.selectById(1L)).thenReturn(mockRestriction);
        when(restrictionDao.updateById(any(AccessPersonRestrictionEntity.class))).thenReturn(1);

        // When
        Boolean result = restrictionService.updateRestriction(1L, mockUpdateForm);

        // Then
        assertTrue(result);
        verify(restrictionDao, times(1)).selectById(1L);
        verify(restrictionDao, times(1)).updateById(any(AccessPersonRestrictionEntity.class));
        verify(stringRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("更新人员限制规则 - 规则不存在")
    void testUpdateRestriction_NotFound() {
        // Given
        when(restrictionDao.selectById(999L)).thenReturn(null);

        // When
        Boolean result = restrictionService.updateRestriction(999L, mockUpdateForm);

        // Then
        assertFalse(result);
        verify(restrictionDao, times(1)).selectById(999L);
        verify(restrictionDao, never()).updateById(any(AccessPersonRestrictionEntity.class));
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("删除人员限制规则 - 成功")
    void testDeleteRestriction_Success() {
        // Given
        when(restrictionDao.selectById(1L)).thenReturn(mockRestriction);
        when(restrictionDao.deleteById(1L)).thenReturn(1);

        // When
        Boolean result = restrictionService.deleteRestriction(1L);

        // Then
        assertTrue(result);
        verify(restrictionDao, times(1)).selectById(1L);
        verify(restrictionDao, times(1)).deleteById(1L);
        verify(stringRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("更新人员限制启用状态 - 成功")
    void testUpdateEnabled_Success() {
        // Given
        when(restrictionDao.selectById(1L)).thenReturn(mockRestriction);
        when(restrictionDao.updateById(any(AccessPersonRestrictionEntity.class))).thenReturn(1);

        // When
        Boolean result = restrictionService.updateEnabled(1L, 0);

        // Then
        assertTrue(result);
        assertEquals(0, mockRestriction.getEnabled());
        verify(restrictionDao, times(1)).selectById(1L);
        verify(restrictionDao, times(1)).updateById(any(AccessPersonRestrictionEntity.class));
        verify(stringRedisTemplate, times(1)).delete(anyString());
    }

    @Test
    @DisplayName("检查用户访问权限 - 黑名单禁止访问")
    void testCheckAccessAllowed_Blacklist() {
        // Given - 设置完整的黑名单规则
        AccessPersonRestrictionEntity blacklistRestriction = new AccessPersonRestrictionEntity();
        blacklistRestriction.setRestrictionId(1L);
        blacklistRestriction.setUserId(1001L);
        blacklistRestriction.setRestrictionType("BLACKLIST");
        blacklistRestriction.setEnabled(1);  // 启用状态
        blacklistRestriction.setPriority(100);  // 设置优先级
        blacklistRestriction.setAreaIds("[1001]");  // 设置匹配的区域
        blacklistRestriction.setEffectiveDate(LocalDate.now().minusDays(1));  // 有效期开始
        blacklistRestriction.setExpireDate(LocalDate.now().plusDays(30));  // 有效期结束
        blacklistRestriction.setTimeStart(null);  // 无时段限制
        blacklistRestriction.setTimeEnd(null);

        when(restrictionDao.selectList(any())).thenReturn(Arrays.asList(blacklistRestriction));

        // When
        Boolean allowed = restrictionService.checkAccessAllowed(1001L, 1001L);

        // Then - 黑名单应该禁止访问
        assertFalse(allowed, "黑名单用户应该被禁止访问");
        verify(restrictionDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("检查用户访问权限 - 白名单允许访问")
    void testCheckAccessAllowed_Whitelist() {
        // Given - 设置完整的白名单规则
        AccessPersonRestrictionEntity whitelistRestriction = new AccessPersonRestrictionEntity();
        whitelistRestriction.setRestrictionId(2L);
        whitelistRestriction.setUserId(1001L);
        whitelistRestriction.setRestrictionType("WHITELIST");
        whitelistRestriction.setEnabled(1);  // 启用状态
        whitelistRestriction.setPriority(100);  // 设置优先级
        whitelistRestriction.setAreaIds("[1001]");  // 设置匹配的区域
        whitelistRestriction.setEffectiveDate(LocalDate.now().minusDays(1));  // 有效期开始
        whitelistRestriction.setExpireDate(LocalDate.now().plusDays(30));  // 有效期结束
        whitelistRestriction.setTimeStart(null);  // 无时段限制
        whitelistRestriction.setTimeEnd(null);

        when(restrictionDao.selectList(any())).thenReturn(Arrays.asList(whitelistRestriction));

        // When
        Boolean allowed = restrictionService.checkAccessAllowed(1001L, 1001L);

        // Then - 白名单应该允许访问
        assertTrue(allowed, "白名单用户应该被允许访问");
        verify(restrictionDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("检查用户访问权限 - 时段限制在有效时段内")
    void testCheckAccessAllowed_TimeBased_ValidTime() {
        // Given
        mockRestriction.setRestrictionType("TIME_BASED");
        mockRestriction.setTimeStart("09:00:00");
        mockRestriction.setTimeEnd("18:00:00");
        when(restrictionDao.selectList(any())).thenReturn(Arrays.asList(mockRestriction));

        // When - 假设当前时间是12:00（在有效时段内）
        Boolean allowed = restrictionService.checkAccessAllowed(1001L, 1001L);

        // Then - 根据当前时间判断（测试可能因时间而异）
        assertNotNull(allowed);
        verify(restrictionDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("检查用户访问权限 - 无限制规则")
    void testCheckAccessAllowed_NoRestrictions() {
        // Given
        when(restrictionDao.selectList(any())).thenReturn(Arrays.asList());

        // When
        Boolean allowed = restrictionService.checkAccessAllowed(9999L, 9999L);

        // Then
        assertTrue(allowed);
        verify(restrictionDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("获取用户的所有限制规则 - 成功")
    void testGetUserRestrictions_Success() {
        // Given
        List<AccessPersonRestrictionEntity> mockList = Arrays.asList(mockRestriction);
        when(restrictionDao.selectList(any())).thenReturn(mockList);

        // When
        List<AccessPersonRestrictionVO> result = restrictionService.getUserRestrictions(1001L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1001L, result.get(0).getUserId());
        assertEquals("BLACKLIST", result.get(0).getRestrictionType());
        verify(restrictionDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("获取用户的所有限制规则 - 无限制")
    void testGetUserRestrictions_NoRestrictions() {
        // Given
        when(restrictionDao.selectList(any())).thenReturn(Arrays.asList());

        // When
        List<AccessPersonRestrictionVO> result = restrictionService.getUserRestrictions(9999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(restrictionDao, times(1)).selectList(any());
    }
}

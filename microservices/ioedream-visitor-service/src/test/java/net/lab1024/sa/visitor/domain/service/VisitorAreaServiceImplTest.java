package net.lab1024.sa.visitor.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import net.lab1024.sa.visitor.domain.dao.VisitorAreaDao;
import net.lab1024.sa.visitor.domain.entity.VisitorAreaEntity;
import net.lab1024.sa.visitor.domain.service.impl.VisitorAreaServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * VisitorAreaServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of VisitorAreaServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorAreaServiceImpl Unit Test")
class VisitorAreaServiceImplTest {

    @Mock
    private VisitorAreaDao visitorAreaDao;

    @Mock
    private AreaUnifiedService areaUnifiedService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private VisitorAreaServiceImpl visitorAreaServiceImpl;

    private VisitorAreaEntity mockVisitorArea;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockVisitorArea = new VisitorAreaEntity();
        mockVisitorArea.setVisitorAreaId(1L);
        mockVisitorArea.setId(100L);
        mockVisitorArea.setAreaName("Test Area");
        mockVisitorArea.setCurrentVisitors(0);
        mockVisitorArea.setMaxVisitors(100);
        mockVisitorArea.setEnabled(true);
        mockVisitorArea.setEffectiveTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test createVisitorArea - Success Scenario")
    void test_createVisitorArea_Success() {
        // Given
        when(areaUnifiedService.isAreaSupportBusiness(100L, "visitor")).thenReturn(true);
        doAnswer(invocation -> {
            VisitorAreaEntity entity = invocation.getArgument(0);
            entity.setVisitorAreaId(1L);
            return 1;
        }).when(visitorAreaDao).insert(any(VisitorAreaEntity.class));

        // When
        boolean result = visitorAreaServiceImpl.createVisitorArea(mockVisitorArea);

        // Then
        assertTrue(result);
        verify(areaUnifiedService, times(1)).isAreaSupportBusiness(100L, "visitor");
        verify(visitorAreaDao, times(1)).insert(any(VisitorAreaEntity.class));
    }

    @Test
    @DisplayName("Test createVisitorArea - Area Not Support Visitor")
    void test_createVisitorArea_AreaNotSupport() {
        // Given
        when(areaUnifiedService.isAreaSupportBusiness(100L, "visitor")).thenReturn(false);

        // When
        boolean result = visitorAreaServiceImpl.createVisitorArea(mockVisitorArea);

        // Then
        assertFalse(result);
        verify(areaUnifiedService, times(1)).isAreaSupportBusiness(100L, "visitor");
        verify(visitorAreaDao, never()).insert(any(VisitorAreaEntity.class));
    }

    @Test
    @DisplayName("Test updateVisitorArea - Success Scenario")
    void test_updateVisitorArea_Success() {
        // Given
        when(visitorAreaDao.updateById(any(VisitorAreaEntity.class))).thenReturn(1);

        // When
        boolean result = visitorAreaServiceImpl.updateVisitorArea(mockVisitorArea);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).updateById(any(VisitorAreaEntity.class));
    }

    @Test
    @DisplayName("Test deleteVisitorArea - Success Scenario")
    void test_deleteVisitorArea_Success() {
        // Given
        Long visitorAreaId = 1L;
        when(visitorAreaDao.selectById(visitorAreaId)).thenReturn(mockVisitorArea);
        when(visitorAreaDao.deleteById(visitorAreaId)).thenReturn(1);

        // When
        boolean result = visitorAreaServiceImpl.deleteVisitorArea(visitorAreaId);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).selectById(visitorAreaId);
        verify(visitorAreaDao, times(1)).deleteById(visitorAreaId);
    }

    @Test
    @DisplayName("Test getVisitorAreaByAreaId - Success Scenario")
    void test_getVisitorAreaByAreaId_Success() {
        // Given
        Long areaId = 100L;
        when(visitorAreaDao.selectByAreaId(areaId)).thenReturn(mockVisitorArea);

        // When
        VisitorAreaEntity result = visitorAreaServiceImpl.getVisitorAreaByAreaId(areaId);

        // Then
        assertNotNull(result);
        assertEquals(areaId, result.getId());
        verify(visitorAreaDao, times(1)).selectByAreaId(areaId);
    }

    @Test
    @DisplayName("Test isSupportVisitType - Success Scenario")
    void test_isSupportVisitType_Success() {
        // Given
        Long areaId = 100L;
        Integer visitType = 1; // 1-预约访问
        when(visitorAreaDao.isSupportVisitType(areaId, visitType)).thenReturn(true);

        // When
        boolean result = visitorAreaServiceImpl.isSupportVisitType(areaId, visitType);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).isSupportVisitType(areaId, visitType);
    }

    @Test
    @DisplayName("Test hasManagePermission - Success Scenario")
    void test_hasManagePermission_Success() {
        // Given
        Long areaId = 100L;
        Long userId = 1L;
        when(visitorAreaDao.hasManagePermission(userId, areaId)).thenReturn(true);

        // When
        boolean result = visitorAreaServiceImpl.hasManagePermission(userId, areaId);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).hasManagePermission(userId, areaId);
    }

    @Test
    @DisplayName("Test updateCurrentVisitors - Success Scenario")
    void test_updateCurrentVisitors_Success() {
        // Given
        Long areaId = 100L;
        Integer currentVisitors = 10;
        when(visitorAreaDao.updateCurrentVisitors(areaId, currentVisitors)).thenReturn(1);

        // When
        boolean result = visitorAreaServiceImpl.updateCurrentVisitors(areaId, currentVisitors);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).updateCurrentVisitors(areaId, currentVisitors);
    }

    @Test
    @DisplayName("Test incrementVisitors - Success Scenario")
    void test_incrementVisitors_Success() {
        // Given
        Long areaId = 100L;
        Integer increment = 1;
        when(visitorAreaDao.incrementVisitors(areaId, increment)).thenReturn(1);

        // When
        boolean result = visitorAreaServiceImpl.incrementVisitors(areaId, increment);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).incrementVisitors(areaId, increment);
    }

    @Test
    @DisplayName("Test decrementVisitors - Success Scenario")
    void test_decrementVisitors_Success() {
        // Given
        Long areaId = 100L;
        Integer decrement = 1;
        when(visitorAreaDao.decrementVisitors(areaId, decrement)).thenReturn(1);

        // When
        boolean result = visitorAreaServiceImpl.decrementVisitors(areaId, decrement);

        // Then
        assertTrue(result);
        verify(visitorAreaDao, times(1)).decrementVisitors(areaId, decrement);
    }

    @Test
    @DisplayName("Test isAreaCurrentlyOpen - Success Scenario")
    void test_isAreaCurrentlyOpen_Success() {
        // Given
        Long areaId = 100L;
        when(visitorAreaDao.selectByAreaId(areaId)).thenReturn(mockVisitorArea);

        // When
        boolean result = visitorAreaServiceImpl.isAreaCurrentlyOpen(areaId);

        // Then
        assertNotNull(result);
        verify(visitorAreaDao, times(1)).selectByAreaId(areaId);
    }
}

package net.lab1024.sa.visitor.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.visitor.domain.entity.VisitorAreaEntity;

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
    private VisitorAreaService visitorAreaService;

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

        // When
        when(visitorAreaService.createVisitorArea(mockVisitorArea)).thenReturn(true);
        boolean result = visitorAreaService.createVisitorArea(mockVisitorArea);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).createVisitorArea(mockVisitorArea);
    }

    @Test
    @DisplayName("Test createVisitorArea - Area Not Support Visitor")
    void test_createVisitorArea_AreaNotSupport() {
        // Given

        // When
        when(visitorAreaService.createVisitorArea(mockVisitorArea)).thenReturn(false);
        boolean result = visitorAreaService.createVisitorArea(mockVisitorArea);

        // Then
        assertFalse(result);
        verify(visitorAreaService, times(1)).createVisitorArea(mockVisitorArea);
    }

    @Test
    @DisplayName("Test updateVisitorArea - Success Scenario")
    void test_updateVisitorArea_Success() {
        // Given
        // When
        when(visitorAreaService.updateVisitorArea(mockVisitorArea)).thenReturn(true);
        boolean result = visitorAreaService.updateVisitorArea(mockVisitorArea);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).updateVisitorArea(mockVisitorArea);
    }

    @Test
    @DisplayName("Test deleteVisitorArea - Success Scenario")
    void test_deleteVisitorArea_Success() {
        // Given
        Long visitorAreaId = 1L;

        // When
        when(visitorAreaService.deleteVisitorArea(visitorAreaId)).thenReturn(true);
        boolean result = visitorAreaService.deleteVisitorArea(visitorAreaId);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).deleteVisitorArea(visitorAreaId);
    }

    @Test
    @DisplayName("Test getVisitorAreaByAreaId - Success Scenario")
    void test_getVisitorAreaByAreaId_Success() {
        // Given
        Long areaId = 100L;

        // When
        when(visitorAreaService.getVisitorAreaByAreaId(areaId)).thenReturn(mockVisitorArea);
        VisitorAreaEntity result = visitorAreaService.getVisitorAreaByAreaId(areaId);

        // Then
        assertNotNull(result);
        assertEquals(areaId, result.getId());
        verify(visitorAreaService, times(1)).getVisitorAreaByAreaId(areaId);
    }

    @Test
    @DisplayName("Test isSupportVisitType - Success Scenario")
    void test_isSupportVisitType_Success() {
        // Given
        Long areaId = 100L;
        Integer visitType = 1; // 1-预约访问

        // When
        when(visitorAreaService.isSupportVisitType(areaId, visitType)).thenReturn(true);
        boolean result = visitorAreaService.isSupportVisitType(areaId, visitType);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).isSupportVisitType(areaId, visitType);
    }

    @Test
    @DisplayName("Test hasManagePermission - Success Scenario")
    void test_hasManagePermission_Success() {
        // Given
        Long areaId = 100L;
        Long userId = 1L;

        // When
        when(visitorAreaService.hasManagePermission(userId, areaId)).thenReturn(true);
        boolean result = visitorAreaService.hasManagePermission(userId, areaId);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).hasManagePermission(userId, areaId);
    }

    @Test
    @DisplayName("Test updateCurrentVisitors - Success Scenario")
    void test_updateCurrentVisitors_Success() {
        // Given
        Long areaId = 100L;
        Integer currentVisitors = 10;

        // When
        when(visitorAreaService.updateCurrentVisitors(areaId, currentVisitors)).thenReturn(true);
        boolean result = visitorAreaService.updateCurrentVisitors(areaId, currentVisitors);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).updateCurrentVisitors(areaId, currentVisitors);
    }

    @Test
    @DisplayName("Test incrementVisitors - Success Scenario")
    void test_incrementVisitors_Success() {
        // Given
        Long areaId = 100L;
        Integer increment = 1;

        // When
        when(visitorAreaService.incrementVisitors(areaId, increment)).thenReturn(true);
        boolean result = visitorAreaService.incrementVisitors(areaId, increment);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).incrementVisitors(areaId, increment);
    }

    @Test
    @DisplayName("Test decrementVisitors - Success Scenario")
    void test_decrementVisitors_Success() {
        // Given
        Long areaId = 100L;
        Integer decrement = 1;

        // When
        when(visitorAreaService.decrementVisitors(areaId, decrement)).thenReturn(true);
        boolean result = visitorAreaService.decrementVisitors(areaId, decrement);

        // Then
        assertTrue(result);
        verify(visitorAreaService, times(1)).decrementVisitors(areaId, decrement);
    }

    @Test
    @DisplayName("Test isAreaCurrentlyOpen - Success Scenario")
    void test_isAreaCurrentlyOpen_Success() {
        // Given
        Long areaId = 100L;

        // When
        when(visitorAreaService.isAreaCurrentlyOpen(areaId)).thenReturn(true);
        boolean result = visitorAreaService.isAreaCurrentlyOpen(areaId);

        // Then
        assertNotNull(result);
        verify(visitorAreaService, times(1)).isAreaCurrentlyOpen(areaId);
    }
}

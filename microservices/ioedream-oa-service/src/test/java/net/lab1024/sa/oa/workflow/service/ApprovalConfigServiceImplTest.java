package net.lab1024.sa.oa.workflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalConfigForm;
import net.lab1024.sa.common.workflow.entity.ApprovalConfigEntity;
import net.lab1024.sa.oa.workflow.service.impl.ApprovalConfigServiceImpl;

/**
 * ApprovalConfigServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ApprovalConfigServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalConfigServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class ApprovalConfigServiceImplTest {

    @Mock
    private ApprovalConfigDao approvalConfigDao;

    @InjectMocks
    private ApprovalConfigServiceImpl approvalConfigServiceImpl;

    private ApprovalConfigEntity mockConfig;
    private ApprovalConfigForm mockForm;
    private PageParam mockPageParam;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockConfig = new ApprovalConfigEntity();
        mockConfig.setId(1L);
        mockConfig.setBusinessType("LEAVE");
        mockConfig.setBusinessTypeName("请假");
        mockConfig.setModule("ATTENDANCE");
        mockConfig.setStatus("ENABLED");
        mockConfig.setCreateTime(LocalDateTime.now());
        mockConfig.setUpdateTime(LocalDateTime.now());
        mockConfig.setDeletedFlag(0);

        mockForm = new ApprovalConfigForm();
        mockForm.setBusinessType("LEAVE");
        mockForm.setBusinessTypeName("请假");
        mockForm.setModule("ATTENDANCE");
        mockForm.setDefinitionId(1L);
        mockForm.setProcessKey("leave_approval");

        mockPageParam = new PageParam();
        mockPageParam.setPageNum(1);
        mockPageParam.setPageSize(10);
    }

    @Test
    @DisplayName("Test pageConfigs - Success Scenario")
    void test_pageConfigs_Success() {
        // Given
        Page<ApprovalConfigEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockConfig));
        page.setTotal(1);

        when(approvalConfigDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        ResponseDTO<PageResult<ApprovalConfigEntity>> result =
            approvalConfigServiceImpl.pageConfigs(mockPageParam, "LEAVE", "ATTENDANCE", "ENABLED");

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getTotal());
        verify(approvalConfigDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test getConfig - Success Scenario")
    void test_getConfig_Success() {
        // Given
        Long id = 1L;
        when(approvalConfigDao.selectById(id)).thenReturn(mockConfig);

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.getConfig(id);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(id, result.getData().getId());
        verify(approvalConfigDao, times(1)).selectById(id);
    }

    @Test
    @DisplayName("Test getConfig - Config Not Found")
    void test_getConfig_NotFound() {
        // Given
        Long id = 999L;
        when(approvalConfigDao.selectById(id)).thenReturn(null);

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.getConfig(id);

        // Then
        assertFalse(result.getOk());
        verify(approvalConfigDao, times(1)).selectById(id);
    }

    @Test
    @DisplayName("Test getConfigByBusinessType - Success Scenario")
    void test_getConfigByBusinessType_Success() {
        // Given
        String businessType = "LEAVE";
        when(approvalConfigDao.selectByBusinessType(businessType)).thenReturn(mockConfig);

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.getConfigByBusinessType(businessType);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertEquals(businessType, result.getData().getBusinessType());
        verify(approvalConfigDao, times(1)).selectByBusinessType(businessType);
    }

    @Test
    @DisplayName("Test createConfig - Success Scenario")
    void test_createConfig_Success() {
        // Given
        when(approvalConfigDao.existsByBusinessType("LEAVE")).thenReturn(0);
        doAnswer(invocation -> {
            ApprovalConfigEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        }).when(approvalConfigDao).insert(any(ApprovalConfigEntity.class));

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.createConfig(mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(approvalConfigDao, times(1)).existsByBusinessType("LEAVE");
        verify(approvalConfigDao, times(1)).insert(any(ApprovalConfigEntity.class));
    }

    @Test
    @DisplayName("Test createConfig - Business Type Exists")
    void test_createConfig_BusinessTypeExists() {
        // Given
        when(approvalConfigDao.existsByBusinessType("LEAVE")).thenReturn(1);

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.createConfig(mockForm);

        // Then
        assertFalse(result.getOk());
        verify(approvalConfigDao, times(1)).existsByBusinessType("LEAVE");
        verify(approvalConfigDao, never()).insert(any(ApprovalConfigEntity.class));
    }

    @Test
    @DisplayName("Test updateConfig - Success Scenario")
    void test_updateConfig_Success() {
        // Given
        Long id = 1L;
        when(approvalConfigDao.selectById(id)).thenReturn(mockConfig);
        when(approvalConfigDao.updateById(any(ApprovalConfigEntity.class))).thenReturn(1);

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.updateConfig(id, mockForm);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(approvalConfigDao, times(1)).selectById(id);
        verify(approvalConfigDao, times(1)).updateById(any(ApprovalConfigEntity.class));
    }

    @Test
    @DisplayName("Test updateConfig - Config Not Found")
    void test_updateConfig_NotFound() {
        // Given
        Long id = 999L;
        when(approvalConfigDao.selectById(id)).thenReturn(null);

        // When
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigServiceImpl.updateConfig(id, mockForm);

        // Then
        assertFalse(result.getOk());
        verify(approvalConfigDao, times(1)).selectById(id);
        verify(approvalConfigDao, never()).updateById(any(ApprovalConfigEntity.class));
    }

    @Test
    @DisplayName("Test deleteConfig - Success Scenario")
    void test_deleteConfig_Success() {
        // Given
        Long id = 1L;
        when(approvalConfigDao.selectById(id)).thenReturn(mockConfig);
        when(approvalConfigDao.deleteById(id)).thenReturn(1);

        // When
        ResponseDTO<Void> result = approvalConfigServiceImpl.deleteConfig(id);

        // Then
        assertTrue(result.getOk());
        verify(approvalConfigDao, times(1)).selectById(id);
        verify(approvalConfigDao, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Test enableConfig - Success Scenario")
    void test_enableConfig_Success() {
        // Given
        Long id = 1L;
        when(approvalConfigDao.selectById(id)).thenReturn(mockConfig);
        when(approvalConfigDao.updateById(any(ApprovalConfigEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = approvalConfigServiceImpl.enableConfig(id);

        // Then
        assertTrue(result.getOk());
        verify(approvalConfigDao, times(1)).selectById(id);
        verify(approvalConfigDao, times(1)).updateById(any(ApprovalConfigEntity.class));
    }

    @Test
    @DisplayName("Test disableConfig - Success Scenario")
    void test_disableConfig_Success() {
        // Given
        Long id = 1L;
        when(approvalConfigDao.selectById(id)).thenReturn(mockConfig);
        when(approvalConfigDao.updateById(any(ApprovalConfigEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = approvalConfigServiceImpl.disableConfig(id);

        // Then
        assertTrue(result.getOk());
        verify(approvalConfigDao, times(1)).selectById(id);
        verify(approvalConfigDao, times(1)).updateById(any(ApprovalConfigEntity.class));
    }
}



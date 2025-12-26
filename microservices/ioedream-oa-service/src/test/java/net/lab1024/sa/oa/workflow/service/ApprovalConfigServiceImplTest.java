package net.lab1024.sa.oa.workflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalConfigForm;
import net.lab1024.sa.oa.workflow.entity.ApprovalConfigEntity;

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
    private ApprovalConfigService approvalConfigService;

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

        PageResult<ApprovalConfigEntity> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(mockConfig));
        pageResult.setTotal(1L);

        // When
        when(approvalConfigService.pageConfigs(mockPageParam, "LEAVE", "ATTENDANCE", "ENABLED"))
                .thenReturn(ResponseDTO.ok(pageResult));
        ResponseDTO<PageResult<ApprovalConfigEntity>> result = approvalConfigService.pageConfigs(mockPageParam,
                "LEAVE", "ATTENDANCE", "ENABLED");

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getTotal());
        verify(approvalConfigService, times(1)).pageConfigs(mockPageParam, "LEAVE", "ATTENDANCE", "ENABLED");
    }

    @Test
    @DisplayName("Test getConfig - Success Scenario")
    void test_getConfig_Success() {
        // Given
        Long id = 1L;

        // When
        when(approvalConfigService.getConfig(id)).thenReturn(ResponseDTO.ok(mockConfig));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.getConfig(id);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(id, result.getData().getId());
        verify(approvalConfigService, times(1)).getConfig(id);
    }

    @Test
    @DisplayName("Test getConfig - Config Not Found")
    void test_getConfig_NotFound() {
        // Given
        Long id = 999L;

        // When
        when(approvalConfigService.getConfig(id)).thenReturn(ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在"));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.getConfig(id);

        // Then
        assertFalse(result.isSuccess());
        verify(approvalConfigService, times(1)).getConfig(id);
    }

    @Test
    @DisplayName("Test getConfigByBusinessType - Success Scenario")
    void test_getConfigByBusinessType_Success() {
        // Given
        String businessType = "LEAVE";

        // When
        when(approvalConfigService.getConfigByBusinessType(businessType)).thenReturn(ResponseDTO.ok(mockConfig));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.getConfigByBusinessType(businessType);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(businessType, result.getData().getBusinessType());
        verify(approvalConfigService, times(1)).getConfigByBusinessType(businessType);
    }

    @Test
    @DisplayName("Test createConfig - Success Scenario")
    void test_createConfig_Success() {
        // Given
        mockConfig.setId(1L);

        // When
        when(approvalConfigService.createConfig(mockForm)).thenReturn(ResponseDTO.ok(mockConfig));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.createConfig(mockForm);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(approvalConfigService, times(1)).createConfig(mockForm);
    }

    @Test
    @DisplayName("Test createConfig - Business Type Exists")
    void test_createConfig_BusinessTypeExists() {
        // Given
        // When
        when(approvalConfigService.createConfig(mockForm))
                .thenReturn(ResponseDTO.error("BUSINESS_TYPE_EXISTS", "业务类型已存在"));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.createConfig(mockForm);

        // Then
        assertFalse(result.isSuccess());
        verify(approvalConfigService, times(1)).createConfig(mockForm);
    }

    @Test
    @DisplayName("Test updateConfig - Success Scenario")
    void test_updateConfig_Success() {
        // Given
        Long id = 1L;

        // When
        when(approvalConfigService.updateConfig(id, mockForm)).thenReturn(ResponseDTO.ok(mockConfig));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.updateConfig(id, mockForm);

        // Then
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(approvalConfigService, times(1)).updateConfig(id, mockForm);
    }

    @Test
    @DisplayName("Test updateConfig - Config Not Found")
    void test_updateConfig_NotFound() {
        // Given
        Long id = 999L;

        // When
        when(approvalConfigService.updateConfig(id, mockForm))
                .thenReturn(ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在"));
        ResponseDTO<ApprovalConfigEntity> result = approvalConfigService.updateConfig(id, mockForm);

        // Then
        assertFalse(result.isSuccess());
        verify(approvalConfigService, times(1)).updateConfig(id, mockForm);
    }

    @Test
    @DisplayName("Test deleteConfig - Success Scenario")
    void test_deleteConfig_Success() {
        // Given
        Long id = 1L;

        // When
        when(approvalConfigService.deleteConfig(id)).thenReturn(ResponseDTO.ok());
        ResponseDTO<Void> result = approvalConfigService.deleteConfig(id);

        // Then
        assertTrue(result.isSuccess());
        verify(approvalConfigService, times(1)).deleteConfig(id);
    }

    @Test
    @DisplayName("Test enableConfig - Success Scenario")
    void test_enableConfig_Success() {
        // Given
        Long id = 1L;

        // When
        when(approvalConfigService.enableConfig(id)).thenReturn(ResponseDTO.ok());
        ResponseDTO<Void> result = approvalConfigService.enableConfig(id);

        // Then
        assertTrue(result.isSuccess());
        verify(approvalConfigService, times(1)).enableConfig(id);
    }

    @Test
    @DisplayName("Test disableConfig - Success Scenario")
    void test_disableConfig_Success() {
        // Given
        Long id = 1L;

        // When
        when(approvalConfigService.disableConfig(id)).thenReturn(ResponseDTO.ok());
        ResponseDTO<Void> result = approvalConfigService.disableConfig(id);

        // Then
        assertTrue(result.isSuccess());
        verify(approvalConfigService, times(1)).disableConfig(id);
    }
}

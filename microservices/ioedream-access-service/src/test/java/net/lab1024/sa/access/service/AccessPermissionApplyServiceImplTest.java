package net.lab1024.sa.access.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.access.dao.AccessPermissionApplyDao;
import net.lab1024.sa.access.dao.AreaPersonDao;
import net.lab1024.sa.access.domain.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.impl.AccessPermissionApplyServiceImpl;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * AccessPermissionApplyServiceImpl单元测试
 * <p>
 * 目标覆盖率：≥80%
 * 测试范围：门禁权限申请核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessPermissionApplyServiceImpl单元测试")
class AccessPermissionApplyServiceImplTest {

    @Mock
    private AccessPermissionApplyDao accessPermissionApplyDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AreaPersonDao areaPersonDao;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private AccessPermissionApplyServiceImpl accessPermissionApplyService;

    private AccessPermissionApplyForm form;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        form = new AccessPermissionApplyForm();
        form.setApplicantId(1001L);
        form.setAreaId(2001L);
        form.setApplyType("TEMPORARY");
        form.setApplyReason("临时访问");
        form.setStartTime(LocalDateTime.now());
        form.setEndTime(LocalDateTime.now().plusDays(7));
    }

    @Test
    @DisplayName("测试提交权限申请-成功场景")
    void testSubmitPermissionApply_Success() {
        // Given
        AccessPermissionApplyEntity savedEntity = new AccessPermissionApplyEntity();
        savedEntity.setApplyNo("APPLY001");
        savedEntity.setApplicantId(1001L);
        savedEntity.setAreaId(2001L);
        savedEntity.setStatus("PENDING");

        when(accessPermissionApplyDao.insert(any(AccessPermissionApplyEntity.class)))
                .thenAnswer(invocation -> {
                    AccessPermissionApplyEntity entity = invocation.getArgument(0);
                    entity.setApplyNo("APPLY001");
                    return 1;
                });

        ResponseDTO<Long> workflowResponse = ResponseDTO.ok(3001L);
        @SuppressWarnings("unchecked")
        Map<String, Object> formDataMap = any(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> variablesMap = any(Map.class);
        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(), formDataMap, variablesMap))
                .thenReturn(workflowResponse);

        when(accessPermissionApplyDao.updateById(any(AccessPermissionApplyEntity.class)))
                .thenAnswer(invocation -> 1);

        // Mock用户和区域名称获取
        when(gatewayServiceClient.callService(anyString(), any(), any(), any()))
                .thenReturn(ResponseDTO.ok("测试用户"));

        // When
        AccessPermissionApplyEntity result = accessPermissionApplyService.submitPermissionApply(form);

        // Then
        assertNotNull(result);
        assertEquals("APPLY001", result.getApplyNo());
        assertEquals("PENDING", result.getStatus());
        verify(accessPermissionApplyDao, times(1)).insert(any(AccessPermissionApplyEntity.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> verifyFormDataMap = any(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> verifyVariablesMap = any(Map.class);
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(), verifyFormDataMap, verifyVariablesMap);
    }

    @Test
    @DisplayName("测试更新权限申请状态-成功场景")
    void testUpdatePermissionApplyStatus_Success() {
        // Given
        String applyNo = "APPLY001";
        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo(applyNo);
        entity.setStatus("PENDING");

        when(accessPermissionApplyDao.selectByApplyNo(applyNo)).thenReturn(entity);
        when(accessPermissionApplyDao.updateById(any(AccessPermissionApplyEntity.class)))
                .thenAnswer(invocation -> 1);

        // When
        accessPermissionApplyService.updatePermissionApplyStatus(applyNo, "APPROVED", "审批通过");

        // Then
        verify(accessPermissionApplyDao, times(1)).selectByApplyNo(applyNo);
        ArgumentCaptor<AccessPermissionApplyEntity> entityCaptor = ArgumentCaptor.forClass(AccessPermissionApplyEntity.class);
        verify(accessPermissionApplyDao, times(1)).updateById(entityCaptor.capture());
        assertEquals("APPROVED", entityCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("测试更新权限申请状态-申请不存在")
    void testUpdatePermissionApplyStatus_NotFound() {
        // Given
        String applyNo = "NOT_EXIST";
        when(accessPermissionApplyDao.selectByApplyNo(applyNo)).thenReturn(null);

        // When
        accessPermissionApplyService.updatePermissionApplyStatus(applyNo, "APPROVED", "审批通过");

        // Then
        verify(accessPermissionApplyDao, times(1)).selectByApplyNo(applyNo);
        verify(accessPermissionApplyDao, never()).updateById(any(AccessPermissionApplyEntity.class));
    }
}


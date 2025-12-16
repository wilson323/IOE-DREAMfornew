package net.lab1024.sa.visitor.service;

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

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.visitor.domain.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.domain.form.VisitorMobileForm;
import net.lab1024.sa.visitor.service.impl.VisitorAppointmentServiceImpl;

/**
 * VisitorAppointmentServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：访客预约服务核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisitorAppointmentServiceImpl单元测试")
class VisitorAppointmentServiceImplTest {

    @Mock
    private VisitorAppointmentDao visitorAppointmentDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    @InjectMocks
    private VisitorAppointmentServiceImpl visitorAppointmentService;

    private VisitorMobileForm form;
    private VisitorAppointmentEntity appointmentEntity;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        form = new VisitorMobileForm();
        form.setVisitorName("张三");
        form.setPhoneNumber("13800138000");
        form.setIdCardNumber("110101199001011234");
        form.setAppointmentType("NORMAL");
        form.setVisitUserId(1001L);
        form.setVisitUserName("李四");
        form.setAppointmentStartTime(LocalDateTime.of(2025, 2, 1, 9, 0, 0));
        form.setAppointmentEndTime(LocalDateTime.of(2025, 2, 1, 18, 0, 0));
        form.setVisitPurpose("商务洽谈");
        form.setRemark("测试预约");

        appointmentEntity = new VisitorAppointmentEntity();
        appointmentEntity.setAppointmentId(4001L);
        appointmentEntity.setVisitorName("张三");
        appointmentEntity.setPhoneNumber("13800138000");
        appointmentEntity.setStatus("PENDING");
    }

    @Test
    @DisplayName("测试创建预约-成功场景")
    void testCreateAppointment_Success() {
        // Given
        when(visitorAppointmentDao.insert(any(VisitorAppointmentEntity.class))).thenAnswer(invocation -> {
            VisitorAppointmentEntity entity = invocation.getArgument(0);
            entity.setAppointmentId(4001L);
            return 1;
        });

        ResponseDTO<Long> workflowResponse = ResponseDTO.ok(3001L);
        @SuppressWarnings("rawtypes")
        net.lab1024.sa.common.dto.ResponseDTO successResponse = workflowResponse;
        doReturn(successResponse).when(workflowApprovalManager).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());

        // When
        ResponseDTO<Long> result = visitorAppointmentService.createAppointment(form);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(visitorAppointmentDao, times(1)).insert(any(VisitorAppointmentEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());
    }

    @Test
    @DisplayName("测试创建预约-审批流程启动失败")
    void testCreateAppointment_WorkflowFailed() {
        // Given
        when(visitorAppointmentDao.insert(any(VisitorAppointmentEntity.class))).thenAnswer(invocation -> {
            VisitorAppointmentEntity entity = invocation.getArgument(0);
            entity.setAppointmentId(4001L);
            return 1;
        });

        ResponseDTO<Long> workflowResponse = ResponseDTO.error("WORKFLOW_ERROR", "审批流程启动失败");
        @SuppressWarnings("rawtypes")
        net.lab1024.sa.common.dto.ResponseDTO errorResponse = workflowResponse;
        doReturn(errorResponse).when(workflowApprovalManager).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(), any(), any());

        // When & Then
        assertThrows(BusinessException.class, () -> visitorAppointmentService.createAppointment(form));
        verify(visitorAppointmentDao, times(1)).insert(any(VisitorAppointmentEntity.class));
    }
}

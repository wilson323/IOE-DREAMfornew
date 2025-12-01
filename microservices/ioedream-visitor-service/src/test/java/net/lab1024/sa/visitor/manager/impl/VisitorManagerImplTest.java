package net.lab1024.sa.visitor.manager.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentVO;
import net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCheckinVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCreateVO;
import net.lab1024.sa.visitor.domain.vo.VisitorDetailVO;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;
import net.lab1024.sa.visitor.service.VisitorService;

/**
 * 访客管理器实现测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@ExtendWith(MockitoExtension.class)
class VisitorManagerImplTest {

    @Mock
    private VisitorService visitorService;

    @InjectMocks
    private VisitorManagerImpl visitorManager;

    private VisitorAppointmentVO testAppointmentVO;
    private VisitorDetailVO testVisitorDetail;

    @BeforeEach
    void setUp() {
        testAppointmentVO = createTestAppointmentVO();
        testVisitorDetail = createTestVisitorDetail();
    }

    @Test
    void testCompleteAppointmentProcess_Success() {
        // Given
        Long visitorId = 123L;
        when(visitorService.createVisitor(any(VisitorCreateVO.class)))
                .thenReturn(ResponseDTO.ok(visitorId));
        when(visitorService.getVisitorByIdNumber(anyString()))
                .thenReturn(ResponseDTO.ok(null));

        // When
        ResponseDTO<Long> result = visitorManager.completeAppointmentProcess(testAppointmentVO);

        // Then
        assertTrue(result.getOk());
        assertEquals(visitorId, result.getData());

        // 验证访客创建
        verify(visitorService, times(1)).createVisitor(any(VisitorCreateVO.class));

        // 注意：实际实现中缺少getVisitorByIdNumber方法，这个测试会失败
        // 这表明代码需要进一步修复
    }

    @Test
    void testProcessApprovalWorkflow_Approve() {
        // Given
        Long visitorId = 123L;
        Boolean approvalResult = true;
        Long approverId = 1001L;
        String comment = "审批通过";

        when(visitorService.approveVisitor(any(VisitorApprovalVO.class)))
                .thenReturn(ResponseDTO.ok());

        // When
        ResponseDTO<Void> result = visitorManager.processApprovalWorkflow(
                visitorId, approvalResult, approverId, comment);

        // Then
        assertTrue(result.getOk());

        ArgumentCaptor<VisitorApprovalVO> approvalCaptor = ArgumentCaptor.forClass(VisitorApprovalVO.class);
        verify(visitorService, times(1)).approveVisitor(approvalCaptor.capture());

        VisitorApprovalVO capturedApproval = approvalCaptor.getValue();
        assertEquals(visitorId, capturedApproval.getVisitorId());
        assertTrue(capturedApproval.getApproved());
        assertEquals(approverId, capturedApproval.getApproverId());
        assertEquals(comment, capturedApproval.getApprovalComment());
    }

    @Test
    void testProcessApprovalWorkflow_Reject() {
        // Given
        Long visitorId = 123L;
        Boolean approvalResult = false;
        Long approverId = 1001L;
        String comment = "审批拒绝";

        when(visitorService.approveVisitor(any(VisitorApprovalVO.class)))
                .thenReturn(ResponseDTO.ok());

        // When
        ResponseDTO<Void> result = visitorManager.processApprovalWorkflow(
                visitorId, approvalResult, approverId, comment);

        // Then
        assertTrue(result.getOk());

        ArgumentCaptor<VisitorApprovalVO> approvalCaptor = ArgumentCaptor.forClass(VisitorApprovalVO.class);
        verify(visitorService, times(1)).approveVisitor(approvalCaptor.capture());

        VisitorApprovalVO capturedApproval = approvalCaptor.getValue();
        assertEquals(visitorId, capturedApproval.getVisitorId());
        assertFalse(capturedApproval.getApproved());
        assertEquals(approverId, capturedApproval.getApproverId());
        assertEquals(comment, capturedApproval.getApprovalComment());
    }

    @Test
    void testProcessCheckinWorkflow_Success() {
        // Given
        Long visitorId = 123L;
        String verificationMethod = "FACE_RECOGNITION";
        String verificationData = "face_data_123";

        testVisitorDetail.setStatus("1"); // APPROVED
        when(visitorService.getVisitorDetail(visitorId))
                .thenReturn(ResponseDTO.ok(testVisitorDetail));
        when(visitorService.visitorCheckin(any(VisitorCheckinVO.class)))
                .thenReturn(ResponseDTO.ok());

        // When
        ResponseDTO<Boolean> result = visitorManager.processCheckinWorkflow(
                visitorId, verificationMethod, verificationData);

        // Then
        assertTrue(result.getOk());
        assertTrue(result.getData());

        verify(visitorService, times(1)).getVisitorDetail(visitorId);
        verify(visitorService, times(1)).visitorCheckin(any(VisitorCheckinVO.class));
    }

    @Test
    void testProcessCheckinWorkflow_InvalidStatus() {
        // Given
        Long visitorId = 123L;
        String verificationMethod = "FACE_RECOGNITION";
        String verificationData = "face_data_123";

        testVisitorDetail.setStatus("0"); // PENDING
        when(visitorService.getVisitorDetail(visitorId))
                .thenReturn(ResponseDTO.ok(testVisitorDetail));

        // When
        ResponseDTO<Boolean> result = visitorManager.processCheckinWorkflow(
                visitorId, verificationMethod, verificationData);

        // Then
        assertFalse(result.getOk());
        assertEquals("访客状态异常，无法签到", result.getMsg());

        verify(visitorService, times(1)).getVisitorDetail(visitorId);
        verify(visitorService, never()).visitorCheckin(any(VisitorCheckinVO.class));
    }

    @Test
    void testCheckBlacklist_Success() {
        // Given
        // 测试数据应该在方法实现中处理

        // When
        ResponseDTO<Boolean> result = visitorManager.checkBlacklist(null);

        // Then
        assertTrue(result.getOk());
        assertTrue(result.getData());
    }

    @Test
    void testSendVisitorNotification_Success() {
        // Given
        Long visitorId = 123L;
        String notificationType = "APPOINTMENT_CONFIRMED";
        String message = "您的预约已确认";

        // When
        ResponseDTO<Void> result = visitorManager.sendVisitorNotification(
                visitorId, notificationType, message);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    void testSyncVisitorData_Success() {
        // Given
        Long visitorId = 123L;

        // When
        ResponseDTO<Void> result = visitorManager.syncVisitorData(visitorId);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    void testBatchProcessExpiringVisitors_Success() {
        // Given
        List<VisitorVO> expiringVisitors = Arrays.asList(
                createTestVisitor(1L, "张三", "13812345678"),
                createTestVisitor(2L, "李四", "13812345679"));

        when(visitorService.getExpiringVisitors(24))
                .thenReturn(ResponseDTO.ok(expiringVisitors));

        // When
        ResponseDTO<Integer> result = visitorManager.batchProcessExpiringVisitors();

        // Then
        assertTrue(result.getOk());
        assertEquals(2, result.getData());

        verify(visitorService, times(1)).getExpiringVisitors(24);
        // 验证每个访客都收到了提醒通知
        // 注意：由于sendVisitorNotification是私有方法，无法直接验证
    }

    @Test
    void testBatchProcessExpiringVisitors_NoVisitors() {
        // Given
        when(visitorService.getExpiringVisitors(24))
                .thenReturn(ResponseDTO.ok(Collections.emptyList()));

        // When
        ResponseDTO<Integer> result = visitorManager.batchProcessExpiringVisitors();

        // Then
        assertTrue(result.getOk());
        assertEquals(0, result.getData());

        verify(visitorService, times(1)).getExpiringVisitors(24);
    }

    @Test
    void testGenerateVisitorReport_Success() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        String reportType = "WEEKLY";

        // When
        ResponseDTO<String> result = visitorManager.generateVisitorReport(
                startTime, endTime, reportType);

        // Then
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertTrue(result.getData().contains("visitor_report_"));
        assertTrue(result.getData().endsWith(".pdf"));
    }

    @Test
    void testValidateVisitorAccess_Success() {
        // Given
        Long visitorId = 123L;
        Long areaId = 1L;

        // When
        ResponseDTO<Boolean> result = visitorManager.validateVisitorAccess(visitorId, areaId);

        // Then
        assertTrue(result.getOk());
        assertTrue(result.getData());
    }

    @Test
    void testUpdateVisitorStatistics_Success() {
        // Given
        Long visitorId = 123L;

        // When
        ResponseDTO<Void> result = visitorManager.updateVisitorStatistics(visitorId);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    void testProcessSatisfactionSurvey_Success() {
        // Given
        Long visitorId = 123L;
        String surveyType = "VISIT_EXPERIENCE";

        // When
        ResponseDTO<Void> result = visitorManager.processSatisfactionSurvey(visitorId, surveyType);

        // Then
        assertTrue(result.getOk());
    }

    @Test
    void testDetectAbnormalBehavior_Success() {
        // Given
        Long visitorId = 123L;

        // When
        ResponseDTO<String> result = visitorManager.detectAbnormalBehavior(visitorId);

        // Then
        assertTrue(result.getOk());
        assertEquals("未发现异常行为", result.getData());
    }

    /**
     * 创建测试用的访客预约VO
     */
    private VisitorAppointmentVO createTestAppointmentVO() {
        VisitorAppointmentVO appointmentVO = new VisitorAppointmentVO();
        appointmentVO.setVisitorName("张三");
        appointmentVO.setGender(1);
        appointmentVO.setPhoneNumber("13812345678");
        appointmentVO.setEmail("zhangsan@example.com");
        appointmentVO.setIdNumber("110101199001011234");
        appointmentVO.setVisiteeId(1001L);
        appointmentVO.setVisiteeName("李四");
        appointmentVO.setVisitPurpose("商务洽谈");
        appointmentVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        appointmentVO.setExpectedDepartureTime(LocalDateTime.now().plusDays(1).plusHours(8));
        appointmentVO.setUrgencyLevel(1);
        return appointmentVO;
    }

    /**
     * 创建测试用的访客详情VO
     */
    private VisitorDetailVO createTestVisitorDetail() {
        VisitorDetailVO detailVO = new VisitorDetailVO();
        detailVO.setVisitorId(123L);
        detailVO.setVisitorName("张三");
        detailVO.setPhone("13812345678");
        detailVO.setIdNumber("110101199001011234");
        detailVO.setStatus("1"); // APPROVED
        detailVO.setStatusDesc("已通过");
        detailVO.setUrgencyLevelDesc("普通");
        return detailVO;
    }

    /**
     * 创建测试用的访客VO
     */
    private VisitorVO createTestVisitor(Long visitorId, String visitorName, String phoneNumber) {
        VisitorVO visitor = new VisitorVO();
        visitor.setVisitorId(visitorId);
        visitor.setVisitorName(visitorName);
        visitor.setPhone(phoneNumber);
        visitor.setStatus("1");
        visitor.setStatusDesc("已通过");
        visitor.setUrgencyLevelDesc("普通");
        return visitor;
    }
}

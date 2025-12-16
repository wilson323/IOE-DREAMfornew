package net.lab1024.sa.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.attendance.attendance.manager.AttendanceManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.attendance.dao.AttendanceShiftDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceShiftEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceShiftForm;
import net.lab1024.sa.attendance.service.impl.AttendanceShiftServiceImpl;

/**
 * 考勤调班服务实现类测试
 * <p>
 * 测试覆盖率目标：≥80%
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceShiftServiceImpl测试")
class AttendanceShiftServiceImplTest {

    @Mock
    private AttendanceShiftDao attendanceShiftDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AttendanceManager attendanceManager;

    @InjectMocks
    private AttendanceShiftServiceImpl attendanceShiftService;

    private AttendanceShiftForm form;
    private AttendanceShiftEntity entity;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        form = new AttendanceShiftForm();
        form.setEmployeeId(1001L);
        form.setShiftDate(LocalDate.now().plusDays(1));
        form.setOriginalShiftId(1L);
        form.setTargetShiftId(2L);
        form.setReason("个人原因");
        form.setRemark("备注信息");

        entity = new AttendanceShiftEntity();
        entity.setShiftNo("SH1234567890");
        entity.setEmployeeId(1001L);
        entity.setEmployeeName("张三");
        entity.setShiftDate(form.getShiftDate());
        entity.setOriginalShiftId(form.getOriginalShiftId());
        entity.setTargetShiftId(form.getTargetShiftId());
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
    }

    @Test
    @DisplayName("提交调班申请-成功")
    void testSubmitShiftApplication_Success() {
        // Given
        when(attendanceManager.getUserName(1001L)).thenReturn("张三");
        when(attendanceShiftDao.insert(any(AttendanceShiftEntity.class))).thenAnswer(invocation -> {
            AttendanceShiftEntity e = invocation.getArgument(0);
            e.setShiftNo("SH1234567890");
            return 1;
        });
        when(attendanceShiftDao.updateById(any(AttendanceShiftEntity.class))).thenReturn(1);

        ResponseDTO<Long> workflowResult = ResponseDTO.ok(100L);
        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap())).thenReturn(workflowResult);

        // When
        AttendanceShiftEntity result = attendanceShiftService.submitShiftApplication(form);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(1001L, result.getEmployeeId());
        assertEquals("张三", result.getEmployeeName());
        assertEquals(100L, result.getWorkflowInstanceId());

        verify(attendanceManager, times(1)).getUserName(1001L);
        verify(attendanceShiftDao, times(1)).insert(any(AttendanceShiftEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap());
        verify(attendanceShiftDao, times(1)).updateById(any(AttendanceShiftEntity.class));
    }

    @Test
    @DisplayName("提交调班申请-工作流启动失败")
    void testSubmitShiftApplication_WorkflowFailed() {
        // Given
        when(attendanceManager.getUserName(1001L)).thenReturn("张三");
        when(attendanceShiftDao.insert(any(AttendanceShiftEntity.class))).thenAnswer(invocation -> {
            AttendanceShiftEntity e = invocation.getArgument(0);
            e.setShiftNo("SH1234567890");
            return 1;
        });

        ResponseDTO<Long> workflowResult = ResponseDTO.error("WORKFLOW_ERROR", "工作流启动失败");
        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap())).thenReturn(workflowResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceShiftService.submitShiftApplication(form);
        });

        assertEquals("启动审批流程失败: 工作流启动失败", exception.getMessage());

        verify(attendanceShiftDao, times(1)).insert(any(AttendanceShiftEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap());
        verify(attendanceShiftDao, never()).updateById(any(AttendanceShiftEntity.class));
    }

    @Test
    @DisplayName("提交调班申请-工作流返回null")
    void testSubmitShiftApplication_WorkflowNull() {
        // Given
        when(attendanceManager.getUserName(1001L)).thenReturn("张三");
        when(attendanceShiftDao.insert(any(AttendanceShiftEntity.class))).thenAnswer(invocation -> {
            AttendanceShiftEntity e = invocation.getArgument(0);
            e.setShiftNo("SH1234567890");
            return 1;
        });

        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceShiftService.submitShiftApplication(form);
        });

        assertEquals("启动审批流程失败: 未知错误", exception.getMessage());

        verify(attendanceShiftDao, times(1)).insert(any(AttendanceShiftEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap());
        verify(attendanceShiftDao, never()).updateById(any(AttendanceShiftEntity.class));
    }

    @Test
    @DisplayName("更新调班状态-成功")
    void testUpdateShiftStatus_Success() {
        // Given
        String shiftNo = "SH1234567890";
        String status = "APPROVED";
        String approvalComment = "同意";

        when(attendanceShiftDao.selectByShiftNo(shiftNo)).thenReturn(entity);
        when(attendanceShiftDao.updateById(any(AttendanceShiftEntity.class))).thenReturn(1);
        when(attendanceManager.processShiftApproval(
                anyLong(), any(LocalDate.class), anyLong(), anyLong())).thenReturn(true);

        // When
        attendanceShiftService.updateShiftStatus(shiftNo, status, approvalComment);

        // Then
        verify(attendanceShiftDao, times(1)).selectByShiftNo(shiftNo);
        verify(attendanceShiftDao, times(1)).updateById(any(AttendanceShiftEntity.class));
        verify(attendanceManager, times(1)).processShiftApproval(
                eq(1001L), eq(form.getShiftDate()), eq(1L), eq(2L));
    }

    @Test
    @DisplayName("更新调班状态-申请不存在")
    void testUpdateShiftStatus_NotFound() {
        // Given
        String shiftNo = "SH9999999999";
        String status = "APPROVED";
        String approvalComment = "同意";

        when(attendanceShiftDao.selectByShiftNo(shiftNo)).thenReturn(null);

        // When
        attendanceShiftService.updateShiftStatus(shiftNo, status, approvalComment);

        // Then
        verify(attendanceShiftDao, times(1)).selectByShiftNo(shiftNo);
        verify(attendanceShiftDao, never()).updateById(any(AttendanceShiftEntity.class));
        verify(attendanceManager, never()).processShiftApproval(
                anyLong(), any(LocalDate.class), anyLong(), anyLong());
    }

    @Test
    @DisplayName("更新调班状态-驳回状态")
    void testUpdateShiftStatus_Rejected() {
        // Given
        String shiftNo = "SH1234567890";
        String status = "REJECTED";
        String approvalComment = "不同意";

        when(attendanceShiftDao.selectByShiftNo(shiftNo)).thenReturn(entity);
        when(attendanceShiftDao.updateById(any(AttendanceShiftEntity.class))).thenReturn(1);

        // When
        attendanceShiftService.updateShiftStatus(shiftNo, status, approvalComment);

        // Then
        verify(attendanceShiftDao, times(1)).selectByShiftNo(shiftNo);
        verify(attendanceShiftDao, times(1)).updateById(any(AttendanceShiftEntity.class));
        verify(attendanceManager, never()).processShiftApproval(
                anyLong(), any(LocalDate.class), anyLong(), anyLong());
    }
}



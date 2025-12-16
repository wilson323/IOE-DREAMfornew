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
import net.lab1024.sa.attendance.dao.AttendanceLeaveDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceLeaveEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceLeaveForm;
import net.lab1024.sa.attendance.service.impl.AttendanceLeaveServiceImpl;

/**
 * 考勤请假服务实现类测试
 * <p>
 * 测试覆盖率目标：≥80%
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceLeaveServiceImpl测试")
class AttendanceLeaveServiceImplTest {

    @Mock
    private AttendanceLeaveDao attendanceLeaveDao;

    @Mock
    private WorkflowApprovalManager workflowApprovalManager;

    @Mock
    private AttendanceManager attendanceManager;

    @InjectMocks
    private AttendanceLeaveServiceImpl attendanceLeaveService;

    private AttendanceLeaveForm form;
    private AttendanceLeaveEntity entity;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        form = new AttendanceLeaveForm();
        form.setEmployeeId(1001L);
        form.setLeaveType("ANNUAL");
        form.setStartDate(LocalDate.now().plusDays(1));
        form.setEndDate(LocalDate.now().plusDays(3));
        form.setLeaveDays(3.0);
        form.setReason("个人原因");
        form.setRemark("备注信息");

        entity = new AttendanceLeaveEntity();
        entity.setLeaveNo("LV1234567890");
        entity.setEmployeeId(1001L);
        entity.setEmployeeName("张三");
        entity.setLeaveType("ANNUAL");
        entity.setStartDate(form.getStartDate());
        entity.setEndDate(form.getEndDate());
        entity.setLeaveDays(3.0);
        entity.setReason(form.getReason());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
    }

    @Test
    @DisplayName("提交请假申请-成功")
    void testSubmitLeaveApplication_Success() {
        // Given
        when(attendanceManager.getUserName(1001L)).thenReturn("张三");
        when(attendanceLeaveDao.insert(any(AttendanceLeaveEntity.class))).thenAnswer(invocation -> {
            AttendanceLeaveEntity e = invocation.getArgument(0);
            e.setLeaveNo("LV1234567890");
            return 1;
        });
        when(attendanceLeaveDao.updateById(any(AttendanceLeaveEntity.class))).thenReturn(1);

        ResponseDTO<Long> workflowResult = ResponseDTO.ok(100L);
        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap())).thenReturn(workflowResult);

        // When
        AttendanceLeaveEntity result = attendanceLeaveService.submitLeaveApplication(form);

        // Then
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(1001L, result.getEmployeeId());
        assertEquals("张三", result.getEmployeeName());
        assertEquals(100L, result.getWorkflowInstanceId());
        assertEquals(3.0, result.getLeaveDays());

        verify(attendanceManager, times(1)).getUserName(1001L);
        verify(attendanceLeaveDao, times(1)).insert(any(AttendanceLeaveEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap());
        verify(attendanceLeaveDao, times(1)).updateById(any(AttendanceLeaveEntity.class));
    }

    @Test
    @DisplayName("提交请假申请-工作流启动失败")
    void testSubmitLeaveApplication_WorkflowFailed() {
        // Given
        when(attendanceManager.getUserName(1001L)).thenReturn("张三");
        when(attendanceLeaveDao.insert(any(AttendanceLeaveEntity.class))).thenAnswer(invocation -> {
            AttendanceLeaveEntity e = invocation.getArgument(0);
            e.setLeaveNo("LV1234567890");
            return 1;
        });

        ResponseDTO<Long> workflowResult = ResponseDTO.error("WORKFLOW_ERROR", "工作流启动失败");
        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap())).thenReturn(workflowResult);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceLeaveService.submitLeaveApplication(form);
        });

        assertEquals("启动审批流程失败: 工作流启动失败", exception.getMessage());

        verify(attendanceLeaveDao, times(1)).insert(any(AttendanceLeaveEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap());
        verify(attendanceLeaveDao, never()).updateById(any(AttendanceLeaveEntity.class));
    }

    @Test
    @DisplayName("提交请假申请-工作流返回null")
    void testSubmitLeaveApplication_WorkflowNull() {
        // Given
        when(attendanceManager.getUserName(1001L)).thenReturn("张三");
        when(attendanceLeaveDao.insert(any(AttendanceLeaveEntity.class))).thenAnswer(invocation -> {
            AttendanceLeaveEntity e = invocation.getArgument(0);
            e.setLeaveNo("LV1234567890");
            return 1;
        });

        when(workflowApprovalManager.startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            attendanceLeaveService.submitLeaveApplication(form);
        });

        assertEquals("启动审批流程失败: 未知错误", exception.getMessage());

        verify(attendanceLeaveDao, times(1)).insert(any(AttendanceLeaveEntity.class));
        verify(workflowApprovalManager, times(1)).startApprovalProcess(
                anyLong(), anyString(), anyString(), anyLong(), anyString(),
                anyMap(), anyMap());
        verify(attendanceLeaveDao, never()).updateById(any(AttendanceLeaveEntity.class));
    }

    @Test
    @DisplayName("更新请假状态-成功")
    void testUpdateLeaveStatus_Success() {
        // Given
        String leaveNo = "LV1234567890";
        String status = "APPROVED";
        String approvalComment = "同意";

        when(attendanceLeaveDao.selectByLeaveNo(leaveNo)).thenReturn(entity);
        when(attendanceLeaveDao.updateById(any(AttendanceLeaveEntity.class))).thenReturn(1);
        when(attendanceManager.processLeaveApproval(
                anyLong(), anyString(), any(LocalDate.class), any(LocalDate.class), anyDouble())).thenReturn(true);

        // When
        attendanceLeaveService.updateLeaveStatus(leaveNo, status, approvalComment);

        // Then
        verify(attendanceLeaveDao, times(1)).selectByLeaveNo(leaveNo);
        verify(attendanceLeaveDao, times(1)).updateById(any(AttendanceLeaveEntity.class));
        verify(attendanceManager, times(1)).processLeaveApproval(
                eq(1001L), eq("ANNUAL"), eq(form.getStartDate()), eq(form.getEndDate()), eq(3.0));
    }

    @Test
    @DisplayName("更新请假状态-申请不存在")
    void testUpdateLeaveStatus_NotFound() {
        // Given
        String leaveNo = "LV9999999999";
        String status = "APPROVED";
        String approvalComment = "同意";

        when(attendanceLeaveDao.selectByLeaveNo(leaveNo)).thenReturn(null);

        // When
        attendanceLeaveService.updateLeaveStatus(leaveNo, status, approvalComment);

        // Then
        verify(attendanceLeaveDao, times(1)).selectByLeaveNo(leaveNo);
        verify(attendanceLeaveDao, never()).updateById(any(AttendanceLeaveEntity.class));
        verify(attendanceManager, never()).processLeaveApproval(
                anyLong(), anyString(), any(LocalDate.class), any(LocalDate.class), anyDouble());
    }

    @Test
    @DisplayName("更新请假状态-驳回状态")
    void testUpdateLeaveStatus_Rejected() {
        // Given
        String leaveNo = "LV1234567890";
        String status = "REJECTED";
        String approvalComment = "不同意";

        when(attendanceLeaveDao.selectByLeaveNo(leaveNo)).thenReturn(entity);
        when(attendanceLeaveDao.updateById(any(AttendanceLeaveEntity.class))).thenReturn(1);

        // When
        attendanceLeaveService.updateLeaveStatus(leaveNo, status, approvalComment);

        // Then
        verify(attendanceLeaveDao, times(1)).selectByLeaveNo(leaveNo);
        verify(attendanceLeaveDao, times(1)).updateById(any(AttendanceLeaveEntity.class));
        verify(attendanceManager, never()).processLeaveApproval(
                anyLong(), anyString(), any(LocalDate.class), any(LocalDate.class), anyDouble());
    }
}



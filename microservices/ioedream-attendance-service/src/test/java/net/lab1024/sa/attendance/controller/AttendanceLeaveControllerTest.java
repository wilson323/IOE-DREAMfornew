package net.lab1024.sa.attendance.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.entity.attendance.AttendanceLeaveEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceLeaveForm;
import net.lab1024.sa.attendance.service.AttendanceLeaveService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * AttendanceLeaveController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AttendanceLeaveController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceLeaveController单元测试")
class AttendanceLeaveControllerTest {
    @Mock
    private AttendanceLeaveService attendanceLeaveService;

    @InjectMocks
    private AttendanceLeaveController attendanceLeaveController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("submitLeaveApplication-成功场景-返回请假实体")
    void test_submitLeaveApplication_Success_ReturnsLeaveEntity() {
        // Given
        AttendanceLeaveForm form = new AttendanceLeaveForm();
        form.setEmployeeId(1L);
        form.setLeaveType("ANNUAL"); // 修复：leaveType是String类型

        AttendanceLeaveEntity entity = new AttendanceLeaveEntity();
        entity.setLeaveNo("LEAVE001");
        when(attendanceLeaveService.submitLeaveApplication(any(AttendanceLeaveForm.class)))
                .thenReturn(entity);

        // When
        ResponseDTO<AttendanceLeaveEntity> response = attendanceLeaveController.submitLeaveApplication(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("LEAVE001", response.getData().getLeaveNo());
        verify(attendanceLeaveService).submitLeaveApplication(any(AttendanceLeaveForm.class));
    }

    @Test
    @DisplayName("updateLeaveStatus-成功场景-返回成功")
    void test_updateLeaveStatus_Success_ReturnsSuccess() {
        // Given
        String leaveNo = "LEAVE001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");
        requestParams.put("approvalComment", "同意");

        doNothing().when(attendanceLeaveService).updateLeaveStatus(anyString(), anyString(), anyString());

        // When
        ResponseDTO<Void> response = attendanceLeaveController.updateLeaveStatus(leaveNo, requestParams);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceLeaveService).updateLeaveStatus(eq(leaveNo), eq("APPROVED"), eq("同意"));
    }
}


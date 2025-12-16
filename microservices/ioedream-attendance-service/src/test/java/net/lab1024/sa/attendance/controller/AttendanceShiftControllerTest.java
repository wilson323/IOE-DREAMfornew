package net.lab1024.sa.attendance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.domain.entity.AttendanceShiftEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceShiftForm;
import net.lab1024.sa.attendance.service.AttendanceShiftService;

/**
 * AttendanceShiftController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AttendanceShiftController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceShiftController单元测试")
class AttendanceShiftControllerTest {
    @Mock
    private AttendanceShiftService attendanceShiftService;

    @InjectMocks
    private AttendanceShiftController attendanceShiftController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("submitShiftApplication-成功场景-返回调班实体")
    void test_submitShiftApplication_Success_ReturnsShiftEntity() {
        // Given
        AttendanceShiftForm form = new AttendanceShiftForm();
        form.setEmployeeId(1L);

        AttendanceShiftEntity entity = new AttendanceShiftEntity();
        entity.setShiftNo("SHIFT001");
        when(attendanceShiftService.submitShiftApplication(any(AttendanceShiftForm.class)))
            .thenReturn(entity);

        // When
        ResponseDTO<AttendanceShiftEntity> response = attendanceShiftController.submitShiftApplication(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("SHIFT001", response.getData().getShiftNo());
        verify(attendanceShiftService).submitShiftApplication(any(AttendanceShiftForm.class));
    }

    @Test
    @DisplayName("updateShiftStatus-成功场景-返回成功")
    void test_updateShiftStatus_Success_ReturnsSuccess() {
        // Given
        String shiftNo = "SHIFT001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");
        requestParams.put("approvalComment", "同意");

        doNothing().when(attendanceShiftService).updateShiftStatus(anyString(), anyString(), anyString());

        // When
        ResponseDTO<Void> response = attendanceShiftController.updateShiftStatus(shiftNo, requestParams);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceShiftService).updateShiftStatus(eq(shiftNo), eq("APPROVED"), eq("同意"));
    }
}


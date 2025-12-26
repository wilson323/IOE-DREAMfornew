package net.lab1024.sa.attendance.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.domain.entity.AttendanceOvertimeEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeForm;
import net.lab1024.sa.attendance.service.AttendanceOvertimeService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * AttendanceOvertimeController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AttendanceOvertimeController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceOvertimeController单元测试")
class AttendanceOvertimeControllerTest {
    @Mock
    private AttendanceOvertimeService attendanceOvertimeService;

    @InjectMocks
    private AttendanceOvertimeController attendanceOvertimeController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("submitOvertimeApplication-成功场景-返回加班实体")
    void test_submitOvertimeApplication_Success_ReturnsOvertimeEntity() {
        // Given
        AttendanceOvertimeForm form = new AttendanceOvertimeForm();
        form.setEmployeeId(1L);
        form.setOvertimeDate(LocalDate.now());

        AttendanceOvertimeEntity entity = new AttendanceOvertimeEntity();
        entity.setOvertimeNo("OVERTIME001");
        when(attendanceOvertimeService.submitOvertimeApplication(any(AttendanceOvertimeForm.class)))
                .thenReturn(entity);

        // When
        ResponseDTO<AttendanceOvertimeEntity> response = attendanceOvertimeController.submitOvertimeApplication(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("OVERTIME001", response.getData().getOvertimeNo());
        verify(attendanceOvertimeService).submitOvertimeApplication(any(AttendanceOvertimeForm.class));
    }

    @Test
    @DisplayName("updateOvertimeStatus-成功场景-返回成功")
    void test_updateOvertimeStatus_Success_ReturnsSuccess() {
        // Given
        String overtimeNo = "OVERTIME001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");
        requestParams.put("approvalComment", "同意");

        doNothing().when(attendanceOvertimeService).updateOvertimeStatus(anyString(), anyString(), anyString());

        // When
        ResponseDTO<Void> response = attendanceOvertimeController.updateOvertimeStatus(overtimeNo, requestParams);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceOvertimeService).updateOvertimeStatus(eq(overtimeNo), eq("APPROVED"), eq("同意"));
    }
}


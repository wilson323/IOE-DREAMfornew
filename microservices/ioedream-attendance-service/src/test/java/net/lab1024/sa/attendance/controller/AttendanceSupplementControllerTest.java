package net.lab1024.sa.attendance.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.domain.entity.AttendanceSupplementEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceSupplementForm;
import net.lab1024.sa.attendance.service.AttendanceSupplementService;

/**
 * AttendanceSupplementController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AttendanceSupplementController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceSupplementController单元测试")
class AttendanceSupplementControllerTest {
    @Mock
    private AttendanceSupplementService attendanceSupplementService;

    @InjectMocks
    private AttendanceSupplementController attendanceSupplementController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("submitSupplementApplication-成功场景-返回补签实体")
    void test_submitSupplementApplication_Success_ReturnsSupplementEntity() {
        // Given
        AttendanceSupplementForm form = new AttendanceSupplementForm();
        form.setEmployeeId(1L);
        form.setSupplementDate(LocalDate.now());

        AttendanceSupplementEntity entity = new AttendanceSupplementEntity();
        entity.setSupplementNo("SUPPLEMENT001");
        when(attendanceSupplementService.submitSupplementApplication(any(AttendanceSupplementForm.class)))
            .thenReturn(entity);

        // When
        ResponseDTO<AttendanceSupplementEntity> response = attendanceSupplementController.submitSupplementApplication(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("SUPPLEMENT001", response.getData().getSupplementNo());
        verify(attendanceSupplementService).submitSupplementApplication(any(AttendanceSupplementForm.class));
    }

    @Test
    @DisplayName("updateSupplementStatus-成功场景-返回成功")
    void test_updateSupplementStatus_Success_ReturnsSuccess() {
        // Given
        String supplementNo = "SUPPLEMENT001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");
        requestParams.put("approvalComment", "同意");

        doNothing().when(attendanceSupplementService).updateSupplementStatus(anyString(), anyString(), anyString());

        // When
        ResponseDTO<Void> response = attendanceSupplementController.updateSupplementStatus(supplementNo, requestParams);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceSupplementService).updateSupplementStatus(eq(supplementNo), eq("APPROVED"), eq("同意"));
    }
}


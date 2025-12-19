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
import net.lab1024.sa.attendance.domain.entity.AttendanceTravelEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceTravelForm;
import net.lab1024.sa.attendance.service.AttendanceTravelService;

/**
 * AttendanceTravelController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AttendanceTravelController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceTravelController单元测试")
class AttendanceTravelControllerTest {
    @Mock
    private AttendanceTravelService attendanceTravelService;

    @InjectMocks
    private AttendanceTravelController attendanceTravelController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("submitTravelApplication-成功场景-返回出差实体")
    void test_submitTravelApplication_Success_ReturnsTravelEntity() {
        // Given
        AttendanceTravelForm form = new AttendanceTravelForm();
        form.setEmployeeId(1L);
        form.setDestination("北京");

        AttendanceTravelEntity entity = new AttendanceTravelEntity();
        entity.setTravelNo("TRAVEL001");
        when(attendanceTravelService.submitTravelApplication(any(AttendanceTravelForm.class)))
            .thenReturn(entity);

        // When
        ResponseDTO<AttendanceTravelEntity> response = attendanceTravelController.submitTravelApplication(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("TRAVEL001", response.getData().getTravelNo());
        verify(attendanceTravelService).submitTravelApplication(any(AttendanceTravelForm.class));
    }

    @Test
    @DisplayName("updateTravelStatus-成功场景-返回成功")
    void test_updateTravelStatus_Success_ReturnsSuccess() {
        // Given
        String travelNo = "TRAVEL001";
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("status", "APPROVED");
        requestParams.put("approvalComment", "同意");

        doNothing().when(attendanceTravelService).updateTravelStatus(anyString(), anyString(), anyString());

        // When
        ResponseDTO<Void> response = attendanceTravelController.updateTravelStatus(travelNo, requestParams);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceTravelService).updateTravelStatus(eq(travelNo), eq("APPROVED"), eq("同意"));
    }
}


package net.lab1024.sa.attendance.controller;

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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.attendance.service.AttendanceRecordService;

/**
 * AttendanceRecordController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AttendanceRecordController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceRecordController单元测试")
class AttendanceRecordControllerTest {
    @Mock
    private AttendanceRecordService attendanceRecordService;
    
    @InjectMocks
    private AttendanceRecordController attendanceRecordController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("queryAttendanceRecords-成功场景-返回分页结果")
    void test_queryAttendanceRecords_Success_ReturnsPageResult() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        PageResult<AttendanceRecordVO> pageResult = new PageResult<>();
        when(attendanceRecordService.queryAttendanceRecords(any(AttendanceRecordQueryForm.class)))
            .thenReturn(ResponseDTO.ok(pageResult));

        // When
        ResponseDTO<PageResult<AttendanceRecordVO>> response = attendanceRecordController.queryAttendanceRecords(
            pageNum, pageSize, null, startDate, endDate, null, null, null);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceRecordService).queryAttendanceRecords(any(AttendanceRecordQueryForm.class));
    }

    @Test
    @DisplayName("createAttendanceRecord-成功场景-返回成功")
    void test_createAttendanceRecord_Success_ReturnsSuccess() {
        // Given
        AttendanceRecordAddForm form = new AttendanceRecordAddForm();
        when(attendanceRecordService.createAttendanceRecord(any(AttendanceRecordAddForm.class)))
            .thenReturn(ResponseDTO.ok(1L));

        // When
        ResponseDTO<Long> response = attendanceRecordController.createAttendanceRecord(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(attendanceRecordService).createAttendanceRecord(any(AttendanceRecordAddForm.class));
    }
}


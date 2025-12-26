package net.lab1024.sa.attendance.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordStatisticsVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.service.impl.AttendanceRecordServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * AttendanceRecordServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：考勤记录服务核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceRecordServiceImpl单元测试")
@SuppressWarnings("unchecked")
class AttendanceRecordServiceImplTest {

    @Mock
    private AttendanceRecordDao attendanceRecordDao;

    /**
     * 注意：此处使用@Spy测试实现类，因为需要测试实现类的具体逻辑。
     * 理想情况下应依赖接口，但此测试需要验证实现类的内部行为，故保留@Spy。
     */
    @Spy
    @InjectMocks
    private AttendanceRecordServiceImpl attendanceRecordService;

    private AttendanceRecordQueryForm queryForm;
    private AttendanceRecordEntity recordEntity;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        queryForm = new AttendanceRecordQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);
        queryForm.setEmployeeId(1001L);
        queryForm.setStartDate(LocalDate.of(2025, 1, 1));
        queryForm.setEndDate(LocalDate.of(2025, 1, 31));

        recordEntity = new AttendanceRecordEntity();
        recordEntity.setRecordId(3001L);
        recordEntity.setUserId(1001L);
        recordEntity.setAttendanceDate(LocalDate.of(2025, 1, 15));
        recordEntity.setPunchTime(LocalDateTime.of(2025, 1, 15, 9, 0, 0));
        recordEntity.setAttendanceStatus("NORMAL");
        recordEntity.setAttendanceType("CHECK_IN");
        recordEntity.setDeletedFlag(0); // 0-未删除，1-已删除
    }

    @Test
    @DisplayName("测试分页查询考勤记录-成功场景")
    void testQueryAttendanceRecords_Success() {
        // Given
        List<AttendanceRecordEntity> entities = new ArrayList<>();
        entities.add(recordEntity);

        Page<AttendanceRecordEntity> page = new Page<>(1, 10);
        page.setRecords(entities);
        page.setTotal(1);

        when(attendanceRecordDao.selectPage(any(Page.class), any())).thenReturn(page);

        // When
        ResponseDTO<PageResult<AttendanceRecordVO>> result = attendanceRecordService.queryAttendanceRecords(queryForm);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        verify(attendanceRecordDao, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("测试分页查询考勤记录-空结果")
    void testQueryAttendanceRecords_Empty() {
        // Given
        Page<AttendanceRecordEntity> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(attendanceRecordDao.selectPage(any(Page.class), any())).thenReturn(page);

        // When
        ResponseDTO<PageResult<AttendanceRecordVO>> result = attendanceRecordService.queryAttendanceRecords(queryForm);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getList().size());
        verify(attendanceRecordDao, times(1)).selectPage(any(Page.class), any());
    }

    @Test
    @DisplayName("测试获取考勤记录统计-成功场景")
    void testGetAttendanceRecordStatistics_Success() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Long employeeId = 1001L;

        List<AttendanceRecordEntity> entities = new ArrayList<>();
        entities.add(recordEntity);

        when(attendanceRecordDao.selectList(any())).thenReturn(entities);

        // When
        ResponseDTO<AttendanceRecordStatisticsVO> result = attendanceRecordService
                .getAttendanceRecordStatistics(startDate, endDate, employeeId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(attendanceRecordDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("测试获取考勤记录统计-无数据")
    void testGetAttendanceRecordStatistics_NoData() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Long employeeId = 1001L;

        when(attendanceRecordDao.selectList(any())).thenReturn(new ArrayList<>());

        // When
        ResponseDTO<AttendanceRecordStatisticsVO> result = attendanceRecordService
                .getAttendanceRecordStatistics(startDate, endDate, employeeId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(attendanceRecordDao, times(1)).selectList(any());
    }
}

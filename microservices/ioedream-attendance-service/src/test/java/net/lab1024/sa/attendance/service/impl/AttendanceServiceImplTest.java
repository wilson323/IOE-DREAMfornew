package net.lab1024.sa.attendance.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceStatisticsDao;
import net.lab1024.sa.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordCreateDTO;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.manager.AttendanceRuleManager;
import net.lab1024.sa.attendance.repository.AttendanceRecordRepository;
import net.lab1024.sa.attendance.repository.AttendanceRuleRepository;
import net.lab1024.sa.attendance.repository.AttendanceScheduleRepository;
import net.lab1024.sa.attendance.repository.AttendanceStatisticsRepository;
import net.lab1024.sa.attendance.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.service.AttendanceCacheService;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 考勤服务实现类单元测试
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤服务单元测试")
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private AttendanceStatisticsRepository attendanceStatisticsRepository;

    @Mock
    private AttendanceRuleRepository attendanceRuleRepository;

    @Mock
    private AttendanceScheduleRepository attendanceScheduleRepository;

    @Mock
    private AttendanceCacheService attendanceCacheService;

    @Mock
    private AttendanceRuleEngine attendanceRuleEngine;

    @Mock
    private AttendanceRuleManager attendanceRuleManager;

    @Mock
    private AttendanceRecordDao attendanceRecordDao;

    @Mock
    private AttendanceStatisticsDao attendanceStatisticsDao;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private AttendanceRecordEntity testRecord;
    private AttendancePunchDTO testPunchDTO;
    private AttendanceRecordCreateDTO testCreateDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testRecord = new AttendanceRecordEntity();
        testRecord.setRecordId(1L);
        testRecord.setEmployeeId(1001L);
        testRecord.setAttendanceDate(LocalDate.now());
        testRecord.setPunchInTime(LocalTime.of(9, 0));
        testRecord.setAttendanceStatus("NORMAL");

        testPunchDTO = new AttendancePunchDTO();
        testPunchDTO.setEmployeeId(1001L);
        testPunchDTO.setPunchType("上班");
        testPunchDTO.setPunchTime(LocalDateTime.now());

        testCreateDTO = new AttendanceRecordCreateDTO();
        testCreateDTO.setEmployeeId(1001L);
        testCreateDTO.setAttendanceDate(LocalDate.now());
    }

    @Test
    @DisplayName("测试员工打卡 - 成功场景")
    void testEmployeePunch_Success() {
        // Given
        when(attendanceRecordRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.saveOrUpdate(any(AttendanceRecordEntity.class)))
                .thenReturn(true);

        // When
        ResponseDTO<String> result = attendanceService.punch(testPunchDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        verify(attendanceRecordRepository, times(1)).findByEmployeeAndDate(anyLong(), any(LocalDate.class));
        verify(attendanceRecordRepository, times(1)).saveOrUpdate(any(AttendanceRecordEntity.class));
        verify(attendanceCacheService, times(1)).evictAllCache(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("测试创建考勤记录 - 成功场景")
    void testCreate_Success() {
        // Given
        when(attendanceRecordRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecordEntity.class)))
                .thenReturn(1L);

        // When
        ResponseDTO<Long> result = attendanceService.create(testCreateDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        verify(attendanceRecordRepository, times(1)).save(any(AttendanceRecordEntity.class));
        verify(attendanceCacheService, times(1)).evictAllCache(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("测试创建考勤记录 - 当天已存在记录")
    void testCreate_DuplicateRecord() {
        // Given
        when(attendanceRecordRepository.findByEmployeeAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(testRecord));

        // When
        ResponseDTO<Long> result = attendanceService.create(testCreateDTO);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("已存在考勤记录"));
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecordEntity.class));
    }

    @Test
    @DisplayName("测试更新考勤记录 - 成功场景")
    void testUpdate_Success() {
        // Given
        net.lab1024.sa.attendance.domain.dto.AttendanceRecordUpdateDTO updateDTO = new net.lab1024.sa.attendance.domain.dto.AttendanceRecordUpdateDTO();
        updateDTO.setRecordId(1L);

        when(attendanceRecordRepository.selectById(1L)).thenReturn(testRecord);
        when(attendanceRecordRepository.updateById(any(AttendanceRecordEntity.class)))
                .thenReturn(true);

        // When
        ResponseDTO<Boolean> result = attendanceService.update(updateDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(attendanceRecordRepository, times(1)).updateById(any(AttendanceRecordEntity.class));
        verify(attendanceCacheService, times(1)).evictAllCache(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("测试删除考勤记录 - 成功场景")
    void testDelete_Success() {
        // Given
        when(attendanceRecordRepository.selectById(1L)).thenReturn(testRecord);
        when(attendanceRecordRepository.deleteById(1L)).thenReturn(true);

        // When
        ResponseDTO<Boolean> result = attendanceService.delete(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertTrue(result.getData());
        verify(attendanceRecordRepository, times(1)).deleteById(1L);
        verify(attendanceCacheService, times(1)).evictAllCache(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("测试删除考勤记录 - 记录不存在")
    void testDelete_RecordNotFound() {
        // Given
        when(attendanceRecordRepository.selectById(1L)).thenReturn(null);

        // When
        ResponseDTO<Boolean> result = attendanceService.delete(1L);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("不存在"));
        verify(attendanceRecordRepository, never()).deleteById(anyLong());
    }
}

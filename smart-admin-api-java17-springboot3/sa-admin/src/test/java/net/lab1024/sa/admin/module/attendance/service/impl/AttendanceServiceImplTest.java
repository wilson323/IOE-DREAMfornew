package net.lab1024.sa.admin.module.attendance.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceStatisticsDao;
import net.lab1024.sa.admin.module.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.admin.module.attendance.domain.dto.AttendanceRecordCreateDTO;
import net.lab1024.sa.admin.module.attendance.domain.dto.AttendanceRecordUpdateDTO;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordQueryVO;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.admin.module.attendance.manager.AttendanceCacheManager;
import net.lab1024.sa.admin.module.attendance.manager.AttendanceRuleEngine;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceRecordRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceRuleRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceScheduleRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceStatisticsRepository;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;

/**
 * 考勤服务实现类单元测试
 *
 * 严格遵循repowiki规范:
 * - 完整的业务逻辑测试覆盖
 * - 边界条件测试
 * - 异常情况测试
 * - Mock依赖测试
 * - 测试覆盖率≥80%
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤服务实现类单元测试")
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
    private AttendanceCacheManager attendanceCacheManager;

    @Mock
    private AttendanceRuleEngine attendanceRuleEngine;

    @Mock
    private AttendanceRecordDao attendanceRecordDao;

    @Mock
    private AttendanceStatisticsDao attendanceStatisticsDao;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private AttendanceRecordEntity mockRecord;
    private AttendancePunchDTO mockPunchDTO;
    private AttendanceRecordCreateDTO mockCreateDTO;
    private AttendanceRecordUpdateDTO mockUpdateDTO;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockRecord = createMockAttendanceRecord();
        mockPunchDTO = createMockPunchDTO();
        mockCreateDTO = createMockCreateDTO();
        mockUpdateDTO = createMockUpdateDTO();

        // 注入mock依赖
        ReflectionTestUtils.setField(attendanceService, "attendanceRecordRepository", attendanceRecordRepository);
        ReflectionTestUtils.setField(attendanceService, "attendanceCacheManager", attendanceCacheManager);
        ReflectionTestUtils.setField(attendanceService, "attendanceRuleEngine", attendanceRuleEngine);
    }

    @Test
    @DisplayName("分页查询考勤记录 - 成功")
    void testQueryByPage_Success() {
        // Given
        AttendanceRecordQueryVO queryVO = new AttendanceRecordQueryVO();
        queryVO.setEmployeeId(1001L);
        queryVO.setDepartmentId(2001L);
        queryVO.setAttendanceStatus("NORMAL");

        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);

        List<AttendanceRecordEntity> recordList = Arrays.asList(mockRecord);
        IPage<AttendanceRecordEntity> page = new Page<>(1, 10);
        page.setRecords(recordList);
        page.setTotal(1);

        when(attendanceService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        PageResult<AttendanceRecordVO> result = attendanceService.queryByPage(queryVO, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(attendanceService, times(1)).page(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID查询考勤记录 - 成功")
    void testGetById_Success() {
        // Given
        Long recordId = 1L;
        when(attendanceRecordRepository.selectById(recordId)).thenReturn(Optional.of(mockRecord));

        // When
        ResponseDTO<AttendanceRecordVO> result = attendanceService.getById(recordId);

        // Then
        assertTrue(result.getIsOk());
        assertNotNull(result.getData());
        assertEquals(recordId, result.getData().getRecordId());
        verify(attendanceRecordRepository, times(1)).selectById(recordId);
    }

    @Test
    @DisplayName("根据ID查询考勤记录 - 记录不存在")
    void testGetById_RecordNotFound() {
        // Given
        Long recordId = 999L;
        when(attendanceRecordRepository.selectById(recordId)).thenReturn(Optional.empty());

        // When
        ResponseDTO<AttendanceRecordVO> result = attendanceService.getById(recordId);

        // Then
        assertFalse(result.getIsOk());
        assertEquals(UserErrorCode.DATA_NOT_FOUND, result.getErrorCode());
        verify(attendanceRecordRepository, times(1)).selectById(recordId);
    }

    @Test
    @DisplayName("员工打卡 - 上班打卡成功")
    void testPunch_ClockInSuccess() {
        // Given
        mockPunchDTO.setPunchType("上班");
        LocalDate attendanceDate = mockPunchDTO.getPunchTime().toLocalDate();

        when(attendanceRecordRepository.findByEmployeeAndDate(
                mockPunchDTO.getEmployeeId(), attendanceDate)).thenReturn(Optional.empty());
        when(attendanceRuleEngine.validateLocation(
                anyLong(), anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRuleEngine.validateDevice(
                anyLong(), anyString())).thenReturn(true);
        when(attendanceRecordRepository.saveOrUpdate(any(AttendanceRecordEntity.class))).thenReturn(true);

        // When
        ResponseDTO<String> result = attendanceService.punch(mockPunchDTO);

        // Then
        assertTrue(result.getIsOk());
        assertEquals("上班打卡成功", result.getData());
        verify(attendanceCacheManager, times(1)).evictAttendanceRecord(
                mockPunchDTO.getEmployeeId(), attendanceDate);
    }

    @Test
    @DisplayName("员工打卡 - 下班打卡成功")
    void testPunch_ClockOutSuccess() {
        // Given
        mockPunchDTO.setPunchType("下班");
        LocalDate attendanceDate = mockPunchDTO.getPunchTime().toLocalDate();

        // 模拟已有上班记录
        AttendanceRecordEntity existingRecord = createMockAttendanceRecord();
        existingRecord.setPunchInTime(LocalTime.of(9, 0));
        existingRecord.setPunchOutTime(null);

        when(attendanceRecordRepository.findByEmployeeAndDate(
                mockPunchDTO.getEmployeeId(), attendanceDate)).thenReturn(Optional.of(existingRecord));
        when(attendanceRuleEngine.validateLocation(
                anyLong(), anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRuleEngine.validateDevice(
                anyLong(), anyString())).thenReturn(true);
        when(attendanceRecordRepository.saveOrUpdate(any(AttendanceRecordEntity.class))).thenReturn(true);

        // When
        ResponseDTO<String> result = attendanceService.punch(mockPunchDTO);

        // Then
        assertTrue(result.getIsOk());
        assertEquals("下班打卡成功", result.getData());
        verify(attendanceCacheManager, times(1)).evictAttendanceRecord(
                mockPunchDTO.getEmployeeId(), attendanceDate);
    }

    @Test
    @DisplayName("员工打卡 - 位置验证失败")
    void testPunch_LocationValidationFailed() {
        // Given
        when(attendanceRuleEngine.validateLocation(
                anyLong(), anyDouble(), anyDouble())).thenReturn(false);

        // When
        ResponseDTO<String> result = attendanceService.punch(mockPunchDTO);

        // Then
        assertFalse(result.getIsOk());
        assertEquals(UserErrorCode.PARAM_ERROR, result.getErrorCode());
        assertTrue(result.getMsg().contains("打卡位置超出允许范围"));
    }

    @Test
    @DisplayName("员工打卡 - 设备验证失败")
    void testPunch_DeviceValidationFailed() {
        // Given
        when(attendanceRuleEngine.validateLocation(
                anyLong(), anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRuleEngine.validateDevice(
                anyLong(), anyString())).thenReturn(false);

        // When
        ResponseDTO<String> result = attendanceService.punch(mockPunchDTO);

        // Then
        assertFalse(result.getIsOk());
        assertEquals(UserErrorCode.PARAM_ERROR, result.getErrorCode());
        assertTrue(result.getMsg().contains("打卡设备不在允许列表中"));
    }

    @Test
    @DisplayName("创建考勤记录 - 成功")
    void testCreate_Success() {
        // Given
        when(attendanceRecordRepository.findByEmployeeAndDate(
                mockCreateDTO.getEmployeeId(), mockCreateDTO.getAttendanceDate())).thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecordEntity.class))).thenReturn(1L);

        // When
        ResponseDTO<Long> result = attendanceService.create(mockCreateDTO);

        // Then
        assertTrue(result.getIsOk());
        assertEquals(1L, result.getData());
        verify(attendanceCacheManager, times(1)).evictAttendanceRecord(
                mockCreateDTO.getEmployeeId(), mockCreateDTO.getAttendanceDate());
    }

    @Test
    @DisplayName("创建考勤记录 - 当天已存在记录")
    void testCreate_RecordAlreadyExists() {
        // Given
        when(attendanceRecordRepository.findByEmployeeAndDate(
                mockCreateDTO.getEmployeeId(), mockCreateDTO.getAttendanceDate())).thenReturn(Optional.of(mockRecord));

        // When
        ResponseDTO<Long> result = attendanceService.create(mockCreateDTO);

        // Then
        assertFalse(result.getIsOk());
        assertEquals(UserErrorCode.PARAM_ERROR, result.getErrorCode());
        assertTrue(result.getMsg().contains("当天已存在考勤记录"));
    }

    @Test
    @DisplayName("更新考勤记录 - 成功")
    void testUpdate_Success() {
        // Given
        when(attendanceRecordRepository.selectById(mockUpdateDTO.getRecordId())).thenReturn(Optional.of(mockRecord));
        when(attendanceRecordRepository.updateById(any(AttendanceRecordEntity.class))).thenReturn(true);

        // When
        ResponseDTO<Boolean> result = attendanceService.update(mockUpdateDTO);

        // Then
        assertTrue(result.getIsOk());
        assertTrue(result.getData());
        verify(attendanceCacheManager, times(1)).evictAttendanceRecord(
                mockRecord.getEmployeeId(), mockRecord.getAttendanceDate());
    }

    @Test
    @DisplayName("更新考勤记录 - 记录不存在")
    void testUpdate_RecordNotFound() {
        // Given
        when(attendanceRecordRepository.selectById(mockUpdateDTO.getRecordId())).thenReturn(Optional.empty());

        // When
        ResponseDTO<Boolean> result = attendanceService.update(mockUpdateDTO);

        // Then
        assertFalse(result.getIsOk());
        assertEquals(UserErrorCode.DATA_NOT_FOUND, result.getErrorCode());
    }

    @Test
    @DisplayName("删除考勤记录 - 成功")
    void testDelete_Success() {
        // Given
        Long recordId = 1L;
        when(attendanceRecordRepository.selectById(recordId)).thenReturn(Optional.of(mockRecord));
        when(attendanceRecordRepository.deleteById(recordId)).thenReturn(true);

        // When
        ResponseDTO<Boolean> result = attendanceService.delete(recordId);

        // Then
        assertTrue(result.getIsOk());
        assertTrue(result.getData());
        verify(attendanceCacheManager, times(1)).evictAttendanceRecord(
                mockRecord.getEmployeeId(), mockRecord.getAttendanceDate());
    }

    @Test
    @DisplayName("批量删除考勤记录 - 成功")
    void testBatchDelete_Success() {
        // Given
        List<Long> recordIds = Arrays.asList(1L, 2L, 3L);
        when(attendanceRecordRepository.selectById(1L)).thenReturn(Optional.of(mockRecord));
        when(attendanceRecordRepository.selectById(2L)).thenReturn(Optional.of(mockRecord));
        when(attendanceRecordRepository.selectById(3L)).thenReturn(Optional.of(mockRecord));
        when(attendanceRecordRepository.batchDeleteByIds(recordIds)).thenReturn(3);

        // When
        ResponseDTO<Integer> result = attendanceService.batchDelete(recordIds);

        // Then
        assertTrue(result.getIsOk());
        assertEquals(3, result.getData());
        verify(attendanceCacheManager, times(3)).evictAttendanceRecord(anyLong(), any());
    }

    @Test
    @DisplayName("批量删除考勤记录 - ID列表为空")
    void testBatchDelete_EmptyList() {
        // Given
        List<Long> recordIds = new ArrayList<>();

        // When
        ResponseDTO<Integer> result = attendanceService.batchDelete(recordIds);

        // Then
        assertFalse(result.getIsOk());
        assertEquals(UserErrorCode.PARAM_ERROR, result.getErrorCode());
        assertTrue(result.getMsg().contains("记录ID列表不能为空"));
    }

    @Test
    @DisplayName("获取员工考勤统计 - 成功")
    void testGetEmployeeStats_Success() {
        // Given
        Long employeeId = 1001L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalDays", 22);
        mockStats.put("workDays", 20);
        mockStats.put("lateDays", 2);
        mockStats.put("absentDays", 0);

        when(attendanceRecordRepository.selectEmployeeStats(employeeId, startDate, endDate)).thenReturn(mockStats);

        // When
        ResponseDTO<Map<String, Object>> result = attendanceService.getEmployeeStats(employeeId, startDate, endDate);

        // Then
        assertTrue(result.getIsOk());
        assertNotNull(result.getData());
        assertEquals(22, result.getData().get("totalDays"));
        verify(attendanceRecordRepository, times(1)).selectEmployeeStats(employeeId, startDate, endDate);
    }

    @Test
    @DisplayName("获取员工考勤统计 - 日期范围验证失败")
    void testGetEmployeeStats_InvalidDateRange() {
        // Given
        Long employeeId = 1001L;
        LocalDate startDate = LocalDate.of(2025, 1, 31);
        LocalDate endDate = LocalDate.of(2025, 1, 1); // 开始日期大于结束日期

        // When & Then
        SmartException exception = assertThrows(SmartException.class, () -> {
            attendanceService.getEmployeeStats(employeeId, startDate, endDate);
        });
        assertTrue(exception.getMessage().contains("开始日期不能大于结束日期"));
    }

    @Test
    @DisplayName("获取考勤异常列表 - 成功")
    void testGetAbnormalRecords_Success() {
        // Given
        Long employeeId = 1001L;
        Long departmentId = 2001L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        String exceptionType = "LATE";

        List<AttendanceRecordEntity> mockRecords = Arrays.asList(mockRecord);
        when(attendanceRecordRepository.selectAbnormalRecords(
                employeeId, departmentId, startDate, endDate, exceptionType)).thenReturn(mockRecords);

        // When
        ResponseDTO<List<AttendanceRecordVO>> result = attendanceService.getAbnormalRecords(
                employeeId, departmentId, startDate, endDate, exceptionType);

        // Then
        assertTrue(result.getIsOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(attendanceRecordRepository, times(1)).selectAbnormalRecords(
                employeeId, departmentId, startDate, endDate, exceptionType);
    }

    @Test
    @DisplayName("获取迟到记录列表 - 成功")
    void testGetLateRecords_Success() {
        // Given
        Long employeeId = 1001L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        List<AttendanceRecordEntity> mockRecords = Arrays.asList(mockRecord);
        when(attendanceRecordRepository.selectAbnormalRecords(
                eq(employeeId), isNull(), eq(startDate), eq(endDate), eq("LATE"))).thenReturn(mockRecords);

        // When
        ResponseDTO<List<AttendanceRecordVO>> result = attendanceService.getLateRecords(employeeId, null, startDate,
                endDate);

        // Then
        assertTrue(result.getIsOk());
        assertNotNull(result.getData());
        verify(attendanceRecordRepository, times(1)).selectAbnormalRecords(
                employeeId, null, startDate, endDate, "LATE");
    }

    @Test
    @DisplayName("批量重新计算考勤记录 - 成功")
    void testBatchRecalculate_Success() {
        // Given
        Long employeeId = 1001L;
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        List<AttendanceRecordEntity> mockRecords = Arrays.asList(mockRecord);
        when(attendanceRuleEngine.batchCalculateAttendanceRecords(
                eq(employeeId), isNull(), eq("FULL_TIME"), eq(startDate), eq(endDate))).thenReturn(mockRecords);
        when(attendanceRecordRepository.updateById(any(AttendanceRecordEntity.class))).thenReturn(true);

        // When
        ResponseDTO<Integer> result = attendanceService.batchRecalculate(employeeId, startDate, endDate);

        // Then
        assertTrue(result.getIsOk());
        assertEquals(1, result.getData());
        verify(attendanceCacheManager, times(1)).evictAttendanceRecord(employeeId, mockRecord.getAttendanceDate());
    }

    @Test
    @DisplayName("验证打卡合法性 - 成功")
    void testValidatePunch_Success() {
        // Given
        when(attendanceRuleEngine.validateLocation(
                anyLong(), anyDouble(), anyDouble())).thenReturn(true);
        when(attendanceRuleEngine.validateDevice(
                anyLong(), anyString())).thenReturn(true);

        // When
        ResponseDTO<Boolean> result = attendanceService.validatePunch(mockPunchDTO);

        // Then
        assertTrue(result.getIsOk());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("验证打卡合法性 - 位置验证失败")
    void testValidatePunch_LocationFailed() {
        // Given
        when(attendanceRuleEngine.validateLocation(
                anyLong(), anyDouble(), anyDouble())).thenReturn(false);

        // When
        ResponseDTO<Boolean> result = attendanceService.validatePunch(mockPunchDTO);

        // Then
        assertTrue(result.getIsOk());
        assertFalse(result.getData());
    }

    @Test
    @DisplayName("获取考勤日历数据 - 成功")
    void testGetCalendarData_Success() {
        // Given
        Long employeeId = 1001L;
        Integer year = 2025;
        Integer month = 1;

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<AttendanceRecordEntity> mockRecords = Arrays.asList(mockRecord);
        when(attendanceRecordRepository.selectByEmployeeAndDateRange(employeeId, startDate, endDate))
                .thenReturn(mockRecords);

        // When
        ResponseDTO<List<Map<String, Object>>> result = attendanceService.getCalendarData(employeeId, year, month);

        // Then
        assertTrue(result.getIsOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        verify(attendanceRecordRepository, times(1)).selectByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建模拟考勤记录
     */
    private AttendanceRecordEntity createMockAttendanceRecord() {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setRecordId(1L);
        record.setEmployeeId(1001L);
        record.setDepartmentId(2001L);
        record.setAttendanceDate(LocalDate.of(2025, 1, 16));
        record.setPunchInTime(LocalTime.of(9, 0));
        record.setPunchOutTime(LocalTime.of(18, 0));
        record.setAttendanceStatus("NORMAL");
        record.setWorkHours(new BigDecimal("8.0"));
        record.setOvertimeHours(BigDecimal.ZERO);
        record.setPunchInLocation("办公室");
        record.setPunchOutLocation("办公室");
        record.setPunchInDevice("DEV001");
        record.setPunchOutDevice("DEV001");
        return record;
    }

    /**
     * 创建模拟打卡DTO
     */
    private AttendancePunchDTO createMockPunchDTO() {
        AttendancePunchDTO punchDTO = new AttendancePunchDTO();
        punchDTO.setEmployeeId(1001L);
        punchDTO.setPunchTime(LocalDateTime.of(2025, 1, 16, 9, 0));
        punchDTO.setPunchType("上班");
        punchDTO.setLatitude(39.9042);
        punchDTO.setLongitude(116.4074);
        punchDTO.setLocation("办公室");
        punchDTO.setDeviceId("DEV001");
        punchDTO.setPhotoUrl("http://example.com/photo.jpg");
        return punchDTO;
    }

    /**
     * 创建模拟创建DTO
     */
    private AttendanceRecordCreateDTO createMockCreateDTO() {
        AttendanceRecordCreateDTO createDTO = new AttendanceRecordCreateDTO();
        createDTO.setEmployeeId(1001L);
        createDTO.setDepartmentId(2001L);
        createDTO.setAttendanceDate(LocalDate.of(2025, 1, 16));
        createDTO.setPunchInTime(LocalTime.of(9, 0));
        createDTO.setPunchOutTime(LocalTime.of(18, 0));
        createDTO.setAttendanceStatus("NORMAL");
        createDTO.setWorkHours(new BigDecimal("8.0"));
        return createDTO;
    }

    /**
     * 创建模拟更新DTO
     */
    private AttendanceRecordUpdateDTO createMockUpdateDTO() {
        AttendanceRecordUpdateDTO updateDTO = new AttendanceRecordUpdateDTO();
        updateDTO.setRecordId(1L);
        updateDTO.setAttendanceStatus("NORMAL");
        updateDTO.setWorkHours(new BigDecimal("8.5"));
        updateDTO.setRemark("备注信息");
        return updateDTO;
    }
}

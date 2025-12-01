package net.lab1024.sa.attendance.repository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceScheduleDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceScheduleEntity;
// TEMP: Error code functionality disabled
// TEMP: Exception module not yet available
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 排班管理 Repository
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Repository
public class AttendanceScheduleRepository {

    @Resource
    private AttendanceScheduleDao attendanceScheduleDao;

    /**
     * 根据员工ID和日期范围查询排班
     */
    public List<AttendanceScheduleEntity> selectByEmployeeAndDateRange(Long employeeId,
                                                                      LocalDate startDate,
                                                                      LocalDate endDate) {
        try {
            return attendanceScheduleDao.selectByEmployeeAndDateRange(employeeId, startDate, endDate);
        } catch (Exception e) {
            log.error("查询员工排班失败", e);
// TEMP: Error code functionality disabled
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 查询员工指定日期的排班
     */
    public Optional<AttendanceScheduleEntity> selectByEmployeeAndDate(Long employeeId, LocalDate scheduleDate) {
        try {
            AttendanceScheduleEntity schedule = attendanceScheduleDao.selectByEmployeeAndDate(employeeId, scheduleDate);
            return Optional.ofNullable(schedule);
        } catch (Exception e) {
            log.error("查询员工排班失败", e);
// TEMP: Error code functionality disabled
            return Optional.empty();
        }
    }

    /**
     * 保存排班
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(AttendanceScheduleEntity entity) {
        try {
            int result = attendanceScheduleDao.insert(entity);
            if (result <= 0) {
// TEMP: Error code functionality disabled
                throw new RuntimeException("保存排班失败");
            }
            return entity.getScheduleId();
        } catch (Exception e) {
            log.error("保存排班失败", e);
// TEMP: Error code functionality disabled
            throw new RuntimeException("保存排班失败", e);
        }
    }

    /**
     * 批量插入排班
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AttendanceScheduleEntity> schedules) {
        try {
            return attendanceScheduleDao.batchInsert(schedules);
        } catch (Exception e) {
            log.error("批量插入排班失败", e);
// TEMP: Error code functionality disabled
            return 0;
        }
    }

    /**
     * 更新排班
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AttendanceScheduleEntity entity) {
        try {
            int result = attendanceScheduleDao.updateById(entity);
            return result > 0;
        } catch (Exception e) {
            log.error("更新排班失败", e);
// TEMP: Error code functionality disabled
            return false;
        }
    }

    /**
     * 检查排班冲突
     */
    public Integer checkScheduleConflict(Long employeeId, LocalDate scheduleDate,
                                         String workStartTime, String workEndTime,
                                         Long excludeScheduleId) {
        try {
            return attendanceScheduleDao.checkScheduleConflict(employeeId, scheduleDate,
                    workStartTime, workEndTime, excludeScheduleId);
        } catch (Exception e) {
            log.error("检查排班冲突失败", e);
// TEMP: Error code functionality disabled
            return 0;
        }
    }

    /**
     * 查询排班报表
     */
    public List<Map<String, Object>> selectScheduleReport(LocalDate startDate, LocalDate endDate,
                                                          String dimension, Long departmentId) {
        try {
            return attendanceScheduleDao.selectScheduleReport(startDate, endDate, dimension, departmentId);
        } catch (Exception e) {
            log.error("查询排班报表失败", e);
// TEMP: Error code functionality disabled
            return java.util.Collections.emptyList();
        }
    }
}
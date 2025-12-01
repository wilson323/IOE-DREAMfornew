package net.lab1024.sa.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// TEMP: Error code functionality disabled
// TEMP: Exception module not yet available
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceExceptionDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceExceptionEntity;

/**
 * 考勤异常 Repository
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Repository
public class AttendanceExceptionRepository {

    @Resource
    private AttendanceExceptionDao attendanceExceptionDao;

    /**
     * 保存考勤异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(AttendanceExceptionEntity entity) {
        try {
            int result = attendanceExceptionDao.insert(entity);
            if (result <= 0) {
                log.warn("保存考勤异常失败: 插入记录数为0");
                throw new RuntimeException("保存考勤异常失败");
            }
            return entity.getExceptionId();
        } catch (Exception e) {
            log.error("保存考勤异常失败", e);
            throw new RuntimeException("保存考勤异常失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新考勤异常
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AttendanceExceptionEntity entity) {
        try {
            int result = attendanceExceptionDao.updateById(entity);
            return result > 0;
        } catch (Exception e) {
            log.error("更新考勤异常失败", e);
            throw new RuntimeException("更新考勤异常失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询待处理的异常记录
     */
    public List<AttendanceExceptionEntity> selectPendingExceptions(List<Long> departmentIds,
            String exceptionLevel) {
        try {
            return attendanceExceptionDao.selectPendingExceptions(departmentIds, exceptionLevel);
        } catch (Exception e) {
            log.error("查询待处理异常失败", e);
            throw new RuntimeException("查询待处理异常失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量处理异常
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchProcessExceptions(List<Long> exceptionIds, String status, String processType,
            Long processedBy, String processedByName,
            String processRemark, Integer isValid) {
        try {
            return attendanceExceptionDao.batchProcessExceptions(exceptionIds, status, processType,
                    processedBy, processedByName, processRemark, isValid);
        } catch (Exception e) {
            log.error("批量处理异常失败", e);
            throw new RuntimeException("批量处理异常失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询异常报表
     */
    public List<Map<String, Object>> selectExceptionReport(LocalDate startDate, LocalDate endDate,
            String dimension, Long departmentId) {
        try {
            return attendanceExceptionDao.selectExceptionReport(startDate, endDate, dimension, departmentId);
        } catch (Exception e) {
            log.error("查询异常报表失败", e);
            throw new RuntimeException("查询异常报表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 统计员工异常数据
     */
    public Map<String, Object> selectEmployeeExceptionStats(Long employeeId,
            LocalDate startDate, LocalDate endDate) {
        try {
            return attendanceExceptionDao.selectEmployeeExceptionStats(employeeId, startDate, endDate);
        } catch (Exception e) {
            log.error("统计员工异常数据失败", e);
            throw new RuntimeException("统计员工异常数据失败: " + e.getMessage(), e);
        }
    }
}

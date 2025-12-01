package net.lab1024.sa.admin.module.attendance.repository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceStatisticsDao;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceStatisticsEntity;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 考勤统计 Repository
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Repository
public class AttendanceStatisticsRepository {

    @Resource
    private AttendanceStatisticsDao attendanceStatisticsDao;

    /**
     * 保存统计数据
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(AttendanceStatisticsEntity entity) {
        try {
            int result = attendanceStatisticsDao.insert(entity);
            if (result <= 0) {
                throw new SmartException(UserErrorCode.DATA_SAVE_ERROR);
            }
            return entity.getStatisticsId();
        } catch (Exception e) {
            log.error("保存统计数据失败", e);
            throw new SmartException(UserErrorCode.DATA_SAVE_ERROR);
        }
    }

    /**
     * 批量插入统计数据
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<AttendanceStatisticsEntity> statisticsList) {
        try {
            return attendanceStatisticsDao.batchInsert(statisticsList);
        } catch (Exception e) {
            log.error("批量插入统计数据失败", e);
            throw new SmartException(UserErrorCode.DATA_SAVE_ERROR);
        }
    }

    /**
     * 更新统计数据
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AttendanceStatisticsEntity entity) {
        try {
            int result = attendanceStatisticsDao.updateById(entity);
            return result > 0;
        } catch (Exception e) {
            log.error("更新统计数据失败", e);
            throw new SmartException(UserErrorCode.DATA_UPDATE_ERROR);
        }
    }

    /**
     * 批量更新统计数据
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(List<AttendanceStatisticsEntity> statisticsList) {
        try {
            return attendanceStatisticsDao.batchUpdate(statisticsList);
        } catch (Exception e) {
            log.error("批量更新统计数据失败", e);
            throw new SmartException(UserErrorCode.DATA_UPDATE_ERROR);
        }
    }

    /**
     * 查询员工最新统计
     */
    public Optional<AttendanceStatisticsEntity> selectLatestByEmployee(Long employeeId, String statisticsType) {
        try {
            AttendanceStatisticsEntity statistics = attendanceStatisticsDao.selectLatestByEmployee(employeeId, statisticsType);
            return Optional.ofNullable(statistics);
        } catch (Exception e) {
            log.error("查询员工最新统计失败", e);
            throw new SmartException(UserErrorCode.DATA_ERROR);
        }
    }

    /**
     * 查询月度考勤汇总
     */
    public List<Map<String, Object>> selectMonthlySummary(Integer year, Integer month,
                                                         Long departmentId, Long employeeId) {
        try {
            return attendanceStatisticsDao.selectMonthlySummary(year, month, departmentId, employeeId);
        } catch (Exception e) {
            log.error("查询月度考勤汇总失败", e);
            throw new SmartException(UserErrorCode.DATA_ERROR);
        }
    }

    /**
     * 查询员工考勤排名
     */
    public List<Map<String, Object>> selectEmployeeRanking(String statisticsType, String statisticsPeriod,
                                                          Long departmentId, String orderBy, Integer limit) {
        try {
            return attendanceStatisticsDao.selectEmployeeRanking(statisticsType, statisticsPeriod,
                    departmentId, orderBy, limit);
        } catch (Exception e) {
            log.error("查询员工考勤排名失败", e);
            throw new SmartException(UserErrorCode.DATA_ERROR);
        }
    }

    /**
     * 更新计算状态
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateCalculationStatus(List<Long> statisticsIds, String calculationStatus,
                                           java.time.LocalDateTime lastCalculatedTime) {
        try {
            return attendanceStatisticsDao.batchUpdateCalculationStatus(statisticsIds, calculationStatus, lastCalculatedTime);
        } catch (Exception e) {
            log.error("批量更新计算状态失败", e);
            throw new SmartException(UserErrorCode.DATA_UPDATE_ERROR);
        }
    }

    /**
     * 查询统计仪表板数据
     */
    public Map<String, Object> selectDashboardData(Long departmentId) {
        try {
            return attendanceStatisticsDao.selectDashboardData(departmentId);
        } catch (Exception e) {
            log.error("查询仪表板数据失败", e);
            throw new SmartException(UserErrorCode.DATA_ERROR);
        }
    }
}
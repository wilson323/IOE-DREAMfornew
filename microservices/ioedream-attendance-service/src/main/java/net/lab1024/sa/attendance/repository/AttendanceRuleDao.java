package net.lab1024.sa.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// TEMP: Error code functionality disabled
// TEMP: Exception module not yet available
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;

/**
 * 考勤规则 Repository
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Repository
public class AttendanceRuleRepository {

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    /**
     * 查询员工的适用考勤规则
     */
    public List<AttendanceRuleEntity> selectApplicableRules(Long employeeId, Long departmentId,
            String employeeType, LocalDate queryDate) {
        try {
            return attendanceRuleDao.selectApplicableRules(employeeId, departmentId, employeeType, queryDate);
        } catch (Exception e) {
            log.error("查询适用考勤规则失败", e);
            // TEMP: Error code functionality disabled
            return List.of(); // 返回空列表而不是null，避免NPE
        }
    }

    /**
     * 查询员工今日适用规则
     */
    public Optional<AttendanceRuleEntity> selectTodayRule(Long employeeId, Long departmentId, String employeeType) {
        try {
            AttendanceRuleEntity rule = attendanceRuleDao.selectTodayRule(employeeId, departmentId, employeeType);
            return Optional.ofNullable(rule);
        } catch (Exception e) {
            log.error("查询今日适用规则失败", e);
            // TEMP: Error code functionality disabled
            return Optional.empty(); // 返回空的Optional
        }
    }

    /**
     * 保存考勤规则
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(AttendanceRuleEntity entity) {
        try {
            int result = attendanceRuleDao.insert(entity);
            if (result <= 0) {
                // TEMP: Error code functionality disabled
                throw new RuntimeException("保存考勤规则失败，影响行数为0");
            }
            return entity.getRuleId();
        } catch (Exception e) {
            log.error("保存考勤规则失败", e);
            // TEMP: Error code functionality disabled
            throw new RuntimeException("保存考勤规则异常", e);
        }
    }

    /**
     * 更新考勤规则
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AttendanceRuleEntity entity) {
        try {
            int result = attendanceRuleDao.updateById(entity);
            return result > 0;
        } catch (Exception e) {
            log.error("更新考勤规则失败", e);
            // TEMP: Error code functionality disabled
            return false; // 更新失败返回false
        }
    }

    /**
     * 查询规则详情
     */
    public Map<String, Object> selectRuleDetails(Long ruleId) {
        try {
            return attendanceRuleDao.selectRuleDetails(ruleId);
        } catch (Exception e) {
            log.error("查询规则详情失败", e);
            // TEMP: Error code functionality disabled
            return Map.of(); // 返回空的Map
        }
    }

    /**
     * 批量更新规则状态
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> ruleIds, String status) {
        try {
            return attendanceRuleDao.batchUpdateStatus(ruleIds, status);
        } catch (Exception e) {
            log.error("批量更新规则状态失败", e);
            // TEMP: Error code functionality disabled
            return 0; // 更新失败返回0
        }
    }
}

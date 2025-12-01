package net.lab1024.sa.admin.module.attendance.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 考勤规则 Repository
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Repository
public class AttendanceRuleRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttendanceRuleRepository.class);

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
            throw new SmartException(UserErrorCode.DATA_ERROR);
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
            throw new SmartException(UserErrorCode.DATA_ERROR);
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
                throw new SmartException(UserErrorCode.DATA_SAVE_ERROR);
            }
            return entity.getRuleId();
        } catch (Exception e) {
            log.error("保存考勤规则失败", e);
            throw new SmartException(UserErrorCode.DATA_SAVE_ERROR);
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
            throw new SmartException(UserErrorCode.DATA_UPDATE_ERROR);
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
            throw new SmartException(UserErrorCode.DATA_ERROR);
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
            throw new SmartException(UserErrorCode.DATA_UPDATE_ERROR);
        }
    }

    /**
     * 根据员工ID查找适用的考勤规则
     *
     * @param employeeId 员工ID
     * @return 适用的考勤规则
     */
    public Optional<AttendanceRuleEntity> findByEmployeeId(Long employeeId) {
        try {
            LambdaQueryWrapper<AttendanceRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRuleEntity::getStatus, "ACTIVE")
                       .orderByDesc(AttendanceRuleEntity::getCreateTime)
                       .last("LIMIT 1");

            AttendanceRuleEntity rule = attendanceRuleDao.selectOne(queryWrapper);
            return Optional.ofNullable(rule);
        } catch (Exception e) {
            log.error("根据员工ID查找考勤规则失败，employeeId: {}", employeeId, e);
            return Optional.empty();
        }
    }
}
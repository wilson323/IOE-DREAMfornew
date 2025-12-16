package net.lab1024.sa.attendance.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.dao.ScheduleTemplateDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;
import net.lab1024.sa.attendance.domain.entity.ScheduleTemplateEntity;
import net.lab1024.sa.attendance.domain.entity.WorkShiftEntity;
import net.lab1024.sa.attendance.domain.form.ScheduleRecordForm;
import net.lab1024.sa.attendance.domain.form.ScheduleTemplateForm;
import net.lab1024.sa.attendance.domain.form.SmartSchedulingForm;
import net.lab1024.sa.attendance.domain.vo.ScheduleRecordVO;
import net.lab1024.sa.attendance.domain.vo.ScheduleTemplateVO;
import net.lab1024.sa.attendance.domain.vo.SchedulingResultVO;
import net.lab1024.sa.attendance.domain.vo.SchedulingStatisticsVO;
import net.lab1024.sa.attendance.manager.SmartSchedulingEngine;
import net.lab1024.sa.attendance.manager.SmartSchedulingEngine.SchedulingRequest;
import net.lab1024.sa.attendance.manager.SmartSchedulingEngine.SchedulingResult;
import net.lab1024.sa.common.core.exception.BusinessException;
import net.lab1024.sa.common.core.util.SmartBeanUtil;
import net.lab1024.sa.common.core.util.SmartDateUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排班服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ScheduleServiceImpl implements ScheduleService {

    @Resource
    private ScheduleRecordDao scheduleRecordDao;

    @Resource
    private ScheduleTemplateDao scheduleTemplateDao;

    @Resource
    private WorkShiftDao workShiftDao;

    @Resource
    private SmartSchedulingEngine smartSchedulingEngine;

    @Override
    public void createScheduleRecord(ScheduleRecordForm form) {
        log.info("[排班服务] 创建排班记录 employeeId={}, date={}", form.getEmployeeId(), form.getScheduleDate());

        // 1. 验证表单数据
        validateScheduleRecordForm(form);

        // 2. 检查排班冲突
        checkScheduleConflict(form.getEmployeeId(), form.getScheduleDate());

        // 3. 获取班次信息
        WorkShiftEntity shift = workShiftDao.selectById(form.getShiftId());
        if (shift == null) {
            throw new BusinessException("SHIFT_NOT_FOUND", "班次不存在");
        }

        // 4. 创建排班记录
        ScheduleRecordEntity record = SmartBeanUtil.copy(form, ScheduleRecordEntity.class);
        record.setScheduleType("正常排班");
        record.setIsTemporary(false);
        record.setStatus(ScheduleRecordEntity.ScheduleStatus.NORMAL.getCode());
        record.setWorkHours(shift.getWorkHours());
        record.setPriority(1);
        record.setCreateUserId(form.getCreateUserId());

        scheduleRecordDao.insert(record);

        log.info("[排班服务] 排班记录创建成功 scheduleId={}", record.getScheduleId());
    }

    @Override
    public void batchCreateScheduleRecords(List<ScheduleRecordForm> forms) {
        log.info("[排班服务] 批量创建排班记录 count={}", forms.size());

        if (forms.isEmpty()) {
            return;
        }

        // 1. 验证所有表单
        for (ScheduleRecordForm form : forms) {
            validateScheduleRecordForm(form);
        }

        // 2. 批量检查冲突
        for (ScheduleRecordForm form : forms) {
            checkScheduleConflict(form.getEmployeeId(), form.getScheduleDate());
        }

        // 3. 转换并批量插入
        List<ScheduleRecordEntity> records = forms.stream()
                .map(form -> {
                    ScheduleRecordEntity record = SmartBeanUtil.copy(form, ScheduleRecordEntity.class);
                    record.setScheduleType("正常排班");
                    record.setIsTemporary(false);
                    record.setStatus(ScheduleRecordEntity.ScheduleStatus.NORMAL.getCode());

                    // 获取班次信息
                    WorkShiftEntity shift = workShiftDao.selectById(form.getShiftId());
                    if (shift != null) {
                        record.setWorkHours(shift.getWorkHours());
                    }

                    record.setPriority(1);
                    record.setCreateUserId(form.getCreateUserId());
                    return record;
                })
                .collect(Collectors.toList());

        scheduleRecordDao.batchInsert(records);

        log.info("[排班服务] 批量创建排班记录完成 count={}", records.size());
    }

    @Override
    public void updateScheduleRecord(Long scheduleId, ScheduleRecordForm form) {
        log.info("[排班服务] 更新排班记录 scheduleId={}", scheduleId);

        ScheduleRecordEntity record = scheduleRecordDao.selectById(scheduleId);
        if (record == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "排班记录不存在");
        }

        // 如果更改了日期或员工，需要检查冲突
        if (!record.getScheduleDate().equals(form.getScheduleDate()) ||
            !record.getEmployeeId().equals(form.getEmployeeId())) {
            checkScheduleConflict(form.getEmployeeId(), form.getScheduleDate());
        }

        // 更新记录
        SmartBeanUtil.copy(form, record);
        record.setUpdateUserId(form.getCreateUserId()); // 使用form中的createUserId作为updateUserId

        scheduleRecordDao.updateById(record);

        log.info("[排班服务] 排班记录更新成功 scheduleId={}", scheduleId);
    }

    @Override
    public void deleteScheduleRecord(Long scheduleId) {
        log.info("[排班服务] 删除排班记录 scheduleId={}", scheduleId);

        ScheduleRecordEntity record = scheduleRecordDao.selectById(scheduleId);
        if (record == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "排班记录不存在");
        }

        scheduleRecordDao.deleteById(scheduleId);

        log.info("[排班服务] 排班记录删除成功 scheduleId={}", scheduleId);
    }

    @Override
    public List<ScheduleRecordVO> getEmployeeSchedules(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班服务] 查询员工排班 employeeId={}, period={}-{}", employeeId, startDate, endDate);

        List<ScheduleRecordEntity> records = scheduleRecordDao.selectByEmployeeIdAndDateRange(employeeId, startDate, endDate);

        return records.stream()
                .map(this::convertToScheduleRecordVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleRecordVO> getDepartmentSchedules(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班服务] 查询部门排班 departmentId={}, period={}-{}", departmentId, startDate, endDate);

        List<ScheduleRecordEntity> records = scheduleRecordDao.selectByDepartmentAndDateRange(departmentId, startDate, endDate);

        return records.stream()
                .map(this::convertToScheduleRecordVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleRecordVO> getScheduleCalendar(Long departmentId, int year, int month) {
        log.info("[排班服务] 查询排班日历 departmentId={}, year={}, month={}", departmentId, year, month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return getDepartmentSchedules(departmentId, startDate, endDate);
    }

    @Override
    public Long createScheduleTemplate(ScheduleTemplateForm form) {
        log.info("[排班服务] 创建排班模板 name={}", form.getTemplateName());

        ScheduleTemplateEntity template = SmartBeanUtil.copy(form, ScheduleTemplateEntity.class);
        template.setTemplateVersion("1.0");
        template.setApplyCount(0);
        template.setStatus(ScheduleTemplateEntity.TemplateStatus.ENABLED.getCode());
        template.setCreateUserId(form.getCreateUserId());

        scheduleTemplateDao.insert(template);

        log.info("[排班服务] 排班模板创建成功 templateId={}", template.getTemplateId());
        return template.getTemplateId();
    }

    @Override
    public void updateScheduleTemplate(Long templateId, ScheduleTemplateForm form) {
        log.info("[排班服务] 更新排班模板 templateId={}", templateId);

        ScheduleTemplateEntity template = scheduleTemplateDao.selectById(templateId);
        if (template == null) {
            throw new BusinessException("TEMPLATE_NOT_FOUND", "排班模板不存在");
        }

        SmartBeanUtil.copy(form, template);
        template.setUpdateUserId(form.getCreateUserId());

        scheduleTemplateDao.updateById(template);

        log.info("[排班服务] 排班模板更新成功 templateId={}", templateId);
    }

    @Override
    public void deleteScheduleTemplate(Long templateId) {
        log.info("[排班服务] 删除排班模板 templateId={}", templateId);

        ScheduleTemplateEntity template = scheduleTemplateDao.selectById(templateId);
        if (template == null) {
            throw new BusinessException("TEMPLATE_NOT_FOUND", "排班模板不存在");
        }

        scheduleTemplateDao.deleteById(templateId);

        log.info("[排班服务] 排班模板删除成功 templateId={}", templateId);
    }

    @Override
    public List<ScheduleRecordVO> applyScheduleTemplate(Long templateId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班服务] 应用排班模板 templateId={}, period={}-{}", templateId, startDate, endDate);

        List<ScheduleRecordEntity> records = smartSchedulingEngine.applyTemplate(templateId, startDate, endDate);

        return records.stream()
                .map(this::convertToScheduleRecordVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleTemplateVO> getScheduleTemplates(String templateType, Long departmentId) {
        log.info("[排班服务] 查询排班模板 type={}, departmentId={}", templateType, departmentId);

        List<ScheduleTemplateEntity> templates;
        if (templateType != null && !templateType.isEmpty()) {
            templates = scheduleTemplateDao.selectByTemplateType(templateType);
        } else if (departmentId != null) {
            templates = scheduleTemplateDao.selectByDepartmentId(departmentId);
        } else {
            templates = scheduleTemplateDao.selectAllActive();
        }

        return templates.stream()
                .map(this::convertToScheduleTemplateVO)
                .collect(Collectors.toList());
    }

    @Override
    public SchedulingResultVO generateSmartSchedule(SmartSchedulingForm form) {
        log.info("[排班服务] 生成智能排班 departmentId={}", form.getDepartmentId());

        // 1. 构建排班请求
        SchedulingRequest request = new SchedulingRequest();
        request.setDepartmentId(form.getDepartmentId());
        request.setStartDate(form.getStartDate());
        request.setEndDate(form.getEndDate());
        request.setAlgorithmType(form.getAlgorithmType());
        request.setConstraints(form.getConstraints());
        request.setPeriod(SmartDateUtil.formatDate(form.getStartDate(), "yyyy-MM-dd") + "~" + SmartDateUtil.formatDate(form.getEndDate(), "yyyy-MM-dd"));

        // 2. 生成排班方案
        SchedulingResult result = smartSchedulingEngine.generateSmartSchedule(request);

        // 3. 转换为VO
        return convertToSchedulingResultVO(result);
    }

    @Override
    public SchedulingResultVO optimizeSchedule(String requestId) {
        log.info("[排班服务] 优化排班方案 requestId={}", requestId);

        // 这里应该从缓存获取原始排班数据并执行优化
        // 简化实现
        SchedulingResult result = new SchedulingResult();
        result.setRequestId(requestId);

        return convertToSchedulingResultVO(result);
    }

    @Override
    public SchedulingStatisticsVO forecastDemand(Long departmentId, String forecastPeriod) {
        log.info("[排班服务] 预测排班需求 departmentId={}, period={}", departmentId, forecastPeriod);

        // 简化实现
        SchedulingStatisticsVO statistics = new SchedulingStatisticsVO();
        statistics.setDepartmentId(departmentId);
        statistics.setForecastPeriod(forecastPeriod);
        statistics.setTotalEmployees(0);
        statistics.setPredictedWorkHours(0.0);
        statistics.setConfidenceLevel(0.0);

        return statistics;
    }

    @Override
    public SchedulingStatisticsVO getSchedulingStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("[排班服务] 查询排班统计 departmentId={}, period={}-{}", departmentId, startDate, endDate);

        java.util.Map<String, Object> stats = scheduleRecordDao.selectDepartmentStats(departmentId, startDate, endDate);

        SchedulingStatisticsVO statistics = new SchedulingStatisticsVO();
        statistics.setDepartmentId(departmentId);
        statistics.setPeriod(SmartDateUtil.formatDate(startDate, "yyyy-MM-dd") + "~" + SmartDateUtil.formatDate(endDate, "yyyy-MM-dd"));
        statistics.setTotalEmployees((Integer) stats.get("employee_count"));
        statistics.setTotalSchedules((Integer) stats.get("total_schedules"));
        statistics.setActiveSchedules((Integer) stats.get("active_schedules"));
        statistics.setTotalWorkHours(((Number) stats.get("total_hours")).doubleValue());
        statistics.setScheduleUtilization(calculateUtilizationRate(stats));

        return statistics;
    }

    @Override
    public List<String> detectScheduleConflicts(List<Long> employeeIds, LocalDate date) {
        log.info("[排班服务] 冲突检测 employeeCount={}, date={}", employeeIds.size(), date);

        // 简化的冲突检测实现
        List<String> conflicts = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            int count = scheduleRecordDao.countEmployeeScheduleOnDate(employeeId, date);
            if (count > 1) {
                conflicts.add(String.format("员工ID %s 在 %s 存在 %d 个排班冲突", employeeId, date, count));
            }
        }

        return conflicts;
    }

    // ========== 私有方法 ==========

    /**
     * 验证排班记录表单
     */
    private void validateScheduleRecordForm(ScheduleRecordForm form) {
        if (form.getEmployeeId() == null) {
            throw new BusinessException("PARAM_ERROR", "员工ID不能为空");
        }
        if (form.getScheduleDate() == null) {
            throw new BusinessException("PARAM_ERROR", "排班日期不能为空");
        }
        if (form.getShiftId() == null) {
            throw new BusinessException("PARAM_ERROR", "班次ID不能为空");
        }
        if (form.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("PARAM_ERROR", "排班日期不能早于今天");
        }
    }

    /**
     * 检查排班冲突
     */
    private void checkScheduleConflict(Long employeeId, LocalDate date) {
        int count = scheduleRecordDao.countEmployeeScheduleOnDate(employeeId, date);
        if (count > 0) {
            throw new BusinessException("SCHEDULE_CONFLICT", "员工在该日期已有排班安排");
        }
    }

    /**
     * 转换排班记录为VO
     */
    private ScheduleRecordVO convertToScheduleRecordVO(ScheduleRecordEntity entity) {
        ScheduleRecordVO vo = SmartBeanUtil.copy(entity, ScheduleRecordVO.class);

        // 设置枚举描述
        if (entity.getStatus() != null) {
            ScheduleRecordEntity.ScheduleStatus status = ScheduleRecordEntity.ScheduleStatus.fromCode(entity.getStatus());
            vo.setStatusDesc(status.getDescription());
        }

        return vo;
    }

    /**
     * 转换排班模板为VO
     */
    private ScheduleTemplateVO convertToScheduleTemplateVO(ScheduleTemplateEntity entity) {
        ScheduleTemplateVO vo = SmartBeanUtil.copy(entity, ScheduleTemplateVO.class);

        // 设置枚举描述
        if (entity.getTemplateType() != null) {
            ScheduleTemplateEntity.TemplateType type = ScheduleTemplateEntity.TemplateType.fromDescription(entity.getTemplateType());
            vo.setTemplateTypeDesc(type.getDescription());
        }

        if (entity.getStatus() != null) {
            ScheduleTemplateEntity.TemplateStatus status = ScheduleTemplateEntity.TemplateStatus.fromCode(entity.getStatus());
            vo.setStatusDesc(status.getDescription());
        }

        return vo;
    }

    /**
     * 转换智能排班结果为VO
     */
    private SchedulingResultVO convertToSchedulingResultVO(SchedulingResult result) {
        SchedulingResultVO vo = new SchedulingResultVO();
        vo.setRequestId(result.getRequestId());
        vo.setOptimizationScore(result.getOptimizationScore());
        vo.setConflictCount(result.getConflictCount());
        vo.setImprovementRate(result.getImprovementRate());
        vo.setCreatedAt(result.getCreatedAt());
        vo.setStatistics(convertToSchedulingStatisticsVO(result.getStatistics()));

        if (result.getScheduleRecords() != null) {
            vo.setScheduleRecords(result.getScheduleRecords().stream()
                    .map(this::convertToScheduleRecordVO)
                    .collect(Collectors.toList()));
        }

        return vo;
    }

    /**
     * 转换统计信息为VO
     */
    private SchedulingStatisticsVO convertToSchedulingStatisticsVO(SmartSchedulingEngine.SchedulingStatistics statistics) {
        if (statistics == null) {
            return new SchedulingStatisticsVO();
        }

        SchedulingStatisticsVO vo = new SchedulingStatisticsVO();
        vo.setTotalEmployees(statistics.getTotalEmployees());
        vo.setTotalShifts(statistics.getTotalShifts());
        vo.setTotalSchedules(statistics.getTotalSchedules());
        vo.setTotalWorkHours(statistics.getTotalWorkHours());
        vo.setAverageWorkHoursPerEmployee(statistics.getAverageWorkHoursPerEmployee());
        vo.setScheduleUtilization(statistics.getScheduleUtilization());

        return vo;
    }

    /**
     * 计算利用率
     */
    private double calculateUtilizationRate(java.util.Map<String, Object> stats) {
        Integer activeSchedules = (Integer) stats.get("active_schedules");
        Integer totalSchedules = (Integer) stats.get("total_schedules");

        if (totalSchedules != null && totalSchedules > 0) {
            return (double) activeSchedules / totalSchedules * 100;
        }

        return 0.0;
    }
}
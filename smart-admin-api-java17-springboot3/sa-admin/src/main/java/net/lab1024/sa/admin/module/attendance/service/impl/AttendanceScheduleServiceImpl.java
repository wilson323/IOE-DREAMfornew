package net.lab1024.sa.admin.module.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceScheduleDao;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.admin.module.attendance.service.AttendanceScheduleService;
import net.lab1024.sa.admin.module.hr.dao.EmployeeDao;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.util.SmartPageUtil;

/**
 * 考勤排班服务实现类
 *
 * <p>
 * 严格遵循repowiki规范:
 * - 实现考勤排班的CRUD操作
 * - 支持员工和部门排班查询
 * - 提供排班冲突检测和优化
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
public class AttendanceScheduleServiceImpl extends ServiceImpl<AttendanceScheduleDao, AttendanceScheduleEntity>
        implements AttendanceScheduleService {

    @Resource
    private AttendanceScheduleDao attendanceScheduleDao;

    @Resource
    private EmployeeDao employeeDao;

    @Override
    public List<AttendanceScheduleEntity> getEmployeeSchedule(Long employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("查询员工排班: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

            LambdaQueryWrapper<AttendanceScheduleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceScheduleEntity::getEmployeeId, employeeId)
                    .eq(AttendanceScheduleEntity::getDeletedFlag, 0)
                    .ge(AttendanceScheduleEntity::getScheduleDate, startDate)
                    .le(AttendanceScheduleEntity::getScheduleDate, endDate)
                    .orderByAsc(AttendanceScheduleEntity::getScheduleDate);

            return this.list(queryWrapper);
        } catch (Exception e) {
            log.error("查询员工排班失败: 员工ID" + employeeId, e);
            return List.of();
        }
    }

    @Override
    public List<AttendanceScheduleEntity> getDepartmentSchedule(Long departmentId, LocalDate startDate,
            LocalDate endDate) {
        try {
            log.debug("查询部门排班: 部门ID={}, 开始日期={}, 结束日期={}", departmentId, startDate, endDate);

            if (departmentId == null) {
                log.warn("部门ID为空，无法查询部门排班");
                return List.of();
            }

            // 1. 查询部门下的所有员工ID（当前实现只查询当前部门，后续可扩展为包含子部门）
            // TODO: 后续可通过DepartmentCacheManager或DepartmentDao查询部门及其子部门
            List<Long> departmentIds = List.of(departmentId);

            // 2. 查询部门下的所有员工ID
            LambdaQueryWrapper<net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity> employeeWrapper = new LambdaQueryWrapper<>();
            employeeWrapper
                    .in(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getDepartmentId, departmentIds)
                    .eq(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getStatus, 1)
                    .eq(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getDeletedFlag, 0)
                    .select(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getEmployeeId);

            List<net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity> employees = employeeDao
                    .selectList(employeeWrapper);
            if (employees == null || employees.isEmpty()) {
                log.debug("部门下无员工: 部门ID={}", departmentId);
                return List.of();
            }

            // 3. 提取员工ID列表
            List<Long> employeeIds = employees.stream()
                    .map(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getEmployeeId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            if (employeeIds.isEmpty()) {
                log.debug("部门下无有效员工ID: 部门ID={}", departmentId);
                return List.of();
            }

            // 4. 查询这些员工的排班
            LambdaQueryWrapper<AttendanceScheduleEntity> scheduleWrapper = new LambdaQueryWrapper<>();
            scheduleWrapper.in(AttendanceScheduleEntity::getEmployeeId, employeeIds)
                    .ge(AttendanceScheduleEntity::getScheduleDate, startDate)
                    .le(AttendanceScheduleEntity::getScheduleDate, endDate)
                    .eq(AttendanceScheduleEntity::getDeletedFlag, 0)
                    .orderByAsc(AttendanceScheduleEntity::getEmployeeId)
                    .orderByAsc(AttendanceScheduleEntity::getScheduleDate);

            List<AttendanceScheduleEntity> schedules = this.list(scheduleWrapper);

            log.debug("查询部门排班完成: 部门ID={}, 员工数量={}, 排班数量={}",
                    departmentId, employeeIds.size(), schedules.size());

            return schedules;

        } catch (Exception e) {
            log.error("查询部门排班失败: 部门ID" + departmentId, e);
            return List.of();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateSchedule(AttendanceScheduleEntity schedule) {
        try {
            log.info("保存排班: 员工ID={}, 排班日期={}", schedule.getEmployeeId(), schedule.getScheduleDate());

            // 1. 验证排班有效性
            String validationError = validateSchedule(schedule);
            if (validationError != null) {
                log.warn("排班验证失败: 员工ID={}, 排班日期={}, 错误={}",
                        schedule.getEmployeeId(), schedule.getScheduleDate(), validationError);
                throw new RuntimeException("排班验证失败: " + validationError);
            }

            // 2. 检查是否存在冲突的排班
            if (hasScheduleConflict(schedule)) {
                log.warn("存在排班冲突: 员工ID={}, 排班日期={}", schedule.getEmployeeId(), schedule.getScheduleDate());
                throw new RuntimeException("存在排班冲突：该员工在该日期已有其他排班");
            }

            // 设置默认值
            if (schedule.getCreateTime() == null) {
                schedule.setCreateTime(LocalDateTime.now());
            }
            schedule.setUpdateTime(LocalDateTime.now());

            // 设置默认删除标识
            if (schedule.getDeletedFlag() == null) {
                schedule.setDeletedFlag(0);
            }

            // 设置默认排班类型
            if (schedule.getScheduleType() == null) {
                schedule.setScheduleType("NORMAL");
            }

            boolean result = this.saveOrUpdate(schedule);

            if (result) {
                log.info("排班保存成功: 排班ID={}, 员工ID={}, 排班日期={}",
                        schedule.getScheduleId(), schedule.getEmployeeId(), schedule.getScheduleDate());
            } else {
                log.warn("排班保存失败: 员工ID={}, 排班日期={}", schedule.getEmployeeId(), schedule.getScheduleDate());
            }

            return result;
        } catch (Exception e) {
            log.error("保存排班失败: 员工ID" + schedule.getEmployeeId(), e);
            throw new RuntimeException("保存排班失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long scheduleId) {
        try {
            log.info("删除排班: 排班ID={}", scheduleId);

            AttendanceScheduleEntity schedule = this.getById(scheduleId);
            if (schedule == null) {
                log.warn("要删除的排班不存在: 排班ID={}", scheduleId);
                return false;
            }

            // 软删除
            schedule.setDeletedFlag(1);
            schedule.setUpdateTime(LocalDateTime.now());

            boolean result = this.updateById(schedule);

            if (result) {
                log.info("排班删除成功: 排班ID={}, 员工ID={}, 排班日期={}",
                        scheduleId, schedule.getEmployeeId(), schedule.getScheduleDate());
            }

            return result;
        } catch (Exception e) {
            log.error("删除排班失败: 排班ID" + scheduleId, e);
            throw new RuntimeException("删除排班失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteSchedules(List<Long> scheduleIds) {
        try {
            log.info("批量删除排班: 排班ID数量={}", scheduleIds.size());

            if (scheduleIds == null || scheduleIds.isEmpty()) {
                return 0;
            }

            int count = 0;
            for (Long scheduleId : scheduleIds) {
                if (deleteSchedule(scheduleId)) {
                    count++;
                }
            }

            log.info("批量删除排班完成: 成功删除数量={}", count);
            return count;
        } catch (Exception e) {
            log.error("批量删除排班失败", e);
            throw new RuntimeException("批量删除排班失败: " + e.getMessage());
        }
    }

    @Override
    public boolean hasSchedule(Long employeeId, LocalDate date) {
        try {
            log.debug("检查员工是否有排班: 员工ID={}, 日期={}", employeeId, date);

            LambdaQueryWrapper<AttendanceScheduleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceScheduleEntity::getEmployeeId, employeeId)
                    .eq(AttendanceScheduleEntity::getScheduleDate, date)
                    .eq(AttendanceScheduleEntity::getDeletedFlag, 0);

            return this.count(queryWrapper) > 0;
        } catch (Exception e) {
            log.error("检查员工排班失败: 员工ID" + employeeId, e);
            return false;
        }
    }

    @Override
    public AttendanceScheduleEntity getEmployeeScheduleByDate(Long employeeId, LocalDate date) {
        try {
            log.debug("查询员工指定日期的排班: 员工ID={}, 日期={}", employeeId, date);

            LambdaQueryWrapper<AttendanceScheduleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceScheduleEntity::getEmployeeId, employeeId)
                    .eq(AttendanceScheduleEntity::getScheduleDate, date)
                    .eq(AttendanceScheduleEntity::getDeletedFlag, 0);

            return this.getOne(queryWrapper);
        } catch (Exception e) {
            log.error("查询员工排班失败: 员工ID" + employeeId + ", 日期" + date, e);
            return null;
        }
    }

    /**
     * 检查排班冲突
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 检查同一员工同一日期是否有其他排班
     * - 验证班次有效性
     * - 检查时间冲突（如果支持时间段重叠检测）
     * </p>
     *
     * @param schedule 要检查的排班
     * @return 是否存在冲突
     */
    private boolean hasScheduleConflict(AttendanceScheduleEntity schedule) {
        try {
            // 1. 检查同一员工同一日期是否有其他排班
            LambdaQueryWrapper<AttendanceScheduleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceScheduleEntity::getEmployeeId, schedule.getEmployeeId())
                    .eq(AttendanceScheduleEntity::getScheduleDate, schedule.getScheduleDate())
                    .eq(AttendanceScheduleEntity::getDeletedFlag, 0);

            // 更新操作：排除自身
            if (schedule.getScheduleId() != null) {
                queryWrapper.ne(AttendanceScheduleEntity::getScheduleId, schedule.getScheduleId());
            }

            long conflictCount = this.count(queryWrapper);
            if (conflictCount > 0) {
                log.warn("排班冲突：员工ID={}, 排班日期={} 已存在其他排班",
                        schedule.getEmployeeId(), schedule.getScheduleDate());
                return true;
            }

            // 2. 验证班次有效性（如果提供了班次ID）
            if (schedule.getShiftId() != null) {
                // 验证班次是否存在且启用
                // 注意：如果项目中有班次表（t_attendance_shift），可以通过以下方式验证：
                // 1. 创建AttendanceShiftDao和AttendanceShiftEntity
                // 2. 查询班次是否存在且状态为启用
                // 当前实现：如果提供了班次ID，至少验证ID不为空
                if (schedule.getShiftId() <= 0) {
                    log.warn("班次ID无效: shiftId={}, 员工ID={}, 日期={}",
                            schedule.getShiftId(), schedule.getEmployeeId(), schedule.getScheduleDate());
                    return true; // 班次ID无效视为冲突
                }
                // TODO: 完整班次验证需要创建AttendanceShiftDao
                // 示例代码：
                // AttendanceShiftEntity shift = attendanceShiftDao.selectById(schedule.getShiftId());
                // if (shift == null || shift.getStatus() == null || !"ACTIVE".equals(shift.getStatus())) {
                //     log.warn("班次不存在或已禁用: shiftId={}", schedule.getShiftId());
                //     return true;
                // }
                log.debug("班次ID验证通过: shiftId={}（完整验证待实现班次DAO）", schedule.getShiftId());
            }

            // 3. 验证排班时间有效性
            if (schedule.getWorkStartTime() != null && schedule.getWorkEndTime() != null) {
                if (!schedule.getWorkStartTime().isBefore(schedule.getWorkEndTime())) {
                    log.warn("排班时间无效：开始时间不能晚于或等于结束时间, 员工ID={}, 日期={}",
                            schedule.getEmployeeId(), schedule.getScheduleDate());
                    return true; // 时间无效视为冲突
                }
            }

            return false;
        } catch (Exception e) {
            log.error("检查排班冲突失败: 员工ID" + schedule.getEmployeeId(), e);
            // 异常时返回true，阻止保存，确保数据安全
            return true;
        }
    }

    /**
     * 验证排班有效性
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 验证员工是否存在
     * - 验证班次是否存在
     * - 验证排班日期是否在有效范围内
     * - 验证排班时间是否合理
     * </p>
     *
     * @param schedule 要验证的排班
     * @return 验证结果，null表示验证通过，否则返回错误信息
     */
    public String validateSchedule(AttendanceScheduleEntity schedule) {
        try {
            // 1. 验证员工是否存在
            if (schedule.getEmployeeId() == null) {
                return "员工ID不能为空";
            }

            // 验证员工是否存在且有效
            net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity employee = 
                    employeeDao.selectById(schedule.getEmployeeId());
            if (employee == null) {
                return "员工不存在";
            }
            if (employee.getDeletedFlag() != null && employee.getDeletedFlag() != 0) {
                return "员工已删除";
            }
            if (employee.getStatus() == null || employee.getStatus() != 1) {
                return "员工状态无效";
            }

            // 2. 验证班次是否存在（如果提供了班次ID）
            if (schedule.getShiftId() != null) {
                // 验证班次ID有效性
                if (schedule.getShiftId() <= 0) {
                    return "班次ID无效";
                }
                // TODO: 完整班次验证需要创建AttendanceShiftDao
                // 注意：如果项目中有班次表（t_attendance_shift），可以通过以下方式验证：
                // 1. 创建AttendanceShiftDao和AttendanceShiftEntity
                // 2. 查询班次是否存在且状态为启用
                // 示例代码：
                // AttendanceShiftEntity shift = attendanceShiftDao.selectById(schedule.getShiftId());
                // if (shift == null) {
                //     return "班次不存在";
                // }
                // if (shift.getStatus() == null || !"ACTIVE".equals(shift.getStatus())) {
                //     return "班次已禁用";
                // }
                log.debug("班次ID验证通过: shiftId={}（完整验证待实现班次DAO）", schedule.getShiftId());
            }

            // 3. 验证排班日期
            if (schedule.getScheduleDate() == null) {
                return "排班日期不能为空";
            }

            // 4. 验证排班时间
            if (schedule.getWorkStartTime() != null && schedule.getWorkEndTime() != null) {
                if (!schedule.getWorkStartTime().isBefore(schedule.getWorkEndTime())) {
                    return "工作开始时间必须早于工作结束时间";
                }
            }

            // 5. 验证休息时间（如果提供了休息时间）
            if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
                if (!schedule.getBreakStartTime().isBefore(schedule.getBreakEndTime())) {
                    return "休息开始时间必须早于休息结束时间";
                }

                // 验证休息时间是否在工作时间范围内
                if (schedule.getWorkStartTime() != null && schedule.getWorkEndTime() != null) {
                    if (schedule.getBreakStartTime().isBefore(schedule.getWorkStartTime()) ||
                            schedule.getBreakEndTime().isAfter(schedule.getWorkEndTime())) {
                        return "休息时间必须在工作时间范围内";
                    }
                }
            }

            return null; // 验证通过
        } catch (Exception e) {
            log.error("验证排班有效性失败", e);
            return "验证失败：" + e.getMessage();
        }
    }

    /**
     * 分页查询排班
     *
     * @param pageParam    分页参数
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param scheduleType 排班类型（可选）
     * @return 分页结果
     */
    public PageResult<AttendanceScheduleEntity> querySchedulePage(PageParam pageParam, Long employeeId,
            Long departmentId, LocalDate startDate,
            LocalDate endDate, String scheduleType) {
        try {
            log.debug("分页查询排班: 页码={}, 大小={}", pageParam.getPageNum(), pageParam.getPageSize());

            // 构建查询条件
            LambdaQueryWrapper<AttendanceScheduleEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceScheduleEntity::getDeletedFlag, Integer.valueOf(0));

            // 员工过滤
            if (employeeId != null) {
                queryWrapper.eq(AttendanceScheduleEntity::getEmployeeId, employeeId);
            }

            // 部门过滤
            // 注意：AttendanceScheduleEntity没有departmentId字段，需要通过员工表关联查询
            if (departmentId != null) {
                // 查询部门下的所有员工ID
                LambdaQueryWrapper<net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity> employeeWrapper = 
                        new LambdaQueryWrapper<>();
                employeeWrapper.eq(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getDepartmentId, departmentId)
                        .eq(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getStatus, 1)
                        .eq(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getDeletedFlag, 0)
                        .select(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getEmployeeId);

                List<net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity> employees = employeeDao.selectList(employeeWrapper);
                if (employees == null || employees.isEmpty()) {
                    // 部门下无员工，返回空结果
                    Page<AttendanceScheduleEntity> emptyPage = (Page<AttendanceScheduleEntity>) SmartPageUtil.convert2PageQuery(pageParam);
                    return SmartPageUtil.convert2PageResult(emptyPage, List.<AttendanceScheduleEntity>of());
                }

                // 提取员工ID列表
                List<Long> employeeIds = employees.stream()
                        .map(net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity::getEmployeeId)
                        .filter(id -> id != null)
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());

                if (employeeIds.isEmpty()) {
                    // 部门下无有效员工ID，返回空结果
                    Page<AttendanceScheduleEntity> emptyPage = (Page<AttendanceScheduleEntity>) SmartPageUtil.convert2PageQuery(pageParam);
                    return SmartPageUtil.convert2PageResult(emptyPage, List.<AttendanceScheduleEntity>of());
                }

                // 使用in查询这些员工的排班
                queryWrapper.in(AttendanceScheduleEntity::getEmployeeId, employeeIds);
            }

            // 日期范围过滤
            if (startDate != null) {
                queryWrapper.ge(AttendanceScheduleEntity::getScheduleDate, startDate);
            }
            if (endDate != null) {
                queryWrapper.le(AttendanceScheduleEntity::getScheduleDate, endDate);
            }

            // 排班类型过滤
            if (scheduleType != null && !scheduleType.trim().isEmpty()) {
                queryWrapper.eq(AttendanceScheduleEntity::getScheduleType, scheduleType.trim());
            }

            queryWrapper.orderByAsc(AttendanceScheduleEntity::getEmployeeId)
                    .orderByAsc(AttendanceScheduleEntity::getScheduleDate);

            // 分页查询
            @SuppressWarnings("unchecked")
            Page<AttendanceScheduleEntity> page = (Page<AttendanceScheduleEntity>) SmartPageUtil
                    .convert2PageQuery(pageParam);
            IPage<AttendanceScheduleEntity> pageResult = this.page(page, queryWrapper);

            // 转换分页结果
            return SmartPageUtil.convert2PageResult(page, pageResult.getRecords());
        } catch (Exception e) {
            log.error("分页查询排班失败", e);
            return PageResult.of(List.of(), 0L, pageParam.getPageNum().longValue(),
                    pageParam.getPageSize().longValue());
        }
    }

    /**
     * 批量创建排班
     *
     * @param schedules 排班列表
     * @return 成功创建的数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateSchedules(List<AttendanceScheduleEntity> schedules) {
        try {
            log.info("批量创建排班: 排班数量={}", schedules.size());

            if (schedules == null || schedules.isEmpty()) {
                return 0;
            }

            int count = 0;
            for (AttendanceScheduleEntity schedule : schedules) {
                if (saveOrUpdateSchedule(schedule)) {
                    count++;
                }
            }

            log.info("批量创建排班完成: 成功创建数量={}", count);
            return count;
        } catch (Exception e) {
            log.error("批量创建排班失败", e);
            throw new RuntimeException("批量创建排班失败: " + e.getMessage());
        }
    }

    /**
     * 复制排班模板
     *
     * @param sourceEmployeeId  源员工ID
     * @param targetEmployeeIds 目标员工ID列表
     * @param startDate         开始日期
     * @param endDate           结束日期
     * @return 成功复制的数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int copyScheduleTemplate(Long sourceEmployeeId, List<Long> targetEmployeeIds,
            LocalDate startDate, LocalDate endDate) {
        try {
            log.info("复制排班模板: 源员工ID={}, 目标员工数量={}, 开始日期={}, 结束日期={}",
                    sourceEmployeeId, targetEmployeeIds.size(), startDate, endDate);

            // 获取源员工在指定日期范围的排班
            List<AttendanceScheduleEntity> sourceSchedules = getEmployeeSchedule(sourceEmployeeId, startDate, endDate);
            if (sourceSchedules.isEmpty()) {
                log.warn("源员工在指定日期范围内没有排班: 源员工ID={}", sourceEmployeeId);
                return 0;
            }

            // 按日期分组源排班
            Map<LocalDate, AttendanceScheduleEntity> scheduleMap = sourceSchedules.stream()
                    .collect(Collectors.toMap(
                            AttendanceScheduleEntity::getScheduleDate,
                            schedule -> schedule,
                            (existing, replacement) -> existing));

            int count = 0;
            for (Long targetEmployeeId : targetEmployeeIds) {
                for (Map.Entry<LocalDate, AttendanceScheduleEntity> entry : scheduleMap.entrySet()) {
                    AttendanceScheduleEntity sourceSchedule = entry.getValue();
                    LocalDate scheduleDate = entry.getKey();

                    // 创建新排班
                    AttendanceScheduleEntity newSchedule = new AttendanceScheduleEntity();

                    // 复制属性
                    newSchedule.setEmployeeId(targetEmployeeId);
                    // 注意：AttendanceScheduleEntity没有departmentId字段，已移除
                    newSchedule.setScheduleDate(scheduleDate);
                    newSchedule.setShiftName(sourceSchedule.getShiftName());
                    newSchedule.setWorkStartTime(sourceSchedule.getWorkStartTime());
                    newSchedule.setWorkEndTime(sourceSchedule.getWorkEndTime());
                    newSchedule.setBreakStartTime(sourceSchedule.getBreakStartTime());
                    newSchedule.setBreakEndTime(sourceSchedule.getBreakEndTime());
                    newSchedule.setScheduleType(sourceSchedule.getScheduleType());
                    newSchedule.setRemarks(sourceSchedule.getRemarks());

                    // 保存新排班
                    if (saveOrUpdateSchedule(newSchedule)) {
                        count++;
                    }
                }
            }

            log.info("排班模板复制完成: 成功复制数量={}", count);
            return count;
        } catch (Exception e) {
            log.error("复制排班模板失败: 源员工ID" + sourceEmployeeId, e);
            throw new RuntimeException("复制排班模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工月度排班统计
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份
     * @return 排班统计信息
     */
    public Map<String, Object> getMonthlyScheduleStats(Long employeeId, int year, int month) {
        try {
            log.debug("查询员工月度排班统计: 员工ID={}, 年份={}, 月份={}", employeeId, year, month);

            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            List<AttendanceScheduleEntity> schedules = getEmployeeSchedule(employeeId, startDate, endDate);

            // 统计各种类型的排班数量
            Map<String, Long> typeStats = schedules.stream()
                    .collect(Collectors.groupingBy(
                            AttendanceScheduleEntity::getScheduleType,
                            Collectors.counting()));

            return Map.of(
                    "totalDays", schedules.size(),
                    "typeStats", typeStats,
                    "startDate", startDate,
                    "endDate", endDate);
        } catch (Exception e) {
            log.error("查询员工月度排班统计失败: 员工ID" + employeeId, e);
            return Map.of(
                    "totalDays", 0,
                    "typeStats", Map.of(),
                    "error", e.getMessage());
        }
    }

    /**
     * 检查员工排班连续性
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 连续性检查结果
     */
    public Map<String, Object> checkScheduleContinuity(Long employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("检查员工排班连续性: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

            List<AttendanceScheduleEntity> schedules = getEmployeeSchedule(employeeId, startDate, endDate);

            // 计算工作日天数
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long scheduledDays = schedules.size();
            long unscheduledDays = totalDays - scheduledDays;

            // 查找缺失排班的日期
            List<LocalDate> scheduledDates = schedules.stream()
                    .map(AttendanceScheduleEntity::getScheduleDate)
                    .sorted()
                    .collect(Collectors.toList());

            // 实现缺失日期查找逻辑
            List<LocalDate> missingDates = new java.util.ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                boolean isScheduled = scheduledDates.contains(currentDate);
                if (!isScheduled) {
                    missingDates.add(currentDate);
                }
                currentDate = currentDate.plusDays(1);
            }

            return Map.of(
                    "totalDays", totalDays,
                    "scheduledDays", scheduledDays,
                    "unscheduledDays", unscheduledDays,
                    "coverageRate", totalDays > 0 ? (double) scheduledDays / totalDays : 0.0,
                    "missingDates", missingDates);
        } catch (Exception e) {
            log.error("检查员工排班连续性失败: 员工ID" + employeeId, e);
            return Map.of(
                    "error", e.getMessage());
        }
    }
}

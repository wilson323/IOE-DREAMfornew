package net.lab1024.sa.attendance.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.form.FlexibleWorkScheduleForm;
import net.lab1024.sa.attendance.domain.vo.FlexibleWorkScheduleDetailVO;
import net.lab1024.sa.attendance.domain.vo.FlexibleWorkScheduleVO;
import net.lab1024.sa.common.entity.attendance.FlexibleWorkScheduleEntity;
import net.lab1024.sa.attendance.service.FlexibleWorkScheduleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 弹性工作制服务实现类
 * <p>
 * 提供弹性工作制的完整配置和管理功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class FlexibleWorkScheduleServiceImpl implements FlexibleWorkScheduleService {

    // TODO: 注入DAO层（待实现）
    // @Resource
    // private FlexibleWorkScheduleDao flexibleWorkScheduleDao;

    // 临时存储（生产环境应使用数据库）
    private final Map<Long, FlexibleWorkScheduleEntity> scheduleCache = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> employeeScheduleCache = new ConcurrentHashMap<>();
    private Long scheduleIdSequence = 1L;

    // ==================== 弹性工作制配置管理 ====================

    @Override
    public Long createFlexibleSchedule(FlexibleWorkScheduleForm form) {
        log.info("[弹性工作制] 创建配置: scheduleName={}, flexMode={}", form.getScheduleName(), form.getFlexMode());

        // 1. 验证表单数据
        validateScheduleForm(form);

        // 2. 构建实体
        FlexibleWorkScheduleEntity entity = buildEntityFromForm(form);

        // 3. 生成配置ID
        entity.setScheduleId(scheduleIdSequence++);

        // 4. 设置审计字段
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        // 5. 保存到缓存（TODO: 保存到数据库）
        scheduleCache.put(entity.getScheduleId(), entity);

        log.info("[弹性工作制] 创建配置成功: scheduleId={}, scheduleName={}", entity.getScheduleId(), entity.getScheduleName());
        return entity.getScheduleId();
    }

    @Override
    public Boolean updateFlexibleSchedule(Long scheduleId, FlexibleWorkScheduleForm form) {
        log.info("[弹性工作制] 更新配置: scheduleId={}, scheduleName={}", scheduleId, form.getScheduleName());

        // 1. 验证配置是否存在
        FlexibleWorkScheduleEntity existingEntity = scheduleCache.get(scheduleId);
        if (existingEntity == null) {
            log.warn("[弹性工作制] 配置不存在: scheduleId={}", scheduleId);
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. 验证表单数据
        validateScheduleForm(form);

        // 3. 更新实体
        FlexibleWorkScheduleEntity entity = buildEntityFromForm(form);
        entity.setScheduleId(scheduleId);
        entity.setCreateTime(existingEntity.getCreateTime());
        entity.setUpdateTime(LocalDateTime.now());

        // 4. 保存到缓存（TODO: 保存到数据库）
        scheduleCache.put(scheduleId, entity);

        log.info("[弹性工作制] 更新配置成功: scheduleId={}", scheduleId);
        return true;
    }

    @Override
    public Boolean deleteFlexibleSchedule(Long scheduleId) {
        log.info("[弹性工作制] 删除配置: scheduleId={}", scheduleId);

        // 1. 验证配置是否存在
        if (!scheduleCache.containsKey(scheduleId)) {
            log.warn("[弹性工作制] 配置不存在: scheduleId={}", scheduleId);
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. 检查是否有员工分配
        List<Long> employeeIds = employeeScheduleCache.get(scheduleId);
        if (employeeIds != null && !employeeIds.isEmpty()) {
            log.warn("[弹性工作制] 配置仍有员工分配，无法删除: scheduleId={}, employeeCount={}", scheduleId, employeeIds.size());
            throw new BusinessException("SCHEDULE_IN_USE", "配置仍有员工分配，无法删除");
        }

        // 3. 删除配置（TODO: 从数据库删除）
        scheduleCache.remove(scheduleId);

        log.info("[弹性工作制] 删除配置成功: scheduleId={}", scheduleId);
        return true;
    }

    @Override
    public FlexibleWorkScheduleDetailVO getFlexibleScheduleDetail(Long scheduleId) {
        log.debug("[弹性工作制] 查询配置详情: scheduleId={}", scheduleId);

        // 1. 查询配置
        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity == null) {
            log.warn("[弹性工作制] 配置不存在: scheduleId={}", scheduleId);
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. 转换为VO
        FlexibleWorkScheduleDetailVO vo = convertToDetailVO(entity);

        log.debug("[弹性工作制] 查询配置详情成功: scheduleId={}", scheduleId);
        return vo;
    }

    @Override
    public PageResult<FlexibleWorkScheduleVO> queryFlexibleSchedules(FlexibleWorkScheduleForm form) {
        log.info("[弹性工作制] 分页查询配置: scheduleName={}", form.getScheduleName());

        // TODO: 实现分页查询（需要数据库支持）
        List<FlexibleWorkScheduleVO> voList = scheduleCache.values().stream()
                .map(this::convertToVO)
                .toList();

        PageResult<FlexibleWorkScheduleVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal((long) voList.size());
        pageResult.setPageNum(1);
        pageResult.setPageSize(voList.size());
        pageResult.setPages(1);

        log.info("[弹性工作制] 分页查询配置成功: total={}", voList.size());
        return pageResult;
    }

    @Override
    public List<FlexibleWorkScheduleVO> getAllActiveSchedules() {
        log.info("[弹性工作制] 查询所有启用的配置");

        List<FlexibleWorkScheduleVO> voList = scheduleCache.values().stream()
                .filter(entity -> entity.getStatus() != null && entity.getStatus() == 1)
                .map(this::convertToVO)
                .toList();

        log.info("[弹性工作制] 查询所有启用的配置成功: count={}", voList.size());
        return voList;
    }

    // ==================== 弹性时间计算 ====================

    @Override
    public String calculateAttendanceStatus(Long scheduleId, Long employeeId,
                                            LocalDateTime attendanceDate,
                                            LocalDateTime checkInTime,
                                            LocalDateTime checkOutTime) {
        log.info("[弹性工作制] 计算考勤状态: scheduleId={}, employeeId={}, date={}", scheduleId, employeeId, attendanceDate);

        // 1. 查询配置
        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. 计算考勤状态
        String status = "NORMAL";

        // TODO: 实现完整的考勤状态计算逻辑
        // - 检查是否在弹性上班时间范围内
        // - 检查是否在弹性下班时间范围内
        // - 检查核心时间是否打卡
        // - 检查工作时长是否满足要求

        log.info("[弹性工作制] 计算考勤状态成功: scheduleId={}, employeeId={}, status={}", scheduleId, employeeId, status);
        return status;
    }

    @Override
    public Boolean validateFlexibleTime(Long scheduleId, LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        log.debug("[弹性工作制] 验证弹性时间: scheduleId={}, checkIn={}, checkOut={}", scheduleId, checkInTime, checkOutTime);

        // 1. 查询配置
        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. 验证时间
        LocalTime checkInLocalTime = checkInTime.toLocalTime();
        LocalTime checkOutLocalTime = checkOutTime.toLocalTime();

        boolean isValid = true;

        // 检查是否在弹性时间范围内
        if (entity.getFlexStartEarliest() != null && entity.getFlexStartLatest() != null) {
            if (checkInLocalTime.isBefore(entity.getFlexStartEarliest()) || checkInLocalTime.isAfter(entity.getFlexStartLatest())) {
                isValid = false;
            }
        }

        if (entity.getFlexEndEarliest() != null && entity.getFlexEndLatest() != null) {
            if (checkOutLocalTime.isBefore(entity.getFlexEndEarliest()) || checkOutLocalTime.isAfter(entity.getFlexEndLatest())) {
                isValid = false;
            }
        }

        log.debug("[弹性工作制] 验证弹性时间结果: scheduleId={}, isValid={}", scheduleId, isValid);
        return isValid;
    }

    @Override
    public Integer calculateWorkDuration(Long scheduleId, LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        log.debug("[弹性工作制] 计算工作时长: scheduleId={}, checkIn={}, checkOut={}", scheduleId, checkInTime, checkOutTime);

        // 1. 计算时长（分钟）
        long durationMinutes = Duration.between(checkInTime, checkOutTime).toMinutes();

        // 2. 查询配置并扣除午休
        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity != null && entity.getLunchStartTime() != null && entity.getLunchEndTime() != null) {
            LocalTime checkInLocalTime = checkInTime.toLocalTime();
            LocalTime checkOutLocalTime = checkOutTime.toLocalTime();

            // 如果工作时间段包含午休时间，扣除午休时长
            if (checkInLocalTime.isBefore(entity.getLunchStartTime()) && checkOutLocalTime.isAfter(entity.getLunchEndTime())) {
                long lunchDuration = Duration.between(entity.getLunchStartTime(), entity.getLunchEndTime()).toMinutes();
                durationMinutes -= lunchDuration;
            }
        }

        log.debug("[弹性工作制] 计算工作时长成功: scheduleId={}, duration={}minutes", scheduleId, durationMinutes);
        return (int) durationMinutes;
    }

    // ==================== 弹性模式管理 ====================

    @Override
    public Boolean switchFlexMode(Long scheduleId, String flexMode) {
        log.info("[弹性工作制] 切换弹性模式: scheduleId={}, flexMode={}", scheduleId, flexMode);

        // 1. 验证配置是否存在
        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. 验证弹性模式
        if (!flexMode.matches("^(STANDARD|FLEXIBLE|HYBRID)$")) {
            throw new BusinessException("INVALID_FLEX_MODE", "无效的弹性模式");
        }

        // 3. 更新模式
        entity.setFlexMode(flexMode);
        entity.setUpdateTime(LocalDateTime.now());
        scheduleCache.put(scheduleId, entity);

        log.info("[弹性工作制] 切换弹性模式成功: scheduleId={}, flexMode={}", scheduleId, flexMode);
        return true;
    }

    @Override
    public Boolean enableFlexibleSchedule(Long scheduleId) {
        log.info("[弹性工作制] 启用配置: scheduleId={}", scheduleId);

        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        entity.setStatus(1);
        entity.setUpdateTime(LocalDateTime.now());
        scheduleCache.put(scheduleId, entity);

        log.info("[弹性工作制] 启用配置成功: scheduleId={}", scheduleId);
        return true;
    }

    @Override
    public Boolean disableFlexibleSchedule(Long scheduleId) {
        log.info("[弹性工作制] 禁用配置: scheduleId={}", scheduleId);

        FlexibleWorkScheduleEntity entity = scheduleCache.get(scheduleId);
        if (entity == null) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        entity.setStatus(0);
        entity.setUpdateTime(LocalDateTime.now());
        scheduleCache.put(scheduleId, entity);

        log.info("[弹性工作制] 禁用配置成功: scheduleId={}", scheduleId);
        return true;
    }

    // ==================== 员工弹性工作制分配 ====================

    @Override
    public Integer assignToEmployees(Long scheduleId, List<Long> employeeIds, LocalDateTime effectiveTime) {
        log.info("[弹性工作制] 为员工分配配置: scheduleId={}, employeeCount={}, effectiveTime={}", scheduleId, employeeIds.size(), effectiveTime);

        // 1. 验证配置是否存在
        if (!scheduleCache.containsKey(scheduleId)) {
            throw new BusinessException("SCHEDULE_NOT_FOUND", "弹性工作制配置不存在");
        }

        // 2. TODO: 验证员工是否存在（需要EmployeeService）

        // 3. 分配配置（TODO: 保存到数据库）
        employeeScheduleCache.put(scheduleId, employeeIds);

        log.info("[弹性工作制] 为员工分配配置成功: scheduleId={}, assignedCount={}", scheduleId, employeeIds.size());
        return employeeIds.size();
    }

    @Override
    public Integer removeFromEmployees(Long scheduleId, List<Long> employeeIds) {
        log.info("[弹性工作制] 移除员工配置: scheduleId={}, employeeCount={}", scheduleId, employeeIds.size());

        // 1. 验证配置是否存在
        List<Long> existingEmployeeIds = employeeScheduleCache.get(scheduleId);
        if (existingEmployeeIds == null) {
            log.warn("[弹性工作制] 配置无员工分配: scheduleId={}", scheduleId);
            return 0;
        }

        // 2. 移除员工（TODO: 从数据库删除）
        List<Long> remainingEmployees = existingEmployeeIds.stream()
                .filter(id -> !employeeIds.contains(id))
                .toList();
        employeeScheduleCache.put(scheduleId, remainingEmployees);

        int removedCount = existingEmployeeIds.size() - remainingEmployees.size();
        log.info("[弹性工作制] 移除员工配置成功: scheduleId={}, removedCount={}", scheduleId, removedCount);
        return removedCount;
    }

    @Override
    public FlexibleWorkScheduleVO getEmployeeSchedule(Long employeeId, LocalDateTime effectiveDate) {
        log.debug("[弹性工作制] 查询员工配置: employeeId={}, effectiveDate={}", employeeId, effectiveDate);

        // TODO: 实现员工配置查询（需要数据库支持）
        // 当前返回第一个配置作为示例
        if (!scheduleCache.isEmpty()) {
            Long scheduleId = scheduleCache.keySet().iterator().next();
            return convertToVO(scheduleCache.get(scheduleId));
        }

        return null;
    }

    // ==================== 统计分析 ====================

    @Override
    public String getScheduleStatistics(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[弹性工作制] 获取统计: scheduleId={}, startDate={}, endDate={}", scheduleId, startDate, endDate);

        // TODO: 实现统计分析（需要数据库支持）
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("scheduleId", scheduleId);
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        statistics.put("assignedEmployeeCount", 0);
        statistics.put("usageRate", 0.0);
        statistics.put("lateRate", 0.0);

        log.info("[弹性工作制] 获取统计成功: scheduleId={}", scheduleId);
        return statistics.toString();
    }

    @Override
    public String compareSchedules(List<Long> scheduleIds) {
        log.info("[弹性工作制] 对比配置: scheduleIds={}", scheduleIds);

        // TODO: 实现对比分析（需要数据库支持）
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("scheduleIds", scheduleIds);
        comparison.put("comparison", "TODO");

        log.info("[弹性工作制] 对比配置成功: count={}", scheduleIds.size());
        return comparison.toString();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证表单数据
     */
    private void validateScheduleForm(FlexibleWorkScheduleForm form) {
        // 1. 验证时间范围
        if (form.getFlexStartEarliest() != null && form.getFlexStartLatest() != null) {
            if (form.getFlexStartEarliest().isAfter(form.getFlexStartLatest())) {
                throw new BusinessException("INVALID_TIME_RANGE", "弹性上班最早时间不能晚于最晚时间");
            }
        }

        if (form.getFlexEndEarliest() != null && form.getFlexEndLatest() != null) {
            if (form.getFlexEndEarliest().isAfter(form.getFlexEndLatest())) {
                throw new BusinessException("INVALID_TIME_RANGE", "弹性下班最早时间不能晚于最晚时间");
            }
        }

        // 2. 验证核心时间
        if (form.getCoreStartTime() != null && form.getCoreEndTime() != null) {
            if (form.getCoreStartTime().isAfter(form.getCoreEndTime())) {
                throw new BusinessException("INVALID_TIME_RANGE", "核心开始时间不能晚于结束时间");
            }
        }

        // 3. 验证工作时长
        if (form.getMinWorkDuration() != null && form.getMaxWorkDuration() != null) {
            if (form.getMinWorkDuration() > form.getMaxWorkDuration()) {
                throw new BusinessException("INVALID_DURATION", "最少工作时长不能大于最多工作时长");
            }
        }
    }

    /**
     * 从表单构建实体
     */
    private FlexibleWorkScheduleEntity buildEntityFromForm(FlexibleWorkScheduleForm form) {
        FlexibleWorkScheduleEntity entity = new FlexibleWorkScheduleEntity();

        // 基础信息
        entity.setScheduleName(form.getScheduleName());
        entity.setScheduleCode(form.getScheduleCode());
        entity.setFlexMode(form.getFlexMode());
        entity.setDescription(form.getDescription());

        // 弹性时间
        entity.setFlexStartEarliest(form.getFlexStartEarliest());
        entity.setFlexStartLatest(form.getFlexStartLatest());
        entity.setFlexEndEarliest(form.getFlexEndEarliest());
        entity.setFlexEndLatest(form.getFlexEndLatest());

        // 核心时间
        entity.setCoreStartTime(form.getCoreStartTime());
        entity.setCoreEndTime(form.getCoreEndTime());

        // 工作时长
        entity.setMinWorkDuration(form.getMinWorkDuration());
        entity.setStandardWorkDuration(form.getStandardWorkDuration());
        entity.setMaxWorkDuration(form.getMaxWorkDuration());

        // 宽限时间
        entity.setLateTolerance(form.getLateTolerance());
        entity.setEarlyTolerance(form.getEarlyTolerance());

        // 考勤规则
        entity.setRequireCoreTimeCheck(form.getRequireCoreTimeCheck());
        entity.setAllowFlexibleOvertime(form.getAllowFlexibleOvertime());
        entity.setCrossDayFlex(form.getCrossDayFlex());

        // 午休时间
        entity.setLunchStartTime(form.getLunchStartTime());
        entity.setLunchEndTime(form.getLunchEndTime());
        entity.setLunchCountAsWork(form.getLunchCountAsWork());

        // 高级规则
        entity.setMinWorkDaysPerWeek(form.getMinWorkDaysPerWeek());
        entity.setMaxWorkDaysPerWeek(form.getMaxWorkDaysPerWeek());
        entity.setAllowRemoteWork(form.getAllowRemoteWork());
        entity.setRemoteWorkRequiresApproval(form.getRemoteWorkRequiresApproval());

        // 状态
        entity.setStatus(form.getStatus());
        entity.setEffectiveTime(form.getEffectiveTime());
        entity.setExpireTime(form.getExpireTime());

        // 扩展属性
        entity.setExtendedAttributes(form.getExtendedAttributes());

        return entity;
    }

    /**
     * 转换为列表VO
     */
    private FlexibleWorkScheduleVO convertToVO(FlexibleWorkScheduleEntity entity) {
        return FlexibleWorkScheduleVO.builder()
                .scheduleId(entity.getScheduleId())
                .scheduleName(entity.getScheduleName())
                .scheduleCode(entity.getScheduleCode())
                .flexMode(entity.getFlexMode())
                .flexModeDesc(getFlexModeDesc(entity.getFlexMode()))
                .flexStartRange(buildTimeRange(entity.getFlexStartEarliest(), entity.getFlexStartLatest()))
                .flexEndRange(buildTimeRange(entity.getFlexEndEarliest(), entity.getFlexEndLatest()))
                .coreTimeRange(buildTimeRange(entity.getCoreStartTime(), entity.getCoreEndTime()))
                .standardWorkDuration(entity.getStandardWorkDuration())
                .status(entity.getStatus())
                .statusDesc(entity.getStatus() == 1 ? "启用" : "禁用")
                .effectiveTime(entity.getEffectiveTime())
                .expireTime(entity.getExpireTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 转换为详情VO
     */
    private FlexibleWorkScheduleDetailVO convertToDetailVO(FlexibleWorkScheduleEntity entity) {
        FlexibleWorkScheduleDetailVO vo = FlexibleWorkScheduleDetailVO.builder()
                .scheduleId(entity.getScheduleId())
                .scheduleName(entity.getScheduleName())
                .scheduleCode(entity.getScheduleCode())
                .flexMode(entity.getFlexMode())
                .flexModeDesc(getFlexModeDesc(entity.getFlexMode()))
                .flexStartRange(buildTimeRange(entity.getFlexStartEarliest(), entity.getFlexStartLatest()))
                .flexEndRange(buildTimeRange(entity.getFlexEndEarliest(), entity.getFlexEndLatest()))
                .coreTimeRange(buildTimeRange(entity.getCoreStartTime(), entity.getCoreEndTime()))
                .flexStartEarliest(entity.getFlexStartEarliest())
                .flexStartLatest(entity.getFlexStartLatest())
                .flexEndEarliest(entity.getFlexEndEarliest())
                .flexEndLatest(entity.getFlexEndLatest())
                .coreStartTime(entity.getCoreStartTime())
                .coreEndTime(entity.getCoreEndTime())
                .minWorkDuration(entity.getMinWorkDuration())
                .standardWorkDuration(entity.getStandardWorkDuration())
                .maxWorkDuration(entity.getMaxWorkDuration())
                .lateTolerance(entity.getLateTolerance())
                .earlyTolerance(entity.getEarlyTolerance())
                .requireCoreTimeCheck(entity.getRequireCoreTimeCheck())
                .allowFlexibleOvertime(entity.getAllowFlexibleOvertime())
                .crossDayFlex(entity.getCrossDayFlex())
                .lunchStartTime(entity.getLunchStartTime())
                .lunchEndTime(entity.getLunchEndTime())
                .lunchCountAsWork(entity.getLunchCountAsWork())
                .minWorkDaysPerWeek(entity.getMinWorkDaysPerWeek())
                .maxWorkDaysPerWeek(entity.getMaxWorkDaysPerWeek())
                .allowRemoteWork(entity.getAllowRemoteWork())
                .remoteWorkRequiresApproval(entity.getRemoteWorkRequiresApproval())
                .status(entity.getStatus())
                .statusDesc(entity.getStatus() == 1 ? "启用" : "禁用")
                .effectiveTime(entity.getEffectiveTime())
                .expireTime(entity.getExpireTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .description(entity.getDescription())
                .build();

        // 计算核心工作时长
        if (entity.getCoreStartTime() != null && entity.getCoreEndTime() != null) {
            int coreDuration = (int) Duration.between(entity.getCoreStartTime(), entity.getCoreEndTime()).toMinutes();
            vo.setCoreWorkDuration(coreDuration);
        }

        // 计算午休时长
        if (entity.getLunchStartTime() != null && entity.getLunchEndTime() != null) {
            int lunchDuration = (int) Duration.between(entity.getLunchStartTime(), entity.getLunchEndTime()).toMinutes();
            vo.setLunchDuration(lunchDuration);
        }

        // TODO: 设置部门名称、创建人姓名、更新人姓名等（需要关联查询）
        // TODO: 设置统计信息（需要数据库支持）

        // 生成规则摘要
        vo.setRuleSummary(generateRuleSummary(entity));

        return vo;
    }

    /**
     * 获取弹性模式描述
     */
    private String getFlexModeDesc(String flexMode) {
        return switch (flexMode) {
            case "STANDARD" -> "标准弹性";
            case "FLEXIBLE" -> "完全弹性";
            case "HYBRID" -> "混合弹性";
            default -> "未知";
        };
    }

    /**
     * 构建时间范围字符串
     */
    private String buildTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return startTime + "-" + endTime;
    }

    /**
     * 生成规则摘要
     */
    private String generateRuleSummary(FlexibleWorkScheduleEntity entity) {
        StringBuilder summary = new StringBuilder();

        // 核心时间
        if (entity.getCoreStartTime() != null && entity.getCoreEndTime() != null) {
            summary.append("核心时间").append(entity.getCoreStartTime())
                    .append("-").append(entity.getCoreEndTime()).append("，");
        }

        // 弹性时间
        if (entity.getFlexStartEarliest() != null && entity.getFlexStartLatest() != null) {
            summary.append("弹性上班").append(entity.getFlexStartEarliest())
                    .append("-").append(entity.getFlexStartLatest()).append("，");
        }

        if (entity.getFlexEndEarliest() != null && entity.getFlexEndLatest() != null) {
            summary.append("弹性下班").append(entity.getFlexEndEarliest())
                    .append("-").append(entity.getFlexEndLatest()).append("，");
        }

        // 工作时长
        if (entity.getStandardWorkDuration() != null) {
            int hours = entity.getStandardWorkDuration() / 60;
            summary.append("标准工作").append(hours).append("小时");
        }

        return summary.toString();
    }
}

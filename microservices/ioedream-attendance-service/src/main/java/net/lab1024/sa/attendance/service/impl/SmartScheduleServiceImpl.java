package net.lab1024.sa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.SmartSchedulePlanDao;
import net.lab1024.sa.attendance.dao.SmartScheduleResultDao;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanAddForm;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanQueryForm;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanDetailVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartScheduleResultVO;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflict;
import net.lab1024.sa.attendance.engine.conflict.ScheduleConflictDetector;
import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationAlgorithmFactory;
import net.lab1024.sa.attendance.engine.optimizer.SimulatedAnnealingOptimizer;
import net.lab1024.sa.common.entity.attendance.SmartSchedulePlanEntity;
import net.lab1024.sa.attendance.service.SmartScheduleService;

import net.lab1024.sa.attendance.manager.SmartSchedulePlanManager;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能排班计划服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class SmartScheduleServiceImpl implements SmartScheduleService {

    @Resource
    private SmartSchedulePlanDao smartSchedulePlanDao;

    @Resource
    private SmartScheduleResultDao smartScheduleResultDao;

    @Resource
    private SmartSchedulePlanManager smartSchedulePlanManager;

    @Resource
    private OptimizationAlgorithmFactory optimizationAlgorithmFactory;

    @Resource
    private ScheduleConflictDetector conflictDetector;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(SmartSchedulePlanAddForm form) {
        log.info("[智能排班] 创建排班计划: name={}, startDate={}, endDate={}",
                form.getPlanName(), form.getStartDate(), form.getEndDate());

        try {
            // 1. 转换List为JSON字符串（处理JsonProcessingException）
            String employeeIdsJson = form.getEmployeeIds() != null
                    ? objectMapper.writeValueAsString(form.getEmployeeIds())
                    : "[]";
            String shiftIdsJson = form.getShiftIds() != null
                    ? objectMapper.writeValueAsString(form.getShiftIds())
                    : "[]";

            // 2. 构建实体
            SmartSchedulePlanEntity entity = SmartSchedulePlanEntity.builder()
                    .planName(form.getPlanName())

                    .startDate(form.getStartDate())
                    .endDate(form.getEndDate())
                    .periodDays(form.getPeriodDays())
                    .employeeIds(employeeIdsJson)
                    .shiftIds(shiftIdsJson)

                    .optimizationGoal(form.getOptimizationGoal())
                    .minConsecutiveWorkDays(form.getMinConsecutiveWorkDays())
                    .maxConsecutiveWorkDays(form.getMaxConsecutiveWorkDays())
                    .minRestDays(form.getMinRestDays())
                    .minDailyStaff(form.getMinDailyStaff())
                    .maxDailyStaff(form.getMaxDailyStaff())

                    .populationSize(form.getPopulationSize())
                    .maxIterations(form.getMaxIterations())
                    .crossoverRate(form.getCrossoverRate())
                    .mutationRate(form.getMutationRate())
                    .selectionRate(form.getSelectionRate())
                    .elitismRate(form.getElitismRate())
                    .fairnessWeight(form.getFairnessWeight())
                    .costWeight(form.getCostWeight())
                    .efficiencyWeight(form.getEfficiencyWeight())
                    .satisfactionWeight(form.getSatisfactionWeight())
                    .overtimeCostPerShift(form.getOvertimeCostPerShift())
                    .weekendCostPerShift(form.getWeekendCostPerShift())
                    .holidayCostPerShift(form.getHolidayCostPerShift())
                    .algorithmType(form.getAlgorithmType())
                    .executionStatus(0) // 待执行
                    .build();

            // 3. 保存到数据库
            smartSchedulePlanDao.insert(entity);

            log.info("[智能排班] 排班计划创建成功: planId={}", entity.getPlanId());

            return entity.getPlanId();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("[智能排班] JSON序列化失败: {}", e.getMessage(), e);
            throw new BusinessException("数据格式错误: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptimizationResult executeOptimization(Long planId) {
        log.info("[智能排班] 开始执行优化: planId={}", planId);

        // 1. 查询排班计划
        SmartSchedulePlanEntity plan = smartSchedulePlanDao.selectById(planId);
        if (plan == null) {
            throw new BusinessException("排班计划不存在");
        }

        // 2. 更新执行状态为执行中
        smartSchedulePlanDao.updateById(SmartSchedulePlanEntity.builder()
                .planId(planId)
                .executionStatus(1) // 执行中
                .build());

        try {
            // 3. 构建优化配置
            OptimizationConfig config = buildOptimizationConfig(plan);

            // 4. 执行优化
            OptimizationResult result = optimizationAlgorithmFactory.optimize(config);

            // 5. 保存排班结果
            saveScheduleResults(planId, result, config);

            // 6. 检测冲突
            List<ScheduleConflict> conflicts = conflictDetector.detectConflicts(
                    result.getBestChromosome(), config);

            // 7. 更新执行状态为已完成
            smartSchedulePlanDao.updateById(SmartSchedulePlanEntity.builder()
                    .planId(planId)
                    .executionStatus(2) // 已完成
                    .fitnessScore(result.getBestFitness())
                    .fairnessScore(result.getFairnessScore())
                    .costScore(result.getCostScore())
                    .efficiencyScore(result.getEfficiencyScore())
                    .satisfactionScore(result.getSatisfactionScore())
                    .executionDurationMs(result.getExecutionDurationMs())
                    .converged(result.getConverged() != null && result.getConverged() ? 1 : 0)
                    .build());

            log.info("[智能排班] 优化执行成功: planId={}, fitness={}", planId, result.getBestFitness());

            return result;

        } catch (Exception e) {
            // 更新执行状态为失败
            smartSchedulePlanDao.updateById(SmartSchedulePlanEntity.builder()
                    .planId(planId)
                    .executionStatus(3) // 执行失败
                    .errorMessage(e.getMessage())
                    .build());

            log.error("[智能排班] 优化执行失败: planId={}, error={}", planId, e.getMessage(), e);
            throw new BusinessException("排班优化失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<SmartSchedulePlanVO> queryPlanPage(SmartSchedulePlanQueryForm form) {
        log.info("[智能排班] 查询排班计划列表: {}", form);

        // 构建查询条件
        LambdaQueryWrapper<SmartSchedulePlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(form.getPlanName() != null,
                        SmartSchedulePlanEntity::getPlanName, form.getPlanName())
                .eq(form.getStatus() != null,
                        SmartSchedulePlanEntity::getExecutionStatus, form.getStatus())
                .ge(form.getStartDateBegin() != null,
                        SmartSchedulePlanEntity::getStartDate, form.getStartDateBegin())
                .le(form.getStartDateEnd() != null,
                        SmartSchedulePlanEntity::getStartDate, form.getStartDateEnd())
                .orderByDesc(SmartSchedulePlanEntity::getCreateTime);

        // 分页查询
        Page<SmartSchedulePlanEntity> page = smartSchedulePlanDao.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()),
                queryWrapper
        );

        // 转换为VO
        List<SmartSchedulePlanVO> voList = SmartBeanUtil.copyList(page.getRecords(), SmartSchedulePlanVO.class);

        return PageResult.of(voList, page.getTotal(), form.getPageNum(), form.getPageSize());
    }

    @Override
    public SmartSchedulePlanDetailVO getPlanDetail(Long planId) {
        log.info("[智能排班] 查询排班计划详情: planId={}", planId);

        SmartSchedulePlanEntity entity = smartSchedulePlanDao.selectById(planId);
        if (entity == null) {
            throw new BusinessException("排班计划不存在");
        }

        return SmartBeanUtil.copy(entity, SmartSchedulePlanDetailVO.class);
    }

    @Override
    public PageResult<SmartScheduleResultVO> queryResultPage(Long planId, Integer pageNum, Integer pageSize,
                                                            Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("[智能排班] 查询排班结果: planId={}, pageNum={}, pageSize={}", planId, pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<SmartScheduleResultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmartScheduleResultEntity::getPlanId, planId)
                .eq(employeeId != null, SmartScheduleResultEntity::getEmployeeId, employeeId)
                .ge(startDate != null, SmartScheduleResultEntity::getScheduleDate, startDate)
                .le(endDate != null, SmartScheduleResultEntity::getScheduleDate, endDate)
                .orderByAsc(SmartScheduleResultEntity::getEmployeeId)
                .orderByAsc(SmartScheduleResultEntity::getScheduleDate);

        // 分页查询
        Page<SmartScheduleResultEntity> page = smartScheduleResultDao.selectPage(
                new Page<>(pageNum, pageSize),
                queryWrapper
        );

        // 转换为VO
        List<SmartScheduleResultVO> voList = SmartBeanUtil.copyList(page.getRecords(), SmartScheduleResultVO.class);

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<SmartScheduleResultVO> queryResultList(Long planId) {
        log.info("[智能排班] 查询所有排班结果: planId={}", planId);

        LambdaQueryWrapper<SmartScheduleResultEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SmartScheduleResultEntity::getPlanId, planId)
                .orderByAsc(SmartScheduleResultEntity::getEmployeeId)
                .orderByAsc(SmartScheduleResultEntity::getScheduleDate);

        List<SmartScheduleResultEntity> entityList = smartScheduleResultDao.selectList(queryWrapper);

        return SmartBeanUtil.copyList(entityList, SmartScheduleResultVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long planId) {
        log.info("[智能排班] 删除排班计划: planId={}", planId);

        SmartSchedulePlanEntity plan = smartSchedulePlanDao.selectById(planId);
        if (plan == null) {
            throw new BusinessException("排班计划不存在");
        }

        // 检查是否已确认
        if (plan.getExecutionStatus() == 2 || plan.getExecutionStatus() == 3) {
            throw new BusinessException("已确认或执行中的计划不能删除");
        }

        // 删除排班结果
        smartScheduleResultDao.delete(QueryBuilder.of(SmartScheduleResultEntity.class)
                .eq(SmartScheduleResultEntity::getPlanId, planId)
                .build()
        );

        // 删除排班计划
        smartSchedulePlanDao.deleteById(planId);

        log.info("[智能排班] 排班计划删除成功: planId={}", planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePlan(List<Long> planIds) {
        log.info("[智能排班] 批量删除排班计划: count={}", planIds.size());

        if (planIds == null || planIds.isEmpty()) {
            throw new BusinessException("计划ID列表不能为空");
        }

        for (Long planId : planIds) {
            deletePlan(planId);
        }

        log.info("[智能排班] 批量删除成功: count={}", planIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPlan(Long planId) {
        log.info("[智能排班] 确认排班计划: planId={}", planId);

        SmartSchedulePlanEntity plan = smartSchedulePlanDao.selectById(planId);
        if (plan == null) {
            throw new BusinessException("排班计划不存在");
        }

        // 检查是否已完成优化
        if (plan.getExecutionStatus() != 2) {
            throw new BusinessException("只有已完成的优化计划才能确认");
        }

        // 更新状态为已确认
        smartSchedulePlanDao.updateById(SmartSchedulePlanEntity.builder()
                .planId(planId)
                .executionStatus(2) // 已确认
                .build());

        log.info("[智能排班] 排班计划确认成功: planId={}", planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPlan(Long planId, String reason) {
        log.info("[智能排班] 取消排班计划: planId={}, reason={}", planId, reason);

        SmartSchedulePlanEntity plan = smartSchedulePlanDao.selectById(planId);
        if (plan == null) {
            throw new BusinessException("排班计划不存在");
        }

        // 更新状态为已取消
        smartSchedulePlanDao.updateById(SmartSchedulePlanEntity.builder()
                .planId(planId)
                .executionStatus(4) // 已取消
                .remark(reason)
                .build());

        log.info("[智能排班] 排班计划取消成功: planId={}", planId);
    }

    @Override
    public byte[] exportScheduleResult(Long planId) {
        log.info("[智能排班] 导出排班结果: planId={}", planId);

        // TODO: 实现Excel导出功能
        // 1. 查询所有排班结果
        // 2. 使用EasyExcel生成Excel文件
        // 3. 返回字节数组

        throw new BusinessException("导出功能待实现");
    }

    /**
     * 构建优化配置
     */
    private OptimizationConfig buildOptimizationConfig(SmartSchedulePlanEntity plan) {
        try {
            // 1. 反序列化JSON字符串为List（处理JsonProcessingException）
            List<Long> employeeIds = objectMapper.readValue(plan.getEmployeeIds(), new TypeReference<List<Long>>() {});
            List<Long> shiftIds = plan.getShiftIds() != null
                    ? objectMapper.readValue(plan.getShiftIds(), new TypeReference<List<Long>>() {})
                    : new ArrayList<>();

            // 2. 构建配置对象
            return OptimizationConfig.builder()
                    .employeeIds(employeeIds)
                    .startDate(plan.getStartDate())
                    .endDate(plan.getEndDate())
                    .shiftIds(shiftIds)
                    .maxConsecutiveWorkDays(plan.getMaxConsecutiveWorkDays())
                    .minRestDays(plan.getMinRestDays())
                    .minDailyStaff(plan.getMinDailyStaff())
                    .fairnessWeight(plan.getFairnessWeight())
                    .costWeight(plan.getCostWeight())
                    .efficiencyWeight(plan.getEfficiencyWeight())
                    .satisfactionWeight(plan.getSatisfactionWeight())
                    .build();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("[智能排班] JSON反序列化失败: planId={}, error={}", plan.getPlanId(), e.getMessage(), e);
            throw new BusinessException("数据格式错误: " + e.getMessage());
        }
    }

    /**
     * 保存排班结果
     */
    private void saveScheduleResults(Long planId, OptimizationResult result, OptimizationConfig config) {
        log.info("[智能排班] 保存排班结果: planId={}", planId);

        Chromosome chromosome = result.getBestChromosome();
        List<SmartScheduleResultEntity> resultList = new ArrayList<>();

        LocalDate startDate = config.getStartDate();
        LocalDate endDate = config.getEndDate();

        // 生成日期列表（严格使用LocalDate，禁止int索引）
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 遍历每个员工
        for (Long employeeId : config.getEmployeeIds()) {
            // 遍历每一天
            for (int day = 0; day < dates.size(); day++) {
                LocalDate scheduleDate = dates.get(day);
                Long shiftId = chromosome.getShift(employeeId, scheduleDate);

                SmartScheduleResultEntity resultEntity = SmartScheduleResultEntity.builder()
                        .planId(planId)
                        .employeeId(employeeId)
                        .scheduleDate(scheduleDate)
                        .shiftId(shiftId)
                        .scheduleStatus(1) // 草稿
                        .build();

                resultList.add(resultEntity);
            }
        }

        // 批量插入
        resultList.forEach(smartScheduleResultDao::insert);

        log.info("[智能排班] 排班结果保存成功: count={}", resultList.size());
    }
}

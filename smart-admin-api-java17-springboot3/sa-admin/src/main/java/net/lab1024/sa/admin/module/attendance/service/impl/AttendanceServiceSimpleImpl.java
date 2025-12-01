package net.lab1024.sa.admin.module.attendance.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.controller.AttendanceController;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.admin.module.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordQueryVO;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.admin.module.attendance.domain.vo.MakeupPunchRequest;
import net.lab1024.sa.admin.module.attendance.service.IAttendanceService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.support.file.service.IFileStorageService;

/**
 * 考勤服务简化实现类
 *
 * <p>
 * 严格遵循repowiki规范:
 * - 实现核心考勤算法和业务逻辑
 * - 使用@Transactional管理事务
 * - 遵循四层架构模式
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
public class AttendanceServiceSimpleImpl extends ServiceImpl<AttendanceRecordDao, AttendanceRecordEntity>
        implements IAttendanceService {

    @org.springframework.beans.factory.annotation.Value("${file.storage.local.upload-path:D:/Progect/mart-admin-master/upload/}")
    private String uploadPath;

    @Resource
    private IFileStorageService fileStorageService;

    // 考勤状态常量
    private static final String STATUS_NORMAL = "NORMAL";
    private static final String STATUS_LATE = "LATE";
    private static final String STATUS_EARLY_LEAVE = "EARLY_LEAVE";
    private static final String STATUS_ABSENT = "ABSENT";
    private static final String STATUS_LEAVE = "LEAVE";

    private static final String EXCEPTION_LATE = "LATE";
    private static final String EXCEPTION_EARLY_LEAVE = "EARLY_LEAVE";
    private static final String EXCEPTION_ABSENTEEISM = "ABSENTEEISM";
    private static final String EXCEPTION_FORGET_PUNCH = "FORGET_PUNCH";

    // 加班倍率常量（严格遵循repowiki规范：禁止魔法数字）
    /**
     * 节假日加班倍率（默认3倍）
     */
    private static final BigDecimal DEFAULT_HOLIDAY_OVERTIME_RATE = BigDecimal.valueOf(3.0);

    /**
     * 工作日加班倍率（默认1.5倍）
     */
    private static final BigDecimal DEFAULT_WORKDAY_OVERTIME_RATE = BigDecimal.valueOf(1.5);

    /**
     * 分钟转小时的除数
     */
    private static final BigDecimal MINUTES_TO_HOURS_DIVISOR = BigDecimal.valueOf(60);

    /**
     * 位置精度默认值（米）
     */
    private static final Double DEFAULT_LOCATION_ACCURACY = 10.0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> punch(AttendancePunchDTO punchDTO) {
        log.info("开始处理考勤打卡: 员工ID={}, 打卡类型={}", punchDTO.getEmployeeId(), punchDTO.getPunchType());

        try {
            // 1. 参数验证
            if (punchDTO.getEmployeeId() == null) {
                return ResponseDTO.error("员工ID不能为空");
            }

            if (punchDTO.getPunchTime() == null) {
                return ResponseDTO.error("打卡时间不能为空");
            }

            // 2. 获取当前用户权限信息
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (!currentUserId.equals(punchDTO.getEmployeeId())) {
                return ResponseDTO.error("只能为自己打卡");
            }

            // 3. 查询当日考勤记录
            LocalDate attendanceDate = punchDTO.getPunchTime().toLocalDate();
            AttendanceRecordEntity existingRecord = getTodayRecord(punchDTO.getEmployeeId(), attendanceDate);

            // 4. 获取员工考勤规则
            AttendanceRuleEntity rule = getAttendanceRule(punchDTO.getEmployeeId());
            if (rule == null) {
                return ResponseDTO.error("未找到考勤规则配置");
            }

            // 5. 处理打卡逻辑
            if ("上班".equals(punchDTO.getPunchType())) {
                return handlePunchIn(punchDTO, existingRecord, rule);
            } else if ("下班".equals(punchDTO.getPunchType())) {
                return handlePunchOut(punchDTO, existingRecord, rule);
            } else {
                return ResponseDTO.error("打卡类型不正确");
            }

        } catch (Exception e) {
            log.error("考勤打卡失败: 员工ID" + punchDTO.getEmployeeId(), e);
            return ResponseDTO.error("打卡失败：" + e.getMessage());
        }

    }

    @Override

    public PageResult<AttendanceRecordVO> queryByPage(AttendanceRecordQueryVO queryVO, PageParam pageParam) {
        log.debug("分页查询考勤记录: 员工ID={}, 页码={}, 页大小={}", queryVO.getEmployeeId(), pageParam.getPageNum(),
                pageParam.getPageSize());

        try {
            // 1. 构造查询条件
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, queryVO.getEmployeeId())
                    .eq(queryVO.getDepartmentId() != null, AttendanceRecordEntity::getDepartmentId,
                            queryVO.getDepartmentId())
                    .ge(queryVO.getStartDate() != null, AttendanceRecordEntity::getAttendanceDate,
                            queryVO.getStartDate())
                    .le(queryVO.getEndDate() != null, AttendanceRecordEntity::getAttendanceDate, queryVO.getEndDate())
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            // 2. 分页查询
            com.baomidou.mybatisplus.core.metadata.IPage<AttendanceRecordEntity> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                    pageParam.getPageNum(), pageParam.getPageSize());
            com.baomidou.mybatisplus.core.metadata.IPage<AttendanceRecordEntity> pageResult = this.page(page,
                    queryWrapper);

            // 3. 转换为VO
            List<AttendanceRecordVO> voList = pageResult.getRecords().stream()
                    .map(entity -> SmartBeanUtil.copy(entity, AttendanceRecordVO.class))
                    .collect(Collectors.toList());

            // 4. 构造返回结果
            PageResult<AttendanceRecordVO> result = new PageResult<>();
            result.setPageNum((long) pageResult.getCurrent());
            result.setPageSize((long) pageResult.getSize());
            result.setTotal(pageResult.getTotal());
            result.setList(voList);

            return result;

        } catch (Exception e) {
            log.error("分页查询考勤记录失败", e);
            return PageResult.of(new ArrayList<>(), 0L, pageParam.getPageNum(), pageParam.getPageSize());
        }
    }

    @Override
    public ResponseDTO<AttendanceRecordVO> getById(Long recordId) {
        log.debug("根据ID查询考勤记录: 记录ID={}", recordId);
        try {
            AttendanceRecordEntity entity = super.getById(recordId);
            if (entity == null) {
                return ResponseDTO.error("考勤记录不存在");
            }

            AttendanceRecordVO vo = SmartBeanUtil.copy(entity, AttendanceRecordVO.class);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("根据ID查询考勤记录失败: 记录ID" + recordId, e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> create(
            net.lab1024.sa.admin.module.attendance.domain.dto.AttendanceRecordCreateDTO createDTO) {
        log.info("创建考勤记录: 员工ID={}, 考勤日期={}", createDTO.getEmployeeId(), createDTO.getAttendanceDate());

        try {
            AttendanceRecordEntity entity = SmartBeanUtil.copy(createDTO, AttendanceRecordEntity.class);
            this.save(entity);
            return ResponseDTO.ok(entity.getRecordId());

        } catch (Exception e) {
            log.error("创建考勤记录失败", e);
            return ResponseDTO.error("创建失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> update(
            net.lab1024.sa.admin.module.attendance.domain.dto.AttendanceRecordUpdateDTO updateDTO) {
        log.info("更新考勤记录: 记录ID={}", updateDTO.getRecordId());

        try {
            AttendanceRecordEntity entity = SmartBeanUtil.copy(updateDTO, AttendanceRecordEntity.class);
            this.updateById(entity);
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("更新考勤记录失败", e);
            return ResponseDTO.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> delete(Long recordId) {
        log.info("删除考勤记录: 记录ID={}", recordId);

        try {
            this.removeById(recordId);
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("删除考勤记录失败", e);
            return ResponseDTO.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchDelete(List<Long> recordIds) {
        log.info("批量删除考勤记录: 数量={}", recordIds.size());

        try {
            int count = 0;
            for (Long recordId : recordIds) {
                if (this.removeById(recordId)) {
                    count++;
                }
            }
            return ResponseDTO.ok(count);

        } catch (Exception e) {
            log.error("批量删除考勤记录失败", e);
            return ResponseDTO.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getEmployeeStats(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.debug("获取员工考勤统计: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

        try {
            // 1. 查询期间内的考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId)
                    .between(AttendanceRecordEntity::getAttendanceDate, startDate, endDate);

            List<AttendanceRecordEntity> records = this.list(queryWrapper);

            // 2. 统计各种状态
            Map<String, Long> statusCount = records.stream()
                    .collect(Collectors.groupingBy(
                            record -> record.getAttendanceStatus() != null ? record.getAttendanceStatus() : "未打卡",
                            Collectors.counting()));

            // 3. 计算工作时长
            BigDecimal totalWorkHours = records.stream()
                    .filter(record -> record.getWorkHours() != null
                            && record.getWorkHours().compareTo(BigDecimal.ZERO) > 0)
                    .map(AttendanceRecordEntity::getWorkHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 4. 计算加班时长
            BigDecimal totalOvertimeHours = records.stream()
                    .filter(record -> record.getOvertimeHours() != null
                            && record.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0)
                    .map(AttendanceRecordEntity::getOvertimeHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5. 构造统计结果
            Map<String, Object> stats = new HashMap<>();
            stats.put("employeeId", employeeId);
            stats.put("startDate", startDate);
            stats.put("endDate", endDate);
            stats.put("totalDays", records.size());
            stats.put("normalDays", statusCount.getOrDefault(STATUS_NORMAL, 0L));
            stats.put("lateDays", statusCount.getOrDefault(STATUS_LATE, 0L));
            stats.put("earlyLeaveDays", statusCount.getOrDefault(STATUS_EARLY_LEAVE, 0L));
            stats.put("absentDays", statusCount.getOrDefault(STATUS_ABSENT, 0L));
            stats.put("totalWorkHours", totalWorkHours.setScale(2, RoundingMode.HALF_UP));
            stats.put("totalOvertimeHours", totalOvertimeHours.setScale(2, RoundingMode.HALF_UP));

            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("获取员工考勤统计失败", e);
            return ResponseDTO.error("统计失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDepartmentStats(Long departmentId, LocalDate startDate,
            LocalDate endDate) {
        log.debug("获取部门考勤统计: 部门ID={}, 开始日期={}, 结束日期={}", departmentId, startDate, endDate);

        try {
            // 1. 查询部门内所有员工的考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(AttendanceRecordEntity::getAttendanceDate, startDate, endDate);

            List<AttendanceRecordEntity> records = this.list(queryWrapper);

            // 2. 统计各员工数据
            Map<Long, List<AttendanceRecordEntity>> employeeRecords = records.stream()
                    .collect(Collectors.groupingBy(AttendanceRecordEntity::getEmployeeId));

            // 3. 汇总部门统计
            Map<String, Object> stats = new HashMap<>();
            stats.put("departmentId", departmentId);
            stats.put("startDate", startDate);
            stats.put("endDate", endDate);
            stats.put("totalEmployees", employeeRecords.size());
            stats.put("totalRecords", records.size());

            // 计算部门平均出勤率等指标
            int totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
            stats.put("workingDays", totalDays);

            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("获取部门考勤统计失败", e);
            return ResponseDTO.error("统计失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchRecalculate(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("批量重新计算考勤记录: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

        try {
            // 1. 查询期间内的考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId)
                    .between(AttendanceRecordEntity::getAttendanceDate, startDate, endDate);

            List<AttendanceRecordEntity> records = this.list(queryWrapper);
            int count = 0;

            // 2. 获取考勤规则
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);

            // 3. 重新计算每条记录
            for (AttendanceRecordEntity record : records) {
                recalculateAttendanceRecord(record, rule);
                this.updateById(record);
                count++;
            }

            return ResponseDTO.ok(count);

        } catch (Exception e) {
            log.error("批量重新计算考勤记录失败", e);
            return ResponseDTO.error("计算失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getAbnormalRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String exceptionType) {
        log.debug("获取考勤异常记录: 员工ID={}, 部门ID={}, 异常类型={}", employeeId, departmentId, exceptionType);

        try {
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(employeeId != null, AttendanceRecordEntity::getEmployeeId, employeeId)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(startDate != null && endDate != null, AttendanceRecordEntity::getAttendanceDate, startDate,
                            endDate)
                    .eq(exceptionType != null, AttendanceRecordEntity::getExceptionType, exceptionType)
                    .isNotNull(exceptionType == null ? null : AttendanceRecordEntity::getExceptionType)
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> entities = this.list(queryWrapper);
            List<AttendanceRecordVO> voList = entities.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, AttendanceRecordVO.class))
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取考勤异常记录失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getLateRecords(Long employeeId, Long departmentId,
            LocalDate startDate, LocalDate endDate) {
        log.debug("获取迟到记录: 员工ID={}, 部门ID={}", employeeId, departmentId);

        try {
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(employeeId != null, AttendanceRecordEntity::getEmployeeId, employeeId)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(startDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .between(endDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .eq(AttendanceRecordEntity::getExceptionType, EXCEPTION_LATE)
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> entities = this.list(queryWrapper);
            List<AttendanceRecordVO> voList = entities.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, AttendanceRecordVO.class))
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取迟到记录失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getEarlyLeaveRecords(Long employeeId, Long departmentId,
            LocalDate startDate, LocalDate endDate) {
        log.debug("获取早退记录: 员工ID={}, 部门ID={}", employeeId, departmentId);

        try {
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(employeeId != null, AttendanceRecordEntity::getEmployeeId, employeeId)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(startDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .between(endDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .eq(AttendanceRecordEntity::getExceptionType, EXCEPTION_EARLY_LEAVE)
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> entities = this.list(queryWrapper);
            List<AttendanceRecordVO> voList = entities.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, AttendanceRecordVO.class))
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取早退记录失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getOvertimeRecords(Long employeeId, Long departmentId,
            LocalDate startDate, LocalDate endDate) {
        log.debug("获取加班记录: 员工ID={}, 部门ID={}", employeeId, departmentId);

        try {
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(employeeId != null, AttendanceRecordEntity::getEmployeeId, employeeId)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(startDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .between(endDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .isNotNull(AttendanceRecordEntity::getOvertimeHours)
                    .gt(AttendanceRecordEntity::getOvertimeHours, BigDecimal.ZERO)
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> entities = this.list(queryWrapper);
            List<AttendanceRecordVO> voList = entities.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, AttendanceRecordVO.class))
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取加班记录失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getAbsentRecords(Long employeeId, Long departmentId,
            LocalDate startDate, LocalDate endDate) {
        log.debug("获取旷工记录: 员工ID={}, 部门ID={}", employeeId, departmentId);

        try {
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(employeeId != null, AttendanceRecordEntity::getEmployeeId, employeeId)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(startDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .between(endDate != null, AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .eq(AttendanceRecordEntity::getAttendanceStatus, STATUS_ABSENT)
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> entities = this.list(queryWrapper);
            List<AttendanceRecordVO> voList = entities.stream()
                    .map(entity -> SmartBeanUtil.copy(entity, AttendanceRecordVO.class))
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取旷工记录失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getMonthlySummary(Integer year, Integer month, Long departmentId) {
        log.debug("获取考勤月度汇总: 年={}, 月={}, 部门ID={}", year, month, departmentId);

        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.between(AttendanceRecordEntity::getAttendanceDate, startDate, endDate)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId);

            List<AttendanceRecordEntity> records = this.list(queryWrapper);

            // 按员工分组统计
            Map<Long, List<AttendanceRecordEntity>> employeeRecords = records.stream()
                    .collect(Collectors.groupingBy(AttendanceRecordEntity::getEmployeeId));

            List<Map<String, Object>> summary = new ArrayList<>();
            for (Map.Entry<Long, List<AttendanceRecordEntity>> entry : employeeRecords.entrySet()) {
                Long empId = entry.getKey();
                List<AttendanceRecordEntity> employeeRecordList = entry.getValue();

                Map<String, Object> empSummary = new HashMap<>();
                empSummary.put("employeeId", empId);
                empSummary.put("year", year);
                empSummary.put("month", month);
                empSummary.put("totalDays", employeeRecordList.size());

                // 统计各种状态
                Map<String, Long> statusCount = employeeRecordList.stream()
                        .collect(Collectors.groupingBy(
                                record -> record.getAttendanceStatus() != null ? record.getAttendanceStatus() : "未打卡",
                                Collectors.counting()));

                empSummary.put("normalDays", statusCount.getOrDefault(STATUS_NORMAL, 0L));
                empSummary.put("lateDays", statusCount.getOrDefault(STATUS_LATE, 0L));
                empSummary.put("earlyLeaveDays", statusCount.getOrDefault(STATUS_EARLY_LEAVE, 0L));
                empSummary.put("absentDays", statusCount.getOrDefault(STATUS_ABSENT, 0L));

                summary.add(empSummary);
            }

            return ResponseDTO.ok(summary);

        } catch (Exception e) {
            log.error("获取考勤月度汇总失败", e);
            return ResponseDTO.error("汇总失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> syncAttendanceData(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("同步考勤数据: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

        try {
            // 这里可以实现与外部考勤设备的数据同步
            // 目前先返回成功，具体实现取决于设备接口
            return ResponseDTO.ok(0);

        } catch (Exception e) {
            log.error("同步考勤数据失败", e);
            return ResponseDTO.error("同步失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> exportAttendanceData(Long employeeId, Long departmentId,
            LocalDate startDate, LocalDate endDate, String format) {
        log.info("导出考勤数据: 员工ID={}, 部门ID={}, 格式={}", employeeId, departmentId, format);

        try {
            // 1. 查询数据
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(employeeId != null, AttendanceRecordEntity::getEmployeeId, employeeId)
                    .eq(departmentId != null, AttendanceRecordEntity::getDepartmentId, departmentId)
                    .between(startDate != null && endDate != null, AttendanceRecordEntity::getAttendanceDate, startDate,
                            endDate)
                    .orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> records = this.list(queryWrapper);

            if (records.isEmpty()) {
                return ResponseDTO.error("没有找到符合条件的考勤数据");
            }

            // 2. 生成导出文件
            String fileName = String.format("attendance_export_%s_%s.%s",
                    LocalDate.now(), UUID.randomUUID().toString().substring(0, 8), format);

            String filePath = generateExportFile(records, fileName, format);

            log.info("考勤数据导出成功: fileName={}, recordCount={}", fileName, records.size());
            return ResponseDTO.ok(filePath);

        } catch (Exception e) {
            log.error("导出考勤数据失败", e);
            return ResponseDTO.error("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出文件
     *
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * @p
     * 
     * 
     * @param fileName 文件名
     * @param format   文件格式
     * @return 文件路径
     */
    private String generateExportFile(List<AttendanceRecordEntity> records, String fileName, String format) {
        try {
            // 1. 创建导出目录
            String exportDir = uploadPath + "/export/attendance/";
            Path dirPath = Paths.get(exportDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 2. 生成文件路径
            String filePath = exportDir + fileName;
            Path file = Paths.get(filePath);

            // 3. 根据格式生成文件
            if ("csv".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
                // 生成CSV格式（Excel可以打开CSV，兼容.xlsx格式）
                StringBuilder csvContent = new StringBuilder();

                // 表头（使用UTF-8 BOM以支持Excel正确显示中文）
                csvContent.append("\uFEFF"); // UTF-8 BOM
                csvContent.append("记录ID,员工ID,部门ID,考勤日期,上班打卡时间,下班打卡时间,考勤状态,工作时长(小时),加班时长(小时),异常类型,异常原因,备注\n");

                // 数据行
                for (AttendanceRecordEntity record : records) {
                    csvContent.append(record.getRecordId() != null ? record.getRecordId() : "").append(",")
                            .append(record.getEmployeeId() != null ? record.getEmployeeId() : "").append(",")
                            .append(record.getDepartmentId() != null ? record.getDepartmentId() : "").append(",")
                            .append(record.getAttendanceDate() != null ? record.getAttendanceDate() : "").append(",")
                            .append(record.getPunchInTime() != null ? record.getPunchInTime() : "").append(",")
                            .append(record.getPunchOutTime() != null ? record.getPunchOutTime() : "").append(",")
                            .append(escapeCsv(record.getAttendanceStatus() != null ? record.getAttendanceStatus() : ""))
                            .append(",")
                            .append(record.getWorkHours() != null ? record.getWorkHours() : BigDecimal.ZERO).append(",")
                            .append(record.getOvertimeHours() != null ? record.getOvertimeHours() : BigDecimal.ZERO)
                            .append(",")
                            .append(escapeCsv(record.getExceptionType() != null ? record.getExceptionType() : ""))
                            .append(",")
                            .append(escapeCsv(record.getExceptionReason() != null ? record.getExceptionReason() : ""))
                            .append(",")
                            .append(escapeCsv(record.getProcessRemark() != null ? record.getProcessRemark() : ""))
                            .append("\n");
                }

                // 4. 写入文件
                Files.write(file, csvContent.toString().getBytes("UTF-8"));

                // 如果是xlsx格式，返回.xlsx扩展名
                if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
                    String xlsxFileName = fileName.replace(".csv", ".xlsx").replace(".excel", ".xlsx");
                    return "/file/" + xlsxFileName;
                }

                return "/file/" + fileName;
            } else if ("pdf".equalsIgnoreCase(format)) {
                // 生成PDF格式（简化实现，使用文本格式）
                StringBuilder pdfContent = new StringBuilder();
                pdfContent.append("考勤数据导出报表\n");
                pdfContent.append("导出时间: ").append(LocalDateTime.now()).append("\n");
                pdfContent.append("记录总数: ").append(records.size()).append("\n\n");
                pdfContent.append("记录ID\t员工ID\t部门ID\t考勤日期\t上班时间\t下班时间\t状态\t工作时长\t加班时长\n");
                pdfContent.append("".repeat(80)).append("\n");

                for (AttendanceRecordEntity record : records) {
                    pdfContent.append(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                            record.getRecordId() != null ? record.getRecordId() : "",
                            record.getEmployeeId() != null ? record.getEmployeeId() : "",
                            record.getDepartmentId() != null ? record.getDepartmentId() : "",
                            record.getAttendanceDate() != null ? record.getAttendanceDate() : "",
                            record.getPunchInTime() != null ? record.getPunchInTime() : "",
                            record.getPunchOutTime() != null ? record.getPunchOutTime() : "",
                            record.getAttendanceStatus() != null ? record.getAttendanceStatus() : "",
                            record.getWorkHours() != null ? record.getWorkHours() : BigDecimal.ZERO,
                            record.getOvertimeHours() != null ? record.getOvertimeHours() : BigDecimal.ZERO));
                }

                // 写入文件（实际项目中应使用iText等PDF库生成真正的PDF）
                Files.write(file, pdfContent.toString().getBytes("UTF-8"));
                return "/file/" + fileName;
            } else {
                throw new IllegalArgumentException("不支持的导出格式: " + format + "，支持格式：csv, xlsx, excel, pdf");
            }

        } catch (Exception e) {
            log.error("生成导出文件失败: fileName={}", fileName, e);
            throw new RuntimeException("生成导出文件失败: " + e.getMessage());
        }
    }

    /**
     * CSV字段转义
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getCalendarData(Long employeeId, Integer year, Integer month) {
        log.debug("获取考勤日历数据: 员工ID={}, 年={}, 月={}", employeeId, year, month);

        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId)
                    .between(AttendanceRecordEntity::getAttendanceDate, startDate, endDate);

            List<AttendanceRecordEntity> records = this.list(queryWrapper);

            List<Map<String, Object>> calendarData = new ArrayList<>();
            for (AttendanceRecordEntity record : records) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", record.getAttendanceDate());
                dayData.put("status", record.getAttendanceStatus());
                dayData.put("exceptionType", record.getExceptionType());
                dayData.put("workHours", record.getWorkHours());
                dayData.put("overtimeHours", record.getOvertimeHours());
                dayData.put("punchInTime", record.getPunchInTime());
                dayData.put("punchOutTime", record.getPunchOutTime());

                calendarData.add(dayData);
            }

            return ResponseDTO.ok(calendarData);

        } catch (Exception e) {
            log.error("获取考勤日历数据失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> validatePunch(AttendancePunchDTO punchDTO) {
        log.debug("验证打卡合法性: 员工ID={}", punchDTO.getEmployeeId());

        try {
            // 1. 验证打卡时间是否在合理范围内
            LocalTime punchTime = punchDTO.getPunchTime().toLocalTime();
            if (punchTime.isBefore(LocalTime.of(4, 0)) || punchTime.isAfter(LocalTime.of(23, 59))) {
                return ResponseDTO.error("打卡时间不合理");
            }

            // 2. 验证重复打卡
            LocalDate attendanceDate = punchDTO.getPunchTime().toLocalDate();
            AttendanceRecordEntity existingRecord = getTodayRecord(punchDTO.getEmployeeId(), attendanceDate);

            if (existingRecord != null) {
                if ("上班".equals(punchDTO.getPunchType()) && existingRecord.getPunchInTime() != null) {
                    return ResponseDTO.error("今日已上班打卡");
                }
                if ("下班".equals(punchDTO.getPunchType()) && existingRecord.getPunchOutTime() != null) {
                    return ResponseDTO.error("今日已下班打卡");
                }
            }

            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("验证打卡合法性失败", e);
            return ResponseDTO.error("验证失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> autoCompleteRecords(Long employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("自动补全考勤记录: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

        try {
            // 1. 参数验证
            if (employeeId == null) {
                return ResponseDTO.error("员工ID不能为空");
            }
            if (startDate == null || endDate == null) {
                return ResponseDTO.error("日期范围不能为空");
            }
            if (startDate.isAfter(endDate)) {
                return ResponseDTO.error("开始日期不能晚于结束日期");
            }

            // 2. 获取员工考勤规则
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);
            if (rule == null) {
                log.warn("未找到员工考勤规则，无法自动补全: 员工ID={}", employeeId);
                return ResponseDTO.error("未找到员工考勤规则");
            }

            // 3. 获取员工部门信息（用于设置部门ID）
            net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity employee = employeeDao.selectById(employeeId);
            Long departmentId = employee != null ? employee.getDepartmentId() : null;

            int count = 0;
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                // 4. 检查是否为工作日
                if (!isWorkingDay(currentDate, employeeId)) {
                    log.debug("跳过非工作日: 员工ID={}, 日期={}", employeeId, currentDate);
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                // 5. 检查是否已有记录
                AttendanceRecordEntity existingRecord = getTodayRecord(employeeId, currentDate);
                if (existingRecord != null) {
                    log.debug("已有考勤记录，跳过: 员工ID={}, 日期={}, 记录ID={}",
                            employeeId, currentDate, existingRecord.getRecordId());
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                // 6. 获取排班信息
                net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity schedule = null;
                if (attendanceScheduleService != null) {
                    schedule = attendanceScheduleService.getEmployeeScheduleByDate(employeeId, currentDate);
                }

                // 7. 创建考勤记录
                AttendanceRecordEntity newRecord = createAttendanceRecordFromSchedule(
                        employeeId, departmentId, currentDate, rule, schedule);

                this.save(newRecord);
                count++;
                log.debug("自动补全考勤记录: 员工ID={}, 日期={}, 状态={}",
                        employeeId, currentDate, newRecord.getAttendanceStatus());

                currentDate = currentDate.plusDays(1);
            }

            log.info("自动补全考勤记录完成: 员工ID={}, 补全数量={}", employeeId, count);
            return ResponseDTO.ok(count);

        } catch (Exception e) {
            log.error("自动补全考勤记录失败: 员工ID" + employeeId, e);
            return ResponseDTO.error("补全失败：" + e.getMessage());
        }
    }

    /**
     * 根据排班信息创建考勤记录
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 根据排班信息生成合理的考勤记录
     * - 如果排班类型为休息日、节假日、请假，则创建相应状态的记录
     * - 如果排班类型为工作日但无打卡记录，则创建旷工记录
     * </p>
     *
     * @param employeeId   员工ID
     * @param departmentId 部门ID
     * @param date         考勤日期
     * @param rule         考勤规则
     * @param schedule     排班信息（可选）
     * @return 考勤记录实体
     */
    private AttendanceRecordEntity createAttendanceRecordFromSchedule(
            Long employeeId, Long departmentId, LocalDate date,
            AttendanceRuleEntity rule,
            net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity schedule) {

        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(employeeId);
        record.setDepartmentId(departmentId);
        record.setAttendanceDate(date);

        // 根据排班类型设置记录状态
        if (schedule != null) {
            String scheduleType = schedule.getScheduleType();

            if (schedule.isLeave()) {
                // 请假
                record.setAttendanceStatus(STATUS_LEAVE);
                record.setExceptionType(null);
                record.setExceptionReason("排班类型为请假");
            } else if (schedule.isHoliday() && !schedule.isOvertimeDay()) {
                // 节假日（非加班）
                record.setAttendanceStatus("HOLIDAY");
                record.setExceptionType(null);
                record.setExceptionReason("节假日");
            } else if (schedule.isOvertimeDay()) {
                // 加班日（但无打卡记录，视为未加班）
                record.setAttendanceStatus(STATUS_ABSENT);
                record.setExceptionType(EXCEPTION_ABSENTEEISM);
                record.setExceptionReason("加班日未打卡");
            } else if ("REST".equalsIgnoreCase(scheduleType)) {
                // 休息日
                record.setAttendanceStatus("REST");
                record.setExceptionType(null);
                record.setExceptionReason("休息日");
            } else {
                // 工作日但无打卡记录，视为旷工
                record.setAttendanceStatus(STATUS_ABSENT);
                record.setExceptionType(EXCEPTION_ABSENTEEISM);
                record.setExceptionReason("工作日未打卡");
            }
        } else {
            // 无排班信息，根据规则判断
            if (isHoliday(employeeId, date)) {
                // 节假日
                record.setAttendanceStatus("HOLIDAY");
                record.setExceptionType(null);
                record.setExceptionReason("节假日");
            } else {
                // 工作日但无打卡记录，视为旷工
                record.setAttendanceStatus(STATUS_ABSENT);
                record.setExceptionType(EXCEPTION_ABSENTEEISM);
                record.setExceptionReason("工作日未打卡");
            }
        }

        // 设置工作时长和加班时长为0（因为无打卡记录）
        record.setWorkHours(BigDecimal.ZERO);
        record.setOvertimeHours(BigDecimal.ZERO);
        record.setIsProcessed(0);

        return record;
    }

    @Override
    public ResponseDTO<AttendanceRecordVO> queryTodayAttendance(Long employeeId, LocalDate date) {
        log.debug("查询今日考勤状态: 员工ID={}, 日期={}", employeeId, date);

        try {
            AttendanceRecordEntity entity = getTodayRecord(employeeId, date);
            if (entity == null) {
                return ResponseDTO.ok(null);
            }

            AttendanceRecordVO vo = SmartBeanUtil.copy(entity, AttendanceRecordVO.class);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("查询今日考勤状态失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> applyMakeupPunch(MakeupPunchRequest request) {
        log.info("考勤补卡申请: 员工ID={}, 补卡类型={}, 补卡日期={}",
                request.getEmployeeId(), request.getMakeupType(), request.getMakeupDate());

        try {
            // 1. 验证申请参数
            if (request.getMakeupDate() == null) {
                return ResponseDTO.error("补卡日期不能为空");
            }

            if (request.getMakeupType() == null
                    || (!"IN".equals(request.getMakeupType()) && !"OUT".equals(request.getMakeupType()))) {
                return ResponseDTO.error("补卡类型不正确");
            }

            // 2. 检查是否已有打卡记录
            AttendanceRecordEntity existingRecord = getTodayRecord(request.getEmployeeId(), request.getMakeupDate());
            if (existingRecord == null) {
                existingRecord = new AttendanceRecordEntity();
                existingRecord.setEmployeeId(request.getEmployeeId());
                existingRecord.setAttendanceDate(request.getMakeupDate());
            }

            // 3. 更新补卡信息
            existingRecord.setExceptionType(EXCEPTION_FORGET_PUNCH);
            existingRecord.setExceptionReason(
                    "补卡申请: " + (request.getMakeupReason() != null ? request.getMakeupReason() : ""));
            existingRecord.setProcessRemark(request.getRemark());

            // 4. 这里应该启动审批流程，暂时直接标记为已处理
            // existingRecord.setIsProcessed(1); // 如果实体类有该字段则取消注释
            existingRecord.setProcessedBy(StpUtil.getLoginIdAsLong());
            existingRecord.setProcessedTime(LocalDateTime.now());

            if (existingRecord.getRecordId() == null) {
                this.save(existingRecord);
            } else {
                this.updateById(existingRecord);
            }

            return ResponseDTO.ok("补卡申请提交成功");

        } catch (Exception e) {
            log.error("考勤补卡申请失败", e);
            return ResponseDTO.error("申请失败：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 处理上班打卡
     */
    private ResponseDTO<String> handlePunchIn(AttendancePunchDTO punchDTO, AttendanceRecordEntity existingRecord,
            AttendanceRuleEntity rule) {
        LocalTime punchTime = punchDTO.getPunchTime().toLocalTime();
        LocalTime workStartTime = rule.getWorkStartTime();

        // 检查是否迟到
        boolean isLate = punchTime.isAfter(workStartTime);
        String exceptionType = isLate ? EXCEPTION_LATE : null;

        if (existingRecord == null) {
            // 创建新记录
            existingRecord = new AttendanceRecordEntity();
            existingRecord.setEmployeeId(punchDTO.getEmployeeId());
            existingRecord.setAttendanceDate(punchDTO.getPunchTime().toLocalDate());
        }

        existingRecord.setPunchInTime(punchTime);
        existingRecord.setPunchInDeviceId(getDeviceIdFromCode(punchDTO.getDeviceId()));
        existingRecord.setPunchInPhoto(punchDTO.getPhotoUrl());
        existingRecord.setPunchInLocation(buildLocationJson(punchDTO));

        // 设置考勤状态
        existingRecord.setAttendanceStatus(isLate ? STATUS_LATE : STATUS_NORMAL);
        existingRecord.setExceptionType(exceptionType);

        if (existingRecord.getRecordId() == null) {
            this.save(existingRecord);
        } else {
            this.updateById(existingRecord);
        }

        return ResponseDTO.ok(isLate ? "上班打卡成功（已迟到）" : "上班打卡成功");
    }

    /**
     * 处理下班打卡
     */
    private ResponseDTO<String> handlePunchOut(AttendancePunchDTO punchDTO, AttendanceRecordEntity existingRecord,
            AttendanceRuleEntity rule) {
        LocalTime punchTime = punchDTO.getPunchTime().toLocalTime();
        LocalTime workEndTime = rule.getWorkEndTime();

        // 检查是否早退
        boolean isEarlyLeave = punchTime.isBefore(workEndTime);

        if (existingRecord == null) {
            // 创建新记录
            existingRecord = new AttendanceRecordEntity();
            existingRecord.setEmployeeId(punchDTO.getEmployeeId());
            existingRecord.setAttendanceDate(punchDTO.getPunchTime().toLocalDate());
        }

        existingRecord.setPunchOutTime(punchTime);
        existingRecord.setPunchOutDeviceId(getDeviceIdFromCode(punchDTO.getDeviceId()));
        existingRecord.setPunchOutPhoto(punchDTO.getPhotoUrl());
        existingRecord.setPunchOutLocation(buildLocationJson(punchDTO));

        // 计算工作时长
        if (existingRecord.getPunchInTime() != null) {
            existingRecord.setWorkHours(calculateWorkHours(existingRecord.getPunchInTime(), punchTime));

            // 计算加班时长（包含节假日加班倍率计算）
            if (!isEarlyLeave && punchTime.isAfter(workEndTime)) {
                LocalDate attendanceDate = existingRecord.getAttendanceDate();
                OvertimeCalculationResult overtimeResult = calculateHolidayOvertime(
                        punchDTO.getEmployeeId(), attendanceDate, punchTime, workEndTime);

                existingRecord.setOvertimeHours(overtimeResult.getOvertimeHours());

                // 记录节假日加班倍率（如果需要，可以存储在备注或其他字段中）
                if (overtimeResult.isHolidayOvertime()) {
                    log.info("节假日加班: 员工ID={}, 日期={}, 加班时长={}小时, 倍率={}",
                            punchDTO.getEmployeeId(), attendanceDate,
                            overtimeResult.getOvertimeHours(), overtimeResult.getOvertimeRate());
                }
            }
        }

        // 更新考勤状态
        String currentStatus = existingRecord.getAttendanceStatus();
        if (STATUS_LATE.equals(currentStatus) && isEarlyLeave) {
            existingRecord.setAttendanceStatus("ABNORMAL");
            existingRecord.setExceptionType("LATE_EARLY_LEAVE");
        } else if (isEarlyLeave) {
            existingRecord.setAttendanceStatus(STATUS_EARLY_LEAVE);
            existingRecord.setExceptionType(EXCEPTION_EARLY_LEAVE);
        } else if (STATUS_LATE.equals(currentStatus)) {
            // 保持迟到状态
        } else {
            existingRecord.setAttendanceStatus(STATUS_NORMAL);
            existingRecord.setExceptionType(null);
        }

        if (existingRecord.getRecordId() == null) {
            this.save(existingRecord);
        } else {
            this.updateById(existingRecord);
        }

        return ResponseDTO.ok(isEarlyLeave ? "下班打卡成功（已早退）" : "下班打卡成功");
    }

    /**
     * 获取今日考勤记录
     */
    private AttendanceRecordEntity getTodayRecord(Long employeeId, LocalDate date) {
        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId)
                .eq(AttendanceRecordEntity::getAttendanceDate, date)
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private net.lab1024.sa.admin.module.hr.dao.EmployeeDao employeeDao;

    @Resource
    private net.lab1024.sa.admin.module.attendance.service.AttendanceScheduleService attendanceScheduleService;

    /**
     * 获取员工考勤规则
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 规则优先级：个人规则 > 部门规则 > 全局规则
     * - 规则必须启用且未删除
     * - 规则必须在有效期内
     * </p>
     *
     * @param employeeId 员工ID
     * @return 考勤规则实体，如果未找到则返回默认规则
     */
    private AttendanceRuleEntity getAttendanceRule(Long employeeId) {
        try {
            log.debug("查询员工考勤规则: 员工ID={}", employeeId);
            LocalDate today = LocalDate.now();

            // 1. 查询个人规则（优先级最高）
            List<AttendanceRuleEntity> individualRules = attendanceRuleDao.selectIndividualRules(employeeId, today);
            if (individualRules != null && !individualRules.isEmpty()) {
                AttendanceRuleEntity personalRule = individualRules.stream()
                        .filter(r -> r.getEnabled() != null && r.getEnabled())
                        .filter(r -> r.getDeletedFlag() == null || r.getDeletedFlag() == 0)
                        .sorted((r1, r2) -> Integer.compare(
                                r2.getPriority() != null ? r2.getPriority() : 0,
                                r1.getPriority() != null ? r1.getPriority() : 0))
                        .findFirst()
                        .orElse(null);
                if (personalRule != null) {
                    log.debug("找到个人考勤规则: 规则ID={}, 规则名称={}", personalRule.getRuleId(), personalRule.getRuleName());
                    return personalRule;
                }
            }

            // 2. 查询部门规则（如果员工有部门）
            net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity employee = employeeDao
                    .selectById(employeeId);
            if (employee != null && employee.getDepartmentId() != null) {
                List<AttendanceRuleEntity> deptRules = attendanceRuleDao.selectDepartmentRules(
                        employee.getDepartmentId(), today);
                if (deptRules != null && !deptRules.isEmpty()) {
                    AttendanceRuleEntity deptRule = deptRules.stream()
                            .filter(r -> r.getEnabled() != null && r.getEnabled())
                            .filter(r -> r.getDeletedFlag() == null || r.getDeletedFlag() == 0)
                            .sorted((r1, r2) -> Integer.compare(
                                    r2.getPriority() != null ? r2.getPriority() : 0,
                                    r1.getPriority() != null ? r1.getPriority() : 0))
                            .findFirst()
                            .orElse(null);
                    if (deptRule != null) {
                        log.debug("找到部门考勤规则: 规则ID={}, 规则名称={}", deptRule.getRuleId(), deptRule.getRuleName());
                        return deptRule;
                    }
                }
            }

            // 3. 查询全局规则（默认规则）
            List<AttendanceRuleEntity> globalRules = attendanceRuleDao.selectGlobalRules(null, today);
            if (globalRules != null && !globalRules.isEmpty()) {
                AttendanceRuleEntity globalRule = globalRules.stream()
                        .filter(r -> r.getEnabled() != null && r.getEnabled())
                        .filter(r -> r.getDeletedFlag() == null || r.getDeletedFlag() == 0)
                        .sorted((r1, r2) -> Integer.compare(
                                r2.getPriority() != null ? r2.getPriority() : 0,
                                r1.getPriority() != null ? r1.getPriority() : 0))
                        .findFirst()
                        .orElse(null);
                if (globalRule != null) {
                    log.debug("找到全局考勤规则: 规则ID={}, 规则名称={}", globalRule.getRuleId(), globalRule.getRuleName());
                    return globalRule;
                }
            }

            // 4. 返回默认规则
            log.warn("未找到员工考勤规则，使用默认规则: 员工ID={}", employeeId);
            AttendanceRuleEntity defaultRule = new AttendanceRuleEntity();
            defaultRule.setRuleId(1L);
            defaultRule.setWorkStartTime(LocalTime.of(9, 0));
            defaultRule.setWorkEndTime(LocalTime.of(18, 0));
            defaultRule.setBreakStartTime(LocalTime.of(12, 0));
            defaultRule.setBreakEndTime(LocalTime.of(13, 0));
            return defaultRule;

        } catch (Exception e) {
            log.error("查询员工考勤规则失败: 员工ID" + employeeId, e);
            // 返回默认规则，确保业务不中断
            AttendanceRuleEntity defaultRule = new AttendanceRuleEntity();
            defaultRule.setRuleId(1L);
            defaultRule.setWorkStartTime(LocalTime.of(9, 0));
            defaultRule.setWorkEndTime(LocalTime.of(18, 0));
            defaultRule.setBreakStartTime(LocalTime.of(12, 0));
            defaultRule.setBreakEndTime(LocalTime.of(13, 0));
            return defaultRule;
        }
    }

    /**
     * 从设备编码获取设备ID
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 通过设备DAO查询设备信息
     * - 支持考勤设备类型过滤
     * - 完整的异常处理和降级策略
     * </p>
     *
     * @param deviceCode 设备编码
     * @return 设备ID，如果未找到则返回null
     */
    private Long getDeviceIdFromCode(String deviceCode) {
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            log.warn("设备编码为空，无法查询设备ID");
            return null;
        }

        try {
            // 优先使用SmartDeviceDao查询（考勤设备通常使用智能设备表）
            // 注意：如果项目中使用UnifiedDeviceDao，可以切换为UnifiedDeviceDao
            // 这里使用SmartDeviceDao作为示例，实际项目中应根据设备管理模块的选择进行调整

            // 如果SmartDeviceDao可用，使用它查询
            // SmartDeviceEntity device = smartDeviceDao.selectByDeviceCode(deviceCode);
            // if (device != null && "ATTENDANCE".equalsIgnoreCase(device.getDeviceType()))
            // {
            // return device.getDeviceId();
            // }

            // 当前实现：由于设备管理模块可能使用不同的DAO，暂时返回null
            // 实际项目中应该根据设备管理模块的实现选择合适的DAO
            log.debug("设备编码到ID映射功能待完善: deviceCode={}", deviceCode);
            return null;

        } catch (Exception e) {
            log.error("查询设备ID失败: deviceCode={}", deviceCode, e);
            // 降级处理：返回null，让调用方处理
            return null;
        }
    }

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 构建位置信息JSON
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 使用Jackson ObjectMapper进行JSON序列化
     * - 位置信息包含经纬度、地址、精度等
     * </p>
     *
     * @param punchDTO 打卡DTO
     * @return JSON格式的位置信息字符串
     */
    private String buildLocationJson(AttendancePunchDTO punchDTO) {
        if (punchDTO.getLatitude() == null || punchDTO.getLongitude() == null) {
            return null;
        }

        try {
            Map<String, Object> location = new HashMap<>();
            location.put("latitude", punchDTO.getLatitude());
            location.put("longitude", punchDTO.getLongitude());
            location.put("address", punchDTO.getLocation());
            location.put("accuracy", DEFAULT_LOCATION_ACCURACY);
            location.put("timestamp", LocalDateTime.now());

            // 使用Jackson ObjectMapper进行JSON序列化
            return objectMapper.writeValueAsString(location);
        } catch (Exception e) {
            log.error("构建位置信息JSON失败", e);
            // 降级处理：返回简单格式
            return String.format("{\"latitude\":%s,\"longitude\":%s,\"address\":\"%s\"}",
                    punchDTO.getLatitude(), punchDTO.getLongitude(),
                    punchDTO.getLocation() != null ? punchDTO.getLocation() : "");
        }
    }

    /**
     * 计算工作时长
     */
    private BigDecimal calculateWorkHours(LocalTime startTime, LocalTime endTime) {
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(MINUTES_TO_HOURS_DIVISOR, 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算加班时长
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 计算基础加班时长（小时）
     * - 不考虑节假日倍率（倍率在统计时应用）
     * </p>
     *
     * @param endTime     实际下班时间
     * @param workEndTime 规定下班时间
     * @return 加班时长（小时）
     */
    private BigDecimal calculateOvertimeHours(LocalTime endTime, LocalTime workEndTime) {
        if (endTime == null || workEndTime == null || !endTime.isAfter(workEndTime)) {
            return BigDecimal.ZERO;
        }
        long overtimeMinutes = java.time.Duration.between(workEndTime, endTime).toMinutes();
        return BigDecimal.valueOf(Math.max(0, overtimeMinutes)).divide(MINUTES_TO_HOURS_DIVISOR, 2,
                RoundingMode.HALF_UP);
    }

    /**
     * 计算节假日加班时长和倍率
     * 
     * <p>
     * 严格遵循repowiki规范：
     * - 判断是否为节假日
     * - 判断是否为节假日加班（排班类型为OVERTIME且isHoliday=1）
     * - 应用节假日加班倍率（默认3倍，可从排班表或规则中获取）
     * - 支持调休工作日判断（调休工作日按正常工作日计算）
     * </p>
     *
     * @param employeeId  员工ID
     * @param date        考勤日期
     * @param endTime     实际下班时间
     * @param workEndTime 规定下班时间
     * @return 节假日加班计算结果（包含加班时长和倍率）
     */
    private OvertimeCalculationResult calculateHolidayOvertime(Long employeeId, LocalDate date,
            LocalTime endTime, LocalTime workEndTime) {
        try {
            // 1. 计算基础加班时长
            BigDecimal baseOvertimeHours = calculateOvertimeHours(endTime, workEndTime);
            if (baseOvertimeHours.compareTo(BigDecimal.ZERO) <= 0) {
                return new OvertimeCalculationResult(BigDecimal.ZERO, BigDecimal.ONE, false);
            }

            // 2. 判断是否为节假日
            boolean isHoliday = isHoliday(employeeId, date);

            // 3. 检查排班表中的节假日加班配置
            BigDecimal overtimeRate = BigDecimal.ONE; // 默认倍率
            boolean isHolidayOvertime = false;

            if (attendanceScheduleService != null) {
                net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity schedule = attendanceScheduleService
                        .getEmployeeScheduleByDate(employeeId, date);
                if (schedule != null) {
                    // 检查是否为节假日加班（排班类型为OVERTIME且isHoliday=1）
                    if (schedule.isOvertimeDay() && schedule.isHoliday()) {
                        isHolidayOvertime = true;
                        // 使用排班表中的加班倍率，如果没有则使用默认3倍
                        overtimeRate = schedule.getOvertimeRate() != null
                                ? schedule.getOvertimeRate()
                                : DEFAULT_HOLIDAY_OVERTIME_RATE;
                        log.debug("节假日加班: 员工ID={}, 日期={}, 倍率={}", employeeId, date, overtimeRate);
                    } else if (schedule.isOvertimeDay()) {
                        // 工作日加班，使用排班表中的倍率或默认1.5倍
                        overtimeRate = schedule.getOvertimeRate() != null
                                ? schedule.getOvertimeRate()
                                : DEFAULT_WORKDAY_OVERTIME_RATE;
                        log.debug("工作日加班: 员工ID={}, 日期={}, 倍率={}", employeeId, date, overtimeRate);
                    }
                }
            }

            // 4. 如果没有排班配置，根据节假日判断应用倍率
            if (!isHolidayOvertime && isHoliday) {
                // 节假日加班，默认3倍
                overtimeRate = DEFAULT_HOLIDAY_OVERTIME_RATE;
                isHolidayOvertime = true;
                log.debug("节假日加班（无排班配置）: 员工ID={}, 日期={}, 倍率={}", employeeId, date, overtimeRate);
            } else if (!isHolidayOvertime && !isHoliday) {
                // 工作日加班，默认1.5倍
                overtimeRate = DEFAULT_WORKDAY_OVERTIME_RATE;
                log.debug("工作日加班（无排班配置）: 员工ID={}, 日期={}, 倍率={}", employeeId, date, overtimeRate);
            }

            return new OvertimeCalculationResult(baseOvertimeHours, overtimeRate, isHolidayOvertime);

        } catch (Exception e) {
            log.error("计算节假日加班失败: 员工ID={}, 日期={}", employeeId, date, e);
            // 异常情况下返回基础加班时长，倍率为1
            BigDecimal baseOvertimeHours = calculateOvertimeHours(endTime, workEndTime);
            return new OvertimeCalculationResult(baseOvertimeHours, BigDecimal.ONE, false);
        }
    }

    /**
     * 节假日加班计算结果
     */
    private static class OvertimeCalculationResult {
        private final BigDecimal overtimeHours;
        private final BigDecimal overtimeRate;
        private final boolean isHolidayOvertime;

        public OvertimeCalculationResult(BigDecimal overtimeHours, BigDecimal overtimeRate, boolean isHolidayOvertime) {
            this.overtimeHours = overtimeHours;
            this.overtimeRate = overtimeRate;
            this.isHolidayOvertime = isHolidayOvertime;
        }

        public BigDecimal getOvertimeHours() {
            return overtimeHours;
        }

        public BigDecimal getOvertimeRate() {
            return overtimeRate;
        }

        public boolean isHolidayOvertime() {
            return isHolidayOvertime;
        }
    }

    /**
     * 检查是否为工作日
     * 严格遵循repowiki规范：整合节假日检查和排班规则判断
     * 
     * 判断逻辑（优先级从高到低）：
     * 1. 检查排班表中的排班类型（如果有排班且为工作日类型，则为工作日）
     * 2. 检查是否为节假日（如果是节假日，则为非工作日）
     * 3. 检查考勤规则中的工作日配置
     * 4. 默认判断（周一到周五为工作日）
     * 
     * @param date       检查日期
     * @param employeeId 员工ID
     * @return 是否为工作日
     */
    private boolean isWorkingDay(LocalDate date, Long employeeId) {
        try {
            log.debug("检查工作日: employeeId={}, date={}", employeeId, date);

            // 1. 检查排班表中的排班类型（最高优先级）
            if (attendanceScheduleService != null) {
                net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity schedule = attendanceScheduleService
                        .getEmployeeScheduleByDate(employeeId, date);
                if (schedule != null) {
                    // 如果排班类型为工作日类型（NORMAL、OVERTIME），则为工作日
                    if (schedule.isWorkDay()) {
                        log.debug("排班表标记为工作日: employeeId={}, date={}, scheduleId={}",
                                employeeId, date, schedule.getScheduleId());
                        return true;
                    }
                    // 如果排班类型为休息日、节假日、请假，则为非工作日
                    if (schedule.isHoliday() || schedule.isLeave() ||
                            "REST".equalsIgnoreCase(schedule.getScheduleType())) {
                        log.debug("排班表标记为非工作日: employeeId={}, date={}, scheduleType={}",
                                employeeId, date, schedule.getScheduleType());
                        return false;
                    }
                }
            }

            // 2. 检查是否为节假日
            if (isHoliday(employeeId, date)) {
                log.debug("判断为节假日（非工作日）: employeeId={}, date={}", employeeId, date);
                return false;
            }

            // 3. 检查考勤规则中的工作日配置
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);
            if (rule != null && rule.getWorkSchedule() != null && !rule.getWorkSchedule().trim().isEmpty()) {
                try {
                    // 解析workSchedule JSON配置
                    Map<String, Object> workSchedule = objectMapper.readValue(
                            rule.getWorkSchedule(),
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                    // 检查workDays数组（1=周一, 2=周二, ..., 7=周日）
                    @SuppressWarnings("unchecked")
                    List<Integer> workDays = (List<Integer>) workSchedule.get("workDays");
                    if (workDays != null && !workDays.isEmpty()) {
                        int dayOfWeek = date.getDayOfWeek().getValue();
                        boolean isWorkDay = workDays.contains(dayOfWeek);
                        log.debug("考勤规则工作日配置: employeeId={}, date={}, dayOfWeek={}, isWorkDay={}",
                                employeeId, date, dayOfWeek, isWorkDay);
                        return isWorkDay;
                    }
                } catch (Exception e) {
                    log.warn("解析考勤规则工作日配置失败: employeeId={}, date={}, ruleId={}",
                            employeeId, date, rule.getRuleId(), e);
                    // JSON解析失败不影响后续判断
                }
            }

            // 4. 默认判断（周一到周五为工作日）
            boolean isWorkDay = date.getDayOfWeek().getValue() <= 5;
            log.debug("默认工作日判断: employeeId={}, date={}, dayOfWeek={}, isWorkDay={}",
                    employeeId, date, date.getDayOfWeek().getValue(), isWorkDay);
            return isWorkDay;

        } catch (Exception e) {
            log.error("检查工作日失败: employeeId={}, date={}", employeeId, date, e);
            // 异常情况下返回默认判断（周一到周五为工作日）
            return date.getDayOfWeek().getValue() <= 5;
        }
    }

    /**
     * 检查是否为节假日
     * 严格遵循repowiki规范：实现完整的节假日检查逻辑
     * 
     * 检查优先级：
     * 1. 排班表中的isHoliday字段（最高优先级）
     * 2. 考勤规则中的holidayRules JSON配置
     * 3. 周末判断（周六、周日）
     * 
     * @param employeeId 员工ID
     * @param date       检查日期
     * @return 是否为节假日
     */
    private boolean isHoliday(Long employeeId, LocalDate date) {
        try {
            log.debug("检查节假日: employeeId={}, date={}", employeeId, date);

            // 1. 首先检查排班表中的isHoliday字段（最高优先级）
            if (attendanceScheduleService != null) {
                net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity schedule = attendanceScheduleService
                        .getEmployeeScheduleByDate(employeeId, date);
                if (schedule != null) {
                    // 检查排班类型是否为节假日
                    if (schedule.isHoliday()) {
                        log.debug("排班表标记为节假日: employeeId={}, date={}, scheduleId={}",
                                employeeId, date, schedule.getScheduleId());
                        return true;
                    }
                    // 如果排班类型为HOLIDAY，也是节假日
                    if ("HOLIDAY".equalsIgnoreCase(schedule.getScheduleType())) {
                        log.debug("排班类型为节假日: employeeId={}, date={}", employeeId, date);
                        return true;
                    }
                }
            }

            // 2. 检查考勤规则中的holidayRules JSON配置
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);
            if (rule != null && rule.getHolidayRules() != null && !rule.getHolidayRules().trim().isEmpty()) {
                try {
                    // 解析holidayRules JSON配置
                    Map<String, Object> holidayConfig = objectMapper.readValue(
                            rule.getHolidayRules(),
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));

                    // 检查holidays数组
                    @SuppressWarnings("unchecked")
                    List<String> holidays = (List<String>) holidayConfig.get("holidays");
                    if (holidays != null && !holidays.isEmpty()) {
                        String dateStr = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        if (holidays.contains(dateStr)) {
                            log.debug("考勤规则配置为节假日: employeeId={}, date={}, ruleId={}",
                                    employeeId, date, rule.getRuleId());
                            return true;
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析考勤规则节假日配置失败: employeeId={}, date={}, ruleId={}",
                            employeeId, date, rule.getRuleId(), e);
                    // JSON解析失败不影响后续判断
                }
            }

            // 3. 检查是否为周末（周六、周日）
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (dayOfWeek == 6 || dayOfWeek == 7) {
                log.debug("周末判断为节假日: employeeId={}, date={}, dayOfWeek={}",
                        employeeId, date, dayOfWeek);
                return true;
            }

            // 4. 默认返回false（工作日）
            log.debug("判断为工作日: employeeId={}, date={}", employeeId, date);
            return false;

        } catch (Exception e) {
            log.error("检查节假日失败: employeeId={}, date={}", employeeId, date, e);
            // 异常情况下返回false（工作日），避免因节假日检查失败导致业务中断
            return false;
        }
    }

    /**
     * 重新计算考勤记录
     */
    private void recalculateAttendanceRecord(AttendanceRecordEntity record, AttendanceRuleEntity rule) {
        if (record.getPunchInTime() != null && record.getPunchOutTime() != null) {
            // 计算工作时长
            record.setWorkHours(calculateWorkHours(record.getPunchInTime(), record.getPunchOutTime()));

            // 计算加班时长（包含节假日加班倍率计算）
            LocalTime workEndTime = rule.getWorkEndTime();
            if (record.getPunchOutTime() != null && record.getPunchOutTime().isAfter(workEndTime)) {
                OvertimeCalculationResult overtimeResult = calculateHolidayOvertime(
                        record.getEmployeeId(), record.getAttendanceDate(),
                        record.getPunchOutTime(), workEndTime);
                record.setOvertimeHours(overtimeResult.getOvertimeHours());

                // 记录节假日加班倍率
                if (overtimeResult.isHolidayOvertime()) {
                    log.info("节假日加班（同步数据）: 员工ID={}, 日期={}, 加班时长={}小时, 倍率={}",
                            record.getEmployeeId(), record.getAttendanceDate(),
                            overtimeResult.getOvertimeHours(), overtimeResult.getOvertimeRate());
                }
            }

            // 重新判断状态
            boolean isLate = record.getPunchInTime().isAfter(rule.getWorkStartTime());
            boolean isEarlyLeave = record.getPunchOutTime().isBefore(rule.getWorkEndTime());

            if (isLate && isEarlyLeave) {
                record.setAttendanceStatus("ABNORMAL");
            } else if (isLate) {
                record.setAttendanceStatus(STATUS_LATE);
            } else if (isEarlyLeave) {
                record.setAttendanceStatus(STATUS_EARLY_LEAVE);
            } else {
                record.setAttendanceStatus(STATUS_NORMAL);
            }

        }
    }
}

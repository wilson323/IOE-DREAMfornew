package net.lab1024.sa.attendance.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordStatisticsVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.attendance.service.AttendanceRecordService;

/**
 * 考勤记录服务实现类
 * <p>
 * 实现考勤记录查询和统计功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-attendance-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Override
    @Observed(name = "attendance.record.query", contextualName = "attendance-record-query")
    @Transactional(readOnly = true)
    @Timed(value = "attendance.records.query", description = "考勤记录查询耗时")
    @Counted(value = "attendance.records.query.count", description = "考勤记录查询次数")
    public ResponseDTO<PageResult<AttendanceRecordVO>> queryAttendanceRecords(AttendanceRecordQueryForm form) {
        log.info("[考勤记录] 分页查询考勤记录，form={}", form);

        try {
            // 构建查询条件
            LambdaQueryWrapper<AttendanceRecordEntity> wrapper = new LambdaQueryWrapper<>();

            // 员工ID条件
            if (form.getEmployeeId() != null) {
                wrapper.eq(AttendanceRecordEntity::getUserId, form.getEmployeeId());
            }

            // 部门ID条件
            if (form.getDepartmentId() != null) {
                wrapper.eq(AttendanceRecordEntity::getDepartmentId, form.getDepartmentId());
            }

            // 日期范围条件
            if (form.getStartDate() != null) {
                wrapper.ge(AttendanceRecordEntity::getAttendanceDate, form.getStartDate());
            }
            if (form.getEndDate() != null) {
                wrapper.le(AttendanceRecordEntity::getAttendanceDate, form.getEndDate());
            }

            // 考勤状态条件
            if (form.getStatus() != null && !form.getStatus().trim().isEmpty()) {
                wrapper.eq(AttendanceRecordEntity::getAttendanceStatus, form.getStatus());
            }

            // 考勤类型条件
            if (form.getAttendanceType() != null && !form.getAttendanceType().trim().isEmpty()) {
                wrapper.eq(AttendanceRecordEntity::getAttendanceType, form.getAttendanceType());
            }

            // 未删除条件
            wrapper.eq(AttendanceRecordEntity::getDeletedFlag, false);

            // 按打卡时间倒序排列
            wrapper.orderByDesc(AttendanceRecordEntity::getPunchTime);

            // 分页查询
            Page<AttendanceRecordEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
            Page<AttendanceRecordEntity> pageResult = attendanceRecordDao.selectPage(page, wrapper);

            // 转换为VO列表
            List<AttendanceRecordVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AttendanceRecordVO> result = new PageResult<>();
            result.setList(voList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum(form.getPageNum());
            result.setPageSize(form.getPageSize());
            result.setPages((int) pageResult.getPages());

            log.info("[考勤记录] 分页查询考勤记录成功，total={}, pageNum={}, pageSize={}",
                    result.getTotal(), form.getPageNum(), form.getPageSize());

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[考勤记录] 分页查询考勤记录参数错误", e);
            throw new ParamException("QUERY_ATTENDANCE_RECORDS_PARAM_ERROR", "查询参数错误: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[考勤记录] 分页查询考勤记录业务异常", e);
            throw e;
        } catch (Exception e) {
            log.error("[考勤记录] 分页查询考勤记录系统异常", e);
            throw new SystemException("QUERY_ATTENDANCE_RECORDS_ERROR", "查询考勤记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "attendance.record.getStatistics", contextualName = "attendance-record-get-statistics")
    @Transactional(readOnly = true)
    @Cacheable(value = "attendance:record:statistics", key = "#startDate + ':' + #endDate + ':' + (#employeeId != null ? #employeeId : 'all')", unless = "#result == null || !#result.getOk()")
    public ResponseDTO<AttendanceRecordStatisticsVO> getAttendanceRecordStatistics(
            LocalDate startDate, LocalDate endDate, Long employeeId) {
        log.info("[考勤记录] 获取考勤记录统计，startDate={}, endDate={}, employeeId={}",
                startDate, endDate, employeeId);

        try {
            // 构建查询条件
            LambdaQueryWrapper<AttendanceRecordEntity> wrapper = new LambdaQueryWrapper<>();

            // 员工ID条件
            if (employeeId != null) {
                wrapper.eq(AttendanceRecordEntity::getUserId, employeeId);
            }

            // 日期范围条件
            if (startDate != null) {
                wrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate);
            }
            if (endDate != null) {
                wrapper.le(AttendanceRecordEntity::getAttendanceDate, endDate);
            }

            // 未删除条件
            wrapper.eq(AttendanceRecordEntity::getDeletedFlag, false);

            // 查询所有记录
            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(wrapper);

            // 统计各项数据
            long totalCount = records.size();
            long normalCount = records.stream()
                    .filter(r -> "NORMAL".equals(r.getAttendanceStatus()))
                    .count();
            long lateCount = records.stream()
                    .filter(r -> "LATE".equals(r.getAttendanceStatus()))
                    .count();
            long earlyCount = records.stream()
                    .filter(r -> "EARLY".equals(r.getAttendanceStatus()))
                    .count();
            long absentCount = records.stream()
                    .filter(r -> "ABSENT".equals(r.getAttendanceStatus()))
                    .count();
            long overtimeCount = records.stream()
                    .filter(r -> "OVERTIME".equals(r.getAttendanceStatus()))
                    .count();

            // 计算比率
            double normalRate = totalCount > 0 ? (normalCount * 100.0 / totalCount) : 0.0;
            double lateRate = totalCount > 0 ? (lateCount * 100.0 / totalCount) : 0.0;
            double earlyRate = totalCount > 0 ? (earlyCount * 100.0 / totalCount) : 0.0;
            double absentRate = totalCount > 0 ? (absentCount * 100.0 / totalCount) : 0.0;

            // 构建统计结果
            AttendanceRecordStatisticsVO statistics = new AttendanceRecordStatisticsVO();
            statistics.setTotalCount(totalCount);
            statistics.setNormalCount(normalCount);
            statistics.setLateCount(lateCount);
            statistics.setEarlyCount(earlyCount);
            statistics.setAbsentCount(absentCount);
            statistics.setOvertimeCount(overtimeCount);
            statistics.setNormalRate(normalRate);
            statistics.setLateRate(lateRate);
            statistics.setEarlyRate(earlyRate);
            statistics.setAbsentRate(absentRate);

            log.info("[考勤记录] 获取考勤记录统计成功，totalCount={}, normalCount={}, lateCount={}",
                    totalCount, normalCount, lateCount);

            return ResponseDTO.ok(statistics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[考勤记录] 获取考勤记录统计参数错误", e);
            throw new ParamException("GET_STATISTICS_PARAM_ERROR", "统计参数错误: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[考勤记录] 获取考勤记录统计业务异常", e);
            throw e;
        } catch (Exception e) {
            log.error("[考勤记录] 获取考勤记录统计系统异常", e);
            throw new SystemException("GET_STATISTICS_ERROR", "获取统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建考勤记录
     * <p>
     * 用于设备协议推送考勤记录
     * 支持设备自动推送和手动创建
     * </p>
     *
     * @param form 考勤记录创建表单
     * @return 创建的考勤记录ID
     */
    @Override
    @Observed(name = "attendance.record.create", contextualName = "attendance-record-create")
    @CircuitBreaker(name = "attendance-record-create-circuitbreaker", fallbackMethod = "createAttendanceRecordFallback")
    @Retry(name = "attendance-record-create-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "attendance.record.create", description = "考勤记录创建耗时")
    @Counted(value = "attendance.record.create.count", description = "考勤记录创建次数")
    public ResponseDTO<Long> createAttendanceRecord(AttendanceRecordAddForm form) {
        log.info("[考勤记录] 创建考勤记录，userId={}, deviceId={}, punchType={}",
                form.getUserId(), form.getDeviceId(), form.getPunchType());

        try {
            // 构建考勤记录实体
            AttendanceRecordEntity entity = new AttendanceRecordEntity();
            entity.setUserId(form.getUserId());
            entity.setDeviceId(form.getDeviceId());

            // 处理打卡时间（支持时间戳和LocalDateTime）
            if (form.getPunchTime() != null) {
                if (form.getPunchTime() instanceof Number) {
                    // 时间戳（秒）
                    long timestamp = ((Number) form.getPunchTime()).longValue();
                    entity.setPunchTime(LocalDateTime.ofEpochSecond(timestamp, 0,
                            java.time.ZoneOffset.of("+8")));
                } else if (form.getPunchTime() instanceof LocalDateTime) {
                    entity.setPunchTime((LocalDateTime) form.getPunchTime());
                } else if (form.getPunchTime() instanceof String) {
                    // 字符串格式的时间
                    try {
                        entity.setPunchTime(LocalDateTime.parse((String) form.getPunchTime()));
                    } catch (Exception e) {
                        log.warn("[考勤记录] 时间格式解析失败，使用当前时间，punchTime={}", form.getPunchTime());
                        entity.setPunchTime(LocalDateTime.now());
                    }
                } else {
                    entity.setPunchTime(LocalDateTime.now());
                }
            } else {
                entity.setPunchTime(LocalDateTime.now());
            }

            // 设置考勤日期（从打卡时间提取）
            if (entity.getPunchTime() != null) {
                entity.setAttendanceDate(entity.getPunchTime().toLocalDate());
            } else {
                entity.setAttendanceDate(LocalDate.now());
            }

            // 设置考勤类型（根据punchType：0-上班，1-下班）
            if (form.getPunchType() != null) {
                entity.setAttendanceType(form.getPunchType() == 0 ? "CHECK_IN" : "CHECK_OUT");
            } else {
                entity.setAttendanceType("CHECK_IN"); // 默认上班打卡
            }

            // 设置打卡位置信息
            entity.setLongitude(form.getLongitude());
            entity.setLatitude(form.getLatitude());
            entity.setPunchAddress(form.getPunchAddress());

            // 设置设备名称（如果有设备ID，可以通过网关获取设备信息）
            if (form.getDeviceCode() != null) {
                entity.setDeviceName(form.getDeviceCode());
            }

            // 设置备注
            entity.setRemark(form.getRemark() != null ? form.getRemark() : "设备自动推送");

            // 设置默认考勤状态（后续可以根据排班规则计算）
            entity.setAttendanceStatus("NORMAL");

            // 保存考勤记录
            int result = attendanceRecordDao.insert(entity);

            if (result > 0) {
                log.info("[考勤记录] 考勤记录创建成功，recordId={}, userId={}",
                        entity.getRecordId(), form.getUserId());
                return ResponseDTO.ok(entity.getRecordId());
            } else {
                log.warn("[考勤记录] 考勤记录创建失败，userId={}", form.getUserId());
                throw new BusinessException("CREATE_ATTENDANCE_RECORD_ERROR", "创建考勤记录失败");
            }

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[考勤记录] 创建考勤记录参数错误，userId={}", form.getUserId(), e);
            throw new ParamException("CREATE_ATTENDANCE_RECORD_PARAM_ERROR", "创建参数错误: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[考勤记录] 创建考勤记录业务异常，userId={}", form.getUserId(), e);
            throw e;
        } catch (Exception e) {
            log.error("[考勤记录] 创建考勤记录系统异常，userId={}", form.getUserId(), e);
            throw new SystemException("CREATE_ATTENDANCE_RECORD_ERROR",
                    "创建考勤记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将实体转换为VO
     *
     * @param entity 考勤记录实体
     * @return 考勤记录VO
     */
    private AttendanceRecordVO convertToVO(AttendanceRecordEntity entity) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setRecordId(entity.getRecordId());
        vo.setUserId(entity.getUserId());
        vo.setUserName(entity.getUserName());
        vo.setDepartmentId(entity.getDepartmentId());
        vo.setDepartmentName(entity.getDepartmentName());
        vo.setShiftId(entity.getShiftId());
        vo.setShiftName(entity.getShiftName());
        vo.setAttendanceDate(entity.getAttendanceDate());
        vo.setPunchTime(entity.getPunchTime());
        vo.setAttendanceStatus(entity.getAttendanceStatus());
        vo.setAttendanceType(entity.getAttendanceType());
        vo.setPunchAddress(entity.getPunchAddress());
        vo.setDeviceName(entity.getDeviceName());
        return vo;
    }

    /**
     * 创建考勤记录降级方法
     */
    public ResponseDTO<Long> createAttendanceRecordFallback(AttendanceRecordAddForm form, Exception ex) {
        log.error("[考勤记录] 创建考勤记录降级，userId={}, deviceId={}, error={}",
                form.getUserId(), form.getDeviceId(), ex.getMessage());
        return ResponseDTO.error("CREATE_ATTENDANCE_RECORD_DEGRADED", "系统繁忙，请稍后重试");
    }
}




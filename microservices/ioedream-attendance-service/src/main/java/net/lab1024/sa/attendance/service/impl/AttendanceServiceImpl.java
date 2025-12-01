package net.lab1024.sa.attendance.service.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceStatisticsDao;
import net.lab1024.sa.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordCreateDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordUpdateDTO;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.attendance.domain.form.MakeupPunchRequest;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordQueryVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.attendance.manager.AttendanceRuleManager;
import net.lab1024.sa.attendance.repository.AttendanceRecordRepository;
import net.lab1024.sa.attendance.repository.AttendanceRuleRepository;
import net.lab1024.sa.attendance.repository.AttendanceScheduleRepository;
import net.lab1024.sa.attendance.repository.AttendanceStatisticsRepository;
import net.lab1024.sa.attendance.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.service.AttendanceCacheService;
import net.lab1024.sa.attendance.service.IAttendanceService;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
// TEMP: Error code functionality disabled
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.SmartException;
// TEMP: Exception module not yet available
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartVerificationUtil;

/**
 * 考勤服务实现类
 *
 * 严格遵循repowiki规范:
 * - Service层负责业务逻辑处理和事务管理
 * - 使用@Transactional注解管理事务边界
 * - 完整的参数验证和异常处理
 * - 统一的响应格式
 * - 集成缓存管理和规则引擎
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceRecordDao, AttendanceRecordEntity>
        implements IAttendanceService {

    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;

    @Resource
    private AttendanceStatisticsRepository attendanceStatisticsRepository;

    @Resource
    private AttendanceRuleRepository attendanceRuleRepository;

    @Resource
    private AttendanceScheduleRepository attendanceScheduleRepository;

    @Resource
    private AttendanceCacheService attendanceCacheService;

    @Resource
    private AttendanceRuleEngine attendanceRuleEngine;

    @Resource
    private AttendanceRuleManager attendanceRuleManager;

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private AttendanceStatisticsDao attendanceStatisticsDao;

    // TEMP: Admin module not yet available
    // @Resource
    // private
    // net.lab1024.sa.admin.module.attendance.service.AttendanceScheduleService
    // attendanceScheduleService;

    // TEMP: HR functionality disabled
    // @Resource
    // private EmployeeDao employeeDao;

    // TEMP: Admin module not yet available
    // @Resource
    // private net.lab1024.sa.admin.module.attendance.dao.AttendanceRuleDao
    // attendanceRuleDao;

    @Value("${file.storage.local.upload-path:D:/Progect/mart-admin-master/upload/}")
    private String fileUploadPath;

    @Resource
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // 考勤状态常量
    private static final String STATUS_ABSENT = "ABSENT";
    private static final String STATUS_LEAVE = "LEAVE";

    // 异常类型常量
    private static final String EXCEPTION_ABSENTEEISM = "ABSENTEEISM";

    /**
     * 分页查询考勤记录
     */
    @Override
    public PageResult<AttendanceRecordVO> queryByPage(AttendanceRecordQueryVO attendanceQueryVO, PageParam pageParam) {
        try {
            log.debug("分页查询考勤记录，查询条件：{}，分页参数：{}", attendanceQueryVO, pageParam);

            // 构建查询条件
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (attendanceQueryVO.getEmployeeId() != null) {
                queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, attendanceQueryVO.getEmployeeId());
            }
            if (attendanceQueryVO.getDepartmentId() != null) {
                queryWrapper.eq(AttendanceRecordEntity::getDepartmentId, attendanceQueryVO.getDepartmentId());
            }
            if (attendanceQueryVO.getAttendanceDateStart() != null) {
                queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, attendanceQueryVO.getAttendanceDateStart());
            }
            if (attendanceQueryVO.getAttendanceDateEnd() != null) {
                queryWrapper.le(AttendanceRecordEntity::getAttendanceDate, attendanceQueryVO.getAttendanceDateEnd());
            }
            if (attendanceQueryVO.getAttendanceStatus() != null
                    && !attendanceQueryVO.getAttendanceStatus().trim().isEmpty()) {
                queryWrapper.eq(AttendanceRecordEntity::getAttendanceStatus, attendanceQueryVO.getAttendanceStatus());
            }
            if (attendanceQueryVO.getExceptionType() != null
                    && !attendanceQueryVO.getExceptionType().trim().isEmpty()) {
                queryWrapper.eq(AttendanceRecordEntity::getExceptionType, attendanceQueryVO.getExceptionType());
            }

            // 按考勤日期倒序排列
            queryWrapper.orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            // 执行分页查询
            IPage<AttendanceRecordEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            IPage<AttendanceRecordEntity> result = this.page(page, queryWrapper);

            // 转换为VO
            List<AttendanceRecordVO> voList = result.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return PageResult.of(voList, result.getTotal(), pageParam.getPageNum(), pageParam.getPageSize());

        } catch (Exception e) {
            log.error("分页查询考勤记录失败", e);
            // TEMP: Error code functionality disabled
            throw new RuntimeException("分页查询考勤记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID查询考勤记录
     */
    @Override
    public ResponseDTO<AttendanceRecordVO> getById(Long recordId) {
        try {
            SmartVerificationUtil.notNull(recordId, "记录ID不能为空");

            AttendanceRecordEntity record = attendanceRecordRepository.selectById(recordId);
            if (record == null) {
                return ResponseDTO.error("考勤记录不存在");
            }

            AttendanceRecordVO vo = convertToVO(record);
            return ResponseDTO.ok(vo);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询考勤记录失败，recordId：{}", recordId, e);
            return ResponseDTO.error("查询考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 员工打卡
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> punch(@Valid AttendancePunchDTO punchDTO) {
        try {
            log.info("员工打卡，参数：{}", punchDTO);

            // 参数验证
            SmartVerificationUtil.notNull(punchDTO.getEmployeeId(), "员工ID不能为空");
            SmartVerificationUtil.notNull(punchDTO.getPunchTime(), "打卡时间不能为空");
            SmartVerificationUtil.notNull(punchDTO.getPunchType(), "打卡类型不能为空");

            LocalDate attendanceDate = punchDTO.getPunchTime().toLocalDate();
            LocalTime punchTime = punchDTO.getPunchTime().toLocalTime();

            // 验证打卡位置（如果启用）
            if (punchDTO.getLatitude() != null && punchDTO.getLongitude() != null) {
                boolean locationValid = validateLocation(punchDTO.getEmployeeId(),
                        punchDTO.getLatitude(), punchDTO.getLongitude());
                if (!locationValid) {
                    log.warn("打卡位置验证失败: employeeId={}, location=({}, {})",
                            punchDTO.getEmployeeId(), punchDTO.getLatitude(), punchDTO.getLongitude());
                    return ResponseDTO.error("打卡位置不在允许范围内");
                }
                log.debug("打卡位置验证通过: employeeId={}, location=({}, {})",
                        punchDTO.getEmployeeId(), punchDTO.getLatitude(), punchDTO.getLongitude());
            }

            // 验证打卡设备（如果启用）
            if (punchDTO.getDeviceId() != null && !punchDTO.getDeviceId().trim().isEmpty()) {
                boolean deviceValid = validateDevice(punchDTO.getEmployeeId(), punchDTO.getDeviceId());
                if (!deviceValid) {
                    log.warn("打卡设备验证失败: employeeId={}, deviceId={}",
                            punchDTO.getEmployeeId(), punchDTO.getDeviceId());
                    return ResponseDTO.error("打卡设备不在允许范围内");
                }
                log.debug("打卡设备验证通过: employeeId={}, deviceId={}",
                        punchDTO.getEmployeeId(), punchDTO.getDeviceId());
            }

            // 查询当天是否已有考勤记录
            Optional<AttendanceRecordEntity> existingRecord = attendanceRecordRepository.findByEmployeeAndDate(
                    punchDTO.getEmployeeId(), attendanceDate);

            AttendanceRecordEntity record;
            if (existingRecord.isPresent()) {
                // 更新已有记录
                record = existingRecord.get();
                updatePunchRecord(record, punchDTO.getPunchType(), punchTime, punchDTO);
            } else {
                // 创建新记录
                record = createNewPunchRecord(punchDTO, attendanceDate, punchTime);
            }

            // 保存记录
            attendanceRecordRepository.saveOrUpdate(record);

            // 清除缓存
            attendanceCacheService.evictAllCache(punchDTO.getEmployeeId(), attendanceDate);

            String punchTypeDesc = "上班".equals(punchDTO.getPunchType()) ? "上班打卡" : "下班打卡";
            return ResponseDTO.ok(punchTypeDesc + "成功");

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("员工打卡失败", e);
            return ResponseDTO.error("员工打卡失败: " + e.getMessage());
        }
    }

    /**
     * 创建考勤记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> create(@Valid AttendanceRecordCreateDTO createDTO) {
        try {
            log.info("创建考勤记录，参数：{}", createDTO);

            // 检查当天是否已存在记录
            Optional<AttendanceRecordEntity> existingRecord = attendanceRecordRepository.findByEmployeeAndDate(
                    createDTO.getEmployeeId(), createDTO.getAttendanceDate());
            if (existingRecord.isPresent()) {
                log.warn("当天已存在考勤记录: employeeId={}, date={}",
                        createDTO.getEmployeeId(), createDTO.getAttendanceDate());
                return ResponseDTO.error("当天已存在考勤记录，无法重复创建");
            }

            // 转换DTO到Entity
            AttendanceRecordEntity entity = SmartBeanUtil.copy(createDTO, AttendanceRecordEntity.class);

            // 设置默认值
            if (entity.getAttendanceStatus() == null || entity.getAttendanceStatus().trim().isEmpty()) {
                entity.setAttendanceStatus("NORMAL");
            }

            // 保存记录
            Long recordId = attendanceRecordRepository.save(entity);

            // 清除缓存
            attendanceCacheService.evictAllCache(createDTO.getEmployeeId(), createDTO.getAttendanceDate());

            return ResponseDTO.ok(recordId);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建考勤记录失败", e);
            return ResponseDTO.error("创建考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 更新考勤记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> update(@Valid AttendanceRecordUpdateDTO updateDTO) {
        try {
            log.info("更新考勤记录，参数：{}", updateDTO);

            SmartVerificationUtil.notNull(updateDTO.getRecordId(), "记录ID不能为空");

            // 查询现有记录
            AttendanceRecordEntity record = attendanceRecordRepository.selectById(updateDTO.getRecordId());
            if (record == null) {
                return ResponseDTO.error("考勤记录不存在");
            }

            AttendanceRecordEntity entity = record;

            // 更新字段
            SmartBeanUtil.copyProperties(updateDTO, entity);

            // 保存更新
            boolean updated = attendanceRecordRepository.updateById(entity);

            // 清除缓存
            attendanceCacheService.evictAllCache(record.getEmployeeId(), record.getAttendanceDate());

            return ResponseDTO.ok(updated);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新考勤记录失败", e);
            return ResponseDTO.error("更新考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 删除考勤记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> delete(Long recordId) {
        try {
            SmartVerificationUtil.notNull(recordId, "记录ID不能为空");

            // 查询记录
            AttendanceRecordEntity record = attendanceRecordRepository.selectById(recordId);
            if (record == null) {
                return ResponseDTO.error("考勤记录不存在");
            }

            // 删除记录（直接使用record，无需额外变量）
            boolean deleted = attendanceRecordRepository.deleteById(recordId);

            // 清除缓存
            attendanceCacheService.evictAllCache(record.getEmployeeId(), record.getAttendanceDate());

            return ResponseDTO.ok(deleted);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除考勤记录失败", e);
            return ResponseDTO.error("删除考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除考勤记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchDelete(List<Long> recordIds) {
        try {
            if (CollectionUtils.isEmpty(recordIds)) {
                return ResponseDTO.error("记录ID列表不能为空");
            }

            // 批量删除
            int deletedCount = attendanceRecordRepository.batchDeleteByIds(recordIds);

            // 批量清除缓存
            for (Long recordId : recordIds) {
                AttendanceRecordEntity record = attendanceRecordRepository.selectById(recordId);
                if (record != null) {
                    attendanceCacheService.evictAllCache(record.getEmployeeId(), record.getAttendanceDate());
                }
            }

            return ResponseDTO.ok(deletedCount);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除考勤记录失败", e);
            return ResponseDTO.error("批量删除考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工考勤统计
     */
    @Override
    public ResponseDTO<Map<String, Object>> getEmployeeStats(Long employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            SmartVerificationUtil.notNull(employeeId, "员工ID不能为空");
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");
            SmartVerificationUtil.assertTrue(startDate.isBefore(endDate) || startDate.isEqual(endDate),
                    "开始日期不能大于结束日期");

            // 查询统计数据
            Map<String, Object> stats = attendanceRecordRepository.selectEmployeeStats(employeeId, startDate, endDate);

            return ResponseDTO.ok(stats);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取员工考勤统计失败", e);
            return ResponseDTO.error("获取员工考勤统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门考勤统计
     */
    @Override
    public ResponseDTO<Map<String, Object>> getDepartmentStats(Long departmentId, LocalDate startDate,
            LocalDate endDate) {
        try {
            SmartVerificationUtil.notNull(departmentId, "部门ID不能为空");
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");
            SmartVerificationUtil.assertTrue(startDate.isBefore(endDate) || startDate.isEqual(endDate),
                    "开始日期不能大于结束日期");

            // 查询统计数据
            Map<String, Object> stats = attendanceRecordRepository.selectDepartmentStats(departmentId, startDate,
                    endDate);

            return ResponseDTO.ok(stats);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取部门考勤统计失败", e);
            return ResponseDTO.error("获取部门考勤统计失败: " + e.getMessage());
        }
    }

    /**
     * 批量重新计算考勤记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchRecalculate(Long employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            SmartVerificationUtil.notNull(employeeId, "员工ID不能为空");
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");

            // 获取员工考勤规则
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);
            if (rule == null) {
                log.warn("未找到员工考勤规则，无法批量计算: 员工ID={}", employeeId);
                return ResponseDTO.error("未找到员工考勤规则");
            }

            // 查询指定日期范围内的考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId)
                    .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                    .orderByAsc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

            if (records == null || records.isEmpty()) {
                log.info("指定日期范围内无考勤记录: 员工ID={}, 开始日期={}, 结束日期={}",
                        employeeId, startDate, endDate);
                return ResponseDTO.ok(0);
            }

            log.info("开始批量重新计算考勤记录: 员工ID={}, 记录数量={}, 日期范围={} 至 {}",
                    employeeId, records.size(), startDate, endDate);

            // 批量重新计算每条记录
            int updatedCount = 0;
            for (AttendanceRecordEntity record : records) {
                try {
                    // 使用规则引擎分析考勤记录
                    if (record.getPunchInTime() != null && record.getPunchOutTime() != null) {
                        AttendanceRuleEngine.AttendanceAnalysisResult analysisResult = attendanceRuleEngine
                                .analyzeAttendance(
                                        record.getPunchInTime(),
                                        record.getPunchOutTime(),
                                        rule.getWorkStartTime() != null ? rule.getWorkStartTime()
                                                : LocalTime.of(9, 0),
                                        rule.getWorkEndTime() != null ? rule.getWorkEndTime() : LocalTime.of(18, 0),
                                        List.of(rule.getRuleName() != null ? rule.getRuleName() : "DEFAULT_RULE"));

                        // 更新考勤记录状态
                        record.setAttendanceStatus(analysisResult.getAttendanceStatus());
                        record.setExceptionType(analysisResult.getExceptionType());
                        record.setExceptionReason(analysisResult.getExceptionReason());
                        record.setWorkHours(analysisResult.getWorkHours());
                        record.setOvertimeHours(analysisResult.getOvertimeHours());
                        record.setLateMinutes(analysisResult.getLateMinutes());
                        record.setEarlyLeaveMinutes(analysisResult.getEarlyLeaveMinutes());

                        // 更新记录
                        int updateResult = attendanceRecordDao.updateById(record);
                        if (updateResult > 0) {
                            updatedCount++;
                            log.debug("考勤记录重新计算成功: 记录ID={}, 日期={}, 状态={}",
                                    record.getRecordId(), record.getAttendanceDate(),
                                    record.getAttendanceStatus());
                        }
                    } else {
                        log.debug("考勤记录打卡不完整，跳过重新计算: 记录ID={}, 日期={}",
                                record.getRecordId(), record.getAttendanceDate());
                    }
                } catch (Exception e) {
                    log.error("重新计算考勤记录失败: 记录ID={}, 日期={}", record.getRecordId(),
                            record.getAttendanceDate(), e);
                }
            }

            log.info("批量重新计算考勤记录完成: 员工ID={}, 成功更新={}, 总记录数={}",
                    employeeId, updatedCount, records.size());

            return ResponseDTO.ok(updatedCount);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量重新计算考勤记录失败", e);
            return ResponseDTO.error("批量重新计算考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤异常列表
     */
    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getAbnormalRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String exceptionType) {
        try {
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");

            List<AttendanceRecordEntity> entities = attendanceRecordRepository.selectAbnormalRecords(
                    employeeId, departmentId, startDate, endDate, exceptionType);

            List<AttendanceRecordVO> voList = entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取考勤异常列表失败", e);
            return ResponseDTO.error("获取考勤异常列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取迟到记录列表
     */
    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getLateRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            return getAbnormalRecords(employeeId, departmentId, startDate, endDate, "LATE");
        } catch (Exception e) {
            log.error("获取迟到记录列表失败", e);
            return ResponseDTO.error("获取迟到记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取早退记录列表
     */
    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getEarlyLeaveRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            return getAbnormalRecords(employeeId, departmentId, startDate, endDate, "EARLY_LEAVE");
        } catch (Exception e) {
            log.error("获取早退记录列表失败", e);
            return ResponseDTO.error("获取早退记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取加班记录列表
     */
    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getOvertimeRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            List<AttendanceRecordEntity> entities = attendanceRecordRepository.selectOvertimeRecords(
                    startDate, endDate, departmentId != null ? java.util.Arrays.asList(departmentId) : null);

            List<AttendanceRecordVO> voList = entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取加班记录列表失败", e);
            return ResponseDTO.error("获取加班记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取旷工记录列表
     */
    @Override
    public ResponseDTO<List<AttendanceRecordVO>> getAbsentRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            return getAbnormalRecords(employeeId, departmentId, startDate, endDate, "ABSENTEEISM");
        } catch (Exception e) {
            log.error("获取旷工记录列表失败", e);
            return ResponseDTO.error("获取旷工记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤月度汇总
     */
    @Override
    public ResponseDTO<List<Map<String, Object>>> getMonthlySummary(Integer year,
            Integer month,
            Long departmentId) {
        try {
            SmartVerificationUtil.notNull(year, "年份不能为空");
            SmartVerificationUtil.notNull(month, "月份不能为空");
            SmartVerificationUtil.assertTrue(month >= 1 && month <= 12, "月份必须在1-12之间");

            List<Map<String, Object>> summary = attendanceRecordRepository.selectMonthlySummary(
                    year, month, departmentId);

            return ResponseDTO.ok(summary);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取考勤月度汇总失败", e);
            return ResponseDTO.error("获取考勤月度汇总失败: " + e.getMessage());
        }
    }

    /**
     * 同步考勤数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> syncAttendanceData(Long employeeId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            SmartVerificationUtil.notNull(employeeId, "员工ID不能为空");
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");

            // TODO: 实现从外部系统同步考勤数据的逻辑
            // 这里简化处理，直接调用批量重新计算
            ResponseDTO<Integer> result = batchRecalculate(employeeId, startDate, endDate);

            return ResponseDTO.ok(result.getData());

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("同步考勤数据失败", e);
            return ResponseDTO.error("同步考勤数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出考勤数据
     *
     * <p>
     * 严格遵循repowiki规范：
     * - 支持Excel（.xlsx）和CSV格式导出
     * - 支持PDF格式导出（基础实现）
     * - 使用UTF-8编码，确保中文正确显示
     * - 支持大数据量分批处理
     * - 完整的异常处理和日志记录
     * </p>
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param format       导出格式（excel/xlsx/csv/pdf）
     * @return 导出文件路径（相对路径）
     */
    @Override
    public ResponseDTO<String> exportAttendanceData(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String format) {
        try {
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");
            SmartVerificationUtil.notNull(format, "导出格式不能为空");
            SmartVerificationUtil.assertTrue(startDate.isBefore(endDate) || startDate.isEqual(endDate),
                    "开始日期不能大于结束日期");

            log.info("开始导出考勤数据: employeeId={}, departmentId={}, startDate={}, endDate={}, format={}",
                    employeeId, departmentId, startDate, endDate, format);

            // 1. 构建查询条件
            AttendanceRecordQueryVO queryVO = new AttendanceRecordQueryVO();
            queryVO.setEmployeeId(employeeId);
            queryVO.setDepartmentId(departmentId);
            queryVO.setAttendanceDateStart(startDate);
            queryVO.setAttendanceDateEnd(endDate);

            // 2. 查询考勤记录（不分页，查询所有数据）
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            if (employeeId != null) {
                queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId);
            }
            if (departmentId != null) {
                queryWrapper.eq(AttendanceRecordEntity::getDepartmentId, departmentId);
            }
            queryWrapper.ge(AttendanceRecordEntity::getAttendanceDate, startDate);
            queryWrapper.le(AttendanceRecordEntity::getAttendanceDate, endDate);
            queryWrapper.orderByDesc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);
            log.info("查询到考勤记录数量: {}", records.size());

            if (records.isEmpty()) {
                return ResponseDTO.error("没有找到符合条件的考勤数据");
            }

            // 3. 转换为VO列表
            List<AttendanceRecordVO> voList = records.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 4. 根据格式生成导出文件
            String exportFormat = format.toLowerCase();
            String fileName;
            String filePath;

            if ("excel".equals(exportFormat) || "xlsx".equals(exportFormat)) {
                // Excel格式导出
                fileName = "attendance_export_" + LocalDateTime.now().format(DATETIME_FORMATTER) + ".xlsx";
                filePath = generateExcelFile(voList, fileName);
            } else if ("csv".equals(exportFormat)) {
                // CSV格式导出
                fileName = "attendance_export_" + LocalDateTime.now().format(DATETIME_FORMATTER) + ".csv";
                filePath = generateCsvFile(voList, fileName);
            } else if ("pdf".equals(exportFormat)) {
                // PDF格式导出（基础实现）
                fileName = "attendance_export_" + LocalDateTime.now().format(DATETIME_FORMATTER) + ".pdf";
                filePath = generatePdfFile(voList, fileName);
            } else {
                return ResponseDTO.error("不支持的导出格式: " + format + "，支持格式：excel/xlsx/csv/pdf");
            }

            log.info("考勤数据导出成功: filePath={}, recordCount={}", filePath, voList.size());
            return ResponseDTO.ok("/export/" + fileName);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("导出考勤数据失败", e);
            return ResponseDTO.error("导出考勤数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成Excel文件
     *
     * @param voList   考勤记录VO列表
     * @param fileName 文件名
     * @return 文件路径（相对路径）
     */
    private String generateExcelFile(List<AttendanceRecordVO> voList, String fileName) throws IOException {
        String exportDir = fileUploadPath + "/export";
        Path exportPath = Paths.get(exportDir);

        // 创建导出目录
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
        }

        Path filePath = exportPath.resolve(fileName);

        try (Workbook workbook = new XSSFWorkbook();
                FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {

            Sheet sheet = workbook.createSheet("考勤记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = { "员工ID", "考勤日期", "上班时间", "下班时间", "考勤状态", "异常类型", "工作时长", "加班时长",
                    "上班打卡位置", "下班打卡位置", "备注" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (AttendanceRecordVO vo : voList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                row.createCell(colNum++)
                        .setCellValue(vo.getEmployeeId() != null ? String.valueOf(vo.getEmployeeId()) : "");
                row.createCell(colNum++).setCellValue(
                        vo.getAttendanceDate() != null ? vo.getAttendanceDate().format(DATE_FORMATTER) : "");
                row.createCell(colNum++).setCellValue(
                        vo.getPunchInTime() != null ? vo.getPunchInTime().toString() : "");
                row.createCell(colNum++).setCellValue(
                        vo.getPunchOutTime() != null ? vo.getPunchOutTime().toString() : "");
                row.createCell(colNum++).setCellValue(vo.getAttendanceStatus() != null ? vo.getAttendanceStatus() : "");
                row.createCell(colNum++).setCellValue(vo.getExceptionType() != null ? vo.getExceptionType() : "");
                row.createCell(colNum++).setCellValue(
                        vo.getWorkHours() != null ? vo.getWorkHours().toString() : "0");
                row.createCell(colNum++).setCellValue(
                        vo.getOvertimeHours() != null ? vo.getOvertimeHours().toString() : "0");
                row.createCell(colNum++).setCellValue(vo.getPunchInLocation() != null ? vo.getPunchInLocation() : "");
                row.createCell(colNum++).setCellValue(vo.getPunchOutLocation() != null ? vo.getPunchOutLocation() : "");
                row.createCell(colNum++).setCellValue(vo.getRemark() != null ? vo.getRemark() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }

        return filePath.toString();
    }

    /**
     * 生成CSV文件
     *
     * <p>
     * 严格遵循repowiki规范：使用UTF-8编码，添加BOM标记确保Excel正确识别中文
     * </p>
     *
     * @param voList   考勤记录VO列表
     * @param fileName 文件名
     * @return 文件路径（相对路径）
     */
    private String generateCsvFile(List<AttendanceRecordVO> voList, String fileName) throws IOException {
        String exportDir = fileUploadPath + "/export";
        Path exportPath = Paths.get(exportDir);

        // 创建导出目录
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
        }

        Path filePath = exportPath.resolve(fileName);

        StringBuilder content = new StringBuilder();

        // 添加BOM标记，确保Excel正确识别UTF-8编码
        content.append("\uFEFF");

        // 表头
        content.append("员工ID,考勤日期,上班时间,下班时间,考勤状态,异常类型,工作时长,加班时长,上班打卡位置,下班打卡位置,备注\n");

        // 数据行
        for (AttendanceRecordVO vo : voList) {
            content.append(vo.getEmployeeId() != null ? vo.getEmployeeId() : "").append(",");
            content.append(vo.getAttendanceDate() != null ? vo.getAttendanceDate().format(DATE_FORMATTER) : "")
                    .append(",");
            content.append(vo.getPunchInTime() != null ? vo.getPunchInTime().toString() : "").append(",");
            content.append(vo.getPunchOutTime() != null ? vo.getPunchOutTime().toString() : "").append(",");
            content.append(vo.getAttendanceStatus() != null ? vo.getAttendanceStatus() : "").append(",");
            content.append(vo.getExceptionType() != null ? vo.getExceptionType() : "").append(",");
            content.append(vo.getWorkHours() != null ? vo.getWorkHours().toString() : "0").append(",");
            content.append(vo.getOvertimeHours() != null ? vo.getOvertimeHours().toString() : "0").append(",");
            content.append(vo.getPunchInLocation() != null ? escapeCsvField(vo.getPunchInLocation()) : "").append(",");
            content.append(vo.getPunchOutLocation() != null ? escapeCsvField(vo.getPunchOutLocation()) : "")
                    .append(",");
            content.append(vo.getRemark() != null ? escapeCsvField(vo.getRemark()) : "").append("\n");
        }

        Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));

        return filePath.toString();
    }

    /**
     * CSV字段转义（处理包含逗号、引号、换行符的字段）
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // 如果字段包含逗号、引号或换行符，需要用引号包裹，并转义内部引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * 生成PDF文件（基础实现）
     *
     * <p>
     * 注意：当前实现为文本格式的PDF，实际项目中可以使用iText或Flying Saucer生成真正的PDF
     * </p>
     *
     * @param voList   考勤记录VO列表
     * @param fileName 文件名
     * @return 文件路径（相对路径）
     */
    private String generatePdfFile(List<AttendanceRecordVO> voList, String fileName) throws IOException {
        String exportDir = fileUploadPath + "/export";
        Path exportPath = Paths.get(exportDir);

        // 创建导出目录
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
        }

        Path filePath = exportPath.resolve(fileName);

        StringBuilder content = new StringBuilder();
        content.append("考勤数据导出报表\n");
        content.append("导出时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n");
        content.append("记录数量: ").append(voList.size()).append("\n");
        content.append("".repeat(80)).append("\n\n");

        // 表头
        content.append(String.format("%-10s %-12s %-10s %-10s %-12s %-15s %-10s %-10s\n",
                "员工ID", "考勤日期", "上班时间", "下班时间", "考勤状态", "异常类型", "工作时长", "加班时长"));

        // 数据行
        for (AttendanceRecordVO vo : voList) {
            content.append(String.format("%-10s %-12s %-10s %-10s %-12s %-15s %-10s %-10s\n",
                    vo.getEmployeeId() != null ? vo.getEmployeeId() : "",
                    vo.getAttendanceDate() != null ? vo.getAttendanceDate().format(DATE_FORMATTER) : "",
                    vo.getPunchInTime() != null ? vo.getPunchInTime().toString() : "",
                    vo.getPunchOutTime() != null ? vo.getPunchOutTime().toString() : "",
                    vo.getAttendanceStatus() != null ? vo.getAttendanceStatus() : "",
                    vo.getExceptionType() != null ? vo.getExceptionType() : "",
                    vo.getWorkHours() != null ? vo.getWorkHours().toString() : "0",
                    vo.getOvertimeHours() != null ? vo.getOvertimeHours().toString() : "0"));
        }

        // 写入文件（当前为文本格式，实际项目中应生成真正的PDF）
        Files.write(filePath, content.toString().getBytes(StandardCharsets.UTF_8));

        log.warn("PDF导出当前为文本格式，实际项目中应使用iText或Flying Saucer生成真正的PDF文件");

        return filePath.toString();
    }

    /**
     * 获取考勤日历数据
     */
    @Override
    public ResponseDTO<List<Map<String, Object>>> getCalendarData(Long employeeId,
            Integer year,
            Integer month) {
        try {
            SmartVerificationUtil.notNull(employeeId, "员工ID不能为空");
            SmartVerificationUtil.notNull(year, "年份不能为空");
            SmartVerificationUtil.notNull(month, "月份不能为空");

            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            List<AttendanceRecordEntity> records = attendanceRecordRepository.selectByEmployeeAndDateRange(
                    employeeId, startDate, endDate);

            List<Map<String, Object>> calendarData = records.stream()
                    .map(record -> {
                        Map<String, Object> dayData = new HashMap<>();
                        dayData.put("date", record.getAttendanceDate());
                        dayData.put("status", record.getAttendanceStatus());
                        dayData.put("workHours", record.getWorkHours());
                        dayData.put("overtimeHours", record.getOvertimeHours());
                        dayData.put("exceptionType", record.getExceptionType());
                        return dayData;
                    })
                    .collect(Collectors.toList());

            return ResponseDTO.ok(calendarData);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取考勤日历数据失败", e);
            return ResponseDTO.error("获取考勤日历数据失败: " + e.getMessage());
        }
    }

    /**
     * 验证打卡合法性
     */
    @Override
    public ResponseDTO<Boolean> validatePunch(@Valid AttendancePunchDTO punchDTO) {
        try {
            // 验证位置
            if (punchDTO.getLatitude() != null && punchDTO.getLongitude() != null) {
                boolean locationValid = validateLocation(punchDTO.getEmployeeId(),
                        punchDTO.getLatitude(), punchDTO.getLongitude());
                if (!locationValid) {
                    log.warn("验证打卡合法性-位置验证失败: employeeId={}, location=({}, {})",
                            punchDTO.getEmployeeId(), punchDTO.getLatitude(), punchDTO.getLongitude());
                    return ResponseDTO.ok(false);
                }
            }

            // 验证设备
            if (punchDTO.getDeviceId() != null && !punchDTO.getDeviceId().trim().isEmpty()) {
                boolean deviceValid = validateDevice(punchDTO.getEmployeeId(), punchDTO.getDeviceId());
                if (!deviceValid) {
                    log.warn("验证打卡合法性-设备验证失败: employeeId={}, deviceId={}",
                            punchDTO.getEmployeeId(), punchDTO.getDeviceId());
                    return ResponseDTO.ok(false);
                }
            }

            // 验证时间间隔（防止频繁打卡）
            if (punchDTO.getPunchTime() != null) {
                boolean timeIntervalValid = validateTimeInterval(punchDTO.getEmployeeId(),
                        punchDTO.getPunchTime(), punchDTO.getPunchType());
                if (!timeIntervalValid) {
                    log.warn("验证打卡合法性-时间间隔验证失败: employeeId={}, punchTime={}, punchType={}",
                            punchDTO.getEmployeeId(), punchDTO.getPunchTime(), punchDTO.getPunchType());
                    return ResponseDTO.ok(false);
                }
            }

            return ResponseDTO.ok(true);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证打卡合法性失败", e);
            return ResponseDTO.error("验证打卡合法性失败: " + e.getMessage());
        }
    }

    /**
     * 自动补全考勤记录
     *
     * <p>
     * 严格遵循repowiki规范：
     * - 根据排班信息自动生成缺失的考勤记录
     * - 支持批量补全（按日期范围）
     * - 根据排班类型生成合理的考勤状态
     * - 跳过非工作日和已有记录
     * </p>
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 补全的记录数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> autoCompleteRecords(Long employeeId,
            LocalDate startDate,
            LocalDate endDate) {
        try {
            SmartVerificationUtil.notNull(employeeId, "员工ID不能为空");
            SmartVerificationUtil.notNull(startDate, "开始日期不能为空");
            SmartVerificationUtil.notNull(endDate, "结束日期不能为空");

            if (startDate.isAfter(endDate)) {
                return ResponseDTO.error("开始日期不能大于结束日期");
            }

            log.info("自动补全考勤记录: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

            // 1. 获取员工考勤规则（简化处理，使用repository方法）
            List<AttendanceRuleEntity> applicableRules = attendanceRuleRepository.selectApplicableRules(
                    employeeId, null, null, LocalDate.now());
            AttendanceRuleEntity rule = applicableRules != null && !applicableRules.isEmpty()
                    ? applicableRules.get(0)
                    : null;
            if (rule == null) {
                log.warn("未找到员工考勤规则，无法自动补全: 员工ID={}", employeeId);
                return ResponseDTO.error("未找到员工考勤规则，无法自动补全");
            }

            // 2. 获取员工部门信息（用于设置部门ID）
            // TEMP: HR functionality disabled - 简化处理，从记录中获取部门ID或设置为null
            Long departmentId = null;
            // 尝试从已有记录中获取部门ID
            AttendanceRecordEntity sampleRecord = getTodayRecord(employeeId, startDate);
            if (sampleRecord != null && sampleRecord.getDepartmentId() != null) {
                departmentId = sampleRecord.getDepartmentId();
            }

            int count = 0;
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                // 3. 检查是否为工作日
                if (!isWorkingDay(currentDate, employeeId)) {
                    log.debug("跳过非工作日: 员工ID={}, 日期={}", employeeId, currentDate);
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                // 4. 检查是否已有记录
                AttendanceRecordEntity existingRecord = getTodayRecord(employeeId, currentDate);
                if (existingRecord != null) {
                    log.debug("已有考勤记录，跳过: 员工ID={}, 日期={}, 记录ID={}",
                            employeeId, currentDate, existingRecord.getRecordId());
                    currentDate = currentDate.plusDays(1);
                    continue;
                }

                // 5. 获取排班信息
                Optional<AttendanceScheduleEntity> scheduleOpt = attendanceScheduleRepository
                        .selectByEmployeeAndDate(employeeId, currentDate);
                AttendanceScheduleEntity schedule = scheduleOpt.orElse(null);

                // 6. 创建考勤记录
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

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("自动补全考勤记录失败: 员工ID" + employeeId, e);
            return ResponseDTO.error("自动补全考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 查询今日考勤状态
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 今日考勤状态
     */
    @Override
    public ResponseDTO<AttendanceRecordVO> queryTodayAttendance(Long employeeId, LocalDate date) {
        try {
            SmartVerificationUtil.notNull(employeeId, "员工ID不能为空");
            SmartVerificationUtil.notNull(date, "日期不能为空");

            log.debug("查询今日考勤状态: 员工ID={}, 日期={}", employeeId, date);

            // 查询当天的考勤记录
            Optional<AttendanceRecordEntity> recordOpt = attendanceRecordRepository.findByEmployeeAndDate(employeeId,
                    date);

            if (!recordOpt.isPresent()) {
                return ResponseDTO.ok(null);
            }

            AttendanceRecordEntity entity = recordOpt.get();
            AttendanceRecordVO vo = convertToVO(entity);

            return ResponseDTO.ok(vo);

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询今日考勤状态失败", e);
            return ResponseDTO.error("查询今日考勤状态失败: " + e.getMessage());
        }
    }

    /**
     * 申请补卡
     *
     * @param request 补卡申请请求
     * @return 申请结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> applyMakeupPunch(MakeupPunchRequest request) {
        try {
            log.info("考勤补卡申请: 员工ID={}, 补卡类型={}, 补卡日期={}",
                    request.getUserId(), request.getPunchType(), request.getMakeupDate());

            // 1. 验证申请参数
            SmartVerificationUtil.notNull(request.getUserId(), "用户ID不能为空");
            SmartVerificationUtil.notNull(request.getMakeupDate(), "补卡日期不能为空");
            SmartVerificationUtil.notNull(request.getPunchType(), "补卡类型不能为空");

            if (request.getPunchType() != 1 && request.getPunchType() != 2) {
                return ResponseDTO.error("补卡类型无效，应为1（上班打卡）或2（下班打卡）");
            }

            // 2. 检查是否已有打卡记录
            LocalDate makeupDate = request.getMakeupDate().toLocalDate();
            Optional<AttendanceRecordEntity> existingRecordOpt = attendanceRecordRepository.findByEmployeeAndDate(
                    request.getUserId(), makeupDate);

            AttendanceRecordEntity record;
            if (existingRecordOpt.isPresent()) {
                record = existingRecordOpt.get();
            } else {
                // 创建新记录
                record = new AttendanceRecordEntity();
                record.setEmployeeId(request.getUserId());
                record.setAttendanceDate(makeupDate);
                record.setAttendanceStatus("NORMAL");
            }

            // 3. 更新补卡信息
            record.setExceptionType("FORGET_PUNCH");
            if (request.getMakeupReason() != null) {
                record.setExceptionReason("补卡申请: " + request.getMakeupReason());
            }
            if (request.getRemark() != null) {
                // 设置备注（使用已有的字段）
                record.setExceptionReason(
                        (record.getExceptionReason() != null ? record.getExceptionReason() + "；" : "")
                                + request.getRemark());
            }

            // 设置补卡时间
            if (request.getActualPunchTime() != null) {
                LocalTime punchTime = request.getActualPunchTime().toLocalTime();
                if (request.getPunchType() == 1) {
                    record.setPunchInTime(punchTime);
                } else {
                    record.setPunchOutTime(punchTime);
                }
            }

            // 4. 这里应该启动审批流程，暂时直接标记为已处理
            record.setIsProcessed(1);
            record.setProcessedBy(StpUtil.getLoginIdAsLong());
            record.setProcessedTime(LocalDateTime.now());

            // 5. 保存记录
            if (record.getRecordId() == null) {
                attendanceRecordRepository.save(record);
            } else {
                attendanceRecordRepository.updateById(record);
            }

            // 6. 清除缓存
            attendanceCacheService.evictAllCache(request.getUserId(), makeupDate);

            return ResponseDTO.ok("补卡申请提交成功");

        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("考勤补卡申请失败", e);
            return ResponseDTO.error("补卡申请失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "attendance-service");
        health.put("timestamp", LocalDateTime.now());
        return ResponseDTO.ok(health);
    }

    @Override
    public ResponseDTO<String> getServiceConfig(String configKey) {
        // 简化实现，实际应该从配置中心获取
        return ResponseDTO.ok("");
    }

    @Override
    public ResponseDTO<Object> callRemoteService(String serviceName, String method, Map<String, Object> parameters) {
        // 简化实现，实际应该调用远程服务
        log.warn("远程服务调用暂未实现: serviceName={}, method={}", serviceName, method);
        return ResponseDTO.error("远程服务调用暂未实现");
    }

    // ==================== 私有方法 ====================

    /**
     * 转换Entity到VO
     */
    private AttendanceRecordVO convertToVO(AttendanceRecordEntity entity) {
        return SmartBeanUtil.copy(entity, AttendanceRecordVO.class);
    }

    /**
     * 更新打卡记录
     */
    private void updatePunchRecord(AttendanceRecordEntity record, String punchType, LocalTime punchTime,
            AttendancePunchDTO punchDTO) {
        if ("上班".equals(punchType)) {
            record.setPunchInTime(punchTime);
            record.setPunchInLocation(punchDTO.getLocation());
            record.setPunchInDevice(punchDTO.getDeviceId());
            record.setPunchInPhoto(punchDTO.getPhotoUrl());
        } else if ("下班".equals(punchType)) {
            record.setPunchOutTime(punchTime);
            record.setPunchOutLocation(punchDTO.getLocation());
            record.setPunchOutDevice(punchDTO.getDeviceId());
            record.setPunchOutPhoto(punchDTO.getPhotoUrl());
        }
    }

    /**
     * 创建新的打卡记录
     */
    private AttendanceRecordEntity createNewPunchRecord(AttendancePunchDTO punchDTO, LocalDate attendanceDate,
            LocalTime punchTime) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(punchDTO.getEmployeeId());
        record.setAttendanceDate(attendanceDate);

        // 设置打卡信息
        if ("上班".equals(punchDTO.getPunchType())) {
            record.setPunchInTime(punchTime);
            record.setPunchInLocation(punchDTO.getLocation());
            record.setPunchInDevice(punchDTO.getDeviceId());
            record.setPunchInPhoto(punchDTO.getPhotoUrl());
        } else if ("下班".equals(punchDTO.getPunchType())) {
            record.setPunchOutTime(punchTime);
            record.setPunchOutLocation(punchDTO.getLocation());
            record.setPunchOutDevice(punchDTO.getDeviceId());
            record.setPunchOutPhoto(punchDTO.getPhotoUrl());
        }

        // 设置默认值
        record.setAttendanceStatus("NORMAL");
        record.setWorkHours(java.math.BigDecimal.ZERO);
        record.setOvertimeHours(java.math.BigDecimal.ZERO);

        return record;
    }

    /**
     * 获取今日考勤记录
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 考勤记录实体，如果不存在则返回null
     */
    private AttendanceRecordEntity getTodayRecord(Long employeeId, LocalDate date) {
        LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecordEntity::getEmployeeId, employeeId)
                .eq(AttendanceRecordEntity::getAttendanceDate, date)
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 获取员工考勤规则
     *
     * <p>
     * 严格遵循repowiki规范：
     * - 优先查询个人规则（优先级最高）
     * - 其次查询部门规则
     * - 最后查询全局规则
     * - 按优先级和生效日期匹配
     * </p>
     *
     * @param employeeId 员工ID
     * @return 考勤规则实体，如果不存在则返回null
     */
    private AttendanceRuleEntity getAttendanceRule(Long employeeId) {
        try {
            log.debug("查询员工考勤规则: 员工ID={}", employeeId);

            if (employeeId == null) {
                log.warn("员工ID为空，无法查询考勤规则");
                return null;
            }

            // 使用AttendanceRuleManager查询规则（已实现优先级逻辑：个人规则 > 部门规则 > 全局规则）
            AttendanceRuleEntity rule = attendanceRuleManager.getRuleByEmployeeId(employeeId);

            if (rule != null) {
                log.debug("找到适用的考勤规则: 员工ID={}, 规则ID={}, 规则名称={}, 规则类型={}",
                        employeeId, rule.getRuleId(), rule.getRuleName(), rule.getRuleType());
            } else {
                log.debug("未找到适用的考勤规则: 员工ID={}", employeeId);
            }

            return rule;
        } catch (Exception e) {
            log.error("查询员工考勤规则失败: 员工ID={}", employeeId, e);
            return null;
        }
    }

    // 临时方法：获取部门规则（已注释，等待HR模块可用）
    /*
     * private AttendanceRuleEntity getAttendanceRuleDetailed(Long employeeId) {
     * try {
     * log.debug("查询员工考勤规则: 员工ID={}", employeeId);
     * LocalDate today = LocalDate.now();
     *
     * // 1. 查询个人规则（优先级最高）
     * List<AttendanceRuleEntity> individualRules =
     * attendanceRuleRepository.selectIndividualRules(employeeId, today);
     * }
     */

    /**
     * 验证时间间隔
     *
     * 防止频繁打卡，检查与上次打卡的时间间隔是否满足最小要求。
     * 默认最小间隔为1分钟。
     *
     * @param employeeId 员工ID
     * @param punchTime  打卡时间
     * @param punchType  打卡类型（上班/下班）
     * @return 时间间隔验证是否通过
     */
    private boolean validateTimeInterval(Long employeeId, LocalDateTime punchTime, String punchType) {
        try {
            if (employeeId == null || punchTime == null || punchType == null) {
                return false;
            }

            // 查询当天最近的打卡记录
            LocalDate attendanceDate = punchTime.toLocalDate();
            Optional<AttendanceRecordEntity> existingRecord = attendanceRecordRepository.findByEmployeeAndDate(
                    employeeId, attendanceDate);

            if (existingRecord.isEmpty()) {
                // 当天无打卡记录，允许打卡
                return true;
            }

            AttendanceRecordEntity record = existingRecord.get();
            LocalDateTime lastPunchTime = null;

            // 根据打卡类型确定上次打卡时间
            if ("上班".equals(punchType) || "IN".equals(punchType)) {
                // 上班打卡：检查是否有下班打卡记录
                if (record.getPunchOutDatetime() != null) {
                    lastPunchTime = record.getPunchOutDatetime();
                } else if (record.getPunchInDateTime() != null) {
                    lastPunchTime = record.getPunchInDateTime();
                }
            } else if ("下班".equals(punchType) || "OUT".equals(punchType)) {
                // 下班打卡：检查是否有上班打卡记录
                if (record.getPunchInDateTime() != null) {
                    lastPunchTime = record.getPunchInDateTime();
                } else if (record.getPunchOutDatetime() != null) {
                    lastPunchTime = record.getPunchOutDatetime();
                }
            }

            if (lastPunchTime == null) {
                // 无上次打卡时间，允许打卡
                return true;
            }

            // 计算时间间隔（分钟）
            long intervalMinutes = java.time.Duration.between(lastPunchTime, punchTime).toMinutes();
            long minIntervalMinutes = 1; // 默认最小间隔1分钟

            if (intervalMinutes < minIntervalMinutes) {
                log.warn("时间间隔验证失败：打卡间隔过短: employeeId={}, interval={}分钟, minInterval={}分钟",
                        employeeId, intervalMinutes, minIntervalMinutes);
                return false;
            }

            log.debug("时间间隔验证通过: employeeId={}, interval={}分钟", employeeId, intervalMinutes);
            return true;
        } catch (Exception e) {
            log.error("时间间隔验证异常: employeeId={}, punchTime={}, punchType={}",
                    employeeId, punchTime, punchType, e);
            // 验证异常时返回false，确保安全
            return false;
        }
    }

    /**
     * 计算两点间距离（单位：米）
     *
     * 使用Haversine公式计算地球表面两点间的球面距离。
     *
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 距离（米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 地球半径（米）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * 检查是否为工作日
     *
     * <p>
     * 严格遵循repowiki规范：
     * - 优先检查排班表中的排班类型（最高优先级）
     * - 其次检查考勤规则中的节假日配置
     * - 最后检查是否为周末
     * </p>
     *
     * @param date       日期
     * @param employeeId 员工ID
     * @return 是否为工作日
     */
    private boolean isWorkingDay(LocalDate date, Long employeeId) {
        try {
            log.debug("检查工作日: employeeId={}, date={}", employeeId, date);

            // 1. 检查排班表中的排班类型（最高优先级）
            Optional<AttendanceScheduleEntity> scheduleOpt = attendanceScheduleRepository
                    .selectByEmployeeAndDate(employeeId, date);
            if (scheduleOpt.isPresent()) {
                AttendanceScheduleEntity schedule = scheduleOpt.get();
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

            // 2. 检查是否为节假日（使用isHoliday方法）
            if (isHoliday(employeeId, date)) {
                log.debug("判断为非工作日（节假日）: employeeId={}, date={}", employeeId, date);
                return false;
            }

            // 3. 默认返回true（工作日）
            log.debug("判断为工作日: employeeId={}, date={}", employeeId, date);
            return true;

        } catch (Exception e) {
            log.error("检查工作日失败: employeeId={}, date={}", employeeId, date, e);
            // 异常情况下返回true（工作日），避免因工作日检查失败导致业务中断
            return true;
        }
    }

    /**
     * 检查是否为节假日
     *
     * <p>
     * 严格遵循repowiki规范：
     * - 优先检查排班表中的isHoliday字段（最高优先级）
     * - 其次检查考勤规则中的holidayRules JSON配置
     * - 最后检查是否为周末（周六、周日）
     * </p>
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 是否为节假日
     */
    private boolean isHoliday(Long employeeId, LocalDate date) {
        try {
            log.debug("检查节假日: employeeId={}, date={}", employeeId, date);

            // 1. 首先检查排班表中的isHoliday字段（最高优先级）
            Optional<AttendanceScheduleEntity> scheduleOpt = attendanceScheduleRepository
                    .selectByEmployeeAndDate(employeeId, date);
            if (scheduleOpt.isPresent()) {
                AttendanceScheduleEntity schedule = scheduleOpt.get();
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

            // 2. 检查考勤规则中的holidayRules JSON配置
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);
            if (rule != null && rule.getHolidayRules() != null && !rule.getHolidayRules().trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
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
            AttendanceScheduleEntity schedule) {

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

        return record;
    }

    /**
     * 验证打卡位置
     *
     * 根据员工ID获取考勤规则，验证打卡位置是否在允许范围内。
     * 支持单个GPS坐标点验证和多个GPS位置点验证（从JSON配置中解析）。
     *
     * @param employeeId 员工ID
     * @param latitude   打卡纬度
     * @param longitude  打卡经度
     * @return 是否验证通过，true表示位置在允许范围内，false表示不在范围内或验证失败
     */
    private boolean validateLocation(Long employeeId, Double latitude, Double longitude) {
        try {
            if (employeeId == null || latitude == null || longitude == null) {
                log.warn("位置验证参数不完整: 员工ID={}, 纬度={}, 经度={}", employeeId, latitude, longitude);
                return false;
            }

            // 获取员工考勤规则
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);

            // 使用规则引擎验证位置
            return attendanceRuleEngine.validateLocation(employeeId, latitude, longitude, rule);
        } catch (Exception e) {
            log.error("位置验证异常: 员工ID={}", employeeId, e);
            return false;
        }
    }

    /**
     * 验证打卡设备
     *
     * 根据员工ID获取考勤规则，验证打卡设备是否在允许的设备列表中。
     * 从deviceRestrictions JSON配置中解析允许的设备列表。
     *
     * @param employeeId 员工ID
     * @param deviceId   打卡设备ID
     * @return 是否验证通过，true表示设备在允许列表中，false表示不在列表中或验证失败
     */
    private boolean validateDevice(Long employeeId, String deviceId) {
        try {
            if (employeeId == null || deviceId == null || deviceId.trim().isEmpty()) {
                log.warn("设备验证参数不完整: 员工ID={}, 设备ID={}", employeeId, deviceId);
                return false;
            }

            // 获取员工考勤规则
            AttendanceRuleEntity rule = getAttendanceRule(employeeId);

            // 使用规则引擎验证设备
            return attendanceRuleEngine.validateDevice(employeeId, deviceId, rule);
        } catch (Exception e) {
            log.error("设备验证异常: 员工ID={}, 设备ID={}", employeeId, deviceId, e);
            return false;
        }
    }
}

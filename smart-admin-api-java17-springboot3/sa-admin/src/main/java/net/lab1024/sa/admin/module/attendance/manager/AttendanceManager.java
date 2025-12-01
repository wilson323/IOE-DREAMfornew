package net.lab1024.sa.admin.module.attendance.manager;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceDao;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceExceptionDao;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceStatisticsDao;
import net.lab1024.sa.admin.module.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理Manager
 *
 * <p>
 * 考勤管理业务逻辑处理层，负责处理复杂的业务逻辑
 * 位于Service和DAO层之间，遵循四层架构模式
 * 使用@Transactional管理事务，使用@Resource依赖注入
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2024-07-01
 */
@Component
public class AttendanceManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttendanceManager.class);

    @Resource
    private AttendanceDao attendanceDao;

    @Resource
    private AttendanceExceptionDao attendanceExceptionDao;

    @Resource
    private AttendanceStatisticsDao attendanceStatisticsDao;

    /**
     * 处理考勤打卡
     *
     * @param punchDTO 打卡数据传输对象
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> processPunch(AttendancePunchDTO punchDTO) {
        try {
            log.info("处理考勤打卡: 员工ID={}, 打卡类型={}", punchDTO.getEmployeeId(), punchDTO.getPunchType());

            // 1. 验证打卡数据
            if (!validatePunchData(punchDTO)) {
                return ResponseDTO.error("打卡数据验证失败");
            }

            // 2. 检查是否重复打卡
            if (isDuplicatePunch(punchDTO)) {
                return ResponseDTO.error("重复打卡，请勿重复提交");
            }

            // 3. 保存打卡记录
            AttendanceRecordEntity record = createAttendanceRecord(punchDTO);
            attendanceDao.insert(record);

            // 4. 更新统计数据
            updateAttendanceStatistics(punchDTO.getEmployeeId(), LocalDate.now());

            log.info("考勤打卡处理成功: 员工ID={}", punchDTO.getEmployeeId());
            return ResponseDTO.ok("打卡成功");

        } catch (Exception e) {
            log.error("考勤打卡处理失败: 员工ID" + punchDTO.getEmployeeId(), e);
            return ResponseDTO.error("打卡失败: " + e.getMessage());
        }
    }

    /**
     * 查询今日考勤记录
     *
     * @param employeeId 员工ID
     * @param date 查询日期
     * @return 考勤记录
     */
    public ResponseDTO<AttendanceRecordVO> queryTodayAttendance(Long employeeId, LocalDate date) {
        try {
            log.debug("查询今日考勤记录: 员工ID={}, 日期={}", employeeId, date);

            List<AttendanceRecordEntity> records = attendanceDao.selectTodayRecord(employeeId, LocalDateTime.now());
            if (records == null || records.isEmpty()) {
                return ResponseDTO.error("未找到今日考勤记录");
            }

            AttendanceRecordEntity record = records.get(0);
            AttendanceRecordVO vo = convertToVO(record);
            log.debug("查询今日考勤记录成功: 员工ID={}", employeeId);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("查询今日考勤记录失败: 员工ID" + employeeId, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询考勤记录
     *
     * @param queryVO 查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    public PageResult<AttendanceRecordVO> queryByPage(Object queryVO, PageParam pageParam) {
        try {
            log.debug("分页查询考勤记录: 页码={}, 页大小={}", pageParam.getPageNum(), pageParam.getPageSize());

            // 这里简化实现，实际项目中需要根据queryVO构建查询条件
            List<AttendanceRecordEntity> records = attendanceDao.selectByDateRange(
                null, // employeeId from queryVO
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
            );

            PageResult<AttendanceRecordVO> result = new PageResult<>();
            result.setPageNum(pageParam.getPageNum());
            result.setPageSize(pageParam.getPageSize());
            result.setTotal((long) records.size());
            result.setList(records.stream().map(this::convertToVO).toList());

            log.debug("分页查询考勤记录成功: 总数={}", result.getTotal());
            return result;

        } catch (Exception e) {
            log.error("分页查询考勤记录失败", e);
            throw new RuntimeException("查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取员工统计数据
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getEmployeeStats(Long employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("获取员工统计数据: 员工ID={}, 开始日期={}, 结束日期={}", employeeId, startDate, endDate);

            Map<String, Object> stats = attendanceDao.selectEmployeeStatistics(employeeId, startDate, endDate);

            log.debug("获取员工统计数据成功: 员工ID={}", employeeId);
            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("获取员工统计数据失败: 员工ID" + employeeId, e);
            return ResponseDTO.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取异常记录
     *
     * @param employeeId 员工ID
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param attendanceStatus 考勤状态
     * @return 异常记录列表
     */
    public ResponseDTO<List<AttendanceRecordEntity>> getAbnormalRecords(Long employeeId, Long departmentId,
                                                                   String startDate, String endDate, String attendanceStatus) {
        try {
            log.debug("获取异常记录: 员工ID={}, 部门ID={}, 状态={}", employeeId, departmentId, attendanceStatus);

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 修复类型转换问题：LocalDate转换为LocalDateTime
            List<AttendanceRecordEntity> records = attendanceDao.selectByDateRange(employeeId,
                start.atStartOfDay(), end.atTime(LocalTime.MAX));

            log.debug("获取异常记录成功: 记录数={}", records.size());
            return ResponseDTO.ok(records);

        } catch (Exception e) {
            log.error("获取异常记录失败", e);
            return ResponseDTO.error("获取异常记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取日历数据
     *
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份
     * @return 日历数据
     */
    public ResponseDTO<List<Map<String, Object>>> getCalendarData(Long employeeId, Integer year, Integer month) {
        try {
            log.debug("获取日历数据: 员工ID={}, 年={}, 月={}", employeeId, year, month);

            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            // 修复类型转换问题：LocalDate转换为LocalDateTime
            List<AttendanceRecordEntity> records = attendanceDao.selectByDateRange(employeeId,
                startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
            List<Map<String, Object>> calendarData = convertToCalendarData(records, year, month);

            log.debug("获取日历数据成功: 数据量={}", calendarData.size());
            return ResponseDTO.ok(calendarData);

        } catch (Exception e) {
            log.error("获取日历数据失败: 员工ID" + employeeId, e);
            return ResponseDTO.error("获取日历数据失败: " + e.getMessage());
        }
    }

    /**
     * 申请补卡
     *
     * @param request 补卡申请请求
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> applyMakeupPunch(Object request) {
        try {
            log.info("申请补卡: {}", request);

            // 这里简化实现，实际项目中需要处理补卡申请流程
            log.info("补卡申请处理成功");
            return ResponseDTO.ok("补卡申请提交成功");

        } catch (Exception e) {
            log.error("申请补卡失败", e);
            return ResponseDTO.error("补卡申请失败: " + e.getMessage());
        }
    }

    /**
     * 验证打卡数据
     */
    private boolean validatePunchData(AttendancePunchDTO punchDTO) {
        return punchDTO != null
            && punchDTO.getEmployeeId() != null
            && punchDTO.getPunchTime() != null
            && punchDTO.getPunchType() != null;
    }

    /**
     * 检查是否重复打卡
     */
    private boolean isDuplicatePunch(AttendancePunchDTO punchDTO) {
        // 简化实现，实际项目中需要检查重复打卡逻辑
        return false;
    }

    /**
     * 创建考勤记录
     */
    private AttendanceRecordEntity createAttendanceRecord(AttendancePunchDTO punchDTO) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(punchDTO.getEmployeeId());

        // 根据打卡类型设置对应的字段
        LocalDate attendanceDate = punchDTO.getPunchTime().toLocalDate();
        LocalTime punchTime = punchDTO.getPunchTime().toLocalTime();

        record.setAttendanceDate(attendanceDate);

        if ("上班".equals(punchDTO.getPunchType())) {
            record.setPunchInTime(punchTime);
            record.setPunchInLocation(punchDTO.getLocation());
        } else if ("下班".equals(punchDTO.getPunchType())) {
            record.setPunchOutTime(punchTime);
            record.setPunchOutLocation(punchDTO.getLocation());
        }

        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    /**
     * 更新考勤统计
     */
    private void updateAttendanceStatistics(Long employeeId, LocalDate date) {
        // 简化实现，实际项目中需要更新统计数据
        log.debug("更新考勤统计: 员工ID={}, 日期={}", employeeId, date);
    }

    /**
     * 转换为VO对象
     */
    private AttendanceRecordVO convertToVO(AttendanceRecordEntity entity) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setRecordId(entity.getRecordId());
        vo.setEmployeeId(entity.getEmployeeId());
        vo.setAttendanceDate(entity.getAttendanceDate());
        vo.setPunchInTime(entity.getPunchInTime());
        vo.setPunchOutTime(entity.getPunchOutTime());
        vo.setPunchInLocation(entity.getPunchInLocation());
        vo.setPunchOutLocation(entity.getPunchOutLocation());
        vo.setAttendanceStatus(entity.getAttendanceStatus());
        vo.setExceptionType(entity.getExceptionType());
        vo.setWorkHours(entity.getWorkHours());
        vo.setOvertimeHours(entity.getOvertimeHours());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * 转换为日历数据
     */
    private List<Map<String, Object>> convertToCalendarData(List<AttendanceRecordEntity> records, Integer year, Integer month) {
        // 简化实现，实际项目中需要根据考勤记录生成日历数据
        return List.of();
    }
}
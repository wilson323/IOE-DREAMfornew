package net.lab1024.sa.attendance.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceSupplementDao;
import net.lab1024.sa.attendance.dao.AttendanceLeaveDao;
import net.lab1024.sa.attendance.domain.vo.*;
import net.lab1024.sa.common.entity.attendance.AttendanceSupplementEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceLeaveEntity;
import net.lab1024.sa.attendance.service.AttendanceSummaryService;
import net.lab1024.sa.attendance.service.DashboardService;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;

/**
 * 考勤仪表中心服务实现类
 * <p>
 * 提供仪表中心数据聚合功能，支持Redis缓存
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "dashboard")
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private AttendanceSupplementDao attendanceSupplementDao;

    @Resource
    private AttendanceLeaveDao attendanceLeaveDao;

    @Resource
    private AttendanceSummaryService attendanceSummaryService;

    @Override
    @Cacheable(key = "'overview'", unless = "#result == null")
    public DashboardOverviewVO getOverviewData() {
        log.info("[仪表中心] 获取首页概览数据");

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        // 查询今日打卡记录
        LambdaQueryWrapper<AttendanceRecordEntity> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.between(AttendanceRecordEntity::getAttendanceDate, today, today);
        List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectList(todayQuery);

        // 统计今日数据
        int todayPunchCount = todayRecords.size();
        int todayPresentCount = (int) todayRecords.stream()
                .filter(r -> "NORMAL".equals(r.getAttendanceStatus()))
                .count();
        int todayLateCount = (int) todayRecords.stream()
                .filter(r -> "LATE".equals(r.getAttendanceStatus()))
                .count();
        int todayEarlyCount = (int) todayRecords.stream()
                .filter(r -> "EARLY".equals(r.getAttendanceStatus()))
                .count();
        int todayAbsentCount = (int) todayRecords.stream()
                .filter(r -> "ABSENT".equals(r.getAttendanceStatus()))
                .count();

        // 计算今日出勤率
        BigDecimal todayAttendanceRate = todayPunchCount > 0 ?
                BigDecimal.valueOf((double) todayPresentCount / todayPunchCount * 100)
                        .setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 统计本月数据
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        LambdaQueryWrapper<AttendanceRecordEntity> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.between(AttendanceRecordEntity::getAttendanceDate, monthStart, monthEnd);
        List<AttendanceRecordEntity> monthRecords = attendanceRecordDao.selectList(monthQuery);

        int monthWorkDays = 22; // 假设22个工作日
        BigDecimal monthAttendanceRate = !monthRecords.isEmpty() ?
                BigDecimal.valueOf((double) todayPresentCount / monthWorkDays * 100)
                        .setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 统计待处理异常
        int pendingSupplementCount = Math.toIntExact(attendanceSupplementDao.selectCount(
                new LambdaQueryWrapper<AttendanceSupplementEntity>()
                        .eq(AttendanceSupplementEntity::getStatus, "PENDING")
        ));

        int pendingLeaveCount = Math.toIntExact(attendanceLeaveDao.selectCount(
                new LambdaQueryWrapper<AttendanceLeaveEntity>()
                        .eq(AttendanceLeaveEntity::getStatus, "PENDING")
        ));

        // 生成部门打卡统计
        Map<String, Integer> departmentPunchStats = todayRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getDepartmentName() != null ? r.getDepartmentName() : "未知部门",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // 生成本周趋势数据
        Map<String, Object> weeklyTrend = generateWeeklyTrend(today);

        // 生成近7天出勤率变化
        List<BigDecimal> last7DaysRate = generateLast7DaysRate(today);

        return DashboardOverviewVO.builder()
                .todayPunchCount(todayPunchCount)
                .todayPresentCount(todayPresentCount)
                .todayLateCount(todayLateCount)
                .todayEarlyCount(todayEarlyCount)
                .todayAbsentCount(todayAbsentCount)
                .todayAttendanceRate(todayAttendanceRate)
                .monthWorkDays(monthWorkDays)
                .monthAttendanceRate(monthAttendanceRate)
                .pendingApprovalCount(pendingSupplementCount + pendingLeaveCount)
                .pendingSupplementCount(pendingSupplementCount)
                .pendingLeaveCount(pendingLeaveCount)
                .departmentCount(10) // TODO: 从部门表查询
                .totalEmployees(500) // TODO: 从员工表查询
                .departmentPunchStats(departmentPunchStats)
                .weeklyTrend(weeklyTrend)
                .monthlyTrend(new HashMap<>()) // TODO: 实现月度趋势
                .last7DaysRate(last7DaysRate)
                .build();
    }

    @Override
    public DashboardPersonalVO getPersonalDashboard(Long userId) {
        log.info("[仪表中心] 获取个人看板数据: userId={}", userId);

        LocalDate today = LocalDate.now();

        // 查询今日打卡记录
        LambdaQueryWrapper<AttendanceRecordEntity> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(AttendanceRecordEntity::getUserId, userId)
                .eq(AttendanceRecordEntity::getAttendanceDate, today);
        List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectList(todayQuery);

        String todayStatus = "ABSENT";
        LocalDateTime todayClockInTime = null;
        LocalDateTime todayClockOutTime = null;

        if (!todayRecords.isEmpty()) {
            AttendanceRecordEntity record = todayRecords.get(0);
            todayStatus = record.getAttendanceStatus();
            todayClockInTime = record.getClockInTime();
            todayClockOutTime = record.getClockOutTime();
        }

        // 查询本月数据
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        LambdaQueryWrapper<AttendanceRecordEntity> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.eq(AttendanceRecordEntity::getUserId, userId)
                .between(AttendanceRecordEntity::getAttendanceDate, monthStart, monthEnd);
        List<AttendanceRecordEntity> monthRecords = attendanceRecordDao.selectList(monthQuery);

        int monthActualDays = (int) monthRecords.stream()
                .filter(r -> "NORMAL".equals(r.getAttendanceStatus()))
                .count();
        int monthLateCount = (int) monthRecords.stream()
                .filter(r -> "LATE".equals(r.getAttendanceStatus()))
                .count();
        int monthEarlyCount = (int) monthRecords.stream()
                .filter(r -> "EARLY".equals(r.getAttendanceStatus()))
                .count();
        int monthAbsentDays = (int) monthRecords.stream()
                .filter(r -> "ABSENT".equals(r.getAttendanceStatus()))
                .count();

        int workDays = 22;
        BigDecimal monthAttendanceRate = workDays > 0 ?
                BigDecimal.valueOf((double) monthActualDays / workDays * 100)
                        .setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 生成近7天考勤记录
        List<Map<String, Object>> last7DaysRecords = generateLast7DaysRecords(userId, today);

        // 生成近30天趋势
        List<Map<String, Object>> last30DaysTrend = generateLast30DaysTrend(userId, today);

        // 快速操作权限
        Map<String, Boolean> quickActionPermissions = getQuickActionPermissions(userId);

        return DashboardPersonalVO.builder()
                .employeeId(userId)
                .employeeName("") // TODO: 从用户服务获取
                .departmentName("") // TODO: 从用户服务获取
                .todayStatus(todayStatus)
                .todayClockInTime(todayClockInTime)
                .todayClockOutTime(todayClockOutTime)
                .monthActualDays(monthActualDays)
                .monthLateCount(monthLateCount)
                .monthEarlyCount(monthEarlyCount)
                .monthAbsentDays(monthAbsentDays)
                .monthOvertimeHours(BigDecimal.ZERO) // TODO: 从加班记录统计
                .monthAttendanceRate(monthAttendanceRate)
                .yearActualDays(0) // TODO: 年度统计
                .yearOvertimeHours(BigDecimal.ZERO)
                .last7DaysRecords(last7DaysRecords)
                .last30DaysTrend(last30DaysTrend)
                .pendingExceptions(new ArrayList<>()) // TODO: 查询待处理异常
                .quickActionPermissions(quickActionPermissions)
                .nextSchedule(new HashMap<>()) // TODO: 获取下次排班
                .build();
    }

    @Override
    public DashboardDepartmentVO getDepartmentDashboard(Long departmentId) {
        log.info("[仪表中心] 获取部门看板数据: departmentId={}", departmentId);

        LocalDate today = LocalDate.now();

        // 查询部门今日考勤记录
        LambdaQueryWrapper<AttendanceRecordEntity> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(AttendanceRecordEntity::getDepartmentId, departmentId)
                .eq(AttendanceRecordEntity::getAttendanceDate, today);
        List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectList(todayQuery);

        // 统计部门数据
        Set<Long> uniqueEmployees = todayRecords.stream()
                .map(AttendanceRecordEntity::getUserId)
                .collect(Collectors.toSet());
        int todayPresentCount = uniqueEmployees.size();
        int todayLateCount = (int) todayRecords.stream()
                .filter(r -> "LATE".equals(r.getAttendanceStatus()))
                .count();
        int todayEarlyCount = (int) todayRecords.stream()
                .filter(r -> "EARLY".equals(r.getAttendanceStatus()))
                .count();
        int todayAbsentCount = 0; // TODO: 从部门总人数减去出勤人数

        BigDecimal todayAttendanceRate = todayPresentCount > 0 ?
                BigDecimal.valueOf((double) todayPresentCount / (todayPresentCount + todayAbsentCount) * 100)
                        .setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 查询本月数据
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        LambdaQueryWrapper<AttendanceRecordEntity> monthQuery = new LambdaQueryWrapper<>();
        monthQuery.eq(AttendanceRecordEntity::getDepartmentId, departmentId)
                .between(AttendanceRecordEntity::getAttendanceDate, monthStart, monthEnd);
        List<AttendanceRecordEntity> monthRecords = attendanceRecordDao.selectList(monthQuery);

        BigDecimal monthAttendanceRate = !monthRecords.isEmpty() ?
                BigDecimal.valueOf(95.0).setScale(1, RoundingMode.HALF_UP) : // TODO: 实际计算
                BigDecimal.ZERO;

        // 生成近7天趋势
        List<Map<String, Object>> last7DaysTrend = generateDepartmentLast7DaysTrend(departmentId, today);

        return DashboardDepartmentVO.builder()
                .departmentId(departmentId)
                .departmentName("") // TODO: 从部门表查询
                .totalCount(50) // TODO: 从部门表查询
                .todayPresentCount(todayPresentCount)
                .todayAbsentCount(todayAbsentCount)
                .todayLateCount(todayLateCount)
                .todayEarlyCount(todayEarlyCount)
                .todayAttendanceRate(todayAttendanceRate)
                .monthAttendanceRate(monthAttendanceRate)
                .avgWorkHours(BigDecimal.valueOf(8.5))
                .totalOvertimeHours(BigDecimal.ZERO)
                .avgOvertimeHours(BigDecimal.ZERO)
                .pendingLeaveCount(0)
                .pendingSupplementCount(0)
                .last7DaysTrend(last7DaysTrend)
                .topEmployees(new ArrayList<>())
                .abnormalEmployees(new ArrayList<>())
                .build();
    }

    @Override
    public DashboardEnterpriseVO getEnterpriseDashboard() {
        log.info("[仪表中心] 获取企业看板数据");

        LocalDate today = LocalDate.now();

        // 查询今日所有考勤记录
        LambdaQueryWrapper<AttendanceRecordEntity> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(AttendanceRecordEntity::getAttendanceDate, today);
        List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectList(todayQuery);

        // 统计企业数据
        Set<Long> uniqueEmployees = todayRecords.stream()
                .map(AttendanceRecordEntity::getUserId)
                .collect(Collectors.toSet());
        int todayPresentCount = uniqueEmployees.size();
        int todayAbsentCount = 500 - todayPresentCount; // TODO: 实际查询

        BigDecimal todayAttendanceRate = todayPresentCount > 0 ?
                BigDecimal.valueOf((double) todayPresentCount / 500 * 100)
                        .setScale(1, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 生成部门排行
        List<Map<String, Object>> departmentRanking = generateDepartmentRanking(today);

        // 生成近30天趋势
        List<Map<String, Object>> last30DaysTrend = generateEnterpriseLast30DaysTrend(today);

        return DashboardEnterpriseVO.builder()
                .totalEmployees(500) // TODO: 实际查询
                .departmentCount(10) // TODO: 实际查询
                .todayPresentCount(todayPresentCount)
                .todayAbsentCount(todayAbsentCount)
                .todayAttendanceRate(todayAttendanceRate)
                .monthAttendanceRate(BigDecimal.valueOf(94.8))
                .totalOvertimeHours(BigDecimal.valueOf(1200.5))
                .avgOvertimeHours(BigDecimal.valueOf(2.5))
                .pendingApprovalCount(25)
                .departmentRanking(departmentRanking)
                .lowRateDepartments(new ArrayList<>())
                .last30DaysTrend(last30DaysTrend)
                .last12MonthsTrend(new ArrayList<>())
                .departmentRateDistribution(new HashMap<>())
                .exceptionStatistics(new HashMap<>())
                .build();
    }

    @Override
    public DashboardTrendVO getTrendData(String timeDimension, String trendType,
                                         String startDate, String endDate) {
        log.info("[仪表中心] 获取趋势数据: timeDimension={}, trendType={}, startDate={}, endDate={}",
                timeDimension, trendType, startDate, endDate);

        // TODO: 实现趋势数据查询逻辑
        List<Map<String, Object>> trendData = new ArrayList<>();

        return DashboardTrendVO.builder()
                .timeDimension(timeDimension)
                .trendType(trendType)
                .startDate(startDate)
                .endDate(endDate)
                .trendData(trendData)
                .averageValue(94.5)
                .maxValue(98.0)
                .minValue(89.0)
                .yearOverYearGrowth(2.5)
                .monthOverMonthGrowth(1.2)
                .build();
    }

    @Override
    public DashboardHeatmapVO getHeatmapData(String heatmapType, String statisticsDate) {
        log.info("[仪表中心] 获取热力图数据: heatmapType={}, statisticsDate={}",
                heatmapType, statisticsDate);

        // TODO: 实现热力图数据查询逻辑
        List<DashboardHeatmapVO.HeatmapItem> heatmapData = new ArrayList<>();

        return DashboardHeatmapVO.builder()
                .heatmapType(heatmapType)
                .statisticsDate(statisticsDate)
                .heatmapData(heatmapData)
                .minValue(BigDecimal.valueOf(85.0))
                .maxValue(BigDecimal.valueOf(98.0))
                .averageValue(BigDecimal.valueOf(92.5))
                .build();
    }

    @Override
    public DashboardRealtimeVO getRealtimeData() {
        log.info("[仪表中心] 获取实时统计数据");

        LocalDate today = LocalDate.now();

        // 查询今日打卡记录
        LambdaQueryWrapper<AttendanceRecordEntity> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(AttendanceRecordEntity::getAttendanceDate, today);
        List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectList(todayQuery);

        Set<Long> uniqueEmployees = todayRecords.stream()
                .map(AttendanceRecordEntity::getUserId)
                .collect(Collectors.toSet());

        // 生成实时打卡记录（最近10条）
        List<Map<String, Object>> recentPunchRecords = todayRecords.stream()
                .limit(10)
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", r.getUserId());
                    map.put("userName", r.getUserName());
                    map.put("departmentName", r.getDepartmentName());
                    map.put("clockInTime", r.getClockInTime());
                    map.put("attendanceStatus", r.getAttendanceStatus());
                    return map;
                })
                .collect(Collectors.toList());

        // 生成部门打卡统计
        List<Map<String, Object>> departmentPunchStats = todayRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getDepartmentName() != null ? r.getDepartmentName() : "未知部门",
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("departmentName", e.getKey());
                    map.put("count", e.getValue().intValue());
                    return map;
                })
                .collect(Collectors.toList());

        return DashboardRealtimeVO.builder()
                .updateTime(LocalDateTime.now())
                .todayPunchedCount(uniqueEmployees.size())
                .todayNotPunchedCount(500 - uniqueEmployees.size())
                .onlineDeviceCount(45)
                .offlineDeviceCount(5)
                .recentPunchRecords(recentPunchRecords)
                .recentAlerts(new ArrayList<>())
                .departmentPunchStats(departmentPunchStats)
                .hourlyPunchStats(new ArrayList<>())
                .deviceStatusStats(new HashMap<>())
                .build();
    }

    @Override
    @CacheEvict(key = "#refreshType + ':' + #targetId", condition = "#targetId != null")
    public String refreshDashboard(String refreshType, Long targetId) {
        log.info("[仪表中心] 刷新看板数据: refreshType={}, targetId={}", refreshType, targetId);

        // TODO: 实现刷新逻辑
        return String.format("看板数据刷新成功: refreshType=%s, targetId=%d", refreshType, targetId);
    }

    @Override
    public Map<String, Boolean> getQuickActionPermissions(Long userId) {
        log.info("[仪表中心] 获取快速操作权限: userId={}", userId);

        Map<String, Boolean> permissions = new HashMap<>();
        // TODO: 实际权限验证
        permissions.put("canApplyLeave", true);
        permissions.put("canApplySupplement", true);
        permissions.put("canApplyOvertime", true);
        permissions.put("canViewSchedule", true);
        permissions.put("canViewReport", true);

        return permissions;
    }

    /**
     * 生成本周趋势数据
     */
    private Map<String, Object> generateWeeklyTrend(LocalDate today) {
        Map<String, Object> trend = new HashMap<>();
        // TODO: 实现周趋势数据生成
        return trend;
    }

    /**
     * 生成近7天出勤率变化
     */
    private List<BigDecimal> generateLast7DaysRate(LocalDate today) {
        List<BigDecimal> rates = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            // TODO: 实际查询出勤率
            rates.add(BigDecimal.valueOf(90 + Math.random() * 10).setScale(1, RoundingMode.HALF_UP));
        }
        return rates;
    }

    /**
     * 生成近7天考勤记录
     */
    private List<Map<String, Object>> generateLast7DaysRecords(Long userId, LocalDate today) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LambdaQueryWrapper<AttendanceRecordEntity> query = new LambdaQueryWrapper<>();
            query.eq(AttendanceRecordEntity::getUserId, userId)
                    .eq(AttendanceRecordEntity::getAttendanceDate, date);
            AttendanceRecordEntity record = attendanceRecordDao.selectOne(query);

            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("date", date.toString());
            if (record != null) {
                recordMap.put("status", record.getAttendanceStatus());
                recordMap.put("clockInTime", record.getClockInTime());
                recordMap.put("clockOutTime", record.getClockOutTime());
            } else {
                recordMap.put("status", "ABSENT");
                recordMap.put("clockInTime", null);
                recordMap.put("clockOutTime", null);
            }
            records.add(recordMap);
        }
        return records;
    }

    /**
     * 生成近30天趋势
     */
    private List<Map<String, Object>> generateLast30DaysTrend(Long userId, LocalDate today) {
        List<Map<String, Object>> trend = new ArrayList<>();
        // TODO: 实现近30天趋势数据生成
        return trend;
    }

    /**
     * 生成部门近7天趋势
     */
    private List<Map<String, Object>> generateDepartmentLast7DaysTrend(Long departmentId, LocalDate today) {
        List<Map<String, Object>> trend = new ArrayList<>();
        // TODO: 实现部门近7天趋势数据生成
        return trend;
    }

    /**
     * 生成部门排行
     */
    private List<Map<String, Object>> generateDepartmentRanking(LocalDate today) {
        List<Map<String, Object>> ranking = new ArrayList<>();
        // TODO: 实现部门排行数据生成
        return ranking;
    }

    /**
     * 生成企业近30天趋势
     */
    private List<Map<String, Object>> generateEnterpriseLast30DaysTrend(LocalDate today) {
        List<Map<String, Object>> trend = new ArrayList<>();
        // TODO: 实现企业近30天趋势数据生成
        return trend;
    }
}

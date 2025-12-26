package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.config.EnhancedTestConfiguration;
import org.springframework.context.annotation.Import;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.common.entity.attendance.AttendanceAnomalyEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceRuleConfigEntity;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import net.lab1024.sa.attendance.service.impl.AttendanceAnomalyDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 考勤异常检测服务单元测试
 *
 * 测试范围：
 * 1. 迟到检测算法
 * 2. 早退检测算法
 * 3. 缺卡检测算法
 * 4. 旷工检测算法
 * 5. 批量检测功能
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(EnhancedTestConfiguration.class)
@DisplayName("考勤异常检测服务测试")
class AttendanceAnomalyDetectionServiceTest {

    @Mock
    private AttendanceAnomalyDao anomalyDao;

    @Mock
    private AttendanceRecordDao recordDao;

    @Mock
    private AttendanceRuleConfigDao ruleConfigDao;

    @Mock
    private WorkShiftDao workShiftDao;

    @InjectMocks
    private AttendanceAnomalyDetectionServiceImpl detectionService;

    private AttendanceRuleConfigEntity defaultRule;
    private WorkShiftEntity defaultShift;

    @BeforeEach
    void setUp() {
        // 创建默认规则配置
        defaultRule = new AttendanceRuleConfigEntity();
        defaultRule.setConfigId(1L);
        defaultRule.setLateCheckEnabled(1);
        defaultRule.setLateMinutes(5);          // 迟到5分钟判定
        defaultRule.setSeriousLateMinutes(30);   // 严重迟到30分钟
        defaultRule.setEarlyCheckEnabled(1);
        defaultRule.setEarlyMinutes(5);          // 早退5分钟判定
        defaultRule.setSeriousEarlyMinutes(30);  // 严重早退30分钟
        defaultRule.setFlexibleStartEnabled(1);
        defaultRule.setFlexibleStartMinutes(15); // 上班弹性15分钟
        defaultRule.setFlexibleEndEnabled(1);
        defaultRule.setFlexibleEndMinutes(15);   // 下班弹性15分钟
        defaultRule.setAbsentCheckEnabled(1);
        defaultRule.setLateToAbsentMinutes(120); // 迟到2小时转旷工

        // 创建默认班次
        defaultShift = new WorkShiftEntity();
        defaultShift.setShiftId(1L);
        defaultShift.setShiftName("正常班");
        defaultShift.setWorkStartTime(LocalTime.of(9, 0));   // 9:00上班
        defaultShift.setWorkEndTime(LocalTime.of(18, 0));    // 18:00下班
        defaultShift.setWorkDuration(480);                   // 8小时

        // Mock返回
        when(ruleConfigDao.selectApplicableRule(any(), any(), any())).thenReturn(defaultRule);
        when(ruleConfigDao.selectGlobalRule()).thenReturn(defaultRule);  // 旷工检测需要
        when(workShiftDao.selectById(any())).thenReturn(defaultShift);
        when(recordDao.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList());  // 默认返回空列表
    }

    // ==================== 迟到检测测试 ====================

    @Test
@DisplayName("测试正常打卡 - 不应产生迟到异常")
    void testNormalCheckIn_NoLateAnomaly() {
        System.out.println("[单元测试] 测试正常打卡场景");

        // Given: 9:00准时打卡
        AttendanceRecordEntity record = createCheckInRecord(
                LocalDateTime.of(2025, 1, 30, 9, 0, 0)  // 9:00:00
        );

        // When: 执行迟到检测
        AttendanceAnomalyEntity anomaly = detectionService.detectLateAnomaly(record);

        // Then: 不应产生异常
        assertNull(anomaly, "准时打卡不应产生迟到异常");
        System.out.println("[单元测试] 测试通过：准时打卡不产生异常");
    }

    @Test
@DisplayName("测试弹性时间内打卡 - 不应产生迟到异常")
    void testFlexibleCheckIn_NoLateAnomaly() {
        System.out.println("[单元测试] 测试弹性时间内打卡场景");

        // Given: 9:10打卡（弹性时间内）
        AttendanceRecordEntity record = createCheckInRecord(
                LocalDateTime.of(2025, 1, 30, 9, 10, 0)  // 9:10:00
        );

        // When: 执行迟到检测
        AttendanceAnomalyEntity anomaly = detectionService.detectLateAnomaly(record);

        // Then: 不应产生异常（在弹性时间内）
        assertNull(anomaly, "弹性时间内打卡不应产生迟到异常");
        System.out.println("[单元测试] 测试通过：弹性时间内打卡不产生异常");
    }

    @Test
@DisplayName("测试迟到22分钟 - 应产生一般迟到异常")
    void testLate22Minutes_GeneralLateAnomaly() {
        System.out.println("[单元测试] 测试迟到22分钟场景");

        // Given: 9:22打卡（迟到22分钟，超过9:20阈值）
        AttendanceRecordEntity record = createCheckInRecord(
                LocalDateTime.of(2025, 1, 30, 9, 22, 0)  // 9:22:00 (超过9:00+5+15=9:20)
        );

        // When: 执行迟到检测
        AttendanceAnomalyEntity anomaly = detectionService.detectLateAnomaly(record);

        // Then: 应产生一般迟到异常
        assertNotNull(anomaly, "迟到22分钟应产生异常");
        assertEquals("LATE", anomaly.getAnomalyType());
        assertEquals("NORMAL", anomaly.getSeverityLevel(), "应为一般严重程度");
        assertEquals(22, anomaly.getAnomalyDuration(), "迟到时长应为22分钟");
        assertEquals("PENDING", anomaly.getAnomalyStatus());

        System.out.println("[单元测试] 测试通过：迟到22分钟产生一般异常，时长={}分钟" + " " + anomaly.getAnomalyDuration());
    }

    @Test
@DisplayName("测试迟到35分钟 - 应产生严重迟到异常")
    void testLate35Minutes_SeriousLateAnomaly() {
        System.out.println("[单元测试] 测试迟到35分钟场景");

        // Given: 9:35打卡（迟到35分钟，超过30分钟严重阈值）
        AttendanceRecordEntity record = createCheckInRecord(
                LocalDateTime.of(2025, 1, 30, 9, 35, 0)  // 9:35:00
        );

        // When: 执行迟到检测
        AttendanceAnomalyEntity anomaly = detectionService.detectLateAnomaly(record);

        // Then: 应产生严重迟到异常
        assertNotNull(anomaly, "迟到35分钟应产生异常");
        assertEquals("LATE", anomaly.getAnomalyType());
        assertEquals("SERIOUS", anomaly.getSeverityLevel(), "应为严重严重程度");
        assertEquals(35, anomaly.getAnomalyDuration(), "迟到时长应为35分钟");

        System.out.println("[单元测试] 测试通过：迟到35分钟产生严重异常，时长={}分钟" + " " + anomaly.getAnomalyDuration());
    }

    // ==================== 早退检测测试 ====================

    @Test
@DisplayName("测试正常下班 - 不应产生早退异常")
    void testNormalCheckOut_NoEarlyAnomaly() {
        System.out.println("[单元测试] 测试正常下班场景");

        // Given: 18:00准时下班
        AttendanceRecordEntity record = createCheckOutRecord(
                LocalDateTime.of(2025, 1, 30, 18, 0, 0)  // 18:00:00
        );

        // When: 执行早退检测
        AttendanceAnomalyEntity anomaly = detectionService.detectEarlyAnomaly(record);

        // Then: 不应产生异常
        assertNull(anomaly, "准时下班不应产生早退异常");
        System.out.println("[单元测试] 测试通过：准时下班不产生异常");
    }

    @Test
@DisplayName("测试早退22分钟 - 应产生一般早退异常")
    void testEarly22Minutes_GeneralEarlyAnomaly() {
        System.out.println("[单元测试] 测试早退22分钟场景");

        // Given: 17:38下班（早退22分钟，早于17:40阈值）
        AttendanceRecordEntity record = createCheckOutRecord(
                LocalDateTime.of(2025, 1, 30, 17, 38, 0)  // 17:38:00 (早于18:00-5-15=17:40)
        );

        // When: 执行早退检测
        AttendanceAnomalyEntity anomaly = detectionService.detectEarlyAnomaly(record);

        // Then: 应产生一般早退异常
        assertNotNull(anomaly, "早退22分钟应产生异常");
        assertEquals("EARLY", anomaly.getAnomalyType());
        assertEquals("NORMAL", anomaly.getSeverityLevel());
        assertEquals(22, anomaly.getAnomalyDuration(), "早退时长应为22分钟");

        System.out.println("[单元测试] 测试通过：早退22分钟产生一般异常，时长={}分钟" + " " + anomaly.getAnomalyDuration());
    }

    // ==================== 缺卡检测测试 ====================

    @Test
@DisplayName("测试上班缺卡 - 应产生缺卡异常")
    void testMissingCheckIn_ShouldCreateAnomaly() {
        System.out.println("[单元测试] 测试上班缺卡场景");

        // Given: 用户ID和日期
        Long userId = 1001L;
        LocalDate date = LocalDate.of(2025, 1, 30);
        Long shiftId = 1L;

        // Mock: 只有下班打卡记录
        List<AttendanceRecordEntity> records = Arrays.asList(
                createCheckOutRecord(LocalDateTime.of(2025, 1, 30, 18, 0, 0))
        );
        when(recordDao.selectList(any(QueryWrapper.class))).thenReturn(records);

        // When: 执行缺卡检测
        List<AttendanceAnomalyEntity> anomalies = detectionService.detectMissingCards(userId, date, shiftId);

        // Then: 应产生上班缺卡异常
        assertFalse(anomalies.isEmpty(), "应产生缺卡异常");
        assertEquals(1, anomalies.size(), "应产生1条缺卡异常");
        assertEquals("MISSING_CARD", anomalies.get(0).getAnomalyType());
        assertEquals("CHECK_IN", anomalies.get(0).getPunchType());

        System.out.println("[单元测试] 测试通过：上班缺卡产生异常，类型={}" + " " + anomalies.get(0).getPunchType());
    }

    @Test
@DisplayName("测试全天无打卡 - 应产生2条缺卡异常")
    void testNoPunchRecords_ShouldCreateTwoAnomalies() {
        System.out.println("[单元测试] 测试全天无打卡场景");

        // Given: 用户ID和日期
        Long userId = 1001L;
        LocalDate date = LocalDate.of(2025, 1, 30);
        Long shiftId = 1L;

        // Mock: 无打卡记录
        when(recordDao.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList());

        // When: 执行缺卡检测
        List<AttendanceAnomalyEntity> anomalies = detectionService.detectMissingCards(userId, date, shiftId);

        // Then: 应产生上下班缺卡异常
        assertEquals(2, anomalies.size(), "应产生2条缺卡异常");
        assertEquals("MISSING_CARD", anomalies.get(0).getAnomalyType());
        assertEquals("CHECK_IN", anomalies.get(0).getPunchType());
        assertEquals("CHECK_OUT", anomalies.get(1).getPunchType());

        System.out.println("[单元测试] 测试通过：全天无打卡产生2条缺卡异常");
    }

    // ==================== 旷工检测测试 ====================

    @Test
@DisplayName("测试全天无打卡 - 应产生旷工异常")
    void testNoPunch_ShouldCreateAbsentAnomaly() {
        System.out.println("[单元测试] 测试全天无打卡旷工场景");

        // Given
        Long userId = 1001L;
        LocalDate date = LocalDate.of(2025, 1, 30);
        Long shiftId = 1L;

        // Mock: 无打卡记录
        when(recordDao.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList());

        // When: 执行旷工检测
        AttendanceAnomalyEntity anomaly = detectionService.detectAbsentAnomaly(userId, date, shiftId);

        // Then: 应产生旷工异常
        assertNotNull(anomaly, "全天无打卡应产生旷工异常");
        assertEquals("ABSENT", anomaly.getAnomalyType());
        assertEquals("CRITICAL", anomaly.getSeverityLevel());
        assertEquals(480, anomaly.getAnomalyDuration(), "旷工时长应为8小时");

        System.out.println("[单元测试] 测试通过：全天无打卡产生旷工异常，时长={}分钟" + " " + anomaly.getAnomalyDuration());
    }

    @Test
@DisplayName("测试迟到2小时 - 应转旷工异常")
    void testLate2Hours_ShouldConvertToAbsent() {
        System.out.println("[单元测试] 测试迟到转旷工场景");

        // Given: 11:00上班打卡（迟到2小时）
        Long userId = 1001L;
        LocalDate date = LocalDate.of(2025, 1, 30);
        Long shiftId = 1L;

        AttendanceRecordEntity record = createCheckInRecord(
                LocalDateTime.of(2025, 1, 30, 11, 0, 0)
        );
        when(recordDao.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(record));

        // When: 执行旷工检测
        AttendanceAnomalyEntity anomaly = detectionService.detectAbsentAnomaly(userId, date, shiftId);

        // Then: 应产生旷工异常
        assertNotNull(anomaly, "迟到2小时应转旷工");
        assertEquals("ABSENT", anomaly.getAnomalyType());
        assertTrue(anomaly.getAnomalyReason().contains("判定为旷工"));

        System.out.println("[单元测试] 测试通过：迟到2小时转旷工，原因={}" + " " + anomaly.getAnomalyReason());
    }

    // ==================== 批量检测测试 ====================

    @Test
@DisplayName("测试批量日期检测 - 应检测所有异常")
    void testBatchDetectionByDate_ShouldDetectAllAnomalies() {
        System.out.println("[单元测试] 测试批量检测场景");

        // Given: 测试日期和多个打卡记录
        LocalDate testDate = LocalDate.of(2025, 1, 30);

        List<AttendanceRecordEntity> records = Arrays.asList(
                createCheckInRecord(LocalDateTime.of(2025, 1, 30, 9, 22, 0)),   // 迟到22分钟（超过9:20）
                createCheckOutRecord(LocalDateTime.of(2025, 1, 30, 17, 38, 0))  // 早退22分钟（早于17:40）
        );
        when(recordDao.selectList(any(QueryWrapper.class))).thenReturn(records);

        // When: 执行批量检测
        List<AttendanceAnomalyEntity> anomalies = detectionService.detectAnomaliesByDate(testDate);

        // Then: 应检测到2条异常
        assertTrue(anomalies.size() >= 2, "应至少检测到2条异常");

        System.out.println("[单元测试] 测试通过：批量检测完成，共检测到{}条异常" + " " + anomalies.size());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建上班打卡记录
     */
    private AttendanceRecordEntity createCheckInRecord(LocalDateTime punchTime) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setRecordId(1L);
        record.setUserId(1001L);
        record.setUserName("张三");
        record.setDepartmentId(1L);
        record.setDepartmentName("研发部");
        record.setShiftId(1L);
        record.setShiftName("正常班");
        record.setAttendanceDate(punchTime.toLocalDate());
        record.setPunchTime(punchTime);
        record.setAttendanceType("CHECK_IN");
        return record;
    }

    /**
     * 创建下班打卡记录
     */
    private AttendanceRecordEntity createCheckOutRecord(LocalDateTime punchTime) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setRecordId(2L);
        record.setUserId(1001L);
        record.setUserName("张三");
        record.setDepartmentId(1L);
        record.setDepartmentName("研发部");
        record.setShiftId(1L);
        record.setShiftName("正常班");
        record.setAttendanceDate(punchTime.toLocalDate());
        record.setPunchTime(punchTime);
        record.setAttendanceType("CHECK_OUT");
        return record;
    }
}

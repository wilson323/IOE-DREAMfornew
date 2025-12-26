package net.lab1024.sa.visitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.SelfCheckOutDao;
import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.common.entity.visitor.SelfCheckOutEntity;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 自助签离管理器单元测试
 *
 * 测试范围：
 * 1. 访问时长计算
 * 2. 超时检测
 * 3. 超时时长计算
 * 4. 签离流程
 * 5. 人工签离
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("自助签离管理器测试")
class SelfCheckOutManagerTest {

    @Mock
    private SelfCheckOutDao selfCheckOutDao;

    @Mock
    private SelfServiceRegistrationDao selfServiceRegistrationDao;

    @InjectMocks
    private SelfCheckOutManager selfCheckOutManager;

    private SelfServiceRegistrationEntity testRegistration;
    private LocalDateTime checkInTime;
    private LocalDateTime expectedLeaveTime;

    @BeforeEach
    void setUp() {
        checkInTime = LocalDateTime.of(2025, 12, 26, 9, 0, 0);
        expectedLeaveTime = LocalDateTime.of(2025, 12, 26, 18, 0, 0);

        testRegistration = SelfServiceRegistrationEntity.builder()
                .registrationId(1L)
                .registrationCode("SSRG20251226000001")
                .visitorCode("VC2025122609100001")
                .visitorName("张三")
                .idCard("110101199001011234")
                .phone("13800138000")
                .intervieweeId(2001L)
                .intervieweeName("李四")
                .checkInTime(checkInTime)
                .expectedLeaveTime(expectedLeaveTime)
                .registrationStatus(3) // 已签到
                .build();
    }

    @Test
    @DisplayName("测试计算访问时长 - 正常情况")
    void testCalculateVisitDuration_Normal() {
        log.info("[单元测试] 测试计算访问时长 - 正常情况");

        // Given - 9:00签到，18:00签离
        LocalDateTime checkOutTime = LocalDateTime.of(2025, 12, 26, 18, 0, 0);

        // When
        Integer duration = selfCheckOutManager.calculateVisitDuration(checkInTime, checkOutTime);

        // Then
        assertNotNull(duration, "访问时长不应为null");
        assertEquals(540, duration, "访问时长应为540分钟（9小时）");

        log.info("[单元测试] 测试通过: 访问时长计算正确，duration={}分钟", duration);
    }

    @Test
    @DisplayName("测试计算访问时长 - 跨天访问")
    void testCalculateVisitDuration_Overnight() {
        log.info("[单元测试] 测试计算访问时长 - 跨天访问");

        // Given - 前一天22:00签到，第二天8:00签离
        LocalDateTime overnightCheckIn = LocalDateTime.of(2025, 12, 25, 22, 0, 0);
        LocalDateTime overnightCheckOut = LocalDateTime.of(2025, 12, 26, 8, 0, 0);

        // When
        Integer duration = selfCheckOutManager.calculateVisitDuration(overnightCheckIn, overnightCheckOut);

        // Then
        assertNotNull(duration, "访问时长不应为null");
        assertEquals(600, duration, "访问时长应为600分钟（10小时）");

        log.info("[单元测试] 测试通过: 跨天访问时长计算正确，duration={}分钟", duration);
    }

    @Test
    @DisplayName("测试计算访问时长 - 空值处理")
    void testCalculateVisitDuration_NullValues() {
        log.info("[单元测试] 测试计算访问时长 - 空值处理");

        // Test cases
        LocalDateTime[][] testCases = {
                {null, LocalDateTime.now()},           // 签到时间为null
                {LocalDateTime.now(), null},           // 签离时间为null
                {null, null}                           // 都为null
        };

        for (LocalDateTime[] testCase : testCases) {
            LocalDateTime inTime = testCase[0];
            LocalDateTime outTime = testCase[1];

            Integer duration = selfCheckOutManager.calculateVisitDuration(inTime, outTime);

            assertEquals(0, duration, "空值情况应返回0");

            log.info("[单元测试] 空值处理正确: checkInTime={}, checkOutTime={}, duration=0",
                    inTime, outTime);
        }

        log.info("[单元测试] 测试通过: 空值处理正确");
    }

    @Test
    @DisplayName("测试判断是否超时 - 未超时")
    void testIsOvertime_NotOvertime() {
        log.info("[单元测试] 测试判断是否超时 - 未超时");

        // Given - 18:00签离，未超过预计离开时间18:00
        LocalDateTime actualLeaveTime = LocalDateTime.of(2025, 12, 26, 17, 59, 59);

        // When
        boolean isOvertime = selfCheckOutManager.isOvertime(expectedLeaveTime, actualLeaveTime);

        // Then
        assertFalse(isOvertime, "未超时应返回false");

        log.info("[单元测试] 测试通过: 未超时判断正确");
    }

    @Test
    @DisplayName("测试判断是否超时 - 已超时")
    void testIsOvertime_Overtime() {
        log.info("[单元测试] 测试判断是否超时 - 已超时");

        // Given - 18:01签离，超过预计离开时间18:00
        LocalDateTime actualLeaveTime = LocalDateTime.of(2025, 12, 26, 18, 1, 0);

        // When
        boolean isOvertime = selfCheckOutManager.isOvertime(expectedLeaveTime, actualLeaveTime);

        // Then
        assertTrue(isOvertime, "超时应返回true");

        log.info("[单元测试] 测试通过: 超时判断正确");
    }

    @Test
    @DisplayName("测试判断是否超时 - 空值处理")
    void testIsOvertime_NullValues() {
        log.info("[单元测试] 测试判断是否超时 - 空值处理");

        // Test cases
        LocalDateTime[][] testCases = {
                {null, LocalDateTime.now()},           // 预计离开时间为null
                {LocalDateTime.now(), null},           // 实际离开时间为null
                {null, null}                           // 都为null
        };

        for (LocalDateTime[] testCase : testCases) {
            LocalDateTime expectTime = testCase[0];
            LocalDateTime actualTime = testCase[1];

            boolean isOvertime = selfCheckOutManager.isOvertime(expectTime, actualTime);

            assertFalse(isOvertime, "空值情况应返回false（未超时）");

            log.info("[单元测试] 空值处理正确: expectedTime={}, actualTime={}, isOvertime=false",
                    expectTime, actualTime);
        }

        log.info("[单元测试] 测试通过: 超时空值处理正确");
    }

    @Test
    @DisplayName("测试计算超时时长")
    void testCalculateOvertimeDuration_Success() {
        log.info("[单元测试] 测试计算超时时长");

        // Given - 超时30分钟
        LocalDateTime actualLeaveTime = LocalDateTime.of(2025, 12, 26, 18, 30, 0);

        // When
        Integer overtimeDuration = selfCheckOutManager.calculateOvertimeDuration(
                expectedLeaveTime, actualLeaveTime
        );

        // Then
        assertNotNull(overtimeDuration, "超时时长不应为null");
        assertEquals(30, overtimeDuration, "超时时长应为30分钟");

        log.info("[单元测试] 测试通过: 超时时长计算正确，overtimeDuration={}分钟", overtimeDuration);
    }

    @Test
    @DisplayName("测试处理签离流程 - 成功")
    void testProcessCheckOut_Success() {
        log.info("[单元测试] 测试处理签离流程 - 成功");

        // Given
        String visitorCode = "VC2025122609100001";
        String terminalId = "TERM001";
        String terminalLocation = "大厅签离机";
        Integer cardReturnStatus = 1; // 已归还
        String visitorCard = "VC001";

        when(selfServiceRegistrationDao.selectByVisitorCode(visitorCode)).thenReturn(testRegistration);
        when(selfCheckOutDao.insert(any(SelfCheckOutEntity.class))).thenReturn(1);
        when(selfServiceRegistrationDao.updateById(any(SelfServiceRegistrationEntity.class))).thenReturn(1);

        // When
        SelfCheckOutEntity checkOut = selfCheckOutManager.processCheckOut(
                visitorCode, terminalId, terminalLocation, cardReturnStatus, visitorCard
        );

        // Then
        assertNotNull(checkOut, "签离记录不应为null");
        assertEquals(testRegistration.getRegistrationId(), checkOut.getRegistrationId(), "登记ID应匹配");
        assertEquals(visitorCode, checkOut.getVisitorCode(), "访客码应匹配");
        assertEquals(terminalId, checkOut.getTerminalId(), "终端ID应匹配");
        assertNotNull(checkOut.getCheckOutTime(), "签离时间应设置");
        assertNotNull(checkOut.getVisitDuration(), "访问时长应计算");
        assertEquals(cardReturnStatus, checkOut.getCardReturnStatus(), "卡归还状态应匹配");
        assertEquals(1, checkOut.getCheckOutMethod(), "签离方式应为1（自助签离）");
        assertEquals(1, checkOut.getCheckOutStatus(), "签离状态应为1（已完成）");

        log.info("[单元测试] 测试通过: 签离流程处理成功，checkOutId={}", checkOut.getCheckOutId());

        verify(selfServiceRegistrationDao, times(1)).selectByVisitorCode(visitorCode);
        verify(selfCheckOutDao, times(1)).insert(any(SelfCheckOutEntity.class));
        verify(selfServiceRegistrationDao, times(1)).updateById(any(SelfServiceRegistrationEntity.class));
    }

    @Test
    @DisplayName("测试处理签离流程 - 访客未签到")
    void testProcessCheckOut_NotCheckedIn() {
        log.info("[单元测试] 测试处理签离流程 - 访客未签到");

        // Given
        String visitorCode = "VC2025122609100001";
        testRegistration.setRegistrationStatus(1); // 审批通过但未签到

        when(selfServiceRegistrationDao.selectByVisitorCode(visitorCode)).thenReturn(testRegistration);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            selfCheckOutManager.processCheckOut(
                    visitorCode, "TERM001", "大厅", 1, "VC001"
            );
        });

        assertTrue(exception.getMessage().contains("访客未签到"), "异常消息应包含'访客未签到'");

        log.info("[单元测试] 测试通过: 访客未签到异常处理正确，message={}", exception.getMessage());
    }

    @Test
    @DisplayName("测试人工签离 - 成功")
    void testManualCheckOut_Success() {
        log.info("[单元测试] 测试人工签离 - 成功");

        // Given
        String visitorCode = "VC2025122609100001";
        Long operatorId = 3001L;
        String operatorName = "管理员";
        String reason = "强制签离";

        when(selfServiceRegistrationDao.selectByVisitorCode(visitorCode)).thenReturn(testRegistration);
        when(selfCheckOutDao.insert(any(SelfCheckOutEntity.class))).thenReturn(1);
        when(selfServiceRegistrationDao.updateById(any(SelfServiceRegistrationEntity.class))).thenReturn(1);

        // When
        SelfCheckOutEntity checkOut = selfCheckOutManager.manualCheckOut(
                visitorCode, operatorId, operatorName, reason
        );

        // Then
        assertNotNull(checkOut, "签离记录不应为null");
        assertEquals(visitorCode, checkOut.getVisitorCode(), "访客码应匹配");
        assertEquals(operatorId, checkOut.getOperatorId(), "操作人ID应匹配");
        assertEquals(operatorName, checkOut.getOperatorName(), "操作人姓名应匹配");
        assertEquals(reason, checkOut.getRemark(), "签离原因应匹配");
        assertEquals(2, checkOut.getCheckOutMethod(), "签离方式应为2（人工签离）");
        assertEquals(0, checkOut.getCardReturnStatus(), "人工签离默认卡归还状态为0（未归还）");

        log.info("[单元测试] 测试通过: 人工签离成功，checkOutId={}", checkOut.getCheckOutId());

        verify(selfServiceRegistrationDao, times(1)).selectByVisitorCode(visitorCode);
        verify(selfCheckOutDao, times(1)).insert(any(SelfCheckOutEntity.class));
        verify(selfServiceRegistrationDao, times(1)).updateById(any(SelfServiceRegistrationEntity.class));
    }

    @Test
    @DisplayName("测试更新满意度评价")
    void testUpdateSatisfaction_Success() {
        log.info("[单元测试] 测试更新满意度评价");

        // Given
        Long checkOutId = 1L;
        Integer satisfactionScore = 5;
        String visitorFeedback = "服务很好，满意！";

        SelfCheckOutEntity checkOut = SelfCheckOutEntity.builder()
                .checkOutId(checkOutId)
                .registrationId(1L)
                .build();

        when(selfCheckOutDao.selectById(checkOutId)).thenReturn(checkOut);
        when(selfCheckOutDao.updateById(any(SelfCheckOutEntity.class))).thenReturn(1);

        // When
        selfCheckOutManager.updateSatisfaction(checkOutId, satisfactionScore, visitorFeedback);

        // Then
        assertEquals(satisfactionScore, checkOut.getSatisfactionScore(), "满意度评分应匹配");
        assertEquals(visitorFeedback, checkOut.getVisitorFeedback(), "访客反馈应匹配");

        log.info("[单元测试] 测试通过: 满意度评价更新成功，score={}", satisfactionScore);

        verify(selfCheckOutDao, times(1)).selectById(checkOutId);
        verify(selfCheckOutDao, times(1)).updateById(any(SelfCheckOutEntity.class));
    }

    @Test
    @DisplayName("测试更新访客卡归还状态")
    void testUpdateCardReturnStatus_Success() {
        log.info("[单元测试] 测试更新访客卡归还状态");

        // Given
        Long checkOutId = 1L;
        Integer cardReturnStatus = 1; // 已归还
        String visitorCard = "VC001";

        SelfCheckOutEntity checkOut = SelfCheckOutEntity.builder()
                .checkOutId(checkOutId)
                .registrationId(1L)
                .build();

        when(selfCheckOutDao.selectById(checkOutId)).thenReturn(checkOut);
        when(selfCheckOutDao.updateById(any(SelfCheckOutEntity.class))).thenReturn(1);

        // When
        selfCheckOutManager.updateCardReturnStatus(checkOutId, cardReturnStatus, visitorCard);

        // Then
        assertEquals(cardReturnStatus, checkOut.getCardReturnStatus(), "卡归还状态应匹配");
        assertEquals(visitorCard, checkOut.getVisitorCard(), "访客卡号应匹配");

        log.info("[单元测试] 测试通过: 访客卡归还状态更新成功，status={}", cardReturnStatus);

        verify(selfCheckOutDao, times(1)).selectById(checkOutId);
        verify(selfCheckOutDao, times(1)).updateById(any(SelfCheckOutEntity.class));
    }
}

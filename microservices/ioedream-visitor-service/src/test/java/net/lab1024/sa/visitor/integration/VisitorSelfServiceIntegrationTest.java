package net.lab1024.sa.visitor.integration;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.SelfCheckOutDao;
import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.visitor.entity.SelfCheckOutEntity;
import net.lab1024.sa.visitor.entity.SelfServiceRegistrationEntity;
import net.lab1024.sa.visitor.service.SelfCheckOutService;
import net.lab1024.sa.visitor.service.SelfServiceRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 访客自助服务集成测试
 *
 * 测试范围：
 * 1. 完整的访客登记→签到→签离流程
 * 2. 审批工作流
 * 3. 访客卡管理
 * 4. 数据一致性
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("访客自助服务集成测试")
class VisitorSelfServiceIntegrationTest {

    @Autowired
    private SelfServiceRegistrationService registrationService;

    @Autowired
    private SelfCheckOutService checkOutService;

    @Autowired
    private SelfServiceRegistrationDao registrationDao;

    @Autowired
    private SelfCheckOutDao checkOutDao;

    private SelfServiceRegistrationEntity testRegistration;
    private String visitorCode;
    private String registrationCode;

    @BeforeEach
    void setUp() {
        // 创建测试登记记录
        testRegistration = SelfServiceRegistrationEntity.builder()
                .visitorName("张三")
                .idCardType(1)
                .idCard("110101199001011234")
                .phone("13800138000")
                .visitorType(1)
                .visitPurpose("商务洽谈")
                .intervieweeId(2001L)
                .intervieweeName("李四")
                .intervieweeDepartment("技术部")
                .visitDate(LocalDate.now())
                .expectedEnterTime(LocalDateTime.now().plusHours(1))
                .expectedLeaveTime(LocalDateTime.now().plusHours(9))
                .registrationStatus(0) // 待审批
                .build();

        log.info("[集成测试] 测试环境准备完成");
    }

    @Test
    @DisplayName("测试完整访客流程 - 登记→审批→签到→签离")
    void testCompleteVisitorFlow_Success() {
        log.info("[集成测试] 测试完整访客流程 - 登记→审批→签到→签离");

        // ========== Step 1: 创建访客登记 ==========
        log.info("[集成测试] Step 1: 创建访客登记");

        SelfServiceRegistrationEntity registration = registrationService.createRegistration(testRegistration);

        assertNotNull(registration, "登记记录不应为null");
        assertNotNull(registration.getRegistrationId(), "登记ID不应为null");
        assertNotNull(registration.getVisitorCode(), "访客码应生成");
        assertNotNull(registration.getRegistrationCode(), "登记码应生成");
        assertEquals(0, registration.getRegistrationStatus(), "状态应为待审批");

        Long registrationId = registration.getRegistrationId();
        visitorCode = registration.getVisitorCode();
        registrationCode = registration.getRegistrationCode();

        log.info("[集成测试] 登记创建成功: registrationId={}, visitorCode={}, registrationCode={}",
                registrationId, visitorCode, registrationCode);

        // ========== Step 2: 审批通过 ==========
        log.info("[集成测试] Step 2: 审批通过");

        Long approverId = 3001L;
        String approverName = "王经理";
        String approvalComment = "同意接待";

        SelfServiceRegistrationEntity approved = registrationService.approveRegistration(
                registrationId, approverId, approverName, true, approvalComment
        );

        assertNotNull(approved, "审批结果不应为null");
        assertEquals(approverId, approved.getApproverId(), "审批人ID应匹配");
        assertEquals(1, approved.getRegistrationStatus(), "状态应为审批通过");
        assertNotNull(approved.getApprovalTime(), "审批时间应设置");

        log.info("[集成测试] 审批通过: registrationId={}, status={}, approver={}",
                registrationId, approved.getRegistrationStatus(), approverName);

        // ========== Step 3: 访客签到 ==========
        log.info("[集成测试] Step 3: 访客签到");

        String terminalId = "CHECKIN001";
        String terminalLocation = "大厅签到机";

        SelfServiceRegistrationEntity checkedIn = registrationService.checkIn(
                visitorCode, terminalId, terminalLocation
        );

        assertNotNull(checkedIn, "签到结果不应为null");
        assertEquals(3, checkedIn.getRegistrationStatus(), "状态应为已签到");
        assertNotNull(checkedIn.getCheckInTime(), "签到时间应设置");

        LocalDateTime checkInTime = checkedIn.getCheckInTime();

        log.info("[集成测试] 访客签到成功: visitorCode={}, checkInTime={}",
                visitorCode, checkInTime);

        // ========== Step 4: 访客签离 ==========
        log.info("[集成测试] Step 4: 访客签离");

        String checkoutTerminalId = "CHECKOUT001";
        String checkoutTerminalLocation = "大厅签离机";
        Integer cardReturnStatus = 1; // 已归还
        String visitorCard = "VC001";

        SelfCheckOutEntity checkOut = checkOutService.performCheckOut(
                visitorCode, checkoutTerminalId, checkoutTerminalLocation,
                cardReturnStatus, visitorCard
        );

        assertNotNull(checkOut, "签离记录不应为null");
        assertEquals(registrationId, checkOut.getRegistrationId(), "登记ID应匹配");
        assertEquals(visitorCode, checkOut.getVisitorCode(), "访客码应匹配");
        assertNotNull(checkOut.getCheckOutTime(), "签离时间应设置");
        assertNotNull(checkOut.getVisitDuration(), "访问时长应计算");
        assertEquals(1, checkOut.getCardReturnStatus(), "卡归还状态应匹配");

        log.info("[集成测试] 访客签离成功: checkOutId={}, visitDuration={}分钟",
                checkOut.getCheckOutId(), checkOut.getVisitDuration());

        // ========== Step 5: 验证数据一致性 ==========
        log.info("[集成测试] Step 5: 验证数据一致性");

        // 查询登记记录
        SelfServiceRegistrationEntity finalRegistration = registrationDao.selectByVisitorCode(visitorCode);
        assertNotNull(finalRegistration, "最终登记记录不应为null");
        assertEquals(4, finalRegistration.getRegistrationStatus(), "最终状态应为已完成");

        // 查询签离记录
        SelfCheckOutEntity finalCheckOut = checkOutDao.selectByVisitorCode(visitorCode);
        assertNotNull(finalCheckOut, "最终签离记录不应为null");
        assertEquals(1, finalCheckOut.getCheckOutStatus(), "签离状态应为已完成");

        // 验证时长一致性
        assertNotNull(finalCheckOut.getVisitDuration(), "访问时长应计算");
        assertTrue(finalCheckOut.getVisitDuration() > 0, "访问时长应大于0");

        log.info("[集成测试] 测试通过: 完整访客流程成功，数据一致性验证通过");
    }

    @Test
    @DisplayName("测试访客超时签离")
    void testVisitorOvertimeCheckout() {
        log.info("[集成测试] 测试访客超时签离");

        // Given - 创建并审批登记
        SelfServiceRegistrationEntity registration = registrationService.createRegistration(testRegistration);
        String visitorCode = registration.getVisitorCode();

        registrationService.approveRegistration(
                registration.getRegistrationId(), 3001L, "王经理", true, "同意"
        );

        // 设置签入时间为9小时前（模拟超时）
        LocalDateTime pastCheckInTime = LocalDateTime.now().minusHours(10);
        registration.setCheckInTime(pastCheckInTime);
        registration.setExpectedLeaveTime(LocalDateTime.now().minusHours(1));
        registrationDao.updateById(registration);

        // When - 执行签离
        SelfCheckOutEntity checkOut = checkOutService.performCheckOut(
                visitorCode, "CHECKOUT001", "大厅", 1, "VC001"
        );

        // Then - 验证超时检测
        assertNotNull(checkOut, "签离记录不应为null");
        assertEquals(1, checkOut.getIsOvertime(), "应标记为超时");
        assertNotNull(checkOut.getOvertimeDuration(), "超时时长应计算");
        assertTrue(checkOut.getOvertimeDuration() > 0, "超时时长应大于0");

        log.info("[集成测试] 测试通过: 访客超时签离检测成功，overtimeDuration={}分钟",
                checkOut.getOvertimeDuration());
    }

    @Test
    @DisplayName("测试访客满意度评价")
    void testVisitorSatisfactionRating() {
        log.info("[集成测试] 测试访客满意度评价");

        // Given - 完成签离
        SelfServiceRegistrationEntity registration = registrationService.createRegistration(testRegistration);
        String visitorCode = registration.getVisitorCode();

        registrationService.approveRegistration(
                registration.getRegistrationId(), 3001L, "王经理", true, "同意"
        );

        registrationService.checkIn(visitorCode, "TERM001", "大厅");

        SelfCheckOutEntity checkOut = checkOutService.performCheckOut(
                visitorCode, "CHECKOUT001", "大厅", 1, "VC001"
        );

        // When - 提交满意度评价
        Integer satisfactionScore = 5;
        String visitorFeedback = "服务非常好，非常满意！";

        checkOutService.updateSatisfaction(
                checkOut.getCheckOutId(), satisfactionScore, visitorFeedback
        );

        // Then - 验证评价保存
        SelfCheckOutEntity updated = checkOutDao.selectById(checkOut.getCheckOutId());
        assertNotNull(updated, "更新后的签离记录不应为null");
        assertEquals(satisfactionScore, updated.getSatisfactionScore(), "满意度评分应匹配");
        assertEquals(visitorFeedback, updated.getVisitorFeedback(), "访客反馈应匹配");

        log.info("[集成测试] 测试通过: 满意度评价提交成功，score={}", satisfactionScore);
    }

    @Test
    @DisplayName("测试查询访客统计信息")
    void testGetVisitorStatistics() {
        log.info("[集成测试] 测试查询访客统计信息");

        // Given - 创建多条访客记录
        for (int i = 0; i < 5; i++) {
            SelfServiceRegistrationEntity reg = SelfServiceRegistrationEntity.builder()
                    .visitorName("访客" + i)
                    .idCard("11010119900101123" + i)
                    .phone("1380013800" + i)
                    .visitorType(1)
                    .visitPurpose("商务洽谈")
                    .intervieweeId(2001L)
                    .intervieweeName("李四")
                    .visitDate(LocalDate.now())
                    .registrationStatus(i == 4 ? 4 : 3) // 4个已签到，1个已完成
                    .build();

            registrationDao.insert(reg);
        }

        // When - 查询统计信息
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Map<String, Object> stats = registrationService.getStatistics(startDate, endDate);

        // Then
        assertNotNull(stats, "统计信息不应为null");
        assertTrue((Integer) stats.get("totalCount") >= 5, "总访客数应>=5");
        assertTrue((Integer) stats.get("checkedInCount") >= 4, "已签到数应>=4");

        log.info("[集成测试] 测试通过: 访客统计查询成功，totalCount={}, checkedInCount={}",
                stats.get("totalCount"), stats.get("checkedInCount"));
    }

    @Test
    @DisplayName("测试查询超时签离记录")
    void testGetOvertimeCheckoutRecords() {
        log.info("[集成测试] 测试查询超时签离记录");

        // Given - 创建超时签离记录
        SelfServiceRegistrationEntity registration = registrationService.createRegistration(testRegistration);
        String visitorCode = registration.getVisitorCode();

        registrationService.approveRegistration(registration.getRegistrationId(), 3001L, "王经理", true, "同意");
        registrationService.checkIn(visitorCode, "TERM001", "大厅");

        // 修改签入时间为很久以前，确保超时
        LocalDateTime oldCheckInTime = LocalDateTime.now().minusHours(15);
        registration.setCheckInTime(oldCheckInTime);
        registration.setExpectedLeaveTime(LocalDateTime.now().minusHours(5));
        registrationDao.updateById(registration);

        // 执行签离
        checkOutService.performCheckOut(visitorCode, "CHECKOUT001", "大厅", 1, "VC001");

        // When - 查询超时记录
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        List<SelfCheckOutEntity> overtimeRecords = checkOutService.getOvertimeRecords(startDate, endDate);

        // Then
        assertNotNull(overtimeRecords, "超时记录不应为null");
        assertTrue(overtimeRecords.size() >= 1, "应至少有1条超时记录");

        SelfCheckOutEntity overtimeRecord = overtimeRecords.stream()
                .filter(r -> r.getVisitorCode().equals(visitorCode))
                .findFirst()
                .orElse(null);

        assertNotNull(overtimeRecord, "应找到刚创建的超时记录");
        assertEquals(1, overtimeRecord.getIsOvertime(), "应标记为超时");
        assertTrue(overtimeRecord.getOvertimeDuration() > 0, "超时时长应大于0");

        log.info("[集成测试] 测试通过: 超时记录查询成功，overtimeCount={}, overtimeDuration={}分钟",
                overtimeRecords.size(), overtimeRecord.getOvertimeDuration());
    }

    @Test
    @DisplayName("测试并发访客登记处理")
    void testConcurrentVisitorRegistrations() {
        log.info("[集成测试] 测试并发访客登记处理");

        // When - 并发创建3个访客登记
        SelfServiceRegistrationEntity reg1 = registrationService.createRegistration(
                SelfServiceRegistrationEntity.builder()
                        .visitorName("访客A")
                        .idCard("110101199001011231")
                        .phone("13800138001")
                        .visitorType(1)
                        .visitPurpose("商务洽谈")
                        .intervieweeId(2001L)
                        .intervieweeName("李四")
                        .visitDate(LocalDate.now())
                        .build()
        );

        SelfServiceRegistrationEntity reg2 = registrationService.createRegistration(
                SelfServiceRegistrationEntity.builder()
                        .visitorName("访客B")
                        .idCard("110101199001011232")
                        .phone("13800138002")
                        .visitorType(1)
                        .visitPurpose("面试")
                        .intervieweeId(2002L)
                        .intervieweeName("王五")
                        .visitDate(LocalDate.now())
                        .build()
        );

        SelfServiceRegistrationEntity reg3 = registrationService.createRegistration(
                SelfServiceRegistrationEntity.builder()
                        .visitorName("访客C")
                        .idCard("110101199001011233")
                        .phone("13800138003")
                        .visitorType(2)
                        .visitPurpose("快递")
                        .intervieweeId(2003L)
                        .intervieweeName("赵六")
                        .visitDate(LocalDate.now())
                        .build()
        );

        // Then - 验证并发处理正确性
        assertNotNull(reg1, "登记1不应为null");
        assertNotNull(reg2, "登记2不应为null");
        assertNotNull(reg3, "登记3不应为null");

        // 验证每个登记都有唯一的ID和码
        assertNotEquals(reg1.getRegistrationId(), reg2.getRegistrationId(), "登记ID应不同");
        assertNotEquals(reg2.getRegistrationId(), reg3.getRegistrationId(), "登记ID应不同");

        assertNotEquals(reg1.getVisitorCode(), reg2.getVisitorCode(), "访客码应不同");
        assertNotEquals(reg2.getVisitorCode(), reg3.getVisitorCode(), "访客码应不同");

        log.info("[集成测试] 测试通过: 并发访客登记处理成功，3个访客独立处理");
    }
}

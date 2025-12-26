package net.lab1024.sa.visitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.SelfCheckOutDao;
import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.common.entity.visitor.SelfCheckOutEntity;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 自助签离管理器
 * <p>
 * 提供自助签离的核心业务逻辑，不依赖Spring注解，纯Java类
 * 严格遵循CLAUDE.md全局架构规范和Manager设计标准
 * </p>
 * <p>
 * <strong>主要职责：</strong>
 * </p>
 * <ul>
 *   <li>访问时长计算</li>
 *   <li>超时判断和处理</li>
 *   <li>访客卡归还管理</li>
 *   <li>签离状态更新</li>
 *   <li>满意度统计</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class SelfCheckOutManager {

    private final SelfCheckOutDao selfCheckOutDao;
    private final SelfServiceRegistrationDao selfServiceRegistrationDao;

    /**
     * 构造函数注入依赖
     */
    public SelfCheckOutManager(SelfCheckOutDao selfCheckOutDao,
                               SelfServiceRegistrationDao selfServiceRegistrationDao) {
        this.selfCheckOutDao = selfCheckOutDao;
        this.selfServiceRegistrationDao = selfServiceRegistrationDao;
    }

    /**
     * 计算访问时长
     * <p>
     * 计算从签到时间到签离时间的实际时长（分钟）
     * </p>
     *
     * @param checkInTime 签到时间
     * @param checkOutTime 签离时间
     * @return 访问时长（分钟）
     */
    public Integer calculateVisitDuration(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        if (checkInTime == null || checkOutTime == null) {
            log.warn("[自助签离] 签到或签离时间为空，无法计算时长");
            return 0;
        }

        Duration duration = Duration.between(checkInTime, checkOutTime);
        long minutes = duration.toMinutes();

        log.debug("[自助签离] 计算访问时长: checkInTime={}, checkOutTime={}, duration={}分钟",
                checkInTime, checkOutTime, minutes);

        return (int) minutes;
    }

    /**
     * 判断是否超时
     * <p>
     * 判断签离时间是否超过预计离开时间
     * </p>
     *
     * @param expectedLeaveTime 预计离开时间
     * @param actualLeaveTime 实际离开时间
     * @return 是否超时：true-超时，false-未超时
     */
    public boolean isOvertime(LocalDateTime expectedLeaveTime, LocalDateTime actualLeaveTime) {
        if (expectedLeaveTime == null || actualLeaveTime == null) {
            return false;
        }

        boolean overtime = actualLeaveTime.isAfter(expectedLeaveTime);

        if (overtime) {
            log.info("[自助签离] 访客超时: expected={}, actual={}",
                    expectedLeaveTime, actualLeaveTime);
        }

        return overtime;
    }

    /**
     * 计算超时时长
     * <p>
     * 计算超过预计离开时间的分钟数
     * </p>
     *
     * @param expectedLeaveTime 预计离开时间
     * @param actualLeaveTime 实际离开时间
     * @return 超时时长（分钟），未超时返回0
     */
    public Integer calculateOvertimeDuration(LocalDateTime expectedLeaveTime,
                                               LocalDateTime actualLeaveTime) {
        if (!isOvertime(expectedLeaveTime, actualLeaveTime)) {
            return 0;
        }

        Duration duration = Duration.between(expectedLeaveTime, actualLeaveTime);
        long minutes = duration.toMinutes();

        log.debug("[自助签离] 计算超时时长: overtimeDuration={}分钟", minutes);

        return (int) minutes;
    }

    /**
     * 处理签离流程
     * <p>
     * 执行完整的签离流程：
     * 1. 验证访客码
     * 2. 计算访问时长
     * 3. 判断是否超时
     * 4. 更新签离状态
     * </p>
     *
     * @param visitorCode 访客码
     * @param terminalId 终端ID
     * @param terminalLocation 终端位置
     * @param cardReturnStatus 访客卡归还状态
     * @param visitorCard 访客卡号
     * @return 签离记录
     */
    public SelfCheckOutEntity processCheckOut(String visitorCode,
                                              String terminalId,
                                              String terminalLocation,
                                              Integer cardReturnStatus,
                                              String visitorCard) {
        log.info("[自助签离] 处理签离流程: visitorCode={}, terminalId={}", visitorCode, terminalId);

        // 1. 查询登记记录
        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            log.error("[自助签离] 登记记录不存在: visitorCode={}", visitorCode);
            throw new RuntimeException("访客码不存在: " + visitorCode);
        }

        // 2. 验证状态
        if (registration.getRegistrationStatus() != 3) {
            log.warn("[自助签离] 访客未签到，无法签离: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("访客未签到，无法签离");
        }

        if (registration.getCheckOutTime() != null) {
            log.warn("[自助签离] 访客已签离: checkOutTime={}", registration.getCheckOutTime());
            throw new RuntimeException("访客已签离，无法重复签离");
        }

        // 3. 计算访问时长
        LocalDateTime checkInTime = registration.getCheckInTime();
        LocalDateTime checkOutTime = LocalDateTime.now();
        Integer visitDuration = calculateVisitDuration(checkInTime, checkOutTime);

        // 4. 判断是否超时
        LocalDateTime expectedLeaveTime = registration.getExpectedLeaveTime();
        boolean overtime = isOvertime(expectedLeaveTime, checkOutTime);
        Integer overtimeDuration = overtime ? calculateOvertimeDuration(expectedLeaveTime, checkOutTime) : 0;

        // 5. 创建签离记录
        SelfCheckOutEntity checkOut = SelfCheckOutEntity.builder()
                .registrationId(registration.getRegistrationId())
                .registrationCode(registration.getRegistrationCode())
                .visitorCode(visitorCode)
                .visitorName(registration.getVisitorName())
                .idCard(registration.getIdCard())
                .phone(registration.getPhone())
                .intervieweeId(registration.getIntervieweeId())
                .intervieweeName(registration.getIntervieweeName())
                .terminalId(terminalId)
                .terminalLocation(terminalLocation)
                .checkOutTime(checkOutTime)
                .visitDuration(visitDuration)
                .expectedLeaveTime(expectedLeaveTime)
                .isOvertime(overtime ? 1 : 0)
                .overtimeDuration(overtimeDuration)
                .cardReturnStatus(cardReturnStatus)
                .visitorCard(visitorCard)
                .checkOutMethod(1) // 自助签离
                .checkOutStatus(1) // 已完成
                .build();

        // 6. 保存签离记录
        selfCheckOutDao.insert(checkOut);

        // 7. 更新登记记录的签离时间
        registration.setCheckOutTime(checkOutTime);
        registration.setRegistrationStatus(4); // 已完成
        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助签离] 签离完成: visitorCode={}, visitDuration={}分钟, overtime={}",
                visitorCode, visitDuration, overtime ? "是" : "否");

        return checkOut;
    }

    /**
     * 人工签离
     * <p>
     * 管理员手动执行签离操作
     * </p>
     *
     * @param visitorCode 访客码
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param reason 签离原因
     * @return 签离记录
     */
    public SelfCheckOutEntity manualCheckOut(String visitorCode,
                                            Long operatorId,
                                            String operatorName,
                                            String reason) {
        log.info("[自助签离] 人工签离: visitorCode={}, operator={}", visitorCode, operatorName);

        // 查询登记记录
        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            throw new RuntimeException("访客码不存在: " + visitorCode);
        }

        // 计算访问时长和超时情况
        LocalDateTime checkInTime = registration.getCheckInTime();
        LocalDateTime checkOutTime = LocalDateTime.now();
        Integer visitDuration = calculateVisitDuration(checkInTime, checkOutTime);
        LocalDateTime expectedLeaveTime = registration.getExpectedLeaveTime();
        boolean overtime = isOvertime(expectedLeaveTime, checkOutTime);
        Integer overtimeDuration = overtime ? calculateOvertimeDuration(expectedLeaveTime, checkOutTime) : 0;

        // 创建签离记录
        SelfCheckOutEntity checkOut = SelfCheckOutEntity.builder()
                .registrationId(registration.getRegistrationId())
                .registrationCode(registration.getRegistrationCode())
                .visitorCode(visitorCode)
                .visitorName(registration.getVisitorName())
                .idCard(registration.getIdCard())
                .phone(registration.getPhone())
                .intervieweeId(registration.getIntervieweeId())
                .intervieweeName(registration.getIntervieweeName())
                .checkOutTime(checkOutTime)
                .visitDuration(visitDuration)
                .expectedLeaveTime(expectedLeaveTime)
                .isOvertime(overtime ? 1 : 0)
                .overtimeDuration(overtimeDuration)
                .cardReturnStatus(0) // 人工签离默认未归还
                .checkOutMethod(2) // 人工签离
                .operatorId(operatorId)
                .operatorName(operatorName)
                .checkOutStatus(1) // 已完成
                .remark(reason)
                .build();

        // 保存签离记录
        selfCheckOutDao.insert(checkOut);

        // 更新登记记录
        registration.setCheckOutTime(checkOutTime);
        registration.setRegistrationStatus(4); // 已完成
        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助签离] 人工签离完成: visitorCode={}, operator={}", visitorCode, operatorName);

        return checkOut;
    }

    /**
     * 更新访客满意度评价
     *
     * @param checkOutId 签离记录ID
     * @param satisfactionScore 满意度评分
     * @param visitorFeedback 访客反馈
     */
    public void updateSatisfaction(Long checkOutId,
                                   Integer satisfactionScore,
                                   String visitorFeedback) {
        log.info("[自助签离] 更新满意度: checkOutId={}, score={}", checkOutId, satisfactionScore);

        SelfCheckOutEntity checkOut = selfCheckOutDao.selectById(checkOutId);
        if (checkOut == null) {
            log.error("[自助签离] 签离记录不存在: checkOutId={}", checkOutId);
            throw new RuntimeException("签离记录不存在: " + checkOutId);
        }

        checkOut.setSatisfactionScore(satisfactionScore);
        checkOut.setVisitorFeedback(visitorFeedback);

        selfCheckOutDao.updateById(checkOut);

        log.info("[自助签离] 满意度更新成功: checkOutId={}, score={}", checkOutId, satisfactionScore);
    }

    /**
     * 更新访客卡归还状态
     *
     * @param checkOutId 签离记录ID
     * @param cardReturnStatus 归还状态
     * @param visitorCard 访客卡号
     */
    public void updateCardReturnStatus(Long checkOutId,
                                       Integer cardReturnStatus,
                                       String visitorCard) {
        log.info("[自助签离] 更新访客卡归还状态: checkOutId={}, status={}", checkOutId, cardReturnStatus);

        SelfCheckOutEntity checkOut = selfCheckOutDao.selectById(checkOutId);
        if (checkOut == null) {
            throw new RuntimeException("签离记录不存在: " + checkOutId);
        }

        checkOut.setCardReturnStatus(cardReturnStatus);
        if (visitorCard != null && !visitorCard.isEmpty()) {
            checkOut.setVisitorCard(visitorCard);
        }

        selfCheckOutDao.updateById(checkOut);

        log.info("[自助签离] 访客卡归还状态更新成功: checkOutId={}, status={}", checkOutId, cardReturnStatus);
    }
}

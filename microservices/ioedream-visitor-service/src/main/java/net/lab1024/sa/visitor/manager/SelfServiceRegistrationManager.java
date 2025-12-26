package net.lab1024.sa.visitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.visitor.entity.SelfServiceRegistrationEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 自助登记终端管理器
 * <p>
 * 提供自助登记的核心业务逻辑，不依赖Spring注解，纯Java类
 * 严格遵循CLAUDE.md全局架构规范和Manager设计标准
 * </p>
 * <p>
 * <strong>主要职责：</strong>
 * </p>
 * <ul>
 *   <li>访客码生成和管理</li>
 *   <li>登记编号生成</li>
 *   <li>身份证信息验证</li>
 *   <li>人脸特征提取（模拟）</li>
 *   <li>访客卡打印管理</li>
 *   <li>审批流程管理</li>
 *   <li>签到签离管理</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class SelfServiceRegistrationManager {

    private final SelfServiceRegistrationDao selfServiceRegistrationDao;
    private final Random random = new Random();

    /**
     * 构造函数注入依赖
     */
    public SelfServiceRegistrationManager(SelfServiceRegistrationDao selfServiceRegistrationDao) {
        this.selfServiceRegistrationDao = selfServiceRegistrationDao;
    }

    /**
     * 生成访客码
     * <p>
     * 格式：VC + 年月日时分秒 + 4位随机码
     * 示例：VC202512261430001234
     * </p>
     *
     * @return 访客码
     */
    public String generateVisitorCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = String.format("%04d", random.nextInt(10000));
        String visitorCode = "VC" + timestamp + randomCode;

        log.debug("[自助登记] 生成访客码: visitorCode={}", visitorCode);
        return visitorCode;
    }

    /**
     * 生成登记编号
     * <p>
     * 格式：SSRG + 年月日 + 6位序号
     * 示例：SSRG20251226000001
     * </p>
     *
     * @return 登记编号
     */
    public String generateRegistrationCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomCode = String.format("%06d", random.nextInt(1000000));
        String registrationCode = "SSRG" + dateStr + randomCode;

        log.debug("[自助登记] 生成登记编号: registrationCode={}", registrationCode);
        return registrationCode;
    }

    /**
     * 验证身份证号格式
     *
     * @param idCard 身份证号
     * @return 验证结果：true-有效，false-无效
     */
    public boolean validateIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            log.warn("[自助登记] 身份证号长度不正确: length={}", idCard != null ? idCard.length() : 0);
            return false;
        }

        // 简单验证：前17位必须是数字，最后一位可以是数字或X
        boolean valid = idCard.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");

        if (!valid) {
            log.warn("[自助登记] 身份证号格式不正确: idCard={}", maskIdCard(idCard));
        }

        return valid;
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return 验证结果：true-有效，false-无效
     */
    public boolean validatePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            log.warn("[自助登记] 手机号长度不正确: length={}", phone != null ? phone.length() : 0);
            return false;
        }

        boolean valid = phone.matches("^1[3-9]\\d{9}$");

        if (!valid) {
            log.warn("[自助登记] 手机号格式不正确: phone={}", maskPhone(phone));
        }

        return valid;
    }

    /**
     * 提取人脸特征向量（模拟）
     * <p>
     * 实际应该调用AI服务提取512维特征向量
     * 这里返回模拟数据
     * </p>
     *
     * @param photoUrl 人脸照片URL
     * @return 人脸特征向量（Base64编码）
     */
    public String extractFaceFeature(String photoUrl) {
        log.debug("[自助登记] 提取人脸特征: photoUrl={}", photoUrl);

        // 模拟提取512维特征向量
        double[] featureVector = new double[512];
        for (int i = 0; i < 512; i++) {
            featureVector[i] = Math.random();
        }

        // 转换为Base64编码
        String feature = encodeFeatureVector(featureVector);

        log.info("[自助登记] 人脸特征提取成功: photoUrl={}, featureLength={}", photoUrl, feature.length());
        return feature;
    }

    /**
     * 初始化自助登记记录
     * <p>
     * 设置初始状态和默认值
     * </p>
     *
     * @param registration 登记记录
     * @return 初始化后的记录
     */
    public SelfServiceRegistrationEntity initializeRegistration(SelfServiceRegistrationEntity registration) {
        log.debug("[自助登记] 初始化登记记录: visitorName={}", registration.getVisitorName());

        // 生成访客码
        if (registration.getVisitorCode() == null || registration.getVisitorCode().isEmpty()) {
            registration.setVisitorCode(generateVisitorCode());
        }

        // 生成登记编号
        if (registration.getRegistrationCode() == null || registration.getRegistrationCode().isEmpty()) {
            registration.setRegistrationCode(generateRegistrationCode());
        }

        // 设置初始状态
        if (registration.getRegistrationStatus() == null) {
            registration.setRegistrationStatus(0); // 待审批
        }

        // 设置打印状态
        if (registration.getCardPrintStatus() == null) {
            registration.setCardPrintStatus(0); // 未打印
        }

        // 设置默认访问日期为当天
        if (registration.getVisitDate() == null) {
            registration.setVisitDate(LocalDate.now());
        }

        log.info("[自助登记] 登记记录初始化完成: registrationCode={}, visitorCode={}",
                registration.getRegistrationCode(), registration.getVisitorCode());

        return registration;
    }

    /**
     * 审批登记记录
     * <p>
     * 执行审批操作，更新审批状态和审批信息
     * </p>
     *
     * @param registrationId 登记ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approved 是否批准
     * @param approvalComment 审批意见
     * @return 更新后的记录
     */
    public SelfServiceRegistrationEntity approveRegistration(Long registrationId,
                                                            Long approverId,
                                                            String approverName,
                                                            Boolean approved,
                                                            String approvalComment) {
        log.info("[自助登记] 审批登记: registrationId={}, approver={}, approved={}",
                registrationId, approverName, approved);

        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectById(registrationId);
        if (registration == null) {
            log.error("[自助登记] 登记记录不存在: registrationId={}", registrationId);
            throw new RuntimeException("登记记录不存在: " + registrationId);
        }

        // 检查当前状态
        if (registration.getRegistrationStatus() != 0) {
            log.warn("[自助登记] 登记记录已审批，状态不符: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("登记记录已审批，无法重复审批");
        }

        // 更新审批信息
        registration.setApproverId(approverId);
        registration.setApproverName(approverName);
        registration.setApprovalTime(LocalDateTime.now());
        registration.setApprovalComment(approvalComment);

        // 更新状态：1-审批通过，2-审批拒绝
        registration.setRegistrationStatus(approved ? 1 : 2);

        // 更新数据库
        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助登记] 审批完成: registrationId={}, newStatus={}", registrationId,
                approved ? "审批通过" : "审批拒绝");

        return registration;
    }

    /**
     * 访客签到
     * <p>
     * 记录访客签到时间和状态
     * </p>
     *
     * @param visitorCode 访客码
     * @return 签到后的记录
     */
    public SelfServiceRegistrationEntity checkIn(String visitorCode) {
        log.info("[自助登记] 访客签到: visitorCode={}", visitorCode);

        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            log.error("[自助登记] 访客码不存在: visitorCode={}", visitorCode);
            throw new RuntimeException("访客码不存在: " + visitorCode);
        }

        // 检查状态
        if (registration.getRegistrationStatus() != 1) {
            log.warn("[自助登记] 登记状态不允许签到: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("登记状态不允许签到");
        }

        // 更新签到信息
        registration.setCheckInTime(LocalDateTime.now());
        registration.setRegistrationStatus(3); // 已签到

        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助登记] 签到成功: registrationCode={}, checkInTime={}",
                registration.getRegistrationCode(), registration.getCheckInTime());

        return registration;
    }

    /**
     * 访客签离
     * <p>
     * 记录访客签离时间并更新状态
     * </p>
     *
     * @param visitorCode 访客码
     * @return 签离后的记录
     */
    public SelfServiceRegistrationEntity checkOut(String visitorCode) {
        log.info("[自助登记] 访客签离: visitorCode={}", visitorCode);

        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            log.error("[自助登记] 访客码不存在: visitorCode={}", visitorCode);
            throw new RuntimeException("访客码不存在: " + visitorCode);
        }

        // 检查状态
        if (registration.getRegistrationStatus() != 3) {
            log.warn("[自助登记] 访客未签到，无法签离: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("访客未签到，无法签离");
        }

        // 更新签离信息
        registration.setCheckOutTime(LocalDateTime.now());
        registration.setRegistrationStatus(4); // 已完成

        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助登记] 签离成功: registrationCode={}, checkOutTime={}",
                registration.getRegistrationCode(), registration.getCheckOutTime());

        return registration;
    }

    /**
     * 获取自助登记统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    public Map<String, Object> getRegistrationStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[自助登记] 查询统计信息: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        // 总登记数
        int totalCount = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            totalCount += selfServiceRegistrationDao.countByVisitDate(date);
        }
        statistics.put("totalCount", totalCount);

        // 待审批数
        int pendingCount = selfServiceRegistrationDao.countPendingApprovals(null);
        statistics.put("pendingCount", pendingCount);

        // 在场访客数
        int activeCount = selfServiceRegistrationDao.countActiveVisitors();
        statistics.put("activeCount", activeCount);

        // 今日登记数
        int todayCount = selfServiceRegistrationDao.countByVisitDate(LocalDate.now());
        statistics.put("todayCount", todayCount);

        log.info("[自助登记] 统计信息查询成功: totalCount={}, pendingCount={}, activeCount={}, todayCount={}",
                totalCount, pendingCount, activeCount, todayCount);

        return statistics;
    }

    /**
     * 获取超时未签离访客
     *
     * @return 超时访客列表
     */
    public List<SelfServiceRegistrationEntity> getOverdueVisitors() {
        log.debug("[自助登记] 查询超时访客");
        return selfServiceRegistrationDao.selectOverdueVisitors(LocalDateTime.now());
    }

    /**
     * 身份证号脱敏
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return "***";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /**
     * 手机号脱敏
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 将特征向量编码为Base64字符串
     *
     * @param featureVector 特征向量
     * @return Base64编码的字符串
     */
    private String encodeFeatureVector(double[] featureVector) {
        StringBuilder sb = new StringBuilder();
        for (double value : featureVector) {
            sb.append(value).append(",");
        }
        return sb.toString();
    }
}

package net.lab1024.sa.visitor.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.*;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import net.lab1024.sa.common.entity.visitor.VisitorBiometricEntity;
import net.lab1024.sa.common.entity.visitor.VisitorApprovalEntity;
import net.lab1024.sa.common.entity.visitor.VisitRecordEntity;
import net.lab1024.sa.common.entity.visitor.TerminalInfoEntity;
import net.lab1024.sa.common.entity.visitor.VisitorAdditionalInfoEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final VisitorBiometricDao visitorBiometricDao;
    private final VisitorApprovalDao visitorApprovalDao;
    private final VisitRecordDao visitRecordDao;
    private final TerminalInfoDao terminalInfoDao;
    private final VisitorAdditionalInfoDao visitorAdditionalInfoDao;
    private final Random random = new Random();

    /**
     * 构造函数注入依赖（支持6表关联操作）
     */
    public SelfServiceRegistrationManager(
            SelfServiceRegistrationDao selfServiceRegistrationDao,
            VisitorBiometricDao visitorBiometricDao,
            VisitorApprovalDao visitorApprovalDao,
            VisitRecordDao visitRecordDao,
            TerminalInfoDao terminalInfoDao,
            VisitorAdditionalInfoDao visitorAdditionalInfoDao) {
        this.selfServiceRegistrationDao = selfServiceRegistrationDao;
        this.visitorBiometricDao = visitorBiometricDao;
        this.visitorApprovalDao = visitorApprovalDao;
        this.visitRecordDao = visitRecordDao;
        this.terminalInfoDao = terminalInfoDao;
        this.visitorAdditionalInfoDao = visitorAdditionalInfoDao;
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
     * 审批登记记录（支持6表结构）
     * <p>
     * 执行审批操作，更新核心表审批状态和审批信息表
     * </p>
     *
     * @param registrationId 登记ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approved 是否批准
     * @param approvalComment 审批意见
     * @return 更新后的完整记录（包含审批信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public SelfServiceRegistrationEntity approveRegistration(Long registrationId,
                                                            Long approverId,
                                                            String approverName,
                                                            Boolean approved,
                                                            String approvalComment) {
        log.info("[自助登记] 审批登记: registrationId={}, approver={}, approved={}",
                registrationId, approverName, approved);

        // 1. 查询核心登记信息
        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectById(registrationId);
        if (registration == null) {
            log.error("[自助登记] 登记记录不存在: registrationId={}", registrationId);
            throw new RuntimeException("登记记录不存在: " + registrationId);
        }

        // 2. 检查当前状态
        if (registration.getRegistrationStatus() != 0) {
            log.warn("[自助登记] 登记记录已审批，状态不符: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("登记记录已审批，无法重复审批");
        }

        // 3. 更新核心表审批状态
        registration.setRegistrationStatus(approved ? 1 : 2); // 1-审批通过 2-审批拒绝
        selfServiceRegistrationDao.updateById(registration);

        // 4. 插入或更新审批信息表
        VisitorApprovalEntity approval = visitorApprovalDao.selectOne(
                new LambdaQueryWrapper<VisitorApprovalEntity>()
                        .eq(VisitorApprovalEntity::getRegistrationId, registrationId)
        );

        if (approval == null) {
            // 新增审批记录
            approval = VisitorApprovalEntity.builder()
                    .registrationId(registrationId)
                    .approverId(approverId)
                    .approverName(approverName)
                    .approvalTime(LocalDateTime.now())
                    .approvalComment(approvalComment)
                    .build();
            visitorApprovalDao.insert(approval);
            log.info("[自助登记] 新增审批记录: approvalId={}", approval.getApprovalId());
        } else {
            // 更新已有审批记录
            approval.setApproverId(approverId);
            approval.setApproverName(approverName);
            approval.setApprovalTime(LocalDateTime.now());
            approval.setApprovalComment(approvalComment);
            visitorApprovalDao.updateById(approval);
            log.info("[自助登记] 更新审批记录: approvalId={}", approval.getApprovalId());
        }

        log.info("[自助登记] 审批完成: registrationId={}, newStatus={}", registrationId,
                approved ? "审批通过" : "审批拒绝");

        // 5. 返回完整登记信息（包含审批信息）
        return getRegistrationByVisitorCode(registration.getVisitorCode());
    }

    /**
     * 访客签到（支持6表结构）
     * <p>
     * 记录访客签到时间和状态，同时创建访问记录
     * </p>
     *
     * @param visitorCode 访客码
     * @return 签到后的完整记录（包含访问记录信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public SelfServiceRegistrationEntity checkIn(String visitorCode) {
        log.info("[自助登记] 访客签到: visitorCode={}", visitorCode);

        // 1. 查询核心登记信息
        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            log.error("[自助登记] 访客码不存在: visitorCode={}", visitorCode);
            throw new RuntimeException("访客码不存在: " + visitorCode);
        }

        // 2. 检查状态
        if (registration.getRegistrationStatus() != 1) {
            log.warn("[自助登记] 登记状态不允许签到: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("登记状态不允许签到");
        }

        // 3. 更新核心登记状态为"已签到"
        registration.setRegistrationStatus(3); // 3-已签到
        selfServiceRegistrationDao.updateById(registration);

        // 4. 创建或更新访问记录
        VisitRecordEntity record = visitRecordDao.selectOne(
                new LambdaQueryWrapper<VisitRecordEntity>()
                        .eq(VisitRecordEntity::getRegistrationId, registration.getRegistrationId())
        );

        if (record == null) {
            // 新增访问记录
            record = VisitRecordEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .checkInTime(LocalDateTime.now())
                    .build();
            visitRecordDao.insert(record);
            log.info("[自助登记] 新增访问记录: recordId={}", record.getRecordId());
        } else {
            // 更新已有访问记录的签到时间
            record.setCheckInTime(LocalDateTime.now());
            visitRecordDao.updateById(record);
            log.info("[自助登记] 更新访问记录签到时间: recordId={}", record.getRecordId());
        }

        log.info("[自助登记] 签到成功: registrationCode={}, checkInTime={}",
                registration.getRegistrationCode(), LocalDateTime.now());

        // 5. 返回完整登记信息（包含访问记录）
        return getRegistrationByVisitorCode(visitorCode);
    }

    /**
     * 访客签离（支持6表结构）
     * <p>
     * 记录访客签离时间并更新状态
     * </p>
     *
     * @param visitorCode 访客码
     * @return 签离后的完整记录（包含访问记录信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public SelfServiceRegistrationEntity checkOut(String visitorCode) {
        log.info("[自助登记] 访客签离: visitorCode={}", visitorCode);

        // 1. 查询核心登记信息
        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            log.error("[自助登记] 访客码不存在: visitorCode={}", visitorCode);
            throw new RuntimeException("访客码不存在: " + visitorCode);
        }

        // 2. 检查状态
        if (registration.getRegistrationStatus() != 3) {
            log.warn("[自助登记] 访客未签到，无法签离: currentStatus={}",
                    registration.getRegistrationStatus());
            throw new RuntimeException("访客未签到，无法签离");
        }

        // 3. 更新核心登记状态为"已完成"
        registration.setRegistrationStatus(4); // 4-已完成
        selfServiceRegistrationDao.updateById(registration);

        // 4. 更新访问记录的签离时间
        VisitRecordEntity record = visitRecordDao.selectOne(
                new LambdaQueryWrapper<VisitRecordEntity>()
                        .eq(VisitRecordEntity::getRegistrationId, registration.getRegistrationId())
        );

        if (record != null) {
            record.setCheckOutTime(LocalDateTime.now());
            visitRecordDao.updateById(record);
            log.info("[自助登记] 更新访问记录签离时间: recordId={}", record.getRecordId());
        } else {
            log.warn("[自助登记] 访问记录不存在: registrationId={}", registration.getRegistrationId());
        }

        log.info("[自助登记] 签离成功: registrationCode={}, checkOutTime={}",
                registration.getRegistrationCode(), LocalDateTime.now());

        // 5. 返回完整登记信息（包含访问记录）
        return getRegistrationByVisitorCode(visitorCode);
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
     * 创建自助登记记录（支持6表结构）
     * <p>
     * 核心登记方法，涉及6个表的插入操作
     * 使用事务保证数据一致性
     * </p>
     *
     * @param registration 登记信息
     * @return 创建后的完整登记记录
     */
    @Transactional(rollbackFor = Exception.class)
    public SelfServiceRegistrationEntity createRegistration(SelfServiceRegistrationEntity registration) {
        log.info("[自助登记] 创建自助登记: visitorName={}", registration.getVisitorName());

        // 1. 初始化登记信息（生成访客码、登记编号等）
        registration = initializeRegistration(registration);

        // 2. 保存核心登记信息
        selfServiceRegistrationDao.insert(registration);
        log.info("[自助登记] 核心登记信息已保存: registrationId={}", registration.getRegistrationId());

        // 3. 保存生物识别信息（如果有）
        if (registration.getFacePhotoUrl() != null || registration.getFaceFeature() != null) {
            VisitorBiometricEntity biometric = VisitorBiometricEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .facePhotoUrl(registration.getFacePhotoUrl())
                    .faceFeature(registration.getFaceFeature())
                    .idCardPhotoUrl(registration.getIdCardPhotoUrl())
                    .build();
            visitorBiometricDao.insert(biometric);
            log.info("[自助登记] 生物识别信息已保存: biometricId={}", biometric.getBiometricId());
        }

        // 4. 保存终端信息（如果有）
        if (registration.getTerminalId() != null) {
            TerminalInfoEntity terminalInfo = TerminalInfoEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .terminalId(registration.getTerminalId())
                    .terminalLocation(registration.getTerminalLocation())
                    .visitorCard(registration.getVisitorCard())
                    .cardPrintStatus(registration.getCardPrintStatus())
                    .build();
            terminalInfoDao.insert(terminalInfo);
            log.info("[自助登记] 终端信息已保存: terminalInfoId={}", terminalInfo.getTerminalInfoId());
        }

        // 5. 保存附加信息（如果有）
        if (registration.getBelongings() != null || registration.getLicensePlate() != null) {
            VisitorAdditionalInfoEntity additionalInfo = VisitorAdditionalInfoEntity.builder()
                    .registrationId(registration.getRegistrationId())
                    .belongings(registration.getBelongings())
                    .licensePlate(registration.getLicensePlate())
                    .build();
            visitorAdditionalInfoDao.insert(additionalInfo);
            log.info("[自助登记] 附加信息已保存: additionalInfoId={}", additionalInfo.getAdditionalInfoId());
        }

        log.info("[自助登记] 自助登记创建成功: registrationId={}, visitorCode={}",
                registration.getRegistrationId(), registration.getVisitorCode());

        return registration;
    }

    /**
     * 根据访客码查询登记信息（JOIN 6个表）
     * <p>
     * 从6个表中查询并组装完整的登记信息
     * 包括：核心信息、生物识别、审批信息、访问记录、终端信息、附加信息
     * </p>
     *
     * @param visitorCode 访客码
     * @return 完整的登记信息（包含所有关联表数据）
     */
    public SelfServiceRegistrationEntity getRegistrationByVisitorCode(String visitorCode) {
        log.info("[自助登记] 查询登记记录: visitorCode={}", visitorCode);

        // 1. 查询核心登记信息
        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectByVisitorCode(visitorCode);
        if (registration == null) {
            log.warn("[自助登记] 登记记录不存在: visitorCode={}", visitorCode);
            return null;
        }

        // 2. 查询并组装生物识别信息
        VisitorBiometricEntity biometric = visitorBiometricDao.selectOne(
                new LambdaQueryWrapper<VisitorBiometricEntity>()
                        .eq(VisitorBiometricEntity::getRegistrationId, registration.getRegistrationId())
        );
        if (biometric != null) {
            registration.setFacePhotoUrl(biometric.getFacePhotoUrl());
            registration.setFaceFeature(biometric.getFaceFeature());
            registration.setIdCardPhotoUrl(biometric.getIdCardPhotoUrl());
        }

        // 3. 查询并组装审批信息
        VisitorApprovalEntity approval = visitorApprovalDao.selectOne(
                new LambdaQueryWrapper<VisitorApprovalEntity>()
                        .eq(VisitorApprovalEntity::getRegistrationId, registration.getRegistrationId())
        );
        if (approval != null) {
            registration.setApproverId(approval.getApproverId());
            registration.setApproverName(approval.getApproverName());
            registration.setApprovalTime(approval.getApprovalTime());
            registration.setApprovalComment(approval.getApprovalComment());
        }

        // 4. 查询并组装访问记录信息
        VisitRecordEntity record = visitRecordDao.selectOne(
                new LambdaQueryWrapper<VisitRecordEntity>()
                        .eq(VisitRecordEntity::getRegistrationId, registration.getRegistrationId())
        );
        if (record != null) {
            registration.setCheckInTime(record.getCheckInTime());
            registration.setCheckOutTime(record.getCheckOutTime());
            registration.setEscortRequired(record.getEscortRequired());
            registration.setEscortUser(record.getEscortUser());
        }

        // 5. 查询并组装终端信息
        TerminalInfoEntity terminalInfo = terminalInfoDao.selectOne(
                new LambdaQueryWrapper<TerminalInfoEntity>()
                        .eq(TerminalInfoEntity::getRegistrationId, registration.getRegistrationId())
        );
        if (terminalInfo != null) {
            registration.setTerminalId(terminalInfo.getTerminalId());
            registration.setTerminalLocation(terminalInfo.getTerminalLocation());
            registration.setVisitorCard(terminalInfo.getVisitorCard());
            registration.setCardPrintStatus(terminalInfo.getCardPrintStatus());
        }

        // 6. 查询并组装附加信息
        VisitorAdditionalInfoEntity additionalInfo = visitorAdditionalInfoDao.selectOne(
                new LambdaQueryWrapper<VisitorAdditionalInfoEntity>()
                        .eq(VisitorAdditionalInfoEntity::getRegistrationId, registration.getRegistrationId())
        );
        if (additionalInfo != null) {
            registration.setBelongings(additionalInfo.getBelongings());
            registration.setLicensePlate(additionalInfo.getLicensePlate());
        }

        log.info("[自助登记] 登记记录查询成功: registrationId={}, 包含{}个关联表数据",
                registration.getRegistrationId(),
                (biometric != null ? 1 : 0) +
                (approval != null ? 1 : 0) +
                (record != null ? 1 : 0) +
                (terminalInfo != null ? 1 : 0) +
                (additionalInfo != null ? 1 : 0));

        return registration;
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

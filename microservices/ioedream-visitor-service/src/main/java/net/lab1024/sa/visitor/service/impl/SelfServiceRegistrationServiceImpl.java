package net.lab1024.sa.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.visitor.dao.SelfServiceRegistrationDao;
import net.lab1024.sa.common.entity.visitor.SelfServiceRegistrationEntity;
import net.lab1024.sa.visitor.manager.SelfServiceRegistrationManager;
import net.lab1024.sa.visitor.service.SelfServiceRegistrationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自助登记终端服务实现类
 * <p>
 * 实现访客自助登记的完整业务功能
 * 严格遵循CLAUDE.md全局架构规范和Service实现标准
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class SelfServiceRegistrationServiceImpl implements SelfServiceRegistrationService {

    @Resource
    private SelfServiceRegistrationManager selfServiceRegistrationManager;

    @Resource
    private SelfServiceRegistrationDao selfServiceRegistrationDao;

    @Override
    public SelfServiceRegistrationEntity createRegistration(SelfServiceRegistrationEntity registration) {
        log.info("[自助登记服务] 创建自助登记: visitorName={}, idCard={}, visitPurpose={}",
                registration.getVisitorName(),
                maskIdCard(registration.getIdCard()),
                registration.getVisitPurpose());

        // 1. 验证身份证号
        if (!selfServiceRegistrationManager.validateIdCard(registration.getIdCard())) {
            throw new RuntimeException("身份证号格式不正确");
        }

        // 2. 验证手机号
        if (!selfServiceRegistrationManager.validatePhone(registration.getPhone())) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 3. 如果有人脸照片，提取特征
        if (registration.getFacePhotoUrl() != null && !registration.getFacePhotoUrl().isEmpty()) {
            String faceFeature = selfServiceRegistrationManager.extractFaceFeature(registration.getFacePhotoUrl());
            registration.setFaceFeature(faceFeature);
        }

        // 4. 通过Manager层创建登记记录（支持6表结构）
        // Manager层会处理：
        // - 核心登记信息
        // - 生物识别信息（如果有）
        // - 终端信息（如果有）
        // - 附加信息（如果有）
        // - 自动生成访客码和登记编号
        // - 事务管理
        SelfServiceRegistrationEntity result = selfServiceRegistrationManager.createRegistration(registration);

        log.info("[自助登记服务] 自助登记创建成功: registrationId={}, registrationCode={}, visitorCode={}",
                result.getRegistrationId(),
                result.getRegistrationCode(),
                result.getVisitorCode());

        return result;
    }

    @Override
    public SelfServiceRegistrationEntity getRegistrationByVisitorCode(String visitorCode) {
        log.info("[自助登记服务] 查询登记记录: visitorCode={}", visitorCode);

        // 通过Manager层查询完整登记信息（支持6表JOIN）
        // Manager层会自动组装：
        // - 核心登记信息
        // - 生物识别信息（如果有）
        // - 审批信息（如果有）
        // - 访问记录信息（如果有）
        // - 终端信息（如果有）
        // - 附加信息（如果有）
        SelfServiceRegistrationEntity result = selfServiceRegistrationManager.getRegistrationByVisitorCode(visitorCode);

        if (result != null) {
            log.info("[自助登记服务] 登记记录查询成功: registrationId={}, visitorCode={}",
                    result.getRegistrationId(), visitorCode);
        } else {
            log.warn("[自助登记服务] 登记记录不存在: visitorCode={}", visitorCode);
        }

        return result;
    }

    @Override
    public SelfServiceRegistrationEntity getRegistrationByCode(String registrationCode) {
        log.info("[自助登记服务] 查询登记记录: registrationCode={}", registrationCode);
        return selfServiceRegistrationDao.selectByRegistrationCode(registrationCode);
    }

    @Override
    public List<SelfServiceRegistrationEntity> getPendingApprovals(Long intervieweeId, Integer limit) {
        log.info("[自助登记服务] 查询待审批记录: intervieweeId={}, limit={}", intervieweeId, limit);
        return selfServiceRegistrationDao.selectPendingApprovals(intervieweeId, limit);
    }

    @Override
    public SelfServiceRegistrationEntity approveRegistration(Long registrationId,
                                                           Long approverId,
                                                           String approverName,
                                                           Boolean approved,
                                                           String approvalComment) {
        log.info("[自助登记服务] 审批登记申请: registrationId={}, approver={}, approved={}",
                registrationId, approverName, approved);

        return selfServiceRegistrationManager.approveRegistration(
                registrationId, approverId, approverName, approved, approvalComment);
    }

    @Override
    public SelfServiceRegistrationEntity checkIn(String visitorCode) {
        log.info("[自助登记服务] 访客签到: visitorCode={}", visitorCode);
        return selfServiceRegistrationManager.checkIn(visitorCode);
    }

    @Override
    public SelfServiceRegistrationEntity checkOut(String visitorCode) {
        log.info("[自助登记服务] 访客签离: visitorCode={}", visitorCode);
        return selfServiceRegistrationManager.checkOut(visitorCode);
    }

    @Override
    public List<SelfServiceRegistrationEntity> getActiveVisitors() {
        log.info("[自助登记服务] 查询在场访客");
        return selfServiceRegistrationDao.selectActiveVisitors();
    }

    @Override
    public List<SelfServiceRegistrationEntity> getOverdueVisitors() {
        log.info("[自助登记服务] 查询超时访客");
        return selfServiceRegistrationManager.getOverdueVisitors();
    }

    @Override
    public PageResult<SelfServiceRegistrationEntity> queryPage(Integer pageNum,
                                                              Integer pageSize,
                                                              String visitorName,
                                                              String phone,
                                                              Integer status,
                                                              LocalDate visitDateStart,
                                                              LocalDate visitDateEnd) {
        log.info("[自助登记服务] 分页查询登记记录: pageNum={}, pageSize={}, visitorName={}, phone={}, status={}",
                pageNum, pageSize, visitorName, maskPhone(phone), status);

        LambdaQueryWrapper<SelfServiceRegistrationEntity> queryWrapper =
                new LambdaQueryWrapper<SelfServiceRegistrationEntity>()
                        .orderByDesc(SelfServiceRegistrationEntity::getVisitDate)
                        .orderByDesc(SelfServiceRegistrationEntity::getCreateTime);

        // 访客姓名模糊查询
        if (visitorName != null && !visitorName.isEmpty()) {
            queryWrapper.like(SelfServiceRegistrationEntity::getVisitorName, visitorName);
        }

        // 手机号精确查询
        if (phone != null && !phone.isEmpty()) {
            queryWrapper.eq(SelfServiceRegistrationEntity::getPhone, phone);
        }

        // 状态查询
        if (status != null) {
            queryWrapper.eq(SelfServiceRegistrationEntity::getRegistrationStatus, status);
        }

        // 访问日期范围查询
        if (visitDateStart != null) {
            queryWrapper.ge(SelfServiceRegistrationEntity::getVisitDate, visitDateStart);
        }
        if (visitDateEnd != null) {
            queryWrapper.le(SelfServiceRegistrationEntity::getVisitDate, visitDateEnd);
        }

        // 分页查询
        Page<SelfServiceRegistrationEntity> page = new Page<>(pageNum, pageSize);
        selfServiceRegistrationDao.selectPage(page, queryWrapper);

        return PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<SelfServiceRegistrationEntity> getByInterviewee(Long intervieweeId,
                                                                LocalDate startDate,
                                                                LocalDate endDate) {
        log.info("[自助登记服务] 查询被访人登记记录: intervieweeId={}, startDate={}, endDate={}",
                intervieweeId, startDate, endDate);
        return selfServiceRegistrationDao.selectByInterviewee(intervieweeId, startDate, endDate);
    }

    @Override
    public List<SelfServiceRegistrationEntity> getByTerminal(String terminalId, Integer limit) {
        log.info("[自助登记服务] 查询终端登记记录: terminalId={}, limit={}", terminalId, limit);
        return selfServiceRegistrationDao.selectByTerminalId(terminalId, limit);
    }

    @Override
    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[自助登记服务] 查询统计信息: startDate={}, endDate={}", startDate, endDate);
        return selfServiceRegistrationManager.getRegistrationStatistics(startDate, endDate);
    }

    @Override
    public SelfServiceRegistrationEntity cancelRegistration(Long registrationId, String cancelReason) {
        log.info("[自助登记服务] 取消登记: registrationId={}, cancelReason={}", registrationId, cancelReason);

        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("登记记录不存在: " + registrationId);
        }

        // 检查状态：只能取消待审批或已审批但未签到的记录
        if (registration.getRegistrationStatus() == 3 || registration.getRegistrationStatus() == 4) {
            throw new RuntimeException("访客已签到或已完成，无法取消");
        }

        // 更新状态为已取消
        registration.setRegistrationStatus(5);
        registration.setRemark(cancelReason);

        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助登记服务] 登记取消成功: registrationId={}", registrationId);

        return registration;
    }

    @Override
    public SelfServiceRegistrationEntity updateCardPrintStatus(Long registrationId,
                                                              Integer printStatus,
                                                              String visitorCard) {
        log.info("[自助登记服务] 更新访客卡打印状态: registrationId={}, printStatus={}, visitorCard={}",
                registrationId, printStatus, visitorCard);

        SelfServiceRegistrationEntity registration = selfServiceRegistrationDao.selectById(registrationId);
        if (registration == null) {
            throw new RuntimeException("登记记录不存在: " + registrationId);
        }

        registration.setCardPrintStatus(printStatus);
        if (visitorCard != null && !visitorCard.isEmpty()) {
            registration.setVisitorCard(visitorCard);
        }

        selfServiceRegistrationDao.updateById(registration);

        log.info("[自助登记服务] 打印状态更新成功: registrationId={}, printStatus={}",
                registrationId, printStatus);

        return registration;
    }

    @Override
    public Map<String, Object> batchApprove(List<Long> registrationIds,
                                            Long approverId,
                                            String approverName,
                                            Boolean approved,
                                            String approvalComment) {
        log.info("[自助登记服务] 批量审批: count={}, approver={}, approved={}",
                registrationIds.size(), approverName, approved);

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long registrationId : registrationIds) {
            try {
                selfServiceRegistrationManager.approveRegistration(
                        registrationId, approverId, approverName, approved, approvalComment);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add("登记ID " + registrationId + " 审批失败: " + e.getMessage());
                log.error("[自助登记服务] 批量审批失败: registrationId={}, error={}",
                        registrationId, e.getMessage(), e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", registrationIds.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);

        log.info("[自助登记服务] 批量审批完成: totalCount={}, successCount={}, failureCount={}",
                registrationIds.size(), successCount, failureCount);

        return result;
    }

    /**
     * 身份证号脱敏
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return "***";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

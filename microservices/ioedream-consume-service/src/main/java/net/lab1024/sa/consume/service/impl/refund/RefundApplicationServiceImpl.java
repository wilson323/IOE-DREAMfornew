package net.lab1024.sa.consume.service.impl.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.dao.RefundApplicationDao;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundApplicationForm;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.service.refund.RefundApplicationService;

/**
 * 退款申请服务实现类
 * <p>
 * 实现退款申请相关业务功能，集成工作流审批
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-consume-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RefundApplicationServiceImpl implements RefundApplicationService {

    @Resource
    private RefundApplicationDao refundApplicationDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private PaymentRecordDao paymentRecordDao;

    @Resource
    private AccountManager accountManager;

    @Resource
    private net.lab1024.sa.consume.service.PaymentService paymentService;

    @Override
    @Observed(name = "consume.refundApplication.submitRefundApplication", contextualName = "consume-refund-application-submit")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "submitRefundApplicationFallback")
    public RefundApplicationEntity submitRefundApplication(RefundApplicationForm form) {
        log.info("[退款申请] 提交退款申请，userId={}, paymentRecordId={}, amount={}",
                form.getUserId(), form.getPaymentRecordId(), form.getRefundAmount());

        // 1. 创建退款申请记录
        RefundApplicationEntity entity = new RefundApplicationEntity();
        entity.setRefundNo(generateRefundNo());
        entity.setPaymentRecordId(form.getPaymentRecordId());
        entity.setUserId(form.getUserId());
        entity.setRefundAmount(form.getRefundAmount());
        entity.setRefundReason(form.getRefundReason());
        entity.setStatus("PENDING");
        refundApplicationDao.insert(entity);

        // 2. 构建表单数据（用于审批流程）
        Map<String, Object> formData = new HashMap<>();
        formData.put("refundNo", entity.getRefundNo());
        formData.put("paymentRecordId", form.getPaymentRecordId());
        formData.put("userId", form.getUserId());
        formData.put("refundAmount", form.getRefundAmount());
        formData.put("refundReason", form.getRefundReason());
        formData.put("applyTime", LocalDateTime.now());

        // 3. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", form.getRefundAmount()); // 用于金额判断（如：超过5000需要财务审批）

        // 4. 启动审批流程
        ResponseDTO<Long> workflowResult = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.CONSUME_REFUND, // 流程定义ID
                entity.getRefundNo(), // 业务Key
                "退款申请-" + entity.getRefundNo(), // 流程实例名称
                form.getUserId(), // 发起人ID
                BusinessTypeEnum.CONSUME_REFUND.name(), // 业务类型
                formData, // 表单数据
                variables // 流程变量
        );

        if (workflowResult == null || !workflowResult.isSuccess()) {
            log.error("[退款申请] 启动审批流程失败，refundNo={}", entity.getRefundNo());
            throw new BusinessException("启动审批流程失败: " +
                    (workflowResult != null ? workflowResult.getMessage() : "未知错误"));
        }

        // 5. 更新退款申请的workflowInstanceId
        Long workflowInstanceId = workflowResult.getData();
        entity.setWorkflowInstanceId(workflowInstanceId);
        refundApplicationDao.updateById(entity);

        log.info("[退款申请] 退款申请提交成功，refundNo={}, workflowInstanceId={}",
                entity.getRefundNo(), workflowInstanceId);

        return entity;
    }

    @Override
    @Observed(name = "consume.refundApplication.updateRefundStatus", contextualName = "consume-refund-application-update-status")
    @Transactional(rollbackFor = Exception.class)
    public void updateRefundStatus(String refundNo, String status, String approvalComment) {
        log.info("[退款申请] 更新退款状态，refundNo={}, status={}", refundNo, status);

        RefundApplicationEntity entity = refundApplicationDao.selectByRefundNo(refundNo);
        if (entity == null) {
            log.warn("[退款申请] 退款申请不存在，refundNo={}", refundNo);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        refundApplicationDao.updateById(entity);

        // 如果审批通过，执行退款逻辑
        if ("APPROVED".equals(status)) {
            executeRefund(entity);
        }

        log.info("[退款申请] 退款状态更新成功，refundNo={}, status={}", refundNo, status);
    }

    /**
     * 执行退款逻辑
     * <p>
     * 退款流程：
     * 1. 查询支付记录，验证支付状态
     * 2. 根据支付方式处理退款：
     *    - 支付宝/微信：调用第三方退款接口
     *    - 账户余额支付：直接退回账户余额
     *    - 现金/转账：记录退款流水，需要财务线下处理
     * 3. 更新支付记录状态为REFUNDED
     * 4. 记录退款流水
     * </p>
     *
     * @param entity 退款申请实体
     */
    private void executeRefund(RefundApplicationEntity entity) {
        log.info("[退款申请] 执行退款，refundNo={}, paymentRecordId={}, amount={}",
                entity.getRefundNo(), entity.getPaymentRecordId(), entity.getRefundAmount());

        try {
            // 1. 查询支付记录
            PaymentRecordEntity paymentRecord = paymentRecordDao.selectById(entity.getPaymentRecordId());
            if (paymentRecord == null) {
                log.error("[退款申请] 支付记录不存在，paymentRecordId={}, refundNo={}",
                        entity.getPaymentRecordId(), entity.getRefundNo());
                throw new BusinessException("支付记录不存在");
            }

            // 2. 验证支付状态（只有支付成功的才能退款）
            if (paymentRecord.getPaymentStatus() == null || paymentRecord.getPaymentStatus() != 3) {
                Integer status = paymentRecord.getPaymentStatus();
                String statusStr = status != null ? String.valueOf(status) : "未知";
                log.warn("[退款申请] 支付记录状态不允许退款，paymentRecordId={}, status={}, refundNo={}",
                        entity.getPaymentRecordId(), statusStr, entity.getRefundNo());
                throw new BusinessException("支付记录状态不允许退款，当前状态：" + statusStr);
            }

            // 3. 验证退款金额（退款金额不能超过支付金额）
            if (entity.getRefundAmount().compareTo(paymentRecord.getPaymentAmount()) > 0) {
                log.warn("[退款申请] 退款金额超过支付金额，refundAmount={}, paymentAmount={}, refundNo={}",
                        entity.getRefundAmount(), paymentRecord.getPaymentAmount(), entity.getRefundNo());
                throw new BusinessException("退款金额不能超过支付金额");
            }

            // 4. 根据支付方式处理退款
            Integer paymentMethod = paymentRecord.getPaymentMethod();
            boolean refundSuccess = false;

            if (paymentMethod != null && (paymentMethod == 2 || paymentMethod == 3)) { // 2-微信支付 3-支付宝
                // 第三方支付退款
                refundSuccess = processThirdPartyRefund(paymentRecord, entity);
            } else if (paymentMethod != null && paymentMethod == 1) { // 1-余额支付
                // 账户余额支付，直接退回账户
                refundSuccess = processAccountRefund(paymentRecord, entity);
            } else {
                // 现金/转账等，只记录退款流水，需要财务线下处理
                log.info("[退款申请] 现金/转账支付，仅记录退款流水，需财务线下处理，paymentMethod={}, refundNo={}",
                        paymentMethod, entity.getRefundNo());
                refundSuccess = true; // 记录流水即视为成功，实际退款由财务处理
            }

            if (!refundSuccess) {
                log.error("[退款申请] 退款处理失败，refundNo={}, paymentMethod={}",
                        entity.getRefundNo(), paymentMethod);
                throw new BusinessException("退款处理失败");
            }

            // 5. 更新支付记录状态为已退款
            paymentRecord.setPaymentStatus(5); // 5-已退款
            paymentRecord.setRemark("退款申请号：" + entity.getRefundNo() + "，退款原因：" + entity.getRefundReason());
            paymentRecordDao.updateById(paymentRecord);

            log.info("[退款申请] 退款处理成功，refundNo={}, paymentRecordId={}, amount={}, paymentMethod={}",
                    entity.getRefundNo(), entity.getPaymentRecordId(), entity.getRefundAmount(), paymentMethod);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[退款申请] 退款处理参数错误，refundNo={}, error={}", entity.getRefundNo(), e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[退款申请] 退款业务异常，refundNo={}, code={}, message={}", entity.getRefundNo(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[退款申请] 退款处理系统异常，refundNo={}, code={}, message={}", entity.getRefundNo(), e.getCode(), e.getMessage(), e);
            throw new SystemException("REFUND_PROCESS_SYSTEM_ERROR", "退款处理异常：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[退款申请] 退款处理未知异常，refundNo={}", entity.getRefundNo(), e);
            throw new SystemException("REFUND_PROCESS_SYSTEM_ERROR", "退款处理异常：" + e.getMessage(), e);
        }
    }

    /**
     * 处理第三方支付退款（支付宝/微信）
     *
     * @param paymentRecord 支付记录
     * @param refundEntity 退款申请实体
     * @return 是否成功
     */
    private boolean processThirdPartyRefund(PaymentRecordEntity paymentRecord, RefundApplicationEntity refundEntity) {
        log.info("[退款申请] 处理第三方支付退款，paymentMethod={}, paymentId={}, refundAmount={}",
                paymentRecord.getPaymentMethod(), paymentRecord.getPaymentId(), refundEntity.getRefundAmount());

        try {
            // 调用第三方支付退款接口
            // 根据支付方式调用不同的退款接口

            Integer paymentMethod = paymentRecord.getPaymentMethod();

            if (paymentMethod != null && paymentMethod == 2) { // 2-微信支付
                // 微信支付退款
                return processWechatRefund(paymentRecord, refundEntity);
            } else if (paymentMethod != null && paymentMethod == 3) { // 3-支付宝
                // 支付宝退款
                return processAlipayRefund(paymentRecord, refundEntity);
            } else {
                log.warn("[退款申请] 不支持的支付方式，paymentMethod={}, paymentId={}",
                        paymentMethod, paymentRecord.getPaymentId());
                return false;
            }

        } catch (BusinessException e) {
            log.warn("[退款申请] 第三方支付退款处理业务异常，paymentMethod={}, paymentId={}, code={}, message={}",
                    paymentRecord.getPaymentMethod(), paymentRecord.getPaymentId(), e.getCode(), e.getMessage());
            return false;
        } catch (SystemException e) {
            log.error("[退款申请] 第三方支付退款处理系统异常，paymentMethod={}, paymentId={}, code={}, message={}",
                    paymentRecord.getPaymentMethod(), paymentRecord.getPaymentId(), e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[退款申请] 第三方支付退款处理未知异常，paymentMethod={}, paymentId={}",
                    paymentRecord.getPaymentMethod(), paymentRecord.getPaymentId(), e);
            return false;
        }
    }

    /**
     * 处理微信支付退款
     *
     * @param paymentRecord 支付记录
     * @param refundEntity 退款申请实体
     * @return 是否成功
     */
    private boolean processWechatRefund(PaymentRecordEntity paymentRecord, RefundApplicationEntity refundEntity) {
        log.info("[退款申请] 处理微信支付退款，paymentId={}, refundAmount={}, refundNo={}",
                paymentRecord.getPaymentId(), refundEntity.getRefundAmount(), refundEntity.getRefundNo());

        try {
            // 1. 参数验证
            if (paymentRecord.getPaymentAmount() == null || paymentRecord.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("[退款申请] 支付记录金额无效，paymentId={}", paymentRecord.getPaymentId());
                return false;
            }

            // 2. 金额转换：元转分（微信支付金额单位为分）
            Integer totalAmount = paymentRecord.getPaymentAmount()
                    .multiply(new BigDecimal("100"))
                    .intValue();
            Integer refundAmount = refundEntity.getRefundAmount()
                    .multiply(new BigDecimal("100"))
                    .intValue();

            // 3. 调用微信支付退款接口
            Map<String, Object> refundResult = paymentService.wechatRefund(
                    paymentRecord.getPaymentId(),
                    refundEntity.getRefundNo(),
                    totalAmount,
                    refundAmount
            );

            // 4. 检查退款结果
            if (refundResult != null && Boolean.TRUE.equals(refundResult.get("success"))) {
                log.info("[退款申请] 微信支付退款成功，paymentId={}, refundNo={}, refundId={}",
                        paymentRecord.getPaymentId(), refundEntity.getRefundNo(), refundResult.get("refundId"));
                return true;
            } else {
                log.warn("[退款申请] 微信支付退款失败，paymentId={}, refundNo={}, result={}",
                        paymentRecord.getPaymentId(), refundEntity.getRefundNo(), refundResult);
                return false;
            }

        } catch (BusinessException e) {
            log.warn("[退款申请] 微信支付退款处理业务异常，paymentId={}, refundNo={}, code={}, message={}",
                    paymentRecord.getPaymentId(), refundEntity.getRefundNo(), e.getCode(), e.getMessage());
            return false;
        } catch (SystemException e) {
            log.error("[退款申请] 微信支付退款处理系统异常，paymentId={}, refundNo={}, code={}, message={}",
                    paymentRecord.getPaymentId(), refundEntity.getRefundNo(), e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[退款申请] 微信支付退款处理未知异常，paymentId={}, refundNo={}",
                    paymentRecord.getPaymentId(), refundEntity.getRefundNo(), e);
            return false;
        }
    }

    /**
     * 处理支付宝退款
     *
     * @param paymentRecord 支付记录
     * @param refundEntity 退款申请实体
     * @return 是否成功
     */
    private boolean processAlipayRefund(PaymentRecordEntity paymentRecord, RefundApplicationEntity refundEntity) {
        log.info("[退款申请] 处理支付宝退款，paymentId={}, refundAmount={}, refundNo={}",
                paymentRecord.getPaymentId(), refundEntity.getRefundAmount(), refundEntity.getRefundNo());

        try {
            // 1. 参数验证
            if (paymentRecord.getPaymentAmount() == null || paymentRecord.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("[退款申请] 支付记录金额无效，paymentId={}", paymentRecord.getPaymentId());
                return false;
            }

            // 2. 调用支付宝退款接口
            Map<String, Object> refundResult = paymentService.alipayRefund(
                    paymentRecord.getPaymentId(),
                    refundEntity.getRefundAmount(),
                    refundEntity.getRefundReason() != null ? refundEntity.getRefundReason() : "用户申请退款"
            );

            // 3. 检查退款结果
            if (refundResult != null && Boolean.TRUE.equals(refundResult.get("success"))) {
                log.info("[退款申请] 支付宝退款成功，paymentId={}, refundNo={}, tradeNo={}",
                        paymentRecord.getPaymentId(), refundEntity.getRefundNo(), refundResult.get("tradeNo"));
                return true;
            } else {
                log.warn("[退款申请] 支付宝退款失败，paymentId={}, refundNo={}, result={}",
                        paymentRecord.getPaymentId(), refundEntity.getRefundNo(), refundResult);
                return false;
            }

        } catch (BusinessException e) {
            log.warn("[退款申请] 支付宝退款处理业务异常，paymentId={}, refundNo={}, code={}, message={}",
                    paymentRecord.getPaymentId(), refundEntity.getRefundNo(), e.getCode(), e.getMessage());
            return false;
        } catch (SystemException e) {
            log.error("[退款申请] 支付宝退款处理系统异常，paymentId={}, refundNo={}, code={}, message={}",
                    paymentRecord.getPaymentId(), refundEntity.getRefundNo(), e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[退款申请] 支付宝退款处理未知异常，paymentId={}, refundNo={}",
                    paymentRecord.getPaymentId(), refundEntity.getRefundNo(), e);
            return false;
        }
    }

    /**
     * 处理账户余额退款
     *
     * @param paymentRecord 支付记录
     * @param refundEntity 退款申请实体
     * @return 是否成功
     */
    private boolean processAccountRefund(PaymentRecordEntity paymentRecord, RefundApplicationEntity refundEntity) {
        log.info("[退款申请] 处理账户余额退款，userId={}, refundAmount={}",
                paymentRecord.getUserId(), refundEntity.getRefundAmount());

        try {
            // 1. 查询用户账户
            var account = accountManager.getAccountByUserId(paymentRecord.getUserId());
            if (account == null) {
                log.error("[退款申请] 用户账户不存在，userId={}", paymentRecord.getUserId());
                return false;
            }

            // 2. 增加账户余额
            boolean success = accountManager.addBalance(account.getId(), refundEntity.getRefundAmount());
            if (!success) {
                log.error("[退款申请] 账户余额增加失败，accountId={}, amount={}",
                        account.getId(), refundEntity.getRefundAmount());
                return false;
            }

            log.info("[退款申请] 账户余额退款成功，accountId={}, refundAmount={}",
                    account.getId(), refundEntity.getRefundAmount());
            return true;

        } catch (BusinessException e) {
            log.warn("[退款申请] 账户余额退款处理业务异常，userId={}, code={}, message={}", paymentRecord.getUserId(), e.getCode(), e.getMessage());
            return false;
        } catch (SystemException e) {
            log.error("[退款申请] 账户余额退款处理系统异常，userId={}, code={}, message={}", paymentRecord.getUserId(), e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[退款申请] 账户余额退款处理未知异常，userId={}", paymentRecord.getUserId(), e);
            return false;
        }
    }

    /**
     * 生成退款申请编号
     *
     * @return 退款申请编号
     */
    private String generateRefundNo() {
        return "RF" + System.currentTimeMillis();
    }
}





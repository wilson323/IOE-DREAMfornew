package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.dao.PaymentRefundRecordDao;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.domain.form.RefundApplyForm;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.entity.PaymentRefundRecordEntity;
import net.lab1024.sa.consume.service.AccountService;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.consume.service.PaymentService;

/**
 * 支付服务实现类
 * <p>
 * 根据chonggou.txt和业务模块文档要求实现
 * 严格遵循CLAUDE.md规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PaymentServiceImpl implements PaymentService {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private PaymentRecordDao paymentRecordDao;

    @Resource
    private PaymentRefundRecordDao paymentRefundRecordDao;

    @Resource
    private AccountService accountService;

    /**
     * 处理支付（旧方法，保留向后兼容）
     *
     * @param accountNo     账户编号
     * @param amount        支付金额
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    @Override
    public Boolean processPayment(String accountNo, BigDecimal amount, String paymentMethod) {
        log.info("[支付服务] 处理支付（旧方法），accountNo={}, amount={}, method={}", accountNo, amount, paymentMethod);

        // 转换为新方法调用
        PaymentProcessForm form = new PaymentProcessForm();
        form.setAccountNo(accountNo);
        form.setPaymentAmount(amount);
        form.setPaymentMethod(convertPaymentMethodToInteger(paymentMethod));

        Map<String, Object> result = processPayment(form);
        return result != null && "SUCCESS".equals(result.get("status"));
    }

    /**
     * 处理支付（表单方式，返回详细结果）
     * <p>
     * 根据chonggou.txt要求：返回Map<String, Object>
     * </p>
     *
     * @param form 支付处理表单
     * @return 支付结果Map
     */
    @Override
    public Map<String, Object> processPayment(PaymentProcessForm form) {
        log.info("[支付服务] 处理支付，userId={}, accountId={}, amount={}, method={}",
                form.getUserId(), form.getAccountId(), form.getPaymentAmount(), form.getPaymentMethod());

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("status", "FAILED");

        try {
            // 参数验证
            if (form.getAccountId() == null) {
                result.put("message", "账户ID不能为空");
                return result;
            }
            if (form.getPaymentAmount() == null || form.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
                result.put("message", "支付金额必须大于0");
                return result;
            }

            // 转换为消费请求DTO
            ConsumeRequestDTO consumeRequest = new ConsumeRequestDTO();
            consumeRequest.setUserId(form.getUserId());
            consumeRequest.setAccountId(form.getAccountId());
            consumeRequest.setAmount(form.getPaymentAmount());
            consumeRequest.setAreaId(form.getAreaId());
            consumeRequest.setDeviceId(parseDeviceId(form.getDeviceId()));
            consumeRequest.setConsumeType("PAYMENT");

            // 调用消费服务处理支付
            net.lab1024.sa.common.dto.ResponseDTO<ConsumeTransactionResultVO> response = consumeService
                    .consume(consumeRequest);

            if (response != null && response.getOk()) {
                ConsumeTransactionResultVO transactionResult = response.getData();
                result.put("success", true);
                result.put("status", "SUCCESS");
                result.put("transactionNo", transactionResult.getTransactionNo());
                result.put("paymentId", transactionResult.getTransactionNo());
                result.put("amount", transactionResult.getAmount());
                result.put("balanceAfter", transactionResult.getBalanceAfter());
                result.put("message", "支付成功");
            } else {
                result.put("message", response != null ? response.getMessage() : "支付处理失败");
            }

        } catch (Exception e) {
            log.error("[支付服务] 支付处理异常", e);
            result.put("message", "支付处理异常：" + e.getMessage());
        }

        return result;
    }

    /**
     * 退款处理
     *
     * @param paymentId 支付ID
     * @param reason    退款原因
     * @return 是否成功
     */
    @Override
    public Boolean refundPayment(Long paymentId, String reason) {
        log.info("[支付服务] 退款处理，paymentId={}, reason={}", paymentId, reason);
        // TODO: 实现退款逻辑
        return false;
    }

    /**
     * 申请退款
     * <p>
     * 根据chonggou.txt要求修复：返回Map<String, Object>而非Boolean
     * </p>
     *
     * @param form 退款申请表单
     * @return 退款结果Map
     */
    @Override
    public Map<String, Object> applyRefund(RefundApplyForm form) {
        log.info("[支付服务] 申请退款，form={}", form);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("status", "FAILED");
        result.put("message", "退款申请功能待实现");
        // TODO: 实现退款申请逻辑
        return result;
    }

    /**
     * 审核退款
     * <p>
     * 根据chonggou.txt要求和业务模块文档06-充值退款流程实现
     * 审核状态：1-通过 2-拒绝
     * </p>
     *
     * @param refundNo 退款单号
     * @param status   审核状态（1-通过 2-拒绝）
     * @param comment  审核意见
     * @return 审核结果Map
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> auditRefund(String refundNo, Integer status, String comment) {
        log.info("[支付服务] 审核退款，refundNo={}, status={}, comment={}", refundNo, status, comment);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("status", "FAILED");

        try {
            // 1. 参数验证
            if (refundNo == null || refundNo.trim().isEmpty()) {
                result.put("message", "退款单号不能为空");
                return result;
            }
            if (status == null || (status != 1 && status != 2)) {
                result.put("message", "审核状态无效，必须为1（通过）或2（拒绝）");
                return result;
            }

            // 2. 查询退款记录
            PaymentRefundRecordEntity refundRecord = paymentRefundRecordDao.selectByRefundNo(refundNo);
            if (refundRecord == null) {
                log.warn("[支付服务] 退款记录不存在，refundNo={}", refundNo);
                result.put("message", "退款记录不存在");
                return result;
            }

            // 3. 验证退款状态（只有待审核状态才能审核）
            // 退款状态：1-待审核 2-已通过 3-已拒绝 4-处理中 5-已完成 6-已取消
            if (refundRecord.getRefundStatus() == null || refundRecord.getRefundStatus() != 1) {
                log.warn("[支付服务] 退款记录状态不允许审核，refundNo={}, currentStatus={}", refundNo, refundRecord.getRefundStatus());
                result.put("message", "退款记录状态不允许审核，当前状态：" + refundRecord.getRefundStatus());
                return result;
            }

            // 4. 更新退款记录审核信息
            refundRecord.setRefundStatus(status == 1 ? 2 : 3); // 2-已通过 3-已拒绝
            refundRecord.setAuditTime(LocalDateTime.now());
            refundRecord.setAuditComment(comment);

            // TODO: 从当前登录用户上下文获取审核人ID
            // refundRecord.setAuditorId(getCurrentUserId());

            int updateCount = paymentRefundRecordDao.updateById(refundRecord);
            if (updateCount <= 0) {
                log.error("[支付服务] 更新退款审核状态失败，refundNo={}", refundNo);
                result.put("message", "更新退款审核状态失败");
                return result;
            }

            // 5. 如果审核通过，可以自动执行退款（可选，根据业务需求）
            if (status == 1) {
                log.info("[支付服务] 退款审核通过，可以执行退款，refundNo={}", refundNo);
                // 这里可以选择自动执行退款，或者返回结果让调用方决定是否执行
            }

            result.put("success", true);
            result.put("status", status == 1 ? "APPROVED" : "REJECTED");
            result.put("message", status == 1 ? "退款审核通过" : "退款审核拒绝");
            result.put("refundNo", refundNo);
            result.put("refundStatus", refundRecord.getRefundStatus());

            log.info("[支付服务] 退款审核成功，refundNo={}, status={}", refundNo, status);
            return result;

        } catch (BusinessException e) {
            log.warn("[支付服务] 退款审核业务异常，refundNo={}, error={}", refundNo, e.getMessage());
            result.put("message", "退款审核失败：" + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[支付服务] 退款审核系统异常，refundNo={}", refundNo, e);
            result.put("message", "退款审核系统异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 执行退款
     * <p>
     * 根据chonggou.txt要求和业务模块文档06-充值退款流程实现
     * 退款流程：
     * 1. 查询退款记录，验证审核状态
     * 2. 查询支付记录，验证支付状态
     * 3. 根据支付方式处理退款：
     * - 余额支付：直接退回账户余额
     * - 第三方支付：调用第三方退款接口（待实现）
     * 4. 更新支付记录和退款记录状态
     * </p>
     *
     * @param refundNo 退款单号
     * @return 执行结果Map
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeRefund(String refundNo) {
        log.info("[支付服务] 执行退款，refundNo={}", refundNo);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("status", "FAILED");

        try {
            // 1. 参数验证
            if (refundNo == null || refundNo.trim().isEmpty()) {
                result.put("message", "退款单号不能为空");
                return result;
            }

            // 2. 查询退款记录
            PaymentRefundRecordEntity refundRecord = paymentRefundRecordDao.selectByRefundNo(refundNo);
            if (refundRecord == null) {
                log.warn("[支付服务] 退款记录不存在，refundNo={}", refundNo);
                result.put("message", "退款记录不存在");
                return result;
            }

            // 3. 验证退款状态（只有已审核通过的才能执行）
            // 退款状态：1-待审核 2-已通过 3-已拒绝 4-处理中 5-已完成 6-已取消
            if (refundRecord.getRefundStatus() == null || refundRecord.getRefundStatus() != 2) {
                log.warn("[支付服务] 退款记录状态不允许执行，refundNo={}, currentStatus={}", refundNo, refundRecord.getRefundStatus());
                result.put("message", "退款记录状态不允许执行，当前状态：" + refundRecord.getRefundStatus());
                return result;
            }

            // 4. 查询支付记录
            PaymentRecordEntity paymentRecord = null;
            if (refundRecord.getPaymentId() != null) {
                paymentRecord = paymentRecordDao.selectById(refundRecord.getPaymentId());
                if (paymentRecord == null) {
                    log.warn("[支付服务] 支付记录不存在，paymentId={}, refundNo={}", refundRecord.getPaymentId(), refundNo);
                    result.put("message", "关联的支付记录不存在");
                    return result;
                }

                // 验证支付状态（只有支付成功的才能退款）
                if (paymentRecord.getPaymentStatus() == null || paymentRecord.getPaymentStatus() != 3) {
                    log.warn("[支付服务] 支付记录状态不允许退款，paymentId={}, status={}, refundNo={}",
                            refundRecord.getPaymentId(), paymentRecord.getPaymentStatus(), refundNo);
                    result.put("message", "支付记录状态不允许退款，当前状态：" + paymentRecord.getPaymentStatus());
                    return result;
                }
            }

            // 5. 验证退款金额
            BigDecimal refundAmount = refundRecord.getRefundAmount();
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[支付服务] 退款金额无效，refundAmount={}, refundNo={}", refundAmount, refundNo);
                result.put("message", "退款金额无效");
                return result;
            }

            if (paymentRecord != null && refundAmount.compareTo(paymentRecord.getPaymentAmount()) > 0) {
                log.warn("[支付服务] 退款金额超过支付金额，refundAmount={}, paymentAmount={}, refundNo={}",
                        refundAmount, paymentRecord.getPaymentAmount(), refundNo);
                result.put("message", "退款金额不能超过支付金额");
                return result;
            }

            // 6. 根据支付方式处理退款
            boolean refundSuccess = false;
            // PaymentRecordEntity.paymentMethod 当前为String类型，这里统一转换为Integer枚举值处理
            Integer paymentMethod = paymentRecord != null
                    ? convertPaymentMethodToInteger(paymentRecord.getPaymentMethod())
                    : null;

            if (paymentMethod != null && paymentMethod == 1) {
                // 余额支付，直接退回账户余额
                refundSuccess = processAccountBalanceRefund(refundRecord, paymentRecord);
            } else if (paymentMethod != null && (paymentMethod == 2 || paymentMethod == 3)) {
                // 第三方支付（微信/支付宝），调用第三方退款接口
                // TODO: 实现第三方支付退款
                log.warn("[支付服务] 第三方支付退款待实现，paymentMethod={}, refundNo={}", paymentMethod, refundNo);
                result.put("message", "第三方支付退款功能待实现");
                return result;
            } else {
                // 现金/转账等，只记录退款流水，需要财务线下处理
                log.info("[支付服务] 现金/转账支付，仅记录退款流水，需财务线下处理，paymentMethod={}, refundNo={}",
                        paymentMethod, refundNo);
                refundSuccess = true; // 记录流水即视为成功
            }

            if (!refundSuccess) {
                log.error("[支付服务] 退款处理失败，refundNo={}, paymentMethod={}", refundNo, paymentMethod);
                result.put("message", "退款处理失败");
                return result;
            }

            // 7. 更新退款记录状态为已完成
            refundRecord.setRefundStatus(5); // 5-已完成
            refundRecord.setProcessTime(LocalDateTime.now());
            refundRecord.setActualRefundAmount(refundAmount);
            // TODO: 从当前登录用户上下文获取处理人ID
            // refundRecord.setProcessorId(getCurrentUserId());

            int updateCount = paymentRefundRecordDao.updateById(refundRecord);
            if (updateCount <= 0) {
                log.error("[支付服务] 更新退款记录状态失败，refundNo={}", refundNo);
                result.put("message", "更新退款记录状态失败");
                return result;
            }

            // 8. 更新支付记录状态为已退款
            if (paymentRecord != null) {
                paymentRecord.setPaymentStatus(5); // 5-已退款
                paymentRecord.setRefundAmount(refundAmount);
                paymentRecord.setRemark("退款单号：" + refundNo);
                paymentRecordDao.updateById(paymentRecord);
            }

            result.put("success", true);
            result.put("status", "SUCCESS");
            result.put("message", "退款执行成功");
            result.put("refundNo", refundNo);
            result.put("refundAmount", refundAmount);
            result.put("refundStatus", refundRecord.getRefundStatus());

            log.info("[支付服务] 退款执行成功，refundNo={}, refundAmount={}, paymentMethod={}", refundNo, refundAmount,
                    paymentMethod);
            return result;

        } catch (BusinessException e) {
            log.warn("[支付服务] 退款执行业务异常，refundNo={}, error={}", refundNo, e.getMessage());
            result.put("message", "退款执行失败：" + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[支付服务] 退款执行系统异常，refundNo={}", refundNo, e);
            result.put("message", "退款执行系统异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 处理账户余额退款
     * <p>
     * 将退款金额退回账户余额
     * </p>
     *
     * @param refundRecord  退款记录
     * @param paymentRecord 支付记录
     * @return 是否成功
     */
    private boolean processAccountBalanceRefund(PaymentRefundRecordEntity refundRecord,
            PaymentRecordEntity paymentRecord) {
        log.info("[支付服务] 处理账户余额退款，refundNo={}, userId={}, refundAmount={}",
                refundRecord.getRefundNo(), refundRecord.getUserId(), refundRecord.getRefundAmount());

        try {
            // 获取用户ID（优先使用退款记录中的用户ID，否则使用支付记录中的用户ID）
            Long userId = refundRecord.getUserId();
            if (userId == null && paymentRecord != null) {
                userId = paymentRecord.getUserId();
            }

            if (userId == null) {
                log.error("[支付服务] 无法获取用户ID，refundNo={}", refundRecord.getRefundNo());
                return false;
            }

            // 通过用户ID查询账户
            AccountEntity account;
            try {
                account = accountService.getByUserId(userId);
                if (account == null) {
                    log.error("[支付服务] 账户不存在，userId={}, refundNo={}", userId, refundRecord.getRefundNo());
                    return false;
                }
            } catch (BusinessException e) {
                log.error("[支付服务] 查询账户失败，userId={}, refundNo={}, error={}", userId, refundRecord.getRefundNo(),
                        e.getMessage());
                return false;
            }

            // 增加账户余额
            boolean success = accountService.addBalance(account.getAccountId(), refundRecord.getRefundAmount(),
                    "退款：" + refundRecord.getRefundNo());
            if (!success) {
                log.error("[支付服务] 账户余额增加失败，accountId={}, amount={}, refundNo={}",
                        account.getAccountId(), refundRecord.getRefundAmount(), refundRecord.getRefundNo());
                return false;
            }

            log.info("[支付服务] 账户余额退款成功，accountId={}, refundAmount={}, refundNo={}",
                    account.getAccountId(), refundRecord.getRefundAmount(), refundRecord.getRefundNo());
            return true;

        } catch (Exception e) {
            log.error("[支付服务] 账户余额退款处理异常，refundNo={}", refundRecord.getRefundNo(), e);
            return false;
        }
    }

    /**
     * 获取支付记录
     * <p>
     * 根据chonggou.txt要求修复：返回PaymentRecordEntity而非ConsumeTransactionResultVO
     * 支持根据支付ID（Long类型主键）或支付编号（String格式）查询
     * </p>
     *
     * @param paymentId 支付ID（String格式，可能是主键ID或支付编号）
     * @return 支付记录实体
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentRecordEntity getPaymentRecord(String paymentId) {
        log.info("[支付服务] 获取支付记录，paymentId={}", paymentId);

        if (paymentId == null || paymentId.trim().isEmpty()) {
            log.warn("[支付服务] 支付ID为空");
            return null;
        }

        try {
            // 尝试作为主键ID查询（Long类型）
            try {
                Long id = Long.parseLong(paymentId);
                PaymentRecordEntity record = paymentRecordDao.selectById(id);
                if (record != null) {
                    log.info("[支付服务] 根据主键ID查询到支付记录，paymentId={}, id={}", paymentId, id);
                    return record;
                }
            } catch (NumberFormatException e) {
                // 不是数字，继续尝试其他查询方式
            }

            // 尝试作为支付编号查询
            PaymentRecordEntity record = paymentRecordDao.selectByOrderNo(paymentId);
            if (record != null) {
                log.info("[支付服务] 根据订单号查询到支付记录，paymentId={}", paymentId);
                return record;
            }

            // 尝试作为交易流水号查询
            record = paymentRecordDao.selectByTransactionNo(paymentId);
            if (record != null) {
                log.info("[支付服务] 根据交易流水号查询到支付记录，paymentId={}", paymentId);
                return record;
            }

            log.warn("[支付服务] 未找到支付记录，paymentId={}", paymentId);
            return null;

        } catch (Exception e) {
            log.error("[支付服务] 获取支付记录异常，paymentId={}", paymentId, e);
            throw new BusinessException("PAYMENT_RECORD_QUERY_ERROR", "查询支付记录失败：" + e.getMessage());
        }
    }

    /**
     * 转换支付方式字符串为Integer
     *
     * @param paymentMethod 支付方式字符串
     * @return 支付方式Integer
     */
    private Integer convertPaymentMethodToInteger(String paymentMethod) {
        if (paymentMethod == null) {
            return 1; // 默认余额支付
        }
        switch (paymentMethod.toUpperCase()) {
            case "BALANCE":
            case "1":
                return 1;
            case "WECHAT":
            case "2":
                return 2;
            case "ALIPAY":
            case "3":
                return 3;
            case "BANK":
            case "4":
                return 4;
            default:
                return 1;
        }
    }

    /**
     * 解析设备ID（String转Long）
     *
     * @param deviceIdStr 设备ID字符串
     * @return 设备ID（Long类型）
     */
    private Long parseDeviceId(String deviceIdStr) {
        if (deviceIdStr == null || deviceIdStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(deviceIdStr);
        } catch (NumberFormatException e) {
            log.warn("[支付服务] 设备ID格式错误，无法转换为Long，deviceId={}", deviceIdStr);
            return null;
        }
    }

    // ==================== PaymentService接口其他方法的占位实现 ====================
    // 这些方法暂时返回空值或默认值，后续根据业务需求实现

    @Override
    public Map<String, Object> getUserPaymentRecords(Long userId, Integer pageNum, Integer pageSize) {
        log.info("[支付服务] 获取用户支付记录列表（分页），userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        try {
            // 参数验证
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 使用MyBatis-Plus分页查询
            Page<PaymentRecordEntity> page = new Page<>(pageNum, pageSize);
            QueryWrapper<PaymentRecordEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("deleted_flag", 0)
                    .orderByDesc("create_time");

            Page<PaymentRecordEntity> resultPage = paymentRecordDao.selectPage(page, queryWrapper);

            // 构建返回结果Map
            Map<String, Object> result = new HashMap<>();
            result.put("list", resultPage.getRecords());
            result.put("total", resultPage.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", resultPage.getPages());

            return result;

        } catch (Exception e) {
            log.error("[支付服务] 获取用户支付记录失败，userId={}, error={}", userId, e.getMessage(), e);
            // 返回空结果而不是抛出异常，保证API稳定性
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0L);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getRefundRecord(String refundId) {
        log.info("[支付服务] 获取退款记录，refundId={}", refundId);

        try {
            // 参数验证
            if (refundId == null || refundId.trim().isEmpty()) {
                throw new IllegalArgumentException("退款ID不能为空");
            }

            // 查询退款记录
            PaymentRefundRecordEntity refundRecord = paymentRefundRecordDao.selectById(refundId);
            // deletedFlag通常为0/1整型（BaseEntity），这里统一按“1=已删除”处理
            if (refundRecord == null || Integer.valueOf(1).equals(refundRecord.getDeletedFlag())) {
                return null; // 或者返回空的Map
            }

            // 构建返回结果Map
            Map<String, Object> result = new HashMap<>();
            result.put("record", refundRecord);

            return result;

        } catch (Exception e) {
            log.error("[支付服务] 获取退款记录失败，refundId={}, error={}", refundId, e.getMessage(), e);
            return null; // 返回null表示未找到或错误
        }
    }

    @Override
    public Map<String, Object> getUserRefundRecords(Long userId, Integer pageNum, Integer pageSize) {
        log.info("[支付服务] 获取用户退款记录列表（分页），userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        try {
            // 参数验证
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 20;
            }

            // 使用MyBatis-Plus分页查询
            Page<PaymentRefundRecordEntity> page = new Page<>(pageNum, pageSize);
            QueryWrapper<PaymentRefundRecordEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("deleted_flag", 0)
                    .orderByDesc("create_time");

            Page<PaymentRefundRecordEntity> resultPage = paymentRefundRecordDao.selectPage(page, queryWrapper);

            // 构建返回结果Map
            Map<String, Object> result = new HashMap<>();
            result.put("list", resultPage.getRecords());
            result.put("total", resultPage.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", resultPage.getPages());

            return result;

        } catch (Exception e) {
            log.error("[支付服务] 获取用户退款记录失败，userId={}, error={}", userId, e.getMessage(), e);
            // 返回空结果而不是抛出异常，保证API稳定性
            Map<String, Object> result = new HashMap<>();
            result.put("list", new ArrayList<>());
            result.put("total", 0L);
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            result.put("pages", 0);
            return result;
        }
    }

    @Override
    public Map<String, Object> getUserPaymentStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[支付服务] 获取用户支付统计，userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", BigDecimal.ZERO);
        result.put("totalCount", 0);
        // TODO: 实现获取用户支付统计逻辑
        return result;
    }

    @Override
    public Map<String, Object> getUserRefundStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[支付服务] 获取用户退款统计，userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", BigDecimal.ZERO);
        result.put("totalCount", 0);
        // TODO: 实现获取用户退款统计逻辑
        return result;
    }

    @Override
    public Map<String, Object> performReconciliation(LocalDateTime startTime, LocalDateTime endTime, Long merchantId) {
        log.info("[支付服务] 执行对账，startTime={}, endTime={}, merchantId={}", startTime, endTime, merchantId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "对账功能待实现");
        // TODO: 实现执行对账逻辑
        return result;
    }

    @Override
    public Map<String, Object> getMerchantSettlementStatistics(Long merchantId, LocalDateTime startTime,
            LocalDateTime endTime) {
        log.info("[支付服务] 获取商户结算统计，merchantId={}, startTime={}, endTime={}", merchantId, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", BigDecimal.ZERO);
        result.put("totalCount", 0);
        // TODO: 实现获取商户结算统计逻辑
        return result;
    }

    @Override
    public Map<String, Object> getPendingAuditRefunds() {
        log.info("[支付服务] 获取待审核退款列表");
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        // TODO: 实现获取待审核退款列表逻辑
        return result;
    }

    @Override
    public Map<String, Object> getPendingProcessRefunds() {
        log.info("[支付服务] 获取待处理退款列表");
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        // TODO: 实现获取待处理退款列表逻辑
        return result;
    }

    @Override
    public Map<String, Object> getHighRiskPayments(Integer riskLevel) {
        log.info("[支付服务] 获取高风险支付记录，riskLevel={}", riskLevel);
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        // TODO: 实现获取高风险支付记录逻辑
        return result;
    }

    @Override
    public Map<String, Object> getHighRiskRefunds(Integer riskLevel) {
        log.info("[支付服务] 获取高风险退款记录，riskLevel={}", riskLevel);
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        // TODO: 实现获取高风险退款记录逻辑
        return result;
    }

    @Override
    public Map<String, Object> getAbnormalPayments(Integer threshold) {
        log.info("[支付服务] 获取异常支付记录，threshold={}", threshold);
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        // TODO: 实现获取异常支付记录逻辑
        return result;
    }

    @Override
    public Map<String, Object> createWechatPayOrder(String accountNo, BigDecimal amount, String description,
            String openId, String notifyUrl) {
        log.info("[支付服务] 创建微信支付订单，accountNo={}, amount={}", accountNo, amount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "微信支付功能待实现");
        // TODO: 实现创建微信支付订单逻辑
        return result;
    }

    @Override
    public Map<String, Object> handleWechatPayNotify(String notifyData) {
        log.info("[支付服务] 处理微信支付回调，notifyData长度={}", notifyData != null ? notifyData.length() : 0);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "微信支付回调处理功能待实现");
        // TODO: 实现处理微信支付回调逻辑
        return result;
    }

    @Override
    public Map<String, Object> createAlipayOrder(String accountNo, BigDecimal amount, String subject,
            String returnUrl) {
        log.info("[支付服务] 创建支付宝支付订单，accountNo={}, amount={}", accountNo, amount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "支付宝支付功能待实现");
        // TODO: 实现创建支付宝支付订单逻辑
        return result;
    }

    @Override
    public Map<String, Object> handleAlipayNotify(Map<String, String> notifyParams) {
        log.info("[支付服务] 处理支付宝回调，notifyParams大小={}", notifyParams != null ? notifyParams.size() : 0);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "支付宝回调处理功能待实现");
        // TODO: 实现处理支付宝回调逻辑
        return result;
    }

    @Override
    public Map<String, Object> wechatRefund(String paymentNo, String refundNo, Integer refundAmount,
            Integer totalAmount) {
        log.info("[支付服务] 微信退款，paymentNo={}, refundNo={}, refundAmount={}", paymentNo, refundNo, refundAmount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "微信退款功能待实现");
        // TODO: 实现微信退款逻辑
        return result;
    }

    @Override
    public Map<String, Object> alipayRefund(String paymentNo, BigDecimal refundAmount, String refundReason) {
        log.info("[支付服务] 支付宝退款，paymentNo={}, refundAmount={}", paymentNo, refundAmount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "支付宝退款功能待实现");
        // TODO: 实现支付宝退款逻辑
        return result;
    }

    @Override
    public Map<String, Object> createBankPaymentOrder(Long accountNo, BigDecimal amount, String bankCardNo,
            String bankName, String transactionId) {
        log.info("[支付服务] 创建银行卡支付订单，accountNo={}, amount={}", accountNo, amount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "银行卡支付功能待实现");
        // TODO: 实现创建银行卡支付订单逻辑
        return result;
    }

    @Override
    public Map<String, Object> processCreditLimitPayment(Long accountNo, BigDecimal amount, String description,
            String creditLimit) {
        log.info("[支付服务] 处理信用额度支付，accountNo={}, amount={}", accountNo, amount);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "信用额度支付功能待实现");
        // TODO: 实现处理信用额度支付逻辑
        return result;
    }
}

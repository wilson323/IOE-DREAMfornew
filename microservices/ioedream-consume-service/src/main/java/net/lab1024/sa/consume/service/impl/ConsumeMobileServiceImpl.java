package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.common.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileMealVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.common.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.domain.vo.MobileBillDetailVO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;

/**
 * 消费移动端服务实现类
 * <p>
 * 实现移动端消费相关业务功能
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
public class ConsumeMobileServiceImpl implements ConsumeMobileService {

    @Resource
    private ConsumeTransactionDao consumeTransactionDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private PaymentRecordDao paymentRecordDao;

    @Override
    @Observed(name = "consume.mobile.quickConsume", contextualName = "consume-mobile-quick")
    public ConsumeMobileResultVO quickConsume(ConsumeMobileQuickForm form) {
        log.warn("[消费移动端] quickConsume方法未实现，form={}", form);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "快速消费功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.scanConsume", contextualName = "consume-mobile-scan")
    public ConsumeMobileResultVO scanConsume(ConsumeMobileScanForm form) {
        log.warn("[消费移动端] scanConsume方法未实现，form={}", form);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "扫码消费功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.nfcConsume", contextualName = "consume-mobile-nfc")
    public ConsumeMobileResultVO nfcConsume(ConsumeMobileNfcForm form) {
        log.warn("[消费移动端] nfcConsume方法未实现，form={}", form);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "NFC消费功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.faceConsume", contextualName = "consume-mobile-face")
    public ConsumeMobileResultVO faceConsume(ConsumeMobileFaceForm form) {
        log.warn("[消费移动端] faceConsume方法未实现，form={}", form);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "人脸识别消费功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.quickUserInfo", contextualName = "consume-mobile-quick-user-info")
    public ConsumeMobileUserVO quickUserInfo(String queryType, String queryValue) {
        log.warn("[消费移动端] quickUserInfo方法未实现，queryType={}, queryValue={}", queryType, queryValue);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "快速用户查询功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.getUserConsumeInfo", contextualName = "consume-mobile-get-user-info")
    public ConsumeMobileUserInfoVO getUserConsumeInfo(Long userId) {
        log.warn("[消费移动端] getUserConsumeInfo方法未实现，userId={}", userId);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "获取用户消费信息功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.getAvailableMeals", contextualName = "consume-mobile-get-meals")
    public List<ConsumeMobileMealVO> getAvailableMeals() {
        log.warn("[消费移动端] getAvailableMeals方法未实现");
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "获取有效餐别功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.getDeviceConfig", contextualName = "consume-mobile-get-device-config")
    public ConsumeDeviceConfigVO getDeviceConfig(Long deviceId) {
        log.warn("[消费移动端] getDeviceConfig方法未实现，deviceId={}", deviceId);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "获取设备配置功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.getDeviceTodayStats", contextualName = "consume-mobile-get-device-stats")
    public ConsumeMobileStatsVO getDeviceTodayStats(Long deviceId) {
        log.warn("[消费移动端] getDeviceTodayStats方法未实现，deviceId={}", deviceId);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "获取设备今日统计功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.getTransactionSummary", contextualName = "consume-mobile-get-summary")
    public ConsumeMobileSummaryVO getTransactionSummary(String areaId) {
        log.warn("[消费移动端] getTransactionSummary方法未实现，areaId={}", areaId);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "获取实时交易汇总功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.syncOfflineTransactions", contextualName = "consume-mobile-sync-offline")
    public ConsumeSyncResultVO syncOfflineTransactions(ConsumeOfflineSyncForm form) {
        log.warn("[消费移动端] syncOfflineTransactions方法未实现，form={}", form);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "离线交易同步功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.getSyncData", contextualName = "consume-mobile-get-sync-data")
    public ConsumeSyncDataVO getSyncData(Long deviceId, String lastSyncTime) {
        log.warn("[消费移动端] getSyncData方法未实现，deviceId={}, lastSyncTime={}", deviceId, lastSyncTime);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "获取同步数据功能待实现");
    }

    @Override
    @Observed(name = "consume.mobile.validateConsumePermission", contextualName = "consume-mobile-validate-permission")
    public ConsumeValidateResultVO validateConsumePermission(ConsumePermissionValidateForm form) {
        log.warn("[消费移动端] validateConsumePermission方法未实现，form={}", form);
        throw new BusinessException("METHOD_NOT_IMPLEMENTED", "验证消费权限功能待实现");
    }

    /**
     * 获取用户统计
     * <p>
     * 获取指定用户的消费统计数据，包括总交易笔数、总金额、今日统计、本月统计
     * </p>
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public ConsumeMobileUserStatsVO getUserStats(Long userId) {
        log.info("[消费移动端] 获取用户统计，userId={}", userId);

        try {
            if (userId == null) {
                throw new BusinessException("PARAM_ERROR", "用户ID不能为空");
            }

            ConsumeMobileUserStatsVO stats = new ConsumeMobileUserStatsVO();
            stats.setUserId(userId);

            // 1. 查询总交易统计（成功状态的交易）
            // 注意：ConsumeTransactionEntity继承BaseEntity，使用getDeleted()方法获取删除标记
            LambdaQueryWrapper<ConsumeTransactionEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(ConsumeTransactionEntity::getUserId, userId.toString())
                    .eq(ConsumeTransactionEntity::getStatus, "SUCCESS")
                    .eq(ConsumeTransactionEntity::getDeleted, 0);
            List<ConsumeTransactionEntity> totalTransactions = consumeTransactionDao.selectList(totalWrapper);

            int totalCount = totalTransactions != null ? totalTransactions.size() : 0;
            BigDecimal totalAmount = totalTransactions != null
                    ? totalTransactions.stream()
                            .map(ConsumeTransactionEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            stats.setTotalCount(totalCount);
            stats.setTotalAmount(totalAmount);

            // 2. 查询今日交易统计
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

            LambdaQueryWrapper<ConsumeTransactionEntity> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.eq(ConsumeTransactionEntity::getUserId, userId.toString())
                    .eq(ConsumeTransactionEntity::getStatus, "SUCCESS")
                    .eq(ConsumeTransactionEntity::getDeleted, 0)
                    .ge(ConsumeTransactionEntity::getCreateTime, todayStart)
                    .le(ConsumeTransactionEntity::getCreateTime, todayEnd);
            List<ConsumeTransactionEntity> todayTransactions = consumeTransactionDao.selectList(todayWrapper);

            int todayCount = todayTransactions != null ? todayTransactions.size() : 0;
            BigDecimal todayAmount = todayTransactions != null
                    ? todayTransactions.stream()
                            .map(ConsumeTransactionEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            stats.setTodayCount(todayCount);
            stats.setTodayAmount(todayAmount);

            // 3. 查询本月交易统计
            LocalDate now = LocalDate.now();
            LocalDateTime monthStart = LocalDateTime.of(now.withDayOfMonth(1), LocalTime.MIN);
            LocalDateTime monthEnd = LocalDateTime.of(now, LocalTime.MAX);

            LambdaQueryWrapper<ConsumeTransactionEntity> monthWrapper = new LambdaQueryWrapper<>();
            monthWrapper.eq(ConsumeTransactionEntity::getUserId, userId.toString())
                    .eq(ConsumeTransactionEntity::getStatus, "SUCCESS")
                    .eq(ConsumeTransactionEntity::getDeleted, 0)
                    .ge(ConsumeTransactionEntity::getCreateTime, monthStart)
                    .le(ConsumeTransactionEntity::getCreateTime, monthEnd);
            List<ConsumeTransactionEntity> monthTransactions = consumeTransactionDao.selectList(monthWrapper);

            int monthCount = monthTransactions != null ? monthTransactions.size() : 0;
            BigDecimal monthAmount = monthTransactions != null
                    ? monthTransactions.stream()
                            .map(ConsumeTransactionEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            stats.setMonthCount(monthCount);
            stats.setMonthAmount(monthAmount);

            log.info("[消费移动端] 获取用户统计成功，userId={}, totalCount={}, totalAmount={}, todayCount={}, todayAmount={}, monthCount={}, monthAmount={}",
                    userId, totalCount, totalAmount, todayCount, todayAmount, monthCount, monthAmount);

            return stats;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费移动端] 获取用户统计参数错误，userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费移动端] 获取用户统计业务异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费移动端] 获取用户统计系统异常，userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_USER_STATS_SYSTEM_ERROR", "获取用户统计失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[消费移动端] 获取用户统计未知异常，userId={}", userId, e);
            throw new SystemException("GET_USER_STATS_SYSTEM_ERROR", "获取用户统计失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "consume.mobile.getBillDetail", contextualName = "consume-mobile-get-bill-detail")
    public MobileBillDetailVO getBillDetail(String orderId) {
        log.info("[消费移动端] 获取账单详情，orderId={}", orderId);

        try {
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new BusinessException("PARAM_ERROR", "订单ID不能为空");
            }

            // 1. 先通过订单号查询消费记录
            ConsumeRecordEntity consumeRecord = consumeRecordDao.selectByOrderNo(orderId);
            if (consumeRecord == null) {
                log.warn("[消费移动端] 消费记录不存在，orderId={}", orderId);
                throw new BusinessException("BILL_NOT_FOUND", "账单不存在");
            }

            // 2. 查询支付记录（通过订单号或交易流水号）
            PaymentRecordEntity paymentRecord = null;
            if (consumeRecord.getTransactionNo() != null) {
                paymentRecord = paymentRecordDao.selectByTransactionNo(consumeRecord.getTransactionNo());
            }
            if (paymentRecord == null && consumeRecord.getOrderNo() != null) {
                paymentRecord = paymentRecordDao.selectByOrderNo(consumeRecord.getOrderNo());
            }

            // 3. 构建账单详情VO
            MobileBillDetailVO billDetail = buildBillDetailVO(consumeRecord, paymentRecord);

            log.info("[消费移动端] 获取账单详情成功，orderId={}, amount={}", orderId, billDetail.getAmount());
            return billDetail;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费移动端] 获取账单详情参数错误，orderId={}, error={}", orderId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费移动端] 获取账单详情业务异常，orderId={}, code={}, message={}", orderId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费移动端] 获取账单详情系统异常，orderId={}, code={}, message={}", orderId, e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_BILL_DETAIL_SYSTEM_ERROR", "获取账单详情失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[消费移动端] 获取账单详情未知异常，orderId={}", orderId, e);
            throw new SystemException("GET_BILL_DETAIL_SYSTEM_ERROR", "获取账单详情失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建账单详情VO
     *
     * @param consumeRecord 消费记录
     * @param paymentRecord 支付记录
     * @return 账单详情VO
     */
    private MobileBillDetailVO buildBillDetailVO(ConsumeRecordEntity consumeRecord, PaymentRecordEntity paymentRecord) {
        MobileBillDetailVO billDetail = new MobileBillDetailVO();

        // 基本信息
        billDetail.setOrderId(consumeRecord.getOrderNo() != null ? consumeRecord.getOrderNo() : consumeRecord.getTransactionNo());
        billDetail.setBillNumber(consumeRecord.getTransactionNo());
        billDetail.setTransactionNumber(consumeRecord.getTransactionNo());

        // 金额信息
        billDetail.setAmount(consumeRecord.getAmount() != null ? consumeRecord.getAmount() : BigDecimal.ZERO);
        billDetail.setOriginalAmount(consumeRecord.getAmount() != null ? consumeRecord.getAmount() : BigDecimal.ZERO);
        billDetail.setDiscountAmount(consumeRecord.getDiscountAmount() != null ? consumeRecord.getDiscountAmount() : BigDecimal.ZERO);
        billDetail.setActualAmount(consumeRecord.getActualAmount() != null ? consumeRecord.getActualAmount() : consumeRecord.getAmount());

        // 如果有支付记录，使用支付记录的金额信息
        if (paymentRecord != null) {
            billDetail.setFee(paymentRecord.getPaymentFee() != null ? paymentRecord.getPaymentFee() : BigDecimal.ZERO);
            billDetail.setActualAmount(paymentRecord.getPaymentAmount() != null ? paymentRecord.getPaymentAmount() : billDetail.getActualAmount());
            // PaymentRecordEntity没有discountAmount字段，保持原有值
        } else {
            billDetail.setFee(BigDecimal.ZERO);
        }

        // 消费类型
        billDetail.setConsumeType(consumeRecord.getConsumeType() != null ? consumeRecord.getConsumeType() : "OTHER");
        billDetail.setDescription(consumeRecord.getMerchantName() != null ? consumeRecord.getMerchantName() : "消费");

        // 商户信息
        billDetail.setMerchantName(consumeRecord.getMerchantName());
        billDetail.setLocation(consumeRecord.getAreaName() != null ? consumeRecord.getAreaName() : "未知位置");

        // 时间信息
        billDetail.setConsumeTime(consumeRecord.getConsumeTime() != null ? consumeRecord.getConsumeTime() : consumeRecord.getCreateTime());
        if (paymentRecord != null) {
            billDetail.setPaymentTime(paymentRecord.getPaymentTime() != null ? paymentRecord.getPaymentTime() : paymentRecord.getCreateTime());
            billDetail.setCompleteTime(paymentRecord.getCompleteTime() != null ? paymentRecord.getCompleteTime() : paymentRecord.getUpdateTime());
        } else {
            billDetail.setPaymentTime(consumeRecord.getPayTime() != null ? consumeRecord.getPayTime() : consumeRecord.getCreateTime());
            billDetail.setCompleteTime(consumeRecord.getConsumeTime() != null ? consumeRecord.getConsumeTime() : consumeRecord.getCreateTime());
        }

        // 账户信息
        billDetail.setAccountId(consumeRecord.getAccountId());
        billDetail.setAccountNumber(consumeRecord.getAccountNo());
        billDetail.setUserId(consumeRecord.getUserId());
        billDetail.setUserName(consumeRecord.getUserName());

        // 支付方式
        String paymentMethod = consumeRecord.getPayMethod() != null ? consumeRecord.getPayMethod() : "BALANCE";
        if (paymentRecord != null && paymentRecord.getPaymentMethod() != null) {
            // 将Integer支付方式转换为String
            paymentMethod = convertPaymentMethodToString(paymentRecord.getPaymentMethod());
        }
        billDetail.setPaymentMethod(paymentMethod);

        // 状态信息
        String status = consumeRecord.getStatus() != null ? consumeRecord.getStatus() : "SUCCESS";
        if (paymentRecord != null) {
            // 将Integer支付状态转换为String
            status = paymentRecord.getPaymentStatus() != null ? convertPaymentStatusToString(paymentRecord.getPaymentStatus()) : status;
        }
        billDetail.setStatus(status);

        // 设备信息
        if (consumeRecord.getDeviceId() != null) {
            billDetail.setDeviceId(consumeRecord.getDeviceId().toString());
            billDetail.setDeviceName(consumeRecord.getDeviceName());
        }

        // 区域信息
        billDetail.setAreaId(consumeRecord.getAreaId());
        billDetail.setAreaName(consumeRecord.getAreaName());

        // 退款信息
        billDetail.setRefundable(consumeRecord.getRefundStatus() != null && consumeRecord.getRefundStatus() == 0);
        if (consumeRecord.getRefundAmount() != null && consumeRecord.getRefundAmount().compareTo(BigDecimal.ZERO) > 0) {
            billDetail.setRefundedAmount(consumeRecord.getRefundAmount());
        }

        // 其他信息
        billDetail.setSource("移动端");
        billDetail.setRemark(consumeRecord.getRemark());
        billDetail.setCreateTime(consumeRecord.getCreateTime());
        billDetail.setUpdateTime(consumeRecord.getUpdateTime());

        return billDetail;
    }

    /**
     * 将Integer支付方式转换为String
     *
     * @param paymentMethod 支付方式整数
     * @return 支付方式字符串
     */
    private String convertPaymentMethodToString(Integer paymentMethod) {
        if (paymentMethod == null) {
            return "BALANCE";
        }
        switch (paymentMethod) {
            case 1:
                return "BALANCE"; // 余额支付
            case 2:
                return "WECHAT"; // 微信支付
            case 3:
                return "ALIPAY"; // 支付宝
            case 4:
                return "BANK"; // 银行卡
            case 5:
                return "CASH"; // 现金
            case 6:
                return "QRCODE"; // 二维码
            case 7:
                return "NFC"; // NFC
            case 8:
                return "BIOMETRIC"; // 生物识别
            default:
                return "BALANCE";
        }
    }

    /**
     * 将Integer支付状态转换为String
     *
     * @param paymentStatus 支付状态整数
     * @return 支付状态字符串
     */
    private String convertPaymentStatusToString(Integer paymentStatus) {
        if (paymentStatus == null) {
            return "PENDING";
        }
        switch (paymentStatus) {
            case 1:
                return "PENDING"; // 待支付
            case 2:
                return "PROCESSING"; // 支付中
            case 3:
                return "SUCCESS"; // 支付成功
            case 4:
                return "FAILED"; // 支付失败
            case 5:
                return "REFUNDED"; // 已退款
            case 6:
                return "PARTIAL_REFUNDED"; // 部分退款
            case 7:
                return "CANCELLED"; // 已取消
            default:
                return "PENDING";
        }
    }
}





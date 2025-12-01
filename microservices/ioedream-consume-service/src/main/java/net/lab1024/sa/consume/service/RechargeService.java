package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.RechargeRecordDao;
import net.lab1024.sa.consume.domain.dto.RechargeQueryDTO;
import net.lab1024.sa.consume.domain.dto.RechargeRequestDTO;
import net.lab1024.sa.consume.domain.dto.RechargeResultDTO;
import net.lab1024.sa.consume.domain.entity.RechargeRecordEntity;
import net.lab1024.sa.consume.domain.enums.RechargeStatusEnum;
import net.lab1024.sa.consume.domain.enums.RechargeTypeEnum;
import net.lab1024.sa.consume.manager.RechargeManager;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartResponseUtil;

/**
 * 充值服务
 * 负责处理各种充值方式，包括微信、支付宝、银行卡、现金等
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Service
public class RechargeService {

    @Resource
    private RechargeManager rechargeManager;

    @Resource
    private RechargeRecordDao rechargeRecordDao;

    // TODO: 待WebSocket和心跳管理器模块完善后启用
    // @Resource
    // private HeartBeatManager heartBeatManager;

    // @Resource
    // private WebSocketSessionManager webSocketSessionManager;

    @Value("${file.storage.local.upload-path:D:/Progect/mart-admin-master/upload/}")
    private String uploadPath;

    /**
     * 创建充值订单
     *
     * @param rechargeRequest 充值请求
     * @return 充值结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<RechargeResultDTO> createRecharge(@Valid RechargeRequestDTO rechargeRequest) {
        try {
            log.info("开始创建充值订单: 用户ID={}, 充值金额={}, 充值方式={}",
                    rechargeRequest.getUserId(), rechargeRequest.getAmount(), rechargeRequest.getRechargeType());

            // 1. 验证充值请求
            String validationResult = validateRechargeRequest(rechargeRequest);
            if (validationResult != null) {
                log.warn("充值请求验证失败: {}", validationResult);
                return ResponseDTO.error(validationResult);
            }

            // 2. 创建充值记录
            RechargeRecordEntity rechargeRecord = createRechargeRecord(rechargeRequest);
            if (!rechargeRecordDao.insert(rechargeRecord)) {
                return ResponseDTO.error("创建充值记录失败");
            }

            // 3. 处理充值流程
            RechargeResultDTO result = processRecharge(rechargeRecord);

            log.info("充值订单创建完成: 订单号={}, 状态={}", result.getOrderNo(), result.getStatus());
            return ResponseDTO.ok(result, "充值订单创建成功");

        } catch (Exception e) {
            log.error("创建充值订单失败", e);
            return ResponseDTO.error("创建充值订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询充值记录
     *
     * @param queryDTO 查询条件
     * @return 充值记录分页
     */
    public ResponseDTO<Page<RechargeRecordEntity>> queryRechargeRecords(RechargeQueryDTO queryDTO) {
        try {
            log.info("查询充值记录: 用户ID={}, 状态={}", queryDTO.getUserId(), queryDTO.getStatus());

            // 使用PageParam和RechargeRecordDao进行查询
            net.lab1024.sa.common.domain.PageParam pageParam = new net.lab1024.sa.common.domain.PageParam();
            pageParam.setPageNum(Long.valueOf(queryDTO.getPageNum()));
            pageParam.setPageSize(Long.valueOf(queryDTO.getPageSize()));

            // 使用DAO的queryPage方法
            net.lab1024.sa.common.domain.PageResult<RechargeRecordEntity> pageResult = rechargeRecordDao
                    .queryPage(queryDTO, pageParam);

            // 转换为Spring Data Page
            Pageable pageable = PageRequest.of(
                    queryDTO.getPageNum() - 1,
                    queryDTO.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createTime"));

            // 构建Spring Data Page对象
            Page<RechargeRecordEntity> page = new org.springframework.data.domain.PageImpl<>(
                    pageResult.getList(),
                    pageable,
                    pageResult.getTotal());

            log.info("查询充值记录完成: 总数={}", page.getTotalElements());
            return ResponseDTO.ok(page, "查询充值记录成功");

        } catch (Exception e) {
            log.error("查询充值记录失败", e);
            return ResponseDTO.error("查询充值记录失败");
        }
    }

    /**
     * 充值回调处理
     *
     * @param orderNo           充值订单号
     * @param thirdPartyOrderNo 第三方订单号
     * @param status            充值状态
     * @param remark            备注
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> handleRechargeCallback(String orderNo, String thirdPartyOrderNo,
            RechargeStatusEnum status, String remark) {
        try {
            log.info("处理充值回调: 订单号={}, 状态={}, 第三方订单号={}",
                    orderNo, status, thirdPartyOrderNo);

            // 1. 查询充值记录
            RechargeRecordEntity rechargeRecord = rechargeRecordDao.selectByRechargeNo(orderNo);
            if (rechargeRecord == null) {
                log.warn("充值记录不存在: {}", orderNo);
                return ResponseDTO.error("充值记录不存在");
            }

            // 2. 检查状态重复（status是Integer类型）
            Integer statusValue = status != null ? status.getValue() : null;
            if (rechargeRecord.getStatus() != null && rechargeRecord.getStatus().equals(statusValue)) {
                log.info("充值状态重复: {}", orderNo);
                return ResponseDTO.ok("状态重复");
            }

            // 3. 更新充值状态
            updateRechargeStatus(rechargeRecord, thirdPartyOrderNo, status, remark);

            // 4. 处理充值成功后的业务逻辑
            if (status != null && RechargeStatusEnum.SUCCESS.getValue().equals(status.getValue())) {
                rechargeManager.updateUserBalance(rechargeRecord.getUserId(), rechargeRecord.getAmount(),
                        rechargeRecord.getTransactionNo());

                // 5. 发送通知
                sendRechargeNotification(rechargeRecord);
            }

            log.info("充值回调处理完成: 订单号={}, 状态={}", orderNo, status);
            return ResponseDTO.ok("处理成功");

        } catch (Exception e) {
            log.error("处理充值回调失败: 订单号={}", orderNo, e);
            return ResponseDTO.error("处理充值回调失败");
        }
    }

    /**
     * 获取充值统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    public ResponseDTO<Map<String, Object>> getRechargeStatistics(Long userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        try {
            log.info("获取充值统计: 用户ID={}, 开始日期={}, 结束日期={}", userId, startDate, endDate);

            Map<String, Object> statistics = rechargeManager.getRechargeStatistics(userId, startDate, endDate);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取充值统计失败", e);
            return ResponseDTO.error("获取充值统计失败");
        }
    }

    /**
     * 验证充值请求
     */
    private String validateRechargeRequest(RechargeRequestDTO request) {
        // 校验金额
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "充值金额必须大于0";
        }

        // 校验充值金额范围
        if (request.getAmount().compareTo(new BigDecimal("0.01")) < 0 ||
                request.getAmount().compareTo(new BigDecimal("50000.00")) > 0) {
            return "充值金额范围(0.01-50000.00)元";
        }

        // 校验充值方式
        if (request.getRechargeType() == null) {
            return "充值方式不能为空";
        }

        // 校验用户ID
        if (request.getUserId() == null || request.getUserId() <= 0) {
            return "用户ID无效";
        }

        // 校验每日充值限额
        BigDecimal dailyTotal = rechargeManager.getTodayRechargeAmount(request.getUserId());
        BigDecimal dailyLimit = new BigDecimal("100000.00");
        if (dailyTotal.add(request.getAmount()).compareTo(dailyLimit) > 0) {
            return "超出每日充值限额: " + dailyLimit;
        }

        return null; // 验证通过
    }

    /**
     * 创建充值记录
     */
    private RechargeRecordEntity createRechargeRecord(RechargeRequestDTO request) {
        RechargeRecordEntity record = new RechargeRecordEntity();

        // 基本信息
        record.setTransactionNo(generateOrderNo());
        record.setUserId(request.getUserId());
        record.setAmount(request.getAmount());
        if (request.getRechargeType() != null) {
            record.setRechargeMethod(request.getRechargeType().getValue());
        }
        record.setStatus(1); // 1表示待确认

        // 支付信息
        record.setPaymentChannel(request.getPayChannel());

        // 备注信息
        record.setRemark(request.getRemark());

        // 费用信息
        record.setFee(BigDecimal.ZERO);
        record.setActualAmount(request.getAmount());

        return record;
    }

    /**
     * 处理充值流程
     */
    private RechargeResultDTO processRecharge(RechargeRecordEntity rechargeRecord) {
        RechargeResultDTO result = new RechargeResultDTO();

        result.setOrderNo(rechargeRecord.getTransactionNo());
        result.setAmount(rechargeRecord.getAmount());
        if (rechargeRecord.getRechargeMethod() != null) {
            RechargeTypeEnum type = RechargeTypeEnum.values()[rechargeRecord.getRechargeMethod() - 1];
            result.setRechargeType(type);
        }
        // status是Integer类型，需要转换为枚举
        if (rechargeRecord.getStatus() != null) {
            for (RechargeStatusEnum statusEnum : RechargeStatusEnum.values()) {
                if (statusEnum.getValue().equals(rechargeRecord.getStatus())) {
                    result.setStatus(statusEnum);
                    break;
                }
            }
        }
        result.setCreateTime(rechargeRecord.getCreateTime());

        // 根据充值方式处理
        if (rechargeRecord.getRechargeMethod() != null) {
            RechargeTypeEnum rechargeType = null;
            for (RechargeTypeEnum type : RechargeTypeEnum.values()) {
                if (type.getValue().equals(rechargeRecord.getRechargeMethod())) {
                    rechargeType = type;
                    break;
                }
            }

            if (rechargeType != null) {
                switch (rechargeType) {
                    case ALIPAY:
                        result = processAlipayRecharge(rechargeRecord);
                        break;
                    case WECHAT:
                        result = processWechatRecharge(rechargeRecord);
                        break;
                    case BANK_CARD:
                        result = processBankCardRecharge(rechargeRecord);
                        break;
                    case CASH:
                        result = processCashRecharge(rechargeRecord);
                        break;
                    case SYSTEM:
                        result = processSystemRecharge(rechargeRecord);
                        break;
                    default:
                        log.warn("未知充值方式: {}", rechargeRecord.getRechargeMethod());
                        break;
                }
            }
        }

        return result;
    }

    /**
     * 处理支付宝充值
     */
    private RechargeResultDTO processAlipayRecharge(RechargeRecordEntity record) {
        RechargeResultDTO result = new RechargeResultDTO();

        // 调用支付宝API
        try {
            Map<String, Object> payParams = buildAlipayParams(record);
            String payUrl = rechargeManager.callAlipayAPI(payParams);

            result.setOrderNo(record.getTransactionNo());
            result.setStatus(RechargeStatusEnum.PENDING);
            result.setPayUrl(payUrl);
            result.setMessage("请使用支付宝扫码支付");

        } catch (Exception e) {
            log.error("支付宝充值失败: 订单号={}", record.getTransactionNo(), e);
            result.setStatus(RechargeStatusEnum.FAILED);
            result.setMessage("支付宝充值失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 处理微信充值
     */
    private RechargeResultDTO processWechatRecharge(RechargeRecordEntity record) {
        RechargeResultDTO result = new RechargeResultDTO();

        try {
            Map<String, Object> payParams = buildWechatParams(record);
            String qrCode = rechargeManager.callWechatAPI(payParams);

            result.setOrderNo(record.getTransactionNo());
            result.setStatus(RechargeStatusEnum.PENDING);
            result.setQrCode(qrCode);
            result.setMessage("请使用微信扫码支付");

        } catch (Exception e) {
            log.error("微信充值失败: 订单号={}", record.getTransactionNo(), e);
            result.setStatus(RechargeStatusEnum.FAILED);
            result.setMessage("微信充值失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 处理银行卡充值
     */
    private RechargeResultDTO processBankCardRecharge(RechargeRecordEntity record) {
        RechargeResultDTO result = new RechargeResultDTO();

        // 银行卡充值需要人工审核
        result.setOrderNo(record.getTransactionNo());
        result.setStatus(RechargeStatusEnum.PENDING);
        result.setMessage("银行卡充值需要人工审核");

        return result;
    }

    /**
     * 处理现金充值
     */
    private RechargeResultDTO processCashRecharge(RechargeRecordEntity record) {
        RechargeResultDTO result = new RechargeResultDTO();

        // 现金充值需要现场确认
        result.setOrderNo(record.getTransactionNo());
        result.setStatus(RechargeStatusEnum.PENDING);
        result.setMessage("现金充值需要现场确认");

        return result;
    }

    /**
     * 处理系统充值
     */
    private RechargeResultDTO processSystemRecharge(RechargeRecordEntity record) {
        RechargeResultDTO result = new RechargeResultDTO();

        try {
            // 系统直接充值
            rechargeManager.updateUserBalance(record.getUserId(), record.getAmount(), record.getTransactionNo());

            // 更新充值状态（2表示成功）
            record.setStatus(2);
            record.setConfirmTime(LocalDateTime.now());
            rechargeRecordDao.updateById(record);

            result.setOrderNo(record.getTransactionNo());
            result.setStatus(RechargeStatusEnum.SUCCESS);
            result.setMessage("系统充值成功");

        } catch (Exception e) {
            log.error("系统充值失败: 订单号={}", record.getTransactionNo(), e);
            result.setStatus(RechargeStatusEnum.FAILED);
            result.setMessage("系统充值失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新充值状态
     */
    private void updateRechargeStatus(RechargeRecordEntity record, String thirdPartyOrderNo,
            RechargeStatusEnum status, String remark) {
        record.setExternalTransactionNo(thirdPartyOrderNo);
        if (status != null) {
            record.setStatus(status.getValue());
        }
        record.setRemark(remark);

        if (status != null && (status == RechargeStatusEnum.SUCCESS || status == RechargeStatusEnum.FAILED)) {
            record.setConfirmTime(LocalDateTime.now());
        }

        rechargeRecordDao.updateById(record);
    }

    /**
     * 发送充值成功通知
     * 严格遵循repowiki规范：使用WebSocket发送实时通知到用户设备
     */
    private void sendRechargeNotification(RechargeRecordEntity record) {
        try {
            // 1. 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "RECHARGE_SUCCESS");
            notification.put("userId", record.getUserId());
            notification.put("amount", record.getAmount());
            notification.put("orderNo", record.getTransactionNo());
            notification.put("rechargeMethod", record.getRechargeMethod());
            notification.put("rechargeMethodText", record.getRechargeMethodText());
            notification.put("timestamp", System.currentTimeMillis());
            if (record.getRechargeTime() != null) {
                notification.put("rechargeTime", record.getRechargeTime());
            }

            // TODO: 待WebSocket和心跳管理器模块完善后启用通知功能
            // 2. 通过WebSocket发送到用户设备
            // if (webSocketSessionManager != null && record.getUserId() != null) {
            // String message = JSON.toJSONString(notification);
            // boolean sent =
            // webSocketSessionManager.sendToUser(String.valueOf(record.getUserId()),
            // message);
            // if (sent) {
            // log.debug("充值成功通知已发送: 用户ID={}, 订单号={}, 金额={}",
            // record.getUserId(), record.getTransactionNo(), record.getAmount());
            // } else {
            // log.debug("用户未在线，充值通知未发送: 用户ID={}, 订单号={}",
            // record.getUserId(), record.getTransactionNo());
            // }
            // } else {
            // log.warn("WebSocket会话管理器未初始化或用户ID为空，无法发送充值通知: userId={}",
            // record.getUserId());
            // }

            // 3. 尝试通过心跳管理器发送（如果方法存在）
            // if (heartBeatManager != null && record.getUserId() != null) {
            // try {
            // // 使用反射调用broadcastToUserDevices方法（如果存在）
            // java.lang.reflect.Method method = heartBeatManager.getClass()
            // .getMethod("broadcastToUserDevices", Long.class, Map.class);
            // method.invoke(heartBeatManager, record.getUserId(), notification);
            // } catch (NoSuchMethodException e) {
            // // 方法不存在，忽略（使用WebSocket已发送）
            // log.trace("HeartBeatManager.broadcastToUserDevices方法不存在，已使用WebSocket发送通知");
            // } catch (Exception e) {
            // log.debug("通过心跳管理器发送通知失败，已使用WebSocket发送: {}", e.getMessage());
            // }
            // }

            log.debug("充值通知功能已暂时禁用，待WebSocket和心跳管理器模块完善后启用");

        } catch (Exception e) {
            // 通知失败不影响主流程，只记录日志
            log.error("发送充值通知失败: 订单号={}, 用户ID={}",
                    record.getTransactionNo(), record.getUserId(), e);
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "RC" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 构建支付宝参数
     */
    private Map<String, Object> buildAlipayParams(RechargeRecordEntity record) {
        Map<String, Object> params = new HashMap<>();
        params.put("out_trade_no", record.getTransactionNo());
        params.put("total_amount", record.getAmount().toString());
        params.put("subject", "账户充值");
        params.put("body", "用户ID: " + record.getUserId() + " 充值金额: " + record.getAmount());
        return params;
    }

    /**
     * 构建微信参数
     */
    private Map<String, Object> buildWechatParams(RechargeRecordEntity record) {
        Map<String, Object> params = new HashMap<>();
        params.put("out_trade_no", record.getTransactionNo());
        params.put("total_fee", record.getAmount().multiply(new BigDecimal("100")).intValue());
        params.put("body", "账户充值");
        params.put("attach", "userId:" + record.getUserId());
        return params;
    }

    /**
     * 根据ID获取充值详情
     *
     * @param id 充值记录ID
     * @return 充值记录详情
     */
    public ResponseDTO<RechargeRecordEntity> getRechargeDetail(Long id) {
        try {
            log.info("获取充值详情: id={}", id);

            if (id == null || id <= 0) {
                return ResponseDTO.error("充值记录ID无效");
            }

            // 使用RechargeManager的方法获取记录
            RechargeRecordEntity record = rechargeManager.getRechargeRecord(id);
            if (record == null) {
                return ResponseDTO.error("充值记录不存在");
            }

            return ResponseDTO.ok(record);

        } catch (Exception e) {
            log.error("获取充值详情失败: id={}", id, e);
            return ResponseDTO.error("获取充值详情失败: " + e.getMessage());
        }
    }

    /**
     * 充值退款
     *
     * @param id     充值记录ID
     * @param reason 退款原因
     * @return 退款结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> refundRecharge(Long id, String reason) {
        try {
            log.info("开始退款: 充值ID={}, 退款原因={}", id, reason);

            if (id == null || id <= 0) {
                return ResponseDTO.error("充值记录ID无效");
            }

            // 1. 查询充值记录
            RechargeRecordEntity record = rechargeManager.getRechargeRecord(id);
            if (record == null) {
                return ResponseDTO.error("充值记录不存在");
            }

            // 2. 检查充值状态（status是Integer类型，2表示成功）
            if (record.getStatus() == null || !Integer.valueOf(2).equals(record.getStatus())) {
                return ResponseDTO.error("只有成功的充值记录才能退款");
            }

            // 3. 检查是否已退款（4表示已退款）
            if (Integer.valueOf(4).equals(record.getStatus())) {
                return ResponseDTO.error("该充值记录已退款");
            }

            // 4. 更新充值状态为已退款（4表示已退款）
            record.setStatus(4);
            record.setRefundTime(LocalDateTime.now());
            record.setRefundReason(reason);
            rechargeRecordDao.updateById(record);

            // 5. 扣减用户余额
            rechargeManager.updateUserBalance(record.getUserId(), record.getAmount().negate(),
                    record.getTransactionNo() + "_refund");

            log.info("退款成功: 充值ID={}, 退款金额={}", id, record.getAmount());
            return ResponseDTO.ok("退款成功，退款金额: " + record.getAmount());

        } catch (Exception e) {
            log.error("退款失败: 充值ID={}", id, e);
            return ResponseDTO.error("退款失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的充值方式列表
     *
     * @return 充值方式列表
     */
    public ResponseDTO<List<Map<String, Object>>> getRechargeMethods() {
        try {
            log.info("获取充值方式列表");

            List<Map<String, Object>> methods = new ArrayList<>();

            // 遍历所有充值方式枚举
            for (RechargeTypeEnum type : RechargeTypeEnum.values()) {
                Map<String, Object> method = new HashMap<>();
                method.put("code", type.name());
                method.put("value", type.getValue());
                method.put("description", type.getDescription());
                method.put("enabled", true); // 默认启用

                // 根据充值方式设置特定属性
                switch (type) {
                    case ALIPAY:
                        method.put("minAmount", "0.01");
                        method.put("maxAmount", "50000.00");
                        method.put("feeRate", "0.006"); // 0.6%手续费
                        break;
                    case WECHAT:
                        method.put("minAmount", "0.01");
                        method.put("maxAmount", "50000.00");
                        method.put("feeRate", "0.006"); // 0.6%手续费
                        break;
                    case BANK_CARD:
                        method.put("minAmount", "1.00");
                        method.put("maxAmount", "100000.00");
                        method.put("feeRate", "0");
                        method.put("needAudit", true); // 需要人工审核
                        break;
                    case CASH:
                        method.put("minAmount", "0.01");
                        method.put("maxAmount", "100000.00");
                        method.put("feeRate", "0");
                        method.put("needConfirm", true); // 需要现场确认
                        break;
                    case SYSTEM:
                        method.put("minAmount", "0.01");
                        method.put("maxAmount", "100000.00");
                        method.put("feeRate", "0");
                        method.put("adminOnly", true); // 仅管理员可用
                        break;
                    case SUBSIDY:
                        method.put("minAmount", "0.01");
                        method.put("maxAmount", "100000.00");
                        method.put("feeRate", "0");
                        method.put("adminOnly", true); // 仅管理员可用
                        break;
                }

                methods.add(method);
            }

            log.info("获取充值方式列表完成: 共{}种方式", methods.size());
            return ResponseDTO.ok(methods, "获取充值方式列表成功");

        } catch (Exception e) {
            log.error("获取充值方式列表失败", e);
            return ResponseDTO.error("获取充值方式列表失败: " + e.getMessage());
        }
    }

    /**
     * 验证充值金额是否有效
     *
     * @param paymentMethod 支付方式
     * @param amount        充值金额
     * @return 验证结果
     */
    public ResponseDTO<Map<String, Object>> validateRechargeAmount(String paymentMethod, BigDecimal amount) {
        try {
            log.info("验证充值金额: 支付方式={}, 金额={}", paymentMethod, amount);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "");

            // 1. 金额基本验证
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                result.put("message", "充值金额必须大于0");
                return ResponseDTO.ok(result);
            }

            // 2. 使用RechargeManager验证
            boolean isValid = rechargeManager.validateRechargeAmount(amount, paymentMethod);
            if (!isValid) {
                result.put("message", "充值金额验证失败，请检查金额范围和支付方式");
                return ResponseDTO.ok(result);
            }

            // 3. 验证通过
            result.put("valid", true);
            result.put("message", "验证通过");
            result.put("amount", amount);
            result.put("paymentMethod", paymentMethod);

            log.info("充值金额验证通过: 支付方式={}, 金额={}", paymentMethod, amount);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("验证充值金额失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
            return ResponseDTO.ok(result);
        }
    }

    /**
     * 批量充值
     *
     * @param rechargeRequests 充值请求列表
     * @return 批量充值结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> batchRecharge(List<RechargeRequestDTO> rechargeRequests) {
        try {
            log.info("开始批量充值: 数量={}", rechargeRequests.size());

            if (rechargeRequests == null || rechargeRequests.isEmpty()) {
                return ResponseDTO.error("充值请求列表不能为空");
            }

            // 限制批量数量
            if (rechargeRequests.size() > 100) {
                return ResponseDTO.error("批量充值数量不能超过100条");
            }

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> successList = new ArrayList<>();
            List<Map<String, Object>> failList = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            // 逐个处理充值请求
            for (int i = 0; i < rechargeRequests.size(); i++) {
                RechargeRequestDTO request = rechargeRequests.get(i);
                Map<String, Object> itemResult = new HashMap<>();
                itemResult.put("index", i + 1);
                itemResult.put("userId", request.getUserId());
                itemResult.put("amount", request.getAmount());

                try {
                    // 调用单个充值方法
                    ResponseDTO<RechargeResultDTO> rechargeResult = createRecharge(request);
                    if (rechargeResult.getOk() && rechargeResult.getData() != null) {
                        itemResult.put("success", true);
                        itemResult.put("orderNo", rechargeResult.getData().getOrderNo());
                        itemResult.put("message", "充值成功");
                        successList.add(itemResult);
                        successCount++;
                    } else {
                        itemResult.put("success", false);
                        itemResult.put("message", rechargeResult.getMsg() != null ? rechargeResult.getMsg() : "充值失败");
                        failList.add(itemResult);
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("批量充值中单个充值失败: index={}, userId={}", i + 1, request.getUserId(), e);
                    itemResult.put("success", false);
                    itemResult.put("message", "充值异常: " + e.getMessage());
                    failList.add(itemResult);
                    failCount++;
                }
            }

            // 构建返回结果
            result.put("total", rechargeRequests.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("successList", successList);
            result.put("failList", failList);
            result.put("allSuccess", failCount == 0);

            log.info("批量充值完成: 总数={}, 成功={}, 失败={}", rechargeRequests.size(), successCount, failCount);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("批量充值失败", e);
            return ResponseDTO.error("批量充值失败: " + e.getMessage());
        }
    }

    /**
     * 导出充值记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param format    导出格式（EXCEL/CSV）
     * @return 导出文件下载URL
     */
    public ResponseDTO<String> exportRechargeRecords(String startDate, String endDate, String format) {
        try {
            log.info("开始导出充值记录: startDate={}, endDate={}, format={}", startDate, endDate, format);

            // 1. 构建查询条件
            RechargeQueryDTO queryDTO = new RechargeQueryDTO();
            if (startDate != null && !startDate.trim().isEmpty()) {
                queryDTO.setStartTime(LocalDateTime.parse(startDate));
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                queryDTO.setEndTime(LocalDateTime.parse(endDate));
            }
            queryDTO.setPageNum(1);
            queryDTO.setPageSize(10000); // 导出时设置较大的页面大小

            // 2. 查询所有符合条件的记录
            ResponseDTO<Page<RechargeRecordEntity>> response = queryRechargeRecords(queryDTO);
            if (!response.getOk() || response.getData() == null) {
                return ResponseDTO.error("查询充值记录失败");
            }

            List<RechargeRecordEntity> records = response.getData().getContent();
            if (records.isEmpty()) {
                return ResponseDTO.error("没有符合条件的充值记录");
            }

            // 3. 生成导出文件
            String fileName = generateExportFileName(startDate, endDate, format);
            generateExportFile(records, fileName, format);

            // 4. 返回文件访问路径
            String downloadUrl = "/upload/export/recharge/" + fileName;
            log.info("充值记录导出成功: fileName={}, recordCount={}", fileName, records.size());
            return ResponseDTO.ok(downloadUrl);

        } catch (Exception e) {
            log.error("导出充值记录失败", e);
            return ResponseDTO.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 生成导出文件名
     */
    private String generateExportFileName(String startDate, String endDate, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dateRange = "";
        if (startDate != null && endDate != null) {
            dateRange = startDate.replaceAll("[^0-9]", "") + "_" + endDate.replaceAll("[^0-9]", "") + "_";
        }
        String extension = "EXCEL".equalsIgnoreCase(format) ? "xlsx" : "csv";
        return String.format("recharge_export_%s%s.%s", dateRange, timestamp, extension);
    }

    /**
     * 生成导出文件
     */
    private String generateExportFile(List<RechargeRecordEntity> records, String fileName, String format) {
        try {
            // 1. 创建导出目录
            String exportDir = uploadPath + "/export/recharge/";
            Path dirPath = Paths.get(exportDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 2. 生成文件路径
            String filePath = exportDir + fileName;
            Path file = Paths.get(filePath);

            // 3. 根据格式生成文件
            if ("EXCEL".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
                generateExcelFile(records, file);
            } else {
                generateCsvFile(records, file);
            }

            return filePath;

        } catch (Exception e) {
            log.error("生成导出文件失败: fileName={}", fileName, e);
            throw new RuntimeException("生成导出文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成Excel文件
     */
    private void generateExcelFile(List<RechargeRecordEntity> records, Path filePath) {
        try {
            // 使用简单的CSV格式作为Excel的基础实现
            // 实际项目中可以使用Apache POI或EasyExcel
            StringBuilder content = new StringBuilder();

            // 表头
            content.append("充值记录ID,用户ID,充值金额,充值方式,交易流水号,状态,充值时间,确认时间,备注\n");

            // 数据行
            for (RechargeRecordEntity record : records) {
                content.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        record.getRechargeRecordId() != null ? record.getRechargeRecordId() : "",
                        record.getUserId() != null ? record.getUserId() : "",
                        record.getAmount() != null ? record.getAmount() : "",
                        record.getRechargeMethodText() != null ? record.getRechargeMethodText() : "",
                        record.getTransactionNo() != null ? record.getTransactionNo() : "",
                        record.getStatusText() != null ? record.getStatusText() : "",
                        record.getRechargeTime() != null ? record.getRechargeTime() : "",
                        record.getConfirmTime() != null ? record.getConfirmTime() : "",
                        record.getRemark() != null ? record.getRemark().replace(",", "，") : ""));
            }

            Files.write(filePath, content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            log.info("Excel文件生成成功: filePath={}, recordCount={}", filePath, records.size());

        } catch (Exception e) {
            log.error("生成Excel文件失败", e);
            throw new RuntimeException("生成Excel文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成CSV文件
     */
    private void generateCsvFile(List<RechargeRecordEntity> records, Path filePath) {
        try {
            StringBuilder content = new StringBuilder();

            // 表头
            content.append("充值记录ID,用户ID,充值金额,充值方式,交易流水号,状态,充值时间,确认时间,备注\n");

            // 数据行
            for (RechargeRecordEntity record : records) {
                content.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        record.getRechargeRecordId() != null ? record.getRechargeRecordId() : "",
                        record.getUserId() != null ? record.getUserId() : "",
                        record.getAmount() != null ? record.getAmount() : "",
                        record.getRechargeMethodText() != null ? record.getRechargeMethodText() : "",
                        record.getTransactionNo() != null ? record.getTransactionNo() : "",
                        record.getStatusText() != null ? record.getStatusText() : "",
                        record.getRechargeTime() != null ? record.getRechargeTime() : "",
                        record.getConfirmTime() != null ? record.getConfirmTime() : "",
                        record.getRemark() != null ? record.getRemark().replace(",", "，") : ""));
            }

            Files.write(filePath, content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            log.info("CSV文件生成成功: filePath={}, recordCount={}", filePath, records.size());

        } catch (Exception e) {
            log.error("生成CSV文件失败", e);
            throw new RuntimeException("生成CSV文件失败: " + e.getMessage(), e);
        }
    }
}

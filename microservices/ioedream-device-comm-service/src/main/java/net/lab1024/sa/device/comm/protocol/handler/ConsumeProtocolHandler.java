package net.lab1024.sa.device.comm.protocol.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费协议处理器
 * <p>
 * 处理消费设备的协议命令，包括支付、查询、配置管理等操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Component
public class ConsumeProtocolHandler extends BaseProtocolHandler {

    /**
     * 消费协议命令类型常量
     */
    public static final String COMMAND_CONSUME = "CONSUME";
    public static final String COMMAND_REFUND = "REFUND";
    public static final String COMMAND_QUERY_BALANCE = "QUERY_BALANCE";
    public static final String COMMAND_QUERY_CONSUME_RECORD = "QUERY_CONSUME_RECORD";
    public static final String COMMAND_GET_DEVICE_STATUS = "GET_DEVICE_STATUS";
    public static final String COMMAND_SET_DEVICE_CONFIG = "SET_DEVICE_CONFIG";
    public static final String COMMAND_RECHARGE = "RECHARGE";
    public static final String COMMAND_GET_PRODUCT_LIST = "GET_PRODUCT_LIST";
    public static final String COMMAND_SYNC_PRICE = "SYNC_PRICE";

    public ConsumeProtocolHandler() {
        super("CONSUME");
    }

    @Override
    protected void doValidateCommand(DeviceCommandRequest request) {
        String commandType = request.getCommandType();

        // 校验消费协议特定的命令类型
        switch (commandType) {
            case COMMAND_CONSUME:
                validateConsumeCommand(request);
                break;
            case COMMAND_REFUND:
                validateRefundCommand(request);
                break;
            case COMMAND_RECHARGE:
                validateRechargeCommand(request);
                break;
            case COMMAND_QUERY_BALANCE:
                validateQueryBalanceCommand(request);
                break;
            case COMMAND_QUERY_CONSUME_RECORD:
                validateQueryConsumeRecordCommand(request);
                break;
            case COMMAND_SET_DEVICE_CONFIG:
                validateSetDeviceConfigCommand(request);
                break;
            case COMMAND_GET_DEVICE_STATUS:
            case COMMAND_GET_PRODUCT_LIST:
            case COMMAND_SYNC_PRICE:
                // 这些命令只需要基本的参数校验
                break;
            default:
                throw ProtocolProcessException.processFailed(
                        String.format("不支持的消费协议命令类型: %s", commandType));
        }
    }

    @Override
    protected ProtocolProcessResult processCommand(DeviceCommandRequest request) {
        String commandType = request.getCommandType();
        Long deviceId = request.getDeviceId();

        log.info("[消费协议] 执行消费命令: deviceId={}, commandType={}", deviceId, commandType);

        switch (commandType) {
            case COMMAND_CONSUME:
                return processConsumeCommand(request);
            case COMMAND_REFUND:
                return processRefundCommand(request);
            case COMMAND_QUERY_BALANCE:
                return processQueryBalanceCommand(request);
            case COMMAND_QUERY_CONSUME_RECORD:
                return processQueryConsumeRecordCommand(request);
            case COMMAND_GET_DEVICE_STATUS:
                return processGetDeviceStatusCommand(request);
            case COMMAND_SET_DEVICE_CONFIG:
                return processSetDeviceConfigCommand(request);
            case COMMAND_RECHARGE:
                return processRechargeCommand(request);
            case COMMAND_GET_PRODUCT_LIST:
                return processGetProductListCommand(request);
            case COMMAND_SYNC_PRICE:
                return processSyncPriceCommand(request);
            default:
                throw ProtocolProcessException.processFailed(
                        String.format("未实现的消费协议命令: %s", commandType));
        }
    }

    /**
     * 校验消费命令参数
     */
    private void validateConsumeCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null) {
            throw ProtocolProcessException.processFailed("消费命令缺少必要参数");
        }

        if (!commandData.containsKey("userId") || !commandData.containsKey("amount")) {
            throw ProtocolProcessException.processFailed("消费命令缺少用户ID或金额参数");
        }

        Object amount = commandData.get("amount");
        if (!(amount instanceof BigDecimal) && !(amount instanceof Double) && !(amount instanceof String)) {
            throw ProtocolProcessException.processFailed("消费金额参数类型错误");
        }

        BigDecimal consumeAmount = new BigDecimal(String.valueOf(amount));
        if (consumeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw ProtocolProcessException.processFailed("消费金额必须大于0");
        }

        if (consumeAmount.compareTo(new BigDecimal("10000")) > 0) {
            throw ProtocolProcessException.processFailed("消费金额超出单笔限额");
        }
    }

    /**
     * 校验退款命令参数
     */
    private void validateRefundCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || !commandData.containsKey("consumeId")) {
            throw ProtocolProcessException.processFailed("退款命令缺少消费记录ID参数");
        }

        if (!commandData.containsKey("refundAmount")) {
            throw ProtocolProcessException.processFailed("退款命令缺少退款金额参数");
        }

        Object refundAmount = commandData.get("refundAmount");
        if (!(refundAmount instanceof BigDecimal) && !(refundAmount instanceof Double) && !(refundAmount instanceof String)) {
            throw ProtocolProcessException.processFailed("退款金额参数类型错误");
        }
    }

    /**
     * 校验充值命令参数
     */
    private void validateRechargeCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || !commandData.containsKey("userId") || !commandData.containsKey("amount")) {
            throw ProtocolProcessException.processFailed("充值命令缺少用户ID或金额参数");
        }

        Object amount = commandData.get("amount");
        if (!(amount instanceof BigDecimal) && !(amount instanceof Double) && !(amount instanceof String)) {
            throw ProtocolProcessException.processFailed("充值金额参数类型错误");
        }

        BigDecimal rechargeAmount = new BigDecimal(String.valueOf(amount));
        if (rechargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw ProtocolProcessException.processFailed("充值金额必须大于0");
        }

        if (rechargeAmount.compareTo(new BigDecimal("50000")) > 0) {
            throw ProtocolProcessException.processFailed("充值金额超出单笔限额");
        }
    }

    /**
     * 校验查询余额命令参数
     */
    private void validateQueryBalanceCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || !commandData.containsKey("userId")) {
            throw ProtocolProcessException.processFailed("查询余额命令缺少用户ID参数");
        }
    }

    /**
     * 校验查询消费记录命令参数
     */
    private void validateQueryConsumeRecordCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData != null) {
            Object startDate = commandData.get("startDate");
            Object endDate = commandData.get("endDate");

            if (startDate != null && !(startDate instanceof String) && !(startDate instanceof LocalDateTime)) {
                throw ProtocolProcessException.processFailed("开始日期参数类型错误");
            }

            if (endDate != null && !(endDate instanceof String) && !(endDate instanceof LocalDateTime)) {
                throw ProtocolProcessException.processFailed("结束日期参数类型错误");
            }
        }
    }

    /**
     * 校验设置设备配置命令参数
     */
    private void validateSetDeviceConfigCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || commandData.isEmpty()) {
            throw ProtocolProcessException.processFailed("设置设备配置命令缺少配置参数");
        }
    }

    /**
     * 处理消费命令
     */
    private ProtocolProcessResult processConsumeCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));
        BigDecimal amount = new BigDecimal(String.valueOf(commandData.get("amount")));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("amount", amount);
        resultData.put("consumeTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        // 获取其他消费信息
        if (commandData.containsKey("productId")) {
            resultData.put("productId", commandData.get("productId"));
        }
        if (commandData.containsKey("productName")) {
            resultData.put("productName", commandData.get("productName"));
        }

        // 模拟消费处理
        BigDecimal currentBalance = simulateGetCurrentBalance(userId);
        if (currentBalance.compareTo(amount) >= 0) {
            BigDecimal newBalance = currentBalance.subtract(amount);
            resultData.put("consumeId", generateConsumeId());
            resultData.put("success", true);
            resultData.put("oldBalance", currentBalance);
            resultData.put("newBalance", newBalance);

            log.info("[消费协议] 消费成功: deviceId={}, userId={}, amount={}, balance={}",
                    request.getDeviceId(), userId, amount, newBalance);

            return createSuccessResult("消费成功", resultData);
        } else {
            resultData.put("success", false);
            resultData.put("balance", currentBalance);

            log.warn("[消费协议] 消费失败余额不足: deviceId={}, userId={}, amount={}, balance={}",
                    request.getDeviceId(), userId, amount, currentBalance);

            return createFailureResult("INSUFFICIENT_BALANCE", "余额不足");
        }
    }

    /**
     * 处理退款命令
     */
    private ProtocolProcessResult processRefundCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String consumeId = String.valueOf(commandData.get("consumeId"));
        BigDecimal refundAmount = new BigDecimal(String.valueOf(commandData.get("refundAmount")));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("consumeId", consumeId);
        resultData.put("refundAmount", refundAmount);
        resultData.put("refundTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        // 模拟退款处理
        boolean refundSuccess = simulateRefundProcess(consumeId, refundAmount);
        resultData.put("success", refundSuccess);

        if (refundSuccess) {
            log.info("[消费协议] 退款成功: deviceId={}, consumeId={}, refundAmount={}",
                    request.getDeviceId(), consumeId, refundAmount);
            return createSuccessResult("退款成功", resultData);
        } else {
            log.warn("[消费协议] 退款失败: deviceId={}, consumeId={}, refundAmount={}",
                    request.getDeviceId(), consumeId, refundAmount);
            return createFailureResult("REFUND_FAILED", "退款失败");
        }
    }

    /**
     * 处理查询余额命令
     */
    private ProtocolProcessResult processQueryBalanceCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));

        Map<String, Object> resultData = new HashMap<>();
        BigDecimal currentBalance = simulateGetCurrentBalance(userId);
        resultData.put("userId", userId);
        resultData.put("balance", currentBalance);
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[消费协议] 查询余额: deviceId={}, userId={}, balance={}",
                request.getDeviceId(), userId, currentBalance);

        return createSuccessResult("余额查询成功", resultData);
    }

    /**
     * 处理查询消费记录命令
     */
    private ProtocolProcessResult processQueryConsumeRecordCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        resultData.put("deviceId", request.getDeviceId());
        resultData.put("totalRecords", 15); // 模拟数据

        if (commandData != null && commandData.containsKey("userId")) {
            String userId = String.valueOf(commandData.get("userId"));
            resultData.put("userId", userId);
        }

        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[消费协议] 查询消费记录: deviceId={}", request.getDeviceId());

        return createSuccessResult("消费记录查询成功", resultData);
    }

    /**
     * 处理获取设备状态命令
     */
    private ProtocolProcessResult processGetDeviceStatusCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();

        // 模拟获取设备状态
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("isOnline", true);
        resultData.put("paperLevel", 85); // 打印纸余量
        resultData.put("cashboxStatus", "NORMAL"); // 钱箱状态
        resultData.put("lastSyncTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[消费协议] 查询设备状态: deviceId={}", request.getDeviceId());

        return createSuccessResult("设备状态查询成功", resultData);
    }

    /**
     * 处理设置设备配置命令
     */
    private ProtocolProcessResult processSetDeviceConfigCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        resultData.put("deviceId", request.getDeviceId());
        resultData.put("configUpdated", true);
        resultData.put("updatedConfig", commandData);
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[消费协议] 设置设备配置: deviceId={}, config={}",
                request.getDeviceId(), commandData);

        return createSuccessResult("设备配置设置成功", resultData);
    }

    /**
     * 处理充值命令
     */
    private ProtocolProcessResult processRechargeCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));
        BigDecimal amount = new BigDecimal(String.valueOf(commandData.get("amount")));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("amount", amount);
        resultData.put("rechargeTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        // 模拟充值处理
        BigDecimal oldBalance = simulateGetCurrentBalance(userId);
        BigDecimal newBalance = oldBalance.add(amount);
        resultData.put("rechargeId", generateRechargeId());
        resultData.put("success", true);
        resultData.put("oldBalance", oldBalance);
        resultData.put("newBalance", newBalance);

        log.info("[消费协议] 充值成功: deviceId={}, userId={}, amount={}, newBalance={}",
                request.getDeviceId(), userId, amount, newBalance);

        return createSuccessResult("充值成功", resultData);
    }

    /**
     * 处理获取商品列表命令
     */
    private ProtocolProcessResult processGetProductListCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();

        // 模拟获取商品列表
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("productCount", 20); // 模拟商品数量
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[消费协议] 获取商品列表: deviceId={}", request.getDeviceId());

        return createSuccessResult("商品列表获取成功", resultData);
    }

    /**
     * 处理同步价格命令
     */
    private ProtocolProcessResult processSyncPriceCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        resultData.put("deviceId", request.getDeviceId());
        resultData.put("priceSynced", true);
        resultData.put("syncCount", commandData != null ? commandData.size() : 0);
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[消费协议] 同步价格: deviceId={}", request.getDeviceId());

        return createSuccessResult("价格同步成功", resultData);
    }

    /**
     * 模拟获取用户当前余额
     */
    private BigDecimal simulateGetCurrentBalance(String userId) {
        // 这里是模拟实现，实际应该查询数据库
        // 根据用户ID生成不同的模拟余额
        return new BigDecimal(1000 + (userId.hashCode() % 500));
    }

    /**
     * 模拟退款处理
     */
    private boolean simulateRefundProcess(String consumeId, BigDecimal refundAmount) {
        // 这里是模拟实现，实际应该：
        // 1. 验证消费记录是否存在
        // 2. 检查退款金额是否合理
        // 3. 更新用户余额
        // 4. 记录退款数据
        return consumeId != null && !consumeId.trim().isEmpty();
    }

    /**
     * 生成消费记录ID
     */
    private String generateConsumeId() {
        return "CONSUME_" + System.currentTimeMillis();
    }

    /**
     * 生成充值记录ID
     */
    private String generateRechargeId() {
        return "RECHARGE_" + System.currentTimeMillis();
    }
}
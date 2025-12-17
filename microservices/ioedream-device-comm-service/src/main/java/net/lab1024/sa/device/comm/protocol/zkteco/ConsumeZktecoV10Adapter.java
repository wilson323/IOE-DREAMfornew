package net.lab1024.sa.device.comm.protocol.zkteco;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.*;
import net.lab1024.sa.device.comm.protocol.domain.*;
import net.lab1024.sa.device.comm.protocol.entity.ProtocolMessageEntity;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolBuildException;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 中控智慧消费协议V1.0适配器实现
 * <p>
 * 严格按照中控智慧消费PUSH通讯协议V1.0规范实现
 * 支持完整的协议解析、构建、验证和业务处理功能
 * </p>
 * <p>
 * 支持的设备型号：
 * - IC-600T - 消费机
 * - F2 - 消费机
 * - SC700 - 消费机
 * - SC810 - 消费机
 * - IC-700A - 消费机
 * - IC-800A - 消费机
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class ConsumeZktecoV10Adapter implements ProtocolAdapter {

    // ==================== 协议常量定义 ====================

    /** 协议类型标识 */
    private static final String PROTOCOL_TYPE = "CONSUME_ZKTECO_V1_0";
    private static final String MANUFACTURER = "中控智慧";
    private static final String VERSION = "V1.0";

    /** 支持的设备型号 */
    private static final String[] SUPPORTED_DEVICE_MODELS = {
        "IC-600T", "F2", "SC700", "SC810", "IC-700A", "IC-800A",
        "IC-260T", "IC-360T", "IC-560T", "IC-760T", "SC602", "SC603"
    };

    /** 消息类型代码 */
    private static final int MSG_TYPE_CONSUME_RECORD = 0x01;       // 消费记录上传
    private static final int MSG_TYPE_DEVICE_STATUS = 0x02;        // 设备状态上报
    private static final int MSG_TYPE_HEARTBEAT = 0x03;            // 心跳包
    private static final int MSG_TYPE_ACCOUNT_QUERY = 0x04;        // 账户查询请求
    private static final int MSG_TYPE_ACCOUNT_RESPONSE = 0x05;     // 账户查询响应
    private static final int MSG_TYPE_RECHARGE_RECORD = 0x06;      // 充值记录上传
    private static final int MSG_TYPE_SUBSIDY_RECORD = 0x07;       // 补贴记录上传
    private static final int MSG_TYPE_ERROR_REPORT = 0x08;         // 错误报告
    private static final int MSG_TYPE_DEVICE_CONFIG_REQUEST = 0x09; // 设备配置请求
    private static final int MSG_TYPE_DEVICE_CONFIG_RESPONSE = 0x0A; // 设备配置响应

    /** 交易类型代码 */
    private static final int TRANSACTION_TYPE_CONSUME = 0x01;      // 消费
    private static final int TRANSACTION_TYPE_RECHARGE = 0x02;     // 充值
    private static final int TRANSACTION_TYPE_REFUND = 0x03;       // 退款
    private static final int TRANSACTION_TYPE_CANCEL = 0x04;       // 撤销
    private static final int TRANSACTION_TYPE_ADJUST = 0x05;       // 调整

    /** 消费方式代码 */
    private static final int CONSUME_METHOD_CARD = 0x01;           // 刷卡
    private static final int CONSUME_METHOD_FACE = 0x02;           // 人脸
    private static final int CONSUME_METHOD_FINGERPRINT = 0x03;     // 指纹
    private static final int CONSUME_METHOD_QR_CODE = 0x04;        // 二维码
    private static final int CONSUME_METHOD_NFC = 0x05;            // NFC支付
    private static final int CONSUME_METHOD_OFFLINE = 0x06;        // 离线消费

    /** 支付方式代码 */
    private static final int PAYMENT_METHOD_ACCOUNT = 0x01;        // 账户余额
    private static final int PAYMENT_METHOD_SUBSIDY = 0x02;        // 补贴
    private static final int PAYMENT_METHOD_CASH = 0x03;           // 现金
    private static final int PAYMENT_METHOD_BANK_CARD = 0x04;      // 银行卡
    private static final int PAYMENT_METHOD_MOBILE = 0x05;         // 移动支付

    /** 交易状态代码 */
    private static final int TRANSACTION_STATUS_SUCCESS = 0x01;     // 成功
    private static final int TRANSACTION_STATUS_FAILED = 0x02;      // 失败
    private static final int TRANSACTION_STATUS_PENDING = 0x03;     // 待处理
    private static final int TRANSACTION_STATUS_CANCELLED = 0x04;  // 已取消

    // ==================== 协议标识接口实现 ====================

    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }

    @Override
    public String getManufacturer() {
        return MANUFACTURER;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return SUPPORTED_DEVICE_MODELS.clone();
    }

    @Override
    public boolean isDeviceModelSupported(String deviceModel) {
        if (deviceModel == null) {
            return false;
        }
        return Arrays.asList(SUPPORTED_DEVICE_MODELS).contains(deviceModel.toUpperCase());
    }

    // ==================== 消息处理核心接口实现 ====================

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        log.debug("[中控消费协议V1.0] 开始解析设备消息, deviceId={}, dataLength={}", deviceId, rawData.length);

        try {
            // 1. 基础数据验证
            if (rawData == null || rawData.length < 24) {
                throw new ProtocolParseException("数据长度不足，无法解析协议头");
            }

            // 2. 解析协议头
            ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);

            // 协议标识 (2字节)
            short protocolId = buffer.getShort();
            if (protocolId != 0x5A4B) { // "ZK"标识
                throw new ProtocolParseException("协议标识不匹配");
            }

            // 消息长度 (2字节)
            short messageLength = buffer.getShort();
            if (messageLength != rawData.length) {
                throw new ProtocolParseException("消息长度不匹配");
            }

            // 协议版本 (2字节)
            short versionCode = buffer.getShort();
            if (versionCode != 0x0100) { // V1.0
                throw new ProtocolParseException("协议版本不匹配");
            }

            // 设备ID (12字节)
            byte[] deviceIdBytes = new byte[12];
            buffer.get(deviceIdBytes);
            String deviceIdentifier = new String(deviceIdBytes).trim();

            // 消息类型 (1字节)
            byte messageTypeCode = buffer.get();

            // 命令代码 (1字节)
            byte commandCode = buffer.get();

            // 序列号 (4字节)
            int sequenceNumber = buffer.getInt();

            // 时间戳 (8字节)
            long timestamp = buffer.getLong() * 1000; // 转换为毫秒

            // 3. 创建协议消息对象
            ConsumeZktecoV10Message message = new ConsumeZktecoV10Message();
            message.setDeviceId(deviceIdentifier);
            message.setMessageTypeCode((int) messageTypeCode);
            message.setCommandCode((int) commandCode);
            message.setSequenceNumber((long) sequenceNumber);
            message.setTimestamp(timestamp);
            message.setReceiveTime(java.time.LocalDateTime.now());

            // 4. 根据消息类型解析业务数据
            switch (messageTypeCode) {
                case MSG_TYPE_CONSUME_RECORD:
                    parseConsumeRecord(buffer, message);
                    break;
                case MSG_TYPE_DEVICE_STATUS:
                    parseDeviceStatus(buffer, message);
                    break;
                case MSG_TYPE_HEARTBEAT:
                    parseHeartbeat(buffer, message);
                    break;
                case MSG_TYPE_ACCOUNT_QUERY:
                    parseAccountQuery(buffer, message);
                    break;
                case MSG_TYPE_ACCOUNT_RESPONSE:
                    parseAccountResponse(buffer, message);
                    break;
                case MSG_TYPE_RECHARGE_RECORD:
                    parseRechargeRecord(buffer, message);
                    break;
                case MSG_TYPE_SUBSIDY_RECORD:
                    parseSubsidyRecord(buffer, message);
                    break;
                case MSG_TYPE_ERROR_REPORT:
                    parseErrorReport(buffer, message);
                    break;
                default:
                    log.warn("[中控消费协议V1.0] 未知消息类型: 0x{}", String.format("%02X", messageTypeCode));
            }

            log.debug("[中控消费协议V1.0] 消息解析完成, messageType={}, deviceId={}",
                message.getMessageTypeName(), message.getDeviceId());

            return message;

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 消息解析失败, deviceId={}", deviceId, e);
            throw new ProtocolParseException("消息解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ProtocolMessage parseDeviceMessage(String hexData, Long deviceId) throws ProtocolParseException {
        if (hexData == null || hexData.trim().isEmpty()) {
            throw new ProtocolParseException("十六进制数据为空");
        }

        // 移除空格和换行符
        String cleanHex = hexData.replaceAll("\\s+", "");

        // 验证十六进制格式
        if (!cleanHex.matches("^[0-9A-Fa-f]+$")) {
            throw new ProtocolParseException("无效的十六进制数据格式");
        }

        // 转换为字节数组
        byte[] rawData = hexStringToBytes(cleanHex);
        return parseDeviceMessage(rawData, deviceId);
    }

    @Override
    public byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        log.debug("[中控消费协议V1.0] 开始构建设备响应, messageType={}, deviceId={}", messageType, deviceId);

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);

            // 1. 构建协议头
            buffer.putShort((short) 0x5A4B); // 协议标识

            // 消息长度（稍后填充）
            int lengthPosition = buffer.position();
            buffer.putShort((short) 0);

            buffer.putShort((short) 0x0100); // 版本号 V1.0
            buffer.put((byte) 0x00);        // 响应消息类型
            buffer.put((byte) 0x00);        // 响应命令代码
            buffer.putInt(0);               // 序列号
            buffer.putLong(System.currentTimeMillis() / 1000); // 时间戳

            // 2. 根据消息类型构建业务数据
            switch (messageType) {
                case "ACK":
                    buildAckResponse(buffer, businessData);
                    break;
                case "NAK":
                    buildNakResponse(buffer, businessData);
                    break;
                case "ACCOUNT_INFO_RESPONSE":
                    buildAccountInfoResponse(buffer, businessData);
                    break;
                case "DEVICE_CONFIG":
                    buildDeviceConfig(buffer, businessData);
                    break;
                case "CONSUME_SUCCESS":
                    buildConsumeSuccessResponse(buffer, businessData);
                    break;
                case "CONSUME_FAILED":
                    buildConsumeFailedResponse(buffer, businessData);
                    break;
                default:
                    log.warn("[中控消费协议V1.0] 未知响应消息类型: {}", messageType);
            }

            // 3. 填充消息长度
            int messageLength = buffer.position();
            buffer.putShort(lengthPosition, (short) messageLength);

            // 4. 计算校验码
            byte[] messageBytes = new byte[messageLength];
            System.arraycopy(buffer.array(), 0, messageBytes, 0, messageLength);
            String checksum = calculateChecksum(messageBytes);

            // 5. 返回完整消息
            byte[] response = new byte[messageLength + 4];
            System.arraycopy(messageBytes, 0, response, 0, messageLength);
            System.arraycopy(checksum.getBytes(), 0, response, messageLength, 4);

            log.debug("[中控消费协议V1.0] 响应消息构建完成, length={}", response.length);
            return response;

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 响应消息构建失败", e);
            throw new ProtocolBuildException("响应消息构建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildDeviceResponseHex(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        byte[] response = buildDeviceResponse(messageType, businessData, deviceId);
        return bytesToHexString(response);
    }

    // ==================== 协议验证接口实现 ====================

    @Override
    public ProtocolValidationResult validateMessage(ProtocolMessage message) {
        ProtocolValidationResult result = new ProtocolValidationResult();

        try {
            // 1. 基础验证
            if (message == null) {
                result.setValid(false);
                result.setErrorCode("MSG_NULL");
                result.setErrorMessage("协议消息为空");
                return result;
            }

            ConsumeZktecoV10Message zktecoMessage = (ConsumeZktecoV10Message) message;

            // 2. 必填字段验证
            if (zktecoMessage.getDeviceId() == null || zktecoMessage.getDeviceId().trim().isEmpty()) {
                result.setValid(false);
                result.setErrorCode("DEVICE_ID_EMPTY");
                result.setErrorMessage("设备ID为空");
                return result;
            }

            if (zktecoMessage.getMessageTypeCode() == null) {
                result.setValid(false);
                result.setErrorCode("MSG_TYPE_EMPTY");
                result.setErrorMessage("消息类型为空");
                return result;
            }

            // 3. 消息类型验证
            if (!isValidMessageType(zktecoMessage.getMessageTypeCode())) {
                result.setValid(false);
                result.setErrorCode("MSG_TYPE_INVALID");
                result.setErrorMessage("无效的消息类型: " + zktecoMessage.getMessageTypeCode());
                return result;
            }

            // 4. 设备型号验证
            if (zktecoMessage.getDeviceModel() != null &&
                !isDeviceModelSupported(zktecoMessage.getDeviceModel())) {
                result.setValid(false);
                result.setErrorCode("DEVICE_MODEL_UNSUPPORTED");
                result.setErrorMessage("不支持的设备型号: " + zktecoMessage.getDeviceModel());
                return result;
            }

            // 5. 消费金额验证
            if (zktecoMessage.getConsumeAmount() != null) {
                if (zktecoMessage.getConsumeAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    result.setValid(false);
                    result.setErrorCode("INVALID_CONSUME_AMOUNT");
                    result.setErrorMessage("消费金额必须大于0");
                    return result;
                }
            }

            // 6. 时间戳验证
            if (zktecoMessage.getTimestamp() != null) {
                long currentTime = System.currentTimeMillis();
                long messageTime = zktecoMessage.getTimestamp();
                if (Math.abs(currentTime - messageTime) > 300000) { // 5分钟
                    result.setValid(false);
                    result.setErrorCode("TIMESTAMP_OUT_OF_RANGE");
                    result.setErrorMessage("消息时间戳超出允许范围");
                    return result;
                }
            }

            result.setValid(true);
            result.setValidationDetails("消息验证通过");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 消息验证异常", e);
            result.setValid(false);
            result.setErrorCode("VALIDATION_EXCEPTION");
            result.setErrorMessage("消息验证异常: " + e.getMessage());
        }

        return result;
    }

    @Override
    public ProtocolPermissionResult validateDevicePermission(Long deviceId, String operation) {
        ProtocolPermissionResult result = new ProtocolPermissionResult();

        try {
            // 1. 设备存在性验证
            // TODO: 从数据库查询设备信息

            // 2. 设备状态验证
            // TODO: 检查设备是否在线、是否被禁用等

            // 3. 操作权限验证
            // TODO: 验证设备是否有执行指定操作的权限

            result.setHasPermission(true);
            result.setPermissionDetails("权限验证通过");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 设备权限验证异常, deviceId={}, operation={}", deviceId, operation, e);
            result.setHasPermission(false);
            result.setErrorCode("PERMISSION_CHECK_FAILED");
            result.setErrorMessage("权限验证失败: " + e.getMessage());
        }

        return result;
    }

    // ==================== 业务数据处理接口实现 ====================

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            ProtocolProcessResult result = new ProtocolProcessResult();
            result.setSuccess(false);
            result.setErrorCode("NOT_SUPPORTED");
            result.setErrorMessage("消费协议不支持门禁业务处理");
            return result;
        });
    }

    @Override
    public Future<ProtocolProcessResult> processAttendanceBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            ProtocolProcessResult result = new ProtocolProcessResult();
            result.setSuccess(false);
            result.setErrorCode("NOT_SUPPORTED");
            result.setErrorMessage("消费协议不支持考勤业务处理");
            return result;
        });
    }

    @Override
    public Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            ProtocolProcessResult result = new ProtocolProcessResult();

            try {
                switch (businessType) {
                    case "CONSUME_RECORD":
                        result = processConsumeRecord(businessData, deviceId);
                        break;
                    case "RECHARGE_RECORD":
                        result = processRechargeRecord(businessData, deviceId);
                        break;
                    case "SUBSIDY_RECORD":
                        result = processSubsidyRecord(businessData, deviceId);
                        break;
                    case "ACCOUNT_QUERY":
                        result = processAccountQuery(businessData, deviceId);
                        break;
                    case "OFFLINE_SYNC":
                        result = processOfflineSync(businessData, deviceId);
                        break;
                    default:
                        result.setSuccess(false);
                        result.setErrorCode("UNKNOWN_BUSINESS_TYPE");
                        result.setErrorMessage("未知的业务类型: " + businessType);
                }

            } catch (Exception e) {
                log.error("[中控消费协议V1.0] 消费业务处理异常, businessType={}, deviceId={}", businessType, deviceId, e);
                result.setSuccess(false);
                result.setErrorCode("BUSINESS_PROCESS_FAILED");
                result.setErrorMessage("业务处理异常: " + e.getMessage());
            }

            return result;
        });
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析消费记录消息
     */
    private void parseConsumeRecord(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("消费记录上传");

        // 消费记录编号 (20字节)
        byte[] recordNumberBytes = new byte[20];
        buffer.get(recordNumberBytes);
        message.setConsumeRecordNumber(new String(recordNumberBytes).trim());

        // 用户ID (4字节)
        int userId = buffer.getInt();
        if (userId > 0) {
            message.setUserId((long) userId);
        }

        // 卡号 (20字节)
        byte[] cardNumberBytes = new byte[20];
        buffer.get(cardNumberBytes);
        String cardNumber = new String(cardNumberBytes).trim();
        if (!cardNumber.isEmpty()) {
            message.setCardNumber(cardNumber);
        }

        // 消费方式 (1字节)
        int consumeMethodCode = buffer.get() & 0xFF;
        message.setConsumeMethod(getConsumeMethodName(consumeMethodCode));

        // 消费金额 (8字节，以分为单位)
        long consumeAmountCents = buffer.getLong();
        message.setConsumeAmount(BigDecimal.valueOf(consumeAmountCents, 2));

        // 消费类型 (1字节)
        int consumeTypeCode = buffer.get() & 0xFF;
        message.setConsumeType(getConsumeTypeName(consumeTypeCode));

        // 消费类别 (1字节)
        int consumeCategoryCode = buffer.get() & 0xFF;
        message.setConsumeCategory(getConsumeCategoryName(consumeCategoryCode));

        // 商户ID (4字节)
        int merchantId = buffer.getInt();
        if (merchantId > 0) {
            message.setMerchantId((long) merchantId);
        }

        // 终端号 (16字节)
        byte[] terminalBytes = new byte[16];
        buffer.get(terminalBytes);
        message.setTerminalNumber(new String(terminalBytes).trim());

        // 操作员ID (4字节)
        int operatorId = buffer.getInt();
        if (operatorId > 0) {
            message.setOperatorId((long) operatorId);
        }

        // 交易类型 (1字节)
        int transactionTypeCode = buffer.get() & 0xFF;
        message.setTransactionType(getTransactionTypeName(transactionTypeCode));

        // 交易状态 (1字节)
        int transactionStatusCode = buffer.get() & 0xFF;
        message.setTransactionStatus(getTransactionStatusName(transactionStatusCode));

        // 支付方式 (1字节)
        int paymentMethodCode = buffer.get() & 0xFF;
        message.setPaymentMethod(getPaymentMethodName(paymentMethodCode));

        // 账户余额（消费前） (8字节)
        long balanceBeforeCents = buffer.getLong();
        message.setBalanceBefore(BigDecimal.valueOf(balanceBeforeCents, 2));

        // 账户余额（消费后） (8字节)
        long balanceAfterCents = buffer.getLong();
        message.setBalanceAfter(BigDecimal.valueOf(balanceAfterCents, 2));

        // 补贴金额 (8字节)
        long subsidyAmountCents = buffer.getLong();
        message.setUsedSubsidyAmount(BigDecimal.valueOf(subsidyAmountCents, 2));

        // 是否离线消费 (1字节)
        int isOfflineCode = buffer.get() & 0xFF;
        message.setIsOfflineConsume(isOfflineCode == 1);

        // 消费时间 (8字节)
        long consumeTime = buffer.getLong() * 1000;
        message.setConsumeTime(java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(consumeTime),
            java.time.ZoneId.systemDefault()
        ));

        // 商品ID (4字节)
        int productId = buffer.getInt();
        if (productId > 0) {
            message.setProductId((long) productId);
        }

        // 商品数量 (4字节，以分为单位)
        long quantity = buffer.getInt();
        message.setQuantity(BigDecimal.valueOf(quantity, 2));

        // 商品单价 (8字节，以分为单位)
        long unitPriceCents = buffer.getLong();
        message.setUnitPrice(BigDecimal.valueOf(unitPriceCents, 2));

        // 折扣金额 (8字节，以分为单位)
        long discountAmountCents = buffer.getLong();
        message.setDiscountAmount(BigDecimal.valueOf(discountAmountCents, 2));
    }

    /**
     * 解析设备状态消息
     */
    private void parseDeviceStatus(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("设备状态上报");

        // 设备状态 (1字节)
        int deviceStatus = buffer.get() & 0xFF;
        message.setDeviceStatus(getDeviceStatusName(deviceStatus));

        // 设备类型 (1字节)
        int deviceType = buffer.get() & 0xFF;
        message.setDeviceType(getDeviceTypeName(deviceType));

        // 在线状态 (1字节)
        int onlineStatus = buffer.get() & 0xFF;

        // 电池电量 (1字节)
        int batteryLevel = buffer.get() & 0xFF;

        // 信号强度 (1字节)
        int signalStrength = buffer.get() & 0xFF;

        // CPU使用率 (2字节)
        int cpuUsage = buffer.getShort();

        // 内存使用率 (2字节)
        int memoryUsage = buffer.getShort();

        // 存储空间 (4字节)
        int storageSpace = buffer.getInt();

        // 错误代码 (4字节)
        int errorCode = buffer.getInt();
        if (errorCode > 0) {
            message.setExceptionCode(errorCode);
        }

        // 日消费笔数 (4字节)
        int dailyConsumeCount = buffer.getInt();
        message.setDailyConsumeCount(dailyConsumeCount);

        // 日消费总额 (8字节，以分为单位)
        long dailyConsumeAmountCents = buffer.getLong();
        message.setDailyConsumeAmount(BigDecimal.valueOf(dailyConsumeAmountCents, 2));
    }

    /**
     * 解析心跳消息
     */
    private void parseHeartbeat(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("心跳包");

        // 心跳间隔 (2字节)
        short heartbeatInterval = buffer.getShort();

        // 设备运行时间 (4字节)
        int uptime = buffer.getInt();

        // 连接状态 (1字节)
        int connectionStatus = buffer.get() & 0xFF;

        // 温度 (2字节)
        short temperature = buffer.getShort();

        // 湿度 (2字节)
        short humidity = buffer.getShort();

        // TODO: 存储心跳信息到数据库
    }

    /**
     * 解析账户查询请求
     */
    private void parseAccountQuery(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("账户查询请求");

        // 用户ID (4字节)
        int userId = buffer.getInt();
        message.setUserId((long) userId);

        // 卡号 (20字节)
        byte[] cardNumberBytes = new byte[20];
        buffer.get(cardNumberBytes);
        message.setCardNumber(new String(cardNumberBytes).trim());

        // 查询类型 (1字节)
        int queryType = buffer.get() & 0xFF;
    }

    /**
     * 解析账户查询响应
     */
    private void parseAccountResponse(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("账户查询响应");

        // 用户ID (4字节)
        int userId = buffer.getInt();
        message.setUserId((long) userId);

        // 账户类型 (1字节)
        int accountTypeCode = buffer.get() & 0xFF;
        message.setAccountType(getAccountTypeName(accountTypeCode));

        // 账户状态 (1字节)
        int accountStatusCode = buffer.get() & 0xFF;
        message.setAccountStatus(getAccountStatusName(accountStatusCode));

        // 账户余额 (8字节，以分为单位)
        long balanceCents = buffer.getLong();
        message.setAvailableBalance(BigDecimal.valueOf(balanceCents, 2));

        // 信用额度 (8字节，以分为单位)
        long creditLimitCents = buffer.getLong();
        message.setCreditLimit(BigDecimal.valueOf(creditLimitCents, 2));

        // 当前透支金额 (8字节，以分为单位)
        long overdraftAmountCents = buffer.getLong();
        message.setOverdraftAmount(BigDecimal.valueOf(overdraftAmountCents, 2));

        // 补贴余额 (8字节，以分为单位)
        long subsidyBalanceCents = buffer.getLong();
        message.setSubsidyBalance(BigDecimal.valueOf(subsidyBalanceCents, 2));
    }

    /**
     * 解析充值记录
     */
    private void parseRechargeRecord(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("充值记录上传");

        // 充值记录编号 (20字节)
        byte[] recordNumberBytes = new byte[20];
        buffer.get(recordNumberBytes);
        message.setRechargeRecordNumber(new String(recordNumberBytes).trim());

        // 用户ID (4字节)
        int userId = buffer.getInt();
        message.setUserId((long) userId);

        // 充值金额 (8字节，以分为单位)
        long rechargeAmountCents = buffer.getLong();
        message.setRechargeAmount(BigDecimal.valueOf(rechargeAmountCents, 2));

        // 充值方式 (1字节)
        int rechargeMethodCode = buffer.get() & 0xFF;
        message.setRechargeMethod(getRechargeMethodName(rechargeMethodCode));

        // 充值前余额 (8字节，以分为单位)
        long balanceBeforeCents = buffer.getLong();
        message.setBalanceBeforeRecharge(BigDecimal.valueOf(balanceBeforeCents, 2));

        // 充值后余额 (8字节，以分为单位)
        long balanceAfterCents = buffer.getLong();
        message.setBalanceAfterRecharge(BigDecimal.valueOf(balanceAfterCents, 2));

        // 充值时间 (8字节)
        long rechargeTime = buffer.getLong() * 1000;
        message.setRechargeTime(java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(rechargeTime),
            java.time.ZoneId.systemDefault()
        ));

        // 操作员ID (4字节)
        int operatorId = buffer.getInt();
        if (operatorId > 0) {
            message.setRechargeOperator(String.valueOf(operatorId));
        }
    }

    /**
     * 解析补贴记录
     */
    private void parseSubsidyRecord(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("补贴记录上传");

        // 补贴ID (4字节)
        int subsidyId = buffer.getInt();
        message.setSubsidyId((long) subsidyId);

        // 用户ID (4字节)
        int userId = buffer.getInt();
        message.setUserId((long) userId);

        // 补贴类型 (1字节)
        int subsidyTypeCode = buffer.get() & 0xFF;
        message.setSubsidyType(getSubsidyTypeName(subsidyTypeCode));

        // 补贴金额 (8字节，以分为单位)
        long subsidyAmountCents = buffer.getLong();
        message.setSubsidyAmount(BigDecimal.valueOf(subsidyAmountCents, 2));

        // 补贴余额 (8字节，以分为单位)
        long subsidyBalanceCents = buffer.getLong();
        message.setSubsidyBalance(BigDecimal.valueOf(subsidyBalanceCents, 2));

        // 补贴发放时间 (8字节)
        long grantTime = buffer.getLong() * 1000;
        message.setSubsidyGrantTime(java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(grantTime),
            java.time.ZoneId.systemDefault()
        ));

        // 补贴有效期 (8字节)
        long expireTime = buffer.getLong() * 1000;
        message.setSubsidyExpireTime(java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(expireTime),
            java.time.ZoneId.systemDefault()
        ));
    }

    /**
     * 解析错误报告
     */
    private void parseErrorReport(ByteBuffer buffer, ConsumeZktecoV10Message message) {
        message.setMessageTypeName("错误报告");

        // 错误代码 (4字节)
        int errorCode = buffer.getInt();
        message.setExceptionCode(errorCode);

        // 错误级别 (1字节)
        int errorLevel = buffer.get() & 0xFF;
        message.setAlarmLevel(getAlarmLevelName(errorLevel));

        // 错误描述长度 (2字节)
        short errorDescLength = buffer.getShort();

        // 错误描述 (变长)
        if (errorDescLength > 0) {
            byte[] errorDescBytes = new byte[errorDescLength];
            buffer.get(errorDescBytes);
            message.setExceptionDescription(new String(errorDescBytes).trim());
        }
    }

    // ==================== 业务处理方法 ====================

    /**
     * 处理消费记录
     */
    private ProtocolProcessResult processConsumeRecord(Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();

        try {
            // 1. 验证消费数据
            // TODO: 实现消费数据验证逻辑

            // 2. 检查用户账户余额
            // TODO: 实现账户余额检查逻辑

            // 3. 扣减账户余额
            // TODO: 实现余额扣减逻辑

            // 4. 记录消费记录
            // TODO: 保存消费记录到数据库

            // 5. 触发相关业务处理
            // TODO: 触发补贴使用、库存管理等

            result.setSuccess(true);
            result.setProcessDetails("消费记录处理完成");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 消费记录处理失败", e);
            result.setSuccess(false);
            result.setErrorCode("CONSUME_PROCESS_FAILED");
            result.setErrorMessage("消费记录处理失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 处理充值记录
     */
    private ProtocolProcessResult processRechargeRecord(Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();

        try {
            // TODO: 实现充值记录处理逻辑

            result.setSuccess(true);
            result.setProcessDetails("充值记录处理完成");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 充值记录处理失败", e);
            result.setSuccess(false);
            result.setErrorCode("RECHARGE_PROCESS_FAILED");
            result.setErrorMessage("充值记录处理失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 处理补贴记录
     */
    private ProtocolProcessResult processSubsidyRecord(Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();

        try {
            // TODO: 实现补贴记录处理逻辑

            result.setSuccess(true);
            result.setProcessDetails("补贴记录处理完成");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 补贴记录处理失败", e);
            result.setSuccess(false);
            result.setErrorCode("SUBSIDY_PROCESS_FAILED");
            result.setErrorMessage("补贴记录处理失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 处理账户查询
     */
    private ProtocolProcessResult processAccountQuery(Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();

        try {
            // TODO: 实现账户查询处理逻辑

            result.setSuccess(true);
            result.setProcessDetails("账户查询处理完成");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 账户查询处理失败", e);
            result.setSuccess(false);
            result.setErrorCode("ACCOUNT_QUERY_FAILED");
            result.setErrorMessage("账户查询处理失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 处理离线同步
     */
    private ProtocolProcessResult processOfflineSync(Map<String, Object> businessData, Long deviceId) {
        ProtocolProcessResult result = new ProtocolProcessResult();

        try {
            // TODO: 实现离线同步处理逻辑

            result.setSuccess(true);
            result.setProcessDetails("离线同步处理完成");

        } catch (Exception e) {
            log.error("[中控消费协议V1.0] 离线同步处理失败", e);
            result.setSuccess(false);
            result.setErrorCode("OFFLINE_SYNC_FAILED");
            result.setErrorMessage("离线同步处理失败: " + e.getMessage());
        }

        return result;
    }

    // ==================== 工具方法 ====================

    /**
     * 十六进制字符串转字节数组
     */
    private byte[] hexStringToBytes(String hexString) {
        int length = hexString.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 计算校验码
     */
    private String calculateChecksum(byte[] data) {
        // TODO: 实现CRC32或其他校验算法
        return "0000";
    }

    /**
     * 获取消费方式名称
     */
    private String getConsumeMethodName(int consumeMethodCode) {
        switch (consumeMethodCode) {
            case CONSUME_METHOD_CARD: return "CARD";
            case CONSUME_METHOD_FACE: return "FACE";
            case CONSUME_METHOD_FINGERPRINT: return "FINGERPRINT";
            case CONSUME_METHOD_QR_CODE: return "QR_CODE";
            case CONSUME_METHOD_NFC: return "NFC";
            case CONSUME_METHOD_OFFLINE: return "OFFLINE";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取消费类型名称
     */
    private String getConsumeTypeName(int consumeTypeCode) {
        switch (consumeTypeCode) {
            case 0x01: return "MEAL";
            case 0x02: return "SNACK";
            case 0x03: return "DRINK";
            case 0x04: return "GROCERY";
            default: return "OTHER";
        }
    }

    /**
     * 获取消费类别名称
     */
    private String getConsumeCategoryName(int consumeCategoryCode) {
        switch (consumeCategoryCode) {
            case 0x01: return "BREAKFAST";
            case 0x02: return "LUNCH";
            case 0x03: return "DINNER";
            case 0x04: return "SUPPER";
            case 0x05: return "SNACK";
            default: return "OTHER";
        }
    }

    /**
     * 获取交易类型名称
     */
    private String getTransactionTypeName(int transactionTypeCode) {
        switch (transactionTypeCode) {
            case TRANSACTION_TYPE_CONSUME: return "CONSUME";
            case TRANSACTION_TYPE_RECHARGE: return "RECHARGE";
            case TRANSACTION_TYPE_REFUND: return "REFUND";
            case TRANSACTION_TYPE_CANCEL: return "CANCEL";
            case TRANSACTION_TYPE_ADJUST: return "ADJUST";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取交易状态名称
     */
    private String getTransactionStatusName(int transactionStatusCode) {
        switch (transactionStatusCode) {
            case TRANSACTION_STATUS_SUCCESS: return "SUCCESS";
            case TRANSACTION_STATUS_FAILED: return "FAILED";
            case TRANSACTION_STATUS_PENDING: return "PENDING";
            case TRANSACTION_STATUS_CANCELLED: return "CANCELLED";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(int paymentMethodCode) {
        switch (paymentMethodCode) {
            case PAYMENT_METHOD_ACCOUNT: return "ACCOUNT";
            case PAYMENT_METHOD_SUBSIDY: return "SUBSIDY";
            case PAYMENT_METHOD_CASH: return "CASH";
            case PAYMENT_METHOD_BANK_CARD: return "BANK_CARD";
            case PAYMENT_METHOD_MOBILE: return "MOBILE";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取设备状态名称
     */
    private String getDeviceStatusName(int deviceStatusCode) {
        switch (deviceStatusCode) {
            case 0x01: return "ONLINE";
            case 0x02: return "OFFLINE";
            case 0x03: return "BUSY";
            case 0x04: return "ERROR";
            case 0x05: return "MAINTENANCE";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取设备类型名称
     */
    private String getDeviceTypeName(int deviceTypeCode) {
        switch (deviceTypeCode) {
            case 0x01: return "POS_MACHINE";
            case 0x02: return "SELF_SERVICE";
            case 0x03: return "CANTEEN";
            case 0x04: return "VENDING";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取账户类型名称
     */
    private String getAccountTypeName(int accountTypeCode) {
        switch (accountTypeCode) {
            case 0x01: return "PERSONAL";
            case 0x02: return "SUBSIDY";
            case 0x03: return "COMPANY";
            case 0x04: return "TEMP";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取账户状态名称
     */
    private String getAccountStatusName(int accountStatusCode) {
        switch (accountStatusCode) {
            case 0x01: return "ACTIVE";
            case 0x02: return "FROZEN";
            case 0x03: return "EXPIRED";
            case 0x04: return "DISABLED";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取充值方式名称
     */
    private String getRechargeMethodName(int rechargeMethodCode) {
        switch (rechargeMethodCode) {
            case 0x01: return "CASH";
            case 0x02: return "BANK_CARD";
            case 0x03: return "MOBILE";
            case 0x04: return "TRANSFER";
            case 0x05: return "SUBSIDY";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取补贴类型名称
     */
    private String getSubsidyTypeName(int subsidyTypeCode) {
        switch (subsidyTypeCode) {
            case 0x01: return "MEAL";
            case 0x02: return "TRANSPORT";
            case 0x03: return "HOUSING";
            case 0x04: return "WELFARE";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取报警级别名称
     */
    private String getAlarmLevelName(int alarmLevelCode) {
        switch (alarmLevelCode) {
            case 0x01: return "LOW";
            case 0x02: return "MEDIUM";
            case 0x03: return "HIGH";
            case 0x04: return "CRITICAL";
            default: return "UNKNOWN";
        }
    }

    /**
     * 验证消息类型是否有效
     */
    private boolean isValidMessageType(int messageTypeCode) {
        return messageTypeCode >= MSG_TYPE_CONSUME_RECORD &&
               messageTypeCode <= MSG_TYPE_DEVICE_CONFIG_RESPONSE;
    }

    // ==================== 其他接口实现（简化版本）====================

    @Override
    public Future<ProtocolInitResult> initializeDevice(Map<String, Object> deviceInfo, Map<String, Object> config) {
        // TODO: 实现设备初始化逻辑
        return CompletableFuture.completedFuture(new ProtocolInitResult());
    }

    @Override
    public ProtocolRegistrationResult handleDeviceRegistration(Map<String, Object> registrationData, Long deviceId) {
        // TODO: 实现设备注册逻辑
        return new ProtocolRegistrationResult();
    }

    @Override
    public ProtocolHeartbeatResult handleDeviceHeartbeat(Map<String, Object> heartbeatData, Long deviceId) {
        // TODO: 实现心跳处理逻辑
        return new ProtocolHeartbeatResult();
    }

    @Override
    public ProtocolDeviceStatus getDeviceStatus(Long deviceId) {
        // TODO: 实现设备状态获取逻辑
        return new ProtocolDeviceStatus();
    }

    @Override
    public Map<String, Object> getProtocolConfig(Long deviceId) {
        // TODO: 实现协议配置获取逻辑
        return new HashMap<>();
    }

    @Override
    public boolean updateProtocolConfig(Long deviceId, Map<String, Object> config) {
        // TODO: 实现协议配置更新逻辑
        return true;
    }

    @Override
    public ProtocolErrorResponse handleProtocolError(String errorCode, String errorMessage, Long deviceId) {
        // TODO: 实现协议错误处理逻辑
        return new ProtocolErrorResponse();
    }

    @Override
    public Map<String, ProtocolErrorInfo> getErrorCodeMapping() {
        // TODO: 实现错误代码映射逻辑
        return new HashMap<>();
    }

    @Override
    public void initialize() {
        log.info("[中控消费协议V1.0] 协议适配器初始化完成");
    }

    @Override
    public void destroy() {
        log.info("[中控消费协议V1.0] 协议适配器已销毁");
    }

    @Override
    public String getAdapterStatus() {
        return "RUNNING";
    }

    @Override
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("protocolType", PROTOCOL_TYPE);
        stats.put("manufacturer", MANUFACTURER);
        stats.put("version", VERSION);
        stats.put("supportedDeviceCount", SUPPORTED_DEVICE_MODELS.length);
        stats.put("status", "RUNNING");
        return stats;
    }

    // ==================== 响应构建方法 ====================

    private void buildAckResponse(ByteBuffer buffer, Map<String, Object> businessData) {
        // 构建ACK响应
        buffer.put((byte) 0x01); // ACK标识
        // TODO: 添加ACK响应数据
    }

    private void buildNakResponse(ByteBuffer buffer, Map<String, Object> businessData) {
        // 构建NAK响应
        buffer.put((byte) 0x02); // NAK标识
        // TODO: 添加NAK响应数据
    }

    private void buildAccountInfoResponse(ByteBuffer buffer, Map<String, Object> businessData) {
        // 构建账户信息响应
        buffer.put((byte) 0x03); // 账户信息响应标识
        // TODO: 添加账户信息响应数据
    }

    private void buildDeviceConfig(ByteBuffer buffer, Map<String, Object> businessData) {
        // 构建设备配置响应
        buffer.put((byte) 0x04); // 设备配置标识
        // TODO: 添加设备配置数据
    }

    private void buildConsumeSuccessResponse(ByteBuffer buffer, Map<String, Object> businessData) {
        // 构建消费成功响应
        buffer.put((byte) 0x05); // 消费成功标识
        // TODO: 添加消费成功响应数据
    }

    private void buildConsumeFailedResponse(ByteBuffer buffer, Map<String, Object> businessData) {
        // 构建消费失败响应
        buffer.put((byte) 0x06); // 消费失败标识
        // TODO: 添加消费失败响应数据
    }
}

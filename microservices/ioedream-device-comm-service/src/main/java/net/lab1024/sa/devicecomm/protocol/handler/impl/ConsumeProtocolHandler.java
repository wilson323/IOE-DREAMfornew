package net.lab1024.sa.devicecomm.protocol.handler.impl;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.devicecomm.cache.ProtocolCacheManager;
import net.lab1024.sa.devicecomm.monitor.ProtocolMetricsCollector;
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;

/**
 * 消费PUSH协议处理器（中控智慧 V1.0）
 * <p>
 * 实现消费设备的PUSH协议处理
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解，由Spring管理
 * - 完整的函数级注释
 * </p>
 * <p>
 * 协议规范：
 * - 协议名称：消费PUSH通讯协议
 * - 厂商：中控智慧
 * - 版本：V1.0
 * - 文档：消费PUSH通讯协议 （中控智慧） V1.0-20181225
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ConsumeProtocolHandler implements ProtocolHandler {

    /**
     * 协议类型
     */
    private static final String PROTOCOL_TYPE = ProtocolTypeEnum.CONSUME_ZKTECO_V1_0.getCode();

    /**
     * 协议头标识（根据实际协议文档定义）
     */
    private static final byte[] PROTOCOL_HEADER = {0x7E, (byte) 0x81};

    /**
     * 最小消息长度（字节）
     * <p>
     * 注意：当前协议使用HTTP文本格式，此字段保留用于未来可能的二进制协议支持
     * </p>
     */
    @SuppressWarnings("unused")
    private static final int MIN_MESSAGE_LENGTH = 28;

    /**
     * 校验和位置（根据实际协议文档，假设在消息末尾前2字节）
     * 注意：HTTP协议不需要校验和，此字段保留用于兼容性
     */
    @SuppressWarnings("unused")
    private static final int CHECKSUM_POSITION_OFFSET = 2;

    /**
     * 网关服务客户端（用于调用其他微服务）
     */
    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * RabbitMQ消息模板（用于发送消息到队列）
     */
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 协议监控指标收集器
     */
    @Resource
    private ProtocolMetricsCollector metricsCollector;

    /**
     * 协议缓存管理器（多级缓存）
     */
    @Resource
    private ProtocolCacheManager cacheManager;

    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }

    @Override
    public String getManufacturer() {
        return ProtocolTypeEnum.CONSUME_ZKTECO_V1_0.getManufacturer();
    }

    @Override
    public String getVersion() {
        return ProtocolTypeEnum.CONSUME_ZKTECO_V1_0.getVersion();
    }

    /**
     * 解析协议消息（二进制格式）
     * <p>
     * <b>注意：当前协议使用HTTP文本格式，此方法保留用于未来可能的二进制协议支持。</b>
     * 实际解析请使用 {@link #parseMessage(String)} 方法。
     * </p>
     * <p>
     * 根据"消费PUSH通讯协议 （中控智慧） V1.0-20181225"文档，
     * 当前实现使用HTTP POST文本格式（制表符分隔），
     * 二进制格式解析方法保留用于兼容性。
     * </p>
     */
    @Override
    public ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException {
        log.warn("[消费协议] 二进制解析方法被调用，当前协议使用HTTP文本格式，请使用parseMessage(String)方法");
        
        // 将字节数组转换为字符串，委托给文本解析方法
        if (rawData == null || rawData.length == 0) {
            throw new ProtocolParseException("INVALID_DATA", "消息数据为空", rawData);
        }
        
        try {
            String textData = new String(rawData, StandardCharsets.UTF_8);
            return parseMessage(textData);
        } catch (Exception e) {
            log.error("[消费协议] 二进制转文本失败，错误={}", e.getMessage(), e);
            throw new ProtocolParseException("CONVERT_ERROR", "二进制数据转换为文本失败：" + e.getMessage(), rawData);
        }
    }

    /**
     * 解析协议消息（字符串格式）
     * <p>
     * 根据"消费PUSH通讯协议 （中控智慧） V1.0-20181225"文档实现
     * 协议格式：HTTP POST，数据格式为制表符分隔的文本
     * 参考文档: docs/PROTOCOL_FORMAT_ANALYSIS.md
     * </p>
     * <p>
     * 消费记录数据格式（BUYLOG表，单钱包）:
     * {SysID}\t{CARDNO}\t{PosTime}\t{PosMoney}\t{Balance}\t{CardRecID}\t{State}\t{MealType}\t{MealDate}\t{RecNo}\t{OPID}
     * </p>
     * <p>
     * 双钱包格式额外字段:
     * {SubPosMoney}\t{SubBalance}\t{User_PIN}
     * </p>
     */
    @Override
    public ProtocolMessage parseMessage(String rawData) throws ProtocolParseException {
        log.debug("[消费协议] 解析HTTP文本消息，数据长度={}", rawData != null ? rawData.length() : 0);

        try {
            if (rawData == null || rawData.trim().isEmpty()) {
                throw new ProtocolParseException("INVALID_DATA", "消息数据为空", null);
            }

            // 创建协议消息对象
            ProtocolMessage message = new ProtocolMessage();
            message.setProtocolType(PROTOCOL_TYPE);
            message.setTimestamp(LocalDateTime.now());
            message.setRawDataHex(rawData);
            message.setStatus("PARSED");

            // 解析制表符分隔的文本格式
            // 多条记录使用换行符分隔，先处理单条记录
            String[] lines = rawData.split("\n");
            if (lines.length == 0) {
                throw new ProtocolParseException("INVALID_FORMAT", "消息格式错误：无数据行", null);
            }

            // 支持多条记录处理
            // 多条记录使用换行符分隔，每条记录都是独立的消费记录
            java.util.List<Map<String, Object>> recordsList = new java.util.ArrayList<>();
            
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                
                String trimmedLine = line.trim();
                String[] fields = trimmedLine.split("\t");

                Map<String, Object> recordData = new HashMap<>();

                // 根据协议文档，单钱包字段顺序为：
                // SysID, CARDNO, PosTime, PosMoney, Balance, CardRecID, State, MealType, MealDate, RecNo, OPID
                if (fields.length >= 1) {
                    recordData.put("sysID", fields[0].trim()); // 系统ID
                }
                if (fields.length >= 2) {
                    recordData.put("cardNo", fields[1].trim()); // 卡号
                }
                if (fields.length >= 3) {
                    recordData.put("posTime", fields[2].trim()); // 消费时间（Unix时间戳，秒）
                }
                if (fields.length >= 4) {
                    recordData.put("posMoney", fields[3].trim()); // 消费金额（单位：分）
                }
                if (fields.length >= 5) {
                    recordData.put("balance", fields[4].trim()); // 余额（单位：分）
                }
                if (fields.length >= 6) {
                    recordData.put("cardRecID", fields[5].trim()); // 卡流水号
                }
                if (fields.length >= 7) {
                    recordData.put("state", fields[6].trim()); // 消费类型
                }
                if (fields.length >= 8) {
                    recordData.put("mealType", fields[7].trim()); // 餐别
                }
                if (fields.length >= 9) {
                    recordData.put("mealDate", fields[8].trim()); // 记餐日期
                }
                if (fields.length >= 10) {
                    recordData.put("recNo", fields[9].trim()); // 机器流水号
                }
                if (fields.length >= 11) {
                    recordData.put("opid", fields[10].trim()); // 操作员ID
                }

                // 判断是否为双钱包格式（14个字段）
                if (fields.length >= 14) {
                    recordData.put("subPosMoney", fields[11].trim()); // 子钱包消费金额
                    recordData.put("subBalance", fields[12].trim()); // 子钱包余额
                    recordData.put("userPIN", fields[13].trim()); // 用户工号
                    recordData.put("walletType", "DUAL"); // 标记为双钱包
                } else {
                    recordData.put("walletType", "SINGLE"); // 标记为单钱包
                }

                recordsList.add(recordData);
            }

            if (recordsList.isEmpty()) {
                throw new ProtocolParseException("INVALID_FORMAT", "消息格式错误：无有效数据行", null);
            }

            // 确定消息类型
            message.setMessageType("CONSUME_RECORD");

            // 提取设备编号（从第一条记录的cardNo）
            String deviceCode = recordsList.get(0).getOrDefault("cardNo", "UNKNOWN").toString();
            message.setDeviceCode(deviceCode);

            // 设置解析后的数据（包含多条记录）
            Map<String, Object> data = new HashMap<>();
            data.put("records", recordsList); // 多条记录列表
            data.put("recordCount", recordsList.size()); // 记录数量
            // 为了兼容性，也保留第一条记录的直接字段
            if (!recordsList.isEmpty()) {
                data.putAll(recordsList.get(0));
            }
            message.setData(data);

            log.info("[消费协议] HTTP文本消息解析成功，消息类型={}, 记录数={}, 卡号={}", 
                    message.getMessageType(), recordsList.size(), 
                    recordsList.isEmpty() ? "N/A" : recordsList.get(0).get("cardNo"));
            return message;

        } catch (ProtocolParseException e) {
            throw e;
        } catch (Exception e) {
            log.error("[消费协议] HTTP文本消息解析异常，错误={}", e.getMessage(), e);
            throw new ProtocolParseException("PARSE_STRING_ERROR", "HTTP文本消息解析失败：" + e.getMessage(), null);
        }
    }

    @Override
    public boolean validateMessage(ProtocolMessage message) {
        if (message == null) {
            log.warn("[消费协议] 消息验证失败：消息为空");
            return false;
        }

        // 验证消息类型
        if (message.getMessageType() == null || message.getMessageType().isEmpty()) {
            log.warn("[消费协议] 消息验证失败：消息类型为空");
            return false;
        }

        // 验证设备编号
        if (message.getDeviceCode() == null || message.getDeviceCode().isEmpty()) {
            log.warn("[消费协议] 消息验证失败：设备编号为空");
            return false;
        }

        // 验证数据
        if (message.getData() == null || message.getData().isEmpty()) {
            log.warn("[消费协议] 消息验证失败：数据为空");
            return false;
        }

        // HTTP协议不需要校验和，数据完整性由TCP层保证
        // validateChecksum方法已移除，HTTP协议不需要校验和验证
        message.setStatus("VALIDATED");
        log.debug("[消费协议] 消息验证通过");
        return true;
    }

    @Override
    public void processMessage(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        log.info("[消费协议] 开始处理消息，设备ID={}, 消息类型={}", deviceId, message.getMessageType());

        try {
            // 设置设备ID
            message.setDeviceId(deviceId);

            // 验证消息
            if (!validateMessage(message)) {
                throw new ProtocolProcessException("VALIDATE_FAILED", "消息验证失败");
            }

            // 根据消息类型处理业务逻辑
            String messageType = message.getMessageType();
            switch (messageType) {
                case "CONSUME_RECORD":
                    processConsumeRecord(message);
                    break;
                case "DEVICE_STATUS":
                    processDeviceStatus(message);
                    break;
                case "BALANCE_QUERY":
                    processBalanceQuery(message);
                    break;
                default:
                    log.warn("[消费协议] 未知消息类型：{}", messageType);
            }

            message.setStatus("PROCESSED");
            log.info("[消费协议] 消息处理成功，设备ID={}", deviceId);

        } catch (ProtocolProcessException e) {
            message.setStatus("FAILED");
            message.setErrorCode(e.getErrorCode());
            message.setErrorMessage(e.getMessage());
            log.error("[消费协议] 消息处理失败，设备ID={}, 错误={}", deviceId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            message.setStatus("FAILED");
            message.setErrorCode("PROCESS_ERROR");
            message.setErrorMessage(e.getMessage());
            log.error("[消费协议] 消息处理异常，设备ID={}, 错误={}", deviceId, e.getMessage(), e);
            throw new ProtocolProcessException("PROCESS_ERROR", "消息处理异常：" + e.getMessage());
        }
    }

    @Override
    public byte[] buildResponse(ProtocolMessage requestMessage, boolean success, String errorCode, String errorMessage) {
        log.debug("[消费协议] 构建响应消息，成功={}, 错误码={}", success, errorCode);

        try {
            // 根据实际协议文档构建响应消息
            ByteBuffer buffer = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN);

            // 协议头
            buffer.put(PROTOCOL_HEADER);

            // 响应类型（假设1字节：0x00-成功，0x01-失败）
            buffer.put((byte) (success ? 0x00 : 0x01));

            // 错误码（失败时，假设2字节）
            if (!success && errorCode != null) {
                buffer.putShort((short) Integer.parseInt(errorCode));
            }

            // 填充到固定长度
            while (buffer.position() < buffer.capacity()) {
                buffer.put((byte) 0x00);
            }

            byte[] response = new byte[buffer.position()];
            buffer.flip();
            buffer.get(response);

            log.info("[消费协议] 响应消息构建成功，长度={}", response.length);
            return response;

        } catch (Exception e) {
            log.error("[消费协议] 响应消息构建失败，错误={}", e.getMessage(), e);
            return new byte[0];
        }
    }

    /**
     * 验证协议头
     * <p>
     * 注意：当前协议使用HTTP文本格式，此方法保留用于未来可能的二进制协议支持
     * </p>
     *
     * @param data 原始数据
     * @return true-验证通过，false-验证失败
     */
    @SuppressWarnings("unused")
    private boolean validateHeader(byte[] data) {
        if (data == null || data.length < PROTOCOL_HEADER.length) {
            return false;
        }
        for (int i = 0; i < PROTOCOL_HEADER.length; i++) {
            if (data[i] != PROTOCOL_HEADER[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取消息类型名称
     * <p>
     * 注意：当前协议使用HTTP文本格式，此方法保留用于未来可能的二进制协议支持
     * </p>
     *
     * @param messageType 消息类型代码
     * @return 消息类型名称
     */
    @SuppressWarnings("unused")
    private String getMessageTypeName(int messageType) {
        switch (messageType) {
            case 0x01:
                return "CONSUME_RECORD";
            case 0x02:
                return "DEVICE_STATUS";
            case 0x03:
                return "BALANCE_QUERY";
            default:
                return "UNKNOWN_" + messageType;
        }
    }

    /**
     * 处理消费记录消息
     * <p>
     * 通过网关调用消费服务保存消费记录
     * </p>
     *
     * @param message 协议消息
     */
    /**
     * 根据卡号查询用户ID
     * <p>
     * 通过消费服务的快速用户查询接口，根据卡号查询用户ID
     * </p>
     *
     * @param cardNumber 卡号
     * @return 用户ID，如果查询失败返回null
     */
    private Long queryUserIdByCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return null;
        }

        try {
            log.debug("[消费协议] 根据卡号查询用户ID，cardNumber={}", cardNumber);

            // 1. 先查询缓存（L1本地缓存 -> L2 Redis缓存）
            Long cachedUserId = cacheManager.getUserIdByCardNumber(cardNumber);
            if (cachedUserId != null) {
                log.info("[消费协议] 从缓存获取用户ID，cardNumber={}, userId={}", cardNumber, cachedUserId);
                return cachedUserId;
            }

            // 2. 缓存未命中，调用消费服务的快速用户查询接口
            // GET /api/v1/consume/mobile/user/quick?queryType=cardNumber&queryValue={cardNumber}
            String url = "/api/v1/consume/mobile/user/quick?queryType=cardNumber&queryValue=" + 
                         java.net.URLEncoder.encode(cardNumber, java.nio.charset.StandardCharsets.UTF_8);

            // 使用Map.class作为响应类型，然后手动解析
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) 
                    (ResponseDTO<?>) gatewayServiceClient.callConsumeService(
                            url,
                            HttpMethod.GET,
                            null,
                            Map.class
                    );

            if (response != null && response.isSuccess() && response.getData() != null) {
                Map<String, Object> userData = response.getData();
                Object userIdObj = userData.get("userId");
                if (userIdObj != null) {
                    try {
                        Long userId = Long.parseLong(userIdObj.toString());
                        
                        // 3. 缓存卡号到用户ID的映射（多级缓存）
                        cacheManager.cacheUserCardMapping(cardNumber, userId);
                        
                        log.info("[消费协议] 根据卡号查询用户ID成功，cardNumber={}, userId={}", cardNumber, userId);
                        return userId;
                    } catch (NumberFormatException e) {
                        log.warn("[消费协议] 用户ID格式错误，userId={}", userIdObj);
                    }
                }
            } else {
                log.warn("[消费协议] 根据卡号查询用户ID失败，cardNumber={}, 错误={}", 
                        cardNumber, response != null ? response.getMessage() : "响应为空");
            }

        } catch (Exception e) {
            log.error("[消费协议] 根据卡号查询用户ID异常，cardNumber={}, 错误={}", cardNumber, e.getMessage(), e);
        }

        return null;
    }

    /**
     * 处理消费记录消息
     * <p>
     * 将消费记录消息发送到RabbitMQ队列，由消费者异步处理
     * 根据"消费PUSH通讯协议 （中控智慧） V1.0-20181225"文档映射字段
     * 支持多条记录批量处理
     * 企业级高可用特性：使用消息队列缓冲，提高系统吞吐量
     * </p>
     *
     * @param message 协议消息
     */
    private void processConsumeRecord(ProtocolMessage message) {
        long startTime = System.currentTimeMillis();
        log.info("[消费协议] 处理消费记录，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[消费协议] 消费记录数据为空，无法处理");
                return;
            }

            // 支持多条记录处理
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> recordsList = (java.util.List<Map<String, Object>>) data.get("records");
            
            if (recordsList != null && !recordsList.isEmpty()) {
                // 批量处理多条记录（发送到队列）
                int successCount = 0;
                int failCount = 0;
                
                for (Map<String, Object> recordData : recordsList) {
                    try {
                        if (processSingleConsumeRecord(recordData, message.getDeviceId(), message.getDeviceCode())) {
                            successCount++;
                        } else {
                            failCount++;
                        }
                    } catch (Exception e) {
                        log.error("[消费协议] 处理单条消费记录异常，recordData={}, 错误={}", recordData, e.getMessage(), e);
                        failCount++;
                    }
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("[消费协议] 批量处理消费记录完成，成功={}, 失败={}, 总计={}, duration={}ms", 
                        successCount, failCount, recordsList.size(), duration);
                
                // 记录监控指标
                metricsCollector.recordSuccess(PROTOCOL_TYPE, duration);
                metricsCollector.recordQueueOperation("protocol.consume.record", "send");
                return;
            }

            // 兼容单条记录处理（直接使用data中的字段）
            boolean success = processSingleConsumeRecord(data, message.getDeviceId(), message.getDeviceCode());
            long duration = System.currentTimeMillis() - startTime;
            
            if (success) {
                metricsCollector.recordSuccess(PROTOCOL_TYPE, duration);
                metricsCollector.recordQueueOperation("protocol.consume.record", "send");
            } else {
                metricsCollector.recordError(PROTOCOL_TYPE, "PROCESS_ERROR");
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[消费协议] 处理消费记录异常，错误={}, duration={}ms", e.getMessage(), duration, e);
            metricsCollector.recordError(PROTOCOL_TYPE, "PROCESS_ERROR");
        }
    }

    /**
     * 处理单条消费记录
     * <p>
     * 处理单条消费记录，映射字段并调用消费服务保存
     * </p>
     *
     * @param recordData 单条记录数据
     * @param deviceId 设备ID
     * @param deviceCode 设备编号（序列号）
     * @return 是否处理成功
     */
    private boolean processSingleConsumeRecord(Map<String, Object> recordData, Long deviceId, String deviceCode) {
        try {

            // 根据协议文档映射字段
            // 协议字段：SysID, CARDNO, PosTime, PosMoney, Balance, CardRecID, State, MealType, MealDate, RecNo, OPID
            Map<String, Object> consumeRequest = new HashMap<>();
            
            // 设备信息
            consumeRequest.put("deviceId", deviceId);
            consumeRequest.put("deviceCode", deviceCode != null ? deviceCode : "UNKNOWN");

            // 卡号（CARDNO字段）
            Object cardNoObj = recordData.get("cardNo");
            if (cardNoObj == null) {
                log.warn("[消费协议] 卡号为空，无法处理消费记录");
                return false;
            }
            String cardNo = cardNoObj.toString();
            
            // 根据卡号查找用户ID
            Long userId = queryUserIdByCardNumber(cardNo);
            if (userId == null) {
                log.warn("[消费协议] 无法根据卡号查询用户ID，cardNo={}，跳过处理", cardNo);
                return false;
            }

            consumeRequest.put("userId", userId);

            // 消费时间（PosTime字段，Unix时间戳，秒）
            Object posTimeObj = recordData.get("posTime");
            if (posTimeObj != null) {
                try {
                    long posTime = Long.parseLong(posTimeObj.toString());
                    consumeRequest.put("consumeTime", posTime);
                } catch (NumberFormatException e) {
                    log.warn("[消费协议] posTime字段格式错误，posTime={}", posTimeObj);
                    consumeRequest.put("consumeTime", System.currentTimeMillis() / 1000);
                }
            } else {
                consumeRequest.put("consumeTime", System.currentTimeMillis() / 1000);
            }

            // 消费金额（PosMoney字段，单位：分）
            Object posMoneyObj = recordData.get("posMoney");
            if (posMoneyObj == null) {
                log.warn("[消费协议] 消费金额为空，无法处理");
                return false;
            }
            try {
                // 金额单位：分，需要转换为元（如果需要）
                int amountInCents = Integer.parseInt(posMoneyObj.toString());
                // 转换为BigDecimal（元）
                java.math.BigDecimal amount = new java.math.BigDecimal(amountInCents).divide(new java.math.BigDecimal(100));
                consumeRequest.put("amount", amount);
            } catch (NumberFormatException e) {
                log.warn("[消费协议] posMoney字段格式错误，posMoney={}", posMoneyObj);
                return false;
            }

            // 余额（Balance字段，单位：分）
            Object balanceObj = recordData.get("balance");
            if (balanceObj != null) {
                try {
                    int balanceInCents = Integer.parseInt(balanceObj.toString());
                    java.math.BigDecimal balance = new java.math.BigDecimal(balanceInCents).divide(new java.math.BigDecimal(100));
                    consumeRequest.put("balance", balance);
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }

            // 消费类型（State字段）
            Object stateObj = recordData.get("state");
            if (stateObj != null) {
                consumeRequest.put("consumeType", stateObj.toString());
            }

            // 餐别（MealType字段）
            Object mealTypeObj = recordData.get("mealType");
            if (mealTypeObj != null) {
                consumeRequest.put("mealType", mealTypeObj.toString());
            }

            // 记餐日期（MealDate字段）
            Object mealDateObj = recordData.get("mealDate");
            if (mealDateObj != null) {
                consumeRequest.put("mealDate", mealDateObj.toString());
            }

            // 交易流水号（RecNo字段）
            Object recNoObj = recordData.get("recNo");
            if (recNoObj != null) {
                consumeRequest.put("transactionNo", recNoObj.toString());
            }

            // 发送到RabbitMQ队列，由消费者异步处理（企业级高可用：消息队列缓冲）
            rabbitTemplate.convertAndSend("protocol.consume.record", consumeRequest);
            
            log.info("[消费协议] 消费记录已发送到队列，userId={}, deviceId={}, amount={}",
                    userId, deviceId, consumeRequest.get("amount"));
            return true;

        } catch (Exception e) {
            log.error("[消费协议] 处理单条消费记录异常，recordData={}, 错误={}", recordData, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理设备状态消息
     * <p>
     * 将设备状态更新消息发送到RabbitMQ队列，由消费者异步处理
     * 企业级高可用特性：使用消息队列缓冲，提高系统吞吐量
     * </p>
     *
     * @param message 协议消息
     */
    private void processDeviceStatus(ProtocolMessage message) {
        long startTime = System.currentTimeMillis();
        log.info("[消费协议] 处理设备状态，数据={}", message.getData());

        try {
            // 从消息数据中解析设备状态信息
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[消费协议] 设备状态数据为空，无法处理");
                return;
            }

            // 构建设备状态更新请求
            Map<String, Object> statusRequest = new HashMap<>();
            Object deviceId = data.get("deviceId");
            Object deviceStatus = data.get("deviceStatus");
            Object onlineStatus = data.get("onlineStatus");

            if (deviceId == null) {
                log.warn("[消费协议] 设备ID为空，无法更新设备状态");
                return;
            }

            statusRequest.put("deviceId", deviceId);
            if (deviceStatus != null) {
                statusRequest.put("deviceStatus", deviceStatus);
            }
            if (onlineStatus != null) {
                statusRequest.put("onlineStatus", onlineStatus);
            }

            // 发送到RabbitMQ队列，由消费者异步处理
            rabbitTemplate.convertAndSend("protocol.device.status", statusRequest);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[消费协议] 设备状态更新已发送到队列，deviceId={}, status={}, duration={}ms",
                    deviceId, deviceStatus, duration);
            
            // 记录监控指标
            metricsCollector.recordQueueOperation("protocol.device.status", "send");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[消费协议] 处理设备状态异常，错误={}, duration={}ms", e.getMessage(), duration, e);
            metricsCollector.recordError(PROTOCOL_TYPE, "DEVICE_STATUS_ERROR");
        }
    }

    /**
     * 处理余额查询消息
     * <p>
     * 处理设备发起的余额查询请求
     * 通过GatewayServiceClient调用ioedream-consume-service查询用户余额
     * </p>
     *
     * @param message 协议消息
     */
    private void processBalanceQuery(ProtocolMessage message) {
        log.info("[消费协议] 处理余额查询，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[消费协议] 余额查询数据为空，跳过处理");
                return;
            }

            // 构建余额查询请求
            Long userId = ((Number) data.get("userId")).longValue();

            // 通过网关调用消费服务查询余额
            ResponseDTO<?> response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/account/balance/user/" + userId,
                    HttpMethod.GET,
                    null,
                    Object.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                Object balanceData = response.getData();
                BigDecimal balance = null;
                
                if (balanceData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> balanceMap = (Map<String, Object>) balanceData;
                    Object balanceObj = balanceMap.get("balance");
                    if (balanceObj instanceof Number) {
                        balance = new BigDecimal(balanceObj.toString());
                    }
                    log.info("[消费协议] 余额查询成功，userId={}, 余额={}", userId, balance);
                } else if (balanceData instanceof Number) {
                    balance = new BigDecimal(balanceData.toString());
                    log.info("[消费协议] 余额查询成功，userId={}, 余额={}", userId, balance);
                } else {
                    log.info("[消费协议] 余额查询成功，userId={}, 余额数据={}", userId, balanceData);
                }
                
                // 将余额查询结果存储到消息数据中，以便后续通过协议响应返回给设备
                if (balance != null) {
                    Map<String, Object> messageData = message.getData();
                    if (messageData == null) {
                        messageData = new HashMap<>();
                        message.setData(messageData);
                    }
                    // 存储余额信息（单位：分，转换为整数）
                    messageData.put("balance", balance.multiply(new BigDecimal("100")).intValue());
                    messageData.put("balanceQueryResult", "SUCCESS");
                    log.info("[消费协议] 余额查询结果已存储到消息中，userId={}, 余额（分）={}", 
                            userId, messageData.get("balance"));
                }
            } else {
                log.warn("[消费协议] 余额查询失败，错误={}", response != null ? response.getMessage() : "响应为空");
                // 存储查询失败信息
                Map<String, Object> messageData = message.getData();
                if (messageData == null) {
                    messageData = new HashMap<>();
                    message.setData(messageData);
                }
                messageData.put("balanceQueryResult", "FAILED");
                messageData.put("errorMessage", response != null ? response.getMessage() : "查询失败");
            }

        } catch (Exception e) {
            log.error("[消费协议] 处理余额查询异常，错误={}", e.getMessage(), e);
            // 不抛出异常，避免影响其他消息处理
        }
    }

    /**
     * 验证校验和
     * <p>
     * 根据实际协议文档实现校验和验证
     * 注意：根据"消费PUSH通讯协议 （中控智慧） V1.0"文档，校验和通常位于消息末尾
     * </p>
     *
     * @param data 消息数据
     * @return 校验和是否正确
     */

}


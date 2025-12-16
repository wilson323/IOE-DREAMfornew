package net.lab1024.sa.devicecomm.protocol.handler.impl;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.Counted;
// import net.lab1024.sa.devicecomm.monitor.ProtocolMetricsCollector; // 已废弃，已移除
import net.lab1024.sa.devicecomm.protocol.enums.AccessEventTypeEnum;
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;

/**
 * 门禁PUSH协议处理器（熵基科技 V4.8）
 * <p>
 * 实现门禁设备的PUSH协议处理
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解，由Spring管理
 * - 完整的函数级注释
 * </p>
 * <p>
 * 协议规范：
 * - 协议名称：安防PUSH通讯协议
 * - 厂商：熵基科技
 * - 版本：V4.8
 * - 文档：安防PUSH通讯协议 （熵基科技）V4.8-20240107
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AccessProtocolHandler implements ProtocolHandler {

    /**
     * 协议类型
     */
    private static final String PROTOCOL_TYPE = ProtocolTypeEnum.ACCESS_ENTROPY_V4_8.getCode();

    /**
     * 协议头标识（根据实际协议文档定义）
     */
    private static final byte[] PROTOCOL_HEADER = {(byte) 0xAA, 0x55};

    /**
     * 最小消息长度（字节）
     * <p>
     * 注意：当前协议使用HTTP文本格式，此字段保留用于未来可能的二进制协议支持
     * </p>
     */
    @SuppressWarnings("unused")
    private static final int MIN_MESSAGE_LENGTH = 24;

    // 注意：HTTP协议不需要校验和，此常量保留用于兼容性
    // private static final int CHECKSUM_POSITION_OFFSET = 2;

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
     * Micrometer指标注册表（用于编程式指标收集）
     */
    @Resource
    private MeterRegistry meterRegistry;

    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }

    @Override
    public String getManufacturer() {
        return ProtocolTypeEnum.ACCESS_ENTROPY_V4_8.getManufacturer();
    }

    @Override
    public String getVersion() {
        return ProtocolTypeEnum.ACCESS_ENTROPY_V4_8.getVersion();
    }

    /**
     * 解析协议消息（二进制格式）
     * <p>
     * <b>注意：当前协议使用HTTP文本格式，此方法保留用于未来可能的二进制协议支持。</b>
     * 实际解析请使用 {@link #parseMessage(String)} 方法。
     * </p>
     * <p>
     * 根据"安防PUSH通讯协议 （熵基科技）V4.8-20240107"文档，
     * 当前实现使用HTTP POST文本格式（键值对，制表符分隔），
     * 二进制格式解析方法保留用于兼容性。
     * </p>
     */
    @Override
    public ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException {
        log.warn("[门禁协议] 二进制解析方法被调用，当前协议使用HTTP文本格式，请使用parseMessage(String)方法");

        // 将字节数组转换为字符串，委托给文本解析方法
        if (rawData == null || rawData.length == 0) {
            throw new ProtocolParseException("INVALID_DATA", "消息数据为空", rawData);
        }

        try {
            String textData = new String(rawData, StandardCharsets.UTF_8);
            return parseMessage(textData);
        } catch (Exception e) {
            log.error("[门禁协议] 二进制转文本失败，错误={}", e.getMessage(), e);
            throw new ProtocolParseException("CONVERT_ERROR", "二进制数据转换为文本失败：" + e.getMessage(), rawData);
        }
    }

    /**
     * 解析协议消息（字符串格式）
     * <p>
     * 根据"安防PUSH通讯协议 （熵基科技）V4.8-20240107"文档实现
     * 协议格式：HTTP POST，数据格式为键值对，使用制表符（HT）分隔
     * 参考文档: docs/PROTOCOL_FORMAT_ANALYSIS.md
     * </p>
     * <p>
     * 实时事件数据格式（rtlog表）:
     * time={Time}\tpin={Pin}\tcardno={CardNo}\tsitecode={SiteCode}\tlinkid={LinkId}\teventaddr={EventAddr}\tevent={Event}\tinoutstatus={InOutStatus}\tverifytype={VerifyType}\tindex={Index}\tmaskflag={MaskFlag}\ttemperature={Temperature}\tconvtemperature={ConvTemperature}\tbitCount={BitCount}
     * </p>
     */
    @Override
    public ProtocolMessage parseMessage(String rawData) throws ProtocolParseException {
        log.debug("[门禁协议] 解析HTTP文本消息，数据长度={}", rawData != null ? rawData.length() : 0);

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

            // 解析键值对格式的数据（使用制表符分隔）
            Map<String, Object> data = new HashMap<>();
            String[] pairs = rawData.split("\t"); // 制表符分隔

            for (String pair : pairs) {
                if (pair == null || pair.trim().isEmpty()) {
                    continue;
                }

                // 解析键值对：key=value
                int equalIndex = pair.indexOf('=');
                if (equalIndex > 0 && equalIndex < pair.length() - 1) {
                    String key = pair.substring(0, equalIndex).trim();
                    String value = pair.substring(equalIndex + 1).trim();
                    data.put(key, value);
                }
            }

            // 根据字段解析消息类型
            String messageType = determineMessageType(data);
            message.setMessageType(messageType);

            // 提取设备编号（从cardno或其他字段，根据实际协议）
            String deviceCode = extractDeviceCode(data);
            message.setDeviceCode(deviceCode);

            // 设置解析后的数据
            message.setData(data);

            log.info("[门禁协议] HTTP文本消息解析成功，消息类型={}, 设备编号={}", messageType, deviceCode);
            return message;

        } catch (ProtocolParseException e) {
            throw e;
        } catch (Exception e) {
            log.error("[门禁协议] HTTP文本消息解析异常，错误={}", e.getMessage(), e);
            throw new ProtocolParseException("PARSE_STRING_ERROR", "HTTP文本消息解析失败：" + e.getMessage(), null);
        }
    }

    /**
     * 根据解析的数据确定消息类型
     * <p>
     * 根据协议文档：
     * - 如果包含event字段，则为实时事件（ACCESS_RECORD）
     * - 如果包含sensor/relay/alarm字段，则为实时状态（DEVICE_STATUS）
     * - 如果包含alarm相关字段，则为报警事件（ALARM_EVENT）
     * </p>
     */
    private String determineMessageType(Map<String, Object> data) {
        if (data.containsKey("event")) {
            // 检查事件码判断是否为报警事件
            Object eventObj = data.get("event");
            if (eventObj != null) {
                try {
                    int event = Integer.parseInt(eventObj.toString());
                    // 根据协议文档：5000-6000为异常事件，6000-7000为警告事件
                    if (event >= 5000 && event < 7000) {
                        return "ALARM_EVENT";
                    }
                } catch (NumberFormatException e) {
                    log.debug("[门禁协议] 事件类型解析失败，忽略: eventObj={}", eventObj);
                }
            }
            return "ACCESS_RECORD";
        } else if (data.containsKey("sensor") || data.containsKey("relay") || data.containsKey("alarm")) {
            return "DEVICE_STATUS";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 从解析的数据中提取设备编号
     * <p>
     * 根据协议文档，设备编号可能来自：
     * - eventaddr字段（事件点，默认为doorid）
     * - 或其他字段（需要根据实际协议确认）
     * </p>
     */
    private String extractDeviceCode(Map<String, Object> data) {
        // 优先使用eventaddr作为设备编号
        if (data.containsKey("eventaddr")) {
            return String.valueOf(data.get("eventaddr"));
        }
        // 如果没有eventaddr，尝试其他字段
        return data.getOrDefault("deviceCode", "UNKNOWN").toString();
    }

    @Override
    public boolean validateMessage(ProtocolMessage message) {
        if (message == null) {
            log.warn("[门禁协议] 消息验证失败：消息为空");
            return false;
        }

        // 验证消息类型
        if (message.getMessageType() == null || message.getMessageType().isEmpty()) {
            log.warn("[门禁协议] 消息验证失败：消息类型为空");
            return false;
        }

        // 验证设备编号
        if (message.getDeviceCode() == null || message.getDeviceCode().isEmpty()) {
            log.warn("[门禁协议] 消息验证失败：设备编号为空");
            return false;
        }

        // 验证数据
        if (message.getData() == null || message.getData().isEmpty()) {
            log.warn("[门禁协议] 消息验证失败：数据为空");
            return false;
        }

        // 验证校验和（根据实际协议文档）
        // 注意：HTTP协议不需要校验和验证，数据完整性由TCP层保证
        // 如果消息有原始字节数据，可以跳过校验和验证
        if (message.getRawDataBytes() != null && message.getRawDataBytes().length > 0) {
            // 对于二进制数据，可以保留校验和验证（如果将来需要支持二进制协议）
            // 当前HTTP协议不需要
        }

        message.setStatus("VALIDATED");
        log.debug("[门禁协议] 消息验证通过");
        return true;
    }

    @Override
    @Timed(value = "protocol.message.process.duration",
           description = "协议消息处理耗时")
    @Counted(value = "protocol.message.process",
             description = "协议消息处理次数")
    public void processMessage(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        log.info("[门禁协议] 开始处理消息，设备ID={}, 消息类型={}", deviceId, message.getMessageType());

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
                case "ACCESS_EVENT":
                case "ACCESS_RECORD":
                    processAccessEvent(message);
                    break;
                case "DEVICE_STATUS":
                    processDeviceStatus(message);
                    break;
                case "ALARM_EVENT":
                    processAlarmEvent(message);
                    break;
                default:
                    log.warn("[门禁协议] 未知消息类型：{}", messageType);
            }

            message.setStatus("PROCESSED");
            log.info("[门禁协议] 消息处理成功，设备ID={}", deviceId);

        } catch (ProtocolProcessException e) {
            message.setStatus("FAILED");
            message.setErrorCode(e.getErrorCode());
            message.setErrorMessage(e.getMessage());
            log.error("[门禁协议] 消息处理失败，设备ID={}, 错误={}", deviceId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            message.setStatus("FAILED");
            message.setErrorCode("PROCESS_ERROR");
            message.setErrorMessage(e.getMessage());
            log.error("[门禁协议] 消息处理异常，设备ID={}, 错误={}", deviceId, e.getMessage(), e);
            throw new ProtocolProcessException("PROCESS_ERROR", "消息处理异常：" + e.getMessage());
        }
    }

    @Override
    public byte[] buildResponse(ProtocolMessage requestMessage, boolean success, String errorCode, String errorMessage) {
        log.debug("[门禁协议] 构建响应消息，成功={}, 错误码={}", success, errorCode);

        try {
            // 根据实际协议文档构建响应消息
            ByteBuffer buffer = ByteBuffer.allocate(24).order(ByteOrder.LITTLE_ENDIAN);

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

            log.info("[门禁协议] 响应消息构建成功，长度={}", response.length);
            return response;

        } catch (Exception e) {
            log.error("[门禁协议] 响应消息构建失败，错误={}", e.getMessage(), e);
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
                return "ACCESS_EVENT";
            case 0x02:
                return "DEVICE_STATUS";
            case 0x03:
                return "ALARM_EVENT";
            default:
                return "UNKNOWN_" + messageType;
        }
    }

    /**
     * 处理门禁事件消息
     * <p>
     * 将门禁事件消息发送到RabbitMQ队列，由消费者异步处理
     * 根据"安防PUSH通讯协议 （熵基科技）V4.8"文档映射字段
     * 企业级高可用特性：
     * - 使用消息队列缓冲，提高系统吞吐量
     * - 异步处理，不阻塞主线程
     * - 支持重试和死信队列
     * </p>
     *
     * @param message 协议消息
     */
    private void processAccessEvent(ProtocolMessage message) {
        long startTime = System.currentTimeMillis();
        log.info("[门禁协议] 处理门禁事件，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[门禁协议] 门禁事件数据为空，跳过处理");
                return;
            }

            // 根据协议文档映射字段到AccessRecordAddForm
            // 协议字段：time, pin, cardno, eventaddr, event, inoutstatus, verifytype, index, maskflag, temperature
            Map<String, Object> accessRecordRequest = new HashMap<>();

            // 设备信息
            accessRecordRequest.put("deviceId", message.getDeviceId());
            accessRecordRequest.put("deviceCode", message.getDeviceCode() != null ? message.getDeviceCode() : data.get("eventaddr"));

            // 用户信息（pin为工号/用户ID）
            Object pinObj = data.get("pin");
            if (pinObj != null) {
                try {
                    Long userId = Long.parseLong(pinObj.toString());
                    accessRecordRequest.put("userId", userId);
                } catch (NumberFormatException e) {
                    log.warn("[门禁协议] pin字段格式错误，pin={}", pinObj);
                }
            }

            // 通行时间（time字段，格式：XXXX-XX-XX XX:XX:XX，需要转换为Unix时间戳）
            Object timeObj = data.get("time");
            if (timeObj != null) {
                try {
                    String timeStr = timeObj.toString();
                    // 解析时间字符串：XXXX-XX-XX XX:XX:XX
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(
                            timeStr.replace(" ", "T"),
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    );
                    long timestamp = dateTime.atZone(java.time.ZoneId.of("Asia/Shanghai")).toEpochSecond();
                    accessRecordRequest.put("passTime", timestamp);
                } catch (Exception e) {
                    log.warn("[门禁协议] time字段解析失败，time={}, 错误={}", timeObj, e.getMessage());
                    // 使用当前时间作为默认值
                    accessRecordRequest.put("passTime", System.currentTimeMillis() / 1000);
                }
            } else {
                accessRecordRequest.put("passTime", System.currentTimeMillis() / 1000);
            }

            // 通行类型（inoutstatus字段：0-入，1-出）
            Object inoutStatusObj = data.get("inoutstatus");
            if (inoutStatusObj != null) {
                try {
                    int inoutStatus = Integer.parseInt(inoutStatusObj.toString());
                    accessRecordRequest.put("passType", inoutStatus); // 0-进入，1-离开
                } catch (NumberFormatException e) {
                    log.warn("[门禁协议] inoutstatus字段格式错误，inoutstatus={}", inoutStatusObj);
                    accessRecordRequest.put("passType", 0); // 默认进入
                }
            } else {
                accessRecordRequest.put("passType", 0); // 默认进入
            }

            // 门号（eventaddr字段，事件点）
            Object eventAddrObj = data.get("eventaddr");
            if (eventAddrObj != null) {
                try {
                    int doorNo = Integer.parseInt(eventAddrObj.toString());
                    accessRecordRequest.put("doorNo", doorNo);
                } catch (NumberFormatException e) {
                    log.warn("[门禁协议] eventaddr字段格式错误，eventaddr={}", eventAddrObj);
                }
            }

            // 通行方式（verifytype字段，需要解析字符串格式，完整支持0-29）
            Object verifyTypeObj = data.get("verifytype");
            if (verifyTypeObj != null) {
                int passMethod = parseVerifyType(verifyTypeObj.toString());
                accessRecordRequest.put("passMethod", passMethod);

                // 记录完整验证方式信息（用于后续分析）
                VerifyTypeEnum verifyType = VerifyTypeEnum.getByCode(passMethod);
                if (verifyType != VerifyTypeEnum.UNKNOWN) {
                    accessRecordRequest.put("verifyTypeName", verifyType.getName());
                    accessRecordRequest.put("verifyTypeCode", verifyType.getCode());
                }
            } else {
                accessRecordRequest.put("passMethod", 0); // 默认卡片
            }

            // 温度字段（temperature字段）
            Object temperatureObj = data.get("temperature");
            if (temperatureObj != null) {
                try {
                    BigDecimal temperature = new BigDecimal(temperatureObj.toString());
                    accessRecordRequest.put("temperature", temperature);
                } catch (NumberFormatException e) {
                    log.warn("[门禁协议] temperature字段格式错误，temperature={}", temperatureObj);
                }
            }

            // 通行结果（根据event字段判断：完整支持4000-7000+事件类型）
            Object eventObj = data.get("event");
            int accessResult = 1; // 默认成功
            String eventTypeName = "正常通行";
            String eventCategory = "正常";

            if (eventObj != null) {
                try {
                    int event = Integer.parseInt(eventObj.toString());

                    // 使用完整的事件类型枚举判断
                    AccessEventTypeEnum eventType = AccessEventTypeEnum.getByCode(event);
                    eventTypeName = AccessEventTypeEnum.getNameByCode(event);
                    eventCategory = eventType.getCategory().getName();

                    // 根据事件类别判断通行结果
                    if (AccessEventTypeEnum.isAbnormalEvent(event) ||
                        AccessEventTypeEnum.isAlarmEvent(event)) {
                        accessResult = 0; // 失败
                    } else if (AccessEventTypeEnum.isNormalEvent(event)) {
                        accessResult = 1; // 成功
                    } else {
                        // 梯控事件等其他事件，根据业务需求判断
                        accessResult = 1; // 默认成功
                    }

                    // 记录事件详细信息
                    accessRecordRequest.put("eventCode", event);
                    accessRecordRequest.put("eventTypeName", eventTypeName);
                    accessRecordRequest.put("eventCategory", eventCategory);

                } catch (NumberFormatException e) {
                    log.warn("[门禁协议] event字段格式错误，event={}", eventObj);
                }
            }
            accessRecordRequest.put("accessResult", accessResult);

            // 可选字段
            Object maskFlagObj = data.get("maskflag");
            if (maskFlagObj != null) {
                try {
                    boolean maskFlag = "1".equals(maskFlagObj.toString()) || Boolean.parseBoolean(maskFlagObj.toString());
                    accessRecordRequest.put("maskFlag", maskFlag);
                } catch (Exception e) {
                    log.debug("[门禁协议] maskflag字段解析失败，maskflag={}", maskFlagObj);
                }
            }

            // 发送到RabbitMQ队列，由消费者异步处理（企业级高可用：消息队列缓冲）
            rabbitTemplate.convertAndSend("protocol.access.record", accessRecordRequest);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[门禁协议] 门禁事件已发送到队列，duration={}ms", duration);

            // 记录监控指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.process")
                    .tag("protocol_type", PROTOCOL_TYPE)
                    .tag("status", "success")
                    .register(meterRegistry)
                    .increment();
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.access.record")
                    .tag("operation", "send")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[门禁协议] 处理门禁事件异常，错误={}, duration={}ms", e.getMessage(), duration, e);

            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", PROTOCOL_TYPE)
                    .tag("error_type", "PROCESS_ERROR")
                    .register(meterRegistry)
                    .increment();

            // 不抛出异常，避免影响其他消息处理
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
        log.info("[门禁协议] 处理设备状态，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[门禁协议] 设备状态数据为空，跳过处理");
                return;
            }

            // 构建设备状态更新请求
            Map<String, Object> deviceStatusRequest = new HashMap<>();
            deviceStatusRequest.put("deviceId", message.getDeviceId());
            deviceStatusRequest.put("deviceCode", message.getDeviceCode());

            // 根据协议文档解析设备状态（假设：0-离线，1-在线，2-维护中）
            Integer statusCode = (Integer) data.get("statusCode");
            String deviceStatus = "OFFLINE";
            if (statusCode != null) {
                switch (statusCode) {
                    case 1:
                        deviceStatus = "ONLINE";
                        break;
                    case 2:
                        deviceStatus = "MAINTAIN";
                        break;
                    default:
                        deviceStatus = "OFFLINE";
                }
            }
            deviceStatusRequest.put("deviceStatus", deviceStatus);
            deviceStatusRequest.put("lastOnlineTime", message.getTimestamp());

            // 发送到RabbitMQ队列，由消费者异步处理
            rabbitTemplate.convertAndSend("protocol.device.status", deviceStatusRequest);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[门禁协议] 设备状态更新已发送到队列，设备ID={}, 状态={}, duration={}ms",
                    message.getDeviceId(), deviceStatus, duration);

            // 记录监控指标（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.device.status")
                    .tag("operation", "send")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[门禁协议] 处理设备状态异常，错误={}, duration={}ms", e.getMessage(), duration, e);
            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", PROTOCOL_TYPE)
                    .tag("error_type", "DEVICE_STATUS_ERROR")
                    .register(meterRegistry)
                    .increment();
            // 不抛出异常，避免影响其他消息处理
        }
    }

    /**
     * 处理报警事件消息
     * <p>
     * 将报警事件消息发送到RabbitMQ队列，由消费者异步处理
     * 企业级高可用特性：使用消息队列缓冲，确保报警及时处理
     * </p>
     *
     * @param message 协议消息
     */
    private void processAlarmEvent(ProtocolMessage message) {
        long startTime = System.currentTimeMillis();
        log.info("[门禁协议] 处理报警事件，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[门禁协议] 报警事件数据为空，跳过处理");
                return;
            }

            // 构建报警记录请求
            Map<String, Object> alarmRequest = new HashMap<>();
            alarmRequest.put("deviceId", message.getDeviceId());
            alarmRequest.put("deviceCode", message.getDeviceCode());
            alarmRequest.put("alarmType", data.get("alarmType")); // 报警类型
            alarmRequest.put("alarmLevel", data.get("alarmLevel")); // 报警级别
            alarmRequest.put("alarmTime", message.getTimestamp());
            alarmRequest.put("alarmDescription", data.get("alarmDescription"));

            // 发送到RabbitMQ队列，由消费者异步处理
            rabbitTemplate.convertAndSend("protocol.alarm.event", alarmRequest);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[门禁协议] 报警事件已发送到队列，设备ID={}, duration={}ms",
                    message.getDeviceId(), duration);

            // 记录监控指标（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.alarm.event")
                    .tag("operation", "send")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[门禁协议] 处理报警事件异常，错误={}, duration={}ms", e.getMessage(), duration, e);
            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", PROTOCOL_TYPE)
                    .tag("error_type", "ALARM_EVENT_ERROR")
                    .register(meterRegistry)
                    .increment();
            // 不抛出异常，避免影响其他消息处理
        }
    }

    /**
     * 字节数组转十六进制字符串
     * <p>
     * 注意：当前协议使用HTTP文本格式，此方法保留用于未来可能的二进制协议支持
     * </p>
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    @SuppressWarnings("unused")
    private String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 解析验证方式（verifytype字段）
     * <p>
     * 根据"安防PUSH通讯协议 （熵基科技）V4.8-20240107"文档解析验证方式
     * verifytype字段可能是数字格式或字符串格式
     * </p>
     * <p>
     * 数字格式（附录3）：
     * - 0: 密码
     * - 1: 指纹
     * - 2: 卡片
     * - 3: 人脸
     * - 4: 掌纹
     * - 5: 虹膜
     * - 6: 声纹
     * - 15: 混合验证（多种方式组合）
     * </p>
     * <p>
     * 字符串格式（新验证方式规则）：
     * - 如果支持新验证方式规则，verifytype为字符串格式，需要解析
     * - 格式示例："1,3" 表示指纹+人脸混合验证
     * </p>
     *
     * @param verifyTypeStr 验证方式字符串（可能是数字或字符串格式）
     * @return 验证方式代码（0-卡片，1-人脸，2-指纹等）
     */
    private int parseVerifyType(String verifyTypeStr) {
        if (verifyTypeStr == null || verifyTypeStr.trim().isEmpty()) {
            return 0; // 默认卡片
        }

        try {
            // 尝试解析为数字格式
            int verifyType = Integer.parseInt(verifyTypeStr.trim());

            // 根据协议文档映射验证方式
            // 注意：这里需要根据实际业务需求调整映射关系
            switch (verifyType) {
                case 0:  // 密码
                    return 0; // 默认映射为卡片
                case 1:  // 指纹
                    return 2; // 指纹
                case 2:  // 卡片
                    return 0; // 卡片
                case 3:  // 人脸
                    return 1; // 人脸
                case 4:  // 掌纹
                    return 2; // 映射为指纹（或根据业务需求调整）
                case 5:  // 虹膜
                    return 1; // 映射为人脸（或根据业务需求调整）
                case 6:  // 声纹
                    return 0; // 映射为卡片（或根据业务需求调整）
                case 15: // 混合验证
                    return 1; // 默认映射为人脸（或根据业务需求调整）
                default:
                    log.warn("[门禁协议] 未知的验证方式代码，verifyType={}", verifyType);
                    return 0; // 默认卡片
            }
        } catch (NumberFormatException e) {
            // 字符串格式，需要解析（例如："1,3" 表示指纹+人脸）
            log.debug("[门禁协议] verifytype为字符串格式，verifyType={}", verifyTypeStr);

            // 解析字符串格式（例如："1,3" 或 "fingerprint,face"）
            String[] parts = verifyTypeStr.split(",");
            if (parts.length > 0) {
                // 取第一个验证方式作为主要验证方式
                String firstPart = parts[0].trim().toLowerCase();

                // 尝试匹配常见的关键词
                if (firstPart.contains("finger") || firstPart.contains("指纹")) {
                    return 2; // 指纹
                } else if (firstPart.contains("face") || firstPart.contains("人脸")) {
                    return 1; // 人脸
                } else if (firstPart.contains("card") || firstPart.contains("卡片")) {
                    return 0; // 卡片
                } else if (firstPart.contains("palm") || firstPart.contains("掌纹")) {
                    return 2; // 掌纹，映射为指纹
                } else if (firstPart.contains("iris") || firstPart.contains("虹膜")) {
                    return 1; // 虹膜，映射为人脸
                } else {
                    // 尝试解析为数字
                    try {
                        int num = Integer.parseInt(firstPart);
                        return parseVerifyType(String.valueOf(num)); // 递归调用数字解析
                    } catch (NumberFormatException ex) {
                        log.warn("[门禁协议] 无法解析验证方式字符串，verifyType={}", verifyTypeStr);
                        return 0; // 默认卡片
                    }
                }
            }

            return 0; // 默认卡片
        }
    }
}


package net.lab1024.sa.devicecomm.protocol.handler.impl;

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
import net.lab1024.sa.devicecomm.protocol.enums.ProtocolTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;

/**
 * 考勤PUSH协议处理器（熵基科技 V4.0）
 * <p>
 * 实现考勤设备的PUSH协议处理
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解，由Spring管理
 * - 使用@Resource注入依赖
 * - 完整的函数级注释
 * </p>
 * <p>
 * 协议规范：
 * - 协议名称：考勤PUSH通讯协议
 * - 厂商：熵基科技
 * - 版本：V4.0
 * - 文档：考勤PUSH通讯协议 （熵基科技） V4.0-20210113
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AttendanceProtocolHandler implements ProtocolHandler {

    /**
     * 协议类型
     */
    private static final String PROTOCOL_TYPE = ProtocolTypeEnum.ATTENDANCE_ENTROPY_V4_0.getCode();

    /**
     * 协议头标识（根据实际协议文档定义）
     */
    private static final byte[] PROTOCOL_HEADER = {0x55, (byte) 0xAA};

    /**
     * 最小消息长度（字节）
     * <p>
     * 注意：当前协议使用HTTP文本格式，此字段保留用于未来可能的二进制协议支持
     * </p>
     */
    @SuppressWarnings("unused")
    private static final int MIN_MESSAGE_LENGTH = 20;

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
        return ProtocolTypeEnum.ATTENDANCE_ENTROPY_V4_0.getManufacturer();
    }

    @Override
    public String getVersion() {
        return ProtocolTypeEnum.ATTENDANCE_ENTROPY_V4_0.getVersion();
    }

    /**
     * 解析协议消息（二进制格式）
     * <p>
     * <b>注意：当前协议使用HTTP文本格式，此方法保留用于未来可能的二进制协议支持。</b>
     * 实际解析请使用 {@link #parseMessage(String)} 方法。
     * </p>
     * <p>
     * 根据"考勤PUSH通讯协议 （熵基科技） V4.0-20210113"文档，
     * 当前实现使用HTTP POST文本格式（制表符分隔），
     * 二进制格式解析方法保留用于兼容性。
     * </p>
     */
    @Override
    public ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException {
        log.warn("[考勤协议] 二进制解析方法被调用，当前协议使用HTTP文本格式，请使用parseMessage(String)方法");

        // 将字节数组转换为字符串，委托给文本解析方法
        if (rawData == null || rawData.length == 0) {
            throw new ProtocolParseException("INVALID_DATA", "消息数据为空", rawData);
        }

        try {
            String textData = new String(rawData, StandardCharsets.UTF_8);
            return parseMessage(textData);
        } catch (Exception e) {
            log.error("[考勤协议] 二进制转文本失败，错误={}", e.getMessage(), e);
            throw new ProtocolParseException("CONVERT_ERROR", "二进制数据转换为文本失败：" + e.getMessage(), rawData);
        }
    }

    /**
     * 解析协议消息（字符串格式）
     * <p>
     * 根据"考勤PUSH通讯协议 （熵基科技） V4.0-20210113"文档实现
     * 协议格式：HTTP POST，数据格式为制表符分隔的文本
     * 参考文档: docs/PROTOCOL_FORMAT_ANALYSIS.md
     * </p>
     * <p>
     * 考勤记录数据格式（ATTLOG表）:
     * {Pin}\t{Time}\t{Status}\t{Verify}\t{Workcode}\t{Reserved1}\t{Reserved2}\t{MaskFlag}\t{Temperature}\t{ConvTemperature}
     * </p>
     */
    @Override
    public ProtocolMessage parseMessage(String rawData) throws ProtocolParseException {
        log.debug("[考勤协议] 解析HTTP文本消息，数据长度={}", rawData != null ? rawData.length() : 0);

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
            // 多条记录使用换行符分隔，支持批量处理
            String[] lines = rawData.split("\n");
            if (lines.length == 0) {
                throw new ProtocolParseException("INVALID_FORMAT", "消息格式错误：无数据行", null);
            }

            // 支持多条记录处理
            java.util.List<Map<String, Object>> recordsList = new java.util.ArrayList<>();

            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                String trimmedLine = line.trim();
                String[] fields = trimmedLine.split("\t");

                Map<String, Object> recordData = new HashMap<>();

                // 根据协议文档，字段顺序为：
                // Pin, Time, Status, Verify, Workcode, Reserved1, Reserved2, MaskFlag, Temperature, ConvTemperature
                if (fields.length >= 1) {
                    recordData.put("pin", fields[0].trim()); // 工号（用户ID）
                }
                if (fields.length >= 2) {
                    recordData.put("time", fields[1].trim()); // 验证时间，格式：XXXX-XX-XX XX:XX:XX
                }
                if (fields.length >= 3) {
                    recordData.put("status", fields[2].trim()); // 考勤状态
                }
                if (fields.length >= 4) {
                    recordData.put("verify", fields[3].trim()); // 验证方式
                }
                if (fields.length >= 5) {
                    recordData.put("workcode", fields[4].trim()); // 工作号码编码
                }
                if (fields.length >= 6) {
                    recordData.put("reserved1", fields[5].trim()); // 预留字段1
                }
                if (fields.length >= 7) {
                    recordData.put("reserved2", fields[6].trim()); // 预留字段2
                }
                if (fields.length >= 8) {
                    recordData.put("maskFlag", fields[7].trim()); // 口罩标志（可选）
                }
                if (fields.length >= 9) {
                    recordData.put("temperature", fields[8].trim()); // 温度值（可选）
                }
                if (fields.length >= 10) {
                    recordData.put("convTemperature", fields[9].trim()); // 转换后的温度值（可选）
                }

                recordsList.add(recordData);
            }

            if (recordsList.isEmpty()) {
                throw new ProtocolParseException("INVALID_FORMAT", "消息格式错误：无有效数据行", null);
            }

            // 确定消息类型
            message.setMessageType("ATTENDANCE_RECORD");

            // 提取设备编号（从第一条记录的pin）
            String deviceCode = recordsList.get(0).getOrDefault("pin", "UNKNOWN").toString();
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

            log.info("[考勤协议] HTTP文本消息解析成功，消息类型={}, 记录数={}, 工号={}",
                    message.getMessageType(), recordsList.size(),
                    recordsList.isEmpty() ? "N/A" : recordsList.get(0).get("pin"));
            return message;

        } catch (ProtocolParseException e) {
            throw e;
        } catch (Exception e) {
            log.error("[考勤协议] HTTP文本消息解析异常，错误={}", e.getMessage(), e);
            throw new ProtocolParseException("PARSE_STRING_ERROR", "HTTP文本消息解析失败：" + e.getMessage(), null);
        }
    }

    @Override
    public boolean validateMessage(ProtocolMessage message) {
        if (message == null) {
            log.warn("[考勤协议] 消息验证失败：消息为空");
            return false;
        }

        // 验证消息类型
        if (message.getMessageType() == null || message.getMessageType().isEmpty()) {
            log.warn("[考勤协议] 消息验证失败：消息类型为空");
            return false;
        }

        // 验证设备编号
        if (message.getDeviceCode() == null || message.getDeviceCode().isEmpty()) {
            log.warn("[考勤协议] 消息验证失败：设备编号为空");
            return false;
        }

        // 验证数据
        if (message.getData() == null || message.getData().isEmpty()) {
            log.warn("[考勤协议] 消息验证失败：数据为空");
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
        log.debug("[考勤协议] 消息验证通过");
        return true;
    }

    @Override
    @Timed(value = "protocol.message.process.duration",
           description = "协议消息处理耗时")
    @Counted(value = "protocol.message.process",
             description = "协议消息处理次数")
    public void processMessage(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        log.info("[考勤协议] 开始处理消息，设备ID={}, 消息类型={}", deviceId, message.getMessageType());

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
                case "ATTENDANCE_RECORD":
                    processAttendanceRecord(message);
                    break;
                case "DEVICE_STATUS":
                    processDeviceStatus(message);
                    break;
                default:
                    log.warn("[考勤协议] 未知消息类型：{}", messageType);
            }

            message.setStatus("PROCESSED");
            log.info("[考勤协议] 消息处理成功，设备ID={}", deviceId);

        } catch (ProtocolProcessException e) {
            message.setStatus("FAILED");
            message.setErrorCode(e.getErrorCode());
            message.setErrorMessage(e.getMessage());
            log.error("[考勤协议] 消息处理失败，设备ID={}, 错误={}", deviceId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            message.setStatus("FAILED");
            message.setErrorCode("PROCESS_ERROR");
            message.setErrorMessage(e.getMessage());
            log.error("[考勤协议] 消息处理异常，设备ID={}, 错误={}", deviceId, e.getMessage(), e);
            throw new ProtocolProcessException("PROCESS_ERROR", "消息处理异常：" + e.getMessage());
        }
    }

    @Override
    public byte[] buildResponse(ProtocolMessage requestMessage, boolean success, String errorCode, String errorMessage) {
        log.debug("[考勤协议] 构建响应消息，成功={}, 错误码={}", success, errorCode);

        try {
            // 根据实际协议文档构建响应消息
            ByteBuffer buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);

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

            log.info("[考勤协议] 响应消息构建成功，长度={}", response.length);
            return response;

        } catch (Exception e) {
            log.error("[考勤协议] 响应消息构建失败，错误={}", e.getMessage(), e);
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
                return "ATTENDANCE_RECORD";
            case 0x02:
                return "DEVICE_STATUS";
            default:
                return "UNKNOWN_" + messageType;
        }
    }

    /**
     * 处理考勤记录消息
     * <p>
     * 将考勤记录消息发送到RabbitMQ队列，由消费者异步处理
     * 根据"考勤PUSH通讯协议 （熵基科技） V4.0"文档映射字段
     * 支持多条记录批量处理
     * 企业级高可用特性：使用消息队列缓冲，提高系统吞吐量
     * </p>
     *
     * @param message 协议消息
     */
    private void processAttendanceRecord(ProtocolMessage message) {
        long startTime = System.currentTimeMillis();
        log.info("[考勤协议] 处理考勤记录，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[考勤协议] 考勤记录数据为空，跳过处理");
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
                        if (processSingleAttendanceRecord(recordData, message.getDeviceId(), message.getDeviceCode())) {
                            successCount++;
                        } else {
                            failCount++;
                        }
                    } catch (Exception e) {
                        log.error("[考勤协议] 处理单条考勤记录异常，recordData={}, 错误={}", recordData, e.getMessage(), e);
                        failCount++;
                    }
                }

                long duration = System.currentTimeMillis() - startTime;
                log.info("[考勤协议] 批量处理考勤记录完成，成功={}, 失败={}, 总计={}, duration={}ms",
                        successCount, failCount, recordsList.size(), duration);

                // 记录监控指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.process")
                        .tag("protocol_type", PROTOCOL_TYPE)
                        .tag("status", "success")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.attendance.record")
                        .tag("operation", "send")
                        .register(meterRegistry)
                        .increment();
                return;
            }

            // 兼容单条记录处理（直接使用data中的字段）
            boolean success = processSingleAttendanceRecord(data, message.getDeviceId(), message.getDeviceCode());

            if (success) {
                // 记录成功指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.process")
                        .tag("protocol_type", PROTOCOL_TYPE)
                        .tag("status", "success")
                        .register(meterRegistry)
                        .increment();
                Counter.builder("protocol.queue.operation")
                        .tag("queue_name", "protocol.attendance.record")
                        .tag("operation", "send")
                        .register(meterRegistry)
                        .increment();
            } else {
                // 记录错误指标（使用Micrometer编程式API）
                Counter.builder("protocol.message.error")
                        .tag("protocol_type", PROTOCOL_TYPE)
                        .tag("error_type", "PROCESS_ERROR")
                        .register(meterRegistry)
                        .increment();
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[考勤协议] 处理考勤记录异常，错误={}, duration={}ms", e.getMessage(), duration, e);
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
     * 处理单条考勤记录
     * <p>
     * 处理单条考勤记录，映射字段并调用考勤服务保存
     * </p>
     *
     * @param recordData 单条记录数据
     * @param deviceId 设备ID
     * @param deviceCode 设备编号
     * @return 是否处理成功
     */
    private boolean processSingleAttendanceRecord(Map<String, Object> recordData, Long deviceId, String deviceCode) {
        try {

            // 根据协议文档映射字段到AttendanceRecordAddForm
            // 协议字段：Pin, Time, Status, Verify, Workcode, Reserved1, Reserved2, MaskFlag, Temperature, ConvTemperature
            Map<String, Object> attendanceRecordRequest = new HashMap<>();

            // 设备信息
            attendanceRecordRequest.put("deviceId", deviceId);
            attendanceRecordRequest.put("deviceCode", deviceCode);

            // 用户信息（pin为工号/用户ID）
            Object pinObj = recordData.get("pin");
            if (pinObj != null) {
                try {
                    Long userId = Long.parseLong(pinObj.toString());
                    attendanceRecordRequest.put("userId", userId);
                    // 如果没有userName，可以使用pin作为userName
                    attendanceRecordRequest.put("userName", pinObj.toString());
                } catch (NumberFormatException e) {
                    log.warn("[考勤协议] pin字段格式错误，pin={}", pinObj);
                }
            }

            // 打卡时间（time字段，格式：XXXX-XX-XX XX:XX:XX，需要转换为Unix时间戳）
            Object timeObj = recordData.get("time");
            if (timeObj != null) {
                try {
                    String timeStr = timeObj.toString();
                    // 解析时间字符串：XXXX-XX-XX XX:XX:XX
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(
                            timeStr.replace(" ", "T"),
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                    );
                    long timestamp = dateTime.atZone(java.time.ZoneId.of("Asia/Shanghai")).toEpochSecond();
                    attendanceRecordRequest.put("punchTime", timestamp);
                } catch (Exception e) {
                    log.warn("[考勤协议] time字段解析失败，time={}, 错误={}", timeObj, e.getMessage());
                    // 使用当前时间作为默认值
                    attendanceRecordRequest.put("punchTime", System.currentTimeMillis() / 1000);
                }
            } else {
                attendanceRecordRequest.put("punchTime", System.currentTimeMillis() / 1000);
            }

            // 打卡类型和考勤状态（根据Status字段判断）
            Object statusObj = recordData.get("status");
            int punchType = 0; // 默认上班
            int attendanceStatus = 0; // 默认正常
            if (statusObj != null) {
                try {
                    int status = Integer.parseInt(statusObj.toString());
                    // 根据协议文档，Status字段含义：
                    // 0-正常，其他值需要根据实际协议文档确认
                    // 暂时使用status值作为考勤状态
                    attendanceStatus = status;
                    // 打卡类型根据时间或其他规则判断，这里暂时使用默认值
                    punchType = 0; // 0-上班，1-下班
                } catch (NumberFormatException e) {
                    log.warn("[考勤协议] status字段格式错误，status={}", statusObj);
                }
            }
            attendanceRecordRequest.put("punchType", punchType);
            attendanceRecordRequest.put("attendanceStatus", attendanceStatus);

            // 验证方式（verify字段，用于记录验证方式，但不影响业务逻辑）
            Object verifyObj = recordData.get("verify");
            if (verifyObj != null) {
                try {
                    int verify = Integer.parseInt(verifyObj.toString());
                    // 根据协议文档附录3，验证方式枚举
                    // 可以记录到remark或其他字段中
                    attendanceRecordRequest.put("verifyType", verify);
                } catch (NumberFormatException e) {
                    log.debug("[考勤协议] verify字段格式错误，verify={}", verifyObj);
                }
            }

            // 可选字段
            Object maskFlagObj = recordData.get("maskFlag");
            if (maskFlagObj != null) {
                try {
                    boolean maskFlag = "1".equals(maskFlagObj.toString()) || Boolean.parseBoolean(maskFlagObj.toString());
                    attendanceRecordRequest.put("maskFlag", maskFlag);
                } catch (Exception e) {
                    log.debug("[考勤协议] maskFlag字段解析失败，maskFlag={}", maskFlagObj);
                }
            }

            Object temperatureObj = recordData.get("temperature");
            if (temperatureObj != null) {
                try {
                    Double temperature = Double.parseDouble(temperatureObj.toString());
                    attendanceRecordRequest.put("temperature", temperature);
                } catch (NumberFormatException e) {
                    log.debug("[考勤协议] 温度解析失败，忽略: temperatureObj={}", temperatureObj);
                }
            }

            // 发送到RabbitMQ队列，由消费者异步处理（企业级高可用：消息队列缓冲）
            rabbitTemplate.convertAndSend("protocol.attendance.record", attendanceRecordRequest);

            log.info("[考勤协议] 考勤记录已发送到队列");
            return true;

        } catch (Exception e) {
            log.error("[考勤协议] 处理单条考勤记录异常，recordData={}, 错误={}", recordData, e.getMessage(), e);
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
        log.info("[考勤协议] 处理设备状态，数据={}", message.getData());

        try {
            Map<String, Object> data = message.getData();
            if (data == null || data.isEmpty()) {
                log.warn("[考勤协议] 设备状态数据为空，跳过处理");
                return;
            }

            // 构建设备状态更新请求
            Map<String, Object> deviceStatusRequest = new HashMap<>();
            deviceStatusRequest.put("deviceId", message.getDeviceId());
            deviceStatusRequest.put("deviceCode", message.getDeviceCode());

            // 根据协议文档解析设备状态
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
            log.info("[考勤协议] 设备状态更新已发送到队列，设备ID={}, 状态={}, duration={}ms",
                    message.getDeviceId(), deviceStatus, duration);

            // 记录监控指标（使用Micrometer编程式API）
            Counter.builder("protocol.queue.operation")
                    .tag("queue_name", "protocol.device.status")
                    .tag("operation", "send")
                    .register(meterRegistry)
                    .increment();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[考勤协议] 处理设备状态异常，错误={}, duration={}ms", e.getMessage(), duration, e);
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

}


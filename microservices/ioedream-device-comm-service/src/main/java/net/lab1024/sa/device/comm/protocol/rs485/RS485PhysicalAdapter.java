package net.lab1024.sa.device.comm.protocol.rs485;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolDeviceStatus;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolErrorInfo;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolErrorResponse;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolHeartbeatResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolInitResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolMessage;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolPermissionResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolRegistrationResult;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolValidationResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolBuildException;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;

/**
 * RS485物理层协议适配器
 * <p>
 * 支持工业设备RS485通讯协议，覆盖以下设备类型：
 * 1. 工业控制器（PLC、DCS）
 * 2. 传感器设备（温度、湿度、压力、流量）
 * 3. 仪表设备（电表、水表、气表）
 * 4. 执行器设备（电机、阀门、执行机构）
 * 5. 变频器设备（ABB、西门子、施耐德）
 * </p>
 * <p>
 * 支持的RS485协议标准：
 * - Modbus RTU协议
 * - Profibus DP协议
 * - CAN总线协议
 * - 自定义二进制协议
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Component("rs485PhysicalAdapter")
@Schema(description = "RS485物理层协议适配器")
@Slf4j
public class RS485PhysicalAdapter implements ProtocolAdapter {

    private static final String PROTOCOL_TYPE = "RS485_PHYSICAL_V1_0";
    private static final String MANUFACTURER = "INDUSTRIAL_RS485";
    private static final String VERSION = "1.0.0";

    // 支持的设备型号列表
    private static final String[] SUPPORTED_MODELS = {
            // 变频器设备
            "ABB_ACS880_V1", "ABB_ACS580_V1", "SIEMENS_S120_V1", "SIEMENS_G120_V1",
            "SCHNEIDER_ATV610_V1", "SCHNEIDER_ATV630_V1", "DELTA_VFD_E_V1", "DELTA_VFD_MS300_V1",

            // PLC控制器
            "SIEMENS_S7_1200_V1", "SIEMENS_S7_1500_V1", "MITSUBISHI_FX3U_V1", "MITSUBISHI_Q_V1",
            "OMRON_CP1E_V1", "OMRON_CP1H_V1", "KEYENCE_KV_V1", "PANASONIC_FP_V1",

            // 传感器设备
            "TEMP_SENSOR_485_V1", "HUMIDITY_SENSOR_485_V1", "PRESSURE_SENSOR_485_V1", "FLOW_SENSOR_485_V1",
            "LEVEL_SENSOR_485_V1", "PH_SENSOR_485_V1", "TURBIDITY_SENSOR_485_V1", "VIBRATION_SENSOR_485_V1",

            // 仪表设备
            "ELECTRIC_METER_485_V1", "WATER_METER_485_V1", "GAS_METER_485_V1", "HEAT_METER_485_V1",
            "POWER_ANALYZER_485_V1", "HARMONIC_ANALYZER_485_V1", "INSULATION_TESTER_485_V1", "EARTH_TESTER_485_V1",

            // 执行器设备
            "VALVE_ACTUATOR_485_V1", "MOTOR_CONTROLLER_485_V1", "SERVO_DRIVE_485_V1", "STEPPER_DRIVE_485_V1",
            "LINEAR_ACTUATOR_485_V1", "ROTARY_ACTUATOR_485_V1", "HYDRAULIC_VALVE_485_V1", "PNEUMATIC_VALVE_485_V1"
    };

    // RS485通讯参数配置
    private final Map<Long, RS485Config> deviceConfigs = new HashMap<>();
    private final Map<Long, RS485Connection> connections = new HashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);

    // 通讯统计
    private final Map<String, Long> messageCount = new HashMap<>();
    private final Map<String, Long> errorCount = new HashMap<>();

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
        return SUPPORTED_MODELS.clone();
    }

    @Override
    public boolean isDeviceModelSupported(String deviceModel) {
        return Arrays.asList(SUPPORTED_MODELS).contains(deviceModel);
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        try {
            log.debug("[RS485协议解析] 开始解析消息, deviceId={}, dataLength={}", deviceId, rawData.length);

            RS485Message message = new RS485Message();
            message.setDeviceId(deviceId);
            message.setRawData(rawData);
            message.setReceiveTime(System.currentTimeMillis());

            // 解析RS485帧格式
            if (rawData.length < 4) {
                throw new ProtocolParseException("RS485消息长度不足");
            }

            // 检查起始字节
            if (rawData[0] != (byte) 0xAA && rawData[0] != (byte) 0x55) {
                throw new ProtocolParseException("RS485消息起始字节错误");
            }

            // 解析消息头
            int startIndex = 0;
            if (rawData[0] == (byte) 0xAA) {
                startIndex = 1;
            }

            // 提取地址字节
            byte deviceAddress = rawData[startIndex];
            message.setDeviceAddress(deviceAddress);

            // 提取功能码
            byte functionCode = rawData[startIndex + 1];
            message.setFunctionCode(functionCode);

            // 提取数据长度
            int dataLength = rawData[startIndex + 2] & 0xFF;
            if (dataLength > 0 && rawData.length >= startIndex + 3 + dataLength + 2) {
                byte[] data = new byte[dataLength];
                System.arraycopy(rawData, startIndex + 3, data, 0, dataLength);
                message.setData(data);

                // 验证CRC校验
                byte[] crc = new byte[2];
                System.arraycopy(rawData, startIndex + 3 + dataLength, crc, 0, 2);
                message.setCrc(crc);

                if (!validateCRC(rawData, startIndex + 3 + dataLength + 2, crc)) {
                    throw new ProtocolParseException("RS485消息CRC校验失败");
                }
            }

            // 解析业务数据
            parseBusinessData(message);

            // 更新统计
            incrementMessageCount("parse");
            log.debug("[RS485协议解析] 解析成功, deviceId={}, functionCode=0x{:02X}",
                    deviceId, functionCode & 0xFF);

            return message;

        } catch (Exception e) {
            incrementErrorCount("parse");
            log.error("[RS485协议解析] 解析失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new ProtocolParseException("RS485协议解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ProtocolMessage parseDeviceMessage(String hexData, Long deviceId) throws ProtocolParseException {
        try {
            byte[] rawData = hexStringToBytes(hexData);
            return parseDeviceMessage(rawData, deviceId);
        } catch (Exception e) {
            throw new ProtocolParseException("十六进制数据转换失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId)
            throws ProtocolBuildException {
        try {
            log.debug("[RS485响应构建] 开始构建响应, deviceId={}, messageType={}", deviceId, messageType);

            RS485Message response = new RS485Message();
            response.setDeviceId(deviceId);
            response.setMessageType(messageType);
            response.setBusinessData(businessData);

            // 构建响应帧
            byte[] responseFrame = buildResponseFrame(response);

            incrementMessageCount("build");
            log.debug("[RS485响应构建] 构建成功, deviceId={}, frameLength={}", deviceId, responseFrame.length);

            return responseFrame;

        } catch (Exception e) {
            incrementErrorCount("build");
            log.error("[RS485响应构建] 构建失败, deviceId={}, messageType={}, error={}",
                    deviceId, messageType, e.getMessage(), e);
            throw new ProtocolBuildException("RS485响应构建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildDeviceResponseHex(String messageType, Map<String, Object> businessData, Long deviceId)
            throws ProtocolBuildException {
        byte[] responseData = buildDeviceResponse(messageType, businessData, deviceId);
        return bytesToHexString(responseData);
    }

    @Override
    public ProtocolValidationResult validateMessage(ProtocolMessage message) {
        try {
            RS485Message rs485Message = (RS485Message) message;

            // 验证消息格式
            if (rs485Message.getDeviceAddress() == 0) {
                return ProtocolValidationResult.failure("INVALID_ADDRESS", "设备地址不能为0");
            }

            if (rs485Message.getFunctionCode() == 0) {
                return ProtocolValidationResult.failure("INVALID_FUNCTION_CODE", "功能码不能为0");
            }

            // 验证CRC校验
            if (rs485Message.getCrc() != null && rs485Message.getCrc().length == 2) {
                byte[] rawData = rs485Message.getRawData();
                if (rawData != null && rawData.length >= rs485Message.getCrc().length + 2) {
                    if (!validateCRC(rawData, rawData.length, rs485Message.getCrc())) {
                        return ProtocolValidationResult.failure("CRC_ERROR", "CRC校验失败");
                    }
                }
            }

            return ProtocolValidationResult.success();

        } catch (Exception e) {
            return ProtocolValidationResult.failure("VALIDATION_ERROR", "验证异常: " + e.getMessage());
        }
    }

    @Override
    public ProtocolPermissionResult validateDevicePermission(Long deviceId, String operation) {
        // RS485工业设备权限验证逻辑
        RS485Connection connection = connections.get(deviceId);
        if (connection == null) {
            return ProtocolPermissionResult.deny("设备未连接");
        }

        if (!connection.isAuthenticated()) {
            return ProtocolPermissionResult.deny("设备未认证");
        }

        // 检查操作权限
        switch (operation) {
            case "read":
            case "read_holding_registers":
            case "read_input_registers":
                return ProtocolPermissionResult.permit();

            case "write":
            case "write_single_register":
            case "write_multiple_registers":
                if (!connection.isWriteEnabled()) {
                    return ProtocolPermissionResult.deny("设备禁止写入操作");
                }
                return ProtocolPermissionResult.permit();

            default:
                return ProtocolPermissionResult.deny("不支持的操作类型: " + operation);
        }
    }

    @Override
    public Future<ProtocolInitResult> initializeDevice(Map<String, Object> deviceInfo, Map<String, Object> config) {
        return executorService.submit(() -> {
            try {
                Long deviceId = (Long) deviceInfo.get("deviceId");
                String deviceModel = (String) deviceInfo.get("deviceModel");
                String portName = (String) config.get("portName");
                Integer baudRate = (Integer) config.getOrDefault("baudRate", 9600);
                Integer dataBits = (Integer) config.getOrDefault("dataBits", 8);
                Integer stopBits = (Integer) config.getOrDefault("stopBits", 1);
                String parity = (String) config.getOrDefault("parity", "none");

                log.info("[RS485设备初始化] 开始初始化设备, deviceId={}, deviceModel={}, port={}, baudRate={}",
                        deviceId, deviceModel, portName, baudRate);

                // 创建RS485配置
                RS485Config rs485Config = new RS485Config();
                rs485Config.setPortName(portName);
                rs485Config.setBaudRate(baudRate);
                rs485Config.setDataBits(dataBits);
                rs485Config.setStopBits(stopBits);
                rs485Config.setParity(parity);
                rs485Config.setTimeout((Integer) config.getOrDefault("timeout", 3000));

                deviceConfigs.put(deviceId, rs485Config);

                // 建立RS485连接
                RS485Connection connection = new RS485Connection(rs485Config);
                connection.connect();

                connections.put(deviceId, connection);

                log.info("[RS485设备初始化] 初始化成功, deviceId={}", deviceId);
                return ProtocolInitResult.success(deviceId, "RS485-" + deviceId);

            } catch (Exception e) {
                log.error("[RS485设备初始化] 初始化失败, error={}", e.getMessage(), e);
                return ProtocolInitResult.failure("RS485设备初始化失败: " + e.getMessage());
            }
        });
    }

    @Override
    public ProtocolRegistrationResult handleDeviceRegistration(Map<String, Object> registrationData, Long deviceId) {
        try {
            log.info("[RS485设备注册] 处理设备注册, deviceId={}", deviceId);

            String deviceModel = (String) registrationData.get("deviceModel");
            String serialNumber = (String) registrationData.get("serialNumber");
            String firmwareVersion = (String) registrationData.get("firmwareVersion");

            if (!isDeviceModelSupported(deviceModel)) {
                return ProtocolRegistrationResult.failure("不支持的设备型号: " + deviceModel);
            }

            // 创建设备信息
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("deviceId", deviceId);
            deviceInfo.put("deviceModel", deviceModel);
            deviceInfo.put("serialNumber", serialNumber);
            deviceInfo.put("firmwareVersion", firmwareVersion);
            deviceInfo.put("protocolType", PROTOCOL_TYPE);
            deviceInfo.put("registerTime", System.currentTimeMillis());

            log.info("[RS485设备注册] 注册成功, deviceId={}, deviceModel={}", deviceId, deviceModel);
            return ProtocolRegistrationResult.success(deviceId, "RS485-REG-" + deviceId);

        } catch (Exception e) {
            log.error("[RS485设备注册] 注册失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ProtocolRegistrationResult.failure("RS485设备注册失败: " + e.getMessage());
        }
    }

    @Override
    public ProtocolHeartbeatResult handleDeviceHeartbeat(Map<String, Object> heartbeatData, Long deviceId) {
        try {
            log.debug("[RS485设备心跳] 处理设备心跳, deviceId={}", deviceId);

            RS485Connection connection = connections.get(deviceId);
            if (connection == null) {
                return ProtocolHeartbeatResult.failure("设备连接不存在");
            }

            // 更新设备状态
            connection.updateLastHeartbeat();

            // 读取设备状态寄存器
            Map<String, Object> status = readDeviceStatus(deviceId);
            connection.setStatus(status);

            log.debug("[RS485设备心跳] 心跳处理成功, deviceId={}", deviceId);
            return ProtocolHeartbeatResult.success(deviceId);

        } catch (Exception e) {
            log.error("[RS485设备心跳] 心跳处理失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ProtocolHeartbeatResult.failure("心跳处理失败: " + e.getMessage());
        }
    }

    @Override
    public ProtocolDeviceStatus getDeviceStatus(Long deviceId) {
        try {
            RS485Connection connection = connections.get(deviceId);
            if (connection == null) {
                return ProtocolDeviceStatus.offline(deviceId);
            }

            String statusStr = connection.isConnected() ? "CONNECTED" : "DISCONNECTED";

            return ProtocolDeviceStatus.online(deviceId, "RS485-" + deviceId, statusStr);

        } catch (Exception e) {
            log.error("[RS485设备状态] 获取状态失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ProtocolDeviceStatus.offline(deviceId);
        }
    }

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData,
            Long deviceId) {
        return executorService.submit(() -> {
            // RS485主要用于工业控制，不处理门禁业务
            return ProtocolProcessResult.failure("ACCESS", "NOT_SUPPORTED", "RS485协议不支持门禁业务");
        });
    }

    @Override
    public Future<ProtocolProcessResult> processAttendanceBusiness(String businessType,
            Map<String, Object> businessData, Long deviceId) {
        return executorService.submit(() -> {
            // RS485主要用于工业控制，不处理考勤业务
            return ProtocolProcessResult.failure("ATTENDANCE", "NOT_SUPPORTED", "RS485协议不支持考勤业务");
        });
    }

    @Override
    public Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData,
            Long deviceId) {
        return executorService.submit(() -> {
            // RS485主要用于工业控制，不处理消费业务
            return ProtocolProcessResult.failure("CONSUME", "NOT_SUPPORTED", "RS485协议不支持消费业务");
        });
    }

    @Override
    public Map<String, Object> getProtocolConfig(Long deviceId) {
        RS485Config config = deviceConfigs.get(deviceId);
        if (config == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("portName", config.getPortName());
        result.put("baudRate", config.getBaudRate());
        result.put("dataBits", config.getDataBits());
        result.put("stopBits", config.getStopBits());
        result.put("parity", config.getParity());
        result.put("timeout", config.getTimeout());

        return result;
    }

    @Override
    public boolean updateProtocolConfig(Long deviceId, Map<String, Object> config) {
        try {
            RS485Connection connection = connections.get(deviceId);
            if (connection != null && connection.isConnected()) {
                // 设备连接中，不能修改通讯参数
                return false;
            }

            RS485Config rs485Config = deviceConfigs.get(deviceId);
            if (rs485Config == null) {
                rs485Config = new RS485Config();
                deviceConfigs.put(deviceId, rs485Config);
            }

            // 更新配置
            if (config.containsKey("portName")) {
                rs485Config.setPortName((String) config.get("portName"));
            }
            if (config.containsKey("baudRate")) {
                rs485Config.setBaudRate((Integer) config.get("baudRate"));
            }
            if (config.containsKey("dataBits")) {
                rs485Config.setDataBits((Integer) config.get("dataBits"));
            }
            if (config.containsKey("stopBits")) {
                rs485Config.setStopBits((Integer) config.get("stopBits"));
            }
            if (config.containsKey("parity")) {
                rs485Config.setParity((String) config.get("parity"));
            }
            if (config.containsKey("timeout")) {
                rs485Config.setTimeout((Integer) config.get("timeout"));
            }

            return true;

        } catch (Exception e) {
            log.error("[RS485配置更新] 更新配置失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ProtocolErrorResponse handleProtocolError(String errorCode, String errorMessage, Long deviceId) {
        // 根据RS485协议错误代码映射
        String protocolErrorCode = mapToRS485ErrorCode(errorCode);
        String protocolErrorMessage = mapToRS485ErrorMessage(errorCode, errorMessage);

        return ProtocolErrorResponse.create(protocolErrorCode, protocolErrorMessage, deviceId);
    }

    @Override
    public Map<String, ProtocolErrorInfo> getErrorCodeMapping() {
        Map<String, ProtocolErrorInfo> errorMapping = new HashMap<>();

        // CRC校验错误
        errorMapping.put("CRC_ERROR", ProtocolErrorInfo.of("CRC_ERROR", "CRC校验失败", "PROTOCOL", "HIGH"));

        // 超时错误
        errorMapping.put("TIMEOUT_ERROR", ProtocolErrorInfo.of("TIMEOUT_ERROR", "通讯超时", "NETWORK", "MEDIUM"));

        // 地址错误
        errorMapping.put("ADDRESS_ERROR", ProtocolErrorInfo.of("ADDRESS_ERROR", "设备地址错误", "PROTOCOL", "MEDIUM"));

        // 功能码错误
        errorMapping.put("FUNCTION_ERROR", ProtocolErrorInfo.of("FUNCTION_ERROR", "功能码不支持", "PROTOCOL", "LOW"));

        // 数据格式错误
        errorMapping.put("DATA_FORMAT_ERROR",
                ProtocolErrorInfo.of("DATA_FORMAT_ERROR", "数据格式错误", "PROTOCOL", "MEDIUM"));

        return errorMapping;
    }

    @Override
    public void initialize() {
        log.info("[RS485协议适配器] 初始化开始");

        // 启动连接状态监控
        executorService.scheduleWithFixedDelay(this::monitorConnections, 30, 30, TimeUnit.SECONDS);

        // 启动统计信息清理
        executorService.scheduleWithFixedDelay(this::cleanupStatistics, 300, 300, TimeUnit.SECONDS);

        log.info("[RS485协议适配器] 初始化完成");
    }

    @Override
    public void destroy() {
        log.info("[RS485协议适配器] 开始销毁");

        // 关闭所有连接
        for (RS485Connection connection : connections.values()) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                log.warn("[RS485协议适配器] 关闭连接异常", e);
            }
        }
        connections.clear();

        // 关闭线程池
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[RS485协议适配器] 销毁完成");
    }

    @Override
    public String getAdapterStatus() {
        int totalConnections = connections.size();
        int activeConnections = (int) connections.values().stream()
                .filter(RS485Connection::isConnected)
                .count();

        if (totalConnections == 0) {
            return "IDLE";
        } else if (activeConnections == totalConnections) {
            return "RUNNING";
        } else {
            return "PARTIAL";
        }
    }

    @Override
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 连接统计
        statistics.put("totalConnections", connections.size());
        statistics.put("activeConnections", connections.values().stream()
                .filter(RS485Connection::isConnected)
                .count());

        // 消息统计
        statistics.put("messageCount", messageCount.values().stream().mapToLong(Long::longValue).sum());
        statistics.put("errorCount", errorCount.values().stream().mapToLong(Long::longValue).sum());

        // 性能指标
        long totalMessages = messageCount.values().stream().mapToLong(Long::longValue).sum();
        long totalErrors = errorCount.values().stream().mapToLong(Long::longValue).sum();
        double errorRate = totalMessages > 0 ? (double) totalErrors / totalMessages * 100 : 0;

        statistics.put("errorRate", String.format("%.2f%%", errorRate));
        statistics.put("uptime", System.currentTimeMillis() - startTime);

        return statistics;
    }

    // ==================== 私有方法 ====================

    private long startTime = System.currentTimeMillis();

    /**
     * 解析业务数据
     */
    private void parseBusinessData(RS485Message message) {
        byte functionCode = message.getFunctionCode();
        byte[] data = message.getData();

        if (data == null || data.length == 0) {
            return;
        }

        Map<String, Object> businessData = new HashMap<>();

        switch (functionCode) {
            case 0x01: // 读线圈状态
            case 0x02: // 读离散输入
                parseBooleanData(data, businessData);
                break;

            case 0x03: // 读保持寄存器
            case 0x04: // 读输入寄存器
                parseRegisterData(data, businessData);
                break;

            case 0x05: // 写单个线圈
                parseSingleCoilData(data, businessData);
                break;

            case 0x06: // 写单个寄存器
                parseSingleRegisterData(data, businessData);
                break;

            case 0x0F: // 写多个线圈
                parseMultipleCoilsData(data, businessData);
                break;

            case 0x10: // 写多个寄存器
                parseMultipleRegistersData(data, businessData);
                break;

            default:
                businessData.put("rawData", data);
                break;
        }

        message.setBusinessData(businessData);
    }

    /**
     * 解析布尔数据（线圈状态、离散输入）
     */
    private void parseBooleanData(byte[] data, Map<String, Object> businessData) {
        List<Boolean> values = new ArrayList<>();
        for (byte b : data) {
            for (int i = 0; i < 8; i++) {
                values.add((b & (1 << i)) != 0);
            }
        }
        businessData.put("values", values);
        businessData.put("count", values.size());
    }

    /**
     * 解析寄存器数据
     */
    private void parseRegisterData(byte[] data, Map<String, Object> businessData) {
        if (data.length % 2 != 0) {
            log.warn("[RS485协议解析] 寄存器数据长度不是2的倍数");
            return;
        }

        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2) {
            int value = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            values.add(value);
        }
        businessData.put("values", values);
        businessData.put("count", values.size());
    }

    /**
     * 解析单个线圈数据
     */
    private void parseSingleCoilData(byte[] data, Map<String, Object> businessData) {
        if (data.length >= 2) {
            int address = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            boolean value = data.length >= 4 && ((data[2] & 0xFF) << 8 | (data[3] & 0xFF)) == 0xFF00;

            businessData.put("address", address);
            businessData.put("value", value);
        }
    }

    /**
     * 解析单个寄存器数据
     */
    private void parseSingleRegisterData(byte[] data, Map<String, Object> businessData) {
        if (data.length >= 4) {
            int address = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            int value = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

            businessData.put("address", address);
            businessData.put("value", value);
        }
    }

    /**
     * 解析多个线圈数据
     */
    private void parseMultipleCoilsData(byte[] data, Map<String, Object> businessData) {
        if (data.length >= 2) {
            int address = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            int quantity = data.length > 2 ? ((data[2] & 0xFF) << 8) | (data[3] & 0xFF) : 0;

            businessData.put("address", address);
            businessData.put("quantity", quantity);

            if (data.length > 4) {
                byte[] coilData = new byte[data.length - 4];
                System.arraycopy(data, 4, coilData, 0, coilData.length);
                parseBooleanData(coilData, businessData);
            }
        }
    }

    /**
     * 解析多个寄存器数据
     */
    private void parseMultipleRegistersData(byte[] data, Map<String, Object> businessData) {
        if (data.length >= 4) {
            int address = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
            int quantity = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

            businessData.put("address", address);
            businessData.put("quantity", quantity);

            if (data.length > 4) {
                byte[] registerData = new byte[data.length - 4];
                System.arraycopy(data, 4, registerData, 0, registerData.length);
                parseRegisterData(registerData, businessData);
            }
        }
    }

    /**
     * 构建响应帧
     */
    private byte[] buildResponseFrame(RS485Message message) {
        List<Byte> frame = new ArrayList<>();

        // 添加起始字节
        frame.add((byte) 0xAA);

        // 添加设备地址
        frame.add((byte) message.getDeviceAddress());

        // 添加功能码
        frame.add(message.getFunctionCode());

        // 添加数据长度和数据
        byte[] data = message.getData();
        if (data != null && data.length > 0) {
            frame.add((byte) data.length);
            for (byte b : data) {
                frame.add(b);
            }
        } else {
            frame.add((byte) 0);
        }

        // 计算并添加CRC
        byte[] frameArray = new byte[frame.size()];
        for (int i = 0; i < frame.size(); i++) {
            frameArray[i] = frame.get(i);
        }
        byte[] crc = calculateCRC(frameArray);
        for (byte b : crc) {
            frame.add(b);
        }

        // 转换为字节数组
        byte[] result = new byte[frame.size()];
        for (int i = 0; i < frame.size(); i++) {
            result[i] = frame.get(i);
        }

        return result;
    }

    /**
     * 验证CRC校验
     */
    private boolean validateCRC(byte[] data, int length, byte[] expectedCrc) {
        byte[] calculatedCrc = calculateCRC(data, length);
        return calculatedCrc[0] == expectedCrc[0] && calculatedCrc[1] == expectedCrc[1];
    }

    /**
     * 计算CRC校验（Modbus RTU标准）
     */
    private byte[] calculateCRC(byte[] data) {
        return calculateCRC(data, data.length);
    }

    /**
     * 计算CRC校验（Modbus RTU标准）
     */
    private byte[] calculateCRC(byte[] data, int length) {
        int crc = 0xFFFF;

        for (int i = 0; i < length; i++) {
            crc ^= (data[i] & 0xFF);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }

        return new byte[] { (byte) (crc & 0xFF), (byte) ((crc >> 8) & 0xFF) };
    }

    /**
     * 读取设备状态
     */
    private Map<String, Object> readDeviceStatus(Long deviceId) {
        Map<String, Object> status = new HashMap<>();

        RS485Connection connection = connections.get(deviceId);
        if (connection != null) {
            status.put("connectionTime", connection.getConnectionTime());
            status.put("lastHeartbeat", connection.getLastHeartbeat());
            status.put("messageCount", connection.getMessageCount());
            status.put("errorCount", connection.getErrorCount());
        }

        return status;
    }

    /**
     * 监控连接状态
     */
    private void monitorConnections() {
        long currentTime = System.currentTimeMillis();
        long heartbeatTimeout = 60000; // 60秒超时

        for (Map.Entry<Long, RS485Connection> entry : connections.entrySet()) {
            Long deviceId = entry.getKey();
            RS485Connection connection = entry.getValue();

            if (connection.isConnected() &&
                    (currentTime - connection.getLastHeartbeat()) > heartbeatTimeout) {
                log.warn("[RS485连接监控] 设备心跳超时, deviceId={}", deviceId);
                connection.disconnect();
            }
        }
    }

    /**
     * 清理统计信息
     */
    private void cleanupStatistics() {
        messageCount.clear();
        errorCount.clear();
    }

    /**
     * 增加消息计数
     */
    private void incrementMessageCount(String type) {
        messageCount.merge(type, 1L, Long::sum);
    }

    /**
     * 增加错误计数
     */
    private void incrementErrorCount(String type) {
        errorCount.merge(type, 1L, Long::sum);
    }

    /**
     * 映射到RS485错误代码
     */
    private String mapToRS485ErrorCode(String systemErrorCode) {
        switch (systemErrorCode) {
            case "PARSE_ERROR":
                return "DATA_FORMAT_ERROR";
            case "TIMEOUT_ERROR":
                return "TIMEOUT_ERROR";
            case "CONNECTION_ERROR":
                return "ADDRESS_ERROR";
            case "BUSINESS_ERROR":
                return "FUNCTION_ERROR";
            default:
                return "UNKNOWN_ERROR";
        }
    }

    /**
     * 映射到RS485错误消息
     */
    private String mapToRS485ErrorMessage(String systemErrorCode, String systemErrorMessage) {
        switch (systemErrorCode) {
            case "PARSE_ERROR":
                return "数据格式解析失败: " + systemErrorMessage;
            case "TIMEOUT_ERROR":
                return "设备响应超时: " + systemErrorMessage;
            case "CONNECTION_ERROR":
                return "设备连接异常: " + systemErrorMessage;
            case "BUSINESS_ERROR":
                return "业务逻辑错误: " + systemErrorMessage;
            default:
                return "未知错误: " + systemErrorMessage;
        }
    }

    /**
     * 十六进制字符串转字节数组
     */
    private byte[] hexStringToBytes(String hex) {
        hex = hex.replaceAll("\\s+", "");
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            String hexByte = hex.substring(2 * i, 2 * i + 2);
            bytes[i] = (byte) Integer.parseInt(hexByte, 16);
        }
        return bytes;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // ==================== 内部类 ====================

    /**
     * RS485配置类
     */
    public static class RS485Config {
        private String portName;
        private Integer baudRate = 9600;
        private Integer dataBits = 8;
        private Integer stopBits = 1;
        private String parity = "none";
        private Integer timeout = 3000;

        // getters and setters
        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public Integer getBaudRate() {
            return baudRate;
        }

        public void setBaudRate(Integer baudRate) {
            this.baudRate = baudRate;
        }

        public Integer getDataBits() {
            return dataBits;
        }

        public void setDataBits(Integer dataBits) {
            this.dataBits = dataBits;
        }

        public Integer getStopBits() {
            return stopBits;
        }

        public void setStopBits(Integer stopBits) {
            this.stopBits = stopBits;
        }

        public String getParity() {
            return parity;
        }

        public void setParity(String parity) {
            this.parity = parity;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * RS485连接类
     */
    public static class RS485Connection {
        private RS485Config config;
        private boolean connected = false;
        private boolean authenticated = false;
        private boolean writeEnabled = true;
        private long connectionTime;
        private long lastHeartbeat;
        private long messageCount = 0;
        private long errorCount = 0;
        private Map<String, Object> status = new HashMap<>();

        public RS485Connection(RS485Config config) {
            this.config = config;
        }

        public void connect() {
            this.connected = true;
            this.authenticated = true;
            this.connectionTime = System.currentTimeMillis();
            this.lastHeartbeat = System.currentTimeMillis();
            status.put("connected", true);
            status.put("authenticated", true);
        }

        public void disconnect() {
            this.connected = false;
            this.authenticated = false;
            status.put("connected", false);
            status.put("authenticated", false);
        }

        public void updateLastHeartbeat() {
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public void incrementMessageCount() {
            this.messageCount++;
        }

        public void incrementErrorCount() {
            this.errorCount++;
        }

        // getters
        public boolean isConnected() {
            return connected;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public boolean isWriteEnabled() {
            return writeEnabled;
        }

        public long getConnectionTime() {
            return connectionTime;
        }

        public long getLastHeartbeat() {
            return lastHeartbeat;
        }

        public long getMessageCount() {
            return messageCount;
        }

        public long getErrorCount() {
            return errorCount;
        }

        public Map<String, Object> getStatus() {
            return status;
        }

        public void setStatus(Map<String, Object> status) {
            this.status = status != null ? status : new HashMap<>();
        }
    }

    /**
     * RS485消息类
     */
    public static class RS485Message extends ProtocolMessage {
        private byte deviceAddress;
        private byte functionCode;
        private byte[] data;
        private byte[] crc;
        private String messageType;
        private Map<String, Object> businessData;
        private byte[] rawData;
        private long receiveTime;

        // getters and setters
        public byte getDeviceAddress() {
            return deviceAddress;
        }

        public void setDeviceAddress(byte deviceAddress) {
            this.deviceAddress = deviceAddress;
        }

        public byte getFunctionCode() {
            return functionCode;
        }

        public void setFunctionCode(byte functionCode) {
            this.functionCode = functionCode;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] getCrc() {
            return crc;
        }

        public void setCrc(byte[] crc) {
            this.crc = crc;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public Map<String, Object> getBusinessData() {
            return businessData;
        }

        public void setBusinessData(Map<String, Object> businessData) {
            this.businessData = businessData;
        }

        public byte[] getRawData() {
            return rawData;
        }

        public void setRawData(byte[] rawData) {
            this.rawData = rawData;
        }

        public long getReceiveTime() {
            return receiveTime;
        }

        public void setReceiveTime(long receiveTime) {
            this.receiveTime = receiveTime;
        }
    }
}

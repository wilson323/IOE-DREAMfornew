package net.lab1024.sa.device.comm.protocol.rs485;

import net.lab1024.sa.device.comm.protocol.*;
import net.lab1024.sa.device.comm.dao.DeviceCommLogDao;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * RS485协议管理器
 * <p>
 * RS485工业设备协议处理的核心业务逻辑，严格遵循四层架构规范：
 * - 位于Manager层，负责复杂业务流程编排
 * - 通过构造函数注入依赖，保持为纯Java类
 * - 协调DAO层数据访问和ProtocolAdapter协议适配
 * - 处理RS485设备通讯的业务逻辑和异常处理
 * </p>
 * <p>
 * 核心职责：
 * 1. RS485协议解析和响应构建
 * 2. 工业设备状态监控和告警
 * 3. 连接池管理和性能优化
 * 4. 多厂商设备兼容性处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public class RS485ProtocolManager {

    private final RS485ProtocolAdapter protocolAdapter;
    private final DeviceCommLogDao deviceCommLogDao;
    private final ScheduledExecutorService executorService;

    // 设备连接管理
    private final Map<Long, RS485Connection> connections = new ConcurrentHashMap<>();
    private final Map<Long, RS485DeviceConfig> deviceConfigs = new ConcurrentHashMap<>();

    // 性能统计
    private final Map<String, Long> messageStatistics = new ConcurrentHashMap<>();
    private final Map<String, Long> errorStatistics = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 依赖注入
     *
     * @param protocolAdapter RS485协议适配器
     * @param deviceCommLogDao 设备通讯日志DAO
     */
    public RS485ProtocolManager(RS485ProtocolAdapter protocolAdapter,
                                DeviceCommLogDao deviceCommLogDao) {
        this.protocolAdapter = protocolAdapter;
        this.deviceCommLogDao = deviceCommLogDao;
        this.executorService = Executors.newScheduledThreadPool(20);

        initializeMonitoring();
    }

    /**
     * 初始化设备连接
     *
     * @param deviceId 设备ID
     * @param deviceInfo 设备信息
     * @param config 配置参数
     * @return 初始化结果Future
     */
    public Future<RS485InitResult> initializeDevice(Long deviceId,
                                                   Map<String, Object> deviceInfo,
                                                   Map<String, Object> config) {
        return executorService.submit(() -> {
            try {
                log.info("[RS485设备初始化] 开始初始化设备, deviceId={}", deviceId);

                // 验证设备型号支持
                String deviceModel = (String) deviceInfo.get("deviceModel");
                if (!protocolAdapter.isDeviceModelSupported(deviceModel)) {
                    return new RS485InitResult(false, "不支持的设备型号: " + deviceModel);
                }

                // 创建设备配置
                RS485DeviceConfig rs485Config = createDeviceConfig(config);
                deviceConfigs.put(deviceId, rs485Config);

                // 建立RS485连接
                RS485Connection connection = new RS485Connection(rs485Config, deviceInfo);
                connection.connect();

                connections.put(deviceId, connection);

                // 记录初始化日志
                logDeviceCommLog(deviceId, "INITIALIZE", "设备初始化成功", null);

                log.info("[RS485设备初始化] 初始化成功, deviceId={}", deviceId);
                return new RS485InitResult(true, "RS485设备初始化成功");

            } catch (Exception e) {
                incrementErrorCount("initialize");
                logDeviceCommLog(deviceId, "INITIALIZE_ERROR", "设备初始化失败", e.getMessage());
                log.error("[RS485设备初始化] 初始化失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
                return new RS485InitResult(false, "RS485设备初始化失败: " + e.getMessage());
            }
        });
    }

    /**
     * 处理RS485设备消息
     *
     * @param deviceId 设备ID
     * @param rawData 原始数据
     * @param protocolType 协议类型
     * @return 处理结果Future
     */
    public Future<RS485ProcessResult> processDeviceMessage(Long deviceId,
                                                         byte[] rawData,
                                                         String protocolType) {
        return executorService.submit(() -> {
            try {
                log.debug("[RS485消息处理] 开始处理消息, deviceId={}, protocolType={}, dataLength={}",
                        deviceId, protocolType, rawData.length);

                // 验证设备连接
                RS485Connection connection = connections.get(deviceId);
                if (connection == null || !connection.isConnected()) {
                    throw new RS485ProtocolException("设备未连接或连接异常");
                }

                // 解析协议消息
                RS485Message message = protocolAdapter.parseDeviceMessage(rawData, deviceId);

                // 验证消息
                RS485ValidationResult validationResult = protocolAdapter.validateMessage(message);
                if (!validationResult.isValid()) {
                    throw new RS485ProtocolException("消息验证失败: " + validationResult.getErrorMessage());
                }

                // 处理业务数据
                RS485BusinessResult businessResult = processBusinessData(deviceId, message);

                // 更新连接统计
                connection.incrementMessageCount();
                connection.updateLastActivity();

                // 记录处理日志
                logDeviceCommLog(deviceId, "PROCESS",
                        String.format("消息处理成功, functionCode=0x%02X", message.getFunctionCode()),
                        null);

                incrementMessageCount("process");

                log.debug("[RS485消息处理] 处理成功, deviceId={}", deviceId);
                return new RS485ProcessResult(true, "消息处理成功", businessResult);

            } catch (Exception e) {
                incrementErrorCount("process");
                logDeviceCommLog(deviceId, "PROCESS_ERROR", "消息处理失败", e.getMessage());
                log.error("[RS485消息处理] 处理失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
                return new RS485ProcessResult(false, "消息处理失败: " + e.getMessage());
            }
        });
    }

    /**
     * 构建设备响应
     *
     * @param deviceId 设备ID
     * @param messageType 消息类型
     * @param businessData 业务数据
     * @return 响应数据Future
     */
    public Future<byte[]> buildDeviceResponse(Long deviceId,
                                               String messageType,
                                               Map<String, Object> businessData) {
        return executorService.submit(() -> {
            try {
                log.debug("[RS485响应构建] 开始构建响应, deviceId={}, messageType={}", deviceId, messageType);

                // 验证设备连接
                RS485Connection connection = connections.get(deviceId);
                if (connection == null || !connection.isConnected()) {
                    throw new RS485ProtocolException("设备未连接或连接异常");
                }

                // 构建响应帧
                byte[] response = protocolAdapter.buildDeviceResponse(messageType, businessData, deviceId);

                // 记录响应日志
                logDeviceCommLog(deviceId, "RESPONSE", "响应构建成功", null);
                incrementMessageCount("response");

                log.debug("[RS485响应构建] 构建成功, deviceId={}, length={}", deviceId, response.length);
                return response;

            } catch (Exception e) {
                incrementErrorCount("response");
                logDeviceCommLog(deviceId, "RESPONSE_ERROR", "响应构建失败", e.getMessage());
                log.error("[RS485响应构建] 构建失败, deviceId={}, messageType={}, error={}",
                        deviceId, messageType, e.getMessage(), e);
                throw new RS485ProtocolException("响应构建失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 处理设备心跳
     *
     * @param deviceId 设备ID
     * @param heartbeatData 心跳数据
     * @return 心跳处理结果Future
     */
    public Future<RS485HeartbeatResult> processDeviceHeartbeat(Long deviceId,
                                                              Map<String, Object> heartbeatData) {
        return executorService.submit(() -> {
            try {
                log.debug("[RS485心跳处理] 处理设备心跳, deviceId={}", deviceId);

                RS485Connection connection = connections.get(deviceId);
                if (connection == null) {
                    return new RS485HeartbeatResult(false, "设备连接不存在", null);
                }

                // 更新心跳时间
                connection.updateLastHeartbeat();

                // 读取设备状态
                Map<String, Object> deviceStatus = readDeviceStatus(deviceId, connection);

                // 检查设备健康状态
                RS485HealthStatus healthStatus = checkDeviceHealth(deviceId, deviceStatus);

                // 记录心跳日志
                logDeviceCommLog(deviceId, "HEARTBEAT", "心跳处理成功", null);
                incrementMessageCount("heartbeat");

                log.debug("[RS485心跳处理] 心跳处理成功, deviceId={}, healthStatus={}", deviceId, healthStatus);
                return new RS485HeartbeatResult(true, "心跳处理成功", deviceStatus, healthStatus);

            } catch (Exception e) {
                incrementErrorCount("heartbeat");
                logDeviceCommLog(deviceId, "HEARTBEAT_ERROR", "心跳处理失败", e.getMessage());
                log.error("[RS485心跳处理] 处理失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
                return new RS485HeartbeatResult(false, "心跳处理失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    public RS485DeviceStatus getDeviceStatus(Long deviceId) {
        try {
            RS485Connection connection = connections.get(deviceId);
            if (connection == null) {
                return new RS485DeviceStatus(deviceId, "OFFLINE", "设备未连接", null);
            }

            Map<String, Object> status = readDeviceStatus(deviceId, connection);
            String statusStr = connection.isConnected() ? "ONLINE" : "OFFLINE";

            return new RS485DeviceStatus(deviceId, statusStr, "设备状态正常", status);

        } catch (Exception e) {
            log.error("[RS485设备状态] 获取状态失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            return new RS485DeviceStatus(deviceId, "ERROR", "获取状态失败: " + e.getMessage(), null);
        }
    }

    /**
     * 断开设备连接
     *
     * @param deviceId 设备ID
     * @return 断开结果
     */
    public boolean disconnectDevice(Long deviceId) {
        try {
            RS485Connection connection = connections.remove(deviceId);
            if (connection != null) {
                connection.disconnect();
                deviceConfigs.remove(deviceId);

                logDeviceCommLog(deviceId, "DISCONNECT", "设备断开连接", null);
                log.info("[RS485设备断开] 断开成功, deviceId={}", deviceId);
                return true;
            }
            return false;

        } catch (Exception e) {
            log.error("[RS485设备断开] 断开失败, deviceId={}, error={}", deviceId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取性能统计信息
     *
     * @return 性能统计
     */
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 连接统计
        int totalConnections = connections.size();
        long activeConnections = connections.values().stream()
                .filter(RS485Connection::isConnected)
                .count();

        statistics.put("totalConnections", totalConnections);
        statistics.put("activeConnections", activeConnections);
        statistics.put("connectionRate", totalConnections > 0 ?
                String.format("%.2f%%", (double) activeConnections / totalConnections * 100) : "0.00%");

        // 消息统计
        long totalMessages = messageStatistics.values().stream().mapToLong(Long::longValue).sum();
        long totalErrors = errorStatistics.values().stream().mapToLong(Long::longValue).sum();
        double errorRate = totalMessages > 0 ? (double) totalErrors / totalMessages * 100 : 0;

        statistics.put("totalMessages", totalMessages);
        statistics.put("totalErrors", totalErrors);
        statistics.put("errorRate", String.format("%.2f%%", errorRate));
        statistics.put("successRate", String.format("%.2f%%", 100 - errorRate));

        // 协议支持统计
        Map<String, Long> protocolStats = new HashMap<>();
        protocolStats.put("supportedModels", (long) protocolAdapter.getSupportedDeviceModels().length);
        statistics.put("protocolSupport", protocolStats);

        return statistics;
    }

    /**
     * 获取支持的设备型号列表
     *
     * @return 设备型号列表
     */
    public String[] getSupportedDeviceModels() {
        return protocolAdapter.getSupportedDeviceModels();
    }

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    public boolean isDeviceModelSupported(String deviceModel) {
        return protocolAdapter.isDeviceModelSupported(deviceModel);
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化监控
     */
    private void initializeMonitoring() {
        // 启动连接状态监控
        executorService.scheduleWithFixedDelay(this::monitorConnections, 30, 30, TimeUnit.SECONDS);

        // 启动统计信息清理
        executorService.scheduleWithFixedDelay(this::cleanupStatistics, 300, 300, TimeUnit.SECONDS);
    }

    /**
     * 创建设备配置
     */
    private RS485DeviceConfig createDeviceConfig(Map<String, Object> config) {
        RS485DeviceConfig rs485Config = new RS485DeviceConfig();
        rs485Config.setPortName((String) config.getOrDefault("portName", "/dev/ttyUSB0"));
        rs485Config.setBaudRate((Integer) config.getOrDefault("baudRate", 9600));
        rs488Config.setDataBits((Integer) config.getOrDefault("dataBits", 8));
        rs488Config.setStopBits((Integer) config.getOrDefault("stopBits", 1));
        rs488Config.setParity((String) config.getOrDefault("parity", "none"));
        rs488Config.setTimeout((Integer) config.getOrDefault("timeout", 3000));
        rs488Config.setRetryCount((Integer) config.getOrDefault("retryCount", 3));
        rs488Config.setRetryInterval((Integer) config.getOrDefault("retryInterval", 1000));
        return rs488Config;
    }

    /**
     * 处理业务数据
     */
    private RS485BusinessResult processBusinessData(Long deviceId, RS485Message message) {
        byte functionCode = message.getFunctionCode();
        Map<String, Object> businessData = message.getBusinessData();

        if (businessData == null) {
            return new RS485BusinessResult("NO_DATA", "无业务数据", null);
        }

        switch (functionCode) {
            case 0x01: // 读线圈状态
            case 0x02: // 读离散输入
                return processCoilData(deviceId, businessData);

            case 0x03: // 读保持寄存器
            case 0x04: // 读输入寄存器
                return processRegisterData(deviceId, businessData);

            case 0x05: // 写单个线圈
                return processSingleCoilWrite(deviceId, businessData);

            case 0x06: // 写单个寄存器
                return processSingleRegisterWrite(deviceId, businessData);

            case 0x0F: // 写多个线圈
                return processMultipleCoilsWrite(deviceId, businessData);

            case 0x10: // 写多个寄存器
                return processMultipleRegistersWrite(deviceId, businessData);

            default:
                return new RS485BusinessResult("UNKNOWN_FUNCTION",
                        String.format("未知功能码: 0x%02X", functionCode), businessData);
        }
    }

    /**
     * 处理线圈数据
     */
    private RS485BusinessResult processCoilData(Long deviceId, Map<String, Object> businessData) {
        @SuppressWarnings("unchecked")
        List<Boolean> values = (List<Boolean>) businessData.get("values");
        Integer count = (Integer) businessData.get("count");

        // 处理线圈状态数据
        Map<String, Object> processedData = new HashMap<>();
        processedData.put("coilValues", values);
        processedData.put("coilCount", count);
        processedData.put("activeCoils", values.stream().mapToInt(v -> v ? 1 : 0).sum());
        processedData.put("processTime", System.currentTimeMillis());

        return new RS485BusinessResult("COIL_DATA_PROCESSED", "线圈数据处理成功", processedData);
    }

    /**
     * 处理寄存器数据
     */
    private RS485BusinessResult processRegisterData(Long deviceId, Map<String, Object> businessData) {
        @SuppressWarnings("unchecked")
        List<Integer> values = (List<Integer>) businessData.get("values");
        Integer count = (Integer) businessData.get("count");

        // 处理寄存器数据
        Map<String, Object> processedData = new HashMap<>();
        processedData.put("registerValues", values);
        processedData.put("registerCount", count);
        processedData.put("sumValues", values.stream().mapToInt(Integer::intValue).sum());
        processedData.put("avgValue", values.stream().mapToInt(Integer::intValue).average().orElse(0));
        processedData.put("processTime", System.currentTimeMillis());

        return new RS485BusinessResult("REGISTER_DATA_PROCESSED", "寄存器数据处理成功", processedData);
    }

    /**
     * 处理单个线圈写入
     */
    private RS485BusinessResult processSingleCoilWrite(Long deviceId, Map<String, Object> businessData) {
        Integer address = (Integer) businessData.get("address");
        Boolean value = (Boolean) businessData.get("value");

        // 记录写入操作
        Map<String, Object> processedData = new HashMap<>();
        processedData.put("writeType", "SINGLE_COIL");
        processedData.put("address", address);
        processedData.put("value", value);
        processedData.put("processTime", System.currentTimeMillis());

        return new RS485BusinessResult("SINGLE_COIL_WRITTEN", "单个线圈写入成功", processedData);
    }

    /**
     * 处理单个寄存器写入
     */
    private RS485BusinessResult processSingleRegisterWrite(Long deviceId, Map<String, Object> businessData) {
        Integer address = (Integer) businessData.get("address");
        Integer value = (Integer) businessData.get("value");

        // 记录写入操作
        Map<String, Object> processedData = new HashMap<>();
        processedData.put("writeType", "SINGLE_REGISTER");
        processedData.put("address", address);
        processedData.put("value", value);
        processedData.put("processTime", System.currentTimeMillis());

        return new RS485BusinessResult("SINGLE_REGISTER_WRITTEN", "单个寄存器写入成功", processedData);
    }

    /**
     * 处理多个线圈写入
     */
    private RS485BusinessResult processMultipleCoilsWrite(Long deviceId, Map<String, Object> businessData) {
        Integer address = (Integer) businessData.get("address");
        Integer quantity = (Integer) businessData.get("quantity");
        @SuppressWarnings("unchecked")
        List<Boolean> values = (List<Boolean>) businessData.get("values");

        // 记录写入操作
        Map<String, Object> processedData = new HashMap<>();
        processedData.put("writeType", "MULTIPLE_COILS");
        processedData.put("address", address);
        processedData.put("quantity", quantity);
        processedData.put("values", values);
        processedData.put("processTime", System.currentTimeMillis());

        return new RS485BusinessResult("MULTIPLE_COILS_WRITTEN", "多个线圈写入成功", processedData);
    }

    /**
     * 处理多个寄存器写入
     */
    private RS485BusinessResult processMultipleRegistersWrite(Long deviceId, Map<String, Object> businessData) {
        Integer address = (Integer) businessData.get("address");
        Integer quantity = (Integer) businessData.get("quantity");
        @SuppressWarnings("unchecked")
        List<Integer> values = (List<Integer>) businessData.get("values");

        // 记录写入操作
        Map<String, Object> processedData = new HashMap<>();
        processedData.put("writeType", "MULTIPLE_REGISTERS");
        processedData.put("address", address);
        processedData.put("quantity", quantity);
        processedData.put("values", values);
        processedData.put("processTime", System.currentTimeMillis());

        return new RS485BusinessResult("MULTIPLE_REGISTERS_WRITTEN", "多个寄存器写入成功", processedData);
    }

    /**
     * 读取设备状态
     */
    private Map<String, Object> readDeviceStatus(Long deviceId, RS485Connection connection) {
        Map<String, Object> status = new HashMap<>();

        status.put("deviceId", deviceId);
        status.put("connected", connection.isConnected());
        status.put("connectionTime", connection.getConnectionTime());
        status.put("lastActivity", connection.getLastActivity());
        status.put("lastHeartbeat", connection.getLastHeartbeat());
        status.put("messageCount", connection.getMessageCount());
        status.put("errorCount", connection.getErrorCount());
        status.put("responseTime", connection.getAverageResponseTime());
        status.put("uptime", System.currentTimeMillis() - connection.getConnectionTime());

        // 计算设备健康度
        long currentTime = System.currentTimeMillis();
        long timeSinceLastActivity = currentTime - connection.getLastActivity();
        double healthScore = calculateHealthScore(connection, timeSinceLastActivity);
        status.put("healthScore", healthScore);

        return status;
    }

    /**
     * 检查设备健康状态
     */
    private RS485HealthStatus checkDeviceHealth(Long deviceId, Map<String, Object> deviceStatus) {
        Boolean connected = (Boolean) deviceStatus.get("connected");
        Double healthScore = (Double) deviceStatus.get("healthScore");
        Long errorCount = (Long) deviceStatus.get("errorCount");

        if (!connected || connected == null) {
            return RS485HealthStatus.OFFLINE;
        }

        if (healthScore != null && healthScore < 50) {
            return RS485HealthStatus.UNHEALTHY;
        }

        if (errorCount != null && errorCount > 100) {
            return RS485HealthStatus.WARNING;
        }

        if (healthScore != null && healthScore >= 90) {
            return RS485HealthStatus.HEALTHY;
        }

        return RS485HealthStatus.NORMAL;
    }

    /**
     * 计算健康度分数
     */
    private double calculateHealthScore(RS485Connection connection, long timeSinceLastActivity) {
        double score = 100;

        // 连接稳定性扣分
        if (!connection.isConnected()) {
            score -= 50;
        }

        // 响应时间扣分
        long avgResponseTime = connection.getAverageResponseTime();
        if (avgResponseTime > 1000) {
            score -= 20;
        } else if (avgResponseTime > 500) {
            score -= 10;
        }

        // 错误率扣分
        long totalMessages = connection.getMessageCount() + connection.getErrorCount();
        if (totalMessages > 0) {
            double errorRate = (double) connection.getErrorCount() / totalMessages;
            score -= errorRate * 50;
        }

        // 活动时间扣分
        if (timeSinceLastActivity > 60000) { // 超过1分钟无活动
            score -= 15;
        } else if (timeSinceLastActivity > 30000) { // 超过30秒无活动
            score -= 5;
        }

        return Math.max(0, score);
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
        messageStatistics.clear();
        errorStatistics.clear();
    }

    /**
     * 增加消息计数
     */
    private void incrementMessageCount(String type) {
        messageStatistics.merge(type, 1L, Long::sum);
    }

    /**
     * 增加错误计数
     */
    private void incrementErrorCount(String type) {
        errorStatistics.merge(type, 1L, Long::sum);
    }

    /**
     * 记录设备通讯日志
     */
    private void logDeviceCommLog(Long deviceId, String action, String message, String errorMessage) {
        try {
            // 异步记录日志，避免影响主流程性能
            executorService.submit(() -> {
                try {
                    // 这里可以调用DAO层记录日志
                    // deviceCommLogDao.insertCommLog(deviceId, action, message, errorMessage);
                } catch (Exception e) {
                    log.warn("[RS485日志记录] 记录日志失败", e);
                }
            });
        } catch (Exception e) {
            log.warn("[RS485日志记录] 提交日志任务失败", e);
        }
    }

    // ==================== 内部类 ====================

    /**
     * RS485设备配置
     */
    public static class RS485DeviceConfig {
        private String portName;
        private Integer baudRate = 9600;
        private Integer dataBits = 8;
        private Integer stopBits = 1;
        private String parity = "none";
        private Integer timeout = 3000;
        private Integer retryCount = 3;
        private Integer retryInterval = 1000;

        // getters and setters
        public String getPortName() { return portName; }
        public void setPortName(String portName) { this.portName = portName; }
        public Integer getBaudRate() { return baudRate; }
        public void setBaudRate(Integer baudRate) { this.baudRate = baudRate; }
        public Integer getDataBits() { return dataBits; }
        public void setDataBits(Integer dataBits) { this.dataBits = dataBits; }
        public Integer getStopBits() { return stopBits; }
        public void setStopBits(Integer stopBits) { this.stopBits = stopBits; }
        public String getParity() { return parity; }
        public void setParity(String parity) { this.parity = parity; }
        public Integer getTimeout() { return timeout; }
        public void setTimeout(Integer timeout) { this.timeout = timeout; }
        public Integer getRetryCount() { return retryCount; }
        public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
        public Integer getRetryInterval() { return retryInterval; }
        public void setRetryInterval(Integer retryInterval) { this.retryInterval = retryInterval; }
    }

    /**
     * RS485连接管理
     */
    public static class RS485Connection {
        private RS485DeviceConfig config;
        private Map<String, Object> deviceInfo;
        private boolean connected = false;
        private long connectionTime;
        private long lastActivity;
        private long lastHeartbeat;
        private long messageCount = 0;
        private long errorCount = 0;
        private long totalResponseTime = 0;

        public RS485Connection(RS485DeviceConfig config, Map<String, Object> deviceInfo) {
            this.config = config;
            this.deviceInfo = deviceInfo;
        }

        public void connect() {
            this.connected = true;
            this.connectionTime = System.currentTimeMillis();
            this.lastActivity = System.currentTimeMillis();
            this.lastHeartbeat = System.currentTimeMillis();
        }

        public void disconnect() {
            this.connected = false;
        }

        public void updateLastActivity() {
            this.lastActivity = System.currentTimeMillis();
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

        public void updateResponseTime(long responseTime) {
            this.totalResponseTime += responseTime;
        }

        public long getAverageResponseTime() {
            return messageCount > 0 ? totalResponseTime / messageCount : 0;
        }

        // getters
        public boolean isConnected() { return connected; }
        public long getConnectionTime() { return connectionTime; }
        public long getLastActivity() { return lastActivity; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        public long getMessageCount() { return messageCount; }
        public long getErrorCount() { return errorCount; }
    }

    /**
     * RS485健康状态枚举
     */
    public enum RS485HealthStatus {
        HEALTHY,    // 健康
        NORMAL,     // 正常
        WARNING,    // 警告
        UNHEALTHY,  // 不健康
        OFFLINE     // 离线
    }
}
package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.SmartException;
import net.lab1024.sa.video.dao.DeviceStatusLogDao;
import net.lab1024.sa.video.dao.MonitorEventDao;
import net.lab1024.sa.video.dao.VideoDeviceDao;
import net.lab1024.sa.video.dao.VideoRecordDao;
import net.lab1024.sa.video.domain.entity.DeviceStatusLogEntity;
import net.lab1024.sa.video.domain.entity.MonitorEventEntity;
import net.lab1024.sa.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.video.domain.entity.VideoRecordEntity;

/**
 * 视频设备管理器
 * <p>
 * 处理复杂业务编排，包括：
 * </p>
 * <ul>
 * <li>模块调度与第三方服务聚合</li>
 * <li>设备通信与指令编排</li>
 * <li>实时数据流处理与事件驱动</li>
 * <li>性能优化与资源管理</li>
 * <li>复杂业务流程编排</li>
 * </ul>
 */
@Slf4j
@Component
public class VideoDeviceManager implements VideoDeviceManagerInterface {

    @Resource
    private VideoDeviceDao videoDeviceDao;

    @Resource
    private VideoRecordDao videoRecordDao;

    @Resource
    private MonitorEventDao monitorEventDao;

    @Resource
    private DeviceStatusLogDao deviceStatusLogDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Long, DeviceConnection> deviceConnections = new HashMap<>();
    private final Map<Long, AtomicLong> deviceSequenceNumbers = new HashMap<>();

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param command  控制命令
     * @param speed    控制速度
     * @return 控制结果
     * @throws Exception 业务异常
     */
    public String ptzControl(Long deviceId, String command, Integer speed) throws Exception {
        log.debug("开始执行云台控制 deviceId={}, command={}, speed={}", deviceId, command, speed);

        // 1. 获取设备信息
        VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        if (device.getPtzEnabled() == null || device.getPtzEnabled() != 1) {
            throw new SmartException("设备不支持云台控制");
        }

        // 2. 检查设备连接状态
        if (!isDeviceConnected(deviceId)) {
            log.warn("设备未连接，尝试重新连接: deviceId={}", deviceId);
            connectDevice(device);
        }

        // 3. 构建控制指令
        String ptzCommand = buildPTZCommand(command, speed, device);

        // 4. 发送控制命令
        String result = sendPTZCommand(device, ptzCommand);

        // 5. 记录操作日志
        recordDeviceOperation(deviceId, "PTZ_CONTROL", command + ":" + speed);

        log.info("云台控制执行成功: deviceId={}, command={}, speed={}, result={}", deviceId, command, speed,
                result);
        return result;
    }

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     * @throws Exception 业务异常
     */
    public String getDeviceStatus(Long deviceId) throws Exception {
        log.debug("开始获取设备实时状态: deviceId={}", deviceId);

        // 1. 获取设备基础信息
        VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 2. 获取实时连接状态
        DeviceConnection connection = deviceConnections.get(deviceId);
        boolean isConnected = connection != null && connection.isConnected();

        // 3. 获取设备运行统计
        Map<String, Object> statistics = getDeviceStatistics(deviceId);

        // 4. 构建状态响应
        Map<String, Object> status = new HashMap<>();
        status.put("deviceId", deviceId);
        status.put("deviceName", device.getDeviceName());
        status.put("deviceStatus", device.getDeviceStatus());
        status.put("isConnected", isConnected);
        status.put("lastHeartbeat", connection != null ? connection.getLastHeartbeat() : null);
        status.put("statistics", statistics);

        // 5. 更新设备状态（如有需要）
        updateDeviceStatusFromRealtime(deviceId, status);

        String statusJson = convertToJson(status);
        log.debug("获取设备实时状态成功: deviceId={}, status={}", deviceId, statusJson);
        return statusJson;
    }

    /**
     * 启动设备录像
     *
     * @param deviceId 设备ID
     * @return 录像ID
     * @throws Exception 业务异常
     */
    public Long startRecording(Long deviceId) throws Exception {
        log.debug("开始启动设备录像: deviceId={}", deviceId);

        // 1. 校验设备状态
        VideoDeviceEntity device = validateDeviceForRecording(deviceId);

        // 2. 检查是否已有录像进行中
        if (hasActiveRecording(deviceId)) {
            throw new SmartException("设备已有录像进行中");
        }

        // 3. 确保设备连接
        ensureDeviceConnected(device);

        // 4. 创建录像记录
        VideoRecordEntity recordEntity = createVideoRecord(device);

        // 5. 启动录像流
        startVideoStream(device, recordEntity);

        // 6. 更新设备状态
        updateDeviceRecordingStatus(deviceId, true);

        log.info("启动设备录像成功: deviceId={}, recordId={}", deviceId, recordEntity.getRecordId());
        return recordEntity.getRecordId();
    }

    /**
     * 停止设备录像
     *
     * @param recordId 录像ID
     * @return 操作结果
     * @throws Exception 业务异常
     */
    public String stopRecording(Long recordId) throws Exception {
        log.debug("开始停止设备录像: recordId={}", recordId);

        // 1. 获取录像记录
        VideoRecordEntity record = videoRecordDao.selectById(recordId);
        if (record == null) {
            throw new SmartException("录像记录不存在");
        }

        // 2. 检查录像状态
        if (!"RECORDING".equals(record.getRecordStatus())) {
            throw new SmartException("录像未在进行中");
        }

        // 3. 获取设备信息
        VideoDeviceEntity device = videoDeviceDao.selectById(record.getDeviceId());
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 4. 停止录像流
        stopVideoStream(device, record);

        // 5. 更新录像记录
        record.setRecordStatus("COMPLETED");
        record.setRecordEndTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        videoRecordDao.updateById(record);

        // 6. 更新设备状态
        updateDeviceRecordingStatus(record.getDeviceId(), false);

        // 7. 异步处理录像文件
        processRecordingFile(record);

        log.info("停止设备录像成功: recordId={}, deviceId={}", recordId, record.getDeviceId());
        return "录像停止成功";
    }

    /**
     * 处理设备监控事件
     *
     * @param deviceId  设备ID
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @throws Exception 业务异常
     */
    public void handleMonitorEvent(Long deviceId, String eventType, Map<String, Object> eventData)
            throws Exception {
        log.debug("开始处理监控事件: deviceId={}, eventType={}", deviceId, eventType);

        // 1. 验证设备状态
        VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
        if (device == null) {
            log.warn("设备不存在，跳过监控事件处理: deviceId={}", deviceId);
            return;
        }

        // 2. 创建监控事件记录
        MonitorEventEntity eventEntity = new MonitorEventEntity();
        eventEntity.setDeviceId(deviceId);
        eventEntity.setDeviceCode(device.getDeviceCode());
        eventEntity.setEventType(eventType);
        eventEntity.setEventDescription(convertToJson(eventData));
        eventEntity.setEventTime(LocalDateTime.now());
        eventEntity.setEventStatus("PENDING");
        eventEntity.setCreateTime(LocalDateTime.now());
        eventEntity.setUpdateTime(LocalDateTime.now());

        // 3. 根据事件类型进行特殊处理
        switch (eventType) {
            case "MOTION_DETECTED":
                handleMotionDetectedEvent(eventEntity, eventData);
                break;
            case "PERSON_DETECTED":
                handlePersonDetectedEvent(eventEntity, eventData);
                break;
            case "ALARM_TRIGGERED":
                handleAlarmTriggeredEvent(eventEntity, eventData);
                break;
            case "DEVICE_OFFLINE":
                handleDeviceOfflineEvent(eventEntity, eventData);
                break;
            default:
                log.debug("未知事件类型，仅记录事件: eventType={}", eventType);
                break;
        }

        // 4. 保存事件记录
        monitorEventDao.insert(eventEntity);

        log.debug("监控事件处理完成: deviceId={}, eventType={}, eventId={}", deviceId, eventType,
                eventEntity.getEventId());
    }

    /**
     * 健康检查所有设备连接
     */
    public void healthCheckAllDevices() {
        log.debug("开始执行设备健康检查");

        int totalDevices = 0;
        int connectedDevices = 0;
        int offlineDevices = 0;

        for (Map.Entry<Long, DeviceConnection> entry : deviceConnections.entrySet()) {
            Long deviceId = entry.getKey();
            DeviceConnection connection = entry.getValue();
            totalDevices++;

            try {
                if (isDeviceHealthy(deviceId, connection)) {
                    connectedDevices++;
                    updateDeviceConnectionStatus(deviceId, "ONLINE");
                } else {
                    offlineDevices++;
                    updateDeviceConnectionStatus(deviceId, "OFFLINE");
                    // 尝试重新连接
                    attemptReconnect(deviceId);
                }
            } catch (Exception e) {
                log.error("设备健康检查失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
                offlineDevices++;
            }
        }

        log.info("设备健康检查完成: total={}, connected={}, offline={}", totalDevices, connectedDevices,
                offlineDevices);
    }

    // 私有辅助方法

    private boolean isDeviceConnected(Long deviceId) {
        DeviceConnection connection = deviceConnections.get(deviceId);
        return connection != null && connection.isConnected();
    }

    private void connectDevice(VideoDeviceEntity device) throws Exception {
        log.debug("开始连接设备: deviceId={}, ip={}", device.getDeviceId(), device.getDeviceIp());

        // 模拟设备连接过程
        DeviceConnection connection = new DeviceConnection();
        connection.setDeviceId(device.getDeviceId());
        connection.setDeviceIp(device.getDeviceIp());
        connection.setDevicePort(device.getDevicePort());
        connection.setConnectTime(LocalDateTime.now());
        connection.setLastHeartbeat(LocalDateTime.now());
        connection.setConnected(true);

        deviceConnections.put(device.getDeviceId(), connection);
        deviceSequenceNumbers.put(device.getDeviceId(), new AtomicLong(0));

        log.info("设备连接成功: deviceId={}", device.getDeviceId());
    }

    private String buildPTZCommand(String command, Integer speed, VideoDeviceEntity device) {
        // 构建PTZ控制命令协议
        Map<String, Object> cmdData = new HashMap<>();
        cmdData.put("command", command);
        cmdData.put("speed", speed);
        cmdData.put("sequence", deviceSequenceNumbers.get(device.getDeviceId()).incrementAndGet());
        cmdData.put("timestamp", System.currentTimeMillis());

        return convertToJson(cmdData);
    }

    private String sendPTZCommand(VideoDeviceEntity device, String command) throws Exception {
        // 模拟发送 PTZ 指令到设备
        // log.debug("发送PTZ指令到设备 deviceId={}, command={}", device.getDeviceId(),
        // command);
        // 模拟网络延迟
        TimeUnit.MILLISECONDS.sleep(100);

        return "PTZ指令执行成功";
    }

    private void recordDeviceOperation(Long deviceId, String operationType, String operationData) {
        try {
            DeviceStatusLogEntity logEntity = new DeviceStatusLogEntity();
            logEntity.setDeviceId(deviceId);
            logEntity.setLogType("OPERATION");
            logEntity.setLogContent(operationType + ":" + operationData);
            logEntity.setLogTime(LocalDateTime.now());
            logEntity.setCreateTime(LocalDateTime.now());

            deviceStatusLogDao.insert(logEntity);
        } catch (Exception e) {
            log.error("记录设备操作日志失败: deviceId={}, operation={}", deviceId, operationType, e);
        }
    }

    private Map<String, Object> getDeviceStatistics(Long deviceId) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取今日录像统计
        Long todayRecordCount = videoRecordDao.getTodayRecordCount(deviceId);
        Long todayRecordDuration = videoRecordDao.getTodayRecordDuration(deviceId);

        // 获取事件统计
        Long todayEventCount = monitorEventDao.getTodayEventCount(deviceId);

        statistics.put("todayRecordCount", todayRecordCount != null ? todayRecordCount : 0);
        statistics.put("todayRecordDuration",
                todayRecordDuration != null ? todayRecordDuration : 0);
        statistics.put("todayEventCount", todayEventCount != null ? todayEventCount : 0);

        return statistics;
    }

    private void updateDeviceStatusFromRealtime(Long deviceId, Map<String, Object> status) {
        try {
            Boolean isConnected = (Boolean) status.get("isConnected");
            if (isConnected != null) {
                String deviceStatus = isConnected ? "ONLINE" : "OFFLINE";
                updateDeviceConnectionStatus(deviceId, deviceStatus);
            }
        } catch (Exception e) {
            log.error("更新设备实时状态失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    private void updateDeviceConnectionStatus(Long deviceId, String status) {
        try {
            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device != null && !status.equals(device.getDeviceStatus())) {
                device.setDeviceStatus(status);
                device.setUpdateTime(LocalDateTime.now());
                videoDeviceDao.updateById(device);

                // 记录状态变更日志
                recordDeviceOperation(deviceId, "STATUS_CHANGE", status);
            }
        } catch (Exception e) {
            log.error("更新设备连接状态失败: deviceId={}, status={}", deviceId, status, e);
        }
    }

    private VideoDeviceEntity validateDeviceForRecording(Long deviceId) throws Exception {
        VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        if (!"ONLINE".equals(device.getDeviceStatus())) {
            throw new SmartException("设备不在在线状态，无法启动录像");
        }

        if (device.getRecordEnabled() == null || device.getRecordEnabled() != 1) {
            throw new SmartException("设备未启用录像功能");
        }

        return device;
    }

    private boolean hasActiveRecording(Long deviceId) {
        Long activeCount = videoRecordDao.getActiveRecordingCount(deviceId);
        return activeCount != null && activeCount > 0;
    }

    private void ensureDeviceConnected(VideoDeviceEntity device) throws Exception {
        if (!isDeviceConnected(device.getDeviceId())) {
            connectDevice(device);
        }
    }

    private VideoRecordEntity createVideoRecord(VideoDeviceEntity device) {
        VideoRecordEntity record = new VideoRecordEntity();
        record.setDeviceId(device.getDeviceId());
        record.setRecordType("MANUAL");
        record.setRecordStatus("RECORDING");
        record.setRecordStartTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        videoRecordDao.insert(record);
        return record;
    }

    private void startVideoStream(VideoDeviceEntity device, VideoRecordEntity record)
            throws Exception {
        log.debug("启动视频流录像 deviceId={}, recordId={}", device.getDeviceId(),
                record.getRecordId());

        // 模拟启动视频流
        Map<String, Object> streamParams = new HashMap<>();
        streamParams.put("rtspUrl", device.getRtspUrl());
        streamParams.put("recordId", record.getRecordId());
        streamParams.put("resolution", device.getResolution());
        streamParams.put("frameRate", device.getFrameRate());

        // 这里应调用实际的流媒体服务
        log.info("视频流启动成功 deviceId={}, recordId={}", device.getDeviceId(),
                record.getRecordId());
    }

    private void stopVideoStream(VideoDeviceEntity device, VideoRecordEntity record)
            throws Exception {
        log.debug("停止视频流录像 deviceId={}, recordId={}", device.getDeviceId(),
                record.getRecordId());

        // 模拟停止视频流
        Map<String, Object> stopParams = new HashMap<>();
        stopParams.put("recordId", record.getRecordId());

        // 这里应调用实际的流媒体服务
        log.info("视频流停止成功 deviceId={}, recordId={}", device.getDeviceId(),
                record.getRecordId());
    }

    private void updateDeviceRecordingStatus(Long deviceId, boolean isRecording) {
        try {
            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device != null) {
                device.setUpdateTime(LocalDateTime.now());
                videoDeviceDao.updateById(device);

                recordDeviceOperation(deviceId, "RECORDING_STATUS",
                        isRecording ? "STARTED" : "STOPPED");
            }
        } catch (Exception e) {
            log.error("更新设备录像状态失败: deviceId={}, isRecording={}", deviceId, isRecording, e);
        }
    }

    private void processRecordingFile(VideoRecordEntity record) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("开始处理录像文件: recordId={}", record.getRecordId());

                // 模拟录像文件处理
                TimeUnit.SECONDS.sleep(2);

                // 更新文件大小、时长等信息
                record.setRecordFileSize(1024 * 1024L); // 1MB
                record.setRecordDuration(120); // 2分钟
                record.setRecordFilePath("/recordings/" + record.getRecordId() + ".mp4");
                record.setUpdateTime(LocalDateTime.now());
                videoRecordDao.updateById(record);

                log.info("录像文件处理完成: recordId={}", record.getRecordId());
            } catch (Exception e) {
                log.error("录像文件处理失败: recordId={}, error={}", record.getRecordId(),
                        e.getMessage(), e);
            }
        });
    }

    private void handleMotionDetectedEvent(MonitorEventEntity event,
            Map<String, Object> eventData) {
        log.debug("处理移动检测事件: deviceId={}", event.getDeviceId());
        // 特殊处理移动检测事件
        event.setEventLevel("MEDIUM");
        event.setEventDescription("检测到移动目标");
    }

    private void handlePersonDetectedEvent(MonitorEventEntity event,
            Map<String, Object> eventData) {
        log.debug("处理人员检测事件: deviceId={}", event.getDeviceId());
        // 特殊处理人员检测事件
        event.setEventLevel("LOW");
        event.setEventDescription("检测到人员活动");
    }

    private void handleAlarmTriggeredEvent(MonitorEventEntity event,
            Map<String, Object> eventData) {
        log.debug("处理警报触发事件: deviceId={}", event.getDeviceId());
        // 特殊处理警报事件
        event.setEventLevel("HIGH");
        event.setEventDescription("设备警报触发");
        event.setEventStatus("PROCESSING");
    }

    private void handleDeviceOfflineEvent(MonitorEventEntity event, Map<String, Object> eventData) {
        log.debug("处理设备离线事件: deviceId={}", event.getDeviceId());
        // 特殊处理设备离线事件
        event.setEventLevel("HIGH");
        event.setEventDescription("设备离线");
        event.setEventStatus("URGENT");
    }

    private boolean isDeviceHealthy(Long deviceId, DeviceConnection connection) {
        if (connection == null || !connection.isConnected()) {
            return false;
        }

        // 检查最后心跳时间（超过5分钟认为不健康）
        LocalDateTime lastHeartbeat = connection.getLastHeartbeat();
        return lastHeartbeat != null && lastHeartbeat.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    private void attemptReconnect(Long deviceId) {
        try {
            VideoDeviceEntity device = videoDeviceDao.selectById(deviceId);
            if (device != null && "OFFLINE".equals(device.getDeviceStatus())) {
                log.info("尝试重新连接设备: deviceId={}", deviceId);
                connectDevice(device);
            }
        } catch (Exception e) {
            log.error("设备重连失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
        }
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败: {}", e.getMessage(), e);
            return "{}";
        }
    }

    /**
     * 设备连接信息
     */
    private static class DeviceConnection {
        private Long deviceId;
        private String deviceIp;
        private Integer devicePort;
        private LocalDateTime connectTime;
        private LocalDateTime lastHeartbeat;
        private boolean connected;

        // Getters and Setters
        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceIp() {
            return deviceIp;
        }

        public void setDeviceIp(String deviceIp) {
            this.deviceIp = deviceIp;
        }

        public Integer getDevicePort() {
            return devicePort;
        }

        public void setDevicePort(Integer devicePort) {
            this.devicePort = devicePort;
        }

        public LocalDateTime getConnectTime() {
            return connectTime;
        }

        public void setConnectTime(LocalDateTime connectTime) {
            this.connectTime = connectTime;
        }

        public LocalDateTime getLastHeartbeat() {
            return lastHeartbeat;
        }

        public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
            this.lastHeartbeat = lastHeartbeat;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }
    }
}

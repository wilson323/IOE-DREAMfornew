package net.lab1024.sa.admin.module.access.adapter;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.device.DeviceAdapterInterface;
import net.lab1024.sa.base.common.device.DeviceDispatchResult;
import net.lab1024.sa.base.common.device.DeviceConnectionTest;
import net.lab1024.sa.base.common.device.DeviceProtocolException;
import net.lab1024.sa.base.common.device.AdapterInfo;
import net.lab1024.sa.admin.module.access.adapter.protocol.AccessProtocolInterface;
import net.lab1024.sa.admin.module.access.adapter.protocol.impl.*;

/**
 * 门禁设备适配器
 * <p>
 * 门禁设备的统一适配器，负责管理和协调各种门禁协议实现
 * 通过协议注册机制支持多种厂商的门禁设备
 * 提供门禁特有的功能：远程开门、权限管理、门禁记录等
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Component("accessDeviceAdapter")
public class AccessDeviceAdapter implements DeviceAdapterInterface {

    // 门禁协议适配器映射
    private final Map<String, AccessProtocolInterface> protocolAdapters;

    @Resource
    private ZKTecoAdapter zktecoAdapter;

    @Resource
    private HikvisionAdapter hikvisionAdapter;

    @Resource
    private DahuaAdapter dahuaAdapter;

    @Resource
    private HttpProtocolAdapter httpProtocolAdapter;

    public AccessDeviceAdapter() {
        this.protocolAdapters = new HashMap<>();
    }

    /**
     * 初始化协议适配器
     */
    public void initialize() {
        log.info("初始化门禁设备适配器...");

        // 注册协议适配器
        registerProtocolAdapter("ZKTeco", zktecoAdapter);
        registerProtocolAdapter("Hikvision", hikvisionAdapter);
        registerProtocolAdapter("Dahua", dahuaAdapter);
        registerProtocolAdapter("HTTP", httpProtocolAdapter);
        registerProtocolAdapter("Generic", httpProtocolAdapter);

        log.info("门禁设备适配器初始化完成，共注册 {} 个协议适配器", protocolAdapters.size());
    }

    /**
     * 注册协议适配器
     */
    private void registerProtocolAdapter(String manufacturer, AccessProtocolInterface adapter) {
        if (adapter != null) {
            protocolAdapters.put(manufacturer, adapter);
            log.debug("注册门禁协议适配器: {} -> {}", manufacturer, adapter.getClass().getSimpleName());
        }
    }

    @Override
    public String getSupportedDeviceType() {
        return "ACCESS";
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return new ArrayList<>(protocolAdapters.keySet());
    }

    @Override
    public boolean supportsDevice(SmartDeviceEntity device) {
        if (device == null) {
            return false;
        }

        // 检查设备类型
        if (!"ACCESS".equals(device.getDeviceType())) {
            return false;
        }

        // 检查制造商
        String manufacturer = device.getManufacturer();
        if (manufacturer == null || manufacturer.trim().isEmpty()) {
            // 如果没有指定制造商，尝试使用通用协议
            manufacturer = "Generic";
        }

        return protocolAdapters.containsKey(manufacturer);
    }

    @Override
    public DeviceConnectionTest testConnection(SmartDeviceEntity device) throws DeviceProtocolException {
        try {
            log.info("测试门禁设备连接: deviceId={}, deviceName={}, manufacturer={}",
                    device.getDeviceId(), device.getDeviceName(), device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);
            DeviceConnectionTest result = adapter.testConnection(device);

            log.info("门禁设备连接测试完成: deviceId={}, result={}",
                    device.getDeviceId(), result.getSummary());

            return result;

        } catch (Exception e) {
            log.error("门禁设备连接测试异常: deviceId={}", device.getDeviceId(), e);
            throw DeviceProtocolException.connectionFailed(device);
        }
    }

    @Override
    public DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData)
            throws DeviceProtocolException {

        try {
            log.info("下发门禁人员数据: deviceId={}, personId={}, manufacturer={}",
                    device.getDeviceId(), personData.get("personId"), device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);

            // 门禁特有的人员数据转换
            Map<String, Object> accessPersonData = convertToAccessPersonData(personData);

            DeviceDispatchResult result = adapter.dispatchPersonData(device, accessPersonData);

            // 记录操作日志
            logAccessOperation(device, "PERSON_DISPATCH", personData, result);

            return result;

        } catch (Exception e) {
            log.error("门禁人员数据下发异常: deviceId={}, personId={}", device.getDeviceId(), personData.get("personId"), e);
            throw DeviceProtocolException.protocolError("门禁", "人员数据下发失败: " + e.getMessage());
        }
    }

    @Override
    public DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData)
            throws DeviceProtocolException {

        try {
            log.info("下发生物特征数据: deviceId={}, biometricType={}, manufacturer={}",
                    device.getDeviceId(), biometricData.get("biometricType"), device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);

            // 门禁特有的生物特征数据转换
            Map<String, Object> accessBiometricData = convertToAccessBiometricData(biometricData);

            DeviceDispatchResult result = adapter.dispatchBiometricData(device, accessBiometricData);

            // 记录操作日志
            logAccessOperation(device, "BIOMETRIC_DISPATCH", biometricData, result);

            return result;

        } catch (Exception e) {
            log.error("门禁生物特征数据下发异常: deviceId={}, biometricType={}",
                    device.getDeviceId(), biometricData.get("biometricType"), e);
            throw DeviceProtocolException.protocolError("门禁", "生物特征数据下发失败: " + e.getMessage());
        }
    }

    @Override
    public DeviceDispatchResult dispatchConfigData(SmartDeviceEntity device, Map<String, Object> configData)
            throws DeviceProtocolException {

        try {
            log.info("下发门禁配置数据: deviceId={}, manufacturer={}",
                    device.getDeviceId(), device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);

            DeviceDispatchResult result = adapter.dispatchAccessConfig(device, configData);

            // 记录操作日志
            logAccessOperation(device, "CONFIG_DISPATCH", configData, result);

            return result;

        } catch (Exception e) {
            log.error("门禁配置数据下发异常: deviceId={}", device.getDeviceId(), e);
            throw DeviceProtocolException.protocolError("门禁", "配置数据下发失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws DeviceProtocolException {
        try {
            log.debug("获取门禁设备状态: deviceId={}", device.getDeviceId());

            AccessProtocolInterface adapter = getProtocolAdapter(device);
            Map<String, Object> status = adapter.getDeviceStatus(device);

            // 添加基础状态信息
            status.put("deviceId", device.getDeviceId());
            status.put("deviceName", device.getDeviceName());
            status.put("deviceCode", device.getDeviceCode());
            status.put("deviceType", device.getDeviceType());
            status.put("manufacturer", device.getManufacturer());
            status.put("ipAddress", device.getIpAddress());
            status.put("port", device.getPort());

            return status;

        } catch (Exception e) {
            log.error("获取门禁设备状态异常: deviceId={}", device.getDeviceId(), e);
            throw DeviceProtocolException.protocolError("门禁", "状态查询失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> queryPersonsOnDevice(SmartDeviceEntity device) throws DeviceProtocolException {
        try {
            log.debug("查询门禁设备人员列表: deviceId={}", device.getDeviceId());

            AccessProtocolInterface adapter = getProtocolAdapter(device);
            List<Map<String, Object>> persons = adapter.queryPersonsOnDevice(device);

            log.debug("门禁设备人员列表查询完成: deviceId={}, count={}", device.getDeviceId(), persons.size());

            return persons;

        } catch (Exception e) {
            log.error("查询门禁设备人员列表异常: deviceId={}", device.getDeviceId(), e);
            throw DeviceProtocolException.protocolError("门禁", "人员列表查询失败: " + e.getMessage());
        }
    }

    @Override
    public DeviceDispatchResult deletePersonData(SmartDeviceEntity device, Long personId) throws DeviceProtocolException {
        try {
            log.info("删除门禁人员数据: deviceId={}, personId={}, manufacturer={}",
                    device.getDeviceId(), personId, device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);

            DeviceDispatchResult result = adapter.deletePersonData(device, personId);

            // 记录操作日志
            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("personId", personId);
            logAccessOperation(device, "PERSON_DELETE", deleteData, result);

            return result;

        } catch (Exception e) {
            log.error("删除门禁人员数据异常: deviceId={}, personId={}", device.getDeviceId(), personId, e);
            throw DeviceProtocolException.protocolError("门禁", "人员数据删除失败: " + e.getMessage());
        }
    }

    @Override
    public DeviceDispatchResult batchDispatchPersonData(SmartDeviceEntity device, List<Map<String, Object>> personList)
            throws DeviceProtocolException {

        try {
            if (personList == null || personList.isEmpty()) {
                return DeviceDispatchResult.failure("人员列表为空", "INVALID_PARAMETER");
            }

            log.info("批量下发门禁人员数据: deviceId={}, count={}, manufacturer={}",
                    device.getDeviceId(), personList.size(), device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);

            // 批量转换人员数据
            List<Map<String, Object>> accessPersonList = new ArrayList<>();
            for (Map<String, Object> personData : personList) {
                accessPersonList.add(convertToAccessPersonData(personData));
            }

            DeviceDispatchResult result = adapter.batchDispatchPersonData(device, accessPersonList);

            // 记录操作日志
            Map<String, Object> batchData = new HashMap<>();
            batchData.put("count", personList.size());
            batchData.put("personIds", personList.stream().map(p -> p.get("personId")).toList());
            logAccessOperation(device, "PERSON_BATCH_DISPATCH", batchData, result);

            return result;

        } catch (Exception e) {
            log.error("批量下发门禁人员数据异常: deviceId={}, count={}", device.getDeviceId(), personList.size(), e);
            throw DeviceProtocolException.protocolError("门禁", "批量人员数据下发失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceConfigTemplate() {
        Map<String, Object> template = new HashMap<>();

        // 基础连接配置
        Map<String, Object> connectionConfig = new HashMap<>();
        connectionConfig.put("ipAddress", "");
        connectionConfig.put("port", 0);
        connectionConfig.put("protocolType", "HTTP");
        connectionConfig.put("timeout", 30000);
        connectionConfig.put("retries", 3);
        template.put("connection", connectionConfig);

        // 门禁设备配置
        Map<String, Object> accessConfig = new HashMap<>();
        accessConfig.put("openMethod", "BIOMETRIC"); // BIOMETRIC, CARD, PASSWORD, QR
        accessConfig.put("recognitionThreshold", 5);
        accessConfig.put("liveDetectionEnabled", true);
        accessConfig.put("tamperAlarmEnabled", true);
        accessConfig.put("openDelay", 3);
        accessConfig.put("unlockDuration", 5);
        template.put("access", accessConfig);

        // 认证配置
        Map<String, Object> authConfig = new HashMap<>();
        authConfig.put("username", "");
        authConfig.put("password", "");
        authConfig.put("token", "");
        authConfig.put("apiKey", "");
        template.put("authentication", authConfig);

        // 高级配置
        Map<String, Object> advancedConfig = new HashMap<>();
        advancedConfig.put("enableBatchOperation", true);
        advancedConfig.put("maxBatchSize", 100);
        advancedConfig.put("batchTimeout", 60000);
        advancedConfig.put("enableRealTimeMonitoring", false);
        advancedConfig.put("monitoringInterval", 30000);
        template.put("advanced", advancedConfig);

        return template;
    }

    @Override
    public boolean validateDeviceConfig(SmartDeviceEntity device) {
        try {
            // 基础验证
            if (device == null) {
                return false;
            }

            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("设备类型不支持: {}，预期: ACCESS", device.getDeviceType());
                return false;
            }

            // 网络配置验证
            if (device.getIpAddress() == null || device.getIpAddress().trim().isEmpty()) {
                log.warn("设备IP地址不能为空: deviceId={}", device.getDeviceId());
                return false;
            }

            // 协议适配器验证
            try {
                AccessProtocolInterface adapter = getProtocolAdapter(device);
                return adapter != null;
            } catch (Exception e) {
                log.warn("无法获取设备协议适配器: deviceId={}, manufacturer={}", device.getDeviceId(), device.getManufacturer());
                return false;
            }

        } catch (Exception e) {
            log.error("设备配置验证异常", e);
            return false;
        }
    }

    @Override
    public AdapterInfo getAdapterInfo() {
        return AdapterInfo.builder()
            .name("AccessDeviceAdapter")
            .version("1.0.0")
            .description("门禁设备统一适配器，支持多种门禁协议")
            .supportedDeviceType("ACCESS")
            .supportedManufacturers(new ArrayList<>(protocolAdapters.keySet()))
            .supportedProtocols(List.of("TCP", "UDP", "HTTP", "HTTPS", "厂商SDK"))
            .supportedFeatures(List.of(
                "连接测试", "人员信息下发", "生物特征下发", "权限管理",
                "远程开门", "门禁记录查询", "批量操作", "实时监控",
                "断线重连", "数据加密"
            ))
            .build();
    }

    // ==================== 门禁特有功能 ====================

    /**
     * 远程开门
     *
     * @param device 设备实体
     * @param doorId 门ID
     * @return 开门结果
     */
    public DeviceDispatchResult remoteOpenDoor(SmartDeviceEntity device, String doorId) {
        try {
            log.info("远程开门: deviceId={}, doorId={}, manufacturer={}",
                    device.getDeviceId(), doorId, device.getManufacturer());

            AccessProtocolInterface adapter = getProtocolAdapter(device);

            Map<String, Object> doorData = new HashMap<>();
            doorData.put("doorId", doorId);
            doorData.put("operation", "OPEN");
            doorData.put("timestamp", System.currentTimeMillis());

            DeviceDispatchResult result = adapter.remoteOpenDoor(device, doorId);

            // 记录操作日志
            Map<String, Object> operationData = new HashMap<>();
            operationData.put("doorId", doorId);
            operationData.put("operationType", "REMOTE_OPEN");
            logAccessOperation(device, "DOOR_OPEN", operationData, result);

            return result;

        } catch (Exception e) {
            log.error("远程开门异常: deviceId={}, doorId={}", device.getDeviceId(), doorId, e);
            return DeviceDispatchResult.failure("远程开门失败: " + e.getMessage());
        }
    }

    /**
     * 获取门禁记录
     *
     * @param device 设备实体
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param recordCount 记录数量限制（可选）
     * @return 门禁记录列表
     */
    public List<Map<String, Object>> getAccessRecords(SmartDeviceEntity device,
                                                        String startTime,
                                                        String endTime,
                                                        Integer recordCount) {
        try {
            log.debug("获取门禁记录: deviceId={}, startTime={}, endTime={}",
                    device.getDeviceId(), startTime, endTime);

            AccessProtocolInterface adapter = getProtocolAdapter(device);
            return adapter.getAccessRecords(device, startTime, endTime, recordCount);

        } catch (Exception e) {
            log.error("获取门禁记录异常: deviceId={}", device.getDeviceId(), e);
            return new ArrayList<>();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取门禁协议适配器
     */
    private AccessProtocolInterface getProtocolAdapter(SmartDeviceEntity device) throws DeviceProtocolException {
        String manufacturer = device.getManufacturer();
        if (manufacturer == null || manufacturer.trim().isEmpty()) {
            manufacturer = "Generic"; // 默认使用通用协议
        }

        AccessProtocolInterface adapter = protocolAdapters.get(manufacturer);
        if (adapter == null) {
            throw DeviceProtocolException.deviceNotSupported(device.getDeviceType(), manufacturer);
        }

        return adapter;
    }

    /**
     * 转换为门禁特有的人员数据
     */
    private Map<String, Object> convertToAccessPersonData(Map<String, Object> personData) {
        Map<String, Object> accessPersonData = new HashMap<>();

        // 基础人员信息
        accessPersonData.put("personId", personData.get("personId"));
        accessPersonData.put("personCode", personData.get("personCode"));
        accessPersonData.put("personName", personData.get("personName"));
        accessPersonData.put("personType", personData.get("personType"));
        accessPersonData.put("department", personData.get("department"));

        // 门禁特有字段
        accessPersonData.put("cardNo", personData.get("cardNo"));
        accessPersonData.put("accessLevel", personData.get("accessLevel"));
        accessPersonData.put("validFrom", personData.get("validFrom"));
        accessPersonData.put("validTo", personData.get("validTo"));
        accessPersonData.put("timeSlots", personData.get("timeSlots"));
        accessPersonData.put("weekdays", personData.get("weekdays"));
        accessPersonData.put("areas", personData.get("areas"));

        // 设备信息
        accessPersonData.put("deviceId", personData.get("deviceId"));
        accessPersonData.put("timestamp", System.currentTimeMillis());

        return accessPersonData;
    }

    /**
     * 转换为门禁特有的生物特征数据
     */
    private Map<String, Object> convertToAccessBiometricData(Map<String, Object> biometricData) {
        Map<String, Object> accessBiometricData = new HashMap<>();

        // 基础生物特征信息
        accessBiometricData.put("personId", biometricData.get("personId"));
        accessBiometricData.put("biometricType", biometricData.get("biometricType"));
        accessBiometricData.put("templateIndex", biometricData.get("templateIndex"));
        accessBiometricData.put("biometricData", biometricData.get("biometricData"));
        accessBiometricData.put("quality", biometricData.get("quality"));
        accessBiometricData.put("templateVersion", biometricData.get("templateVersion"));
        accessBiometricData.put("captureTime", biometricData.get("captureTime"));

        // 门禁特有字段
        accessBiometricData.put("fingerId", biometricData.get("fingerId"));
        accessBiometricData.put("faceId", biometricData.get("faceId"));
        accessBiometricData.put("irisId", biometricData.get("irisId"));

        // 设备信息
        accessBiometricData.put("deviceId", biometricData.get("deviceId"));
        accessBiometricData.put("timestamp", System.currentTimeMillis());

        return accessBiometricData;
    }

    /**
     * 记录门禁操作日志
     */
    private void logAccessOperation(SmartDeviceEntity device, String operationType, Map<String, Object> operationData, DeviceDispatchResult result) {
        log.info("门禁操作记录: deviceId={}, deviceName={}, manufacturer={}, operationType={}, result={}, data={}",
                device.getDeviceId(),
                device.getDeviceName(),
                device.getManufacturer(),
                operationType,
                result.getSummary(),
                operationData);
    }
}
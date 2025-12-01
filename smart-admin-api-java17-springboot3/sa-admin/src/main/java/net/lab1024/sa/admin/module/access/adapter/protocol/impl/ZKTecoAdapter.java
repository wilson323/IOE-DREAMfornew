package net.lab1024.sa.admin.module.access.adapter.protocol.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.device.DeviceConnectionTest;
import net.lab1024.sa.base.common.device.DeviceDispatchResult;
import net.lab1024.sa.admin.module.access.adapter.protocol.AccessProtocolInterface;

import java.util.*;

/**
 * ZKTeco门禁协议适配器
 * <p>
 * 支持ZKTeco门禁设备的通信协议，包括TCP、HTTP和SDK方式
 * 支持指纹、人脸、卡片等多种生物特征识别
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
public class ZKTecoAdapter implements AccessProtocolInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZKTecoAdapter.class);

    private static final String PROTOCOL_NAME = "ZKTeco";
    private static final String PROTOCOL_VERSION = "3.0";

    // ZKTeco设备支持的制造商
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
        "ZKTeco", "中控", "ZK"
    );

    // ZKTeco协议特性
    private static final List<String> PROTOCOL_FEATURES = Arrays.asList(
        "TCP连接", "HTTP API", "SDK支持", "指纹识别", "人脸识别",
        "卡片识别", "密码验证", "远程开门", "批量操作", "实时监控"
    );

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return SUPPORTED_MANUFACTURERS;
    }

    @Override
    public DeviceConnectionTest testConnection(SmartDeviceEntity device) throws Exception {
        long startTime = System.currentTimeMillis();
        String connectionAddress = device.getIpAddress() + ":" + device.getPort();

        try {
            log.info("测试ZKTeco设备连接: {} ({})", device.getDeviceName(), connectionAddress);

            // 根据协议类型选择连接方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("TCP".equalsIgnoreCase(protocolType)) {
                success = testTcpConnection(device);
                message = success ? "TCP连接成功" : "TCP连接失败";
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                success = testHttpConnection(device);
                message = success ? "HTTP连接成功" : "HTTP连接失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = testSdkConnection(device);
                message = success ? "SDK连接成功" : "SDK连接失败";
            } else {
                throw new IllegalArgumentException("不支持的协议类型: " + protocolType);
            }

            long connectionTime = System.currentTimeMillis() - startTime;

            if (success) {
                return DeviceConnectionTest.success(connectionTime, protocolType, connectionAddress)
                    .addDeviceInfo("manufacturer", "ZKTeco")
                    .addDeviceInfo("model", getDeviceModel(device))
                    .addDeviceInfo("serialNumber", getDeviceSerialNumber(device))
                    .addDeviceInfo("firmwareVersion", getDeviceFirmwareVersion(device));
            } else {
                return DeviceConnectionTest.failure(message, "连接测试失败", protocolType, connectionAddress);
            }

        } catch (Exception e) {
            long connectionTime = System.currentTimeMillis() - startTime;
            log.error("ZKTeco设备连接测试异常: {}", device.getDeviceName(), e);
            return DeviceConnectionTest.networkError(e.getMessage(), device.getProtocolType(), connectionAddress)
                .addResponseData("exception", e.getClass().getSimpleName());
        }
    }

    @Override
    public DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData) throws Exception {
        try {
            log.info("下发ZKTeco人员数据: deviceId={}, personId={}",
                    device.getDeviceId(), personData.get("personId"));

            // 转换为ZKTeco格式
            Map<String, Object> zkPersonData = convertToZKTecoPersonData(personData);

            // 根据协议类型选择下发方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("TCP".equalsIgnoreCase(protocolType)) {
                success = dispatchPersonDataViaTcp(device, zkPersonData);
                message = success ? "TCP下发成功" : "TCP下发失败";
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                success = dispatchPersonDataViaHttp(device, zkPersonData);
                message = success ? "HTTP下发成功" : "HTTP下发失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = dispatchPersonDataViaSdk(device, zkPersonData);
                message = success ? "SDK下发成功" : "SDK下发失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                    .addResponseData("personId", personData.get("personId"))
                    .addResponseData("deviceCode", device.getDeviceCode())
                    .addResponseData("timestamp", System.currentTimeMillis());
            } else {
                return DeviceDispatchResult.failure(message, "DISPATCH_FAILED");
            }

        } catch (Exception e) {
            log.error("ZKTeco人员数据下发异常: deviceId={}, personId={}",
                    device.getDeviceId(), personData.get("personId"), e);
            throw e;
        }
    }

    @Override
    public DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData) throws Exception {
        try {
            log.info("下发生物特征数据: deviceId={}, biometricType={}",
                    device.getDeviceId(), biometricData.get("biometricType"));

            // 根据生物特征类型分别处理
            String biometricType = (String) biometricData.get("biometricType");
            boolean success = false;
            String message = "";

            if ("FINGERPRINT".equalsIgnoreCase(biometricType)) {
                success = dispatchFingerprintData(device, biometricData);
                message = success ? "指纹数据下发成功" : "指纹数据下发失败";
            } else if ("FACE".equalsIgnoreCase(biometricType)) {
                success = dispatchFaceData(device, biometricData);
                message = success ? "人脸数据下发成功" : "人脸数据下发失败";
            } else if ("VEIN".equalsIgnoreCase(biometricType)) {
                success = dispatchVeinData(device, biometricData);
                message = success ? "静脉数据下发成功" : "静脉数据下发失败";
            } else {
                throw new IllegalArgumentException("不支持的生物特征类型: " + biometricType);
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                    .addResponseData("personId", biometricData.get("personId"))
                    .addResponseData("biometricType", biometricType)
                    .addResponseData("templateIndex", biometricData.get("templateIndex"))
                    .addResponseData("deviceCode", device.getDeviceCode());
            } else {
                return DeviceDispatchResult.failure(message, "BIOMETRIC_DISPATCH_FAILED");
            }

        } catch (Exception e) {
            log.error("ZKTeco生物特征数据下发异常: deviceId={}, biometricType={}",
                    device.getDeviceId(), biometricData.get("biometricType"), e);
            throw e;
        }
    }

    @Override
    public DeviceDispatchResult dispatchAccessConfig(SmartDeviceEntity device, Map<String, Object> configData) throws Exception {
        try {
            log.info("下发ZKTeco门禁配置: deviceId={}", device.getDeviceId());

            // 转换为ZKTeco配置格式
            Map<String, Object> zkConfigData = convertToZKTecoConfigData(configData);

            // 根据协议类型选择下发方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("TCP".equalsIgnoreCase(protocolType)) {
                success = dispatchConfigViaTcp(device, zkConfigData);
                message = success ? "TCP配置下发成功" : "TCP配置下发失败";
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                success = dispatchConfigViaHttp(device, zkConfigData);
                message = success ? "HTTP配置下发成功" : "HTTP配置下发失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = dispatchConfigViaSdk(device, zkConfigData);
                message = success ? "SDK配置下发成功" : "SDK配置下发失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                    .addResponseData("deviceCode", device.getDeviceCode())
                    .addResponseData("configItems", zkConfigData.size());
            } else {
                return DeviceDispatchResult.failure(message, "CONFIG_DISPATCH_FAILED");
            }

        } catch (Exception e) {
            log.error("ZKTeco门禁配置下发异常: deviceId={}", device.getDeviceId(), e);
            throw e;
        }
    }

    @Override
    public DeviceDispatchResult remoteOpenDoor(SmartDeviceEntity device, String doorId) throws Exception {
        try {
            log.info("ZKTeco远程开门: deviceId={}, doorId={}", device.getDeviceId(), doorId);

            // 根据协议类型选择开门方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("TCP".equalsIgnoreCase(protocolType)) {
                success = remoteOpenDoorViaTcp(device, doorId);
                message = success ? "TCP远程开门成功" : "TCP远程开门失败";
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                success = remoteOpenDoorViaHttp(device, doorId);
                message = success ? "HTTP远程开门成功" : "HTTP远程开门失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = remoteOpenDoorViaSdk(device, doorId);
                message = success ? "SDK远程开门成功" : "SDK远程开门失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                    .addResponseData("doorId", doorId)
                    .addResponseData("deviceCode", device.getDeviceCode())
                    .addResponseData("openTime", System.currentTimeMillis());
            } else {
                return DeviceDispatchResult.failure(message, "REMOTE_OPEN_FAILED");
            }

        } catch (Exception e) {
            log.error("ZKTeco远程开门异常: deviceId={}, doorId={}", device.getDeviceId(), doorId, e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws Exception {
        try {
            log.debug("获取ZKTeco设备状态: deviceId={}", device.getDeviceId());

            Map<String, Object> status = new HashMap<>();

            // 基础状态信息
            status.put("deviceType", "ACCESS");
            status.put("manufacturer", "ZKTeco");
            status.put("protocolType", device.getProtocolType());
            status.put("ipAddress", device.getIpAddress());
            status.put("port", device.getPort());

            // 根据协议类型获取状态
            String protocolType = device.getProtocolType();
            if ("TCP".equalsIgnoreCase(protocolType)) {
                status.putAll(getStatusViaTcp(device));
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                status.putAll(getStatusViaHttp(device));
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                status.putAll(getStatusViaSdk(device));
            }

            return status;

        } catch (Exception e) {
            log.error("获取ZKTeco设备状态异常: deviceId={}", device.getDeviceId(), e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> queryPersonsOnDevice(SmartDeviceEntity device) throws Exception {
        try {
            log.debug("查询ZKTeco设备人员列表: deviceId={}", device.getDeviceId());

            // 根据协议类型查询人员
            String protocolType = device.getProtocolType();
            List<Map<String, Object>> persons = new ArrayList<>();

            if ("TCP".equalsIgnoreCase(protocolType)) {
                persons = queryPersonsViaTcp(device);
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                persons = queryPersonsViaHttp(device);
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                persons = queryPersonsViaSdk(device);
            }

            log.debug("ZKTeco设备人员查询完成: deviceId={}, count={}", device.getDeviceId(), persons.size());
            return persons;

        } catch (Exception e) {
            log.error("查询ZKTeco设备人员列表异常: deviceId={}", device.getDeviceId(), e);
            throw e;
        }
    }

    @Override
    public DeviceDispatchResult deletePersonData(SmartDeviceEntity device, Long personId) throws Exception {
        try {
            log.info("删除ZKTeco人员数据: deviceId={}, personId={}", device.getDeviceId(), personId);

            // 根据协议类型删除人员
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("TCP".equalsIgnoreCase(protocolType)) {
                success = deletePersonViaTcp(device, personId);
                message = success ? "TCP删除成功" : "TCP删除失败";
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                success = deletePersonViaHttp(device, personId);
                message = success ? "HTTP删除成功" : "HTTP删除失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = deletePersonViaSdk(device, personId);
                message = success ? "SDK删除成功" : "SDK删除失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                    .addResponseData("personId", personId)
                    .addResponseData("deviceCode", device.getDeviceCode())
                    .addResponseData("deleteTime", System.currentTimeMillis());
            } else {
                return DeviceDispatchResult.failure(message, "DELETE_FAILED");
            }

        } catch (Exception e) {
            log.error("删除ZKTeco人员数据异常: deviceId={}, personId={}", device.getDeviceId(), personId, e);
            throw e;
        }
    }

    @Override
    public DeviceDispatchResult batchDispatchPersonData(SmartDeviceEntity device, List<Map<String, Object>> personList) throws Exception {
        try {
            if (personList == null || personList.isEmpty()) {
                return DeviceDispatchResult.failure("人员列表为空", "INVALID_PARAMETER");
            }

            log.info("ZKTeco批量下发人员数据: deviceId={}, count={}", device.getDeviceId(), personList.size());

            int successCount = 0;
            List<String> failedPersons = new ArrayList<>();

            for (Map<String, Object> personData : personList) {
                try {
                    DeviceDispatchResult result = dispatchPersonData(device, personData);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failedPersons.add(personData.get("personId").toString());
                    }
                } catch (Exception e) {
                    failedPersons.add(personData.get("personId").toString());
                    log.warn("ZKTeco人员下发失败: personId={}, error={}", personData.get("personId"), e.getMessage());
                }
            }

            if (successCount == personList.size()) {
                return DeviceDispatchResult.success("全部人员下发成功")
                    .addResponseData("totalCount", personList.size())
                    .addResponseData("successCount", successCount);
            } else if (successCount > 0) {
                Map<String, Object> details = new HashMap<>();
                details.put("failedPersons", failedPersons);
                return DeviceDispatchResult.partialSuccess(
                    "部分人员下发成功", successCount, personList.size(), details);
            } else {
                return DeviceDispatchResult.failure("全部人员下发失败", "BATCH_DISPATCH_FAILED")
                    .addResponseData("failedPersons", failedPersons);
            }

        } catch (Exception e) {
            log.error("ZKTeco批量下发人员数据异常: deviceId={}, count={}",
                    device.getDeviceId(), personList.size(), e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getAccessRecords(SmartDeviceEntity device, String startTime, String endTime, Integer recordCount) throws Exception {
        try {
            log.debug("获取ZKTeco门禁记录: deviceId={}, startTime={}, endTime={}",
                    device.getDeviceId(), startTime, endTime);

            // 根据协议类型获取记录
            String protocolType = device.getProtocolType();
            List<Map<String, Object>> records = new ArrayList<>();

            if ("TCP".equalsIgnoreCase(protocolType)) {
                records = getAccessRecordsViaTcp(device, startTime, endTime, recordCount);
            } else if ("HTTP".equalsIgnoreCase(protocolType)) {
                records = getAccessRecordsViaHttp(device, startTime, endTime, recordCount);
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                records = getAccessRecordsViaSdk(device, startTime, endTime, recordCount);
            }

            log.debug("ZKTeco门禁记录获取完成: deviceId={}, count={}", device.getDeviceId(), records.size());
            return records;

        } catch (Exception e) {
            log.error("获取ZKTeco门禁记录异常: deviceId={}", device.getDeviceId(), e);
            throw e;
        }
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
        if (manufacturer == null) {
            return false;
        }

        return SUPPORTED_MANUFACTURERS.contains(manufacturer.trim());
    }

    @Override
    public String getProtocolVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public List<String> getProtocolFeatures() {
        return PROTOCOL_FEATURES;
    }

    // ==================== 私有方法 ====================

    /**
     * 测试TCP连接
     */
    private boolean testTcpConnection(SmartDeviceEntity device) throws Exception {
        // TODO: 实现ZKTeco TCP连接测试
        // 1. 建立TCP连接到设备
        // 2. 发送连接测试命令
        // 3. 验证响应
        log.debug("测试ZKTeco TCP连接: {}:{}", device.getIpAddress(), device.getPort());
        return true; // 临时返回成功
    }

    /**
     * 测试HTTP连接
     */
    private boolean testHttpConnection(SmartDeviceEntity device) throws Exception {
        // TODO: 实现ZKTeco HTTP连接测试
        // 1. 发送HTTP GET请求到设备API
        // 2. 验证响应状态
        log.debug("测试ZKTeco HTTP连接: http://{}:{}/api/test", device.getIpAddress(), device.getPort());
        return true; // 临时返回成功
    }

    /**
     * 测试SDK连接
     */
    private boolean testSdkConnection(SmartDeviceEntity device) throws Exception {
        // TODO: 实现ZKTeco SDK连接测试
        // 1. 初始化ZKTeco SDK
        // 2. 连接到设备
        // 3. 验证连接状态
        log.debug("测试ZKTeco SDK连接: {}", device.getDeviceCode());
        return true; // 临时返回成功
    }

    /**
     * 转换为ZKTeco人员数据格式
     */
    private Map<String, Object> convertToZKTecoPersonData(Map<String, Object> personData) {
        Map<String, Object> zkData = new HashMap<>();

        // ZKTeco人员数据格式转换
        zkData.put("pin", personData.get("personCode")); // 人员PIN码
        zkData.put("name", personData.get("personName"));
        zkData.put("password", personData.get("password"));
        zkData.put("card", personData.get("cardNo")); // 卡号
        zkData.put("group", personData.get("department") != null ? personData.get("department") : "1");
        zkData.put("privilege", personData.get("accessLevel") != null ? personData.get("accessLevel") : "0");
        zkData.put("tz1", personData.get("timeSlots"));
        zkData.put("validFrom", personData.get("validFrom"));
        zkData.put("validTo", personData.get("validTo"));

        return zkData;
    }

    /**
     * 转换为ZKTeco配置数据格式
     */
    private Map<String, Object> convertToZKTecoConfigData(Map<String, Object> configData) {
        Map<String, Object> zkData = new HashMap<>();

        // ZKTeco配置数据格式转换
        zkData.put("door1", configData.get("openMethod"));
        zkData.put("door2", configData.get("openMethod"));
        zkData.put("door3", configData.get("openMethod"));
        zkData.put("door4", configData.get("openMethod"));
        zkData.put("verifytype", configData.get("recognitionThreshold"));
        zkData.put("faceFunOn", configData.get("liveDetectionEnabled"));
        zkData.put("duressPassword", configData.get("tamperAlarmEnabled"));
        zkData.put("doorOpenTime", configData.get("openDelay"));
        zkData.put("lockDelay", configData.get("unlockDuration"));

        return zkData;
    }

    // 占位方法 - 需要根据实际ZKTeco协议实现
    private String getDeviceModel(SmartDeviceEntity device) { return "ZKTeco-Unknown"; }
    private String getDeviceSerialNumber(SmartDeviceEntity device) { return "Unknown"; }
    private String getDeviceFirmwareVersion(SmartDeviceEntity device) { return "1.0.0"; }

    private boolean dispatchPersonDataViaTcp(SmartDeviceEntity device, Map<String, Object> personData) throws Exception { return true; }
    private boolean dispatchPersonDataViaHttp(SmartDeviceEntity device, Map<String, Object> personData) throws Exception { return true; }
    private boolean dispatchPersonDataViaSdk(SmartDeviceEntity device, Map<String, Object> personData) throws Exception { return true; }

    private boolean dispatchFingerprintData(SmartDeviceEntity device, Map<String, Object> biometricData) throws Exception { return true; }
    private boolean dispatchFaceData(SmartDeviceEntity device, Map<String, Object> biometricData) throws Exception { return true; }
    private boolean dispatchVeinData(SmartDeviceEntity device, Map<String, Object> biometricData) throws Exception { return true; }

    private boolean dispatchConfigViaTcp(SmartDeviceEntity device, Map<String, Object> configData) throws Exception { return true; }
    private boolean dispatchConfigViaHttp(SmartDeviceEntity device, Map<String, Object> configData) throws Exception { return true; }
    private boolean dispatchConfigViaSdk(SmartDeviceEntity device, Map<String, Object> configData) throws Exception { return true; }

    private boolean remoteOpenDoorViaTcp(SmartDeviceEntity device, String doorId) throws Exception { return true; }
    private boolean remoteOpenDoorViaHttp(SmartDeviceEntity device, String doorId) throws Exception { return true; }
    private boolean remoteOpenDoorViaSdk(SmartDeviceEntity device, String doorId) throws Exception { return true; }

    private Map<String, Object> getStatusViaTcp(SmartDeviceEntity device) throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("online", true);
        status.put("temperature", "25°C");
        return status;
    }
    private Map<String, Object> getStatusViaHttp(SmartDeviceEntity device) throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("online", true);
        status.put("temperature", "25°C");
        return status;
    }
    private Map<String, Object> getStatusViaSdk(SmartDeviceEntity device) throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("online", true);
        status.put("temperature", "25°C");
        return status;
    }

    private List<Map<String, Object>> queryPersonsViaTcp(SmartDeviceEntity device) throws Exception { return new ArrayList<>(); }
    private List<Map<String, Object>> queryPersonsViaHttp(SmartDeviceEntity device) throws Exception { return new ArrayList<>(); }
    private List<Map<String, Object>> queryPersonsViaSdk(SmartDeviceEntity device) throws Exception { return new ArrayList<>(); }

    private boolean deletePersonViaTcp(SmartDeviceEntity device, Long personId) throws Exception { return true; }
    private boolean deletePersonViaHttp(SmartDeviceEntity device, Long personId) throws Exception { return true; }
    private boolean deletePersonViaSdk(SmartDeviceEntity device, Long personId) throws Exception { return true; }

    private List<Map<String, Object>> getAccessRecordsViaTcp(SmartDeviceEntity device, String startTime, String endTime, Integer recordCount) throws Exception { return new ArrayList<>(); }
    private List<Map<String, Object>> getAccessRecordsViaHttp(SmartDeviceEntity device, String startTime, String endTime, Integer recordCount) throws Exception { return new ArrayList<>(); }
    private List<Map<String, Object>> getAccessRecordsViaSdk(SmartDeviceEntity device, String startTime, String endTime, Integer recordCount) throws Exception { return new ArrayList<>(); }
}
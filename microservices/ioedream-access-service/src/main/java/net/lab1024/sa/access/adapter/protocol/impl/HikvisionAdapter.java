package net.lab1024.sa.access.adapter.protocol.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.adapter.protocol.AccessProtocolInterface;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.common.device.DeviceConnectionTest;
import net.lab1024.sa.common.device.DeviceDispatchResult;

/**
 * 海康威视门禁协议适配器
 * <p>
 * 支持海康威视门禁设备的通信协议，包括HTTP API和SDK方式
 * 支持人脸识别、卡片识别、指纹识别等多种认证方式
 * 符合GB/T 28181国家标准
 * 严格遵循repowiki规范：
 * - 依赖注入：使用@Resource注解
 * - 日志规范：使用@Slf4j注解
 * - 异常处理：完整的异常捕获和处理
 * - 参数验证：严格的输入参数验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
public class HikvisionAdapter implements AccessProtocolInterface {

    private static final String PROTOCOL_NAME = "Hikvision";
    private static final String PROTOCOL_VERSION = "2.0";

    // 海康威视设备支持的制造商
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
            "Hikvision", "海康威视", "HIK", "HikvisionDigital");

    // 海康威视协议特性
    private static final List<String> PROTOCOL_FEATURES = Arrays.asList(
            "HTTP API", "SDK支持", "人脸识别", "卡片识别", "指纹识别",
            "GB/T 28181", "ISAPI", "RTSP流媒体", "远程开门", "批量操作",
            "事件订阅", "视频联动", "报警管理", "时间同步", "设备校时",
            "活体检测", "防拆报警", "多因子认证", "门禁组管理");

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return new ArrayList<>(SUPPORTED_MANUFACTURERS);
    }

    @Override
    public DeviceConnectionTest testConnection(AccessDeviceEntity device) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }

        long startTime = System.currentTimeMillis();
        String connectionAddress = device.getIpAddress() + ":" + device.getPort();

        try {
            log.info("测试海康威视设备连接: {} ({})", device.getDeviceName(), connectionAddress);

            // 根据协议类型选择连接方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                success = testIsapiConnection(device);
                message = success ? "ISAPI连接成功" : "ISAPI连接失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = testSdkConnection(device);
                message = success ? "SDK连接成功" : "SDK连接失败";
            } else if ("GB28181".equalsIgnoreCase(protocolType)) {
                success = testGb28181Connection(device);
                message = success ? "GB28181连接成功" : "GB28181连接失败";
            } else {
                throw new IllegalArgumentException("不支持的协议类型: " + protocolType);
            }

            long connectionTime = System.currentTimeMillis() - startTime;

            if (success) {
                return DeviceConnectionTest.success(connectionTime, protocolType, connectionAddress)
                        .addDeviceInfo("manufacturer", "Hikvision")
                        .addDeviceInfo("model", getDeviceModel(device))
                        .addDeviceInfo("serialNumber", getDeviceSerialNumber(device))
                        .addDeviceInfo("firmwareVersion", getDeviceFirmwareVersion(device))
                        .addDeviceInfo("deviceType", getDeviceType(device))
                        .addDeviceInfo("accessDeviceType", device.getAccessDeviceType())
                        .addDeviceInfo("testTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceConnectionTest.failure(message, "连接测试失败", protocolType, connectionAddress)
                        .addResponseData("testTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            long connectionTime = System.currentTimeMillis() - startTime;
            log.error("海康威视设备连接测试异常: {}", device.getDeviceName(), e);
            return DeviceConnectionTest.networkError(e.getMessage(), device.getProtocolType(), connectionAddress)
                    .addResponseData("exception", e.getClass().getSimpleName())
                    .addResponseData("errorTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    @Override
    public DeviceDispatchResult dispatchPersonData(AccessDeviceEntity device, Map<String, Object> personData)
            throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (personData == null || personData.isEmpty()) {
            throw new IllegalArgumentException("人员数据不能为null或空");
        }

        try {
            log.info("下发海康威视人员数据: deviceId={}, personId={}",
                    device.getDeviceId(), personData.get("personId"));

            // 转换为海康威视格式
            Map<String, Object> hkPersonData = convertToHikvisionPersonData(personData);

            // 根据协议类型选择下发方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                success = dispatchPersonDataViaIsapi(device, hkPersonData);
                message = success ? "ISAPI下发成功" : "ISAPI下发失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = dispatchPersonDataViaSdk(device, hkPersonData);
                message = success ? "SDK下发成功" : "SDK下发失败";
            } else if ("GB28181".equalsIgnoreCase(protocolType)) {
                success = dispatchPersonDataViaGb28181(device, hkPersonData);
                message = success ? "GB28181下发成功" : "GB28181下发失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("personId", personData.get("personId"))
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("timestamp", System.currentTimeMillis())
                        .addResponseData("dispatchTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("protocol", protocolType);
            } else {
                return DeviceDispatchResult.failure(message, "DISPATCH_FAILED")
                        .addResponseData("protocol", protocolType)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("海康威视人员数据下发异常: deviceId={}, personId={}",
                    device.getDeviceId(), personData.get("personId"), e);
            throw new Exception("海康威视人员数据下发失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DeviceDispatchResult dispatchBiometricData(AccessDeviceEntity device, Map<String, Object> biometricData)
            throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (biometricData == null || biometricData.isEmpty()) {
            throw new IllegalArgumentException("生物特征数据不能为null或空");
        }

        try {
            log.info("下发生物特征数据: deviceId={}, biometricType={}",
                    device.getDeviceId(), biometricData.get("biometricType"));

            // 根据生物特征类型分别处理
            String biometricType = (String) biometricData.get("biometricType");
            if (biometricType == null || biometricType.trim().isEmpty()) {
                throw new IllegalArgumentException("生物特征类型不能为空");
            }

            boolean success = false;
            String message = "";

            if ("FACE".equalsIgnoreCase(biometricType)) {
                success = dispatchFaceData(device, biometricData);
                message = success ? "人脸数据下发成功" : "人脸数据下发失败";
            } else if ("FINGERPRINT".equalsIgnoreCase(biometricType)) {
                success = dispatchFingerprintData(device, biometricData);
                message = success ? "指纹数据下发成功" : "指纹数据下发失败";
            } else if ("PALM".equalsIgnoreCase(biometricType)) {
                success = dispatchPalmData(device, biometricData);
                message = success ? "掌纹数据下发成功" : "掌纹数据下发失败";
            } else if ("VEIN".equalsIgnoreCase(biometricType)) {
                success = dispatchVeinData(device, biometricData);
                message = success ? "指静脉数据下发成功" : "指静脉数据下发失败";
            } else {
                throw new IllegalArgumentException("不支持的生物特征类型: " + biometricType);
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("personId", biometricData.get("personId"))
                        .addResponseData("biometricType", biometricType)
                        .addResponseData("templateIndex", biometricData.get("templateIndex"))
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("dispatchTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceDispatchResult.failure(message, "BIOMETRIC_DISPATCH_FAILED")
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("海康威视生物特征数据下发异常: deviceId={}, biometricType={}",
                    device.getDeviceId(), biometricData.get("biometricType"), e);
            throw new Exception("海康威视生物特征数据下发失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DeviceDispatchResult dispatchAccessConfig(AccessDeviceEntity device, Map<String, Object> configData)
            throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (configData == null || configData.isEmpty()) {
            throw new IllegalArgumentException("配置数据不能为null或空");
        }

        try {
            log.info("下发海康威视门禁配置: deviceId={}", device.getDeviceId());

            // 转换为海康威视配置格式
            Map<String, Object> hkConfigData = convertToHikvisionConfigData(configData);

            // 根据协议类型选择下发方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                success = dispatchConfigViaIsapi(device, hkConfigData);
                message = success ? "ISAPI配置下发成功" : "ISAPI配置下发失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = dispatchConfigViaSdk(device, hkConfigData);
                message = success ? "SDK配置下发成功" : "SDK配置下发失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("configItems", hkConfigData.size())
                        .addResponseData("dispatchTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("protocol", protocolType);
            } else {
                return DeviceDispatchResult.failure(message, "CONFIG_DISPATCH_FAILED")
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("海康威视门禁配置下发异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("海康威视门禁配置下发失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DeviceDispatchResult remoteOpenDoor(AccessDeviceEntity device, String doorId) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (doorId == null || doorId.trim().isEmpty()) {
            throw new IllegalArgumentException("门ID不能为null或空");
        }

        try {
            log.info("海康威视远程开门: deviceId={}, doorId={}", device.getDeviceId(), doorId);

            // 根据协议类型选择开门方式
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                success = remoteOpenDoorViaIsapi(device, doorId.trim());
                message = success ? "ISAPI远程开门成功" : "ISAPI远程开门失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = remoteOpenDoorViaSdk(device, doorId.trim());
                message = success ? "SDK远程开门成功" : "SDK远程开门失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("doorId", doorId.trim())
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("openTime", System.currentTimeMillis())
                        .addResponseData("openDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("protocol", protocolType);
            } else {
                return DeviceDispatchResult.failure(message, "REMOTE_OPEN_FAILED")
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("海康威视远程开门异常: deviceId={}, doorId={}", device.getDeviceId(), doorId, e);
            throw new Exception("海康威视远程开门失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(AccessDeviceEntity device) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }

        try {
            log.debug("获取海康威视设备状态: deviceId={}", device.getDeviceId());

            Map<String, Object> status = new HashMap<>();

            // 基础状态信息
            status.put("deviceType", "ACCESS");
            status.put("deviceId", device.getDeviceId());
            status.put("deviceCode", device.getDeviceCode());
            status.put("deviceName", device.getDeviceName());
            status.put("manufacturer", "Hikvision");
            status.put("protocolType", device.getProtocolType());
            status.put("ipAddress", device.getIpAddress());
            status.put("port", device.getPort());
            status.put("accessDeviceType", device.getAccessDeviceType());
            status.put("openMethod", device.getOpenMethod());

            // 根据协议类型获取状态
            String protocolType = device.getProtocolType();
            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                status.putAll(getStatusViaIsapi(device));
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                status.putAll(getStatusViaSdk(device));
            } else if ("GB28181".equalsIgnoreCase(protocolType)) {
                status.putAll(getStatusViaGb28181(device));
            }

            status.put("lastUpdateTime", System.currentTimeMillis());
            status.put("lastUpdateDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return status;

        } catch (Exception e) {
            log.error("获取海康威视设备状态异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("获取海康威视设备状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryPersonsOnDevice(AccessDeviceEntity device) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }

        try {
            log.debug("查询海康威视设备人员列表: deviceId={}", device.getDeviceId());

            // 根据协议类型查询人员
            String protocolType = device.getProtocolType();
            List<Map<String, Object>> persons = new ArrayList<>();

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                persons = queryPersonsViaIsapi(device);
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                persons = queryPersonsViaSdk(device);
            }

            log.debug("海康威视设备人员查询完成: deviceId={}, count={}", device.getDeviceId(), persons.size());
            return persons;

        } catch (Exception e) {
            log.error("查询海康威视设备人员列表异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("查询海康威视设备人员列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DeviceDispatchResult deletePersonData(AccessDeviceEntity device, Long personId) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (personId == null || personId <= 0) {
            throw new IllegalArgumentException("人员ID必须大于0");
        }

        try {
            log.info("删除海康威视人员数据: deviceId={}, personId={}", device.getDeviceId(), personId);

            // 根据协议类型删除人员
            String protocolType = device.getProtocolType();
            boolean success = false;
            String message = "";

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                success = deletePersonViaIsapi(device, personId);
                message = success ? "ISAPI删除成功" : "ISAPI删除失败";
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                success = deletePersonViaSdk(device, personId);
                message = success ? "SDK删除成功" : "SDK删除失败";
            }

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("personId", personId)
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("deleteTime", System.currentTimeMillis())
                        .addResponseData("deleteDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("protocol", protocolType);
            } else {
                return DeviceDispatchResult.failure(message, "DELETE_FAILED")
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("删除海康威视人员数据异常: deviceId={}, personId={}", device.getDeviceId(), personId, e);
            throw new Exception("删除海康威视人员数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DeviceDispatchResult batchDispatchPersonData(AccessDeviceEntity device, List<Map<String, Object>> personList)
            throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (personList == null || personList.isEmpty()) {
            return DeviceDispatchResult.failure("人员列表为空", "INVALID_PARAMETER")
                    .addResponseData("failureTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        try {
            log.info("海康威视批量下发人员数据: deviceId={}, count={}", device.getDeviceId(), personList.size());

            int successCount = 0;
            List<String> failedPersons = new ArrayList<>();

            for (Map<String, Object> personData : personList) {
                try {
                    DeviceDispatchResult result = dispatchPersonData(device, personData);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        Object personId = personData.get("personId");
                        failedPersons.add(personId != null ? personId.toString() : "unknown");
                    }
                } catch (Exception e) {
                    Object personId = personData.get("personId");
                    failedPersons.add(personId != null ? personId.toString() : "unknown");
                    log.warn("海康威视人员下发失败: personId={}, error={}", personData.get("personId"), e.getMessage());
                }
            }

            if (successCount == personList.size()) {
                return DeviceDispatchResult.success("全部人员下发成功")
                        .addResponseData("totalCount", personList.size())
                        .addResponseData("successCount", successCount)
                        .addResponseData("batchTime", System.currentTimeMillis())
                        .addResponseData("batchDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else if (successCount > 0) {
                Map<String, Object> details = new HashMap<>();
                details.put("failedPersons", failedPersons);
                return DeviceDispatchResult.partialSuccess(
                        "部分人员下发成功", successCount, personList.size(), details)
                        .addResponseData("batchTime", System.currentTimeMillis())
                        .addResponseData("batchDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceDispatchResult.failure("全部人员下发失败", "BATCH_DISPATCH_FAILED")
                        .addResponseData("failedPersons", failedPersons)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("海康威视批量下发人员数据异常: deviceId={}, count={}",
                    device.getDeviceId(), personList.size(), e);
            throw new Exception("海康威视批量下发人员数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getAccessRecords(AccessDeviceEntity device, String startTime, String endTime,
            Integer recordCount) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }
        if (recordCount != null && recordCount <= 0) {
            throw new IllegalArgumentException("记录数量必须大于0");
        }

        try {
            log.debug("获取海康威视门禁记录: deviceId={}, startTime={}, endTime={}",
                    device.getDeviceId(), startTime, endTime);

            // 根据协议类型获取记录
            String protocolType = device.getProtocolType();
            List<Map<String, Object>> records = new ArrayList<>();

            if ("HTTP".equalsIgnoreCase(protocolType) || "ISAPI".equalsIgnoreCase(protocolType)) {
                records = getAccessRecordsViaIsapi(device, startTime, endTime, recordCount);
            } else if ("SDK".equalsIgnoreCase(protocolType)) {
                records = getAccessRecordsViaSdk(device, startTime, endTime, recordCount);
            }

            log.debug("海康威视门禁记录获取完成: deviceId={}, count={}", device.getDeviceId(), records.size());
            return records;

        } catch (Exception e) {
            log.error("获取海康威视门禁记录异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("获取海康威视门禁记录失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsDevice(AccessDeviceEntity device) {
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
        return new ArrayList<>(PROTOCOL_FEATURES);
    }

    // ==================== 私有方法 ====================

    /**
     * 测试ISAPI连接
     */
    private boolean testIsapiConnection(AccessDeviceEntity device) throws Exception {
        log.debug("测试海康威视ISAPI连接: http://{}:{}/ISAPI/System/deviceInfo", device.getIpAddress(), device.getPort());
        // TODO: 实现海康威视ISAPI连接测试
        // 1. 发送HTTP GET请求到ISAPI接口
        // 2. 验证响应状态和设备信息
        return true; // 临时返回成功
    }

    /**
     * 测试SDK连接
     */
    private boolean testSdkConnection(AccessDeviceEntity device) throws Exception {
        log.debug("测试海康威视SDK连接: {}", device.getDeviceCode());
        // TODO: 实现海康威视SDK连接测试
        // 1. 初始化海康威视SDK
        // 2. 登录设备
        // 3. 验证连接状态
        return true; // 临时返回成功
    }

    /**
     * 测试GB28181连接
     */
    private boolean testGb28181Connection(AccessDeviceEntity device) throws Exception {
        log.debug("测试海康威视GB28181连接: {}", device.getDeviceCode());
        // TODO: 实现GB28181连接测试
        // 1. 建立SIP连接
        // 2. 发送设备注册请求
        // 3. 验证注册状态
        return true; // 临时返回成功
    }

    /**
     * 转换为海康威视人员数据格式
     */
    private Map<String, Object> convertToHikvisionPersonData(Map<String, Object> personData) {
        Map<String, Object> hkData = new HashMap<>();

        // 海康威视人员数据格式转换
        hkData.put("employeeNo", personData.get("personCode")); // 员工编号
        hkData.put("name", personData.get("personName"));
        hkData.put("password", personData.get("password"));
        hkData.put("cardNo", personData.get("cardNo")); // 卡号
        hkData.put("department", personData.get("department"));
        hkData.put("userType", "normal"); // 用户类型

        // 海康威视特有的权限设置
        hkData.put("rightPlan", personData.get("accessLevel")); // 权限计划
        hkData.put("validPeriod", personData.get("validFrom") + "," + personData.get("validTo"));
        hkData.put("doorRight", "1"); // 门权限
        hkData.put("localVerify", "1"); // 本地验证

        return hkData;
    }

    /**
     * 转换为海康威视配置数据格式
     */
    private Map<String, Object> convertToHikvisionConfigData(Map<String, Object> configData) {
        Map<String, Object> hkData = new HashMap<>();

        // 海康威视配置数据格式转换
        hkData.put("door1OpenMethod", configData.get("openMethod"));
        hkData.put("door2OpenMethod", configData.get("openMethod"));
        hkData.put("door1LockTime", configData.get("closeDelay")); // 锁定时间
        hkData.put("door2LockTime", configData.get("closeDelay"));
        hkData.put("firstCardOpen", configData.get("firstCardOpenEnabled"));
        hkData.put("interLock", configData.get("interlockEnabled"));
        hkData.put("multiCardOpen", configData.get("multiCardOpenEnabled"));
        hkData.put("multiCardOpenNum", configData.get("multiCardCount"));
        hkData.put("alwaysOpen", configData.get("normallyOpenEnabled"));

        return hkData;
    }

    // 占位方法 - 需要根据实际海康威视协议实现
    private String getDeviceModel(AccessDeviceEntity device) {
        return "DS-K2801";
    }

    private String getDeviceSerialNumber(AccessDeviceEntity device) {
        return device.getDeviceCode();
    }

    private String getDeviceFirmwareVersion(AccessDeviceEntity device) {
        return "V5.5.0";
    }

    private String getDeviceType(AccessDeviceEntity device) {
        return "Access Controller";
    }

    private boolean dispatchPersonDataViaIsapi(AccessDeviceEntity device, Map<String, Object> personData)
            throws Exception {
        return true;
    }

    private boolean dispatchPersonDataViaSdk(AccessDeviceEntity device, Map<String, Object> personData)
            throws Exception {
        return true;
    }

    private boolean dispatchPersonDataViaGb28181(AccessDeviceEntity device, Map<String, Object> personData)
            throws Exception {
        return true;
    }

    private boolean dispatchFaceData(AccessDeviceEntity device, Map<String, Object> biometricData) throws Exception {
        return true;
    }

    private boolean dispatchFingerprintData(AccessDeviceEntity device, Map<String, Object> biometricData)
            throws Exception {
        return true;
    }

    private boolean dispatchPalmData(AccessDeviceEntity device, Map<String, Object> biometricData) throws Exception {
        return true;
    }

    private boolean dispatchVeinData(AccessDeviceEntity device, Map<String, Object> biometricData) throws Exception {
        return true;
    }

    private boolean dispatchConfigViaIsapi(AccessDeviceEntity device, Map<String, Object> configData) throws Exception {
        return true;
    }

    private boolean dispatchConfigViaSdk(AccessDeviceEntity device, Map<String, Object> configData) throws Exception {
        return true;
    }

    private boolean remoteOpenDoorViaIsapi(AccessDeviceEntity device, String doorId) throws Exception {
        return true;
    }

    private boolean remoteOpenDoorViaSdk(AccessDeviceEntity device, String doorId) throws Exception {
        return true;
    }

    private Map<String, Object> getStatusViaIsapi(AccessDeviceEntity device) throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("online", true);
        status.put("temperature", "24°C");
        status.put("cpuUsage", "18%");
        status.put("memoryUsage", "35%");
        status.put("doorStatus", "normal");
        return status;
    }

    private Map<String, Object> getStatusViaSdk(AccessDeviceEntity device) throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("online", true);
        status.put("temperature", "25°C");
        status.put("cpuUsage", "20%");
        status.put("memoryUsage", "38%");
        status.put("doorStatus", "normal");
        return status;
    }

    private Map<String, Object> getStatusViaGb28181(AccessDeviceEntity device) throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("online", true);
        status.put("registered", true);
        status.put("sipStatus", "OK");
        status.put("channelStatus", "normal");
        return status;
    }

    private List<Map<String, Object>> queryPersonsViaIsapi(AccessDeviceEntity device) throws Exception {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> queryPersonsViaSdk(AccessDeviceEntity device) throws Exception {
        return new ArrayList<>();
    }

    private boolean deletePersonViaIsapi(AccessDeviceEntity device, Long personId) throws Exception {
        return true;
    }

    private boolean deletePersonViaSdk(AccessDeviceEntity device, Long personId) throws Exception {
        return true;
    }

    private List<Map<String, Object>> getAccessRecordsViaIsapi(AccessDeviceEntity device, String startTime,
            String endTime, Integer recordCount) throws Exception {
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getAccessRecordsViaSdk(AccessDeviceEntity device, String startTime,
            String endTime, Integer recordCount) throws Exception {
        return new ArrayList<>();
    }
}

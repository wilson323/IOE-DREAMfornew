package net.lab1024.sa.access.adapter.protocol.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.adapter.protocol.AccessProtocolInterface;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.common.device.DeviceConnectionTest;
import net.lab1024.sa.common.device.DeviceDispatchResult;

/**
 * 通用HTTP协议适配器
 * <p>
 * 支持基于HTTP/HTTPS协议的门禁设备通信
 * 适用于标准HTTP API接口的门禁设备
 * 提供灵活的API端点配置和参数映射
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
@Component
public class HttpProtocolAdapter implements AccessProtocolInterface {

    private static final String PROTOCOL_NAME = "HTTP";
    private static final String PROTOCOL_VERSION = "1.0";

    // 通用HTTP协议支持的制造商（用于Generic设备）
    private static final List<String> SUPPORTED_MANUFACTURERS = Arrays.asList(
            "Generic", "HTTP", "REST", "API", "Custom", "JSON", "Standard");

    // HTTP协议特性
    private static final List<String> PROTOCOL_FEATURES = Arrays.asList(
            "HTTP API", "HTTPS支持", "RESTful接口", "JSON数据格式",
            "批量操作", "异步请求", "认证机制", "错误处理",
            "状态查询", "配置管理", "远程控制", "灵活扩展",
            "跨平台", "标准化", "易于集成", "调试友好");

    @Resource
    private RestTemplate restTemplate;

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
            log.info("测试HTTP门禁设备连接: {} ({})", device.getDeviceName(), connectionAddress);

            // 构建测试URL
            String testUrl = buildTestUrl(device);

            // 执行HTTP连接测试
            boolean success = testHttpConnection(device, testUrl);

            long connectionTime = System.currentTimeMillis() - startTime;

            if (success) {
                return DeviceConnectionTest.success(connectionTime, "HTTP", connectionAddress)
                        .addDeviceInfo("manufacturer", device.getManufacturer())
                        .addDeviceInfo("apiEndpoint", testUrl)
                        .addDeviceInfo("protocol", "HTTP/HTTPS")
                        .addDeviceInfo("apiVersion", getApiVersion(device))
                        .addDeviceInfo("testTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceConnectionTest.failure("HTTP连接测试失败", "CONNECTION_FAILED", "HTTP", connectionAddress)
                        .addResponseData("apiEndpoint", testUrl)
                        .addResponseData("testTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            long connectionTime = System.currentTimeMillis() - startTime;
            log.error("HTTP设备连接测试异常: {}", device.getDeviceName(), e);
            return DeviceConnectionTest.networkError(e.getMessage(), "HTTP", connectionAddress)
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
            log.info("下发HTTP门禁人员数据: deviceId={}, personId={}",
                    device.getDeviceId(), personData.get("personId"));

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "personDispatch");

            // 转换为HTTP API格式
            Map<String, Object> httpPersonData = convertToHttpPersonData(personData, apiConfig);

            // 执行HTTP请求
            Map<String, Object> response = executeHttpRequest(device, endpoint, httpPersonData, "POST");

            // 解析响应结果
            boolean success = parseSuccessResponse(response);
            String message = success ? "HTTP API下发成功" : "HTTP API下发失败";

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("personId", personData.get("personId"))
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("apiResponse", response)
                        .addResponseData("timestamp", System.currentTimeMillis())
                        .addResponseData("dispatchTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceDispatchResult.failure(message, "API_CALL_FAILED")
                        .addResponseData("apiResponse", response)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("HTTP人员数据下发异常: deviceId={}, personId={}",
                    device.getDeviceId(), personData.get("personId"), e);
            throw new Exception("HTTP人员数据下发失败: " + e.getMessage(), e);
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

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "biometricDispatch");

            // 转换为HTTP API格式
            Map<String, Object> httpBiometricData = convertToHttpBiometricData(biometricData, apiConfig);

            // 执行HTTP请求
            Map<String, Object> response = executeHttpRequest(device, endpoint, httpBiometricData, "POST");

            // 解析响应结果
            boolean success = parseSuccessResponse(response);
            String message = success ? "HTTP API下发成功" : "HTTP API下发失败";

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("personId", biometricData.get("personId"))
                        .addResponseData("biometricType", biometricData.get("biometricType"))
                        .addResponseData("templateIndex", biometricData.get("templateIndex"))
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("apiResponse", response)
                        .addResponseData("dispatchTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceDispatchResult.failure(message, "API_CALL_FAILED")
                        .addResponseData("apiResponse", response)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("HTTP生物特征数据下发异常: deviceId={}, biometricType={}",
                    device.getDeviceId(), biometricData.get("biometricType"), e);
            throw new Exception("HTTP生物特征数据下发失败: " + e.getMessage(), e);
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
            log.info("下发HTTP门禁配置: deviceId={}", device.getDeviceId());

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "configDispatch");

            // 转换为HTTP API格式
            Map<String, Object> httpConfigData = convertToHttpConfigData(configData, apiConfig);

            // 执行HTTP请求
            Map<String, Object> response = executeHttpRequest(device, endpoint, httpConfigData, "POST");

            // 解析响应结果
            boolean success = parseSuccessResponse(response);
            String message = success ? "HTTP API配置下发成功" : "HTTP API配置下发失败";

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("configItems", configData.size())
                        .addResponseData("apiResponse", response)
                        .addResponseData("dispatchTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                return DeviceDispatchResult.failure(message, "API_CALL_FAILED")
                        .addResponseData("apiResponse", response)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("HTTP门禁配置下发异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("HTTP门禁配置下发失败: " + e.getMessage(), e);
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
            log.info("HTTP远程开门: deviceId={}, doorId={}", device.getDeviceId(), doorId);

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "remoteOpen");

            // 构建开门请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("doorId", doorId.trim());
            requestData.put("action", "open");
            requestData.put("timestamp", System.currentTimeMillis());
            requestData.put("operator", "SmartAdmin");
            requestData.put("source", "RemoteOpen");

            // 执行HTTP请求
            Map<String, Object> response = executeHttpRequest(device, endpoint, requestData, "POST");

            // 解析响应结果
            boolean success = parseSuccessResponse(response);
            String message = success ? "HTTP远程开门成功" : "HTTP远程开门失败";

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("doorId", doorId.trim())
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("openTime", System.currentTimeMillis())
                        .addResponseData("openDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("apiResponse", response);
            } else {
                return DeviceDispatchResult.failure(message, "API_CALL_FAILED")
                        .addResponseData("apiResponse", response)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("HTTP远程开门异常: deviceId={}, doorId={}", device.getDeviceId(), doorId, e);
            throw new Exception("HTTP远程开门失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(AccessDeviceEntity device) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }

        try {
            log.debug("获取HTTP设备状态: deviceId={}", device.getDeviceId());

            Map<String, Object> status = new HashMap<>();

            // 基础状态信息
            status.put("deviceType", "ACCESS");
            status.put("deviceId", device.getDeviceId());
            status.put("deviceCode", device.getDeviceCode());
            status.put("deviceName", device.getDeviceName());
            status.put("manufacturer", device.getManufacturer());
            status.put("protocolType", device.getProtocolType());
            status.put("ipAddress", device.getIpAddress());
            status.put("port", device.getPort());
            status.put("accessDeviceType", device.getAccessDeviceType());
            status.put("openMethod", device.getOpenMethod());

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "deviceStatus");

            try {
                // 执行HTTP状态查询
                Map<String, Object> response = executeHttpRequest(device, endpoint, null, "GET");

                // 合并API响应到状态信息
                status.putAll(response);
                status.put("online", true);
                status.put("lastUpdateTime", System.currentTimeMillis());
                status.put("lastUpdateDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            } catch (Exception e) {
                log.warn("HTTP设备状态查询失败: deviceId={}, error={}", device.getDeviceId(), e.getMessage());
                status.put("online", false);
                status.put("error", e.getMessage());
                status.put("lastUpdateDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            return status;

        } catch (Exception e) {
            log.error("获取HTTP设备状态异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("获取HTTP设备状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryPersonsOnDevice(AccessDeviceEntity device) throws Exception {
        if (device == null) {
            throw new IllegalArgumentException("设备实体不能为null");
        }

        try {
            log.debug("查询HTTP设备人员列表: deviceId={}", device.getDeviceId());

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "personQuery");

            // 执行HTTP查询
            Map<String, Object> response = executeHttpRequest(device, endpoint, null, "GET");

            // 解析人员列表
            List<Map<String, Object>> persons = parsePersonListResponse(response);

            log.debug("HTTP设备人员查询完成: deviceId={}, count={}", device.getDeviceId(), persons.size());
            return persons;

        } catch (Exception e) {
            log.error("查询HTTP设备人员列表异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("查询HTTP设备人员列表失败: " + e.getMessage(), e);
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
            log.info("删除HTTP人员数据: deviceId={}, personId={}", device.getDeviceId(), personId);

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "personDelete");

            // 构建删除请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("personId", personId);
            requestData.put("action", "delete");
            requestData.put("timestamp", System.currentTimeMillis());

            // 执行HTTP请求
            Map<String, Object> response = executeHttpRequest(device, endpoint, requestData, "DELETE");

            // 解析响应结果
            boolean success = parseSuccessResponse(response);
            String message = success ? "HTTP删除成功" : "HTTP删除失败";

            if (success) {
                return DeviceDispatchResult.success(message)
                        .addResponseData("personId", personId)
                        .addResponseData("deviceCode", device.getDeviceCode())
                        .addResponseData("deleteTime", System.currentTimeMillis())
                        .addResponseData("deleteDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("apiResponse", response);
            } else {
                return DeviceDispatchResult.failure(message, "API_CALL_FAILED")
                        .addResponseData("apiResponse", response)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

        } catch (Exception e) {
            log.error("删除HTTP人员数据异常: deviceId={}, personId={}", device.getDeviceId(), personId, e);
            throw new Exception("删除HTTP人员数据失败: " + e.getMessage(), e);
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
            log.info("HTTP批量下发人员数据: deviceId={}, count={}", device.getDeviceId(), personList.size());

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "personBatch");

            // 构建批量请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("persons", personList);
            requestData.put("batchSize", personList.size());
            requestData.put("timestamp", System.currentTimeMillis());
            requestData.put("source", "SmartAdmin");

            // 执行HTTP请求
            Map<String, Object> response = executeHttpRequest(device, endpoint, requestData, "POST");

            // 解析批量响应结果
            Map<String, Object> batchResult = parseBatchResponse(response);
            int successCount = (Integer) batchResult.getOrDefault("successCount", 0);
            List<String> failedPersons = (List<String>) batchResult.getOrDefault("failedPersons", new ArrayList<>());

            if (successCount == personList.size()) {
                return DeviceDispatchResult.success("全部人员下发成功")
                        .addResponseData("totalCount", personList.size())
                        .addResponseData("successCount", successCount)
                        .addResponseData("batchTime", System.currentTimeMillis())
                        .addResponseData("batchDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("apiResponse", response);
            } else if (successCount > 0) {
                Map<String, Object> details = new HashMap<>();
                details.put("failedPersons", failedPersons);
                return DeviceDispatchResult.partialSuccess(
                        "部分人员下发成功", successCount, personList.size(), details)
                        .addResponseData("batchTime", System.currentTimeMillis())
                        .addResponseData("batchDateTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("apiResponse", response);
            } else {
                return DeviceDispatchResult.failure("全部人员下发失败", "BATCH_DISPATCH_FAILED")
                        .addResponseData("failedPersons", failedPersons)
                        .addResponseData("failureTime",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .addResponseData("apiResponse", response);
            }

        } catch (Exception e) {
            log.error("HTTP批量下发人员数据异常: deviceId={}, count={}",
                    device.getDeviceId(), personList.size(), e);
            throw new Exception("HTTP批量下发人员数据失败: " + e.getMessage(), e);
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
            log.debug("获取HTTP门禁记录: deviceId={}, startTime={}, endTime={}",
                    device.getDeviceId(), startTime, endTime);

            // 获取API配置
            Map<String, Object> apiConfig = getApiConfig(device);
            String endpoint = getApiEndpoint(apiConfig, "accessRecords");

            // 构建查询参数
            Map<String, Object> queryParams = new HashMap<>();
            if (startTime != null && !startTime.trim().isEmpty())
                queryParams.put("startTime", startTime.trim());
            if (endTime != null && !endTime.trim().isEmpty())
                queryParams.put("endTime", endTime.trim());
            if (recordCount != null)
                queryParams.put("limit", recordCount);

            // 执行HTTP查询
            Map<String, Object> response = executeHttpRequest(device, endpoint, queryParams, "GET");

            // 解析记录列表
            List<Map<String, Object>> records = parseAccessRecordsResponse(response);

            log.debug("HTTP门禁记录获取完成: deviceId={}, count={}", device.getDeviceId(), records.size());
            return records;

        } catch (Exception e) {
            log.error("获取HTTP门禁记录异常: deviceId={}", device.getDeviceId(), e);
            throw new Exception("获取HTTP门禁记录失败: " + e.getMessage(), e);
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

        // 检查协议类型
        String protocolType = device.getProtocolType();
        if (!"HTTP".equalsIgnoreCase(protocolType) && !"HTTPS".equalsIgnoreCase(protocolType)) {
            return false;
        }

        // 通用HTTP适配器支持所有制造商，主要用于Generic类型设备
        String manufacturer = device.getManufacturer();
        if (manufacturer == null || "Generic".equalsIgnoreCase(manufacturer.trim())) {
            return true;
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
     * 构建测试URL
     */
    private String buildTestUrl(AccessDeviceEntity device) {
        String protocol = "http";
        if ("HTTPS".equalsIgnoreCase(device.getProtocolType())) {
            protocol = "https";
        }
        return String.format("%s://%s:%d/api/test", protocol, device.getIpAddress(), device.getPort());
    }

    /**
     * 测试HTTP连接
     */
    private boolean testHttpConnection(AccessDeviceEntity device, String testUrl) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "SmartAdmin-AccessService/1.0");
            headers.add("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(testUrl, HttpMethod.GET, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("HTTP连接测试失败: {}", testUrl, e);
            return false;
        }
    }

    /**
     * 获取API配置
     */
    private Map<String, Object> getApiConfig(AccessDeviceEntity device) {
        // 从设备扩展配置中解析API配置
        Map<String, Object> config = new HashMap<>();
        config.put("baseApi", "/api/v1");
        config.put("authType", "Bearer");
        config.put("timeout", 30000);
        config.put("retryCount", 3);
        config.put("retryDelay", 1000);

        // 如果设备有自定义配置，则使用设备配置
        if (device.getExtensionConfig() != null && !device.getExtensionConfig().trim().isEmpty()) {
            try {
                // 这里可以解析JSON配置
                config.put("customConfig", device.getExtensionConfig());
            } catch (Exception e) {
                log.warn("解析设备自定义API配置失败: {}", device.getDeviceId(), e);
            }
        }

        return config;
    }

    /**
     * 获取API端点
     */
    private String getApiEndpoint(Map<String, Object> apiConfig, String operation) {
        String baseApi = (String) apiConfig.get("baseApi");

        switch (operation) {
            case "personDispatch":
                return baseApi + "/persons";
            case "biometricDispatch":
                return baseApi + "/biometrics";
            case "configDispatch":
                return baseApi + "/config";
            case "remoteOpen":
                return baseApi + "/doors/open";
            case "deviceStatus":
                return baseApi + "/status";
            case "personQuery":
                return baseApi + "/persons";
            case "personDelete":
                return baseApi + "/persons/delete";
            case "personBatch":
                return baseApi + "/persons/batch";
            case "accessRecords":
                return baseApi + "/records";
            default:
                return baseApi + "/unknown";
        }
    }

    /**
     * 执行HTTP请求
     */
    private Map<String, Object> executeHttpRequest(AccessDeviceEntity device, String endpoint,
            Map<String, Object> data, String method) throws Exception {
        try {
            String url = buildFullUrl(device, endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "SmartAdmin-AccessService/1.0");
            headers.add("Accept", "application/json");
            headers.add("Content-Type", "application/json");

            // 添加认证信息
            String authType = "Bearer"; // 可以从配置获取
            headers.add("Authorization", authType + " " + generateAuthToken(device));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);

            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
            ResponseEntity<Map> response = restTemplate.exchange(url, httpMethod, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody() != null ? response.getBody() : new HashMap<>();
            } else {
                throw new Exception("HTTP请求失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("执行HTTP请求失败: {} {}", method, endpoint, e);
            throw new Exception("HTTP请求执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建完整URL
     */
    private String buildFullUrl(AccessDeviceEntity device, String endpoint) {
        String protocol = "http";
        if ("HTTPS".equalsIgnoreCase(device.getProtocolType())) {
            protocol = "https";
        }
        return String.format("%s://%s:%d%s", protocol, device.getIpAddress(), device.getPort(), endpoint);
    }

    /**
     * 生成认证Token
     */
    private String generateAuthToken(AccessDeviceEntity device) {
        // 这里可以实现更复杂的认证逻辑
        return "device-token-" + device.getDeviceCode();
    }

    /**
     * 转换为HTTP API人员数据格式
     */
    private Map<String, Object> convertToHttpPersonData(Map<String, Object> personData, Map<String, Object> apiConfig) {
        Map<String, Object> httpData = new HashMap<>(personData);

        // 添加HTTP API特有的字段
        httpData.put("timestamp", System.currentTimeMillis());
        httpData.put("source", "SmartAdmin");
        httpData.put("version", "1.0");

        return httpData;
    }

    /**
     * 转换为HTTP API生物特征数据格式
     */
    private Map<String, Object> convertToHttpBiometricData(Map<String, Object> biometricData,
            Map<String, Object> apiConfig) {
        Map<String, Object> httpData = new HashMap<>(biometricData);

        // 添加HTTP API特有的字段
        httpData.put("timestamp", System.currentTimeMillis());
        httpData.put("source", "SmartAdmin");
        httpData.put("version", "1.0");

        return httpData;
    }

    /**
     * 转换为HTTP API配置数据格式
     */
    private Map<String, Object> convertToHttpConfigData(Map<String, Object> configData, Map<String, Object> apiConfig) {
        Map<String, Object> httpData = new HashMap<>(configData);

        // 添加HTTP API特有的字段
        httpData.put("timestamp", System.currentTimeMillis());
        httpData.put("source", "SmartAdmin");
        httpData.put("version", "1.0");

        return httpData;
    }

    /**
     * 解析成功响应
     */
    private boolean parseSuccessResponse(Map<String, Object> response) {
        if (response == null) {
            return false;
        }

        Object successObj = response.get("success");
        if (successObj instanceof Boolean) {
            return (Boolean) successObj;
        } else if (successObj instanceof Integer) {
            return ((Integer) successObj) == 1 || ((Integer) successObj) == 200;
        } else if (successObj instanceof String) {
            return "true".equalsIgnoreCase((String) successObj) || "success".equalsIgnoreCase((String) successObj);
        }

        // 检查状态码
        Object codeObj = response.get("code");
        if (codeObj instanceof Integer) {
            return ((Integer) codeObj) == 200 || ((Integer) codeObj) == 0;
        } else if (codeObj instanceof String) {
            return "200".equals(codeObj) || "0".equals(codeObj) || "success".equalsIgnoreCase((String) codeObj);
        }

        return false;
    }

    /**
     * 解析人员列表响应
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parsePersonListResponse(Map<String, Object> response) {
        Object dataObj = response.get("data");
        if (dataObj instanceof List) {
            return (List<Map<String, Object>>) dataObj;
        }

        // 尝试从其他字段获取
        Object listObj = response.get("list");
        if (listObj instanceof List) {
            return (List<Map<String, Object>>) listObj;
        }

        return new ArrayList<>();
    }

    /**
     * 解析批量响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseBatchResponse(Map<String, Object> response) {
        Map<String, Object> result = new HashMap<>();

        Object dataObj = response.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) dataObj;
            result.put("successCount", data.getOrDefault("successCount", 0));
            result.put("failedPersons", data.getOrDefault("failedPersons", new ArrayList<>()));
            result.put("total", data.getOrDefault("total", 0));
        } else {
            result.put("successCount", 0);
            result.put("failedPersons", new ArrayList<>());
            result.put("total", 0);
        }

        return result;
    }

    /**
     * 解析门禁记录响应
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAccessRecordsResponse(Map<String, Object> response) {
        Object dataObj = response.get("data");
        if (dataObj instanceof List) {
            return (List<Map<String, Object>>) dataObj;
        }

        // 尝试从records字段获取
        Object recordsObj = response.get("records");
        if (recordsObj instanceof List) {
            return (List<Map<String, Object>>) recordsObj;
        }

        return new ArrayList<>();
    }

    /**
     * 获取API版本
     */
    private String getApiVersion(AccessDeviceEntity device) {
        // 从设备配置或通过API查询获取版本信息
        return "1.0";
    }
}

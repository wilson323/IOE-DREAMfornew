# 设备适配器架构重设计方案

## 🎯 设计原则

基于用户反馈和业务需求分析，重新设计设备适配器架构，遵循以下核心原则：

1. **业务独立性**：每个业务模块的协议适配器独立维护
2. **通用性分离**：将通用的设备管理逻辑提取到base模块
3. **协议专用性**：业务相关的协议实现保留在各自业务模块
4. **接口标准化**：定义统一的适配器接口规范

## 🔍 当前问题分析

### 协议差异性分析

| 业务模块 | 设备类型 | 协议特点 | 数据格式 |
|---------|---------|---------|---------|
| **门禁模块** | 门禁控制器、生物识别设备 | TCP/HTTP厂商协议、SDK调用 | JSON/XML/二进制 |
| **考勤模块** | 考勤机、人脸识别终端 | HTTP/WebSocket、考勤专用协议 | JSON/自定义格式 |
| **消费模块** | POS机、售货机、食堂消费机 | TCP/串口/HTTP、支付协议 | JSON/二进制/ISO8583 |
| **视频模块** | 摄像头、NVR、DVR | RTSP/ONVIF/HTTP厂商协议 | XML/JSON/流媒体 |

### 核心发现
- **协议实现高度专业化**：每个厂商都有自己的协议规范
- **数据格式差异巨大**：JSON、XML、二进制、流媒体等
- **设备参数定制化强**：不同业务模块关注的设备参数完全不同
- **错误处理策略不同**：网络超时、设备故障的处理方式各异

## 🏗️ 新架构设计方案

### 方案：分层式设备适配器架构

#### 目录结构设计

```
sa-base/
├── src/main/java/net/lab1024/sa/base/module/
│   ├── device/                           # 设备管理基础模块
│   │   ├── entity/
│   │   │   ├── SmartDeviceEntity.java       # 基础设备实体
│   │   │   ├── DeviceType.java             # 设备类型枚举
│   │   │   └── DeviceStatus.java          # 设备状态枚举
│   │   ├── service/
│   │   │   └── SmartDeviceService.java     # 设备基础服务
│   │   ├── manager/
│   │   │   └── SmartDeviceManager.java     # 设备管理器
│   │   └── cache/
│   │       └── DeviceCacheManager.java     # 设备缓存管理
│   │
│   ├── biometric/                         # 生物特征管理模块
│   │   ├── service/
│   │   │   └── BiometricDispatchEngine.java # 生物特征下发引擎（协议无关）
│   │   └── manager/
│   │       └── BiometricCacheManager.java
│   │
│   └── common/
│       └── device/                        # 通用设备接口定义
│           ├── DeviceAdapterInterface.java  # 设备适配器接口规范
│           ├── DeviceDispatchResult.java    # 统一下发结果
│           ├── DeviceProtocolException.java # 统一异常定义
│           └── DeviceConnectionTest.java    # 连接测试工具类

sa-admin/module/
├── access/                                # 门禁模块
│   ├── adapter/                           # 门禁设备适配器
│   │   ├── AccessDeviceAdapter.java        # 门禁适配器主类
│   │   ├── protocol/                       # 协议实现
│   │   │   ├── ZKTecoAdapter.java         # 中控智慧协议
│   │   │   ├── HikvisionAdapter.java       # 海康威视协议
│   │   │   ├── DahuaAdapter.java           # 大华协议
│   │   │   └── HttpProtocolAdapter.java    # HTTP通用协议
│   │   └── utils/
│   │       ├── AccessProtocolUtils.java    # 协议工具类
│   │       └── BiometricDataConverter.java # 生物特征数据转换
│   │
├── attendance/                            # 考勤模块
│   ├── adapter/                           # 考勤设备适配器
│   │   ├── AttendanceDeviceAdapter.java    # 考勤适配器主类
│   │   ├── protocol/
│   │   │   ├── ZKTecoTimeAdapter.java      # 中控考勤协议
│   │   │   ├── AnvizAdapter.java           # 安讯士考勤协议
│   │   │   └── WebSocketProtocolAdapter.java # WebSocket协议
│   │   └── utils/
│   │       ├── AttendanceProtocolUtils.java
│   │       └── AttendanceDataConverter.java
│   │
├── consume/                               # 消费模块
│   ├── adapter/                           # 消费设备适配器
│   │   ├── ConsumeDeviceAdapter.java        # 消费适配器主类
│   │   ├── protocol/
│   │   │   ├── PosProtocolAdapter.java      # POS机协议
│   │   │   ├── VendingProtocolAdapter.java # 售货机协议
│   │   │   └── SerialProtocolAdapter.java  # 串口协议
│   │   └── utils/
│   │       ├── PaymentProtocolUtils.java   # 支付协议工具
│   │       └── PaymentDataConverter.java   # 支付数据转换
│   │
└── video/                                 # 视频模块
    ├── adapter/                           # 视频设备适配器
    │   ├── VideoDeviceAdapter.java          # 视频适配器主类
    │   ├── protocol/
    │   │   ├── OnvifAdapter.java            # ONVIF协议
    │   │   ├── RtspAdapter.java             # RTSP协议
    │   │   └── HttpVideoAdapter.java        # HTTP视频协议
    │   └── utils/
    │       ├── VideoProtocolUtils.java      # 视频协议工具
    │       └── StreamDataConverter.java     # 流数据转换
```

### 核心接口设计

#### 1. 统一设备适配器接口

```java
// sa-base/module/common/device/DeviceAdapterInterface.java
package net.lab1024.sa.base.module.common.device;

import java.util.List;
import java.util.Map;

public interface DeviceAdapterInterface {

    /**
     * 获取支持的设备类型
     */
    String getSupportedDeviceType();

    /**
     * 获取支持的设备厂商
     */
    List<String> getSupportedManufacturers();

    /**
     * 检查是否支持指定设备
     */
    boolean supportsDevice(SmartDeviceEntity device);

    /**
     * 测试设备连接
     */
    DeviceConnectionTest testConnection(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 下发人员基础信息
     */
    DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData)
        throws DeviceProtocolException;

    /**
     * 下发生物特征数据
     */
    DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData)
        throws DeviceProtocolException;

    /**
     * 下发配置信息
     */
    DeviceDispatchResult dispatchConfigData(SmartDeviceEntity device, Map<String, Object> configData)
        throws DeviceProtocolException;

    /**
     * 查询设备状态
     */
    Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 删除设备数据
     */
    DeviceDispatchResult deleteDeviceData(SmartDeviceEntity device, Long personId)
        throws DeviceProtocolException;
}
```

#### 2. 统一结果和异常定义

```java
// sa-base/module/common/device/DeviceDispatchResult.java
package net.lab1024.sa.base.module.common.device;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDispatchResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 响应数据
     */
    private Map<String, Object> responseData;

    /**
     * 操作耗时（毫秒）
     */
    private Long executionTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    // 成功结果
    public static DeviceDispatchResult success(String message, Map<String, Object> responseData) {
        return DeviceDispatchResult.builder()
            .success(true)
            .message(message)
            .responseData(responseData)
            .build();
    }

    // 失败结果
    public static DeviceDispatchResult failure(String message, String errorCode) {
        return DeviceDispatchResult.builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .build();
    }
}

// sa-base/module/common/device/DeviceProtocolException.java
package net.lab1024.sa.base.module.common.device;

public class DeviceProtocolException extends Exception {

    private String errorCode;
    private SmartDeviceEntity device;

    public DeviceProtocolException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DeviceProtocolException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public DeviceProtocolException(String message, SmartDeviceEntity device, String errorCode) {
        super(message);
        this.device = device;
        this.errorCode = errorCode;
    }

    // 静态工厂方法
    public static DeviceProtocolException connectionFailed(SmartDeviceEntity device) {
        return new DeviceProtocolException("设备连接失败: " + device.getDeviceName(),
                                        device, "CONNECTION_FAILED");
    }

    public static DeviceProtocolException protocolError(String protocol, String detail) {
        return new DeviceProtocolException("协议错误: " + protocol + " - " + detail,
                                        "PROTOCOL_ERROR");
    }

    public static DeviceProtocolException deviceNotSupported(String deviceType) {
        return new DeviceProtocolException("不支持的设备类型: " + deviceType,
                                        "DEVICE_NOT_SUPPORTED");
    }

    public static DeviceProtocolException dataFormatError(String detail) {
        return new DeviceProtocolException("数据格式错误: " + detail,
                                        "DATA_FORMAT_ERROR");
    }

    public static DeviceProtocolException timeoutError(Long timeoutMs) {
        return new DeviceProtocolException("操作超时: " + timeoutMs + "ms",
                                        "TIMEOUT_ERROR");
    }
}
```

### 业务模块适配器实现示例

#### 门禁模块适配器

```java
// sa-admin/module/access/adapter/AccessDeviceAdapter.java
package net.lab1024.sa.admin.module.access.adapter;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.module.common.device.DeviceAdapterInterface;
import net.lab1024.sa.base.module.common.device.DeviceDispatchResult;
import net.lab1024.sa.base.module.common.device.DeviceProtocolException;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.access.adapter.protocol.*;

@Slf4j
@Component
public class AccessDeviceAdapter implements DeviceAdapterInterface {

    // 门禁协议适配器映射
    private final Map<String, AccessProtocolInterface> protocolAdapters;

    public AccessDeviceAdapter() {
        this.protocolAdapters = Map.of(
            "ZKTeco", new ZKTecoAdapter(),
            "Hikvision", new HikvisionAdapter(),
            "Dahua", new DahuaAdapter(),
            "HTTP", new HttpProtocolAdapter()
        );
    }

    @Override
    public String getSupportedDeviceType() {
        return "ACCESS";
    }

    @Override
    public List<String> getSupportedManufacturers() {
        return List.of("ZKTeco", "Hikvision", "Dahua", "Generic");
    }

    @Override
    public boolean supportsDevice(SmartDeviceEntity device) {
        return "ACCESS".equals(device.getDeviceType())
            && protocolAdapters.containsKey(device.getManufacturer());
    }

    @Override
    public DeviceConnectionTest testConnection(SmartDeviceEntity device) throws DeviceProtocolException {
        AccessProtocolInterface adapter = getProtocolAdapter(device);
        return adapter.testConnection(device);
    }

    @Override
    public DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData)
            throws DeviceProtocolException {
        AccessProtocolInterface adapter = getProtocolAdapter(device);

        // 门禁特有的数据转换
        Map<String, Object> accessPersonData = convertToAccessPersonData(personData);

        return adapter.dispatchPersonData(device, accessPersonData);
    }

    @Override
    public DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData)
            throws DeviceProtocolException {
        AccessProtocolInterface adapter = getProtocolAdapter(device);

        // 门禁生物特征数据转换
        Map<String, Object> accessBiometricData = convertToAccessBiometricData(biometricData);

        return adapter.dispatchBiometricData(device, accessBiometricData);
    }

    @Override
    public DeviceDispatchResult dispatchConfigData(SmartDeviceEntity device, Map<String, Object> configData)
            throws DeviceProtocolException {
        AccessProtocolInterface adapter = getProtocolAdapter(device);
        return adapter.dispatchConfigData(device, configData);
    }

    @Override
    public Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws DeviceProtocolException {
        AccessProtocolInterface adapter = getProtocolAdapter(device);
        return adapter.getDeviceStatus(device);
    }

    @Override
    public DeviceDispatchResult deleteDeviceData(SmartDeviceEntity device, Long personId)
            throws DeviceProtocolException {
        AccessProtocolInterface adapter = getProtocolAdapter(device);
        return adapter.deletePersonData(device, personId);
    }

    // 门禁协议获取
    private AccessProtocolInterface getProtocolAdapter(SmartDeviceEntity device) throws DeviceProtocolException {
        String manufacturer = device.getManufacturer();
        AccessProtocolInterface adapter = protocolAdapters.get(manufacturer);

        if (adapter == null) {
            throw DeviceProtocolException.deviceNotSupported(manufacturer);
        }

        return adapter;
    }

    // 门禁数据转换方法
    private Map<String, Object> convertToAccessPersonData(Map<String, Object> personData) {
        // 门禁特有的人员数据转换逻辑
        return AccessProtocolUtils.convertPersonData(personData);
    }

    private Map<String, Object> convertToAccessBiometricData(Map<String, Object> biometricData) {
        // 门禁特有的生物特征数据转换逻辑
        return BiometricDataConverter.convertForAccess(biometricData);
    }
}
```

#### 门禁协议接口定义

```java
// sa-admin/module/access/adapter/protocol/AccessProtocolInterface.java
package net.lab1024.sa.admin.module.access.adapter.protocol;

import java.util.Map;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.module.common.device.DeviceDispatchResult;
import net.lab1024.sa.base.module.common.device.DeviceConnectionTest;
import net.lab1024.sa.base.module.common.device.DeviceProtocolException;

/**
 * 门禁设备协议接口
 * 定义门禁设备协议的标准操作
 */
public interface AccessProtocolInterface {

    /**
     * 测试设备连接
     */
    DeviceConnectionTest testConnection(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 下发人员信息
     */
    DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData)
        throws DeviceProtocolException;

    /**
     * 下发生物特征数据
     */
    DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData)
        throws DeviceProtocolException;

    /**
     * 下发门禁配置
     */
    DeviceDispatchResult dispatchAccessConfig(SmartDeviceEntity device, Map<String, Object> configData)
        throws DeviceProtocolException;

    /**
     * 删除人员信息
     */
    DeviceDispatchResult deletePersonData(SmartDeviceEntity device, Long personId)
        throws DeviceProtocolException;

    /**
     * 获取设备状态
     */
    Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws DeviceProtocolException;

    /**
     * 远程开门
     */
    DeviceDispatchResult remoteOpenDoor(SmartDeviceEntity device, String doorId)
        throws DeviceProtocolException;
}
```

#### 具体协议实现示例

```java
// sa-admin/module/access/adapter/protocol/ZKTecoAdapter.java
package net.lab1024.sa.admin.module.access.adapter.protocol;

import java.util.Map;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.module.common.device.DeviceDispatchResult;
import net.lab1024.sa.base.module.common.device.DeviceConnectionTest;
import net.lab1024.sa.base.module.common.device.DeviceProtocolException;

@Slf4j
@Component
public class ZKTecoAdapter implements AccessProtocolInterface {

    private static final String DEFAULT_PORT = "4370";
    private static final int TIMEOUT = 30000; // 30秒超时

    @Override
    public DeviceConnectionTest testConnection(SmartDeviceEntity device) throws DeviceProtocolException {
        try {
            String url = String.format("http://%s:%s/api/test",
                                       device.getIpAddress(),
                                       device.getPort() != null ? device.getPort() : DEFAULT_PORT);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return DeviceConnectionTest.success("ZKTeco设备连接成功");
            } else {
                throw DeviceProtocolException.connectionFailed(device);
            }

        } catch (Exception e) {
            log.error("ZKTeco设备连接测试失败", e);
            throw DeviceProtocolException.connectionFailed(device);
        }
    }

    @Override
    public DeviceDispatchResult dispatchPersonData(SmartDeviceEntity device, Map<String, Object> personData)
            throws DeviceProtocolException {

        try {
            // ZKTeco专用的人员数据格式
            Map<String, Object> zktecoData = new HashMap<>();
            zktecoData.put("PIN", personData.get("personId"));
            zktecoData.put("Name", personData.get("personName"));
            zktecoData.put("Password", personData.get("password"));
            zktecoData.put("Card", personData.get("cardNo"));
            zktecoData.put("Group", personData.get("accessGroup"));
            zktecoData.put("StartTime", personData.get("validFrom"));
            zktecoData.put("EndTime", personData.get("validTo"));

            // 调用ZKTeco SDK或HTTP接口
            boolean success = callZktecoAPI(device, "/api/person/set", zktecoData);

            if (success) {
                return DeviceDispatchResult.success("ZKTeco人员数据下发成功", zktecoData);
            } else {
                return DeviceDispatchResult.failure("ZKTeco人员数据下发失败", "API_ERROR");
            }

        } catch (Exception e) {
            log.error("ZKTeco人员数据下发失败", e);
            throw DeviceProtocolException.protocolError("ZKTeco", e.getMessage());
        }
    }

    @Override
    public DeviceDispatchResult dispatchBiometricData(SmartDeviceEntity device, Map<String, Object> biometricData)
            throws DeviceProtocolException {

        try {
            // ZKTeco生物特征数据格式（指纹、人脸等）
            Map<String, Object> zktecoBioData = new HashMap<>();
            zktecoBioData.put("PIN", biometricData.get("personId"));
            zktecoBioData.put("FingerID", biometricData.get("fingerId"));
            zktecoBioData.put("FingerData", biometricData.get("templateData"));
            zktecoBioData.put("Flag", biometricData.get("biometricType")); // 1-指纹, 9-人脸
            zktecoBioData.put("Size", biometricData.get("templateSize"));

            boolean success = callZktecoAPI(device, "/api/fingerprint/set", zktecoBioData);

            if (success) {
                return DeviceDispatchResult.success("ZKTeco生物特征数据下发成功", zktecoBioData);
            } else {
                return DeviceDispatchResult.failure("ZKTeco生物特征数据下发失败", "API_ERROR");
            }

        } catch (Exception e) {
            log.error("ZKTeco生物特征数据下发失败", e);
            throw DeviceProtocolException.protocolError("ZKTeco", e.getMessage());
        }
    }

    @Override
    public DeviceDispatchResult dispatchAccessConfig(SmartDeviceEntity device, Map<String, Object> configData)
            throws DeviceProtocolException {
        // ZKTeco门禁配置下发实现
        return DeviceDispatchResult.success("配置下发成功", configData);
    }

    @Override
    public DeviceDispatchResult deletePersonData(SmartDeviceEntity device, Long personId)
            throws DeviceProtocolException {
        try {
            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("PIN", personId);

            boolean success = callZktecoAPI(device, "/api/person/delete", deleteData);

            if (success) {
                return DeviceDispatchResult.success("ZKTeco人员数据删除成功", deleteData);
            } else {
                return DeviceDispatchResult.failure("ZKTeco人员数据删除失败", "API_ERROR");
            }

        } catch (Exception e) {
            log.error("ZKTeco人员数据删除失败", e);
            throw DeviceProtocolException.protocolError("ZKTeco", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeviceStatus(SmartDeviceEntity device) throws DeviceProtocolException {
        try {
            Map<String, Object> status = callZktecoAPIForStatus(device, "/api/device/status");
            return status;
        } catch (Exception e) {
            log.error("获取ZKTeco设备状态失败", e);
            throw DeviceProtocolException.protocolError("ZKTeco", "状态查询失败");
        }
    }

    @Override
    public DeviceDispatchResult remoteOpenDoor(SmartDeviceEntity device, String doorId)
            throws DeviceProtocolException {
        try {
            Map<String, Object> openData = new HashMap<>();
            openData.put("Door", doorId);

            boolean success = callZktecoAPI(device, "/api/door/open", openData);

            if (success) {
                return DeviceDispatchResult.success("ZKTeco远程开门成功", openData);
            } else {
                return DeviceDispatchResult.failure("ZKTeco远程开门失败", "API_ERROR");
            }

        } catch (Exception e) {
            log.error("ZKTeco远程开门失败", e);
            throw DeviceProtocolException.protocolError("ZKTeco", e.getMessage());
        }
    }

    // ZKTeco API调用辅助方法
    private boolean callZktecoAPI(SmartDeviceEntity device, String endpoint, Map<String, Object> data) {
        // 实现ZKTeco具体协议调用逻辑
        // 这里可以是HTTP调用、SDK调用或TCP通信
        return true; // 临时返回成功
    }

    private Map<String, Object> callZktecoAPIForStatus(SmartDeviceEntity device, String endpoint) {
        // 实现ZKTeco状态查询逻辑
        return new HashMap<>(); // 临时返回空状态
    }
}
```

### 生物特征下发引擎优化

```java
// sa-base/module/biometric/service/BiometricDispatchEngine.java
package net.lab1024.sa.base.module.biometric.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;

import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.base.module.device.service.SmartDeviceService;
import net.lab1024.sa.base.module.common.device.DeviceAdapterInterface;
import net.lab1024.sa.base.module.common.device.DeviceDispatchResult;
import net.lab1024.sa.base.module.common.device.DeviceProtocolException;
import net.lab1024.sa.base.module.biometric.entity.BiometricTemplateEntity;
import net.lab1024.sa.base.module.biometric.entity.PersonBiometricEntity;

/**
 * 生物特征下发引擎
 * 协议无关的统一下发引擎，通过适配器模式支持各业务模块的设备协议
 */
@Slf4j
@Service
public class BiometricDispatchEngine {

    @Resource
    private SmartDeviceService smartDeviceService;

    // 通过Spring注入各业务模块的设备适配器
    @Resource(name = "accessDeviceAdapter")
    private DeviceAdapterInterface accessDeviceAdapter;

    @Resource(name = "attendanceDeviceAdapter")
    private DeviceAdapterInterface attendanceDeviceAdapter;

    @Resource(name = "consumeDeviceAdapter")
    private DeviceAdapterInterface consumeDeviceAdapter;

    @Resource(name = "videoDeviceAdapter")
    private DeviceAdapterInterface videoDeviceAdapter;

    /**
     * 向指定设备下发生物特征数据
     */
    public Map<String, DeviceDispatchResult> dispatchBiometricToDevice(Long deviceId,
                                                                       Long personId,
                                                                       String biometricType) {

        Map<String, DeviceDispatchResult> results = new HashMap<>();

        try {
            // 获取设备信息
            SmartDeviceEntity device = smartDeviceService.getById(deviceId);
            if (device == null) {
                results.put("ERROR", DeviceDispatchResult.failure("设备不存在", "DEVICE_NOT_FOUND"));
                return results;
            }

            // 获取适配器
            DeviceAdapterInterface adapter = getDeviceAdapter(device.getDeviceType());
            if (adapter == null) {
                results.put("ERROR", DeviceDispatchResult.failure("设备类型不支持", "DEVICE_TYPE_NOT_SUPPORTED"));
                return results;
            }

            // 准备生物特征数据
            Map<String, Object> biometricData = prepareBiometricData(personId, biometricType);

            // 执行下发
            DeviceDispatchResult result = adapter.dispatchBiometricData(device, biometricData);
            results.put(device.getDeviceType(), result);

            log.info("生物特征下发完成: deviceId={}, personId={}, biometricType={}, result={}",
                    deviceId, personId, biometricType, result.isSuccess());

        } catch (Exception e) {
            log.error("生物特征下发异常", e);
            results.put("ERROR", DeviceDispatchResult.failure("下发异常: " + e.getMessage(), "DISPATCH_ERROR"));
        }

        return results;
    }

    /**
     * 批量向多个设备下发生物特征数据
     */
    public Map<Long, Map<String, DeviceDispatchResult>> batchDispatchBiometric(List<Long> deviceIds,
                                                                            Long personId,
                                                                            String biometricType) {
        Map<Long, Map<String, DeviceDispatchResult>> batchResults = new HashMap<>();

        for (Long deviceId : deviceIds) {
            Map<String, DeviceDispatchResult> deviceResults = dispatchBiometricToDevice(deviceId, personId, biometricType);
            batchResults.put(deviceId, deviceResults);
        }

        return batchResults;
    }

    /**
     * 根据设备类型获取对应的适配器
     */
    private DeviceAdapterInterface getDeviceAdapter(String deviceType) {
        switch (deviceType) {
            case "ACCESS":
                return accessDeviceAdapter;
            case "ATTENDANCE":
                return attendanceDeviceAdapter;
            case "CONSUME":
                return consumeDeviceAdapter;
            case "VIDEO":
                return videoDeviceAdapter;
            default:
                return null;
        }
    }

    /**
     * 准备生物特征数据
     */
    private Map<String, Object> prepareBiometricData(Long personId, String biometricType) {
        // 从生物特征服务获取数据
        Map<String, Object> biometricData = new HashMap<>();
        biometricData.put("personId", personId);
        biometricData.put("biometricType", biometricType);

        // 这里应该调用生物特征服务获取具体的模板数据
        // 为简化示例，这里直接返回基础信息
        return biometricData;
    }
}
```

## 🎯 方案优势

### 1. 业务独立性 ✅
- 每个业务模块的协议实现完全独立
- 协议代码不会相互影响
- 便于业务模块独立开发和维护

### 2. 协议专业化 ✅
- 针对具体厂商和协议的专门实现
- 协议细节处理更加精准
- 支持复杂的协议定制需求

### 3. 接口标准化 ✅
- 统一的适配器接口规范
- 标准化的结果和异常定义
- 便于上层业务调用

### 4. 扩展性强 ✅
- 新增厂商协议只需实现对应的Adapter
- 新增设备类型只需添加新的适配器
- 不影响现有业务模块

### 5. 维护性好 ✅
- 协议问题定位精确到具体模块
- 代码结构清晰，职责明确
- 便于团队协作开发

## 📋 实施建议

### 1. 分阶段实施
- **第一阶段**：创建基础接口和结果类
- **第二阶段**：实现门禁模块适配器（作为示例）
- **第三阶段**：推广到其他业务模块
- **第四阶段**：完善生物特征下发引擎

### 2. 文档先行
- 为每个适配器编写详细的协议文档
- 提供接口使用示例
- 建立协议测试规范

### 3. 测试覆盖
- 每个适配器都有独立的单元测试
- 协议异常场景的集成测试
- 设备模拟器支持自动化测试

这个方案既解决了架构问题，又保持了业务协议的独立性和专业性，是一个更加务实和可行的架构设计。
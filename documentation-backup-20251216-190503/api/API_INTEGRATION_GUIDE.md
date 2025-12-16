# IOE-DREAM API集成指南

**版本**: v1.0.0
**制定日期**: 2025-12-16
**适用范围**: 第三方开发者、系统集成商、企业用户
**目标**: 帮助开发者快速集成IOE-DREAM平台API

---

## 📋 快速开始

### 🎯 获取API密钥

1. **注册开发者账号**
   - 访问 [IOE-DREAM开发者中心](https://developer.ioe-dream.com)
   - 完成开发者注册和实名认证
   - 创建应用项目

2. **生成API密钥**
   ```bash
   curl -X POST "https://api.ioe-dream.com/v1/developer/api-keys" \
        -H "Authorization: Bearer YOUR_DEVELOPER_TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
          "keyName": "MyApp API Key",
          "permissions": ["access:read", "attendance:read", "consume:read"],
          "rateLimitPerMinute": 1000
        }'
   ```

3. **获取密钥信息**
   ```json
   {
     "apiKey": "IDR2A3X7Y9Z4C6D8E1F0G2H5I8J9K0L1M2N3O4P5",
     "keyId": "key_123",
     "expiresAt": "2026-12-16T00:00:00Z"
   }
   ```

### 🔐 API认证方式

#### 方式一：API Key认证（推荐）
```bash
curl -X GET "https://api.ioe-dream.com/v1/users/profile" \
     -H "X-API-Key: IDR2A3X7Y9Z4C6D8E1F0G2H5I8J9K0L1M2N3O4P5" \
     -H "Content-Type: application/json"
```

#### 方式二：OAuth 2.0认证
```bash
# 获取授权码
https://api.ioe-dream.com/oauth/authorize?
  response_type=code&
  client_id=YOUR_CLIENT_ID&
  redirect_uri=https://your-app.com/callback&
  scope=read write&
  state=random_state

# 使用授权码获取访问令牌
curl -X POST "https://api.ioe-dream.com/oauth/token" \
     -H "Authorization: Basic base64(client_id:client_secret)" \
     -d "grant_type=authorization_code&code=AUTH_CODE&redirect_uri=https://your-app.com/callback&state=STATE"
```

---

## 💻 开发环境配置

### 🛠️ Java SDK集成

#### 1. Maven依赖
```xml
<dependency>
    <groupId>ioe-dream</groupId>
    <artifactId>ioe-dream-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2. 基础配置
```java
@Configuration
public class IoeDreamConfig {

    @Value("${ioedream.api.key}")
    private String apiKey;

    @Value("${ioedream.api.baseUrl}")
    private String baseUrl;

    @Bean
    public IoeDreamClient ioeDreamClient() {
        return new IoeDreamClient(apiKey, baseUrl);
    }
}
```

#### 3. 使用示例
```java
@RestController
@RequestMapping("/api/external")
public class ExternalController {

    @Resource
    private IoeDreamClient ioeDreamClient;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = ioeDreamClient.auth().login(request);
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            return ResponseEntity.status(e.getStatusCode())
                           .body(ErrorResponse.fromException(e));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserVO> getUser(@PathVariable String userId) {
        UserVO user = ioeDreamClient.users().getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/device/{deviceId}/open")
    public ResponseEntity<AccessControlResult> openDoor(
            @PathVariable String deviceId,
            @RequestBody OpenDoorRequest request) {
        AccessControlResult result = ioeDreamClient.access().control(
            deviceId, "door_001", "OPEN", request
        );
        return ResponseEntity.ok(result);
    }
}
```

### 🐍 Python SDK集成

#### 1. 安装SDK
```bash
pip install ioe-dream-python-sdk
```

#### 2. 基础配置
```python
from ioe_dream import IoeDreamClient

# 初始化客户端
client = IoeDreamClient(
    api_key="YOUR_API_KEY",
    base_url="https://api.ioe-dream.com/v1"
)
```

#### 3. 使用示例
```python
from flask import Flask, request, jsonify

app = Flask(__name__)
client = IoeDreamClient(api_key="YOUR_API_KEY")

@app.route('/api/external/login', methods=['POST'])
def login():
    data = request.get_json()

    try:
        response = client.auth().login(data)
        return jsonify(response)
    except ApiError as e:
        return jsonify({
            "error": str(e),
            "code": e.status_code
        }), e.status_code

@app.route('/api/external/user/<user_id>')
def get_user(user_id):
    try:
        user = client.users().get_user_profile(user_id)
        return jsonify(user)
    except ApiError as e:
        return jsonify({
            "error": str(e),
            "code": e.status_code
        }), e.status_code
```

### 🌐 JavaScript/TypeScript SDK集成

#### 1. 安装SDK
```bash
npm install @ioe-dream/javascript-sdk
```

#### 2. 基础配置
```typescript
import { IoeDreamClient } from '@ioe-dream/javascript-sdk';

// 初始化客户端
const client = new IoeDreamClient({
    apiKey: 'YOUR_API_KEY',
    baseUrl: 'https://api.ioe-dream.com/v1'
});
```

#### 3. 使用示例
```typescript
import express from 'express';
import { IoeDreamClient } from '@ioe-dream/javascript-sdk';

const app = express();
const client = new IoeDreamClient({
    apiKey: 'YOUR_API_KEY',
    baseUrl: 'https://api.ioe-dream.com/v1'
});

app.post('/api/external/login', async (req, res) => {
    try {
        const response = await client.auth.login(req.body);
        res.json(response);
    } catch (error) {
        res.status(error.status || 500).json({
            error: error.message,
            code: error.code
        });
    }
});

app.get('/api/external/user/:userId', async (req, res) => {
    try {
        const user = await client.users.getUserProfile(req.params.userId);
        res.json(user);
    } catch (error) {
        res.status(error.status || 500).json({
            error: error.message,
            code: error.code
        });
    }
});
```

---

## 🔌 核心业务API集成示例

### 👤 用户认证集成

#### 1. 用户登录
```java
// Java示例
LoginRequest loginRequest = LoginRequest.builder()
    .username("john.doe")
    .password("password123")
    .clientType("MOBILE")
    .deviceInfo(DeviceInfo.builder()
        .deviceId("unique_device_id")
        .deviceName("iPhone 14")
        .osVersion("iOS 16.0")
        .build())
    .build();

LoginResponse response = ioeDreamClient.auth().login(loginRequest);
String accessToken = response.getAccessToken();

// 存储用户token到session或数据库
```

#### 2. 生物识别注册
```java
// 人脸识别注册
MultipartFile faceImage = getFaceImageFile();
BiometricRegistrationRequest request = BiometricRegistrationRequest.builder()
    .userId("user123")
    .biometricType("FACE")
    .imageData(Base64.getEncoder().encode(faceImage.getBytes()))
    .livenessCheck(true)
    .qualityThreshold(95)
    .build();

BiometricRegistrationResponse response = ioeDreamClient.users().registerBiometric(request);
String faceId = response.getBiometricId();
```

### 🔌 设备管理集成

#### 1. 设备注册
```python
# Python示例
device_data = {
    "device_name": "主入口门禁控制器",
    "device_type": "ACCESS_CONTROLLER",
    "vendor": "海康威视",
    "model": "DS-K2801",
    "serial_number": "HK-2023-001234",
    "ip_address": "192.168.1.100",
    "port": 80,
    "location": {
        "building_id": "building001",
        "floor_id": "floor01",
        "area_id": "area001"
    }
}

response = client.devices.register_device(device_data)
device_id = response['device_id']
```

#### 2. 设备控制
```python
# 远程开门控制
control_data = {
    "action": "OPEN_DOOR",
    "parameters": {
        "door_id": "door_001",
        "duration": 3000,
        "verify_identity": True
    }
}

response = client.devices.control_device(device_id, control_data)
command_id = response['command_id']
```

### 🚪 门禁管理集成

#### 1. 实时门禁控制
```typescript
// TypeScript示例
const accessRequest = {
    device_id: "device_123",
    access_point_id: "door_001",
    action: "OPEN",
    verification_method: "FACE_RECOGNITION",
    verification_data: {
        card_id: "card_456",
        pin: "encrypted_pin",
        biometric_id: "face_789",
        biometric_type: "FACE"
    }
};

try {
    const response = await client.access.control(accessRequest);
    console.log("门禁控制成功:", response.access_id);
} catch (error) {
    console.error("门禁控制失败:", error.message);
}
```

#### 2. 通行记录查询
```java
// Java示例
DateTime startTime = LocalDateTime.now().minusDays(7);
DateTime endTime = LocalDateTime.now();

AccessRecordQuery query = AccessRecordQuery.builder()
    .deviceId("device_123")
    .userId("user_123")
    .startTime(startTime)
    .endTime(endTime)
    .accessResult(AccessResult.GRANTED)
    .page(1)
    .size(20)
    .build();

PageResult<AccessRecordVO> records = client.access().getAccessRecords(query);
List<AccessRecordVO> accessRecords = records.getList();
```

### ⏰ 考勤管理集成

#### 1. 考勤打卡
```python
# Python示例
import base64
from datetime import datetime

clock_request = {
    "user_id": "user_123",
    "clock_type": "IN",
    "verification_method": "BIOMETRIC",
    "verification_data": {
        "face_image": base64.b64encode(face_image_bytes),
        "location_data": {
            "latitude": 39.9042,
            "longitude": 116.4074,
            "accuracy": 5.0
        }
    },
    "work_location": {
        "location_id": "location_001",
        "location_name": "公司总部",
        "device_id": "attendance_device_001"
    }
}

response = client.attendance.clock(clock_request)
attendance_id = response['attendance_id']
print(f"考勤记录ID: {attendance_id}")
```

#### 2. 请假申请
```java
// Java示例
LeaveApplicationRequest leaveRequest = LeaveApplicationRequest.builder()
    .userId("user_123")
    .leaveType("ANNUAL")
    .startDate(LocalDate.of(2025, 12, 20))
    .endDate(LocalDate.of(2025, 12, 25))
    .leaveDays(5)
    .reason("家庭旅行")
    .approverIds(Arrays.asList("manager_001", "hr_001"))
    .build();

LeaveApplicationResponse response = client.attendance.applyLeave(leaveRequest);
String leaveId = response.getLeaveId();
```

### 💳 消费管理集成

#### 1. 消费支付
```typescript
// TypeScript示例
const paymentRequest = {
    user_id: "user_123",
    payment_method: "FACE_RECOGNITION",
    amount: 25.50,
    currency: "CNY",
    transaction_type: "MEAL",
    merchant_id: "merchant_001",
    merchant_name: "员工餐厅",
    items: [
        {
            item_id: "item_001",
            item_name: "午餐套餐",
            quantity: 1,
            unit_price: 25.50,
            subtotal: 25.50
        }
    ],
    verification_data: {
        face_image: base64_face_image,
        confidence: 98.5
    }
};

try {
    const response = await client.consume.payment(paymentRequest);
    console.log("支付成功:", response.transaction_id);
} catch (error) {
    console.error("支付失败:", error.message);
}
```

#### 2. 账户余额查询
```python
# Python示例
response = client.consume.get_account_balance("user_123")
balance_info = response['data']

total_balance = balance_info['total_balance']
available_balance = balance_info['available_balance']
accounts = balance_info['accounts']

for account in accounts:
    print(f"账户类型: {account['account_type']}")
    print(f"余额: {account['balance']}")
    print(f"状态: {account['status']}")
```

### 👥 访客管理集成

#### 1. 访客预约
```java
// Java示例
VisitorAppointmentRequest appointmentRequest = VisitorAppointmentRequest.builder()
    .visitorInfo(VisitorInfo.builder()
            .name("张三")
            .company("ABC科技有限公司")
            .phone("+86-13800138000")
            .email("zhangsan@abc.com")
            .idCard("110101199001011234")
            .idCardType("ID_CARD")
            .build())
    .appointmentInfo(AppointmentInfo.builder()
            .visitDate(LocalDate.of(2025, 12, 20))
            .visitStartTime(LocalTime.of(14, 0))
            .visitEndTime(LocalTime.of(17, 0))
            .purpose("商务洽谈")
            .hostId("host_123")
            .hostName("李经理")
            .build())
    .accessControl(AccessControl.builder()
            .accessAreas(Arrays.asList("area_001", "area_002"))
            .accessLevels(Arrays.asList("LEVEL_1", "LEVEL_2"))
            .build())
    .build();

VisitorAppointmentResponse response = client.visitor.applyAppointment(appointmentRequest);
String appointmentId = response.getAppointmentId();
```

#### 2. 访客签到
```python
# Python示例
checkin_request = {
    "appointment_id": "appointment_123",
    "check_in_method": "QR_CODE",
    "verification_data": {
        "qr_code": "qr_data_here",
        "face_image": base64.b64encode(face_image_bytes)
    },
    "access_point": {
        "access_point_id": "entrance_001",
        "device_id": "device_123"
    }
}

response = client.visitor.check_in(checkin_request)
visit_id = response['visit_id']
print(f"访客ID: {visit_id}")
```

### 📹 视频监控集成

#### 1. 实时视频流
```java
// Java示例
String deviceId = "camera_001";
Map<String, String> params = Map.of(
    "streamType", "live",
    "quality", "high",
    "channel", "main"
);

try {
    // 获取视频流URL
    String streamUrl = client.video.getStreamUrl(deviceId, params);
    System.out.println("视频流URL: " + streamUrl);

    // 使用VLC或其他播放器播放流
    playVideoStream(streamUrl);

} catch (ApiException e) {
    System.err.println("获取视频流失败: " + e.getMessage());
}
```

#### 2. AI视频分析
```python
# Python示例
import base64

face_detection_request = {
    "device_id": "camera_001",
    "image_data": base64.b64encode(image_bytes),
    "detection_config": {
        "face_count": 10,
        "confidence_threshold": 0.85,
        "include_emotions": True,
        "include_age": True,
        "include_gender": True
    },
    "time_window": {
        "start_time": "2025-12-16T10:00:00Z",
        "end_time": "2025-12-16T10:05:00Z"
    }
}

response = client.video.ai_detect_faces(face_detection_request)
faces = response['data']['faces']

for face in faces:
    print(f"人脸ID: {face['face_id']}")
    print(f"置信度: {face['confidence']}")
    print(f"年龄: {face['attributes']['age']}")
    print(f"性别: {face['attributes']['gender']}")
```

---

## 🔄 Webhook集成

### 1. Webhook注册

#### 注册门禁事件Webhook
```bash
curl -X POST "https://api.ioe-dream.com/v1/webhooks/register" \
     -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "webhook_name": "门禁事件通知",
       "webhook_url": "https://your-app.com/webhooks/access-events",
       "event_types": ["ACCESS_GRANTED", "ACCESS_DENIED", "DEVICE_OFFLINE"],
       "authentication": {
         "type": "HMAC",
         "secret": "your_webhook_secret"
       },
       "retry_policy": {
         "max_retries": 3,
         "retry_delay": 1000
       }
     }'
```

### 2. 事件处理

#### 接收和处理Webhook事件
```java
// Spring Boot Webhook控制器
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Resource
    private WebhookService webhookService;

    @PostMapping("/access-events")
    public ResponseEntity<String> handleAccessEvent(
            @RequestBody String payload,
            @RequestHeader("X-Signature") String signature) {

        // 验证签名
        if (!webhookService.verifySignature(payload, signature)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        // 解析事件
        WebhookEvent event = JsonUtils.fromJson(payload, WebhookEvent.class);

        // 处理事件
        switch (event.getEventType()) {
            case "ACCESS_GRANTED":
                handleAccessGranted(event);
                break;
            case "ACCESS_DENIED":
                handleAccessDenied(event);
                break;
            case "DEVICE_OFFLINE":
                handleDeviceOffline(event);
                break;
            default:
                logger.warn("Unknown event type: " + event.getEventType());
        }

        return ResponseEntity.ok("Event processed");
    }

    private void handleAccessGranted(WebhookEvent event) {
        // 处理门禁授权事件
        AccessData accessData = event.getData();

        // 发送通知
        notificationService.sendAccessGrantedNotification(accessData);

        // 更新本地记录
        accessRecordService.updateAccessRecord(accessData);

        // 触发业务逻辑
        eventPublisher.publishEvent(new AccessGrantedEvent(accessData));
    }
}
```

---

## 📊 数据集成与同步

### 1. 实时数据同步

#### WebSocket实时数据推送
```javascript
// JavaScript WebSocket客户端
const ws = new WebSocket('wss://api.ioe-dream.com/v1/ws/realtime');

ws.onopen = function() {
    console.log('WebSocket连接已建立');

    // 订阅特定事件
    ws.send(JSON.stringify({
        action: 'subscribe',
        event_types: ['ACCESS_GRANTED', 'DEVICE_STATUS_CHANGE', 'ALARM_TRIGGERED']
    }));
};

ws.onmessage = function(event) {
    const data = JSON.parse(event.data);

    switch (data.event_type) {
        case 'ACCESS_GRANTED':
            handleAccessGrantedEvent(data);
            break;
        case 'DEVICE_STATUS_CHANGE':
            handleDeviceStatusChange(data);
            break;
        case 'ALARM_TRIGGERED':
            handleAlarmTriggeredEvent(data);
            break;
    }
};
```

#### 定时数据同步
```python
# Python定时同步任务
import asyncio
import schedule

async def sync_device_data():
    """同步设备数据"""
    try:
        # 获取设备列表
        devices = client.devices.get_devices(status='ONLINE')

        # 更新本地数据库
        for device in devices['devices']:
            update_local_device(device)

        print(f"同步了 {len(devices['devices'])} 个设备")

    except Exception as e:
        print(f"设备数据同步失败: {e}")

# 每5分钟同步一次
schedule.every(300).do(sync_device_data)

# 启动定时任务
schedule.run_pending()
```

### 2. 批量数据处理

#### 批量用户数据导入
```java
// Java批量数据导入
@Service
public class UserDataImportService {

    @Resource
    private IoeDreamClient ioeDreamClient;

    public void importUsersFromCSV(String csvFilePath) {
        try (FileReader reader = new FileReader(csvFilePath);
            CSVReader csvReader = new CSVReader(reader)) {
                String[] headers = csvReader.readNext();
                String[] line;

                while ((line = csvReader.readNext()) != null) {
                    Map<String, String> userData = new HashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        userData.put(headers[i], line[i]);
                    }

                    // 创建用户
                    importSingleUser(userData);
                }
            }

            logger.info("用户数据导入完成");

        } catch (Exception e) {
            logger.error("用户数据导入失败", e);
        }
    }

    private void importSingleUser(Map<String, String> userData) {
        try {
            UserCreationRequest request = UserCreationRequest.builder()
                .username(userData.get("username"))
                .displayName(userData.get("display_name"))
                .email(userData.get("email"))
                .phone(userData.get("phone"))
                .departmentId(userData.get("department_id"))
                .build();

            ioeDreamClient.users().createUser(request);

        } catch (Exception e) {
            logger.error("用户导入失败: " + e.getMessage(), e);
        }
    }
}
```

---

## 🚨 错误处理与最佳实践

### 1. 统一错误处理

#### 错误响应格式
```json
{
  "code": 401,
  "message": "Authentication failed",
  "error_code": "AUTH_FAILED",
  "details": "Invalid credentials or expired token",
  "timestamp": "2025-12-16T10:30:00Z",
  "path": "/api/v1/auth/login",
  "request_id": "req_123"
}
```

#### 错误处理实现
```java
// 统一异常处理器
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getStatus())
                .message(e.getMessage())
                .errorCode(e.getErrorCode())
                .details(e.getDetails())
                .timestamp(Instant.now())
                .path(e.getPath())
                .requestId(MDC.get("requestId"))
                .build();

        return ResponseEntity
                .status(e.getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(500)
                .message("Internal server error")
                .errorCode("INTERNAL_ERROR")
                .details(e.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(500)
                .body(errorResponse);
    }
}
```

### 2. 重试机制

#### 指数退避重试
```java
// 重试配置
@Configuration
public class RetryConfiguration {

    @Bean
    public RetryTemplate retryTemplate() {
        return new RetryTemplateBuilder()
                .maxAttempts(3)
                .exponentialBackoff(1000, 2.0)
                .retryOn(exception -> true)
                .build();
    }
}

@Service
public class ApiService {

    @Resource
    private RetryTemplate retryTemplate;

    public String callApiWithRetry() {
        return retryTemplate.execute(context -> {
            try {
                return ioeDreamClient.callApi();
            } catch (ApiException e) {
                if (e.isRetryable()) {
                    throw e;
                }
                throw new RuntimeException(e);
            }
        });
    }
}
```

### 3. 限流处理

#### 智能限流策略
```python
# Python智能限流装饰器
import time
import random
from functools import wraps

def rate_limit(max_calls_per_minute=60, max_calls_per_hour=1000):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 检查分钟级限流
            minute_key = f"rate_limit:minute:{time.strftime('%Y-%m-%d %H:%M')}"
            minute_count = cache.get(minute_key, 0)

            if minute_count >= max_calls_per_minute:
                sleep(random.uniform(1, 3))
                minute_count += 1
            cache.set(minute_key, minute_count, ttl=60)

            try:
                return func(*args, **kwargs)
            finally:
                minute_count = cache.get(minute_key, 0) - 1
                cache.set(minute_key, minute_count, ttl=60)
        return wrapper
    return decorator
```

---

## 📚 部署与监控

### 1. 生产环境配置

#### 环境变量配置
```bash
# .env.production
IOEDREAM_API_KEY=YOUR_PRODUCTION_API_KEY
IOEDREAM_BASE_URL=https://api.ioe-dream.com/v1
IOEDREAM_TIMEOUT=30000
IOEDREAM_RETRY_ATTEMPTS=3
IOEDREAM_LOG_LEVEL=INFO
```

#### Docker部署配置
```dockerfile
FROM openjdk:11-jre-slim

ENV IOEDREAM_API_KEY=${IOEDREAM_API_KEY}
ENV IOEDREAM_BASE_URL=${IOEDREAM_BASE_URL}
ENV JAVA_OPTS="-Xmx512m -Xms256m"

COPY target/ioe-dream-app.jar /app/ioe-dream-app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/ioe-dream-app.jar"]
```

### 2. 监控配置

#### Prometheus指标配置
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'ioe-dream-api'
    static_configs:
      - targets: ['api.ioe-dream.com:8080']
    metrics_path: '/actuator/prometheus'
    relabelings:
      - source_labels: [app='ioe-dream', env='production']
```

#### Grafana仪表板
```json
{
  "dashboard": {
    "title": "IOE-DREAM API监控",
    "panels": [
      {
        "title": "API请求量",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(api_requests_total[5m])",
            "legendFormat": "{{method}} {{endpoint}}"
          }
        ]
      },
      {
        "title": "API响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, api_response_time)",
            "legendFormat": "95th percentile"
          }
        ]
      }
    ]
  }
}
```

---

## 🎯 测试最佳实践

### 1. 单元测试

#### API客户端测试
```java
// Java单元测试
@SpringBootTest
class IoeDreamClientTest {

    @Mock
    private IoeDreamClient ioeDreamClient;

    @Test
    void testLoginSuccess() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("testpass")
                .build();

        LoginResponse expectedResponse = LoginResponse.builder()
                .accessToken("test_access_token")
                .refreshToken("test_refresh_token")
                .build();

        when(ioeDreamClient.auth().login(any())).thenReturn(expectedResponse);

        // When
        LoginResponse actualResponse = ioeDreamClient.auth().login(request);

        // Then
        assertThat(actualResponse.getAccessToken()).isEqualTo("test_access_token");
        assertThat(actualResponse.getRefreshToken()).isEqualTo("test_refresh_token");
    }
}
```

### 2. 集成测试

#### 端到端测试
```java
// 集成测试
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
class IoeDreamApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void testCompleteLoginFlow() {
        // 1. 用户登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass");

        ResponseEntity<LoginResponse> loginResponse = restTemplate.post(
                createURL("/api/v1/auth/login"), loginRequest, LoginResponse.class);

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().getAccessToken()).isNotEmpty();

        // 2. 使用access token访问用户信息
        String accessToken = loginResponse.getBody().getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("X-API-Key", "TEST_API_KEY");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserVO> userResponse = restTemplate.exchange(
                createURL("/api/v1/users/profile"),
                HttpMethod.GET, entity, UserVO.class);

        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userResponse.getBody().getUsername()).isEqualTo("testuser");
    }

    private String createURL(String path) {
        return "http://localhost:" + port + path;
    }
}
```

### 3. 性能测试

#### API性能基准测试
```python
# Python性能测试
import asyncio
import aiohttp
import time
import statistics

async def benchmark_api_performance():
    """API性能基准测试"""
    base_url = "https://api.ioe-dream.com/v1"
    api_key = "YOUR_API_KEY"

    headers = {
        "Authorization": "Bearer ACCESS_TOKEN",
        "X-API-Key": api_key,
        "Content-Type": "application/json"
    }

    # 测试多个端点的性能
    endpoints = [
        "/users/profile",
        "/devices/list",
        "/access/records",
        "/attendance/clock"
    ]

    for endpoint in endpoints:
        response_times = []

        # 执行100次请求
        for i in range(100):
            start_time = time.time()

            async with aiohttp.ClientSession() as session:
                async with session.get(
                    url=base_url + endpoint,
                    headers=headers
                ) as response:
                    end_time = time.time()
                    response_times.append(end_time - start_time)

        # 计算统计信息
        avg_time = statistics.mean(response_times)
        p95_time = statistics.quantiles(response_times, 0.95)
        max_time = max(response_times)
        min_time = min(response_times)

        print(f"端点: {endpoint}")
        print(f"平均响应时间: {avg_time:.3f}ms")
        print(f"95%响应时间: {p95_time:.3f}ms")
        print(f"最大响应时间: {max_time:.3f}ms")
        print(f"最小响应时间: {min_time:.3f}ms")
        print(f"总请求数: {len(response_times)}")
        print()
```

---

## 📚� 常见问题与解决方案

### 1. 认证问题

#### 问题：API密钥无效
```bash
# 检查API密钥状态
curl -X GET "https://api.ioe-dream.com/v1/developer/api-keys" \
     -H "Authorization: Bearer YOUR_DEVELOPERER_TOKEN"
```

#### 解决方案：
1. 检查API密钥是否过期
2. 验证API密钥权限范围
3. 重新生成API密钥

### 2. 限流问题

#### 问题：请求频率过高被限流
```bash
# 检查限流状态
curl -I -H "X-API-Key: YOUR_API_KEY" \
     "https://api.ioe-dream.com/v1/rate-limit/status"
```

#### 解决方案：
1. 检查当前配额使用情况
2. 实现客户端限流逻辑
3. 考虑升级API密钥套餐

### 3. 数据格式问题

#### 问题：请求/响应格式不正确
```json
// 错误：缺少Content-Type头
// 正确：明确指定Content-Type
headers.put("Content-Type", "application/json")
```

#### 解决方案：
1. 检查HTTP头设置
2. 验证JSON格式
3. 使用官方SDK避免格式问题

---

## 📚� 技术支持

### 📞 开发文档
- [API参考文档](https://api.ioe-dream.com/docs)
- [SDK使用指南](https://docs.ioe-dream.com/sdk)
- [Webhook开发指南](https://docs.ioe-dream.com/webhooks)

### 💬 社区支持
- [开发者论坛](https://forum.ioe-dream.com)
- [技术博客](https://blog.ioe-dream.com)
- [GitHub仓库](https://github.com/ioe-dream)

### 📞 技术支持
- 技术支持邮箱：api-support@ioe-dream.com
- 工单支持：https://support.ioe-dream.com/tickets
- 开发者交流群：微信群/钉钉群

---

**文档版本**: v1.0.0
**制定团队**: IOE-DREAM API设计团队
**最后更新**: 2025-12-16
**下次评审**: 2026-01-16
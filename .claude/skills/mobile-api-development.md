---
name: mobile-api-development
description: IOE-DREAM移动端API开发规范和uni-app最佳实践
version: 1.0.0
---

# IOE-DREAM移动端API开发规范

> **技术栈**: uni-app 3.0 + Vue 3.2 + Pinia 2.0
> **目标**: 统一移动端API调用规范，确保移动端体验一致性
> **适用**: IOE-DREAM智慧园区一卡通移动应用

## 🎯 快速开始

### 1. 移动端API路径规范

```yaml
# 移动端专用前缀
/api/v1/mobile/{module}/{action}

# 示例
/api/v1/mobile/access/check          # 移动端门禁检查
/api/v1/mobile/consume/quick          # 移动端快速消费
/api/v1/mobile/attendance/clock       # 移动端考勤打卡
```

### 2. 统一请求配置

```javascript
// 必需请求头
{
  'Authorization': 'Bearer {token}',
  'X-Platform': 'ios|android|web',
  'X-Device-ID': '{unique_device_id}',
  'X-App-Version': '{app_version}',
  'X-Request-ID': '{unique_request_id}',
  'X-Timestamp': '{unix_timestamp}'
}
```

## 📱 核心移动端API

### 门禁验证 (/api/v1/mobile/access)

```yaml
# 门禁检查
POST   /api/v1/mobile/access/check
{
  "userId": 1001,
  "deviceId": "DEV001",
  "accessMode": "FACE|CARD|NFC|QR"
}

# 二维码验证
POST   /api/v1/mobile/access/qr/verify
{
  "qrCode": "QR_DATA_STRING",
  "timestamp": 1703847600000
}

# NFC验证
POST   /api/v1/mobile/access/nfc/verify
{
  "nfcData": "NFC_DATA_STRING",
  "timestamp": 1703847600000
}

# 生物识别验证
POST   /api/v1/mobile/access/biometric/verify
{
  "biometricType": "FACE|FINGERPRINT|IRIS",
  "biometricData": "ENCRYPTED_BIOMETRIC_DATA",
  "timestamp": 1703847600000
}

# 获取附近设备
GET    /api/v1/mobile/access/devices/nearby
params: {
  "userId": 1001,
  "latitude": 31.2304,
  "longitude": 121.4737,
  "radius": 500
}
```

### 消费支付 (/api/v1/mobile/consume)

```yaml
# 快速消费
POST   /api/v1/mobile/consume/quick
{
  "amount": 15.50,
  "consumeMode": "FACE|CARD|NFC|QR",
  "deviceId": "POS001"
}

# 扫码消费
POST   /api/v1/mobile/consume/scan
{
  "qrCode": "PAYMENT_QR_CODE",
  "amount": 15.50
}

# 人脸消费
POST   /api/v1/mobile/consume/face
{
  "faceData": "ENCRYPTED_FACE_DATA",
  "amount": 15.50,
  "deviceId": "FACE_DEV001"
}

# NFC刷卡消费
POST   /api/v1/mobile/consume/nfc
{
  "nfcData": "CARD_NFC_DATA",
  "amount": 15.50
}

# 获取账户余额
GET    /api/v1/mobile/consume/account/balance
response: {
  "balance": 1250.75,
  "frozenAmount": 0.00,
  "availableAmount": 1250.75
}

# 消费记录
GET    /api/v1/mobile/consume/records
params: {
  "userId": 1001,
  "pageSize": 20,
  "lastId": null  // 游标分页
}
```

### 考勤打卡 (/api/v1/mobile/attendance)

```yaml
# 考勤打卡
POST   /api/v1/mobile/attendance/clock
{
  "clockType": "IN|OUT",  // 上班/下班
  "deviceId": "ATT_DEV001",
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "address": "公司大楼A座"
  },
  "biometricData": "OPTIONAL_BIOMETRIC_DATA"
}

# 位置验证
POST   /api/v1/mobile/attendance/location/verify
{
  "latitude": 31.2304,
  "longitude": 121.4737,
  "deviceId": "ATT_DEV001"
}

# 考勤记录
GET    /api/v1/mobile/attendance/records
params: {
  "userId": 1001,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31"
}

# 获取排班信息
GET    /api/v1/mobile/attendance/schedule/current
response: {
  "shiftId": 1001,
  "shiftName": "正常班",
  "workStartTime": "09:00",
  "workEndTime": "18:00",
  "todayStatus": "NORMAL|LATE|EARLY|ABSENT"
}
```

### 访客管理 (/api/v1/mobile/visitor)

```yaml
# 访客签到
POST   /api/v1/mobile/visitor/checkin
{
  "appointmentId": 1001,
  "faceData": "OPTIONAL_FACE_DATA",
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737
  }
}

# 访客签退
POST   /api/v1/mobile/visitor/checkout
{
  "visitId": 1001,
  "checkoutTime": "2025-12-15T18:00:00"
}

# 我的预约
GET    /api/v1/mobile/visitor/appointments
params: {
  "status": "PENDING|APPROVED|COMPLETED|CANCELLED"
}

# 访问记录
GET    /api/v1/mobile/visitor/records
params: {
  "userId": 1001,
  "pageSize": 20
}
```

### 用户中心 (/api/v1/mobile/user)

```yaml
# 获取用户信息
GET    /api/v1/mobile/user/profile
response: {
  "id": 1001,
  "name": "张三",
  "avatar": "https://example.com/avatar.jpg",
  "department": "技术部",
  "position": "软件工程师",
  "employeeNo": "EMP001"
}

# 更新用户信息
PUT    /api/v1/mobile/user/profile
{
  "avatar": "NEW_AVATAR_URL",
  "phone": "13800138000",
  "email": "zhangsan@company.com"
}

# 修改密码
PUT    /api/v1/mobile/user/password
{
  "oldPassword": "old_password",
  "newPassword": "new_password"
}

# 消息通知
GET    /api/v1/mobile/user/notifications
params: {
  "type": "SYSTEM|BUSINESS|REMINDER",
  "unreadOnly": true,
  "pageSize": 20
}
```

## 🔧 开发实现模板

### 请求封装

```javascript
// src/lib/request.js
import { getToken, removeToken } from '@/lib/auth';

const BASE_URL = process.env.VUE_APP_API_BASE_URL + '/api/v1';

class MobileRequest {
  constructor() {
    this.baseURL = BASE_URL;
    this.timeout = 30000;
  }

  // 生成请求头
  getHeaders() {
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`,
      'X-Platform': uni.getSystemInfoSync().platform,
      'X-Device-ID': this.getDeviceId(),
      'X-App-Version': process.env.VUE_APP_VERSION,
      'X-Request-ID': this.generateRequestId(),
      'X-Timestamp': Date.now()
    };
  }

  // 获取设备ID
  getDeviceId() {
    let deviceId = uni.getStorageSync('device_id');
    if (!deviceId) {
      deviceId = this.generateDeviceId();
      uni.setStorageSync('device_id', deviceId);
    }
    return deviceId;
  }

  // 生成请求ID
  generateRequestId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  // 核心请求方法
  request(options) {
    return new Promise((resolve, reject) => {
      const config = {
        url: this.baseURL + options.url,
        method: options.method || 'GET',
        data: options.data || {},
        header: { ...this.getHeaders(), ...options.header },
        timeout: options.timeout || this.timeout
      };

      // 数据加密
      if (options.encrypt) {
        config.data = this.encryptData(config.data);
        config.header['X-Encrypted'] = 'true';
      }

      uni.request({
        ...config,
        success: (response) => {
          this.handleResponse(response, resolve, reject);
        },
        fail: (error) => {
          this.handleError(error, reject);
        }
      });
    });
  }

  // 响应处理
  handleResponse(response, resolve, reject) {
    const res = response.data;

    // 数据解密
    if (response.header['x-encrypted'] === 'true' && res.encryptData) {
      res.data = this.decryptData(res.encryptData);
    }

    // 成功响应
    if (res.code === 200 || res.success === true) {
      resolve(res);
      return;
    }

    // Token过期处理
    if (res.code === 30007 || res.code === 30008 || res.code === 30012) {
      uni.showToast({
        title: '登录已过期',
        icon: 'none'
      });
      removeToken();
      uni.reLaunch({
        url: '/pages/login/login'
      });
      reject(new Error(res.message));
      return;
    }

    // 其他错误
    uni.showToast({
      title: res.message || '操作失败',
      icon: 'none'
    });
    reject(new Error(res.message));
  }

  // 错误处理
  handleError(error, reject) {
    console.error('请求错误:', error);

    let message = '网络错误，请稍后重试';

    if (error.errMsg) {
      if (error.errMsg.includes('timeout')) {
        message = '请求超时，请稍后重试';
      } else if (error.errMsg.includes('fail')) {
        message = '网络连接失败';
      }
    }

    uni.showToast({
      title: message,
      icon: 'none'
    });

    reject(new Error(message));
  }

  // 便捷方法
  get(url, params = {}, options = {}) {
    return this.request({
      url,
      method: 'GET',
      data: params,
      ...options
    });
  }

  post(url, data = {}, options = {}) {
    return this.request({
      url,
      method: 'POST',
      data,
      ...options
    });
  }

  put(url, data = {}, options = {}) {
    return this.request({
      url,
      method: 'PUT',
      data,
      ...options
    });
  }
}

export default new MobileRequest();
```

### API模块封装

```javascript
// src/api/mobile-access.js
import request from '@/lib/request';

export const mobileAccessApi = {
  // 门禁检查
  checkAccess: (data) => {
    return request.post('/mobile/access/check', data, {
      encrypt: true
    });
  },

  // 二维码验证
  verifyQR: (qrCode) => {
    return request.post('/mobile/access/qr/verify', {
      qrCode,
      timestamp: Date.now()
    });
  },

  // NFC验证
  verifyNFC: (nfcData) => {
    return request.post('/mobile/access/nfc/verify', {
      nfcData,
      timestamp: Date.now()
    });
  },

  // 人脸识别验证
  verifyFace: (faceData) => {
    return request.post('/mobile/access/biometric/verify', {
      biometricType: 'FACE',
      biometricData: faceData,
      timestamp: Date.now()
    }, {
      encrypt: true // 生物识别数据必须加密
    });
  },

  // 获取附近设备
  getNearbyDevices: (latitude, longitude, radius = 500) => {
    return request.get('/mobile/access/devices/nearby', {
      latitude,
      longitude,
      radius
    });
  },

  // 获取用户权限
  getUserPermissions: () => {
    return request.get('/mobile/access/permissions');
  }
};
```

### 组件中使用

```vue
<template>
  <view class="access-check">
    <view class="device-list" v-if="devices.length > 0">
      <view class="device-item" v-for="device in devices" :key="device.id">
        <text>{{device.name}}</text>
        <text>{{device.location}}</text>
        <button @click="checkAccess(device)">验证</button>
      </view>
    </view>

    <view class="biometric-check">
      <button @click="startFaceRecognition">人脸识别</button>
      <button @click="startNFCRead">NFC读卡</button>
      <button @click="scanQRCode">扫码验证</button>
    </view>
  </view>
</template>

<script>
import { mobileAccessApi } from '@/api/mobile-access';
import { useUserStore } from '@/stores/user';

export default {
  data() {
    return {
      devices: [],
      loading: false
    };
  },

  async onLoad() {
    await this.loadNearbyDevices();
  },

  methods: {
    // 加载附近设备
    async loadNearbyDevices() {
      try {
        const position = await this.getCurrentPosition();
        const response = await mobileAccessApi.getNearbyDevices(
          position.latitude,
          position.longitude,
          500
        );

        if (response.code === 200) {
          this.devices = response.data;
        }
      } catch (error) {
        console.error('加载附近设备失败:', error);
      }
    },

    // 获取当前位置
    getCurrentPosition() {
      return new Promise((resolve, reject) => {
        uni.getLocation({
          type: 'wgs84',
          success: (res) => {
            resolve({
              latitude: res.latitude,
              longitude: res.longitude
            });
          },
          fail: reject
        });
      });
    },

    // 门禁验证
    async checkAccess(device) {
      if (this.loading) return;

      this.loading = true;
      uni.showLoading({
        title: '正在验证...'
      });

      try {
        const userStore = useUserStore();
        const response = await mobileAccessApi.checkAccess({
          userId: userStore.userInfo.id,
          deviceId: device.id,
          accessMode: 'FACE'
        });

        if (response.code === 200) {
          uni.showToast({
            title: '验证成功',
            icon: 'success'
          });

          // 记录访问日志
          this.logAccessRecord(device.id, 'SUCCESS');
        }
      } catch (error) {
        uni.showToast({
          title: '验证失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
        uni.hideLoading();
      }
    },

    // 人脸识别
    async startFaceRecognition() {
      try {
        const faceData = await this.captureFace();
        const response = await mobileAccessApi.verifyFace(faceData);

        if (response.code === 200) {
          uni.showToast({
            title: '人脸验证成功',
            icon: 'success'
          });
        }
      } catch (error) {
        uni.showToast({
          title: '人脸验证失败',
          icon: 'none'
        });
      }
    },

    // 人脸数据采集
    captureFace() {
      return new Promise((resolve, reject) => {
        // 调用原生人脸识别插件
        plus.device.capture(
          (path) => {
            // 处理人脸数据
            const faceData = this.processFaceData(path);
            resolve(faceData);
          },
          (error) => {
            reject(error);
          },
          {
            filename: '_doc/face_' + Date.now() + '.jpg',
            resolution: '640x480'
          }
        );
      });
    },

    // NFC读卡
    async startNFCRead() {
      try {
        const nfcData = await this.readNFC();
        const response = await mobileAccessApi.verifyNFC(nfcData);

        if (response.code === 200) {
          uni.showToast({
            title: 'NFC验证成功',
            icon: 'success'
          });
        }
      } catch (error) {
        uni.showToast({
          title: 'NFC验证失败',
          icon: 'none'
        });
      }
    },

    // NFC数据读取
    readNFC() {
      return new Promise((resolve, reject) => {
        // 调用原生NFC读取插件
        nfc.readNDEF(
          (nfcEvent) => {
            resolve(nfcEvent.id);
          },
          (error) => {
            reject(error);
          }
        );
      });
    },

    // 扫码验证
    async scanQRCode() {
      try {
        const qrCode = await this.scanCode();
        const response = await mobileAccessApi.verifyQR(qrCode);

        if (response.code === 200) {
          uni.showToast({
            title: '扫码验证成功',
            icon: 'success'
          });
        }
      } catch (error) {
        uni.showToast({
          title: '扫码验证失败',
          icon: 'none'
        });
      }
    },

    // 扫码
    scanCode() {
      return new Promise((resolve, reject) => {
        uni.scanCode({
          success: (res) => {
            resolve(res.result);
          },
          fail: reject
        });
      });
    },

    // 记录访问日志
    async logAccessRecord(deviceId, result) {
      try {
        // 异步记录，不阻塞主流程
        request.post('/mobile/access/log', {
          deviceId,
          result,
          timestamp: Date.now()
        });
      } catch (error) {
        console.error('记录访问日志失败:', error);
      }
    }
  }
};
</script>

<style scoped>
.access-check {
  padding: 20px;
}

.device-list {
  margin-bottom: 30px;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.biometric-check {
  display: flex;
  justify-content: space-around;
  margin-top: 30px;
}

.biometric-check button {
  padding: 12px 24px;
  background: #007AFF;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
}
</style>
```

## 🔒 移动端安全规范

### 1. 数据加密

```javascript
// 敏感数据加密
export const encryptSensitiveData = (data) => {
  const key = process.env.VUE_APP_ENCRYPT_KEY;

  // AES加密实现
  const encrypt = (text, key) => {
    return CryptoJS.AES.encrypt(text, key).toString();
  };

  return encrypt(JSON.stringify(data), key);
};

// 生物识别数据特殊处理
export const processBiometricData = (imageData) => {
  // 1. 图像预处理
  const processedImage = preprocessImage(imageData);

  // 2. 特征提取
  const features = extractFeatures(processedImage);

  // 3. 数据加密
  return encryptSensitiveData({
    features,
    timestamp: Date.now(),
    deviceId: getDeviceId()
  });
};
```

### 2. 设备认证

```javascript
// 设备认证
export const deviceAuth = () => {
  const deviceInfo = {
    deviceId: getDeviceId(),
    platform: uni.getSystemInfoSync().platform,
    appVersion: process.env.VUE_APP_VERSION,
    timestamp: Date.now()
  };

  return request.post('/mobile/device/auth', deviceInfo);
};

// 设备心跳
export const deviceHeartbeat = () => {
  return request.post('/mobile/device/heartbeat', {
    deviceId: getDeviceId(),
    status: 'ONLINE',
    batteryLevel: getBatteryLevel(),
    timestamp: Date.now()
  });
};
```

### 3. 离线数据同步

```javascript
// 离线数据存储
export const storeOfflineData = (type, data) => {
  const offlineData = {
    type,
    data,
    timestamp: Date.now(),
    synced: false
  };

  const existingData = uni.getStorageSync('offline_data') || [];
  existingData.push(offlineData);
  uni.setStorageSync('offline_data', existingData);
};

// 同步离线数据
export const syncOfflineData = async () => {
  const offlineData = uni.getStorageSync('offline_data') || [];
  const unsyncedData = offlineData.filter(item => !item.synced);

  for (const item of unsyncedData) {
    try {
      await request.post(`/mobile/sync/${item.type}`, item.data);
      item.synced = true;
      item.syncTime = Date.now();
    } catch (error) {
      console.error('同步失败:', error);
    }
  }

  uni.setStorageSync('offline_data', offlineData);
};
```

## 📊 移动端性能优化

### 1. 网络优化

```javascript
// 请求防抖
export const debounceRequest = (func, delay = 300) => {
  let timeoutId;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func.apply(this, args), delay);
  };
};

// 请求缓存
const requestCache = new Map();

export const cachedRequest = (key, requestFunc, ttl = 60000) => {
  const cached = requestCache.get(key);
  if (cached && Date.now() - cached.timestamp < ttl) {
    return Promise.resolve(cached.data);
  }

  return requestFunc().then(data => {
    requestCache.set(key, {
      data,
      timestamp: Date.now()
    });
    return data;
  });
};

// 使用示例
export const getUserProfile = () => {
  return cachedRequest('user_profile', () => {
    return request.get('/mobile/user/profile');
  });
};
```

### 2. 图片优化

```javascript
// 图片压缩
export const compressImage = (imagePath, quality = 0.8) => {
  return new Promise((resolve, reject) => {
    plus.zip.compressImage({
      src: imagePath,
      dst: imagePath.replace(/\.(\w+)$/, '_compressed.$1'),
      quality: quality,
      width: 'auto',
      height: 'auto'
    }, () => {
      resolve(imagePath.replace(/\.(\w+)$/, '_compressed.$1'));
    }, reject);
  });
};

// 懒加载图片
export const lazyLoadImage = (src, placeholder = '/static/images/placeholder.png') => {
  return new Promise((resolve) => {
    const img = new Image();
    img.src = src;

    img.onload = () => resolve(src);
    img.onerror = () => resolve(placeholder);
    img.src = placeholder;
  });
};
```

## 🧪 移动端测试

### 1. 单元测试

```javascript
// tests/unit/api.test.js
import { mobileAccessApi } from '@/api/mobile-access';
import MockAdapter from 'axios-mock-adapter';

const mock = new MockAdapter(request);

describe('Mobile Access API', () => {
  beforeEach(() => {
    mock.reset();
  });

  test('check access success', async () => {
    const mockResponse = {
      code: 200,
      message: '验证成功',
      data: {
        access: true,
        timestamp: Date.now()
      }
    };

    mock.onPost('/mobile/access/check').reply(200, mockResponse);

    const result = await mobileAccessApi.checkAccess({
      userId: 1001,
      deviceId: 'DEV001'
    });

    expect(result.code).toBe(200);
    expect(result.data.access).toBe(true);
  });
});
```

### 2. 集成测试

```javascript
// tests/integration/access-flow.test.js
import { mobileAccessApi } from '@/api/mobile-access';

describe('Access Flow Integration', () => {
  test('complete access flow', async () => {
    // 1. 获取附近设备
    const devicesResponse = await mobileAccessApi.getNearbyDevices(
      31.2304, 121.4737, 500
    );
    expect(devicesResponse.code).toBe(200);
    expect(devicesResponse.data.length).toBeGreaterThan(0);

    // 2. 门禁验证
    const device = devicesResponse.data[0];
    const accessResponse = await mobileAccessApi.checkAccess({
      userId: 1001,
      deviceId: device.id,
      accessMode: 'FACE'
    });
    expect(accessResponse.code).toBe(200);
  });
});
```

## 📱 平台特定功能

### iOS平台

```javascript
// iOS Face ID
export const authenticateWithFaceID = () => {
  return new Promise((resolve, reject) => {
    const FingerprintAuth = plus.ios.importClass('LAContext');
    const context = new FingerprintAuth();

    if (context.canEvaluatePolicy(1)) { // LAContext.deviceOwnerAuthenticationWithBiometrics
      context.evaluatePolicy(
        1, // LAContext.deviceOwnerAuthenticationWithBiometrics
        '使用Face ID验证',
        (success, error) => {
          if (success) {
            resolve(true);
          } else {
            reject(error);
          }
        }
      );
    } else {
      reject(new Error('Face ID不可用'));
    }
  });
};
```

### Android平台

```javascript
// Android 指纹识别
export const authenticateWithFingerprint = () => {
  return new Promise((resolve, reject) => {
    const FingerprintAuth = plus.android.importClass('android.hardware.fingerprint.FingerprintManager');
    const fingerprintManager = plus.android.runtimeMainActivity().getSystemService('fingerprint');

    if (fingerprintManager.isHardwareDetected()) {
      if (fingerprintManager.hasEnrolledFingerprints()) {
        // 调用指纹识别
        resolve(true);
      } else {
        reject(new Error('未录入指纹'));
      }
    } else {
      reject(new Error('设备不支持指纹识别'));
    }
  });
};
```

## 📝 检查清单

### 开发检查

- [ ] API路径符合移动端规范
- [ ] 请求头包含必要信息
- [ ] 敏感数据加密传输
- [ ] 错误处理完善
- [ ] 离线功能支持
- [ ] 设备认证机制

### 测试检查

- [ ] 各平台兼容性测试
- [ ] 网络异常处理测试
- [ ] 性能测试
- [ ] 安全测试
- [ ] 用户体验测试

### 发布检查

- [ ] API文档更新
- [ ] 版本兼容性确认
- [ ] 性能指标达标
- [ ] 安全审计通过
- [ ] 用户测试反馈

---

**维护团队**: IOE-DREAM移动端开发团队
**更新频率**: 版本迭代时更新
**技术支持**: 项目Issue或开发团队
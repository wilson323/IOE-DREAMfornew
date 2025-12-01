#!/usr/bin/env node

/**
 * 设备管理API测试脚本
 * 用于验证设备管理后端API的功能是否正常
 */

const axios = require('axios');

// 配置
const BASE_URL = 'http://localhost:1024';
let authToken = null;

// 创建axios实例
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 测试数据
const testDevice = {
  deviceCode: 'TEST_DEVICE_001',
  deviceName: '测试设备001',
  deviceType: 'CAMERA',
  deviceBrand: '测试品牌',
  deviceModel: 'TEST-MODEL-001',
  deviceSerial: 'SN001234567890',
  areaId: 1,
  areaName: '测试区域',
  locationDesc: '测试位置描述',
  ipAddress: '192.168.1.100',
  port: 8080,
  macAddress: 'AA:BB:CC:DD:EE:FF',
  protocolType: 'TCP',
  vendorInfo: '测试供应商信息',
  contactPerson: '测试联系人',
  contactPhone: '13800138000',
  configJson: {
    resolution: '1080p',
    framerate: 30,
    compression: 'H.264'
  },
  extendInfo: {
    installationDate: '2024-01-01',
    warrantyPeriod: 24
  }
};

let createdDeviceId = null;

// 工具函数
function log(message, type = 'INFO') {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] [${type}] ${message}`);
}

function success(message) {
  log(message, 'SUCCESS');
}

function error(message) {
  log(message, 'ERROR');
}

function warn(message) {
  log(message, 'WARN');
}

// 测试函数
async function testLogin() {
  try {
    log('测试用户登录...');

    const response = await api.post('/api/auth/login', {
      loginName: 'admin',
      loginPwd: '123456'
    });

    if (response.data.code === 200) {
      authToken = response.data.data.token;
      api.defaults.headers.Authorization = `Bearer ${authToken}`;
      success('用户登录成功');
      return true;
    } else {
      error(`登录失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`登录请求失败: ${err.message}`);
    return false;
  }
}

async function testCreateDevice() {
  try {
    log('测试创建设备...');

    const response = await api.post('/api/smart/device/add', testDevice);

    if (response.data.code === 200) {
      createdDeviceId = response.data.data;
      success(`设备创建成功，ID: ${createdDeviceId}`);
      return true;
    } else {
      error(`创建设备失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`创建设备请求失败: ${err.message}`);
    return false;
  }
}

async function testGetDeviceDetail() {
  if (!createdDeviceId) {
    warn('跳过设备详情测试，没有可用的设备ID');
    return true;
  }

  try {
    log('测试获取设备详情...');

    const response = await api.get(`/api/smart/device/detail/${createdDeviceId}`);

    if (response.data.code === 200) {
      const device = response.data.data;
      success(`获取设备详情成功，设备名称: ${device.deviceName}`);

      // 验证数据完整性
      if (device.deviceCode === testDevice.deviceCode &&
          device.deviceName === testDevice.deviceName &&
          device.deviceType === testDevice.deviceType) {
        success('设备数据完整性验证通过');
      } else {
        warn('设备数据完整性验证失败');
      }
      return true;
    } else {
      error(`获取设备详情失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`获取设备详情请求失败: ${err.message}`);
    return false;
  }
}

async function testUpdateDevice() {
  if (!createdDeviceId) {
    warn('跳过设备更新测试，没有可用的设备ID');
    return true;
  }

  try {
    log('测试更新设备...');

    const updateData = {
      deviceId: createdDeviceId,
      deviceName: '测试设备001-已更新',
      deviceBrand: '更新后的品牌',
      contactPerson: '更新后的联系人'
    };

    const response = await api.post('/api/smart/device/update', updateData);

    if (response.data.code === 200) {
      success('设备更新成功');
      return true;
    } else {
      error(`更新设备失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`更新设备请求失败: ${err.message}`);
    return false;
  }
}

async function testQueryDevices() {
  try {
    log('测试分页查询设备列表...');

    const queryParams = {
      current: 1,
      pageSize: 10,
      deviceName: '测试',
      deviceType: 'CAMERA'
    };

    const response = await api.post('/api/smart/device/page', queryParams);

    if (response.data.code === 200) {
      const data = response.data.data;
      success(`查询设备列表成功，共 ${data.total} 条记录，当前页 ${data.records.length} 条`);

      // 检查是否包含我们创建的测试设备
      const testDeviceFound = data.records.some(device =>
        device.deviceId === createdDeviceId
      );

      if (testDeviceFound) {
        success('测试设备在查询结果中找到');
      } else {
        warn('测试设备在查询结果中未找到');
      }

      return true;
    } else {
      error(`查询设备列表失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`查询设备列表请求失败: ${err.message}`);
    return false;
  }
}

async function testDeviceControl() {
  if (!createdDeviceId) {
    warn('跳过设备控制测试，没有可用的设备ID');
    return true;
  }

  try {
    log('测试设备控制功能...');

    // 测试启用设备
    const enableResponse = await api.post(`/api/smart/device/enable/${createdDeviceId}`);
    if (enableResponse.data.code === 200) {
      success('设备启用成功');
    } else {
      error(`设备启用失败: ${enableResponse.data.message}`);
      return false;
    }

    // 测试设备上线
    const onlineResponse = await api.post(`/api/smart/device/online/${createdDeviceId}`);
    if (onlineResponse.data.code === 200) {
      success('设备上线成功');
    } else {
      error(`设备上线失败: ${onlineResponse.data.message}`);
      return false;
    }

    // 测试设备离线
    const offlineResponse = await api.post(`/api/smart/device/offline/${createdDeviceId}`);
    if (offlineResponse.data.code === 200) {
      success('设备离线成功');
    } else {
      error(`设备离线失败: ${offlineResponse.data.message}`);
      return false;
    }

    return true;
  } catch (err) {
    error(`设备控制请求失败: ${err.message}`);
    return false;
  }
}

async function testGetStatistics() {
  try {
    log('测试获取设备统计...');

    const response = await api.get('/api/smart/device/statistics');

    if (response.data.code === 200) {
      const stats = response.data.data;
      success(`获取设备统计成功: 总数${stats.totalDevices}, 在线${stats.onlineDevices}, 离线${stats.offlineDevices}, 故障${stats.faultDevices}`);
      return true;
    } else {
      error(`获取设备统计失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`获取设备统计请求失败: ${err.message}`);
    return false;
  }
}

async function testDeleteDevice() {
  if (!createdDeviceId) {
    warn('跳过设备删除测试，没有可用的设备ID');
    return true;
  }

  try {
    log('测试删除设备...');

    const response = await api.post('/api/smart/device/delete', createdDeviceId);

    if (response.data.code === 200) {
      success('设备删除成功');
      return true;
    } else {
      error(`删除设备失败: ${response.data.message}`);
      return false;
    }
  } catch (err) {
    error(`删除设备请求失败: ${err.message}`);
    return false;
  }
}

// 主测试函数
async function runTests() {
  log('开始设备管理API测试...');
  log('===============================');

  let allTestsPassed = true;

  // 登录测试
  const loginSuccess = await testLogin();
  if (!loginSuccess) {
    error('登录失败，无法继续测试其他API');
    process.exit(1);
  }

  // 功能测试
  const tests = [
    testCreateDevice,
    testGetDeviceDetail,
    testUpdateDevice,
    testQueryDevices,
    testDeviceControl,
    testGetStatistics,
    testDeleteDevice
  ];

  for (const test of tests) {
    try {
      const result = await test();
      if (!result) {
        allTestsPassed = false;
      }

      // 添加延迟避免请求过快
      await new Promise(resolve => setTimeout(resolve, 500));
    } catch (err) {
      error(`测试执行异常: ${err.message}`);
      allTestsPassed = false;
    }
  }

  log('===============================');
  if (allTestsPassed) {
    success('所有测试通过！');
    process.exit(0);
  } else {
    error('部分测试失败，请检查后端服务！');
    process.exit(1);
  }
}

// 运行测试
runTests().catch(err => {
  error(`测试运行失败: ${err.message}`);
  process.exit(1);
});
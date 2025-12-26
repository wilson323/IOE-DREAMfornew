# P0-3 电子地图集成前端实施指南

**📅 创建时间**: 2025-12-26
**👯‍♂️ 工作量**: 3人天
**⭐ 优先级**: P0级核心功能
**🎯 目标**: 为智慧园区管理平台集成电子地图功能

---

## 📊 功能需求概述

### 核心功能
1. **园区地图展示** - 显示园区整体布局
2. **区域标注** - 显示各功能区域（门禁点、考勤点、消费点等）
3. **设备定位** - 在地图上显示设备位置
4. **实时状态** - 显示设备在线/离线状态
5. **交互操作** - 点击设备查看详情

### 技术选型
- **地图SDK**: 百度地图API / 高德地图API（二选一）
- **前端框架**: Vue 3.4 + Composition API
- **组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia 2.x
- **HTTP客户端**: Axios 1.6.x

---

## 🏗️ 前端架构设计

### 目录结构
```
smart-admin-web-javascript/src/
├── views/
│   └── business/
│       └── map/                      # 地图模块
│           ├── index.vue            # 地图主页
│           ├── device-map.vue       # 设备地图
│           ├── area-map.vue         # 区域地图
│           └── components/          # 地图子组件
│               ├── map-marker.vue   # 地图标记
│               ├── map-info-window.vue  # 信息窗口
│               └── map-legend.vue   # 图例
├── api/
│   └── business/
│       └── map/                     # 地图API
│           └── map-api.js          # 地图接口定义
└── store/
    └── modules/
        └── map.js                   # 地图状态管理
```

---

## 📝 开发步骤

### 步骤1: 创建地图页面骨架（0.5天）
- [ ] 创建地图模块目录结构
- [ ] 创建地图主页面（index.vue）
- [ ] 配置路由和菜单
- [ ] 集成地图SDK

### 步骤2: 实现设备地图展示（1天）
- [ ] 实现设备地图组件（device-map.vue）
- [ ] 实现地图标记组件（map-marker.vue）
- [ ] 实现设备图标和状态颜色
- [ ] 实现点击查看详情功能

### 步骤3: 实现区域地图展示（0.5天）
- [ ] 实现区域地图组件（area-map.vue）
- [ ] 实现区域标注功能
- [ ] 实现区域边界绘制
- [ ] 实现区域图例（map-legend.vue）

### 步骤4: 集成后端API（0.5天）
- [ ] 创建地图API接口（map-api.js）
- [ ] 实现设备数据获取
- [ ] 实现区域数据获取
- [ ] 实现实时状态更新

### 步骤5: 优化和测试（0.5天）
- [ ] 性能优化（懒加载、缓存）
- [ ] 响应式适配
- [ ] 功能测试
- [ ] 用户体验优化

---

## 🎨 UI/UX设计要点

### 地图页面布局
```vue
<template>
  <div class="map-container">
    <!-- 顶部工具栏 -->
    <div class="map-toolbar">
      <a-space>
        <a-button type="primary" @click="showAllDevices">显示所有设备</a-button>
        <a-button @click="showOnlineDevices">在线设备</a-button>
        <a-button @click="showOfflineDevices">离线设备</a-button>
        <a-select v-model="selectedModule" placeholder="选择业务模块">
          <a-select-option value="access">门禁</a-select-option>
          <a-select-option value="attendance">考勤</a-select-option>
          <a-select-option value="consume">消费</a-select-option>
          <a-select-option value="video">视频</a-select-option>
        </a-select>
      </a-space>
    </div>

    <!-- 地图容器 -->
    <div id="map-container" class="map-content"></div>

    <!-- 图例 -->
    <div class="map-legend">
      <map-legend />
    </div>
  </div>
</template>
```

### 设备标记样式
```javascript
// 设备类型图标映射
const deviceIcons = {
  1: { icon: 'access-door', color: '#1890ff', name: '门禁设备' },
  2: { icon: 'attendance-clock', color: '#52c41a', name: '考勤设备' },
  3: { icon: 'consume-pos', color: '#faad14', name: '消费设备' },
  4: { icon: 'video-camera', color: '#722ed1', name: '视频设备' },
  5: { icon: 'visitor-qr', color: '#eb2f96', name: '访客设备' }
};

// 设备状态颜色
const statusColors = {
  1: '#52c41a',  // 在线 - 绿色
  2: '#faad14',  // 离线 - 橙色
  3: '#f5222d',  // 故障 - 红色
  4: '#d9d9d9'   // 停用 - 灰色
};
```

---

## 🔌 API接口设计

### 前端API接口
```javascript
// src/api/business/map/map-api.js

import { request } from '@/utils/request';

export const mapApi = {
  /**
   * 获取所有设备位置
   */
  getDeviceLocations() {
    return request.get('/api/device/locations');
  },

  /**
   * 获取指定区域设备
   */
  getAreaDevices(areaId) {
    return request.get('/api/device/area/' + areaId);
  },

  /**
   * 获取设备详情
   */
  getDeviceDetail(deviceId) {
    return request.get('/api/device/' + deviceId);
  },

  /**
   * 获取区域列表
   */
  getAreaList() {
    return request.get('/api/area/list');
  },

  /**
   * 获取设备实时状态
   */
  getDeviceStatus(deviceId) {
    return request.get('/api/device/' + deviceId + '/status');
  }
};
```

### 后端Controller接口
```java
@RestController
@RequestMapping("/api/map")
@Tag(name = "电子地图")
public class MapController {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @GetMapping("/device/locations")
    @Operation(summary = "获取所有设备位置")
    public ResponseDTO<List<DeviceLocationVO>> getDeviceLocations() {
        // 通过网关调用设备服务
        return gatewayServiceClient.callDeviceService(
            "/api/device/locations",
            HttpMethod.GET,
            null,
            new TypeReference<ResponseDTO<List<DeviceLocationVO>>>() {}
        );
    }

    @GetMapping("/area/{areaId}/devices")
    @Operation(summary = "获取区域设备")
    public ResponseDTO<List<DeviceVO>> getAreaDevices(@PathVariable Long areaId) {
        return gatewayServiceClient.callDeviceService(
            "/api/device/area/" + areaId,
            HttpMethod.GET,
            null,
            new TypeReference<ResponseDTO<List<DeviceVO>>>() {}
        );
    }
}
```

---

## 🎯 核心功能实现

### 1. 地图初始化
```javascript
import { onMounted, ref } from 'vue';
import { loadMapScript } from '@/utils/map-loader';

const mapInstance = ref(null);

onMounted(async () => {
  try {
    // 加载地图SDK
    await loadMapScript('YOUR_API_KEY');

    // 初始化地图
    mapInstance.value = new BMapGL.Map('map-container');
    mapInstance.value.centerAndZoom(
      new BMapGL.Point(116.404, 39.915),
      15
    );

    // 启用滚轮缩放
    mapInstance.value.enableScrollWheelZoom(true);

    // 加载设备数据
    await loadDevices();

  } catch (error) {
    console.error('地图初始化失败:', error);
    message.error('地图加载失败');
  }
});
```

### 2. 设备标记渲染
```javascript
const renderDeviceMarkers = (devices) => {
  // 清除现有标记
  mapInstance.value.clearOverlays();

  devices.forEach(device => {
    const point = new BMapGL.Point(device.longitude, device.latitude);

    // 创建自定义标记
    const marker = new BMapGL.Marker(point, {
      icon: new BMapGL.Icon(
        getDeviceIcon(device.deviceType, device.status),
        new BMapGL.Size(32, 32)
      )
    });

    // 点击事件
    marker.addEventListener('click', () => {
      showDeviceInfo(device);
    });

    // 添加到地图
    mapInstance.value.addOverlay(marker);
  });
};
```

### 3. 信息窗口展示
```javascript
const showDeviceInfo = (device) => {
  const point = new BMapGL.Point(device.longitude, device.latitude);

  const infoWindow = new BMapGL.InfoWindow(`
    <div class="device-info">
      <h4>${device.deviceName}</h4>
      <p>设备类型: ${getDeviceTypeName(device.deviceType)}</p>
      <p>设备状态: ${getDeviceStatusName(device.status)}</p>
      <p>所在区域: ${device.areaName}</p>
      <p>最后在线: ${device.lastOnlineTime}</p>
      <a-button type="link" onclick="viewDeviceDetail('${device.deviceId}')">
        查看详情
      </a-button>
    </div>
  `);

  mapInstance.value.openInfoWindow(infoWindow, point);
};
```

---

## 📦 依赖说明

### 前端依赖（package.json）
```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.0.0",
    "pinia": "^2.0.0",
    "axios": "^1.6.0",
    "ant-design-vue": "^4.0.0"
  }
}
```

### 地图SDK选择
- **百度地图API**: 适合国内使用，文档完善
  - 申请地址: https://lbsyun.baidu.com/
  - SDK加载: `https://api.map.baidu.com/api?v=1.0&type=webgl&ak=YOUR_API_KEY`

- **高德地图API**: 国内主流，功能强大
  - 申请地址: https://lbs.amap.com/
  - SDK加载: `https://webapi.amap.com/maps?v=2.0&key=YOUR_API_KEY`

**推荐**: 使用百度地图API（项目已有使用基础）

---

## ✅ 验收标准

### 功能验收
- [ ] 地图正常加载显示
- [ ] 设备标记正确显示在地图上
- [ ] 点击标记显示设备详情
- [ ] 设备状态颜色正确（在线/离线/故障）
- [ ] 区域边界正确绘制
- [ ] 筛选功能正常工作
- [ ] 响应式布局正常

### 性能验收
- [ ] 地图加载时间 < 3秒
- [ ] 标记渲染流畅（100+设备不卡顿）
- [ ] 内存占用合理（< 200MB）
- [ ] 移动端适配正常

### 代码质量
- [ ] Vue 3 Composition API规范
- [ ] 组件拆分合理
- [ ] 代码注释完整
- [ ] 无ESLint警告

---

## 🚀 实施优先级

**P0核心功能（必须完成）**:
1. 地图基础展示
2. 设备标记显示
3. 点击查看详情

**P1增强功能（可选）**:
1. 设备筛选功能
2. 实时状态更新
3. 区域标注

**P2优化功能（可选）**:
1. 路线规划
2. 热力图分析
3. 历史轨迹回放

---

**📅 预计完成时间**: 3个工作日
**👥 开发人员**: 前端工程师
**🎯 里程碑**: 每日下班前提交代码并演示进度

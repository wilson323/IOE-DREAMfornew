# 前端地图集成方案

## 技术选型

**选择：高德地图（AMap）+ Vue 3 封装库**

**理由**：
1. 高德地图在国内稳定可靠
2. 提供 Vue 3 组件封装：`@amap/amap-vue`
3. 文档完善，社区活跃

## 安装依赖

```bash
# 进入前端目录
cd smart-admin-web-javascript

# 安装高德地图 Vue 3 组件
npm install @amap/amap-vue

# 或使用 yarn
yarn add @amap/amap-vue
```

## 组件设计

### 1. DeviceMap.vue（设备地图组件）
- **位置**: `src/views/business/access/components/DeviceMap.vue`
- **功能**:
  - 显示设备位置标记
  - 支持设备详情弹窗
  - 支持设备筛选（在线/离线）
  - 支持区域边界显示

### 2. AccessTrajectory.vue（通行轨迹组件）
- **位置**: `src/views/business/access/components/AccessTrajectory.vue`
- **功能**:
  - 显示用户通行路径
  - 时间轴回放
  - 速度热力图

### 3. AreaVisualization.vue（区域可视化组件）
- **位置**: `src/views/business/access/components/AreaVisualization.vue`
- **功能**:
  - 显示区域边界
  - 区域设备统计
  - 区域权限可视化

## API 接口需求

### 获取设备位置
```
GET /api/v1/access/device-locations
Response: {
  code: 200,
  data: [
    {
      deviceId: "001",
      deviceName: "A栋大门",
      latitude: 39.908823,
      longitude: 116.397470,
      status: "online",
      areaId: "AREA_001"
    }
  ]
}
```

### 获取通行轨迹
```
GET /api/v1/access/trajectories?userId={userId}&date={date}
Response: {
  code: 200,
  data: {
    userId: "U001",
    date: "2025-12-26",
    trajectories: [
      {
        time: "08:30:00",
        location: { lat: 39.908823, lng: 116.397470 },
        deviceName: "A栋大门",
        action: "进入"
      }
    ]
  }
}
```

### 获取区域边界
```
GET /api/v1/access/area-boundaries
Response: {
  code: 200,
  data: [
    {
      areaId: "AREA_001",
      areaName: "A栋",
      boundary: [
        { lat: 39.908823, lng: 116.397470 },
        { lat: 39.909000, lng: 116.397600 },
        { lat: 39.909100, lng: 116.397400 }
      ]
    }
  ]
}
```

## 实施步骤

1. ✅ 安装高德地图 SDK
2. 创建 DeviceMap.vue 组件
3. 集成到设备管理页面
4. 创建 AccessTrajectory.vue 组件
5. 集成到通行记录页面
6. 创建 AreaVisualization.vue 组件
7. 集成到区域管理页面
8. 添加后端 API 接口
9. 测试和优化

## 注意事项

- ⚠️ 需要申请高德地图 API Key
- ⚠️ 需要在后端配置地图 Key
- ⚠️ 设备经纬度需要预先配置
- ⚠️ 区域边界需要使用多边形编辑器配置

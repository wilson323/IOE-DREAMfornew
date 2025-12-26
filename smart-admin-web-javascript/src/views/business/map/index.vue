<template>
  <div class="map-page">
    <!-- 顶部工具栏 -->
    <div class="map-toolbar">
      <a-card :bordered="false">
        <a-space wrap>
          <a-button type="primary" @click="showAllDevices">
            <template #icon><Icon icon="material-symbols:map" /></template>
            显示全部设备
          </a-button>
          <a-button @click="showOnlineDevices">
            <template #icon><Icon icon="mdi:wifi" /></template>
            在线设备
          </a-button>
          <a-button @click="showOfflineDevices">
            <template #icon><Icon icon="mdi:wifi-off" /></template>
            离线设备
          </a-button>
          <a-divider type="vertical" />
          <a-select
            v-model:value="selectedModule"
            placeholder="选择业务模块"
            style="width: 150px"
            allowClear
            @change="handleModuleChange"
          >
            <a-select-option value="access">
              <Icon icon="mdi:door-closed-lock" style="margin-right: 8px" />
              门禁设备
            </a-select-option>
            <a-select-option value="attendance">
              <Icon icon="mdi:clock-check" style="margin-right: 8px" />
              考勤设备
            </a-select-option>
            <a-select-option value="consume">
              <Icon icon="mdi:point-of-sale" style="margin-right: 8px" />
              消费设备
            </a-select-option>
            <a-select-option value="video">
              <Icon icon="mdi:cctv" style="margin-right: 8px" />
              视频设备
            </a-select-option>
            <a-select-option value="visitor">
              <Icon icon="mdi:account-search" style="margin-right: 8px" />
              访客设备
            </a-select-option>
          </a-select>
          <a-select
            v-model:value="selectedArea"
            placeholder="选择区域"
            style="width: 150px"
            allowClear
            @change="handleAreaChange"
          >
            <a-select-option v-for="area in areaList" :key="area.areaId" :value="area.areaId">
              {{ area.areaName }}
            </a-select-option>
          </a-select>
          <a-divider type="vertical" />
          <a-button @click="refreshMap">
            <template #icon><Icon icon="mdi:refresh" /></template>
            刷新
          </a-button>
        </a-space>
      </a-card>
    </div>

    <!-- 地图容器 -->
    <div class="map-container">
      <div id="baidu-map" class="map-content"></div>

      <!-- 加载状态 -->
      <div v-if="loading" class="map-loading">
        <a-spin size="large" tip="地图加载中..." />
      </div>

      <!-- 图例 -->
      <div class="map-legend">
        <a-card title="图例" size="small" :bordered="false">
          <div class="legend-item">
            <span class="legend-icon" style="background: #52c41a"></span>
            <span class="legend-text">在线</span>
          </div>
          <div class="legend-item">
            <span class="legend-icon" style="background: #faad14"></span>
            <span class="legend-text">离线</span>
          </div>
          <div class="legend-item">
            <span class="legend-icon" style="background: #f5222d"></span>
            <span class="legend-text">故障</span>
          </div>
          <div class="legend-item">
            <span class="legend-icon" style="background: #d9d9d9"></span>
            <span class="legend-text">停用</span>
          </div>
          <a-divider style="margin: 8px 0" />
          <div class="legend-item">
            <Icon icon="mdi:door-closed-lock" style="color: #1890ff" />
            <span class="legend-text">门禁</span>
          </div>
          <div class="legend-item">
            <Icon icon="mdi:clock-check" style="color: #52c41a" />
            <span class="legend-text">考勤</span>
          </div>
          <div class="legend-item">
            <Icon icon="mdi:point-of-sale" style="color: #faad14" />
            <span class="legend-text">消费</span>
          </div>
          <div class="legend-item">
            <Icon icon="mdi:cctv" style="color: #722ed1" />
            <span class="legend-text">视频</span>
          </div>
          <div class="legend-item">
            <Icon icon="mdi:account-search" style="color: #eb2f96" />
            <span class="legend-text">访客</span>
          </div>
        </a-card>
      </div>

      <!-- 统计信息 -->
      <div class="map-statistics">
        <a-card size="small" :bordered="false">
          <a-statistic-group direction="horizontal">
            <a-statistic title="设备总数" :value="statistics.total" />
            <a-statistic title="在线" :value="statistics.online" :value-style="{ color: '#52c41a' }" />
            <a-statistic title="离线" :value="statistics.offline" :value-style="{ color: '#faad14' }" />
            <a-statistic title="故障" :value="statistics.fault" :value-style="{ color: '#f5222d' }" />
          </a-statistic-group>
        </a-card>
      </div>
    </div>

    <!-- 设备详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="设备详情"
      placement="right"
      width="480"
      @close="handleDrawerClose"
    >
      <DeviceDetail v-if="selectedDevice" :device="selectedDevice" />
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import Icon from '@/components/Icon/index.vue';
import { mapApi } from '@/api/business/map/map-api';
import DeviceDetail from './components/DeviceDetail.vue';

// 地图实例
let mapInstance = null;
let markers = [];
let infoWindow = null;

// 响应式数据
const loading = ref(false);
const selectedModule = ref(undefined);
const selectedArea = ref(undefined);
const areaList = ref([]);
const deviceList = ref([]);
const selectedDevice = ref(null);
const detailDrawerVisible = ref(false);

// 统计数据
const statistics = computed(() => {
  const total = deviceList.value.length;
  const online = deviceList.value.filter(d => d.status === 1).length;
  const offline = deviceList.value.filter(d => d.status === 2).length;
  const fault = deviceList.value.filter(d => d.status === 3).length;
  return { total, online, offline, fault };
});

/**
 * 初始化地图
 */
const initMap = async () => {
  try {
    loading.value = true;

    // 加载百度地图SDK
    await loadBaiduMapScript();

    // 创建地图实例
    mapInstance = new BMapGL.Map('baidu-map');

    // 设置中心点（默认北京天安门）
    const point = new BMapGL.Point(116.404, 39.915);
    mapInstance.centerAndZoom(point, 15);

    // 启用滚轮缩放
    mapInstance.enableScrollWheelZoom(true);

    // 添加控件
    mapInstance.addControl(new BMapGL.ScaleControl());
    mapInstance.addControl(new BMapGL.ZoomControl());

    // 加载数据
    await loadAreaList();
    await loadDeviceLocations();

  } catch (error) {
    console.error('地图初始化失败:', error);
    message.error('地图加载失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载百度地图SDK
 */
const loadBaiduMapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.BMapGL) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = `https://api.map.baidu.com/api?v=1.0&type=webgl&ak=YOUR_BAIDU_MAP_AK&callback=initMapCallback`;
    script.onerror = () => reject(new Error('百度地图SDK加载失败'));

    window.initMapCallback = () => {
      resolve();
    };

    document.head.appendChild(script);
  });
};

/**
 * 加载区域列表
 */
const loadAreaList = async () => {
  try {
    const res = await mapApi.getAreaList();
    if (res.data) {
      areaList.value = res.data;
    }
  } catch (error) {
    console.error('加载区域列表失败:', error);
  }
};

/**
 * 加载设备位置
 */
const loadDeviceLocations = async () => {
  try {
    const res = await mapApi.getDeviceLocations();
    if (res.data) {
      deviceList.value = res.data;
      renderMarkers();
    }
  } catch (error) {
    console.error('加载设备位置失败:', error);
    message.error('加载设备数据失败');
  }
};

/**
 * 渲染设备标记
 */
const renderMarkers = () => {
  if (!mapInstance) return;

  // 清除现有标记
  clearMarkers();

  deviceList.value.forEach(device => {
    const point = new BMapGL.Point(device.longitude, device.latitude);

    // 创建自定义图标
    const icon = new BMapGL.Icon(
      getDeviceIcon(device.deviceType, device.status),
      new BMapGL.Size(32, 32)
    );

    const marker = new BMapGL.Marker(point, { icon });

    // 点击事件
    marker.addEventListener('click', () => {
      showDeviceInfo(device);
    });

    mapInstance.addOverlay(marker);
    markers.push(marker);
  });

  // 自动调整视野
  if (markers.length > 0) {
    const points = markers.map(marker => marker.getPosition());
    mapInstance.setViewport(points);
  }
};

/**
 * 清除所有标记
 */
const clearMarkers = () => {
  if (mapInstance) {
    mapInstance.clearOverlays();
  }
  markers = [];
};

/**
 * 获取设备图标URL
 */
const getDeviceIcon = (deviceType, status) => {
  const statusColors = {
    1: '52c41a', // 在线 - 绿色
    2: 'faad14', // 离线 - 橙色
    3: 'f5222d', // 故障 - 红色
    4: 'd9d9d9'  // 停用 - 灰色
  };

  const deviceIcons = {
    1: 'door',   // 门禁
    2: 'clock',  // 考勤
    3: 'pos',    // 消费
    4: 'camera', // 视频
    5: 'qr'      // 访客
  };

  const color = statusColors[status] || 'd9d9d9';
  const icon = deviceIcons[deviceType] || 'device';

  // 使用Ant Design图标生成的SVG
  return `data:image/svg+xml;base64,${generateSvgIcon(icon, color)}`;
};

/**
 * 生成SVG图标
 */
const generateSvgIcon = (iconType, color) => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
      <circle cx="16" cy="16" r="14" fill="#${color}" stroke="#fff" stroke-width="2"/>
      <text x="16" y="20" font-size="14" text-anchor="middle" fill="#fff">${iconType[0].toUpperCase()}</text>
    </svg>
  `;
  return btoa(svg);
};

/**
 * 显示设备信息
 */
const showDeviceInfo = (device) => {
  if (!mapInstance) return;

  const point = new BMapGL.Point(device.longitude, device.latitude);

  const content = `
    <div style="padding: 12px; min-width: 200px;">
      <h4 style="margin: 0 0 8px 0; font-size: 16px; font-weight: bold;">${device.deviceName}</h4>
      <p style="margin: 4px 0; color: #666;">设备类型: ${getDeviceTypeName(device.deviceType)}</p>
      <p style="margin: 4px 0; color: #666;">设备状态: ${getDeviceStatusName(device.status)}</p>
      <p style="margin: 4px 0; color: #666;">所在区域: ${device.areaName || '-'}</p>
      <p style="margin: 4px 0; color: #666;">设备地址: ${device.deviceAddress || '-'}</p>
      <button onclick="viewDeviceDetail('${device.deviceId}')"
              style="margin-top: 8px; padding: 4px 12px; background: #1890ff; color: #fff; border: none; border-radius: 4px; cursor: pointer;">
        查看详情
      </button>
    </div>
  `;

  if (infoWindow) {
    infoWindow.close();
  }

  infoWindow = new BMapGL.InfoWindow(content, {
    width: 250,
    height: 180,
    title: '设备信息'
  });

  mapInstance.openInfoWindow(infoWindow, point);

  // 挂载全局函数
  window.viewDeviceDetail = (deviceId) => {
    const device = deviceList.value.find(d => d.deviceId === deviceId);
    if (device) {
      selectedDevice.value = device;
      detailDrawerVisible.value = true;
    }
  };
};

/**
 * 获取设备类型名称
 */
const getDeviceTypeName = (type) => {
  const typeMap = {
    1: '门禁设备',
    2: '考勤设备',
    3: '消费设备',
    4: '视频设备',
    5: '访客设备'
  };
  return typeMap[type] || '未知设备';
};

/**
 * 获取设备状态名称
 */
const getDeviceStatusName = (status) => {
  const statusMap = {
    1: '在线',
    2: '离线',
    3: '故障',
    4: '停用'
  };
  return statusMap[status] || '未知';
};

/**
 * 显示所有设备
 */
const showAllDevices = async () => {
  selectedModule.value = undefined;
  selectedArea.value = undefined;
  await loadDeviceLocations();
};

/**
 * 显示在线设备
 */
const showOnlineDevices = async () => {
  try {
    const res = await mapApi.getDevicesByStatus(1);
    if (res.data) {
      deviceList.value = res.data;
      renderMarkers();
    }
  } catch (error) {
    message.error('加载在线设备失败');
  }
};

/**
 * 显示离线设备
 */
const showOfflineDevices = async () => {
  try {
    const res = await mapApi.getDevicesByStatus(2);
    if (res.data) {
      deviceList.value = res.data;
      renderMarkers();
    }
  } catch (error) {
    message.error('加载离线设备失败');
  }
};

/**
 * 业务模块切换
 */
const handleModuleChange = async (value) => {
  if (!value) {
    await loadDeviceLocations();
    return;
  }

  try {
    const res = await mapApi.getDevicesByModule(value);
    if (res.data) {
      deviceList.value = res.data;
      renderMarkers();
    }
  } catch (error) {
    message.error('加载设备失败');
  }
};

/**
 * 区域切换
 */
const handleAreaChange = async (value) => {
  if (!value) {
    await loadDeviceLocations();
    return;
  }

  try {
    const res = await mapApi.getAreaDevices(value);
    if (res.data) {
      deviceList.value = res.data;
      renderMarkers();
    }
  } catch (error) {
    message.error('加载区域设备失败');
  }
};

/**
 * 刷新地图
 */
const refreshMap = async () => {
  await loadDeviceLocations();
  message.success('刷新成功');
};

/**
 * 关闭抽屉
 */
const handleDrawerClose = () => {
  selectedDevice.value = null;
  detailDrawerVisible.value = false;
};

// 生命周期
onMounted(() => {
  initMap();
});

onUnmounted(() => {
  if (mapInstance) {
    mapInstance = null;
  }
});
</script>

<style scoped lang="less">
.map-page {
  height: 100%;
  display: flex;
  flex-direction: column;

  .map-toolbar {
    flex-shrink: 0;
    margin-bottom: 16px;
  }

  .map-container {
    flex: 1;
    position: relative;

    .map-content {
      width: 100%;
      height: 100%;
      border-radius: 4px;
      overflow: hidden;
    }

    .map-loading {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 1000;
    }

    .map-legend {
      position: absolute;
      top: 16px;
      right: 16px;
      z-index: 999;
      width: 150px;

      .legend-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .legend-icon {
          width: 16px;
          height: 16px;
          border-radius: 50%;
          margin-right: 8px;
        }

        .legend-text {
          font-size: 12px;
          color: #666;
        }
      }
    }

    .map-statistics {
      position: absolute;
      bottom: 16px;
      left: 16px;
      z-index: 999;
    }
  }
}
</style>
